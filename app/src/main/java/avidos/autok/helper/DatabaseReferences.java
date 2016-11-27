package avidos.autok.helper;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

/**
 * Created by Alan on 11/18/2016.
 */

public class DatabaseReferences implements Parcelable {

    private StorageReference storageReference;

    public DatabaseReferences() {
    }

    protected DatabaseReferences(Parcel in) {
    }

    public static final Creator<DatabaseReferences> CREATOR = new Creator<DatabaseReferences>() {
        @Override
        public DatabaseReferences createFromParcel(Parcel in) {
            return new DatabaseReferences(in);
        }

        @Override
        public DatabaseReferences[] newArray(int size) {
            return new DatabaseReferences[size];
        }
    };

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
