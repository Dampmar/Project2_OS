package tests;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import classes.*;
import utils.*;

/**
 * AI DISCLAIMER: This code was generated partially by an AI model. The outputs were reviewed and modified by a human developer to ensure 
 * correctness and adherence to the project's purpose.
 * PROMPT: Help me write a JUnit test for the RentalShop class, while covering the main functionality of the class. 
 *  */

public class RentalShopTest {
    private static final String SHOPS_DIR = "src" + File.separator + "files" + File.separator + "shops";
    private static final String LOTS_DIR = "src" + File.separator + "files" + File.separator + "lots";
    private static final String RENTAL_FILE = SHOPS_DIR + File.separator + "rentals.txt";
    private static final String TEST_CITY = "testCity";
    private File shopFile;
    private File rentalFile;
    private File testLotFile;
    private RentalShop shop;
    
    // Capture System.out and System.err for testing output
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        // Set up directories
        new File(SHOPS_DIR).mkdirs();
        new File(LOTS_DIR).mkdirs();
        
        // Create test files
        shopFile = new File(SHOPS_DIR + File.separator + TEST_CITY + ".txt");
        rentalFile = new File(RENTAL_FILE);
        testLotFile = new File(LOTS_DIR + File.separator + "testLot.txt");
        
        // Delete files if they exist
        if (shopFile.exists()) shopFile.delete();
        if (rentalFile.exists()) rentalFile.delete();
        if (testLotFile.exists()) testLotFile.delete();
        
        // Create and populate test lot file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testLotFile))) {
            writer.write("ABC-123,SEDAN,0\n");
            writer.write("DEF-456,SUV,0\n");
            writer.write("GHI-789,VAN,0\n");
        } catch (IOException e) {
            fail("Could not set up test lot file: " + e.getMessage());
        }
        
        // Set up output capture first
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        
        // Set up shop
        List<String> lots = new ArrayList<>();
        lots.add("testLot");
        shop = new RentalShop(TEST_CITY, 10, lots);
        
        // Clear output buffer to remove initialization messages
        outContent.reset();
        errContent.reset();
    }
    
    @After
    public void tearDown() {
        // Clean up files
        if (shopFile.exists()) shopFile.delete();
        if (rentalFile.exists()) rentalFile.delete();
        if (testLotFile.exists()) testLotFile.delete();
        
        // Restore output streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    public void testShopInitialization() {
        // Test that the shop initializes correctly
        assertEquals(TEST_CITY, shop.getCity());
        assertEquals(10, shop.getSpaces());
        assertTrue(shop.getLots().contains("testLot"));
        
        // Test that vehicles are loaded from lots during initialization
        assertTrue("Shop should have initialized with vehicles from lot", shop.getVehicles().size() > 0);
    }
    
    @Test
    public void testRentVehicle() throws Exception {
        // Use reflection to access private method
        Method rentVehicleMethod = RentalShop.class.getDeclaredMethod("rentVehicle", String.class);
        rentVehicleMethod.setAccessible(true);
        
        // Get initial vehicle count
        int initialVehicleCount = shop.getVehicles().size();
        
        // Test renting a SEDAN
        rentVehicleMethod.invoke(shop, "SEDAN");
        
        // Check if a vehicle was removed from the shop
        assertEquals("Vehicle should be removed from shop after renting", 
            initialVehicleCount - 1, shop.getVehicles().size());
        
        // Check if rental was recorded
        assertTrue("Rental should be recorded in output", 
            outContent.toString().contains("rented successfully"));
    }
    
    @Test
    public void testRentInvalidVehicleType() throws Exception {
        // Use reflection to access private method
        Method rentVehicleMethod = RentalShop.class.getDeclaredMethod("rentVehicle", String.class);
        rentVehicleMethod.setAccessible(true);
        
        // Get initial vehicle count
        int initialVehicleCount = shop.getVehicles().size();
        
        // Test renting with invalid vehicle type
        rentVehicleMethod.invoke(shop, "INVALID_TYPE");
        
        // Check if error message was displayed and no vehicle was removed
        assertEquals("No vehicle should be removed for invalid type", 
            initialVehicleCount, shop.getVehicles().size());
        assertTrue("Error message should be displayed for invalid vehicle type", 
            outContent.toString().contains("ERROR: Invalid vehicle type"));
    }
    
    @Test
    public void testReturnVehicle() throws Exception {
        // First rent a vehicle
        Method rentVehicleMethod = RentalShop.class.getDeclaredMethod("rentVehicle", String.class);
        rentVehicleMethod.setAccessible(true);
        rentVehicleMethod.invoke(shop, "SEDAN");
        
        // Clear output
        outContent.reset();
        
        // Get the license plate from the rental file
        String licensePlate = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(rentalFile))) {
            String line = reader.readLine();
            if (line != null) {
                licensePlate = line.split(",")[0];
            }
        } catch (IOException e) {
            fail("Could not read rental file: " + e.getMessage());
        }
        
        assertNotNull("License plate should be found in rental file", licensePlate);
        
        // Get initial balance
        double initialBalance = shop.getBalance();
        
        // Return the vehicle
        Method returnVehicleMethod = RentalShop.class.getDeclaredMethod("returnVehicle", String.class, int.class);
        returnVehicleMethod.setAccessible(true);
        returnVehicleMethod.invoke(shop, licensePlate, 100);
        
        // Check if balance increased
        assertTrue("Balance should increase after vehicle return", shop.getBalance() > initialBalance);
        
        // Check if a transaction was added
        assertEquals("A transaction should be added", 1, shop.getTransactions().size());
        
        // Check if vehicle is back in the shop
        boolean vehicleFound = false;
        for (Vehicle v : shop.getVehicles()) {
            if (v.getLicensePlate().equals(licensePlate)) {
                vehicleFound = true;
                assertEquals("Odometer should be updated", 100, v.getOdometer());
                break;
            }
        }
        assertTrue("Returned vehicle should be back in the shop", vehicleFound);
    }
    
    @Test
    public void testReturnNonExistentVehicle() throws Exception {
        // Try to return a non-existent vehicle
        Method returnVehicleMethod = RentalShop.class.getDeclaredMethod("returnVehicle", String.class, int.class);
        returnVehicleMethod.setAccessible(true);
        returnVehicleMethod.invoke(shop, "NON-EXISTENT", 100);
        
        // Check for error message
        assertTrue("Error message should be displayed for non-existent vehicle", 
            outContent.toString().contains("ERROR: Vehicle NON-EXISTENT not found"));
    }
    
    @Test
    public void testReturnWithInvalidKilometers() throws Exception {
        // First rent a vehicle
        Method rentVehicleMethod = RentalShop.class.getDeclaredMethod("rentVehicle", String.class);
        rentVehicleMethod.setAccessible(true);
        rentVehicleMethod.invoke(shop, "SEDAN");
        
        // Get the license plate from the rental file
        String licensePlate = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(rentalFile))) {
            String line = reader.readLine();
            if (line != null) {
                licensePlate = line.split(",")[0];
            }
        } catch (IOException e) {
            fail("Could not read rental file: " + e.getMessage());
        }
        
        // Clear output
        outContent.reset();
        
        // Try to return with negative kilometers
        Method returnVehicleMethod = RentalShop.class.getDeclaredMethod("returnVehicle", String.class, int.class);
        returnVehicleMethod.setAccessible(true);
        returnVehicleMethod.invoke(shop, licensePlate, -10);
        
        // Check for error message
        assertTrue("Error message should be displayed for invalid kilometers", 
            outContent.toString().contains("ERROR: Invalid kilometers"));
    }
    
    @Test
    public void testProcessCommand() {
        // Test LIST command
        shop.processCommand("LIST");
        assertTrue("LIST command should display shop state", 
            outContent.toString().contains("SHOP STATE"));
        
        // Reset output
        outContent.reset();
        
        // Test TRANSACTIONS command
        shop.processCommand("TRANSACTIONS");
        assertTrue("TRANSACTIONS command should display transactions", 
            outContent.toString().contains("SHOP Transactions"));
        
        // Reset output
        outContent.reset();
        
        // Test invalid command
        shop.processCommand("INVALID_COMMAND");
        assertTrue("Invalid command should display error message", 
            outContent.toString().contains("ERROR: Unknown command"));
    }
    
    @Test
    public void testCheckThreshold() throws Exception {
        // Add vehicles to reach the threshold
        for (int i = 0; i < 9; i++) {
            Vehicle vehicle = new Vehicle("TEST-" + i, "SEDAN", i * 100);
            shop.addVehicle(vehicle);
        }
        
        // Manually invoke the checkThreshold method
        Method checkThresholdMethod = RentalShop.class.getDeclaredMethod("checkThreshold");
        checkThresholdMethod.setAccessible(true);
        checkThresholdMethod.invoke(shop);
        
        // Check if warning was displayed and vehicles were redistributed
        assertTrue("Warning message should be displayed when below threshold", 
            outContent.toString().contains("WARNING: Available parking spaces below threshold"));
        assertTrue("Some vehicles should be redistributed", 
            outContent.toString().contains("Moving"));
    }
}