package com.example.pbc.res_controller;

import com.example.pbc.config.TestSecurityConfig;
import com.example.pbc.exception.BadRequestException;
import com.example.pbc.model.Score;
import com.example.pbc.rest_controller.ScoreController;
import com.example.pbc.service.ScoreService;
import com.example.pbc.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ScoreControllerTest {

    @InjectMocks
    private ScoreController scoreController;

    @Mock
    private ScoreService scoreService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // === openNewScore tests ===

    @Test
    void testOpenNewScore_success() {
        String token = "Bearer valid-token";
        String uuid = "user-uuid";
        Long userId = 1L;
        Score score = new Score(1L, userId, "ACC001", 100L, true);

        when(jwtUtil.extractUuid(token)).thenReturn(uuid);
        when(scoreService.getUserIdByUuid(uuid)).thenReturn(userId);

        ResponseEntity<String> response = scoreController.openNewScore(token, score);

        assertEquals(ResponseEntity.ok(score.getScoreNumber()), response);
        verify(scoreService).openScore(userId, score.getBalance());
    }

    @Test
    void testOpenNewScore_invalidBalance_throwsBadRequestException() {
        String token = "Bearer valid-token";
        Score score = new Score(1L, 1L, "ACC001", -100L, true);

        assertThrows(BadRequestException.class, () -> {
            scoreController.openNewScore(token, score);
        });
    }

    @Test
    void testOpenNewScore_invalidToken_throwsIllegalArgumentException() {
        String invalidToken = "Bearer invalid";

        doThrow(new IllegalArgumentException("Неверный токен")).when(jwtUtil).extractUuid(invalidToken);

        Score score = new Score(1L, 1L, "ACC001", 100L, true);

        assertThrows(IllegalArgumentException.class, () -> {
            scoreController.openNewScore(invalidToken, score);
        });
    }

    // === listScores tests ===

    @Test
    void testListScores_success() {
        String token = "Bearer valid-token";
        String uuid = "user-uuid";
        Long userId = 1L;
        List<Score> scores = Arrays.asList(new Score(1L, userId, "ACC001", 100L, true), new Score(2L, userId, "ACC001", 100L, true));

        when(jwtUtil.extractUuid(token)).thenReturn(uuid);
        when(scoreService.getUserIdByUuid(uuid)).thenReturn(userId);
        when(scoreService.getScoresForUser(userId)).thenReturn(scores);

        List<Score> result = scoreController.listScores(token);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(scoreService).getScoresForUser(userId);
    }

    // === closeScore tests ===

    @Test
    void testCloseScore_success() {
        String token = "Bearer valid-token";
        String uuid = "user-uuid";
        Long userId = 1L;
        Score score = new Score(1L, userId, "ACC001", 100L, true);


        when(jwtUtil.extractUuid(token)).thenReturn(uuid);
        when(scoreService.getUserIdByUuid(uuid)).thenReturn(userId);

        ResponseEntity<String> response = scoreController.closeScore(token, score);

        assertEquals(ResponseEntity.ok("Счёт закрыт"), response);
        verify(scoreService).closeScore(score.getScoreNumber(), userId);
    }

    @Test
    void testCloseScore_emptyScoreNumber_throwsBadRequestException() {
        String token = "Bearer valid-token";
        Score score = new Score(0L, 0L, null, 100L, true);

        assertThrows(BadRequestException.class, () -> {
            scoreController.closeScore(token, score);
        });
    }
}