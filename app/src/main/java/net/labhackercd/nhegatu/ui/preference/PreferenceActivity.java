package net.labhackercd.nhegatu.ui.preference;

import android.os.Bundle;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.BaseActivity;

public class PreferenceActivity extends BaseActivity {
    public static final String ACTION_SET_YOUTUBE_ACCOUNT = "setUpYouTubeAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIXME Do we really need a layout just for this?
        setContentView(R.layout.preference_activity);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new PreferenceFragment())
                .commit();
    }
}
