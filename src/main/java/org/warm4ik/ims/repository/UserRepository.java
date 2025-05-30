package org.warm4ik.ims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.warm4ik.ims.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findByEmail(String email);

  boolean existsUserByEmail(String email);

  boolean existsUserByUsername(String username);
}
