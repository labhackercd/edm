package net.labhackercd.edemocracia.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.util.EDMSession;

import javax.inject.Inject;


public class SplashScreenActivity extends Activity {
    @Inject EDMSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EDMApplication) getApplication()).inject(this);

        setContentView(R.layout.activity_splash_screen);

        // Kick off a background thread to check if the user is authenticated
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkIsAuthenticated();
            }
        }).start();
    }

    private void checkIsAuthenticated() {
        boolean isAuthenticated = false;

        try {
            isAuthenticated = session.isAuthenticated();
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "Failed to authenticate: " + e);
        }

        Class nextActivity = isAuthenticated ? MainActivity.class : SignInActivity.class;

        startActivity(new Intent(getApplicationContext(), nextActivity));

        finish();
    }
}