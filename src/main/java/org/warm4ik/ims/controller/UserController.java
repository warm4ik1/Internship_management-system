package org.warm4ik.ims.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.warm4ik.ims.dto.user.UserResponseDto;
import org.warm4ik.ims.service.UserService;

@RestController
@RequestMapping("/ims/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userServiceImpl;

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == principal.getId()")
  public UserResponseDto getUserById(@PathVariable(name = "id") @Min(1L) Long id) {
    return userServiceImpl.getUserById(id);
  }

  @GetMapping("/email/{email}")
  @PreAuthorize("hasRole('ADMIN') or #email == principal.username")
  public UserResponseDto getUserByEmail(@PathVariable(name = "email") @NotBlank String email) {
    return userServiceImpl.getUserByEmail(email);
  }

  @GetMapping("/me")
  public UserResponseDto getCurrentUser(Authentication authentication) {
    String email = (String) authentication.getPrincipal();
    return userServiceImpl.getUserByEmail(email);
  }
}
