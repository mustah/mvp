package com.elvaco.mvp.core.domainmodels;

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
    String externalId
  ) {
    this(id, name, slug, externalId, null, null);
  }

  public Organisation(
    UUID id,
    String name,
    String slug,
    String externalId,
    @Nullable Organisation parent,
    @Nullable UserSelection selection
  ) {
    if (parent == null ^ selection == null) {
      throw new IllegalArgumentException(
        "Organisation needs either both 'parent' and 'selection', or none");
    }
    this.id = id;
    this.name = name;
    this.slug = slugify(slug);
    this.externalId = externalId;
    this.parent = parent;
    this.selection = selection;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public boolean isSubOrganisation() {
    return parent != null;
  }
}
