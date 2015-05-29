package net.labhackercd.nhegatu.ui;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

public interface NavDrawerItem {

    interface OnClickListener {
        void onClick(DrawerLayout drawer, View view);
    }

    String getTitle();
    int getIcon();
    OnClickListener getOnClickListener();
}