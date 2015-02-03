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
import net.labhackercd.edemocracia.content.Message;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.List;

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

        private final TextView bodyView;
        private final TextView userView;
        private final TextView subjectView;
        private final TextView dateView;
        private final ImageView iconView;

        private Message message;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            bodyView = (TextView) view.findViewById(R.id.body);
            userView = (TextView) view.findViewById(android.R.id.text1);
            subjectView = (TextView) view.findViewById(android.R.id.text2);
            dateView = (TextView) view.findViewById(R.id.date);
            iconView = (ImageView) view.findViewById(android.R.id.icon);
        }

        public void bindMessage(Message message) {
            this.message = message;

            bodyView.setText(message.getBody());
            userView.setText(message.getUserName());
            subjectView.setText(message.getSubject());

            // Set the date
            DateTime date = message.getCreateDate();

            if (date != null) {
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date.toDate()));
            }

            // Fill the user avatar image
            Picasso.with(context)
                    .load(message.getUserAvatarUri())
                    .resize(100, 100)
                    .centerCrop()
                    .into(iconView);
        }

        @Override
        public void onClick(View v) {
            if (message != null) {
                // TODO Do something when a message is clicked
            }
        }
    }
}
