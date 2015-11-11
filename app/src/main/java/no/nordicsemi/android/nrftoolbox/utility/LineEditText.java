package no.nordicsemi.android.nrftoolbox.utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by hj on 2015/10/27.
 */
public class LineEditText extends EditText {
    private Paint paint;
    public LineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(R.color.LineEditTextColor);
        paint.setAntiAlias(true);
    }

    protected void onDraw(Canvas canvas) {
               super.onDraw(canvas);
               // 得到总行数
               int lineCount = getLineCount();
               // 得到每行的高度
               int lineHeight = getLineHeight();
               // 根据行数循环画线
               for (int i = 0; i < lineCount; i++) {
                       int lineY = (i + 1) * lineHeight;
                       canvas.drawLine(0, lineY, this.getWidth(), lineY, paint);
                   }
    }
}
