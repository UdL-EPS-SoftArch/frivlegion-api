package com.frivlegion.platform.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Retained user-authored note resource (PRIVATE by default, optionally PUBLIC).
 * It is not a minigame artifact: the game domain uses Game, Score and GameState.
 * Kept because it is part of the existing public API and BDD contract (issue #33).
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class Record extends UriEntity<Long> {

    public enum Status {
        PUBLIC, PRIVATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Length(max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PRIVATE;

    @CreatedDate
    @DateTimeFormat
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant created;

    @LastModifiedDate
    @DateTimeFormat
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant modified;

    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    private Player ownedBy;


}
