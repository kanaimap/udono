package com.example.maptest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;


//データベースによって自動的に割り当てられたユーザーIDを取得するAsyncTask
public class GetMyId extends AsyncTask<Void, Void, Void> {
	
	MainActivity main;
	String name,id;
	
	public GetMyId(MainActivity main,String name){
		this.name =name;
		this.main = main;
	}

	@Override
	protected Void doInBackground(Void... unused) {

		String url = "http://10.29.31.66/get_my_id.php";
		DefaultHttpClient http = new DefaultHttpClient();
		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("name", name));

		HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			HttpResponse response = http.execute(post);
			id = EntityUtils.toString(response.getEntity(), "UTF-8").trim();
			main.userid = id;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
