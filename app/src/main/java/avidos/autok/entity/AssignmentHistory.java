package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 13/12/2016.
 */

public class AssignmentHistory extends Assignment implements Serializable {

    public boolean released;

    public AssignmentHistory() {
    }

    public AssignmentHistory(Long end, Long start, String uid, String use, String type, String userName, String destination, Double fuelLevel, Long mileage, Check check, boolean released) {
        super(end, start, uid, use, type, userName, destination, fuelLevel, mileage, check);
        this.released = released;
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
        result.put("released", released);

        return result;
    }
}
