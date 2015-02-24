package net.labhackercd.edemocracia.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.liferay.mobile.android.v62.user.UserService;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.api.EDMSession;
import net.labhackercd.edemocracia.data.api.SessionManager;

import org.json.JSONObject;

import javax.inject.Inject;

import timber.log.Timber;

public class SplashScreenActivity extends Activity {
    @Inject EDMSession session;
    @Inject SessionManager sessionManager;

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
        // with some credentials and some user information.
        boolean isAuthenticated = session.getAuthentication() != null && session.getUser() != null;

        if (isAuthenticated) {
            JSONObject jsonUser;

            try {
                // But just for the sake of it, we try to reach the remote service.
                // And since we're at it, update the user information.
                jsonUser = new UserService(session).getUserById(session.getUser().getUserId());

                // FIXME We should only set and save if the user info changed
                session.setUser(User.JSON_READER.fromJSON(jsonUser));

                sessionManager.save(session);
            } catch (Exception e) {
                // We keep the previous result if something goes wrong, but we log the exception
                // as a warning just for the sake of it.
                Timber.e(e, "Failed to check user credentials.");
            }
        }

        Class nextActivity = isAuthenticated ? MainActivity.class : SignInActivity.class;

        startActivity(new Intent(getApplicationContext(), nextActivity));
        finish();
    }
}