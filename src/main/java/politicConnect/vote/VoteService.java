package politicConnect.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;

    public List<VoteDto> getVotesByCategory(String category) {
        // 1. 카테고리로 조회
        List<Vote> votes = voteRepository.findByVoteCategoryOrderByStartDateDesc(category);

        // 2. DTO 변환 (VoteDto.from 사용)
        return votes.stream()
                .map(VoteDto::from)
                .collect(Collectors.toList());
    }


}
