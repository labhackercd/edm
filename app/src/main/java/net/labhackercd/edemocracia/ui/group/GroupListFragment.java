package net.labhackercd.edemocracia.ui.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.DataRepository;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.BaseActivity;
import net.labhackercd.edemocracia.ui.UberRecyclerView;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;

public class GroupListFragment extends Fragment {
    @Inject User user;
    @Inject EventBus eventBus;
    @Inject DataRepository repository;

    private UberRecyclerView uberRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uberRecyclerView = (UberRecyclerView) inflater.inflate(
                R.layout.uber_recycler_view, container, false);
        return uberRecyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BaseActivity.get2(getActivity()).getObjectGraph().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final GroupListAdapter adapter = new GroupListAdapter(eventBus);

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
}
