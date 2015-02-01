package net.labhackercd.edemocracia.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import org.json.JSONArray;

import java.util.List;

import net.labhackercd.edemocracia.activity.SessionProvider;
import net.labhackercd.edemocracia.content.Category;
import net.labhackercd.edemocracia.content.Forum;
import net.labhackercd.edemocracia.content.Group;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.util.EDMBatchSession;
import net.labhackercd.edemocracia.util.EDMSession;
import net.labhackercd.edemocracia.util.JSONReader;
import net.labhackercd.edemocracia.util.SimpleListFragment;

public class ThreadListFragment extends SimpleListFragment<ThreadItem> {

    public static String ARG_PARENT = "parent";

    private Forum forum;
    private OnThreadSelectedListener listener;

    public static ThreadListFragment newInstance(Forum forum) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_PARENT, forum);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            forum = args.getParcelable(ARG_PARENT);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnThreadSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnThreadSelectedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @NonNull
    @Override
    protected android.widget.ListAdapter createAdapter(Context context, List<ThreadItem> items) {
        return new ThreadListAdapter(context, items);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            ThreadItem item = (ThreadItem) getListAdapter().getItem(position);

            if (item.getForum() == null) {
                listener.onThreadSelected(item.getThread());
            } else {
                listener.onForumSelected(item.getForum());
            }
        }
    }

    @Override
    protected List<ThreadItem> fetchItems() throws Exception {
        EDMSession session = ((SessionProvider) getActivity()).getSession();

        EDMBatchSession batchSession = new EDMBatchSession(session);

        MBThreadService threadService = new MBThreadService(batchSession);
        MBCategoryService categoryService = new MBCategoryService(batchSession);

        if (forum instanceof Group) {
            threadService.getGroupThreads(forum.getGroupId(), -1, 0, -1, -1);
            categoryService.getCategories(forum.getGroupId());
        } else {
            threadService.getThreads(forum.getGroupId(), forum.getCategoryId(), 0, -1, -1);
            categoryService.getCategories(forum.getGroupId(), forum.getCategoryId(), -1, -1);
        }

        JSONArray jsonResult = batchSession.invoke();
        JSONArray jsonThreads = jsonResult.getJSONArray(0);
        JSONArray jsonCategories = jsonResult.getJSONArray(1);

        List<Thread> threads = JSONReader.fromJSON(jsonThreads, Thread.JSON_READER);

        MBMessageService messageService = new MBMessageService(batchSession);

        for (Thread thread : threads) {
            messageService.getMessage(thread.getRootMessageId());
        }

        List<Message> messages = JSONReader.fromJSON(batchSession.invoke(), Message.JSON_READER);

        for (Message message : messages) {
            threads.get(messages.indexOf(message)).setRootMessage(message);
        }

        List<Category> categories = JSONReader.fromJSON(jsonCategories, Category.JSON_READER);

        Iterable<ThreadItem> ithreads = Collections2.transform(threads, new Function<Thread, ThreadItem>() {
            @Override
            public ThreadItem apply(Thread thread) {
                return new ThreadItem(thread);
            }
        });

        Iterable<ThreadItem> icategories = Collections2.transform(categories, new Function<Category, ThreadItem>() {
            @Override
            public ThreadItem apply(Category category) {
                return new ThreadItem(category);
            }
        });

        return Lists.newArrayList(Iterables.concat(icategories, ithreads));
    }

    public interface OnThreadSelectedListener {
        public void onForumSelected(Forum forum);
        public void onThreadSelected(Thread thread);
    }

}
