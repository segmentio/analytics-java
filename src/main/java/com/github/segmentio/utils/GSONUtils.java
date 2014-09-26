package com.github.segmentio.utils;

import com.github.segmentio.gson.BasePayloadTypeAdapter;
import com.github.segmentio.gson.DateTimeTypeConverter;
import com.github.segmentio.models.BasePayload;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;

public class GSONUtils {

       public static final GsonBuilder BUILDER = new GsonBuilder()
               .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
               .registerTypeAdapter(BasePayload.class, new BasePayloadTypeAdapter());
}
