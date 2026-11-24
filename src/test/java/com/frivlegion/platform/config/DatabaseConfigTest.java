package com.frivlegion.platform.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @Test
    void parsesAFlyStyleUrlWithPort() {
        DatabaseConfig.ConnectionInfo info =
            DatabaseConfig.parse("postgres://frivlegion:s3cret@frivlegion-db.flycast:5432/frivlegion");
        assertEquals("jdbc:postgresql://frivlegion-db.flycast:5432/frivlegion", info.jdbcUrl());
        assertEquals("frivlegion", info.username());
        assertEquals("s3cret", info.password());
    }

    @Test
    void defaultsThePortAndKeepsSslOptions() {
        DatabaseConfig.ConnectionInfo info =
            DatabaseConfig.parse("postgresql://user:p%40ss@ep-cool-db.eu-central-1.aws.neon.tech/neondb?sslmode=require");
        assertEquals("jdbc:postgresql://ep-cool-db.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require", info.jdbcUrl());
        assertEquals("user", info.username());
        assertEquals("p@ss", info.password());
    }
}
