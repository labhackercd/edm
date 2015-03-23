package net.labhackercd.edemocracia.ui.preference;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import net.labhackercd.edemocracia.youtube.Constants;

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

    public GoogleAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        credential = GoogleAccountCredential.usingOAuth2(
                getContext().getApplicationContext(), Arrays.asList(Constants.AUTH_SCOPES));
        credential.setBackOff(new ExponentialBackOff());
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

    public void onChooseAccountResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String account = data.getExtras()
                    .getString(AccountManager.KEY_ACCOUNT_NAME);
            if (account != null) {
                credential.setSelectedAccountName(account);
                persistString(account);
            }
        }
    }

    public Intent newChooseAccountIntent() {
        return credential.newChooseAccountIntent();
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }
}
