package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.ui.MainActivity;
import net.labhackercd.edemocracia.ui.UberLoader;
import net.labhackercd.edemocracia.ui.UberRecyclerView;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ThreadListFragment extends Fragment {

    public static String ARG_PARENT = "parent";

    @Inject EventBus eventBus;
    @Inject DataRepository repository;

    private Object parent;
    private UberLoader uberLoader;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.get(getActivity()).getObjectGraph().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        uberLoader = new UberLoader(uberRecyclerView)
                .install(dataSource())
                .start();
    }

    private Observable<RecyclerView.Adapter> dataSource() {
        final ThreadListAdapter adapter = new ThreadListAdapter(eventBus);

        return Observable.defer(() -> {
            Observable<List<ThreadItem>> items;

            if (parent instanceof Group) {
                Group group = (Group) parent;

                Observable<List<ThreadItem>> threads = repository
                        .getThreads(group.getGroupId())
                        .flatMap(Observable::from)
                        .filter(thread -> thread != null && thread.getCategoryId() == 0)
                        .map(ThreadItem::new)
                        .toList();

                Observable<List<ThreadItem>> categories = repository
                        .getCategories(group.getGroupId())
                        .flatMap(Observable::from)
                        .filter(cat -> cat != null && cat.getParentCategoryId() == 0)
                        .map(ThreadItem::new)
                        .toList();

                items = Observable.zip(
                        threads, categories, (t, c) -> Lists.newArrayList(Iterables.concat(t, c)));
            } else {
                Category category = (Category) parent;

                Observable<List<ThreadItem>> threads = repository
                        .getThreads(category.getGroupId(), category.getCategoryId())
                        .flatMap(Observable::from)
                        .map(ThreadItem::new)
                        .toList();

                Observable<List<ThreadItem>> categories = repository
                        .getCategories(category.getGroupId(), category.getCategoryId())
                        .flatMap(Observable::from)
                        .map(ThreadItem::new)
                        .toList();

                items = Observable.zip(
                        threads, categories, (t, c) -> Lists.newArrayList(Iterables.concat(t, c)));
            }

            return items.observeOn(AndroidSchedulers.mainThread())
                    .map(adapter::replaceWith);
        });
    }
}
