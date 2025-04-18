package lib;

public class Vehicle {
    private String licensePlate;
    private String model;
    private int odometer;

    // Constructor 
    public Vehicle(String licensePlate, String model, int kms) {
        this.licensePlate = licensePlate;
        this.model = model;
        this.odometer = kms;
    }

    // Getters 
    public String getLicensePlate() { return licensePlate; }
    public String getModel() { return model; }
    public int getOdometer() { return odometer; }

    // Setters 
    public void setKilometers(int kilometers) { this.odometer = kilometers; }

    // Stringify method to return a string representation of the vehicle
    public String toString() {
        return licensePlate + ", model: " + model + ", km: " + odometer;
    }
}
