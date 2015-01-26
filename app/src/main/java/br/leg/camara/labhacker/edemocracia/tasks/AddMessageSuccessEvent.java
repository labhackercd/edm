package br.leg.camara.labhacker.edemocracia.tasks;

import br.leg.camara.labhacker.edemocracia.content.Message;

public class AddMessageSuccessEvent {
    private Message message;

    public AddMessageSuccessEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
