package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementRequest;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementResponse;
import dev.tomislavmiksik.phoenixbe.entity.Measurement;
import dev.tomislavmiksik.phoenixbe.entity.User;
import dev.tomislavmiksik.phoenixbe.repository.MeasurementRepository;
import dev.tomislavmiksik.phoenixbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementServiceImpl implements MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeasurementResponse createMeasurement(MeasurementRequest request) {
        User currentUser = getCurrentUser();

        Measurement measurement = Measurement.builder()
                .user(currentUser)
                .weight(request.getWeight())
                .height(request.getHeight())
                .chestCircumference(request.getChestCircumference())
                .armCircumference(request.getArmCircumference())
                .legCircumference(request.getLegCircumference())
                .waistCircumference(request.getWaistCircumference())
                .measurementDate(request.getMeasurementDate() != null ?
                        request.getMeasurementDate() : LocalDateTime.now())
                .build();

        Measurement savedMeasurement = measurementRepository.save(measurement);
        return mapToResponse(savedMeasurement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeasurementResponse> getAllMeasurements() {
        User currentUser = getCurrentUser();
        return measurementRepository.findByUserIdOrderByMeasurementDateDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeasurementResponse> getRecentMeasurements(int limit) {
        User currentUser = getCurrentUser();
        return measurementRepository.findTop10ByUserIdOrderByMeasurementDateDesc(currentUser.getId())
                .stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MeasurementResponse getMeasurementById(Long id) {
        User currentUser = getCurrentUser();
        Measurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));

        if (!measurement.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(measurement);
    }

    @Override
    @Transactional
    public MeasurementResponse updateMeasurement(Long id, MeasurementRequest request) {
        User currentUser = getCurrentUser();
        Measurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));

        if (!measurement.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        measurement.setWeight(request.getWeight());
        measurement.setHeight(request.getHeight());
        measurement.setChestCircumference(request.getChestCircumference());
        measurement.setArmCircumference(request.getArmCircumference());
        measurement.setLegCircumference(request.getLegCircumference());
        measurement.setWaistCircumference(request.getWaistCircumference());
        if (request.getMeasurementDate() != null) {
            measurement.setMeasurementDate(request.getMeasurementDate());
        }

        Measurement updatedMeasurement = measurementRepository.save(measurement);
        return mapToResponse(updatedMeasurement);
    }

    @Override
    @Transactional
    public void deleteMeasurement(Long id) {
        User currentUser = getCurrentUser();
        Measurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));

        if (!measurement.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        measurementRepository.delete(measurement);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private MeasurementResponse mapToResponse(Measurement measurement) {
        return MeasurementResponse.builder()
                .id(measurement.getId())
                .userId(measurement.getUser().getId())
                .weight(measurement.getWeight())
                .height(measurement.getHeight())
                .chestCircumference(measurement.getChestCircumference())
                .armCircumference(measurement.getArmCircumference())
                .legCircumference(measurement.getLegCircumference())
                .waistCircumference(measurement.getWaistCircumference())
                .measurementDate(measurement.getMeasurementDate())
                .createdAt(measurement.getCreatedAt())
                .updatedAt(measurement.getUpdatedAt())
                .build();
    }
}
