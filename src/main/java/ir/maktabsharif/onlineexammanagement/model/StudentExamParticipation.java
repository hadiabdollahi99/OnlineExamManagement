package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_exam_participation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamParticipation extends BaseEntity<Long> implements Serializable {
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Boolean isCompleted = false;
    private Boolean autoSubmitted = false;
    private Double totalScore = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExamParticipationStatus status = ExamParticipationStatus.NOT_STARTED;
}