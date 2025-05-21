package pl.awsb.soccer.security;

import com.google.common.base.Strings;
import jakarta.servlet.DispatcherType;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.awsb.soccer.auth.AuthService;
import pl.awsb.soccer.user.api.response.UserType;
import pl.awsb.soccer.user.domain.DbUserRepository;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SecurityConfig {
    DbUserRepository repository;
    UserDetailsService userDetailsService;
    String swaggerKey;
    PasswordEncoder passwordEncoder;
    AuthService authService;

    public SecurityConfig(DbUserRepository repository,
                          UserDetailsService userDetailsService,
                          AuthService authService,
                          PasswordEncoder passwordEncoder,
                          @Value("${swagger.key}") String swaggerKey) {
        this.repository = repository;
        this.userDetailsService = userDetailsService;
        this.swaggerKey = swaggerKey;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(repository);
    }

    String[] swaggerUris = new String[]{
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/actuator/**"
    };

    String[] allowedUris = new String[]{
            "/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> swaggerAuthorizationContext = (request, context) -> {
            String key = context.getRequest().getHeader("X-API-KEY");

            return new AuthorizationDecision(!Strings.isNullOrEmpty(key) && swaggerKey.equals(key));
        };

        http    
                .cors()
                .and()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                       // .requestMatchers(swaggerUris).access(swaggerAuthorizationContext)
                        .requestMatchers(allowedUris).permitAll()
                        .requestMatchers(swaggerUris).permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/admin/**").hasAuthority(UserType.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(authService, userDetailsService);
    }
}