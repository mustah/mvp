package com.elvaco.mvp.entities.dataanalysis;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class DataAnalysisEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String author;
  public String title;

  public DataAnalysisEntity() {}

  public DataAnalysisEntity(String author, String title) {
    this.author = author;
    this.title = title;
  }
}
