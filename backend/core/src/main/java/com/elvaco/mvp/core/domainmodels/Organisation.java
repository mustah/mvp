package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Slugify.slugify;
import static java.util.UUID.randomUUID;

@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class Organisation implements Identifiable<UUID> {

  @Builder.Default
  public UUID id = randomUUID();
  public final String name;
  public final String slug;
  public final String externalId;
  @Nullable
  public final Organisation parent;
  @Nullable
  public final UserSelection selection;

  public Organisation(
    UUID id,
    String name,
    String slug,
    String externalId,
    @Nullable Organisation parent,
    @Nullable UserSelection selection
  ) {
    checkIsValidSubOrganisation(parent, selection);
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.externalId = externalId;
    this.parent = parent;
    this.selection = selection;
  }

  public static OrganisationBuilder builderFrom(String name) {
    return Organisation.builder()
      .name(name)
      .externalId(name)
      .slug(slugify(name));
  }

  public static Organisation of(String name) {
    return Organisation.builderFrom(name).build();
  }

  public static Organisation of(String name, @Nullable UUID id) {
    return Organisation.builderFrom(name)
      .id(id != null ? id : randomUUID())
      .build();
  }

  public static OrganisationBuilder subOrganisation(
    String name,
    @Nullable Organisation parent,
    @Nullable UserSelection selection
  ) {
    checkIsValidSubOrganisation(parent, selection);
    return Organisation.builderFrom(name)
      .parent(parent)
      .selection(selection);
  }

  @Override
  public UUID getId() {
    return id;
  }

  Optional<UUID> getParentId() {
    return Optional.ofNullable(parent).map(Organisation::getId);
  }

  Optional<UserSelection.SelectionParametersDto> getSelectionParameters() {
    return Optional.ofNullable(selection)
      .flatMap(UserSelection::toSelectionParametersDto);
  }

  private static void checkIsValidSubOrganisation(
    @Nullable Organisation parent,
    @Nullable UserSelection selection
  ) {
    if (parent == null ^ selection == null) {
      throw new IllegalArgumentException(
        "Organisation needs either both 'parent' and 'selection', or none");
    }
  }
}
