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
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.BaseFragment;
import net.labhackercd.edemocracia.ui.UberRecyclerView;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class GroupListFragment extends BaseFragment {
    @Inject EventBus eventBus;
    @Inject UserData userData;
    @Inject DataRepository repository;

    private User user;
    private UberRecyclerView uberRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uberRecyclerView = (UberRecyclerView) inflater.inflate(
                R.layout.uber_recycler_view, container, false);
        return uberRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final GroupListAdapter adapter = new GroupListAdapter(eventBus);

        Account account = AccountUtils.getAccount(getActivity());
        AccountManager manager = AccountManager.get(getActivity());

        user = userData.getUser(manager, account);

        uberRecyclerView.refreshEvents()
                .forEach(fresh -> {
                    uberRecyclerView.setRefreshing(true);
                    repository.getGroups(user.getCompanyId())
                            .take(fresh ? 2 : 1).last()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(adapter::replaceWith)
                            .subscribe(uberRecyclerView.dataHandler());
                });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public Observable<List<Group>> refresh(boolean fresh) {
        return repository.getGroups(user.getCompanyId())
                .take(fresh ? 2 : 1).last();
    }
}
