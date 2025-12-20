package politicConnect.survey;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Data
public class SurveyDto {

    private Long id;
    private String title;
    private int timeRequired;
    private String period;
    private Double participationRate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static SurveyDto from(Survey survey) {
        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .timeRequired(survey.getTimeRequired())
                .participationRate(survey.getParticipationRate())
                .period(makePeriodString(survey.getStartDate(), survey.getEndDate()))
                .build();
    }

    private static String makePeriodString(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "기간 미정";
        return start.format(FORMATTER) + " ~ " + end.format(FORMATTER);
    }









}
