package net.labhackercd.edemocracia.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.v62.group.GroupService;

import net.labhackercd.edemocracia.util.EDMSession;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.application.EDMApplication;

import javax.inject.Inject;


public class SignInActivity extends Activity implements LoaderCallbacks<Cursor> {

    @Inject EDMSession session;

    private UserLoginTask authenticationTask = null;

    private EditText passwordView;
    private AutoCompleteTextView emailView;

    private View progressView;
    private View loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        ((EDMApplication) getApplication()).inject(this);

        setContentView(R.layout.activity_sign_in);

        emailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }

            return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(android.R.id.progress);

        progressView.setVisibility(View.GONE);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
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

    private boolean isEmailValid(String email) {
        // XXX Everything else would be wrong. It's very, very hard to check the
        // validity of e-mail addresses.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        // Everything is possible!
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1);
            progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(SignInActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
        emailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to createSession
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {


        private final String email;
        private final String password;



        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }


        @Override
        protected Integer doInBackground(Void... params) {
            int result_code = 0;
            session.setAuthentication(new BasicAuthentication(email, password));

            GroupService groupService = new GroupService(session);

            long companyId = -1;

            JSONArray groups;
            try {
                groups = groupService.getUserSites();
                companyId = groups.getJSONObject(0).getLong("companyId");
                result_code = R.string.success;
            } catch (UnknownHostException u)
            {
                Log.d(getClass().getSimpleName(), "No Connection" + u);
                result_code = R.string.connection;
            } catch (ServerException s)
            {
                Log.d(getClass().getSimpleName(), s.toString());
                result_code = R.string.invalid_credentials;
            }

            catch (JSONException e) {
                // XXX I do not think that liferay will send a wrong answer, so i'll threat as
                // generic error
                e.printStackTrace();
                result_code = R.string.generic_error;
            } catch (Exception e) {
                // XXX THIS NEVER GOING TO HAPPEN, BUT LIFERAY ASSIGNS IT WITH GENERIC EXCEPTION
                result_code = R.string.generic_error;
            }

            session.setCompanyId(companyId);

            // Store session for future uses
            ((EDMApplication) getApplication()).saveEDMSession(session);

            return result_code;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            authenticationTask = null;

            showProgress(false);

            switch (success)
            {
                case R.string.success:
                   startActivity(new Intent(getApplicationContext(), MainActivity.class));
                   finish();
                   break;

                case R.string.connection_failure:
                    callAlertDialog(R.string.connection_failure);
                    break;

                case R.string.invalid_credentials:
                    callAlertDialog(R.string.invalid_credentials);
                    break;

                case R.string.generic_error:
                    callAlertDialog(R.string.generic_error);
                    break;
            }

        }

        private void callAlertDialog(Integer message) {
            new AlertDialog.Builder(new ContextThemeWrapper(
                    SignInActivity.this, android.R.style.Theme_Material_Light_Dialog))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        @Override
        protected void onCancelled() {
            authenticationTask = null;
            showProgress(false);
        }
    }
}
