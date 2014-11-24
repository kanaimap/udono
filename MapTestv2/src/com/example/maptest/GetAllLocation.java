package com.example.maptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;


//データベースから位置情報をJSON形式で取得するAsyncTask
public class GetAllLocation extends AsyncTask<String, String, String> {

	private String sReturn = "";
	int num = 0;
	double[] latitude;
	double[] longitude;
	int[] id;
	String[] time;
	MainActivity _mainAct;

	public GetAllLocation(MainActivity mainAct) {
		_mainAct = mainAct;
	}

	@Override
	protected String doInBackground(String... unused) {

		String url = "http://10.29.31.66/get_mysql.php";
		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);

		try {
			HttpResponse response = http.execute(post);
			if (response.getStatusLine().getStatusCode() < 400) {
				InputStream objStream = response.getEntity().getContent();
				InputStreamReader objReader = new InputStreamReader(objStream);
				BufferedReader objBuf = new BufferedReader(objReader);
				StringBuilder objJson = new StringBuilder();
				String sLine;
				while ((sLine = objBuf.readLine()) != null) {
					objJson.append(sLine);
				}
				sReturn = objJson.toString();
				objStream.close();
			}
		} catch (IOException e) {
		}
		return sReturn;
	}

	@Override
	protected void onPostExecute(String sReturn) {
		this.sReturn = sReturn;
		_mainAct.json = sReturn;
	}
}
