package com.elvaco.mvp.mapper;

import java.util.Collections;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.security.MvpUserDetails;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserDetailsMapperTest {

  private static final Organisation ELVACO = new Organisation(1L, "Elvaco", "elvaco");
  private static final Organisation WAYNE_INDUSTRIES = new Organisation(2L, "Wayne", "Wayne Inc");

  @Test(expected = NullPointerException.class)
  public void throwsWhenUsernameIsNull() {
    User user = new User(
      2L,
      "some name",
      null,
      "password",
      new Organisation(1L, "t", "b"),
      Collections.emptyList()
    );

    new MvpUserDetails(user);
  }

  @Test(expected = NullPointerException.class)
  public void throwsWhenPasswordIsNull() {
    User user = new User(
      2L,
      "some name",
      "email@a.com",
      null,
      new Organisation(1L, "t", "b"),
      Collections.emptyList()
    );

    new MvpUserDetails(user);
  }

  @Test
  public void isInstanceOfMvpUserDetails() {
    assertThat((UserDetails) mvpUserDetails()).isExactlyInstanceOf(MvpUserDetails.class);
  }

  @Test
  public void hasSpringPrefixedRoles() {
    Stream<String> roles = mvpUserDetails()
      .getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority);

    assertThat(roles).containsExactly("ROLE_" + Role.ADMIN, "ROLE_" + Role.USER);
  }

  @Test
  public void userIsNotSuperAdminAndAdmin() {
    assertThat(mvpUserDetails().isSuperAdmin()).isFalse();
  }

  @Test
  public void userIsAdminAndAdmin() {
    assertThat(mvpUserDetails().isAdmin()).isTrue();
  }

  @Test
  public void userIsWithinOrganisation() {
    assertThat(mvpUserDetails().isWithinOrganisation(ELVACO)).isTrue();
  }

  @Test
  public void userIsNotWithingOrganisation() {
    assertThat(mvpUserDetails().isWithinOrganisation(WAYNE_INDUSTRIES)).isFalse();
  }

  private static MvpUserDetails mvpUserDetails() {
    return new MvpUserDetails(new User(
      1L,
      "john doh",
      "a@b.com",
      "letmein",
      ELVACO,
      asList(Role.admin(), Role.user())
    ));
  }
}
