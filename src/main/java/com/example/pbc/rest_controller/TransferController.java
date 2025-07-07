package com.example.pbc.rest_controller;

import com.example.pbc.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/perform")
    @Operation(summary = "Выполнить перевод", description = "Пользователь указывает счёт-отправитель, счёт-получатель и сумму в копейках")
    public ResponseEntity<String> performTransfer(@RequestParam String fromScore,
                                                  @RequestParam String toScore,
                                                  @RequestParam float amount) {
        transferService.transfer(fromScore, toScore, amount);
        return ResponseEntity.ok("Перевод выполнен успешно");
    }

    @GetMapping("/check-score")
    public ResponseEntity<Boolean> checkScore(@RequestParam String scoreNumber) {
        boolean exists = transferService.scoreExists(scoreNumber);
        return ResponseEntity.ok(exists);
    }
}