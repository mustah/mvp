package com.elvaco.mvp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findByFirstName(@Param("firstName") String firstName);
    Collection<User> findByLastName(@Param("lastName") String lastName);
}
