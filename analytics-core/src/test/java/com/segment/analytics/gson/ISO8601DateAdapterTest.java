package com.segment.analytics.gson;

import static com.segment.analytics.TestUtils.newDate;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class ISO8601DateAdapterTest {

  public static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(Date.class, new ISO8601DateAdapter()).create();

  private static class TestModel {
    final Date timestamp;

    public TestModel(Date timestamp) {
      this.timestamp = timestamp;
    }
  }

  @Test
  public void testSerializeDate() {
    TestModel testModel = new TestModel(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60));

    JsonElement e = GSON.toJsonTree(testModel);
    Assert.assertTrue(e.isJsonObject());

    JsonObject o = e.getAsJsonObject();
    Assert.assertTrue(o.has("timestamp"));

    assertThat(o.get("timestamp").getAsString()).isEqualTo("1996-12-20T00:39:57.000Z");
  }

  @Test
  public void testDeserializeDate() {
    String serializedTestModel = "{\"timestamp\":\"1996-06-01T16:39:57.000Z\"}";
    Date expected = newDate(1996, 06, 01, 16, 39, 57, 0, 0);
    TestModel actual = GSON.fromJson(serializedTestModel, TestModel.class);
    assertThat(actual.timestamp).isEqualTo(expected);
  }
}
