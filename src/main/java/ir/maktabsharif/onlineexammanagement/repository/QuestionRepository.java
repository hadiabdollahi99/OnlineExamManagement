package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.BaseQuestion;
import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.QuestionType;
import ir.maktabsharif.onlineexammanagement.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<BaseQuestion, Long> {

    @Query("SELECT q FROM BaseQuestion q WHERE " +
            "q.teacher.id = :teacherId AND " +
            "q.course.id = :courseId AND " +
            "(:title IS NULL OR q.title LIKE %:title%) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType)")
    List<BaseQuestion> searchQuestions(
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId,
            @Param("title") String title,
            @Param("questionType") QuestionType questionType);
}