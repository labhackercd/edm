package net.labhackercd.edemocracia.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.google.common.collect.Sets;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.data.Cache;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.MainActivity;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import org.kefirsf.bb.TextProcessor;

import java.util.*;

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
    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;
    @Inject TextProcessor textProcessor;
    @Inject LocalMessageStore messageRepository;

    private ThreadData data;
    private UUID scrollToItem;
    private Message rootMessage;
    private ItemListView listView;
    private MessageListAdapter adapter;

    public static MessageListFragment newInstance(Thread thread) {
        return newInstance(ThreadData.create(thread));
    }

    public static Fragment newInstance(LocalMessage message) {
        return newInstance(ThreadData.create(message), message.getUuid());
    }

    private static MessageListFragment newInstance(ThreadData data) {
        return newInstance(data, null);
    }

    private static MessageListFragment newInstance(ThreadData data, UUID uuid) {
        MessageListFragment fragment = new MessageListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_THREAD_DATA, data);
        args.putSerializable(ARG_SCROLL_TO_ITEM, uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        data = args.getParcelable(ARG_THREAD_DATA);
        scrollToItem = (UUID) args.getSerializable(ARG_SCROLL_TO_ITEM);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ItemListView) inflater.inflate(R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSERT_MESSAGE && resultCode == Activity.RESULT_OK)
            scrollToItem = (UUID) data.getSerializableExtra(ComposeActivity.PARAM_INSERTED_MESSAGE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::refreshList)
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::replaceAdapterData)
                .subscribe(listView.dataHandler());
    }


    private Observable<List<Item>> refreshList(boolean fresh) {
        Observable<List<Item>> remoteMessages = repository
                .getThreadMessages(data.getGroupId(), data.getCategoryId(), data.getThreadId())
                .transform(r -> r.asObservable()
                        .compose(AccountUtils.requireAccount(getActivity()))
                        .compose(cache.cacheSkipIf(r.key(), fresh)))
                .asObservable()
                .subscribeOn(Schedulers.io())
                .doOnNext(this::setRootMessage)
                .map(messages -> Observable.from(messages)
                        .map(Item::create)
                        .toList().toBlocking().single());

        Observable<User> currentUser = AccountUtils.getCurrentUser(repository, getActivity());

        Observable<List<Item>> localMessages = messageRepository
                .getUnsentMessages(data.getRootMessageId())
                .subscribeOn(Schedulers.io())
                .zipWith(currentUser.repeat(), (messages, user) ->
                        Observable.from(messages)
                                .map(Item::builder)
                                .map(builder -> builder.setUserId(user.getUserId()))
                                .map(builder -> builder.setUserName(MainActivity.getUserDisplayName(user)))
                                .map(Item.Builder::build)
                                .toList().toBlocking().single());

        return localMessages.zipWith(remoteMessages.repeat(), MessageListFragment::combineMessageLists);
    }

    private static List<Item> combineMessageLists(List<Item> local, List<Item> remote) {
        // A set containing all remote message UUIDs
        Set<String> remoteUUIDs = Sets.newHashSet(
                Observable.from(remote)
                        .map(Item::getUuid)
                        .map(UUID::toString)
                        .toBlocking()
                        .toIterable());

        // Filter local messages, ignoring those which UUIDs are already in the remote server.
        Iterable<Item> filtered = Observable.from(local)
                .filter(item -> !remoteUUIDs.contains(item.getUuid().toString()))
                .toBlocking()
                .toIterable();

        return Lists.newLinkedList(Iterables.concat(remote, filtered));
    }

    private void setRootMessage(List<Message> messages) {
        rootMessage = messages.isEmpty() ? null : messages.get(0);
    }

    private MessageListAdapter replaceAdapterData(List<Item> data) {
        if (adapter == null) {
            adapter = new MessageListAdapter(messageRepository, textProcessor, imageLoader);

            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (scrollToItem != null) {
                        adapter.scrollToItem(scrollToItem);
                        scrollToItem = null;
                    }
                }
            });
        }

        adapter.replaceWith(data);

        return adapter;
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
            // TODO Show a toast?
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
            return create(localMessage.getGroupId(), localMessage.getCategoryId(),
                    localMessage.getThreadId(), localMessage.getRootMessageId());
        }

        private static ThreadData create(long groupId, long categoryId, long threadId, long rootMessageId) {
            return new AutoParcel_MessageListFragment_ThreadData(groupId, categoryId, threadId, rootMessageId);
        }
    }

    @AutoParcel
    static abstract class Item implements Parcelable {
        public abstract UUID getUuid();
        public abstract long getUserId();
        @Nullable public abstract String getUserName();
        public abstract String getSubject();
        public abstract String getBody();
        public abstract LocalMessage.Status getStatus();
        public abstract Date getCreateDate();

        @AutoParcel.Builder
        interface Builder {
            Builder setUuid(UUID uuid);
            Builder setUserId(long userId);
            Builder setUserName(@Nullable String userName);
            Builder setSubject(String subject);
            Builder setBody(String body);
            Builder setStatus(LocalMessage.Status status);
            Builder setCreateDate(Date createDate);
            Item build();
        }

        static Builder builder() {
            return new AutoParcel_MessageListFragment_Item.Builder()
                    .setStatus(LocalMessage.Status.SUCCESS)
                    .setUserId(0)
                    .setUserName(null);
        }

        static Builder builder(LocalMessage m) {
            return builder()
                    .setUuid(m.getUuid())
                    .setSubject(m.getSubject())
                    .setBody(m.getBody())
                    .setStatus(m.getStatus())
                    .setCreateDate(m.getInsertionDate());
        }

        static Item create(Message m) {
            return builder()
                    .setUuid(UUID.fromString(m.getUuid()))
                    .setUserId(m.getUserId())
                    .setUserName(m.getUserName())
                    .setSubject(m.getSubject())
                    .setBody(m.getBody())
                    .setCreateDate(m.getCreateDate())
                    .build();
        }
    }
}
