package br.leg.camara.labhacker.edemocracia;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.text.format.Time;

import org.json.JSONArray;

import br.leg.camara.labhacker.edemocracia.liferay.AuthenticationToken;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testMethodInvocationWorks() throws Exception {
        LiferayClient client = new LiferayClient();

        assertTrue(client.authenticate("dirleyrls@gmail.com", "12345"));

        JSONArray result = client.listGroups(1);

        assertNotNull(result);

        client.setToken(new AuthenticationToken());

        result = client.listGroups(1);

        assertNotNull(result);
    }
}