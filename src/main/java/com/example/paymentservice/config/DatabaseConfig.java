package com.example.paymentservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Database configuration that handles DATABASE_URL format from cloud platforms.
 * Parses postgresql://user:pass@host:port/dbname format.
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${POSTGRES_HOST:localhost}")
    private String postgresHost;

    @Value("${POSTGRES_PORT:5432}")
    private String postgresPort;

    @Value("${POSTGRES_DB:mydb}")
    private String postgresDb;

    @Value("${POSTGRES_USER:myuser}")
    private String postgresUser;

    @Value("${POSTGRES_PASSWORD:mypassword}")
    private String postgresPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.driverClassName("org.postgresql.Driver");

        // If DATABASE_URL is provided, parse it
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            try {
                URI dbUri = new URI(databaseUrl.replace("postgresql://", "postgres://"));
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                String dbName = dbUri.getPath().replaceFirst("/", "");

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                
                log.info("Using DATABASE_URL for database connection: {}@{}:{}/{}", username, host, port, dbName);
                
                builder.url(jdbcUrl);
                builder.username(username);
                builder.password(password);
            } catch (Exception e) {
                log.error("Failed to parse DATABASE_URL: {}", databaseUrl, e);
                log.warn("Falling back to individual environment variables");
                // Fall through to use individual variables
                builder.url(String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresDb));
                builder.username(postgresUser);
                builder.password(postgresPassword);
            }
        } else {
            // Use individual environment variables
            log.info("Using individual environment variables for database connection");
            builder.url(String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresDb));
            builder.username(postgresUser);
            builder.password(postgresPassword);
        }

        return builder.build();
    }
}

