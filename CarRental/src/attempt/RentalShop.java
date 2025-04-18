package attempt;

import java.io.*;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RentalShop {
    // File Paths and Car Types 
    private static final String FILES_DIR = "src" + File.separator + "stores";
    private static final String RENTED_FILE = FILES_DIR + File.separator + "rented.txt";
    private static final String[] CAR_TYPES = {"SEDAN", "SUV", "VAN"};

    // Shop Details 
    private String city;
    private int spaces; 
    private double balance = 0.0;
    private String shopFile;
    private List<String> lots;
    private List<Vehicle> vehiclesInShop = new ArrayList<>(); // Vehicles in the shop
    private List<Transaction> transactions = new ArrayList<>(); // Transactions made in the shop

    // Constructor for RentalShop
    public RentalShop(String city, int spaces, List<String> lots) {
        this.city = city;
        this.spaces = spaces;
        this.lots = lots;
        this.shopFile = FILES_DIR + File.separator + city + ".txt";

        // Check if the shop file exists, if not create it
        if (new File(shopFile).exists()) {
            loadShopData();
        } else {
            initializeShopData();
        }
    }

    // Save the Shop Data to a file 
    public void saveShopData() {
        File file = new File(shopFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("ERROR: Could not create the shop file.");
                return;
            }
        }

        // Lock the file for writing 
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock(0, Long.MAX_VALUE, false)) {
            
            // Clear the contents of the file 
            raf.setLength(0);

            // Write the shop details to the file 
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(raf.getFD()))) {
                oos.writeObject(city);
                oos.writeInt(spaces);
                oos.writeDouble(balance);
                oos.writeObject(lots);
                oos.writeObject(vehiclesInShop);
                oos.writeObject(transactions);
            } catch (IOException e) {
                System.out.println("ERROR: Could not write to the shop file.");
                e.printStackTrace();
            }  
        } catch (IOException e) {
            System.out.println("ERROR: Could not lock the shop file for writing.");
            e.printStackTrace();
        }
    }

    // Load the Shop Data from a file
    private void loadShopData() {
        File file = new File(shopFile);
        if (!file.exists() || !file.canRead()) {
            System.out.println("ERROR: Shop file does not exist or is not readable.");
            return;
        }

        // Lock the file for reading 
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
            
            // Read the shop details from the file 
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(raf.getFD()))) {
                city = (String) ois.readObject();
                spaces = ois.readInt();
                balance = ois.readDouble();
                lots = (List<String>) ois.readObject();
                vehiclesInShop = (List<Vehicle>) ois.readObject();
                transactions = (List<Transaction>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("ERROR: Could not read from the shop file.");
                e.printStackTrace();
            }  
        } catch (IOException e) {
            System.out.println("ERROR: Could not lock the shop file for reading.");
            e.printStackTrace();
        }
    }

    // Initialize the Shop Data
    private void initializeShopData() {
        // Retrieve vehicles from the lot files 
        while (vehiclesInShop.size() < spaces - 2) {
            boolean foundVehicle = false;
            for (String type : CAR_TYPES) {
                VehicleRetrival retrival = requestVehicleFromLots(type);
                if (retrival != null) {
                    vehiclesInShop.add(retrival.getVehicle());
                    foundVehicle = true;
                    if (vehiclesInShop.size() >= spaces - 2) {
                        break;
                    }
                }
            }
            if (!foundVehicle) break; // No more vehicles or spaces available
        }
        // Save the initial state of the shop
        saveShopData();
    }

    // Request a vehicle from the lots
    private VehicleRetrival requestVehicleFromLots(String type) {
        VehicleRetrival vehicleRetrival = null;

        // Iterate through the lots to find a vehicle of the requested type
        for (String lot : lots) {
            String lotFile = FILES_DIR + File.separator + lot + ".txt";
            List<Vehicle> vehicles = new ArrayList<>(); // Placeholder for vehicles in the lot
            File file = new File(lotFile);
            if (!file.exists()) continue; // Skip if the lot file does not exist

            // Lock the lot file for reading and writing 
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 FileChannel channel = raf.getChannel();
                 FileLock lock = channel.lock(0, Long.MAX_VALUE, false)) {
                
                // Read the file contents 
                raf.seek(0);
                String line;
                
                // Read the vehicles from the lot file
                while ((line = raf.readLine()) != null) {
                    String[] strings = line.split(",");
                    if (strings.length == 3) {
                        String licensePlate = strings[0];
                        String model = strings[1];
                        int kilometers = Integer.parseInt(strings[2]);
                        vehicles.add(new Vehicle(licensePlate, model, kilometers));
                    }
                }

                // Find a vehicle of the requested type 
                Iterator<Vehicle> iterator = vehicles.iterator();
                while (iterator.hasNext()) {
                    Vehicle vehicle = iterator.next();
                    if (vehicle.getModel().equalsIgnoreCase(type)) {
                        vehicleRetrival = new VehicleRetrival(vehicle, lot);
                        iterator.remove(); // Remove the vehicle from the lot
                        break;
                    }
                }

                // Rewrite the lot file with the updated vehicles if a vehicle was found
                if (vehicleRetrival != null) {
                    raf.setLength(0); // Clear the file contents
                    for (Vehicle vehicle : vehicles) {
                        raf.writeBytes(vehicle.getLicensePlate() + "," + vehicle.getModel() + "," + vehicle.getOdometer() + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("ERROR: Could not lock the lot file for read/write.");
                e.printStackTrace();
                continue;
            } 
        }

        return vehicleRetrival;
    }

    // RENT Command: Rent a vehicle to a customer 
    private void rentVehicle(String vehicleType) {
        loadShopData(); // Load the shop data before renting a vehicle
        Vehicle vehicle = null;
        boolean applyDiscount = false; 

        // Check validity of the vehicle type
        boolean isValidType = false;
        for (String type : CAR_TYPES) {
            if (vehicleType.equalsIgnoreCase(type)) {
                isValidType = true; 
                break;
            }
        }
        if (!isValidType) {
            System.out.println("ERROR: Invalid vehicle type. Valid types are: " + Arrays.toString(CAR_TYPES));
            return;
        }

        // Check if there are available vehicles in the shop
        for (Vehicle v : vehiclesInShop) {
            if (v.getModel().equalsIgnoreCase(vehicleType)) {
                vehicle = v;
                break;
            }
        }

        if (vehicle == null) {
            VehicleRetrival retrival = requestVehicleFromLots(vehicleType);
            if (retrival == null) {
                System.out.println("ERROR: No available vehicles of type " + vehicleType + " in lots nor in shop.");
                return;
            } 

            vehicle = retrival.getVehicle();
            String lot = retrival.getLot();
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " retrieved from lot " + lot + ".");
        } else {
            vehiclesInShop.remove(vehicle);
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " rented from the shop.");
        }

        // Record the rental information in the file 
        if (addToRentalFile(new RentInfo(vehicle, applyDiscount))) {
            System.out.println("INFO: Vehicle " + vehicle.getLicensePlate() + " rented successfully.");
        } else {
            System.out.println("ERROR: Could not add rental information to the file, vehicle not rented.");
        }

        saveShopData(); // Save the updated shop data
    }

    // Add rental information to the rented file
    private boolean addToRentalFile(RentInfo rentInfo) {
        File file = new File(RENTED_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("ERROR: Could not create the rented file.");
            }
        }

        // Lock the file for appending rental information 
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock(0, Long.MAX_VALUE, false)) {
            
            raf.seek(file.length()); // Move to the end of the file

            // Format the rental information
            Vehicle vehicle = rentInfo.getVehicle();
            boolean discount = rentInfo.isDiscount();
            String line = vehicle.getLicensePlate() + "," + vehicle.getModel() + "," + vehicle.getOdometer() + "," + discount + "\n";

            // Write the rental information to the file
            raf.writeBytes(line);
        } catch (IOException e) {
            System.out.println("ERROR: Could not lock the rented file for writing, car not rented.");
            e.printStackTrace();
        }
        // If we reach here, the rental information was successfully added to the file
        return true; // Return true to indicate successful addition of rental information
    }

    // RETURN Command: Rent a vehicle to a customer
    private void returnVehicle(String licensePlate, int kilometers) {
        loadShopData(); // Load the shop data before returning a vehicle
        Vehicle vehicle = null;
        boolean applyDiscount = false;
        boolean found = false;

        // Check if the vehicle is in the rented file 
        try (RandomAccessFile raf = new RandomAccessFile(RENTED_FILE, "r");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
            
            // Read the rented file line by line 
            String line;
            while ((line = raf.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equalsIgnoreCase(licensePlate)) {
                    vehicle = new Vehicle(parts[0], parts[1], Integer.parseInt(parts[2]));
                    applyDiscount = Boolean.parseBoolean(parts[3]);
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could not lock the rented file for reading.");
            e.printStackTrace();
        }

        if (!found) {
            System.out.println("ERROR: Vehicle " + licensePlate + " not found in the rented file.");
            return;
        }

        
        // Finally check before completing the return
        System.out.println("INFO: Vehicle " + licensePlate + " is being processed.");
        double charge = calculateCharge(vehicle, kilometers, applyDiscount);
        if (charge < 0) {
            System.out.println("ERROR: Invalid kilometers driven. Charge calculation failed.");
            return;
        }

        // Remove the vehicle from the rented file 
        try (RandomAccessFile raf = new RandomAccessFile(RENTED_FILE, "rw");
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.lock(0, Long.MAX_VALUE, false)) {
            
            // Remove the vehicle from the rented file 
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = raf.readLine()) != null) {
                if (!line.startsWith(licensePlate)) {
                    sb.append(line).append("\n");
                }
            }
            raf.setLength(0); // Clear the file contents
            raf.writeBytes(sb.toString()); // Write the updated contents back to the file  
        } catch (IOException e) {
            System.out.println("ERROR: Could not lock the rented file for writing.");
            e.printStackTrace();
        }

        // Create a transaction for the rental 
        Transaction transaction = new Transaction(licensePlate, kilometers, applyDiscount, charge);
        transactions.add(transaction); // Add the transaction to the list

        // Update the balance 
        balance += charge;
        
        // Add the vehicle back to the shop or lot
        vehiclesInShop.add(vehicle); // Add back to shop
        
        // Check if the shop is over capacity, reduce the number of vehicles in the shop to 80% of the spaces 
        while (vehiclesInShop.size() > spaces*0.8) {
            if (!reduceShopInventory()) break; 
        }

        saveShopData(); // Save the updated shop data
        System.out.println("INFO: Vehicle " + licensePlate + " returned successfully. Charge: $" + charge);
    }

    private double calculateCharge(Vehicle vehicle, int kilometers, boolean applyDiscount) {
        if (kilometers < 0) return -1; // Invalid kilometers
        double charge = 1.0*kilometers;
        charge = (applyDiscount) ? charge*0.9 : charge; // Apply discount if applicable
        return charge;
    }

    private boolean reduceShopInventory() {
        Vehicle leastKilometersVehicle = vehiclesInShop.stream()
                .min(Comparator.comparingInt(Vehicle::getOdometer))
                .orElse(null);
        
        // Randomly select a lot to return the vehicle to
        if (leastKilometersVehicle != null) {
            String lot = lots.get(new Random().nextInt(lots.size()));
            String lotFile = FILES_DIR + File.separator + lot + ".txt";

            // Lock the lot file for writing
            try (RandomAccessFile raf = new RandomAccessFile(lotFile, "rw");
                 FileChannel channel = raf.getChannel();
                 FileLock lock = channel.lock(0, Long.MAX_VALUE, false)) {
                
                // Append the vehicle to the lot file 
                raf.seek(raf.length()); // Move to the end of the file
                String line = leastKilometersVehicle.getLicensePlate() + "," + leastKilometersVehicle.getModel() + "," + leastKilometersVehicle.getOdometer() + "\n";
                raf.writeBytes(line);
            } catch (IOException e) {
                System.out.println("ERROR: Could not lock the lot file for writing.");
                e.printStackTrace();
                return false; // Failed to write to the lot file
            }
            vehiclesInShop.remove(leastKilometersVehicle); // Remove from the shop
            System.out.println("INFO: Vehicle " + leastKilometersVehicle.getLicensePlate() + " returned to lot " + lot + ".");
            return true; // Successfully reduced the inventory
        } else {
            System.out.println("ERROR: No vehicles to reduce in the shop.");
            return false; // No vehicles to reduce
        }
    }

    private void printShopState() {
        loadShopData(); // Load the shop data before printing the state
        System.out.println("----SHOP STATE (" + city + ")----");
        System.out.println("Total Parking Spaces: " + spaces);
        System.out.println("Shop Parking Spaces Available: " + (spaces - vehiclesInShop.size()));
        System.out.println("Shop Balance: $" + balance);
        System.out.println("Vehicles in Shop: ");
        for (Vehicle vehicle : vehiclesInShop) {
            System.out.println(">" + vehicle.getLicensePlate() + " - " + vehicle.getModel() + " - " + vehicle.getOdometer() + " km");
        }
    }

    private void printTransactions() {
        loadShopData(); // Load the shop data before printing transactions
        System.out.println("----TRANSACTIONS----");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }
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
                printTransactions();
                break;
            default:
                System.out.println("ERROR: Unknown command. Valid commands are: RENT, RETURN, LIST, TRANSACTIONS.");
        }
    }
}

