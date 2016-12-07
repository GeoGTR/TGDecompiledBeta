package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewStructure;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.FileDownloadProgressListener;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessageObject.TextLayoutBlock;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.exoplayer.util.MimeTypes;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_game;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonSwitchInline;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonUrl;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SeekBarWaveform;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.PhotoViewer;

public class ChatMessageCell extends BaseCell implements SeekBarDelegate, ImageReceiverDelegate, FileDownloadProgressListener {
    private static final int DOCUMENT_ATTACH_TYPE_AUDIO = 3;
    private static final int DOCUMENT_ATTACH_TYPE_DOCUMENT = 1;
    private static final int DOCUMENT_ATTACH_TYPE_GIF = 2;
    private static final int DOCUMENT_ATTACH_TYPE_MUSIC = 5;
    private static final int DOCUMENT_ATTACH_TYPE_NONE = 0;
    private static final int DOCUMENT_ATTACH_TYPE_STICKER = 6;
    private static final int DOCUMENT_ATTACH_TYPE_VIDEO = 4;
    private static TextPaint audioPerformerPaint;
    private static TextPaint audioTimePaint;
    private static TextPaint audioTitlePaint;
    private static TextPaint botButtonPaint;
    private static Paint botProgressPaint;
    private static TextPaint contactNamePaint;
    private static TextPaint contactPhonePaint;
    private static Paint deleteProgressPaint;
    private static Paint docBackPaint;
    private static TextPaint docNamePaint;
    private static TextPaint durationPaint;
    private static TextPaint forwardNamePaint;
    private static TextPaint gamePaint;
    private static TextPaint infoPaint;
    private static TextPaint locationAddressPaint;
    private static TextPaint locationTitlePaint;
    private static TextPaint namePaint;
    private static Paint replyLinePaint;
    private static TextPaint replyNamePaint;
    private static TextPaint replyTextPaint;
    private static TextPaint timePaint;
    private static Paint urlPaint;
    private static Paint urlSelectionPaint;
    private int TAG;
    private boolean allowAssistant;
    private StaticLayout authorLayout;
    private int authorX;
    private int availableTimeWidth;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver avatarImage;
    private boolean avatarPressed;
    private int backgroundDrawableLeft;
    private int backgroundWidth = 100;
    private ArrayList<BotButton> botButtons = new ArrayList();
    private HashMap<String, BotButton> botButtonsByData = new HashMap();
    private int buttonPressed;
    private int buttonState;
    private int buttonX;
    private int buttonY;
    private boolean cancelLoading;
    private int captionHeight;
    private StaticLayout captionLayout;
    private int captionX;
    private int captionY;
    private AvatarDrawable contactAvatarDrawable;
    private Drawable currentBackgroundDrawable;
    private Chat currentChat;
    private Chat currentForwardChannel;
    private String currentForwardNameString;
    private User currentForwardUser;
    private MessageObject currentMessageObject;
    private String currentNameString;
    private FileLocation currentPhoto;
    private String currentPhotoFilter;
    private String currentPhotoFilterThumb;
    private PhotoSize currentPhotoObject;
    private PhotoSize currentPhotoObjectThumb;
    private FileLocation currentReplyPhoto;
    private String currentTimeString;
    private String currentUrl;
    private User currentUser;
    private User currentViaBotUser;
    private String currentViewsString;
    private ChatMessageCellDelegate delegate;
    private RectF deleteProgressRect = new RectF();
    private StaticLayout descriptionLayout;
    private int descriptionX;
    private int descriptionY;
    private boolean disallowLongPress;
    private StaticLayout docTitleLayout;
    private int docTitleOffsetX;
    private Document documentAttach;
    private int documentAttachType;
    private boolean drawBackground = true;
    private boolean drawForwardedName;
    private boolean drawImageButton;
    private boolean drawName;
    private boolean drawNameLayout;
    private boolean drawPhotoImage;
    private boolean drawShareButton;
    private boolean drawTime = true;
    private StaticLayout durationLayout;
    private int durationWidth;
    private int firstVisibleBlockNum;
    private boolean forwardBotPressed;
    private boolean forwardName;
    private float[] forwardNameOffsetX = new float[2];
    private boolean forwardNamePressed;
    private int forwardNameX;
    private int forwardNameY;
    private StaticLayout[] forwardedNameLayout = new StaticLayout[2];
    private int forwardedNameWidth;
    private boolean gamePreviewPressed;
    private boolean hasGamePreview;
    private boolean hasLinkPreview;
    private boolean imagePressed;
    private boolean inLayout;
    private StaticLayout infoLayout;
    private int infoWidth;
    private boolean isAvatarVisible;
    public boolean isChat;
    private boolean isCheckPressed = true;
    private boolean isHighlighted;
    private boolean isPressed;
    private boolean isSmallImage;
    private int keyboardHeight;
    private int lastDeleteDate;
    private int lastSendState;
    private String lastTimeString;
    private int lastViewsCount;
    private int lastVisibleBlockNum;
    private int layoutHeight;
    private int layoutWidth;
    private int linkBlockNum;
    private int linkPreviewHeight;
    private boolean linkPreviewPressed;
    private int linkSelectionBlockNum;
    private boolean mediaBackground;
    private int mediaOffsetY;
    private StaticLayout nameLayout;
    private float nameOffsetX;
    private int nameWidth;
    private float nameX;
    private float nameY;
    private int namesOffset;
    private boolean needNewVisiblePart;
    private boolean needReplyImage;
    private boolean otherPressed;
    private int otherX;
    private int otherY;
    private StaticLayout performerLayout;
    private int performerX;
    private ImageReceiver photoImage;
    private boolean photoNotSet;
    private int pressedBotButton;
    private ClickableSpan pressedLink;
    private int pressedLinkType;
    private RadialProgress radialProgress;
    private RectF rect = new RectF();
    private ImageReceiver replyImageReceiver;
    private StaticLayout replyNameLayout;
    private float replyNameOffset;
    private int replyNameWidth;
    private boolean replyPressed;
    private int replyStartX;
    private int replyStartY;
    private StaticLayout replyTextLayout;
    private float replyTextOffset;
    private int replyTextWidth;
    private Rect scrollRect = new Rect();
    private SeekBar seekBar;
    private SeekBarWaveform seekBarWaveform;
    private int seekBarX;
    private int seekBarY;
    private boolean sharePressed;
    private int shareStartX;
    private int shareStartY;
    private StaticLayout siteNameLayout;
    private StaticLayout songLayout;
    private int songX;
    private int substractBackgroundHeight;
    private int textX;
    private int textY;
    private int timeAudioX;
    private StaticLayout timeLayout;
    private int timeTextWidth;
    private int timeWidth;
    private int timeWidthAudio;
    private int timeX;
    private StaticLayout titleLayout;
    private int titleX;
    private int totalHeight;
    private int totalVisibleBlocksCount;
    private ArrayList<LinkPath> urlPath = new ArrayList();
    private ArrayList<LinkPath> urlPathCache = new ArrayList();
    private ArrayList<LinkPath> urlPathSelection = new ArrayList();
    private boolean useSeekBarWaweform;
    private int viaNameWidth;
    private int viaWidth;
    private StaticLayout videoInfoLayout;
    private StaticLayout viewsLayout;
    private int viewsTextWidth;
    private boolean wasLayout;
    private int widthForButtons;

    private class BotButton {
        private int angle;
        private KeyboardButton button;
        private int height;
        private long lastUpdateTime;
        private float progressAlpha;
        private StaticLayout title;
        private int width;
        private int x;
        private int y;

        private BotButton() {
        }
    }

    public interface ChatMessageCellDelegate {
        boolean canPerformActions();

        void didLongPressed(ChatMessageCell chatMessageCell);

        void didPressedBotButton(ChatMessageCell chatMessageCell, KeyboardButton keyboardButton);

        void didPressedCancelSendButton(ChatMessageCell chatMessageCell);

        void didPressedChannelAvatar(ChatMessageCell chatMessageCell, Chat chat, int i);

        void didPressedImage(ChatMessageCell chatMessageCell);

        void didPressedOther(ChatMessageCell chatMessageCell);

        void didPressedReplyMessage(ChatMessageCell chatMessageCell, int i);

        void didPressedShare(ChatMessageCell chatMessageCell);

        void didPressedUrl(MessageObject messageObject, ClickableSpan clickableSpan, boolean z);

        void didPressedUserAvatar(ChatMessageCell chatMessageCell, User user);

        void didPressedViaBot(ChatMessageCell chatMessageCell, String str);

        void needOpenWebView(String str, String str2, String str3, String str4, int i, int i2);

        boolean needPlayAudio(MessageObject messageObject);
    }

    public ChatMessageCell(Context context) {
        super(context);
        if (infoPaint == null) {
            infoPaint = new TextPaint(1);
            docNamePaint = new TextPaint(1);
            docNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            docBackPaint = new Paint(1);
            deleteProgressPaint = new Paint(1);
            deleteProgressPaint.setColor(Theme.MSG_SECRET_TIME_TEXT_COLOR);
            botProgressPaint = new Paint(1);
            botProgressPaint.setColor(-1);
            botProgressPaint.setStrokeCap(Cap.ROUND);
            botProgressPaint.setStyle(Style.STROKE);
            locationTitlePaint = new TextPaint(1);
            locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            locationAddressPaint = new TextPaint(1);
            urlPaint = new Paint();
            urlPaint.setColor(Theme.MSG_LINK_SELECT_BACKGROUND_COLOR);
            urlSelectionPaint = new Paint();
            urlSelectionPaint.setColor(Theme.MSG_TEXT_SELECT_BACKGROUND_COLOR);
            audioTimePaint = new TextPaint(1);
            audioTitlePaint = new TextPaint(1);
            audioTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            audioPerformerPaint = new TextPaint(1);
            botButtonPaint = new TextPaint(1);
            botButtonPaint.setColor(-1);
            botButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            contactNamePaint = new TextPaint(1);
            contactNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            contactPhonePaint = new TextPaint(1);
            durationPaint = new TextPaint(1);
            durationPaint.setColor(-1);
            gamePaint = new TextPaint(1);
            gamePaint.setColor(-1);
            gamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            timePaint = new TextPaint(1);
            namePaint = new TextPaint(1);
            namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            forwardNamePaint = new TextPaint(1);
            replyNamePaint = new TextPaint(1);
            replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            replyTextPaint = new TextPaint(1);
            replyTextPaint.linkColor = Theme.MSG_LINK_TEXT_COLOR;
            replyLinePaint = new Paint();
        }
        botProgressPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
        infoPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
        docNamePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        locationTitlePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        locationAddressPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
        audioTimePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
        audioTitlePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        audioPerformerPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        botButtonPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        contactNamePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        contactPhonePaint.setTextSize((float) AndroidUtilities.dp(13.0f));
        durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
        timePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
        namePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        forwardNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        replyNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        replyTextPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        gamePaint.setTextSize((float) AndroidUtilities.dp(13.0f));
        this.avatarImage = new ImageReceiver(this);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarDrawable = new AvatarDrawable();
        this.replyImageReceiver = new ImageReceiver(this);
        this.TAG = MediaController.getInstance().generateObserverTag();
        this.contactAvatarDrawable = new AvatarDrawable();
        this.photoImage = new ImageReceiver(this);
        this.photoImage.setDelegate(this);
        this.radialProgress = new RadialProgress(this);
        this.seekBar = new SeekBar(context);
        this.seekBar.setDelegate(this);
        this.seekBarWaveform = new SeekBarWaveform(context);
        this.seekBarWaveform.setDelegate(this);
        this.seekBarWaveform.setParentView(this);
        this.radialProgress = new RadialProgress(this);
    }

    private void resetPressedLink(int type) {
        if (this.pressedLink == null) {
            return;
        }
        if (this.pressedLinkType == type || type == -1) {
            resetUrlPaths(false);
            this.pressedLink = null;
            this.pressedLinkType = -1;
            invalidate();
        }
    }

    private void resetUrlPaths(boolean text) {
        if (text) {
            if (!this.urlPathSelection.isEmpty()) {
                this.urlPathCache.addAll(this.urlPathSelection);
                this.urlPathSelection.clear();
            }
        } else if (!this.urlPath.isEmpty()) {
            this.urlPathCache.addAll(this.urlPath);
            this.urlPath.clear();
        }
    }

    private LinkPath obtainNewUrlPath(boolean text) {
        LinkPath linkPath;
        if (this.urlPathCache.isEmpty()) {
            linkPath = new LinkPath();
        } else {
            linkPath = (LinkPath) this.urlPathCache.get(0);
            this.urlPathCache.remove(0);
        }
        if (text) {
            this.urlPathSelection.add(linkPath);
        } else {
            this.urlPath.add(linkPath);
        }
        return linkPath;
    }

    private boolean checkTextBlockMotionEvent(MotionEvent event) {
        if (this.currentMessageObject.type != 0 || this.currentMessageObject.textLayoutBlocks == null || this.currentMessageObject.textLayoutBlocks.isEmpty() || !(this.currentMessageObject.messageText instanceof Spannable)) {
            return false;
        }
        if (event.getAction() == 0 || (event.getAction() == 1 && this.pressedLinkType == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (x < this.textX || y < this.textY || x > this.textX + this.currentMessageObject.textWidth || y > this.textY + this.currentMessageObject.textHeight) {
                resetPressedLink(1);
            } else {
                y -= this.textY;
                int blockNum = 0;
                int a = 0;
                while (a < this.currentMessageObject.textLayoutBlocks.size() && ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a)).textYOffset <= ((float) y)) {
                    blockNum = a;
                    a++;
                }
                try {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(blockNum);
                    x -= this.textX - ((int) Math.ceil((double) block.textXOffset));
                    int line = block.textLayout.getLineForVertical((int) (((float) y) - block.textYOffset));
                    int off = block.textLayout.getOffsetForHorizontal(line, (float) x) + block.charactersOffset;
                    float left = block.textLayout.getLineLeft(line);
                    if (left <= ((float) x) && block.textLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentMessageObject.messageText;
                        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            if (event.getAction() == 0) {
                                this.pressedLink = link[0];
                                this.linkBlockNum = blockNum;
                                this.pressedLinkType = 1;
                                resetUrlPaths(false);
                                try {
                                    TextLayoutBlock nextBlock;
                                    ClickableSpan[] nextLink;
                                    Path path = obtainNewUrlPath(false);
                                    int start = buffer.getSpanStart(this.pressedLink) - block.charactersOffset;
                                    int end = buffer.getSpanEnd(this.pressedLink);
                                    int length = block.textLayout.getText().length();
                                    path.setCurrentLayout(block.textLayout, start, 0.0f);
                                    block.textLayout.getSelectionPath(start, end - block.charactersOffset, path);
                                    if (end >= block.charactersOffset + length) {
                                        a = blockNum + 1;
                                        while (a < this.currentMessageObject.textLayoutBlocks.size()) {
                                            nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                            length = nextBlock.textLayout.getText().length();
                                            nextLink = (ClickableSpan[]) buffer.getSpans(nextBlock.charactersOffset, nextBlock.charactersOffset, ClickableSpan.class);
                                            if (nextLink != null && nextLink.length != 0 && nextLink[0] == this.pressedLink) {
                                                path = obtainNewUrlPath(false);
                                                path.setCurrentLayout(nextBlock.textLayout, 0, (float) nextBlock.height);
                                                nextBlock.textLayout.getSelectionPath(0, end - nextBlock.charactersOffset, path);
                                                if (end < (block.charactersOffset + length) - 1) {
                                                    break;
                                                }
                                                a++;
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                    if (start < 0) {
                                        a = blockNum - 1;
                                        while (a >= 0) {
                                            nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                            length = nextBlock.textLayout.getText().length();
                                            nextLink = (ClickableSpan[]) buffer.getSpans((nextBlock.charactersOffset + length) - 1, (nextBlock.charactersOffset + length) - 1, ClickableSpan.class);
                                            if (nextLink != null && nextLink.length != 0) {
                                                if (nextLink[0] == this.pressedLink) {
                                                    path = obtainNewUrlPath(false);
                                                    start = buffer.getSpanStart(this.pressedLink) - nextBlock.charactersOffset;
                                                    path.setCurrentLayout(nextBlock.textLayout, start, (float) (-nextBlock.height));
                                                    nextBlock.textLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink) - nextBlock.charactersOffset, path);
                                                    if (start < 0) {
                                                        a--;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable e) {
                                    FileLog.e("tmessages", e);
                                }
                                invalidate();
                                return true;
                            }
                            if (link[0] == this.pressedLink) {
                                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
                                resetPressedLink(1);
                                return true;
                            }
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
            }
        }
        return false;
    }

    private boolean checkCaptionMotionEvent(MotionEvent event) {
        if (!(this.currentMessageObject.caption instanceof Spannable) || this.captionLayout == null) {
            return false;
        }
        if (event.getAction() == 0 || ((this.linkPreviewPressed || this.pressedLink != null) && event.getAction() == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (x < this.captionX || x > this.captionX + this.backgroundWidth || y < this.captionY || y > this.captionY + this.captionHeight) {
                resetPressedLink(3);
            } else if (event.getAction() == 0) {
                try {
                    x -= this.captionX;
                    int line = this.captionLayout.getLineForVertical(y - this.captionY);
                    int off = this.captionLayout.getOffsetForHorizontal(line, (float) x);
                    float left = this.captionLayout.getLineLeft(line);
                    if (left <= ((float) x) && this.captionLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentMessageObject.caption;
                        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            this.pressedLink = link[0];
                            this.pressedLinkType = 3;
                            resetUrlPaths(false);
                            try {
                                LinkPath path = obtainNewUrlPath(false);
                                int start = buffer.getSpanStart(this.pressedLink);
                                path.setCurrentLayout(this.captionLayout, start, 0.0f);
                                this.captionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                            } catch (Throwable e) {
                                FileLog.e("tmessages", e);
                            }
                            invalidate();
                            return true;
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
            } else if (this.pressedLinkType == 3) {
                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
                resetPressedLink(3);
                return true;
            }
        }
        return false;
    }

    private boolean checkGameMotionEvent(MotionEvent event) {
        if (!this.hasGamePreview) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            if (this.drawPhotoImage && this.photoImage.isInsideImage((float) x, (float) y)) {
                this.gamePreviewPressed = true;
                return true;
            } else if (this.descriptionLayout != null && y >= this.descriptionY) {
                try {
                    x -= (this.textX + AndroidUtilities.dp(10.0f)) + this.descriptionX;
                    int line = this.descriptionLayout.getLineForVertical(y - this.descriptionY);
                    int off = this.descriptionLayout.getOffsetForHorizontal(line, (float) x);
                    float left = this.descriptionLayout.getLineLeft(line);
                    if (left <= ((float) x) && this.descriptionLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentMessageObject.linkDescription;
                        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            this.pressedLink = link[0];
                            this.linkBlockNum = -10;
                            this.pressedLinkType = 2;
                            resetUrlPaths(false);
                            try {
                                LinkPath path = obtainNewUrlPath(false);
                                int start = buffer.getSpanStart(this.pressedLink);
                                path.setCurrentLayout(this.descriptionLayout, start, 0.0f);
                                this.descriptionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                            } catch (Throwable e) {
                                FileLog.e("tmessages", e);
                            }
                            invalidate();
                            return true;
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
            }
        } else if (event.getAction() == 1) {
            if (this.pressedLinkType != 2 && !this.gamePreviewPressed) {
                resetPressedLink(2);
            } else if (this.pressedLink != null) {
                if (this.pressedLink instanceof URLSpan) {
                    Browser.openUrl(getContext(), ((URLSpan) this.pressedLink).getURL());
                } else {
                    this.pressedLink.onClick(this);
                }
                resetPressedLink(2);
            } else {
                this.gamePreviewPressed = false;
                for (int a = 0; a < this.botButtons.size(); a++) {
                    BotButton button = (BotButton) this.botButtons.get(a);
                    if (button.button instanceof TL_keyboardButtonGame) {
                        playSoundEffect(0);
                        this.delegate.didPressedBotButton(this, button.button);
                        invalidate();
                        break;
                    }
                }
                resetPressedLink(2);
                return true;
            }
        }
        return false;
    }

    private boolean checkLinkPreviewMotionEvent(MotionEvent event) {
        if (this.currentMessageObject.type != 0 || !this.hasLinkPreview) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x >= this.textX && x <= this.textX + this.backgroundWidth && y >= this.textY + this.currentMessageObject.textHeight && y <= ((this.textY + this.currentMessageObject.textHeight) + this.linkPreviewHeight) + AndroidUtilities.dp(8.0f)) {
            WebPage webPage;
            if (event.getAction() == 0) {
                if (this.documentAttachType != 1 && this.drawPhotoImage && this.photoImage.isInsideImage((float) x, (float) y)) {
                    if (!this.drawImageButton || this.buttonState == -1 || x < this.buttonX || x > this.buttonX + AndroidUtilities.dp(48.0f) || y < this.buttonY || y > this.buttonY + AndroidUtilities.dp(48.0f)) {
                        this.linkPreviewPressed = true;
                        webPage = this.currentMessageObject.messageOwner.media.webpage;
                        if (this.documentAttachType != 2 || this.buttonState != -1 || !MediaController.getInstance().canAutoplayGifs() || (this.photoImage.getAnimation() != null && TextUtils.isEmpty(webPage.embed_url))) {
                            return true;
                        }
                        this.linkPreviewPressed = false;
                        return false;
                    }
                    this.buttonPressed = 1;
                    return true;
                } else if (this.descriptionLayout != null && y >= this.descriptionY) {
                    try {
                        x -= (this.textX + AndroidUtilities.dp(10.0f)) + this.descriptionX;
                        int line = this.descriptionLayout.getLineForVertical(y - this.descriptionY);
                        int off = this.descriptionLayout.getOffsetForHorizontal(line, (float) x);
                        float left = this.descriptionLayout.getLineLeft(line);
                        if (left <= ((float) x) && this.descriptionLayout.getLineWidth(line) + left >= ((float) x)) {
                            Spannable buffer = this.currentMessageObject.linkDescription;
                            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            boolean ignore = false;
                            if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                                ignore = true;
                            }
                            if (!ignore) {
                                this.pressedLink = link[0];
                                this.linkBlockNum = -10;
                                this.pressedLinkType = 2;
                                resetUrlPaths(false);
                                try {
                                    LinkPath path = obtainNewUrlPath(false);
                                    int start = buffer.getSpanStart(this.pressedLink);
                                    path.setCurrentLayout(this.descriptionLayout, start, 0.0f);
                                    this.descriptionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                                } catch (Throwable e) {
                                    FileLog.e("tmessages", e);
                                }
                                invalidate();
                                return true;
                            }
                        }
                    } catch (Throwable e2) {
                        FileLog.e("tmessages", e2);
                    }
                }
            } else if (event.getAction() == 1) {
                if (this.pressedLinkType != 2 && this.buttonPressed == 0 && !this.linkPreviewPressed) {
                    resetPressedLink(2);
                } else if (this.buttonPressed != 0) {
                    if (event.getAction() == 1) {
                        this.buttonPressed = 0;
                        playSoundEffect(0);
                        didPressedButton(false);
                        invalidate();
                    }
                } else if (this.pressedLink != null) {
                    if (this.pressedLink instanceof URLSpan) {
                        Browser.openUrl(getContext(), ((URLSpan) this.pressedLink).getURL());
                    } else {
                        this.pressedLink.onClick(this);
                    }
                    resetPressedLink(2);
                } else {
                    if (this.documentAttachType != 2 || !this.drawImageButton) {
                        webPage = this.currentMessageObject.messageOwner.media.webpage;
                        if (webPage != null && VERSION.SDK_INT >= 16 && !TextUtils.isEmpty(webPage.embed_url)) {
                            this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.description, webPage.url, webPage.embed_width, webPage.embed_height);
                        } else if (this.buttonState == -1) {
                            this.delegate.didPressedImage(this);
                            playSoundEffect(0);
                        } else if (webPage != null) {
                            Browser.openUrl(getContext(), webPage.url);
                        }
                    } else if (this.buttonState == -1) {
                        if (MediaController.getInstance().canAutoplayGifs()) {
                            this.delegate.didPressedImage(this);
                        } else {
                            this.buttonState = 2;
                            this.currentMessageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                            this.photoImage.setAllowStartAnimation(false);
                            this.photoImage.stopAnimation();
                            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                            invalidate();
                            playSoundEffect(0);
                        }
                    } else if (this.buttonState == 2 || this.buttonState == 0) {
                        didPressedButton(false);
                        playSoundEffect(0);
                    }
                    resetPressedLink(2);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkOtherButtonMotionEvent(MotionEvent event) {
        if ((this.documentAttachType != 1 && this.currentMessageObject.type != 12 && this.documentAttachType != 5 && this.documentAttachType != 4 && this.documentAttachType != 2 && this.currentMessageObject.type != 8) || this.hasGamePreview) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            if (x < this.otherX - AndroidUtilities.dp(20.0f) || x > this.otherX + AndroidUtilities.dp(20.0f) || y < this.otherY - AndroidUtilities.dp(4.0f) || y > this.otherY + AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE)) {
                return false;
            }
            this.otherPressed = true;
            return true;
        } else if (event.getAction() != 1 || !this.otherPressed) {
            return false;
        } else {
            this.otherPressed = false;
            playSoundEffect(0);
            this.delegate.didPressedOther(this);
            return false;
        }
    }

    private boolean checkPhotoImageMotionEvent(MotionEvent event) {
        if (!this.drawPhotoImage && this.documentAttachType != 1) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean result = false;
        if (event.getAction() == 0) {
            if (this.buttonState != -1 && x >= this.buttonX && x <= this.buttonX + AndroidUtilities.dp(48.0f) && y >= this.buttonY && y <= this.buttonY + AndroidUtilities.dp(48.0f)) {
                this.buttonPressed = 1;
                invalidate();
                result = true;
            } else if (this.documentAttachType == 1) {
                if (x >= this.photoImage.getImageX() && x <= (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f) && y >= this.photoImage.getImageY() && y <= this.photoImage.getImageY() + this.photoImage.getImageHeight()) {
                    this.imagePressed = true;
                    result = true;
                }
            } else if (!(this.currentMessageObject.type == 13 && this.currentMessageObject.getInputStickerSet() == null)) {
                if (x >= this.photoImage.getImageX() && x <= this.photoImage.getImageX() + this.backgroundWidth && y >= this.photoImage.getImageY() && y <= this.photoImage.getImageY() + this.photoImage.getImageHeight()) {
                    this.imagePressed = true;
                    result = true;
                }
                if (this.currentMessageObject.type == 12 && MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)) == null) {
                    this.imagePressed = false;
                    result = false;
                }
            }
            if (!this.imagePressed) {
                return result;
            }
            if (this.currentMessageObject.isSecretPhoto()) {
                this.imagePressed = false;
                return result;
            } else if (this.currentMessageObject.isSendError()) {
                this.imagePressed = false;
                return false;
            } else if (this.currentMessageObject.type != 8 || this.buttonState != -1 || !MediaController.getInstance().canAutoplayGifs() || this.photoImage.getAnimation() != null) {
                return result;
            } else {
                this.imagePressed = false;
                return false;
            }
        } else if (event.getAction() != 1) {
            return false;
        } else {
            if (this.buttonPressed == 1) {
                this.buttonPressed = 0;
                playSoundEffect(0);
                didPressedButton(false);
                this.radialProgress.swapBackground(getDrawableForCurrentState());
                invalidate();
                return false;
            } else if (!this.imagePressed) {
                return false;
            } else {
                this.imagePressed = false;
                if (this.buttonState == -1 || this.buttonState == 2 || this.buttonState == 3) {
                    playSoundEffect(0);
                    didClickedImage();
                } else if (this.buttonState == 0 && this.documentAttachType == 1) {
                    playSoundEffect(0);
                    didPressedButton(false);
                }
                invalidate();
                return false;
            }
        }
    }

    private boolean checkAudioMotionEvent(MotionEvent event) {
        if (this.documentAttachType != 3 && this.documentAttachType != 5) {
            return false;
        }
        boolean result;
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (this.useSeekBarWaweform) {
            result = this.seekBarWaveform.onTouch(event.getAction(), (event.getX() - ((float) this.seekBarX)) - ((float) AndroidUtilities.dp(13.0f)), event.getY() - ((float) this.seekBarY));
        } else {
            result = this.seekBar.onTouch(event.getAction(), event.getX() - ((float) this.seekBarX), event.getY() - ((float) this.seekBarY));
        }
        if (result) {
            if (!this.useSeekBarWaweform && event.getAction() == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
            } else if (this.useSeekBarWaweform && !this.seekBarWaveform.isStartDraging() && event.getAction() == 1) {
                didPressedButton(true);
            }
            this.disallowLongPress = true;
            invalidate();
            return result;
        }
        int side = AndroidUtilities.dp(36.0f);
        boolean area = (this.buttonState == 0 || this.buttonState == 1 || this.buttonState == 2) ? x >= this.buttonX - AndroidUtilities.dp(12.0f) && x <= (this.buttonX - AndroidUtilities.dp(12.0f)) + this.backgroundWidth && y >= this.namesOffset + this.mediaOffsetY && y <= this.layoutHeight : x >= this.buttonX && x <= this.buttonX + side && y >= this.buttonY && y <= this.buttonY + side;
        if (event.getAction() == 0) {
            if (!area) {
                return result;
            }
            this.buttonPressed = 1;
            invalidate();
            this.radialProgress.swapBackground(getDrawableForCurrentState());
            return true;
        } else if (this.buttonPressed == 0) {
            return result;
        } else {
            if (event.getAction() == 1) {
                this.buttonPressed = 0;
                playSoundEffect(0);
                didPressedButton(true);
                invalidate();
            } else if (event.getAction() == 3) {
                this.buttonPressed = 0;
                invalidate();
            } else if (event.getAction() == 2 && !area) {
                this.buttonPressed = 0;
                invalidate();
            }
            this.radialProgress.swapBackground(getDrawableForCurrentState());
            return result;
        }
    }

    private boolean checkBotButtonMotionEvent(MotionEvent event) {
        if (this.botButtons.isEmpty()) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            int addX;
            if (this.currentMessageObject.isOutOwner()) {
                addX = (getMeasuredWidth() - this.widthForButtons) - AndroidUtilities.dp(10.0f);
            } else {
                addX = this.backgroundDrawableLeft + AndroidUtilities.dp(this.mediaBackground ? DefaultRetryPolicy.DEFAULT_BACKOFF_MULT : 7.0f);
            }
            int a = 0;
            while (a < this.botButtons.size()) {
                BotButton button = (BotButton) this.botButtons.get(a);
                int y2 = (button.y + this.layoutHeight) - AndroidUtilities.dp(2.0f);
                if (x < button.x + addX || x > (button.x + addX) + button.width || y < y2 || y > button.height + y2) {
                    a++;
                } else {
                    this.pressedBotButton = a;
                    invalidate();
                    return true;
                }
            }
            return false;
        } else if (event.getAction() != 1 || this.pressedBotButton == -1) {
            return false;
        } else {
            playSoundEffect(0);
            this.delegate.didPressedBotButton(this, ((BotButton) this.botButtons.get(this.pressedBotButton)).button);
            this.pressedBotButton = -1;
            invalidate();
            return false;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.currentMessageObject == null || !this.delegate.canPerformActions()) {
            return super.onTouchEvent(event);
        }
        this.disallowLongPress = false;
        boolean result = checkTextBlockMotionEvent(event);
        if (!result) {
            result = checkOtherButtonMotionEvent(event);
        }
        if (!result) {
            result = checkLinkPreviewMotionEvent(event);
        }
        if (!result) {
            result = checkGameMotionEvent(event);
        }
        if (!result) {
            result = checkCaptionMotionEvent(event);
        }
        if (!result) {
            result = checkAudioMotionEvent(event);
        }
        if (!result) {
            result = checkPhotoImageMotionEvent(event);
        }
        if (!result) {
            result = checkBotButtonMotionEvent(event);
        }
        if (event.getAction() == 3) {
            this.buttonPressed = 0;
            this.pressedBotButton = -1;
            this.linkPreviewPressed = false;
            this.otherPressed = false;
            this.imagePressed = false;
            result = false;
            resetPressedLink(-1);
        }
        if (!this.disallowLongPress && result && event.getAction() == 0) {
            startCheckLongPress();
        }
        if (!(event.getAction() == 0 || event.getAction() == 2)) {
            cancelCheckLongPress();
        }
        if (result) {
            return result;
        }
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.avatarPressed) {
                if (event.getAction() == 1) {
                    this.avatarPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    if (this.currentUser != null) {
                        this.delegate.didPressedUserAvatar(this, this.currentUser);
                        return result;
                    } else if (this.currentChat == null) {
                        return result;
                    } else {
                        this.delegate.didPressedChannelAvatar(this, this.currentChat, 0);
                        return result;
                    }
                } else if (event.getAction() == 3) {
                    this.avatarPressed = false;
                    return result;
                } else if (event.getAction() != 2 || !this.isAvatarVisible || this.avatarImage.isInsideImage(x, y)) {
                    return result;
                } else {
                    this.avatarPressed = false;
                    return result;
                }
            } else if (this.forwardNamePressed) {
                if (event.getAction() == 1) {
                    this.forwardNamePressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    if (this.currentForwardChannel != null) {
                        this.delegate.didPressedChannelAvatar(this, this.currentForwardChannel, this.currentMessageObject.messageOwner.fwd_from.channel_post);
                        return result;
                    } else if (this.currentForwardUser == null) {
                        return result;
                    } else {
                        this.delegate.didPressedUserAvatar(this, this.currentForwardUser);
                        return result;
                    }
                } else if (event.getAction() == 3) {
                    this.forwardNamePressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                        return result;
                    }
                    this.forwardNamePressed = false;
                    return result;
                }
            } else if (this.forwardBotPressed) {
                if (event.getAction() == 1) {
                    this.forwardBotPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    this.delegate.didPressedViaBot(this, this.currentViaBotUser != null ? this.currentViaBotUser.username : this.currentMessageObject.messageOwner.via_bot_name);
                    return result;
                } else if (event.getAction() == 3) {
                    this.forwardBotPressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (!this.drawForwardedName || this.forwardedNameLayout[0] == null) {
                        if (x >= this.nameX + ((float) this.viaNameWidth) && x <= (this.nameX + ((float) this.viaNameWidth)) + ((float) this.viaWidth) && y >= this.nameY - ((float) AndroidUtilities.dp(4.0f)) && y <= this.nameY + ((float) AndroidUtilities.dp(20.0f))) {
                            return result;
                        }
                        this.forwardBotPressed = false;
                        return result;
                    } else if (x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                        return result;
                    } else {
                        this.forwardBotPressed = false;
                        return result;
                    }
                }
            } else if (this.replyPressed) {
                if (event.getAction() == 1) {
                    this.replyPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    this.delegate.didPressedReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
                    return result;
                } else if (event.getAction() == 3) {
                    this.replyPressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (x >= ((float) this.replyStartX) && x <= ((float) (this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth))) && y >= ((float) this.replyStartY) && y <= ((float) (this.replyStartY + AndroidUtilities.dp(35.0f)))) {
                        return result;
                    }
                    this.replyPressed = false;
                    return result;
                }
            } else if (!this.sharePressed) {
                return result;
            } else {
                if (event.getAction() == 1) {
                    this.sharePressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        this.delegate.didPressedShare(this);
                    }
                } else if (event.getAction() == 3) {
                    this.sharePressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.shareStartX) || x > ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) || y < ((float) this.shareStartY) || y > ((float) (this.shareStartY + AndroidUtilities.dp(32.0f))))) {
                    this.sharePressed = false;
                }
                invalidate();
                return result;
            }
        } else if (this.delegate != null && !this.delegate.canPerformActions()) {
            return result;
        } else {
            if (this.isAvatarVisible && this.avatarImage.isInsideImage(x, y)) {
                this.avatarPressed = true;
                result = true;
            } else if (this.drawForwardedName && this.forwardedNameLayout[0] != null && x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                if (this.viaWidth == 0 || x < ((float) ((this.forwardNameX + this.viaNameWidth) + AndroidUtilities.dp(4.0f)))) {
                    this.forwardNamePressed = true;
                } else {
                    this.forwardBotPressed = true;
                }
                result = true;
            } else if (this.drawNameLayout && this.nameLayout != null && this.viaWidth != 0 && x >= this.nameX + ((float) this.viaNameWidth) && x <= (this.nameX + ((float) this.viaNameWidth)) + ((float) this.viaWidth) && y >= this.nameY - ((float) AndroidUtilities.dp(4.0f)) && y <= this.nameY + ((float) AndroidUtilities.dp(20.0f))) {
                this.forwardBotPressed = true;
                result = true;
            } else if (this.currentMessageObject.isReply() && x >= ((float) this.replyStartX) && x <= ((float) (this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth))) && y >= ((float) this.replyStartY) && y <= ((float) (this.replyStartY + AndroidUtilities.dp(35.0f)))) {
                this.replyPressed = true;
                result = true;
            } else if (this.drawShareButton && x >= ((float) this.shareStartX) && x <= ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) && y >= ((float) this.shareStartY) && y <= ((float) (this.shareStartY + AndroidUtilities.dp(32.0f)))) {
                this.sharePressed = true;
                result = true;
                invalidate();
            }
            if (!result) {
                return result;
            }
            startCheckLongPress();
            return result;
        }
    }

    public void updateAudioProgress() {
        if (this.currentMessageObject != null && this.documentAttach != null) {
            if (this.useSeekBarWaweform) {
                if (!this.seekBarWaveform.isDragging()) {
                    this.seekBarWaveform.setProgress(this.currentMessageObject.audioProgress);
                }
            } else if (!this.seekBar.isDragging()) {
                this.seekBar.setProgress(this.currentMessageObject.audioProgress);
            }
            int duration = 0;
            int a;
            DocumentAttribute attribute;
            String timeString;
            if (this.documentAttachType == 3) {
                if (MediaController.getInstance().isPlayingAudio(this.currentMessageObject)) {
                    duration = this.currentMessageObject.audioProgressSec;
                } else {
                    for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                        attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                        if (attribute instanceof TL_documentAttributeAudio) {
                            duration = attribute.duration;
                            break;
                        }
                    }
                }
                timeString = String.format("%02d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
                if (this.lastTimeString == null || !(this.lastTimeString == null || this.lastTimeString.equals(timeString))) {
                    this.lastTimeString = timeString;
                    this.timeWidthAudio = (int) Math.ceil((double) audioTimePaint.measureText(timeString));
                    this.durationLayout = new StaticLayout(timeString, audioTimePaint, this.timeWidthAudio, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                }
            } else {
                int currentProgress = 0;
                for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                    attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                    if (attribute instanceof TL_documentAttributeAudio) {
                        duration = attribute.duration;
                        break;
                    }
                }
                if (MediaController.getInstance().isPlayingAudio(this.currentMessageObject)) {
                    currentProgress = this.currentMessageObject.audioProgressSec;
                }
                timeString = String.format("%d:%02d / %d:%02d", new Object[]{Integer.valueOf(currentProgress / 60), Integer.valueOf(currentProgress % 60), Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
                if (this.lastTimeString == null || !(this.lastTimeString == null || this.lastTimeString.equals(timeString))) {
                    this.lastTimeString = timeString;
                    this.durationLayout = new StaticLayout(timeString, audioTimePaint, (int) Math.ceil((double) audioTimePaint.measureText(timeString)), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                }
            }
            invalidate();
        }
    }

    public void downloadAudioIfNeed() {
        if (this.documentAttachType == 3 && this.documentAttach.size < 1048576 && this.buttonState == 2) {
            FileLoader.getInstance().loadFile(this.documentAttach, true, false);
            this.buttonState = 4;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
        }
    }

    public void setVisiblePart(int position, int height) {
        if (this.currentMessageObject != null && this.currentMessageObject.textLayoutBlocks != null) {
            position -= this.textY;
            int newFirst = -1;
            int newLast = -1;
            int newCount = 0;
            int startBlock = 0;
            int a = 0;
            while (a < this.currentMessageObject.textLayoutBlocks.size() && ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a)).textYOffset <= ((float) position)) {
                startBlock = a;
                a++;
            }
            for (a = startBlock; a < this.currentMessageObject.textLayoutBlocks.size(); a++) {
                TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                float y = block.textYOffset;
                if (intersect(y, ((float) block.height) + y, (float) position, (float) (position + height))) {
                    if (newFirst == -1) {
                        newFirst = a;
                    }
                    newLast = a;
                    newCount++;
                } else if (y > ((float) position)) {
                    break;
                }
            }
            if (this.lastVisibleBlockNum != newLast || this.firstVisibleBlockNum != newFirst || this.totalVisibleBlocksCount != newCount) {
                this.lastVisibleBlockNum = newLast;
                this.firstVisibleBlockNum = newFirst;
                this.totalVisibleBlocksCount = newCount;
                invalidate();
            }
        }
    }

    private boolean intersect(float left1, float right1, float left2, float right2) {
        if (left1 <= left2) {
            if (right1 >= left2) {
                return true;
            }
            return false;
        } else if (left1 > right2) {
            return false;
        } else {
            return true;
        }
    }

    public static StaticLayout generateStaticLayout(CharSequence text, TextPaint paint, int maxWidth, int smallWidth, int linesCount, int maxLines) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
        int addedChars = 0;
        StaticLayout layout = new StaticLayout(text, paint, smallWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
        int a = 0;
        while (a < linesCount) {
            Directions directions = layout.getLineDirections(a);
            if (layout.getLineLeft(a) != 0.0f || layout.isRtlCharAt(layout.getLineStart(a)) || layout.isRtlCharAt(layout.getLineEnd(a))) {
                maxWidth = smallWidth;
            }
            int pos = layout.getLineEnd(a);
            if (pos != text.length()) {
                pos--;
                if (stringBuilder.charAt(pos + addedChars) == ' ') {
                    stringBuilder.replace(pos + addedChars, (pos + addedChars) + 1, "\n");
                } else {
                    if (stringBuilder.charAt(pos + addedChars) != '\n') {
                        stringBuilder.insert(pos + addedChars, "\n");
                        addedChars++;
                    }
                }
                if (a == layout.getLineCount() - 1 || a == maxLines - 1) {
                    break;
                }
                a++;
            } else {
                break;
            }
        }
        return StaticLayoutEx.createStaticLayout(stringBuilder, paint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, (float) AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT), false, TruncateAt.END, maxWidth, maxLines);
    }

    private void didClickedImage() {
        if (this.currentMessageObject.type == 1 || this.currentMessageObject.type == 13) {
            if (this.buttonState == -1) {
                this.delegate.didPressedImage(this);
            } else if (this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 12) {
            this.delegate.didPressedUserAvatar(this, MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)));
        } else if (this.currentMessageObject.type == 8) {
            if (this.buttonState == -1) {
                if (MediaController.getInstance().canAutoplayGifs()) {
                    this.delegate.didPressedImage(this);
                    return;
                }
                this.buttonState = 2;
                this.currentMessageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                this.photoImage.setAllowStartAnimation(false);
                this.photoImage.stopAnimation();
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            } else if (this.buttonState == 2 || this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.documentAttachType == 4) {
            if (this.buttonState == 0 || this.buttonState == 3) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 4) {
            this.delegate.didPressedImage(this);
        } else if (this.documentAttachType == 1) {
            if (this.buttonState == -1) {
                this.delegate.didPressedImage(this);
            }
        } else if (this.documentAttachType == 2 && this.buttonState == -1) {
            WebPage webPage = this.currentMessageObject.messageOwner.media.webpage;
            if (webPage == null) {
                return;
            }
            if (VERSION.SDK_INT < 16 || webPage.embed_url == null || webPage.embed_url.length() == 0) {
                Browser.openUrl(getContext(), webPage.url);
            } else {
                this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.description, webPage.url, webPage.embed_width, webPage.embed_height);
            }
        }
    }

    private void updateSecretTimeText(MessageObject messageObject) {
        if (messageObject != null && !messageObject.isOut()) {
            String str = messageObject.getSecretTimeString();
            if (str != null) {
                this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(str));
                this.infoLayout = new StaticLayout(TextUtils.ellipsize(str, infoPaint, (float) this.infoWidth, TruncateAt.END), infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                invalidate();
            }
        }
    }

    private boolean isPhotoDataChanged(MessageObject object) {
        if (object.type == 0 || object.type == 14) {
            return false;
        }
        if (object.type == 4) {
            if (this.currentUrl == null) {
                return true;
            }
            double lat = object.messageOwner.media.geo.lat;
            double lon = object.messageOwner.media.geo._long;
            if (!String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=100x100&maptype=roadmap&scale=%d&markers=color:red|size:mid|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)}).equals(this.currentUrl)) {
                return true;
            }
        } else if (this.currentPhotoObject == null || (this.currentPhotoObject.location instanceof TL_fileLocationUnavailable)) {
            return true;
        } else {
            if (this.currentMessageObject != null && this.photoNotSet && FileLoader.getPathToMessage(this.currentMessageObject.messageOwner).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserDataChanged() {
        boolean z = false;
        if (this.currentMessageObject != null && !this.hasLinkPreview && this.currentMessageObject.messageOwner.media != null && (this.currentMessageObject.messageOwner.media.webpage instanceof TL_webPage)) {
            return true;
        }
        if (this.currentMessageObject == null || (this.currentUser == null && this.currentChat == null)) {
            return false;
        }
        if (this.lastSendState != this.currentMessageObject.messageOwner.send_state || this.lastDeleteDate != this.currentMessageObject.messageOwner.destroyTime || this.lastViewsCount != this.currentMessageObject.messageOwner.views) {
            return true;
        }
        User newUser = null;
        Chat newChat = null;
        if (this.currentMessageObject.isFromUser()) {
            newUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
        } else if (this.currentMessageObject.messageOwner.from_id < 0) {
            newChat = MessagesController.getInstance().getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
        } else if (this.currentMessageObject.messageOwner.post) {
            newChat = MessagesController.getInstance().getChat(Integer.valueOf(this.currentMessageObject.messageOwner.to_id.channel_id));
        }
        FileLocation newPhoto = null;
        if (this.isAvatarVisible) {
            if (newUser != null && newUser.photo != null) {
                newPhoto = newUser.photo.photo_small;
            } else if (!(newChat == null || newChat.photo == null)) {
                newPhoto = newChat.photo.photo_small;
            }
        }
        if (this.replyTextLayout == null && this.currentMessageObject.replyMessageObject != null) {
            return true;
        }
        if (this.currentPhoto == null && newPhoto != null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto == null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto != null && (this.currentPhoto.local_id != newPhoto.local_id || this.currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }
        FileLocation newReplyPhoto = null;
        if (this.currentMessageObject.replyMessageObject != null) {
            PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.replyMessageObject.photoThumbs, 80);
            if (!(photoSize == null || this.currentMessageObject.replyMessageObject.type == 13)) {
                newReplyPhoto = photoSize.location;
            }
        }
        if (this.currentReplyPhoto == null && newReplyPhoto != null) {
            return true;
        }
        String newNameString = null;
        if (this.drawName && this.isChat && !this.currentMessageObject.isOutOwner()) {
            if (newUser != null) {
                newNameString = UserObject.getUserName(newUser);
            } else if (newChat != null) {
                newNameString = newChat.title;
            }
        }
        if (this.currentNameString == null && newNameString != null) {
            return true;
        }
        if (this.currentNameString != null && newNameString == null) {
            return true;
        }
        if (this.currentNameString != null && newNameString != null && !this.currentNameString.equals(newNameString)) {
            return true;
        }
        if (!this.drawForwardedName) {
            return false;
        }
        newNameString = this.currentMessageObject.getForwardedName();
        if ((this.currentForwardNameString == null && newNameString != null) || ((this.currentForwardNameString != null && newNameString == null) || !(this.currentForwardNameString == null || newNameString == null || this.currentForwardNameString.equals(newNameString)))) {
            z = true;
        }
        return z;
    }

    public ImageReceiver getPhotoImage() {
        return this.photoImage;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.avatarImage.onDetachedFromWindow();
        this.replyImageReceiver.onDetachedFromWindow();
        this.photoImage.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.avatarImage.onAttachedToWindow();
        this.replyImageReceiver.onAttachedToWindow();
        if (!this.drawPhotoImage) {
            updateButtonState(false);
        } else if (this.photoImage.onAttachedToWindow()) {
            updateButtonState(false);
        }
    }

    protected void onLongPress() {
        if (this.pressedLink instanceof URLSpanNoUnderline) {
            if (this.pressedLink.getURL().startsWith("/")) {
                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
                return;
            }
        } else if (this.pressedLink instanceof URLSpan) {
            this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
            return;
        }
        resetPressedLink(-1);
        if (!(this.buttonPressed == 0 && this.pressedBotButton == -1)) {
            this.buttonPressed = 0;
            this.pressedBotButton = -1;
            invalidate();
        }
        if (this.delegate != null) {
            this.delegate.didLongPressed(this);
        }
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        this.isCheckPressed = value;
        this.isPressed = pressed;
        this.radialProgress.swapBackground(getDrawableForCurrentState());
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectedBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectedBackground());
        }
        invalidate();
    }

    public void setHighlighted(boolean value) {
        if (this.isHighlighted != value) {
            this.isHighlighted = value;
            this.radialProgress.swapBackground(getDrawableForCurrentState());
            if (this.useSeekBarWaweform) {
                this.seekBarWaveform.setSelected(isDrawSelectedBackground());
            } else {
                this.seekBar.setSelected(isDrawSelectedBackground());
            }
            invalidate();
        }
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        this.radialProgress.swapBackground(getDrawableForCurrentState());
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectedBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectedBackground());
        }
        invalidate();
    }

    public void onSeekBarDrag(float progress) {
        if (this.currentMessageObject != null) {
            this.currentMessageObject.audioProgress = progress;
            MediaController.getInstance().seekToProgress(this.currentMessageObject, progress);
        }
    }

    private void updateWaveform() {
        if (this.currentMessageObject != null && this.documentAttachType == 3) {
            for (int a = 0; a < this.documentAttach.attributes.size(); a++) {
                DocumentAttribute attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    if (attribute.waveform == null || attribute.waveform.length == 0) {
                        MediaController.getInstance().generateWaveform(this.currentMessageObject);
                    }
                    this.useSeekBarWaweform = attribute.waveform != null;
                    this.seekBarWaveform.setWaveform(attribute.waveform);
                    return;
                }
            }
        }
    }

    private int createDocumentLayout(int maxWidth, MessageObject messageObject) {
        if (messageObject.type == 0) {
            this.documentAttach = messageObject.messageOwner.media.webpage.document;
        } else {
            this.documentAttach = messageObject.messageOwner.media.document;
        }
        if (this.documentAttach == null) {
            return 0;
        }
        int duration;
        int a;
        DocumentAttribute attribute;
        if (MessageObject.isVoiceDocument(this.documentAttach)) {
            this.documentAttachType = 3;
            duration = 0;
            for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    duration = attribute.duration;
                    break;
                }
            }
            this.availableTimeWidth = (maxWidth - AndroidUtilities.dp(94.0f)) - ((int) Math.ceil((double) audioTimePaint.measureText("00:00")));
            measureTime(messageObject);
            int minSize = AndroidUtilities.dp(174.0f) + this.timeWidth;
            if (!this.hasLinkPreview) {
                this.backgroundWidth = Math.min(maxWidth, (AndroidUtilities.dp(10.0f) * duration) + minSize);
            }
            if (messageObject.isOutOwner()) {
                this.seekBarWaveform.setColors(-4463700, -8863118, -5644906);
                this.seekBar.setColors(-4463700, -8863118, -5644906);
            } else {
                this.seekBarWaveform.setColors(Theme.MSG_IN_VOICE_SEEKBAR_COLOR, -9259544, -4399384);
                this.seekBar.setColors(Theme.MSG_IN_AUDIO_SEEKBAR_COLOR, -9259544, -4399384);
            }
            this.seekBarWaveform.setMessageObject(messageObject);
            return 0;
        } else if (MessageObject.isMusicDocument(this.documentAttach)) {
            this.documentAttachType = 5;
            if (messageObject.isOutOwner()) {
                this.seekBar.setColors(-4463700, -8863118, -5644906);
            } else {
                this.seekBar.setColors(Theme.MSG_IN_AUDIO_SEEKBAR_COLOR, -9259544, -4399384);
            }
            maxWidth -= AndroidUtilities.dp(86.0f);
            this.songLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicTitle().replace('\n', ' '), audioTitlePaint, (float) (maxWidth - AndroidUtilities.dp(12.0f)), TruncateAt.END), audioTitlePaint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            if (this.songLayout.getLineCount() > 0) {
                this.songX = -((int) Math.ceil((double) this.songLayout.getLineLeft(0)));
            }
            this.performerLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicAuthor().replace('\n', ' '), audioPerformerPaint, (float) maxWidth, TruncateAt.END), audioPerformerPaint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            if (this.performerLayout.getLineCount() > 0) {
                this.performerX = -((int) Math.ceil((double) this.performerLayout.getLineLeft(0)));
            }
            duration = 0;
            for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    duration = attribute.duration;
                    break;
                }
            }
            int durationWidth = (int) Math.ceil((double) audioTimePaint.measureText(String.format("%d:%02d / %d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60), Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)})));
            this.availableTimeWidth = (this.backgroundWidth - AndroidUtilities.dp(94.0f)) - durationWidth;
            return durationWidth;
        } else if (MessageObject.isVideoDocument(this.documentAttach)) {
            this.documentAttachType = 4;
            duration = 0;
            for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeVideo) {
                    duration = attribute.duration;
                    break;
                }
            }
            int seconds = duration - ((duration / 60) * 60);
            str = String.format("%d:%02d, %s", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds), AndroidUtilities.formatFileSize((long) this.documentAttach.size)});
            this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(str));
            this.infoLayout = new StaticLayout(str, infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            return 0;
        } else {
            int width;
            boolean z = (this.documentAttach.mime_type != null && this.documentAttach.mime_type.toLowerCase().startsWith("image/")) || ((this.documentAttach.thumb instanceof TL_photoSize) && !(this.documentAttach.thumb.location instanceof TL_fileLocationUnavailable));
            this.drawPhotoImage = z;
            if (!this.drawPhotoImage) {
                maxWidth += AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
            }
            this.documentAttachType = 1;
            String name = FileLoader.getDocumentFileName(this.documentAttach);
            if (name == null || name.length() == 0) {
                name = LocaleController.getString("AttachDocument", R.string.AttachDocument);
            }
            this.docTitleLayout = StaticLayoutEx.createStaticLayout(name, docNamePaint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false, TruncateAt.MIDDLE, maxWidth, this.drawPhotoImage ? 2 : 1);
            this.docTitleOffsetX = Integer.MIN_VALUE;
            if (this.docTitleLayout == null || this.docTitleLayout.getLineCount() <= 0) {
                width = maxWidth;
                this.docTitleOffsetX = 0;
            } else {
                int maxLineWidth = 0;
                for (a = 0; a < this.docTitleLayout.getLineCount(); a++) {
                    maxLineWidth = Math.max(maxLineWidth, (int) Math.ceil((double) this.docTitleLayout.getLineWidth(a)));
                    this.docTitleOffsetX = Math.max(this.docTitleOffsetX, (int) Math.ceil((double) (-this.docTitleLayout.getLineLeft(a))));
                }
                width = Math.min(maxWidth, maxLineWidth);
            }
            str = AndroidUtilities.formatFileSize((long) this.documentAttach.size) + " " + FileLoader.getDocumentExtension(this.documentAttach);
            this.infoWidth = Math.min(maxWidth - AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE), (int) Math.ceil((double) infoPaint.measureText(str)));
            CharSequence str2 = TextUtils.ellipsize(str, infoPaint, (float) this.infoWidth, TruncateAt.END);
            try {
                if (this.infoWidth < 0) {
                    this.infoWidth = AndroidUtilities.dp(10.0f);
                }
                this.infoLayout = new StaticLayout(str2, infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
            if (this.drawPhotoImage) {
                this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                this.photoImage.setNeedsQualityThumb(true);
                this.photoImage.setShouldGenerateQualityThumb(true);
                this.photoImage.setParentMessageObject(messageObject);
                if (this.currentPhotoObject != null) {
                    this.currentPhotoFilter = "86_86_b";
                    this.photoImage.setImage(null, null, null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, true);
                } else {
                    this.photoImage.setImageBitmap((BitmapDrawable) null);
                }
            }
            return width;
        }
    }

    private void calcBackgroundWidth(int maxWidth, int timeMore, int maxChildWidth) {
        if (this.hasLinkPreview || this.hasGamePreview || maxWidth - this.currentMessageObject.lastLineWidth < timeMore) {
            this.totalHeight += AndroidUtilities.dp(14.0f);
            this.backgroundWidth = Math.max(maxChildWidth, this.currentMessageObject.lastLineWidth) + AndroidUtilities.dp(31.0f);
            this.backgroundWidth = Math.max(this.backgroundWidth, this.timeWidth + AndroidUtilities.dp(31.0f));
            return;
        }
        int diff = maxChildWidth - this.currentMessageObject.lastLineWidth;
        if (diff < 0 || diff > timeMore) {
            this.backgroundWidth = Math.max(maxChildWidth, this.currentMessageObject.lastLineWidth + timeMore) + AndroidUtilities.dp(31.0f);
        } else {
            this.backgroundWidth = ((maxChildWidth + timeMore) - diff) + AndroidUtilities.dp(31.0f);
        }
    }

    public void setHighlightedText(String text) {
        if (this.currentMessageObject.messageOwner.message != null && this.currentMessageObject != null && this.currentMessageObject.type == 0 && !TextUtils.isEmpty(this.currentMessageObject.messageText) && text != null) {
            int start = TextUtils.indexOf(this.currentMessageObject.messageOwner.message.toLowerCase(), text.toLowerCase());
            if (start != -1) {
                int end = start + text.length();
                int c = 0;
                while (c < this.currentMessageObject.textLayoutBlocks.size()) {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(c);
                    if (start < block.charactersOffset || start >= block.charactersOffset + block.textLayout.getText().length()) {
                        c++;
                    } else {
                        this.linkSelectionBlockNum = c;
                        resetUrlPaths(true);
                        try {
                            LinkPath path = obtainNewUrlPath(true);
                            int length = block.textLayout.getText().length();
                            path.setCurrentLayout(block.textLayout, start, 0.0f);
                            block.textLayout.getSelectionPath(start, end - block.charactersOffset, path);
                            if (end >= block.charactersOffset + length) {
                                for (int a = c + 1; a < this.currentMessageObject.textLayoutBlocks.size(); a++) {
                                    TextLayoutBlock nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                    length = nextBlock.textLayout.getText().length();
                                    path = obtainNewUrlPath(true);
                                    path.setCurrentLayout(nextBlock.textLayout, 0, (float) nextBlock.height);
                                    nextBlock.textLayout.getSelectionPath(0, end - nextBlock.charactersOffset, path);
                                    if (end < (block.charactersOffset + length) - 1) {
                                        break;
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            FileLog.e("tmessages", e);
                        }
                        invalidate();
                        return;
                    }
                }
            } else if (!this.urlPathSelection.isEmpty()) {
                this.linkSelectionBlockNum = -1;
                resetUrlPaths(true);
                invalidate();
            }
        } else if (!this.urlPathSelection.isEmpty()) {
            this.linkSelectionBlockNum = -1;
            resetUrlPaths(true);
            invalidate();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMessageObject(MessageObject messageObject) {
        int maxWidth;
        Photo photo;
        TLObject document;
        int duration;
        boolean smallImage;
        int height;
        Throwable e;
        int lineLeft;
        boolean authorIsRTL;
        boolean hasRTL;
        int maxPhotoWidth;
        int i;
        int maxWidth2;
        DocumentAttribute attribute;
        PhotoSize photoSize;
        PhotoSize photoSize2;
        int dp;
        int durationWidth;
        float scale;
        ImageReceiver imageReceiver;
        TLObject tLObject;
        String str;
        FileLocation fileLocation;
        String fileName;
        int seconds;
        String str2;
        int rows;
        boolean fullWidth;
        int maxButtonWidth;
        int maxButtonsWidth;
        TL_keyboardButtonRow row;
        int buttonsCount;
        int buttonWidth;
        int b;
        ChatMessageCell chatMessageCell;
        BotButton botButton;
        String key;
        BotButton oldButton;
        if (messageObject.checkLayout()) {
            this.currentMessageObject = null;
        }
        boolean messageIdChanged = this.currentMessageObject == null || this.currentMessageObject.getId() != messageObject.getId();
        boolean messageChanged = this.currentMessageObject != messageObject || messageObject.forceUpdate;
        boolean dataChanged = this.currentMessageObject == messageObject && (isUserDataChanged() || this.photoNotSet);
        if (messageChanged || dataChanged || isPhotoDataChanged(messageObject)) {
            int width;
            int a;
            int timeWidthTotal;
            float f;
            int dp2;
            this.currentMessageObject = messageObject;
            this.lastSendState = messageObject.messageOwner.send_state;
            this.lastDeleteDate = messageObject.messageOwner.destroyTime;
            this.lastViewsCount = messageObject.messageOwner.views;
            this.isPressed = false;
            this.isCheckPressed = true;
            this.isAvatarVisible = false;
            this.wasLayout = false;
            this.drawShareButton = checkNeedDrawShareButton(messageObject);
            this.replyNameLayout = null;
            this.replyTextLayout = null;
            this.replyNameWidth = 0;
            this.replyTextWidth = 0;
            this.viaWidth = 0;
            this.viaNameWidth = 0;
            this.currentReplyPhoto = null;
            this.currentUser = null;
            this.currentChat = null;
            this.currentViaBotUser = null;
            this.drawNameLayout = false;
            resetPressedLink(-1);
            messageObject.forceUpdate = false;
            this.drawPhotoImage = false;
            this.hasLinkPreview = false;
            this.hasGamePreview = false;
            this.linkPreviewPressed = false;
            this.buttonPressed = 0;
            this.pressedBotButton = -1;
            this.linkPreviewHeight = 0;
            this.mediaOffsetY = 0;
            this.documentAttachType = 0;
            this.documentAttach = null;
            this.descriptionLayout = null;
            this.titleLayout = null;
            this.videoInfoLayout = null;
            this.siteNameLayout = null;
            this.authorLayout = null;
            this.captionLayout = null;
            this.docTitleLayout = null;
            this.drawImageButton = false;
            this.currentPhotoObject = null;
            this.currentPhotoObjectThumb = null;
            this.currentPhotoFilter = null;
            this.infoLayout = null;
            this.cancelLoading = false;
            this.buttonState = -1;
            this.currentUrl = null;
            this.photoNotSet = false;
            this.drawBackground = true;
            this.drawName = false;
            this.useSeekBarWaweform = false;
            this.drawForwardedName = false;
            this.mediaBackground = false;
            this.availableTimeWidth = 0;
            this.photoImage.setNeedsQualityThumb(false);
            this.photoImage.setShouldGenerateQualityThumb(false);
            this.photoImage.setParentMessageObject(null);
            this.photoImage.setRoundRadius(AndroidUtilities.dp(3.0f));
            if (messageChanged) {
                this.firstVisibleBlockNum = 0;
                this.lastVisibleBlockNum = 0;
                this.needNewVisiblePart = true;
            }
            boolean z;
            boolean photoExist;
            if (messageObject.type == 0) {
                this.drawForwardedName = true;
                if (AndroidUtilities.isTablet()) {
                    if (this.isChat && !messageObject.isOutOwner() && messageObject.isFromUser()) {
                        maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122.0f);
                        this.drawName = true;
                    } else {
                        z = (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isOutOwner()) ? false : true;
                        this.drawName = z;
                        maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(80.0f);
                    }
                } else if (this.isChat && !messageObject.isOutOwner() && messageObject.isFromUser()) {
                    maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122.0f);
                    this.drawName = true;
                } else {
                    maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(80.0f);
                    z = (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isOutOwner()) ? false : true;
                    this.drawName = z;
                }
                this.availableTimeWidth = maxWidth;
                measureTime(messageObject);
                int timeMore = this.timeWidth + AndroidUtilities.dp(6.0f);
                if (messageObject.isOutOwner()) {
                    timeMore += AndroidUtilities.dp(20.5f);
                }
                z = (messageObject.messageOwner.media instanceof TL_messageMediaGame) && (messageObject.messageOwner.media.game instanceof TL_game);
                this.hasGamePreview = z;
                z = (messageObject.messageOwner.media instanceof TL_messageMediaWebPage) && (messageObject.messageOwner.media.webpage instanceof TL_webPage);
                this.hasLinkPreview = z;
                this.backgroundWidth = maxWidth;
                if (this.hasLinkPreview || this.hasGamePreview || maxWidth - messageObject.lastLineWidth < timeMore) {
                    this.backgroundWidth = Math.max(this.backgroundWidth, messageObject.lastLineWidth) + AndroidUtilities.dp(31.0f);
                    this.backgroundWidth = Math.max(this.backgroundWidth, this.timeWidth + AndroidUtilities.dp(31.0f));
                } else {
                    int diff = this.backgroundWidth - messageObject.lastLineWidth;
                    if (diff < 0 || diff > timeMore) {
                        this.backgroundWidth = Math.max(this.backgroundWidth, messageObject.lastLineWidth + timeMore) + AndroidUtilities.dp(31.0f);
                    } else {
                        this.backgroundWidth = ((this.backgroundWidth + timeMore) - diff) + AndroidUtilities.dp(31.0f);
                    }
                }
                this.availableTimeWidth = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                setMessageObjectInternal(messageObject);
                this.backgroundWidth = (this.hasGamePreview ? AndroidUtilities.dp(10.0f) : 0) + messageObject.textWidth;
                this.totalHeight = (messageObject.textHeight + AndroidUtilities.dp(19.5f)) + this.namesOffset;
                int maxChildWidth = Math.max(Math.max(Math.max(Math.max(this.backgroundWidth, this.nameWidth), this.forwardedNameWidth), this.replyNameWidth), this.replyTextWidth);
                int maxWebWidth = 0;
                if (this.hasLinkPreview || this.hasGamePreview) {
                    int linkPreviewMaxWidth;
                    String site_name;
                    String title;
                    String author;
                    String description;
                    String type;
                    int restLines;
                    int restLinesCount;
                    ArrayList arrayList;
                    if (AndroidUtilities.isTablet()) {
                        if (!messageObject.isFromUser() || ((this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.messageOwner.to_id.chat_id == 0) || this.currentMessageObject.isOut())) {
                            linkPreviewMaxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(80.0f);
                        } else {
                            linkPreviewMaxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122.0f);
                        }
                    } else if (!messageObject.isFromUser() || ((this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.messageOwner.to_id.chat_id == 0) || this.currentMessageObject.isOutOwner())) {
                        linkPreviewMaxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(80.0f);
                    } else {
                        linkPreviewMaxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122.0f);
                    }
                    if (this.drawShareButton) {
                        linkPreviewMaxWidth -= AndroidUtilities.dp(20.0f);
                    }
                    if (messageObject.messageOwner.media.webpage != null) {
                        TL_webPage webPage = (TL_webPage) messageObject.messageOwner.media.webpage;
                        site_name = webPage.site_name;
                        title = webPage.title;
                        author = webPage.author;
                        description = webPage.description;
                        photo = webPage.photo;
                        document = webPage.document;
                        type = webPage.type;
                        duration = webPage.duration;
                        if (!(site_name == null || photo == null || !site_name.toLowerCase().equals("instagram"))) {
                            linkPreviewMaxWidth = Math.max(AndroidUtilities.displaySize.y / 3, this.currentMessageObject.textWidth);
                        }
                        if (type != null) {
                            if (!type.equals("app")) {
                                if (!type.equals(Scopes.PROFILE)) {
                                }
                            }
                            smallImage = true;
                            if (!(description == null || type == null)) {
                                if (!type.equals("app")) {
                                    if (!type.equals(Scopes.PROFILE)) {
                                    }
                                }
                                if (this.currentMessageObject.photoThumbs != null) {
                                    z = true;
                                    this.isSmallImage = z;
                                }
                            }
                            z = false;
                            this.isSmallImage = z;
                        }
                        smallImage = false;
                        if (type.equals("app")) {
                            if (type.equals(Scopes.PROFILE)) {
                            }
                        }
                        if (this.currentMessageObject.photoThumbs != null) {
                            z = true;
                            this.isSmallImage = z;
                        }
                        z = false;
                        this.isSmallImage = z;
                    } else {
                        TL_game game = messageObject.messageOwner.media.game;
                        site_name = game.title;
                        title = null;
                        description = TextUtils.isEmpty(messageObject.messageText) ? game.description : null;
                        photo = game.photo;
                        author = null;
                        document = game.document;
                        duration = 0;
                        type = "game";
                        this.isSmallImage = false;
                        smallImage = false;
                    }
                    int additinalWidth = AndroidUtilities.dp(10.0f);
                    int restLinesCount2 = 3;
                    int additionalHeight = 0;
                    linkPreviewMaxWidth -= additinalWidth;
                    if (this.currentMessageObject.photoThumbs == null && photo != null) {
                        this.currentMessageObject.generateThumbs(true);
                    }
                    if (site_name != null) {
                        try {
                            this.siteNameLayout = new StaticLayout(site_name, replyNamePaint, Math.min((int) Math.ceil((double) replyNamePaint.measureText(site_name)), linkPreviewMaxWidth), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            height = this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                            this.linkPreviewHeight += height;
                            this.totalHeight += height;
                            additionalHeight = 0 + height;
                            width = this.siteNameLayout.getWidth();
                            maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                            maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                        } catch (Throwable e2) {
                            FileLog.e("tmessages", e2);
                        }
                    }
                    boolean titleIsRTL = false;
                    if (title != null) {
                        try {
                            this.titleX = ConnectionsManager.DEFAULT_DATACENTER_ID;
                            if (this.linkPreviewHeight != 0) {
                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                this.totalHeight += AndroidUtilities.dp(2.0f);
                            }
                            restLines = 0;
                            if (!this.isSmallImage || description == null) {
                                this.titleLayout = StaticLayoutEx.createStaticLayout(title, replyNamePaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, (float) AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT), false, TruncateAt.END, linkPreviewMaxWidth, 4);
                                restLinesCount = 3;
                            } else {
                                restLines = 3;
                                this.titleLayout = generateStaticLayout(title, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), 3, 4);
                                restLinesCount = 3 - this.titleLayout.getLineCount();
                            }
                            try {
                                height = this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
                                this.linkPreviewHeight += height;
                                this.totalHeight += height;
                                for (a = 0; a < this.titleLayout.getLineCount(); a++) {
                                    lineLeft = (int) this.titleLayout.getLineLeft(a);
                                    if (lineLeft != 0) {
                                        titleIsRTL = true;
                                    }
                                    if (this.titleX == ConnectionsManager.DEFAULT_DATACENTER_ID) {
                                        this.titleX = -lineLeft;
                                    } else {
                                        this.titleX = Math.max(this.titleX, -lineLeft);
                                    }
                                    if (lineLeft != 0) {
                                        width = this.titleLayout.getWidth() - lineLeft;
                                    } else {
                                        width = (int) Math.ceil((double) this.titleLayout.getLineWidth(a));
                                    }
                                    if (a < restLines || (lineLeft != 0 && this.isSmallImage)) {
                                        width += AndroidUtilities.dp(52.0f);
                                    }
                                    maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                    maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                                }
                                restLinesCount2 = restLinesCount;
                            } catch (Exception e3) {
                                e2 = e3;
                            }
                        } catch (Exception e4) {
                            e2 = e4;
                            restLinesCount = 3;
                            FileLog.e("tmessages", e2);
                            restLinesCount2 = restLinesCount;
                            authorIsRTL = false;
                            if (author == null) {
                                restLinesCount = restLinesCount2;
                            } else {
                                try {
                                    if (this.linkPreviewHeight != 0) {
                                        this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                        this.totalHeight += AndroidUtilities.dp(2.0f);
                                    }
                                    if (restLinesCount2 == 3) {
                                    }
                                    this.authorLayout = generateStaticLayout(author, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount2, 1);
                                    restLinesCount = restLinesCount2 - this.authorLayout.getLineCount();
                                    try {
                                        height = this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                                        this.linkPreviewHeight += height;
                                        this.totalHeight += height;
                                        lineLeft = (int) this.authorLayout.getLineLeft(0);
                                        this.authorX = -lineLeft;
                                        if (lineLeft == 0) {
                                            width = (int) Math.ceil((double) this.authorLayout.getLineWidth(0));
                                        } else {
                                            width = this.authorLayout.getWidth() - lineLeft;
                                            authorIsRTL = true;
                                        }
                                        maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                        maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                                    } catch (Exception e5) {
                                        e2 = e5;
                                        FileLog.e("tmessages", e2);
                                        if (description != null) {
                                            try {
                                                this.descriptionX = 0;
                                                this.currentMessageObject.generateLinkDescription();
                                                if (this.linkPreviewHeight != 0) {
                                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                                }
                                                restLines = 0;
                                                if (restLinesCount == 3) {
                                                }
                                                restLines = restLinesCount;
                                                this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount, 6);
                                                height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                                                this.linkPreviewHeight += height;
                                                this.totalHeight += height;
                                                hasRTL = false;
                                                for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                                    lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                                    if (lineLeft == 0) {
                                                        hasRTL = true;
                                                        if (this.descriptionX != 0) {
                                                            this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                                        } else {
                                                            this.descriptionX = -lineLeft;
                                                        }
                                                    }
                                                }
                                                while (a < this.descriptionLayout.getLineCount()) {
                                                    lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                                    this.descriptionX = 0;
                                                    if (lineLeft != 0) {
                                                        width = hasRTL ? (int) Math.ceil((double) this.descriptionLayout.getLineWidth(a)) : this.descriptionLayout.getWidth();
                                                    } else {
                                                        width = this.descriptionLayout.getWidth() - lineLeft;
                                                    }
                                                    width += AndroidUtilities.dp(52.0f);
                                                    if (maxWebWidth >= width + additinalWidth) {
                                                        if (titleIsRTL) {
                                                            this.titleX += (width + additinalWidth) - maxWebWidth;
                                                        }
                                                        if (authorIsRTL) {
                                                            this.authorX += (width + additinalWidth) - maxWebWidth;
                                                        }
                                                        maxWebWidth = width + additinalWidth;
                                                    }
                                                    maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                                }
                                            } catch (Throwable e22) {
                                                FileLog.e("tmessages", e22);
                                            }
                                        }
                                        smallImage = false;
                                        this.isSmallImage = false;
                                        if (smallImage) {
                                            maxPhotoWidth = AndroidUtilities.dp(48.0f);
                                        } else {
                                            maxPhotoWidth = linkPreviewMaxWidth;
                                        }
                                        if (document == null) {
                                            if (photo != null) {
                                                if (type != null) {
                                                    if (type.equals("photo")) {
                                                        z = true;
                                                        this.drawImageButton = z;
                                                        arrayList = messageObject.photoThumbs;
                                                        if (this.drawImageButton) {
                                                            i = maxPhotoWidth;
                                                        } else {
                                                            i = AndroidUtilities.getPhotoSize();
                                                        }
                                                        this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                                        this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                                        if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                                            this.currentPhotoObjectThumb = null;
                                                        }
                                                    }
                                                }
                                                z = false;
                                                this.drawImageButton = z;
                                                arrayList = messageObject.photoThumbs;
                                                if (this.drawImageButton) {
                                                    i = maxPhotoWidth;
                                                } else {
                                                    i = AndroidUtilities.getPhotoSize();
                                                }
                                                if (this.drawImageButton) {
                                                }
                                                this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                                this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                                if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                                    this.currentPhotoObjectThumb = null;
                                                }
                                            }
                                            maxWidth2 = maxWidth;
                                        } else if (!MessageObject.isGifDocument(document)) {
                                            if (!MediaController.getInstance().canAutoplayGifs()) {
                                                messageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                                            }
                                            this.photoImage.setAllowStartAnimation(messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                            this.currentPhotoObject = document.thumb;
                                            while (a < document.attributes.size()) {
                                                attribute = (DocumentAttribute) document.attributes.get(a);
                                                if (attribute instanceof TL_documentAttributeImageSize) {
                                                }
                                                this.currentPhotoObject.w = attribute.w;
                                                this.currentPhotoObject.h = attribute.h;
                                            }
                                            photoSize = this.currentPhotoObject;
                                            photoSize2 = this.currentPhotoObject;
                                            dp = AndroidUtilities.dp(150.0f);
                                            photoSize2.h = dp;
                                            photoSize.w = dp;
                                            this.documentAttachType = 2;
                                            maxWidth2 = maxWidth;
                                        } else if (!MessageObject.isVideoDocument(document)) {
                                            this.currentPhotoObject = document.thumb;
                                            for (a = 0; a < document.attributes.size(); a++) {
                                                attribute = (DocumentAttribute) document.attributes.get(a);
                                                if (!(attribute instanceof TL_documentAttributeVideo)) {
                                                    this.currentPhotoObject.w = attribute.w;
                                                    this.currentPhotoObject.h = attribute.h;
                                                    break;
                                                }
                                            }
                                            photoSize = this.currentPhotoObject;
                                            photoSize2 = this.currentPhotoObject;
                                            dp = AndroidUtilities.dp(150.0f);
                                            photoSize2.h = dp;
                                            photoSize.w = dp;
                                            createDocumentLayout(0, messageObject);
                                            maxWidth2 = maxWidth;
                                        } else if (MessageObject.isStickerDocument(document)) {
                                            this.currentPhotoObject = document.thumb;
                                            for (a = 0; a < document.attributes.size(); a++) {
                                                attribute = (DocumentAttribute) document.attributes.get(a);
                                                if (!(attribute instanceof TL_documentAttributeImageSize)) {
                                                    this.currentPhotoObject.w = attribute.w;
                                                    this.currentPhotoObject.h = attribute.h;
                                                    break;
                                                }
                                            }
                                            photoSize = this.currentPhotoObject;
                                            photoSize2 = this.currentPhotoObject;
                                            dp = AndroidUtilities.dp(150.0f);
                                            photoSize2.h = dp;
                                            photoSize.w = dp;
                                            this.documentAttach = document;
                                            this.documentAttachType = 6;
                                            maxWidth2 = maxWidth;
                                        } else {
                                            calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                            if (!MessageObject.isStickerDocument(document)) {
                                                if (this.backgroundWidth < AndroidUtilities.dp(20.0f) + maxWidth) {
                                                    this.backgroundWidth = AndroidUtilities.dp(20.0f) + maxWidth;
                                                }
                                                if (!MessageObject.isVoiceDocument(document)) {
                                                    createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                                    this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                    this.totalHeight += AndroidUtilities.dp(44.0f);
                                                    this.linkPreviewHeight += AndroidUtilities.dp(44.0f);
                                                    calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                                    maxWidth2 = maxWidth;
                                                } else if (MessageObject.isMusicDocument(document)) {
                                                    durationWidth = createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                                    this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                    this.totalHeight += AndroidUtilities.dp(56.0f);
                                                    this.linkPreviewHeight += AndroidUtilities.dp(56.0f);
                                                    maxWidth -= AndroidUtilities.dp(86.0f);
                                                    maxChildWidth = Math.max(maxChildWidth, (durationWidth + additinalWidth) + AndroidUtilities.dp(94.0f));
                                                    maxChildWidth = (int) Math.max((float) maxChildWidth, (this.songLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                                    maxChildWidth = (int) Math.max((float) maxChildWidth, (this.performerLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                                    calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                                    maxWidth2 = maxWidth;
                                                } else {
                                                    createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(168.0f), messageObject);
                                                    this.drawImageButton = true;
                                                    if (this.drawPhotoImage) {
                                                        this.totalHeight += AndroidUtilities.dp(100.0f);
                                                        this.linkPreviewHeight += AndroidUtilities.dp(86.0f);
                                                        this.photoImage.setImageCoords(0, this.totalHeight + this.namesOffset, AndroidUtilities.dp(86.0f), AndroidUtilities.dp(86.0f));
                                                        maxWidth2 = maxWidth;
                                                    } else {
                                                        this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                        this.photoImage.setImageCoords(0, (this.totalHeight + this.namesOffset) - AndroidUtilities.dp(14.0f), AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                                                        this.totalHeight += AndroidUtilities.dp(64.0f);
                                                        this.linkPreviewHeight += AndroidUtilities.dp(50.0f);
                                                        maxWidth2 = maxWidth;
                                                    }
                                                }
                                            }
                                            maxWidth2 = maxWidth;
                                        }
                                        if (this.currentPhotoObject == null) {
                                            this.photoImage.setImageBitmap((Drawable) null);
                                            this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                                            this.totalHeight += AndroidUtilities.dp(4.0f);
                                        } else {
                                            if (type != null) {
                                                if (!type.equals("photo")) {
                                                    if (!type.equals("gif")) {
                                                    }
                                                }
                                                z = true;
                                                this.drawImageButton = z;
                                                if (this.linkPreviewHeight != 0) {
                                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                                }
                                                if (this.documentAttachType == 6) {
                                                    if (AndroidUtilities.isTablet()) {
                                                        maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                                    } else {
                                                        maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                                    }
                                                }
                                                maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                                this.currentPhotoObject.size = -1;
                                                if (this.currentPhotoObjectThumb != null) {
                                                    this.currentPhotoObjectThumb.size = -1;
                                                }
                                                if (!smallImage) {
                                                    height = maxPhotoWidth;
                                                    width = maxPhotoWidth;
                                                } else if (this.hasGamePreview) {
                                                    width = this.currentPhotoObject.w;
                                                    scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                    width = (int) (((float) width) / scale);
                                                    height = (int) (((float) this.currentPhotoObject.h) / scale);
                                                    height = AndroidUtilities.displaySize.y / 3;
                                                } else {
                                                    scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                    width = (int) (((float) 640) / scale);
                                                    height = (int) (((float) 360) / scale);
                                                }
                                                if (this.isSmallImage) {
                                                    this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                                    this.linkPreviewHeight += height;
                                                } else {
                                                    if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                        this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                        this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                                    }
                                                    this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                                }
                                                this.photoImage.setImageCoords(0, 0, width, height);
                                                this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                                this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                                if (this.documentAttachType != 6) {
                                                    imageReceiver = this.photoImage;
                                                    tLObject = this.documentAttach;
                                                    str = this.currentPhotoFilter;
                                                    if (this.currentPhotoObject == null) {
                                                        fileLocation = this.currentPhotoObject.location;
                                                    } else {
                                                        fileLocation = null;
                                                    }
                                                    imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                                } else if (this.documentAttachType != 4) {
                                                    this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                                } else if (this.documentAttachType != 2) {
                                                    photoExist = messageObject.mediaExists;
                                                    fileName = FileLoader.getAttachFileName(document);
                                                    if (!this.hasGamePreview) {
                                                    }
                                                    this.photoNotSet = false;
                                                    this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                                } else {
                                                    photoExist = messageObject.mediaExists;
                                                    fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                                    if (!this.hasGamePreview) {
                                                    }
                                                    this.photoNotSet = false;
                                                    this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                                }
                                                this.drawPhotoImage = true;
                                                if (type != null) {
                                                    seconds = duration - ((duration / 60) * 60);
                                                    str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                                    this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                                    this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                                }
                                                if (this.hasGamePreview) {
                                                    str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                                    this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                                    this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                                }
                                            }
                                            z = false;
                                            this.drawImageButton = z;
                                            if (this.linkPreviewHeight != 0) {
                                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                this.totalHeight += AndroidUtilities.dp(2.0f);
                                            }
                                            if (this.documentAttachType == 6) {
                                                if (AndroidUtilities.isTablet()) {
                                                    maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                                } else {
                                                    maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                                }
                                            }
                                            maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                            this.currentPhotoObject.size = -1;
                                            if (this.currentPhotoObjectThumb != null) {
                                                this.currentPhotoObjectThumb.size = -1;
                                            }
                                            if (!smallImage) {
                                                height = maxPhotoWidth;
                                                width = maxPhotoWidth;
                                            } else if (this.hasGamePreview) {
                                                width = this.currentPhotoObject.w;
                                                scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                width = (int) (((float) width) / scale);
                                                height = (int) (((float) this.currentPhotoObject.h) / scale);
                                                height = AndroidUtilities.displaySize.y / 3;
                                            } else {
                                                scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                width = (int) (((float) 640) / scale);
                                                height = (int) (((float) 360) / scale);
                                            }
                                            if (this.isSmallImage) {
                                                this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                                this.linkPreviewHeight += height;
                                            } else {
                                                if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                    this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                    this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                                }
                                                this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                            }
                                            this.photoImage.setImageCoords(0, 0, width, height);
                                            this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            if (this.documentAttachType != 6) {
                                                imageReceiver = this.photoImage;
                                                tLObject = this.documentAttach;
                                                str = this.currentPhotoFilter;
                                                if (this.currentPhotoObject == null) {
                                                    fileLocation = null;
                                                } else {
                                                    fileLocation = this.currentPhotoObject.location;
                                                }
                                                imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                            } else if (this.documentAttachType != 4) {
                                                this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                            } else if (this.documentAttachType != 2) {
                                                photoExist = messageObject.mediaExists;
                                                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                                if (this.hasGamePreview) {
                                                }
                                                this.photoNotSet = false;
                                                if (this.currentPhotoObjectThumb == null) {
                                                }
                                                this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                            } else {
                                                photoExist = messageObject.mediaExists;
                                                fileName = FileLoader.getAttachFileName(document);
                                                if (this.hasGamePreview) {
                                                }
                                                this.photoNotSet = false;
                                                this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                            }
                                            this.drawPhotoImage = true;
                                            if (type != null) {
                                                seconds = duration - ((duration / 60) * 60);
                                                str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                                this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                                this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                            }
                                            if (this.hasGamePreview) {
                                                str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                                this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                                this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                            }
                                        }
                                        this.linkPreviewHeight += messageObject.textHeight + AndroidUtilities.dp(6.0f);
                                        this.totalHeight += AndroidUtilities.dp(4.0f);
                                        calcBackgroundWidth(maxWidth2, timeMore, maxChildWidth);
                                        maxWidth = maxWidth2;
                                        width = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                                        this.captionLayout = new StaticLayout(messageObject.caption, MessageObject.getTextPaint(), width - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                        if (this.captionLayout.getLineCount() > 0) {
                                            timeWidthTotal = this.timeWidth + (messageObject.isOutOwner() ? 0 : AndroidUtilities.dp(20.0f));
                                            this.captionHeight = this.captionLayout.getHeight();
                                            this.totalHeight += this.captionHeight + AndroidUtilities.dp(9.0f);
                                            if (((float) (width - AndroidUtilities.dp(8.0f))) - (this.captionLayout.getLineWidth(this.captionLayout.getLineCount() - 1) + this.captionLayout.getLineLeft(this.captionLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                                                this.totalHeight += AndroidUtilities.dp(14.0f);
                                                this.captionHeight += AndroidUtilities.dp(14.0f);
                                            }
                                        }
                                        this.botButtons.clear();
                                        if (messageIdChanged) {
                                            this.botButtonsByData.clear();
                                        }
                                        if (messageObject.messageOwner.reply_markup instanceof TL_replyInlineMarkup) {
                                            rows = messageObject.messageOwner.reply_markup.rows.size();
                                            i = (AndroidUtilities.dp(48.0f) * rows) + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                            this.keyboardHeight = i;
                                            this.substractBackgroundHeight = i;
                                            this.widthForButtons = this.backgroundWidth;
                                            fullWidth = false;
                                            if (messageObject.wantedBotKeyboardWidth > this.widthForButtons) {
                                                if (this.isChat) {
                                                }
                                                maxButtonWidth = -AndroidUtilities.dp(f);
                                                if (AndroidUtilities.isTablet()) {
                                                    maxButtonWidth += AndroidUtilities.getMinTabletSide();
                                                } else {
                                                    maxButtonWidth += Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                                                }
                                                this.widthForButtons = Math.max(this.backgroundWidth, Math.min(messageObject.wantedBotKeyboardWidth, maxButtonWidth));
                                                fullWidth = true;
                                            }
                                            maxButtonsWidth = 0;
                                            for (a = 0; a < rows; a++) {
                                                row = (TL_keyboardButtonRow) messageObject.messageOwner.reply_markup.rows.get(a);
                                                buttonsCount = row.buttons.size();
                                                if (buttonsCount != 0) {
                                                    dp2 = this.widthForButtons - (AndroidUtilities.dp(5.0f) * (buttonsCount - 1));
                                                    if (!fullWidth) {
                                                    }
                                                    buttonWidth = ((dp2 - AndroidUtilities.dp(f)) - AndroidUtilities.dp(2.0f)) / buttonsCount;
                                                    for (b = 0; b < row.buttons.size(); b++) {
                                                        chatMessageCell = this;
                                                        botButton = new BotButton();
                                                        botButton.button = (KeyboardButton) row.buttons.get(b);
                                                        key = Utilities.bytesToHex(botButton.button.data);
                                                        oldButton = (BotButton) this.botButtonsByData.get(key);
                                                        if (oldButton == null) {
                                                            botButton.lastUpdateTime = System.currentTimeMillis();
                                                        } else {
                                                            botButton.progressAlpha = oldButton.progressAlpha;
                                                            botButton.angle = oldButton.angle;
                                                            botButton.lastUpdateTime = oldButton.lastUpdateTime;
                                                        }
                                                        this.botButtonsByData.put(key, botButton);
                                                        botButton.x = (AndroidUtilities.dp(5.0f) + buttonWidth) * b;
                                                        botButton.y = (AndroidUtilities.dp(48.0f) * a) + AndroidUtilities.dp(5.0f);
                                                        botButton.width = buttonWidth;
                                                        botButton.height = AndroidUtilities.dp(44.0f);
                                                        botButton.title = new StaticLayout(TextUtils.ellipsize(Emoji.replaceEmoji(botButton.button.text, botButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false), botButtonPaint, (float) (buttonWidth - AndroidUtilities.dp(10.0f)), TruncateAt.END), botButtonPaint, buttonWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_CENTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                                        this.botButtons.add(botButton);
                                                        if (b != row.buttons.size() - 1) {
                                                            maxButtonsWidth = Math.max(maxButtonsWidth, botButton.x + botButton.width);
                                                        }
                                                    }
                                                }
                                            }
                                            this.widthForButtons = maxButtonsWidth;
                                        } else {
                                            this.substractBackgroundHeight = 0;
                                            this.keyboardHeight = 0;
                                        }
                                        updateWaveform();
                                        updateButtonState(dataChanged);
                                    }
                                } catch (Exception e6) {
                                    e22 = e6;
                                    restLinesCount = restLinesCount2;
                                    FileLog.e("tmessages", e22);
                                    if (description != null) {
                                        this.descriptionX = 0;
                                        this.currentMessageObject.generateLinkDescription();
                                        if (this.linkPreviewHeight != 0) {
                                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                            this.totalHeight += AndroidUtilities.dp(2.0f);
                                        }
                                        restLines = 0;
                                        if (restLinesCount == 3) {
                                        }
                                        restLines = restLinesCount;
                                        this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount, 6);
                                        height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                                        this.linkPreviewHeight += height;
                                        this.totalHeight += height;
                                        hasRTL = false;
                                        for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                            lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                            if (lineLeft == 0) {
                                                hasRTL = true;
                                                if (this.descriptionX != 0) {
                                                    this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                                } else {
                                                    this.descriptionX = -lineLeft;
                                                }
                                            }
                                        }
                                        while (a < this.descriptionLayout.getLineCount()) {
                                            lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                            this.descriptionX = 0;
                                            if (lineLeft != 0) {
                                                width = this.descriptionLayout.getWidth() - lineLeft;
                                            } else if (hasRTL) {
                                            }
                                            width += AndroidUtilities.dp(52.0f);
                                            if (maxWebWidth >= width + additinalWidth) {
                                                if (titleIsRTL) {
                                                    this.titleX += (width + additinalWidth) - maxWebWidth;
                                                }
                                                if (authorIsRTL) {
                                                    this.authorX += (width + additinalWidth) - maxWebWidth;
                                                }
                                                maxWebWidth = width + additinalWidth;
                                            }
                                            maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                        }
                                    }
                                    smallImage = false;
                                    this.isSmallImage = false;
                                    if (smallImage) {
                                        maxPhotoWidth = linkPreviewMaxWidth;
                                    } else {
                                        maxPhotoWidth = AndroidUtilities.dp(48.0f);
                                    }
                                    if (document == null) {
                                        if (photo != null) {
                                            if (type != null) {
                                                if (type.equals("photo")) {
                                                    z = true;
                                                    this.drawImageButton = z;
                                                    arrayList = messageObject.photoThumbs;
                                                    if (this.drawImageButton) {
                                                        i = AndroidUtilities.getPhotoSize();
                                                    } else {
                                                        i = maxPhotoWidth;
                                                    }
                                                    if (this.drawImageButton) {
                                                    }
                                                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                                    if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                                        this.currentPhotoObjectThumb = null;
                                                    }
                                                }
                                            }
                                            z = false;
                                            this.drawImageButton = z;
                                            arrayList = messageObject.photoThumbs;
                                            if (this.drawImageButton) {
                                                i = maxPhotoWidth;
                                            } else {
                                                i = AndroidUtilities.getPhotoSize();
                                            }
                                            if (this.drawImageButton) {
                                            }
                                            this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                            this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                            if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                                this.currentPhotoObjectThumb = null;
                                            }
                                        }
                                        maxWidth2 = maxWidth;
                                    } else if (!MessageObject.isGifDocument(document)) {
                                        if (MediaController.getInstance().canAutoplayGifs()) {
                                            messageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                                        }
                                        if (messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                                        }
                                        this.photoImage.setAllowStartAnimation(messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                        this.currentPhotoObject = document.thumb;
                                        while (a < document.attributes.size()) {
                                            attribute = (DocumentAttribute) document.attributes.get(a);
                                            if (attribute instanceof TL_documentAttributeImageSize) {
                                            }
                                            this.currentPhotoObject.w = attribute.w;
                                            this.currentPhotoObject.h = attribute.h;
                                        }
                                        photoSize = this.currentPhotoObject;
                                        photoSize2 = this.currentPhotoObject;
                                        dp = AndroidUtilities.dp(150.0f);
                                        photoSize2.h = dp;
                                        photoSize.w = dp;
                                        this.documentAttachType = 2;
                                        maxWidth2 = maxWidth;
                                    } else if (!MessageObject.isVideoDocument(document)) {
                                        this.currentPhotoObject = document.thumb;
                                        for (a = 0; a < document.attributes.size(); a++) {
                                            attribute = (DocumentAttribute) document.attributes.get(a);
                                            if (!(attribute instanceof TL_documentAttributeVideo)) {
                                                this.currentPhotoObject.w = attribute.w;
                                                this.currentPhotoObject.h = attribute.h;
                                                break;
                                            }
                                        }
                                        photoSize = this.currentPhotoObject;
                                        photoSize2 = this.currentPhotoObject;
                                        dp = AndroidUtilities.dp(150.0f);
                                        photoSize2.h = dp;
                                        photoSize.w = dp;
                                        createDocumentLayout(0, messageObject);
                                        maxWidth2 = maxWidth;
                                    } else if (MessageObject.isStickerDocument(document)) {
                                        calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                        if (MessageObject.isStickerDocument(document)) {
                                            if (this.backgroundWidth < AndroidUtilities.dp(20.0f) + maxWidth) {
                                                this.backgroundWidth = AndroidUtilities.dp(20.0f) + maxWidth;
                                            }
                                            if (!MessageObject.isVoiceDocument(document)) {
                                                createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                                this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                this.totalHeight += AndroidUtilities.dp(44.0f);
                                                this.linkPreviewHeight += AndroidUtilities.dp(44.0f);
                                                calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                                maxWidth2 = maxWidth;
                                            } else if (MessageObject.isMusicDocument(document)) {
                                                createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(168.0f), messageObject);
                                                this.drawImageButton = true;
                                                if (this.drawPhotoImage) {
                                                    this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                    this.photoImage.setImageCoords(0, (this.totalHeight + this.namesOffset) - AndroidUtilities.dp(14.0f), AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                                                    this.totalHeight += AndroidUtilities.dp(64.0f);
                                                    this.linkPreviewHeight += AndroidUtilities.dp(50.0f);
                                                    maxWidth2 = maxWidth;
                                                } else {
                                                    this.totalHeight += AndroidUtilities.dp(100.0f);
                                                    this.linkPreviewHeight += AndroidUtilities.dp(86.0f);
                                                    this.photoImage.setImageCoords(0, this.totalHeight + this.namesOffset, AndroidUtilities.dp(86.0f), AndroidUtilities.dp(86.0f));
                                                    maxWidth2 = maxWidth;
                                                }
                                            } else {
                                                durationWidth = createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                                this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                                this.totalHeight += AndroidUtilities.dp(56.0f);
                                                this.linkPreviewHeight += AndroidUtilities.dp(56.0f);
                                                maxWidth -= AndroidUtilities.dp(86.0f);
                                                maxChildWidth = Math.max(maxChildWidth, (durationWidth + additinalWidth) + AndroidUtilities.dp(94.0f));
                                                maxChildWidth = (int) Math.max((float) maxChildWidth, (this.songLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                                maxChildWidth = (int) Math.max((float) maxChildWidth, (this.performerLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                                calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                                maxWidth2 = maxWidth;
                                            }
                                        }
                                        maxWidth2 = maxWidth;
                                    } else {
                                        this.currentPhotoObject = document.thumb;
                                        for (a = 0; a < document.attributes.size(); a++) {
                                            attribute = (DocumentAttribute) document.attributes.get(a);
                                            if (!(attribute instanceof TL_documentAttributeImageSize)) {
                                                this.currentPhotoObject.w = attribute.w;
                                                this.currentPhotoObject.h = attribute.h;
                                                break;
                                            }
                                        }
                                        photoSize = this.currentPhotoObject;
                                        photoSize2 = this.currentPhotoObject;
                                        dp = AndroidUtilities.dp(150.0f);
                                        photoSize2.h = dp;
                                        photoSize.w = dp;
                                        this.documentAttach = document;
                                        this.documentAttachType = 6;
                                        maxWidth2 = maxWidth;
                                    }
                                    if (this.currentPhotoObject == null) {
                                        this.photoImage.setImageBitmap((Drawable) null);
                                        this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                                        this.totalHeight += AndroidUtilities.dp(4.0f);
                                    } else {
                                        if (type != null) {
                                            if (type.equals("photo")) {
                                                if (type.equals("gif")) {
                                                }
                                            }
                                            z = true;
                                            this.drawImageButton = z;
                                            if (this.linkPreviewHeight != 0) {
                                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                this.totalHeight += AndroidUtilities.dp(2.0f);
                                            }
                                            if (this.documentAttachType == 6) {
                                                if (AndroidUtilities.isTablet()) {
                                                    maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                                } else {
                                                    maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                                }
                                            }
                                            maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                            this.currentPhotoObject.size = -1;
                                            if (this.currentPhotoObjectThumb != null) {
                                                this.currentPhotoObjectThumb.size = -1;
                                            }
                                            if (!smallImage) {
                                                height = maxPhotoWidth;
                                                width = maxPhotoWidth;
                                            } else if (this.hasGamePreview) {
                                                scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                width = (int) (((float) 640) / scale);
                                                height = (int) (((float) 360) / scale);
                                            } else {
                                                width = this.currentPhotoObject.w;
                                                scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                                width = (int) (((float) width) / scale);
                                                height = (int) (((float) this.currentPhotoObject.h) / scale);
                                                height = AndroidUtilities.displaySize.y / 3;
                                            }
                                            if (this.isSmallImage) {
                                                if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                    this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                    this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                                }
                                                this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                            } else {
                                                this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                                this.linkPreviewHeight += height;
                                            }
                                            this.photoImage.setImageCoords(0, 0, width, height);
                                            this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            if (this.documentAttachType != 6) {
                                                imageReceiver = this.photoImage;
                                                tLObject = this.documentAttach;
                                                str = this.currentPhotoFilter;
                                                if (this.currentPhotoObject == null) {
                                                    fileLocation = this.currentPhotoObject.location;
                                                } else {
                                                    fileLocation = null;
                                                }
                                                imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                            } else if (this.documentAttachType != 4) {
                                                this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                            } else if (this.documentAttachType != 2) {
                                                photoExist = messageObject.mediaExists;
                                                fileName = FileLoader.getAttachFileName(document);
                                                if (this.hasGamePreview) {
                                                }
                                                this.photoNotSet = false;
                                                this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                            } else {
                                                photoExist = messageObject.mediaExists;
                                                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                                if (this.hasGamePreview) {
                                                }
                                                this.photoNotSet = false;
                                                if (this.currentPhotoObjectThumb == null) {
                                                }
                                                this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                            }
                                            this.drawPhotoImage = true;
                                            if (type != null) {
                                                seconds = duration - ((duration / 60) * 60);
                                                str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                                this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                                this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                            }
                                            if (this.hasGamePreview) {
                                                str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                                this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                                this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                            }
                                        }
                                        z = false;
                                        this.drawImageButton = z;
                                        if (this.linkPreviewHeight != 0) {
                                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                            this.totalHeight += AndroidUtilities.dp(2.0f);
                                        }
                                        if (this.documentAttachType == 6) {
                                            if (AndroidUtilities.isTablet()) {
                                                maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                            } else {
                                                maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                            }
                                        }
                                        maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                        this.currentPhotoObject.size = -1;
                                        if (this.currentPhotoObjectThumb != null) {
                                            this.currentPhotoObjectThumb.size = -1;
                                        }
                                        if (!smallImage) {
                                            height = maxPhotoWidth;
                                            width = maxPhotoWidth;
                                        } else if (this.hasGamePreview) {
                                            width = this.currentPhotoObject.w;
                                            scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                            width = (int) (((float) width) / scale);
                                            height = (int) (((float) this.currentPhotoObject.h) / scale);
                                            height = AndroidUtilities.displaySize.y / 3;
                                        } else {
                                            scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                            width = (int) (((float) 640) / scale);
                                            height = (int) (((float) 360) / scale);
                                        }
                                        if (this.isSmallImage) {
                                            this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                            this.linkPreviewHeight += height;
                                        } else {
                                            if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                            }
                                            this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                        }
                                        this.photoImage.setImageCoords(0, 0, width, height);
                                        this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                        this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                        if (this.documentAttachType != 6) {
                                            imageReceiver = this.photoImage;
                                            tLObject = this.documentAttach;
                                            str = this.currentPhotoFilter;
                                            if (this.currentPhotoObject == null) {
                                                fileLocation = null;
                                            } else {
                                                fileLocation = this.currentPhotoObject.location;
                                            }
                                            imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                        } else if (this.documentAttachType != 4) {
                                            this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                        } else if (this.documentAttachType != 2) {
                                            photoExist = messageObject.mediaExists;
                                            fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                            if (this.hasGamePreview) {
                                            }
                                            this.photoNotSet = false;
                                            if (this.currentPhotoObjectThumb == null) {
                                            }
                                            this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                        } else {
                                            photoExist = messageObject.mediaExists;
                                            fileName = FileLoader.getAttachFileName(document);
                                            if (this.hasGamePreview) {
                                            }
                                            this.photoNotSet = false;
                                            this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                        }
                                        this.drawPhotoImage = true;
                                        if (type != null) {
                                            seconds = duration - ((duration / 60) * 60);
                                            str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                            this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                            this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                        }
                                        if (this.hasGamePreview) {
                                            str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                            this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                            this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                        }
                                    }
                                    this.linkPreviewHeight += messageObject.textHeight + AndroidUtilities.dp(6.0f);
                                    this.totalHeight += AndroidUtilities.dp(4.0f);
                                    calcBackgroundWidth(maxWidth2, timeMore, maxChildWidth);
                                    maxWidth = maxWidth2;
                                    width = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                                    this.captionLayout = new StaticLayout(messageObject.caption, MessageObject.getTextPaint(), width - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                    if (this.captionLayout.getLineCount() > 0) {
                                        if (messageObject.isOutOwner()) {
                                        }
                                        timeWidthTotal = this.timeWidth + (messageObject.isOutOwner() ? 0 : AndroidUtilities.dp(20.0f));
                                        this.captionHeight = this.captionLayout.getHeight();
                                        this.totalHeight += this.captionHeight + AndroidUtilities.dp(9.0f);
                                        if (((float) (width - AndroidUtilities.dp(8.0f))) - (this.captionLayout.getLineWidth(this.captionLayout.getLineCount() - 1) + this.captionLayout.getLineLeft(this.captionLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                                            this.totalHeight += AndroidUtilities.dp(14.0f);
                                            this.captionHeight += AndroidUtilities.dp(14.0f);
                                        }
                                    }
                                    this.botButtons.clear();
                                    if (messageIdChanged) {
                                        this.botButtonsByData.clear();
                                    }
                                    if (messageObject.messageOwner.reply_markup instanceof TL_replyInlineMarkup) {
                                        this.substractBackgroundHeight = 0;
                                        this.keyboardHeight = 0;
                                    } else {
                                        rows = messageObject.messageOwner.reply_markup.rows.size();
                                        i = (AndroidUtilities.dp(48.0f) * rows) + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                        this.keyboardHeight = i;
                                        this.substractBackgroundHeight = i;
                                        this.widthForButtons = this.backgroundWidth;
                                        fullWidth = false;
                                        if (messageObject.wantedBotKeyboardWidth > this.widthForButtons) {
                                            if (this.isChat) {
                                            }
                                            maxButtonWidth = -AndroidUtilities.dp(f);
                                            if (AndroidUtilities.isTablet()) {
                                                maxButtonWidth += Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                                            } else {
                                                maxButtonWidth += AndroidUtilities.getMinTabletSide();
                                            }
                                            this.widthForButtons = Math.max(this.backgroundWidth, Math.min(messageObject.wantedBotKeyboardWidth, maxButtonWidth));
                                            fullWidth = true;
                                        }
                                        maxButtonsWidth = 0;
                                        for (a = 0; a < rows; a++) {
                                            row = (TL_keyboardButtonRow) messageObject.messageOwner.reply_markup.rows.get(a);
                                            buttonsCount = row.buttons.size();
                                            if (buttonsCount != 0) {
                                                dp2 = this.widthForButtons - (AndroidUtilities.dp(5.0f) * (buttonsCount - 1));
                                                if (fullWidth) {
                                                }
                                                buttonWidth = ((dp2 - AndroidUtilities.dp(f)) - AndroidUtilities.dp(2.0f)) / buttonsCount;
                                                for (b = 0; b < row.buttons.size(); b++) {
                                                    chatMessageCell = this;
                                                    botButton = new BotButton();
                                                    botButton.button = (KeyboardButton) row.buttons.get(b);
                                                    key = Utilities.bytesToHex(botButton.button.data);
                                                    oldButton = (BotButton) this.botButtonsByData.get(key);
                                                    if (oldButton == null) {
                                                        botButton.lastUpdateTime = System.currentTimeMillis();
                                                    } else {
                                                        botButton.progressAlpha = oldButton.progressAlpha;
                                                        botButton.angle = oldButton.angle;
                                                        botButton.lastUpdateTime = oldButton.lastUpdateTime;
                                                    }
                                                    this.botButtonsByData.put(key, botButton);
                                                    botButton.x = (AndroidUtilities.dp(5.0f) + buttonWidth) * b;
                                                    botButton.y = (AndroidUtilities.dp(48.0f) * a) + AndroidUtilities.dp(5.0f);
                                                    botButton.width = buttonWidth;
                                                    botButton.height = AndroidUtilities.dp(44.0f);
                                                    botButton.title = new StaticLayout(TextUtils.ellipsize(Emoji.replaceEmoji(botButton.button.text, botButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false), botButtonPaint, (float) (buttonWidth - AndroidUtilities.dp(10.0f)), TruncateAt.END), botButtonPaint, buttonWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_CENTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                                    this.botButtons.add(botButton);
                                                    if (b != row.buttons.size() - 1) {
                                                        maxButtonsWidth = Math.max(maxButtonsWidth, botButton.x + botButton.width);
                                                    }
                                                }
                                            }
                                        }
                                        this.widthForButtons = maxButtonsWidth;
                                    }
                                    updateWaveform();
                                    updateButtonState(dataChanged);
                                }
                            }
                            if (description != null) {
                                this.descriptionX = 0;
                                this.currentMessageObject.generateLinkDescription();
                                if (this.linkPreviewHeight != 0) {
                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                }
                                restLines = 0;
                                if (restLinesCount == 3) {
                                }
                                restLines = restLinesCount;
                                this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount, 6);
                                height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                                this.linkPreviewHeight += height;
                                this.totalHeight += height;
                                hasRTL = false;
                                for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                    lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                    if (lineLeft == 0) {
                                        hasRTL = true;
                                        if (this.descriptionX != 0) {
                                            this.descriptionX = -lineLeft;
                                        } else {
                                            this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                        }
                                    }
                                }
                                while (a < this.descriptionLayout.getLineCount()) {
                                    lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                    this.descriptionX = 0;
                                    if (lineLeft != 0) {
                                        width = this.descriptionLayout.getWidth() - lineLeft;
                                    } else if (hasRTL) {
                                    }
                                    width += AndroidUtilities.dp(52.0f);
                                    if (maxWebWidth >= width + additinalWidth) {
                                        if (titleIsRTL) {
                                            this.titleX += (width + additinalWidth) - maxWebWidth;
                                        }
                                        if (authorIsRTL) {
                                            this.authorX += (width + additinalWidth) - maxWebWidth;
                                        }
                                        maxWebWidth = width + additinalWidth;
                                    }
                                    maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                }
                            }
                            smallImage = false;
                            this.isSmallImage = false;
                            if (smallImage) {
                                maxPhotoWidth = AndroidUtilities.dp(48.0f);
                            } else {
                                maxPhotoWidth = linkPreviewMaxWidth;
                            }
                            if (document == null) {
                                if (photo != null) {
                                    if (type != null) {
                                        if (type.equals("photo")) {
                                            z = true;
                                            this.drawImageButton = z;
                                            arrayList = messageObject.photoThumbs;
                                            if (this.drawImageButton) {
                                                i = AndroidUtilities.getPhotoSize();
                                            } else {
                                                i = maxPhotoWidth;
                                            }
                                            if (this.drawImageButton) {
                                            }
                                            this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                            this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                            if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                                this.currentPhotoObjectThumb = null;
                                            }
                                        }
                                    }
                                    z = false;
                                    this.drawImageButton = z;
                                    arrayList = messageObject.photoThumbs;
                                    if (this.drawImageButton) {
                                        i = maxPhotoWidth;
                                    } else {
                                        i = AndroidUtilities.getPhotoSize();
                                    }
                                    if (this.drawImageButton) {
                                    }
                                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                    if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                        this.currentPhotoObjectThumb = null;
                                    }
                                }
                                maxWidth2 = maxWidth;
                            } else if (!MessageObject.isGifDocument(document)) {
                                if (MediaController.getInstance().canAutoplayGifs()) {
                                    messageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                                }
                                if (messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                                }
                                this.photoImage.setAllowStartAnimation(messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                this.currentPhotoObject = document.thumb;
                                while (a < document.attributes.size()) {
                                    attribute = (DocumentAttribute) document.attributes.get(a);
                                    if (attribute instanceof TL_documentAttributeImageSize) {
                                    }
                                    this.currentPhotoObject.w = attribute.w;
                                    this.currentPhotoObject.h = attribute.h;
                                }
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                                this.documentAttachType = 2;
                                maxWidth2 = maxWidth;
                            } else if (!MessageObject.isVideoDocument(document)) {
                                this.currentPhotoObject = document.thumb;
                                for (a = 0; a < document.attributes.size(); a++) {
                                    attribute = (DocumentAttribute) document.attributes.get(a);
                                    if (!(attribute instanceof TL_documentAttributeVideo)) {
                                        this.currentPhotoObject.w = attribute.w;
                                        this.currentPhotoObject.h = attribute.h;
                                        break;
                                    }
                                }
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                                createDocumentLayout(0, messageObject);
                                maxWidth2 = maxWidth;
                            } else if (MessageObject.isStickerDocument(document)) {
                                this.currentPhotoObject = document.thumb;
                                for (a = 0; a < document.attributes.size(); a++) {
                                    attribute = (DocumentAttribute) document.attributes.get(a);
                                    if (!(attribute instanceof TL_documentAttributeImageSize)) {
                                        this.currentPhotoObject.w = attribute.w;
                                        this.currentPhotoObject.h = attribute.h;
                                        break;
                                    }
                                }
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                                this.documentAttach = document;
                                this.documentAttachType = 6;
                                maxWidth2 = maxWidth;
                            } else {
                                calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                if (MessageObject.isStickerDocument(document)) {
                                    if (this.backgroundWidth < AndroidUtilities.dp(20.0f) + maxWidth) {
                                        this.backgroundWidth = AndroidUtilities.dp(20.0f) + maxWidth;
                                    }
                                    if (!MessageObject.isVoiceDocument(document)) {
                                        createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                        this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                        this.totalHeight += AndroidUtilities.dp(44.0f);
                                        this.linkPreviewHeight += AndroidUtilities.dp(44.0f);
                                        calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                        maxWidth2 = maxWidth;
                                    } else if (MessageObject.isMusicDocument(document)) {
                                        durationWidth = createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                        this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                        this.totalHeight += AndroidUtilities.dp(56.0f);
                                        this.linkPreviewHeight += AndroidUtilities.dp(56.0f);
                                        maxWidth -= AndroidUtilities.dp(86.0f);
                                        maxChildWidth = Math.max(maxChildWidth, (durationWidth + additinalWidth) + AndroidUtilities.dp(94.0f));
                                        maxChildWidth = (int) Math.max((float) maxChildWidth, (this.songLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                        maxChildWidth = (int) Math.max((float) maxChildWidth, (this.performerLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                        calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                        maxWidth2 = maxWidth;
                                    } else {
                                        createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(168.0f), messageObject);
                                        this.drawImageButton = true;
                                        if (this.drawPhotoImage) {
                                            this.totalHeight += AndroidUtilities.dp(100.0f);
                                            this.linkPreviewHeight += AndroidUtilities.dp(86.0f);
                                            this.photoImage.setImageCoords(0, this.totalHeight + this.namesOffset, AndroidUtilities.dp(86.0f), AndroidUtilities.dp(86.0f));
                                            maxWidth2 = maxWidth;
                                        } else {
                                            this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                            this.photoImage.setImageCoords(0, (this.totalHeight + this.namesOffset) - AndroidUtilities.dp(14.0f), AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                                            this.totalHeight += AndroidUtilities.dp(64.0f);
                                            this.linkPreviewHeight += AndroidUtilities.dp(50.0f);
                                            maxWidth2 = maxWidth;
                                        }
                                    }
                                }
                                maxWidth2 = maxWidth;
                            }
                            if (this.currentPhotoObject == null) {
                                if (type != null) {
                                    if (type.equals("photo")) {
                                        if (type.equals("gif")) {
                                        }
                                    }
                                    z = true;
                                    this.drawImageButton = z;
                                    if (this.linkPreviewHeight != 0) {
                                        this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                        this.totalHeight += AndroidUtilities.dp(2.0f);
                                    }
                                    if (this.documentAttachType == 6) {
                                        if (AndroidUtilities.isTablet()) {
                                            maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                        } else {
                                            maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                        }
                                    }
                                    maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                    this.currentPhotoObject.size = -1;
                                    if (this.currentPhotoObjectThumb != null) {
                                        this.currentPhotoObjectThumb.size = -1;
                                    }
                                    if (!smallImage) {
                                        height = maxPhotoWidth;
                                        width = maxPhotoWidth;
                                    } else if (this.hasGamePreview) {
                                        scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                        width = (int) (((float) 640) / scale);
                                        height = (int) (((float) 360) / scale);
                                    } else {
                                        width = this.currentPhotoObject.w;
                                        scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                        width = (int) (((float) width) / scale);
                                        height = (int) (((float) this.currentPhotoObject.h) / scale);
                                        height = AndroidUtilities.displaySize.y / 3;
                                    }
                                    if (this.isSmallImage) {
                                        if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                            this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                            this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                        }
                                        this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                    } else {
                                        this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                        this.linkPreviewHeight += height;
                                    }
                                    this.photoImage.setImageCoords(0, 0, width, height);
                                    this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                    this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                    if (this.documentAttachType != 6) {
                                        imageReceiver = this.photoImage;
                                        tLObject = this.documentAttach;
                                        str = this.currentPhotoFilter;
                                        if (this.currentPhotoObject == null) {
                                            fileLocation = this.currentPhotoObject.location;
                                        } else {
                                            fileLocation = null;
                                        }
                                        imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                    } else if (this.documentAttachType != 4) {
                                        this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                    } else if (this.documentAttachType != 2) {
                                        photoExist = messageObject.mediaExists;
                                        fileName = FileLoader.getAttachFileName(document);
                                        if (this.hasGamePreview) {
                                        }
                                        this.photoNotSet = false;
                                        this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                    } else {
                                        photoExist = messageObject.mediaExists;
                                        fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                        if (this.hasGamePreview) {
                                        }
                                        this.photoNotSet = false;
                                        if (this.currentPhotoObjectThumb == null) {
                                        }
                                        this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                    }
                                    this.drawPhotoImage = true;
                                    if (type != null) {
                                        seconds = duration - ((duration / 60) * 60);
                                        str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                        this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                        this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                    }
                                    if (this.hasGamePreview) {
                                        str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                        this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                        this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                    }
                                }
                                z = false;
                                this.drawImageButton = z;
                                if (this.linkPreviewHeight != 0) {
                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                }
                                if (this.documentAttachType == 6) {
                                    if (AndroidUtilities.isTablet()) {
                                        maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                    } else {
                                        maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                    }
                                }
                                maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                this.currentPhotoObject.size = -1;
                                if (this.currentPhotoObjectThumb != null) {
                                    this.currentPhotoObjectThumb.size = -1;
                                }
                                if (!smallImage) {
                                    height = maxPhotoWidth;
                                    width = maxPhotoWidth;
                                } else if (this.hasGamePreview) {
                                    width = this.currentPhotoObject.w;
                                    scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                    width = (int) (((float) width) / scale);
                                    height = (int) (((float) this.currentPhotoObject.h) / scale);
                                    height = AndroidUtilities.displaySize.y / 3;
                                } else {
                                    scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                    width = (int) (((float) 640) / scale);
                                    height = (int) (((float) 360) / scale);
                                }
                                if (this.isSmallImage) {
                                    this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                    this.linkPreviewHeight += height;
                                } else {
                                    if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                        this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                        this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                    }
                                    this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                }
                                this.photoImage.setImageCoords(0, 0, width, height);
                                this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                if (this.documentAttachType != 6) {
                                    imageReceiver = this.photoImage;
                                    tLObject = this.documentAttach;
                                    str = this.currentPhotoFilter;
                                    if (this.currentPhotoObject == null) {
                                        fileLocation = null;
                                    } else {
                                        fileLocation = this.currentPhotoObject.location;
                                    }
                                    imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                } else if (this.documentAttachType != 4) {
                                    this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                } else if (this.documentAttachType != 2) {
                                    photoExist = messageObject.mediaExists;
                                    fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                    if (this.hasGamePreview) {
                                    }
                                    this.photoNotSet = false;
                                    if (this.currentPhotoObjectThumb == null) {
                                    }
                                    this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                } else {
                                    photoExist = messageObject.mediaExists;
                                    fileName = FileLoader.getAttachFileName(document);
                                    if (this.hasGamePreview) {
                                    }
                                    this.photoNotSet = false;
                                    this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                }
                                this.drawPhotoImage = true;
                                if (type != null) {
                                    seconds = duration - ((duration / 60) * 60);
                                    str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                    this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                    this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                }
                                if (this.hasGamePreview) {
                                    str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                    this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                    this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                }
                            } else {
                                this.photoImage.setImageBitmap((Drawable) null);
                                this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                                this.totalHeight += AndroidUtilities.dp(4.0f);
                            }
                            this.linkPreviewHeight += messageObject.textHeight + AndroidUtilities.dp(6.0f);
                            this.totalHeight += AndroidUtilities.dp(4.0f);
                            calcBackgroundWidth(maxWidth2, timeMore, maxChildWidth);
                            maxWidth = maxWidth2;
                            try {
                                width = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                                this.captionLayout = new StaticLayout(messageObject.caption, MessageObject.getTextPaint(), width - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                if (this.captionLayout.getLineCount() > 0) {
                                    if (messageObject.isOutOwner()) {
                                    }
                                    timeWidthTotal = this.timeWidth + (messageObject.isOutOwner() ? 0 : AndroidUtilities.dp(20.0f));
                                    this.captionHeight = this.captionLayout.getHeight();
                                    this.totalHeight += this.captionHeight + AndroidUtilities.dp(9.0f);
                                    if (((float) (width - AndroidUtilities.dp(8.0f))) - (this.captionLayout.getLineWidth(this.captionLayout.getLineCount() - 1) + this.captionLayout.getLineLeft(this.captionLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                                        this.totalHeight += AndroidUtilities.dp(14.0f);
                                        this.captionHeight += AndroidUtilities.dp(14.0f);
                                    }
                                }
                            } catch (Throwable e222) {
                                FileLog.e("tmessages", e222);
                            }
                            this.botButtons.clear();
                            if (messageIdChanged) {
                                this.botButtonsByData.clear();
                            }
                            if (messageObject.messageOwner.reply_markup instanceof TL_replyInlineMarkup) {
                                rows = messageObject.messageOwner.reply_markup.rows.size();
                                i = (AndroidUtilities.dp(48.0f) * rows) + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                this.keyboardHeight = i;
                                this.substractBackgroundHeight = i;
                                this.widthForButtons = this.backgroundWidth;
                                fullWidth = false;
                                if (messageObject.wantedBotKeyboardWidth > this.widthForButtons) {
                                    if (this.isChat) {
                                    }
                                    maxButtonWidth = -AndroidUtilities.dp(f);
                                    if (AndroidUtilities.isTablet()) {
                                        maxButtonWidth += AndroidUtilities.getMinTabletSide();
                                    } else {
                                        maxButtonWidth += Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                                    }
                                    this.widthForButtons = Math.max(this.backgroundWidth, Math.min(messageObject.wantedBotKeyboardWidth, maxButtonWidth));
                                    fullWidth = true;
                                }
                                maxButtonsWidth = 0;
                                for (a = 0; a < rows; a++) {
                                    row = (TL_keyboardButtonRow) messageObject.messageOwner.reply_markup.rows.get(a);
                                    buttonsCount = row.buttons.size();
                                    if (buttonsCount != 0) {
                                        dp2 = this.widthForButtons - (AndroidUtilities.dp(5.0f) * (buttonsCount - 1));
                                        if (fullWidth) {
                                        }
                                        buttonWidth = ((dp2 - AndroidUtilities.dp(f)) - AndroidUtilities.dp(2.0f)) / buttonsCount;
                                        for (b = 0; b < row.buttons.size(); b++) {
                                            chatMessageCell = this;
                                            botButton = new BotButton();
                                            botButton.button = (KeyboardButton) row.buttons.get(b);
                                            key = Utilities.bytesToHex(botButton.button.data);
                                            oldButton = (BotButton) this.botButtonsByData.get(key);
                                            if (oldButton == null) {
                                                botButton.progressAlpha = oldButton.progressAlpha;
                                                botButton.angle = oldButton.angle;
                                                botButton.lastUpdateTime = oldButton.lastUpdateTime;
                                            } else {
                                                botButton.lastUpdateTime = System.currentTimeMillis();
                                            }
                                            this.botButtonsByData.put(key, botButton);
                                            botButton.x = (AndroidUtilities.dp(5.0f) + buttonWidth) * b;
                                            botButton.y = (AndroidUtilities.dp(48.0f) * a) + AndroidUtilities.dp(5.0f);
                                            botButton.width = buttonWidth;
                                            botButton.height = AndroidUtilities.dp(44.0f);
                                            botButton.title = new StaticLayout(TextUtils.ellipsize(Emoji.replaceEmoji(botButton.button.text, botButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false), botButtonPaint, (float) (buttonWidth - AndroidUtilities.dp(10.0f)), TruncateAt.END), botButtonPaint, buttonWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_CENTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                            this.botButtons.add(botButton);
                                            if (b != row.buttons.size() - 1) {
                                                maxButtonsWidth = Math.max(maxButtonsWidth, botButton.x + botButton.width);
                                            }
                                        }
                                    }
                                }
                                this.widthForButtons = maxButtonsWidth;
                            } else {
                                this.substractBackgroundHeight = 0;
                                this.keyboardHeight = 0;
                            }
                            updateWaveform();
                            updateButtonState(dataChanged);
                        }
                    }
                    authorIsRTL = false;
                    if (author == null) {
                        if (this.linkPreviewHeight != 0) {
                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                            this.totalHeight += AndroidUtilities.dp(2.0f);
                        }
                        if (restLinesCount2 == 3 || (this.isSmallImage && description != null)) {
                            this.authorLayout = generateStaticLayout(author, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount2, 1);
                            restLinesCount = restLinesCount2 - this.authorLayout.getLineCount();
                        } else {
                            this.authorLayout = new StaticLayout(author, replyNamePaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            restLinesCount = restLinesCount2;
                        }
                        height = this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                        this.linkPreviewHeight += height;
                        this.totalHeight += height;
                        lineLeft = (int) this.authorLayout.getLineLeft(0);
                        this.authorX = -lineLeft;
                        if (lineLeft == 0) {
                            width = this.authorLayout.getWidth() - lineLeft;
                            authorIsRTL = true;
                        } else {
                            width = (int) Math.ceil((double) this.authorLayout.getLineWidth(0));
                        }
                        maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                        maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                    } else {
                        restLinesCount = restLinesCount2;
                    }
                    if (description != null) {
                        this.descriptionX = 0;
                        this.currentMessageObject.generateLinkDescription();
                        if (this.linkPreviewHeight != 0) {
                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                            this.totalHeight += AndroidUtilities.dp(2.0f);
                        }
                        restLines = 0;
                        if (restLinesCount == 3 || this.isSmallImage) {
                            restLines = restLinesCount;
                            this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(52.0f), restLinesCount, 6);
                        } else {
                            this.descriptionLayout = StaticLayoutEx.createStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, (float) AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT), false, TruncateAt.END, linkPreviewMaxWidth, 6);
                        }
                        height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                        this.linkPreviewHeight += height;
                        this.totalHeight += height;
                        hasRTL = false;
                        for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                            lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                            if (lineLeft == 0) {
                                hasRTL = true;
                                if (this.descriptionX != 0) {
                                    this.descriptionX = -lineLeft;
                                } else {
                                    this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                }
                            }
                        }
                        for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                            lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                            if (lineLeft == 0 && this.descriptionX != 0) {
                                this.descriptionX = 0;
                            }
                            if (lineLeft != 0) {
                                width = this.descriptionLayout.getWidth() - lineLeft;
                            } else if (hasRTL) {
                            }
                            if (a < restLines || !(restLines == 0 || lineLeft == 0 || !this.isSmallImage)) {
                                width += AndroidUtilities.dp(52.0f);
                            }
                            if (maxWebWidth >= width + additinalWidth) {
                                if (titleIsRTL) {
                                    this.titleX += (width + additinalWidth) - maxWebWidth;
                                }
                                if (authorIsRTL) {
                                    this.authorX += (width + additinalWidth) - maxWebWidth;
                                }
                                maxWebWidth = width + additinalWidth;
                            }
                            maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                        }
                    }
                    if (smallImage && (this.descriptionLayout == null || (this.descriptionLayout != null && this.descriptionLayout.getLineCount() == 1))) {
                        smallImage = false;
                        this.isSmallImage = false;
                    }
                    if (smallImage) {
                        maxPhotoWidth = AndroidUtilities.dp(48.0f);
                    } else {
                        maxPhotoWidth = linkPreviewMaxWidth;
                    }
                    if (document == null) {
                        if (photo != null) {
                            if (type != null) {
                                if (type.equals("photo")) {
                                    z = true;
                                    this.drawImageButton = z;
                                    arrayList = messageObject.photoThumbs;
                                    if (this.drawImageButton) {
                                        i = AndroidUtilities.getPhotoSize();
                                    } else {
                                        i = maxPhotoWidth;
                                    }
                                    if (this.drawImageButton) {
                                    }
                                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                    if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                        this.currentPhotoObjectThumb = null;
                                    }
                                }
                            }
                            z = false;
                            this.drawImageButton = z;
                            arrayList = messageObject.photoThumbs;
                            if (this.drawImageButton) {
                                i = maxPhotoWidth;
                            } else {
                                i = AndroidUtilities.getPhotoSize();
                            }
                            if (this.drawImageButton) {
                            }
                            this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, this.drawImageButton);
                            this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                            if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                this.currentPhotoObjectThumb = null;
                            }
                        }
                        maxWidth2 = maxWidth;
                    } else if (!MessageObject.isGifDocument(document)) {
                        if (MediaController.getInstance().canAutoplayGifs()) {
                            messageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                        }
                        if (messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                        }
                        this.photoImage.setAllowStartAnimation(messageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        this.currentPhotoObject = document.thumb;
                        if (this.currentPhotoObject != null && (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0)) {
                            for (a = 0; a < document.attributes.size(); a++) {
                                attribute = (DocumentAttribute) document.attributes.get(a);
                                if ((attribute instanceof TL_documentAttributeImageSize) || (attribute instanceof TL_documentAttributeVideo)) {
                                    this.currentPhotoObject.w = attribute.w;
                                    this.currentPhotoObject.h = attribute.h;
                                    break;
                                }
                            }
                            if (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0) {
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                            }
                        }
                        this.documentAttachType = 2;
                        maxWidth2 = maxWidth;
                    } else if (!MessageObject.isVideoDocument(document)) {
                        this.currentPhotoObject = document.thumb;
                        if (this.currentPhotoObject != null && (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0)) {
                            for (a = 0; a < document.attributes.size(); a++) {
                                attribute = (DocumentAttribute) document.attributes.get(a);
                                if (!(attribute instanceof TL_documentAttributeVideo)) {
                                    this.currentPhotoObject.w = attribute.w;
                                    this.currentPhotoObject.h = attribute.h;
                                    break;
                                }
                            }
                            if (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0) {
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                            }
                        }
                        createDocumentLayout(0, messageObject);
                        maxWidth2 = maxWidth;
                    } else if (MessageObject.isStickerDocument(document)) {
                        this.currentPhotoObject = document.thumb;
                        if (this.currentPhotoObject != null && (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0)) {
                            for (a = 0; a < document.attributes.size(); a++) {
                                attribute = (DocumentAttribute) document.attributes.get(a);
                                if (!(attribute instanceof TL_documentAttributeImageSize)) {
                                    this.currentPhotoObject.w = attribute.w;
                                    this.currentPhotoObject.h = attribute.h;
                                    break;
                                }
                            }
                            if (this.currentPhotoObject.w == 0 || this.currentPhotoObject.h == 0) {
                                photoSize = this.currentPhotoObject;
                                photoSize2 = this.currentPhotoObject;
                                dp = AndroidUtilities.dp(150.0f);
                                photoSize2.h = dp;
                                photoSize.w = dp;
                            }
                        }
                        this.documentAttach = document;
                        this.documentAttachType = 6;
                        maxWidth2 = maxWidth;
                    } else {
                        calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                        if (MessageObject.isStickerDocument(document)) {
                            if (this.backgroundWidth < AndroidUtilities.dp(20.0f) + maxWidth) {
                                this.backgroundWidth = AndroidUtilities.dp(20.0f) + maxWidth;
                            }
                            if (!MessageObject.isVoiceDocument(document)) {
                                createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                this.totalHeight += AndroidUtilities.dp(44.0f);
                                this.linkPreviewHeight += AndroidUtilities.dp(44.0f);
                                calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                maxWidth2 = maxWidth;
                            } else if (MessageObject.isMusicDocument(document)) {
                                durationWidth = createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(10.0f), messageObject);
                                this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                this.totalHeight += AndroidUtilities.dp(56.0f);
                                this.linkPreviewHeight += AndroidUtilities.dp(56.0f);
                                maxWidth -= AndroidUtilities.dp(86.0f);
                                maxChildWidth = Math.max(maxChildWidth, (durationWidth + additinalWidth) + AndroidUtilities.dp(94.0f));
                                if (this.songLayout != null && this.songLayout.getLineCount() > 0) {
                                    maxChildWidth = (int) Math.max((float) maxChildWidth, (this.songLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                }
                                if (this.performerLayout != null && this.performerLayout.getLineCount() > 0) {
                                    maxChildWidth = (int) Math.max((float) maxChildWidth, (this.performerLayout.getLineWidth(0) + ((float) additinalWidth)) + ((float) AndroidUtilities.dp(86.0f)));
                                }
                                calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                                maxWidth2 = maxWidth;
                            } else {
                                createDocumentLayout(this.backgroundWidth - AndroidUtilities.dp(168.0f), messageObject);
                                this.drawImageButton = true;
                                if (this.drawPhotoImage) {
                                    this.totalHeight += AndroidUtilities.dp(100.0f);
                                    this.linkPreviewHeight += AndroidUtilities.dp(86.0f);
                                    this.photoImage.setImageCoords(0, this.totalHeight + this.namesOffset, AndroidUtilities.dp(86.0f), AndroidUtilities.dp(86.0f));
                                    maxWidth2 = maxWidth;
                                } else {
                                    this.mediaOffsetY = (this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0f)) + this.linkPreviewHeight;
                                    this.photoImage.setImageCoords(0, (this.totalHeight + this.namesOffset) - AndroidUtilities.dp(14.0f), AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                                    this.totalHeight += AndroidUtilities.dp(64.0f);
                                    this.linkPreviewHeight += AndroidUtilities.dp(50.0f);
                                    maxWidth2 = maxWidth;
                                }
                            }
                        }
                        maxWidth2 = maxWidth;
                    }
                    if (!(this.documentAttachType == 5 || this.documentAttachType == 3 || this.documentAttachType == 1)) {
                        if (this.currentPhotoObject == null) {
                            if (type != null) {
                                if (type.equals("photo")) {
                                    if (!type.equals("document") || this.documentAttachType == 6) {
                                        if (type.equals("gif")) {
                                        }
                                    }
                                }
                                z = true;
                                this.drawImageButton = z;
                                if (this.linkPreviewHeight != 0) {
                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                }
                                if (this.documentAttachType == 6) {
                                    if (AndroidUtilities.isTablet()) {
                                        maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                    } else {
                                        maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                    }
                                }
                                maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                this.currentPhotoObject.size = -1;
                                if (this.currentPhotoObjectThumb != null) {
                                    this.currentPhotoObjectThumb.size = -1;
                                }
                                if (!smallImage) {
                                    height = maxPhotoWidth;
                                    width = maxPhotoWidth;
                                } else if (this.hasGamePreview) {
                                    scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                    width = (int) (((float) 640) / scale);
                                    height = (int) (((float) 360) / scale);
                                } else {
                                    width = this.currentPhotoObject.w;
                                    scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                    width = (int) (((float) width) / scale);
                                    height = (int) (((float) this.currentPhotoObject.h) / scale);
                                    if ((site_name == null || !(site_name == null || site_name.toLowerCase().equals("instagram") || this.documentAttachType != 0)) && height > AndroidUtilities.displaySize.y / 3) {
                                        height = AndroidUtilities.displaySize.y / 3;
                                    }
                                }
                                if (this.isSmallImage) {
                                    if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                        this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                        this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                    }
                                    this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                } else {
                                    this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                    this.linkPreviewHeight += height;
                                }
                                this.photoImage.setImageCoords(0, 0, width, height);
                                this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                if (this.documentAttachType != 6) {
                                    imageReceiver = this.photoImage;
                                    tLObject = this.documentAttach;
                                    str = this.currentPhotoFilter;
                                    if (this.currentPhotoObject == null) {
                                        fileLocation = this.currentPhotoObject.location;
                                    } else {
                                        fileLocation = null;
                                    }
                                    imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                                } else if (this.documentAttachType != 4) {
                                    this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                } else if (this.documentAttachType != 2) {
                                    photoExist = messageObject.mediaExists;
                                    fileName = FileLoader.getAttachFileName(document);
                                    if (this.hasGamePreview || photoExist || MediaController.getInstance().canDownloadMedia(32) || FileLoader.getInstance().isLoadingFile(fileName)) {
                                        this.photoNotSet = false;
                                        this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                                    } else {
                                        this.photoNotSet = true;
                                        this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                                    }
                                } else {
                                    photoExist = messageObject.mediaExists;
                                    fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                    if (this.hasGamePreview || photoExist || MediaController.getInstance().canDownloadMedia(1) || FileLoader.getInstance().isLoadingFile(fileName)) {
                                        this.photoNotSet = false;
                                        if (this.currentPhotoObjectThumb == null) {
                                        }
                                        this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                    } else {
                                        this.photoNotSet = true;
                                        if (this.currentPhotoObjectThumb != null) {
                                            this.photoImage.setImage(null, null, this.currentPhotoObjectThumb.location, String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)}), 0, null, false);
                                        } else {
                                            this.photoImage.setImageBitmap((Drawable) null);
                                        }
                                    }
                                }
                                this.drawPhotoImage = true;
                                if (type != null) {
                                    if (type.equals(MimeTypes.BASE_TYPE_VIDEO) && duration != 0) {
                                        seconds = duration - ((duration / 60) * 60);
                                        str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                        this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                        this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                    }
                                }
                                if (this.hasGamePreview) {
                                    str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                    this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                    this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                                }
                            }
                            z = false;
                            this.drawImageButton = z;
                            if (this.linkPreviewHeight != 0) {
                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                this.totalHeight += AndroidUtilities.dp(2.0f);
                            }
                            if (this.documentAttachType == 6) {
                                if (AndroidUtilities.isTablet()) {
                                    maxPhotoWidth = (int) (((float) AndroidUtilities.displaySize.x) * 0.5f);
                                } else {
                                    maxPhotoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                                }
                            }
                            maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                            this.currentPhotoObject.size = -1;
                            if (this.currentPhotoObjectThumb != null) {
                                this.currentPhotoObjectThumb.size = -1;
                            }
                            if (!smallImage) {
                                height = maxPhotoWidth;
                                width = maxPhotoWidth;
                            } else if (this.hasGamePreview) {
                                width = this.currentPhotoObject.w;
                                scale = ((float) width) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                width = (int) (((float) width) / scale);
                                height = (int) (((float) this.currentPhotoObject.h) / scale);
                                height = AndroidUtilities.displaySize.y / 3;
                            } else {
                                scale = ((float) 640) / ((float) (maxPhotoWidth - AndroidUtilities.dp(2.0f)));
                                width = (int) (((float) 640) / scale);
                                height = (int) (((float) 360) / scale);
                            }
                            if (this.isSmallImage) {
                                this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                this.linkPreviewHeight += height;
                            } else {
                                if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                    this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                    this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                }
                                this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                            }
                            this.photoImage.setImageCoords(0, 0, width, height);
                            this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                            this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                            if (this.documentAttachType != 6) {
                                imageReceiver = this.photoImage;
                                tLObject = this.documentAttach;
                                str = this.currentPhotoFilter;
                                if (this.currentPhotoObject == null) {
                                    fileLocation = null;
                                } else {
                                    fileLocation = this.currentPhotoObject.location;
                                }
                                imageReceiver.setImage(tLObject, null, str, null, fileLocation, "b1", this.documentAttach.size, "webp", true);
                            } else if (this.documentAttachType != 4) {
                                this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                            } else if (this.documentAttachType != 2) {
                                photoExist = messageObject.mediaExists;
                                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                if (this.hasGamePreview) {
                                }
                                this.photoNotSet = false;
                                if (this.currentPhotoObjectThumb == null) {
                                }
                                this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                            } else {
                                photoExist = messageObject.mediaExists;
                                fileName = FileLoader.getAttachFileName(document);
                                if (this.hasGamePreview) {
                                }
                                this.photoNotSet = false;
                                this.photoImage.setImage(document, null, this.currentPhotoObject.location, this.currentPhotoFilter, document.size, null, false);
                            }
                            this.drawPhotoImage = true;
                            if (type != null) {
                                seconds = duration - ((duration / 60) * 60);
                                str2 = String.format("%d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds)});
                                this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str2));
                                this.videoInfoLayout = new StaticLayout(str2, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            }
                            if (this.hasGamePreview) {
                                str2 = LocaleController.getString("AttachGame", R.string.AttachGame).toUpperCase();
                                this.durationWidth = (int) Math.ceil((double) gamePaint.measureText(str2));
                                this.videoInfoLayout = new StaticLayout(str2, gamePaint, this.durationWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            }
                        } else {
                            this.photoImage.setImageBitmap((Drawable) null);
                            this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                            this.totalHeight += AndroidUtilities.dp(4.0f);
                        }
                        if (this.hasGamePreview && messageObject.textHeight != 0) {
                            this.linkPreviewHeight += messageObject.textHeight + AndroidUtilities.dp(6.0f);
                            this.totalHeight += AndroidUtilities.dp(4.0f);
                        }
                        calcBackgroundWidth(maxWidth2, timeMore, maxChildWidth);
                    }
                    maxWidth = maxWidth2;
                } else {
                    this.photoImage.setImageBitmap((Drawable) null);
                    calcBackgroundWidth(maxWidth, timeMore, maxChildWidth);
                }
            } else if (messageObject.type == 12) {
                Drawable drawable;
                this.drawName = false;
                this.drawForwardedName = true;
                this.drawPhotoImage = true;
                this.photoImage.setRoundRadius(AndroidUtilities.dp(22.0f));
                if (AndroidUtilities.isTablet()) {
                    dp2 = AndroidUtilities.getMinTabletSide();
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                } else {
                    dp2 = AndroidUtilities.displaySize.x;
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                }
                this.availableTimeWidth = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                User user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.media.user_id));
                maxWidth = getMaxNameWidth() - AndroidUtilities.dp(110.0f);
                if (maxWidth < 0) {
                    maxWidth2 = AndroidUtilities.dp(10.0f);
                } else {
                    maxWidth2 = maxWidth;
                }
                TLObject currentPhoto = null;
                if (user != null) {
                    if (user.photo != null) {
                        currentPhoto = user.photo.photo_small;
                    }
                    this.contactAvatarDrawable.setInfo(user);
                }
                ImageReceiver imageReceiver2 = this.photoImage;
                String str3 = "50_50";
                if (user != null) {
                    drawable = this.contactAvatarDrawable;
                } else {
                    drawable = Theme.contactDrawable[messageObject.isOutOwner() ? 1 : 0];
                }
                imageReceiver2.setImage(currentPhoto, str3, drawable, null, false);
                String phone = messageObject.messageOwner.media.phone_number;
                if (phone == null || phone.length() == 0) {
                    phone = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                } else {
                    phone = PhoneFormat.getInstance().format(phone);
                }
                CharSequence currentNameString = ContactsController.formatName(messageObject.messageOwner.media.first_name, messageObject.messageOwner.media.last_name).replace('\n', ' ');
                if (currentNameString.length() == 0) {
                    currentNameString = phone;
                }
                this.titleLayout = new StaticLayout(TextUtils.ellipsize(currentNameString, contactNamePaint, (float) maxWidth2, TruncateAt.END), contactNamePaint, maxWidth2 + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                this.docTitleLayout = new StaticLayout(TextUtils.ellipsize(phone.replace('\n', ' '), contactPhonePaint, (float) maxWidth2, TruncateAt.END), contactPhonePaint, maxWidth2 + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                setMessageObjectInternal(messageObject);
                if (this.drawForwardedName && messageObject.isForwarded()) {
                    this.namesOffset += AndroidUtilities.dp(5.0f);
                } else if (this.drawNameLayout && messageObject.messageOwner.reply_to_msg_id == 0) {
                    this.namesOffset += AndroidUtilities.dp(7.0f);
                }
                this.totalHeight = AndroidUtilities.dp(70.0f) + this.namesOffset;
                if (this.docTitleLayout.getLineCount() > 0 && (this.backgroundWidth - AndroidUtilities.dp(110.0f)) - ((int) Math.ceil((double) this.docTitleLayout.getLineWidth(0))) < this.timeWidth) {
                    this.totalHeight += AndroidUtilities.dp(8.0f);
                }
            } else if (messageObject.type == 2) {
                this.drawForwardedName = true;
                if (AndroidUtilities.isTablet()) {
                    dp2 = AndroidUtilities.getMinTabletSide();
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                } else {
                    dp2 = AndroidUtilities.displaySize.x;
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                }
                createDocumentLayout(this.backgroundWidth, messageObject);
                setMessageObjectInternal(messageObject);
                this.totalHeight = AndroidUtilities.dp(70.0f) + this.namesOffset;
            } else if (messageObject.type == 14) {
                if (AndroidUtilities.isTablet()) {
                    dp2 = AndroidUtilities.getMinTabletSide();
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                } else {
                    dp2 = AndroidUtilities.displaySize.x;
                    f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                    this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                }
                createDocumentLayout(this.backgroundWidth, messageObject);
                setMessageObjectInternal(messageObject);
                this.totalHeight = AndroidUtilities.dp(82.0f) + this.namesOffset;
            } else {
                z = (messageObject.messageOwner.fwd_from == null || messageObject.type == 13) ? false : true;
                this.drawForwardedName = z;
                this.mediaBackground = messageObject.type != 9;
                this.drawImageButton = true;
                this.drawPhotoImage = true;
                int photoWidth = 0;
                int i2 = 0;
                int additionHeight = 0;
                if (!(messageObject.audioProgress == 2.0f || MediaController.getInstance().canAutoplayGifs() || messageObject.type != 8)) {
                    messageObject.audioProgress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                }
                this.photoImage.setAllowStartAnimation(messageObject.audioProgress == 0.0f);
                this.photoImage.setForcePreview(messageObject.isSecretPhoto());
                if (messageObject.type == 9) {
                    if (AndroidUtilities.isTablet()) {
                        dp2 = AndroidUtilities.getMinTabletSide();
                        f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                        this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                    } else {
                        dp2 = AndroidUtilities.displaySize.x;
                        f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                        this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                    }
                    if (checkNeedDrawShareButton(messageObject)) {
                        this.backgroundWidth -= AndroidUtilities.dp(20.0f);
                    }
                    maxWidth = this.backgroundWidth - AndroidUtilities.dp(138.0f);
                    createDocumentLayout(maxWidth, messageObject);
                    if (!TextUtils.isEmpty(messageObject.caption)) {
                        maxWidth += AndroidUtilities.dp(86.0f);
                    }
                    if (this.drawPhotoImage) {
                        photoWidth = AndroidUtilities.dp(86.0f);
                        i2 = AndroidUtilities.dp(86.0f);
                    } else {
                        photoWidth = AndroidUtilities.dp(56.0f);
                        i2 = AndroidUtilities.dp(56.0f);
                        maxWidth += AndroidUtilities.dp(TextUtils.isEmpty(messageObject.caption) ? 51.0f : 21.0f);
                    }
                    this.availableTimeWidth = maxWidth;
                    if (!this.drawPhotoImage && TextUtils.isEmpty(messageObject.caption) && this.infoLayout.getLineCount() > 0) {
                        measureTime(messageObject);
                        if ((this.backgroundWidth - AndroidUtilities.dp(122.0f)) - ((int) Math.ceil((double) this.infoLayout.getLineWidth(0))) < this.timeWidth) {
                            i2 += AndroidUtilities.dp(8.0f);
                        }
                    }
                } else if (messageObject.type == 4) {
                    Drawable drawable2;
                    double lat = messageObject.messageOwner.media.geo.lat;
                    double lon = messageObject.messageOwner.media.geo._long;
                    if (messageObject.messageOwner.media.title == null || messageObject.messageOwner.media.title.length() <= 0) {
                        this.availableTimeWidth = AndroidUtilities.dp(186.0f);
                        photoWidth = AndroidUtilities.dp(200.0f);
                        i2 = AndroidUtilities.dp(100.0f);
                        this.backgroundWidth = AndroidUtilities.dp(12.0f) + photoWidth;
                        this.currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=200x100&maptype=roadmap&scale=%d&markers=color:red|size:mid|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)});
                    } else {
                        if (AndroidUtilities.isTablet()) {
                            dp2 = AndroidUtilities.getMinTabletSide();
                            f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                            this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                        } else {
                            dp2 = AndroidUtilities.displaySize.x;
                            f = (this.isChat && messageObject.isFromUser() && !messageObject.isOutOwner()) ? 102.0f : 50.0f;
                            this.backgroundWidth = Math.min(dp2 - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_VIOLET));
                        }
                        if (checkNeedDrawShareButton(messageObject)) {
                            this.backgroundWidth -= AndroidUtilities.dp(20.0f);
                        }
                        maxWidth = this.backgroundWidth - AndroidUtilities.dp(123.0f);
                        this.docTitleLayout = StaticLayoutEx.createStaticLayout(messageObject.messageOwner.media.title, locationTitlePaint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false, TruncateAt.END, maxWidth, 2);
                        int lineCount = this.docTitleLayout.getLineCount();
                        if (messageObject.messageOwner.media.address == null || messageObject.messageOwner.media.address.length() <= 0) {
                            this.infoLayout = null;
                        } else {
                            this.infoLayout = StaticLayoutEx.createStaticLayout(messageObject.messageOwner.media.address, locationAddressPaint, maxWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false, TruncateAt.END, maxWidth, Math.min(3, 3 - lineCount));
                        }
                        this.mediaBackground = false;
                        this.availableTimeWidth = maxWidth;
                        photoWidth = AndroidUtilities.dp(86.0f);
                        i2 = AndroidUtilities.dp(86.0f);
                        this.currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=72x72&maptype=roadmap&scale=%d&markers=color:red|size:mid|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)});
                    }
                    r18 = this.photoImage;
                    String str4 = this.currentUrl;
                    if (messageObject.isOutOwner()) {
                        drawable2 = Theme.geoOutDrawable;
                    } else {
                        drawable2 = Theme.geoInDrawable;
                    }
                    r18.setImage(str4, null, drawable2, null, 0);
                } else if (messageObject.type == 13) {
                    float maxWidth3;
                    float maxHeight;
                    this.drawBackground = false;
                    for (a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        attribute = (DocumentAttribute) messageObject.messageOwner.media.document.attributes.get(a);
                        if (attribute instanceof TL_documentAttributeImageSize) {
                            photoWidth = attribute.w;
                            i2 = attribute.h;
                            break;
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        maxWidth3 = ((float) AndroidUtilities.getMinTabletSide()) * 0.4f;
                        maxHeight = maxWidth3;
                    } else {
                        maxWidth3 = ((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.5f;
                        maxHeight = maxWidth3;
                    }
                    if (photoWidth == 0) {
                        i2 = (int) maxHeight;
                        photoWidth = i2 + AndroidUtilities.dp(100.0f);
                    }
                    i2 = (int) (((float) i2) * (maxWidth3 / ((float) photoWidth)));
                    photoWidth = (int) maxWidth3;
                    if (((float) i2) > maxHeight) {
                        photoWidth = (int) (((float) photoWidth) * (maxHeight / ((float) i2)));
                        i2 = (int) maxHeight;
                    }
                    this.documentAttachType = 6;
                    this.availableTimeWidth = photoWidth - AndroidUtilities.dp(14.0f);
                    this.backgroundWidth = AndroidUtilities.dp(12.0f) + photoWidth;
                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                    if (messageObject.attachPathExists) {
                        this.photoImage.setImage(null, messageObject.messageOwner.attachPath, String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(photoWidth), Integer.valueOf(i2)}), null, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, "b1", messageObject.messageOwner.media.document.size, "webp", true);
                    } else if (messageObject.messageOwner.media.document.id != 0) {
                        this.photoImage.setImage(messageObject.messageOwner.media.document, null, String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(photoWidth), Integer.valueOf(i2)}), null, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, "b1", messageObject.messageOwner.media.document.size, "webp", true);
                    }
                } else {
                    float hScale;
                    if (AndroidUtilities.isTablet()) {
                        photoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
                        maxPhotoWidth = photoWidth;
                    } else {
                        photoWidth = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
                        maxPhotoWidth = photoWidth;
                    }
                    i2 = photoWidth + AndroidUtilities.dp(100.0f);
                    if (checkNeedDrawShareButton(messageObject)) {
                        maxPhotoWidth -= AndroidUtilities.dp(20.0f);
                        photoWidth -= AndroidUtilities.dp(20.0f);
                    }
                    if (photoWidth > AndroidUtilities.getPhotoSize()) {
                        photoWidth = AndroidUtilities.getPhotoSize();
                    }
                    if (i2 > AndroidUtilities.getPhotoSize()) {
                        i2 = AndroidUtilities.getPhotoSize();
                    }
                    if (messageObject.type == 1) {
                        updateSecretTimeText(messageObject);
                        this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                    } else if (messageObject.type == 3) {
                        createDocumentLayout(0, messageObject);
                        this.photoImage.setNeedsQualityThumb(true);
                        this.photoImage.setShouldGenerateQualityThumb(true);
                        this.photoImage.setParentMessageObject(messageObject);
                    } else if (messageObject.type == 8) {
                        str2 = AndroidUtilities.formatFileSize((long) messageObject.messageOwner.media.document.size);
                        this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(str2));
                        this.infoLayout = new StaticLayout(str2, infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                        this.photoImage.setNeedsQualityThumb(true);
                        this.photoImage.setShouldGenerateQualityThumb(true);
                        this.photoImage.setParentMessageObject(messageObject);
                    }
                    if (messageObject.caption != null) {
                        this.mediaBackground = false;
                    }
                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    int w = 0;
                    int h = 0;
                    if (this.currentPhotoObject != null && this.currentPhotoObject == this.currentPhotoObjectThumb) {
                        this.currentPhotoObjectThumb = null;
                    }
                    if (this.currentPhotoObject != null) {
                        scale = ((float) this.currentPhotoObject.w) / ((float) photoWidth);
                        w = (int) (((float) this.currentPhotoObject.w) / scale);
                        h = (int) (((float) this.currentPhotoObject.h) / scale);
                        if (w == 0) {
                            w = AndroidUtilities.dp(150.0f);
                        }
                        if (h == 0) {
                            h = AndroidUtilities.dp(150.0f);
                        }
                        if (h > i2) {
                            h = i2;
                            w = (int) (((float) w) / (((float) h) / ((float) h)));
                        } else if (h < AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN)) {
                            h = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN);
                            hScale = ((float) this.currentPhotoObject.h) / ((float) h);
                            if (((float) this.currentPhotoObject.w) / hScale < ((float) photoWidth)) {
                                w = (int) (((float) this.currentPhotoObject.w) / hScale);
                            }
                        }
                    }
                    if ((w == 0 || h == 0) && messageObject.type == 8) {
                        a = 0;
                        while (a < messageObject.messageOwner.media.document.attributes.size()) {
                            attribute = (DocumentAttribute) messageObject.messageOwner.media.document.attributes.get(a);
                            if ((attribute instanceof TL_documentAttributeImageSize) || (attribute instanceof TL_documentAttributeVideo)) {
                                scale = ((float) attribute.w) / ((float) photoWidth);
                                w = (int) (((float) attribute.w) / scale);
                                h = (int) (((float) attribute.h) / scale);
                                if (h > i2) {
                                    h = i2;
                                    w = (int) (((float) w) / (((float) h) / ((float) h)));
                                } else if (h < AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN)) {
                                    h = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN);
                                    hScale = ((float) attribute.h) / ((float) h);
                                    if (((float) attribute.w) / hScale < ((float) photoWidth)) {
                                        w = (int) (((float) attribute.w) / hScale);
                                    }
                                }
                            } else {
                                a++;
                            }
                        }
                    }
                    if (w == 0 || h == 0) {
                        h = AndroidUtilities.dp(150.0f);
                        w = h;
                    }
                    if (messageObject.type == 3 && w < this.infoWidth + AndroidUtilities.dp(40.0f)) {
                        w = this.infoWidth + AndroidUtilities.dp(40.0f);
                    }
                    this.availableTimeWidth = maxPhotoWidth - AndroidUtilities.dp(14.0f);
                    measureTime(messageObject);
                    timeWidthTotal = this.timeWidth + AndroidUtilities.dp((float) ((messageObject.isOutOwner() ? 20 : 0) + 14));
                    if (w < timeWidthTotal) {
                        w = timeWidthTotal;
                    }
                    if (messageObject.isSecretPhoto()) {
                        if (AndroidUtilities.isTablet()) {
                            h = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                            w = h;
                        } else {
                            h = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.5f);
                            w = h;
                        }
                    }
                    photoWidth = w;
                    i2 = h;
                    this.backgroundWidth = AndroidUtilities.dp(12.0f) + w;
                    if (!this.mediaBackground) {
                        this.backgroundWidth += AndroidUtilities.dp(9.0f);
                    }
                    if (messageObject.caption != null) {
                        try {
                            this.captionLayout = new StaticLayout(messageObject.caption, MessageObject.getTextPaint(), photoWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            if (this.captionLayout.getLineCount() > 0) {
                                this.captionHeight = this.captionLayout.getHeight();
                                additionHeight = 0 + (this.captionHeight + AndroidUtilities.dp(9.0f));
                                if (((float) (photoWidth - AndroidUtilities.dp(8.0f))) - (this.captionLayout.getLineWidth(this.captionLayout.getLineCount() - 1) + this.captionLayout.getLineLeft(this.captionLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                                    additionHeight += AndroidUtilities.dp(14.0f);
                                }
                            }
                        } catch (Throwable e2222) {
                            FileLog.e("tmessages", e2222);
                        }
                    }
                    this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf((int) (((float) w) / AndroidUtilities.density)), Integer.valueOf((int) (((float) h) / AndroidUtilities.density))});
                    if ((messageObject.photoThumbs != null && messageObject.photoThumbs.size() > 1) || messageObject.type == 3 || messageObject.type == 8) {
                        if (messageObject.isSecretPhoto()) {
                            this.currentPhotoFilter += "_b2";
                        } else {
                            this.currentPhotoFilter += "_b";
                        }
                    }
                    boolean noSize = false;
                    if (messageObject.type == 3 || messageObject.type == 8) {
                        noSize = true;
                    }
                    if (!(this.currentPhotoObject == null || noSize || this.currentPhotoObject.size != 0)) {
                        this.currentPhotoObject.size = -1;
                    }
                    String str5;
                    if (messageObject.type == 1) {
                        if (this.currentPhotoObject != null) {
                            photoExist = true;
                            fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                            if (messageObject.mediaExists) {
                                MediaController.getInstance().removeLoadingFileObserver(this);
                            } else {
                                photoExist = false;
                            }
                            if (photoExist || MediaController.getInstance().canDownloadMedia(1) || FileLoader.getInstance().isLoadingFile(fileName)) {
                                FileLocation fileLocation2;
                                int i3;
                                r18 = this.photoImage;
                                TLObject tLObject2 = this.currentPhotoObject.location;
                                str5 = this.currentPhotoFilter;
                                if (this.currentPhotoObjectThumb != null) {
                                    fileLocation2 = this.currentPhotoObjectThumb.location;
                                } else {
                                    fileLocation2 = null;
                                }
                                String str6 = this.currentPhotoFilter;
                                if (noSize) {
                                    i3 = 0;
                                } else {
                                    i3 = this.currentPhotoObject.size;
                                }
                                r18.setImage(tLObject2, str5, fileLocation2, str6, i3, null, false);
                            } else {
                                this.photoNotSet = true;
                                if (this.currentPhotoObjectThumb != null) {
                                    this.photoImage.setImage(null, null, this.currentPhotoObjectThumb.location, this.currentPhotoFilter, 0, null, false);
                                } else {
                                    this.photoImage.setImageBitmap((Drawable) null);
                                }
                            }
                        } else {
                            this.photoImage.setImageBitmap((BitmapDrawable) null);
                        }
                    } else if (messageObject.type == 8) {
                        fileName = FileLoader.getAttachFileName(messageObject.messageOwner.media.document);
                        int localFile = 0;
                        if (messageObject.attachPathExists) {
                            MediaController.getInstance().removeLoadingFileObserver(this);
                            localFile = 1;
                        } else if (messageObject.mediaExists) {
                            localFile = 2;
                        }
                        if (messageObject.isSending() || (localFile == 0 && !((MediaController.getInstance().canDownloadMedia(32) && MessageObject.isNewGifDocument(messageObject.messageOwner.media.document)) || FileLoader.getInstance().isLoadingFile(fileName)))) {
                            this.photoNotSet = true;
                            this.photoImage.setImage(null, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilter, 0, null, false);
                        } else if (localFile == 1) {
                            FileLocation fileLocation3;
                            r18 = this.photoImage;
                            if (messageObject.isSendError()) {
                                str5 = null;
                            } else {
                                str5 = messageObject.messageOwner.attachPath;
                            }
                            if (this.currentPhotoObject != null) {
                                fileLocation3 = this.currentPhotoObject.location;
                            } else {
                                fileLocation3 = null;
                            }
                            r18.setImage(null, str5, null, null, fileLocation3, this.currentPhotoFilter, 0, null, false);
                        } else {
                            this.photoImage.setImage(messageObject.messageOwner.media.document, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilter, messageObject.messageOwner.media.document.size, null, false);
                        }
                    } else {
                        this.photoImage.setImage(null, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilter, 0, null, false);
                    }
                }
                setMessageObjectInternal(messageObject);
                if (this.drawForwardedName) {
                    this.namesOffset += AndroidUtilities.dp(5.0f);
                } else if (this.drawNameLayout && messageObject.messageOwner.reply_to_msg_id == 0) {
                    this.namesOffset += AndroidUtilities.dp(7.0f);
                }
                invalidate();
                this.photoImage.setImageCoords(0, AndroidUtilities.dp(7.0f) + this.namesOffset, photoWidth, i2);
                this.totalHeight = ((AndroidUtilities.dp(14.0f) + i2) + this.namesOffset) + additionHeight;
            }
            if (!(this.captionLayout != null || messageObject.caption == null || messageObject.type == 13)) {
                width = this.backgroundWidth - AndroidUtilities.dp(31.0f);
                this.captionLayout = new StaticLayout(messageObject.caption, MessageObject.getTextPaint(), width - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                if (this.captionLayout.getLineCount() > 0) {
                    if (messageObject.isOutOwner()) {
                    }
                    timeWidthTotal = this.timeWidth + (messageObject.isOutOwner() ? 0 : AndroidUtilities.dp(20.0f));
                    this.captionHeight = this.captionLayout.getHeight();
                    this.totalHeight += this.captionHeight + AndroidUtilities.dp(9.0f);
                    if (((float) (width - AndroidUtilities.dp(8.0f))) - (this.captionLayout.getLineWidth(this.captionLayout.getLineCount() - 1) + this.captionLayout.getLineLeft(this.captionLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                        this.totalHeight += AndroidUtilities.dp(14.0f);
                        this.captionHeight += AndroidUtilities.dp(14.0f);
                    }
                }
            }
            this.botButtons.clear();
            if (messageIdChanged) {
                this.botButtonsByData.clear();
            }
            if (messageObject.messageOwner.reply_markup instanceof TL_replyInlineMarkup) {
                rows = messageObject.messageOwner.reply_markup.rows.size();
                i = (AndroidUtilities.dp(48.0f) * rows) + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                this.keyboardHeight = i;
                this.substractBackgroundHeight = i;
                this.widthForButtons = this.backgroundWidth;
                fullWidth = false;
                if (messageObject.wantedBotKeyboardWidth > this.widthForButtons) {
                    f = (this.isChat || !messageObject.isFromUser() || messageObject.isOutOwner()) ? 10.0f : 62.0f;
                    maxButtonWidth = -AndroidUtilities.dp(f);
                    if (AndroidUtilities.isTablet()) {
                        maxButtonWidth += AndroidUtilities.getMinTabletSide();
                    } else {
                        maxButtonWidth += Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                    }
                    this.widthForButtons = Math.max(this.backgroundWidth, Math.min(messageObject.wantedBotKeyboardWidth, maxButtonWidth));
                    fullWidth = true;
                }
                maxButtonsWidth = 0;
                for (a = 0; a < rows; a++) {
                    row = (TL_keyboardButtonRow) messageObject.messageOwner.reply_markup.rows.get(a);
                    buttonsCount = row.buttons.size();
                    if (buttonsCount != 0) {
                        dp2 = this.widthForButtons - (AndroidUtilities.dp(5.0f) * (buttonsCount - 1));
                        f = (fullWidth || !this.mediaBackground) ? 9.0f : 0.0f;
                        buttonWidth = ((dp2 - AndroidUtilities.dp(f)) - AndroidUtilities.dp(2.0f)) / buttonsCount;
                        for (b = 0; b < row.buttons.size(); b++) {
                            chatMessageCell = this;
                            botButton = new BotButton();
                            botButton.button = (KeyboardButton) row.buttons.get(b);
                            key = Utilities.bytesToHex(botButton.button.data);
                            oldButton = (BotButton) this.botButtonsByData.get(key);
                            if (oldButton == null) {
                                botButton.progressAlpha = oldButton.progressAlpha;
                                botButton.angle = oldButton.angle;
                                botButton.lastUpdateTime = oldButton.lastUpdateTime;
                            } else {
                                botButton.lastUpdateTime = System.currentTimeMillis();
                            }
                            this.botButtonsByData.put(key, botButton);
                            botButton.x = (AndroidUtilities.dp(5.0f) + buttonWidth) * b;
                            botButton.y = (AndroidUtilities.dp(48.0f) * a) + AndroidUtilities.dp(5.0f);
                            botButton.width = buttonWidth;
                            botButton.height = AndroidUtilities.dp(44.0f);
                            botButton.title = new StaticLayout(TextUtils.ellipsize(Emoji.replaceEmoji(botButton.button.text, botButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false), botButtonPaint, (float) (buttonWidth - AndroidUtilities.dp(10.0f)), TruncateAt.END), botButtonPaint, buttonWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_CENTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                            this.botButtons.add(botButton);
                            if (b != row.buttons.size() - 1) {
                                maxButtonsWidth = Math.max(maxButtonsWidth, botButton.x + botButton.width);
                            }
                        }
                    }
                }
                this.widthForButtons = maxButtonsWidth;
            } else {
                this.substractBackgroundHeight = 0;
                this.keyboardHeight = 0;
            }
        }
        updateWaveform();
        updateButtonState(dataChanged);
    }

    public void requestLayout() {
        if (!this.inLayout) {
            super.requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.currentMessageObject != null && this.currentMessageObject.checkLayout()) {
            this.inLayout = true;
            MessageObject messageObject = this.currentMessageObject;
            this.currentMessageObject = null;
            setMessageObject(messageObject);
            this.inLayout = false;
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.totalHeight + this.keyboardHeight);
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        if (changed || !this.wasLayout) {
            this.layoutWidth = getMeasuredWidth();
            this.layoutHeight = getMeasuredHeight() - this.substractBackgroundHeight;
            if (this.timeTextWidth < 0) {
                this.timeTextWidth = AndroidUtilities.dp(10.0f);
            }
            this.timeLayout = new StaticLayout(this.currentTimeString, timePaint, this.timeTextWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            int dp;
            int dp2;
            if (this.mediaBackground) {
                if (this.currentMessageObject.isOutOwner()) {
                    this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(42.0f);
                } else {
                    dp = (this.backgroundWidth - AndroidUtilities.dp(4.0f)) - this.timeWidth;
                    dp2 = (this.isChat && this.currentMessageObject.isFromUser()) ? AndroidUtilities.dp(48.0f) : 0;
                    this.timeX = dp2 + dp;
                }
            } else if (this.currentMessageObject.isOutOwner()) {
                this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(38.5f);
            } else {
                dp = (this.backgroundWidth - AndroidUtilities.dp(9.0f)) - this.timeWidth;
                if (this.isChat && this.currentMessageObject.isFromUser()) {
                    dp2 = AndroidUtilities.dp(48.0f);
                } else {
                    dp2 = 0;
                }
                this.timeX = dp2 + dp;
            }
            if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                this.viewsLayout = new StaticLayout(this.currentViewsString, timePaint, this.viewsTextWidth, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
            } else {
                this.viewsLayout = null;
            }
            if (this.isAvatarVisible) {
                this.avatarImage.setImageCoords(AndroidUtilities.dp(6.0f), this.layoutHeight - AndroidUtilities.dp(44.0f), AndroidUtilities.dp(42.0f), AndroidUtilities.dp(42.0f));
            }
            this.wasLayout = true;
        }
        if (this.currentMessageObject.type == 0) {
            this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
        }
        if (this.documentAttachType == 3) {
            if (this.currentMessageObject.isOutOwner()) {
                this.seekBarX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(57.0f);
                this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                this.timeAudioX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(67.0f);
            } else if (this.isChat && this.currentMessageObject.isFromUser()) {
                this.seekBarX = AndroidUtilities.dp(114.0f);
                this.buttonX = AndroidUtilities.dp(71.0f);
                this.timeAudioX = AndroidUtilities.dp(124.0f);
            } else {
                this.seekBarX = AndroidUtilities.dp(66.0f);
                this.buttonX = AndroidUtilities.dp(23.0f);
                this.timeAudioX = AndroidUtilities.dp(76.0f);
            }
            if (this.hasLinkPreview) {
                this.seekBarX += AndroidUtilities.dp(10.0f);
                this.buttonX += AndroidUtilities.dp(10.0f);
                this.timeAudioX += AndroidUtilities.dp(10.0f);
            }
            this.seekBarWaveform.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 92)), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE));
            this.seekBar.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 72)), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE));
            this.seekBarY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
            this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
            updateAudioProgress();
        } else if (this.documentAttachType == 5) {
            if (this.currentMessageObject.isOutOwner()) {
                this.seekBarX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(56.0f);
                this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                this.timeAudioX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(67.0f);
            } else if (this.isChat && this.currentMessageObject.isFromUser()) {
                this.seekBarX = AndroidUtilities.dp(113.0f);
                this.buttonX = AndroidUtilities.dp(71.0f);
                this.timeAudioX = AndroidUtilities.dp(124.0f);
            } else {
                this.seekBarX = AndroidUtilities.dp(65.0f);
                this.buttonX = AndroidUtilities.dp(23.0f);
                this.timeAudioX = AndroidUtilities.dp(76.0f);
            }
            if (this.hasLinkPreview) {
                this.seekBarX += AndroidUtilities.dp(10.0f);
                this.buttonX += AndroidUtilities.dp(10.0f);
                this.timeAudioX += AndroidUtilities.dp(10.0f);
            }
            this.seekBar.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 65)), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE));
            this.seekBarY = (AndroidUtilities.dp(29.0f) + this.namesOffset) + this.mediaOffsetY;
            this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
            updateAudioProgress();
        } else if (this.documentAttachType == 1 && !this.drawPhotoImage) {
            if (this.currentMessageObject.isOutOwner()) {
                this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
            } else if (this.isChat && this.currentMessageObject.isFromUser()) {
                this.buttonX = AndroidUtilities.dp(71.0f);
            } else {
                this.buttonX = AndroidUtilities.dp(23.0f);
            }
            if (this.hasLinkPreview) {
                this.buttonX += AndroidUtilities.dp(10.0f);
            }
            this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
            this.photoImage.setImageCoords(this.buttonX - AndroidUtilities.dp(10.0f), this.buttonY - AndroidUtilities.dp(10.0f), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
        } else if (this.currentMessageObject.type == 12) {
            if (this.currentMessageObject.isOutOwner()) {
                x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
            } else if (this.isChat && this.currentMessageObject.isFromUser()) {
                x = AndroidUtilities.dp(72.0f);
            } else {
                x = AndroidUtilities.dp(23.0f);
            }
            this.photoImage.setImageCoords(x, AndroidUtilities.dp(13.0f) + this.namesOffset, AndroidUtilities.dp(44.0f), AndroidUtilities.dp(44.0f));
        } else {
            if (this.currentMessageObject.isOutOwner()) {
                if (this.mediaBackground) {
                    x = (this.layoutWidth - this.backgroundWidth) - AndroidUtilities.dp(3.0f);
                } else {
                    x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(6.0f);
                }
            } else if (this.isChat && this.currentMessageObject.isFromUser()) {
                x = AndroidUtilities.dp(63.0f);
            } else {
                x = AndroidUtilities.dp(15.0f);
            }
            this.photoImage.setImageCoords(x, this.photoImage.getImageY(), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
            this.buttonX = (int) (((float) x) + (((float) (this.photoImage.getImageWidth() - AndroidUtilities.dp(48.0f))) / 2.0f));
            this.buttonY = ((int) (((float) AndroidUtilities.dp(7.0f)) + (((float) (this.photoImage.getImageHeight() - AndroidUtilities.dp(48.0f))) / 2.0f))) + this.namesOffset;
            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0f), this.buttonY + AndroidUtilities.dp(48.0f));
            this.deleteProgressRect.set((float) (this.buttonX + AndroidUtilities.dp(3.0f)), (float) (this.buttonY + AndroidUtilities.dp(3.0f)), (float) (this.buttonX + AndroidUtilities.dp(45.0f)), (float) (this.buttonY + AndroidUtilities.dp(45.0f)));
        }
    }

    private void drawContent(Canvas canvas) {
        int a;
        int b;
        int x;
        int y;
        int imageX;
        RadialProgress radialProgress;
        int i;
        Drawable menuDrawable;
        if (this.needNewVisiblePart && this.currentMessageObject.type == 0) {
            getLocalVisibleRect(this.scrollRect);
            setVisiblePart(this.scrollRect.top, this.scrollRect.bottom - this.scrollRect.top);
            this.needNewVisiblePart = false;
        }
        this.photoImage.setPressed(isDrawSelectedBackground());
        this.photoImage.setVisible(!PhotoViewer.getInstance().isShowingImage(this.currentMessageObject), false);
        this.radialProgress.setHideCurrentDrawable(false);
        this.radialProgress.setProgressColor(-1);
        boolean imageDrawn = false;
        if (this.currentMessageObject.type == 0) {
            if (this.currentMessageObject.isOutOwner()) {
                this.textX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
            } else {
                this.textX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(17.0f);
            }
            if (this.hasGamePreview) {
                this.textX += AndroidUtilities.dp(11.0f);
                this.textY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                if (this.siteNameLayout != null) {
                    this.textY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
            } else {
                this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            }
            if (!(this.currentMessageObject.textLayoutBlocks == null || this.currentMessageObject.textLayoutBlocks.isEmpty() || this.firstVisibleBlockNum < 0)) {
                a = this.firstVisibleBlockNum;
                while (a <= this.lastVisibleBlockNum && a < this.currentMessageObject.textLayoutBlocks.size()) {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                    canvas.save();
                    canvas.translate((float) (this.textX - ((int) Math.ceil((double) block.textXOffset))), ((float) this.textY) + block.textYOffset);
                    if (this.pressedLink != null && a == this.linkBlockNum) {
                        for (b = 0; b < this.urlPath.size(); b++) {
                            canvas.drawPath((Path) this.urlPath.get(b), urlPaint);
                        }
                    }
                    if (a == this.linkSelectionBlockNum && !this.urlPathSelection.isEmpty()) {
                        for (b = 0; b < this.urlPathSelection.size(); b++) {
                            canvas.drawPath((Path) this.urlPathSelection.get(b), urlSelectionPaint);
                        }
                    }
                    try {
                        block.textLayout.draw(canvas);
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                    canvas.restore();
                    a++;
                }
            }
            if (this.hasLinkPreview || this.hasGamePreview) {
                int startY;
                int linkX;
                if (this.hasGamePreview) {
                    startY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                    linkX = this.textX - AndroidUtilities.dp(10.0f);
                } else {
                    startY = (this.textY + this.currentMessageObject.textHeight) + AndroidUtilities.dp(8.0f);
                    linkX = this.textX + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                }
                int linkPreviewY = startY;
                int smallImageStartY = 0;
                replyLinePaint.setColor(this.currentMessageObject.isOutOwner() ? -7812741 : -9390872);
                canvas.drawRect((float) linkX, (float) (linkPreviewY - AndroidUtilities.dp(3.0f)), (float) (AndroidUtilities.dp(2.0f) + linkX), (float) ((this.linkPreviewHeight + linkPreviewY) + AndroidUtilities.dp(3.0f)), replyLinePaint);
                if (this.siteNameLayout != null) {
                    replyNamePaint.setColor(this.currentMessageObject.isOutOwner() ? -11162801 : -12940081);
                    canvas.save();
                    canvas.translate((float) (AndroidUtilities.dp(10.0f) + linkX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.siteNameLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
                if (this.hasGamePreview && this.currentMessageObject.textHeight != 0) {
                    startY += this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0f);
                    linkPreviewY += this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0f);
                }
                replyNamePaint.setColor(-16777216);
                replyTextPaint.setColor(-16777216);
                if (this.titleLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    smallImageStartY = linkPreviewY - AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    canvas.save();
                    canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.titleX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.titleLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
                }
                if (this.authorLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    }
                    canvas.save();
                    canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.authorX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.authorLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                }
                if (this.descriptionLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    }
                    this.descriptionY = linkPreviewY - AndroidUtilities.dp(3.0f);
                    canvas.save();
                    canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.descriptionX), (float) this.descriptionY);
                    if (this.pressedLink != null && this.linkBlockNum == -10) {
                        for (b = 0; b < this.urlPath.size(); b++) {
                            canvas.drawPath((Path) this.urlPath.get(b), urlPaint);
                        }
                    }
                    this.descriptionLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                }
                if (this.drawPhotoImage) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (this.isSmallImage) {
                        this.photoImage.setImageCoords((this.backgroundWidth + linkX) - AndroidUtilities.dp(81.0f), smallImageStartY, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                    } else {
                        this.photoImage.setImageCoords(AndroidUtilities.dp(10.0f) + linkX, linkPreviewY, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                        if (this.drawImageButton) {
                            int size = AndroidUtilities.dp(48.0f);
                            this.buttonX = (int) (((float) this.photoImage.getImageX()) + (((float) (this.photoImage.getImageWidth() - size)) / 2.0f));
                            this.buttonY = (int) (((float) this.photoImage.getImageY()) + (((float) (this.photoImage.getImageHeight() - size)) / 2.0f));
                            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0f), this.buttonY + AndroidUtilities.dp(48.0f));
                        }
                    }
                    imageDrawn = this.photoImage.draw(canvas);
                    if (this.videoInfoLayout != null) {
                        if (this.hasGamePreview) {
                            x = this.photoImage.getImageX() + AndroidUtilities.dp(8.5f);
                            y = this.photoImage.getImageY() + AndroidUtilities.dp(6.0f);
                            Theme.timeBackgroundDrawable.setBounds(x - AndroidUtilities.dp(4.0f), y - AndroidUtilities.dp(1.5f), (this.durationWidth + x) + AndroidUtilities.dp(4.0f), AndroidUtilities.dp(16.5f) + y);
                            Theme.timeBackgroundDrawable.draw(canvas);
                        } else {
                            x = ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(8.0f)) - this.durationWidth;
                            y = (this.photoImage.getImageY() + this.photoImage.getImageHeight()) - AndroidUtilities.dp(19.0f);
                            Theme.timeBackgroundDrawable.setBounds(x - AndroidUtilities.dp(4.0f), y - AndroidUtilities.dp(1.5f), (this.durationWidth + x) + AndroidUtilities.dp(4.0f), AndroidUtilities.dp(14.5f) + y);
                            Theme.timeBackgroundDrawable.draw(canvas);
                        }
                        canvas.save();
                        canvas.translate((float) x, (float) y);
                        this.videoInfoLayout.draw(canvas);
                        canvas.restore();
                    }
                }
            }
            this.drawTime = true;
        } else if (this.drawPhotoImage) {
            imageDrawn = this.photoImage.draw(canvas);
            this.drawTime = this.photoImage.getVisible();
        }
        if (this.buttonState == -1 && this.currentMessageObject.isSecretPhoto()) {
            int drawable = 4;
            if (this.currentMessageObject.messageOwner.destroyTime != 0) {
                if (this.currentMessageObject.isOutOwner()) {
                    drawable = 6;
                } else {
                    drawable = 5;
                }
            }
            setDrawableBounds(Theme.photoStatesDrawables[drawable][this.buttonPressed], this.buttonX, this.buttonY);
            Theme.photoStatesDrawables[drawable][this.buttonPressed].setAlpha((int) (255.0f * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - this.radialProgress.getAlpha())));
            Theme.photoStatesDrawables[drawable][this.buttonPressed].draw(canvas);
            if (!(this.currentMessageObject.isOutOwner() || this.currentMessageObject.messageOwner.destroyTime == 0)) {
                float progress = ((float) Math.max(0, (((long) this.currentMessageObject.messageOwner.destroyTime) * 1000) - (System.currentTimeMillis() + ((long) (ConnectionsManager.getInstance().getTimeDifference() * 1000))))) / (((float) this.currentMessageObject.messageOwner.ttl) * 1000.0f);
                canvas.drawArc(this.deleteProgressRect, -90.0f, -360.0f * progress, true, deleteProgressPaint);
                if (progress != 0.0f) {
                    int offset = AndroidUtilities.dp(2.0f);
                    invalidate(((int) this.deleteProgressRect.left) - offset, ((int) this.deleteProgressRect.top) - offset, ((int) this.deleteProgressRect.right) + (offset * 2), ((int) this.deleteProgressRect.bottom) + (offset * 2));
                }
                updateSecretTimeText(this.currentMessageObject);
            }
        }
        if (this.documentAttachType == 2 || this.currentMessageObject.type == 8) {
            if (this.photoImage.getVisible() && !this.hasGamePreview) {
                Drawable drawable2 = Theme.docMenuDrawable[3];
                imageX = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(14.0f);
                this.otherX = imageX;
                int imageY = this.photoImage.getImageY() + AndroidUtilities.dp(8.1f);
                this.otherY = imageY;
                setDrawableBounds(drawable2, imageX, imageY);
                Theme.docMenuDrawable[3].draw(canvas);
            }
        } else if (this.documentAttachType == 5) {
            if (this.currentMessageObject.isOutOwner()) {
                audioTitlePaint.setColor(-11162801);
                audioPerformerPaint.setColor(-13286860);
                audioTimePaint.setColor(-10112933);
                radialProgress = this.radialProgress;
                i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.MSG_OUT_AUDIO_SELECTED_PROGRESS_COLOR : Theme.MSG_OUT_AUDIO_PROGRESS_COLOR;
                radialProgress.setProgressColor(i);
            } else {
                audioTitlePaint.setColor(-11625772);
                audioPerformerPaint.setColor(-13683656);
                audioTimePaint.setColor(-6182221);
                radialProgress = this.radialProgress;
                i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.MSG_IN_AUDIO_SELECTED_PROGRESS_COLOR : -1;
                radialProgress.setProgressColor(i);
            }
            this.radialProgress.draw(canvas);
            canvas.save();
            canvas.translate((float) (this.timeAudioX + this.songX), (float) ((AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY));
            this.songLayout.draw(canvas);
            canvas.restore();
            canvas.save();
            if (MediaController.getInstance().isPlayingAudio(this.currentMessageObject)) {
                canvas.translate((float) this.seekBarX, (float) this.seekBarY);
                this.seekBar.draw(canvas);
            } else {
                canvas.translate((float) (this.timeAudioX + this.performerX), (float) ((AndroidUtilities.dp(35.0f) + this.namesOffset) + this.mediaOffsetY));
                this.performerLayout.draw(canvas);
            }
            canvas.restore();
            canvas.save();
            canvas.translate((float) this.timeAudioX, (float) ((AndroidUtilities.dp(57.0f) + this.namesOffset) + this.mediaOffsetY));
            this.durationLayout.draw(canvas);
            canvas.restore();
            if (this.currentMessageObject.isOutOwner()) {
                menuDrawable = Theme.docMenuDrawable[1];
            } else {
                menuDrawable = Theme.docMenuDrawable[isDrawSelectedBackground() ? 2 : 0];
            }
            i = (this.backgroundWidth + this.buttonX) - AndroidUtilities.dp(this.currentMessageObject.type == 0 ? 58.0f : 48.0f);
            this.otherX = i;
            imageX = this.buttonY - AndroidUtilities.dp(5.0f);
            this.otherY = imageX;
            setDrawableBounds(menuDrawable, i, imageX);
            menuDrawable.draw(canvas);
        } else if (this.documentAttachType == 3) {
            if (this.currentMessageObject.isOutOwner()) {
                audioTimePaint.setColor(isDrawSelectedBackground() ? -10112933 : -10112933);
                radialProgress = this.radialProgress;
                i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.MSG_OUT_AUDIO_SELECTED_PROGRESS_COLOR : Theme.MSG_OUT_AUDIO_PROGRESS_COLOR;
                radialProgress.setProgressColor(i);
            } else {
                audioTimePaint.setColor(isDrawSelectedBackground() ? -7752511 : -6182221);
                radialProgress = this.radialProgress;
                i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.MSG_IN_AUDIO_SELECTED_PROGRESS_COLOR : -1;
                radialProgress.setProgressColor(i);
            }
            this.radialProgress.draw(canvas);
            canvas.save();
            if (this.useSeekBarWaweform) {
                canvas.translate((float) (this.seekBarX + AndroidUtilities.dp(13.0f)), (float) this.seekBarY);
                this.seekBarWaveform.draw(canvas);
            } else {
                canvas.translate((float) this.seekBarX, (float) this.seekBarY);
                this.seekBar.draw(canvas);
            }
            canvas.restore();
            canvas.save();
            canvas.translate((float) this.timeAudioX, (float) ((AndroidUtilities.dp(44.0f) + this.namesOffset) + this.mediaOffsetY));
            this.durationLayout.draw(canvas);
            canvas.restore();
            if (this.currentMessageObject.type != 0 && this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.isContentUnread()) {
                docBackPaint.setColor(this.currentMessageObject.isOutOwner() ? -8863118 : -9259544);
                canvas.drawCircle((float) ((this.timeAudioX + this.timeWidthAudio) + AndroidUtilities.dp(6.0f)), (float) ((AndroidUtilities.dp(51.0f) + this.namesOffset) + this.mediaOffsetY), (float) AndroidUtilities.dp(3.0f), docBackPaint);
            }
        }
        if (this.currentMessageObject.type == 1 || this.documentAttachType == 4) {
            if (this.photoImage.getVisible()) {
                if (this.documentAttachType == 4) {
                    drawable2 = Theme.docMenuDrawable[3];
                    imageX = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(14.0f);
                    this.otherX = imageX;
                    imageY = this.photoImage.getImageY() + AndroidUtilities.dp(8.1f);
                    this.otherY = imageY;
                    setDrawableBounds(drawable2, imageX, imageY);
                    Theme.docMenuDrawable[3].draw(canvas);
                }
                if (this.infoLayout != null && (this.buttonState == 1 || this.buttonState == 0 || this.buttonState == 3 || this.currentMessageObject.isSecretPhoto())) {
                    infoPaint.setColor(-1);
                    setDrawableBounds(Theme.timeBackgroundDrawable, AndroidUtilities.dp(4.0f) + this.photoImage.getImageX(), AndroidUtilities.dp(4.0f) + this.photoImage.getImageY(), AndroidUtilities.dp(8.0f) + this.infoWidth, AndroidUtilities.dp(16.5f));
                    Theme.timeBackgroundDrawable.draw(canvas);
                    canvas.save();
                    canvas.translate((float) (this.photoImage.getImageX() + AndroidUtilities.dp(8.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(5.5f)));
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                }
            }
        } else if (this.currentMessageObject.type == 4) {
            if (this.docTitleLayout != null) {
                if (this.currentMessageObject.isOutOwner()) {
                    locationTitlePaint.setColor(-11162801);
                    locationAddressPaint.setColor(isDrawSelectedBackground() ? -10112933 : -10112933);
                } else {
                    locationTitlePaint.setColor(-11625772);
                    locationAddressPaint.setColor(isDrawSelectedBackground() ? -7752511 : -6182221);
                }
                canvas.save();
                canvas.translate((float) (((this.docTitleOffsetX + this.photoImage.getImageX()) + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(8.0f)));
                this.docTitleLayout.draw(canvas);
                canvas.restore();
                if (this.infoLayout != null) {
                    canvas.save();
                    canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) ((this.photoImage.getImageY() + this.docTitleLayout.getLineBottom(this.docTitleLayout.getLineCount() - 1)) + AndroidUtilities.dp(13.0f)));
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                }
            }
        } else if (this.currentMessageObject.type == 12) {
            contactNamePaint.setColor(this.currentMessageObject.isOutOwner() ? -11162801 : -11625772);
            contactPhonePaint.setColor(this.currentMessageObject.isOutOwner() ? -13286860 : -13683656);
            if (this.titleLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(16.0f) + this.namesOffset));
                this.titleLayout.draw(canvas);
                canvas.restore();
            }
            if (this.docTitleLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(39.0f) + this.namesOffset));
                this.docTitleLayout.draw(canvas);
                canvas.restore();
            }
            if (this.currentMessageObject.isOutOwner()) {
                menuDrawable = Theme.docMenuDrawable[1];
            } else {
                menuDrawable = Theme.docMenuDrawable[isDrawSelectedBackground() ? 2 : 0];
            }
            i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(48.0f);
            this.otherX = i;
            imageX = this.photoImage.getImageY() - AndroidUtilities.dp(5.0f);
            this.otherY = imageX;
            setDrawableBounds(menuDrawable, i, imageX);
            menuDrawable.draw(canvas);
        }
        if (this.captionLayout != null) {
            canvas.save();
            float f;
            if (this.currentMessageObject.type == 1 || this.documentAttachType == 4 || this.currentMessageObject.type == 8) {
                i = this.photoImage.getImageX() + AndroidUtilities.dp(5.0f);
                this.captionX = i;
                f = (float) i;
                imageX = (this.photoImage.getImageY() + this.photoImage.getImageHeight()) + AndroidUtilities.dp(6.0f);
                this.captionY = imageX;
                canvas.translate(f, (float) imageX);
            } else {
                i = AndroidUtilities.dp(this.currentMessageObject.isOutOwner() ? 11.0f : 17.0f) + this.backgroundDrawableLeft;
                this.captionX = i;
                f = (float) i;
                imageX = (this.totalHeight - this.captionHeight) - AndroidUtilities.dp(10.0f);
                this.captionY = imageX;
                canvas.translate(f, (float) imageX);
            }
            if (this.pressedLink != null) {
                for (b = 0; b < this.urlPath.size(); b++) {
                    canvas.drawPath((Path) this.urlPath.get(b), urlPaint);
                }
            }
            try {
                this.captionLayout.draw(canvas);
            } catch (Throwable e2) {
                FileLog.e("tmessages", e2);
            }
            canvas.restore();
        }
        if (this.documentAttachType == 1) {
            int titleY;
            int subtitleY;
            if (this.currentMessageObject.isOutOwner()) {
                docNamePaint.setColor(-11162801);
                infoPaint.setColor(isDrawSelectedBackground() ? -10112933 : -10112933);
                docBackPaint.setColor(isDrawSelectedBackground() ? -3806041 : -2427453);
                menuDrawable = Theme.docMenuDrawable[1];
            } else {
                docNamePaint.setColor(-11625772);
                infoPaint.setColor(isDrawSelectedBackground() ? -7752511 : -6182221);
                docBackPaint.setColor(isDrawSelectedBackground() ? -3413258 : -1314571);
                menuDrawable = Theme.docMenuDrawable[isDrawSelectedBackground() ? 2 : 0];
            }
            if (this.drawPhotoImage) {
                if (this.currentMessageObject.type == 0) {
                    i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(56.0f);
                    this.otherX = i;
                    imageX = this.photoImage.getImageY() + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    this.otherY = imageX;
                    setDrawableBounds(menuDrawable, i, imageX);
                } else {
                    i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(40.0f);
                    this.otherX = i;
                    imageX = this.photoImage.getImageY() + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    this.otherY = imageX;
                    setDrawableBounds(menuDrawable, i, imageX);
                }
                x = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f);
                titleY = this.photoImage.getImageY() + AndroidUtilities.dp(8.0f);
                subtitleY = (this.photoImage.getImageY() + this.docTitleLayout.getLineBottom(this.docTitleLayout.getLineCount() - 1)) + AndroidUtilities.dp(13.0f);
                if (this.buttonState >= 0 && this.buttonState < 4) {
                    if (imageDrawn) {
                        this.radialProgress.swapBackground(Theme.photoStatesDrawables[this.buttonState][this.buttonPressed]);
                    } else {
                        int image = this.buttonState;
                        if (this.buttonState == 0) {
                            image = this.currentMessageObject.isOutOwner() ? 7 : 10;
                        } else if (this.buttonState == 1) {
                            image = this.currentMessageObject.isOutOwner() ? 8 : 11;
                        }
                        radialProgress = this.radialProgress;
                        Drawable[] drawableArr = Theme.photoStatesDrawables[image];
                        i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? 1 : 0;
                        radialProgress.swapBackground(drawableArr[i]);
                    }
                }
                if (imageDrawn) {
                    if (this.buttonState == -1) {
                        this.radialProgress.setHideCurrentDrawable(true);
                    }
                    this.radialProgress.setProgressColor(-1);
                } else {
                    this.rect.set((float) this.photoImage.getImageX(), (float) this.photoImage.getImageY(), (float) (this.photoImage.getImageX() + this.photoImage.getImageWidth()), (float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(3.0f), (float) AndroidUtilities.dp(3.0f), docBackPaint);
                    if (this.currentMessageObject.isOutOwner()) {
                        radialProgress = this.radialProgress;
                        if (isDrawSelectedBackground()) {
                            i = -3806041;
                        } else {
                            i = -2427453;
                        }
                        radialProgress.setProgressColor(i);
                    } else {
                        this.radialProgress.setProgressColor(isDrawSelectedBackground() ? -3413258 : -1314571);
                    }
                }
            } else {
                i = (this.backgroundWidth + this.buttonX) - AndroidUtilities.dp(this.currentMessageObject.type == 0 ? 58.0f : 48.0f);
                this.otherX = i;
                imageX = this.buttonY - AndroidUtilities.dp(5.0f);
                this.otherY = imageX;
                setDrawableBounds(menuDrawable, i, imageX);
                x = this.buttonX + AndroidUtilities.dp(53.0f);
                titleY = this.buttonY + AndroidUtilities.dp(4.0f);
                subtitleY = this.buttonY + AndroidUtilities.dp(27.0f);
                if (this.currentMessageObject.isOutOwner()) {
                    radialProgress = this.radialProgress;
                    if (isDrawSelectedBackground() || this.buttonPressed != 0) {
                        i = Theme.MSG_OUT_AUDIO_SELECTED_PROGRESS_COLOR;
                    } else {
                        i = Theme.MSG_OUT_AUDIO_PROGRESS_COLOR;
                    }
                    radialProgress.setProgressColor(i);
                } else {
                    radialProgress = this.radialProgress;
                    i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.MSG_IN_AUDIO_SELECTED_PROGRESS_COLOR : -1;
                    radialProgress.setProgressColor(i);
                }
            }
            menuDrawable.draw(canvas);
            try {
                if (this.docTitleLayout != null) {
                    canvas.save();
                    canvas.translate((float) (this.docTitleOffsetX + x), (float) titleY);
                    this.docTitleLayout.draw(canvas);
                    canvas.restore();
                }
            } catch (Throwable e22) {
                FileLog.e("tmessages", e22);
            }
            try {
                if (this.infoLayout != null) {
                    canvas.save();
                    canvas.translate((float) x, (float) subtitleY);
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                }
            } catch (Throwable e222) {
                FileLog.e("tmessages", e222);
            }
        }
        if (this.drawImageButton && this.photoImage.getVisible()) {
            this.radialProgress.draw(canvas);
        }
        if (!this.botButtons.isEmpty()) {
            int addX;
            if (this.currentMessageObject.isOutOwner()) {
                addX = (getMeasuredWidth() - this.widthForButtons) - AndroidUtilities.dp(10.0f);
            } else {
                addX = this.backgroundDrawableLeft + AndroidUtilities.dp(this.mediaBackground ? DefaultRetryPolicy.DEFAULT_BACKOFF_MULT : 7.0f);
            }
            a = 0;
            while (a < this.botButtons.size()) {
                BotButton button = (BotButton) this.botButtons.get(a);
                y = (button.y + this.layoutHeight) - AndroidUtilities.dp(2.0f);
                Theme.systemDrawable.setColorFilter(a == this.pressedBotButton ? Theme.colorPressedFilter : Theme.colorFilter);
                Theme.systemDrawable.setBounds(button.x + addX, y, (button.x + addX) + button.width, button.height + y);
                Theme.systemDrawable.draw(canvas);
                canvas.save();
                canvas.translate((float) ((button.x + addX) + AndroidUtilities.dp(5.0f)), (float) (((AndroidUtilities.dp(44.0f) - button.title.getLineBottom(button.title.getLineCount() - 1)) / 2) + y));
                button.title.draw(canvas);
                canvas.restore();
                if (button.button instanceof TL_keyboardButtonUrl) {
                    setDrawableBounds(Theme.botLink, (((button.x + button.width) - AndroidUtilities.dp(3.0f)) - Theme.botLink.getIntrinsicWidth()) + addX, AndroidUtilities.dp(3.0f) + y);
                    Theme.botLink.draw(canvas);
                } else if (button.button instanceof TL_keyboardButtonSwitchInline) {
                    setDrawableBounds(Theme.botInline, (((button.x + button.width) - AndroidUtilities.dp(3.0f)) - Theme.botInline.getIntrinsicWidth()) + addX, AndroidUtilities.dp(3.0f) + y);
                    Theme.botInline.draw(canvas);
                } else if ((button.button instanceof TL_keyboardButtonCallback) || (button.button instanceof TL_keyboardButtonRequestGeoLocation) || (button.button instanceof TL_keyboardButtonGame)) {
                    boolean drawProgress = (((button.button instanceof TL_keyboardButtonCallback) || (button.button instanceof TL_keyboardButtonGame)) && SendMessagesHelper.getInstance().isSendingCallback(this.currentMessageObject, button.button)) || ((button.button instanceof TL_keyboardButtonRequestGeoLocation) && SendMessagesHelper.getInstance().isSendingCurrentLocation(this.currentMessageObject, button.button));
                    if (drawProgress || !(drawProgress || button.progressAlpha == 0.0f)) {
                        botProgressPaint.setAlpha(Math.min(255, (int) (button.progressAlpha * 255.0f)));
                        x = ((button.x + button.width) - AndroidUtilities.dp(12.0f)) + addX;
                        this.rect.set((float) x, (float) (AndroidUtilities.dp(4.0f) + y), (float) (AndroidUtilities.dp(8.0f) + x), (float) (AndroidUtilities.dp(12.0f) + y));
                        canvas.drawArc(this.rect, (float) button.angle, 220.0f, false, botProgressPaint);
                        invalidate(((int) this.rect.left) - AndroidUtilities.dp(2.0f), ((int) this.rect.top) - AndroidUtilities.dp(2.0f), ((int) this.rect.right) + AndroidUtilities.dp(2.0f), ((int) this.rect.bottom) + AndroidUtilities.dp(2.0f));
                        long newTime = System.currentTimeMillis();
                        if (Math.abs(button.lastUpdateTime - System.currentTimeMillis()) < 1000) {
                            long delta = newTime - button.lastUpdateTime;
                            button.angle = (int) (((float) button.angle) + (((float) (360 * delta)) / 2000.0f));
                            button.angle = button.angle - ((button.angle / 360) * 360);
                            if (drawProgress) {
                                if (button.progressAlpha < DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                                    button.progressAlpha = button.progressAlpha + (((float) delta) / 200.0f);
                                    if (button.progressAlpha > DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                                        button.progressAlpha = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                                    }
                                }
                            } else if (button.progressAlpha > 0.0f) {
                                button.progressAlpha = button.progressAlpha - (((float) delta) / 200.0f);
                                if (button.progressAlpha < 0.0f) {
                                    button.progressAlpha = 0.0f;
                                }
                            }
                        }
                        button.lastUpdateTime = newTime;
                    }
                }
                a++;
            }
        }
    }

    private Drawable getDrawableForCurrentState() {
        int i = 3;
        int i2 = 0;
        int i3 = 1;
        if (this.documentAttachType != 3 && this.documentAttachType != 5) {
            Drawable[] drawableArr;
            if (this.documentAttachType != 1 || this.drawPhotoImage) {
                this.radialProgress.setAlphaForPrevious(true);
                if (this.buttonState < 0 || this.buttonState >= 4) {
                    if (this.buttonState == -1 && this.documentAttachType == 1) {
                        drawableArr = Theme.photoStatesDrawables[this.currentMessageObject.isOutOwner() ? 9 : 12];
                        if (!isDrawSelectedBackground()) {
                            i3 = 0;
                        }
                        return drawableArr[i3];
                    }
                } else if (this.documentAttachType != 1) {
                    return Theme.photoStatesDrawables[this.buttonState][this.buttonPressed];
                } else {
                    int image = this.buttonState;
                    if (this.buttonState == 0) {
                        image = this.currentMessageObject.isOutOwner() ? 7 : 10;
                    } else if (this.buttonState == 1) {
                        image = this.currentMessageObject.isOutOwner() ? 8 : 11;
                    }
                    drawableArr = Theme.photoStatesDrawables[image];
                    if (isDrawSelectedBackground() || this.buttonPressed != 0) {
                        i2 = 1;
                    }
                    return drawableArr[i2];
                }
            }
            this.radialProgress.setAlphaForPrevious(false);
            if (this.buttonState == -1) {
                Drawable[][] drawableArr2 = Theme.fileStatesDrawable;
                if (!this.currentMessageObject.isOutOwner()) {
                    i = 8;
                }
                drawableArr = drawableArr2[i];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            } else if (this.buttonState == 0) {
                drawableArr = Theme.fileStatesDrawable[this.currentMessageObject.isOutOwner() ? 2 : 7];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            } else if (this.buttonState == 1) {
                drawableArr = Theme.fileStatesDrawable[this.currentMessageObject.isOutOwner() ? 4 : 9];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            }
            return null;
        } else if (this.buttonState == -1) {
            return null;
        } else {
            this.radialProgress.setAlphaForPrevious(false);
            Drawable[] drawableArr3 = Theme.fileStatesDrawable[this.currentMessageObject.isOutOwner() ? this.buttonState : this.buttonState + 5];
            i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? 1 : 0;
            return drawableArr3[i];
        }
    }

    private int getMaxNameWidth() {
        if (this.documentAttachType == 6) {
            int maxWidth;
            if (AndroidUtilities.isTablet()) {
                if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.isFromUser()) {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(42.0f);
                } else {
                    maxWidth = AndroidUtilities.getMinTabletSide();
                }
            } else if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.isFromUser()) {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(42.0f);
            } else {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            }
            return (maxWidth - this.backgroundWidth) - AndroidUtilities.dp(57.0f);
        }
        return this.backgroundWidth - AndroidUtilities.dp(this.mediaBackground ? 22.0f : 31.0f);
    }

    public void updateButtonState(boolean animated) {
        String fileName = null;
        boolean fileExists = false;
        if (this.currentMessageObject.type == 1) {
            if (this.currentPhotoObject != null) {
                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                fileExists = this.currentMessageObject.mediaExists;
            } else {
                return;
            }
        } else if (this.currentMessageObject.type == 8 || this.documentAttachType == 4 || this.currentMessageObject.type == 9 || this.documentAttachType == 3 || this.documentAttachType == 5) {
            if (this.currentMessageObject.attachPathExists) {
                fileName = this.currentMessageObject.messageOwner.attachPath;
                fileExists = true;
            } else if (!this.currentMessageObject.isSendError() || this.documentAttachType == 3 || this.documentAttachType == 5) {
                fileName = this.currentMessageObject.getFileName();
                fileExists = this.currentMessageObject.mediaExists;
            }
        } else if (this.documentAttachType != 0) {
            fileName = FileLoader.getAttachFileName(this.documentAttach);
            fileExists = this.currentMessageObject.mediaExists;
        } else if (this.currentPhotoObject != null) {
            fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
            fileExists = this.currentMessageObject.mediaExists;
        }
        if (TextUtils.isEmpty(fileName)) {
            this.radialProgress.setBackground(null, false, false);
            return;
        }
        boolean fromBot = this.currentMessageObject.messageOwner.params != null && this.currentMessageObject.messageOwner.params.containsKey("query_id");
        Float progress;
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            if ((this.currentMessageObject.isOut() && this.currentMessageObject.isSending()) || (this.currentMessageObject.isSendError() && fromBot)) {
                MediaController.getInstance().addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
                this.buttonState = 4;
                this.radialProgress.setBackground(getDrawableForCurrentState(), !fromBot, animated);
                if (fromBot) {
                    this.radialProgress.setProgress(0.0f, false);
                } else {
                    float floatValue;
                    progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
                    if (progress == null && SendMessagesHelper.getInstance().isSendingMessage(this.currentMessageObject.getId())) {
                        progress = Float.valueOf(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    }
                    RadialProgress radialProgress = this.radialProgress;
                    if (progress != null) {
                        floatValue = progress.floatValue();
                    } else {
                        floatValue = 0.0f;
                    }
                    radialProgress.setProgress(floatValue, false);
                }
            } else if (fileExists) {
                MediaController.getInstance().removeLoadingFileObserver(this);
                boolean playing = MediaController.getInstance().isPlayingAudio(this.currentMessageObject);
                if (!playing || (playing && MediaController.getInstance().isAudioPaused())) {
                    this.buttonState = 0;
                } else {
                    this.buttonState = 1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            } else {
                MediaController.getInstance().addLoadingFileObserver(fileName, this.currentMessageObject, this);
                if (FileLoader.getInstance().isLoadingFile(fileName)) {
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
            }
            updateAudioProgress();
        } else if (this.currentMessageObject.type != 0 || this.documentAttachType == 1 || this.documentAttachType == 4) {
            if (!this.currentMessageObject.isOut() || !this.currentMessageObject.isSending()) {
                if (!(this.currentMessageObject.messageOwner.attachPath == null || this.currentMessageObject.messageOwner.attachPath.length() == 0)) {
                    MediaController.getInstance().removeLoadingFileObserver(this);
                }
                if (fileExists) {
                    MediaController.getInstance().removeLoadingFileObserver(this);
                    if (this.currentMessageObject.type == 8 && !this.photoImage.isAllowStartAnimation()) {
                        this.buttonState = 2;
                    } else if (this.documentAttachType == 4) {
                        this.buttonState = 3;
                    } else {
                        this.buttonState = -1;
                    }
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                    if (this.photoNotSet) {
                        setMessageObject(this.currentMessageObject);
                    }
                    invalidate();
                    return;
                }
                MediaController.getInstance().addLoadingFileObserver(fileName, this.currentMessageObject, this);
                setProgress = 0.0f;
                progressVisible = false;
                if (FileLoader.getInstance().isLoadingFile(fileName)) {
                    progressVisible = true;
                    this.buttonState = 1;
                    progress = ImageLoader.getInstance().getFileProgress(fileName);
                    setProgress = progress != null ? progress.floatValue() : 0.0f;
                } else if (this.cancelLoading || !((this.currentMessageObject.type == 1 && MediaController.getInstance().canDownloadMedia(1)) || (this.currentMessageObject.type == 8 && MediaController.getInstance().canDownloadMedia(32) && MessageObject.isNewGifDocument(this.currentMessageObject.messageOwner.media.document)))) {
                    this.buttonState = 0;
                } else {
                    progressVisible = true;
                    this.buttonState = 1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
                this.radialProgress.setProgress(setProgress, false);
                invalidate();
            } else if (this.currentMessageObject.messageOwner.attachPath != null && this.currentMessageObject.messageOwner.attachPath.length() > 0) {
                MediaController.getInstance().addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
                boolean needProgress = this.currentMessageObject.messageOwner.attachPath == null || !this.currentMessageObject.messageOwner.attachPath.startsWith("http");
                HashMap<String, String> params = this.currentMessageObject.messageOwner.params;
                if (this.currentMessageObject.messageOwner.message == null || params == null || !(params.containsKey("url") || params.containsKey("bot"))) {
                    this.buttonState = 1;
                } else {
                    needProgress = false;
                    this.buttonState = -1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), needProgress, animated);
                if (needProgress) {
                    progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
                    if (progress == null && SendMessagesHelper.getInstance().isSendingMessage(this.currentMessageObject.getId())) {
                        progress = Float.valueOf(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    }
                    this.radialProgress.setProgress(progress != null ? progress.floatValue() : 0.0f, false);
                } else {
                    this.radialProgress.setProgress(0.0f, false);
                }
                invalidate();
            }
        } else if (this.currentPhotoObject != null && this.drawImageButton) {
            if (fileExists) {
                MediaController.getInstance().removeLoadingFileObserver(this);
                if (this.documentAttachType != 2 || this.photoImage.isAllowStartAnimation()) {
                    this.buttonState = -1;
                } else {
                    this.buttonState = 2;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
                return;
            }
            MediaController.getInstance().addLoadingFileObserver(fileName, this.currentMessageObject, this);
            setProgress = 0.0f;
            progressVisible = false;
            if (FileLoader.getInstance().isLoadingFile(fileName)) {
                progressVisible = true;
                this.buttonState = 1;
                progress = ImageLoader.getInstance().getFileProgress(fileName);
                setProgress = progress != null ? progress.floatValue() : 0.0f;
            } else if (this.cancelLoading || !((this.documentAttachType == 0 && MediaController.getInstance().canDownloadMedia(1)) || (this.documentAttachType == 2 && MediaController.getInstance().canDownloadMedia(32)))) {
                this.buttonState = 0;
            } else {
                progressVisible = true;
                this.buttonState = 1;
            }
            this.radialProgress.setProgress(setProgress, false);
            this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
            invalidate();
        }
    }

    private void didPressedButton(boolean animated) {
        if (this.buttonState == 0) {
            if (this.documentAttachType != 3 && this.documentAttachType != 5) {
                this.cancelLoading = false;
                this.radialProgress.setProgress(0.0f, false);
                if (this.currentMessageObject.type == 1) {
                    FileLocation fileLocation;
                    ImageReceiver imageReceiver = this.photoImage;
                    TLObject tLObject = this.currentPhotoObject.location;
                    String str = this.currentPhotoFilter;
                    if (this.currentPhotoObjectThumb != null) {
                        fileLocation = this.currentPhotoObjectThumb.location;
                    } else {
                        fileLocation = null;
                    }
                    imageReceiver.setImage(tLObject, str, fileLocation, this.currentPhotoFilter, this.currentPhotoObject.size, null, false);
                } else if (this.currentMessageObject.type == 8) {
                    this.currentMessageObject.audioProgress = 2.0f;
                    this.photoImage.setImage(this.currentMessageObject.messageOwner.media.document, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilter, this.currentMessageObject.messageOwner.media.document.size, null, false);
                } else if (this.currentMessageObject.type == 9) {
                    FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.document, false, false);
                } else if (this.documentAttachType == 4) {
                    FileLoader.getInstance().loadFile(this.documentAttach, true, false);
                } else if (this.currentMessageObject.type != 0 || this.documentAttachType == 0) {
                    this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                } else if (this.documentAttachType == 2) {
                    this.photoImage.setImage(this.currentMessageObject.messageOwner.media.webpage.document, null, this.currentPhotoObject.location, this.currentPhotoFilter, this.currentMessageObject.messageOwner.media.webpage.document.size, null, false);
                    this.currentMessageObject.audioProgress = 2.0f;
                } else if (this.documentAttachType == 1) {
                    FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.webpage.document, false, false);
                }
                this.buttonState = 1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
                invalidate();
            } else if (this.delegate.needPlayAudio(this.currentMessageObject)) {
                this.buttonState = 1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            }
        } else if (this.buttonState == 1) {
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                if (MediaController.getInstance().pauseAudio(this.currentMessageObject)) {
                    this.buttonState = 0;
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                    invalidate();
                }
            } else if (this.currentMessageObject.isOut() && this.currentMessageObject.isSending()) {
                this.delegate.didPressedCancelSendButton(this);
            } else {
                this.cancelLoading = true;
                if (this.documentAttachType == 4 || this.documentAttachType == 1) {
                    FileLoader.getInstance().cancelLoadFile(this.documentAttach);
                } else if (this.currentMessageObject.type == 0 || this.currentMessageObject.type == 1 || this.currentMessageObject.type == 8) {
                    this.photoImage.cancelLoadImage();
                } else if (this.currentMessageObject.type == 9) {
                    FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.document);
                }
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            }
        } else if (this.buttonState == 2) {
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                this.radialProgress.setProgress(0.0f, false);
                FileLoader.getInstance().loadFile(this.documentAttach, true, false);
                this.buttonState = 4;
                this.radialProgress.setBackground(getDrawableForCurrentState(), true, false);
                invalidate();
                return;
            }
            this.photoImage.setAllowStartAnimation(true);
            this.photoImage.startAnimation();
            this.currentMessageObject.audioProgress = 0.0f;
            this.buttonState = -1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
        } else if (this.buttonState == 3) {
            this.delegate.didPressedImage(this);
        } else if (this.buttonState != 4) {
        } else {
            if (this.documentAttachType != 3 && this.documentAttachType != 5) {
                return;
            }
            if ((!this.currentMessageObject.isOut() || !this.currentMessageObject.isSending()) && !this.currentMessageObject.isSendError()) {
                FileLoader.getInstance().cancelLoadFile(this.documentAttach);
                this.buttonState = 2;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            } else if (this.delegate != null) {
                this.delegate.didPressedCancelSendButton(this);
            }
        }
    }

    public void onFailedDownload(String fileName) {
        boolean z = this.documentAttachType == 3 || this.documentAttachType == 5;
        updateButtonState(z);
    }

    public void onSuccessDownload(String fileName) {
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            updateButtonState(true);
            updateWaveform();
            return;
        }
        this.radialProgress.setProgress(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, true);
        if (this.currentMessageObject.type != 0) {
            if (!this.photoNotSet || (this.currentMessageObject.type == 8 && this.currentMessageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)) {
                if (this.currentMessageObject.type != 8 || this.currentMessageObject.audioProgress == DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                    updateButtonState(true);
                } else {
                    this.photoNotSet = false;
                    this.buttonState = 2;
                    didPressedButton(true);
                }
            }
            if (this.photoNotSet) {
                setMessageObject(this.currentMessageObject);
            }
        } else if (this.documentAttachType == 2 && this.currentMessageObject.audioProgress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            this.buttonState = 2;
            didPressedButton(true);
        } else if (this.photoNotSet) {
            setMessageObject(this.currentMessageObject);
        } else {
            updateButtonState(true);
        }
    }

    public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
        if (this.currentMessageObject != null && set && !thumb && !this.currentMessageObject.mediaExists && !this.currentMessageObject.attachPathExists) {
            this.currentMessageObject.mediaExists = true;
            updateButtonState(true);
        }
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            if (this.buttonState != 4) {
                updateButtonState(false);
            }
        } else if (this.buttonState != 1) {
            updateButtonState(false);
        }
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
        this.radialProgress.setProgress(progress, true);
    }

    public void onProvideStructure(ViewStructure structure) {
        super.onProvideStructure(structure);
        if (this.allowAssistant && VERSION.SDK_INT >= 23) {
            if (this.currentMessageObject.messageText != null && this.currentMessageObject.messageText.length() > 0) {
                structure.setText(this.currentMessageObject.messageText);
            } else if (this.currentMessageObject.caption != null && this.currentMessageObject.caption.length() > 0) {
                structure.setText(this.currentMessageObject.caption);
            }
        }
    }

    public void setDelegate(ChatMessageCellDelegate chatMessageCellDelegate) {
        this.delegate = chatMessageCellDelegate;
    }

    public void setAllowAssistant(boolean value) {
        this.allowAssistant = value;
    }

    private void measureTime(MessageObject messageObject) {
        String timeString;
        boolean hasSign = !messageObject.isOutOwner() && messageObject.messageOwner.from_id > 0 && messageObject.messageOwner.post;
        User signUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
        if (hasSign && signUser == null) {
            hasSign = false;
        }
        User author = null;
        if (this.currentMessageObject.isFromUser()) {
            author = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
        }
        if (messageObject.messageOwner.via_bot_id == 0 && messageObject.messageOwner.via_bot_name == null && ((author == null || !author.bot) && (messageObject.messageOwner.flags & 32768) != 0)) {
            timeString = LocaleController.getString("EditedMessage", R.string.EditedMessage) + " " + LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        } else {
            timeString = LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        }
        if (hasSign) {
            this.currentTimeString = ", " + timeString;
        } else {
            this.currentTimeString = timeString;
        }
        int ceil = (int) Math.ceil((double) timePaint.measureText(this.currentTimeString));
        this.timeWidth = ceil;
        this.timeTextWidth = ceil;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            this.currentViewsString = String.format("%s", new Object[]{LocaleController.formatShortNumber(Math.max(1, messageObject.messageOwner.views), null)});
            this.viewsTextWidth = (int) Math.ceil((double) timePaint.measureText(this.currentViewsString));
            this.timeWidth += (this.viewsTextWidth + Theme.viewsCountDrawable[0].getIntrinsicWidth()) + AndroidUtilities.dp(10.0f);
        }
        if (hasSign) {
            if (this.availableTimeWidth == 0) {
                this.availableTimeWidth = AndroidUtilities.dp(1000.0f);
            }
            CharSequence name = ContactsController.formatName(signUser.first_name, signUser.last_name).replace('\n', ' ');
            int widthForSign = this.availableTimeWidth - this.timeWidth;
            int width = (int) Math.ceil((double) timePaint.measureText(name, 0, name.length()));
            if (width > widthForSign) {
                name = TextUtils.ellipsize(name, timePaint, (float) widthForSign, TruncateAt.END);
                width = widthForSign;
            }
            this.currentTimeString = name + this.currentTimeString;
            this.timeTextWidth += width;
            this.timeWidth += width;
        }
    }

    private boolean isDrawSelectedBackground() {
        return (isPressed() && this.isCheckPressed) || ((!this.isCheckPressed && this.isPressed) || this.isHighlighted);
    }

    private boolean checkNeedDrawShareButton(MessageObject messageObject) {
        if (messageObject.type == 13) {
            return false;
        }
        if (messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.channel_id != 0 && !messageObject.isOut()) {
            return true;
        }
        if (messageObject.isFromUser()) {
            if ((messageObject.messageOwner.media instanceof TL_messageMediaEmpty) || messageObject.messageOwner.media == null || ((messageObject.messageOwner.media instanceof TL_messageMediaWebPage) && !(messageObject.messageOwner.media.webpage instanceof TL_webPage))) {
                return false;
            }
            User user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
            if (user != null && user.bot) {
                return true;
            }
            if (!messageObject.isOut()) {
                if (messageObject.messageOwner.media instanceof TL_messageMediaGame) {
                    return true;
                }
                if (messageObject.isMegagroup()) {
                    Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(messageObject.messageOwner.to_id.channel_id));
                    if (chat == null || chat.username == null || chat.username.length() <= 0 || (messageObject.messageOwner.media instanceof TL_messageMediaContact) || (messageObject.messageOwner.media instanceof TL_messageMediaGeo)) {
                        return false;
                    }
                    return true;
                }
            }
        } else if ((messageObject.messageOwner.from_id < 0 || messageObject.messageOwner.post) && messageObject.messageOwner.to_id.channel_id != 0 && ((messageObject.messageOwner.via_bot_id == 0 && messageObject.messageOwner.reply_to_msg_id == 0) || messageObject.type != 13)) {
            return true;
        }
        return false;
    }

    private void setMessageObjectInternal(MessageObject messageObject) {
        String name;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            if (this.currentMessageObject.isContentUnread() && !this.currentMessageObject.isOut()) {
                MessagesController.getInstance().addToViewsQueue(this.currentMessageObject.messageOwner, false);
                this.currentMessageObject.setContentIsRead();
            } else if (!this.currentMessageObject.viewsReloaded) {
                MessagesController.getInstance().addToViewsQueue(this.currentMessageObject.messageOwner, true);
                this.currentMessageObject.viewsReloaded = true;
            }
        }
        if (this.currentMessageObject.isFromUser()) {
            this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
        } else if (this.currentMessageObject.messageOwner.from_id < 0) {
            this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
        } else if (this.currentMessageObject.messageOwner.post) {
            this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(this.currentMessageObject.messageOwner.to_id.channel_id));
        }
        if (this.isChat && !messageObject.isOutOwner() && messageObject.isFromUser()) {
            this.isAvatarVisible = true;
            if (this.currentUser != null) {
                if (this.currentUser.photo != null) {
                    this.currentPhoto = this.currentUser.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentUser);
            } else if (this.currentChat != null) {
                if (this.currentChat.photo != null) {
                    this.currentPhoto = this.currentChat.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentChat);
            } else {
                this.currentPhoto = null;
                this.avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            this.avatarImage.setImage(this.currentPhoto, "50_50", this.avatarDrawable, null, false);
        }
        measureTime(messageObject);
        this.namesOffset = 0;
        String viaUsername = null;
        CharSequence viaString = null;
        if (messageObject.messageOwner.via_bot_id != 0) {
            User botUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.via_bot_id));
            if (!(botUser == null || botUser.username == null || botUser.username.length() <= 0)) {
                viaUsername = "@" + botUser.username;
                viaString = AndroidUtilities.replaceTags(String.format(" via <b>%s</b>", new Object[]{viaUsername}));
                this.viaWidth = (int) Math.ceil((double) replyNamePaint.measureText(viaString, 0, viaString.length()));
                this.currentViaBotUser = botUser;
            }
        } else if (messageObject.messageOwner.via_bot_name != null && messageObject.messageOwner.via_bot_name.length() > 0) {
            viaUsername = "@" + messageObject.messageOwner.via_bot_name;
            viaString = AndroidUtilities.replaceTags(String.format(" via <b>%s</b>", new Object[]{viaUsername}));
            this.viaWidth = (int) Math.ceil((double) replyNamePaint.measureText(viaString, 0, viaString.length()));
        }
        boolean authorName = this.drawName && this.isChat && !this.currentMessageObject.isOutOwner();
        boolean viaBot = (messageObject.messageOwner.fwd_from == null || messageObject.type == 14) && viaUsername != null;
        if (authorName || viaBot) {
            this.drawNameLayout = true;
            this.nameWidth = getMaxNameWidth();
            if (this.nameWidth < 0) {
                this.nameWidth = AndroidUtilities.dp(100.0f);
            }
            if (!authorName) {
                this.currentNameString = "";
            } else if (this.currentUser != null) {
                this.currentNameString = UserObject.getUserName(this.currentUser);
            } else if (this.currentChat != null) {
                this.currentNameString = this.currentChat.title;
            } else {
                this.currentNameString = "DELETED";
            }
            CharSequence nameStringFinal = TextUtils.ellipsize(this.currentNameString.replace('\n', ' '), namePaint, (float) (this.nameWidth - (viaBot ? this.viaWidth : 0)), TruncateAt.END);
            if (viaBot) {
                int color;
                this.viaNameWidth = (int) Math.ceil((double) namePaint.measureText(nameStringFinal, 0, nameStringFinal.length()));
                if (this.viaNameWidth != 0) {
                    this.viaNameWidth += AndroidUtilities.dp(4.0f);
                }
                if (this.currentMessageObject.type == 13) {
                    color = -1;
                } else {
                    color = this.currentMessageObject.isOutOwner() ? -11162801 : -12940081;
                }
                SpannableStringBuilder spannableStringBuilder;
                if (this.currentNameString.length() > 0) {
                    spannableStringBuilder = new SpannableStringBuilder(String.format("%s via %s", new Object[]{nameStringFinal, viaUsername}));
                    spannableStringBuilder.setSpan(new TypefaceSpan(Typeface.DEFAULT, 0, color), nameStringFinal.length() + 1, nameStringFinal.length() + 4, 33);
                    spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, color), nameStringFinal.length() + 5, spannableStringBuilder.length(), 33);
                    nameStringFinal = spannableStringBuilder;
                } else {
                    spannableStringBuilder = new SpannableStringBuilder(String.format("via %s", new Object[]{viaUsername}));
                    spannableStringBuilder.setSpan(new TypefaceSpan(Typeface.DEFAULT, 0, color), 0, 4, 33);
                    spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, color), 4, spannableStringBuilder.length(), 33);
                    Object nameStringFinal2 = spannableStringBuilder;
                }
                nameStringFinal = TextUtils.ellipsize(nameStringFinal, namePaint, (float) this.nameWidth, TruncateAt.END);
            }
            try {
                this.nameLayout = new StaticLayout(nameStringFinal, namePaint, this.nameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                if (this.nameLayout == null || this.nameLayout.getLineCount() <= 0) {
                    this.nameWidth = 0;
                    if (this.currentNameString.length() == 0) {
                        this.currentNameString = null;
                    }
                } else {
                    this.nameWidth = (int) Math.ceil((double) this.nameLayout.getLineWidth(0));
                    if (messageObject.type != 13) {
                        this.namesOffset += AndroidUtilities.dp(19.0f);
                    }
                    this.nameOffsetX = this.nameLayout.getLineLeft(0);
                    if (this.currentNameString.length() == 0) {
                        this.currentNameString = null;
                    }
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
        } else {
            this.currentNameString = null;
            this.nameLayout = null;
            this.nameWidth = 0;
        }
        this.currentForwardUser = null;
        this.currentForwardNameString = null;
        this.currentForwardChannel = null;
        this.forwardedNameLayout[0] = null;
        this.forwardedNameLayout[1] = null;
        this.forwardedNameWidth = 0;
        if (this.drawForwardedName && messageObject.isForwarded()) {
            if (messageObject.messageOwner.fwd_from.channel_id != 0) {
                this.currentForwardChannel = MessagesController.getInstance().getChat(Integer.valueOf(messageObject.messageOwner.fwd_from.channel_id));
            }
            if (messageObject.messageOwner.fwd_from.from_id != 0) {
                this.currentForwardUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.fwd_from.from_id));
            }
            if (!(this.currentForwardUser == null && this.currentForwardChannel == null)) {
                CharSequence lastLine;
                if (this.currentForwardChannel != null) {
                    if (this.currentForwardUser != null) {
                        this.currentForwardNameString = String.format("%s (%s)", new Object[]{this.currentForwardChannel.title, UserObject.getUserName(this.currentForwardUser)});
                    } else {
                        this.currentForwardNameString = this.currentForwardChannel.title;
                    }
                } else if (this.currentForwardUser != null) {
                    this.currentForwardNameString = UserObject.getUserName(this.currentForwardUser);
                }
                this.forwardedNameWidth = getMaxNameWidth();
                name = TextUtils.ellipsize(this.currentForwardNameString.replace('\n', ' '), replyNamePaint, (float) ((this.forwardedNameWidth - ((int) Math.ceil((double) forwardNamePaint.measureText(LocaleController.getString("From", R.string.From) + " ")))) - this.viaWidth), TruncateAt.END);
                if (viaString != null) {
                    this.viaNameWidth = (int) Math.ceil((double) forwardNamePaint.measureText(LocaleController.getString("From", R.string.From) + " " + name));
                    lastLine = AndroidUtilities.replaceTags(String.format("%s <b>%s</b> via <b>%s</b>", new Object[]{LocaleController.getString("From", R.string.From), name, viaUsername}));
                } else {
                    lastLine = AndroidUtilities.replaceTags(String.format("%s <b>%s</b>", new Object[]{LocaleController.getString("From", R.string.From), name}));
                }
                try {
                    this.forwardedNameLayout[1] = new StaticLayout(TextUtils.ellipsize(lastLine, forwardNamePaint, (float) this.forwardedNameWidth, TruncateAt.END), forwardNamePaint, this.forwardedNameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                    this.forwardedNameLayout[0] = new StaticLayout(TextUtils.ellipsize(AndroidUtilities.replaceTags(LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage)), forwardNamePaint, (float) this.forwardedNameWidth, TruncateAt.END), forwardNamePaint, this.forwardedNameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                    this.forwardedNameWidth = Math.max((int) Math.ceil((double) this.forwardedNameLayout[0].getLineWidth(0)), (int) Math.ceil((double) this.forwardedNameLayout[1].getLineWidth(0)));
                    this.forwardNameOffsetX[0] = this.forwardedNameLayout[0].getLineLeft(0);
                    this.forwardNameOffsetX[1] = this.forwardedNameLayout[1].getLineLeft(0);
                    this.namesOffset += AndroidUtilities.dp(36.0f);
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
            }
        }
        if (messageObject.isReply()) {
            CharSequence stringFinalName;
            this.namesOffset += AndroidUtilities.dp(42.0f);
            if (messageObject.type != 0) {
                if (messageObject.type == 13) {
                    this.namesOffset -= AndroidUtilities.dp(42.0f);
                } else {
                    this.namesOffset += AndroidUtilities.dp(5.0f);
                }
            }
            int maxWidth = getMaxNameWidth();
            if (messageObject.type != 13) {
                maxWidth -= AndroidUtilities.dp(10.0f);
            }
            CharSequence stringFinalText = null;
            if (messageObject.replyMessageObject != null) {
                PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs2, 80);
                if (photoSize == null) {
                    photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs, 80);
                }
                if (photoSize == null || messageObject.replyMessageObject.type == 13 || ((messageObject.type == 13 && !AndroidUtilities.isTablet()) || messageObject.replyMessageObject.isSecretMedia())) {
                    this.replyImageReceiver.setImageBitmap((Drawable) null);
                    this.needReplyImage = false;
                } else {
                    this.currentReplyPhoto = photoSize.location;
                    this.replyImageReceiver.setImage(photoSize.location, "50_50", null, null, true);
                    this.needReplyImage = true;
                    maxWidth -= AndroidUtilities.dp(44.0f);
                }
                name = null;
                if (messageObject.replyMessageObject.isFromUser()) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.replyMessageObject.messageOwner.from_id));
                    if (user != null) {
                        name = UserObject.getUserName(user);
                    }
                } else if (messageObject.replyMessageObject.messageOwner.from_id < 0) {
                    chat = MessagesController.getInstance().getChat(Integer.valueOf(-messageObject.replyMessageObject.messageOwner.from_id));
                    if (chat != null) {
                        name = chat.title;
                    }
                } else {
                    chat = MessagesController.getInstance().getChat(Integer.valueOf(messageObject.replyMessageObject.messageOwner.to_id.channel_id));
                    if (chat != null) {
                        name = chat.title;
                    }
                }
                if (name != null) {
                    stringFinalName = TextUtils.ellipsize(name.replace('\n', ' '), replyNamePaint, (float) maxWidth, TruncateAt.END);
                } else {
                    stringFinalName = null;
                }
                if (messageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame) {
                    stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(messageObject.replyMessageObject.messageOwner.media.game.title, replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), replyTextPaint, (float) maxWidth, TruncateAt.END);
                } else if (messageObject.replyMessageObject.messageText != null && messageObject.replyMessageObject.messageText.length() > 0) {
                    String mess = messageObject.replyMessageObject.messageText.toString();
                    if (mess.length() > 150) {
                        mess = mess.substring(0, 150);
                    }
                    stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(mess.replace('\n', ' '), replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), replyTextPaint, (float) maxWidth, TruncateAt.END);
                }
            } else {
                stringFinalName = null;
            }
            if (stringFinalName == null) {
                stringFinalName = LocaleController.getString("Loading", R.string.Loading);
            }
            try {
                this.replyNameLayout = new StaticLayout(stringFinalName, replyNamePaint, maxWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                if (this.replyNameLayout.getLineCount() > 0) {
                    this.replyNameWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 12)) + ((int) Math.ceil((double) this.replyNameLayout.getLineWidth(0)));
                    this.replyNameOffset = this.replyNameLayout.getLineLeft(0);
                }
            } catch (Throwable e22) {
                FileLog.e("tmessages", e22);
            }
            if (stringFinalText != null) {
                try {
                    this.replyTextLayout = new StaticLayout(stringFinalText, replyTextPaint, maxWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
                    if (this.replyTextLayout.getLineCount() > 0) {
                        this.replyTextWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 12)) + ((int) Math.ceil((double) this.replyTextLayout.getLineWidth(0)));
                        this.replyTextOffset = this.replyTextLayout.getLineLeft(0);
                    }
                } catch (Throwable e222) {
                    FileLog.e("tmessages", e222);
                }
            }
        }
        requestLayout();
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            if (this.wasLayout) {
                Drawable drawable;
                int i;
                int dp;
                int dp2;
                if (this.isAvatarVisible) {
                    this.avatarImage.draw(canvas);
                }
                if (this.mediaBackground) {
                    timePaint.setColor(-1);
                } else if (this.currentMessageObject.isOutOwner()) {
                    timePaint.setColor(isDrawSelectedBackground() ? -9391780 : -9391780);
                } else {
                    timePaint.setColor(isDrawSelectedBackground() ? -7752511 : -6182221);
                }
                int i2;
                if (this.currentMessageObject.isOutOwner()) {
                    if (isDrawSelectedBackground()) {
                        if (this.mediaBackground) {
                            this.currentBackgroundDrawable = Theme.backgroundMediaDrawableOutSelected;
                        } else {
                            this.currentBackgroundDrawable = Theme.backgroundDrawableOutSelected;
                        }
                    } else if (this.mediaBackground) {
                        this.currentBackgroundDrawable = Theme.backgroundMediaDrawableOut;
                    } else {
                        this.currentBackgroundDrawable = Theme.backgroundDrawableOut;
                    }
                    drawable = this.currentBackgroundDrawable;
                    i = this.layoutWidth - this.backgroundWidth;
                    if (this.mediaBackground) {
                        dp = AndroidUtilities.dp(9.0f);
                    } else {
                        dp = 0;
                    }
                    i -= dp;
                    this.backgroundDrawableLeft = i;
                    dp2 = AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    i2 = this.backgroundWidth;
                    if (this.mediaBackground) {
                        dp = 0;
                    } else {
                        dp = AndroidUtilities.dp(3.0f);
                    }
                    setDrawableBounds(drawable, i, dp2, i2 - dp, this.layoutHeight - AndroidUtilities.dp(2.0f));
                } else {
                    if (isDrawSelectedBackground()) {
                        if (this.mediaBackground) {
                            this.currentBackgroundDrawable = Theme.backgroundMediaDrawableInSelected;
                        } else {
                            this.currentBackgroundDrawable = Theme.backgroundDrawableInSelected;
                        }
                    } else if (this.mediaBackground) {
                        this.currentBackgroundDrawable = Theme.backgroundMediaDrawableIn;
                    } else {
                        this.currentBackgroundDrawable = Theme.backgroundDrawableIn;
                    }
                    if (this.isChat && this.currentMessageObject.isFromUser()) {
                        drawable = this.currentBackgroundDrawable;
                        if (this.mediaBackground) {
                            dp = 9;
                        } else {
                            dp = 3;
                        }
                        i = AndroidUtilities.dp((float) (dp + 48));
                        this.backgroundDrawableLeft = i;
                        dp2 = AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        i2 = this.backgroundWidth;
                        if (this.mediaBackground) {
                            dp = 0;
                        } else {
                            dp = AndroidUtilities.dp(3.0f);
                        }
                        setDrawableBounds(drawable, i, dp2, i2 - dp, this.layoutHeight - AndroidUtilities.dp(2.0f));
                    } else {
                        drawable = this.currentBackgroundDrawable;
                        i = !this.mediaBackground ? AndroidUtilities.dp(3.0f) : AndroidUtilities.dp(9.0f);
                        this.backgroundDrawableLeft = i;
                        setDrawableBounds(drawable, i, AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT), this.backgroundWidth - (this.mediaBackground ? 0 : AndroidUtilities.dp(3.0f)), this.layoutHeight - AndroidUtilities.dp(2.0f));
                    }
                }
                if (this.drawBackground && this.currentBackgroundDrawable != null) {
                    this.currentBackgroundDrawable.draw(canvas);
                }
                drawContent(canvas);
                if (this.drawShareButton) {
                    Theme.shareDrawable.setColorFilter(this.sharePressed ? Theme.colorPressedFilter : Theme.colorFilter);
                    if (this.currentMessageObject.isOutOwner()) {
                        this.shareStartX = (this.currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(8.0f)) - Theme.shareDrawable.getIntrinsicWidth();
                    } else {
                        this.shareStartX = this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(8.0f);
                    }
                    Drawable drawable2 = Theme.shareDrawable;
                    i = this.shareStartX;
                    dp2 = this.layoutHeight - AndroidUtilities.dp(41.0f);
                    this.shareStartY = dp2;
                    setDrawableBounds(drawable2, i, dp2);
                    Theme.shareDrawable.draw(canvas);
                    setDrawableBounds(Theme.shareIconDrawable, this.shareStartX + AndroidUtilities.dp(9.0f), this.shareStartY + AndroidUtilities.dp(9.0f));
                    Theme.shareIconDrawable.draw(canvas);
                }
                if (this.drawNameLayout && this.nameLayout != null) {
                    canvas.save();
                    if (this.currentMessageObject.type == 13) {
                        namePaint.setColor(-1);
                        if (this.currentMessageObject.isOutOwner()) {
                            this.nameX = (float) AndroidUtilities.dp(28.0f);
                        } else {
                            this.nameX = (float) (this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(22.0f));
                        }
                        this.nameY = (float) (this.layoutHeight - AndroidUtilities.dp(38.0f));
                        Theme.systemDrawable.setColorFilter(Theme.colorFilter);
                        Theme.systemDrawable.setBounds(((int) this.nameX) - AndroidUtilities.dp(12.0f), ((int) this.nameY) - AndroidUtilities.dp(5.0f), (((int) this.nameX) + AndroidUtilities.dp(12.0f)) + this.nameWidth, ((int) this.nameY) + AndroidUtilities.dp(22.0f));
                        Theme.systemDrawable.draw(canvas);
                    } else {
                        if (this.mediaBackground || this.currentMessageObject.isOutOwner()) {
                            this.nameX = ((float) (this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f))) - this.nameOffsetX;
                        } else {
                            this.nameX = ((float) (this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(17.0f))) - this.nameOffsetX;
                        }
                        if (this.currentUser != null) {
                            namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentUser.id));
                        } else if (this.currentChat != null) {
                            namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentChat.id));
                        } else {
                            namePaint.setColor(AvatarDrawable.getNameColorForId(0));
                        }
                        this.nameY = (float) AndroidUtilities.dp(10.0f);
                    }
                    canvas.translate(this.nameX, this.nameY);
                    this.nameLayout.draw(canvas);
                    canvas.restore();
                }
                if (!(!this.drawForwardedName || this.forwardedNameLayout[0] == null || this.forwardedNameLayout[1] == null)) {
                    this.forwardNameY = AndroidUtilities.dp((float) ((this.drawNameLayout ? 19 : 0) + 10));
                    if (this.currentMessageObject.isOutOwner()) {
                        forwardNamePaint.setColor(-11162801);
                        this.forwardNameX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
                    } else {
                        forwardNamePaint.setColor(Theme.MSG_IN_FORDWARDED_NAME_TEXT_COLOR);
                        if (this.mediaBackground) {
                            this.forwardNameX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
                        } else {
                            this.forwardNameX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(17.0f);
                        }
                    }
                    for (int a = 0; a < 2; a++) {
                        canvas.save();
                        canvas.translate(((float) this.forwardNameX) - this.forwardNameOffsetX[a], (float) (this.forwardNameY + (AndroidUtilities.dp(16.0f) * a)));
                        this.forwardedNameLayout[a].draw(canvas);
                        canvas.restore();
                    }
                }
                if (this.currentMessageObject.isReply()) {
                    if (this.currentMessageObject.type == 13) {
                        replyLinePaint.setColor(-1);
                        replyNamePaint.setColor(-1);
                        replyTextPaint.setColor(-1);
                        if (this.currentMessageObject.isOutOwner()) {
                            this.replyStartX = AndroidUtilities.dp(23.0f);
                        } else {
                            this.replyStartX = this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(17.0f);
                        }
                        this.replyStartY = this.layoutHeight - AndroidUtilities.dp(58.0f);
                        if (this.nameLayout != null) {
                            this.replyStartY -= AndroidUtilities.dp(31.0f);
                        }
                        int backWidth = Math.max(this.replyNameWidth, this.replyTextWidth) + AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 14));
                        Theme.systemDrawable.setColorFilter(Theme.colorFilter);
                        Theme.systemDrawable.setBounds(this.replyStartX - AndroidUtilities.dp(7.0f), this.replyStartY - AndroidUtilities.dp(6.0f), (this.replyStartX - AndroidUtilities.dp(7.0f)) + backWidth, this.replyStartY + AndroidUtilities.dp(41.0f));
                        Theme.systemDrawable.draw(canvas);
                    } else {
                        if (this.currentMessageObject.isOutOwner()) {
                            replyLinePaint.setColor(-7812741);
                            replyNamePaint.setColor(-11162801);
                            if (this.currentMessageObject.replyMessageObject == null || this.currentMessageObject.replyMessageObject.type != 0 || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame)) {
                                replyTextPaint.setColor(isDrawSelectedBackground() ? -10112933 : -10112933);
                            } else {
                                replyTextPaint.setColor(-16777216);
                            }
                            this.replyStartX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(12.0f);
                        } else {
                            replyLinePaint.setColor(-9390872);
                            replyNamePaint.setColor(-12940081);
                            if (this.currentMessageObject.replyMessageObject == null || this.currentMessageObject.replyMessageObject.type != 0 || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame)) {
                                replyTextPaint.setColor(isDrawSelectedBackground() ? -7752511 : -6182221);
                            } else {
                                replyTextPaint.setColor(-16777216);
                            }
                            if (this.mediaBackground) {
                                this.replyStartX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(12.0f);
                            } else {
                                this.replyStartX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(18.0f);
                            }
                        }
                        if (!this.drawForwardedName || this.forwardedNameLayout[0] == null) {
                            dp = 0;
                        } else {
                            dp = 36;
                        }
                        i = dp + 12;
                        if (!this.drawNameLayout || this.nameLayout == null) {
                            dp = 0;
                        } else {
                            dp = 20;
                        }
                        this.replyStartY = AndroidUtilities.dp((float) (dp + i));
                    }
                    canvas.drawRect((float) this.replyStartX, (float) this.replyStartY, (float) (this.replyStartX + AndroidUtilities.dp(2.0f)), (float) (this.replyStartY + AndroidUtilities.dp(35.0f)), replyLinePaint);
                    if (this.needReplyImage) {
                        this.replyImageReceiver.setImageCoords(this.replyStartX + AndroidUtilities.dp(10.0f), this.replyStartY, AndroidUtilities.dp(35.0f), AndroidUtilities.dp(35.0f));
                        this.replyImageReceiver.draw(canvas);
                    }
                    if (this.replyNameLayout != null) {
                        canvas.save();
                        canvas.translate(((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))) + (((float) this.replyStartX) - this.replyNameOffset), (float) this.replyStartY);
                        this.replyNameLayout.draw(canvas);
                        canvas.restore();
                    }
                    if (this.replyTextLayout != null) {
                        canvas.save();
                        canvas.translate(((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))) + (((float) this.replyStartX) - this.replyTextOffset), (float) (this.replyStartY + AndroidUtilities.dp(19.0f)));
                        this.replyTextLayout.draw(canvas);
                        canvas.restore();
                    }
                }
                if (this.drawTime || !this.mediaBackground) {
                    int additionalX;
                    if (this.mediaBackground) {
                        if (this.currentMessageObject.type == 13) {
                            drawable = Theme.timeStickerBackgroundDrawable;
                        } else {
                            drawable = Theme.timeBackgroundDrawable;
                        }
                        setDrawableBounds(drawable, this.timeX - AndroidUtilities.dp(4.0f), this.layoutHeight - AndroidUtilities.dp(27.0f), this.timeWidth + AndroidUtilities.dp((float) ((this.currentMessageObject.isOutOwner() ? 20 : 0) + 8)), AndroidUtilities.dp(17.0f));
                        drawable.draw(canvas);
                        additionalX = 0;
                        if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                            additionalX = (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                            if (this.currentMessageObject.isSending()) {
                                if (!this.currentMessageObject.isOutOwner()) {
                                    setDrawableBounds(Theme.clockMediaDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - Theme.clockMediaDrawable.getIntrinsicHeight());
                                    Theme.clockMediaDrawable.draw(canvas);
                                }
                            } else if (!this.currentMessageObject.isSendError()) {
                                Drawable countDrawable = Theme.viewsMediaCountDrawable;
                                setDrawableBounds(countDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(9.5f)) - this.timeLayout.getHeight());
                                countDrawable.draw(canvas);
                                if (this.viewsLayout != null) {
                                    canvas.save();
                                    canvas.translate((float) ((this.timeX + countDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(11.3f)) - this.timeLayout.getHeight()));
                                    this.viewsLayout.draw(canvas);
                                    canvas.restore();
                                }
                            } else if (!this.currentMessageObject.isOutOwner()) {
                                setDrawableBounds(Theme.errorDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - Theme.errorDrawable.getIntrinsicHeight());
                                Theme.errorDrawable.draw(canvas);
                            }
                        }
                        canvas.save();
                        canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(11.3f)) - this.timeLayout.getHeight()));
                        this.timeLayout.draw(canvas);
                        canvas.restore();
                    } else {
                        additionalX = 0;
                        if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                            additionalX = (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                            if (this.currentMessageObject.isSending()) {
                                if (!this.currentMessageObject.isOutOwner()) {
                                    Drawable clockDrawable = Theme.clockChannelDrawable[isDrawSelectedBackground() ? 1 : 0];
                                    setDrawableBounds(clockDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - clockDrawable.getIntrinsicHeight());
                                    clockDrawable.draw(canvas);
                                }
                            } else if (!this.currentMessageObject.isSendError()) {
                                if (this.currentMessageObject.isOutOwner()) {
                                    setDrawableBounds(Theme.viewsOutCountDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                                    Theme.viewsOutCountDrawable.draw(canvas);
                                } else {
                                    setDrawableBounds(Theme.viewsCountDrawable[isDrawSelectedBackground() ? 1 : 0], this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                                    Theme.viewsCountDrawable[isDrawSelectedBackground() ? 1 : 0].draw(canvas);
                                }
                                if (this.viewsLayout != null) {
                                    canvas.save();
                                    canvas.translate((float) ((this.timeX + Theme.viewsOutCountDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                                    this.viewsLayout.draw(canvas);
                                    canvas.restore();
                                }
                            } else if (!this.currentMessageObject.isOutOwner()) {
                                setDrawableBounds(Theme.errorDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(6.5f)) - Theme.errorDrawable.getIntrinsicHeight());
                                Theme.errorDrawable.draw(canvas);
                            }
                        }
                        canvas.save();
                        canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                        this.timeLayout.draw(canvas);
                        canvas.restore();
                    }
                    if (this.currentMessageObject.isOutOwner()) {
                        boolean drawCheck1 = false;
                        boolean drawCheck2 = false;
                        boolean drawClock = false;
                        boolean drawError = false;
                        boolean isBroadcast = ((int) (this.currentMessageObject.getDialogId() >> 32)) == 1;
                        if (this.currentMessageObject.isSending()) {
                            drawCheck1 = false;
                            drawCheck2 = false;
                            drawClock = true;
                            drawError = false;
                        } else if (this.currentMessageObject.isSendError()) {
                            drawCheck1 = false;
                            drawCheck2 = false;
                            drawClock = false;
                            drawError = true;
                        } else if (this.currentMessageObject.isSent()) {
                            if (this.currentMessageObject.isUnread()) {
                                drawCheck1 = false;
                                drawCheck2 = true;
                            } else {
                                drawCheck1 = true;
                                drawCheck2 = true;
                            }
                            drawClock = false;
                            drawError = false;
                        }
                        if (drawClock) {
                            if (this.mediaBackground) {
                                setDrawableBounds(Theme.clockMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - Theme.clockMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - Theme.clockMediaDrawable.getIntrinsicHeight());
                                Theme.clockMediaDrawable.draw(canvas);
                            } else {
                                setDrawableBounds(Theme.clockDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - Theme.clockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - Theme.clockDrawable.getIntrinsicHeight());
                                Theme.clockDrawable.draw(canvas);
                            }
                        }
                        if (!isBroadcast) {
                            if (drawCheck2) {
                                if (this.mediaBackground) {
                                    if (drawCheck1) {
                                        setDrawableBounds(Theme.checkMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(26.3f)) - Theme.checkMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - Theme.checkMediaDrawable.getIntrinsicHeight());
                                    } else {
                                        setDrawableBounds(Theme.checkMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.checkMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - Theme.checkMediaDrawable.getIntrinsicHeight());
                                    }
                                    Theme.checkMediaDrawable.draw(canvas);
                                } else {
                                    if (drawCheck1) {
                                        setDrawableBounds(Theme.checkDrawable, (this.layoutWidth - AndroidUtilities.dp(22.5f)) - Theme.checkDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.checkDrawable.getIntrinsicHeight());
                                    } else {
                                        setDrawableBounds(Theme.checkDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - Theme.checkDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.checkDrawable.getIntrinsicHeight());
                                    }
                                    Theme.checkDrawable.draw(canvas);
                                }
                            }
                            if (drawCheck1) {
                                if (this.mediaBackground) {
                                    setDrawableBounds(Theme.halfCheckMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.halfCheckMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - Theme.halfCheckMediaDrawable.getIntrinsicHeight());
                                    Theme.halfCheckMediaDrawable.draw(canvas);
                                } else {
                                    setDrawableBounds(Theme.halfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - Theme.halfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.halfCheckDrawable.getIntrinsicHeight());
                                    Theme.halfCheckDrawable.draw(canvas);
                                }
                            }
                        } else if (drawCheck1 || drawCheck2) {
                            if (this.mediaBackground) {
                                setDrawableBounds(Theme.broadcastMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(24.0f)) - Theme.broadcastMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - Theme.broadcastMediaDrawable.getIntrinsicHeight());
                                Theme.broadcastMediaDrawable.draw(canvas);
                            } else {
                                setDrawableBounds(Theme.broadcastDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - Theme.broadcastDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.broadcastDrawable.getIntrinsicHeight());
                                Theme.broadcastDrawable.draw(canvas);
                            }
                        }
                        if (!drawError) {
                            return;
                        }
                        if (this.mediaBackground) {
                            setDrawableBounds(Theme.errorDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - Theme.errorDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(11.5f)) - Theme.errorDrawable.getIntrinsicHeight());
                            Theme.errorDrawable.draw(canvas);
                            return;
                        }
                        setDrawableBounds(Theme.errorDrawable, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - Theme.errorDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(7.0f)) - Theme.errorDrawable.getIntrinsicHeight());
                        Theme.errorDrawable.draw(canvas);
                        return;
                    }
                    return;
                }
                return;
            }
            requestLayout();
        }
    }

    public int getObserverTag() {
        return this.TAG;
    }

    public MessageObject getMessageObject() {
        return this.currentMessageObject;
    }
}
