package br.leg.camara.labhacker.edemocracia;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class VideoSourceDialogFragment extends DialogFragment {
    private ListView listView;
    private ListAdapter listAdapter;

    final private AdapterView.OnItemClickListener mOnClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView)parent, v, position, id);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.list_content, container, false);

        listView = (ListView) view.findViewById(android.R.id.list);

        if (listAdapter != null) {
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(mOnClickListener);
        }

        return view;
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    public void onListItemClick(ListView parent, View v, int position, long id) {
    }

    public void setListAdapter(ListAdapter adapter) {
        this.listAdapter = adapter;
        if (adapter != null && listView != null) {
            listView.setAdapter(adapter);
        }
    }
}
