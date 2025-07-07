package com.example.pbc.rest_controller;

import com.example.pbc.model.Score;
import com.example.pbc.service.ScoreService;
import com.example.pbc.exception.BadRequestException;
import com.example.pbc.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scores")
@Tag(name = "Счета", description = "API для работы со счетами пользователей")
public class ScoreController {

    private static final Logger log = LoggerFactory.getLogger(ScoreController.class);

    private final ScoreService scoreService;
    private final JwtUtil jwtUtil;

    public ScoreController(ScoreService scoreService, JwtUtil jwtUtil) {
        this.scoreService = scoreService;
        this.jwtUtil = jwtUtil;
    }

    // === Открытие счёта (теперь использует uuid из токена) ===
    @PostMapping("/open")
    @Operation(
            summary = "Открыть новый счёт",
            description = "Создаёт новый счёт с начальным балансом (требуется авторизация через JWT)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Счёт успешно открыт"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    public ResponseEntity<String> openNewScore(
            @RequestHeader("Authorization") String token,
            @RequestBody Score score) {

        String uuid = extractUuidFromToken(token);
        Long userId = scoreService.getUserIdByUuid(uuid); // получить ID из БД по UUID

        log.info("Попытка открытия счёта для пользователя: {}", userId);

        if (score.getBalance() <= 0) {
            log.warn("Некорректный баланс");
            throw new BadRequestException("Баланс не может быть отрицательным или нулём");
        }

        scoreService.openScore(userId, score.getBalance());
        log.info("Счёт успешно открыт для пользователя: {}", userId);
        return ResponseEntity.ok("Счёт открыт");
    }

    // === Получение списка счетов (через токен) ===
    @GetMapping("/list")
    @Operation(
            summary = "Получить список счетов пользователя",
            description = "Возвращает все активные счета текущего пользователя (на основе токена)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список счетов успешно получен"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат токена", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    public List<Score> listScores(@RequestHeader("Authorization") String token) {
        String uuid = extractUuidFromToken(token);
        Long userId = scoreService.getUserIdByUuid(uuid);

        log.info("Запрос на получение списка счетов для пользователя: {}", userId);

        List<Score> scores = scoreService.getScoresForUser(userId);
        log.info("Получено {} счетов для пользователя: {}", scores.size(), userId);
        return scores;
    }

    // === Закрытие счёта (с проверкой прав) ===
    @PostMapping("/close")
    @Operation(
            summary = "Закрыть счёт",
            description = "Закрывает указанный счёт, если он принадлежит пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Счёт успешно закрыт"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат запроса", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    public ResponseEntity<String> closeScore(
            @RequestHeader("Authorization") String token,
            @RequestBody Score score) {

        String uuid = extractUuidFromToken(token);
        Long userId = scoreService.getUserIdByUuid(uuid);

        String scoreNumber = score.getScoreNumber();
        log.info("Попытка закрытия счёта: {}", scoreNumber);

        if (scoreNumber == null || scoreNumber.trim().isEmpty()) {
            log.warn("Номер счёта не указан");
            throw new BadRequestException("Номер счёта обязателен для закрытия");
        }

        scoreService.closeScore(scoreNumber, userId);
        log.info("Счёт закрыт: {}", scoreNumber);
        return ResponseEntity.ok("Счёт закрыт");
    }

    // Вспомогательный метод для извлечения uuid из токена
    private String extractUuidFromToken(String token) {
        try {
            return jwtUtil.extractUuid(token);
        } catch (Exception ex) {
            log.warn("Неверный токен или истёкший срок действия");
            throw new IllegalArgumentException("Неверный токен");
        }
    }
}