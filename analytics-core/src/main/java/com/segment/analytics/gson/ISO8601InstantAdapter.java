package com.segment.analytics.gson;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;

/**
 * A {@link JsonSerializer} that formats {@link Instant} objects into iso8601 formatted strings, and
 * {@link JsonDeserializer} that parses iso8601 formatted strings into {@link Instant} objects.
 */
public class ISO8601InstantAdapter extends TypeAdapter<Instant>
    implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
  @Override
  public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(Iso8601Utils.format(src)); // ISO 8601 format
  }

  @Override
  public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Instant.parse(json.getAsString());
  }

  @Override
  public void write(JsonWriter out, Instant value) throws IOException {
    out.value(value == null ? null : value.toString());
  }

  @Override
  public Instant read(JsonReader in) throws IOException {
    String str = in.nextString();
    return str == null ? null : Instant.parse(str);
  }
}
