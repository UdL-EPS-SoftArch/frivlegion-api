package com.frivlegion.platform.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Game extends UriEntity<String> {

    @Id
    @NotBlank
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Length(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

    /** URL loaded in the platform's iframe to play the game (#11). */
    private String gameUrl;

    private String category;

    @Column(nullable = false)
    private Boolean isActive = true;
}
