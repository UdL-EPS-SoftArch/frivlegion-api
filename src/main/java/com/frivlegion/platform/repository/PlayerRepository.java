package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface PlayerRepository extends CrudRepository<Player, Long>, PagingAndSortingRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    boolean existsByUsername(String username);
}
