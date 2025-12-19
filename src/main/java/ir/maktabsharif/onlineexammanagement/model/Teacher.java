package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class Teacher extends User implements Serializable {
    private String teacherNumber;

    @OneToMany(mappedBy = "teacher")
    private Set<Course> taughtCourses = new HashSet<>();
}
