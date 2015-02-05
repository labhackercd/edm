package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.ShowForumEvent;
import net.labhackercd.edemocracia.activity.ShowThreadEvent;
import net.labhackercd.edemocracia.content.Forum;
import net.labhackercd.edemocracia.fragment.simplerecyclerview.SimpleRecyclerViewAdapter;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class ThreadListAdapter extends SimpleRecyclerViewAdapter<ThreadItem, ThreadListAdapter.ViewHolder> {

    private final Context context;
    private final EventBus eventBus;

    public ThreadListAdapter(Context context, EventBus eventBus, List<ThreadItem> items) {
        super(items);
        this.context = context;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.thread_list_item, parent, false);
        return new ViewHolder(view, eventBus);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindThreadItem(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(android.R.id.icon) ImageView iconView;
        @InjectView(android.R.id.text2) TextView titleView;
        @InjectView(R.id.body) TextView bodyView;
        @InjectView(R.id.itemCount) TextView countView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(android.R.id.text1) TextView userView;

        private ThreadItem item;
        private final EventBus eventBus;

        public ViewHolder(View view, EventBus eventBus) {
            super(view);
            this.eventBus = eventBus;
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (item != null) {
                Forum forum = item.getForum();
                if (forum != null) {
                    eventBus.post(new ShowForumEvent(forum));
                } else {
                    eventBus.post(new ShowThreadEvent(item.getThread()));
                }
            }
        }

        public void bindThreadItem(ThreadItem item) {
            this.item = item;

            // Fill the icon
            Picasso.with(context)
                    .load(item.getIconUri())
                    .resize(100, 100)
                    .centerCrop()
                    .into(iconView);

            // Fill the user name
            userView.setText(item.getUserName());

            // Fill the main text view with the item title
            titleView.setText(item.toString());

            // Fill the other text view with the item content, if available
            String body = item.getBody();
            if (body != null) {
                bodyView.setText(body.replaceAll("\\n+", " "));
            }


            // Fill the item count field
            int itemCount = item.getItemCount();
            if (itemCount > 0) {
                countView.setText(Integer.toString(itemCount));
            }

            // Fill the date view if any date is available
            DateTime date = item.getLastPostDate();

            if (date == null) {
                date = item.getCreateDate();
            }

            if (date != null) {
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date.toDate()));
            }
        }
    }
}
