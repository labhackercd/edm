package net.labhackercd.nhegatu.data.api;

import com.google.common.collect.Lists;
import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.group.GroupService;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;
import com.liferay.mobile.android.v62.user.UserService;

import net.labhackercd.nhegatu.data.api.client.CustomService;
import net.labhackercd.nhegatu.data.api.client.EDMBatchSession;
import net.labhackercd.nhegatu.data.api.client.EDMGetSessionWrapper;
import net.labhackercd.nhegatu.data.api.client.EDMSession;
import net.labhackercd.nhegatu.data.api.client.Endpoint;
import net.labhackercd.nhegatu.data.api.client.exception.NotFoundException;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import rx.Observable;

class EDMServiceImpl implements EDMService {


    public static class Builder implements EDMService.Builder {
        private Endpoint endpoint;
        private ErrorHandler errorHandler;
        private Authentication authentication;

        public Builder() {
            this(null, null, null);
        }

        public Builder setEndpoint(final Endpoint endpoint) {
            if (endpoint == null)
                throw new IllegalStateException("endpoint == null");
            this.endpoint = endpoint;
            return this;
        }

        public Builder setAuthentication(final Authentication authentication) {
            this.authentication = authentication;
            return this;
        }

        public Builder setErrorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public EDMService build() {
            Endpoint endpoint = this.endpoint;
            ErrorHandler errorHandler = this.errorHandler;
            Authentication authentication = this.authentication;

            if (endpoint == null)
                throw new IllegalStateException("A Endpoint must be specified.");

            if (errorHandler == null)
                errorHandler = ErrorHandler.DEFAULT;

            return new EDMServiceImpl(endpoint, errorHandler, authentication);
        }

        private Builder(Endpoint endpoint, ErrorHandler errorHandler, Authentication authentication) {
            this.endpoint = endpoint;
            this.errorHandler = errorHandler;
            this.authentication = authentication;
        }
    }

    private final Session session;
    private final Endpoint endpoint;
    private final ErrorHandler errorHandler;
    private final Authentication authentication;

    private EDMServiceImpl(Endpoint endpoint, ErrorHandler errorHandler, Authentication authentication) {
        this.endpoint = endpoint;
        this.errorHandler = errorHandler;
        this.authentication = authentication;
        this.session = new EDMSession(endpoint, authentication);
    }

    public Builder newBuilder() {
        return new Builder(endpoint, errorHandler, authentication);
    }

    private ServiceError handleException(Exception e) {
        // TODO Properly turn the Exception into a ServiceError, identify Exception types, etc.
        ServiceError error = new ServiceError(e);
        Throwable t = handleError(error);
        if (t instanceof ServiceError)
            return (ServiceError) t;
        else
            return new ServiceError(t);
    }

    private Throwable handleError(ServiceError error) {
        Throwable throwable = errorHandler.handleError(error);
        if (throwable == null) {
            throwable = new IllegalStateException(
                    "Error handler returned null for wrapped exception.", error);
        }
        return throwable;
    }

    private UserService userService;
    private GroupService groupService;
    private CustomService customService;
    private MBThreadService threadService;
    private MBMessageService messageService;
    private MBCategoryService categoryService;

    private GroupService getGroupService() {
        if (groupService == null)
            groupService = new GroupService(session);
        return groupService;
    }

    private MBThreadService getThreadService() {
        if (threadService == null)
            threadService = new MBThreadService(session);
        return threadService;
    }

    private MBMessageService getMessageService() {
        if (messageService == null)
            messageService = new MBMessageService(session);
        return messageService;
    }

    private MBCategoryService getCategoryService() {
        if (categoryService == null)
            categoryService = new MBCategoryService(session);
        return categoryService;
    }

    private CustomService getCustomService() {
        if (customService == null)
            customService = new CustomService(session);
        return customService;
    }

    private UserService getUserService() {
        if (userService == null)
            userService = new UserService(session);
        return userService;
    }

    @Override
    public User getUser() {
        try {
            JSONObject command = new JSONObject("{\"/user/get-user-by-id\": {}}");
            JSONObject json = session.invoke(command).getJSONObject(0);
            return json == null ? null : User.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public User getUser(long userId) {
        try {
            JSONObject json = getUserService().getUserById(userId);
            return json == null ? null : User.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Thread> getThreads(long groupId) {
        try {
            JSONArray json = getThreadService().getGroupThreads(groupId, -1, 0, -1, -1);
            return json == null ? null : Thread.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Thread> getThreads(long groupId, long categoryId) {
        try {
            JSONArray json = getThreadService().getThreads(groupId, categoryId, 0, -1, -1);
            return json == null ? null : Thread.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Category> getCategories(long groupId) {
        try {
            JSONArray json = getCategoryService().getCategories(groupId);
            return json == null ? null : Category.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Category> getCategories(long groupId, long categoryId) {
        try {
            JSONArray json = getCategoryService().getCategories(groupId, categoryId, -1, -1);
            return json == null ? null : Category.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Group> getGroups(long companyId) {
        try {
            JSONArray json = new RetryHelper<JSONArray>() {
                @Override
                public JSONArray call() throws Throwable {
                    return getGroupsArray(companyId);
                }

                @Override
                protected boolean shouldRetry(Throwable t) {
                    return (t instanceof NotFoundException
                            && t.getMessage().toLowerCase().contains("group"));
                }
            }.call(3);
            return json == null ? null : Group.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private static abstract class RetryHelper<T> {
        public abstract T call() throws Throwable;

        public T call(int attempts) {
            RuntimeException lastException = null;
            for (int i = 0; i < attempts; i++) {
                try {
                    return call();
                } catch (Throwable t) {
                    lastException = handleException(t);
                    if (!shouldRetry(t))
                        break;
                }
            }
            throw lastException;
        }

        protected boolean shouldRetry(Throwable t) {
            return true;
        }

        private RuntimeException handleException(Throwable t) {
            return new RuntimeException(t);
        }
    }

    private JSONArray getGroupsArray(long companyId) throws Exception {
        EDMBatchSession batch = new EDMBatchSession(session);

        for (Long groupId : getGroupIds(companyId)) {
            String field = "groupId";
            String alias = "group" + groupId;
            batch.invoke(new JSONObject("{" +
                    "  \"$" + alias + " = /group/get-group\": {" +
                    "    \"groupId\": " + groupId + "," +
                    "    \"$" + Group.CLOSED + " = /expandovalue/get-data.5\": {" +
                    "      \"companyId\": " + companyId + "," +
                    "      \"className\": \"com.liferay.portal.model.Group\"," +
                    "      \"tableName\": \"CUSTOM_FIELDS\"," +
                    "      \"columnName\": \"Encerrada\"," +
                    "      \"@classPk\": \"$" + alias + "." + field  + "\"" +
                    "    }" +
                    "  }" +
                    "}"));
        }

        return batch.invoke();
    }

    private List<Long> getGroupIds(long companyId) throws Exception {
        JSONArray json = getGroupService().search(companyId, "%", "%", new JSONArray(), -1, -1);
        return Lists.newArrayList(
                createObjectObservable(json)
                        .map(o -> o.optLong("groupId"))
                        .filter(id -> id > 0)
                        .toBlocking()
                        .toIterable());
    }

    private static Observable<JSONObject> createObjectObservable(JSONArray json) {
        return Observable.create(f -> {
            for (int i = 0; i < json.length(); i++) {
                if (f.isUnsubscribed()) {
                    break;
                } else {
                    try {
                        f.onNext(json.getJSONObject(i));
                    } catch (JSONException e) {
                        f.onError(e);
                    }
                }
            }
            if (!f.isUnsubscribed())
                f.onCompleted();
        });
    }

    @Override
    public List<Message> getThreadMessages(long groupId, long categoryId, long threadId) {
        try {
            JSONArray json = getMessageService().getThreadMessages(
                    groupId, categoryId, threadId, 0, -1, -1);
            return json == null ? null : Message.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Message addMessage(UUID uuid, long groupId, long categoryId, long threadId,
                              long parentMessageId, String subject, String body) {
        try {
            JSONObject scArgs = new JSONObject();
            scArgs.put("addGuestPermissions", true);

            if (uuid != null)
                scArgs.put("uuid", uuid.toString());

            JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                    "com.liferay.portal.service.ServiceContext", scArgs);

            MBMessageService service = new MBMessageService(new EDMGetSessionWrapper(session));

            JSONObject inserted = service.addMessage(
                    groupId, categoryId, threadId, parentMessageId,
                    subject, body, "bbcode", new JSONArray(), false, 0.0, true, serviceContext);

            return Message.JSON_READER.fromJSON(inserted);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
