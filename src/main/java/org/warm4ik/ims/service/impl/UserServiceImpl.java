package org.warm4ik.ims.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.warm4ik.ims.dto.user.UserCreateDto;
import org.warm4ik.ims.dto.user.UserResponseDto;
import org.warm4ik.ims.entity.Role;
import org.warm4ik.ims.entity.User;
import org.warm4ik.ims.exception.custom.ResourceNotFoundException;
import org.warm4ik.ims.exception.custom.ValidationException;
import org.warm4ik.ims.mapper.UserMapper;
import org.warm4ik.ims.repository.UserRepository;
import org.warm4ik.ims.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final RoleServiceImpl roleServiceImpl;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  @Override
  public UserResponseDto getUserById(Long userId) {

    return userMapper.userToUserResponseDto(
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Пользователь с id: " + userId + " не найден")));
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponseDto getUserByEmail(String email) {

    return userMapper.userToUserResponseDto(
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Пользователь с email: " + email + " не найден")));
  }

  @Transactional
  @Override
  public UserResponseDto addUser(UserCreateDto userDto) {

    if (userRepository.existsUserByEmail(userDto.email())) {
      throw new ValidationException("Email уже используется другим пользователем.");
    } else if (userRepository.existsUserByUsername(userDto.username())) {
      throw new ValidationException("Username занят, попробуйте ввести другой.");
    }

    User user = userMapper.userCreateDtoToUser(userDto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    Role userRole = roleServiceImpl.findRoleByTitle("USER");
    user.setRoles(List.of(userRole));

    userRepository.save(user);

    return userMapper.userToUserResponseDto(user);
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {

    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () ->
                (new ResourceNotFoundException(
                    String.format("Пользователь с таким email: %s, не найден ", email))));
  }

  @Transactional(readOnly = true)
  public User getEntityById(Long userId) {

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Пользователь с id: " + userId + " не найден"));
  }

  @Transactional
  public void save(User user) {

    userRepository.save(user);
  }
}
