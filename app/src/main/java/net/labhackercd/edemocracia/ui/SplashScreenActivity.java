package net.labhackercd.edemocracia.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.liferay.mobile.android.service.Session;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.data.api.CredentialStore;
import net.labhackercd.edemocracia.data.api.exception.AuthorizationException;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class SplashScreenActivity extends Activity {
    @Inject Session session;
    @Inject CredentialStore credentialStore;

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
        Class<? extends Activity> nextActivity;

        try {
            JSONObject command = new JSONObject("{\"/user/get-user-by-id\": {}}");
            session.invoke(command).getJSONObject(0);
            nextActivity = MainActivity.class;
        } catch (AuthorizationException e) {
            // Unauthorized. Send user to the login screen.
            nextActivity = SignInActivity.class;
        } catch (IOException e) {
            // This is a network error. Let the user go through only if there are credentials stored.
            if (credentialStore.get() != null) {
                nextActivity = MainActivity.class;
            } else {
                nextActivity = SignInActivity.class;
            }
        } catch (Exception e) {
            Timber.e(e, "Error while trying to load User.");
            nextActivity = SignInActivity.class;
        }

        startActivity(new Intent(getApplicationContext(), nextActivity));
        finish();
    }
}