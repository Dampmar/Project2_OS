package tests;

import classes.Vehicle;
import java.io.*;

public class VehicleSerializationTest {
    public static void main(String[] args) {
        try {
            // Create a test vehicle
            Vehicle testVehicle = new Vehicle("ABC-123", "Sedan", 1000);
            
            // Try to serialize it
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(testVehicle);
            oos.close();
            
            System.out.println("Vehicle successfully serialized!");
            
            // Try to deserialize
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Vehicle deserializedVehicle = (Vehicle) ois.readObject();
            ois.close();
            
            System.out.println("Vehicle successfully deserialized: " + deserializedVehicle);
            
        } catch (Exception e) {
            System.err.println("Serialization error: " + e);
            e.printStackTrace();
        }
    }
}