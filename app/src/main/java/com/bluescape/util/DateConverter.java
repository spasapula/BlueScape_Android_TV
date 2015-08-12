package com.bluescape.util;

import java.util.Calendar;

public class DateConverter {
	public static String timeStampToDate(long timeStamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp * 1000);
		return (calendar.get(Calendar.MONTH) + 1 + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR));
	}
}
