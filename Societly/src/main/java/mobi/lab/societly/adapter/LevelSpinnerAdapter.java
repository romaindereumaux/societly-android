package mobi.lab.societly.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.R;
import mobi.lab.societly.util.ViewUtil;

public abstract class LevelSpinnerAdapter<T> extends BaseAdapter {

    protected Log log = Log.getInstance(this);

    private List<T> data;

    public LevelSpinnerAdapter(List<T> newData) {
        setData(newData);
    }

    protected abstract String getDisplayText(T item);

    private void setData(List<T> newData) {
        data = newData == null ? new ArrayList<>() : newData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    public List<T> getItems() {
        return data;
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Hide dropdown drawable if we only have one item
        Drawable dropdownDrawable = holder.title.getCompoundDrawables()[2].mutate();
        if (getCount() == 1) {
            // Single item => no selection
            dropdownDrawable.setAlpha(0);
        } else {
            dropdownDrawable.setAlpha(255);
            ViewUtil.setColor(holder.title.getCompoundDrawables()[2], Color.WHITE);
        }
        holder.title.setCompoundDrawables(null, null, dropdownDrawable, null);
        holder.title.setText(getDisplayText(getItem(position)));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_dropdown, parent, false);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(getDisplayText(getItem(position)));
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
    }
}
