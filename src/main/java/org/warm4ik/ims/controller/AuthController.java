package org.warm4ik.ims.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.warm4ik.ims.service.AuthService;
import org.warm4ik.ims.dto.security.JwtAuthenticationDto;
import org.warm4ik.ims.dto.security.RefreshTokenDto;
import org.warm4ik.ims.dto.security.UserCredentialDto;
import org.warm4ik.ims.dto.user.UserCreateDto;
import org.warm4ik.ims.dto.user.UserResponseDto;
import org.warm4ik.ims.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userServiceImpl;
  private final AuthService authServiceImpl;

  @PostMapping("/sign-in")
  public ResponseEntity<JwtAuthenticationDto> signIn(
      @RequestBody @Valid UserCredentialDto userCredentialDto) {

    JwtAuthenticationDto jwtAuthenticationDto = authServiceImpl.signIn(userCredentialDto);

    return ResponseEntity.ok(jwtAuthenticationDto);
  }

  @PostMapping("/refresh")
  public JwtAuthenticationDto refresh(@RequestBody @Valid RefreshTokenDto refreshTokenDto) {
    return authServiceImpl.refreshToken(refreshTokenDto);
  }

  @PostMapping("/registration")
  public ResponseEntity<UserResponseDto> createUser(
      @RequestBody @Valid UserCreateDto userCreateDto) {
    return new ResponseEntity<>(userServiceImpl.addUser(userCreateDto), HttpStatus.CREATED);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshTokenDto dtoRT) {
    authServiceImpl.revokeRefreshToken(dtoRT.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
