package dev.tomislavmiksik.phoenixbe.controller;

import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementRequest;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementResponse;
import dev.tomislavmiksik.phoenixbe.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @PostMapping
    public ResponseEntity<?> createMeasurement(@Valid @RequestBody MeasurementRequest request) {
        try {
            MeasurementResponse response = measurementService.createMeasurement(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MeasurementResponse>> getAllMeasurements() {
        List<MeasurementResponse> measurements = measurementService.getAllMeasurements();
        return ResponseEntity.ok(measurements);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MeasurementResponse>> getRecentMeasurements(
            @RequestParam(defaultValue = "10") int limit) {
        List<MeasurementResponse> measurements = measurementService.getRecentMeasurements(limit);
        return ResponseEntity.ok(measurements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMeasurementById(@PathVariable Long id) {
        try {
            MeasurementResponse response = measurementService.getMeasurementById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeasurement(
            @PathVariable Long id,
            @Valid @RequestBody MeasurementRequest request) {
        try {
            MeasurementResponse response = measurementService.updateMeasurement(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeasurement(@PathVariable Long id) {
        try {
            measurementService.deleteMeasurement(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
