package br.leg.camara.labhacker.edemocracia.tasks;

public class AddMessageQueueSizeEvent {
    public final int size;

    public AddMessageQueueSizeEvent(int size) {
        this.size = size;
    }
}
