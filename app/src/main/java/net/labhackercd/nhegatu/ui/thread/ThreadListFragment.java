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

package net.labhackercd.nhegatu.ui.thread;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.Request;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.cache.LHMCache;
import net.labhackercd.nhegatu.ui.Util;
import net.labhackercd.nhegatu.ui.listview.ItemListView;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class ThreadListFragment extends Fragment {

    private static final String ARG_DATA =
            ThreadListFragment.class.getCanonicalName().concat(".arg.data");
    protected static final String ARG_GROUP =
            ThreadListFragment.class.getCanonicalName().concat(".arg.group");
    protected static final String ARG_CATEGORY =
            ThreadListFragment.class.getCanonicalName().concat(".arg.category");

    @Inject LHMCache cache;
    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;

    private ItemListView listView;

    public static Fragment newInstance(Group group) {
        ThreadListFragment fragment = new GroupThreadListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_GROUP, group);

        fragment.setArguments(args);

        return fragment;
    }

    public static Fragment newInstance(Category category) {
        ThreadListFragment fragment = new CategoryThreadListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);

        fragment.setArguments(args);

        return fragment;
    }

    public static Fragment newInstance(Context context, Uri uri) {
        String type = context.getContentResolver().getType(uri);

        ThreadListFragment fragment;
        if (GroupThreadListFragment.canHandleUriOfType(type))
            fragment = new GroupThreadListFragment();
        else if (CategoryThreadListFragment.canHandleUriOfType(type))
            fragment = new CategoryThreadListFragment();
        else
            throw new IllegalArgumentException("Can't display content of type " + type);

        Bundle args = new Bundle();
        args.putParcelable(ARG_DATA, uri);

        fragment.setArguments(args);

        return fragment;
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

        final ThreadListAdapter adapter = new ThreadListAdapter(imageLoader);

        listView.refreshEvents()
                .startWith(false)
                .doOnNext(fresh -> listView.setRefreshing(true))
                .flatMap(this::getListData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(pair -> adapter.replaceWith(pair.first, pair.second))
                .subscribe(listView.dataHandler());
    }

    private Observable<Pair<List<Category>, List<ThreadListAdapter.ThreadItem>>> getListData(boolean fresh) {
        Activity activity = getActivity();

        Observable<List<ThreadListAdapter.ThreadItem>> t = getListThreadsRequest()
                .transform(r -> r.asObservable()
                        .compose(Util.applyCache(cache, r.key(), fresh))
                        .subscribeOn(Schedulers.io()))
                .asObservable()
                .map(list -> Observable.from(list)
                        .map(thread -> create(activity, thread, fresh))
                        .toList().toBlocking().single());

        Observable<List<Category>> c = getListCategoriesRequest()
                .transform(r -> r.asObservable()
                        .compose(Util.applyCache(cache, r.key(), fresh))
                        .subscribeOn(Schedulers.io()))
                .asObservable();

        return Observable.zip(c, t, Pair::new);
    }

    private ThreadListAdapter.ThreadItem create(final Activity activity, final Thread thread, final boolean fresh) {
        return new ThreadListAdapter.ThreadItem() {
            @Override
            public Thread getThread() {
                return thread;
            }

            @Override
            public Observable<Message> getRootMessage() {
                return repository.getMessage(thread.getRootMessageId())
                        .transform(r -> r.asObservable()
                                .compose(Util.applyCache(cache, r.key())))
                        .asObservable();
            }
        };
    }

    protected abstract Request<List<Thread>> getListThreadsRequest();
    protected abstract Request<List<Category>> getListCategoriesRequest();
}
