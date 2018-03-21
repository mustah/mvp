package com.elvaco.mvp.adapters.spring;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class RequestParametersAdapter implements RequestParameters {

  private final MultiValueMap<String, String> delegate;

  private RequestParametersAdapter(@Nullable MultiValueMap<String, String> multiValueMap) {
    this.delegate = multiValueMap != null ? multiValueMap : new LinkedMultiValueMap<>();
  }

  public RequestParametersAdapter() {
    this(new LinkedMultiValueMap<>());
  }

  public static RequestParameters of(@Nullable MultiValueMap<String, String> multiValueMap) {
    return new RequestParametersAdapter(multiValueMap);
  }

  public static RequestParameters of(Map<String, List<String>> multiValueMap) {
    return of(multiValueMap != null
                ? new LinkedMultiValueMap<>(multiValueMap)
                : new LinkedMultiValueMap<>());
  }

  @Override
  public RequestParameters add(String name, String value) {
    delegate.add(name, value);
    return this;
  }

  @Override
  public RequestParameters setAll(String name, List<String> values) {
    delegate.put(name, values);
    return this;
  }

  @Override
  public RequestParameters setAll(Map<String, String> values) {
    delegate.setAll(values);
    return this;
  }

  @Override
  public RequestParameters replace(String name, String value) {
    setAll(name, singletonList(value));
    return this;
  }

  @Override
  public List<String> getValues(String name) {
    List<String> values = delegate.get(name);
    return values != null ? values : emptyList();
  }

  @Nullable
  @Override
  public String getFirst(String name) {
    return delegate.getFirst(name);
  }

  @Override
  public boolean hasName(String name) {
    return delegate.containsKey(name);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public Set<Entry<String, List<String>>> entrySet() {
    return delegate.entrySet();
  }

  public MultiValueMap<String, String> multiValueMap() {
    return delegate;
  }

  @Override
  public String toString() {
    return entrySet().toString();
  }
}
