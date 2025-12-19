package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student,Long> {
}
