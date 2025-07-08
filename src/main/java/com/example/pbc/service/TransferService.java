package com.example.pbc.service;

import com.example.pbc.exception.BadRequestException;
import com.example.pbc.exception.ForbiddenException;
import com.example.pbc.exception.NotFoundException;
import com.example.pbc.work_databased.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final TransferRepository transferRepository;
    private final ScoreService scoreService;

    public TransferService(TransferRepository transferRepository, ScoreService scoreService) {
        this.transferRepository = transferRepository;
        this.scoreService = scoreService;
    }

    /**
     * Выполнить перевод между счетами
     */
    public void transfer(String fromScoreNumber, String toScoreNumber, Long amount) {
        long startTime = System.currentTimeMillis();
        log.info("Попытка перевода: {} -> {} на сумму {}", fromScoreNumber, toScoreNumber, amount);

        if (amount == null || amount <= 0) {
            log.warn("Некорректная сумма перевода: {}", amount);
            throw new BadRequestException("Сумма должна быть положительной");
        }

        if (fromScoreNumber == null || fromScoreNumber.trim().isEmpty()) {
            log.warn("Счёт отправителя не указан");
            throw new BadRequestException("Счёт отправителя обязателен");
        }

        if (toScoreNumber == null || toScoreNumber.trim().isEmpty()) {
            log.warn("Счёт получателя не указан");
            throw new BadRequestException("Счёт получателя обязателен");
        }

        try {
            validateTransfer(fromScoreNumber, toScoreNumber, amount);
            transferRepository.executeTransfer(fromScoreNumber, toScoreNumber, amount,getScoreId(fromScoreNumber),getScoreId(toScoreNumber));
            log.info("Перевод успешен: {} -> {}", fromScoreNumber, toScoreNumber);
        } catch (NotFoundException | ForbiddenException ex) {
            log.warn("Ошибка валидации перевода", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при переводе", ex);
            throw new RuntimeException("Ошибка перевода: " + ex.getMessage(), ex);
        } finally {
            log.debug("Метод transfer выполнен за {} мс", System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Проверка условий перевода
     */
    private void validateTransfer(String fromScoreNumber, String toScoreNumber, Long amount) {
        long startTime = System.currentTimeMillis();
        log.debug("Начинаю валидацию перевода");

        if (fromScoreNumber.equals(toScoreNumber)) {
            log.warn("Перевод на тот же счёт: {} -> {}", fromScoreNumber, toScoreNumber);
            throw new BadRequestException("Нельзя перевести самому себе");
        }

        if (!scoreService.scoreExists(fromScoreNumber)) {
            log.warn("Счёт отправителя не найден: {}", fromScoreNumber);
            throw new NotFoundException("Счёт отправителя не существует");
        }

        if (!scoreService.scoreExists(toScoreNumber)) {
            log.warn("Счёт получателя не найден: {}", toScoreNumber);
            throw new NotFoundException("Счёт получателя не существует");
        }

        Long fromBalance = scoreService.getBalance(fromScoreNumber);
        if (fromBalance < amount) {
            log.warn("Недостаточно средств: баланс {}, требуется {}", fromBalance, amount);
            throw new ForbiddenException("Недостаточно средств на счёте");
        }

        log.debug("Валидация завершена успешно");
        log.debug("Метод validateTransfer выполнен за {} мс", System.currentTimeMillis() - startTime);
    }

    /**
     * Проверяет существование счёта
     */
    public boolean scoreExists(String scoreNumber) {
        boolean exists = scoreService.scoreExists(scoreNumber);
        log.debug("Проверка существования счёта {}: {}", scoreNumber, exists ? "существует" : "не найден");
        return exists;
    }

    /**
     * Проверяет, является ли пользователь владельцем счёта
     */
    public boolean isOwner(String scoreNumber, String userUuid) {
        boolean owner = transferRepository.isScoreBelongsToUser(scoreNumber, userUuid);
        if (!owner) {
            log.warn("Счёт {} не принадлежит пользователю {}", scoreNumber, userUuid);
        }
        return owner;
    }

    /**
     * Получение баланса по счёту
     */
    public Long getBalance(String scoreNumber) {
        Long balance = scoreService.getBalance(scoreNumber);
        log.debug("Баланс счёта {}: {}", scoreNumber, balance);
        return balance;
    }
    public Long getScoreId(String scoreNumber) {
        Long id = scoreService.getScoreId(scoreNumber);
        log.debug("Id счёта {}: {}", scoreNumber, id);
        return id;
    }
}