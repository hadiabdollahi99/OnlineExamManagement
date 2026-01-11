package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.ExamParticipationDto;
import ir.maktabsharif.onlineexammanagement.exception.ExamNotFoundException;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.repository.ExamRepository;
import ir.maktabsharif.onlineexammanagement.repository.StudentAnswerRepository;
import ir.maktabsharif.onlineexammanagement.repository.StudentExamParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherGradingService {

    private final StudentExamParticipationRepository participationRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamRepository examRepository;
    private final UserService userService;

    public List<ExamParticipationDto> getExamParticipants(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        if (!exam.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("شما استاد این آزمون نیستید");
        }

        List<StudentExamParticipation> participations =
                participationRepository.findActiveByExamId(examId);

        return participations.stream()
                .map(p -> {
                    ExamParticipationDto dto = new ExamParticipationDto();
                    dto.setId(p.getId());
                    dto.setStudentId(p.getStudent().getId());
                    dto.setStudentName(p.getStudent().getFirstName() + " " + p.getStudent().getLastName());
                    dto.setExamId(p.getExam().getId());
                    dto.setExamTitle(p.getExam().getTitle());

                    dto.setStartedAt(p.getStartedAt());
                    dto.setFinishedAt(p.getFinishedAt());
                    dto.setIsCompleted(p.getIsCompleted());
                    dto.setAutoSubmitted(p.getAutoSubmitted());
                    dto.setStatus(p.getStatus().toString());
                    dto.setTotalScore(p.getTotalScore());

//                    if (p.getStartedAt() != null && p.getFinishedAt() != null) {
//                        Duration duration = Duration.between(p.getStartedAt(), p.getFinishedAt());
//                        dto.setTimeSpentMinutes((int) duration.toMinutes());
//                    }

                    List<StudentAnswer> answers = studentAnswerRepository.findByParticipation(p);
                    boolean allGraded = answers.stream()
                            .allMatch(StudentAnswer::getIsGraded);
                    dto.setIsGraded(allGraded);

                    Double maxScore = p.getExam().getExamQuestions().stream()
                            .mapToDouble(ExamQuestion::getDefaultScore)
                            .sum();
                    dto.setMaxScore(maxScore);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<StudentAnswer> getStudentAnswers(Long participationId, Long teacherId) {
        StudentExamParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("شرکت در آزمون یافت نشد"));

        if (!participation.getExam().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("شما استاد این آزمون نیستید");
        }

        return studentAnswerRepository.findByParticipation(participation);
    }

    @Transactional
    public StudentAnswer gradeDescriptiveAnswer(
            Long answerId,
            Long teacherId,
            Double score
    ) {
        StudentAnswer answer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("پاسخ یافت نشد"));

        if (!answer.getExamQuestion().getExam().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("شما استاد این آزمون نیستید");
        }

        BaseQuestion question = answer.getExamQuestion().getQuestion();
        if (question.getQuestionType() != QuestionType.DESCRIPTIVE) {
            throw new RuntimeException("این سوال تشریحی نیست");
        }

        Double maxScore = answer.getExamQuestion().getDefaultScore();
        if (score > maxScore) {
            throw new RuntimeException("نمره نمی‌تواند از " + maxScore + " بیشتر باشد");
        }

        if (score < 0) {
            throw new RuntimeException("نمره نمی‌تواند منفی باشد");
        }

        answer.setScore(score);
        answer.setIsGraded(true);

        updateTotalScore(answer.getParticipation());

        return studentAnswerRepository.save(answer);
    }

    @Transactional
    protected void updateTotalScore(StudentExamParticipation participation) {
        List<StudentAnswer> answers = studentAnswerRepository.findByParticipation(participation);

        double totalScore = answers.stream()
                .mapToDouble(StudentAnswer::getScore)
                .sum();

        participation.setTotalScore(totalScore);
        participationRepository.save(participation);
    }

    public Map<String, Object> getExamStatistics(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));

        if (!exam.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("شما استاد این آزمون نیستید");
        }

        List<StudentExamParticipation> participations =
                participationRepository.findActiveByExamId(examId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalParticipants", participations.size());

        long completedCount = participations.stream()
                .filter(p -> p.getStatus() == ExamParticipationStatus.COMPLETED ||
                        p.getStatus() == ExamParticipationStatus.AUTO_SUBMITTED)
                .count();
        stats.put("completedCount", completedCount);

        long inProgressCount = participations.stream()
                .filter(p -> p.getStatus() == ExamParticipationStatus.IN_PROGRESS)
                .count();
        stats.put("inProgressCount", inProgressCount);

        double averageScore = participations.stream()
                .filter(p -> p.getTotalScore() != null)
                .mapToDouble(StudentExamParticipation::getTotalScore)
                .average()
                .orElse(0.0);
        stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);

        double maxScore = participations.stream()
                .filter(p -> p.getTotalScore() != null)
                .mapToDouble(StudentExamParticipation::getTotalScore)
                .max()
                .orElse(0.0);
        stats.put("maxScore", maxScore);

        double minScore = participations.stream()
                .filter(p -> p.getTotalScore() != null)
                .mapToDouble(StudentExamParticipation::getTotalScore)
                .min()
                .orElse(0.0);
        stats.put("minScore", minScore);

        return stats;
    }
}