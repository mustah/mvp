package com.elvaco.mvp.entity.dashboard;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "dashboards")
public class DashboardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String title;
  public String author;

  public DashboardEntity() {
  }

  public DashboardEntity(String title, String author) {
    this.title = title;
    this.author = author;
  }
}
