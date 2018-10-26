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
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.util.CollectionUtils;

import static com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.util.CollectionUtils.isNotEmpty;

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
  String getFirst(RequestParameter param);

  boolean hasParam(RequestParameter param);

  boolean isEmpty();

  RequestParameters shallowCopy();

  default RequestParameters ensureOrganisationFilters(AuthenticatedUser currentUser) {
    ensureOrganisation(currentUser);

    var selectionParameters = Optional.ofNullable(currentUser.selectionParameters());

    selectionParameters.map(SelectionParametersDto::getFacilityIds)
      .filter(CollectionUtils::isNotEmpty)
      .map(retainAllFor(FACILITY))
      .ifPresent(facilities -> setAll(FACILITY, facilities));

    selectionParameters.map(SelectionParametersDto::getCityIds)
      .filter(CollectionUtils::isNotEmpty)
      .map(retainAllFor(CITY))
      .ifPresent(cities -> setAll(CITY, cities));

    return this;
  }

  default Optional<ZonedDateTime> getAsZonedDateTime(RequestParameter param) {
    try {
      return Optional.ofNullable(getFirst(param)).map(ZonedDateTime::parse);
    } catch (DateTimeParseException ex) {
      return Optional.empty();
    }
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

  private void ensureOrganisation(AuthenticatedUser currentUser) {
    if (!currentUser.isSuperAdmin()) {
      var organisationId = currentUser.getParentOrganisationId() != null
        ? currentUser.getParentOrganisationId()
        : currentUser.getOrganisationId();
      replace(ORGANISATION, organisationId.toString());
    }
  }

  private Function<List<String>, List<String>> retainAllFor(RequestParameter parameter) {
    return selectionParameters -> {
      var fromParameters = getValues(parameter);
      if (isNotEmpty(fromParameters)) {
        selectionParameters.retainAll(fromParameters);
      }
      return selectionParameters;
    };
  }
}
