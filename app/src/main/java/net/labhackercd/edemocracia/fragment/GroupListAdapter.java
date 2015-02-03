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

import butterknife.ButterKnife;
import butterknife.InjectView;

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(android.R.id.icon) ImageView iconView;
        @InjectView(android.R.id.text1) TextView textView;

        private Group group;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
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
                Intent intent = MainActivity.getIntent(v.getContext(), group);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }
}
