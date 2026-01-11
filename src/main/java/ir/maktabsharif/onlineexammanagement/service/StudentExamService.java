package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.*;
import ir.maktabsharif.onlineexammanagement.exception.*;
import ir.maktabsharif.onlineexammanagement.mapper.ExamMapper;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentExamService {

    private final StudentExamParticipationRepository participationRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentCoursesRepository studentCoursesRepository;
    private final ExamMapper examMapper;

    public List<Course> getStudentCourses(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("دانشجو یافت نشد"));

        List<StudentCourses> studentCourses = student.getEnrolledCourses();

        return studentCourses.stream()
                .map(StudentCourses::getCourse)
                .filter(course -> course.getEndDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public List<StudentExamDto> getAvailableExams(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("دانشجو یافت نشد"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("دوره یافت نشد"));

        boolean isEnrolled = studentCoursesRepository.existsByStudentAndCourse(student, course);
        if (!isEnrolled) {
            throw new RuntimeException("شما در این دوره ثبت‌نام نکرده‌اید");
        }


        List<Exam> exams = examRepository.findByCourse(course);

        return exams.stream()
                .filter(Exam::getIsActive)
                .map(exam -> {
                    StudentExamDto dto = examMapper.toStudentExamDto(exam);

                    Optional<StudentExamParticipation> participation =
                            participationRepository.findByStudentAndExam(student, exam);

                    if (participation.isPresent()) {
                        dto.setHasTakenExam(true);
                        dto.setTakenAt(participation.get().getStartedAt());
                        dto.setScore(participation.get().getTotalScore());
                        dto.setCanTakeExam(false);
                    } else {
                        dto.setHasTakenExam(false);
                        dto.setCanTakeExam(true);
                    }

                    List<ExamQuestion> examQuestions = examQuestionRepository.findByExam(exam);
                    dto.setQuestionCount(examQuestions.size());
                    dto.setTotalScore(examQuestions.stream()
                            .mapToDouble(ExamQuestion::getDefaultScore)
                            .sum());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentExamParticipation startExam(Long studentId, Long examId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("دانشجو یافت نشد"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("آزمون یافت نشد"));


        boolean isEnrolled = studentCoursesRepository.existsByStudentAndCourse(student, exam.getCourse());
        if (!isEnrolled) {
            throw new RuntimeException("شما در این دوره ثبت‌نام نکرده‌اید");
        }

        if (!exam.getIsActive()) {
            throw new RuntimeException("این آزمون غیرفعال است");
        }

        Optional<StudentExamParticipation> existing =
                participationRepository.findByStudentAndExam(student, exam);

        if (existing.isPresent()) {
            StudentExamParticipation participation = existing.get();

            if (participation.getIsCompleted() ||
                    participation.getStatus() == ExamParticipationStatus.COMPLETED) {
                throw new RuntimeException("شما قبلاً در این آزمون شرکت کرده‌اید");
            }

            if (participation.getStatus() == ExamParticipationStatus.IN_PROGRESS) {
                LocalDateTime startedAt = participation.getStartedAt();
                int examDuration = exam.getDurationMinutes();
                LocalDateTime endTime = startedAt.plusMinutes(examDuration);

                if (LocalDateTime.now().isAfter(endTime)) {
                    autoSubmitExam(participation);
                    throw new RuntimeException("زمان آزمون به پایان رسیده است");
                }

                return participation;
            }
        }

        StudentExamParticipation participation = new StudentExamParticipation();
        participation.setStudent(student);
        participation.setExam(exam);
        participation.setStartedAt(LocalDateTime.now());
        participation.setStatus(ExamParticipationStatus.IN_PROGRESS);

        return participationRepository.save(participation);
    }

    public List<ExamQuestionForStudentDto> getExamQuestionsForStudent(
            Long participationId,
            Long studentId
    ) {
        StudentExamParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("شرکت در آزمون یافت نشد"));

        if (!participation.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("شما مجاز به دیدن این آزمون نیستید");
        }

        checkExamTime(participation);

        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdWithQuestion(
                participation.getExam().getId());

        return examQuestions.stream()
                .map(eq -> {
                    ExamQuestionForStudentDto dto = new ExamQuestionForStudentDto();
                    dto.setId(eq.getId());
                    dto.setExamId(eq.getExam().getId());
                    dto.setDefaultScore(eq.getDefaultScore());
                    dto.setQuestionOrder(eq.getQuestionOrder());

                    QuestionDto questionDto = new QuestionDto();
                    BaseQuestion question = eq.getQuestion();
                    questionDto.setId(question.getId());
                    questionDto.setTitle(question.getTitle());
                    questionDto.setQuestionText(question.getQuestionText());
                    questionDto.setQuestionType(question.getQuestionType());

                    dto.setQuestion(questionDto);

                    if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
                        dto.setIsMultipleChoice(true);

                        List<QuestionOptionDto> optionDtos = mcq.getOptions().stream()
                                .map(option -> {
                                    QuestionOptionDto optionDto = new QuestionOptionDto();
                                    optionDto.setId(option.getId());
                                    optionDto.setOptionText(option.getOptionText());
                                    optionDto.setOrderIndex(option.getOrderIndex());
                                    return optionDto;
                                })
                                .collect(Collectors.toList());

                        if (mcq.getShuffleOptions()) {
                            Collections.shuffle(optionDtos);
                        }

                        dto.setOptions(optionDtos);
                    } else {
                        dto.setIsDescriptive(true);
                    }

                    Optional<StudentAnswer> existingAnswer =
                            studentAnswerRepository.findByParticipationAndExamQuestion(participation, eq);

                    if (existingAnswer.isPresent()) {
                        StudentAnswer answer = existingAnswer.get();
                        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                            dto.setSelectedOptionId(answer.getSelectedOption() != null ?
                                    answer.getSelectedOption().getId() : null);
                        } else {
                            dto.setStudentAnswer(answer.getAnswerText());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAnswer(Long participationId, Long studentId,
                           Long examQuestionId, String answerText, Long selectedOptionId) {

         StudentExamParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("شرکت در آزمون یافت نشد"));

        if (!participation.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("شما مجاز به ذخیره پاسخ نیستید");
        }

        checkExamTime(participation);

        ExamQuestion examQuestion = examQuestionRepository.findByIdWithExam(examQuestionId)
                .orElseThrow(() -> new RuntimeException("سوال آزمون یافت نشد"));


        Optional<StudentAnswer> existingAnswer =
                studentAnswerRepository.findByParticipationAndExamQuestion(participation, examQuestion);

        StudentAnswer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
        } else {
            answer = new StudentAnswer();
            answer.setParticipation(participation);
            answer.setExamQuestion(examQuestion);
        }

        BaseQuestion question = examQuestion.getQuestion();
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            if (selectedOptionId != null) {
                MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
                QuestionOption selectedOption = mcq.getOptions().stream()
                        .filter(opt -> opt.getId().equals(selectedOptionId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("گزینه انتخابی معتبر نیست"));
                answer.setSelectedOption(selectedOption);
            }
        } else {
            answer.setAnswerText(answerText);
        }

        answer.setAnsweredAt(LocalDateTime.now());
        answer.setIsAutoSaved(true);

        studentAnswerRepository.save(answer);

        updateExamSession(participation);
    }

    @Transactional
    public StudentExamParticipation submitExam(Long participationId, Long studentId) {
        StudentExamParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("شرکت در آزمون یافت نشد"));

        if (!participation.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("شما مجاز به ارسال آزمون نیستید");
        }

        participation.setFinishedAt(LocalDateTime.now());
        participation.setIsCompleted(true);
        participation.setStatus(ExamParticipationStatus.COMPLETED);

        calculateMultipleChoiceScores(participation);

        examSessionRepository.deleteByParticipation(participation);

        return participationRepository.save(participation);
    }

    private void calculateMultipleChoiceScores(StudentExamParticipation participation) {
        List<StudentAnswer> answers = studentAnswerRepository.findByParticipation(participation);
        double totalScore = 0.0;

        for (StudentAnswer answer : answers) {
            BaseQuestion question = answer.getExamQuestion().getQuestion();

            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                if (answer.getSelectedOption() != null && answer.getSelectedOption().getIsCorrect()) {
                    answer.setScore(answer.getExamQuestion().getDefaultScore());
                    answer.setIsGraded(true);
                } else {
                    answer.setScore(0.0);
                    answer.setIsGraded(true);
                }
                studentAnswerRepository.save(answer);

                totalScore += answer.getScore();
            }
        }

        participation.setTotalScore(totalScore);
    }

    private void checkExamTime(StudentExamParticipation participation) {
        LocalDateTime startedAt = participation.getStartedAt();
        int examDuration = participation.getExam().getDurationMinutes();
        LocalDateTime endTime = startedAt.plusMinutes(examDuration);

        if (LocalDateTime.now().isAfter(endTime)) {
            autoSubmitExam(participation);
            throw new RuntimeException("زمان آزمون به پایان رسیده است");
        }
    }

    @Transactional
    public void autoSubmitExam(StudentExamParticipation participation) {
        participation.setFinishedAt(LocalDateTime.now());
        participation.setIsCompleted(true);
        participation.setAutoSubmitted(true);
        participation.setStatus(ExamParticipationStatus.AUTO_SUBMITTED);

        calculateMultipleChoiceScores(participation);

        examSessionRepository.deleteByParticipation(participation);

        participationRepository.save(participation);
    }

    private void updateExamSession(StudentExamParticipation participation) {
        String sessionId = "session_" + participation.getId() + "_" + System.currentTimeMillis();

        Optional<ExamSession> existingSession =
                examSessionRepository.findByParticipation(participation);

        ExamSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
        } else {
            session = new ExamSession();
            session.setParticipation(participation);
        }

        session.setSessionId(sessionId);
        session.setLastActivity(LocalDateTime.now());
        session.setIsActive(true);

        LocalDateTime startedAt = participation.getStartedAt();
        int examDuration = participation.getExam().getDurationMinutes();
        LocalDateTime endTime = startedAt.plusMinutes(examDuration);

        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
        session.setRemainingTime((int) Math.max(0, remainingSeconds));

        examSessionRepository.save(session);
    }

    public Optional<ExamSession> recoverSession(String sessionId, Long studentId) {
        return examSessionRepository.findBySessionId(sessionId)
                .filter(session -> session.getIsActive())
                .filter(session -> session.getParticipation().getStudent().getId().equals(studentId))
                .filter(session -> {
                    // بررسی زمان
                    StudentExamParticipation participation = session.getParticipation();
                    LocalDateTime startedAt = participation.getStartedAt();
                    int examDuration = participation.getExam().getDurationMinutes();
                    LocalDateTime endTime = startedAt.plusMinutes(examDuration);

                    return LocalDateTime.now().isBefore(endTime);
                });
    }

    public Integer getRemainingTime(Long participationId) {
        StudentExamParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("شرکت در آزمون یافت نشد"));

        LocalDateTime startedAt = participation.getStartedAt();
        int examDuration = participation.getExam().getDurationMinutes();
        LocalDateTime endTime = startedAt.plusMinutes(examDuration);

        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
        return (int) Math.max(0, remainingSeconds);
    }
}