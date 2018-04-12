package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

import static java.util.UUID.randomUUID;

public final class UserTestData {

  public static final Organisation MARVEL =
    new Organisation(
      randomUUID(),
      "Marvel",
      "marvel"
    );

  public static final Organisation DAILY_PLANET =
    new Organisation(
      randomUUID(),
      "Daily Planet",
      "daily-planet"
    );

  public static final User CLARK_KENT = new UserBuilder()
    .name("Clark Kent")
    .email("clark@dailyplanet.org")
    .password("KalEl")
    .language(Language.en)
    .organisation(DAILY_PLANET)
    .asAdmin()
    .build();

  private UserTestData() {}

  public static User dailyPlanetUser(Organisation organisation) {
    return new UserBuilder()
      .name("Jimmy Olsen")
      .email("jimy@dailyplanet.org")
      .password("jimols")
      .language(Language.en)
      .organisation(organisation)
      .asUser()
      .build();
  }
}
