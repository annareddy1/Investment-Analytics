package com.marketlens.repository;

import com.marketlens.model.AnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends MongoRepository<AnalysisResult, String> {
    Optional<AnalysisResult> findByAnalysisId(String analysisId);
}