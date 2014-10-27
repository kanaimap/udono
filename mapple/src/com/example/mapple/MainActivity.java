package com.example.mapple;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener{
	
	//map操作用変数
	GoogleMap map;
	
	 LatLng NOW ;
	 double myLongitude;
	 double myLatitude;
	 int firstswitch = 0;
	 MarkerOptions options; //markerの設定
	 BitmapDescriptor icon;//markerのアイコンの設定
	 String icon_list;
	 SharedPreferences sharedpreferences;
	 ListPreference listpreference;
	 
	 //初期画面を構成
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		MapsInitializer.initialize(getApplicationContext());
		
		MapsInitializer.initialize(this);
		options = new MarkerOptions();
		map = ((SupportMapFragment)
				getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setMyLocationEnabled(true);
		
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
	}
	

	//ユーザーの操作待ち
	@Override
	protected void onResume(){
		super.onResume();
		UiSettings settings = map.getUiSettings();
		// 現在位置表示の有効化
		map.setMyLocationEnabled(true);
		// ズームイン・アウトボタンの有効化
		settings.setZoomControlsEnabled(true);
		// 回転ジェスチャーの有効化
		settings.setCompassEnabled(true);
		
		//ロングタップでの動作:マーカーを置く
		map.setOnMapLongClickListener(new OnMapLongClickListener(){
			@Override
			public void onMapLongClick(LatLng point){

		    	//スニペット:ユーザー名を取得
		    	String name_result = (String)sharedpreferences.getString("name","unknown");
		    	
				options.position(point);
				icon_color();
				options.icon(icon);
				options.title("ここ！");
				options.snippet(name_result);
				map.addMarker(options);

			}
		});
	}
	  @Override
	    public void onProviderDisabled(String provider) {}
	    @Override
	    public void onProviderEnabled(String provider) {}
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    
	    
	    //マップの位置情報が取得出来た時に呼び出されるメソッド
	    @Override
	    public void onLocationChanged(Location location) {
	    	 myLatitude = location.getLatitude();
	          //経度を取得
	        myLongitude = location.getLongitude();

	        //LatLngBoundは南西地点と北東地点から矩形領域を作成する。
	        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
	   
	          //LatLngに地理的な座標の点、つまり緯度と経度を格納
	          //LatLng position = new LatLng(myLatitude,myLongitude);
	          NOW = new LatLng(myLatitude, myLongitude);//NOW
	          
	          if(firstswitch == 0){
		          map.addMarker(new MarkerOptions()
					.position(NOW)
					.title("現在地")
					.snippet("なうなう！")
					);
		          	moveCameraToNow(true);
		          	firstswitch = 1;
	          }
	    }
	    
	    //現在地へとぶ
	    private void moveCameraToNow(boolean isAnimation){
	    	CameraUpdate camera = CameraUpdateFactory
	    			.newCameraPosition(new CameraPosition.Builder()
	    								.target(NOW)
	    								.zoom(15.0f).build());
	    	if(isAnimation){
	    		map.animateCamera(camera);
	    	}
	    	else{
	    		map.moveCamera(camera);
	    	}
	    	
	    }
	    
	     //メッセージを表示する
	    protected void showMessage(String msg){
			Toast.makeText(
				this, 
				msg, Toast.LENGTH_SHORT).show();
		}
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
				startActivity(new Intent(this, Setting.class));
		            return true;
				default:
					break;
			}
			return false;
		}
	    
	    //マーカーの色情報を設定からとる
	    public void icon_color(){
	    	String list_result = (String)sharedpreferences.getString("list","unknown");
			if(list_result.equals("totoro")){
				icon = BitmapDescriptorFactory.fromResource(R.drawable.totoro);
			}
			else if(list_result.equals("blue")){
				icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
			}
			else if(list_result.equals("green")){
				icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
			}
	    }
}