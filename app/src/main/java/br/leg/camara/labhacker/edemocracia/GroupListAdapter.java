package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;

public class GroupListAdapter extends ArrayAdapter<Group> {

    private int resourceId;
    private int nameTextId;

    public GroupListAdapter(Context context, int resource, int nameTextId, List<Group> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.nameTextId = nameTextId;
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

        TextView name;

        try {
            name = (TextView) view.findViewById(nameTextId);
        } catch (ClassCastException e) {
            Log.e(getClass().getSimpleName(), "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    getClass().getSimpleName() + " requires the resource ID to be a TextView", e);
        }

        Group item = getItem(position);

        name.setText(getItemName(item));

        return view;
    }

    @Override
    public long getItemId(int position) {
        Group item = getItem(position);
        return item.getGroupId();
    }

    public String getItemName(Group item) {
        return item.getName();
    }
}
