package politicConnect.survey;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveySubmitRequest {

    private Long surveyId;           // 어느 설문인지
    private String suggestion;       // 마지막 한마디 (주관식)
    private List<AnswerRequest> answers; // 선택한 답변 리스트

    @Data
    @NoArgsConstructor
    public static class AnswerRequest {
        private Long questionId;     // 질문 ID
        private Long optionId;       // 선택한 보기 ID
    }

}
