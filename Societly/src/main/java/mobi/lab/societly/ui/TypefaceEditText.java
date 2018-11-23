package mobi.lab.societly.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import mobi.lab.societly.R;
import mobi.lab.societly.util.ViewUtil;

/**
 * Created by KMI on 10.11.14.
 */
public class TypefaceEditText extends EditText {

	public TypefaceEditText(Context context) {
		super(context);
	}

	public TypefaceEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}

	public TypefaceEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ViewUtil.setCustomFont(this, context, attrs, R.styleable.TypeFaceTextView, R.styleable.TypeFaceTextView_font);
	}


}
