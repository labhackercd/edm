package net.labhackercd.edemocracia.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.BaseActivity;
import net.labhackercd.edemocracia.ui.UberRecyclerView;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.Observers;
import timber.log.Timber;

public class MessageListFragment extends Fragment {

    private static final String ARG_THREAD = "thread";

    @Inject User user;
    @Inject DataRepository repository;

    private Thread thread;
    private Message rootMessage;
    private UberRecyclerView uberRecyclerView;

    public static MessageListFragment newInstance(Thread thread) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_THREAD, thread);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            thread = (Thread) args.getSerializable(ARG_THREAD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uberRecyclerView = (UberRecyclerView) inflater.inflate(R.layout.uber_recycler_view, container, false);
        return uberRecyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BaseActivity.get2(getActivity()).getObjectGraph().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final MessageListAdapter adapter = new MessageListAdapter();

        final Observer<? super MessageListAdapter> setRootMessage = Observers.create(newAdapter -> {
            rootMessage = newAdapter.getRootMessage();
        });

        uberRecyclerView.refreshEvents()
                .forEach(fresh -> {
                    uberRecyclerView.setRefreshing(true);
                    repository.getMessages(thread)
                            .take(fresh ? 2 : 1).first()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(adapter::replaceWith)
                            .doOnEach(setRootMessage)
                            .subscribe(uberRecyclerView.dataHandler());
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.show_thread_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply:
                return onReplySelected();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean onReplySelected() {
        if (rootMessage == null) {
            Timber.w("No root message set.");
            return false;
        }
        Intent intent = ComposeActivity.createIntent(getActivity(), ComposeActivity.class, user);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(ComposeActivity.PARAM_PARENT_MESSAGE, rootMessage);
        startActivity(intent);
        return true;
    }
}
