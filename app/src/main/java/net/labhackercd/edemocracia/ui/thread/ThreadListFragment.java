package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import org.json.JSONArray;

import java.util.List;

import javax.inject.Inject;

import net.labhackercd.edemocracia.data.api.model.BaseModel;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.data.api.EDMBatchSession;

import de.greenrobot.event.EventBus;

public class ThreadListFragment extends SimpleRecyclerViewFragment<ThreadItem> {

    public static String ARG_PARENT = "parent";

    @Inject EventBus eventBus;
    @Inject Session session;

    private BaseModel parent;

    public static Fragment newInstance(Group group) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_PARENT, group);

        fragment.setArguments(args);

        return fragment;
    }

    public static Fragment newInstance(Category category) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_PARENT, category);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            parent = (BaseModel) args.getSerializable(ARG_PARENT);
        }
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<ThreadItem> items) {
        return new ThreadListAdapter(getActivity(), eventBus, items);
    }

    @Override
    protected List<ThreadItem> blockingFetchItems() throws Exception {
        EDMBatchSession batchSession = new EDMBatchSession(session);

        MBThreadService threadService = new MBThreadService(batchSession);
        MBCategoryService categoryService = new MBCategoryService(batchSession);

        boolean parentIsAGroup = parent instanceof Group;

        if (parentIsAGroup) {
            Group group = (Group) parent;
            threadService.getGroupThreads(group.getGroupId(), -1, 0, -1, -1);
            categoryService.getCategories(group.getGroupId());
        } else {
            Category category = (Category) parent;
            threadService.getThreads(category.getGroupId(), category.getCategoryId(), 0, -1, -1);
            categoryService.getCategories(category.getGroupId(), category.getCategoryId(), -1, -1);
        }

        JSONArray jsonResult = batchSession.invoke();
        JSONArray jsonThreads = jsonResult.getJSONArray(0);
        JSONArray jsonCategories = jsonResult.getJSONArray(1);

        if (jsonThreads == null) {
            jsonThreads = new JSONArray();
        }

        if (jsonCategories == null) {
            jsonCategories = new JSONArray();
        }

        List<Thread> threads = Thread.JSON_READER.fromJSON(jsonThreads);
        List<Category> categories = Category.JSON_READER.fromJSON(jsonCategories);

        if (parentIsAGroup) {
            // Ignore threads and categories that don't belong to the given group. This is
            // required because the webservice returns all the threads and categories inside a
            // group, even the ones nested inside subcategories.
            threads = Lists.newArrayList(Collections2.filter(threads, new Predicate<Thread>() {
                @Override
                public boolean apply(@Nullable Thread thread) {
                    return thread != null && thread.getCategoryId() == 0;
                }
            }));

            categories = Lists.newArrayList(Collections2.filter(categories, new Predicate<Category>() {
                @Override
                public boolean apply(@Nullable Category category) {
                    return category != null && category.getParentCategoryId() == 0;
                }
            }));
        }

        /*
        TODO Pull root messages

        MBMessageService messageService = new MBMessageService(batchSession);

        for (Thread thread : threads) {
            messageService.getMessage(thread.getRootMessageId());
        }

        JSONArray jsonMessages = batchSession.invoke();

        if (jsonMessages == null) {
            jsonMessages = new JSONArray();
        }

        List<Message> messages = Message.JSON_READER.fromJSON(jsonMessages);

        for (Message message : messages) {
            threads.get(messages.indexOf(message)).setRootMessage(message);
        }
        */

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

    @Override
    protected Object getRefreshTaskTag() {
        if (parent instanceof Group) {
            return new Pair<Class, Long>(getClass(), ((Group) parent).getGroupId());
        } else {
            Category category = (Category) parent;
            return new Pair<Class, Pair<Long, Long>>(getClass(), new Pair<>(
                    category.getGroupId(), category.getCategoryId()));
        }
    }
}
