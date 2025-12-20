package com.project.event_ticket_platform.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

	@Value("${jwt.secret:your-256-bit-secret-key-that-should-be-changed-in-production-environment-please-use-a-strong-secret-key}")
	private String secret;

	@Value("${jwt.expiration-hours:24}")
	private long expirationHours;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UUID userId, String email, String role) {
		Instant now = Instant.now();
		Instant expiration = now.plus(expirationHours, ChronoUnit.HOURS);

		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.claim("role", role)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(getSigningKey())
				.compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public UUID extractUserId(String token) {
		Claims claims = parseToken(token);
		return UUID.fromString(claims.getSubject());
	}

	public String extractRole(String token) {
		Claims claims = parseToken(token);
		return claims.get("role", String.class);
	}

	public boolean isTokenValid(String token) {
		try {
			parseToken(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
