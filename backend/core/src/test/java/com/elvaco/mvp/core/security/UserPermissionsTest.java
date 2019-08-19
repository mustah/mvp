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
import static com.elvaco.mvp.testing.fixture.UserTestData.ANOTHER_MVP_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ANOTHER_OTC_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.ANOTHER_OTC_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.MVP_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.MVP_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTC_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTC_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_MVP_ADMIN;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_MVP_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.SUPER_ADMIN;
import static java.util.UUID.randomUUID;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserPermissionsTest {

  private UserPermissions permissionEvaluator;

  @Before
  public void setUp() {
    Users users = new MockUsers(List.of(
      SUPER_ADMIN,
      MVP_ADMIN,
      MVP_USER,
      OTC_ADMIN,
      OTHER_MVP_ADMIN,
      OTHER_MVP_USER
    ));
    permissionEvaluator = new UserPermissions(users);
  }

  @Test
  public void lastSuperAdminCannotBeDeleted() {
    assertThat(isAllowed(SUPER_ADMIN, SUPER_ADMIN, DELETE)).isFalse();
  }

  @Test
  public void superAdmin_CanCreate_OtcAdmin() {
    assertThat(isAllowed(SUPER_ADMIN, OTC_ADMIN, CREATE)).isTrue();
  }

  @Test
  public void otcAdmin_WithinSameOrganisation_CanCreate_OtcAdmin() {
    assertThat(isAllowed(ANOTHER_OTC_ADMIN, OTC_ADMIN, CREATE)).isTrue();
  }

  @Test
  public void otcAdmin_WithinSameOrganisation_CannotCreate_MvpAdmin() {
    assertThat(isAllowed(OTC_ADMIN, MVP_ADMIN, CREATE)).isFalse();
  }

  @Test
  public void otcAdmin_CanCreate_OtcUser_WithinSameOrganisation() {
    assertThat(isAllowed(OTC_ADMIN, OTC_USER, CREATE)).isTrue();
  }

  @Test
  public void mvpAdmin_NotWithinSameOrganisation_CannotCreate_OtcAdmin() {
    assertThat(isAllowed(OTHER_MVP_ADMIN, OTC_ADMIN, CREATE)).isFalse();
  }

  @Test
  public void otherMvpUser_NotWithinSameOrganisation_CannotCreate_OtcAdmin() {
    assertThat(isAllowed(OTHER_MVP_USER, OTC_ADMIN, CREATE)).isFalse();
  }

  @Test
  public void mvpAdmin_CannotCreate() {
    assertThat(isAllowed(MVP_ADMIN, OTC_USER, CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTC_ADMIN, CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, SUPER_ADMIN, CREATE)).isFalse();
  }

  @Test
  public void otcUser_CannotCreate_OtcUser_WithinSameOrganisation() {
    assertThat(isAllowed(OTC_USER, ANOTHER_OTC_USER, CREATE)).isFalse();
  }

  @Test
  public void mvpAdmin_HasPermissions_ToUserInSameOrganisation() {
    assertThat(isAllowed(MVP_ADMIN, MVP_USER, CREATE)).isTrue();
    assertThat(isAllowed(MVP_ADMIN, MVP_USER, READ)).isTrue();
    assertThat(isAllowed(MVP_ADMIN, MVP_USER, UPDATE)).isTrue();
    assertThat(isAllowed(MVP_ADMIN, MVP_USER, DELETE)).isTrue();
  }

  @Test
  public void mvpAdmin_HasNotPermissions_ToUserInOtherOrganisation() {
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_USER, CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_USER, READ)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_USER, UPDATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_USER, DELETE)).isFalse();
  }

  @Test
  public void mvpAdmin_HasNoAccess_ToAdminInOtherOrganisation() {
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_ADMIN, CREATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_ADMIN, READ)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_ADMIN, UPDATE)).isFalse();
    assertThat(isAllowed(MVP_ADMIN, OTHER_MVP_ADMIN, DELETE)).isFalse();
  }

  @Test
  public void mvpUser_WithinSameOrganisation_CannotCreate_OtcAdmin() {
    assertThat(isAllowed(MVP_USER, OTC_ADMIN, CREATE)).isFalse();
  }

  @Test
  public void mvpUser_HasNoPermission_ToMvpUserInSameOrganisation() {
    assertThat(isAllowed(MVP_USER, ANOTHER_MVP_USER, READ)).isTrue();
    assertThat(isAllowed(MVP_USER, ANOTHER_MVP_USER, CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, ANOTHER_MVP_USER, UPDATE)).isFalse();
  }

  @Test
  public void mvpUser_HasNoPermission_ToMvpUserInOtherOrganisation() {
    assertThat(isAllowed(MVP_USER, OTHER_MVP_USER, CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_USER, READ)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_USER, UPDATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_USER, DELETE)).isFalse();
  }

  @Test
  public void mvpUser_HasNoAccess_ToAdminInOtherOrganisation() {
    assertThat(isAllowed(MVP_USER, OTHER_MVP_ADMIN, CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_ADMIN, READ)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_ADMIN, UPDATE)).isFalse();
    assertThat(isAllowed(MVP_USER, OTHER_MVP_ADMIN, DELETE)).isFalse();
  }

  @Test
  public void mvpUser_Permissions_OnSelf() {
    assertThat(isAllowed(MVP_USER, MVP_USER, CREATE)).isFalse();
    assertThat(isAllowed(MVP_USER, MVP_USER, READ)).isTrue();
    assertThat(isAllowed(MVP_USER, MVP_USER, UPDATE)).isTrue();
    assertThat(isAllowed(MVP_USER, MVP_USER, DELETE)).isFalse();
  }

  @Test
  public void userCanNotElevateRoleToAdmin() {
    assertThat(
      isAllowed(MVP_USER, userWithRole(MVP_USER, Role.MVP_ADMIN), MVP_USER, UPDATE)
    ).isFalse();
  }

  @Test
  public void userCanNotElevateRoleToSuperAdmin() {
    assertThat(
      isAllowed(MVP_USER, userWithRole(MVP_USER, Role.SUPER_ADMIN), MVP_USER, UPDATE)
    ).isFalse();
  }

  @Test
  public void adminCanNotElevateRoleToSuperAdmin() {
    assertThat(
      isAllowed(
        MVP_ADMIN,
        userWithRole(MVP_ADMIN, Role.SUPER_ADMIN),
        MVP_ADMIN,
        UPDATE
      )
    ).isFalse();
  }

  @Test
  public void adminCanNotElevateUserToSuperAdmin() {
    assertThat(
      isAllowed(
        MVP_ADMIN,
        userWithRole(MVP_USER, Role.SUPER_ADMIN),
        MVP_USER,
        UPDATE
      )
    ).isFalse();
  }

  @Test
  public void adminCanElevateUserToAdmin() {
    assertTrue(
      isAllowed(
        MVP_ADMIN,
        userWithRole(MVP_USER, Role.MVP_ADMIN),
        MVP_USER,
        UPDATE
      )
    );
  }

  @Test
  public void adminCanDemoteAdminToUser() {
    assertTrue(
      isAllowed(
        MVP_ADMIN,
        userWithRole(MVP_ADMIN, Role.MVP_USER),
        MVP_ADMIN,
        UPDATE
      )
    );
  }

  @Test
  public void superAdminCanElevateUserToSuperAdmin() {
    assertTrue(
      isAllowed(
        SUPER_ADMIN,
        userWithRole(MVP_USER, Role.SUPER_ADMIN),
        MVP_USER,
        UPDATE
      )
    );
  }

  @Test
  public void superAdminCanDemoteSuperAdminToUser() {
    assertTrue(
      isAllowed(
        SUPER_ADMIN,
        userWithRole(SUPER_ADMIN, Role.MVP_USER),
        MVP_ADMIN,
        UPDATE
      )
    );
  }

  @Test
  public void unhandledRoleThrowsException() {
    assertThatThrownBy(() -> isAllowed(
      SUPER_ADMIN,
      userWithRole(SUPER_ADMIN, new Role("Dictator")),
      MVP_ADMIN,
      UPDATE
    )).isInstanceOf(RuntimeException.class);
  }

  private boolean isAllowed(User currentUser, User targetUser, Permission permission) {
    return isAllowed(currentUser, targetUser, targetUser, permission);
  }

  private boolean isAllowed(
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
