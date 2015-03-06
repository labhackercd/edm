package net.labhackercd.edemocracia.data;

import android.support.v4.util.Pair;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * A repository containing many stores.
 */
public class DataRepository {
    private final EDMService service;

    @Inject public DataRepository(EDMService service) {
        this.service = service;
    }

    public Observable<User> getUser() {
        return userStore.get(null);
    }

    public Observable<List<Group>> getGroups(long companyId) {
        return groupStore.get(companyId);
    }

    public Observable<List<Thread>> getThreads(long groupId) {
        return groupThreadStore.get(groupId);
    }

    public Observable<List<Thread>> getThreads(long groupId, long categoryId) {
        return categoryThreadStore.get(new Pair<>(groupId, categoryId));
    }

    public Observable<List<Category>> getCategories(long groupId) {
        return groupCategoryStore.get(groupId);
    }

    public Observable<List<Category>> getCategories(long groupId, long categoryId) {
        return categoryCategoryStore.get(new Pair<>(groupId, categoryId));
    }

    public Observable<List<Message>> getMessages(Thread thread) {
        return getMessages(thread.getGroupId(), thread.getCategoryId(), thread.getThreadId());
    }

    public Observable<List<Message>> getMessages(long groupId, long categoryId, long threadId) {
        return messageStore.get(new MessageStoreKey(groupId, categoryId, threadId));
    }

    /**
     * THE STORES
     */
    private ObservableStore<Void, User> userStore =
            new ObservableStore<Void, User>() {
                @Override
                protected User fetch(Void request) {
                    return service.getUser();
                }
            };

    private ObservableStore<Long, List<Group>> groupStore =
            new ObservableStore<Long, List<Group>>() {
                @Override
                protected List<Group> fetch(Long companyId) {
                    return service.getGroups(companyId);
                }
            };

    private ObservableStore<Long, List<Thread>> groupThreadStore =
            new ObservableStore<Long, List<Thread>>() {
                @Override
                protected List<Thread> fetch(Long groupId) {
                    return service.getThreads(groupId);
                }
            };

    private ObservableStore<Pair<Long, Long>, List<Thread>> categoryThreadStore =
            new ObservableStore<Pair<Long, Long>, List<Thread>>() {
                @Override
                protected List<Thread> fetch(Pair<Long, Long> key) {
                    return service.getThreads(key.first, key.second);
                }
            };

    private ObservableStore<Long, List<Category>> groupCategoryStore =
            new ObservableStore<Long, List<Category>>() {
                @Override
                protected List<Category> fetch(Long groupId) {
                    return service.getCategories(groupId);
                }
            };

    private ObservableStore<Pair<Long, Long>, List<Category>> categoryCategoryStore =
            new ObservableStore<Pair<Long, Long>, List<Category>>() {
                @Override
                protected List<Category> fetch(Pair<Long, Long> key) {
                    return service.getCategories(key.first, key.second);
                }
            };

    private ObservableStore<MessageStoreKey, List<Message>> messageStore =
            new ObservableStore<MessageStoreKey, List<Message>>() {
                @Override
                protected List<Message> fetch(MessageStoreKey request) {
                    return service.getThreadMessages(
                            request.groupId, request.categoryId, request.threadId);
                }
            };

    protected class MessageStoreKey {
        public final long groupId;
        public final long threadId;
        public final long categoryId;

        public MessageStoreKey(long groupId, long categoryId, long threadId) {
            this.groupId = groupId;
            this.threadId = threadId;
            this.categoryId = categoryId;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MessageStoreKey))
                return false;

            if (obj == this)
                return true;

            MessageStoreKey other = (MessageStoreKey) obj;

            return 0 == ComparisonChain.start()
                    .compare(groupId, other.groupId)
                    .compare(threadId, other.threadId)
                    .compare(categoryId, other.categoryId)
                    .result();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(groupId, threadId, categoryId);
        }
    }
}
