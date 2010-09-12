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
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.BatteryManager;
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
		
		/*Bitmap b1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.five);
		Bitmap b2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.three);
		Bitmap b3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.nine);
		
		Bitmap newBitmap = Bitmap.createBitmap(b1.getWidth() + b2.getWidth() + b3.getWidth(), 
				b1.getHeight() + b2.getHeight() + b3.getHeight(),
				b1.getConfig());
		
		int[] pixels = new int[newBitmap.getWidth()*newBitmap.getHeight()];
		b1.getPixels(pixels, 0, 0, 0, 0, b1.getWidth(), b1.getHeight());
		b2.getPixels(pixels, b1.getHeight()*b1.getWidth(), 0, 0, 0, b2.getWidth(), b2.getHeight());
		b3.getPixels(pixels, (b1.getHeight()*b1.getWidth())+(b2.getHeight()*b2.getWidth()), 0, 0, 0, b3.getWidth(),
				b3.getHeight());
		
		newBitmap.setPixels(pixels, 0, 0, 0, 0, newBitmap.getWidth(), newBitmap.getHeight());
		
		b1.recycle();
		b2.recycle();
		b3.recycle();
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = "CurrentWidget";
		
		
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		
		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		Intent notificationIntent = new Intent(context, CurrentWidget.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);		

		notificationManager.notify(1,notification);*/

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
      
		
		String text = null;
		boolean isCharging = true;
		
		// @@@ add /sys/class/power_supply/battery/batt_chg_current?
		ICurrentReader currentReader =  CurrentReaderFactory.getCurrentReader();
		if (currentReader == null)
			text = "error1";	
		else
		{	
			Long value = currentReader.getValue();
			if (value == null)
				text = "error2";
			else
			{
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
		}	
		
		
		remoteViews.setTextViewText(R.id.text, text);
		
		// set last update
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
		
		// write to log file
		if (settings.getBoolean(CurrentWidgetConfigure.LOG_ENABLED_SETTING + appWidgetId, false)) {
			
			try {
				FileOutputStream logFile = new FileOutputStream(settings.getString(CurrentWidgetConfigure.LOG_FILENAME_SETTING + appWidgetId, "/sdcard/currentwidget.log"), true);
				DataOutputStream logOutput = new DataOutputStream(logFile);
				
				String str = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) + ",";
				if (!isCharging)
					str += "-";
				
				str += text;
				
				// get battery level
				try {
					Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
					if (batteryIntent != null) {
						str += "," + String.valueOf(batteryIntent.getIntExtra("level", 0)) + "%";
					}
				}
				catch (Exception ex) {
					// can't register service from on click
					str += ",000";
				}
				 
				 str += "\r\n";
				
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

