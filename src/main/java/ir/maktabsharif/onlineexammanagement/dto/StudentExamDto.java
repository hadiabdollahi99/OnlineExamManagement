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
public class StudentExamDto {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Boolean isActive;

    private Long courseId;
    private String courseTitle;
    private String courseCode;

    private Long teacherId;
    private String teacherName;

    private Boolean canTakeExam;
    private Boolean hasTakenExam;
    private LocalDateTime takenAt;
    private Double score;

    private Integer questionCount;
    private Double totalScore;
}
