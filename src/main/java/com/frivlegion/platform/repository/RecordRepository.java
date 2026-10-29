package com.frivlegion.platform.repository;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface RecordRepository extends CrudRepository<Record, Long>, PagingAndSortingRepository<Record, Long> {

    String OWNERSHIP_CLAUSE =
        "r.ownedBy.username = ?#{authentication.name} " +
        "OR r.status = 'PUBLIC' " +
        "OR ?#{hasRole('ADMIN')} = true";

    @Override
    @Query("SELECT r FROM Record r WHERE " + OWNERSHIP_CLAUSE)
    Page<Record> findAll(Pageable pageable);
    @Override
    @Query("SELECT r FROM Record r WHERE r.id = :id AND (" + OWNERSHIP_CLAUSE + ")")
    Optional<Record> findById(@Param("id") Long id);

    @Query("SELECT r FROM Record r WHERE r.ownedBy = :user AND (" + OWNERSHIP_CLAUSE + ")")
    Page<Record> findByOwnedBy(@Param("user") Player owner, Pageable pageable);

    List<Record> findByName(String name);
}
