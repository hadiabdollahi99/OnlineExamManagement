package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "courses")
public class Course extends BaseEntity<Long> implements Serializable {

    @Column(unique = true, nullable = false)
    private String title;

    @Column(unique = true)
    private String courseCode;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private Set<StudentCourses> students = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<Exam> courseExams = new ArrayList<>();


    public void generateCourseCode() {
        if (courseCode == null) {
            String prefix = title.substring(0, Math.min(3, title.length())).toUpperCase();
            String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
            this.courseCode = prefix + "-" + timestamp;
        }
    }
}