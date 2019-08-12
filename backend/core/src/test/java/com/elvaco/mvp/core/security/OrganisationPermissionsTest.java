package com.elvaco.mvp.core.security;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_ELVACO_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_USER;
import static java.util.UUID.randomUUID;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;

public class OrganisationPermissionsTest {

  private OrganisationPermissions permissionEvaluator;

  @Before
  public void setUp() {
    Users users = new MockUsers(List.of(
      ELVACO_SUPER_ADMIN_USER,
      ELVACO_ADMIN_USER,
      ELVACO_USER,
      OTHER_ADMIN_USER,
      OTHER_USER
    ));
    permissionEvaluator = new OrganisationPermissions(users);
  }

  @Test
  public void lastSuperAdminCannotBeDeleted() {
    assertFalse(hasPermission(ELVACO_SUPER_ADMIN_USER, ELVACO_SUPER_ADMIN_USER, DELETE));
  }

  @Test
  public void adminCanReadUserInSameOrganisation() {
    assertTrue(hasPermission(ELVACO_ADMIN_USER, ELVACO_USER, READ));
  }

  @Test
  public void adminCanCreateUserInSameOrganisation() {
    assertTrue(hasPermission(ELVACO_ADMIN_USER, ELVACO_USER, CREATE));
  }

  @Test
  public void adminCanUpdateUserInSameOrganisation() {
    assertTrue(hasPermission(ELVACO_ADMIN_USER, ELVACO_USER, UPDATE));
  }

  @Test
  public void adminCanDeleteUserInSameOrganisation() {
    assertTrue(hasPermission(ELVACO_ADMIN_USER, ELVACO_USER, DELETE));
  }

  @Test
  public void regularUserCanReadUsersInSameOrganisation() {
    assertTrue(hasPermission(ELVACO_USER, OTHER_ELVACO_USER, READ));
  }

  @Test
  public void regularUserCannotCreateUserInSameOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ELVACO_USER, CREATE));
  }

  @Test
  public void regularUserCannotUpdateUserInSameOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ELVACO_USER, UPDATE));
  }

  @Test
  public void regularUserCannotDeleteUserInSameOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_USER, DELETE));
  }

  @Test
  public void adminCannotCreateUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_USER, CREATE));
  }

  @Test
  public void adminCannotReadUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_USER, READ));
  }

  @Test
  public void adminCannotUpdateUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_USER, UPDATE));
  }

  @Test
  public void adminCannotDeleteUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_USER, DELETE));
  }

  @Test
  public void adminCannotCreateAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_ADMIN_USER, CREATE));
  }

  @Test
  public void adminCannotReadAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_ADMIN_USER, READ));
  }

  @Test
  public void adminCannotUpdateAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_ADMIN_USER, UPDATE));
  }

  @Test
  public void adminCannotDeleteAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_ADMIN_USER, OTHER_ADMIN_USER, DELETE));
  }

  @Test
  public void regularUserCannotCreateUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_USER, CREATE));
  }

  @Test
  public void regularUserCannotReadUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_USER, READ));
  }

  @Test
  public void regularUserCannotUpdateUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_USER, UPDATE));
  }

  @Test
  public void regularUserCannotDeleteUserInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_USER, DELETE));
  }

  @Test
  public void regularUserCannotCreateAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ADMIN_USER, CREATE));
  }

  @Test
  public void regularUserCannotReadAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ADMIN_USER, READ));
  }

  @Test
  public void regularUserCannotUpdateAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ADMIN_USER, UPDATE));
  }

  @Test
  public void regularUserCannotDeleteAdminInOtherOrganisation() {
    assertFalse(hasPermission(ELVACO_USER, OTHER_ADMIN_USER, DELETE));
  }

  @Test
  public void userCannotCreateSelf() {
    assertFalse(hasPermission(ELVACO_USER, ELVACO_USER, CREATE));
  }

  @Test
  public void userCanReadSelf() {
    assertTrue(hasPermission(ELVACO_USER, ELVACO_USER, READ));
  }

  @Test
  public void userCanUpdateSelf() {
    assertTrue(hasPermission(ELVACO_USER, ELVACO_USER, UPDATE));
  }

  @Test
  public void userCanNotElevateRoleToAdmin() {
    assertFalse(
      hasPermission(ELVACO_USER, userWithRole(ELVACO_USER, Role.MVP_ADMIN), ELVACO_USER, UPDATE)
    );
  }

  @Test
  public void userCanNotElevateRoleToSuperAdmin() {
    assertFalse(
      hasPermission(ELVACO_USER, userWithRole(ELVACO_USER, Role.SUPER_ADMIN), ELVACO_USER, UPDATE)
    );
  }

  @Test
  public void adminCanNotElevateRoleToSuperAdmin() {
    assertFalse(
      hasPermission(
        ELVACO_ADMIN_USER,
        userWithRole(ELVACO_ADMIN_USER, Role.SUPER_ADMIN),
        ELVACO_ADMIN_USER,
        UPDATE
      )
    );
  }

  @Test
  public void adminCanNotElevateUserToSuperAdmin() {
    assertFalse(
      hasPermission(
        ELVACO_ADMIN_USER,
        userWithRole(ELVACO_USER, Role.SUPER_ADMIN),
        ELVACO_USER,
        UPDATE
      )
    );
  }

  @Test
  public void adminCanElevateUserToAdmin() {
    assertTrue(
      hasPermission(
        ELVACO_ADMIN_USER,
        userWithRole(ELVACO_USER, Role.MVP_ADMIN),
        ELVACO_USER,
        UPDATE
      )
    );
  }

  @Test
  public void adminCanDemoteAdminToUser() {
    assertTrue(
      hasPermission(
        ELVACO_ADMIN_USER,
        userWithRole(ELVACO_ADMIN_USER, Role.MVP_USER),
        ELVACO_ADMIN_USER,
        UPDATE
      )
    );
  }

  @Test
  public void superAdminCanElevateUserToSuperAdmin() {
    assertTrue(
      hasPermission(
        ELVACO_SUPER_ADMIN_USER,
        userWithRole(ELVACO_USER, Role.SUPER_ADMIN),
        ELVACO_USER,
        UPDATE
      )
    );
  }

  @Test
  public void superAdminCanDemoteSuperAdminToUser() {
    assertTrue(
      hasPermission(
        ELVACO_SUPER_ADMIN_USER,
        userWithRole(ELVACO_SUPER_ADMIN_USER, Role.MVP_USER),
        ELVACO_ADMIN_USER,
        UPDATE
      )
    );
  }

  @Test
  public void unhandledRoleThrowsException() {
    assertThatThrownBy(() -> hasPermission(
      ELVACO_SUPER_ADMIN_USER,
      userWithRole(ELVACO_SUPER_ADMIN_USER, new Role("Dictator")),
      ELVACO_ADMIN_USER,
      UPDATE
    )).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void userCannotDeleteSelf() {
    assertFalse(hasPermission(ELVACO_USER, ELVACO_USER, DELETE));
  }

  private boolean hasPermission(User currentUser, User targetUser, Permission permission) {
    return hasPermission(currentUser, targetUser, targetUser, permission);
  }

  private boolean hasPermission(
    User currentUser,
    User targetUser,
    User beforeUpdate,
    Permission permission
  ) {
    return permissionEvaluator.isAllowed(
      new MockAuthenticatedUser(currentUser, randomUUID().toString()),
      targetUser,
      beforeUpdate,
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
