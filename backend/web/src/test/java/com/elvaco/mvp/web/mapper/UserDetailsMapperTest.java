package com.elvaco.mvp.web.mapper;

import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.fixture.DomainModels.WAYNE_INDUSTRIES;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserDetailsMapperTest {

  @Test
  public void throwsWhenPasswordIsNull() {
    User user = new User(
      2L,
      "some name",
      "email@a.com",
      null,
      ELVACO,
      emptyList()
    );

    assertThatThrownBy(() -> new MvpUserDetails(user, randomUUID().toString()))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("User must have a password.");
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

    assertThat(roles).containsExactly("ROLE_" + ADMIN.role, "ROLE_" + USER.role);
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
      asList(ADMIN, USER)
    ), randomUUID().toString());
  }
}
