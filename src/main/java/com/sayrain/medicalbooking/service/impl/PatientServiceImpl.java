package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.repository.PatientRepository;
import com.sayrain.medicalbooking.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Cacheable(value = "patients", key = "'all'", unless = "#result == null or #result.size() == 0")
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Cacheable(value = "patients", key = "#id", unless = "#result == null or !#result.isPresent()")
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    @CacheEvict(value = "patients", allEntries = true)
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @CacheEvict(value = "patients", allEntries = true)
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
