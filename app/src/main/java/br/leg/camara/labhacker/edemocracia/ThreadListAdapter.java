package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Thread;

public class ThreadListAdapter extends ArrayAdapter<Thread> {

    public ThreadListAdapter(Context context, int resource, int textViewResourceId, List<Thread> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getThreadId();
    }
}
