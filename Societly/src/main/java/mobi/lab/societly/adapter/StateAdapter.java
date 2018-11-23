package mobi.lab.societly.adapter;

import java.util.List;

import mobi.lab.societly.R;
import mobi.lab.societly.dto.State;

public class StateAdapter extends LevelSpinnerAdapter<State> {

    public static NoHintSpinnerAdapter<State> createNoHintAdapter(List<State> states) {
        return new NoHintSpinnerAdapter<>(new StateAdapter(states), R.string.hint_spinner_state, R.layout.item_spinner);
    }

    public StateAdapter(List<State> newData) {
        super(newData);
    }

    @Override
    protected String getDisplayText(State item) {
        return item.getName();
    }
}
