package net.labhackercd.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.v62.expandovalue.ExpandoValueService;
import com.liferay.mobile.android.v62.group.GroupService;

import net.labhackercd.edemocracia.data.api.EDMBatchSession;
import net.labhackercd.edemocracia.data.api.EDMSession;
import net.labhackercd.edemocracia.data.model.Group;
import net.labhackercd.edemocracia.data.model.util.JSONReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

class Helper {
    private static Properties properties = null;

    public static String getProperty(String name) throws IOException {
        return getProperty(name, null);
    }

    public static String getProperty(String name, String defaultValue) throws IOException {
        if (properties == null) {
            ClassLoader loader = java.lang.Thread.currentThread().getContextClassLoader();
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

    public void testExpandoValues() throws Throwable {
        EDMSession session = new EDMSession(new BasicAuthentication(
                Helper.getProperty("username"), Helper.getProperty("password")));

        JSONArray jsonGroups = new GroupService(session).getUserSites();
        List<Group> groups = JSONReader.fromJSON(jsonGroups, Group.JSON_READER);

        EDMBatchSession batchSession = new EDMBatchSession(session);

        for (Group group : groups) {
            JSONObject params = new JSONObject();

            params.put("companyId", group.getCompanyId());
            params.put("className", "com.liferay.portal.model.Group");
            params.put("tableName", "CUSTOM_FIELDS");
            params.put("columnName", "Encerrada");
            params.put("classPk", group.getGroupId());

            JSONObject command = new JSONObject();
            command.put("/expandovalue/get-data.5", params);

            batchSession.invoke(command);
        }

        JSONArray jsonEncerradas = batchSession.invoke();
        assert jsonEncerradas.length() == groups.size();
    }
}