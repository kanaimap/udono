package com.example.maptest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	// map操作用変数
	GoogleMap map;

	// 現在地に打つマーカ-
	MarkerOptions options1 = new MarkerOptions();

	// 法政大学に打つマーカ
	MarkerOptions options2 = new MarkerOptions();

	// ボタン用変数 1:NOW 	2,3,4:未使用
	Button mButton1, mButton2, mButton3, mButton4;
	ToggleButton tb1;
	// タイムゾーン取得用変数
	TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
	
	MainActivity main = this;
	
	BitmapDescriptor icon;
	
	//使用アイコン
	int icon_id;
	
	String userid;
	String abnormal_termination = "none";
	
	//Preference取得用変数
	SharedPreferences sharedpreferences ;
	
	//現在利用している名前を保管する変数
	String name_result;
	//ひとつ前に利用していた名前を保管する変数
	String before_name;
	//現在利用しているマーカーの情報を保管する変数
	String list_result;
	//ひとつ前に利用していたマーカーの情報を保管する変数
	String before_list;
	
	//名前の重複チェック用変数
	String check ="";
	
	//JSON形式の位置情報を処理するための変数群
	String json = "";
	int database_number;
	int[] database_id;
	double[] database_latitude, database_longitude;
	String[] database_name,database_time;
	
	//初期設定が必要であるかを判断するフラグ
	boolean flag1 = false;
	//設定画面において、マーカー配置が押された場合にtureとなるフラグ
	boolean flag2 = false;
	

	static final int SUB_ACTIVITY = 1001;
	
	//現在時刻取得用の変数群
	Calendar calendar;
	String time;
	SimpleDateFormat df;
	

	// 初期画面を構成
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MapsInitializer.initialize(getApplicationContext());
		
		try{
			  Class.forName("android.os.AsyncTask");
		}catch(ClassNotFoundException e){}


		//現在設定されている名前とマーカー、ユーザーIDを読み込む
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		list_result = (String)sharedpreferences.getString("list","Unselected");
		before_list = (String)sharedpreferences.getString("list","Unselected");
		name_result = (String)sharedpreferences.getString("name","Unselected");   
		before_name = (String)sharedpreferences.getString("name","Unselected");
		userid = (String)sharedpreferences.getString("userid","Unselected");
		icon_color();
		options1.icon(icon);

		setContentView(R.layout.activity_main);

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		MapsInitializer.initialize(this);

		// ボタン割り当て
		//mbutton1:now 	tb1:自動更新
		mButton1 = (Button) findViewById(R.id.Button01);
//		mButton2 = (Button) findViewById(R.id.Button02);
//		mButton3 = (Button) findViewById(R.id.Button03);
		tb1 = (ToggleButton) findViewById(R.id.toggleButton1);
		tb1.setOnCheckedChangeListener(tb1_OnCheckedChangeListener);
		
		// タイマーOFFボタンをクリック禁止に
		//mButton3.setEnabled(false);

		// 初期位置を法政大学とする
		moveToFirstRocation();
		
		//名前とユーザーIDが共に設定されていないならば、初期設定へ
		if(name_result.equals("Unselected") && userid == "Unselected"){
			flag1 = true;
			Editor editor = sharedpreferences.edit();
			editor.putString("list","totoro");
			editor.commit();
			list_result = (String)sharedpreferences.getString("list","Unselected");
			before_list = (String)sharedpreferences.getString("list","Unselected");
		}
		
		
		
		//端末側の名前とデータベース側の名前の矛盾をチェックする
		if(!flag1){
			CheckName check_name = new CheckName(name_result,userid,main);
			check_name.execute();
			//１秒待つ
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			
			//矛盾していた場合は修正する
			if(!(abnormal_termination.equals("none"))){
				Editor editor = sharedpreferences.edit();
				editor.putString("name",abnormal_termination);
				editor.commit();
				name_result = abnormal_termination;
				before_name = abnormal_termination;
			}
			
		}
		
		
		// ズームボタンと現在地取得ボタンを可視化
		UiSettings settings = map.getUiSettings();
		settings.setZoomControlsEnabled(true);
		map.setMyLocationEnabled(true);
	}
	
	 //メッセージを表示する
    protected void showMessage(String msg){
		Toast.makeText(
			this, 
			msg, Toast.LENGTH_SHORT).show();
	}
	
    //menuの設定
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	//menuのアイテムを押したときの行動
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_settings:
			showMessage("open the setting");
			startActivityForResult(new Intent(this, Setting.class),SUB_ACTIVITY);
			default:
				break;
		}
		return true;
	}

	// ユーザーの操作待ち
	@Override
	protected void onResume() {
		super.onResume();		
		
		
		/*******************************flag1がtrueならば初期設定を行う******************************/
		if(flag1){
			//入力された名前が"Unselected"である場合、警告を出し再入力させる
			if(((String)sharedpreferences.getString("name","Unselected")).equals("Unselected")){
				first_setting();
			}
			//入力された名前が"Unselected"以外の場合、重複をチェックする
			else{
				name_setting();
			}
		}
		/********************************************************************************************/
		
		
		
		/*********************flag1がfalseかつ名前が変更されていれば、名前変更処理へ************************/
		else if(!flag1 && !((String)sharedpreferences.getString("name","Unselected")).equals(name_result)){
			change_name();
		}
		/***************************************************************************************************/
		
		
		
		/*******************flag1がfalseかつマーカーが変更されいれば、マーカー変更処理へ********************/
		else if(!flag1 && !((String)sharedpreferences.getString("list","Unselected")).equals(list_result)){
			change_icon();
		}
		/***************************************************************************************************/
		
		
		
		/**flag2がtrueならばマーカーの配置処理へ**/
		if(flag2){
			putmarker();
			flag2 = false;
		}
		/*****************************************/
		
		
		
		/***************************ロングクリックによる現在地取得***************************/
		map.setOnMapLongClickListener(new OnMapLongClickListener(){ 
 			@Override 
			public void onMapLongClick(LatLng point){ 
 				
 				//現在地を取得する
 				double mylat = point.latitude;
				double mylon = point.longitude;				
				options1.position(point);
				//名前を取得する
		    	String name_result = (String)sharedpreferences.getString("name","Unselected");  		
		    	//マーカー画像を設定する
				icon_color();
				//現在時刻を取得する
				calendar = Calendar.getInstance(tz);
				df = new SimpleDateFormat("HH:mm",
						Locale.JAPANESE);
				time = df.format(calendar.getTime());
				
				options1.icon(icon);
				//マーカー配置
				options1.title("今ここ！at " + time + " by" + name_result);
				map.addMarker(options1);
				//位置情報をデータベースに送信
				InsertMyLocation post = new InsertMyLocation(mylat, mylon, time,name_result);
				post.execute();
			 
 			} 
		}); 
		/************************************************************************************/

		
		
		/************************************Nowボタンが押された時の処理****************************************/
		mButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 現在地が取得できない場合は、何もしない
				if (map.getMyLocation() == null);
				else {
					// 現在地の緯度、経度を取得
					double mylat = map.getMyLocation().getLatitude();
					double mylon = map.getMyLocation().getLongitude();
					
					
					// 現在地を取得
					LatLng position = new LatLng(map.getMyLocation()
							.getLatitude(), map.getMyLocation().getLongitude());

					// アイコンを打つ場所を現在地に設定
					options1.position(position);

					// マップの中心を現在地へ
					CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(
							mylat, mylon));
					map.moveCamera(cu);

					// 現在時刻を取得
					calendar = Calendar.getInstance(tz);
					df = new SimpleDateFormat("HH:mm",
							Locale.JAPANESE);
					time = df.format(calendar.getTime());
					
					icon_color();
					
					options1.icon(icon);
					
					// アイコンを配置
					options1.title("今ここ！at " + time + " by" + name_result);
					map.addMarker(options1);
					
					//位置情報をデータベースに送信
					InsertMyLocation post = new InsertMyLocation(mylat, mylon, time,name_result);
					post.execute();
				}
			}
		});
		/*****************************************************************************************************/
		
		
		
		/***************************************自動更新関連の処理********************************************/
		/*
		// 自動更新ONボタンが押された時の処理
		mButton2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// 自動更新ONボタンをクリック禁止に
				mButton2.setEnabled(false);

				// 自動更新OFFボタンをクリック可能に
				mButton3.setEnabled(true);

				// バックグラウンドでも動作を可能とするためにServiceを呼び出す
				startService(new Intent(MainActivity.this, AutoGetLocation.class));
			}
		});

		// 自動更新OFFボタンが押された時の処理
		mButton3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// 自動更新ONボタンをクリック可能に
				mButton2.setEnabled(true);

				// 自動更新OFFボタンをクック禁止に
				mButton3.setEnabled(false);

				// Serviceを停止する.
				stopService(new Intent(MainActivity.this, AutoGetLocation.class));
			}
		});*/
		/**************************************************************************************************/	
		
	
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 private CompoundButton.OnCheckedChangeListener tb1_OnCheckedChangeListener =   
		        new CompoundButton.OnCheckedChangeListener(){  
		            @Override  
		            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {  
		            	if(isChecked==true){
		            		//自動更新を始める
		            		startService(new Intent(MainActivity.this, AutoGetLocation.class));
		            	}else{
		            		//自動更新を終了する
		            		stopService(new Intent(MainActivity.this, AutoGetLocation.class));
		            	}
		            	
		           }  
		        };  
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*****設定画面において、マーカー配置ボタンが押されたならば、flag2をtrueにする*****/
	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {  
		if(requestCode == SUB_ACTIVITY){
			{	
				if(resultCode == 100){
					flag2 =true;
				}	
			}
		}
	}
	/*********************************************************************************/

	
	
	/***************************************** 初期位置を設定するメソッド***********************************/
	protected void moveToFirstRocation() {
		// 法政大学が中心となるように移動
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(
				35.710085, 139.523088), 13);
		map.moveCamera(cu);

		// マーカーを打つ位置を法政大学に設定
		LatLng position2 = new LatLng(35.710085, 139.523088);
		options2.position(position2);

		// 法政大学にマーカーを打つ
		options2.title("法政大学");
		map.addMarker(options2);
	}
	/*******************************************************************************************************/
	
	

	/********************データベースから位置情報を取得しマーカーを配置するメソッド*******************************/
	public void putmarker() {
		/*************データベースから位置情報を取得******************/
		GetAllLocation post = new GetAllLocation(this);
		post.execute();
		
		//１秒待つ
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		
		//データベースから受け取ったJSON形式のデータを分解し、配列に格納する
		try {
			JSONArray jsonArray = new JSONArray(json);
			database_name = new String[jsonArray.length()];
			database_latitude = new double[jsonArray.length()];
			database_longitude = new double[jsonArray.length()];
			database_time = new String[jsonArray.length()];
			database_id = new int[jsonArray.length()];
			database_number = jsonArray.length();
			for (int i = 0; i < database_number; i++) {

				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				database_name[i] = jsonObject.getString("name");
				database_latitude[i] = jsonObject.getDouble("latitude");
				database_longitude[i] = jsonObject.getDouble("longitude");
				database_time[i] = jsonObject.getString("time");
				database_id[i] = jsonObject.getInt("icon_id");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/**************************************************************/
		
		
		
		/************取得した位置情報をもとにマーカーを配置***************************************************/
		//画像を追加したときに変更するところ
		for (int i = 0; i < database_number; ++i) {
			// 緯度、経度を読み込み、マーカーを打つ位置を設定する
			LatLng position = new LatLng(database_latitude[i], database_longitude[i]);
			options1.position(position);
			
			switch(database_id[i]){
    		case 0: icon = BitmapDescriptorFactory.fromResource(R.drawable.totoro); break;
    		case 1: icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); break;
    		case 2: icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN); break;
    		case 3: icon = BitmapDescriptorFactory.fromResource(R.drawable.footprint); break;
    	}
			options1.icon(icon);
			// マーカーを打つ
			options1.title("今ここ! at " + database_time[i] + " by" + database_name[i]);
			map.addMarker(options1);
		}
		/*****************************************************************************************************/
	}
	/*************************************************************************************************************/

	
	
	/******************************使用するマーカーを設定する************************************/
	//gazoutuikasurunara kokowohennkou
    public void icon_color(){
    	String list_result = (String)sharedpreferences.getString("list","Unselected");
    	if(list_result.equals("totoro")){
    		icon = BitmapDescriptorFactory.fromResource(R.drawable.totoro);
    		icon_id = 0;
    	}
    	else if(list_result.equals("blue")){
    		icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); 
    		icon_id = 1;
    	}
    	else if(list_result.equals("green")){
    		icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
    		icon_id = 2;
    	}
    	else if(list_result.equals("footprint")){	
    		icon = BitmapDescriptorFactory.fromResource(R.drawable.footprint);
    		icon_id =3;
    	}
    }
    /*********************************************************************************************/
    
    
    /**********初期設定において、入力された名前が"Unselected"のままの場合は、再入力させる**********/
    public void first_setting(){
    	Toast.makeText(this, "Unselected以外の名前を登録してください", Toast.LENGTH_LONG).show();
    	list_result = before_list;
		icon_color();
		Editor editor = sharedpreferences.edit();
		editor.putString("name",name_result);
		editor.commit();
		editor.putString("list",list_result);
		editor.commit();
    	startActivity(new Intent(this, Setting.class));
    }
    /**********************************************************************************************/
    
    
    /**初期設定において、入力された名前が"Unselected"以外であったのならば、重複をチェックし、データベースに登録する**/
    public void name_setting(){
    	name_result = (String)sharedpreferences.getString("name","Unselected");
    	list_result = (String)sharedpreferences.getString("list","Unselected");
		icon_color();
		
		//重複チェック
    	CheckDuplication check_duplication = new CheckDuplication(name_result,this);
		check_duplication.execute()
		;
		
		//１秒待つ
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		
		//重複していた場合は、警告をだし、再入力させる
		if(check.equals("duplication"))
		{
			Toast.makeText(this, "その名前は既に利用されています", Toast.LENGTH_LONG).show();
			name_result = before_name;
			list_result = before_list;
			icon_color();
			check = "";
			Editor editor = sharedpreferences.edit();
    		editor.putString("name",name_result);
    		editor.commit();
    		editor.putString("list",list_result);
    		editor.commit();
    		startActivity(new Intent(this, Setting.class));
		}
		//重複していなかった場合、データベースに名前とマーカー情報を登録する
		else{
			SetMyNameAndIconID set_iconID_and_name = new SetMyNameAndIconID(icon_id,name_result,this);
			set_iconID_and_name.execute();
			
			//メッセージボックスを表示し、登録が完了したことを知らせる
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    	alertDialogBuilder.setMessage("登録完了です");
	    	alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int which) {
	            }
	        });
	    	alertDialogBuilder.create();
	        alertDialogBuilder.show();
	        
			before_name = name_result;
			before_list = list_result;
			flag1 = false;	//初期設定が終了したため、flag1をfalseにする
			
			//データベースによって自動的に割り当てられたユーザーIDを取得し、記録する
			GetMyId get_my_id = new GetMyId(this,name_result);
			get_my_id.execute();
			//１秒待つ
    		try {
    			Thread.sleep(1000);
    		} catch (InterruptedException e1) {
    			// TODO 自動生成された catch ブロック
    			e1.printStackTrace();
    		}
			Editor editor = sharedpreferences.edit();
			editor.putString("userid", userid);
			editor.commit();

		}
    }
    /****************************************************************************************************************/
    
    
    
    /*******************名前が変更されたならば、重複をチェックし、データベースを更新する********************/
    public void change_name(){
    	//変更後の名前が"Unselected"の場合、警告をだし、再入力させる
    	if(((String)sharedpreferences.getString("name","Unselected")).equals("Unselected")){
    		Toast.makeText(this, "Unselected以外の名前を登録してください", Toast.LENGTH_LONG).show();
    		name_result = before_name;
			list_result = before_list;
			Editor editor = sharedpreferences.edit();
    		editor.putString("name",name_result);
    		editor.commit();
    		editor.putString("list",list_result);
    		editor.commit();
    		startActivity(new Intent(this, Setting.class));
    	}
    	//入力された名前が"Unselected"以外の場合
    	else{
    		name_result = (String)sharedpreferences.getString("name","Unselected");
    		list_result = (String)sharedpreferences.getString("list","Unselected");
    		icon_color();
    		
    		//重複チェック
    		CheckDuplication check_duplication = new CheckDuplication(name_result,this);
    		check_duplication.execute();
    		
    		//１秒待つ
    		try {
    			Thread.sleep(1000);
    		} catch (InterruptedException e1) {
    			// TODO 自動生成された catch ブロック
    			e1.printStackTrace();
    		}
    		
    		//重複していた場合は、警告をだし、再入力させる
    		if(check.equals("duplication"))
			{
    			name_result = before_name;
    			list_result = before_list;
    			icon_color();
				Toast.makeText(this, "その名前は既に利用されています", Toast.LENGTH_LONG).show();
				Editor editor = sharedpreferences.edit();
	    		editor.putString("name",name_result);
	    		editor.commit();
	    		editor.putString("list",list_result);
	    		editor.commit();
				check = "";
				startActivity(new Intent(this, Setting.class));
			}
    		
    		//重複していなかった場合は、データベースを更新する
    		else{
    			ChangeName change_name = new ChangeName(name_result,before_name,icon_id);
				change_name.execute();
				before_name = name_result;
				before_list = list_result;
				
				//メッセージボックスを表示し、変更が完了したことを知らせる
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		    	alertDialogBuilder.setMessage("変更完了です");
		    	alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int which) {
		            }
		           
		        });
		    	alertDialogBuilder.create();
		        alertDialogBuilder.show();
    		}
    	}
    }
    /*****************************************************************************************************/
    
    
    /****************マーカーが変更されたならば、データベースを更新する**************/
    public void change_icon(){
    	name_result = (String)sharedpreferences.getString("name","Unselected");
		list_result = (String)sharedpreferences.getString("list","Unselected");
		icon_color();
		ChangeIcon change_icon = new ChangeIcon(icon_id,name_result);
		change_icon.execute();
		
		//メッセージボックスを表示し、変更が完了したことを知らせる
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	
    	alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
            }
           
        });
    	alertDialogBuilder.create();
        alertDialogBuilder.show();
    }
    /********************************************************************************/
}
