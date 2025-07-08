package com.example.pbc.service;


import com.example.pbc.exception.BadRequestException;
import com.example.pbc.model.Score;
import com.example.pbc.work_databased.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scoreService = new ScoreService(scoreRepository);
    }

    @Test
    void openScore_successfully_opens_new_score() {
        Long userId = 1L;
        Long initialBalance = 1000L;

        doNothing().when(scoreRepository).createScore(any(Score.class));

        assertDoesNotThrow(() -> scoreService.openScore(userId, initialBalance));
        verify(scoreRepository, times(1)).createScore(any(Score.class));
    }

    @Test
    void openScore_throws_exception_when_user_id_is_null() {
        Long userId = null;
        Long initialBalance = 1000L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreService.openScore(userId, initialBalance)
        );

        assertEquals("ID пользователя не может быть пустым или отрицательным", thrown.getMessage());
        verify(scoreRepository, never()).createScore(any(Score.class));
    }

    @Test
    void openScore_throws_exception_when_user_id_is_negative() {
        Long userId = -1L;
        Long initialBalance = 1000L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreService.openScore(userId, initialBalance)
        );

        assertEquals("ID пользователя не может быть пустым или отрицательным", thrown.getMessage());
        verify(scoreRepository, never()).createScore(any(Score.class));
    }

    @Test
    void openScore_throws_exception_when_initial_balance_is_negative() {
        Long userId = 1L;
        Long initialBalance = -100L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> scoreService.openScore(userId, initialBalance)
        );

        assertEquals("Баланс не может быть отрицательным", thrown.getMessage());
        verify(scoreRepository, never()).createScore(any(Score.class));
    }

    @Test
    void getScoresForUser_returns_list_of_scores() {
        Long userId = 1L;
        List<Score> scores = List.of(new Score(1L, userId, "ACC123456", 1000L, true));

        when(scoreRepository.getScoresByUserId(userId)).thenReturn(scores);

        List<Score> result = scoreService.getScoresForUser(userId);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scoreRepository, times(1)).getScoresByUserId(userId);
    }

    @Test
    void closeScore_successfully_closes_score() {
        String scoreNumber = "ACC123456";
        Long userId = 1L;

        when(scoreRepository.isScoreBelongsToUser(scoreNumber, userId)).thenReturn(true);

        assertDoesNotThrow(() -> scoreService.closeScore(scoreNumber, userId));
        verify(scoreRepository, times(1)).closeScore(scoreNumber);
    }

    @Test
    void closeScore_throws_exception_when_score_does_not_belong_to_user() {
        String scoreNumber = "ACC123456";
        Long userId = 1L;

        when(scoreRepository.isScoreBelongsToUser(scoreNumber, userId)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> scoreService.closeScore(scoreNumber, userId)
        );

        assertEquals("Счёт не принадлежит пользователю", thrown.getMessage());
        verify(scoreRepository, never()).closeScore(anyString());
    }

    @Test
    void scoreExists_returns_true_for_existing_score() {
        String scoreNumber = "ACC123456";
        when(scoreRepository.scoreExists(scoreNumber)).thenReturn(true);

        boolean exists = scoreService.scoreExists(scoreNumber);
        assertTrue(exists);
    }

    @Test
    void scoreExists_returns_false_for_nonexistent_score() {
        String scoreNumber = "ACC123456";
        when(scoreRepository.scoreExists(scoreNumber)).thenReturn(false);

        boolean exists = scoreService.scoreExists(scoreNumber);
        assertFalse(exists);
    }

    @Test
    void getBalance_returns_balance_for_valid_score() {
        String scoreNumber = "ACC123456";
        Long balance = 5000L;

        Score score = new Score(1L, 1L, scoreNumber, balance, true);
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.of(score));

        Long result = scoreService.getBalance(scoreNumber);
        assertEquals(balance, result);
    }

    @Test
    void getBalance_throws_exception_when_score_not_found() {
        String scoreNumber = "ACC123456";
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> scoreService.getBalance(scoreNumber)
        );

        assertEquals("Счёт не найден: ACC123456", thrown.getMessage());
    }

    @Test
    void getBalance_throws_exception_when_score_is_closed() {
        String scoreNumber = "ACC123456";

        Score score = new Score(1L, 1L, scoreNumber, 5000L, false);
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.of(score));

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> scoreService.getBalance(scoreNumber)
        );

        assertEquals("Счёт закрыт", thrown.getMessage());
    }

    @Test
    void getScoreId_returns_id_for_valid_score() {
        String scoreNumber = "ACC123456";
        Long expectedId = 123L;

        Score score = new Score(expectedId, 1L, scoreNumber, 5000L, true);
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.of(score));

        Long id = scoreService.getScoreId(scoreNumber);
        assertEquals(expectedId, id);
    }

    @Test
    void getScoreId_throws_exception_when_score_not_found() {
        String scoreNumber = "ACC123456";
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> scoreService.getScoreId(scoreNumber)
        );

        assertEquals("Счёт не найден: ACC123456", thrown.getMessage());
    }

    @Test
    void getScoreId_throws_exception_when_score_is_closed() {
        String scoreNumber = "ACC123456";

        Score score = new Score(1L, 1L, scoreNumber, 5000L, false);
        when(scoreRepository.findByScoreNumber(scoreNumber)).thenReturn(Optional.of(score));

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> scoreService.getScoreId(scoreNumber)
        );

        assertEquals("Счёт закрыт", thrown.getMessage());
    }

    @Test
    void getUserIdByUuid_delegates_to_repository() {
        String uuid = "uuid-123";
        Long expectedUserId = 1L;

        when(scoreRepository.getUserIdByUuid(uuid)).thenReturn(expectedUserId);

        Long actualUserId = scoreService.getUserIdByUuid(uuid);
        assertEquals(expectedUserId, actualUserId);
    }
}