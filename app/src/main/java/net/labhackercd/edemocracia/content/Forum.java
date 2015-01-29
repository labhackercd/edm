package net.labhackercd.edemocracia.content;

import android.os.Parcelable;

/**
 * @author Dirley Rodrigues
 */
public interface Forum extends Parcelable {
    public long getGroupId();
    public long getCategoryId();
}
