package politicConnect.surveyResponse;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse,Long> {
    boolean existsBySurveyIdAndUserId(Long surveyId, Long userId);
}
