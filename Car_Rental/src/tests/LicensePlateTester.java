package tests;

import utils.LicensePlateGenerator;
import java.util.HashSet;
import java.util.Set;

public class LicensePlateTester {
    public static void main(String[] args) {
        // Get process ID (or use a random ID if not provided)
        String processId = args.length > 0 ? args[0] : String.valueOf((int)(Math.random() * 1000));
        System.out.println("Process " + processId + " starting license plate generation");

        // Number of plates to generate per process
        int plateCount = 20;
        Set<String> generatedPlates = new HashSet<>();

        for (int i = 0; i < plateCount; i++) {
            String plate = LicensePlateGenerator.generateLicensePlate();
            generatedPlates.add(plate);
            System.out.println("Process " + processId + " - Generated plate #" + (i+1) + ": " + plate);
        }
        // Verify uniqueness within this process
        System.out.println("\nProcess " + processId + " generated " + generatedPlates.size() + " unique plates out of " + plateCount + " attempts");
        System.out.println("Process " + processId + " finished");
    }
}
