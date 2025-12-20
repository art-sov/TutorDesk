package com.art.tutordesk.payment;

import com.art.tutordesk.student.service.StudentService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PaymentMapper {

    @Autowired
    protected StudentService studentService;

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.firstName", target = "studentFirstName")
    @Mapping(source = "student.lastName", target = "studentLastName")
    public abstract PaymentDto toPaymentDto(Payment payment);

    @Mapping(target = "student", ignore = true)
    public abstract Payment toPayment(PaymentDto paymentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    public abstract void updatePaymentFromDto(PaymentDto paymentDto, @MappingTarget Payment payment);
}
