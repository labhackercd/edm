package net.labhackercd.edemocracia.ui.message;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import net.labhackercd.edemocracia.R;

public class StatusView extends TextView {
    private static final int[] STATE_MESSAGE_SUBMISSION_ERROR =
            {R.attr.state_message_submission_error};

    private boolean messageSubmissionError = false;

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(
                attrs, R.styleable.MessageSubmissionState, 0, 0);
        try {
            messageSubmissionError = ta.getBoolean(
                    R.styleable.MessageSubmissionState_state_message_submission_error, false);
        } finally {
             ta.recycle();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (messageSubmissionError) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_MESSAGE_SUBMISSION_ERROR);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public boolean getMessageSubmissionError() {
        return messageSubmissionError;
    }

    public void setMessageSubmissionError(boolean error) {
        if (messageSubmissionError != error)
            messageSubmissionError = error;
        refreshDrawableState();
    }
}
