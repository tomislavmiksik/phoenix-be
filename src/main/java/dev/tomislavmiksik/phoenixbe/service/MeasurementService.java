package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementRequest;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementResponse;

import java.util.List;

/**
 * The interface of Measurement service.
 */
public interface MeasurementService {

    /**
     * Create measurement measurement response.
     *
     * @param request the request
     * @return the measurement response
     */
    MeasurementResponse createMeasurement(MeasurementRequest request);

    /**
     * Gets all measurements.
     *
     * @return the all measurements
     */
    List<MeasurementResponse> getAllMeasurements();

    /**
     * Gets recent measurements.
     *
     * @param limit the limit
     * @return the recent measurements
     */
    List<MeasurementResponse> getRecentMeasurements(int limit);

    /**
     * Gets measurement by id.
     *
     * @param id the id
     * @return the measurement by id
     */
    MeasurementResponse getMeasurementById(Long id);

    /**
     * Update measurement measurement response.
     *
     * @param id      the id
     * @param request the request
     * @return the measurement response
     */
    MeasurementResponse updateMeasurement(Long id, MeasurementRequest request);

    /**
     * Delete measurement.
     *
     * @param id the id
     */
    void deleteMeasurement(Long id);
}
