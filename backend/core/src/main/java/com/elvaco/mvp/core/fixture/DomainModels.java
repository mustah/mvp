package com.elvaco.mvp.core.fixture;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

public final class DomainModels {

  public static final Organisation ELVACO = new Organisation(randomUUID(), "Elvaco", "elvaco");

  public static final Organisation WAYNE_INDUSTRIES =
    new Organisation(
      randomUUID(),
      "Wayne Industries",
      "wayne-industries"
    );

  public static final User OTHER_ADMIN_USER = new User(
    "Elvis Cohan",
    "elvis.cohan@wayne.com",
    "elvis123",
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(ADMIN)
  );

  public static final User OTHER_USER = new User(
    "Erik Karlsson",
    "erikar@wayne.se",
    "erik123",
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(USER)
  );

  public static final User ELVACO_ADMIN_USER = new User(
    "Peter Eriksson",
    "peteri@elvaco.se",
    "peter123",
    Language.en,
    ELVACO,
    singletonList(ADMIN)
  );

  public static final User ELVACO_USER = new User(
    "Stefan Stefanson",
    "steste@elvaco.se",
    "stefan123",
    Language.en,
    ELVACO,
    singletonList(USER)
  );

  public static final User OTHER_ELVACO_USER = new User(
    "Eva Nilsson",
    "evanil@elvaco.se",
    "eva123",
    Language.en,
    ELVACO,
    singletonList(USER)
  );

  public static final User ELVACO_SUPER_ADMIN_USER = new User(
    "Super Admin",
    "superadmin@elvaco.se",
    "admin123",
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

  public static final User RANDOM_ELVACO_USER = new User(
    "Random User",
    "random@user.tld",
    "yes-random",
    Language.en,
    ELVACO,
    singletonList(ADMIN)
  );

  private DomainModels() {}
}
