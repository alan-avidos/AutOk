package avidos.autok.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.Arrays;

import avidos.autok.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, ExteriorFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, AssignationFragment.OnFragmentInteractionListener,
        FuelFragment.OnFragmentInteractionListener, InteriorFragment.OnFragmentInteractionListener,
        CancelDialog.CancelDialogListener, DocumentationFragment.OnFragmentInteractionListener {

    // Fragments
    private MainFragment mMainFragment;
    private ProfileFragment mProfileFragment;
    // FireBase references.
    private FirebaseAuth mAuth;
    //
    private boolean isACarAssigned = false;
    private boolean [] mFragmentInteraction = {false, false, false};

    /*

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toolbar.setNavigationOnClickListener(this);
        toggle.syncState();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mMainFragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mMainFragment, "MainFragment").commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_assignments) {

            if(isACarAssigned) {
                AssignationFragment mAssignationFragment = (AssignationFragment) getSupportFragmentManager().findFragmentByTag("AssignationFragment");
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mAssignationFragment, "AssignationFragment").commit();
            } else {
                mMainFragment = MainFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mMainFragment, "MainFragment").commit();
            }
        } else if (id == R.id.nav_profile) {

            mProfileFragment = ProfileFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mProfileFragment, "ProfileFragment").commit();
        } else if (id == R.id.nav_signout) {

            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            MainActivity.this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onClick(View v) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            drawer.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void onFragmentInteraction(String fragment) {

        if (fragment.equals("AssignationFragment")) {
            isACarAssigned = true;
        } else if (fragment.equals("MainFragment")) {
            isACarAssigned = false;
        } else if (fragment.equals("ExteriorFragment")) {
            mFragmentInteraction[0] = true;
        } else if (fragment.equals("InteriorFragment")) {
            mFragmentInteraction[1] = true;
        } else if (fragment.equals("FuelFragment")) {
            mFragmentInteraction[2] = true;
        } else if (fragment.equals("Reset")) {
            Arrays.fill(mFragmentInteraction, false);
        }
    }

    @Override
    public boolean checkFragmentInteraction() {
        for (boolean interacted : mFragmentInteraction) {
            if (!interacted) return false;
        }
        return true;
    }

    @Override
    public String getFragments() {
        String fragments = "";
        boolean comma = false;
        if(!mFragmentInteraction[0]) {
            fragments = fragments.concat("Verificación exterior");
            comma = true;
        }
        if(!mFragmentInteraction[1]) {
            if(comma) {
                fragments = fragments.concat(", ");
            }
            fragments = fragments.concat("Verificación interior");
            comma = true;
        }
        if(!mFragmentInteraction[2]) {
            if(comma) {
                fragments = fragments.concat(", ");
            }
            fragments = fragments.concat("Combustible / Odómetro");
        }
        return fragments;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        AssignationFragment mAssignationFragment = (AssignationFragment) getSupportFragmentManager().findFragmentByTag("AssignationFragment");
        mAssignationFragment.reAssignCar();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
