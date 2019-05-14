package com.elvaco.mvp.database.repository.jpa;

import java.util.Collection;
import java.util.UUID;

import com.elvaco.mvp.database.entity.organisationtheme.OrganisationThemeEntity;
import com.elvaco.mvp.database.entity.organisationtheme.OrganisationThemePk;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationThemeJpaRepository
  extends JpaRepository<OrganisationThemeEntity, OrganisationThemePk> {

  void deleteByOrganisationThemePkOrganisationId(UUID organisationId);

  Collection<OrganisationThemeEntity> findByOrganisationThemePkOrganisationId(UUID organisationId);
}
