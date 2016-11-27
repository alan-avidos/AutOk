package avidos.autok.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import avidos.autok.R;
import avidos.autok.entity.User;
import avidos.autok.helper.DownloadService;
import avidos.autok.helper.UploadService;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private static final String LOAD_TAG = "param1";

    // UI references.
    private EditText mNameView;
    private EditText mUserView;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private Button mButtonUpdate;
    private Button mButtonCancel;
    private CircleImageView mProfileImage;

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;
    private DatabaseReference mDatabase;
    private DatabaseReference mUpdateDatabase;
    private FirebaseUser mUser;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    private User user;
    private String adminUid;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mNameView = (EditText) view.findViewById(R.id.editText_name);
        mUserView = (EditText) view.findViewById(R.id.editText_user);
        mPasswordView = (EditText) view.findViewById(R.id.editText_password);
        mPhoneView = (EditText) view.findViewById(R.id.editText_phone);
        mButtonUpdate = (Button) view.findViewById(R.id.update_button);
        mButtonCancel = (Button) view.findViewById(R.id.cancel_button);
        mProfileImage = (CircleImageView) view.findViewById(R.id.profile_image);

        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.update_button:
                        updateUser(user.adminUid, user.bloodType, user.company, user.job, user.name, user.password, mPhoneView.getText().toString(), user.email);
                        break;
                    case R.id.cancel_button:
                        mPhoneView.setText(user.telephone);
                        break;
                    case R.id.profile_image:
                        launchCamera();
                        break;
                    default:
                        break;
                }
            }
        };

        mButtonUpdate.setOnClickListener(onClickListener);
        mButtonCancel.setOnClickListener(onClickListener);
        mProfileImage.setOnClickListener(onClickListener);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        readUserAdmin();

        beginDownload();

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOAD_TAG, "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {
                    case DownloadService.DOWNLOAD_COMPLETED:
                        Picasso.with(context).load(intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)).noFade().into(mProfileImage);

                        break;
                    case DownloadService.DOWNLOAD_ERROR:
                        // Alert failure
                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));
                        break;
                    case UploadService.UPLOAD_COMPLETED:
                        beginDownload();
                        break;
                    case UploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };

        return view;
    }

    final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            //mButtonUpdate.setVisibility(View.VISIBLE);
            //mButtonCancel.setVisibility(View.VISIBLE);
            mButtonUpdate.setEnabled(true);
            mButtonCancel.setEnabled(true);
        }
    };

    public void updateUser (String adminUid, String bloodType, String company, String job, String name, String password, String telephone, String email) {

        mUpdateDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(this.adminUid);
        User userValues = new User(adminUid, bloodType, company, job, name, password, telephone, email);
        Map<String, Object> userMap = userValues.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + mUser.getUid(), userMap);
        mUpdateDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getContext(), "Número de telefono actualizado", Toast.LENGTH_LONG).show();
                    mButtonUpdate.setEnabled(false);
                    mButtonUpdate.setEnabled(false);
                }
                try {
                    throw task.getException();
                }catch(FirebaseException e) {
                    Toast.makeText(getContext(), "Ocurrió un error de conexión. intente de nuevo.", Toast.LENGTH_LONG).show();
                    Log.e(LOAD_TAG, "e: " + e.getMessage());
                } catch (Exception e) {
                    // Do nothing...
                }
            }
        });
    }

    private void readUserAdmin() {

        showProgressDialog();

        try {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("userToAdmin").child(mUser.getUid()).child("adminUid");
        } catch (NullPointerException npe) {
            hideProgressDialog();
            readUserAdmin();
            return;
        }

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                adminUid = dataSnapshot.getValue(String.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(LOAD_TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    public void updateUI() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(adminUid).child(mUser.getUid());

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get User object and use the values to update the UI
                hideProgressDialog();
                user = dataSnapshot.getValue(User.class);
                mNameView.setText(user.name);
                mUserView.setText(user.email);
                mPasswordView.setText(user.password);
                mPhoneView.setText(user.telephone);
                mPhoneView.addTextChangedListener(textWatcher);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting User failed, log a message
                // Getting User failed, log a message
                Log.w(LOAD_TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mBroadcastReceiver, DownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String fragment);
    }

    @AfterPermissionGranted(RC_STORAGE_PERMS)
    private void launchCamera() {
        Log.d(LOAD_TAG, "launchCamera");

        // Check that we have permission to read images from external storage.
        String perm = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (!EasyPermissions.hasPermissions(getContext(), perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERMS, perm);
            return;
        }

        // Choose file storage location, must be listed in res/xml/file_paths.xml
        File dir = new File(Environment.getExternalStorageDirectory() + "/photos");
        File file = new File(dir, UUID.randomUUID().toString() + ".jpg");
        try {
            // Create directory if it does not exist.
            if (!dir.exists()) {
                dir.mkdir();
            }
            boolean created = file.createNewFile();
            Log.d(LOAD_TAG, "file.createNewFile:" + file.getAbsolutePath() + ":" + created);
        } catch (IOException e) {
            Log.e(LOAD_TAG, "file.createNewFile" + file.getAbsolutePath() + ":FAILED", e);
        }

        // Create content:// URI for file, required since Android N
        // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        mFileUri = FileProvider.getUriForFile(getContext(),
                "com.google.firebase.quickstart.firebasestorage.fileprovider", file);

        // Create and launch the intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        // Grant permission to camera (this is required on KitKat and below)
        List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            final String packageName = resolveInfo.activityInfo.packageName;
            final Intent intent = new Intent(takePictureIntent);
            getActivity().grantUriPermission(packageName, mFileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, RC_TAKE_PICTURE);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOAD_TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (resultCode == RESULT_OK) {
            if (requestCode == RC_TAKE_PICTURE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = mFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                uploadFromUri(selectedImageUri);
            }
        }
    }

    private void beginDownload() {
        // Get path
        String path = "pictures/" + mUser.getUid() + ".jpg";

        // Kick off DownloadService to download the file
        Intent intent = new Intent(getContext(), DownloadService.class)
                .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, path)
                .putExtra(DownloadService.EXTRA_PICTURE_ID, "profilePic")
                .setAction(DownloadService.ACTION_DOWNLOAD);
        getActivity().startService(intent);

        // Show loading spinner
        showProgressDialog();
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(LOAD_TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Toast message in case the user does not see the notification
        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        // Start UploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        getContext().startService(new Intent(getContext(), UploadService.class)
                .putExtra(UploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(UploadService.EXTRA_UPLOAD_FILENAME, mUser.getUid())
                .setAction(UploadService.ACTION_UPLOAD));
    }

    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from UploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(UploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(UploadService.EXTRA_FILE_URI);
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                    Toast.makeText(getContext(),"Revise su conexión a internet",Toast.LENGTH_LONG).show();
                }
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 10000);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
