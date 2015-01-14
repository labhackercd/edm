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
import android.support.v4.widget.SwipeRefreshLayout;
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
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;


public class MessageListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static String ARG_GROUP = "group";
    private SwipeRefreshLayout refreshLayout;
    public static String ARG_CATEGORY = "category";
    public static String ARG_THREAD = "thread";

    private int groupId;
    private int categoryId;
    private int threadId;

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
            Uri groupUri = Uri.parse(args.getString(ARG_GROUP));
            Uri categoryUri = Uri.parse(args.getString(ARG_CATEGORY));
            Uri threadUri = Uri.parse(args.getString(ARG_THREAD));

            groupId = (int) ContentUris.parseId(groupUri);
            categoryId = (int) ContentUris.parseId(categoryUri);
            threadId = (int) ContentUris.parseId(threadUri);
        }
    }


    private void setAppearance() {
        refreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void showSwipeProgress() {
        refreshLayout.setRefreshing(true);
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void hideSwipeProgress() {
        refreshLayout.setRefreshing(false);
    }

    /**
     * It must be overriden by parent classes if manual swipe is enabled.
     */
    @Override
    public void onRefresh() {
        refreshList();
        hideSwipeProgress();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_list_fragment, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            refreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
            refreshLayout.setOnRefreshListener(this);
            setAppearance();
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
        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }




    public interface OnMessageSelectedListener {
        public void onMessageSelect(Uri uri);
    }


    public class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        List<Message> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            LiferayClient client = ((Application) getActivity().getApplication()).getLiferayClient();

            JSONArray result;
            try {
                result = client.listThreadMessages(groupId, categoryId, threadId);
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


            if (!success) {
                items = new ArrayList<>();
            }

            ListAdapter adapter = new MessageListAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, items);

            setListAdapter(adapter);
        }

        @Override
        protected void onCancelled() {
            refreshListTask = null;
            setListAdapter(null);

        }
    }
}
