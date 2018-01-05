package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.user.OrganisationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<OrganisationEntity, Long> {
}
