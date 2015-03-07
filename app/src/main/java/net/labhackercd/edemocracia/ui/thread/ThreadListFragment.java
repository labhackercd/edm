package net.labhackercd.edemocracia.ui.thread;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.RequestCache;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.rx.Operators;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ThreadListFragment extends BaseFragment {

    public static String ARG_PARENT = "parent";

    @Inject EventBus eventBus;
    @Inject RequestCache cache;
    @Inject MainRepository repository;

    private Object parent;
    private ItemListView listView;

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
        listView = (ItemListView) inflater.inflate(
                R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ThreadListAdapter adapter = new ThreadListAdapter(eventBus);

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(pair -> adapter.replaceWith(pair.first, pair.second))
                .subscribe(listView.dataHandler());
    }

    private Observable<Pair<List<Category>, List<Thread>>> getListData(boolean fresh) {
        MainRepository.Request<List<Thread>> threads;
        MainRepository.Request<List<Category>> categories;

        if (parent instanceof Group) {
            Group group = (Group) parent;
            threads = repository.getThreads(group.getGroupId());
            categories = repository.getCategories(group.getGroupId());
        } else {
            Category category = (Category) parent;
            threads = repository.getThreads(category.getGroupId(), category.getCategoryId());
            categories = repository.getCategories(category.getGroupId(), category.getCategoryId());
        }

        Activity activity = getActivity();

        Observable<List<Thread>> t = Observable.just(threads)
                .compose(Operators.requireAccount2(activity))
                .flatMap(cache.skipIf(fresh))
                .subscribeOn(Schedulers.io());

        Observable<List<Category>> c = Observable.just(categories)
                .compose(Operators.requireAccount2(activity))
                .flatMap(cache.skipIf(fresh))
                .subscribeOn(Schedulers.io());

        return Observable.zip(c, t, Pair::new);
    }
}
