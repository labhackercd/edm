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


public abstract class SimpleRecycleViewFragment<T extends Identifiable> extends InjectableFragment {

    private RefreshListTask refreshListTask;
    private RecyclerView recyclerView;
    private View progressView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.simple_recycler_view, container, false);

        progressView = root.findViewById(R.id.progress_container);
        progressView.setVisibility(View.GONE);

        Activity activity = getActivity();

        recyclerView = (RecyclerView) root.findViewById(android.R.id.list);
        recyclerView.addItemDecoration(new SimpleRecyclerViewDivider(activity));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshList();
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

    protected abstract List<T> blockingFetchItems() throws Exception;

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }

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

            RecyclerView view = getRecyclerView();
            if (view != null) {
                view.setAdapter(createAdapter(items));
            }

            setProgressVisibility(false);
        }

        @Override
        protected void onCancelled() {
            onPostExecute(false);
        }
    }
}