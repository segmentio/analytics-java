package com.segment.analytics.internal.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.segment.analytics.internal.gson.LowerCaseEnumTypeAdapterFactory.LowerCaseTypeAdapter;
import com.segment.analytics.messages.Message;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(BurstJUnit4.class) public class LowerCaseEnumTypeAdapterFactoryTest {

  @Test public void throwsIOExceptionForNullValue() throws IOException {
    TypeAdapter<Message.Type> typeAdapter = new LowerCaseTypeAdapter<>(Message.Type.class);

    try {
      typeAdapter.write(mock(JsonWriter.class), null);
    } catch (IOException e) {
      assertThat(e).hasMessage("Null value");
    }

    try {
      typeAdapter.read(mock(JsonReader.class));
    } catch (IOException e) {
      assertThat(e).hasMessage("Null value");
    }
  }

  @Test public void factoryReturnsCorrectTypeAdapter() {
    TypeAdapterFactory factory = new LowerCaseEnumTypeAdapterFactory<>(Message.Type.class);

    TypeAdapter adapter = factory.create(null, new TypeToken<Message.Type>() {
    });
    assertThat(adapter).isInstanceOf(LowerCaseTypeAdapter.class);
  }
}
