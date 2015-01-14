package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.content.Thread;

public class ThreadListAdapter extends ArrayAdapter<Thread> {

    private int resourceId;
    private int textViewResourceId;

    public ThreadListAdapter(Context context, int resource, int textViewResourceId, List<Thread> objects) {
        super(context, resource, textViewResourceId, objects);

        this.resourceId = resource;
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resourceId, parent, false);
        } else {
            view = convertView;
        }

        TextView subject;

        try {
            subject = (TextView) view.findViewById(textViewResourceId);
        } catch (ClassCastException e) {
            Log.e(getClass().getSimpleName(), "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    getClass().getSimpleName() + " requires the resource ID to be a TextView", e);
        }

        Thread item = getItem(position);

        subject.setText(getItemSubject(item));

        return view;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getThreadId();
    }

    public String getItemSubject(Thread item) {
        Message rootMessage = item.getRootMessage();
        if (rootMessage != null) {
            return rootMessage.getSubject();
        } else {
            return "Message " + item.getThreadId();
        }
    }
}
