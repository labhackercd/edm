package net.labhackercd.edemocracia.activity;

import net.labhackercd.edemocracia.content.Thread;

public class ShowThreadEvent {
    private final Thread thread;

    public ShowThreadEvent(Thread thread) {
        this.thread = thread;
    }

    public Thread getThread() {
        return thread;
    }
}
