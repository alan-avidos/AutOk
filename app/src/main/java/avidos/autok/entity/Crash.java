package avidos.autok.entity;

/**
 * Created by Alan on 11/16/2016.
 */

public class Crash {

    public boolean accepted;
    public String pic1;
    public String pic2;
    public String pic3;

    public Crash() {
        // Default constructor required for calls to DataSnapshot.getValue(Crash.class)
    }

    public Crash(boolean accepted, String pic1, String pic2, String pic3) {
        this.accepted = accepted;
        this.pic1 = pic1;
        this.pic2 = pic2;
        this.pic3 = pic3;
    }
}
