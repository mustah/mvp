package com.elvaco.mvp.security;

import java.util.Arrays;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.mapper.UserMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.CREATE;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.DELETE;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.READ;
import static com.elvaco.mvp.security.OrganisationPermissionEvaluator.Permission.UPDATE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class OrganisationPermissionEvaluatorTest {

  private static UserMapper userMapper;
  private Organisation elvaco = new Organisation(1L, "Elvaco AB", "elvaco");
  private Organisation other = new Organisation(2L, "Other, Inc.", "other");

  private User superAdmin = new User("Super Admin", "root@superus.er", "password", elvaco, Arrays
    .asList(Role.superAdmin()));
  private User elvacoAdmin = new User("Elvaco Administrator", "admin@elvaco.se", "password",
    elvaco, Arrays.asList(Role.admin()));
  private User elvacoUser = new User("Elvaco User", "user@elvaco.se", "password", elvaco, Arrays
    .asList(Role.user()));
  private User secondElvacoUser = new User("Elvaco User No. 2", "user2@elvaco.se", "password",
    elvaco, Arrays
    .asList(Role.user()));
  private User otherAdmin = new User("Other Admin", "admin@other.co.uk", "password", other,
    Arrays.asList(Role.admin()));
  private User otherUser = new User("Other User", "user@other.co.uk", "password", other, Arrays
    .asList(Role.user()));

  private OrganisationPermissionEvaluator ope;

  @BeforeClass
  public static void setUpClass() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setFieldAccessLevel(Configuration.AccessLevel.PUBLIC)
      .setFieldMatchingEnabled(true);

    userMapper = new UserMapper(modelMapper);
  }

  @Before
  public void setUp() {
    MockedUsers users = new MockedUsers(Arrays.asList(superAdmin, elvacoAdmin, elvacoUser,
      secondElvacoUser,
      otherAdmin, otherUser));
    ope = new OrganisationPermissionEvaluator(users, userMapper);
  }

  @Test
  public void lastSuperAdminCannotBeDeleted() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(superAdmin),
      userMapper.toDto(superAdmin),
      DELETE));
  }

  @Test
  public void adminCanReadUserInSameOrganisation() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(elvacoUser),
      READ));
  }

  @Test
  public void adminCanCreateUserInSameOrganisation() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(elvacoUser),
      CREATE));
  }

  @Test
  public void adminCanUpdateUserInSameOrganisation() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(elvacoUser),
      UPDATE));
  }

  @Test
  public void adminCanDeleteUserInSameOrganisation() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(elvacoUser),
      DELETE));

  }

  @Test
  public void regularUserCanReadUsersInSameOrganisation() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(secondElvacoUser),
      READ));

  }

  @Test
  public void regularUserCannotCreateUserInSameOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(secondElvacoUser),
      CREATE));
  }

  @Test
  public void regularUserCannotUpdateUserInSameOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(secondElvacoUser),
      UPDATE));
  }

  @Test
  public void regularUserCannotDeleteUserInSameOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(secondElvacoUser),
      DELETE));
  }


  @Test
  public void adminCannotCreateUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherUser),
      CREATE));
  }

  @Test
  public void adminCannotReadUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherUser),
      READ));
  }

  @Test
  public void adminCannotUpdateUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherUser),
      UPDATE));
  }

  @Test
  public void adminCannotDeleteUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherUser),
      DELETE));
  }

  @Test
  public void adminCannotCreateAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherAdmin),
      CREATE));
  }


  @Test
  public void adminCannotReadAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherAdmin),
      READ));
  }

  @Test
  public void adminCannotUpdateAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherAdmin),
      UPDATE));
  }

  @Test
  public void adminCannotDeleteAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoAdmin),
      userMapper.toDto(otherAdmin),
      DELETE));
  }

  @Test
  public void regularUserCannotCreateUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherUser),
      CREATE));
  }

  @Test
  public void regularUserCannotReadUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherUser),
      READ));
  }

  @Test
  public void regularUserCannotUpdateUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherUser),
      UPDATE));
  }

  @Test
  public void regularUserCannotDeleteUserInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherUser),
      DELETE));
  }

  @Test
  public void regularUserCannotCreateAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherAdmin),
      CREATE));
  }

  @Test
  public void regularUserCannotReadAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherAdmin),
      READ));
  }

  @Test
  public void regularUserCannotUpdateAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherAdmin),
      UPDATE));
  }

  @Test
  public void regularUserCannotDeleteAdminInOtherOrganisation() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(otherAdmin),
      DELETE));
  }

  @Test
  public void userCannotCreateSelf() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(elvacoUser),
      CREATE));
  }

  @Test
  public void userCanReadSelf() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(elvacoUser),
      READ));
  }

  @Test
  public void userCanUpdateSelf() {
    assertTrue(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(elvacoUser),
      UPDATE));
  }

  @Test
  public void userCannotDeleteSelf() {
    assertFalse(ope.evaluateUserDtoPermissions(new MvpUserDetails(elvacoUser),
      userMapper.toDto(elvacoUser),
      DELETE));
  }
}
