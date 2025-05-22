package pl.awsb.soccer.auth.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.awsb.soccer.auth.AuthService;
import pl.awsb.soccer.auth.api.request.AuthLoginRequest;
import pl.awsb.soccer.auth.api.request.AuthRefreshTokenRequest;
import pl.awsb.soccer.auth.api.request.AuthRegisterRequest;
import pl.awsb.soccer.auth.api.response.AuthLoginResponse;
import pl.awsb.soccer.auth.api.response.AuthRefreshTokenResponse;
import pl.awsb.soccer.exception.AccessDeniedException;
import pl.awsb.soccer.security.CustomUserDetails;
import pl.awsb.soccer.security.LoggedUser;
import pl.awsb.soccer.user.domain.DbUser;
import pl.awsb.soccer.util.RequestUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AuthController {
    AuthService authService;
    AuthenticationManager authenticationManager;
    Cache<String, Long> blockedUsersCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @PostMapping("/login")
    public AuthLoginResponse login(
            HttpServletRequest request,
            @RequestBody AuthLoginRequest authLoginRequest) {
        Long attempts = blockedUsersCache.getIfPresent(authLoginRequest.email());

        if (Objects.nonNull(attempts) && attempts >= 5) {
            throw new AccessDeniedException("Zbyt duża ilośc błędnego logowania, spróbuj ponownie za 10 minut.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authLoginRequest.email(),
                            authLoginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            DbUser user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

            String token = authService.generateToken(user, request);
            String refreshToken = authService.generateRefreshToken(user, RequestUtil.getClientIP(request));

            return AuthLoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthenticationException e) {
            Long ifPresent = blockedUsersCache.getIfPresent(authLoginRequest.email());
            blockedUsersCache.put(authLoginRequest.email(), Objects.nonNull(ifPresent) ? ifPresent + 1 : 1);

            throw new AccessDeniedException("Can't authorize..");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@LoggedUser CustomUserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        authService.logout(userDetails.getUser());

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public AuthLoginResponse register(HttpServletRequest request, @Valid @RequestBody AuthRegisterRequest authRegisterRequest) {
        return authService.register(request, authRegisterRequest);
    }

    @PostMapping("/refresh")
    public AuthRefreshTokenResponse refreshUserToken(@RequestBody AuthRefreshTokenRequest authRefreshTokenRequest,
                                                     HttpServletRequest request
    ) {
        return authService.refreshToken(request, authRefreshTokenRequest);
    }
}
