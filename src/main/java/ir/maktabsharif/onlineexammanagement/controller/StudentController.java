package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.dto.StudentExamDto;
import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.StudentExamParticipation;
import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.repository.StudentAnswerRepository;
import ir.maktabsharif.onlineexammanagement.repository.StudentExamParticipationRepository;
import ir.maktabsharif.onlineexammanagement.service.CourseService;
import ir.maktabsharif.onlineexammanagement.service.StudentExamService;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentExamService studentExamService;
    private final UserService userService;
    private final CourseService courseService;
    private final StudentExamParticipationRepository studentExamParticipationRepository;
    private final StudentAnswerRepository studentAnswerRepository;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Course> courses = studentExamService.getStudentCourses(user.getId());

        model.addAttribute("studentName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("courses", courses);

        return "student/dashboard";
    }

    @GetMapping("/courses")
    public String listCourses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Course> courses = studentExamService.getStudentCourses(user.getId());

        model.addAttribute("courses", courses);
        return "student/courses";
    }

    @GetMapping("/courses/{courseId}/exams")
    public String listExams(@PathVariable Long courseId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        List<StudentExamDto> exams = studentExamService.getAvailableExams(user.getId(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("exams", exams);

        return "student/exams";
    }

    @PostMapping("/exams/{examId}/start")
    public String startExam(@PathVariable Long examId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            StudentExamParticipation participation = studentExamService.startExam(user.getId(), examId);

            return "redirect:/student/exam/" + participation.getId() + "/take";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
            return "redirect:/student/courses";
        }
    }

    @GetMapping("/exam/{participationId}/take")
    public String takeExam(@PathVariable Long participationId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            var questions = studentExamService.getExamQuestionsForStudent(participationId, user.getId());
            Integer remainingTime = studentExamService.getRemainingTime(participationId);

            model.addAttribute("participationId", participationId);
            model.addAttribute("questions", questions);
            model.addAttribute("remainingTime", remainingTime);
            model.addAttribute("totalQuestions", questions.size());

            return "student/take-exam";

        } catch (Exception e) {
            return "redirect:/student/courses";
        }
    }

    @PostMapping("/exam/{participationId}/save-answer")
    @ResponseBody
    public String saveAnswer(@PathVariable Long participationId,
                             @RequestParam Long questionId,
                             @RequestParam(required = false) String answerText,
                             @RequestParam(required = false) Long selectedOptionId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "error";
        }

        try {
            studentExamService.saveAnswer(participationId, user.getId(),
                    questionId, answerText, selectedOptionId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/exam/{participationId}/submit")
    public String submitExam(@PathVariable Long participationId,
                             RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            studentExamService.submitExam(participationId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "آزمون با موفقیت ارسال شد");

            return "redirect:/student/courses";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
            return "redirect:/student/exam/" + participationId + "/take";
        }
    }

}