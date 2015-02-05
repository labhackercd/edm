package net.labhackercd.edemocracia.activity;

import net.labhackercd.edemocracia.content.Forum;

public class ShowForumEvent {
    private final Forum forum;

    public ShowForumEvent(Forum forum) {
        this.forum = forum;
    }

    public Forum getForum() {
        return forum;
    }
}
