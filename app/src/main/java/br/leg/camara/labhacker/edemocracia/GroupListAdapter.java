package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;

public class GroupListAdapter extends ArrayAdapter<Group> {

    public GroupListAdapter(Context context, int resource, int textViewResourceId, List<Group> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public long getItemId(int position) {
        Group item = getItem(position);
        return item.getGroupId();
    }
}
