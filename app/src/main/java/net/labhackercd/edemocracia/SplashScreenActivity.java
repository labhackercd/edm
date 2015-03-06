package net.labhackercd.edemocracia.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserData;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.User;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SplashScreenActivity extends BaseActivity {
    @Inject UserData userData;
    @Inject DataRepository repository;

    private AccountManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        super.onCreate(savedInstanceState);

        manager = AccountManager.get(this);

        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void injectObjectGraph() {
        super.injectObjectGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();
        repository.getUser()
                .subscribeOn(Schedulers.io())
                .last()
                .compose(RxOperators.signInOnAuthorizationError(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError);
    }

    private void handleSuccess(User user) {
        Account account = AccountUtils.getAccount(this);

        userData.setUser(manager, account, user);

        startMainActivity();
    }

    private void handleError(Throwable t) {
        /*
        if (EDMErrorHandler.isAuthorizationError(t)) {
            requestAccount(this)
                    .subscribeOn(Schedulers.io())
                    .map(account -> userData.getUser(manager, account))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleSuccess, this::handleError);
            return;
        } else if (EDMErrorHandler.isNetworkError(t)) {
            Account account = AccountUtils.getAccount(this);
            if (account != null && userData.getUser(manager, account) != null) {
                startMainActivity();
                return;
            }
        }
        */

        // Log the error...
        Timber.e(t, "Failed to retrieve user.");

        // ...and let it burn!
        throw new RuntimeException(t);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Observable<Account> requestAccount(final Activity activity) {
        return Observable.create(subscriber -> {
            AccountManager manager = AccountManager.get(activity);
            try {
                Account account = AccountUtils.getAccount(manager, activity);
                subscriber.onNext(account);
                subscriber.onCompleted();
            /*
            TODO What if?
            } catch (IOException e) {
                Account account = AccountUtils.getAccount(activity);
                if (account != null) {
                    User user = userData.getUser(manager, account);
                    if (user != null) {
                        startMainActivity();
                    }
                }
            */
            } catch (IOException | AccountsException e) {
                subscriber.onError(e);
            }
        });
    }
}