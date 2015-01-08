package br.leg.camara.labhacker.edemocracia;

import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class Application extends android.app.Application {
    private LiferayClient liferayClient;

    public LiferayClient getLiferayClient() {
        if (this.liferayClient == null) {
            // TODO Try to recover our last LiferayClient from some persistent storage
        }
        return this.liferayClient;
    }

    public void setLiferayClient(LiferayClient liferayClient) {
        this.liferayClient = liferayClient;
    }
}
