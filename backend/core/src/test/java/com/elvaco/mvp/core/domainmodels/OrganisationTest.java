package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Slugify.slugify;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationTest {

  @Test
  public void subOrganisation_withParentWithoutSelection() {
    Organisation parent = new Organisation(randomUUID(), "parent", "parent-slug", "parent");

    assertThatThrownBy(
      () -> new Organisation(randomUUID(), "name", "slug", "external-id", parent, null)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void subOrganisation_withSelectionWithoutParent() {
    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .build();

    assertThatThrownBy(
      () -> new Organisation(randomUUID(), "name", "slug", "external-id", null, selection)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void organisationSlugIsSlugified_parentConstructor() {
    String slug = "w@t;tH3-deux";
    assertThat(
      new Organisation(randomUUID(), "name", "w@t;th3-deux", "external-id").slug
    ).isEqualTo(slugify(slug));
  }

  @Test
  public void organisationSlugIsSlugified_subOrgConstructor() {
    String slug = "w@t;tH3-deux";
    assertThat(
      new Organisation(randomUUID(), "name", "w@t;th3-deux", "external-id", null, null).slug
    ).isEqualTo(slugify(slug));
  }
}
