package avidos.autok.entity;

import java.io.Serializable;

/**
 * Created by Alan on 11/16/2016.
 */

public class WarningLights implements Serializable {

    public boolean accepted;
    public String pic;

    public WarningLights() {
        // Default constructor required for calls to DataSnapshot.getValue(Crash.class)
    }

    public WarningLights(boolean accepted, String pic) {
        this.accepted = accepted;
        this.pic = pic;
    }
}
