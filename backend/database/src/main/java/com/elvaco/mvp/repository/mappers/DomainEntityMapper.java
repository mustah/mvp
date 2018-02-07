package com.elvaco.mvp.repository.mappers;

public interface DomainEntityMapper<D, E> {

  D toDomainModel(E entity);

  E toEntity(D domainModel);
}
