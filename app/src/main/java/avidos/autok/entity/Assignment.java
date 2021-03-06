package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 11/2/2016.
 */

public class Assignment implements Serializable{

    public Long end;
    public Long start;
    public String uid;
    public String use;
    public String type;
    public String userName;
    public String destination;
    public Double fuelLevel;
    public Long mileage;
    public Check check;

    public Assignment() {
        // Default constructor required for calls to DataSnapshot.getValue(Assignment.class)
    }

    public Assignment(Long end, Long start, String uid, String use, String type, String userName, String destination, Double fuelLevel, Long mileage, Check check) {
        this.end = end;
        this.start = start;
        this.uid = uid;
        this.use = use;
        this.type = type;
        this.userName = userName;
        this.destination = destination;
        this.fuelLevel = fuelLevel;
        this.mileage = mileage;
        this.check = check;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("end", end);
        result.put("start", start);
        result.put("uid", uid);
        result.put("use", use);
        result.put("type", type);
        result.put("userName", userName);
        result.put("destination", destination);
        result.put("fuelLevel", fuelLevel);
        result.put("mileage", mileage);
        result.put("check", check);

        return result;
    }
}
