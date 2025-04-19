package utils;

import java.io.*;
import java.util.*;
import classes.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RentalFileManager {
    private static final String FILES_DIR = "src" + File.separator + "files" + File.separator + "shops";
    private static final String RENT_FILE = FILES_DIR + File.separator + "rentals.txt";

    /**
     * Add rental information to the rental file.
     * @param rentInfo RentInfo object containing vehicle and discount information.
     * @return true if the information was added successfully, false otherwise.
     */
    public static boolean addToRentalFile(RentInfo rentInfo) {
        File file = new File(RENT_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("ERROR: Could not create rental file: " + e.getMessage());
                return false;
            }
        }

        // Append the rental information to the file 
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            // Acquire an exclusive lock on the file 
            FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
            try {
                raf.seek(raf.length());
                Vehicle vehicle = rentInfo.getVehicle();
                boolean isDiscounted = rentInfo.isDiscount();
                String line = vehicle.getLicensePlate() + ","
                    + vehicle.getType() + ","
                    + vehicle.getOdometer() + ","
                    + isDiscounted + "\n";
                raf.writeBytes(line);
            } finally {
                // Release the lock 
                lock.release();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not write to rental file: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Check if a vehicle is already rented.
     * @param licensePlate The license plate of the vehicle to check.
     * @return RentInfo object if found, null otherwise.
     */
    public static RentInfo checkRentalRecord(String licensePlate) {
        RentInfo rentInfo = null;
        File file = new File(RENT_FILE);
        if (!file.exists()) return rentInfo;

        // Read the rental file to check for the vehicle
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {
            
            // Acquire a shared lock on the file
            FileLock lock = channel.lock(0, Long.MAX_VALUE, true);
            try {
                String line;
                while ((line = raf.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4 && parts[0].trim().equals(licensePlate)) {
                        String plate = parts[0].trim();
                        String type = parts[1].trim();
                        int odometer = Integer.parseInt(parts[2].trim());
                        boolean isDiscounted = Boolean.parseBoolean(parts[3].trim());
                        
                        // Create a RentInfo object with the found data
                        rentInfo = new RentInfo(new Vehicle(plate, type, odometer), isDiscounted);
                        break; // Exit loop if vehicle is found
                    }
                }
            } finally {
                // Release the lock
                lock.release();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not read rental file: " + e.getMessage());
        }
        return rentInfo;
    }

    /**
     * Delete a rental record from the file -> vehicle is returned.
     * @param licensePlate The license plate of the vehicle to delete.
     */
    public static boolean removeFromRentalFile(String licensePlate) {
        File file = new File(RENT_FILE);
        if (!file.exists()) return false;

        // Rewrite the rentals except the one to be removed 
        List<String> remainingRentals = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            
            // Acquire an exclusive lock on the file
            FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
            try {
                String line;
                while ((line = raf.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4 && !parts[0].trim().equals(licensePlate)) {
                        remainingRentals.add(line); // Keep the rental record
                    }
                }

                // Clear the file and write the remaining rentals back
                raf.setLength(0); // Clear the file
                for (String rental : remainingRentals) {
                    raf.writeBytes(rental + "\n"); // Write each remaining rental
                }
            } finally {
                // Release the lock
                lock.release();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not access rental file: " + e.getMessage());
            return false;
        }
        return true;
    }
}
