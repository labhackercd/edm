package net.labhackercd.edemocracia.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserData;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.User;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
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
                .last() // Always fresh.
                .compose(RxOperators.requireAccount(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError);
    }

    private void handleSuccess(User user) {
        Account account = AccountUtils.getAccount(this);

        userData.setUser(manager, account, user);

        startMainActivity();
    }

    private void handleError(Throwable t) {
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
}