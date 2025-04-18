package attempt;

public class RentInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L; // Unique ID for serialization
    private Vehicle vehicle; // Vehicle being rented
    private boolean discount; // Indicates if a 10% discount is applied

    // Constructor for RentInfo
    public RentInfo(Vehicle vehicle, boolean discount) {
        this.vehicle = vehicle;
        this.discount = discount;
    }

    public Vehicle getVehicle() { return vehicle; }
    public boolean isDiscount() { return discount; }
}
