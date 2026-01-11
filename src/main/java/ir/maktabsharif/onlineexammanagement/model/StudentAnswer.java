package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer extends BaseEntity<Long> implements Serializable {

    @ManyToOne
    @JoinColumn(name = "participation_id", nullable = false)
    private StudentExamParticipation participation;

    @ManyToOne
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    @Column(length = 2000)
    private String answerText;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    private Double score = 0.0;
    private Boolean isAutoSaved = false;
    private LocalDateTime answeredAt;
    private Boolean isGraded = false;
}