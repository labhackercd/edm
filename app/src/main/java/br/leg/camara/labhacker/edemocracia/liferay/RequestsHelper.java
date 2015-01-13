package br.leg.camara.labhacker.edemocracia.liferay;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

public class RequestsHelper {
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
}
