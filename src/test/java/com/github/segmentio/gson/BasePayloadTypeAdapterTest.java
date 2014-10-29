package com.github.segmentio.gson;

import java.util.Arrays;

import com.github.segmentio.models.Alias;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.models.Group;
import com.github.segmentio.models.Identify;
import com.github.segmentio.models.Options;
import com.github.segmentio.models.Page;
import com.github.segmentio.models.Props;
import com.github.segmentio.models.Screen;
import com.github.segmentio.models.Track;
import com.github.segmentio.models.Traits;
import com.github.segmentio.utils.GSONUtils;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BasePayloadTypeAdapterTest
{

  private Gson gson;
  private String testCategory = "test-category";
  private String testName = "test-name";
  private String testWriteKey = "test-write-key";
  private String testUserId = "test-user-id";
  private Traits testTraits = new Traits().put("test-trait-key", "test-trait-value");
  private Options testOptions = new Options().setIntegration("all", false);
  private Props testProperties = new Props().put("test-prop-key", "test-prop-value");

  @Before
  public void setup() {
    gson = GSONUtils.BUILDER.create();
  }

  @Test
  public void testBasePayloadSerialization() {
    BasePayload payload = new Identify(testUserId, testTraits, testOptions);
    Batch batch = new Batch(testWriteKey, Arrays.asList(payload));
    String json = gson.toJson(batch);
    assertJson(json, "identify");
    assertJsonMap(json, "test-trait-key", "test-trait-value");
    assertJsonOptions(json, "all", "false");

    payload = new Alias(testUserId, testUserId, testOptions);
    batch = new Batch(testWriteKey, Arrays.asList(payload));
    json = gson.toJson(batch);
    assertJson(json, "alias");
    assertJsonOptions(json, "all", "false");

    payload = new Group(testUserId, testUserId, testTraits, testOptions);
    batch = new Batch(testWriteKey, Arrays.asList(payload));
    json = gson.toJson(batch);
    assertJson(json, "group");
    assertJsonMap(json, "test-trait-key", "test-trait-value");
    assertJsonOptions(json, "all", "false");

    payload = new Page(testUserId, testName, testCategory, testProperties, testOptions);
    batch = new Batch(testWriteKey, Arrays.asList(payload));
    json = gson.toJson(batch);
    assertJson(json, "page");
    assertTrue(json.contains(testName));
    assertTrue(json.contains(testCategory));
    assertJsonMap(json, "test-prop-key", "test-prop-value");
    assertJsonOptions(json, "all", "false");

    payload = new Screen(testUserId, testName, testCategory, testProperties, testOptions);
    batch = new Batch(testWriteKey, Arrays.asList(payload));
    json = gson.toJson(batch);
    assertJson(json, "screen");
    assertTrue(json.contains(testName));
    assertTrue(json.contains(testCategory));
    assertJsonMap(json, "test-prop-key", "test-prop-value");
    assertJsonOptions(json, "all", "false");

    payload = new Track(testUserId, testName, testProperties, testOptions);
    batch = new Batch(testWriteKey, Arrays.asList(payload));
    json = gson.toJson(batch);
    assertJson(json, "track");
    assertTrue(json.contains(testName));
    assertJsonMap(json, "test-prop-key", "test-prop-value");
    assertJsonOptions(json, "all", "false");
  }

  private void assertJson(String json, String type) {
    assertTrue(json.contains(type));
    assertTrue(json.contains(testUserId));
  }

  private void assertJsonMap(String json, String key, String value) {
    assertTrue(json.contains(key));
    assertTrue(json.contains(value));
  }

  private void assertJsonOptions(String json, String optionKey, String optionValue) {
    assertTrue(json.contains(optionKey));
    assertTrue(json.contains(optionValue));
  }
}
