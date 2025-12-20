package politicConnect.option;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import politicConnect.question.Question;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    private String text; // 선지 내용 (예: "Java", "Python")
    private int orderNo; // 보기 순서 (1번, 2번...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Builder
    public Option(Question question, String text, int orderNo) {
        this.question = question;
        this.text = text;
        this.orderNo = orderNo;
    }
}


