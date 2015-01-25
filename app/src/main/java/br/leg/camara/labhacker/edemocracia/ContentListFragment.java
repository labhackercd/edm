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
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;


/**
 * A {@link ListFragment} with a progress bar that is displayed while loading items.
 *
 * To use it, implement the fetchItems method.
 *
 * It's possible to use specific ListAdapters by overriding the createAdapter method.
 *
 * @param <T extends Content>
 */
public abstract class ContentListFragment<T extends Content> extends ListFragment {

    private RefreshListTask refreshListTask;

    public ContentListFragment() {
        // Required empty constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            refreshList();
        }
    }

    protected abstract List<T> fetchItems() throws Exception;

    @NonNull
    protected ListAdapter createAdapter(Context context, List<T> items) {
        return new ContentArrayAdapter<>(context, items);
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
            setListShown(true);
        }

        @Override
        protected void onCancelled() {
            refreshListTask = null;

            setListItems(null);
            setListShown(true);
        }
    }
}