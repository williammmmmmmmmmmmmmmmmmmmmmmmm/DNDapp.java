package org.example.dndapp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EncounterStorage {
    private static final String FILE_NAME = "encounters.dat";

    // Save the list to a file
    public static void saveEncounters(List<Encounter> encounters) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(encounters);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the list from the file
    @SuppressWarnings("unchecked")
    public static List<Encounter> loadEncounters() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Encounter>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}