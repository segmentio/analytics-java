package com.segment.analytics.internal.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.segment.analytics.Payload;
import java.io.IOException;

// TODO: handle nulls?
public class PayloadTypeTypeAdapter extends TypeAdapter<Payload.Type> {
  @Override public void write(JsonWriter out, Payload.Type value) throws IOException {
    out.value(value.toString().toLowerCase());
  }

  @Override public Payload.Type read(JsonReader in) throws IOException {
    String type = in.nextString();
    return Payload.Type.valueOf(type.toUpperCase());
  }
}
