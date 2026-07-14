package com.assetmanagement.assetservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${security.jwt.enabled:false}")
    private boolean jwtEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {

                    // Public endpoints
                    auth.requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/v3/api-docs",
                            "/actuator/**"
                    ).permitAll();

                    if (jwtEnabled) {

                        // Only SUPER_ADMIN can create assets
                        auth.requestMatchers(HttpMethod.POST, "/assets")
                                .hasRole("SUPER_ADMIN");

                        auth.requestMatchers(HttpMethod.POST, "/assets/bulk")
                                .hasRole("SUPER_ADMIN");

                        auth.requestMatchers(HttpMethod.POST, "/assets/upload")
                                .hasRole("SUPER_ADMIN");

                        // Only SUPER_ADMIN can modify assets
                        auth.requestMatchers(HttpMethod.PUT, "/assets/**")
                                .hasRole("SUPER_ADMIN");

                        auth.requestMatchers(HttpMethod.PATCH, "/assets/**")
                                .hasRole("SUPER_ADMIN");

                        auth.requestMatchers(HttpMethod.DELETE, "/assets/**")
                                .hasRole("SUPER_ADMIN");

                        // Both SUPER_ADMIN and EMPLOYEE can view assets
                        auth.requestMatchers(HttpMethod.GET, "/assets/**")
                                .hasAnyRole("SUPER_ADMIN", "EMPLOYEE");

                        auth.anyRequest().authenticated();

                    } else {

                        auth.anyRequest().permitAll();

                    }

                })

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}