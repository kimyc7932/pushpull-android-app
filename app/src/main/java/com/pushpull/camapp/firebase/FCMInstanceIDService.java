package com.pushpull.camapp.firebase;

import android.os.AsyncTask;

import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.utils.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FCMInstanceIDService {
    private static final String TAG = FCMInstanceIDService.class.getSimpleName();
    private String mToken = "";

    public void sendRegistrationToServer(String token)
    {
        mToken = token;
        new sendToServer().execute();
    }

    public class sendToServer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                OkHttpClient client = new OkHttpClient();
                String url = ACONST.API_FCM_TOKEN_RERESH;
                JSONObject jobj = new JSONObject();
                jobj.put("uid", ACONST.LOGIN_UID);
                jobj.put("phone_token", ACONST.DEVICE_TOKEN);
                jobj.put("phone_type", "1");
                String pdata = jobj.toString();
                MediaType contentType = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(contentType, pdata);
                Request request = new Request.Builder().url(url).post(body).build();
                Log.d(TAG, "<----- REQ: " + url + ", " + pdata);
                Response response = client.newCall(request).execute();
                if (response != null) {
                    Log.d(TAG, "-----> RES: " + response.body().string());
                    if (response.code() == 200) {
                        ResponseBody responseBody = response.body();
                        if (responseBody == null) {
                        }
                    }
                }
            } catch (JSONException e){

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
