package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.MainActivity;
import net.labhackercd.edemocracia.content.Group;

import java.util.List;

public class GroupListAdapter extends SimpleRecyclerViewAdapter<Group, GroupListAdapter.ViewHolder> {

    private final Context context;

    public GroupListAdapter(Context context, List<Group> items) {
        super(items);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindGroup(getItem(i));
    }

    public class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        private Group group;
        private final TextView textView;
        private final ImageView iconView;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            iconView = (ImageView) view.findViewById(android.R.id.icon);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }

        public void bindGroup(Group group) {
            this.group = group;

            textView.setText(group.getName());

            Picasso.with(context)
                    .load(group.getIconUri())
                    .resize(128, 128)
                    .centerCrop()
                    .into(iconView);
        }

        @Override
        public void onClick(View v) {
            if (group != null) {
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(MainActivity.getIntent(v.getContext(), group));
            }
        }
    }
}
