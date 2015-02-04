package net.labhackercd.edemocracia.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.liferay.mobile.android.v62.group.GroupService;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

import javax.inject.Inject;


public class SplashScreenActivity extends Activity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

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
        // Essentially, the session is authenticated if it is associated
        // with some credentials and a companyId
        boolean isAuthenticated = session.getAuthentication() != null && session.getCompanyId() > 0;

        try {
            // But just for the sake of it, we try to reach the remote service.
            isAuthenticated = new GroupService(session).getUserSites().length() > 0;
        } catch (Exception e) {
            // We keep the previous result if something goes wrong, but we log the exception
            // as a warning just for the sake of it.
            Log.w(TAG, "Failed to check user credentials.");
        }

        Class nextActivity = isAuthenticated ? MainActivity.class : SignInActivity.class;

        startActivity(new Intent(getApplicationContext(), nextActivity));
        finish();
    }
}