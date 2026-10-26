package com.rekstudios.commandmacros;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class CommandMacrosClient implements ClientModInitializer {
    public static final String MOD_ID = "commandmacros";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final List<MacroDefinition> MACROS = new ArrayList<>();

    private static final Queue<String> COMMAND_QUEUE = new ArrayDeque<>();
    private static final Map<Integer, Boolean> PREVIOUS_KEY_STATE = new HashMap<>();
    private static KeyMapping openMenuKey;
    private static int commandDelay = 0;

    @Override
    public void onInitializeClient() {
        MACROS.addAll(MacroStorage.load());

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "general")
        );
        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.commandmacros.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(CommandMacrosClient::onClientTick);
        LOGGER.info("Command Macros loaded {} saved macros", MACROS.size());
    }

    private static void onClientTick(Minecraft client) {
        while (openMenuKey.consumeClick()) {
            if (client.player != null && client.screen == null) {
                client.gui.setScreen(new MacroListScreen(0));
            }
        }

        if (client.player == null || client.getConnection() == null) {
            COMMAND_QUEUE.clear();
            PREVIOUS_KEY_STATE.clear();
            commandDelay = 0;
            return;
        }

        if (client.screen == null) {
            pollMacroShortcuts(client);
        }

        if (commandDelay > 0) {
            commandDelay--;
            return;
        }

        String nextCommand = COMMAND_QUEUE.poll();
        if (nextCommand != null && !nextCommand.isBlank()) {
            client.getConnection().sendCommand(stripLeadingSlash(nextCommand));
            commandDelay = 3;
        }
    }

    private static void pollMacroShortcuts(Minecraft client) {
        long windowHandle = client.getWindow().getWindow();
        for (MacroDefinition macro : MACROS) {
            if (macro.keyCode < 0) continue;

            boolean down = GLFW.glfwGetKey(windowHandle, macro.keyCode) == GLFW.GLFW_PRESS;
            boolean wasDown = PREVIOUS_KEY_STATE.getOrDefault(macro.keyCode, false);
            if (down && !wasDown) {
                runMacro(client, macro);
            }
            PREVIOUS_KEY_STATE.put(macro.keyCode, down);
        }
    }

    public static void runMacro(Minecraft client, MacroDefinition macro) {
        if (client.player == null || client.getConnection() == null) {
            return;
        }

        COMMAND_QUEUE.clear();
        if (macro.commands != null) {
            for (String command : macro.commands) {
                if (command != null && !command.isBlank()) {
                    COMMAND_QUEUE.add(command.trim());
                }
            }
        }
        commandDelay = 0;

        if (COMMAND_QUEUE.isEmpty()) {
            client.player.sendSystemMessage(Component.literal("[Macros] This macro has no commands yet."));
            return;
        }

        client.player.sendSystemMessage(Component.literal(
                "[Macros] Running \"" + macro.name + "\" (" + COMMAND_QUEUE.size() + " command(s))"
        ));
    }

    public static void saveMacros() {
        MacroStorage.save(MACROS);
    }

    public static String stripLeadingSlash(String command) {
        String trimmed = command == null ? "" : command.trim();
        return trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
    }

    public static String keyName(int keyCode) {
        if (keyCode < 0) return "None";
        String glfwName = GLFW.glfwGetKeyName(keyCode, 0);
        if (glfwName != null && !glfwName.isBlank()) {
            return glfwName.toUpperCase(java.util.Locale.ROOT);
        }
        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGE UP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGE DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT ARROW";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT ARROW";
            case GLFW.GLFW_KEY_UP -> "UP ARROW";
            case GLFW.GLFW_KEY_DOWN -> "DOWN ARROW";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LEFT SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RIGHT SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LEFT CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RIGHT CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LEFT ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RIGHT ALT";
            default -> "KEY " + keyCode;
        };
    }
}
