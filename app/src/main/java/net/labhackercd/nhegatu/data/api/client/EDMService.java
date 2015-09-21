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

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.service.BatchSessionImpl;
import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.v62.group.GroupService;
import com.liferay.mobile.android.v62.mbcategory.MBCategoryService;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;
import com.liferay.mobile.android.v62.user.UserService;
import net.labhackercd.nhegatu.data.api.client.error.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;


public class EDMService {

    private static final JSONObject GET_CURRENT_USER_COMMAND;
    static {
        try {
            GET_CURRENT_USER_COMMAND = new JSONObject("{\"/user/get-user-by-id\": {}}");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private final Session session;

    private EDMService(Session session) {
        this.session = session;
    }

    public JSONObject getUser() throws ServiceError {
        try {
            return session.invoke(GET_CURRENT_USER_COMMAND).getJSONObject(0);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONObject getUser(long userId) throws ServiceError {
        try {
            return new UserService(session).getUserById(userId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONArray getThreads(long groupId) throws ServiceError {
        try {
            return new MBThreadService(session).getGroupThreads(groupId, -1, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONArray getThreads(long groupId, long categoryId) throws ServiceError {
        try {
            return new MBThreadService(session).getThreads(groupId, categoryId, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONArray getCategories(long groupId) throws ServiceError {
        try {
            return new MBCategoryService(session).getCategories(groupId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONArray getCategories(long groupId, long categoryId) throws ServiceError {
        try {
            return new MBCategoryService(session).getCategories(groupId, categoryId, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONArray getGroups(long companyId) throws ServiceError, BatchedRequestFailure {
        BatchSessionImpl batch = new BatchSessionImpl(session);

        JSONArray groups;
        try {
            groups = new GroupService(session).search(companyId, "%", "%", new JSONArray(), -1, -1);
        } catch (Exception e) {
            throw new ServiceError(e);
        }

        for (int i = 0; i < groups.length(); i++) {
            JSONObject group;
            try {
                group = groups.getJSONObject(i);
            } catch (JSONException e) {
                throw new BatchedRequestFailure(e);
            }

            long groupId = group.optLong("groupId");

            try {
                batch.invoke(getGroupCommand(companyId, groupId));
            } catch (Exception e) {
                throw new BatchedRequestFailure(e);
            }
        }

        try {
            return batch.invoke();
        } catch (Exception exception) {
            ServiceError error = handleException(exception);
            if (error instanceof NotFoundException) {
                throw new BatchedRequestFailure(exception);
            } else {
                throw error;
            }
        }
    }

    private JSONObject getGroupCommand(long companyId, long groupId) throws JSONException {
        String alias = "group" + groupId;
        return new JSONObject("{" +
                "  \"$" + alias + " = /group/get-group\": {" +
                "    \"groupId\": " + groupId + "," +

                "    \"$closed = /expandovalue/get-data.5\": {" +
                "      \"companyId\": " + companyId + "," +
                "      \"className\": \"com.liferay.portal.model.Group\"," +
                "      \"tableName\": \"CUSTOM_FIELDS\"," +
                "      \"columnName\": \"Encerrada\"," +
                "      \"@classPk\": \"$" + alias + ".groupId\"" +
                "    }," +

                // webOnly = !notWebOnly
                "    \"$notWebOnly = /expandovalue/get-data.5\": {" +
                "      \"companyId\": " + companyId + "," +
                "      \"className\": \"com.liferay.portal.model.Group\"," +
                "      \"tableName\": \"CUSTOM_FIELDS\"," +
                "      \"columnName\": \"Mostrarnoapp\"," +
                "      \"@classPk\": \"$" + alias + ".groupId\"" +
                "    }," +

                // Ordering priority
                "    \"$priority = /expandovalue/get-data.5\": {" +
                "      \"companyId\": " + companyId + "," +
                "      \"className\": \"com.liferay.portal.model.Group\"," +
                "      \"tableName\": \"CUSTOM_FIELDS\"," +
                "      \"columnName\": \"Prioridade\"," +
                "      \"@classPk\": \"$" + alias + ".groupId\"" +
                "    }" +
                "  }" +
                "}");
    }

    public JSONArray getThreadMessages(long groupId,
                                       long categoryId, long threadId) throws ServiceError {
        try {
            return new MBMessageService(session).getThreadMessages(groupId, categoryId, threadId, 0, -1, -1);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONObject addMessage(
            UUID uuid, long groupId, long categoryId,
            long threadId, long parentMessageId, String subject, String body) throws ServiceError {

        JSONObject contextArgs;
        try {
            contextArgs = new JSONObject();
            contextArgs.put("addGuestPermissions", true);

            if (uuid != null)
                contextArgs.put("uuid", uuid.toString());
        } catch (JSONException e) {
            throw new RuntimeException("This was not supposed to happen.", e);
        }

        JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                "com.liferay.portal.service.ServiceContext", contextArgs);

        MBMessageService service = new MBMessageService(new GETSessionWrapper(session));

        try {
            return service.addMessage(
                    groupId, categoryId, threadId, parentMessageId,
                    subject, body, "bbcode", new JSONArray(), false, 0.0, true, serviceContext);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public JSONObject getMessage(long messageId) throws ServiceError {
        try {
            return new MBMessageService(session).getMessage(messageId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private ServiceError handleException(Exception error) {
        if (error instanceof IOException) {
            return new ProbablyNetworkError(error);
        } else if (error instanceof ServerException) {
            String message = error.getMessage().toLowerCase().trim();
            if (message.matches(".*(no *such|no *\\w+ *exists).*")) {
                return new NotFoundException(error);
            } else if (message.matches(".*(please *sign|authenticated *access|authentication *failed).*")) {
                return new AuthorizationException(error);
            }
        }
        return new ServiceError(error);
    }

    public Builder newBuilder() {
        return new Builder(session.getServer(), session.getAuthentication());
    }

    public static class Builder {
        private String baseUrl;
        private Authentication authentication;

        public Builder() {
            this(null, null);
        }

        private Builder(String baseUrl, Authentication authentication) {
            this.baseUrl = baseUrl;
            this.authentication = authentication;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setAuthentication(Authentication authentication) {
            this.authentication = authentication;
            return this;
        }

        public EDMService build() {
            return new EDMService(new SessionImpl(baseUrl, authentication));
        }
    }
}
