package com.rekstudios.commandmacros;

import java.util.ArrayList;
import java.util.List;

public final class MacroDefinition {
    public String name;
    public List<String> commands;
    /** GLFW keyboard key code, or -1 if no shortcut is assigned. */
    public int keyCode;

    public MacroDefinition() {
        this("New Macro", new ArrayList<>(), -1);
    }

    public MacroDefinition(String name, List<String> commands, int keyCode) {
        this.name = name == null || name.isBlank() ? "New Macro" : name;
        this.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
        this.keyCode = keyCode;
    }
}
