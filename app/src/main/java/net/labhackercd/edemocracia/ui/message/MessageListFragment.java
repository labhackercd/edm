package net.labhackercd.edemocracia.ui.message;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserData;
import net.labhackercd.edemocracia.data.Cache;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.model.LocalMessage;
import net.labhackercd.edemocracia.data.rx.Operators;
import net.labhackercd.edemocracia.job.EDMJobManager;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListFragment extends BaseFragment {

    private static final String ARG_THREAD = "thread";
    private static final int REQUEST_INSERT_MESSAGE = 1;

    @Inject Cache cache;
    @Inject UserData userData;
    @Inject EventBus eventBus;
    @Inject EDMJobManager jobManager;
    @Inject MainRepository repository;

    private Thread thread;
    private Message rootMessage;
    private ItemListView listView;
    private LocalMessage scrollToItem;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSERT_MESSAGE && resultCode == Activity.RESULT_OK) {
            LocalMessage inserted = (LocalMessage) data.getSerializableExtra(
                    ComposeActivity.PARAM_INSERTED_MESSAGE);
            if (inserted != null) {
                scrollToItem = inserted;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        AccountManager manager = AccountManager.get(activity);
        Account account = AccountUtils.getAccount(activity);
        User user = userData.getUser(manager, account);

        final MessageListAdapter adapter = new MessageListAdapter(user, eventBus);

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .doOnNext(this::setRootMessage)
                .doOnNext(this::scrollToLastInsertedItem)
                .cast(RecyclerView.Adapter.class)
                .subscribe(listView.dataHandler());
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

    private void setRootMessage(MessageListAdapter adapter) {
        rootMessage = adapter.getRootMessage();
    }

    private Observable<Pair<List<Message>, List<LocalMessage>>> getListData(boolean fresh) {
        Activity activity = getActivity();

        Observable<List<Message>> remoteMessages = repository.getThreadMessages(thread)
                .transform(r -> r.asObservable()
                        .compose(Operators.requireAccount(activity))
                        .compose(cache.cacheSkipIf(r.key(), fresh)))
                .asObservable()
                .subscribeOn(Schedulers.io());

        Observable<List<LocalMessage>> localMessages = jobManager
                .getUnsentMessages(thread.getRootMessageId());

        return Observable.zip(
                remoteMessages, localMessages, MessageListFragment::combineMessageLists);
    }

    /** Combine two lists of messages. */
    private static Pair<List<Message>, List<LocalMessage>> combineMessageLists(
            List<Message> remote, List<LocalMessage> local) {
        // A set containing all remote message UUIDs
        Set<String> remoteUUIDs = Sets.newHashSet(Observable.from(remote)
                .map(Message::getUuid)
                .toBlocking()
                .toIterable());

        // Filter local messages, ignoring those which UUIDs are already in the remote server.
        Iterable<LocalMessage> filtered = Observable.from(local)
                .filter(msg -> !remoteUUIDs.contains(msg.uuid.toString()))
                .toBlocking()
                .toIterable();

        // Combine the two lists.
        List<LocalMessage> filteredList = Lists.newArrayList(filtered);
        return new Pair<>(remote, filteredList);
    }

    private void scrollToLastInsertedItem(MessageListAdapter adapter) {
        if (scrollToItem != null) {
            int position = adapter.getItemPosition(scrollToItem);
            if (position > 10)
                listView.scrollToPosition(position);
            else
                listView.smoothScrollToPosition(position);
            scrollToItem = null;
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
        startActivityForResult(intent, REQUEST_INSERT_MESSAGE);
        return true;
    }
}
