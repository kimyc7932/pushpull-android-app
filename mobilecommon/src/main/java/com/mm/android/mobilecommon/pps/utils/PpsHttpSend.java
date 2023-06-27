package com.mm.android.mobilecommon.pps.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.CONST;
import com.mm.android.mobilecommon.openapi.HttpClient;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.mm.android.mobilecommon.utils.MD5Helper;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLException;

public class PpsHttpSend {

    private static final String TAG = PpsHttpSend.class.getSimpleName();

    public static JsonObject execute(Map<String, Object> paramsMap, String method,int timeOut) throws BusinessException {
        Gson gson = new Gson();
        Map<String, Object> map = paramsInit(paramsMap);
        // 返回json
        JsonObject jsonObj = doPost(ACONST.GET_API_URL() + method, map,timeOut);
        LogUtil.debugLog(TAG, "url::"+method+"\n"+"response result：" + jsonObj.toString());
        System.out.println("PpsHttpSend.execute.method start --------------------------------------");
        System.out.println("PpsHttpSend."+method+".requrl :: " + ACONST.GET_API_URL() + method);
        System.out.println("PpsHttpSend."+method+".paramsMap :: " + gson.toJson(map));
        System.out.println("PpsHttpSend."+method+".response.result :: " + jsonObj.toString());
        System.out.println("PpsHttpSend.execute.method finish --------------------------------------");
//        System.out.println("");
        if (jsonObj == null) {
            throw new BusinessException("openApi response is null");
        }
        return jsonObj;
    }

    private static JsonObject getResultJson(JsonObject jsonResult, String key) {
        JsonObject obj = jsonResult.getAsJsonObject(key);
        if(obj == null) {
            obj = new JsonObject();
            obj.add(key, jsonResult);
        }
        return obj;
    }

    public static JsonObject execute(String json, String method,int timeOut) throws BusinessException {
        // 返回json
        LogUtil.debugLog(TAG, "request result：" + json.toString());
        JsonObject jsonObj = doPost(CONST.HOST + "/openapi/" + method, json,timeOut);
        LogUtil.debugLog(TAG, "URL::"+method+"\n"+"response result：" + jsonObj.toString());
//        System.out.println("httpSend."+method+".response result - " + jsonObj.toString());
        if (jsonObj == null) {
            throw new BusinessException("openApi response is null");
        }
        JsonObject jsonResult = jsonObj.getAsJsonObject("result");
        if (jsonResult == null) {
            throw new BusinessException("openApi response is null");
        }
        String code = jsonResult.get("code").getAsString();
        if (!"0".equals(code)) {
            String msg = jsonResult.get("msg").getAsString();
            throw new BusinessException(code + msg);
        }
        try {
            JsonObject jsonData = jsonResult.getAsJsonObject("data");
            if (jsonData ==null){
                jsonData=new JsonObject();
            }
            return jsonData;
        } catch (Throwable e) {
            BusinessException businessException = new BusinessException(e);
            throw businessException;
        }
    }

    private static JsonObject doPost(String url, Map<String, Object> map, int timeOut) throws BusinessException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        JsonObject jsonObject = new JsonObject();
        try {
            LogUtil.debugLog(TAG, "reqest: " + url + " data:" + json.toString());
            String openApi = HttpClient.post(url, json, "application/json", "OpenApi",timeOut);
            jsonObject =  new JsonParser().parse(openApi).getAsJsonObject();
        } catch (IOException e) {
            BusinessException b = new BusinessException(e);
            if (e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException
                    || e instanceof UnknownHostException
                    || e instanceof UnknownServiceException || e instanceof SSLException
                    || e instanceof SocketException) {
                b.errorDescription = "에러，네트워크를 확인해 주세요"; //操作失败，请检查网络
            }
            throw b;
        } catch (Throwable e) {
            BusinessException b = new BusinessException(e);
            throw b;
        }
        return jsonObject;
    }

    private static JsonObject doPost(String url, String json, int timeOut) throws BusinessException {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        try {
            LogUtil.debugLog(TAG, "reqest: " + url + " data:" + json.toString());
            String openApi = HttpClient.post(url, json, "application/json", "OpenApi",timeOut);
            jsonObject =  new JsonParser().parse(openApi).getAsJsonObject();
        } catch (IOException e) {
            BusinessException b = new BusinessException(e);
            if (e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException
                    || e instanceof UnknownHostException
                    || e instanceof UnknownServiceException || e instanceof SSLException
                    || e instanceof SocketException) {
                b.errorDescription = "에러，네트워크를 확인해 주세요";
            }
            throw b;
        } catch (Throwable e) {
            BusinessException b = new BusinessException(e);
            throw b;
        }
        return jsonObject;
    }

    private static Map<String, Object> paramsInit(Map<String, Object> paramsMap) {
        long time = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        StringBuilder paramString = new StringBuilder();
        paramString.append("time:").append(time).append(",");
        paramString.append("nonce:").append(nonce).append(",");
        paramString.append("appSecret:").append(CONST.SECRET);
        String sign = "";
        // 计算MD5得值
        sign = MD5Helper.encodeToLowerCase(paramString.toString().trim());
        Map<String, Object> systemMap = new HashMap<String, Object>();
        systemMap.put("ver", "1.0");
        systemMap.put("sign", sign);
        systemMap.put("appId", CONST.APPID);
        systemMap.put("nonce", nonce);
        systemMap.put("time", time);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("system", systemMap);
        for(String key : paramsMap.keySet()) {
            map.put(key, paramsMap.get(key));
        }
        map.put("id", id);
        return map;
    }
}
