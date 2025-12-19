package ir.maktabsharif.onlineexammanagement.repository;


import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher(Teacher teacher);
    long count();

    @Query("SELECT c FROM Course c WHERE " +
            "(:title IS NULL OR c.title LIKE %:title%) AND " +
            "(:courseCode IS NULL OR c.courseCode LIKE %:courseCode%)")
    List<Course> searchCourses(@Param("title") String title,
                               @Param("courseCode") String courseCode);

}