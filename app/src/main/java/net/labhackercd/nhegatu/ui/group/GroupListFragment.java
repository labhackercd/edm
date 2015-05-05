package net.labhackercd.nhegatu.ui.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.data.Cache;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.ui.BaseFragment;
import net.labhackercd.nhegatu.ui.listview.ItemListView;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupListFragment extends BaseFragment {
    @Inject Cache cache;
    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;

    private ItemListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ItemListView) inflater.inflate(
                R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final GroupListAdapter adapter = new GroupListAdapter(imageLoader);

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .subscribe(listView.dataHandler());
    }

    public Observable<List<Group>> getListData(boolean fresh) {
        Activity activity = getActivity();
        return AccountUtils.getCurrentUser(repository, activity)
                .flatMap(user -> repository
                        .getGroups(user.getCompanyId())
                        .transform(r -> r.asObservable()
                                .compose(AccountUtils.requireAccount(activity))
                                .compose(cache.cacheSkipIf(r.key(), fresh)))
                        .asObservable())
                .subscribeOn(Schedulers.io());
    }
}