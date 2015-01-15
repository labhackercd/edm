package br.leg.camara.labhacker.edemocracia.liferay.exception;

public class RedirectException extends ServerException {
    public RedirectException(String url) {
        super("The requested URL has moved to " + url);
        this.url = url;
    }

    public String getURL() {
        return this.url;
    }

    private String url;
}
