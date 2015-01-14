package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Message {

    private String body;
    private int categoryId;
    private int companyId;
    private Date createDate;
    private String format;
    private int groupId;
    private int messageId;
    private Date modifiedDate;
    private int parentMessageId;
    private int rootMessageId;
    private int status;
    private String subject;
    private int threadId;
    private int userId;
    private String userName;

    /* {
        "allowPingbacks": false,
        "anonymous": false,
        "answer": false,
        "attachments": false,
        "body": "Pessoal, recebemos uma sugestão da colega Mônica Xavier do Departamento de Taquigrafia da Câmara.\n\nO discurso da mulher no Parlamento – APP para visualização dos discursos das Deputadas, com nome, partido, Estado, data do pronunciamento, a partir do Banco de Discursos do DETAQ.",
        "categoryId": 1934029,
        "classNameId": 0,
        "classPK": 0,
        "companyId": 10131,
        "createDate": 1412953870343,
        "format": "bbcode",
        "groupId": 1927974,
        "messageId": 1941849,
        "modifiedDate": 1412953886867,
        "parentMessageId": 0,
        "priority": 0,
        "rootMessageId": 1941849,
        "status": 0,
        "statusByUserId": 12777,
        "statusByUserName": "Equipe e-Democracia",
        "statusDate": 1412953886870,
        "subject": "Discurso da mulher no parlamento",
        "threadId": 1941850,
        "userId": 12777,
        "userName": "Equipe e-Democracia",
        "uuid": "a5f447f1-2c80-47df-b9dc-cb2177898353"
    } */
    public static Message fromJSONObject(JSONObject o) throws JSONException {
        Message r = new Message();

        r.body = o.getString("body");
        r.categoryId = o.getInt("categoryId");
        r.companyId = o.getInt("companyId");
        // TODO r.createDate = o.getDate("createDate");
        r.format = o.getString("format");
        r.groupId = o.getInt("groupId");
        r.messageId = o.getInt("messageId");
        // TODO r.modifiedDate = o.getDate("modifiedDate");
        r.parentMessageId = o.getInt("parentMessageId");
        r.rootMessageId = o.getInt("rootMessageId");
        r.status = o.getInt("status");
        r.subject = o.getString("subject");
        r.threadId = o.getInt("threadId");
        r.userId = o.getInt("userId");
        r.userName = o.getString("userName");

        return r;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getGroupId() {
        return groupId;
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

    public String getBody() {
        return body;
    }

    public int getCompanyId() {
        return companyId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getFormat() {
        return format;
    }

    public int getMessageId() {
        return messageId;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public int getParentMessageId() {
        return parentMessageId;
    }

    public String getSubject() {
        return subject;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
