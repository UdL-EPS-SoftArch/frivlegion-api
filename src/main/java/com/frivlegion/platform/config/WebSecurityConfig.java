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
        // Deny by default (#40): only the routes listed here are public; everything else needs a JWT.
        http.authorizeHttpRequests((auth) -> auth
                // Authentication: registering is only for visitors without a session.
                .requestMatchers(HttpMethod.POST, "/auth/register").anonymous()
                .requestMatchers("/auth/**").permitAll()
                // Public API root, HAL Explorer, ALPS metadata and error pages.
                .requestMatchers(HttpMethod.GET, "/", "/explorer/**", "/profile/**").permitAll()
                .requestMatchers("/error").permitAll()
                // Catalog and leaderboards are public; writing the catalog is for admins (#17).
                .requestMatchers(HttpMethod.GET, "/games", "/games/**", "/scores", "/scores/**").permitAll()
                .requestMatchers("/games", "/games/**").hasRole("ADMIN")
                // Players: registration only through /auth/register (#40), public profiles
                // without email (#25), self-service PATCH/DELETE checked by PlayerEventHandler (#29).
                .requestMatchers(HttpMethod.POST, "/players").permitAll() // answered 405 by PlayerEventHandler
                .requestMatchers(HttpMethod.GET, "/players").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/players/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/players/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/players/*").authenticated()
                .requestMatchers("/players", "/players/**").denyAll()
                // Template records: public ones can be read without logging in.
                .requestMatchers(HttpMethod.GET, "/records", "/records/*").permitAll()
                .anyRequest().authenticated())
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
