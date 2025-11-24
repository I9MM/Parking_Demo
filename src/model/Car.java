package model;

import java.io.Serializable;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String ownerName;
    private String nId;
    private String plateNumber;

    public Car(String ownerName, String nId, String plateNumber) {
        this.ownerName = ownerName;
        this.nId = nId;
        this.plateNumber = plateNumber;
    }

    // getters
    public String getOwnerName() { return ownerName; }
    public String getNId() { return nId; }
    public String getPlateNumber() { return plateNumber; }
}
