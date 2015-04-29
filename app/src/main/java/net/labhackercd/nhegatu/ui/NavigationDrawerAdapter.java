package net.labhackercd.nhegatu.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.preference.PreferenceActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private List<Item> items = Lists.newArrayList(
            Item.create(R.string.title_group_list, R.drawable.ic_forum_black_24dp, view -> {
                Context context = view.getContext();
                Intent intent = MainActivity.createIntent(context);
                return LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }),
            Item.create(R.string.title_preferences, R.drawable.ic_settings_black_24dp, view -> {
                Context context = view.getContext();
                Intent intent = new Intent(context, PreferenceActivity.class);
                context.startActivity(intent);
                return true;
            }),
            Item.create(R.string.title_about, R.drawable.ic_info_black_24dp, view -> {
                // TODO About screen.
                return true;
            })
    );

    private int selectedPosition;
    private SelectionListener selectionListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
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

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setSelectedItem(int position) {
        this.selectedPosition = position;
    }

    public int getSelectedItemPosition() {
        return selectedPosition;
    }

    private void onItemSelected(View view, int position) {
        selectedPosition = position;
        if (selectionListener != null)
            selectionListener.onItemSelected(view, position);
    }

    public interface SelectionListener {
        public void onItemSelected(View view, int position);
    }

    public abstract static class Item {
        public final int icon;
        public final int label;

        public Item(int label, int icon) {
            this.icon = icon;
            this.label = label;
        }

        public abstract boolean onSelected(View view);

        public static Item create(int label, int icon, OnSelectCallback callback) {
            return new Item(label, icon) {
                @Override
                public boolean onSelected(View view) {
                    return callback.onSelect(view);
                }
            };
        }

        public interface OnSelectCallback {
            public boolean onSelect(View view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.icon) ImageView icon;
        @InjectView(R.id.label) TextView label;

        private Item item;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
        }

        public void bindItem(Item item) {
            this.item = item;
            setIcon(item.icon);
            setLabel(item.label);
            setSelected(isSelected());
        }

        private void setSelected(boolean selected) {
            itemView.setSelected(selected);
            // FIXME This is terrible. It should be done through XML styles, attributes or whatever.
            if (selected)
                itemView.setBackgroundColor(Color.LTGRAY);
            else
                itemView.setBackgroundColor(Color.WHITE);
        }

        private boolean isSelected() {
            return getPosition() == selectedPosition;
        }

        private void setIcon(int resId) {
            icon.setImageResource(resId);
        }

        private void setLabel(int resId) {
            label.setText(resId);
        }

        private void handleClick(View view) {
            if (item != null && item.onSelected(view))
                onItemSelected(view, getPosition());
        }
    }
}
