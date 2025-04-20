package classes;

import utils.*;

import java.io.*;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RentalShop {
    // File Directory 
    private static final String FILES_DIR = "src" + File.separator + "files" + File.separator + "shops";
    private static final String LOTS_DIR = "src" + File.separator + "files" + File.separator + "lots";
    private static final String[] CAR_TYPES = {"SEDAN", "SUV", "VAN"};

    // Attributes 
    private String city;
    private int spaces; 
    private double balance = 0.0;
    private String shopFile;
    private List<String> lots;
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Constructor for RentalShop class
     * @param city The city where the shop is located
     * @param spaces The number of parking spaces 
     * @param lots The list of parking lots
     */
    public RentalShop(String city, int spaces, List<String> lots) {
        this.city = city; 
        this.spaces = spaces; 
        this.lots = new ArrayList<>(lots);
        this.shopFile = FILES_DIR + File.separator + city + ".txt"; // File name for the shop

        RentalShop loadedShop = ShopPersistanceManager.loadShop(this.city);
        if (loadedShop != null) {
            this.spaces = loadedShop.getSpaces();
            this.balance = loadedShop.getBalance();
            this.lots = loadedShop.getLots();
            this.vehicles = loadedShop.getVehicles();
            this.transactions = loadedShop.getTransactions();
        } else { 
            initializeShopData(); 
        }
    }

    /**
     * Constructor for Loading an existing shop
     * @param shopFile file location
     * @param city city name
     */
    public RentalShop(String shopFile, String city) {
        this.city = city;
        this.spaces = 0;
        this.balance = 0.0;
        this.shopFile = shopFile;
        this.lots = new ArrayList<>();
    }

    /**
     * Getters and Setters for the ShopPersistanceManager class
     * @return the values of the attributes
     * * Utilized inside the ShopPersistanceManager class to save and load the shop data consistently across
     * * all instances of the RentalShop class for a specific city.
     * * This ensures that the shop data is synchronized and consistent across different instances.
     */
    public String getCity() { return city; }
    public int getSpaces() { return spaces; }
    public double getBalance() { return balance; }
    public String getShopFile() { return shopFile; }
    public List<String> getLots() { return lots; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setSpaces(int spaces) { this.spaces = spaces; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setShopFile(String shopFile) { this.shopFile = shopFile; }
    public void setLots(List<String> lots) { this.lots = lots; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    // Helper methods to load shop data 
    public void addLot(String lot) { this.lots.add(lot); }
    public void addVehicle(Vehicle vehicle) { this.vehicles.add(vehicle); }
    public void addTransaction(Transaction transaction) { this.transactions.add(transaction); }

    // Print the shop state
    public void printShopState() {
        RentalShop temp = ShopPersistanceManager.loadShop(this.city); // Load the shop data before printing the state
        if (temp != null) {
            this.spaces = temp.getSpaces();
            this.balance = temp.getBalance();
            this.lots = temp.getLots();
            this.vehicles = temp.getVehicles();
            this.transactions = temp.getTransactions();
        } else {
            System.out.println("ERROR: Unable to load shop data.");
            return;
        }

        // Print the shop state
        System.out.println("----SHOP STATE (" + city + ")----");
        System.out.println("Total Parking Spaces: " + spaces);
        System.out.println("Available Parking Spaces: " + (spaces - vehicles.size()));
        System.out.println("Current Balance: $" + balance);
        System.out.println("Parking Lots: " + String.join(", ", lots));
        System.out.println("Vehicles: ");
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle.toString());
        }
        /* 
        System.out.println("Transactions: ");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }
        */
        System.out.println("------------------------------");
    }

    // Print the shop transactions
    public void printShopTransactions() {
        RentalShop temp = ShopPersistanceManager.loadShop(this.city); // Load the shop data before printing the state
        if (temp != null) {
            this.spaces = temp.getSpaces();
            this.balance = temp.getBalance();
            this.lots = temp.getLots();
            this.vehicles = temp.getVehicles();
            this.transactions = temp.getTransactions();
        } else {
            System.out.println("ERROR: Unable to load shop data.");
            return;
        }

        // Print the shop state
        System.out.println("----SHOP Transactions (" + city + ")----");
        System.out.println("Transactions: ");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }
        System.out.println("------------------------------");
    }

    /**
     * Initialize the shop data with vehicles from the Lots 
     */
    private void initializeShopData() {
        while (vehicles.size() < spaces - 2) {
            boolean foundVehicle = false;
            // Request vehicles from the parking lots
            for (String type : CAR_TYPES) {
                VehicleRetrival retrival = requestVehicle(type);
                if (retrival != null) {
                    addVehicle(retrival.getVehicle());
                    foundVehicle = true;
                    if (vehicles.size() >= spaces - 2) break;
                }
            }
            if (!foundVehicle) {
                System.out.println("No more vehicles available in the parking lots.");
                break; // Exit the loop if no vehicles are found
            }
        }
        // Save the shop data to a file
        if (!ShopPersistanceManager.saveShop(this)) {
            System.out.println("ERROR: Unable to save shop data.");
        } else {
            System.out.println("Shop data initialized and saved successfully.");
        }
    }

    /**
     * Request a vehicle of a specific type from the parking lots
     * @param type The type of vehicle to request
     * @return VehicleRetrival object containing the vehicle and its lot
     */
    public VehicleRetrival requestVehicle(String type) {
        VehicleRetrival retrival = null;
        // Check if the requested vehicle type is valid
        for (String lot : lots) {
            String lotFile = LOTS_DIR + File.separator + lot + ".txt";
            List<Vehicle> vehicles = new ArrayList<>(); 
            File file = new File(lotFile);
            if (!file.exists()) continue; // Skip if the file doesn't exist
            // Open the file and read the vehicles
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 FileChannel channel = raf.getChannel()) {
                // Acquire a reader/writer lock on the file 
                FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
                raf.seek(0);
                String line;
                try {
                    // Read the vehicles from the file 
                    while ((line = raf.readLine()) != null) {
                        String[] strings = line.split(",");
                        if (strings.length == 3) {
                            String licensePlate = strings[0];
                            String model = strings[1];
                            int kilometers = Integer.parseInt(strings[2]);
                            vehicles.add(new Vehicle(licensePlate, model, kilometers));
                        }
                    }
                    // Check if the requested vehicle type is available in the lot
                    for (Vehicle vehicle : vehicles) {
                        if (vehicle.getType().equalsIgnoreCase(type)) {
                            retrival = new VehicleRetrival(vehicle, lot);
                            vehicles.remove(vehicle); // Remove the vehicle from the lot
                            break;
                        }
                    }
                    // Write the updated vehicles back to the file
                    if (retrival != null) {
                        raf.setLength(0); // Clear the file
                        for (Vehicle vehicle : vehicles) {
                            raf.writeBytes(vehicle.getLicensePlate() + "," + vehicle.getType() + "," + vehicle.getOdometer() + "\n");
                        }
                    }
                } finally {
                    lock.release(); // Release the lock
                }
            } catch (IOException e) {
                System.err.println("Error accessing lot file: " + e.getMessage());
            }
        }
        return retrival;
    }

    /**
     * RENT A VEHICLE - RENT CMD
     * This method is used to rent a vehicle from the shop.
     * @param vehicleType The type of vehicle to rent (e.g., SEDAN, SUV, VAN)
     */
    private void rentVehicle(String vehicleType) {
        RentalShop temp = ShopPersistanceManager.loadShop(this.city); // Load the shop data before printing the state
        if (temp != null) {
            this.spaces = temp.getSpaces();
            this.balance = temp.getBalance();
            this.lots = temp.getLots();
            this.vehicles = temp.getVehicles();
            this.transactions = temp.getTransactions();
        } else {
            System.out.println("ERROR: Unable to load shop data.");
            return;
        }

        Vehicle vehicle = null;
        boolean applyDiscount = false; 
        
        // Check validity of the vehicle type
        if (!Arrays.asList(CAR_TYPES).contains(vehicleType.toUpperCase())) {
            System.out.println("ERROR: Invalid vehicle type. Please choose from: " + String.join(", ", CAR_TYPES));
            return;
        }

        // Check if the vehicle is available in the shop
        for (Vehicle v : vehicles) {
            if (v.getType().equalsIgnoreCase(vehicleType)) {
                vehicle = v;
                break;
            }
        }

        if (vehicle == null) {
            // If the vehicle is not available, request it from the parking lots
            VehicleRetrival retrival = requestVehicle(vehicleType);
            if (retrival == null) {
                System.out.println("ERROR: No vehicles available for rent.");
                return;
            }
            vehicle = retrival.getVehicle();
            String lot = retrival.getLot();
            applyDiscount = true; // Apply discount if the vehicle is retrieved from a lot
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " retrieved from lot: " + lot);
        } else {
            vehicles.remove(vehicle); // Remove the vehicle from the shop   
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " rented from the shop.");
        }

        // Record the rental information 
        if (RentalFileManager.addToRentalFile(new RentInfo(vehicle, applyDiscount))) {
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " rented successfully.");
        } else {
            vehicles.add(vehicle); // Add the vehicle back to the shop if rental fails
            System.out.println("ERROR: Could not add rental information to the file, vehicle not rented.");
        }

        ShopPersistanceManager.saveShop(this); // Save the shop data after renting a vehicle
    }

    /**
     * RETURN A VEHICLE - RETURN CMD
     * This method is used to return a vehicle to any shop.
     * @param licensePlate the license plate of the vehicle to return
     * @param kilometers the number of kilometers driven during the rental period 
     */
    private void returnVehicle(String licensePlate, int kilometers) {
        RentalShop temp = ShopPersistanceManager.loadShop(this.city); // Load the shop data before printing the state
        if (temp != null) {
            this.spaces = temp.getSpaces();
            this.balance = temp.getBalance();
            this.lots = temp.getLots();
            this.vehicles = temp.getVehicles();
            this.transactions = temp.getTransactions();
        } else {
            System.out.println("ERROR: Unable to load shop data.");
            return;
        }
        
        // Check if the vehicle is in the rental record 
        RentInfo rentInfo = RentalFileManager.checkRentalRecord(licensePlate);
        if (rentInfo == null) {
            System.out.println("ERROR: Vehicle " + licensePlate + " not found in rental record.");
            return;
        }

        // Calculate the rental cost and load the transaction
        Vehicle vehicle = rentInfo.getVehicle();

        // Check if the kilometers driven is valid
        if (kilometers < 0) {
            System.out.println("ERROR: Invalid kilometers driven. Must be a positive number.");
            return;
        }

        // Update the vehicle's odometer and calculate the rental cost 
        vehicle.addToOdometer(kilometers);
        double cost = (rentInfo.isDiscount()) ? 0.9 * kilometers : kilometers; 
        this.balance += cost; // Update the shop's balance

        // Add the vehicle back to the shop
        vehicles.add(vehicle);

        // Create a transaction record for the return
        Transaction transaction = new Transaction(licensePlate, kilometers, rentInfo.isDiscount(), cost);
        transactions.add(transaction); // Add the transaction to the list

        // Remove from the rental file 
        RentalFileManager.removeFromRentalFile(licensePlate); // Remove the rental record from the file

        // Check spaces threshold
        checkThreshold(); 

        if (ShopPersistanceManager.saveShop(this)) {
            System.out.println("INFO: Vehicle " + licensePlate + " returned successfully. Total cost: $" + cost);
        } else {
            System.out.println("ERROR: Could not save shop data after vehicle return.");
        }

    }

    private void checkThreshold() {
        int availableSpaces = spaces - vehicles.size(); // Calculate available spaces
        if (availableSpaces < 2) {
            System.out.println("WARNING: Available parking spaces below threshold (" + availableSpaces + " spaces).");
            System.out.println("Redistributing 20% of vehicles to parking lots...");

            int vehiclesToRedistribute = (int) Math.ceil(vehicles.size() * 0.2); // Calculate vehicles to redistribute
            System.out.println("Moving " + vehiclesToRedistribute + " vehicles to parking lots.");

            // Sort Vehicles by odometer reading in ascending order
            vehicles.sort((v1, v2) -> Integer.compare(v1.getOdometer(), v2.getOdometer())); 

            // Keep track of successfully moved vehicles
            int movedVehicles = 0;
            List<Vehicle> vehiclesToRemove = new ArrayList<>(); // List to store vehicles to move
            for (int i = 0; i < vehicles.size() && movedVehicles < vehiclesToRedistribute; i++) {
                Vehicle vehicle = vehicles.get(i);
                if (returnVehicleToLot(vehicle)) {
                    vehiclesToRemove.add(vehicle); // Add the vehicle to the list of vehicles to remove
                    movedVehicles++;
                }
            }

            // Remove moved vehicles from the shop
            vehicles.removeAll(vehiclesToRemove); // Remove vehicles from the shop list
        }
    }

    /**
     * Returns a vehicle to the parking lot
     * @param vehicle The vehicle to return to a lot
     * @return true if successful, false otherwise
     */
    private boolean returnVehicleToLot(Vehicle vehicle) {
        // Check if we have any lots to return vehicles to
        if (lots == null || lots.isEmpty()) {
            System.err.println("ERROR: No parking lots available to return vehicle " + vehicle.getLicensePlate());
            return false;
        }

        // Shuffle the lots to randomize the selection
        List<String> randomizedLots = new ArrayList<>(lots);
        Collections.shuffle(randomizedLots);
        for (String lot : randomizedLots) {
            String lotFile = LOTS_DIR + File.separator + lot + ".txt";
            File file = new File(lotFile);
            File dir = new File(file.getParent());
            if (!dir.exists()) dir.mkdirs();
    
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 FileChannel channel = raf.getChannel()) {
                
                FileLock lock = channel.lock(0, Long.MAX_VALUE, false); // Acquire a writer lock on the file
                try {
                    raf.seek(raf.length());
                    String line = vehicle.getLicensePlate() + "," + vehicle.getType() + "," + vehicle.getOdometer() + "\n";
                    raf.writeBytes(line); // Write the vehicle information to the file
                    System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " returned to lot: " + randomizedLots.get(0));
                } finally {
                    lock.release(); // Release the lock
                }
            } catch (IOException e) {
                System.err.println("ERROR: Could not write to lot file: " + e.getMessage());
                return false;
            }
            break;
        }

        return true; // Vehicle successfully returned to a lot
    }



    // Process commands from the user 
    public void processCommand(String command) {
        String[] tokens = command.split(" ");
        if (tokens.length == 0) return; // No command entered
        String action = tokens[0].toUpperCase(); // Get the action (RENT, RETURN, LIST, TRANSACTIONS)
        switch (action) {
            case "RENT":
                if (tokens.length < 2) {
                    System.out.println("ERROR: RENT command requires a vehicle type.");
                } else {
                    String vehicleType = tokens[1];
                    rentVehicle(vehicleType);
                }
                break;
            case "RETURN": 
                if (tokens.length < 3) {
                    System.out.println("ERROR: RETURN command requires a license plate and kilometers driven.");
                } else {
                    String licensePlate = tokens[1];
                    int kilometers = Integer.parseInt(tokens[2]);
                    returnVehicle(licensePlate, kilometers);
                }
                break;
            case "LIST":
                printShopState();
                break;
            case "TRANSACTIONS":
                printShopTransactions();
                break;
            default:
                System.out.println("ERROR: Unknown command. Valid commands are: RENT, RETURN, LIST, TRANSACTIONS.");
        }
    }

    /**
     * Run the RentalShop application
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Rental Shop in " + city + "! Type 'exit' to quit.");
        System.out.println("Command: RENT <vehicle_type>, RETURN <license_plate> <kilometers>, LIST, TRANSACTIONS");
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("exit") || command == null) {
                break; // Exit the loop if the user types 'exit'
            }
            processCommand(command); // Process the command entered by the user
        }
        scanner.close(); // Close the scanner
    }
}
