package net.labhackercd.edemocracia.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.v62.group.GroupService;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.List;

import javax.annotation.Nullable;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.SessionProvider;
import net.labhackercd.edemocracia.content.Group;
import net.labhackercd.edemocracia.util.EDMSession;
import net.labhackercd.edemocracia.util.SimpleArrayAdapter;
import net.labhackercd.edemocracia.util.SimpleListFragment;
import net.labhackercd.edemocracia.util.JSONReader;

public class GroupListFragment extends SimpleListFragment<Group> {

    private OnGroupSelectedListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnGroupSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnGroupSelectedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            listener.onGroupSelected((Group) getListAdapter().getItem(position));
        }
    }

    @NonNull
    @Override
    protected ListAdapter createAdapter(Context context, List<Group> items) {
        return new GroupListAdapter(context, items);
    }

    @Override
    protected List<Group> fetchItems() throws Exception {
        EDMSession session = ((SessionProvider) getActivity()).getSession();

        GroupService groupService = new GroupService(session);

        JSONArray jsonGroups = groupService.search(
                session.getCompanyId(), "%", "%", new JSONArray(), -1, -1);

        List<Group> groups = JSONReader.fromJSON(jsonGroups, Group.JSON_READER);

        return Lists.newArrayList(Collections2.filter(groups, new Predicate<Group>() {
            @Override
            public boolean apply(@Nullable Group group) {
                return group != null && group.isActive() && (group.getType() == 1 || group.getType() == 3);
            }
        }));
    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Group group);
    }

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
}
