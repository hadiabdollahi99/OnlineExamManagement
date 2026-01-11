package ir.maktabsharif.onlineexammanagement.mapper;

import ir.maktabsharif.onlineexammanagement.dto.ExamDto;
import ir.maktabsharif.onlineexammanagement.dto.StudentExamDto;
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

    public StudentExamDto toStudentExamDto(Exam exam) {
        return StudentExamDto.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .isActive(exam.getIsActive())
                .courseId(exam.getCourse().getId())
                .courseTitle(exam.getCourse().getTitle())
                .courseCode(exam.getCourse().getCourseCode())
                .teacherId(exam.getTeacher().getId())
                .teacherName(exam.getTeacher().getFirstName() + " " + exam.getTeacher().getLastName())
                .build();
    }
}
