package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Message;

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
                .inflate(R.layout.group_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindMessage(getItem(i));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Message message;

        private final TextView textView;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            textView = (TextView) view.findViewById(android.R.id.text1);
        }

        public void bindMessage(Message message) {
            this.message = message;

            textView.setText(message.getBody());
        }

        @Override
        public void onClick(View v) {
            if (message != null) {
                // TODO Do something when a message is clicked
            }
        }
    }
}
