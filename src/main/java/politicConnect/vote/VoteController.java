package politicConnect.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    /**
     * 카테고리별 투표 목록 조회
     * GET /api/votes/category/SPORTS
     * GET /api/votes/category/POLITICS
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<VoteDto>> getVotesByCategory(@PathVariable String category) {
        // 카테고리 값이 대문자/소문자 구분이 필요하다면 여기서 변환(toUpperCase 등) 가능
        List<VoteDto> voteList = voteService.getVotesByCategory(category);
        return ResponseEntity.ok(voteList);
    }
}
