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

import android.os.AsyncTask;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.task.callback.AsyncTaskCallback;

import org.json.JSONArray;
import org.json.JSONObject;

class SessionImpl implements Session {
    private final String baseUrl;
    private final Authentication authentication;

    SessionImpl(String baseUrl, Authentication authentication) {
        this.baseUrl = baseUrl;
        this.authentication = authentication;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public AsyncTaskCallback getCallback() {
        return null;
    }

    @Override
    public int getConnectionTimeout() {
        return 1500;
    }

    @Override
    public String getServer() {
        return baseUrl;
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        return PathlessHttpUtil.post(this, command);
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        throw new UnsupportedOperationException("authentication is final.");
    }

    @Override
    public void setCallback(AsyncTaskCallback callback) {
        throw new UnsupportedOperationException("Asynchronous calls are not supported.");
    }

    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        throw new UnsupportedOperationException("connectionTimeout is final.");
    }

    @Override
    public void setServer(String server) {
        throw new UnsupportedOperationException("server is final.");
    }

    @Override
    public AsyncTask upload(JSONObject command) throws Exception {
        throw new UnsupportedOperationException("Uploads are not supported.");
    }
}
