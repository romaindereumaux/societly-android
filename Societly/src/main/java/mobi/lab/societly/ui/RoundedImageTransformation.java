package mobi.lab.societly.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class RoundedImageTransformation implements Transformation {
    private int mCornerRadius = 0;

    public RoundedImageTransformation(int cornerRadius) {
        this.mCornerRadius = cornerRadius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap image = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(image);
        canvas.drawARGB(0, 0, 0, 0);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawRoundRect(new RectF(rect), this.mCornerRadius, this.mCornerRadius, paint);

        paint.setXfermode(new PorterDuffXfermode((PorterDuff.Mode.SRC_IN)));
        canvas.drawBitmap(source, rect, rect, paint);

        Bitmap output = Bitmap.createBitmap(width, height, source.getConfig());
        canvas.setBitmap(output);
        canvas.drawARGB(0, 0, 0, 0);

        rect = new Rect(0, 0, width, height);

        paint.setXfermode(null);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(new RectF(rect), this.mCornerRadius, this.mCornerRadius, paint);

        canvas.drawBitmap(image, 0, 0, null);

        if (source != output) {
            source.recycle();
        }
        return output;
    }

    @Override
    public String key() {
        return "bitmapBorder(cornerRadius=" + this.mCornerRadius;
    }

}