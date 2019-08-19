package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;

@UtilityClass
public class UserTestData {

  public static final User CLARK_KENT_MVP_ADMIN = new UserBuilder()
    .name("Clark Kent")
    .email("clark@dailyplanet.org")
    .password("KalEl")
    .organisation(DAILY_PLANET)
    .asMvpAdmin()
    .build();

  public static final User SUPER_ADMIN = new UserBuilder()
    .name("Super Admin")
    .email("superadmin@elvaco.se")
    .password("superadmin123")
    .organisation(ELVACO)
    .asSuperAdmin()
    .build();

  public static final User MVP_ADMIN = new UserBuilder()
    .name("Admin")
    .email("admin@elvaco.se")
    .password("admin123")
    .organisation(ELVACO)
    .asMvpAdmin()
    .build();

  public static final User MVP_USER = new UserBuilder()
    .name("User")
    .email("user@elvaco.se")
    .password("user123")
    .organisation(ELVACO)
    .asMvpUser()
    .build();

  public static final User ANOTHER_MVP_USER = new UserBuilder()
    .name("User 2")
    .email("user2@elvaco.se")
    .password("user123-2")
    .organisation(ELVACO)
    .asMvpUser()
    .build();

  public static final User OTHER_MVP_USER = new UserBuilder()
    .name("User")
    .email("user@other.com")
    .password("user123")
    .organisation(OTHER_ORGANISATION)
    .asMvpUser()
    .build();

  public static final User OTHER_MVP_ADMIN = new UserBuilder()
    .name("Admin")
    .email("admin@other.com")
    .password("admin123")
    .organisation(OTHER_ORGANISATION)
    .asMvpAdmin()
    .build();

  public static final User OTC_ADMIN = new UserBuilder()
    .name("Jimmy Olsen")
    .email("jimmy@evo.se")
    .password("jimols")
    .organisation(ELVACO)
    .asOtcAdmin()
    .build();

  public static final User OTC_USER = new UserBuilder()
    .name("Jimmy Olsen Otc")
    .email("jimmy-otc@evo.se")
    .password("jimols")
    .organisation(ELVACO)
    .asOtcUser()
    .build();

  public static final User ANOTHER_OTC_ADMIN = new UserBuilder()
    .name("Simone Heart")
    .email("other.jimmy@evo.se")
    .password("jimmy")
    .organisation(ELVACO)
    .asOtcAdmin()
    .build();

  public static final User ANOTHER_OTC_USER = new UserBuilder()
    .name("Simone Heart")
    .email("other.otc-user.jimmy@evo.se")
    .password("jimmy")
    .organisation(ELVACO)
    .asOtcUser()
    .build();

  public static UserBuilder userBuilder() {
    return new UserBuilder()
      .name("john doh")
      .email("a@b.com")
      .password("letmein")
      .roles(Role.MVP_ADMIN, Role.MVP_USER);
  }

  public static UserBuilder subOrgUser() {
    return new UserBuilder()
      .name("sub-org-user")
      .email("sub-org-user@sub.org.com")
      .password("password-god")
      .asMvpUser();
  }
}
