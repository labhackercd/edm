package net.labhackercd.edemocracia.data.api;

import net.labhackercd.edemocracia.data.model.*;
import net.labhackercd.edemocracia.data.model.Thread;
import net.romenor.deathray.action.ObjectWrapper;
import net.romenor.deathray.internal.Null;
import net.romenor.deathray.internal.Path;
import net.romenor.deathray.internal.Service;

import java.util.List;

@Service
public interface GroupService {

    public static final String SERVICE_URL = "https://edemocracia.camara.gov.br/api/secure/jsonws";

    @Path("/group/get-user-sites")
    public List<Group> getUserSites();

    // companyId defaults to the user's companyId
    @Path("/group/search")
    public List<Group> search(String name, String description, List params, int start, int end);

    @Path("/mbthread/get-threads")
    public List<Thread> getThreads(long groupId, long categoryId, int status, int start, int end);

    @Path("/mbcategory/get-categories")
    public List<Category> getCategories(long groupId);

    @Path("/mbcategory/get-categories")
    public List<Category> getCategories(long groupId, long categoryId, int start, int end);

    @Path("/mbmessage/get-thread-messages")
    public List<Message> getThreadMessages(
            long groupId, long categoryId, long threadId, int status, int start, int end);

    @Path("/mbmessage/add-message")
    public Message addMessage(
            long groupId, long categoryId, long threadId, long parentMessageId,
            String subject, String body, String format, List inputStreamOVPs,
            boolean anonymous, double priority, boolean allowPingbacks,
            ObjectWrapper serviceContext);

    // userId defaults to the id of the currently authenticated user
    @Null({"userId"})
    @Path("/user/get-user-by-id")
    public User getUserById();

    @Path("/user/get-user-by-id")
    public User getUserById(long userId);
}