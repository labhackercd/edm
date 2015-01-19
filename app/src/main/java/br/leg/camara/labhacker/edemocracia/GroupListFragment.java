package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;


public class GroupListFragment extends ContentListFragment<Group> {

    private OnGroupSelectedListener listener;

    @Override
    protected List<Group> fetchItems() throws Exception {
        Session session = SessionProvider.getSession(getActivity().getApplication());
        CustomService service = new CustomService(session);

        JSONArray result = service.listGroups(SessionProvider.DEFAULT_COMPANY_ID);

        /* TODO Filter
        if (!group.isActive() || group.getType() != 1) {
            continue;
        }
        */

        return JSONReader.fromJSON(result, Group.JSON_READER);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        setContentView(view);

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            listener.onGroupSelected((Group) getListAdapter().getItem(position));
        }
    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Group group);
    }
}
