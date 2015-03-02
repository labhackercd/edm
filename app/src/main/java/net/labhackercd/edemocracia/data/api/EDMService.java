package net.labhackercd.edemocracia.data.api;

import com.liferay.mobile.android.auth.Authentication;

import net.labhackercd.edemocracia.data.api.client.Endpoint;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;

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

    List<Thread> getThreads(long groupId);

    List<Thread> getThreads(long groupId, long categoryId);

    List<Category> getCategories(long groupId);

    List<Category> getCategories(long groupId, long categoryId);

    List<Group> getGroups(long companyId);

    List<Message> getThreadMessages(long groupId, long categoryId, long threadId);

    Message addMessage(UUID uuid, Message message, String subject, String body);
}
