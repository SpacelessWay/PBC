package com.example.pbc.work_databased;


import com.example.pbc.model.Data_user;
import com.example.pbc.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthRepositoryTest {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private AuthRepository authRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        authRepository = new AuthRepository(jdbcTemplate);

        // Очистка таблиц перед каждым тестом
        clearTables();
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM app_users");
    }

    @Test
    @Order(1)
    void testSaveUserAndFindByLogin() {
        User user = new User(1L, "testuser", "password");

        // Сохраняем пользователя
        authRepository.save(user);

        // Проверяем, что он сохранился
        Optional<User> found = authRepository.findByLogin("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    @Order(2)
    void testSaveDataUserAndGetUuid() {
        // Сохраняем пользователя в app_users
        User user = new User(1L, "datatest", "password");
        authRepository.save(user);

        // Подготавливаем Data_user
        Data_user dataUser = new Data_user("datatest", "password123","uuid123","John", "Doe","john@example.com","1234567890");


        // Сохраняем в users
        authRepository.saveDataUser(dataUser);

        // Получаем uuid
        String uuid = authRepository.getUuid("datatest");
        assertThat(uuid).isEqualTo("uuid123");
    }

    @Test
    @Order(3)
    void testGetUuid_UserNotFound() {
        String uuid = authRepository.getUuid("nonexistentuser");
        assertThat(uuid).isNull();
    }

    @Test
    @Order(4)
    void testFindByLogin_NotFound() {
        Optional<User> user = authRepository.findByLogin("notexists");
        assertThat(user).isEmpty();
    }
}