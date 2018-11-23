package mobi.lab.societly.model;

import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.dto.Questionnaire;

public class CandidateOverviewResult extends BaseResp {

    private CandidateCompatibility compatibility;
    private Questionnaire userAnswers;

    public CandidateOverviewResult(CandidateCompatibility result, Questionnaire userAnswers) {
        super(null);
        this.userAnswers = userAnswers;
        this.compatibility = result;
    }

    public CandidateOverviewResult(Error error) {
        super(error);
        compatibility = null;
        userAnswers = null;
    }

    public CandidateCompatibility getCompatibility() {
        return compatibility;
    }

    public Questionnaire getUserAnswers() {
        return userAnswers;
    }
}
