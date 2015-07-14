/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.ui.message;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import net.labhackercd.nhegatu.R;

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
        if (messageSubmissionError != error) {
            messageSubmissionError = error;
            refreshDrawableState();
        }
    }
}
