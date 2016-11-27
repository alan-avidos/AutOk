package avidos.autok.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SecondPageDocumentationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SecondPageDocumentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecondPageDocumentationFragment extends Fragment {


    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";

    private Cars mCar;
    private User mUser;
    private Assignment mAssignment;
    private OnFragmentInteractionListener mListener;
    // UI
    private EditText mDestinationView;
    private EditText mTimeOfUseView;
    private EditText mAssuranceView;
    private EditText mPoliceNumberView;
    private EditText mExpirationView;
    private EditText mTelephoneView;
    private Button mFinishButton;

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
    public static SecondPageDocumentationFragment newInstance(User user, Cars car, @Nullable Assignment assignment) {
        SecondPageDocumentationFragment fragment = new SecondPageDocumentationFragment();
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
        View view = inflater.inflate(R.layout.fragment_second_page_documentation, container, false);

        mDestinationView = (EditText) view.findViewById(R.id.edittext_brand);
        mTimeOfUseView = (EditText) view.findViewById(R.id.edittext_timeofuse);
        mAssuranceView = (EditText) view.findViewById(R.id.edittext_assurance);
        mPoliceNumberView = (EditText) view.findViewById(R.id.edittext_policenumber);
        mExpirationView = (EditText) view.findViewById(R.id.edittext_expiration);
        mTelephoneView = (EditText) view.findViewById(R.id.edittext_telephone);
        mFinishButton = (Button) view.findViewById(R.id.finish_button);

        mAssuranceView.setText(mCar.insuranceCompany);
        mPoliceNumberView.setText(mCar.engineNumber);
        mExpirationView.setText(mCar.insurancePeriodEnd.toString());
        mTelephoneView.setText(mCar.insuranceTelephone);

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
