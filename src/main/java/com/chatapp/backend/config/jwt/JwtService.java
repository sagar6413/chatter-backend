package com.chatapp.backend.config.jwt;

import com.chatapp.backend.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.chatapp.backend.exception.ErrorCode.*;

@Service
@Slf4j
public class JwtService {

    private static final String AUTHORITIES_CLAIM = "authorities";

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long accessTokenExpirationMillis;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMillis;

    public String extractUsername(String token) {
        if (token == null) {
            log.warn("Attempted to extract username from a null token.");
            return null;
        }
        return extractClaim(token, Claims::getSubject);
    }

    public String generateAccessToken(@NotNull UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, @NotNull UserDetails userDetails) {
        log.info("Generating access token for user: {}", userDetails.getUsername());
        return buildToken(extraClaims, userDetails, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(@NotNull UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, @NotNull UserDetails userDetails) {
        log.info("Generating refresh token for user: {}", userDetails.getUsername());
        return buildToken(extraClaims, userDetails, refreshTokenExpirationMillis);
    }

    public void validateToken(String token) {
        validateToken(token, null);
    }

    public void validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            if (isTokenExpired(token)) {
                log.warn("Token validation failed for user: {}. Token is expired.", username);
                throw new ApiException(EXPIRED_TOKEN);
            }
            if (userDetails != null && !username.equals(userDetails.getUsername())) {
                log.warn("Token validation failed for user: {}. Username in token does not match user details.", username);
                throw new ApiException(INVALID_USERNAME, "Username in token does not match provided user details");
            }
            log.info("Token validation successful for user: {}", username);
        } catch (ExpiredJwtException e) {
            log.warn("Token validation failed. Token is expired: {}", e.getMessage());
            throw new ApiException(JWT_EXPIRED);
        } catch (MalformedJwtException e) {
            log.error("Token validation failed. Token is malformed: {}", e.getMessage());
            throw new ApiException(JWT_MALFORMED);
        } catch (SignatureException e) {
            log.error("Token validation failed. Signature is invalid: {}", e.getMessage());
            throw new ApiException(JWT_SIGNATURE_INVALID);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new ApiException(INTERNAL_SERVER_ERROR, "Token validation failed: " + e.getMessage());
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        Instant now = Instant.now();
        Map<String, Object> claims = prepareClaims(extraClaims, userDetails);

        log.info("Building token for user: {}, with authorities: {}, expiration: {}ms", userDetails.getUsername(), claims.get(AUTHORITIES_CLAIM), expiration);

        return Jwts.builder()
                   .claims(claims)
                   .subject(userDetails.getUsername())
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(now.plusMillis(expiration)))
                   .signWith(getSigningKey())
                   .compact();
    }

    private Map<String, Object> prepareClaims(Map<String, Object> extraClaims, UserDetails userDetails) {
        List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put(AUTHORITIES_CLAIM, authorities);
        return claims;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenExpired(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public String validateTokenAndGetUsername(String jwt) {
        try {
            validateToken(jwt);
            return extractUsername(jwt);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new ApiException(INTERNAL_SERVER_ERROR, "Token validation failed: " + e.getMessage());
        }
    }
}