package com.elvaco.mvp.database.entity.gateway;

import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import static java.util.Collections.emptySet;

@NoArgsConstructor
@ToString(exclude = "meters")
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway")
public class GatewayEntity extends EntityType<UUID> {

  private static final long serialVersionUID = -2132372383987246715L;

  @Id
  public UUID id;

  @Column(nullable = false)
  public String serial;

  @Column(nullable = false)
  public String productModel;

  @Column(nullable = false)
  public UUID organisationId;

  @ManyToMany(mappedBy = "gateways")
  @Fetch(FetchMode.SUBSELECT)
  public Set<LogicalMeterEntity> meters;

  public GatewayEntity(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = emptySet();
  }

  @Override
  public UUID getId() {
    return id;
  }
}
