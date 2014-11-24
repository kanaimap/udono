package com.example.maptest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;


//位置情報、時間、名前をデータベースに登録するAsyncTask
public class InsertMyLocation extends AsyncTask<Void, Void, Void> {

	private String latitude, longitude, time,name;

	public InsertMyLocation(double latitude, double longitude, String time,String name) {
		this.latitude = String.valueOf(latitude);
		this.longitude = String.valueOf(longitude);
		this.time = time;
		this.name = name;
	}

	@Override
	protected Void doInBackground(Void... unused) {

		String url = "http://10.29.31.66/insert_mysql.php";
		DefaultHttpClient http = new DefaultHttpClient();

		HttpPost post = new HttpPost(url);

		List<NameValuePair> params = new ArrayList<NameValuePair>(4);
		params.add(new BasicNameValuePair("lat", latitude));
		params.add(new BasicNameValuePair("lon", longitude));
		params.add(new BasicNameValuePair("time", time));
		params.add(new BasicNameValuePair("name", name));

		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			http.execute(post);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
