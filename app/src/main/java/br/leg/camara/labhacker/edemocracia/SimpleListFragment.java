package br.leg.camara.labhacker.edemocracia;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link ListFragment} with a progress bar that is displayed while loading items.
 *
 * To use it, implement the fetchItems method.
 *
 * It's possible to use specific ListAdapters by overriding the createAdapter method.
 *
 * @param <T>
 */
public abstract class SimpleListFragment<T> extends ListFragment {

    private View listView;
    private View progressView;

    private RefreshListTask refreshListTask;

    public SimpleListFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setContentView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            refreshList();
        }
    }

    protected abstract List<T> fetchItems() throws Exception;

    @NonNull
    protected ListAdapter createAdapter(Context context, List<T> items) {
        return new SimpleArrayAdapter<>(context, items);
    }

    protected final void setContentView(View view) {
        listView = view.findViewById(android.R.id.list);
        progressView = view.findViewById(android.R.id.progress);

        progressView.setVisibility(View.GONE);
    }

    private void refreshList() {
        showProgress(true);

        // Clear the list
        setListItems(null);

        // Kick the background task to refresh the list
        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }

    private void setListItems(@Nullable List<T> items) {
        if (getActivity() == null) {
            return;
        }

        if (items == null) {
            items = new ArrayList<>();
        }

        setListAdapter(createAdapter(getActivity(), items));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        listView.setVisibility(show ? View.GONE : View.VISIBLE);
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            listView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1);
            progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0);
        }
    }

    private class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        private List<T> items;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                items = fetchItems();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Failed to load items: " + e);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            refreshListTask = null;

            if (!success) {
                items = null;
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