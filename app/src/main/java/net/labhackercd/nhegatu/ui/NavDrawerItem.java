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

    static NavDrawerItem create(final String title, final int icon, final OnClickListener onClickListener) {
        return new NavDrawerItem() {
            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public int getIcon() {
                return icon;
            }

            @Override
            public OnClickListener getOnClickListener() {
                return onClickListener;
            }
        };
    }
}
