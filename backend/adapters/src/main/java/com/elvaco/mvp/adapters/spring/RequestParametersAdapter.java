package com.elvaco.mvp.adapters.spring;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.util.CollectionHelper.isNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class RequestParametersAdapter implements RequestParameters {

  private final MultiValueMap<RequestParameter, String> delegate;

  @Nullable
  private final RequestParameters subOrganisationParameters;

  private RequestParametersAdapter(
    @Nullable MultiValueMap<RequestParameter, String> multiValueMap,
    @Nullable RequestParameters subOrganisationParameters
  ) {
    this.delegate = multiValueMap != null ? multiValueMap : new LinkedMultiValueMap<>();
    this.subOrganisationParameters = subOrganisationParameters;
  }

  public RequestParametersAdapter() {
    this(new LinkedMultiValueMap<>(), null);
  }

  public static RequestParameters of() {
    return of(null, null);
  }

  public static RequestParameters of(@Nullable Map<String, List<String>> multiValueMap) {
    return of(multiValueMap, null);
  }

  public static RequestParameters of(
    @Nullable Map<String, List<String>> multiValueMap,
    @Nullable RequestParameter idParameter
  ) {
    if (multiValueMap == null) {
      return new RequestParametersAdapter();
    }

    if (multiValueMap.containsKey("id")) {
      multiValueMap.put(
        Optional.ofNullable(idParameter).map(RequestParameter::toString)
          .orElseThrow(() -> new IllegalArgumentException(
              "Ambiguous parameter 'id' can not be mapped"
            )
          ),
        multiValueMap.remove("id")
      );
    }

    MultiValueMap<RequestParameter, String> typedParams = new LinkedMultiValueMap<>();
    for (Map.Entry<String, List<String>> entry : multiValueMap.entrySet()) {
      Optional.ofNullable(RequestParameter.from(entry.getKey()))
        .ifPresent(parameter -> typedParams.put(parameter, entry.getValue()));
    }
    return new RequestParametersAdapter(typedParams, null);
  }

  @Override
  public RequestParameters add(RequestParameter param, String value) {
    delegate.add(param, value);
    return this;
  }

  @Override
  public RequestParameters setAll(Map<RequestParameter, String> values) {
    delegate.setAll(values);
    return this;
  }

  @Override
  public RequestParameters setAll(RequestParameter param, List<String> values) {
    delegate.put(param, values);
    return this;
  }

  @Override
  public RequestParameters setAllIds(RequestParameter param, Collection<UUID> ids) {
    List<String> values = ids.stream().map(UUID::toString).collect(toList());
    delegate.put(param, values);
    return this;
  }

  @Override
  public RequestParameters replace(RequestParameter param, String value) {
    setAll(param, singletonList(value));
    return this;
  }

  @Override
  public RequestParameters transform(RequestParameter from, RequestParameter into) {
    if (hasParam(from)) {
      delegate.put(into, getValues(from));
      delegate.remove(from);
    }
    return this;
  }

  @Override
  public List<String> getValues(RequestParameter param) {
    List<String> values = delegate.get(param);
    return values != null ? values : emptyList();
  }

  @Override
  public Set<Entry<RequestParameter, List<String>>> entrySet() {
    return delegate.entrySet();
  }

  @Nullable
  @Override
  public String getFirst(RequestParameter... param) {
    return Arrays.stream(param)
      .map(delegate::getFirst)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);
  }

  @Override
  public boolean hasParam(RequestParameter param) {
    return delegate.containsKey(param);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public Optional<RequestParameters> implicitParameters() {
    return Optional.ofNullable(subOrganisationParameters);
  }

  @Override
  public RequestParameters ensureOrganisationFilters(AuthenticatedUser currentUser) {
    ensureOrganisation(currentUser);

    return currentUser.subOrganisationParameters()
      .selectionParameters()
      .map(this::applySubOrganisationParameters)
      .orElse(this);
  }

  public MultiValueMap<RequestParameter, String> multiValueMap() {
    return delegate;
  }

  @Override
  public String toString() {
    return entrySet().toString();
  }

  private RequestParameters applySubOrganisationParameters(SelectionParametersDto parameters) {
    var subOrganisationParameters = new RequestParametersAdapter()
      .setIfNotEmpty(FACILITY, parameters.getFacilityIds())
      .setIfNotEmpty(CITY, parameters.getCityIds());
    return new RequestParametersAdapter(delegate, subOrganisationParameters);
  }

  private RequestParametersAdapter setIfNotEmpty(RequestParameter parameter, List<String> values) {
    if (isNotEmpty(values)) {
      setAll(parameter, values);
    }
    return this;
  }
}
