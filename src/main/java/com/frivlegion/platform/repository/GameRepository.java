package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource
public interface GameRepository extends CrudRepository<Game, String>, PagingAndSortingRepository<Game, String> {

    List<Game> findByIsActiveTrue();

    List<Game> findByTitleContainingIgnoreCase(String title);

    List<Game> findByCategoryIgnoreCase(String category);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends Game> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteById(String id);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(Game entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends Game> entities);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAllById(Iterable<? extends String> ids);
}
