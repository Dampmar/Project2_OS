import java.util.*;
import utils.*;
import classes.*;

public class App {
    /**
     * Main method for testing the RentalShop class
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = ParseArgs.parseArgs(args);
        String city = params.get("location");
        if (city == null || city.isEmpty() || city.equals("rentals")) {
            System.out.println("ERROR: Please provide a valid city name using --location=<city>. P.S. rentals is not a valid city name.");
            return;
        }
        
        int spaces = (params.containsKey("spaces-available")) ? Integer.parseInt(params.get("spaces-available")) : 10; // Default spaces available
        List<String> lots = new ArrayList<>();
        lots = (params.containsKey("lots")) ? Arrays.asList(params.get("lots").split(",")) : new ArrayList<>(); // Default lots available

        RentalShop shop = new RentalShop(city, spaces, lots); // Create a new RentalShop instance
        shop.run(); // Start the shop
    }
}
