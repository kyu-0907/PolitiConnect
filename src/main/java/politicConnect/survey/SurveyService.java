package politicConnect.survey;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import politicConnect.option.Option;
import politicConnect.option.OptionRepository;
import politicConnect.responseDetail.ResponseDetail;
import politicConnect.responseDetail.ResponseDetailRepository;
import politicConnect.surveyResponse.SurveyResponse;
import politicConnect.surveyResponse.SurveyResponseRepository;
import politicConnect.user.User;
import politicConnect.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final ResponseDetailRepository responseDetailRepository;


    public void submitSurvey(Long currentUserId, SurveySubmitRequest request) {

        // 1. 유저 조회 (로그인한 사용자)
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 설문 조회
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));

        // 3. 중복 참여 검사 (기획에 따라 생략 가능)
        if (responseRepository.existsBySurveyIdAndUserId(survey.getId(), user.getId())) {
            throw new IllegalStateException("이미 참여한 설문입니다.");
        }

        // 4. 응답지(헤더) 저장
        SurveyResponse response = SurveyResponse.builder()
                .user(user)
                .survey(survey)
                .suggestion(request.getSuggestion())
                .build();

        responseRepository.save(response); // 먼저 저장해서 response_id 생성

        // 5. 상세 답변 저장 로직
        List<ResponseDetail> details = new ArrayList<>();

        for (SurveySubmitRequest.AnswerRequest answerDto : request.getAnswers()) {
            // 5-1. 선택한 옵션 조회
            Option selectedOption = optionRepository.findById(answerDto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다. ID=" + answerDto.getOptionId()));

            // 5-2. 데이터 무결성 검증 (선택한 옵션이 요청한 질문에 속하는지)
            if (!selectedOption.getQuestion().getId().equals(answerDto.getQuestionId())) {
                throw new IllegalArgumentException("잘못된 요청: 옵션이 해당 질문에 속하지 않습니다.");
            }

            // 5-3. 상세 엔티티 생성
            // Question은 DB 조회 없이 Option에서 바로 꺼내 씀 (일관성 보장)
            ResponseDetail detail = ResponseDetail.builder()
                    .surveyResponse(response)
                    .question(selectedOption.getQuestion())
                    .selectedOption(selectedOption)
                    .build();

            details.add(detail);
        }

        // 6. 상세 답변 일괄 저장 (Batch Insert 효과)
        responseDetailRepository.saveAll(details);
    }

    public List<SurveyDto> getSurveysByCategory(SurveyCategory category) {
        // 1. DB에서 엔티티 조회
        List<Survey> surveys = surveyRepository.findBySurveyCategory(category);

        // 2. 엔티티 -> DTO 변환 (SurveyDto.from 메서드 활용)
        return surveys.stream()
                .map(SurveyDto::from)
                .collect(Collectors.toList());
    }



}
