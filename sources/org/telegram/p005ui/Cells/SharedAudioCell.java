package org.telegram.p005ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import java.io.File;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CLASSNAMER;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Components.CheckBox;
import org.telegram.p005ui.Components.CombinedDrawable;
import org.telegram.p005ui.Components.LayoutHelper;
import org.telegram.p005ui.Components.RadialProgress;

/* renamed from: org.telegram.ui.Cells.SharedAudioCell */
public class SharedAudioCell extends FrameLayout implements FileDownloadProgressListener {
    private int TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
    private boolean buttonPressed;
    private int buttonState;
    private int buttonX;
    private int buttonY;
    private CheckBox checkBox;
    private int currentAccount = UserConfig.selectedAccount;
    private MessageObject currentMessageObject;
    private StaticLayout descriptionLayout;
    private int descriptionY = AndroidUtilities.m10dp(29.0f);
    private int hasMiniProgress;
    private boolean miniButtonPressed;
    private int miniButtonState;
    private boolean needDivider;
    private RadialProgress radialProgress = new RadialProgress(this);
    private StaticLayout titleLayout;
    private int titleY = AndroidUtilities.m10dp(9.0f);

    public SharedAudioCell(Context context) {
        float f = 40.0f;
        super(context);
        setWillNotDraw(false);
        this.checkBox = new CheckBox(context, CLASSNAMER.drawable.round_check2);
        this.checkBox.setVisibility(4);
        this.checkBox.setColor(Theme.getColor(Theme.key_checkbox), Theme.getColor(Theme.key_checkboxCheck));
        View view = this.checkBox;
        int i = (LocaleController.isRTL ? 5 : 3) | 48;
        float f2 = LocaleController.isRTL ? 0.0f : 40.0f;
        if (!LocaleController.isRTL) {
            f = 0.0f;
        }
        addView(view, LayoutHelper.createFrame(20, 20.0f, i, f2, 34.0f, f, 0.0f));
    }

    @SuppressLint({"DrawAllocation"})
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.descriptionLayout = null;
        this.titleLayout = null;
        int maxWidth = (MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.m10dp((float) AndroidUtilities.leftBaseline)) - AndroidUtilities.m10dp(28.0f);
        try {
            String title = this.currentMessageObject.getMusicTitle();
            this.titleLayout = new StaticLayout(TextUtils.ellipsize(title.replace(10, ' '), Theme.chat_contextResult_titleTextPaint, (float) Math.min((int) Math.ceil((double) Theme.chat_contextResult_titleTextPaint.measureText(title)), maxWidth), TruncateAt.END), Theme.chat_contextResult_titleTextPaint, maxWidth + AndroidUtilities.m10dp(4.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Throwable e) {
            FileLog.m14e(e);
        }
        try {
            String author = this.currentMessageObject.getMusicAuthor();
            this.descriptionLayout = new StaticLayout(TextUtils.ellipsize(author.replace(10, ' '), Theme.chat_contextResult_descriptionTextPaint, (float) Math.min((int) Math.ceil((double) Theme.chat_contextResult_descriptionTextPaint.measureText(author)), maxWidth), TruncateAt.END), Theme.chat_contextResult_descriptionTextPaint, maxWidth + AndroidUtilities.m10dp(4.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Throwable e2) {
            FileLog.m14e(e2);
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (this.needDivider ? 1 : 0) + AndroidUtilities.m10dp(56.0f));
        int x = LocaleController.isRTL ? (MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.m10dp(8.0f)) - AndroidUtilities.m10dp(52.0f) : AndroidUtilities.m10dp(8.0f);
        RadialProgress radialProgress = this.radialProgress;
        int dp = AndroidUtilities.m10dp(4.0f) + x;
        this.buttonX = dp;
        int dp2 = AndroidUtilities.m10dp(6.0f);
        this.buttonY = dp2;
        radialProgress.setProgressRect(dp, dp2, AndroidUtilities.m10dp(48.0f) + x, AndroidUtilities.m10dp(50.0f));
        measureChildWithMargins(this.checkBox, widthMeasureSpec, 0, heightMeasureSpec, 0);
    }

    public void setMessageObject(MessageObject messageObject, boolean divider) {
        this.needDivider = divider;
        this.currentMessageObject = messageObject;
        requestLayout();
        updateButtonState(false);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (this.checkBox.getVisibility() != 0) {
            this.checkBox.setVisibility(0);
        }
        this.checkBox.setChecked(checked, animated);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
    }

    public MessageObject getMessage() {
        return this.currentMessageObject;
    }

    private boolean checkAudioMotionEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int side = AndroidUtilities.m10dp(36.0f);
        boolean area = false;
        if (this.miniButtonState >= 0) {
            int offset = AndroidUtilities.m10dp(27.0f);
            if (x < this.buttonX + offset || x > (this.buttonX + offset) + side || y < this.buttonY + offset || y > (this.buttonY + offset) + side) {
                area = false;
            } else {
                area = true;
            }
        }
        if (event.getAction() == 0) {
            if (!area) {
                return false;
            }
            this.miniButtonPressed = true;
            invalidate();
            updateRadialProgressBackground();
            return true;
        } else if (!this.miniButtonPressed) {
            return false;
        } else {
            if (event.getAction() == 1) {
                this.miniButtonPressed = false;
                playSoundEffect(0);
                didPressedMiniButton(true);
                invalidate();
            } else if (event.getAction() == 3) {
                this.miniButtonPressed = false;
                invalidate();
            } else if (event.getAction() == 2 && !area) {
                this.miniButtonPressed = false;
                invalidate();
            }
            updateRadialProgressBackground();
            return false;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.currentMessageObject == null) {
            return super.onTouchEvent(event);
        }
        boolean result = checkAudioMotionEvent(event);
        if (event.getAction() != 3) {
            return result;
        }
        this.miniButtonPressed = false;
        this.buttonPressed = false;
        return false;
    }

    private void updateRadialProgressBackground() {
        this.radialProgress.swapBackground(getDrawableForCurrentState());
        if (this.hasMiniProgress != 0) {
            this.radialProgress.swapMiniBackground(getMiniDrawableForCurrentState());
        }
    }

    private void didPressedMiniButton(boolean animated) {
        if (this.miniButtonState == 0) {
            this.miniButtonState = 1;
            this.radialProgress.setProgress(0.0f, false);
            FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 0);
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
            invalidate();
        } else if (this.miniButtonState == 1) {
            if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                MediaController.getInstance().cleanupPlayer(true, true);
            }
            this.miniButtonState = 0;
            FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.getDocument());
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
            invalidate();
        }
    }

    public void didPressedButton() {
        if (this.buttonState == 0) {
            if (this.miniButtonState == 0) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 0);
            }
            if (needPlayMessage(this.currentMessageObject)) {
                if (this.hasMiniProgress == 2 && this.miniButtonState != 1) {
                    this.miniButtonState = 1;
                    this.radialProgress.setProgress(0.0f, false);
                    this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
                }
                this.buttonState = 1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            }
        } else if (this.buttonState == 1) {
            if (MediaController.getInstance().pauseMessage(this.currentMessageObject)) {
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            }
        } else if (this.buttonState == 2) {
            this.radialProgress.setProgress(0.0f, false);
            FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 0);
            this.buttonState = 4;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, false);
            invalidate();
        } else if (this.buttonState == 4) {
            FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.getDocument());
            this.buttonState = 2;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        float f = 8.0f;
        if (this.titleLayout != null) {
            canvas.save();
            canvas.translate((float) AndroidUtilities.m10dp(LocaleController.isRTL ? 8.0f : (float) AndroidUtilities.leftBaseline), (float) this.titleY);
            this.titleLayout.draw(canvas);
            canvas.restore();
        }
        if (this.descriptionLayout != null) {
            Theme.chat_contextResult_descriptionTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            canvas.save();
            if (!LocaleController.isRTL) {
                f = (float) AndroidUtilities.leftBaseline;
            }
            canvas.translate((float) AndroidUtilities.m10dp(f), (float) this.descriptionY);
            this.descriptionLayout.draw(canvas);
            canvas.restore();
        }
        this.radialProgress.setProgressColor(Theme.getColor(this.buttonPressed ? Theme.key_chat_inAudioSelectedProgress : Theme.key_chat_inAudioProgress));
        this.radialProgress.draw(canvas);
        if (this.needDivider) {
            canvas.drawLine((float) AndroidUtilities.m10dp(72.0f), (float) (getHeight() - 1), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - 1), Theme.dividerPaint);
        }
    }

    private Drawable getMiniDrawableForCurrentState() {
        int i = 0;
        if (this.miniButtonState < 0) {
            return null;
        }
        this.radialProgress.setAlphaForPrevious(false);
        CombinedDrawable[] combinedDrawableArr = Theme.chat_fileMiniStatesDrawable[this.miniButtonState + 2];
        if (this.miniButtonPressed) {
            i = 1;
        }
        return combinedDrawableArr[i];
    }

    private Drawable getDrawableForCurrentState() {
        int i = 0;
        if (this.buttonState == -1) {
            return null;
        }
        this.radialProgress.setAlphaForPrevious(false);
        Drawable[] drawableArr = Theme.chat_fileStatesDrawable[this.buttonState + 5];
        if (this.buttonPressed) {
            i = 1;
        }
        return drawableArr[i];
    }

    public void updateButtonState(boolean animated) {
        String fileName = this.currentMessageObject.getFileName();
        File cacheFile = null;
        if (!TextUtils.isEmpty(this.currentMessageObject.messageOwner.attachPath)) {
            cacheFile = new File(this.currentMessageObject.messageOwner.attachPath);
            if (!cacheFile.exists()) {
                cacheFile = null;
            }
        }
        if (cacheFile == null) {
            cacheFile = FileLoader.getPathToAttach(this.currentMessageObject.getDocument());
        }
        if (TextUtils.isEmpty(fileName)) {
            this.radialProgress.setBackground(null, false, false);
            return;
        }
        if (cacheFile.exists() && cacheFile.length() == 0) {
            cacheFile.delete();
        }
        boolean fileExists = cacheFile.exists();
        if (SharedConfig.streamMedia && this.currentMessageObject.isMusic() && ((int) this.currentMessageObject.getDialogId()) != 0) {
            this.hasMiniProgress = fileExists ? 1 : 2;
            fileExists = true;
        } else {
            this.miniButtonState = -1;
        }
        boolean playing;
        Float progress;
        if (this.hasMiniProgress != 0) {
            boolean z;
            this.radialProgress.setMiniProgressBackgroundColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outLoader : Theme.key_chat_inLoader));
            playing = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
            if (!playing || (playing && MediaController.getInstance().isMessagePaused())) {
                this.buttonState = 0;
            } else {
                this.buttonState = 1;
            }
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            if (this.hasMiniProgress == 1) {
                DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                this.miniButtonState = -1;
            } else {
                DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                    this.miniButtonState = 1;
                    progress = ImageLoader.getInstance().getFileProgress(fileName);
                    if (progress != null) {
                        this.radialProgress.setProgress(progress.floatValue(), animated);
                    } else {
                        this.radialProgress.setProgress(0.0f, animated);
                    }
                } else {
                    this.radialProgress.setProgress(0.0f, animated);
                    this.miniButtonState = 0;
                }
            }
            RadialProgress radialProgress = this.radialProgress;
            Drawable miniDrawableForCurrentState = getMiniDrawableForCurrentState();
            if (this.miniButtonState == 1) {
                z = true;
            } else {
                z = false;
            }
            radialProgress.setMiniBackground(miniDrawableForCurrentState, z, animated);
        } else if (fileExists) {
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
            playing = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
            if (!playing || (playing && MediaController.getInstance().isMessagePaused())) {
                this.buttonState = 0;
            } else {
                this.buttonState = 1;
            }
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            invalidate();
        } else {
            DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this);
            if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                this.buttonState = 4;
                progress = ImageLoader.getInstance().getFileProgress(fileName);
                if (progress != null) {
                    this.radialProgress.setProgress(progress.floatValue(), animated);
                } else {
                    this.radialProgress.setProgress(0.0f, animated);
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
            } else {
                this.buttonState = 2;
                this.radialProgress.setProgress(0.0f, animated);
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            }
            invalidate();
        }
    }

    public void onFailedDownload(String fileName) {
        updateButtonState(false);
    }

    public void onSuccessDownload(String fileName) {
        this.radialProgress.setProgress(1.0f, true);
        updateButtonState(true);
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.hasMiniProgress != 0) {
            if (this.miniButtonState != 1) {
                updateButtonState(false);
            }
        } else if (this.buttonState != 4) {
            updateButtonState(false);
        }
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
    }

    public int getObserverTag() {
        return this.TAG;
    }

    protected boolean needPlayMessage(MessageObject messageObject) {
        return false;
    }
}
