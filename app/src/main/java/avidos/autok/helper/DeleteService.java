package avidos.autok.helper;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Service to handle downloading files from Firebase Storage.
 */
public class DeleteService extends BaseTaskService {

    private static final String TAG = "Storage#DeleteService";

    /** Actions **/
    public static final String ACTION_DELETE = "action_delete";
    public static final String DELETE_COMPLETED = "delete_completed";
    public static final String DELETE_ERROR = "delete_error";

    /** Extras **/
    public static final String EXTRA_DELETE_PATH = "extra_download_path";

    private StorageReference mStorageRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DELETE.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DELETE_PATH);
            deleteFromPath(downloadPath);
        }

        return START_REDELIVER_INTENT;
    }

    private void deleteFromPath(final String downloadPath) {
        Log.d(TAG, "deleteFromPath:" + downloadPath);

        // Mark task started
        taskStarted();

        // Download and get total bytes
        mStorageRef.child(downloadPath).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "deleted:SUCCESS");

                        // Send success broadcast with number of bytes downloaded
                        Intent broadcast = new Intent(DELETE_COMPLETED);
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "delete:FAILURE", e);

                        // Send failure broadcast
                        Intent broadcast = new Intent(DELETE_ERROR);
                        broadcast.putExtra(EXTRA_DELETE_PATH, downloadPath);
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                });
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DELETE_COMPLETED);
        filter.addAction(DELETE_ERROR);

        return filter;
    }
}