package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);

    Optional<Patient> findByUserId(Long userId);
}
