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

    public void testSomeStuff() {
        Time time = new Time();
        time.set(1, 1, 1, 31, 0, 2014);

        time.monthDay += 1;
        time.normalize(true);

        assertEquals(time.month, 1);
        assertEquals(time.monthDay, 1);
    }

    public void testTheShitOutOfIt() throws Exception {
        LiferayClient client = new LiferayClient();

        assertTrue(client.authenticate("dirleyrls@gmail.com", "12345"));

        JSONArray result = client.listGroups(1);

        assertNotNull(result);

        client.setToken(new AuthenticationToken());

        result = client.listGroups(1);

        assertNotNull(result);
    }
}