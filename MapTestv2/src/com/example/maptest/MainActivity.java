package com.example.maptest;

import static java.lang.Math.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	// map操作用変数
	GoogleMap map;

	// 現在地に打つマーカ-
	MarkerOptions options1 = new MarkerOptions();

	// 法政大学に打つマーカ
	MarkerOptions options2 = new MarkerOptions();

	// 足跡配置用
	MarkerOptions options3 = new MarkerOptions();

	// 現在地強調表示用
	MarkerOptions options4 = new MarkerOptions();

	// 配置されたすべてのマーカーの情報を補完する配列。マーカーの全削除に利用する
	ArrayList<Marker> all_marker_list = new ArrayList<Marker>();

	// ボタン用変数
	Button SettingB, NowB, DoukiB,User;
	ToggleButton AutoB, Clear2B;
	int marker_switch = 0;
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////

	// タイムゾーン取得用変数
	TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");

	MainActivity main = this;

	BitmapDescriptor icon;

	// 使用アイコン
	int icon_id;

	// ユーザーID
	String userid;

	// 不正終了情報を格納する変数
	String abnormal_termination = "none";

	// Preference取得用変数
	SharedPreferences sharedpreferences;

	// 現在利用している名前を保管する変数
	String name_result;
	// ひとつ前に利用していた名前を保管する変数
	String before_name;
	// 現在利用しているマーカーの情報を保管する変数
	String list_result;
	// ひとつ前に利用していたマーカーの情報を保管する変数
	String before_list;
	// マーカーに付与するコメントを保管する変数
	String comment;

	// 名前の重複チェック用変数
	String check = "";

	// JSON形式の位置情報を処理するための変数群
	String json = "";
	int database_number;
	ArrayList<Integer> database_id = new ArrayList<Integer>();
	ArrayList<Double> database_latitude = new ArrayList<Double>();
	ArrayList<Double> database_longitude = new ArrayList<Double>();
	ArrayList<String> database_name = new ArrayList<String>();
	ArrayList<String> database_time = new ArrayList<String>();
	ArrayList<String> database_comment = new ArrayList<String>();

	// 初期設定が必要であるかを判断するフラグ
	boolean flag1 = false;
	// 設定画面において、マーカー配置が押された場合にtureとなるフラグ
	boolean flag2 = false;
	// サーバーが稼働しているか否かを格納するフラグ
	boolean server = false;

	boolean flag3 = false;

	static final int SUB_ACTIVITY = 1001;

	// 現在時刻取得用の変数群
	Calendar calendar;
	String time;
	SimpleDateFormat df;

	// http通信用変数
	Http.Request request;
	Http.Response response;

	// 初期画面を構成
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MapsInitializer.initialize(getApplicationContext());

		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
		}

		// 現在設定されている名前とマーカー、ユーザーIDを読み込む
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		list_result = (String) sharedpreferences
				.getString("list", "Unselected");
		before_list = (String) sharedpreferences
				.getString("list", "Unselected");
		name_result = (String) sharedpreferences
				.getString("name", "Unselected");
		before_name = (String) sharedpreferences
				.getString("name", "Unselected");
		userid = (String) sharedpreferences.getString("userid", "Unselected");
		icon_color();
		options1.icon(icon);

		// マップを表示する
		setContentView(R.layout.activity_main);
		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		MapsInitializer.initialize(this);

		// 足跡配置用optionsの設定
		options3.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.footprint));

		// 現在地強調表示用のoptionの設定
		options4.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.now_nomal));

		// ボタン割り当て mButton1:nowボタン mButton2:マーカー全削除ボタン mButton3:ユーザ一覧表示ボタン
		// tb1:自動更新機能のON/OFFボタン
		// mButton2およびmButton3はテスト用の仮配置ボタン
		NowB = (Button) findViewById(R.id.NowB);
		SettingB = (Button) findViewById(R.id.SettingB);
		DoukiB = (Button) findViewById(R.id.DoukiB);
		AutoB = (ToggleButton) findViewById(R.id.AutoB);
		AutoB.setOnCheckedChangeListener(autob_OnCheckedChangeListener);
		Clear2B = (ToggleButton) findViewById(R.id.Clear2B);
		Clear2B.setOnCheckedChangeListener(clear2b_OnCheckedChangeListener);
		User = (Button) findViewById(R.id.User);

		// 初期位置を法政大学とする
		moveToFirstRocation();

		// 名前とユーザーIDが共に設定されていないならば、初期設定へ
		if (name_result.equals("Unselected") && userid == "Unselected") {
			flag1 = true;
			Editor editor = sharedpreferences.edit();
			editor.putString("list", "totoro");
			editor.commit();
			list_result = (String) sharedpreferences.getString("list",
					"Unselected");
			before_list = (String) sharedpreferences.getString("list",
					"Unselected");
		}

		/****************************** サーバーの稼働状況を調べる *******************************/
		request = new Http.Request();
		request.url = "http://10.29.31.1/check_server.php";

		// requeatSyncは通信終了まで待機する同期通信用メソッド
		// 8秒でタイムアウトするように設定してあり、タイムアウトした場合は"404"という文字列が返ってくる
		response = Http.requestSync(request,
				StringResponseHandler.getInstance());

		// タイムアウトした場合
		if (((String) response.value).equals("404")) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setMessage("現在サーバーが利用できません");
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialogBuilder.create();
			alertDialogBuilder.show();
		}

		// タイムアウトしなかった場合
		else if (response.code == 200) {
			// 受信した文字列を取得
			if (((String) response.value).equals("OK")) {
				server = true;
			}
		}
		// 以上の処理でサーバが稼働していればserver = true、稼働していなければserver = falseとなる
		// sever = falseの場合、以降のあらゆるhttp通信処理が実行されないようになっている
		/****************************************************************************************/

		// 端末側の名前とデータベース側の名前の矛盾をチェックする
		if (!flag1 && server) {
			request = new Http.Request();
			request.url = "http://10.29.31.1/check_name.php";
			request.params.add(new Http.Param(Http.Param.TYPE_STRING, "name",
					name_result));
			request.params.add(new Http.Param(Http.Param.TYPE_STRING, "id",
					userid));
			// 同期通信、タイムアウト8秒
			response = Http.requestSync(request,
					StringResponseHandler.getInstance());
			// タイムアウトした場合は警告を出す
			if (((String) response.value).equals("404")) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				alertDialogBuilder.setMessage("現在サーバーが利用できません");
				alertDialogBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				alertDialogBuilder.create();
				alertDialogBuilder.show();
				server = false;
			}
			// HTTPステータスコードが200
			else if (response.code == 200) {
				// 受信した文字列を取得
				abnormal_termination = (String) response.value;

			}

			// 矛盾していた場合は修正する
			if (!(abnormal_termination.equals("none"))) {
				Editor editor = sharedpreferences.edit();
				editor.putString("name", abnormal_termination);
				editor.commit();
				name_result = abnormal_termination;
				before_name = abnormal_termination;
			}

		}
		// ズームボタンと現在地取得ボタンを可視化
		UiSettings settings = map.getUiSettings();
		settings.setZoomControlsEnabled(true);
		map.setMyLocationEnabled(true);

		if (!server) {
			AutoB.setClickable(false);
		}
	}

	// メッセージを表示する
	protected void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	// menuの設定
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// menuのアイテムを押したときの行動
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			showMessage("open the setting");
			startActivityForResult(new Intent(this, Setting.class),
					SUB_ACTIVITY);
		default:
			break;
		}
		return true;
	}

	// ユーザーの操作待ち
	@Override
	protected void onResume() {
		super.onResume();

		/******************************* flag1がtrueならば初期設定を行う ******************************/
		if (flag1) {
			// 入力された名前が"Unselected"である場合、警告を出し再入力させる
			if (((String) sharedpreferences.getString("name", "Unselected"))
					.equals("Unselected")) {
				first_setting();
			}
			// 入力された名前が"Unselected"以外の場合、重複をチェックする
			else {
				name_setting();
			}
		}
		/**********************************************************************************************/

		/********************* flag1がfalseかつ名前が変更されていれば、名前変更処理へ ************************/
		else if (!flag1
				&& !((String) sharedpreferences.getString("name", "Unselected"))
						.equals(name_result) && server) {
			change_name();
		}
		/*****************************************************************************************************/

		/******************* flag1がfalseかつマーカーが変更されいれば、マーカー変更処理へ ********************/
		else if (!flag1
				&& !((String) sharedpreferences.getString("list", "Unselected"))
						.equals(list_result) && server) {
			change_icon();
		}
		/*****************************************************************************************************/

		/******************************** マーカーの全削除(仮) *****************************/
		if (flag2 && server) {
			/*
			 * putmarker(); flag2 = false;
			 */
			if (all_marker_list.size() > 0) {
				for (int i = 0; i < all_marker_list.size(); ++i) {
					all_marker_list.get(i).remove();
				}
				all_marker_list.clear();
				flag2 = false;
			}
		}
		/*********************************************************************************/


		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				if (marker_switch == 1) {
					Toast.makeText(getApplicationContext(), "マーカー削除",
							Toast.LENGTH_LONG).show();
					marker.remove();
				}
				return false;
			}
		});

		/*************************** ロングクリックによる現在地取得 ***************************/
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng point) {

				if (marker_switch == 0) {
					// 現在地を取得する
					double mylat = point.latitude;
					double mylon = point.longitude;
					options1.position(point);
					// 名前を取得する
					String name_result = (String) sharedpreferences.getString(
							"name", "Unselected");
					// マーカー画像を設定する
					icon_color();
					// 現在時刻を取得する
					calendar = Calendar.getInstance(tz);
					df = new SimpleDateFormat("HH:mm:ss", Locale.JAPANESE);
					time = df.format(calendar.getTime());

					options1.icon(icon);
					// マーカー配置

					comment = (String) sharedpreferences.getString("comment",
							"今ここ");

					if (server) {
						request = new Http.Request();
						request.url = "http://10.29.31.1/insert_mysql.php";
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "lat", String
										.valueOf(mylat)));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "lon", String
										.valueOf(mylon)));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "time", time));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "name", name_result));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "comment", comment));
						// 非同期通信
						Http.request(request,
								StringResponseHandler.getInstance());
					}

					options1.title(comment + "at " + time + " by" + name_result);
					all_marker_list.add(map.addMarker(options1));

				}
			}
		});
		/**************************************************************************************/

		/************************************ Nowボタンが押された時の処理 ****************************************/
		NowB.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 現在地が取得できない場合は、何もしない
				if (map.getMyLocation() == null)
					;
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
					df = new SimpleDateFormat("HH:mm:ss", Locale.JAPANESE);
					time = df.format(calendar.getTime());

					icon_color();
				//	icon = BitmapDescriptorFactory.fromResource(R.drawable.pengin);
					options1.icon(icon);

					// アイコンを配置
					options1.title("今ここ！at " + time + " by" + name_result);
					map.addMarker(options1);
					all_marker_list.add(map.addMarker(options1));

					// メッセージボックスを表示し、マーカーを配置したことを知らせる
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							main);
					alertDialogBuilder.setMessage("マーカーを配置しました");
					alertDialogBuilder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					alertDialogBuilder.create();
					alertDialogBuilder.show();
					
					if (server) {
						request = new Http.Request();
						request.url = "http://10.29.31.1/insert_mysql.php";
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "lat", String
										.valueOf(mylat)));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "lon", String
										.valueOf(mylon)));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "time", time));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "name", name_result));
						request.params.add(new Http.Param(
								Http.Param.TYPE_STRING, "comment", "今ここ"));
						// 非同期通信
						Http.request(request,
								StringResponseHandler.getInstance());
					}

				}
			}
		});
		/*********************************************************************************************************/

		/**** サーバが稼働していない場合、名前とアイコンの変更処理を無効化する ****/
		if (!server) {
			name_result = before_name;
			list_result = before_list;
			Editor editor = sharedpreferences.edit();
			editor.putString("name", name_result);
			editor.putString("list", list_result);
			editor.commit();
		}
		/************************************************************************/

		SettingB.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 現在地が取得できない場合は、何もしない
				if (map.getMyLocation() == null)
					;
				else {
					Intent intent = new Intent();
					intent.setClassName("com.example.maptest",
							"com.example.maptest.Setting");
					showMessage("open the setting");
					startActivity(intent);
				}
			}
		});

		/*********************************** Doukiボタンが押された時の処理 ******************************************/
		DoukiB.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				putmarker();
			}
		});

		/*****************************************************************************************************/
		
		User.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(server){
					Intent intent = new Intent(getApplication(), MemberList.class);
					startActivity(intent);
				}
			}
		});
	}

	/************************************ Clearボタンが押された時の処理 *****************************************/
	private CompoundButton.OnCheckedChangeListener clear2b_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked == true) {
				// マーカー設置モード
				marker_switch = 0;
			} else {
				// マーカー削除モード
				marker_switch = 1;
			}

		}
	};
	/*****************************************************************************************************/
	
	

	// 自動更新ボタン//////////////////////////////////////////↓ここの部分///////////
	private CompoundButton.OnCheckedChangeListener autob_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked == true) {
				// 自動更新を始める
				startService(new Intent(MainActivity.this,
						AutoGetLocation.class));
			} else {
				// 自動更新を終了する
				stopService(new Intent(MainActivity.this, AutoGetLocation.class));
			}

		}
	};

	/****************************************************************************************************************************/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Serviceを停止する.
		stopService(new Intent(MainActivity.this, AutoGetLocation.class));
	}

	/***** 設定画面において、マーカー削除ボタンが押されたならばflag2をtrue、ユーザ一覧ボタンが押されたならばflag3をtrue *****/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SUB_ACTIVITY) {
			{
				if (resultCode == 100) {
					flag2 = true;
				} else if (resultCode == 101) {
					if (server) {
						flag3 = true;
					}

				}
			}
		}
	}

	/************************************************************************************************************************/

	/***************************************** 初期位置を設定するメソッド ***********************************/
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

	/*********************************************************************************************************/

	/**************************************** データベースから位置情報を取得しマーカーを配置するメソッド *****************************************/
	public void putmarker() {

		/******************* データベースから位置情報を取得 ********************/
		request = new Http.Request();
		request.url = "http://10.29.31.1/get_mysql.php";
		// 同期通信　タイムアウト8秒
		response = Http.requestSync(request, JSONResponseHandler.getInstance());
		// タイムアウトした場合はトーストで知らせる
		if (((String) response.value).equals("404")) {
			Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
			return;
		}

		json = (String) response.value;

		// 一旦すべてのマーカーを削除する
		if (all_marker_list.size() > 0) {
			for (int i = 0; i < all_marker_list.size(); ++i) {
				all_marker_list.get(i).remove();
			}
			all_marker_list.clear();
		}
		// データベースから受け取ったJSON形式のデータを分解し、配列に格納する
		try {
			JSONArray jsonArray = new JSONArray(json);

			database_number = jsonArray.length();

			for (int i = 0; i < database_number; i++) {

				JSONObject jsonObject = jsonArray.getJSONObject(i);

				database_name.add(jsonObject.getString("name"));
				database_latitude.add(jsonObject.getDouble("latitude"));
				database_longitude.add(jsonObject.getDouble("longitude"));
				database_time.add(jsonObject.getString("time"));
				database_id.add(jsonObject.getInt("icon_id"));
				database_comment.add(jsonObject.getString("comment"));
			}
			jsonArray = null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/************************************************************************/

		/*************************** 取得した位置情報をもとにマーカーを配置 ***************************************************/
		for (int i = 0; i < database_number; ++i) {
			// 緯度、経度を読み込み、マーカーを打つ位置を設定する
			LatLng position = new LatLng(database_latitude.get(i),
					database_longitude.get(i));
			options1.position(position);

			// 使用するマーカーの画像を設定
			switch (database_id.get(i)) {
			case 0:
				icon = BitmapDescriptorFactory.fromResource(R.drawable.totoro);
				break;
			case 1:
				icon = BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
				break;
			case 2:
				icon = BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
				break;
			}
			options1.icon(icon);
			// マーカーを打つ
			options1.title(database_comment.get(i) + "at "
					+ database_time.get(i) + " by" + database_name.get(i));
			all_marker_list.add(map.addMarker(options1));

			if (i != 0) {
				// ひとつ前のマーカーと現在のマーカーとでユーザが異なる場合、ひとつ前のマーカーに重ねるように現在地強調表示用のマーカーを配置する
				if (!(database_name.get(i - 1).equals(database_name.get(i)))
						&& !(database_name.get(i - 1).equals(name_result))) {
					LatLng position2 = new LatLng(database_latitude.get(i - 1),
							database_longitude.get(i - 1));
					options4.title("今ここ!" + "at" + database_time.get(i - 1)
							+ "by" + database_name.get(i - 1));
					options4.position(position2);
					all_marker_list.add(map.addMarker(options4));
				}
				// 配置したマーカーが最後であり、かつ自分のマーカーでない場合、配置したマーカーに重ねるように現在地強調表示用のマーカーを配置する
				else if (i == (database_number - 1)
						&& !(database_name.get(i - 1).equals(name_result))) {
					LatLng position2 = new LatLng(database_latitude.get(i),
							database_longitude.get(i));
					options4.title("今ここ!" + "at" + database_time.get(i) + "by"
							+ database_name.get(i));
					options4.position(position2);
					all_marker_list.add(map.addMarker(options4));
				}
			}

			if (i != 0 && i < database_number) {
				if (database_name.get(i).equals(database_name.get(i - 1))) {
					// 2点間の距離を格納する変数
					double distance;
					// 配置する足跡の数を格納する変数
					int footprint_number;
					// 2点間の距離を求める
					distance = distance(database_latitude.get(i - 1),
							database_latitude.get(i),
							database_longitude.get(i - 1),
							database_longitude.get(i)) * 1000;
					// 配置する足跡の数を求める
					int footprint_interval = Integer
							.parseInt((String) sharedpreferences.getString(
									"interval", "500").trim());
					footprint_number = (int) (distance / footprint_interval);

					double footprint_latitude;
					double footprint_longitude;
					double footprint_X, footprint_Y, deg, angle;
					float footprint_angle;
					for (int j = 0; j < footprint_number - 1; j++) {
						// 足跡を配置する位置を求める
						footprint_latitude = database_latitude.get(i - 1)
								+ (database_latitude.get(i) - database_latitude
										.get(i - 1)) * (j + 1)
								/ footprint_number;
						footprint_longitude = database_longitude.get(i - 1)
								+ (database_longitude.get(i) - database_longitude
										.get(i - 1)) * (j + 1)
								/ footprint_number;
						LatLng foot_temp = new LatLng(footprint_latitude,
								footprint_longitude);

						// 足跡の向きを求める
						footprint_X = cos(database_latitude.get(i - 1))
								* sin(database_latitude.get(i))
								- sin(database_latitude.get(i - 1))
								* cos(database_latitude.get(i))
								* cos(database_longitude.get(i)
										- database_longitude.get(i - 1));
						footprint_Y = sin(database_longitude.get(i)
								- database_longitude.get(i - 1))
								* cos(database_latitude.get(i));
						deg = toDegrees(atan2(footprint_Y, footprint_X));
						angle = (deg + 360) % 360;
						footprint_angle = (float) (abs(angle) + (1 / 7200));

						// 足跡を配置する
						options3.position(foot_temp);
						if (footprint_angle < 0) {
							footprint_angle = -footprint_angle;
						} else {
							footprint_angle = 360 - footprint_angle;
						}
						options3.rotation(footprint_angle);
						all_marker_list.add(map.addMarker(options3));
					}
				}
			}
		}
		/**********************************************************************************************************************/
		// 配列のクリア
		database_name.clear();
		database_latitude.clear();
		database_longitude.clear();
		database_time.clear();
		database_id.clear();
		database_comment.clear();
	}

	/*********************************************************************************************************************************************/

	/****************************** 使用するマーカーを設定する ************************************/
	public void icon_color() {
		String list_result = (String) sharedpreferences.getString("list",
				"Unselected");
		if (list_result.equals("totoro")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.totoro);
			icon_id = 0;
		} else if (list_result.equals("blue")) {
			icon = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
			icon_id = 1;
		} else if (list_result.equals("green")) {
			icon = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
			icon_id = 2;
		}
		else if (list_result.equals("akaka")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.akaka);
			icon_id = 3;
		}
		else if (list_result.equals("cat")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.cat);
			icon_id = 4;
		}
		else if (list_result.equals("kuma")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.kuma);
			icon_id = 5;
			
		}
		else if (list_result.equals("obake")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.obake);
			icon_id = 5;
			
		}
		else if (list_result.equals("pen")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.pen);
			icon_id = 5;
			
		}
		else if (list_result.equals("pengin")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.pengin);
			icon_id = 5;
			
		}
		else if (list_result.equals("toge")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.toge);
			icon_id = 5;
			
		}
		else if (list_result.equals("yajirushi")) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.yajirushi);
			icon_id = 5;
			
		}
	}

	/*********************************************************************************************/

	/********** 初期設定において、入力された名前が"Unselected"のままの場合は、再入力させる **********/
	public void first_setting() {
		Toast.makeText(this, "Unselected以外の名前を登録してください", Toast.LENGTH_LONG)
				.show();
		list_result = before_list;
		icon_color();
		Editor editor = sharedpreferences.edit();
		editor.putString("name", name_result);
		editor.commit();
		editor.putString("list", list_result);
		editor.commit();
		startActivity(new Intent(this, Setting.class));
	}

	/************************************************************************************************/

	/** 初期設定において、入力された名前が"Unselected"以外であったのならば、重複をチェックし、データベースに登録する **/
	public void name_setting() {
		name_result = (String) sharedpreferences
				.getString("name", "Unselected");
		list_result = (String) sharedpreferences
				.getString("list", "Unselected");
		icon_color();

		request = new Http.Request();
		request.url = "http://10.29.31.1/check_duplication.php";
		// 同期通信　タイムアウト8秒
		response = Http.requestSync(request,
				StringResponseHandler.getInstance());

		// 通信に成功した場合
		if (((String) response.value).equals("duplication")) {
			main.check = (String) response.value;
		}
		// タイムアウトした場合、ロールバックする
		else if (((String) response.value).equals("404")) {
			Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
			name_result = before_name;
			list_result = before_list;
			Editor editor = sharedpreferences.edit();
			editor.putString("name", name_result);
			editor.commit();
			editor.putString("list", list_result);
			editor.commit();
			return;
		}

		// 重複していた場合は、警告をだし、再入力させる
		if (check.equals("duplication")) {
			Toast.makeText(this, "その名前は既に利用されています", Toast.LENGTH_LONG).show();
			name_result = before_name;
			list_result = before_list;
			icon_color();
			check = "";
			Editor editor = sharedpreferences.edit();
			editor.putString("name", name_result);
			editor.commit();
			editor.putString("list", list_result);
			editor.commit();
			startActivity(new Intent(this, Setting.class));
		}
		// 重複していなかった場合、データベースに名前とマーカー情報を登録する
		else {
			request = new Http.Request();
			request.url = "http://10.29.31.1/set_iconID_and_name.php";
			request.params.add(new Http.Param(Http.Param.TYPE_STRING, "name",
					name_result));
			request.params.add(new Http.Param(Http.Param.TYPE_STRING,
					"ICON_ID", String.valueOf(icon_id)));
			// 同期通信 タイムアウト8秒
			response = Http.requestSync(request,
					StringResponseHandler.getInstance());

			// タイムアウトした場合、ロールバックする
			if (((String) response.value).equals("404")) {
				Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
				name_result = before_name;
				list_result = before_list;
				Editor editor = sharedpreferences.edit();
				editor.putString("name", name_result);
				editor.commit();
				editor.putString("list", list_result);
				editor.commit();
				return;
			}

			// メッセージボックスを表示し、登録が完了したことを知らせる
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setMessage("登録完了");
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialogBuilder.create();
			alertDialogBuilder.show();

			before_name = name_result;
			before_list = list_result;
			flag1 = false; // 初期設定が終了したため、flag1をfalseにする

			// データベースから割り振られるユーザIDを受信する
			request = new Http.Request();
			request.url = "http://10.29.31.1/get_my_id.php";
			request.params.add(new Http.Param(Http.Param.TYPE_STRING, "name",
					name_result));
			// 同期通信 タイムアウト8秒
			response = Http.requestSync(request,
					StringResponseHandler.getInstance());

			// タイムアウトした場合、警告を出す。
			// このタイミングでタイムアウトが発生した場合、現在対処は不可能。
			// アプリのデータを削除する必要がある。
			// 前提条件として、この処理を行う前に２回通信に成功していることが必要であるため、
			// ここでのみタイムアウトする可能性は低い。
			if (((String) response.value).equals("404")) {
				Toast.makeText(this, "致命的なエラー", Toast.LENGTH_SHORT).show();
				return;
			}
			userid = (String) response.value;

			Editor editor = sharedpreferences.edit();
			editor.putString("userid", userid);
			editor.commit();

		}
	}

	/****************************************************************************************************************/

	/******************* 名前が変更されたならば、重複をチェックし、データベースを更新する ********************/
	public void change_name() {
		// 変更後の名前が"Unselected"の場合、警告をだし、再入力させる
		if (((String) sharedpreferences.getString("name", "Unselected"))
				.equals("Unselected")) {
			Toast.makeText(this, "Unselected以外の名前を登録してください", Toast.LENGTH_LONG)
					.show();
			name_result = before_name;
			list_result = before_list;
			Editor editor = sharedpreferences.edit();
			editor.putString("name", name_result);
			editor.commit();
			editor.putString("list", list_result);
			editor.commit();
			startActivity(new Intent(this, Setting.class));
		}
		// 入力された名前が"Unselected"以外の場合
		else {
			name_result = (String) sharedpreferences.getString("name",
					"Unselected");
			list_result = (String) sharedpreferences.getString("list",
					"Unselected");
			icon_color();

			// 重複チェック
			request = new Http.Request();
			request.url = "http://10.29.31.1/check_duplication.php";
			// 同期通信　タイムアウト8秒
			response = Http.requestSync(request,
					StringResponseHandler.getInstance());
			// タイムアウトした場合、ロールバックする
			if (((String) response.value).equals("404")) {
				Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
				name_result = before_name;
				list_result = before_list;
				Editor editor = sharedpreferences.edit();
				editor.putString("name", name_result);
				editor.commit();
				editor.putString("list", list_result);
				editor.commit();
				return;
			} else if (((String) response.value).equals("duplication")) {
				main.check = (String) response.value;
			}

			// 重複していた場合は、警告をだし、再入力させる
			if (check.equals("duplication")) {
				name_result = before_name;
				list_result = before_list;
				icon_color();
				Toast.makeText(this, "その名前は既に利用されています", Toast.LENGTH_LONG)
						.show();
				Editor editor = sharedpreferences.edit();
				editor.putString("name", name_result);
				editor.commit();
				editor.putString("list", list_result);
				editor.commit();
				check = "";
				startActivity(new Intent(this, Setting.class));
			}

			// 重複していなかった場合は、データベースを更新する
			else {
				request = new Http.Request();
				request.url = "http://10.29.31.1/change_name.php";
				request.params.add(new Http.Param(Http.Param.TYPE_STRING,
						"name_result", name_result));
				request.params.add(new Http.Param(Http.Param.TYPE_STRING,
						"before_name", before_name));
				request.params.add(new Http.Param(Http.Param.TYPE_STRING,
						"icon_id", String.valueOf(icon_id)));
				// 同期通信　タイムアウト8秒
				Http.requestSync(request, StringResponseHandler.getInstance());
				// タイムアウトした場合、ロールバックする
				if (((String) response.value).equals("404")) {
					Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
					name_result = before_name;
					list_result = before_list;
					Editor editor = sharedpreferences.edit();
					editor.putString("name", name_result);
					editor.commit();
					editor.putString("list", list_result);
					editor.commit();
					return;
				}

				before_name = name_result;
				before_list = list_result;

				// メッセージボックスを表示し、変更が完了したことを知らせる
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				alertDialogBuilder.setMessage("変更完了です");
				alertDialogBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}

						});
				alertDialogBuilder.create();
				alertDialogBuilder.show();
			}
		}
	}

	/*********************************************************************************************************/

	/**************** マーカーが変更されたならば、データベースを更新する **************/
	public void change_icon() {
		name_result = (String) sharedpreferences
				.getString("name", "Unselected");
		list_result = (String) sharedpreferences
				.getString("list", "Unselected");
		icon_color();

		request = new Http.Request();
		request.url = "http://10.29.31.1/change_icon_id.php";
		request.params.add(new Http.Param(Http.Param.TYPE_STRING, "name",
				name_result));
		request.params.add(new Http.Param(Http.Param.TYPE_STRING, "icon_id",
				String.valueOf(icon_id)));
		// 同期通信　タイムアウト8秒
		Http.requestSync(request, StringResponseHandler.getInstance());
		// タイムアウトした場合、ロールバックする
		if (((String) response.value).equals("404")) {
			Toast.makeText(this, "タイムアウト", Toast.LENGTH_SHORT).show();
			list_result = before_list;
			Editor editor = sharedpreferences.edit();
			editor.putString("list", list_result);
			editor.commit();
			return;
		}

		before_list = list_result;
		// メッセージボックスを表示し、変更が完了したことを知らせる
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("変更完了です");
		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}

				});
		alertDialogBuilder.create();
		alertDialogBuilder.show();
	}

	/**********************************************************************************/

	public double distance(double lat1, double lat2, double lon1, double lon2) {
		double r = 6378.137; // 赤道半径[km]

		lat1 = lat1 * PI / 180;
		lon1 = lon1 * PI / 180;

		lat2 = lat2 * PI / 180;
		lon2 = lon2 * PI / 180;

		// 2点間の距離[km]
		double distance = r
				* acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2)
						* cos(lon2 - lon1));
		return distance;
	}
}
