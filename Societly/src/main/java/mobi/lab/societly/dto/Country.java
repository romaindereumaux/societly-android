package mobi.lab.societly.dto;

import java.util.List;

public class Country {

    private List<State> states;

    public Country(List<State> states) {
        this.states = states;
    }

    public List<State> getStates() {
        return states;
    }
}
