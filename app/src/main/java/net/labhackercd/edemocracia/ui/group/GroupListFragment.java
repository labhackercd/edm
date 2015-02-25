package net.labhackercd.edemocracia.ui.group;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;

import de.greenrobot.event.EventBus;

public class GroupListFragment extends SimpleRecyclerViewFragment<Group> {

    @Inject User user;
    @Inject EventBus eventBus;
    @Inject EDMService service;

    @Override
    protected List<Group> blockingFetchItems() throws Exception {
        final List<Group> groups = service.getGroups(user.getCompanyId());

        /*
        TODO Identify closed groups.

        EDMBatchSession batchSession = new EDMBatchSession(session);
        EDMFixedService batchedCustomService = new EDMFixedService(batchSession);

        for (Group group : groups) {
            batchedCustomService.expandoValueGetData(group.getCompanyId(),
                    "com.liferay.portal.model.Group", "CUSTOM_FIELDS",
                    "Encerrada", group.getGroupId());
        }

        final JSONArray jsonStatuses = batchSession.invoke();
        */

        return Lists.newArrayList(Collections2.filter(groups, new Predicate<Group>() {
            @Override
            public boolean apply(@Nullable Group group) {
                if (group == null)
                    return false;

                if (!group.isActive())
                    return false;

                if (!(group.getType() == 1 || group.getType() == 3))
                    return false;

                /*
                TODO

                int idx = groups.indexOf(group);
                try {
                    return !jsonStatuses.getBoolean(idx);
                } catch (JSONException e) {
                    return false;
                }
                */
                return true;
            }
        }));
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<Group> items) {
        return new GroupListAdapter(getActivity(), eventBus, items);
    }

    @Override
    protected Object getRefreshTaskTag() {
        return new Pair<Class, Long>(getClass(), user.getCompanyId());
    }
}
