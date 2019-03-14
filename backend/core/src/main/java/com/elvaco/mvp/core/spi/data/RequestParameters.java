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
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.security.AuthenticatedUser;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;

public interface RequestParameters {

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

  default RequestParameters setPeriod(ZonedDateTime start, ZonedDateTime stop) {
    return add(AFTER, start.toString()).add(BEFORE, stop.toString());
  }

  default Optional<SelectionPeriod> getPeriod() {
    Optional<ZonedDateTime> start = getAsZonedDateTime(AFTER);
    Optional<ZonedDateTime> stop = getAsZonedDateTime(BEFORE);
    if (start.isPresent() && stop.isPresent()) {
      return Optional.of(new SelectionPeriod(start.get(), stop.get()));
    }
    return Optional.empty();
  }

  default Optional<RequestParameters> has(RequestParameter param) {
    return hasParam(param) ? Optional.of(this) : Optional.empty();
  }

  default void ensureOrganisation(AuthenticatedUser currentUser) {
    if (!currentUser.isSuperAdmin()) {
      replace(
        ORGANISATION,
        currentUser.subOrganisationParameters().getEffectiveOrganisationId().toString()
      );
    }
  }
}
