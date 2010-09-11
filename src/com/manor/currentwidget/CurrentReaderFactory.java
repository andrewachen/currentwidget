package com.manor.currentwidget;

import java.io.File;

public class CurrentReaderFactory {
	static public ICurrentReader getCurrentReader() {
		
		File f = new File("/sys/class/power_supply/battery/smem_text");				
		
		if (f.exists())
			return new SMemTextReader();
		
		f = new File("/sys/class/power_supply/battery/batt_current");
		if (f.exists())
			return new OneLineReader(f, false);
		
		f = new File("/sys/class/power_supply/battery/current_now");
		if (f.exists())
			return new OneLineReader(f, true);
		
		return null;
	}
}
