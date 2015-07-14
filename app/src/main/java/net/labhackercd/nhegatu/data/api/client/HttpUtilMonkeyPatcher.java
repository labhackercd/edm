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

package net.labhackercd.nhegatu.data.api.client;

import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.Session;

import org.json.JSONArray;

import java.lang.reflect.Field;

/**
 * Deep inside Liferay's mobile SDK for Android there is {@link HttpUtil}, which is used
 * for basically every single request to the service.
 *
 * Inside that little *utility class* there is a hardcoded field called `_JSONWS_PATH`
 * which is appended to every single request to the service.
 *
 * And that sucks.
 *
 * It sucks because since it's a *private final* field, we can't change it's value. I mean,
 * we almost can't.
 *
 * This class basically removes this inconvenience by replacing that field's value with a
 * simple "/". This way, we can set the full URL of the remote service in the Session and
 * this is how it should be since the beginning.
 */
public class HttpUtilMonkeyPatcher {
    private static final Object lock = new Object();
    private static boolean patched = false;

    public static void patch() {
        if (!patched) {
            synchronized (lock) {
                try {
                    Field field = HttpUtil.class.getDeclaredField("_JSONWS_PATH");
                    field.setAccessible(true);
                    field.set(null, "/");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to override _JSONWS_PATH.", e);
                }
                patched = true;
            }
        }
    }

    public static JSONArray post(Session session, JSONArray commands) throws Exception {
        patch();
        return HttpUtil.post(session, commands);
    }
}
