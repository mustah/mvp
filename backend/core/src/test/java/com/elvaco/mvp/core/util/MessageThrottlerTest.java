package com.elvaco.mvp.core.util;

import com.elvaco.mvp.testing.cache.MockCache;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageThrottlerTest {

  private MockCache<String, String> cache;
  private MessageThrottler<String, String> messageThrottler;

  @Before
  public void setUp() {
    cache = new MockCache<>();
    messageThrottler = new MessageThrottler<>(cache, s -> s);
  }

  @Test
  public void firstInstanceIsNotThrottled() {
    assertThat(messageThrottler.throttle("abc")).isFalse();
  }

  @Test
  public void secondInstanceIsThrottled() {
    messageThrottler.throttle("abc");

    assertThat(messageThrottler.throttle("abc")).isTrue();
  }

  @Test
  public void throttleManyTimes() {
    assertThat(messageThrottler.throttle("abc")).isFalse();
    assertThat(messageThrottler.throttle("abc")).isTrue();
    assertThat(messageThrottler.throttle("abc")).isTrue();
    assertThat(messageThrottler.throttle("abc")).isTrue();
  }

  @Test
  public void secondInstanceIsNotThrottledWhenRemovedFromCache() {
    messageThrottler.throttle("abc");
    cache.remove("abc");

    assertThat(messageThrottler.throttle("abc")).isFalse();
  }
}
