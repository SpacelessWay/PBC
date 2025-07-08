package com.example.pbc.work_databased;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ScoreRepository scoreRepository;

    private TransferRepository transferRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferRepository = new TransferRepository(jdbcTemplate, scoreRepository);
    }

    @Test
    void executeTransfer_successfully_executes_transfer() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;
        Long idFrom = 1L;
        Long idTo = 2L;

        when(scoreRepository.scoreExists(fromScore)).thenReturn(true);
        when(scoreRepository.scoreExists(toScore)).thenReturn(true);

        transferRepository.executeTransfer(fromScore, toScore, amount, idFrom, idTo);

        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE scores SET balance = balance - ? WHERE score_number = ?"),
                eq(amount),
                eq(fromScore)
        );

        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE scores SET balance = balance + ? WHERE score_number = ?"),
                eq(amount),
                eq(toScore)
        );

        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO transfers (transfer_uuid, from_score_id, to_score_id, amount) VALUES (?, ?, ?, ?)"),
                anyString(),
                eq(idFrom),
                eq(idTo),
                eq(amount)
        );
    }

    @Test
    void executeTransfer_throws_IllegalArgumentException_when_from_score_not_found() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;
        Long idFrom = 1L;
        Long idTo = 2L;

        when(scoreRepository.scoreExists(fromScore)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> transferRepository.executeTransfer(fromScore, toScore, amount, idFrom, idTo)
        );

        assertEquals("Счёт отправителя не найден", thrown.getMessage());

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void executeTransfer_throws_IllegalArgumentException_when_to_score_not_found() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;
        Long idFrom = 1L;
        Long idTo = 2L;

        when(scoreRepository.scoreExists(fromScore)).thenReturn(true);
        when(scoreRepository.scoreExists(toScore)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> transferRepository.executeTransfer(fromScore, toScore, amount, idFrom, idTo)
        );

        assertEquals("Счёт получателя не найден", thrown.getMessage());

        // Проверяем, что уменьшение баланса всё-таки произошло?
        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE scores SET balance = balance - ? WHERE score_number = ?"),
                eq(amount),
                eq(fromScore)
        );

        // А вот увеличение — не должно быть
        verify(jdbcTemplate, never()).update(
                eq("UPDATE scores SET balance = balance + ? WHERE score_number = ?"),
                eq(amount),
                eq(toScore)
        );

        // INSERT в transfers тоже не должен быть выполнен
        verify(jdbcTemplate, never()).update(
                eq("INSERT INTO transfers (...) VALUES (?, ?, ?, ?)"),
                anyString(),
                eq(idFrom),
                eq(idTo),
                eq(amount)
        );
    }

    @Test
    void isScoreBelongsToUser_returns_true_when_belongs() {
        String scoreNumber = "ACC123456";
        String userUuid = "uuid-123";

        when(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_uuid = ?",
                Integer.class,
                scoreNumber,
                userUuid
        )).thenReturn(1);

        boolean belongs = transferRepository.isScoreBelongsToUser(scoreNumber, userUuid);
        assertTrue(belongs);
    }

    @Test
    void isScoreBelongsToUser_returns_false_when_not_belonging() {
        String scoreNumber = "ACC123456";
        String userUuid = "uuid-123";

        when(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_uuid = ?",
                Integer.class,
                scoreNumber,
                userUuid
        )).thenReturn(0);

        boolean belongs = transferRepository.isScoreBelongsToUser(scoreNumber, userUuid);
        assertFalse(belongs);
    }

    @Test
    void isScoreBelongsToUser_throws_exception_on_sql_error() {
        String scoreNumber = "ACC123456";
        String userUuid = "uuid-123";

        doThrow(new DataAccessException("DB Error") {})
                .when(jdbcTemplate)
                .queryForObject(
                        "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_uuid = ?",
                        Integer.class,
                        scoreNumber,
                        userUuid
                );

        assertThrows(DataAccessException.class, () ->
                transferRepository.isScoreBelongsToUser(scoreNumber, userUuid)
        );
    }
}
