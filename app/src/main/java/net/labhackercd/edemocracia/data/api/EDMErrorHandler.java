package net.labhackercd.edemocracia.data.api;

/**
 * Yeah, this is kinda hard to explain...
 *
 * But hey, don't worry! It should be gone by the next commit.
 */
public class EDMErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(ServiceError error) {
        return error.getCause();
    }

    public static Throwable getCause(Throwable t) {
        return t instanceof ServiceError ? t.getCause() : t;
    }
}
