package com.github.sanctum.panther.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public interface ParsedTimeFormat {

	long getDays();

	long getHours();

	long getMinutes();

	long getSeconds();

	default long toSeconds() {
		return TimeUnit.DAYS.toSeconds(getDays()) + TimeUnit.HOURS.toSeconds(getHours()) + TimeUnit.MINUTES.toSeconds(getMinutes()) + getSeconds();
	}

	@NotNull default Date getDate() {
		long seconds = toSeconds();
		long time = System.currentTimeMillis() + (seconds * 1000);
		return new Date(time);
	}

	static ParsedTimeFormat of(long sec) {
		long hours = sec / 3600;
		long minutes = (sec % 3600) / 60;
		long seconds = sec % 60;
		return of(0, hours, minutes, seconds);
	}

	static ParsedTimeFormat of(@NotNull String format) {
		Pattern pattern = Pattern.compile("(\\d+)(d|hr|m|s)");
		Matcher matcher = pattern.matcher(format);
		long days = 0;
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		while (matcher.find()) {
			switch (matcher.group(2)) {
				case "d":
					days = Long.parseLong(matcher.group(1));
					break;
				case "hr":
					hours = Long.parseLong(matcher.group(1));
					break;
				case "m":
					minutes = Long.parseLong(matcher.group(1));
					break;
				case "s":
					seconds = Long.parseLong(matcher.group(1));
					break;
			}
		}
		if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) return null;
		long finalDays = days;
		long finalHours = hours;
		long finalMinutes = minutes;
		long finalSeconds = seconds;
		return new ParsedTimeFormat() {
			@Override
			public long getDays() {
				return finalDays;
			}

			@Override
			public long getHours() {
				return finalHours;
			}

			@Override
			public long getMinutes() {
				return finalMinutes;
			}

			@Override
			public long getSeconds() {
				return finalSeconds;
			}

			@Override
			public String toString() {
				ZonedDateTime time = new Date().toInstant().atZone(ZoneId.systemDefault());
				String month = time.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
				String year = String.valueOf(time.getYear());
				String day = String.valueOf(time.getDayOfMonth() + getDays());
				Date date = getDate();
				ZonedDateTime current = date.toInstant().atZone(ZoneId.systemDefault());
				String clock = current.getHour() + ":" + current.getMinute();
				// format 'Month day, year @ clock'
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				return month + " " + day + ", " + year + " @ " + clock + (calendar.get(Calendar.AM_PM) == Calendar.PM ? "pm" : "am");
			}
		};
	}

	static @NotNull ParsedTimeFormat of(long days, long hours, long minutes, long seconds) {
		return new ParsedTimeFormat() {
			@Override
			public long getDays() {
				return days;
			}

			@Override
			public long getHours() {
				return hours;
			}

			@Override
			public long getMinutes() {
				return minutes;
			}

			@Override
			public long getSeconds() {
				return seconds;
			}

			@Override
			public String toString() {
				ZonedDateTime time = new Date().toInstant().atZone(ZoneId.systemDefault());
				String month = time.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
				String year = String.valueOf(time.getYear());
				String day = String.valueOf(time.getDayOfMonth() + getDays());
				Date date = getDate();
				ZonedDateTime current = date.toInstant().atZone(ZoneId.systemDefault());
				String clock = current.getHour() + ":" + current.getMinute();
				// format 'Month day, year @ clock'
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				return month + " " + day + ", " + year + " @ " + clock + (calendar.get(Calendar.AM_PM) == Calendar.PM ? "pm" : "am");
			}
		};
	}

}
