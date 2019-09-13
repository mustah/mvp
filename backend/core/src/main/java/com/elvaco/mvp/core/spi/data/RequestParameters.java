package com.elvaco.mvp.core.spi.data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.security.AuthenticatedUser;

import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORT_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORT_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_BEFORE;

public interface RequestParameters {

  Predicate<RequestParameters> HAS_ORGANISATIONS = p -> p.hasValues(ORGANISATION);

  int DEFAULT_LIMIT = 2_000;

  RequestParameters add(RequestParameter param, String value);

  RequestParameters setAll(Map<RequestParameter, String> values);

  RequestParameters setAll(RequestParameter param, List<String> values);

  RequestParameters setAllIds(RequestParameter param, Collection<UUID> ids);

  RequestParameters replace(RequestParameter param, String value);

  RequestParameters transform(RequestParameter from, RequestParameter into);

  Collection<String> getValues(RequestParameter param);

  Set<Entry<RequestParameter, List<String>>> entrySet();

  @Nullable
  String getFirst(RequestParameter... param);

  boolean hasParam(RequestParameter param);

  boolean isEmpty();

  Optional<RequestParameters> implicitParameters();

  RequestParameters ensureOrganisationFilters(AuthenticatedUser currentUser);

  default Optional<ZonedDateTime> getAsZonedDateTime(RequestParameter param) {
    try {
      return Optional.ofNullable(getFirst(param)).map(ZonedDateTime::parse);
    } catch (DateTimeParseException ex) {
      return Optional.empty();
    }
  }

  default RequestParameters setReportPeriod(ZonedDateTime start, ZonedDateTime stop) {
    return add(REPORT_AFTER, start.toString()).add(REPORT_BEFORE, stop.toString());
  }

  default Optional<FilterPeriod> getCollectionPeriod() {
    return getPeriod(COLLECTION_AFTER, COLLECTION_BEFORE);
  }

  default Optional<FilterPeriod> getReportPeriod() {
    return getPeriod(REPORT_AFTER, REPORT_BEFORE);
  }

  default Optional<FilterPeriod> getThresholdPeriod() {
    return getPeriod(THRESHOLD_AFTER, THRESHOLD_BEFORE);
  }

  default Optional<FilterPeriod> getPeriod(RequestParameter after, RequestParameter before) {
    Optional<ZonedDateTime> start = getAsZonedDateTime(after);
    Optional<ZonedDateTime> stop = getAsZonedDateTime(before);
    if (start.isPresent() && stop.isPresent()) {
      return Optional.of(new FilterPeriod(start.get(), stop.get()));
    }
    return Optional.empty();
  }

  default Optional<RequestParameters> has(RequestParameter param) {
    return hasParam(param) ? Optional.of(this) : Optional.empty();
  }

  default boolean hasValues(RequestParameter param) {
    var values = getValues(param);
    return values != null && !values.isEmpty();
  }

  default int getLimit() {
    try {
      return Optional.ofNullable(getFirst(RequestParameter.LIMIT))
        .map(Integer::parseInt)
        .orElse(DEFAULT_LIMIT);
    } catch (Exception e) {
      return DEFAULT_LIMIT;
    }
  }

  default void ensureOrganisation(
    AuthenticatedUser currentUser,
    Predicate<RequestParameters> predicate
  ) {
    if (!currentUser.isSuperAdmin() || !predicate.test(this)) {
      replace(
        ORGANISATION,
        currentUser.subOrganisationParameters().getEffectiveOrganisationId().toString()
      );
    }
  }
}
