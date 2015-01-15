package br.leg.camara.labhacker.edemocracia.liferay.exception;

public class ServerException extends Exception {
    public ServerException() {
        super();
    }

    public ServerException(String msg) {
        super(msg);
    }

    public ServerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }
}
