package com.elvaco.mvp.testing.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;

import static java.util.Collections.emptyList;

public class MockRequestParameters implements RequestParameters {

  private final Map<String, List<String>> map;

  public MockRequestParameters() {
    this.map = new HashMap<>();
  }

  @Override
  public RequestParameters add(String name, String value) {
    map.computeIfAbsent(name, v -> {
      List<String> values = new ArrayList<>();
      values.add(v);
      return values;
    });
    map.computeIfPresent(name, (s, strings) -> {
      strings.add(s);
      return strings;
    });
    return this;
  }

  @Override
  public RequestParameters setAll(Map<String, String> values) {
    return this;
  }

  @Override
  public RequestParameters setAll(String name, List<String> values) {
    return this;
  }

  @Override
  public RequestParameters replace(String name, String value) {
    List<String> values = new ArrayList<>();
    values.add(value);
    map.put(name, values);
    return this;
  }

  @Override
  public List<String> getValues(String name) {
    return map.getOrDefault(name, emptyList());
  }

  @Override
  public Set<Entry<String, List<String>>> entrySet() {
    return map.entrySet();
  }

  @Nullable
  @Override
  public String getFirst(String name) {
    List<String> values = map.get(name);
    return values.isEmpty() ? null : values.get(0);
  }

  @Override
  public boolean hasName(String name) {
    return map.containsKey(name);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }
}