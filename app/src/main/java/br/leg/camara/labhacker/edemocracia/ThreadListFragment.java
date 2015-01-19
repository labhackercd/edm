package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import org.json.JSONArray;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;


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
        EDMSession session = EDMSession.get(getActivity().getApplicationContext());

        assert session != null;

        MBThreadService service = new MBThreadService(session);

        JSONArray threads = service.getGroupThreads(group.getGroupId(), -1, 0, -1, -1);

        return JSONReader.fromJSON(threads, Thread.JSON_READER);
    }

    public interface OnThreadSelectedListener {
        public void onThreadSelected(Thread thread);
    }
}
