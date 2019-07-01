package com.elvaco.mvp.adapters.ehcache;

import com.elvaco.mvp.core.spi.cache.Cache;

public class CacheAdapter<K, V> implements Cache<K, V> {

  private final org.ehcache.Cache<K, V> delegate;

  public CacheAdapter(org.ehcache.Cache<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean containsKey(K key) {
    return delegate.containsKey(key);
  }

  @Override
  public void put(K key, V value) {
    delegate.put(key, value);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    return delegate.putIfAbsent(key, value);
  }

  @Override
  public V get(K key) {
    return delegate.get(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }
}
