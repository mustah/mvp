package com.elvaco.mvp.entity.collection;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class CollectionEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String author;
  public String title;

  public CollectionEntity() {}

  public CollectionEntity(String author, String title) {
    this.author = author;
    this.title = title;
  }
}

