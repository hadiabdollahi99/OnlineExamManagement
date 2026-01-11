package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSession extends BaseEntity<Long> implements Serializable {
    @Column(unique = true)
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "participation_id", nullable = false)
    private StudentExamParticipation participation;

    @Column(length = 5000)
    private String answersData;
    private Integer currentQuestionIndex = 0;
    private Integer remainingTime;
    private LocalDateTime lastActivity;
    private Boolean isActive = true;
}
