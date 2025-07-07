package com.example.pbc.service;

import com.example.pbc.model.Score;
import com.example.pbc.work_databased.ScoreRepository;

import java.time.LocalDateTime;
import java.util.List;


public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public void openScore(Long userId, float initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        String scoreNumber = generateScoreNumber();

        Score score = new Score(null, userId, scoreNumber, initialBalance, true);
        scoreRepository.createScore(score);
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

    public float getBalance(String scoreNumber) {
        Score score = scoreRepository.findByScoreNumber(scoreNumber)
                .orElseThrow(() -> new IllegalArgumentException("Счёт не найден: " + scoreNumber));

        if (!score.getStatus()) {
            throw new IllegalArgumentException("Счёт закрыт");
        }

        return score.getBalance();
    }
    public Long getUserIdByUuid(String uuid) {
        return scoreRepository.getUserIdByUuid(uuid);
    }
}
