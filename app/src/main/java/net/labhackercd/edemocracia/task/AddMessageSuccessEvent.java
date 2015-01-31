package net.labhackercd.edemocracia.task;

import net.labhackercd.edemocracia.content.Message;

public class AddMessageSuccessEvent {
    private Message message;

    public AddMessageSuccessEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
