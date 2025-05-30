package org.warm4ik.ims.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.warm4ik.ims.entity.Role;
import org.warm4ik.ims.exception.custom.ResourceNotFoundException;
import org.warm4ik.ims.repository.RoleRepository;
import org.warm4ik.ims.service.RoleService;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  @Transactional(readOnly = true)
  @Override
  public Role findRoleByTitle(String title) {
    return roleRepository
        .findRoleByTitle(title)
        .orElseThrow(() -> (new ResourceNotFoundException("Роль не найдена.")));
  }
}
