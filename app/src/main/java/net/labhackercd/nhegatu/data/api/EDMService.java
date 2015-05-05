package net.labhackercd.nhegatu.data.api;

import com.liferay.mobile.android.auth.Authentication;

import net.labhackercd.nhegatu.data.api.client.Endpoint;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;

import java.util.List;
import java.util.UUID;

public interface EDMService {

    public interface Builder {
        public Builder setEndpoint(Endpoint endpoint);

        public Builder setAuthentication(Authentication authentication);

        public Builder setErrorHandler(ErrorHandler errorHandler);

        public EDMService build();
    }

    public Builder newBuilder();

    public User getUser();

    public User getUser(long userId);

    public List<Thread> getThreads(long groupId);

    public List<Thread> getThreads(long groupId, long categoryId);

    public List<Category> getCategories(long groupId);

    public List<Category> getCategories(long groupId, long categoryId);

    public List<Group> getGroups(long companyId);

    public List<Message> getThreadMessages(long groupId, long categoryId, long threadId);

    public Message addMessage(UUID uuid, long groupId, long categoryId, long threadId,
                              long parentMessageId, String subject, String body);
}