package org.telegram.ui.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LineProgressView;
import org.telegram.ui.Components.RadialProgressView;

public class AlertDialog extends Dialog implements Callback {
    private Rect backgroundPaddings = new Rect();
    private FrameLayout buttonsLayout;
    private ScrollView contentScrollView;
    private int currentProgress;
    private View customView;
    private int[] itemIcons;
    private CharSequence[] items;
    private int lastScreenWidth;
    private LineProgressView lineProgressView;
    private TextView lineProgressViewPercent;
    private CharSequence message;
    private TextView messageTextView;
    private OnClickListener negativeButtonListener;
    private CharSequence negativeButtonText;
    private OnClickListener neutralButtonListener;
    private CharSequence neutralButtonText;
    private OnClickListener onClickListener;
    private OnDismissListener onDismissListener;
    private OnScrollChangedListener onScrollChangedListener;
    private OnClickListener positiveButtonListener;
    private CharSequence positiveButtonText;
    private FrameLayout progressViewContainer;
    private int progressViewStyle;
    private TextView progressViewTextView;
    private LinearLayout scrollContainer;
    private BitmapDrawable[] shadow = new BitmapDrawable[2];
    private AnimatorSet[] shadowAnimation = new AnimatorSet[2];
    private Drawable shadowDrawable;
    private boolean[] shadowVisibility = new boolean[2];
    private CharSequence title;
    private TextView titleTextView;

    private class AlertDialogCell extends FrameLayout {
        private ImageView imageView;
        private TextView textView;
        final /* synthetic */ AlertDialog this$0;

        public AlertDialogCell(AlertDialog alertDialog, Context context) {
            int i = 3;
            this.this$0 = alertDialog;
            super(context);
            setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 2));
            setPadding(AndroidUtilities.dp(23.0f), 0, AndroidUtilities.dp(23.0f), 0);
            this.imageView = new ImageView(context);
            this.imageView.setScaleType(ScaleType.CENTER);
            this.imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon), Mode.MULTIPLY));
            addView(this.imageView, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? 5 : 3) | 16));
            this.textView = new TextView(context);
            this.textView.setLines(1);
            this.textView.setSingleLine(true);
            this.textView.setGravity(1);
            this.textView.setEllipsize(TruncateAt.END);
            this.textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            this.textView.setTextSize(1, 16.0f);
            View view = this.textView;
            if (LocaleController.isRTL) {
                i = 5;
            }
            addView(view, LayoutHelper.createFrame(-2, -2, i | 16));
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0f), NUM));
        }

        public void setTextColor(int color) {
            this.textView.setTextColor(color);
        }

        public void setGravity(int gravity) {
            this.textView.setGravity(gravity);
        }

        public void setTextAndIcon(CharSequence text, int icon) {
            this.textView.setText(text);
            if (icon != 0) {
                this.imageView.setImageResource(icon);
                this.imageView.setVisibility(0);
                this.textView.setPadding(LocaleController.isRTL ? 0 : AndroidUtilities.dp(56.0f), 0, LocaleController.isRTL ? AndroidUtilities.dp(56.0f) : 0, 0);
                return;
            }
            this.imageView.setVisibility(4);
            this.textView.setPadding(0, 0, 0, 0);
        }
    }

    public static class Builder {
        private AlertDialog alertDialog;

        public Builder(Context context) {
            this.alertDialog = new AlertDialog(context, 0);
        }

        public Builder(Context context, int progressViewStyle) {
            this.alertDialog = new AlertDialog(context, progressViewStyle);
        }

        public Context getContext() {
            return this.alertDialog.getContext();
        }

        public Builder setItems(CharSequence[] items, OnClickListener onClickListener) {
            this.alertDialog.items = items;
            this.alertDialog.onClickListener = onClickListener;
            return this;
        }

        public Builder setItems(CharSequence[] items, int[] icons, OnClickListener onClickListener) {
            this.alertDialog.items = items;
            this.alertDialog.itemIcons = icons;
            this.alertDialog.onClickListener = onClickListener;
            return this;
        }

        public Builder setView(View view) {
            this.alertDialog.customView = view;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.alertDialog.title = title;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.alertDialog.message = message;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            this.alertDialog.positiveButtonText = text;
            this.alertDialog.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            this.alertDialog.negativeButtonText = text;
            this.alertDialog.negativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
            this.alertDialog.neutralButtonText = text;
            this.alertDialog.neutralButtonListener = listener;
            return this;
        }

        public AlertDialog create() {
            return this.alertDialog;
        }

        public AlertDialog show() {
            this.alertDialog.show();
            return this.alertDialog;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            this.alertDialog.setOnDismissListener(onDismissListener);
            return this;
        }
    }

    public AlertDialog(Context context, int progressStyle) {
        super(context, R.style.TransparentDialog);
        this.shadowDrawable = context.getResources().getDrawable(R.drawable.popup_fixed).mutate();
        this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), Mode.MULTIPLY));
        this.shadowDrawable.getPadding(this.backgroundPaddings);
        this.progressViewStyle = progressStyle;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onCreate(Bundle savedInstanceState) {
        int i;
        int maxWidth;
        super.onCreate(savedInstanceState);
        LinearLayout containerView = new LinearLayout(getContext()) {
            private boolean inLayout;

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                this.inLayout = true;
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int maxContentHeight = (MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()) - getPaddingBottom();
                int availableHeight = maxContentHeight;
                int availableWidth = (width - getPaddingLeft()) - getPaddingRight();
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth - AndroidUtilities.dp(48.0f), NUM);
                int childFullWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth, NUM);
                if (AlertDialog.this.buttonsLayout != null) {
                    int count = AlertDialog.this.buttonsLayout.getChildCount();
                    for (int a = 0; a < count; a++) {
                        ((TextView) AlertDialog.this.buttonsLayout.getChildAt(a)).setMaxWidth(AndroidUtilities.dp((float) ((availableWidth - AndroidUtilities.dp(24.0f)) / 2)));
                    }
                    AlertDialog.this.buttonsLayout.measure(childFullWidthMeasureSpec, heightMeasureSpec);
                    LayoutParams layoutParams = (LayoutParams) AlertDialog.this.buttonsLayout.getLayoutParams();
                    availableHeight -= (AlertDialog.this.buttonsLayout.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                }
                if (AlertDialog.this.titleTextView != null) {
                    AlertDialog.this.titleTextView.measure(childWidthMeasureSpec, heightMeasureSpec);
                    layoutParams = (LayoutParams) AlertDialog.this.titleTextView.getLayoutParams();
                    availableHeight -= (AlertDialog.this.titleTextView.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                }
                if (AlertDialog.this.progressViewStyle == 0) {
                    layoutParams = (LayoutParams) AlertDialog.this.contentScrollView.getLayoutParams();
                    int dp;
                    if (AlertDialog.this.customView != null) {
                        dp = (AlertDialog.this.titleTextView == null && AlertDialog.this.messageTextView.getVisibility() == 8 && AlertDialog.this.items == null) ? AndroidUtilities.dp(16.0f) : 0;
                        layoutParams.topMargin = dp;
                        layoutParams.bottomMargin = AlertDialog.this.buttonsLayout == null ? AndroidUtilities.dp(8.0f) : 0;
                    } else if (AlertDialog.this.items != null) {
                        dp = (AlertDialog.this.titleTextView == null && AlertDialog.this.messageTextView.getVisibility() == 8) ? AndroidUtilities.dp(8.0f) : 0;
                        layoutParams.topMargin = dp;
                        layoutParams.bottomMargin = AndroidUtilities.dp(8.0f);
                    } else if (AlertDialog.this.messageTextView.getVisibility() == 0) {
                        layoutParams.topMargin = AlertDialog.this.titleTextView == null ? AndroidUtilities.dp(19.0f) : 0;
                        layoutParams.bottomMargin = AndroidUtilities.dp(20.0f);
                    }
                    availableHeight -= layoutParams.bottomMargin + layoutParams.topMargin;
                    AlertDialog.this.contentScrollView.measure(childFullWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, Integer.MIN_VALUE));
                    availableHeight -= AlertDialog.this.contentScrollView.getMeasuredHeight();
                } else {
                    if (AlertDialog.this.progressViewContainer != null) {
                        AlertDialog.this.progressViewContainer.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, Integer.MIN_VALUE));
                        layoutParams = (LayoutParams) AlertDialog.this.progressViewContainer.getLayoutParams();
                        availableHeight -= (AlertDialog.this.progressViewContainer.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                    } else if (AlertDialog.this.messageTextView != null) {
                        AlertDialog.this.messageTextView.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, Integer.MIN_VALUE));
                        if (AlertDialog.this.messageTextView.getVisibility() != 8) {
                            layoutParams = (LayoutParams) AlertDialog.this.messageTextView.getLayoutParams();
                            availableHeight -= (AlertDialog.this.messageTextView.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                        }
                    }
                    if (AlertDialog.this.lineProgressView != null) {
                        AlertDialog.this.lineProgressView.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(4.0f), NUM));
                        layoutParams = (LayoutParams) AlertDialog.this.lineProgressView.getLayoutParams();
                        availableHeight -= (AlertDialog.this.lineProgressView.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                        AlertDialog.this.lineProgressViewPercent.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, Integer.MIN_VALUE));
                        layoutParams = (LayoutParams) AlertDialog.this.lineProgressViewPercent.getLayoutParams();
                        availableHeight -= (AlertDialog.this.lineProgressViewPercent.getMeasuredHeight() + layoutParams.bottomMargin) + layoutParams.topMargin;
                    }
                }
                setMeasuredDimension(width, ((maxContentHeight - availableHeight) + getPaddingTop()) + getPaddingBottom());
                this.inLayout = false;
                if (AlertDialog.this.lastScreenWidth != AndroidUtilities.displaySize.x) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            int maxWidth;
                            AlertDialog.this.lastScreenWidth = AndroidUtilities.displaySize.x;
                            int calculatedWidth = AndroidUtilities.displaySize.x - AndroidUtilities.dp(56.0f);
                            if (!AndroidUtilities.isTablet()) {
                                maxWidth = AndroidUtilities.dp(356.0f);
                            } else if (AndroidUtilities.isSmallTablet()) {
                                maxWidth = AndroidUtilities.dp(446.0f);
                            } else {
                                maxWidth = AndroidUtilities.dp(496.0f);
                            }
                            Window window = AlertDialog.this.getWindow();
                            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                            params.copyFrom(window.getAttributes());
                            params.width = (Math.min(maxWidth, calculatedWidth) + AlertDialog.this.backgroundPaddings.left) + AlertDialog.this.backgroundPaddings.right;
                            window.setAttributes(params);
                        }
                    });
                }
            }

            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if (AlertDialog.this.contentScrollView != null) {
                    if (AlertDialog.this.onScrollChangedListener == null) {
                        AlertDialog.this.onScrollChangedListener = new OnScrollChangedListener() {
                            public void onScrollChanged() {
                                boolean z;
                                boolean z2 = false;
                                AlertDialog alertDialog = AlertDialog.this;
                                if (AlertDialog.this.titleTextView == null || AlertDialog.this.contentScrollView.getScrollY() <= AlertDialog.this.scrollContainer.getTop()) {
                                    z = false;
                                } else {
                                    z = true;
                                }
                                alertDialog.runShadowAnimation(0, z);
                                AlertDialog alertDialog2 = AlertDialog.this;
                                if (AlertDialog.this.buttonsLayout != null && AlertDialog.this.contentScrollView.getScrollY() + AlertDialog.this.contentScrollView.getHeight() < AlertDialog.this.scrollContainer.getBottom()) {
                                    z2 = true;
                                }
                                alertDialog2.runShadowAnimation(1, z2);
                                AlertDialog.this.contentScrollView.invalidate();
                            }
                        };
                        AlertDialog.this.contentScrollView.getViewTreeObserver().addOnScrollChangedListener(AlertDialog.this.onScrollChangedListener);
                    }
                    AlertDialog.this.onScrollChangedListener.onScrollChanged();
                }
            }

            public void requestLayout() {
                if (!this.inLayout) {
                    super.requestLayout();
                }
            }

            public boolean hasOverlappingRendering() {
                return false;
            }
        };
        containerView.setOrientation(1);
        containerView.setBackgroundDrawable(this.shadowDrawable);
        containerView.setFitsSystemWindows(VERSION.SDK_INT >= 21);
        setContentView(containerView);
        boolean hasButtons = (this.positiveButtonText == null && this.negativeButtonText == null && this.neutralButtonText == null) ? false : true;
        if (this.title != null) {
            this.titleTextView = new TextView(getContext());
            this.titleTextView.setText(this.title);
            this.titleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            this.titleTextView.setTextSize(1, 20.0f);
            this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.titleTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
            View view = this.titleTextView;
            int i2 = (LocaleController.isRTL ? 5 : 3) | 48;
            if (this.items != null) {
                i = 14;
            } else {
                i = 10;
            }
            containerView.addView(view, LayoutHelper.createLinear(-2, -2, i2, 24, 19, 24, i));
        }
        if (this.progressViewStyle == 0) {
            this.shadow[0] = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.header_shadow).mutate();
            this.shadow[1] = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.header_shadow_reverse).mutate();
            this.shadow[0].setAlpha(0);
            this.shadow[1].setAlpha(0);
            this.shadow[0].setCallback(this);
            this.shadow[1].setCallback(this);
            this.contentScrollView = new ScrollView(getContext()) {
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (AlertDialog.this.shadow[0].getPaint().getAlpha() != 0) {
                        AlertDialog.this.shadow[0].setBounds(0, getScrollY(), getMeasuredWidth(), getScrollY() + AndroidUtilities.dp(3.0f));
                        AlertDialog.this.shadow[0].draw(canvas);
                    }
                    if (AlertDialog.this.shadow[1].getPaint().getAlpha() != 0) {
                        AlertDialog.this.shadow[1].setBounds(0, (getScrollY() + getMeasuredHeight()) - AndroidUtilities.dp(3.0f), getMeasuredWidth(), getScrollY() + getMeasuredHeight());
                        AlertDialog.this.shadow[1].draw(canvas);
                    }
                    return result;
                }
            };
            this.contentScrollView.setVerticalScrollBarEnabled(false);
            AndroidUtilities.setScrollViewEdgeEffectColor(this.contentScrollView, Theme.getColor(Theme.key_dialogScrollGlow));
            containerView.addView(this.contentScrollView, LayoutHelper.createLinear(-1, -2, 0.0f, 0.0f, 0.0f, 0.0f));
            this.scrollContainer = new LinearLayout(getContext());
            this.scrollContainer.setOrientation(1);
            this.contentScrollView.addView(this.scrollContainer, new FrameLayout.LayoutParams(-1, -2));
        }
        this.messageTextView = new TextView(getContext());
        this.messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        this.messageTextView.setTextSize(1, 16.0f);
        this.messageTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
        View view2;
        if (this.progressViewStyle == 1) {
            int i3;
            int i4;
            this.progressViewContainer = new FrameLayout(getContext());
            containerView.addView(this.progressViewContainer, LayoutHelper.createLinear(-1, 44, 51, 23, this.title == null ? 24 : 0, 23, 24));
            View radialProgressView = new RadialProgressView(getContext());
            radialProgressView.setProgressColor(Theme.getColor(Theme.key_dialogProgressCircle));
            this.progressViewContainer.addView(radialProgressView, LayoutHelper.createFrame(44, 44, (LocaleController.isRTL ? 5 : 3) | 48));
            this.messageTextView.setLines(1);
            this.messageTextView.setSingleLine(true);
            this.messageTextView.setEllipsize(TruncateAt.END);
            FrameLayout frameLayout = this.progressViewContainer;
            view2 = this.messageTextView;
            if (LocaleController.isRTL) {
                i2 = 5;
            } else {
                i2 = 3;
            }
            i2 |= 16;
            if (LocaleController.isRTL) {
                i3 = 0;
            } else {
                i3 = 62;
            }
            float f = (float) i3;
            if (LocaleController.isRTL) {
                i4 = 62;
            } else {
                i4 = 0;
            }
            frameLayout.addView(view2, LayoutHelper.createFrame(-2, -2.0f, i2, f, 0.0f, (float) i4, 0.0f));
        } else if (this.progressViewStyle == 2) {
            containerView.addView(this.messageTextView, LayoutHelper.createLinear(-2, -2, (LocaleController.isRTL ? 5 : 3) | 48, 24, this.title == null ? 19 : 0, 24, 20));
            this.lineProgressView = new LineProgressView(getContext());
            this.lineProgressView.setProgress(((float) this.currentProgress) / 100.0f, false);
            this.lineProgressView.setProgressColor(Theme.getColor(Theme.key_dialogLineProgress));
            this.lineProgressView.setBackColor(Theme.getColor(Theme.key_dialogLineProgressBackground));
            containerView.addView(this.lineProgressView, LayoutHelper.createLinear(-1, 4, 19, 24, 0, 24, 0));
            this.lineProgressViewPercent = new TextView(getContext());
            this.lineProgressViewPercent.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.lineProgressViewPercent.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
            this.lineProgressViewPercent.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            this.lineProgressViewPercent.setTextSize(1, 14.0f);
            containerView.addView(this.lineProgressViewPercent, LayoutHelper.createLinear(-2, -2, (LocaleController.isRTL ? 5 : 3) | 48, 23, 4, 23, 24));
            updateLineProgressTextView();
        } else {
            LinearLayout linearLayout = this.scrollContainer;
            view2 = this.messageTextView;
            i2 = (LocaleController.isRTL ? 5 : 3) | 48;
            i = (this.customView == null && this.items == null) ? 0 : 20;
            linearLayout.addView(view2, LayoutHelper.createLinear(-2, -2, i2, 24, 0, 24, i));
        }
        if (TextUtils.isEmpty(this.message)) {
            this.messageTextView.setVisibility(8);
        } else {
            this.messageTextView.setText(this.message);
            this.messageTextView.setVisibility(0);
        }
        if (this.items != null) {
            int a = 0;
            while (a < this.items.length) {
                if (this.items[a] != null) {
                    AlertDialogCell cell = new AlertDialogCell(this, getContext());
                    cell.setTextAndIcon(this.items[a], this.itemIcons != null ? this.itemIcons[a] : 0);
                    this.scrollContainer.addView(cell, LayoutHelper.createLinear(-1, 48));
                    cell.setTag(Integer.valueOf(a));
                    cell.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (AlertDialog.this.onClickListener != null) {
                                AlertDialog.this.onClickListener.onClick(AlertDialog.this, ((Integer) v.getTag()).intValue());
                            }
                            AlertDialog.this.dismiss();
                        }
                    });
                }
                a++;
            }
        }
        if (this.customView != null) {
            if (this.customView.getParent() != null) {
                ((ViewGroup) this.customView.getParent()).removeView(this.customView);
            }
            this.scrollContainer.addView(this.customView, LayoutHelper.createLinear(-1, -2));
        }
        if (hasButtons) {
            this.buttonsLayout = new FrameLayout(getContext()) {
                protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                    int count = getChildCount();
                    View positiveButton = null;
                    int width = right - left;
                    for (int a = 0; a < count; a++) {
                        View child = getChildAt(a);
                        if (((Integer) child.getTag()).intValue() == -1) {
                            positiveButton = child;
                            child.layout((width - getPaddingRight()) - child.getMeasuredWidth(), getPaddingTop(), (width - getPaddingRight()) + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                        } else if (((Integer) child.getTag()).intValue() == -2) {
                            int x = (width - getPaddingRight()) - child.getMeasuredWidth();
                            if (positiveButton != null) {
                                x -= positiveButton.getMeasuredWidth() + AndroidUtilities.dp(8.0f);
                            }
                            child.layout(x, getPaddingTop(), child.getMeasuredWidth() + x, getPaddingTop() + child.getMeasuredHeight());
                        } else {
                            child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                        }
                    }
                }
            };
            this.buttonsLayout.setPadding(AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f));
            containerView.addView(this.buttonsLayout, LayoutHelper.createLinear(-1, 52));
            if (this.positiveButtonText != null) {
                radialProgressView = new TextView(getContext()) {
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }
                };
                radialProgressView.setMinWidth(AndroidUtilities.dp(64.0f));
                radialProgressView.setTag(Integer.valueOf(-1));
                radialProgressView.setTextSize(1, 14.0f);
                radialProgressView.setTextColor(Theme.getColor(Theme.key_dialogButton));
                radialProgressView.setGravity(17);
                radialProgressView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                radialProgressView.setText(this.positiveButtonText.toString().toUpperCase());
                radialProgressView.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
                radialProgressView.setPadding(AndroidUtilities.dp(10.0f), 0, AndroidUtilities.dp(10.0f), 0);
                this.buttonsLayout.addView(radialProgressView, LayoutHelper.createFrame(-2, 36, 53));
                radialProgressView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (AlertDialog.this.positiveButtonListener != null) {
                            AlertDialog.this.positiveButtonListener.onClick(AlertDialog.this, -1);
                        }
                        AlertDialog.this.dismiss();
                    }
                });
            }
            if (this.negativeButtonText != null) {
                radialProgressView = new TextView(getContext()) {
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }
                };
                radialProgressView.setMinWidth(AndroidUtilities.dp(64.0f));
                radialProgressView.setTag(Integer.valueOf(-2));
                radialProgressView.setTextSize(1, 14.0f);
                radialProgressView.setTextColor(Theme.getColor(Theme.key_dialogButton));
                radialProgressView.setGravity(17);
                radialProgressView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                radialProgressView.setText(this.negativeButtonText.toString().toUpperCase());
                radialProgressView.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
                radialProgressView.setPadding(AndroidUtilities.dp(10.0f), 0, AndroidUtilities.dp(10.0f), 0);
                this.buttonsLayout.addView(radialProgressView, LayoutHelper.createFrame(-2, 36, 53));
                radialProgressView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (AlertDialog.this.negativeButtonListener != null) {
                            AlertDialog.this.negativeButtonListener.onClick(AlertDialog.this, -2);
                        }
                        AlertDialog.this.cancel();
                    }
                });
            }
            if (this.neutralButtonText != null) {
                radialProgressView = new TextView(getContext()) {
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }
                };
                radialProgressView.setMinWidth(AndroidUtilities.dp(64.0f));
                radialProgressView.setTag(Integer.valueOf(-3));
                radialProgressView.setTextSize(1, 14.0f);
                radialProgressView.setTextColor(Theme.getColor(Theme.key_dialogButton));
                radialProgressView.setGravity(17);
                radialProgressView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                radialProgressView.setText(this.neutralButtonText.toString().toUpperCase());
                radialProgressView.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
                radialProgressView.setPadding(AndroidUtilities.dp(10.0f), 0, AndroidUtilities.dp(10.0f), 0);
                this.buttonsLayout.addView(radialProgressView, LayoutHelper.createFrame(-2, 36, 51));
                radialProgressView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (AlertDialog.this.neutralButtonListener != null) {
                            AlertDialog.this.neutralButtonListener.onClick(AlertDialog.this, -2);
                        }
                        AlertDialog.this.dismiss();
                    }
                });
            }
        }
        this.lastScreenWidth = AndroidUtilities.displaySize.x;
        int calculatedWidth = AndroidUtilities.displaySize.x - AndroidUtilities.dp(56.0f);
        if (!AndroidUtilities.isTablet()) {
            maxWidth = AndroidUtilities.dp(356.0f);
        } else if (AndroidUtilities.isSmallTablet()) {
            maxWidth = AndroidUtilities.dp(446.0f);
        } else {
            maxWidth = AndroidUtilities.dp(496.0f);
        }
        Window window = getWindow();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(window.getAttributes());
        params.dimAmount = 0.6f;
        params.width = (Math.min(maxWidth, calculatedWidth) + this.backgroundPaddings.left) + this.backgroundPaddings.right;
        params.flags |= 2;
        if (this.customView != null) {
        }
        params.flags |= 131072;
        window.setAttributes(params);
    }

    private void runShadowAnimation(final int num, boolean show) {
        if ((show && !this.shadowVisibility[num]) || (!show && this.shadowVisibility[num])) {
            this.shadowVisibility[num] = show;
            if (this.shadowAnimation[num] != null) {
                this.shadowAnimation[num].cancel();
            }
            this.shadowAnimation[num] = new AnimatorSet();
            AnimatorSet animatorSet = this.shadowAnimation[num];
            Animator[] animatorArr = new Animator[1];
            Object obj = this.shadow[num];
            String str = "alpha";
            int[] iArr = new int[1];
            iArr[0] = show ? 255 : 0;
            animatorArr[0] = ObjectAnimator.ofInt(obj, str, iArr);
            animatorSet.playTogether(animatorArr);
            this.shadowAnimation[num].setDuration(150);
            this.shadowAnimation[num].addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (AlertDialog.this.shadowAnimation[num] != null && AlertDialog.this.shadowAnimation[num].equals(animation)) {
                        AlertDialog.this.shadowAnimation[num] = null;
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    if (AlertDialog.this.shadowAnimation[num] != null && AlertDialog.this.shadowAnimation[num].equals(animation)) {
                        AlertDialog.this.shadowAnimation[num] = null;
                    }
                }
            });
            this.shadowAnimation[num].start();
        }
    }

    public void setProgressStyle(int style) {
        this.progressViewStyle = style;
    }

    public void setProgress(int progress) {
        this.currentProgress = progress;
        if (this.lineProgressView != null) {
            this.lineProgressView.setProgress(((float) progress) / 100.0f, true);
            updateLineProgressTextView();
        }
    }

    private void updateLineProgressTextView() {
        this.lineProgressViewPercent.setText(String.format("%d%%", new Object[]{Integer.valueOf(this.currentProgress)}));
    }

    private boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void dismiss() {
        super.dismiss();
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
    }

    public void setMessage(CharSequence message) {
        if (TextUtils.isEmpty(message)) {
            this.messageTextView.setVisibility(8);
            return;
        }
        this.messageTextView.setText(message);
        this.messageTextView.setVisibility(0);
    }

    public void setButton(int type, CharSequence text, OnClickListener listener) {
        switch (type) {
            case C.RESULT_NOTHING_READ /*-3*/:
                this.neutralButtonText = text;
                this.neutralButtonListener = listener;
                return;
            case -2:
                this.negativeButtonText = text;
                this.negativeButtonListener = listener;
                return;
            case -1:
                this.positiveButtonText = text;
                this.positiveButtonListener = listener;
                return;
            default:
                return;
        }
    }

    public View getButton(int type) {
        return this.buttonsLayout.findViewWithTag(Integer.valueOf(type));
    }

    public void invalidateDrawable(Drawable who) {
        this.contentScrollView.invalidate();
        this.scrollContainer.invalidate();
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (this.contentScrollView != null) {
            this.contentScrollView.postDelayed(what, when);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (this.contentScrollView != null) {
            this.contentScrollView.removeCallbacks(what);
        }
    }
}
