package net.labhackercd.edemocracia.ui.group;

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

import com.amulyakhare.textdrawable.TextDrawable;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.ui.MainActivity;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private final ImageLoader imageLoader;
    private List<Group> groups = Collections.emptyList();

    public GroupListAdapter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public GroupListAdapter replaceWith(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindGroup(groups.get(i));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Group group;

        @InjectView(android.R.id.icon) ImageView iconView;
        @InjectView(android.R.id.text1) TextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
        }

        public void bindGroup(Group group) {
            this.group = group;

            setName(group.getName());

            setImage(group.getName(), group.getGroupId());
        }

        private void setName(String name) {
            textView.setText(name);
        }

        private void setImage(String name, long groupId) {
            String firstLetter = name.trim().substring(0, 1).toUpperCase();
            TextDrawable placeholder = TextDrawable.builder().buildRect(firstLetter, Color.LTGRAY);
            imageLoader.group(groupId)
                    .placeholder(placeholder)
                    .resize(128, 128)
                    .centerCrop()
                    .into(iconView);
        }

        private void handleClick(View v) {
            if (group != null) {
                Context context = v.getContext();
                Intent intent = MainActivity.createIntent(context, group);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }
}
