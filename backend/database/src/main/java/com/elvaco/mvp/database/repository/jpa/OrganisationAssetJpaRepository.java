package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.organisationasset.OrganisationAssetEntity;
import com.elvaco.mvp.database.entity.organisationasset.OrganisationAssetPk;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationAssetJpaRepository
  extends JpaRepository<OrganisationAssetEntity, OrganisationAssetPk> {

}
