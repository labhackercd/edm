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

package net.labhackercd.nhegatu.data.api.client;

import com.google.common.collect.Lists;
import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.v62.group.GroupService;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;
import com.liferay.mobile.android.v62.user.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;

import java.util.List;
import java.util.UUID;

class EDMServiceImpl implements EDMService {

    public static class Builder implements EDMService.Builder {
        private Endpoint endpoint;
        private ErrorHandler errorHandler;
        private Authentication authentication;

        public Builder() {
            this(null, null, null);
        }

        private Builder(ErrorHandler errorHandler, Endpoint endpoint, Authentication authentication) {
            this.endpoint = endpoint;
            this.errorHandler = errorHandler;
            this.authentication = authentication;
        }

        @Override
        public Builder setErrorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @Override
        public Builder setEndpoint(final Endpoint endpoint) {
            if (endpoint == null)
                throw new IllegalStateException("An endpoint must be specified.");
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public Builder setAuthentication(final Authentication authentication) {
            this.authentication = authentication;
            return this;
        }

        @Override
        public EDMService build() {
            Endpoint endpoint = this.endpoint;
            ErrorHandler errorHandler = this.errorHandler;
            Authentication authentication = this.authentication;

            if (endpoint == null)
                throw new IllegalStateException("An endpoint must be specified.");

            if (errorHandler == null)
                errorHandler = ErrorHandler.DEFAULT;

            return new EDMServiceImpl(errorHandler, endpoint,  authentication);
        }
    }

    private final SessionImpl session;
    private final ErrorHandler errorHandler;

    private EDMServiceImpl(ErrorHandler errorHandler, Endpoint endpoint, Authentication authentication) {
        this.session = new SessionImpl(endpoint, authentication);
        this.errorHandler = errorHandler;
    }

    @Override
    public Builder newBuilder() {
        return new Builder(errorHandler, session.getEndpoint(), session.getAuthentication());
    }

    @Override
    public JSONObject getUser() {
        try {
            JSONObject command = new JSONObject("{\"/user/get-user-by-id\": {}}");
            return session.invoke(command).getJSONObject(0);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONObject getUser(long userId) {
        try {
            return new UserService(session).getUserById(userId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONArray getThreads(long groupId) {
        try {
            return new MBThreadService(session).getGroupThreads(groupId, -1, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONArray getThreads(long groupId, long categoryId) {
        try {
            return new MBThreadService(session).getThreads(groupId, categoryId, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONArray getCategories(long groupId) {
        try {
            return new MBCategoryService(session).getCategories(groupId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONArray getCategories(long groupId, long categoryId) {
        try {
            return new MBCategoryService(session).getCategories(groupId, categoryId, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONArray getGroups(long companyId) {
        try {
            // TODO Move retry logic out of here.
            return new RetryHelper<JSONArray>() {
                @Override
                public JSONArray call() throws Throwable {
                    return getGroupsArray(companyId);
                }

                @Override
                protected boolean shouldRetry(Throwable t) {
                    // TODO Test retry cases. Please.
                    // TODO Move this a layer down? We could implement this retry policy at the repository level where we *will probably* have proper exception handling.
                    if (t instanceof ServerException) {
                        String message = t.getMessage().toLowerCase().trim();
                        return message.matches(".*((no *such|no *)group *exists).*");
                    }
                    return false;
                }
            }.call(3);
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
        BatchSession batch = new BatchSession(session);

        for (Long groupId : getGroupIds(companyId)) {
            String field = "groupId";
            String alias = "group" + groupId;
            batch.invoke(new JSONObject("{" +
                    "  \"$" + alias + " = /group/get-group\": {" +
                    "    \"groupId\": " + groupId + "," +

                    "    \"$closed = /expandovalue/get-data.5\": {" +
                    "      \"companyId\": " + companyId + "," +
                    "      \"className\": \"com.liferay.portal.model.Group\"," +
                    "      \"tableName\": \"CUSTOM_FIELDS\"," +
                    "      \"columnName\": \"Encerrada\"," +
                    "      \"@classPk\": \"$" + alias + "." + field  + "\"" +
                    "    }," +

                        // webOnly = !notWebOnly
                    "    \"$notWebOnly = /expandovalue/get-data.5\": {" +
                    "      \"companyId\": " + companyId + "," +
                    "      \"className\": \"com.liferay.portal.model.Group\"," +
                    "      \"tableName\": \"CUSTOM_FIELDS\"," +
                    "      \"columnName\": \"Mostrarnoapp\"," +
                    "      \"@classPk\": \"$" + alias + "." + field  + "\"" +
                    "    }," +

                        // Ordering priority
                    "    \"$priority = /expandovalue/get-data.5\": {" +
                    "      \"companyId\": " + companyId + "," +
                    "      \"className\": \"com.liferay.portal.model.Group\"," +
                    "      \"tableName\": \"CUSTOM_FIELDS\"," +
                    "      \"columnName\": \"Prioridade\"," +
                    "      \"@classPk\": \"$" + alias + "." + field  + "\"" +
                    "    }" +
                    "  }" +
                    "}"));
        }

        return batch.invoke();
    }

    private List<Long> getGroupIds(long companyId) throws Exception {
        JSONArray json = new GroupService(session).search(companyId, "%", "%", new JSONArray(), -1, -1);
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
    public JSONArray getThreadMessages(long groupId, long categoryId, long threadId) {
        try {
            return new MBMessageService(session).getThreadMessages(groupId, categoryId, threadId, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONObject addMessage(UUID uuid, long groupId, long categoryId,
                                 long threadId, long parentMessageId, String subject, String body) {
        try {
            JSONObject scArgs = new JSONObject();
            scArgs.put("addGuestPermissions", true);

            if (uuid != null)
                scArgs.put("uuid", uuid.toString());

            JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                    "com.liferay.portal.service.ServiceContext", scArgs);

            MBMessageService service = new MBMessageService(new GETSessionWrapper(session));

            return service.addMessage(
                    groupId, categoryId, threadId, parentMessageId,
                    subject, body, "bbcode", new JSONArray(), false, 0.0, true, serviceContext);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public JSONObject getMessage(long messageId) {
        try {
            return new MBMessageService(session).getMessage(messageId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private ClientError handleException(Exception error) {
        Throwable newError = errorHandler.handleError(error);
        if (newError == null) {
            throw new IllegalStateException("Error handler returned null for wrapped exception.", error);
        }
        return new ClientError(newError);
    }
}
