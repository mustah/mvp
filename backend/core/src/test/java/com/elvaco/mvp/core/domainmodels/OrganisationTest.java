package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationTest {

  @Test
  public void subOrganisation_withParentWithoutSelection() {
    var parent = Organisation.of("parent");

    assertThatThrownBy(() -> Organisation.builderFrom("name").parent(parent).build())
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void subOrganisation_withSelectionWithoutParent() {
    var selection = UserSelection.builder()
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .build();

    assertThatThrownBy(() -> Organisation.builderFrom("name").selection(selection).build())
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void organisationNameIsUsedSlug() {
    assertThat(Organisation.of("w@t;tH3-deux").slug).isEqualTo("wtth3-deux");
  }
}
