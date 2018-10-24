package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Slugify.slugify;

@EqualsAndHashCode
@ToString
public class Organisation implements Identifiable<UUID> {

  public final UUID id;
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
    this.id = id;
    this.name = name;
    this.slug = slugify(slug);
    this.externalId = externalId;
    if (parent == null ^ selection == null) {
      throw new IllegalArgumentException(
        "Organisation needs either both 'parent' and 'selection', or none");
    }
    this.parent = parent;
    this.selection = selection;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
