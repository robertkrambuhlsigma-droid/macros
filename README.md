# Command Macros — Fabric for Minecraft Java 26.2

A client-side Fabric mod that gives you a Bedrock-inspired command macro menu.

## Features

- Press **F6** to open the saved macro menu (the key can also be changed under **Options → Controls → Key Binds**).
- Create, edit, delete, and run named macros in-game.
- Each macro can contain up to **6 commands**, run from top to bottom with a short delay between commands.
- Assign a keyboard shortcut to each macro in the editor.
- Macros are saved to `config/commandmacros.json`, so they remain after restarting the game.
- A leading `/` is optional.

## Requirements

- Minecraft **Java Edition 26.2**
- Fabric Loader **0.19.5 or newer**
- Fabric API for Minecraft 26.2
- Java Development Kit **25 or newer** to build the project

This is a client-side quality-of-life tool. It sends commands as your current player; it does **not** give operator permissions, bypass server permissions, or grant access to commands that your world/server disallows. Use it only where you are allowed to run those commands.

## Build on Windows

1. Install a JDK 25 or newer and make sure `java -version` reports it in Command Prompt.
2. Extract this ZIP to a normal folder.
3. Double-click `build.bat`.
4. The script downloads Gradle 9.5.1 if it is not already in the project folder, then builds the mod.
5. Find `commandmacros-1.0.0.jar` in `build/libs/` and copy it into the `mods` folder for your **Fabric 26.2** Minecraft installation.
6. Make sure the matching Fabric API JAR is also in that instance's `mods` folder, then launch the Fabric profile.

The first build needs an internet connection to download Gradle and the Fabric/Minecraft build dependencies. The build script never uploads or publishes the mod.

## In-game use

1. Join a world or server and press **F6**.
2. Select **+ Add Macro**.
3. Give the macro a name, enter one or more commands, and optionally click **Shortcut** then press the key you want.
4. Click **Save**.
5. Click **Run** in the menu, or use the assigned key while the game world is active.

Commands execute in the order shown. Keep shortcuts unique; if multiple macros share a key, only the last matching macro in the list is likely to remain queued. Starting a new macro replaces any commands still queued by the previous one.

## Source/build note

This ZIP contains the mod source and Windows build script, not a precompiled JAR. The execution environment used to prepare it has Java 21 and no outbound package-network access, whereas Minecraft 26.2 mod builds require Java 25 and Gradle/Fabric dependencies. Run `build.bat` on a Windows machine with JDK 25+ and internet access to produce the installable JAR.
