package br.leg.camara.labhacker.edemocracia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentUris;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;
import br.leg.camara.labhacker.edemocracia.liferay.Session;


public class MessageListFragment extends ListFragment {

    public static String ARG_GROUP = "group";
    public static String ARG_CATEGORY = "category";
    public static String ARG_THREAD = "thread";

    private int groupId;
    private int categoryId;
    private int threadId;

    private View listView;
    private View progressView;
    private RefreshListTask refreshListTask;
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

        listView = view.findViewById(android.R.id.list);
        progressView = view.findViewById(android.R.id.progress);

        progressView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            refreshList();
        }
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

    private void refreshList() {
        showProgress(true);

        setListItems(new ArrayList<Message>());

        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }

    private void setListItems(List<Message> items) {
        ListAdapter adapter = new MessageListAdapter(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, items);

        setListAdapter(adapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        listView.setVisibility(show ? View.GONE : View.VISIBLE);
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            listView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1);
            progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0);
        }
    }


    public interface OnMessageSelectedListener {
        public void onMessageSelect(Uri uri);
    }


    public class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        List<Message> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            Session session = SessionProvider.getSession(getActivity().getApplication());
            CustomService service = new CustomService(session);

            JSONArray result;
            try {
                result = service.listThreadMessages(groupId, categoryId, threadId);
            } catch (Exception e) {
                // TODO FIXME Notify error
                Log.e(this.getClass().getName(), "Failed to retrieve message list: " + e.toString());
                return false;
            }

            items = new ArrayList<>(result.length());

            for (int i = 0; i < result.length(); i++) {
                try {
                    Message item = Message.fromJSONObject(result.getJSONObject(i));
                    items.add(item);
                } catch (JSONException e) {
                    // FIXME XXX Notify error
                    Log.e(this.getClass().getSimpleName(), "Failed to load message list: " + e.toString());
                    return false;
                }
            }

            Log.v(getClass().getSimpleName(), "Loaded " + items.size() + " items with " + groupId + ", " + categoryId + ", " + threadId);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            refreshListTask = null;
            showProgress(false);

            if (!success) {
                items = new ArrayList<>();
            }

            setListItems(items);
        }

        @Override
        protected void onCancelled() {
            refreshListTask = null;
            showProgress(false);
        }
    }
}
