package com.elvaco.mvp.core.spi.cache;

public interface Cache<K, V> {

  boolean containsKey(K key);

  void put(K key, V value);

  V get(K key);
}
