package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;

public class GroupListFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String ARG_GROUPS = "groups";

    private List<String> groups;

    private OnGroupSelectedListener listener;

    private AbsListView listView;

    private ListAdapter adapter;

    public static GroupListFragment newInstance(List<String> groups) {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        args.putCharSequenceArray(ARG_GROUPS, groups.toArray(new CharSequence[groups.size()]));
        fragment.setArguments(args);
        return fragment;
    }

    public GroupListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            CharSequence[] groupsCs = getArguments().getCharSequenceArray(ARG_GROUPS);
            groups = new ArrayList<String>();
            for (CharSequence cs : groupsCs) {
                groups.add(cs.toString());
            }
        }

        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, groups);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grouplist, container, false);

        // Set the adapter
        listView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) listView).setAdapter(adapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

        return view;
    }

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
            Uri groupUri = ContentUris.withAppendedId(Group.CONTENT_URI, id);
            listener.onGroupSelected(groupUri);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = listView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Uri groupUri);
    }

}
