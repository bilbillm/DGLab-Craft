param(
    [Parameter(Mandatory = $true)]
    [string]$Tag,

    [string]$Workflow = 'Publish Release',
    [int]$RunLimit = 50
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$errors = New-Object System.Collections.Generic.List[string]

function Add-Error {
    param([string]$Message)
    $errors.Add($Message) | Out-Null
    Write-Host "::error::$Message"
}

function Write-Check {
    param([string]$Message)
    Write-Host "[release-postcheck] $Message"
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
        if ($key.Length -gt 0) {
            $properties[$key] = $parts[1].Trim()
        }
    }

    return $properties
}

function Invoke-GhJson {
    param([string[]]$Arguments)

    $output = & gh @Arguments 2>$null
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return ($output | ConvertFrom-Json)
}

if ($null -eq (Get-Command gh -ErrorAction SilentlyContinue)) {
    Add-Error 'GitHub CLI is not installed; cannot run post-release checks.'
}

if (-not (Test-Path -LiteralPath 'gradle.properties')) {
    Add-Error 'gradle.properties is missing.'
}

$properties = @{}
if (Test-Path -LiteralPath 'gradle.properties') {
    $properties = Get-Properties -Path 'gradle.properties'
}

$minecraftVersion = $properties['minecraft_version']
$modVersion = $properties['mod_version']
if ([string]::IsNullOrWhiteSpace($minecraftVersion) -or [string]::IsNullOrWhiteSpace($modVersion)) {
    Add-Error 'gradle.properties must define minecraft_version and mod_version.'
}

if ($errors.Count -eq 0) {
    $expectedTags = @("v$modVersion", "v$modVersion-$minecraftVersion")
    if ($properties.ContainsKey('neoforge_version')) {
        $expectedTags += "v$modVersion-$minecraftVersion-NeoForge"
    }
    if ($expectedTags -notcontains $Tag) {
        Add-Error "Tag '$Tag' does not match this branch. Expected one of: $($expectedTags -join ', ')."
    }
    else {
        Write-Check 'Tag matches this branch version metadata.'
    }
}

if ($errors.Count -eq 0) {
    $expectedJar = "DGLabCraft-$minecraftVersion-$modVersion.jar"
    $release = Invoke-GhJson -Arguments @('release', 'view', $Tag, '--json', 'tagName,url,assets,isDraft')
    if ($null -eq $release) {
        Add-Error "GitHub Release '$Tag' was not found."
    }
    else {
        Write-Check "GitHub Release exists: $($release.url)"
        if ($release.isDraft) {
            Add-Error "GitHub Release '$Tag' is still a draft."
        }

        $jarAssets = @($release.assets | Where-Object {
            $_.name -like '*.jar' -and
            $_.name -notlike '*-slim.jar' -and
            $_.name -notlike '*-sources.jar' -and
            $_.name -notlike '*-javadoc.jar' -and
            $_.name -notlike '*-dev.jar' -and
            $_.name -notlike '*-all.jar'
        })
        if ($jarAssets.name -notcontains $expectedJar) {
            Add-Error "GitHub Release does not contain expected jar '$expectedJar'. Found: $(@($jarAssets.name) -join ', ')."
        }
        else {
            Write-Check "GitHub Release jar asset is current: $expectedJar"
        }
    }
}

if ($errors.Count -eq 0) {
    $runs = Invoke-GhJson -Arguments @('run', 'list', '--workflow', $Workflow, '--limit', "$RunLimit", '--json', 'databaseId,displayTitle,event,headBranch,status,conclusion,url,createdAt')
    if ($null -eq $runs) {
        Add-Error "Could not read GitHub Actions runs for workflow '$Workflow'."
    }
    else {
        $matchingRun = $null
        foreach ($run in @($runs)) {
            if ($run.status -ne 'completed' -or $run.conclusion -ne 'success') {
                continue
            }

            $matchesTag = $run.headBranch -eq $Tag -or $run.displayTitle -like "*$Tag*"
            $log = $null
            if (-not $matchesTag -or $run.event -eq 'workflow_dispatch') {
                $log = (& gh run view $run.databaseId --log 2>$null) -join "`n"
                if ($LASTEXITCODE -eq 0 -and $log -match [regex]::Escape("RELEASE_TAG: $Tag")) {
                    $matchesTag = $true
                }
            }

            if (-not $matchesTag) {
                continue
            }

            if ($null -eq $log) {
                $log = (& gh run view $run.databaseId --log 2>$null) -join "`n"
            }
            if ($LASTEXITCODE -ne 0 -or $log -notmatch 'Upload jar to CurseForge') {
                continue
            }

            $matchingRun = $run
            break
        }

        if ($null -eq $matchingRun) {
            Add-Error "No successful '$Workflow' run found for '$Tag' with a CurseForge upload step in the latest $RunLimit runs."
        }
        else {
            Write-Check "Publish workflow succeeded for '$Tag': $($matchingRun.url)"
        }
    }
}

if ($errors.Count -gt 0) {
    Write-Host "[release-postcheck] Failed with $($errors.Count) error(s)."
    exit 1
}

Write-Host '[release-postcheck] Passed.'
