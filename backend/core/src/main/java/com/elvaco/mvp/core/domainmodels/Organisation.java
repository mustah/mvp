package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.UUID;

import com.elvaco.mvp.core.util.Slugify;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Organisation implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -375927914085016616L;

  public final UUID id;
  public final String name;
  public final String slug;
  public final String externalId;

  public Organisation(UUID id, String name) {
    this(id, name, Slugify.slugify(name));
  }

  public Organisation(UUID id, String name, String slug) {
    this(id, name, slug, slug);
  }

  public Organisation(UUID id, String name, String slug, String externalId) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.externalId = externalId;
  }

  @Override
  public UUID getId() {
    return id;
  }

}
