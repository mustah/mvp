package com.elvaco.mvp.testing.fixture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MockRequestParameters implements RequestParameters {

  private final Map<RequestParameter, List<String>> map;

  public MockRequestParameters() {
    this(new HashMap<>());
  }

  private MockRequestParameters(Map<RequestParameter, List<String>> map) {
    this.map = map;
  }

  @Override
  public RequestParameters add(RequestParameter param, String value) {
    map.computeIfPresent(param, (s, strings) -> {
      strings.add(value);
      return strings;
    });
    map.computeIfAbsent(param, v -> {
      List<String> values = new ArrayList<>();
      values.add(value);
      return values;
    });
    return this;
  }

  @Override
  public RequestParameters setAll(Map<RequestParameter, String> values) {
    return this;
  }

  @Override
  public RequestParameters setAll(RequestParameter param, List<String> values) {
    map.put(param, values);
    return this;
  }

  @Override
  public RequestParameters setAllIds(RequestParameter param, Collection<UUID> ids) {
    List<String> values = ids.stream().map(UUID::toString).collect(toList());
    return setAll(param, values);
  }

  @Override
  public RequestParameters replace(RequestParameter param, String value) {
    map.put(param, List.of(value));
    return this;
  }

  @Override
  public RequestParameters transform(RequestParameter from, RequestParameter into) {
    if (!hasParam(from)) {
      return this;
    }

    map.put(into, getValues(from));
    map.remove(from);
    return this;
  }

  @Override
  public List<String> getValues(RequestParameter param) {
    return map.getOrDefault(param, List.of());
  }

  @Override
  public Set<Entry<RequestParameter, List<String>>> entrySet() {
    return map.entrySet();
  }

  @Nullable
  @Override
  public String getFirst(RequestParameter... param) {
    return Stream.of(param)
      .map(map::get)
      .filter(Objects::nonNull)
      .map(strings -> strings.isEmpty() ? null : strings.getFirst())
      .findFirst()
      .orElse(null);
  }

  @Override
  public boolean hasParam(RequestParameter param) {
    return map.containsKey(param);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Optional<RequestParameters> implicitParameters() {
    return Optional.empty();
  }

  @Override
  public RequestParameters ensureOrganisationFilters(AuthenticatedUser currentUser) {
    ensureOrganisation(currentUser, HAS_ORGANISATIONS);
    return this;
  }
}
