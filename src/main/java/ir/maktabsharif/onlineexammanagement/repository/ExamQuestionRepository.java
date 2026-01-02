package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.BaseQuestion;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.ExamQuestion;
import ir.maktabsharif.onlineexammanagement.model.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExam(Exam exam);

    boolean existsByExamAndQuestion(Exam exam, BaseQuestion question);

    @Query("SELECT DISTINCT eq FROM ExamQuestion eq " +
            "LEFT JOIN FETCH eq.question q " +
            "LEFT JOIN FETCH q.teacher " +
            "LEFT JOIN FETCH q.course " +
            "WHERE eq.exam.id = :examId " +
            "ORDER BY eq.questionOrder")
    List<ExamQuestion> findByExamIdWithQuestion(@Param("examId") Long examId);


    @Query("SELECT eq FROM ExamQuestion eq WHERE " +
            "eq.question.id = :questionId")
    ExamQuestion findByQuestionId(@Param("questionId") Long questionId);
}