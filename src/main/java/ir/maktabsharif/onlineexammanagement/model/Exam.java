package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "exams")
public class Exam extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_score")
    private Double totalScore = 0.0;

    @Column(name = "is_active")
    private Boolean isActive = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;


    @OneToMany(mappedBy = "exam", cascade = CascadeType.REMOVE)
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    public Double getTotalScore() {
        if (examQuestions == null || examQuestions.isEmpty()) {
            return 0.0;
        }
        return examQuestions.stream()
                .mapToDouble(ExamQuestion::getDefaultScore)
                .sum();
    }
}