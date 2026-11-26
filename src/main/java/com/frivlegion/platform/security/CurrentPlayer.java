package com.frivlegion.platform.security;

import com.frivlegion.platform.domain.Player;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentPlayer {

    public Player require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Player player)) {
            throw new AccessDeniedException("Authentication required");
        }
        return player;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public void requireOwner(Player owner) {
        Player current = require();
        if (isAdmin()) {
            return;
        }
        if (owner == null) {
            throw new AccessDeniedException("Only the owner can modify this resource");
        }
        if (current.getId() != null && owner.getId() != null) {
            if (current.getId().equals(owner.getId())) {
                return;
            }
        } else if (current.getUsername() != null && current.getUsername().equals(owner.getUsername())) {
            return;
        }
        throw new AccessDeniedException("Only the owner can modify this resource");
    }

    public void requireSelfOrAdmin(Player target) {
        requireOwner(target);
    }
}
