package politicConnect.mainPage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import politicConnect.survey.SurveyService;
import politicConnect.vote.VoteService;

@Controller
@RequiredArgsConstructor
public class MainPageController {


    private final VoteService voteService;
    private final SurveyService surveyService;






}
