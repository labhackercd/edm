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

import br.leg.camara.labhacker.edemocracia.content.Content;
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;


public class ThreadListFragment extends ListFragment {

    public static String ARG_PARENT = "parent";

    private View progressView;
    private RefreshListTask refreshListTask;
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
            progressView = getActivity().findViewById(R.id.refresh_progress);
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
            Uri uri = Content.withAppendedId(Thread.class, id);
            listener.onThreadSelected(uri);
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


    public interface OnThreadSelectedListener {
        public void onThreadSelected(Uri uri);
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
            showProgress(false);

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
            showProgress(false);
        }
    }
}
