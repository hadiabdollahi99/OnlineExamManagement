package ir.maktabsharif.onlineexammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamParticipationDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long examId;
    private String examTitle;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Boolean isCompleted;
    private Boolean autoSubmitted;

    private Double totalScore;
    private Double maxScore;
    private String status;

    private Integer timeSpentMinutes;
    private Boolean isGraded;
}