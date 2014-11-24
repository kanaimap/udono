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


//アイコンIDと名前をデータベースに登録するAsyncTask
public class SetMyNameAndIconID extends AsyncTask<String, String, String> {
	
	String name,icon_id;
	String TAG = "check";
	MainActivity main;
	String r ="";
	
	public SetMyNameAndIconID(int icon_id,String name,MainActivity main){
		this.name =name;
		this.icon_id = String.valueOf(icon_id);
		this.main = main;
	}

	@Override
	protected String doInBackground(String... unused) {

		String url = "http://10.29.31.66/set_iconID_and_name.php";
		DefaultHttpClient http = new DefaultHttpClient();
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("ICON_ID", icon_id));

		HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			HttpResponse response = http.execute(post);
			r = EntityUtils.toString(response.getEntity(), "UTF-8");
			if(r.equals("duplication")){
				main.check = r;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	@Override
	protected void onPostExecute(String sReturn) {
		main.check = sReturn;
	}
}
