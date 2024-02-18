package com.elvaco.mvp.database.entity.dashboard;

import java.io.Serial;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.WidgetType;
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
@Table(name = "widget")
@Access(AccessType.FIELD)
public class WidgetEntity extends IdentifiableType<UUID> {

  @Serial private static final long serialVersionUID = 3077521204858765442L;

  @Id
  public UUID id;

  @Column(nullable = false)
  public UUID dashboardId;

  @Column(nullable = false)
  public UUID ownerUserId;

  @Column(nullable = false)
  public UUID organisationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public WidgetType type;

  @Column
  public String title;

  @Column(nullable = false)
  public JsonField settings;

  @Override
  public UUID getId() {
    return id;
  }
}
