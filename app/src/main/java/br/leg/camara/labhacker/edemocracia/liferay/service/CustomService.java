package br.leg.camara.labhacker.edemocracia.liferay.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import br.leg.camara.labhacker.edemocracia.liferay.Session;
import br.leg.camara.labhacker.edemocracia.liferay.exception.ServerException;


public class CustomService extends Service {

    public CustomService(Session session) {
        super(session);
    }

    public JSONArray listGroups(int companyId) throws JSONException, IOException, ServerException {
        JSONObject args = new JSONObject();

        args.put("companyId", companyId);
        args.put("name", "%");
        args.put("description", "%");
        args.put("params", "");
        args.put("start", -1);
        args.put("end", -1);

        JSONObject cmd = new JSONObject();

        cmd.put("/group/search", args);

        return getSession().invoke(cmd);
    }

    public JSONArray listGroupThreads(int groupId) throws JSONException, IOException, ServerException {

        // WARNING This method actually issues two method calls on the server.
        // First we get the list of threads, and then we get a list of messages
        // for each rootMessage of each thread.

        // First, we issue a "/mbthread/get-group-threads" command
        // to get the list of threads inside the given group.
        JSONObject args = new JSONObject();

        args.put("groupId", groupId);
        args.put("userId", -1);
        args.put("status", 0);
        args.put("start", -1);
        args.put("end", -1);

        JSONObject command = new JSONObject();

        command.put("/mbthread/get-group-threads", args);

        JSONArray threads = getSession().invoke(command);

        // Then we construct a batch call with a /mbmessage/get-message for each rootMessageId
        // of each thread we just got.

        JSONArray commands = new JSONArray();

        for (int i = 0; i < threads.length(); i++) {
            JSONObject thread = threads.getJSONObject(i);

            args = new JSONObject();
            args.put("messageId", thread.getInt("rootMessageId"));

            command = new JSONObject();
            command.put("/mbmessage/get-message", args);

            commands.put(command);
        }

        JSONArray messages = getSession().invoke(commands);

        // Then we place each rootMessage in the rootMessage column of the threads

        for (int i = 0; i < threads.length(); i++) {
            JSONObject thread = threads.getJSONObject(i);
            JSONObject message = messages.getJSONObject(i);
            thread.put("rootMessage", message);
        }

        // And then we return :)

        return threads;
    }

    public JSONArray listThreadMessages(int groupId, int categoryId, int threadId) throws JSONException, IOException, ServerException {
       JSONObject args = new JSONObject();

        args.put("groupId", groupId);
        args.put("categoryId", categoryId);
        args.put("threadId", threadId);
        args.put("status", 0);
        args.put("start", -1);
        args.put("end", -1);

        JSONObject command = new JSONObject();
        command.put("/mbmessage/get-thread-messages", args);

        return getSession().invoke(command);
    }
}
