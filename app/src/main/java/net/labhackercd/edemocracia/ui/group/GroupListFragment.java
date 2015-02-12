package net.labhackercd.edemocracia.ui.group;

import android.support.v7.widget.RecyclerView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.labhackercd.edemocracia.data.model.Group;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.data.api.GroupService;

import de.greenrobot.event.EventBus;

public class GroupListFragment extends SimpleRecyclerViewFragment<Group> {

    @Inject Picasso picasso;
    @Inject EventBus eventBus;
    @Inject GroupService groupService;

    @Override
    protected List<Group> blockingFetchItems() throws Exception {
        // FIXME Remove this hardcoded companyId from here.
        List<Group> groups = groupService.search("%", "%", Collections.emptyList(), -1, -1);
        return Lists.newArrayList(Collections2.filter(groups, new Predicate<Group>() {
            @Override
            public boolean apply(@Nullable Group group) {
                return group != null && group.isActive() && (group.getType() == 1 || group.getType() == 3);
            }
        }));
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<Group> items) {
        return new GroupListAdapter(getActivity(), picasso, eventBus, items);
    }
}