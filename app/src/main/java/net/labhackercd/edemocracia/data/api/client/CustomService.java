package net.labhackercd.edemocracia.data.api.client;

import com.liferay.mobile.android.service.BaseService;
import com.liferay.mobile.android.service.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomService extends BaseService {
    public CustomService(Session session) {
        super(session);
    }

    /**
     * It's just like ExpandoValueService.getData, but with hints and fixed argument names.
     */
    public JSONObject expandoValueGetData(long companyId, String className, String tableName,
                                          String columnName, long classPK) throws Exception {
        JSONObject _command = new JSONObject();

        try {
            JSONObject _params = new JSONObject();

            _params.put("companyId", companyId);
            _params.put("className", className);
            _params.put("tableName", tableName);
            _params.put("columnName", columnName);

            // Also, it's classPk that works, not classPK
            _params.put("classPk", classPK);

            // XXX Watch for the hint (.5), everyone!
            _command.put("/expandovalue/get-data.5", _params);
        }
        catch (JSONException _je) {
            throw new Exception(_je);
        }

        JSONArray _result = session.invoke(_command);

        if (_result == null) {
            return null;
        }

        return _result.getJSONObject(0);
    }
}
