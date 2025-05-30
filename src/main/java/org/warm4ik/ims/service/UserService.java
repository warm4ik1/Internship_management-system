package org.warm4ik.ims.service;

import org.warm4ik.ims.dto.user.UserCreateDto;
import org.warm4ik.ims.dto.user.UserResponseDto;
import org.warm4ik.ims.entity.User;

public interface UserService {

  UserResponseDto getUserById(Long userId);

  UserResponseDto getUserByEmail(String email);

  UserResponseDto addUser(UserCreateDto userDto);

  User findByEmail(String email);

  User getEntityById(Long userId);
}
