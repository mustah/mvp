package com.elvaco.mvp.security;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.mapper.UserMapper;
import com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static com.elvaco.mvp.fixture.DomainModels.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.fixture.DomainModels.ELVACO_USER;
import static com.elvaco.mvp.fixture.DomainModels.OTHER_ADMIN_USER;
import static com.elvaco.mvp.fixture.DomainModels.OTHER_ELVACO_USER;
import static com.elvaco.mvp.fixture.DomainModels.OTHER_USER;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.CREATE;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.DELETE;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.READ;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.UPDATE;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.modelmapper.config.Configuration.AccessLevel;

public class OrganisationPermissionEvaluatorTest {

  private static UserMapper userMapper;

  private OrganisationPermissionEvaluator permissionEvaluator;

  @BeforeClass
  public static void setUpClass() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
      .setFieldAccessLevel(AccessLevel.PUBLIC)
      .setFieldMatchingEnabled(true);

    userMapper = new UserMapper(modelMapper);
  }

  @Before
  public void setUp() {
    Users users = new MockUsers(asList(
      ELVACO_SUPER_ADMIN_USER,
      ELVACO_ADMIN_USER,
      ELVACO_USER,
      OTHER_ADMIN_USER,
      OTHER_USER
    ));
    permissionEvaluator = new OrganisationPermissionEvaluator(users, userMapper);
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
  public void userCannotDeleteSelf() {
    assertFalse(hasPermission(ELVACO_USER, ELVACO_USER, DELETE));
  }

  private boolean hasPermission(User currentUser, User targetUser, Permission permission) {
    return permissionEvaluator.evaluateUserDtoPermissions(
      new MvpUserDetails(currentUser),
      userMapper.toDto(targetUser),
      permission
    );
  }
}
