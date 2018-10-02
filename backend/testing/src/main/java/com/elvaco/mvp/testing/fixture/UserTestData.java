package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.DAILY_PLANET;

@UtilityClass
public class UserTestData {

  public static final User CLARK_KENT = new UserBuilder()
    .name("Clark Kent")
    .email("clark@dailyplanet.org")
    .password("KalEl")
    .language(Language.en)
    .organisation(DAILY_PLANET)
    .asAdmin()
    .build();

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
