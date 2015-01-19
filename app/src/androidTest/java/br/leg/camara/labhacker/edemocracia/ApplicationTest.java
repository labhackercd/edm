package br.leg.camara.labhacker.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.service.SessionImpl;
import com.liferay.mobile.android.v62.group.GroupService;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import br.leg.camara.labhacker.edemocracia.util.EDMAuthentication;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;

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

    public void testItAll() throws Exception {
        String username = getUsername();
        String password = getPassword();

        Session session = new EDMSession(new BasicAuthentication(username, password));

        GroupService groupService = new GroupService(session);

        JSONArray sites = groupService.getUserSites();

        assertNotNull(sites);
        assertTrue(sites.length() > 0);

        long companyId = sites.getJSONObject(0).getLong("companyId");

        session = new EDMSession(new EDMAuthentication(username, password, companyId));
        groupService = new GroupService(session);

        JSONArray result = groupService.search(getCompanyId(), "%", "%", new JSONArray(), -1, -1);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }
}