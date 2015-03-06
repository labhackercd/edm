package net.labhackercd.edemocracia.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.client.exception.AuthorizationException;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public class UberRecyclerView extends SwipeRefreshLayout {

    @InjectView(R.id.progress_container) View progressView;
    @InjectView(R.id.errorMessage) TextView errorMessageView;
    @InjectView(android.R.id.list) RecyclerView recyclerView;
    @InjectView(R.id.load_error_container) View errorContainerView;

    private OnRefreshListener onRefreshListener;

    public UberRecyclerView(Context context) {
        this(context, null);
    }

    public UberRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.inject(this);

        final Context context = getContext();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new UberDivider(context));

        // Progress view starts invisible
        progressView.setVisibility(View.GONE);

        // Error view also starts hidden
        errorContainerView.setVisibility(View.GONE);

        // Swipe to refresh starts disabled
        super.setEnabled(false);

        super.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onUberRefresh();
            }
        });

        errorContainerView.findViewById(R.id.retryButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i("Reload pls.");
                onUberRefresh();
            }
        });
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.onRefreshListener = listener;
    }

    @Override
    public boolean canChildScrollUp() {
        // XXX Only works for vertical LinearLayoutManager. If you need something else,
        // implement it.
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
        return position != 0;
    }

    private void onUberRefresh() {
        if (onRefreshListener != null)
            onRefreshListener.onRefresh();
    }

    private void setProgressVisibility(boolean visible) {
        progressView.setVisibility(visible ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (super.isEnabled()) {
            super.setRefreshing(refreshing);
        } else {
            setProgressVisibility(refreshing);
        }
    }

    @Override
    @Deprecated
    public void setEnabled(boolean enabled) {
        setSwipeRefreshEnabled(enabled);
    }

    public void setSwipeRefreshEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public RecyclerView.Adapter getAdapter() {
        return recyclerView.getAdapter();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void showError(int errorMessage) {
        if (super.isEnabled()) {
            // If the swipe to refresh gesture is enabled we should display the error message
            // in some non-disruptive way, which is through a Toast.
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            // Hide everything...
            progressView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);

            // Disable the swipe-to-refresh gesture...
            super.setEnabled(false);

            errorMessageView.setText(errorMessage);

            // Display the error view
            errorContainerView.setVisibility(View.VISIBLE);
        }
    }

    public void clearError() {
        recyclerView.setVisibility(View.VISIBLE);
        errorContainerView.setVisibility(View.GONE);
    }

    public Observable<Boolean> refreshEvents() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final OnRefreshListener listener = new OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onNext(true);
                    }
                };

                UberRecyclerView.this.setOnRefreshListener(listener);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        UberRecyclerView.this.setOnRefreshListener(null);
                    }
                }));

                // First refresh never cares about data freshness
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (adapter == null || adapter.getItemCount() == 0)
                    if (!subscriber.isUnsubscribed())
                        subscriber.onNext(false);
            }
        });
    }

    public Observer<RecyclerView.Adapter> dataHandler() {
        return new Observer<RecyclerView.Adapter>() {
            @Override
            public void onNext(RecyclerView.Adapter adapter) {
                boolean hasItems = adapter != null && adapter.getItemCount() > 0;

                // Set the adapter
                if (adapter != getAdapter())
                    setAdapter(adapter);

                // TODO Show something when the list is empty

                // Hide eventual error message
                clearError();

                // Stop the refreshing animation
                setRefreshing(false);

                // Keep the swipe-to-refresh gesture enabled only if there are any items to display
                setSwipeRefreshEnabled(hasItems);
            }

            @Override
            public void onCompleted() {
                // Nothing to do
            }

            @Override
            public void onError(Throwable e) {
                int errorMessage;
                if (e instanceof IOException) {
                    errorMessage = R.string.network_error_message;
                } else if (e instanceof AuthorizationException) {
                    errorMessage = R.string.authorization_error_message;
                } else {
                    Timber.e(e, "Failed to load list data.");
                    errorMessage = R.string.load_error_message;
                }

                setRefreshing(false);
                showError(errorMessage);
            }
        };
    }
}

