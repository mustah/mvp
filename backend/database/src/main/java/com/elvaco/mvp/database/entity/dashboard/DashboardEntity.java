package com.elvaco.mvp.database.entity.dashboard;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.JsonField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "dashboard")
@Access(AccessType.FIELD)
public class DashboardEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = 3077521204858765442L;

  @Id
  public UUID id;

  @Column(nullable = false)
  public UUID ownerUserId;

  @Column(nullable = false)
  public UUID organisationId;

  @Column
  public String name;

  @Column(nullable = false)
  public JsonField layout;

  @Override
  public UUID getId() {
    return id;
  }
}
