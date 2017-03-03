package avidos.autok.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.Crash;
import avidos.autok.entity.Exterior;
import avidos.autok.entity.Scratch;
import avidos.autok.entity.User;
import avidos.autok.helper.DeleteService;
import avidos.autok.helper.DownloadService;
import avidos.autok.helper.UploadService;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExteriorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExteriorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExteriorFragment extends Fragment {

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";
    private static final String ARG_ASSIGNED = "assigned";

    private static final String TAG = "Storage#MainActivity";

    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    // Entities
    private User mUser;
    private Cars mCar;
    private Assignment mAssignment;
    private Exterior mExterior;
    private Exterior mExteriorCopy;
    private Crash mCrash;
    private Scratch mScratch;
    private Boolean mIsRatingModified = false;
    private Boolean mIsAssigned;
    private Boolean mAllImagesUploaded = false;
    private File dir;
    private File file;
    // UI
    private CircleImageView mMainPic;
    private CircleImageView mFrontPic;
    private CircleImageView mRearPic;
    private CircleImageView mLeftPic;
    private CircleImageView mRightPic;
    private CircleImageView mScratchPic;
    private CircleImageView mCrashPic;
    private RangeBar mRating;
    private SwitchButton mCrashSwitch;
    private SwitchButton mScratchSwitch;
    private TextView mCarInfoView;
    private FloatingActionButton mDoneButton;
    private FloatingActionButton mCancelButton;

    // FireBase
    private DatabaseReference mDatabaseCheck;

    // Storage
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String mFileName = null;

    // Listeners
    private OnFragmentInteractionListener mListener;

    public ExteriorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExteriorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExteriorFragment newInstance(User user, Cars car, Assignment assignment, boolean isAssigned) {
        ExteriorFragment fragment = new ExteriorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CAR, car);
        args.putSerializable(ARG_USER, user);
        args.putSerializable(ARG_ASSIGNMENT, assignment);
        args.putSerializable(ARG_ASSIGNED, isAssigned);
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
            mIsAssigned = getArguments().getBoolean(ARG_ASSIGNED);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exterior, container, false);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        mMainPic = (CircleImageView) view.findViewById(R.id.car_image_exterior);
        mFrontPic = (CircleImageView) view.findViewById(R.id.front_pic);
        mRearPic = (CircleImageView) view.findViewById(R.id.rear_pic);
        mLeftPic = (CircleImageView) view.findViewById(R.id.left_pic);
        mRightPic = (CircleImageView) view.findViewById(R.id.right_pic);
        mCrashPic = (CircleImageView) view.findViewById(R.id.car_image_crash);
        mScratchPic = (CircleImageView) view.findViewById(R.id.car_image_scratch);
        mRating = (RangeBar) view.findViewById(R.id.ratingBar);
        mCrashSwitch = (SwitchButton) view.findViewById(R.id.switch_crash);
        mScratchSwitch = (SwitchButton) view.findViewById(R.id.switch_scratch);
        mCarInfoView = (TextView) view.findViewById(R.id.car_info_exterior);
        mDoneButton = (FloatingActionButton) view.findViewById(R.id.fab_exterior_done);
        mCancelButton = (FloatingActionButton) view.findViewById(R.id.fab_exterior_cancel);

        mFrontPic.setTag(false);
        mRearPic.setTag(false);
        mLeftPic.setTag(false);
        mRightPic.setTag(false);

        readExteriorCheck();

        // if the assignation form has already finished, avoid modifications.
        if (mIsAssigned) {
            mRating.setEnabled(false);
            //mFrontPic.setEnabled(false);
            //mRearPic.setEnabled(false);
            //mLeftPic.setEnabled(false);
            //mRightPic.setEnabled(false);
            //mScratchPic.setEnabled(false);
            //mCrashPic.setEnabled(false);
            mScratchSwitch.setClickable(false);
            mCrashSwitch.setClickable(false);
        }

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExterior = mExteriorCopy;
                writeExteriorCheck();
                getFragmentManager().popBackStack();
            }
        });

        mRating.setSeekPinByValue(0);

        mCarInfoView.setText(getString(R.string.title_car_info, mCar.brand, mCar.model, mCar.plate));
        mCarInfoView.setSelected(true);

        mRating.setOnRangeBarChangeListener(onRangeBarChangeListener);
        mCrashSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mScratchSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        mFrontPic.setOnClickListener(onImageClickListener);
        mRearPic.setOnClickListener(onImageClickListener);
        mLeftPic.setOnClickListener(onImageClickListener);
        mRightPic.setOnClickListener(onImageClickListener);
        mScratchPic.setOnClickListener(onImageClickListener);
        mCrashPic.setOnClickListener(onImageClickListener);

        downloadImages();

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {
                    case DownloadService.DOWNLOAD_COMPLETED:
                        String picId = intent.getStringExtra(DownloadService.EXTRA_PICTURE_ID);
                        String downloadPath = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);
                        updateCircleImageViews(picId, downloadPath);
                        if(mIsRatingModified && getTagImages()) {
                            showFAB();
                            mListener.onFragmentInteraction("ExteriorFragment");
                        }
                        writeExteriorImage(picId);
                        break;
                    case DownloadService.DOWNLOAD_ERROR:
                        // Not Alert failure
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

                    Bitmap bMap= BitmapFactory.decodeFile(file.getAbsolutePath());
                    Bitmap out = Bitmap.createScaledBitmap(bMap, 500, 500, false);
                    File resizedFile = new File(dir, "resize.png");


                    OutputStream fOut = null;
                    try {
                        fOut = new BufferedOutputStream(new FileOutputStream(resizedFile));
                        out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        bMap.recycle();
                        out.recycle();

                    } catch (Exception e) {

                    }
                    uploadFromUri(Uri.fromFile(resizedFile));
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

    /**
     * Instantiate a service to get image link.
     * @param fileName The name of the file.
     * @param picId The image id.
     */
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
        dir = new File(Environment.getExternalStorageDirectory() + "/photos");
        file = new File(dir, UUID.randomUUID().toString() + ".jpg");
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
        // Got a new intent from UploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(UploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(UploadService.EXTRA_FILE_URI);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // If an image has already taken, show a dialog with it.
            // If not take a photo.
            Boolean tag;
            if (v.getTag() != null) {
                tag = v.getTag().equals(true);
            } else {
                tag = false;
            }
            switch (v.getId()) {

                case R.id.front_pic:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_frontpic), mCar.plate));
                        beginDownload(path, "frontPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_frontpic), mCar.plate);
                    }
                    break;
                case R.id.rear_pic:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_rearpic), mCar.plate));
                        beginDownload(path, "rearPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_rearpic), mCar.plate);
                    }
                    break;
                case R.id.left_pic:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_leftpic), mCar.plate));
                        beginDownload(path, "leftPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_leftpic), mCar.plate);
                    }
                    break;
                case R.id.right_pic:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_rightpic), mCar.plate));
                        beginDownload(path, "rightPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_rightpic), mCar.plate);
                    }
                    break;
                case R.id.car_image_crash:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_crashpic), mCar.plate));
                        beginDownload(path, "crashPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_crashpic), mCar.plate);
                    }
                    break;
                case R.id.car_image_scratch:
                    if(tag) {
                        String path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                                String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate));
                        beginDownload(path, "scratchPicDialog");
                        return;
                    } else {
                        mFileName = String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate);
                    }
                    break;
                case R.id.car_image_exterior:
                        mFileName = getResources().getString(R.string.filename_mainpic1);
                    break;
                default:
                    break;
            }
            launchCamera();
        }
    };

    /**
     * Update switches.
     */
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            switch (buttonView.getId()){
                case R.id.switch_crash:
                    mExterior.crash.accepted = isChecked;
                    mCrashPic.setVisibility(visibility);
                    writeExteriorCheck();
                    break;
                case R.id.switch_scratch:
                    mExterior.scratch.accepted = isChecked;
                    mScratchPic.setVisibility(visibility);
                    writeExteriorCheck();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Update RangeBar.
     */
    RangeBar.OnRangeBarChangeListener onRangeBarChangeListener = new RangeBar.OnRangeBarChangeListener() {
        @Override
        public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
            if(getTagImages()) {
                showFAB();
                mListener.onFragmentInteraction("ExteriorFragment");
            }
            mIsRatingModified = true;
            mExterior.rating = Long.valueOf(rightPinValue);
            writeExteriorCheck();
        }
    };

    /**
     * Load images gotten from FireBase.
     * @param picId An id to know what image is.
     * @param downloadPath Link of the image alocated in FireBase.
     */
    public void updateCircleImageViews(String picId, String downloadPath) {
        String path;
        switch (picId) {

            case "frontPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mFrontPic);
                mFrontPic.setTag(true);
                break;
            case "rearPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mRearPic);
                mRearPic.setTag(true);
                break;
            case "leftPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mLeftPic);
                mLeftPic.setTag(true);
                break;
            case "rightPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mRightPic);
                mRightPic.setTag(true);
                break;
            case "crashPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mCrashPic);
                mCrashPic.setTag(true);
                break;
            case "scratchPic":
                Picasso.with(getContext()).load(downloadPath).resize(25, 25).noFade().into(mScratchPic);
                mScratchPic.setTag(true);
                break;
            case "car":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mMainPic);
                mMainPic.setTag(true);
                break;
            case "frontPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_frontpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
            case "rearPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_rearpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
            case "leftPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_leftpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
            case "rightPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_rightpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
            case "crashPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_crashpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
            case "scratchPicDialog":
                path = String.format("pictures/cars/%1$s/%2$s/history/%3$s/%4$s.jpg", mUser.adminUid, mCar.plate, mAssignment.start,
                        String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate));
                showImage(downloadPath, picId, path);
                break;
        }
    }

    /**
     * To download all images at the same time.
     */
    public void downloadImages() {

        String historyPath = String.format("pictures/cars/%1$s/%2$s/history/%3$s/", mUser.adminUid, mCar.plate, mAssignment.start.toString());
        String mainPath = String.format("pictures/cars/%1$s/%2$s/pictures/", mUser.adminUid, mCar.plate);
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_frontpic) + ".jpg", mCar.plate), "frontPic");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_rearpic) + ".jpg", mCar.plate), "rearPic");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_leftpic) + ".jpg", mCar.plate), "leftPic");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_rightpic) + ".jpg", mCar.plate), "rightPic");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_crashpic) + ".jpg", mCar.plate), "crashPic");
        beginDownload(String.format(historyPath + getResources().getString(R.string.filename_scratchpic) + ".jpg", mCar.plate), "scratchPic");
        beginDownload(mainPath + getResources().getString(R.string.filename_mainpic1) + ".jpg", "car");
    }

    /**
     * To know if an image is already uploaded.
     * @param picId The image id.
     */
    public void setTagImages(String picId) {

        switch (picId) {
            case "frontPicDialog":
                mFrontPic.setTag(false);
                break;
            case "rearPicDialog":
                mRearPic.setTag(false);
                break;
            case "leftPicDialog":
                mLeftPic.setTag(false);
                break;
            case "rightPicDialog":
                mRightPic.setTag(false);
                break;
            case "crashPicDialog":
                mCrashPic.setTag(false);
                break;
            case "scratchPicDialog":
                mScratchPic.setTag(false);
                break;
            case "carDialog":
                mMainPic.setTag(false);
                break;
        }
    }

    /**
     * To know if all images were uploaded
     */
    public boolean getTagImages() {

        return mFrontPic.getTag().equals(true) && mRearPic.getTag().equals(true) &&
                mLeftPic.getTag().equals(true) && mRightPic.getTag().equals(true);
    }

    /**
     * Write assignment exterior check on FireBase.
     */
    public void writeExteriorCheck() {
        mDatabaseCheck = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("check").child("exterior");
        mDatabaseCheck.setValue(mExterior);
    }

    /**
     * Write assignment exterior check on FireBase.
     */
    public void writeExteriorImage(String picId) {

        switch (picId) {

            case "frontPic":
                mExterior.pic1 = String.format(getResources().getString(R.string.filename_frontpic), mCar.plate);
                break;
            case "rearPic":
                mExterior.pic2 = String.format(getResources().getString(R.string.filename_rearpic), mCar.plate);
                break;
            case "leftPic":
                mExterior.pic3 = String.format(getResources().getString(R.string.filename_leftpic), mCar.plate);
                break;
            case "rightPic":
                mExterior.pic4 = String.format(getResources().getString(R.string.filename_rightpic), mCar.plate);
                break;
        }

        mDatabaseCheck = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("check").child("exterior");
        mDatabaseCheck.setValue(mExterior);
    }

    /**
     * Read assignment exterior check data (if exists).
     */
    public void readExteriorCheck() {

        mDatabaseCheck = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("check").child("exterior");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Exterior object and use the values to update the UI

                mExterior = dataSnapshot.getValue(Exterior.class);
                mExteriorCopy = dataSnapshot.getValue(Exterior.class);

                if (mExterior == null) {

                    mCrash = new Crash(false,
                            String.format(getResources().getString(R.string.filename_crashpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_crashpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_crashpic), mCar.plate));

                    mScratch = new Scratch(false,
                            String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_scratchpic), mCar.plate));

                    mExterior = new Exterior(null,
                            null,
                            null,
                            null,
                            0L,
                            mCrash,
                            mScratch);

/*                    mExterior = new Exterior(String.format(getResources().getString(R.string.filename_frontpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_rearpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_leftpic), mCar.plate),
                            String.format(getResources().getString(R.string.filename_rightpic), mCar.plate),
                            0L,
                            mCrash,
                            mScratch);*/
                } else {
                    mScratchSwitch.setChecked(mExterior.scratch.accepted);
                    mCrashSwitch.setChecked(mExterior.crash.accepted);
                    mRating.setSeekPinByValue(mExterior.rating);
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

    /**
     * Instantiate a service to delete an image from FireBase.
     * @param fileName
     */
    public void deleteImage(String fileName) {
        // Kick off DeleteService to download the file
        Intent intent = new Intent(getContext(), DeleteService.class)
                .putExtra(DeleteService.EXTRA_DELETE_PATH, fileName)
                .setAction(DeleteService.ACTION_DELETE);
        getActivity().startService(intent);

        // Show loading spinner
        showProgressDialog();
    }

    /**
     * To display a dialog with an image.
     * @param downloadPath The link to the image in FireBase.
     */
    public void showImage(final String downloadPath, final String picId, final String path) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (!mIsAssigned) {
            builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteImage(path);
                    setTagImages(picId);
                }
            });
        }
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_image, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ImageView image = (ImageView) dialogLayout.findViewById(R.id.goProDialogImage);
        Picasso.with(getContext()).load(downloadPath).into(image);

        dialog.show();
    }

    /**
     * Show a Floating Action Button when completed obligatory fields.
     */
    public void showFAB() {
        mDoneButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        mDoneButton.setClickable(true);
        mCancelButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        mCancelButton.setClickable(true);
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
        void onFragmentInteraction(String fragment);
    }
}
