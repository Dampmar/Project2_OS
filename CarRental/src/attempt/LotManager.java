package attempt;

import java.io.*;
import java.util.*;

public class LotManager {
    // Constants for file paths 
    private static final String FILES_DIR = "src" + File.separator + "files";
    private static final String INDEX_FILE = FILES_DIR + File.separator + "index.txt";
    private static final String[] VEHICLE_TYPES = {"SEDAN", "SUV", "VAN"};

    // Vehicle counts 
    private static int sedans;
    private static int suvs;
    private static int vans; 

    // Main method to run and create the lot manager 
    public static void main(String[] args) {
        // Create the files directory if it doesn't exist 
        File filesDir = new File(FILES_DIR);
        if (!filesDir.exists()) {
            if (!filesDir.mkdir()) {
                System.out.println("ERROR: Could not create the 'files' directory.");
                System.exit(1);
            }
        }

        // Parse the arguments from the CLI
        Map<String, String> params = parseArgs(args);
        String lotName = params.get("lot-name");
        if (lotName == null || lotName.isEmpty()) {
            System.out.println("ERROR: --lot-name argument is required.");
            System.exit(1);
        }

        String lotFile = FILES_DIR + File.separator + lotName + ".txt";

        // Retrieve the number of vehicles to add
        try {
            sedans = params.containsKey("add-sedan") ? Integer.parseInt(params.get("add-sedan")) : 0;
            suvs = params.containsKey("add-suv") ? Integer.parseInt(params.get("add-suv")) : 0;
            vans = params.containsKey("add-van") ? Integer.parseInt(params.get("add-van")) : 0;
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid number format for vehicle counts.");
            System.exit(1);
        }

        // Retrieve existing vehicles from the lot file
        List<Vehicle> vehicles = getLotContents(lotFile);

        // Add new vehicles to the lot 
        for (int i = 0; i < sedans; i++) { vehicles.add(createNewVehicle("sedan")); }
        for (int i = 0; i < suvs; i++) { vehicles.add(createNewVehicle("suv")); }
        for (int i = 0; i < vans; i++) { vehicles.add(createNewVehicle("van")); }

        // Check for deletes 
        if (params.containsKey("remove-vehicle")) {
            String licensePlate = params.get("remove-vehicle");
            vehicles.removeIf(vehicle -> vehicle.getLicensePlate().equals(licensePlate));
        }

        // Add the vehicles to the lot file 
        addVehiclesToLot(lotFile, vehicles);
    }

    // Method to get contents of the lot 
    private static List<Vehicle> getLotContents(String lotFile) {
        List<Vehicle> vehicles = new ArrayList<>();
        File file = new File(lotFile);
        // Check if the file exists and read its contents
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line; 
                while ((line = br.readLine()) != null) {
                    String[] strings = line.split(",");
                    if (strings.length == 3){
                        String licensePlate = strings[0];
                        String model = strings[1];
                        int kilometers = Integer.parseInt(strings[2]);
                        vehicles.add(new Vehicle(licensePlate, model, kilometers));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
        return vehicles;
    }

    // Method to add vehicles to lot 
    private static void addVehiclesToLot(String lotFile, List<Vehicle> vehicles) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(lotFile))) {
            for (Vehicle vehicle : vehicles) {
                bw.write(vehicle.getLicensePlate() + "," + vehicle.getModel() + "," + vehicle.getOdometer() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
    

    // Method to create a new vehicle
    private static Vehicle createNewVehicle(String type) {
        for (String vehicleType : VEHICLE_TYPES) {
            if (vehicleType.equalsIgnoreCase(type)) {
                return new Vehicle(generateLicensePlate(), type.toUpperCase(), 0);
            }
        }

        throw new IllegalArgumentException("Invalid vehicle type: " + type);
    }

    // Method to generate a random unique license plate
    private static String generateLicensePlate() {
        List<String> existingPlates = loadPlates();
        Random random = new Random();
        String plate = null;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                sb.append((char) (random.nextInt(26) + 'A'));
            }
            sb.append("-");
            for (int i = 0; i < 3; i++) {
                sb.append(random.nextInt(10));
            }
            plate = sb.toString();
        } while (existingPlates.contains(plate)); // Ensure the plate is unique
        existingPlates.add(plate); // Add the new plate to the list of existing plates
        savePlates(existingPlates); // Save the updated list of plates to the file
        return plate;
    }

    // Method to load existing license plates from the index file
    private static List<String> loadPlates() {
        List<String> plates = new ArrayList<>();
        File file = new File(INDEX_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line; 
                while ((line = br.readLine()) != null) {
                    plates.add(line.trim());
                }
            } catch (IOException e) {
                System.out.println("Error reading index file: " + e.getMessage());
            }
        }
        return plates;
    }

    // Method to save the updated list of license plates to the index file 
    private static void savePlates(List<String> plates) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INDEX_FILE))) {
            for (String plate : plates) {
                bw.write(plate + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to index file: " + e.getMessage());
        }
    }

    // Method to parse the flags 
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for(String arg : args) {
            if (arg.startsWith("--")) {
                int equalityIndex = arg.indexOf('=');
                if (equalityIndex > 0) {
                    String key = arg.substring(2, equalityIndex);
                    String value = arg.substring(equalityIndex + 1);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}
