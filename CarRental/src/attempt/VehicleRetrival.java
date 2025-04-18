package attempt;

import attempt.Vehicle; 

public class VehicleRetrival {
    private Vehicle vehicle;
    private String lot; 

    public VehicleRetrival(Vehicle vehicle, String lot) {
        this.vehicle = vehicle;
        this.lot = lot;
    }

    public Vehicle getVehicle() { return vehicle; }
    public String getLot() { return lot; }
}
