package net.labhackercd.edemocracia.ui;

import android.support.v7.widget.RecyclerView;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.client.exception.AuthorizationException;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import timber.log.Timber;

public class UberLoader {
    private final UberRecyclerView uberRecyclerView;
    private Observable<RecyclerView.Adapter> loader;

    public UberLoader(UberRecyclerView uberRecyclerView) {
        this.uberRecyclerView = uberRecyclerView;
    }

    public UberLoader install(Observable<RecyclerView.Adapter> loader) {
        this.loader = loader;
        uberRecyclerView.setOnRefreshListener(this::trigger);
        return this;
    }

    public UberLoader start() {
        this.trigger();
        return this;
    }

    private void trigger() {
        uberRecyclerView.setRefreshing(true);
        loader.subscribe(new Observer<RecyclerView.Adapter>() {
            @Override
            public void onCompleted() {
                // Nothing to do here.
            }

            @Override
            public void onError(Throwable e) {
                UberLoader.this.onError(e);
            }

            @Override
            public void onNext(RecyclerView.Adapter adapter) {
                UberLoader.this.onNext(adapter);
            }
        });
    }

    protected void onError(Throwable e) {
        int errorMessage;
        if (e instanceof IOException) {
            errorMessage = R.string.network_error_message;
        } else if (e instanceof AuthorizationException) {
            errorMessage = R.string.authorization_error_message;
        } else {
            errorMessage = R.string.load_error_message;
            Timber.e(e, "Failed to load list data.");
        }

        uberRecyclerView.setRefreshing(false);
        uberRecyclerView.showError(errorMessage);
    }

    protected void onNext(RecyclerView.Adapter adapter) {
        boolean hasItems = adapter != null && adapter.getItemCount() > 0;

        // Set the adapter
        if (adapter != uberRecyclerView.getAdapter())
            uberRecyclerView.setAdapter(adapter);

        // TODO Show something when the list is empty

        // Hide eventual error message
        uberRecyclerView.clearError();

        // Stop the refreshing animation
        uberRecyclerView.setRefreshing(false);

        // Keep the swipe-to-refresh gesture enabled only if there are any items to display
        uberRecyclerView.setSwipeRefreshEnabled(hasItems);
    }
}
