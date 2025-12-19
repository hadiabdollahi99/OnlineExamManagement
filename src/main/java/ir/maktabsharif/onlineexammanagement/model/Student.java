package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Student extends User implements Serializable {
    private String studentNumber;

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private Set<StudentCourses> enrolledCourses = new HashSet<>();
}
