package politicConnect.surveyResponse;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import politicConnect.responseDetail.ResponseDetail;
import politicConnect.survey.Survey;
import politicConnect.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        uniqueConstraints = {
                // 유저 + 설문 + '날짜'는 유일해야 함 (하루에 한 번만 생성 가능)
                @UniqueConstraint(
                        name = "uk_response_daily",
                        columnNames = {"user_id", "survey_id", "submissionDate"}
                )
        }
)
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long id;

    // [수정] 단순 Long userId -> User 객체 매핑 (FK 설정됨)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB에는 user_id 컬럼으로 생성됨
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Enumerated(EnumType.STRING)
    private SurveyStatus surveyStatus;

    private LocalDate submissionDate;

    @Column(columnDefinition = "TEXT")
    private String suggestion;



    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponseDetail> details = new ArrayList<>();

    @Builder
    public SurveyResponse(Survey survey, User user, String suggestion) {
        this.survey = survey;
        this.user = user; // 객체를 직접 받음
        this.suggestion = suggestion;
        this.surveyStatus = surveyStatus!= null ? surveyStatus : SurveyStatus.IN_PROGRESS;
    }

    public void update(String suggestion, SurveyStatus surveyStatus) {
        this.suggestion = suggestion;
        this.surveyStatus = surveyStatus;
    }
}