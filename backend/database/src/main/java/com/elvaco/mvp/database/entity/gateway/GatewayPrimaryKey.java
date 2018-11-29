package com.elvaco.mvp.database.entity.gateway;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class GatewayPrimaryKey implements Serializable, PrimaryKey {

  private static final long serialVersionUID = 7252179265239386706L;

  @Column(nullable = false, updatable = false)
  public UUID gatewayId;

  @Column(nullable = false, updatable = false)
  public UUID organisationId;

  @Override
  public UUID getId() {
    return gatewayId;
  }

  @Override
  public UUID getOrganisationId() {
    return organisationId;
  }
}
