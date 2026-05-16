package com.art.tutordesk.balance;

import com.art.tutordesk.BaseIntegrationTest;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/data-test.sql")
public class BalanceTransactionRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private BalanceTransactionRepository balanceTransactionRepository;
    @Autowired
    private StudentRepository studentRepository;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        student1 = studentRepository.findById(1L).orElseThrow();
        student2 = studentRepository.findById(2L).orElseThrow();
    }

    @Test
    void whenFindByStudentId_thenReturnsAllTransactionsForStudent() {
        List<BalanceTransaction> transactions = balanceTransactionRepository.findByStudentId(student1.getId());

        assertThat(transactions).isNotNull();
        assertThat(transactions).hasSize(3);
        assertThat(transactions.stream().allMatch(t -> t.getStudent().getId().equals(student1.getId()))).isTrue();
    }

    @Test
    void whenFindByStudentId_withNoTransactions_thenReturnsEmptyList() {
        List<BalanceTransaction> transactions = balanceTransactionRepository.findByStudentId(99L); // Non-existent student ID

        assertThat(transactions).isNotNull();
        assertThat(transactions).isEmpty();
    }

    @Test
    void whenFindByStudentIdAndCurrency_thenReturnsFilteredTransactions() {
        List<BalanceTransaction> usdTransactions = balanceTransactionRepository.findByStudentIdAndCurrency(student1.getId(), Currency.USD);

        assertThat(usdTransactions).isNotNull();
        assertThat(usdTransactions).hasSize(3); // PAYMENT_RECEIVED (USD), LESSON_CHARGE (USD) for student 1
        assertThat(usdTransactions.stream().allMatch(t -> t.getStudent().getId().equals(student1.getId()) && t.getCurrency().equals(Currency.USD))).isTrue();

        List<BalanceTransaction> eurTransactions = balanceTransactionRepository.findByStudentIdAndCurrency(student1.getId(), Currency.EUR);
        assertThat(eurTransactions).hasSize(0); // LESSON_CHARGE (EUR) for student 1
    }

    @Test
    void whenFindByStudentIdAndCurrency_withNoMatchingCurrency_thenReturnsEmptyList() {
        List<BalanceTransaction> plnTransactions = balanceTransactionRepository.findByStudentIdAndCurrency(student1.getId(), Currency.PLN);

        assertThat(plnTransactions).isNotNull();
        assertThat(plnTransactions).isEmpty();
    }

    @Test
    void whenDeleteByStudentId_thenRemovesAllTransactionsForStudent() {
        Long studentIdToDelete = student1.getId();

        long initialCount = balanceTransactionRepository.findByStudentId(studentIdToDelete).size();
        assertThat(initialCount).isEqualTo(3);

        balanceTransactionRepository.deleteByStudentId(studentIdToDelete);
        balanceTransactionRepository.flush();

        List<BalanceTransaction> remainingTransactions = balanceTransactionRepository.findByStudentId(studentIdToDelete);
        assertThat(remainingTransactions).isEmpty();

        // Verify other students' transactions are unaffected
        List<BalanceTransaction> otherStudentTransactions = balanceTransactionRepository.findByStudentId(student2.getId());
        assertThat(otherStudentTransactions).hasSize(3); // student2 has 3 transactions in data-test.sql
    }
    
    @Test
    void whenSaveBalanceTransaction_thenTransactionIsPersisted() {
        BalanceTransaction newTransaction = new BalanceTransaction();
        newTransaction.setStudent(student1); // Set the actual Student object
        newTransaction.setTransactionDateTime(LocalDateTime.now());
        newTransaction.setType(TransactionType.PAYMENT_RECEIVED);
        newTransaction.setAmount(new BigDecimal("50.00"));
        newTransaction.setCurrency(Currency.USD);
        newTransaction.setSourceEntity(TransactionSource.PAYMENT);
        newTransaction.setSourceId(1L);

        BalanceTransaction savedTransaction = balanceTransactionRepository.save(newTransaction);

        assertNotNull(savedTransaction);
        assertNotNull(savedTransaction.getId());
        assertThat(savedTransaction.getAmount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(savedTransaction.getCurrency()).isEqualTo(Currency.USD);

        // Verify it's linked to the correct student
        Optional<BalanceTransaction> foundTransaction = balanceTransactionRepository.findById(savedTransaction.getId());
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getStudent().getId()).isEqualTo(student1.getId());
    }
}
