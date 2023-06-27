package com.mm.android.mobilecommon.pps.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.utils.PpsHttpSend;

import java.util.HashMap;

public class PpsDeviceApiManager {
    private static int TIME_OUT = 10 * 1000;
    private static int TOKEN_TIME_OUT = 4 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;

    public static String getNewestVersion(String uid)throws BusinessException {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("uid", uid);
        JsonObject json = PpsHttpSend.execute(paramsMap, ACONST.API_METHOD_NEWEST_VERSION,TIME_OUT);
        String result = json.get("result").getAsString();
        if(result == null || result.equals("")) return "ERROR";
        JsonArray data = json.getAsJsonArray("data");
        if(data == null) return "ERROR";

        String returnStr = "";
        for(int i=0; i<data.size(); i++) {
            JsonObject o = (JsonObject) data.get(i);
            if(o.get("dtype").getAsString().equals("1")) {
                returnStr = o.get("version").getAsString()+"^"+o.get("force_update_yn").getAsString();
                break;
            }
        }

        return returnStr;
    }

}
