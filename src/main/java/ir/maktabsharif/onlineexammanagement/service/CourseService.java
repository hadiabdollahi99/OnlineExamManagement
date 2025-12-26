package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.exception.CourseNotFoundException;
import ir.maktabsharif.onlineexammanagement.exception.StudentCourseExistException;
import ir.maktabsharif.onlineexammanagement.exception.UserNotFoundException;
import ir.maktabsharif.onlineexammanagement.exception.UserRoleException;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final StudentCoursesService studentCoursesService;

    @Transactional
    public Course createCourse(Course course) {
        course.generateCourseCode();
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, Course courseDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());

        return courseRepository.save(course);
    }

    @Transactional
    public void addTeacherToCourse(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("استاد یافت نشد"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new UserRoleException("کاربر انتخاب شده استاد نیست");
        }

        course.setTeacher(teacher);
        courseRepository.save(course);
    }

    @Transactional
    public void addStudentToCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("دانشجو یافت نشد"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new UserRoleException("کاربر انتخاب شده دانشجو نیست");
        }

        if (studentCoursesService.existsByStudentAndCourse(student, course)){
            throw new StudentCourseExistException("دانشجو قبلا در دوره ثبت نام شده است");
        }

        studentCoursesService.save(new StudentCourses(student,course));
    }

    @Transactional
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("دانشجو یافت نشد"));

        studentCoursesService.deleteByCourseAndStudent(course,student);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));
    }

    public List<Course> searchCourses(String title, String courseCode){
        return courseRepository.searchCourses(title, courseCode);
    }

    public List<Student> getCourseStudents(Long courseId) {
        Course course = getCourseById(courseId);
        return studentCoursesService.findCourseStudents(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    @Transactional
    public List<Course> findByTeacher(Teacher teacher){
        return courseRepository.findByTeacher(teacher);
    }

    public long getTotalCount() {
        return courseRepository.count();
    }
}