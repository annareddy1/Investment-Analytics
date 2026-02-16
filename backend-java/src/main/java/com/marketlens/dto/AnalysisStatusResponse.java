package com.marketlens.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ✅ Analysis Job Status Response
 * - Used for polling endpoint /api/analysis/{id}/status
 * - Returns current state of analysis job
 * - Includes progress tracking and timestamps
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisStatusResponse {

    private String analysisId;
    
    private String ticker;
    
    private String period;
    
    /**
     * Job status: PROCESSING, COMPLETED, FAILED
     */
    private String status;
    
    /**
     * Progress percentage: 0-100
     */
    private Integer progress;
    
    /**
     * Human-readable message about current state
     */
    private String message;
    
    /**
     * When the job was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * When the job was last updated
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * When the job completed (success or failure)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
    
    /**
     * Error message (only present if status=FAILED)
     */
    private String errorMessage;
}
