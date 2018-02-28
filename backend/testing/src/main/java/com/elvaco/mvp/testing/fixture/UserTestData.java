package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static java.util.Collections.singletonList;

public final class UserTestData {

  public static final Organisation MARVEL =
    new Organisation(
      10L,
      "Marvel",
      "marvel"
    );

  public static final Organisation DAILY_PLANET =
    new Organisation(
      9L,
      "Daily Planet",
      "daily-planet"
    );

  public static final User CLARK_KENT = new User(
    "Clark Kent",
    "clark@dailyplanet.org",
    "KalEl",
    DAILY_PLANET,
    singletonList(ADMIN)
  );

  private UserTestData() {}

  public static User dailyPlanetUser(Organisation organisation) {
    return new User(
      "Jimmy Olsen",
      "jimy@dailyplanet.org",
      "jimols",
      organisation,
      singletonList(USER)
    );
  }
}
