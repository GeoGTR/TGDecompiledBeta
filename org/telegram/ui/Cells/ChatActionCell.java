package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.PhotoViewer;

public class ChatActionCell extends BaseCell {
    private static Paint backPaint;
    private static TextPaint textPaint;
    private AvatarDrawable avatarDrawable;
    private MessageObject currentMessageObject;
    private int customDate;
    private CharSequence customText;
    private ChatActionCellDelegate delegate;
    private boolean hasReplyMessage;
    private boolean imagePressed = false;
    private ImageReceiver imageReceiver;
    private URLSpan pressedLink;
    private int previousWidth = 0;
    private int textHeight = 0;
    private StaticLayout textLayout;
    private int textWidth = 0;
    private int textX = 0;
    private int textXLeft = 0;
    private int textY = 0;

    public interface ChatActionCellDelegate {
        void didClickedImage(ChatActionCell chatActionCell);

        void didLongPressed(ChatActionCell chatActionCell);

        void didPressedBotButton(MessageObject messageObject, KeyboardButton keyboardButton);

        void didPressedReplyMessage(ChatActionCell chatActionCell, int i);

        void needOpenUserProfile(int i);
    }

    public ChatActionCell(Context context) {
        super(context);
        if (textPaint == null) {
            textPaint = new TextPaint(1);
            textPaint.setColor(-1);
            textPaint.linkColor = -1;
            textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            backPaint = new Paint(1);
        }
        backPaint.setColor(ApplicationLoader.getServiceMessageColor());
        this.imageReceiver = new ImageReceiver(this);
        this.imageReceiver.setRoundRadius(AndroidUtilities.dp(32.0f));
        this.avatarDrawable = new AvatarDrawable();
        textPaint.setTextSize((float) AndroidUtilities.dp((float) (MessagesController.getInstance().fontSize - 2)));
    }

    public void setDelegate(ChatActionCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setCustomDate(int date) {
        if (this.customDate != date) {
            CharSequence newText = LocaleController.formatDateChat((long) date);
            if (this.customText == null || !TextUtils.equals(newText, this.customText)) {
                this.previousWidth = 0;
                this.customDate = date;
                this.customText = newText;
                requestLayout();
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        ChatActionCell.this.requestLayout();
                    }
                });
            } else if (this.textLayout == null) {
                requestLayout();
            }
        } else if (this.textLayout == null) {
            requestLayout();
        }
    }

    public void setMessageObject(MessageObject messageObject) {
        boolean z = true;
        if (this.currentMessageObject != messageObject || (!this.hasReplyMessage && messageObject.replyMessageObject != null)) {
            this.currentMessageObject = messageObject;
            this.hasReplyMessage = messageObject.replyMessageObject != null;
            this.previousWidth = 0;
            if (this.currentMessageObject.type == 11) {
                int id = 0;
                if (messageObject.messageOwner.to_id != null) {
                    if (messageObject.messageOwner.to_id.chat_id != 0) {
                        id = messageObject.messageOwner.to_id.chat_id;
                    } else if (messageObject.messageOwner.to_id.channel_id != 0) {
                        id = messageObject.messageOwner.to_id.channel_id;
                    } else {
                        id = messageObject.messageOwner.to_id.user_id;
                        if (id == UserConfig.getClientUserId()) {
                            id = messageObject.messageOwner.from_id;
                        }
                    }
                }
                this.avatarDrawable.setInfo(id, null, null, false);
                if (this.currentMessageObject.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                    this.imageReceiver.setImage(this.currentMessageObject.messageOwner.action.newUserPhoto.photo_small, "50_50", this.avatarDrawable, null, false);
                } else {
                    PhotoSize photo = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.photoThumbs, AndroidUtilities.dp(64.0f));
                    if (photo != null) {
                        this.imageReceiver.setImage(photo.location, "50_50", this.avatarDrawable, null, false);
                    } else {
                        this.imageReceiver.setImageBitmap(this.avatarDrawable);
                    }
                }
                ImageReceiver imageReceiver = this.imageReceiver;
                if (PhotoViewer.getInstance().isShowingImage(this.currentMessageObject)) {
                    z = false;
                }
                imageReceiver.setVisible(z, false);
            } else {
                this.imageReceiver.setImageBitmap((Bitmap) null);
            }
            requestLayout();
        }
    }

    public MessageObject getMessageObject() {
        return this.currentMessageObject;
    }

    public ImageReceiver getPhotoImage() {
        return this.imageReceiver;
    }

    protected void onLongPress() {
        if (this.delegate != null) {
            this.delegate.didLongPressed(this);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.currentMessageObject == null) {
            return super.onTouchEvent(event);
        }
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.imagePressed) {
                if (event.getAction() == 1) {
                    this.imagePressed = false;
                    if (this.delegate != null) {
                        this.delegate.didClickedImage(this);
                        playSoundEffect(0);
                    }
                } else if (event.getAction() == 3) {
                    this.imagePressed = false;
                } else if (event.getAction() == 2 && !this.imageReceiver.isInsideImage(x, y)) {
                    this.imagePressed = false;
                }
            }
        } else if (this.delegate != null) {
            if (this.currentMessageObject.type == 11 && this.imageReceiver.isInsideImage(x, y)) {
                this.imagePressed = true;
                result = true;
            }
            if (result) {
                startCheckLongPress();
            }
        }
        if (!result && (event.getAction() == 0 || (this.pressedLink != null && event.getAction() == 1))) {
            if (x < ((float) this.textX) || y < ((float) this.textY) || x > ((float) (this.textX + this.textWidth)) || y > ((float) (this.textY + this.textHeight))) {
                this.pressedLink = null;
            } else {
                x -= (float) this.textXLeft;
                int line = this.textLayout.getLineForVertical((int) (y - ((float) this.textY)));
                int off = this.textLayout.getOffsetForHorizontal(line, x);
                float left = this.textLayout.getLineLeft(line);
                if (left > x || this.textLayout.getLineWidth(line) + left < x || !(this.currentMessageObject.messageText instanceof Spannable)) {
                    this.pressedLink = null;
                } else {
                    URLSpan[] link = (URLSpan[]) this.currentMessageObject.messageText.getSpans(off, off, URLSpan.class);
                    if (link.length == 0) {
                        this.pressedLink = null;
                    } else if (event.getAction() == 0) {
                        this.pressedLink = link[0];
                        result = true;
                    } else if (link[0] == this.pressedLink) {
                        if (this.delegate != null) {
                            String url = link[0].getURL();
                            if (url.startsWith("game")) {
                                this.delegate.didPressedReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
                            } else {
                                this.delegate.needOpenUserProfile(Integer.parseInt(url));
                            }
                        }
                        result = true;
                    }
                }
            }
        }
        if (result) {
            return result;
        }
        return super.onTouchEvent(event);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.currentMessageObject == null && this.customText == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.textHeight + AndroidUtilities.dp(14.0f));
            return;
        }
        int width = Math.max(AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE), MeasureSpec.getSize(widthMeasureSpec));
        if (width != this.previousWidth) {
            CharSequence text;
            if (this.currentMessageObject != null) {
                text = this.currentMessageObject.messageText;
            } else {
                text = this.customText;
            }
            this.previousWidth = width;
            int maxWidth = width - AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
            this.textLayout = new StaticLayout(text, textPaint, maxWidth, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            this.textHeight = 0;
            this.textWidth = 0;
            try {
                int linesCount = this.textLayout.getLineCount();
                int a = 0;
                while (a < linesCount) {
                    try {
                        float lineWidth = this.textLayout.getLineWidth(a);
                        if (lineWidth > ((float) maxWidth)) {
                            lineWidth = (float) maxWidth;
                        }
                        this.textHeight = (int) Math.max((double) this.textHeight, Math.ceil((double) this.textLayout.getLineBottom(a)));
                        this.textWidth = (int) Math.max((double) this.textWidth, Math.ceil((double) lineWidth));
                        a++;
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                        return;
                    }
                }
            } catch (Throwable e2) {
                FileLog.e("tmessages", e2);
            }
            this.textX = (width - this.textWidth) / 2;
            this.textY = AndroidUtilities.dp(7.0f);
            this.textXLeft = (width - this.textLayout.getWidth()) / 2;
            if (this.currentMessageObject != null && this.currentMessageObject.type == 11) {
                this.imageReceiver.setImageCoords((width - AndroidUtilities.dp(64.0f)) / 2, this.textHeight + AndroidUtilities.dp(15.0f), AndroidUtilities.dp(64.0f), AndroidUtilities.dp(64.0f));
            }
        }
        int i = this.textHeight;
        int i2 = (this.currentMessageObject == null || this.currentMessageObject.type != 11) ? 0 : 70;
        setMeasuredDimension(width, AndroidUtilities.dp((float) (i2 + 14)) + i);
    }

    public int getCustomDate() {
        return this.customDate;
    }

    private int findMaxWidthAroundLine(int line) {
        int a;
        int width = (int) Math.ceil((double) this.textLayout.getLineWidth(line));
        int count = this.textLayout.getLineCount();
        for (a = line + 1; a < count; a++) {
            int w = (int) Math.ceil((double) this.textLayout.getLineWidth(a));
            if (Math.abs(w - width) >= AndroidUtilities.dp(12.0f)) {
                break;
            }
            width = Math.max(w, width);
        }
        for (a = line - 1; a >= 0; a--) {
            w = (int) Math.ceil((double) this.textLayout.getLineWidth(a));
            if (Math.abs(w - width) >= AndroidUtilities.dp(12.0f)) {
                break;
            }
            width = Math.max(w, width);
        }
        return width;
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null && this.currentMessageObject.type == 11) {
            this.imageReceiver.draw(canvas);
        }
        if (this.textLayout != null) {
            int count = this.textLayout.getLineCount();
            int corner = AndroidUtilities.dp(6.0f);
            int y = AndroidUtilities.dp(7.0f);
            int previousLineBottom = 0;
            int a = 0;
            while (a < count) {
                int dy;
                int dx;
                int width = findMaxWidthAroundLine(a);
                int x = ((getMeasuredWidth() - width) / 2) - AndroidUtilities.dp(3.0f);
                width += AndroidUtilities.dp(6.0f);
                int lineBottom = this.textLayout.getLineBottom(a);
                int height = lineBottom - previousLineBottom;
                int additionalHeight = 0;
                previousLineBottom = lineBottom;
                boolean drawBottomCorners = a == count + -1;
                boolean drawTopCorners = a == 0;
                if (drawTopCorners) {
                    y -= AndroidUtilities.dp(3.0f);
                    height += AndroidUtilities.dp(3.0f);
                }
                if (drawBottomCorners) {
                    height += AndroidUtilities.dp(3.0f);
                }
                canvas.drawRect((float) x, (float) y, (float) (x + width), (float) (y + height), backPaint);
                if (!drawBottomCorners && a + 1 < count) {
                    int nextLineWidth = findMaxWidthAroundLine(a + 1) + AndroidUtilities.dp(6.0f);
                    if ((corner * 2) + nextLineWidth < width) {
                        int nextX = (getMeasuredWidth() - nextLineWidth) / 2;
                        drawBottomCorners = true;
                        additionalHeight = AndroidUtilities.dp(3.0f);
                        canvas.drawRect((float) x, (float) (y + height), (float) nextX, (float) ((y + height) + AndroidUtilities.dp(3.0f)), backPaint);
                        canvas.drawRect((float) (nextX + nextLineWidth), (float) (y + height), (float) (x + width), (float) ((y + height) + AndroidUtilities.dp(3.0f)), backPaint);
                    } else if ((corner * 2) + width < nextLineWidth) {
                        additionalHeight = AndroidUtilities.dp(3.0f);
                        dy = (y + height) - AndroidUtilities.dp(9.0f);
                        dx = x - (corner * 2);
                        Theme.cornerInner[2].setBounds(dx, dy, dx + corner, dy + corner);
                        Theme.cornerInner[2].draw(canvas);
                        dx = (x + width) + corner;
                        Theme.cornerInner[3].setBounds(dx, dy, dx + corner, dy + corner);
                        Theme.cornerInner[3].draw(canvas);
                    } else {
                        additionalHeight = AndroidUtilities.dp(6.0f);
                    }
                }
                if (!drawTopCorners && a > 0) {
                    int prevLineWidth = findMaxWidthAroundLine(a - 1) + AndroidUtilities.dp(6.0f);
                    if ((corner * 2) + prevLineWidth < width) {
                        int prevX = (getMeasuredWidth() - prevLineWidth) / 2;
                        drawTopCorners = true;
                        y -= AndroidUtilities.dp(3.0f);
                        height += AndroidUtilities.dp(3.0f);
                        canvas.drawRect((float) x, (float) y, (float) prevX, (float) (AndroidUtilities.dp(3.0f) + y), backPaint);
                        canvas.drawRect((float) (prevX + prevLineWidth), (float) y, (float) (x + width), (float) (AndroidUtilities.dp(3.0f) + y), backPaint);
                    } else if ((corner * 2) + width < prevLineWidth) {
                        y -= AndroidUtilities.dp(3.0f);
                        height += AndroidUtilities.dp(3.0f);
                        dy = y + corner;
                        dx = x - (corner * 2);
                        Theme.cornerInner[0].setBounds(dx, dy, dx + corner, dy + corner);
                        Theme.cornerInner[0].draw(canvas);
                        dx = (x + width) + corner;
                        Theme.cornerInner[1].setBounds(dx, dy, dx + corner, dy + corner);
                        Theme.cornerInner[1].draw(canvas);
                    } else {
                        y -= AndroidUtilities.dp(6.0f);
                        height += AndroidUtilities.dp(6.0f);
                    }
                }
                canvas.drawRect((float) (x - corner), (float) (y + corner), (float) x, (float) (((y + height) + additionalHeight) - corner), backPaint);
                canvas.drawRect((float) (x + width), (float) (y + corner), (float) ((x + width) + corner), (float) (((y + height) + additionalHeight) - corner), backPaint);
                if (drawTopCorners) {
                    dx = x - corner;
                    Theme.cornerOuter[0].setBounds(dx, y, dx + corner, y + corner);
                    Theme.cornerOuter[0].draw(canvas);
                    dx = x + width;
                    Theme.cornerOuter[1].setBounds(dx, y, dx + corner, y + corner);
                    Theme.cornerOuter[1].draw(canvas);
                }
                if (drawBottomCorners) {
                    dy = ((y + height) + additionalHeight) - corner;
                    dx = x + width;
                    Theme.cornerOuter[2].setBounds(dx, dy, dx + corner, dy + corner);
                    Theme.cornerOuter[2].draw(canvas);
                    dx = x - corner;
                    Theme.cornerOuter[3].setBounds(dx, dy, dx + corner, dy + corner);
                    Theme.cornerOuter[3].draw(canvas);
                }
                y += height;
                a++;
            }
            canvas.save();
            canvas.translate((float) this.textXLeft, (float) this.textY);
            this.textLayout.draw(canvas);
            canvas.restore();
        }
    }
}
