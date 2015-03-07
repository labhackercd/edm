package net.labhackercd.edemocracia.ui.group;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserData;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.RequestCache;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.rx.Operators;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.listview.ItemListView;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupListFragment extends BaseFragment {
    @Inject EventBus eventBus;
    @Inject UserData userData;
    @Inject RequestCache cache;
    @Inject MainRepository repository;

    private User user;
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

        final GroupListAdapter adapter = new GroupListAdapter(eventBus);

        Account account = AccountUtils.getAccount(getActivity());
        AccountManager manager = AccountManager.get(getActivity());

        user = userData.getUser(manager, account);

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .subscribe(listView.dataHandler());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public Observable<List<Group>> getListData(boolean fresh) {
        return Observable.just(repository.getGroups(user.getCompanyId()))
                .compose(Operators.requireAccount2(getActivity()))
                .flatMap(cache.skipIf(fresh))
                .subscribeOn(Schedulers.io());
    }
}
