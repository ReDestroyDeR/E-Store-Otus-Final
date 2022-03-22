package ru.red.authenticationservice.configuration;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";
    private static PostgresContainer postgresContainer;

    private PostgresContainer() {
        super(IMAGE_VERSION);
    }

    public static PostgresContainer getInstance() {
        if (postgresContainer == null) {
            postgresContainer = new PostgresContainer()
                    .withDatabaseName("authentication")
                    .withUsername("postgres")
                    .withPassword("postgres");
        }
        return postgresContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("POSTGRES_URL", postgresContainer.getJdbcUrl());
        System.setProperty("POSTGRES_USERNAME", postgresContainer.getUsername());
        System.setProperty("POSTGRES_PASSWORD", postgresContainer.getPassword());
    }

    @Override
    public void stop() {
        super.stop();
    }
}
