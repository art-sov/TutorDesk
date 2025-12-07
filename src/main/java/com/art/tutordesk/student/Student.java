package com.art.tutordesk.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String knowledgeLevel; // "A2", "B1", "C1"
    private String country;
    private String phoneNumber;
    @Column(columnDefinition = "TEXT")
    private String globalGoal;
    private Integer age;
}

