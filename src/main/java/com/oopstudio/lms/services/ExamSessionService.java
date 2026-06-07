package com.oopstudio.lms.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oopstudio.lms.models.ExamSession;
import com.oopstudio.lms.repositories.ExamSessionRepository;

@Service
public class ExamSessionService {

	private final ExamSessionRepository examSessionRepository;

	public ExamSessionService(ExamSessionRepository examSessionRepository) {
		this.examSessionRepository = examSessionRepository;
	}

	@Transactional(readOnly = true)
	public boolean isOfficialExamActive() {
		return examSessionRepository.findFirstByOrderByIdAsc()
				.map(ExamSession::getActive)
				.orElse(false);
	}

	@Transactional
	public ExamSession getOrCreateExamSession() {
		return examSessionRepository.findFirstByOrderByIdAsc()
				.orElseGet(() -> {
					ExamSession session = new ExamSession();
					session.setActive(false);
					return examSessionRepository.save(session);
				});
	}

	@Transactional
	public ExamSession toggleOfficialExam() {
		ExamSession session = getOrCreateExamSession();
		session.setActive(!Boolean.TRUE.equals(session.getActive()));
		return examSessionRepository.save(session);
	}
}
