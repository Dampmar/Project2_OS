package lib;

import java.io.Serializable;

public class RentRecord implements Serializable {
    private static final long serialVersionUID = 1L; // Unique ID for serialization
    Vehicle vehicle; // Vehicle being rented
    boolean discount; // Indicates if a 10% discount is applied

    // Constructor for RentRecord
    public RentRecord(Vehicle vehicle, boolean discount) {
        this.vehicle = vehicle;
        this.discount = discount;
    }
}
