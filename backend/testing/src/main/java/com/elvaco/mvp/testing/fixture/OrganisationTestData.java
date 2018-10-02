package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Organisation;
import lombok.experimental.UtilityClass;

import static java.util.UUID.randomUUID;

@UtilityClass
public class OrganisationTestData {

  public static final Organisation OTHER_ORGANISATION = new Organisation(
    randomUUID(),
    "Other Organisation",
    "other-organisation",
    "Other Organisation"
  );

  public static final Organisation SECRET_SERVICE = new Organisation(
    randomUUID(),
    "Secret Service",
    "secret-service",
    "Secret Service"
  );

  public static final Organisation DAILY_PLANET =
    new Organisation(
      randomUUID(),
      "Daily Planet",
      "daily-planet",
      "Daily Planet"
    );

  public static final Organisation MARVEL =
    new Organisation(
      randomUUID(),
      "Marvel",
      "marvel",
      "Marvel"
    );
}
