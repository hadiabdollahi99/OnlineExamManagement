package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.dto.ExamDto;
import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.Teacher;
import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.service.CourseService;
import ir.maktabsharif.onlineexammanagement.service.ExamService;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final ExamService examService;
    private final CourseService courseService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        List<Course> currentCourses = examService.getCoursesByTeacher(user.getId());
        List<Exam> allMyExams = examService.getAllExamsByTeacher(user.getId());
        List<Course> coursesWithMyExams = examService.getCoursesWithTeacherExams(user.getId());

        long totalExams = allMyExams.size();
        long activeExams = allMyExams.stream()
                .filter(Exam::getIsActive)
                .count();

        Map<Course, List<Exam>> examsByCourse = new HashMap<>();
        for (Exam exam : allMyExams) {
            examsByCourse
                    .computeIfAbsent(exam.getCourse(), k -> new ArrayList<>())
                    .add(exam);
        }


        List<Course> courses = courseService.findByTeacher((Teacher) user);

        Map<Long, Long> examCounts = new HashMap<>();
        for (Course course : courses) {
            Long count = examService.getExamCountByCourseAndTeacher(course.getId(), user.getId());
            examCounts.put(course.getId(), count);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("examCounts", examCounts);
        model.addAttribute("teacherName", user.getFirstName().concat(" " + user.getLastName()));
        model.addAttribute("currentCourses", currentCourses);
        model.addAttribute("allMyExams", allMyExams);
        model.addAttribute("coursesWithMyExams", coursesWithMyExams);
        model.addAttribute("examsByCourse", examsByCourse);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("activeExams", activeExams);

        return "teacher/dashboard";
    }

    @GetMapping("/my-exams")
    public String showMyExams(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        List<Exam> myExams = examService.getAllExamsByTeacher(user.getId());

        Map<Course, List<Exam>> examsGroupedByCourse = new HashMap<>();
        for (Exam exam : myExams) {
            examsGroupedByCourse
                    .computeIfAbsent(exam.getCourse(), k -> new ArrayList<>())
                    .add(exam);
        }

        model.addAttribute("myExams", myExams);
        model.addAttribute("examsGroupedByCourse", examsGroupedByCourse);

        return "teacher/my-exams";
    }

    @GetMapping("/my-exams/{examId}")
    public String viewMyExam(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/my-exams";
        }

        model.addAttribute("exam", exam);
        model.addAttribute("isOwner", true);

        return "teacher/exam-details";
    }

    @GetMapping("/my-exams/{examId}/edit")
    public String editMyExam(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/my-exams";
        }

        model.addAttribute("exam", exam);
        return "teacher/edit-exam";
    }


    @GetMapping("/courses")
    public String listCourses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        List<Course> courses = examService.getCoursesByTeacher(user.getId());

        model.addAttribute("courses", courses);
        return "teacher/courses";
    }


    @GetMapping("/courses/{courseId}/exams/new")
    public String showCreateExamForm(@PathVariable Long courseId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        model.addAttribute("exam", new ExamDto());
        model.addAttribute("course", course);
        return "teacher/create-exam";
    }


    @PostMapping("/courses/{courseId}/exams/new")
    public String createExam(@PathVariable Long courseId,
                             @ModelAttribute("exam") ExamDto examDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "لطفاً اطلاعات را صحیح وارد کنید");
            return "redirect:/teacher/courses/" + courseId + "/exams/new";
        }

        try {
            Exam createdExam = examService.createExam(examDto, courseId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "آزمون با موفقیت ایجاد شد");
            return "redirect:/teacher/questions/exam/" + createdExam.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/exams/new";
        }


    }


    @GetMapping("/courses/{courseId}/exams")
    public String listExams(@PathVariable Long courseId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        boolean isCurrentTeacher = course.getTeacher() != null &&
                course.getTeacher().getId().equals(user.getId());

        List<Exam> allExams = examService.getAllExamsByCourse(courseId);

        List<Exam> myExams = examService.getExamsByCourseAndTeacher(courseId, user.getId());

        List<User> examTeachers = examService.getTeachersWhoCreatedExams(courseId);

        model.addAttribute("course", course);
        model.addAttribute("allExams", allExams);
        model.addAttribute("myExams", myExams);
        model.addAttribute("examTeachers", examTeachers);
        model.addAttribute("isCurrentTeacher", isCurrentTeacher);
        model.addAttribute("currentTeacherId", user.getId());

        return "teacher/exams";
    }

    @GetMapping("/exams/{examId}")
    public String viewExam(@PathVariable Long examId, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        Course course = exam.getCourse();
        boolean hasAccess = false;

        if (course.getTeacher() != null && course.getTeacher().getId().equals(user.getId())) {
            hasAccess = true;
        }

        if (exam.getTeacher().getId().equals(user.getId())) {
            hasAccess = true;
        }

        List<User> examTeachers = examService.getTeachersWhoCreatedExams(course.getId());
        boolean isPreviousTeacher = examTeachers.stream()
                .anyMatch(t -> t.getId().equals(user.getId()));

        if (isPreviousTeacher) {
            hasAccess = true;
        }

        if (!hasAccess) {
            return "redirect:/teacher/courses";
        }

        boolean isOwner = examService.isExamOwner(examId, user.getId());
        boolean canEdit = isOwner;

        model.addAttribute("exam", exam);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("currentTeacherId", user.getId());

        return "teacher/exam-details";
    }

    @GetMapping("/exams/{examId}/edit")
    public String showEditExamForm(@PathVariable Long examId, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!examService.isExamOwner(examId, user.getId())) {
            return "redirect:/teacher/exams/" + examId;
        }

        model.addAttribute("exam", exam);
        return "teacher/edit-exam";
    }

    @PostMapping("/exams/{examId}/edit")
    public String updateExam(@PathVariable Long examId,
                             @ModelAttribute("exam") ExamDto examDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "لطفاً اطلاعات را صحیح وارد کنید");
            return "redirect:/teacher/exams/" + examId + "/edit";
        }

        try {
            examService.updateExam(examId, examDto, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "آزمون با موفقیت ویرایش شد");
            return "redirect:/teacher/exams/" + examId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/exams/" + examId + "/edit";
        }
    }

    @PostMapping("/exams/{examId}/delete")
    public String deleteExam(@PathVariable Long examId,
                             RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null){
            return "redirect:/login";
        }

        try {
            Exam exam = examService.getExamById(examId);

            examService.deleteExam(examId, user.getId());

            redirectAttributes.addFlashAttribute("successMessage", "آزمون با موفقیت حذف شد");
            return "redirect:/teacher/courses/" + exam.getCourse().getId() + "/exams";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/teacher/exams/" + examId;
        }
    }

    @GetMapping("/exams/{examId}/questions")
    public String redirectToManageQuestions(@PathVariable Long examId) {
        return "redirect:/teacher/questions/exam/" + examId;
    }
}