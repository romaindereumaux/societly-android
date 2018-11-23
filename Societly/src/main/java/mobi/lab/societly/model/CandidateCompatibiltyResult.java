package mobi.lab.societly.model;

import android.support.v4.util.LongSparseArray;

import java.util.List;

import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.dto.Country;

public class CandidateCompatibiltyResult extends BaseResp {

    private List<CandidateCompatibility> countryResults;
    private LongSparseArray<List<CandidateCompatibility>> stateResults;
    private LongSparseArray<List<CandidateCompatibility>> districtResults;
    private List<CandidateCompatibility> results;

    private Country country;

    public CandidateCompatibiltyResult(List<CandidateCompatibility> results) {
        super(null);
        this.results = results;
    }

    public CandidateCompatibiltyResult(Country country,
           List<CandidateCompatibility> countryResults,
           LongSparseArray<List<CandidateCompatibility>> stateResults,
           LongSparseArray<List<CandidateCompatibility>> districtResults) {
        super(null);
        this.country = country;
        this.countryResults = countryResults;
        this.stateResults = stateResults;
        this.districtResults = districtResults;
    }

    public CandidateCompatibiltyResult(Error error) {
        super(error);
        results = null;
    }

    public List<CandidateCompatibility> getResults() {
        return results;
    }

    public List<CandidateCompatibility> getCountryResults() {
        return countryResults;
    }

    public LongSparseArray<List<CandidateCompatibility>> getStateResults() {
        return stateResults;
    }

    public LongSparseArray<List<CandidateCompatibility>> getDistrictResults() {
        return districtResults;
    }

    public Country getCountry() {
        return country;
    }
}
