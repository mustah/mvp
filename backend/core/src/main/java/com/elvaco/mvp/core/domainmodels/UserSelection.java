package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class UserSelection {

  public final UUID id;
  public final UUID ownerUserId;
  public final UUID organisationId;
  public final String name;
  public final JsonNode selectionParameters;

  public Optional<SelectionParametersDto> toSelectionParametersDto() {
    return Optional.ofNullable(Json.toObject(selectionParameters, SelectionParametersDto.class));
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SelectionParametersDto implements Serializable {

    private static final long serialVersionUID = 2068289577213198309L;

    @Nullable
    public List<IdNamedDto> facilities;

    @Nullable
    public List<IdNamedDto> cities;

    public List<String> getFacilityIds() {
      return facilities != null
        ? facilities.stream().map(idNamedDto -> idNamedDto.id).collect(toList())
        : emptyList();
    }

    public List<String> getCityIds() {
      return cities != null
        ? cities.stream().map(idNamedDto -> idNamedDto.id).collect(toList())
        : emptyList();
    }
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  @NoArgsConstructor
  public static class IdNamedDto implements Serializable {

    private static final long serialVersionUID = 3006064813180853555L;

    public String id;
    public String name;
  }
}
