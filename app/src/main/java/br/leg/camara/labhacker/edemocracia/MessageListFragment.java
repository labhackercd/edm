package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
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

import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;

public class MessageListFragment extends ContentListFragment<Message> {

    public static String ARG_THREAD = "thread";

    private Thread thread;

    private OnMessageSelectedListener listener;

    public static MessageListFragment newInstance(Thread thread) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_THREAD, thread);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            thread = args.getParcelable(ARG_THREAD);
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
            listener = (OnMessageSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnMessageSelectedListener.class.getSimpleName());
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
            listener.onMessageSelect((Message) getListAdapter().getItem(position));
        }
    }

    @Override
    protected List<Message> fetchItems() throws Exception {
        Session session = SessionProvider.getSession(getActivity().getApplication());
        CustomService service = new CustomService(session);

        JSONArray result;
        result = service.listThreadMessages(thread.getGroupId(), thread.getCategoryId(), thread.getThreadId());

        List<Message> items = new ArrayList<>(result.length());

        for (int i = 0; i < result.length(); i++) {
            try {
                Message item = Message.fromJSONObject(result.getJSONObject(i));
                items.add(item);
            } catch (JSONException e) {
                // XXX Silently ignore errors
                Log.w(this.getClass().getSimpleName(), "Failed to load message: " + e.toString());
            }
        }

        return items;
    }

    public interface OnMessageSelectedListener {
        public void onMessageSelect(Message message);
    }
}
