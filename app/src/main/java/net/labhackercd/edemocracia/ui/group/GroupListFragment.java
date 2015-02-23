package net.labhackercd.edemocracia.ui.group;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.v62.group.GroupService;

import org.json.JSONArray;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.labhackercd.edemocracia.data.model.Group;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.data.api.EDMSession;
import net.labhackercd.edemocracia.data.model.util.JSONReader;

import de.greenrobot.event.EventBus;

public class GroupListFragment extends SimpleRecyclerViewFragment<Group> {

    @Inject EventBus eventBus;
    @Inject EDMSession session;

    @Override
    protected List<Group> blockingFetchItems() throws Exception {
        GroupService groupService = new GroupService(session);

        JSONArray jsonGroups = groupService.search(
                session.getCompanyId(), "%", "%", new JSONArray(), -1, -1);

        if (jsonGroups == null) {
            jsonGroups = new JSONArray();
        }

        List<Group> groups = JSONReader.fromJSON(jsonGroups, Group.JSON_READER);

        return Lists.newArrayList(Collections2.filter(groups, new Predicate<Group>() {
            @Override
            public boolean apply(@Nullable Group group) {
                return group != null && group.isActive() && (group.getType() == 1 || group.getType() == 3);
            }
        }));
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<Group> items) {
        return new GroupListAdapter(getActivity(), eventBus, items);
    }

    @Override
    protected Object getRefreshTaskTag() {
        return new Pair<Class, Long>(getClass(), session.getCompanyId());
    }
}
