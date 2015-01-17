package br.leg.camara.labhacker.edemocracia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ListFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.service.CustomService;


public class GroupListFragment extends ListFragment {

    private View listView;
    private View progressView;
    private RefreshListTask refreshListTask;
    private OnGroupSelectedListener listener;

    public GroupListFragment() {
        // Required empty constructor
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
        showProgress(true);

        setListItems(new ArrayList<Group>());

        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }

    private void setListItems(List<Group> items) {
        if (getActivity() == null) {
            return;
        }

        ListAdapter adapter = new GroupListAdapter(getActivity(),
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

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Uri uri);
    }

    private class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        List<Group> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            Session session = SessionProvider.getSession(getActivity().getApplication());
            CustomService service = new CustomService(session);

            JSONArray result;
            try {
                result = service.listGroups(SessionProvider.DEFAULT_COMPANY_ID);
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

            setListItems(items);

            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            refreshListTask = null;

            showProgress(false);
        }
    }
}
