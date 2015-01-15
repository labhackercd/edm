package br.leg.camara.labhacker.edemocracia.liferay;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationHelper {

    public static CookieCredentials authenticate(URL loginUrl, String username, String password) throws IOException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        FormElement form = getLoginForm(loginUrl);

        List<NameValuePair> loginFormData = getLoginFormData(form);

        for (NameValuePair item : loginFormData) {
            String name = item.getName();
            if (name.endsWith("login")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, username));
            } else if (name.endsWith("password")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, password));
            } else if (name.endsWith("rememberMe")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, "true"));
            }
        }

        loginUrl = new URL(form.attr("action"));
        HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();

        connection.setInstanceFollowRedirects(true);

        LiferayClient.writeFormData(connection, loginFormData);

        String loginBody = LiferayClient.readBody(connection);

        String location;
        while (null != (location = connection.getHeaderField("Location"))) {
            connection = (HttpURLConnection) (new URL(location)).openConnection();
            loginBody = LiferayClient.readBody(connection);
        }

        if (!checkIsAuthenticated(loginBody)) {
            Log.e(">", "rofl?");
            return null;
        }

        return CookieCredentials.fromCookieStore(cookieManager.getCookieStore());
    }

    public static boolean isAuthenticated(Session session) throws IOException {
        if (session == null) {
            return false;
        }

        HttpURLConnection connection = (HttpURLConnection) session.getPortalURL().openConnection();

        String body = LiferayClient.readBody(connection);

        return checkIsAuthenticated(body);
    }

    private static FormElement getLoginForm(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String body = LiferayClient.readBody(connection);

        Document document = Jsoup.parse(body, "UTF-8");

        return (FormElement) document.select("#p_p_id_58_").first().select("form").first();
    }

    private static boolean checkIsAuthenticated(String body) {
        // FIXME Implement a proper logic to check if the user is authenticated
        return body.contains("/c/portal/logout");
    }

    private static List<NameValuePair> getLoginFormData(FormElement form) {
        List<NameValuePair> formData = new ArrayList<>();

        for (Connection.KeyVal item : form.formData()) {
            formData.add(new BasicNameValuePair(item.key(), item.value()));
        }

        return formData;
    }
}
