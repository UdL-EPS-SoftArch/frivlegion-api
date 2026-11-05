# Domain Models (Spring Boot - JPA Entities)

This document defines the core domain models for the Minigames Platform. 
Framework: Java Spring Boot with Spring Data JPA.

## 1. Player (User)
Represents a registered user in the platform.
*   **id**: `Long` (Primary Key, Auto-generated)
*   **username**: `String` (Unique, not null)
*   **email**: `String` (Unique, not null)
*   **passwordHash**: `String` (Not null)
*   **createdAt**: `LocalDateTime` (Auto-assigned on creation)

## 2. Game (Minigame)
Represents a playable game in the catalog.
*   **id**: `String` (Primary Key, e.g., "mc-prominence", "pkmn-tcg")
*   **title**: `String` (Not null)
*   **description**: `String` (Text area)
*   **thumbnailUrl**: `String`
*   **category**: `String`
*   **isActive**: `Boolean` (Default to true)

## 3. Score
Represents a high score submitted by a player for a specific game.
*   **id**: `Long` (Primary Key, Auto-generated)
*   **player**: `Player` (Many-to-One relationship)
*   **game**: `Game` (Many-to-One relationship)
*   **scoreValue**: `Integer` (Not null)
*   **achievedAt**: `LocalDateTime` (Auto-assigned on creation)

## 4. GameState (Progress)
Represents the saved state (progress) of a player in a specific game.
*   **id**: `Long` (Primary Key, Auto-generated)
*   **player**: `Player` (Many-to-One relationship)
*   **game**: `Game` (Many-to-One relationship)
*   **payload**: `String` (Stores JSON string with game state data)
*   **updatedAt**: `LocalDateTime` (Auto-updates on change)