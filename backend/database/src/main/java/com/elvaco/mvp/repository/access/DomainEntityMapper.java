package com.elvaco.mvp.repository.access;

interface DomainEntityMapper<D, E> {
  D toDomainModel(E entity);

  E toEntity(D domainModel);
}
