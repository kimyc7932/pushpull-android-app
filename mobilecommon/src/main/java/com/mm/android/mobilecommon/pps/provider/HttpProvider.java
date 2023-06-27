package com.mm.android.mobilecommon.pps.provider;

import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.utils.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpProvider implements RemoteProvider {
	private static final String TAG = HttpProvider.class.getSimpleName();
	private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 8192;
	private static final int DEFALUT_CONNECTION_TIMEOUT = 10000;
	private static final int DEFALUT_SO_TIMEOUT = 10000; //3000 ->10000 // 2019.12.08

	private OkHttpClient client;
	private String url;
	private String payload;
	private ResponseBody responseBody=null;
	private Response response=null;

	public HttpProvider(String url) {
		this.url = url;
		client = new OkHttpClient.Builder()
				.connectTimeout(DEFALUT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
				.readTimeout(DEFALUT_SO_TIMEOUT, TimeUnit.MILLISECONDS)
				.writeTimeout(DEFALUT_SO_TIMEOUT, TimeUnit.MILLISECONDS)
				.build();
	}

	public HttpProvider(String url, String payload) {
		this.url = url;
		this.payload = payload;

		client = new OkHttpClient.Builder()
				.connectTimeout(DEFALUT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
				.readTimeout(DEFALUT_SO_TIMEOUT, TimeUnit.MILLISECONDS)
				.writeTimeout(DEFALUT_SO_TIMEOUT, TimeUnit.MILLISECONDS)
				.build();
	}

//	@Override
	public byte[] provide() throws Exception {
		try {
			return _provide();
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "URL = " + this.url+", exception: "+ex.toString());
		} catch (Exception ex) {
			Log.e(TAG, "URL = " + this.url+", exception: "+ex.toString());
		}

		return null;
	}

	public byte[] _provide() throws Exception {
		Request request;
		MediaType contentType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(contentType, payload);

		String appVerCode = String.valueOf(ACONST.APP_VERCODE);
		request = new Request.Builder().url(url)
				.header("User-Agent", "PPS")
				.header("x-pps-appver", appVerCode)
				.post(body).build();
		response = client.newCall(request).execute();
		responseBody = response.body();

		if( response.code() != 200 ){
			throw new RemoteProviderException("Failed to get remote datas. error code:" + response.toString());
		}

		byte[] result=responseBody.bytes();
		return result;
	}


	@Override
	public void setConnetcionTimeout(int mills) {

	}

	@Override
	public void setSoTimeout(int mills) {

	}

	public void destruct() {
		try {
			if(response != null){
				response.close();
				response=null;
			}

			if(responseBody != null){
				responseBody.close();
				responseBody=null;
			}

			if(client != null) {
				if( client.cache() != null){
					client.cache().close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}