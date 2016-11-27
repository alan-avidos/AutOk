package avidos.autok.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import avidos.autok.R;
import avidos.autok.adapter.AssignmentsAdapter;
import avidos.autok.adapter.OptionsAdapter;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.CarsUnassigned;
import avidos.autok.entity.Crash;
import avidos.autok.entity.Exterior;
import avidos.autok.entity.Scratch;
import avidos.autok.entity.User;
import avidos.autok.helper.DownloadService;
import avidos.autok.helper.ItemClickSupport;
import avidos.autok.helper.UploadService;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AssignationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AssignationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";
    //
    private Cars mCar;
    private User mUser;
    private Assignment mAssignment;

    // UI
    private TextView mTextViewCarInfo;
    private Button mButtonReasign;
    private RecyclerView mRecyclerViewOptions;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private CircleImageView mCarPic1;
    private CircleImageView mCarPic2;
    private CircleImageView mCarPic3;

    // Fragments
    private FuelFragment mFuelFragment;
    private ExteriorFragment mExteriorFragment;
    private InteriorFragment mInteriorFragment;
    private DocumentationFragment mDocumentationFragment;
    private OnFragmentInteractionListener mListener;

    // FireBase
    private DatabaseReference mOriginDatabase;
    private DatabaseReference mDestinyDatabase;

    // Storage
    private BroadcastReceiver mBroadcastReceiver;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private ProgressDialog mProgressDialog;


    public AssignationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param cars Parameter 1.
     * @return A new instance of fragment AssignationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AssignationFragment newInstance(Cars cars, User user, Assignment assignment) {
        AssignationFragment fragment = new AssignationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CAR, cars);
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
        View view = inflater.inflate(R.layout.fragment_assignation, container, false);

        mTextViewCarInfo = (TextView) view.findViewById(R.id.car_info);
        mRecyclerViewOptions = (RecyclerView) view.findViewById(R.id.recycler_view_options);
        mButtonReasign = (Button) view.findViewById(R.id.reasign_button);
        mCarPic1 = (CircleImageView) view.findViewById(R.id.car_image_1);
        mCarPic2 = (CircleImageView) view.findViewById(R.id.car_image_2);
        mCarPic3 = (CircleImageView) view.findViewById(R.id.car_image_3);

        mTextViewCarInfo.setText(getString(R.string.title_car_info, mCar.brand, mCar.model, mCar.plate));

        mCarPic1.setOnClickListener(onImageClickListener);
        mCarPic2.setOnClickListener(onImageClickListener);
        mCarPic3.setOnClickListener(onImageClickListener);

        downloadImages();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerViewOptions.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewOptions.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new OptionsAdapter(getContext());
        mRecyclerViewOptions.setAdapter(mAdapter);

        ItemClickSupport.addTo(mRecyclerViewOptions).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                //TODO: improve this
                switch (position) {

                    case 0:
                        mExteriorFragment = ExteriorFragment.newInstance(mUser, mCar, mAssignment);
                        getFragmentManager().beginTransaction().replace(R.id.content_main, mExteriorFragment, "ExteriorFragment").addToBackStack("ExteriorFragment").commit();
                        break;
                    case 1:
                        mInteriorFragment = InteriorFragment.newInstance(mUser, mCar, mAssignment);
                        getFragmentManager().beginTransaction().replace(R.id.content_main, mInteriorFragment, "InteriorFragment").addToBackStack("InteriorFragment").commit();
                        break;
                    case 2:
                        mFuelFragment = FuelFragment.newInstance(mUser, mCar);
                        getFragmentManager().beginTransaction().replace(R.id.content_main, mFuelFragment, "FuelFragment").addToBackStack("FuelFragment").commit();
                        break;
                    case 3:
                        mDocumentationFragment = DocumentationFragment.newInstance(mUser, mCar, mAssignment);
                        getFragmentManager().beginTransaction().replace(R.id.content_main, mDocumentationFragment, "DocumentationFragment").addToBackStack("DocumentationFragment").commit();
                        break;
                }
            }
        });

        mButtonReasign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reAssignCar();
                getFragmentManager().popBackStack();
            }
        });

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("__TAG__", "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {
                    case DownloadService.DOWNLOAD_COMPLETED:
                        //long numBytes = intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0);
                        String picId = intent.getStringExtra(DownloadService.EXTRA_PICTURE_ID);
                        String downloadPath = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);
                        updateCircleImageViews(picId, downloadPath);
                        break;
                    case DownloadService.DOWNLOAD_ERROR:
                        // not Alert failure
/*                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));*/
                        break;
                }
            }
        };

        return view;
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

    View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.car_image_1:
                    beginDownload(String.format(getResources().getString(R.string.filename_mainpic1), mCar.plate), "pic1Dialog");
                    break;
                case R.id.car_image_2:
                    beginDownload(String.format(getResources().getString(R.string.filename_mainpic2), mCar.plate), "pic2Dialog");
                    break;
                case R.id.car_image_3:
                    beginDownload(String.format(getResources().getString(R.string.filename_mainpic3), mCar.plate), "pic3Dialog");
                    break;
            }
        }
    };

    public void updateCircleImageViews(String picId, String downloadPath) {

        switch (picId) {

            case "pic1":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mCarPic1);
                break;
            case "pic2":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mCarPic2);
                break;
            case "pic3":
                Picasso.with(getContext()).load(downloadPath).noFade().into(mCarPic3);
                break;
            case "pic1Dialog":
                showImage(downloadPath);
                break;
            case "pic2Dialog":
                showImage(downloadPath);
                break;
            case "pic3Dialog":
                showImage(downloadPath);
                break;
        }
    }

    public void downloadImages() {

        beginDownload(String.format(getResources().getString(R.string.filename_mainpic1), mCar.plate), "pic1");
        beginDownload(String.format(getResources().getString(R.string.filename_mainpic2), mCar.plate), "pic2");
        beginDownload(String.format(getResources().getString(R.string.filename_mainpic3), mCar.plate), "pic3");
    }

    private void beginDownload(String fileName, String picId) {
        // Get path
        String path = String.format("pictures/cars/%1$s/%2$s/pictures/%3$s.jpg", mUser.adminUid, mCar.plate, fileName);

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(getContext(), DownloadService.class)
                .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, path)
                .putExtra(DownloadService.EXTRA_PICTURE_ID, picId)
                .setAction(DownloadService.ACTION_DOWNLOAD);
        getActivity().startService(intent);

        // Show loading spinner
        showProgressDialog();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showImage(final String downloadPath) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_image, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ImageView image = (ImageView) dialogLayout.findViewById(R.id.goProDialogImage);
        Picasso.with(getContext()).load(downloadPath).into(image);

        dialog.show();

/*        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

            }
        });*/
    }

    public void reAssignCar() {

        mDestinyDatabase = FirebaseDatabase.getInstance().getReference().child("car_history").child(mUser.adminUid).child(mCar.plate).child(mAssignment.start.toString());
        mOriginDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Exterior object and use the values to update the UI

                mDestinyDatabase.setValue(dataSnapshot.getValue());
                mOriginDatabase.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Exterior failed, log a message
                // Getting Exterior failed, log a message
                Log.w("__load__", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mOriginDatabase.addListenerForSingleValueEvent(reFuelListener);
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
