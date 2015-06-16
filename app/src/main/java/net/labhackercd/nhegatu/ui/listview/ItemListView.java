/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.ui.listview;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.labhackercd.nhegatu.R;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import net.labhackercd.nhegatu.data.api.error.AuthorizationException;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * A SwipeRefreshLayout wrapping a RecyclerView with a vertical layout manager and a separator.
 *
 * It displays a circular progress indicator while loading data for the first time, and a centered
 * error message with a *Retry* button if it fails to load the first data.
 *
 * When there is items displayed in the list, it can be refreshed through the SwipeRefreshLayout,
 * and the eventual error messages will be displayed in a non-disruptive way (currently a Toast).
 *
 * It also has some nice RxJava helpers:
 *
 *     itemListView.refreshEvents()
 *             .startWith(false)
 *             .flatMap(fresh -> getListData(fresh))
 *             .subscribe(itemListView.dataHandler());
 */
public class ItemListView extends SwipeRefreshLayout {

    @InjectView(R.id.progress_container) View progressView;
    @InjectView(R.id.errorMessage) TextView errorMessageView;
    @InjectView(android.R.id.list) RecyclerView recyclerView;
    @InjectView(R.id.load_error_container) View errorContainerView;

    private OnRefreshListener onRefreshListener;
    private PublishSubject<Boolean> refreshEvents = PublishSubject.create();
    private Observer<RecyclerView.Adapter> resultHandler = new Observer<RecyclerView.Adapter>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            handleError(e);
        }

        @Override
        public void onNext(RecyclerView.Adapter adapter) {
            handleResult(adapter);
        }
    };

    public ItemListView(Context context) {
        this(context, null);
    }

    public ItemListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.inject(this);

        final Context context = getContext();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new ListItemSeparator(context));

        // Progress view starts invisible
        progressView.setVisibility(View.GONE);

        // Error view also starts hidden
        errorContainerView.setVisibility(View.GONE);

        // Swipe to refresh starts disabled
        super.setEnabled(false);

        // Emit refresh events into the subject.
        super.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (onRefreshListener != null)
                    onRefreshListener.onRefresh();
                refreshEvents.onNext(true);
            }
        });

        errorContainerView.findViewById(R.id.retryButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRefreshListener != null)
                    onRefreshListener.onRefresh();
                refreshEvents.onNext(true);
            }
        });
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.onRefreshListener = listener;
    }

    @Override
    public boolean canChildScrollUp() {
        // XXX Only works for vertical LinearLayoutManager.
        LinearLayoutManager layoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        return 0 != layoutManager.findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (super.isEnabled()) {
            super.setRefreshing(refreshing);
        } else {
            setProgressVisibility(refreshing);
        }
    }

    private void setProgressVisibility(boolean visible) {
        progressView.setVisibility(visible ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    public RecyclerView.Adapter getAdapter() {
        return recyclerView.getAdapter();
    }

    public void showError(int errorMessage) {
        if (super.isEnabled()) {
            // If the swipe to refresh gesture is enabled we should display the error
            // message in some non-disruptive way, which is through a Toast.
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
        return refreshEvents.asObservable();
    }

    public Observer<RecyclerView.Adapter> dataHandler() {
        return resultHandler;
    }

    private void handleResult(RecyclerView.Adapter adapter) {
        boolean hasItems = adapter != null && adapter.getItemCount() > 0;

        // Set the adapter
        if (adapter != getAdapter())
            recyclerView.setAdapter(adapter);

        // TODO Show something when the list is empty

        // Hide eventual error message
        clearError();

        // Stop the refreshing animation
        setRefreshing(false);

        // Keep the swipe-to-refresh gesture enabled only if there are any items to display
        setEnabled(hasItems);
    }

    private void handleError(Throwable error) {
        int errorMessage;
        if (error instanceof IOException) {
            errorMessage = R.string.network_error_message;
        } else if (error instanceof AuthorizationException) {
            errorMessage = R.string.authorization_error_message;
        } else {
            Timber.e(error, "Failed to load list data.");
            errorMessage = R.string.load_error_message;
        }
        setRefreshing(false);
        showError(errorMessage);
    }

    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    public void smoothScrollToPosition(int position) {
        recyclerView.smoothScrollToPosition(position);
    }
}

