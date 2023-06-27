package com.mm.android.mobilecommon.pps.provider;

import android.os.AsyncTask;
import android.util.Log;

import com.mm.android.mobilecommon.pps.utils.JsonUtils;

import org.json.JSONObject;


public class AsyncJSON {
	protected Processable<JSONObject> processable;
	protected String url;
	protected String payload;

	class JSONTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... arg) {
			String url = arg[0];
			String payload=arg[1];
			JSONObject result = null;
			HttpProvider provider=null;

			try {
				provider = new HttpProvider(url,payload);
				byte[] data = provider.provide();
				if( data == null ){
					return null;
				}

				result = JsonUtils.jsonAs(data);

			} catch(Throwable t) {
				Log.e("ERROR", "AsyncJSON.requestJSON, "+url, t);
				result=null;
			}

			if( provider != null){
				provider.destruct();
			}

			return result;
		}
		
      @Override
		protected void onPostExecute(JSONObject json) {
			processable.process(json);
		}
	}

	JSONTask task = new JSONTask();

	public AsyncJSON(String url, String payload, Processable<JSONObject> processable) {
		this.url = url;
		this.payload = payload;
		this.processable = processable;
	}

	public void requestJSON() {
		task.execute(url, payload);
	}

	public static AsyncJSON requestJSON(String url, String payload, Processable<JSONObject> processable) {
		AsyncJSON asyncJSON = new AsyncJSON(url, payload, processable);
		asyncJSON.requestJSON();
		return asyncJSON;
	}
}