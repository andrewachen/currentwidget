package com.manor.currentwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class CurrentWidgetConfigure extends Activity {

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
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
		
		EditText intervalEdit = (EditText)findViewById(R.id.interval_edit);		
		SharedPreferences settings = getSharedPreferences("currentWidgetPrefs", 0);
		long interval = settings.getLong("secondsInterval" + mAppWidgetId, 60);
		int unit = settings.getInt("units" + mAppWidgetId, 1);
		if (unit == 1)
			interval/=60;
		
		unitsSpinner.setSelection(unit);
		intervalEdit.setText(Long.toString(interval));
	}
	
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
					editor.putLong("secondsInterval" + mAppWidgetId, interval);
					editor.putInt("units" + mAppWidgetId, selectedUnit);
					editor.commit();					
					
					CurrentWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), 
							mAppWidgetId, interval);
					
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
					break;
				
			}
		}
	};
}
