package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.dto.QuestionDto;
import ir.maktabsharif.onlineexammanagement.model.BaseQuestion;
import ir.maktabsharif.onlineexammanagement.model.Course;
import ir.maktabsharif.onlineexammanagement.model.QuestionType;
import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.service.CourseService;
import ir.maktabsharif.onlineexammanagement.service.QuestionService;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/question-bank")
@RequiredArgsConstructor
public class TeacherQuestionBankController {

    private final QuestionService questionService;
    private final CourseService courseService;
    private final UserService userService;

    @GetMapping("/course/{courseId}")
    public String viewQuestionBank(@PathVariable Long courseId,
                                   @RequestParam(required = false) String title,
                                   @RequestParam(required = false) QuestionType questionType,
                                   Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);

        if (!course.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/courses";
        }

        var questions = questionService.searchQuestions(
                user.getId(),
                courseId,
                title,
                questionType
        );

        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchQuestionType", questionType);

        return "teacher/question-bank";
    }

    @GetMapping("/question/{questionId}/edit")
    public String editQuestionForm(@PathVariable Long questionId,
                                   @RequestParam Long courseId,
                                   Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        BaseQuestion originalQuestion = questionService.getQuestionById(questionId);

        if (!originalQuestion.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/question-bank/course/" + courseId;
        }

        QuestionDto questionDto = questionService.convertToDto(originalQuestion);
        questionDto.setId(null);

        model.addAttribute("course", course);
        model.addAttribute("originalQuestion", originalQuestion);
        model.addAttribute("question", questionDto);

        if (originalQuestion.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            return "teacher/edit-multiple-choice-question";
        } else {
            return "teacher/edit-descriptive-question";
        }
    }

    @PostMapping("/question/{originalQuestionId}/save-copy")
    public String saveQuestionCopy(@PathVariable Long originalQuestionId,
                                   @ModelAttribute QuestionDto questionDto,
                                   @RequestParam Long courseId,
                                   @RequestParam(defaultValue = "false") Boolean keepOriginal,
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }


        try {
            BaseQuestion question = questionService.getQuestionById(originalQuestionId);

            questionDto.setQuestionType(question.getQuestionType());

            BaseQuestion newQuestion = questionService.createQuestion(
                    questionDto,
                    user.getId(),
                    courseId
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "سوال ویرایش شده با موفقیت به بانک اضافه شد");

            if (!keepOriginal) {
                questionService.deleteQuestion(originalQuestionId);
                redirectAttributes.addFlashAttribute("infoMessage",
                        "سوال اصلی حذف شد");
            }

            return "redirect:/teacher/question-bank/course/" + courseId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
            return "redirect:/teacher/question-bank/question/" + originalQuestionId + "/edit?courseId=" + courseId;
        }
    }

    @PostMapping("/question/{questionId}/delete")
    public String deleteQuestionFromBank(@PathVariable Long questionId,
                                         @RequestParam Long courseId,
                                         RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            questionService.deleteQuestion(questionId);
            redirectAttributes.addFlashAttribute("successMessage", "سوال با موفقیت حذف شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "خطا: " + e.getMessage());
        }

        return "redirect:/teacher/question-bank/course/" + courseId;
    }

    @GetMapping("/question/{questionId}")
    public String viewQuestionDetails(@PathVariable Long questionId,
                                      @RequestParam Long courseId,
                                      Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        BaseQuestion question = questionService.getQuestionById(questionId);

        if (!question.getTeacher().getId().equals(user.getId())) {
            return "redirect:/teacher/question-bank/course/" + courseId;
        }

        model.addAttribute("course", course);
        model.addAttribute("question", question);

        return "teacher/question-details";
    }
}