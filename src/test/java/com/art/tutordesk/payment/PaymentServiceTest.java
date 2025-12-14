package com.art.tutordesk.payment;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private PaymentService paymentService;

    private Student student;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        payment1 = new Payment();
        payment1.setId(100L);
        payment1.setStudent(student);
        payment1.setAmount(new BigDecimal("50.00"));
        payment1.setCurrency(Currency.USD);
        payment1.setPaymentDate(LocalDate.now());

        payment2 = new Payment();
        payment2.setId(101L);
        payment2.setStudent(student);
        payment2.setAmount(new BigDecimal("75.00"));
        payment2.setCurrency(Currency.EUR);
        payment2.setPaymentDate(LocalDate.now().minusDays(1));
    }

    @Test
    void getAllPayments_shouldReturnAllPayments() {
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        List<Payment> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(payment1));
        assertTrue(result.contains(payment2));
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getAllPayments_shouldReturnEmptyList_whenNoPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<Payment> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getPaymentById_shouldReturnPayment_whenFound() {
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment1));

        Payment result = paymentService.getPaymentById(100L);

        assertNotNull(result);
        assertEquals(payment1.getId(), result.getId());
        verify(paymentRepository, times(1)).findById(100L);
    }

    @Test
    void getPaymentById_shouldThrowException_whenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.getPaymentById(999L));

        assertEquals("Payment not found with id: 999", exception.getMessage());
        verify(paymentRepository, times(1)).findById(999L);
    }

    @Test
    void createPayment_shouldSavePaymentAndUpdateBalance() {
        when(paymentRepository.save(payment1)).thenReturn(payment1);

        Payment createdPayment = paymentService.createPayment(payment1);

        assertNotNull(createdPayment);
        assertEquals(payment1.getId(), createdPayment.getId());
        verify(paymentRepository, times(1)).save(payment1);
        verify(balanceService, times(1)).changeBalance(
                student.getId(), payment1.getCurrency(), payment1.getAmount());
        verify(balanceService, times(1)).resyncPaymentStatus(student.getId());
    }

    @Test
    void updatePayment_shouldUpdatePaymentAndBalance() {
        Payment updatedPayment = new Payment();
        updatedPayment.setId(100L);
        updatedPayment.setStudent(student);
        updatedPayment.setAmount(new BigDecimal("60.00")); // Amount changed from 50.00 to 60.00
        updatedPayment.setCurrency(Currency.USD);
        updatedPayment.setPaymentDate(LocalDate.now());

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment1));
        when(paymentRepository.save(updatedPayment)).thenReturn(updatedPayment);

        Payment result = paymentService.updatePayment(updatedPayment);

        assertNotNull(result);
        assertEquals(updatedPayment.getAmount(), result.getAmount());
        verify(paymentRepository, times(1)).findById(100L);
        verify(paymentRepository, times(1)).save(updatedPayment);

        ArgumentCaptor<BigDecimal> deltaCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(balanceService, times(1)).changeBalance(eq(student.getId()), eq(Currency.USD), deltaCaptor.capture());
        assertEquals(new BigDecimal("10.00"), deltaCaptor.getValue()); // 60.00 - 50.00 = 10.00
        verify(balanceService, times(1)).resyncPaymentStatus(student.getId());
    }

    @Test
    void updatePayment_shouldThrowException_whenPaymentNotFound() {
        Payment nonExistentPayment = new Payment();
        nonExistentPayment.setId(999L);
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.updatePayment(nonExistentPayment));

        assertEquals("Payment not found for update with id: 999", exception.getMessage());
        verify(paymentRepository, times(1)).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(balanceService, never()).changeBalance(anyLong(), any(Currency.class), any(BigDecimal.class));
        verify(balanceService, never()).resyncPaymentStatus(anyLong());
    }

    @Test
    void deletePayment_shouldDeletePaymentAndUpdateBalance() {
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment1));
        doNothing().when(paymentRepository).deleteById(100L);

        paymentService.deletePayment(100L);

        verify(paymentRepository, times(1)).findById(100L);
        verify(balanceService, times(1)).changeBalance(
                student.getId(), payment1.getCurrency(), payment1.getAmount().negate());
        verify(paymentRepository, times(1)).deleteById(100L);
        verify(balanceService, times(1)).resyncPaymentStatus(student.getId());
    }

    @Test
    void deletePayment_shouldThrowException_whenPaymentNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.deletePayment(999L));

        assertEquals("Payment not found for deletion with id: 999", exception.getMessage());
        verify(paymentRepository, times(1)).findById(999L);
        verify(paymentRepository, never()).deleteById(anyLong());
        verify(balanceService, never()).changeBalance(anyLong(), any(Currency.class), any(BigDecimal.class));
        verify(balanceService, never()).resyncPaymentStatus(anyLong());
    }
}
