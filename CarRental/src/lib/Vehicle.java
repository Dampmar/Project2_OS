package lib;

public class Vehicle {
    private String licensePlate;
    private String model;
    private int kilometers;

    // Constructor 
    public Vehicle(String licensePlate, String model, int kms) {
        this.licensePlate = licensePlate;
        this.model = model;
        this.kilometers = kms;
    }

    // Getters 
    public String getLicensePlate() { return licensePlate; }
    public String getModel() { return model; }
    public int getKilometers() { return kilometers; }

    // Setters 
    public void setKilometers(int kilometers) { this.kilometers = kilometers; }

    // Stringify method to return a string representation of the vehicle
    public String toString() {
        return licensePlate + ", model: " + model + ", km: " + kilometers;
    }
}
