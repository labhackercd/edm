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
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.task.callback.AsyncTaskCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EDMBatchSession implements Session {
    private final Session session;
    private final Object lock = new Object();
    private final List<JSONObject> commands = new ArrayList<>();

    public EDMBatchSession(Session session) {
        this.session = session;
    }

    @Override
    public Authentication getAuthentication() {
        return session.getAuthentication();
    }

    @Override
    public AsyncTaskCallback getCallback() {
        return null;
    }

    @Override
    public int getConnectionTimeout() {
        return session.getConnectionTimeout();
    }

    @Override
    public String getServer() {
        return session.getServer();
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        synchronized (lock) {
            commands.add(command);
        }
        return null;
    }

    public JSONArray invoke() throws Exception {
        if (this.commands.size() == 0) {
            return null;
        }

        JSONArray commands;

        synchronized (lock) {
            commands = new JSONArray(this.commands);
            try {
                return HttpUtilMonkeyPatcher.post(this, commands);
            } catch (ServerException e) {
                throw EDMSession.handleException(e);
            } finally {
                this.commands.clear();
            }
        }
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCallback(AsyncTaskCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setServer(String server) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncTask upload(JSONObject command) throws Exception {
        throw new UnsupportedOperationException();
    }
}
