package com.marketlens.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String ticker;
    
    @Column(nullable = false)
    private String period;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "progress")
    private Integer progress;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AnalysisStatus.PROCESSING;
        }
        if (progress == null) {
            progress = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AnalysisStatus {
        PROCESSING,
        COMPLETED,
        FAILED
    }
}