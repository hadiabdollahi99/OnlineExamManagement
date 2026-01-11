package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.dto.ExamParticipationDto;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/grading")
@RequiredArgsConstructor
public class TeacherGradingController {

    private final TeacherGradingService teacherGradingService;
    private final ExamService examService;
    private final UserService userService;

    @GetMapping("/exams")
    public String listExamsForGrading(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Exam> exams = examService.getAllExamsByTeacher(user.getId());

        model.addAttribute("exams", exams);
        return "teacher/grading-exams";
    }

    @GetMapping("/exam/{examId}/participants")
    public String viewParticipants(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<ExamParticipationDto> participants =
                teacherGradingService.getExamParticipants(examId, user.getId());

        Map<String, Object> statistics =
                teacherGradingService.getExamStatistics(examId, user.getId());

        Exam exam = examService.getExamById(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("participants", participants);
        model.addAttribute("statistics", statistics);

        return "teacher/exam-participants";
    }

    @GetMapping("/participation/{participationId}/grade")
    public String gradeStudentAnswers(@PathVariable Long participationId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<StudentAnswer> answers = teacherGradingService.getStudentAnswers(participationId, user.getId());

        List<StudentAnswer> descriptiveAnswers = answers.stream()
                .filter(answer -> answer.getExamQuestion().getQuestion().getQuestionType() == QuestionType.DESCRIPTIVE)
                .filter(answer -> !answer.getIsGraded())
                .toList();

        model.addAttribute("answers", descriptiveAnswers);
        model.addAttribute("participationId", participationId);

        return "teacher/grade-student-answers";
    }

    @PostMapping("/answer/{answerId}/grade")
    public String saveGrade(@PathVariable Long answerId,
                            @RequestParam Double score,
                            @RequestParam Long participationId,
                            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            teacherGradingService.gradeDescriptiveAnswer(answerId, user.getId(), score);
            redirectAttributes.addFlashAttribute("successMessage", "نمره با موفقیت ثبت شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/grading/participation/" + participationId + "/grade";
    }
}