package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceQueryService {

    private final BalanceTransactionRepository balanceTransactionRepository;

    @Transactional(readOnly = true)
    public Map<Currency, BigDecimal> getAllBalancesForStudent(Long studentId) {
        List<BalanceTransaction> transactions = balanceTransactionRepository.findByStudentId(studentId);
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        BalanceTransaction::getCurrency,
                        Collectors.mapping(BalanceTransaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }
}
