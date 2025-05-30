package org.warm4ik.ims.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.warm4ik.ims.dto.security.JwtAuthenticationDto;
import org.warm4ik.ims.security.user.CustomUserDetails;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Сервис генерации, валидации и парсинга JWT-токенов. */
@Component
public class JwtServiceImpl implements JwtService {

  private static final Logger LOGGER = LogManager.getLogger(JwtServiceImpl.class);
  private final ConcurrentHashMap<String, Date> revokedRefreshTokens = new ConcurrentHashMap<>();

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  public JwtAuthenticationDto generateAuthToken(CustomUserDetails userDetails) {
    String access = generateJwtToken(userDetails);
    String refresh = generateRefreshJwtToken(userDetails);
    return new JwtAuthenticationDto(access, refresh);
  }

  @Override
  public JwtAuthenticationDto refreshBaseToken(CustomUserDetails userDetails, String refreshToken) {
    // На основе userDetails пересоздаем access, а refresh оставляем старый
    String access = generateJwtToken(userDetails);
    return new JwtAuthenticationDto(access, refreshToken);
  }

  /** Извлекает email (subject) из переданного JWT-токена. */
  public String getEmailFromToken(String token) {
    return extractAllClaims(token).getSubject();
  }

  @Override
  public boolean validateJwtToken(String token) {
    try {
      Claims claims = extractAllClaims(token);
      // если передан refresh‑токен с jti в чёрном списке — запрещаем
      String jti = claims.getId();
      if (jti != null && revokedRefreshTokens.containsKey(jti)) {
        LOGGER.warn("Rejected revoked refresh token: {}", jti);
        return false;
      }
      return true;
    } catch (ExpiredJwtException e) {
      LOGGER.error("Expired jwt exception", e);
    } catch (UnsupportedJwtException e) {
      LOGGER.error("Unsupported jwt exception", e);
    } catch (MalformedJwtException e) {
      LOGGER.error("Malformed jwt exception", e);
    } catch (SecurityException e) {
      LOGGER.error("Security exception", e);
    } catch (Exception e) {
      LOGGER.error("Invalid token", e);
    }
    return false;
  }

  private String generateJwtToken(CustomUserDetails userDetails) {

    Date issued = new Date();
    Date expires =
        Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());

    boolean isAdmin =
        userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    return Jwts.builder()
        .claim(Claims.SUBJECT, userDetails.getUsername())
        .claim("admin", isAdmin)
        .claim(Claims.ISSUED_AT, issued)
        .claim(Claims.EXPIRATION, expires)
        .signWith(getSignKey())
        .compact();
  }

  private String generateRefreshJwtToken(CustomUserDetails userDetails) {
    Date issued = new Date();
    Date expires =
        Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());

    String jti = UUID.randomUUID().toString();

    return Jwts.builder()
        .claim(Claims.ID, jti)
        .claim(Claims.SUBJECT, userDetails.getUsername())
        .claim(Claims.ISSUED_AT, issued)
        .claim(Claims.EXPIRATION, expires)
        .signWith(getSignKey())
        .compact();
  }

  /**
   * Декодирует BASE64-строку секрета и создает SecretKey для подписи/верификации токенов алгоритмом
   * HMAC-SHA.
   *
   * @return SecretKey, пригодный для использования в JJWT
   */
  private SecretKey getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /** Разбирает и проверяет JWT, возвращает body (Claims). */
  public Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
  }

  /** При logout вызываете этот метод */
  @Override
  public void revokeRefreshToken(String refreshToken) {
    Claims claims = extractAllClaims(refreshToken);
    String jti = claims.getId();
    Date exp = claims.getExpiration();
    if (jti != null) {
      revokedRefreshTokens.put(jti, exp);
    }
  }

  /** метод для очистки уже истёкших записей */
  @Scheduled(fixedDelay = 60_000) // 1 min
  public void cleanUpRevoked() {
    Date now = new Date();
    revokedRefreshTokens.entrySet().removeIf(e -> e.getValue().before(now));
  }
}
