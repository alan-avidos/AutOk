package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 10/31/2016.
 */

public class Cars implements Serializable{

    public String brand;
    public String color;
    public String engineNumber;
    public String fuelCapacity;
    public String insuranceAgent;
    public String insuranceCompany;
    public Long insurancePeriodEnd;
    public Long insurancePeriodStart;
    public String insuranceTelephone;
    public String insuranceYearlyCost;
    public String model;
    public String plate;
    public String policyNumber;
    public String serialNumber;
    public String state;
    public String type;
    public AllowedUse allowedUse;

    public Cars() {
        // Default constructor required for calls to DataSnapshot.getValue(Cars.class)
    }

    public Cars(String brand, String color, String engineNumber, String fuelCapacity, String insuranceAgent, String insuranceCompany, Long insurancePeriodEnd, Long insurancePeriodStart, String insuranceTelephone, String insuranceYearlyCost, String model, String plate, String policyNumber, String serialNumber, String state, String type, AllowedUse allowedUse) {
        this.brand = brand;
        this.color = color;
        this.engineNumber = engineNumber;
        this.fuelCapacity = fuelCapacity;
        this.insuranceAgent = insuranceAgent;
        this.insuranceCompany = insuranceCompany;
        this.insurancePeriodEnd = insurancePeriodEnd;
        this.insurancePeriodStart = insurancePeriodStart;
        this.insuranceTelephone = insuranceTelephone;
        this.insuranceYearlyCost = insuranceYearlyCost;
        this.model = model;
        this.plate = plate;
        this.policyNumber = policyNumber;
        this.serialNumber = serialNumber;
        this.state = state;
        this.type = type;
        this.allowedUse = allowedUse;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("brand", brand);
        result.put("color", color);
        result.put("engineNumber", engineNumber);
        result.put("fuelCapacity", fuelCapacity);
        result.put("insuranceAgent", insuranceAgent);
        result.put("insuranceCompany", insuranceCompany);
        result.put("insurancePeriodEnd", insurancePeriodEnd);
        result.put("insurancePeriodStart", insurancePeriodStart);
        result.put("insuranceTelephone", insuranceTelephone);
        result.put("insuranceYearlyCost", insuranceYearlyCost);
        result.put("model", model);
        result.put("plate", plate);
        result.put("policyNumber", policyNumber);
        result.put("serialNumber", serialNumber);
        result.put("state", state);
        result.put("type", type);
        result.put("allowedUse", allowedUse);
        return result;
    }
}
