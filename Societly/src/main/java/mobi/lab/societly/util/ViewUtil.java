package mobi.lab.societly.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

import mobi.lab.societly.R;
import mobi.lab.societly.dto.Answer;

public class ViewUtil {

    public static void setColor(Drawable drawable, int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public static int getAnswerColor(Context context, Answer answer) {
        return getAnswerTypeColor(context.getResources(), answer.getValue());
    }

    private static int getAnswerTypeColor(Resources res, Answer.AnswerType type) {
        int color = getColor(res, R.color.bg_default);
        if (type == null) {
            return color;
        }
        switch (type) {
            case NEUTRAL:
                color = getColor(res, R.color.bg_neutral);
                break;
            case YES:
                color = getColor(res, R.color.bg_yes);
                break;
            case SKIP:
                color = getColor(res, R.color.bg_skip);
                break;
            case NO:
                color = getColor(res, R.color.bg_no);
                break;
        }
        return color;
    }

    public static Answer.AnswerType getAreaAnswerType(int area) {
        Answer.AnswerType type = null;
        switch (area) {
            case SwipeFlingAdapterView.onFlingListener.AREA_TOP:
                type = Answer.AnswerType.NEUTRAL;
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_RIGHT:
                type = Answer.AnswerType.YES;
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_BOTTOM:
                type = Answer.AnswerType.SKIP;
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_LEFT:
                type = Answer.AnswerType.NO;
                break;
        }
        return type;
    }

    public static int getAreaColor(Context context, int area) {
        Answer.AnswerType type = getAreaAnswerType(area);
        return getAnswerTypeColor(context.getResources(), type);
    }

    /**
     * Returns an array of alpha values in the order: [yes, no, neutral, skip]
     * @param answer
     * @return
     */
    public static float[] getAnswerHintAlphas(Answer answer) {
        float yesAlpha = 0;
        float noAlpha = 0;
        float neutralAlpha = 0;
        float skipAlpha = 0;

        if (answer != null) {
            switch (answer.getValue()) {
                case YES:
                    yesAlpha = 1;
                    break;
                case NO:
                    noAlpha = 1;
                    break;
                case NEUTRAL:
                    neutralAlpha = 1;
                    break;
                case SKIP:
                    skipAlpha = 1;
                    break;
            }
        }
        return new float[] {yesAlpha, noAlpha, neutralAlpha, skipAlpha};
    }

    public static int getColor(Resources res, int id) throws Resources.NotFoundException {
        // 23 and newer have a new function..
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return res.getColor(id, null);
        } else {
            return res.getColor(id);
        }
    }

    public static int getScreenWidth(final Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getDimensionInPx(Context context, int resId) {
        return context.getResources().getDimensionPixelSize(resId);
    }

    public static String getPaddedNumber(int number) {
        return String.format("%02d", number);
    }

    private static final Hashtable<String, SoftReference<Typeface>> fontCache = new Hashtable<String, SoftReference<Typeface>>();

    public static void setCustomFont(View textViewOrButton, Context ctx, AttributeSet attrs, int[] attributeSet, int asset) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, attributeSet);
        String font = a.getString(asset);
        setCustomFont(textViewOrButton, ctx, font);
        a.recycle();
    }

    public static boolean setCustomFont(View textViewOrButton, Context ctx, String font) {
        Typeface tf = null;
        try {
            tf = getFont(ctx, font);
            if (textViewOrButton instanceof TextView) {
                ((TextView) textViewOrButton).setTypeface(tf);
            } else {
                ((Button) textViewOrButton).setTypeface(tf);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static Typeface getFont(final Context c, final String font) {
        // These font style ints are defined in values/constants.xml
        String fontPath = font.startsWith("font/") ? font : "fonts/" + font;
        synchronized (fontCache) {
            SoftReference<Typeface> ref = fontCache.get(fontPath);
            if (ref != null) {
                if (ref.get() != null) {
                    return ref.get();
                }
            }

            Typeface typeface = Typeface.createFromAsset(c.getAssets(), fontPath);
            fontCache.put(fontPath, new SoftReference<Typeface>(typeface));
            return typeface;
        }
    }

    public static int blendColors(int foreground, int background) {
        int bgRed = Color.red(background);
        int bgGreen = Color.green(background);
        int bgBlue = Color.blue(background);

        int fgRed = Color.red(foreground);
        int fgGreen = Color.green(foreground);
        int fgBlue = Color.blue(foreground);

        float alpha = Color.alpha(foreground) / 255.0f;

        int red = blendAlpha(alpha, fgRed, bgRed);
        int green = blendAlpha(alpha, fgGreen, bgGreen);
        int blue = blendAlpha(alpha, fgBlue, bgBlue);
        int result = Color.rgb(red, green, blue);
//        Log log = Log.getInstance("ViewUtil");
//        log.d("getColorWithoutAlpha red=" + fgRed + " => " + red);
//        log.d("getColorWithoutAlpha red=" + fgGreen + " => " + green);
//        log.d("getColorWithoutAlpha red=" + fgBlue + " => " + blue);
//        log.d("getColorWithoutAlpha alpha=" + alpha + " color=" + getColorString(foreground) + " blended=" + getColorString(result));
        return result;
    }

    public static int setAlpha(float alpha, int color) {
        return Color.argb(
                Math.round(255 * alpha),
                Color.red(color), Color.green(color), Color.blue(color)
        );
    }

    public static String getColorString(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return "#" + Integer.toHexString(alpha) + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
    }

    private static int blendAlpha(float alpha, int fgColor, int bgColor) {
        return Math.round(fgColor * alpha + (1 - alpha) * bgColor);
    }
}
