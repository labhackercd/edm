package net.labhackercd.edemocracia.ui.group;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.group.GroupService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.labhackercd.edemocracia.data.api.EDMBatchSession;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;

import de.greenrobot.event.EventBus;

public class GroupListFragment extends SimpleRecyclerViewFragment<Group> {

    @Inject User user;
    @Inject Session session;
    @Inject EventBus eventBus;

    @Override
    protected List<Group> blockingFetchItems() throws Exception {
        GroupService groupService = new GroupService(session);

        JSONArray jsonGroups = groupService.search(
                user.getCompanyId(),"%", "%", new JSONArray(), -1, -1);

        if (jsonGroups == null) {
            jsonGroups = new JSONArray();
        }

        final List<Group> groups = Group.JSON_READER.fromJSON(jsonGroups);

        EDMBatchSession batchSession = new EDMBatchSession(session);
        EDMService batchedCustomService = new EDMService(batchSession);

        for (Group group : groups) {
            batchedCustomService.expandoValueGetData(group.getCompanyId(),
                    "com.liferay.portal.model.Group", "CUSTOM_FIELDS",
                    "Encerrada", group.getGroupId());
        }

        final JSONArray jsonStatuses = batchSession.invoke();

        return Lists.newArrayList(Collections2.filter(groups, new Predicate<Group>() {
            @Override
            public boolean apply(@Nullable Group group) {
                if (group == null)
                    return false;

                if (!group.isActive())
                    return false;

                if (!(group.getType() == 1 || group.getType() == 3))
                    return false;

                int idx = groups.indexOf(group);
                try {
                    return !jsonStatuses.getBoolean(idx);
                } catch (JSONException e) {
                    return false;
                }
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
