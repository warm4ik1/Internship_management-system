package org.warm4ik.ims.repository;

import org.springframework.data.repository.CrudRepository;
import org.warm4ik.ims.entity.Role;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {

  Optional<Role> findRoleByTitle(String title);
}
