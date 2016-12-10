package avidos.autok.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import avidos.autok.R;
import avidos.autok.entity.AC;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.Interior;
import avidos.autok.entity.Mat;
import avidos.autok.entity.Radio;
import avidos.autok.entity.Seats;
import avidos.autok.entity.User;
import avidos.autok.entity.WarningLights;
import avidos.autok.helper.OnPageCommunication;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SecondPageDocumentationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SecondPageDocumentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecondPageDocumentationFragment extends Fragment implements View.OnClickListener, CalendarDatePickerDialogFragment.OnDateSetListener, RadialTimePickerDialogFragment.OnTimeSetListener {

    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker_name";

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";
    private static final String WHITESPACE = " ";

    private Cars mCar;
    private User mUser;
    private Assignment mAssignment;
    private OnFragmentInteractionListener mListener;
    private Boolean fieldFilled = false;
    private String dateTime;
    // UI
    private EditText mDestinationView;
    private EditText mTimeOfUseView;
    private EditText mAssuranceView;
    private EditText mPoliceNumberView;
    private EditText mExpirationView;
    private EditText mTelephoneView;
    private Button mFinishButton;
    // FireBase
    private DatabaseReference mDatabase;


    public SecondPageDocumentationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstPageDocumentationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SecondPageDocumentationFragment newInstance(User user, Cars car) {
        SecondPageDocumentationFragment fragment = new SecondPageDocumentationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CAR, car);
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCar = (Cars) getArguments().getSerializable(ARG_CAR);
            mUser = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second_page_documentation, container, false);

        mDestinationView = (EditText) view.findViewById(R.id.edittext_destination);
        mTimeOfUseView = (EditText) view.findViewById(R.id.edittext_timeofuse);
        mAssuranceView = (EditText) view.findViewById(R.id.edittext_assurance);
        mPoliceNumberView = (EditText) view.findViewById(R.id.edittext_policenumber);
        mExpirationView = (EditText) view.findViewById(R.id.edittext_expiration);
        mTelephoneView = (EditText) view.findViewById(R.id.edittext_telephone);
        mFinishButton = (Button) view.findViewById(R.id.finish_button);

        readAssignment();

        mAssuranceView.setText(mCar.insuranceCompany);
        mPoliceNumberView.setText(mCar.engineNumber);
        mExpirationView.setText(getDate(mCar.insurancePeriodEnd));
        mTelephoneView.setText(mCar.insuranceTelephone);

        mTimeOfUseView.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.edittext_timeofuse:
                mTimeOfUseView.setFocusable(false);
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(this);
                cdp.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
                break;
            case R.id.finish_button:
                if (mListener != null) {
                    if(mListener.checkFragmentInteraction()) {
                        attemptWrite();
                    } else {
                        Toast.makeText(getContext(), "No ha completado: " + mListener.getFragments(), Toast.LENGTH_LONG).show();
                    }
                }
            default:
                break;
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener(this);
        rtpd.show(getFragmentManager(), FRAG_TAG_TIME_PICKER);
        dateTime = getString(R.string.date_picker_result_value, Integer.toString(dayOfMonth), Integer.toString(monthOfYear),
                Integer.toString(year));
    }

    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
        dateTime = dateTime.concat(WHITESPACE);
        dateTime = dateTime.concat(getString(R.string.radial_time_picker_result_value, hourOfDay, minute));
        mTimeOfUseView.setText(dateTime);
    }

    private void attemptWrite() {

        // Reset errors.
        mDestinationView.setError(null);
        mTimeOfUseView.setError(null);

        // Store values at the time of the login attempt.
        String destination = mDestinationView.getText().toString();
        String timeofuse = mTimeOfUseView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(timeofuse)) {
            mTimeOfUseView.setFocusable(true);
            mTimeOfUseView.setError(getString(R.string.error_field_required));
            focusView = mTimeOfUseView;
            cancel = true;
        }

        if (TextUtils.isEmpty(destination)) {
            mDestinationView.setError(getString(R.string.error_field_required));
            focusView = mDestinationView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            writeAssignment();
            getFragmentManager().popBackStack();
        }
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

    public Long getTimeStamp(String dateTime) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = null;
        try {
            date = formatter.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date != null ? date.getTime() : 0;
    }

    private void writeAssignment() {

        mAssignment.end = getTimeStamp(dateTime);
        mAssignment.destination = mDestinationView.getText().toString();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate);
        Map<String, Object> postValues = mAssignment.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/assignment/", postValues);

        mDatabase.updateChildren(childUpdates);
    }

    public void readAssignment() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Exterior object and use the values to update the UI

                mAssignment = dataSnapshot.getValue(Assignment.class);

                if (mAssignment.destination.length() > 0) {
                    mDestinationView.setText(mAssignment.destination);
                } else if (mAssignment.end > 0) {
                    mTimeOfUseView.setText(getDate(mAssignment.end));
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
        mDatabase.addListenerForSingleValueEvent(reFuelListener);
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
        boolean checkFragmentInteraction();
        String getFragments();
    }
}
