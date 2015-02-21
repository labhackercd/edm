package net.labhackercd.edemocracia;

import android.test.ApplicationTestCase;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.labhackercd.edemocracia.data.model.Group;
import net.labhackercd.edemocracia.data.api.GroupService;
import net.romenor.deathray.LiferayAdapter;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

public class ApplicationTest extends ApplicationTestCase<EDMApplication> {
    private static final String TAG = ApplicationTest.class.getSimpleName();

    public ApplicationTest() {
        super(EDMApplication.class);
    }

    private OkHttpClient getClient() throws IOException {
        final OkHttpClient client = new OkHttpClient();

        final String userName = Helper.getProperty("username");
        final String password = Helper.getProperty("password");

        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(userName, password);
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        return client;
    }

    private LiferayAdapter getLiferayAdapter() throws IOException {
        return new LiferayAdapter.Builder()
                .setEndpoint("https://edemocracia.camara.gov.br/api/secure/jsonws")
                .setClient(getClient())
                .build();
    }

    public void testNewClient() throws Throwable {
        GroupService service = getLiferayAdapter().create(GroupService.class);

        List<Group> groups = service.getUserSites();

        assert groups.size() > 0;
    }
}