package com.elvaco.mvp.entity.validation;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class ValidationEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String author;
  public String title;

  public ValidationEntity() {
  }

  public ValidationEntity(String author, String title) {
    this.author = author;
    this.title = title;
  }
}

