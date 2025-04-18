package classes;

import java.io.*;
import java.util.*;

import utils.VehicleFactory;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RentalShop{
    // File Directory 
    private static final String FILES_DIR = "src" + File.separator + "files" + File.separator + "shops";
    private static final String RENT_FILE = FILES_DIR + File.separator + "rentals.txt";
    private static final String[] CAR_TYPES = {"SEDAN", "SUV", "VAN"};
    
    // Attributes 
    private String city;                                                // City name      
    private int spaces;                                                 // Number of parking spaces      
    private double balance = 0.0;                                       // Shop balance     
    private String shopFile;                                            // File name for the shop
    private List<String> lots;                                          // Parking lots list   
    private List<Vehicle> vehicles = new ArrayList<>();                 // Vehicles list
    private List<Transaction> transactions = new ArrayList<>();        // Transactions list 

    // Constructor 
    public RentalShop(String city, int spaces, List<String> lots) {
        this.city = city;
        this.spaces = spaces; 
        this.lots = lots;
        this.shopFile = FILES_DIR + File.separator + city + ".txt"; // File name for the shop

        // Create the shop file if it doesn't exist, else load the data
        if (new File(shopFile).exists()) loadShopData();
        else initializeShopData();
    }

    /**
     * Saves the shop data to a file 
     * Utilizes the FileChannel and FileLock classes to ensure process synchronization
     * when accessing a shared buffer (the shop file).
     */
    private void saveShopData() {
        File file = new File(shopFile);
        File dir = new File(file.getParent());

        // Create the directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            // Acquire a writer lock on the file
            FileLock lock = channel.lock();
            try {
                raf.seek(0);
                raf.writeBytes("City: " + city + "\n");
                raf.writeBytes("Spaces: " + spaces + "\n");
                raf.writeBytes("Balance: " + balance + "\n");
                raf.writeBytes("Vehicles:\n");
                for (Vehicle vehicle : vehicles) {
                    raf.writeBytes(vehicle.toString() + "\n");
                } 
            } finally {
                lock.release(); // Release the lock
            }
        } catch (IOException e) {
            System.err.println("ERROR: Saving shop data: " + e.getMessage());
        }
    }

    /**
     * Loads the shop data from a file
     * Utilizes the FileChannel and FileLock classes to ensure process synchronization
     * when accessing a shared buffer (the shop file).
     */
    private void loadShopData() {
    }

    // Initialize the Shop Data
    private void initializeShopData() {
        // Retrieve vehicles from the lot files 
        vehicles = new ArrayList<>();
        vehicles.add(VehicleFactory.createVehicle("SEDAN"));
        vehicles.add(VehicleFactory.createVehicle("SUV"));
        vehicles.add(VehicleFactory.createVehicle("VAN"));
        // Save the initial state of the shop
        saveShopData();
    }

    public void printShopState() {
        loadShopData(); // Load the shop data before printing the state
        System.out.println("----SHOP STATE (" + city + ")----");
        System.out.println("Total Parking Spaces: " + spaces);
        System.out.println("Shop Parking Spaces Available: " + (spaces - vehicles.size()));
        System.out.println("Shop Balance: $" + balance);
        System.out.println("Vehicles in Shop: ");
        for (Vehicle vehicle : vehicles) {
            System.out.println(">" + vehicle.getLicensePlate() + " - " + vehicle.getModel() + " - " + vehicle.getOdometer() + " km");
        }
    }

}
