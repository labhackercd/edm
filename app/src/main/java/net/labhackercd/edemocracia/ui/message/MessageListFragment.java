package net.labhackercd.edemocracia.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.Cache;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.rx.Operators;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListFragment extends BaseFragment {

    private static final String ARG_THREAD = "thread";

    @Inject Cache cache;
    @Inject MainRepository repository;

    private Thread thread;
    private Message rootMessage;
    private ItemListView listView;

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
        listView = (ItemListView) inflater.inflate(R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final MessageListAdapter adapter = new MessageListAdapter();

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .doOnNext(this::setRootMessage)
                .subscribe(listView.dataHandler());
    }

    private void setRootMessage(MessageListAdapter adapter) {
        rootMessage = adapter.getRootMessage();
    }

    private Observable<List<Message>> getListData(boolean fresh) {
        Activity activity = getActivity();
        return repository.getThreadMessages(thread)
                .transform(r -> r.asObservable()
                        .compose(Operators.requireAccount(activity))
                        .compose(cache.cacheSkipIf(r.key(), fresh)))
                .asObservable()
                .subscribeOn(Schedulers.io());
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
        Intent intent = new Intent(getActivity(), ComposeActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(ComposeActivity.PARAM_PARENT_MESSAGE, rootMessage);
        startActivity(intent);
        return true;
    }
}
