package br.leg.camara.labhacker.edemocracia.liferay;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthHelper {

    public static class TokenAndCookies {
        public TokenAndCookies(String token, CookieManager cookieManager) {
            this.token = token;
            this.cookieManager = cookieManager;
        }

        public String getToken() {
            return token;
        }

        /**
         * Please don't modify this. This should really be Immutable,
         * but I don't know how to do that.
         */
        public CookieManager getCookieManager() {
            return this.cookieManager;
        }

        private String token;
        private CookieManager cookieManager;
    }

    public static TokenAndCookies authenticate(String username, String password) throws IOException {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        URL url = new URL("http://192.168.25.9:8080/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        // Where the content of the request will be saved
        String content = "";

        try {
            String line;
            while (null != (line = reader.readLine())) {
                content += line;
            }
        } finally {
            reader.close();
            urlConnection.disconnect();
        }

        Document doc = (Document) Jsoup.parse(content, "UTF-8");
        Element form = doc.getElementsByTag("form").first();

        Elements fields = form.getElementsByAttribute("name");
        ListIterator<Element> it = fields.listIterator();

        List<NameValuePair> formData = new ArrayList<NameValuePair>();

        while (it.hasNext()) {
            Element cur = it.next();
            String name = cur.attr("name");
            String value = cur.attr("value");

            if (name.endsWith("login")) {
                value = username;
            } else if (name.endsWith("password")) {
                value = password;
            } else if (name.endsWith("rememberMe")) {
                value = "true";
            }

            if (value != null && value.length() > 0) {
                formData.add(new BasicNameValuePair(name, value));
            }
        }

        UrlEncodedFormEntity formDataEntity = new UrlEncodedFormEntity(formData);

        URL loginUrl = new URL(form.attr("action"));
        HttpURLConnection loginConnection = (HttpURLConnection) loginUrl.openConnection();

        loginConnection.setRequestMethod("POST");
        loginConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //loginConnection.setRequestProperty("Content-Length", Integer.toString(formDataEntity.toString().length()));
        loginConnection.setUseCaches(false);
        loginConnection.setDoInput(true);
        loginConnection.setDoOutput(true);

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(loginConnection.getOutputStream());
            formDataEntity.writeTo(out);
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

        BufferedReader loginReader = new BufferedReader(new InputStreamReader(loginConnection.getInputStream()));
        String loginContent = "";

        try {
            String line;
            while (null != (line = loginReader.readLine())) {
                loginContent += line;
            }
        } finally {
            loginReader.close();
            loginConnection.disconnect();
        }

        // TODO FIXME Stupid check logic.
        if (!loginContent.contains("/c/portal/logout")) {
            // TODO Auth Error. Probably invalid credentials. Should we throw?
            return null;
        }

        // Extract the token
        Pattern pattern = Pattern.compile("authToken\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(loginContent);

        matcher.find();

        String token = matcher.group(1).toString();

        return new TokenAndCookies(token, cookieManager);
    }
}
