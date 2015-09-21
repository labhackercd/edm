/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.ui;

import android.accounts.Account;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import net.labhackercd.nhegatu.EDMApplication;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountManager;
import net.labhackercd.nhegatu.account.Authenticator;
import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.api.client.error.AuthorizationException;
import net.labhackercd.nhegatu.data.api.client.error.ProbablyNetworkError;
import net.labhackercd.nhegatu.data.api.model.User;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class SignInActivity extends AppCompatActivity {
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_AUTHTOKEN_TYPE = "authTokenType";

    @Inject EDMService service;

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

        findViewById(R.id.sign_up).setOnClickListener(this::onSignUpButtonClicked);

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

    private void onSignUpButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // TODO Remove hardcoded URL.
        // TODO Use https url (not using now since its broken coz of mixed http/https content)
        intent.setData(Uri.parse("http://edemocracia.camara.gov.br/cadastro"));
        startActivity(intent);
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(() -> showProgress(true))
                    .map(user -> {
                        AccountManager manager = AccountManager.get(this);

                        Account account;
                        if (requestNewAccount) {
                            account = manager.addAccountExplicitly(user, password);
                        } else {
                            account = AccountManager.createAccount(user);
                            manager.setPassword(account, password);
                        }

                        return account;
                    })
                    .subscribe(account -> finishLogin(account, password), this::handleError);
        }
    }

    private Observable<User> checkCredentials(String email, String password) {
        return Observable.defer(() -> {
            JSONObject json = service.newBuilder()
                    .setAuthentication(new BasicAuthentication(email, password))
                    .build()
                    .getUser();
            try {
                return Observable.just(json == null ? null : User.JSON_READER.fromJSON(json));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void finishLogin(Account account, String password) {
        String authToken = account.type.equals(authTokenType) ? password : null;
        Intent intent = Authenticator.createLoginOkIntent(account, authToken);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void handleError(Throwable error) {
        showProgress(false);

        String message;
        if (error instanceof AuthorizationException) {
            message = getString(R.string.invalid_credentials_message);
        } else if (error instanceof ProbablyNetworkError) {
            message = getString(R.string.network_error_message);
        } else {
            Timber.e(error, "Error while checking user credentials.");
            message = error.getMessage();
        }

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
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
