package net.labhackercd.edemocracia.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link ListFragment} that loads its items inside an AsyncTask. It displays a progress
 * indicator while loading items.
 *
 * To use it, implement the fetchItems method.
 *
 * It's possible to use specific ListAdapters by overriding the createAdapter method.
 *
 * @param <T>
 */
public abstract class SimpleListFragment<T extends Identifiable> extends ListFragment {

    private RefreshListTask refreshListTask;

    public SimpleListFragment() {
        // Required empty constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshList();
    }

    protected abstract List<T> fetchItems() throws Exception;

    @NonNull
    protected ListAdapter createAdapter(Context context, List<T> items) {
        return new SimpleArrayAdapter<>(context, items);
    }

    private void refreshList() {
        // Clear the list
        setListItems(null);

        // Call this after that
        setListShown(false);

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

            if (SimpleListFragment.this.getView() != null) {
                setListShown(true);
            }
        }

        @Override
        protected void onCancelled() {
            refreshListTask = null;

            setListItems(null);

            if (SimpleListFragment.this.getView() != null) {
                setListShown(true);
            }
        }
    }
}