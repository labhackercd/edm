package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.content.ContentUris;
import android.net.Uri;
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

import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;

public class MessageListFragment extends SimpleListFragment<Message> {

    public static String ARG_GROUP = "group";
    public static String ARG_CATEGORY = "category";
    public static String ARG_THREAD = "thread";

    private int groupId;
    private int categoryId;
    private int threadId;

    private OnMessageSelectedListener listener;

    public static MessageListFragment newInstance(Uri groupUri, Uri categoryUri, Uri threadUri) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_GROUP, groupUri.toString());
        args.putString(ARG_CATEGORY, categoryUri.toString());
        args.putString(ARG_THREAD, threadUri.toString());

        fragment.setArguments(args);

        return fragment;
    }

    public MessageListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            Uri categoryUri = Uri.parse(args.getString(ARG_CATEGORY));
            Uri groupUri = Uri.parse(args.getString(ARG_GROUP));
            Uri threadUri = Uri.parse(args.getString(ARG_THREAD));

            categoryId = (int) ContentUris.parseId(categoryUri);
            groupId = (int) ContentUris.parseId(groupUri);
            threadId = (int) ContentUris.parseId(threadUri);
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
            Uri uri = Content.withAppendedId(Thread.class, id);
            listener.onMessageSelect(uri);
        }
    }

    @Override
    protected List<Message> fetchItems() throws Exception {
        Session session = SessionProvider.getSession(getActivity().getApplication());
        CustomService service = new CustomService(session);

        JSONArray result;
        result = service.listThreadMessages(groupId, categoryId, threadId);

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
        public void onMessageSelect(Uri uri);
    }
}
