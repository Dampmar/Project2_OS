package classes;

import utils.*;
import java.io.*;
import java.util.*;


public class LotManager {
    // Constant for file directory
    private static final String FILES_DIR = "src" + File.separator + "files" + File.separator + "lots";
    private static final String[] VEHICLE_TYPES = {"SEDAN", "SUV", "VAN"};

    // Vehicle counts 
    private static int sedans;
    private static int suvs;
    private static int vans;

    /**
     * Main method to manage the vehicle lot app 
     * @param args Command line arguments
     * --lot-name=<lot_name> : Name of the lot
     * --add-sedan=<count> : Number of sedans to add
     * --add-suv=<count> : Number of SUVs to add
     * --add-van=<count> : Number of vans to add
     * --remove-vehicle=<license_plate> : License plate of the vehicle to remove
     */
    public static void main(String[] args) {
        // Create the directory if it doesn't exist 
        File dir = new File(FILES_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Error creating directory: " + FILES_DIR);
                System.exit(1);
            }
        }

        // Parse command line arguments
        Map<String, String> params = ParseArgs.parseArgs(args);
        String lotName = params.get("lot-name");
        if (lotName == null || lotName.isEmpty()) {
            System.err.println("Error: Lot name is required.");
            System.exit(1);
        }

        try {
            sedans = params.containsKey("add-sedan") ? Integer.parseInt(params.get("add-sedan")) : 0;
            suvs = params.containsKey("add-suv") ? Integer.parseInt(params.get("add-suv")) : 0;
            vans = params.containsKey("add-van") ? Integer.parseInt(params.get("add-van")) : 0;
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for vehicle counts.");
            System.exit(1);
        }

        // Retrieve existing vehicles from the lot file 
        List<Vehicle> vehicles = getContents(lotName);

        // Add new vehicles to the list
        for (int i = 0; i < sedans; i++) { vehicles.add(VehicleFactory.createVehicle("SEDAN")); }
        for (int i = 0; i < suvs; i++) { vehicles.add(VehicleFactory.createVehicle("SUV")); }
        for (int i = 0; i < vans; i++) { vehicles.add(VehicleFactory.createVehicle("VAN")); }

        // Check for deletes 
        if (params.containsKey("remove-vehicle")) {
            String licensePlate = params.get("remove-vehicle");
            vehicles.removeIf(vehicle -> vehicle.getLicensePlate().equals(licensePlate));
        }

        // Add vehicles to the file 
        addContents(lotName, vehicles);
    }

    /**
     * Reads the contents of the lot file
     * @param lotName Name of the lot
     * @return List of vehicles in the lot
     */
    private static List<Vehicle> getContents(String lotName) {
        List<Vehicle> vehicles = new ArrayList<>();
        File lotFile = new File(FILES_DIR + File.separator + lotName + ".txt");

        // Create the file if it doesn't exist 
        if (!lotFile.exists()) {
            try {
                lotFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating lot file: " + e.getMessage());
                System.exit(1);
            }
        }

        // Read the file contents 
        try (BufferedReader reader = new BufferedReader(new FileReader(lotFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    vehicles.add(new Vehicle(parts[0], parts[1], Integer.parseInt(parts[2])));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading lot file: " + e.getMessage());
        }

        return vehicles;
    }

    /**
     * Writes the contents of the lot file
     * @param lotName Name of the lot
     * @param vehicles List of vehicles to write
     */
    private static void addContents(String lotName, List<Vehicle> vehicles) {
        File lotFile = new File(FILES_DIR + File.separator + lotName + ".txt");

        // Write the file contents 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lotFile))) {
            for (Vehicle vehicle : vehicles) {
                writer.write(vehicle.getLicensePlate() + "," + vehicle.getType() + "," + vehicle.getOdometer() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to lot file: " + e.getMessage());
        }
    }
}
