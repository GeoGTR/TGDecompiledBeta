package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;

public class PhotoEditorSeekBar extends View {
    private PhotoEditorSeekBarDelegate delegate;
    private Paint innerPaint = new Paint();
    private int maxValue;
    private int minValue;
    private Paint outerPaint = new Paint(1);
    private boolean pressed = false;
    private float progress = 0.0f;
    private int thumbDX = 0;
    private int thumbSize = AndroidUtilities.dp(16.0f);

    public interface PhotoEditorSeekBarDelegate {
        void onProgressChanged(int i, int i2);
    }

    public PhotoEditorSeekBar(Context context) {
        super(context);
        this.innerPaint.setColor(-11711155);
        this.outerPaint.setColor(-1);
    }

    public void setDelegate(PhotoEditorSeekBarDelegate photoEditorSeekBarDelegate) {
        this.delegate = photoEditorSeekBarDelegate;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        float measuredWidth = (float) ((int) (((float) (getMeasuredWidth() - this.thumbSize)) * this.progress));
        float f = 0.0f;
        float f2;
        if (motionEvent.getAction() == 0) {
            int measuredHeight = getMeasuredHeight();
            int i = this.thumbSize;
            f2 = (float) ((measuredHeight - i) / 2);
            if (measuredWidth - f2 <= x && x <= (((float) i) + measuredWidth) + f2 && y >= 0.0f && y <= ((float) getMeasuredHeight())) {
                this.pressed = true;
                this.thumbDX = (int) (x - measuredWidth);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }
        } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            if (this.pressed) {
                this.pressed = false;
                invalidate();
                return true;
            }
        } else if (motionEvent.getAction() == 2 && this.pressed) {
            f2 = (float) ((int) (x - ((float) this.thumbDX)));
            if (f2 >= 0.0f) {
                f = f2 > ((float) (getMeasuredWidth() - this.thumbSize)) ? (float) (getMeasuredWidth() - this.thumbSize) : f2;
            }
            this.progress = f / ((float) (getMeasuredWidth() - this.thumbSize));
            PhotoEditorSeekBarDelegate photoEditorSeekBarDelegate = this.delegate;
            if (photoEditorSeekBarDelegate != null) {
                photoEditorSeekBarDelegate.onProgressChanged(((Integer) getTag()).intValue(), getProgress());
            }
            invalidate();
            return true;
        }
        return false;
    }

    public void setProgress(int i) {
        setProgress(i, true);
    }

    public void setProgress(int i, boolean z) {
        int i2 = this.minValue;
        if (i >= i2) {
            i2 = this.maxValue;
            if (i <= i2) {
                i2 = i;
            }
        }
        i = this.minValue;
        this.progress = ((float) (i2 - i)) / ((float) (this.maxValue - i));
        invalidate();
        if (z) {
            PhotoEditorSeekBarDelegate photoEditorSeekBarDelegate = this.delegate;
            if (photoEditorSeekBarDelegate != null) {
                photoEditorSeekBarDelegate.onProgressChanged(((Integer) getTag()).intValue(), getProgress());
            }
        }
    }

    public int getProgress() {
        int i = this.minValue;
        return (int) (((float) i) + (this.progress * ((float) (this.maxValue - i))));
    }

    public void setMinMax(int i, int i2) {
        this.minValue = i;
        this.maxValue = i2;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        int measuredHeight = (getMeasuredHeight() - this.thumbSize) / 2;
        int measuredWidth = getMeasuredWidth();
        int i = this.thumbSize;
        measuredWidth = (int) (((float) (measuredWidth - i)) * this.progress);
        canvas.drawRect((float) (i / 2), (float) ((getMeasuredHeight() / 2) - AndroidUtilities.dp(1.0f)), (float) (getMeasuredWidth() - (this.thumbSize / 2)), (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(1.0f)), this.innerPaint);
        if (this.minValue == 0) {
            canvas.drawRect((float) (this.thumbSize / 2), (float) ((getMeasuredHeight() / 2) - AndroidUtilities.dp(1.0f)), (float) measuredWidth, (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(1.0f)), this.outerPaint);
        } else if (this.progress > 0.5f) {
            canvas.drawRect((float) ((getMeasuredWidth() / 2) - AndroidUtilities.dp(1.0f)), (float) ((getMeasuredHeight() - this.thumbSize) / 2), (float) (getMeasuredWidth() / 2), (float) ((getMeasuredHeight() + this.thumbSize) / 2), this.outerPaint);
            canvas.drawRect((float) (getMeasuredWidth() / 2), (float) ((getMeasuredHeight() / 2) - AndroidUtilities.dp(1.0f)), (float) measuredWidth, (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(1.0f)), this.outerPaint);
        } else {
            canvas.drawRect((float) (getMeasuredWidth() / 2), (float) ((getMeasuredHeight() - this.thumbSize) / 2), (float) ((getMeasuredWidth() / 2) + AndroidUtilities.dp(1.0f)), (float) ((getMeasuredHeight() + this.thumbSize) / 2), this.outerPaint);
            canvas.drawRect((float) measuredWidth, (float) ((getMeasuredHeight() / 2) - AndroidUtilities.dp(1.0f)), (float) (getMeasuredWidth() / 2), (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(1.0f)), this.outerPaint);
        }
        i = this.thumbSize;
        canvas.drawCircle((float) (measuredWidth + (i / 2)), (float) (measuredHeight + (i / 2)), (float) (i / 2), this.outerPaint);
    }
}
