package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
public class Student extends User implements Serializable {
    private String studentNumber;

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private List<StudentCourses> enrolledCourses = new ArrayList<>();
}
