package org.warm4ik.ims.service;

import org.warm4ik.ims.dto.security.JwtAuthenticationDto;
import org.warm4ik.ims.dto.security.RefreshTokenDto;
import org.warm4ik.ims.dto.security.UserCredentialDto;

public interface AuthService {

  JwtAuthenticationDto signIn(UserCredentialDto userCredentialDto);

  JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto);

  void revokeRefreshToken(String refreshToken);
}
