param(
    [Parameter(Mandatory = $true)]
    [string]$Tag,

    [switch]$RequireGitHubAsset,
    [switch]$SkipGitHubReleaseCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$errors = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]

function Add-Error {
    param([string]$Message)
    $errors.Add($Message) | Out-Null
    Write-Host "::error::$Message"
}

function Add-Warning {
    param([string]$Message)
    $warnings.Add($Message) | Out-Null
    Write-Host "::warning::$Message"
}

function Write-Check {
    param([string]$Message)
    Write-Host "[release-check] $Message"
}

function Get-Properties {
    param([string]$Path)

    $properties = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^\s*#' -or $line -notmatch '=') {
            continue
        }

        $parts = $line -split '=', 2
        $key = $parts[0].Trim()
        if ($key.Length -eq 0) {
            continue
        }

        $properties[$key] = $parts[1].Trim()
    }

    return $properties
}

function Test-TextFile {
    param([string]$Path)

    try {
        $bytes = [System.IO.File]::ReadAllBytes($Path)
        if ($bytes.Length -eq 0) {
            return $true
        }

        $sampleLength = [Math]::Min($bytes.Length, 4096)
        for ($i = 0; $i -lt $sampleLength; $i++) {
            if ($bytes[$i] -eq 0) {
                return $false
            }
        }

        return $true
    }
    catch {
        Add-Warning "Could not read tracked file '$Path' while scanning for tokens: $($_.Exception.Message)"
        return $false
    }
}

function Invoke-GhJson {
    param([string[]]$Arguments)

    $output = & gh @Arguments 2>$null
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return ($output | ConvertFrom-Json)
}

$repoRoot = (Resolve-Path -LiteralPath '.').Path
Write-Check "Checking release '$Tag' in $repoRoot"

if (-not (Test-Path -LiteralPath 'gradle.properties')) {
    Add-Error 'gradle.properties is missing.'
}
else {
    $properties = Get-Properties -Path 'gradle.properties'
    $minecraftVersion = $properties['minecraft_version']
    $modVersion = $properties['mod_version']

    if ([string]::IsNullOrWhiteSpace($minecraftVersion)) {
        Add-Error 'gradle.properties is missing minecraft_version.'
    }
    if ([string]::IsNullOrWhiteSpace($modVersion)) {
        Add-Error 'gradle.properties is missing mod_version.'
    }

    if (-not [string]::IsNullOrWhiteSpace($minecraftVersion) -and -not [string]::IsNullOrWhiteSpace($modVersion)) {
        $expectedTags = @("v$modVersion-$minecraftVersion")
        if ($properties.ContainsKey('neoforge_version')) {
            $expectedTags += "v$modVersion-$minecraftVersion-NeoForge"
        }

        if ($expectedTags -notcontains $Tag) {
            Add-Error "Tag '$Tag' does not match gradle.properties. Expected one of: $($expectedTags -join ', ')."
        }
        else {
            Write-Check "Tag matches gradle.properties."
        }

        $expectedJar = "DGLabCraft-$minecraftVersion-$modVersion.jar"
        $expectedJarPath = Join-Path 'build/libs' $expectedJar
        Write-Check "Expected release jar name: $expectedJarPath"

        if (Test-Path -LiteralPath 'build/libs') {
            $jarCandidates = Get-ChildItem -LiteralPath 'build/libs' -File -Filter '*.jar' |
                Where-Object {
                    $_.Name -notlike '*-slim.jar' -and
                    $_.Name -notlike '*-sources.jar' -and
                    $_.Name -notlike '*-javadoc.jar' -and
                    $_.Name -notlike '*-dev.jar' -and
                    $_.Name -notlike '*-all.jar'
                }

            if ($jarCandidates.Count -gt 0 -and ($jarCandidates.Name -notcontains $expectedJar)) {
                Add-Error "build/libs contains release jar candidates, but not '$expectedJar'. Found: $($jarCandidates.Name -join ', ')."
            }
        }

        $readmeFiles = @('README.md', 'README_EN.md') | Where-Object { Test-Path -LiteralPath $_ }
        if ($readmeFiles.Count -eq 0) {
            Add-Warning 'No README.md or README_EN.md found for release link checks.'
        }
        else {
            $readmeText = ($readmeFiles | ForEach-Object { Get-Content -LiteralPath $_ -Raw }) -join "`n"
            if ($readmeText -notmatch [regex]::Escape($Tag) -and $readmeText -notmatch [regex]::Escape($expectedJar)) {
                Add-Error "README files do not mention '$Tag' or '$expectedJar'."
            }
            else {
                Write-Check 'README release references look current.'
            }
        }
    }
}

if (-not (Test-Path -LiteralPath 'changelog.txt')) {
    Add-Error 'changelog.txt is missing.'
}
elseif ([string]::IsNullOrWhiteSpace((Get-Content -LiteralPath 'changelog.txt' -Raw))) {
    Add-Error 'changelog.txt is empty.'
}
else {
    Write-Check 'changelog.txt is present.'
}

$workflowPath = '.github/workflows/publish-release.yml'
if (-not (Test-Path -LiteralPath $workflowPath)) {
    Add-Error "$workflowPath is missing."
}
else {
    $workflow = Get-Content -LiteralPath $workflowPath -Raw
    foreach ($required in @('CF_API_TOKEN', '1538053', 'curseforge-upload')) {
        if ($workflow -notmatch [regex]::Escape($required)) {
            Add-Error "$workflowPath does not contain required release workflow marker '$required'."
        }
    }
    Write-Check 'CurseForge workflow markers checked.'
}

if (-not $SkipGitHubReleaseCheck) {
    $ghCommand = Get-Command gh -ErrorAction SilentlyContinue
    if ($null -eq $ghCommand) {
        Add-Warning 'GitHub CLI is not installed; skipping GitHub Release checks.'
    }
    else {
        $release = Invoke-GhJson -Arguments @('release', 'view', $Tag, '--json', 'tagName,assets')
        if ($null -eq $release) {
            Add-Warning "Could not read GitHub Release '$Tag' with gh; skipping remote release checks."
        }
        else {
            Write-Check "GitHub Release '$Tag' exists."
            $jarAssets = @($release.assets | Where-Object { $_.name -like '*.jar' })
            if ($jarAssets.Count -eq 0) {
                if ($RequireGitHubAsset) {
                    Add-Error "GitHub Release '$Tag' has no jar asset."
                }
                else {
                    Add-Warning "GitHub Release '$Tag' has no jar asset yet."
                }
            }
            else {
                Write-Check "GitHub Release has jar asset(s): $(@($jarAssets.name) -join ', ')"
            }
        }
    }
}

$trackedFiles = & git ls-files
if ($LASTEXITCODE -ne 0) {
    Add-Warning 'Could not list tracked files; skipping token scan.'
}
else {
    $excludedPathPrefixes = @(
        'build/',
        '.gradle/',
        'run/',
        'logs/',
        'crash-reports/',
        'saves/',
        'run-data/'
    )
    $excludedExtensions = @('.jar', '.zip', '.png', '.jpg', '.jpeg', '.gif', '.class', '.keystore', '.jks')
    $tokenPattern = '(?i)(api[_-]?token|curseforge[_-]?token|cf[_-]?api[_-]?token|secret|password|api[_-]?key)\s*[:=]\s*["'']?([A-Za-z0-9_\-]{20,})'

    foreach ($file in $trackedFiles) {
        $normalized = $file -replace '\\', '/'
        if ($excludedPathPrefixes | Where-Object { $normalized.StartsWith($_) }) {
            continue
        }

        $extension = [System.IO.Path]::GetExtension($file).ToLowerInvariant()
        if ($excludedExtensions -contains $extension) {
            continue
        }

        if (-not (Test-Path -LiteralPath $file) -or -not (Test-TextFile -Path $file)) {
            continue
        }

        $content = Get-Content -LiteralPath $file -Raw
        foreach ($match in [regex]::Matches($content, $tokenPattern)) {
            $line = ($content.Substring(0, $match.Index) -split "`n").Count
            $value = $match.Groups[2].Value
            $allowedSecretReference =
                $match.Value -match '\$\{\{\s*secrets\.' -or
                $value -match '^(CHANGE_ME|REPLACE_ME|placeholder|example)$'

            if (-not $allowedSecretReference) {
                Add-Error "Possible committed token in ${file}:$line. Use repository secrets instead."
            }
        }
    }

    Write-Check 'Tracked-file token scan completed.'
}

if ($errors.Count -gt 0) {
    Write-Host "[release-check] Failed with $($errors.Count) error(s) and $($warnings.Count) warning(s)."
    exit 1
}

Write-Host "[release-check] Passed with $($warnings.Count) warning(s)."
