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
public class DownloadService extends BaseTaskService {

    private static final String TAG = "Storage#DownloadService";

    /** Actions **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_ERROR = "download_error";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";
    public static final String EXTRA_PICTURE_ID = "extra_picture_id";

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

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
            String picId = intent.getStringExtra(EXTRA_PICTURE_ID);
            downloadFromPath(downloadPath, picId);
        }

        return START_REDELIVER_INTENT;
    }

    private void downloadFromPath(final String downloadPath, final String picId) {
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();

        // Download and get total bytes
        mStorageRef.child(downloadPath).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "download:SUCCESS" + " " + uri.toString());

                        // Send success broadcast with number of bytes downloaded
                        Intent broadcast = new Intent(DOWNLOAD_COMPLETED);
                        broadcast.putExtra(EXTRA_DOWNLOAD_PATH, uri.toString());
                        broadcast.putExtra(EXTRA_PICTURE_ID, picId);
                        broadcast.putExtra(EXTRA_BYTES_DOWNLOADED, 20);
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "download:FAILURE", e);

                        // Send failure broadcast
                        Intent broadcast = new Intent(DOWNLOAD_ERROR);
                        broadcast.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                });
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_COMPLETED);
        filter.addAction(DOWNLOAD_ERROR);

        return filter;
    }
}