package avidos.autok.entity;

/**
 * Created by Alan on 27/11/2016.
 */

public class AllowedUse {
    public Use external;
    public Use local;

    public AllowedUse() {
    }

    public AllowedUse(Use external, Use internal) {
        this.external = external;
        this.local = internal;
    }
}
