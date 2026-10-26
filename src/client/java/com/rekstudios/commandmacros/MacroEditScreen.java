package com.rekstudios.commandmacros;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class MacroEditScreen extends Screen {
    private static final int COMMAND_FIELD_COUNT = 6;

    private final Screen parent;
    private final MacroDefinition editingMacro;
    private EditBox nameField;
    private final List<EditBox> commandFields = new ArrayList<>();
    private Button shortcutButton;
    private int shortcutKey = -1;
    private boolean waitingForShortcut = false;
    private String errorMessage = "";

    public MacroEditScreen(Screen parent, MacroDefinition editingMacro) {
        super(Component.literal(editingMacro == null ? "Create Macro" : "Edit Macro"));
        this.parent = parent;
        this.editingMacro = editingMacro;
        if (editingMacro != null) shortcutKey = editingMacro.keyCode;
    }

    private int bottomY() {
        return this.height - 26;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 180;

        nameField = new EditBox(this.font, left, 54, 360, 20, Component.literal("Macro name"));
        nameField.setMaxLength(64);
        if (editingMacro != null && editingMacro.name != null) nameField.setValue(editingMacro.name);
        this.addRenderableWidget(nameField);

        commandFields.clear();
        for (int i = 0; i < COMMAND_FIELD_COUNT; i++) {
            EditBox commandField = new EditBox(this.font, left, 92 + i * 22, 360, 20,
                    Component.literal("Command " + (i + 1)));
            commandField.setMaxLength(32500);
            if (editingMacro != null && editingMacro.commands != null && i < editingMacro.commands.size()) {
                String saved = editingMacro.commands.get(i);
                if (saved != null) commandField.setValue(saved);
            }
            commandFields.add(commandField);
            this.addRenderableWidget(commandField);
        }

        shortcutButton = this.addRenderableWidget(Button.builder(shortcutLabel(), button -> {
            waitingForShortcut = true;
            errorMessage = "Press a keyboard key now (Esc cancels).";
            shortcutButton.setMessage(Component.literal("Press a key..."));
        }).bounds(left, 230, 174, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveMacro())
                .bounds(left + 195, bottomY(), 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> returnToParent())
                .bounds(left + 280, bottomY(), 80, 20).build());
    }

    private Component shortcutLabel() {
        return Component.literal("Shortcut: " + CommandMacrosClient.keyName(shortcutKey));
    }

    private void saveMacro() {
        List<String> commands = new ArrayList<>();
        for (EditBox field : commandFields) {
            String value = field.getValue().trim();
            if (!value.isBlank()) commands.add(value);
        }
        if (commands.isEmpty()) {
            errorMessage = "Enter at least one command before saving.";
            return;
        }

        String name = nameField.getValue().trim();
        if (name.isBlank()) name = "Macro " + (CommandMacrosClient.MACROS.size() + 1);

        if (editingMacro == null) {
            CommandMacrosClient.MACROS.add(new MacroDefinition(name, commands, shortcutKey));
        } else {
            editingMacro.name = name;
            editingMacro.commands = commands;
            editingMacro.keyCode = shortcutKey;
        }
        CommandMacrosClient.saveMacros();
        this.minecraft.gui.setScreen(new MacroListScreen(0));
    }

    private void returnToParent() {
        waitingForShortcut = false;
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (waitingForShortcut) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                waitingForShortcut = false;
                errorMessage = "Shortcut capture cancelled.";
                if (shortcutButton != null) shortcutButton.setMessage(shortcutLabel());
                return true;
            }
            if (event.key() >= 0) {
                shortcutKey = event.key();
                waitingForShortcut = false;
                errorMessage = "Shortcut set. Save to keep it.";
                if (shortcutButton != null) shortcutButton.setMessage(shortcutLabel());
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int left = this.width / 2 - 180;
        graphics.text(this.font, editingMacro == null ? "Create Command Macro" : "Edit Command Macro",
                left, 25, 0xFFFFFFFF, true);
        graphics.text(this.font, "Name", left, 42, 0xFFBDBDBD, true);
        graphics.text(this.font, "Commands run top to bottom. Slash is optional.",
                left, 78, 0xFFBDBDBD, true);
                if (!errorMessage.isBlank()) {
            graphics.text(this.font, errorMessage, left + 184, 232, 0xFFFFD36A, true);
        }
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
