package net.labhackercd.edemocracia.ui.preference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.db.LocalMessage;

import java.io.IOException;

import javax.inject.Inject;

import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PreferenceFragment extends android.preference.PreferenceFragment {
    public static final String PREF_YOUTUBE_ACCOUNT = "youtube_account";

    private static final String ACTION_RECOVER_AUTH_EXCEPTION = "recoverAuthException";
    private static final String EXTRA_RECOVER_INTENT = "recoverIntent";
    private static final int REQUEST_CODE_CHOOSE_ACCOUNT = 1;
    private static final int REQUEST_CODE_AUTHORIZE = 2;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 3;

    @Inject LocalMessageStore messages;

    private GoogleAccountPreference googleCredentialPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_screen);

        googleCredentialPreference = (GoogleAccountPreference) findPreference(PREF_YOUTUBE_ACCOUNT);
        googleCredentialPreference.setOnPreferenceClickListener(preference -> {
            int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(preference.getContext());

            if (result != ConnectionResult.SUCCESS) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result, getActivity(), REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            } else {
                Intent intent = googleCredentialPreference.newChooseAccountIntent();
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_CHOOSE_ACCOUNT);
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EDMApplication.get(getActivity()).getObjectGraph().inject(this);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (ACTION_RECOVER_AUTH_EXCEPTION.equals(intent.getAction())) {
                Intent toRun = intent.getParcelableExtra(EXTRA_RECOVER_INTENT);
                startActivityForResult(toRun, REQUEST_CODE_AUTHORIZE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_ACCOUNT:
                googleCredentialPreference.onChooseAccountResult(resultCode, data);
                // TODO Only trigger this if the user effectively selected any account, it changed, there wasn't a token, etc.
                if (resultCode == Activity.RESULT_OK) {
                    Schedulers.newThread().createWorker()
                            .schedule(() -> {
                                try {
                                    String token = googleCredentialPreference.getCredential().getToken();
                                    Timber.d("Got %s as a token.", token);
                                } catch (UserRecoverableAuthException recoverableException) {
                                    Intent recoveryIntent = recoverableException.getIntent();
                                    startActivityForResult(recoveryIntent, REQUEST_CODE_AUTHORIZE);
                                } catch (IOException | GoogleAuthException e) {
                                    // TODO Notify the error.
                                    Timber.d(e, "There was an error, man.");
                                }
                            });
                }
                break;
            case REQUEST_CODE_AUTHORIZE:
                if (resultCode == Activity.RESULT_OK) {
                    Timber.d("We got authorized by Youtube!");
                    messages.retryAll(LocalMessage.Status.CANCEL_RECOVERABLE_AUTH_ERROR);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static Intent newRecoverIntent(Context context, UserRecoverableAuthException exception) {
        Intent intent = new Intent(context, PreferenceActivity.class);
        intent.setAction(ACTION_RECOVER_AUTH_EXCEPTION);
        intent.putExtra(EXTRA_RECOVER_INTENT, exception.getIntent());
        return intent;
    }
}