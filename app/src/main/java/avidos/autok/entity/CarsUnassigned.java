package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 10/26/2016.
 */

public class CarsUnassigned implements Serializable {

    public String model;
    public String plate;

    public CarsUnassigned() {
        // Default constructor required for calls to DataSnapshot.getValue(CarsUnassigned.class)
    }

    public CarsUnassigned(String model, String plate) {
        this.model = model;
        this.plate = plate;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("model", model);
        result.put("plate", plate);
        return result;
    }
}
