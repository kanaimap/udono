package com.example.mapple;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;



public class Setting extends PreferenceActivity 
														implements OnPreferenceChangeListener{
	
	 // Preference
	private ListPreference list;
	private EditTextPreference name;
	 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref);
		 // Preferenceの取得
		list = (ListPreference)findPreference("list");
		name = (EditTextPreference)findPreference("name");
		
		// リスナーを設定する
		list.setOnPreferenceChangeListener(this);
		name.setOnPreferenceChangeListener(this);
		
		// 保存されたデータを読み込む
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		
		// 値の取得
		String param_list = p.getString("list", "Unselected");
		String param_name = p.getString("name", "Unselected");
		
		// サマリーの設定
		setSummary(list, param_list);
		setSummary(name, param_name);
		 
			
	}
	
	 public boolean onPreferenceChange(android.preference.Preference preference,	
			 Object newValue) {
			  
			 if(newValue != null){
				 // newValueの型でサマリーの設定を分ける
				 if(newValue instanceof String){
				  
						 // preferenceの型でサマリーの設定を分ける
						 if(preference instanceof ListPreference)
							 setSummary((ListPreference)preference, (String)newValue);
						else if(preference instanceof EditTextPreference)
							setSummary((EditTextPreference)preference, (String)newValue);
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
