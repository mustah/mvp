package com.elvaco.mvp.database.repository.mappers;

public interface DomainEntityMapper<D, E> {

  D toDomainModel(E entity);

  E toEntity(D domainModel);
}
