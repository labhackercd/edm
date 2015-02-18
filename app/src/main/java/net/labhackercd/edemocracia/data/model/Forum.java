package net.labhackercd.edemocracia.data.model;

import android.os.Parcelable;

public interface Forum extends Parcelable {
    public long getGroupId();
    public long getCategoryId();
}
