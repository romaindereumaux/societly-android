package mobi.lab.societly.adapter;

import java.util.List;

import mobi.lab.societly.R;
import mobi.lab.societly.dto.District;

public class DistrictAdapter extends LevelSpinnerAdapter<District> {

    public static NoHintSpinnerAdapter<District> createNoHintAdapter(List<District> districts) {
        return new NoHintSpinnerAdapter<>(new DistrictAdapter(districts), R.string.hint_spinner_district, R.layout.item_spinner);
    }

    public DistrictAdapter(List<District> newData) {
        super(newData);
    }

    @Override
    protected String getDisplayText(District item) {
        return item.getName();
    }
}
