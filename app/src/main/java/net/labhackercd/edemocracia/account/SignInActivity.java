package net.labhackercd.edemocracia.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.EDMErrorHandler;
import net.labhackercd.edemocracia.data.api.model.User;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static net.labhackercd.edemocracia.account.AccountConstants.ACCOUNT_TYPE;

public class SignInActivity extends ActionBarActivity {
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_AUTHTOKEN_TYPE = "authTokenType";

    @Inject UserData userData;
    @Inject MainRepository repository;

    @InjectView(R.id.email) AutoCompleteTextView emailView;
    @InjectView(R.id.password) EditText passwordView;
    @InjectView(R.id.login_form) View loginFormView;
    @InjectView(android.R.id.progress) View progressView;

    private String authTokenType;
    private boolean requestNewAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        super.onCreate(savedInstanceState);

        EDMApplication.get(this).getObjectGraph().inject(this);

        setContentView(R.layout.activity_sign_in);

        ButterKnife.inject(this);

        // progress view shouldn't be visible at startup
        progressView.setVisibility(View.GONE);

        final Intent intent = getIntent();
        String email = intent.getStringExtra(PARAM_EMAIL);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        requestNewAccount = TextUtils.isEmpty(email);

        if (!requestNewAccount) {
            emailView.setText(email);
            emailView.setEnabled(false);
            emailView.setFocusable(false);
        }
    }

    @OnEditorAction(R.id.password)
    @SuppressWarnings("UnusedDeclaration")
    public boolean onPasswordEditorAction(TextView view, int id, KeyEvent event) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
            handleLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.email_sign_in_button)
    @SuppressWarnings("UnusedDeclaration")
    public void onSignInButtonClick(View view) {
        handleLogin();
    }

    public void handleLogin() {
        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

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
            checkCredentials(email, password)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(() -> showProgress(true))
                    .doOnError(t -> showProgress(false))
                    .map(user -> new Pair<>(user, password))
                    .subscribe(this::handleSuccess, this::handleError);
        }
    }

    private Observable<User> checkCredentials(String email, String password) {
        return repository.getUserWithCredentials(email, password).asObservable();
    }

    private void handleSuccess(Pair<User, String> pair) {
        User user = pair.first;
        String email = user.getEmailAddress();
        String password = pair.second;

        AccountManager manager = AccountManager.get(this);

        Account account = new Account(user.getEmailAddress(), ACCOUNT_TYPE);
        if (requestNewAccount) {
            manager.addAccountExplicitly(account, password, null);
            configureSyncFor(account);
        } else {
            manager.setPassword(account, password);
        }

        // Save the user.
        userData.setUser(manager, account, user);

        finishLogin(email, password);
    }

    private void handleError(Throwable throwable) {
        int errorMessage;
        if (EDMErrorHandler.isNetworkError(throwable)) {
            errorMessage = R.string.network_error_message;
        } else if (EDMErrorHandler.isAuthorizationError(throwable)) {
            errorMessage = R.string.invalid_credentials_message;
        } else {
            errorMessage = R.string.unknown_error_message;
            Timber.e(throwable, "Failed to check credentials.");
        }
        new AlertDialog.Builder(this)
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

    private void configureSyncFor(Account account) {
        // TODO
    }

    private void finishLogin(String email, String password) {
        Intent intent = new Intent();
        intent.putExtra(KEY_ACCOUNT_NAME, email);
        intent.putExtra(KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        if (ACCOUNT_TYPE.equals(authTokenType))
            intent.putExtra(KEY_AUTHTOKEN, password);
        setResult(RESULT_OK, intent);
        finish();
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
}
