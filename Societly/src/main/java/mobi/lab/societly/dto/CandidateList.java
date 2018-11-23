package mobi.lab.societly.dto;

import java.util.List;

import mobi.lab.societly.util.Util;

public class CandidateList {

    private List<Candidate> countryCandidates;
    private List<Candidate> stateCandidates;
    private List<Candidate> districtCandidates;

    public CandidateList(List<Candidate> countryCandidates, List<Candidate> stateCandidates, List<Candidate> districtCandidates) {
        this.countryCandidates = countryCandidates;
        this.stateCandidates = stateCandidates;
        this.districtCandidates = districtCandidates;
    }

    public List<Candidate> getCountryCandidates() {
        return countryCandidates;
    }

    public List<Candidate> getStateCandidates() {
        return stateCandidates;
    }

    public List<Candidate> getDistrictCandidates() {
        return districtCandidates;
    }

    @Override
    public String toString() {
        return "CandidateList{" +
                "countryCandidates=" + Util.sizeOf(countryCandidates) +
                ", stateCandidates=" + Util.sizeOf(stateCandidates) +
                ", districtCandidates=" + Util.sizeOf(districtCandidates) +
                '}';
    }
}
