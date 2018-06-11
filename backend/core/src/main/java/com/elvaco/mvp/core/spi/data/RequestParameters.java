package com.elvaco.mvp.core.spi.data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;

public interface RequestParameters {

  RequestParameters add(String name, String value);

  RequestParameters setAll(Map<String, String> values);

  RequestParameters setAll(String name, List<String> values);

  RequestParameters setAllIds(String name, List<UUID> ids);

  RequestParameters replace(String name, String value);

  List<String> getValues(String name);

  Set<Entry<String, List<String>>> entrySet();

  @Nullable
  String getFirst(String name);

  boolean hasName(String name);

  boolean isEmpty();

  RequestParameters shallowCopy();

  default Optional<ZonedDateTime> getAsZonedDateTime(String name) {
    try {
      return Optional.ofNullable(getFirst(name)).map(ZonedDateTime::parse);
    } catch (DateTimeParseException ex) {
      return Optional.empty();
    }
  }

  default Optional<SelectionPeriod> getAsSelectionPeriod(String startParam, String endParam) {
    Optional<ZonedDateTime> start = getAsZonedDateTime(startParam);
    Optional<ZonedDateTime> end = getAsZonedDateTime(endParam);
    if (start.isPresent() && end.isPresent()) {
      return Optional.of(new SelectionPeriod(start.get(), end.get()));
    }
    return Optional.empty();
  }
}
