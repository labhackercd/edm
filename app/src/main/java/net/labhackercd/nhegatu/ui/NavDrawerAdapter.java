package net.labhackercd.nhegatu.ui;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import net.labhackercd.nhegatu.R;

public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {

    private final DrawerLayout drawer;
    private final List<NavDrawerItem> items;

    public NavDrawerAdapter(DrawerLayout drawer, List<NavDrawerItem> items) {
        this.items = items;
        this.drawer = drawer;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.navdrawer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.icon) ImageView icon;
        @InjectView(R.id.label) TextView label;

        private NavDrawerItem item;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
        }

        public void bindItem(NavDrawerItem item) {
            this.item = item;
            setIcon(item.getIcon());
            setTitle(item.getTitle());
        }

        private void setIcon(int resId) {
            icon.setImageResource(resId);
        }

        private void setTitle(String title) {
            label.setText(title);
        }

        private void handleClick(View view) {
            NavDrawerItem.OnClickListener listener = item.getOnClickListener();
            if (listener != null)
                listener.onClick(drawer, view);
        }
    }
}
