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
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.ui.MainActivity;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private List<Group> groups = Collections.emptyList();

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(android.R.id.icon) ImageView iconView;
        @InjectView(android.R.id.text1) TextView textView;

        private Group group;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
        }

        public void bindGroup(Group group) {
            this.group = group;

            textView.setText(group.getName());

            String letter = group.getName().trim().substring(0, 1).toUpperCase();

            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

            iconView.setImageDrawable(textDrawable);

            /*
            TODO Display group icons
            Picasso.with(context)
                    .load(group.getGroupImage())
                    .placeholder(textDrawable)
                    .resize(128, 128)
                    .centerCrop()
                    .into(iconView);
                    */
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
