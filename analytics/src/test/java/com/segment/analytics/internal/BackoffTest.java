package com.segment.analytics.internal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BackOffTest {
  @Test public void backoff() {
    BackOff backOff = BackOff.create();

    assertThat(backOff.duration()).isEqualTo(100);
    assertThat(backOff.duration()).isEqualTo(200);
    assertThat(backOff.duration()).isEqualTo(400);
    assertThat(backOff.duration()).isEqualTo(800);

    backOff.reset();
    assertThat(backOff.duration()).isEqualTo(100);
    assertThat(backOff.duration()).isEqualTo(200);
  }

  @Test public void overflow() {
    BackOff backOff = new BackOff(100, 2, 1, 10000);

    for (int i = 0; i < 100; i++) {
      assertThat(backOff.duration()).isGreaterThanOrEqualTo(100).isLessThanOrEqualTo(10000);
    }
  }
}
