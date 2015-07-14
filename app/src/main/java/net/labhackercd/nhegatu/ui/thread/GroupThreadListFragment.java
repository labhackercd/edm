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

import android.os.Bundle;

import android.support.annotation.Nullable;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.Request;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.provider.EDMContract;

import java.util.List;

import javax.inject.Inject;

public class GroupThreadListFragment extends ThreadListFragment {

    @Inject MainRepository repository;
    private Group group;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        group = (Group) args.getSerializable(ThreadListFragment.ARG_GROUP);
        if (group == null)
            throw new IllegalArgumentException("group == null");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (group == null)
                group = (Group) savedInstanceState.getSerializable(ThreadListFragment.ARG_GROUP);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (group != null)
            outState.putSerializable(ThreadListFragment.ARG_GROUP, group);
    }

    @Override
    protected Request<List<Thread>> getListThreadsRequest() {
        return repository.getThreads(group.getGroupId());
    }

    @Override
    protected Request<List<Category>> getListCategoriesRequest() {
        return repository.getCategories(group.getGroupId());
    }

    public static boolean canHandleUriOfType(String type) {
        return EDMContract.Group.CONTENT_ITEM_TYPE.equals(type);
    }
}
