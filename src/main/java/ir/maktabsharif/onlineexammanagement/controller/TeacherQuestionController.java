package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.dto.QuestionDto;
import ir.maktabsharif.onlineexammanagement.dto.QuestionOptionDto;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.QuestionType;
import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.service.ExamQuestionService;
import ir.maktabsharif.onlineexammanagement.service.ExamService;
import ir.maktabsharif.onlineexammanagement.service.QuestionService;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/teacher/questions")
@RequiredArgsConstructor
public class TeacherQuestionController {

    private final ExamQuestionService examQuestionService;
    private final QuestionService questionService;
    private final ExamService examService;
    private final UserService userService;

    @GetMapping("/exam/{examId}")
    public String manageExamQuestions(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        var examQuestions = examQuestionService.getExamQuestions(examId);
        Double totalScore = examQuestionService.calculateExamTotalScore(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("examQuestions", examQuestions);
        model.addAttribute("totalScore", totalScore);
        model.addAttribute("questionTypes", QuestionType.values());

        return "teacher/manage-exam-questions";
    }

    @GetMapping("/exam/{examId}/add-from-bank")
    public String showAddFromBank(@PathVariable Long examId, Model model,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(required = false) QuestionType questionType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        List<QuestionDto> bankQuestions = questionService.searchQuestions(
                user.getId(),
                exam.getCourse().getId(),
                title,
                questionType
        );

        model.addAttribute("exam", exam);
        model.addAttribute("bankQuestions", bankQuestions);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchQuestionType", questionType);

        return "teacher/add-question-from-bank";
    }

    @GetMapping("/exam/{examId}/create-new/select-type")
    public String selectQuestionType(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        model.addAttribute("exam", exam);
        model.addAttribute("questionTypes", QuestionType.values());

        return "teacher/select-question-type";
    }

    @GetMapping("/exam/{examId}/create-new/multiple-choice")
    public String showCreateMultipleChoice(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        QuestionDto question = QuestionDto.builder()
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(new ArrayList<>())
                .build();

        question.getOptions().add(QuestionOptionDto.builder()
                .orderIndex(1)
                .optionText("")
                .isCorrect(false)
                .build());
        question.getOptions().add(QuestionOptionDto.builder()
                .orderIndex(2)
                .optionText("")
                .isCorrect(false)
                .build());

        model.addAttribute("exam", exam);
        model.addAttribute("question", question);

        return "teacher/create-multiple-choice-question";
    }

    @GetMapping("/exam/{examId}/create-new/descriptive")
    public String showCreateDescriptive(@PathVariable Long examId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (!exam.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        QuestionDto question = QuestionDto.builder()
                .questionType(QuestionType.DESCRIPTIVE)
                .build();

        model.addAttribute("exam", exam);
        model.addAttribute("question", question);

        return "teacher/create-descriptive-question";
    }

    @PostMapping("/exam/{examId}/add-from-bank")
    public String addQuestionFromBank(@PathVariable Long examId,
                                      @RequestParam Long questionId,
                                      @RequestParam(defaultValue = "1.0") Double score,
                                      RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            examQuestionService.addQuestionFromBank(examId, questionId, score);
            redirectAttributes.addFlashAttribute("successMessage", "سوال با موفقیت اضافه شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/questions/exam/" + examId;
    }

    @PostMapping("/exam/{examId}/create-new/multiple-choice")
    public String createMultipleChoiceQuestion(@PathVariable Long examId,
                                               @ModelAttribute QuestionDto questionDto,
                                               @RequestParam(defaultValue = "1.0") Double score,
                                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            questionDto.setQuestionType(QuestionType.MULTIPLE_CHOICE);

            boolean hasCorrectOption = questionDto.getOptions().stream()
                    .anyMatch(QuestionOptionDto::getIsCorrect);

            if (!hasCorrectOption) {
                throw new RuntimeException("حداقل یک گزینه باید صحیح باشد");
            }

            examQuestionService.addNewQuestionToExam(examId, questionDto, score);
            redirectAttributes.addFlashAttribute("successMessage", "سوال چندگزینه‌ای ایجاد و اضافه شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/questions/exam/" + examId;
    }

    @PostMapping("/exam/{examId}/create-new/descriptive")
    public String createDescriptiveQuestion(@PathVariable Long examId,
                                            @ModelAttribute QuestionDto questionDto,
                                            @RequestParam(defaultValue = "1.0") Double score,
                                            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            questionDto.setQuestionType(QuestionType.DESCRIPTIVE);

            examQuestionService.addNewQuestionToExam(examId, questionDto, score);
            redirectAttributes.addFlashAttribute("successMessage", "سوال تشریحی ایجاد و اضافه شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/questions/exam/" + examId;
    }

    @PostMapping("/exam-question/{examQuestionId}/update-score")
    public String updateQuestionScore(@PathVariable Long examQuestionId,
                                      @RequestParam Double newScore,
                                      RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            examQuestionService.updateQuestionScore(examQuestionId, newScore);
            redirectAttributes.addFlashAttribute("successMessage", "نمره سوال به‌روز شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/questions/exam/" + examQuestionService.getExamIdFromExamQuestionId(examQuestionId);
    }

    @PostMapping("/exam-question/{examQuestionId}/delete")
    public String deleteQuestionFromExam(@PathVariable Long examQuestionId,
                                         RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Long examId = examQuestionService.getExamIdFromExamQuestionId(examQuestionId);
            examQuestionService.deleteQuestionFromExam(examQuestionId);
            redirectAttributes.addFlashAttribute("successMessage", "سوال از آزمون حذف شد");
            return "redirect:/teacher/questions/exam/" + examId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
            return "redirect:/teacher/dashboard";
        }
    }
}