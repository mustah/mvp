package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Slugify.slugify;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Organisation implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -375927914085016616L;

  public final UUID id;
  public final String name;
  public final String slug;
  public final String externalId;

  @Nullable
  public final Organisation parent;

  public Organisation(
    UUID id,
    String name,
    String slug,
    String externalId
  ) {
    this(id, name, slugify(slug), externalId, null);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
