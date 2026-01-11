package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.Student;
import ir.maktabsharif.onlineexammanagement.model.StudentExamParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentExamParticipationRepository extends JpaRepository<StudentExamParticipation, Long> {

    Optional<StudentExamParticipation> findByStudentAndExam(Student student, Exam exam);

    @Query("SELECT p FROM StudentExamParticipation p WHERE " +
            "p.exam.id = :examId AND p.status IN ('IN_PROGRESS', 'COMPLETED')")
    List<StudentExamParticipation> findActiveByExamId(@Param("examId") Long examId);
}
