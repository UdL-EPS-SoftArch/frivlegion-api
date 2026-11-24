package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Score;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface ScoreRepository extends CrudRepository<Score, Long>, PagingAndSortingRepository<Score, Long> {

    List<Score> findByGameIdOrderByScoreValueDesc(String gameId);

    @RestResource(exported = false)
    List<Score> findByGameId(String gameId);

    List<Score> findByPlayerId(Long playerId);

    @RestResource(exported = false)
    List<Score> findByPlayerIdAndGameId(Long playerId, String gameId);

    Optional<Score> findFirstByPlayerIdAndGameIdOrderByScoreValueDesc(Long playerId, String gameId);
}
