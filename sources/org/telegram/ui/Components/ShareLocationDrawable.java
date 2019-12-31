package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import org.telegram.messenger.AndroidUtilities;

public class ShareLocationDrawable extends Drawable {
    private int currentType;
    private Drawable drawable;
    private Drawable drawableLeft;
    private Drawable drawableRight;
    private long lastUpdateTime = 0;
    private float[] progress = new float[]{0.0f, -0.5f};

    public int getOpacity() {
        return -2;
    }

    public void setAlpha(int i) {
    }

    public ShareLocationDrawable(Context context, int i) {
        this.currentType = i;
        if (i == 4) {
            this.drawable = context.getResources().getDrawable(NUM);
            this.drawableLeft = context.getResources().getDrawable(NUM);
            this.drawableRight = context.getResources().getDrawable(NUM);
        } else if (i == 3) {
            this.drawable = context.getResources().getDrawable(NUM);
            this.drawableLeft = context.getResources().getDrawable(NUM);
            this.drawableRight = context.getResources().getDrawable(NUM);
        } else if (i == 2) {
            this.drawable = context.getResources().getDrawable(NUM);
            this.drawableLeft = context.getResources().getDrawable(NUM);
            this.drawableRight = context.getResources().getDrawable(NUM);
        } else if (i == 1) {
            this.drawable = context.getResources().getDrawable(NUM);
            this.drawableLeft = context.getResources().getDrawable(NUM);
            this.drawableRight = context.getResources().getDrawable(NUM);
        } else {
            this.drawable = context.getResources().getDrawable(NUM);
            this.drawableLeft = context.getResources().getDrawable(NUM);
            this.drawableRight = context.getResources().getDrawable(NUM);
        }
    }

    private void update() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - this.lastUpdateTime;
        this.lastUpdateTime = currentTimeMillis;
        currentTimeMillis = 16;
        if (j <= 16) {
            currentTimeMillis = j;
        }
        for (int i = 0; i < 2; i++) {
            float[] fArr = this.progress;
            if (fArr[i] >= 1.0f) {
                fArr[i] = 0.0f;
            }
            fArr = this.progress;
            fArr[i] = fArr[i] + (((float) currentTimeMillis) / 1300.0f);
            if (fArr[i] > 1.0f) {
                fArr[i] = 1.0f;
            }
        }
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        Canvas canvas2 = canvas;
        int intrinsicWidth = this.drawable.getIntrinsicWidth();
        int intrinsicHeight = this.drawable.getIntrinsicHeight();
        int i = this.currentType;
        int i2 = 3;
        int i3 = 4;
        int i4 = 1;
        if (i == 4) {
            i = AndroidUtilities.dp(24.0f);
        } else if (i == 3) {
            i = AndroidUtilities.dp(44.0f);
        } else if (i == 2) {
            i = AndroidUtilities.dp(32.0f);
        } else if (i == 1) {
            i = AndroidUtilities.dp(30.0f);
        } else {
            i = AndroidUtilities.dp(120.0f);
        }
        int intrinsicHeight2 = getBounds().top + ((getIntrinsicHeight() - i) / 2);
        int intrinsicWidth2 = getBounds().left + ((getIntrinsicWidth() - i) / 2);
        intrinsicWidth += intrinsicWidth2;
        this.drawable.setBounds(intrinsicWidth2, intrinsicHeight2, intrinsicWidth, intrinsicHeight2 + intrinsicHeight);
        this.drawable.draw(canvas2);
        i = 0;
        while (i < 2) {
            float[] fArr = this.progress;
            if (fArr[i] >= 0.0f) {
                int dp;
                int dp2;
                int dp3;
                int dp4;
                int dp5;
                float f;
                float f2 = (fArr[i] * 0.5f) + 0.5f;
                int i5 = this.currentType;
                if (i5 == i3) {
                    i5 = AndroidUtilities.dp(2.5f * f2);
                    dp = AndroidUtilities.dp(f2 * 6.5f);
                    dp2 = AndroidUtilities.dp(this.progress[i] * 6.0f);
                    dp3 = (intrinsicWidth2 + AndroidUtilities.dp(3.0f)) - dp2;
                    dp4 = (intrinsicHeight2 + (intrinsicHeight / 2)) - AndroidUtilities.dp(2.0f);
                    dp5 = AndroidUtilities.dp(3.0f);
                } else if (i5 == i2) {
                    i5 = AndroidUtilities.dp(5.0f * f2);
                    dp = AndroidUtilities.dp(f2 * 18.0f);
                    dp2 = AndroidUtilities.dp(this.progress[i] * 15.0f);
                    dp3 = (AndroidUtilities.dp(2.0f) + intrinsicWidth2) - dp2;
                    dp4 = ((intrinsicHeight / 2) + intrinsicHeight2) - AndroidUtilities.dp(7.0f);
                    dp5 = AndroidUtilities.dp(2.0f);
                } else if (i5 == 2) {
                    i5 = AndroidUtilities.dp(5.0f * f2);
                    dp = AndroidUtilities.dp(f2 * 18.0f);
                    dp2 = AndroidUtilities.dp(this.progress[i] * 15.0f);
                    dp3 = (AndroidUtilities.dp(2.0f) + intrinsicWidth2) - dp2;
                    dp4 = intrinsicHeight2 + (intrinsicHeight / 2);
                    dp5 = AndroidUtilities.dp(2.0f);
                } else if (i5 == i4) {
                    i5 = AndroidUtilities.dp(2.5f * f2);
                    dp = AndroidUtilities.dp(f2 * 6.5f);
                    dp2 = AndroidUtilities.dp(this.progress[i] * 6.0f);
                    dp3 = (AndroidUtilities.dp(7.0f) + intrinsicWidth2) - dp2;
                    dp4 = intrinsicHeight2 + (intrinsicHeight / 2);
                    dp5 = AndroidUtilities.dp(7.0f);
                } else {
                    i5 = AndroidUtilities.dp(5.0f * f2);
                    dp = AndroidUtilities.dp(f2 * 18.0f);
                    dp2 = AndroidUtilities.dp(this.progress[i] * 15.0f);
                    dp3 = (intrinsicWidth2 + AndroidUtilities.dp(42.0f)) - dp2;
                    dp4 = (intrinsicHeight2 + (intrinsicHeight / 2)) - AndroidUtilities.dp(7.0f);
                    dp5 = AndroidUtilities.dp(42.0f);
                }
                dp5 = (intrinsicWidth - dp5) + dp2;
                float[] fArr2 = this.progress;
                if (fArr2[i] < 0.5f) {
                    f = fArr2[i] / 0.5f;
                } else {
                    f = 1.0f - ((fArr2[i] - 0.5f) / 0.5f);
                }
                dp2 = (int) (f * 255.0f);
                this.drawableLeft.setAlpha(dp2);
                i3 = dp4 - dp;
                dp = dp4 + dp;
                this.drawableLeft.setBounds(dp3 - i5, i3, dp3 + i5, dp);
                this.drawableLeft.draw(canvas2);
                this.drawableRight.setAlpha(dp2);
                this.drawableRight.setBounds(dp5 - i5, i3, dp5 + i5, dp);
                this.drawableRight.draw(canvas2);
            }
            i++;
            i2 = 3;
            i3 = 4;
            i4 = 1;
        }
        update();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.drawable.setColorFilter(colorFilter);
        this.drawableLeft.setColorFilter(colorFilter);
        this.drawableRight.setColorFilter(colorFilter);
    }

    public int getIntrinsicWidth() {
        int i = this.currentType;
        if (i == 4) {
            return AndroidUtilities.dp(42.0f);
        }
        if (i == 3) {
            return AndroidUtilities.dp(100.0f);
        }
        if (i == 2) {
            return AndroidUtilities.dp(74.0f);
        }
        if (i == 1) {
            return AndroidUtilities.dp(40.0f);
        }
        return AndroidUtilities.dp(120.0f);
    }

    public int getIntrinsicHeight() {
        int i = this.currentType;
        if (i == 4) {
            return AndroidUtilities.dp(42.0f);
        }
        if (i == 3) {
            return AndroidUtilities.dp(100.0f);
        }
        if (i == 2) {
            return AndroidUtilities.dp(74.0f);
        }
        if (i == 1) {
            return AndroidUtilities.dp(40.0f);
        }
        return AndroidUtilities.dp(180.0f);
    }
}
