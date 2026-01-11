package ir.maktabsharif.onlineexammanagement.repository;

import ir.maktabsharif.onlineexammanagement.model.ExamSession;
import ir.maktabsharif.onlineexammanagement.model.StudentExamParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findBySessionId(String sessionId);

    Optional<ExamSession> findByParticipation(StudentExamParticipation participation);

    void deleteByParticipation(StudentExamParticipation participation);
}