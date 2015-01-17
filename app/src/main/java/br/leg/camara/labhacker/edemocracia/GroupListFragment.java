package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;


public class GroupListFragment extends SimpleListFragment<Group> {

    private OnGroupSelectedListener listener;

    public GroupListFragment() {
        // Required empty constructor
    }

    @Override
    protected List<Group> fetchItems() throws Exception {
        Session session = SessionProvider.getSession(getActivity().getApplication());
        CustomService service = new CustomService(session);

        JSONArray result = service.listGroups(SessionProvider.DEFAULT_COMPANY_ID);

        List<Group> items = new ArrayList<>(result.length());

        for (int i = 0; i < result.length(); i++) {
            Group group = Group.fromJSONObject(result.getJSONObject(i));

            // Ignore non public (type != 1) or inactive (active != true) groups
            // FIXME We should probably place this filter at some other layer.
            if (!group.isActive() || group.getType() != 1) {
                continue;
            }

            items.add(group);
        }

        return items;
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
            Uri groupUri = Content.withAppendedId(Group.class, id);
            listener.onGroupSelected(groupUri);
        }
    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Uri uri);
    }
}
