package net.labhackercd.edemocracia.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.v62.group.GroupService;

import net.labhackercd.edemocracia.liferay.exception.AuthorizationException;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

import org.json.JSONArray;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.liferay.session.SessionManager;


public class SignInActivity extends Activity {

    private static final String TAG = SignInActivity.class.getSimpleName();

    @Inject EDMSession session;
    @Inject SessionManager sessionManager;

    @InjectView(R.id.email) AutoCompleteTextView emailView;
    @InjectView(R.id.password) EditText passwordView;
    @InjectView(R.id.login_form) View loginFormView;
    @InjectView(android.R.id.progress) View progressView;

    private UserLoginTask authenticationTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EDMApplication) getApplication()).inject(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        setContentView(R.layout.activity_sign_in);

        ButterKnife.inject(this);

        // progress view shouldn't be visible at startup
        progressView.setVisibility(View.GONE);
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
        if (authenticationTask != null) {
            return;
        }

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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            authenticationTask = new UserLoginTask(email, password);
            authenticationTask.execute((Void) null);
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

    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String email;
        private final String password;

        public UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // Prepare to test the supplied credentials
            session.setAuthentication(new BasicAuthentication(email, password));

            GroupService groupService = new GroupService(session);

            int error = 0;
            long companyId = -1;
            Exception exception = null;

            try {
                JSONArray userGroups = groupService.getUserSites();
                companyId = userGroups.getJSONObject(0).getLong("companyId");
            } catch (IOException e) {
                // IOException are probably only caused by network problems
                exception = e;
                error = R.string.network_error_message;
            } catch (AuthorizationException e) {
                exception = e;
                error = R.string.invalid_credentials_message;
            } catch (Exception e) {
                exception = e;
                error = R.string.unknown_error_message;
            }

            if (companyId < 0 || exception != null) {
                Log.e(TAG, "Failed to authenticate user: " + (exception == null ? "Unknown error" : exception));
            } else {
                // Save the companyId
                session.setCompanyId(companyId);

                // And persist the session for future uses.
                sessionManager.save(session);
            }

            return error;
        }

        @Override
        protected void onPostExecute(final Integer error) {
            authenticationTask = null;

            showProgress(false);

            AlertDialog.Builder dialogBuilder;

            if (error != 0) {
                new AlertDialog.Builder(SignInActivity.this)
                        .setMessage(error)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                // It's all good, present the user with the MainActivity.
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        }
        @Override

        protected void onCancelled() {
            authenticationTask = null;
            showProgress(false);
        }
    }
}
