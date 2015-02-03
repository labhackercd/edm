package net.labhackercd.edemocracia.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.util.Identifiable;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public abstract class SimpleRecyclerViewFragment<T extends Identifiable> extends InjectableFragment {

    private RefreshListTask refreshListTask;

    @InjectView(R.id.progress_container) View progressView;
    @InjectView(android.R.id.list) RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.simple_recycler_view, container, false);

        ButterKnife.inject(this, root);

        // Progress view starts hidden
        progressView.setVisibility(View.GONE);

        Activity activity = getActivity();

        recyclerView.addItemDecoration(new SimpleRecyclerViewDivider(activity));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    protected void refreshList() {
        // Kick the background task to refresh the list
        refreshListTask = new RefreshListTask();
        refreshListTask.execute((Void) null);
    }

    protected void setProgressVisibility(boolean visible) {
        progressView.setVisibility(visible ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    /**
     * Synchronously fetch list items. This method is called inside an AsyncTask to refresh the list.
     *
     * If an exception is thrown, it'll be logged and silenced.
     *
     * @return List<T>
     * @throws Exception
     */
    protected abstract List<T> blockingFetchItems() throws Exception;

    /**
     * Create a RecyclerView adapter for the list.
     *
     * @param List<T> items
     * @return RecyclerView.Adapter The adapter
     */
    protected abstract RecyclerView.Adapter createAdapter(List<T> items);

    private class RefreshListTask extends AsyncTask<Void, Void, Boolean> {
        private List<T> items;

        @Override
        protected void onPreExecute() {
            setProgressVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                items = blockingFetchItems();
            } catch (Exception e) {
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

            if (items == null) {
                items = new ArrayList<>();
            }

            if (recyclerView != null) {
                recyclerView.setAdapter(createAdapter(items));
            }

            setProgressVisibility(false);
        }

        @Override
        protected void onCancelled() {
            onPostExecute(false);
        }
    }
}