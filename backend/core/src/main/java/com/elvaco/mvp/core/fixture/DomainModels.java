package com.elvaco.mvp.core.fixture;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@UtilityClass
public class DomainModels {

  public static final Organisation ELVACO = new Organisation(randomUUID(), "Elvaco", "elvaco");

  public static final Organisation WAYNE_INDUSTRIES =
    new Organisation(
      randomUUID(),
      "Wayne Industries",
      "wayne-industries"
    );

  public static final String ELVACO_SUPER_ADMIN_USER_PASSWORD = "admin123";
  public static final User ELVACO_SUPER_ADMIN_USER = new User(
    "Super Admin",
    "superadmin@elvaco.se",
    ELVACO_SUPER_ADMIN_USER_PASSWORD,
    Language.en,
    ELVACO,
    singletonList(SUPER_ADMIN)
  );

  public static final User DEVELOPER_USER = new User(
    "Developer",
    "user@domain.tld",
    "complicated_password",
    Language.en,
    ELVACO,
    singletonList(SUPER_ADMIN)
  );

  public static final String RANDOM_ELVACO_USER_PASSWORD = "yes-random";
  public static final User RANDOM_ELVACO_USER = new User(
    "Random User",
    "random@user.tld",
    RANDOM_ELVACO_USER_PASSWORD,
    Language.en,
    ELVACO,
    singletonList(ADMIN)
  );

  private static final String OTHER_ADMIN_USER_PASSWORD = "elvis123";
  public static final User OTHER_ADMIN_USER = new User(
    "Elvis Cohan",
    "elvis.cohan@wayne.com",
    OTHER_ADMIN_USER_PASSWORD,
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(ADMIN)
  );

  private static final String OTHER_USER_PASSWORD = "erik123";
  public static final User OTHER_USER = new User(
    "Erik Karlsson",
    "erikar@wayne.se",
    OTHER_USER_PASSWORD,
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(USER)
  );

  private static final String ELVACO_ADMIN_USER_PASSWORD = "peter123";
  public static final User ELVACO_ADMIN_USER = new User(
    "Peter Eriksson",
    "peteri@elvaco.se",
    ELVACO_ADMIN_USER_PASSWORD,
    Language.en,
    ELVACO,
    singletonList(ADMIN)
  );

  private static final String ELVACO_USER_PASSWORD = "stefan123";
  public static final User ELVACO_USER = new User(
    "Stefan Stefanson",
    "steste@elvaco.se",
    ELVACO_USER_PASSWORD,
    Language.en,
    ELVACO,
    singletonList(USER)
  );

  private static final String OTHER_ELVACO_USER_PASSWORD = "eva123";
  public static final User OTHER_ELVACO_USER = new User(
    "Eva Nilsson",
    "evanil@elvaco.se",
    OTHER_ELVACO_USER_PASSWORD,
    Language.en,
    ELVACO,
    singletonList(USER)
  );
}
