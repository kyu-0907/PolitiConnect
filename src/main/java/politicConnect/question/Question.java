package politicConnect.question;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import politicConnect.option.Option;
import politicConnect.survey.Survey;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    private String text; // 질문 내용 (예: "가장 선호하는 언어는?")
    private int orderNo; // 질문 순서 (1번, 2번...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    // [핵심] 보기 목록을 가져올 때 'orderNo' 기준으로 오름차순 정렬 (1, 2, 3...)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    private List<Option> options = new ArrayList<>();

    @Builder
    public Question(Survey survey, String text, int orderNo) {
        this.survey = survey;
        this.text = text;
        this.orderNo = orderNo;
    }
}
