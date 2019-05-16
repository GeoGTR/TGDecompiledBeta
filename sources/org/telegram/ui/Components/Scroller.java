package org.telegram.ui.Components;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class Scroller {
    private static float DECELERATION_RATE = ((float) (Math.log(0.75d) / Math.log(0.9d)));
    private static final int DEFAULT_DURATION = 250;
    private static float END_TENSION = (1.0f - START_TENSION);
    private static final int FLING_MODE = 1;
    private static final int NB_SAMPLES = 100;
    private static final int SCROLL_MODE = 0;
    private static final float[] SPLINE = new float[101];
    private static float START_TENSION = 0.4f;
    private static float sViscousFluidNormalize;
    private static float sViscousFluidScale = 8.0f;
    private int mCurrX;
    private int mCurrY;
    private float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;

    static {
        float f = 0.0f;
        for (int i = 0; i <= 100; i++) {
            float f2;
            float f3;
            float f4 = ((float) i) / 100.0f;
            float f5 = 1.0f;
            while (true) {
                float f6 = ((f5 - f) / 2.0f) + f;
                float f7 = 1.0f - f6;
                f2 = (3.0f * f6) * f7;
                f3 = (f6 * f6) * f6;
                f7 = (((f7 * START_TENSION) + (END_TENSION * f6)) * f2) + f3;
                if (((double) Math.abs(f7 - f4)) < 1.0E-5d) {
                    break;
                } else if (f7 > f4) {
                    f5 = f6;
                } else {
                    f = f6;
                }
            }
            SPLINE[i] = f2 + f3;
        }
        SPLINE[100] = 1.0f;
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    public Scroller(Context context) {
        this(context, null);
    }

    public Scroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public Scroller(Context context, Interpolator interpolator, boolean z) {
        this.mFinished = true;
        this.mInterpolator = interpolator;
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        this.mFlywheel = z;
    }

    public final void setFriction(float f) {
        this.mDeceleration = computeDeceleration(f);
    }

    private float computeDeceleration(float f) {
        return (this.mPpi * 386.0878f) * f;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public final void forceFinished(boolean z) {
        this.mFinished = z;
    }

    public final int getDuration() {
        return this.mDuration;
    }

    public final int getCurrX() {
        return this.mCurrX;
    }

    public final int getCurrY() {
        return this.mCurrY;
    }

    public float getCurrVelocity() {
        return this.mVelocity - ((this.mDeceleration * ((float) timePassed())) / 2000.0f);
    }

    public final int getStartX() {
        return this.mStartX;
    }

    public final int getStartY() {
        return this.mStartY;
    }

    public final int getFinalX() {
        return this.mFinalX;
    }

    public final int getFinalY() {
        return this.mFinalY;
    }

    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int currentAnimationTimeMillis = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        int i = this.mDuration;
        if (currentAnimationTimeMillis < i) {
            int i2 = this.mMode;
            if (i2 == 0) {
                float f = ((float) currentAnimationTimeMillis) * this.mDurationReciprocal;
                Interpolator interpolator = this.mInterpolator;
                if (interpolator == null) {
                    f = viscousFluid(f);
                } else {
                    f = interpolator.getInterpolation(f);
                }
                this.mCurrX = this.mStartX + Math.round(this.mDeltaX * f);
                this.mCurrY = this.mStartY + Math.round(f * this.mDeltaY);
            } else if (i2 == 1) {
                float f2 = ((float) currentAnimationTimeMillis) / ((float) i);
                i2 = (int) (f2 * 100.0f);
                float f3 = ((float) i2) / 100.0f;
                int i3 = i2 + 1;
                float f4 = ((float) i3) / 100.0f;
                float[] fArr = SPLINE;
                float f5 = fArr[i2];
                f5 += ((f2 - f3) / (f4 - f3)) * (fArr[i3] - f5);
                i = this.mStartX;
                this.mCurrX = i + Math.round(((float) (this.mFinalX - i)) * f5);
                this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                i = this.mStartY;
                this.mCurrY = i + Math.round(f5 * ((float) (this.mFinalY - i)));
                this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                    this.mFinished = true;
                }
            }
        } else {
            this.mCurrX = this.mFinalX;
            this.mCurrY = this.mFinalY;
            this.mFinished = true;
        }
        return true;
    }

    public void startScroll(int i, int i2, int i3, int i4) {
        startScroll(i, i2, i3, i4, 250);
    }

    public void startScroll(int i, int i2, int i3, int i4, int i5) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = i5;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        this.mFinalX = i + i3;
        this.mFinalY = i2 + i4;
        this.mDeltaX = (float) i3;
        this.mDeltaY = (float) i4;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00aa  */
    public void fling(int r17, int r18, int r19, int r20, int r21, int r22, int r23, int r24) {
        /*
        r16 = this;
        r0 = r16;
        r1 = r17;
        r2 = r18;
        r3 = r0.mFlywheel;
        if (r3 == 0) goto L_0x0053;
    L_0x000a:
        r3 = r0.mFinished;
        if (r3 != 0) goto L_0x0053;
    L_0x000e:
        r3 = r16.getCurrVelocity();
        r4 = r0.mFinalX;
        r5 = r0.mStartX;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mFinalY;
        r6 = r0.mStartY;
        r5 = r5 - r6;
        r5 = (float) r5;
        r6 = r4 * r4;
        r7 = r5 * r5;
        r6 = r6 + r7;
        r6 = (double) r6;
        r6 = java.lang.Math.sqrt(r6);
        r6 = (float) r6;
        r4 = r4 / r6;
        r5 = r5 / r6;
        r4 = r4 * r3;
        r5 = r5 * r3;
        r3 = r19;
        r6 = (float) r3;
        r7 = java.lang.Math.signum(r6);
        r8 = java.lang.Math.signum(r4);
        r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r7 != 0) goto L_0x0055;
    L_0x003e:
        r7 = r20;
        r8 = (float) r7;
        r9 = java.lang.Math.signum(r8);
        r10 = java.lang.Math.signum(r5);
        r9 = (r9 > r10 ? 1 : (r9 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x0057;
    L_0x004d:
        r6 = r6 + r4;
        r3 = (int) r6;
        r8 = r8 + r5;
        r4 = (int) r8;
        r7 = r4;
        goto L_0x0057;
    L_0x0053:
        r3 = r19;
    L_0x0055:
        r7 = r20;
    L_0x0057:
        r4 = 1;
        r0.mMode = r4;
        r4 = 0;
        r0.mFinished = r4;
        r4 = r3 * r3;
        r5 = r7 * r7;
        r4 = r4 + r5;
        r4 = (double) r4;
        r4 = java.lang.Math.sqrt(r4);
        r4 = (float) r4;
        r0.mVelocity = r4;
        r5 = NUM; // 0x44480000 float:800.0 double:5.65986426E-315;
        r6 = START_TENSION;
        r6 = r6 * r4;
        r6 = r6 / r5;
        r8 = (double) r6;
        r8 = java.lang.Math.log(r8);
        r10 = NUM; // 0x408fNUM float:0.0 double:1000.0;
        r6 = DECELERATION_RATE;
        r12 = (double) r6;
        r14 = NUM; // 0x3ffNUM float:0.0 double:1.0;
        java.lang.Double.isNaN(r12);
        r12 = r12 - r14;
        r12 = r8 / r12;
        r12 = java.lang.Math.exp(r12);
        r12 = r12 * r10;
        r6 = (int) r12;
        r0.mDuration = r6;
        r10 = android.view.animation.AnimationUtils.currentAnimationTimeMillis();
        r0.mStartTime = r10;
        r0.mStartX = r1;
        r0.mStartY = r2;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = 0;
        r11 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r11 != 0) goto L_0x00a3;
    L_0x00a0:
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x00a5;
    L_0x00a3:
        r3 = (float) r3;
        r3 = r3 / r4;
    L_0x00a5:
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r10 != 0) goto L_0x00aa;
    L_0x00a9:
        goto L_0x00ac;
    L_0x00aa:
        r6 = (float) r7;
        r6 = r6 / r4;
    L_0x00ac:
        r4 = (double) r5;
        r7 = DECELERATION_RATE;
        r10 = (double) r7;
        r12 = (double) r7;
        java.lang.Double.isNaN(r12);
        r12 = r12 - r14;
        java.lang.Double.isNaN(r10);
        r10 = r10 / r12;
        r10 = r10 * r8;
        r7 = java.lang.Math.exp(r10);
        java.lang.Double.isNaN(r4);
        r4 = r4 * r7;
        r4 = (int) r4;
        r5 = r21;
        r0.mMinX = r5;
        r5 = r22;
        r0.mMaxX = r5;
        r5 = r23;
        r0.mMinY = r5;
        r5 = r24;
        r0.mMaxY = r5;
        r4 = (float) r4;
        r3 = r3 * r4;
        r3 = java.lang.Math.round(r3);
        r1 = r1 + r3;
        r0.mFinalX = r1;
        r1 = r0.mFinalX;
        r3 = r0.mMaxX;
        r1 = java.lang.Math.min(r1, r3);
        r0.mFinalX = r1;
        r1 = r0.mFinalX;
        r3 = r0.mMinX;
        r1 = java.lang.Math.max(r1, r3);
        r0.mFinalX = r1;
        r4 = r4 * r6;
        r1 = java.lang.Math.round(r4);
        r1 = r1 + r2;
        r0.mFinalY = r1;
        r1 = r0.mFinalY;
        r2 = r0.mMaxY;
        r1 = java.lang.Math.min(r1, r2);
        r0.mFinalY = r1;
        r1 = r0.mFinalY;
        r2 = r0.mMinY;
        r1 = java.lang.Math.max(r1, r2);
        r0.mFinalY = r1;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.Scroller.fling(int, int, int, int, int, int, int, int):void");
    }

    static float viscousFluid(float f) {
        f *= sViscousFluidScale;
        if (f < 1.0f) {
            f -= 1.0f - ((float) Math.exp((double) (-f)));
        } else {
            f = ((1.0f - ((float) Math.exp((double) (1.0f - f)))) * 0.63212055f) + 0.36787945f;
        }
        return f * sViscousFluidNormalize;
    }

    public void abortAnimation() {
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
    }

    public void extendDuration(int i) {
        this.mDuration = timePassed() + i;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
        this.mFinished = false;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }

    public void setFinalX(int i) {
        this.mFinalX = i;
        this.mDeltaX = (float) (this.mFinalX - this.mStartX);
        this.mFinished = false;
    }

    public void setFinalY(int i) {
        this.mFinalY = i;
        this.mDeltaY = (float) (this.mFinalY - this.mStartY);
        this.mFinished = false;
    }

    public boolean isScrollingInDirection(float f, float f2) {
        return !this.mFinished && Math.signum(f) == Math.signum((float) (this.mFinalX - this.mStartX)) && Math.signum(f2) == Math.signum((float) (this.mFinalY - this.mStartY));
    }
}
