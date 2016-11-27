package avidos.autok.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;

import avidos.autok.R;
import avidos.autok.entity.Assignment;
import avidos.autok.entity.Cars;
import avidos.autok.entity.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DocumentationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DocumentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocumentationFragment extends Fragment {

    private static final String ARG_CAR = "car";
    private static final String ARG_USER = "user";
    private static final String ARG_ASSIGNMENT = "assignment";

    private User mUser;
    private Cars mCar;
    private Assignment mAssignment;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private CirclePageIndicator mIndicator;

    private OnFragmentInteractionListener mListener;

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
    public static DocumentationFragment newInstance(User user, Cars car, Assignment assignment) {
        DocumentationFragment fragment = new DocumentationFragment();
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
        View view = inflater.inflate(R.layout.fragment_documentation, container, false);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FirstPageDocumentationFragment.newInstance(mUser, mCar, null);
                case 1:
                    return SecondPageDocumentationFragment.newInstance(mUser, mCar, null);
                default:
                    return FirstPageDocumentationFragment.newInstance(mUser, mCar, null);
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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