package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Group;
import net.labhackercd.edemocracia.util.SimpleArrayAdapter;

import java.util.List;

public class GroupListAdapter extends SimpleArrayAdapter<Group> {
    public GroupListAdapter(Context context, List<Group> objects) {
        super(context, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.group_list_item, parent, false);

        ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        TextView titleView = (TextView) view.findViewById(android.R.id.text1);

        Group group = getItem(position);

        Picasso.with(getContext())
                .load(group.getIconUri())
                .resize(128, 128)
                .centerCrop()
                .into(iconView);

        titleView.setText(group.getName());

        return view;
    }
}
