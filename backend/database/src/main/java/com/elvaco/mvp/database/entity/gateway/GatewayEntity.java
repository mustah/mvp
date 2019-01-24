package com.elvaco.mvp.database.entity.gateway;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.NotAudited;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@ToString(exclude = "meters")
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway")
public class GatewayEntity extends IdentifiableType<EntityPk> {

  private static final long serialVersionUID = -2132372383987246715L;

  @EmbeddedId
  public EntityPk pk;

  @Column(nullable = false)
  public String serial;

  @Column(nullable = false)
  public String productModel;

  public String ip;

  public String phoneNumber;

  @ManyToMany(mappedBy = "gateways")
  public Set<LogicalMeterEntity> meters = new HashSet<>();

  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "gatewayId", fetch = FetchType.LAZY)
  @Cascade(value = CascadeType.MERGE)
  @NotAudited
  public Set<GatewayStatusLogEntity> statusLogs = new HashSet<>();

  public GatewayEntity(
    EntityPk pk,
    String serial,
    String productModel,
    String ip,
    String phoneNumber,
    Set<GatewayStatusLogEntity> statusLogs
  ) {
    this.pk = pk;
    this.serial = serial;
    this.productModel = productModel;
    this.ip = ip;
    this.phoneNumber = phoneNumber;
    this.meters = emptySet();
    this.statusLogs = unmodifiableSet(statusLogs);
  }

  @Override
  public EntityPk getId() {
    return pk;
  }
}
