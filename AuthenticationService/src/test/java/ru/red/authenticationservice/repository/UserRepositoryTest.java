package ru.red.authenticationservice.repository;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.red.authenticationservice.configuration.PostgresContainer;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@Transactional
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTest {
    @ClassRule
    @Container
    public final static PostgreSQLContainer<?> postgresSQLContainer = PostgresContainer.getInstance();

    @BeforeAll
    public static void startContainers() {
        postgresSQLContainer.start();
    }

    @Autowired
    Environment environment;

    @Autowired
    UserRepository service;

    private UsersRecord createTestUsersRecord() {
        UsersRecord record = new UsersRecord();
        record.setEmail("test@example.com");
        record.setSalt("Test salt");
        record.setPassword("Test password");
        return record;
    }

    private Long createTestUserGetId(UsersRecord record) {
        Long id = service.createUser(record).map(UsersRecord::getId).block();
        return id;
    }

    @Test
    void createUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        record.setId(id);
        assertEquals(record, service.getUser(id).block());
    }

    @Test
    void deleteUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        assertEquals(1, service.deleteUser(id).block());
    }

    @Test
    void updateUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        String text = "New salt";
        record.setSalt(text);
        service.updateUser(id, record).block();
        assertEquals(text, service.getUser(id).block().getSalt());
    }
}