package com.segment.analytics.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A type adapter factory for enums that are named in uppercase (as per Java conventions) but are
 * serialized in lowercase (as opposed to the default of serializing by the enum name).
 */
public class LowerCaseEnumTypeAdapterFactory<T> implements TypeAdapterFactory {
  private final Class<T> clazz;

  public LowerCaseEnumTypeAdapterFactory(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<? super T> rawType = type.getRawType();
    if (rawType != clazz) {
      return null;
    }
    return (TypeAdapter<T>) new LowerCaseTypeAdapter<>(clazz);
  }

  static class LowerCaseTypeAdapter<T> extends TypeAdapter<T> {
    private final Class<T> clazz;

    public LowerCaseTypeAdapter(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override public void write(JsonWriter writer, T value) throws IOException {
      if (value == null) {
        throw new IOException("Null value");
      }
      writer.value(value.toString().toLowerCase());
    }

    @Override public T read(JsonReader reader) throws IOException {
      String value = reader.nextString();
      if (value == null) {
        throw new IOException("Null value");
      }
      try {
        Method valuesMethod = clazz.getMethod("valueOf", String.class);
        return (T) valuesMethod.invoke(null, value);
      } catch (NoSuchMethodException e) {
        throw new IOException(e);
      } catch (IllegalAccessException e) {
        throw new IOException(e);
      } catch (InvocationTargetException e) {
        throw new IOException(e);
      }
    }
  }
}

