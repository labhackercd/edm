package net.labhackercd.edemocracia.ui.thread;

import android.os.Bundle;

import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.Request;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.provider.EDMContract;

import java.util.List;

import javax.inject.Inject;

public class CategoryThreadListFragment extends ThreadListFragment {

    @Inject MainRepository repository;
    private Category category;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        category = (Category) args.getSerializable(ThreadListFragment.ARG_CATEGORY);
        if (category == null)
            throw new IllegalArgumentException("category == null");
    }

    @Override
    protected Request<List<Thread>> getListThreadsRequest() {
        return repository.getThreads(category.getGroupId(), category.getCategoryId());
    }

    @Override
    protected Request<List<Category>> getListCategoriesRequest() {
        return repository.getCategories(category.getGroupId(), category.getCategoryId());
    }

    public static boolean canHandleUriOfType(String type) {
        return EDMContract.Category.CONTENT_ITEM_TYPE.equals(type);
    }
}
