package org.example.dndapp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CharacterFileManager {

    private static final String CHARACTERS_FOLDER_NAME = "Characters";

    /**
     * Creates the Characters directory if it doesn't exist.
     */
    private static void ensureCharactersDirectoryExists() {
        try {
            Path path = Paths.get(CHARACTERS_FOLDER_NAME);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a Character object to a file.
     * @param character The Character object to save.
     */
    public static void saveCharacter(Character character) {
        ensureCharactersDirectoryExists();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fileName = CHARACTERS_FOLDER_NAME + File.separator + character.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".dnd";
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(character, writer);
            System.out.println("Character saved to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a Character object from a file.
     * @param fileName The name of the file to load.
     * @return The loaded Character object, or null if an error occurs.
     */
    public static Character loadCharacter(String fileName) {
        ensureCharactersDirectoryExists();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<List<Item>>(){}.getType(), new InventoryDeserializer());
        Gson gson = gsonBuilder.create();
        String filePath = CHARACTERS_FOLDER_NAME + File.separator + fileName;
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Character.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Custom TypeAdapter to handle deserializing both old String and new Item objects for inventory.
     */
    private static class InventoryDeserializer implements JsonDeserializer<List<Item>> {
        @Override
        public List<Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<Item> inventory = new ArrayList<>();
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonPrimitive()) {
                    // It's an old-style string item, create a new Item object
                    inventory.add(new Item(element.getAsString(), ""));
                } else if (element.isJsonObject()) {
                    // It's a new-style Item object
                    inventory.add(context.deserialize(element, Item.class));
                }
            }
            return inventory;
        }
    }

    /**
     * Gets a list of all saved character files in the Characters folder.
     * @return A List of file names.
     */
    public static List<String> getSavedCharacters() {
        ensureCharactersDirectoryExists();
        List<String> characterFiles = new ArrayList<>();
        File folder = new File(CHARACTERS_FOLDER_NAME);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".dnd"));
        if (files != null) {
            for (File file : files) {
                characterFiles.add(file.getName());
            }
        }
        return characterFiles;
    }
}