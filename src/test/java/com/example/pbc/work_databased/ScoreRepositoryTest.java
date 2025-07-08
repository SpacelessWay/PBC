package com.example.pbc.work_databased;

import com.example.pbc.exception.BadRequestException;
import com.example.pbc.exception.NotFoundException;
import com.example.pbc.model.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ScoreRepository scoreRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scoreRepository = new ScoreRepository(jdbcTemplate);
    }

    @Test
    void createScore_successfully_inserts_score() {
        Score score = new Score(1L, 100L, "ACC123456", 1000L, true);

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertDoesNotThrow(() -> scoreRepository.createScore(score));
        verify(jdbcTemplate, times(1)).update(anyString(), anyLong(), anyString(), anyLong(), anyBoolean());
    }

    @Test
    void createScore_throws_BadRequestException_when_user_id_is_invalid() {
        Score score = new Score(null, null, "ACC123456", 1000L, true);
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.createScore(score)
        );
        assertEquals("ID пользователя обязателен", thrown.getMessage());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void createScore_throws_RuntimeException_on_db_error() {
        Score score = new Score(null, 100L, "ACC123456", 1000L, true);
        doThrow(new DataAccessException("DB Error") {})
                .when(jdbcTemplate)
                .update(anyString(), any(Object[].class));
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> scoreRepository.createScore(score)
        );
        assertTrue(thrown.getMessage().contains("Не удалось сохранить счёт в базе данных"));
    }

    @Test
    void getScoresByUserId_returns_scores_for_valid_user_id() {
        Long userId = 100L;
        List<Score> scores = List.of(new Score(1L, userId, "ACC123456", 1000L, true));

        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(userId)
        )).thenReturn(scores);

        List<Score> result = scoreRepository.getScoresByUserId(userId);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(userId));
    }

    @Test
    void getScoresByUserId_throws_NotFoundException_when_no_scores_found() {
        Long userId = 100L;
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(userId))).thenReturn(Collections.emptyList());
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> scoreRepository.getScoresByUserId(userId)
        );
        assertEquals("Счета не найдены для пользователя", thrown.getMessage());
    }

    @Test
    void getScoresByUserId_throws_BadRequestException_when_invalid_user_id() {
        Long userId = -1L;
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.getScoresByUserId(userId)
        );
        assertEquals("ID пользователя обязателен", thrown.getMessage());
        verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), anyLong());
    }

    @Test
    void closeScore_successfully_closes_score() {
        String scoreNumber = "ACC123456";
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1); // 1 строка изменена
        assertDoesNotThrow(() -> scoreRepository.closeScore(scoreNumber));
        verify(jdbcTemplate, times(1)).update(anyString(), eq(scoreNumber));
    }

    @Test
    void closeScore_throws_NotFoundException_when_score_not_found() {
        String scoreNumber = "ACC123456";
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(0); // 0 строк изменено
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> scoreRepository.closeScore(scoreNumber)
        );
        assertEquals("Счёт не найден", thrown.getMessage());
    }

    @Test
    void closeScore_throws_BadRequestException_when_score_number_is_empty() {
        String scoreNumber = "";
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.closeScore(scoreNumber)
        );
        assertEquals("Номер счёта обязателен", thrown.getMessage());
        verify(jdbcTemplate, never()).update(anyString(), anyString());
    }

    @Test
    void scoreExists_returns_true_when_score_exists() {
        String scoreNumber = "ACC123456";
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(scoreNumber))).thenReturn(1);
        boolean exists = scoreRepository.scoreExists(scoreNumber);
        assertTrue(exists);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(scoreNumber));
    }

    @Test
    void scoreExists_returns_false_when_score_does_not_exist() {
        String scoreNumber = "ACC123456";
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(scoreNumber))).thenReturn(0);
        boolean exists = scoreRepository.scoreExists(scoreNumber);
        assertFalse(exists);
    }

    @Test
    void scoreExists_throws_BadRequestException_when_score_number_is_null() {
        String scoreNumber = null;
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.scoreExists(scoreNumber)
        );
        assertEquals("Номер счёта обязателен", thrown.getMessage());
        verify(jdbcTemplate, never()).queryForObject(anyString(), any(Class.class), anyString());
    }

    @Test
    void findByScoreNumber_returns_score_when_exists() {
        String scoreNumber = "ACC123456";
        Score expected = new Score(1L, 100L, scoreNumber, 1000L, true);
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(scoreNumber))).thenReturn(expected);
        Optional<Score> result = scoreRepository.findByScoreNumber(scoreNumber);
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void findByScoreNumber_returns_empty_optional_when_not_found() {
        String scoreNumber = "ACC123456";
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(scoreNumber))).thenReturn(null);
        Optional<Score> result = scoreRepository.findByScoreNumber(scoreNumber);
        assertFalse(result.isPresent());
    }

    @Test
    void findByScoreNumber_throws_RuntimeException_on_data_access_exception() {
        String scoreNumber = "ACC123456";
        doThrow(new DataAccessException("DB error") {})
                .when(jdbcTemplate)
                .queryForObject(anyString(), any(RowMapper.class), eq(scoreNumber));
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> scoreRepository.findByScoreNumber(scoreNumber)
        );
        assertTrue(thrown.getMessage().contains("Ошибка базы данных при поиске счёта"));
    }

    @Test
    void getUserIdByUuid_returns_user_id_when_exists() {
        String uuid = "uuid-123";
        Long expectedId = 100L;
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(uuid))).thenReturn(expectedId);
        Long userId = scoreRepository.getUserIdByUuid(uuid);
        assertEquals(expectedId, userId);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Long.class), eq(uuid));
    }

    @Test
    void getUserIdByUuid_throws_NotFoundException_when_user_not_found() {
        String uuid = "uuid-123";
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(uuid))).thenReturn(-1L);
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> scoreRepository.getUserIdByUuid(uuid)
        );
        assertEquals("Пользователь не найден", thrown.getMessage());
    }

    @Test
    void isScoreBelongsToUser_returns_true_when_belongs() {
        String scoreNumber = "ACC123456";
        Long userId = 100L;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(scoreNumber), eq(userId))).thenReturn(1);
        boolean belongs = scoreRepository.isScoreBelongsToUser(scoreNumber, userId);
        assertTrue(belongs);
    }

    @Test
    void isScoreBelongsToUser_returns_false_when_not_belonging() {
        String scoreNumber = "ACC123456";
        Long userId = 100L;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(scoreNumber), eq(userId))).thenReturn(0);
        boolean belongs = scoreRepository.isScoreBelongsToUser(scoreNumber, userId);
        assertFalse(belongs);
    }

    @Test
    void isScoreBelongsToUser_throws_BadRequestException_when_score_number_is_empty() {
        String scoreNumber = "";
        Long userId = 100L;
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.isScoreBelongsToUser(scoreNumber, userId)
        );
        assertEquals("Номер счёта обязателен", thrown.getMessage());
    }

    @Test
    void isScoreBelongsToUser_throws_BadRequestException_when_user_id_is_invalid() {
        String scoreNumber = "ACC123456";
        Long userId = -1L;
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreRepository.isScoreBelongsToUser(scoreNumber, userId)
        );
        assertEquals("ID пользователя обязателен", thrown.getMessage());
    }
}