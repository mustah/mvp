package com.elvaco.mvp.database.entity.gateway;

import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@ToString(exclude = "meters")
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway")
@Audited
public class GatewayEntity extends IdentifiableType<UUID> {

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

  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "gatewayId", fetch = FetchType.LAZY)
  @Cascade(value = CascadeType.MERGE)
  @NotAudited
  public Set<GatewayStatusLogEntity> statusLogs;

  public GatewayEntity(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    Set<GatewayStatusLogEntity> statusLogs
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = emptySet();
    this.statusLogs = unmodifiableSet(statusLogs);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
