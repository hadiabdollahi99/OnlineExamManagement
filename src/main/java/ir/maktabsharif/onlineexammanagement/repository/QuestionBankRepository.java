package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    boolean existsByCourseAndQuestion(Course course, BaseQuestion question);

    @Query("SELECT qb FROM QuestionBank qb WHERE " +
            "qb.question.id = :questionId")
    QuestionBank findByQuestionId(@Param("questionId") Long questionId);
}