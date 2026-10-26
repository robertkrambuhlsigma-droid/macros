package com.rekstudios.commandmacros;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MacroStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MACRO_LIST_TYPE = new TypeToken<List<MacroDefinition>>() {}.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("commandmacros.json");

    private MacroStorage() {}

    public static List<MacroDefinition> load() {
        if (!Files.exists(FILE)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            List<MacroDefinition> loaded = GSON.fromJson(reader, MACRO_LIST_TYPE);
            if (loaded == null) return new ArrayList<>();

            loaded.removeIf(macro -> macro == null);
            for (MacroDefinition macro : loaded) {
                if (macro.name == null || macro.name.isBlank()) macro.name = "Unnamed Macro";
                if (macro.commands == null) macro.commands = new ArrayList<>();
                if (macro.keyCode < -1) macro.keyCode = -1;
            }
            return loaded;
        } catch (Exception exception) {
            CommandMacrosClient.LOGGER.error("Could not read command macros from {}", FILE, exception);
            return new ArrayList<>();
        }
    }

    public static void save(List<MacroDefinition> macros) {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(macros, MACRO_LIST_TYPE, writer);
            }
        } catch (IOException exception) {
            CommandMacrosClient.LOGGER.error("Could not save command macros to {}", FILE, exception);
        }
    }
}
