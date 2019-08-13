package com.elvaco.mvp.core.usecase;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;
import com.elvaco.mvp.testing.repository.MockOrganisationAssets;
import com.elvaco.mvp.testing.repository.MockOrganisationThemes;

import org.junit.Before;
import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationThemeUseCasesTest extends DefaultTestFixture {

  private OrganisationThemeUseCases organisationThemeUseCases;

  @Before
  public void before() {
    organisationThemeUseCases = new OrganisationThemeUseCases(
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
    assertThatThrownBy(() -> organisationThemeUseCases.saveTheme(theme.build()))
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

    assertThatThrownBy(() -> organisationThemeUseCases.saveTheme(theme.build()))
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

    assertThatThrownBy(() -> organisationThemeUseCases.saveTheme(theme.build()))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
