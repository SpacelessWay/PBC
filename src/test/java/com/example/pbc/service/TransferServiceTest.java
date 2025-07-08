package com.example.pbc.service;

import com.example.pbc.exception.*;
import com.example.pbc.work_databased.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ScoreService scoreService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferService = new TransferService(transferRepository, scoreService);
    }

    @Test
    void transfer_successfully_executes_valid_transfer() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;

        when(scoreService.scoreExists(fromScore)).thenReturn(true);
        when(scoreService.scoreExists(toScore)).thenReturn(true);
        when(scoreService.getBalance(fromScore)).thenReturn(2000L);
        when(scoreService.getScoreId(fromScore)).thenReturn(1L);
        when(scoreService.getScoreId(toScore)).thenReturn(2L);
        doNothing().when(transferRepository).executeTransfer(fromScore, toScore, amount, 1L, 2L);

        assertDoesNotThrow(() -> transferService.transfer(fromScore, toScore, amount));
        verify(transferRepository, times(1)).executeTransfer(fromScore, toScore, amount, 1L, 2L);
    }

    @Test
    void transfer_throws_BadRequestException_when_from_score_is_empty() {
        String fromScore = "";
        String toScore = "ACC789012";
        Long amount = 1000L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Счёт отправителя обязателен", thrown.getMessage());
        verify(scoreService, never()).getBalance(anyString());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_BadRequestException_when_to_score_is_empty() {
        String fromScore = "ACC123456";
        String toScore = "";
        Long amount = 1000L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Счёт получателя обязателен", thrown.getMessage());
        verify(scoreService, never()).getBalance(anyString());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_BadRequestException_when_amount_is_negative() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = -100L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Сумма должна быть положительной", thrown.getMessage());
        verify(scoreService, never()).scoreExists(anyString());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_BadRequestException_when_amount_is_zero() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 0L;

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Сумма должна быть положительной", thrown.getMessage());
        verify(scoreService, never()).scoreExists(anyString());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_BadRequestException_when_same_account_used() {
        String scoreNumber = "ACC123456";
        Long amount = 1000L;

        when(scoreService.scoreExists(scoreNumber)).thenReturn(true);
        when(scoreService.getBalance(scoreNumber)).thenReturn(2000L);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> transferService.transfer(scoreNumber, scoreNumber, amount)
        );

        assertTrue(thrown.getMessage().contains("Нельзя перевести самому себе"));
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_NotFoundException_when_from_score_does_not_exist() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;

        when(scoreService.scoreExists(fromScore)).thenReturn(false);

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Счёт отправителя не существует", thrown.getMessage());
        verify(scoreService, never()).getBalance(anyString());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_NotFoundException_when_to_score_does_not_exist() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;

        when(scoreService.scoreExists(fromScore)).thenReturn(true);
        when(scoreService.scoreExists(toScore)).thenReturn(false);

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Счёт получателя не существует", thrown.getMessage());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void transfer_throws_ForbiddenException_when_insufficient_balance() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;

        when(scoreService.scoreExists(fromScore)).thenReturn(true);
        when(scoreService.scoreExists(toScore)).thenReturn(true);
        when(scoreService.getBalance(fromScore)).thenReturn(500L);

        ForbiddenException thrown = assertThrows(
                ForbiddenException.class,
                () -> transferService.transfer(fromScore, toScore, amount)
        );

        assertEquals("Недостаточно средств на счёте", thrown.getMessage());
        verify(transferRepository, never()).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void validateTransfer_returns_no_exception_for_valid_data() {
        String fromScore = "ACC123456";
        String toScore = "ACC789012";
        Long amount = 1000L;

        when(scoreService.scoreExists(fromScore)).thenReturn(true);
        when(scoreService.scoreExists(toScore)).thenReturn(true);
        when(scoreService.getBalance(fromScore)).thenReturn(2000L);

        assertDoesNotThrow(() -> transferService.transfer(fromScore, toScore, amount));
        verify(transferRepository, times(1)).executeTransfer(anyString(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void isOwner_returns_true_when_user_owns_account() {
        String scoreNumber = "ACC123456";
        String userUuid = "uuid-123";

        when(transferRepository.isScoreBelongsToUser(scoreNumber, userUuid)).thenReturn(true);

        boolean result = transferService.isOwner(scoreNumber, userUuid);
        assertTrue(result);
    }

    @Test
    void isOwner_returns_false_when_user_does_not_own_account() {
        String scoreNumber = "ACC123456";
        String userUuid = "uuid-123";

        when(transferRepository.isScoreBelongsToUser(scoreNumber, userUuid)).thenReturn(false);

        boolean result = transferService.isOwner(scoreNumber, userUuid);
        assertFalse(result);
    }

    @Test
    void getBalance_returns_balance_when_score_exists() {
        String scoreNumber = "ACC123456";
        Long balance = 1000L;

        when(scoreService.getBalance(scoreNumber)).thenReturn(balance);

        Long result = transferService.getBalance(scoreNumber);
        assertNotNull(result);
        assertEquals(balance, result);
    }

    @Test
    void getScoreId_returns_id_when_score_exists() {
        String scoreNumber = "ACC123456";
        Long expectedId = 1L;

        when(scoreService.getScoreId(scoreNumber)).thenReturn(expectedId);

        Long id = transferService.getScoreId(scoreNumber);
        assertNotNull(id);
        assertEquals(expectedId, id);
    }
}