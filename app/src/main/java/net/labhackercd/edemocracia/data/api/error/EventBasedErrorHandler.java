package net.labhackercd.edemocracia.data.api.error;

import net.romenor.deathray.ClientError;
import net.romenor.deathray.ErrorHandler;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.ThrowableFailureEvent;

public class EventBasedErrorHandler implements ErrorHandler {
    private final EventBus eventBus;

    public EventBasedErrorHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public ClientError handleError(ClientError error) {
        eventBus.post(throwableFailureEvent(error));
        return error;
    }

    private ThrowableFailureEvent throwableFailureEvent(ClientError error) {
        return new ThrowableFailureEvent(error);
    }
}
