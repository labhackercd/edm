package br.leg.camara.labhacker.edemocracia.liferay;

public class Service {
    private Session session;

    public Service(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
