package org.warm4ik.ims.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.warm4ik.ims.dto.security.JwtAuthenticationDto;
import org.warm4ik.ims.dto.security.RefreshTokenDto;
import org.warm4ik.ims.dto.security.UserCredentialDto;
import org.warm4ik.ims.exception.custom.CustomAuthException;
import org.warm4ik.ims.service.AuthService;
import org.warm4ik.ims.entity.User;
import org.warm4ik.ims.security.jwt.JwtService;
import org.warm4ik.ims.security.user.CustomUserDetails;
import org.warm4ik.ims.service.UserService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userServiceImpl;
  private final JwtService jwtServiceImpl;
  private final PasswordEncoder passwordEncoder;

  @Override
  public JwtAuthenticationDto signIn(UserCredentialDto userCredentialDto) {

    User user = findByCredentials(userCredentialDto);
    CustomUserDetails userDetails = new CustomUserDetails(user);

    return jwtServiceImpl.generateAuthToken(userDetails);
  }

  @Override
  public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) {
    String refreshToken = refreshTokenDto.refreshToken();

    if (refreshToken != null && jwtServiceImpl.validateJwtToken(refreshToken)) {

      String email = jwtServiceImpl.getEmailFromToken(refreshToken);

      User user = userServiceImpl.findByEmail(email);
      CustomUserDetails userDetails = new CustomUserDetails(user);

      return jwtServiceImpl.refreshBaseToken(userDetails, refreshToken);
    }
    throw new CustomAuthException("Invalid refresh token");
  }

  private User findByCredentials(UserCredentialDto dto) {
    User user = userServiceImpl.findByEmail(dto.email());

    if (passwordEncoder.matches(dto.password(), user.getPassword())) {
      return user;
    }
    throw new CustomAuthException("Почта или пароль некорректны.");
  }

  public void revokeRefreshToken(String refreshToken) {
    jwtServiceImpl.revokeRefreshToken(refreshToken);
  }
}
