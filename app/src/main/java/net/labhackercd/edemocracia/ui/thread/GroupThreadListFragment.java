package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;

import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.Request;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.provider.EDMContract;

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
