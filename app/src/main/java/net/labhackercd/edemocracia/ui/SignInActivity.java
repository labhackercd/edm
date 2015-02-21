package net.labhackercd.edemocracia.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.Credentials;

import net.labhackercd.edemocracia.data.model.User;
import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.data.api.error.ClientErrorEvent;
import net.labhackercd.edemocracia.data.api.SharedPreferencesCredentialStorage;
import net.labhackercd.edemocracia.data.api.GroupService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.AsyncExecutor;
import de.greenrobot.event.util.ThrowableFailureEvent;
import timber.log.Timber;

import net.labhackercd.edemocracia.R;

public class SignInActivity extends Activity {
    private static final String TAG = SignInActivity.class.getSimpleName();

    @Inject EventBus eventBus;
    @Inject GroupService groupService;
    @Inject SharedPreferencesCredentialStorage credentials;

    @InjectView(R.id.email) AutoCompleteTextView emailView;
    @InjectView(R.id.password) EditText passwordView;
    @InjectView(R.id.login_form) View loginFormView;
    @InjectView(android.R.id.progress) View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EDMApplication.get(this).inject(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        setContentView(R.layout.activity_sign_in);

        ButterKnife.inject(this);

        // progress view shouldn't be visible at startup
        progressView.setVisibility(View.GONE);
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

    @OnEditorAction(R.id.password)
    @SuppressWarnings("UnusedDeclaration")
    public boolean onPasswordEditorAction(TextView view, int id, KeyEvent event) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
            attemptLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.email_sign_in_button)
    @SuppressWarnings("UnusedDeclaration")
    public void onSignInButtonClick(View view) {
        attemptLogin();
    }

    public void attemptLogin() {
        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            doSignIn(email, password);
        }
    }

    protected void showProgress(final boolean show) {
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1);
            progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0);
        }
    }

    private boolean isEmailValid(String email) {
        // We won't bother users with stupid email checks.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        // Just let the users do whatever they want.
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(Success event) {
        showProgress(false);

        // TODO Store the user for future uses.

        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ThrowableFailureEvent event) {
        if (!getClass().equals(event.getExecutionScope())) {
            return;
        }

        showProgress(false);

        int errorMessage;
        if (ClientErrorEvent.isNetworkError(event)) {
            errorMessage = R.string.network_error_message;
        } else if (ClientErrorEvent.isAuthorizationError(event)) {
            errorMessage = R.string.invalid_credentials_message;
        } else {
            errorMessage = R.string.generic_error_message;
            Timber.e(event.getThrowable(), "Failed to authenticate user.");
        }

        new AlertDialog.Builder(SignInActivity.this)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
        .show();
    }

    private void doSignIn(final String email, final String password) {
        // Show the progress indicator
        showProgress(true);

        // Try to authenticate in another thread.
        AsyncExecutor.builder().buildForScope(getClass()).execute(new AsyncExecutor.RunnableEx() {
            @Override
            public void run() throws Exception {
                // Inject the supplied credentials into the client
                credentials.save(Credentials.basic(email, password));

                // Make an authenticated call
                User user = groupService.getUserById();

                // If the call goes OK, it means we're authenticated.
                // So we store the credentials for future use.
                if (user != null) {
                    eventBus.post(new Success(user));
                } else {
                    credentials.clear();
                }
            }
        });
    }

    private static class Success {
        public final User user;

        public Success(User user) {
            this.user = user;
        }
    }
}
