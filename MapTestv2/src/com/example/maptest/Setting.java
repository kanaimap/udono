package com.example.maptest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


//名前とマーカー情報を保存するためのPreference
public class Setting extends PreferenceActivity 
														implements OnPreferenceChangeListener{
	
	 // Preference
	private ListPreference list;
	private ListPreference ashiato;
	private EditTextPreference name;
	private EditTextPreference interval;
	private EditTextPreference comment;
	

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref);
		 // Preferenceの取得
		list = (ListPreference)findPreference("list");
		ashiato = (ListPreference)findPreference("ashiato");
		name = (EditTextPreference)findPreference("name");
		comment = (EditTextPreference)findPreference("comment");
		interval = (EditTextPreference)findPreference("interval");
		
		// リスナーを設定する
		list.setOnPreferenceChangeListener(this);
		ashiato.setOnPreferenceChangeListener(this);
		name.setOnPreferenceChangeListener(this);
		comment.setOnPreferenceChangeListener(this);
		interval.setOnPreferenceChangeListener(this);
		
		Intent intent = getIntent();
		String data = intent.getStringExtra("key");
		
		// 保存されたデータを読み込む
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		
		// 値の取得
		String param_list = p.getString("list", "Unselected");
		String ashiato_list = p.getString("ashiato", "Unselected");
		String param_name = p.getString("name", "Unselected");
		String param_comment = p.getString("comment","今ここ");
		String param_interval = p.getString("interval","500");
		
		// サマリーの設定
		setSummary(list, param_list);
		setSummary(ashiato, ashiato_list);
		setSummary(name, param_name);
		setSummary(comment,param_comment);
		setSummary(interval,param_interval);
		//マーカー削除ボタン
		Preference button1 = (Preference)findPreference("button1");
		button1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) { 
				Intent intent_ret = new Intent();
				intent_ret.putExtra("ReturnDate","marker_set");
				setResult(100,intent_ret);
				finish();
				return true;
			}
		});	
	
	}
	
	 public boolean onPreferenceChange(android.preference.Preference preference,	
			 Object newValue) {
			  
			 if(newValue != null){
				 // newValueの型でサマリーの設定を分ける
				 if(newValue instanceof String){
				  
						 // preferenceの型でサマリーの設定を分ける
						 if(preference instanceof ListPreference){
							 setSummary((ListPreference)preference, (String)newValue);
						 }
						else if(preference instanceof EditTextPreference){
							setSummary((EditTextPreference)preference, (String)newValue);
						}
				 }
				 return true;
			 }
			 return false;
	 }
	 // Summaryを設定（リスト）
	 public void setSummary(ListPreference lp, String param){
	  
			 if(param == null){
				 lp.setSummary("Unselected");
			 }else{
				 lp.setSummary(param);
			 }
			 param = null;
	 }
	 // Summaryを設定（エディットテキスト）
	 private void setSummary(EditTextPreference ep, String param) {
	  
		 if(param == null){
			 ep.setSummary("Unselected");
		 }else{
			 ep.setSummary( param);
		 }
		 param = null;
	 }
	 /*
	 // Summaryを設定（チェックボックス）
	 public void setSummary(CheckBoxPreference cp, Boolean param){
	  
		 cp.setSummary("Selected「" + param + "」");
		 param = false;
	 }*/
	 
}
