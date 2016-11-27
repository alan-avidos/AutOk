package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 11/15/2016.
 */

public class Interior {

    public Long rating;
    public WarningLights warningLights;
    public Seats seats;
    public AC ac;
    public Radio radio;
    public Mat mat;

    public Interior() {
    }

    public Interior(Long rating, WarningLights warningLights, Seats seats, AC ac, Radio radio, Mat mat) {
        this.rating = rating;
        this.warningLights = warningLights;
        this.seats = seats;
        this.ac = ac;
        this.radio = radio;
        this.mat = mat;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("rating", rating);
        result.put("warningLights", warningLights);
        result.put("seats", seats);
        result.put("ac", ac);
        result.put("radio", radio);
        result.put("mat", mat);
        return result;
    }
}
