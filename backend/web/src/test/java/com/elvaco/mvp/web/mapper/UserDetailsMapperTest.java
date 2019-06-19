package com.elvaco.mvp.web.mapper;

import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.security.MvpUserDetails;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static com.elvaco.mvp.web.security.MvpUserDetails.SPRING_ROLE_PREFIX;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserDetailsMapperTest {

  @Test
  public void throwsWhenPasswordIsNull() {
    User user = new UserBuilder()
      .name("some name")
      .email("email@a.com")
      .organisationElvaco()
      .build();

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
    Stream<String> roles = mvpUserDetails().getAuthorities().stream()
      .map(GrantedAuthority::getAuthority);

    assertThat(roles).containsExactly(
      SPRING_ROLE_PREFIX + MVP_ADMIN.role,
      SPRING_ROLE_PREFIX + MVP_USER.role
    );
  }

  @Test
  public void userIsNotSuperAdmin() {
    assertThat(mvpUserDetails().isSuperAdmin()).isFalse();
  }

  @Test
  public void userIsMvpAdmin() {
    assertThat(mvpUserDetails().isMvpAdmin()).isTrue();
  }

  @Test
  public void userIsWithinOrganisation() {
    assertThat(mvpUserDetails().isWithinOrganisation(ELVACO.id)).isTrue();
  }

  @Test
  public void userIsNotWithinOrganisation() {
    assertThat(mvpUserDetails().isWithinOrganisation(randomUUID())).isFalse();
  }

  private static MvpUserDetails mvpUserDetails() {
    return new MvpUserDetails(
      userBuilder().organisationElvaco().build(),
      randomUUID().toString()
    );
  }
}
