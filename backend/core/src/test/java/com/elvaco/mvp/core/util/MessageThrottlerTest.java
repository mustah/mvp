package com.elvaco.mvp.core.util;

import com.elvaco.mvp.testing.cache.MockCache;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageThrottlerTest {

  @Test
  public void firstInstanceIsNotThrottled() {
    MessageThrottler<String, String> messageThrottler = new MessageThrottler<>(
      new MockCache<>(),
      s -> s
    );

    assertThat(messageThrottler.throttle("abc")).isFalse();
  }

  @Test
  public void secondInstanceIsThrottled() {
    MessageThrottler<String, String> messageThrottler = new MessageThrottler<>(
      new MockCache<>(),
      s -> s
    );

    messageThrottler.throttle("abc");

    assertThat(messageThrottler.throttle("abc")).isTrue();
  }

  @Test
  public void secondInstanceIsNotThrottledWhenRemovedFromCache() {
    MockCache<String, String> cache = new MockCache<>();
    MessageThrottler<String, String> messageThrottler = new MessageThrottler<>(
      cache,
      s -> s
    );

    messageThrottler.throttle("abc");
    cache.remove("abc");

    assertThat(messageThrottler.throttle("abc")).isFalse();
  }
}
