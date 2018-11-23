package mobi.lab.societly.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.R;

/**
 * Created by lauris on 28/09/16.
 */

public class AdapterLinearLayout extends LinearLayout {

    private Log log = Log.getInstance(this);
    private BaseAdapter adapter;
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            log.d("onChanged");
            showViews();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            log.d("onInvalidated");
            showViews();
        }
    };
    private int emptyResId;

    public AdapterLinearLayout(Context context) {
        super(context);
        init();
    }

    public AdapterLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdapterLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Add custom init code here
        setOrientation(VERTICAL);
    }

    public void setEmptyText(int emptyResId) {
        this.emptyResId = emptyResId;
    }

    public void setAdapter(BaseAdapter newAdapter) {
        if (adapter != null) {
            unregisterObserver(adapter);
        }
        adapter = newAdapter;
        if (adapter != null) {
            adapter.registerDataSetObserver(observer);
        }
        showViews();
    }

    private void unregisterObserver(BaseAdapter adapter) {
        try {
            adapter.unregisterDataSetObserver(observer);
        } catch (IllegalStateException e) {
            // Do nothing
        }
    }

    private void showViews() {
        removeAllViews();
        if (adapter == null) {
            requestLayout();
            return;
        }

        if (adapter.isEmpty()) {
            addEmptyView();
        } else {
            for (int i = 0; i < adapter.getCount(); i++) {
                // Add the views here
                addView(adapter.getView(i, null, this));
            }
        }
        requestLayout();
    }

    private void addEmptyView() {
        if (emptyResId == 0) {
            return;
        }
        TextView empty = (TextView) View.inflate(getContext(), R.layout.view_results_empty, null);
        empty.setText(emptyResId);
        addView(empty);
    }
}
