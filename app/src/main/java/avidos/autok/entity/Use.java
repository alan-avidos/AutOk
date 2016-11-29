package avidos.autok.entity;

import java.io.Serializable;

/**
 * Created by Alan on 27/11/2016.
 */

public class Use implements Serializable {

    public Boolean materialTransport;
    public Boolean personal;
    public Boolean personnelTransport;
    public Boolean rent;

    public Use() {
    }

    public Use(Boolean materialTransport, Boolean personal, Boolean personnelTransport, Boolean rent) {
        this.materialTransport = materialTransport;
        this.personal = personal;
        this.personnelTransport = personnelTransport;
        this.rent = rent;
    }
}
