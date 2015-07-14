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

import android.app.Application;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.group.GroupListFragment;
import net.labhackercd.nhegatu.ui.message.ComposeActivity;
import net.labhackercd.nhegatu.ui.message.MessageListFragment;
import net.labhackercd.nhegatu.ui.preference.PreferenceActivity;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.ui.thread.CategoryThreadListFragment;
import net.labhackercd.nhegatu.ui.thread.GroupThreadListFragment;
import net.labhackercd.nhegatu.ui.thread.ThreadListFragment;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.io.InputStream;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                MainActivity.class,
                ComposeActivity.class,
                GroupListFragment.class,
                PreferenceFragment.class,
                PreferenceActivity.class,
                ThreadListFragment.class,
                MessageListFragment.class,
                GroupThreadListFragment.class,
                CategoryThreadListFragment.class
        },
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class UiModule {
    @Provides @Singleton
    TextProcessor provideTextProcessor(Application application) {
        InputStream input = null;
        try {
            input = application.getResources().openRawResource(R.raw.kefirbb);
            return BBProcessorFactory.getInstance().create(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable e) {
                    // Ignore.
                }
            }
        }
    }
}