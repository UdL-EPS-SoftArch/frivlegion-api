package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RepositoryRestResource
public interface PlayerRepository extends CrudRepository<Player, Long>, PagingAndSortingRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    @RestResource(exported = false)
    Optional<Player> findByEmail(String email);

    boolean existsByUsername(String username);

    @RestResource(exported = false)
    boolean existsByEmail(String email);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    Iterable<Player> findAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    Page<Player> findAll(Pageable pageable);
}
