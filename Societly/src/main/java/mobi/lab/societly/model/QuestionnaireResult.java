package mobi.lab.societly.model;

import mobi.lab.societly.dto.Questionnaire;

public class QuestionnaireResult extends BaseResp {

    private Questionnaire questionnaire;

    public QuestionnaireResult(Questionnaire questionnaire) {
        super(null);
        this.questionnaire = questionnaire;
    }

    public QuestionnaireResult(Error error) {
        super(error);
        questionnaire = null;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }
}
