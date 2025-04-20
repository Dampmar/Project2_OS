package tests;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.nio.Buffer;
import java.util.*;
import classes.*;
import utils.*;

/**
 * AI DISCLAIMER: This code was generated partially by an AI model. The outputs were reviewed and modified by a human developer to ensure 
 * correctness and adherence to the project's purpose.
 * PROMPT: Help me write a JUnit test for the LotManager class, while covering the main functionality of the class. 
 *  */


public class LotManagerTest {
    private static final String LOTS_DIR = "src" + File.separator + "files" + File.separator + "lots";
    private static final String LOT_NAME = "testLot";
    private File lotFile;

    @Before 
    public void setUp() {
        // Ensure the directory exists
        File dir = new File(LOTS_DIR);
        if (!dir.exists()) dir.mkdirs();
        lotFile = new File(LOTS_DIR + File.separator + LOT_NAME + ".txt");
    }

    @After 
    public void tearDown() {
        if (lotFile.exists()) lotFile.delete(); // Clean up after test
        // if (lotFile.getParentFile().exists()) lotFile.getParentFile().delete(); // Clean up directory
    }

    @Test 
    public void testCreateLot() {
        String[] args = {"--lot-name=" + LOT_NAME, "--add-sedan=2", "--add-suv=3", "--add-van=1"};
        LotManager.main(args);

        // Check if the lot file was created and is not empty
        assertTrue("Lot file should exist after creation", lotFile.exists());
        assertTrue("Lot file should not be empty", lotFile.length() > 0);

        // Check if the lot file contains the expected data
        try (BufferedReader reader = new BufferedReader(new FileReader(lotFile))) {
            int count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            assertEquals("Lot file should contain at least one line of data", count, 6);
        } catch (IOException e) {
            fail("IOException while reading lot file: " + e.getMessage());
        } 
    }

    @Test 
    public void testRemoveVehicle() {
        // Prepopulate the lot file with some vehicles 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lotFile))){
            writer.write("ABC-123;Sedan;1000\n");
            writer.write("DEF-456;SUV;1500\n");
            writer.write("GHI-789;Van;2000\n");
        } catch (IOException e) {
            fail("IOException while writing to lot file: " + e.getMessage());
        } 

        // Generate the command to remove a vehicle
        String[] args = {
            "--lot-name=" + LOT_NAME, 
            "--remove-vehicle=ABC-123"
        };

        LotManager.main(args);
        try (BufferedReader reader = new BufferedReader(new FileReader(lotFile))) {
            String line;
            boolean vehicleFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ABC-123")) {
                    vehicleFound = true;
                    break;
                }
            }
            assertFalse("Vehicle should have been removed from the lot", vehicleFound);
        } catch (IOException e) {
            fail("IOException while reading lot file: " + e.getMessage());
        }
    }
}