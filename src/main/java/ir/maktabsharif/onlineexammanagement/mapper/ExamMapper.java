package ir.maktabsharif.onlineexammanagement.mapper;

import ir.maktabsharif.onlineexammanagement.dto.ExamDto;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper {
    public Exam toEntity(ExamDto dto) {
        if (dto == null) {
            return null;
        }
        return Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMinutes(dto.getDurationMinutes())
                .isActive(dto.getIsActive())
                .build();
    }
}
