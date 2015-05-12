package net.labhackercd.nhegatu.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.squareup.picasso.Picasso;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.data.Cache;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.LocalMessageStore;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;
import net.labhackercd.nhegatu.data.db.LocalMessage;
import net.labhackercd.nhegatu.data.model.Message;
import net.labhackercd.nhegatu.service.VideoAttachmentUploader;
import net.labhackercd.nhegatu.ui.BaseFragment;
import net.labhackercd.nhegatu.ui.MainActivity;
import net.labhackercd.nhegatu.ui.listview.ItemListView;
import net.labhackercd.nhegatu.ui.message.MessageListAdapter.Item;

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
    @Inject Picasso picasso;
    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;
    @Inject TextProcessor textProcessor;
    @Inject VideoAttachmentUploader uploader;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            data = savedInstanceState.getParcelable(ARG_THREAD_DATA);
            scrollToItem = (UUID) savedInstanceState.getSerializable(ARG_SCROLL_TO_ITEM);
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (data != null)
            outState.putParcelable(ARG_THREAD_DATA, data);
        if (scrollToItem != null)
            outState.putSerializable(ARG_SCROLL_TO_ITEM, scrollToItem);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSERT_MESSAGE && resultCode == Activity.RESULT_OK)
            scrollToItem = (UUID) data.getSerializableExtra(ComposeActivity.PARAM_INSERTED_MESSAGE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Observable<List<? extends Item>> refreshList(boolean fresh) {
        Observable<List<ItemImpl>> remoteMessages = repository
                .getThreadMessages(data.getGroupId(), data.getCategoryId(), data.getThreadId())
                .transform(r -> r.asObservable()
                        .compose(AccountUtils.requireAccount(getActivity()))
                        .compose(cache.cacheSkipIf(r.key(), fresh)))
                .asObservable()
                .subscribeOn(Schedulers.io())
                .doOnNext(this::setRootMessage)
                .single()
                .map(messages -> Observable.from(messages)
                        .map(ItemImpl::create)
                        .toList().toBlocking().single());

        Observable<List<ItemImpl>> localMessages = AccountUtils.getCurrentUser(repository, getActivity())
                .flatMap(user -> messageRepository
                        .getUnsentMessages(user.getUserId(), data.getRootMessageId())
                        .map(messages -> Observable.from(messages)
                                .map(message -> ItemImpl.create(message, user))
                                .toList().toBlocking().single()));

        return Observable.combineLatest(remoteMessages, localMessages, MessageListFragment::combineMessageLists);
    }

    private static List<? extends Item> combineMessageLists(List<? extends Item> remote, List<? extends Item> local) {
        // A set containing all remote message UUIDs
        Set<UUID> remoteUUIDs = Sets.newHashSet(
                Observable.from(remote)
                        .map(i -> i.getMessage().getUuid())
                        .toBlocking()
                        .toIterable());

        // Filter local messages, ignoring those which UUIDs are already in the remote server.
        Iterable<? extends Item> filtered = Observable.from(local)
                .filter(item -> !remoteUUIDs.contains(item.getMessage().getUuid()))
                .toBlocking()
                .toIterable();

        return Lists.newLinkedList(Iterables.concat(remote, filtered));
    }

    private MessageListAdapter replaceAdapterData(List<? extends Item> data) {
        if (adapter == null) {
            adapter = new MessageListAdapter(
                    listView.getContext(), messageRepository, textProcessor, imageLoader, picasso, uploader);

            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (scrollToItem != null) {
                        int position = adapter.getItemPosition(scrollToItem);
                        if (position >= 0)
                            listView.smoothScrollToPosition(position);
                        scrollToItem = null;
                    }
                }
            });
        }

        adapter.replaceWith(data);

        return adapter;
    }

    private void setRootMessage(List<? extends Message> messages) {
        // XXX This only works because the *rootMessage* is the first in the list, obviously.
        rootMessage = messages.isEmpty() ? null : messages.get(0);
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
    static abstract class ItemImpl implements Parcelable, MessageListAdapter.Item {
        public abstract Message getMessage();
        public abstract String getUserName();
        public abstract LocalMessage.Status getStatus();
        @Nullable public abstract Uri getVideoAttachment();
        public abstract long getUploadId();

        static ItemImpl create(net.labhackercd.nhegatu.data.api.model.Message message) {
            return new AutoParcel_MessageListFragment_ItemImpl(
                    message, message.getUserName(), LocalMessage.Status.SUCCESS, null, 0);
        }

        static ItemImpl create(LocalMessage message, User user) {
            return new AutoParcel_MessageListFragment_ItemImpl(
                    message, MainActivity.getUserDisplayName(user), message.getStatus(), message.getVideoAttachment(), message.getId());
        }
    }
}
