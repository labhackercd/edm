/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.ui.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.data.cache.Cache;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.cache.LHMCache;
import net.labhackercd.nhegatu.data.cache.UserCache;
import net.labhackercd.nhegatu.ui.BaseFragment;
import net.labhackercd.nhegatu.ui.Util;
import net.labhackercd.nhegatu.ui.listview.ItemListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupListFragment extends BaseFragment {
    @Inject LHMCache cache;
    @Inject UserCache userCache;
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .subscribe(listView.dataHandler());
    }

    public Observable<List<Group>> getListData(boolean fresh) {
        Activity activity = getActivity();
        return AccountUtils.getCurrentUser(repository, activity, userCache)
                .flatMap(user -> repository.getGroups(user.getCompanyId())
                        .transform(r -> r.asObservable()
                                .map(this::sortListData)
                                .compose(AccountUtils.requireAccount(activity))
                                .compose(Util.applyCache(cache, r.key(), fresh)))
                        .asObservable()
                        .subscribeOn(Schedulers.io()));
    }

    private List<Group> sortListData(List<Group> groups) {
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group a, Group b) {
                return b.getPriority() - a.getPriority();
            }
        });
        return groups;
    }
}
