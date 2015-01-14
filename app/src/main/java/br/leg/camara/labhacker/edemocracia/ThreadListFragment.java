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

import br.leg.camara.labhacker.edemocracia.content.Category;
import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;
import android.support.v4.widget.SwipeRefreshLayout;


public class ThreadListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static String ARG_PARENT = "parent";


    private RefreshListTask refreshListTask;
    private SwipeRefreshLayout refreshLayout;
    private OnThreadSelectedListener listener;
    private int parentContentId;
    private Class parentContentClass;


    public static ThreadListFragment newInstance(Uri groupUri) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARENT, groupUri.toString());

        fragment.setArguments(args);

        return fragment;
    }

    public ThreadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Uri parentUri = Uri.parse(getArguments().getString(ARG_PARENT));

            // FIXME We will support other kinds of parents later on
            setParentContent(Group.class, ContentUris.parseId(parentUri));
        }
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
            refreshGroupList();
        }
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
            Thread item = (Thread) getListAdapter().getItem(position);

            Uri groupUri = Content.withAppendedId(Group.class, item.getGroupId());
            Uri categoryUri = Content.withAppendedId(Category.class, item.getCategoryId());
            Uri threadUri = Content.withAppendedId(Thread.class, item.getThreadId());

            listener.onThreadSelected(groupUri, categoryUri, threadUri);
        }
    }

    protected void setParentContent(Class cls, long id) {
        parentContentId = (int) id;
        parentContentClass = cls;
    }

    protected Class getParentContentClass() {
        return parentContentClass;
    }

    protected int getParentContentId() {
        return parentContentId;
    }

    private void refreshGroupList() {

        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
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

    @Override public void onRefresh() {
        refreshGroupList();
        hideSwipeProgress();
    }





    public interface OnThreadSelectedListener {
        public void onThreadSelected(Uri groupUri, Uri categoryUri, Uri threadUri);
    }


    public class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        List<Thread> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            LiferayClient client = ((Application) getActivity().getApplication()).getLiferayClient();

            JSONArray result;
            try {
                result = client.listGroupThreads(getParentContentId());
            } catch (Exception e) {
                // TODO FIXME Notify error
                Log.e(this.getClass().getName(), "Failed to retrieve thread list: " + e.toString());
                return false;
            }

            items = new ArrayList<>(result.length());

            for (int i = 0; i < result.length(); i++) {
                try {
                    Thread item = Thread.fromJSONObject(result.getJSONObject(i));
                    items.add(item);
                } catch (JSONException e) {
                    // FIXME XXX Notify error
                    Log.e(this.getClass().getSimpleName(), "Failed to load thread list: " + e.toString());
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            refreshListTask = null;

            if (!success) {
                items = new ArrayList<>();
            }

            ListAdapter adapter = new ThreadListAdapter(getActivity(),
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
