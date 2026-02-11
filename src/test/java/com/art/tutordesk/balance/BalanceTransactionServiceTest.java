package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceTransactionServiceTest {

    @Mock
    private BalanceTransactionRepository balanceTransactionRepository;

    @InjectMocks
    private BalanceTransactionService balanceTransactionService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setCurrency(Currency.USD);
    }

    @Test
    void createBalanceTransaction_shouldSaveBalanceTransaction() {
        TransactionType type = TransactionType.LESSON_CHARGE;
        Currency currency = Currency.USD;
        TransactionSource source = TransactionSource.LESSON;
        BigDecimal amount = BigDecimal.TEN;
        Long sourceId = 100L;

        ArgumentCaptor<BalanceTransaction> transactionCaptor = ArgumentCaptor.forClass(BalanceTransaction.class);
        when(balanceTransactionRepository.save(transactionCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        balanceTransactionService.createBalanceTransaction(student, type, currency, source, amount, sourceId);

        verify(balanceTransactionRepository, times(1)).save(any(BalanceTransaction.class));
        BalanceTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(student.getId(), savedTransaction.getStudent().getId());
        assertEquals(type, savedTransaction.getType());
        assertEquals(currency, savedTransaction.getCurrency());
        assertEquals(source, savedTransaction.getSourceEntity());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(sourceId, savedTransaction.getSourceId());
    }

    @Test
    void deleteTransactionsByStudentId_shouldCallRepositoryDelete() {
        Long studentIdToDelete = 2L;

        balanceTransactionService.deleteTransactionsByStudentId(studentIdToDelete);

        verify(balanceTransactionRepository, times(1)).deleteByStudentId(studentIdToDelete);
    }
}
