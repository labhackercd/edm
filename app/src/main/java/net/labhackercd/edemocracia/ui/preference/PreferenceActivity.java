package net.labhackercd.edemocracia.ui.preference;

import android.os.Bundle;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.ui.BaseActivity;

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
