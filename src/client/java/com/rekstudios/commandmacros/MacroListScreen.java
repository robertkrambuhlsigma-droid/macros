package com.rekstudios.commandmacros;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class MacroListScreen extends Screen {
    private static final int PAGE_SIZE = 6;
    private final int requestedPage;
    private int page;

    public MacroListScreen(int page) {
        super(Component.literal("Command Macros"));
        this.requestedPage = Math.max(0, page);
    }

    private int bottomY() {
        return this.height - 28;
    }

    @Override
    protected void init() {
        int pageCount = Math.max(1, (CommandMacrosClient.MACROS.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(requestedPage, pageCount - 1);

        int left = this.width / 2 - 220;
        int rowTop = 60;
        int firstIndex = page * PAGE_SIZE;
        int lastIndex = Math.min(firstIndex + PAGE_SIZE, CommandMacrosClient.MACROS.size());

        for (int index = firstIndex; index < lastIndex; index++) {
            final MacroDefinition macro = CommandMacrosClient.MACROS.get(index);
            int y = rowTop + (index - firstIndex) * 30;

            this.addRenderableWidget(Button.builder(Component.literal("Run"), button -> {
                CommandMacrosClient.runMacro(Minecraft.getInstance(), macro);
            }).bounds(left, y, 54, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Edit"), button -> {
                this.minecraft.gui.setScreen(new MacroEditScreen(this, macro));
            }).bounds(left + 59, y, 54, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
                CommandMacrosClient.MACROS.remove(macro);
                CommandMacrosClient.saveMacros();
                this.minecraft.gui.setScreen(new MacroListScreen(page));
            }).bounds(left + 118, y, 60, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("+ Add Macro"), button -> {
            this.minecraft.gui.setScreen(new MacroEditScreen(this, null));
        }).bounds(left, bottomY(), 95, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            this.minecraft.gui.setScreen(new MacroListScreen(Math.max(0, page - 1)));
        }).bounds(left + 100, bottomY(), 62, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            this.minecraft.gui.setScreen(new MacroListScreen(Math.min(pageCount - 1, page + 1)));
        }).bounds(left + 167, bottomY(), 52, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(left + 345, bottomY(), 75, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, "Command Macros", this.width / 2 - 50, 25, 0xFFFFFFFF, true);
        graphics.text(this.font, "F6 opens this menu  |  Click Run or use a macro's shortcut key", this.width / 2 - 166, 44, 0xFFBDBDBD, true);

        List<MacroDefinition> macros = CommandMacrosClient.MACROS;
        if (macros.isEmpty()) {
            graphics.text(this.font, "No macros yet. Click + Add Macro to create one.", this.width / 2 - 135, 160, 0xFFFFFFFF, true);
        } else {
            int firstIndex = page * PAGE_SIZE;
            int lastIndex = Math.min(firstIndex + PAGE_SIZE, macros.size());
            int left = this.width / 2 - 220;
            for (int index = firstIndex; index < lastIndex; index++) {
                MacroDefinition macro = macros.get(index);
                int y = 60 + (index - firstIndex) * 30;
                String shortcut = macro.keyCode < 0 ? "No key" : "Key: " + CommandMacrosClient.keyName(macro.keyCode);
                graphics.text(this.font, crop(macro.name, 30), left + 188, y, 0xFFFFFFFF, true);
                graphics.text(this.font, crop(commandPreview(macro), 34), left + 188, y + 10, 0xFFBDBDBD, true);
                graphics.text(this.font, shortcut, left + 188, y + 20, 0xFF72D6FF, true);
            }
        }

        int count = Math.max(1, (macros.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        graphics.text(this.font, (page + 1) + " / " + count, this.width / 2 - 26, bottomY() + 6, 0xFFFFFFFF, true);
    }

    private static String commandPreview(MacroDefinition macro) {
        if (macro.commands == null || macro.commands.isEmpty()) return "No commands saved";
        long nonEmpty = macro.commands.stream().filter(command -> command != null && !command.isBlank()).count();
        String first = macro.commands.stream().filter(command -> command != null && !command.isBlank()).findFirst().orElse("");
        return CommandMacrosClient.stripLeadingSlash(first) + (nonEmpty > 1 ? "  ( +" + (nonEmpty - 1) + " more )" : "");
    }

    private static String crop(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
