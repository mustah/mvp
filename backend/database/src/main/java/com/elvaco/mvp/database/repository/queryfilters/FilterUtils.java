package com.elvaco.mvp.database.repository.queryfilters;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;

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
}
