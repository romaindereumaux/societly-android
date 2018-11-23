package mobi.lab.societly.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import mobi.lab.societly.R;
import mobi.lab.societly.util.ViewUtil;

public class TypefaceButton extends Button {

	public TypefaceButton(Context context) {
		super(context);
	}

	public TypefaceButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}

	public TypefaceButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}

}
