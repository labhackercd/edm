package net.labhackercd.edemocracia.ui.thread;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ocpsoft.pretty.time.PrettyTime;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.ui.MainActivity;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class ThreadListAdapter extends RecyclerView.Adapter<ThreadListAdapter.ViewHolder> {

    private final EventBus eventBus;
    private List<ThreadItem> items = Collections.emptyList();

    public ThreadListAdapter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public ThreadListAdapter replaceWith(List<ThreadItem> items) {
        this.items = items;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.thread_list_item, parent, false);
        return new ViewHolder(view, eventBus);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        holder.bindThreadItem(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(android.R.id.text2) TextView titleView;
        @InjectView(R.id.body) TextView bodyView;
        @InjectView(R.id.itemCount) TextView countView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.portrait) ImageView portraitView;
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
                if (item.getThread() != null) {
                    eventBus.post(new MainActivity.ShowThreadEvent(item.getThread()));
                } else {
                    eventBus.post(new MainActivity.ShowCategoryEvent(item.getCategory()));
                }
            }
        }

        public void bindThreadItem(ThreadItem item) {
            this.item = item;

            // Fill the user portrait
            String letter = item.getUserName().trim().substring(0, 1).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

            portraitView.setImageDrawable(textDrawable);

            /*
            TODO Show user portraits
            Uri userPortrait = item.getUserPortrait();
            if (userPortrait == null) {
                portraitView.setImageDrawable(textDrawable);
            } else {
                Picasso.with(context)
                        .load(item.getUserPortrait())
                        .placeholder(textDrawable)
                        .resize(100, 100)
                        .centerCrop()
                        .into(portraitView);
            }
             */

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
                Context context = dateView.getContext();
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date));
            }
        }
    }
}
