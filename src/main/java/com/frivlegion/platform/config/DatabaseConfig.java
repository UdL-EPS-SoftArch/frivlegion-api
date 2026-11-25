package com.frivlegion.platform.config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Uses PostgreSQL whenever DATABASE_URL is set (e.g. as a Fly.io secret);
 * without it the application keeps the embedded H2 database.
 * Accepts the usual provider format: postgres://user:password@host[:port]/database[?sslmode=require]
 */
@Configuration
@ConditionalOnExpression("'${DATABASE_URL:}'.length() > 0")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        ConnectionInfo info = parse(databaseUrl);
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(info.jdbcUrl());
        dataSource.setUsername(info.username());
        dataSource.setPassword(info.password());
        return dataSource;
    }

    static ConnectionInfo parse(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String[] userInfo = uri.getRawUserInfo().split(":", 2);
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        return new ConnectionInfo(
            "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath() + query,
            URLDecoder.decode(userInfo[0], StandardCharsets.UTF_8),
            userInfo.length > 1 ? URLDecoder.decode(userInfo[1], StandardCharsets.UTF_8) : "");
    }

    record ConnectionInfo(String jdbcUrl, String username, String password) {
    }
}
