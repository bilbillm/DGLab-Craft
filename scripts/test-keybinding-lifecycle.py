"""Exercise production key diagnostics with a minimal client lifecycle fixture.

Compilation against the real Minecraft APIs is checked by each branch's build.
This fixture checks that a menu tick does not consume the later in-world warning.
"""
from pathlib import Path
import argparse
import subprocess
import tempfile


def check(project: Path) -> None:
    package = Path("src/main/java/com/lumoren/dglabcraft")
    helper = project / package / "KeyBindingDiagnostics.java"
    original = not helper.exists()
    target = helper if helper.exists() else project / package / "ClientModEvents.java"
    source = target.read_text(encoding="utf-8")
    old_key = "import net.minecraft.client.settings.KeyBinding;" in source
    key_type = "KeyBinding" if old_key else "KeyMapping"
    key_package = "net.minecraft.client.settings" if old_key else "net.minecraft.client"
    fixtures = {
        "com/mojang/blaze3d/platform/InputConstants.java": """package com.mojang.blaze3d.platform;
public class InputConstants { public enum Type { KEYSYM } }
""",
        "org/lwjgl/glfw/GLFW.java": "package org.lwjgl.glfw; public class GLFW { public static final int GLFW_KEY_K=75; }",
        "org/slf4j/Logger.java": "package org.slf4j; public interface Logger { default void info(String s,Object... args){} default void warn(String s,Object... args){} }",
        "org/slf4j/LoggerFactory.java": "package org.slf4j; public class LoggerFactory { public static Logger getLogger(String s){return new Logger(){};} }",
        "net/fabricmc/fabric/api/client/keybinding/v1/KeyBindingHelper.java": "package net.fabricmc.fabric.api.client.keybinding.v1; public class KeyBindingHelper { public static void registerKeyBinding(net.minecraft.client.KeyMapping k){} }",
        "net/minecraft/network/chat/TranslatableComponent.java": """
package net.minecraft.network.chat;
public class TranslatableComponent {
    private final String value;
    public TranslatableComponent(String value,Object... args){this.value=value;}
    public String getString(){return value;}
}
""",
        "net/minecraft/network/chat/Component.java": "package net.minecraft.network.chat; public class Component { public static TranslatableComponent translatable(String s,Object... args){return new TranslatableComponent(s,args);} }",
    }
    for name in ["net/minecraft/util/ChatComponentTranslation", "net/minecraft/util/text/TextComponentTranslation", "net/minecraft/util/text/TranslationTextComponent"]:
        pkg, cls = name.rsplit("/", 1)
        fixtures[name + ".java"] = f"package {pkg.replace('/', '.')}; public class {cls} extends net.minecraft.network.chat.TranslatableComponent {{ public {cls}(String s,Object... args){{super(s,args);}} }}"
    fixtures[key_package.replace(".", "/") + "/" + key_type + ".java"] = f"""
package {key_package};
public class {key_type} {{
    private final String name;
    public {key_type}(String name,com.mojang.blaze3d.platform.InputConstants.Type t,int k,String c){{this.name=name;}}
    public {key_type}(String name,int k,String c){{this.name=name;}}
    public String getName(){{return name;}}
    public String getKeyDescription(){{return name;}}
    public int getKeyCode(){{return 75;}}
    public boolean isUnbound(){{return false;}}
    public {key_type} getDefaultKey(){{return this;}}
    public net.minecraft.network.chat.TranslatableComponent getTranslatedKeyMessage(){{return new net.minecraft.network.chat.TranslatableComponent("K");}}
}}
"""
    if old_key:
        fixtures["net/minecraft/client/KeyMapping.java"] = "package net.minecraft.client; public class KeyMapping extends net.minecraft.client.settings.KeyBinding { public KeyMapping(String n,int k,String c){super(n,k,c);} }"
    fixtures["net/minecraft/client/Minecraft.java"] = f"""
package net.minecraft.client;
import {key_package}.{key_type};
public class Minecraft {{
    public static int messages;
    public Options options = new Options(), gameSettings = options;
    public Player player, thePlayer;
    public Gui gui = new Gui();
    public static class Options {{ public {key_type}[] keyMappings, keyBindings; }}
    public static class Player {{
        public void displayClientMessage(Object text,boolean actionbar){{messages++;}}
        public void sendMessage(Object text){{messages++;}}
        public void addChatMessage(Object text){{messages++;}}
    }}
    public static class Gui {{
        public Gui hud=this;
        public Gui getChat(){{return this;}}
        public void addMessage(Object text){{messages++;}}
        public void addClientSystemMessage(Object text){{messages++;}}
    }}
}}
"""
    call = "ClientModEvents.checkKeyConflicts(mc);" if original else "KeyBindingDiagnostics.check(mc, own);"
    own = "ClientModEvents.OPEN_SETTINGS_KEY.get()" if original else f'new {key_type}("own",75,"fixture")'
    fixtures["com/lumoren/dglabcraft/KeyLifecycleFixture.java"] = f"""
package com.lumoren.dglabcraft;
import net.minecraft.client.Minecraft;
import {key_package}.{key_type};
public class KeyLifecycleFixture {{
    public static void main(String[] args) {{
        Minecraft mc=new Minecraft();
        {key_type} own={own};
        mc.options.keyMappings=new {key_type}[]{{own,new {key_type}("other",75,"fixture")}};
        mc.options.keyBindings=mc.options.keyMappings;
        {call}
        if(Minecraft.messages!=0) throw new AssertionError("Warning emitted without a player");
        mc.player=new Minecraft.Player();mc.thePlayer=mc.player;
        {call}
        if(Minecraft.messages!=1) throw new AssertionError("Menu tick consumed the in-world warning");
        {call}
        if(Minecraft.messages!=1) throw new AssertionError("Warning repeated on every tick");
    }}
}}
"""
    fixtures["com/lumoren/dglabcraft/" + target.name] = source
    with tempfile.TemporaryDirectory(prefix="dglab-key-lifecycle-") as tmp:
        directory = Path(tmp)
        for name, content in fixtures.items():
            path = directory / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        classes = directory / "classes"
        command = ["javac", "-encoding", "UTF-8", "-d", str(classes)] + [str(p) for p in directory.rglob("*.java")]
        for invocation in [command, ["java", "-cp", str(classes), "com.lumoren.dglabcraft.KeyLifecycleFixture"]]:
            result = subprocess.run(invocation, capture_output=True)
            if result.returncode:
                raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    print(f"PASS {project.name}: menu -> player -> repeated tick")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("projects", nargs="+", type=Path)
    args = parser.parse_args()
    for project in args.projects:
        check(project.resolve())
