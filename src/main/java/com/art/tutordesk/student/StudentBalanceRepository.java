package com.art.tutordesk.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentBalanceRepository extends JpaRepository<StudentBalance, Long> {
    Optional<StudentBalance> findByStudentId(Long studentId);
}
