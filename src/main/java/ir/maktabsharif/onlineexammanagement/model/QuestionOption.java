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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "question_options")
public class QuestionOption extends BaseEntity<Long>{

    @Column(nullable = false, length = 500)
    private String optionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestion question;

    private Integer orderIndex = 0;
    private Boolean isCorrect = false;
}