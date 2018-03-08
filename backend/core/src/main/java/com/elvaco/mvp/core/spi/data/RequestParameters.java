package com.elvaco.mvp.core.spi.data;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

public interface RequestParameters {

  RequestParameters add(String name, String value);

  RequestParameters setAll(Map<String, String> values);

  RequestParameters setAll(String name, List<String> values);

  RequestParameters replace(String name, String value);

  List<String> getValues(String name);

  Set<Entry<String, List<String>>> entrySet();

  @Nullable
  String getFirst(String name);

  boolean hasName(String name);

  boolean isEmpty();
}
