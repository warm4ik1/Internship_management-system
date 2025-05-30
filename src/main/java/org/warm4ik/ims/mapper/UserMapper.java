package org.warm4ik.ims.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.warm4ik.ims.dto.role.RoleDto;
import org.warm4ik.ims.dto.user.UserCreateDto;
import org.warm4ik.ims.dto.user.UserResponseDto;
import org.warm4ik.ims.entity.Role;
import org.warm4ik.ims.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "roles", target = "roles")
  UserResponseDto userToUserResponseDto(User user);

  User userCreateDtoToUser(UserCreateDto userCreateDto);

  RoleDto roleToRoleDto(Role role);
}
