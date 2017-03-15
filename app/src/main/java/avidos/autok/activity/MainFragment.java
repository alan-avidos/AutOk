package avidos.autok.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import avidos.autok.R;
import avidos.autok.adapter.AssignmentsAdapter;
import avidos.autok.entity.Cars;
import avidos.autok.entity.Check;
import avidos.autok.entity.Exterior;
import avidos.autok.entity.Interior;
import avidos.autok.entity.User;
import avidos.autok.entity.Assignment;
import avidos.autok.helper.ItemClickSupport;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnTouchListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "param1";
    private static final String ARG_PARAM2 = "param2";

    // UI
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mButtonAvailable;
    private Button mButtonNext;
    private Spinner mSpinnerType;
    private Spinner mSpinnerService;
    private ProgressDialog mProgressDialog;

    // FireBase
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    //
    private User mUserData;
    private Cars mCar;
    private List<Cars> carsList;
    private List<Assignment> assignmentList;
    private String mSelectionService;
    private String mSelectionType;
    private boolean mSpinnerSelected = false;
    private String adminUid;
    private Assignment mAssignment;
    private Boolean mIsAvailableSelected = true;

    // Fragments
    private AssignationFragment assignationFragment;
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AssignmentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
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
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        mButtonAvailable = (Button) view.findViewById(R.id.button_available);
        mButtonNext = (Button) view.findViewById(R.id.button_next);
        mSpinnerService = (Spinner) view.findViewById(R.id.spinner_service);
        mSpinnerType = (Spinner) view.findViewById(R.id.spinner_type);


        mButtonNext.setOnClickListener(this);
        mButtonAvailable.setOnClickListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> serviceAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.service_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerService.setAdapter(serviceAdapter);
        mSpinnerType.setAdapter(typeAdapter);

        mSpinnerType.setEnabled(false);

        readUserAdmin();

        mSpinnerType.setSelection(0, false);
        mSpinnerService.setSelection(0, false);
        mSpinnerService.setOnItemSelectedListener(this);
        mSpinnerType.setOnItemSelectedListener(this);

        mSpinnerType.setOnTouchListener(this);
        mSpinnerService.setOnTouchListener(this);

        carsList = new ArrayList<>();
        assignmentList = new ArrayList<>();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_assignments);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new AssignmentsAdapter(carsList);
        mRecyclerView.setAdapter(mAdapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if(mIsAvailableSelected) {
                    writeNewAssignation(carsList.get(position));
                    loadAssignation(carsList.get(position));
                } else {
                    if(assignmentList.get(position).end == 0)
                        Snackbar.make(view, "Este vehículo no está disponible. Aún no hay fecha de disponibilidad.", Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(view,  "Este vehiculo estará disponible hasta el " + getDate(assignmentList.get(position).end), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.onFragmentInteraction("MainFragment");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next:
                //carsList.clear();
                mAdapter.notifyDataSetChanged();
                buttonTabs(false);
                break;
            case R.id.button_available:
                //carsList.clear();
                mAdapter.notifyDataSetChanged();
                buttonTabs(true);
                break;
            default:
                break;
        }
    }

    public void buttonTabs(boolean isAvailableSelected) {

        if(isAvailableSelected) {
            mIsAvailableSelected = true;
            mButtonAvailable.setBackgroundResource(R.drawable.tab_selected);
            mButtonNext.setBackgroundResource(R.drawable.tab_unselected);
            mButtonAvailable.setTextColor(getResources().getColor(R.color.colorAppOrange));
            mButtonNext.setTextColor(getResources().getColor(R.color.colorAppWhiteText));
        } else {
            mIsAvailableSelected = false;
            mButtonNext.setBackgroundResource(R.drawable.tab_selected);
            mButtonAvailable.setBackgroundResource(R.drawable.tab_unselected);
            mButtonNext.setTextColor(getResources().getColor(R.color.colorAppOrange));
            mButtonAvailable.setTextColor(getResources().getColor(R.color.colorAppWhiteText));
        }

        if(mSelectionService != null && mSelectionType != null) {
            if(!mSelectionService.equals(getResources().getStringArray(R.array.service_array_en)[0]) ||
                    !mSelectionType.equals(getResources().getStringArray(R.array.type_array_en)[0])) {
                filterCars();
            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(mSpinnerSelected) {
            switch (parent.getId()) {
                case R.id.spinner_service:
                    mSpinnerType.setEnabled(true);
                    //carsList.clear();
                    mSelectionService = getResources().getStringArray(R.array.service_array_en)[position];
                    break;
                case R.id.spinner_type:
                    //carsList.clear();
                    mSelectionType = getResources().getStringArray(R.array.type_array_en)[position];
                    if(mUserData == null) {
                        readUserData();
                    } else {
                        if(mSelectionService.equals(getResources().getStringArray(R.array.service_array_en)[0]) ||
                                mSelectionType.equals(getResources().getStringArray(R.array.type_array_en)[0]))
                                    return;
                        filterCars();
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
            mSpinnerSelected = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void readUserAdmin() {

        showProgressDialog();

        try {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("userToAdmin").child(mUser.getUid()).child("adminUid");
        } catch (NullPointerException npe) {
            return;
        }

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                adminUid = dataSnapshot.getValue(String.class);
                readUserData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    private void readUserData() {

        try {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(adminUid).child(mUser.getUid());
        } catch (NullPointerException npe) {
            return;
        }

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                mUserData = dataSnapshot.getValue(User.class);
                if(mUserData.assignment != null) {
                    if(mUserData.assignment.length() > 0) {
                        getCar();
                    } else {
                        hideProgressDialog();
                    }
                } else {
                    hideProgressDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    private void filterCars() {

        try {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUserData.adminUid);
        } catch (NullPointerException npe) {
            return;
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carsList.clear();
                assignmentList.clear();
                if(mIsAvailableSelected) {
                    for (DataSnapshot carSnapshot: dataSnapshot.getChildren()) {
                        if(carSnapshot.child("assignment").exists()) {
                            if(carSnapshot.child("assignment").child("userName").getValue(String.class).equals("")){
                                Cars car = carSnapshot.child("generalInfo").getValue(Cars.class);
                                carsList.add(car);
                            }
                        } else if(!carSnapshot.child("assignment").exists()) {
                            if(carSnapshot.child("generalInfo").child("allowedUse").child(mSelectionService).child(mSelectionType).getValue(boolean.class)) {
                                Cars car = carSnapshot.child("generalInfo").getValue(Cars.class);
                                carsList.add(car);
                            }
                        }
                    }
                } else {
                    for (DataSnapshot carSnapshot: dataSnapshot.getChildren()) {
                        if(carSnapshot.child("assignment").exists() && carSnapshot.child("assignment").child("userName").getValue(String.class).length() > 0) {
                            if(carSnapshot.child("generalInfo").child("allowedUse").child(mSelectionService).child(mSelectionType).getValue(boolean.class)) {
                                Cars car = carSnapshot.child("generalInfo").getValue(Cars.class);
                                Assignment assignment = carSnapshot.child("assignment").getValue(Assignment.class);
                                carsList.add(car);
                                assignmentList.add(assignment);
                            }
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void getCar() {

        try {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUserData.adminUid).child(mUserData.assignment).child("generalInfo");
        } catch (NullPointerException npe) {
            return;
        }

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                mCar = dataSnapshot.getValue(Cars.class);
                getAssignment();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    private void getAssignment() {

        try {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUserData.adminUid).child(mUserData.assignment).child("assignment");
        } catch (NullPointerException npe) {
            return;
        }

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                mAssignment = dataSnapshot.getValue(Assignment.class);
                hideProgressDialog();
                loadAssignation(mCar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    private void writeNewAssignation(Cars car) {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUserData.adminUid).child(car.plate).child("assignment");
        mAssignment = new Assignment((long)0, System.currentTimeMillis(), mUser.getUid(), mSelectionService, mSelectionType, mUserData.name, "",0.0, (long)0, new Check(new Interior(), new Exterior()));
        mDatabase.setValue(mAssignment);
        updateUser(car);
    }

    private void updateUser(Cars car) {

        mUserData.assignment = car.plate;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mUserData.adminUid);
        Map<String, Object> postValues = mUserData.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + mUser.getUid() + "/", postValues);

        mDatabase.updateChildren(childUpdates);
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
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP) mSpinnerSelected = true;
        return false;
    }

    private String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private void loadAssignation (Cars car) {
        mSpinnerType.setSelection(0);
        mSpinnerService.setSelection(0);
        mListener.onFragmentInteraction("AssignationFragment");
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        assignationFragment = AssignationFragment.newInstance(car, mUserData, mAssignment, mUser.getUid());
        Log.e("___errir___", mUserData.adminUid + "/" + car.plate + "/" + mAssignment.start.toString());
        carsList.clear();
        assignmentList.clear();
        mAdapter.notifyDataSetChanged();
        fragmentTransaction.replace(R.id.content_main, assignationFragment, "AssignationFragment");
        fragmentTransaction.addToBackStack("AssignationFragment");
        fragmentTransaction.commit();
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
