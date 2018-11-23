package mobi.lab.societly.adapter;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import mobi.lab.societly.R;
import mobi.lab.societly.util.ViewUtil;

/**
 * Created by lauris on 28/10/2016.
 */

public class NoHintSpinnerAdapter<T> extends BaseAdapter {

    private LevelSpinnerAdapter<T> adapter;
    private int hintTextResId;
    private int hintLayoutResId;

    public NoHintSpinnerAdapter(LevelSpinnerAdapter<T> adapter, int hintTextResId, int hintLayoutResId) {
        this.adapter = adapter;
        this.hintTextResId = hintTextResId;
        this.hintLayoutResId = hintLayoutResId;
    }

    @Override
    public int getCount() {
        return adapter.getCount() + 1;
    }

    @Override
    public T getItem(int position) {
        if (position == 0) {
            return null;
        }
        return adapter.getItem(position - 1);
    }

    public List<T> getItems() {
        return adapter.getItems();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(hintLayoutResId, parent, false);
            view.setBackgroundResource(R.drawable.shape_spinner_light);
            TextView title = (TextView) view.findViewById(android.R.id.text1);
            int blueColor = ContextCompat.getColor(parent.getContext(), R.color.btn_blue);
            ViewUtil.setColor(title.getCompoundDrawables()[2], blueColor);
            title.setTextColor(blueColor);
            title.setText(hintTextResId);
            return view;
        }
        return adapter.getView(position - 1, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            return new View(parent.getContext());
        }
        return adapter.getDropDownView(position - 1, convertView, parent);
    }
}
