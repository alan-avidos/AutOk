package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 11/4/2016.
 */

public class FuelLoad {

    public Long amount;
    public Double costPerLiter;
    public Long km;

    public FuelLoad() {
        // Default constructor required for calls to DataSnapshot.getValue(FuelLoad.class)
    }

    public FuelLoad(Long amount, Double costPerLiter, Long km) {
        this.amount = amount;
        this.costPerLiter = costPerLiter;
        this.km = km;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("costPerLiter", costPerLiter);
        result.put("km", km);

        return result;
    }
}
