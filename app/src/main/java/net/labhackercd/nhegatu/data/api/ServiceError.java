package net.labhackercd.nhegatu.data.api;

public class ServiceError extends RuntimeException {
    private Throwable cause;

    ServiceError(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
