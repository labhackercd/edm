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

import android.accounts.Account;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountManager;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.api.TypedService;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.ui.BaseFragment;
import net.labhackercd.nhegatu.ui.listview.ItemListView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupListFragment extends BaseFragment {
    @Inject Account account;
    @Inject TypedService service;
    @Inject ImageLoader imageLoader;
    @Inject AccountManager accountManager;

    private ItemListView listView;
    private GroupListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ItemListView) inflater.inflate(
                R.layout.item_list_view, container, false);
        return listView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupListAdapter(imageLoader);
    }

    @Override
    public void onResume() {
        super.onResume();

        listView.refreshEvents()
                .doOnNext(fresh -> {
                    if (!listView.isEnabled()) {
                        listView.setRefreshing(true);
                    }
                })
                .startWith(false)
                .flatMap(this::getListData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(adapter::replaceWith)
                .subscribe(listView.dataHandler());
    }

    public Observable<List<Group>> getListData(boolean fresh) {
        return service.getUser()
                .flatMap(user -> service.getGroups(user.getCompanyId()))
                .map(groups -> {
                    // Sort groups by priority.
                    Collections.sort(groups, (a, b) -> b.getPriority() - a.getPriority());
                    return groups;
                })
                .subscribeOn(Schedulers.io());
    }
}
