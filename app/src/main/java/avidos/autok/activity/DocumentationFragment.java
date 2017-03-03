package avidos.autok.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.AssignmentHistory;
import avidos.autok.entity.Cars;
import avidos.autok.entity.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DocumentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocumentationFragment extends Fragment implements View.OnClickListener, CalendarDatePickerDialogFragment.OnDateSetListener, RadialTimePickerDialogFragment.OnTimeSetListener {

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker_name";
    private static final String WHITESPACE = " ";
    private static final String ARG_ASSIGNED = "assigned";

    private OnFragmentInteractionListener mListener;
    private Boolean mIsExteriorCompleted;
    private Boolean mIsInteriorCompleted;
    private Boolean mIsFuelCompleted;
    private Boolean mIsAssigned;

    private Cars mCar;
    private User mUser;
    private Assignment mAssignment;
    private String dateTime;
    // UI
    private EditText mBrandView;
    private EditText mSubBrandView;
    private EditText mTypeView;
    private EditText mModelView;
    private EditText mColorView;
    private EditText mPlateView;
    private EditText mUserView;
    private EditText mDateTimeView;
    private EditText mDestinationView;
    private EditText mTimeOfUseView;
    private EditText mAssuranceView;
    private EditText mPoliceNumberView;
    private EditText mExpirationView;
    private EditText mTelephoneView;
    private TextInputLayout mBrandLayout;
    private TextInputLayout mSubBrandLayout;
    private TextInputLayout mTypeLayout;
    private TextInputLayout mModelLayout;
    private TextInputLayout mColorLayout;
    private TextInputLayout mPlateLayout;
    private TextInputLayout mUserLayout;
    private TextInputLayout mDateTimeLayout;
    private TextInputLayout mAssuranceLayout;
    private TextInputLayout mPoliceNumberLayout;
    private TextInputLayout mExpirationLayout;
    private TextInputLayout mTelephoneLayout;
    private Button mFinishButton;
    private Button mCancelButton;
    private FrameLayout mVehicleSubtitleLayout;
    private FrameLayout mPossesionSubtitleLayout;
    private FrameLayout mAssuranceSubtitleLayout;

    // FireBase
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCarHistory;
    private DatabaseReference mDatabaseUserHistory;

    public DocumentationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DocumentationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DocumentationFragment newInstance(User user, Cars car, Assignment assignment, boolean isAssigned) {
        DocumentationFragment fragment = new DocumentationFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_documentation, container, false);

        mDestinationView = (EditText) view.findViewById(R.id.edittext_destination);
        mTimeOfUseView = (EditText) view.findViewById(R.id.edittext_timeofuse);
        mAssuranceView = (EditText) view.findViewById(R.id.edittext_assurance);
        mPoliceNumberView = (EditText) view.findViewById(R.id.edittext_policenumber);
        mExpirationView = (EditText) view.findViewById(R.id.edittext_expiration);
        mTelephoneView = (EditText) view.findViewById(R.id.edittext_telephone);
        mFinishButton = (Button) view.findViewById(R.id.finish_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_assignation_button);

        mBrandView = (EditText) view.findViewById(R.id.edittext_brand);
        mSubBrandView = (EditText) view.findViewById(R.id.edittext_subbrand);
        mTypeView = (EditText) view.findViewById(R.id.edittext_type);
        mModelView = (EditText) view.findViewById(R.id.edittext_model);
        mColorView = (EditText) view.findViewById(R.id.edittext_color);
        mPlateView = (EditText) view.findViewById(R.id.edittext_plate);
        mUserView = (EditText) view.findViewById(R.id.edittext_user);
        mDateTimeView = (EditText) view.findViewById(R.id.edittext_datetime);

        mAssuranceLayout = (TextInputLayout) view.findViewById(R.id.layout_assurance);
        mPoliceNumberLayout = (TextInputLayout) view.findViewById(R.id.layout_policenumber);
        mExpirationLayout = (TextInputLayout) view.findViewById(R.id.layout_expiration);
        mTelephoneLayout = (TextInputLayout) view.findViewById(R.id.layout_telephone);

        mBrandLayout = (TextInputLayout) view.findViewById(R.id.layout_brand);
        mSubBrandLayout = (TextInputLayout) view.findViewById(R.id.layout_subbrand);
        mTypeLayout = (TextInputLayout) view.findViewById(R.id.layout_type);
        mModelLayout = (TextInputLayout) view.findViewById(R.id.layout_model);
        mColorLayout = (TextInputLayout) view.findViewById(R.id.layout_color);
        mPlateLayout = (TextInputLayout) view.findViewById(R.id.layout_plate);
        mUserLayout = (TextInputLayout) view.findViewById(R.id.layout_user);
        mDateTimeLayout = (TextInputLayout) view.findViewById(R.id.layout_datetime);

        mVehicleSubtitleLayout = (FrameLayout) view.findViewById(R.id.vehicle_subtitle_layout);
        mPossesionSubtitleLayout = (FrameLayout) view.findViewById(R.id.possession_subtitle_layout);
        mAssuranceSubtitleLayout = (FrameLayout) view.findViewById(R.id.assurance_subtitle_layout);

        mBrandView.setText(mCar.brand);
        mSubBrandView.setText(mCar.brand);
        mTypeView.setText(mCar.type);
        mModelView.setText(mCar.model);
        mColorView.setText(mCar.color);
        mPlateView.setText(mCar.plate);
        mUserView.setText(mUser.name);
        mDateTimeView.setText(getDateTime(mAssignment.start));

        if (mIsAssigned) {
            mFinishButton.setVisibility(View.GONE);
            mDestinationView.setFocusable(false);
            mTimeOfUseView.setClickable(false);
            mTimeOfUseView.setFocusable(false);
            mTimeOfUseView.setOnClickListener(null);
            mTimeOfUseView.setEnabled(false);
        }

        readAssignment();

        mAssuranceView.setText(mCar.insuranceCompany);
        mPoliceNumberView.setText(mCar.engineNumber);
        mExpirationView.setText(getDate(mCar.insurancePeriodEnd));
        mTelephoneView.setText(mCar.insuranceTelephone);

        mTimeOfUseView.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mVehicleSubtitleLayout.setOnClickListener(this);
        mPossesionSubtitleLayout.setOnClickListener(this);
        mAssuranceSubtitleLayout.setOnClickListener(this);

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
                Calendar now = Calendar.getInstance();
                MonthAdapter.CalendarDay minDate = new MonthAdapter.CalendarDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                MonthAdapter.CalendarDay maxDate = new MonthAdapter.CalendarDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 6, now.get(Calendar.DAY_OF_MONTH));

                // Initialize disabled days list
                // Disabled days are located at a formatted location in the format "yyyyMMdd"
                SparseArray<MonthAdapter.CalendarDay> disabledDays = new SparseArray<>();
                Calendar startCal = Calendar.getInstance();
                startCal.setTimeInMillis(minDate.getDateInMillis());
                Calendar endCal = Calendar.getInstance();
                endCal.setTimeInMillis(maxDate.getDateInMillis());
                // Add all weekend days within range to disabled days
                while (startCal.before(endCal)) {
                    startCal.add(Calendar.DATE, 1);
                }

                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setDateRange(minDate, maxDate)
                        // Set Disabled Days
                        .setDisabledDays(disabledDays)
                        .setOnDateSetListener(this);

                cdp.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
                break;
            case R.id.finish_button:
                    if(mIsExteriorCompleted && mIsInteriorCompleted && mIsFuelCompleted) {
                        attemptWrite();
                    } else {
                        Toast.makeText(getContext(), "No ha completado: " + notCompletedFragments(), Toast.LENGTH_LONG).show();
                    }
                break;
            case R.id.cancel_assignation_button:
                getFragmentManager().popBackStack();
                break;
            case R.id.vehicle_subtitle_layout:
                if(mBrandLayout.getVisibility() == View.GONE) {
                    mBrandLayout.setVisibility(View.VISIBLE);
                    mSubBrandLayout.setVisibility(View.VISIBLE);
                    mTypeLayout.setVisibility(View.VISIBLE);
                    mModelLayout.setVisibility(View.VISIBLE);
                    mColorLayout.setVisibility(View.VISIBLE);
                    mPlateLayout.setVisibility(View.VISIBLE);
                } else {
                    mBrandLayout.setVisibility(View.GONE);
                    mSubBrandLayout.setVisibility(View.GONE);
                    mTypeLayout.setVisibility(View.GONE);
                    mModelLayout.setVisibility(View.GONE);
                    mColorLayout.setVisibility(View.GONE);
                    mPlateLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.possession_subtitle_layout:
                if(mUserLayout.getVisibility() == View.GONE) {
                    mUserLayout.setVisibility(View.VISIBLE);
                    mDateTimeLayout.setVisibility(View.VISIBLE);
                } else {
                    mUserLayout.setVisibility(View.GONE);
                    mDateTimeLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.assurance_subtitle_layout:
                if(mAssuranceLayout.getVisibility() == View.GONE) {
                    mAssuranceLayout.setVisibility(View.VISIBLE);
                    mPoliceNumberLayout.setVisibility(View.VISIBLE);
                    mExpirationLayout.setVisibility(View.VISIBLE);
                    mTelephoneLayout.setVisibility(View.VISIBLE);
                } else {
                    mAssuranceLayout.setVisibility(View.GONE);
                    mPoliceNumberLayout.setVisibility(View.GONE);
                    mExpirationLayout.setVisibility(View.GONE);
                    mTelephoneLayout.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener(this);
        rtpd.show(getFragmentManager(), FRAG_TAG_TIME_PICKER);
        dateTime = getString(R.string.date_picker_result_value, dayOfMonth, monthOfYear + 1,
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

    @Override
    public void onResume() {
        super.onResume();
    }

    private String getDateTime(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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

    public String notCompletedFragments() {
        String fragments = "";
        boolean comma = false;
        if(!mIsExteriorCompleted) {
            fragments = fragments.concat("Verificación exterior");
            comma = true;
        }
        if(!mIsInteriorCompleted) {
            if(comma) {
                fragments = fragments.concat(", ");
            }
            fragments = fragments.concat("Verificación interior");
            comma = true;
        }
        if(!mIsFuelCompleted) {
            if(comma) {
                fragments = fragments.concat(", ");
            }
            fragments = fragments.concat("Combustible / Odómetro");
        }
        return fragments;
    }

    private void writeAssignment() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate);

        mAssignment.end = getTimeStamp(dateTime);
        mAssignment.destination = mDestinationView.getText().toString();

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

                mIsExteriorCompleted = dataSnapshot.hasChild("check/exterior/");
                mIsInteriorCompleted = dataSnapshot.hasChild("check/interior/");
                mIsFuelCompleted = mAssignment.fuelLevel > 0;

                if (mAssignment.destination.length() > 0) {
                    mDestinationView.setText(mAssignment.destination);
                }
                if (mAssignment.end > 0) {
                    mTimeOfUseView.setText(getDateTime(mAssignment.end));
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