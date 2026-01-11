package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.ExamQuestion;
import ir.maktabsharif.onlineexammanagement.model.StudentAnswer;
import ir.maktabsharif.onlineexammanagement.model.StudentExamParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByParticipation(StudentExamParticipation participation);

    Optional<StudentAnswer> findByParticipationAndExamQuestion(
            StudentExamParticipation participation,
            ExamQuestion examQuestion
    );

}
