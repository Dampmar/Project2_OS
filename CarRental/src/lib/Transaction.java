package lib;

import java.io.*;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;    // Unique ID for serialization
    private String licensePlate;                        // License plate of the vehicle
    private int distance;                                    // Kilometers driven during the rental period  
    private boolean discount;                           // Indicates if a 10% discount is applied 
    private double amount;                              // Amount charged for the transaction

    public Transaction(String licensePlate, int kms, boolean discount, double charge) {
        this.licensePlate = licensePlate;
        this.distance = kms;
        this.discount = discount;
        this.amount = charge;
    }

    public boolean isDiscount() { return discount; }
    public double getAmount() { return amount; }
    public String getLicensePlate() { return licensePlate; }
    public int getDistance() { return distance; } 

    @Override 
    public String toString() {
        return "TRANSACTION: " + licensePlate + "; Distance: " + distance + " km; Discount: " + (discount ? "10%" : "0%") + "; Amount: $" + amount;
    }
}
