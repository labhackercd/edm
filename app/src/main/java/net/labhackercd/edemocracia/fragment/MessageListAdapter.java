package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.fragment.simplerecyclerview.SimpleRecyclerViewAdapter;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MessageListAdapter extends SimpleRecyclerViewAdapter<Message, MessageListAdapter.ViewHolder> {

    private final Context context;

    public MessageListAdapter(Context context, List<Message> items) {
        super(items);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindMessage(getItem(i));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.body) TextView bodyView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;

        private Message message;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        public void bindMessage(Message message) {
            this.message = message;

            bodyView.setText(message.getBody());
            userView.setText(message.getUserName());
            subjectView.setText(message.getSubject());

            // Set the date
            Date date = message.getCreateDate();

            if (date != null) {
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date));
            }

            // Fill the user portrait
            Uri portrait = message.getUserPortrait();

            if (portrait == null) {
                // Clean up portrait ImageView in case it's filled with someone else's portrait
                portraitView.setImageDrawable(null);
            } else {
                // Load current user's portrait image
                Picasso.with(context)
                        .load(portrait)
                        .resize(100, 100)
                        .centerCrop()
                        .into(portraitView);
            }
        }

        @Override
        public void onClick(View v) {
            if (message != null) {
                // TODO Do something when a message is clicked
            }
        }
    }
}
