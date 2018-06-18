package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public final class FilterUtils {

  public static boolean isStatusQuery(RequestParameters parameters) {
    return parameters.hasName("before")
      && parameters.hasName("after")
      && parameters.hasName("status");
  }

  public static boolean isGatewayQuery(RequestParameters parameters) {
    return parameters.hasName("gatewaySerial");
  }

  public static boolean isPhysicalQuery(RequestParameters parameters) {
    return parameters.hasName("facility")
      || parameters.hasName("secondaryAddress")
      || isStatusQuery(parameters);
  }

  public static boolean isOrganisationQuery(RequestParameters parameters) {
    return parameters.hasName("organisation");
  }

  static List<StatusType> toStatusTypes(List<String> values) {
    return values.stream()
      .map(StatusType::from)
      .collect(toList());
  }

  static List<UUID> toUuids(List<String> values) {
    return values.stream()
      .map(UUID::fromString)
      .collect(toList());
  }
}
