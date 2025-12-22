package com.art.tutordesk.payment;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
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
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private StudentService studentService;
    @InjectMocks
    private PaymentService paymentService;

    private Student student;
    private Payment payment1;
    private Payment payment2;
    private PaymentDto paymentDto1;
    private PaymentDto paymentDto2;

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

        paymentDto1 = new PaymentDto();
        paymentDto1.setId(100L);
        paymentDto1.setStudentId(student.getId());
        paymentDto1.setStudentFirstName(student.getFirstName());
        paymentDto1.setStudentLastName(student.getLastName());
        paymentDto1.setAmount(new BigDecimal("50.00"));
        paymentDto1.setCurrency(Currency.USD);
        paymentDto1.setPaymentDate(LocalDate.now());

        payment2 = new Payment();
        payment2.setId(101L);
        payment2.setStudent(student);
        payment2.setAmount(new BigDecimal("75.00"));
        payment2.setCurrency(Currency.EUR);
        payment2.setPaymentDate(LocalDate.now().minusDays(1));

        paymentDto2 = new PaymentDto();
        paymentDto2.setId(101L);
        paymentDto2.setStudentId(student.getId());
        paymentDto2.setStudentFirstName(student.getFirstName());
        paymentDto2.setStudentLastName(student.getLastName());
        paymentDto2.setAmount(new BigDecimal("75.00"));
        paymentDto2.setCurrency(Currency.EUR);
        paymentDto2.setPaymentDate(LocalDate.now().minusDays(1));
    }

    @Test
    void getAllPayments_shouldReturnAllPayments() {
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));
        when(paymentMapper.toPaymentDto(payment1)).thenReturn(paymentDto1);
        when(paymentMapper.toPaymentDto(payment2)).thenReturn(paymentDto2);

        List<PaymentDto> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(paymentDto1));
        assertTrue(result.contains(paymentDto2));
        verify(paymentRepository, times(1)).findAll();
        verify(paymentMapper, times(1)).toPaymentDto(payment1);
        verify(paymentMapper, times(1)).toPaymentDto(payment2);
    }

    @Test
    void getAllPayments_shouldReturnEmptyList_whenNoPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<PaymentDto> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findAll();
        verify(paymentMapper, never()).toPaymentDto(any(Payment.class));
    }

    @Test
    void getPaymentById_shouldReturnPayment_whenFound() {
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment1));
        when(paymentMapper.toPaymentDto(payment1)).thenReturn(paymentDto1);

        PaymentDto result = paymentService.getPaymentById(100L);

        assertNotNull(result);
        assertEquals(paymentDto1.getId(), result.getId());
        verify(paymentRepository, times(1)).findById(100L);
        verify(paymentMapper, times(1)).toPaymentDto(payment1);
    }

    @Test
    void getPaymentById_shouldThrowException_whenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.getPaymentById(999L));

        assertEquals("Payment not found with id: 999", exception.getMessage());
        verify(paymentRepository, times(1)).findById(999L);
        verify(paymentMapper, never()).toPaymentDto(any(Payment.class));
    }

    @Test
    void createPayment_shouldSavePaymentAndUpdateBalance() {
        when(paymentMapper.toPayment(any(PaymentDto.class))).thenReturn(payment1);
        when(studentService.getStudentEntityById(student.getId())).thenReturn(student);
        when(paymentRepository.save(payment1)).thenReturn(payment1);
        when(paymentMapper.toPaymentDto(payment1)).thenReturn(paymentDto1);

        PaymentDto createdPaymentDto = paymentService.createPayment(paymentDto1);

        assertNotNull(createdPaymentDto);
        assertEquals(paymentDto1.getId(), createdPaymentDto.getId());
        verify(paymentMapper, times(1)).toPayment(any(PaymentDto.class));
        verify(studentService, times(1)).getStudentEntityById(student.getId());
        verify(paymentRepository, times(1)).save(payment1);
        verify(balanceService, times(1)).changeBalance(
                student.getId(), payment1.getCurrency(), payment1.getAmount());
        verify(balanceService, times(1)).resyncPaymentStatus(student.getId());
        verify(paymentMapper, times(1)).toPaymentDto(payment1);
    }

    @Test
    void updatePayment_shouldUpdatePaymentAndBalance() {
        PaymentDto updatedPaymentDto = new PaymentDto();
        updatedPaymentDto.setId(100L);
        updatedPaymentDto.setStudentId(student.getId());
        updatedPaymentDto.setAmount(new BigDecimal("60.00"));
        updatedPaymentDto.setCurrency(Currency.USD);
        updatedPaymentDto.setPaymentDate(LocalDate.now());

        Payment updatedPaymentEntity = new Payment();
        updatedPaymentEntity.setId(100L);
        updatedPaymentEntity.setStudent(student);
        updatedPaymentEntity.setAmount(new BigDecimal("60.00"));
        updatedPaymentEntity.setCurrency(Currency.USD);
        updatedPaymentEntity.setPaymentDate(LocalDate.now());

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment1));
        doNothing().when(paymentMapper).updatePaymentFromDto(any(PaymentDto.class), eq(payment1));
        when(studentService.getStudentEntityById(student.getId())).thenReturn(student);
        when(paymentRepository.save(payment1)).thenReturn(updatedPaymentEntity);
        when(paymentMapper.toPaymentDto(updatedPaymentEntity)).thenReturn(updatedPaymentDto);

        PaymentDto result = paymentService.updatePayment(updatedPaymentDto);

        assertNotNull(result);
        assertEquals(updatedPaymentDto.getAmount(), result.getAmount());
        verify(paymentRepository, times(1)).findById(100L);
        verify(paymentMapper, times(1)).updatePaymentFromDto(any(PaymentDto.class), eq(payment1));
        verify(studentService, times(1)).getStudentEntityById(student.getId());
        verify(paymentRepository, times(1)).save(payment1);

        ArgumentCaptor<BigDecimal> deltaCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(balanceService, times(1)).changeBalance(eq(student.getId()), eq(Currency.USD), deltaCaptor.capture());
        assertEquals(new BigDecimal("10.00"), deltaCaptor.getValue());
        verify(balanceService, times(1)).resyncPaymentStatus(student.getId());
        verify(paymentMapper, times(1)).toPaymentDto(updatedPaymentEntity);
    }

    @Test
    void updatePayment_shouldThrowException_whenPaymentNotFound() {
        PaymentDto nonExistentPaymentDto = new PaymentDto();
        nonExistentPaymentDto.setId(999L);
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.updatePayment(nonExistentPaymentDto));

        assertEquals("Payment not found for update with id: 999", exception.getMessage());
        verify(paymentRepository, times(1)).findById(999L);
        verify(paymentMapper, never()).updatePaymentFromDto(any(PaymentDto.class), any(Payment.class));
        verify(studentService, never()).getStudentById(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(balanceService, never()).changeBalance(anyLong(), any(Currency.class), any(BigDecimal.class));
        verify(balanceService, never()).resyncPaymentStatus(anyLong());
        verify(paymentMapper, never()).toPaymentDto(any(Payment.class));
    }

    @Test
    void deletePayment_shouldDeletePaymentAndBalance() {
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