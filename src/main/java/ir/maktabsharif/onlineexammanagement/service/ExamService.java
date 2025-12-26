package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.ExamDto;
import ir.maktabsharif.onlineexammanagement.exception.CourseNotFoundException;
import ir.maktabsharif.onlineexammanagement.exception.ExamNotFoundException;
import ir.maktabsharif.onlineexammanagement.exception.UserForbiddenException;
import ir.maktabsharif.onlineexammanagement.exception.UserNotFoundException;
import ir.maktabsharif.onlineexammanagement.mapper.ExamMapper;
import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.Teacher;
import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.repository.CourseRepository;
import ir.maktabsharif.onlineexammanagement.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final ExamMapper examMapper;

    @Transactional
    public Exam createExam(ExamDto examDto, Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        User teacher = userService.getUserById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("استاد یافت نشد"));

        Exam exam = examMapper.toEntity(examDto);

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new UserForbiddenException("شما استاد این دوره نیستید");
        }

        exam.setCourse(course);
        exam.setTeacher(teacher);

        return examRepository.save(exam);
    }


    public List<Exam> getAllExamsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        return examRepository.findByCourse(course);
    }

    public List<Exam> getExamsByCourseAndTeacher(Long courseId, Long teacherId) {
        return examRepository.findByCourseIdAndTeacherId(courseId, teacherId);
    }


    public boolean isExamOwner(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        return exam.getTeacher().getId().equals(teacherId);
    }

    public List<User> getTeachersWhoCreatedExams(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        List<Exam> exams = examRepository.findByCourse(course);
        return exams.stream()
                .map(Exam::getTeacher)
                .distinct()
                .toList();
    }

    @Transactional
    public Exam updateExam(Long examId, ExamDto examDto, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        if (!exam.getTeacher().getId().equals(teacherId)) {
            throw new UserForbiddenException("شما صاحب این آزمون نیستید");
        }

        exam.setTitle(examDto.getTitle());
        exam.setDescription(examDto.getDescription());
        exam.setDurationMinutes(examDto.getDurationMinutes());
        exam.setIsActive(examDto.getIsActive());

        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        if (!exam.getTeacher().getId().equals(teacherId)) {
            throw new UserForbiddenException("شما صاحب این آزمون نیستید");
        }

        examRepository.delete(exam);
    }


    public Exam getExamById(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));
    }

    public List<Course> getCoursesByTeacher(Long teacherId) {
        User teacher = userService.getUserById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("استاد یافت نشد"));

        return courseRepository.findByTeacher((Teacher) teacher);
    }


    public List<Exam> getAllExamsByTeacher(Long teacherId) {
        User teacher = userService.getUserById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("استاد یافت نشد"));

        return examRepository.findByTeacher(teacher);
    }

    public List<Course> getCoursesWithTeacherExams(Long teacherId) {
        User teacher = userService.getUserById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("استاد یافت نشد"));

        List<Exam> teacherExams = examRepository.findByTeacher(teacher);

        return teacherExams.stream()
                .map(Exam::getCourse)
                .distinct()
                .toList();
    }

    public long getExamCountByCourseAndTeacher(Long courseId, Long teacherId) {
        List<Exam> exams = examRepository.findByCourseIdAndTeacherId(courseId, teacherId);
        return exams.size();
    }
}