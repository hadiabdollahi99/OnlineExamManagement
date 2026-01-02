package ir.maktabsharif.onlineexammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDto {

    private Long id;
    private String optionText;
    private Integer orderIndex;
    private Boolean isCorrect = false;
}