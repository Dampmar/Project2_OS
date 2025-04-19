package classes;

public class Vehicle implements java.io.Serializable {
    private static final long serialVersionUID = 1L; // Unique ID for serialization
    private String licensePlate;
    private String type;
    private int odometer; 

    // Constructor 
    public Vehicle(String licensePlate, String type, int odometer) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.odometer = odometer;
    }

    // Getters
    public String getLicensePlate() { return licensePlate; }
    public String getType() { return type; }
    public int getOdometer() { return odometer; }

    // Setters
    public void addToOdometer(int kilometers) { this.odometer += kilometers; }

    @Override
    public String toString() {
        return licensePlate 
            + "," + type 
            + "," + odometer; 
    }
}