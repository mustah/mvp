package com.elvaco.mvp.entity.dashboard;

import javax.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(name = "dashboards")
public class DashboardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String title;
  public String author;

  public DashboardEntity() {}

  public DashboardEntity(String title, String author) {
    this.title = title;
    this.author = author;
  }
}
