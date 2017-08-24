package com.elvaco.mvp.entities.dashboard;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class DashboardEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String title;
  public String author;

  public DashboardEntity() {}

  public DashboardEntity(String title, String author) {
    this.title = title;
    this.author = author;
  }
}
