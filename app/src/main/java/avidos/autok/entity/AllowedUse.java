package avidos.autok.entity;

import java.io.Serializable;

/**
 * Created by Alan on 27/11/2016.
 */

public class AllowedUse implements Serializable {
    public Use external;
    public Use local;

    public AllowedUse() {
    }

    public AllowedUse(Use external, Use internal) {
        this.external = external;
        this.local = internal;
    }
}
