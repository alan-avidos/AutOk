package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 09/12/2016.
 */

public class Check implements Serializable {

    public Interior interior;
    public Exterior exterior;

    public Check() {
    }

    public Check(Interior interior, Exterior exterior) {
        this.interior = interior;
        this.exterior = exterior;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("end", interior);
        result.put("start", exterior);

        return result;
    }
}
