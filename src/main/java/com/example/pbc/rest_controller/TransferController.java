package com.example.pbc.rest_controller;

import com.example.pbc.model.Transfer;
import com.example.pbc.security.JwtUtil;
import com.example.pbc.service.TransferService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Переводы", description = "API для работы с переводами пользователей")
public class TransferController {

    private final TransferService transferService;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    public TransferController(TransferService transferService, JwtUtil jwtUtil) {
        this.transferService = transferService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/perform")
    @Operation(summary = "Выполнить перевод", description = "Пользователь указывает счёт-отправитель, счёт-получатель и сумму")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Неверный или отсутствующий токен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Пользователь не является владельцем счёта", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<String> performTransfer(
            @RequestHeader("Authorization") String token,
            @RequestBody Transfer transfer) {

        String uuid = jwtUtil.extractUuid(token); // извлекаем uuid из токена
        log.info("Вызван метод performTransfer с данными: {}, пользователь: {}", transfer, uuid);

        // Проверка прав доступа
        /*if (!transferService.isOwner(transfer.getFromScoreNumber(), uuid)) {
            log.warn("Пользователь {} не является владельцем счёта {}", uuid, transfer.getFromScoreNumber());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не являетесь владельцем счёта");
        }*/

        try {
            transferService.transfer(transfer.getFromScoreNumber(), transfer.getToScoreNumber(), transfer.getAmount());
            log.info("Перевод успешно выполнен: {}", transfer);
            return ResponseEntity.ok("Перевод выполнен успешно");
        } catch (Exception e) {
            log.error("Ошибка при выполнении перевода: {}", transfer, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при выполнении перевода");
        }
    }

    @PostMapping("/check-score")
    @Operation(summary = "Проверить возможность перевода", description = "Проверяет существование счетов и наличие достаточного баланса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Неверный или отсутствующий токен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> checkScore(@RequestHeader("Authorization") String token,
                                                          @RequestBody Transfer transfer) {
        Map<String, Object> result = new HashMap<>();
        String uuid;
        try {
            uuid = jwtUtil.extractUuid(token);
        } catch (JwtException e) {
            log.warn("Невалидный токен: {}", e.getMessage());
            result.put("error", "Невалидный токен");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        log.info("Вызван метод checkScore с данными: {}, пользователь: {}", transfer, uuid);



        boolean fromExists = transferService.scoreExists(transfer.getFromScoreNumber());
        boolean toExists = transferService.scoreExists(transfer.getToScoreNumber());
        Long balance = transferService.getBalance(transfer.getFromScoreNumber());

        if (!fromExists || !toExists) {
            result.put("error", "Один из счетов не существует или вы не владелец счёта отправителя");
            log.warn("Ошибка проверки: {}", result.get("error"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        if (balance < transfer.getAmount()) {
            result.put("error", "Недостаточно средств на счёте");
            log.warn("Ошибка проверки: {}", result.get("error"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        result.put("from_score_exists", fromExists);
        result.put("to_score_exists", toExists);
        result.put("balance", balance);
        result.put("amount", transfer.getAmount());
        result.put("possible", balance >= transfer.getAmount());

        log.info("Результат проверки перевода: {}", result);
        return ResponseEntity.ok(result);
    }

    /*@PostMapping("/execute")
    public ResponseEntity<String> executeTransfer(
            @RequestBody Transfer transfer,
            @RequestHeader("Authorization") String token) {

        String uuid = jwtUtil.extractUuid(token);
        log.info("Вызван метод executeTransfer с данными: {}, пользователь: {}", transfer, uuid);

        try {
            transferService.transfer(transfer.getFromScoreNumber(), transfer.getToScoreNumber(), transfer.getAmount());
            log.info("Перевод выполнен: {}", transfer);
            return ResponseEntity.ok("Перевод выполнен успешно");
        } catch (Exception e) {
            log.error("Ошибка при выполнении перевода: {}", transfer, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при выполнении перевода");
        }
    }*/
}