package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "descriptive_questions")
@PrimaryKeyJoinColumn(name = "question_id")
public class DescriptiveQuestion extends BaseQuestion {

    @Column(length = 2000)
    private String sampleAnswer;
    private Integer expectedAnswerLength;

}