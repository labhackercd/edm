package net.labhackercd.edemocracia.ui.preference;

import android.content.Intent;
import android.os.Bundle;

import net.labhackercd.edemocracia.R;

import timber.log.Timber;

public class PreferenceFragment extends android.preference.PreferenceFragment {
    public static final String PREF_YOUTUBE_ACCOUNT = "youtube_account";

    private GoogleAccountPreference googleCredentialPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("Creating fragment.");

        addPreferencesFromResource(R.xml.preference_screen);

        googleCredentialPreference = (GoogleAccountPreference) findPreference(PREF_YOUTUBE_ACCOUNT);
        googleCredentialPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = googleCredentialPreference.newChooseAccountIntent();
            if (intent != null) {
                startActivityForResult(intent, GoogleAccountPreference.REQUEST_CODE);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GoogleAccountPreference.REQUEST_CODE)
            googleCredentialPreference.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}