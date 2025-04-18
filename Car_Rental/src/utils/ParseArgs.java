package utils;

import java.util.*;

public class ParseArgs {
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                int equalsIndex = arg.indexOf('=');
                if (equalsIndex > 0) {
                    String key = arg.substring(2, equalsIndex).trim();
                    String value = arg.substring(equalsIndex + 1).trim();
                    parsedArgs.put(key, value);
                }
            }
        }
        return parsedArgs;
    }
}
