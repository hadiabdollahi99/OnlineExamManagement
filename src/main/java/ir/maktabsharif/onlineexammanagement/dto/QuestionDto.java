package ir.maktabsharif.onlineexammanagement.dto;

import ir.maktabsharif.onlineexammanagement.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {

    private Long id;
    private String title;
    private String questionText;
    private QuestionType questionType;
    private Long teacherId;
    private Long courseId;

    private List<QuestionOptionDto> options = new ArrayList<>();
    private Boolean shuffleOptions = true;

    private String sampleAnswer;
    private Integer expectedAnswerLength;
}