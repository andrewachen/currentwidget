package com.manor.currentwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CurrentWidgetConfigure extends Activity {

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	public final static String LOG_ENABLED_SETTING = "logEnabled";
	public final static String LOG_FILENAME_SETTING = "logFilename";
	public final static String SECOND_INTERVAL_SETTING = "secondsInterval";
	public final static String UNITS_SETTING = "units";
	public final static String LOG_APPS_SETTING = "logApps";
	
	public CurrentWidgetConfigure() {
		super();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// init result as canceled
		setResult(RESULT_CANCELED);
		
		setContentView(R.layout.configure);
		
		findViewById(R.id.save_button).setOnClickListener(mOnSaveClickListener);
		
		Spinner unitsSpinner = (Spinner)findViewById(R.id.units_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.units_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitsSpinner.setAdapter(adapter);
		
		// get widget id
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
		
		SharedPreferences settings = getSharedPreferences("currentWidgetPrefs", 0);
		
		long interval = settings.getLong(SECOND_INTERVAL_SETTING + mAppWidgetId, 60);
		int unit = settings.getInt(UNITS_SETTING + mAppWidgetId, 1);
		if (unit == 1)
			interval/=60;
		
		unitsSpinner.setSelection(unit);
		
		EditText intervalEdit = (EditText)findViewById(R.id.interval_edit);
		intervalEdit.setText(Long.toString(interval));
		
		boolean logEnabled = settings.getBoolean(LOG_ENABLED_SETTING + mAppWidgetId, false);
		String logFilename = settings.getString(LOG_FILENAME_SETTING + mAppWidgetId, "/sdcard/currentwidget.log");
		
		CheckBox logCheckbox = (CheckBox) findViewById(R.id.log_checkbox);
		logCheckbox.setChecked(logEnabled);
		
		EditText logFilenameEdit = (EditText)findViewById(R.id.log_filename);
		
		logFilenameEdit.setText(logFilename);
		logFilenameEdit.setEnabled(logEnabled);
		
		logCheckbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		CheckBox logAppsCheckbox = (CheckBox)findViewById(R.id.log_apps_checkbox);
		
		boolean logApps = settings.getBoolean(LOG_APPS_SETTING + mAppWidgetId, false);
		
		logAppsCheckbox.setChecked(logApps);
		logAppsCheckbox.setEnabled(logEnabled);
		
		logAppsCheckbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
	}
	
	CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
			switch(buttonView.getId()) {
				case R.id.log_checkbox:
					findViewById(R.id.log_filename).setEnabled(isChecked);
					findViewById(R.id.log_apps_checkbox).setEnabled(isChecked);
					break;
			}
			
		}
	};
	
	View.OnClickListener mOnSaveClickListener = new View.OnClickListener() {	
		
		public void onClick(View v) {

			switch(v.getId())	{
				case R.id.save_button:
					Context context = CurrentWidgetConfigure.this;
					TextView view = (TextView)findViewById(R.id.interval_edit);
					Spinner spinner = (Spinner)findViewById(R.id.units_spinner);
					int selectedUnit = spinner.getSelectedItemPosition();
					
					Long interval = null;
					try {					
						 interval = Long.valueOf(view.getText().toString());
						 if (selectedUnit == 1) // if minutes
							 interval*=60; // convert to seconds
					}
					catch (NumberFormatException nfe) {
						interval = 60l;
					}
					
					SharedPreferences settings = getSharedPreferences("currentWidgetPrefs", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putLong(SECOND_INTERVAL_SETTING + mAppWidgetId, interval);
					editor.putInt(UNITS_SETTING + mAppWidgetId, selectedUnit);
					editor.putBoolean(LOG_ENABLED_SETTING + mAppWidgetId, ((CheckBox)findViewById(R.id.log_checkbox)).isChecked());
					editor.putString(LOG_FILENAME_SETTING + mAppWidgetId, ((EditText)findViewById(R.id.log_filename)).getText().toString());
					editor.putBoolean(LOG_APPS_SETTING + mAppWidgetId, ((CheckBox)findViewById(R.id.log_apps_checkbox)).isChecked());
					
					editor.commit();					
					
					CurrentWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), 
							mAppWidgetId);
					
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
					break;
				
			}
		}
	};
}
