package com.elvaco.mvp.core.security;

import java.util.Arrays;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.CREATE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.DELETE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.READ;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.UPDATE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class OrganisationPermissionsTest {

  private Organisation elvaco = new Organisation(1L, "Elvaco AB", "elvaco");
  private Organisation other = new Organisation(2L, "Other, Inc.", "other");

  private User superAdmin = new User("Super Admin", "root@superus.er", "password", elvaco, Arrays
    .asList(Role.superAdmin()));
  private User elvacoAdmin = new User("Elvaco Administrator", "admin@elvaco.se", "password",
                                      elvaco, Arrays.asList(Role.admin())
  );
  private User elvacoUser = new User("Elvaco User", "user@elvaco.se", "password", elvaco, Arrays
    .asList(Role.user()));
  private User secondElvacoUser = new User("Elvaco User No. 2", "user2@elvaco.se", "password",
                                           elvaco, Arrays
                                             .asList(Role.user())
  );
  private User otherAdmin = new User("Other Admin", "admin@other.co.uk", "password", other,
                                     Arrays.asList(Role.admin())
  );
  private User otherUser = new User("Other User", "user@other.co.uk", "password", other, Arrays
    .asList(Role.user()));

  private OrganisationPermissions ope;

  @Before
  public void setUp() {
    MockedUsers users = new MockedUsers(Arrays.asList(superAdmin, elvacoAdmin, elvacoUser,
                                                      secondElvacoUser,
                                                      otherAdmin, otherUser
    ));
    ope = new OrganisationPermissions(users);
  }

  @Test
  public void lastSuperAdminCannotBeDeleted() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(superAdmin),
      superAdmin,
      DELETE
    ));
  }

  @Test
  public void adminCanReadUserInSameOrganisation() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      elvacoUser,
      READ
    ));
  }

  @Test
  public void adminCanCreateUserInSameOrganisation() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      elvacoUser,
      CREATE
    ));
  }

  @Test
  public void adminCanUpdateUserInSameOrganisation() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      elvacoUser,
      UPDATE
    ));
  }

  @Test
  public void adminCanDeleteUserInSameOrganisation() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      elvacoUser,
      DELETE
    ));

  }

  @Test
  public void regularUserCanReadUsersInSameOrganisation() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      secondElvacoUser,
      READ
    ));

  }

  @Test
  public void regularUserCannotCreateUserInSameOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      secondElvacoUser,
      CREATE
    ));
  }

  @Test
  public void regularUserCannotUpdateUserInSameOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      secondElvacoUser,
      UPDATE
    ));
  }

  @Test
  public void regularUserCannotDeleteUserInSameOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      secondElvacoUser,
      DELETE
    ));
  }


  @Test
  public void adminCannotCreateUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherUser,
      CREATE
    ));
  }

  @Test
  public void adminCannotReadUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherUser,
      READ
    ));
  }

  @Test
  public void adminCannotUpdateUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherUser,
      UPDATE
    ));
  }

  @Test
  public void adminCannotDeleteUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherUser,
      DELETE
    ));
  }

  @Test
  public void adminCannotCreateAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherAdmin,
      CREATE
    ));
  }


  @Test
  public void adminCannotReadAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherAdmin,
      READ
    ));
  }

  @Test
  public void adminCannotUpdateAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherAdmin,
      UPDATE
    ));
  }

  @Test
  public void adminCannotDeleteAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoAdmin),
      otherAdmin,
      DELETE
    ));
  }

  @Test
  public void regularUserCannotCreateUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherUser,
      CREATE
    ));
  }

  @Test
  public void regularUserCannotReadUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherUser,
      READ
    ));
  }

  @Test
  public void regularUserCannotUpdateUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherUser,
      UPDATE
    ));
  }

  @Test
  public void regularUserCannotDeleteUserInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherUser,
      DELETE
    ));
  }

  @Test
  public void regularUserCannotCreateAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherAdmin,
      CREATE
    ));
  }

  @Test
  public void regularUserCannotReadAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherAdmin,
      READ
    ));
  }

  @Test
  public void regularUserCannotUpdateAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherAdmin,
      UPDATE
    ));
  }

  @Test
  public void regularUserCannotDeleteAdminInOtherOrganisation() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      otherAdmin,
      DELETE
    ));
  }

  @Test
  public void userCannotCreateSelf() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      elvacoUser,
      CREATE
    ));
  }

  @Test
  public void userCanReadSelf() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      elvacoUser,
      READ
    ));
  }

  @Test
  public void userCanUpdateSelf() {
    assertTrue(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      elvacoUser,
      UPDATE
    ));
  }

  @Test
  public void userCannotDeleteSelf() {
    assertFalse(ope.isAllowed(
      new MockAuthenticatedUser(elvacoUser),
      elvacoUser,
      DELETE
    ));
  }
}
