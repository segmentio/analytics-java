package com.github.segmentio.utils;

import org.joda.time.DateTime;
import com.google.gson.GsonBuilder;
import com.github.segmentio.gson.DateTimeTypeConverter;

public class GSONUtils {

	public static final GsonBuilder BUILDER = new GsonBuilder()
		.registerTypeAdapter(DateTime.class,new DateTimeTypeConverter());

}
