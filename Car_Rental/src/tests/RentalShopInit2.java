package tests;

import java.util.Arrays;
import java.util.List;

import classes.RentalShop;

public class RentalShopInit2 {
    public static void main(String[] args) {
        // Initialize the rental shop with a city name, number of spaces, and parking lots
        String city = "Lisbon";
        int spaces = 10;
        List<String> lots = Arrays.asList("testLot", "Lot", "Lot2");
        
        RentalShop shop = new RentalShop(city, spaces, lots);
        
        // Print the initialized shop details
        System.out.println("Rental Shop initialized in " + city + " with " + spaces + " spaces.");
        System.out.println("Parking lots: " + lots);
        shop.printShopState();
        shop.processCommand("RETURN MPH-012 1");
    } 
}
