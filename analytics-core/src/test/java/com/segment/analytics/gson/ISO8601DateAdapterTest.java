package com.segment.analytics.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.segment.analytics.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static com.segment.analytics.TestUtils.newDate;
import static org.assertj.core.api.Assertions.assertThat;

public class ISO8601DateAdapterTest {

    private static class TestModel {
        final Date timestamp;
        public TestModel(Date timestamp) {
            this.timestamp = timestamp;
        }
    }

    @Test
    public void testSerializeDate() {
        TestModel testModel = new TestModel(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60));

        JsonElement e = TestUtils.GSON.toJsonTree(testModel);
        Assert.assertTrue(e.isJsonObject());

        JsonObject o = e.getAsJsonObject();
        Assert.assertTrue(o.has("timestamp"));

        assertThat(o.get("timestamp").getAsString()).isEqualTo("1996-12-20T00:39:57.000Z");
    }

    @Test
    public void testDeserializeDate() {
        String serializedTestModel = "{\"timestamp\":\"1996-06-01T16:39:57.000Z\"}";
        Date expected = newDate(1996, 06, 01, 16, 39, 57, 0, 0);
        TestModel actual = TestUtils.GSON.fromJson(serializedTestModel, TestModel.class);
        assertThat(actual.timestamp).isEqualTo(expected);
    }
}
