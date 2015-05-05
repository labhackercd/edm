package net.labhackercd.nhegatu.ui.preference;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import android.view.View;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.upload.Constants;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import java.util.Arrays;

/**
 * A preference for selecting a Google account.
 *
 * It shows an "account picker" dialog when clicked.
 *
 * TODO Show currently selected account in the widget.
 * TODO Option to *clear* the account.
 * TODO Trigger OAuth approval process (dialog) when the account is changed.
 */
public class GoogleAccountPreference extends Preference {
    private GoogleAccountCredential credential;

    private Subscription summarySubscription;
    private PublishSubject<Object> valueChanges = PublishSubject.create();
    private OnPreferenceChangeListener onPreferenceChangeListener;

    public GoogleAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        credential = GoogleAccountCredential.usingOAuth2(
                getContext().getApplicationContext(), Arrays.asList(Constants.AUTH_SCOPES));
        credential.setBackOff(new ExponentialBackOff());

        onPreferenceChangeListener = super.getOnPreferenceChangeListener();

        super.setOnPreferenceChangeListener((preference, value) -> {
            valueChanges.onNext(value);
            return onPreferenceChangeListener == null
                    || onPreferenceChangeListener.onPreferenceChange(preference, value);
        });
    }

    @Override
    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return onPreferenceChangeListener;
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.onPreferenceChangeListener = onPreferenceChangeListener;
    }

    public Observable<Object> getPreferenceChangeObservable() {
        return valueChanges.asObservable();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // FIXME What to do with the "defaultValue"?
        if (restorePersistedValue) {
            String account = getPersistedString(null);
            if (account != null)
                credential.setSelectedAccountName(account);
        } else {
            String account = credential.getSelectedAccountName();
            if (account != null)
                persistString(account);
        }
        super.onSetInitialValue(restorePersistedValue, defaultValue);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        // Display the selected account instead of the summary.
        summarySubscription = getPreferenceChangeObservable()
                .startWith(getCurrentValue())
                .distinctUntilChanged()
                .map(account -> account == null ? null : account.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setSelectedAccount);
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        if (summarySubscription != null) {
            summarySubscription.unsubscribe();
            summarySubscription = null;
        }
    }

    private void setSelectedAccount(String account) {
        if (account == null)
            setSummary(R.string.none_selected);
        else
            setSummary(account);
    }

    public boolean onChooseAccountResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String account = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
            if (account != null && callChangeListener(account)) {
                return setAccount(account);
            }
        }
        return false;
    }

    private boolean setAccount(String account) {
        credential.setSelectedAccountName(account);
        return persistString(account);
    }

    private Object getCurrentValue() {
        return credential == null ? null : credential.getSelectedAccountName();
    }

    public Intent newChooseAccountIntent() {
        return credential.newChooseAccountIntent();
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }
}
