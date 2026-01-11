package ir.maktabsharif.onlineexammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionForStudentDto {

    private Long id;
    private Long examId;
    private Double defaultScore;
    private Integer questionOrder;

    private QuestionDto question;
    private String studentAnswer;
    private Long selectedOptionId;

    private List<QuestionOptionDto> options;

    private Boolean isMultipleChoice;
    private Boolean isDescriptive;
}