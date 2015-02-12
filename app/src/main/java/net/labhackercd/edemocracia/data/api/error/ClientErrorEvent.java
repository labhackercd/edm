package net.labhackercd.edemocracia.data.api.error;

import net.romenor.deathray.ClientError;

import de.greenrobot.event.util.ThrowableFailureEvent;

public class ClientErrorEvent extends ThrowableFailureEvent {
    public ClientErrorEvent(ClientError error) {
        super(error);
    }

    @Override
    public ClientError getThrowable() {
        return (ClientError) super.getThrowable();
    }

    public static ClientErrorEvent get(ThrowableFailureEvent event) {
        return (ClientErrorEvent) event;
    }

    public static boolean isNetworkError(ThrowableFailureEvent event) {
        Throwable throwable = event.getThrowable();
        return throwable instanceof ClientError && ((ClientError) throwable).isNetworkError();
    }

    public static boolean isAuthorizationError(ThrowableFailureEvent event) {
        Throwable throwable = event.getThrowable();
        if (throwable instanceof ClientError
                && isAuthorizationError((ClientError) event.getThrowable())) {
            return true;
        }
        return false;
    }

    public static boolean isAuthorizationError(ClientError clientError) {
        if (clientError.getKind() == ClientError.Kind.HTTP) {
            int statusCode = clientError.getResponse().code();
            return statusCode >= 400 && statusCode < 500;
        }
        String message = clientError.getMessage().toLowerCase().trim();
        return message.matches(".*(please *sign|authenticated *access|authentication *failed).*");
    }
}
