package com.elvaco.mvp.core.usecase;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;
import com.elvaco.mvp.testing.repository.MockOrganisationAssets;
import com.elvaco.mvp.testing.repository.MockOrganisationThemes;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationUseCasesTest extends DefaultTestFixture {

  private OrganisationUseCases organisationUseCases;

  @Before
  public void before() {
    var currentUser = new MockAuthenticatedUser(ELVACO_SUPER_ADMIN_USER, "token123");
    organisationUseCases = new OrganisationUseCases(
      currentUser,
      new MockOrganisations(),
      null,
      new MockOrganisationAssets(),
      new MockOrganisationThemes()
    );
  }

  @Test
  public void saveTheme_tooManyProperties() {
    var theme = Theme.builder();
    theme.organisationId(randomUUID());

    for (int i = 0; i < 101; i++) {
      theme.property("Key" + i, "Value" + i);
    }
    assertThatThrownBy(() -> organisationUseCases.saveTheme(theme.build()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void saveTheme_tooLongKey() {
    var theme = Theme.builder();
    theme.organisationId(randomUUID());

    theme.property(
      IntStream.range(0, 101).mapToObj(i -> "x").collect(Collectors.joining()),
      "Value"
    );

    assertThatThrownBy(() -> organisationUseCases.saveTheme(theme.build()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void saveTheme_tooLongValue() {
    var theme = Theme.builder();
    theme.organisationId(randomUUID());

    theme.property(
      "Key",
      IntStream.range(0, 101).mapToObj(i -> "x").collect(Collectors.joining())
    );

    assertThatThrownBy(() -> organisationUseCases.saveTheme(theme.build()))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
