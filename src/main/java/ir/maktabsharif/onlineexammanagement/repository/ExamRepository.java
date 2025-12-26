package ir.maktabsharif.onlineexammanagement.repository;


import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourse(Course course);
    List<Exam> findByTeacher(User teacher);

    @Query("SELECT e FROM Exam e WHERE " +
                        " e.course.id = :courseId and e.teacher.id = :teacherId ")
    List<Exam> findByCourseIdAndTeacherId(@Param("courseId") Long courseId,
                                          @Param("teacherId") Long teacherId);

}