package avidos.autok.activity;

/**
 * Created by Alan on 10/12/2016.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import avidos.autok.R;

public class LauncherActivity extends AppCompatActivity {

    private boolean mIsBackButtonPressed;
    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    public void onBackPressed() {
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsBackButtonPressed) {
                    Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    LauncherActivity.this.startActivity(intent);
                }
                finish();
            }
        },SPLASH_DURATION);
    }

}