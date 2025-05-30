package org.warm4ik.ims.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtServiceImpl;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
      throws ServletException, IOException {

    String token = getTokenFromRequest(req);
    if (token != null && jwtServiceImpl.validateJwtToken(token)) {

      Claims claims = jwtServiceImpl.extractAllClaims(token);

      String email = claims.getSubject();

      // всегда даём ROLE_USER
      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

      // если admin=true — ещё и ROLE_ADMIN
      Boolean isAdmin = claims.get("admin", Boolean.class);
      if (Boolean.TRUE.equals(isAdmin)) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      }

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(email, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(req, res);
  }

  private String getTokenFromRequest(HttpServletRequest req) {
    String bearer = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearer != null && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return null;
  }
}
