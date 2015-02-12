package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.labhackercd.edemocracia.data.api.GroupService;
import net.labhackercd.edemocracia.data.model.Category;
import net.labhackercd.edemocracia.data.model.Forum;
import net.labhackercd.edemocracia.data.model.Group;
import net.labhackercd.edemocracia.data.model.Thread;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;

import de.greenrobot.event.EventBus;

public class ThreadListFragment extends SimpleRecyclerViewFragment<ThreadItem> {
    private static final String TAG = ThreadListFragment.class.getSimpleName();

    public static String ARG_PARENT = "parent";

    @Inject Picasso picasso;
    @Inject EventBus eventBus;
    @Inject GroupService groupService;

    private Forum forum;

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
    protected RecyclerView.Adapter createAdapter(List<ThreadItem> items) {
        return new ThreadListAdapter(getActivity(), picasso, eventBus, items);
    }

    @Override
    protected List<ThreadItem> blockingFetchItems() throws Exception {
        boolean forumIsAGroup = forum instanceof Group;

        List<Thread> threads;
        List<Category> categories;

        if (forumIsAGroup) {
            threads = groupService.getThreads(forum.getGroupId(), 0, 0, -1, -1);
            categories = groupService.getCategories(forum.getGroupId());

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
        } else {
            threads = groupService.getThreads(forum.getGroupId(), forum.getCategoryId(), 0, -1, -1);
            categories = groupService.getCategories(forum.getGroupId(), forum.getCategoryId(), -1, -1);
        }

        /* TODO Get root messages for each thread
        List<Message> messages = groupService.getThreadMessages(threads);

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
}
