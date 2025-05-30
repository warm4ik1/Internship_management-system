package org.warm4ik.ims.service;

import org.warm4ik.ims.entity.Role;

public interface RoleService {
    Role findRoleByTitle(String title);
}
