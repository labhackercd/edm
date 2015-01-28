package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.os.Bundle;
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

import javax.annotation.Nullable;

import br.leg.camara.labhacker.edemocracia.content.Category;
import br.leg.camara.labhacker.edemocracia.content.Forum;
import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.util.EDMBatchSession;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;
import br.leg.camara.labhacker.edemocracia.util.Identifiable;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;
import br.leg.camara.labhacker.edemocracia.util.SimpleListFragment;

public class ForumListFragment extends SimpleListFragment<ForumListFragment.ItemWrapper> {

    public static String ARG_PARENT = "parent";

    private Forum forum;
    private OnThreadSelectedListener listener;

    public static ForumListFragment newInstance(Forum forum) {
        ForumListFragment fragment = new ForumListFragment();

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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            ItemWrapper item = (ItemWrapper) getListAdapter().getItem(position);

            if (item.getForum() == null) {
                listener.onThreadSelected(item.getThread());
            } else {
                listener.onForumSelected(item.getForum());
            }
        }
    }

    @Override
    protected List<ItemWrapper> fetchItems() throws Exception {
        EDMSession session = EDMSession.get(getActivity().getApplicationContext());

        assert session != null;

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

        Iterable<ItemWrapper> ithreads = Collections2.transform(threads, new Function<Thread, ItemWrapper>() {
            @Override
            public ItemWrapper apply(Thread thread) {
                return new ItemWrapper(thread);
            }
        });

        Iterable<ItemWrapper> icategories = Collections2.transform(categories, new Function<Category, ItemWrapper>() {
            @Override
            public ItemWrapper apply(Category category) {
                return new ItemWrapper(category);
            }
        });

        return Lists.newArrayList(Iterables.concat(icategories, ithreads));
    }

    public interface OnThreadSelectedListener {
        public void onForumSelected(Forum forum);
        public void onThreadSelected(Thread thread);
    }

    protected class ItemWrapper implements Identifiable {
        private final Thread thread;
        private final Category category;

        public ItemWrapper(Thread thread) {
            this.thread = thread;
            this.category = null;
        }

        public ItemWrapper(Category category) {
            this.thread = null;
            this.category = category;
        }

        @Nullable
        public Forum getForum() {
            if (this.category != null) {
                return this.category;
            }
            return null;
        }

        @Nullable
        public Thread getThread() {
            return this.thread;
        }

        @Override
        public long getId() {
            Thread thread = getThread();
            if (thread != null) {
                return getThread().getId();
            } else {
                return this.category.getId();
            }
        }

        @Override
        public String toString() {
            Thread thread = getThread();
            if (thread != null) {
                return "[t] " + thread.toString();
            } else {
                return "[c] " + this.category.toString();
            }
        }
    }
}
