package br.leg.camara.labhacker.edemocracia.content;

import android.content.ContentResolver;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class Group {
    public static final String AUTHORITY = Group.class.getName();
    public static final Uri CONTENT_URI = (new Uri.Builder()).authority(AUTHORITY).scheme(ContentResolver.SCHEME_CONTENT).build();

    private boolean active;
    private int companyId;
    private String description;
    private int groupId;
    private String name;
    private int type;

    public static Group fromJSONObject(JSONObject o) throws JSONException {
        Group g = new Group();
        g.active = o.getBoolean("active");
        g.companyId = o.getInt("companyId");
        g.description = o.getString("description");
        g.groupId = o.getInt("groupId");
        g.name = o.getString("name");
        g.type = o.getInt("type");
        return g;
    }

    public boolean isActive() {
        return active;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getDescription() {
        return description;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}
