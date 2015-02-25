package net.labhackercd.edemocracia.data.api;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.group.GroupService;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import net.labhackercd.edemocracia.data.api.client.EDMGetSessionWrapper;
import net.labhackercd.edemocracia.data.api.client.EDMSession;
import net.labhackercd.edemocracia.data.api.client.Endpoint;
import net.labhackercd.edemocracia.data.api.model.*;
import net.labhackercd.edemocracia.data.api.model.Thread;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

class EDMServiceImpl implements EDMService {

    public static class Builder {
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

    private GroupService groupService;
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
            JSONArray json = getGroupService().search(
                    companyId, "%", "%", new JSONArray(), -1, -1);
            return json == null ? null : Group.JSON_READER.fromJSON(json);
        } catch (Exception e) {
            throw handleException(e);
        }
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
    public Message addMessage(Message message) {
        try {
            JSONObject scArgs = new JSONObject();
            scArgs.put("addGuestPermissions", true);

            String uuid = message.getUuid();
            if (uuid != null)
                scArgs.put("uuid", uuid);

            JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                    "com.liferay.portal.service.ServiceContext", scArgs);

            MBMessageService service = new MBMessageService(new EDMGetSessionWrapper(session));

            JSONObject inserted = service.addMessage(
                    message.getGroupId(), message.getCategoryId(), message.getThreadId(),
                    message.getParentMessageId(), message.getSubject(), message.getBody(),
                    message.getFormat(), new JSONArray(), message.isAnonymous(),
                    message.getPriority(), message.allowPingbacks(), serviceContext);

            return Message.JSON_READER.fromJSON(inserted);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
