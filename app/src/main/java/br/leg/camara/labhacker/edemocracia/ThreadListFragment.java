package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;


public class ThreadListFragment extends ContentListFragment<Thread> {

    public static String ARG_PARENT = "parent";

    private Group group;
    private OnThreadSelectedListener listener;

    public static ThreadListFragment newInstance(Group group) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_PARENT, group);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            group = args.getParcelable(ARG_PARENT);
        }
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
            listener = (OnThreadSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnThreadSelectedListener.class.getSimpleName());
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
            listener.onThreadSelected((Thread) getListAdapter().getItem(position));
        }
    }

    @Override
    protected List<Thread> fetchItems() throws Exception {
        Application application = getActivity().getApplication();
        Session session = SessionProvider.getSession(application);
        CustomService service = new CustomService(session);

        JSONArray result;

        result = service.listGroupThreads(group.getId());

        List<Thread> items = new ArrayList<>(result.length());

        for (int i = 0; i < result.length(); i++) {
            try {
                Thread item = Thread.fromJSONObject(result.getJSONObject(i));
                items.add(item);
            } catch (JSONException e) {
                // XXX Silently ignore errors
                Log.w(this.getClass().getSimpleName(), "Failed to load thread list: " + e.toString());
            }
        }

        return items;
    }

    public interface OnThreadSelectedListener {
        public void onThreadSelected(Thread thread);
    }
}
