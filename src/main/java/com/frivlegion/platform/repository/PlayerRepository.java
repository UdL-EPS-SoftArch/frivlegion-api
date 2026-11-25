package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource
public interface PlayerRepository extends CrudRepository<Player, Long>, PagingAndSortingRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    @RestResource(exported = false)
    Optional<Player> findByEmail(String email);

    boolean existsByUsername(String username);

    @RestResource(exported = false)
    boolean existsByEmail(String email);
}
