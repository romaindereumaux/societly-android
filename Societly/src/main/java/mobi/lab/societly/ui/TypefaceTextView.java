package mobi.lab.societly.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import mobi.lab.societly.R;
import mobi.lab.societly.util.ViewUtil;

/**
 * Created by KMI on 10.11.14.
 */
public class TypefaceTextView extends TextView {

	public TypefaceTextView(Context context) {
		super(context);
	}

	public TypefaceTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}

	public TypefaceTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}

}
