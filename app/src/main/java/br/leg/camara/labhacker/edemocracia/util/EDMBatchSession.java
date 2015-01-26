package br.leg.camara.labhacker.edemocracia.util;

import com.liferay.mobile.android.http.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EDMBatchSession extends EDMSession {
    private List<JSONObject> commands;

    public EDMBatchSession(EDMSession session) {
        super(session.getAuthentication());
        commands = new ArrayList<>();
    }

    public JSONArray invoke() throws Exception {
        if (this.commands.size() == 0) {
            return null;
        }

        JSONArray commands = new JSONArray(this.commands);

        try {
            if (callback != null) {
                throw new RuntimeException("Not implemented");
            } else {
                return HttpUtil.post(this, commands);
            }
        } finally {
            this.commands = new ArrayList<>();
        }
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        commands.add(command);
        return null;
    }

    @Override
    public JSONArray upload(JSONObject command) throws Exception {
        throw new RuntimeException("Can't batch upload requests");
    }
}
