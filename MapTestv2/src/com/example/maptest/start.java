package com.example.maptest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class start extends Activity {
	ImageButton start_button;
	Handler hdl = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  // スプラッシュ用のビューを取得する
	  setContentView(R.layout.start);
	  start_button = (ImageButton) findViewById(R.id.start_button);
	  
	}
	protected void onResume(){
		super.onResume();
		start_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hdl.post(new Runnable(){
					@Override
					public void run() {
						// スプラッシュ完了後に実行するActivityを指定します。
						Intent intent = new Intent(getApplication(), MainActivity.class);
						startActivity(intent);
						// SplashActivityを終了させます。
						start.this.finish();
					}
				});
			}
		});
	}

}

