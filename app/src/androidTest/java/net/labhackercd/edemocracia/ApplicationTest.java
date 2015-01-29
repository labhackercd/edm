package net.labhackercd.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.group.GroupService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.labhackercd.edemocracia.content.*;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.util.EDMAuthentication;
import net.labhackercd.edemocracia.util.EDMSession;
import net.labhackercd.edemocracia.util.EDMGetSessionWrapper;

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

         JSONArray groupsJson = groupService.search(getCompanyId(), "%", "%", new JSONArray(), -1, -1);

        assertNotNull(groupsJson);
        assertTrue(groupsJson.length() > 0);

        Group group = Group.JSON_READER.fromJSON(groupsJson.getJSONObject(0));

        JSONArray threadsJson = new MBThreadService(session)
                .getGroupThreads(group.getGroupId(), -1, 0, -1, -1);

        assertNotNull(threadsJson);
        assertTrue(threadsJson.length() > 0);

        net.labhackercd.edemocracia.content.Thread thread =
                Thread.JSON_READER.fromJSON(threadsJson.getJSONObject(0));

        Session wrappedSession = new EDMGetSessionWrapper(session);

        JSONObject serviceContextJson = new JSONObject();

        serviceContextJson.put("addGuestPermissions", "true");

        JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                "com.liferay.portal.service.ServiceContext", serviceContextJson);

        JSONObject messageJson = new MBMessageService(wrappedSession)
                .addMessage(thread.getGroupId(), thread.getCategoryId(), thread.getThreadId(), thread.getRootMessageId(),
                //.addMessage(28402, 182302, 182403, 182402,
                        "ASSUNTO", "CORPO", "bbcode", new JSONArray(), false, 0, true, serviceContext);

        Message message = Message.JSON_READER.fromJSON(messageJson);
    }
}