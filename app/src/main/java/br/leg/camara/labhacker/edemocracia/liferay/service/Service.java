package br.leg.camara.labhacker.edemocracia.liferay.service;

import br.leg.camara.labhacker.edemocracia.liferay.Session;

public class Service {
    private Session session;

    public Service(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
