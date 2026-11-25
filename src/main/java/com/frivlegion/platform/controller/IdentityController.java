package com.frivlegion.platform.controller;

import com.frivlegion.platform.domain.Player;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The authenticated player's own, complete profile. It is the only REST resource that
 * includes the player's email: /players/** only returns the public representation (#25).
 */
@BasePathAwareController
public class IdentityController {

  private final RepositoryEntityLinks entityLinks;

  public IdentityController(RepositoryEntityLinks entityLinks) {
    this.entityLinks = entityLinks;
  }

  @GetMapping("/identity")
  public @ResponseBody EntityModel<Identity> getAuthenticatedUserIdentity() {
    Player player = (Player) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Identity identity = new Identity(
        player.getId(),
        player.getUri(),
        player.getUsername(),
        player.getEmail(),
        player.getCreatedAt(),
        player.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

    return EntityModel.of(identity,
        entityLinks.linkToItemResource(Player.class, player.getId()).withSelfRel(),
        entityLinks.linkToItemResource(Player.class, player.getId()).withRel("player"));
  }

  public record Identity(
      Long id,
      String uri,
      String username,
      String email,
      LocalDateTime createdAt,
      List<String> roles) {
  }
}
