package com.example.maptest;


import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Http {

	/**
	 * リクエスト情報クラス
	 */
	public static class Request {
		// リクエストURL
		public String url = null;
		// POSTパラメータ
		public List<Param> params = new ArrayList<Param>();
		// 保持オブジェクト
		public Object keepObject = null;
	}

	/**
	 * レスポンス情報クラス
	 */
	public static class Response implements Serializable {

		private static final long serialVersionUID = 1L;

		// HTTPレスポンスコード
		public Integer code = null;
		// レスポンス内容
		public Object value = null;
		
		// リクエスト時の保持オブジェクト
		public Object keepObject = null;
	}

	/**
	 * POSTパラメータクラス
	 */
	public static class Param {

		public static final int TYPE_STRING = 1;
		public static final int TYPE_IMAGE = 2;

		private int type;
		private String key;
		private String value;

		public Param(int type, String key, String value) {
			this.type = type;
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * リクエスト実行クラス
	 */
	private static class HttpRequest extends AsyncTask<Void, Void, Void> {

		// URL文字列
		private String url = null;
		// POSTパラメータ
		private List<Param> params = new ArrayList<Param>();
		// レスポンスハンドラ
		private ResponseHandlerBase handler = null;
		// 保持オブジェクト
		private Object keepObject = null;
		// レスポンスオブジェクト
		private Response ret = null;
		// interruptするスレッド
		private Thread interruptThread = null;

		@Override
		protected Void doInBackground(Void... params) {

			// URI構築
			URI uri = null;
			ContentType textContentType = ContentType.create("text/plain",Consts.UTF_8);
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return null;
			}

			// GET/POSTリクエスト作成
			HttpUriRequest request;
			if (this.params != null && this.params.size() > 0) {
				HttpPost r = new HttpPost(uri);
				MultipartEntityBuilder entity = MultipartEntityBuilder.create();
				for (Param p : this.params) {
					switch (p.type) {
					case Param.TYPE_STRING:
						entity.addTextBody(p.key,p.value,textContentType);
						break;
					}
				}
				r.setEntity(entity.build());
				request = r;
			} else {
				HttpGet r = new HttpGet(uri);
				request = r;
			}

			// リクエストを実行
			DefaultHttpClient httpClient = new DefaultHttpClient();
			try {
				httpClient.execute(request,
						new org.apache.http.client.ResponseHandler<Void>() {

							// HTTPレスポンスから，受信文字列をエンコードして文字列として返す
							@Override
							public Void handleResponse(HttpResponse response)
									throws IOException {

								if (ret == null) {
									ret = new Response();
								}

								// ret = new Http.Response();
								ret.code = response.getStatusLine()
										.getStatusCode();
								// 正常に受信できた場合は200
								if (ret.code == 200) {
									ret.value = handler
											.createObjectFromResponse(response);
								}

								// 保持オブジェクトを継承
								ret.keepObject = keepObject;

								// スレッドが指定されている場合はinterrupt
								if (interruptThread != null) {
									interruptThread.interrupt();
								}

								return null;
							}

						});
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				// 通信のshutdown
				if (httpClient != null) {
					try {
						httpClient.getConnectionManager().shutdown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			return null;

		}

		// タスク終了時
		protected void onPostExecute(Void unused) {

			// 受信結果をUIに渡すためにまとめる
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putSerializable("http_response", ret);
			message.setData(bundle);

			// 受信結果に基づいてUI操作させる
			handler.sendMessage(message);
		}

	}

	/**
	 * レスポンス実行ベースクラス
	 */
	public static abstract class ResponseHandlerBase extends Handler {

		// コンストラクタ
		public ResponseHandlerBase() {
		}

		// リクエストタスク完了時にコールされる
		public void handleMessage(Message msg) {
			try {
				onFinish((Response) msg.getData().get("http_response"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// レスポンス取得時にオブジェクトに変換するクラス
		public abstract Object createObjectFromResponse(HttpResponse response);

		// レスポンス完了時に呼ばれるクラス
		public abstract void onFinish(Response response);

	}

	/**
	 * 同期リクエスト
	 */
	public static final Response requestSync(Request request,
			ResponseHandlerBase handler) {

		// レスポンス格納用
		Response resp = new Response();

		// リクエスト構築
		HttpRequest httpReq = new HttpRequest();
		httpReq.url = request.url;
		httpReq.handler = handler;
		if (request.params != null) {
			httpReq.params.addAll(request.params);
		}
		httpReq.keepObject = request.keepObject;
		httpReq.ret = resp;
		httpReq.interruptThread = Thread.currentThread();

		// リクエスト実行
		httpReq.execute();
		
		int i = 0;
		// レスポンスが返るまで待機
		while (true) {
			try {
				if(i > 0){
					resp.value = "404";
					return resp;
				}
				Thread.sleep(8000);
				++i;
			} catch (InterruptedException e) {
				// レスポンスが返った
				break;
			}
		}

		// レスポンスを返す
		return resp;
	}
	
	/**
	 * 非同期リクエスト
	 */
	public static final void request(Request request,
			ResponseHandlerBase handler) {

		// リクエスト構築
		HttpRequest httpReq = new HttpRequest();
		httpReq.url = request.url;
		httpReq.handler = handler;
		if (request.params != null) {
			httpReq.params.addAll(request.params);
		}
		httpReq.keepObject = request.keepObject;
		httpReq.ret = null;
		httpReq.interruptThread = null;

		// リクエスト実行
		httpReq.execute();
	}

}

