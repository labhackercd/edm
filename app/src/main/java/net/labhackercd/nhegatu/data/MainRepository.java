package net.labhackercd.nhegatu.data;

import android.support.v4.util.Pair;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.nhegatu.data.api.EDMService;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;
import net.labhackercd.nhegatu.data.rx.RxSupport;

import java.util.Arrays;
import java.util.List;

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
        return request0("getUser", service::getUser);
    }

    public Request<User> getUser(long userId) {
        return request1("getUser", service::getUser, userId);
    }

    public Request<User> getUserWithCredentials(String email, String password) {
        EDMService newService = service.newBuilder()
                .setAuthentication(new BasicAuthentication(email, password))
                .build();
        return request0("getUserWithCredentials", newService::getUser);
    }

    public Request<List<Group>> getGroups(long companyId) {
        return request1("getGroups", service::getGroups, companyId)
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(group -> group != null)
                        .filter(group -> group.isActive())
                        .filter(group -> !group.isClosed())
                        // TODO Define a proper policy for *unlisted groups*?
                        .filter(group -> group.getType() != 2)
                        .toList());
    }

    public Request<List<Thread>> getThreads(long groupId) {
        return request1("getThreads", service::getThreads, groupId)
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(thread -> thread != null && thread.getCategoryId() == 0)
                        .toList());
    }

    public Request<List<Thread>> getThreads(long groupId, long categoryId) {
        return request2("getThreads", service::getThreads, groupId, categoryId);
    }

    public Request<List<Category>> getCategories(long groupId) {
        return request1("getCategories", service::getCategories, groupId)
                .transform(r -> r.asObservable()
                        .flatMap(Observable::from)
                        .filter(category -> category != null && category.getParentCategoryId() == 0)
                        .toList());
    }

    public Request<List<Category>> getCategories(long groupId, long categoryId) {
        return request2("getCategories", service::getCategories, groupId, categoryId);
    }

    public Request<List<Message>> getThreadMessages(Thread thread) {
        return getThreadMessages(
                thread.getGroupId(), thread.getCategoryId(), thread.getThreadId());
    }

    public Request<List<Message>> getThreadMessages(long groupId, long categoryId, long threadId) {
        return request3("getThreadMessages", service::getThreadMessages,
                groupId, categoryId, threadId);
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
}
