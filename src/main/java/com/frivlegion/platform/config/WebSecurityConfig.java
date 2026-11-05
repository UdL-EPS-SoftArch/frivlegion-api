package com.frivlegion.platform.config;

import java.util.Arrays;
import java.util.List;

import com.frivlegion.platform.security.JwtAuthenticationFilter;
import com.frivlegion.platform.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Value("${allowed-origins}")
    String[] allowedOrigins;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/games", "/games/**", "/scores", "/scores/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/scores").authenticated()
                .requestMatchers("/gameStates", "/gameStates/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/players", "/players/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/players").anonymous()
                .requestMatchers(HttpMethod.POST, "/players/**").denyAll()
                .requestMatchers(HttpMethod.PUT, "/players", "/players/**").denyAll()
                .requestMatchers(HttpMethod.PATCH, "/players", "/players/**").denyAll()
                .requestMatchers(HttpMethod.DELETE, "/players", "/players/**").denyAll()
                .requestMatchers(HttpMethod.POST, "/games", "/games/**").denyAll()
                .requestMatchers(HttpMethod.PUT, "/games", "/games/**").denyAll()
                .requestMatchers(HttpMethod.PATCH, "/games", "/games/**").denyAll()
                .requestMatchers(HttpMethod.DELETE, "/games", "/games/**").denyAll()
                .requestMatchers(HttpMethod.GET, "/identity").authenticated()
                .requestMatchers(HttpMethod.GET, "/records").permitAll()    // Allow listing public records
                .requestMatchers(HttpMethod.GET, "/records/*").permitAll()  // Allow retrieving public records
                .requestMatchers(HttpMethod.GET, "/*/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/*/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/*/*").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/*/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/*/*").authenticated()
                .anyRequest().permitAll())
            .csrf((csrf) -> csrf.disable())
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling((exceptions) -> exceptions
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
