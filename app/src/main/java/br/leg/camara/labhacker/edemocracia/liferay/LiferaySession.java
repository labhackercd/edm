package br.leg.camara.labhacker.edemocracia.liferay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class LiferaySession implements Session {

    private final URL url;
    private final CookieCredentials credentials;

    public LiferaySession(URL url, CookieCredentials credentials) {
        this.url = url;
        this.credentials = credentials;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public URL getPortalURL() {
        try {
            return url.toURI().resolve("/").toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareRequest(HttpURLConnection connection) {
        if (credentials != null) {
            credentials.prepareRequest(connection, this);
        }
    }

    @Override
    public void processResponse(HttpURLConnection connection) throws IOException {
        if (credentials != null) {
            credentials.processResponse(connection, this);
        }
    }

    @Override
    public JSONArray invoke(JSONObject command) throws IOException, ServerException {
        JSONArray batch = new JSONArray();

        batch.put(command);

        try {
            return invoke(batch).getJSONArray(0);
        } catch (JSONException e) {
            // FIXME Use a more specific Exception
            throw new ServerException("Unexpected response type", e);
        }
    }

    @Override
    public JSONArray invoke(JSONArray commands) throws IOException, ServerException {
        return LiferayClient.invoke(this, commands);
    }
}
