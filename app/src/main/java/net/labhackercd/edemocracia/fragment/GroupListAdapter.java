package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.ShowForumEvent;
import net.labhackercd.edemocracia.content.Group;
import net.labhackercd.edemocracia.fragment.simplerecyclerview.SimpleRecyclerViewAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class GroupListAdapter extends SimpleRecyclerViewAdapter<Group, GroupListAdapter.ViewHolder> {

    private final Context context;
    private final EventBus eventBus;

    public GroupListAdapter(Context context, EventBus eventBus, List<Group> items) {
        super(items);
        this.context = context;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_list_item, viewGroup, false);
        return new ViewHolder(view, eventBus);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindGroup(getItem(i));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(android.R.id.icon) ImageView iconView;
        @InjectView(android.R.id.text1) TextView textView;

        private Group group;
        private final EventBus eventBus;

        public ViewHolder(View view, EventBus eventBus) {
            super(view);
            this.eventBus = eventBus;
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        public void bindGroup(Group group) {
            this.group = group;

            textView.setText(group.getName());

            String letter = group.getName().trim().substring(0, 1).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

            Picasso.with(context)
                    .load(group.getGroupImage())
                    .placeholder(textDrawable)
                    .resize(128, 128)
                    .centerCrop()
                    .into(iconView);
        }

        @Override
        public void onClick(View v) {
            if (group != null) {
                eventBus.post(new ShowForumEvent(group));
            }
        }
    }
}
