package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentCoursesRepository extends JpaRepository<StudentCourses,Long> {

    boolean existsByStudentAndCourse(Student student, Course course);

    void deleteByCourseAndStudent(Course course, Student student);

    @Query("SELECT s.student FROM StudentCourses s WHERE " +
            "s.course = :course")
    List<Student> findCourseStudents (@Param("course") Course course);

}
