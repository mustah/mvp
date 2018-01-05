package com.elvaco.mvp.entity.collection;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "collection")
public class CollectionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String author;
  public String title;

  public CollectionEntity() {}

  public CollectionEntity(String author, String title) {
    this.author = author;
    this.title = title;
  }
}

