package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.StaticLayout.Builder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class AboutLinkCell extends FrameLayout {
    private AboutLinkCellDelegate delegate;
    private ImageView imageView;
    private String oldText;
    private ClickableSpan pressedLink;
    private SpannableStringBuilder stringBuilder;
    private StaticLayout textLayout;
    private int textX;
    private int textY;
    private LinkPath urlPath = new LinkPath();

    public interface AboutLinkCellDelegate {
        void didPressUrl(String str);
    }

    public AboutLinkCell(Context context) {
        super(context);
        this.imageView = new ImageView(context);
        this.imageView.setScaleType(ScaleType.CENTER);
        this.imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), Mode.MULTIPLY));
        addView(this.imageView, LayoutHelper.createFrame(-2, -2.0f, (LocaleController.isRTL ? 5 : 3) | 48, LocaleController.isRTL ? 0.0f : 16.0f, 5.0f, LocaleController.isRTL ? 16.0f : 0.0f, 0.0f));
        setWillNotDraw(false);
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public void setDelegate(AboutLinkCellDelegate botHelpCellDelegate) {
        this.delegate = botHelpCellDelegate;
    }

    private void resetPressedLink() {
        if (this.pressedLink != null) {
            this.pressedLink = null;
        }
        invalidate();
    }

    public void setTextAndIcon(String text, int resId, boolean parseLinks) {
        if (!TextUtils.isEmpty(text)) {
            if (text == null || this.oldText == null || !text.equals(this.oldText)) {
                this.oldText = text;
                this.stringBuilder = new SpannableStringBuilder(this.oldText);
                if (parseLinks) {
                    MessageObject.addLinks(false, this.stringBuilder, false);
                }
                Emoji.replaceEmoji(this.stringBuilder, Theme.profile_aboutTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
                requestLayout();
                if (resId == 0) {
                    this.imageView.setImageDrawable(null);
                } else {
                    this.imageView.setImageResource(resId);
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (this.textLayout != null) {
            if (event.getAction() != 0) {
                if (r1.pressedLink == null || event.getAction() != 1) {
                    if (event.getAction() == 3) {
                        resetPressedLink();
                    }
                }
            }
            if (event.getAction() == 0) {
                resetPressedLink();
                try {
                    int x2 = (int) (x - ((float) r1.textX));
                    int line = r1.textLayout.getLineForVertical((int) (y - ((float) r1.textY)));
                    int off = r1.textLayout.getOffsetForHorizontal(line, (float) x2);
                    float left = r1.textLayout.getLineLeft(line);
                    if (left > ((float) x2) || r1.textLayout.getLineWidth(line) + left < ((float) x2)) {
                        resetPressedLink();
                    } else {
                        Spannable buffer = (Spannable) r1.textLayout.getText();
                        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                        if (link.length != 0) {
                            resetPressedLink();
                            r1.pressedLink = link[0];
                            result = true;
                            try {
                                int start = buffer.getSpanStart(r1.pressedLink);
                                r1.urlPath.setCurrentLayout(r1.textLayout, start, 0.0f);
                                r1.textLayout.getSelectionPath(start, buffer.getSpanEnd(r1.pressedLink), r1.urlPath);
                            } catch (Throwable e) {
                                FileLog.m3e(e);
                            }
                        } else {
                            resetPressedLink();
                        }
                    }
                } catch (Throwable e2) {
                    boolean result2 = result;
                    Throwable e3 = e2;
                    resetPressedLink();
                    FileLog.m3e(e3);
                    result = result2;
                }
            } else if (r1.pressedLink != null) {
                try {
                    if (r1.pressedLink instanceof URLSpanNoUnderline) {
                        String url = ((URLSpanNoUnderline) r1.pressedLink).getURL();
                        if ((url.startsWith("@") || url.startsWith("#") || url.startsWith("/")) && r1.delegate != null) {
                            r1.delegate.didPressUrl(url);
                        }
                    } else if (r1.pressedLink instanceof URLSpan) {
                        Browser.openUrl(getContext(), ((URLSpan) r1.pressedLink).getURL());
                    } else {
                        r1.pressedLink.onClick(r1);
                    }
                } catch (Throwable e22) {
                    FileLog.m3e(e22);
                }
                resetPressedLink();
                result = true;
            }
        }
        if (!result) {
            if (!super.onTouchEvent(event)) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint({"DrawAllocation"})
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.stringBuilder != null) {
            int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(87.0f);
            if (VERSION.SDK_INT >= 24) {
                this.textLayout = Builder.obtain(this.stringBuilder, 0, this.stringBuilder.length(), Theme.profile_aboutTextPaint, maxWidth).setBreakStrategy(1).setHyphenationFrequency(0).setAlignment(Alignment.ALIGN_NORMAL).build();
            } else {
                this.textLayout = new StaticLayout(this.stringBuilder, Theme.profile_aboutTextPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), NUM), MeasureSpec.makeMeasureSpec((this.textLayout != null ? this.textLayout.getHeight() : AndroidUtilities.dp(20.0f)) + AndroidUtilities.dp(16.0f), NUM));
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        int dp = AndroidUtilities.dp(LocaleController.isRTL ? 16.0f : 71.0f);
        this.textX = dp;
        float f = (float) dp;
        int dp2 = AndroidUtilities.dp(8.0f);
        this.textY = dp2;
        canvas.translate(f, (float) dp2);
        if (this.pressedLink != null) {
            canvas.drawPath(this.urlPath, Theme.linkSelectionPaint);
        }
        try {
            if (this.textLayout != null) {
                this.textLayout.draw(canvas);
            }
        } catch (Throwable e) {
            FileLog.m3e(e);
        }
        canvas.restore();
    }
}
