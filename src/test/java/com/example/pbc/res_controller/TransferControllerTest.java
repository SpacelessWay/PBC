package com.example.pbc.res_controller;


import com.example.pbc.config.TestSecurityConfig;
import com.example.pbc.model.Transfer;
import com.example.pbc.rest_controller.TransferController;
import com.example.pbc.security.JwtUtil;
import com.example.pbc.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken = "Bearer valid-token";
    private String uuid = "123e4567-e89b-12d3-a456-426614174000";

    @BeforeEach
    void setUp() {
        when(jwtUtil.extractUuid("valid-token")).thenReturn(uuid);
    }

    // --- ТЕСТЫ ДЛЯ /perform ---

    @Test
    void performTransfer_ShouldReturnOk_WhenSuccessful() throws Exception {
        Transfer transfer = new Transfer("123", "456", 100L);

        doNothing().when(transferService).transfer("123", "456", 100L);

        mockMvc.perform(post("/transfers/perform")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен успешно"));

        verify(transferService, times(1)).transfer("123", "456", 100L);
    }

    @Test
    void performTransfer_ShouldReturnInternalServerError_WhenExceptionOccurs() throws Exception {
        Transfer transfer = new Transfer("123", "456", 100L);

        doThrow(new RuntimeException("Database error")).when(transferService).transfer("123", "456", 100L);

        mockMvc.perform(post("/transfers/perform")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Произошла ошибка при выполнении перевода"));
    }

    // --- ТЕСТЫ ДЛЯ /check-score ---

    @Test
    void checkScore_ShouldReturnOkWithBalanceData_WhenValid() throws Exception {
        Transfer transfer = new Transfer("123", "456", 100L);
        Map<String, Object> result = new HashMap<>();
        result.put("from_score_exists", true);
        result.put("to_score_exists", true);
        result.put("balance", 200L);
        result.put("amount", 100L);
        result.put("possible", true);

        when(transferService.scoreExists("123")).thenReturn(true);
        when(transferService.scoreExists("456")).thenReturn(true);
        when(transferService.getBalance("123")).thenReturn(200L);

        mockMvc.perform(post("/transfers/check-score")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from_score_exists").value(true))
                .andExpect(jsonPath("$.to_score_exists").value(true))
                .andExpect(jsonPath("$.balance").value(200))
                .andExpect(jsonPath("$.possible").value(true));
    }

    @Test
    void checkScore_ShouldReturnBadRequest_WhenFromScoreDoesNotExist() throws Exception {
        Transfer transfer = new Transfer("123", "456", 100L);

        when(transferService.scoreExists("123")).thenReturn(false);
        when(transferService.scoreExists("456")).thenReturn(true);

        mockMvc.perform(post("/transfers/check-score")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Один из счетов не существует или вы не владелец счёта отправителя"));
    }

    @Test
    void checkScore_ShouldReturnBadRequest_WhenInsufficientFunds() throws Exception {
        Transfer transfer = new Transfer("123", "456", 200L);

        when(transferService.scoreExists("123")).thenReturn(true);
        when(transferService.scoreExists("456")).thenReturn(true);
        when(transferService.getBalance("123")).thenReturn(100L);

        mockMvc.perform(post("/transfers/check-score")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств на счёте"));
    }

    @Test
    void checkScore_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        Transfer transfer = new Transfer("123", "456", 100L);

        doThrow(new JwtException("Invalid token")).when(jwtUtil).extractUuid(anyString());

        mockMvc.perform(post("/transfers/check-score")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Невалидный токен"));
    }
}
