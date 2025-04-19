package utils;

import classes.RentalShop;
import classes.Vehicle;
import classes.Transaction;

import java.io.*;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ShopPersistanceManager {
    private static final String FILES_DIR = "src" + File.separator + "files" + File.separator + "shops";

    /**
     * Save shop data to a file with a specific format and proper synchronization.
     * @param shop The RentalShop object to save.
     * @return true if the data was saved successfully, false otherwise.
     */
    public static boolean saveShop(RentalShop shop) {
        String shopFile = FILES_DIR + File.separator + shop.getCity() + ".txt";
        File file = new File(shopFile);
        File dir = new File(file.getParent());

        // Create the directory
        if (!dir.exists()) dir.mkdirs();
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            // Acquire a writer lock on the file
            FileLock lock = channel.lock();
            
            try {
                raf.setLength(0); // Clear the file content

                // Write the shop data to the file
                raf.writeBytes("City:" + shop.getCity() + "\n");
                raf.writeBytes("Spaces:" + shop.getSpaces() + "\n");
                raf.writeBytes("Balance:" + shop.getBalance() + "\n");
                raf.writeBytes("Lots:" + String.join(",", shop.getLots()) + "\n");
                raf.writeBytes("Vehicles:\n");
                for (Vehicle vehicle : shop.getVehicles()) {
                    raf.writeBytes(vehicle.toString() + "\n");
                }
                raf.writeBytes("Transactions:\n");
                for (Transaction transaction : shop.getTransactions()) {
                    raf.writeBytes(transaction.toString() + "\n");
                }
            } finally {
                lock.release(); // Release the lock
            }
        } catch (IOException e) {
            System.err.println("Error saving shop data: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Load shop data from a file with a specific format and proper synchronization.
     * @param city The city name of the shop to load.
     * @return A RentalShop object with the loaded data, or null if loading failed.
     */
    public static RentalShop loadShop(String city) {
        String shopFile = FILES_DIR + File.separator + city + ".txt";
        File file = new File(shopFile);

        if (!file.exists()) return null;
        RentalShop shop = new RentalShop(shopFile, city);
        
        try(RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel()) {

            // Acquire a reader lock on the file
            FileLock lock = channel.lock(0, Long.MAX_VALUE, true);

            // Read the shop data from the file 
            try {
                raf.seek(0);
                String line;

                // Read the first line to get the city name 
                if ((line = raf.readLine()) != null) { city = line.substring(5).trim(); }

                // Read the second line to get the number of spaces 
                if ((line = raf.readLine()) != null) {
                    int spaces = Integer.parseInt(line.substring(7).trim());
                    shop.setSpaces(spaces);
                }

                // Read the third line to get the balance 
                if ((line = raf.readLine()) != null) {
                    double balance = Double.parseDouble(line.substring(8).trim());
                    shop.setBalance(balance);
                }

                // Read the fourth line to get the parking lots 
                line = raf.readLine(); // Read "Lots:" line
                if (line.startsWith("Lots:")) {
                    String[] lotArray = line.substring(5).split(",");
                    for (String lot : lotArray) {
                        shop.addLot(lot.trim());
                    }
                }

                // Read the vehicles section
                if ((line = raf.readLine()) != null && line.startsWith("Vehicles:")) {
                    while ((line = raf.readLine()) != null) {
                        if (line.startsWith("Transactions:")) break; // Transactions section starts here 
                        String[] parts = line.split(",");
                        String plate = parts[0].trim();
                        String type = parts[1].trim();
                        int odometer = Integer.parseInt(parts[2].trim());
                        Vehicle vehicle = new Vehicle(plate, type, odometer);
                        shop.addVehicle(vehicle);
                    }
                }

                // Read the transactions section
                if (line != null && line.startsWith("Transactions:")) {
                    while ((line = raf.readLine()) != null) {
                        String[] parts = line.split(";");
                        // Retrieve the license plate of the vehicle
                        String[] dummy = parts[0].split(": ");
                        String licensePlate = dummy[1].trim();
                        // Retrieve the kilometers driven during the rental period
                        dummy = parts[1].split(": ");
                        int distance = Integer.parseInt(dummy[1].replaceAll("\\D+", ""));
                        // Retrieve the discount status 
                        dummy = parts[2].split(": ");
                        boolean discount = (dummy[1].trim().equals("10%")) ? true : false;
                        // Retrieve the amount charged for the transaction
                        dummy = parts[3].split(":");
                        double amount = Double.parseDouble(dummy[1].substring(2).trim());
                        // Create a new Transaction object and add it to the shop
                        shop.addTransaction(new Transaction(licensePlate, distance, discount, amount));
                    }
                }
            } finally {
                lock.release(); // Release the lock
            }
        } catch (IOException e) {
            System.err.println("Error loading shop data: " + e.getMessage());
            return null;
        }

        return shop;
    }
}
