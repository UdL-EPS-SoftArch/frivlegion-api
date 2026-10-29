package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.GameState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface GameStateRepository extends CrudRepository<GameState, Long>, PagingAndSortingRepository<GameState, Long> {

    String OWNERSHIP_CLAUSE =
        "s.player.username = ?#{authentication.name} " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT s FROM GameState s WHERE " + OWNERSHIP_CLAUSE)
    Page<GameState> findAll(Pageable pageable);

    @Override
    @Query("SELECT s FROM GameState s WHERE s.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<GameState> findById(@Param("id") Long id);

    @RestResource(exported = false)
    List<GameState> findByPlayerIdAndGameIdOrderByUpdatedAtDesc(Long playerId, String gameId);

    @RestResource(exported = false)
    Optional<GameState> findFirstByPlayerIdAndGameIdOrderByUpdatedAtDesc(Long playerId, String gameId);

    @RestResource(exported = false)
    List<GameState> findByPlayerIdAndGameId(Long playerId, String gameId);
}
