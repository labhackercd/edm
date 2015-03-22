package net.labhackercd.edemocracia.ui;

import com.google.common.base.Joiner;

public class MimeTypes {
    public static final String GROUP = type("content", "group");
    public static final String THREAD = type("content", "thread");
    public static final String MESSAGE = type("content", "message");
    public static final String CATEGORY = type("content", "category");

    private static final String VND = "vnd.net.labhackercd.edemocracia";

    private static String type(String type, String subtype) {
        return Joiner.on('.').join(VND, Joiner.on('/').join(type, subtype));
    }
}
