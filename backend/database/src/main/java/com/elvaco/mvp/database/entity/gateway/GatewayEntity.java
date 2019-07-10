package com.elvaco.mvp.database.entity.gateway;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.JsonField;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.NotAudited;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@ToString(exclude = "gatewayMeters")
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

  @OneToMany(mappedBy = "gateway")
  @NotAudited
  public Set<GatewayMeterEntity> gatewayMeters = new HashSet<>();

  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "gatewayId", fetch = FetchType.LAZY)
  @Cascade(value = CascadeType.MERGE)
  @NotAudited
  public Set<GatewayStatusLogEntity> statusLogs = new HashSet<>();

  @Column(nullable = false)
  public JsonField extraInfo;

  public GatewayEntity(
    EntityPk pk,
    String serial,
    String productModel,
    String ip,
    String phoneNumber,
    Set<GatewayStatusLogEntity> statusLogs,
    JsonField extraInfo
  ) {
    this.pk = pk;
    this.serial = serial;
    this.productModel = productModel;
    this.ip = ip;
    this.phoneNumber = phoneNumber;
    this.gatewayMeters = emptySet();
    this.statusLogs = unmodifiableSet(statusLogs);
    this.extraInfo = extraInfo;
  }

  @Override
  public EntityPk getId() {
    return pk;
  }
}
