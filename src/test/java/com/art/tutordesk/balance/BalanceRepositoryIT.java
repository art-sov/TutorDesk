package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class BalanceRepositoryIT {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private TestEntityManager entityManager;


    @Test
    void findByStudentIdAndCurrency_whenBalanceExists_shouldReturnBalance() {
        Long studentId = 1L;
        Currency currency = Currency.USD;

        Optional<Balance> foundBalance = balanceRepository.findByStudentIdAndCurrency(studentId, currency);

        assertThat(foundBalance).isPresent();
        assertThat(foundBalance.get().getStudent().getId()).isEqualTo(studentId);
        assertThat(foundBalance.get().getCurrency()).isEqualTo(currency);
        assertThat(foundBalance.get().getAmount()).isEqualByComparingTo(new BigDecimal("-5.00"));
    }

    @Test
    void findByStudentIdAndCurrency_whenBalanceDoesNotExist_shouldReturnEmpty() {
        Long studentId = 2L; // Student 2 has EUR balance, not USD
        Currency currency = Currency.USD;

        Optional<Balance> foundBalance = balanceRepository.findByStudentIdAndCurrency(studentId, currency);

        assertThat(foundBalance).isNotPresent();
    }

    @Test
    void findByStudentId_whenStudentHasMultipleBalances_shouldReturnAll() {
        Long studentId = 1L; // Student 1 has balances in USD and EUR

        List<Balance> balances = balanceRepository.findByStudentId(studentId);

        assertThat(balances).hasSize(2);
        Set<Currency> currencies = balances.stream().map(Balance::getCurrency).collect(Collectors.toSet());
        assertThat(currencies).containsExactlyInAnyOrder(Currency.USD, Currency.EUR);
    }

    @Test
    void findByStudentId_whenStudentHasOneBalance_shouldReturnOne() {
        Long studentId = 2L; // Student 2 has only EUR balance

        List<Balance> balances = balanceRepository.findByStudentId(studentId);

        assertThat(balances).hasSize(1);
        assertThat(balances.getFirst().getCurrency()).isEqualTo(Currency.EUR);
    }

    @Test
    void findByStudentId_whenStudentHasNoBalances_shouldReturnEmptyList() {
        // We need to create a student without a balance for this test
        // But our data file links all students to balances, so we'll test by deleting them first
        Long studentId = 1L;
        balanceRepository.deleteAllByStudentId(studentId);
        entityManager.flush();
        entityManager.clear();

        List<Balance> balances = balanceRepository.findByStudentId(studentId);

        assertThat(balances).isEmpty();
    }

    @Test
    void deleteAllByStudentId_shouldRemoveAllStudentBalances() {
        Long studentIdToDelete = 1L;
        long initialCount = balanceRepository.findByStudentId(studentIdToDelete).size();
        assertThat(initialCount).isEqualTo(2);

        balanceRepository.deleteAllByStudentId(studentIdToDelete);
        entityManager.flush(); // Use entity manager to flush changes for @Modifying query
        entityManager.clear(); // Detach all entities to ensure fresh data is loaded

        List<Balance> balances = balanceRepository.findByStudentId(studentIdToDelete);
        assertThat(balances).isEmpty();

        // And other students' balances should remain
        long otherStudentsBalanceCount = balanceRepository.count();
        assertThat(otherStudentsBalanceCount).isEqualTo(4); // 6 total - 2 deleted
    }

    @Test
    void findCurrenciesByStudentId_withMultipleBalances_shouldReturnDistinctSet() {
        Long studentId = 1L; // Student 1 has balances in USD and EUR

        Set<Currency> currencies = balanceRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).hasSize(2);
        assertThat(currencies).containsExactlyInAnyOrder(Currency.USD, Currency.EUR);
    }

    @Test
    void findCurrenciesByStudentId_withSingleBalance_shouldReturnSingleElementSet() {
        Long studentId = 2L; // Student 2 only has a balance in EUR

        Set<Currency> currencies = balanceRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).hasSize(1);
        assertThat(currencies).contains(Currency.EUR);
    }

    @Test
    void findCurrenciesByStudentId_withNoBalances_shouldReturnEmptySet() {
        Long studentId = 1L;
        balanceRepository.deleteAllByStudentId(studentId);
        entityManager.flush();
        entityManager.clear();

        Set<Currency> currencies = balanceRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).isEmpty();
    }
}