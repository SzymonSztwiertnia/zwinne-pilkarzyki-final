
package pl.awsb.soccer.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import pl.awsb.soccer.auth.api.request.AuthRefreshTokenRequest;
import pl.awsb.soccer.auth.api.request.AuthRegisterRequest;
import pl.awsb.soccer.auth.api.response.AuthLoginResponse;
import pl.awsb.soccer.auth.api.response.AuthRefreshTokenResponse;
import pl.awsb.soccer.auth.domain.DbRefreshToken;
import pl.awsb.soccer.auth.domain.DbRefreshTokenRepository;
import pl.awsb.soccer.exception.BadRequestException;
import pl.awsb.soccer.exception.ObjectNotFoundException;
import pl.awsb.soccer.user.api.response.UserType;
import pl.awsb.soccer.user.domain.DbUser;
import pl.awsb.soccer.user.domain.DbUserRepository;
import pl.awsb.soccer.util.RequestUtil;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AuthService {
    Key jwtSecretKey;
    Key refreshTokenSecretKey;
    Long jwtExpireTime;
    Long refreshExpireTime;
    DbRefreshTokenRepository refreshTokenRepository;
    DbUserRepository dbUserRepository;
    PasswordEncoder passwordEncoder;

    public AuthService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.secret.refresh}") String refreshTokenSecret,
            @Value("${jwt.expire.time.token}") Long jwtExpireTime,
            @Value("${jwt.expire.time.refresh.token}") Long refreshExpireTime,
            PasswordEncoder passwordEncoder,
            DbRefreshTokenRepository refreshTokenRepository,
            DbUserRepository dbUserRepository) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.refreshTokenSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecret));
        this.jwtExpireTime = jwtExpireTime;
        this.refreshExpireTime = refreshExpireTime;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.dbUserRepository = dbUserRepository;
    }

    public String generateToken(DbUser user, HttpServletRequest request) {
        Map<String, Object> claims = Map.of(
                "role", user.getAccountType(),
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "lastName", user.getLastName(),
                "ip", RequestUtil.getClientIP(request)
        );

        return createToken(claims, user.getEmail(), jwtSecretKey, jwtExpireTime);
    }

    public String generateRefreshToken(DbUser user, String address) {
        String refreshToken = createToken(
                Map.of("ip", address),
                user.getEmail(),
                refreshTokenSecretKey,
                refreshExpireTime
        );

        refreshTokenRepository.deleteAllByUserId(user.getId());

        refreshTokenRepository.save(DbRefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .build()
        );

        return refreshToken;
    }

    private String createToken(Map<String, Object> claims, String subject, Key secretKey, Long expireTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token, jwtSecretKey).getSubject();
    }

    public boolean validateToken(String token, HttpServletRequest request) {
        return validateJwtToken(token, jwtSecretKey, RequestUtil.getClientIP(request));
    }

    public void logout(DbUser user) {
        refreshTokenRepository.deleteAllByUserId(user.getId());
    }

    public AuthRefreshTokenResponse refreshToken(HttpServletRequest httpServletRequest, AuthRefreshTokenRequest request) {
        DbRefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken());

        if (!validateJwtToken(request.refreshToken(), refreshTokenSecretKey, RequestUtil.getClientIP(httpServletRequest))) {
            if (Objects.nonNull(refreshToken)) {
                refreshTokenRepository.delete(refreshToken);
            }

            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        DbUser user = dbUserRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ObjectNotFoundException("Nie odnaleziono użytkownika."));

        if (!Objects.equals(user.getEmail(), request.email())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        String accessToken = generateToken(user, httpServletRequest);

        return AuthRefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private boolean validateJwtToken(String token, Key secretKey, String requestIp) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseSignedClaims(token);

            String tokenIp = claimsJws.getPayload().get("ip", String.class);

            return requestIp == null || requestIp.equals(tokenIp);
        } catch (JwtException ignored) {
        }

        return false;
    }

    private Claims extractClaims(String token, Key secretKey) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthLoginResponse register(HttpServletRequest httpServletRequest, AuthRegisterRequest request) {
        if (dbUserRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Coś poszło nie tak..");
        }

        DbUser user = DbUser.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .lastName(request.lastName())
                .accountType(UserType.USER)
                .build();

        dbUserRepository.save(user);

        return AuthLoginResponse.builder()
                .accessToken(generateToken(user, httpServletRequest))
                .refreshToken(generateRefreshToken(user, RequestUtil.getClientIP(httpServletRequest)))
                .build();
    }
}
