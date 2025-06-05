package org.web.codefm.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.web.codefm.domain.service.SessionUserService;

import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Oauth2SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = token -> {
            String tokenIssuer = token.getIssuer().toString();
            String expectedIssuer = issuerUri;

            tokenIssuer = normalizeUrl(tokenIssuer);
            expectedIssuer = normalizeUrl(expectedIssuer);

            if (!tokenIssuer.equals(expectedIssuer)) {
                log.error("Issuer mismatch. Expected: {}, Found: {}", expectedIssuer, tokenIssuer);
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer", "The issuer is invalid", null));
            }
            return OAuth2TokenValidatorResult.success();
        };

        jwtDecoder.setJwtValidator(withIssuer);
        return jwtDecoder;
    }

    private String normalizeUrl(String url) {
        return url.replaceAll("/$", "").replace("https://", "http://");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthConverter, SessionUserService sessionUserService) throws Exception {

        http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthConverter)))
                .addFilterBefore(new CookieAuthenticationFilter(jwtDecoder, jwtAuthConverter),
                        BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                            throws ServletException, IOException {
                        sessionUserService.setSessionUser();
                        filterChain.doFilter(request, response);
                    }
                }, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Component
    public static class CookieAuthenticationFilter extends OncePerRequestFilter {
        private final JwtDecoder jwtDecoder;
        private final JwtAuthenticationConverter jwtAuthConverter;

        public CookieAuthenticationFilter(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthConverter) {
            this.jwtDecoder = jwtDecoder;
            this.jwtAuthConverter = jwtAuthConverter;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            try {
                Cookie[] cookies = request.getCookies();
                String token = null;

                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("access_token".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
                    }
                }

                if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Jwt jwt = jwtDecoder.decode(token);
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthConverter.convert(jwt));
                }
            } catch (Exception e) {
                log.info("Error al procesar el token JWT", e);
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Set<GrantedAuthority> authorities = new HashSet<>();

            Optional.ofNullable(jwt.getClaim("realm_access"))
                    .map(realmAccess -> ((Map<?, ?>) realmAccess).get("roles"))
                    .filter(Objects::nonNull)
                    .map(roles -> (List<?>) roles)
                    .ifPresent(roles -> roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.toString()))));

            Optional.ofNullable(jwt.getClaim("resource_access"))
                    .map(resourceAccess -> ((Map<?, ?>) resourceAccess).get("codefm"))
                    .filter(Objects::nonNull)
                    .map(codefm -> ((Map<?, ?>) codefm).get("roles"))
                    .filter(Objects::nonNull)
                    .map(roles -> (List<?>) roles)
                    .ifPresent(roles -> roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role))));

            return authorities;
        });
        return jwtConverter;
    }

}