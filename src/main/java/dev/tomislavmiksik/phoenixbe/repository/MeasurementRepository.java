package dev.tomislavmiksik.phoenixbe.repository;

import dev.tomislavmiksik.phoenixbe.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findByUserIdOrderByMeasurementDateDesc(Long userId);

    List<Measurement> findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Measurement> findTop10ByUserIdOrderByMeasurementDateDesc(Long userId);
}
