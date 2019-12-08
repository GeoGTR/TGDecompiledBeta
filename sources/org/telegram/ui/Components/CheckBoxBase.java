package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;
import androidx.annotation.Keep;
import org.telegram.messenger.AndroidUtilities;

public class CheckBoxBase {
    private static Paint eraser;
    private static Paint paint;
    private boolean attachedToWindow;
    private String background2ColorKey;
    private float backgroundAlpha;
    private String backgroundColorKey;
    private Paint backgroundPaint;
    private Canvas bitmapCanvas;
    private Rect bounds;
    private ObjectAnimator checkAnimator;
    private String checkColorKey;
    private Paint checkPaint;
    private String checkedText;
    private int drawBackgroundAsArc;
    private Bitmap drawBitmap;
    private boolean drawUnchecked;
    private boolean enabled;
    private boolean isChecked;
    private View parentView;
    private Path path;
    private float progress;
    private ProgressDelegate progressDelegate;
    private RectF rect;
    private float size;
    private TextPaint textPaint;
    private boolean useDefaultCheck;

    public interface ProgressDelegate {
        void setProgress(float f);
    }

    public CheckBoxBase(View view) {
        this(view, 21);
    }

    public CheckBoxBase(View view, int i) {
        this.bounds = new Rect();
        this.rect = new RectF();
        this.path = new Path();
        this.enabled = true;
        this.backgroundAlpha = 1.0f;
        this.checkColorKey = "checkboxCheck";
        String str = "chat_serviceBackground";
        this.backgroundColorKey = str;
        this.background2ColorKey = str;
        this.drawUnchecked = true;
        this.size = 21.0f;
        this.parentView = view;
        this.size = (float) i;
        if (paint == null) {
            paint = new Paint(1);
            eraser = new Paint(1);
            eraser.setColor(0);
            eraser.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        }
        this.checkPaint = new Paint(1);
        this.checkPaint.setStrokeCap(Cap.ROUND);
        this.checkPaint.setStyle(Style.STROKE);
        this.checkPaint.setStrokeJoin(Join.ROUND);
        this.checkPaint.setStrokeWidth((float) AndroidUtilities.dp(1.9f));
        this.backgroundPaint = new Paint(1);
        this.backgroundPaint.setStyle(Style.STROKE);
        this.backgroundPaint.setStrokeWidth((float) AndroidUtilities.dp(1.2f));
        this.drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(this.size), AndroidUtilities.dp(this.size), Config.ARGB_4444);
        this.bitmapCanvas = new Canvas(this.drawBitmap);
    }

    public void onAttachedToWindow() {
        this.attachedToWindow = true;
    }

    public void onDetachedFromWindow() {
        this.attachedToWindow = false;
    }

    public void setBounds(int i, int i2, int i3, int i4) {
        Rect rect = this.bounds;
        rect.left = i;
        rect.top = i2;
        rect.right = i + i3;
        rect.bottom = i2 + i4;
    }

    public void setDrawUnchecked(boolean z) {
        this.drawUnchecked = z;
    }

    @Keep
    public void setProgress(float f) {
        if (this.progress != f) {
            this.progress = f;
            invalidate();
            ProgressDelegate progressDelegate = this.progressDelegate;
            if (progressDelegate != null) {
                progressDelegate.setProgress(f);
            }
        }
    }

    private void invalidate() {
        if (this.parentView.getParent() != null) {
            ((View) this.parentView.getParent()).invalidate();
        }
        this.parentView.invalidate();
    }

    public void setProgressDelegate(ProgressDelegate progressDelegate) {
        this.progressDelegate = progressDelegate;
    }

    public float getProgress() {
        return this.progress;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    public void setDrawBackgroundAsArc(int i) {
        this.drawBackgroundAsArc = i;
        if (i == 4 || i == 5) {
            this.backgroundPaint.setStrokeWidth((float) AndroidUtilities.dp(1.9f));
            if (i == 5) {
                this.checkPaint.setStrokeWidth((float) AndroidUtilities.dp(1.5f));
            }
        } else if (i == 3) {
            this.backgroundPaint.setStrokeWidth((float) AndroidUtilities.dp(1.2f));
        } else if (i != 0) {
            this.backgroundPaint.setStrokeWidth((float) AndroidUtilities.dp(1.5f));
        }
    }

    private void cancelCheckAnimator() {
        ObjectAnimator objectAnimator = this.checkAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
            this.checkAnimator = null;
        }
    }

    private void animateToCheckedState(boolean z) {
        float[] fArr = new float[1];
        fArr[0] = z ? 1.0f : 0.0f;
        this.checkAnimator = ObjectAnimator.ofFloat(this, "progress", fArr);
        this.checkAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (animator.equals(CheckBoxBase.this.checkAnimator)) {
                    CheckBoxBase.this.checkAnimator = null;
                }
                if (!CheckBoxBase.this.isChecked) {
                    CheckBoxBase.this.checkedText = null;
                }
            }
        });
        this.checkAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        this.checkAnimator.setDuration(200);
        this.checkAnimator.start();
    }

    public void setColor(String str, String str2, String str3) {
        this.backgroundColorKey = str;
        this.background2ColorKey = str2;
        this.checkColorKey = str3;
    }

    public void setUseDefaultCheck(boolean z) {
        this.useDefaultCheck = z;
    }

    public void setBackgroundAlpha(float f) {
        this.backgroundAlpha = f;
    }

    public void setNum(int i) {
        if (i >= 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(i + 1);
            this.checkedText = stringBuilder.toString();
        } else if (this.checkAnimator == null) {
            this.checkedText = null;
        }
        invalidate();
    }

    public void setChecked(boolean z, boolean z2) {
        setChecked(-1, z, z2);
    }

    public void setChecked(int i, boolean z, boolean z2) {
        if (i >= 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(i + 1);
            this.checkedText = stringBuilder.toString();
            invalidate();
        }
        if (z != this.isChecked) {
            this.isChecked = z;
            if (this.attachedToWindow && z2) {
                animateToCheckedState(z);
            } else {
                cancelCheckAnimator();
                setProgress(z ? 1.0f : 0.0f);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x025e  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01b3  */
    public void draw(android.graphics.Canvas r19) {
        /*
        r18 = this;
        r0 = r18;
        r7 = r19;
        r1 = r0.drawBitmap;
        if (r1 != 0) goto L_0x0009;
    L_0x0008:
        return;
    L_0x0009:
        r2 = 0;
        r1.eraseColor(r2);
        r1 = r0.size;
        r8 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r1 = r1 / r8;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r9 = (float) r1;
        r1 = r0.drawBackgroundAsArc;
        if (r1 == 0) goto L_0x0026;
    L_0x001b:
        r1 = NUM; // 0x3e4ccccd float:0.2 double:5.164075695E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r1 = r9 - r1;
        goto L_0x0027;
    L_0x0026:
        r1 = r9;
    L_0x0027:
        r3 = r0.progress;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        r4 = (r3 > r11 ? 1 : (r3 == r11 ? 0 : -1));
        if (r4 < 0) goto L_0x0034;
    L_0x0031:
        r12 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x0036;
    L_0x0034:
        r3 = r3 / r11;
        r12 = r3;
    L_0x0036:
        r3 = r0.bounds;
        r13 = r3.centerX();
        r3 = r0.bounds;
        r14 = r3.centerY();
        r3 = r0.backgroundColorKey;
        r4 = 16777215; // 0xffffff float:2.3509886E-38 double:8.2890456E-317;
        if (r3 == 0) goto L_0x007f;
    L_0x0049:
        r3 = r0.drawUnchecked;
        if (r3 == 0) goto L_0x0066;
    L_0x004d:
        r3 = paint;
        r5 = org.telegram.ui.ActionBar.Theme.getServiceMessageColor();
        r4 = r4 & r5;
        r5 = NUM; // 0x28000000 float:7.1054274E-15 double:3.315618423E-315;
        r4 = r4 | r5;
        r3.setColor(r4);
        r3 = r0.backgroundPaint;
        r4 = r0.checkColorKey;
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
        goto L_0x00c0;
    L_0x0066:
        r3 = r0.backgroundPaint;
        r5 = r0.background2ColorKey;
        if (r5 == 0) goto L_0x006d;
    L_0x006c:
        goto L_0x006f;
    L_0x006d:
        r5 = r0.checkColorKey;
    L_0x006f:
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r6 = r0.progress;
        r15 = r0.backgroundAlpha;
        r4 = org.telegram.messenger.AndroidUtilities.getOffsetColor(r4, r5, r6, r15);
        r3.setColor(r4);
        goto L_0x00c0;
    L_0x007f:
        r3 = r0.drawUnchecked;
        if (r3 == 0) goto L_0x00a8;
    L_0x0083:
        r3 = paint;
        r4 = NUM; // 0x41CLASSNAME float:25.0 double:5.45263811E-315;
        r5 = r0.backgroundAlpha;
        r5 = r5 * r4;
        r4 = (int) r5;
        r4 = android.graphics.Color.argb(r4, r2, r2, r2);
        r3.setColor(r4);
        r3 = r0.backgroundPaint;
        r4 = -1;
        r5 = r0.checkColorKey;
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r6 = r0.progress;
        r15 = r0.backgroundAlpha;
        r4 = org.telegram.messenger.AndroidUtilities.getOffsetColor(r4, r5, r6, r15);
        r3.setColor(r4);
        goto L_0x00c0;
    L_0x00a8:
        r3 = r0.backgroundPaint;
        r5 = r0.background2ColorKey;
        if (r5 == 0) goto L_0x00af;
    L_0x00ae:
        goto L_0x00b1;
    L_0x00af:
        r5 = r0.checkColorKey;
    L_0x00b1:
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r6 = r0.progress;
        r15 = r0.backgroundAlpha;
        r4 = org.telegram.messenger.AndroidUtilities.getOffsetColor(r4, r5, r6, r15);
        r3.setColor(r4);
    L_0x00c0:
        r3 = r0.drawUnchecked;
        r15 = NUM; // 0x3fCLASSNAME float:1.5 double:5.28426686E-315;
        r6 = 7;
        r5 = 6;
        if (r3 == 0) goto L_0x00f1;
    L_0x00c8:
        r3 = r0.drawBackgroundAsArc;
        if (r3 == r5) goto L_0x00d7;
    L_0x00cc:
        if (r3 != r6) goto L_0x00cf;
    L_0x00ce:
        goto L_0x00d7;
    L_0x00cf:
        r3 = (float) r13;
        r4 = (float) r14;
        r2 = paint;
        r7.drawCircle(r3, r4, r9, r2);
        goto L_0x00f1;
    L_0x00d7:
        r2 = (float) r13;
        r3 = (float) r14;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r4 = (float) r4;
        r4 = r9 - r4;
        r8 = paint;
        r7.drawCircle(r2, r3, r4, r8);
        r4 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r4 = (float) r4;
        r4 = r9 - r4;
        r8 = r0.backgroundPaint;
        r7.drawCircle(r2, r3, r4, r8);
    L_0x00f1:
        r2 = paint;
        r3 = r0.checkColorKey;
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r0.drawBackgroundAsArc;
        r8 = 1;
        if (r2 == r6) goto L_0x01ac;
    L_0x0101:
        if (r2 != 0) goto L_0x010c;
    L_0x0103:
        r1 = (float) r13;
        r2 = (float) r14;
        r3 = r0.backgroundPaint;
        r7.drawCircle(r1, r2, r9, r3);
        goto L_0x01ac;
    L_0x010c:
        r2 = r0.rect;
        r3 = (float) r13;
        r4 = r3 - r1;
        r6 = (float) r14;
        r15 = r6 - r1;
        r3 = r3 + r1;
        r6 = r6 + r1;
        r2.set(r4, r15, r3, r6);
        r1 = r0.drawBackgroundAsArc;
        if (r1 != r5) goto L_0x0126;
    L_0x011d:
        r1 = -NUM; // 0xffffffffc3b40000 float:-360.0 double:NaN;
        r2 = r0.progress;
        r2 = r2 * r1;
        r1 = (int) r2;
        r2 = 0;
        goto L_0x0138;
    L_0x0126:
        if (r1 != r8) goto L_0x012f;
    L_0x0128:
        r2 = -90;
        r1 = -NUM; // 0xffffffffCLASSNAME float:-270.0 double:NaN;
        r3 = r0.progress;
        goto L_0x0135;
    L_0x012f:
        r2 = 90;
        r1 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r3 = r0.progress;
    L_0x0135:
        r3 = r3 * r1;
        r1 = (int) r3;
    L_0x0138:
        r3 = r0.drawBackgroundAsArc;
        if (r3 != r5) goto L_0x0198;
    L_0x013c:
        r3 = "dialogBackground";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r4 = android.graphics.Color.alpha(r3);
        r6 = r0.backgroundPaint;
        r6.setColor(r3);
        r3 = r0.backgroundPaint;
        r4 = (float) r4;
        r6 = r0.progress;
        r4 = r4 * r6;
        r4 = (int) r4;
        r3.setAlpha(r4);
        r3 = r0.rect;
        r15 = (float) r2;
        r6 = (float) r1;
        r16 = 0;
        r4 = r0.backgroundPaint;
        r1 = r19;
        r2 = r3;
        r3 = r15;
        r17 = r4;
        r4 = r6;
        r8 = 6;
        r5 = r16;
        r16 = r6;
        r10 = 7;
        r6 = r17;
        r1.drawArc(r2, r3, r4, r5, r6);
        r1 = "chat_attachPhotoBackground";
        r1 = org.telegram.ui.ActionBar.Theme.getColor(r1);
        r2 = android.graphics.Color.alpha(r1);
        r3 = r0.backgroundPaint;
        r3.setColor(r1);
        r1 = r0.backgroundPaint;
        r2 = (float) r2;
        r3 = r0.progress;
        r2 = r2 * r3;
        r2 = (int) r2;
        r1.setAlpha(r2);
        r2 = r0.rect;
        r5 = 0;
        r6 = r0.backgroundPaint;
        r1 = r19;
        r3 = r15;
        r4 = r16;
        r1.drawArc(r2, r3, r4, r5, r6);
        goto L_0x01ae;
    L_0x0198:
        r8 = 6;
        r10 = 7;
        r3 = r0.rect;
        r4 = (float) r2;
        r5 = (float) r1;
        r6 = 0;
        r15 = r0.backgroundPaint;
        r1 = r19;
        r2 = r3;
        r3 = r4;
        r4 = r5;
        r5 = r6;
        r6 = r15;
        r1.drawArc(r2, r3, r4, r5, r6);
        goto L_0x01ae;
    L_0x01ac:
        r8 = 6;
        r10 = 7;
    L_0x01ae:
        r1 = 0;
        r2 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1));
        if (r2 <= 0) goto L_0x0338;
    L_0x01b3:
        r2 = r0.progress;
        r3 = (r2 > r11 ? 1 : (r2 == r11 ? 0 : -1));
        if (r3 >= 0) goto L_0x01bb;
    L_0x01b9:
        r2 = 0;
        goto L_0x01bd;
    L_0x01bb:
        r2 = r2 - r11;
        r2 = r2 / r11;
    L_0x01bd:
        r3 = r0.drawBackgroundAsArc;
        if (r3 == r8) goto L_0x01df;
    L_0x01c1:
        if (r3 == r10) goto L_0x01df;
    L_0x01c3:
        r3 = r0.drawUnchecked;
        if (r3 != 0) goto L_0x01cc;
    L_0x01c7:
        r3 = r0.backgroundColorKey;
        if (r3 == 0) goto L_0x01cc;
    L_0x01cb:
        goto L_0x01df;
    L_0x01cc:
        r3 = paint;
        r4 = r0.enabled;
        if (r4 == 0) goto L_0x01d5;
    L_0x01d2:
        r4 = "checkbox";
        goto L_0x01d7;
    L_0x01d5:
        r4 = "checkboxDisabled";
    L_0x01d7:
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
        goto L_0x01ea;
    L_0x01df:
        r3 = paint;
        r4 = r0.backgroundColorKey;
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
    L_0x01ea:
        r3 = r0.useDefaultCheck;
        if (r3 != 0) goto L_0x01fc;
    L_0x01ee:
        r3 = r0.checkColorKey;
        if (r3 == 0) goto L_0x01fc;
    L_0x01f2:
        r4 = r0.checkPaint;
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r4.setColor(r3);
        goto L_0x0207;
    L_0x01fc:
        r3 = r0.checkPaint;
        r4 = "checkboxCheck";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
    L_0x0207:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r3 = (float) r3;
        r9 = r9 - r3;
        r3 = r0.bitmapCanvas;
        r4 = r0.drawBitmap;
        r4 = r4.getWidth();
        r5 = 2;
        r4 = r4 / r5;
        r4 = (float) r4;
        r6 = r0.drawBitmap;
        r6 = r6.getHeight();
        r6 = r6 / r5;
        r6 = (float) r6;
        r8 = paint;
        r3.drawCircle(r4, r6, r9, r8);
        r3 = r0.bitmapCanvas;
        r4 = r0.drawBitmap;
        r4 = r4.getWidth();
        r4 = r4 / r5;
        r4 = (float) r4;
        r6 = r0.drawBitmap;
        r6 = r6.getWidth();
        r6 = r6 / r5;
        r6 = (float) r6;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = r8 - r12;
        r9 = r9 * r10;
        r8 = eraser;
        r3.drawCircle(r4, r6, r9, r8);
        r3 = r0.drawBitmap;
        r4 = r3.getWidth();
        r4 = r4 / r5;
        r4 = r13 - r4;
        r4 = (float) r4;
        r6 = r0.drawBitmap;
        r6 = r6.getHeight();
        r6 = r6 / r5;
        r6 = r14 - r6;
        r6 = (float) r6;
        r8 = 0;
        r7.drawBitmap(r3, r4, r6, r8);
        r1 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1));
        if (r1 == 0) goto L_0x0338;
    L_0x025e:
        r1 = r0.checkedText;
        if (r1 == 0) goto L_0x02d0;
    L_0x0262:
        r1 = r0.textPaint;
        if (r1 != 0) goto L_0x0279;
    L_0x0266:
        r1 = new android.text.TextPaint;
        r3 = 1;
        r1.<init>(r3);
        r0.textPaint = r1;
        r1 = r0.textPaint;
        r3 = "fonts/rmedium.ttf";
        r3 = org.telegram.messenger.AndroidUtilities.getTypeface(r3);
        r1.setTypeface(r3);
    L_0x0279:
        r1 = r0.checkedText;
        r1 = r1.length();
        if (r1 == 0) goto L_0x0293;
    L_0x0281:
        r3 = 1;
        if (r1 == r3) goto L_0x0293;
    L_0x0284:
        if (r1 == r5) goto L_0x0293;
    L_0x0286:
        r3 = 3;
        if (r1 == r3) goto L_0x028e;
    L_0x0289:
        r1 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r3 = NUM; // 0x417CLASSNAME float:15.75 double:5.428030005E-315;
        goto L_0x0297;
    L_0x028e:
        r1 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r3 = NUM; // 0x41840000 float:16.5 double:5.43062033E-315;
        goto L_0x0297;
    L_0x0293:
        r1 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r3 = NUM; // 0x41900000 float:18.0 double:5.43450582E-315;
    L_0x0297:
        r4 = r0.textPaint;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r4.setTextSize(r1);
        r1 = r0.textPaint;
        r4 = r0.checkColorKey;
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r1.setColor(r4);
        r19.save();
        r1 = (float) r13;
        r4 = (float) r14;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r7.scale(r2, r8, r1, r4);
        r2 = r0.checkedText;
        r4 = r0.textPaint;
        r4 = r4.measureText(r2);
        r5 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = r4 / r5;
        r1 = r1 - r4;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = (float) r3;
        r4 = r0.textPaint;
        r7.drawText(r2, r1, r3, r4);
        r19.restore();
        goto L_0x0338;
    L_0x02d0:
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1 = r0.path;
        r1.reset();
        r1 = r0.drawBackgroundAsArc;
        r3 = 5;
        if (r1 != r3) goto L_0x02e2;
    L_0x02dc:
        r10 = NUM; // 0x3f4ccccd float:0.8 double:5.246966156E-315;
        r8 = NUM; // 0x3f4ccccd float:0.8 double:5.246966156E-315;
    L_0x02e2:
        r1 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r1 = r1 * r8;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r1 = r1 * r2;
        r3 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r8 = r8 * r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r4 = (float) r4;
        r4 = r4 * r2;
        r2 = NUM; // 0x3fCLASSNAME float:1.5 double:5.28426686E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r13 = r13 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r14 = r14 + r2;
        r4 = r4 * r4;
        r2 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = r4 / r2;
        r2 = (double) r4;
        r2 = java.lang.Math.sqrt(r2);
        r2 = (float) r2;
        r3 = r0.path;
        r4 = (float) r13;
        r5 = r4 - r2;
        r6 = (float) r14;
        r2 = r6 - r2;
        r3.moveTo(r5, r2);
        r2 = r0.path;
        r2.lineTo(r4, r6);
        r1 = r1 * r1;
        r2 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r1 = r1 / r2;
        r1 = (double) r1;
        r1 = java.lang.Math.sqrt(r1);
        r1 = (float) r1;
        r2 = r0.path;
        r4 = r4 + r1;
        r6 = r6 - r1;
        r2.lineTo(r4, r6);
        r1 = r0.path;
        r2 = r0.checkPaint;
        r7.drawPath(r1, r2);
    L_0x0338:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.CheckBoxBase.draw(android.graphics.Canvas):void");
    }
}
