package net.labhackercd.edemocracia.ui.message;

import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AlignmentSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class MessageTagHandler extends AttributeTagHandler {
    private final List<Span> spanStack = new ArrayList<>();

    private static class Span {}
    private static final class Center extends Span {}

    @Override
    protected void handleTag(boolean opening, String tag, Map<String, String> attrs, Editable output) {
        if ("span".equalsIgnoreCase(tag)) {
            if (opening) {
                Span mark = null;
                if ("center".equalsIgnoreCase(attrs.get("class"))) {
                    mark = new Center();
                    handleP(output);
                    start(output, mark);
                }
                spanStack.add(mark);
            } else {
                // Remove.
                Object repl = null;
                Class<? extends Span> kind = Span.class;
                Span span = spanStack.remove(spanStack.size() - 1);
                if (span instanceof Center) {
                    handleP(output);
                    kind = Center.class;
                    repl = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
                }
                end(output, kind, repl);
            }
        }
    }

    private static void handleP(Editable text) {
        int len = text.length();

        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static void start(Editable output, Object mark) {
        int len = output.length();
        output.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(Editable text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len && repl != null) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }
}
