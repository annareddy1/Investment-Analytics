package com.marketlens.repository;

import com.marketlens.model.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {
    Optional<AnalysisRun> findTopByTickerAndStatusOrderByCreatedAtDesc(
            String ticker, 
            AnalysisRun.AnalysisStatus status
    );
}