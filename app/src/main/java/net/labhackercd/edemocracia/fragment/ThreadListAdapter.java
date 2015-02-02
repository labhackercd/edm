package net.labhackercd.edemocracia.fragment;

import android.content.Context;
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
import net.labhackercd.edemocracia.content.Forum;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class ThreadListAdapter extends SimpleRecyclerViewAdapter<ThreadItem, ThreadListAdapter.ViewHolder> {

    private final Context context;

    public ThreadListAdapter(Context context, List<ThreadItem> items) {
        super(items);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.thread_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindThreadItem(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView iconView;
        private final TextView titleView;
        private final TextView bodyView;
        private final TextView countView;
        private final TextView dateView;
        private final TextView userView;

        private ThreadItem item;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            iconView = (ImageView) view.findViewById(android.R.id.icon);
            userView = (TextView) view.findViewById(android.R.id.text1);
            titleView = (TextView) view.findViewById(android.R.id.text2);
            bodyView = (TextView) view.findViewById(R.id.body);
            countView = (TextView) view.findViewById(R.id.itemCount);
            dateView = (TextView) view.findViewById(R.id.date);
        }

        @Override
        public void onClick(View v) {
            if (item != null) {
                Forum forum = item.getForum();
                if (forum != null) {
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(MainActivity.getIntent(context, forum));
                } else {
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(MainActivity.getIntent(context, item.getThread()));
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
            Date date = item.getLastPostDate();
            if (date == null) {
                date = item.getCreateDate();
            }
            if (date != null) {
                // TODO format date to localized dd MMM (21 de jan)
                DateFormat dateFormat = DateFormat
                        .getDateInstance(DateFormat.DATE_FIELD | DateFormat.MONTH_FIELD);

                dateView.setText(dateFormat.format(date));
            }
        }
    }
}
