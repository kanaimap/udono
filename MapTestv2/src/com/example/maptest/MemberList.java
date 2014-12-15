package com.example.maptest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MemberList extends Activity {
	
	String json ="";
	int numberofmember;
	String[] memberlist;
	ListView lv;
	Http.Request request;
	Http.Response response;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.member);
	  lv = (ListView) findViewById(R.id.memberlist);

	  request = new Http.Request();
	  request.url = "http://10.29.31.119/get_all_name.php";
	  //同期通信　タイムアウト8秒
	  response = Http.requestSync(request, JSONResponseHandler.getInstance());
	  //タイムアウトした場合、警告をだし、MainActivityへ戻る
	  if(((String) response.value).equals("404")){
			Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
			MemberList.this.finish();
		}
	  
	  //リストビューを利用してユーザ一覧を表示する
	  json = (String) response.value;
	  try {
			JSONArray jsonArray = new JSONArray(json);
			
			numberofmember = jsonArray.length();
			memberlist = new String[numberofmember];

			for (int i = 0; i < numberofmember; i++) {

				JSONObject jsonObject = jsonArray.getJSONObject(i);
				memberlist[i] = jsonObject.getString("name");
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_expandable_list_item_1, memberlist);

			lv.setAdapter(adapter);
			jsonArray = null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		MemberList.this.finish();
	}
}
