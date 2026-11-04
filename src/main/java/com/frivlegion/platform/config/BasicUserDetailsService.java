package com.frivlegion.platform.config;

import com.frivlegion.platform.repository.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BasicUserDetailsService implements UserDetailsService {

  final PlayerRepository playerRepository;

  public BasicUserDetailsService(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    return playerRepository.findByUsername(usernameOrEmail)
        .or(() -> playerRepository.findByEmail(usernameOrEmail))
        .orElseThrow(() -> new UsernameNotFoundException("Player not found: " + usernameOrEmail));
  }
}
