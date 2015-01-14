package br.leg.camara.labhacker.edemocracia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ListFragment;
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
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;
import android.support.v4.widget.SwipeRefreshLayout;


public class GroupListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private RefreshListTask refreshListTask;
    private SwipeRefreshLayout refreshLayout;
    private OnGroupSelectedListener listener;


    public GroupListFragment() {
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
    @Override public void onRefresh() {
      refreshGroupList();
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
            refreshGroupList();
        }
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

    private void refreshGroupList() {
        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }


    public interface OnGroupSelectedListener {
        public void onGroupSelected(Uri uri);
    }


    public class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        List<Group> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            LiferayClient client = ((Application) getActivity().getApplication()).getLiferayClient();

            JSONArray result;
            try {
                result = client.listGroups(Application.DEFAULT_COMPANY_ID);
            } catch (Exception e) {
                // TODO FIXME Notify error
                Log.e(this.getClass().getName(), "Failed to retrieve group list: " + e.toString());
                return false;
            }

            items = new ArrayList<>(result.length());

            for (int i = 0; i < result.length(); i++) {
                try {
                    Group group = Group.fromJSONObject(result.getJSONObject(i));

                    // Ignore non public (type != 1) or inactive (active != true) groups
                    // FIXME We should probably place this filter at some other layer.
                    if (!group.isActive() || group.getType() != 1) {
                        continue;
                    }

                    items.add(group);
                } catch (JSONException e) {
                    // FIXME XXX Notify error
                    Log.e(this.getClass().getSimpleName(), "Failed to load group list: " + e.toString());
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

            ListAdapter adapter = new GroupListAdapter(getActivity(),
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
