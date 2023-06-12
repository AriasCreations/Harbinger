package dev.zontreck.harbinger.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

	/**
	 * Formats a date and time in the ISO8601 format
	 *
	 * @param date The date and time to set
	 * @return ISO-8601 timestamp
	 */
	public static String makeTimestamp(Date date) {

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		return df.format(date);
	}
}
