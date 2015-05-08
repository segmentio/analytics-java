package com.segment.analytics.gson;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/** A {@link TypeAdapterFactory} that allows deserialization of {@link AutoValue} classes. */
public final class AutoValueAdapterFactory implements TypeAdapterFactory {
  @SuppressWarnings("unchecked") @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<? super T> rawType = type.getRawType();
    if (!rawType.isAnnotationPresent(AutoGson.class)) {
      return null;
    }

    String packageName = rawType.getPackage().getName();
    String className = rawType.getName().substring(packageName.length() + 1).replace('$', '_');
    String autoValueName = packageName + ".AutoValue_" + className;

    try {
      Class<?> autoValueType = Class.forName(autoValueName);
      return (TypeAdapter<T>) gson.getAdapter(autoValueType);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load AutoValue type " + autoValueName, e);
    }
  }
}
