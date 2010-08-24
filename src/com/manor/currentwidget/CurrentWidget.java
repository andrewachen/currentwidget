/**
 * 
 */
package com.manor.currentwidget;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author Ran
 *
 */
public class CurrentWidget extends AppWidgetProvider {
	
	@Override
	public void onEnabled(Context context) {
		
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		
		for (int appWidgetId : appWidgetIds) {
			
			//Log.i("CurrentWidget", String.format("onDeleted, id: %s", Integer.toString(appWidgetId)));

			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent widgetUpdate = new Intent(context, CurrentWidget.class);
			widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
			
			PendingIntent sender = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);			
			
			alarmManager.cancel(sender);

		}

	}	

	
	// fix for 1.5 SDK bug
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		/*if (action == null) {
	    	Bundle extras = intent.getExtras();
	    	if (extras != null) {
	    		final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
	    				AppWidgetManager.INVALID_APPWIDGET_ID); 
	    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
	    			
	    		}
		}
		else*/ if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
	    	Bundle extras = intent.getExtras();
	    	if (extras != null) {
	    		final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
	    				AppWidgetManager.INVALID_APPWIDGET_ID); 
	    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { 
	    			this.onDeleted(context, new int[] { appWidgetId });
	    		}
	        } 
	    } else { 
	        super.onReceive(context, intent); 
	    } 	
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		
		 for (int appWidgetId : appWidgetIds) {
			 //Log.i("CurrentWidget", String.format("onUpdate, id: %s", Integer.toString(appWidgetId))); 
			 	
			 updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);			 

		 }
	}
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		
		SharedPreferences settings = context.getSharedPreferences("currentWidgetPrefs", 0);
		
		long secondsInterval = settings.getLong(CurrentWidgetConfigure.SECOND_INTERVAL_SETTING + appWidgetId, 60);
		 
		
		// set on click for whole layout to launch configuration
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		Intent configIntent = new Intent(context, CurrentWidgetConfigure.class);		
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.linear_layout, configPendingIntent);        
      
		
		FileInputStream fs = null;
		String text = null;
		boolean success = false;		
		File f = null;
		
		boolean convertToMillis = false;
		boolean readLine = true;
		
		try {
			
			f = new File("/sys/class/power_supply/battery/smem_text");				
			
			if (f.exists())
			{
				try 
				{
					readLine = false;
					// @@@ debug StringReader fr = new StringReader("batt_id: 1\r\nbatt_vol: 3840\r\nbatt_vol_last: 0\r\nbatt_temp: 1072\r\nbatt_current: 1\r\nbatt_current_last: 0\r\nbatt_discharge_current: 112\r\nVREF_2: 0\r\nVREF: 1243\r\nADC4096_VREF: 4073\r\nRtemp: 70\r\nTemp: 324\r\nTemp_last: 0\r\npd_M: 20\r\nMBAT_pd: 3860\r\nI_MBAT: -114\r\npd_temp: 0\r\npercent_last: 57\r\npercent_update: 58\r\ndis_percent: 64\r\nvbus: 0\r\nusbid: 1\r\ncharging_source: 0\r\nMBAT_IN: 1\r\nfull_bat: 1300000\r\neval_current: 115\r\neval_current_last: 0\r\ncharging_enabled: 0\r\ntimeout: 30\r\nfullcharge: 0\r\nlevel: 58\r\ndelta: 1\r\nchg_time: 0\r\nlevel_change: 0\r\nsleep_timer_count: 11\r\nOT_led_on: 0\r\noverloading_charge: 0\r\na2m_cable_type: 0\r\nover_vchg: 0\r\n");
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);	
					
					String line = br.readLine();
					while (line != null) 
					{
						if (line.contains("I_MBAT"))
						{
							text = line.substring(line.indexOf("I_MBAT: ") + 8);
							success = true;
							break;
						}
						line = br.readLine();
					}
					
					if (!success)
						text = "r_error";
					
					
					br.close();
					fr.close();
				}
				catch (IOException ioe)
				{
					
				}
				
				
			}
			else
			{
				f = new File("/sys/class/power_supply/battery/batt_current");	
				if (f.exists())
					fs = new FileInputStream(f);
				else {
					fs = new FileInputStream("/sys/class/power_supply/battery/current_now");
					convertToMillis = true;
				}
			}
			

		} catch (FileNotFoundException e) {
			fs = null;
			e.printStackTrace();
			text = "o error";
		}
		
		
		if (fs != null && readLine)
		{
			DataInputStream ds = new DataInputStream(fs);
			
			
			try {
				text = ds.readLine();
				ds.close();		
				fs.close();
				success = true;
			} catch (IOException e) {
				text = "r error";
				e.printStackTrace();
			}
		}		
		
		Long value = null;
		boolean isCharging = true;
		
		if (success)
		{			
			try
			{
				value = Long.parseLong(text);
			}
			catch (NumberFormatException nfe)
			{
				value = 0l;
			}
			
			if (convertToMillis)
				value = value/1000; // convert to milliampere
			
			if (value < 0)
			{
				value = value*(-1);
				remoteViews.setTextColor(R.id.text, Color.rgb(117, 120, 118)); // drawing
				isCharging = false;
			}
			else
				remoteViews.setTextColor(R.id.text, Color.rgb(100, 168, 0)); // charging
				
			
			text = value.toString() + "mA";
			
		}	
		
		
		remoteViews.setTextViewText(R.id.text, text);
		
		// set last update
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
		
		// write to log file
		if (settings.getBoolean(CurrentWidgetConfigure.LOG_ENABLED_SETTING + appWidgetId, false)) {
			
			try {
				FileOutputStream logFile = new FileOutputStream(settings.getString(CurrentWidgetConfigure.LOG_FILENAME_SETTING + appWidgetId, "/sdcard/currentwidget.log"), true);
				DataOutputStream logOutput = new DataOutputStream(logFile);
				
				String str = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) + " ";
				if (!isCharging)
					str += "-";
				
				str += text + "\r\n";
				
				logOutput.writeBytes(str);
				
				logOutput.close();
				logFile.close();
			}
			catch (Exception ex) {
				Log.e("CurrentWidget", ex.getMessage());
			}
			
		}

        Intent widgetUpdate = new Intent(context, CurrentWidget.class);
        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        
        // make this pending intent unique
        //widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(CurrentWidget.URI_SCHEME + "://widget/id/"), String.valueOf(mAppWidgetId)));
        
        PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        // set on click for button
        remoteViews.setOnClickPendingIntent(R.id.update_now_button, newPending);
        
        // schedule the new widget for updating
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*60*1000, newPending);
        alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
                secondsInterval * 1000, newPending);        
       
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);


	}

}
