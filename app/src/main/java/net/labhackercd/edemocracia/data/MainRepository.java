package net.labhackercd.edemocracia.data;

import android.support.v4.util.Pair;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.rx.RxSupport;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

public class MainRepository {

    public static interface Request<T> {
        public Object request();
        public Observable<T> observable();
    }

    private EDMService service;

    public MainRepository(EDMService service) {
        this.service = service;
    }

    public Request<User> getUser() {
        return request0("getUser", service::getUser);
    }

    public Request<User> getUserWithCredentials(String email, String password) {
        EDMService newService = service.newBuilder()
                .setAuthentication(new BasicAuthentication(email, password))
                .build();
        return request0("getUserWithCredentials", newService::getUser);
    }

    public Request<List<Group>> getGroups(long companyId) {
        return request1("getGroups", service::getGroups, companyId);
    }

    public Request<List<Thread>> getThreads(long groupId) {
        Request<List<Thread>> request = request1("getThreads", service::getThreads, groupId);
        return transform(request, observable -> observable
                .flatMap(Observable::from)
                .filter(thread -> thread != null && thread.getCategoryId() == 0)
                .toList());
    }

    public Request<List<Thread>> getThreads(long groupId, long categoryId) {
        return request2("getThreads", service::getThreads, groupId, categoryId);
    }

    public Request<List<Category>> getCategories(long groupId) {
        Request<List<Category>> request = request1("getCategories", service::getCategories, groupId);
        return transform(request, observable -> observable
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

    public static <T> Request<T> transform(Request<T> request, Observable.Transformer<T, T> transformer) {
        return new Request<T>() {
            @Override
            public Object request() {
                return request.request();
            }

            @Override
            public Observable<T> observable() {
                return request.observable().compose(transformer);
            }
        };
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
        return new Request<T>() {
            @Override
            public Object request() {
                return key;
            }

            @Override
            public Observable<T> observable() {
                return observable;
            }
        };
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
