package ir.maktabsharif.onlineexammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDto {

    private Long id;
    private Long examId;
    private Long questionId;
    private Double defaultScore;
    private Integer questionOrder;
    private Boolean isRequired = true;

    private QuestionDto question;

}