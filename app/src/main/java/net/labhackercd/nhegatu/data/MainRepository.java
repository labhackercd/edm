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

package net.labhackercd.nhegatu.data;

import android.support.v4.util.Pair;

import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;
import net.labhackercd.nhegatu.data.rx.RxSupport;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

public class MainRepository {

    private EDMService service;

    public MainRepository(EDMService service) {
        this.service = service;
    }

    public Request<User> getUser() {
        return request0("getUser", service::getUser)
                .transform(new JsonObjectReader<>(User.JSON_READER));
    }

    public Request<User> getUser(long userId) {
        return request1("getUser", service::getUser, userId)
                .transform(new JsonObjectReader<>(User.JSON_READER));
    }

    public Request<List<Group>> getGroups(long companyId) {
        return request1("getGroups", service::getGroups, companyId)
                .transform(new JsonArrayReader<>(Group.JSON_READER))
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(g -> g != null)
                        .filter(this::isDisplayable)
                        .toList());
    }

    private boolean isDisplayable(Group group) {
        return !group.isClosed() && !group.isWebOnly() && group.isActive() && group.getType() != 2;
    }

    public Request<List<Thread>> getThreads(long groupId) {
        return request1("getThreads", service::getThreads, groupId)
                .transform(new JsonArrayReader<>(Thread.JSON_READER))
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(thread -> thread != null && thread.getCategoryId() == 0)
                        .toList());
    }

    public Request<List<Thread>> getThreads(long groupId, long categoryId) {
        return request2("getThreads", service::getThreads, groupId, categoryId)
                .transform(new JsonArrayReader<>(Thread.JSON_READER));
    }

    public Request<List<Category>> getCategories(long groupId) {
        return request1("getCategories", service::getCategories, groupId)
                .transform(new JsonArrayReader<>(Category.JSON_READER))
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(category -> category != null && category.getParentCategoryId() == 0)
                        .toList());
    }

    public Request<List<Category>> getCategories(long groupId, long categoryId) {
        return request2("getCategories", service::getCategories, groupId, categoryId)
                .transform(new JsonArrayReader<>(Category.JSON_READER));
    }

    public Request<List<Message>> getThreadMessages(long groupId, long categoryId, long threadId) {
        return request3("getThreadMessages", service::getThreadMessages, groupId, categoryId, threadId)
                .transform(new JsonArrayReader<>(Message.JSON_READER));
    }

    public Request<Message> getMessage(long messageId) {
        return request1("getMessage", service::getMessage, messageId)
                .transform(new JsonObjectReader<>(Message.JSON_READER));
    }

    /** WARNING: Black magic ahead. */

    private static <R> Request<R> request0(String id, Func0<R> callable) {
        return createRequest(callable, id);
    }

    private static <T1, R> Request<R> request1(String id, Func1<T1, R> callable, T1 arg1) {
        return createRequest(() -> callable.call(arg1), id, arg1);
    }

    private static <T1, T2, R> Request<R> request2(String id, Func2<T1, T2, R> callable,
                                                   T1 arg1, T2 arg2) {
        return createRequest(() -> callable.call(arg1, arg2), id, arg1, arg2);
    }

    private static <T1, T2, T3, R> Request<R> request3(String id, Func3<T1, T2, T3, R> callable,
                                                       T1 arg1, T2 arg2, T3 arg3) {
        return createRequest(() -> callable.call(arg1, arg2, arg3), id, arg1, arg2, arg3);
    }

    private static <T> Request<T> createRequest(Func0<T> callable, String id, Object... args) {
        final Object key = createRequestKey(id, args);
        final Observable<T> observable = createRequestObservable(callable);
        return Request.create(key, observable);
    }

    private static Object createRequestKey(String id, Object[] args) {
        switch (args.length) {
            case 0:
                return id;
            case 1:
                return new Pair<>(id, args[0]);
            default:
                return new Pair<>(id, new RequestArguments(args));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Observable<T> createRequestObservable(Func0<T> callable) {
        return RxSupport.createRequestObservable(new RxSupport.Invoker() {
            @Override
            public void invoke(Callback callback) {
                try {
                    callback.next(callable.call());
                } catch (Throwable t) {
                    callback.error(t);
                }
            }
        });
    }

    private static class RequestArguments {
        private final Object[] args;

        public RequestArguments(Object[] args) {
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof RequestArguments))
                return false;

            RequestArguments other = (RequestArguments) o;

            return Arrays.equals(args, other.args);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(args);
        }
    }

    /** FOR NARNIA! */

    private static class JsonObjectReader<T> implements Request.Transformer<JSONObject, T> {
        private final JSONReader<T> reader;

        private JsonObjectReader(JSONReader<T> reader) {
            this.reader = reader;
        }

        @Override
        public Observable<T> call(Request<JSONObject> r) {
            return r.asObservable()
                    .map(json -> fromJSON(reader, json))
                    .onErrorResumeNext(throwable -> {
                        return Observable.error(
                                throwable instanceof ConversionException
                                        ? throwable.getCause() : throwable);
                    });
        }
    }

    private static class JsonArrayReader<T> implements Request.Transformer<JSONArray, List<T>> {
        private final JSONReader<T> reader;

        private JsonArrayReader(JSONReader<T> reader) {
            this.reader = reader;
        }

        @Override
        public Observable<List<T>> call(Request<JSONArray> r) {
            return r.asObservable()
                    .map(json -> fromJSON(reader, json))
                    .onErrorResumeNext(throwable -> {
                        return Observable.error(
                                throwable instanceof ConversionException
                                        ? throwable.getCause() : throwable);
                    });
        }
    }

    private static <R> R fromJSON(JSONReader<R> reader, JSONObject json) {
        try {
            return reader.fromJSON(json);
        } catch (JSONException e) {
            throw new ConversionException(e);
        }
    }

    private static <R> List<R> fromJSON(JSONReader<R> reader, JSONArray json) {
        try {
            return reader.fromJSON(json);
        } catch (JSONException e) {
            throw new ConversionException(e);
        }
    }

    private static class ConversionException extends RuntimeException {
        private ConversionException(Throwable cause) {
            super(cause);
        }
    }
}
