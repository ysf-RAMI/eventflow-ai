package com.eventflow.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway security.
 *
 * <p>With {@code eventflow.security.jwt-enabled=false} (local default) every public route is open,
 * so the team can test without Keycloak. With {@code true}, requests need a valid Keycloak JWT,
 * except catalog reads and health checks. {@code /internal/**} is never reachable through the gateway.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${eventflow.security.jwt-enabled:false}") boolean jwtEnabled) {

        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        if (!jwtEnabled) {
            return http.authorizeExchange(ex -> ex
                            .pathMatchers("/internal/**").denyAll()
                            .anyExchange().permitAll())
                    .build();
        }

        return http.authorizeExchange(ex -> ex
                        .pathMatchers("/internal/**").denyAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/events/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(new KeycloakRoleConverter())))
                .build();
    }
}
