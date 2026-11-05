package com.frivlegion.platform.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import com.frivlegion.platform.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PlayerRepository playerRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PlayerRepository playerRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.playerRepository = playerRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            Player player = (Player) authentication.getPrincipal();
            return ResponseEntity.ok(responseFor(player));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (playerRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username is already in use"));
        }
        if (playerRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email is already in use"));
        }

        Player player = new Player();
        player.setUsername(request.username());
        player.setEmail(request.email());
        player.setRawPassword(request.password());
        player.encodePassword();
        player.setRawPassword(null);

        try {
            player = playerRepository.save(player);
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username or email is already in use"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFor(player));
    }

    private AuthResponse responseFor(Player player) {
        return new AuthResponse(
                jwtService.generateToken(player),
                "Bearer",
                jwtService.getExpirationSeconds(),
                player.getId(),
                player.getUsername(),
                player.getEmail());
    }

    public record LoginRequest(
            @JsonAlias("email") @NotBlank String username,
            @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 256) String password) {
    }

    public record AuthResponse(
            String token,
            String tokenType,
            long expiresIn,
            Long playerId,
            String username,
            String email) {
    }
}
