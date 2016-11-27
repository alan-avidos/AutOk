package avidos.autok.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.FuelLoad;
import avidos.autok.entity.User;
import me.tankery.lib.circularseekbar.CircularSeekBar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FuelFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FuelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FuelFragment extends Fragment implements NumberPickerDialogFragment.NumberPickerDialogHandlerV2 {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";

    private User mUser;
    private Cars mCar;
    private BigInteger mKilometers;
    private FuelLoad fuelLoad;
    private List<FuelLoad> fuelLoads;
    private Locale locale;
    private boolean mIsOdometerModified = false;
    private boolean mIsFuelLevelModified = false;
    // Fragment
    private OnFragmentInteractionListener mListener;
    // UI
    private RelativeLayout mKmPicker;
    private TextView mTextViewKm;
    private TextView mTextViewInfo;
    private ImageView mImageViewUp;
    private ImageView mImageViewDown;
    private CircularSeekBar mFuelLevel;
    private EditText mEditTextLiters;
    private EditText mEditTextCost;
    private EditText mEditTextKm;
    private Button mReFuelButton;
    private Button mSaveButton;
    private FloatingActionButton mDoneButton;
    private PercentRelativeLayout mSeekBarLayout;


    // FireBase
    private DatabaseReference mDatabaseFuel;
    private DatabaseReference mDatabaseReFuel;

    public FuelFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FuelFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FuelFragment newInstance(User user, Cars car) {
        FuelFragment fragment = new FuelFragment();
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
        View view = inflater.inflate(R.layout.fragment_fuel, container, false);

        getLocale();

        mKmPicker = (RelativeLayout) view.findViewById(R.id.km_picker);
        mTextViewKm = (TextView) view.findViewById(R.id.text_kilometers);
        mTextViewInfo = (TextView) view.findViewById(R.id.car_info_fuel);
        mImageViewDown = (ImageView) view.findViewById(R.id.down_button);
        mImageViewUp = (ImageView) view.findViewById(R.id.up_button);
        mFuelLevel = (CircularSeekBar) view.findViewById(R.id.fuel_level);
        mEditTextCost = (EditText) view.findViewById(R.id.editText_price);
        mEditTextLiters = (EditText) view.findViewById(R.id.editText_litres);
        mEditTextKm = (EditText) view.findViewById(R.id.editText_km);
        mSaveButton = (Button) view.findViewById(R.id.save_fuel_button);
        mReFuelButton = (Button) view.findViewById(R.id.refuel_button);
        mDoneButton = (FloatingActionButton) view.findViewById(R.id.fab_fuel_done);
        mSeekBarLayout = (PercentRelativeLayout) view.findViewById(R.id.seekbar_layout);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mTextViewInfo.setText(getString(R.string.title_car_info, mCar.brand, mCar.model, mCar.plate));

        readFuelOdometer();
        readLastReFuel();

        mKilometers = BigInteger.ZERO;

        mKmPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getFragmentManager())
                        .setTargetFragment(FuelFragment.this)
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setDecimalVisibility(View.INVISIBLE)
                        .setMaxNumber(BigDecimal.valueOf((long)999999));
                npb.show();
            }
        });

        mFuelLevel.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                if(mIsFuelLevelModified) {
                    if(!mIsOdometerModified) {
                        showFAB();
                    }
                    mIsFuelLevelModified = true;
                }
                writeFuelLevel((double) progress / 100);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });

        mImageViewDown.setOnClickListener(onDownUpPressed);
        mImageViewUp.setOnClickListener(onDownUpPressed);

        mSaveButton.setOnClickListener(onClickListener);
        mReFuelButton.setOnClickListener(onClickListener);

        mEditTextCost.addTextChangedListener(generalTextWatcher);
        mEditTextKm.addTextChangedListener(generalTextWatcher);
        mEditTextLiters.addTextChangedListener(generalTextWatcher);

        return view;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.save_fuel_button:
                    attemptToWrite();
                    break;
                case R.id.refuel_button:
                    mEditTextLiters.setText("");
                    mEditTextKm.setText("");
                    mEditTextCost.setText("");
                    mEditTextCost.setEnabled(true);
                    mEditTextKm.setEnabled(true);
                    mEditTextLiters.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };

    View.OnClickListener onDownUpPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.down_button:
                    mKilometers = mKilometers.subtract(BigInteger.ONE);
                    mTextViewKm.setText(NumberFormat.getNumberInstance(locale).format(mKilometers));
                    break;
                case R.id.up_button:
                    mKilometers = mKilometers.add(BigInteger.ONE);
                    mTextViewKm.setText(NumberFormat.getNumberInstance(locale).format(mKilometers));
                    break;
                default:
                    break;
            }
            writeOdometer(Long.valueOf(mKilometers.toString()));
        }
    };

    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

            mKmPicker.setVisibility(View.GONE);
            mSeekBarLayout.setVisibility(View.GONE);

            if (mEditTextLiters.getText().hashCode() == s.hashCode()) {

            } else if (mEditTextCost.getText().hashCode() == s.hashCode()) {

            } else if (mEditTextKm.getText().hashCode() == s.hashCode()) {

            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

            if (mEditTextLiters.getText().hashCode() == s.hashCode()) {

            } else if (mEditTextCost.getText().hashCode() == s.hashCode()) {

            } else if (mEditTextKm.getText().hashCode() == s.hashCode()) {

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (mEditTextLiters.getText().hashCode() == s.hashCode()) {

                mSaveButton.setEnabled(true);
            } else if (mEditTextCost.getText().hashCode() == s.hashCode()) {

                mSaveButton.setEnabled(true);
            } else if (mEditTextKm.getText().hashCode() == s.hashCode()) {

                mSaveButton.setEnabled(true);
            }
            mKmPicker.setVisibility(View.VISIBLE);
            mSeekBarLayout.setVisibility(View.VISIBLE);
        }
    };

    public void readLastReFuel() {

        fuelLoads = new ArrayList<>();
        mDatabaseReFuel = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("fuelLoad");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get fuelLoad object and use the values to update the UI

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    fuelLoads.add(postSnapshot.getValue(FuelLoad.class));
                }

                if(fuelLoads.size() == 0)
                    return;

                fuelLoad = fuelLoads.get(fuelLoads.size()-1);
                mEditTextCost.setText(String.format(Locale.getDefault(), "%f", fuelLoad.costPerLiter));
                mEditTextKm.setText(String.format(Locale.getDefault(), "%d", fuelLoad.km));
                mEditTextLiters.setText(String.format(Locale.getDefault(), "%d", fuelLoad.amount));
                mReFuelButton.setEnabled(true);
                mEditTextCost.setEnabled(false);
                mEditTextKm.setEnabled(false);
                mEditTextLiters.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting fuelLoad failed, log a message
                Log.w("__load__", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabaseReFuel.addValueEventListener(reFuelListener);
    }

    public void readFuelOdometer() {

        fuelLoads = new ArrayList<>();
        mDatabaseReFuel = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment");
        ValueEventListener reFuelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get fuelLoad object and use the values to update the UI

                Assignment assignment = dataSnapshot.getValue(Assignment.class);
                if(assignment == null)
                    return;
                mFuelLevel.setProgress((float) (assignment.fuelLevel * 100));
                mTextViewKm.setText(NumberFormat.getNumberInstance(locale).format(assignment.mileage));
                if(assignment.fuelLevel > 0.01 && assignment.mileage > 0) {
                    if(mIsOdometerModified) {
                        if(!mIsFuelLevelModified) {
                            showFAB();
                        }
                        mIsFuelLevelModified = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting fuelLoad failed, log a message
                Log.w("__load__", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabaseReFuel.addValueEventListener(reFuelListener);
    }

    public void attemptToWrite() {

        mEditTextCost.setError(null);
        mEditTextKm.setError(null);
        mEditTextLiters.setError(null);

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(mEditTextLiters.getText().toString())) {
            mEditTextLiters.setError(getString(R.string.error_field_required));
            focusView = mEditTextLiters;
            cancel = true;
        } else if(TextUtils.isEmpty(mEditTextCost.getText().toString())) {
            mEditTextCost.setError(getString(R.string.error_field_required));
            focusView = mEditTextCost;
            cancel = true;
        } else if(TextUtils.isEmpty(mEditTextKm.getText().toString())) {
            mEditTextKm.setError(getString(R.string.error_field_required));
            focusView = mEditTextKm;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {

            writeFuelLoad();
            mSaveButton.setEnabled(false);
            mReFuelButton.setEnabled(true);
            mEditTextCost.setEnabled(false);
            mEditTextKm.setEnabled(false);
            mEditTextLiters.setEnabled(false);
        }
    }

    public void writeFuelLoad() {
        mDatabaseReFuel = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("fuelLoad").child(String.valueOf(System.currentTimeMillis()/1000));
        mDatabaseReFuel.setValue(new FuelLoad(Long.valueOf(mEditTextLiters.getText().toString()), Double.valueOf(mEditTextCost.getText().toString()), Long.valueOf(mEditTextKm.getText().toString())));
    }

    public void writeFuelLevel(double fuelLevel) {
        mDatabaseFuel = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("fuelLevel");
        mDatabaseFuel.setValue(fuelLevel);
    }

    public void writeOdometer(Long kilometers) {
        if(mIsOdometerModified) {
            if(!mIsFuelLevelModified) {
                showFAB();
            }
            mIsFuelLevelModified = true;
        }
        mDatabaseFuel = FirebaseDatabase.getInstance().getReference().child("cars").child(mUser.adminUid).child(mCar.plate).child("assignment").child("mileage");
        mDatabaseFuel.setValue(kilometers);
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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
        mKilometers = number;
        mTextViewKm.setText(NumberFormat.getNumberInstance(locale).format(mKilometers));
        writeOdometer(Long.valueOf(mKilometers.toString()));
    }

    public void getLocale() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getContext().getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getContext().getResources().getConfiguration().locale;
        }
    }

    public void showFAB() {
        mDoneButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        mDoneButton.setClickable(true);
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
