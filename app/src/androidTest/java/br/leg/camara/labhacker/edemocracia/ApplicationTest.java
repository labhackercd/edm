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

    private String getUsername() throws IOException {
        return Helper.getProperty("username");
    }

    private String getPassword() throws IOException {
        return Helper.getProperty("password");
    }

    private int getCompanyId() throws IOException {
        return Integer.parseInt(Helper.getProperty("test.companyId"));
    }

    public void testListGroups() throws Exception {
        String username = getUsername();
        String password = getPassword();

        assertNotNull(username);
        assertNotNull(password);

        LiferayClient client = new LiferayClient();

        assertTrue(client.authenticate(username, password));

        assertTrue(client.authenticate(getUsername(), getPassword()));

        JSONArray result = client.listGroups(getCompanyId());

        assertNotNull(result);

        if (!(result.length() > 0)) {
            throw new AssertionError("result length should be greater than 0");
        }
    }
}