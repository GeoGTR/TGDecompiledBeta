package org.telegram.ui.Charts;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.MotionEvent;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Charts.ChartPickerDelegate;

public class ChartPickerDelegate {
    CapturesData[] capturedStates = {null, null};
    public boolean disabled;
    public Rect leftPickerArea = new Rect();
    public Rect middlePickerArea = new Rect();
    public float minDistance = 0.1f;
    ValueAnimator moveToAnimator;
    public float moveToX;
    public float moveToY;
    public float pickerEnd = 1.0f;
    public float pickerStart = 0.7f;
    public float pickerWidth;
    public Rect rightPickerArea = new Rect();
    public long startTapTime;
    public boolean tryMoveTo;
    Listener view;

    interface Listener {
        void invalidate();

        void onPickerDataChanged();

        void onPickerJumpTo(float f, float f2, boolean z);
    }

    public ChartPickerDelegate(Listener listener) {
        this.view = listener;
    }

    public CapturesData getMiddleCaptured() {
        CapturesData[] capturesDataArr = this.capturedStates;
        if (capturesDataArr[0] != null && capturesDataArr[0].state == 4) {
            return capturesDataArr[0];
        }
        if (capturesDataArr[1] == null || capturesDataArr[1].state != 4) {
            return null;
        }
        return capturesDataArr[1];
    }

    public CapturesData getLeftCaptured() {
        CapturesData[] capturesDataArr = this.capturedStates;
        if (capturesDataArr[0] != null && capturesDataArr[0].state == 1) {
            return capturesDataArr[0];
        }
        if (capturesDataArr[1] == null || capturesDataArr[1].state != 1) {
            return null;
        }
        return capturesDataArr[1];
    }

    public CapturesData getRightCaptured() {
        CapturesData[] capturesDataArr = this.capturedStates;
        if (capturesDataArr[0] != null && capturesDataArr[0].state == 2) {
            return capturesDataArr[0];
        }
        if (capturesDataArr[1] == null || capturesDataArr[1].state != 2) {
            return null;
        }
        return capturesDataArr[1];
    }

    class CapturesData {
        ValueAnimator a;
        public float aValue = 0.0f;
        public int capturedX;
        public float end;
        ValueAnimator jumpToAnimator;
        public int lastMovingX;
        public float start;
        public final int state;

        public CapturesData(int i) {
            this.state = i;
        }

        public void captured() {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.a = ofFloat;
            ofFloat.setDuration(600);
            this.a.setInterpolator(BaseChartView.INTERPOLATOR);
            this.a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ChartPickerDelegate.CapturesData.this.lambda$captured$0$ChartPickerDelegate$CapturesData(valueAnimator);
                }
            });
            this.a.start();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$captured$0 */
        public /* synthetic */ void lambda$captured$0$ChartPickerDelegate$CapturesData(ValueAnimator valueAnimator) {
            this.aValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            ChartPickerDelegate.this.view.invalidate();
        }

        public void uncapture() {
            ValueAnimator valueAnimator = this.a;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            ValueAnimator valueAnimator2 = this.jumpToAnimator;
            if (valueAnimator2 != null) {
                valueAnimator2.cancel();
            }
        }
    }

    public boolean capture(int i, int i2, int i3) {
        if (this.disabled) {
            return false;
        }
        if (i3 == 0) {
            if (this.leftPickerArea.contains(i, i2)) {
                CapturesData[] capturesDataArr = this.capturedStates;
                if (capturesDataArr[0] != null) {
                    capturesDataArr[1] = capturesDataArr[0];
                }
                capturesDataArr[0] = new CapturesData(1);
                capturesDataArr[0].start = this.pickerStart;
                capturesDataArr[0].capturedX = i;
                capturesDataArr[0].lastMovingX = i;
                capturesDataArr[0].captured();
                ValueAnimator valueAnimator = this.moveToAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                return true;
            } else if (this.rightPickerArea.contains(i, i2)) {
                CapturesData[] capturesDataArr2 = this.capturedStates;
                if (capturesDataArr2[0] != null) {
                    capturesDataArr2[1] = capturesDataArr2[0];
                }
                capturesDataArr2[0] = new CapturesData(2);
                capturesDataArr2[0].end = this.pickerEnd;
                capturesDataArr2[0].capturedX = i;
                capturesDataArr2[0].lastMovingX = i;
                capturesDataArr2[0].captured();
                ValueAnimator valueAnimator2 = this.moveToAnimator;
                if (valueAnimator2 != null) {
                    valueAnimator2.cancel();
                }
                return true;
            } else if (this.middlePickerArea.contains(i, i2)) {
                CapturesData[] capturesDataArr3 = this.capturedStates;
                capturesDataArr3[0] = new CapturesData(4);
                capturesDataArr3[0].end = this.pickerEnd;
                capturesDataArr3[0].start = this.pickerStart;
                capturesDataArr3[0].capturedX = i;
                capturesDataArr3[0].lastMovingX = i;
                capturesDataArr3[0].captured();
                ValueAnimator valueAnimator3 = this.moveToAnimator;
                if (valueAnimator3 != null) {
                    valueAnimator3.cancel();
                }
                return true;
            } else {
                Rect rect = this.leftPickerArea;
                if (i2 < rect.bottom && i2 > rect.top) {
                    this.tryMoveTo = true;
                    this.moveToX = (float) i;
                    this.moveToY = (float) i2;
                    this.startTapTime = System.currentTimeMillis();
                    ValueAnimator valueAnimator4 = this.moveToAnimator;
                    if (valueAnimator4 != null) {
                        if (valueAnimator4.isRunning()) {
                            this.view.onPickerJumpTo(this.pickerStart, this.pickerEnd, true);
                        }
                        this.moveToAnimator.cancel();
                    }
                    return true;
                }
            }
        } else if (i3 == 1) {
            CapturesData[] capturesDataArr4 = this.capturedStates;
            if (capturesDataArr4[0] == null || capturesDataArr4[0].state == 4) {
                return false;
            }
            if (this.leftPickerArea.contains(i, i2)) {
                CapturesData[] capturesDataArr5 = this.capturedStates;
                if (capturesDataArr5[0].state != 1) {
                    capturesDataArr5[1] = new CapturesData(1);
                    capturesDataArr5[1].start = this.pickerStart;
                    capturesDataArr5[1].capturedX = i;
                    capturesDataArr5[1].lastMovingX = i;
                    capturesDataArr5[1].captured();
                    ValueAnimator valueAnimator5 = this.moveToAnimator;
                    if (valueAnimator5 != null) {
                        valueAnimator5.cancel();
                    }
                    return true;
                }
            }
            if (this.rightPickerArea.contains(i, i2)) {
                CapturesData[] capturesDataArr6 = this.capturedStates;
                if (capturesDataArr6[0].state == 2) {
                    return false;
                }
                capturesDataArr6[1] = new CapturesData(2);
                capturesDataArr6[1].end = this.pickerEnd;
                capturesDataArr6[1].capturedX = i;
                capturesDataArr6[1].lastMovingX = i;
                capturesDataArr6[1].captured();
                ValueAnimator valueAnimator6 = this.moveToAnimator;
                if (valueAnimator6 != null) {
                    valueAnimator6.cancel();
                }
                return true;
            }
        }
        return false;
    }

    public boolean captured() {
        return this.capturedStates[0] != null || this.tryMoveTo;
    }

    public boolean move(int i, int i2, int i3) {
        CapturesData capturesData;
        boolean z = false;
        if (this.tryMoveTo || (capturesData = this.capturedStates[i3]) == null) {
            return false;
        }
        int i4 = capturesData.state;
        float f = capturesData.start;
        float f2 = capturesData.end;
        int i5 = capturesData.capturedX;
        capturesData.lastMovingX = i;
        if (i4 == 1) {
            float f3 = f - (((float) (i5 - i)) / this.pickerWidth);
            this.pickerStart = f3;
            if (f3 < 0.0f) {
                this.pickerStart = 0.0f;
            }
            float f4 = this.pickerEnd;
            float f5 = this.minDistance;
            if (f4 - this.pickerStart < f5) {
                this.pickerStart = f4 - f5;
            }
            z = true;
        }
        if (i4 == 2) {
            float f6 = f2 - (((float) (i5 - i)) / this.pickerWidth);
            this.pickerEnd = f6;
            if (f6 > 1.0f) {
                this.pickerEnd = 1.0f;
            }
            float f7 = this.pickerEnd;
            float f8 = this.pickerStart;
            float f9 = this.minDistance;
            if (f7 - f8 < f9) {
                this.pickerEnd = f8 + f9;
            }
            z = true;
        }
        if (i4 == 4) {
            float var_ = (float) (i5 - i);
            float var_ = this.pickerWidth;
            float var_ = f - (var_ / var_);
            this.pickerStart = var_;
            this.pickerEnd = f2 - (var_ / var_);
            if (var_ < 0.0f) {
                this.pickerStart = 0.0f;
                this.pickerEnd = f2 - f;
            }
            if (this.pickerEnd > 1.0f) {
                this.pickerEnd = 1.0f;
                this.pickerStart = 1.0f - (f2 - f);
            }
            z = true;
        }
        if (z) {
            this.view.onPickerDataChanged();
        }
        return true;
    }

    public boolean uncapture(MotionEvent motionEvent, int i) {
        float f;
        float f2;
        if (i != 0) {
            CapturesData[] capturesDataArr = this.capturedStates;
            if (capturesDataArr[1] != null) {
                capturesDataArr[1].uncapture();
            }
            this.capturedStates[1] = null;
        } else if (this.tryMoveTo) {
            this.tryMoveTo = false;
            float x = this.moveToX - motionEvent.getX();
            float y = this.moveToY - motionEvent.getY();
            if (motionEvent.getAction() == 1 && System.currentTimeMillis() - this.startTapTime < 300 && Math.sqrt((double) ((x * x) + (y * y))) < ((double) AndroidUtilities.dp(10.0f))) {
                float f3 = (this.moveToX - BaseChartView.HORIZONTAL_PADDING) / this.pickerWidth;
                float f4 = this.pickerEnd;
                float f5 = this.pickerStart;
                float f6 = f4 - f5;
                float f7 = f6 / 2.0f;
                float f8 = f3 - f7;
                float f9 = f3 + f7;
                if (f8 < 0.0f) {
                    f = f6;
                    f2 = 0.0f;
                } else if (f9 > 1.0f) {
                    f2 = 1.0f - f6;
                    f = 1.0f;
                } else {
                    f = f9;
                    f2 = f8;
                }
                this.moveToAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                this.view.onPickerJumpTo(f2, f, true);
                this.moveToAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(f5, f2, f4, f) {
                    public final /* synthetic */ float f$1;
                    public final /* synthetic */ float f$2;
                    public final /* synthetic */ float f$3;
                    public final /* synthetic */ float f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ChartPickerDelegate.this.lambda$uncapture$0$ChartPickerDelegate(this.f$1, this.f$2, this.f$3, this.f$4, valueAnimator);
                    }
                });
                this.moveToAnimator.setInterpolator(BaseChartView.INTERPOLATOR);
                this.moveToAnimator.start();
            }
            return true;
        } else {
            CapturesData[] capturesDataArr2 = this.capturedStates;
            if (capturesDataArr2[0] != null) {
                capturesDataArr2[0].uncapture();
            }
            CapturesData[] capturesDataArr3 = this.capturedStates;
            capturesDataArr3[0] = null;
            if (capturesDataArr3[1] != null) {
                capturesDataArr3[0] = capturesDataArr3[1];
                capturesDataArr3[1] = null;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$uncapture$0 */
    public /* synthetic */ void lambda$uncapture$0$ChartPickerDelegate(float f, float f2, float f3, float f4, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.pickerStart = f + ((f2 - f) * floatValue);
        this.pickerEnd = f3 + ((f4 - f3) * floatValue);
        this.view.onPickerJumpTo(f2, f4, false);
    }

    public void uncapture() {
        CapturesData[] capturesDataArr = this.capturedStates;
        if (capturesDataArr[0] != null) {
            capturesDataArr[0].uncapture();
        }
        CapturesData[] capturesDataArr2 = this.capturedStates;
        if (capturesDataArr2[1] != null) {
            capturesDataArr2[1].uncapture();
        }
        CapturesData[] capturesDataArr3 = this.capturedStates;
        capturesDataArr3[0] = null;
        capturesDataArr3[1] = null;
    }
}
