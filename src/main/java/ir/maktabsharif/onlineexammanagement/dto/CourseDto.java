package ir.maktabsharif.onlineexammanagement.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private String title;
    private String courseCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Long teacherId;
}
