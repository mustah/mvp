package com.elvaco.mvp.core.security;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static com.elvaco.mvp.testing.fixture.UserTestData.MVP_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.MVP_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTC_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.SUPER_ADMIN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationPermissionsTest {

  private OrganisationPermissions permissionEvaluator;

  @Before
  public void setUp() {
    permissionEvaluator = new OrganisationPermissions();
  }

  @Test
  public void superAdmin_HasAllAccess() {
    assertThat(isAllowed(SUPER_ADMIN, ELVACO, Permission.READ)).isTrue();
    assertThat(isAllowed(SUPER_ADMIN, ELVACO, Permission.CREATE)).isTrue();
    assertThat(isAllowed(SUPER_ADMIN, ELVACO, Permission.UPDATE)).isTrue();
    assertThat(isAllowed(SUPER_ADMIN, ELVACO, Permission.DELETE)).isTrue();
  }

  @Test
  public void mvpAdmin_WithinOrganisation_Can_Read() {
    assertThat(isAllowed(MVP_ADMIN, ELVACO, Permission.READ)).isTrue();
    assertThat(isAllowed(MVP_ADMIN, ELVACO, Permission.CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, ELVACO, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, ELVACO, Permission.DELETE)).isFalse();
  }

  @Test
  public void otcAdmin_WithinOrganisation_Can_Read() {
    assertThat(isAllowed(OTC_ADMIN, ELVACO, Permission.READ)).isTrue();
    assertThat(isAllowed(OTC_ADMIN, ELVACO, Permission.CREATE)).isFalse();
    assertThat(isAllowed(OTC_ADMIN, ELVACO, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(OTC_ADMIN, ELVACO, Permission.DELETE)).isFalse();
  }

  @Test
  public void mvpAdmin_HasNoAccessToOtherOrganisation() {
    assertThat(isAllowed(MVP_ADMIN, OTHER_ORGANISATION, Permission.CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_ORGANISATION, Permission.READ)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_ORGANISATION, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_ORGANISATION, Permission.DELETE)).isFalse();
  }

  @Test
  public void mvpUser_HasNoAccess() {
    assertThat(isAllowed(MVP_USER, ELVACO, Permission.CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, ELVACO, Permission.READ)).isFalse();
    assertThat(isAllowed(MVP_USER, ELVACO, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(MVP_USER, ELVACO, Permission.DELETE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_ORGANISATION, Permission.CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_ORGANISATION, Permission.READ)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_ORGANISATION, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_ORGANISATION, Permission.DELETE)).isFalse();
  }

  @Test
  public void otcAdmin_HasNoAccess_OutsideOfItsOrganisation() {
    assertThat(isAllowed(OTC_ADMIN, OTHER_ORGANISATION, Permission.CREATE)).isFalse();
    assertThat(isAllowed(OTC_ADMIN, OTHER_ORGANISATION, Permission.READ)).isFalse();
    assertThat(isAllowed(OTC_ADMIN, OTHER_ORGANISATION, Permission.UPDATE)).isFalse();
    assertThat(isAllowed(OTC_ADMIN, OTHER_ORGANISATION, Permission.DELETE)).isFalse();
  }

  private boolean isAllowed(
    User currentUser,
    Organisation target,
    Permission permission
  ) {
    return permissionEvaluator.isAllowed(
      new MockAuthenticatedUser(currentUser, randomUUID().toString()),
      target,
      permission
    );
  }

  private User userWithRole(User user, Role role) {
    return new User(
      user.id,
      user.name,
      user.email,
      user.password,
      user.language,
      user.organisation,
      List.of(role)
    );
  }
}
