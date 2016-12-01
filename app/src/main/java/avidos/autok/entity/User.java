package avidos.autok.entity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alan on 10/20/2016.
 */

@IgnoreExtraProperties
public class User implements Serializable{

    public String adminUid;
    public String bloodType;
    public String company;
    public String job;
    public String name;
    public String password;
    public String telephone;
    public String email;
    public String assignation;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String adminUid, String bloodType, String company, String job, String name, String password, String telephone, String email, String assignation) {
        this.adminUid = adminUid;
        this.bloodType = bloodType;
        this.company = company;
        this.job = job;
        this.name = name;
        this.password = password;
        this.telephone = telephone;
        this.email = email;
        this.assignation = assignation;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("adminUid", adminUid);
        result.put("bloodType", bloodType);
        result.put("company", company);
        result.put("email", email);
        result.put("job", job);
        result.put("name", name);
        result.put("password", password);
        result.put("telephone", telephone);
        result.put("assignation", assignation);
        return result;
    }
}