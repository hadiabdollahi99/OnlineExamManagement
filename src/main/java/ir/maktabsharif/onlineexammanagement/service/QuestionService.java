package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.QuestionDto;
import ir.maktabsharif.onlineexammanagement.dto.QuestionOptionDto;
import ir.maktabsharif.onlineexammanagement.exception.QuestionNotFoundException;
import ir.maktabsharif.onlineexammanagement.mapper.QuestionMapper;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final QuestionMapper questionMapper;


    @Transactional
    public BaseQuestion createQuestion(QuestionDto questionDto, Long teacherId, Long courseId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("استاد یافت نشد"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("دوره یافت نشد"));

        BaseQuestion question = questionMapper.toEntity(questionDto, teacher, course);
        BaseQuestion savedQuestion = questionRepository.save(question);

        addQuestionToBank(savedQuestion, course);

        return savedQuestion;
    }

    @Transactional
    public void addQuestionToBank(BaseQuestion question, Course course) {
        if (!questionBankRepository.existsByCourseAndQuestion(course, question)) {
            QuestionBank bankEntry = new QuestionBank();
            bankEntry.setCourse(course);
            bankEntry.setQuestion(question);
            questionBankRepository.save(bankEntry);
        }
    }

    public BaseQuestion getQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("سوال یافت نشد"));
    }

    public List<QuestionDto> searchQuestions(Long teacherId, Long courseId,
                                             String title, QuestionType questionType) {
        List<BaseQuestion> questions = questionRepository.searchQuestions(
                teacherId, courseId, title, questionType);

        return questions.stream()
                .map(questionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        ExamQuestion examQuestion = examQuestionRepository.findByQuestionId(questionId);

        if (examQuestion != null){
            examQuestionRepository.deleteById(examQuestion.getId());
            QuestionBank questionBank = questionBankRepository.findByQuestionId(questionId);
            questionBankRepository.deleteById(questionBank.getId());
            questionRepository.deleteById(questionId);
            return;
        }
        QuestionBank questionBank = questionBankRepository.findByQuestionId(questionId);
        questionBankRepository.deleteById(questionBank.getId());
        questionRepository.deleteById(questionId);
    }


    public QuestionDto convertToDto(BaseQuestion question) {
        QuestionDto dto = new QuestionDto();
        dto.setTitle(question.getTitle());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());

        if (question instanceof MultipleChoiceQuestion) {
            MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
            dto.setShuffleOptions(mcq.getShuffleOptions());

            ArrayList<QuestionOptionDto> optionDtos = new ArrayList<>();
            for (QuestionOption option : mcq.getOptions()) {
                QuestionOptionDto optionDto = new QuestionOptionDto();
                optionDto.setOptionText(option.getOptionText());
                optionDto.setOrderIndex(option.getOrderIndex());
                optionDto.setIsCorrect(option.getIsCorrect());
                optionDtos.add(optionDto);
            }
            dto.setOptions(optionDtos);

        } else if (question instanceof DescriptiveQuestion) {
            DescriptiveQuestion dq = (DescriptiveQuestion) question;
            dto.setSampleAnswer(dq.getSampleAnswer());
            dto.setExpectedAnswerLength(dq.getExpectedAnswerLength());
        }

        return dto;
    }

}