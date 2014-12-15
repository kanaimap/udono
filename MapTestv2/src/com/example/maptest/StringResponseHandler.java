package com.example.maptest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.example.maptest.Http.Response;

public abstract class StringResponseHandler extends Http.ResponseHandlerBase {

	// 受信データをオブジェクトに変換
	@Override
	public Object createObjectFromResponse(HttpResponse response) {
		try {
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (ParseException e) {
		} catch (IOException e) {
		}
		return null;
	}
		
	// 空のインスタンスを返す
	public static final StringResponseHandler getInstance() {
		return new StringResponseHandler() {
			@Override
			public void onFinish(Response response) {
			}
		};
	}

}


