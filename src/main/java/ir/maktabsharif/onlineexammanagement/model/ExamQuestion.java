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
@Table(name = "exam_questions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "question_id"})
})
public class ExamQuestion extends BaseEntity<Long>{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private BaseQuestion question;

    @Column(nullable = false)
    private Double defaultScore = 1.0;

    private Integer questionOrder = 0;

    private Boolean isRequired = true;
}