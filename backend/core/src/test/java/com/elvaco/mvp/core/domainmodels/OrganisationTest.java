package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Slugify.slugify;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationTest {

  @Test
  public void subOrganisation_withParentWithoutSelection() {
    Organisation parent = Organisation.builder()
      .name("parent")
      .slug("parent-slug")
      .externalId("parent")
      .build();

    assertThatThrownBy(() -> organisation().parent(parent).build())
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void subOrganisation_withSelectionWithoutParent() {
    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .build();

    assertThatThrownBy(() -> organisation().selection(selection).build())
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void organisationSlugIsSlugified_parentConstructor() {
    String slug = "w@t;tH3-deux";

    assertThat(organisation().slug(slug).build().slug).isEqualTo(slugify(slug));
  }

  @Test
  public void organisationSlugIsSlugified_subOrgConstructor() {
    String slug = "w@t;tH3-deux";

    assertThat(organisation().slug(slug).build().slug).isEqualTo(slugify(slug));
  }

  private static Organisation.OrganisationBuilder organisation() {
    return Organisation.builder()
      .name("name")
      .slug("slug")
      .externalId("external-id");
  }
}
