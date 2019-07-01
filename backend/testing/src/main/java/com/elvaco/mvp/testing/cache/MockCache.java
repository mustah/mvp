package com.elvaco.mvp.testing.cache;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.core.spi.cache.Cache;

public class MockCache<K, V> implements Cache<K, V> {

  private final Map<K, V> map = new HashMap<>();

  @Override
  public boolean containsKey(K key) {
    return map.containsKey(key);
  }

  @Override
  public void put(K key, V value) {
    map.put(key, value);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    return map.putIfAbsent(key, value);
  }

  @Override
  public V get(K key) {
    return map.getOrDefault(key, null);
  }

  @Override
  public void clear() {
    map.clear();
  }

  public void remove(K key) {
    map.remove(key);
  }
}
