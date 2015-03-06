package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.UberRecyclerView;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ThreadListFragment extends BaseFragment {

    public static String ARG_PARENT = "parent";

    @Inject EventBus eventBus;
    @Inject DataRepository repository;

    private Object parent;
    private UberRecyclerView uberRecyclerView;

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
            parent = args.getSerializable(ARG_PARENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uberRecyclerView = (UberRecyclerView) inflater.inflate(
                R.layout.uber_recycler_view, container, false);
        return uberRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ThreadListAdapter adapter = new ThreadListAdapter(eventBus);

        uberRecyclerView.refreshEvents()
                .forEach(fresh -> {
                    uberRecyclerView.setRefreshing(true);
                    getListData(fresh)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(pair -> adapter.replaceWith(pair.first, pair.second))
                            .subscribe(uberRecyclerView.dataHandler());
                });
    }

    private Observable<Pair<List<Category>, List<Thread>>> getListData(boolean fresh) {
        Observable<List<Thread>> threads;
        Observable<List<Category>> categories;

        if (parent instanceof Group) {
            Group group = (Group) parent;
            threads = repository.getThreads(group.getGroupId())
                    .take(fresh ? 2 : 1).last()
                    .flatMap(Observable::from)
                    .filter(thread -> thread != null && thread.getCategoryId() == 0)
                    .toList();
            categories = repository.getCategories(group.getGroupId())
                    .flatMap(Observable::from)
                    .take(fresh ? 2 : 1).last()
                    .filter(cat -> cat != null && cat.getParentCategoryId() == 0)
                    .toList();
        } else {
            Category category = (Category) parent;
            threads = repository.getThreads(category.getGroupId(), category.getCategoryId())
                    .take(fresh ? 2 : 1).last();
            categories = repository.getCategories(category.getGroupId(), category.getCategoryId())
                    .take(fresh ? 2 : 1).last();
        }

        return Observable.zip(categories, threads, Pair<List<Category>, List<Thread>>::new);
    }
}
