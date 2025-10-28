package org.example.dndapp;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {
    private static final String CHARACTERS_FOLDER_NAME = "Characters";

    public void generatePdf(Character character) {
        File charactersDir = new File(CHARACTERS_FOLDER_NAME);
        if (!charactersDir.exists()) {
            charactersDir.mkdirs();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Character Sheet PDF");
        fileChooser.setInitialDirectory(charactersDir);
        fileChooser.setInitialFileName(character.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // This is a placeholder. You need to use an external library like iText or Apache PDFBox.
                // For example, using a simplified iText code:
                /*
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.add(new Paragraph(character.getName()).setFontSize(20).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Class & Species: " + character.getSelectedClass() + " " + character.getSelectedSpecies()));
                document.add(new Paragraph("Ability Scores: " + character.getAbilityScores().toString()));
                document.close();
                */

                // For demonstration, we'll just create a placeholder text file.
                FileOutputStream fos = new FileOutputStream(file);
                String content = "--- Character Sheet PDF Placeholder ---\n\n" +
                        "Name: " + character.getName() + "\n" +
                        "Class: " + character.getSelectedClass() + "\n" +
                        "Species: " + character.getSelectedSpecies() + "\n" +
                        "Alignment: " + character.getAlignment() + "\n\n" +
                        "Ability Scores:\n" +
                        "STR: " + character.getAbilityScores().getOrDefault("STR", 0) + "\n" +
                        "DEX: " + character.getAbilityScores().getOrDefault("DEX", 0) + "\n" +
                        "CON: " + character.getAbilityScores().getOrDefault("CON", 0) + "\n" +
                        "INT: " + character.getAbilityScores().getOrDefault("INT", 0) + "\n" +
                        "WIS: " + character.getAbilityScores().getOrDefault("WIS", 0) + "\n" +
                        "CHA: " + character.getAbilityScores().getOrDefault("CHA", 0) + "\n\n" +
                        "Physical Characteristics:\n" +
                        "Age: " + character.getAge() + "\n" +
                        "Height: " + character.getHeight() + "\n" +
                        "Weight: " + character.getWeight() + "\n" +
                        "Hair: " + character.getHair() + "\n" +
                        "Eyes: " + character.getEyes() + "\n" +
                        "Skin: " + character.getSkin() + "\n";
                fos.write(content.getBytes());
                fos.close();

                System.out.println("Placeholder PDF created at: " + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}