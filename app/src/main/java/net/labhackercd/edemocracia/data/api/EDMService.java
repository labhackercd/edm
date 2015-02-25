package net.labhackercd.edemocracia.data.api;

import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;

import java.util.List;

public interface EDMService {

    public User getUser();

    List<Thread> getThreads(long groupId);

    List<Thread> getThreads(long groupId, long categoryId);

    List<Category> getCategories(long groupId);

    List<Category> getCategories(long groupId, long categoryId);

    List<Group> getGroups(long companyId);

    List<Message> getThreadMessages(long groupId, long categoryId, long threadId);

    Message addMessage(Message message);

    public static class Builder extends EDMServiceImpl.Builder {
    }
}
