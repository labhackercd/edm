package net.labhackercd.nhegatu;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.common.base.Joiner;
import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.nhegatu.data.api.client.EDMSession;
import net.labhackercd.nhegatu.data.api.client.Endpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    public void testSomeStupidCalls() throws Throwable {
        Endpoint endpoint = Endpoint.createFixed("https://edemocracia.camara.gov.br/api/secure/jsonws");
        Authentication authentication = new BasicAuthentication(
                Helper.getProperty("username"), Helper.getProperty("password"));

        EDMSession session = new EDMSession(endpoint, authentication);

        JSONObject params = new JSONObject();

        params.put("companyId", 10131);
        params.put("name", "%");
        params.put("description", "%");
        params.put("params", new ArrayList());
        params.put("start", -1);
        params.put("end", -1);
        params.put("$closed = /expandovalue/get-data", new JSONObject(Joiner.on('\n').join(
                        "       {",
                        "           \"companyId\": 10131,",
                        "           \"className\": \"com.liferay.portal.model.Group\",",
                        "           \"tableName\": \"CUSTOM_FIELDS\",",
                        "           \"columnName\": \"Encerrada\",",
                        "           \"@classPk\": \"$group.groupId\"",
                        "}"
                )));


        JSONObject cmd = new JSONObject();
        cmd.put("$group = /group/search", params);

        JSONArray json = session.invoke(cmd);

        assert json.length() == 1;

        JSONArray result = json.getJSONArray(0);

        //Log.i("->", json.toString());
        for (int i = 0; i < result.length(); i++) {
            Log.i("->", result.getJSONObject(i).toString());
        }

        assert result.length() > 0;

        /*
        EDMBatchSession batchSession = new EDMBatchSession(session);

        CustomService batchService = new CustomService(batchSession);

        String s1 = String.format(Joiner.on('\n').join(
                "{",
                "   \"$group = /group/get-group\": {",
                "       \"groupId\": %d,",
                "       \"$closed = /expandovalue/get-data\": {",
                "           \"companyId\": 10131,",
                "           \"className\": \"com.liferay.portal.model.Group\",",
                "           \"tableName\": \"CUSTOM_FIELDS\",",
                "           \"columnName\": \"Encerrada\",",
                "           \"@classPk\": \"$group.groupId\"",
                "       }",
                "   }",
                "}"
        ), 0);

        batchSession.invoke(new JSONObject(s1));

        for (int i = 0 ; i < result.length(); i++) {
            JSONObject item = result.getJSONObject(i);
            long groupId = item.getLong("groupId");

            String s = String.format(Joiner.on('\n').join(
                    "{",
                    "   \"$group = /group/get-group\": {",
                    "       \"groupId\": %d,",
                    "       \"$closed = /expandovalue/get-data\": {",
                    "           \"companyId\": 10131,",
                    "           \"className\": \"com.liferay.portal.model.Group\",",
                    "           \"tableName\": \"CUSTOM_FIELDS\",",
                    "           \"columnName\": \"Encerrada\",",
                    "           \"@classPk\": \"$group.groupId\"",
                    "       }",
                    "   }",
                    "}"
            ), groupId);

            Log.i("->", s);

            batchSession.invoke(new JSONObject(s));
        }

        JSONArray bresult = batchSession.invoke();

        Log.i("->", bresult.toString());

        assert bresult.length() == result.length();
        */
    }
}