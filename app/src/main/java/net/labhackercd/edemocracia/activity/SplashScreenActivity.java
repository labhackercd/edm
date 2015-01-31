package net.labhackercd.edemocracia.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.MainActivity;
import net.labhackercd.edemocracia.activity.SignInActivity;
import net.labhackercd.edemocracia.util.EDMSession;


public class SplashScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        // Kick off a background thread to check if the user is authenticated
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkIsAuthenticated();
            }
        }).start();
    }

    private void onResult(boolean success) {
    }

    private void checkIsAuthenticated() {
        EDMSession session = EDMSession.get(getApplicationContext());

        boolean isAuthenticated = false;

        if (session != null) {
            try {
                isAuthenticated = session.isAuthenticated();
            } catch (Exception e) {
                Log.d(getClass().getSimpleName(), "Failed to authenticate: " + e);
            }
        }

        Class nextActivity = isAuthenticated ? MainActivity.class : SignInActivity.class;

        startActivity(new Intent(getApplicationContext(), nextActivity));
        finish();
    }
}