package com.segment.analytics.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * A {@link JsonSerializer} that formats {@link Date} objects into iso8601 formatted strings, and
 * {@link JsonDeserializer} that parses iso8601 formatted strings into {@link Date} objects.
 */
public class ISO8601DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(Iso8601Utils.format(src));
  }

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Iso8601Utils.parse(json.toString());
  }
}
