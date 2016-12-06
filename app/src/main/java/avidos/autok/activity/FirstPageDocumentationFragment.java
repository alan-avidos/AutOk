package avidos.autok.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.User;
import avidos.autok.helper.OnPageCommunication;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FirstPageDocumentationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FirstPageDocumentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstPageDocumentationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker_name";

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";
    private static final String WHITESPACE = " ";

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


    public FirstPageDocumentationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstPageDocumentationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstPageDocumentationFragment newInstance(User user, Cars car, @Nullable Assignment assignment) {
        FirstPageDocumentationFragment fragment = new FirstPageDocumentationFragment();
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
        View view = inflater.inflate(R.layout.fragment_first_page_documentation, container, false);
        mBrandView = (EditText) view.findViewById(R.id.edittext_brand);
        mSubBrandView = (EditText) view.findViewById(R.id.edittext_subbrand);
        mTypeView = (EditText) view.findViewById(R.id.edittext_type);
        mModelView = (EditText) view.findViewById(R.id.edittext_model);
        mColorView = (EditText) view.findViewById(R.id.edittext_color);
        mPlateView = (EditText) view.findViewById(R.id.edittext_plate);
        mUserView = (EditText) view.findViewById(R.id.edittext_user);
        mDateTimeView = (EditText) view.findViewById(R.id.edittext_datetime);

        mBrandView.setText(mCar.brand);
        mSubBrandView.setText(mCar.brand);
        mTypeView.setText(mCar.type);
        mModelView.setText(mCar.model);
        mColorView.setText(mCar.color);
        mPlateView.setText(mCar.plate);
        mUserView.setText(mUser.name);
        mDateTimeView.setText(getDateTime(mAssignment.start));
        //mDateTimeView.setOnClickListener(this);

        return view;

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

        Boolean onFragmentInteraction(String action);
    }
}
