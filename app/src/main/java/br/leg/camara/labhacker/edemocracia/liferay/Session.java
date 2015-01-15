package br.leg.camara.labhacker.edemocracia.liferay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import br.leg.camara.labhacker.edemocracia.liferay.exception.ServerException;

public interface Session {
    public URL getURL();

    public URL getPortalURL();

    public void prepareRequest(HttpURLConnection connection);

    public void processResponse(HttpURLConnection connection) throws IOException;

    public JSONArray invoke(JSONObject command) throws IOException, ServerException;

    public JSONArray invoke(JSONArray commands) throws IOException, ServerException;

    public interface Middleware {
        public void prepareRequest(HttpURLConnection request, Session session);
        public void processResponse(HttpURLConnection response, Session session) throws IOException;
    }
}
