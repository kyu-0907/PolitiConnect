package politicConnect.responseDetail;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import politicConnect.option.Option;
import politicConnect.question.Question;
import politicConnect.surveyResponse.SurveyResponse;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    // 1. 어느 응답지(부모)에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse surveyResponse;

    // 2. 어떤 질문에 대한 답인지 (Question과 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 3. 어떤 보기를 선택했는지 (Option과 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option selectedOption;

    @Builder
    public ResponseDetail(SurveyResponse surveyResponse, Question question, Option selectedOption) {
        this.surveyResponse = surveyResponse;
        this.question = question;
        this.selectedOption = selectedOption;
    }
}

