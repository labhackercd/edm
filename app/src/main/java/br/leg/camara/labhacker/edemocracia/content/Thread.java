package br.leg.camara.labhacker.edemocracia.content;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class Thread {
    private int categoryId;
    private int groupId;
    private int messageCount;
    private boolean question;
    private int rootMessageId;
    private int status;
    private int threadId;
    private int viewCount;

    private Message rootMessage;

    /* {
        "categoryId": 1934029,
        "companyId": 10131,
        "groupId": 1927974,
        "lastPostByUserId": 12777,
        "lastPostDate": 1412953871147,
        "messageCount": 1,
        "priority": 0,
        "question": false,
        "rootMessageId": 1941849,
        "rootMessageUserId": 12777,
        "status": 0,
        "statusByUserId": 12777,
        "statusByUserName": "Equipe e-Democracia",
        "statusDate": 1412953871147,
        "threadId": 1941850,
        "viewCount": 162
    } */
    public static Thread fromJSONObject(JSONObject o) throws JSONException {
        Thread r = new Thread();

        r.categoryId = o.getInt("categoryId");
        r.groupId = o.getInt("groupId");
        r.messageCount = o.getInt("messageCount");
        r.question = o.getBoolean("question");
        r.rootMessageId = o.getInt("rootMessageId");
        r.threadId = o.getInt("threadId");
        r.viewCount = o.getInt("viewCount");

        JSONObject rootMsg = o.getJSONObject("rootMessage");
        if (rootMsg != null) {
            r.rootMessage = Message.fromJSONObject(rootMsg);
        }

        return r;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public boolean isQuestion() {
        return question;
    }

    public int getRootMessageId() {
        return rootMessageId;
    }

    public int getStatus() {
        return status;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public Message getRootMessage() {
        return rootMessage;
    }
}
