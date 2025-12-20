package politicConnect.survey;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;


    @PostMapping("/submit")
    public ResponseEntity<String> submitSurvey(
            @RequestBody SurveySubmitRequest request,
            @AuthenticationPrincipal Long userId // (주의) Security 설정에 따라 타입(User/UserDetails/Long) 다름
    ) {
        // 1. 로그인 여부 체크 (Security가 안 걸러주는 경우 수동 체크)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 2. 서비스 호출
        try {
            surveyService.submitSurvey(userId, request);
            return ResponseEntity.ok("설문 제출이 완료되었습니다.");
        } catch (IllegalStateException e) {
            // 중복 참여 등 로직 에러
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 잘못된 ID 등 유효성 에러
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청입니다: " + e.getMessage());
        }
    }

         /**
         * 카테고리별 설문 조회 API
         * GET /api/surveys/category/{categoryName}
         * 예: /api/surveys/category/EDUCATION
         */
        @GetMapping("/category/{surveyCategory}")
        public ResponseEntity<List<SurveyDto>> getSurveysByCategory(@PathVariable SurveyCategory surveyCategory) {
            List<SurveyDto> surveyDtos = surveyService.getSurveysByCategory(surveyCategory);
            return ResponseEntity.ok(surveyDtos);
        }


}
