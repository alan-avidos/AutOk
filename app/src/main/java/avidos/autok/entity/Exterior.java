package avidos.autok.entity;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 11/10/2016.
 */

public class Exterior {

    public String pic1;
    public String pic2;
    public String pic3;
    public String pic4;
    public Long rating;
    public Crash crash;
    public Scratch scratch;

    public Exterior() {
        // Default constructor required for calls to DataSnapshot.getValue(Exterior.class)
    }

    public Exterior(String pic1, String pic2, String pic3, String pic4, Long rating, Crash crash, Scratch scratch) {
        this.pic1 = pic1;
        this.pic2 = pic2;
        this.pic3 = pic3;
        this.pic4 = pic4;
        this.rating = rating;
        this.crash = crash;
        this.scratch = scratch;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("pic1", pic1);
        result.put("pic2", pic2);
        result.put("pic3", pic3);
        result.put("pic4", pic4);
        result.put("rating", rating);
        result.put("crash", crash);
        result.put("scratch", scratch);
        return result;
    }
}
