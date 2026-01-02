package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.ExamQuestionDto;
import ir.maktabsharif.onlineexammanagement.dto.QuestionDto;
import ir.maktabsharif.onlineexammanagement.exception.ExamNotFoundException;
import ir.maktabsharif.onlineexammanagement.exception.QuestionNotFoundException;
import ir.maktabsharif.onlineexammanagement.mapper.QuestionMapper;
import ir.maktabsharif.onlineexammanagement.model.BaseQuestion;
import ir.maktabsharif.onlineexammanagement.model.Exam;
import ir.maktabsharif.onlineexammanagement.model.ExamQuestion;
import ir.maktabsharif.onlineexammanagement.repository.ExamQuestionRepository;
import ir.maktabsharif.onlineexammanagement.repository.ExamRepository;
import ir.maktabsharif.onlineexammanagement.repository.QuestionBankRepository;
import ir.maktabsharif.onlineexammanagement.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

    @Transactional
    public ExamQuestion addQuestionFromBank(Long examId, Long questionId, Double score) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        BaseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("سوال یافت نشد"));

        if (!questionBankRepository.existsByCourseAndQuestion(exam.getCourse(), question)) {
            throw new RuntimeException("سوال در بانک این دوره موجود نیست");
        }

        if (examQuestionRepository.existsByExamAndQuestion(exam, question)) {
            throw new RuntimeException("این سوال قبلاً به آزمون اضافه شده است");
        }

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setDefaultScore(score != null ? score : 1.0);

        Integer maxOrder = examQuestionRepository.findByExam(exam).stream()
                .mapToInt(ExamQuestion::getQuestionOrder)
                .max().orElse(0);
        examQuestion.setQuestionOrder(maxOrder + 1);

        return examQuestionRepository.save(examQuestion);
    }

    @Transactional
    public ExamQuestion addNewQuestionToExam(Long examId, QuestionDto questionDto, Double score) {
        Exam exam = examRepository.findByIdWithCourseAndTeacher(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));


        BaseQuestion question = questionService.createQuestion(
                questionDto,
                exam.getTeacher().getId(),
                exam.getCourse().getId()
        );

        return addQuestionFromBank(examId, question.getId(), score);
    }

    @Transactional
    public ExamQuestion updateQuestionScore(Long examQuestionId, Double newScore) {
        ExamQuestion examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new RuntimeException("ارتباط سوال-آزمون یافت نشد"));

        examQuestion.setDefaultScore(newScore);
        return examQuestionRepository.save(examQuestion);
    }

    @Transactional
    public void deleteQuestionFromExam(Long examQuestionId) {
        examQuestionRepository.deleteById(examQuestionId);
    }

    @Transactional
    public Long getExamIdFromExamQuestionId(Long examQuestionId) {
        ExamQuestion examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new RuntimeException("سوال آزمون یافت نشد!"));
        return examQuestion.getExam().getId();
    }

    public List<ExamQuestionDto> getExamQuestions(Long examId) {
        return examQuestionRepository.findByExamIdWithQuestion(examId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Double calculateExamTotalScore(Long examId) {
        return examQuestionRepository.findByExamIdWithQuestion(examId).stream()
                .mapToDouble(ExamQuestion::getDefaultScore)
                .sum();
    }

    private ExamQuestionDto convertToDto(ExamQuestion examQuestion) {
        ExamQuestionDto dto = new ExamQuestionDto();
        dto.setId(examQuestion.getId());
        dto.setExamId(examQuestion.getExam().getId());
        dto.setQuestionId(examQuestion.getQuestion().getId());
        dto.setDefaultScore(examQuestion.getDefaultScore());
        dto.setQuestionOrder(examQuestion.getQuestionOrder());
        dto.setIsRequired(examQuestion.getIsRequired());
        dto.setQuestion(questionMapper.toDto(examQuestion.getQuestion()));
        return dto;
    }

}