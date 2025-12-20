package politicConnect.vote;

import lombok.Builder;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class VoteDto {
    private Long id;
    private String title;
    private int timeRequired;
    private String period;            // 예: "2025.05.01 ~ 2025.05.05"
    private Double participationRate;

    // [수정] 년도를 포함한 포맷터 (yyyy.MM.dd)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static VoteDto from(Vote vote) {
        return VoteDto.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .timeRequired(vote.getTimeRequired())
                .participationRate(vote.getParticipationRate())
                .period(makePeriodString(vote.getStartDate(), vote.getEndDate()))
                .build();
    }

    private static String makePeriodString(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "기간 미정";
        return start.format(FORMATTER) + " ~ " + end.format(FORMATTER);
    }
}
