package com.example.usermodule.api;

import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.utils.PpsHttpSend;

import java.util.Map;

public class PpsUserApiManager {
    private static int TIME_OUT = 10 * 1000;
    private static int TOKEN_TIME_OUT = 4 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;

    public static String updateUserInfo(Map<String, Object> paramMap)throws BusinessException {
        JsonObject json = PpsHttpSend.execute(paramMap, ACONST.API_UPDATE_USER_INFO,TIME_OUT);
        String result = json.get("result").getAsString();
        if(result == null || result.equals("")) return "ERROR";

        return result;
    }

}
