package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface GameRepository extends CrudRepository<Game, String>, PagingAndSortingRepository<Game, String> {

    List<Game> findByIsActiveTrue();

    List<Game> findByTitleContainingIgnoreCase(String title);

    List<Game> findByCategoryIgnoreCase(String category);
}
