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

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;


public class GroupListFragment extends ListFragment {

    private View progressView;
    private RefreshListTask refreshListTask;
    private OnGroupSelectedListener listener;


    public GroupListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            progressView = getActivity().findViewById(R.id.refresh_progress);
            refreshGroupList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_list_fragment, container, false);
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
            Uri groupUri = ContentUris.withAppendedId(Group.CONTENT_URI, id);
            listener.onGroupSelected(groupUri);
        }
    }

    private void refreshGroupList() {
        showProgress(true);
        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    public interface OnGroupSelectedListener {
        public void onGroupSelected(Uri groupUri);
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
            showProgress(false);

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
            showProgress(false);
        }
    }
}
