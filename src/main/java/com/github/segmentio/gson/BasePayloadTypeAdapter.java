package com.github.segmentio.gson;

import java.lang.reflect.Type;

import com.github.segmentio.models.BasePayload;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BasePayloadTypeAdapter
    implements JsonSerializer<BasePayload>
{
  public JsonElement serialize(BasePayload payload, Type type, JsonSerializationContext context) {
    return context.serialize(payload, payload.getClass());
  }
}
