/**
 * 
 */
package com.manor.currentwidget;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		
		/*Intent widgetUpdate = new Intent(context, CurrentWidget.class);
		widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		//widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {3 });
		
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, widgetUpdate, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
				10*1000, sender);*/		
	
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
	
		SharedPreferences settings = context.getSharedPreferences("currentWidgetPrefs", 0);
		
		 for (int appWidgetId : appWidgetIds) {
			 //Log.i("CurrentWidget", String.format("onUpdate, id: %s", Integer.toString(appWidgetId)));			 
			 
			 long secondsInterval = settings.getLong("secondsInterval" + appWidgetId, 60);			 
				
			 updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, secondsInterval);			 

		 }
	}
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, long secondsInterval) {
		
		
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
		
		try {
			f = new File("/sys/class/power_supply/battery/current_now");	
			
			if (f.exists())
				fs = new FileInputStream(f);
			else {
				fs = new FileInputStream("/sys/class/power_supply/battery/batt_current");
			}				
			

		} catch (FileNotFoundException e) {
			fs = null;
			e.printStackTrace();
			text = "open error";
		}
		
		
		if (fs != null)
		{
			DataInputStream ds = new DataInputStream(fs);
			
			
			try {
				text = ds.readLine();
				ds.close();		
				fs.close();
				success = true;
			} catch (IOException e) {
				text = "read/close error";
				e.printStackTrace();
			}
		}		
		
		if (success)
		{
			Long value = Long.parseLong(text);
			value = value/1000; // convert to milliamper
			if (value < 0)
			{
				value = value*(-1);
				remoteViews.setTextColor(R.id.text, Color.WHITE); // drawing
			}
			else
				remoteViews.setTextColor(R.id.text, Color.rgb(162, 255, 0)); // charging
				
			
			text = value.toString() + "mA";
			
		}		
		
		remoteViews.setTextViewText(R.id.text, text);
		
		// set last updated	
		
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
		
        Intent widgetUpdate = new Intent(context, CurrentWidget.class);
        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        
        // make this pending intent unique
        //widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(CurrentWidget.URI_SCHEME + "://widget/id/"), String.valueOf(mAppWidgetId)));
        
        PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        // schedule the new widget for updating
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*60*1000, newPending);
        alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
                secondsInterval * 1000, newPending);
        
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);


	}

}
