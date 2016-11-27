package avidos.autok.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyleduo.switchbutton.SwitchButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import avidos.autok.R;
import avidos.autok.entity.AC;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.Exterior;
import avidos.autok.entity.Interior;
import avidos.autok.entity.Mat;
import avidos.autok.entity.Radio;
import avidos.autok.entity.Seats;
import avidos.autok.entity.User;
import avidos.autok.entity.WarningLights;
import avidos.autok.helper.DownloadService;
import avidos.autok.helper.UploadService;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InteriorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InteriorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InteriorFragment extends Fragment {

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";

    //
    private User mUser;
    private Cars mCar;
    private Interior mInterior;
    private WarningLights mWarningLights;
    private Seats mSeats;
    private Radio mRadio;
    private AC mAC;
    private Mat mMat;
    private Assignment mAssignment;
    private boolean mIsRatingModified = false;

    // UI
    private TextView mCarInfoView;
    private CircleImageView mCarImage;
    private RangeBar mRatingBar;
    private CircleImageView mWarningLightsImage;
    private SwitchButton mWarningLightsSwitch;
    private CircleImageView mSeatsImage;
    private SwitchButton mSeatsSwitch;
    private CircleImageView mACImage;
    private SwitchButton mACSwitch;
    private CircleImageView mRadioImage;
    private SwitchButton mRadioSwitch;
    private CircleImageView mMatImage;
    private SwitchButton mMatSwitch;
    private FloatingActionButton mDoneButton;

    // FireBase
    private DatabaseReference mDatabaseCheck;

    // Storage
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String mFileName = null;
    private static final String TAG = "Storage#MainActivity";
    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private OnFragmentInteractionListener mListener;

    public InteriorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user User data.
     * @param car Car selected.
     * @return A new instance of fragment InteriorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InteriorFragment newInstance(User user, Cars car, Assignment assignment) {
        InteriorFragment fragment = new InteriorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CAR, car);
        args.putSerializable(ARG_USER, user);
        args.putSerializable(ARG_ASSIGNMENT, assignment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCar = (Cars) getArguments().getSerializable(ARG_CAR);
            mUser = (User) getArguments().getSerializable(ARG_USER);
            mAssignment = (Assignment) getArguments().getSerializable(ARG_ASSIGNMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_interior, container, false);

        mCarInfoView = (TextView) view.findViewById(R.id.car_info_interior);
        mCarImage = (CircleImageView) view.findViewById(R.id.car_image_interior);
        mRatingBar = (RangeBar) view.findViewById(R.id.ratingBar_interior);
        mWarningLightsImage = (CircleImageView) view.findViewById(R.id.car_image_warning_lights);
        mWarningLightsSwitch = (SwitchButton) view.findViewById(R.id.switch_warning_lights);
        mSeatsImage = (CircleImageView) view.findViewById(R.id.car_image_seats);
        mSeatsSwitch = (SwitchButton) view.findViewById(R.id.switch_seats);
        mACImage = (CircleImageView) view.findViewById(R.id.car_image_ac);
        mACSwitch = (SwitchButton) view.findViewById(R.id.switch_ac);
        mRadioImage = (CircleImageView) view.findViewById(R.id.car_image_radio);
        mRadioSwitch = (SwitchButton) view.findViewById(R.id.switch_radio);
        mMatImage = (CircleImageView) view.findViewById(R.id.car_image_mat);
        mMatSwitch = (SwitchButton) view.findViewById(R.id.switch_mat);
        mDoneButton = (FloatingActionButton) view.findViewById(R.id.fab_interior_done);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mRatingBar.setSeekPinByValue(0);
        mRatingBar.setOnRangeBarChangeListener(onRangeBarChangeListener);
        mCarInfoView.setText(getString(R.string.title_car_info, mCar.brand, mCar.model, mCar.plate));

        mWarningLightsSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mSeatsSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mACSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mRadioSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mMatSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

        mWarningLightsImage.setOnClickListener(onImageClickListener);
        mSeatsImage.setOnClickListener(onImageClickListener);
        mACImage.setOnClickListener(onImageClickListener);
        mRadioImage.setOnClickListener(onImageClickListener);
        mMatImage.setOnClickListener(onImageClickListener);

        readInteriorCheck();
        downloadImages();
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {
                    case DownloadService.DOWNLOAD_COMPLETED:
                        // Get number of bytes downloaded
                        //long numBytes = intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0);
                        String picId = intent.getStringExtra(DownloadService.EXTRA_PICTURE_ID);
                        String downloadPath = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);
                        updateCircleImageViews(picId, downloadPath);

                        break;
                    case DownloadService.DOWNLOAD_ERROR:
                        // Alert failure
/*                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));*/
                        break;
                    case UploadService.UPLOAD_COMPLETED:
                        downloadImages();
                    case UploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };

        return view;
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.switch_warning_lights:
                    mInterior.warningLights.accepted = isChecked;
                    writeExteriorCheck();
                    break;
                case R.id.switch_seats:
                    mInterior.seats.accepted = isChecked;
                    writeExteriorCheck();
                    break;
                case R.id.switch_ac:
                    mInterior.ac.accepted = isChecked;
                    writeExteriorCheck();
                    break;
                case R.id.switch_radio:
                    mInterior.radio.accepted = isChecked;
                    writeExteriorCheck();
                    break;
                case R.id.switch_mat:
                    mInterior.mat.accepted = isChecked;
                    writeExteriorCheck();
                    break;
                default:
                    break;
            }
        }
    };

    RangeBar.OnRangeBarChangeListener onRangeBarChangeListener = new RangeBar.OnRangeBarChangeListener() {
        @Override
        public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
            if(!mIsRatingModified) {
                showFAB();
                mIsRatingModified = true;
            }
            mInterior.rating = Long.valueOf(rightPinValue);
            writeExteriorCheck();
        }
    };

    View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.car_image_warning_lights:
                    mFileName = String.format(getResources().getString(R.string.filename_frontpic), mCar.plate);
                    break;
                case R.id.car_image_seats:
                    mFileName = String.format(getResources().getString(R.string.filename_seats), mCar.plate);
                    break;
                case R.id.car_image_ac:
                    mFileName = String.format(getResources().getString(R.string.filename_ac), mCar.plate);
                    break;
                case R.id.car_image_radio:
                    mFileName = String.format(getResources().getString(R.string.filename_radio), mCar.plate);
                    break;
                case R.id.car_image_mat:
                    mFileName = String.format(getResources().getString(R.string.filename_mat), mCar.plate);
                    break;
                case R.id.car_image_interior:
                    mFileName = String.format(getResources().getString(R.string.filename_mainpic2), mCar.plate);
                    break;
                default:
                    break;
            }
            launchCamera();
        }
    };

    public void updateCircleImageViews(String picId, String downloadPath) {

        switch (picId) {

            case "warningLights":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mWarningLightsImage);
                break;
            case "seats":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mSeatsImage);
                break;
            case "ac":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mACImage);
                break;
            case "radio":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mRadioImage);
                break;
            case "mat":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mMatImage);
                break;
            case "car":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mCarImage);
                break;
        }
    }

    public void downloadImages() {

        String historyPath = String.format("pictures/cars/%1$s/%2$s/history/%3$s/", mUser.adminUid, mCar.plate, mAssignment.start.toString());
        String mainPath = String.format("pictures/cars/%1$s/%2$s/pictures/", mUser.adminUid, mCar.plate);
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_warning_lights) + ".jpg", mCar.plate), "warningLights");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_seats), mCar.plate) + ".jpg", "seats");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_ac), mCar.plate) + ".jpg", "ac");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_radio), mCar.plate) + ".jpg", "radio");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_mat), mCar.plate) + ".jpg", "mat");
        beginDownload(String.format(mainPath + getResources().getString(R.string.filename_mainpic2), mCar.plate) + ".jpg", "car");
    }

    public void writeExteriorCheck() {
        mDatabaseCheck = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("check").child("interior");
        mDatabaseCheck.setValue(mInterior);
    }

    public void readInteriorCheck() {

        mDatabaseCheck = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("check").child("interior");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Exterior object and use the values to update the UI

                mInterior = dataSnapshot.getValue(Interior.class);

                if (mInterior == null) {

                    mWarningLights = new WarningLights(false,
                            String.format(getResources().getString(R.string.filename_warning_lights), mCar.plate),
                            String.format(getResources().getString(R.string.filename_warning_lights), mCar.plate),
                            String.format(getResources().getString(R.string.filename_warning_lights), mCar.plate));

                    mSeats = new Seats(false,
                            String.format(getResources().getString(R.string.filename_seats), mCar.plate),
                            String.format(getResources().getString(R.string.filename_seats), mCar.plate),
                            String.format(getResources().getString(R.string.filename_seats), mCar.plate));

                    mRadio = new Radio(false,
                            String.format(getResources().getString(R.string.filename_radio), mCar.plate),
                            String.format(getResources().getString(R.string.filename_radio), mCar.plate),
                            String.format(getResources().getString(R.string.filename_radio), mCar.plate));

                    mAC = new AC(false,
                            String.format(getResources().getString(R.string.filename_ac), mCar.plate),
                            String.format(getResources().getString(R.string.filename_ac), mCar.plate),
                            String.format(getResources().getString(R.string.filename_ac), mCar.plate));

                    mMat = new Mat(false,
                            String.format(getResources().getString(R.string.filename_mat), mCar.plate),
                            String.format(getResources().getString(R.string.filename_mat), mCar.plate),
                            String.format(getResources().getString(R.string.filename_mat), mCar.plate));


                    mInterior = new Interior(0L, mWarningLights, mSeats, mAC, mRadio, mMat);
                } else {
                    mWarningLightsSwitch.setChecked(mInterior.warningLights.accepted);
                    mSeatsSwitch.setChecked(mInterior.seats.accepted);
                    mACSwitch.setChecked(mInterior.ac.accepted);
                    mRadioSwitch.setChecked(mInterior.radio.accepted);
                    mMatSwitch.setChecked(mInterior.mat.accepted);
                    mRatingBar.setSeekPinByValue(mInterior.rating);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Exterior failed, log a message
                // Getting Exterior failed, log a message
                Log.w("__load__", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabaseCheck.addListenerForSingleValueEvent(reFuelListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mBroadcastReceiver, DownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());
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
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (mFileUri != null) {
                    uploadFromUri(mFileUri);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(getContext(), "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Toast message in case the user does not see the notificatio
        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        //FireBase reference
        String reference = String.format("pictures/cars/%1$s/%2$s/history/%3$s", mUser.adminUid, mCar.plate, mAssignment.start.toString());

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        getActivity().startService(new Intent(getContext(), UploadService.class)
                .putExtra(UploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(UploadService.EXTRA_UPLOAD_FILENAME, mFileName)
                .putExtra(UploadService.EXTRA_STORAGE_REFERENCE, reference)
                .setAction(UploadService.ACTION_UPLOAD));
    }

    private void beginDownload(String fileName, String picId) {

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(getContext(), DownloadService.class)
                .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, fileName)
                .putExtra(DownloadService.EXTRA_PICTURE_ID, picId)
                .setAction(DownloadService.ACTION_DOWNLOAD);
        getActivity().startService(intent);

        // Show loading spinner
        showProgressDialog();
    }


    @AfterPermissionGranted(RC_STORAGE_PERMS)
    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Check that we have permission to read images from external storage.
        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
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
            Log.d(TAG, "file.createNewFile:" + file.getAbsolutePath() + ":" + created);
        } catch (IOException e) {
            Log.e(TAG, "file.createNewFile" + file.getAbsolutePath() + ":FAILED", e);
        }

        // Create content:// URI for file, required since Android N
        // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        mFileUri = FileProvider.getUriForFile(getContext(),
                "com.google.firebase.quickstart.firebasestorage.fileprovider", file);

        // Create and launch the intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

        // Grant permission to camera (this is required on KitKat and below)
        List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            getActivity().grantUriPermission(packageName, mFileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        // Start picture-taking intent
        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
    }


    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
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
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void showFAB() {
        mDoneButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        mDoneButton.setClickable(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
        void onFragmentInteraction(Uri uri);
    }
}
