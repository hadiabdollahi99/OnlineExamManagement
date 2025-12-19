package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.Student;
import ir.maktabsharif.onlineexammanagement.model.StudentCourses;
import ir.maktabsharif.onlineexammanagement.repository.StudentCoursesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCoursesService {
    private final StudentCoursesRepository studentCoursesRepository;

    public boolean existsByStudentAndCourse(Student student, Course course){
        return studentCoursesRepository.existsByStudentAndCourse(student,course);
    }


    public void deleteByCourseAndStudent(Course course, Student student){
        studentCoursesRepository.deleteByCourseAndStudent(course,student);
    }

    public List<Student> findCourseStudents(Course course){
        return studentCoursesRepository.findCourseStudents(course);
    }

    public void save(StudentCourses studentCourses){
        studentCoursesRepository.save(studentCourses);
    }
}
