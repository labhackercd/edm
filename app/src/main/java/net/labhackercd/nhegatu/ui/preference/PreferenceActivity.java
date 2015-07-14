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
