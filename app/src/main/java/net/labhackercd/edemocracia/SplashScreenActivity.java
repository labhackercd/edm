package net.labhackercd.edemocracia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.liferay.mobile.android.service.Session;

import net.labhackercd.edemocracia.account.CredentialStore;
import net.labhackercd.edemocracia.account.SignInActivity;
import net.labhackercd.edemocracia.data.api.exception.AuthorizationException;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.MainActivity;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.AsyncExecutor;
import de.greenrobot.event.util.ThrowableFailureEvent;
import timber.log.Timber;

public class SplashScreenActivity extends Activity {
    @Inject Session session;
    @Inject EventBus eventBus;
    @Inject CredentialStore credentialStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EDMApplication.get(this).getObjectGraph().inject(this);

        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();

        eventBus.register(this);

        AsyncExecutor.builder().buildForScope(getClass()).execute(new AsyncExecutor.RunnableEx() {
            @Override
            public void run() throws Exception {
                JSONObject command = new JSONObject("{\"/user/get-user-by-id\": {}}");
                JSONObject jsonUser = session.invoke(command).getJSONObject(0);
                User user = User.JSON_READER.fromJSON(jsonUser);
                eventBus.post(new UserLoaded(user));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(UserLoaded event) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.KEY_USER, event.user);
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ThrowableFailureEvent event) {
        if (!getClass().equals(event.getExecutionScope())) {
            // Not our business...
            return;
        }

        Throwable throwable = event.getThrowable();

        if (throwable instanceof AuthorizationException
            || throwable instanceof IOException) {
            // If it's an authentication failure or a network error, send the user to the
            // sign in activity.
            credentialStore.clear();

            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Log the error.
        Timber.e(throwable, "Failed to load user.");

        // Show a toast with some random error message.
        Toast.makeText(this, R.string.unknown_error_message, Toast.LENGTH_LONG).show();

        // Kill the application
        finish();
    }

    private class UserLoaded {
        private final User user;

        public UserLoaded(User user) {
            this.user = user;
        }
    }
}