package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Message;

public class MessageListAdapter extends ArrayAdapter<Message> {

    public MessageListAdapter(Context context, int resource, int textViewResourceId, List<Message> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getMessageId();
    }
}
