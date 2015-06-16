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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public interface EDMService {

    interface Builder {
        Builder setErrorHandler(ErrorHandler errorHandler);
        Builder setEndpoint(Endpoint endpoint);
        Builder setAuthentication(Authentication authentication);
        EDMService build();
    }

    Builder newBuilder();

    JSONObject getUser();

    JSONObject getUser(long userId);

    JSONArray getThreads(long groupId);

    JSONArray getThreads(long groupId, long categoryId);

    JSONArray getCategories(long groupId);

    JSONArray getCategories(long groupId, long categoryId);

    JSONArray getGroups(long companyId);

    JSONArray getThreadMessages(long groupId, long categoryId, long threadId);

    JSONObject addMessage(UUID uuid, long groupId, long categoryId,
                          long threadId, long parentMessageId, String subject, String body);

    JSONObject getMessage(long messageId);
}
