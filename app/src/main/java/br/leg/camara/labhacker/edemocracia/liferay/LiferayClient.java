package br.leg.camara.labhacker.edemocracia.liferay;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class LiferayClient {

    public static JSONArray getServiceResponse(HttpURLConnection response) throws ServerException, IOException {
        int status = response.getResponseCode();

        if ((status == HttpURLConnection.HTTP_MOVED_PERM) ||
                (status == HttpURLConnection.HTTP_MOVED_TEMP) ||
                (status == HttpURLConnection.HTTP_SEE_OTHER)) {
            throw new RedirectException(response.getHeaderField("Location"));
        }

        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServerException("Unauthorized");
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new ServerException("Request failed with code " + status);
        }

        JSONArray result;

        try {
            String body = readBody(response);

            if (isJSONObject(body)) {
                JSONObject obj = new JSONObject(body);

                if (obj.has("exception")) {
                    throw new ServerException(obj.getString("exception"));
                } else {
                    result = new JSONArray();
                    result.put(obj);
                }
            } else {
                result = new JSONArray(body);
            }
        } catch (JSONException e) {
            throw new ServerException("Invalid response", e);
        }

        return result;
    }

    public static String readBody(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String body = "";

        try {
            String line;
            while (null != (line = reader.readLine())) {
                body += line;
            }
        } finally {
            reader.close();
            connection.disconnect();
        }

        return body;
    }

    public static void writeFormData(HttpURLConnection connection, List<NameValuePair> formData) throws IOException {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
            entity.writeTo(out);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    public static JSONArray invoke(Session session, JSONArray commands) throws IOException, ServerException {
        URL url = buildURL(session, commands);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        session.prepareRequest(connection);

        session.processResponse(connection);

        return LiferayClient.getServiceResponse(connection);
    }

    private static URL buildURL(Session session, JSONArray commands) throws IOException, ServerException {
        List<NameValuePair> params = new ArrayList<>();

        try {
            String token = AuthTokenManager.getAuthenticationToken(session);
            if (token != null) {
                params.add(new BasicNameValuePair("p_auth", token));
            }
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve valid authentication token", e);
        }

        params.add(new BasicNameValuePair("cmd", commands.toString()));

        String queryString = URLEncodedUtils.format(params, "UTF-8");

        URI uri;
        URL url = session.getURL();
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }

        String path = uri.getPath().concat("?").concat(queryString);
        return uri.resolve(path).normalize().toURL();
    }

    private static boolean isJSONObject(String s) {
        return s != null && !s.isEmpty() && s.startsWith("{");
    }
}
