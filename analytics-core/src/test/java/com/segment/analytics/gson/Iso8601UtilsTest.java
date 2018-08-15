package com.segment.analytics.gson;

import static com.segment.analytics.TestUtils.newDate;
import static com.segment.analytics.gson.Iso8601Utils.format;
import static com.segment.analytics.gson.Iso8601Utils.parse;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class Iso8601UtilsTest {

  @Test
  public void fromJsonWithTwoDigitMillis() throws Exception {
    assertThat(parse("1985-04-12T23:20:50.52Z"))
        .isEqualTo(newDate(1985, 4, 12, 23, 20, 50, 520, 0));
  }

  @Test
  public void fromJson() throws Exception {
    assertThat(parse("1970-01-01T00:00:00.000Z")).isEqualTo(newDate(1970, 1, 1, 0, 0, 0, 0, 0));
    assertThat(parse("1985-04-12T23:20:50.520Z"))
        .isEqualTo(newDate(1985, 4, 12, 23, 20, 50, 520, 0));
    assertThat(parse("1996-12-19T16:39:57-08:00"))
        .isEqualTo(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60));
    assertThat(parse("1990-12-31T23:59:60Z")).isEqualTo(newDate(1990, 12, 31, 23, 59, 59, 0, 0));
    assertThat(parse("1990-12-31T15:59:60-08:00"))
        .isEqualTo(newDate(1990, 12, 31, 15, 59, 59, 0, -8 * 60));
    assertThat(parse("1937-01-01T12:00:27.870+00:20"))
        .isEqualTo(newDate(1937, 1, 1, 12, 0, 27, 870, 20));
  }

  @Test
  public void toJson() throws Exception {
    assertThat(format(newDate(1970, 1, 1, 0, 0, 0, 0, 0))).isEqualTo("1970-01-01T00:00:00.000Z");
    assertThat(format(newDate(1985, 4, 12, 23, 20, 50, 520, 0)))
        .isEqualTo("1985-04-12T23:20:50.520Z");
    assertThat(format(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60)))
        .isEqualTo("1996-12-20T00:39:57.000Z");
    assertThat(format(newDate(1990, 12, 31, 23, 59, 59, 0, 0)))
        .isEqualTo("1990-12-31T23:59:59.000Z");
    assertThat(format(newDate(1990, 12, 31, 15, 59, 59, 0, -8 * 60)))
        .isEqualTo("1990-12-31T23:59:59.000Z");
    assertThat(format(newDate(1937, 1, 1, 12, 0, 27, 870, 20)))
        .isEqualTo("1937-01-01T11:40:27.870Z");
  }
}
