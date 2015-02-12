package net.labhackercd.edemocracia.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.error.ClientErrorEvent;
import net.labhackercd.edemocracia.data.model.User;
import net.labhackercd.edemocracia.data.api.SharedPreferencesCredentialStorage;
import net.labhackercd.edemocracia.data.api.GroupService;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.AsyncExecutor;
import de.greenrobot.event.util.ThrowableFailureEvent;
import timber.log.Timber;

public class SplashScreenActivity extends Activity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Inject EventBus eventBus;
    @Inject GroupService groupService;
    @Inject SharedPreferencesCredentialStorage credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EDMApplication.get(this).inject(this);

        setContentView(R.layout.activity_splash_screen);

        // Kick off a background thread to check if the user is authenticated
        AsyncExecutor.builder().buildForScope(this).execute(new AsyncExecutor.RunnableEx() {
            @Override
            public void run() throws Exception {
                User user = groupService.getUserById();
                eventBus.post(new AuthenticationSuccess(user));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    /**
     * Called when the authentication attempt goes alright.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AuthenticationSuccess event) {
        // TODO Store the user for future uses.

        startNextActivity(true);
    }

    /**
     * Called when something goes wrong while authenticating.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ThrowableFailureEvent event) {
        if (!this.equals(event.getExecutionScope())) {
            // Don't touch other people's business.
            return;
        }

        // If the failure wasn't caused by a network error send the user
        // to the login screen.
        if (!ClientErrorEvent.isNetworkError(event)) {
            credentials.clear();
        }

        // Only way we know to *kind of check* if the user is authenticated without
        // relying on connectivity.
        boolean isAuthenticated = credentials.load() != null;

        startNextActivity(isAuthenticated);
    }

    private void startNextActivity(boolean isAuthenticated) {
        Class nextActivity = isAuthenticated ? MainActivity.class : SignInActivity.class;

        startActivity(new Intent(getApplicationContext(), nextActivity));
        finish();
    }

    private static class AuthenticationSuccess {
        public final User user;

        public AuthenticationSuccess(User user) {
            this.user = user;
        }
    }
}