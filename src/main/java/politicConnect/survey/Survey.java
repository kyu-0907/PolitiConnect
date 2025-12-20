package politicConnect.survey;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import politicConnect.question.Question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 보호
@Table(name = "surveys") // 테이블명 지정
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 설문 제목
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    private List<Question> questions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private SurveyFrequency surveyFrequency;

    // 카테고리 (정치, 사회, 청년 경제)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyCategory surveyCategory;



    // 소요 시간 (분 단위 권장, 예: 5분 -> 5)
    private int timeRequired;

    private Boolean completed;

    // 진행 기간 (시작일)
    private LocalDateTime startDate;

    // 진행 기간 (종료일)
    private LocalDateTime endDate;

    // 참여율 (퍼센트, 예: 55.5%)
    private Double participationRate;




    // 생성자 (Builder 패턴 사용)
    @Builder
    public Survey(String title, SurveyCategory category, int timeRequired,
                  LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.surveyCategory = category;
        this.timeRequired = timeRequired;
        this.startDate = startDate;
        this.endDate = endDate;
        this.participationRate = 50.0; // 초기 참여율은 50%로 설정
    }

    // 참여율 업데이트를 위한 메서드 (비즈니스 로직)
    public void updateParticipationRate(Double newRate) {
        this.participationRate = newRate;
    }
}
