package utils;

import classes.Vehicle;

public class VehicleFactory {
    private static final String[] VEHICLE_TYPES = {"SEDAN", "SUV", "VAN"};

    public static Vehicle createVehicle(String type) {
        boolean isValidType = false;
        for (String vehicleType : VEHICLE_TYPES) {
            if (vehicleType.equalsIgnoreCase(type)) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            throw new IllegalArgumentException("Invalid vehicle type. Valid types are: SEDAN, SUV, VAN.");
        }
        
        String licensePlate = LicensePlateGenerator.generateLicensePlate();
        int odometer = 0; // Default odometer value
        return new Vehicle(licensePlate, type, odometer);
    }
}
