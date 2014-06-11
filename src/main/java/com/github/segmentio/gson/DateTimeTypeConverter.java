package com.github.segmentio.gson;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeTypeConverter implements JsonSerializer<DateTime> {
	 
	private DateTimeFormatter formatter;

	public DateTimeTypeConverter () {
		formatter = ISODateTimeFormat.dateTime().withOffsetParsed();
	}

	public JsonElement serialize(DateTime src, Type typeOfSrc,
			JsonSerializationContext context) {

		String formatted = formatter.print(src);
		return new JsonPrimitive(formatted);
	}
}
