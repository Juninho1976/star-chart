package com.example.starchart.security;

import com.example.starchart.domain.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
  private final JwtProps props;

  public JwtService(JwtProps props) {
    this.props = props;
  }

  public String issueToken(UUID userId, String email, Role role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.expMinutes() * 60L);

    var key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .issuer(props.issuer())
        .subject(userId.toString())
        .claim("email", email)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public JwtPrincipal parse(String token) {
    var key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    var claims = Jwts.parser()
        .verifyWith(key)
        .requireIssuer(props.issuer())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    UUID userId = UUID.fromString(claims.getSubject());
    String email = claims.get("email", String.class);
    Role role = Role.valueOf(claims.get("role", String.class));
    return new JwtPrincipal(userId, email, role);
  }
}