package com.example.pbc.service;

import com.example.pbc.exception.BadRequestException;
import com.example.pbc.model.Score;
import com.example.pbc.work_databased.ScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;


public class ScoreService {

    private final ScoreRepository scoreRepository;
    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public void openScore(Long userId, Long initialBalance) {
        long startTime = System.currentTimeMillis();
        log.info("Попытка открытия счёта для пользователя с ID {}", userId);

        if (userId == null || userId <= 0) {
            log.warn("Неверный ID пользователя");
            throw new BadRequestException("ID пользователя не может быть пустым или отрицательным");
        }

        if (initialBalance < 0) {
            log.warn("Некорректный баланс: {}", initialBalance);
            throw new BadRequestException("Баланс не может быть отрицательным");
        }

        try {
            String scoreNumber = generateScoreNumber();
            Score score = new Score(null, userId, scoreNumber, initialBalance, true);
            scoreRepository.createScore(score);
            log.info("Счёт {} успешно открыт для пользователя {}", scoreNumber, userId);
        } catch (Exception e) {
            log.error("Ошибка при открытии счёта для пользователя {}", userId, e);
            throw new RuntimeException("Не удалось открыть счёт", e);
        } finally {
            log.debug("Метод openScore выполнен за {} мс", System.currentTimeMillis() - startTime);
        }
    }

    public List<Score> getScoresForUser(Long userId) {
        return scoreRepository.getScoresByUserId(userId);
    }

    public void closeScore(String scoreNumber, Long userId) {
        if (!scoreRepository.isScoreBelongsToUser(scoreNumber, userId)) {
            throw new IllegalArgumentException("Счёт не принадлежит пользователю");
        }
        scoreRepository.closeScore(scoreNumber);
    }
    public boolean scoreExists(String scoreNumber) {
        return scoreRepository.scoreExists(scoreNumber);
    }

    private String generateScoreNumber() {
        // Пример генерации: ACC + 6 случайных цифр
        String prefix = "ACC";
        int suffixLength = 6;
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder(prefix);

        for (int i = 0; i < suffixLength; i++) {
            int index = (int) (Math.random() * numbers.length());
            sb.append(numbers.charAt(index));
        }

        return sb.toString();
    }

    public Long getBalance(String scoreNumber) {
        Score score = scoreRepository.findByScoreNumber(scoreNumber)
                .orElseThrow(() -> new IllegalArgumentException("Счёт не найден: " + scoreNumber));

        if (!score.getStatus()) {
            throw new IllegalArgumentException("Счёт закрыт");
        }

        return score.getBalance();
    }
    public Long getScoreId(String scoreNumber) {
        Score score = scoreRepository.findByScoreNumber(scoreNumber)
                .orElseThrow(() -> new IllegalArgumentException("Счёт не найден: " + scoreNumber));

        if (!score.getStatus()) {
            throw new IllegalArgumentException("Счёт закрыт");
        }

        return score.getId();
    }
    public Long getUserIdByUuid(String uuid) {

        return scoreRepository.getUserIdByUuid(uuid);
    }
}
