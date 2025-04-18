import java.util.*;

import attempt.*;

import java.io.*;

public class App {
    private RentalShop shop;
    public static void main(String[] args) {
        Map<String, String> flags = parseArgs(args);
        RentalShop shop;
        String location = flags.get("location");
        if (location == null || location.isEmpty()) {
            System.out.println("Please provide a location using the -location flag.");
            return;
        }

        int spaces = 10;    // Default value for spaces available
        if (flags.containsKey("spaces-available")){
            spaces = Integer.parseInt(flags.get("spaces-available"));
            if (spaces <= 0) {
                System.out.println("Please provide a valid number of spaces available.");
                return;
            }
        }

        List<String> lotNames = new ArrayList<>();
        if (flags.containsKey("lot-names")) {
            String[] names = flags.get("lot-names").split(",");
            for (String name : names) {
                lotNames.add(name.trim());
            }
        } else {
            System.out.println("Please provide lot names using the --lot-names flag.");
            return;
        }

        shop = new RentalShop(location, spaces, lotNames);
        runCommandLoop(shop, location);
    }

    private static void runCommandLoop(RentalShop shop, String location) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Rental Shop at " + location + " ready. Type commands (RENT, RETURN, LIST, TRANSACTIONS). Type EXIT to quit.");
        while(true){
            System.out.print("> ");
            String input = scanner.nextLine();
            if(input == null || input.trim().equalsIgnoreCase("EXIT")){
                break;
            }
            shop.processCommand(input.trim());
        }
        scanner.close();
    }

    // Parse command line arguments.
    private static Map<String,String> parseArgs(String[] args){
        Map<String, String> flags = new HashMap<>();
        for(String arg: args){
            if(arg.startsWith("--")){
                int eq = arg.indexOf('=');
                if(eq > 0){
                    String key = arg.substring(2, eq);
                    String value = arg.substring(eq + 1);
                    flags.put(key, value);
                } else {
                    flags.put(arg, "");
                }
            }
        }
        return flags;
    }
}
