package com.example.pbc.work_databased;

import com.example.pbc.model.Score;
import com.example.pbc.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.List;
import java.util.Optional;


public class ScoreRepository {
    private static final Logger log = LoggerFactory.getLogger(TransferRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public ScoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createScore(Score score) {
        jdbcTemplate.update("INSERT INTO scores(user_id, score_number, balance, is_active) VALUES (?, ?, ?, ?)",
                score.getUserId(), score.getScoreNumber(), score.getBalance(), score.getStatus());
    }

    public List<Score> getScoresByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM scores WHERE user_id = ? AND is_active = true",
                (rs, rowNum) -> new Score(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("score_number"),
                        rs.getFloat("balance"),
                        rs.getBoolean("is_active")
                ),
                userId);
    }

    public void closeScore(String scoreNumber) {
        jdbcTemplate.update("UPDATE scores SET is_active = false, closed_at = NOW() WHERE score_number = ?",
                scoreNumber);
    }
    public boolean scoreExists(String scoreNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ?",
                Integer.class,
                scoreNumber
        );
        return count != null && count > 0;
    }

    public Optional<Score> findByScoreNumber(String scoreNumber) {
        try {
            log.info("Ищу счет по номеру: {}", scoreNumber);
            Score score = jdbcTemplate.queryForObject(
                    "SELECT * FROM scores WHERE score_number = ?",
                    (rs, rowNum) -> new Score(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("score_number"),
                            rs.getFloat("balance"),
                            rs.getBoolean("is_active")
                    ),
                    scoreNumber);
            if (score == null) {
                log.warn("Счет {} не найден", scoreNumber);
                return Optional.empty();
            }
            log.info("Найден счет: {}", scoreNumber);
            return Optional.of(score);
        } catch (Exception e) {
            log.error("Ошибка при поиске счета: {}", scoreNumber, e);
            return Optional.empty();
        }
    }
    public Long getUserIdByUuid(String uuid) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE uuid = ?", Long.class, uuid);
    }
    public boolean isScoreBelongsToUser(String scoreNumber, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_id = ?",
                Integer.class, scoreNumber, userId);
        return count != null && count > 0;
    }
}