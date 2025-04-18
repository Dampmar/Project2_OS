package utils;

import java.io.*;
import java.util.*;
import java.nio.channels.*;

public class LicensePlateGenerator {
    private static final String INDEX_FILE = "src" + File.separator + "files" + File.separator + "indexes" + File.separator + "index.txt";
    /**
     * Generates a unique license plate in the format "XXX-###" 
     * where X is a letter and # is a digit
     * @return
     */
    public static String generateLicensePlate() {
        List<String> plates = readExistingPlates();
        Random random = new Random();
        String plate;
        StringBuilder plateBuilder;
        do {
            plateBuilder = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                char letter = (char) ('A' + random.nextInt(26)); // Random letter
                plateBuilder.append(letter);
            }
            plateBuilder.append("-");
            for (int i = 0; i < 3; i++) {
                int digit = random.nextInt(10); // Random digit
                plateBuilder.append(digit);
            }
            plate = plateBuilder.toString();
        } while (plates.contains(plate)); // Ensure uniqueness

        // Save the new plate to the index file 
        savePlateToIndex(plate);
        return plate;
    }

    /**
     * Reads existing plates from the index file
     * @return List of existing plates
     */
    private static List<String> readExistingPlates() {
        List<String> plates = new ArrayList<>();
        File indexFile = new File(INDEX_FILE);
        File indexDir = new File(indexFile.getParent());

        // Create the directory if it doesn't exist
        if (!indexDir.exists()) indexDir.mkdirs();
        // Create file if it doesn't exist
        if (!indexFile.exists()) {
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating index file: " + e.getMessage());
            }
        }

        try (RandomAccessFile file = new RandomAccessFile(indexFile, "r");
             FileChannel channel = file.getChannel()) {
            // Acquire a reader lock on the file 
            FileLock lock = channel.lock(0, Long.MAX_VALUE, true);
            try {
                String line;
                while ((line = file.readLine()) != null) {
                    plates.add(line.trim()); // Add each plate to the list
                }
            } finally {
                lock.release(); // Release the lock
            }
        } catch (IOException e) {
            System.err.println("Error reading index file: " + e.getMessage());
        }

        return plates;
    }

    /**
     * Saves the new plate to the index file
     * @param plate The new license plate to save
     */
    private static void savePlateToIndex(String plate) {
        File indexFile = new File(INDEX_FILE);
        try (RandomAccessFile file = new RandomAccessFile(indexFile, "rw");
             FileChannel channel = file.getChannel()) {
            // Acquire an exclusive lock on the file 
            FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
            try {
                file.seek(file.length()); // Move to the end of the file
                file.writeBytes(plate + System.lineSeparator()); // Write the new plate
            } finally {
                lock.release(); // Release the lock
            }
        } catch (IOException e) {
            System.err.println("Error writing to index file: " + e.getMessage());
        }
    }
}