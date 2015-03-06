package net.labhackercd.edemocracia.data.api;

import net.labhackercd.edemocracia.data.api.client.exception.AuthorizationException;

import java.io.IOException;

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

    public static boolean isAuthorizationError(Throwable t) {
        while (t != null) {
            if (t instanceof AuthorizationException)
                return true;
            t = t.getCause();
        }
        return false;
    }

    public static boolean isNetworkError(Throwable t) {
        while (t != null) {
            if (t instanceof IOException)
                return true;
            t = t.getCause();
        }
        return false;
    }
}
