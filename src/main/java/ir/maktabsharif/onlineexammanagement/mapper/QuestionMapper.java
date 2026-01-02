package ir.maktabsharif.onlineexammanagement.mapper;

import ir.maktabsharif.onlineexammanagement.dto.QuestionDto;
import ir.maktabsharif.onlineexammanagement.dto.QuestionOptionDto;
import ir.maktabsharif.onlineexammanagement.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuestionMapper {

    public QuestionDto toDto(BaseQuestion question) {
        QuestionDto dto = QuestionDto.builder()
                .id(question.getId())
                .title(question.getTitle())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .teacherId(question.getTeacher().getId())
                .courseId(question.getCourse().getId())
                .build();

        if (question instanceof MultipleChoiceQuestion) {
            MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
            dto.setShuffleOptions(mcq.getShuffleOptions());
            dto.setOptions(mcq.getOptions().stream()
                    .map(this::optionToDto)
                    .collect(Collectors.toList()));
        } else if (question instanceof DescriptiveQuestion) {
            DescriptiveQuestion dq = (DescriptiveQuestion) question;
            dto.setSampleAnswer(dq.getSampleAnswer());
            dto.setExpectedAnswerLength(dq.getExpectedAnswerLength());
        }

        return dto;
    }

    public BaseQuestion toEntity(QuestionDto dto, Teacher teacher, Course course) {
        BaseQuestion question;

        if (dto.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            MultipleChoiceQuestion mcq = MultipleChoiceQuestion.builder()
                    .title(dto.getTitle())
                    .questionText(dto.getQuestionText())
                    .teacher(teacher)
                    .course(course)
                    .shuffleOptions(dto.getShuffleOptions())
                    .questionType(QuestionType.MULTIPLE_CHOICE)
                    .build();

            if (dto.getOptions() != null) {
                dto.getOptions().forEach(optionDto -> {
                    QuestionOption option = new QuestionOption();
                    option.setOptionText(optionDto.getOptionText());
                    option.setOrderIndex(optionDto.getOrderIndex());
                    option.setIsCorrect(optionDto.getIsCorrect());
                    mcq.addOption(option);
                });
            }

            question = mcq;
        } else {
            DescriptiveQuestion dq = DescriptiveQuestion.builder()
                    .title(dto.getTitle())
                    .questionText(dto.getQuestionText())
                    .teacher(teacher)
                    .course(course)
                    .sampleAnswer(dto.getSampleAnswer())
                    .expectedAnswerLength(dto.getExpectedAnswerLength())
                    .questionType(QuestionType.DESCRIPTIVE)
                    .build();

            question = dq;
        }

        return question;
    }

    private QuestionOptionDto optionToDto(QuestionOption option) {
        return QuestionOptionDto.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .isCorrect(option.getIsCorrect())
                .build();
    }
}