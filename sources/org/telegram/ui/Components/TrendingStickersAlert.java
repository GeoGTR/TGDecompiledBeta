package org.telegram.ui.Components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TrendingStickersAlert;

public class TrendingStickersAlert extends BottomSheet {
    private final AlertContainerView alertContainerView;
    /* access modifiers changed from: private */
    public final TrendingStickersLayout layout;
    /* access modifiers changed from: private */
    public int scrollOffsetY;
    /* access modifiers changed from: private */
    public final GradientDrawable shapeDrawable = new GradientDrawable();
    /* access modifiers changed from: private */
    public final int topOffset = AndroidUtilities.dp(12.0f);

    /* access modifiers changed from: protected */
    public boolean canDismissWithSwipe() {
        return false;
    }

    public TrendingStickersAlert(Context context, BaseFragment baseFragment, TrendingStickersLayout trendingStickersLayout) {
        super(context, true);
        AlertContainerView alertContainerView2 = new AlertContainerView(context);
        this.alertContainerView = alertContainerView2;
        alertContainerView2.addView(trendingStickersLayout, LayoutHelper.createFrame(-1, -1.0f));
        this.containerView = alertContainerView2;
        this.layout = trendingStickersLayout;
        trendingStickersLayout.setParentFragment(baseFragment);
        trendingStickersLayout.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int scrolledY;

            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 0) {
                    this.scrolledY = 0;
                }
            }

            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                this.scrolledY += i2;
                if (recyclerView.getScrollState() == 1 && Math.abs(this.scrolledY) > AndroidUtilities.dp(96.0f)) {
                    View findFocus = TrendingStickersAlert.this.layout.findFocus();
                    if (findFocus == null) {
                        findFocus = TrendingStickersAlert.this.layout;
                    }
                    AndroidUtilities.hideKeyboard(findFocus);
                }
                if (i2 != 0) {
                    TrendingStickersAlert.this.updateLayout();
                }
            }
        });
    }

    public void show() {
        super.show();
        setHeavyOperationsEnabled(false);
    }

    public void dismiss() {
        super.dismiss();
        this.layout.recycle();
        setHeavyOperationsEnabled(true);
    }

    public void setHeavyOperationsEnabled(boolean z) {
        NotificationCenter.getGlobalInstance().postNotificationName(z ? NotificationCenter.startAllHeavyOperations : NotificationCenter.stopAllHeavyOperations, 2);
    }

    public TrendingStickersLayout getLayout() {
        return this.layout;
    }

    /* access modifiers changed from: private */
    public void updateLayout() {
        if (this.layout.update()) {
            this.scrollOffsetY = this.layout.getContentTopOffset();
            this.containerView.invalidate();
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        TrendingStickersLayout trendingStickersLayout = this.layout;
        trendingStickersLayout.getClass();
        trendingStickersLayout.getThemeDescriptions(arrayList, new ThemeDescription.ThemeDescriptionDelegate() {
            public final void didSetColor() {
                TrendingStickersLayout.this.updateColors();
            }
        });
        arrayList.add(new ThemeDescription(this.alertContainerView, 0, (Class[]) null, (Paint) null, new Drawable[]{this.shadowDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBackground"));
        arrayList.add(new ThemeDescription(this.alertContainerView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_sheet_scrollUp"));
        return arrayList;
    }

    public void setAllowNestedScroll(boolean z) {
        this.allowNestedScroll = z;
    }

    private class AlertContainerView extends SizeNotifierFrameLayout {
        /* access modifiers changed from: private */
        public boolean gluedToTop = false;
        private boolean ignoreLayout = false;
        private final Paint paint = new Paint(1);
        private float[] radii = new float[8];
        private float statusBarAlpha = 0.0f;
        private ValueAnimator statusBarAnimator;
        private boolean statusBarVisible = false;

        public AlertContainerView(Context context) {
            super(context);
            setWillNotDraw(false);
            setPadding(TrendingStickersAlert.this.backgroundPaddingLeft, 0, TrendingStickersAlert.this.backgroundPaddingLeft, 0);
            setDelegate(new SizeNotifierFrameLayout.SizeNotifierFrameLayoutDelegate(TrendingStickersAlert.this) {
                private boolean lastIsWidthGreater;
                private int lastKeyboardHeight;

                public void onSizeChanged(int i, boolean z) {
                    if (this.lastKeyboardHeight != i || this.lastIsWidthGreater != z) {
                        this.lastKeyboardHeight = i;
                        this.lastIsWidthGreater = z;
                        if (i > AndroidUtilities.dp(20.0f) && !AlertContainerView.this.gluedToTop) {
                            TrendingStickersAlert.this.setAllowNestedScroll(false);
                            boolean unused = AlertContainerView.this.gluedToTop = true;
                        }
                    }
                }
            });
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            AndroidUtilities.runOnUIThread(new Runnable() {
                public final void run() {
                    TrendingStickersAlert.AlertContainerView.this.requestLayout();
                }
            }, 200);
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getAction() != 0 || TrendingStickersAlert.this.scrollOffsetY == 0 || motionEvent.getY() >= ((float) TrendingStickersAlert.this.scrollOffsetY)) {
                return super.onInterceptTouchEvent(motionEvent);
            }
            TrendingStickersAlert.this.dismiss();
            return true;
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            return !TrendingStickersAlert.this.isDismissed() && super.onTouchEvent(motionEvent);
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), NUM));
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            int i5 = Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
            int measureKeyboardHeight = measureKeyboardHeight();
            int size = (int) (((float) ((View.MeasureSpec.getSize(getMeasuredHeight()) - i5) + measureKeyboardHeight)) * 0.2f);
            this.ignoreLayout = true;
            if (measureKeyboardHeight > AndroidUtilities.dp(20.0f)) {
                TrendingStickersAlert.this.layout.glueToTop(true);
                TrendingStickersAlert.this.setAllowNestedScroll(false);
                this.gluedToTop = true;
            } else {
                TrendingStickersAlert.this.layout.glueToTop(false);
                TrendingStickersAlert.this.setAllowNestedScroll(true);
                this.gluedToTop = false;
            }
            TrendingStickersAlert.this.layout.setContentViewPaddingTop(size);
            if (getPaddingTop() != i5) {
                setPadding(TrendingStickersAlert.this.backgroundPaddingLeft, i5, TrendingStickersAlert.this.backgroundPaddingLeft, 0);
            }
            this.ignoreLayout = false;
            super.onLayout(z, i, i2, i3, i4);
        }

        public void requestLayout() {
            if (!this.ignoreLayout) {
                super.requestLayout();
            }
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            TrendingStickersAlert.this.updateLayout();
            super.onDraw(canvas);
            float fraction = getFraction();
            int access$800 = (int) (((float) TrendingStickersAlert.this.topOffset) * (1.0f - fraction));
            int access$8002 = (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0) - TrendingStickersAlert.this.topOffset;
            canvas.save();
            canvas.translate(0.0f, TrendingStickersAlert.this.layout.getTranslationY() + ((float) access$8002));
            TrendingStickersAlert.this.shadowDrawable.setBounds(0, (TrendingStickersAlert.this.scrollOffsetY - TrendingStickersAlert.this.backgroundPaddingTop) + access$800, getMeasuredWidth(), getMeasuredHeight() + (access$8002 < 0 ? -access$8002 : 0));
            TrendingStickersAlert.this.shadowDrawable.draw(canvas);
            if (fraction > 0.0f && fraction < 1.0f) {
                float dp = ((float) AndroidUtilities.dp(12.0f)) * fraction;
                TrendingStickersAlert.this.shapeDrawable.setColor(Theme.getColor("dialogBackground"));
                float[] fArr = this.radii;
                fArr[3] = dp;
                fArr[2] = dp;
                fArr[1] = dp;
                fArr[0] = dp;
                TrendingStickersAlert.this.shapeDrawable.setCornerRadii(this.radii);
                TrendingStickersAlert.this.shapeDrawable.setBounds(TrendingStickersAlert.this.backgroundPaddingLeft, TrendingStickersAlert.this.scrollOffsetY + access$800, getWidth() - TrendingStickersAlert.this.backgroundPaddingLeft, TrendingStickersAlert.this.scrollOffsetY + access$800 + AndroidUtilities.dp(24.0f));
                TrendingStickersAlert.this.shapeDrawable.draw(canvas);
            }
            canvas.restore();
        }

        /* access modifiers changed from: protected */
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            float fraction = getFraction();
            canvas.save();
            float translationY = TrendingStickersAlert.this.layout.getTranslationY();
            int i = Build.VERSION.SDK_INT;
            boolean z = false;
            canvas.translate(0.0f, (translationY + ((float) (i >= 21 ? AndroidUtilities.statusBarHeight : 0))) - ((float) TrendingStickersAlert.this.topOffset));
            int dp = AndroidUtilities.dp(36.0f);
            int dp2 = AndroidUtilities.dp(4.0f);
            int i2 = (int) (((float) dp2) * 2.0f * (1.0f - fraction));
            TrendingStickersAlert.this.shapeDrawable.setCornerRadius((float) AndroidUtilities.dp(2.0f));
            int color = Theme.getColor("key_sheet_scrollUp");
            TrendingStickersAlert.this.shapeDrawable.setColor(ColorUtils.setAlphaComponent(color, (int) (((float) Color.alpha(color)) * fraction)));
            TrendingStickersAlert.this.shapeDrawable.setBounds((getWidth() - dp) / 2, TrendingStickersAlert.this.scrollOffsetY + AndroidUtilities.dp(10.0f) + i2, (getWidth() + dp) / 2, TrendingStickersAlert.this.scrollOffsetY + AndroidUtilities.dp(10.0f) + i2 + dp2);
            TrendingStickersAlert.this.shapeDrawable.draw(canvas);
            canvas.restore();
            if (fraction == 0.0f && i >= 21 && !TrendingStickersAlert.this.isDismissed()) {
                z = true;
            }
            setStatusBarVisible(z, true);
            if (this.statusBarAlpha > 0.0f) {
                int color2 = Theme.getColor("dialogBackground");
                this.paint.setColor(Color.argb((int) (this.statusBarAlpha * 255.0f), (int) (((float) Color.red(color2)) * 0.8f), (int) (((float) Color.green(color2)) * 0.8f), (int) (((float) Color.blue(color2)) * 0.8f)));
                canvas.drawRect((float) TrendingStickersAlert.this.backgroundPaddingLeft, 0.0f, (float) (getMeasuredWidth() - TrendingStickersAlert.this.backgroundPaddingLeft), (float) AndroidUtilities.statusBarHeight, this.paint);
            }
        }

        public void setTranslationY(float f) {
            TrendingStickersAlert.this.layout.setTranslationY(f);
            invalidate();
        }

        public float getTranslationY() {
            return TrendingStickersAlert.this.layout.getTranslationY();
        }

        private float getFraction() {
            return Math.min(1.0f, Math.max(0.0f, ((float) TrendingStickersAlert.this.scrollOffsetY) / (((float) TrendingStickersAlert.this.topOffset) * 2.0f)));
        }

        private void setStatusBarVisible(boolean z, boolean z2) {
            if (this.statusBarVisible != z) {
                ValueAnimator valueAnimator = this.statusBarAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                this.statusBarVisible = z;
                float f = 1.0f;
                if (z2) {
                    ValueAnimator valueAnimator2 = this.statusBarAnimator;
                    if (valueAnimator2 == null) {
                        float[] fArr = new float[2];
                        fArr[0] = this.statusBarAlpha;
                        if (!z) {
                            f = 0.0f;
                        }
                        fArr[1] = f;
                        ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
                        this.statusBarAnimator = ofFloat;
                        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                                TrendingStickersAlert.AlertContainerView.this.lambda$setStatusBarVisible$0$TrendingStickersAlert$AlertContainerView(valueAnimator);
                            }
                        });
                        this.statusBarAnimator.setDuration(200);
                    } else {
                        float[] fArr2 = new float[2];
                        fArr2[0] = this.statusBarAlpha;
                        if (!z) {
                            f = 0.0f;
                        }
                        fArr2[1] = f;
                        valueAnimator2.setFloatValues(fArr2);
                    }
                    this.statusBarAnimator.start();
                    return;
                }
                if (!z) {
                    f = 0.0f;
                }
                this.statusBarAlpha = f;
                invalidate();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setStatusBarVisible$0 */
        public /* synthetic */ void lambda$setStatusBarVisible$0$TrendingStickersAlert$AlertContainerView(ValueAnimator valueAnimator) {
            this.statusBarAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }
    }
}
