package com.elvaco.mvp.entity.report;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "reports")
public class ReportEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String author;
  public String title;

  public ReportEntity() {}

  public ReportEntity(String author, String title) {
    this.author = author;
    this.title = title;
  }
}
