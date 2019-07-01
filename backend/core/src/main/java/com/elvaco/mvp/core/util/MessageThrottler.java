package com.elvaco.mvp.core.util;

import java.util.function.Function;

import com.elvaco.mvp.core.spi.cache.Cache;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageThrottler<K, V> {

  private final Cache<K, V> cache;
  private final Function<V, K> keyFactory;

  public boolean throttle(V value) {
    K key = keyFactory.apply(value);
    return cache.putIfAbsent(key, value) != null;
  }
}
