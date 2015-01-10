package br.leg.camara.labhacker.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import br.leg.camara.labhacker.edemocracia.liferay.AuthenticationToken;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

class Helper {
    private static Properties properties = null;

    public static String getProperty(String name) throws IOException {
        return getProperty(name, null);
    }

    public static String getProperty(String name, String defaultValue) throws IOException {
        if (properties == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream("test.properties");
            if (stream == null) {
                throw new IOException("Resource `test.properties` doesn't exist");
            }
            try {
                properties = new Properties();
                properties.load(stream);
            } catch (IOException | NullPointerException e) {
                throw new IOException("Failed to load testing properties: " + e.toString());
            }
        }
        return properties.getProperty(name, defaultValue);
    }
}

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testMethodInvocationWorks() throws Exception {
        String username = Helper.getProperty("username");
        String password = Helper.getProperty("password");

        assertNotNull(username);
        assertNotNull(password);

        LiferayClient client = new LiferayClient();

        Log.v(getClass().getSimpleName(), username + ":" + password);

        assertTrue(client.authenticate(username, password));

        JSONArray result = client.listGroups(1);

        assertNotNull(result);

        client.setToken(new AuthenticationToken());

        result = client.listGroups(1);

        assertNotNull(result);
    }
}