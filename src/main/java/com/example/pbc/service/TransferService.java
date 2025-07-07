package com.example.pbc.service;


import com.example.pbc.work_databased.TransferRepository;

public class TransferService {

    private final TransferRepository transferRepository;
    private final ScoreService scoreService;

    public TransferService(TransferRepository transferRepository, ScoreService scoreService) {
        this.transferRepository = transferRepository;
        this.scoreService = scoreService;
    }

    //@Transactional
    public void transfer(String fromScoreNumber, String toScoreNumber, float amount) {
        validateTransfer(fromScoreNumber, toScoreNumber, amount);
        transferRepository.executeTransfer(fromScoreNumber, toScoreNumber, amount);
    }
    public boolean scoreExists(String scoreNumber) {
        return scoreService.scoreExists(scoreNumber);
    }

    private void validateTransfer(String fromScoreNumber, String toScoreNumber, float amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        if (fromScoreNumber.equals(toScoreNumber)) {
            throw new IllegalArgumentException("Нельзя перевести самому себе");
        }

        if (!scoreService.scoreExists(fromScoreNumber)) {
            throw new IllegalArgumentException("Счёт отправителя не существует");
        }

        if (!scoreService.scoreExists(toScoreNumber)) {
            throw new IllegalArgumentException("Счёт получателя не существует");
        }

        float fromBalance = scoreService.getBalance(fromScoreNumber);
        if (fromBalance < amount) {
            throw new IllegalArgumentException("Недостаточно средств на счёте");
        }
    }
}