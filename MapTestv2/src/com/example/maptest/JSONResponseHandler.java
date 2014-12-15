package com.example.maptest;

import java.io.IOException;

import org.apache.http.HttpResponse;

import com.example.maptest.Http.Response;

public abstract class JSONResponseHandler extends Http.ResponseHandlerBase {

	// 受信データをオブジェクトに変換
	@Override
	public Object createObjectFromResponse(HttpResponse response) {
		
			try {
				return response.getEntity().getContent();
			} catch (IllegalStateException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
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
