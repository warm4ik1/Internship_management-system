package org.warm4ik.ims.security.jwt;

import io.jsonwebtoken.Claims;
import org.warm4ik.ims.dto.security.JwtAuthenticationDto;
import org.warm4ik.ims.security.user.CustomUserDetails;

public interface JwtService {
  JwtAuthenticationDto generateAuthToken(CustomUserDetails userDetails);

  JwtAuthenticationDto refreshBaseToken(CustomUserDetails userDetails, String refreshToken);

  boolean validateJwtToken(String token);

  void revokeRefreshToken(String refreshToken);

  Claims extractAllClaims(String token);

  String getEmailFromToken(String token);
}
