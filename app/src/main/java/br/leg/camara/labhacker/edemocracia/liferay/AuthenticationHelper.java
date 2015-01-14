package br.leg.camara.labhacker.edemocracia.liferay;

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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationHelper {

    private URL url;

    public AuthenticationHelper(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

    public CookieCredentials authenticate(String username, String password) throws IOException, URISyntaxException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        FormElement form = getLoginForm();

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

        URL loginUrl = new URL(form.attr("action"));
        HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();

        connection.setInstanceFollowRedirects(true);

        RequestsHelper.writeFormData(connection, loginFormData);

        String loginBody = RequestsHelper.readBody(connection);

        String location;
        while (null != (location = connection.getHeaderField("Location"))) {
            connection = (HttpURLConnection) (new URL(location)).openConnection();
            loginBody = RequestsHelper.readBody(connection);
        }

        if (checkIsAuthenticated(loginBody)) {
            return CookieCredentials.fromCookieStore(cookieManager.getCookieStore());
        }

        return null;
    }

    public boolean credentialsAreStillValid(CookieCredentials credentials) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) getURL().toURI().resolve("/").toURL().openConnection();

        credentials.authenticate(connection);

        String body = RequestsHelper.readBody(connection);

        return checkIsAuthenticated(body);
    }

    private FormElement getLoginForm() throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) getURL().openConnection();

        String body = RequestsHelper.readBody(connection);

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
