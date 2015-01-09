package br.leg.camara.labhacker.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.service.SessionImpl;
import com.liferay.mobile.android.v62.group.GroupService;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.leg.camara.labhacker.edemocracia.liferay.AuthHelper;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testTheShitOutOfIt() throws Exception {
        LiferayClient client = new LiferayClient();

        assertTrue(client.authenticate("dirleyrls@gmail.com", "12345"));

        JSONArray result = client.listGroups(1);

        Log.v("----->", result.toString());
    }
}