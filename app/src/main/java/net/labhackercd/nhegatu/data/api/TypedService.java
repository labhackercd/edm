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

package net.labhackercd.nhegatu.data.api;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.liferay.mobile.android.auth.Authentication;
import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.api.model.*;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;

import java.util.List;

public class TypedService {

    private final EDMService service;

    private TypedService(EDMService service) {
        this.service = service;
    }

    public static class Builder {
        private final EDMService.Builder serviceBuilder;

        public Builder() {
            this(new EDMService.Builder());
        }

        private Builder(EDMService.Builder serviceBuilder) {
            this.serviceBuilder = serviceBuilder;
        }

        public Builder setBaseUrl(String url) {
            serviceBuilder.setBaseUrl(url);
            return this;
        }

        public Builder setAuthentication(Authentication authentication) {
            serviceBuilder.setAuthentication(authentication);
            return this;
        }

        public TypedService build() {
            return new TypedService(serviceBuilder.build());
        }
    }

    public Builder newBuilder() {
        return new Builder(service.newBuilder());
    }

    public Observable<User> getUser() {
        return make(service::getUser).compose(readObject(User.JSON_READER));
    }

    public Observable<User> getUser(long userId) {
        return make(() -> service.getUser(userId)).compose(readObject(User.JSON_READER));
    }

    public Observable<List<Group>> getGroups(long companyId) {
        return make(() -> service.getGroups(companyId))
                .compose(readArray(Group.JSON_READER))
                .map(groups -> Observable.from(groups)
                        .filter(TypedService::isDisplayable)
                        .toList().toBlocking().first());
    }

    private static boolean isDisplayable(Group group) {
        return !group.isClosed() && !group.isWebOnly() && group.isActive() && group.getType() != 2;
    }

    public Observable<List<Thread>> getThreads(long groupId) {
        return make(() -> service.getThreads(groupId))
                .compose(readArray(Thread.JSON_READER))
                .map(threads -> Lists.newArrayList(
                        Collections2.filter(threads,
                                thread -> thread.getCategoryId() == 0)));
    }

    public Observable<List<Thread>> getThreads(long groupId, long categoryId) {
        return make(() -> service.getThreads(groupId, categoryId))
                .compose(readArray(Thread.JSON_READER));
    }

    public Observable<List<Category>> getCategories(long groupId) {
        return make(() -> service.getCategories(groupId))
                .compose(readArray(Category.JSON_READER))
                .map(categories -> Lists.newArrayList(
                        Collections2.filter(categories,
                                category -> category.getParentCategoryId() == 0)));
    }

    public Observable<List<Category>> getCategories(long groupId, long categoryId) {
        return make(() -> service.getCategories(groupId, categoryId))
                .compose(readArray(Category.JSON_READER));
    }

    public Observable<List<Message>> getThreadMessages(long groupId, long categoryId, long threadId) {
        return make(() -> service.getThreadMessages(groupId, categoryId, threadId))
                .compose(readArray(Message.JSON_READER));
    }

    public Observable<Message> getMessage(long messageId) {
        return make(() -> service.getMessage(messageId))
                .compose(readObject(Message.JSON_READER));
    }

    private static <T> Observable<T> make(Func0<T> request) {
        return Observable.defer(() -> {
            try {
                return Observable.just(request.call());
            } catch (Throwable t) {
                return Observable.error(t);
            }
        });
    }

    private static <T> Observable.Transformer<JSONObject, T> readObject(JSONReader<T> reader) {
        return observable -> observable
                .map(json -> {
                    try {
                        return reader.fromJSON(json);
                    } catch (JSONException e) {
                        throw OnErrorThrowable.from(e);
                    }
                });
    }

    private static <T> Observable.Transformer<JSONArray, List<T>> readArray(JSONReader<T> reader) {
        return observable -> observable
                .map(json -> {
                    try {
                        return reader.fromJSON(json);
                    } catch (JSONException e) {
                        throw OnErrorThrowable.from(e);
                    }
                });
    }
}
