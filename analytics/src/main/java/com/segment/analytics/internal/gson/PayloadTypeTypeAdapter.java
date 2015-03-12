package com.segment.analytics.internal.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.segment.analytics.messages.Message;
import java.io.IOException;

/**
 * Our servers only accept 'type' as a lowercase string.
 * Alternatively, we could simply have the enums be named in lower case as well, which is likely to
 * be more compatible with non-Gson converters.
 */
// TODO: handle nulls?
public class PayloadTypeTypeAdapter extends TypeAdapter<Message.Type> {
  @Override public void write(JsonWriter out, Message.Type value) throws IOException {
    out.value(value.toString().toLowerCase());
  }

  @Override public Message.Type read(JsonReader in) throws IOException {
    String type = in.nextString();
    return Message.Type.valueOf(type.toUpperCase());
  }
}
