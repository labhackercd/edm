package net.labhackercd.edemocracia.ui.message;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.data.rx.Operators;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import org.kefirsf.bb.TextProcessor;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import auto.parcel.AutoParcel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListFragment extends BaseFragment {

    private static final String ARG_THREAD_DATA = "threadData";
    private static final String ARG_SCROLL_TO_ITEM = "scrollToItemById";

    private static final int REQUEST_INSERT_MESSAGE = 1;

    @Inject Cache cache;
    @Inject UserData userData;
    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;
    @Inject TextProcessor textProcessor;
    @Inject
    LocalMessageStore messageRepository;

    private ThreadData data;
    private Message rootMessage;
    private ItemListView listView;
    private long scrollToItem = -1;

    public static MessageListFragment newInstance(Thread thread) {
        return newInstance(ThreadData.create(thread));
    }

    public static Fragment newInstance(LocalMessage message) {
        return newInstance(ThreadData.create(message), message.id());
    }

    private static MessageListFragment newInstance(ThreadData data) {
        return newInstance(data, -1);
    }

    private static MessageListFragment newInstance(ThreadData data, long localMessageId) {
        MessageListFragment fragment = new MessageListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_THREAD_DATA, data);
        args.putLong(ARG_SCROLL_TO_ITEM, localMessageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        data = args.getParcelable(ARG_THREAD_DATA);
        scrollToItem = args.getLong(ARG_SCROLL_TO_ITEM, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ItemListView) inflater.inflate(R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSERT_MESSAGE && resultCode == Activity.RESULT_OK) {
            long inserted = data.getLongExtra(ComposeActivity.PARAM_INSERTED_MESSAGE, -1);
            if (inserted >= 0)
                scrollToItem = inserted;
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

        final MessageListAdapter adapter = new MessageListAdapter(
                messageRepository, user, textProcessor, imageLoader);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (scrollToItem > 0) {
                    int position = adapter.getLocalMessagePositionById(scrollToItem);
                    if (position > 0) {
                        Timber.d("Scrolling to last inserted item %d", position);
                        scrollToPosition(position);
                    }
                    scrollToItem = -1;
                }
            }
        });

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .doOnNext(this::setRootMessage)
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

        Observable<List<Message>> remoteMessages = repository
                .getThreadMessages(data.getGroupId(), data.getCategoryId(), data.getThreadId())
                .transform(r -> r.asObservable()
                        .compose(Operators.requireAccount(activity))
                        .compose(cache.cacheSkipIf(r.key(), fresh)))
                .asObservable()
                .subscribeOn(Schedulers.io());

        Observable<List<LocalMessage>> localMessages = messageRepository
                .getUnsentMessages(data.getRootMessageId())
                .first();

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
                .filter(msg -> !remoteUUIDs.contains(msg.uuid().toString()))
                .toBlocking()
                .toIterable();

        // Combine the two lists.
        List<LocalMessage> filteredList = Lists.newArrayList(filtered);
        return new Pair<>(remote, filteredList);
    }

    private void scrollToPosition(int position) {
        if (position > 10)
            listView.scrollToPosition(position);
        else
            listView.smoothScrollToPosition(position);
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

    @AutoParcel
    static abstract class ThreadData implements Parcelable {
        public abstract long getGroupId();
        public abstract long getCategoryId();
        public abstract long getThreadId();
        public abstract long getRootMessageId();

        static ThreadData create(Thread thread) {
            return create(thread.getGroupId(), thread.getCategoryId(), thread.getThreadId(),
                    thread.getRootMessageId());
        }

        static ThreadData create(LocalMessage localMessage) {
            return create(localMessage.groupId(), localMessage.categoryId(),
                    localMessage.threadId(), localMessage.rootMessageId());
        }

        private static ThreadData create(long groupId, long categoryId, long threadId, long rootMessageId) {
            return new AutoParcel_MessageListFragment_ThreadData(groupId, categoryId, threadId, rootMessageId);
        }
    }
}
