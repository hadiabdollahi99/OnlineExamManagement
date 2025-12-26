package ir.maktabsharif.onlineexammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {

    private String title;
    private String description;
    private Integer durationMinutes;
    private Boolean isActive = true;
    private Long courseId;
    private String courseTitle;
    private String courseCode;
    private Long teacherId;
    private String teacherName;
}
