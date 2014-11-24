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


//名前とマーカー情報を更新するAsyncTask
public class ChangeName extends AsyncTask<Void, Void, Void> {
	
	String name_result,before_name,icon_id;
	
	public ChangeName(String name_result,String before_name,int icon_id){
		this.name_result = name_result;
		this.before_name = before_name;
		this.icon_id =String.valueOf(icon_id);
	}

	@Override
	protected Void doInBackground(Void... unused) {

		String url = "http://10.29.31.66/change_name.php";
		DefaultHttpClient http = new DefaultHttpClient();
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("name_result", name_result));
		params.add(new BasicNameValuePair("before_name", before_name));
		params.add(new BasicNameValuePair("icon_id", icon_id));

		HttpPost post = new HttpPost(url);
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
