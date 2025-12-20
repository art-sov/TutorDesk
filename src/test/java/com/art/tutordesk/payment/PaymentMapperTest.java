package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {PaymentMapperImpl.class})
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    private Payment payment;
    private PaymentDto paymentDto;

    @BeforeEach
    void setUp() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        payment = new Payment();
        payment.setId(100L);
        payment.setPaymentDate(LocalDate.of(2025, 1, 1));
        payment.setStudent(student);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency(Currency.USD);

        paymentDto = new PaymentDto();
        paymentDto.setId(null);
        paymentDto.setPaymentDate(LocalDate.of(2025, 1, 1));
        paymentDto.setStudentId(1L);
        paymentDto.setStudentFirstName("John");
        paymentDto.setStudentLastName("Doe");
        paymentDto.setPaymentMethod(PaymentMethod.CASH);
        paymentDto.setAmount(new BigDecimal("100.00"));
        paymentDto.setCurrency(Currency.USD);
    }

    @Test
    void toPaymentDto_shouldMapAllFields() {
        PaymentDto result = paymentMapper.toPaymentDto(payment);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(payment.getId());
        assertThat(result.getPaymentDate()).isEqualTo(payment.getPaymentDate());
        assertThat(result.getStudentId()).isEqualTo(payment.getStudent().getId());
        assertThat(result.getStudentFirstName()).isEqualTo(payment.getStudent().getFirstName());
        assertThat(result.getStudentLastName()).isEqualTo(payment.getStudent().getLastName());
        assertThat(result.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
        assertThat(result.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(result.getCurrency()).isEqualTo(payment.getCurrency());
    }

    @Test
    void toPayment_shouldMapAllFieldsAndFetchStudent() {
        Payment result = paymentMapper.toPayment(paymentDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getPaymentDate()).isEqualTo(paymentDto.getPaymentDate());
        assertNull(result.getStudent());
        assertThat(result.getPaymentMethod()).isEqualTo(paymentDto.getPaymentMethod());
        assertThat(result.getAmount()).isEqualByComparingTo(paymentDto.getAmount());
        assertThat(result.getCurrency()).isEqualTo(paymentDto.getCurrency());
    }

    @Test
    void updatePaymentFromDto_shouldUpdateExistingPayment() {
        Payment existingPayment = new Payment();
        existingPayment.setId(100L);
        existingPayment.setPaymentDate(LocalDate.of(2024, 12, 1));
        existingPayment.setStudent(new Student());
        existingPayment.setPaymentMethod(PaymentMethod.CARD);
        existingPayment.setAmount(new BigDecimal("50.00"));
        existingPayment.setCurrency(Currency.EUR);

        PaymentDto updatedDto = new PaymentDto();
        updatedDto.setId(100L);
        updatedDto.setPaymentDate(LocalDate.of(2025, 2, 1));
        updatedDto.setStudentId(1L); // Student to update to
        updatedDto.setPaymentMethod(PaymentMethod.CASH);
        updatedDto.setAmount(new BigDecimal("150.00"));
        updatedDto.setCurrency(Currency.USD);

        paymentMapper.updatePaymentFromDto(updatedDto, existingPayment);

        assertThat(existingPayment).isNotNull();
        assertThat(existingPayment.getId()).isEqualTo(100L);
        assertThat(existingPayment.getPaymentDate()).isEqualTo(updatedDto.getPaymentDate());
        assertThat(existingPayment.getPaymentMethod()).isEqualTo(updatedDto.getPaymentMethod());
        assertThat(existingPayment.getAmount()).isEqualByComparingTo(updatedDto.getAmount());
        assertThat(existingPayment.getCurrency()).isEqualTo(updatedDto.getCurrency());
    }
}
