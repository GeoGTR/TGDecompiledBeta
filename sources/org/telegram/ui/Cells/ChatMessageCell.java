package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.SparseArray;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessageObject.GroupedMessagePosition;
import org.telegram.messenger.MessageObject.GroupedMessages;
import org.telegram.messenger.MessageObject.TextLayoutBlock;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.WebFile;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageReactions;
import org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
import org.telegram.tgnet.TLRPC.TL_poll;
import org.telegram.tgnet.TLRPC.TL_pollAnswer;
import org.telegram.tgnet.TLRPC.TL_pollAnswerVoters;
import org.telegram.tgnet.TLRPC.TL_reactionCount;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.MessageDrawable;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.CheckBoxBase;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.RadialProgress2;
import org.telegram.ui.Components.RoundVideoPlayingDrawable;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate.-CC;
import org.telegram.ui.Components.SeekBarWaveform;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.PhotoViewer;

public class ChatMessageCell extends BaseCell implements SeekBarDelegate, ImageReceiverDelegate, FileDownloadProgressListener {
    private static final int DOCUMENT_ATTACH_TYPE_AUDIO = 3;
    private static final int DOCUMENT_ATTACH_TYPE_DOCUMENT = 1;
    private static final int DOCUMENT_ATTACH_TYPE_GIF = 2;
    private static final int DOCUMENT_ATTACH_TYPE_MUSIC = 5;
    private static final int DOCUMENT_ATTACH_TYPE_NONE = 0;
    private static final int DOCUMENT_ATTACH_TYPE_ROUND = 7;
    private static final int DOCUMENT_ATTACH_TYPE_STICKER = 6;
    private static final int DOCUMENT_ATTACH_TYPE_THEME = 9;
    private static final int DOCUMENT_ATTACH_TYPE_VIDEO = 4;
    private static final int DOCUMENT_ATTACH_TYPE_WALLPAPER = 8;
    private int TAG;
    private SparseArray<Rect> accessibilityVirtualViewBounds = new SparseArray();
    private int addedCaptionHeight;
    private boolean addedForTest;
    private StaticLayout adminLayout;
    private boolean allowAssistant;
    private boolean animatePollAnswer;
    private boolean animatePollAnswerAlpha;
    private int animatingDrawVideoImageButton;
    private float animatingDrawVideoImageButtonProgress;
    private int animatingNoSound;
    private boolean animatingNoSoundPlaying;
    private float animatingNoSoundProgress;
    private boolean animationRunning;
    private boolean attachedToWindow;
    private StaticLayout authorLayout;
    private int authorX;
    private boolean autoPlayingMedia;
    private int availableTimeWidth;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver avatarImage = new ImageReceiver();
    private boolean avatarPressed;
    private int backgroundDrawableLeft;
    private int backgroundDrawableRight;
    private int backgroundWidth = 100;
    private ArrayList<BotButton> botButtons = new ArrayList();
    private HashMap<String, BotButton> botButtonsByData = new HashMap();
    private HashMap<String, BotButton> botButtonsByPosition = new HashMap();
    private String botButtonsLayout;
    private boolean bottomNearToSet;
    private int buttonPressed;
    private int buttonState;
    private int buttonX;
    private int buttonY;
    private boolean canStreamVideo;
    private boolean cancelLoading;
    private int captionHeight;
    private StaticLayout captionLayout;
    private int captionOffsetX;
    private int captionWidth;
    private int captionX;
    private int captionY;
    private CheckBoxBase checkBox;
    private boolean checkBoxAnimationInProgress;
    private float checkBoxAnimationProgress;
    private int checkBoxTranslation;
    private boolean checkBoxVisible;
    private boolean checkOnlyButtonPressed;
    private AvatarDrawable contactAvatarDrawable;
    private float controlsAlpha = 1.0f;
    private int currentAccount = UserConfig.selectedAccount;
    private Drawable currentBackgroundDrawable;
    private CharSequence currentCaption;
    private Chat currentChat;
    private int currentFocusedVirtualView = -1;
    private Chat currentForwardChannel;
    private String currentForwardName;
    private String currentForwardNameString;
    private User currentForwardUser;
    private int currentMapProvider;
    private MessageObject currentMessageObject;
    private GroupedMessages currentMessagesGroup;
    private String currentNameString;
    private FileLocation currentPhoto;
    private String currentPhotoFilter;
    private String currentPhotoFilterThumb;
    private PhotoSize currentPhotoObject;
    private PhotoSize currentPhotoObjectThumb;
    private GroupedMessagePosition currentPosition;
    private PhotoSize currentReplyPhoto;
    private String currentTimeString;
    private String currentUrl;
    private User currentUser;
    private User currentViaBotUser;
    private String currentViewsString;
    private WebFile currentWebFile;
    private ChatMessageCellDelegate delegate;
    private RectF deleteProgressRect = new RectF();
    private StaticLayout descriptionLayout;
    private int descriptionX;
    private int descriptionY;
    private boolean disallowLongPress;
    private StaticLayout docTitleLayout;
    private int docTitleOffsetX;
    private int docTitleWidth;
    private Document documentAttach;
    private int documentAttachType;
    private boolean drawBackground = true;
    private boolean drawForwardedName;
    private boolean drawImageButton;
    private boolean drawInstantView;
    private int drawInstantViewType;
    private boolean drawJoinChannelView;
    private boolean drawJoinGroupView;
    private boolean drawName;
    private boolean drawNameLayout;
    private boolean drawPhotoCheckBox;
    private boolean drawPhotoImage;
    private boolean drawPinnedBottom;
    private boolean drawPinnedTop;
    private boolean drawRadialCheckBackground;
    private boolean drawSelectionBackground;
    private boolean drawShareButton;
    private boolean drawTime = true;
    private boolean drawVideoImageButton;
    private boolean drawVideoSize;
    private boolean drwaShareGoIcon;
    private StaticLayout durationLayout;
    private int durationWidth;
    private boolean firstCircleLength;
    private int firstVisibleBlockNum;
    private boolean forceNotDrawTime;
    private boolean forwardBotPressed;
    private boolean forwardName;
    private int forwardNameCenterX;
    private float[] forwardNameOffsetX = new float[2];
    private boolean forwardNamePressed;
    private int forwardNameX;
    private int forwardNameY;
    private StaticLayout[] forwardedNameLayout = new StaticLayout[2];
    private int forwardedNameWidth;
    private boolean fullyDraw;
    private boolean gamePreviewPressed;
    private boolean groupPhotoInvisible;
    private GroupedMessages groupedMessagesToSet;
    private boolean hasEmbed;
    private boolean hasGamePreview;
    private boolean hasInvoicePreview;
    private boolean hasLinkPreview;
    private int hasMiniProgress;
    private boolean hasNewLineForTime;
    private boolean hasOldCaptionPreview;
    private int highlightProgress;
    private int imageBackgroundColor;
    private int imageBackgroundSideColor;
    private int imageBackgroundSideWidth;
    private boolean imagePressed;
    private boolean inLayout;
    private StaticLayout infoLayout;
    private int infoWidth;
    private int infoX;
    private boolean instantButtonPressed;
    private RectF instantButtonRect = new RectF();
    private boolean instantPressed;
    private int instantTextLeftX;
    private int instantTextX;
    private StaticLayout instantViewLayout;
    private int instantWidth;
    private Runnable invalidateRunnable = new Runnable() {
        public void run() {
            ChatMessageCell.this.checkLocationExpired();
            if (ChatMessageCell.this.locationExpired) {
                ChatMessageCell.this.invalidate();
                ChatMessageCell.this.scheduledInvalidate = false;
                return;
            }
            ChatMessageCell chatMessageCell = ChatMessageCell.this;
            chatMessageCell.invalidate(((int) chatMessageCell.rect.left) - 5, ((int) ChatMessageCell.this.rect.top) - 5, ((int) ChatMessageCell.this.rect.right) + 5, ((int) ChatMessageCell.this.rect.bottom) + 5);
            if (ChatMessageCell.this.scheduledInvalidate) {
                AndroidUtilities.runOnUIThread(ChatMessageCell.this.invalidateRunnable, 1000);
            }
        }
    };
    private boolean invalidatesParent;
    private boolean isAvatarVisible;
    public boolean isChat;
    private boolean isCheckPressed = true;
    private boolean isHighlighted;
    private boolean isHighlightedAnimated;
    public boolean isMegagroup;
    private boolean isPressed;
    private boolean isSmallImage;
    private int keyboardHeight;
    private long lastAnimationTime;
    private long lastCheckBoxAnimationTime;
    private long lastControlsAlphaChangeTime;
    private int lastDeleteDate;
    private int lastHeight;
    private long lastHighlightProgressTime;
    private TL_poll lastPoll;
    private ArrayList<TL_pollAnswerVoters> lastPollResults;
    private int lastPollResultsVoters;
    private TL_messageReactions lastReactions;
    private int lastSendState;
    private int lastTime;
    private float lastTouchX;
    private float lastTouchY;
    private int lastViewsCount;
    private int lastVisibleBlockNum;
    private int layoutHeight;
    private int layoutWidth;
    private int linkBlockNum;
    private int linkPreviewHeight;
    private boolean linkPreviewPressed;
    private int linkSelectionBlockNum;
    private boolean locationExpired;
    private ImageReceiver locationImageReceiver;
    private boolean mediaBackground;
    private int mediaOffsetY;
    private boolean mediaWasInvisible;
    private MessageObject messageObjectToSet;
    private int miniButtonPressed;
    private int miniButtonState;
    private StaticLayout nameLayout;
    private float nameOffsetX;
    private int nameWidth;
    private float nameX;
    private float nameY;
    private int namesOffset;
    private boolean needNewVisiblePart;
    private boolean needReplyImage;
    private int noSoundCenterX;
    private boolean otherPressed;
    private int otherX;
    private int otherY;
    private StaticLayout performerLayout;
    private int performerX;
    private CheckBoxBase photoCheckBox;
    private ImageReceiver photoImage;
    private boolean photoNotSet;
    private TLObject photoParentObject;
    private StaticLayout photosCountLayout;
    private int photosCountWidth;
    private boolean pinnedBottom;
    private boolean pinnedTop;
    private float pollAnimationProgress;
    private float pollAnimationProgressTime;
    private ArrayList<PollButton> pollButtons = new ArrayList();
    private boolean pollClosed;
    private boolean pollUnvoteInProgress;
    private boolean pollVoteInProgress;
    private int pollVoteInProgressNum;
    private boolean pollVoted;
    private int pressedBotButton;
    private CharacterStyle pressedLink;
    private int pressedLinkType;
    private int[] pressedState = new int[]{16842910, 16842919};
    private int pressedVoteButton;
    private RadialProgress2 radialProgress;
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
    private RoundVideoPlayingDrawable roundVideoPlayingDrawable;
    private boolean scheduledInvalidate;
    private Rect scrollRect = new Rect();
    private SeekBar seekBar;
    private SeekBarWaveform seekBarWaveform;
    private int seekBarX;
    private int seekBarY;
    private Drawable selectorDrawable;
    private int selectorDrawableMaskType;
    private boolean sharePressed;
    private int shareStartX;
    private int shareStartY;
    private StaticLayout siteNameLayout;
    private boolean siteNameRtl;
    private int siteNameWidth;
    private StaticLayout songLayout;
    private int songX;
    private int substractBackgroundHeight;
    private int textX;
    private int textY;
    private float timeAlpha = 1.0f;
    private int timeAudioX;
    private StaticLayout timeLayout;
    private int timeTextWidth;
    private boolean timeWasInvisible;
    private int timeWidth;
    private int timeWidthAudio;
    private int timeX;
    private StaticLayout titleLayout;
    private int titleX;
    private boolean topNearToSet;
    private long totalChangeTime;
    private int totalHeight;
    private int totalVisibleBlocksCount;
    private int unmovedTextX;
    private ArrayList<LinkPath> urlPath = new ArrayList();
    private ArrayList<LinkPath> urlPathCache = new ArrayList();
    private ArrayList<LinkPath> urlPathSelection = new ArrayList();
    private boolean useSeekBarWaweform;
    private int viaNameWidth;
    private TypefaceSpan viaSpan1;
    private TypefaceSpan viaSpan2;
    private int viaWidth;
    private int videoButtonPressed;
    private int videoButtonX;
    private int videoButtonY;
    private StaticLayout videoInfoLayout;
    private RadialProgress2 videoRadialProgress;
    private StaticLayout viewsLayout;
    private int viewsTextWidth;
    private float voteCurrentCircleLength;
    private float voteCurrentProgressTime;
    private long voteLastUpdateTime;
    private float voteRadOffset;
    private boolean voteRisingCircleLength;
    private boolean wasLayout;
    private boolean wasSending;
    private int widthBeforeNewTimeLine;
    private int widthForButtons;

    private class BotButton {
        private int angle;
        private KeyboardButton button;
        private int height;
        private long lastUpdateTime;
        private float progressAlpha;
        private TL_reactionCount reaction;
        private StaticLayout title;
        private int width;
        private int x;
        private int y;

        private BotButton() {
        }

        /* synthetic */ BotButton(ChatMessageCell chatMessageCell, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    public interface ChatMessageCellDelegate {

        public final /* synthetic */ class -CC {
            public static boolean $default$canPerformActions(ChatMessageCellDelegate chatMessageCellDelegate) {
                return false;
            }

            public static void $default$didLongPress(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, float f, float f2) {
            }

            public static void $default$didPressBotButton(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, KeyboardButton keyboardButton) {
            }

            public static void $default$didPressCancelSendButton(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell) {
            }

            public static void $default$didPressChannelAvatar(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, Chat chat, int i, float f, float f2) {
            }

            public static void $default$didPressHiddenForward(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell) {
            }

            public static void $default$didPressImage(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, float f, float f2) {
            }

            public static void $default$didPressInstantButton(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, int i) {
            }

            public static void $default$didPressOther(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, float f, float f2) {
            }

            public static void $default$didPressReaction(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, TL_reactionCount tL_reactionCount) {
            }

            public static void $default$didPressReplyMessage(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, int i) {
            }

            public static void $default$didPressShare(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell) {
            }

            public static void $default$didPressUrl(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, CharacterStyle characterStyle, boolean z) {
            }

            public static void $default$didPressUserAvatar(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, User user, float f, float f2) {
            }

            public static void $default$didPressViaBot(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, String str) {
            }

            public static void $default$didPressVoteButton(ChatMessageCellDelegate chatMessageCellDelegate, ChatMessageCell chatMessageCell, TL_pollAnswer tL_pollAnswer) {
            }

            public static void $default$didStartVideoStream(ChatMessageCellDelegate chatMessageCellDelegate, MessageObject messageObject) {
            }

            public static String $default$getAdminRank(ChatMessageCellDelegate chatMessageCellDelegate, int i) {
                return null;
            }

            public static void $default$needOpenWebView(ChatMessageCellDelegate chatMessageCellDelegate, String str, String str2, String str3, String str4, int i, int i2) {
            }

            public static boolean $default$needPlayMessage(ChatMessageCellDelegate chatMessageCellDelegate, MessageObject messageObject) {
                return false;
            }

            public static void $default$setShouldNotRepeatSticker(ChatMessageCellDelegate chatMessageCellDelegate, MessageObject messageObject) {
            }

            public static boolean $default$shouldRepeatSticker(ChatMessageCellDelegate chatMessageCellDelegate, MessageObject messageObject) {
                return true;
            }

            public static void $default$videoTimerReached(ChatMessageCellDelegate chatMessageCellDelegate) {
            }
        }

        boolean canPerformActions();

        void didLongPress(ChatMessageCell chatMessageCell, float f, float f2);

        void didPressBotButton(ChatMessageCell chatMessageCell, KeyboardButton keyboardButton);

        void didPressCancelSendButton(ChatMessageCell chatMessageCell);

        void didPressChannelAvatar(ChatMessageCell chatMessageCell, Chat chat, int i, float f, float f2);

        void didPressHiddenForward(ChatMessageCell chatMessageCell);

        void didPressImage(ChatMessageCell chatMessageCell, float f, float f2);

        void didPressInstantButton(ChatMessageCell chatMessageCell, int i);

        void didPressOther(ChatMessageCell chatMessageCell, float f, float f2);

        void didPressReaction(ChatMessageCell chatMessageCell, TL_reactionCount tL_reactionCount);

        void didPressReplyMessage(ChatMessageCell chatMessageCell, int i);

        void didPressShare(ChatMessageCell chatMessageCell);

        void didPressUrl(ChatMessageCell chatMessageCell, CharacterStyle characterStyle, boolean z);

        void didPressUserAvatar(ChatMessageCell chatMessageCell, User user, float f, float f2);

        void didPressViaBot(ChatMessageCell chatMessageCell, String str);

        void didPressVoteButton(ChatMessageCell chatMessageCell, TL_pollAnswer tL_pollAnswer);

        void didStartVideoStream(MessageObject messageObject);

        String getAdminRank(int i);

        void needOpenWebView(String str, String str2, String str3, String str4, int i, int i2);

        boolean needPlayMessage(MessageObject messageObject);

        void setShouldNotRepeatSticker(MessageObject messageObject);

        boolean shouldRepeatSticker(MessageObject messageObject);

        void videoTimerReached();
    }

    private class MessageAccessibilityNodeProvider extends AccessibilityNodeProvider {
        private final int BOT_BUTTONS_START;
        private final int INSTANT_VIEW;
        private final int LINK_IDS_START;
        private final int POLL_BUTTONS_START;
        private final int REPLY;
        private final int SHARE;
        private Path linkPath;
        private Rect rect;
        private RectF rectF;

        private MessageAccessibilityNodeProvider() {
            this.LINK_IDS_START = 2000;
            this.BOT_BUTTONS_START = 1000;
            this.POLL_BUTTONS_START = 500;
            this.INSTANT_VIEW = 499;
            this.SHARE = 498;
            this.REPLY = 497;
            this.linkPath = new Path();
            this.rectF = new RectF();
            this.rect = new Rect();
        }

        /* synthetic */ MessageAccessibilityNodeProvider(ChatMessageCell chatMessageCell, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(int i) {
            int[] iArr = new int[]{0, 0};
            ChatMessageCell.this.getLocationOnScreen(iArr);
            CharSequence charSequence = null;
            String str = ", ";
            int i2 = 0;
            String stringBuilder;
            int i3;
            int i4;
            if (i == -1) {
                int access$3300;
                AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain(ChatMessageCell.this);
                ChatMessageCell.this.onInitializeAccessibilityNodeInfo(obtain);
                StringBuilder stringBuilder2 = new StringBuilder();
                ChatMessageCell chatMessageCell = ChatMessageCell.this;
                if (!(!chatMessageCell.isChat || chatMessageCell.currentUser == null || ChatMessageCell.this.currentMessageObject.isOut())) {
                    stringBuilder2.append(UserObject.getUserName(ChatMessageCell.this.currentUser));
                    stringBuilder2.append(10);
                }
                if (!TextUtils.isEmpty(ChatMessageCell.this.currentMessageObject.messageText)) {
                    stringBuilder2.append(ChatMessageCell.this.currentMessageObject.messageText);
                }
                String str2 = "\n";
                if (ChatMessageCell.this.currentMessageObject.isMusic()) {
                    stringBuilder2.append(str2);
                    stringBuilder2.append(LocaleController.formatString("AccDescrMusicInfo", NUM, ChatMessageCell.this.currentMessageObject.getMusicAuthor(), ChatMessageCell.this.currentMessageObject.getMusicTitle()));
                } else if (ChatMessageCell.this.currentMessageObject.isVoice() || ChatMessageCell.this.currentMessageObject.isRoundVideo()) {
                    stringBuilder2.append(str);
                    stringBuilder2.append(LocaleController.formatCallDuration(ChatMessageCell.this.currentMessageObject.getDuration()));
                    if (ChatMessageCell.this.currentMessageObject.isContentUnread()) {
                        stringBuilder2.append(str);
                        stringBuilder2.append(LocaleController.getString("AccDescrMsgNotPlayed", NUM));
                    }
                }
                if (ChatMessageCell.this.lastPoll != null) {
                    stringBuilder2.append(str);
                    stringBuilder2.append(ChatMessageCell.this.lastPoll.question);
                    stringBuilder2.append(str);
                    stringBuilder2.append(LocaleController.getString("AnonymousPoll", NUM));
                }
                if (!(ChatMessageCell.this.currentMessageObject.messageOwner.media == null || TextUtils.isEmpty(ChatMessageCell.this.currentMessageObject.caption))) {
                    stringBuilder2.append(str2);
                    stringBuilder2.append(ChatMessageCell.this.currentMessageObject.caption);
                }
                stringBuilder2.append(str2);
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append(LocaleController.getString("TodayAt", NUM));
                stringBuilder3.append(" ");
                stringBuilder3.append(ChatMessageCell.this.currentTimeString);
                stringBuilder = stringBuilder3.toString();
                if (ChatMessageCell.this.currentMessageObject.isOut()) {
                    stringBuilder2.append(LocaleController.formatString("AccDescrSentDate", NUM, stringBuilder));
                    stringBuilder2.append(str);
                    if (ChatMessageCell.this.currentMessageObject.isUnread()) {
                        i3 = NUM;
                        stringBuilder = "AccDescrMsgUnread";
                    } else {
                        i3 = NUM;
                        stringBuilder = "AccDescrMsgRead";
                    }
                    stringBuilder2.append(LocaleController.getString(stringBuilder, i3));
                } else {
                    stringBuilder2.append(LocaleController.formatString("AccDescrReceivedDate", NUM, stringBuilder));
                }
                obtain.setContentDescription(stringBuilder2.toString());
                obtain.setEnabled(true);
                if (VERSION.SDK_INT >= 19) {
                    CollectionItemInfo collectionItemInfo = obtain.getCollectionItemInfo();
                    if (collectionItemInfo != null) {
                        obtain.setCollectionItemInfo(CollectionItemInfo.obtain(collectionItemInfo.getRowIndex(), 1, 0, 1, false));
                    }
                }
                if (VERSION.SDK_INT >= 21) {
                    obtain.addAction(new AccessibilityAction(NUM, LocaleController.getString("AccActionMessageOptions", NUM)));
                    access$3300 = ChatMessageCell.this.getIconForCurrentState();
                    if (access$3300 == 0) {
                        charSequence = LocaleController.getString("AccActionPlay", NUM);
                    } else if (access$3300 == 1) {
                        charSequence = LocaleController.getString("AccActionPause", NUM);
                    } else if (access$3300 == 2) {
                        charSequence = LocaleController.getString("AccActionDownload", NUM);
                    } else if (access$3300 == 3) {
                        charSequence = LocaleController.getString("AccActionCancelDownload", NUM);
                    } else if (access$3300 == 5) {
                        charSequence = LocaleController.getString("AccActionOpenFile", NUM);
                    } else if (ChatMessageCell.this.currentMessageObject.type == 16) {
                        charSequence = LocaleController.getString("CallAgain", NUM);
                    }
                    obtain.addAction(new AccessibilityAction(16, charSequence));
                    obtain.addAction(new AccessibilityAction(32, LocaleController.getString("AccActionEnterSelectionMode", NUM)));
                    if (ChatMessageCell.this.getMiniIconForCurrentState() == 2) {
                        obtain.addAction(new AccessibilityAction(NUM, LocaleController.getString("AccActionDownload", NUM)));
                    }
                } else {
                    obtain.addAction(16);
                    obtain.addAction(32);
                }
                if (ChatMessageCell.this.currentMessageObject.messageText instanceof Spannable) {
                    Spannable spannable = (Spannable) ChatMessageCell.this.currentMessageObject.messageText;
                    i4 = 0;
                    for (CharacterStyle characterStyle : (CharacterStyle[]) spannable.getSpans(0, spannable.length(), ClickableSpan.class)) {
                        obtain.addChild(ChatMessageCell.this, i4 + 2000);
                        i4++;
                    }
                }
                Iterator it = ChatMessageCell.this.botButtons.iterator();
                access$3300 = 0;
                while (it.hasNext()) {
                    BotButton botButton = (BotButton) it.next();
                    obtain.addChild(ChatMessageCell.this, access$3300 + 1000);
                    access$3300++;
                }
                it = ChatMessageCell.this.pollButtons.iterator();
                while (it.hasNext()) {
                    PollButton pollButton = (PollButton) it.next();
                    obtain.addChild(ChatMessageCell.this, i2 + 500);
                    i2++;
                }
                if (ChatMessageCell.this.drawInstantView) {
                    obtain.addChild(ChatMessageCell.this, 499);
                }
                if (ChatMessageCell.this.drawShareButton) {
                    obtain.addChild(ChatMessageCell.this, 498);
                }
                if (ChatMessageCell.this.replyNameLayout != null) {
                    obtain.addChild(ChatMessageCell.this, 497);
                }
                if (ChatMessageCell.this.drawSelectionBackground || ChatMessageCell.this.getBackground() != null) {
                    obtain.setSelected(true);
                }
                return obtain;
            }
            AccessibilityNodeInfo obtain2 = AccessibilityNodeInfo.obtain();
            obtain2.setSource(ChatMessageCell.this, i);
            obtain2.setParent(ChatMessageCell.this);
            obtain2.setPackageName(ChatMessageCell.this.getContext().getPackageName());
            if (i >= 2000) {
                Spannable spannable2 = (Spannable) ChatMessageCell.this.currentMessageObject.messageText;
                ClickableSpan linkById = getLinkById(i);
                if (linkById == null) {
                    return null;
                }
                int[] access$4100 = ChatMessageCell.this.getRealSpanStartAndEnd(spannable2, linkById);
                obtain2.setText(spannable2.subSequence(access$4100[0], access$4100[1]).toString());
                Iterator it2 = ChatMessageCell.this.currentMessageObject.textLayoutBlocks.iterator();
                while (it2.hasNext()) {
                    TextLayoutBlock textLayoutBlock = (TextLayoutBlock) it2.next();
                    i3 = textLayoutBlock.textLayout.getText().length();
                    int i5 = textLayoutBlock.charactersOffset;
                    if (i5 <= access$4100[0] && i3 + i5 >= access$4100[1]) {
                        textLayoutBlock.textLayout.getSelectionPath(access$4100[0] - i5, access$4100[1] - i5, this.linkPath);
                        this.linkPath.computeBounds(this.rectF, true);
                        Rect rect = this.rect;
                        RectF rectF = this.rectF;
                        rect.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                        this.rect.offset(0, (int) textLayoutBlock.textYOffset);
                        this.rect.offset(ChatMessageCell.this.textX, ChatMessageCell.this.textY);
                        obtain2.setBoundsInParent(this.rect);
                        if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null) {
                            ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                        }
                        this.rect.offset(iArr[0], iArr[1]);
                        obtain2.setBoundsInScreen(this.rect);
                        obtain2.setClassName("android.widget.TextView");
                        obtain2.setEnabled(true);
                        obtain2.setClickable(true);
                        obtain2.setLongClickable(true);
                        obtain2.addAction(16);
                        obtain2.addAction(32);
                    }
                }
                obtain2.setClassName("android.widget.TextView");
                obtain2.setEnabled(true);
                obtain2.setClickable(true);
                obtain2.setLongClickable(true);
                obtain2.addAction(16);
                obtain2.addAction(32);
            } else {
                stringBuilder = "android.widget.Button";
                int i6;
                if (i >= 1000) {
                    i6 = i - 1000;
                    if (i6 >= ChatMessageCell.this.botButtons.size()) {
                        return null;
                    }
                    BotButton botButton2 = (BotButton) ChatMessageCell.this.botButtons.get(i6);
                    obtain2.setText(botButton2.title.getText());
                    obtain2.setClassName(stringBuilder);
                    obtain2.setEnabled(true);
                    obtain2.setClickable(true);
                    obtain2.addAction(16);
                    this.rect.set(botButton2.x, botButton2.y, botButton2.x + botButton2.width, botButton2.y + botButton2.height);
                    if (ChatMessageCell.this.currentMessageObject.isOutOwner()) {
                        i6 = (ChatMessageCell.this.getMeasuredWidth() - ChatMessageCell.this.widthForButtons) - AndroidUtilities.dp(10.0f);
                    } else {
                        i6 = ChatMessageCell.this.backgroundDrawableLeft + AndroidUtilities.dp(ChatMessageCell.this.mediaBackground ? 1.0f : 7.0f);
                    }
                    this.rect.offset(i6, ChatMessageCell.this.layoutHeight);
                    obtain2.setBoundsInParent(this.rect);
                    if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null) {
                        ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                    }
                    this.rect.offset(iArr[0], iArr[1]);
                    obtain2.setBoundsInScreen(this.rect);
                } else if (i >= 500) {
                    i6 = i - 500;
                    if (i6 >= ChatMessageCell.this.pollButtons.size()) {
                        return null;
                    }
                    PollButton pollButton2 = (PollButton) ChatMessageCell.this.pollButtons.get(i6);
                    obtain2.setText(pollButton2.title.getText());
                    if (ChatMessageCell.this.pollVoted) {
                        StringBuilder stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(obtain2.getText());
                        stringBuilder4.append(str);
                        stringBuilder4.append(pollButton2.percent);
                        stringBuilder4.append("%");
                        obtain2.setText(stringBuilder4.toString());
                    } else {
                        obtain2.setClassName(stringBuilder);
                    }
                    obtain2.setEnabled(true);
                    obtain2.addAction(16);
                    this.rect.set(pollButton2.x, pollButton2.y, pollButton2.x + (ChatMessageCell.this.backgroundWidth - AndroidUtilities.dp(76.0f)), pollButton2.y + pollButton2.height);
                    obtain2.setBoundsInParent(this.rect);
                    if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null) {
                        ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                    }
                    this.rect.offset(iArr[0], iArr[1]);
                    obtain2.setBoundsInScreen(this.rect);
                    obtain2.setClickable(true);
                } else if (i == 499) {
                    obtain2.setClassName(stringBuilder);
                    obtain2.setEnabled(true);
                    if (ChatMessageCell.this.instantViewLayout != null) {
                        obtain2.setText(ChatMessageCell.this.instantViewLayout.getText());
                    }
                    obtain2.addAction(16);
                    i6 = ChatMessageCell.this.photoImage.getImageX();
                    int measuredHeight = ChatMessageCell.this.getMeasuredHeight() - AndroidUtilities.dp(64.0f);
                    if (ChatMessageCell.this.currentMessageObject.isOutOwner()) {
                        i4 = (ChatMessageCell.this.getMeasuredWidth() - ChatMessageCell.this.widthForButtons) - AndroidUtilities.dp(10.0f);
                    } else {
                        i4 = AndroidUtilities.dp(ChatMessageCell.this.mediaBackground ? 1.0f : 7.0f) + ChatMessageCell.this.backgroundDrawableLeft;
                    }
                    this.rect.set(i6 + i4, measuredHeight, (i6 + ChatMessageCell.this.instantWidth) + i4, AndroidUtilities.dp(38.0f) + measuredHeight);
                    obtain2.setBoundsInParent(this.rect);
                    if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null || !((Rect) ChatMessageCell.this.accessibilityVirtualViewBounds.get(i)).equals(this.rect)) {
                        ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                    }
                    this.rect.offset(iArr[0], iArr[1]);
                    obtain2.setBoundsInScreen(this.rect);
                    obtain2.setClickable(true);
                } else if (i == 498) {
                    obtain2.setClassName("android.widget.ImageButton");
                    obtain2.setEnabled(true);
                    ChatMessageCell chatMessageCell2 = ChatMessageCell.this;
                    if (chatMessageCell2.isOpenChatByShare(chatMessageCell2.currentMessageObject)) {
                        obtain2.setContentDescription(LocaleController.getString("AccDescrOpenChat", NUM));
                    } else {
                        obtain2.setContentDescription(LocaleController.getString("ShareFile", NUM));
                    }
                    obtain2.addAction(16);
                    this.rect.set(ChatMessageCell.this.shareStartX, ChatMessageCell.this.shareStartY, ChatMessageCell.this.shareStartX + AndroidUtilities.dp(40.0f), ChatMessageCell.this.shareStartY + AndroidUtilities.dp(32.0f));
                    obtain2.setBoundsInParent(this.rect);
                    if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null || !((Rect) ChatMessageCell.this.accessibilityVirtualViewBounds.get(i)).equals(this.rect)) {
                        ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                    }
                    this.rect.offset(iArr[0], iArr[1]);
                    obtain2.setBoundsInScreen(this.rect);
                    obtain2.setClickable(true);
                } else if (i == 497) {
                    obtain2.setEnabled(true);
                    StringBuilder stringBuilder5 = new StringBuilder();
                    stringBuilder5.append(LocaleController.getString("Reply", NUM));
                    stringBuilder5.append(str);
                    if (ChatMessageCell.this.replyNameLayout != null) {
                        stringBuilder5.append(ChatMessageCell.this.replyNameLayout.getText());
                        stringBuilder5.append(str);
                    }
                    if (ChatMessageCell.this.replyTextLayout != null) {
                        stringBuilder5.append(ChatMessageCell.this.replyTextLayout.getText());
                    }
                    obtain2.setContentDescription(stringBuilder5.toString());
                    obtain2.addAction(16);
                    this.rect.set(ChatMessageCell.this.replyStartX, ChatMessageCell.this.replyStartY, ChatMessageCell.this.replyStartX + Math.max(ChatMessageCell.this.replyNameWidth, ChatMessageCell.this.replyTextWidth), ChatMessageCell.this.replyStartY + AndroidUtilities.dp(35.0f));
                    obtain2.setBoundsInParent(this.rect);
                    if (ChatMessageCell.this.accessibilityVirtualViewBounds.get(i) == null || !((Rect) ChatMessageCell.this.accessibilityVirtualViewBounds.get(i)).equals(this.rect)) {
                        ChatMessageCell.this.accessibilityVirtualViewBounds.put(i, new Rect(this.rect));
                    }
                    this.rect.offset(iArr[0], iArr[1]);
                    obtain2.setBoundsInScreen(this.rect);
                    obtain2.setClickable(true);
                }
            }
            obtain2.setFocusable(true);
            obtain2.setVisibleToUser(true);
            return obtain2;
        }

        public boolean performAction(int i, int i2, Bundle bundle) {
            ClickableSpan linkById;
            if (i == -1) {
                ChatMessageCell.this.performAccessibilityAction(i2, bundle);
            } else if (i2 == 64) {
                ChatMessageCell.this.sendAccessibilityEventForVirtualView(i, 32768);
            } else if (i2 == 16) {
                ChatMessageCellDelegate access$6300;
                ChatMessageCell chatMessageCell;
                if (i >= 2000) {
                    linkById = getLinkById(i);
                    if (linkById != null) {
                        ChatMessageCell.this.delegate.didPressUrl(ChatMessageCell.this, linkById, false);
                        ChatMessageCell.this.sendAccessibilityEventForVirtualView(i, 1);
                    }
                } else if (i >= 1000) {
                    i2 = i - 1000;
                    if (i2 >= ChatMessageCell.this.botButtons.size()) {
                        return false;
                    }
                    BotButton botButton = (BotButton) ChatMessageCell.this.botButtons.get(i2);
                    if (ChatMessageCell.this.delegate != null) {
                        if (botButton.button != null) {
                            ChatMessageCell.this.delegate.didPressBotButton(ChatMessageCell.this, botButton.button);
                        } else if (botButton.reaction != null) {
                            ChatMessageCell.this.delegate.didPressReaction(ChatMessageCell.this, botButton.reaction);
                        }
                    }
                    ChatMessageCell.this.sendAccessibilityEventForVirtualView(i, 1);
                } else if (i >= 500) {
                    i2 = i - 500;
                    if (i2 >= ChatMessageCell.this.pollButtons.size()) {
                        return false;
                    }
                    PollButton pollButton = (PollButton) ChatMessageCell.this.pollButtons.get(i2);
                    if (ChatMessageCell.this.delegate != null) {
                        ChatMessageCell.this.delegate.didPressVoteButton(ChatMessageCell.this, pollButton.answer);
                    }
                    ChatMessageCell.this.sendAccessibilityEventForVirtualView(i, 1);
                } else if (i == 499) {
                    if (ChatMessageCell.this.delegate != null) {
                        access$6300 = ChatMessageCell.this.delegate;
                        chatMessageCell = ChatMessageCell.this;
                        access$6300.didPressInstantButton(chatMessageCell, chatMessageCell.drawInstantViewType);
                    }
                } else if (i == 498) {
                    if (ChatMessageCell.this.delegate != null) {
                        ChatMessageCell.this.delegate.didPressShare(ChatMessageCell.this);
                    }
                } else if (i == 497 && ChatMessageCell.this.delegate != null && ChatMessageCell.this.currentMessageObject.hasValidReplyMessageObject()) {
                    access$6300 = ChatMessageCell.this.delegate;
                    chatMessageCell = ChatMessageCell.this;
                    access$6300.didPressReplyMessage(chatMessageCell, chatMessageCell.currentMessageObject.messageOwner.reply_to_msg_id);
                }
            } else if (i2 == 32) {
                linkById = getLinkById(i);
                if (linkById != null) {
                    ChatMessageCell.this.delegate.didPressUrl(ChatMessageCell.this, linkById, true);
                    ChatMessageCell.this.sendAccessibilityEventForVirtualView(i, 2);
                }
            }
            return true;
        }

        private ClickableSpan getLinkById(int i) {
            i -= 2000;
            if (!(ChatMessageCell.this.currentMessageObject.messageText instanceof Spannable)) {
                return null;
            }
            Spannable spannable = (Spannable) ChatMessageCell.this.currentMessageObject.messageText;
            ClickableSpan[] clickableSpanArr = (ClickableSpan[]) spannable.getSpans(0, spannable.length(), ClickableSpan.class);
            if (clickableSpanArr.length <= i) {
                return null;
            }
            return clickableSpanArr[i];
        }
    }

    private class PollButton {
        private TL_pollAnswer answer;
        private float decimal;
        private int height;
        private int percent;
        private float percentProgress;
        private int prevPercent;
        private float prevPercentProgress;
        private StaticLayout title;
        private int x;
        private int y;

        private PollButton() {
        }

        /* synthetic */ PollButton(ChatMessageCell chatMessageCell, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    private boolean intersect(float f, float f2, float f3, float f4) {
        boolean z = true;
        if (f <= f3) {
            if (f2 < f3) {
                z = false;
            }
            return z;
        }
        if (f > f4) {
            z = false;
        }
        return z;
    }

    public /* synthetic */ void onSeekBarContinuousDrag(float f) {
        -CC.$default$onSeekBarContinuousDrag(this, f);
    }

    public ChatMessageCell(Context context) {
        super(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarDrawable = new AvatarDrawable();
        this.replyImageReceiver = new ImageReceiver(this);
        this.locationImageReceiver = new ImageReceiver(this);
        this.locationImageReceiver.setRoundRadius(AndroidUtilities.dp(26.1f));
        this.TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
        this.contactAvatarDrawable = new AvatarDrawable();
        this.photoImage = new ImageReceiver(this);
        this.photoImage.setDelegate(this);
        this.radialProgress = new RadialProgress2(this);
        this.videoRadialProgress = new RadialProgress2(this);
        this.videoRadialProgress.setDrawBackground(false);
        this.videoRadialProgress.setCircleRadius(AndroidUtilities.dp(15.0f));
        this.seekBar = new SeekBar(context);
        this.seekBar.setDelegate(this);
        this.seekBarWaveform = new SeekBarWaveform(context);
        this.seekBarWaveform.setDelegate(this);
        this.seekBarWaveform.setParentView(this);
        this.roundVideoPlayingDrawable = new RoundVideoPlayingDrawable(this);
    }

    private void resetPressedLink(int i) {
        if (this.pressedLink == null) {
            return;
        }
        if (this.pressedLinkType == i || i == -1) {
            resetUrlPaths(false);
            this.pressedLink = null;
            this.pressedLinkType = -1;
            invalidate();
        }
    }

    private void resetUrlPaths(boolean z) {
        if (z) {
            if (!this.urlPathSelection.isEmpty()) {
                this.urlPathCache.addAll(this.urlPathSelection);
                this.urlPathSelection.clear();
            }
        } else if (!this.urlPath.isEmpty()) {
            this.urlPathCache.addAll(this.urlPath);
            this.urlPath.clear();
        }
    }

    private LinkPath obtainNewUrlPath(boolean z) {
        LinkPath linkPath;
        if (this.urlPathCache.isEmpty()) {
            linkPath = new LinkPath();
        } else {
            linkPath = (LinkPath) this.urlPathCache.get(0);
            this.urlPathCache.remove(0);
        }
        linkPath.reset();
        if (z) {
            this.urlPathSelection.add(linkPath);
        } else {
            this.urlPath.add(linkPath);
        }
        return linkPath;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0020  */
    private int[] getRealSpanStartAndEnd(android.text.Spannable r6, android.text.style.CharacterStyle r7) {
        /*
        r5 = this;
        r0 = r7 instanceof org.telegram.ui.Components.URLSpanBrowser;
        r1 = 1;
        r2 = 0;
        if (r0 == 0) goto L_0x001b;
    L_0x0006:
        r0 = r7;
        r0 = (org.telegram.ui.Components.URLSpanBrowser) r0;
        r0 = r0.getStyle();
        if (r0 == 0) goto L_0x001b;
    L_0x000f:
        r0 = r0.urlEntity;
        if (r0 == 0) goto L_0x001b;
    L_0x0013:
        r3 = r0.offset;
        r0 = r0.length;
        r0 = r0 + r3;
        r4 = r0;
        r0 = 1;
        goto L_0x001e;
    L_0x001b:
        r0 = 0;
        r3 = 0;
        r4 = 0;
    L_0x001e:
        if (r0 != 0) goto L_0x0028;
    L_0x0020:
        r3 = r6.getSpanStart(r7);
        r4 = r6.getSpanEnd(r7);
    L_0x0028:
        r6 = 2;
        r6 = new int[r6];
        r6[r2] = r3;
        r6[r1] = r4;
        return r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.getRealSpanStartAndEnd(android.text.Spannable, android.text.style.CharacterStyle):int[]");
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x00e9 A:{Catch:{ Exception -> 0x01f1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00d6 A:{Catch:{ Exception -> 0x01f1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00e9 A:{Catch:{ Exception -> 0x01f1 }} */
    private boolean checkTextBlockMotionEvent(android.view.MotionEvent r14) {
        /*
        r13 = this;
        r0 = r13.currentMessageObject;
        r1 = r0.type;
        r2 = 0;
        if (r1 != 0) goto L_0x01f9;
    L_0x0007:
        r0 = r0.textLayoutBlocks;
        if (r0 == 0) goto L_0x01f9;
    L_0x000b:
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x01f9;
    L_0x0011:
        r0 = r13.currentMessageObject;
        r0 = r0.messageText;
        r0 = r0 instanceof android.text.Spannable;
        if (r0 != 0) goto L_0x001b;
    L_0x0019:
        goto L_0x01f9;
    L_0x001b:
        r0 = r14.getAction();
        r1 = 1;
        if (r0 == 0) goto L_0x002c;
    L_0x0022:
        r0 = r14.getAction();
        if (r0 != r1) goto L_0x01f9;
    L_0x0028:
        r0 = r13.pressedLinkType;
        if (r0 != r1) goto L_0x01f9;
    L_0x002c:
        r0 = r14.getX();
        r0 = (int) r0;
        r3 = r14.getY();
        r3 = (int) r3;
        r4 = r13.textX;
        if (r0 < r4) goto L_0x01f6;
    L_0x003a:
        r5 = r13.textY;
        if (r3 < r5) goto L_0x01f6;
    L_0x003e:
        r6 = r13.currentMessageObject;
        r7 = r6.textWidth;
        r4 = r4 + r7;
        if (r0 > r4) goto L_0x01f6;
    L_0x0045:
        r4 = r6.textHeight;
        r4 = r4 + r5;
        if (r3 > r4) goto L_0x01f6;
    L_0x004a:
        r3 = r3 - r5;
        r4 = 0;
        r5 = 0;
    L_0x004d:
        r6 = r13.currentMessageObject;
        r6 = r6.textLayoutBlocks;
        r6 = r6.size();
        if (r4 >= r6) goto L_0x006f;
    L_0x0057:
        r6 = r13.currentMessageObject;
        r6 = r6.textLayoutBlocks;
        r6 = r6.get(r4);
        r6 = (org.telegram.messenger.MessageObject.TextLayoutBlock) r6;
        r6 = r6.textYOffset;
        r7 = (float) r3;
        r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r6 <= 0) goto L_0x0069;
    L_0x0068:
        goto L_0x006f;
    L_0x0069:
        r5 = r4 + 1;
        r12 = r5;
        r5 = r4;
        r4 = r12;
        goto L_0x004d;
    L_0x006f:
        r4 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01f1 }
        r4 = r4.textLayoutBlocks;	 Catch:{ Exception -> 0x01f1 }
        r4 = r4.get(r5);	 Catch:{ Exception -> 0x01f1 }
        r4 = (org.telegram.messenger.MessageObject.TextLayoutBlock) r4;	 Catch:{ Exception -> 0x01f1 }
        r0 = (float) r0;	 Catch:{ Exception -> 0x01f1 }
        r6 = r13.textX;	 Catch:{ Exception -> 0x01f1 }
        r6 = (float) r6;	 Catch:{ Exception -> 0x01f1 }
        r7 = r4.isRtl();	 Catch:{ Exception -> 0x01f1 }
        r8 = 0;
        if (r7 == 0) goto L_0x0089;
    L_0x0084:
        r7 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01f1 }
        r7 = r7.textXOffset;	 Catch:{ Exception -> 0x01f1 }
        goto L_0x008a;
    L_0x0089:
        r7 = 0;
    L_0x008a:
        r6 = r6 - r7;
        r0 = r0 - r6;
        r0 = (int) r0;	 Catch:{ Exception -> 0x01f1 }
        r3 = (float) r3;	 Catch:{ Exception -> 0x01f1 }
        r6 = r4.textYOffset;	 Catch:{ Exception -> 0x01f1 }
        r3 = r3 - r6;
        r3 = (int) r3;	 Catch:{ Exception -> 0x01f1 }
        r6 = r4.textLayout;	 Catch:{ Exception -> 0x01f1 }
        r3 = r6.getLineForVertical(r3);	 Catch:{ Exception -> 0x01f1 }
        r6 = r4.textLayout;	 Catch:{ Exception -> 0x01f1 }
        r0 = (float) r0;	 Catch:{ Exception -> 0x01f1 }
        r6 = r6.getOffsetForHorizontal(r3, r0);	 Catch:{ Exception -> 0x01f1 }
        r7 = r4.textLayout;	 Catch:{ Exception -> 0x01f1 }
        r7 = r7.getLineLeft(r3);	 Catch:{ Exception -> 0x01f1 }
        r9 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1));
        if (r9 > 0) goto L_0x01f9;
    L_0x00a9:
        r9 = r4.textLayout;	 Catch:{ Exception -> 0x01f1 }
        r3 = r9.getLineWidth(r3);	 Catch:{ Exception -> 0x01f1 }
        r7 = r7 + r3;
        r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1));
        if (r0 < 0) goto L_0x01f9;
    L_0x00b4:
        r0 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01f1 }
        r0 = r0.messageText;	 Catch:{ Exception -> 0x01f1 }
        r0 = (android.text.Spannable) r0;	 Catch:{ Exception -> 0x01f1 }
        r3 = android.text.style.ClickableSpan.class;
        r3 = r0.getSpans(r6, r6, r3);	 Catch:{ Exception -> 0x01f1 }
        r3 = (android.text.style.CharacterStyle[]) r3;	 Catch:{ Exception -> 0x01f1 }
        if (r3 == 0) goto L_0x00ca;
    L_0x00c4:
        r7 = r3.length;	 Catch:{ Exception -> 0x01f1 }
        if (r7 != 0) goto L_0x00c8;
    L_0x00c7:
        goto L_0x00ca;
    L_0x00c8:
        r6 = 0;
        goto L_0x00d3;
    L_0x00ca:
        r3 = org.telegram.ui.Components.URLSpanMono.class;
        r3 = r0.getSpans(r6, r6, r3);	 Catch:{ Exception -> 0x01f1 }
        r3 = (android.text.style.CharacterStyle[]) r3;	 Catch:{ Exception -> 0x01f1 }
        r6 = 1;
    L_0x00d3:
        r7 = r3.length;	 Catch:{ Exception -> 0x01f1 }
        if (r7 == 0) goto L_0x00e6;
    L_0x00d6:
        r7 = r3.length;	 Catch:{ Exception -> 0x01f1 }
        if (r7 == 0) goto L_0x00e4;
    L_0x00d9:
        r7 = r3[r2];	 Catch:{ Exception -> 0x01f1 }
        r7 = r7 instanceof org.telegram.ui.Components.URLSpanBotCommand;	 Catch:{ Exception -> 0x01f1 }
        if (r7 == 0) goto L_0x00e4;
    L_0x00df:
        r7 = org.telegram.ui.Components.URLSpanBotCommand.enabled;	 Catch:{ Exception -> 0x01f1 }
        if (r7 != 0) goto L_0x00e4;
    L_0x00e3:
        goto L_0x00e6;
    L_0x00e4:
        r7 = 0;
        goto L_0x00e7;
    L_0x00e6:
        r7 = 1;
    L_0x00e7:
        if (r7 != 0) goto L_0x01f9;
    L_0x00e9:
        r14 = r14.getAction();	 Catch:{ Exception -> 0x01f1 }
        if (r14 != 0) goto L_0x01e0;
    L_0x00ef:
        r14 = r3[r2];	 Catch:{ Exception -> 0x01f1 }
        r13.pressedLink = r14;	 Catch:{ Exception -> 0x01f1 }
        r13.linkBlockNum = r5;	 Catch:{ Exception -> 0x01f1 }
        r13.pressedLinkType = r1;	 Catch:{ Exception -> 0x01f1 }
        r13.resetUrlPaths(r2);	 Catch:{ Exception -> 0x01f1 }
        r14 = r13.obtainNewUrlPath(r2);	 Catch:{ Exception -> 0x01d8 }
        r3 = r13.pressedLink;	 Catch:{ Exception -> 0x01d8 }
        r3 = r13.getRealSpanStartAndEnd(r0, r3);	 Catch:{ Exception -> 0x01d8 }
        r7 = r4.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r9 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r14.setCurrentLayout(r7, r9, r8);	 Catch:{ Exception -> 0x01d8 }
        r7 = r4.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r8 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r9 = r3[r1];	 Catch:{ Exception -> 0x01d8 }
        r7.getSelectionPath(r8, r9, r14);	 Catch:{ Exception -> 0x01d8 }
        r14 = r3[r1];	 Catch:{ Exception -> 0x01d8 }
        r7 = r4.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        if (r14 < r7) goto L_0x0177;
    L_0x011a:
        r14 = r5 + 1;
    L_0x011c:
        r7 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7.textLayoutBlocks;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7.size();	 Catch:{ Exception -> 0x01d8 }
        if (r14 >= r7) goto L_0x0177;
    L_0x0126:
        r7 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7.textLayoutBlocks;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7.get(r14);	 Catch:{ Exception -> 0x01d8 }
        r7 = (org.telegram.messenger.MessageObject.TextLayoutBlock) r7;	 Catch:{ Exception -> 0x01d8 }
        if (r6 == 0) goto L_0x013f;
    L_0x0132:
        r8 = r7.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        r9 = r7.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        r10 = org.telegram.ui.Components.URLSpanMono.class;
        r8 = r0.getSpans(r8, r9, r10);	 Catch:{ Exception -> 0x01d8 }
        r8 = (android.text.style.CharacterStyle[]) r8;	 Catch:{ Exception -> 0x01d8 }
        goto L_0x014b;
    L_0x013f:
        r8 = r7.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        r9 = r7.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        r10 = android.text.style.ClickableSpan.class;
        r8 = r0.getSpans(r8, r9, r10);	 Catch:{ Exception -> 0x01d8 }
        r8 = (android.text.style.CharacterStyle[]) r8;	 Catch:{ Exception -> 0x01d8 }
    L_0x014b:
        if (r8 == 0) goto L_0x0177;
    L_0x014d:
        r9 = r8.length;	 Catch:{ Exception -> 0x01d8 }
        if (r9 == 0) goto L_0x0177;
    L_0x0150:
        r8 = r8[r2];	 Catch:{ Exception -> 0x01d8 }
        r9 = r13.pressedLink;	 Catch:{ Exception -> 0x01d8 }
        if (r8 == r9) goto L_0x0157;
    L_0x0156:
        goto L_0x0177;
    L_0x0157:
        r8 = r13.obtainNewUrlPath(r2);	 Catch:{ Exception -> 0x01d8 }
        r9 = r7.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r10 = r7.textYOffset;	 Catch:{ Exception -> 0x01d8 }
        r11 = r4.textYOffset;	 Catch:{ Exception -> 0x01d8 }
        r10 = r10 - r11;
        r8.setCurrentLayout(r9, r2, r10);	 Catch:{ Exception -> 0x01d8 }
        r9 = r7.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r10 = r3[r1];	 Catch:{ Exception -> 0x01d8 }
        r9.getSelectionPath(r2, r10, r8);	 Catch:{ Exception -> 0x01d8 }
        r8 = r3[r1];	 Catch:{ Exception -> 0x01d8 }
        r7 = r7.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7 - r1;
        if (r8 >= r7) goto L_0x0174;
    L_0x0173:
        goto L_0x0177;
    L_0x0174:
        r14 = r14 + 1;
        goto L_0x011c;
    L_0x0177:
        r14 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r4 = r4.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        if (r14 > r4) goto L_0x01dc;
    L_0x017d:
        r5 = r5 - r1;
        r14 = 0;
    L_0x017f:
        if (r5 < 0) goto L_0x01dc;
    L_0x0181:
        r4 = r13.currentMessageObject;	 Catch:{ Exception -> 0x01d8 }
        r4 = r4.textLayoutBlocks;	 Catch:{ Exception -> 0x01d8 }
        r4 = r4.get(r5);	 Catch:{ Exception -> 0x01d8 }
        r4 = (org.telegram.messenger.MessageObject.TextLayoutBlock) r4;	 Catch:{ Exception -> 0x01d8 }
        if (r6 == 0) goto L_0x019c;
    L_0x018d:
        r7 = r4.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7 - r1;
        r8 = r4.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        r8 = r8 - r1;
        r9 = org.telegram.ui.Components.URLSpanMono.class;
        r7 = r0.getSpans(r7, r8, r9);	 Catch:{ Exception -> 0x01d8 }
        r7 = (android.text.style.CharacterStyle[]) r7;	 Catch:{ Exception -> 0x01d8 }
        goto L_0x01aa;
    L_0x019c:
        r7 = r4.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        r7 = r7 - r1;
        r8 = r4.charactersEnd;	 Catch:{ Exception -> 0x01d8 }
        r8 = r8 - r1;
        r9 = android.text.style.ClickableSpan.class;
        r7 = r0.getSpans(r7, r8, r9);	 Catch:{ Exception -> 0x01d8 }
        r7 = (android.text.style.CharacterStyle[]) r7;	 Catch:{ Exception -> 0x01d8 }
    L_0x01aa:
        if (r7 == 0) goto L_0x01dc;
    L_0x01ac:
        r8 = r7.length;	 Catch:{ Exception -> 0x01d8 }
        if (r8 == 0) goto L_0x01dc;
    L_0x01af:
        r7 = r7[r2];	 Catch:{ Exception -> 0x01d8 }
        r8 = r13.pressedLink;	 Catch:{ Exception -> 0x01d8 }
        if (r7 == r8) goto L_0x01b6;
    L_0x01b5:
        goto L_0x01dc;
    L_0x01b6:
        r7 = r13.obtainNewUrlPath(r2);	 Catch:{ Exception -> 0x01d8 }
        r8 = r4.height;	 Catch:{ Exception -> 0x01d8 }
        r14 = r14 - r8;
        r8 = r4.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r9 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r10 = (float) r14;	 Catch:{ Exception -> 0x01d8 }
        r7.setCurrentLayout(r8, r9, r10);	 Catch:{ Exception -> 0x01d8 }
        r8 = r4.textLayout;	 Catch:{ Exception -> 0x01d8 }
        r9 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r10 = r3[r1];	 Catch:{ Exception -> 0x01d8 }
        r8.getSelectionPath(r9, r10, r7);	 Catch:{ Exception -> 0x01d8 }
        r7 = r3[r2];	 Catch:{ Exception -> 0x01d8 }
        r4 = r4.charactersOffset;	 Catch:{ Exception -> 0x01d8 }
        if (r7 <= r4) goto L_0x01d5;
    L_0x01d4:
        goto L_0x01dc;
    L_0x01d5:
        r5 = r5 + -1;
        goto L_0x017f;
    L_0x01d8:
        r14 = move-exception;
        org.telegram.messenger.FileLog.e(r14);	 Catch:{ Exception -> 0x01f1 }
    L_0x01dc:
        r13.invalidate();	 Catch:{ Exception -> 0x01f1 }
        return r1;
    L_0x01e0:
        r14 = r3[r2];	 Catch:{ Exception -> 0x01f1 }
        r0 = r13.pressedLink;	 Catch:{ Exception -> 0x01f1 }
        if (r14 != r0) goto L_0x01f9;
    L_0x01e6:
        r14 = r13.delegate;	 Catch:{ Exception -> 0x01f1 }
        r0 = r13.pressedLink;	 Catch:{ Exception -> 0x01f1 }
        r14.didPressUrl(r13, r0, r2);	 Catch:{ Exception -> 0x01f1 }
        r13.resetPressedLink(r1);	 Catch:{ Exception -> 0x01f1 }
        return r1;
    L_0x01f1:
        r14 = move-exception;
        org.telegram.messenger.FileLog.e(r14);
        goto L_0x01f9;
    L_0x01f6:
        r13.resetPressedLink(r1);
    L_0x01f9:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkTextBlockMotionEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x009c A:{Catch:{ Exception -> 0x00dc }} */
    private boolean checkCaptionMotionEvent(android.view.MotionEvent r8) {
        /*
        r7 = this;
        r0 = r7.currentCaption;
        r0 = r0 instanceof android.text.Spannable;
        r1 = 0;
        if (r0 == 0) goto L_0x00f3;
    L_0x0007:
        r0 = r7.captionLayout;
        if (r0 != 0) goto L_0x000d;
    L_0x000b:
        goto L_0x00f3;
    L_0x000d:
        r0 = r8.getAction();
        r2 = 1;
        if (r0 == 0) goto L_0x0022;
    L_0x0014:
        r0 = r7.linkPreviewPressed;
        if (r0 != 0) goto L_0x001c;
    L_0x0018:
        r0 = r7.pressedLink;
        if (r0 == 0) goto L_0x00f3;
    L_0x001c:
        r0 = r8.getAction();
        if (r0 != r2) goto L_0x00f3;
    L_0x0022:
        r0 = r8.getX();
        r0 = (int) r0;
        r3 = r8.getY();
        r3 = (int) r3;
        r4 = r7.captionX;
        r5 = 3;
        if (r0 < r4) goto L_0x00f0;
    L_0x0031:
        r6 = r7.captionWidth;
        r4 = r4 + r6;
        if (r0 > r4) goto L_0x00f0;
    L_0x0036:
        r4 = r7.captionY;
        if (r3 < r4) goto L_0x00f0;
    L_0x003a:
        r6 = r7.captionHeight;
        r4 = r4 + r6;
        if (r3 > r4) goto L_0x00f0;
    L_0x003f:
        r8 = r8.getAction();
        if (r8 != 0) goto L_0x00e1;
    L_0x0045:
        r8 = r7.captionX;	 Catch:{ Exception -> 0x00dc }
        r0 = r0 - r8;
        r8 = r7.captionY;	 Catch:{ Exception -> 0x00dc }
        r3 = r3 - r8;
        r8 = r7.captionLayout;	 Catch:{ Exception -> 0x00dc }
        r8 = r8.getLineForVertical(r3);	 Catch:{ Exception -> 0x00dc }
        r3 = r7.captionLayout;	 Catch:{ Exception -> 0x00dc }
        r0 = (float) r0;	 Catch:{ Exception -> 0x00dc }
        r3 = r3.getOffsetForHorizontal(r8, r0);	 Catch:{ Exception -> 0x00dc }
        r4 = r7.captionLayout;	 Catch:{ Exception -> 0x00dc }
        r4 = r4.getLineLeft(r8);	 Catch:{ Exception -> 0x00dc }
        r6 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r6 > 0) goto L_0x00f3;
    L_0x0062:
        r6 = r7.captionLayout;	 Catch:{ Exception -> 0x00dc }
        r8 = r6.getLineWidth(r8);	 Catch:{ Exception -> 0x00dc }
        r4 = r4 + r8;
        r8 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r8 < 0) goto L_0x00f3;
    L_0x006d:
        r8 = r7.currentCaption;	 Catch:{ Exception -> 0x00dc }
        r8 = (android.text.Spannable) r8;	 Catch:{ Exception -> 0x00dc }
        r0 = android.text.style.ClickableSpan.class;
        r0 = r8.getSpans(r3, r3, r0);	 Catch:{ Exception -> 0x00dc }
        r0 = (android.text.style.CharacterStyle[]) r0;	 Catch:{ Exception -> 0x00dc }
        if (r0 == 0) goto L_0x007e;
    L_0x007b:
        r4 = r0.length;	 Catch:{ Exception -> 0x00dc }
        if (r4 != 0) goto L_0x0086;
    L_0x007e:
        r0 = org.telegram.ui.Components.URLSpanMono.class;
        r0 = r8.getSpans(r3, r3, r0);	 Catch:{ Exception -> 0x00dc }
        r0 = (android.text.style.CharacterStyle[]) r0;	 Catch:{ Exception -> 0x00dc }
    L_0x0086:
        r3 = r0.length;	 Catch:{ Exception -> 0x00dc }
        if (r3 == 0) goto L_0x0099;
    L_0x0089:
        r3 = r0.length;	 Catch:{ Exception -> 0x00dc }
        if (r3 == 0) goto L_0x0097;
    L_0x008c:
        r3 = r0[r1];	 Catch:{ Exception -> 0x00dc }
        r3 = r3 instanceof org.telegram.ui.Components.URLSpanBotCommand;	 Catch:{ Exception -> 0x00dc }
        if (r3 == 0) goto L_0x0097;
    L_0x0092:
        r3 = org.telegram.ui.Components.URLSpanBotCommand.enabled;	 Catch:{ Exception -> 0x00dc }
        if (r3 != 0) goto L_0x0097;
    L_0x0096:
        goto L_0x0099;
    L_0x0097:
        r3 = 0;
        goto L_0x009a;
    L_0x0099:
        r3 = 1;
    L_0x009a:
        if (r3 != 0) goto L_0x00f3;
    L_0x009c:
        r0 = r0[r1];	 Catch:{ Exception -> 0x00dc }
        r7.pressedLink = r0;	 Catch:{ Exception -> 0x00dc }
        r7.pressedLinkType = r5;	 Catch:{ Exception -> 0x00dc }
        r7.resetUrlPaths(r1);	 Catch:{ Exception -> 0x00dc }
        r0 = r7.obtainNewUrlPath(r1);	 Catch:{ Exception -> 0x00c1 }
        r3 = r7.pressedLink;	 Catch:{ Exception -> 0x00c1 }
        r8 = r7.getRealSpanStartAndEnd(r8, r3);	 Catch:{ Exception -> 0x00c1 }
        r3 = r7.captionLayout;	 Catch:{ Exception -> 0x00c1 }
        r4 = r8[r1];	 Catch:{ Exception -> 0x00c1 }
        r5 = 0;
        r0.setCurrentLayout(r3, r4, r5);	 Catch:{ Exception -> 0x00c1 }
        r3 = r7.captionLayout;	 Catch:{ Exception -> 0x00c1 }
        r4 = r8[r1];	 Catch:{ Exception -> 0x00c1 }
        r8 = r8[r2];	 Catch:{ Exception -> 0x00c1 }
        r3.getSelectionPath(r4, r8, r0);	 Catch:{ Exception -> 0x00c1 }
        goto L_0x00c5;
    L_0x00c1:
        r8 = move-exception;
        org.telegram.messenger.FileLog.e(r8);	 Catch:{ Exception -> 0x00dc }
    L_0x00c5:
        r8 = r7.currentMessagesGroup;	 Catch:{ Exception -> 0x00dc }
        if (r8 == 0) goto L_0x00d8;
    L_0x00c9:
        r8 = r7.getParent();	 Catch:{ Exception -> 0x00dc }
        if (r8 == 0) goto L_0x00d8;
    L_0x00cf:
        r8 = r7.getParent();	 Catch:{ Exception -> 0x00dc }
        r8 = (android.view.ViewGroup) r8;	 Catch:{ Exception -> 0x00dc }
        r8.invalidate();	 Catch:{ Exception -> 0x00dc }
    L_0x00d8:
        r7.invalidate();	 Catch:{ Exception -> 0x00dc }
        return r2;
    L_0x00dc:
        r8 = move-exception;
        org.telegram.messenger.FileLog.e(r8);
        goto L_0x00f3;
    L_0x00e1:
        r8 = r7.pressedLinkType;
        if (r8 != r5) goto L_0x00f3;
    L_0x00e5:
        r8 = r7.delegate;
        r0 = r7.pressedLink;
        r8.didPressUrl(r7, r0, r1);
        r7.resetPressedLink(r5);
        return r2;
    L_0x00f0:
        r7.resetPressedLink(r5);
    L_0x00f3:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkCaptionMotionEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00bb A:{Catch:{ Exception -> 0x00ec }} */
    private boolean checkGameMotionEvent(android.view.MotionEvent r8) {
        /*
        r7 = this;
        r0 = r7.hasGamePreview;
        r1 = 0;
        if (r0 != 0) goto L_0x0006;
    L_0x0005:
        return r1;
    L_0x0006:
        r0 = r8.getX();
        r0 = (int) r0;
        r2 = r8.getY();
        r2 = (int) r2;
        r3 = r8.getAction();
        r4 = 2;
        r5 = 1;
        if (r3 != 0) goto L_0x00f2;
    L_0x0018:
        r8 = r7.drawPhotoImage;
        if (r8 == 0) goto L_0x004c;
    L_0x001c:
        r8 = r7.drawImageButton;
        if (r8 == 0) goto L_0x004c;
    L_0x0020:
        r8 = r7.buttonState;
        r3 = -1;
        if (r8 == r3) goto L_0x004c;
    L_0x0025:
        r8 = r7.buttonX;
        if (r0 < r8) goto L_0x004c;
    L_0x0029:
        r3 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r8 = r8 + r6;
        if (r0 > r8) goto L_0x004c;
    L_0x0032:
        r8 = r7.buttonY;
        if (r2 < r8) goto L_0x004c;
    L_0x0036:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r8 = r8 + r3;
        if (r2 > r8) goto L_0x004c;
    L_0x003d:
        r8 = r7.radialProgress;
        r8 = r8.getIcon();
        r3 = 4;
        if (r8 == r3) goto L_0x004c;
    L_0x0046:
        r7.buttonPressed = r5;
        r7.invalidate();
        return r5;
    L_0x004c:
        r8 = r7.drawPhotoImage;
        if (r8 == 0) goto L_0x005d;
    L_0x0050:
        r8 = r7.photoImage;
        r3 = (float) r0;
        r6 = (float) r2;
        r8 = r8.isInsideImage(r3, r6);
        if (r8 == 0) goto L_0x005d;
    L_0x005a:
        r7.gamePreviewPressed = r5;
        return r5;
    L_0x005d:
        r8 = r7.descriptionLayout;
        if (r8 == 0) goto L_0x0170;
    L_0x0061:
        r8 = r7.descriptionY;
        if (r2 < r8) goto L_0x0170;
    L_0x0065:
        r8 = r7.unmovedTextX;	 Catch:{ Exception -> 0x00ec }
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x00ec }
        r8 = r8 + r3;
        r3 = r7.descriptionX;	 Catch:{ Exception -> 0x00ec }
        r8 = r8 + r3;
        r0 = r0 - r8;
        r8 = r7.descriptionY;	 Catch:{ Exception -> 0x00ec }
        r2 = r2 - r8;
        r8 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00ec }
        r8 = r8.getLineForVertical(r2);	 Catch:{ Exception -> 0x00ec }
        r2 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00ec }
        r0 = (float) r0;	 Catch:{ Exception -> 0x00ec }
        r2 = r2.getOffsetForHorizontal(r8, r0);	 Catch:{ Exception -> 0x00ec }
        r3 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00ec }
        r3 = r3.getLineLeft(r8);	 Catch:{ Exception -> 0x00ec }
        r6 = (r3 > r0 ? 1 : (r3 == r0 ? 0 : -1));
        if (r6 > 0) goto L_0x0170;
    L_0x008c:
        r6 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00ec }
        r8 = r6.getLineWidth(r8);	 Catch:{ Exception -> 0x00ec }
        r3 = r3 + r8;
        r8 = (r3 > r0 ? 1 : (r3 == r0 ? 0 : -1));
        if (r8 < 0) goto L_0x0170;
    L_0x0097:
        r8 = r7.currentMessageObject;	 Catch:{ Exception -> 0x00ec }
        r8 = r8.linkDescription;	 Catch:{ Exception -> 0x00ec }
        r8 = (android.text.Spannable) r8;	 Catch:{ Exception -> 0x00ec }
        r0 = android.text.style.ClickableSpan.class;
        r0 = r8.getSpans(r2, r2, r0);	 Catch:{ Exception -> 0x00ec }
        r0 = (android.text.style.ClickableSpan[]) r0;	 Catch:{ Exception -> 0x00ec }
        r2 = r0.length;	 Catch:{ Exception -> 0x00ec }
        if (r2 == 0) goto L_0x00b8;
    L_0x00a8:
        r2 = r0.length;	 Catch:{ Exception -> 0x00ec }
        if (r2 == 0) goto L_0x00b6;
    L_0x00ab:
        r2 = r0[r1];	 Catch:{ Exception -> 0x00ec }
        r2 = r2 instanceof org.telegram.ui.Components.URLSpanBotCommand;	 Catch:{ Exception -> 0x00ec }
        if (r2 == 0) goto L_0x00b6;
    L_0x00b1:
        r2 = org.telegram.ui.Components.URLSpanBotCommand.enabled;	 Catch:{ Exception -> 0x00ec }
        if (r2 != 0) goto L_0x00b6;
    L_0x00b5:
        goto L_0x00b8;
    L_0x00b6:
        r2 = 0;
        goto L_0x00b9;
    L_0x00b8:
        r2 = 1;
    L_0x00b9:
        if (r2 != 0) goto L_0x0170;
    L_0x00bb:
        r0 = r0[r1];	 Catch:{ Exception -> 0x00ec }
        r7.pressedLink = r0;	 Catch:{ Exception -> 0x00ec }
        r0 = -10;
        r7.linkBlockNum = r0;	 Catch:{ Exception -> 0x00ec }
        r7.pressedLinkType = r4;	 Catch:{ Exception -> 0x00ec }
        r7.resetUrlPaths(r1);	 Catch:{ Exception -> 0x00ec }
        r0 = r7.obtainNewUrlPath(r1);	 Catch:{ Exception -> 0x00e4 }
        r2 = r7.pressedLink;	 Catch:{ Exception -> 0x00e4 }
        r8 = r7.getRealSpanStartAndEnd(r8, r2);	 Catch:{ Exception -> 0x00e4 }
        r2 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00e4 }
        r3 = r8[r1];	 Catch:{ Exception -> 0x00e4 }
        r4 = 0;
        r0.setCurrentLayout(r2, r3, r4);	 Catch:{ Exception -> 0x00e4 }
        r2 = r7.descriptionLayout;	 Catch:{ Exception -> 0x00e4 }
        r3 = r8[r1];	 Catch:{ Exception -> 0x00e4 }
        r8 = r8[r5];	 Catch:{ Exception -> 0x00e4 }
        r2.getSelectionPath(r3, r8, r0);	 Catch:{ Exception -> 0x00e4 }
        goto L_0x00e8;
    L_0x00e4:
        r8 = move-exception;
        org.telegram.messenger.FileLog.e(r8);	 Catch:{ Exception -> 0x00ec }
    L_0x00e8:
        r7.invalidate();	 Catch:{ Exception -> 0x00ec }
        return r5;
    L_0x00ec:
        r8 = move-exception;
        org.telegram.messenger.FileLog.e(r8);
        goto L_0x0170;
    L_0x00f2:
        r8 = r8.getAction();
        if (r8 != r5) goto L_0x0170;
    L_0x00f8:
        r8 = r7.pressedLinkType;
        if (r8 == r4) goto L_0x0109;
    L_0x00fc:
        r8 = r7.gamePreviewPressed;
        if (r8 != 0) goto L_0x0109;
    L_0x0100:
        r8 = r7.buttonPressed;
        if (r8 == 0) goto L_0x0105;
    L_0x0104:
        goto L_0x0109;
    L_0x0105:
        r7.resetPressedLink(r4);
        goto L_0x0170;
    L_0x0109:
        r8 = r7.buttonPressed;
        if (r8 == 0) goto L_0x0119;
    L_0x010d:
        r7.buttonPressed = r1;
        r7.playSoundEffect(r1);
        r7.didPressButton(r5, r1);
        r7.invalidate();
        goto L_0x0170;
    L_0x0119:
        r8 = r7.pressedLink;
        if (r8 == 0) goto L_0x013e;
    L_0x011d:
        r0 = r8 instanceof android.text.style.URLSpan;
        if (r0 == 0) goto L_0x0131;
    L_0x0121:
        r8 = r7.getContext();
        r0 = r7.pressedLink;
        r0 = (android.text.style.URLSpan) r0;
        r0 = r0.getURL();
        org.telegram.messenger.browser.Browser.openUrl(r8, r0);
        goto L_0x013a;
    L_0x0131:
        r0 = r8 instanceof android.text.style.ClickableSpan;
        if (r0 == 0) goto L_0x013a;
    L_0x0135:
        r8 = (android.text.style.ClickableSpan) r8;
        r8.onClick(r7);
    L_0x013a:
        r7.resetPressedLink(r4);
        goto L_0x0170;
    L_0x013e:
        r7.gamePreviewPressed = r1;
        r8 = 0;
    L_0x0141:
        r0 = r7.botButtons;
        r0 = r0.size();
        if (r8 >= r0) goto L_0x016c;
    L_0x0149:
        r0 = r7.botButtons;
        r0 = r0.get(r8);
        r0 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r0;
        r2 = r0.button;
        r2 = r2 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
        if (r2 == 0) goto L_0x0169;
    L_0x0159:
        r7.playSoundEffect(r1);
        r8 = r7.delegate;
        r0 = r0.button;
        r8.didPressBotButton(r7, r0);
        r7.invalidate();
        goto L_0x016c;
    L_0x0169:
        r8 = r8 + 1;
        goto L_0x0141;
    L_0x016c:
        r7.resetPressedLink(r4);
        return r5;
    L_0x0170:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkGameMotionEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b5 A:{Catch:{ Exception -> 0x00e6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0117  */
    /* JADX WARNING: Missing block: B:101:0x0185, code skipped:
            if (r1.radialProgress.getIcon() != 4) goto L_0x0187;
     */
    private boolean checkLinkPreviewMotionEvent(android.view.MotionEvent r17) {
        /*
        r16 = this;
        r1 = r16;
        r0 = r1.currentMessageObject;
        r0 = r0.type;
        r2 = 0;
        if (r0 != 0) goto L_0x0371;
    L_0x0009:
        r0 = r1.hasLinkPreview;
        if (r0 != 0) goto L_0x000f;
    L_0x000d:
        goto L_0x0371;
    L_0x000f:
        r0 = r17.getX();
        r3 = (int) r0;
        r0 = r17.getY();
        r4 = (int) r0;
        r0 = r1.unmovedTextX;
        if (r3 < r0) goto L_0x0371;
    L_0x001d:
        r5 = r1.backgroundWidth;
        r0 = r0 + r5;
        if (r3 > r0) goto L_0x0371;
    L_0x0022:
        r0 = r1.textY;
        r5 = r1.currentMessageObject;
        r5 = r5.textHeight;
        r6 = r0 + r5;
        if (r4 < r6) goto L_0x0371;
    L_0x002c:
        r0 = r0 + r5;
        r5 = r1.linkPreviewHeight;
        r0 = r0 + r5;
        r5 = r1.drawInstantView;
        if (r5 == 0) goto L_0x0037;
    L_0x0034:
        r5 = 46;
        goto L_0x0038;
    L_0x0037:
        r5 = 0;
    L_0x0038:
        r5 = r5 + 8;
        r5 = (float) r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        if (r4 > r0) goto L_0x0371;
    L_0x0042:
        r0 = r17.getAction();
        r5 = 21;
        r6 = -1;
        r7 = 2;
        r8 = 1;
        if (r0 != 0) goto L_0x01f5;
    L_0x004d:
        r0 = r1.descriptionLayout;
        if (r0 == 0) goto L_0x00ea;
    L_0x0051:
        r0 = r1.descriptionY;
        if (r4 < r0) goto L_0x00ea;
    L_0x0055:
        r0 = r1.unmovedTextX;	 Catch:{ Exception -> 0x00e6 }
        r9 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);	 Catch:{ Exception -> 0x00e6 }
        r0 = r0 + r9;
        r9 = r1.descriptionX;	 Catch:{ Exception -> 0x00e6 }
        r0 = r0 + r9;
        r0 = r3 - r0;
        r9 = r1.descriptionY;	 Catch:{ Exception -> 0x00e6 }
        r9 = r4 - r9;
        r10 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00e6 }
        r10 = r10.getHeight();	 Catch:{ Exception -> 0x00e6 }
        if (r9 > r10) goto L_0x00ea;
    L_0x006f:
        r10 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00e6 }
        r9 = r10.getLineForVertical(r9);	 Catch:{ Exception -> 0x00e6 }
        r10 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00e6 }
        r0 = (float) r0;	 Catch:{ Exception -> 0x00e6 }
        r10 = r10.getOffsetForHorizontal(r9, r0);	 Catch:{ Exception -> 0x00e6 }
        r11 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00e6 }
        r11 = r11.getLineLeft(r9);	 Catch:{ Exception -> 0x00e6 }
        r12 = (r11 > r0 ? 1 : (r11 == r0 ? 0 : -1));
        if (r12 > 0) goto L_0x00ea;
    L_0x0086:
        r12 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00e6 }
        r9 = r12.getLineWidth(r9);	 Catch:{ Exception -> 0x00e6 }
        r11 = r11 + r9;
        r0 = (r11 > r0 ? 1 : (r11 == r0 ? 0 : -1));
        if (r0 < 0) goto L_0x00ea;
    L_0x0091:
        r0 = r1.currentMessageObject;	 Catch:{ Exception -> 0x00e6 }
        r0 = r0.linkDescription;	 Catch:{ Exception -> 0x00e6 }
        r0 = (android.text.Spannable) r0;	 Catch:{ Exception -> 0x00e6 }
        r9 = android.text.style.ClickableSpan.class;
        r9 = r0.getSpans(r10, r10, r9);	 Catch:{ Exception -> 0x00e6 }
        r9 = (android.text.style.ClickableSpan[]) r9;	 Catch:{ Exception -> 0x00e6 }
        r10 = r9.length;	 Catch:{ Exception -> 0x00e6 }
        if (r10 == 0) goto L_0x00b2;
    L_0x00a2:
        r10 = r9.length;	 Catch:{ Exception -> 0x00e6 }
        if (r10 == 0) goto L_0x00b0;
    L_0x00a5:
        r10 = r9[r2];	 Catch:{ Exception -> 0x00e6 }
        r10 = r10 instanceof org.telegram.ui.Components.URLSpanBotCommand;	 Catch:{ Exception -> 0x00e6 }
        if (r10 == 0) goto L_0x00b0;
    L_0x00ab:
        r10 = org.telegram.ui.Components.URLSpanBotCommand.enabled;	 Catch:{ Exception -> 0x00e6 }
        if (r10 != 0) goto L_0x00b0;
    L_0x00af:
        goto L_0x00b2;
    L_0x00b0:
        r10 = 0;
        goto L_0x00b3;
    L_0x00b2:
        r10 = 1;
    L_0x00b3:
        if (r10 != 0) goto L_0x00ea;
    L_0x00b5:
        r9 = r9[r2];	 Catch:{ Exception -> 0x00e6 }
        r1.pressedLink = r9;	 Catch:{ Exception -> 0x00e6 }
        r9 = -10;
        r1.linkBlockNum = r9;	 Catch:{ Exception -> 0x00e6 }
        r1.pressedLinkType = r7;	 Catch:{ Exception -> 0x00e6 }
        r1.resetUrlPaths(r2);	 Catch:{ Exception -> 0x00e6 }
        r9 = r1.obtainNewUrlPath(r2);	 Catch:{ Exception -> 0x00de }
        r10 = r1.pressedLink;	 Catch:{ Exception -> 0x00de }
        r0 = r1.getRealSpanStartAndEnd(r0, r10);	 Catch:{ Exception -> 0x00de }
        r10 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00de }
        r11 = r0[r2];	 Catch:{ Exception -> 0x00de }
        r12 = 0;
        r9.setCurrentLayout(r10, r11, r12);	 Catch:{ Exception -> 0x00de }
        r10 = r1.descriptionLayout;	 Catch:{ Exception -> 0x00de }
        r11 = r0[r2];	 Catch:{ Exception -> 0x00de }
        r0 = r0[r8];	 Catch:{ Exception -> 0x00de }
        r10.getSelectionPath(r11, r0, r9);	 Catch:{ Exception -> 0x00de }
        goto L_0x00e2;
    L_0x00de:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);	 Catch:{ Exception -> 0x00e6 }
    L_0x00e2:
        r16.invalidate();	 Catch:{ Exception -> 0x00e6 }
        return r8;
    L_0x00e6:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x00ea:
        r0 = r1.pressedLink;
        if (r0 != 0) goto L_0x0371;
    L_0x00ee:
        r0 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r10 = r1.miniButtonState;
        if (r10 < 0) goto L_0x0114;
    L_0x00f8:
        r10 = NUM; // 0x41d80000 float:27.0 double:5.457818764E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r11 = r1.buttonX;
        r12 = r11 + r10;
        if (r3 < r12) goto L_0x0114;
    L_0x0104:
        r11 = r11 + r10;
        r11 = r11 + r9;
        if (r3 > r11) goto L_0x0114;
    L_0x0108:
        r11 = r1.buttonY;
        r12 = r11 + r10;
        if (r4 < r12) goto L_0x0114;
    L_0x010e:
        r11 = r11 + r10;
        r11 = r11 + r9;
        if (r4 > r11) goto L_0x0114;
    L_0x0112:
        r9 = 1;
        goto L_0x0115;
    L_0x0114:
        r9 = 0;
    L_0x0115:
        if (r9 == 0) goto L_0x011d;
    L_0x0117:
        r1.miniButtonPressed = r8;
        r16.invalidate();
        return r8;
    L_0x011d:
        r9 = r1.drawVideoImageButton;
        if (r9 == 0) goto L_0x014e;
    L_0x0121:
        r9 = r1.buttonState;
        if (r9 == r6) goto L_0x014e;
    L_0x0125:
        r9 = r1.videoButtonX;
        if (r3 < r9) goto L_0x014e;
    L_0x0129:
        r10 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 + r10;
        r10 = r1.infoWidth;
        r11 = r1.docTitleWidth;
        r10 = java.lang.Math.max(r10, r11);
        r9 = r9 + r10;
        if (r3 > r9) goto L_0x014e;
    L_0x013b:
        r9 = r1.videoButtonY;
        if (r4 < r9) goto L_0x014e;
    L_0x013f:
        r10 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 + r10;
        if (r4 > r9) goto L_0x014e;
    L_0x0148:
        r1.videoButtonPressed = r8;
        r16.invalidate();
        return r8;
    L_0x014e:
        r9 = r1.drawPhotoImage;
        if (r9 == 0) goto L_0x018d;
    L_0x0152:
        r9 = r1.drawImageButton;
        if (r9 == 0) goto L_0x018d;
    L_0x0156:
        r9 = r1.buttonState;
        if (r9 == r6) goto L_0x018d;
    L_0x015a:
        r9 = r1.checkOnlyButtonPressed;
        if (r9 != 0) goto L_0x0168;
    L_0x015e:
        r9 = r1.photoImage;
        r10 = (float) r3;
        r11 = (float) r4;
        r9 = r9.isInsideImage(r10, r11);
        if (r9 != 0) goto L_0x0187;
    L_0x0168:
        r9 = r1.buttonX;
        if (r3 < r9) goto L_0x018d;
    L_0x016c:
        r10 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r9 = r9 + r10;
        if (r3 > r9) goto L_0x018d;
    L_0x0173:
        r9 = r1.buttonY;
        if (r4 < r9) goto L_0x018d;
    L_0x0177:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r9 = r9 + r0;
        if (r4 > r9) goto L_0x018d;
    L_0x017e:
        r0 = r1.radialProgress;
        r0 = r0.getIcon();
        r9 = 4;
        if (r0 == r9) goto L_0x018d;
    L_0x0187:
        r1.buttonPressed = r8;
        r16.invalidate();
        return r8;
    L_0x018d:
        r0 = r1.drawInstantView;
        if (r0 == 0) goto L_0x01b9;
    L_0x0191:
        r1.instantPressed = r8;
        r0 = android.os.Build.VERSION.SDK_INT;
        if (r0 < r5) goto L_0x01b5;
    L_0x0197:
        r0 = r1.selectorDrawable;
        if (r0 == 0) goto L_0x01b5;
    L_0x019b:
        r0 = r0.getBounds();
        r0 = r0.contains(r3, r4);
        if (r0 == 0) goto L_0x01b5;
    L_0x01a5:
        r0 = r1.selectorDrawable;
        r2 = r1.pressedState;
        r0.setState(r2);
        r0 = r1.selectorDrawable;
        r2 = (float) r3;
        r3 = (float) r4;
        r0.setHotspot(r2, r3);
        r1.instantButtonPressed = r8;
    L_0x01b5:
        r16.invalidate();
        return r8;
    L_0x01b9:
        r0 = r1.documentAttachType;
        if (r0 == r8) goto L_0x0371;
    L_0x01bd:
        r0 = r1.drawPhotoImage;
        if (r0 == 0) goto L_0x0371;
    L_0x01c1:
        r0 = r1.photoImage;
        r3 = (float) r3;
        r4 = (float) r4;
        r0 = r0.isInsideImage(r3, r4);
        if (r0 == 0) goto L_0x0371;
    L_0x01cb:
        r1.linkPreviewPressed = r8;
        r0 = r1.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r3 = r1.documentAttachType;
        if (r3 != r7) goto L_0x01f4;
    L_0x01d9:
        r3 = r1.buttonState;
        if (r3 != r6) goto L_0x01f4;
    L_0x01dd:
        r3 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r3 == 0) goto L_0x01f4;
    L_0x01e1:
        r3 = r1.photoImage;
        r3 = r3.getAnimation();
        if (r3 == 0) goto L_0x01f1;
    L_0x01e9:
        r0 = r0.embed_url;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 != 0) goto L_0x01f4;
    L_0x01f1:
        r1.linkPreviewPressed = r2;
        return r2;
    L_0x01f4:
        return r8;
    L_0x01f5:
        r0 = r17.getAction();
        if (r0 != r8) goto L_0x035a;
    L_0x01fb:
        r0 = r1.instantPressed;
        if (r0 == 0) goto L_0x0221;
    L_0x01ff:
        r0 = r1.delegate;
        if (r0 == 0) goto L_0x0208;
    L_0x0203:
        r3 = r1.drawInstantViewType;
        r0.didPressInstantButton(r1, r3);
    L_0x0208:
        r1.playSoundEffect(r2);
        r0 = android.os.Build.VERSION.SDK_INT;
        if (r0 < r5) goto L_0x0218;
    L_0x020f:
        r0 = r1.selectorDrawable;
        if (r0 == 0) goto L_0x0218;
    L_0x0213:
        r3 = android.util.StateSet.NOTHING;
        r0.setState(r3);
    L_0x0218:
        r1.instantButtonPressed = r2;
        r1.instantPressed = r2;
        r16.invalidate();
        goto L_0x0371;
    L_0x0221:
        r0 = r1.pressedLinkType;
        if (r0 == r7) goto L_0x023b;
    L_0x0225:
        r0 = r1.buttonPressed;
        if (r0 != 0) goto L_0x023b;
    L_0x0229:
        r0 = r1.miniButtonPressed;
        if (r0 != 0) goto L_0x023b;
    L_0x022d:
        r0 = r1.videoButtonPressed;
        if (r0 != 0) goto L_0x023b;
    L_0x0231:
        r0 = r1.linkPreviewPressed;
        if (r0 == 0) goto L_0x0236;
    L_0x0235:
        goto L_0x023b;
    L_0x0236:
        r1.resetPressedLink(r7);
        goto L_0x0371;
    L_0x023b:
        r0 = r1.videoButtonPressed;
        if (r0 != r8) goto L_0x024c;
    L_0x023f:
        r1.videoButtonPressed = r2;
        r1.playSoundEffect(r2);
        r1.didPressButton(r8, r8);
        r16.invalidate();
        goto L_0x0371;
    L_0x024c:
        r0 = r1.buttonPressed;
        if (r0 == 0) goto L_0x0265;
    L_0x0250:
        r1.buttonPressed = r2;
        r1.playSoundEffect(r2);
        r0 = r1.drawVideoImageButton;
        if (r0 == 0) goto L_0x025d;
    L_0x0259:
        r16.didClickedImage();
        goto L_0x0260;
    L_0x025d:
        r1.didPressButton(r8, r2);
    L_0x0260:
        r16.invalidate();
        goto L_0x0371;
    L_0x0265:
        r0 = r1.miniButtonPressed;
        if (r0 == 0) goto L_0x0276;
    L_0x0269:
        r1.miniButtonPressed = r2;
        r1.playSoundEffect(r2);
        r1.didPressMiniButton(r8);
        r16.invalidate();
        goto L_0x0371;
    L_0x0276:
        r0 = r1.pressedLink;
        if (r0 == 0) goto L_0x029c;
    L_0x027a:
        r3 = r0 instanceof android.text.style.URLSpan;
        if (r3 == 0) goto L_0x028e;
    L_0x027e:
        r0 = r16.getContext();
        r3 = r1.pressedLink;
        r3 = (android.text.style.URLSpan) r3;
        r3 = r3.getURL();
        org.telegram.messenger.browser.Browser.openUrl(r0, r3);
        goto L_0x0297;
    L_0x028e:
        r3 = r0 instanceof android.text.style.ClickableSpan;
        if (r3 == 0) goto L_0x0297;
    L_0x0292:
        r0 = (android.text.style.ClickableSpan) r0;
        r0.onClick(r1);
    L_0x0297:
        r1.resetPressedLink(r7);
        goto L_0x0371;
    L_0x029c:
        r0 = r1.documentAttachType;
        r3 = 7;
        if (r0 != r3) goto L_0x02cc;
    L_0x02a1:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r2 = r1.currentMessageObject;
        r0 = r0.isPlayingMessage(r2);
        if (r0 == 0) goto L_0x02c3;
    L_0x02ad:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r0 = r0.isMessagePaused();
        if (r0 == 0) goto L_0x02b8;
    L_0x02b7:
        goto L_0x02c3;
    L_0x02b8:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r2 = r1.currentMessageObject;
        r0.lambda$startAudioAgain$5$MediaController(r2);
        goto L_0x0356;
    L_0x02c3:
        r0 = r1.delegate;
        r2 = r1.currentMessageObject;
        r0.needPlayMessage(r2);
        goto L_0x0356;
    L_0x02cc:
        if (r0 != r7) goto L_0x0312;
    L_0x02ce:
        r0 = r1.drawImageButton;
        if (r0 == 0) goto L_0x0312;
    L_0x02d2:
        r0 = r1.buttonState;
        if (r0 != r6) goto L_0x0307;
    L_0x02d6:
        r0 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r0 == 0) goto L_0x02e5;
    L_0x02da:
        r0 = r1.delegate;
        r2 = r1.lastTouchX;
        r3 = r1.lastTouchY;
        r0.didPressImage(r1, r2, r3);
        goto L_0x0356;
    L_0x02e5:
        r1.buttonState = r7;
        r0 = r1.currentMessageObject;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.gifState = r3;
        r0 = r1.photoImage;
        r0.setAllowStartAnimation(r2);
        r0 = r1.photoImage;
        r0.stopAnimation();
        r0 = r1.radialProgress;
        r3 = r16.getIconForCurrentState();
        r0.setIcon(r3, r2, r8);
        r16.invalidate();
        r1.playSoundEffect(r2);
        goto L_0x0356;
    L_0x0307:
        if (r0 == r7) goto L_0x030b;
    L_0x0309:
        if (r0 != 0) goto L_0x0356;
    L_0x030b:
        r1.didPressButton(r8, r2);
        r1.playSoundEffect(r2);
        goto L_0x0356;
    L_0x0312:
        r0 = r1.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        if (r0 == 0) goto L_0x0336;
    L_0x031c:
        r3 = r0.embed_url;
        r3 = android.text.TextUtils.isEmpty(r3);
        if (r3 != 0) goto L_0x0336;
    L_0x0324:
        r9 = r1.delegate;
        r10 = r0.embed_url;
        r11 = r0.site_name;
        r12 = r0.title;
        r13 = r0.url;
        r14 = r0.embed_width;
        r15 = r0.embed_height;
        r9.needOpenWebView(r10, r11, r12, r13, r14, r15);
        goto L_0x0356;
    L_0x0336:
        r3 = r1.buttonState;
        if (r3 == r6) goto L_0x034a;
    L_0x033a:
        r4 = 3;
        if (r3 != r4) goto L_0x033e;
    L_0x033d:
        goto L_0x034a;
    L_0x033e:
        if (r0 == 0) goto L_0x0356;
    L_0x0340:
        r2 = r16.getContext();
        r0 = r0.url;
        org.telegram.messenger.browser.Browser.openUrl(r2, r0);
        goto L_0x0356;
    L_0x034a:
        r0 = r1.delegate;
        r3 = r1.lastTouchX;
        r4 = r1.lastTouchY;
        r0.didPressImage(r1, r3, r4);
        r1.playSoundEffect(r2);
    L_0x0356:
        r1.resetPressedLink(r7);
        return r8;
    L_0x035a:
        r0 = r17.getAction();
        if (r0 != r7) goto L_0x0371;
    L_0x0360:
        r0 = r1.instantButtonPressed;
        if (r0 == 0) goto L_0x0371;
    L_0x0364:
        r0 = android.os.Build.VERSION.SDK_INT;
        if (r0 < r5) goto L_0x0371;
    L_0x0368:
        r0 = r1.selectorDrawable;
        if (r0 == 0) goto L_0x0371;
    L_0x036c:
        r3 = (float) r3;
        r4 = (float) r4;
        r0.setHotspot(r3, r4);
    L_0x0371:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkLinkPreviewMotionEvent(android.view.MotionEvent):boolean");
    }

    private boolean checkPollButtonMotionEvent(MotionEvent motionEvent) {
        if (this.currentMessageObject.eventId != 0 || this.pollVoted || this.pollClosed || this.pollVoteInProgress || this.pollUnvoteInProgress || this.pollButtons.isEmpty()) {
            return false;
        }
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject.type != 17 || !messageObject.isSent()) {
            return false;
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int i;
        Drawable drawable;
        if (motionEvent.getAction() == 0) {
            this.pressedVoteButton = -1;
            i = 0;
            while (i < this.pollButtons.size()) {
                PollButton pollButton = (PollButton) this.pollButtons.get(i);
                int access$600 = (pollButton.y + this.namesOffset) - AndroidUtilities.dp(13.0f);
                if (x < pollButton.x || x > (pollButton.x + this.backgroundWidth) - AndroidUtilities.dp(31.0f) || y < access$600 || y > (pollButton.height + access$600) + AndroidUtilities.dp(26.0f)) {
                    i++;
                } else {
                    this.pressedVoteButton = i;
                    if (VERSION.SDK_INT >= 21) {
                        drawable = this.selectorDrawable;
                        if (drawable != null) {
                            drawable.setBounds(pollButton.x - AndroidUtilities.dp(9.0f), access$600, (pollButton.x + this.backgroundWidth) - AndroidUtilities.dp(22.0f), (pollButton.height + access$600) + AndroidUtilities.dp(26.0f));
                            this.selectorDrawable.setState(this.pressedState);
                            this.selectorDrawable.setHotspot((float) x, (float) y);
                        }
                    }
                    invalidate();
                    return true;
                }
            }
            return false;
        } else if (motionEvent.getAction() == 1) {
            if (this.pressedVoteButton == -1) {
                return false;
            }
            playSoundEffect(0);
            if (VERSION.SDK_INT >= 21) {
                drawable = this.selectorDrawable;
                if (drawable != null) {
                    drawable.setState(StateSet.NOTHING);
                }
            }
            if (this.currentMessageObject.scheduled) {
                Toast.makeText(getContext(), LocaleController.getString("MessageScheduledVote", NUM), 1).show();
            } else {
                i = this.pressedVoteButton;
                this.pollVoteInProgressNum = i;
                this.pollVoteInProgress = true;
                this.voteCurrentProgressTime = 0.0f;
                this.firstCircleLength = true;
                this.voteCurrentCircleLength = 360.0f;
                this.voteRisingCircleLength = false;
                this.delegate.didPressVoteButton(this, ((PollButton) this.pollButtons.get(i)).answer);
            }
            this.pressedVoteButton = -1;
            invalidate();
            return false;
        } else if (motionEvent.getAction() != 2 || this.pressedVoteButton == -1 || VERSION.SDK_INT < 21) {
            return false;
        } else {
            drawable = this.selectorDrawable;
            if (drawable == null) {
                return false;
            }
            drawable.setHotspot((float) x, (float) y);
            return false;
        }
    }

    private boolean checkInstantButtonMotionEvent(MotionEvent motionEvent) {
        if (this.drawInstantView && this.currentMessageObject.type != 0) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            Drawable drawable;
            if (motionEvent.getAction() == 0) {
                if (this.drawInstantView) {
                    float f = (float) x;
                    float f2 = (float) y;
                    if (this.instantButtonRect.contains(f, f2)) {
                        this.instantPressed = true;
                        if (VERSION.SDK_INT >= 21) {
                            drawable = this.selectorDrawable;
                            if (drawable != null && drawable.getBounds().contains(x, y)) {
                                this.selectorDrawable.setState(this.pressedState);
                                this.selectorDrawable.setHotspot(f, f2);
                                this.instantButtonPressed = true;
                            }
                        }
                        invalidate();
                        return true;
                    }
                }
            } else if (motionEvent.getAction() == 1) {
                if (this.instantPressed) {
                    ChatMessageCellDelegate chatMessageCellDelegate = this.delegate;
                    if (chatMessageCellDelegate != null) {
                        chatMessageCellDelegate.didPressInstantButton(this, this.drawInstantViewType);
                    }
                    playSoundEffect(0);
                    if (VERSION.SDK_INT >= 21) {
                        drawable = this.selectorDrawable;
                        if (drawable != null) {
                            drawable.setState(StateSet.NOTHING);
                        }
                    }
                    this.instantButtonPressed = false;
                    this.instantPressed = false;
                    invalidate();
                }
            } else if (motionEvent.getAction() == 2 && this.instantButtonPressed && VERSION.SDK_INT >= 21) {
                drawable = this.selectorDrawable;
                if (drawable != null) {
                    drawable.setHotspot((float) x, (float) y);
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:16:0x0026, code skipped:
            if (r4 != 8) goto L_0x0032;
     */
    private boolean checkOtherButtonMotionEvent(android.view.MotionEvent r7) {
        /*
        r6 = this;
        r0 = r6.currentMessageObject;
        r0 = r0.type;
        r1 = 16;
        r2 = 0;
        r3 = 1;
        if (r0 != r1) goto L_0x000c;
    L_0x000a:
        r0 = 1;
        goto L_0x000d;
    L_0x000c:
        r0 = 0;
    L_0x000d:
        if (r0 != 0) goto L_0x0033;
    L_0x000f:
        r0 = r6.documentAttachType;
        if (r0 == r3) goto L_0x0028;
    L_0x0013:
        r4 = r6.currentMessageObject;
        r4 = r4.type;
        r5 = 12;
        if (r4 == r5) goto L_0x0028;
    L_0x001b:
        r5 = 5;
        if (r0 == r5) goto L_0x0028;
    L_0x001e:
        r5 = 4;
        if (r0 == r5) goto L_0x0028;
    L_0x0021:
        r5 = 2;
        if (r0 == r5) goto L_0x0028;
    L_0x0024:
        r0 = 8;
        if (r4 != r0) goto L_0x0032;
    L_0x0028:
        r0 = r6.hasGamePreview;
        if (r0 != 0) goto L_0x0032;
    L_0x002c:
        r0 = r6.hasInvoicePreview;
        if (r0 != 0) goto L_0x0032;
    L_0x0030:
        r0 = 1;
        goto L_0x0033;
    L_0x0032:
        r0 = 0;
    L_0x0033:
        if (r0 != 0) goto L_0x0036;
    L_0x0035:
        return r2;
    L_0x0036:
        r0 = r7.getX();
        r0 = (int) r0;
        r4 = r7.getY();
        r4 = (int) r4;
        r5 = r7.getAction();
        if (r5 != 0) goto L_0x00a5;
    L_0x0046:
        r7 = r6.currentMessageObject;
        r7 = r7.type;
        if (r7 != r1) goto L_0x0075;
    L_0x004c:
        r7 = r6.otherX;
        if (r0 < r7) goto L_0x00c3;
    L_0x0050:
        r1 = NUM; // 0x436b0000 float:235.0 double:5.58830648E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r7 = r7 + r1;
        if (r0 > r7) goto L_0x00c3;
    L_0x0059:
        r7 = r6.otherY;
        r0 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r7 = r7 - r0;
        if (r4 < r7) goto L_0x00c3;
    L_0x0064:
        r7 = r6.otherY;
        r0 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r7 = r7 + r0;
        if (r4 > r7) goto L_0x00c3;
    L_0x006f:
        r6.otherPressed = r3;
        r6.invalidate();
        goto L_0x00c4;
    L_0x0075:
        r7 = r6.otherX;
        r1 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r7 = r7 - r5;
        if (r0 < r7) goto L_0x00c3;
    L_0x0080:
        r7 = r6.otherX;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r7 = r7 + r1;
        if (r0 > r7) goto L_0x00c3;
    L_0x0089:
        r7 = r6.otherY;
        r0 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r7 = r7 - r0;
        if (r4 < r7) goto L_0x00c3;
    L_0x0094:
        r7 = r6.otherY;
        r0 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r7 = r7 + r0;
        if (r4 > r7) goto L_0x00c3;
    L_0x009f:
        r6.otherPressed = r3;
        r6.invalidate();
        goto L_0x00c4;
    L_0x00a5:
        r7 = r7.getAction();
        if (r7 != r3) goto L_0x00c3;
    L_0x00ab:
        r7 = r6.otherPressed;
        if (r7 == 0) goto L_0x00c3;
    L_0x00af:
        r6.otherPressed = r2;
        r6.playSoundEffect(r2);
        r7 = r6.delegate;
        r0 = r6.otherX;
        r0 = (float) r0;
        r1 = r6.otherY;
        r1 = (float) r1;
        r7.didPressOther(r6, r0, r1);
        r6.invalidate();
        goto L_0x00c4;
    L_0x00c3:
        r3 = 0;
    L_0x00c4:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkOtherButtonMotionEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x014d  */
    private boolean checkPhotoImageMotionEvent(android.view.MotionEvent r9) {
        /*
        r8 = this;
        r0 = r8.drawPhotoImage;
        r1 = 0;
        r2 = 1;
        if (r0 != 0) goto L_0x000b;
    L_0x0006:
        r0 = r8.documentAttachType;
        if (r0 == r2) goto L_0x000b;
    L_0x000a:
        return r1;
    L_0x000b:
        r0 = r9.getX();
        r0 = (int) r0;
        r3 = r9.getY();
        r3 = (int) r3;
        r4 = r9.getAction();
        r5 = -1;
        if (r4 != 0) goto L_0x0177;
    L_0x001c:
        r9 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r4 = r8.miniButtonState;
        if (r4 < 0) goto L_0x0042;
    L_0x0026:
        r4 = NUM; // 0x41d80000 float:27.0 double:5.457818764E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r6 = r8.buttonX;
        r7 = r6 + r4;
        if (r0 < r7) goto L_0x0042;
    L_0x0032:
        r6 = r6 + r4;
        r6 = r6 + r9;
        if (r0 > r6) goto L_0x0042;
    L_0x0036:
        r6 = r8.buttonY;
        r7 = r6 + r4;
        if (r3 < r7) goto L_0x0042;
    L_0x003c:
        r6 = r6 + r4;
        r6 = r6 + r9;
        if (r3 > r6) goto L_0x0042;
    L_0x0040:
        r4 = 1;
        goto L_0x0043;
    L_0x0042:
        r4 = 0;
    L_0x0043:
        if (r4 == 0) goto L_0x004c;
    L_0x0045:
        r8.miniButtonPressed = r2;
        r8.invalidate();
        goto L_0x0149;
    L_0x004c:
        r4 = r8.buttonState;
        if (r4 == r5) goto L_0x006e;
    L_0x0050:
        r4 = r8.radialProgress;
        r4 = r4.getIcon();
        r6 = 4;
        if (r4 == r6) goto L_0x006e;
    L_0x0059:
        r4 = r8.buttonX;
        if (r0 < r4) goto L_0x006e;
    L_0x005d:
        r4 = r4 + r9;
        if (r0 > r4) goto L_0x006e;
    L_0x0060:
        r4 = r8.buttonY;
        if (r3 < r4) goto L_0x006e;
    L_0x0064:
        r4 = r4 + r9;
        if (r3 > r4) goto L_0x006e;
    L_0x0067:
        r8.buttonPressed = r2;
        r8.invalidate();
        goto L_0x0149;
    L_0x006e:
        r9 = r8.drawVideoImageButton;
        if (r9 == 0) goto L_0x00a0;
    L_0x0072:
        r9 = r8.buttonState;
        if (r9 == r5) goto L_0x00a0;
    L_0x0076:
        r9 = r8.videoButtonX;
        if (r0 < r9) goto L_0x00a0;
    L_0x007a:
        r4 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r9 = r9 + r4;
        r4 = r8.infoWidth;
        r6 = r8.docTitleWidth;
        r4 = java.lang.Math.max(r4, r6);
        r9 = r9 + r4;
        if (r0 > r9) goto L_0x00a0;
    L_0x008c:
        r9 = r8.videoButtonY;
        if (r3 < r9) goto L_0x00a0;
    L_0x0090:
        r4 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r9 = r9 + r4;
        if (r3 > r9) goto L_0x00a0;
    L_0x0099:
        r8.videoButtonPressed = r2;
        r8.invalidate();
        goto L_0x0149;
    L_0x00a0:
        r9 = r8.documentAttachType;
        if (r9 != r2) goto L_0x00d9;
    L_0x00a4:
        r9 = r8.photoImage;
        r9 = r9.getImageX();
        if (r0 < r9) goto L_0x00f2;
    L_0x00ac:
        r9 = r8.photoImage;
        r9 = r9.getImageX();
        r4 = r8.backgroundWidth;
        r9 = r9 + r4;
        r4 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r9 = r9 - r4;
        if (r0 > r9) goto L_0x00f2;
    L_0x00be:
        r9 = r8.photoImage;
        r9 = r9.getImageY();
        if (r3 < r9) goto L_0x00f2;
    L_0x00c6:
        r9 = r8.photoImage;
        r9 = r9.getImageY();
        r0 = r8.photoImage;
        r0 = r0.getImageHeight();
        r9 = r9 + r0;
        if (r3 > r9) goto L_0x00f2;
    L_0x00d5:
        r8.imagePressed = r2;
        goto L_0x0149;
    L_0x00d9:
        r9 = r8.currentMessageObject;
        r9 = r9.isAnyKindOfSticker();
        if (r9 == 0) goto L_0x00f4;
    L_0x00e1:
        r9 = r8.currentMessageObject;
        r9 = r9.getInputStickerSet();
        if (r9 != 0) goto L_0x00f4;
    L_0x00e9:
        r9 = r8.currentMessageObject;
        r9 = r9.isAnimatedEmoji();
        if (r9 == 0) goto L_0x00f2;
    L_0x00f1:
        goto L_0x00f4;
    L_0x00f2:
        r2 = 0;
        goto L_0x0149;
    L_0x00f4:
        r9 = r8.photoImage;
        r9 = r9.getImageX();
        if (r0 < r9) goto L_0x0125;
    L_0x00fc:
        r9 = r8.photoImage;
        r9 = r9.getImageX();
        r4 = r8.photoImage;
        r4 = r4.getImageWidth();
        r9 = r9 + r4;
        if (r0 > r9) goto L_0x0125;
    L_0x010b:
        r9 = r8.photoImage;
        r9 = r9.getImageY();
        if (r3 < r9) goto L_0x0125;
    L_0x0113:
        r9 = r8.photoImage;
        r9 = r9.getImageY();
        r0 = r8.photoImage;
        r0 = r0.getImageHeight();
        r9 = r9 + r0;
        if (r3 > r9) goto L_0x0125;
    L_0x0122:
        r8.imagePressed = r2;
        goto L_0x0126;
    L_0x0125:
        r2 = 0;
    L_0x0126:
        r9 = r8.currentMessageObject;
        r9 = r9.type;
        r0 = 12;
        if (r9 != r0) goto L_0x0149;
    L_0x012e:
        r9 = r8.currentAccount;
        r9 = org.telegram.messenger.MessagesController.getInstance(r9);
        r0 = r8.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.media;
        r0 = r0.user_id;
        r0 = java.lang.Integer.valueOf(r0);
        r9 = r9.getUser(r0);
        if (r9 != 0) goto L_0x0149;
    L_0x0146:
        r8.imagePressed = r1;
        goto L_0x00f2;
    L_0x0149:
        r9 = r8.imagePressed;
        if (r9 == 0) goto L_0x0175;
    L_0x014d:
        r9 = r8.currentMessageObject;
        r9 = r9.isSendError();
        if (r9 == 0) goto L_0x0159;
    L_0x0155:
        r8.imagePressed = r1;
        goto L_0x01dc;
    L_0x0159:
        r9 = r8.currentMessageObject;
        r9 = r9.type;
        r0 = 8;
        if (r9 != r0) goto L_0x0175;
    L_0x0161:
        r9 = r8.buttonState;
        if (r9 != r5) goto L_0x0175;
    L_0x0165:
        r9 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r9 == 0) goto L_0x0175;
    L_0x0169:
        r9 = r8.photoImage;
        r9 = r9.getAnimation();
        if (r9 != 0) goto L_0x0175;
    L_0x0171:
        r8.imagePressed = r1;
        goto L_0x01dc;
    L_0x0175:
        r1 = r2;
        goto L_0x01dc;
    L_0x0177:
        r9 = r9.getAction();
        if (r9 != r2) goto L_0x01dc;
    L_0x017d:
        r9 = r8.videoButtonPressed;
        if (r9 != r2) goto L_0x018d;
    L_0x0181:
        r8.videoButtonPressed = r1;
        r8.playSoundEffect(r1);
        r8.didPressButton(r2, r2);
        r8.invalidate();
        goto L_0x01dc;
    L_0x018d:
        r9 = r8.buttonPressed;
        if (r9 != r2) goto L_0x01a5;
    L_0x0191:
        r8.buttonPressed = r1;
        r8.playSoundEffect(r1);
        r9 = r8.drawVideoImageButton;
        if (r9 == 0) goto L_0x019e;
    L_0x019a:
        r8.didClickedImage();
        goto L_0x01a1;
    L_0x019e:
        r8.didPressButton(r2, r1);
    L_0x01a1:
        r8.invalidate();
        goto L_0x01dc;
    L_0x01a5:
        r9 = r8.miniButtonPressed;
        if (r9 != r2) goto L_0x01b5;
    L_0x01a9:
        r8.miniButtonPressed = r1;
        r8.playSoundEffect(r1);
        r8.didPressMiniButton(r2);
        r8.invalidate();
        goto L_0x01dc;
    L_0x01b5:
        r9 = r8.imagePressed;
        if (r9 == 0) goto L_0x01dc;
    L_0x01b9:
        r8.imagePressed = r1;
        r9 = r8.buttonState;
        if (r9 == r5) goto L_0x01d3;
    L_0x01bf:
        r0 = 2;
        if (r9 == r0) goto L_0x01d3;
    L_0x01c2:
        r0 = 3;
        if (r9 == r0) goto L_0x01d3;
    L_0x01c5:
        r0 = r8.drawVideoImageButton;
        if (r0 == 0) goto L_0x01ca;
    L_0x01c9:
        goto L_0x01d3;
    L_0x01ca:
        if (r9 != 0) goto L_0x01d9;
    L_0x01cc:
        r8.playSoundEffect(r1);
        r8.didPressButton(r2, r1);
        goto L_0x01d9;
    L_0x01d3:
        r8.playSoundEffect(r1);
        r8.didClickedImage();
    L_0x01d9:
        r8.invalidate();
    L_0x01dc:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkPhotoImageMotionEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:75:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x010b A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x010b A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x011b  */
    /* JADX WARNING: Missing block: B:49:0x00c5, code skipped:
            if (r3 <= (r0 + r6)) goto L_0x00c7;
     */
    /* JADX WARNING: Missing block: B:65:0x0101, code skipped:
            if (r3 <= (r0 + r6)) goto L_0x00c7;
     */
    private boolean checkAudioMotionEvent(android.view.MotionEvent r13) {
        /*
        r12 = this;
        r0 = r12.documentAttachType;
        r1 = 3;
        r2 = 0;
        if (r0 == r1) goto L_0x000a;
    L_0x0006:
        r3 = 5;
        if (r0 == r3) goto L_0x000a;
    L_0x0009:
        return r2;
    L_0x000a:
        r0 = r13.getX();
        r0 = (int) r0;
        r3 = r13.getY();
        r3 = (int) r3;
        r4 = r12.useSeekBarWaweform;
        if (r4 == 0) goto L_0x003b;
    L_0x0018:
        r4 = r12.seekBarWaveform;
        r5 = r13.getAction();
        r6 = r13.getX();
        r7 = r12.seekBarX;
        r7 = (float) r7;
        r6 = r6 - r7;
        r7 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r6 = r6 - r7;
        r7 = r13.getY();
        r8 = r12.seekBarY;
        r8 = (float) r8;
        r7 = r7 - r8;
        r4 = r4.onTouch(r5, r6, r7);
        goto L_0x0055;
    L_0x003b:
        r4 = r12.seekBar;
        r5 = r13.getAction();
        r6 = r13.getX();
        r7 = r12.seekBarX;
        r7 = (float) r7;
        r6 = r6 - r7;
        r7 = r13.getY();
        r8 = r12.seekBarY;
        r8 = (float) r8;
        r7 = r7 - r8;
        r4 = r4.onTouch(r5, r6, r7);
    L_0x0055:
        r5 = 1;
        if (r4 == 0) goto L_0x0086;
    L_0x0058:
        r0 = r12.useSeekBarWaweform;
        if (r0 != 0) goto L_0x006a;
    L_0x005c:
        r0 = r13.getAction();
        if (r0 != 0) goto L_0x006a;
    L_0x0062:
        r13 = r12.getParent();
        r13.requestDisallowInterceptTouchEvent(r5);
        goto L_0x007f;
    L_0x006a:
        r0 = r12.useSeekBarWaweform;
        if (r0 == 0) goto L_0x007f;
    L_0x006e:
        r0 = r12.seekBarWaveform;
        r0 = r0.isStartDraging();
        if (r0 != 0) goto L_0x007f;
    L_0x0076:
        r13 = r13.getAction();
        if (r13 != r5) goto L_0x007f;
    L_0x007c:
        r12.didPressButton(r5, r2);
    L_0x007f:
        r12.disallowLongPress = r5;
        r12.invalidate();
        goto L_0x017a;
    L_0x0086:
        r6 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r7 = r12.miniButtonState;
        if (r7 < 0) goto L_0x00ac;
    L_0x0090:
        r7 = NUM; // 0x41d80000 float:27.0 double:5.457818764E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = r12.buttonX;
        r9 = r8 + r7;
        if (r0 < r9) goto L_0x00ac;
    L_0x009c:
        r8 = r8 + r7;
        r8 = r8 + r6;
        if (r0 > r8) goto L_0x00ac;
    L_0x00a0:
        r8 = r12.buttonY;
        r9 = r8 + r7;
        if (r3 < r9) goto L_0x00ac;
    L_0x00a6:
        r8 = r8 + r7;
        r8 = r8 + r6;
        if (r3 > r8) goto L_0x00ac;
    L_0x00aa:
        r7 = 1;
        goto L_0x00ad;
    L_0x00ac:
        r7 = 0;
    L_0x00ad:
        r8 = 2;
        if (r7 != 0) goto L_0x0104;
    L_0x00b0:
        r9 = r12.buttonState;
        if (r9 == 0) goto L_0x00c9;
    L_0x00b4:
        if (r9 == r5) goto L_0x00c9;
    L_0x00b6:
        if (r9 != r8) goto L_0x00b9;
    L_0x00b8:
        goto L_0x00c9;
    L_0x00b9:
        r9 = r12.buttonX;
        if (r0 < r9) goto L_0x0104;
    L_0x00bd:
        r9 = r9 + r6;
        if (r0 > r9) goto L_0x0104;
    L_0x00c0:
        r0 = r12.buttonY;
        if (r3 < r0) goto L_0x0104;
    L_0x00c4:
        r0 = r0 + r6;
        if (r3 > r0) goto L_0x0104;
    L_0x00c7:
        r0 = 1;
        goto L_0x0105;
    L_0x00c9:
        r9 = r12.buttonX;
        r10 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 - r11;
        if (r0 < r9) goto L_0x0104;
    L_0x00d4:
        r9 = r12.buttonX;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 - r10;
        r10 = r12.backgroundWidth;
        r9 = r9 + r10;
        if (r0 > r9) goto L_0x0104;
    L_0x00e0:
        r0 = r12.drawInstantView;
        if (r0 == 0) goto L_0x00e7;
    L_0x00e4:
        r0 = r12.buttonY;
        goto L_0x00ec;
    L_0x00e7:
        r0 = r12.namesOffset;
        r9 = r12.mediaOffsetY;
        r0 = r0 + r9;
    L_0x00ec:
        if (r3 < r0) goto L_0x0104;
    L_0x00ee:
        r0 = r12.drawInstantView;
        if (r0 == 0) goto L_0x00f5;
    L_0x00f2:
        r0 = r12.buttonY;
        goto L_0x0100;
    L_0x00f5:
        r0 = r12.namesOffset;
        r6 = r12.mediaOffsetY;
        r0 = r0 + r6;
        r6 = NUM; // 0x42a40000 float:82.0 double:5.5238721E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
    L_0x0100:
        r0 = r0 + r6;
        if (r3 > r0) goto L_0x0104;
    L_0x0103:
        goto L_0x00c7;
    L_0x0104:
        r0 = 0;
    L_0x0105:
        r3 = r13.getAction();
        if (r3 != 0) goto L_0x011b;
    L_0x010b:
        if (r0 != 0) goto L_0x010f;
    L_0x010d:
        if (r7 == 0) goto L_0x017a;
    L_0x010f:
        if (r0 == 0) goto L_0x0114;
    L_0x0111:
        r12.buttonPressed = r5;
        goto L_0x0116;
    L_0x0114:
        r12.miniButtonPressed = r5;
    L_0x0116:
        r12.invalidate();
        r4 = 1;
        goto L_0x017a;
    L_0x011b:
        r3 = r12.buttonPressed;
        if (r3 == 0) goto L_0x014b;
    L_0x011f:
        r3 = r13.getAction();
        if (r3 != r5) goto L_0x0131;
    L_0x0125:
        r12.buttonPressed = r2;
        r12.playSoundEffect(r2);
        r12.didPressButton(r5, r2);
        r12.invalidate();
        goto L_0x017a;
    L_0x0131:
        r3 = r13.getAction();
        if (r3 != r1) goto L_0x013d;
    L_0x0137:
        r12.buttonPressed = r2;
        r12.invalidate();
        goto L_0x017a;
    L_0x013d:
        r13 = r13.getAction();
        if (r13 != r8) goto L_0x017a;
    L_0x0143:
        if (r0 != 0) goto L_0x017a;
    L_0x0145:
        r12.buttonPressed = r2;
        r12.invalidate();
        goto L_0x017a;
    L_0x014b:
        r0 = r12.miniButtonPressed;
        if (r0 == 0) goto L_0x017a;
    L_0x014f:
        r0 = r13.getAction();
        if (r0 != r5) goto L_0x0161;
    L_0x0155:
        r12.miniButtonPressed = r2;
        r12.playSoundEffect(r2);
        r12.didPressMiniButton(r5);
        r12.invalidate();
        goto L_0x017a;
    L_0x0161:
        r0 = r13.getAction();
        if (r0 != r1) goto L_0x016d;
    L_0x0167:
        r12.miniButtonPressed = r2;
        r12.invalidate();
        goto L_0x017a;
    L_0x016d:
        r13 = r13.getAction();
        if (r13 != r8) goto L_0x017a;
    L_0x0173:
        if (r7 != 0) goto L_0x017a;
    L_0x0175:
        r12.miniButtonPressed = r2;
        r12.invalidate();
    L_0x017a:
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.checkAudioMotionEvent(android.view.MotionEvent):boolean");
    }

    private boolean checkBotButtonMotionEvent(MotionEvent motionEvent) {
        if (this.botButtons.isEmpty() || this.currentMessageObject.eventId != 0) {
            return false;
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (motionEvent.getAction() == 0) {
            int measuredWidth;
            if (this.currentMessageObject.isOutOwner()) {
                measuredWidth = (getMeasuredWidth() - this.widthForButtons) - AndroidUtilities.dp(10.0f);
            } else {
                measuredWidth = this.backgroundDrawableLeft + AndroidUtilities.dp(this.mediaBackground ? 1.0f : 7.0f);
            }
            int i = 0;
            while (i < this.botButtons.size()) {
                BotButton botButton = (BotButton) this.botButtons.get(i);
                int access$1000 = (botButton.y + this.layoutHeight) - AndroidUtilities.dp(2.0f);
                if (x < botButton.x + measuredWidth || x > (botButton.x + measuredWidth) + botButton.width || y < access$1000 || y > access$1000 + botButton.height) {
                    i++;
                } else {
                    this.pressedBotButton = i;
                    invalidate();
                    return true;
                }
            }
            return false;
        } else if (motionEvent.getAction() != 1 || this.pressedBotButton == -1) {
            return false;
        } else {
            playSoundEffect(0);
            if (this.currentMessageObject.scheduled) {
                Toast.makeText(getContext(), LocaleController.getString("MessageScheduledBotAction", NUM), 1).show();
            } else {
                BotButton botButton2 = (BotButton) this.botButtons.get(this.pressedBotButton);
                if (botButton2.button != null) {
                    this.delegate.didPressBotButton(this, botButton2.button);
                } else if (botButton2.reaction != null) {
                    this.delegate.didPressReaction(this, botButton2.reaction);
                }
            }
            this.pressedBotButton = -1;
            invalidate();
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:118:0x01d4  */
    /* JADX WARNING: Missing block: B:174:0x02b4, code skipped:
            if (r4 <= ((float) (r1 + org.telegram.messenger.AndroidUtilities.dp(32.0f)))) goto L_0x040a;
     */
    /* JADX WARNING: Missing block: B:203:0x0317, code skipped:
            if (r4 <= ((float) (r1 + org.telegram.messenger.AndroidUtilities.dp(32.0f)))) goto L_0x040a;
     */
    /* JADX WARNING: Missing block: B:239:0x03b8, code skipped:
            if (r4 <= ((float) (r1 + org.telegram.messenger.AndroidUtilities.dp(35.0f)))) goto L_0x040a;
     */
    /* JADX WARNING: Missing block: B:260:0x0403, code skipped:
            if (r4 <= ((float) (r1 + org.telegram.messenger.AndroidUtilities.dp(32.0f)))) goto L_0x0407;
     */
    public boolean onTouchEvent(android.view.MotionEvent r14) {
        /*
        r13 = this;
        r0 = r13.currentMessageObject;
        if (r0 == 0) goto L_0x040b;
    L_0x0004:
        r0 = r13.delegate;
        r0 = r0.canPerformActions();
        if (r0 == 0) goto L_0x040b;
    L_0x000c:
        r0 = r13.animationRunning;
        if (r0 == 0) goto L_0x0012;
    L_0x0010:
        goto L_0x040b;
    L_0x0012:
        r0 = 0;
        r13.disallowLongPress = r0;
        r1 = r14.getX();
        r13.lastTouchX = r1;
        r1 = r14.getY();
        r13.lastTouchX = r1;
        r1 = r13.checkTextBlockMotionEvent(r14);
        if (r1 != 0) goto L_0x002b;
    L_0x0027:
        r1 = r13.checkOtherButtonMotionEvent(r14);
    L_0x002b:
        if (r1 != 0) goto L_0x0031;
    L_0x002d:
        r1 = r13.checkCaptionMotionEvent(r14);
    L_0x0031:
        if (r1 != 0) goto L_0x0037;
    L_0x0033:
        r1 = r13.checkAudioMotionEvent(r14);
    L_0x0037:
        if (r1 != 0) goto L_0x003d;
    L_0x0039:
        r1 = r13.checkLinkPreviewMotionEvent(r14);
    L_0x003d:
        if (r1 != 0) goto L_0x0043;
    L_0x003f:
        r1 = r13.checkInstantButtonMotionEvent(r14);
    L_0x0043:
        if (r1 != 0) goto L_0x0049;
    L_0x0045:
        r1 = r13.checkGameMotionEvent(r14);
    L_0x0049:
        if (r1 != 0) goto L_0x004f;
    L_0x004b:
        r1 = r13.checkPhotoImageMotionEvent(r14);
    L_0x004f:
        if (r1 != 0) goto L_0x0055;
    L_0x0051:
        r1 = r13.checkBotButtonMotionEvent(r14);
    L_0x0055:
        if (r1 != 0) goto L_0x005b;
    L_0x0057:
        r1 = r13.checkPollButtonMotionEvent(r14);
    L_0x005b:
        r2 = r14.getAction();
        r3 = 3;
        if (r2 != r3) goto L_0x008d;
    L_0x0062:
        r13.buttonPressed = r0;
        r13.miniButtonPressed = r0;
        r1 = -1;
        r13.pressedBotButton = r1;
        r13.pressedVoteButton = r1;
        r13.linkPreviewPressed = r0;
        r13.otherPressed = r0;
        r13.sharePressed = r0;
        r13.imagePressed = r0;
        r13.gamePreviewPressed = r0;
        r13.instantButtonPressed = r0;
        r13.instantPressed = r0;
        r2 = android.os.Build.VERSION.SDK_INT;
        r4 = 21;
        if (r2 < r4) goto L_0x0088;
    L_0x007f:
        r2 = r13.selectorDrawable;
        if (r2 == 0) goto L_0x0088;
    L_0x0083:
        r4 = android.util.StateSet.NOTHING;
        r2.setState(r4);
    L_0x0088:
        r13.resetPressedLink(r1);
        r6 = 0;
        goto L_0x008e;
    L_0x008d:
        r6 = r1;
    L_0x008e:
        r13.updateRadialProgressBackground();
        r1 = r13.disallowLongPress;
        if (r1 != 0) goto L_0x00a0;
    L_0x0095:
        if (r6 == 0) goto L_0x00a0;
    L_0x0097:
        r1 = r14.getAction();
        if (r1 != 0) goto L_0x00a0;
    L_0x009d:
        r13.startCheckLongPress();
    L_0x00a0:
        r1 = r14.getAction();
        r2 = 2;
        if (r1 == 0) goto L_0x00b0;
    L_0x00a7:
        r1 = r14.getAction();
        if (r1 == r2) goto L_0x00b0;
    L_0x00ad:
        r13.cancelCheckLongPress();
    L_0x00b0:
        if (r6 != 0) goto L_0x040a;
    L_0x00b2:
        r1 = r14.getX();
        r4 = r14.getY();
        r5 = r14.getAction();
        r7 = NUM; // 0x420CLASSNAME float:35.0 double:5.47465589E-315;
        r8 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r9 = NUM; // 0x42200000 float:40.0 double:5.481131706E-315;
        r10 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r11 = NUM; // 0x42000000 float:32.0 double:5.4707704E-315;
        r12 = 1;
        if (r5 != 0) goto L_0x01d9;
    L_0x00cb:
        r2 = r13.delegate;
        if (r2 == 0) goto L_0x00d5;
    L_0x00cf:
        r2 = r2.canPerformActions();
        if (r2 == 0) goto L_0x040a;
    L_0x00d5:
        r2 = r13.isAvatarVisible;
        if (r2 == 0) goto L_0x00ec;
    L_0x00d9:
        r2 = r13.avatarImage;
        r3 = r13.getTop();
        r3 = (float) r3;
        r3 = r3 + r4;
        r2 = r2.isInsideImage(r1, r3);
        if (r2 == 0) goto L_0x00ec;
    L_0x00e7:
        r13.avatarPressed = r12;
    L_0x00e9:
        r6 = 1;
        goto L_0x01d2;
    L_0x00ec:
        r2 = r13.drawForwardedName;
        if (r2 == 0) goto L_0x012f;
    L_0x00f0:
        r2 = r13.forwardedNameLayout;
        r0 = r2[r0];
        if (r0 == 0) goto L_0x012f;
    L_0x00f6:
        r0 = r13.forwardNameX;
        r2 = (float) r0;
        r2 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x012f;
    L_0x00fd:
        r2 = r13.forwardedNameWidth;
        r0 = r0 + r2;
        r0 = (float) r0;
        r0 = (r1 > r0 ? 1 : (r1 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x012f;
    L_0x0105:
        r0 = r13.forwardNameY;
        r2 = (float) r0;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x012f;
    L_0x010c:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r0 = r0 + r2;
        r0 = (float) r0;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x012f;
    L_0x0116:
        r0 = r13.viaWidth;
        if (r0 == 0) goto L_0x012c;
    L_0x011a:
        r0 = r13.forwardNameX;
        r2 = r13.viaNameWidth;
        r0 = r0 + r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = r0 + r2;
        r0 = (float) r0;
        r0 = (r1 > r0 ? 1 : (r1 == r0 ? 0 : -1));
        if (r0 < 0) goto L_0x012c;
    L_0x0129:
        r13.forwardBotPressed = r12;
        goto L_0x00e9;
    L_0x012c:
        r13.forwardNamePressed = r12;
        goto L_0x00e9;
    L_0x012f:
        r0 = r13.drawNameLayout;
        if (r0 == 0) goto L_0x0168;
    L_0x0133:
        r0 = r13.nameLayout;
        if (r0 == 0) goto L_0x0168;
    L_0x0137:
        r0 = r13.viaWidth;
        if (r0 == 0) goto L_0x0168;
    L_0x013b:
        r2 = r13.nameX;
        r3 = r13.viaNameWidth;
        r5 = (float) r3;
        r5 = r5 + r2;
        r5 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1));
        if (r5 < 0) goto L_0x0168;
    L_0x0145:
        r3 = (float) r3;
        r2 = r2 + r3;
        r0 = (float) r0;
        r2 = r2 + r0;
        r0 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r0 > 0) goto L_0x0168;
    L_0x014d:
        r0 = r13.nameY;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r2 = (float) r2;
        r0 = r0 - r2;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 < 0) goto L_0x0168;
    L_0x0159:
        r0 = r13.nameY;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r2 = (float) r2;
        r0 = r0 + r2;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x0168;
    L_0x0165:
        r13.forwardBotPressed = r12;
        goto L_0x00e9;
    L_0x0168:
        r0 = r13.drawShareButton;
        if (r0 == 0) goto L_0x0195;
    L_0x016c:
        r0 = r13.shareStartX;
        r2 = (float) r0;
        r2 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x0195;
    L_0x0173:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r0 = r0 + r2;
        r0 = (float) r0;
        r0 = (r1 > r0 ? 1 : (r1 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x0195;
    L_0x017d:
        r0 = r13.shareStartY;
        r2 = (float) r0;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x0195;
    L_0x0184:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r0 = r0 + r2;
        r0 = (float) r0;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x0195;
    L_0x018e:
        r13.sharePressed = r12;
        r13.invalidate();
        goto L_0x00e9;
    L_0x0195:
        r0 = r13.replyNameLayout;
        if (r0 == 0) goto L_0x01d2;
    L_0x0199:
        r0 = r13.currentMessageObject;
        r0 = r0.shouldDrawWithoutBackground();
        if (r0 == 0) goto L_0x01ac;
    L_0x01a1:
        r0 = r13.replyStartX;
        r2 = r13.replyNameWidth;
        r3 = r13.replyTextWidth;
        r2 = java.lang.Math.max(r2, r3);
        goto L_0x01b0;
    L_0x01ac:
        r0 = r13.replyStartX;
        r2 = r13.backgroundDrawableRight;
    L_0x01b0:
        r0 = r0 + r2;
        r2 = r13.replyStartX;
        r2 = (float) r2;
        r2 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x01d2;
    L_0x01b8:
        r0 = (float) r0;
        r0 = (r1 > r0 ? 1 : (r1 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x01d2;
    L_0x01bd:
        r0 = r13.replyStartY;
        r1 = (float) r0;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 < 0) goto L_0x01d2;
    L_0x01c4:
        r1 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r0 = r0 + r1;
        r0 = (float) r0;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 > 0) goto L_0x01d2;
    L_0x01ce:
        r13.replyPressed = r12;
        goto L_0x00e9;
    L_0x01d2:
        if (r6 == 0) goto L_0x040a;
    L_0x01d4:
        r13.startCheckLongPress();
        goto L_0x040a;
    L_0x01d9:
        r5 = r14.getAction();
        if (r5 == r2) goto L_0x01e2;
    L_0x01df:
        r13.cancelCheckLongPress();
    L_0x01e2:
        r5 = r13.avatarPressed;
        if (r5 == 0) goto L_0x0247;
    L_0x01e6:
        r5 = r14.getAction();
        if (r5 != r12) goto L_0x0221;
    L_0x01ec:
        r13.avatarPressed = r0;
        r13.playSoundEffect(r0);
        r0 = r13.delegate;
        if (r0 == 0) goto L_0x040a;
    L_0x01f5:
        r1 = r13.currentUser;
        if (r1 == 0) goto L_0x020b;
    L_0x01f9:
        r2 = r1.id;
        if (r2 != 0) goto L_0x0202;
    L_0x01fd:
        r0.didPressHiddenForward(r13);
        goto L_0x040a;
    L_0x0202:
        r2 = r13.lastTouchX;
        r3 = r13.lastTouchY;
        r0.didPressUserAvatar(r13, r1, r2, r3);
        goto L_0x040a;
    L_0x020b:
        r2 = r13.currentChat;
        if (r2 == 0) goto L_0x040a;
    L_0x020f:
        r1 = r13.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.fwd_from;
        r3 = r1.channel_post;
        r4 = r13.lastTouchX;
        r5 = r13.lastTouchY;
        r1 = r13;
        r0.didPressChannelAvatar(r1, r2, r3, r4, r5);
        goto L_0x040a;
    L_0x0221:
        r5 = r14.getAction();
        if (r5 != r3) goto L_0x022b;
    L_0x0227:
        r13.avatarPressed = r0;
        goto L_0x040a;
    L_0x022b:
        r3 = r14.getAction();
        if (r3 != r2) goto L_0x040a;
    L_0x0231:
        r2 = r13.isAvatarVisible;
        if (r2 == 0) goto L_0x040a;
    L_0x0235:
        r2 = r13.avatarImage;
        r3 = r13.getTop();
        r3 = (float) r3;
        r4 = r4 + r3;
        r1 = r2.isInsideImage(r1, r4);
        if (r1 != 0) goto L_0x040a;
    L_0x0243:
        r13.avatarPressed = r0;
        goto L_0x040a;
    L_0x0247:
        r5 = r13.forwardNamePressed;
        if (r5 == 0) goto L_0x02ba;
    L_0x024b:
        r5 = r14.getAction();
        if (r5 != r12) goto L_0x0286;
    L_0x0251:
        r13.forwardNamePressed = r0;
        r13.playSoundEffect(r0);
        r0 = r13.delegate;
        if (r0 == 0) goto L_0x040a;
    L_0x025a:
        r2 = r13.currentForwardChannel;
        if (r2 == 0) goto L_0x0270;
    L_0x025e:
        r1 = r13.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.fwd_from;
        r3 = r1.channel_post;
        r4 = r13.lastTouchX;
        r5 = r13.lastTouchY;
        r1 = r13;
        r0.didPressChannelAvatar(r1, r2, r3, r4, r5);
        goto L_0x040a;
    L_0x0270:
        r1 = r13.currentForwardUser;
        if (r1 == 0) goto L_0x027d;
    L_0x0274:
        r2 = r13.lastTouchX;
        r3 = r13.lastTouchY;
        r0.didPressUserAvatar(r13, r1, r2, r3);
        goto L_0x040a;
    L_0x027d:
        r1 = r13.currentForwardName;
        if (r1 == 0) goto L_0x040a;
    L_0x0281:
        r0.didPressHiddenForward(r13);
        goto L_0x040a;
    L_0x0286:
        r5 = r14.getAction();
        if (r5 != r3) goto L_0x0290;
    L_0x028c:
        r13.forwardNamePressed = r0;
        goto L_0x040a;
    L_0x0290:
        r3 = r14.getAction();
        if (r3 != r2) goto L_0x040a;
    L_0x0296:
        r2 = r13.forwardNameX;
        r3 = (float) r2;
        r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r3 < 0) goto L_0x02b6;
    L_0x029d:
        r3 = r13.forwardedNameWidth;
        r2 = r2 + r3;
        r2 = (float) r2;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 > 0) goto L_0x02b6;
    L_0x02a5:
        r1 = r13.forwardNameY;
        r2 = (float) r1;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x02b6;
    L_0x02ac:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r1 = r1 + r2;
        r1 = (float) r1;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x040a;
    L_0x02b6:
        r13.forwardNamePressed = r0;
        goto L_0x040a;
    L_0x02ba:
        r5 = r13.forwardBotPressed;
        if (r5 == 0) goto L_0x034d;
    L_0x02be:
        r5 = r14.getAction();
        if (r5 != r12) goto L_0x02df;
    L_0x02c4:
        r13.forwardBotPressed = r0;
        r13.playSoundEffect(r0);
        r0 = r13.delegate;
        if (r0 == 0) goto L_0x040a;
    L_0x02cd:
        r1 = r13.currentViaBotUser;
        if (r1 == 0) goto L_0x02d4;
    L_0x02d1:
        r1 = r1.username;
        goto L_0x02da;
    L_0x02d4:
        r1 = r13.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.via_bot_name;
    L_0x02da:
        r0.didPressViaBot(r13, r1);
        goto L_0x040a;
    L_0x02df:
        r5 = r14.getAction();
        if (r5 != r3) goto L_0x02e9;
    L_0x02e5:
        r13.forwardBotPressed = r0;
        goto L_0x040a;
    L_0x02e9:
        r3 = r14.getAction();
        if (r3 != r2) goto L_0x040a;
    L_0x02ef:
        r2 = r13.drawForwardedName;
        if (r2 == 0) goto L_0x031d;
    L_0x02f3:
        r2 = r13.forwardedNameLayout;
        r2 = r2[r0];
        if (r2 == 0) goto L_0x031d;
    L_0x02f9:
        r2 = r13.forwardNameX;
        r3 = (float) r2;
        r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r3 < 0) goto L_0x0319;
    L_0x0300:
        r3 = r13.forwardedNameWidth;
        r2 = r2 + r3;
        r2 = (float) r2;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 > 0) goto L_0x0319;
    L_0x0308:
        r1 = r13.forwardNameY;
        r2 = (float) r1;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x0319;
    L_0x030f:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r1 = r1 + r2;
        r1 = (float) r1;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x040a;
    L_0x0319:
        r13.forwardBotPressed = r0;
        goto L_0x040a;
    L_0x031d:
        r2 = r13.nameX;
        r3 = r13.viaNameWidth;
        r5 = (float) r3;
        r5 = r5 + r2;
        r5 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1));
        if (r5 < 0) goto L_0x0349;
    L_0x0327:
        r3 = (float) r3;
        r2 = r2 + r3;
        r3 = r13.viaWidth;
        r3 = (float) r3;
        r2 = r2 + r3;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 > 0) goto L_0x0349;
    L_0x0331:
        r1 = r13.nameY;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r2 = (float) r2;
        r1 = r1 - r2;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 < 0) goto L_0x0349;
    L_0x033d:
        r1 = r13.nameY;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r2 = (float) r2;
        r1 = r1 + r2;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x040a;
    L_0x0349:
        r13.forwardBotPressed = r0;
        goto L_0x040a;
    L_0x034d:
        r5 = r13.replyPressed;
        if (r5 == 0) goto L_0x03bd;
    L_0x0351:
        r5 = r14.getAction();
        if (r5 != r12) goto L_0x0375;
    L_0x0357:
        r13.replyPressed = r0;
        r13.playSoundEffect(r0);
        r0 = r13.delegate;
        if (r0 == 0) goto L_0x040a;
    L_0x0360:
        r0 = r13.currentMessageObject;
        r0 = r0.hasValidReplyMessageObject();
        if (r0 == 0) goto L_0x040a;
    L_0x0368:
        r0 = r13.delegate;
        r1 = r13.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.reply_to_msg_id;
        r0.didPressReplyMessage(r13, r1);
        goto L_0x040a;
    L_0x0375:
        r5 = r14.getAction();
        if (r5 != r3) goto L_0x037f;
    L_0x037b:
        r13.replyPressed = r0;
        goto L_0x040a;
    L_0x037f:
        r3 = r14.getAction();
        if (r3 != r2) goto L_0x040a;
    L_0x0385:
        r2 = r13.currentMessageObject;
        r2 = r2.shouldDrawWithoutBackground();
        if (r2 == 0) goto L_0x0398;
    L_0x038d:
        r2 = r13.replyStartX;
        r3 = r13.replyNameWidth;
        r5 = r13.replyTextWidth;
        r3 = java.lang.Math.max(r3, r5);
        goto L_0x039c;
    L_0x0398:
        r2 = r13.replyStartX;
        r3 = r13.backgroundDrawableRight;
    L_0x039c:
        r2 = r2 + r3;
        r3 = r13.replyStartX;
        r3 = (float) r3;
        r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r3 < 0) goto L_0x03ba;
    L_0x03a4:
        r2 = (float) r2;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 > 0) goto L_0x03ba;
    L_0x03a9:
        r1 = r13.replyStartY;
        r2 = (float) r1;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x03ba;
    L_0x03b0:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r1 = r1 + r2;
        r1 = (float) r1;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x040a;
    L_0x03ba:
        r13.replyPressed = r0;
        goto L_0x040a;
    L_0x03bd:
        r5 = r13.sharePressed;
        if (r5 == 0) goto L_0x040a;
    L_0x03c1:
        r5 = r14.getAction();
        if (r5 != r12) goto L_0x03d4;
    L_0x03c7:
        r13.sharePressed = r0;
        r13.playSoundEffect(r0);
        r0 = r13.delegate;
        if (r0 == 0) goto L_0x0407;
    L_0x03d0:
        r0.didPressShare(r13);
        goto L_0x0407;
    L_0x03d4:
        r5 = r14.getAction();
        if (r5 != r3) goto L_0x03dd;
    L_0x03da:
        r13.sharePressed = r0;
        goto L_0x0407;
    L_0x03dd:
        r3 = r14.getAction();
        if (r3 != r2) goto L_0x0407;
    L_0x03e3:
        r2 = r13.shareStartX;
        r3 = (float) r2;
        r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r3 < 0) goto L_0x0405;
    L_0x03ea:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r2 = r2 + r3;
        r2 = (float) r2;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 > 0) goto L_0x0405;
    L_0x03f4:
        r1 = r13.shareStartY;
        r2 = (float) r1;
        r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x0405;
    L_0x03fb:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r1 = r1 + r2;
        r1 = (float) r1;
        r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x0407;
    L_0x0405:
        r13.sharePressed = r0;
    L_0x0407:
        r13.invalidate();
    L_0x040a:
        return r6;
    L_0x040b:
        r0 = super.onTouchEvent(r14);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void updatePlayingMessageProgress() {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null) {
            int i = 0;
            String formatShortDuration;
            int i2;
            if (this.documentAttachType == 4) {
                if (this.infoLayout == null || !(PhotoViewer.isPlayingMessage(messageObject) || MediaController.getInstance().isGoingToShowMessageObject(this.currentMessageObject))) {
                    AnimatedFileDrawable animation = this.photoImage.getAnimation();
                    if (animation != null) {
                        i = animation.getDurationMs() / 1000;
                        this.currentMessageObject.audioPlayerDuration = i;
                        MessageObject messageObject2 = this.currentMessageObject;
                        Message message = messageObject2.messageOwner;
                        if (message.ttl > 0 && message.destroyTime == 0 && !messageObject2.needDrawBluredPreview() && this.currentMessageObject.isVideo() && animation.hasBitmap()) {
                            this.delegate.didStartVideoStream(this.currentMessageObject);
                        }
                    }
                    if (i == 0) {
                        i = this.currentMessageObject.getDuration();
                    }
                    if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                        float f = (float) i;
                        i = (int) (f - (this.currentMessageObject.audioProgress * f));
                    } else if (animation != null) {
                        if (i != 0) {
                            i -= animation.getCurrentProgressMs() / 1000;
                        }
                        if (this.delegate != null && animation.getCurrentProgressMs() >= 3000) {
                            this.delegate.videoTimerReached();
                        }
                    }
                    if (this.lastTime != i) {
                        formatShortDuration = AndroidUtilities.formatShortDuration(i);
                        this.infoWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(formatShortDuration));
                        this.infoLayout = new StaticLayout(formatShortDuration, Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        this.lastTime = i;
                    }
                }
            } else if (messageObject.isRoundVideo()) {
                Document document = this.currentMessageObject.getDocument();
                for (int i3 = 0; i3 < document.attributes.size(); i3++) {
                    DocumentAttribute documentAttribute = (DocumentAttribute) document.attributes.get(i3);
                    if (documentAttribute instanceof TL_documentAttributeVideo) {
                        i2 = documentAttribute.duration;
                        break;
                    }
                }
                i2 = 0;
                if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                    i2 = Math.max(0, i2 - this.currentMessageObject.audioProgressSec);
                }
                if (this.lastTime != i2) {
                    this.lastTime = i2;
                    String formatLongDuration = AndroidUtilities.formatLongDuration(i2);
                    this.timeWidthAudio = (int) Math.ceil((double) Theme.chat_timePaint.measureText(formatLongDuration));
                    this.durationLayout = new StaticLayout(formatLongDuration, Theme.chat_timePaint, this.timeWidthAudio, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    invalidate();
                }
            } else if (this.documentAttach != null) {
                if (this.useSeekBarWaweform) {
                    if (!this.seekBarWaveform.isDragging()) {
                        this.seekBarWaveform.setProgress(this.currentMessageObject.audioProgress);
                    }
                } else if (!this.seekBar.isDragging()) {
                    this.seekBar.setProgress(this.currentMessageObject.audioProgress);
                    this.seekBar.setBufferedProgress(this.currentMessageObject.bufferedProgress);
                }
                if (this.documentAttachType == 3) {
                    if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                        i = this.currentMessageObject.audioProgressSec;
                    } else {
                        for (i2 = 0; i2 < this.documentAttach.attributes.size(); i2++) {
                            DocumentAttribute documentAttribute2 = (DocumentAttribute) this.documentAttach.attributes.get(i2);
                            if (documentAttribute2 instanceof TL_documentAttributeAudio) {
                                i = documentAttribute2.duration;
                                break;
                            }
                        }
                    }
                    if (this.lastTime != i) {
                        this.lastTime = i;
                        formatShortDuration = AndroidUtilities.formatLongDuration(i);
                        this.timeWidthAudio = (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(formatShortDuration));
                        this.durationLayout = new StaticLayout(formatShortDuration, Theme.chat_audioTimePaint, this.timeWidthAudio, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    }
                } else {
                    i2 = this.currentMessageObject.getDuration();
                    if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                        i = this.currentMessageObject.audioProgressSec;
                    }
                    if (this.lastTime != i) {
                        this.lastTime = i;
                        formatShortDuration = AndroidUtilities.formatShortDuration(i, i2);
                        this.durationLayout = new StaticLayout(formatShortDuration, Theme.chat_audioTimePaint, (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(formatShortDuration)), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    }
                }
                invalidate();
            }
        }
    }

    public void setFullyDraw(boolean z) {
        this.fullyDraw = z;
    }

    public void setVisiblePart(int i, int i2) {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject == null || messageObject.textLayoutBlocks == null) {
            if (this.currentBackgroundDrawable instanceof MessageDrawable) {
                invalidate();
            }
            return;
        }
        i -= this.textY;
        int i3 = 0;
        int i4 = 0;
        while (i3 < this.currentMessageObject.textLayoutBlocks.size() && ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(i3)).textYOffset <= ((float) i)) {
            i4 = i3;
            i3++;
        }
        int i5 = -1;
        int i6 = -1;
        int i7 = 0;
        for (i4 = 
/*
Method generation error in method: org.telegram.ui.Cells.ChatMessageCell.setVisiblePart(int, int):void, dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r2_1 'i4' int) = (r2_0 'i4' int), (r2_3 'i4' int) binds: {(r2_0 'i4' int)=B:4:0x000a, (r2_3 'i4' int)=B:9:0x002c} in method: org.telegram.ui.Cells.ChatMessageCell.setVisiblePart(int, int):void, dex: classes.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:185)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:183)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 19 more

*/

    public static StaticLayout generateStaticLayout(CharSequence charSequence, TextPaint textPaint, int i, int i2, int i3, int i4) {
        CharSequence charSequence2 = charSequence;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(charSequence);
        StaticLayout staticLayout = new StaticLayout(charSequence, textPaint, i2, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int i5 = i;
        int i6 = i3;
        int i7 = 0;
        int i8 = 0;
        while (i7 < i6) {
            staticLayout.getLineDirections(i7);
            if (staticLayout.getLineLeft(i7) != 0.0f || staticLayout.isRtlCharAt(staticLayout.getLineStart(i7)) || staticLayout.isRtlCharAt(staticLayout.getLineEnd(i7))) {
                i5 = i2;
            }
            int lineEnd = staticLayout.getLineEnd(i7);
            if (lineEnd != charSequence.length()) {
                lineEnd = (lineEnd - 1) + i8;
                String str = "\n";
                if (spannableStringBuilder.charAt(lineEnd) == ' ') {
                    spannableStringBuilder.replace(lineEnd, lineEnd + 1, str);
                } else if (spannableStringBuilder.charAt(lineEnd) != 10) {
                    spannableStringBuilder.insert(lineEnd, str);
                    i8++;
                }
                if (i7 == staticLayout.getLineCount() - 1 || i7 == i4 - 1) {
                    break;
                }
                i7++;
            } else {
                break;
            }
        }
        int i9 = i5;
        return StaticLayoutEx.createStaticLayout(spannableStringBuilder, textPaint, i9, Alignment.ALIGN_NORMAL, 1.0f, (float) AndroidUtilities.dp(1.0f), false, TruncateAt.END, i9, i4, true);
    }

    private void didClickedImage() {
        MessageObject messageObject = this.currentMessageObject;
        int i;
        if (messageObject.type == 1 || messageObject.isAnyKindOfSticker()) {
            i = this.buttonState;
            if (i == -1) {
                this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
                return;
            } else if (i == 0) {
                didPressButton(true, false);
                return;
            } else {
                return;
            }
        }
        messageObject = this.currentMessageObject;
        int i2 = messageObject.type;
        if (i2 == 12) {
            this.delegate.didPressUserAvatar(this, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)), this.lastTouchX, this.lastTouchY);
        } else if (i2 == 5) {
            if (this.buttonState != -1) {
                didPressButton(true, false);
            } else if (!MediaController.getInstance().isPlayingMessage(this.currentMessageObject) || MediaController.getInstance().isMessagePaused()) {
                this.delegate.needPlayMessage(this.currentMessageObject);
            } else {
                MediaController.getInstance().lambda$startAudioAgain$5$MediaController(this.currentMessageObject);
            }
        } else if (i2 == 8) {
            i = this.buttonState;
            if (i == -1 || (i == 1 && this.canStreamVideo && this.autoPlayingMedia)) {
                this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
                return;
            }
            i = this.buttonState;
            if (i == 2 || i == 0) {
                didPressButton(true, false);
            }
        } else {
            int i3 = this.documentAttachType;
            if (i3 == 4) {
                if (this.buttonState == -1 || (this.drawVideoImageButton && (this.autoPlayingMedia || (SharedConfig.streamMedia && this.canStreamVideo)))) {
                    this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
                } else if (this.drawVideoImageButton) {
                    didPressButton(true, true);
                } else {
                    i = this.buttonState;
                    if (i == 0 || i == 3) {
                        didPressButton(true, false);
                    }
                }
            } else if (i2 == 4) {
                this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
            } else if (i3 == 1) {
                if (this.buttonState == -1) {
                    this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
                }
            } else if (i3 == 2) {
                if (this.buttonState == -1) {
                    WebPage webPage = messageObject.messageOwner.media.webpage;
                    if (webPage != null) {
                        String str = webPage.embed_url;
                        if (str == null || str.length() == 0) {
                            Browser.openUrl(getContext(), webPage.url);
                        } else {
                            this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.description, webPage.url, webPage.embed_width, webPage.embed_height);
                        }
                    }
                }
            } else if (this.hasInvoicePreview && this.buttonState == -1) {
                this.delegate.didPressImage(this, this.lastTouchX, this.lastTouchY);
            }
        }
    }

    private void updateSecretTimeText(MessageObject messageObject) {
        if (messageObject != null && messageObject.needDrawBluredPreview()) {
            String secretTimeString = messageObject.getSecretTimeString();
            if (secretTimeString != null) {
                this.infoWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(secretTimeString));
                this.infoLayout = new StaticLayout(TextUtils.ellipsize(secretTimeString, Theme.chat_infoPaint, (float) this.infoWidth, TruncateAt.END), Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                invalidate();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0048  */
    private boolean isPhotoDataChanged(org.telegram.messenger.MessageObject r25) {
        /*
        r24 = this;
        r0 = r24;
        r1 = r25;
        r2 = r1.type;
        r3 = 0;
        if (r2 == 0) goto L_0x0156;
    L_0x0009:
        r4 = 14;
        if (r2 != r4) goto L_0x000f;
    L_0x000d:
        goto L_0x0156;
    L_0x000f:
        r4 = 3;
        r5 = 4;
        r6 = 1;
        if (r2 != r5) goto L_0x0123;
    L_0x0014:
        r2 = r0.currentUrl;
        if (r2 != 0) goto L_0x0019;
    L_0x0018:
        return r6;
    L_0x0019:
        r2 = r1.messageOwner;
        r2 = r2.media;
        r2 = r2.geo;
        r8 = r2.lat;
        r13 = r2._long;
        r2 = r25.getDialogId();
        r3 = (int) r2;
        r2 = -1;
        if (r3 != 0) goto L_0x003a;
    L_0x002b:
        r3 = org.telegram.messenger.SharedConfig.mapPreviewType;
        if (r3 != 0) goto L_0x0030;
    L_0x002f:
        goto L_0x003a;
    L_0x0030:
        if (r3 != r6) goto L_0x0035;
    L_0x0032:
        r19 = 4;
        goto L_0x003c;
    L_0x0035:
        if (r3 != r4) goto L_0x003a;
    L_0x0037:
        r19 = 1;
        goto L_0x003c;
    L_0x003a:
        r19 = -1;
    L_0x003c:
        r1 = r1.messageOwner;
        r1 = r1.media;
        r2 = r1 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
        r3 = NUM; // 0x41a80000 float:21.0 double:5.442276803E-315;
        r4 = NUM; // 0x43430000 float:195.0 double:5.575354847E-315;
        if (r2 == 0) goto L_0x00ce;
    L_0x0048:
        r1 = r0.backgroundWidth;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r1 = r1 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = NUM; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r3 = (double) r3;
        r10 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        java.lang.Double.isNaN(r3);
        r15 = r3 / r10;
        r8 = r8 * r10;
        r17 = NUM; // 0xNUM float:0.0 double:180.0;
        r8 = r8 / r17;
        r20 = java.lang.Math.sin(r8);
        r22 = NUM; // 0x3ffNUM float:0.0 double:1.0;
        r20 = r20 + r22;
        r7 = java.lang.Math.sin(r8);
        r22 = r22 - r7;
        r20 = r20 / r22;
        r7 = java.lang.Math.log(r20);
        r7 = r7 * r15;
        r20 = NUM; // 0xNUM float:0.0 double:2.0;
        r7 = r7 / r20;
        java.lang.Double.isNaN(r3);
        r7 = r3 - r7;
        r7 = java.lang.Math.round(r7);
        r5 = NUM; // 0x4124cccd float:10.3 double:5.399795443E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = r5 << 6;
        r10 = (long) r5;
        r7 = r7 - r10;
        r7 = (double) r7;
        r9 = NUM; // 0x3fvar_fb54442d18 float:3.37028055E12 double:1.NUM;
        java.lang.Double.isNaN(r7);
        java.lang.Double.isNaN(r3);
        r7 = r7 - r3;
        r7 = r7 / r15;
        r3 = java.lang.Math.exp(r7);
        r3 = java.lang.Math.atan(r3);
        r3 = r3 * r20;
        r9 = r9 - r3;
        r9 = r9 * r17;
        r3 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        r11 = r9 / r3;
        r10 = r0.currentAccount;
        r1 = (float) r1;
        r3 = org.telegram.messenger.AndroidUtilities.density;
        r1 = r1 / r3;
        r15 = (int) r1;
        r1 = (float) r2;
        r1 = r1 / r3;
        r1 = (int) r1;
        r17 = 0;
        r18 = 15;
        r16 = r1;
        r1 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r10, r11, r13, r15, r16, r17, r18, r19);
        goto L_0x011b;
    L_0x00ce:
        r1 = r1.title;
        r1 = android.text.TextUtils.isEmpty(r1);
        if (r1 != 0) goto L_0x00f8;
    L_0x00d6:
        r1 = r0.backgroundWidth;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r1 = r1 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r7 = r0.currentAccount;
        r1 = (float) r1;
        r3 = org.telegram.messenger.AndroidUtilities.density;
        r1 = r1 / r3;
        r12 = (int) r1;
        r1 = (float) r2;
        r1 = r1 / r3;
        r1 = (int) r1;
        r2 = 1;
        r15 = 15;
        r10 = r13;
        r13 = r1;
        r14 = r2;
        r16 = r19;
        r1 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r7, r8, r10, r12, r13, r14, r15, r16);
        goto L_0x011b;
    L_0x00f8:
        r1 = r0.backgroundWidth;
        r2 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r7 = r0.currentAccount;
        r1 = (float) r1;
        r3 = org.telegram.messenger.AndroidUtilities.density;
        r1 = r1 / r3;
        r12 = (int) r1;
        r1 = (float) r2;
        r1 = r1 / r3;
        r1 = (int) r1;
        r2 = 1;
        r15 = 15;
        r10 = r13;
        r13 = r1;
        r14 = r2;
        r16 = r19;
        r1 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r7, r8, r10, r12, r13, r14, r15, r16);
    L_0x011b:
        r2 = r0.currentUrl;
        r1 = r1.equals(r2);
        r1 = r1 ^ r6;
        return r1;
    L_0x0123:
        r2 = r0.currentPhotoObject;
        if (r2 == 0) goto L_0x0142;
    L_0x0127:
        r2 = r2.location;
        r2 = r2 instanceof org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
        if (r2 == 0) goto L_0x012e;
    L_0x012d:
        goto L_0x0142;
    L_0x012e:
        r1 = r0.currentMessageObject;
        if (r1 == 0) goto L_0x0141;
    L_0x0132:
        r2 = r0.photoNotSet;
        if (r2 == 0) goto L_0x0141;
    L_0x0136:
        r1 = r1.messageOwner;
        r1 = org.telegram.messenger.FileLoader.getPathToMessage(r1);
        r1 = r1.exists();
        return r1;
    L_0x0141:
        return r3;
    L_0x0142:
        r2 = r1.type;
        if (r2 == r6) goto L_0x0155;
    L_0x0146:
        r5 = 5;
        if (r2 == r5) goto L_0x0155;
    L_0x0149:
        if (r2 == r4) goto L_0x0155;
    L_0x014b:
        r4 = 8;
        if (r2 == r4) goto L_0x0155;
    L_0x014f:
        r1 = r25.isAnyKindOfSticker();
        if (r1 == 0) goto L_0x0156;
    L_0x0155:
        r3 = 1;
    L_0x0156:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.isPhotoDataChanged(org.telegram.messenger.MessageObject):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00b4 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00d0  */
    /* JADX WARNING: Missing block: B:66:0x00ac, code skipped:
            if (r8.currentMessageObject.replyMessageObject.isAnyKindOfSticker() == false) goto L_0x00b0;
     */
    private boolean isUserDataChanged() {
        /*
        r8 = this;
        r0 = r8.currentMessageObject;
        r1 = 1;
        if (r0 == 0) goto L_0x0016;
    L_0x0005:
        r2 = r8.hasLinkPreview;
        if (r2 != 0) goto L_0x0016;
    L_0x0009:
        r0 = r0.messageOwner;
        r0 = r0.media;
        if (r0 == 0) goto L_0x0016;
    L_0x000f:
        r0 = r0.webpage;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_webPage;
        if (r0 == 0) goto L_0x0016;
    L_0x0015:
        return r1;
    L_0x0016:
        r0 = r8.currentMessageObject;
        r2 = 0;
        if (r0 == 0) goto L_0x011c;
    L_0x001b:
        r0 = r8.currentUser;
        if (r0 != 0) goto L_0x0025;
    L_0x001f:
        r0 = r8.currentChat;
        if (r0 != 0) goto L_0x0025;
    L_0x0023:
        goto L_0x011c;
    L_0x0025:
        r0 = r8.lastSendState;
        r3 = r8.currentMessageObject;
        r3 = r3.messageOwner;
        r4 = r3.send_state;
        if (r0 == r4) goto L_0x0030;
    L_0x002f:
        return r1;
    L_0x0030:
        r0 = r8.lastDeleteDate;
        r4 = r3.destroyTime;
        if (r0 == r4) goto L_0x0037;
    L_0x0036:
        return r1;
    L_0x0037:
        r0 = r8.lastViewsCount;
        r4 = r3.views;
        if (r0 == r4) goto L_0x003e;
    L_0x003d:
        return r1;
    L_0x003e:
        r0 = r8.lastReactions;
        r3 = r3.reactions;
        if (r0 == r3) goto L_0x0045;
    L_0x0044:
        return r1;
    L_0x0045:
        r8.updateCurrentUserAndChat();
        r0 = r8.isAvatarVisible;
        r3 = 0;
        if (r0 == 0) goto L_0x0063;
    L_0x004d:
        r0 = r8.currentUser;
        if (r0 == 0) goto L_0x0058;
    L_0x0051:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x0058;
    L_0x0055:
        r0 = r0.photo_small;
        goto L_0x0064;
    L_0x0058:
        r0 = r8.currentChat;
        if (r0 == 0) goto L_0x0063;
    L_0x005c:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x0063;
    L_0x0060:
        r0 = r0.photo_small;
        goto L_0x0064;
    L_0x0063:
        r0 = r3;
    L_0x0064:
        r4 = r8.replyTextLayout;
        if (r4 != 0) goto L_0x006f;
    L_0x0068:
        r4 = r8.currentMessageObject;
        r4 = r4.replyMessageObject;
        if (r4 == 0) goto L_0x006f;
    L_0x006e:
        return r1;
    L_0x006f:
        r4 = r8.currentPhoto;
        if (r4 != 0) goto L_0x0075;
    L_0x0073:
        if (r0 != 0) goto L_0x008f;
    L_0x0075:
        r4 = r8.currentPhoto;
        if (r4 == 0) goto L_0x007b;
    L_0x0079:
        if (r0 == 0) goto L_0x008f;
    L_0x007b:
        r4 = r8.currentPhoto;
        if (r4 == 0) goto L_0x0090;
    L_0x007f:
        if (r0 == 0) goto L_0x0090;
    L_0x0081:
        r5 = r4.local_id;
        r6 = r0.local_id;
        if (r5 != r6) goto L_0x008f;
    L_0x0087:
        r4 = r4.volume_id;
        r6 = r0.volume_id;
        r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r0 == 0) goto L_0x0090;
    L_0x008f:
        return r1;
    L_0x0090:
        r0 = r8.replyNameLayout;
        if (r0 == 0) goto L_0x00af;
    L_0x0094:
        r0 = r8.currentMessageObject;
        r0 = r0.replyMessageObject;
        if (r0 == 0) goto L_0x00af;
    L_0x009a:
        r0 = r0.photoThumbs;
        r4 = 40;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r4);
        if (r0 == 0) goto L_0x00af;
    L_0x00a4:
        r4 = r8.currentMessageObject;
        r4 = r4.replyMessageObject;
        r4 = r4.isAnyKindOfSticker();
        if (r4 != 0) goto L_0x00af;
    L_0x00ae:
        goto L_0x00b0;
    L_0x00af:
        r0 = r3;
    L_0x00b0:
        r4 = r8.currentReplyPhoto;
        if (r4 != 0) goto L_0x00b7;
    L_0x00b4:
        if (r0 == 0) goto L_0x00b7;
    L_0x00b6:
        return r1;
    L_0x00b7:
        r0 = r8.drawName;
        if (r0 == 0) goto L_0x00d6;
    L_0x00bb:
        r0 = r8.isChat;
        if (r0 == 0) goto L_0x00d6;
    L_0x00bf:
        r0 = r8.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 != 0) goto L_0x00d6;
    L_0x00c7:
        r0 = r8.currentUser;
        if (r0 == 0) goto L_0x00d0;
    L_0x00cb:
        r3 = org.telegram.messenger.UserObject.getUserName(r0);
        goto L_0x00d6;
    L_0x00d0:
        r0 = r8.currentChat;
        if (r0 == 0) goto L_0x00d6;
    L_0x00d4:
        r3 = r0.title;
    L_0x00d6:
        r0 = r8.currentNameString;
        if (r0 != 0) goto L_0x00dc;
    L_0x00da:
        if (r3 != 0) goto L_0x00ee;
    L_0x00dc:
        r0 = r8.currentNameString;
        if (r0 == 0) goto L_0x00e2;
    L_0x00e0:
        if (r3 == 0) goto L_0x00ee;
    L_0x00e2:
        r0 = r8.currentNameString;
        if (r0 == 0) goto L_0x00ef;
    L_0x00e6:
        if (r3 == 0) goto L_0x00ef;
    L_0x00e8:
        r0 = r0.equals(r3);
        if (r0 != 0) goto L_0x00ef;
    L_0x00ee:
        return r1;
    L_0x00ef:
        r0 = r8.drawForwardedName;
        if (r0 == 0) goto L_0x011c;
    L_0x00f3:
        r0 = r8.currentMessageObject;
        r0 = r0.needDrawForwarded();
        if (r0 == 0) goto L_0x011c;
    L_0x00fb:
        r0 = r8.currentMessageObject;
        r0 = r0.getForwardedName();
        r3 = r8.currentForwardNameString;
        if (r3 != 0) goto L_0x0107;
    L_0x0105:
        if (r0 != 0) goto L_0x011b;
    L_0x0107:
        r3 = r8.currentForwardNameString;
        if (r3 == 0) goto L_0x010d;
    L_0x010b:
        if (r0 == 0) goto L_0x011b;
    L_0x010d:
        r3 = r8.currentForwardNameString;
        if (r3 == 0) goto L_0x011a;
    L_0x0111:
        if (r0 == 0) goto L_0x011a;
    L_0x0113:
        r0 = r3.equals(r0);
        if (r0 != 0) goto L_0x011a;
    L_0x0119:
        goto L_0x011b;
    L_0x011a:
        r1 = 0;
    L_0x011b:
        return r1;
    L_0x011c:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.isUserDataChanged():boolean");
    }

    public ImageReceiver getPhotoImage() {
        return this.photoImage;
    }

    public int getNoSoundIconCenterX() {
        return this.noSoundCenterX;
    }

    public int getForwardNameCenterX() {
        User user = this.currentUser;
        if (user == null || user.id != 0) {
            return this.forwardNameX + this.forwardNameCenterX;
        }
        return (int) this.avatarImage.getCenterX();
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.animationRunning) {
            CheckBoxBase checkBoxBase = this.checkBox;
            if (checkBoxBase != null) {
                checkBoxBase.onDetachedFromWindow();
            }
            checkBoxBase = this.photoCheckBox;
            if (checkBoxBase != null) {
                checkBoxBase.onDetachedFromWindow();
            }
            this.attachedToWindow = false;
            this.radialProgress.onDetachedFromWindow();
            this.videoRadialProgress.onDetachedFromWindow();
            this.avatarImage.onDetachedFromWindow();
            this.replyImageReceiver.onDetachedFromWindow();
            this.locationImageReceiver.onDetachedFromWindow();
            this.photoImage.onDetachedFromWindow();
            if (!(!this.addedForTest || this.currentUrl == null || this.currentWebFile == null)) {
                ImageLoader.getInstance().removeTestWebFile(this.currentUrl);
                this.addedForTest = false;
            }
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        MessageObject messageObject = this.messageObjectToSet;
        if (messageObject != null) {
            setMessageContent(messageObject, this.groupedMessagesToSet, this.bottomNearToSet, this.topNearToSet);
            this.messageObjectToSet = null;
            this.groupedMessagesToSet = null;
        }
        CheckBoxBase checkBoxBase = this.checkBox;
        if (checkBoxBase != null) {
            checkBoxBase.onAttachedToWindow();
        }
        checkBoxBase = this.photoCheckBox;
        if (checkBoxBase != null) {
            checkBoxBase.onAttachedToWindow();
        }
        this.attachedToWindow = true;
        float f = 0.0f;
        setTranslationX(0.0f);
        this.radialProgress.onAttachedToWindow();
        this.videoRadialProgress.onAttachedToWindow();
        this.avatarImage.onAttachedToWindow();
        this.avatarImage.setParentView((View) getParent());
        this.replyImageReceiver.onAttachedToWindow();
        this.locationImageReceiver.onAttachedToWindow();
        if (!this.photoImage.onAttachedToWindow()) {
            updateButtonState(false, false, false);
        } else if (this.drawPhotoImage) {
            updateButtonState(false, false, false);
        }
        MessageObject messageObject2 = this.currentMessageObject;
        if (messageObject2 != null && (messageObject2.isRoundVideo() || this.currentMessageObject.isVideo())) {
            checkVideoPlayback(true);
        }
        if (this.documentAttachType == 4 && this.autoPlayingMedia) {
            this.animatingNoSoundPlaying = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
            if (!this.animatingNoSoundPlaying) {
                f = 1.0f;
            }
            this.animatingNoSoundProgress = f;
            this.animatingNoSound = 0;
            return;
        }
        this.animatingNoSoundPlaying = false;
        this.animatingNoSoundProgress = 0.0f;
        int i = this.documentAttachType;
        if ((i == 4 || i == 2) && this.drawVideoSize) {
            f = 1.0f;
        }
        this.animatingDrawVideoImageButtonProgress = f;
    }

    /* JADX WARNING: Removed duplicated region for block: B:637:0x0c6d A:{Catch:{ Exception -> 0x0c8b }} */
    /* JADX WARNING: Removed duplicated region for block: B:1069:0x1749  */
    /* JADX WARNING: Removed duplicated region for block: B:1068:0x1747  */
    /* JADX WARNING: Removed duplicated region for block: B:909:0x1296  */
    /* JADX WARNING: Removed duplicated region for block: B:913:0x12b1  */
    /* JADX WARNING: Removed duplicated region for block: B:912:0x12aa  */
    /* JADX WARNING: Removed duplicated region for block: B:935:0x12f9  */
    /* JADX WARNING: Removed duplicated region for block: B:934:0x12f2  */
    /* JADX WARNING: Removed duplicated region for block: B:941:0x130f  */
    /* JADX WARNING: Removed duplicated region for block: B:938:0x1305  */
    /* JADX WARNING: Removed duplicated region for block: B:944:0x1316  */
    /* JADX WARNING: Removed duplicated region for block: B:980:0x13ea  */
    /* JADX WARNING: Removed duplicated region for block: B:976:0x13b9  */
    /* JADX WARNING: Removed duplicated region for block: B:990:0x147c  */
    /* JADX WARNING: Removed duplicated region for block: B:989:0x1453  */
    /* JADX WARNING: Removed duplicated region for block: B:1079:0x17a7  */
    /* JADX WARNING: Removed duplicated region for block: B:871:0x1210  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x068b  */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x06b5  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0721  */
    /* JADX WARNING: Removed duplicated region for block: B:338:0x06f9  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x074d  */
    /* JADX WARNING: Removed duplicated region for block: B:415:0x0807  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x0758  */
    /* JADX WARNING: Removed duplicated region for block: B:430:0x086e  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x0862  */
    /* JADX WARNING: Removed duplicated region for block: B:436:0x0889  */
    /* JADX WARNING: Removed duplicated region for block: B:435:0x0885  */
    /* JADX WARNING: Removed duplicated region for block: B:465:0x091d  */
    /* JADX WARNING: Removed duplicated region for block: B:442:0x08a2 A:{SYNTHETIC, Splitter:B:442:0x08a2} */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x0604  */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x068b  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0695  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x06b5  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x06dc  */
    /* JADX WARNING: Removed duplicated region for block: B:338:0x06f9  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0721  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x074d  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x0758  */
    /* JADX WARNING: Removed duplicated region for block: B:415:0x0807  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x0862  */
    /* JADX WARNING: Removed duplicated region for block: B:430:0x086e  */
    /* JADX WARNING: Removed duplicated region for block: B:435:0x0885  */
    /* JADX WARNING: Removed duplicated region for block: B:436:0x0889  */
    /* JADX WARNING: Removed duplicated region for block: B:442:0x08a2 A:{SYNTHETIC, Splitter:B:442:0x08a2} */
    /* JADX WARNING: Removed duplicated region for block: B:465:0x091d  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:1213:0x1CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1217:0x1CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1216:0x1c1d  */
    /* JADX WARNING: Removed duplicated region for block: B:1384:0x209d  */
    /* JADX WARNING: Removed duplicated region for block: B:1374:0x2077  */
    /* JADX WARNING: Removed duplicated region for block: B:1373:0x2072  */
    /* JADX WARNING: Removed duplicated region for block: B:1384:0x209d  */
    /* JADX WARNING: Removed duplicated region for block: B:1392:0x20d6  */
    /* JADX WARNING: Removed duplicated region for block: B:1384:0x209d  */
    /* JADX WARNING: Removed duplicated region for block: B:1392:0x20d6  */
    /* JADX WARNING: Removed duplicated region for block: B:1676:0x28ef  */
    /* JADX WARNING: Removed duplicated region for block: B:1665:0x28d7  */
    /* JADX WARNING: Removed duplicated region for block: B:1680:0x2914  */
    /* JADX WARNING: Removed duplicated region for block: B:1679:0x28fe  */
    /* JADX WARNING: Removed duplicated region for block: B:1598:0x267b  */
    /* JADX WARNING: Removed duplicated region for block: B:1558:0x2443  */
    /* JADX WARNING: Removed duplicated region for block: B:1665:0x28d7  */
    /* JADX WARNING: Removed duplicated region for block: B:1676:0x28ef  */
    /* JADX WARNING: Removed duplicated region for block: B:1679:0x28fe  */
    /* JADX WARNING: Removed duplicated region for block: B:1680:0x2914  */
    /* JADX WARNING: Removed duplicated region for block: B:2076:0x317b  */
    /* JADX WARNING: Removed duplicated region for block: B:1925:0x2e4d  */
    /* JADX WARNING: Removed duplicated region for block: B:2132:0x32d5  */
    /* JADX WARNING: Removed duplicated region for block: B:2093:0x31ce A:{SYNTHETIC, Splitter:B:2093:0x31ce} */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:1901:0x2dc7  */
    /* JADX WARNING: Removed duplicated region for block: B:1894:0x2d85  */
    /* JADX WARNING: Removed duplicated region for block: B:1904:0x2dd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1908:0x2e05  */
    /* JADX WARNING: Removed duplicated region for block: B:1907:0x2e02  */
    /* JADX WARNING: Removed duplicated region for block: B:1911:0x2e10  */
    /* JADX WARNING: Removed duplicated region for block: B:1916:0x2e27  */
    /* JADX WARNING: Removed duplicated region for block: B:1914:0x2e17  */
    /* JADX WARNING: Removed duplicated region for block: B:1925:0x2e4d  */
    /* JADX WARNING: Removed duplicated region for block: B:2076:0x317b  */
    /* JADX WARNING: Removed duplicated region for block: B:2093:0x31ce A:{SYNTHETIC, Splitter:B:2093:0x31ce} */
    /* JADX WARNING: Removed duplicated region for block: B:2132:0x32d5  */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:1843:0x2cbf  */
    /* JADX WARNING: Removed duplicated region for block: B:1842:0x2cbc  */
    /* JADX WARNING: Removed duplicated region for block: B:1853:0x2cd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1868:0x2d0a  */
    /* JADX WARNING: Removed duplicated region for block: B:1894:0x2d85  */
    /* JADX WARNING: Removed duplicated region for block: B:1901:0x2dc7  */
    /* JADX WARNING: Removed duplicated region for block: B:1904:0x2dd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1907:0x2e02  */
    /* JADX WARNING: Removed duplicated region for block: B:1908:0x2e05  */
    /* JADX WARNING: Removed duplicated region for block: B:1911:0x2e10  */
    /* JADX WARNING: Removed duplicated region for block: B:1914:0x2e17  */
    /* JADX WARNING: Removed duplicated region for block: B:1916:0x2e27  */
    /* JADX WARNING: Removed duplicated region for block: B:2076:0x317b  */
    /* JADX WARNING: Removed duplicated region for block: B:1925:0x2e4d  */
    /* JADX WARNING: Removed duplicated region for block: B:2132:0x32d5  */
    /* JADX WARNING: Removed duplicated region for block: B:2093:0x31ce A:{SYNTHETIC, Splitter:B:2093:0x31ce} */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:1814:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1813:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1832:0x2ca0  */
    /* JADX WARNING: Removed duplicated region for block: B:1842:0x2cbc  */
    /* JADX WARNING: Removed duplicated region for block: B:1843:0x2cbf  */
    /* JADX WARNING: Removed duplicated region for block: B:1853:0x2cd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1868:0x2d0a  */
    /* JADX WARNING: Removed duplicated region for block: B:1901:0x2dc7  */
    /* JADX WARNING: Removed duplicated region for block: B:1894:0x2d85  */
    /* JADX WARNING: Removed duplicated region for block: B:1904:0x2dd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1908:0x2e05  */
    /* JADX WARNING: Removed duplicated region for block: B:1907:0x2e02  */
    /* JADX WARNING: Removed duplicated region for block: B:1911:0x2e10  */
    /* JADX WARNING: Removed duplicated region for block: B:1916:0x2e27  */
    /* JADX WARNING: Removed duplicated region for block: B:1914:0x2e17  */
    /* JADX WARNING: Removed duplicated region for block: B:1925:0x2e4d  */
    /* JADX WARNING: Removed duplicated region for block: B:2076:0x317b  */
    /* JADX WARNING: Removed duplicated region for block: B:2093:0x31ce A:{SYNTHETIC, Splitter:B:2093:0x31ce} */
    /* JADX WARNING: Removed duplicated region for block: B:2132:0x32d5  */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:1791:0x2bf3  */
    /* JADX WARNING: Removed duplicated region for block: B:1779:0x2bc2  */
    /* JADX WARNING: Removed duplicated region for block: B:1802:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1801:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1813:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1814:0x2CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1832:0x2ca0  */
    /* JADX WARNING: Removed duplicated region for block: B:1843:0x2cbf  */
    /* JADX WARNING: Removed duplicated region for block: B:1842:0x2cbc  */
    /* JADX WARNING: Removed duplicated region for block: B:1853:0x2cd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1868:0x2d0a  */
    /* JADX WARNING: Removed duplicated region for block: B:1894:0x2d85  */
    /* JADX WARNING: Removed duplicated region for block: B:1901:0x2dc7  */
    /* JADX WARNING: Removed duplicated region for block: B:1904:0x2dd5  */
    /* JADX WARNING: Removed duplicated region for block: B:1907:0x2e02  */
    /* JADX WARNING: Removed duplicated region for block: B:1908:0x2e05  */
    /* JADX WARNING: Removed duplicated region for block: B:1911:0x2e10  */
    /* JADX WARNING: Removed duplicated region for block: B:1914:0x2e17  */
    /* JADX WARNING: Removed duplicated region for block: B:1916:0x2e27  */
    /* JADX WARNING: Removed duplicated region for block: B:2076:0x317b  */
    /* JADX WARNING: Removed duplicated region for block: B:1925:0x2e4d  */
    /* JADX WARNING: Removed duplicated region for block: B:2132:0x32d5  */
    /* JADX WARNING: Removed duplicated region for block: B:2093:0x31ce A:{SYNTHETIC, Splitter:B:2093:0x31ce} */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:2494:0x3c0a  */
    /* JADX WARNING: Removed duplicated region for block: B:2468:0x3ac0  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:1438:0x21cc  */
    /* JADX WARNING: Removed duplicated region for block: B:1437:0x21c6  */
    /* JADX WARNING: Removed duplicated region for block: B:1543:0x2413  */
    /* JADX WARNING: Removed duplicated region for block: B:1454:0x220a  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02bc  */
    /* JADX WARNING: Removed duplicated region for block: B:1109:0x18e7  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02bc  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:1109:0x18e7  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x015a  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02bc  */
    /* JADX WARNING: Removed duplicated region for block: B:1109:0x18e7  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:467:0x0925  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x0a83  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:2134:0x32da  */
    /* JADX WARNING: Removed duplicated region for block: B:2147:0x333b  */
    /* JADX WARNING: Removed duplicated region for block: B:2154:0x3373  */
    /* JADX WARNING: Removed duplicated region for block: B:2153:0x3348  */
    /* JADX WARNING: Removed duplicated region for block: B:2157:0x338d  */
    /* JADX WARNING: Removed duplicated region for block: B:2165:0x339c A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:2198:0x33f4  */
    /* JADX WARNING: Removed duplicated region for block: B:2178:0x33b7  */
    /* JADX WARNING: Removed duplicated region for block: B:2209:0x344f  */
    /* JADX WARNING: Removed duplicated region for block: B:2201:0x33ff  */
    /* JADX WARNING: Removed duplicated region for block: B:2213:0x349d  */
    /* JADX WARNING: Removed duplicated region for block: B:2212:0x3454  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:2403:0x3944 A:{Catch:{ Exception -> 0x39b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:2407:0x398f A:{Catch:{ Exception -> 0x39b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:2418:0x39c2  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0a93 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0cad  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0b3c  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:658:0x0cb5  */
    /* JADX WARNING: Removed duplicated region for block: B:665:0x0cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:856:0x11c5  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0cd2  */
    /* JADX WARNING: Removed duplicated region for block: B:882:0x1231  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:1501:0x2321  */
    /* JADX WARNING: Removed duplicated region for block: B:1494:0x22ee  */
    /* JADX WARNING: Removed duplicated region for block: B:1504:0x2326  */
    /* JADX WARNING: Removed duplicated region for block: B:1513:0x2355  */
    /* JADX WARNING: Removed duplicated region for block: B:1520:0x2383  */
    /* JADX WARNING: Removed duplicated region for block: B:1524:0x239e  */
    /* JADX WARNING: Removed duplicated region for block: B:1523:0x2391  */
    /* JADX WARNING: Removed duplicated region for block: B:1535:0x23d7  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Removed duplicated region for block: B:1494:0x22ee  */
    /* JADX WARNING: Removed duplicated region for block: B:1501:0x2321  */
    /* JADX WARNING: Removed duplicated region for block: B:1504:0x2326  */
    /* JADX WARNING: Removed duplicated region for block: B:1513:0x2355  */
    /* JADX WARNING: Removed duplicated region for block: B:1520:0x2383  */
    /* JADX WARNING: Removed duplicated region for block: B:1523:0x2391  */
    /* JADX WARNING: Removed duplicated region for block: B:1524:0x239e  */
    /* JADX WARNING: Removed duplicated region for block: B:1535:0x23d7  */
    /* JADX WARNING: Removed duplicated region for block: B:2305:0x36bb  */
    /* JADX WARNING: Removed duplicated region for block: B:2325:0x3731  */
    /* JADX WARNING: Removed duplicated region for block: B:2318:0x3704  */
    /* JADX WARNING: Removed duplicated region for block: B:2328:0x3736  */
    /* JADX WARNING: Removed duplicated region for block: B:2335:0x3769  */
    /* JADX WARNING: Removed duplicated region for block: B:2331:0x3743  */
    /* JADX WARNING: Removed duplicated region for block: B:2348:0x37ad  */
    /* JADX WARNING: Removed duplicated region for block: B:2359:0x380d A:{SYNTHETIC, Splitter:B:2359:0x380d} */
    /* JADX WARNING: Removed duplicated region for block: B:2376:0x3899  */
    /* JADX WARNING: Removed duplicated region for block: B:2383:0x38ba  */
    /* JADX WARNING: Removed duplicated region for block: B:2424:0x39e4  */
    /* JADX WARNING: Removed duplicated region for block: B:2427:0x39f5  */
    /* JADX WARNING: Removed duplicated region for block: B:2512:0x3d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:2518:0x3d39  */
    /* JADX WARNING: Removed duplicated region for block: B:2517:0x3d2f  */
    /* JADX WARNING: Removed duplicated region for block: B:2529:0x3d58  */
    /* JADX WARNING: Removed duplicated region for block: B:2534:0x3d71  */
    /* JADX WARNING: Removed duplicated region for block: B:2537:0x3d80  */
    /* JADX WARNING: Removed duplicated region for block: B:2547:0x3dbd  */
    /* JADX WARNING: Removed duplicated region for block: B:2540:0x3d8b  */
    /* JADX WARNING: Removed duplicated region for block: B:2550:0x3dc8  */
    /* JADX WARNING: Missing block: B:904:0x128b, code skipped:
            if (r4 != 8) goto L_0x128f;
     */
    /* JADX WARNING: Missing block: B:916:0x12b9, code skipped:
            if (r4 != 0) goto L_0x12bb;
     */
    /* JADX WARNING: Missing block: B:1633:0x27ee, code skipped:
            if (r0 < (r1.timeWidth + org.telegram.messenger.AndroidUtilities.dp((float) ((r60.isOutOwner() ? 20 : 0) + 20)))) goto L_0x27f0;
     */
    /* JADX WARNING: Missing block: B:1854:0x2cdd, code skipped:
            if ("m".equals(r4.type) == false) goto L_0x2cdf;
     */
    /* JADX WARNING: Missing block: B:2150:0x3340, code skipped:
            if (r2 != 5) goto L_0x3388;
     */
    /* JADX WARNING: Missing block: B:2432:0x3a07, code skipped:
            if (r0.results.isEmpty() == false) goto L_0x3a09;
     */
    private void setMessageContent(org.telegram.messenger.MessageObject r60, org.telegram.messenger.MessageObject.GroupedMessages r61, boolean r62, boolean r63) {
        /*
        r59 = this;
        r1 = r59;
        r14 = r60;
        r0 = r61;
        r2 = r62;
        r3 = r63;
        r4 = r60.checkLayout();
        r15 = 0;
        if (r4 != 0) goto L_0x001d;
    L_0x0011:
        r4 = r1.currentPosition;
        if (r4 == 0) goto L_0x001f;
    L_0x0015:
        r4 = r1.lastHeight;
        r5 = org.telegram.messenger.AndroidUtilities.displaySize;
        r5 = r5.y;
        if (r4 == r5) goto L_0x001f;
    L_0x001d:
        r1.currentMessageObject = r15;
    L_0x001f:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r1.lastHeight = r4;
        r4 = r1.currentMessageObject;
        r13 = 1;
        r12 = 0;
        if (r4 == 0) goto L_0x0039;
    L_0x002b:
        r4 = r4.getId();
        r5 = r60.getId();
        if (r4 == r5) goto L_0x0036;
    L_0x0035:
        goto L_0x0039;
    L_0x0036:
        r16 = 0;
        goto L_0x003b;
    L_0x0039:
        r16 = 1;
    L_0x003b:
        r4 = r1.currentMessageObject;
        if (r4 != r14) goto L_0x0046;
    L_0x003f:
        r4 = r14.forceUpdate;
        if (r4 == 0) goto L_0x0044;
    L_0x0043:
        goto L_0x0046;
    L_0x0044:
        r4 = 0;
        goto L_0x0047;
    L_0x0046:
        r4 = 1;
    L_0x0047:
        r5 = r1.currentMessageObject;
        r11 = 3;
        if (r5 == 0) goto L_0x0060;
    L_0x004c:
        r5 = r5.getId();
        r6 = r60.getId();
        if (r5 != r6) goto L_0x0060;
    L_0x0056:
        r5 = r1.lastSendState;
        if (r5 != r11) goto L_0x0060;
    L_0x005a:
        r5 = r60.isSent();
        if (r5 != 0) goto L_0x006e;
    L_0x0060:
        r5 = r1.currentMessageObject;
        if (r5 != r14) goto L_0x0071;
    L_0x0064:
        r5 = r59.isUserDataChanged();
        if (r5 != 0) goto L_0x006e;
    L_0x006a:
        r5 = r1.photoNotSet;
        if (r5 == 0) goto L_0x0071;
    L_0x006e:
        r17 = 1;
        goto L_0x0073;
    L_0x0071:
        r17 = 0;
    L_0x0073:
        r5 = r1.currentMessagesGroup;
        if (r0 == r5) goto L_0x0079;
    L_0x0077:
        r5 = 1;
        goto L_0x007a;
    L_0x0079:
        r5 = 0;
    L_0x007a:
        r10 = 0;
        if (r4 != 0) goto L_0x00cf;
    L_0x007d:
        r6 = r14.type;
        r7 = 17;
        if (r6 != r7) goto L_0x00cf;
    L_0x0083:
        r6 = r14.messageOwner;
        r6 = r6.media;
        r7 = r6 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaPoll;
        if (r7 == 0) goto L_0x0096;
    L_0x008b:
        r6 = (org.telegram.tgnet.TLRPC.TL_messageMediaPoll) r6;
        r7 = r6.results;
        r8 = r7.results;
        r6 = r6.poll;
        r7 = r7.total_voters;
        goto L_0x0099;
    L_0x0096:
        r6 = r15;
        r8 = r6;
        r7 = 0;
    L_0x0099:
        if (r8 == 0) goto L_0x00a5;
    L_0x009b:
        r9 = r1.lastPollResults;
        if (r9 == 0) goto L_0x00a5;
    L_0x009f:
        r9 = r1.lastPollResultsVoters;
        if (r7 == r9) goto L_0x00a5;
    L_0x00a3:
        r7 = 1;
        goto L_0x00a6;
    L_0x00a5:
        r7 = 0;
    L_0x00a6:
        if (r7 != 0) goto L_0x00ad;
    L_0x00a8:
        r9 = r1.lastPollResults;
        if (r8 == r9) goto L_0x00ad;
    L_0x00ac:
        r7 = 1;
    L_0x00ad:
        if (r7 != 0) goto L_0x00ba;
    L_0x00af:
        r8 = r1.lastPoll;
        if (r8 == r6) goto L_0x00ba;
    L_0x00b3:
        r8 = r8.closed;
        r6 = r6.closed;
        if (r8 == r6) goto L_0x00ba;
    L_0x00b9:
        r7 = 1;
    L_0x00ba:
        if (r7 == 0) goto L_0x00d0;
    L_0x00bc:
        r6 = r1.attachedToWindow;
        if (r6 == 0) goto L_0x00d0;
    L_0x00c0:
        r1.pollAnimationProgressTime = r10;
        r6 = r1.pollVoted;
        if (r6 == 0) goto L_0x00d0;
    L_0x00c6:
        r6 = r60.isVoted();
        if (r6 != 0) goto L_0x00d0;
    L_0x00cc:
        r1.pollUnvoteInProgress = r13;
        goto L_0x00d0;
    L_0x00cf:
        r7 = 0;
    L_0x00d0:
        if (r5 != 0) goto L_0x00f1;
    L_0x00d2:
        if (r0 == 0) goto L_0x00f1;
    L_0x00d4:
        r5 = r0.messages;
        r5 = r5.size();
        if (r5 <= r13) goto L_0x00e9;
    L_0x00dc:
        r5 = r1.currentMessagesGroup;
        r5 = r5.positions;
        r6 = r1.currentMessageObject;
        r5 = r5.get(r6);
        r5 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r5;
        goto L_0x00ea;
    L_0x00e9:
        r5 = r15;
    L_0x00ea:
        r6 = r1.currentPosition;
        if (r5 == r6) goto L_0x00f0;
    L_0x00ee:
        r5 = 1;
        goto L_0x00f1;
    L_0x00f0:
        r5 = 0;
    L_0x00f1:
        r9 = 2;
        if (r4 != 0) goto L_0x010c;
    L_0x00f4:
        if (r17 != 0) goto L_0x010c;
    L_0x00f6:
        if (r5 != 0) goto L_0x010c;
    L_0x00f8:
        if (r7 != 0) goto L_0x010c;
    L_0x00fa:
        r5 = r59.isPhotoDataChanged(r60);
        if (r5 != 0) goto L_0x010c;
    L_0x0100:
        r5 = r1.pinnedBottom;
        if (r5 != r2) goto L_0x010c;
    L_0x0104:
        r5 = r1.pinnedTop;
        if (r5 == r3) goto L_0x0109;
    L_0x0108:
        goto L_0x010c;
    L_0x0109:
        r15 = 0;
        goto L_0x3dc3;
    L_0x010c:
        r1.pinnedBottom = r2;
        r1.pinnedTop = r3;
        r1.currentMessageObject = r14;
        r1.currentMessagesGroup = r0;
        r0 = -2;
        r1.lastTime = r0;
        r1.isHighlightedAnimated = r12;
        r8 = -1;
        r1.widthBeforeNewTimeLine = r8;
        r0 = r1.currentMessagesGroup;
        if (r0 == 0) goto L_0x013d;
    L_0x0120:
        r0 = r0.posArray;
        r0 = r0.size();
        if (r0 <= r13) goto L_0x013d;
    L_0x0128:
        r0 = r1.currentMessagesGroup;
        r0 = r0.positions;
        r2 = r1.currentMessageObject;
        r0 = r0.get(r2);
        r0 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r0;
        r1.currentPosition = r0;
        r0 = r1.currentPosition;
        if (r0 != 0) goto L_0x0141;
    L_0x013a:
        r1.currentMessagesGroup = r15;
        goto L_0x0141;
    L_0x013d:
        r1.currentMessagesGroup = r15;
        r1.currentPosition = r15;
    L_0x0141:
        r0 = r1.pinnedTop;
        if (r0 == 0) goto L_0x0151;
    L_0x0145:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x014f;
    L_0x0149:
        r0 = r0.flags;
        r0 = r0 & 4;
        if (r0 == 0) goto L_0x0151;
    L_0x014f:
        r0 = 1;
        goto L_0x0152;
    L_0x0151:
        r0 = 0;
    L_0x0152:
        r1.drawPinnedTop = r0;
        r0 = r1.pinnedBottom;
        r6 = 8;
        if (r0 == 0) goto L_0x0165;
    L_0x015a:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x0163;
    L_0x015e:
        r0 = r0.flags;
        r0 = r0 & r6;
        if (r0 == 0) goto L_0x0165;
    L_0x0163:
        r0 = 1;
        goto L_0x0166;
    L_0x0165:
        r0 = 0;
    L_0x0166:
        r1.drawPinnedBottom = r0;
        r0 = r1.photoImage;
        r0.setCrossfadeWithOldImage(r12);
        r0 = r14.messageOwner;
        r2 = r0.send_state;
        r1.lastSendState = r2;
        r2 = r0.destroyTime;
        r1.lastDeleteDate = r2;
        r0 = r0.views;
        r1.lastViewsCount = r0;
        r1.isPressed = r12;
        r1.gamePreviewPressed = r12;
        r1.sharePressed = r12;
        r1.isCheckPressed = r13;
        r1.hasNewLineForTime = r12;
        r0 = r1.isChat;
        if (r0 == 0) goto L_0x019f;
    L_0x0189:
        r0 = r60.isOutOwner();
        if (r0 != 0) goto L_0x019f;
    L_0x018f:
        r0 = r60.needDrawAvatar();
        if (r0 == 0) goto L_0x019f;
    L_0x0195:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x019d;
    L_0x0199:
        r0 = r0.edge;
        if (r0 == 0) goto L_0x019f;
    L_0x019d:
        r0 = 1;
        goto L_0x01a0;
    L_0x019f:
        r0 = 0;
    L_0x01a0:
        r1.isAvatarVisible = r0;
        r1.wasLayout = r12;
        r1.drwaShareGoIcon = r12;
        r1.groupPhotoInvisible = r12;
        r1.animatingDrawVideoImageButton = r12;
        r1.drawVideoSize = r12;
        r1.canStreamVideo = r12;
        r1.animatingNoSound = r12;
        r0 = r59.checkNeedDrawShareButton(r60);
        r1.drawShareButton = r0;
        r1.replyNameLayout = r15;
        r1.adminLayout = r15;
        r1.checkOnlyButtonPressed = r12;
        r1.replyTextLayout = r15;
        r1.hasEmbed = r12;
        r1.autoPlayingMedia = r12;
        r1.replyNameWidth = r12;
        r1.replyTextWidth = r12;
        r1.viaWidth = r12;
        r1.viaNameWidth = r12;
        r1.addedCaptionHeight = r12;
        r1.currentReplyPhoto = r15;
        r1.currentUser = r15;
        r1.currentChat = r15;
        r1.currentViaBotUser = r15;
        r1.instantViewLayout = r15;
        r1.drawNameLayout = r12;
        r0 = r1.scheduledInvalidate;
        if (r0 == 0) goto L_0x01e3;
    L_0x01dc:
        r0 = r1.invalidateRunnable;
        org.telegram.messenger.AndroidUtilities.cancelRunOnUIThread(r0);
        r1.scheduledInvalidate = r12;
    L_0x01e3:
        r1.resetPressedLink(r8);
        r14.forceUpdate = r12;
        r1.drawPhotoImage = r12;
        r1.drawPhotoCheckBox = r12;
        r1.hasLinkPreview = r12;
        r1.hasOldCaptionPreview = r12;
        r1.hasGamePreview = r12;
        r1.hasInvoicePreview = r12;
        r1.instantButtonPressed = r12;
        r1.instantPressed = r12;
        if (r7 != 0) goto L_0x020e;
    L_0x01fa:
        r0 = android.os.Build.VERSION.SDK_INT;
        r2 = 21;
        if (r0 < r2) goto L_0x020e;
    L_0x0200:
        r0 = r1.selectorDrawable;
        if (r0 == 0) goto L_0x020e;
    L_0x0204:
        r0.setVisible(r12, r12);
        r0 = r1.selectorDrawable;
        r2 = android.util.StateSet.NOTHING;
        r0.setState(r2);
    L_0x020e:
        r1.linkPreviewPressed = r12;
        r1.buttonPressed = r12;
        r1.miniButtonPressed = r12;
        r1.pressedBotButton = r8;
        r1.pressedVoteButton = r8;
        r1.linkPreviewHeight = r12;
        r1.mediaOffsetY = r12;
        r1.documentAttachType = r12;
        r1.documentAttach = r15;
        r1.descriptionLayout = r15;
        r1.titleLayout = r15;
        r1.videoInfoLayout = r15;
        r1.photosCountLayout = r15;
        r1.siteNameLayout = r15;
        r1.authorLayout = r15;
        r1.captionLayout = r15;
        r1.captionOffsetX = r12;
        r1.currentCaption = r15;
        r1.docTitleLayout = r15;
        r1.drawImageButton = r12;
        r1.drawVideoImageButton = r12;
        r1.currentPhotoObject = r15;
        r1.photoParentObject = r15;
        r1.currentPhotoObjectThumb = r15;
        r1.currentPhotoFilter = r15;
        r1.infoLayout = r15;
        r1.cancelLoading = r12;
        r1.buttonState = r8;
        r1.miniButtonState = r8;
        r1.hasMiniProgress = r12;
        r0 = r1.addedForTest;
        if (r0 == 0) goto L_0x025f;
    L_0x024e:
        r0 = r1.currentUrl;
        if (r0 == 0) goto L_0x025f;
    L_0x0252:
        r0 = r1.currentWebFile;
        if (r0 == 0) goto L_0x025f;
    L_0x0256:
        r0 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r1.currentUrl;
        r0.removeTestWebFile(r2);
    L_0x025f:
        r1.addedForTest = r12;
        r1.currentUrl = r15;
        r1.currentWebFile = r15;
        r1.photoNotSet = r12;
        r1.drawBackground = r13;
        r1.drawName = r12;
        r1.useSeekBarWaweform = r12;
        r1.drawInstantView = r12;
        r1.drawInstantViewType = r12;
        r1.drawForwardedName = r12;
        r0 = r1.photoImage;
        r0.setSideClip(r10);
        r1.imageBackgroundColor = r12;
        r1.imageBackgroundSideColor = r12;
        r1.mediaBackground = r12;
        r0 = r1.photoImage;
        r7 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.setAlpha(r7);
        if (r4 != 0) goto L_0x0289;
    L_0x0287:
        if (r17 == 0) goto L_0x028e;
    L_0x0289:
        r0 = r1.pollButtons;
        r0.clear();
    L_0x028e:
        r1.availableTimeWidth = r12;
        r0 = r14.messageOwner;
        r0 = r0.reactions;
        r1.lastReactions = r0;
        r0 = r1.photoImage;
        r0.setForceLoading(r12);
        r0 = r1.photoImage;
        r0.setNeedsQualityThumb(r12);
        r0 = r1.photoImage;
        r0.setShouldGenerateQualityThumb(r12);
        r0 = r1.photoImage;
        r0.setAllowDecodeSingleFrame(r12);
        r0 = r1.photoImage;
        r2 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0.setRoundRadius(r2);
        r0 = r1.photoImage;
        r0.setColorFilter(r15);
        if (r4 == 0) goto L_0x02c2;
    L_0x02bc:
        r1.firstVisibleBlockNum = r12;
        r1.lastVisibleBlockNum = r12;
        r1.needNewVisiblePart = r13;
    L_0x02c2:
        r0 = r14.type;
        r18 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r2 = 6;
        r19 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r20 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        if (r0 != 0) goto L_0x18e7;
    L_0x02cd:
        r1.drawForwardedName = r13;
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x0312;
    L_0x02d5:
        r0 = r1.isChat;
        if (r0 == 0) goto L_0x02f3;
    L_0x02d9:
        r0 = r60.isOutOwner();
        if (r0 != 0) goto L_0x02f3;
    L_0x02df:
        r0 = r60.needDrawAvatar();
        if (r0 == 0) goto L_0x02f3;
    L_0x02e5:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = NUM; // 0x42var_ float:122.0 double:5.54977537E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        r1.drawName = r13;
        goto L_0x035a;
    L_0x02f3:
        r0 = r14.messageOwner;
        r0 = r0.to_id;
        r0 = r0.channel_id;
        if (r0 == 0) goto L_0x0303;
    L_0x02fb:
        r0 = r60.isOutOwner();
        if (r0 != 0) goto L_0x0303;
    L_0x0301:
        r0 = 1;
        goto L_0x0304;
    L_0x0303:
        r0 = 0;
    L_0x0304:
        r1.drawName = r0;
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = NUM; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        goto L_0x035a;
    L_0x0312:
        r0 = r1.isChat;
        if (r0 == 0) goto L_0x0336;
    L_0x0316:
        r0 = r60.isOutOwner();
        if (r0 != 0) goto L_0x0336;
    L_0x031c:
        r0 = r60.needDrawAvatar();
        if (r0 == 0) goto L_0x0336;
    L_0x0322:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r0.x;
        r0 = r0.y;
        r0 = java.lang.Math.min(r4, r0);
        r4 = NUM; // 0x42var_ float:122.0 double:5.54977537E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        r1.drawName = r13;
        goto L_0x035a;
    L_0x0336:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r0.x;
        r0 = r0.y;
        r0 = java.lang.Math.min(r4, r0);
        r4 = NUM; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        r4 = r14.messageOwner;
        r4 = r4.to_id;
        r4 = r4.channel_id;
        if (r4 == 0) goto L_0x0357;
    L_0x034f:
        r4 = r60.isOutOwner();
        if (r4 != 0) goto L_0x0357;
    L_0x0355:
        r4 = 1;
        goto L_0x0358;
    L_0x0357:
        r4 = 0;
    L_0x0358:
        r1.drawName = r4;
    L_0x035a:
        r4 = r0;
        r1.availableTimeWidth = r4;
        r0 = r60.isRoundVideo();
        if (r0 == 0) goto L_0x038f;
    L_0x0363:
        r0 = r1.availableTimeWidth;
        r6 = (double) r0;
        r0 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r5 = "00:00";
        r0 = r0.measureText(r5);
        r5 = r4;
        r3 = (double) r0;
        r3 = java.lang.Math.ceil(r3);
        r0 = r60.isOutOwner();
        if (r0 == 0) goto L_0x037c;
    L_0x037a:
        r0 = 0;
        goto L_0x0382;
    L_0x037c:
        r0 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
    L_0x0382:
        r10 = (double) r0;
        java.lang.Double.isNaN(r10);
        r3 = r3 + r10;
        java.lang.Double.isNaN(r6);
        r6 = r6 - r3;
        r0 = (int) r6;
        r1.availableTimeWidth = r0;
        goto L_0x0390;
    L_0x038f:
        r5 = r4;
    L_0x0390:
        r59.measureTime(r60);
        r0 = r1.timeWidth;
        r3 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 + r3;
        r3 = r60.isOutOwner();
        if (r3 == 0) goto L_0x03a9;
    L_0x03a2:
        r3 = NUM; // 0x41a40000 float:20.5 double:5.44098164E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 + r3;
    L_0x03a9:
        r11 = r0;
        r0 = r14.messageOwner;
        r0 = r0.media;
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGame;
        if (r3 == 0) goto L_0x03ba;
    L_0x03b2:
        r0 = r0.game;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_game;
        if (r0 == 0) goto L_0x03ba;
    L_0x03b8:
        r0 = 1;
        goto L_0x03bb;
    L_0x03ba:
        r0 = 0;
    L_0x03bb:
        r1.hasGamePreview = r0;
        r0 = r14.messageOwner;
        r0 = r0.media;
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
        r1.hasInvoicePreview = r3;
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r3 == 0) goto L_0x03d1;
    L_0x03c9:
        r0 = r0.webpage;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_webPage;
        if (r0 == 0) goto L_0x03d1;
    L_0x03cf:
        r0 = 1;
        goto L_0x03d2;
    L_0x03d1:
        r0 = 0;
    L_0x03d2:
        r1.hasLinkPreview = r0;
        r0 = r1.hasLinkPreview;
        if (r0 == 0) goto L_0x03e4;
    L_0x03d8:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r0 = r0.cached_page;
        if (r0 == 0) goto L_0x03e4;
    L_0x03e2:
        r0 = 1;
        goto L_0x03e5;
    L_0x03e4:
        r0 = 0;
    L_0x03e5:
        r1.drawInstantView = r0;
        r0 = r1.hasLinkPreview;
        if (r0 == 0) goto L_0x0401;
    L_0x03eb:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r0 = r0.embed_url;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 != 0) goto L_0x0401;
    L_0x03f9:
        r0 = r60.isGif();
        if (r0 != 0) goto L_0x0401;
    L_0x03ff:
        r0 = 1;
        goto L_0x0402;
    L_0x0401:
        r0 = 0;
    L_0x0402:
        r1.hasEmbed = r0;
        r0 = r1.hasLinkPreview;
        if (r0 == 0) goto L_0x0411;
    L_0x0408:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r0 = r0.site_name;
        goto L_0x0412;
    L_0x0411:
        r0 = r15;
    L_0x0412:
        r3 = r1.hasLinkPreview;
        if (r3 == 0) goto L_0x041f;
    L_0x0416:
        r3 = r14.messageOwner;
        r3 = r3.media;
        r3 = r3.webpage;
        r3 = r3.type;
        goto L_0x0420;
    L_0x041f:
        r3 = r15;
    L_0x0420:
        r4 = r1.drawInstantView;
        if (r4 != 0) goto L_0x053e;
    L_0x0424:
        r0 = "telegram_channel";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x0432;
    L_0x042c:
        r1.drawInstantView = r13;
        r1.drawInstantViewType = r13;
        goto L_0x05ed;
    L_0x0432:
        r0 = "telegram_megagroup";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x0440;
    L_0x043a:
        r1.drawInstantView = r13;
        r1.drawInstantViewType = r9;
        goto L_0x05ed;
    L_0x0440:
        r0 = "telegram_message";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x044f;
    L_0x0448:
        r1.drawInstantView = r13;
        r4 = 3;
        r1.drawInstantViewType = r4;
        goto L_0x05ed;
    L_0x044f:
        r0 = "telegram_theme";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x0481;
    L_0x0457:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r0 = r0.documents;
        r4 = r0.size();
        r6 = 0;
    L_0x0464:
        if (r6 >= r4) goto L_0x05ed;
    L_0x0466:
        r7 = r0.get(r6);
        r7 = (org.telegram.tgnet.TLRPC.Document) r7;
        r10 = r7.mime_type;
        r8 = "application/x-tgtheme-android";
        r8 = r8.equals(r10);
        if (r8 == 0) goto L_0x047d;
    L_0x0476:
        r1.drawInstantView = r13;
        r0 = 7;
        r1.drawInstantViewType = r0;
        goto L_0x05ee;
    L_0x047d:
        r6 = r6 + 1;
        r8 = -1;
        goto L_0x0464;
    L_0x0481:
        r0 = "telegram_background";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x05ed;
    L_0x0489:
        r1.drawInstantView = r13;
        r1.drawInstantViewType = r2;
        r0 = r14.messageOwner;	 Catch:{ Exception -> 0x05ed }
        r0 = r0.media;	 Catch:{ Exception -> 0x05ed }
        r0 = r0.webpage;	 Catch:{ Exception -> 0x05ed }
        r0 = r0.url;	 Catch:{ Exception -> 0x05ed }
        r0 = android.net.Uri.parse(r0);	 Catch:{ Exception -> 0x05ed }
        r4 = "intensity";
        r4 = r0.getQueryParameter(r4);	 Catch:{ Exception -> 0x05ed }
        r4 = org.telegram.messenger.Utilities.parseInt(r4);	 Catch:{ Exception -> 0x05ed }
        r4 = r4.intValue();	 Catch:{ Exception -> 0x05ed }
        r6 = "bg_color";
        r6 = r0.getQueryParameter(r6);	 Catch:{ Exception -> 0x05ed }
        r7 = android.text.TextUtils.isEmpty(r6);	 Catch:{ Exception -> 0x05ed }
        if (r7 == 0) goto L_0x04c9;
    L_0x04b3:
        r7 = r60.getDocument();	 Catch:{ Exception -> 0x05ed }
        if (r7 == 0) goto L_0x04c5;
    L_0x04b9:
        r8 = "image/png";
        r7 = r7.mime_type;	 Catch:{ Exception -> 0x05ed }
        r7 = r8.equals(r7);	 Catch:{ Exception -> 0x05ed }
        if (r7 == 0) goto L_0x04c5;
    L_0x04c3:
        r6 = "ffffff";
    L_0x04c5:
        if (r4 != 0) goto L_0x04c9;
    L_0x04c7:
        r4 = 50;
    L_0x04c9:
        if (r6 == 0) goto L_0x04fb;
    L_0x04cb:
        r0 = 16;
        r0 = java.lang.Integer.parseInt(r6, r0);	 Catch:{ Exception -> 0x05ed }
        r6 = -16777216; // 0xfffffffffvar_ float:-1.7014118E38 double:NaN;
        r0 = r0 | r6;
        r1.imageBackgroundColor = r0;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.imageBackgroundColor;	 Catch:{ Exception -> 0x05ed }
        r0 = org.telegram.messenger.AndroidUtilities.getPatternSideColor(r0);	 Catch:{ Exception -> 0x05ed }
        r1.imageBackgroundSideColor = r0;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.photoImage;	 Catch:{ Exception -> 0x05ed }
        r6 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Exception -> 0x05ed }
        r7 = r1.imageBackgroundColor;	 Catch:{ Exception -> 0x05ed }
        r7 = org.telegram.messenger.AndroidUtilities.getPatternColor(r7);	 Catch:{ Exception -> 0x05ed }
        r8 = android.graphics.PorterDuff.Mode.SRC_IN;	 Catch:{ Exception -> 0x05ed }
        r6.<init>(r7, r8);	 Catch:{ Exception -> 0x05ed }
        r0.setColorFilter(r6);	 Catch:{ Exception -> 0x05ed }
        r0 = r1.photoImage;	 Catch:{ Exception -> 0x05ed }
        r4 = (float) r4;	 Catch:{ Exception -> 0x05ed }
        r6 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r4 = r4 / r6;
        r0.setAlpha(r4);	 Catch:{ Exception -> 0x05ed }
        goto L_0x05ed;
    L_0x04fb:
        r0 = r0.getLastPathSegment();	 Catch:{ Exception -> 0x05ed }
        if (r0 == 0) goto L_0x05ed;
    L_0x0501:
        r4 = r0.length();	 Catch:{ Exception -> 0x05ed }
        if (r4 != r2) goto L_0x05ed;
    L_0x0507:
        r4 = 16;
        r0 = java.lang.Integer.parseInt(r0, r4);	 Catch:{ Exception -> 0x05ed }
        r4 = -16777216; // 0xfffffffffvar_ float:-1.7014118E38 double:NaN;
        r0 = r0 | r4;
        r1.imageBackgroundColor = r0;	 Catch:{ Exception -> 0x05ed }
        r0 = new org.telegram.tgnet.TLRPC$TL_photoSizeEmpty;	 Catch:{ Exception -> 0x05ed }
        r0.<init>();	 Catch:{ Exception -> 0x05ed }
        r1.currentPhotoObject = r0;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.currentPhotoObject;	 Catch:{ Exception -> 0x05ed }
        r4 = "s";
        r0.type = r4;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.currentPhotoObject;	 Catch:{ Exception -> 0x05ed }
        r4 = NUM; // 0x43340000 float:180.0 double:5.570497984E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x05ed }
        r0.w = r4;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.currentPhotoObject;	 Catch:{ Exception -> 0x05ed }
        r4 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x05ed }
        r0.h = r4;	 Catch:{ Exception -> 0x05ed }
        r0 = r1.currentPhotoObject;	 Catch:{ Exception -> 0x05ed }
        r4 = new org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;	 Catch:{ Exception -> 0x05ed }
        r4.<init>();	 Catch:{ Exception -> 0x05ed }
        r0.location = r4;	 Catch:{ Exception -> 0x05ed }
        goto L_0x05ed;
    L_0x053e:
        if (r0 == 0) goto L_0x05ed;
    L_0x0540:
        r0 = r0.toLowerCase();
        r4 = "instagram";
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x055c;
    L_0x054c:
        r4 = "twitter";
        r0 = r0.equals(r4);
        if (r0 != 0) goto L_0x055c;
    L_0x0554:
        r0 = "telegram_album";
        r0 = r0.equals(r3);
        if (r0 == 0) goto L_0x05ed;
    L_0x055c:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r4 = r0.cached_page;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_page;
        if (r4 == 0) goto L_0x05ed;
    L_0x0568:
        r4 = r0.photo;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_photo;
        if (r4 != 0) goto L_0x0576;
    L_0x056e:
        r0 = r0.document;
        r0 = org.telegram.messenger.MessageObject.isVideoDocument(r0);
        if (r0 == 0) goto L_0x05ed;
    L_0x0576:
        r1.drawInstantView = r12;
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        r0 = r0.cached_page;
        r0 = r0.blocks;
        r4 = 0;
        r6 = 1;
    L_0x0584:
        r7 = r0.size();
        if (r4 >= r7) goto L_0x05ac;
    L_0x058a:
        r7 = r0.get(r4);
        r7 = (org.telegram.tgnet.TLRPC.PageBlock) r7;
        r8 = r7 instanceof org.telegram.tgnet.TLRPC.TL_pageBlockSlideshow;
        if (r8 == 0) goto L_0x059d;
    L_0x0594:
        r7 = (org.telegram.tgnet.TLRPC.TL_pageBlockSlideshow) r7;
        r6 = r7.items;
        r6 = r6.size();
        goto L_0x05a9;
    L_0x059d:
        r8 = r7 instanceof org.telegram.tgnet.TLRPC.TL_pageBlockCollage;
        if (r8 == 0) goto L_0x05a9;
    L_0x05a1:
        r7 = (org.telegram.tgnet.TLRPC.TL_pageBlockCollage) r7;
        r6 = r7.items;
        r6 = r6.size();
    L_0x05a9:
        r4 = r4 + 1;
        goto L_0x0584;
    L_0x05ac:
        r0 = NUM; // 0x7f0e0758 float:1.887885E38 double:1.0531630855E-314;
        r4 = new java.lang.Object[r9];
        r7 = java.lang.Integer.valueOf(r13);
        r4[r12] = r7;
        r6 = java.lang.Integer.valueOf(r6);
        r4[r13] = r6;
        r6 = "Of";
        r0 = org.telegram.messenger.LocaleController.formatString(r6, r0, r4);
        r4 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r4 = r4.measureText(r0);
        r6 = (double) r4;
        r6 = java.lang.Math.ceil(r6);
        r4 = (int) r6;
        r1.photosCountWidth = r4;
        r4 = new android.text.StaticLayout;
        r26 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r6 = r1.photosCountWidth;
        r28 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r29 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r30 = 0;
        r31 = 0;
        r24 = r4;
        r25 = r0;
        r27 = r6;
        r24.<init>(r25, r26, r27, r28, r29, r30, r31);
        r1.photosCountLayout = r4;
        r7 = r15;
        r0 = 1;
        goto L_0x05ef;
    L_0x05ed:
        r7 = r15;
    L_0x05ee:
        r0 = 0;
    L_0x05ef:
        r1.backgroundWidth = r5;
        r4 = r1.hasLinkPreview;
        if (r4 != 0) goto L_0x062b;
    L_0x05f5:
        r4 = r1.hasGamePreview;
        if (r4 != 0) goto L_0x062b;
    L_0x05f9:
        r4 = r1.hasInvoicePreview;
        if (r4 != 0) goto L_0x062b;
    L_0x05fd:
        r4 = r14.lastLineWidth;
        r6 = r5 - r4;
        if (r6 >= r11) goto L_0x0604;
    L_0x0603:
        goto L_0x062b;
    L_0x0604:
        r6 = r1.backgroundWidth;
        r4 = r6 - r4;
        if (r4 < 0) goto L_0x0618;
    L_0x060a:
        if (r4 > r11) goto L_0x0618;
    L_0x060c:
        r6 = r6 + r11;
        r6 = r6 - r4;
        r4 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r6 = r6 + r4;
        r1.backgroundWidth = r6;
        goto L_0x064d;
    L_0x0618:
        r4 = r1.backgroundWidth;
        r6 = r14.lastLineWidth;
        r6 = r6 + r11;
        r4 = java.lang.Math.max(r4, r6);
        r6 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r1.backgroundWidth = r4;
        goto L_0x064d;
    L_0x062b:
        r4 = r1.backgroundWidth;
        r6 = r14.lastLineWidth;
        r4 = java.lang.Math.max(r4, r6);
        r6 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r1.backgroundWidth = r4;
        r4 = r1.backgroundWidth;
        r6 = r1.timeWidth;
        r8 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6 = r6 + r8;
        r4 = java.lang.Math.max(r4, r6);
        r1.backgroundWidth = r4;
    L_0x064d:
        r4 = r1.backgroundWidth;
        r6 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r1.availableTimeWidth = r4;
        r4 = r60.isRoundVideo();
        if (r4 == 0) goto L_0x068b;
    L_0x065e:
        r4 = r1.availableTimeWidth;
        r9 = (double) r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r6 = "00:00";
        r4 = r4.measureText(r6);
        r12 = (double) r4;
        r12 = java.lang.Math.ceil(r12);
        r4 = r60.isOutOwner();
        if (r4 == 0) goto L_0x0677;
    L_0x0674:
        r8 = r3;
        r4 = 0;
        goto L_0x067e;
    L_0x0677:
        r4 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r8 = r3;
    L_0x067e:
        r2 = (double) r4;
        java.lang.Double.isNaN(r2);
        r12 = r12 + r2;
        java.lang.Double.isNaN(r9);
        r9 = r9 - r12;
        r2 = (int) r9;
        r1.availableTimeWidth = r2;
        goto L_0x068c;
    L_0x068b:
        r8 = r3;
    L_0x068c:
        r59.setMessageObjectInternal(r60);
        r2 = r14.textWidth;
        r3 = r1.hasGamePreview;
        if (r3 != 0) goto L_0x069c;
    L_0x0695:
        r3 = r1.hasInvoicePreview;
        if (r3 == 0) goto L_0x069a;
    L_0x0699:
        goto L_0x069c;
    L_0x069a:
        r12 = 0;
        goto L_0x06a0;
    L_0x069c:
        r12 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x06a0:
        r2 = r2 + r12;
        r1.backgroundWidth = r2;
        r2 = r14.textHeight;
        r3 = NUM; // 0x419CLASSNAME float:19.5 double:5.43839131E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r3 = r1.namesOffset;
        r2 = r2 + r3;
        r1.totalHeight = r2;
        r2 = r1.drawPinnedTop;
        if (r2 == 0) goto L_0x06be;
    L_0x06b5:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 - r4;
        r1.namesOffset = r3;
    L_0x06be:
        r2 = r1.backgroundWidth;
        r3 = r1.nameWidth;
        r2 = java.lang.Math.max(r2, r3);
        r3 = r1.forwardedNameWidth;
        r2 = java.lang.Math.max(r2, r3);
        r3 = r1.replyNameWidth;
        r2 = java.lang.Math.max(r2, r3);
        r3 = r1.replyTextWidth;
        r2 = java.lang.Math.max(r2, r3);
        r3 = r1.hasLinkPreview;
        if (r3 != 0) goto L_0x06f3;
    L_0x06dc:
        r3 = r1.hasGamePreview;
        if (r3 != 0) goto L_0x06f3;
    L_0x06e0:
        r3 = r1.hasInvoicePreview;
        if (r3 == 0) goto L_0x06e5;
    L_0x06e4:
        goto L_0x06f3;
    L_0x06e5:
        r0 = r1.photoImage;
        r0.setImageBitmap(r15);
        r1.calcBackgroundWidth(r5, r11, r2);
        r13 = 0;
        r15 = 1;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x2175;
    L_0x06f3:
        r3 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r3 == 0) goto L_0x0721;
    L_0x06f9:
        r3 = r1.isChat;
        if (r3 == 0) goto L_0x0716;
    L_0x06fd:
        r3 = r60.needDrawAvatar();
        if (r3 == 0) goto L_0x0716;
    L_0x0703:
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 != 0) goto L_0x0716;
    L_0x070b:
        r3 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = NUM; // 0x43040000 float:132.0 double:5.554956023E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x0748;
    L_0x0716:
        r3 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = NUM; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x0748;
    L_0x0721:
        r3 = r1.isChat;
        if (r3 == 0) goto L_0x073e;
    L_0x0725:
        r3 = r60.needDrawAvatar();
        if (r3 == 0) goto L_0x073e;
    L_0x072b:
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 != 0) goto L_0x073e;
    L_0x0733:
        r3 = org.telegram.messenger.AndroidUtilities.displaySize;
        r3 = r3.x;
        r4 = NUM; // 0x43040000 float:132.0 double:5.554956023E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x0748;
    L_0x073e:
        r3 = org.telegram.messenger.AndroidUtilities.displaySize;
        r3 = r3.x;
        r4 = NUM; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x0748:
        r3 = r3 - r4;
        r4 = r1.drawShareButton;
        if (r4 == 0) goto L_0x0754;
    L_0x074d:
        r4 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
    L_0x0754:
        r4 = r1.hasLinkPreview;
        if (r4 == 0) goto L_0x0807;
    L_0x0758:
        r4 = r14.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = (org.telegram.tgnet.TLRPC.TL_webPage) r4;
        r9 = r4.site_name;
        r10 = r1.drawInstantViewType;
        r6 = 6;
        if (r10 == r6) goto L_0x076d;
    L_0x0767:
        r12 = 7;
        if (r10 == r12) goto L_0x076d;
    L_0x076a:
        r10 = r4.title;
        goto L_0x076e;
    L_0x076d:
        r10 = r15;
    L_0x076e:
        r12 = r1.drawInstantViewType;
        if (r12 == r6) goto L_0x0778;
    L_0x0772:
        r13 = 7;
        if (r12 == r13) goto L_0x0778;
    L_0x0775:
        r12 = r4.author;
        goto L_0x0779;
    L_0x0778:
        r12 = r15;
    L_0x0779:
        r13 = r1.drawInstantViewType;
        if (r13 == r6) goto L_0x0783;
    L_0x077d:
        r6 = 7;
        if (r13 == r6) goto L_0x0783;
    L_0x0780:
        r6 = r4.description;
        goto L_0x0784;
    L_0x0783:
        r6 = r15;
    L_0x0784:
        r13 = r4.photo;
        r15 = r1.drawInstantViewType;
        r29 = r3;
        r3 = 7;
        if (r15 != r3) goto L_0x078f;
    L_0x078d:
        r15 = r7;
        goto L_0x0792;
    L_0x078f:
        r3 = r4.document;
        r15 = r3;
    L_0x0792:
        r3 = r4.type;
        r4 = r4.duration;
        if (r9 == 0) goto L_0x07b9;
    L_0x0798:
        if (r13 == 0) goto L_0x07b9;
    L_0x079a:
        r7 = r9.toLowerCase();
        r30 = r4;
        r4 = "instagram";
        r4 = r7.equals(r4);
        if (r4 == 0) goto L_0x07bb;
    L_0x07a8:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r7 = 3;
        r4 = r4 / r7;
        r7 = r1.currentMessageObject;
        r7 = r7.textWidth;
        r4 = java.lang.Math.max(r4, r7);
        r29 = r4;
        goto L_0x07bb;
    L_0x07b9:
        r30 = r4;
    L_0x07bb:
        r4 = "app";
        r4 = r4.equals(r3);
        if (r4 != 0) goto L_0x07d6;
    L_0x07c3:
        r4 = "profile";
        r4 = r4.equals(r3);
        if (r4 != 0) goto L_0x07d6;
    L_0x07cb:
        r4 = "article";
        r4 = r4.equals(r3);
        if (r4 == 0) goto L_0x07d4;
    L_0x07d3:
        goto L_0x07d6;
    L_0x07d4:
        r4 = 0;
        goto L_0x07d7;
    L_0x07d6:
        r4 = 1;
    L_0x07d7:
        if (r0 != 0) goto L_0x07e3;
    L_0x07d9:
        r7 = r1.drawInstantView;
        if (r7 != 0) goto L_0x07e3;
    L_0x07dd:
        if (r15 != 0) goto L_0x07e3;
    L_0x07df:
        if (r4 == 0) goto L_0x07e3;
    L_0x07e1:
        r7 = 1;
        goto L_0x07e4;
    L_0x07e3:
        r7 = 0;
    L_0x07e4:
        if (r0 != 0) goto L_0x07fa;
    L_0x07e6:
        r0 = r1.drawInstantView;
        if (r0 != 0) goto L_0x07fa;
    L_0x07ea:
        if (r15 != 0) goto L_0x07fa;
    L_0x07ec:
        if (r6 == 0) goto L_0x07fa;
    L_0x07ee:
        if (r3 == 0) goto L_0x07fa;
    L_0x07f0:
        if (r4 == 0) goto L_0x07fa;
    L_0x07f2:
        r0 = r1.currentMessageObject;
        r0 = r0.photoThumbs;
        if (r0 == 0) goto L_0x07fa;
    L_0x07f8:
        r0 = 1;
        goto L_0x07fb;
    L_0x07fa:
        r0 = 0;
    L_0x07fb:
        r1.isSmallImage = r0;
        r40 = r30;
        r4 = 0;
        r58 = r6;
        r6 = r3;
        r3 = r15;
        r15 = r58;
        goto L_0x085b;
    L_0x0807:
        r29 = r3;
        r0 = r1.hasInvoicePreview;
        if (r0 == 0) goto L_0x0834;
    L_0x080d:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r3 = r0;
        r3 = (org.telegram.tgnet.TLRPC.TL_messageMediaInvoice) r3;
        r9 = r0.title;
        r0 = r3.photo;
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r3 == 0) goto L_0x0823;
    L_0x081c:
        r0 = org.telegram.messenger.WebFile.createWithWebDocument(r0);
        r15 = r0;
        r3 = 0;
        goto L_0x0825;
    L_0x0823:
        r3 = 0;
        r15 = 0;
    L_0x0825:
        r1.isSmallImage = r3;
        r3 = "invoice";
        r6 = r3;
        r4 = r15;
        r3 = 0;
        r7 = 0;
        r10 = 0;
        r12 = 0;
        r13 = 0;
        r15 = 0;
        r40 = 0;
        goto L_0x085b;
    L_0x0834:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.game;
        r9 = r0.title;
        r3 = r14.messageText;
        r3 = android.text.TextUtils.isEmpty(r3);
        if (r3 == 0) goto L_0x0848;
    L_0x0844:
        r3 = r0.description;
        r15 = r3;
        goto L_0x0849;
    L_0x0848:
        r15 = 0;
    L_0x0849:
        r3 = r0.photo;
        r0 = r0.document;
        r4 = 0;
        r1.isSmallImage = r4;
        r4 = "game";
        r13 = r3;
        r6 = r4;
        r4 = 0;
        r7 = 0;
        r10 = 0;
        r12 = 0;
        r40 = 0;
        r3 = r0;
    L_0x085b:
        r0 = r1.drawInstantViewType;
        r30 = r6;
        r6 = 6;
        if (r0 != r6) goto L_0x086e;
    L_0x0862:
        r6 = r30;
        r0 = NUM; // 0x7f0e029e float:1.8876396E38 double:1.0531624877E-314;
        r8 = "ChatBackground";
        r9 = org.telegram.messenger.LocaleController.getString(r8, r0);
        goto L_0x0881;
    L_0x086e:
        r6 = r30;
        r0 = "telegram_theme";
        r0 = r0.equals(r8);
        if (r0 == 0) goto L_0x0881;
    L_0x0878:
        r0 = NUM; // 0x7f0e0311 float:1.887663E38 double:1.0531625445E-314;
        r8 = "ColorTheme";
        r9 = org.telegram.messenger.LocaleController.getString(r8, r0);
    L_0x0881:
        r0 = r1.hasInvoicePreview;
        if (r0 == 0) goto L_0x0889;
    L_0x0885:
        r41 = r4;
        r8 = 0;
        goto L_0x0890;
    L_0x0889:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r8 = r0;
        r41 = r4;
    L_0x0890:
        r4 = r29 - r8;
        r0 = r1.currentMessageObject;
        r29 = r6;
        r6 = r0.photoThumbs;
        if (r6 != 0) goto L_0x08a0;
    L_0x089a:
        if (r13 == 0) goto L_0x08a0;
    L_0x089c:
        r6 = 1;
        r0.generateThumbs(r6);
    L_0x08a0:
        if (r9 == 0) goto L_0x091d;
    L_0x08a2:
        r0 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0912 }
        r0 = r0.measureText(r9);	 Catch:{ Exception -> 0x0912 }
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0 = r0 + r6;
        r42 = r5;
        r5 = (double) r0;
        r5 = java.lang.Math.ceil(r5);	 Catch:{ Exception -> 0x0910 }
        r0 = (int) r5;	 Catch:{ Exception -> 0x0910 }
        r5 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x0910 }
        r32 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0910 }
        r33 = java.lang.Math.min(r0, r4);	 Catch:{ Exception -> 0x0910 }
        r34 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0910 }
        r35 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r36 = 0;
        r37 = 0;
        r30 = r5;
        r31 = r9;
        r30.<init>(r31, r32, r33, r34, r35, r36, r37);	 Catch:{ Exception -> 0x0910 }
        r1.siteNameLayout = r5;	 Catch:{ Exception -> 0x0910 }
        r0 = r1.siteNameLayout;	 Catch:{ Exception -> 0x0910 }
        r5 = 0;
        r0 = r0.getLineLeft(r5);	 Catch:{ Exception -> 0x0910 }
        r22 = 0;
        r0 = (r0 > r22 ? 1 : (r0 == r22 ? 0 : -1));
        if (r0 == 0) goto L_0x08db;
    L_0x08d9:
        r0 = 1;
        goto L_0x08dc;
    L_0x08db:
        r0 = 0;
    L_0x08dc:
        r1.siteNameRtl = r0;	 Catch:{ Exception -> 0x090e }
        r0 = r1.siteNameLayout;	 Catch:{ Exception -> 0x090e }
        r5 = r1.siteNameLayout;	 Catch:{ Exception -> 0x090e }
        r5 = r5.getLineCount();	 Catch:{ Exception -> 0x090e }
        r6 = 1;
        r5 = r5 - r6;
        r0 = r0.getLineBottom(r5);	 Catch:{ Exception -> 0x090e }
        r5 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x090e }
        r5 = r5 + r0;
        r1.linkPreviewHeight = r5;	 Catch:{ Exception -> 0x090e }
        r5 = r1.totalHeight;	 Catch:{ Exception -> 0x090e }
        r5 = r5 + r0;
        r1.totalHeight = r5;	 Catch:{ Exception -> 0x090e }
        r5 = 0;
        r6 = r0 + 0;
        r0 = r1.siteNameLayout;	 Catch:{ Exception -> 0x090c }
        r0 = r0.getWidth();	 Catch:{ Exception -> 0x090c }
        r1.siteNameWidth = r0;	 Catch:{ Exception -> 0x090c }
        r0 = r0 + r8;
        r2 = java.lang.Math.max(r2, r0);	 Catch:{ Exception -> 0x090c }
        r0 = java.lang.Math.max(r5, r0);	 Catch:{ Exception -> 0x090c }
        r5 = r0;
        goto L_0x0923;
    L_0x090c:
        r0 = move-exception;
        goto L_0x0918;
    L_0x090e:
        r0 = move-exception;
        goto L_0x0917;
    L_0x0910:
        r0 = move-exception;
        goto L_0x0915;
    L_0x0912:
        r0 = move-exception;
        r42 = r5;
    L_0x0915:
        r22 = 0;
    L_0x0917:
        r6 = 0;
    L_0x0918:
        org.telegram.messenger.FileLog.e(r0);
        r5 = 0;
        goto L_0x0923;
    L_0x091d:
        r42 = r5;
        r22 = 0;
        r5 = 0;
        r6 = 0;
    L_0x0923:
        if (r10 == 0) goto L_0x0a83;
    L_0x0925:
        r0 = NUM; // 0x7fffffff float:NaN double:1.060997895E-314;
        r1.titleX = r0;	 Catch:{ Exception -> 0x0a57 }
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0a57 }
        if (r0 == 0) goto L_0x0954;
    L_0x092e:
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0943 }
        r30 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0943 }
        r0 = r0 + r30;
        r1.linkPreviewHeight = r0;	 Catch:{ Exception -> 0x0943 }
        r0 = r1.totalHeight;	 Catch:{ Exception -> 0x0943 }
        r30 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0943 }
        r0 = r0 + r30;
        r1.totalHeight = r0;	 Catch:{ Exception -> 0x0943 }
        goto L_0x0954;
    L_0x0943:
        r0 = move-exception;
        r44 = r3;
        r38 = r6;
        r39 = r11;
        r43 = r13;
        r30 = 3;
        r31 = 0;
        r6 = r5;
        r5 = r2;
        goto L_0x0a6c;
    L_0x0954:
        r0 = r1.isSmallImage;	 Catch:{ Exception -> 0x0a57 }
        if (r0 == 0) goto L_0x0983;
    L_0x0958:
        if (r15 != 0) goto L_0x095b;
    L_0x095a:
        goto L_0x0983;
    L_0x095b:
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0943 }
        r0 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);	 Catch:{ Exception -> 0x0943 }
        r33 = r4 - r0;
        r35 = 4;
        r34 = 3;
        r30 = r10;
        r32 = r4;
        r0 = generateStaticLayout(r30, r31, r32, r33, r34, r35);	 Catch:{ Exception -> 0x0943 }
        r1.titleLayout = r0;	 Catch:{ Exception -> 0x0943 }
        r0 = r1.titleLayout;	 Catch:{ Exception -> 0x0943 }
        r0 = r0.getLineCount();	 Catch:{ Exception -> 0x0943 }
        r21 = 3;
        r0 = 3 - r0;
        r30 = r0;
        r31 = r2;
        r0 = 3;
        goto L_0x09a9;
    L_0x0983:
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0a57 }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0a57 }
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r30);	 Catch:{ Exception -> 0x0a57 }
        r0 = (float) r0;	 Catch:{ Exception -> 0x0a57 }
        r36 = 0;
        r37 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x0a57 }
        r39 = 4;
        r30 = r10;
        r32 = r4;
        r35 = r0;
        r38 = r4;
        r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r30, r31, r32, r33, r34, r35, r36, r37, r38, r39);	 Catch:{ Exception -> 0x0a57 }
        r1.titleLayout = r0;	 Catch:{ Exception -> 0x0a57 }
        r31 = r2;
        r0 = 0;
        r30 = 3;
    L_0x09a9:
        r2 = r1.titleLayout;	 Catch:{ Exception -> 0x0a47 }
        r32 = r5;
        r5 = r1.titleLayout;	 Catch:{ Exception -> 0x0a43 }
        r5 = r5.getLineCount();	 Catch:{ Exception -> 0x0a43 }
        r25 = 1;
        r5 = r5 + -1;
        r2 = r2.getLineBottom(r5);	 Catch:{ Exception -> 0x0a43 }
        r5 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0a43 }
        r5 = r5 + r2;
        r1.linkPreviewHeight = r5;	 Catch:{ Exception -> 0x0a43 }
        r5 = r1.totalHeight;	 Catch:{ Exception -> 0x0a43 }
        r5 = r5 + r2;
        r1.totalHeight = r5;	 Catch:{ Exception -> 0x0a43 }
        r38 = r6;
        r39 = r11;
        r5 = r31;
        r6 = r32;
        r2 = 0;
        r31 = 0;
    L_0x09d0:
        r11 = r1.titleLayout;	 Catch:{ Exception -> 0x0a3d }
        r11 = r11.getLineCount();	 Catch:{ Exception -> 0x0a3d }
        if (r2 >= r11) goto L_0x0a38;
    L_0x09d8:
        r11 = r1.titleLayout;	 Catch:{ Exception -> 0x0a3d }
        r11 = r11.getLineLeft(r2);	 Catch:{ Exception -> 0x0a3d }
        r11 = (int) r11;
        r43 = r13;
        if (r11 == 0) goto L_0x09e5;
    L_0x09e3:
        r31 = 1;
    L_0x09e5:
        r13 = r1.titleX;	 Catch:{ Exception -> 0x0a34 }
        r44 = r3;
        r3 = NUM; // 0x7fffffff float:NaN double:1.060997895E-314;
        if (r13 != r3) goto L_0x09f2;
    L_0x09ee:
        r3 = -r11;
        r1.titleX = r3;	 Catch:{ Exception -> 0x0a32 }
        goto L_0x09fb;
    L_0x09f2:
        r3 = r1.titleX;	 Catch:{ Exception -> 0x0a32 }
        r13 = -r11;
        r3 = java.lang.Math.max(r3, r13);	 Catch:{ Exception -> 0x0a32 }
        r1.titleX = r3;	 Catch:{ Exception -> 0x0a32 }
    L_0x09fb:
        if (r11 == 0) goto L_0x0a05;
    L_0x09fd:
        r3 = r1.titleLayout;	 Catch:{ Exception -> 0x0a32 }
        r3 = r3.getWidth();	 Catch:{ Exception -> 0x0a32 }
        r3 = r3 - r11;
        goto L_0x0a11;
    L_0x0a05:
        r3 = r1.titleLayout;	 Catch:{ Exception -> 0x0a32 }
        r3 = r3.getLineWidth(r2);	 Catch:{ Exception -> 0x0a32 }
        r13 = (double) r3;	 Catch:{ Exception -> 0x0a32 }
        r13 = java.lang.Math.ceil(r13);	 Catch:{ Exception -> 0x0a32 }
        r3 = (int) r13;	 Catch:{ Exception -> 0x0a32 }
    L_0x0a11:
        if (r2 < r0) goto L_0x0a19;
    L_0x0a13:
        if (r11 == 0) goto L_0x0a20;
    L_0x0a15:
        r11 = r1.isSmallImage;	 Catch:{ Exception -> 0x0a32 }
        if (r11 == 0) goto L_0x0a20;
    L_0x0a19:
        r11 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);	 Catch:{ Exception -> 0x0a32 }
        r3 = r3 + r11;
    L_0x0a20:
        r3 = r3 + r8;
        r5 = java.lang.Math.max(r5, r3);	 Catch:{ Exception -> 0x0a32 }
        r6 = java.lang.Math.max(r6, r3);	 Catch:{ Exception -> 0x0a32 }
        r2 = r2 + 1;
        r14 = r60;
        r13 = r43;
        r3 = r44;
        goto L_0x09d0;
    L_0x0a32:
        r0 = move-exception;
        goto L_0x0a6c;
    L_0x0a34:
        r0 = move-exception;
        r44 = r3;
        goto L_0x0a6c;
    L_0x0a38:
        r44 = r3;
        r43 = r13;
        goto L_0x0a6f;
    L_0x0a3d:
        r0 = move-exception;
        r44 = r3;
        r43 = r13;
        goto L_0x0a6c;
    L_0x0a43:
        r0 = move-exception;
        r44 = r3;
        goto L_0x0a4c;
    L_0x0a47:
        r0 = move-exception;
        r44 = r3;
        r32 = r5;
    L_0x0a4c:
        r38 = r6;
        r39 = r11;
        r43 = r13;
        r5 = r31;
        r6 = r32;
        goto L_0x0a6a;
    L_0x0a57:
        r0 = move-exception;
        r31 = r2;
        r44 = r3;
        r32 = r5;
        r38 = r6;
        r39 = r11;
        r43 = r13;
        r5 = r31;
        r6 = r32;
        r30 = 3;
    L_0x0a6a:
        r31 = 0;
    L_0x0a6c:
        org.telegram.messenger.FileLog.e(r0);
    L_0x0a6f:
        r2 = r5;
        r5 = r6;
        r11 = r30;
        if (r31 == 0) goto L_0x0a80;
    L_0x0a75:
        r0 = r1.isSmallImage;
        if (r0 == 0) goto L_0x0a80;
    L_0x0a79:
        r0 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r4 = r4 - r0;
    L_0x0a80:
        r3 = r31;
        goto L_0x0a91;
    L_0x0a83:
        r31 = r2;
        r44 = r3;
        r32 = r5;
        r38 = r6;
        r39 = r11;
        r43 = r13;
        r3 = 0;
        r11 = 3;
    L_0x0a91:
        if (r12 == 0) goto L_0x0b39;
    L_0x0a93:
        if (r10 != 0) goto L_0x0b39;
    L_0x0a95:
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0b33 }
        if (r0 == 0) goto L_0x0aab;
    L_0x0a99:
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0b33 }
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0b33 }
        r0 = r0 + r6;
        r1.linkPreviewHeight = r0;	 Catch:{ Exception -> 0x0b33 }
        r0 = r1.totalHeight;	 Catch:{ Exception -> 0x0b33 }
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0b33 }
        r0 = r0 + r6;
        r1.totalHeight = r0;	 Catch:{ Exception -> 0x0b33 }
    L_0x0aab:
        r6 = 3;
        if (r11 != r6) goto L_0x0acc;
    L_0x0aae:
        r0 = r1.isSmallImage;	 Catch:{ Exception -> 0x0b33 }
        if (r0 == 0) goto L_0x0ab4;
    L_0x0ab2:
        if (r15 != 0) goto L_0x0acc;
    L_0x0ab4:
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x0b33 }
        r32 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0b33 }
        r34 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0b33 }
        r35 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r36 = 0;
        r37 = 0;
        r30 = r0;
        r31 = r12;
        r33 = r4;
        r30.<init>(r31, r32, r33, r34, r35, r36, r37);	 Catch:{ Exception -> 0x0b33 }
        r1.authorLayout = r0;	 Catch:{ Exception -> 0x0b33 }
        goto L_0x0aeb;
    L_0x0acc:
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0b33 }
        r0 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);	 Catch:{ Exception -> 0x0b33 }
        r33 = r4 - r0;
        r35 = 1;
        r30 = r12;
        r32 = r4;
        r34 = r11;
        r0 = generateStaticLayout(r30, r31, r32, r33, r34, r35);	 Catch:{ Exception -> 0x0b33 }
        r1.authorLayout = r0;	 Catch:{ Exception -> 0x0b33 }
        r0 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r0 = r0.getLineCount();	 Catch:{ Exception -> 0x0b33 }
        r11 = r11 - r0;
    L_0x0aeb:
        r0 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r6 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0b33 }
        r10 = 1;
        r6 = r6 - r10;
        r0 = r0.getLineBottom(r6);	 Catch:{ Exception -> 0x0b33 }
        r6 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0b33 }
        r6 = r6 + r0;
        r1.linkPreviewHeight = r6;	 Catch:{ Exception -> 0x0b33 }
        r6 = r1.totalHeight;	 Catch:{ Exception -> 0x0b33 }
        r6 = r6 + r0;
        r1.totalHeight = r6;	 Catch:{ Exception -> 0x0b33 }
        r0 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r6 = 0;
        r0 = r0.getLineLeft(r6);	 Catch:{ Exception -> 0x0b33 }
        r0 = (int) r0;	 Catch:{ Exception -> 0x0b33 }
        r6 = -r0;
        r1.authorX = r6;	 Catch:{ Exception -> 0x0b33 }
        if (r0 == 0) goto L_0x0b19;
    L_0x0b10:
        r6 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r6 = r6.getWidth();	 Catch:{ Exception -> 0x0b33 }
        r6 = r6 - r0;
        r12 = 1;
        goto L_0x0b27;
    L_0x0b19:
        r0 = r1.authorLayout;	 Catch:{ Exception -> 0x0b33 }
        r6 = 0;
        r0 = r0.getLineWidth(r6);	 Catch:{ Exception -> 0x0b33 }
        r12 = (double) r0;	 Catch:{ Exception -> 0x0b33 }
        r12 = java.lang.Math.ceil(r12);	 Catch:{ Exception -> 0x0b33 }
        r6 = (int) r12;
        r12 = 0;
    L_0x0b27:
        r6 = r6 + r8;
        r2 = java.lang.Math.max(r2, r6);	 Catch:{ Exception -> 0x0b31 }
        r5 = java.lang.Math.max(r5, r6);	 Catch:{ Exception -> 0x0b31 }
        goto L_0x0b3a;
    L_0x0b31:
        r0 = move-exception;
        goto L_0x0b35;
    L_0x0b33:
        r0 = move-exception;
        r12 = 0;
    L_0x0b35:
        org.telegram.messenger.FileLog.e(r0);
        goto L_0x0b3a;
    L_0x0b39:
        r12 = 0;
    L_0x0b3a:
        if (r15 == 0) goto L_0x0cad;
    L_0x0b3c:
        r6 = 0;
        r1.descriptionX = r6;	 Catch:{ Exception -> 0x0ca2 }
        r0 = r1.currentMessageObject;	 Catch:{ Exception -> 0x0ca2 }
        r0.generateLinkDescription();	 Catch:{ Exception -> 0x0ca2 }
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0ca2 }
        if (r0 == 0) goto L_0x0b5a;
    L_0x0b48:
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0ca2 }
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0ca2 }
        r0 = r0 + r6;
        r1.linkPreviewHeight = r0;	 Catch:{ Exception -> 0x0ca2 }
        r0 = r1.totalHeight;	 Catch:{ Exception -> 0x0ca2 }
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x0ca2 }
        r0 = r0 + r6;
        r1.totalHeight = r0;	 Catch:{ Exception -> 0x0ca2 }
    L_0x0b5a:
        if (r9 == 0) goto L_0x0b6a;
    L_0x0b5c:
        r0 = r9.toLowerCase();	 Catch:{ Exception -> 0x0ca2 }
        r6 = "twitter";
        r0 = r0.equals(r6);	 Catch:{ Exception -> 0x0ca2 }
        if (r0 == 0) goto L_0x0b6a;
    L_0x0b68:
        r0 = 1;
        goto L_0x0b6b;
    L_0x0b6a:
        r0 = 0;
    L_0x0b6b:
        r6 = 3;
        if (r11 != r6) goto L_0x0ba0;
    L_0x0b6e:
        r6 = r1.isSmallImage;	 Catch:{ Exception -> 0x0ca2 }
        if (r6 != 0) goto L_0x0ba0;
    L_0x0b72:
        r14 = r60;
        r6 = r14.linkDescription;	 Catch:{ Exception -> 0x0ca0 }
        r46 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x0ca0 }
        r48 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0ca0 }
        r49 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r10);	 Catch:{ Exception -> 0x0ca0 }
        r10 = (float) r11;	 Catch:{ Exception -> 0x0ca0 }
        r51 = 0;
        r52 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x0ca0 }
        if (r0 == 0) goto L_0x0b8e;
    L_0x0b89:
        r0 = 100;
        r54 = 100;
        goto L_0x0b90;
    L_0x0b8e:
        r54 = 6;
    L_0x0b90:
        r45 = r6;
        r47 = r4;
        r50 = r10;
        r53 = r4;
        r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r45, r46, r47, r48, r49, r50, r51, r52, r53, r54);	 Catch:{ Exception -> 0x0ca0 }
        r1.descriptionLayout = r0;	 Catch:{ Exception -> 0x0ca0 }
        r11 = 0;
        goto L_0x0bc3;
    L_0x0ba0:
        r14 = r60;
        r6 = r14.linkDescription;	 Catch:{ Exception -> 0x0ca0 }
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x0ca0 }
        r10 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);	 Catch:{ Exception -> 0x0ca0 }
        r33 = r4 - r10;
        if (r0 == 0) goto L_0x0bb5;
    L_0x0bb0:
        r0 = 100;
        r35 = 100;
        goto L_0x0bb7;
    L_0x0bb5:
        r35 = 6;
    L_0x0bb7:
        r30 = r6;
        r32 = r4;
        r34 = r11;
        r0 = generateStaticLayout(r30, r31, r32, r33, r34, r35);	 Catch:{ Exception -> 0x0ca0 }
        r1.descriptionLayout = r0;	 Catch:{ Exception -> 0x0ca0 }
    L_0x0bc3:
        r0 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0ca0 }
        r10 = 1;
        r6 = r6 - r10;
        r0 = r0.getLineBottom(r6);	 Catch:{ Exception -> 0x0ca0 }
        r6 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r6 + r0;
        r1.linkPreviewHeight = r6;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r1.totalHeight;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r6 + r0;
        r1.totalHeight = r6;	 Catch:{ Exception -> 0x0ca0 }
        r0 = 0;
        r13 = 0;
    L_0x0bdd:
        r6 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0ca0 }
        if (r0 >= r6) goto L_0x0c0c;
    L_0x0be5:
        r6 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0ca0 }
        r6 = r6.getLineLeft(r0);	 Catch:{ Exception -> 0x0ca0 }
        r10 = r4;
        r15 = r5;
        r4 = (double) r6;
        r4 = java.lang.Math.ceil(r4);	 Catch:{ Exception -> 0x0c9a }
        r4 = (int) r4;	 Catch:{ Exception -> 0x0c9a }
        if (r4 == 0) goto L_0x0CLASSNAME;
    L_0x0bf5:
        r5 = r1.descriptionX;	 Catch:{ Exception -> 0x0c9a }
        if (r5 != 0) goto L_0x0bfd;
    L_0x0bf9:
        r4 = -r4;
        r1.descriptionX = r4;	 Catch:{ Exception -> 0x0c9a }
        goto L_0x0CLASSNAME;
    L_0x0bfd:
        r5 = r1.descriptionX;	 Catch:{ Exception -> 0x0c9a }
        r4 = -r4;
        r4 = java.lang.Math.max(r5, r4);	 Catch:{ Exception -> 0x0c9a }
        r1.descriptionX = r4;	 Catch:{ Exception -> 0x0c9a }
    L_0x0CLASSNAME:
        r13 = 1;
    L_0x0CLASSNAME:
        r0 = r0 + 1;
        r4 = r10;
        r5 = r15;
        goto L_0x0bdd;
    L_0x0c0c:
        r10 = r4;
        r15 = r5;
        r0 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0c9a }
        r0 = r0.getWidth();	 Catch:{ Exception -> 0x0c9a }
        r4 = r2;
        r2 = 0;
    L_0x0CLASSNAME:
        r5 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0CLASSNAME }
        r5 = r5.getLineCount();	 Catch:{ Exception -> 0x0CLASSNAME }
        if (r2 >= r5) goto L_0x0c8d;
    L_0x0c1e:
        r5 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0CLASSNAME }
        r5 = r5.getLineLeft(r2);	 Catch:{ Exception -> 0x0CLASSNAME }
        r5 = (double) r5;	 Catch:{ Exception -> 0x0CLASSNAME }
        r5 = java.lang.Math.ceil(r5);	 Catch:{ Exception -> 0x0CLASSNAME }
        r5 = (int) r5;
        if (r5 != 0) goto L_0x0CLASSNAME;
    L_0x0c2c:
        r6 = r1.descriptionX;	 Catch:{ Exception -> 0x0CLASSNAME }
        if (r6 == 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r6 = 0;
        r1.descriptionX = r6;	 Catch:{ Exception -> 0x0CLASSNAME }
        goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r0 = move-exception;
        r2 = r4;
        goto L_0x0c9b;
    L_0x0CLASSNAME:
        if (r5 == 0) goto L_0x0CLASSNAME;
    L_0x0c3a:
        r6 = r0 - r5;
    L_0x0c3c:
        r30 = r9;
        r31 = r10;
        goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        if (r13 == 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r6 = r0;
        goto L_0x0c3c;
    L_0x0CLASSNAME:
        r6 = r1.descriptionLayout;	 Catch:{ Exception -> 0x0CLASSNAME }
        r6 = r6.getLineWidth(r2);	 Catch:{ Exception -> 0x0CLASSNAME }
        r30 = r9;
        r31 = r10;
        r9 = (double) r6;
        r9 = java.lang.Math.ceil(r9);	 Catch:{ Exception -> 0x0c8b }
        r6 = (int) r9;	 Catch:{ Exception -> 0x0c8b }
        r6 = java.lang.Math.min(r6, r0);	 Catch:{ Exception -> 0x0c8b }
    L_0x0CLASSNAME:
        if (r2 < r11) goto L_0x0CLASSNAME;
    L_0x0c5b:
        if (r11 == 0) goto L_0x0c6a;
    L_0x0c5d:
        if (r5 == 0) goto L_0x0c6a;
    L_0x0c5f:
        r5 = r1.isSmallImage;	 Catch:{ Exception -> 0x0c8b }
        if (r5 == 0) goto L_0x0c6a;
    L_0x0CLASSNAME:
        r5 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);	 Catch:{ Exception -> 0x0c8b }
        r6 = r6 + r5;
    L_0x0c6a:
        r6 = r6 + r8;
        if (r15 >= r6) goto L_0x0CLASSNAME;
    L_0x0c6d:
        if (r3 == 0) goto L_0x0CLASSNAME;
    L_0x0c6f:
        r5 = r1.titleX;	 Catch:{ Exception -> 0x0c8b }
        r9 = r6 - r15;
        r5 = r5 + r9;
        r1.titleX = r5;	 Catch:{ Exception -> 0x0c8b }
    L_0x0CLASSNAME:
        if (r12 == 0) goto L_0x0c7f;
    L_0x0CLASSNAME:
        r5 = r1.authorX;	 Catch:{ Exception -> 0x0c8b }
        r9 = r6 - r15;
        r5 = r5 + r9;
        r1.authorX = r5;	 Catch:{ Exception -> 0x0c8b }
    L_0x0c7f:
        r15 = r6;
    L_0x0CLASSNAME:
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x0c8b }
        r2 = r2 + 1;
        r9 = r30;
        r10 = r31;
        goto L_0x0CLASSNAME;
    L_0x0c8b:
        r0 = move-exception;
        goto L_0x0CLASSNAME;
    L_0x0c8d:
        r30 = r9;
        r31 = r10;
        r2 = r4;
        goto L_0x0cb3;
    L_0x0CLASSNAME:
        r0 = move-exception;
        r30 = r9;
        r31 = r10;
    L_0x0CLASSNAME:
        r2 = r4;
        goto L_0x0ca9;
    L_0x0c9a:
        r0 = move-exception;
    L_0x0c9b:
        r30 = r9;
        r31 = r10;
        goto L_0x0ca9;
    L_0x0ca0:
        r0 = move-exception;
        goto L_0x0ca5;
    L_0x0ca2:
        r0 = move-exception;
        r14 = r60;
    L_0x0ca5:
        r31 = r4;
        r30 = r9;
    L_0x0ca9:
        org.telegram.messenger.FileLog.e(r0);
        goto L_0x0cb3;
    L_0x0cad:
        r14 = r60;
        r31 = r4;
        r30 = r9;
    L_0x0cb3:
        if (r7 == 0) goto L_0x0cc6;
    L_0x0cb5:
        r0 = r1.descriptionLayout;
        if (r0 == 0) goto L_0x0cc2;
    L_0x0cb9:
        if (r0 == 0) goto L_0x0cc6;
    L_0x0cbb:
        r0 = r0.getLineCount();
        r3 = 1;
        if (r0 != r3) goto L_0x0cc6;
    L_0x0cc2:
        r3 = 0;
        r1.isSmallImage = r3;
        r7 = 0;
    L_0x0cc6:
        if (r7 == 0) goto L_0x0cd0;
    L_0x0cc8:
        r0 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r31 = r4;
    L_0x0cd0:
        if (r44 == 0) goto L_0x11c5;
    L_0x0cd2:
        r0 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r44);
        if (r0 == 0) goto L_0x0cf9;
    L_0x0cd8:
        r15 = r44;
        r0 = r15.thumbs;
        r3 = 90;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3);
        r1.currentPhotoObject = r0;
        r1.photoParentObject = r15;
        r1.documentAttach = r15;
        r0 = 7;
        r1.documentAttachType = r0;
        r6 = r29;
        r11 = r39;
        r5 = r41;
        r4 = r42;
        r13 = r43;
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x122b;
    L_0x0cf9:
        r15 = r44;
        r0 = org.telegram.messenger.MessageObject.isGifDocument(r15);
        if (r0 == 0) goto L_0x0d86;
    L_0x0d01:
        r0 = r60.isGame();
        if (r0 != 0) goto L_0x0d10;
    L_0x0d07:
        r0 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r0 != 0) goto L_0x0d10;
    L_0x0d0b:
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r14.gifState = r9;
        goto L_0x0d12;
    L_0x0d10:
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x0d12:
        r0 = r1.photoImage;
        r3 = r14.gifState;
        r3 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1));
        if (r3 == 0) goto L_0x0d1c;
    L_0x0d1a:
        r3 = 1;
        goto L_0x0d1d;
    L_0x0d1c:
        r3 = 0;
    L_0x0d1d:
        r0.setAllowStartAnimation(r3);
        r0 = r15.thumbs;
        r3 = 90;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3);
        r1.currentPhotoObject = r0;
        r1.photoParentObject = r15;
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x0d75;
    L_0x0d30:
        r3 = r0.w;
        if (r3 == 0) goto L_0x0d38;
    L_0x0d34:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0d75;
    L_0x0d38:
        r0 = 0;
    L_0x0d39:
        r3 = r15.attributes;
        r3 = r3.size();
        if (r0 >= r3) goto L_0x0d5f;
    L_0x0d41:
        r3 = r15.attributes;
        r3 = r3.get(r0);
        r3 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r3;
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 != 0) goto L_0x0d55;
    L_0x0d4d:
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r4 == 0) goto L_0x0d52;
    L_0x0d51:
        goto L_0x0d55;
    L_0x0d52:
        r0 = r0 + 1;
        goto L_0x0d39;
    L_0x0d55:
        r0 = r1.currentPhotoObject;
        r4 = r3.w;
        r0.w = r4;
        r3 = r3.h;
        r0.h = r3;
    L_0x0d5f:
        r0 = r1.currentPhotoObject;
        r3 = r0.w;
        if (r3 == 0) goto L_0x0d69;
    L_0x0d65:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0d75;
    L_0x0d69:
        r0 = r1.currentPhotoObject;
        r3 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0.h = r3;
        r0.w = r3;
    L_0x0d75:
        r1.documentAttach = r15;
        r3 = 2;
        r1.documentAttachType = r3;
        r6 = r29;
        r11 = r39;
        r5 = r41;
        r4 = r42;
        r13 = r43;
        goto L_0x122b;
    L_0x0d86:
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0 = org.telegram.messenger.MessageObject.isVideoDocument(r15);
        if (r0 == 0) goto L_0x0e54;
    L_0x0d8e:
        r13 = r43;
        if (r43 == 0) goto L_0x0dab;
    L_0x0d92:
        r0 = r13.sizes;
        r3 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r4 = 1;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3, r4);
        r1.currentPhotoObject = r0;
        r0 = r13.sizes;
        r3 = 40;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3);
        r1.currentPhotoObjectThumb = r0;
        r1.photoParentObject = r13;
    L_0x0dab:
        r0 = r1.currentPhotoObject;
        if (r0 != 0) goto L_0x0dc5;
    L_0x0daf:
        r0 = r15.thumbs;
        r3 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3);
        r1.currentPhotoObject = r0;
        r0 = r15.thumbs;
        r3 = 40;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r3);
        r1.currentPhotoObjectThumb = r0;
        r1.photoParentObject = r15;
    L_0x0dc5:
        r0 = r1.currentPhotoObject;
        r3 = r1.currentPhotoObjectThumb;
        if (r0 != r3) goto L_0x0dce;
    L_0x0dcb:
        r3 = 0;
        r1.currentPhotoObjectThumb = r3;
    L_0x0dce:
        r0 = r1.currentPhotoObject;
        if (r0 != 0) goto L_0x0de6;
    L_0x0dd2:
        r0 = new org.telegram.tgnet.TLRPC$TL_photoSize;
        r0.<init>();
        r1.currentPhotoObject = r0;
        r0 = r1.currentPhotoObject;
        r3 = "s";
        r0.type = r3;
        r3 = new org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;
        r3.<init>();
        r0.location = r3;
    L_0x0de6:
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x0e4e;
    L_0x0dea:
        r3 = r0.w;
        if (r3 == 0) goto L_0x0df6;
    L_0x0dee:
        r3 = r0.h;
        if (r3 == 0) goto L_0x0df6;
    L_0x0df2:
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r0 == 0) goto L_0x0e4e;
    L_0x0df6:
        r0 = 0;
    L_0x0df7:
        r3 = r15.attributes;
        r3 = r3.size();
        if (r0 >= r3) goto L_0x0e38;
    L_0x0dff:
        r3 = r15.attributes;
        r3 = r3.get(r0);
        r3 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r3;
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r4 == 0) goto L_0x0e35;
    L_0x0e0b:
        r0 = r1.currentPhotoObject;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r4 == 0) goto L_0x0e2c;
    L_0x0e11:
        r0 = r3.w;
        r0 = java.lang.Math.max(r0, r0);
        r0 = (float) r0;
        r4 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r0 = r0 / r4;
        r4 = r1.currentPhotoObject;
        r5 = r3.w;
        r5 = (float) r5;
        r5 = r5 / r0;
        r5 = (int) r5;
        r4.w = r5;
        r3 = r3.h;
        r3 = (float) r3;
        r3 = r3 / r0;
        r0 = (int) r3;
        r4.h = r0;
        goto L_0x0e38;
    L_0x0e2c:
        r4 = r3.w;
        r0.w = r4;
        r3 = r3.h;
        r0.h = r3;
        goto L_0x0e38;
    L_0x0e35:
        r0 = r0 + 1;
        goto L_0x0df7;
    L_0x0e38:
        r0 = r1.currentPhotoObject;
        r3 = r0.w;
        if (r3 == 0) goto L_0x0e42;
    L_0x0e3e:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0e4e;
    L_0x0e42:
        r0 = r1.currentPhotoObject;
        r3 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0.h = r3;
        r0.w = r3;
    L_0x0e4e:
        r3 = 0;
        r1.createDocumentLayout(r3, r14);
        goto L_0x0ef1;
    L_0x0e54:
        r13 = r43;
        r0 = org.telegram.messenger.MessageObject.isStickerDocument(r15);
        if (r0 != 0) goto L_0x1167;
    L_0x0e5c:
        r3 = 1;
        r0 = org.telegram.messenger.MessageObject.isAnimatedStickerDocument(r15, r3);
        if (r0 == 0) goto L_0x0e65;
    L_0x0e63:
        goto L_0x1167;
    L_0x0e65:
        r0 = r1.drawInstantViewType;
        r3 = 6;
        if (r0 != r3) goto L_0x0efb;
    L_0x0e6a:
        r0 = r15.thumbs;
        r4 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r4);
        r1.currentPhotoObject = r0;
        r1.photoParentObject = r15;
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x0ebb;
    L_0x0e7a:
        r4 = r0.w;
        if (r4 == 0) goto L_0x0e82;
    L_0x0e7e:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0ebb;
    L_0x0e82:
        r0 = 0;
    L_0x0e83:
        r4 = r15.attributes;
        r4 = r4.size();
        if (r0 >= r4) goto L_0x0ea5;
    L_0x0e8b:
        r4 = r15.attributes;
        r4 = r4.get(r0);
        r4 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r4;
        r5 = r4 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r5 == 0) goto L_0x0ea2;
    L_0x0e97:
        r0 = r1.currentPhotoObject;
        r5 = r4.w;
        r0.w = r5;
        r4 = r4.h;
        r0.h = r4;
        goto L_0x0ea5;
    L_0x0ea2:
        r0 = r0 + 1;
        goto L_0x0e83;
    L_0x0ea5:
        r0 = r1.currentPhotoObject;
        r4 = r0.w;
        if (r4 == 0) goto L_0x0eaf;
    L_0x0eab:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0ebb;
    L_0x0eaf:
        r0 = r1.currentPhotoObject;
        r4 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0.h = r4;
        r0.w = r4;
    L_0x0ebb:
        r1.documentAttach = r15;
        r4 = 8;
        r1.documentAttachType = r4;
        r0 = r1.documentAttach;
        r0 = r0.size;
        r4 = (long) r0;
        r0 = org.telegram.messenger.AndroidUtilities.formatFileSize(r4);
        r4 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r4 = r4.measureText(r0);
        r4 = (double) r4;
        r4 = java.lang.Math.ceil(r4);
        r4 = (int) r4;
        r1.durationWidth = r4;
        r4 = new android.text.StaticLayout;
        r45 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r5 = r1.durationWidth;
        r47 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r48 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r49 = 0;
        r50 = 0;
        r43 = r4;
        r44 = r0;
        r46 = r5;
        r43.<init>(r44, r45, r46, r47, r48, r49, r50);
        r1.videoInfoLayout = r4;
    L_0x0ef1:
        r6 = r29;
        r11 = r39;
        r5 = r41;
        r4 = r42;
        goto L_0x122b;
    L_0x0efb:
        r4 = 7;
        if (r0 != r4) goto L_0x0var_;
    L_0x0efe:
        r0 = r15.thumbs;
        r4 = 700; // 0x2bc float:9.81E-43 double:3.46E-321;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r4);
        r1.currentPhotoObject = r0;
        r0 = r15.thumbs;
        r4 = 40;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r4);
        r1.currentPhotoObjectThumb = r0;
        r1.photoParentObject = r15;
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x0var_;
    L_0x0var_:
        r4 = r0.w;
        if (r4 == 0) goto L_0x0var_;
    L_0x0f1c:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0var_;
    L_0x0var_:
        r0 = 0;
    L_0x0var_:
        r4 = r15.attributes;
        r4 = r4.size();
        if (r0 >= r4) goto L_0x0var_;
    L_0x0var_:
        r4 = r15.attributes;
        r4 = r4.get(r0);
        r4 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r4;
        r5 = r4 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r5 == 0) goto L_0x0var_;
    L_0x0var_:
        r0 = r1.currentPhotoObject;
        r5 = r4.w;
        r0.w = r5;
        r4 = r4.h;
        r0.h = r4;
        goto L_0x0var_;
    L_0x0var_:
        r0 = r0 + 1;
        goto L_0x0var_;
    L_0x0var_:
        r0 = r1.currentPhotoObject;
        r4 = r0.w;
        if (r4 == 0) goto L_0x0f4d;
    L_0x0var_:
        r0 = r0.h;
        if (r0 != 0) goto L_0x0var_;
    L_0x0f4d:
        r0 = r1.currentPhotoObject;
        r4 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0.h = r4;
        r0.w = r4;
    L_0x0var_:
        r1.documentAttach = r15;
        r0 = 9;
        r1.documentAttachType = r0;
        goto L_0x0ef1;
    L_0x0var_:
        r11 = r39;
        r4 = r42;
        r1.calcBackgroundWidth(r4, r11, r2);
        r0 = r1.backgroundWidth;
        r5 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = r5 + r4;
        if (r0 >= r5) goto L_0x0f7b;
    L_0x0var_:
        r0 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = r0 + r4;
        r1.backgroundWidth = r0;
    L_0x0f7b:
        r0 = org.telegram.messenger.MessageObject.isVoiceDocument(r15);
        if (r0 == 0) goto L_0x1029;
    L_0x0var_:
        r0 = r1.backgroundWidth;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r0 = r0 - r5;
        r1.createDocumentLayout(r0, r14);
        r0 = r1.currentMessageObject;
        r0 = r0.textHeight;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r0 = r0 + r5;
        r5 = r1.linkPreviewHeight;
        r0 = r0 + r5;
        r1.mediaOffsetY = r0;
        r0 = r1.totalHeight;
        r5 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r1.totalHeight = r0;
        r0 = r1.linkPreviewHeight;
        r5 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r1.linkPreviewHeight = r0;
        r0 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r4 = r4 - r0;
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x0ff0;
    L_0x0fbc:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r5 = r1.isChat;
        if (r5 == 0) goto L_0x0fd3;
    L_0x0fc4:
        r5 = r60.needDrawAvatar();
        if (r5 == 0) goto L_0x0fd3;
    L_0x0fca:
        r5 = r60.isOutOwner();
        if (r5 != 0) goto L_0x0fd3;
    L_0x0fd0:
        r10 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        goto L_0x0fd4;
    L_0x0fd3:
        r10 = 0;
    L_0x0fd4:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = r0 - r5;
        r5 = NUM; // 0x435CLASSNAME float:220.0 double:5.58344962E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = java.lang.Math.min(r0, r5);
        r5 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 - r5;
        r0 = r0 + r8;
        r0 = java.lang.Math.max(r2, r0);
        goto L_0x1023;
    L_0x0ff0:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r5 = r1.isChat;
        if (r5 == 0) goto L_0x1007;
    L_0x0ff8:
        r5 = r60.needDrawAvatar();
        if (r5 == 0) goto L_0x1007;
    L_0x0ffe:
        r5 = r60.isOutOwner();
        if (r5 != 0) goto L_0x1007;
    L_0x1004:
        r10 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        goto L_0x1008;
    L_0x1007:
        r10 = 0;
    L_0x1008:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = r0 - r5;
        r5 = NUM; // 0x435CLASSNAME float:220.0 double:5.58344962E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = java.lang.Math.min(r0, r5);
        r5 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 - r5;
        r0 = r0 + r8;
        r0 = java.lang.Math.max(r2, r0);
    L_0x1023:
        r2 = r0;
        r1.calcBackgroundWidth(r4, r11, r2);
        goto L_0x10b7;
    L_0x1029:
        r0 = org.telegram.messenger.MessageObject.isMusicDocument(r15);
        if (r0 == 0) goto L_0x10be;
    L_0x102f:
        r0 = r1.backgroundWidth;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r0 = r0 - r5;
        r0 = r1.createDocumentLayout(r0, r14);
        r5 = r1.currentMessageObject;
        r5 = r5.textHeight;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r5 = r5 + r6;
        r6 = r1.linkPreviewHeight;
        r5 = r5 + r6;
        r1.mediaOffsetY = r5;
        r5 = r1.totalHeight;
        r6 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r1.totalHeight = r5;
        r5 = r1.linkPreviewHeight;
        r6 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r1.linkPreviewHeight = r5;
        r5 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r0 = r0 + r8;
        r5 = NUM; // 0x42bCLASSNAME float:94.0 double:5.53164308E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r0 = java.lang.Math.max(r2, r0);
        r2 = r1.songLayout;
        if (r2 == 0) goto L_0x1092;
    L_0x1075:
        r2 = r2.getLineCount();
        if (r2 <= 0) goto L_0x1092;
    L_0x107b:
        r0 = (float) r0;
        r2 = r1.songLayout;
        r5 = 0;
        r2 = r2.getLineWidth(r5);
        r5 = (float) r8;
        r2 = r2 + r5;
        r5 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = (float) r5;
        r2 = r2 + r5;
        r0 = java.lang.Math.max(r0, r2);
        r0 = (int) r0;
    L_0x1092:
        r2 = r1.performerLayout;
        if (r2 == 0) goto L_0x10b3;
    L_0x1096:
        r2 = r2.getLineCount();
        if (r2 <= 0) goto L_0x10b3;
    L_0x109c:
        r0 = (float) r0;
        r2 = r1.performerLayout;
        r5 = 0;
        r2 = r2.getLineWidth(r5);
        r5 = (float) r8;
        r2 = r2 + r5;
        r5 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = (float) r5;
        r2 = r2 + r5;
        r0 = java.lang.Math.max(r0, r2);
        r0 = (int) r0;
    L_0x10b3:
        r2 = r0;
        r1.calcBackgroundWidth(r4, r11, r2);
    L_0x10b7:
        r0 = r4;
        r6 = r29;
        r5 = r41;
        goto L_0x122c;
    L_0x10be:
        r0 = r1.backgroundWidth;
        r5 = NUM; // 0x43280000 float:168.0 double:5.566612494E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 - r5;
        r1.createDocumentLayout(r0, r14);
        r5 = 1;
        r1.drawImageButton = r5;
        r0 = r1.drawPhotoImage;
        if (r0 == 0) goto L_0x1100;
    L_0x10d1:
        r0 = r1.totalHeight;
        r5 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r1.totalHeight = r0;
        r0 = r1.linkPreviewHeight;
        r5 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r1.linkPreviewHeight = r0;
        r0 = r1.photoImage;
        r5 = r1.totalHeight;
        r6 = r1.namesOffset;
        r5 = r5 + r6;
        r6 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r10 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r12 = 0;
        r0.setImageCoords(r12, r5, r6, r10);
        goto L_0x11c1;
    L_0x1100:
        r0 = r1.currentMessageObject;
        r0 = r0.textHeight;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r0 = r0 + r5;
        r5 = r1.linkPreviewHeight;
        r0 = r0 + r5;
        r1.mediaOffsetY = r0;
        r0 = r1.photoImage;
        r5 = r1.totalHeight;
        r6 = r1.namesOffset;
        r5 = r5 + r6;
        r6 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 - r6;
        r6 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r10 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r12 = 0;
        r0.setImageCoords(r12, r5, r6, r10);
        r0 = r1.totalHeight;
        r5 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r1.totalHeight = r0;
        r0 = r1.linkPreviewHeight;
        r5 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r6;
        r1.linkPreviewHeight = r0;
        r0 = r1.docTitleLayout;
        if (r0 == 0) goto L_0x11c1;
    L_0x1146:
        r0 = r0.getLineCount();
        r5 = 1;
        if (r0 <= r5) goto L_0x11c1;
    L_0x114d:
        r0 = r1.docTitleLayout;
        r0 = r0.getLineCount();
        r0 = r0 - r5;
        r5 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 * r5;
        r5 = r1.totalHeight;
        r5 = r5 + r0;
        r1.totalHeight = r5;
        r5 = r1.linkPreviewHeight;
        r5 = r5 + r0;
        r1.linkPreviewHeight = r5;
        goto L_0x11c1;
    L_0x1167:
        r11 = r39;
        r4 = r42;
        r0 = r15.thumbs;
        r5 = 90;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r5);
        r1.currentPhotoObject = r0;
        r1.photoParentObject = r15;
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x11bc;
    L_0x117b:
        r5 = r0.w;
        if (r5 == 0) goto L_0x1183;
    L_0x117f:
        r0 = r0.h;
        if (r0 != 0) goto L_0x11bc;
    L_0x1183:
        r0 = 0;
    L_0x1184:
        r5 = r15.attributes;
        r5 = r5.size();
        if (r0 >= r5) goto L_0x11a6;
    L_0x118c:
        r5 = r15.attributes;
        r5 = r5.get(r0);
        r5 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r5;
        r6 = r5 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r6 == 0) goto L_0x11a3;
    L_0x1198:
        r0 = r1.currentPhotoObject;
        r6 = r5.w;
        r0.w = r6;
        r5 = r5.h;
        r0.h = r5;
        goto L_0x11a6;
    L_0x11a3:
        r0 = r0 + 1;
        goto L_0x1184;
    L_0x11a6:
        r0 = r1.currentPhotoObject;
        r5 = r0.w;
        if (r5 == 0) goto L_0x11b0;
    L_0x11ac:
        r0 = r0.h;
        if (r0 != 0) goto L_0x11bc;
    L_0x11b0:
        r0 = r1.currentPhotoObject;
        r5 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0.h = r5;
        r0.w = r5;
    L_0x11bc:
        r1.documentAttach = r15;
        r3 = 6;
        r1.documentAttachType = r3;
    L_0x11c1:
        r6 = r29;
        goto L_0x1229;
    L_0x11c5:
        r11 = r39;
        r4 = r42;
        r13 = r43;
        r15 = r44;
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r13 == 0) goto L_0x1214;
    L_0x11d1:
        if (r29 == 0) goto L_0x11df;
    L_0x11d3:
        r0 = "photo";
        r6 = r29;
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x11e1;
    L_0x11dd:
        r0 = 1;
        goto L_0x11e2;
    L_0x11df:
        r6 = r29;
    L_0x11e1:
        r0 = 0;
    L_0x11e2:
        r5 = r14.photoThumbs;
        if (r0 != 0) goto L_0x11ec;
    L_0x11e6:
        if (r7 != 0) goto L_0x11e9;
    L_0x11e8:
        goto L_0x11ec;
    L_0x11e9:
        r10 = r31;
        goto L_0x11f0;
    L_0x11ec:
        r10 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x11f0:
        r12 = r0 ^ 1;
        r5 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r5, r10, r12);
        r1.currentPhotoObject = r5;
        r5 = r14.photoThumbsObject;
        r1.photoParentObject = r5;
        r5 = 1;
        r0 = r0 ^ r5;
        r1.checkOnlyButtonPressed = r0;
        r0 = r14.photoThumbs;
        r5 = 40;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r5);
        r1.currentPhotoObjectThumb = r0;
        r0 = r1.currentPhotoObjectThumb;
        r5 = r1.currentPhotoObject;
        if (r0 != r5) goto L_0x1229;
    L_0x1210:
        r5 = 0;
        r1.currentPhotoObjectThumb = r5;
        goto L_0x1229;
    L_0x1214:
        r6 = r29;
        if (r41 == 0) goto L_0x1229;
    L_0x1218:
        r5 = r41;
        r0 = r5.mime_type;
        r10 = "image/";
        r0 = r0.startsWith(r10);
        if (r0 != 0) goto L_0x1225;
    L_0x1224:
        r5 = 0;
    L_0x1225:
        r10 = 0;
        r1.drawImageButton = r10;
        goto L_0x122b;
    L_0x1229:
        r5 = r41;
    L_0x122b:
        r0 = r4;
    L_0x122c:
        r4 = r1.documentAttachType;
        r10 = 5;
        if (r4 == r10) goto L_0x18de;
    L_0x1231:
        r10 = 3;
        if (r4 == r10) goto L_0x18de;
    L_0x1234:
        r10 = 1;
        if (r4 == r10) goto L_0x18de;
    L_0x1237:
        r4 = r1.currentPhotoObject;
        if (r4 != 0) goto L_0x1261;
    L_0x123b:
        if (r5 == 0) goto L_0x123e;
    L_0x123d:
        goto L_0x1261;
    L_0x123e:
        r3 = r1.photoImage;
        r4 = 0;
        r3.setImageBitmap(r4);
        r3 = r1.linkPreviewHeight;
        r4 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r1.linkPreviewHeight = r3;
        r3 = r1.totalHeight;
        r4 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r1.totalHeight = r3;
        r55 = r11;
        r15 = 1;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x17d6;
    L_0x1261:
        if (r13 == 0) goto L_0x1265;
    L_0x1263:
        if (r7 == 0) goto L_0x128d;
    L_0x1265:
        if (r6 == 0) goto L_0x128f;
    L_0x1267:
        r4 = "photo";
        r4 = r6.equals(r4);
        if (r4 != 0) goto L_0x128d;
    L_0x126f:
        r4 = "document";
        r4 = r6.equals(r4);
        if (r4 == 0) goto L_0x127c;
    L_0x1277:
        r4 = r1.documentAttachType;
        r3 = 6;
        if (r4 != r3) goto L_0x128d;
    L_0x127c:
        r4 = "gif";
        r4 = r6.equals(r4);
        if (r4 != 0) goto L_0x128d;
    L_0x1284:
        r4 = r1.documentAttachType;
        r10 = 4;
        if (r4 == r10) goto L_0x128d;
    L_0x1289:
        r10 = 8;
        if (r4 != r10) goto L_0x128f;
    L_0x128d:
        r4 = 1;
        goto L_0x1290;
    L_0x128f:
        r4 = 0;
    L_0x1290:
        r1.drawImageButton = r4;
        r4 = r1.linkPreviewHeight;
        if (r4 == 0) goto L_0x12a6;
    L_0x1296:
        r10 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r4 = r4 + r10;
        r1.linkPreviewHeight = r4;
        r4 = r1.totalHeight;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r4 = r4 + r10;
        r1.totalHeight = r4;
    L_0x12a6:
        r4 = r1.imageBackgroundSideColor;
        if (r4 == 0) goto L_0x12b1;
    L_0x12aa:
        r4 = NUM; // 0x43500000 float:208.0 double:5.57956413E-315;
        r31 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x12ee;
    L_0x12b1:
        r4 = r1.currentPhotoObject;
        r10 = r4 instanceof org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
        if (r10 == 0) goto L_0x12be;
    L_0x12b7:
        r4 = r4.w;
        if (r4 == 0) goto L_0x12be;
    L_0x12bb:
        r31 = r4;
        goto L_0x12ee;
    L_0x12be:
        r4 = r1.documentAttachType;
        r3 = 6;
        if (r4 == r3) goto L_0x12d8;
    L_0x12c3:
        r10 = 8;
        if (r4 == r10) goto L_0x12d8;
    L_0x12c7:
        r10 = 9;
        if (r4 != r10) goto L_0x12cc;
    L_0x12cb:
        goto L_0x12d8;
    L_0x12cc:
        r10 = 7;
        if (r4 != r10) goto L_0x12ee;
    L_0x12cf:
        r31 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r4 = r1.photoImage;
        r10 = 1;
        r4.setAllowDecodeSingleFrame(r10);
        goto L_0x12ee;
    L_0x12d8:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x12e3;
    L_0x12de:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        goto L_0x12e7;
    L_0x12e3:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
    L_0x12e7:
        r4 = (float) r4;
        r10 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        r4 = r4 * r10;
        r4 = (int) r4;
        goto L_0x12bb;
    L_0x12ee:
        r4 = r1.hasInvoicePreview;
        if (r4 == 0) goto L_0x12f9;
    L_0x12f2:
        r4 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x12fa;
    L_0x12f9:
        r12 = 0;
    L_0x12fa:
        r4 = r31 - r12;
        r4 = r4 + r8;
        r27 = java.lang.Math.max(r2, r4);
        r2 = r1.currentPhotoObject;
        if (r2 == 0) goto L_0x130f;
    L_0x1305:
        r8 = -1;
        r2.size = r8;
        r2 = r1.currentPhotoObjectThumb;
        if (r2 == 0) goto L_0x1312;
    L_0x130c:
        r2.size = r8;
        goto L_0x1312;
    L_0x130f:
        r8 = -1;
        r5.size = r8;
    L_0x1312:
        r2 = r1.imageBackgroundSideColor;
        if (r2 == 0) goto L_0x1320;
    L_0x1316:
        r2 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r27 - r2;
        r1.imageBackgroundSideWidth = r2;
    L_0x1320:
        if (r7 != 0) goto L_0x13b2;
    L_0x1322:
        r2 = r1.documentAttachType;
        r4 = 7;
        if (r2 != r4) goto L_0x1329;
    L_0x1327:
        goto L_0x13b2;
    L_0x1329:
        r2 = r1.hasGamePreview;
        if (r2 != 0) goto L_0x1399;
    L_0x132d:
        r2 = r1.hasInvoicePreview;
        if (r2 == 0) goto L_0x1332;
    L_0x1331:
        goto L_0x1399;
    L_0x1332:
        r2 = r1.currentPhotoObject;
        r4 = r2.w;
        r2 = r2.h;
        r4 = (float) r4;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r7 = r31 - r7;
        r7 = (float) r7;
        r7 = r4 / r7;
        r4 = r4 / r7;
        r4 = (int) r4;
        r2 = (float) r2;
        r2 = r2 / r7;
        r2 = (int) r2;
        if (r30 == 0) goto L_0x1368;
    L_0x1349:
        if (r30 == 0) goto L_0x135c;
    L_0x134b:
        r7 = r30.toLowerCase();
        r10 = "instagram";
        r7 = r7.equals(r10);
        if (r7 != 0) goto L_0x135c;
    L_0x1357:
        r7 = r1.documentAttachType;
        if (r7 != 0) goto L_0x135c;
    L_0x135b:
        goto L_0x1368;
    L_0x135c:
        r7 = org.telegram.messenger.AndroidUtilities.displaySize;
        r7 = r7.y;
        r10 = r7 / 2;
        if (r2 <= r10) goto L_0x1373;
    L_0x1364:
        r10 = 2;
        r2 = r7 / 2;
        goto L_0x1373;
    L_0x1368:
        r7 = org.telegram.messenger.AndroidUtilities.displaySize;
        r7 = r7.y;
        r10 = r7 / 3;
        if (r2 <= r10) goto L_0x1373;
    L_0x1370:
        r10 = 3;
        r2 = r7 / 3;
    L_0x1373:
        r7 = r1.imageBackgroundSideColor;
        if (r7 == 0) goto L_0x1386;
    L_0x1377:
        r2 = (float) r2;
        r7 = NUM; // 0x43200000 float:160.0 double:5.564022167E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r7 = r2 / r7;
        r4 = (float) r4;
        r4 = r4 / r7;
        r4 = (int) r4;
        r2 = r2 / r7;
        r2 = (int) r2;
    L_0x1386:
        r31 = r4;
        r4 = NUM; // 0x42700000 float:60.0 double:5.507034975E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        if (r2 >= r4) goto L_0x1396;
    L_0x1390:
        r2 = NUM; // 0x42700000 float:60.0 double:5.507034975E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
    L_0x1396:
        r4 = r31;
        goto L_0x13b5;
    L_0x1399:
        r2 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r4 = 360; // 0x168 float:5.04E-43 double:1.78E-321;
        r2 = (float) r2;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r7 = r31 - r7;
        r7 = (float) r7;
        r7 = r2 / r7;
        r2 = r2 / r7;
        r2 = (int) r2;
        r4 = (float) r4;
        r4 = r4 / r7;
        r4 = (int) r4;
        r58 = r4;
        r4 = r2;
        r2 = r58;
        goto L_0x13b5;
    L_0x13b2:
        r2 = r31;
        r4 = r2;
    L_0x13b5:
        r7 = r1.isSmallImage;
        if (r7 == 0) goto L_0x13ea;
    L_0x13b9:
        r7 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r10 = r10 + r38;
        r12 = r1.linkPreviewHeight;
        if (r10 <= r12) goto L_0x13e0;
    L_0x13c5:
        r10 = r1.totalHeight;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r12 = r12 + r38;
        r13 = r1.linkPreviewHeight;
        r12 = r12 - r13;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r12 = r12 + r13;
        r10 = r10 + r12;
        r1.totalHeight = r10;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = r7 + r38;
        r1.linkPreviewHeight = r7;
    L_0x13e0:
        r7 = r1.linkPreviewHeight;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r7 = r7 - r10;
        r1.linkPreviewHeight = r7;
        goto L_0x13fb;
    L_0x13ea:
        r7 = r1.totalHeight;
        r10 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r10 = r10 + r2;
        r7 = r7 + r10;
        r1.totalHeight = r7;
        r7 = r1.linkPreviewHeight;
        r7 = r7 + r2;
        r1.linkPreviewHeight = r7;
    L_0x13fb:
        r7 = r1.documentAttachType;
        r10 = 8;
        if (r7 != r10) goto L_0x1418;
    L_0x1401:
        r7 = r1.imageBackgroundSideColor;
        if (r7 != 0) goto L_0x1418;
    L_0x1405:
        r7 = r1.photoImage;
        r10 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r10 = r27 - r10;
        r10 = java.lang.Math.max(r10, r4);
        r12 = 0;
        r7.setImageCoords(r12, r12, r10, r2);
        goto L_0x141e;
    L_0x1418:
        r12 = 0;
        r7 = r1.photoImage;
        r7.setImageCoords(r12, r12, r4, r2);
    L_0x141e:
        r7 = java.util.Locale.US;
        r10 = 2;
        r13 = new java.lang.Object[r10];
        r23 = java.lang.Integer.valueOf(r4);
        r13[r12] = r23;
        r23 = java.lang.Integer.valueOf(r2);
        r24 = 1;
        r13[r24] = r23;
        r3 = "%d_%d";
        r3 = java.lang.String.format(r7, r3, r13);
        r1.currentPhotoFilter = r3;
        r3 = java.util.Locale.US;
        r7 = new java.lang.Object[r10];
        r13 = java.lang.Integer.valueOf(r4);
        r7[r12] = r13;
        r12 = java.lang.Integer.valueOf(r2);
        r7[r24] = r12;
        r12 = "%d_%d_b";
        r3 = java.lang.String.format(r3, r12, r7);
        r1.currentPhotoFilterThumb = r3;
        if (r5 == 0) goto L_0x147c;
    L_0x1453:
        r2 = r1.photoImage;
        r3 = org.telegram.messenger.ImageLocation.getForWebFile(r5);
        r4 = r1.currentPhotoFilter;
        r7 = 0;
        r12 = 0;
        r13 = r5.size;
        r15 = 0;
        r23 = 1;
        r5 = r7;
        r7 = r6;
        r6 = r12;
        r12 = r7;
        r7 = r13;
        r13 = -1;
        r8 = r15;
        r15 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r9 = r60;
        r13 = 2;
        r10 = r23;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        r55 = r11;
        r56 = r12;
        r15 = 1;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x176d;
    L_0x147c:
        r12 = r6;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r13 = 2;
        r3 = r1.documentAttachType;
        r5 = 8;
        if (r3 != r5) goto L_0x14c1;
    L_0x1486:
        r2 = r14.mediaExists;
        if (r2 == 0) goto L_0x14a9;
    L_0x148a:
        r2 = r1.photoImage;
        r3 = r1.documentAttach;
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObject;
        r5 = org.telegram.messenger.ImageLocation.getForDocument(r5, r15);
        r7 = 0;
        r15 = 1;
        r6 = "b1";
        r8 = "jpg";
        r9 = r60;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = r15;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x14e2;
    L_0x14a9:
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = r1.photoImage;
        r3 = 0;
        r4 = 0;
        r5 = r1.currentPhotoObject;
        r5 = org.telegram.messenger.ImageLocation.getForDocument(r5, r15);
        r7 = 0;
        r10 = 1;
        r6 = "b1";
        r8 = "jpg";
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x14e2;
    L_0x14c1:
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r5 = 9;
        if (r3 != r5) goto L_0x14e9;
    L_0x14c7:
        r2 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3, r15);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObjectThumb;
        r5 = org.telegram.messenger.ImageLocation.getForDocument(r5, r15);
        r7 = 0;
        r10 = 1;
        r6 = "b1";
        r8 = "jpg";
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
    L_0x14e2:
        r55 = r11;
        r56 = r12;
        r15 = 1;
        goto L_0x176d;
    L_0x14e9:
        r5 = 6;
        if (r3 != r5) goto L_0x1555;
    L_0x14ec:
        r3 = r60.isSticker();
        r5 = org.telegram.messenger.SharedConfig.loopStickers;
        if (r5 != 0) goto L_0x152a;
    L_0x14f4:
        if (r3 == 0) goto L_0x14f7;
    L_0x14f6:
        goto L_0x152a;
    L_0x14f7:
        r3 = java.util.Locale.US;
        r15 = 3;
        r5 = new java.lang.Object[r15];
        r4 = java.lang.Integer.valueOf(r4);
        r21 = 0;
        r5[r21] = r4;
        r2 = java.lang.Integer.valueOf(r2);
        r4 = 1;
        r5[r4] = r2;
        r2 = r60.toString();
        r5[r13] = r2;
        r2 = "%d_%d_nr_%s";
        r2 = java.lang.String.format(r3, r2, r5);
        r1.currentPhotoFilter = r2;
        r2 = r1.photoImage;
        r3 = r1.delegate;
        r3 = r3.shouldRepeatSticker(r14);
        if (r3 == 0) goto L_0x1525;
    L_0x1523:
        r3 = 2;
        goto L_0x1526;
    L_0x1525:
        r3 = 3;
    L_0x1526:
        r2.setAutoRepeat(r3);
        goto L_0x1533;
    L_0x152a:
        r15 = 3;
        r21 = 0;
        r2 = r1.photoImage;
        r3 = 1;
        r2.setAutoRepeat(r3);
    L_0x1533:
        r2 = r1.photoImage;
        r3 = r1.documentAttach;
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObject;
        r6 = r1.documentAttach;
        r5 = org.telegram.messenger.ImageLocation.getForDocument(r5, r6);
        r6 = r1.documentAttach;
        r7 = r6.size;
        r10 = 1;
        r6 = "b1";
        r8 = "webp";
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x14e2;
    L_0x1555:
        r21 = 0;
        r22 = 3;
        r5 = 4;
        if (r3 != r5) goto L_0x1622;
    L_0x155c:
        r2 = r1.photoImage;
        r3 = 1;
        r2.setNeedsQualityThumb(r3);
        r2 = r1.photoImage;
        r2.setShouldGenerateQualityThumb(r3);
        r2 = org.telegram.messenger.SharedConfig.autoplayVideo;
        if (r2 == 0) goto L_0x15cd;
    L_0x156b:
        r2 = r1.currentMessageObject;
        r2 = r2.mediaExists;
        if (r2 != 0) goto L_0x1585;
    L_0x1571:
        r2 = r60.canStreamVideo();
        if (r2 == 0) goto L_0x15cd;
    L_0x1577:
        r2 = r1.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r1.currentMessageObject;
        r2 = r2.canDownloadMedia(r3);
        if (r2 == 0) goto L_0x15cd;
    L_0x1585:
        r2 = r1.photoImage;
        r15 = 1;
        r2.setAllowDecodeSingleFrame(r15);
        r2 = r1.photoImage;
        r2.setAllowStartAnimation(r15);
        r2 = r1.photoImage;
        r2.startAnimation();
        r2 = r1.photoImage;
        r3 = r1.documentAttach;
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3);
        r4 = r1.currentPhotoObject;
        r5 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r4, r5);
        r6 = r1.currentPhotoFilter;
        r4 = r1.currentPhotoObjectThumb;
        r7 = r1.documentAttach;
        r7 = org.telegram.messenger.ImageLocation.getForDocument(r4, r7);
        r8 = r1.currentPhotoFilterThumb;
        r9 = 0;
        r4 = r1.documentAttach;
        r10 = r4.size;
        r23 = 0;
        r24 = 0;
        r4 = "g";
        r55 = r11;
        r11 = r23;
        r56 = r12;
        r12 = r60;
        r13 = r24;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
        r1.autoPlayingMedia = r15;
        goto L_0x176d;
    L_0x15cd:
        r55 = r11;
        r56 = r12;
        r15 = 1;
        r2 = r1.currentPhotoObjectThumb;
        if (r2 == 0) goto L_0x15f6;
    L_0x15d6:
        r2 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r4 = r1.photoParentObject;
        r3 = org.telegram.messenger.ImageLocation.getForObject(r3, r4);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilterThumb;
        r7 = 0;
        r8 = 0;
        r10 = 0;
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x176d;
    L_0x15f6:
        r2 = r1.photoImage;
        r3 = 0;
        r4 = 0;
        r5 = r1.currentPhotoObject;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoObject;
        r7 = r6 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r7 != 0) goto L_0x1616;
    L_0x1608:
        r6 = r6.type;
        r7 = "s";
        r6 = r7.equals(r6);
        if (r6 == 0) goto L_0x1613;
    L_0x1612:
        goto L_0x1616;
    L_0x1613:
        r6 = r1.currentPhotoFilter;
        goto L_0x1618;
    L_0x1616:
        r6 = r1.currentPhotoFilterThumb;
    L_0x1618:
        r7 = 0;
        r8 = 0;
        r10 = 0;
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x176d;
    L_0x1622:
        r55 = r11;
        r56 = r12;
        r5 = r15;
        r6 = 2;
        r13 = -1;
        r15 = 1;
        if (r3 == r6) goto L_0x16c2;
    L_0x162c:
        r6 = 7;
        if (r3 != r6) goto L_0x1631;
    L_0x162f:
        goto L_0x16c2;
    L_0x1631:
        r3 = r14.mediaExists;
        r5 = r1.currentPhotoObject;
        r5 = org.telegram.messenger.FileLoader.getAttachFileName(r5);
        r6 = r1.hasGamePreview;
        if (r6 != 0) goto L_0x169f;
    L_0x163d:
        if (r3 != 0) goto L_0x169f;
    L_0x163f:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.DownloadController.getInstance(r3);
        r6 = r1.currentMessageObject;
        r3 = r3.canDownloadMedia(r6);
        if (r3 != 0) goto L_0x169f;
    L_0x164d:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.FileLoader.getInstance(r3);
        r3 = r3.isLoadingFile(r5);
        if (r3 == 0) goto L_0x165a;
    L_0x1659:
        goto L_0x169f;
    L_0x165a:
        r1.photoNotSet = r15;
        r3 = r1.currentPhotoObjectThumb;
        if (r3 == 0) goto L_0x1696;
    L_0x1660:
        r5 = r1.photoImage;
        r6 = 0;
        r7 = 0;
        r8 = r1.photoParentObject;
        r8 = org.telegram.messenger.ImageLocation.getForObject(r3, r8);
        r3 = java.util.Locale.US;
        r9 = 2;
        r10 = new java.lang.Object[r9];
        r4 = java.lang.Integer.valueOf(r4);
        r12 = 0;
        r10[r12] = r4;
        r2 = java.lang.Integer.valueOf(r2);
        r10[r15] = r2;
        r2 = "%d_%d_b";
        r9 = java.lang.String.format(r3, r2, r10);
        r10 = 0;
        r11 = 0;
        r22 = 0;
        r2 = r5;
        r3 = r6;
        r4 = r7;
        r5 = r8;
        r6 = r9;
        r7 = r10;
        r8 = r11;
        r9 = r60;
        r10 = r22;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x176d;
    L_0x1696:
        r12 = 0;
        r2 = r1.photoImage;
        r3 = 0;
        r2.setImageBitmap(r3);
        goto L_0x176d;
    L_0x169f:
        r12 = 0;
        r1.photoNotSet = r12;
        r2 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r4 = r1.photoParentObject;
        r3 = org.telegram.messenger.ImageLocation.getForObject(r3, r4);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilterThumb;
        r7 = 0;
        r8 = 0;
        r10 = 0;
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x176d;
    L_0x16c2:
        r12 = 0;
        r2 = r1.photoImage;
        r2.setAllowDecodeSingleFrame(r15);
        org.telegram.messenger.FileLoader.getAttachFileName(r5);
        r2 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r5);
        if (r2 == 0) goto L_0x16e7;
    L_0x16d1:
        r2 = r1.photoImage;
        r3 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r4 = 2;
        r3 = r3 / r4;
        r2.setRoundRadius(r3);
        r2 = r1.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r1.currentMessageObject;
        r2 = r2.canDownloadMedia(r3);
        goto L_0x16fb;
    L_0x16e7:
        r2 = org.telegram.messenger.MessageObject.isGifDocument(r5);
        if (r2 == 0) goto L_0x16fa;
    L_0x16ed:
        r2 = r1.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r1.currentMessageObject;
        r2 = r2.canDownloadMedia(r3);
        goto L_0x16fb;
    L_0x16fa:
        r2 = 0;
    L_0x16fb:
        r3 = r1.currentPhotoObject;
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r4 != 0) goto L_0x170f;
    L_0x1701:
        r3 = r3.type;
        r4 = "s";
        r3 = r4.equals(r3);
        if (r3 == 0) goto L_0x170c;
    L_0x170b:
        goto L_0x170f;
    L_0x170c:
        r3 = r1.currentPhotoFilter;
        goto L_0x1711;
    L_0x170f:
        r3 = r1.currentPhotoFilterThumb;
    L_0x1711:
        r35 = r3;
        r3 = r14.mediaExists;
        if (r3 != 0) goto L_0x1738;
    L_0x1717:
        if (r2 == 0) goto L_0x171a;
    L_0x1719:
        goto L_0x1738;
    L_0x171a:
        r2 = r1.photoImage;
        r32 = 0;
        r33 = 0;
        r3 = r1.currentPhotoObject;
        r4 = r1.documentAttach;
        r34 = org.telegram.messenger.ImageLocation.getForDocument(r3, r4);
        r36 = 0;
        r37 = 0;
        r3 = r1.currentMessageObject;
        r39 = 0;
        r31 = r2;
        r38 = r3;
        r31.setImage(r32, r33, r34, r35, r36, r37, r38, r39);
        goto L_0x176d;
    L_0x1738:
        r1.autoPlayingMedia = r15;
        r2 = r1.photoImage;
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r5);
        r4 = r5.size;
        r6 = 32768; // 0x8000 float:4.5918E-41 double:1.61895E-319;
        if (r4 >= r6) goto L_0x1749;
    L_0x1747:
        r4 = 0;
        goto L_0x174b;
    L_0x1749:
        r4 = "g";
    L_0x174b:
        r6 = r1.currentPhotoObject;
        r7 = r1.documentAttach;
        r6 = org.telegram.messenger.ImageLocation.getForDocument(r6, r7);
        r7 = r1.currentPhotoObjectThumb;
        r8 = r1.documentAttach;
        r7 = org.telegram.messenger.ImageLocation.getForDocument(r7, r8);
        r8 = r1.currentPhotoFilterThumb;
        r9 = 0;
        r10 = r5.size;
        r11 = 0;
        r22 = 0;
        r5 = r6;
        r6 = r35;
        r12 = r60;
        r13 = r22;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
    L_0x176d:
        r1.drawPhotoImage = r15;
        r3 = r56;
        if (r3 == 0) goto L_0x17a3;
    L_0x1773:
        r2 = "video";
        r2 = r3.equals(r2);
        if (r2 == 0) goto L_0x17a3;
    L_0x177c:
        if (r40 == 0) goto L_0x17a3;
    L_0x177e:
        r4 = org.telegram.messenger.AndroidUtilities.formatShortDuration(r40);
        r2 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r2 = r2.measureText(r4);
        r2 = (double) r2;
        r2 = java.lang.Math.ceil(r2);
        r2 = (int) r2;
        r1.durationWidth = r2;
        r2 = new android.text.StaticLayout;
        r5 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r6 = r1.durationWidth;
        r7 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r9 = 0;
        r10 = 0;
        r3 = r2;
        r3.<init>(r4, r5, r6, r7, r8, r9, r10);
        r1.videoInfoLayout = r2;
        goto L_0x17d4;
    L_0x17a3:
        r2 = r1.hasGamePreview;
        if (r2 == 0) goto L_0x17d4;
    L_0x17a7:
        r2 = NUM; // 0x7f0e014e float:1.8875715E38 double:1.0531623216E-314;
        r3 = "AttachGame";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r4 = r2.toUpperCase();
        r2 = org.telegram.ui.ActionBar.Theme.chat_gamePaint;
        r2 = r2.measureText(r4);
        r2 = (double) r2;
        r2 = java.lang.Math.ceil(r2);
        r2 = (int) r2;
        r1.durationWidth = r2;
        r2 = new android.text.StaticLayout;
        r5 = org.telegram.ui.ActionBar.Theme.chat_gamePaint;
        r6 = r1.durationWidth;
        r7 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r9 = 0;
        r10 = 0;
        r3 = r2;
        r3.<init>(r4, r5, r6, r7, r8, r9, r10);
        r1.videoInfoLayout = r2;
    L_0x17d4:
        r2 = r27;
    L_0x17d6:
        r3 = r1.hasInvoicePreview;
        if (r3 == 0) goto L_0x18b8;
    L_0x17da:
        r3 = r14.messageOwner;
        r3 = r3.media;
        r4 = r3.flags;
        r4 = r4 & 4;
        if (r4 == 0) goto L_0x17f2;
    L_0x17e4:
        r3 = NUM; // 0x7f0e087c float:1.8879443E38 double:1.0531632297E-314;
        r4 = "PaymentReceipt";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
        r3 = r3.toUpperCase();
        goto L_0x1811;
    L_0x17f2:
        r3 = r3.test;
        if (r3 == 0) goto L_0x1804;
    L_0x17f6:
        r3 = NUM; // 0x7f0e088e float:1.887948E38 double:1.0531632386E-314;
        r4 = "PaymentTestInvoice";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
        r3 = r3.toUpperCase();
        goto L_0x1811;
    L_0x1804:
        r3 = NUM; // 0x7f0e086f float:1.8879417E38 double:1.0531632233E-314;
        r4 = "PaymentInvoice";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
        r3 = r3.toUpperCase();
    L_0x1811:
        r4 = org.telegram.messenger.LocaleController.getInstance();
        r5 = r14.messageOwner;
        r5 = r5.media;
        r6 = r5.total_amount;
        r5 = r5.currency;
        r4 = r4.formatCurrencyString(r6, r5);
        r6 = new android.text.SpannableStringBuilder;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r5.append(r4);
        r7 = " ";
        r5.append(r7);
        r5.append(r3);
        r3 = r5.toString();
        r6.<init>(r3);
        r3 = new org.telegram.ui.Components.TypefaceSpan;
        r5 = "fonts/rmedium.ttf";
        r5 = org.telegram.messenger.AndroidUtilities.getTypeface(r5);
        r3.<init>(r5);
        r4 = r4.length();
        r5 = 33;
        r13 = 0;
        r6.setSpan(r3, r13, r4, r5);
        r3 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r4 = r6.length();
        r3 = r3.measureText(r6, r13, r4);
        r3 = (double) r3;
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r1.durationWidth = r3;
        r3 = new android.text.StaticLayout;
        r7 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r4 = r1.durationWidth;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r8 = r4 + r5;
        r9 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = 0;
        r12 = 0;
        r5 = r3;
        r5.<init>(r6, r7, r8, r9, r10, r11, r12);
        r1.videoInfoLayout = r3;
        r3 = r1.drawPhotoImage;
        if (r3 != 0) goto L_0x18b9;
    L_0x187d:
        r3 = r1.totalHeight;
        r4 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r1.totalHeight = r3;
        r3 = r1.timeWidth;
        r4 = r60.isOutOwner();
        if (r4 == 0) goto L_0x1893;
    L_0x1890:
        r12 = 20;
        goto L_0x1894;
    L_0x1893:
        r12 = 0;
    L_0x1894:
        r12 = r12 + 14;
        r4 = (float) r12;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r4 = r1.durationWidth;
        r5 = r4 + r3;
        if (r5 <= r0) goto L_0x18b2;
    L_0x18a2:
        r2 = java.lang.Math.max(r4, r2);
        r3 = r1.totalHeight;
        r4 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r1.totalHeight = r3;
        goto L_0x18b9;
    L_0x18b2:
        r4 = r4 + r3;
        r2 = java.lang.Math.max(r4, r2);
        goto L_0x18b9;
    L_0x18b8:
        r13 = 0;
    L_0x18b9:
        r3 = r1.hasGamePreview;
        if (r3 == 0) goto L_0x18d8;
    L_0x18bd:
        r3 = r14.textHeight;
        if (r3 == 0) goto L_0x18d8;
    L_0x18c1:
        r4 = r1.linkPreviewHeight;
        r5 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r3 = r3 + r5;
        r4 = r4 + r3;
        r1.linkPreviewHeight = r4;
        r3 = r1.totalHeight;
        r4 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r1.totalHeight = r3;
    L_0x18d8:
        r3 = r55;
        r1.calcBackgroundWidth(r0, r3, r2);
        goto L_0x18e2;
    L_0x18de:
        r13 = 0;
        r15 = 1;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x18e2:
        r59.createInstantViewButton();
        goto L_0x2175;
    L_0x18e7:
        r13 = 0;
        r15 = 1;
        r30 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = 16;
        r3 = NUM; // 0x42cCLASSNAME float:102.0 double:5.536823734E-315;
        if (r0 != r2) goto L_0x1a43;
    L_0x18f1:
        r1.drawName = r13;
        r1.drawForwardedName = r13;
        r1.drawPhotoImage = r13;
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x1926;
    L_0x18fd:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1912;
    L_0x1905:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1912;
    L_0x190b:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1912;
    L_0x1911:
        goto L_0x1914;
    L_0x1912:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1914:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
        goto L_0x194e;
    L_0x1926:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x193b;
    L_0x192e:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x193b;
    L_0x1934:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x193b;
    L_0x193a:
        goto L_0x193d;
    L_0x193b:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x193d:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
    L_0x194e:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r1.availableTimeWidth = r0;
        r0 = r59.getMaxNameWidth();
        r2 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        if (r0 >= 0) goto L_0x196a;
    L_0x1966:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x196a:
        r2 = org.telegram.messenger.LocaleController.getInstance();
        r2 = r2.formatterDay;
        r3 = r14.messageOwner;
        r3 = r3.date;
        r3 = (long) r3;
        r5 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r3 = r3 * r5;
        r2 = r2.format(r3);
        r3 = r14.messageOwner;
        r3 = r3.action;
        r3 = (org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall) r3;
        r4 = r3.reason;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
        r5 = r60.isOutOwner();
        if (r5 == 0) goto L_0x19a3;
    L_0x198d:
        if (r4 == 0) goto L_0x1999;
    L_0x198f:
        r4 = NUM; // 0x7f0e0202 float:1.887608E38 double:1.0531624106E-314;
        r5 = "CallMessageOutgoingMissed";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        goto L_0x19c8;
    L_0x1999:
        r4 = NUM; // 0x7f0e0201 float:1.8876078E38 double:1.05316241E-314;
        r5 = "CallMessageOutgoing";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        goto L_0x19c8;
    L_0x19a3:
        if (r4 == 0) goto L_0x19af;
    L_0x19a5:
        r4 = NUM; // 0x7f0e0200 float:1.8876076E38 double:1.0531624096E-314;
        r5 = "CallMessageIncomingMissed";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        goto L_0x19c8;
    L_0x19af:
        r4 = r3.reason;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
        if (r4 == 0) goto L_0x19bf;
    L_0x19b5:
        r4 = NUM; // 0x7f0e01ff float:1.8876074E38 double:1.053162409E-314;
        r5 = "CallMessageIncomingDeclined";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        goto L_0x19c8;
    L_0x19bf:
        r4 = NUM; // 0x7f0e01fe float:1.8876072E38 double:1.0531624086E-314;
        r5 = "CallMessageIncoming";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
    L_0x19c8:
        r5 = r3.duration;
        if (r5 <= 0) goto L_0x19e6;
    L_0x19cc:
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r5.append(r2);
        r2 = ", ";
        r5.append(r2);
        r2 = r3.duration;
        r2 = org.telegram.messenger.LocaleController.formatCallDuration(r2);
        r5.append(r2);
        r2 = r5.toString();
    L_0x19e6:
        r3 = new android.text.StaticLayout;
        r5 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r12 = (float) r0;
        r6 = android.text.TextUtils.TruncateAt.END;
        r6 = android.text.TextUtils.ellipsize(r4, r5, r12, r6);
        r7 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r8 = r0 + r4;
        r9 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = 0;
        r4 = 0;
        r5 = r3;
        r15 = r12;
        r12 = r4;
        r5.<init>(r6, r7, r8, r9, r10, r11, r12);
        r1.titleLayout = r3;
        r3 = new android.text.StaticLayout;
        r4 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r5 = android.text.TextUtils.TruncateAt.END;
        r32 = android.text.TextUtils.ellipsize(r2, r4, r15, r5);
        r33 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r34 = r0 + r2;
        r35 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r36 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r37 = 0;
        r38 = 0;
        r31 = r3;
        r31.<init>(r32, r33, r34, r35, r36, r37, r38);
        r1.docTitleLayout = r3;
        r59.setMessageObjectInternal(r60);
        r0 = NUM; // 0x42820000 float:65.0 double:5.51286321E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.totalHeight = r0;
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x2175;
    L_0x1a3a:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r30);
        r2 = r2 - r0;
        r1.namesOffset = r2;
        goto L_0x2175;
    L_0x1a43:
        r2 = 12;
        if (r0 != r2) goto L_0x1CLASSNAME;
    L_0x1a47:
        r1.drawName = r13;
        r2 = 1;
        r1.drawForwardedName = r2;
        r1.drawPhotoImage = r2;
        r0 = r1.photoImage;
        r2 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0.setRoundRadius(r2);
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x1a88;
    L_0x1a5f:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1a74;
    L_0x1a67:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1a74;
    L_0x1a6d:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1a74;
    L_0x1a73:
        goto L_0x1a76;
    L_0x1a74:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1a76:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
        goto L_0x1ab0;
    L_0x1a88:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1a9d;
    L_0x1a90:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1a9d;
    L_0x1a96:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1a9d;
    L_0x1a9c:
        goto L_0x1a9f;
    L_0x1a9d:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1a9f:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
    L_0x1ab0:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r1.availableTimeWidth = r0;
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.user_id;
        r2 = r1.currentAccount;
        r2 = org.telegram.messenger.MessagesController.getInstance(r2);
        r0 = java.lang.Integer.valueOf(r0);
        r0 = r2.getUser(r0);
        r2 = r59.getMaxNameWidth();
        r3 = NUM; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        if (r2 >= 0) goto L_0x1ae0;
    L_0x1adc:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x1ae0:
        r9 = r2;
        if (r0 == 0) goto L_0x1ae8;
    L_0x1ae3:
        r2 = r1.contactAvatarDrawable;
        r2.setInfo(r0);
    L_0x1ae8:
        r2 = r1.photoImage;
        r3 = org.telegram.messenger.ImageLocation.getForUser(r0, r13);
        if (r0 == 0) goto L_0x1af4;
    L_0x1af0:
        r4 = r1.contactAvatarDrawable;
    L_0x1af2:
        r5 = r4;
        goto L_0x1afd;
    L_0x1af4:
        r4 = org.telegram.ui.ActionBar.Theme.chat_contactDrawable;
        r5 = r60.isOutOwner();
        r4 = r4[r5];
        goto L_0x1af2;
    L_0x1afd:
        r6 = 0;
        r8 = 0;
        r4 = "50_50";
        r7 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8);
        r2 = r14.vCardData;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 != 0) goto L_0x1b19;
    L_0x1b0e:
        r0 = r14.vCardData;
        r2 = 1;
        r1.drawInstantView = r2;
        r2 = 5;
        r1.drawInstantViewType = r2;
    L_0x1b16:
        r32 = r0;
        goto L_0x1b5e;
    L_0x1b19:
        if (r0 == 0) goto L_0x1b3f;
    L_0x1b1b:
        r2 = r0.phone;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 != 0) goto L_0x1b3f;
    L_0x1b23:
        r2 = org.telegram.PhoneFormat.PhoneFormat.getInstance();
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "+";
        r3.append(r4);
        r0 = r0.phone;
        r3.append(r0);
        r0 = r3.toString();
        r0 = r2.format(r0);
        goto L_0x1b16;
    L_0x1b3f:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.phone_number;
        r2 = android.text.TextUtils.isEmpty(r0);
        if (r2 != 0) goto L_0x1b54;
    L_0x1b4b:
        r2 = org.telegram.PhoneFormat.PhoneFormat.getInstance();
        r0 = r2.format(r0);
        goto L_0x1b16;
    L_0x1b54:
        r0 = NUM; // 0x7f0e0755 float:1.8878845E38 double:1.053163084E-314;
        r2 = "NumberUnknown";
        r0 = org.telegram.messenger.LocaleController.getString(r2, r0);
        goto L_0x1b16;
    L_0x1b5e:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r2 = r0.first_name;
        r0 = r0.last_name;
        r0 = org.telegram.messenger.ContactsController.formatName(r2, r0);
        r2 = 10;
        r3 = 32;
        r0 = r0.replace(r2, r3);
        r2 = r0.length();
        if (r2 != 0) goto L_0x1b82;
    L_0x1b78:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.phone_number;
        if (r0 != 0) goto L_0x1b82;
    L_0x1b80:
        r0 = "";
    L_0x1b82:
        r2 = new android.text.StaticLayout;
        r3 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r4 = (float) r9;
        r5 = android.text.TextUtils.TruncateAt.END;
        r34 = android.text.TextUtils.ellipsize(r0, r3, r4, r5);
        r35 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r0 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r36 = r9 + r0;
        r37 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r38 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r39 = 0;
        r40 = 0;
        r33 = r2;
        r33.<init>(r34, r35, r36, r37, r38, r39, r40);
        r1.titleLayout = r2;
        r0 = new android.text.StaticLayout;
        r33 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r34 = r9 + r2;
        r35 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r36 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r30);
        r2 = (float) r2;
        r38 = 0;
        r31 = r0;
        r37 = r2;
        r31.<init>(r32, r33, r34, r35, r36, r37, r38);
        r1.docTitleLayout = r0;
        r59.setMessageObjectInternal(r60);
        r0 = r1.drawForwardedName;
        if (r0 == 0) goto L_0x1be5;
    L_0x1bcb:
        r0 = r60.needDrawForwarded();
        if (r0 == 0) goto L_0x1be5;
    L_0x1bd1:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x1bd9;
    L_0x1bd5:
        r0 = r0.minY;
        if (r0 != 0) goto L_0x1be5;
    L_0x1bd9:
        r0 = r1.namesOffset;
        r2 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.namesOffset = r0;
        goto L_0x1bfa;
    L_0x1be5:
        r0 = r1.drawNameLayout;
        if (r0 == 0) goto L_0x1bfa;
    L_0x1be9:
        r0 = r14.messageOwner;
        r0 = r0.reply_to_msg_id;
        if (r0 != 0) goto L_0x1bfa;
    L_0x1bef:
        r0 = r1.namesOffset;
        r2 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.namesOffset = r0;
    L_0x1bfa:
        r0 = NUM; // 0x425CLASSNAME float:55.0 double:5.50055916E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r2 = r1.docTitleLayout;
        r2 = r2.getHeight();
        r0 = r0 + r2;
        r1.totalHeight = r0;
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x1CLASSNAME;
    L_0x1CLASSNAME:
        r0 = r1.namesOffset;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r30);
        r0 = r0 - r2;
        r1.namesOffset = r0;
    L_0x1CLASSNAME:
        r0 = r1.drawInstantView;
        if (r0 == 0) goto L_0x1CLASSNAME;
    L_0x1c1d:
        r59.createInstantViewButton();
        goto L_0x2175;
    L_0x1CLASSNAME:
        r0 = r1.docTitleLayout;
        r0 = r0.getLineCount();
        if (r0 <= 0) goto L_0x2175;
    L_0x1c2a:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x42dCLASSNAME float:110.0 double:5.54200439E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r2 = r1.docTitleLayout;
        r3 = r2.getLineCount();
        r4 = 1;
        r3 = r3 - r4;
        r2 = r2.getLineWidth(r3);
        r2 = (double) r2;
        r2 = java.lang.Math.ceil(r2);
        r2 = (int) r2;
        r0 = r0 - r2;
        r2 = r1.timeWidth;
        if (r0 >= r2) goto L_0x2175;
    L_0x1c4a:
        r0 = r1.totalHeight;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r0 = r0 + r2;
        r1.totalHeight = r0;
        goto L_0x2175;
    L_0x1CLASSNAME:
        r2 = 2;
        if (r0 != r2) goto L_0x1cd2;
    L_0x1CLASSNAME:
        r2 = 1;
        r1.drawForwardedName = r2;
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x1c8a;
    L_0x1CLASSNAME:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1CLASSNAME;
    L_0x1CLASSNAME:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1CLASSNAME;
    L_0x1c6f:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1CLASSNAME;
    L_0x1CLASSNAME:
        goto L_0x1CLASSNAME;
    L_0x1CLASSNAME:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1CLASSNAME:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
        goto L_0x1cb2;
    L_0x1c8a:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1c9f;
    L_0x1CLASSNAME:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1c9f;
    L_0x1CLASSNAME:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1c9f;
    L_0x1c9e:
        goto L_0x1ca1;
    L_0x1c9f:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1ca1:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
    L_0x1cb2:
        r0 = r1.backgroundWidth;
        r1.createDocumentLayout(r0, r14);
        r59.setMessageObjectInternal(r60);
        r0 = NUM; // 0x428CLASSNAME float:70.0 double:5.51610112E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.totalHeight = r0;
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x2175;
    L_0x1cc9:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r30);
        r2 = r2 - r0;
        r1.namesOffset = r2;
        goto L_0x2175;
    L_0x1cd2:
        r2 = 14;
        if (r0 != r2) goto L_0x1d4d;
    L_0x1cd6:
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x1d05;
    L_0x1cdc:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1cf1;
    L_0x1ce4:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1cf1;
    L_0x1cea:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1cf1;
    L_0x1cf0:
        goto L_0x1cf3;
    L_0x1cf1:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1cf3:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
        goto L_0x1d2d;
    L_0x1d05:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x1d1a;
    L_0x1d0d:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x1d1a;
    L_0x1d13:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x1d1a;
    L_0x1d19:
        goto L_0x1d1c;
    L_0x1d1a:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x1d1c:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43870000 float:270.0 double:5.597372625E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
    L_0x1d2d:
        r0 = r1.backgroundWidth;
        r1.createDocumentLayout(r0, r14);
        r59.setMessageObjectInternal(r60);
        r0 = NUM; // 0x42a40000 float:82.0 double:5.5238721E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.totalHeight = r0;
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x2175;
    L_0x1d44:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r30);
        r2 = r2 - r0;
        r1.namesOffset = r2;
        goto L_0x2175;
    L_0x1d4d:
        r2 = 17;
        if (r0 != r2) goto L_0x217d;
    L_0x1d51:
        r59.createSelectorDrawable();
        r2 = 1;
        r1.drawName = r2;
        r1.drawForwardedName = r2;
        r1.drawPhotoImage = r13;
        r0 = NUM; // 0x43fa0000 float:500.0 double:5.634608575E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r60.getMaxMessageTextWidth();
        r0 = java.lang.Math.min(r0, r2);
        r1.availableTimeWidth = r0;
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r2 + r0;
        r1.backgroundWidth = r2;
        r2 = NUM; // 0x42var_ float:120.0 double:5.548480205E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1.availableTimeWidth = r2;
        r59.measureTime(r60);
        r2 = r14.messageOwner;
        r2 = r2.media;
        r2 = (org.telegram.tgnet.TLRPC.TL_messageMediaPoll) r2;
        r3 = r2.poll;
        r3 = r3.closed;
        r1.pollClosed = r3;
        r3 = r60.isVoted();
        r1.pollVoted = r3;
        r3 = new android.text.StaticLayout;
        r4 = r2.poll;
        r4 = r4.question;
        r5 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r5 = r5.getFontMetricsInt();
        r6 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = org.telegram.messenger.Emoji.replaceEmoji(r4, r5, r6, r13);
        r6 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r7 = r0 + r4;
        r8 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = 0;
        r11 = 0;
        r4 = r3;
        r4.<init>(r5, r6, r7, r8, r9, r10, r11);
        r1.titleLayout = r3;
        r3 = r1.titleLayout;
        if (r3 == 0) goto L_0x1dd6;
    L_0x1dbf:
        r3 = r3.getLineCount();
        r4 = 0;
    L_0x1dc4:
        if (r4 >= r3) goto L_0x1dd6;
    L_0x1dc6:
        r5 = r1.titleLayout;
        r5 = r5.getLineLeft(r4);
        r15 = 0;
        r5 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1));
        if (r5 == 0) goto L_0x1dd3;
    L_0x1dd1:
        r3 = 1;
        goto L_0x1dd8;
    L_0x1dd3:
        r4 = r4 + 1;
        goto L_0x1dc4;
    L_0x1dd6:
        r15 = 0;
        r3 = 0;
    L_0x1dd8:
        r12 = new android.text.StaticLayout;
        r4 = r2.poll;
        r4 = r4.closed;
        if (r4 == 0) goto L_0x1de6;
    L_0x1de0:
        r4 = NUM; // 0x7f0e04ae float:1.8877467E38 double:1.0531627485E-314;
        r5 = "FinalResults";
        goto L_0x1deb;
    L_0x1de6:
        r4 = NUM; // 0x7f0e00f5 float:1.8875534E38 double:1.0531622777E-314;
        r5 = "AnonymousPoll";
    L_0x1deb:
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r5 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r11 = (float) r0;
        r6 = android.text.TextUtils.TruncateAt.END;
        r5 = android.text.TextUtils.ellipsize(r4, r5, r11, r6);
        r6 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r7 = r0 + r4;
        r8 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r9 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10 = 0;
        r22 = 0;
        r4 = r12;
        r23 = r11;
        r11 = r22;
        r4.<init>(r5, r6, r7, r8, r9, r10, r11);
        r1.docTitleLayout = r12;
        r4 = r1.docTitleLayout;
        if (r4 == 0) goto L_0x1e47;
    L_0x1e15:
        r4 = r4.getLineCount();
        if (r4 <= 0) goto L_0x1e47;
    L_0x1e1b:
        if (r3 == 0) goto L_0x1e32;
    L_0x1e1d:
        r4 = org.telegram.messenger.LocaleController.isRTL;
        if (r4 != 0) goto L_0x1e32;
    L_0x1e21:
        r3 = r1.docTitleLayout;
        r3 = r3.getLineWidth(r13);
        r11 = r23 - r3;
        r3 = (double) r11;
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r1.docTitleOffsetX = r3;
        goto L_0x1e47;
    L_0x1e32:
        if (r3 != 0) goto L_0x1e47;
    L_0x1e34:
        r3 = org.telegram.messenger.LocaleController.isRTL;
        if (r3 == 0) goto L_0x1e47;
    L_0x1e38:
        r3 = r1.docTitleLayout;
        r3 = r3.getLineLeft(r13);
        r3 = (double) r3;
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r3 = -r3;
        r1.docTitleOffsetX = r3;
    L_0x1e47:
        r3 = r1.timeWidth;
        r3 = r0 - r3;
        r4 = r60.isOutOwner();
        if (r4 == 0) goto L_0x1e54;
    L_0x1e51:
        r4 = NUM; // 0x41e00000 float:28.0 double:5.46040909E-315;
        goto L_0x1e56;
    L_0x1e54:
        r4 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
    L_0x1e56:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r8 = r3 - r4;
        r3 = new android.text.StaticLayout;
        r4 = r2.results;
        r4 = r4.total_voters;
        if (r4 != 0) goto L_0x1e6e;
    L_0x1e64:
        r4 = NUM; // 0x7f0e06bf float:1.887854E38 double:1.05316301E-314;
        r5 = "NoVotes";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        goto L_0x1e74;
    L_0x1e6e:
        r5 = "Vote";
        r4 = org.telegram.messenger.LocaleController.formatPluralString(r5, r4);
    L_0x1e74:
        r5 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r6 = (float) r8;
        r7 = android.text.TextUtils.TruncateAt.END;
        r6 = android.text.TextUtils.ellipsize(r4, r5, r6, r7);
        r7 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r9 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = 0;
        r12 = 0;
        r5 = r3;
        r5.<init>(r6, r7, r8, r9, r10, r11, r12);
        r1.infoLayout = r3;
        r3 = r1.infoLayout;
        if (r3 == 0) goto L_0x1e9e;
    L_0x1e8f:
        r3 = r3.getLineCount();
        if (r3 <= 0) goto L_0x1e9e;
    L_0x1e95:
        r3 = r1.infoLayout;
        r3 = r3.getLineLeft(r13);
        r3 = -r3;
        r3 = (double) r3;
        goto L_0x1ea0;
    L_0x1e9e:
        r3 = 0;
    L_0x1ea0:
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r1.infoX = r3;
        r3 = r2.poll;
        r1.lastPoll = r3;
        r3 = r2.results;
        r4 = r3.results;
        r1.lastPollResults = r4;
        r3 = r3.total_voters;
        r1.lastPollResultsVoters = r3;
        r3 = r1.animatePollAnswer;
        if (r3 != 0) goto L_0x1ec3;
    L_0x1eb9:
        r3 = r1.pollVoteInProgress;
        if (r3 == 0) goto L_0x1ec3;
    L_0x1ebd:
        r3 = 2;
        r12 = 3;
        r1.performHapticFeedback(r12, r3);
        goto L_0x1ec4;
    L_0x1ec3:
        r12 = 3;
    L_0x1ec4:
        r3 = r1.attachedToWindow;
        if (r3 == 0) goto L_0x1ed2;
    L_0x1ec8:
        r3 = r1.pollVoteInProgress;
        if (r3 != 0) goto L_0x1ed0;
    L_0x1ecc:
        r3 = r1.pollUnvoteInProgress;
        if (r3 == 0) goto L_0x1ed2;
    L_0x1ed0:
        r3 = 1;
        goto L_0x1ed3;
    L_0x1ed2:
        r3 = 0;
    L_0x1ed3:
        r1.animatePollAnswer = r3;
        r1.animatePollAnswerAlpha = r3;
        r3 = new java.util.ArrayList;
        r3.<init>();
        r4 = r1.pollButtons;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x1var_;
    L_0x1ee4:
        r4 = new java.util.ArrayList;
        r5 = r1.pollButtons;
        r4.<init>(r5);
        r5 = r1.pollButtons;
        r5.clear();
        r5 = r1.animatePollAnswer;
        if (r5 != 0) goto L_0x1var_;
    L_0x1ef4:
        r5 = r1.attachedToWindow;
        if (r5 == 0) goto L_0x1var_;
    L_0x1ef8:
        r5 = r1.pollVoted;
        if (r5 != 0) goto L_0x1var_;
    L_0x1efc:
        r5 = r1.pollClosed;
        if (r5 == 0) goto L_0x1var_;
    L_0x1var_:
        r5 = 1;
        goto L_0x1var_;
    L_0x1var_:
        r5 = 0;
    L_0x1var_:
        r1.animatePollAnswer = r5;
    L_0x1var_:
        r5 = r1.pollAnimationProgress;
        r6 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1));
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r6 <= 0) goto L_0x1var_;
    L_0x1f0d:
        r5 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1));
        if (r5 >= 0) goto L_0x1var_;
    L_0x1var_:
        r5 = r4.size();
        r6 = 0;
    L_0x1var_:
        if (r6 >= r5) goto L_0x1var_;
    L_0x1var_:
        r7 = r4.get(r6);
        r7 = (org.telegram.ui.Cells.ChatMessageCell.PollButton) r7;
        r8 = r7.prevPercent;
        r8 = (float) r8;
        r9 = r7.percent;
        r10 = r7.prevPercent;
        r9 = r9 - r10;
        r9 = (float) r9;
        r10 = r1.pollAnimationProgress;
        r9 = r9 * r10;
        r8 = r8 + r9;
        r8 = (double) r8;
        r8 = java.lang.Math.ceil(r8);
        r8 = (int) r8;
        r7.percent = r8;
        r8 = r7.prevPercentProgress;
        r9 = r7.percentProgress;
        r10 = r7.prevPercentProgress;
        r9 = r9 - r10;
        r10 = r1.pollAnimationProgress;
        r9 = r9 * r10;
        r8 = r8 + r9;
        r7.percentProgress = r8;
        r6 = r6 + 1;
        goto L_0x1var_;
    L_0x1var_:
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = 0;
    L_0x1var_:
        r5 = r1.animatePollAnswer;
        if (r5 == 0) goto L_0x1f5c;
    L_0x1f5a:
        r5 = 0;
        goto L_0x1f5e;
    L_0x1f5c:
        r5 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x1f5e:
        r1.pollAnimationProgress = r5;
        r5 = r1.animatePollAnswerAlpha;
        if (r5 != 0) goto L_0x1var_;
    L_0x1var_:
        r1.pollVoteInProgress = r13;
        r10 = -1;
        r1.pollVoteInProgressNum = r10;
        r5 = r1.currentAccount;
        r5 = org.telegram.messenger.SendMessagesHelper.getInstance(r5);
        r6 = r1.currentMessageObject;
        r5 = r5.isSendingVote(r6);
        goto L_0x1var_;
    L_0x1var_:
        r10 = -1;
        r5 = 0;
    L_0x1var_:
        r6 = r1.titleLayout;
        if (r6 == 0) goto L_0x1var_;
    L_0x1f7c:
        r6 = r6.getHeight();
        goto L_0x1var_;
    L_0x1var_:
        r6 = 0;
    L_0x1var_:
        r7 = 100;
        r8 = r2.poll;
        r8 = r8.answers;
        r8 = r8.size();
        r11 = r5;
        r7 = r6;
        r5 = 0;
        r6 = 0;
        r9 = 100;
        r10 = 0;
        r12 = 0;
    L_0x1var_:
        if (r5 >= r8) goto L_0x20f9;
    L_0x1var_:
        r15 = new org.telegram.ui.Cells.ChatMessageCell$PollButton;
        r13 = 0;
        r15.<init>(r1, r13);
        r13 = r2.poll;
        r13 = r13.answers;
        r13 = r13.get(r5);
        r13 = (org.telegram.tgnet.TLRPC.TL_pollAnswer) r13;
        r15.answer = r13;
        r13 = new android.text.StaticLayout;
        r61 = r8;
        r8 = r15.answer;
        r8 = r8.text;
        r22 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r14 = r22.getFontMetricsInt();
        r22 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r63 = r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r22 = r5;
        r5 = 0;
        r30 = org.telegram.messenger.Emoji.replaceEmoji(r8, r14, r6, r5);
        r31 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r5 = NUM; // 0x42040000 float:33.0 double:5.47206556E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r32 = r0 - r5;
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r13;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);
        r15.title = r13;
        r5 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = r5 + r7;
        r15.y = r5;
        r5 = r15.title;
        r5 = r5.getHeight();
        r15.height = r5;
        r5 = r1.pollButtons;
        r5.add(r15);
        r3.add(r15);
        r5 = r15.height;
        r6 = NUM; // 0x41d00000 float:26.0 double:5.455228437E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r7 = r7 + r5;
        r5 = r2.results;
        r5 = r5.results;
        r5 = r5.isEmpty();
        if (r5 != 0) goto L_0x2099;
    L_0x2015:
        r5 = r2.results;
        r5 = r5.results;
        r5 = r5.size();
        r6 = 0;
    L_0x201e:
        if (r6 >= r5) goto L_0x2099;
    L_0x2020:
        r8 = r2.results;
        r8 = r8.results;
        r8 = r8.get(r6);
        r8 = (org.telegram.tgnet.TLRPC.TL_pollAnswerVoters) r8;
        r13 = r15.answer;
        r13 = r13.option;
        r14 = r8.option;
        r13 = java.util.Arrays.equals(r13, r14);
        if (r13 == 0) goto L_0x2096;
    L_0x2038:
        r5 = r1.pollVoted;
        if (r5 != 0) goto L_0x2040;
    L_0x203c:
        r5 = r1.pollClosed;
        if (r5 == 0) goto L_0x2068;
    L_0x2040:
        r5 = r2.results;
        r5 = r5.total_voters;
        if (r5 <= 0) goto L_0x2068;
    L_0x2046:
        r6 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r8 = r8.voters;
        r8 = (float) r8;
        r5 = (float) r5;
        r8 = r8 / r5;
        r8 = r8 * r6;
        r15.decimal = r8;
        r5 = r15.decimal;
        r5 = (int) r5;
        r15.percent = r5;
        r5 = r15.decimal;
        r6 = r15.percent;
        r6 = (float) r6;
        r5 = r5 - r6;
        r15.decimal = r5;
        goto L_0x2070;
    L_0x2068:
        r5 = 0;
        r15.percent = r5;
        r5 = 0;
        r15.decimal = r5;
    L_0x2070:
        if (r12 != 0) goto L_0x2077;
    L_0x2072:
        r12 = r15.percent;
        goto L_0x2085;
    L_0x2077:
        r5 = r15.percent;
        if (r5 == 0) goto L_0x2085;
    L_0x207d:
        r5 = r15.percent;
        if (r12 == r5) goto L_0x2085;
    L_0x2083:
        r6 = 1;
        goto L_0x2087;
    L_0x2085:
        r6 = r63;
    L_0x2087:
        r5 = r15.percent;
        r9 = r9 - r5;
        r5 = r15.percent;
        r5 = java.lang.Math.max(r5, r10);
        r10 = r5;
        goto L_0x209b;
    L_0x2096:
        r6 = r6 + 1;
        goto L_0x201e;
    L_0x2099:
        r6 = r63;
    L_0x209b:
        if (r4 == 0) goto L_0x20d2;
    L_0x209d:
        r5 = r4.size();
        r8 = 0;
    L_0x20a2:
        if (r8 >= r5) goto L_0x20d2;
    L_0x20a4:
        r13 = r4.get(r8);
        r13 = (org.telegram.ui.Cells.ChatMessageCell.PollButton) r13;
        r14 = r15.answer;
        r14 = r14.option;
        r24 = r0;
        r0 = r13.answer;
        r0 = r0.option;
        r0 = java.util.Arrays.equals(r14, r0);
        if (r0 == 0) goto L_0x20cd;
    L_0x20be:
        r0 = r13.percent;
        r15.prevPercent = r0;
        r0 = r13.percentProgress;
        r15.prevPercentProgress = r0;
        goto L_0x20d4;
    L_0x20cd:
        r8 = r8 + 1;
        r0 = r24;
        goto L_0x20a2;
    L_0x20d2:
        r24 = r0;
    L_0x20d4:
        if (r11 == 0) goto L_0x20eb;
    L_0x20d6:
        r0 = r15.answer;
        r0 = r0.option;
        r0 = java.util.Arrays.equals(r0, r11);
        if (r0 == 0) goto L_0x20eb;
    L_0x20e2:
        r13 = r22;
        r1.pollVoteInProgressNum = r13;
        r5 = 1;
        r1.pollVoteInProgress = r5;
        r11 = 0;
        goto L_0x20ed;
    L_0x20eb:
        r13 = r22;
    L_0x20ed:
        r5 = r13 + 1;
        r14 = r60;
        r8 = r61;
        r0 = r24;
        r13 = 0;
        r15 = 0;
        goto L_0x1var_;
    L_0x20f9:
        r63 = r6;
        if (r63 == 0) goto L_0x2121;
    L_0x20fd:
        if (r9 == 0) goto L_0x2121;
    L_0x20ff:
        r0 = org.telegram.ui.Cells.-$$Lambda$ChatMessageCell$hzMG4njhE1StYhHOT542pSi6Cf0.INSTANCE;
        java.util.Collections.sort(r3, r0);
        r0 = r3.size();
        r0 = java.lang.Math.min(r9, r0);
        r2 = 0;
    L_0x210d:
        if (r2 >= r0) goto L_0x2121;
    L_0x210f:
        r4 = r3.get(r2);
        r4 = (org.telegram.ui.Cells.ChatMessageCell.PollButton) r4;
        r5 = r4.percent;
        r6 = 1;
        r5 = r5 + r6;
        r4.percent = r5;
        r2 = r2 + 1;
        goto L_0x210d;
    L_0x2121:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x42980000 float:76.0 double:5.51998661E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r2 = r1.pollButtons;
        r2 = r2.size();
        r3 = 0;
    L_0x2131:
        if (r3 >= r2) goto L_0x2159;
    L_0x2133:
        r4 = r1.pollButtons;
        r4 = r4.get(r3);
        r4 = (org.telegram.ui.Cells.ChatMessageCell.PollButton) r4;
        r5 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = (float) r5;
        r6 = (float) r0;
        r5 = r5 / r6;
        if (r10 == 0) goto L_0x214e;
    L_0x2146:
        r6 = r4.percent;
        r6 = (float) r6;
        r8 = (float) r10;
        r6 = r6 / r8;
        goto L_0x214f;
    L_0x214e:
        r6 = 0;
    L_0x214f:
        r5 = java.lang.Math.max(r5, r6);
        r4.percentProgress = r5;
        r3 = r3 + 1;
        goto L_0x2131;
    L_0x2159:
        r59.setMessageObjectInternal(r60);
        r0 = NUM; // 0x42920000 float:73.0 double:5.518043864E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r0 = r0 + r7;
        r1.totalHeight = r0;
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x2175;
    L_0x216c:
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r0;
        r1.namesOffset = r2;
    L_0x2175:
        r14 = r60;
        r9 = 0;
        r15 = 0;
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x379b;
    L_0x217d:
        r0 = r14.messageOwner;
        r0 = r0.fwd_from;
        if (r0 == 0) goto L_0x218b;
    L_0x2183:
        r0 = r60.isAnyKindOfSticker();
        if (r0 != 0) goto L_0x218b;
    L_0x2189:
        r0 = 1;
        goto L_0x218c;
    L_0x218b:
        r0 = 0;
    L_0x218c:
        r1.drawForwardedName = r0;
        r0 = r14.type;
        r2 = 9;
        if (r0 == r2) goto L_0x2196;
    L_0x2194:
        r0 = 1;
        goto L_0x2197;
    L_0x2196:
        r0 = 0;
    L_0x2197:
        r1.mediaBackground = r0;
        r2 = 1;
        r1.drawImageButton = r2;
        r1.drawPhotoImage = r2;
        r0 = r14.gifState;
        r0 = (r0 > r19 ? 1 : (r0 == r19 ? 0 : -1));
        if (r0 == 0) goto L_0x21b6;
    L_0x21a4:
        r0 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r0 != 0) goto L_0x21b6;
    L_0x21a8:
        r0 = r14.type;
        r7 = 8;
        if (r0 == r7) goto L_0x21b1;
    L_0x21ae:
        r2 = 5;
        if (r0 != r2) goto L_0x21b8;
    L_0x21b1:
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r14.gifState = r11;
        goto L_0x21ba;
    L_0x21b6:
        r7 = 8;
    L_0x21b8:
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x21ba:
        r0 = r1.photoImage;
        r2 = 1;
        r0.setAllowDecodeSingleFrame(r2);
        r0 = r60.isVideo();
        if (r0 == 0) goto L_0x21cc;
    L_0x21c6:
        r0 = r1.photoImage;
        r0.setAllowStartAnimation(r2);
        goto L_0x21fb;
    L_0x21cc:
        r0 = r60.isRoundVideo();
        if (r0 == 0) goto L_0x21ec;
    L_0x21d2:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r0 = r0.getPlayingMessageObject();
        r2 = r1.photoImage;
        if (r0 == 0) goto L_0x21e7;
    L_0x21de:
        r0 = r0.isRoundVideo();
        if (r0 != 0) goto L_0x21e5;
    L_0x21e4:
        goto L_0x21e7;
    L_0x21e5:
        r0 = 0;
        goto L_0x21e8;
    L_0x21e7:
        r0 = 1;
    L_0x21e8:
        r2.setAllowStartAnimation(r0);
        goto L_0x21fb;
    L_0x21ec:
        r0 = r1.photoImage;
        r2 = r14.gifState;
        r4 = 0;
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r2 != 0) goto L_0x21f7;
    L_0x21f5:
        r2 = 1;
        goto L_0x21f8;
    L_0x21f7:
        r2 = 0;
    L_0x21f8:
        r0.setAllowStartAnimation(r2);
    L_0x21fb:
        r0 = r1.photoImage;
        r2 = r60.needDrawBluredPreview();
        r0.setForcePreview(r2);
        r0 = r14.type;
        r2 = 9;
        if (r0 != r2) goto L_0x2413;
    L_0x220a:
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x2239;
    L_0x2210:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x2225;
    L_0x2218:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x2225;
    L_0x221e:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x2225;
    L_0x2224:
        goto L_0x2227;
    L_0x2225:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x2227:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43960000 float:300.0 double:5.60222949E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
        goto L_0x2261;
    L_0x2239:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r0.x;
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x224e;
    L_0x2241:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x224e;
    L_0x2247:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x224e;
    L_0x224d:
        goto L_0x2250;
    L_0x224e:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x2250:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        r2 = NUM; // 0x43960000 float:300.0 double:5.60222949E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = java.lang.Math.min(r0, r2);
        r1.backgroundWidth = r0;
    L_0x2261:
        r0 = r59.checkNeedDrawShareButton(r60);
        if (r0 == 0) goto L_0x2272;
    L_0x2267:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r1.backgroundWidth = r0;
    L_0x2272:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x430a0000 float:138.0 double:5.55689877E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r0 - r2;
        r1.createDocumentLayout(r2, r14);
        r0 = r14.caption;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 != 0) goto L_0x22e9;
    L_0x2287:
        r0 = r14.caption;	 Catch:{ Exception -> 0x22e3 }
        r1.currentCaption = r0;	 Catch:{ Exception -> 0x22e3 }
        r0 = r1.backgroundWidth;	 Catch:{ Exception -> 0x22e3 }
        r3 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x22e3 }
        r0 = r0 - r3;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r20);	 Catch:{ Exception -> 0x22e3 }
        r3 = r0 - r3;
        r0 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x22e0 }
        r4 = 24;
        if (r0 < r4) goto L_0x22c5;
    L_0x22a0:
        r0 = r14.caption;	 Catch:{ Exception -> 0x22e0 }
        r4 = r14.caption;	 Catch:{ Exception -> 0x22e0 }
        r4 = r4.length();	 Catch:{ Exception -> 0x22e0 }
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x22e0 }
        r6 = 0;
        r0 = android.text.StaticLayout.Builder.obtain(r0, r6, r4, r5, r3);	 Catch:{ Exception -> 0x22e0 }
        r4 = 1;
        r0 = r0.setBreakStrategy(r4);	 Catch:{ Exception -> 0x22e0 }
        r0 = r0.setHyphenationFrequency(r6);	 Catch:{ Exception -> 0x22e0 }
        r4 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x22e0 }
        r0 = r0.setAlignment(r4);	 Catch:{ Exception -> 0x22e0 }
        r0 = r0.build();	 Catch:{ Exception -> 0x22e0 }
        r1.captionLayout = r0;	 Catch:{ Exception -> 0x22e0 }
        goto L_0x22de;
    L_0x22c5:
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x22e0 }
        r4 = r14.caption;	 Catch:{ Exception -> 0x22e0 }
        r31 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x22e0 }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x22e0 }
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r0;
        r30 = r4;
        r32 = r3;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);	 Catch:{ Exception -> 0x22e0 }
        r1.captionLayout = r0;	 Catch:{ Exception -> 0x22e0 }
    L_0x22de:
        r12 = r3;
        goto L_0x22ea;
    L_0x22e0:
        r0 = move-exception;
        r12 = r3;
        goto L_0x22e5;
    L_0x22e3:
        r0 = move-exception;
        r12 = 0;
    L_0x22e5:
        org.telegram.messenger.FileLog.e(r0);
        goto L_0x22ea;
    L_0x22e9:
        r12 = 0;
    L_0x22ea:
        r0 = r1.docTitleLayout;
        if (r0 == 0) goto L_0x2321;
    L_0x22ee:
        r0 = r0.getLineCount();
        r3 = 0;
        r4 = 0;
    L_0x22f4:
        if (r3 >= r0) goto L_0x2322;
    L_0x22f6:
        r5 = r1.docTitleLayout;
        r5 = r5.getLineWidth(r3);
        r6 = r1.docTitleLayout;
        r6 = r6.getLineLeft(r3);
        r5 = r5 + r6;
        r5 = (double) r5;
        r5 = java.lang.Math.ceil(r5);
        r5 = (int) r5;
        r6 = r1.drawPhotoImage;
        if (r6 == 0) goto L_0x2310;
    L_0x230d:
        r6 = 52;
        goto L_0x2312;
    L_0x2310:
        r6 = 22;
    L_0x2312:
        r6 = r6 + 86;
        r6 = (float) r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r4 = java.lang.Math.max(r4, r5);
        r3 = r3 + 1;
        goto L_0x22f4;
    L_0x2321:
        r4 = 0;
    L_0x2322:
        r0 = r1.infoLayout;
        if (r0 == 0) goto L_0x2351;
    L_0x2326:
        r0 = r0.getLineCount();
        r3 = 0;
    L_0x232b:
        if (r3 >= r0) goto L_0x2351;
    L_0x232d:
        r5 = r1.infoLayout;
        r5 = r5.getLineWidth(r3);
        r5 = (double) r5;
        r5 = java.lang.Math.ceil(r5);
        r5 = (int) r5;
        r6 = r1.drawPhotoImage;
        if (r6 == 0) goto L_0x2340;
    L_0x233d:
        r6 = 52;
        goto L_0x2342;
    L_0x2340:
        r6 = 22;
    L_0x2342:
        r6 = r6 + 86;
        r6 = (float) r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r4 = java.lang.Math.max(r4, r5);
        r3 = r3 + 1;
        goto L_0x232b;
    L_0x2351:
        r0 = r1.captionLayout;
        if (r0 == 0) goto L_0x2381;
    L_0x2355:
        r0 = r0.getLineCount();
        r3 = 0;
    L_0x235a:
        if (r3 >= r0) goto L_0x2381;
    L_0x235c:
        r5 = (float) r12;
        r6 = r1.captionLayout;
        r6 = r6.getLineWidth(r3);
        r8 = r1.captionLayout;
        r8 = r8.getLineLeft(r3);
        r6 = r6 + r8;
        r5 = java.lang.Math.min(r5, r6);
        r5 = (double) r5;
        r5 = java.lang.Math.ceil(r5);
        r5 = (int) r5;
        r6 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        if (r5 <= r4) goto L_0x237e;
    L_0x237d:
        r4 = r5;
    L_0x237e:
        r3 = r3 + 1;
        goto L_0x235a;
    L_0x2381:
        if (r4 <= 0) goto L_0x238d;
    L_0x2383:
        r1.backgroundWidth = r4;
        r0 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r4 - r0;
    L_0x238d:
        r0 = r1.drawPhotoImage;
        if (r0 == 0) goto L_0x239e;
    L_0x2391:
        r0 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r3 = NUM; // 0x42aCLASSNAME float:86.0 double:5.526462427E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        goto L_0x23c5;
    L_0x239e:
        r0 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r3 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.docTitleLayout;
        if (r4 == 0) goto L_0x23c5;
    L_0x23ae:
        r4 = r4.getLineCount();
        r5 = 1;
        if (r4 <= r5) goto L_0x23c5;
    L_0x23b5:
        r4 = r1.docTitleLayout;
        r4 = r4.getLineCount();
        r4 = r4 - r5;
        r5 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 * r5;
        r3 = r3 + r4;
    L_0x23c5:
        r1.availableTimeWidth = r2;
        r2 = r1.drawPhotoImage;
        if (r2 != 0) goto L_0x240b;
    L_0x23cb:
        r2 = r14.caption;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 == 0) goto L_0x240b;
    L_0x23d3:
        r2 = r1.infoLayout;
        if (r2 == 0) goto L_0x240b;
    L_0x23d7:
        r2 = r2.getLineCount();
        r59.measureTime(r60);
        r4 = r1.backgroundWidth;
        r5 = NUM; // 0x42var_ float:122.0 double:5.54977537E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r5 = r1.infoLayout;
        r6 = 0;
        r5 = r5.getLineWidth(r6);
        r5 = (double) r5;
        r5 = java.lang.Math.ceil(r5);
        r5 = (int) r5;
        r4 = r4 - r5;
        r5 = r1.timeWidth;
        if (r4 >= r5) goto L_0x2401;
    L_0x23f9:
        r2 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
    L_0x23ff:
        r3 = r3 + r2;
        goto L_0x240b;
    L_0x2401:
        r4 = 1;
        if (r2 != r4) goto L_0x240b;
    L_0x2404:
        r2 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        goto L_0x23ff;
    L_0x240b:
        r12 = 0;
        r15 = 0;
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r57 = 0;
        goto L_0x36a1;
    L_0x2413:
        r2 = 4;
        if (r0 != r2) goto L_0x296a;
    L_0x2416:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.geo;
        r4 = r0.lat;
        r8 = r0._long;
        r12 = r60.getDialogId();
        r2 = (int) r12;
        if (r2 != 0) goto L_0x2439;
    L_0x2427:
        r2 = org.telegram.messenger.SharedConfig.mapPreviewType;
        if (r2 != 0) goto L_0x242c;
    L_0x242b:
        goto L_0x2439;
    L_0x242c:
        r10 = 1;
        if (r2 != r10) goto L_0x2433;
    L_0x242f:
        r13 = 4;
        r38 = 4;
        goto L_0x243b;
    L_0x2433:
        r10 = 3;
        if (r2 != r10) goto L_0x2439;
    L_0x2436:
        r38 = 1;
        goto L_0x243b;
    L_0x2439:
        r38 = -1;
    L_0x243b:
        r2 = r14.messageOwner;
        r2 = r2.media;
        r10 = r2 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
        if (r10 == 0) goto L_0x267b;
    L_0x2443:
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x2473;
    L_0x2449:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r10 = r1.isChat;
        if (r10 == 0) goto L_0x245e;
    L_0x2451:
        r10 = r60.needDrawAvatar();
        if (r10 == 0) goto L_0x245e;
    L_0x2457:
        r10 = r60.isOutOwner();
        if (r10 != 0) goto L_0x245e;
    L_0x245d:
        goto L_0x2460;
    L_0x245e:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x2460:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
        goto L_0x249c;
    L_0x2473:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r2 = r2.x;
        r10 = r1.isChat;
        if (r10 == 0) goto L_0x2488;
    L_0x247b:
        r10 = r60.needDrawAvatar();
        if (r10 == 0) goto L_0x2488;
    L_0x2481:
        r10 = r60.isOutOwner();
        if (r10 != 0) goto L_0x2488;
    L_0x2487:
        goto L_0x248a;
    L_0x2488:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x248a:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
    L_0x249c:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
        r2 = r59.checkNeedDrawShareButton(r60);
        if (r2 == 0) goto L_0x24b8;
    L_0x24ad:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
    L_0x24b8:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x42140000 float:37.0 double:5.477246216E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.availableTimeWidth = r2;
        r3 = NUM; // 0x42580000 float:54.0 double:5.499263994E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = r1.backgroundWidth;
        r10 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r3 = r3 - r10;
        r10 = NUM; // 0x43430000 float:195.0 double:5.575354847E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r12 = NUM; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r12 = (double) r12;
        r26 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        java.lang.Double.isNaN(r12);
        r26 = r12 / r26;
        r29 = NUM; // 0x3ffNUM float:0.0 double:1.0;
        r31 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        r4 = r4 * r31;
        r31 = NUM; // 0xNUM float:0.0 double:180.0;
        r4 = r4 / r31;
        r31 = java.lang.Math.sin(r4);
        r31 = r31 + r29;
        r4 = java.lang.Math.sin(r4);
        r29 = r29 - r4;
        r31 = r31 / r29;
        r4 = java.lang.Math.log(r31);
        r4 = r4 * r26;
        r29 = NUM; // 0xNUM float:0.0 double:2.0;
        r4 = r4 / r29;
        java.lang.Double.isNaN(r12);
        r4 = r12 - r4;
        r4 = java.lang.Math.round(r4);
        r15 = NUM; // 0x4124cccd float:10.3 double:5.399795443E-315;
        r15 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r6 = 6;
        r6 = r15 << 6;
        r39 = r8;
        r7 = (long) r6;
        r4 = r4 - r7;
        r4 = (double) r4;
        r6 = NUM; // 0x3fvar_fb54442d18 float:3.37028055E12 double:1.NUM;
        r8 = NUM; // 0xNUM float:0.0 double:2.0;
        java.lang.Double.isNaN(r4);
        java.lang.Double.isNaN(r12);
        r4 = r4 - r12;
        r4 = r4 / r26;
        r4 = java.lang.Math.exp(r4);
        r4 = java.lang.Math.atan(r4);
        r4 = r4 * r8;
        r6 = r6 - r4;
        r4 = NUM; // 0xNUM float:0.0 double:180.0;
        r6 = r6 * r4;
        r4 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        r4 = r6 / r4;
        r6 = r1.currentAccount;
        r7 = (float) r3;
        r8 = org.telegram.messenger.AndroidUtilities.density;
        r9 = r7 / r8;
        r9 = (int) r9;
        r12 = (float) r10;
        r8 = r12 / r8;
        r8 = (int) r8;
        r36 = 0;
        r37 = 15;
        r29 = r6;
        r30 = r4;
        r32 = r39;
        r34 = r9;
        r35 = r8;
        r6 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r29, r30, r32, r34, r35, r36, r37, r38);
        r1.currentUrl = r6;
        r8 = r0.access_hash;
        r0 = org.telegram.messenger.AndroidUtilities.density;
        r7 = r7 / r0;
        r6 = (int) r7;
        r12 = r12 / r0;
        r7 = (int) r12;
        r12 = (double) r0;
        r12 = java.lang.Math.ceil(r12);
        r0 = (int) r12;
        r12 = 2;
        r38 = java.lang.Math.min(r12, r0);
        r29 = r4;
        r31 = r39;
        r33 = r8;
        r35 = r6;
        r36 = r7;
        r0 = org.telegram.messenger.WebFile.createWithGeoPoint(r29, r31, r33, r35, r36, r37, r38);
        r1.currentWebFile = r0;
        r0 = r59.isCurrentLocationTimeExpired(r60);
        r1.locationExpired = r0;
        if (r0 != 0) goto L_0x25b3;
    L_0x259a:
        r0 = r1.photoImage;
        r4 = 1;
        r0.setCrossfadeWithOldImage(r4);
        r5 = 0;
        r1.mediaBackground = r5;
        r0 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = r1.invalidateRunnable;
        r5 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r0, r5);
        r1.scheduledInvalidate = r4;
        goto L_0x25bf;
    L_0x25b3:
        r0 = r1.backgroundWidth;
        r4 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        r1.backgroundWidth = r0;
        r12 = 0;
    L_0x25bf:
        r0 = new android.text.StaticLayout;
        r4 = NUM; // 0x7f0e0154 float:1.8875727E38 double:1.0531623246E-314;
        r5 = "AttachLiveLocation";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r5 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r6 = (float) r2;
        r7 = android.text.TextUtils.TruncateAt.END;
        r30 = android.text.TextUtils.ellipsize(r4, r5, r6, r7);
        r31 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r0;
        r32 = r2;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);
        r1.docTitleLayout = r0;
        r59.updateCurrentUserAndChat();
        r0 = r1.currentUser;
        if (r0 == 0) goto L_0x260f;
    L_0x25ed:
        r4 = r1.contactAvatarDrawable;
        r4.setInfo(r0);
        r0 = r1.locationImageReceiver;
        r4 = r1.currentUser;
        r5 = 0;
        r30 = org.telegram.messenger.ImageLocation.getForUser(r4, r5);
        r4 = r1.contactAvatarDrawable;
        r33 = 0;
        r5 = r1.currentUser;
        r35 = 0;
        r31 = "50_50";
        r29 = r0;
        r32 = r4;
        r34 = r5;
        r29.setImage(r30, r31, r32, r33, r34, r35);
        goto L_0x2654;
    L_0x260f:
        r0 = r1.currentChat;
        if (r0 == 0) goto L_0x263f;
    L_0x2613:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x261b;
    L_0x2617:
        r0 = r0.photo_small;
        r1.currentPhoto = r0;
    L_0x261b:
        r0 = r1.contactAvatarDrawable;
        r4 = r1.currentChat;
        r0.setInfo(r4);
        r0 = r1.locationImageReceiver;
        r4 = r1.currentChat;
        r5 = 0;
        r30 = org.telegram.messenger.ImageLocation.getForChat(r4, r5);
        r4 = r1.contactAvatarDrawable;
        r33 = 0;
        r5 = r1.currentChat;
        r35 = 0;
        r31 = "50_50";
        r29 = r0;
        r32 = r4;
        r34 = r5;
        r29.setImage(r30, r31, r32, r33, r34, r35);
        goto L_0x2654;
    L_0x263f:
        r0 = r1.locationImageReceiver;
        r37 = 0;
        r38 = 0;
        r4 = r1.contactAvatarDrawable;
        r40 = 0;
        r41 = 0;
        r42 = 0;
        r36 = r0;
        r39 = r4;
        r36.setImage(r37, r38, r39, r40, r41, r42);
    L_0x2654:
        r0 = new android.text.StaticLayout;
        r4 = r14.messageOwner;
        r5 = r4.edit_date;
        if (r5 == 0) goto L_0x265e;
    L_0x265c:
        r4 = (long) r5;
        goto L_0x2661;
    L_0x265e:
        r4 = r4.date;
        r4 = (long) r4;
    L_0x2661:
        r30 = org.telegram.messenger.LocaleController.formatLocationUpdateDate(r4);
        r31 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r0;
        r32 = r2;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);
        r1.infoLayout = r0;
        r0 = r3;
        goto L_0x2802;
    L_0x267b:
        r39 = r8;
        r2 = r2.title;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 != 0) goto L_0x2805;
    L_0x2685:
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x26b5;
    L_0x268b:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = r1.isChat;
        if (r6 == 0) goto L_0x26a0;
    L_0x2693:
        r6 = r60.needDrawAvatar();
        if (r6 == 0) goto L_0x26a0;
    L_0x2699:
        r6 = r60.isOutOwner();
        if (r6 != 0) goto L_0x26a0;
    L_0x269f:
        goto L_0x26a2;
    L_0x26a0:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x26a2:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
        goto L_0x26de;
    L_0x26b5:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r2 = r2.x;
        r6 = r1.isChat;
        if (r6 == 0) goto L_0x26ca;
    L_0x26bd:
        r6 = r60.needDrawAvatar();
        if (r6 == 0) goto L_0x26ca;
    L_0x26c3:
        r6 = r60.isOutOwner();
        if (r6 != 0) goto L_0x26ca;
    L_0x26c9:
        goto L_0x26cc;
    L_0x26ca:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x26cc:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
    L_0x26de:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
        r2 = r59.checkNeedDrawShareButton(r60);
        if (r2 == 0) goto L_0x26fa;
    L_0x26ef:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
    L_0x26fa:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.availableTimeWidth = r2;
        r3 = r1.backgroundWidth;
        r6 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r3 = r3 - r6;
        r6 = NUM; // 0x43430000 float:195.0 double:5.575354847E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r7 = 0;
        r1.mediaBackground = r7;
        r7 = r1.currentAccount;
        r8 = (float) r3;
        r9 = org.telegram.messenger.AndroidUtilities.density;
        r10 = r8 / r9;
        r10 = (int) r10;
        r12 = (float) r6;
        r9 = r12 / r9;
        r9 = (int) r9;
        r36 = 1;
        r37 = 15;
        r29 = r7;
        r30 = r4;
        r32 = r39;
        r34 = r10;
        r35 = r9;
        r4 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r29, r30, r32, r34, r35, r36, r37, r38);
        r1.currentUrl = r4;
        r4 = org.telegram.messenger.AndroidUtilities.density;
        r8 = r8 / r4;
        r5 = (int) r8;
        r12 = r12 / r4;
        r7 = (int) r12;
        r8 = 15;
        r9 = (double) r4;
        r9 = java.lang.Math.ceil(r9);
        r4 = (int) r9;
        r9 = 2;
        r4 = java.lang.Math.min(r9, r4);
        r0 = org.telegram.messenger.WebFile.createWithGeoPoint(r0, r5, r7, r8, r4);
        r1.currentWebFile = r0;
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.title;
        r42 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r4 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r43 = r2 + r4;
        r44 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r45 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r46 = 0;
        r47 = 0;
        r48 = android.text.TextUtils.TruncateAt.END;
        r50 = 1;
        r41 = r0;
        r49 = r2;
        r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r41, r42, r43, r44, r45, r46, r47, r48, r49, r50);
        r1.docTitleLayout = r0;
        r7 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r4 = 0;
        r12 = r4 + r0;
        r0 = r1.docTitleLayout;
        r0.getLineCount();
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.address;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 != 0) goto L_0x27fd;
    L_0x2790:
        r0 = r14.messageOwner;
        r0 = r0.media;
        r0 = r0.address;
        r42 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r44 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r45 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r46 = 0;
        r47 = 0;
        r48 = android.text.TextUtils.TruncateAt.END;
        r50 = 1;
        r41 = r0;
        r43 = r2;
        r49 = r2;
        r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r41, r42, r43, r44, r45, r46, r47, r48, r49, r50);
        r1.infoLayout = r0;
        r59.measureTime(r60);
        r0 = r1.backgroundWidth;
        r2 = r1.infoLayout;
        r4 = 0;
        r2 = r2.getLineWidth(r4);
        r7 = (double) r2;
        r7 = java.lang.Math.ceil(r7);
        r2 = (int) r7;
        r0 = r0 - r2;
        r2 = NUM; // 0x41CLASSNAME float:24.0 double:5.450047783E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r2 = r1.infoLayout;
        r2 = r2.getLineLeft(r4);
        r4 = 0;
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r2 <= 0) goto L_0x27d7;
    L_0x27d5:
        r2 = 1;
        goto L_0x27d8;
    L_0x27d7:
        r2 = 0;
    L_0x27d8:
        if (r2 != 0) goto L_0x27f0;
    L_0x27da:
        r4 = r1.timeWidth;
        r5 = r60.isOutOwner();
        if (r5 == 0) goto L_0x27e5;
    L_0x27e2:
        r5 = 20;
        goto L_0x27e6;
    L_0x27e5:
        r5 = 0;
    L_0x27e6:
        r5 = r5 + 20;
        r5 = (float) r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 + r5;
        if (r0 >= r4) goto L_0x2800;
    L_0x27f0:
        if (r2 == 0) goto L_0x27f5;
    L_0x27f2:
        r0 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        goto L_0x27f7;
    L_0x27f5:
        r0 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
    L_0x27f7:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r12 = r12 + r0;
        goto L_0x2800;
    L_0x27fd:
        r2 = 0;
        r1.infoLayout = r2;
    L_0x2800:
        r0 = r3;
        r10 = r6;
    L_0x2802:
        r8 = 2;
        goto L_0x28d0;
    L_0x2805:
        r7 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x2837;
    L_0x280d:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = r1.isChat;
        if (r6 == 0) goto L_0x2822;
    L_0x2815:
        r6 = r60.needDrawAvatar();
        if (r6 == 0) goto L_0x2822;
    L_0x281b:
        r6 = r60.isOutOwner();
        if (r6 != 0) goto L_0x2822;
    L_0x2821:
        goto L_0x2824;
    L_0x2822:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x2824:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
        goto L_0x2860;
    L_0x2837:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r2 = r2.x;
        r6 = r1.isChat;
        if (r6 == 0) goto L_0x284c;
    L_0x283f:
        r6 = r60.needDrawAvatar();
        if (r6 == 0) goto L_0x284c;
    L_0x2845:
        r6 = r60.isOutOwner();
        if (r6 != 0) goto L_0x284c;
    L_0x284b:
        goto L_0x284e;
    L_0x284c:
        r3 = NUM; // 0x42480000 float:50.0 double:5.49408334E-315;
    L_0x284e:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = NUM; // 0x43908000 float:289.0 double:5.60044864E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = java.lang.Math.min(r2, r3);
        r1.backgroundWidth = r2;
    L_0x2860:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
        r2 = r59.checkNeedDrawShareButton(r60);
        if (r2 == 0) goto L_0x287c;
    L_0x2871:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.backgroundWidth = r2;
    L_0x287c:
        r2 = r1.backgroundWidth;
        r3 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.availableTimeWidth = r2;
        r2 = r1.backgroundWidth;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r2 = r2 - r3;
        r3 = NUM; // 0x43430000 float:195.0 double:5.575354847E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r6 = r1.currentAccount;
        r7 = (float) r2;
        r8 = org.telegram.messenger.AndroidUtilities.density;
        r9 = r7 / r8;
        r9 = (int) r9;
        r10 = (float) r3;
        r8 = r10 / r8;
        r8 = (int) r8;
        r36 = 1;
        r37 = 15;
        r29 = r6;
        r30 = r4;
        r32 = r39;
        r34 = r9;
        r35 = r8;
        r4 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r29, r30, r32, r34, r35, r36, r37, r38);
        r1.currentUrl = r4;
        r4 = org.telegram.messenger.AndroidUtilities.density;
        r7 = r7 / r4;
        r5 = (int) r7;
        r10 = r10 / r4;
        r6 = (int) r10;
        r7 = 15;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r8 = 2;
        r4 = java.lang.Math.min(r8, r4);
        r0 = org.telegram.messenger.WebFile.createWithGeoPoint(r0, r5, r6, r7, r4);
        r1.currentWebFile = r0;
        r0 = r2;
        r10 = r3;
        r12 = 0;
    L_0x28d0:
        r2 = r60.getDialogId();
        r3 = (int) r2;
        if (r3 != 0) goto L_0x28ef;
    L_0x28d7:
        r2 = org.telegram.messenger.SharedConfig.mapPreviewType;
        if (r2 != 0) goto L_0x28df;
    L_0x28db:
        r1.currentMapProvider = r8;
    L_0x28dd:
        r2 = -1;
        goto L_0x28fa;
    L_0x28df:
        r3 = 1;
        if (r2 != r3) goto L_0x28e5;
    L_0x28e2:
        r1.currentMapProvider = r3;
        goto L_0x28dd;
    L_0x28e5:
        r4 = 3;
        if (r2 != r4) goto L_0x28eb;
    L_0x28e8:
        r1.currentMapProvider = r3;
        goto L_0x28dd;
    L_0x28eb:
        r2 = -1;
        r1.currentMapProvider = r2;
        goto L_0x28fa;
    L_0x28ef:
        r2 = -1;
        r3 = r14.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r3 = r3.mapProvider;
        r1.currentMapProvider = r3;
    L_0x28fa:
        r3 = r1.currentMapProvider;
        if (r3 != r2) goto L_0x2914;
    L_0x28fe:
        r2 = r1.photoImage;
        r3 = 0;
        r4 = 0;
        r5 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r6 = r60.isOutOwner();
        r5 = r5[r6];
        r6 = 0;
        r8 = 0;
        r15 = 8;
        r7 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8);
        goto L_0x2961;
    L_0x2914:
        r2 = 2;
        r15 = 8;
        if (r3 != r2) goto L_0x2939;
    L_0x2919:
        r2 = r1.currentWebFile;
        if (r2 == 0) goto L_0x2961;
    L_0x291d:
        r3 = r1.photoImage;
        r4 = org.telegram.messenger.ImageLocation.getForWebFile(r2);
        r5 = 0;
        r2 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r6 = r60.isOutOwner();
        r6 = r2[r6];
        r7 = 0;
        r8 = 0;
        r2 = r3;
        r3 = r4;
        r4 = r5;
        r5 = r6;
        r6 = r7;
        r7 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8);
        goto L_0x2961;
    L_0x2939:
        r2 = 3;
        if (r3 == r2) goto L_0x293f;
    L_0x293c:
        r2 = 4;
        if (r3 != r2) goto L_0x294d;
    L_0x293f:
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r3 = r1.currentUrl;
        r4 = r1.currentWebFile;
        r2.addTestWebFile(r3, r4);
        r2 = 1;
        r1.addedForTest = r2;
    L_0x294d:
        r4 = r1.currentUrl;
        if (r4 == 0) goto L_0x2961;
    L_0x2951:
        r3 = r1.photoImage;
        r5 = 0;
        r2 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r6 = r60.isOutOwner();
        r6 = r2[r6];
        r7 = 0;
        r8 = 0;
        r3.setImage(r4, r5, r6, r7, r8);
    L_0x2961:
        r3 = r10;
        r57 = r12;
        r12 = 0;
        r15 = 0;
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x36a1;
    L_0x296a:
        r15 = 8;
        r0 = r60.isAnyKindOfSticker();
        if (r0 == 0) goto L_0x2b51;
    L_0x2972:
        r2 = 0;
        r1.drawBackground = r2;
        r0 = r14.type;
        r2 = 13;
        if (r0 != r2) goto L_0x297d;
    L_0x297b:
        r0 = 1;
        goto L_0x297e;
    L_0x297d:
        r0 = 0;
    L_0x297e:
        r2 = 0;
    L_0x297f:
        r3 = r60.getDocument();
        r3 = r3.attributes;
        r3 = r3.size();
        if (r2 >= r3) goto L_0x29a3;
    L_0x298b:
        r3 = r60.getDocument();
        r3 = r3.attributes;
        r3 = r3.get(r2);
        r3 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r3;
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 == 0) goto L_0x29a0;
    L_0x299b:
        r12 = r3.w;
        r2 = r3.h;
        goto L_0x29a5;
    L_0x29a0:
        r2 = r2 + 1;
        goto L_0x297f;
    L_0x29a3:
        r2 = 0;
        r12 = 0;
    L_0x29a5:
        r3 = r60.isAnimatedSticker();
        if (r3 == 0) goto L_0x29b3;
    L_0x29ab:
        if (r12 != 0) goto L_0x29b3;
    L_0x29ad:
        if (r2 != 0) goto L_0x29b3;
    L_0x29af:
        r12 = 512; // 0x200 float:7.175E-43 double:2.53E-321;
        r2 = 512; // 0x200 float:7.175E-43 double:2.53E-321;
    L_0x29b3:
        r3 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r3 == 0) goto L_0x29c2;
    L_0x29b9:
        r3 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r3 = (float) r3;
        r4 = NUM; // 0x3ecccccd float:0.4 double:5.205520926E-315;
        goto L_0x29cf;
    L_0x29c2:
        r3 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r3.x;
        r3 = r3.y;
        r3 = java.lang.Math.min(r4, r3);
        r3 = (float) r3;
        r4 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
    L_0x29cf:
        r3 = r3 * r4;
        r4 = r60.isAnimatedEmoji();
        if (r4 == 0) goto L_0x29f2;
    L_0x29d7:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r4 = r4.animatedEmojisZoom;
        r5 = (float) r12;
        r7 = NUM; // 0x44000000 float:512.0 double:5.63655132E-315;
        r5 = r5 / r7;
        r5 = r5 * r3;
        r5 = r5 * r4;
        r5 = (int) r5;
        r2 = (float) r2;
        r2 = r2 / r7;
        r2 = r2 * r3;
        r2 = r2 * r4;
        r2 = (int) r2;
        r3 = r2;
        r2 = r5;
        goto L_0x2a13;
    L_0x29f2:
        if (r12 != 0) goto L_0x29fd;
    L_0x29f4:
        r2 = (int) r3;
        r4 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r12 = r2 + r4;
    L_0x29fd:
        r2 = (float) r2;
        r4 = (float) r12;
        r4 = r3 / r4;
        r2 = r2 * r4;
        r2 = (int) r2;
        r4 = (int) r3;
        r5 = (float) r2;
        r7 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1));
        if (r7 <= 0) goto L_0x2a11;
    L_0x2a0a:
        r2 = (float) r4;
        r3 = r3 / r5;
        r2 = r2 * r3;
        r2 = (int) r2;
        r3 = r4;
        goto L_0x2a13;
    L_0x2a11:
        r3 = r2;
        r2 = r4;
    L_0x2a13:
        r4 = r60.isAnimatedEmoji();
        if (r4 == 0) goto L_0x2a65;
    L_0x2a19:
        r4 = java.util.Locale.US;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r7 = "%d_%d_nr_%s";
        r5.append(r7);
        r7 = r14.emojiAnimatedStickerColor;
        r5.append(r7);
        r5 = r5.toString();
        r7 = 3;
        r8 = new java.lang.Object[r7];
        r7 = java.lang.Integer.valueOf(r2);
        r9 = 0;
        r8[r9] = r7;
        r7 = java.lang.Integer.valueOf(r3);
        r9 = 1;
        r8[r9] = r7;
        r7 = r60.toString();
        r9 = 2;
        r8[r9] = r7;
        r4 = java.lang.String.format(r4, r5, r8);
        r5 = r1.photoImage;
        r7 = r1.delegate;
        r7 = r7.shouldRepeatSticker(r14);
        if (r7 == 0) goto L_0x2a56;
    L_0x2a54:
        r7 = 2;
        goto L_0x2a57;
    L_0x2a56:
        r7 = 3;
    L_0x2a57:
        r5.setAutoRepeat(r7);
        r5 = r14.emojiAnimatedSticker;
        r5 = org.telegram.messenger.MessageObject.getInputStickerSet(r5);
        r31 = r4;
        r36 = r5;
        goto L_0x2abf;
    L_0x2a65:
        r4 = org.telegram.messenger.SharedConfig.loopStickers;
        if (r4 != 0) goto L_0x2a9d;
    L_0x2a69:
        if (r0 == 0) goto L_0x2a6c;
    L_0x2a6b:
        goto L_0x2a9d;
    L_0x2a6c:
        r4 = java.util.Locale.US;
        r5 = 3;
        r7 = new java.lang.Object[r5];
        r5 = java.lang.Integer.valueOf(r2);
        r8 = 0;
        r7[r8] = r5;
        r5 = java.lang.Integer.valueOf(r3);
        r8 = 1;
        r7[r8] = r5;
        r5 = r60.toString();
        r8 = 2;
        r7[r8] = r5;
        r5 = "%d_%d_nr_%s";
        r4 = java.lang.String.format(r4, r5, r7);
        r5 = r1.photoImage;
        r7 = r1.delegate;
        r7 = r7.shouldRepeatSticker(r14);
        if (r7 == 0) goto L_0x2a98;
    L_0x2a96:
        r7 = 2;
        goto L_0x2a99;
    L_0x2a98:
        r7 = 3;
    L_0x2a99:
        r5.setAutoRepeat(r7);
        goto L_0x2abb;
    L_0x2a9d:
        r4 = java.util.Locale.US;
        r5 = 2;
        r7 = new java.lang.Object[r5];
        r5 = java.lang.Integer.valueOf(r2);
        r8 = 0;
        r7[r8] = r5;
        r5 = java.lang.Integer.valueOf(r3);
        r8 = 1;
        r7[r8] = r5;
        r5 = "%d_%d";
        r4 = java.lang.String.format(r4, r5, r7);
        r5 = r1.photoImage;
        r5.setAutoRepeat(r8);
    L_0x2abb:
        r31 = r4;
        r36 = r14;
    L_0x2abf:
        r4 = 6;
        r1.documentAttachType = r4;
        r4 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r2 - r4;
        r1.availableTimeWidth = r4;
        r4 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r2;
        r1.backgroundWidth = r4;
        r4 = r14.photoThumbs;
        r5 = 40;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r5);
        r1.currentPhotoObjectThumb = r4;
        r4 = r14.photoThumbsObject;
        r1.photoParentObject = r4;
        r4 = r14.attachPathExists;
        if (r4 == 0) goto L_0x2b15;
    L_0x2ae7:
        r4 = r1.photoImage;
        r5 = r14.messageOwner;
        r5 = r5.attachPath;
        r30 = org.telegram.messenger.ImageLocation.getForPath(r5);
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r32 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r5 = r60.getDocument();
        r5 = r5.size;
        if (r0 == 0) goto L_0x2b07;
    L_0x2b01:
        r0 = "webp";
        r35 = r0;
        goto L_0x2b09;
    L_0x2b07:
        r35 = 0;
    L_0x2b09:
        r37 = 1;
        r33 = "b1";
        r29 = r4;
        r34 = r5;
        r29.setImage(r30, r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2b4e;
    L_0x2b15:
        r4 = r60.getDocument();
        r4 = r4.id;
        r6 = 0;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x2b4e;
    L_0x2b21:
        r4 = r1.photoImage;
        r5 = r60.getDocument();
        r30 = org.telegram.messenger.ImageLocation.getForDocument(r5);
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r32 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r5 = r60.getDocument();
        r5 = r5.size;
        if (r0 == 0) goto L_0x2b41;
    L_0x2b3b:
        r0 = "webp";
        r35 = r0;
        goto L_0x2b43;
    L_0x2b41:
        r35 = 0;
    L_0x2b43:
        r37 = 1;
        r33 = "b1";
        r29 = r4;
        r34 = r5;
        r29.setImage(r30, r31, r32, r33, r34, r35, r36, r37);
    L_0x2b4e:
        r0 = r2;
        goto L_0x240b;
    L_0x2b51:
        r0 = r14.photoThumbs;
        r2 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r2);
        r1.currentPhotoObject = r0;
        r0 = r14.photoThumbsObject;
        r1.photoParentObject = r0;
        r0 = r14.type;
        r2 = 5;
        if (r0 != r2) goto L_0x2b73;
    L_0x2b66:
        r0 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r2 = r60.getDocument();
        r1.documentAttach = r2;
        r2 = 7;
        r1.documentAttachType = r2;
    L_0x2b71:
        r2 = 0;
        goto L_0x2bb9;
    L_0x2b73:
        r0 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r0 == 0) goto L_0x2b85;
    L_0x2b79:
        r0 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
    L_0x2b7d:
        r0 = (float) r0;
        r2 = NUM; // 0x3var_ float:0.7 double:5.23867711E-315;
        r0 = r0 * r2;
        r0 = (int) r0;
        goto L_0x2b71;
    L_0x2b85:
        r0 = r1.currentPhotoObject;
        if (r0 == 0) goto L_0x2bae;
    L_0x2b89:
        r0 = r14.type;
        r2 = 1;
        if (r0 == r2) goto L_0x2b93;
    L_0x2b8e:
        r2 = 3;
        if (r0 == r2) goto L_0x2b93;
    L_0x2b91:
        if (r0 != r15) goto L_0x2bae;
    L_0x2b93:
        r0 = r1.currentPhotoObject;
        r2 = r0.w;
        r0 = r0.h;
        if (r2 < r0) goto L_0x2bae;
    L_0x2b9b:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r2 = r0.x;
        r0 = r0.y;
        r0 = java.lang.Math.min(r2, r0);
        r2 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r2 = 1;
        goto L_0x2bb9;
    L_0x2bae:
        r0 = org.telegram.messenger.AndroidUtilities.displaySize;
        r2 = r0.x;
        r0 = r0.y;
        r0 = java.lang.Math.min(r2, r0);
        goto L_0x2b7d;
    L_0x2bb9:
        r3 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r0;
        if (r2 != 0) goto L_0x2bf3;
    L_0x2bc2:
        r2 = r14.type;
        r4 = 5;
        if (r2 == r4) goto L_0x2bdd;
    L_0x2bc7:
        r2 = r59.checkNeedDrawShareButton(r60);
        if (r2 == 0) goto L_0x2bdd;
    L_0x2bcd:
        r2 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r0 - r2;
        r4 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        goto L_0x2bde;
    L_0x2bdd:
        r2 = r0;
    L_0x2bde:
        r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        if (r0 <= r4) goto L_0x2be8;
    L_0x2be4:
        r0 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x2be8:
        r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        if (r3 <= r4) goto L_0x2CLASSNAME;
    L_0x2bee:
        r3 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        goto L_0x2CLASSNAME;
    L_0x2bf3:
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x2CLASSNAME;
    L_0x2bf7:
        r2 = r60.needDrawAvatar();
        if (r2 == 0) goto L_0x2CLASSNAME;
    L_0x2bfd:
        r2 = r60.isOutOwner();
        if (r2 != 0) goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r2 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r0 - r2;
        r58 = r2;
        r2 = r0;
        r0 = r58;
        goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r2 = r0;
    L_0x2CLASSNAME:
        r4 = r14.type;
        r5 = 1;
        if (r4 != r5) goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r59.updateSecretTimeText(r60);
        r4 = r14.photoThumbs;
        r5 = 40;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r5);
        r1.currentPhotoObjectThumb = r4;
        goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r5 = 3;
        if (r4 == r5) goto L_0x2c3b;
    L_0x2CLASSNAME:
        if (r4 != r15) goto L_0x2c2b;
    L_0x2c2a:
        goto L_0x2c3b;
    L_0x2c2b:
        r5 = 5;
        if (r4 != r5) goto L_0x2CLASSNAME;
    L_0x2c2e:
        r4 = r14.photoThumbs;
        r5 = 40;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r5);
        r1.currentPhotoObjectThumb = r4;
        goto L_0x2c4c;
    L_0x2CLASSNAME:
        r4 = 0;
        goto L_0x2c4d;
    L_0x2c3b:
        r4 = 0;
        r1.createDocumentLayout(r4, r14);
        r4 = r14.photoThumbs;
        r5 = 40;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r5);
        r1.currentPhotoObjectThumb = r4;
        r59.updateSecretTimeText(r60);
    L_0x2c4c:
        r4 = 1;
    L_0x2c4d:
        r5 = r14.type;
        r6 = 5;
        if (r5 != r6) goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r5 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r6 = r5;
        goto L_0x2c9c;
    L_0x2CLASSNAME:
        r5 = r1.currentPhotoObject;
        if (r5 == 0) goto L_0x2c5b;
    L_0x2c5a:
        goto L_0x2c5d;
    L_0x2c5b:
        r5 = r1.currentPhotoObjectThumb;
    L_0x2c5d:
        if (r5 == 0) goto L_0x2CLASSNAME;
    L_0x2c5f:
        r12 = r5.w;
        r5 = r5.h;
        goto L_0x2c8d;
    L_0x2CLASSNAME:
        r5 = r1.documentAttach;
        if (r5 == 0) goto L_0x2c8b;
    L_0x2CLASSNAME:
        r5 = r5.attributes;
        r5 = r5.size();
        r6 = 0;
        r7 = 0;
        r12 = 0;
    L_0x2CLASSNAME:
        if (r6 >= r5) goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r8 = r1.documentAttach;
        r8 = r8.attributes;
        r8 = r8.get(r6);
        r8 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r8;
        r9 = r8 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r9 == 0) goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r7 = r8.w;
        r12 = r8.h;
    L_0x2CLASSNAME:
        r6 = r6 + 1;
        goto L_0x2CLASSNAME;
    L_0x2CLASSNAME:
        r5 = r12;
        r12 = r7;
        goto L_0x2c8d;
    L_0x2c8b:
        r5 = 0;
        r12 = 0;
    L_0x2c8d:
        r5 = getMessageSize(r12, r5, r0, r3);
        r6 = r5.x;
        r6 = (int) r6;
        r5 = r5.y;
        r5 = (int) r5;
        r58 = r6;
        r6 = r5;
        r5 = r58;
    L_0x2c9c:
        r7 = r1.currentPhotoObject;
        if (r7 == 0) goto L_0x2cae;
    L_0x2ca0:
        r7 = r7.type;
        r8 = "s";
        r7 = r8.equals(r7);
        if (r7 == 0) goto L_0x2cae;
    L_0x2caa:
        r7 = 0;
        r1.currentPhotoObject = r7;
        goto L_0x2caf;
    L_0x2cae:
        r7 = 0;
    L_0x2caf:
        r8 = r1.currentPhotoObject;
        if (r8 == 0) goto L_0x2cc1;
    L_0x2cb3:
        r9 = r1.currentPhotoObjectThumb;
        if (r8 != r9) goto L_0x2cc1;
    L_0x2cb7:
        r8 = r14.type;
        r9 = 1;
        if (r8 != r9) goto L_0x2cbf;
    L_0x2cbc:
        r1.currentPhotoObjectThumb = r7;
        goto L_0x2cc1;
    L_0x2cbf:
        r1.currentPhotoObject = r7;
    L_0x2cc1:
        if (r4 == 0) goto L_0x2cea;
    L_0x2cc3:
        r4 = r60.needDrawBluredPreview();
        if (r4 != 0) goto L_0x2cea;
    L_0x2cc9:
        r4 = r1.currentPhotoObject;
        if (r4 == 0) goto L_0x2cd1;
    L_0x2ccd:
        r7 = r1.currentPhotoObjectThumb;
        if (r4 != r7) goto L_0x2cea;
    L_0x2cd1:
        r4 = r1.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x2cdf;
    L_0x2cd5:
        r4 = r4.type;
        r7 = "m";
        r4 = r7.equals(r4);
        if (r4 != 0) goto L_0x2cea;
    L_0x2cdf:
        r4 = r1.photoImage;
        r7 = 1;
        r4.setNeedsQualityThumb(r7);
        r4 = r1.photoImage;
        r4.setShouldGenerateQualityThumb(r7);
    L_0x2cea:
        r4 = r1.currentMessagesGroup;
        if (r4 != 0) goto L_0x2cf5;
    L_0x2cee:
        r4 = r14.caption;
        if (r4 == 0) goto L_0x2cf5;
    L_0x2cf2:
        r4 = 0;
        r1.mediaBackground = r4;
    L_0x2cf5:
        if (r5 == 0) goto L_0x2cf9;
    L_0x2cf7:
        if (r6 != 0) goto L_0x2d5b;
    L_0x2cf9:
        r4 = r14.type;
        if (r4 != r15) goto L_0x2d5b;
    L_0x2cfd:
        r4 = 0;
    L_0x2cfe:
        r7 = r60.getDocument();
        r7 = r7.attributes;
        r7 = r7.size();
        if (r4 >= r7) goto L_0x2d5b;
    L_0x2d0a:
        r7 = r60.getDocument();
        r7 = r7.attributes;
        r7 = r7.get(r4);
        r7 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r7;
        r8 = r7 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r8 != 0) goto L_0x2d22;
    L_0x2d1a:
        r8 = r7 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r8 == 0) goto L_0x2d1f;
    L_0x2d1e:
        goto L_0x2d22;
    L_0x2d1f:
        r4 = r4 + 1;
        goto L_0x2cfe;
    L_0x2d22:
        r4 = r7.w;
        r5 = (float) r4;
        r0 = (float) r0;
        r5 = r5 / r0;
        r4 = (float) r4;
        r4 = r4 / r5;
        r4 = (int) r4;
        r6 = r7.h;
        r6 = (float) r6;
        r6 = r6 / r5;
        r5 = (int) r6;
        if (r5 <= r3) goto L_0x2d38;
    L_0x2d31:
        r0 = (float) r5;
        r5 = (float) r3;
        r0 = r0 / r5;
        r4 = (float) r4;
        r4 = r4 / r0;
        r5 = (int) r4;
        goto L_0x2d5c;
    L_0x2d38:
        r3 = NUM; // 0x42var_ float:120.0 double:5.548480205E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        if (r5 >= r3) goto L_0x2d58;
    L_0x2d40:
        r3 = NUM; // 0x42var_ float:120.0 double:5.548480205E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r5 = r7.h;
        r5 = (float) r5;
        r6 = (float) r3;
        r5 = r5 / r6;
        r6 = r7.w;
        r7 = (float) r6;
        r7 = r7 / r5;
        r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1));
        if (r0 >= 0) goto L_0x2d59;
    L_0x2d53:
        r0 = (float) r6;
        r0 = r0 / r5;
        r0 = (int) r0;
        r5 = r0;
        goto L_0x2d5c;
    L_0x2d58:
        r3 = r5;
    L_0x2d59:
        r5 = r4;
        goto L_0x2d5c;
    L_0x2d5b:
        r3 = r6;
    L_0x2d5c:
        if (r5 == 0) goto L_0x2d60;
    L_0x2d5e:
        if (r3 != 0) goto L_0x2d67;
    L_0x2d60:
        r0 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r3 = r5;
    L_0x2d67:
        r0 = r14.type;
        r4 = 3;
        if (r0 != r4) goto L_0x2d81;
    L_0x2d6c:
        r0 = r1.infoWidth;
        r4 = NUM; // 0x42200000 float:40.0 double:5.481131706E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        if (r5 >= r0) goto L_0x2d81;
    L_0x2d77:
        r0 = r1.infoWidth;
        r4 = NUM; // 0x42200000 float:40.0 double:5.481131706E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r0 + r4;
    L_0x2d81:
        r0 = r1.currentMessagesGroup;
        if (r0 == 0) goto L_0x2dc7;
    L_0x2d85:
        r0 = r59.getGroupPhotosWidth();
        r2 = 0;
        r4 = 0;
    L_0x2d8b:
        r6 = r1.currentMessagesGroup;
        r6 = r6.posArray;
        r6 = r6.size();
        if (r2 >= r6) goto L_0x2dbd;
    L_0x2d95:
        r6 = r1.currentMessagesGroup;
        r6 = r6.posArray;
        r6 = r6.get(r2);
        r6 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r6;
        r7 = r6.minY;
        if (r7 != 0) goto L_0x2dbd;
    L_0x2da3:
        r7 = (double) r4;
        r4 = r6.pw;
        r6 = r6.leftSpanOffset;
        r4 = r4 + r6;
        r4 = (float) r4;
        r6 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r6 = (float) r0;
        r4 = r4 * r6;
        r9 = (double) r4;
        r9 = java.lang.Math.ceil(r9);
        java.lang.Double.isNaN(r7);
        r7 = r7 + r9;
        r4 = (int) r7;
        r2 = r2 + 1;
        goto L_0x2d8b;
    L_0x2dbd:
        r0 = NUM; // 0x420CLASSNAME float:35.0 double:5.47465589E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r4 = r4 - r0;
        r1.availableTimeWidth = r4;
        goto L_0x2dd0;
    L_0x2dc7:
        r0 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r2 = r2 - r0;
        r1.availableTimeWidth = r2;
    L_0x2dd0:
        r0 = r14.type;
        r2 = 5;
        if (r0 != r2) goto L_0x2df7;
    L_0x2dd5:
        r0 = r1.availableTimeWidth;
        r6 = (double) r0;
        r0 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r2 = "00:00";
        r0 = r0.measureText(r2);
        r8 = (double) r0;
        r8 = java.lang.Math.ceil(r8);
        r0 = NUM; // 0x41d00000 float:26.0 double:5.455228437E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r12 = (double) r0;
        java.lang.Double.isNaN(r12);
        r8 = r8 + r12;
        java.lang.Double.isNaN(r6);
        r6 = r6 - r8;
        r0 = (int) r6;
        r1.availableTimeWidth = r0;
    L_0x2df7:
        r59.measureTime(r60);
        r0 = r1.timeWidth;
        r2 = r60.isOutOwner();
        if (r2 == 0) goto L_0x2e05;
    L_0x2e02:
        r12 = 20;
        goto L_0x2e06;
    L_0x2e05:
        r12 = 0;
    L_0x2e06:
        r12 = r12 + 14;
        r2 = (float) r12;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        if (r5 >= r0) goto L_0x2e11;
    L_0x2e10:
        r5 = r0;
    L_0x2e11:
        r2 = r60.isRoundVideo();
        if (r2 == 0) goto L_0x2e27;
    L_0x2e17:
        r3 = java.lang.Math.min(r5, r3);
        r2 = 0;
        r1.drawBackground = r2;
        r2 = r1.photoImage;
        r4 = r3 / 2;
        r2.setRoundRadius(r4);
    L_0x2e25:
        r5 = r3;
        goto L_0x2e49;
    L_0x2e27:
        r2 = r60.needDrawBluredPreview();
        if (r2 == 0) goto L_0x2e49;
    L_0x2e2d:
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x2e38;
    L_0x2e33:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        goto L_0x2e42;
    L_0x2e38:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r3 = r2.x;
        r2 = r2.y;
        r2 = java.lang.Math.min(r3, r2);
    L_0x2e42:
        r2 = (float) r2;
        r3 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        r2 = r2 * r3;
        r3 = (int) r2;
        goto L_0x2e25;
    L_0x2e49:
        r2 = r1.currentMessagesGroup;
        if (r2 == 0) goto L_0x317b;
    L_0x2e4d:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r3 = r2.x;
        r2 = r2.y;
        r2 = java.lang.Math.max(r3, r2);
        r2 = (float) r2;
        r3 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        r2 = r2 * r3;
        r3 = r59.getGroupPhotosWidth();
        r4 = r1.currentPosition;
        r4 = r4.pw;
        r4 = (float) r4;
        r5 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r5;
        r3 = (float) r3;
        r4 = r4 * r3;
        r4 = (double) r4;
        r4 = java.lang.Math.ceil(r4);
        r4 = (int) r4;
        r5 = r1.currentPosition;
        r5 = r5.minY;
        if (r5 == 0) goto L_0x2var_;
    L_0x2e77:
        r5 = r60.isOutOwner();
        if (r5 == 0) goto L_0x2e85;
    L_0x2e7d:
        r5 = r1.currentPosition;
        r5 = r5.flags;
        r6 = 1;
        r5 = r5 & r6;
        if (r5 != 0) goto L_0x2e93;
    L_0x2e85:
        r5 = r60.isOutOwner();
        if (r5 != 0) goto L_0x2var_;
    L_0x2e8b:
        r5 = r1.currentPosition;
        r5 = r5.flags;
        r6 = 2;
        r5 = r5 & r6;
        if (r5 == 0) goto L_0x2var_;
    L_0x2e93:
        r5 = 0;
        r6 = 0;
        r7 = 0;
    L_0x2e96:
        r8 = r1.currentMessagesGroup;
        r8 = r8.posArray;
        r8 = r8.size();
        if (r5 >= r8) goto L_0x2f0f;
    L_0x2ea0:
        r8 = r1.currentMessagesGroup;
        r8 = r8.posArray;
        r8 = r8.get(r5);
        r8 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r8;
        r9 = r8.minY;
        if (r9 != 0) goto L_0x2eda;
    L_0x2eae:
        r9 = (double) r6;
        r6 = r8.pw;
        r6 = (float) r6;
        r12 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r6 = r6 / r12;
        r6 = r6 * r3;
        r12 = (double) r6;
        r12 = java.lang.Math.ceil(r12);
        r6 = r8.leftSpanOffset;
        if (r6 == 0) goto L_0x2ece;
    L_0x2ec0:
        r6 = (float) r6;
        r8 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r6 = r6 / r8;
        r6 = r6 * r3;
        r26 = r12;
        r11 = (double) r6;
        r11 = java.lang.Math.ceil(r11);
        goto L_0x2ed2;
    L_0x2ece:
        r26 = r12;
        r11 = 0;
    L_0x2ed2:
        r12 = r26 + r11;
        java.lang.Double.isNaN(r9);
        r9 = r9 + r12;
        r6 = (int) r9;
        goto L_0x2f0a;
    L_0x2eda:
        r10 = r1.currentPosition;
        r10 = r10.minY;
        if (r9 != r10) goto L_0x2var_;
    L_0x2ee0:
        r9 = (double) r7;
        r7 = r8.pw;
        r7 = (float) r7;
        r11 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r7 = r7 / r11;
        r7 = r7 * r3;
        r11 = (double) r7;
        r11 = java.lang.Math.ceil(r11);
        r7 = r8.leftSpanOffset;
        if (r7 == 0) goto L_0x2efe;
    L_0x2ef2:
        r7 = (float) r7;
        r8 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r7 = r7 / r8;
        r7 = r7 * r3;
        r7 = (double) r7;
        r7 = java.lang.Math.ceil(r7);
        goto L_0x2var_;
    L_0x2efe:
        r7 = 0;
    L_0x2var_:
        r11 = r11 + r7;
        java.lang.Double.isNaN(r9);
        r9 = r9 + r11;
        r7 = (int) r9;
        goto L_0x2f0a;
    L_0x2var_:
        if (r9 <= r10) goto L_0x2f0a;
    L_0x2var_:
        goto L_0x2f0f;
    L_0x2f0a:
        r5 = r5 + 1;
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x2e96;
    L_0x2f0f:
        r6 = r6 - r7;
        r4 = r4 + r6;
    L_0x2var_:
        r5 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r5 = r1.isAvatarVisible;
        if (r5 == 0) goto L_0x2var_;
    L_0x2f1c:
        r5 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
    L_0x2var_:
        r5 = r1.currentPosition;
        r6 = r5.siblingHeights;
        if (r6 == 0) goto L_0x2var_;
    L_0x2var_:
        r5 = 0;
        r6 = 0;
    L_0x2f2b:
        r7 = r1.currentPosition;
        r8 = r7.siblingHeights;
        r9 = r8.length;
        if (r5 >= r9) goto L_0x2var_;
    L_0x2var_:
        r7 = r8[r5];
        r7 = r7 * r2;
        r7 = (double) r7;
        r7 = java.lang.Math.ceil(r7);
        r7 = (int) r7;
        r6 = r6 + r7;
        r5 = r5 + 1;
        goto L_0x2f2b;
    L_0x2var_:
        r2 = r7.maxY;
        r5 = r7.minY;
        r2 = r2 - r5;
        r5 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r7 = org.telegram.messenger.AndroidUtilities.density;
        r7 = r7 * r5;
        r5 = java.lang.Math.round(r7);
        r2 = r2 * r5;
        r6 = r6 + r2;
        goto L_0x2f5d;
    L_0x2var_:
        r5 = r5.ph;
        r2 = r2 * r5;
        r5 = (double) r2;
        r5 = java.lang.Math.ceil(r5);
        r6 = (int) r5;
    L_0x2f5d:
        r1.backgroundWidth = r4;
        r2 = r1.currentPosition;
        r2 = r2.flags;
        r5 = r2 & 2;
        if (r5 == 0) goto L_0x2var_;
    L_0x2var_:
        r5 = 1;
        r2 = r2 & r5;
        if (r2 == 0) goto L_0x2var_;
    L_0x2f6b:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
    L_0x2f6f:
        r4 = r4 - r2;
        goto L_0x2var_;
    L_0x2var_:
        r2 = r1.currentPosition;
        r2 = r2.flags;
        r5 = r2 & 2;
        if (r5 != 0) goto L_0x2var_;
    L_0x2var_:
        r5 = 1;
        r2 = r2 & r5;
        if (r2 != 0) goto L_0x2var_;
    L_0x2f7d:
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        goto L_0x2f6f;
    L_0x2var_:
        r2 = r1.currentPosition;
        r2 = r2.flags;
        r5 = 2;
        r2 = r2 & r5;
        if (r2 == 0) goto L_0x2var_;
    L_0x2f8c:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        goto L_0x2f6f;
    L_0x2var_:
        r2 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        goto L_0x2f6f;
    L_0x2var_:
        r2 = r1.currentPosition;
        r2 = r2.edge;
        if (r2 != 0) goto L_0x2fa5;
    L_0x2f9e:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r2 + r4;
        r5 = r2;
        goto L_0x2fa6;
    L_0x2fa5:
        r5 = r4;
    L_0x2fa6:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r5 - r2;
        r7 = 0;
        r12 = r7 + r2;
        r2 = r1.currentPosition;
        r2 = r2.flags;
        r7 = r2 & 8;
        if (r7 != 0) goto L_0x2fc9;
    L_0x2fb7:
        r7 = r1.currentMessagesGroup;
        r7 = r7.hasSibling;
        if (r7 == 0) goto L_0x2fc2;
    L_0x2fbd:
        r2 = r2 & 4;
        if (r2 != 0) goto L_0x2fc2;
    L_0x2fc1:
        goto L_0x2fc9;
    L_0x2fc2:
        r24 = r5;
        r22 = r6;
        r5 = r4;
        goto L_0x3172;
    L_0x2fc9:
        r2 = r1.currentPosition;
        r2 = r1.getAdditionalWidthForPosition(r2);
        r12 = r12 + r2;
        r2 = r1.currentMessagesGroup;
        r2 = r2.messages;
        r2 = r2.size();
        r7 = r4;
        r4 = 0;
    L_0x2fda:
        if (r4 >= r2) goto L_0x316b;
    L_0x2fdc:
        r8 = r1.currentMessagesGroup;
        r8 = r8.messages;
        r8 = r8.get(r4);
        r8 = (org.telegram.messenger.MessageObject) r8;
        r9 = r1.currentMessagesGroup;
        r9 = r9.posArray;
        r9 = r9.get(r4);
        r9 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r9;
        r10 = r1.currentPosition;
        if (r9 == r10) goto L_0x3145;
    L_0x2ff4:
        r10 = r9.flags;
        r10 = r10 & r15;
        if (r10 == 0) goto L_0x3145;
    L_0x2ff9:
        r7 = r9.pw;
        r7 = (float) r7;
        r10 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r7 = r7 / r10;
        r7 = r7 * r3;
        r10 = (double) r7;
        r10 = java.lang.Math.ceil(r10);
        r7 = (int) r10;
        r10 = r9.minY;
        if (r10 == 0) goto L_0x30bc;
    L_0x300b:
        r10 = r60.isOutOwner();
        if (r10 == 0) goto L_0x3017;
    L_0x3011:
        r10 = r9.flags;
        r11 = 1;
        r10 = r10 & r11;
        if (r10 != 0) goto L_0x3023;
    L_0x3017:
        r10 = r60.isOutOwner();
        if (r10 != 0) goto L_0x30bc;
    L_0x301d:
        r10 = r9.flags;
        r11 = 2;
        r10 = r10 & r11;
        if (r10 == 0) goto L_0x30bc;
    L_0x3023:
        r10 = 0;
        r11 = 0;
        r13 = 0;
    L_0x3026:
        r15 = r1.currentMessagesGroup;
        r15 = r15.posArray;
        r15 = r15.size();
        if (r10 >= r15) goto L_0x30b1;
    L_0x3030:
        r15 = r1.currentMessagesGroup;
        r15 = r15.posArray;
        r15 = r15.get(r10);
        r15 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r15;
        r63 = r2;
        r2 = r15.minY;
        if (r2 != 0) goto L_0x306e;
    L_0x3040:
        r24 = r5;
        r22 = r6;
        r5 = (double) r11;
        r2 = r15.pw;
        r2 = (float) r2;
        r11 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = r2 / r11;
        r2 = r2 * r3;
        r26 = r12;
        r11 = (double) r2;
        r11 = java.lang.Math.ceil(r11);
        r2 = r15.leftSpanOffset;
        if (r2 == 0) goto L_0x3064;
    L_0x3058:
        r2 = (float) r2;
        r15 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = r2 / r15;
        r2 = r2 * r3;
        r14 = (double) r2;
        r14 = java.lang.Math.ceil(r14);
        goto L_0x3066;
    L_0x3064:
        r14 = 0;
    L_0x3066:
        r11 = r11 + r14;
        java.lang.Double.isNaN(r5);
        r5 = r5 + r11;
        r2 = (int) r5;
        r11 = r2;
        goto L_0x30a3;
    L_0x306e:
        r24 = r5;
        r22 = r6;
        r26 = r12;
        r5 = r9.minY;
        if (r2 != r5) goto L_0x30a0;
    L_0x3078:
        r5 = (double) r13;
        r2 = r15.pw;
        r2 = (float) r2;
        r12 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = r2 / r12;
        r2 = r2 * r3;
        r12 = (double) r2;
        r12 = java.lang.Math.ceil(r12);
        r2 = r15.leftSpanOffset;
        if (r2 == 0) goto L_0x3096;
    L_0x308a:
        r2 = (float) r2;
        r14 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = r2 / r14;
        r2 = r2 * r3;
        r14 = (double) r2;
        r14 = java.lang.Math.ceil(r14);
        goto L_0x3098;
    L_0x3096:
        r14 = 0;
    L_0x3098:
        r12 = r12 + r14;
        java.lang.Double.isNaN(r5);
        r5 = r5 + r12;
        r2 = (int) r5;
        r13 = r2;
        goto L_0x30a3;
    L_0x30a0:
        if (r2 <= r5) goto L_0x30a3;
    L_0x30a2:
        goto L_0x30b9;
    L_0x30a3:
        r10 = r10 + 1;
        r14 = r60;
        r2 = r63;
        r6 = r22;
        r5 = r24;
        r12 = r26;
        goto L_0x3026;
    L_0x30b1:
        r63 = r2;
        r24 = r5;
        r22 = r6;
        r26 = r12;
    L_0x30b9:
        r11 = r11 - r13;
        r7 = r7 + r11;
        goto L_0x30c4;
    L_0x30bc:
        r63 = r2;
        r24 = r5;
        r22 = r6;
        r26 = r12;
    L_0x30c4:
        r2 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r7 = r7 - r2;
        r2 = r9.flags;
        r5 = r2 & 2;
        if (r5 == 0) goto L_0x30db;
    L_0x30d1:
        r2 = r2 & 1;
        if (r2 == 0) goto L_0x30db;
    L_0x30d5:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
    L_0x30d9:
        r7 = r7 - r2;
        goto L_0x30fe;
    L_0x30db:
        r2 = r9.flags;
        r5 = r2 & 2;
        if (r5 != 0) goto L_0x30ec;
    L_0x30e1:
        r2 = r2 & 1;
        if (r2 != 0) goto L_0x30ec;
    L_0x30e5:
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        goto L_0x30d9;
    L_0x30ec:
        r2 = r9.flags;
        r5 = 2;
        r2 = r2 & r5;
        if (r2 == 0) goto L_0x30f7;
    L_0x30f2:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        goto L_0x30d9;
    L_0x30f7:
        r2 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        goto L_0x30d9;
    L_0x30fe:
        r2 = r1.isChat;
        if (r2 == 0) goto L_0x311b;
    L_0x3102:
        r2 = r8.isOutOwner();
        if (r2 != 0) goto L_0x311b;
    L_0x3108:
        r2 = r8.needDrawAvatar();
        if (r2 == 0) goto L_0x311b;
    L_0x310e:
        if (r9 == 0) goto L_0x3114;
    L_0x3110:
        r2 = r9.edge;
        if (r2 == 0) goto L_0x311b;
    L_0x3114:
        r2 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r7 = r7 - r2;
    L_0x311b:
        r2 = r1.getAdditionalWidthForPosition(r9);
        r7 = r7 + r2;
        r2 = r9.edge;
        if (r2 != 0) goto L_0x3129;
    L_0x3124:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r7 = r7 + r2;
    L_0x3129:
        r12 = r26 + r7;
        r2 = r9.minX;
        r5 = r1.currentPosition;
        r5 = r5.minX;
        if (r2 < r5) goto L_0x313f;
    L_0x3133:
        r2 = r1.currentMessagesGroup;
        r2 = r2.hasSibling;
        if (r2 == 0) goto L_0x314f;
    L_0x3139:
        r2 = r9.minY;
        r5 = r9.maxY;
        if (r2 == r5) goto L_0x314f;
    L_0x313f:
        r2 = r1.captionOffsetX;
        r2 = r2 - r7;
        r1.captionOffsetX = r2;
        goto L_0x314f;
    L_0x3145:
        r63 = r2;
        r24 = r5;
        r22 = r6;
        r26 = r12;
        r12 = r26;
    L_0x314f:
        r2 = r8.caption;
        if (r2 == 0) goto L_0x315d;
    L_0x3153:
        r5 = r1.currentCaption;
        if (r5 == 0) goto L_0x315b;
    L_0x3157:
        r5 = 0;
        r1.currentCaption = r5;
        goto L_0x3171;
    L_0x315b:
        r1.currentCaption = r2;
    L_0x315d:
        r4 = r4 + 1;
        r14 = r60;
        r2 = r63;
        r6 = r22;
        r5 = r24;
        r15 = 8;
        goto L_0x2fda;
    L_0x316b:
        r24 = r5;
        r22 = r6;
        r26 = r12;
    L_0x3171:
        r5 = r7;
    L_0x3172:
        r14 = r60;
        r3 = r5;
        r15 = r22;
        r5 = r24;
        r2 = 0;
        goto L_0x31ca;
    L_0x317b:
        r2 = r14.caption;
        r1.currentCaption = r2;
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x318a;
    L_0x3185:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        goto L_0x3194;
    L_0x318a:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r2.x;
        r2 = r2.y;
        r2 = java.lang.Math.min(r4, r2);
    L_0x3194:
        r2 = (float) r2;
        r4 = NUM; // 0x3var_ float:0.65 double:5.234532584E-315;
        r2 = r2 * r4;
        r2 = (int) r2;
        r4 = r60.needDrawBluredPreview();
        if (r4 != 0) goto L_0x31aa;
    L_0x31a1:
        r4 = r1.currentCaption;
        if (r4 == 0) goto L_0x31aa;
    L_0x31a5:
        if (r5 >= r2) goto L_0x31aa;
    L_0x31a7:
        r12 = r2;
        r2 = 1;
        goto L_0x31b2;
    L_0x31aa:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r5 - r2;
        r12 = r2;
        r2 = 0;
    L_0x31b2:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 + r5;
        r1.backgroundWidth = r4;
        r4 = r1.mediaBackground;
        if (r4 != 0) goto L_0x31c8;
    L_0x31bd:
        r4 = r1.backgroundWidth;
        r6 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r1.backgroundWidth = r4;
    L_0x31c8:
        r15 = r3;
        r3 = r5;
    L_0x31ca:
        r4 = r1.currentCaption;
        if (r4 == 0) goto L_0x32d5;
    L_0x31ce:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x32cc }
        r7 = 24;
        if (r6 < r7) goto L_0x31f5;
    L_0x31d4:
        r6 = r4.length();	 Catch:{ Exception -> 0x32cc }
        r7 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x32cc }
        r8 = 0;
        r4 = android.text.StaticLayout.Builder.obtain(r4, r8, r6, r7, r12);	 Catch:{ Exception -> 0x32cc }
        r6 = 1;
        r4 = r4.setBreakStrategy(r6);	 Catch:{ Exception -> 0x32cc }
        r4 = r4.setHyphenationFrequency(r8);	 Catch:{ Exception -> 0x32cc }
        r6 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x32cc }
        r4 = r4.setAlignment(r6);	 Catch:{ Exception -> 0x32cc }
        r4 = r4.build();	 Catch:{ Exception -> 0x32cc }
        r1.captionLayout = r4;	 Catch:{ Exception -> 0x32cc }
        goto L_0x320c;
    L_0x31f5:
        r6 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x32cc }
        r31 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x32cc }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x32cc }
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r6;
        r30 = r4;
        r32 = r12;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);	 Catch:{ Exception -> 0x32cc }
        r1.captionLayout = r6;	 Catch:{ Exception -> 0x32cc }
    L_0x320c:
        r4 = r1.captionLayout;	 Catch:{ Exception -> 0x32cc }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x32cc }
        if (r4 <= 0) goto L_0x32c6;
    L_0x3214:
        if (r2 == 0) goto L_0x3249;
    L_0x3216:
        r6 = 0;
        r1.captionWidth = r6;	 Catch:{ Exception -> 0x32cc }
        r6 = 0;
    L_0x321a:
        if (r6 >= r4) goto L_0x3242;
    L_0x321c:
        r7 = r1.captionWidth;	 Catch:{ Exception -> 0x32cc }
        r7 = (double) r7;	 Catch:{ Exception -> 0x32cc }
        r9 = r1.captionLayout;	 Catch:{ Exception -> 0x32cc }
        r9 = r9.getLineWidth(r6);	 Catch:{ Exception -> 0x32cc }
        r9 = (double) r9;	 Catch:{ Exception -> 0x32cc }
        r9 = java.lang.Math.ceil(r9);	 Catch:{ Exception -> 0x32cc }
        r7 = java.lang.Math.max(r7, r9);	 Catch:{ Exception -> 0x32cc }
        r7 = (int) r7;	 Catch:{ Exception -> 0x32cc }
        r1.captionWidth = r7;	 Catch:{ Exception -> 0x32cc }
        r7 = r1.captionLayout;	 Catch:{ Exception -> 0x32cc }
        r7 = r7.getLineLeft(r6);	 Catch:{ Exception -> 0x32cc }
        r8 = 0;
        r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r7 == 0) goto L_0x323f;
    L_0x323c:
        r1.captionWidth = r12;	 Catch:{ Exception -> 0x32cc }
        goto L_0x3242;
    L_0x323f:
        r6 = r6 + 1;
        goto L_0x321a;
    L_0x3242:
        r4 = r1.captionWidth;	 Catch:{ Exception -> 0x32cc }
        if (r4 <= r12) goto L_0x324b;
    L_0x3246:
        r1.captionWidth = r12;	 Catch:{ Exception -> 0x32cc }
        goto L_0x324b;
    L_0x3249:
        r1.captionWidth = r12;	 Catch:{ Exception -> 0x32cc }
    L_0x324b:
        r4 = r1.captionLayout;	 Catch:{ Exception -> 0x32cc }
        r4 = r4.getHeight();	 Catch:{ Exception -> 0x32cc }
        r1.captionHeight = r4;	 Catch:{ Exception -> 0x32cc }
        r4 = r1.captionHeight;	 Catch:{ Exception -> 0x32cc }
        r6 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x32cc }
        r4 = r4 + r6;
        r1.addedCaptionHeight = r4;	 Catch:{ Exception -> 0x32cc }
        r4 = r1.currentPosition;	 Catch:{ Exception -> 0x32cc }
        if (r4 == 0) goto L_0x3270;
    L_0x3262:
        r4 = r1.currentPosition;	 Catch:{ Exception -> 0x32cc }
        r4 = r4.flags;	 Catch:{ Exception -> 0x32cc }
        r6 = 8;
        r4 = r4 & r6;
        if (r4 == 0) goto L_0x326c;
    L_0x326b:
        goto L_0x3270;
    L_0x326c:
        r4 = 0;
        r1.captionLayout = r4;	 Catch:{ Exception -> 0x32cc }
        goto L_0x32c6;
    L_0x3270:
        r4 = r1.addedCaptionHeight;	 Catch:{ Exception -> 0x32cc }
        r6 = 0;
        r12 = r6 + r4;
        r4 = r1.captionWidth;	 Catch:{ Exception -> 0x32c4 }
        r6 = org.telegram.messenger.AndroidUtilities.dp(r20);	 Catch:{ Exception -> 0x32c4 }
        r6 = r5 - r6;
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x32c4 }
        r6 = r1.captionLayout;	 Catch:{ Exception -> 0x32c4 }
        r7 = r1.captionLayout;	 Catch:{ Exception -> 0x32c4 }
        r7 = r7.getLineCount();	 Catch:{ Exception -> 0x32c4 }
        r8 = 1;
        r7 = r7 - r8;
        r6 = r6.getLineWidth(r7);	 Catch:{ Exception -> 0x32c4 }
        r7 = r1.captionLayout;	 Catch:{ Exception -> 0x32c4 }
        r9 = r1.captionLayout;	 Catch:{ Exception -> 0x32c4 }
        r9 = r9.getLineCount();	 Catch:{ Exception -> 0x32c4 }
        r9 = r9 - r8;
        r7 = r7.getLineLeft(r9);	 Catch:{ Exception -> 0x32c4 }
        r6 = r6 + r7;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x32c4 }
        r4 = r4 + r7;
        r4 = (float) r4;	 Catch:{ Exception -> 0x32c4 }
        r4 = r4 - r6;
        r0 = (float) r0;	 Catch:{ Exception -> 0x32c4 }
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 >= 0) goto L_0x32bd;
    L_0x32a9:
        r0 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);	 Catch:{ Exception -> 0x32c4 }
        r12 = r12 + r0;
        r0 = r1.addedCaptionHeight;	 Catch:{ Exception -> 0x32c4 }
        r4 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x32c4 }
        r0 = r0 + r4;
        r1.addedCaptionHeight = r0;	 Catch:{ Exception -> 0x32c4 }
        r0 = 1;
        goto L_0x32be;
    L_0x32bd:
        r0 = 0;
    L_0x32be:
        r58 = r12;
        r12 = r0;
        r0 = r58;
        goto L_0x32c8;
    L_0x32c4:
        r0 = move-exception;
        goto L_0x32ce;
    L_0x32c6:
        r0 = 0;
        r12 = 0;
    L_0x32c8:
        r22 = r0;
        r0 = r12;
        goto L_0x32d8;
    L_0x32cc:
        r0 = move-exception;
        r12 = 0;
    L_0x32ce:
        org.telegram.messenger.FileLog.e(r0);
        r22 = r12;
        r0 = 0;
        goto L_0x32d8;
    L_0x32d5:
        r0 = 0;
        r22 = 0;
    L_0x32d8:
        if (r2 == 0) goto L_0x3303;
    L_0x32da:
        r2 = r1.captionWidth;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r2 + r4;
        if (r5 >= r2) goto L_0x3303;
    L_0x32e3:
        r2 = r1.captionWidth;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r2 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 + r2;
        r1.backgroundWidth = r4;
        r4 = r1.mediaBackground;
        if (r4 != 0) goto L_0x3300;
    L_0x32f5:
        r4 = r1.backgroundWidth;
        r5 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 + r5;
        r1.backgroundWidth = r4;
    L_0x3300:
        r24 = r2;
        goto L_0x3305;
    L_0x3303:
        r24 = r5;
    L_0x3305:
        r2 = java.util.Locale.US;
        r4 = 2;
        r5 = new java.lang.Object[r4];
        r3 = (float) r3;
        r4 = org.telegram.messenger.AndroidUtilities.density;
        r3 = r3 / r4;
        r3 = (int) r3;
        r3 = java.lang.Integer.valueOf(r3);
        r4 = 0;
        r5[r4] = r3;
        r3 = (float) r15;
        r4 = org.telegram.messenger.AndroidUtilities.density;
        r3 = r3 / r4;
        r3 = (int) r3;
        r3 = java.lang.Integer.valueOf(r3);
        r4 = 1;
        r5[r4] = r3;
        r3 = "%d_%d";
        r2 = java.lang.String.format(r2, r3, r5);
        r1.currentPhotoFilterThumb = r2;
        r1.currentPhotoFilter = r2;
        r2 = r14.photoThumbs;
        if (r2 == 0) goto L_0x3336;
    L_0x3330:
        r2 = r2.size();
        if (r2 > r4) goto L_0x3342;
    L_0x3336:
        r2 = r14.type;
        r3 = 3;
        if (r2 == r3) goto L_0x3342;
    L_0x333b:
        r3 = 8;
        if (r2 == r3) goto L_0x3342;
    L_0x333f:
        r3 = 5;
        if (r2 != r3) goto L_0x3388;
    L_0x3342:
        r2 = r60.needDrawBluredPreview();
        if (r2 == 0) goto L_0x3373;
    L_0x3348:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = r1.currentPhotoFilter;
        r2.append(r3);
        r3 = "_b2";
        r2.append(r3);
        r2 = r2.toString();
        r1.currentPhotoFilter = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = r1.currentPhotoFilterThumb;
        r2.append(r3);
        r3 = "_b2";
        r2.append(r3);
        r2 = r2.toString();
        r1.currentPhotoFilterThumb = r2;
        goto L_0x3388;
    L_0x3373:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = r1.currentPhotoFilterThumb;
        r2.append(r3);
        r3 = "_b";
        r2.append(r3);
        r2 = r2.toString();
        r1.currentPhotoFilterThumb = r2;
    L_0x3388:
        r2 = r14.type;
        r3 = 3;
        if (r2 == r3) goto L_0x3397;
    L_0x338d:
        r3 = 8;
        if (r2 == r3) goto L_0x3397;
    L_0x3391:
        r3 = 5;
        if (r2 != r3) goto L_0x3395;
    L_0x3394:
        goto L_0x3397;
    L_0x3395:
        r2 = 0;
        goto L_0x3398;
    L_0x3397:
        r2 = 1;
    L_0x3398:
        r3 = r1.currentPhotoObject;
        if (r3 == 0) goto L_0x33a6;
    L_0x339c:
        if (r2 != 0) goto L_0x33a6;
    L_0x339e:
        r4 = r3.size;
        if (r4 != 0) goto L_0x33a6;
    L_0x33a2:
        r10 = -1;
        r3.size = r10;
        goto L_0x33a7;
    L_0x33a6:
        r10 = -1;
    L_0x33a7:
        r3 = r1.currentPhotoObjectThumb;
        if (r3 == 0) goto L_0x33b3;
    L_0x33ab:
        if (r2 != 0) goto L_0x33b3;
    L_0x33ad:
        r4 = r3.size;
        if (r4 != 0) goto L_0x33b3;
    L_0x33b1:
        r3.size = r10;
    L_0x33b3:
        r3 = org.telegram.messenger.SharedConfig.autoplayVideo;
        if (r3 == 0) goto L_0x33f4;
    L_0x33b7:
        r3 = r14.type;
        r12 = 3;
        if (r3 != r12) goto L_0x33f5;
    L_0x33bc:
        r3 = r60.needDrawBluredPreview();
        if (r3 != 0) goto L_0x33f5;
    L_0x33c2:
        r3 = r1.currentMessageObject;
        r3 = r3.mediaExists;
        if (r3 != 0) goto L_0x33dc;
    L_0x33c8:
        r3 = r60.canStreamVideo();
        if (r3 == 0) goto L_0x33f5;
    L_0x33ce:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.DownloadController.getInstance(r3);
        r4 = r1.currentMessageObject;
        r3 = r3.canDownloadMedia(r4);
        if (r3 == 0) goto L_0x33f5;
    L_0x33dc:
        r3 = r1.currentPosition;
        if (r3 == 0) goto L_0x33f0;
    L_0x33e0:
        r3 = r3.flags;
        r4 = r3 & 1;
        if (r4 == 0) goto L_0x33ec;
    L_0x33e6:
        r4 = 2;
        r3 = r3 & r4;
        if (r3 == 0) goto L_0x33ec;
    L_0x33ea:
        r3 = 1;
        goto L_0x33ed;
    L_0x33ec:
        r3 = 0;
    L_0x33ed:
        r1.autoPlayingMedia = r3;
        goto L_0x33f5;
    L_0x33f0:
        r3 = 1;
        r1.autoPlayingMedia = r3;
        goto L_0x33f5;
    L_0x33f4:
        r12 = 3;
    L_0x33f5:
        r3 = r60.getDocument();
        r3 = org.telegram.messenger.MessageObject.isGifDocument(r3);
        if (r3 == 0) goto L_0x344f;
    L_0x33ff:
        r3 = r1.currentPhotoObjectThumb;
        if (r3 == 0) goto L_0x343f;
    L_0x3403:
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r3 == 0) goto L_0x343f;
    L_0x3407:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = r1.currentMessageObject;
        r5 = r1.currentPhotoObjectThumb;
        r4 = org.telegram.messenger.ImageLocation.getStippedKey(r4, r4, r5);
        r3.append(r4);
        r4 = "@";
        r3.append(r4);
        r4 = r1.currentPhotoFilterThumb;
        r3.append(r4);
        r3 = r3.toString();
        r4 = org.telegram.messenger.ImageLoader.getInstance();
        r13 = 0;
        r3 = r4.isInMemCache(r3, r13);
        if (r3 != 0) goto L_0x3450;
    L_0x3430:
        r3 = r60.getDocument();
        r3 = r3.thumbs;
        r4 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r3 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r3, r4);
        r1.currentPhotoObjectThumb = r3;
        goto L_0x3450;
    L_0x343f:
        r13 = 0;
        r3 = r60.getDocument();
        r3 = r3.thumbs;
        r4 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r3 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r3, r4);
        r1.currentPhotoObjectThumb = r3;
        goto L_0x3450;
    L_0x344f:
        r13 = 0;
    L_0x3450:
        r3 = r1.autoPlayingMedia;
        if (r3 == 0) goto L_0x349d;
    L_0x3454:
        r2 = r1.photoImage;
        r3 = 1;
        r2.setAllowStartAnimation(r3);
        r2 = r1.photoImage;
        r2.startAnimation();
        r2 = r60.getDocument();
        r3 = r1.photoImage;
        r4 = org.telegram.messenger.ImageLocation.getForDocument(r2);
        r5 = r1.currentPhotoObject;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilter;
        r7 = r1.currentPhotoObjectThumb;
        r7 = org.telegram.messenger.ImageLocation.getForDocument(r7, r2);
        r8 = r1.currentPhotoFilterThumb;
        r9 = 0;
        r2 = r60.getDocument();
        r11 = r2.size;
        r23 = 0;
        r26 = 0;
        r27 = "g";
        r2 = r3;
        r3 = r4;
        r4 = r27;
        r10 = r11;
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11 = r23;
        r12 = r60;
        r23 = r15;
        r15 = 0;
        r13 = r26;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
        goto L_0x369a;
    L_0x349d:
        r23 = r15;
        r15 = 0;
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r3 = r14.type;
        r4 = 1;
        if (r3 != r4) goto L_0x3557;
    L_0x34a7:
        r3 = r14.useCustomPhoto;
        if (r3 == 0) goto L_0x34bd;
    L_0x34ab:
        r2 = r1.photoImage;
        r3 = r59.getResources();
        r4 = NUM; // 0x7var_c9 float:1.7946024E38 double:1.0529358553E-314;
        r3 = r3.getDrawable(r4);
        r2.setImageBitmap(r3);
        goto L_0x369a;
    L_0x34bd:
        r3 = r1.currentPhotoObject;
        if (r3 == 0) goto L_0x354f;
    L_0x34c1:
        r3 = org.telegram.messenger.FileLoader.getAttachFileName(r3);
        r4 = r14.mediaExists;
        if (r4 == 0) goto L_0x34d4;
    L_0x34c9:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r4.removeLoadingFileObserver(r1);
        r4 = 1;
        goto L_0x34d5;
    L_0x34d4:
        r4 = 0;
    L_0x34d5:
        if (r4 != 0) goto L_0x351f;
    L_0x34d7:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r5 = r1.currentMessageObject;
        r4 = r4.canDownloadMedia(r5);
        if (r4 != 0) goto L_0x351f;
    L_0x34e5:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r3 = r4.isLoadingFile(r3);
        if (r3 == 0) goto L_0x34f2;
    L_0x34f1:
        goto L_0x351f;
    L_0x34f2:
        r3 = 1;
        r1.photoNotSet = r3;
        r2 = r1.currentPhotoObjectThumb;
        if (r2 == 0) goto L_0x3517;
    L_0x34f9:
        r3 = r1.photoImage;
        r4 = 0;
        r5 = 0;
        r6 = r1.photoParentObject;
        r6 = org.telegram.messenger.ImageLocation.getForObject(r2, r6);
        r7 = r1.currentPhotoFilterThumb;
        r8 = 0;
        r9 = 0;
        r10 = r1.currentMessageObject;
        r2 = r10.shouldEncryptPhotoOrVideo();
        if (r2 == 0) goto L_0x3511;
    L_0x350f:
        r11 = 2;
        goto L_0x3512;
    L_0x3511:
        r11 = 0;
    L_0x3512:
        r3.setImage(r4, r5, r6, r7, r8, r9, r10, r11);
        goto L_0x369a;
    L_0x3517:
        r2 = r1.photoImage;
        r3 = 0;
        r2.setImageBitmap(r3);
        goto L_0x369a;
    L_0x351f:
        r4 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r5 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r3, r5);
        r6 = r1.currentPhotoFilter;
        r3 = r1.currentPhotoObjectThumb;
        r7 = r1.photoParentObject;
        r7 = org.telegram.messenger.ImageLocation.getForObject(r3, r7);
        r8 = r1.currentPhotoFilterThumb;
        if (r2 == 0) goto L_0x3539;
    L_0x3537:
        r9 = 0;
        goto L_0x353e;
    L_0x3539:
        r2 = r1.currentPhotoObject;
        r12 = r2.size;
        r9 = r12;
    L_0x353e:
        r10 = 0;
        r11 = r1.currentMessageObject;
        r2 = r11.shouldEncryptPhotoOrVideo();
        if (r2 == 0) goto L_0x3549;
    L_0x3547:
        r12 = 2;
        goto L_0x354a;
    L_0x3549:
        r12 = 0;
    L_0x354a:
        r4.setImage(r5, r6, r7, r8, r9, r10, r11, r12);
        goto L_0x369a;
    L_0x354f:
        r2 = r1.photoImage;
        r3 = 0;
        r2.setImageBitmap(r3);
        goto L_0x369a;
    L_0x3557:
        r2 = 8;
        if (r3 == r2) goto L_0x3589;
    L_0x355b:
        r2 = 5;
        if (r3 != r2) goto L_0x355f;
    L_0x355e:
        goto L_0x3589;
    L_0x355f:
        r2 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r4 = r1.photoParentObject;
        r3 = org.telegram.messenger.ImageLocation.getForObject(r3, r4);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilterThumb;
        r7 = 0;
        r8 = 0;
        r9 = r1.currentMessageObject;
        r9 = r9.shouldEncryptPhotoOrVideo();
        if (r9 == 0) goto L_0x3581;
    L_0x357f:
        r10 = 2;
        goto L_0x3582;
    L_0x3581:
        r10 = 0;
    L_0x3582:
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x369a;
    L_0x3589:
        r2 = r60.getDocument();
        r2 = org.telegram.messenger.FileLoader.getAttachFileName(r2);
        r3 = r14.attachPathExists;
        if (r3 == 0) goto L_0x35a0;
    L_0x3595:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.DownloadController.getInstance(r3);
        r3.removeLoadingFileObserver(r1);
        r3 = 1;
        goto L_0x35a7;
    L_0x35a0:
        r3 = r14.mediaExists;
        if (r3 == 0) goto L_0x35a6;
    L_0x35a4:
        r3 = 2;
        goto L_0x35a7;
    L_0x35a6:
        r3 = 0;
    L_0x35a7:
        r4 = r60.getDocument();
        r4 = org.telegram.messenger.MessageObject.isGifDocument(r4);
        if (r4 != 0) goto L_0x35b9;
    L_0x35b1:
        r4 = r14.type;
        r5 = 5;
        if (r4 != r5) goto L_0x35b7;
    L_0x35b6:
        goto L_0x35b9;
    L_0x35b7:
        r12 = 0;
        goto L_0x35c5;
    L_0x35b9:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r5 = r1.currentMessageObject;
        r12 = r4.canDownloadMedia(r5);
    L_0x35c5:
        r4 = r60.isSending();
        if (r4 != 0) goto L_0x367c;
    L_0x35cb:
        r4 = r60.isEditing();
        if (r4 != 0) goto L_0x367c;
    L_0x35d1:
        if (r3 != 0) goto L_0x35e1;
    L_0x35d3:
        r4 = r1.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r2 = r4.isLoadingFile(r2);
        if (r2 != 0) goto L_0x35e1;
    L_0x35df:
        if (r12 == 0) goto L_0x367c;
    L_0x35e1:
        r2 = 1;
        if (r3 == r2) goto L_0x3626;
    L_0x35e4:
        r4 = r60.needDrawBluredPreview();
        if (r4 != 0) goto L_0x3626;
    L_0x35ea:
        if (r3 != 0) goto L_0x35f4;
    L_0x35ec:
        r4 = r60.canStreamVideo();
        if (r4 == 0) goto L_0x3626;
    L_0x35f2:
        if (r12 == 0) goto L_0x3626;
    L_0x35f4:
        r1.autoPlayingMedia = r2;
        r2 = r1.photoImage;
        r3 = r60.getDocument();
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3);
        r4 = r1.currentPhotoObject;
        r5 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r4, r5);
        r6 = r1.currentPhotoFilter;
        r4 = r1.currentPhotoObjectThumb;
        r7 = r1.photoParentObject;
        r7 = org.telegram.messenger.ImageLocation.getForObject(r4, r7);
        r8 = r1.currentPhotoFilterThumb;
        r9 = 0;
        r4 = r60.getDocument();
        r10 = r4.size;
        r11 = 0;
        r13 = 0;
        r4 = "g";
        r12 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
        goto L_0x369a;
    L_0x3626:
        if (r3 != r2) goto L_0x364e;
    L_0x3628:
        r2 = r1.photoImage;
        r3 = r60.isSendError();
        if (r3 == 0) goto L_0x3632;
    L_0x3630:
        r3 = 0;
        goto L_0x3636;
    L_0x3632:
        r3 = r14.messageOwner;
        r3 = r3.attachPath;
    L_0x3636:
        r3 = org.telegram.messenger.ImageLocation.getForPath(r3);
        r4 = 0;
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilterThumb;
        r7 = 0;
        r8 = 0;
        r10 = 0;
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
        goto L_0x369a;
    L_0x364e:
        r2 = r1.photoImage;
        r3 = r60.getDocument();
        r3 = org.telegram.messenger.ImageLocation.getForDocument(r3);
        r4 = 0;
        r5 = r1.currentPhotoObject;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilter;
        r7 = r1.currentPhotoObjectThumb;
        r8 = r1.photoParentObject;
        r7 = org.telegram.messenger.ImageLocation.getForObject(r7, r8);
        r8 = r1.currentPhotoFilterThumb;
        r9 = 0;
        r10 = r60.getDocument();
        r10 = r10.size;
        r11 = 0;
        r13 = 0;
        r12 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
        goto L_0x369a;
    L_0x367c:
        r2 = r1.photoImage;
        r3 = r1.currentPhotoObject;
        r4 = r1.photoParentObject;
        r3 = org.telegram.messenger.ImageLocation.getForObject(r3, r4);
        r4 = r1.currentPhotoFilter;
        r5 = r1.currentPhotoObjectThumb;
        r6 = r1.photoParentObject;
        r5 = org.telegram.messenger.ImageLocation.getForObject(r5, r6);
        r6 = r1.currentPhotoFilterThumb;
        r7 = 0;
        r8 = 0;
        r10 = 0;
        r9 = r60;
        r2.setImage(r3, r4, r5, r6, r7, r8, r9, r10);
    L_0x369a:
        r12 = r0;
        r57 = r22;
        r3 = r23;
        r0 = r24;
    L_0x36a1:
        r59.setMessageObjectInternal(r60);
        r2 = r1.drawForwardedName;
        if (r2 == 0) goto L_0x36c7;
    L_0x36a8:
        r2 = r60.needDrawForwarded();
        if (r2 == 0) goto L_0x36c7;
    L_0x36ae:
        r2 = r1.currentPosition;
        if (r2 == 0) goto L_0x36b6;
    L_0x36b2:
        r2 = r2.minY;
        if (r2 != 0) goto L_0x36c7;
    L_0x36b6:
        r2 = r14.type;
        r4 = 5;
        if (r2 == r4) goto L_0x36dc;
    L_0x36bb:
        r2 = r1.namesOffset;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 + r4;
        r1.namesOffset = r2;
        goto L_0x36dc;
    L_0x36c7:
        r2 = r1.drawNameLayout;
        if (r2 == 0) goto L_0x36dc;
    L_0x36cb:
        r2 = r14.messageOwner;
        r2 = r2.reply_to_msg_id;
        if (r2 != 0) goto L_0x36dc;
    L_0x36d1:
        r2 = r1.namesOffset;
        r4 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 + r4;
        r1.namesOffset = r2;
    L_0x36dc:
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r2 + r3;
        r4 = r1.namesOffset;
        r2 = r2 + r4;
        r2 = r2 + r57;
        r1.totalHeight = r2;
        r2 = r1.currentPosition;
        if (r2 == 0) goto L_0x3700;
    L_0x36ee:
        r2 = r2.flags;
        r4 = 8;
        r2 = r2 & r4;
        if (r2 != 0) goto L_0x3700;
    L_0x36f5:
        r2 = r1.totalHeight;
        r4 = NUM; // 0x40400000 float:3.0 double:5.325712093E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 - r4;
        r1.totalHeight = r2;
    L_0x3700:
        r2 = r1.currentPosition;
        if (r2 == 0) goto L_0x3731;
    L_0x3704:
        r2 = r1.getAdditionalWidthForPosition(r2);
        r0 = r0 + r2;
        r2 = r1.currentPosition;
        r2 = r2.flags;
        r2 = r2 & 4;
        if (r2 != 0) goto L_0x3721;
    L_0x3711:
        r2 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 + r2;
        r2 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = 0 - r2;
        goto L_0x3722;
    L_0x3721:
        r2 = 0;
    L_0x3722:
        r4 = r1.currentPosition;
        r4 = r4.flags;
        r5 = 8;
        r4 = r4 & r5;
        if (r4 != 0) goto L_0x3732;
    L_0x372b:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r3 = r3 + r4;
        goto L_0x3732;
    L_0x3731:
        r2 = 0;
    L_0x3732:
        r4 = r1.drawPinnedTop;
        if (r4 == 0) goto L_0x373f;
    L_0x3736:
        r4 = r1.namesOffset;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r4 = r4 - r5;
        r1.namesOffset = r4;
    L_0x373f:
        r4 = r1.currentPosition;
        if (r4 == 0) goto L_0x3769;
    L_0x3743:
        r4 = r1.namesOffset;
        if (r4 <= 0) goto L_0x3757;
    L_0x3747:
        r4 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r1.totalHeight;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r5 = r5 - r6;
        r1.totalHeight = r5;
        goto L_0x378e;
    L_0x3757:
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r1.totalHeight;
        r6 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 - r6;
        r1.totalHeight = r5;
        goto L_0x378e;
    L_0x3769:
        r4 = r1.namesOffset;
        if (r4 <= 0) goto L_0x377d;
    L_0x376d:
        r4 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r1.totalHeight;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r5 = r5 - r6;
        r1.totalHeight = r5;
        goto L_0x378e;
    L_0x377d:
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r1.totalHeight;
        r6 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 - r6;
        r1.totalHeight = r5;
    L_0x378e:
        r5 = r1.photoImage;
        r6 = r1.namesOffset;
        r4 = r4 + r6;
        r4 = r4 + r2;
        r5.setImageCoords(r15, r4, r0, r3);
        r59.invalidate();
        r9 = r12;
    L_0x379b:
        r0 = r1.currentPosition;
        if (r0 != 0) goto L_0x3895;
    L_0x379f:
        r0 = r60.isAnyKindOfSticker();
        if (r0 != 0) goto L_0x3895;
    L_0x37a5:
        r0 = r1.addedCaptionHeight;
        if (r0 != 0) goto L_0x3895;
    L_0x37a9:
        r0 = r1.captionLayout;
        if (r0 != 0) goto L_0x3809;
    L_0x37ad:
        r0 = r14.caption;
        if (r0 == 0) goto L_0x3809;
    L_0x37b1:
        r1.currentCaption = r0;	 Catch:{ Exception -> 0x3805 }
        r0 = r1.backgroundWidth;	 Catch:{ Exception -> 0x3805 }
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);	 Catch:{ Exception -> 0x3805 }
        r0 = r0 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);	 Catch:{ Exception -> 0x3805 }
        r0 = r0 - r2;
        r2 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x3805 }
        r3 = 24;
        if (r2 < r3) goto L_0x37eb;
    L_0x37c7:
        r2 = r14.caption;	 Catch:{ Exception -> 0x3805 }
        r3 = r14.caption;	 Catch:{ Exception -> 0x3805 }
        r3 = r3.length();	 Catch:{ Exception -> 0x3805 }
        r4 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x3805 }
        r0 = android.text.StaticLayout.Builder.obtain(r2, r15, r3, r4, r0);	 Catch:{ Exception -> 0x3805 }
        r2 = 1;
        r0 = r0.setBreakStrategy(r2);	 Catch:{ Exception -> 0x3805 }
        r0 = r0.setHyphenationFrequency(r15);	 Catch:{ Exception -> 0x3805 }
        r2 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3805 }
        r0 = r0.setAlignment(r2);	 Catch:{ Exception -> 0x3805 }
        r0 = r0.build();	 Catch:{ Exception -> 0x3805 }
        r1.captionLayout = r0;	 Catch:{ Exception -> 0x3805 }
        goto L_0x3809;
    L_0x37eb:
        r2 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x3805 }
        r3 = r14.caption;	 Catch:{ Exception -> 0x3805 }
        r31 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x3805 }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3805 }
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r2;
        r30 = r3;
        r32 = r0;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);	 Catch:{ Exception -> 0x3805 }
        r1.captionLayout = r2;	 Catch:{ Exception -> 0x3805 }
        goto L_0x3809;
    L_0x3805:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x3809:
        r0 = r1.captionLayout;
        if (r0 == 0) goto L_0x3895;
    L_0x380d:
        r0 = r1.backgroundWidth;	 Catch:{ Exception -> 0x3891 }
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);	 Catch:{ Exception -> 0x3891 }
        r0 = r0 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);	 Catch:{ Exception -> 0x3891 }
        r2 = r0 - r2;
        r3 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        if (r3 == 0) goto L_0x3895;
    L_0x3820:
        r3 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r3 = r3.getLineCount();	 Catch:{ Exception -> 0x3891 }
        if (r3 <= 0) goto L_0x3895;
    L_0x3828:
        r1.captionWidth = r2;	 Catch:{ Exception -> 0x3891 }
        r2 = r1.timeWidth;	 Catch:{ Exception -> 0x3891 }
        r3 = r60.isOutOwner();	 Catch:{ Exception -> 0x3891 }
        if (r3 == 0) goto L_0x3839;
    L_0x3832:
        r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x3891 }
        goto L_0x383a;
    L_0x3839:
        r12 = 0;
    L_0x383a:
        r2 = r2 + r12;
        r3 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r3 = r3.getHeight();	 Catch:{ Exception -> 0x3891 }
        r1.captionHeight = r3;	 Catch:{ Exception -> 0x3891 }
        r3 = r1.totalHeight;	 Catch:{ Exception -> 0x3891 }
        r4 = r1.captionHeight;	 Catch:{ Exception -> 0x3891 }
        r5 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);	 Catch:{ Exception -> 0x3891 }
        r4 = r4 + r5;
        r3 = r3 + r4;
        r1.totalHeight = r3;	 Catch:{ Exception -> 0x3891 }
        r3 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r4 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x3891 }
        r5 = 1;
        r4 = r4 - r5;
        r3 = r3.getLineWidth(r4);	 Catch:{ Exception -> 0x3891 }
        r4 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r6 = r1.captionLayout;	 Catch:{ Exception -> 0x3891 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x3891 }
        r6 = r6 - r5;
        r4 = r4.getLineLeft(r6);	 Catch:{ Exception -> 0x3891 }
        r3 = r3 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r18);	 Catch:{ Exception -> 0x3891 }
        r0 = r0 - r4;
        r0 = (float) r0;	 Catch:{ Exception -> 0x3891 }
        r0 = r0 - r3;
        r2 = (float) r2;	 Catch:{ Exception -> 0x3891 }
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 >= 0) goto L_0x3895;
    L_0x3879:
        r0 = r1.totalHeight;	 Catch:{ Exception -> 0x3891 }
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);	 Catch:{ Exception -> 0x3891 }
        r0 = r0 + r2;
        r1.totalHeight = r0;	 Catch:{ Exception -> 0x3891 }
        r0 = r1.captionHeight;	 Catch:{ Exception -> 0x3891 }
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);	 Catch:{ Exception -> 0x3891 }
        r0 = r0 + r2;
        r1.captionHeight = r0;	 Catch:{ Exception -> 0x3891 }
        r9 = 2;
        goto L_0x3895;
    L_0x3891:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x3895:
        r0 = r1.captionLayout;
        if (r0 != 0) goto L_0x38b0;
    L_0x3899:
        r0 = r1.widthBeforeNewTimeLine;
        r2 = -1;
        if (r0 == r2) goto L_0x38b0;
    L_0x389e:
        r2 = r1.availableTimeWidth;
        r2 = r2 - r0;
        r0 = r1.timeWidth;
        if (r2 >= r0) goto L_0x38b0;
    L_0x38a5:
        r0 = r1.totalHeight;
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.totalHeight = r0;
    L_0x38b0:
        r0 = r1.currentMessageObject;
        r2 = r0.eventId;
        r4 = 0;
        r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r6 == 0) goto L_0x39dc;
    L_0x38ba:
        r0 = r0.isMediaEmpty();
        if (r0 != 0) goto L_0x39dc;
    L_0x38c0:
        r0 = r1.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.media;
        r0 = r0.webpage;
        if (r0 == 0) goto L_0x39dc;
    L_0x38ca:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x42240000 float:41.0 double:5.48242687E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r0 - r2;
        r3 = 1;
        r1.hasOldCaptionPreview = r3;
        r1.linkPreviewHeight = r15;
        r0 = r1.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.media;
        r3 = r0.webpage;
        r0 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3939 }
        r4 = r3.site_name;	 Catch:{ Exception -> 0x3939 }
        r0 = r0.measureText(r4);	 Catch:{ Exception -> 0x3939 }
        r0 = r0 + r27;
        r4 = (double) r0;	 Catch:{ Exception -> 0x3939 }
        r4 = java.lang.Math.ceil(r4);	 Catch:{ Exception -> 0x3939 }
        r0 = (int) r4;	 Catch:{ Exception -> 0x3939 }
        r1.siteNameWidth = r0;	 Catch:{ Exception -> 0x3939 }
        r4 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x3939 }
        r5 = r3.site_name;	 Catch:{ Exception -> 0x3939 }
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3939 }
        r32 = java.lang.Math.min(r0, r2);	 Catch:{ Exception -> 0x3939 }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3939 }
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r4;
        r30 = r5;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);	 Catch:{ Exception -> 0x3939 }
        r1.siteNameLayout = r4;	 Catch:{ Exception -> 0x3939 }
        r0 = r1.siteNameLayout;	 Catch:{ Exception -> 0x3939 }
        r0 = r0.getLineLeft(r15);	 Catch:{ Exception -> 0x3939 }
        r5 = 0;
        r0 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1));
        if (r0 == 0) goto L_0x391b;
    L_0x3919:
        r0 = 1;
        goto L_0x391c;
    L_0x391b:
        r0 = 0;
    L_0x391c:
        r1.siteNameRtl = r0;	 Catch:{ Exception -> 0x3937 }
        r0 = r1.siteNameLayout;	 Catch:{ Exception -> 0x3937 }
        r4 = r1.siteNameLayout;	 Catch:{ Exception -> 0x3937 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x3937 }
        r6 = 1;
        r4 = r4 - r6;
        r0 = r0.getLineBottom(r4);	 Catch:{ Exception -> 0x3937 }
        r4 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x3937 }
        r4 = r4 + r0;
        r1.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x3937 }
        r4 = r1.totalHeight;	 Catch:{ Exception -> 0x3937 }
        r4 = r4 + r0;
        r1.totalHeight = r4;	 Catch:{ Exception -> 0x3937 }
        goto L_0x393e;
    L_0x3937:
        r0 = move-exception;
        goto L_0x393b;
    L_0x3939:
        r0 = move-exception;
        r5 = 0;
    L_0x393b:
        org.telegram.messenger.FileLog.e(r0);
    L_0x393e:
        r1.descriptionX = r15;	 Catch:{ Exception -> 0x39b1 }
        r0 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x39b1 }
        if (r0 == 0) goto L_0x394d;
    L_0x3944:
        r0 = r1.totalHeight;	 Catch:{ Exception -> 0x39b1 }
        r4 = org.telegram.messenger.AndroidUtilities.dp(r19);	 Catch:{ Exception -> 0x39b1 }
        r0 = r0 + r4;
        r1.totalHeight = r0;	 Catch:{ Exception -> 0x39b1 }
    L_0x394d:
        r0 = r3.description;	 Catch:{ Exception -> 0x39b1 }
        r30 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x39b1 }
        r32 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x39b1 }
        r33 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r27);	 Catch:{ Exception -> 0x39b1 }
        r3 = (float) r3;	 Catch:{ Exception -> 0x39b1 }
        r35 = 0;
        r36 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x39b1 }
        r38 = 6;
        r29 = r0;
        r31 = r2;
        r34 = r3;
        r37 = r2;
        r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r29, r30, r31, r32, r33, r34, r35, r36, r37, r38);	 Catch:{ Exception -> 0x39b1 }
        r1.descriptionLayout = r0;	 Catch:{ Exception -> 0x39b1 }
        r0 = r1.descriptionLayout;	 Catch:{ Exception -> 0x39b1 }
        r2 = r1.descriptionLayout;	 Catch:{ Exception -> 0x39b1 }
        r2 = r2.getLineCount();	 Catch:{ Exception -> 0x39b1 }
        r3 = 1;
        r2 = r2 - r3;
        r0 = r0.getLineBottom(r2);	 Catch:{ Exception -> 0x39b1 }
        r2 = r1.linkPreviewHeight;	 Catch:{ Exception -> 0x39b1 }
        r2 = r2 + r0;
        r1.linkPreviewHeight = r2;	 Catch:{ Exception -> 0x39b1 }
        r2 = r1.totalHeight;	 Catch:{ Exception -> 0x39b1 }
        r2 = r2 + r0;
        r1.totalHeight = r2;	 Catch:{ Exception -> 0x39b1 }
        r0 = 0;
    L_0x3987:
        r2 = r1.descriptionLayout;	 Catch:{ Exception -> 0x39b1 }
        r2 = r2.getLineCount();	 Catch:{ Exception -> 0x39b1 }
        if (r0 >= r2) goto L_0x39b5;
    L_0x398f:
        r2 = r1.descriptionLayout;	 Catch:{ Exception -> 0x39b1 }
        r2 = r2.getLineLeft(r0);	 Catch:{ Exception -> 0x39b1 }
        r2 = (double) r2;	 Catch:{ Exception -> 0x39b1 }
        r2 = java.lang.Math.ceil(r2);	 Catch:{ Exception -> 0x39b1 }
        r2 = (int) r2;	 Catch:{ Exception -> 0x39b1 }
        if (r2 == 0) goto L_0x39ae;
    L_0x399d:
        r3 = r1.descriptionX;	 Catch:{ Exception -> 0x39b1 }
        if (r3 != 0) goto L_0x39a5;
    L_0x39a1:
        r2 = -r2;
        r1.descriptionX = r2;	 Catch:{ Exception -> 0x39b1 }
        goto L_0x39ae;
    L_0x39a5:
        r3 = r1.descriptionX;	 Catch:{ Exception -> 0x39b1 }
        r2 = -r2;
        r2 = java.lang.Math.max(r3, r2);	 Catch:{ Exception -> 0x39b1 }
        r1.descriptionX = r2;	 Catch:{ Exception -> 0x39b1 }
    L_0x39ae:
        r0 = r0 + 1;
        goto L_0x3987;
    L_0x39b1:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x39b5:
        r0 = r1.totalHeight;
        r2 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.totalHeight = r0;
        if (r9 == 0) goto L_0x39dd;
    L_0x39c2:
        r0 = r1.totalHeight;
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r1.totalHeight = r0;
        r2 = 2;
        if (r9 != r2) goto L_0x39dd;
    L_0x39d0:
        r0 = r1.captionHeight;
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r1.captionHeight = r0;
        goto L_0x39dd;
    L_0x39dc:
        r5 = 0;
    L_0x39dd:
        r0 = r1.botButtons;
        r0.clear();
        if (r16 == 0) goto L_0x39f1;
    L_0x39e4:
        r0 = r1.botButtonsByData;
        r0.clear();
        r0 = r1.botButtonsByPosition;
        r0.clear();
        r2 = 0;
        r1.botButtonsLayout = r2;
    L_0x39f1:
        r0 = r1.currentPosition;
        if (r0 != 0) goto L_0x3d15;
    L_0x39f5:
        r0 = r14.messageOwner;
        r2 = r0.reply_markup;
        r2 = r2 instanceof org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
        if (r2 != 0) goto L_0x3a09;
    L_0x39fd:
        r0 = r0.reactions;
        if (r0 == 0) goto L_0x3d15;
    L_0x3a01:
        r0 = r0.results;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x3d15;
    L_0x3a09:
        r0 = r14.messageOwner;
        r0 = r0.reply_markup;
        r2 = r0 instanceof org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
        if (r2 == 0) goto L_0x3a18;
    L_0x3a11:
        r0 = r0.rows;
        r13 = r0.size();
        goto L_0x3a19;
    L_0x3a18:
        r13 = 1;
    L_0x3a19:
        r0 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = r0 * r13;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r0 = r0 + r2;
        r1.keyboardHeight = r0;
        r1.substractBackgroundHeight = r0;
        r0 = r1.backgroundWidth;
        r2 = r1.mediaBackground;
        if (r2 == 0) goto L_0x3a31;
    L_0x3a30:
        goto L_0x3a35;
    L_0x3a31:
        r10 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r5 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
    L_0x3a35:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 - r2;
        r1.widthForButtons = r0;
        r0 = r14.wantedBotKeyboardWidth;
        r2 = r1.widthForButtons;
        if (r0 <= r2) goto L_0x3a87;
    L_0x3a42:
        r0 = r1.isChat;
        if (r0 == 0) goto L_0x3a55;
    L_0x3a46:
        r0 = r60.needDrawAvatar();
        if (r0 == 0) goto L_0x3a55;
    L_0x3a4c:
        r0 = r60.isOutOwner();
        if (r0 != 0) goto L_0x3a55;
    L_0x3a52:
        r0 = NUM; // 0x42780000 float:62.0 double:5.5096253E-315;
        goto L_0x3a57;
    L_0x3a55:
        r0 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
    L_0x3a57:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = -r0;
        r2 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r2 == 0) goto L_0x3a67;
    L_0x3a62:
        r2 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        goto L_0x3a78;
    L_0x3a67:
        r2 = org.telegram.messenger.AndroidUtilities.displaySize;
        r3 = r2.x;
        r2 = r2.y;
        r2 = java.lang.Math.min(r3, r2);
        r3 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
    L_0x3a78:
        r0 = r0 + r2;
        r2 = r1.backgroundWidth;
        r3 = r14.wantedBotKeyboardWidth;
        r0 = java.lang.Math.min(r3, r0);
        r0 = java.lang.Math.max(r2, r0);
        r1.widthForButtons = r0;
    L_0x3a87:
        r0 = new java.util.HashMap;
        r2 = r1.botButtonsByData;
        r0.<init>(r2);
        r2 = r14.botButtonsLayout;
        if (r2 == 0) goto L_0x3aa8;
    L_0x3a92:
        r3 = r1.botButtonsLayout;
        if (r3 == 0) goto L_0x3aa8;
    L_0x3a96:
        r2 = r2.toString();
        r2 = r3.equals(r2);
        if (r2 == 0) goto L_0x3aa8;
    L_0x3aa0:
        r2 = new java.util.HashMap;
        r3 = r1.botButtonsByPosition;
        r2.<init>(r3);
        goto L_0x3ab3;
    L_0x3aa8:
        r2 = r14.botButtonsLayout;
        if (r2 == 0) goto L_0x3ab2;
    L_0x3aac:
        r2 = r2.toString();
        r1.botButtonsLayout = r2;
    L_0x3ab2:
        r2 = 0;
    L_0x3ab3:
        r3 = r1.botButtonsByData;
        r3.clear();
        r3 = r14.messageOwner;
        r4 = r3.reply_markup;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
        if (r4 == 0) goto L_0x3c0a;
    L_0x3ac0:
        r3 = 0;
        r4 = 0;
    L_0x3ac2:
        if (r3 >= r13) goto L_0x3d12;
    L_0x3ac4:
        r5 = r14.messageOwner;
        r5 = r5.reply_markup;
        r5 = r5.rows;
        r5 = r5.get(r3);
        r5 = (org.telegram.tgnet.TLRPC.TL_keyboardButtonRow) r5;
        r6 = r5.buttons;
        r6 = r6.size();
        if (r6 != 0) goto L_0x3ada;
    L_0x3ad8:
        goto L_0x3CLASSNAME;
    L_0x3ada:
        r7 = r1.widthForButtons;
        r8 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r9 = r6 + -1;
        r8 = r8 * r9;
        r7 = r7 - r8;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r7 = r7 - r8;
        r7 = r7 / r6;
        r6 = r4;
        r4 = 0;
    L_0x3aef:
        r8 = r5.buttons;
        r8 = r8.size();
        if (r4 >= r8) goto L_0x3CLASSNAME;
    L_0x3af7:
        r8 = new org.telegram.ui.Cells.ChatMessageCell$BotButton;
        r9 = 0;
        r8.<init>(r1, r9);
        r9 = r5.buttons;
        r9 = r9.get(r4);
        r9 = (org.telegram.tgnet.TLRPC.KeyboardButton) r9;
        r8.button = r9;
        r9 = r8.button;
        r9 = r9.data;
        r9 = org.telegram.messenger.Utilities.bytesToHex(r9);
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r10.append(r3);
        r11 = "";
        r10.append(r11);
        r10.append(r4);
        r10 = r10.toString();
        if (r2 == 0) goto L_0x3b2f;
    L_0x3b28:
        r11 = r2.get(r10);
        r11 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r11;
        goto L_0x3b35;
    L_0x3b2f:
        r11 = r0.get(r9);
        r11 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r11;
    L_0x3b35:
        if (r11 == 0) goto L_0x3b4d;
    L_0x3b37:
        r12 = r11.progressAlpha;
        r8.progressAlpha = r12;
        r12 = r11.angle;
        r8.angle = r12;
        r11 = r11.lastUpdateTime;
        r8.lastUpdateTime = r11;
        goto L_0x3b54;
    L_0x3b4d:
        r11 = java.lang.System.currentTimeMillis();
        r8.lastUpdateTime = r11;
    L_0x3b54:
        r11 = r1.botButtonsByData;
        r11.put(r9, r8);
        r9 = r1.botButtonsByPosition;
        r9.put(r10, r8);
        r9 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r9 = r9 + r7;
        r9 = r9 * r4;
        r8.x = r9;
        r9 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r9 = r9 * r3;
        r10 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 + r10;
        r8.y = r9;
        r8.width = r7;
        r9 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r8.height = r9;
        r9 = r8.button;
        r9 = r9 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
        if (r9 == 0) goto L_0x3ba4;
    L_0x3b90:
        r9 = r14.messageOwner;
        r9 = r9.media;
        r9 = r9.flags;
        r9 = r9 & 4;
        if (r9 == 0) goto L_0x3ba4;
    L_0x3b9a:
        r9 = NUM; // 0x7f0e087c float:1.8879443E38 double:1.0531632297E-314;
        r10 = "PaymentReceipt";
        r9 = org.telegram.messenger.LocaleController.getString(r10, r9);
        goto L_0x3bc9;
    L_0x3ba4:
        r9 = r8.button;
        r9 = r9.text;
        r10 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r10 = r10.getFontMetricsInt();
        r11 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r9 = org.telegram.messenger.Emoji.replaceEmoji(r9, r10, r11, r15);
        r10 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r11 = r7 - r11;
        r11 = (float) r11;
        r12 = android.text.TextUtils.TruncateAt.END;
        r9 = android.text.TextUtils.ellipsize(r9, r10, r11, r12);
    L_0x3bc9:
        r30 = r9;
        r9 = new android.text.StaticLayout;
        r31 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r32 = r7 - r10;
        r33 = android.text.Layout.Alignment.ALIGN_CENTER;
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r9;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);
        r8.title = r9;
        r9 = r1.botButtons;
        r9.add(r8);
        r9 = r5.buttons;
        r9 = r9.size();
        r10 = 1;
        r9 = r9 - r10;
        if (r4 != r9) goto L_0x3CLASSNAME;
    L_0x3bf4:
        r9 = r8.x;
        r8 = r8.width;
        r9 = r9 + r8;
        r6 = java.lang.Math.max(r6, r9);
    L_0x3CLASSNAME:
        r4 = r4 + 1;
        goto L_0x3aef;
    L_0x3CLASSNAME:
        r4 = r6;
    L_0x3CLASSNAME:
        r3 = r3 + 1;
        goto L_0x3ac2;
    L_0x3c0a:
        r3 = r3.reactions;
        r3 = r3.results;
        r3 = r3.size();
        r4 = r1.widthForButtons;
        r5 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r6 = r3 + -1;
        r5 = r5 * r6;
        r4 = r4 - r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r4 = r4 - r5;
        r4 = r4 / r3;
        r5 = 0;
        r7 = 0;
    L_0x3CLASSNAME:
        if (r5 >= r3) goto L_0x3d11;
    L_0x3CLASSNAME:
        r8 = r14.messageOwner;
        r8 = r8.reactions;
        r8 = r8.results;
        r8 = r8.get(r5);
        r8 = (org.telegram.tgnet.TLRPC.TL_reactionCount) r8;
        r9 = new org.telegram.ui.Cells.ChatMessageCell$BotButton;
        r10 = 0;
        r9.<init>(r1, r10);
        r9.reaction = r8;
        r10 = r8.reaction;
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "0";
        r11.append(r12);
        r11.append(r5);
        r11 = r11.toString();
        if (r2 == 0) goto L_0x3c5a;
    L_0x3CLASSNAME:
        r12 = r2.get(r11);
        r12 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r12;
        goto L_0x3CLASSNAME;
    L_0x3c5a:
        r12 = r0.get(r10);
        r12 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r12;
    L_0x3CLASSNAME:
        if (r12 == 0) goto L_0x3CLASSNAME;
    L_0x3CLASSNAME:
        r13 = r12.progressAlpha;
        r9.progressAlpha = r13;
        r13 = r12.angle;
        r9.angle = r13;
        r12 = r12.lastUpdateTime;
        r9.lastUpdateTime = r12;
        goto L_0x3c7f;
    L_0x3CLASSNAME:
        r12 = java.lang.System.currentTimeMillis();
        r9.lastUpdateTime = r12;
    L_0x3c7f:
        r12 = r1.botButtonsByData;
        r12.put(r10, r9);
        r10 = r1.botButtonsByPosition;
        r10.put(r11, r9);
        r10 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r10 = r10 + r4;
        r10 = r10 * r5;
        r9.x = r10;
        r10 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9.y = r10;
        r9.width = r4;
        r10 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9.height = r10;
        r10 = 2;
        r11 = new java.lang.Object[r10];
        r10 = r8.count;
        r10 = java.lang.Integer.valueOf(r10);
        r11[r15] = r10;
        r8 = r8.reaction;
        r10 = 1;
        r11[r10] = r8;
        r8 = "%d %s";
        r8 = java.lang.String.format(r8, r11);
        r10 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r10 = r10.getFontMetricsInt();
        r11 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r8 = org.telegram.messenger.Emoji.replaceEmoji(r8, r10, r11, r15);
        r10 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r11 = r4 - r11;
        r11 = (float) r11;
        r12 = android.text.TextUtils.TruncateAt.END;
        r30 = android.text.TextUtils.ellipsize(r8, r10, r11, r12);
        r8 = new android.text.StaticLayout;
        r31 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r32 = r4 - r10;
        r33 = android.text.Layout.Alignment.ALIGN_CENTER;
        r34 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r35 = 0;
        r36 = 0;
        r29 = r8;
        r29.<init>(r30, r31, r32, r33, r34, r35, r36);
        r9.title = r8;
        r8 = r1.botButtons;
        r8.add(r9);
        if (r5 != r6) goto L_0x3d0d;
    L_0x3d00:
        r8 = r9.x;
        r9 = r9.width;
        r8 = r8 + r9;
        r7 = java.lang.Math.max(r7, r8);
    L_0x3d0d:
        r5 = r5 + 1;
        goto L_0x3CLASSNAME;
    L_0x3d11:
        r4 = r7;
    L_0x3d12:
        r1.widthForButtons = r4;
        goto L_0x3d19;
    L_0x3d15:
        r1.substractBackgroundHeight = r15;
        r1.keyboardHeight = r15;
    L_0x3d19:
        r0 = r1.drawPinnedBottom;
        if (r0 == 0) goto L_0x3d2b;
    L_0x3d1d:
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x3d2b;
    L_0x3d21:
        r0 = r1.totalHeight;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r0 = r0 - r2;
        r1.totalHeight = r0;
        goto L_0x3d52;
    L_0x3d2b:
        r0 = r1.drawPinnedBottom;
        if (r0 == 0) goto L_0x3d39;
    L_0x3d2f:
        r0 = r1.totalHeight;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r0 = r0 - r2;
        r1.totalHeight = r0;
        goto L_0x3d52;
    L_0x3d39:
        r0 = r1.drawPinnedTop;
        if (r0 == 0) goto L_0x3d52;
    L_0x3d3d:
        r0 = r1.pinnedBottom;
        if (r0 == 0) goto L_0x3d52;
    L_0x3d41:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x3d52;
    L_0x3d45:
        r0 = r0.siblingHeights;
        if (r0 != 0) goto L_0x3d52;
    L_0x3d49:
        r0 = r1.totalHeight;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r0 = r0 - r2;
        r1.totalHeight = r0;
    L_0x3d52:
        r0 = r60.isAnyKindOfSticker();
        if (r0 == 0) goto L_0x3d6b;
    L_0x3d58:
        r0 = r1.totalHeight;
        r2 = NUM; // 0x428CLASSNAME float:70.0 double:5.51610112E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        if (r0 >= r2) goto L_0x3d6b;
    L_0x3d62:
        r0 = NUM; // 0x428CLASSNAME float:70.0 double:5.51610112E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r1.totalHeight = r0;
        goto L_0x3d7c;
    L_0x3d6b:
        r0 = r60.isAnimatedEmoji();
        if (r0 == 0) goto L_0x3d7c;
    L_0x3d71:
        r0 = r1.totalHeight;
        r2 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.totalHeight = r0;
    L_0x3d7c:
        r0 = r1.drawPhotoImage;
        if (r0 != 0) goto L_0x3d86;
    L_0x3d80:
        r0 = r1.photoImage;
        r2 = 0;
        r0.setImageBitmap(r2);
    L_0x3d86:
        r0 = r1.documentAttachType;
        r2 = 5;
        if (r0 != r2) goto L_0x3dbd;
    L_0x3d8b:
        r0 = r1.documentAttach;
        r0 = org.telegram.messenger.MessageObject.isDocumentHasThumb(r0);
        if (r0 == 0) goto L_0x3da5;
    L_0x3d93:
        r0 = r1.documentAttach;
        r0 = r0.thumbs;
        r2 = 90;
        r0 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r0, r2);
        r2 = r1.radialProgress;
        r3 = r1.documentAttach;
        r2.setImageOverlay(r0, r3, r14);
        goto L_0x3dc3;
    L_0x3da5:
        r2 = 1;
        r0 = r14.getArtworkUrl(r2);
        r2 = android.text.TextUtils.isEmpty(r0);
        if (r2 != 0) goto L_0x3db6;
    L_0x3db0:
        r2 = r1.radialProgress;
        r2.setImageOverlay(r0);
        goto L_0x3dc3;
    L_0x3db6:
        r0 = r1.radialProgress;
        r2 = 0;
        r0.setImageOverlay(r2, r2, r2);
        goto L_0x3dc3;
    L_0x3dbd:
        r2 = 0;
        r0 = r1.radialProgress;
        r0.setImageOverlay(r2, r2, r2);
    L_0x3dc3:
        r59.updateWaveform();
        if (r17 == 0) goto L_0x3dce;
    L_0x3dc8:
        r0 = r14.cancelEditing;
        if (r0 != 0) goto L_0x3dce;
    L_0x3dcc:
        r0 = 1;
        goto L_0x3dcf;
    L_0x3dce:
        r0 = 0;
    L_0x3dcf:
        r2 = 1;
        r1.updateButtonState(r15, r0, r2);
        r0 = r1.buttonState;
        r2 = 2;
        if (r0 != r2) goto L_0x3e03;
    L_0x3dd8:
        r0 = r1.documentAttachType;
        r2 = 3;
        if (r0 != r2) goto L_0x3e03;
    L_0x3ddd:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.DownloadController.getInstance(r0);
        r0 = r0.canDownloadMedia(r14);
        if (r0 == 0) goto L_0x3e03;
    L_0x3de9:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.FileLoader.getInstance(r0);
        r2 = r1.documentAttach;
        r3 = r1.currentMessageObject;
        r4 = 1;
        r0.loadFile(r2, r3, r4, r15);
        r0 = 4;
        r1.buttonState = r0;
        r0 = r1.radialProgress;
        r2 = r59.getIconForCurrentState();
        r0.setIcon(r2, r15, r15);
    L_0x3e03:
        r0 = r1.accessibilityVirtualViewBounds;
        r0.clear();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.setMessageContent(org.telegram.messenger.MessageObject, org.telegram.messenger.MessageObject$GroupedMessages, boolean, boolean):void");
    }

    static /* synthetic */ int lambda$setMessageContent$0(PollButton pollButton, PollButton pollButton2) {
        if (pollButton.decimal > pollButton2.decimal) {
            return -1;
        }
        return pollButton.decimal < pollButton2.decimal ? 1 : 0;
    }

    public void checkVideoPlayback(boolean z) {
        if (!this.currentMessageObject.isVideo()) {
            if (z) {
                MessageObject playingMessageObject = MediaController.getInstance().getPlayingMessageObject();
                z = playingMessageObject == null || !playingMessageObject.isRoundVideo();
            }
            this.photoImage.setAllowStartAnimation(z);
            if (z) {
                this.photoImage.startAnimation();
            } else {
                this.photoImage.stopAnimation();
            }
        } else if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
            this.photoImage.setAllowStartAnimation(false);
            this.photoImage.stopAnimation();
        } else {
            this.photoImage.setAllowStartAnimation(true);
            this.photoImage.startAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLongPress() {
        CharacterStyle characterStyle = this.pressedLink;
        if (characterStyle instanceof URLSpanMono) {
            this.delegate.didPressUrl(this, characterStyle, true);
            return;
        }
        if (characterStyle instanceof URLSpanNoUnderline) {
            if (((URLSpanNoUnderline) characterStyle).getURL().startsWith("/")) {
                this.delegate.didPressUrl(this, this.pressedLink, true);
                return;
            }
        } else if (characterStyle instanceof URLSpan) {
            this.delegate.didPressUrl(this, characterStyle, true);
            return;
        }
        resetPressedLink(-1);
        if (!(this.buttonPressed == 0 && this.miniButtonPressed == 0 && this.videoButtonPressed == 0 && this.pressedBotButton == -1)) {
            this.buttonPressed = 0;
            this.miniButtonPressed = 0;
            this.videoButtonPressed = 0;
            this.pressedBotButton = -1;
            invalidate();
        }
        this.linkPreviewPressed = false;
        this.otherPressed = false;
        this.sharePressed = false;
        this.imagePressed = false;
        this.gamePreviewPressed = false;
        if (this.instantPressed) {
            this.instantButtonPressed = false;
            this.instantPressed = false;
            if (VERSION.SDK_INT >= 21) {
                Drawable drawable = this.selectorDrawable;
                if (drawable != null) {
                    drawable.setState(StateSet.NOTHING);
                }
            }
            invalidate();
        }
        if (this.pressedVoteButton != -1) {
            this.pressedVoteButton = -1;
            if (VERSION.SDK_INT >= 21) {
                Drawable drawable2 = this.selectorDrawable;
                if (drawable2 != null) {
                    drawable2.setState(StateSet.NOTHING);
                }
            }
            invalidate();
        }
        ChatMessageCellDelegate chatMessageCellDelegate = this.delegate;
        if (chatMessageCellDelegate != null) {
            chatMessageCellDelegate.didLongPress(this, this.lastTouchX, this.lastTouchY);
        }
    }

    public void setCheckPressed(boolean z, boolean z2) {
        this.isCheckPressed = z;
        this.isPressed = z2;
        updateRadialProgressBackground();
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectionBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectionBackground());
        }
        invalidate();
    }

    public void setInvalidatesParent(boolean z) {
        this.invalidatesParent = z;
    }

    public void invalidate() {
        super.invalidate();
        if (this.invalidatesParent && getParent() != null) {
            View view = (View) getParent();
            if (view.getParent() != null) {
                ((View) view.getParent()).invalidate();
            }
        }
    }

    public void setHighlightedAnimated() {
        this.isHighlightedAnimated = true;
        this.highlightProgress = 1000;
        this.lastHighlightProgressTime = System.currentTimeMillis();
        invalidate();
        if (getParent() != null) {
            ((View) getParent()).invalidate();
        }
    }

    public boolean isHighlighted() {
        return this.isHighlighted;
    }

    public void setHighlighted(boolean z) {
        if (this.isHighlighted != z) {
            this.isHighlighted = z;
            if (this.isHighlighted) {
                this.isHighlightedAnimated = false;
                this.highlightProgress = 0;
            } else {
                this.lastHighlightProgressTime = System.currentTimeMillis();
                this.isHighlightedAnimated = true;
                this.highlightProgress = 300;
            }
            updateRadialProgressBackground();
            if (this.useSeekBarWaweform) {
                this.seekBarWaveform.setSelected(isDrawSelectionBackground());
            } else {
                this.seekBar.setSelected(isDrawSelectionBackground());
            }
            invalidate();
            if (getParent() != null) {
                ((View) getParent()).invalidate();
            }
        }
    }

    public void setPressed(boolean z) {
        super.setPressed(z);
        updateRadialProgressBackground();
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectionBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectionBackground());
        }
        invalidate();
    }

    private void updateRadialProgressBackground() {
        if (!this.drawRadialCheckBackground) {
            boolean z = true;
            Object obj = ((this.isHighlighted || this.isPressed || isPressed()) && !(this.drawPhotoImage && this.photoImage.hasBitmapImage())) ? 1 : null;
            RadialProgress2 radialProgress2 = this.radialProgress;
            boolean z2 = (obj == null && this.buttonPressed == 0) ? false : true;
            radialProgress2.setPressed(z2, false);
            if (this.hasMiniProgress != 0) {
                radialProgress2 = this.radialProgress;
                z2 = (obj == null && this.miniButtonPressed == 0) ? false : true;
                radialProgress2.setPressed(z2, true);
            }
            radialProgress2 = this.videoRadialProgress;
            if (obj == null && this.videoButtonPressed == 0) {
                z = false;
            }
            radialProgress2.setPressed(z, false);
        }
    }

    public void onSeekBarDrag(float f) {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null) {
            messageObject.audioProgress = f;
            MediaController.getInstance().seekToProgress(this.currentMessageObject, f);
        }
    }

    private void updateWaveform() {
        if (this.currentMessageObject != null && this.documentAttachType == 3) {
            boolean z = false;
            for (int i = 0; i < this.documentAttach.attributes.size(); i++) {
                DocumentAttribute documentAttribute = (DocumentAttribute) this.documentAttach.attributes.get(i);
                if (documentAttribute instanceof TL_documentAttributeAudio) {
                    byte[] bArr = documentAttribute.waveform;
                    if (bArr == null || bArr.length == 0) {
                        MediaController.getInstance().generateWaveform(this.currentMessageObject);
                    }
                    if (documentAttribute.waveform != null) {
                        z = true;
                    }
                    this.useSeekBarWaweform = z;
                    this.seekBarWaveform.setWaveform(documentAttribute.waveform);
                    return;
                }
            }
        }
    }

    private int createDocumentLayout(int i, MessageObject messageObject) {
        int i2 = i;
        MessageObject messageObject2 = messageObject;
        if (messageObject2.type == 0) {
            this.documentAttach = messageObject2.messageOwner.media.webpage.document;
        } else {
            this.documentAttach = messageObject.getDocument();
        }
        Document document = this.documentAttach;
        int i3 = 0;
        if (document == null) {
            return 0;
        }
        int i4;
        if (MessageObject.isVoiceDocument(document)) {
            this.documentAttachType = 3;
            for (i4 = 0; i4 < this.documentAttach.attributes.size(); i4++) {
                DocumentAttribute documentAttribute = (DocumentAttribute) this.documentAttach.attributes.get(i4);
                if (documentAttribute instanceof TL_documentAttributeAudio) {
                    i4 = documentAttribute.duration;
                    break;
                }
            }
            i4 = 0;
            this.widthBeforeNewTimeLine = (i2 - AndroidUtilities.dp(94.0f)) - ((int) Math.ceil((double) Theme.chat_audioTimePaint.measureText("00:00")));
            this.availableTimeWidth = i2 - AndroidUtilities.dp(18.0f);
            measureTime(messageObject2);
            int dp = AndroidUtilities.dp(174.0f) + this.timeWidth;
            if (!this.hasLinkPreview) {
                this.backgroundWidth = Math.min(i2, dp + (i4 * AndroidUtilities.dp(10.0f)));
            }
            this.seekBarWaveform.setMessageObject(messageObject2);
            return 0;
        } else if (MessageObject.isMusicDocument(this.documentAttach)) {
            this.documentAttachType = 5;
            i2 -= AndroidUtilities.dp(86.0f);
            if (i2 < 0) {
                i2 = AndroidUtilities.dp(100.0f);
            }
            int i5 = i2;
            this.songLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicTitle().replace(10, ' '), Theme.chat_audioTitlePaint, (float) (i5 - AndroidUtilities.dp(12.0f)), TruncateAt.END), Theme.chat_audioTitlePaint, i5, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.songLayout.getLineCount() > 0) {
                this.songX = -((int) Math.ceil((double) this.songLayout.getLineLeft(0)));
            }
            this.performerLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicAuthor().replace(10, ' '), Theme.chat_audioPerformerPaint, (float) i5, TruncateAt.END), Theme.chat_audioPerformerPaint, i5, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.performerLayout.getLineCount() > 0) {
                this.performerX = -((int) Math.ceil((double) this.performerLayout.getLineLeft(0)));
            }
            for (i2 = 0; i2 < this.documentAttach.attributes.size(); i2++) {
                DocumentAttribute documentAttribute2 = (DocumentAttribute) this.documentAttach.attributes.get(i2);
                if (documentAttribute2 instanceof TL_documentAttributeAudio) {
                    i3 = documentAttribute2.duration;
                    break;
                }
            }
            i2 = (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(AndroidUtilities.formatShortDuration(i3, i3)));
            this.widthBeforeNewTimeLine = (this.backgroundWidth - AndroidUtilities.dp(86.0f)) - i2;
            this.availableTimeWidth = this.backgroundWidth - AndroidUtilities.dp(28.0f);
            return i2;
        } else {
            String str = "%s";
            String format;
            if (MessageObject.isVideoDocument(this.documentAttach)) {
                this.documentAttachType = 4;
                if (!messageObject.needDrawBluredPreview()) {
                    updatePlayingMessageProgress();
                    format = String.format(str, new Object[]{AndroidUtilities.formatFileSize((long) this.documentAttach.size)});
                    this.docTitleWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(format));
                    this.docTitleLayout = new StaticLayout(format, Theme.chat_infoPaint, this.docTitleWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                return 0;
            } else if (MessageObject.isGifDocument(this.documentAttach)) {
                this.documentAttachType = 2;
                if (!messageObject.needDrawBluredPreview()) {
                    String string = LocaleController.getString("AttachGif", NUM);
                    this.infoWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(string));
                    this.infoLayout = new StaticLayout(string, Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    format = String.format(str, new Object[]{AndroidUtilities.formatFileSize((long) this.documentAttach.size)});
                    this.docTitleWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(format));
                    this.docTitleLayout = new StaticLayout(format, Theme.chat_infoPaint, this.docTitleWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                return 0;
            } else {
                int i6;
                String str2 = this.documentAttach.mime_type;
                boolean z = (str2 != null && str2.toLowerCase().startsWith("image/")) || MessageObject.isDocumentHasThumb(this.documentAttach);
                this.drawPhotoImage = z;
                if (!this.drawPhotoImage) {
                    i2 += AndroidUtilities.dp(30.0f);
                }
                this.documentAttachType = 1;
                str2 = FileLoader.getDocumentFileName(this.documentAttach);
                if (str2 == null || str2.length() == 0) {
                    str2 = LocaleController.getString("AttachDocument", NUM);
                }
                this.docTitleLayout = StaticLayoutEx.createStaticLayout(str2, Theme.chat_docNamePaint, i2, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.MIDDLE, i2, 2, false);
                this.docTitleOffsetX = Integer.MIN_VALUE;
                StaticLayout staticLayout = this.docTitleLayout;
                if (staticLayout == null || staticLayout.getLineCount() <= 0) {
                    this.docTitleOffsetX = 0;
                    i6 = i2;
                } else {
                    i4 = 0;
                    while (i3 < this.docTitleLayout.getLineCount()) {
                        i4 = Math.max(i4, (int) Math.ceil((double) this.docTitleLayout.getLineWidth(i3)));
                        this.docTitleOffsetX = Math.max(this.docTitleOffsetX, (int) Math.ceil((double) (-this.docTitleLayout.getLineLeft(i3))));
                        i3++;
                    }
                    i6 = Math.min(i2, i4);
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(AndroidUtilities.formatFileSize((long) this.documentAttach.size));
                stringBuilder.append(" ");
                stringBuilder.append(FileLoader.getDocumentExtension(this.documentAttach));
                str2 = stringBuilder.toString();
                this.infoWidth = Math.min(i2 - AndroidUtilities.dp(30.0f), (int) Math.ceil((double) Theme.chat_infoPaint.measureText(str2)));
                CharSequence ellipsize = TextUtils.ellipsize(str2, Theme.chat_infoPaint, (float) this.infoWidth, TruncateAt.END);
                try {
                    if (this.infoWidth < 0) {
                        this.infoWidth = AndroidUtilities.dp(10.0f);
                    }
                    this.infoLayout = new StaticLayout(ellipsize, Theme.chat_infoPaint, this.infoWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (this.drawPhotoImage) {
                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject2.photoThumbs, 320);
                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject2.photoThumbs, 40);
                    if ((DownloadController.getInstance(this.currentAccount).getAutodownloadMask() & 1) == 0) {
                        this.currentPhotoObject = null;
                    }
                    PhotoSize photoSize = this.currentPhotoObject;
                    if (photoSize == null || photoSize == this.currentPhotoObjectThumb) {
                        this.currentPhotoObject = null;
                        this.photoImage.setNeedsQualityThumb(true);
                        this.photoImage.setShouldGenerateQualityThumb(true);
                    }
                    this.currentPhotoFilter = "86_86_b";
                    this.photoImage.setImage(ImageLocation.getForObject(this.currentPhotoObject, messageObject2.photoThumbsObject), "86_86", ImageLocation.getForObject(this.currentPhotoObjectThumb, messageObject2.photoThumbsObject), this.currentPhotoFilter, 0, null, messageObject, 1);
                }
                return i6;
            }
        }
    }

    private void calcBackgroundWidth(int i, int i2, int i3) {
        if (!(this.hasLinkPreview || this.hasOldCaptionPreview || this.hasGamePreview || this.hasInvoicePreview)) {
            MessageObject messageObject = this.currentMessageObject;
            int i4 = messageObject.lastLineWidth;
            if (i - i4 >= i2 && !messageObject.hasRtl) {
                i = i3 - i4;
                if (i < 0 || i > i2) {
                    this.backgroundWidth = Math.max(i3, this.currentMessageObject.lastLineWidth + i2) + AndroidUtilities.dp(31.0f);
                    return;
                } else {
                    this.backgroundWidth = ((i3 + i2) - i) + AndroidUtilities.dp(31.0f);
                    return;
                }
            }
        }
        this.totalHeight += AndroidUtilities.dp(14.0f);
        this.hasNewLineForTime = true;
        this.backgroundWidth = Math.max(i3, this.currentMessageObject.lastLineWidth) + AndroidUtilities.dp(31.0f);
        this.backgroundWidth = Math.max(this.backgroundWidth, (this.currentMessageObject.isOutOwner() ? this.timeWidth + AndroidUtilities.dp(17.0f) : this.timeWidth) + AndroidUtilities.dp(31.0f));
    }

    public void setHighlightedText(String str) {
        MessageObject messageObject = this.messageObjectToSet;
        if (messageObject == null) {
            messageObject = this.currentMessageObject;
        }
        if (messageObject == null || messageObject.messageOwner.message == null || TextUtils.isEmpty(str)) {
            if (!this.urlPathSelection.isEmpty()) {
                this.linkSelectionBlockNum = -1;
                resetUrlPaths(true);
                invalidate();
            }
            return;
        }
        String toLowerCase = str.toLowerCase();
        String toLowerCase2 = messageObject.messageOwner.message.toLowerCase();
        String str2 = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~\n";
        int length = toLowerCase2.length();
        int i = 0;
        int i2 = -1;
        int i3 = -1;
        while (i < length) {
            int min = Math.min(toLowerCase.length(), length - i);
            int i4 = 0;
            int i5 = 0;
            while (i4 < min) {
                Object obj = toLowerCase2.charAt(i + i4) == toLowerCase.charAt(i4) ? 1 : null;
                if (obj != null) {
                    if (i5 != 0 || i == 0 || str2.indexOf(toLowerCase2.charAt(i - 1)) >= 0) {
                        i5++;
                    } else {
                        obj = null;
                    }
                }
                if (obj == null || i4 == min - 1) {
                    if (i5 > 0 && i5 > i3) {
                        i2 = i;
                        i3 = i5;
                    }
                    i++;
                } else {
                    i4++;
                }
            }
            i++;
        }
        if (i2 == -1) {
            if (!this.urlPathSelection.isEmpty()) {
                this.linkSelectionBlockNum = -1;
                resetUrlPaths(true);
                invalidate();
            }
            return;
        }
        int i6 = i2 + i3;
        int length2 = toLowerCase2.length();
        while (i6 < length2 && str2.indexOf(toLowerCase2.charAt(i6)) < 0) {
            i3++;
            i6++;
        }
        i6 = i2 + i3;
        if (this.captionLayout != null && !TextUtils.isEmpty(messageObject.caption)) {
            resetUrlPaths(true);
            try {
                LinkPath obtainNewUrlPath = obtainNewUrlPath(true);
                obtainNewUrlPath.setCurrentLayout(this.captionLayout, i2, 0.0f);
                this.captionLayout.getSelectionPath(i2, i6, obtainNewUrlPath);
            } catch (Exception e) {
                FileLog.e(e);
            }
            invalidate();
        } else if (messageObject.textLayoutBlocks != null) {
            length2 = 0;
            while (length2 < messageObject.textLayoutBlocks.size()) {
                TextLayoutBlock textLayoutBlock = (TextLayoutBlock) messageObject.textLayoutBlocks.get(length2);
                length = textLayoutBlock.charactersOffset;
                if (i2 < length || i2 >= length + textLayoutBlock.textLayout.getText().length()) {
                    length2++;
                } else {
                    this.linkSelectionBlockNum = length2;
                    resetUrlPaths(true);
                    try {
                        LinkPath obtainNewUrlPath2 = obtainNewUrlPath(true);
                        obtainNewUrlPath2.setCurrentLayout(textLayoutBlock.textLayout, i2, 0.0f);
                        textLayoutBlock.textLayout.getSelectionPath(i2, i6 - textLayoutBlock.charactersOffset, obtainNewUrlPath2);
                        if (i6 >= textLayoutBlock.charactersOffset + i3) {
                            for (length2++; length2 < messageObject.textLayoutBlocks.size(); length2++) {
                                TextLayoutBlock textLayoutBlock2 = (TextLayoutBlock) messageObject.textLayoutBlocks.get(length2);
                                length = textLayoutBlock2.textLayout.getText().length();
                                LinkPath obtainNewUrlPath3 = obtainNewUrlPath(true);
                                obtainNewUrlPath3.setCurrentLayout(textLayoutBlock2.textLayout, 0, (float) textLayoutBlock2.height);
                                textLayoutBlock2.textLayout.getSelectionPath(0, i6 - textLayoutBlock2.charactersOffset, obtainNewUrlPath3);
                                if (i6 < (textLayoutBlock.charactersOffset + length) - 1) {
                                    break;
                                }
                            }
                        }
                    } catch (Exception e2) {
                        FileLog.e(e2);
                    }
                    invalidate();
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.selectorDrawable;
    }

    private boolean isCurrentLocationTimeExpired(MessageObject messageObject) {
        boolean z = true;
        if (this.currentMessageObject.messageOwner.media.period % 60 == 0) {
            if (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - messageObject.messageOwner.date) <= messageObject.messageOwner.media.period) {
                z = false;
            }
            return z;
        }
        if (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - messageObject.messageOwner.date) <= messageObject.messageOwner.media.period - 5) {
            z = false;
        }
        return z;
    }

    private void checkLocationExpired() {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null) {
            boolean isCurrentLocationTimeExpired = isCurrentLocationTimeExpired(messageObject);
            if (isCurrentLocationTimeExpired != this.locationExpired) {
                this.locationExpired = isCurrentLocationTimeExpired;
                if (this.locationExpired) {
                    messageObject = this.currentMessageObject;
                    this.currentMessageObject = null;
                    setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
                } else {
                    AndroidUtilities.runOnUIThread(this.invalidateRunnable, 1000);
                    this.scheduledInvalidate = true;
                    int dp = this.backgroundWidth - AndroidUtilities.dp(91.0f);
                    this.docTitleLayout = new StaticLayout(TextUtils.ellipsize(LocaleController.getString("AttachLiveLocation", NUM), Theme.chat_locationTitlePaint, (float) dp, TruncateAt.END), Theme.chat_locationTitlePaint, dp, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
            }
        }
    }

    public void setMessageObject(MessageObject messageObject, GroupedMessages groupedMessages, boolean z, boolean z2) {
        if (this.attachedToWindow) {
            setMessageContent(messageObject, groupedMessages, z, z2);
            return;
        }
        this.messageObjectToSet = messageObject;
        this.groupedMessagesToSet = groupedMessages;
        this.bottomNearToSet = z;
        this.topNearToSet = z2;
    }

    private int getAdditionalWidthForPosition(GroupedMessagePosition groupedMessagePosition) {
        int i = 0;
        if (groupedMessagePosition == null) {
            return 0;
        }
        if ((groupedMessagePosition.flags & 2) == 0) {
            i = 0 + AndroidUtilities.dp(4.0f);
        }
        return (groupedMessagePosition.flags & 1) == 0 ? i + AndroidUtilities.dp(4.0f) : i;
    }

    private void createSelectorDrawable() {
        if (VERSION.SDK_INT >= 21) {
            Drawable drawable = this.selectorDrawable;
            String str = "chat_outPreviewInstantText";
            String str2 = "chat_inPreviewInstantText";
            if (drawable == null) {
                final Paint paint = new Paint(1);
                paint.setColor(-1);
                AnonymousClass2 anonymousClass2 = new Drawable() {
                    RectF rect = new RectF();

                    public int getOpacity() {
                        return -2;
                    }

                    public void setAlpha(int i) {
                    }

                    public void setColorFilter(ColorFilter colorFilter) {
                    }

                    public void draw(Canvas canvas) {
                        Rect bounds = getBounds();
                        this.rect.set((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom);
                        RectF rectF = this.rect;
                        float f = 0.0f;
                        float dp = ChatMessageCell.this.selectorDrawableMaskType == 0 ? (float) AndroidUtilities.dp(6.0f) : 0.0f;
                        if (ChatMessageCell.this.selectorDrawableMaskType == 0) {
                            f = (float) AndroidUtilities.dp(6.0f);
                        }
                        canvas.drawRoundRect(rectF, dp, f, paint);
                    }
                };
                int[][] iArr = new int[][]{StateSet.WILD_CARD};
                int[] iArr2 = new int[1];
                if (!this.currentMessageObject.isOutOwner()) {
                    str = str2;
                }
                iArr2[0] = NUM & Theme.getColor(str);
                this.selectorDrawable = new RippleDrawable(new ColorStateList(iArr, iArr2), null, anonymousClass2);
                this.selectorDrawable.setCallback(this);
            } else {
                if (!this.currentMessageObject.isOutOwner()) {
                    str = str2;
                }
                Theme.setSelectorDrawableColor(drawable, NUM & Theme.getColor(str), true);
            }
            this.selectorDrawable.setVisible(true, false);
        }
    }

    private void createInstantViewButton() {
        if (VERSION.SDK_INT >= 21 && this.drawInstantView) {
            createSelectorDrawable();
        }
        if (this.drawInstantView && this.instantViewLayout == null) {
            CharSequence string;
            this.instantWidth = AndroidUtilities.dp(33.0f);
            int i = this.drawInstantViewType;
            if (i == 1) {
                string = LocaleController.getString("OpenChannel", NUM);
            } else if (i == 2) {
                string = LocaleController.getString("OpenGroup", NUM);
            } else if (i == 3) {
                string = LocaleController.getString("OpenMessage", NUM);
            } else if (i == 5) {
                string = LocaleController.getString("ViewContact", NUM);
            } else if (i == 6) {
                string = LocaleController.getString("OpenBackground", NUM);
            } else if (i == 7) {
                string = LocaleController.getString("OpenTheme", NUM);
            } else {
                string = LocaleController.getString("InstantView", NUM);
            }
            int dp = this.backgroundWidth - AndroidUtilities.dp(75.0f);
            this.instantViewLayout = new StaticLayout(TextUtils.ellipsize(string, Theme.chat_instantViewPaint, (float) dp, TruncateAt.END), Theme.chat_instantViewPaint, dp + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            this.instantWidth = this.backgroundWidth - AndroidUtilities.dp(34.0f);
            this.totalHeight += AndroidUtilities.dp(46.0f);
            if (this.currentMessageObject.type == 12) {
                this.totalHeight += AndroidUtilities.dp(14.0f);
            }
            StaticLayout staticLayout = this.instantViewLayout;
            if (staticLayout != null && staticLayout.getLineCount() > 0) {
                double d = (double) this.instantWidth;
                double ceil = Math.ceil((double) this.instantViewLayout.getLineWidth(0));
                Double.isNaN(d);
                this.instantTextX = (((int) (d - ceil)) / 2) + (this.drawInstantViewType == 0 ? AndroidUtilities.dp(8.0f) : 0);
                this.instantTextLeftX = (int) this.instantViewLayout.getLineLeft(0);
                this.instantTextX += -this.instantTextLeftX;
            }
        }
    }

    public void requestLayout() {
        if (!this.inLayout) {
            super.requestLayout();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null && (messageObject.checkLayout() || this.lastHeight != AndroidUtilities.displaySize.y)) {
            this.inLayout = true;
            messageObject = this.currentMessageObject;
            this.currentMessageObject = null;
            setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
            this.inLayout = false;
        }
        setMeasuredDimension(MeasureSpec.getSize(i), this.totalHeight + this.keyboardHeight);
    }

    public void forceResetMessageObject() {
        MessageObject messageObject = this.messageObjectToSet;
        if (messageObject == null) {
            messageObject = this.currentMessageObject;
        }
        this.currentMessageObject = null;
        setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
    }

    private int getGroupPhotosWidth() {
        if (AndroidUtilities.isInMultiwindow || !AndroidUtilities.isTablet() || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
            return AndroidUtilities.displaySize.x;
        }
        int i = (AndroidUtilities.displaySize.x / 100) * 35;
        if (i < AndroidUtilities.dp(320.0f)) {
            i = AndroidUtilities.dp(320.0f);
        }
        return AndroidUtilities.displaySize.x - i;
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0461  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0489  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0461  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0489  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0461  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0489  */
    @android.annotation.SuppressLint({"DrawAllocation"})
    public void onLayout(boolean r11, int r12, int r13, int r14, int r15) {
        /*
        r10 = this;
        r12 = r10.currentMessageObject;
        if (r12 != 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r12 = 1;
        r13 = 0;
        r14 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r15 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        if (r11 != 0) goto L_0x0011;
    L_0x000d:
        r11 = r10.wasLayout;
        if (r11 != 0) goto L_0x0109;
    L_0x0011:
        r11 = r10.getMeasuredWidth();
        r10.layoutWidth = r11;
        r11 = r10.getMeasuredHeight();
        r0 = r10.substractBackgroundHeight;
        r11 = r11 - r0;
        r10.layoutHeight = r11;
        r11 = r10.timeTextWidth;
        if (r11 >= 0) goto L_0x002a;
    L_0x0024:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r10.timeTextWidth = r11;
    L_0x002a:
        r11 = new android.text.StaticLayout;
        r1 = r10.currentTimeString;
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r0 = r10.timeTextWidth;
        r3 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r0;
        r4 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r5 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r6 = 0;
        r7 = 0;
        r0 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r10.timeLayout = r11;
        r11 = r10.mediaBackground;
        r0 = NUM; // 0x42280000 float:42.0 double:5.483722033E-315;
        if (r11 != 0) goto L_0x007c;
    L_0x004b:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 != 0) goto L_0x006d;
    L_0x0053:
        r11 = r10.backgroundWidth;
        r1 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r11 = r11 - r1;
        r1 = r10.timeWidth;
        r11 = r11 - r1;
        r1 = r10.isAvatarVisible;
        if (r1 == 0) goto L_0x0068;
    L_0x0063:
        r1 = org.telegram.messenger.AndroidUtilities.dp(r14);
        goto L_0x0069;
    L_0x0068:
        r1 = 0;
    L_0x0069:
        r11 = r11 + r1;
        r10.timeX = r11;
        goto L_0x00c8;
    L_0x006d:
        r11 = r10.layoutWidth;
        r1 = r10.timeWidth;
        r11 = r11 - r1;
        r1 = NUM; // 0x421a0000 float:38.5 double:5.47918896E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r11 = r11 - r1;
        r10.timeX = r11;
        goto L_0x00c8;
    L_0x007c:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 != 0) goto L_0x00bc;
    L_0x0084:
        r11 = r10.backgroundWidth;
        r1 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r11 = r11 - r1;
        r1 = r10.timeWidth;
        r11 = r11 - r1;
        r1 = r10.isAvatarVisible;
        if (r1 == 0) goto L_0x0099;
    L_0x0094:
        r1 = org.telegram.messenger.AndroidUtilities.dp(r14);
        goto L_0x009a;
    L_0x0099:
        r1 = 0;
    L_0x009a:
        r11 = r11 + r1;
        r10.timeX = r11;
        r11 = r10.currentPosition;
        if (r11 == 0) goto L_0x00c8;
    L_0x00a1:
        r11 = r11.leftSpanOffset;
        if (r11 == 0) goto L_0x00c8;
    L_0x00a5:
        r1 = r10.timeX;
        r11 = (float) r11;
        r2 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r11 = r11 / r2;
        r2 = r10.getGroupPhotosWidth();
        r2 = (float) r2;
        r11 = r11 * r2;
        r2 = (double) r11;
        r2 = java.lang.Math.ceil(r2);
        r11 = (int) r2;
        r1 = r1 + r11;
        r10.timeX = r1;
        goto L_0x00c8;
    L_0x00bc:
        r11 = r10.layoutWidth;
        r1 = r10.timeWidth;
        r11 = r11 - r1;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r11 = r11 - r1;
        r10.timeX = r11;
    L_0x00c8:
        r11 = r10.currentMessageObject;
        r11 = r11.messageOwner;
        r11 = r11.flags;
        r11 = r11 & 1024;
        if (r11 == 0) goto L_0x00e7;
    L_0x00d2:
        r11 = new android.text.StaticLayout;
        r2 = r10.currentViewsString;
        r3 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r4 = r10.viewsTextWidth;
        r5 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r7 = 0;
        r8 = 0;
        r1 = r11;
        r1.<init>(r2, r3, r4, r5, r6, r7, r8);
        r10.viewsLayout = r11;
        goto L_0x00ea;
    L_0x00e7:
        r11 = 0;
        r10.viewsLayout = r11;
    L_0x00ea:
        r11 = r10.isAvatarVisible;
        if (r11 == 0) goto L_0x0107;
    L_0x00ee:
        r11 = r10.avatarImage;
        r1 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r2 = r10.avatarImage;
        r2 = r2.getImageY();
        r3 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r11.setImageCoords(r1, r2, r3, r0);
    L_0x0107:
        r10.wasLayout = r12;
    L_0x0109:
        r11 = r10.currentMessageObject;
        r11 = r11.type;
        if (r11 != 0) goto L_0x0118;
    L_0x010f:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r0 = r10.namesOffset;
        r11 = r11 + r0;
        r10.textY = r11;
    L_0x0118:
        r11 = r10.currentMessageObject;
        r11 = r11.isRoundVideo();
        if (r11 == 0) goto L_0x0123;
    L_0x0120:
        r10.updatePlayingMessageProgress();
    L_0x0123:
        r11 = r10.documentAttachType;
        r0 = 3;
        r1 = NUM; // 0x42980000 float:76.0 double:5.51998661E-315;
        r2 = NUM; // 0x42860000 float:67.0 double:5.514158374E-315;
        r3 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r4 = 10;
        r5 = NUM; // 0x428e0000 float:71.0 double:5.5167487E-315;
        r6 = NUM; // 0x41b80000 float:23.0 double:5.447457457E-315;
        r7 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r8 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r9 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        if (r11 != r0) goto L_0x0225;
    L_0x013a:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 == 0) goto L_0x0169;
    L_0x0142:
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = NUM; // 0x42640000 float:57.0 double:5.503149485E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r11 = r11 + r12;
        r10.seekBarX = r11;
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r11 = r11 + r12;
        r10.buttonX = r11;
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r11 = r11 + r12;
        r10.timeAudioX = r11;
        goto L_0x01a0;
    L_0x0169:
        r11 = r10.isChat;
        if (r11 == 0) goto L_0x018c;
    L_0x016d:
        r11 = r10.currentMessageObject;
        r11 = r11.needDrawAvatar();
        if (r11 == 0) goto L_0x018c;
    L_0x0175:
        r11 = NUM; // 0x42e40000 float:114.0 double:5.544594715E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.seekBarX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r10.buttonX = r11;
        r11 = NUM; // 0x42var_ float:124.0 double:5.55107053E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.timeAudioX = r11;
        goto L_0x01a0;
    L_0x018c:
        r11 = NUM; // 0x42840000 float:66.0 double:5.51351079E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.seekBarX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r10.buttonX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r10.timeAudioX = r11;
    L_0x01a0:
        r11 = r10.hasLinkPreview;
        if (r11 == 0) goto L_0x01bf;
    L_0x01a4:
        r11 = r10.seekBarX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.seekBarX = r11;
        r11 = r10.buttonX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.buttonX = r11;
        r11 = r10.timeAudioX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.timeAudioX = r11;
    L_0x01bf:
        r11 = r10.seekBarWaveform;
        r12 = r10.backgroundWidth;
        r14 = r10.hasLinkPreview;
        if (r14 == 0) goto L_0x01ca;
    L_0x01c7:
        r14 = 10;
        goto L_0x01cb;
    L_0x01ca:
        r14 = 0;
    L_0x01cb:
        r14 = r14 + 92;
        r14 = (float) r14;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r12 = r12 - r14;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r11.setSize(r12, r14);
        r11 = r10.seekBar;
        r12 = r10.backgroundWidth;
        r14 = r10.hasLinkPreview;
        if (r14 == 0) goto L_0x01e4;
    L_0x01e2:
        r13 = 10;
    L_0x01e4:
        r13 = r13 + 72;
        r13 = (float) r13;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r12 = r12 - r13;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r11.setSize(r12, r13);
        r11 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r12 = r10.namesOffset;
        r11 = r11 + r12;
        r12 = r10.mediaOffsetY;
        r11 = r11 + r12;
        r10.seekBarY = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r12 = r10.namesOffset;
        r11 = r11 + r12;
        r12 = r10.mediaOffsetY;
        r11 = r11 + r12;
        r10.buttonY = r11;
        r11 = r10.radialProgress;
        r12 = r10.buttonX;
        r13 = r10.buttonY;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r14 = r14 + r12;
        r15 = r10.buttonY;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r15 = r15 + r0;
        r11.setProgressRect(r12, r13, r14, r15);
        r10.updatePlayingMessageProgress();
        goto L_0x054c;
    L_0x0225:
        r0 = 5;
        if (r11 != r0) goto L_0x02fa;
    L_0x0228:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 == 0) goto L_0x0257;
    L_0x0230:
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r11 = r11 + r12;
        r10.seekBarX = r11;
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r11 = r11 + r12;
        r10.buttonX = r11;
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r11 = r11 + r12;
        r10.timeAudioX = r11;
        goto L_0x028e;
    L_0x0257:
        r11 = r10.isChat;
        if (r11 == 0) goto L_0x027a;
    L_0x025b:
        r11 = r10.currentMessageObject;
        r11 = r11.needDrawAvatar();
        if (r11 == 0) goto L_0x027a;
    L_0x0263:
        r11 = NUM; // 0x42e20000 float:113.0 double:5.543947133E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.seekBarX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r10.buttonX = r11;
        r11 = NUM; // 0x42var_ float:124.0 double:5.55107053E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.timeAudioX = r11;
        goto L_0x028e;
    L_0x027a:
        r11 = NUM; // 0x42820000 float:65.0 double:5.51286321E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r10.seekBarX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r10.buttonX = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r10.timeAudioX = r11;
    L_0x028e:
        r11 = r10.hasLinkPreview;
        if (r11 == 0) goto L_0x02ad;
    L_0x0292:
        r11 = r10.seekBarX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.seekBarX = r11;
        r11 = r10.buttonX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.buttonX = r11;
        r11 = r10.timeAudioX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.timeAudioX = r11;
    L_0x02ad:
        r11 = r10.seekBar;
        r12 = r10.backgroundWidth;
        r14 = r10.hasLinkPreview;
        if (r14 == 0) goto L_0x02b7;
    L_0x02b5:
        r13 = 10;
    L_0x02b7:
        r13 = r13 + 65;
        r13 = (float) r13;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r12 = r12 - r13;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r11.setSize(r12, r13);
        r11 = NUM; // 0x41e80000 float:29.0 double:5.46299942E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r12 = r10.namesOffset;
        r11 = r11 + r12;
        r12 = r10.mediaOffsetY;
        r11 = r11 + r12;
        r10.seekBarY = r11;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r12 = r10.namesOffset;
        r11 = r11 + r12;
        r12 = r10.mediaOffsetY;
        r11 = r11 + r12;
        r10.buttonY = r11;
        r11 = r10.radialProgress;
        r12 = r10.buttonX;
        r13 = r10.buttonY;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r14 = r14 + r12;
        r15 = r10.buttonY;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r15 = r15 + r0;
        r11.setProgressRect(r12, r13, r14, r15);
        r10.updatePlayingMessageProgress();
        goto L_0x054c;
    L_0x02fa:
        if (r11 != r12) goto L_0x037d;
    L_0x02fc:
        r11 = r10.drawPhotoImage;
        if (r11 != 0) goto L_0x037d;
    L_0x0300:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 == 0) goto L_0x0315;
    L_0x0308:
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r11 = r11 + r12;
        r10.buttonX = r11;
        goto L_0x032e;
    L_0x0315:
        r11 = r10.isChat;
        if (r11 == 0) goto L_0x0328;
    L_0x0319:
        r11 = r10.currentMessageObject;
        r11 = r11.needDrawAvatar();
        if (r11 == 0) goto L_0x0328;
    L_0x0321:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r10.buttonX = r11;
        goto L_0x032e;
    L_0x0328:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r10.buttonX = r11;
    L_0x032e:
        r11 = r10.hasLinkPreview;
        if (r11 == 0) goto L_0x033b;
    L_0x0332:
        r11 = r10.buttonX;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 + r12;
        r10.buttonX = r11;
    L_0x033b:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r12 = r10.namesOffset;
        r11 = r11 + r12;
        r12 = r10.mediaOffsetY;
        r11 = r11 + r12;
        r10.buttonY = r11;
        r11 = r10.radialProgress;
        r12 = r10.buttonX;
        r13 = r10.buttonY;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r14 = r14 + r12;
        r0 = r10.buttonY;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r0 = r0 + r1;
        r11.setProgressRect(r12, r13, r14, r0);
        r11 = r10.photoImage;
        r12 = r10.buttonX;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r12 = r12 - r13;
        r13 = r10.buttonY;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r13 = r13 - r14;
        r14 = r10.photoImage;
        r14 = r14.getImageWidth();
        r15 = r10.photoImage;
        r15 = r15.getImageHeight();
        r11.setImageCoords(r12, r13, r14, r15);
        goto L_0x054c;
    L_0x037d:
        r11 = r10.currentMessageObject;
        r13 = r11.type;
        r0 = 12;
        if (r13 != r0) goto L_0x03c3;
    L_0x0385:
        r11 = r11.isOutOwner();
        if (r11 == 0) goto L_0x0396;
    L_0x038b:
        r11 = r10.layoutWidth;
        r12 = r10.backgroundWidth;
        r11 = r11 - r12;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r11 = r11 + r12;
        goto L_0x03ad;
    L_0x0396:
        r11 = r10.isChat;
        if (r11 == 0) goto L_0x03a9;
    L_0x039a:
        r11 = r10.currentMessageObject;
        r11 = r11.needDrawAvatar();
        if (r11 == 0) goto L_0x03a9;
    L_0x03a2:
        r11 = NUM; // 0x42900000 float:72.0 double:5.517396283E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        goto L_0x03ad;
    L_0x03a9:
        r11 = org.telegram.messenger.AndroidUtilities.dp(r6);
    L_0x03ad:
        r12 = r10.photoImage;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r14 = r10.namesOffset;
        r13 = r13 + r14;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r15 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r12.setImageCoords(r11, r13, r14, r15);
        goto L_0x054c;
    L_0x03c3:
        if (r13 != 0) goto L_0x0413;
    L_0x03c5:
        r11 = r10.hasLinkPreview;
        if (r11 != 0) goto L_0x03d1;
    L_0x03c9:
        r11 = r10.hasGamePreview;
        if (r11 != 0) goto L_0x03d1;
    L_0x03cd:
        r11 = r10.hasInvoicePreview;
        if (r11 == 0) goto L_0x0413;
    L_0x03d1:
        r11 = r10.hasGamePreview;
        if (r11 == 0) goto L_0x03dd;
    L_0x03d5:
        r11 = r10.unmovedTextX;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 - r13;
        goto L_0x03f3;
    L_0x03dd:
        r11 = r10.hasInvoicePreview;
        if (r11 == 0) goto L_0x03ea;
    L_0x03e1:
        r11 = r10.unmovedTextX;
        r13 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        goto L_0x03f2;
    L_0x03ea:
        r11 = r10.unmovedTextX;
        r13 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
    L_0x03f2:
        r11 = r11 + r13;
    L_0x03f3:
        r13 = r10.isSmallImage;
        if (r13 == 0) goto L_0x0401;
    L_0x03f7:
        r13 = r10.backgroundWidth;
        r11 = r11 + r13;
        r13 = NUM; // 0x42a20000 float:81.0 double:5.52322452E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        goto L_0x042a;
    L_0x0401:
        r13 = r10.hasInvoicePreview;
        if (r13 == 0) goto L_0x040e;
    L_0x0405:
        r13 = NUM; // 0x40CLASSNAMEa float:6.3 double:5.370265717E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r13 = -r13;
        goto L_0x0437;
    L_0x040e:
        r13 = org.telegram.messenger.AndroidUtilities.dp(r15);
        goto L_0x0437;
    L_0x0413:
        r11 = r10.currentMessageObject;
        r11 = r11.isOutOwner();
        if (r11 == 0) goto L_0x0439;
    L_0x041b:
        r11 = r10.mediaBackground;
        if (r11 == 0) goto L_0x042c;
    L_0x041f:
        r11 = r10.layoutWidth;
        r13 = r10.backgroundWidth;
        r11 = r11 - r13;
        r13 = NUM; // 0x40400000 float:3.0 double:5.325712093E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
    L_0x042a:
        r11 = r11 - r13;
        goto L_0x045b;
    L_0x042c:
        r11 = r10.layoutWidth;
        r13 = r10.backgroundWidth;
        r11 = r11 - r13;
        r13 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
    L_0x0437:
        r11 = r11 + r13;
        goto L_0x045b;
    L_0x0439:
        r11 = r10.isChat;
        if (r11 == 0) goto L_0x0448;
    L_0x043d:
        r11 = r10.isAvatarVisible;
        if (r11 == 0) goto L_0x0448;
    L_0x0441:
        r11 = NUM; // 0x427CLASSNAME float:63.0 double:5.510920465E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        goto L_0x044e;
    L_0x0448:
        r11 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
    L_0x044e:
        r13 = r10.currentPosition;
        if (r13 == 0) goto L_0x045b;
    L_0x0452:
        r13 = r13.edge;
        if (r13 != 0) goto L_0x045b;
    L_0x0456:
        r13 = org.telegram.messenger.AndroidUtilities.dp(r15);
        goto L_0x042a;
    L_0x045b:
        r13 = r10.currentPosition;
        r15 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r13 == 0) goto L_0x0483;
    L_0x0461:
        r13 = r13.flags;
        r12 = r12 & r13;
        if (r12 != 0) goto L_0x046b;
    L_0x0466:
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 - r12;
    L_0x046b:
        r12 = r10.currentPosition;
        r12 = r12.leftSpanOffset;
        if (r12 == 0) goto L_0x0483;
    L_0x0471:
        r12 = (float) r12;
        r13 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r12 = r12 / r13;
        r13 = r10.getGroupPhotosWidth();
        r13 = (float) r13;
        r12 = r12 * r13;
        r12 = (double) r12;
        r12 = java.lang.Math.ceil(r12);
        r12 = (int) r12;
        r11 = r11 + r12;
    L_0x0483:
        r12 = r10.currentMessageObject;
        r12 = r12.type;
        if (r12 == 0) goto L_0x048e;
    L_0x0489:
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r11 = r11 - r12;
    L_0x048e:
        r12 = r10.photoImage;
        r13 = r12.getImageY();
        r0 = r10.photoImage;
        r0 = r0.getImageWidth();
        r1 = r10.photoImage;
        r1 = r1.getImageHeight();
        r12.setImageCoords(r11, r13, r0, r1);
        r11 = (float) r11;
        r12 = r10.photoImage;
        r12 = r12.getImageWidth();
        r13 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r12 = r12 - r13;
        r12 = (float) r12;
        r12 = r12 / r15;
        r11 = r11 + r12;
        r11 = (int) r11;
        r10.buttonX = r11;
        r11 = r10.photoImage;
        r11 = r11.getImageY();
        r12 = r10.photoImage;
        r12 = r12.getImageHeight();
        r13 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r12 = r12 - r13;
        r12 = r12 / 2;
        r11 = r11 + r12;
        r10.buttonY = r11;
        r11 = r10.radialProgress;
        r12 = r10.buttonX;
        r13 = r10.buttonY;
        r15 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r15 = r15 + r12;
        r0 = r10.buttonY;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r0 = r0 + r14;
        r11.setProgressRect(r12, r13, r15, r0);
        r11 = r10.deleteProgressRect;
        r12 = r10.buttonX;
        r13 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r12 = r12 + r13;
        r12 = (float) r12;
        r13 = r10.buttonY;
        r14 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r13 = r13 + r14;
        r13 = (float) r13;
        r14 = r10.buttonX;
        r15 = NUM; // 0x422CLASSNAME float:43.0 double:5.485017196E-315;
        r15 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r14 = r14 + r15;
        r14 = (float) r14;
        r15 = r10.buttonY;
        r0 = NUM; // 0x422CLASSNAME float:43.0 double:5.485017196E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r15 = r15 + r0;
        r15 = (float) r15;
        r11.set(r12, r13, r14, r15);
        r11 = r10.documentAttachType;
        r12 = 4;
        if (r11 == r12) goto L_0x0515;
    L_0x0512:
        r12 = 2;
        if (r11 != r12) goto L_0x054c;
    L_0x0515:
        r11 = r10.photoImage;
        r11 = r11.getImageX();
        r12 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r11 = r11 + r12;
        r10.videoButtonX = r11;
        r11 = r10.photoImage;
        r11 = r11.getImageY();
        r12 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r11 = r11 + r12;
        r10.videoButtonY = r11;
        r11 = r10.videoRadialProgress;
        r12 = r10.videoButtonX;
        r13 = r10.videoButtonY;
        r14 = NUM; // 0x41CLASSNAME float:24.0 double:5.450047783E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r14 = r14 + r12;
        r15 = r10.videoButtonY;
        r0 = NUM; // 0x41CLASSNAME float:24.0 double:5.450047783E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r15 = r15 + r0;
        r11.setProgressRect(r12, r13, r14, r15);
    L_0x054c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.onLayout(boolean, int, int, int, int):void");
    }

    public boolean needDelayRoundProgressDraw() {
        int i = this.documentAttachType;
        return (i == 7 || i == 4) && this.currentMessageObject.type != 5 && MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
    }

    public void drawRoundProgress(Canvas canvas) {
        this.rect.set(((float) this.photoImage.getImageX()) + AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageY()) + AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageX2()) - AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageY2()) - AndroidUtilities.dpf2(1.5f));
        canvas.drawArc(this.rect, -90.0f, this.currentMessageObject.audioProgress * 360.0f, false, Theme.chat_radialProgressPaint);
    }

    private void updatePollAnimations() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - this.voteLastUpdateTime;
        if (j > 17) {
            j = 17;
        }
        this.voteLastUpdateTime = currentTimeMillis;
        if (this.pollVoteInProgress) {
            this.voteRadOffset += ((float) (360 * j)) / 2000.0f;
            float f = this.voteRadOffset;
            int i = 360;
            this.voteRadOffset = f - ((float) (((int) (f / 360.0f)) * 360));
            this.voteCurrentProgressTime += (float) j;
            if (this.voteCurrentProgressTime >= 500.0f) {
                this.voteCurrentProgressTime = 500.0f;
            }
            if (this.voteRisingCircleLength) {
                this.voteCurrentCircleLength = (AndroidUtilities.accelerateInterpolator.getInterpolation(this.voteCurrentProgressTime / 500.0f) * 266.0f) + 4.0f;
            } else {
                if (!this.firstCircleLength) {
                    i = 270;
                }
                this.voteCurrentCircleLength = 4.0f - (((float) i) * (1.0f - AndroidUtilities.decelerateInterpolator.getInterpolation(this.voteCurrentProgressTime / 500.0f)));
            }
            if (this.voteCurrentProgressTime == 500.0f) {
                if (this.voteRisingCircleLength) {
                    this.voteRadOffset += 270.0f;
                    this.voteCurrentCircleLength = -266.0f;
                }
                this.voteRisingCircleLength ^= 1;
                if (this.firstCircleLength) {
                    this.firstCircleLength = false;
                }
                this.voteCurrentProgressTime = 0.0f;
            }
            invalidate();
        }
        if (this.animatePollAnswer) {
            this.pollAnimationProgressTime += (float) j;
            if (this.pollAnimationProgressTime >= 300.0f) {
                this.pollAnimationProgressTime = 300.0f;
            }
            this.pollAnimationProgress = AndroidUtilities.decelerateInterpolator.getInterpolation(this.pollAnimationProgressTime / 300.0f);
            if (this.pollAnimationProgress >= 1.0f) {
                this.pollAnimationProgress = 1.0f;
                this.animatePollAnswer = false;
                this.animatePollAnswerAlpha = false;
                this.pollVoteInProgress = false;
                this.pollUnvoteInProgress = false;
            }
            invalidate();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:370:0x0ac5  */
    /* JADX WARNING: Removed duplicated region for block: B:561:0x0fc1  */
    /* JADX WARNING: Removed duplicated region for block: B:560:0x0fbe  */
    private void drawContent(android.graphics.Canvas r29) {
        /*
        r28 = this;
        r1 = r28;
        r8 = r29;
        r0 = r1.needNewVisiblePart;
        r9 = 0;
        if (r0 == 0) goto L_0x0020;
    L_0x0009:
        r0 = r1.currentMessageObject;
        r0 = r0.type;
        if (r0 != 0) goto L_0x0020;
    L_0x000f:
        r0 = r1.scrollRect;
        r1.getLocalVisibleRect(r0);
        r0 = r1.scrollRect;
        r2 = r0.top;
        r0 = r0.bottom;
        r0 = r0 - r2;
        r1.setVisiblePart(r2, r0);
        r1.needNewVisiblePart = r9;
    L_0x0020:
        r0 = r1.currentMessagesGroup;
        r10 = 1;
        if (r0 == 0) goto L_0x0027;
    L_0x0025:
        r0 = 1;
        goto L_0x0028;
    L_0x0027:
        r0 = 0;
    L_0x0028:
        r1.forceNotDrawTime = r0;
        r0 = r1.photoImage;
        r2 = r1.currentMessageObject;
        r2 = org.telegram.ui.PhotoViewer.isShowingImage(r2);
        if (r2 != 0) goto L_0x0042;
    L_0x0034:
        r2 = org.telegram.ui.SecretMediaViewer.getInstance();
        r3 = r1.currentMessageObject;
        r2 = r2.isShowingImage(r3);
        if (r2 != 0) goto L_0x0042;
    L_0x0040:
        r2 = 1;
        goto L_0x0043;
    L_0x0042:
        r2 = 0;
    L_0x0043:
        r0.setVisible(r2, r9);
        r0 = r1.photoImage;
        r0 = r0.getVisible();
        r11 = 0;
        r12 = 2;
        r13 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r0 != 0) goto L_0x0066;
    L_0x0052:
        r1.mediaWasInvisible = r10;
        r1.timeWasInvisible = r10;
        r0 = r1.animatingNoSound;
        if (r0 != r10) goto L_0x005f;
    L_0x005a:
        r1.animatingNoSoundProgress = r11;
        r1.animatingNoSound = r9;
        goto L_0x008f;
    L_0x005f:
        if (r0 != r12) goto L_0x008f;
    L_0x0061:
        r1.animatingNoSoundProgress = r13;
        r1.animatingNoSound = r9;
        goto L_0x008f;
    L_0x0066:
        r0 = r1.groupPhotoInvisible;
        if (r0 == 0) goto L_0x006d;
    L_0x006a:
        r1.timeWasInvisible = r10;
        goto L_0x008f;
    L_0x006d:
        r0 = r1.mediaWasInvisible;
        if (r0 != 0) goto L_0x0075;
    L_0x0071:
        r0 = r1.timeWasInvisible;
        if (r0 == 0) goto L_0x008f;
    L_0x0075:
        r0 = r1.mediaWasInvisible;
        if (r0 == 0) goto L_0x007d;
    L_0x0079:
        r1.controlsAlpha = r11;
        r1.mediaWasInvisible = r9;
    L_0x007d:
        r0 = r1.timeWasInvisible;
        if (r0 == 0) goto L_0x0085;
    L_0x0081:
        r1.timeAlpha = r11;
        r1.timeWasInvisible = r9;
    L_0x0085:
        r2 = java.lang.System.currentTimeMillis();
        r1.lastControlsAlphaChangeTime = r2;
        r2 = 0;
        r1.totalChangeTime = r2;
    L_0x008f:
        r0 = r1.radialProgress;
        r2 = "chat_mediaProgress";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r0.setProgressColor(r2);
        r0 = r1.videoRadialProgress;
        r2 = "chat_mediaProgress";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r0.setProgressColor(r2);
        r0 = r1.currentMessageObject;
        r2 = r0.type;
        r14 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r16 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        r7 = 4;
        r17 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r18 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r19 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r20 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r21 = NUM; // 0x40400000 float:3.0 double:5.325712093E-315;
        r22 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        if (r2 != 0) goto L_0x093c;
    L_0x00bc:
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x00d2;
    L_0x00c2:
        r0 = r1.currentBackgroundDrawable;
        r0 = r0.getBounds();
        r0 = r0.left;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r0 = r0 + r2;
        r1.textX = r0;
        goto L_0x00ee;
    L_0x00d2:
        r0 = r1.currentBackgroundDrawable;
        r0 = r0.getBounds();
        r0 = r0.left;
        r2 = r1.mediaBackground;
        if (r2 != 0) goto L_0x00e5;
    L_0x00de:
        r2 = r1.drawPinnedBottom;
        if (r2 == 0) goto L_0x00e5;
    L_0x00e2:
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        goto L_0x00e7;
    L_0x00e5:
        r2 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
    L_0x00e7:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
        r1.textX = r0;
    L_0x00ee:
        r0 = r1.hasGamePreview;
        if (r0 == 0) goto L_0x0117;
    L_0x00f2:
        r0 = r1.textX;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r0 = r0 + r2;
        r1.textX = r0;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.textY = r0;
        r0 = r1.siteNameLayout;
        if (r0 == 0) goto L_0x0140;
    L_0x0108:
        r2 = r1.textY;
        r3 = r0.getLineCount();
        r3 = r3 - r10;
        r0 = r0.getLineBottom(r3);
        r2 = r2 + r0;
        r1.textY = r2;
        goto L_0x0140;
    L_0x0117:
        r0 = r1.hasInvoicePreview;
        if (r0 == 0) goto L_0x0137;
    L_0x011b:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.textY = r0;
        r0 = r1.siteNameLayout;
        if (r0 == 0) goto L_0x0140;
    L_0x0128:
        r2 = r1.textY;
        r3 = r0.getLineCount();
        r3 = r3 - r10;
        r0 = r0.getLineBottom(r3);
        r2 = r2 + r0;
        r1.textY = r2;
        goto L_0x0140;
    L_0x0137:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r1.textY = r0;
    L_0x0140:
        r0 = r1.textX;
        r1.unmovedTextX = r0;
        r0 = r1.currentMessageObject;
        r0 = r0.textXOffset;
        r0 = (r0 > r11 ? 1 : (r0 == r11 ? 0 : -1));
        if (r0 == 0) goto L_0x017d;
    L_0x014c:
        r0 = r1.replyNameLayout;
        if (r0 == 0) goto L_0x017d;
    L_0x0150:
        r0 = r1.backgroundWidth;
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 - r2;
        r2 = r1.currentMessageObject;
        r3 = r2.textWidth;
        r0 = r0 - r3;
        r3 = r1.hasNewLineForTime;
        if (r3 != 0) goto L_0x0176;
    L_0x0162:
        r3 = r1.timeWidth;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x016d;
    L_0x016a:
        r2 = 20;
        goto L_0x016e;
    L_0x016d:
        r2 = 0;
    L_0x016e:
        r2 = r2 + r7;
        r2 = (float) r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 + r2;
        r0 = r0 - r3;
    L_0x0176:
        if (r0 <= 0) goto L_0x017d;
    L_0x0178:
        r2 = r1.textX;
        r2 = r2 + r0;
        r1.textX = r2;
    L_0x017d:
        r0 = r1.currentMessageObject;
        r0 = r0.textLayoutBlocks;
        if (r0 == 0) goto L_0x0231;
    L_0x0183:
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0231;
    L_0x0189:
        r0 = r1.fullyDraw;
        if (r0 == 0) goto L_0x0199;
    L_0x018d:
        r1.firstVisibleBlockNum = r9;
        r0 = r1.currentMessageObject;
        r0 = r0.textLayoutBlocks;
        r0 = r0.size();
        r1.lastVisibleBlockNum = r0;
    L_0x0199:
        r0 = r1.firstVisibleBlockNum;
        if (r0 < 0) goto L_0x0231;
    L_0x019d:
        r2 = r0;
    L_0x019e:
        r0 = r1.lastVisibleBlockNum;
        if (r2 > r0) goto L_0x0231;
    L_0x01a2:
        r0 = r1.currentMessageObject;
        r0 = r0.textLayoutBlocks;
        r0 = r0.size();
        if (r2 < r0) goto L_0x01ae;
    L_0x01ac:
        goto L_0x0231;
    L_0x01ae:
        r0 = r1.currentMessageObject;
        r0 = r0.textLayoutBlocks;
        r0 = r0.get(r2);
        r0 = (org.telegram.messenger.MessageObject.TextLayoutBlock) r0;
        r29.save();
        r3 = r1.textX;
        r4 = r0.isRtl();
        if (r4 == 0) goto L_0x01ce;
    L_0x01c3:
        r4 = r1.currentMessageObject;
        r4 = r4.textXOffset;
        r4 = (double) r4;
        r4 = java.lang.Math.ceil(r4);
        r4 = (int) r4;
        goto L_0x01cf;
    L_0x01ce:
        r4 = 0;
    L_0x01cf:
        r3 = r3 - r4;
        r3 = (float) r3;
        r4 = r1.textY;
        r4 = (float) r4;
        r5 = r0.textYOffset;
        r4 = r4 + r5;
        r8.translate(r3, r4);
        r3 = r1.pressedLink;
        if (r3 == 0) goto L_0x01fb;
    L_0x01de:
        r3 = r1.linkBlockNum;
        if (r2 != r3) goto L_0x01fb;
    L_0x01e2:
        r3 = 0;
    L_0x01e3:
        r4 = r1.urlPath;
        r4 = r4.size();
        if (r3 >= r4) goto L_0x01fb;
    L_0x01eb:
        r4 = r1.urlPath;
        r4 = r4.get(r3);
        r4 = (android.graphics.Path) r4;
        r5 = org.telegram.ui.ActionBar.Theme.chat_urlPaint;
        r8.drawPath(r4, r5);
        r3 = r3 + 1;
        goto L_0x01e3;
    L_0x01fb:
        r3 = r1.linkSelectionBlockNum;
        if (r2 != r3) goto L_0x0220;
    L_0x01ff:
        r3 = r1.urlPathSelection;
        r3 = r3.isEmpty();
        if (r3 != 0) goto L_0x0220;
    L_0x0207:
        r3 = 0;
    L_0x0208:
        r4 = r1.urlPathSelection;
        r4 = r4.size();
        if (r3 >= r4) goto L_0x0220;
    L_0x0210:
        r4 = r1.urlPathSelection;
        r4 = r4.get(r3);
        r4 = (android.graphics.Path) r4;
        r5 = org.telegram.ui.ActionBar.Theme.chat_textSearchSelectionPaint;
        r8.drawPath(r4, r5);
        r3 = r3 + 1;
        goto L_0x0208;
    L_0x0220:
        r0 = r0.textLayout;	 Catch:{ Exception -> 0x0226 }
        r0.draw(r8);	 Catch:{ Exception -> 0x0226 }
        goto L_0x022a;
    L_0x0226:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x022a:
        r29.restore();
        r2 = r2 + 1;
        goto L_0x019e;
    L_0x0231:
        r0 = r1.hasLinkPreview;
        if (r0 != 0) goto L_0x0241;
    L_0x0235:
        r0 = r1.hasGamePreview;
        if (r0 != 0) goto L_0x0241;
    L_0x0239:
        r0 = r1.hasInvoicePreview;
        if (r0 == 0) goto L_0x023e;
    L_0x023d:
        goto L_0x0241;
    L_0x023e:
        r3 = 0;
        goto L_0x0937;
    L_0x0241:
        r0 = r1.hasGamePreview;
        if (r0 == 0) goto L_0x0255;
    L_0x0245:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r2 = r1.unmovedTextX;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r2 - r3;
    L_0x0253:
        r6 = r2;
        goto L_0x027b;
    L_0x0255:
        r0 = r1.hasInvoicePreview;
        if (r0 == 0) goto L_0x0267;
    L_0x0259:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r2 = r1.namesOffset;
        r0 = r0 + r2;
        r2 = r1.unmovedTextX;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r13);
        goto L_0x0279;
    L_0x0267:
        r0 = r1.textY;
        r2 = r1.currentMessageObject;
        r2 = r2.textHeight;
        r0 = r0 + r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r0 = r0 + r2;
        r2 = r1.unmovedTextX;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r13);
    L_0x0279:
        r2 = r2 + r3;
        goto L_0x0253;
    L_0x027b:
        r2 = r1.hasInvoicePreview;
        if (r2 != 0) goto L_0x02bd;
    L_0x027f:
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x028c;
    L_0x0289:
        r3 = "chat_outPreviewLine";
        goto L_0x028e;
    L_0x028c:
        r3 = "chat_inPreviewLine";
    L_0x028e:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r3 = (float) r6;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r0 - r2;
        r4 = (float) r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r6;
        r5 = (float) r2;
        r2 = r1.linkPreviewHeight;
        r2 = r2 + r0;
        r23 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r2 + r23;
        r2 = (float) r2;
        r23 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r24 = r2;
        r2 = r29;
        r15 = r6;
        r6 = r24;
        r14 = 4;
        r7 = r23;
        r2.drawRect(r3, r4, r5, r6, r7);
        goto L_0x02bf;
    L_0x02bd:
        r15 = r6;
        r14 = 4;
    L_0x02bf:
        r2 = r1.siteNameLayout;
        if (r2 == 0) goto L_0x0319;
    L_0x02c3:
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x02d0;
    L_0x02cd:
        r3 = "chat_outSiteNameText";
        goto L_0x02d2;
    L_0x02d0:
        r3 = "chat_inSiteNameText";
    L_0x02d2:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r29.save();
        r2 = r1.siteNameRtl;
        if (r2 == 0) goto L_0x02ed;
    L_0x02e0:
        r2 = r1.backgroundWidth;
        r3 = r1.siteNameWidth;
        r2 = r2 - r3;
        r3 = NUM; // 0x42000000 float:32.0 double:5.4707704E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        goto L_0x02f7;
    L_0x02ed:
        r2 = r1.hasInvoicePreview;
        if (r2 == 0) goto L_0x02f3;
    L_0x02f1:
        r2 = 0;
        goto L_0x02f7;
    L_0x02f3:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x02f7:
        r6 = r15 + r2;
        r2 = (float) r6;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r3 = r0 - r3;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.siteNameLayout;
        r2.draw(r8);
        r29.restore();
        r2 = r1.siteNameLayout;
        r3 = r2.getLineCount();
        r3 = r3 - r10;
        r2 = r2.getLineBottom(r3);
        r2 = r2 + r0;
        goto L_0x031a;
    L_0x0319:
        r2 = r0;
    L_0x031a:
        r3 = r1.hasGamePreview;
        if (r3 != 0) goto L_0x0322;
    L_0x031e:
        r3 = r1.hasInvoicePreview;
        if (r3 == 0) goto L_0x0338;
    L_0x0322:
        r3 = r1.currentMessageObject;
        r3 = r3.textHeight;
        if (r3 == 0) goto L_0x0338;
    L_0x0328:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = r3 + r4;
        r0 = r0 + r3;
        r3 = r1.currentMessageObject;
        r3 = r3.textHeight;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = r3 + r4;
        r2 = r2 + r3;
    L_0x0338:
        r3 = r1.drawPhotoImage;
        if (r3 == 0) goto L_0x0340;
    L_0x033c:
        r3 = r1.drawInstantView;
        if (r3 != 0) goto L_0x0349;
    L_0x0340:
        r3 = r1.drawInstantViewType;
        r4 = 6;
        if (r3 != r4) goto L_0x048c;
    L_0x0345:
        r3 = r1.imageBackgroundColor;
        if (r3 == 0) goto L_0x048c;
    L_0x0349:
        if (r2 == r0) goto L_0x0350;
    L_0x034b:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r3;
    L_0x0350:
        r7 = r2;
        r2 = r1.imageBackgroundSideColor;
        if (r2 == 0) goto L_0x03a6;
    L_0x0355:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r6 = r15 + r2;
        r2 = r1.photoImage;
        r3 = r1.imageBackgroundSideWidth;
        r4 = r2.getImageWidth();
        r3 = r3 - r4;
        r3 = r3 / r12;
        r3 = r3 + r6;
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r5 = r1.photoImage;
        r5 = r5.getImageHeight();
        r2.setImageCoords(r3, r7, r4, r5);
        r2 = r1.rect;
        r3 = (float) r6;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r4 = (float) r4;
        r5 = r1.imageBackgroundSideWidth;
        r6 = r6 + r5;
        r5 = (float) r6;
        r6 = r1.photoImage;
        r6 = r6.getImageY2();
        r6 = (float) r6;
        r2.set(r3, r4, r5, r6);
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r3 = r1.imageBackgroundSideColor;
        r2.setColor(r3);
        r2 = r1.rect;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = (float) r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r4 = (float) r4;
        r5 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r8.drawRoundRect(r2, r3, r4, r5);
        goto L_0x03bd;
    L_0x03a6:
        r2 = r1.photoImage;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r6 = r15 + r3;
        r3 = r1.photoImage;
        r3 = r3.getImageWidth();
        r4 = r1.photoImage;
        r4 = r4.getImageHeight();
        r2.setImageCoords(r6, r7, r3, r4);
    L_0x03bd:
        r2 = r1.imageBackgroundColor;
        if (r2 == 0) goto L_0x0427;
    L_0x03c1:
        r3 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r3.setColor(r2);
        r2 = r1.rect;
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r3 = (float) r3;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r4 = (float) r4;
        r5 = r1.photoImage;
        r5 = r5.getImageX2();
        r5 = (float) r5;
        r6 = r1.photoImage;
        r6 = r6.getImageY2();
        r6 = (float) r6;
        r2.set(r3, r4, r5, r6);
        r2 = r1.imageBackgroundSideColor;
        if (r2 == 0) goto L_0x0413;
    L_0x03eb:
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r3 = (float) r2;
        r2 = r1.photoImage;
        r2 = r2.getImageY();
        r4 = (float) r2;
        r2 = r1.photoImage;
        r2 = r2.getImageX2();
        r5 = (float) r2;
        r2 = r1.photoImage;
        r2 = r2.getImageY2();
        r6 = (float) r2;
        r23 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r2 = r29;
        r25 = r7;
        r7 = r23;
        r2.drawRect(r3, r4, r5, r6, r7);
        goto L_0x0429;
    L_0x0413:
        r25 = r7;
        r2 = r1.rect;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = (float) r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r4 = (float) r4;
        r5 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r8.drawRoundRect(r2, r3, r4, r5);
        goto L_0x0429;
    L_0x0427:
        r25 = r7;
    L_0x0429:
        r2 = r1.drawPhotoImage;
        if (r2 == 0) goto L_0x0478;
    L_0x042d:
        r2 = r1.drawInstantView;
        if (r2 == 0) goto L_0x0478;
    L_0x0431:
        r2 = r1.drawImageButton;
        if (r2 == 0) goto L_0x0471;
    L_0x0435:
        r2 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r3 = (float) r3;
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r4 = r4 - r2;
        r4 = (float) r4;
        r4 = r4 / r19;
        r3 = r3 + r4;
        r3 = (int) r3;
        r1.buttonX = r3;
        r3 = r1.photoImage;
        r3 = r3.getImageY();
        r3 = (float) r3;
        r4 = r1.photoImage;
        r4 = r4.getImageHeight();
        r4 = r4 - r2;
        r4 = (float) r4;
        r4 = r4 / r19;
        r3 = r3 + r4;
        r3 = (int) r3;
        r1.buttonY = r3;
        r3 = r1.radialProgress;
        r4 = r1.buttonX;
        r5 = r1.buttonY;
        r6 = r4 + r2;
        r2 = r2 + r5;
        r3.setProgressRect(r4, r5, r6, r2);
    L_0x0471:
        r2 = r1.photoImage;
        r2 = r2.draw(r8);
        goto L_0x0479;
    L_0x0478:
        r2 = 0;
    L_0x0479:
        r3 = r1.photoImage;
        r3 = r3.getImageHeight();
        r4 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r3 = r3 + r4;
        r3 = r25 + r3;
        r27 = r3;
        r3 = r2;
        r2 = r27;
        goto L_0x048d;
    L_0x048c:
        r3 = 0;
    L_0x048d:
        r4 = r1.currentMessageObject;
        r4 = r4.isOutOwner();
        if (r4 == 0) goto L_0x04ac;
    L_0x0495:
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r5 = "chat_messageTextOut";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r5 = "chat_messageTextOut";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
        goto L_0x04c2;
    L_0x04ac:
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r5 = "chat_messageTextIn";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r5 = "chat_messageTextIn";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
    L_0x04c2:
        r4 = r1.titleLayout;
        if (r4 == 0) goto L_0x04ff;
    L_0x04c6:
        if (r2 == r0) goto L_0x04cd;
    L_0x04c8:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r4;
    L_0x04cd:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r4 = r2 - r4;
        r29.save();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r6 = r15 + r5;
        r5 = r1.titleX;
        r6 = r6 + r5;
        r5 = (float) r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r6 = r2 - r6;
        r6 = (float) r6;
        r8.translate(r5, r6);
        r5 = r1.titleLayout;
        r5.draw(r8);
        r29.restore();
        r5 = r1.titleLayout;
        r6 = r5.getLineCount();
        r6 = r6 - r10;
        r5 = r5.getLineBottom(r6);
        r2 = r2 + r5;
        goto L_0x0500;
    L_0x04ff:
        r4 = 0;
    L_0x0500:
        r5 = r1.authorLayout;
        if (r5 == 0) goto L_0x053e;
    L_0x0504:
        if (r2 == r0) goto L_0x050b;
    L_0x0506:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r5;
    L_0x050b:
        if (r4 != 0) goto L_0x0513;
    L_0x050d:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r4 = r2 - r4;
    L_0x0513:
        r29.save();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r6 = r15 + r5;
        r5 = r1.authorX;
        r6 = r6 + r5;
        r5 = (float) r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r6 = r2 - r6;
        r6 = (float) r6;
        r8.translate(r5, r6);
        r5 = r1.authorLayout;
        r5.draw(r8);
        r29.restore();
        r5 = r1.authorLayout;
        r6 = r5.getLineCount();
        r6 = r6 - r10;
        r5 = r5.getLineBottom(r6);
        r2 = r2 + r5;
    L_0x053e:
        r5 = r1.descriptionLayout;
        if (r5 == 0) goto L_0x05a9;
    L_0x0542:
        if (r2 == r0) goto L_0x0549;
    L_0x0544:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r5;
    L_0x0549:
        if (r4 != 0) goto L_0x0551;
    L_0x054b:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r4 = r2 - r4;
    L_0x0551:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r5 = r2 - r5;
        r1.descriptionY = r5;
        r29.save();
        r5 = r1.hasInvoicePreview;
        if (r5 == 0) goto L_0x0562;
    L_0x0560:
        r5 = 0;
        goto L_0x0566;
    L_0x0562:
        r5 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x0566:
        r6 = r15 + r5;
        r5 = r1.descriptionX;
        r6 = r6 + r5;
        r5 = (float) r6;
        r6 = r1.descriptionY;
        r6 = (float) r6;
        r8.translate(r5, r6);
        r5 = r1.pressedLink;
        if (r5 == 0) goto L_0x0595;
    L_0x0576:
        r5 = r1.linkBlockNum;
        r6 = -10;
        if (r5 != r6) goto L_0x0595;
    L_0x057c:
        r5 = 0;
    L_0x057d:
        r6 = r1.urlPath;
        r6 = r6.size();
        if (r5 >= r6) goto L_0x0595;
    L_0x0585:
        r6 = r1.urlPath;
        r6 = r6.get(r5);
        r6 = (android.graphics.Path) r6;
        r7 = org.telegram.ui.ActionBar.Theme.chat_urlPaint;
        r8.drawPath(r6, r7);
        r5 = r5 + 1;
        goto L_0x057d;
    L_0x0595:
        r5 = r1.descriptionLayout;
        r5.draw(r8);
        r29.restore();
        r5 = r1.descriptionLayout;
        r6 = r5.getLineCount();
        r6 = r6 - r10;
        r5 = r5.getLineBottom(r6);
        r2 = r2 + r5;
    L_0x05a9:
        r5 = r1.drawPhotoImage;
        if (r5 == 0) goto L_0x0665;
    L_0x05ad:
        r5 = r1.drawInstantView;
        if (r5 != 0) goto L_0x0665;
    L_0x05b1:
        if (r2 == r0) goto L_0x05b8;
    L_0x05b3:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r3;
    L_0x05b8:
        r3 = r1.isSmallImage;
        if (r3 == 0) goto L_0x05d9;
    L_0x05bc:
        r3 = r1.photoImage;
        r5 = r1.backgroundWidth;
        r6 = r15 + r5;
        r5 = NUM; // 0x42a20000 float:81.0 double:5.52322452E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r6 = r6 - r5;
        r5 = r1.photoImage;
        r5 = r5.getImageWidth();
        r7 = r1.photoImage;
        r7 = r7.getImageHeight();
        r3.setImageCoords(r6, r4, r5, r7);
        goto L_0x063d;
    L_0x05d9:
        r3 = r1.photoImage;
        r4 = r1.hasInvoicePreview;
        if (r4 == 0) goto L_0x05e8;
    L_0x05df:
        r4 = NUM; // 0x40CLASSNAMEa float:6.3 double:5.370265717E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = -r4;
        goto L_0x05ec;
    L_0x05e8:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x05ec:
        r6 = r15 + r4;
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r5 = r1.photoImage;
        r5 = r5.getImageHeight();
        r3.setImageCoords(r6, r2, r4, r5);
        r3 = r1.drawImageButton;
        if (r3 == 0) goto L_0x063d;
    L_0x0601:
        r3 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.photoImage;
        r4 = r4.getImageX();
        r4 = (float) r4;
        r5 = r1.photoImage;
        r5 = r5.getImageWidth();
        r5 = r5 - r3;
        r5 = (float) r5;
        r5 = r5 / r19;
        r4 = r4 + r5;
        r4 = (int) r4;
        r1.buttonX = r4;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r4 = (float) r4;
        r5 = r1.photoImage;
        r5 = r5.getImageHeight();
        r5 = r5 - r3;
        r5 = (float) r5;
        r5 = r5 / r19;
        r4 = r4 + r5;
        r4 = (int) r4;
        r1.buttonY = r4;
        r4 = r1.radialProgress;
        r5 = r1.buttonX;
        r6 = r1.buttonY;
        r7 = r5 + r3;
        r3 = r3 + r6;
        r4.setProgressRect(r5, r6, r7, r3);
    L_0x063d:
        r3 = r1.currentMessageObject;
        r3 = r3.isRoundVideo();
        if (r3 == 0) goto L_0x065f;
    L_0x0645:
        r3 = org.telegram.messenger.MediaController.getInstance();
        r4 = r1.currentMessageObject;
        r3 = r3.isPlayingMessage(r4);
        if (r3 == 0) goto L_0x065f;
    L_0x0651:
        r3 = org.telegram.messenger.MediaController.getInstance();
        r3 = r3.isVideoDrawingReady();
        if (r3 == 0) goto L_0x065f;
    L_0x065b:
        r1.drawTime = r10;
        r3 = 1;
        goto L_0x0665;
    L_0x065f:
        r3 = r1.photoImage;
        r3 = r3.draw(r8);
    L_0x0665:
        r4 = r1.documentAttachType;
        if (r4 == r14) goto L_0x066b;
    L_0x0669:
        if (r4 != r12) goto L_0x069f;
    L_0x066b:
        r4 = r1.photoImage;
        r4 = r4.getImageX();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 + r5;
        r1.videoButtonX = r4;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 + r5;
        r1.videoButtonY = r4;
        r4 = r1.videoRadialProgress;
        r5 = r1.videoButtonX;
        r6 = r1.videoButtonY;
        r7 = NUM; // 0x41CLASSNAME float:24.0 double:5.450047783E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = r7 + r5;
        r14 = r1.videoButtonY;
        r25 = NUM; // 0x41CLASSNAME float:24.0 double:5.450047783E-315;
        r25 = org.telegram.messenger.AndroidUtilities.dp(r25);
        r14 = r14 + r25;
        r4.setProgressRect(r5, r6, r7, r14);
    L_0x069f:
        r4 = r1.photosCountLayout;
        if (r4 == 0) goto L_0x0746;
    L_0x06a3:
        r4 = r1.photoImage;
        r4 = r4.getVisible();
        if (r4 == 0) goto L_0x0746;
    L_0x06ab:
        r4 = r1.photoImage;
        r4 = r4.getImageX();
        r5 = r1.photoImage;
        r5 = r5.getImageWidth();
        r4 = r4 + r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 - r5;
        r5 = r1.photosCountWidth;
        r4 = r4 - r5;
        r5 = r1.photoImage;
        r5 = r5.getImageY();
        r6 = r1.photoImage;
        r6 = r6.getImageHeight();
        r5 = r5 + r6;
        r6 = NUM; // 0x41980000 float:19.0 double:5.43709615E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 - r6;
        r6 = r1.rect;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r7 = r4 - r7;
        r7 = (float) r7;
        r14 = NUM; // 0x3fCLASSNAME float:1.5 double:5.28426686E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        r14 = r5 - r14;
        r14 = (float) r14;
        r11 = r1.photosCountWidth;
        r11 = r11 + r4;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r11 = r11 + r26;
        r11 = (float) r11;
        r26 = NUM; // 0x41680000 float:14.5 double:5.42155419E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
        r12 = r5 + r26;
        r12 = (float) r12;
        r6.set(r7, r14, r11, r12);
        r6 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r6 = r6.getAlpha();
        r7 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r11 = (float) r6;
        r12 = r1.controlsAlpha;
        r11 = r11 * r12;
        r11 = (int) r11;
        r7.setAlpha(r11);
        r7 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r11 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r12 = r1.controlsAlpha;
        r12 = r12 * r11;
        r11 = (int) r12;
        r7.setAlpha(r11);
        r7 = r1.rect;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r11 = (float) r11;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r12 = (float) r12;
        r14 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r8.drawRoundRect(r7, r11, r12, r14);
        r7 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r7.setAlpha(r6);
        r29.save();
        r4 = (float) r4;
        r5 = (float) r5;
        r8.translate(r4, r5);
        r4 = r1.photosCountLayout;
        r4.draw(r8);
        r29.restore();
        r4 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r5 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r4.setAlpha(r5);
    L_0x0746:
        r4 = r1.videoInfoLayout;
        if (r4 == 0) goto L_0x086d;
    L_0x074a:
        r4 = r1.drawPhotoImage;
        if (r4 == 0) goto L_0x0756;
    L_0x074e:
        r4 = r1.photoImage;
        r4 = r4.getVisible();
        if (r4 == 0) goto L_0x086d;
    L_0x0756:
        r4 = r1.imageBackgroundSideColor;
        if (r4 != 0) goto L_0x086d;
    L_0x075a:
        r4 = r1.hasGamePreview;
        if (r4 != 0) goto L_0x07cb;
    L_0x075e:
        r4 = r1.hasInvoicePreview;
        if (r4 != 0) goto L_0x07cb;
    L_0x0762:
        r4 = r1.documentAttachType;
        r5 = 8;
        if (r4 != r5) goto L_0x0769;
    L_0x0768:
        goto L_0x07cb;
    L_0x0769:
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r2 = r2 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r2 = r2 - r4;
        r4 = r1.durationWidth;
        r6 = r2 - r4;
        r2 = r1.photoImage;
        r2 = r2.getImageY();
        r4 = r1.photoImage;
        r4 = r4.getImageHeight();
        r2 = r2 + r4;
        r4 = NUM; // 0x41980000 float:19.0 double:5.43709615E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 - r4;
        r4 = r1.rect;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r5 = r6 - r5;
        r5 = (float) r5;
        r7 = NUM; // 0x3fCLASSNAME float:1.5 double:5.28426686E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = r2 - r7;
        r7 = (float) r7;
        r11 = r1.durationWidth;
        r11 = r11 + r6;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r11 = r11 + r12;
        r11 = (float) r11;
        r12 = NUM; // 0x41680000 float:14.5 double:5.42155419E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r12 = r12 + r2;
        r12 = (float) r12;
        r4.set(r5, r7, r11, r12);
        r4 = r1.rect;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r5 = (float) r5;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r7 = (float) r7;
        r11 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r8.drawRoundRect(r4, r5, r7, r11);
        goto L_0x082a;
    L_0x07cb:
        r4 = r1.drawPhotoImage;
        if (r4 == 0) goto L_0x0829;
    L_0x07cf:
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r4 = NUM; // 0x41080000 float:8.5 double:5.390470265E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r6 = r2 + r4;
        r2 = r1.photoImage;
        r2 = r2.getImageY();
        r4 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r2 = r2 + r4;
        r4 = r1.documentAttachType;
        r5 = 8;
        if (r4 != r5) goto L_0x07f1;
    L_0x07ee:
        r4 = NUM; // 0x41680000 float:14.5 double:5.42155419E-315;
        goto L_0x07f3;
    L_0x07f1:
        r4 = NUM; // 0x41840000 float:16.5 double:5.43062033E-315;
    L_0x07f3:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r1.rect;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r7 = r6 - r7;
        r7 = (float) r7;
        r11 = NUM; // 0x3fCLASSNAME float:1.5 double:5.28426686E-315;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r11 = r2 - r11;
        r11 = (float) r11;
        r12 = r1.durationWidth;
        r12 = r12 + r6;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r12 = r12 + r14;
        r12 = (float) r12;
        r4 = r4 + r2;
        r4 = (float) r4;
        r5.set(r7, r11, r12, r4);
        r4 = r1.rect;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r5 = (float) r5;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r7 = (float) r7;
        r11 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r8.drawRoundRect(r4, r5, r7, r11);
        goto L_0x082a;
    L_0x0829:
        r6 = r15;
    L_0x082a:
        r29.save();
        r4 = (float) r6;
        r2 = (float) r2;
        r8.translate(r4, r2);
        r2 = r1.hasInvoicePreview;
        if (r2 == 0) goto L_0x0865;
    L_0x0836:
        r2 = r1.drawPhotoImage;
        if (r2 == 0) goto L_0x0846;
    L_0x083a:
        r2 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r4 = "chat_previewGameText";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
        goto L_0x0865;
    L_0x0846:
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x085a;
    L_0x084e:
        r2 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r4 = "chat_messageTextOut";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
        goto L_0x0865;
    L_0x085a:
        r2 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r4 = "chat_messageTextIn";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
    L_0x0865:
        r2 = r1.videoInfoLayout;
        r2.draw(r8);
        r29.restore();
    L_0x086d:
        r2 = r1.drawInstantView;
        if (r2 == 0) goto L_0x0937;
    L_0x0871:
        r2 = r1.linkPreviewHeight;
        r0 = r0 + r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r0 = r0 + r2;
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r4 = r1.currentMessageObject;
        r4 = r4.isOutOwner();
        if (r4 == 0) goto L_0x089a;
    L_0x0883:
        r4 = org.telegram.ui.ActionBar.Theme.chat_msgOutInstantDrawable;
        r5 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r6 = "chat_outPreviewInstantText";
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r5.setColor(r6);
        r5 = "chat_outPreviewInstantText";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r2.setColor(r5);
        goto L_0x08b0;
    L_0x089a:
        r4 = org.telegram.ui.ActionBar.Theme.chat_msgInInstantDrawable;
        r5 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r6 = "chat_inPreviewInstantText";
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r5.setColor(r6);
        r5 = "chat_inPreviewInstantText";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r2.setColor(r5);
    L_0x08b0:
        r5 = android.os.Build.VERSION.SDK_INT;
        r6 = 21;
        if (r5 < r6) goto L_0x08cc;
    L_0x08b6:
        r1.selectorDrawableMaskType = r9;
        r5 = r1.selectorDrawable;
        r6 = r1.instantWidth;
        r6 = r6 + r15;
        r7 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = r7 + r0;
        r5.setBounds(r15, r0, r6, r7);
        r5 = r1.selectorDrawable;
        r5.draw(r8);
    L_0x08cc:
        r5 = r1.rect;
        r6 = (float) r15;
        r7 = (float) r0;
        r11 = r1.instantWidth;
        r11 = r11 + r15;
        r11 = (float) r11;
        r12 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r12 = r12 + r0;
        r12 = (float) r12;
        r5.set(r6, r7, r11, r12);
        r5 = r1.rect;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r6 = (float) r6;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r7 = (float) r7;
        r8.drawRoundRect(r5, r6, r7, r2);
        r2 = r1.drawInstantViewType;
        if (r2 != 0) goto L_0x0918;
    L_0x08f2:
        r2 = r1.instantTextLeftX;
        r5 = r1.instantTextX;
        r2 = r2 + r5;
        r2 = r2 + r15;
        r5 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r2 = r2 - r5;
        r5 = NUM; // 0x41380000 float:11.5 double:5.406012226E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = r5 + r0;
        r6 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r7 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r4, r2, r5, r6, r7);
        r4.draw(r8);
    L_0x0918:
        r2 = r1.instantViewLayout;
        if (r2 == 0) goto L_0x0937;
    L_0x091c:
        r29.save();
        r2 = r1.instantTextX;
        r6 = r15 + r2;
        r2 = (float) r6;
        r4 = NUM; // 0x41280000 float:10.5 double:5.40083157E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        r0 = (float) r0;
        r8.translate(r2, r0);
        r0 = r1.instantViewLayout;
        r0.draw(r8);
        r29.restore();
    L_0x0937:
        r1.drawTime = r10;
        r0 = r3;
        goto L_0x0b1c;
    L_0x093c:
        r2 = r1.drawPhotoImage;
        if (r2 == 0) goto L_0x0b1b;
    L_0x0940:
        r0 = r0.isRoundVideo();
        if (r0 == 0) goto L_0x0961;
    L_0x0946:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r2 = r1.currentMessageObject;
        r0 = r0.isPlayingMessage(r2);
        if (r0 == 0) goto L_0x0961;
    L_0x0952:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r0 = r0.isVideoDrawingReady();
        if (r0 == 0) goto L_0x0961;
    L_0x095c:
        r1.drawTime = r10;
        r0 = 1;
        goto L_0x0b1c;
    L_0x0961:
        r0 = r1.currentMessageObject;
        r0 = r0.type;
        r2 = 5;
        if (r0 != r2) goto L_0x09e5;
    L_0x0968:
        r0 = org.telegram.ui.ActionBar.Theme.chat_roundVideoShadow;
        if (r0 == 0) goto L_0x09e5;
    L_0x096c:
        r0 = r1.photoImage;
        r0 = r0.getImageX();
        r2 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r0 = r0 - r2;
        r2 = r1.photoImage;
        r2 = r2.getImageY();
        r3 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 - r3;
        r3 = org.telegram.ui.ActionBar.Theme.chat_roundVideoShadow;
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r3.setAlpha(r4);
        r3 = org.telegram.ui.ActionBar.Theme.chat_roundVideoShadow;
        r4 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r4 = r4 + r0;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r4 = r4 + r5;
        r5 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r5 = r5 + r2;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r5 = r5 + r6;
        r3.setBounds(r0, r2, r4, r5);
        r0 = org.telegram.ui.ActionBar.Theme.chat_roundVideoShadow;
        r0.draw(r8);
        r0 = r1.photoImage;
        r0 = r0.hasBitmapImage();
        if (r0 == 0) goto L_0x09b5;
    L_0x09ab:
        r0 = r1.photoImage;
        r0 = r0.getCurrentAlpha();
        r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1));
        if (r0 == 0) goto L_0x09e5;
    L_0x09b5:
        r0 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x09c2;
    L_0x09bf:
        r2 = "chat_outBubble";
        goto L_0x09c4;
    L_0x09c2:
        r2 = "chat_inBubble";
    L_0x09c4:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r0.setColor(r2);
        r0 = r1.photoImage;
        r0 = r0.getCenterX();
        r2 = r1.photoImage;
        r2 = r2.getCenterY();
        r3 = r1.photoImage;
        r3 = r3.getImageWidth();
        r4 = 2;
        r3 = r3 / r4;
        r3 = (float) r3;
        r4 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r8.drawCircle(r0, r2, r3, r4);
    L_0x09e5:
        r0 = r1.photoCheckBox;
        if (r0 == 0) goto L_0x0a08;
    L_0x09e9:
        r2 = r1.checkBoxVisible;
        if (r2 != 0) goto L_0x09fa;
    L_0x09ed:
        r0 = r0.getProgress();
        r2 = 0;
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 != 0) goto L_0x09fa;
    L_0x09f6:
        r0 = r1.checkBoxAnimationInProgress;
        if (r0 == 0) goto L_0x0a08;
    L_0x09fa:
        r0 = r1.currentMessagesGroup;
        if (r0 == 0) goto L_0x0a08;
    L_0x09fe:
        r0 = r0.messages;
        r0 = r0.size();
        if (r0 <= r10) goto L_0x0a08;
    L_0x0a06:
        r0 = 1;
        goto L_0x0a09;
    L_0x0a08:
        r0 = 0;
    L_0x0a09:
        r1.drawPhotoCheckBox = r0;
        r0 = r1.drawPhotoCheckBox;
        if (r0 == 0) goto L_0x0a9f;
    L_0x0a0f:
        r0 = r1.photoCheckBox;
        r0 = r0.isChecked();
        if (r0 != 0) goto L_0x0a26;
    L_0x0a17:
        r0 = r1.photoCheckBox;
        r0 = r0.getProgress();
        r2 = 0;
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 != 0) goto L_0x0a26;
    L_0x0a22:
        r0 = r1.checkBoxAnimationInProgress;
        if (r0 == 0) goto L_0x0a9f;
    L_0x0a26:
        r0 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0a33;
    L_0x0a30:
        r2 = "chat_outBubbleSelected";
        goto L_0x0a35;
    L_0x0a33:
        r2 = "chat_inBubbleSelected";
    L_0x0a35:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r0.setColor(r2);
        r0 = r1.rect;
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r2 = (float) r2;
        r3 = r1.photoImage;
        r3 = r3.getImageY();
        r3 = (float) r3;
        r4 = r1.photoImage;
        r4 = r4.getImageX2();
        r4 = (float) r4;
        r5 = r1.photoImage;
        r5 = r5.getImageY2();
        r5 = (float) r5;
        r0.set(r2, r3, r4, r5);
        r0 = r1.rect;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r2 = (float) r2;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = (float) r3;
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r8.drawRoundRect(r0, r2, r3, r4);
        r0 = r1.photoImage;
        r2 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = (float) r3;
        r3 = r1.photoCheckBox;
        r3 = r3.getProgress();
        r2 = r2 * r3;
        r0.setSideClip(r2);
        r0 = r1.checkBoxAnimationInProgress;
        if (r0 == 0) goto L_0x0a8e;
    L_0x0a86:
        r0 = r1.photoCheckBox;
        r2 = r1.checkBoxAnimationProgress;
        r0.setBackgroundAlpha(r2);
        goto L_0x0aa5;
    L_0x0a8e:
        r0 = r1.photoCheckBox;
        r2 = r1.checkBoxVisible;
        if (r2 == 0) goto L_0x0a97;
    L_0x0a94:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x0a9b;
    L_0x0a97:
        r2 = r0.getProgress();
    L_0x0a9b:
        r0.setBackgroundAlpha(r2);
        goto L_0x0aa5;
    L_0x0a9f:
        r0 = r1.photoImage;
        r2 = 0;
        r0.setSideClip(r2);
    L_0x0aa5:
        r0 = r1.photoImage;
        r0 = r0.draw(r8);
        r2 = r1.drawTime;
        r3 = r1.photoImage;
        r3 = r3.getVisible();
        r1.drawTime = r3;
        r3 = r1.currentPosition;
        if (r3 == 0) goto L_0x0b1c;
    L_0x0ab9:
        r3 = r1.drawTime;
        if (r2 == r3) goto L_0x0b1c;
    L_0x0abd:
        r2 = r28.getParent();
        r2 = (android.view.ViewGroup) r2;
        if (r2 == 0) goto L_0x0b1c;
    L_0x0ac5:
        r3 = r1.currentPosition;
        r3 = r3.last;
        if (r3 != 0) goto L_0x0b17;
    L_0x0acb:
        r3 = r2.getChildCount();
        r4 = 0;
    L_0x0ad0:
        if (r4 >= r3) goto L_0x0b1c;
    L_0x0ad2:
        r5 = r2.getChildAt(r4);
        if (r5 == r1) goto L_0x0b14;
    L_0x0ad8:
        r6 = r5 instanceof org.telegram.ui.Cells.ChatMessageCell;
        if (r6 != 0) goto L_0x0add;
    L_0x0adc:
        goto L_0x0b14;
    L_0x0add:
        r5 = (org.telegram.ui.Cells.ChatMessageCell) r5;
        r6 = r5.getCurrentMessagesGroup();
        r7 = r1.currentMessagesGroup;
        if (r6 != r7) goto L_0x0b14;
    L_0x0ae7:
        r6 = r5.getCurrentPosition();
        r7 = r6.last;
        if (r7 == 0) goto L_0x0b14;
    L_0x0aef:
        r6 = r6.maxY;
        r7 = r1.currentPosition;
        r7 = r7.maxY;
        if (r6 != r7) goto L_0x0b14;
    L_0x0af7:
        r6 = r5.timeX;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r6 = r6 - r7;
        r7 = r5.getLeft();
        r6 = r6 + r7;
        r7 = r28.getRight();
        if (r6 >= r7) goto L_0x0b14;
    L_0x0b09:
        r6 = r1.drawTime;
        r6 = r6 ^ r10;
        r5.groupPhotoInvisible = r6;
        r5.invalidate();
        r2.invalidate();
    L_0x0b14:
        r4 = r4 + 1;
        goto L_0x0ad0;
    L_0x0b17:
        r2.invalidate();
        goto L_0x0b1c;
    L_0x0b1b:
        r0 = 0;
    L_0x0b1c:
        r2 = r1.documentAttachType;
        r3 = 2;
        if (r2 != r3) goto L_0x0b83;
    L_0x0b21:
        r2 = r1.photoImage;
        r2 = r2.getVisible();
        if (r2 == 0) goto L_0x0var_;
    L_0x0b29:
        r2 = r1.hasGamePreview;
        if (r2 != 0) goto L_0x0var_;
    L_0x0b2d:
        r2 = r1.currentMessageObject;
        r2 = r2.needDrawBluredPreview();
        if (r2 != 0) goto L_0x0var_;
    L_0x0b35:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r2 = (android.graphics.drawable.BitmapDrawable) r2;
        r2 = r2.getPaint();
        r2 = r2.getAlpha();
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r4 = (float) r2;
        r5 = r1.controlsAlpha;
        r4 = r4 * r5;
        r4 = (int) r4;
        r3.setAlpha(r4);
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r4 = r1.photoImage;
        r4 = r4.getImageX();
        r5 = r1.photoImage;
        r5 = r5.getImageWidth();
        r4 = r4 + r5;
        r5 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r1.otherX = r4;
        r5 = r1.photoImage;
        r5 = r5.getImageY();
        r6 = NUM; // 0x4101999a float:8.1 double:5.388398005E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 + r6;
        r1.otherY = r5;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r3, r4, r5);
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r3.draw(r8);
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r3.setAlpha(r2);
        goto L_0x0var_;
    L_0x0b83:
        r3 = 7;
        if (r2 != r3) goto L_0x0cd9;
    L_0x0b86:
        r2 = r1.durationLayout;
        if (r2 == 0) goto L_0x0var_;
    L_0x0b8a:
        r2 = org.telegram.messenger.MediaController.getInstance();
        r3 = r1.currentMessageObject;
        r2 = r2.isPlayingMessage(r3);
        if (r2 == 0) goto L_0x0ba3;
    L_0x0b96:
        r3 = r1.currentMessageObject;
        r3 = r3.type;
        r4 = 5;
        if (r3 != r4) goto L_0x0ba3;
    L_0x0b9d:
        r28.drawRoundProgress(r29);
        r28.drawOverlays(r29);
    L_0x0ba3:
        r3 = r1.currentMessageObject;
        r4 = r3.type;
        r5 = 5;
        if (r4 != r5) goto L_0x0CLASSNAME;
    L_0x0baa:
        r3 = r1.backgroundDrawableLeft;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r3 = r3 + r4;
        r4 = r1.layoutHeight;
        r5 = r1.drawPinnedBottom;
        if (r5 == 0) goto L_0x0bb9;
    L_0x0bb7:
        r5 = 2;
        goto L_0x0bba;
    L_0x0bb9:
        r5 = 0;
    L_0x0bba:
        r5 = 28 - r5;
        r5 = (float) r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r5 = r1.rect;
        r6 = (float) r3;
        r7 = (float) r4;
        r11 = r1.timeWidthAudio;
        r11 = r11 + r3;
        r12 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r11 = r11 + r12;
        r11 = (float) r11;
        r12 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r14 = r14 + r4;
        r12 = (float) r14;
        r5.set(r6, r7, r11, r12);
        r5 = org.telegram.ui.ActionBar.Theme.chat_actionBackgroundPaint;
        r5 = r5.getAlpha();
        r6 = org.telegram.ui.ActionBar.Theme.chat_actionBackgroundPaint;
        r7 = (float) r5;
        r11 = r1.timeAlpha;
        r7 = r7 * r11;
        r7 = (int) r7;
        r6.setAlpha(r7);
        r6 = r1.rect;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r7 = (float) r7;
        r11 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r11 = (float) r11;
        r12 = org.telegram.ui.ActionBar.Theme.chat_actionBackgroundPaint;
        r8.drawRoundRect(r6, r7, r11, r12);
        r6 = org.telegram.ui.ActionBar.Theme.chat_actionBackgroundPaint;
        r6.setAlpha(r5);
        if (r2 != 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r5 = r1.currentMessageObject;
        r5 = r5.isContentUnread();
        if (r5 == 0) goto L_0x0CLASSNAME;
    L_0x0c0d:
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r5 = "chat_mediaTimeText";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r2.setColor(r5);
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r5 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r6 = r1.timeAlpha;
        r6 = r6 * r5;
        r5 = (int) r6;
        r2.setAlpha(r5);
        r2 = r1.timeWidthAudio;
        r2 = r2 + r3;
        r5 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r2 = r2 + r5;
        r2 = (float) r2;
        r5 = NUM; // 0x4104cccd float:8.3 double:5.389434135E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = r5 + r4;
        r5 = (float) r5;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r6 = (float) r6;
        r7 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r8.drawCircle(r2, r5, r6, r7);
        goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        if (r2 == 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r2 = org.telegram.messenger.MediaController.getInstance();
        r2 = r2.isMessagePaused();
        if (r2 != 0) goto L_0x0CLASSNAME;
    L_0x0c4f:
        r2 = r1.roundVideoPlayingDrawable;
        r2.start();
        goto L_0x0c5a;
    L_0x0CLASSNAME:
        r2 = r1.roundVideoPlayingDrawable;
        r2.stop();
    L_0x0c5a:
        r2 = r1.roundVideoPlayingDrawable;
        r5 = r1.timeWidthAudio;
        r5 = r5 + r3;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r5 = r5 + r6;
        r6 = NUM; // 0x40133333 float:2.3 double:5.31120626E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r4;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r5, r6);
        r2 = r1.roundVideoPlayingDrawable;
        r2.draw(r8);
    L_0x0CLASSNAME:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r3 = r3 + r2;
        r2 = NUM; // 0x3fd9999a float:1.7 double:5.29255591E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r4 = r4 + r2;
        goto L_0x0cb4;
    L_0x0CLASSNAME:
        r2 = r1.backgroundDrawableLeft;
        r3 = r3.isOutOwner();
        if (r3 != 0) goto L_0x0CLASSNAME;
    L_0x0c8a:
        r3 = r1.drawPinnedBottom;
        if (r3 == 0) goto L_0x0c8f;
    L_0x0c8e:
        goto L_0x0CLASSNAME;
    L_0x0c8f:
        r3 = NUM; // 0x41900000 float:18.0 double:5.43450582E-315;
        goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r3 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
    L_0x0CLASSNAME:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r2;
        r2 = r1.layoutHeight;
        r4 = NUM; // 0x40CLASSNAMEa float:6.3 double:5.370265717E-315;
        r5 = r1.drawPinnedBottom;
        if (r5 == 0) goto L_0x0ca4;
    L_0x0ca2:
        r5 = 2;
        goto L_0x0ca5;
    L_0x0ca4:
        r5 = 0;
    L_0x0ca5:
        r5 = (float) r5;
        r4 = r4 - r5;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 - r4;
        r4 = r1.timeLayout;
        r4 = r4.getHeight();
        r4 = r2 - r4;
    L_0x0cb4:
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r5 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r6 = r1.timeAlpha;
        r6 = r6 * r5;
        r5 = (int) r6;
        r2.setAlpha(r5);
        r29.save();
        r2 = (float) r3;
        r3 = (float) r4;
        r8.translate(r2, r3);
        r2 = r1.durationLayout;
        r2.draw(r8);
        r29.restore();
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r3 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r2.setAlpha(r3);
        goto L_0x0var_;
    L_0x0cd9:
        r3 = 5;
        if (r2 != r3) goto L_0x0e46;
    L_0x0cdc:
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0d31;
    L_0x0ce4:
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r3 = "chat_outAudioTitleText";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0cfa;
    L_0x0cf7:
        r3 = "chat_outAudioPerfomerSelectedText";
        goto L_0x0cfc;
    L_0x0cfa:
        r3 = "chat_outAudioPerfomerText";
    L_0x0cfc:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0d0e;
    L_0x0d0b:
        r3 = "chat_outAudioDurationSelectedText";
        goto L_0x0d10;
    L_0x0d0e:
        r3 = "chat_outAudioDurationText";
    L_0x0d10:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r1.radialProgress;
        r3 = r28.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x0d27;
    L_0x0d1f:
        r3 = r1.buttonPressed;
        if (r3 == 0) goto L_0x0d24;
    L_0x0d23:
        goto L_0x0d27;
    L_0x0d24:
        r3 = "chat_outAudioProgress";
        goto L_0x0d29;
    L_0x0d27:
        r3 = "chat_outAudioSelectedProgress";
    L_0x0d29:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setProgressColor(r3);
        goto L_0x0d7d;
    L_0x0d31:
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r3 = "chat_inAudioTitleText";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0d47;
    L_0x0d44:
        r3 = "chat_inAudioPerfomerSelectedText";
        goto L_0x0d49;
    L_0x0d47:
        r3 = "chat_inAudioPerfomerText";
    L_0x0d49:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0d5b;
    L_0x0d58:
        r3 = "chat_inAudioDurationSelectedText";
        goto L_0x0d5d;
    L_0x0d5b:
        r3 = "chat_inAudioDurationText";
    L_0x0d5d:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r1.radialProgress;
        r3 = r28.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x0d74;
    L_0x0d6c:
        r3 = r1.buttonPressed;
        if (r3 == 0) goto L_0x0d71;
    L_0x0d70:
        goto L_0x0d74;
    L_0x0d71:
        r3 = "chat_inAudioProgress";
        goto L_0x0d76;
    L_0x0d74:
        r3 = "chat_inAudioSelectedProgress";
    L_0x0d76:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setProgressColor(r3);
    L_0x0d7d:
        r2 = r1.radialProgress;
        r2.draw(r8);
        r29.save();
        r2 = r1.timeAudioX;
        r3 = r1.songX;
        r2 = r2 + r3;
        r2 = (float) r2;
        r3 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.namesOffset;
        r3 = r3 + r4;
        r4 = r1.mediaOffsetY;
        r3 = r3 + r4;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.songLayout;
        r2.draw(r8);
        r29.restore();
        r29.save();
        r2 = org.telegram.messenger.MediaController.getInstance();
        r3 = r1.currentMessageObject;
        r2 = r2.isPlayingMessage(r3);
        if (r2 == 0) goto L_0x0dc1;
    L_0x0db2:
        r2 = r1.seekBarX;
        r2 = (float) r2;
        r3 = r1.seekBarY;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.seekBar;
        r2.draw(r8);
        goto L_0x0ddc;
    L_0x0dc1:
        r2 = r1.timeAudioX;
        r3 = r1.performerX;
        r2 = r2 + r3;
        r2 = (float) r2;
        r3 = NUM; // 0x420CLASSNAME float:35.0 double:5.47465589E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.namesOffset;
        r3 = r3 + r4;
        r4 = r1.mediaOffsetY;
        r3 = r3 + r4;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.performerLayout;
        r2.draw(r8);
    L_0x0ddc:
        r29.restore();
        r29.save();
        r2 = r1.timeAudioX;
        r2 = (float) r2;
        r3 = NUM; // 0x42640000 float:57.0 double:5.503149485E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.namesOffset;
        r3 = r3 + r4;
        r4 = r1.mediaOffsetY;
        r3 = r3 + r4;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.durationLayout;
        r2.draw(r8);
        r29.restore();
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0e11;
    L_0x0e05:
        r2 = r28.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x0e0e;
    L_0x0e0b:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuSelectedDrawable;
        goto L_0x0e1c;
    L_0x0e0e:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuDrawable;
        goto L_0x0e1c;
    L_0x0e11:
        r2 = r28.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x0e1a;
    L_0x0e17:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuSelectedDrawable;
        goto L_0x0e1c;
    L_0x0e1a:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuDrawable;
    L_0x0e1c:
        r3 = r1.buttonX;
        r4 = r1.backgroundWidth;
        r3 = r3 + r4;
        r4 = r1.currentMessageObject;
        r4 = r4.type;
        if (r4 != 0) goto L_0x0e2a;
    L_0x0e27:
        r4 = NUM; // 0x42680000 float:58.0 double:5.50444465E-315;
        goto L_0x0e2c;
    L_0x0e2a:
        r4 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
    L_0x0e2c:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r1.otherX = r3;
        r4 = r1.buttonY;
        r5 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r1.otherY = r4;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r3, r4);
        r2.draw(r8);
        goto L_0x0var_;
    L_0x0e46:
        r3 = 3;
        if (r2 != r3) goto L_0x0var_;
    L_0x0e49:
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0e7f;
    L_0x0e51:
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0e5c;
    L_0x0e59:
        r3 = "chat_outAudioDurationSelectedText";
        goto L_0x0e5e;
    L_0x0e5c:
        r3 = "chat_outAudioDurationText";
    L_0x0e5e:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r1.radialProgress;
        r3 = r28.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x0e75;
    L_0x0e6d:
        r3 = r1.buttonPressed;
        if (r3 == 0) goto L_0x0e72;
    L_0x0e71:
        goto L_0x0e75;
    L_0x0e72:
        r3 = "chat_outAudioProgress";
        goto L_0x0e77;
    L_0x0e75:
        r3 = "chat_outAudioSelectedProgress";
    L_0x0e77:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setProgressColor(r3);
        goto L_0x0eac;
    L_0x0e7f:
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0e8a;
    L_0x0e87:
        r3 = "chat_inAudioDurationSelectedText";
        goto L_0x0e8c;
    L_0x0e8a:
        r3 = "chat_inAudioDurationText";
    L_0x0e8c:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r1.radialProgress;
        r3 = r28.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x0ea3;
    L_0x0e9b:
        r3 = r1.buttonPressed;
        if (r3 == 0) goto L_0x0ea0;
    L_0x0e9f:
        goto L_0x0ea3;
    L_0x0ea0:
        r3 = "chat_inAudioProgress";
        goto L_0x0ea5;
    L_0x0ea3:
        r3 = "chat_inAudioSelectedProgress";
    L_0x0ea5:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setProgressColor(r3);
    L_0x0eac:
        r2 = r1.radialProgress;
        r2.draw(r8);
        r29.save();
        r2 = r1.useSeekBarWaweform;
        if (r2 == 0) goto L_0x0ece;
    L_0x0eb8:
        r2 = r1.seekBarX;
        r3 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r2 = (float) r2;
        r3 = r1.seekBarY;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.seekBarWaveform;
        r2.draw(r8);
        goto L_0x0edc;
    L_0x0ece:
        r2 = r1.seekBarX;
        r2 = (float) r2;
        r3 = r1.seekBarY;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.seekBar;
        r2.draw(r8);
    L_0x0edc:
        r29.restore();
        r29.save();
        r2 = r1.timeAudioX;
        r2 = (float) r2;
        r3 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.namesOffset;
        r3 = r3 + r4;
        r4 = r1.mediaOffsetY;
        r3 = r3 + r4;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.durationLayout;
        r2.draw(r8);
        r29.restore();
        r2 = r1.currentMessageObject;
        r3 = r2.type;
        if (r3 == 0) goto L_0x0var_;
    L_0x0var_:
        r2 = r2.isContentUnread();
        if (r2 == 0) goto L_0x0var_;
    L_0x0var_:
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x0var_;
    L_0x0var_:
        r3 = "chat_outVoiceSeekbarFill";
        goto L_0x0var_;
    L_0x0var_:
        r3 = "chat_inVoiceSeekbarFill";
    L_0x0var_:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r1.timeAudioX;
        r3 = r1.timeWidthAudio;
        r2 = r2 + r3;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r2 = r2 + r3;
        r2 = (float) r2;
        r3 = NUM; // 0x424CLASSNAME float:51.0 double:5.495378504E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r1.namesOffset;
        r3 = r3 + r4;
        r4 = r1.mediaOffsetY;
        r3 = r3 + r4;
        r3 = (float) r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r4 = (float) r4;
        r5 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r8.drawCircle(r2, r3, r4, r5);
    L_0x0var_:
        r2 = r1.captionLayout;
        if (r2 == 0) goto L_0x0ff4;
    L_0x0var_:
        r2 = r1.currentMessageObject;
        r3 = r2.type;
        if (r3 == r10) goto L_0x0fcb;
    L_0x0f4b:
        r4 = r1.documentAttachType;
        r5 = 4;
        if (r4 == r5) goto L_0x0fcb;
    L_0x0var_:
        r4 = 8;
        if (r3 != r4) goto L_0x0var_;
    L_0x0var_:
        goto L_0x0fcb;
    L_0x0var_:
        r3 = r1.hasOldCaptionPreview;
        if (r3 == 0) goto L_0x0var_;
    L_0x0f5a:
        r3 = r1.backgroundDrawableLeft;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0var_;
    L_0x0var_:
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        goto L_0x0var_;
    L_0x0var_:
        r2 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
    L_0x0var_:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 + r2;
        r2 = r1.captionOffsetX;
        r3 = r3 + r2;
        r1.captionX = r3;
        r2 = r1.totalHeight;
        r3 = r1.captionHeight;
        r2 = r2 - r3;
        r3 = r1.drawPinnedTop;
        if (r3 == 0) goto L_0x0f7d;
    L_0x0f7a:
        r3 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        goto L_0x0f7f;
    L_0x0f7d:
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
    L_0x0f7f:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = r1.linkPreviewHeight;
        r2 = r2 - r3;
        r12 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r2 = r2 - r3;
        r1.captionY = r2;
        goto L_0x0ff6;
    L_0x0var_:
        r12 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r3 = r1.backgroundDrawableLeft;
        r2 = r2.isOutOwner();
        if (r2 != 0) goto L_0x0fa9;
    L_0x0f9b:
        r2 = r1.mediaBackground;
        if (r2 != 0) goto L_0x0fa9;
    L_0x0f9f:
        if (r2 != 0) goto L_0x0fa6;
    L_0x0fa1:
        r2 = r1.drawPinnedBottom;
        if (r2 == 0) goto L_0x0fa6;
    L_0x0fa5:
        goto L_0x0fa9;
    L_0x0fa6:
        r2 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        goto L_0x0fab;
    L_0x0fa9:
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
    L_0x0fab:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 + r2;
        r2 = r1.captionOffsetX;
        r3 = r3 + r2;
        r1.captionX = r3;
        r2 = r1.totalHeight;
        r3 = r1.captionHeight;
        r2 = r2 - r3;
        r3 = r1.drawPinnedTop;
        if (r3 == 0) goto L_0x0fc1;
    L_0x0fbe:
        r3 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        goto L_0x0fc3;
    L_0x0fc1:
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
    L_0x0fc3:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r1.captionY = r2;
        goto L_0x0ff6;
    L_0x0fcb:
        r12 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r3 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r3 = r1.captionOffsetX;
        r2 = r2 + r3;
        r1.captionX = r2;
        r2 = r1.photoImage;
        r2 = r2.getImageY();
        r3 = r1.photoImage;
        r3 = r3.getImageHeight();
        r2 = r2 + r3;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r2 = r2 + r3;
        r1.captionY = r2;
        goto L_0x0ff6;
    L_0x0ff4:
        r12 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
    L_0x0ff6:
        r2 = r1.currentPosition;
        if (r2 != 0) goto L_0x0ffd;
    L_0x0ffa:
        r1.drawCaptionLayout(r8, r9);
    L_0x0ffd:
        r2 = r1.hasOldCaptionPreview;
        if (r2 == 0) goto L_0x111f;
    L_0x1001:
        r2 = r1.currentMessageObject;
        r3 = r2.type;
        if (r3 == r10) goto L_0x1022;
    L_0x1007:
        r4 = r1.documentAttachType;
        r5 = 4;
        if (r4 == r5) goto L_0x1022;
    L_0x100c:
        r4 = 8;
        if (r3 != r4) goto L_0x1011;
    L_0x1010:
        goto L_0x1022;
    L_0x1011:
        r3 = r1.backgroundDrawableLeft;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x101a;
    L_0x1019:
        goto L_0x101c;
    L_0x101a:
        r16 = NUM; // 0x41880000 float:17.0 double:5.431915495E-315;
    L_0x101c:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r3 = r3 + r2;
        goto L_0x102f;
    L_0x1022:
        r2 = r1.photoImage;
        r2 = r2.getImageX();
        r3 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r2;
    L_0x102f:
        r11 = r3;
        r2 = r1.totalHeight;
        r3 = r1.drawPinnedTop;
        if (r3 == 0) goto L_0x1039;
    L_0x1036:
        r3 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        goto L_0x103b;
    L_0x1039:
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
    L_0x103b:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = r1.linkPreviewHeight;
        r2 = r2 - r3;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r12 = r2 - r3;
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x1056;
    L_0x1053:
        r3 = "chat_outPreviewLine";
        goto L_0x1058;
    L_0x1056:
        r3 = "chat_inPreviewLine";
    L_0x1058:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r3 = (float) r11;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r12 - r2;
        r4 = (float) r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r11;
        r5 = (float) r2;
        r2 = r1.linkPreviewHeight;
        r2 = r2 + r12;
        r6 = (float) r2;
        r7 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r2 = r29;
        r2.drawRect(r3, r4, r5, r6, r7);
        r2 = r1.siteNameLayout;
        if (r2 == 0) goto L_0x10d1;
    L_0x107c:
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x1089;
    L_0x1086:
        r3 = "chat_outSiteNameText";
        goto L_0x108b;
    L_0x1089:
        r3 = "chat_inSiteNameText";
    L_0x108b:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r29.save();
        r2 = r1.siteNameRtl;
        if (r2 == 0) goto L_0x10a6;
    L_0x1099:
        r2 = r1.backgroundWidth;
        r3 = r1.siteNameWidth;
        r2 = r2 - r3;
        r3 = NUM; // 0x42000000 float:32.0 double:5.4707704E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        goto L_0x10b0;
    L_0x10a6:
        r2 = r1.hasInvoicePreview;
        if (r2 == 0) goto L_0x10ac;
    L_0x10aa:
        r2 = 0;
        goto L_0x10b0;
    L_0x10ac:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
    L_0x10b0:
        r2 = r2 + r11;
        r2 = (float) r2;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r3 = r12 - r3;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.siteNameLayout;
        r2.draw(r8);
        r29.restore();
        r2 = r1.siteNameLayout;
        r3 = r2.getLineCount();
        r3 = r3 - r10;
        r2 = r2.getLineBottom(r3);
        r2 = r2 + r12;
        goto L_0x10d2;
    L_0x10d1:
        r2 = r12;
    L_0x10d2:
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x10e6;
    L_0x10da:
        r3 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r4 = "chat_messageTextOut";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
        goto L_0x10f1;
    L_0x10e6:
        r3 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r4 = "chat_messageTextIn";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
    L_0x10f1:
        r3 = r1.descriptionLayout;
        if (r3 == 0) goto L_0x111d;
    L_0x10f5:
        if (r2 == r12) goto L_0x10fc;
    L_0x10f7:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 + r3;
    L_0x10fc:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r2 - r3;
        r1.descriptionY = r2;
        r29.save();
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r11 = r11 + r2;
        r2 = r1.descriptionX;
        r11 = r11 + r2;
        r2 = (float) r11;
        r3 = r1.descriptionY;
        r3 = (float) r3;
        r8.translate(r2, r3);
        r2 = r1.descriptionLayout;
        r2.draw(r8);
        r29.restore();
    L_0x111d:
        r1.drawTime = r10;
    L_0x111f:
        r2 = r1.documentAttachType;
        if (r2 != r10) goto L_0x144d;
    L_0x1123:
        r2 = r1.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x116a;
    L_0x112b:
        r2 = org.telegram.ui.ActionBar.Theme.chat_docNamePaint;
        r3 = "chat_outFileNameText";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x1141;
    L_0x113e:
        r3 = "chat_outFileInfoSelectedText";
        goto L_0x1143;
    L_0x1141:
        r3 = "chat_outFileInfoText";
    L_0x1143:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x1155;
    L_0x1152:
        r3 = "chat_outFileBackgroundSelected";
        goto L_0x1157;
    L_0x1155:
        r3 = "chat_outFileBackground";
    L_0x1157:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r28.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x1167;
    L_0x1164:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuSelectedDrawable;
        goto L_0x11a8;
    L_0x1167:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuDrawable;
        goto L_0x11a8;
    L_0x116a:
        r2 = org.telegram.ui.ActionBar.Theme.chat_docNamePaint;
        r3 = "chat_inFileNameText";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x1180;
    L_0x117d:
        r3 = "chat_inFileInfoSelectedText";
        goto L_0x1182;
    L_0x1180:
        r3 = "chat_inFileInfoText";
    L_0x1182:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r3 = r28.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x1194;
    L_0x1191:
        r3 = "chat_inFileBackgroundSelected";
        goto L_0x1196;
    L_0x1194:
        r3 = "chat_inFileBackground";
    L_0x1196:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setColor(r3);
        r2 = r28.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x11a6;
    L_0x11a3:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuSelectedDrawable;
        goto L_0x11a8;
    L_0x11a6:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuDrawable;
    L_0x11a8:
        r3 = r1.drawPhotoImage;
        if (r3 == 0) goto L_0x134a;
    L_0x11ac:
        r3 = r1.currentMessageObject;
        r3 = r3.type;
        if (r3 != 0) goto L_0x11d5;
    L_0x11b2:
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r4 = r1.backgroundWidth;
        r3 = r3 + r4;
        r4 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r1.otherX = r3;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r4 = r4 + r5;
        r1.otherY = r4;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r3, r4);
        goto L_0x11f7;
    L_0x11d5:
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r4 = r1.backgroundWidth;
        r3 = r3 + r4;
        r4 = NUM; // 0x42200000 float:40.0 double:5.481131706E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r1.otherX = r3;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r4 = r4 + r5;
        r1.otherY = r4;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r3, r4);
    L_0x11f7:
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r3 = r3 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r3 = r3 + r4;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r5 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r4 = r4 + r5;
        r5 = r1.photoImage;
        r5 = r5.getImageY();
        r6 = r1.docTitleLayout;
        if (r6 == 0) goto L_0x122f;
    L_0x121e:
        r7 = r6.getLineCount();
        r7 = r7 - r10;
        r6 = r6.getLineBottom(r7);
        r7 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r6 = r6 + r7;
        goto L_0x1233;
    L_0x122f:
        r6 = org.telegram.messenger.AndroidUtilities.dp(r18);
    L_0x1233:
        r5 = r5 + r6;
        if (r0 != 0) goto L_0x1305;
    L_0x1236:
        r0 = r1.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x1281;
    L_0x123e:
        r0 = r1.radialProgress;
        r6 = "chat_outLoader";
        r7 = "chat_outLoaderSelected";
        r11 = "chat_outMediaIcon";
        r12 = "chat_outMediaIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.radialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 == 0) goto L_0x1256;
    L_0x1253:
        r6 = "chat_outFileProgressSelected";
        goto L_0x1258;
    L_0x1256:
        r6 = "chat_outFileProgress";
    L_0x1258:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.videoRadialProgress;
        r6 = "chat_outLoader";
        r7 = "chat_outLoaderSelected";
        r11 = "chat_outMediaIcon";
        r12 = "chat_outMediaIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.videoRadialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 == 0) goto L_0x1277;
    L_0x1274:
        r6 = "chat_outFileProgressSelected";
        goto L_0x1279;
    L_0x1277:
        r6 = "chat_outFileProgress";
    L_0x1279:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        goto L_0x12c3;
    L_0x1281:
        r0 = r1.radialProgress;
        r6 = "chat_inLoader";
        r7 = "chat_inLoaderSelected";
        r11 = "chat_inMediaIcon";
        r12 = "chat_inMediaIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.radialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 == 0) goto L_0x1299;
    L_0x1296:
        r6 = "chat_inFileProgressSelected";
        goto L_0x129b;
    L_0x1299:
        r6 = "chat_inFileProgress";
    L_0x129b:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.videoRadialProgress;
        r6 = "chat_inLoader";
        r7 = "chat_inLoaderSelected";
        r11 = "chat_inMediaIcon";
        r12 = "chat_inMediaIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.videoRadialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 == 0) goto L_0x12ba;
    L_0x12b7:
        r6 = "chat_inFileProgressSelected";
        goto L_0x12bc;
    L_0x12ba:
        r6 = "chat_inFileProgress";
    L_0x12bc:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
    L_0x12c3:
        r0 = r1.rect;
        r6 = r1.photoImage;
        r6 = r6.getImageX();
        r6 = (float) r6;
        r7 = r1.photoImage;
        r7 = r7.getImageY();
        r7 = (float) r7;
        r11 = r1.photoImage;
        r11 = r11.getImageX();
        r12 = r1.photoImage;
        r12 = r12.getImageWidth();
        r11 = r11 + r12;
        r11 = (float) r11;
        r12 = r1.photoImage;
        r12 = r12.getImageY();
        r14 = r1.photoImage;
        r14 = r14.getImageHeight();
        r12 = r12 + r14;
        r12 = (float) r12;
        r0.set(r6, r7, r11, r12);
        r0 = r1.rect;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r6 = (float) r6;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r7 = (float) r7;
        r11 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r8.drawRoundRect(r0, r6, r7, r11);
        goto L_0x1415;
    L_0x1305:
        r0 = r1.radialProgress;
        r6 = "chat_mediaLoaderPhoto";
        r7 = "chat_mediaLoaderPhotoSelected";
        r11 = "chat_mediaLoaderPhotoIcon";
        r12 = "chat_mediaLoaderPhotoIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.radialProgress;
        r6 = "chat_mediaProgress";
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.videoRadialProgress;
        r6 = "chat_mediaLoaderPhoto";
        r7 = "chat_mediaLoaderPhotoSelected";
        r11 = "chat_mediaLoaderPhotoIcon";
        r12 = "chat_mediaLoaderPhotoIconSelected";
        r0.setColors(r6, r7, r11, r12);
        r0 = r1.videoRadialProgress;
        r6 = "chat_mediaProgress";
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.buttonState;
        r6 = -1;
        if (r0 != r6) goto L_0x1415;
    L_0x133a:
        r0 = r1.radialProgress;
        r0 = r0.getIcon();
        r6 = 4;
        if (r0 == r6) goto L_0x1415;
    L_0x1343:
        r0 = r1.radialProgress;
        r0.setIcon(r6, r10, r10);
        goto L_0x1415;
    L_0x134a:
        r0 = r1.buttonX;
        r3 = r1.backgroundWidth;
        r0 = r0 + r3;
        r3 = r1.currentMessageObject;
        r3 = r3.type;
        if (r3 != 0) goto L_0x1358;
    L_0x1355:
        r3 = NUM; // 0x42680000 float:58.0 double:5.50444465E-315;
        goto L_0x135a;
    L_0x1358:
        r3 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
    L_0x135a:
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r3;
        r1.otherX = r0;
        r3 = r1.buttonY;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r1.otherY = r3;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r0, r3);
        r0 = r1.buttonX;
        r3 = NUM; // 0x42540000 float:53.0 double:5.49796883E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r0;
        r0 = r1.buttonY;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r4 = r4 + r0;
        r0 = r1.buttonY;
        r5 = NUM; // 0x41d80000 float:27.0 double:5.457818764E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r0 = r0 + r5;
        r5 = r1.docTitleLayout;
        if (r5 == 0) goto L_0x13a7;
    L_0x138c:
        r5 = r5.getLineCount();
        if (r5 <= r10) goto L_0x13a7;
    L_0x1392:
        r5 = r1.docTitleLayout;
        r5 = r5.getLineCount();
        r5 = r5 - r10;
        r6 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 * r6;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r5 = r5 + r6;
        r0 = r0 + r5;
    L_0x13a7:
        r5 = r0;
        r0 = r1.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x13e3;
    L_0x13b0:
        r0 = r1.radialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 != 0) goto L_0x13c0;
    L_0x13b8:
        r6 = r1.buttonPressed;
        if (r6 == 0) goto L_0x13bd;
    L_0x13bc:
        goto L_0x13c0;
    L_0x13bd:
        r6 = "chat_outAudioProgress";
        goto L_0x13c2;
    L_0x13c0:
        r6 = "chat_outAudioSelectedProgress";
    L_0x13c2:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.videoRadialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 != 0) goto L_0x13d9;
    L_0x13d1:
        r6 = r1.videoButtonPressed;
        if (r6 == 0) goto L_0x13d6;
    L_0x13d5:
        goto L_0x13d9;
    L_0x13d6:
        r6 = "chat_outAudioProgress";
        goto L_0x13db;
    L_0x13d9:
        r6 = "chat_outAudioSelectedProgress";
    L_0x13db:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        goto L_0x1415;
    L_0x13e3:
        r0 = r1.radialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 != 0) goto L_0x13f3;
    L_0x13eb:
        r6 = r1.buttonPressed;
        if (r6 == 0) goto L_0x13f0;
    L_0x13ef:
        goto L_0x13f3;
    L_0x13f0:
        r6 = "chat_inAudioProgress";
        goto L_0x13f5;
    L_0x13f3:
        r6 = "chat_inAudioSelectedProgress";
    L_0x13f5:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
        r0 = r1.videoRadialProgress;
        r6 = r28.isDrawSelectionBackground();
        if (r6 != 0) goto L_0x140c;
    L_0x1404:
        r6 = r1.videoButtonPressed;
        if (r6 == 0) goto L_0x1409;
    L_0x1408:
        goto L_0x140c;
    L_0x1409:
        r6 = "chat_inAudioProgress";
        goto L_0x140e;
    L_0x140c:
        r6 = "chat_inAudioSelectedProgress";
    L_0x140e:
        r6 = org.telegram.ui.ActionBar.Theme.getColor(r6);
        r0.setProgressColor(r6);
    L_0x1415:
        r2.draw(r8);
        r0 = r1.docTitleLayout;	 Catch:{ Exception -> 0x1430 }
        if (r0 == 0) goto L_0x1434;
    L_0x141c:
        r29.save();	 Catch:{ Exception -> 0x1430 }
        r0 = r1.docTitleOffsetX;	 Catch:{ Exception -> 0x1430 }
        r0 = r0 + r3;
        r0 = (float) r0;	 Catch:{ Exception -> 0x1430 }
        r2 = (float) r4;	 Catch:{ Exception -> 0x1430 }
        r8.translate(r0, r2);	 Catch:{ Exception -> 0x1430 }
        r0 = r1.docTitleLayout;	 Catch:{ Exception -> 0x1430 }
        r0.draw(r8);	 Catch:{ Exception -> 0x1430 }
        r29.restore();	 Catch:{ Exception -> 0x1430 }
        goto L_0x1434;
    L_0x1430:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x1434:
        r0 = r1.infoLayout;	 Catch:{ Exception -> 0x1449 }
        if (r0 == 0) goto L_0x144d;
    L_0x1438:
        r29.save();	 Catch:{ Exception -> 0x1449 }
        r0 = (float) r3;	 Catch:{ Exception -> 0x1449 }
        r2 = (float) r5;	 Catch:{ Exception -> 0x1449 }
        r8.translate(r0, r2);	 Catch:{ Exception -> 0x1449 }
        r0 = r1.infoLayout;	 Catch:{ Exception -> 0x1449 }
        r0.draw(r8);	 Catch:{ Exception -> 0x1449 }
        r29.restore();	 Catch:{ Exception -> 0x1449 }
        goto L_0x144d;
    L_0x1449:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x144d:
        r0 = r1.buttonState;
        r2 = -1;
        if (r0 != r2) goto L_0x14ef;
    L_0x1452:
        r0 = r1.currentMessageObject;
        r0 = r0.needDrawBluredPreview();
        if (r0 == 0) goto L_0x14ef;
    L_0x145a:
        r0 = org.telegram.messenger.MediaController.getInstance();
        r2 = r1.currentMessageObject;
        r0 = r0.isPlayingMessage(r2);
        if (r0 != 0) goto L_0x14ef;
    L_0x1466:
        r0 = r1.photoImage;
        r0 = r0.getVisible();
        if (r0 == 0) goto L_0x14ef;
    L_0x146e:
        r0 = r1.currentMessageObject;
        r2 = r0.messageOwner;
        r2 = r2.destroyTime;
        if (r2 == 0) goto L_0x14ef;
    L_0x1476:
        r0 = r0.isOutOwner();
        if (r0 != 0) goto L_0x14ea;
    L_0x147c:
        r2 = java.lang.System.currentTimeMillis();
        r0 = r1.currentAccount;
        r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r0);
        r0 = r0.getTimeDifference();
        r0 = r0 * 1000;
        r4 = (long) r0;
        r2 = r2 + r4;
        r4 = 0;
        r0 = r1.currentMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.destroyTime;
        r6 = (long) r0;
        r11 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r6 = r6 * r11;
        r6 = r6 - r2;
        r2 = java.lang.Math.max(r4, r6);
        r0 = (float) r2;
        r2 = r1.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.ttl;
        r2 = (float) r2;
        r3 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = r2 * r3;
        r0 = r0 / r2;
        r2 = org.telegram.ui.ActionBar.Theme.chat_deleteProgressPaint;
        r3 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r4 = r1.controlsAlpha;
        r4 = r4 * r3;
        r3 = (int) r4;
        r2.setAlpha(r3);
        r3 = r1.deleteProgressRect;
        r4 = -NUM; // 0xffffffffc2b40000 float:-90.0 double:NaN;
        r2 = -NUM; // 0xffffffffc3b40000 float:-360.0 double:NaN;
        r5 = r0 * r2;
        r6 = 1;
        r7 = org.telegram.ui.ActionBar.Theme.chat_deleteProgressPaint;
        r2 = r29;
        r2.drawArc(r3, r4, r5, r6, r7);
        r2 = 0;
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 == 0) goto L_0x14ea;
    L_0x14ce:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r1.deleteProgressRect;
        r3 = r2.left;
        r3 = (int) r3;
        r3 = r3 - r0;
        r4 = r2.top;
        r4 = (int) r4;
        r4 = r4 - r0;
        r5 = r2.right;
        r5 = (int) r5;
        r6 = 2;
        r0 = r0 * 2;
        r5 = r5 + r0;
        r2 = r2.bottom;
        r2 = (int) r2;
        r2 = r2 + r0;
        r1.invalidate(r3, r4, r5, r2);
    L_0x14ea:
        r0 = r1.currentMessageObject;
        r1.updateSecretTimeText(r0);
    L_0x14ef:
        r0 = r1.currentMessageObject;
        r2 = r0.type;
        r3 = 4;
        if (r2 != r3) goto L_0x1560;
    L_0x14f6:
        r0 = r0.messageOwner;
        r0 = r0.media;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
        if (r0 != 0) goto L_0x1560;
    L_0x14fe:
        r0 = r1.currentMapProvider;
        r2 = 2;
        if (r0 != r2) goto L_0x1560;
    L_0x1503:
        r0 = r1.photoImage;
        r0 = r0.hasNotThumb();
        if (r0 == 0) goto L_0x1560;
    L_0x150b:
        r0 = org.telegram.ui.ActionBar.Theme.chat_redLocationIcon;
        r0 = r0.getIntrinsicWidth();
        r0 = (float) r0;
        r2 = NUM; // 0x3f4ccccd float:0.8 double:5.246966156E-315;
        r0 = r0 * r2;
        r0 = (int) r0;
        r2 = org.telegram.ui.ActionBar.Theme.chat_redLocationIcon;
        r2 = r2.getIntrinsicHeight();
        r2 = (float) r2;
        r3 = NUM; // 0x3f4ccccd float:0.8 double:5.246966156E-315;
        r2 = r2 * r3;
        r2 = (int) r2;
        r3 = r1.photoImage;
        r3 = r3.getImageX();
        r4 = r1.photoImage;
        r4 = r4.getImageWidth();
        r4 = r4 - r0;
        r5 = 2;
        r4 = r4 / r5;
        r3 = r3 + r4;
        r4 = r1.photoImage;
        r4 = r4.getImageY();
        r6 = r1.photoImage;
        r6 = r6.getImageHeight();
        r6 = r6 / r5;
        r6 = r6 - r2;
        r4 = r4 + r6;
        r5 = org.telegram.ui.ActionBar.Theme.chat_redLocationIcon;
        r6 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r7 = r1.photoImage;
        r7 = r7.getCurrentAlpha();
        r7 = r7 * r6;
        r6 = (int) r7;
        r5.setAlpha(r6);
        r5 = org.telegram.ui.ActionBar.Theme.chat_redLocationIcon;
        r0 = r0 + r3;
        r2 = r2 + r4;
        r5.setBounds(r3, r4, r0, r2);
        r0 = org.telegram.ui.ActionBar.Theme.chat_redLocationIcon;
        r0.draw(r8);
    L_0x1560:
        r0 = r1.botButtons;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x17d5;
    L_0x1568:
        r0 = r1.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x157d;
    L_0x1570:
        r0 = r28.getMeasuredWidth();
        r2 = r1.widthForButtons;
        r0 = r0 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r0 = r0 - r2;
        goto L_0x1592;
    L_0x157d:
        r0 = r1.backgroundDrawableLeft;
        r2 = r1.mediaBackground;
        if (r2 != 0) goto L_0x158b;
    L_0x1583:
        r2 = r1.drawPinnedBottom;
        if (r2 == 0) goto L_0x1588;
    L_0x1587:
        goto L_0x158b;
    L_0x1588:
        r2 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        goto L_0x158d;
    L_0x158b:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x158d:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
    L_0x1592:
        r11 = 0;
    L_0x1593:
        r2 = r1.botButtons;
        r2 = r2.size();
        if (r11 >= r2) goto L_0x17d5;
    L_0x159b:
        r2 = r1.botButtons;
        r2 = r2.get(r11);
        r12 = r2;
        r12 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r12;
        r2 = r12.y;
        r3 = r1.layoutHeight;
        r2 = r2 + r3;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r19);
        r2 = r2 - r3;
        r3 = org.telegram.ui.ActionBar.Theme.chat_systemDrawable;
        r4 = r1.pressedBotButton;
        if (r11 != r4) goto L_0x15b9;
    L_0x15b6:
        r4 = org.telegram.ui.ActionBar.Theme.colorPressedFilter;
        goto L_0x15bb;
    L_0x15b9:
        r4 = org.telegram.ui.ActionBar.Theme.colorFilter;
    L_0x15bb:
        r3.setColorFilter(r4);
        r3 = org.telegram.ui.ActionBar.Theme.chat_systemDrawable;
        r4 = r12.x;
        r4 = r4 + r0;
        r5 = r12.x;
        r5 = r5 + r0;
        r6 = r12.width;
        r5 = r5 + r6;
        r6 = r12.height;
        r6 = r6 + r2;
        r3.setBounds(r4, r2, r5, r6);
        r3 = org.telegram.ui.ActionBar.Theme.chat_systemDrawable;
        r3.draw(r8);
        r29.save();
        r3 = r12.x;
        r3 = r3 + r0;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r3 = (float) r3;
        r4 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = r12.title;
        r6 = r12.title;
        r6 = r6.getLineCount();
        r6 = r6 - r10;
        r5 = r5.getLineBottom(r6);
        r4 = r4 - r5;
        r14 = 2;
        r4 = r4 / r14;
        r4 = r4 + r2;
        r4 = (float) r4;
        r8.translate(r3, r4);
        r3 = r12.title;
        r3.draw(r8);
        r29.restore();
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonUrl;
        if (r3 == 0) goto L_0x1645;
    L_0x161d:
        r3 = r12.x;
        r4 = r12.width;
        r3 = r3 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r3 = r3 - r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_botLinkDrawalbe;
        r4 = r4.getIntrinsicWidth();
        r3 = r3 - r4;
        r3 = r3 + r0;
        r4 = org.telegram.ui.ActionBar.Theme.chat_botLinkDrawalbe;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r2 + r5;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r4, r3, r2);
        r2 = org.telegram.ui.ActionBar.Theme.chat_botLinkDrawalbe;
        r2.draw(r8);
    L_0x1642:
        r7 = 0;
        goto L_0x17d1;
    L_0x1645:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonSwitchInline;
        if (r3 == 0) goto L_0x1673;
    L_0x164d:
        r3 = r12.x;
        r4 = r12.width;
        r3 = r3 + r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r3 = r3 - r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_botInlineDrawable;
        r4 = r4.getIntrinsicWidth();
        r3 = r3 - r4;
        r3 = r3 + r0;
        r4 = org.telegram.ui.ActionBar.Theme.chat_botInlineDrawable;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r21);
        r2 = r2 + r5;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r4, r3, r2);
        r2 = org.telegram.ui.ActionBar.Theme.chat_botInlineDrawable;
        r2.draw(r8);
        goto L_0x1642;
    L_0x1673:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
        if (r3 != 0) goto L_0x169b;
    L_0x167b:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
        if (r3 != 0) goto L_0x169b;
    L_0x1683:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
        if (r3 != 0) goto L_0x169b;
    L_0x168b:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
        if (r3 != 0) goto L_0x169b;
    L_0x1693:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonUrlAuth;
        if (r3 == 0) goto L_0x1642;
    L_0x169b:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
        if (r3 != 0) goto L_0x16bb;
    L_0x16a3:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
        if (r3 != 0) goto L_0x16bb;
    L_0x16ab:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
        if (r3 != 0) goto L_0x16bb;
    L_0x16b3:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonUrlAuth;
        if (r3 == 0) goto L_0x16cd;
    L_0x16bb:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.SendMessagesHelper.getInstance(r3);
        r4 = r1.currentMessageObject;
        r5 = r12.button;
        r3 = r3.isSendingCallback(r4, r5);
        if (r3 != 0) goto L_0x16ea;
    L_0x16cd:
        r3 = r12.button;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
        if (r3 == 0) goto L_0x16e8;
    L_0x16d5:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.SendMessagesHelper.getInstance(r3);
        r4 = r1.currentMessageObject;
        r5 = r12.button;
        r3 = r3.isSendingCurrentLocation(r4, r5);
        if (r3 == 0) goto L_0x16e8;
    L_0x16e7:
        goto L_0x16ea;
    L_0x16e8:
        r15 = 0;
        goto L_0x16eb;
    L_0x16ea:
        r15 = 1;
    L_0x16eb:
        if (r15 != 0) goto L_0x16f8;
    L_0x16ed:
        if (r15 != 0) goto L_0x1642;
    L_0x16ef:
        r3 = r12.progressAlpha;
        r4 = 0;
        r3 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1));
        if (r3 == 0) goto L_0x1642;
    L_0x16f8:
        r3 = org.telegram.ui.ActionBar.Theme.chat_botProgressPaint;
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r5 = r12.progressAlpha;
        r6 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r5 = r5 * r6;
        r5 = (int) r5;
        r4 = java.lang.Math.min(r4, r5);
        r3.setAlpha(r4);
        r3 = r12.x;
        r4 = r12.width;
        r3 = r3 + r4;
        r4 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r3 = r3 + r0;
        r4 = r1.rect;
        r5 = (float) r3;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r22);
        r6 = r6 + r2;
        r6 = (float) r6;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r3 = r3 + r7;
        r3 = (float) r3;
        r7 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r2 = r2 + r7;
        r2 = (float) r2;
        r4.set(r5, r6, r3, r2);
        r3 = r1.rect;
        r2 = r12.angle;
        r4 = (float) r2;
        r5 = NUM; // 0x435CLASSNAME float:220.0 double:5.58344962E-315;
        r6 = 0;
        r7 = org.telegram.ui.ActionBar.Theme.chat_botProgressPaint;
        r2 = r29;
        r2.drawArc(r3, r4, r5, r6, r7);
        r28.invalidate();
        r2 = java.lang.System.currentTimeMillis();
        r4 = r12.lastUpdateTime;
        r6 = java.lang.System.currentTimeMillis();
        r4 = r4 - r6;
        r4 = java.lang.Math.abs(r4);
        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r16 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r16 >= 0) goto L_0x17cd;
    L_0x1762:
        r4 = r12.lastUpdateTime;
        r4 = r2 - r4;
        r6 = 360; // 0x168 float:5.04E-43 double:1.78E-321;
        r6 = r6 * r4;
        r6 = (float) r6;
        r7 = NUM; // 0x44fa0000 float:2000.0 double:5.717499035E-315;
        r6 = r6 / r7;
        r7 = r12.angle;
        r7 = (float) r7;
        r7 = r7 + r6;
        r6 = (int) r7;
        r12.angle = r6;
        r6 = r12.angle;
        r7 = r12.angle;
        r7 = r7 / 360;
        r7 = r7 * 360;
        r6 = r6 - r7;
        r12.angle = r6;
        if (r15 == 0) goto L_0x17ac;
    L_0x178c:
        r6 = r12.progressAlpha;
        r6 = (r6 > r13 ? 1 : (r6 == r13 ? 0 : -1));
        if (r6 >= 0) goto L_0x17cd;
    L_0x1794:
        r6 = r12.progressAlpha;
        r4 = (float) r4;
        r5 = NUM; // 0x43480000 float:200.0 double:5.5769738E-315;
        r4 = r4 / r5;
        r6 = r6 + r4;
        r12.progressAlpha = r6;
        r4 = r12.progressAlpha;
        r4 = (r4 > r13 ? 1 : (r4 == r13 ? 0 : -1));
        if (r4 <= 0) goto L_0x17cd;
    L_0x17a8:
        r12.progressAlpha = r13;
        goto L_0x17cd;
    L_0x17ac:
        r6 = r12.progressAlpha;
        r7 = 0;
        r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r6 <= 0) goto L_0x17ce;
    L_0x17b5:
        r6 = r12.progressAlpha;
        r4 = (float) r4;
        r5 = NUM; // 0x43480000 float:200.0 double:5.5769738E-315;
        r4 = r4 / r5;
        r6 = r6 - r4;
        r12.progressAlpha = r6;
        r4 = r12.progressAlpha;
        r4 = (r4 > r7 ? 1 : (r4 == r7 ? 0 : -1));
        if (r4 >= 0) goto L_0x17ce;
    L_0x17c9:
        r12.progressAlpha = r7;
        goto L_0x17ce;
    L_0x17cd:
        r7 = 0;
    L_0x17ce:
        r12.lastUpdateTime = r2;
    L_0x17d1:
        r11 = r11 + 1;
        goto L_0x1593;
    L_0x17d5:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.drawContent(android.graphics.Canvas):void");
    }

    private int getMiniIconForCurrentState() {
        int i = this.miniButtonState;
        if (i < 0) {
            return 4;
        }
        return i == 0 ? 2 : 3;
    }

    /* JADX WARNING: Missing block: B:47:0x0097, code skipped:
            if (r0.attachPathExists == false) goto L_0x009a;
     */
    private int getIconForCurrentState() {
        /*
        r15 = this;
        r0 = r15.documentAttachType;
        r1 = "chat_outMediaIconSelected";
        r2 = "chat_outMediaIcon";
        r3 = "chat_outLoaderSelected";
        r4 = "chat_outLoader";
        r5 = "chat_inMediaIconSelected";
        r6 = "chat_inMediaIcon";
        r7 = "chat_inLoaderSelected";
        r8 = "chat_inLoader";
        r9 = 0;
        r10 = 4;
        r11 = 2;
        r12 = 3;
        r13 = 1;
        if (r0 == r12) goto L_0x00c0;
    L_0x0019:
        r14 = 5;
        if (r0 != r14) goto L_0x001e;
    L_0x001c:
        goto L_0x00c0;
    L_0x001e:
        if (r0 != r13) goto L_0x0043;
    L_0x0020:
        r0 = r15.drawPhotoImage;
        if (r0 != 0) goto L_0x0043;
    L_0x0024:
        r0 = r15.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x0032;
    L_0x002c:
        r0 = r15.radialProgress;
        r0.setColors(r4, r3, r2, r1);
        goto L_0x0037;
    L_0x0032:
        r0 = r15.radialProgress;
        r0.setColors(r8, r7, r6, r5);
    L_0x0037:
        r0 = r15.buttonState;
        r1 = -1;
        if (r0 != r1) goto L_0x003d;
    L_0x003c:
        return r14;
    L_0x003d:
        if (r0 != 0) goto L_0x0040;
    L_0x003f:
        return r11;
    L_0x0040:
        if (r0 != r13) goto L_0x00bf;
    L_0x0042:
        return r12;
    L_0x0043:
        r0 = r15.radialProgress;
        r1 = "chat_mediaLoaderPhoto";
        r2 = "chat_mediaLoaderPhotoSelected";
        r3 = "chat_mediaLoaderPhotoIcon";
        r4 = "chat_mediaLoaderPhotoIconSelected";
        r0.setColors(r1, r2, r3, r4);
        r0 = r15.videoRadialProgress;
        r2 = "chat_mediaLoaderPhotoSelected";
        r3 = "chat_mediaLoaderPhotoIcon";
        r4 = "chat_mediaLoaderPhotoIconSelected";
        r0.setColors(r1, r2, r3, r4);
        r0 = r15.buttonState;
        if (r0 < 0) goto L_0x0072;
    L_0x005f:
        if (r0 >= r10) goto L_0x0072;
    L_0x0061:
        if (r0 != 0) goto L_0x0064;
    L_0x0063:
        return r11;
    L_0x0064:
        if (r0 != r13) goto L_0x0067;
    L_0x0066:
        return r12;
    L_0x0067:
        if (r0 != r11) goto L_0x006a;
    L_0x0069:
        return r9;
    L_0x006a:
        if (r0 != r12) goto L_0x00bf;
    L_0x006c:
        r0 = r15.autoPlayingMedia;
        if (r0 == 0) goto L_0x0071;
    L_0x0070:
        r9 = 4;
    L_0x0071:
        return r9;
    L_0x0072:
        r0 = r15.buttonState;
        r1 = -1;
        if (r0 != r1) goto L_0x00bf;
    L_0x0077:
        r0 = r15.documentAttachType;
        if (r0 != r13) goto L_0x009c;
    L_0x007b:
        r0 = r15.drawPhotoImage;
        if (r0 == 0) goto L_0x009a;
    L_0x007f:
        r0 = r15.currentPhotoObject;
        if (r0 != 0) goto L_0x0087;
    L_0x0083:
        r0 = r15.currentPhotoObjectThumb;
        if (r0 == 0) goto L_0x009a;
    L_0x0087:
        r0 = r15.photoImage;
        r0 = r0.hasBitmapImage();
        if (r0 != 0) goto L_0x009b;
    L_0x008f:
        r0 = r15.currentMessageObject;
        r1 = r0.mediaExists;
        if (r1 != 0) goto L_0x009b;
    L_0x0095:
        r0 = r0.attachPathExists;
        if (r0 == 0) goto L_0x009a;
    L_0x0099:
        goto L_0x009b;
    L_0x009a:
        r10 = 5;
    L_0x009b:
        return r10;
    L_0x009c:
        r0 = r15.currentMessageObject;
        r0 = r0.needDrawBluredPreview();
        if (r0 == 0) goto L_0x00ba;
    L_0x00a4:
        r0 = r15.currentMessageObject;
        r1 = r0.messageOwner;
        r1 = r1.destroyTime;
        if (r1 == 0) goto L_0x00b8;
    L_0x00ac:
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x00b5;
    L_0x00b2:
        r0 = 9;
        return r0;
    L_0x00b5:
        r0 = 11;
        return r0;
    L_0x00b8:
        r0 = 7;
        return r0;
    L_0x00ba:
        r0 = r15.hasEmbed;
        if (r0 == 0) goto L_0x00bf;
    L_0x00be:
        return r9;
    L_0x00bf:
        return r10;
    L_0x00c0:
        r0 = r15.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 == 0) goto L_0x00ce;
    L_0x00c8:
        r0 = r15.radialProgress;
        r0.setColors(r4, r3, r2, r1);
        goto L_0x00d3;
    L_0x00ce:
        r0 = r15.radialProgress;
        r0.setColors(r8, r7, r6, r5);
    L_0x00d3:
        r0 = r15.buttonState;
        if (r0 != r13) goto L_0x00d8;
    L_0x00d7:
        return r13;
    L_0x00d8:
        if (r0 != r11) goto L_0x00db;
    L_0x00da:
        return r11;
    L_0x00db:
        if (r0 != r10) goto L_0x00de;
    L_0x00dd:
        return r12;
    L_0x00de:
        return r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.getIconForCurrentState():int");
    }

    private int getMaxNameWidth() {
        int dp;
        int i = this.documentAttachType;
        if (i == 6 || i == 8 || this.currentMessageObject.type == 5) {
            Point point;
            if (AndroidUtilities.isTablet()) {
                if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.needDrawAvatar()) {
                    i = AndroidUtilities.getMinTabletSide();
                    dp = AndroidUtilities.dp(42.0f);
                } else {
                    i = AndroidUtilities.getMinTabletSide();
                    i -= this.backgroundWidth;
                    dp = AndroidUtilities.dp(57.0f);
                }
            } else if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.needDrawAvatar()) {
                point = AndroidUtilities.displaySize;
                i = Math.min(point.x, point.y);
                dp = AndroidUtilities.dp(42.0f);
            } else {
                point = AndroidUtilities.displaySize;
                i = Math.min(point.x, point.y);
                i -= this.backgroundWidth;
                dp = AndroidUtilities.dp(57.0f);
            }
            i -= dp;
            i -= this.backgroundWidth;
            dp = AndroidUtilities.dp(57.0f);
        } else if (this.currentMessagesGroup != null) {
            if (AndroidUtilities.isTablet()) {
                i = AndroidUtilities.getMinTabletSide();
            } else {
                i = AndroidUtilities.displaySize.x;
            }
            dp = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < this.currentMessagesGroup.posArray.size(); i3++) {
                GroupedMessagePosition groupedMessagePosition = (GroupedMessagePosition) this.currentMessagesGroup.posArray.get(i3);
                if (groupedMessagePosition.minY != (byte) 0) {
                    break;
                }
                double d = (double) i2;
                double ceil = Math.ceil((double) ((((float) (groupedMessagePosition.pw + groupedMessagePosition.leftSpanOffset)) / 1000.0f) * ((float) i)));
                Double.isNaN(d);
                i2 = (int) (d + ceil);
            }
            if (this.isAvatarVisible) {
                dp = 48;
            }
            return i2 - AndroidUtilities.dp((float) (dp + 31));
        } else {
            i = this.backgroundWidth;
            dp = AndroidUtilities.dp(this.mediaBackground ? 22.0f : 31.0f);
        }
        return i - dp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00e3  */
    /* JADX WARNING: Missing block: B:41:0x009c, code skipped:
            if (r5 != 5) goto L_0x009f;
     */
    /* JADX WARNING: Missing block: B:72:0x0109, code skipped:
            if ((r2 & 2) != 0) goto L_0x0116;
     */
    public void updateButtonState(boolean r17, boolean r18, boolean r19) {
        /*
        r16 = this;
        r0 = r16;
        r1 = r17;
        r2 = 0;
        if (r18 == 0) goto L_0x0015;
    L_0x0007:
        r3 = r0.currentMessageObject;
        r3 = org.telegram.ui.PhotoViewer.isShowingImage(r3);
        if (r3 != 0) goto L_0x0013;
    L_0x000f:
        r3 = r0.attachedToWindow;
        if (r3 != 0) goto L_0x0015;
    L_0x0013:
        r3 = 0;
        goto L_0x0017;
    L_0x0015:
        r3 = r18;
    L_0x0017:
        r0.drawRadialCheckBackground = r2;
        r4 = 0;
        r5 = r0.currentMessageObject;
        r5 = r5.type;
        r6 = 9;
        r7 = 8;
        r8 = 5;
        r9 = 3;
        r10 = 7;
        r11 = 4;
        r12 = 1;
        if (r5 != r12) goto L_0x003d;
    L_0x0029:
        r4 = r0.currentPhotoObject;
        if (r4 != 0) goto L_0x0033;
    L_0x002d:
        r2 = r0.radialProgress;
        r2.setIcon(r11, r1, r3);
        return;
    L_0x0033:
        r4 = org.telegram.messenger.FileLoader.getAttachFileName(r4);
        r5 = r0.currentMessageObject;
        r5 = r5.mediaExists;
        goto L_0x00ab;
    L_0x003d:
        if (r5 == r7) goto L_0x0068;
    L_0x003f:
        r13 = r0.documentAttachType;
        if (r13 == r10) goto L_0x0068;
    L_0x0043:
        if (r13 == r11) goto L_0x0068;
    L_0x0045:
        if (r13 == r7) goto L_0x0068;
    L_0x0047:
        if (r5 == r6) goto L_0x0068;
    L_0x0049:
        if (r13 == r9) goto L_0x0068;
    L_0x004b:
        if (r13 != r8) goto L_0x004e;
    L_0x004d:
        goto L_0x0068;
    L_0x004e:
        if (r13 == 0) goto L_0x005b;
    L_0x0050:
        r4 = r0.documentAttach;
        r4 = org.telegram.messenger.FileLoader.getAttachFileName(r4);
        r5 = r0.currentMessageObject;
        r5 = r5.mediaExists;
        goto L_0x00ab;
    L_0x005b:
        r5 = r0.currentPhotoObject;
        if (r5 == 0) goto L_0x009f;
    L_0x005f:
        r4 = org.telegram.messenger.FileLoader.getAttachFileName(r5);
        r5 = r0.currentMessageObject;
        r5 = r5.mediaExists;
        goto L_0x00ab;
    L_0x0068:
        r5 = r0.currentMessageObject;
        r13 = r5.useCustomPhoto;
        if (r13 == 0) goto L_0x007a;
    L_0x006e:
        r0.buttonState = r12;
        r2 = r0.radialProgress;
        r4 = r16.getIconForCurrentState();
        r2.setIcon(r4, r1, r3);
        return;
    L_0x007a:
        r13 = r5.attachPathExists;
        if (r13 == 0) goto L_0x0090;
    L_0x007e:
        r5 = r5.messageOwner;
        r5 = r5.attachPath;
        r5 = android.text.TextUtils.isEmpty(r5);
        if (r5 != 0) goto L_0x0090;
    L_0x0088:
        r4 = r0.currentMessageObject;
        r4 = r4.messageOwner;
        r4 = r4.attachPath;
        r5 = 1;
        goto L_0x00ab;
    L_0x0090:
        r5 = r0.currentMessageObject;
        r5 = r5.isSendError();
        if (r5 == 0) goto L_0x00a1;
    L_0x0098:
        r5 = r0.documentAttachType;
        if (r5 == r9) goto L_0x00a1;
    L_0x009c:
        if (r5 != r8) goto L_0x009f;
    L_0x009e:
        goto L_0x00a1;
    L_0x009f:
        r5 = 0;
        goto L_0x00ab;
    L_0x00a1:
        r4 = r0.currentMessageObject;
        r4 = r4.getFileName();
        r5 = r0.currentMessageObject;
        r5 = r5.mediaExists;
    L_0x00ab:
        r13 = r0.currentAccount;
        r13 = org.telegram.messenger.DownloadController.getInstance(r13);
        r14 = r0.currentMessageObject;
        r13 = r13.canDownloadMedia(r14);
        r14 = r0.currentMessageObject;
        r14 = r14.isSent();
        r15 = 2;
        if (r14 == 0) goto L_0x00dc;
    L_0x00c0:
        r14 = r0.documentAttachType;
        if (r14 == r11) goto L_0x00ca;
    L_0x00c4:
        if (r14 == r10) goto L_0x00ca;
    L_0x00c6:
        if (r14 != r15) goto L_0x00dc;
    L_0x00c8:
        if (r13 == 0) goto L_0x00dc;
    L_0x00ca:
        r14 = r0.currentMessageObject;
        r14 = r14.canStreamVideo();
        if (r14 == 0) goto L_0x00dc;
    L_0x00d2:
        r14 = r0.currentMessageObject;
        r14 = r14.needDrawBluredPreview();
        if (r14 != 0) goto L_0x00dc;
    L_0x00da:
        r14 = 1;
        goto L_0x00dd;
    L_0x00dc:
        r14 = 0;
    L_0x00dd:
        r0.canStreamVideo = r14;
        r14 = org.telegram.messenger.SharedConfig.streamMedia;
        if (r14 == 0) goto L_0x0114;
    L_0x00e3:
        r14 = r0.currentMessageObject;
        r18 = r3;
        r2 = r14.getDialogId();
        r3 = (int) r2;
        if (r3 == 0) goto L_0x0116;
    L_0x00ee:
        r2 = r0.currentMessageObject;
        r2 = r2.isSecretMedia();
        if (r2 != 0) goto L_0x0116;
    L_0x00f6:
        r2 = r0.documentAttachType;
        if (r2 == r8) goto L_0x010b;
    L_0x00fa:
        r2 = r0.canStreamVideo;
        if (r2 == 0) goto L_0x0116;
    L_0x00fe:
        r2 = r0.currentPosition;
        if (r2 == 0) goto L_0x0116;
    L_0x0102:
        r2 = r2.flags;
        r3 = r2 & 1;
        if (r3 == 0) goto L_0x010b;
    L_0x0108:
        r2 = r2 & r15;
        if (r2 != 0) goto L_0x0116;
    L_0x010b:
        if (r5 == 0) goto L_0x010f;
    L_0x010d:
        r2 = 1;
        goto L_0x0110;
    L_0x010f:
        r2 = 2;
    L_0x0110:
        r0.hasMiniProgress = r2;
        r5 = 1;
        goto L_0x0116;
    L_0x0114:
        r18 = r3;
    L_0x0116:
        r2 = r0.currentMessageObject;
        r2 = r2.isSendError();
        if (r2 != 0) goto L_0x0734;
    L_0x011e:
        r2 = android.text.TextUtils.isEmpty(r4);
        if (r2 == 0) goto L_0x0136;
    L_0x0124:
        r2 = r0.currentMessageObject;
        r2 = r2.isSending();
        if (r2 != 0) goto L_0x0136;
    L_0x012c:
        r2 = r0.currentMessageObject;
        r2 = r2.isEditing();
        if (r2 != 0) goto L_0x0136;
    L_0x0134:
        goto L_0x0734;
    L_0x0136:
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.params;
        if (r2 == 0) goto L_0x0148;
    L_0x013e:
        r3 = "query_id";
        r2 = r2.containsKey(r3);
        if (r2 == 0) goto L_0x0148;
    L_0x0146:
        r2 = 1;
        goto L_0x0149;
    L_0x0148:
        r2 = 0;
    L_0x0149:
        r3 = r0.documentAttachType;
        r14 = 0;
        if (r3 == r9) goto L_0x0576;
    L_0x014e:
        if (r3 != r8) goto L_0x0152;
    L_0x0150:
        goto L_0x0576;
    L_0x0152:
        r2 = r0.currentMessageObject;
        r2 = r2.type;
        if (r2 != 0) goto L_0x01f9;
    L_0x0158:
        if (r3 == r12) goto L_0x01f9;
    L_0x015a:
        if (r3 == r15) goto L_0x01f9;
    L_0x015c:
        if (r3 == r10) goto L_0x01f9;
    L_0x015e:
        if (r3 == r11) goto L_0x01f9;
    L_0x0160:
        if (r3 == r7) goto L_0x01f9;
    L_0x0162:
        if (r3 == r6) goto L_0x01f9;
    L_0x0164:
        r2 = r0.currentPhotoObject;
        if (r2 == 0) goto L_0x01f8;
    L_0x0168:
        r2 = r0.drawImageButton;
        if (r2 != 0) goto L_0x016e;
    L_0x016c:
        goto L_0x01f8;
    L_0x016e:
        if (r5 != 0) goto L_0x01cd;
    L_0x0170:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r2.addLoadingFileObserver(r4, r3, r0);
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r2 = r2.isLoadingFile(r4);
        if (r2 != 0) goto L_0x01a6;
    L_0x0187:
        r2 = r0.cancelLoading;
        if (r2 != 0) goto L_0x01a2;
    L_0x018b:
        r2 = r0.documentAttachType;
        if (r2 != 0) goto L_0x0191;
    L_0x018f:
        if (r13 != 0) goto L_0x019f;
    L_0x0191:
        r2 = r0.documentAttachType;
        if (r2 != r15) goto L_0x01a2;
    L_0x0195:
        r2 = r0.documentAttach;
        r2 = org.telegram.messenger.MessageObject.isGifDocument(r2);
        if (r2 == 0) goto L_0x01a2;
    L_0x019d:
        if (r13 == 0) goto L_0x01a2;
    L_0x019f:
        r0.buttonState = r12;
        goto L_0x01b7;
    L_0x01a2:
        r2 = 0;
        r0.buttonState = r2;
        goto L_0x01b7;
    L_0x01a6:
        r0.buttonState = r12;
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r2.getFileProgress(r4);
        if (r2 == 0) goto L_0x01b7;
    L_0x01b2:
        r2 = r2.floatValue();
        r14 = r2;
    L_0x01b7:
        r2 = r0.radialProgress;
        r3 = 0;
        r2.setProgress(r14, r3);
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r6 = r18;
        r2.setIcon(r3, r1, r6);
        r16.invalidate();
        goto L_0x0729;
    L_0x01cd:
        r6 = r18;
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
        r2 = r0.documentAttachType;
        if (r2 != r15) goto L_0x01e7;
    L_0x01dc:
        r2 = r0.photoImage;
        r2 = r2.isAllowStartAnimation();
        if (r2 != 0) goto L_0x01e7;
    L_0x01e4:
        r0.buttonState = r15;
        goto L_0x01ea;
    L_0x01e7:
        r2 = -1;
        r0.buttonState = r2;
    L_0x01ea:
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        r16.invalidate();
        goto L_0x0729;
    L_0x01f8:
        return;
    L_0x01f9:
        r6 = r18;
        r2 = r0.currentMessageObject;
        r2 = r2.isOut();
        if (r2 == 0) goto L_0x02fd;
    L_0x0203:
        r2 = r0.currentMessageObject;
        r2 = r2.isSending();
        if (r2 != 0) goto L_0x0213;
    L_0x020b:
        r2 = r0.currentMessageObject;
        r2 = r2.isEditing();
        if (r2 == 0) goto L_0x02fd;
    L_0x0213:
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.attachPath;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 != 0) goto L_0x02c8;
    L_0x021f:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r4 = r3.messageOwner;
        r4 = r4.attachPath;
        r2.addLoadingFileObserver(r4, r3, r0);
        r0.wasSending = r12;
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.attachPath;
        if (r2 == 0) goto L_0x0243;
    L_0x0238:
        r3 = "http";
        r2 = r2.startsWith(r3);
        if (r2 != 0) goto L_0x0241;
    L_0x0240:
        goto L_0x0243;
    L_0x0241:
        r2 = 0;
        goto L_0x0244;
    L_0x0243:
        r2 = 1;
    L_0x0244:
        r3 = r0.currentMessageObject;
        r3 = r3.messageOwner;
        r4 = r3.params;
        r3 = r3.message;
        if (r3 == 0) goto L_0x0265;
    L_0x024e:
        if (r4 == 0) goto L_0x0265;
    L_0x0250:
        r3 = "url";
        r3 = r4.containsKey(r3);
        if (r3 != 0) goto L_0x0260;
    L_0x0258:
        r3 = "bot";
        r3 = r4.containsKey(r3);
        if (r3 == 0) goto L_0x0265;
    L_0x0260:
        r2 = -1;
        r0.buttonState = r2;
        r2 = 0;
        goto L_0x0267;
    L_0x0265:
        r0.buttonState = r12;
    L_0x0267:
        r3 = r0.currentAccount;
        r3 = org.telegram.messenger.SendMessagesHelper.getInstance(r3);
        r4 = r0.currentMessageObject;
        r4 = r4.getId();
        r3 = r3.isSendingMessage(r4);
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x028d;
    L_0x027b:
        if (r3 == 0) goto L_0x028d;
    L_0x027d:
        r4 = r0.buttonState;
        if (r4 != r12) goto L_0x028d;
    L_0x0281:
        r0.drawRadialCheckBackground = r12;
        r16.getIconForCurrentState();
        r4 = r0.radialProgress;
        r5 = 6;
        r4.setIcon(r5, r1, r6);
        goto L_0x0296;
    L_0x028d:
        r4 = r0.radialProgress;
        r5 = r16.getIconForCurrentState();
        r4.setIcon(r5, r1, r6);
    L_0x0296:
        if (r2 == 0) goto L_0x02bd;
    L_0x0298:
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r4 = r0.currentMessageObject;
        r4 = r4.messageOwner;
        r4 = r4.attachPath;
        r2 = r2.getFileProgress(r4);
        if (r2 != 0) goto L_0x02b0;
    L_0x02a8:
        if (r3 == 0) goto L_0x02b0;
    L_0x02aa:
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = java.lang.Float.valueOf(r3);
    L_0x02b0:
        r3 = r0.radialProgress;
        if (r2 == 0) goto L_0x02b8;
    L_0x02b4:
        r14 = r2.floatValue();
    L_0x02b8:
        r2 = 0;
        r3.setProgress(r14, r2);
        goto L_0x02c3;
    L_0x02bd:
        r2 = 0;
        r3 = r0.radialProgress;
        r3.setProgress(r14, r2);
    L_0x02c3:
        r16.invalidate();
        r4 = 0;
        goto L_0x02f6;
    L_0x02c8:
        r2 = -1;
        r0.buttonState = r2;
        r16.getIconForCurrentState();
        r2 = r0.radialProgress;
        r3 = r0.currentMessageObject;
        r3 = r3.isSticker();
        if (r3 != 0) goto L_0x02ec;
    L_0x02d8:
        r3 = r0.currentMessageObject;
        r3 = r3.isAnimatedSticker();
        if (r3 != 0) goto L_0x02ec;
    L_0x02e0:
        r3 = r0.currentMessageObject;
        r3 = r3.isLocation();
        if (r3 == 0) goto L_0x02e9;
    L_0x02e8:
        goto L_0x02ec;
    L_0x02e9:
        r3 = 12;
        goto L_0x02ed;
    L_0x02ec:
        r3 = 4;
    L_0x02ed:
        r4 = 0;
        r2.setIcon(r3, r1, r4);
        r2 = r0.radialProgress;
        r2.setProgress(r14, r4);
    L_0x02f6:
        r2 = r0.videoRadialProgress;
        r2.setIcon(r11, r1, r4);
        goto L_0x0729;
    L_0x02fd:
        r2 = r0.wasSending;
        if (r2 == 0) goto L_0x0316;
    L_0x0301:
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.attachPath;
        r2 = android.text.TextUtils.isEmpty(r2);
        if (r2 != 0) goto L_0x0316;
    L_0x030d:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
    L_0x0316:
        r2 = r0.documentAttachType;
        if (r2 == r11) goto L_0x031e;
    L_0x031a:
        if (r2 == r15) goto L_0x031e;
    L_0x031c:
        if (r2 != r10) goto L_0x036b;
    L_0x031e:
        r2 = r0.autoPlayingMedia;
        if (r2 == 0) goto L_0x036b;
    L_0x0322:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r3 = r0.documentAttach;
        r7 = org.telegram.messenger.MediaController.getInstance();
        r8 = r0.currentMessageObject;
        r7 = r7.isPlayingMessage(r8);
        r2 = r2.isLoadingVideo(r3, r7);
        r3 = r0.photoImage;
        r3 = r3.getAnimation();
        if (r3 == 0) goto L_0x0360;
    L_0x0340:
        r7 = r0.currentMessageObject;
        r8 = r7.hadAnimationNotReadyLoading;
        if (r8 == 0) goto L_0x0352;
    L_0x0346:
        r3 = r3.hasBitmap();
        if (r3 == 0) goto L_0x036c;
    L_0x034c:
        r3 = r0.currentMessageObject;
        r7 = 0;
        r3.hadAnimationNotReadyLoading = r7;
        goto L_0x036c;
    L_0x0352:
        if (r2 == 0) goto L_0x035c;
    L_0x0354:
        r3 = r3.hasBitmap();
        if (r3 != 0) goto L_0x035c;
    L_0x035a:
        r3 = 1;
        goto L_0x035d;
    L_0x035c:
        r3 = 0;
    L_0x035d:
        r7.hadAnimationNotReadyLoading = r3;
        goto L_0x036c;
    L_0x0360:
        r3 = r0.documentAttachType;
        if (r3 != r15) goto L_0x036c;
    L_0x0364:
        if (r5 != 0) goto L_0x036c;
    L_0x0366:
        r3 = r0.currentMessageObject;
        r3.hadAnimationNotReadyLoading = r12;
        goto L_0x036c;
    L_0x036b:
        r2 = 0;
    L_0x036c:
        r3 = r0.hasMiniProgress;
        if (r3 == 0) goto L_0x03d8;
    L_0x0370:
        r2 = r0.radialProgress;
        r3 = "chat_inLoaderPhoto";
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setMiniProgressBackgroundColor(r3);
        r0.buttonState = r9;
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        r2 = r0.hasMiniProgress;
        if (r2 != r12) goto L_0x0397;
    L_0x038a:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
        r2 = -1;
        r0.miniButtonState = r2;
        goto L_0x03cd;
    L_0x0397:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r2.addLoadingFileObserver(r4, r3, r0);
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r2 = r2.isLoadingFile(r4);
        if (r2 != 0) goto L_0x03b2;
    L_0x03ae:
        r2 = 0;
        r0.miniButtonState = r2;
        goto L_0x03cd;
    L_0x03b2:
        r0.miniButtonState = r12;
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r2.getFileProgress(r4);
        if (r2 == 0) goto L_0x03c8;
    L_0x03be:
        r3 = r0.radialProgress;
        r2 = r2.floatValue();
        r3.setProgress(r2, r6);
        goto L_0x03cd;
    L_0x03c8:
        r2 = r0.radialProgress;
        r2.setProgress(r14, r6);
    L_0x03cd:
        r2 = r0.radialProgress;
        r3 = r16.getMiniIconForCurrentState();
        r2.setMiniIcon(r3, r1, r6);
        goto L_0x0729;
    L_0x03d8:
        if (r5 != 0) goto L_0x04f8;
    L_0x03da:
        r3 = r0.documentAttachType;
        if (r3 == r11) goto L_0x03e2;
    L_0x03de:
        if (r3 == r15) goto L_0x03e2;
    L_0x03e0:
        if (r3 != r10) goto L_0x03f0;
    L_0x03e2:
        r3 = r0.autoPlayingMedia;
        if (r3 == 0) goto L_0x03f0;
    L_0x03e6:
        r3 = r0.currentMessageObject;
        r3 = r3.hadAnimationNotReadyLoading;
        if (r3 != 0) goto L_0x03f0;
    L_0x03ec:
        if (r2 != 0) goto L_0x03f0;
    L_0x03ee:
        goto L_0x04f8;
    L_0x03f0:
        r2 = r0.documentAttachType;
        if (r2 == r11) goto L_0x03f9;
    L_0x03f4:
        if (r2 != r15) goto L_0x03f7;
    L_0x03f6:
        goto L_0x03f9;
    L_0x03f7:
        r2 = 0;
        goto L_0x03fa;
    L_0x03f9:
        r2 = 1;
    L_0x03fa:
        r0.drawVideoSize = r2;
        r2 = r0.documentAttachType;
        if (r2 == r11) goto L_0x0404;
    L_0x0400:
        if (r2 == r15) goto L_0x0404;
    L_0x0402:
        if (r2 != r10) goto L_0x0421;
    L_0x0404:
        r2 = r0.canStreamVideo;
        if (r2 == 0) goto L_0x0421;
    L_0x0408:
        r2 = r0.drawVideoImageButton;
        if (r2 != 0) goto L_0x0421;
    L_0x040c:
        if (r6 == 0) goto L_0x0421;
    L_0x040e:
        r2 = r0.animatingDrawVideoImageButton;
        if (r2 == r15) goto L_0x0429;
    L_0x0412:
        r3 = r0.animatingDrawVideoImageButtonProgress;
        r5 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r3 >= 0) goto L_0x0429;
    L_0x041a:
        if (r2 != 0) goto L_0x041e;
    L_0x041c:
        r0.animatingDrawVideoImageButtonProgress = r14;
    L_0x041e:
        r0.animatingDrawVideoImageButton = r15;
        goto L_0x0429;
    L_0x0421:
        r2 = r0.animatingDrawVideoImageButton;
        if (r2 != 0) goto L_0x0429;
    L_0x0425:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.animatingDrawVideoImageButtonProgress = r2;
    L_0x0429:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r2.addLoadingFileObserver(r4, r3, r0);
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r2 = r2.isLoadingFile(r4);
        if (r2 != 0) goto L_0x048c;
    L_0x0440:
        r2 = r0.cancelLoading;
        if (r2 != 0) goto L_0x0449;
    L_0x0444:
        if (r13 == 0) goto L_0x0449;
    L_0x0446:
        r0.buttonState = r12;
        goto L_0x044c;
    L_0x0449:
        r2 = 0;
        r0.buttonState = r2;
    L_0x044c:
        r2 = r0.documentAttachType;
        if (r2 == r11) goto L_0x0454;
    L_0x0450:
        if (r2 != r15) goto L_0x0470;
    L_0x0452:
        if (r13 == 0) goto L_0x0470;
    L_0x0454:
        r2 = r0.canStreamVideo;
        if (r2 == 0) goto L_0x0470;
    L_0x0458:
        r0.drawVideoImageButton = r12;
        r16.getIconForCurrentState();
        r2 = r0.radialProgress;
        r3 = r0.autoPlayingMedia;
        if (r3 == 0) goto L_0x0465;
    L_0x0463:
        r3 = 4;
        goto L_0x0466;
    L_0x0465:
        r3 = 0;
    L_0x0466:
        r2.setIcon(r3, r1, r6);
        r2 = r0.videoRadialProgress;
        r2.setIcon(r15, r1, r6);
        goto L_0x04f3;
    L_0x0470:
        r2 = 0;
        r0.drawVideoImageButton = r2;
        r3 = r0.radialProgress;
        r4 = r16.getIconForCurrentState();
        r3.setIcon(r4, r1, r6);
        r3 = r0.videoRadialProgress;
        r3.setIcon(r11, r1, r2);
        r1 = r0.drawVideoSize;
        if (r1 != 0) goto L_0x04f3;
    L_0x0485:
        r1 = r0.animatingDrawVideoImageButton;
        if (r1 != 0) goto L_0x04f3;
    L_0x0489:
        r0.animatingDrawVideoImageButtonProgress = r14;
        goto L_0x04f3;
    L_0x048c:
        r0.buttonState = r12;
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r2.getFileProgress(r4);
        r3 = r0.documentAttachType;
        if (r3 == r11) goto L_0x049e;
    L_0x049a:
        if (r3 != r15) goto L_0x04cb;
    L_0x049c:
        if (r13 == 0) goto L_0x04cb;
    L_0x049e:
        r3 = r0.canStreamVideo;
        if (r3 == 0) goto L_0x04cb;
    L_0x04a2:
        r0.drawVideoImageButton = r12;
        r16.getIconForCurrentState();
        r3 = r0.radialProgress;
        r4 = r0.autoPlayingMedia;
        if (r4 != 0) goto L_0x04b4;
    L_0x04ad:
        r4 = r0.documentAttachType;
        if (r4 != r15) goto L_0x04b2;
    L_0x04b1:
        goto L_0x04b4;
    L_0x04b2:
        r4 = 0;
        goto L_0x04b5;
    L_0x04b4:
        r4 = 4;
    L_0x04b5:
        r3.setIcon(r4, r1, r6);
        r3 = r0.videoRadialProgress;
        if (r2 == 0) goto L_0x04c0;
    L_0x04bc:
        r14 = r2.floatValue();
    L_0x04c0:
        r3.setProgress(r14, r6);
        r2 = r0.videoRadialProgress;
        r3 = 14;
        r2.setIcon(r3, r1, r6);
        goto L_0x04f3;
    L_0x04cb:
        r3 = 0;
        r0.drawVideoImageButton = r3;
        r4 = r0.radialProgress;
        if (r2 == 0) goto L_0x04d7;
    L_0x04d2:
        r2 = r2.floatValue();
        goto L_0x04d8;
    L_0x04d7:
        r2 = 0;
    L_0x04d8:
        r4.setProgress(r2, r6);
        r2 = r0.radialProgress;
        r4 = r16.getIconForCurrentState();
        r2.setIcon(r4, r1, r6);
        r2 = r0.videoRadialProgress;
        r2.setIcon(r11, r1, r3);
        r1 = r0.drawVideoSize;
        if (r1 != 0) goto L_0x04f3;
    L_0x04ed:
        r1 = r0.animatingDrawVideoImageButton;
        if (r1 != 0) goto L_0x04f3;
    L_0x04f1:
        r0.animatingDrawVideoImageButtonProgress = r14;
    L_0x04f3:
        r16.invalidate();
        goto L_0x0729;
    L_0x04f8:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
        r2 = r0.drawVideoImageButton;
        if (r2 == 0) goto L_0x051a;
    L_0x0505:
        if (r6 == 0) goto L_0x051a;
    L_0x0507:
        r2 = r0.animatingDrawVideoImageButton;
        if (r2 == r12) goto L_0x0520;
    L_0x050b:
        r3 = r0.animatingDrawVideoImageButtonProgress;
        r3 = (r3 > r14 ? 1 : (r3 == r14 ? 0 : -1));
        if (r3 <= 0) goto L_0x0520;
    L_0x0511:
        if (r2 != 0) goto L_0x0517;
    L_0x0513:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.animatingDrawVideoImageButtonProgress = r2;
    L_0x0517:
        r0.animatingDrawVideoImageButton = r12;
        goto L_0x0520;
    L_0x051a:
        r2 = r0.animatingDrawVideoImageButton;
        if (r2 != 0) goto L_0x0520;
    L_0x051e:
        r0.animatingDrawVideoImageButtonProgress = r14;
    L_0x0520:
        r2 = 0;
        r0.drawVideoImageButton = r2;
        r0.drawVideoSize = r2;
        r2 = r0.currentMessageObject;
        r2 = r2.needDrawBluredPreview();
        if (r2 == 0) goto L_0x0531;
    L_0x052d:
        r2 = -1;
        r0.buttonState = r2;
        goto L_0x054c;
    L_0x0531:
        r2 = r0.documentAttachType;
        if (r2 != r15) goto L_0x0542;
    L_0x0535:
        r2 = r0.currentMessageObject;
        r2 = r2.gifState;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1));
        if (r2 != 0) goto L_0x0542;
    L_0x053f:
        r0.buttonState = r15;
        goto L_0x054c;
    L_0x0542:
        r2 = r0.documentAttachType;
        if (r2 != r11) goto L_0x0549;
    L_0x0546:
        r0.buttonState = r9;
        goto L_0x054c;
    L_0x0549:
        r2 = -1;
        r0.buttonState = r2;
    L_0x054c:
        r2 = r0.videoRadialProgress;
        r3 = r0.animatingDrawVideoImageButton;
        if (r3 == 0) goto L_0x0553;
    L_0x0552:
        goto L_0x0554;
    L_0x0553:
        r12 = 0;
    L_0x0554:
        r2.setIcon(r11, r1, r12);
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        if (r19 != 0) goto L_0x0571;
    L_0x0562:
        r1 = r0.photoNotSet;
        if (r1 == 0) goto L_0x0571;
    L_0x0566:
        r1 = r0.currentMessageObject;
        r2 = r0.currentMessagesGroup;
        r3 = r0.pinnedBottom;
        r4 = r0.pinnedTop;
        r0.setMessageObject(r1, r2, r3, r4);
    L_0x0571:
        r16.invalidate();
        goto L_0x0729;
    L_0x0576:
        r6 = r18;
        r3 = r0.currentMessageObject;
        r3 = r3.isOut();
        if (r3 == 0) goto L_0x0590;
    L_0x0580:
        r3 = r0.currentMessageObject;
        r3 = r3.isSending();
        if (r3 != 0) goto L_0x059a;
    L_0x0588:
        r3 = r0.currentMessageObject;
        r3 = r3.isEditing();
        if (r3 != 0) goto L_0x059a;
    L_0x0590:
        r3 = r0.currentMessageObject;
        r3 = r3.isSendError();
        if (r3 == 0) goto L_0x0617;
    L_0x0598:
        if (r2 == 0) goto L_0x0617;
    L_0x059a:
        r3 = r0.currentMessageObject;
        r3 = r3.messageOwner;
        r3 = r3.attachPath;
        r3 = android.text.TextUtils.isEmpty(r3);
        if (r3 != 0) goto L_0x0602;
    L_0x05a6:
        r3 = r0.currentAccount;
        r3 = org.telegram.messenger.DownloadController.getInstance(r3);
        r4 = r0.currentMessageObject;
        r5 = r4.messageOwner;
        r5 = r5.attachPath;
        r3.addLoadingFileObserver(r5, r4, r0);
        r0.wasSending = r12;
        r0.buttonState = r11;
        r3 = r0.radialProgress;
        r4 = r16.getIconForCurrentState();
        r3.setIcon(r4, r1, r6);
        if (r2 != 0) goto L_0x05fa;
    L_0x05c4:
        r1 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.attachPath;
        r1 = r1.getFileProgress(r2);
        if (r1 != 0) goto L_0x05ec;
    L_0x05d4:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.SendMessagesHelper.getInstance(r2);
        r3 = r0.currentMessageObject;
        r3 = r3.getId();
        r2 = r2.isSendingMessage(r3);
        if (r2 == 0) goto L_0x05ec;
    L_0x05e6:
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1 = java.lang.Float.valueOf(r2);
    L_0x05ec:
        r2 = r0.radialProgress;
        if (r1 == 0) goto L_0x05f4;
    L_0x05f0:
        r14 = r1.floatValue();
    L_0x05f4:
        r3 = 0;
        r2.setProgress(r14, r3);
        goto L_0x0726;
    L_0x05fa:
        r3 = 0;
        r1 = r0.radialProgress;
        r1.setProgress(r14, r3);
        goto L_0x0726;
    L_0x0602:
        r2 = -1;
        r3 = 0;
        r0.buttonState = r2;
        r16.getIconForCurrentState();
        r2 = r0.radialProgress;
        r4 = 12;
        r2.setIcon(r4, r1, r3);
        r1 = r0.radialProgress;
        r1.setProgress(r14, r3);
        goto L_0x0726;
    L_0x0617:
        r2 = r0.hasMiniProgress;
        if (r2 == 0) goto L_0x06ab;
    L_0x061b:
        r2 = r0.radialProgress;
        r3 = r0.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x0628;
    L_0x0625:
        r3 = "chat_outLoader";
        goto L_0x062a;
    L_0x0628:
        r3 = "chat_inLoader";
    L_0x062a:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r2.setMiniProgressBackgroundColor(r3);
        r2 = org.telegram.messenger.MediaController.getInstance();
        r3 = r0.currentMessageObject;
        r2 = r2.isPlayingMessage(r3);
        if (r2 == 0) goto L_0x064d;
    L_0x063d:
        if (r2 == 0) goto L_0x064a;
    L_0x063f:
        r2 = org.telegram.messenger.MediaController.getInstance();
        r2 = r2.isMessagePaused();
        if (r2 == 0) goto L_0x064a;
    L_0x0649:
        goto L_0x064d;
    L_0x064a:
        r0.buttonState = r12;
        goto L_0x0650;
    L_0x064d:
        r2 = 0;
        r0.buttonState = r2;
    L_0x0650:
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        r2 = r0.hasMiniProgress;
        if (r2 != r12) goto L_0x066a;
    L_0x065d:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
        r2 = -1;
        r0.miniButtonState = r2;
        goto L_0x06a0;
    L_0x066a:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r2.addLoadingFileObserver(r4, r3, r0);
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r2 = r2.isLoadingFile(r4);
        if (r2 != 0) goto L_0x0685;
    L_0x0681:
        r2 = 0;
        r0.miniButtonState = r2;
        goto L_0x06a0;
    L_0x0685:
        r0.miniButtonState = r12;
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r2.getFileProgress(r4);
        if (r2 == 0) goto L_0x069b;
    L_0x0691:
        r3 = r0.radialProgress;
        r2 = r2.floatValue();
        r3.setProgress(r2, r6);
        goto L_0x06a0;
    L_0x069b:
        r2 = r0.radialProgress;
        r2.setProgress(r14, r6);
    L_0x06a0:
        r2 = r0.radialProgress;
        r3 = r16.getMiniIconForCurrentState();
        r2.setMiniIcon(r3, r1, r6);
        goto L_0x0726;
    L_0x06ab:
        if (r5 == 0) goto L_0x06df;
    L_0x06ad:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r2.removeLoadingFileObserver(r0);
        r2 = org.telegram.messenger.MediaController.getInstance();
        r3 = r0.currentMessageObject;
        r2 = r2.isPlayingMessage(r3);
        if (r2 == 0) goto L_0x06d2;
    L_0x06c2:
        if (r2 == 0) goto L_0x06cf;
    L_0x06c4:
        r2 = org.telegram.messenger.MediaController.getInstance();
        r2 = r2.isMessagePaused();
        if (r2 == 0) goto L_0x06cf;
    L_0x06ce:
        goto L_0x06d2;
    L_0x06cf:
        r0.buttonState = r12;
        goto L_0x06d5;
    L_0x06d2:
        r2 = 0;
        r0.buttonState = r2;
    L_0x06d5:
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        goto L_0x0726;
    L_0x06df:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.DownloadController.getInstance(r2);
        r3 = r0.currentMessageObject;
        r2.addLoadingFileObserver(r4, r3, r0);
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.FileLoader.getInstance(r2);
        r2 = r2.isLoadingFile(r4);
        if (r2 != 0) goto L_0x0702;
    L_0x06f6:
        r0.buttonState = r15;
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
        goto L_0x0726;
    L_0x0702:
        r0.buttonState = r11;
        r2 = org.telegram.messenger.ImageLoader.getInstance();
        r2 = r2.getFileProgress(r4);
        if (r2 == 0) goto L_0x0718;
    L_0x070e:
        r3 = r0.radialProgress;
        r2 = r2.floatValue();
        r3.setProgress(r2, r6);
        goto L_0x071d;
    L_0x0718:
        r2 = r0.radialProgress;
        r2.setProgress(r14, r6);
    L_0x071d:
        r2 = r0.radialProgress;
        r3 = r16.getIconForCurrentState();
        r2.setIcon(r3, r1, r6);
    L_0x0726:
        r16.updatePlayingMessageProgress();
    L_0x0729:
        r1 = r0.hasMiniProgress;
        if (r1 != 0) goto L_0x0733;
    L_0x072d:
        r1 = r0.radialProgress;
        r2 = 0;
        r1.setMiniIcon(r11, r2, r6);
    L_0x0733:
        return;
    L_0x0734:
        r2 = 0;
        r3 = r0.radialProgress;
        r3.setIcon(r11, r1, r2);
        r3 = r0.radialProgress;
        r3.setMiniIcon(r11, r1, r2);
        r3 = r0.videoRadialProgress;
        r3.setIcon(r11, r1, r2);
        r3 = r0.videoRadialProgress;
        r3.setMiniIcon(r11, r1, r2);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.updateButtonState(boolean, boolean, boolean):void");
    }

    private void didPressMiniButton(boolean z) {
        int i = this.miniButtonState;
        if (i == 0) {
            this.miniButtonState = 1;
            this.radialProgress.setProgress(0.0f, false);
            i = this.documentAttachType;
            if (i == 3 || i == 5) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 1, 0);
            } else if (i == 4) {
                FileLoader instance = FileLoader.getInstance(this.currentAccount);
                Document document = this.documentAttach;
                MessageObject messageObject = this.currentMessageObject;
                instance.loadFile(document, messageObject, 1, messageObject.shouldEncryptPhotoOrVideo() ? 2 : 0);
            }
            this.radialProgress.setMiniIcon(getMiniIconForCurrentState(), false, true);
            invalidate();
        } else if (i == 1) {
            i = this.documentAttachType;
            if ((i == 3 || i == 5) && MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                MediaController.getInstance().cleanupPlayer(true, true);
            }
            this.miniButtonState = 0;
            FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
            this.radialProgress.setMiniIcon(getMiniIconForCurrentState(), false, true);
            invalidate();
        }
    }

    private void didPressButton(boolean z, boolean z2) {
        boolean z3 = z;
        int i = 2;
        int i2;
        if (this.buttonState == 0 && (!this.drawVideoImageButton || z2)) {
            i2 = this.documentAttachType;
            if (i2 == 3 || i2 == 5) {
                if (this.miniButtonState == 0) {
                    FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 1, 0);
                }
                if (this.delegate.needPlayMessage(this.currentMessageObject)) {
                    if (this.hasMiniProgress == 2 && this.miniButtonState != 1) {
                        this.miniButtonState = 1;
                        this.radialProgress.setProgress(0.0f, false);
                        this.radialProgress.setMiniIcon(getMiniIconForCurrentState(), false, true);
                    }
                    updatePlayingMessageProgress();
                    this.buttonState = 1;
                    this.radialProgress.setIcon(getIconForCurrentState(), false, true);
                    invalidate();
                    return;
                }
                return;
            }
            PhotoSize photoSize;
            String str;
            this.cancelLoading = false;
            if (z2) {
                this.videoRadialProgress.setProgress(0.0f, false);
            } else {
                this.radialProgress.setProgress(0.0f, false);
            }
            if (this.currentPhotoObject == null || !(this.photoImage.hasNotThumb() || this.currentPhotoObjectThumb == null)) {
                photoSize = this.currentPhotoObjectThumb;
                str = this.currentPhotoFilterThumb;
            } else {
                photoSize = this.currentPhotoObject;
                if (!(photoSize instanceof TL_photoStrippedSize)) {
                    if (!"s".equals(photoSize.type)) {
                        str = this.currentPhotoFilter;
                    }
                }
                str = this.currentPhotoFilterThumb;
            }
            String str2 = str;
            MessageObject messageObject = this.currentMessageObject;
            int i3 = messageObject.type;
            int i4;
            MessageObject messageObject2;
            Document document;
            if (i3 == 1) {
                this.photoImage.setForceLoading(true);
                ImageReceiver imageReceiver = this.photoImage;
                ImageLocation forObject = ImageLocation.getForObject(this.currentPhotoObject, this.photoParentObject);
                String str3 = this.currentPhotoFilter;
                ImageLocation forObject2 = ImageLocation.getForObject(this.currentPhotoObjectThumb, this.photoParentObject);
                String str4 = this.currentPhotoFilterThumb;
                i4 = this.currentPhotoObject.size;
                messageObject2 = this.currentMessageObject;
                imageReceiver.setImage(forObject, str3, forObject2, str4, i4, null, messageObject2, messageObject2.shouldEncryptPhotoOrVideo() ? 2 : 0);
            } else if (i3 == 8) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 1, 0);
            } else if (!messageObject.isRoundVideo()) {
                i2 = this.currentMessageObject.type;
                if (i2 == 9) {
                    FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 1, 0);
                } else {
                    i4 = this.documentAttachType;
                    if (i4 == 4) {
                        FileLoader instance = FileLoader.getInstance(this.currentAccount);
                        document = this.documentAttach;
                        messageObject2 = this.currentMessageObject;
                        if (!messageObject2.shouldEncryptPhotoOrVideo()) {
                            i = 0;
                        }
                        instance.loadFile(document, messageObject2, 1, i);
                    } else if (i2 != 0 || i4 == 0) {
                        this.photoImage.setForceLoading(true);
                        this.photoImage.setImage(ImageLocation.getForObject(this.currentPhotoObject, this.photoParentObject), this.currentPhotoFilter, ImageLocation.getForObject(this.currentPhotoObjectThumb, this.photoParentObject), this.currentPhotoFilterThumb, 0, null, this.currentMessageObject, 0);
                    } else if (i4 == 2) {
                        this.photoImage.setForceLoading(true);
                        this.photoImage.setImage(ImageLocation.getForDocument(this.documentAttach), null, ImageLocation.getForDocument(this.currentPhotoObject, this.documentAttach), this.currentPhotoFilterThumb, this.documentAttach.size, null, this.currentMessageObject, 0);
                        this.currentMessageObject.gifState = 2.0f;
                    } else if (i4 == 1) {
                        FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 0, 0);
                    } else if (i4 == 8) {
                        this.photoImage.setImage(ImageLocation.getForDocument(this.documentAttach), this.currentPhotoFilter, ImageLocation.getForDocument(this.currentPhotoObject, this.documentAttach), "b1", 0, "jpg", this.currentMessageObject, 1);
                    }
                }
            } else if (this.currentMessageObject.isSecretMedia()) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), this.currentMessageObject, 1, 1);
            } else {
                MessageObject messageObject3 = this.currentMessageObject;
                messageObject3.gifState = 2.0f;
                document = messageObject3.getDocument();
                this.photoImage.setForceLoading(true);
                this.photoImage.setImage(ImageLocation.getForDocument(document), null, ImageLocation.getForObject(photoSize, document), str2, document.size, null, this.currentMessageObject, 0);
            }
            this.buttonState = 1;
            if (z2) {
                this.videoRadialProgress.setIcon(14, false, z3);
            } else {
                this.radialProgress.setIcon(getIconForCurrentState(), false, z3);
            }
            invalidate();
        } else if (this.buttonState != 1 || (this.drawVideoImageButton && !z2)) {
            i2 = this.buttonState;
            if (i2 == 2) {
                i2 = this.documentAttachType;
                if (i2 == 3 || i2 == 5) {
                    this.radialProgress.setProgress(0.0f, false);
                    FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, this.currentMessageObject, 1, 0);
                    this.buttonState = 4;
                    this.radialProgress.setIcon(getIconForCurrentState(), true, z3);
                    invalidate();
                    return;
                }
                if (this.currentMessageObject.isRoundVideo()) {
                    MessageObject playingMessageObject = MediaController.getInstance().getPlayingMessageObject();
                    if (playingMessageObject == null || !playingMessageObject.isRoundVideo()) {
                        this.photoImage.setAllowStartAnimation(true);
                        this.photoImage.startAnimation();
                    }
                } else {
                    this.photoImage.setAllowStartAnimation(true);
                    this.photoImage.startAnimation();
                }
                this.currentMessageObject.gifState = 0.0f;
                this.buttonState = -1;
                this.radialProgress.setIcon(getIconForCurrentState(), false, z3);
            } else if (i2 == 3 || (i2 == 0 && this.drawVideoImageButton)) {
                if (this.hasMiniProgress == 2 && this.miniButtonState != 1) {
                    this.miniButtonState = 1;
                    this.radialProgress.setProgress(0.0f, false);
                    this.radialProgress.setMiniIcon(getMiniIconForCurrentState(), false, z3);
                }
                this.delegate.didPressImage(this, 0.0f, 0.0f);
            } else if (this.buttonState == 4) {
                i2 = this.documentAttachType;
                if (i2 != 3 && i2 != 5) {
                    return;
                }
                if ((this.currentMessageObject.isOut() && (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing())) || this.currentMessageObject.isSendError()) {
                    ChatMessageCellDelegate chatMessageCellDelegate = this.delegate;
                    if (chatMessageCellDelegate != null) {
                        chatMessageCellDelegate.didPressCancelSendButton(this);
                        return;
                    }
                    return;
                }
                FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
                this.buttonState = 2;
                this.radialProgress.setIcon(getIconForCurrentState(), false, z3);
                invalidate();
            }
        } else {
            this.photoImage.setForceLoading(false);
            i2 = this.documentAttachType;
            if (i2 == 3 || i2 == 5) {
                if (MediaController.getInstance().lambda$startAudioAgain$5$MediaController(this.currentMessageObject)) {
                    this.buttonState = 0;
                    this.radialProgress.setIcon(getIconForCurrentState(), false, z3);
                    invalidate();
                }
            } else if (!this.currentMessageObject.isOut() || (!this.currentMessageObject.isSending() && !this.currentMessageObject.isEditing())) {
                this.cancelLoading = true;
                i2 = this.documentAttachType;
                if (i2 == 2 || i2 == 4 || i2 == 1 || i2 == 8) {
                    FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
                } else {
                    i2 = this.currentMessageObject.type;
                    if (i2 == 0 || i2 == 1 || i2 == 8 || i2 == 5) {
                        ImageLoader.getInstance().cancelForceLoadingForImageReceiver(this.photoImage);
                        this.photoImage.cancelLoadImage();
                    } else if (i2 == 9) {
                        FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.getDocument());
                    }
                }
                this.buttonState = 0;
                if (z2) {
                    this.videoRadialProgress.setIcon(2, false, z3);
                } else {
                    this.radialProgress.setIcon(getIconForCurrentState(), false, z3);
                }
                invalidate();
            } else if (this.radialProgress.getIcon() != 6) {
                this.delegate.didPressCancelSendButton(this);
            }
        }
    }

    public void onFailedDownload(String str, boolean z) {
        int i = this.documentAttachType;
        boolean z2 = i == 3 || i == 5;
        updateButtonState(true, z2, false);
    }

    /* JADX WARNING: Missing block: B:33:0x009b, code skipped:
            if ((r1 & 2) != 0) goto L_0x009d;
     */
    public void onSuccessDownload(java.lang.String r22) {
        /*
        r21 = this;
        r0 = r21;
        r1 = r0.documentAttachType;
        r2 = 0;
        r3 = 1;
        r4 = 3;
        if (r1 == r4) goto L_0x01aa;
    L_0x0009:
        r4 = 5;
        if (r1 != r4) goto L_0x000e;
    L_0x000c:
        goto L_0x01aa;
    L_0x000e:
        r1 = r0.drawVideoImageButton;
        r4 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r1 == 0) goto L_0x001a;
    L_0x0014:
        r1 = r0.videoRadialProgress;
        r1.setProgress(r4, r3);
        goto L_0x001f;
    L_0x001a:
        r1 = r0.radialProgress;
        r1.setProgress(r4, r3);
    L_0x001f:
        r1 = r0.currentMessageObject;
        r1 = r1.needDrawBluredPreview();
        r5 = 2;
        if (r1 != 0) goto L_0x0165;
    L_0x0028:
        r1 = r0.autoPlayingMedia;
        if (r1 != 0) goto L_0x0165;
    L_0x002c:
        r1 = r0.documentAttach;
        if (r1 == 0) goto L_0x0165;
    L_0x0030:
        r6 = r0.documentAttachType;
        r7 = 7;
        r8 = "s";
        if (r6 != r7) goto L_0x0089;
    L_0x0037:
        r9 = r0.photoImage;
        r10 = org.telegram.messenger.ImageLocation.getForDocument(r1);
        r1 = r0.currentPhotoObject;
        r6 = r0.photoParentObject;
        r12 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r1 = r0.currentPhotoObject;
        r6 = r1 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r6 != 0) goto L_0x0059;
    L_0x004b:
        if (r1 == 0) goto L_0x0056;
    L_0x004d:
        r1 = r1.type;
        r1 = r8.equals(r1);
        if (r1 == 0) goto L_0x0056;
    L_0x0055:
        goto L_0x0059;
    L_0x0056:
        r1 = r0.currentPhotoFilter;
        goto L_0x005b;
    L_0x0059:
        r1 = r0.currentPhotoFilterThumb;
    L_0x005b:
        r13 = r1;
        r1 = r0.currentPhotoObjectThumb;
        r6 = r0.photoParentObject;
        r14 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r15 = r0.currentPhotoFilterThumb;
        r16 = 0;
        r1 = r0.documentAttach;
        r1 = r1.size;
        r18 = 0;
        r6 = r0.currentMessageObject;
        r20 = 0;
        r11 = "g";
        r17 = r1;
        r19 = r6;
        r9.setImage(r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
        r1 = r0.photoImage;
        r1.setAllowStartAnimation(r3);
        r1 = r0.photoImage;
        r1.startAnimation();
        r0.autoPlayingMedia = r3;
        goto L_0x0165;
    L_0x0089:
        r1 = org.telegram.messenger.SharedConfig.autoplayVideo;
        if (r1 == 0) goto L_0x0100;
    L_0x008d:
        r1 = 4;
        if (r6 != r1) goto L_0x0100;
    L_0x0090:
        r1 = r0.currentPosition;
        if (r1 == 0) goto L_0x009d;
    L_0x0094:
        r1 = r1.flags;
        r6 = r1 & 1;
        if (r6 == 0) goto L_0x0100;
    L_0x009a:
        r1 = r1 & r5;
        if (r1 == 0) goto L_0x0100;
    L_0x009d:
        r0.animatingNoSound = r5;
        r9 = r0.photoImage;
        r1 = r0.documentAttach;
        r10 = org.telegram.messenger.ImageLocation.getForDocument(r1);
        r1 = r0.currentPhotoObject;
        r6 = r0.photoParentObject;
        r12 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r1 = r0.currentPhotoObject;
        r6 = r1 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r6 != 0) goto L_0x00c3;
    L_0x00b5:
        if (r1 == 0) goto L_0x00c0;
    L_0x00b7:
        r1 = r1.type;
        r1 = r8.equals(r1);
        if (r1 == 0) goto L_0x00c0;
    L_0x00bf:
        goto L_0x00c3;
    L_0x00c0:
        r1 = r0.currentPhotoFilter;
        goto L_0x00c5;
    L_0x00c3:
        r1 = r0.currentPhotoFilterThumb;
    L_0x00c5:
        r13 = r1;
        r1 = r0.currentPhotoObjectThumb;
        r6 = r0.photoParentObject;
        r14 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r15 = r0.currentPhotoFilterThumb;
        r16 = 0;
        r1 = r0.documentAttach;
        r1 = r1.size;
        r18 = 0;
        r6 = r0.currentMessageObject;
        r20 = 0;
        r11 = "g";
        r17 = r1;
        r19 = r6;
        r9.setImage(r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
        r1 = r0.currentMessageObject;
        r1 = org.telegram.ui.PhotoViewer.isPlayingMessage(r1);
        if (r1 != 0) goto L_0x00f8;
    L_0x00ed:
        r1 = r0.photoImage;
        r1.setAllowStartAnimation(r3);
        r1 = r0.photoImage;
        r1.startAnimation();
        goto L_0x00fd;
    L_0x00f8:
        r1 = r0.photoImage;
        r1.setAllowStartAnimation(r2);
    L_0x00fd:
        r0.autoPlayingMedia = r3;
        goto L_0x0165;
    L_0x0100:
        r1 = r0.documentAttachType;
        if (r1 != r5) goto L_0x0165;
    L_0x0104:
        r9 = r0.photoImage;
        r1 = r0.documentAttach;
        r10 = org.telegram.messenger.ImageLocation.getForDocument(r1);
        r1 = r0.currentPhotoObject;
        r6 = r0.photoParentObject;
        r12 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r1 = r0.currentPhotoObject;
        r6 = r1 instanceof org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
        if (r6 != 0) goto L_0x0128;
    L_0x011a:
        if (r1 == 0) goto L_0x0125;
    L_0x011c:
        r1 = r1.type;
        r1 = r8.equals(r1);
        if (r1 == 0) goto L_0x0125;
    L_0x0124:
        goto L_0x0128;
    L_0x0125:
        r1 = r0.currentPhotoFilter;
        goto L_0x012a;
    L_0x0128:
        r1 = r0.currentPhotoFilterThumb;
    L_0x012a:
        r13 = r1;
        r1 = r0.currentPhotoObjectThumb;
        r6 = r0.photoParentObject;
        r14 = org.telegram.messenger.ImageLocation.getForObject(r1, r6);
        r15 = r0.currentPhotoFilterThumb;
        r16 = 0;
        r1 = r0.documentAttach;
        r1 = r1.size;
        r18 = 0;
        r6 = r0.currentMessageObject;
        r20 = 0;
        r11 = "g";
        r17 = r1;
        r19 = r6;
        r9.setImage(r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
        r1 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r1 == 0) goto L_0x0159;
    L_0x014e:
        r1 = r0.photoImage;
        r1.setAllowStartAnimation(r3);
        r1 = r0.photoImage;
        r1.startAnimation();
        goto L_0x0163;
    L_0x0159:
        r1 = r0.photoImage;
        r1.setAllowStartAnimation(r2);
        r1 = r0.photoImage;
        r1.stopAnimation();
    L_0x0163:
        r0.autoPlayingMedia = r3;
    L_0x0165:
        r1 = r0.currentMessageObject;
        r6 = r1.type;
        if (r6 != 0) goto L_0x0193;
    L_0x016b:
        r6 = r0.autoPlayingMedia;
        if (r6 != 0) goto L_0x017f;
    L_0x016f:
        r6 = r0.documentAttachType;
        if (r6 != r5) goto L_0x017f;
    L_0x0173:
        r1 = r1.gifState;
        r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1));
        if (r1 == 0) goto L_0x017f;
    L_0x0179:
        r0.buttonState = r5;
        r0.didPressButton(r3, r2);
        goto L_0x01b0;
    L_0x017f:
        r1 = r0.photoNotSet;
        if (r1 != 0) goto L_0x0187;
    L_0x0183:
        r0.updateButtonState(r2, r3, r2);
        goto L_0x01b0;
    L_0x0187:
        r1 = r0.currentMessageObject;
        r2 = r0.currentMessagesGroup;
        r3 = r0.pinnedBottom;
        r4 = r0.pinnedTop;
        r0.setMessageObject(r1, r2, r3, r4);
        goto L_0x01b0;
    L_0x0193:
        r1 = r0.photoNotSet;
        if (r1 != 0) goto L_0x019a;
    L_0x0197:
        r0.updateButtonState(r2, r3, r2);
    L_0x019a:
        r1 = r0.photoNotSet;
        if (r1 == 0) goto L_0x01b0;
    L_0x019e:
        r1 = r0.currentMessageObject;
        r2 = r0.currentMessagesGroup;
        r3 = r0.pinnedBottom;
        r4 = r0.pinnedTop;
        r0.setMessageObject(r1, r2, r3, r4);
        goto L_0x01b0;
    L_0x01aa:
        r0.updateButtonState(r2, r3, r2);
        r21.updateWaveform();
    L_0x01b0:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.onSuccessDownload(java.lang.String):void");
    }

    /* JADX WARNING: Missing block: B:14:0x001e, code skipped:
            if (r1 != 6) goto L_0x0020;
     */
    public void didSetImage(org.telegram.messenger.ImageReceiver r1, boolean r2, boolean r3) {
        /*
        r0 = this;
        r1 = r0.currentMessageObject;
        if (r1 == 0) goto L_0x002e;
    L_0x0004:
        if (r2 == 0) goto L_0x002e;
    L_0x0006:
        if (r3 != 0) goto L_0x002e;
    L_0x0008:
        r2 = r1.mediaExists;
        if (r2 != 0) goto L_0x002e;
    L_0x000c:
        r2 = r1.attachPathExists;
        if (r2 != 0) goto L_0x002e;
    L_0x0010:
        r1 = r1.type;
        r2 = 1;
        if (r1 != 0) goto L_0x0020;
    L_0x0015:
        r1 = r0.documentAttachType;
        r3 = 8;
        if (r1 == r3) goto L_0x0026;
    L_0x001b:
        if (r1 == 0) goto L_0x0026;
    L_0x001d:
        r3 = 6;
        if (r1 == r3) goto L_0x0026;
    L_0x0020:
        r1 = r0.currentMessageObject;
        r1 = r1.type;
        if (r1 != r2) goto L_0x002e;
    L_0x0026:
        r1 = r0.currentMessageObject;
        r1.mediaExists = r2;
        r1 = 0;
        r0.updateButtonState(r1, r2, r1);
    L_0x002e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.didSetImage(org.telegram.messenger.ImageReceiver, boolean, boolean):void");
    }

    public void onAnimationReady(ImageReceiver imageReceiver) {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null && imageReceiver == this.photoImage && messageObject.isAnimatedSticker()) {
            this.delegate.setShouldNotRepeatSticker(this.currentMessageObject);
        }
    }

    public void onProgressDownload(String str, float f) {
        if (this.drawVideoImageButton) {
            this.videoRadialProgress.setProgress(f, true);
        } else {
            this.radialProgress.setProgress(f, true);
        }
        int i = this.documentAttachType;
        if (i == 3 || i == 5) {
            if (this.hasMiniProgress != 0) {
                if (this.miniButtonState != 1) {
                    updateButtonState(false, false, false);
                }
            } else if (this.buttonState != 4) {
                updateButtonState(false, false, false);
            }
        } else if (this.hasMiniProgress != 0) {
            if (this.miniButtonState != 1) {
                updateButtonState(false, false, false);
            }
        } else if (this.buttonState != 1) {
            updateButtonState(false, false, false);
        }
    }

    public void onProgressUpload(String str, float f, boolean z) {
        this.radialProgress.setProgress(f, true);
        if (f == 1.0f && this.currentPosition != null && SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId()) && this.buttonState == 1) {
            this.drawRadialCheckBackground = true;
            getIconForCurrentState();
            this.radialProgress.setIcon(6, false, true);
        }
    }

    public void onProvideStructure(ViewStructure viewStructure) {
        super.onProvideStructure(viewStructure);
        if (this.allowAssistant && VERSION.SDK_INT >= 23) {
            CharSequence charSequence = this.currentMessageObject.messageText;
            if (charSequence == null || charSequence.length() <= 0) {
                charSequence = this.currentMessageObject.caption;
                if (charSequence != null && charSequence.length() > 0) {
                    viewStructure.setText(this.currentMessageObject.caption);
                    return;
                }
                return;
            }
            viewStructure.setText(this.currentMessageObject.messageText);
        }
    }

    public void setDelegate(ChatMessageCellDelegate chatMessageCellDelegate) {
        this.delegate = chatMessageCellDelegate;
    }

    public void setAllowAssistant(boolean z) {
        this.allowAssistant = z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01d2  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01d2  */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Missing block: B:54:0x00de, code skipped:
            if (r14.isEditing() == false) goto L_0x00e1;
     */
    private void measureTime(org.telegram.messenger.MessageObject r14) {
        /*
        r13 = this;
        r0 = r14.scheduled;
        r1 = "";
        r2 = 0;
        if (r0 == 0) goto L_0x0009;
    L_0x0007:
        r0 = r2;
        goto L_0x0057;
    L_0x0009:
        r0 = r14.messageOwner;
        r3 = r0.post_author;
        r4 = "\n";
        if (r3 == 0) goto L_0x0016;
    L_0x0011:
        r0 = r3.replace(r4, r1);
        goto L_0x0057;
    L_0x0016:
        r0 = r0.fwd_from;
        if (r0 == 0) goto L_0x0023;
    L_0x001a:
        r0 = r0.post_author;
        if (r0 == 0) goto L_0x0023;
    L_0x001e:
        r0 = r0.replace(r4, r1);
        goto L_0x0057;
    L_0x0023:
        r0 = r14.isOutOwner();
        if (r0 != 0) goto L_0x0007;
    L_0x0029:
        r0 = r14.messageOwner;
        r3 = r0.from_id;
        if (r3 <= 0) goto L_0x0007;
    L_0x002f:
        r0 = r0.post;
        if (r0 == 0) goto L_0x0007;
    L_0x0033:
        r0 = r13.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r3 = r14.messageOwner;
        r3 = r3.from_id;
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r0.getUser(r3);
        if (r0 == 0) goto L_0x0007;
    L_0x0047:
        r3 = r0.first_name;
        r0 = r0.last_name;
        r0 = org.telegram.messenger.ContactsController.formatName(r3, r0);
        r3 = 10;
        r4 = 32;
        r0 = r0.replace(r3, r4);
    L_0x0057:
        r3 = r13.currentMessageObject;
        r3 = r3.isFromUser();
        if (r3 == 0) goto L_0x0072;
    L_0x005f:
        r3 = r13.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r4 = r14.messageOwner;
        r4 = r4.from_id;
        r4 = java.lang.Integer.valueOf(r4);
        r3 = r3.getUser(r4);
        goto L_0x0073;
    L_0x0072:
        r3 = r2;
    L_0x0073:
        r4 = r14.scheduled;
        r5 = 1;
        r6 = 0;
        if (r4 != 0) goto L_0x00e1;
    L_0x0079:
        r4 = r14.isLiveLocation();
        if (r4 != 0) goto L_0x00e1;
    L_0x007f:
        r4 = r14.messageOwner;
        r4 = r4.edit_hide;
        if (r4 != 0) goto L_0x00e1;
    L_0x0085:
        r7 = r14.getDialogId();
        r9 = 777000; // 0xbdb28 float:1.088809E-39 double:3.83889E-318;
        r4 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1));
        if (r4 == 0) goto L_0x00e1;
    L_0x0090:
        r4 = r14.messageOwner;
        r7 = r4.via_bot_id;
        if (r7 != 0) goto L_0x00e1;
    L_0x0096:
        r4 = r4.via_bot_name;
        if (r4 != 0) goto L_0x00e1;
    L_0x009a:
        if (r3 == 0) goto L_0x00a1;
    L_0x009c:
        r3 = r3.bot;
        if (r3 == 0) goto L_0x00a1;
    L_0x00a0:
        goto L_0x00e1;
    L_0x00a1:
        r3 = r13.currentPosition;
        r4 = 32768; // 0x8000 float:4.5918E-41 double:1.61895E-319;
        if (r3 == 0) goto L_0x00d3;
    L_0x00a8:
        r3 = r13.currentMessagesGroup;
        if (r3 != 0) goto L_0x00ad;
    L_0x00ac:
        goto L_0x00d3;
    L_0x00ad:
        r3 = r3.messages;
        r3 = r3.size();
        r7 = 0;
    L_0x00b4:
        if (r7 >= r3) goto L_0x00e1;
    L_0x00b6:
        r8 = r13.currentMessagesGroup;
        r8 = r8.messages;
        r8 = r8.get(r7);
        r8 = (org.telegram.messenger.MessageObject) r8;
        r9 = r8.messageOwner;
        r9 = r9.flags;
        r9 = r9 & r4;
        if (r9 != 0) goto L_0x00d1;
    L_0x00c7:
        r8 = r8.isEditing();
        if (r8 == 0) goto L_0x00ce;
    L_0x00cd:
        goto L_0x00d1;
    L_0x00ce:
        r7 = r7 + 1;
        goto L_0x00b4;
    L_0x00d1:
        r3 = 1;
        goto L_0x00e2;
    L_0x00d3:
        r3 = r14.messageOwner;
        r3 = r3.flags;
        r3 = r3 & r4;
        if (r3 != 0) goto L_0x00d1;
    L_0x00da:
        r3 = r14.isEditing();
        if (r3 == 0) goto L_0x00e1;
    L_0x00e0:
        goto L_0x00d1;
    L_0x00e1:
        r3 = 0;
    L_0x00e2:
        r4 = r13.currentMessageObject;
        r7 = r4.scheduled;
        r8 = NUM; // 0x7ffffffe float:NaN double:1.0609978945E-314;
        if (r7 == 0) goto L_0x00f3;
    L_0x00eb:
        r4 = r4.messageOwner;
        r4 = r4.date;
        if (r4 != r8) goto L_0x00f3;
    L_0x00f1:
        r3 = r1;
        goto L_0x0137;
    L_0x00f3:
        r9 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        if (r3 == 0) goto L_0x0126;
    L_0x00f7:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = NUM; // 0x7f0e0403 float:1.887712E38 double:1.053162664E-314;
        r7 = "EditedMessage";
        r4 = org.telegram.messenger.LocaleController.getString(r7, r4);
        r3.append(r4);
        r4 = " ";
        r3.append(r4);
        r4 = org.telegram.messenger.LocaleController.getInstance();
        r4 = r4.formatterDay;
        r7 = r14.messageOwner;
        r7 = r7.date;
        r11 = (long) r7;
        r11 = r11 * r9;
        r4 = r4.format(r11);
        r3.append(r4);
        r3 = r3.toString();
        goto L_0x0137;
    L_0x0126:
        r3 = org.telegram.messenger.LocaleController.getInstance();
        r3 = r3.formatterDay;
        r4 = r14.messageOwner;
        r4 = r4.date;
        r11 = (long) r4;
        r11 = r11 * r9;
        r3 = r3.format(r11);
    L_0x0137:
        if (r0 == 0) goto L_0x014d;
    L_0x0139:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r7 = ", ";
        r4.append(r7);
        r4.append(r3);
        r3 = r4.toString();
        r13.currentTimeString = r3;
        goto L_0x014f;
    L_0x014d:
        r13.currentTimeString = r3;
    L_0x014f:
        r3 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r4 = r13.currentTimeString;
        r3 = r3.measureText(r4);
        r3 = (double) r3;
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r13.timeWidth = r3;
        r13.timeTextWidth = r3;
        r3 = r13.currentMessageObject;
        r4 = r3.scheduled;
        if (r4 == 0) goto L_0x0178;
    L_0x0167:
        r3 = r3.messageOwner;
        r3 = r3.date;
        if (r3 != r8) goto L_0x0178;
    L_0x016d:
        r3 = r13.timeWidth;
        r4 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r13.timeWidth = r3;
    L_0x0178:
        r3 = r14.messageOwner;
        r4 = r3.flags;
        r4 = r4 & 1024;
        if (r4 == 0) goto L_0x01bb;
    L_0x0180:
        r4 = new java.lang.Object[r5];
        r3 = r3.views;
        r3 = java.lang.Math.max(r5, r3);
        r2 = org.telegram.messenger.LocaleController.formatShortNumber(r3, r2);
        r4[r6] = r2;
        r2 = "%s";
        r2 = java.lang.String.format(r2, r4);
        r13.currentViewsString = r2;
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r3 = r13.currentViewsString;
        r2 = r2.measureText(r3);
        r2 = (double) r2;
        r2 = java.lang.Math.ceil(r2);
        r2 = (int) r2;
        r13.viewsTextWidth = r2;
        r2 = r13.timeWidth;
        r3 = r13.viewsTextWidth;
        r4 = org.telegram.ui.ActionBar.Theme.chat_msgInViewsDrawable;
        r4 = r4.getIntrinsicWidth();
        r3 = r3 + r4;
        r4 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 + r4;
        r2 = r2 + r3;
        r13.timeWidth = r2;
    L_0x01bb:
        r2 = r14.scheduled;
        if (r2 == 0) goto L_0x01d0;
    L_0x01bf:
        r2 = r14.isSendError();
        if (r2 == 0) goto L_0x01d0;
    L_0x01c5:
        r2 = r13.timeWidth;
        r3 = NUM; // 0x41900000 float:18.0 double:5.43450582E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r13.timeWidth = r2;
    L_0x01d0:
        if (r0 == 0) goto L_0x023b;
    L_0x01d2:
        r2 = r13.availableTimeWidth;
        if (r2 != 0) goto L_0x01de;
    L_0x01d6:
        r2 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r13.availableTimeWidth = r2;
    L_0x01de:
        r2 = r13.availableTimeWidth;
        r3 = r13.timeWidth;
        r2 = r2 - r3;
        r3 = r14.isOutOwner();
        if (r3 == 0) goto L_0x01fc;
    L_0x01e9:
        r14 = r14.type;
        r3 = 5;
        if (r14 != r3) goto L_0x01f5;
    L_0x01ee:
        r14 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
        goto L_0x01fb;
    L_0x01f5:
        r14 = NUM; // 0x42CLASSNAME float:96.0 double:5.532938244E-315;
        r14 = org.telegram.messenger.AndroidUtilities.dp(r14);
    L_0x01fb:
        r2 = r2 - r14;
    L_0x01fc:
        r14 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r3 = r0.length();
        r14 = r14.measureText(r0, r6, r3);
        r3 = (double) r14;
        r3 = java.lang.Math.ceil(r3);
        r14 = (int) r3;
        if (r14 <= r2) goto L_0x021d;
    L_0x020e:
        if (r2 > 0) goto L_0x0212;
    L_0x0210:
        r14 = 0;
        goto L_0x021e;
    L_0x0212:
        r14 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r1 = (float) r2;
        r3 = android.text.TextUtils.TruncateAt.END;
        r1 = android.text.TextUtils.ellipsize(r0, r14, r1, r3);
        r14 = r2;
        goto L_0x021e;
    L_0x021d:
        r1 = r0;
    L_0x021e:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r0.append(r1);
        r1 = r13.currentTimeString;
        r0.append(r1);
        r0 = r0.toString();
        r13.currentTimeString = r0;
        r0 = r13.timeTextWidth;
        r0 = r0 + r14;
        r13.timeTextWidth = r0;
        r0 = r13.timeWidth;
        r0 = r0 + r14;
        r13.timeWidth = r0;
    L_0x023b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.measureTime(org.telegram.messenger.MessageObject):void");
    }

    private boolean isDrawSelectionBackground() {
        return (isPressed() && this.isCheckPressed) || ((!this.isCheckPressed && this.isPressed) || this.isHighlighted);
    }

    private boolean isOpenChatByShare(MessageObject messageObject) {
        MessageFwdHeader messageFwdHeader = messageObject.messageOwner.fwd_from;
        return (messageFwdHeader == null || messageFwdHeader.saved_from_peer == null) ? false : true;
    }

    private boolean checkNeedDrawShareButton(MessageObject messageObject) {
        GroupedMessagePosition groupedMessagePosition = this.currentPosition;
        if (groupedMessagePosition != null && !groupedMessagePosition.last) {
            return false;
        }
        if (!(messageObject.messageOwner.fwd_from == null || messageObject.isOutOwner() || messageObject.messageOwner.fwd_from.saved_from_peer == null || messageObject.getDialogId() != ((long) UserConfig.getInstance(this.currentAccount).getClientUserId()))) {
            this.drwaShareGoIcon = true;
        }
        return messageObject.needDrawShareButton();
    }

    public boolean isInsideBackground(float f, float f2) {
        if (this.currentBackgroundDrawable != null) {
            int i = this.backgroundDrawableLeft;
            if (f >= ((float) i) && f <= ((float) (i + this.backgroundDrawableRight))) {
                return true;
            }
        }
        return false;
    }

    private void updateCurrentUserAndChat() {
        MessagesController instance = MessagesController.getInstance(this.currentAccount);
        MessageFwdHeader messageFwdHeader = this.currentMessageObject.messageOwner.fwd_from;
        int clientUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
        if (messageFwdHeader == null || messageFwdHeader.channel_id == 0 || this.currentMessageObject.getDialogId() != ((long) clientUserId)) {
            if (messageFwdHeader != null) {
                Peer peer = messageFwdHeader.saved_from_peer;
                if (peer != null) {
                    clientUserId = peer.user_id;
                    int i;
                    if (clientUserId != 0) {
                        i = messageFwdHeader.from_id;
                        if (i != 0) {
                            this.currentUser = instance.getUser(Integer.valueOf(i));
                            return;
                        } else {
                            this.currentUser = instance.getUser(Integer.valueOf(clientUserId));
                            return;
                        }
                    } else if (peer.channel_id != 0) {
                        if (this.currentMessageObject.isSavedFromMegagroup()) {
                            clientUserId = messageFwdHeader.from_id;
                            if (clientUserId != 0) {
                                this.currentUser = instance.getUser(Integer.valueOf(clientUserId));
                                return;
                            }
                        }
                        this.currentChat = instance.getChat(Integer.valueOf(messageFwdHeader.saved_from_peer.channel_id));
                        return;
                    } else {
                        clientUserId = peer.chat_id;
                        if (clientUserId != 0) {
                            i = messageFwdHeader.from_id;
                            if (i != 0) {
                                this.currentUser = instance.getUser(Integer.valueOf(i));
                                return;
                            } else {
                                this.currentChat = instance.getChat(Integer.valueOf(clientUserId));
                                return;
                            }
                        }
                        return;
                    }
                }
            }
            if (messageFwdHeader != null && messageFwdHeader.from_id != 0 && messageFwdHeader.channel_id == 0 && this.currentMessageObject.getDialogId() == ((long) clientUserId)) {
                this.currentUser = instance.getUser(Integer.valueOf(messageFwdHeader.from_id));
                return;
            } else if (messageFwdHeader != null && !TextUtils.isEmpty(messageFwdHeader.from_name) && this.currentMessageObject.getDialogId() == ((long) clientUserId)) {
                this.currentUser = new TL_user();
                this.currentUser.first_name = messageFwdHeader.from_name;
                return;
            } else if (this.currentMessageObject.isFromUser()) {
                this.currentUser = instance.getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
                return;
            } else {
                Message message = this.currentMessageObject.messageOwner;
                clientUserId = message.from_id;
                if (clientUserId < 0) {
                    this.currentChat = instance.getChat(Integer.valueOf(-clientUserId));
                    return;
                } else if (message.post) {
                    this.currentChat = instance.getChat(Integer.valueOf(message.to_id.channel_id));
                    return;
                } else {
                    return;
                }
            }
        }
        this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(messageFwdHeader.channel_id));
    }

    /* JADX WARNING: Removed duplicated region for block: B:280:0x0855 A:{Catch:{ Exception -> 0x08a4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0852 A:{Catch:{ Exception -> 0x08a4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0860 A:{Catch:{ Exception -> 0x08a4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x08b0 A:{Catch:{ Exception -> 0x08fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x08ad A:{Catch:{ Exception -> 0x08fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x08bb A:{Catch:{ Exception -> 0x08fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x023c  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0223  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x024f  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x025a  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x03d4 A:{Catch:{ Exception -> 0x03d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x03a3 A:{Catch:{ Exception -> 0x03d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x03e5  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0412  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0430  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x044e  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0480  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0460  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x053e  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x04f5  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x0620 A:{Catch:{ Exception -> 0x062c }} */
    /* JADX WARNING: Removed duplicated region for block: B:301:0x0901  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0638  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x016b  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x017f A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01a3  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0223  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x023c  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x024f  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x025a  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x036f A:{Catch:{ Exception -> 0x03d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x03a3 A:{Catch:{ Exception -> 0x03d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x03d4 A:{Catch:{ Exception -> 0x03d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x03e5  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0412  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0430  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x044e  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0460  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0480  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x04f5  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x053e  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x0620 A:{Catch:{ Exception -> 0x062c }} */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0638  */
    /* JADX WARNING: Removed duplicated region for block: B:301:0x0901  */
    private void setMessageObjectInternal(org.telegram.messenger.MessageObject r40) {
        /*
        r39 = this;
        r1 = r39;
        r2 = r40;
        r0 = r2.messageOwner;
        r0 = r0.flags;
        r0 = r0 & 1024;
        r3 = 1;
        if (r0 == 0) goto L_0x0026;
    L_0x000d:
        r0 = r1.currentMessageObject;
        r4 = r0.scheduled;
        if (r4 != 0) goto L_0x0026;
    L_0x0013:
        r0 = r0.viewsReloaded;
        if (r0 != 0) goto L_0x0026;
    L_0x0017:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r4 = r1.currentMessageObject;
        r0.addToViewsQueue(r4);
        r0 = r1.currentMessageObject;
        r0.viewsReloaded = r3;
    L_0x0026:
        r39.updateCurrentUserAndChat();
        r0 = r1.isAvatarVisible;
        r4 = 0;
        r5 = 0;
        if (r0 == 0) goto L_0x009b;
    L_0x002f:
        r0 = r1.currentUser;
        if (r0 == 0) goto L_0x0059;
    L_0x0033:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x003c;
    L_0x0037:
        r0 = r0.photo_small;
        r1.currentPhoto = r0;
        goto L_0x003e;
    L_0x003c:
        r1.currentPhoto = r4;
    L_0x003e:
        r0 = r1.avatarDrawable;
        r6 = r1.currentUser;
        r0.setInfo(r6);
        r7 = r1.avatarImage;
        r0 = r1.currentUser;
        r8 = org.telegram.messenger.ImageLocation.getForUser(r0, r5);
        r10 = r1.avatarDrawable;
        r11 = 0;
        r12 = r1.currentUser;
        r13 = 0;
        r9 = "50_50";
        r7.setImage(r8, r9, r10, r11, r12, r13);
        goto L_0x009d;
    L_0x0059:
        r0 = r1.currentChat;
        if (r0 == 0) goto L_0x0083;
    L_0x005d:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x0066;
    L_0x0061:
        r0 = r0.photo_small;
        r1.currentPhoto = r0;
        goto L_0x0068;
    L_0x0066:
        r1.currentPhoto = r4;
    L_0x0068:
        r0 = r1.avatarDrawable;
        r6 = r1.currentChat;
        r0.setInfo(r6);
        r7 = r1.avatarImage;
        r0 = r1.currentChat;
        r8 = org.telegram.messenger.ImageLocation.getForChat(r0, r5);
        r10 = r1.avatarDrawable;
        r11 = 0;
        r12 = r1.currentChat;
        r13 = 0;
        r9 = "50_50";
        r7.setImage(r8, r9, r10, r11, r12, r13);
        goto L_0x009d;
    L_0x0083:
        r1.currentPhoto = r4;
        r0 = r1.avatarDrawable;
        r6 = r2.messageOwner;
        r6 = r6.from_id;
        r0.setInfo(r6, r4, r4);
        r7 = r1.avatarImage;
        r8 = 0;
        r9 = 0;
        r10 = r1.avatarDrawable;
        r11 = 0;
        r12 = 0;
        r13 = 0;
        r7.setImage(r8, r9, r10, r11, r12, r13);
        goto L_0x009d;
    L_0x009b:
        r1.currentPhoto = r4;
    L_0x009d:
        r39.measureTime(r40);
        r1.namesOffset = r5;
        r0 = r2.messageOwner;
        r6 = r0.via_bot_id;
        r7 = NUM; // 0x7f0e0b98 float:1.8881057E38 double:1.053163623E-314;
        r8 = "ViaBot";
        r9 = 2;
        if (r6 == 0) goto L_0x0108;
    L_0x00ae:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r6 = r2.messageOwner;
        r6 = r6.via_bot_id;
        r6 = java.lang.Integer.valueOf(r6);
        r0 = r0.getUser(r6);
        if (r0 == 0) goto L_0x0150;
    L_0x00c2:
        r6 = r0.username;
        if (r6 == 0) goto L_0x0150;
    L_0x00c6:
        r6 = r6.length();
        if (r6 <= 0) goto L_0x0150;
    L_0x00cc:
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r10 = "@";
        r6.append(r10);
        r10 = r0.username;
        r6.append(r10);
        r6 = r6.toString();
        r10 = new java.lang.Object[r9];
        r11 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r10[r5] = r11;
        r10[r3] = r6;
        r11 = " %s <b>%s</b>";
        r10 = java.lang.String.format(r11, r10);
        r10 = org.telegram.messenger.AndroidUtilities.replaceTags(r10);
        r11 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r12 = r10.length();
        r11 = r11.measureText(r10, r5, r12);
        r11 = (double) r11;
        r11 = java.lang.Math.ceil(r11);
        r11 = (int) r11;
        r1.viaWidth = r11;
        r1.currentViaBotUser = r0;
        goto L_0x0152;
    L_0x0108:
        r0 = r0.via_bot_name;
        if (r0 == 0) goto L_0x0150;
    L_0x010c:
        r0 = r0.length();
        if (r0 <= 0) goto L_0x0150;
    L_0x0112:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r6 = "@";
        r0.append(r6);
        r6 = r2.messageOwner;
        r6 = r6.via_bot_name;
        r0.append(r6);
        r0 = r0.toString();
        r6 = new java.lang.Object[r9];
        r10 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r6[r5] = r10;
        r6[r3] = r0;
        r10 = " %s <b>%s</b>";
        r6 = java.lang.String.format(r10, r6);
        r6 = org.telegram.messenger.AndroidUtilities.replaceTags(r6);
        r10 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r11 = r6.length();
        r10 = r10.measureText(r6, r5, r11);
        r10 = (double) r10;
        r10 = java.lang.Math.ceil(r10);
        r10 = (int) r10;
        r1.viaWidth = r10;
        r10 = r6;
        r6 = r0;
        goto L_0x0152;
    L_0x0150:
        r6 = r4;
        r10 = r6;
    L_0x0152:
        r0 = r1.drawName;
        if (r0 == 0) goto L_0x0164;
    L_0x0156:
        r0 = r1.isChat;
        if (r0 == 0) goto L_0x0164;
    L_0x015a:
        r0 = r1.currentMessageObject;
        r0 = r0.isOutOwner();
        if (r0 != 0) goto L_0x0164;
    L_0x0162:
        r0 = 1;
        goto L_0x0165;
    L_0x0164:
        r0 = 0;
    L_0x0165:
        r11 = r2.messageOwner;
        r11 = r11.fwd_from;
        if (r11 == 0) goto L_0x0171;
    L_0x016b:
        r11 = r2.type;
        r12 = 14;
        if (r11 != r12) goto L_0x0175;
    L_0x0171:
        if (r6 == 0) goto L_0x0175;
    L_0x0173:
        r11 = 1;
        goto L_0x0176;
    L_0x0175:
        r11 = 0;
    L_0x0176:
        r12 = "fonts/rmedium.ttf";
        r15 = 32;
        r14 = 10;
        r13 = 5;
        if (r0 != 0) goto L_0x018b;
    L_0x017f:
        if (r11 == 0) goto L_0x0182;
    L_0x0181:
        goto L_0x018b;
    L_0x0182:
        r1.currentNameString = r4;
        r1.nameLayout = r4;
        r1.nameWidth = r5;
        r3 = r4;
        goto L_0x03e7;
    L_0x018b:
        r1.drawNameLayout = r3;
        r4 = r39.getMaxNameWidth();
        r1.nameWidth = r4;
        r4 = r1.nameWidth;
        if (r4 >= 0) goto L_0x019f;
    L_0x0197:
        r4 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r1.nameWidth = r4;
    L_0x019f:
        r4 = r1.isMegagroup;
        if (r4 == 0) goto L_0x01d0;
    L_0x01a3:
        r4 = r1.currentChat;
        if (r4 == 0) goto L_0x01d0;
    L_0x01a7:
        r4 = r1.currentMessageObject;
        r4 = r4.isForwardedChannelPost();
        if (r4 == 0) goto L_0x01d0;
    L_0x01af:
        r4 = NUM; // 0x7f0e03be float:1.887698E38 double:1.05316263E-314;
        r9 = "DiscussChannel";
        r4 = org.telegram.messenger.LocaleController.getString(r9, r4);
        r9 = org.telegram.ui.ActionBar.Theme.chat_adminPaint;
        r9 = r9.measureText(r4);
        r21 = r4;
        r3 = (double) r9;
        r3 = java.lang.Math.ceil(r3);
        r3 = (int) r3;
        r4 = r1.nameWidth;
        r4 = r4 - r3;
        r1.nameWidth = r4;
        r23 = r21;
        r21 = r8;
        goto L_0x0221;
    L_0x01d0:
        r3 = r1.currentUser;
        if (r3 == 0) goto L_0x021c;
    L_0x01d4:
        r3 = r1.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 != 0) goto L_0x021c;
    L_0x01dc:
        r3 = r1.currentMessageObject;
        r3 = r3.isAnyKindOfSticker();
        if (r3 != 0) goto L_0x021c;
    L_0x01e4:
        r3 = r1.currentMessageObject;
        r3 = r3.type;
        if (r3 == r13) goto L_0x021c;
    L_0x01ea:
        r3 = r1.delegate;
        r4 = r1.currentUser;
        r4 = r4.id;
        r3 = r3.getAdminRank(r4);
        if (r3 == 0) goto L_0x021c;
    L_0x01f6:
        r4 = r3.length();
        if (r4 != 0) goto L_0x0205;
    L_0x01fc:
        r3 = NUM; // 0x7f0e029b float:1.887639E38 double:1.053162486E-314;
        r4 = "ChatAdmin";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
    L_0x0205:
        r4 = r3;
        r3 = org.telegram.ui.ActionBar.Theme.chat_adminPaint;
        r3 = r3.measureText(r4);
        r21 = r8;
        r7 = (double) r3;
        r7 = java.lang.Math.ceil(r7);
        r3 = (int) r7;
        r7 = r1.nameWidth;
        r7 = r7 - r3;
        r1.nameWidth = r7;
        r23 = r4;
        goto L_0x0221;
    L_0x021c:
        r21 = r8;
        r3 = 0;
        r23 = 0;
    L_0x0221:
        if (r0 == 0) goto L_0x023c;
    L_0x0223:
        r0 = r1.currentUser;
        if (r0 == 0) goto L_0x022e;
    L_0x0227:
        r0 = org.telegram.messenger.UserObject.getUserName(r0);
        r1.currentNameString = r0;
        goto L_0x0240;
    L_0x022e:
        r0 = r1.currentChat;
        if (r0 == 0) goto L_0x0237;
    L_0x0232:
        r0 = r0.title;
        r1.currentNameString = r0;
        goto L_0x0240;
    L_0x0237:
        r0 = "DELETED";
        r1.currentNameString = r0;
        goto L_0x0240;
    L_0x023c:
        r0 = "";
        r1.currentNameString = r0;
    L_0x0240:
        r0 = r1.currentNameString;
        r0 = r0.replace(r14, r15);
        r4 = org.telegram.ui.ActionBar.Theme.chat_namePaint;
        r7 = r1.nameWidth;
        if (r11 == 0) goto L_0x024f;
    L_0x024c:
        r8 = r1.viaWidth;
        goto L_0x0250;
    L_0x024f:
        r8 = 0;
    L_0x0250:
        r7 = r7 - r8;
        r7 = (float) r7;
        r8 = android.text.TextUtils.TruncateAt.END;
        r0 = android.text.TextUtils.ellipsize(r0, r4, r7, r8);
        if (r11 == 0) goto L_0x034a;
    L_0x025a:
        r4 = org.telegram.ui.ActionBar.Theme.chat_namePaint;
        r7 = r0.length();
        r4 = r4.measureText(r0, r5, r7);
        r7 = (double) r4;
        r7 = java.lang.Math.ceil(r7);
        r4 = (int) r7;
        r1.viaNameWidth = r4;
        r4 = r1.viaNameWidth;
        if (r4 == 0) goto L_0x0279;
    L_0x0270:
        r7 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r4 = r4 + r7;
        r1.viaNameWidth = r4;
    L_0x0279:
        r4 = r1.currentMessageObject;
        r4 = r4.shouldDrawWithoutBackground();
        if (r4 == 0) goto L_0x028d;
    L_0x0281:
        r4 = "chat_stickerViaBotNameText";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
    L_0x0287:
        r8 = r21;
        r7 = NUM; // 0x7f0e0b98 float:1.8881057E38 double:1.053163623E-314;
        goto L_0x029f;
    L_0x028d:
        r4 = r1.currentMessageObject;
        r4 = r4.isOutOwner();
        if (r4 == 0) goto L_0x0298;
    L_0x0295:
        r4 = "chat_outViaBotNameText";
        goto L_0x029a;
    L_0x0298:
        r4 = "chat_inViaBotNameText";
    L_0x029a:
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        goto L_0x0287;
    L_0x029f:
        r11 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r7 = r1.currentNameString;
        r7 = r7.length();
        if (r7 <= 0) goto L_0x0301;
    L_0x02ab:
        r7 = new android.text.SpannableStringBuilder;
        r9 = 3;
        r9 = new java.lang.Object[r9];
        r9[r5] = r0;
        r20 = 1;
        r9[r20] = r11;
        r19 = 2;
        r9[r19] = r6;
        r13 = "%s %s %s";
        r9 = java.lang.String.format(r13, r9);
        r7.<init>(r9);
        r9 = new org.telegram.ui.Components.TypefaceSpan;
        r13 = android.graphics.Typeface.DEFAULT;
        r9.<init>(r13, r5, r4);
        r1.viaSpan1 = r9;
        r13 = r0.length();
        r13 = r13 + 1;
        r22 = r0.length();
        r22 = r22 + 1;
        r24 = r11.length();
        r14 = r22 + r24;
        r15 = 33;
        r7.setSpan(r9, r13, r14, r15);
        r9 = new org.telegram.ui.Components.TypefaceSpan;
        r13 = org.telegram.messenger.AndroidUtilities.getTypeface(r12);
        r9.<init>(r13, r5, r4);
        r1.viaSpan2 = r9;
        r0 = r0.length();
        r13 = 2;
        r0 = r0 + r13;
        r4 = r11.length();
        r0 = r0 + r4;
        r4 = r7.length();
        r7.setSpan(r9, r0, r4, r15);
        goto L_0x033e;
    L_0x0301:
        r13 = 2;
        r7 = new android.text.SpannableStringBuilder;
        r0 = new java.lang.Object[r13];
        r0[r5] = r11;
        r9 = 1;
        r0[r9] = r6;
        r13 = "%s %s";
        r0 = java.lang.String.format(r13, r0);
        r7.<init>(r0);
        r0 = new org.telegram.ui.Components.TypefaceSpan;
        r13 = android.graphics.Typeface.DEFAULT;
        r0.<init>(r13, r5, r4);
        r1.viaSpan1 = r0;
        r13 = r11.length();
        r13 = r13 + r9;
        r14 = 33;
        r7.setSpan(r0, r5, r13, r14);
        r0 = new org.telegram.ui.Components.TypefaceSpan;
        r13 = org.telegram.messenger.AndroidUtilities.getTypeface(r12);
        r0.<init>(r13, r5, r4);
        r1.viaSpan2 = r0;
        r4 = r11.length();
        r4 = r4 + r9;
        r9 = r7.length();
        r7.setSpan(r0, r4, r9, r14);
    L_0x033e:
        r0 = org.telegram.ui.ActionBar.Theme.chat_namePaint;
        r4 = r1.nameWidth;
        r4 = (float) r4;
        r9 = android.text.TextUtils.TruncateAt.END;
        r0 = android.text.TextUtils.ellipsize(r7, r0, r4, r9);
        goto L_0x034c;
    L_0x034a:
        r8 = r21;
    L_0x034c:
        r31 = r0;
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x03d8 }
        r32 = org.telegram.ui.ActionBar.Theme.chat_namePaint;	 Catch:{ Exception -> 0x03d8 }
        r4 = r1.nameWidth;	 Catch:{ Exception -> 0x03d8 }
        r7 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r7);	 Catch:{ Exception -> 0x03d8 }
        r33 = r4 + r9;
        r34 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x03d8 }
        r35 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r36 = 0;
        r37 = 0;
        r30 = r0;
        r30.<init>(r31, r32, r33, r34, r35, r36, r37);	 Catch:{ Exception -> 0x03d8 }
        r1.nameLayout = r0;	 Catch:{ Exception -> 0x03d8 }
        r0 = r1.nameLayout;	 Catch:{ Exception -> 0x03d8 }
        if (r0 == 0) goto L_0x039f;
    L_0x036f:
        r0 = r1.nameLayout;	 Catch:{ Exception -> 0x03d8 }
        r0 = r0.getLineCount();	 Catch:{ Exception -> 0x03d8 }
        if (r0 <= 0) goto L_0x039f;
    L_0x0377:
        r0 = r1.nameLayout;	 Catch:{ Exception -> 0x03d8 }
        r0 = r0.getLineWidth(r5);	 Catch:{ Exception -> 0x03d8 }
        r13 = (double) r0;	 Catch:{ Exception -> 0x03d8 }
        r13 = java.lang.Math.ceil(r13);	 Catch:{ Exception -> 0x03d8 }
        r0 = (int) r13;	 Catch:{ Exception -> 0x03d8 }
        r1.nameWidth = r0;	 Catch:{ Exception -> 0x03d8 }
        r0 = r40.isAnyKindOfSticker();	 Catch:{ Exception -> 0x03d8 }
        if (r0 != 0) goto L_0x0396;
    L_0x038b:
        r0 = r1.namesOffset;	 Catch:{ Exception -> 0x03d8 }
        r4 = NUM; // 0x41980000 float:19.0 double:5.43709615E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x03d8 }
        r0 = r0 + r4;
        r1.namesOffset = r0;	 Catch:{ Exception -> 0x03d8 }
    L_0x0396:
        r0 = r1.nameLayout;	 Catch:{ Exception -> 0x03d8 }
        r0 = r0.getLineLeft(r5);	 Catch:{ Exception -> 0x03d8 }
        r1.nameOffsetX = r0;	 Catch:{ Exception -> 0x03d8 }
        goto L_0x03a1;
    L_0x039f:
        r1.nameWidth = r5;	 Catch:{ Exception -> 0x03d8 }
    L_0x03a1:
        if (r23 == 0) goto L_0x03d4;
    L_0x03a3:
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x03d8 }
        r24 = org.telegram.ui.ActionBar.Theme.chat_adminPaint;	 Catch:{ Exception -> 0x03d8 }
        r4 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x03d8 }
        r25 = r3 + r7;
        r26 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x03d8 }
        r27 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r28 = 0;
        r29 = 0;
        r22 = r0;
        r22.<init>(r23, r24, r25, r26, r27, r28, r29);	 Catch:{ Exception -> 0x03d8 }
        r1.adminLayout = r0;	 Catch:{ Exception -> 0x03d8 }
        r0 = r1.nameWidth;	 Catch:{ Exception -> 0x03d8 }
        r0 = (float) r0;	 Catch:{ Exception -> 0x03d8 }
        r3 = r1.adminLayout;	 Catch:{ Exception -> 0x03d8 }
        r3 = r3.getLineWidth(r5);	 Catch:{ Exception -> 0x03d8 }
        r4 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x03d8 }
        r4 = (float) r7;	 Catch:{ Exception -> 0x03d8 }
        r3 = r3 + r4;
        r0 = r0 + r3;
        r0 = (int) r0;	 Catch:{ Exception -> 0x03d8 }
        r1.nameWidth = r0;	 Catch:{ Exception -> 0x03d8 }
        goto L_0x03dc;
    L_0x03d4:
        r3 = 0;
        r1.adminLayout = r3;	 Catch:{ Exception -> 0x03d8 }
        goto L_0x03dc;
    L_0x03d8:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x03dc:
        r0 = r1.currentNameString;
        r0 = r0.length();
        r3 = 0;
        if (r0 != 0) goto L_0x03e7;
    L_0x03e5:
        r1.currentNameString = r3;
    L_0x03e7:
        r1.currentForwardUser = r3;
        r1.currentForwardNameString = r3;
        r1.currentForwardChannel = r3;
        r1.currentForwardName = r3;
        r0 = r1.forwardedNameLayout;
        r0[r5] = r3;
        r4 = 1;
        r0[r4] = r3;
        r1.forwardedNameWidth = r5;
        r0 = r1.drawForwardedName;
        if (r0 == 0) goto L_0x0630;
    L_0x03fc:
        r0 = r40.needDrawForwarded();
        if (r0 == 0) goto L_0x0630;
    L_0x0402:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x040a;
    L_0x0406:
        r0 = r0.minY;
        if (r0 != 0) goto L_0x0630;
    L_0x040a:
        r0 = r2.messageOwner;
        r0 = r0.fwd_from;
        r0 = r0.channel_id;
        if (r0 == 0) goto L_0x0428;
    L_0x0412:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r3 = r2.messageOwner;
        r3 = r3.fwd_from;
        r3 = r3.channel_id;
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r0.getChat(r3);
        r1.currentForwardChannel = r0;
    L_0x0428:
        r0 = r2.messageOwner;
        r0 = r0.fwd_from;
        r0 = r0.from_id;
        if (r0 == 0) goto L_0x0446;
    L_0x0430:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r3 = r2.messageOwner;
        r3 = r3.fwd_from;
        r3 = r3.from_id;
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r0.getUser(r3);
        r1.currentForwardUser = r0;
    L_0x0446:
        r0 = r2.messageOwner;
        r0 = r0.fwd_from;
        r0 = r0.from_name;
        if (r0 == 0) goto L_0x0450;
    L_0x044e:
        r1.currentForwardName = r0;
    L_0x0450:
        r0 = r1.currentForwardUser;
        if (r0 != 0) goto L_0x045c;
    L_0x0454:
        r0 = r1.currentForwardChannel;
        if (r0 != 0) goto L_0x045c;
    L_0x0458:
        r0 = r1.currentForwardName;
        if (r0 == 0) goto L_0x0630;
    L_0x045c:
        r0 = r1.currentForwardChannel;
        if (r0 == 0) goto L_0x0480;
    L_0x0460:
        r3 = r1.currentForwardUser;
        if (r3 == 0) goto L_0x047b;
    L_0x0464:
        r4 = 2;
        r7 = new java.lang.Object[r4];
        r0 = r0.title;
        r7[r5] = r0;
        r0 = org.telegram.messenger.UserObject.getUserName(r3);
        r3 = 1;
        r7[r3] = r0;
        r0 = "%s (%s)";
        r0 = java.lang.String.format(r0, r7);
        r1.currentForwardNameString = r0;
        goto L_0x0491;
    L_0x047b:
        r0 = r0.title;
        r1.currentForwardNameString = r0;
        goto L_0x0491;
    L_0x0480:
        r0 = r1.currentForwardUser;
        if (r0 == 0) goto L_0x048b;
    L_0x0484:
        r0 = org.telegram.messenger.UserObject.getUserName(r0);
        r1.currentForwardNameString = r0;
        goto L_0x0491;
    L_0x048b:
        r0 = r1.currentForwardName;
        if (r0 == 0) goto L_0x0491;
    L_0x048f:
        r1.currentForwardNameString = r0;
    L_0x0491:
        r0 = r39.getMaxNameWidth();
        r1.forwardedNameWidth = r0;
        r0 = NUM; // 0x7f0e0504 float:1.8877642E38 double:1.053162791E-314;
        r3 = "From";
        r0 = org.telegram.messenger.LocaleController.getString(r3, r0);
        r3 = NUM; // 0x7f0e050c float:1.8877658E38 double:1.053162795E-314;
        r4 = "FromFormatted";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
        r4 = "%1$s";
        r4 = r3.indexOf(r4);
        r7 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r9.append(r0);
        r0 = " ";
        r9.append(r0);
        r0 = r9.toString();
        r0 = r7.measureText(r0);
        r13 = (double) r0;
        r13 = java.lang.Math.ceil(r13);
        r0 = (int) r13;
        r7 = r1.currentForwardNameString;
        r9 = 32;
        r11 = 10;
        r7 = r7.replace(r11, r9);
        r9 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r11 = r1.forwardedNameWidth;
        r11 = r11 - r0;
        r13 = r1.viaWidth;
        r11 = r11 - r13;
        r11 = (float) r11;
        r13 = android.text.TextUtils.TruncateAt.END;
        r7 = android.text.TextUtils.ellipsize(r7, r9, r11, r13);
        r9 = 1;
        r11 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x04ef }
        r11[r5] = r7;	 Catch:{ Exception -> 0x04ef }
        r9 = java.lang.String.format(r3, r11);	 Catch:{ Exception -> 0x04ef }
        goto L_0x04f3;
    L_0x04ef:
        r9 = r7.toString();
    L_0x04f3:
        if (r10 == 0) goto L_0x053e;
    L_0x04f5:
        r3 = new android.text.SpannableStringBuilder;
        r10 = 3;
        r10 = new java.lang.Object[r10];
        r10[r5] = r9;
        r11 = NUM; // 0x7f0e0b98 float:1.8881057E38 double:1.053163623E-314;
        r8 = org.telegram.messenger.LocaleController.getString(r8, r11);
        r11 = 1;
        r10[r11] = r8;
        r8 = 2;
        r10[r8] = r6;
        r8 = "%s %s %s";
        r8 = java.lang.String.format(r8, r10);
        r3.<init>(r8);
        r8 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;
        r8 = r8.measureText(r9);
        r8 = (double) r8;
        r8 = java.lang.Math.ceil(r8);
        r8 = (int) r8;
        r1.viaNameWidth = r8;
        r8 = new org.telegram.ui.Components.TypefaceSpan;
        r9 = org.telegram.messenger.AndroidUtilities.getTypeface(r12);
        r8.<init>(r9);
        r9 = r3.length();
        r6 = r6.length();
        r9 = r9 - r6;
        r6 = 1;
        r9 = r9 - r6;
        r10 = r3.length();
        r11 = 33;
        r3.setSpan(r8, r9, r10, r11);
        goto L_0x054d;
    L_0x053e:
        r6 = 1;
        r8 = new android.text.SpannableStringBuilder;
        r9 = new java.lang.Object[r6];
        r9[r5] = r7;
        r3 = java.lang.String.format(r3, r9);
        r8.<init>(r3);
        r3 = r8;
    L_0x054d:
        r6 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;
        r8 = r7.length();
        r6 = r6.measureText(r7, r5, r8);
        r8 = (double) r6;
        r8 = java.lang.Math.ceil(r8);
        r6 = (int) r8;
        r8 = 2;
        r6 = r6 / r8;
        r0 = r0 + r6;
        r1.forwardNameCenterX = r0;
        if (r4 < 0) goto L_0x0583;
    L_0x0564:
        r0 = r1.currentForwardName;
        if (r0 == 0) goto L_0x0570;
    L_0x0568:
        r0 = r2.messageOwner;
        r0 = r0.fwd_from;
        r0 = r0.from_id;
        if (r0 == 0) goto L_0x0583;
    L_0x0570:
        r0 = new org.telegram.ui.Components.TypefaceSpan;
        r6 = org.telegram.messenger.AndroidUtilities.getTypeface(r12);
        r0.<init>(r6);
        r6 = r7.length();
        r6 = r6 + r4;
        r7 = 33;
        r3.setSpan(r0, r4, r6, r7);
    L_0x0583:
        r0 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;
        r4 = r1.forwardedNameWidth;
        r4 = (float) r4;
        r6 = android.text.TextUtils.TruncateAt.END;
        r8 = android.text.TextUtils.ellipsize(r3, r0, r4, r6);
        r0 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r3 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x062c }
        r9 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;	 Catch:{ Exception -> 0x062c }
        r4 = r1.forwardedNameWidth;	 Catch:{ Exception -> 0x062c }
        r6 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x062c }
        r10 = r4 + r7;
        r11 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x062c }
        r12 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r13 = 0;
        r14 = 0;
        r7 = r3;
        r7.<init>(r8, r9, r10, r11, r12, r13, r14);	 Catch:{ Exception -> 0x062c }
        r4 = 1;
        r0[r4] = r3;	 Catch:{ Exception -> 0x062c }
        r0 = "ForwardedMessage";
        r3 = NUM; // 0x7f0e04d8 float:1.8877553E38 double:1.0531627693E-314;
        r0 = org.telegram.messenger.LocaleController.getString(r0, r3);	 Catch:{ Exception -> 0x062c }
        r0 = org.telegram.messenger.AndroidUtilities.replaceTags(r0);	 Catch:{ Exception -> 0x062c }
        r3 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;	 Catch:{ Exception -> 0x062c }
        r4 = r1.forwardedNameWidth;	 Catch:{ Exception -> 0x062c }
        r4 = (float) r4;	 Catch:{ Exception -> 0x062c }
        r6 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x062c }
        r8 = android.text.TextUtils.ellipsize(r0, r3, r4, r6);	 Catch:{ Exception -> 0x062c }
        r0 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r3 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x062c }
        r9 = org.telegram.ui.ActionBar.Theme.chat_forwardNamePaint;	 Catch:{ Exception -> 0x062c }
        r4 = r1.forwardedNameWidth;	 Catch:{ Exception -> 0x062c }
        r6 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x062c }
        r10 = r4 + r6;
        r11 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x062c }
        r12 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r13 = 0;
        r14 = 0;
        r7 = r3;
        r7.<init>(r8, r9, r10, r11, r12, r13, r14);	 Catch:{ Exception -> 0x062c }
        r0[r5] = r3;	 Catch:{ Exception -> 0x062c }
        r0 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r0 = r0[r5];	 Catch:{ Exception -> 0x062c }
        r0 = r0.getLineWidth(r5);	 Catch:{ Exception -> 0x062c }
        r3 = (double) r0;	 Catch:{ Exception -> 0x062c }
        r3 = java.lang.Math.ceil(r3);	 Catch:{ Exception -> 0x062c }
        r0 = (int) r3;	 Catch:{ Exception -> 0x062c }
        r3 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r4 = 1;
        r3 = r3[r4];	 Catch:{ Exception -> 0x062c }
        r3 = r3.getLineWidth(r5);	 Catch:{ Exception -> 0x062c }
        r3 = (double) r3;	 Catch:{ Exception -> 0x062c }
        r3 = java.lang.Math.ceil(r3);	 Catch:{ Exception -> 0x062c }
        r3 = (int) r3;	 Catch:{ Exception -> 0x062c }
        r0 = java.lang.Math.max(r0, r3);	 Catch:{ Exception -> 0x062c }
        r1.forwardedNameWidth = r0;	 Catch:{ Exception -> 0x062c }
        r0 = r1.forwardNameOffsetX;	 Catch:{ Exception -> 0x062c }
        r3 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r3 = r3[r5];	 Catch:{ Exception -> 0x062c }
        r3 = r3.getLineLeft(r5);	 Catch:{ Exception -> 0x062c }
        r0[r5] = r3;	 Catch:{ Exception -> 0x062c }
        r0 = r1.forwardNameOffsetX;	 Catch:{ Exception -> 0x062c }
        r3 = r1.forwardedNameLayout;	 Catch:{ Exception -> 0x062c }
        r4 = 1;
        r3 = r3[r4];	 Catch:{ Exception -> 0x062c }
        r3 = r3.getLineLeft(r5);	 Catch:{ Exception -> 0x062c }
        r0[r4] = r3;	 Catch:{ Exception -> 0x062c }
        r0 = r2.type;	 Catch:{ Exception -> 0x062c }
        r3 = 5;
        if (r0 == r3) goto L_0x0630;
    L_0x0620:
        r0 = r1.namesOffset;	 Catch:{ Exception -> 0x062c }
        r3 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x062c }
        r0 = r0 + r3;
        r1.namesOffset = r0;	 Catch:{ Exception -> 0x062c }
        goto L_0x0630;
    L_0x062c:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0630:
        r0 = r40.hasValidReplyMessageObject();
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        if (r0 == 0) goto L_0x0901;
    L_0x0638:
        r0 = r1.currentPosition;
        if (r0 == 0) goto L_0x0640;
    L_0x063c:
        r0 = r0.minY;
        if (r0 != 0) goto L_0x0996;
    L_0x0640:
        r0 = r40.isAnyKindOfSticker();
        if (r0 != 0) goto L_0x0665;
    L_0x0646:
        r0 = r2.type;
        r4 = 5;
        if (r0 == r4) goto L_0x0665;
    L_0x064b:
        r0 = r1.namesOffset;
        r4 = NUM; // 0x42280000 float:42.0 double:5.483722033E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        r1.namesOffset = r0;
        r0 = r2.type;
        if (r0 == 0) goto L_0x0665;
    L_0x065a:
        r0 = r1.namesOffset;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        r1.namesOffset = r0;
    L_0x0665:
        r0 = r39.getMaxNameWidth();
        r4 = r40.shouldDrawWithoutBackground();
        if (r4 != 0) goto L_0x0675;
    L_0x066f:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r4;
        goto L_0x0681;
    L_0x0675:
        r4 = r2.type;
        r6 = 5;
        if (r4 != r6) goto L_0x0681;
    L_0x067a:
        r4 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
    L_0x0681:
        r4 = r2.replyMessageObject;
        r4 = r4.photoThumbs2;
        r6 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r6 = r2.replyMessageObject;
        r6 = r6.photoThumbs2;
        r7 = 40;
        r6 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r6, r7);
        r7 = r2.replyMessageObject;
        r8 = r7.photoThumbsObject2;
        if (r4 != 0) goto L_0x06d1;
    L_0x069b:
        r4 = r7.mediaExists;
        if (r4 == 0) goto L_0x06b1;
    L_0x069f:
        r4 = r7.photoThumbs;
        r6 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        if (r4 == 0) goto L_0x06ae;
    L_0x06ab:
        r6 = r4.size;
        goto L_0x06af;
    L_0x06ae:
        r6 = 0;
    L_0x06af:
        r7 = 0;
        goto L_0x06bb;
    L_0x06b1:
        r4 = r7.photoThumbs;
        r6 = 320; // 0x140 float:4.48E-43 double:1.58E-321;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r6 = 0;
        r7 = 1;
    L_0x06bb:
        r8 = r2.replyMessageObject;
        r8 = r8.photoThumbs;
        r9 = 40;
        r8 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r8, r9);
        r9 = r2.replyMessageObject;
        r9 = r9.photoThumbsObject;
        r26 = r6;
        r29 = r7;
        r6 = r4;
        r4 = r8;
        r8 = r9;
        goto L_0x06da;
    L_0x06d1:
        r26 = 0;
        r29 = 1;
        r38 = r6;
        r6 = r4;
        r4 = r38;
    L_0x06da:
        if (r4 != r6) goto L_0x06dd;
    L_0x06dc:
        r4 = 0;
    L_0x06dd:
        if (r6 == 0) goto L_0x073d;
    L_0x06df:
        r7 = r2.replyMessageObject;
        r7 = r7.isAnyKindOfSticker();
        if (r7 != 0) goto L_0x073d;
    L_0x06e7:
        r7 = r40.isAnyKindOfSticker();
        if (r7 == 0) goto L_0x06f3;
    L_0x06ed:
        r7 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r7 == 0) goto L_0x073d;
    L_0x06f3:
        r7 = r2.replyMessageObject;
        r7 = r7.isSecretMedia();
        if (r7 == 0) goto L_0x06fc;
    L_0x06fb:
        goto L_0x073d;
    L_0x06fc:
        r7 = r2.replyMessageObject;
        r7 = r7.isRoundVideo();
        if (r7 == 0) goto L_0x0710;
    L_0x0704:
        r7 = r1.replyImageReceiver;
        r9 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r7.setRoundRadius(r9);
        goto L_0x0715;
    L_0x0710:
        r7 = r1.replyImageReceiver;
        r7.setRoundRadius(r5);
    L_0x0715:
        r1.currentReplyPhoto = r6;
        r7 = r1.replyImageReceiver;
        r22 = org.telegram.messenger.ImageLocation.getForObject(r6, r8);
        r24 = org.telegram.messenger.ImageLocation.getForObject(r4, r8);
        r27 = 0;
        r4 = r2.replyMessageObject;
        r23 = "50_50";
        r25 = "50_50_b";
        r21 = r7;
        r28 = r4;
        r21.setImage(r22, r23, r24, r25, r26, r27, r28, r29);
        r4 = 1;
        r1.needReplyImage = r4;
        r4 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 - r4;
        r4 = r0;
        r6 = 0;
        goto L_0x0746;
    L_0x073d:
        r4 = r1.replyImageReceiver;
        r6 = 0;
        r4.setImageBitmap(r6);
        r1.needReplyImage = r5;
        r4 = r0;
    L_0x0746:
        r0 = r2.customReplyName;
        if (r0 == 0) goto L_0x074b;
    L_0x074a:
        goto L_0x07ac;
    L_0x074b:
        r0 = r2.replyMessageObject;
        r0 = r0.isFromUser();
        if (r0 == 0) goto L_0x076e;
    L_0x0753:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r7 = r2.replyMessageObject;
        r7 = r7.messageOwner;
        r7 = r7.from_id;
        r7 = java.lang.Integer.valueOf(r7);
        r0 = r0.getUser(r7);
        if (r0 == 0) goto L_0x07ab;
    L_0x0769:
        r0 = org.telegram.messenger.UserObject.getUserName(r0);
        goto L_0x07ac;
    L_0x076e:
        r0 = r2.replyMessageObject;
        r0 = r0.messageOwner;
        r0 = r0.from_id;
        if (r0 >= 0) goto L_0x0790;
    L_0x0776:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r7 = r2.replyMessageObject;
        r7 = r7.messageOwner;
        r7 = r7.from_id;
        r7 = -r7;
        r7 = java.lang.Integer.valueOf(r7);
        r0 = r0.getChat(r7);
        if (r0 == 0) goto L_0x07ab;
    L_0x078d:
        r0 = r0.title;
        goto L_0x07ac;
    L_0x0790:
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r7 = r2.replyMessageObject;
        r7 = r7.messageOwner;
        r7 = r7.to_id;
        r7 = r7.channel_id;
        r7 = java.lang.Integer.valueOf(r7);
        r0 = r0.getChat(r7);
        if (r0 == 0) goto L_0x07ab;
    L_0x07a8:
        r0 = r0.title;
        goto L_0x07ac;
    L_0x07ab:
        r0 = r6;
    L_0x07ac:
        if (r0 != 0) goto L_0x07b7;
    L_0x07ae:
        r0 = NUM; // 0x7f0e05d2 float:1.887806E38 double:1.053162893E-314;
        r7 = "Loading";
        r0 = org.telegram.messenger.LocaleController.getString(r7, r0);
    L_0x07b7:
        r7 = 32;
        r8 = 10;
        r0 = r0.replace(r8, r7);
        r7 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r8 = (float) r4;
        r9 = android.text.TextUtils.TruncateAt.END;
        r17 = android.text.TextUtils.ellipsize(r0, r7, r8, r9);
        r0 = r2.replyMessageObject;
        r7 = r0.messageOwner;
        r7 = r7.media;
        r9 = r7 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGame;
        r10 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        if (r9 == 0) goto L_0x07f0;
    L_0x07d4:
        r0 = r7.game;
        r0 = r0.title;
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r2 = r2.getFontMetricsInt();
        r6 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r2, r6, r5);
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r6 = android.text.TextUtils.TruncateAt.END;
        r0 = android.text.TextUtils.ellipsize(r0, r2, r8, r6);
    L_0x07ee:
        r7 = r0;
        goto L_0x084d;
    L_0x07f0:
        r9 = r7 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
        if (r9 == 0) goto L_0x080d;
    L_0x07f4:
        r0 = r7.title;
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r2 = r2.getFontMetricsInt();
        r6 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r2, r6, r5);
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r6 = android.text.TextUtils.TruncateAt.END;
        r0 = android.text.TextUtils.ellipsize(r0, r2, r8, r6);
        goto L_0x07ee;
    L_0x080d:
        r0 = r0.messageText;
        if (r0 == 0) goto L_0x084c;
    L_0x0811:
        r0 = r0.length();
        if (r0 <= 0) goto L_0x084c;
    L_0x0817:
        r0 = r2.replyMessageObject;
        r0 = r0.messageText;
        r0 = r0.toString();
        r2 = r0.length();
        r6 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        if (r2 <= r6) goto L_0x082d;
    L_0x0827:
        r2 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        r0 = r0.substring(r5, r2);
    L_0x082d:
        r2 = 32;
        r6 = 10;
        r0 = r0.replace(r6, r2);
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r2 = r2.getFontMetricsInt();
        r6 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r2, r6, r5);
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;
        r6 = android.text.TextUtils.TruncateAt.END;
        r0 = android.text.TextUtils.ellipsize(r0, r2, r8, r6);
        goto L_0x07ee;
    L_0x084c:
        r7 = r6;
    L_0x084d:
        r0 = 4;
        r2 = r1.needReplyImage;	 Catch:{ Exception -> 0x08a4 }
        if (r2 == 0) goto L_0x0855;
    L_0x0852:
        r2 = 44;
        goto L_0x0856;
    L_0x0855:
        r2 = 0;
    L_0x0856:
        r0 = r0 + r2;
        r0 = (float) r0;	 Catch:{ Exception -> 0x08a4 }
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);	 Catch:{ Exception -> 0x08a4 }
        r1.replyNameWidth = r0;	 Catch:{ Exception -> 0x08a4 }
        if (r17 == 0) goto L_0x08a8;
    L_0x0860:
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x08a4 }
        r18 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x08a4 }
        r2 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);	 Catch:{ Exception -> 0x08a4 }
        r19 = r4 + r2;
        r20 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x08a4 }
        r21 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r22 = 0;
        r23 = 0;
        r16 = r0;
        r16.<init>(r17, r18, r19, r20, r21, r22, r23);	 Catch:{ Exception -> 0x08a4 }
        r1.replyNameLayout = r0;	 Catch:{ Exception -> 0x08a4 }
        r0 = r1.replyNameLayout;	 Catch:{ Exception -> 0x08a4 }
        r0 = r0.getLineCount();	 Catch:{ Exception -> 0x08a4 }
        if (r0 <= 0) goto L_0x08a8;
    L_0x0883:
        r0 = r1.replyNameWidth;	 Catch:{ Exception -> 0x08a4 }
        r2 = r1.replyNameLayout;	 Catch:{ Exception -> 0x08a4 }
        r2 = r2.getLineWidth(r5);	 Catch:{ Exception -> 0x08a4 }
        r8 = (double) r2;	 Catch:{ Exception -> 0x08a4 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x08a4 }
        r2 = (int) r8;	 Catch:{ Exception -> 0x08a4 }
        r6 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x08a4 }
        r2 = r2 + r8;
        r0 = r0 + r2;
        r1.replyNameWidth = r0;	 Catch:{ Exception -> 0x08a4 }
        r0 = r1.replyNameLayout;	 Catch:{ Exception -> 0x08a4 }
        r0 = r0.getLineLeft(r5);	 Catch:{ Exception -> 0x08a4 }
        r1.replyNameOffset = r0;	 Catch:{ Exception -> 0x08a4 }
        goto L_0x08a8;
    L_0x08a4:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x08a8:
        r0 = 4;
        r2 = r1.needReplyImage;	 Catch:{ Exception -> 0x08fb }
        if (r2 == 0) goto L_0x08b0;
    L_0x08ad:
        r2 = 44;
        goto L_0x08b1;
    L_0x08b0:
        r2 = 0;
    L_0x08b1:
        r0 = r0 + r2;
        r0 = (float) r0;	 Catch:{ Exception -> 0x08fb }
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);	 Catch:{ Exception -> 0x08fb }
        r1.replyTextWidth = r0;	 Catch:{ Exception -> 0x08fb }
        if (r7 == 0) goto L_0x0996;
    L_0x08bb:
        r0 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x08fb }
        r8 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x08fb }
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x08fb }
        r9 = r4 + r2;
        r10 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x08fb }
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r12 = 0;
        r13 = 0;
        r6 = r0;
        r6.<init>(r7, r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x08fb }
        r1.replyTextLayout = r0;	 Catch:{ Exception -> 0x08fb }
        r0 = r1.replyTextLayout;	 Catch:{ Exception -> 0x08fb }
        r0 = r0.getLineCount();	 Catch:{ Exception -> 0x08fb }
        if (r0 <= 0) goto L_0x0996;
    L_0x08d9:
        r0 = r1.replyTextWidth;	 Catch:{ Exception -> 0x08fb }
        r2 = r1.replyTextLayout;	 Catch:{ Exception -> 0x08fb }
        r2 = r2.getLineWidth(r5);	 Catch:{ Exception -> 0x08fb }
        r2 = (double) r2;	 Catch:{ Exception -> 0x08fb }
        r2 = java.lang.Math.ceil(r2);	 Catch:{ Exception -> 0x08fb }
        r2 = (int) r2;	 Catch:{ Exception -> 0x08fb }
        r3 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);	 Catch:{ Exception -> 0x08fb }
        r2 = r2 + r3;
        r0 = r0 + r2;
        r1.replyTextWidth = r0;	 Catch:{ Exception -> 0x08fb }
        r0 = r1.replyTextLayout;	 Catch:{ Exception -> 0x08fb }
        r0 = r0.getLineLeft(r5);	 Catch:{ Exception -> 0x08fb }
        r1.replyTextOffset = r0;	 Catch:{ Exception -> 0x08fb }
        goto L_0x0996;
    L_0x08fb:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
        goto L_0x0996;
    L_0x0901:
        r0 = r2.messageOwner;
        r0 = r0.reply_to_msg_id;
        if (r0 == 0) goto L_0x0996;
    L_0x0907:
        r0 = r2.replyMessageObject;
        if (r0 == 0) goto L_0x0911;
    L_0x090b:
        r0 = r0.messageOwner;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageEmpty;
        if (r0 != 0) goto L_0x0996;
    L_0x0911:
        r0 = r40.isAnyKindOfSticker();
        if (r0 != 0) goto L_0x0936;
    L_0x0917:
        r0 = r2.type;
        r4 = 5;
        if (r0 == r4) goto L_0x0936;
    L_0x091c:
        r0 = r1.namesOffset;
        r4 = NUM; // 0x42280000 float:42.0 double:5.483722033E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        r1.namesOffset = r0;
        r0 = r2.type;
        if (r0 == 0) goto L_0x0936;
    L_0x092b:
        r0 = r1.namesOffset;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r0 + r4;
        r1.namesOffset = r0;
    L_0x0936:
        r1.needReplyImage = r5;
        r0 = r39.getMaxNameWidth();
        r4 = r40.shouldDrawWithoutBackground();
        if (r4 != 0) goto L_0x0948;
    L_0x0942:
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r0 = r0 - r2;
        goto L_0x0954;
    L_0x0948:
        r2 = r2.type;
        r3 = 5;
        if (r2 != r3) goto L_0x0954;
    L_0x094d:
        r2 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0 = r0 + r2;
    L_0x0954:
        r2 = new android.text.StaticLayout;
        r8 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;
        r3 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r9 = r0 + r3;
        r10 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r12 = 0;
        r13 = 0;
        r7 = "";
        r6 = r2;
        r6.<init>(r7, r8, r9, r10, r11, r12, r13);
        r1.replyNameLayout = r2;
        r0 = r1.replyNameLayout;
        r0 = r0.getLineCount();
        if (r0 <= 0) goto L_0x0996;
    L_0x0976:
        r0 = r1.replyNameWidth;
        r2 = r1.replyNameLayout;
        r2 = r2.getLineWidth(r5);
        r2 = (double) r2;
        r2 = java.lang.Math.ceil(r2);
        r2 = (int) r2;
        r3 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r0 = r0 + r2;
        r1.replyNameWidth = r0;
        r0 = r1.replyNameLayout;
        r0 = r0.getLineLeft(r5);
        r1.replyNameOffset = r0;
    L_0x0996:
        r39.requestLayout();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.setMessageObjectInternal(org.telegram.messenger.MessageObject):void");
    }

    public int getCaptionHeight() {
        return this.addedCaptionHeight;
    }

    public ImageReceiver getAvatarImage() {
        return this.isAvatarVisible ? this.avatarImage : null;
    }

    public float getCheckBoxTranslation() {
        return (float) this.checkBoxTranslation;
    }

    public void drawCheckBox(Canvas canvas) {
        MessageObject messageObject = this.currentMessageObject;
        if (messageObject != null && !messageObject.isSending() && !this.currentMessageObject.isSendError() && this.checkBox != null) {
            if (this.checkBoxVisible || this.checkBoxAnimationInProgress) {
                GroupedMessagePosition groupedMessagePosition = this.currentPosition;
                if (groupedMessagePosition != null) {
                    int i = groupedMessagePosition.flags;
                    if ((i & 8) == 0 || (i & 1) == 0) {
                        return;
                    }
                }
                canvas.save();
                canvas.translate(0.0f, (float) getTop());
                this.checkBox.draw(canvas);
                canvas.restore();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    @SuppressLint({"WrongCall"})
    public void onDraw(Canvas canvas) {
        Canvas canvas2 = canvas;
        if (this.currentMessageObject != null) {
            if (this.wasLayout || this.animationRunning) {
                int i;
                Drawable drawable;
                Drawable drawable2;
                int i2;
                int dp;
                int i3;
                GroupedMessagePosition groupedMessagePosition;
                if (!this.wasLayout && this.animationRunning) {
                    onLayout(false, getLeft(), getTop(), getRight(), getBottom());
                }
                String str;
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
                    str = "chat_messageLinkOut";
                    Theme.chat_msgTextPaint.linkColor = Theme.getColor(str);
                    Theme.chat_msgGameTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
                    Theme.chat_msgGameTextPaint.linkColor = Theme.getColor(str);
                    Theme.chat_replyTextPaint.linkColor = Theme.getColor(str);
                } else {
                    Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
                    str = "chat_messageLinkIn";
                    Theme.chat_msgTextPaint.linkColor = Theme.getColor(str);
                    Theme.chat_msgGameTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
                    Theme.chat_msgGameTextPaint.linkColor = Theme.getColor(str);
                    Theme.chat_replyTextPaint.linkColor = Theme.getColor(str);
                }
                if (this.documentAttach != null) {
                    i = this.documentAttachType;
                    String str2 = "chat_outAudioSeekbarFill";
                    String str3 = "chat_inAudioSeekbarFill";
                    if (i == 3) {
                        if (this.currentMessageObject.isOutOwner()) {
                            this.seekBarWaveform.setColors(Theme.getColor("chat_outVoiceSeekbar"), Theme.getColor("chat_outVoiceSeekbarFill"), Theme.getColor("chat_outVoiceSeekbarSelected"));
                            this.seekBar.setColors(Theme.getColor("chat_outAudioSeekbar"), Theme.getColor("chat_outAudioCacheSeekbar"), Theme.getColor(str2), Theme.getColor(str2), Theme.getColor("chat_outAudioSeekbarSelected"));
                        } else {
                            this.seekBarWaveform.setColors(Theme.getColor("chat_inVoiceSeekbar"), Theme.getColor("chat_inVoiceSeekbarFill"), Theme.getColor("chat_inVoiceSeekbarSelected"));
                            this.seekBar.setColors(Theme.getColor("chat_inAudioSeekbar"), Theme.getColor("chat_inAudioCacheSeekbar"), Theme.getColor(str3), Theme.getColor(str3), Theme.getColor("chat_inAudioSeekbarSelected"));
                        }
                    } else if (i == 5) {
                        this.documentAttachType = 5;
                        if (this.currentMessageObject.isOutOwner()) {
                            this.seekBar.setColors(Theme.getColor("chat_outAudioSeekbar"), Theme.getColor("chat_outAudioCacheSeekbar"), Theme.getColor(str2), Theme.getColor(str2), Theme.getColor("chat_outAudioSeekbarSelected"));
                        } else {
                            this.seekBar.setColors(Theme.getColor("chat_inAudioSeekbar"), Theme.getColor("chat_inAudioCacheSeekbar"), Theme.getColor(str3), Theme.getColor(str3), Theme.getColor("chat_inAudioSeekbarSelected"));
                        }
                    }
                }
                MessageObject messageObject = this.currentMessageObject;
                if (messageObject.type == 5) {
                    Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
                } else if (this.mediaBackground) {
                    if (messageObject.shouldDrawWithoutBackground()) {
                        Theme.chat_timePaint.setColor(Theme.getColor("chat_serviceText"));
                    } else {
                        Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
                    }
                } else if (messageObject.isOutOwner()) {
                    Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_outTimeSelectedText" : "chat_outTimeText"));
                } else {
                    Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_inTimeSelectedText" : "chat_inTimeText"));
                }
                int i4 = 0;
                int dp2;
                if (this.currentMessageObject.isOutOwner()) {
                    int i5;
                    if (this.mediaBackground || this.drawPinnedBottom) {
                        this.currentBackgroundDrawable = Theme.chat_msgOutMediaDrawable;
                        drawable = Theme.chat_msgOutMediaSelectedDrawable;
                        drawable2 = Theme.chat_msgOutMediaShadowDrawable;
                    } else {
                        this.currentBackgroundDrawable = Theme.chat_msgOutDrawable;
                        drawable = Theme.chat_msgOutSelectedDrawable;
                        drawable2 = Theme.chat_msgOutShadowDrawable;
                    }
                    this.backgroundDrawableLeft = (this.layoutWidth - this.backgroundWidth) - (!this.mediaBackground ? 0 : AndroidUtilities.dp(9.0f));
                    this.backgroundDrawableRight = this.backgroundWidth - (this.mediaBackground ? 0 : AndroidUtilities.dp(3.0f));
                    if (!(this.currentMessagesGroup == null || this.currentPosition.edge)) {
                        this.backgroundDrawableRight += AndroidUtilities.dp(10.0f);
                    }
                    i2 = this.backgroundDrawableLeft;
                    if (!this.mediaBackground && this.drawPinnedBottom) {
                        this.backgroundDrawableRight -= AndroidUtilities.dp(6.0f);
                    }
                    GroupedMessagePosition groupedMessagePosition2 = this.currentPosition;
                    if (groupedMessagePosition2 != null) {
                        if ((groupedMessagePosition2.flags & 2) == 0) {
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 1) == 0) {
                            i2 -= AndroidUtilities.dp(8.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 4) == 0) {
                            dp = 0 - AndroidUtilities.dp(9.0f);
                            dp2 = AndroidUtilities.dp(9.0f) + 0;
                        } else {
                            dp = 0;
                            dp2 = 0;
                        }
                        if ((this.currentPosition.flags & 8) == 0) {
                            dp2 += AndroidUtilities.dp(9.0f);
                        }
                    } else {
                        dp = 0;
                        dp2 = 0;
                    }
                    if (this.drawPinnedBottom && this.drawPinnedTop) {
                        i5 = 0;
                    } else if (this.drawPinnedBottom) {
                        i5 = AndroidUtilities.dp(1.0f);
                    } else {
                        i5 = AndroidUtilities.dp(2.0f);
                    }
                    boolean z = this.drawPinnedTop;
                    int dp3 = (z || (z && this.drawPinnedBottom)) ? 0 : AndroidUtilities.dp(1.0f);
                    dp += dp3;
                    BaseCell.setDrawableBounds(this.currentBackgroundDrawable, i2, dp, this.backgroundDrawableRight, (this.layoutHeight - i5) + dp2);
                    BaseCell.setDrawableBounds(drawable, i2, dp, this.backgroundDrawableRight, (this.layoutHeight - i5) + dp2);
                    BaseCell.setDrawableBounds(drawable2, i2, dp, this.backgroundDrawableRight, (this.layoutHeight - i5) + dp2);
                } else {
                    Drawable drawable3;
                    if (this.mediaBackground || this.drawPinnedBottom) {
                        this.currentBackgroundDrawable = Theme.chat_msgInMediaDrawable;
                        drawable = Theme.chat_msgInMediaSelectedDrawable;
                        drawable3 = Theme.chat_msgInMediaShadowDrawable;
                    } else {
                        this.currentBackgroundDrawable = Theme.chat_msgInDrawable;
                        drawable = Theme.chat_msgInSelectedDrawable;
                        drawable3 = Theme.chat_msgInShadowDrawable;
                    }
                    drawable2 = drawable3;
                    i3 = (this.isChat && this.isAvatarVisible) ? 48 : 0;
                    this.backgroundDrawableLeft = AndroidUtilities.dp((float) (i3 + (!this.mediaBackground ? 3 : 9)));
                    this.backgroundDrawableRight = this.backgroundWidth - (this.mediaBackground ? 0 : AndroidUtilities.dp(3.0f));
                    if (this.currentMessagesGroup != null) {
                        if (!this.currentPosition.edge) {
                            this.backgroundDrawableLeft -= AndroidUtilities.dp(10.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(10.0f);
                        }
                        i3 = this.currentPosition.leftSpanOffset;
                        if (i3 != 0) {
                            this.backgroundDrawableLeft += (int) Math.ceil((double) ((((float) i3) / 1000.0f) * ((float) getGroupPhotosWidth())));
                        }
                    }
                    if (!this.mediaBackground && this.drawPinnedBottom) {
                        this.backgroundDrawableRight -= AndroidUtilities.dp(6.0f);
                        this.backgroundDrawableLeft += AndroidUtilities.dp(6.0f);
                    }
                    GroupedMessagePosition groupedMessagePosition3 = this.currentPosition;
                    if (groupedMessagePosition3 != null) {
                        if ((groupedMessagePosition3.flags & 2) == 0) {
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 1) == 0) {
                            this.backgroundDrawableLeft -= AndroidUtilities.dp(8.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 4) == 0) {
                            i3 = 0 - AndroidUtilities.dp(9.0f);
                            dp = AndroidUtilities.dp(9.0f) + 0;
                        } else {
                            i3 = 0;
                            dp = 0;
                        }
                        if ((this.currentPosition.flags & 8) == 0) {
                            dp += AndroidUtilities.dp(10.0f);
                        }
                    } else {
                        i3 = 0;
                        dp = 0;
                    }
                    if (this.drawPinnedBottom && this.drawPinnedTop) {
                        i2 = 0;
                    } else if (this.drawPinnedBottom) {
                        i2 = AndroidUtilities.dp(1.0f);
                    } else {
                        i2 = AndroidUtilities.dp(2.0f);
                    }
                    boolean z2 = this.drawPinnedTop;
                    dp2 = (z2 || (z2 && this.drawPinnedBottom)) ? 0 : AndroidUtilities.dp(1.0f);
                    i3 += dp2;
                    BaseCell.setDrawableBounds(this.currentBackgroundDrawable, this.backgroundDrawableLeft, i3, this.backgroundDrawableRight, (this.layoutHeight - i2) + dp);
                    BaseCell.setDrawableBounds(drawable, this.backgroundDrawableLeft, i3, this.backgroundDrawableRight, (this.layoutHeight - i2) + dp);
                    BaseCell.setDrawableBounds(drawable2, this.backgroundDrawableLeft, i3, this.backgroundDrawableRight, (this.layoutHeight - i2) + dp);
                }
                if (this.checkBoxVisible || this.checkBoxAnimationInProgress) {
                    if ((this.checkBoxVisible && this.checkBoxAnimationProgress == 1.0f) || (!this.checkBoxVisible && this.checkBoxAnimationProgress == 0.0f)) {
                        this.checkBoxAnimationInProgress = false;
                    }
                    this.checkBoxTranslation = (int) Math.ceil((double) ((this.checkBoxVisible ? CubicBezierInterpolator.EASE_OUT : CubicBezierInterpolator.EASE_IN).getInterpolation(this.checkBoxAnimationProgress) * ((float) AndroidUtilities.dp(35.0f))));
                    if (!this.currentMessageObject.isOutOwner()) {
                        setTranslationX((float) this.checkBoxTranslation);
                    }
                    i3 = AndroidUtilities.dp(21.0f);
                    this.checkBox.setBounds(AndroidUtilities.dp(-27.0f) + this.checkBoxTranslation, (this.currentBackgroundDrawable.getBounds().bottom - AndroidUtilities.dp(8.0f)) - i3, i3, i3);
                    if (this.checkBoxAnimationInProgress) {
                        long uptimeMillis = SystemClock.uptimeMillis();
                        long j = uptimeMillis - this.lastCheckBoxAnimationTime;
                        this.lastCheckBoxAnimationTime = uptimeMillis;
                        if (this.checkBoxVisible) {
                            this.checkBoxAnimationProgress += ((float) j) / 200.0f;
                            if (this.checkBoxAnimationProgress > 1.0f) {
                                this.checkBoxAnimationProgress = 1.0f;
                            }
                            invalidate();
                            ((View) getParent()).invalidate();
                        } else {
                            this.checkBoxAnimationProgress -= ((float) j) / 200.0f;
                            if (this.checkBoxAnimationProgress <= 0.0f) {
                                this.checkBoxAnimationProgress = 0.0f;
                            }
                            invalidate();
                            ((View) getParent()).invalidate();
                        }
                    }
                }
                if (this.drawBackground && this.currentBackgroundDrawable != null) {
                    i3 = 0;
                    while (i3 < 2) {
                        Drawable drawable4 = i3 == 0 ? this.currentBackgroundDrawable : drawable;
                        if (drawable4 instanceof MessageDrawable) {
                            MessageDrawable messageDrawable = (MessageDrawable) drawable4;
                            i2 = AndroidUtilities.displaySize.y;
                            if (getParent() instanceof View) {
                                i2 = ((View) getParent()).getMeasuredHeight();
                            }
                            messageDrawable.setTop(getTop(), i2);
                        }
                        i3++;
                    }
                    if (this.isHighlightedAnimated) {
                        this.currentBackgroundDrawable.draw(canvas2);
                        i3 = this.highlightProgress;
                        float f = i3 >= 300 ? 1.0f : ((float) i3) / 300.0f;
                        if (this.currentPosition == null) {
                            drawable.setAlpha((int) (f * 255.0f));
                            drawable.draw(canvas2);
                        }
                    } else if (!isDrawSelectionBackground() || (this.currentPosition != null && getBackground() == null)) {
                        this.currentBackgroundDrawable.draw(canvas2);
                    } else {
                        drawable.setAlpha(255);
                        drawable.draw(canvas2);
                    }
                    groupedMessagePosition = this.currentPosition;
                    if (groupedMessagePosition == null || groupedMessagePosition.flags != 0) {
                        drawable2.draw(canvas2);
                    }
                }
                if (this.isHighlightedAnimated) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long abs = Math.abs(currentTimeMillis - this.lastHighlightProgressTime);
                    if (abs > 17) {
                        abs = 17;
                    }
                    this.highlightProgress = (int) (((long) this.highlightProgress) - abs);
                    this.lastHighlightProgressTime = currentTimeMillis;
                    if (this.highlightProgress <= 0) {
                        this.highlightProgress = 0;
                        this.isHighlightedAnimated = false;
                    }
                    invalidate();
                    if (getParent() != null) {
                        ((View) getParent()).invalidate();
                    }
                }
                drawContent(canvas);
                float f2 = 12.0f;
                if (this.drawShareButton) {
                    if (this.sharePressed) {
                        if (!Theme.isCustomTheme() || Theme.hasThemeKey("chat_shareBackgroundSelected")) {
                            Theme.chat_shareDrawable.setColorFilter(Theme.getShareColorFilter(Theme.getColor("chat_shareBackgroundSelected"), true));
                        } else {
                            Theme.chat_shareDrawable.setColorFilter(Theme.colorPressedFilter2);
                        }
                    } else if (!Theme.isCustomTheme() || Theme.hasThemeKey("chat_shareBackground")) {
                        Theme.chat_shareDrawable.setColorFilter(Theme.getShareColorFilter(Theme.getColor("chat_shareBackground"), false));
                    } else {
                        Theme.chat_shareDrawable.setColorFilter(Theme.colorFilter2);
                    }
                    if (this.currentMessageObject.isOutOwner()) {
                        this.shareStartX = (this.currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(8.0f)) - Theme.chat_shareDrawable.getIntrinsicWidth();
                    } else {
                        this.shareStartX = this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(8.0f);
                    }
                    drawable = Theme.chat_shareDrawable;
                    dp = this.shareStartX;
                    int dp4 = this.layoutHeight - AndroidUtilities.dp(41.0f);
                    this.shareStartY = dp4;
                    BaseCell.setDrawableBounds(drawable, dp, dp4);
                    Theme.chat_shareDrawable.draw(canvas2);
                    if (this.drwaShareGoIcon) {
                        BaseCell.setDrawableBounds(Theme.chat_goIconDrawable, this.shareStartX + AndroidUtilities.dp(12.0f), this.shareStartY + AndroidUtilities.dp(9.0f));
                        Theme.chat_goIconDrawable.draw(canvas2);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_shareIconDrawable, this.shareStartX + AndroidUtilities.dp(8.0f), this.shareStartY + AndroidUtilities.dp(9.0f));
                        Theme.chat_shareIconDrawable.draw(canvas2);
                    }
                }
                if (this.replyNameLayout != null) {
                    if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                        if (this.currentMessageObject.isOutOwner()) {
                            this.replyStartX = AndroidUtilities.dp(23.0f);
                        } else if (this.currentMessageObject.type == 5) {
                            this.replyStartX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(4.0f);
                        } else {
                            this.replyStartX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(17.0f);
                        }
                        this.replyStartY = AndroidUtilities.dp(12.0f);
                    } else {
                        if (this.currentMessageObject.isOutOwner()) {
                            this.replyStartX = this.backgroundDrawableLeft + AndroidUtilities.dp(12.0f);
                        } else {
                            boolean z3 = this.mediaBackground;
                            if (z3) {
                                this.replyStartX = this.backgroundDrawableLeft + AndroidUtilities.dp(12.0f);
                            } else {
                                dp = this.backgroundDrawableLeft;
                                if (z3 || !this.drawPinnedBottom) {
                                    f2 = 18.0f;
                                }
                                this.replyStartX = dp + AndroidUtilities.dp(f2);
                            }
                        }
                        i3 = (!this.drawForwardedName || this.forwardedNameLayout[0] == null) ? 0 : 36;
                        i = 12 + i3;
                        if (this.drawNameLayout && this.nameLayout != null) {
                            i4 = 20;
                        }
                        this.replyStartY = AndroidUtilities.dp((float) (i + i4));
                    }
                }
                if (this.currentPosition == null) {
                    drawNamesLayout(canvas);
                }
                if (!(this.autoPlayingMedia && MediaController.getInstance().isPlayingMessageAndReadyToDraw(this.currentMessageObject))) {
                    drawOverlays(canvas);
                }
                if ((this.drawTime || !this.mediaBackground) && !this.forceNotDrawTime) {
                    drawTime(canvas);
                }
                if (!((this.controlsAlpha == 1.0f && this.timeAlpha == 1.0f) || this.currentMessageObject.type == 5)) {
                    long currentTimeMillis2 = System.currentTimeMillis();
                    long abs2 = Math.abs(this.lastControlsAlphaChangeTime - currentTimeMillis2);
                    if (abs2 > 17) {
                        abs2 = 17;
                    }
                    this.totalChangeTime += abs2;
                    if (this.totalChangeTime > 100) {
                        this.totalChangeTime = 100;
                    }
                    this.lastControlsAlphaChangeTime = currentTimeMillis2;
                    if (this.controlsAlpha != 1.0f) {
                        this.controlsAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(((float) this.totalChangeTime) / 100.0f);
                    }
                    if (this.timeAlpha != 1.0f) {
                        this.timeAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(((float) this.totalChangeTime) / 100.0f);
                    }
                    invalidate();
                    if (this.forceNotDrawTime) {
                        groupedMessagePosition = this.currentPosition;
                        if (!(groupedMessagePosition == null || !groupedMessagePosition.last || getParent() == null)) {
                            ((View) getParent()).invalidate();
                        }
                    }
                }
                return;
            }
            requestLayout();
        }
    }

    public void setTimeAlpha(float f) {
        this.timeAlpha = f;
    }

    public float getTimeAlpha() {
        return this.timeAlpha;
    }

    public int getBackgroundDrawableLeft() {
        int i = 0;
        if (this.currentMessageObject.isOutOwner()) {
            int i2 = this.layoutWidth - this.backgroundWidth;
            if (this.mediaBackground) {
                i = AndroidUtilities.dp(9.0f);
            }
            return i2 - i;
        }
        if (this.isChat && this.isAvatarVisible) {
            i = 48;
        }
        return AndroidUtilities.dp((float) (i + (!this.mediaBackground ? 3 : 9)));
    }

    public boolean hasNameLayout() {
        if (this.drawNameLayout && this.nameLayout != null) {
            return true;
        }
        if (this.drawForwardedName) {
            StaticLayout[] staticLayoutArr = this.forwardedNameLayout;
            if (!(staticLayoutArr[0] == null || staticLayoutArr[1] == null)) {
                GroupedMessagePosition groupedMessagePosition = this.currentPosition;
                if (groupedMessagePosition == null) {
                    return true;
                }
                if (groupedMessagePosition.minY == (byte) 0 && groupedMessagePosition.minX == (byte) 0) {
                    return true;
                }
            }
        }
        return this.replyNameLayout != null;
    }

    public boolean isDrawNameLayout() {
        return this.drawNameLayout && this.nameLayout != null;
    }

    public void drawNamesLayout(Canvas canvas) {
        float f;
        int alpha;
        GroupedMessagePosition groupedMessagePosition;
        float f2 = 17.0f;
        int i = 0;
        if (this.drawNameLayout && this.nameLayout != null) {
            canvas.save();
            if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                Theme.chat_namePaint.setColor(Theme.getColor("chat_stickerNameText"));
                if (this.currentMessageObject.isOutOwner()) {
                    this.nameX = (float) AndroidUtilities.dp(28.0f);
                } else {
                    this.nameX = (float) ((this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(22.0f));
                }
                this.nameY = (float) (this.layoutHeight - AndroidUtilities.dp(38.0f));
                f = (this.currentMessageObject.isOut() && (this.checkBoxVisible || this.checkBoxAnimationInProgress)) ? 1.0f - this.checkBoxAnimationProgress : 1.0f;
                Theme.chat_systemDrawable.setAlpha((int) (255.0f * f));
                Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(((int) this.nameX) - AndroidUtilities.dp(12.0f), ((int) this.nameY) - AndroidUtilities.dp(5.0f), (((int) this.nameX) + AndroidUtilities.dp(12.0f)) + this.nameWidth, ((int) this.nameY) + AndroidUtilities.dp(22.0f));
                Theme.chat_systemDrawable.draw(canvas);
                if (this.checkBoxVisible || this.checkBoxAnimationInProgress) {
                    Theme.chat_systemDrawable.setAlpha(255);
                }
                this.nameX -= this.nameOffsetX;
                alpha = (((int) (((float) Color.alpha(Theme.getColor("chat_stickerViaBotNameText"))) * f)) << 24) | (Theme.getColor("chat_stickerViaBotNameText") & 16777215);
                TypefaceSpan typefaceSpan = this.viaSpan1;
                if (typefaceSpan != null) {
                    typefaceSpan.setColor(alpha);
                }
                typefaceSpan = this.viaSpan2;
                if (typefaceSpan != null) {
                    typefaceSpan.setColor(alpha);
                }
                Theme.chat_systemDrawable.setAlpha(255);
            } else {
                if (this.mediaBackground || this.currentMessageObject.isOutOwner()) {
                    this.nameX = ((float) (this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f))) - this.nameOffsetX;
                } else {
                    alpha = this.backgroundDrawableLeft;
                    float f3 = (this.mediaBackground || !this.drawPinnedBottom) ? 17.0f : 11.0f;
                    this.nameX = ((float) (alpha + AndroidUtilities.dp(f3))) - this.nameOffsetX;
                }
                User user = this.currentUser;
                if (user != null) {
                    Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(user.id));
                } else {
                    Chat chat = this.currentChat;
                    if (chat == null) {
                        Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(0));
                    } else if (!ChatObject.isChannel(chat) || this.currentChat.megagroup) {
                        Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentChat.id));
                    } else {
                        Theme.chat_namePaint.setColor(Theme.changeColorAccent(AvatarDrawable.getNameColorForId(5)));
                    }
                }
                this.nameY = (float) AndroidUtilities.dp(this.drawPinnedTop ? 9.0f : 10.0f);
            }
            canvas.translate(this.nameX, this.nameY);
            this.nameLayout.draw(canvas);
            canvas.restore();
            if (this.adminLayout != null) {
                Theme.chat_adminPaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_adminSelectedText" : "chat_adminText"));
                canvas.save();
                canvas.translate(((float) ((this.backgroundDrawableLeft + this.backgroundDrawableRight) - AndroidUtilities.dp(11.0f))) - this.adminLayout.getLineWidth(0), this.nameY + ((float) AndroidUtilities.dp(0.5f)));
                this.adminLayout.draw(canvas);
                canvas.restore();
            }
        }
        String str = "chat_stickerReplyNameText";
        if (this.drawForwardedName) {
            StaticLayout[] staticLayoutArr = this.forwardedNameLayout;
            if (!(staticLayoutArr[0] == null || staticLayoutArr[1] == null)) {
                groupedMessagePosition = this.currentPosition;
                if (groupedMessagePosition == null || (groupedMessagePosition.minY == (byte) 0 && groupedMessagePosition.minX == (byte) 0)) {
                    if (this.currentMessageObject.type == 5) {
                        Theme.chat_forwardNamePaint.setColor(Theme.getColor(str));
                        if (this.currentMessageObject.isOutOwner()) {
                            this.forwardNameX = AndroidUtilities.dp(23.0f);
                        } else {
                            this.forwardNameX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(17.0f);
                        }
                        this.forwardNameY = AndroidUtilities.dp(12.0f);
                        alpha = this.forwardedNameWidth + AndroidUtilities.dp(14.0f);
                        Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                        Theme.chat_systemDrawable.setBounds(this.forwardNameX - AndroidUtilities.dp(7.0f), this.forwardNameY - AndroidUtilities.dp(6.0f), (this.forwardNameX - AndroidUtilities.dp(7.0f)) + alpha, this.forwardNameY + AndroidUtilities.dp(38.0f));
                        Theme.chat_systemDrawable.draw(canvas);
                    } else {
                        this.forwardNameY = AndroidUtilities.dp((float) ((this.drawNameLayout ? 19 : 0) + 10));
                        if (this.currentMessageObject.isOutOwner()) {
                            Theme.chat_forwardNamePaint.setColor(Theme.getColor("chat_outForwardedNameText"));
                            this.forwardNameX = this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f);
                        } else {
                            Theme.chat_forwardNamePaint.setColor(Theme.getColor("chat_inForwardedNameText"));
                            boolean z = this.mediaBackground;
                            if (z) {
                                this.forwardNameX = this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f);
                            } else {
                                int i2 = this.backgroundDrawableLeft;
                                if (!z && this.drawPinnedBottom) {
                                    f2 = 11.0f;
                                }
                                this.forwardNameX = i2 + AndroidUtilities.dp(f2);
                            }
                        }
                    }
                    for (alpha = 0; alpha < 2; alpha++) {
                        canvas.save();
                        canvas.translate(((float) this.forwardNameX) - this.forwardNameOffsetX[alpha], (float) (this.forwardNameY + (AndroidUtilities.dp(16.0f) * alpha)));
                        this.forwardedNameLayout[alpha].draw(canvas);
                        canvas.restore();
                    }
                }
            }
        }
        if (this.replyNameLayout != null) {
            MessageObject messageObject;
            MessageMedia messageMedia;
            if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_stickerReplyLine"));
                Theme.chat_replyNamePaint.setColor(Theme.getColor(str));
                Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_stickerReplyMessageText"));
                alpha = Math.max(this.replyNameWidth, this.replyTextWidth) + AndroidUtilities.dp(14.0f);
                Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(this.replyStartX - AndroidUtilities.dp(7.0f), this.replyStartY - AndroidUtilities.dp(6.0f), (this.replyStartX - AndroidUtilities.dp(7.0f)) + alpha, this.replyStartY + AndroidUtilities.dp(41.0f));
                Theme.chat_systemDrawable.draw(canvas);
            } else if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_outReplyLine"));
                Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_outReplyNameText"));
                if (this.currentMessageObject.hasValidReplyMessageObject()) {
                    messageObject = this.currentMessageObject.replyMessageObject;
                    if (messageObject.type == 0) {
                        messageMedia = messageObject.messageOwner.media;
                        if (!((messageMedia instanceof TL_messageMediaGame) || (messageMedia instanceof TL_messageMediaInvoice))) {
                            Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_outReplyMessageText"));
                        }
                    }
                }
                Theme.chat_replyTextPaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_outReplyMediaMessageSelectedText" : "chat_outReplyMediaMessageText"));
            } else {
                Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_inReplyLine"));
                Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_inReplyNameText"));
                if (this.currentMessageObject.hasValidReplyMessageObject()) {
                    messageObject = this.currentMessageObject.replyMessageObject;
                    if (messageObject.type == 0) {
                        messageMedia = messageObject.messageOwner.media;
                        if (!((messageMedia instanceof TL_messageMediaGame) || (messageMedia instanceof TL_messageMediaInvoice))) {
                            Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_inReplyMessageText"));
                        }
                    }
                }
                Theme.chat_replyTextPaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_inReplyMediaMessageSelectedText" : "chat_inReplyMediaMessageText"));
            }
            groupedMessagePosition = this.currentPosition;
            if (groupedMessagePosition == null || (groupedMessagePosition.minY == (byte) 0 && groupedMessagePosition.minX == (byte) 0)) {
                alpha = this.replyStartX;
                canvas.drawRect((float) alpha, (float) this.replyStartY, (float) (alpha + AndroidUtilities.dp(2.0f)), (float) (this.replyStartY + AndroidUtilities.dp(35.0f)), Theme.chat_replyLinePaint);
                if (this.needReplyImage) {
                    this.replyImageReceiver.setImageCoords(this.replyStartX + AndroidUtilities.dp(10.0f), this.replyStartY, AndroidUtilities.dp(35.0f), AndroidUtilities.dp(35.0f));
                    this.replyImageReceiver.draw(canvas);
                }
                if (this.replyNameLayout != null) {
                    canvas.save();
                    canvas.translate((((float) this.replyStartX) - this.replyNameOffset) + ((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))), (float) this.replyStartY);
                    this.replyNameLayout.draw(canvas);
                    canvas.restore();
                }
                if (this.replyTextLayout != null) {
                    canvas.save();
                    f = ((float) this.replyStartX) - this.replyTextOffset;
                    if (this.needReplyImage) {
                        i = 44;
                    }
                    canvas.translate(f + ((float) AndroidUtilities.dp((float) (i + 10))), (float) (this.replyStartY + AndroidUtilities.dp(19.0f)));
                    this.replyTextLayout.draw(canvas);
                    canvas.restore();
                }
            }
        }
    }

    public boolean hasCaptionLayout() {
        return this.captionLayout != null;
    }

    public void setDrawSelectionBackground(boolean z) {
        this.drawSelectionBackground = z;
        invalidate();
    }

    public boolean isDrawingSelectionBackground() {
        return this.drawSelectionBackground || this.isHighlightedAnimated || this.isHighlighted;
    }

    public float getHightlightAlpha() {
        if (this.drawSelectionBackground || !this.isHighlightedAnimated) {
            return 1.0f;
        }
        int i = this.highlightProgress;
        if (i >= 300) {
            return 1.0f;
        }
        return ((float) i) / 300.0f;
    }

    public void setCheckBoxVisible(boolean z, boolean z2) {
        if (z && this.checkBox == null) {
            this.checkBox = new CheckBoxBase(this, 21);
            if (this.attachedToWindow) {
                this.checkBox.onAttachedToWindow();
            }
        }
        if (z && this.photoCheckBox == null) {
            GroupedMessages groupedMessages = this.currentMessagesGroup;
            if (groupedMessages != null && groupedMessages.messages.size() > 1) {
                this.photoCheckBox = new CheckBoxBase(this, 21);
                this.photoCheckBox.setUseDefaultCheck(true);
                if (this.attachedToWindow) {
                    this.photoCheckBox.onAttachedToWindow();
                }
            }
        }
        float f = 1.0f;
        if (this.checkBoxVisible == z) {
            if (!(z2 == this.checkBoxAnimationInProgress || z2)) {
                if (!z) {
                    f = 0.0f;
                }
                this.checkBoxAnimationProgress = f;
                invalidate();
            }
            return;
        }
        this.checkBoxAnimationInProgress = z2;
        this.checkBoxVisible = z;
        if (z2) {
            this.lastCheckBoxAnimationTime = SystemClock.uptimeMillis();
        } else {
            if (!z) {
                f = 0.0f;
            }
            this.checkBoxAnimationProgress = f;
        }
        invalidate();
    }

    public void setChecked(boolean z, boolean z2, boolean z3) {
        CheckBoxBase checkBoxBase = this.checkBox;
        if (checkBoxBase != null) {
            checkBoxBase.setChecked(z2, z3);
        }
        CheckBoxBase checkBoxBase2 = this.photoCheckBox;
        if (checkBoxBase2 != null) {
            checkBoxBase2.setChecked(z, z3);
        }
    }

    public void drawCaptionLayout(Canvas canvas, boolean z) {
        if (this.captionLayout == null) {
            return;
        }
        if (!z || this.pressedLink != null) {
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
                Theme.chat_msgTextPaint.linkColor = Theme.getColor("chat_messageLinkOut");
            } else {
                Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
                Theme.chat_msgTextPaint.linkColor = Theme.getColor("chat_messageLinkIn");
            }
            canvas.save();
            canvas.translate((float) this.captionX, (float) this.captionY);
            if (this.pressedLink != null) {
                for (int i = 0; i < this.urlPath.size(); i++) {
                    canvas.drawPath((Path) this.urlPath.get(i), Theme.chat_urlPaint);
                }
            }
            if (!this.urlPathSelection.isEmpty()) {
                for (int i2 = 0; i2 < this.urlPathSelection.size(); i2++) {
                    canvas.drawPath((Path) this.urlPathSelection.get(i2), Theme.chat_textSearchSelectionPaint);
                }
            }
            if (!z) {
                try {
                    this.captionLayout.draw(canvas);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            canvas.restore();
        }
    }

    public boolean needDrawTime() {
        return this.forceNotDrawTime ^ 1;
    }

    public void drawTime(Canvas canvas) {
        Canvas canvas2 = canvas;
        if (((this.drawTime && !this.groupPhotoInvisible) || !this.mediaBackground || this.captionLayout != null) && this.timeLayout != null) {
            int i;
            int dp;
            int dp2;
            MessageObject messageObject = this.currentMessageObject;
            String str = "chat_mediaTimeText";
            if (messageObject.type == 5) {
                Theme.chat_timePaint.setColor(Theme.getColor(str));
            } else if (this.mediaBackground && this.captionLayout == null) {
                if (messageObject.shouldDrawWithoutBackground()) {
                    Theme.chat_timePaint.setColor(Theme.getColor("chat_serviceText"));
                } else {
                    Theme.chat_timePaint.setColor(Theme.getColor(str));
                }
            } else if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_outTimeSelectedText" : "chat_outTimeText"));
            } else {
                Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectionBackground() ? "chat_inTimeSelectedText" : "chat_inTimeText"));
            }
            if (this.drawPinnedBottom) {
                canvas2.translate(0.0f, (float) AndroidUtilities.dp(2.0f));
            }
            int i2 = 0;
            Drawable drawable;
            if (this.mediaBackground && this.captionLayout == null) {
                Paint paint;
                if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                    paint = Theme.chat_actionBackgroundPaint;
                } else {
                    paint = Theme.chat_timeBackgroundPaint;
                }
                int alpha = paint.getAlpha();
                paint.setAlpha((int) (((float) alpha) * this.timeAlpha));
                Theme.chat_timePaint.setAlpha((int) (this.timeAlpha * 255.0f));
                int dp3 = this.timeX - AndroidUtilities.dp(4.0f);
                int dp4 = this.layoutHeight - AndroidUtilities.dp(28.0f);
                this.rect.set((float) dp3, (float) dp4, (float) ((dp3 + this.timeWidth) + AndroidUtilities.dp((float) ((this.currentMessageObject.isOutOwner() ? 20 : 0) + 8))), (float) (dp4 + AndroidUtilities.dp(17.0f)));
                canvas2.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), paint);
                paint.setAlpha(alpha);
                i = (int) (-this.timeLayout.getLineLeft(0));
                if ((ChatObject.isChannel(this.currentChat) && !this.currentChat.megagroup) || (this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                    i += (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                    if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            BaseCell.setDrawableBounds(Theme.chat_msgMediaClockDrawable, this.timeX + (this.currentMessageObject.scheduled ? 0 : AndroidUtilities.dp(11.0f)), (this.layoutHeight - AndroidUtilities.dp(14.0f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
                            Theme.chat_msgMediaClockDrawable.draw(canvas2);
                        }
                    } else if (this.currentMessageObject.isSendError()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            dp = this.timeX + (this.currentMessageObject.scheduled ? 0 : AndroidUtilities.dp(11.0f));
                            dp2 = this.layoutHeight - AndroidUtilities.dp(26.5f);
                            this.rect.set((float) dp, (float) dp2, (float) (AndroidUtilities.dp(14.0f) + dp), (float) (AndroidUtilities.dp(14.0f) + dp2));
                            canvas2.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                            BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, dp + AndroidUtilities.dp(6.0f), dp2 + AndroidUtilities.dp(2.0f));
                            Theme.chat_msgErrorDrawable.draw(canvas2);
                        }
                    } else if (this.viewsLayout != null) {
                        if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                            drawable = Theme.chat_msgStickerViewsDrawable;
                        } else {
                            drawable = Theme.chat_msgMediaViewsDrawable;
                        }
                        dp2 = ((BitmapDrawable) drawable).getPaint().getAlpha();
                        drawable.setAlpha((int) (this.timeAlpha * ((float) dp2)));
                        BaseCell.setDrawableBounds(drawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(10.5f)) - this.timeLayout.getHeight());
                        drawable.draw(canvas2);
                        drawable.setAlpha(dp2);
                        canvas.save();
                        canvas2.translate((float) ((this.timeX + drawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(12.3f)) - this.timeLayout.getHeight()));
                        this.viewsLayout.draw(canvas2);
                        canvas.restore();
                    }
                }
                canvas.save();
                canvas2.translate((float) (this.timeX + i), (float) ((this.layoutHeight - AndroidUtilities.dp(12.3f)) - this.timeLayout.getHeight()));
                this.timeLayout.draw(canvas2);
                canvas.restore();
                Theme.chat_timePaint.setAlpha(255);
            } else {
                i = (int) (-this.timeLayout.getLineLeft(0));
                if ((ChatObject.isChannel(this.currentChat) && !this.currentChat.megagroup) || (this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                    i += (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                    if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            drawable = isDrawSelectionBackground() ? Theme.chat_msgInSelectedClockDrawable : Theme.chat_msgInClockDrawable;
                            BaseCell.setDrawableBounds(drawable, this.timeX + (this.currentMessageObject.scheduled ? 0 : AndroidUtilities.dp(11.0f)), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - drawable.getIntrinsicHeight());
                            drawable.draw(canvas2);
                        }
                    } else if (this.currentMessageObject.isSendError()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            dp = this.timeX + (this.currentMessageObject.scheduled ? 0 : AndroidUtilities.dp(11.0f));
                            dp2 = this.layoutHeight - AndroidUtilities.dp(20.5f);
                            this.rect.set((float) dp, (float) dp2, (float) (AndroidUtilities.dp(14.0f) + dp), (float) (AndroidUtilities.dp(14.0f) + dp2));
                            canvas2.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                            BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, dp + AndroidUtilities.dp(6.0f), dp2 + AndroidUtilities.dp(2.0f));
                            Theme.chat_msgErrorDrawable.draw(canvas2);
                        }
                    } else if (this.viewsLayout != null) {
                        if (this.currentMessageObject.isOutOwner()) {
                            drawable = isDrawSelectionBackground() ? Theme.chat_msgOutViewsSelectedDrawable : Theme.chat_msgOutViewsDrawable;
                            BaseCell.setDrawableBounds(drawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                            drawable.draw(canvas2);
                        } else {
                            drawable = isDrawSelectionBackground() ? Theme.chat_msgInViewsSelectedDrawable : Theme.chat_msgInViewsDrawable;
                            BaseCell.setDrawableBounds(drawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                            drawable.draw(canvas2);
                        }
                        canvas.save();
                        canvas2.translate((float) ((this.timeX + Theme.chat_msgInViewsDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                        this.viewsLayout.draw(canvas2);
                        canvas.restore();
                    }
                }
                canvas.save();
                canvas2.translate((float) (this.timeX + i), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                this.timeLayout.draw(canvas2);
                canvas.restore();
            }
            if (this.currentMessageObject.isOutOwner()) {
                Object obj;
                Object obj2;
                Object obj3 = 1;
                Object obj4 = ((int) (this.currentMessageObject.getDialogId() >> 32)) == 1 ? 1 : null;
                if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                    obj3 = null;
                    obj = null;
                    obj2 = null;
                    i2 = 1;
                } else if (this.currentMessageObject.isSendError()) {
                    obj3 = null;
                    obj = null;
                    obj2 = 1;
                } else {
                    if (this.currentMessageObject.isSent()) {
                        MessageObject messageObject2 = this.currentMessageObject;
                        if (!(messageObject2.scheduled || messageObject2.isUnread())) {
                            obj = 1;
                            obj2 = null;
                        }
                    } else {
                        obj3 = null;
                    }
                    obj = null;
                    obj2 = null;
                }
                if (i2 != 0) {
                    if (!this.mediaBackground || this.captionLayout != null) {
                        BaseCell.setDrawableBounds(Theme.chat_msgOutClockDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - Theme.chat_msgOutClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - Theme.chat_msgOutClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgOutClockDrawable.draw(canvas2);
                    } else if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                        Theme.chat_msgStickerClockDrawable.setAlpha((int) (this.timeAlpha * 255.0f));
                        BaseCell.setDrawableBounds(Theme.chat_msgStickerClockDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - Theme.chat_msgStickerClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgStickerClockDrawable.draw(canvas2);
                        Theme.chat_msgStickerClockDrawable.setAlpha(255);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_msgMediaClockDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgMediaClockDrawable.draw(canvas2);
                    }
                }
                if (obj4 == null) {
                    Drawable drawable2;
                    if (obj3 != null) {
                        if (!this.mediaBackground || this.captionLayout != null) {
                            if (obj != null) {
                                drawable2 = isDrawSelectionBackground() ? Theme.chat_msgOutCheckReadSelectedDrawable : Theme.chat_msgOutCheckReadDrawable;
                                BaseCell.setDrawableBounds(drawable2, (this.layoutWidth - AndroidUtilities.dp(22.5f)) - drawable2.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable2.getIntrinsicHeight());
                            } else {
                                drawable2 = isDrawSelectionBackground() ? Theme.chat_msgOutCheckSelectedDrawable : Theme.chat_msgOutCheckDrawable;
                                BaseCell.setDrawableBounds(drawable2, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - drawable2.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable2.getIntrinsicHeight());
                            }
                            drawable2.draw(canvas2);
                        } else if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                            if (obj != null) {
                                BaseCell.setDrawableBounds(Theme.chat_msgStickerCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(26.3f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
                            } else {
                                BaseCell.setDrawableBounds(Theme.chat_msgStickerCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
                            }
                            Theme.chat_msgStickerCheckDrawable.draw(canvas2);
                        } else {
                            if (obj != null) {
                                BaseCell.setDrawableBounds(Theme.chat_msgMediaCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(26.3f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
                            } else {
                                BaseCell.setDrawableBounds(Theme.chat_msgMediaCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
                            }
                            Theme.chat_msgMediaCheckDrawable.setAlpha((int) (this.timeAlpha * 255.0f));
                            Theme.chat_msgMediaCheckDrawable.draw(canvas2);
                            Theme.chat_msgMediaCheckDrawable.setAlpha(255);
                        }
                    }
                    if (obj != null) {
                        if (!this.mediaBackground || this.captionLayout != null) {
                            drawable2 = isDrawSelectionBackground() ? Theme.chat_msgOutHalfCheckSelectedDrawable : Theme.chat_msgOutHalfCheckDrawable;
                            BaseCell.setDrawableBounds(drawable2, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - drawable2.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable2.getIntrinsicHeight());
                            drawable2.draw(canvas2);
                        } else if (this.currentMessageObject.shouldDrawWithoutBackground()) {
                            BaseCell.setDrawableBounds(Theme.chat_msgStickerHalfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicHeight());
                            Theme.chat_msgStickerHalfCheckDrawable.draw(canvas2);
                        } else {
                            BaseCell.setDrawableBounds(Theme.chat_msgMediaHalfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicHeight());
                            Theme.chat_msgMediaHalfCheckDrawable.setAlpha((int) (this.timeAlpha * 255.0f));
                            Theme.chat_msgMediaHalfCheckDrawable.draw(canvas2);
                            Theme.chat_msgMediaHalfCheckDrawable.setAlpha(255);
                        }
                    }
                } else if (!(obj == null && obj3 == null)) {
                    if (this.mediaBackground && this.captionLayout == null) {
                        BaseCell.setDrawableBounds(Theme.chat_msgBroadcastMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(24.0f)) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(14.0f)) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicHeight());
                        Theme.chat_msgBroadcastMediaDrawable.draw(canvas2);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_msgBroadcastDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - Theme.chat_msgBroadcastDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.chat_msgBroadcastDrawable.getIntrinsicHeight());
                        Theme.chat_msgBroadcastDrawable.draw(canvas2);
                    }
                }
                if (obj2 != null) {
                    if (this.mediaBackground && this.captionLayout == null) {
                        i = this.layoutWidth - AndroidUtilities.dp(34.5f);
                        dp = this.layoutHeight;
                        dp2 = AndroidUtilities.dp(26.5f);
                    } else {
                        i = this.layoutWidth - AndroidUtilities.dp(32.0f);
                        dp = this.layoutHeight;
                        dp2 = AndroidUtilities.dp(21.0f);
                    }
                    dp -= dp2;
                    this.rect.set((float) i, (float) dp, (float) (AndroidUtilities.dp(14.0f) + i), (float) (AndroidUtilities.dp(14.0f) + dp));
                    canvas2.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                    BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, i + AndroidUtilities.dp(6.0f), dp + AndroidUtilities.dp(2.0f));
                    Theme.chat_msgErrorDrawable.draw(canvas2);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:374:0x0c0c  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0c2e  */
    /* JADX WARNING: Removed duplicated region for block: B:397:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0c3c  */
    public void drawOverlays(android.graphics.Canvas r24) {
        /*
        r23 = this;
        r0 = r23;
        r7 = r24;
        r1 = android.os.SystemClock.uptimeMillis();
        r3 = r0.lastAnimationTime;
        r3 = r1 - r3;
        r5 = 17;
        r8 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r8 <= 0) goto L_0x0014;
    L_0x0012:
        r3 = 17;
    L_0x0014:
        r0.lastAnimationTime = r1;
        r1 = r0.currentMessageObject;
        r1 = r1.hadAnimationNotReadyLoading;
        r2 = 4;
        r8 = 2;
        r9 = 1;
        r10 = 0;
        if (r1 == 0) goto L_0x004e;
    L_0x0020:
        r1 = r0.photoImage;
        r1 = r1.getVisible();
        if (r1 == 0) goto L_0x004e;
    L_0x0028:
        r1 = r0.currentMessageObject;
        r1 = r1.needDrawBluredPreview();
        if (r1 != 0) goto L_0x004e;
    L_0x0030:
        r1 = r0.documentAttachType;
        r5 = 7;
        if (r1 == r5) goto L_0x0039;
    L_0x0035:
        if (r1 == r2) goto L_0x0039;
    L_0x0037:
        if (r1 != r8) goto L_0x004e;
    L_0x0039:
        r1 = r0.photoImage;
        r1 = r1.getAnimation();
        if (r1 == 0) goto L_0x004e;
    L_0x0041:
        r1 = r1.hasBitmap();
        if (r1 == 0) goto L_0x004e;
    L_0x0047:
        r1 = r0.currentMessageObject;
        r1.hadAnimationNotReadyLoading = r10;
        r0.updateButtonState(r10, r9, r10);
    L_0x004e:
        r1 = r0.currentMessageObject;
        r5 = r1.type;
        r11 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r12 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r13 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r14 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r15 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r5 == r9) goto L_0x0934;
    L_0x005e:
        r6 = r0.documentAttachType;
        if (r6 == r2) goto L_0x0934;
    L_0x0062:
        if (r6 != r8) goto L_0x0066;
    L_0x0064:
        goto L_0x0934;
    L_0x0066:
        r3 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
        r17 = NUM; // 0x40CLASSNAME float:6.0 double:5.367157323E-315;
        r18 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r5 != r2) goto L_0x029a;
    L_0x006e:
        r2 = r0.docTitleLayout;
        if (r2 == 0) goto L_0x0bf8;
    L_0x0072:
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x0098;
    L_0x0078:
        r1 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r2 = "chat_messageTextOut";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x008e;
    L_0x008b:
        r2 = "chat_outVenueInfoSelectedText";
        goto L_0x0090;
    L_0x008e:
        r2 = "chat_outVenueInfoText";
    L_0x0090:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        goto L_0x00b7;
    L_0x0098:
        r1 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r2 = "chat_messageTextIn";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x00ae;
    L_0x00ab:
        r2 = "chat_inVenueInfoSelectedText";
        goto L_0x00b0;
    L_0x00ae:
        r2 = "chat_inVenueInfoText";
    L_0x00b0:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
    L_0x00b7:
        r1 = r0.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.media;
        r1 = r1 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
        if (r1 == 0) goto L_0x025c;
    L_0x00c1:
        r1 = r0.photoImage;
        r1 = r1.getImageY2();
        r2 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r10 = r1 + r2;
        r1 = r0.locationExpired;
        if (r1 != 0) goto L_0x0202;
    L_0x00d3:
        r0.forceNotDrawTime = r9;
        r1 = r0.currentAccount;
        r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r1);
        r1 = r1.getCurrentTime();
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.date;
        r1 = r1 - r2;
        r1 = java.lang.Math.abs(r1);
        r1 = (float) r1;
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.media;
        r2 = r2.period;
        r2 = (float) r2;
        r1 = r1 / r2;
        r1 = r15 - r1;
        r2 = r0.rect;
        r4 = r0.photoImage;
        r4 = r4.getImageX2();
        r5 = NUM; // 0x422CLASSNAME float:43.0 double:5.485017196E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r5 = r10 - r5;
        r5 = (float) r5;
        r6 = r0.photoImage;
        r6 = r6.getImageX2();
        r9 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r6 = r6 - r9;
        r6 = (float) r6;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r9 = r9 + r10;
        r9 = (float) r9;
        r2.set(r4, r5, r6, r9);
        r2 = r0.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0144;
    L_0x012d:
        r2 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r4 = "chat_outInstant";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
        r2 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r4 = "chat_outInstant";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
        goto L_0x015a;
    L_0x0144:
        r2 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r4 = "chat_inInstant";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
        r2 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r4 = "chat_inInstant";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r2.setColor(r4);
    L_0x015a:
        r2 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r4 = 50;
        r2.setAlpha(r4);
        r2 = r0.rect;
        r2 = r2.centerX();
        r4 = r0.rect;
        r4 = r4.centerY();
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = (float) r3;
        r5 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r7.drawCircle(r2, r4, r3, r5);
        r2 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r2.setAlpha(r12);
        r2 = r0.rect;
        r3 = -NUM; // 0xffffffffc2b40000 float:-90.0 double:NaN;
        r4 = -NUM; // 0xffffffffc3b40000 float:-360.0 double:NaN;
        r4 = r4 * r1;
        r5 = 0;
        r6 = org.telegram.ui.ActionBar.Theme.chat_radialProgress2Paint;
        r1 = r24;
        r9 = 0;
        r1.drawArc(r2, r3, r4, r5, r6);
        r1 = r0.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = r1.media;
        r1 = r1.period;
        r2 = r0.currentAccount;
        r2 = org.telegram.tgnet.ConnectionsManager.getInstance(r2);
        r2 = r2.getCurrentTime();
        r3 = r0.currentMessageObject;
        r3 = r3.messageOwner;
        r3 = r3.date;
        r2 = r2 - r3;
        r1 = r1 - r2;
        r1 = java.lang.Math.abs(r1);
        r1 = org.telegram.messenger.LocaleController.formatLocationLeftTime(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r2 = r2.measureText(r1);
        r3 = r0.rect;
        r3 = r3.centerX();
        r2 = r2 / r18;
        r3 = r3 - r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r10 = r10 + r2;
        r2 = (float) r10;
        r4 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r7.drawText(r1, r3, r2, r4);
        r24.save();
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 + r2;
        r1 = (float) r1;
        r2 = r0.photoImage;
        r2 = r2.getImageY2();
        r3 = NUM; // 0x41200000 float:10.0 double:5.398241246E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.docTitleLayout;
        r1.draw(r7);
        r1 = NUM; // 0x41b80000 float:23.0 double:5.447457457E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r7.translate(r9, r1);
        r1 = r0.infoLayout;
        r1.draw(r7);
        r24.restore();
    L_0x0202:
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = r0.photoImage;
        r2 = r2.getImageWidth();
        r2 = r2 / r8;
        r1 = r1 + r2;
        r2 = NUM; // 0x41var_ float:31.0 double:5.46818007E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 - r2;
        r2 = r0.photoImage;
        r2 = r2.getImageY();
        r3 = r0.photoImage;
        r3 = r3.getImageHeight();
        r3 = r3 / r8;
        r2 = r2 + r3;
        r3 = NUM; // 0x42180000 float:38.0 double:5.47854138E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgAvatarLiveLocationDrawable;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r3, r1, r2);
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgAvatarLiveLocationDrawable;
        r3.draw(r7);
        r3 = r0.locationImageReceiver;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r1 = r1 + r4;
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r2 = r2 + r4;
        r4 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r5 = NUM; // 0x42500000 float:52.0 double:5.496673668E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r3.setImageCoords(r1, r2, r4, r5);
        r1 = r0.locationImageReceiver;
        r1.draw(r7);
        goto L_0x0bf8;
    L_0x025c:
        r9 = 0;
        r24.save();
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r1 = r1 + r2;
        r1 = (float) r1;
        r2 = r0.photoImage;
        r2 = r2.getImageY2();
        r3 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.docTitleLayout;
        r1.draw(r7);
        r1 = r0.infoLayout;
        if (r1 == 0) goto L_0x0295;
    L_0x0286:
        r1 = NUM; // 0x41a80000 float:21.0 double:5.442276803E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r7.translate(r9, r1);
        r1 = r0.infoLayout;
        r1.draw(r7);
    L_0x0295:
        r24.restore();
        goto L_0x0bf8;
    L_0x029a:
        r8 = 0;
        r2 = 16;
        if (r5 != r2) goto L_0x03c6;
    L_0x029f:
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x02c5;
    L_0x02a5:
        r1 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r2 = "chat_messageTextOut";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x02bb;
    L_0x02b8:
        r2 = "chat_outTimeSelectedText";
        goto L_0x02bd;
    L_0x02bb:
        r2 = "chat_outTimeText";
    L_0x02bd:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        goto L_0x02e4;
    L_0x02c5:
        r1 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r2 = "chat_messageTextIn";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x02db;
    L_0x02d8:
        r2 = "chat_inTimeSelectedText";
        goto L_0x02dd;
    L_0x02db:
        r2 = "chat_inTimeText";
    L_0x02dd:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
    L_0x02e4:
        r0.forceNotDrawTime = r9;
        r1 = r0.currentMessageObject;
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x02fb;
    L_0x02ee:
        r1 = r0.layoutWidth;
        r2 = r0.backgroundWidth;
        r1 = r1 - r2;
        r2 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 + r2;
        goto L_0x0314;
    L_0x02fb:
        r1 = r0.isChat;
        if (r1 == 0) goto L_0x030e;
    L_0x02ff:
        r1 = r0.currentMessageObject;
        r1 = r1.needDrawAvatar();
        if (r1 == 0) goto L_0x030e;
    L_0x0307:
        r1 = NUM; // 0x42940000 float:74.0 double:5.518691446E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        goto L_0x0314;
    L_0x030e:
        r1 = NUM; // 0x41CLASSNAME float:25.0 double:5.45263811E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
    L_0x0314:
        r0.otherX = r1;
        r2 = r0.titleLayout;
        if (r2 == 0) goto L_0x0333;
    L_0x031a:
        r24.save();
        r2 = (float) r1;
        r3 = NUM; // 0x41400000 float:12.0 double:5.408602553E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r0.namesOffset;
        r3 = r3 + r4;
        r3 = (float) r3;
        r7.translate(r2, r3);
        r2 = r0.titleLayout;
        r2.draw(r7);
        r24.restore();
    L_0x0333:
        r2 = r0.docTitleLayout;
        if (r2 == 0) goto L_0x0357;
    L_0x0337:
        r24.save();
        r2 = NUM; // 0x41980000 float:19.0 double:5.43709615E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r2 + r1;
        r2 = (float) r2;
        r3 = NUM; // 0x42140000 float:37.0 double:5.477246216E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = r0.namesOffset;
        r3 = r3 + r4;
        r3 = (float) r3;
        r7.translate(r2, r3);
        r2 = r0.docTitleLayout;
        r2.draw(r7);
        r24.restore();
    L_0x0357:
        r2 = r0.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0372;
    L_0x035f:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgCallUpGreenDrawable;
        r3 = r23.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x036f;
    L_0x0367:
        r3 = r0.otherPressed;
        if (r3 == 0) goto L_0x036c;
    L_0x036b:
        goto L_0x036f;
    L_0x036c:
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgOutCallDrawable;
        goto L_0x0398;
    L_0x036f:
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgOutCallSelectedDrawable;
        goto L_0x0398;
    L_0x0372:
        r2 = r0.currentMessageObject;
        r2 = r2.messageOwner;
        r2 = r2.action;
        r2 = r2.reason;
        r3 = r2 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
        if (r3 != 0) goto L_0x0386;
    L_0x037e:
        r2 = r2 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
        if (r2 == 0) goto L_0x0383;
    L_0x0382:
        goto L_0x0386;
    L_0x0383:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgCallDownGreenDrawable;
        goto L_0x0388;
    L_0x0386:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgCallDownRedDrawable;
    L_0x0388:
        r3 = r23.isDrawSelectionBackground();
        if (r3 != 0) goto L_0x0396;
    L_0x038e:
        r3 = r0.otherPressed;
        if (r3 == 0) goto L_0x0393;
    L_0x0392:
        goto L_0x0396;
    L_0x0393:
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgInCallDrawable;
        goto L_0x0398;
    L_0x0396:
        r3 = org.telegram.ui.ActionBar.Theme.chat_msgInCallSelectedDrawable;
    L_0x0398:
        r4 = NUM; // 0x40400000 float:3.0 double:5.325712093E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r1 - r4;
        r5 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r6 = r0.namesOffset;
        r5 = r5 + r6;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r2, r4, r5);
        r2.draw(r7);
        r2 = NUM; // 0x434d0000 float:205.0 double:5.578592756E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 + r2;
        r2 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r0.otherY = r2;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r3, r1, r2);
        r3.draw(r7);
        goto L_0x0bf8;
    L_0x03c6:
        r2 = 17;
        r16 = NUM; // 0x41100000 float:9.0 double:5.39306059E-315;
        if (r5 != r2) goto L_0x079b;
    L_0x03cc:
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x0401;
    L_0x03d2:
        r1 = "chat_messageTextOut";
        r1 = org.telegram.ui.ActionBar.Theme.getColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r2.setColor(r1);
        r1 = r23.isDrawSelectionBackground();
        if (r1 == 0) goto L_0x03f0;
    L_0x03ed:
        r1 = "chat_outTimeSelectedText";
        goto L_0x03f2;
    L_0x03f0:
        r1 = "chat_outTimeText";
    L_0x03f2:
        r1 = org.telegram.ui.ActionBar.Theme.getColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r2.setColor(r1);
        goto L_0x042f;
    L_0x0401:
        r1 = "chat_messageTextIn";
        r1 = org.telegram.ui.ActionBar.Theme.getColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_audioPerformerPaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r2.setColor(r1);
        r1 = r23.isDrawSelectionBackground();
        if (r1 == 0) goto L_0x041f;
    L_0x041c:
        r1 = "chat_inTimeSelectedText";
        goto L_0x0421;
    L_0x041f:
        r1 = "chat_inTimeText";
    L_0x0421:
        r1 = org.telegram.ui.ActionBar.Theme.getColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_timePaint;
        r2.setColor(r1);
        r2 = org.telegram.ui.ActionBar.Theme.chat_livePaint;
        r2.setColor(r1);
    L_0x042f:
        r1 = r0.currentMessageObject;
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x0445;
    L_0x0437:
        r1 = r0.layoutWidth;
        r2 = r0.backgroundWidth;
        r1 = r1 - r2;
        r2 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 + r2;
    L_0x0443:
        r6 = r1;
        goto L_0x045f;
    L_0x0445:
        r1 = r0.isChat;
        if (r1 == 0) goto L_0x0458;
    L_0x0449:
        r1 = r0.currentMessageObject;
        r1 = r1.needDrawAvatar();
        if (r1 == 0) goto L_0x0458;
    L_0x0451:
        r1 = NUM; // 0x42880000 float:68.0 double:5.514805956E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        goto L_0x0443;
    L_0x0458:
        r1 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        goto L_0x0443;
    L_0x045f:
        r1 = r0.titleLayout;
        if (r1 == 0) goto L_0x047a;
    L_0x0463:
        r24.save();
        r1 = (float) r6;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r0.namesOffset;
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.titleLayout;
        r1.draw(r7);
        r24.restore();
    L_0x047a:
        r1 = r0.docTitleLayout;
        if (r1 == 0) goto L_0x04a5;
    L_0x047e:
        r24.save();
        r1 = r0.docTitleOffsetX;
        r1 = r1 + r6;
        r1 = (float) r1;
        r2 = r0.titleLayout;
        if (r2 == 0) goto L_0x048e;
    L_0x0489:
        r2 = r2.getHeight();
        goto L_0x048f;
    L_0x048e:
        r2 = 0;
    L_0x048f:
        r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r3 = r0.namesOffset;
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.docTitleLayout;
        r1.draw(r7);
        r24.restore();
    L_0x04a5:
        r1 = android.os.Build.VERSION.SDK_INT;
        r2 = 21;
        if (r1 < r2) goto L_0x04b4;
    L_0x04ab:
        r1 = r0.selectorDrawable;
        if (r1 == 0) goto L_0x04b4;
    L_0x04af:
        r0.selectorDrawableMaskType = r9;
        r1.draw(r7);
    L_0x04b4:
        r1 = r0.pollButtons;
        r5 = r1.size();
        r4 = 0;
        r19 = 0;
    L_0x04bd:
        if (r4 >= r5) goto L_0x0774;
    L_0x04bf:
        r1 = r0.pollButtons;
        r1 = r1.get(r4);
        r3 = r1;
        r3 = (org.telegram.ui.Cells.ChatMessageCell.PollButton) r3;
        r3.x = r6;
        r24.save();
        r1 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = r1 + r6;
        r1 = (float) r1;
        r2 = r3.y;
        r13 = r0.namesOffset;
        r2 = r2 + r13;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r3.title;
        r1.draw(r7);
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x0502;
    L_0x04ec:
        r1 = r0.pollUnvoteInProgress;
        if (r1 == 0) goto L_0x04f5;
    L_0x04f0:
        r1 = r0.pollAnimationProgress;
        r1 = r15 - r1;
        goto L_0x04f7;
    L_0x04f5:
        r1 = r0.pollAnimationProgress;
    L_0x04f7:
        r2 = NUM; // 0x3e99999a float:0.3 double:5.188942835E-315;
        r1 = r1 / r2;
        r1 = java.lang.Math.min(r1, r15);
        r1 = r1 * r14;
        goto L_0x0504;
    L_0x0502:
        r1 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
    L_0x0504:
        r13 = (int) r1;
        r1 = r0.pollVoted;
        if (r1 != 0) goto L_0x0511;
    L_0x0509:
        r1 = r0.pollClosed;
        if (r1 != 0) goto L_0x0511;
    L_0x050d:
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x05df;
    L_0x0511:
        r1 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r2 = r0.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x051e;
    L_0x051b:
        r2 = "chat_outAudioSeekbarFill";
        goto L_0x0520;
    L_0x051e:
        r2 = "chat_inAudioSeekbarFill";
    L_0x0520:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x054c;
    L_0x052b:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r1 = r1.getAlpha();
        r1 = (float) r1;
        r1 = r1 / r14;
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r15 = (float) r13;
        r1 = r1 * r15;
        r1 = (int) r1;
        r2.setAlpha(r1);
        r1 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r1 = r1.getAlpha();
        r1 = (float) r1;
        r1 = r1 / r14;
        r2 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r15 = r15 * r1;
        r1 = (int) r15;
        r2.setAlpha(r1);
    L_0x054c:
        r1 = r3.prevPercent;
        r1 = (float) r1;
        r2 = r3.percent;
        r15 = r3.prevPercent;
        r2 = r2 - r15;
        r2 = (float) r2;
        r15 = r0.pollAnimationProgress;
        r2 = r2 * r15;
        r1 = r1 + r2;
        r1 = (double) r1;
        r1 = java.lang.Math.ceil(r1);
        r1 = (int) r1;
        r2 = new java.lang.Object[r9];
        r1 = java.lang.Integer.valueOf(r1);
        r2[r10] = r1;
        r1 = "%d%%";
        r1 = java.lang.String.format(r1, r2);
        r2 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r2 = r2.measureText(r1);
        r9 = (double) r2;
        r9 = java.lang.Math.ceil(r9);
        r2 = (int) r9;
        r9 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r9 = -r9;
        r9 = r9 - r2;
        r2 = (float) r9;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r9 = (float) r9;
        r10 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r7.drawText(r1, r2, r9, r10);
        r1 = r0.backgroundWidth;
        r2 = NUM; // 0x42980000 float:76.0 double:5.51998661E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 - r2;
        r2 = r3.prevPercentProgress;
        r9 = r3.percentProgress;
        r10 = r3.prevPercentProgress;
        r9 = r9 - r10;
        r10 = r0.pollAnimationProgress;
        r9 = r9 * r10;
        r2 = r2 + r9;
        r9 = r0.instantButtonRect;
        r10 = r3.height;
        r20 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r10 = r10 + r20;
        r10 = (float) r10;
        r1 = (float) r1;
        r1 = r1 * r2;
        r2 = r3.height;
        r20 = NUM; // 0x41300000 float:11.0 double:5.4034219E-315;
        r20 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r2 = r2 + r20;
        r2 = (float) r2;
        r9.set(r8, r10, r1, r2);
        r1 = r0.instantButtonRect;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r2 = (float) r2;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r9 = (float) r9;
        r10 = org.telegram.ui.ActionBar.Theme.chat_docBackPaint;
        r7.drawRoundRect(r1, r2, r9, r10);
    L_0x05df:
        r1 = r0.pollVoted;
        if (r1 != 0) goto L_0x05e7;
    L_0x05e3:
        r1 = r0.pollClosed;
        if (r1 == 0) goto L_0x05eb;
    L_0x05e7:
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x074c;
    L_0x05eb:
        r1 = r23.isDrawSelectionBackground();
        if (r1 == 0) goto L_0x0608;
    L_0x05f1:
        r1 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r2 = r0.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x05fe;
    L_0x05fb:
        r2 = "chat_outVoiceSeekbarSelected";
        goto L_0x0600;
    L_0x05fe:
        r2 = "chat_inVoiceSeekbarSelected";
    L_0x0600:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        goto L_0x061e;
    L_0x0608:
        r1 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r2 = r0.currentMessageObject;
        r2 = r2.isOutOwner();
        if (r2 == 0) goto L_0x0615;
    L_0x0612:
        r2 = "chat_outVoiceSeekbar";
        goto L_0x0617;
    L_0x0615:
        r2 = "chat_inVoiceSeekbar";
    L_0x0617:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
    L_0x061e:
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x0635;
    L_0x0622:
        r1 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r1 = r1.getAlpha();
        r1 = (float) r1;
        r1 = r1 / r14;
        r2 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r9 = 255 - r13;
        r9 = (float) r9;
        r9 = r9 * r1;
        r1 = (int) r9;
        r2.setAlpha(r1);
    L_0x0635:
        r1 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r1 = -r1;
        r2 = (float) r1;
        r1 = r3.height;
        r9 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r1 = r1 + r9;
        r9 = (float) r1;
        r1 = r0.backgroundWidth;
        r10 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r1 = r1 - r10;
        r10 = (float) r1;
        r1 = r3.height;
        r20 = NUM; // 0x41500000 float:13.0 double:5.413783207E-315;
        r20 = org.telegram.messenger.AndroidUtilities.dp(r20);
        r1 = r1 + r20;
        r1 = (float) r1;
        r20 = org.telegram.ui.ActionBar.Theme.chat_replyLinePaint;
        r21 = r1;
        r1 = r24;
        r22 = r3;
        r3 = r9;
        r9 = r4;
        r4 = r10;
        r10 = r5;
        r5 = r21;
        r21 = r6;
        r6 = r20;
        r1.drawLine(r2, r3, r4, r5, r6);
        r1 = r0.pollVoteInProgress;
        r2 = NUM; // 0x41080000 float:8.5 double:5.390470265E-315;
        if (r1 == 0) goto L_0x06ec;
    L_0x0679:
        r1 = r0.pollVoteInProgressNum;
        if (r9 != r1) goto L_0x06ec;
    L_0x067d:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r3 = r0.currentMessageObject;
        r3 = r3.isOutOwner();
        if (r3 == 0) goto L_0x068a;
    L_0x0687:
        r3 = "chat_outAudioSeekbarFill";
        goto L_0x068c;
    L_0x068a:
        r3 = "chat_inAudioSeekbarFill";
    L_0x068c:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r1.setColor(r3);
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x06aa;
    L_0x0697:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r1 = r1.getAlpha();
        r1 = (float) r1;
        r1 = r1 / r14;
        r3 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r4 = 255 - r13;
        r4 = (float) r4;
        r4 = r4 * r1;
        r1 = (int) r4;
        r3.setAlpha(r1);
    L_0x06aa:
        r1 = r0.instantButtonRect;
        r3 = NUM; // 0x41b80000 float:23.0 double:5.447457457E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = -r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r3 - r4;
        r3 = (float) r3;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r5 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = NUM; // 0x41b80000 float:23.0 double:5.447457457E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = -r5;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r5 = r5 + r6;
        r5 = (float) r5;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r6 = r6 + r2;
        r2 = (float) r6;
        r1.set(r3, r4, r5, r2);
        r2 = r0.instantButtonRect;
        r3 = r0.voteRadOffset;
        r4 = r0.voteCurrentCircleLength;
        r5 = 0;
        r6 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r1 = r24;
        r1.drawArc(r2, r3, r4, r5, r6);
        goto L_0x0752;
    L_0x06ec:
        r1 = r0.currentMessageObject;
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x0709;
    L_0x06f4:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r3 = r23.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x06ff;
    L_0x06fc:
        r3 = "chat_outMenuSelected";
        goto L_0x0701;
    L_0x06ff:
        r3 = "chat_outMenu";
    L_0x0701:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r1.setColor(r3);
        goto L_0x071d;
    L_0x0709:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r3 = r23.isDrawSelectionBackground();
        if (r3 == 0) goto L_0x0714;
    L_0x0711:
        r3 = "chat_inMenuSelected";
        goto L_0x0716;
    L_0x0714:
        r3 = "chat_inMenu";
    L_0x0716:
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r3);
        r1.setColor(r3);
    L_0x071d:
        r1 = r0.animatePollAnswerAlpha;
        if (r1 == 0) goto L_0x0734;
    L_0x0721:
        r1 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r1 = r1.getAlpha();
        r1 = (float) r1;
        r1 = r1 / r14;
        r3 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r4 = 255 - r13;
        r4 = (float) r4;
        r4 = r4 * r1;
        r1 = (int) r4;
        r3.setAlpha(r1);
    L_0x0734:
        r1 = NUM; // 0x41b80000 float:23.0 double:5.447457457E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = -r1;
        r1 = (float) r1;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r3 = (float) r3;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = (float) r2;
        r4 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r7.drawCircle(r1, r3, r2, r4);
        goto L_0x0752;
    L_0x074c:
        r22 = r3;
        r9 = r4;
        r10 = r5;
        r21 = r6;
    L_0x0752:
        r24.restore();
        r5 = r10 + -1;
        if (r9 != r5) goto L_0x0767;
    L_0x0759:
        r1 = r22.y;
        r2 = r0.namesOffset;
        r1 = r1 + r2;
        r2 = r22.height;
        r1 = r1 + r2;
        r19 = r1;
    L_0x0767:
        r4 = r9 + 1;
        r5 = r10;
        r6 = r21;
        r9 = 1;
        r10 = 0;
        r13 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r15 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x04bd;
    L_0x0774:
        r21 = r6;
        r1 = r0.infoLayout;
        if (r1 == 0) goto L_0x0796;
    L_0x077a:
        r24.save();
        r1 = r0.infoX;
        r6 = r21 + r1;
        r1 = (float) r6;
        r2 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = r19 + r2;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.infoLayout;
        r1.draw(r7);
        r24.restore();
    L_0x0796:
        r23.updatePollAnimations();
        goto L_0x0bf8;
    L_0x079b:
        r2 = 12;
        if (r5 != r2) goto L_0x0bf8;
    L_0x079f:
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x07c5;
    L_0x07a5:
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r2 = "chat_outContactNameText";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x07bb;
    L_0x07b8:
        r2 = "chat_outContactPhoneSelectedText";
        goto L_0x07bd;
    L_0x07bb:
        r2 = "chat_outContactPhoneText";
    L_0x07bd:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        goto L_0x07e4;
    L_0x07c5:
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r2 = "chat_inContactNameText";
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
        r1 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r2 = r23.isDrawSelectionBackground();
        if (r2 == 0) goto L_0x07db;
    L_0x07d8:
        r2 = "chat_inContactPhoneSelectedText";
        goto L_0x07dd;
    L_0x07db:
        r2 = "chat_inContactPhoneText";
    L_0x07dd:
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r2);
        r1.setColor(r2);
    L_0x07e4:
        r1 = r0.titleLayout;
        if (r1 == 0) goto L_0x0813;
    L_0x07e8:
        r24.save();
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = r0.photoImage;
        r2 = r2.getImageWidth();
        r1 = r1 + r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r1 = r1 + r2;
        r1 = (float) r1;
        r2 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r0.namesOffset;
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.titleLayout;
        r1.draw(r7);
        r24.restore();
    L_0x0813:
        r1 = r0.docTitleLayout;
        if (r1 == 0) goto L_0x0842;
    L_0x0817:
        r24.save();
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = r0.photoImage;
        r2 = r2.getImageWidth();
        r1 = r1 + r2;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r16);
        r1 = r1 + r2;
        r1 = (float) r1;
        r2 = NUM; // 0x421CLASSNAME float:39.0 double:5.479836543E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = r0.namesOffset;
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.docTitleLayout;
        r1.draw(r7);
        r24.restore();
    L_0x0842:
        r1 = r0.currentMessageObject;
        r1 = r1.isOutOwner();
        if (r1 == 0) goto L_0x0856;
    L_0x084a:
        r1 = r23.isDrawSelectionBackground();
        if (r1 == 0) goto L_0x0853;
    L_0x0850:
        r1 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuSelectedDrawable;
        goto L_0x0861;
    L_0x0853:
        r1 = org.telegram.ui.ActionBar.Theme.chat_msgOutMenuDrawable;
        goto L_0x0861;
    L_0x0856:
        r1 = r23.isDrawSelectionBackground();
        if (r1 == 0) goto L_0x085f;
    L_0x085c:
        r1 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuSelectedDrawable;
        goto L_0x0861;
    L_0x085f:
        r1 = org.telegram.ui.ActionBar.Theme.chat_msgInMenuDrawable;
    L_0x0861:
        r2 = r0.photoImage;
        r2 = r2.getImageX();
        r3 = r0.backgroundWidth;
        r2 = r2 + r3;
        r3 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r0.otherX = r2;
        r3 = r0.photoImage;
        r3 = r3.getImageY();
        r4 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r0.otherY = r3;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r1, r2, r3);
        r1.draw(r7);
        r1 = r0.drawInstantView;
        if (r1 == 0) goto L_0x0bf8;
    L_0x088c:
        r1 = r0.photoImage;
        r1 = r1.getImageX();
        r2 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r1 = r1 - r2;
        r2 = r23.getMeasuredHeight();
        r3 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 - r3;
        r3 = org.telegram.ui.ActionBar.Theme.chat_instantViewRectPaint;
        r4 = r0.currentMessageObject;
        r4 = r4.isOutOwner();
        if (r4 == 0) goto L_0x08c1;
    L_0x08ac:
        r4 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r5 = "chat_outPreviewInstantText";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
        r4 = "chat_outPreviewInstantText";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
        goto L_0x08d5;
    L_0x08c1:
        r4 = org.telegram.ui.ActionBar.Theme.chat_instantViewPaint;
        r5 = "chat_inPreviewInstantText";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r4.setColor(r5);
        r4 = "chat_inPreviewInstantText";
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r4);
        r3.setColor(r4);
    L_0x08d5:
        r4 = android.os.Build.VERSION.SDK_INT;
        r5 = 21;
        if (r4 < r5) goto L_0x08f2;
    L_0x08db:
        r4 = 0;
        r0.selectorDrawableMaskType = r4;
        r4 = r0.selectorDrawable;
        r5 = r0.instantWidth;
        r5 = r5 + r1;
        r6 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r2;
        r4.setBounds(r1, r2, r5, r6);
        r4 = r0.selectorDrawable;
        r4.draw(r7);
    L_0x08f2:
        r4 = r0.instantButtonRect;
        r5 = (float) r1;
        r6 = (float) r2;
        r8 = r0.instantWidth;
        r8 = r8 + r1;
        r8 = (float) r8;
        r9 = NUM; // 0x42100000 float:36.0 double:5.47595105E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r9 = r9 + r2;
        r9 = (float) r9;
        r4.set(r5, r6, r8, r9);
        r4 = r0.instantButtonRect;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r5 = (float) r5;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r17);
        r6 = (float) r6;
        r7.drawRoundRect(r4, r5, r6, r3);
        r3 = r0.instantViewLayout;
        if (r3 == 0) goto L_0x0bf8;
    L_0x0918:
        r24.save();
        r3 = r0.instantTextX;
        r1 = r1 + r3;
        r1 = (float) r1;
        r3 = NUM; // 0x41280000 float:10.5 double:5.40083157E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r2 = (float) r2;
        r7.translate(r1, r2);
        r1 = r0.instantViewLayout;
        r1.draw(r7);
        r24.restore();
        goto L_0x0bf8;
    L_0x0934:
        r1 = 0;
        r5 = r0.photoImage;
        r5 = r5.getVisible();
        if (r5 == 0) goto L_0x0bf8;
    L_0x093d:
        r5 = r0.currentMessageObject;
        r5 = r5.needDrawBluredPreview();
        if (r5 != 0) goto L_0x09ab;
    L_0x0945:
        r5 = r0.documentAttachType;
        if (r5 != r2) goto L_0x09ab;
    L_0x0949:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r2 = (android.graphics.drawable.BitmapDrawable) r2;
        r2 = r2.getPaint();
        r2 = r2.getAlpha();
        r5 = r0.drawPhotoCheckBox;
        if (r5 == 0) goto L_0x096d;
    L_0x0959:
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r6 = (float) r2;
        r9 = r0.controlsAlpha;
        r6 = r6 * r9;
        r9 = r0.checkBoxAnimationProgress;
        r10 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r9 = r10 - r9;
        r6 = r6 * r9;
        r6 = (int) r6;
        r5.setAlpha(r6);
        goto L_0x0978;
    L_0x096d:
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r6 = (float) r2;
        r9 = r0.controlsAlpha;
        r6 = r6 * r9;
        r6 = (int) r6;
        r5.setAlpha(r6);
    L_0x0978:
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r6 = r0.photoImage;
        r6 = r6.getImageX();
        r9 = r0.photoImage;
        r9 = r9.getImageWidth();
        r6 = r6 + r9;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r6 = r6 - r9;
        r0.otherX = r6;
        r9 = r0.photoImage;
        r9 = r9.getImageY();
        r10 = NUM; // 0x4101999a float:8.1 double:5.388398005E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = r9 + r10;
        r0.otherY = r9;
        org.telegram.ui.Cells.BaseCell.setDrawableBounds(r5, r6, r9);
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r5.draw(r7);
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgMediaMenuDrawable;
        r5.setAlpha(r2);
    L_0x09ab:
        r2 = org.telegram.messenger.MediaController.getInstance();
        r5 = r0.currentMessageObject;
        r2 = r2.isPlayingMessage(r5);
        r5 = r0.animatingNoSoundPlaying;
        if (r5 == r2) goto L_0x09ca;
    L_0x09b9:
        r0.animatingNoSoundPlaying = r2;
        if (r2 == 0) goto L_0x09bf;
    L_0x09bd:
        r5 = 1;
        goto L_0x09c0;
    L_0x09bf:
        r5 = 2;
    L_0x09c0:
        r0.animatingNoSound = r5;
        if (r2 == 0) goto L_0x09c7;
    L_0x09c4:
        r5 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        goto L_0x09c8;
    L_0x09c7:
        r5 = 0;
    L_0x09c8:
        r0.animatingNoSoundProgress = r5;
    L_0x09ca:
        r5 = r0.buttonState;
        r6 = 1;
        if (r5 == r6) goto L_0x09e1;
    L_0x09cf:
        if (r5 == r8) goto L_0x09e1;
    L_0x09d1:
        if (r5 == 0) goto L_0x09e1;
    L_0x09d3:
        r6 = 3;
        if (r5 == r6) goto L_0x09e1;
    L_0x09d6:
        r6 = -1;
        if (r5 == r6) goto L_0x09e1;
    L_0x09d9:
        r5 = r0.currentMessageObject;
        r5 = r5.needDrawBluredPreview();
        if (r5 == 0) goto L_0x0b88;
    L_0x09e1:
        r5 = r0.autoPlayingMedia;
        if (r5 == 0) goto L_0x09e8;
    L_0x09e5:
        r23.updatePlayingMessageProgress();
    L_0x09e8:
        r5 = r0.infoLayout;
        if (r5 == 0) goto L_0x0b88;
    L_0x09ec:
        r5 = r0.forceNotDrawTime;
        if (r5 == 0) goto L_0x09f8;
    L_0x09f0:
        r5 = r0.autoPlayingMedia;
        if (r5 != 0) goto L_0x09f8;
    L_0x09f4:
        r5 = r0.drawVideoImageButton;
        if (r5 == 0) goto L_0x0b88;
    L_0x09f8:
        r5 = r0.currentMessageObject;
        r5 = r5.needDrawBluredPreview();
        if (r5 == 0) goto L_0x0a06;
    L_0x0a00:
        r5 = r0.docTitleLayout;
        if (r5 != 0) goto L_0x0a06;
    L_0x0a04:
        r6 = 0;
        goto L_0x0a08;
    L_0x0a06:
        r6 = r0.animatingDrawVideoImageButtonProgress;
    L_0x0a08:
        r5 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r9 = "chat_mediaInfoText";
        r9 = org.telegram.ui.ActionBar.Theme.getColor(r9);
        r5.setColor(r9);
        r5 = r0.photoImage;
        r5 = r5.getImageX();
        r9 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r5 = r5 + r10;
        r10 = r0.photoImage;
        r10 = r10.getImageY();
        r13 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r10 = r10 + r13;
        r9 = r0.autoPlayingMedia;
        if (r9 == 0) goto L_0x0a49;
    L_0x0a2f:
        if (r2 == 0) goto L_0x0a35;
    L_0x0a31:
        r2 = r0.animatingNoSound;
        if (r2 == 0) goto L_0x0a49;
    L_0x0a35:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgNoSoundDrawable;
        r2 = r2.getIntrinsicWidth();
        r9 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r2 = r2 + r13;
        r2 = (float) r2;
        r9 = r0.animatingNoSoundProgress;
        r2 = r2 * r9;
        r2 = (int) r2;
        goto L_0x0a4a;
    L_0x0a49:
        r2 = 0;
    L_0x0a4a:
        r9 = r0.infoWidth;
        r13 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r13);
        r9 = r9 + r13;
        r9 = r9 + r2;
        r9 = (float) r9;
        r13 = r0.infoWidth;
        r13 = r13 + r2;
        r15 = r0.docTitleWidth;
        r13 = java.lang.Math.max(r13, r15);
        r15 = r0.canStreamVideo;
        if (r15 == 0) goto L_0x0a69;
    L_0x0a62:
        r15 = NUM; // 0x42000000 float:32.0 double:5.4707704E-315;
        r15 = org.telegram.messenger.AndroidUtilities.dp(r15);
        goto L_0x0a6a;
    L_0x0a69:
        r15 = 0;
    L_0x0a6a:
        r13 = r13 + r15;
        r15 = r0.infoWidth;
        r13 = r13 - r15;
        r13 = r13 - r2;
        r13 = (float) r13;
        r13 = r13 * r6;
        r9 = r9 + r13;
        r12 = (double) r9;
        r12 = java.lang.Math.ceil(r12);
        r9 = (int) r12;
        r12 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1));
        if (r12 == 0) goto L_0x0a82;
    L_0x0a7d:
        r12 = r0.docTitleLayout;
        if (r12 != 0) goto L_0x0a82;
    L_0x0a81:
        r6 = 0;
    L_0x0a82:
        r12 = r0.rect;
        r13 = (float) r5;
        r15 = (float) r10;
        r5 = r5 + r9;
        r5 = (float) r5;
        r9 = NUM; // 0x41840000 float:16.5 double:5.43062033E-315;
        r18 = NUM; // 0x41780000 float:15.5 double:5.42673484E-315;
        r18 = r18 * r6;
        r18 = r18 + r9;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r18);
        r10 = r10 + r9;
        r9 = (float) r10;
        r12.set(r13, r15, r5, r9);
        r5 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r5 = r5.getAlpha();
        r9 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r10 = (float) r5;
        r12 = r0.controlsAlpha;
        r10 = r10 * r12;
        r10 = (int) r10;
        r9.setAlpha(r10);
        r9 = r0.rect;
        r10 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r12 = (float) r12;
        r13 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r10 = (float) r13;
        r13 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r7.drawRoundRect(r9, r12, r10, r13);
        r9 = org.telegram.ui.ActionBar.Theme.chat_timeBackgroundPaint;
        r9.setAlpha(r5);
        r5 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r9 = r0.controlsAlpha;
        r9 = r9 * r14;
        r9 = (int) r9;
        r5.setAlpha(r9);
        r24.save();
        r5 = r0.photoImage;
        r5 = r5.getImageX();
        r9 = NUM; // 0x41000000 float:8.0 double:5.38787994E-315;
        r10 = r0.canStreamVideo;
        if (r10 == 0) goto L_0x0ae0;
    L_0x0adb:
        r10 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r10 = r10 * r6;
        goto L_0x0ae1;
    L_0x0ae0:
        r10 = 0;
    L_0x0ae1:
        r10 = r10 + r9;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r5 = r5 + r9;
        r0.noSoundCenterX = r5;
        r5 = (float) r5;
        r9 = r0.photoImage;
        r9 = r9.getImageY();
        r10 = NUM; // 0x40b00000 float:5.5 double:5.36197667E-315;
        r12 = NUM; // 0x3e4ccccd float:0.2 double:5.164075695E-315;
        r12 = r12 * r6;
        r12 = r12 + r10;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r12);
        r9 = r9 + r10;
        r9 = (float) r9;
        r7.translate(r5, r9);
        r5 = r0.infoLayout;
        if (r5 == 0) goto L_0x0b08;
    L_0x0b05:
        r5.draw(r7);
    L_0x0b08:
        r5 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1));
        if (r5 <= 0) goto L_0x0b34;
    L_0x0b0c:
        r5 = r0.docTitleLayout;
        if (r5 == 0) goto L_0x0b34;
    L_0x0b10:
        r24.save();
        r5 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r9 = r0.controlsAlpha;
        r9 = r9 * r14;
        r9 = r9 * r6;
        r9 = (int) r9;
        r5.setAlpha(r9);
        r5 = NUM; // 0x4164cccd float:14.3 double:5.42051806E-315;
        r6 = r6 * r5;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = (float) r5;
        r7.translate(r1, r5);
        r5 = r0.docTitleLayout;
        r5.draw(r7);
        r24.restore();
    L_0x0b34:
        if (r2 == 0) goto L_0x0b7e;
    L_0x0b36:
        r2 = org.telegram.ui.ActionBar.Theme.chat_msgNoSoundDrawable;
        r5 = r0.animatingNoSoundProgress;
        r14 = r14 * r5;
        r14 = r14 * r5;
        r5 = r0.controlsAlpha;
        r14 = r14 * r5;
        r5 = (int) r14;
        r2.setAlpha(r5);
        r2 = r0.infoWidth;
        r5 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r2 = r2 + r6;
        r2 = (float) r2;
        r7.translate(r2, r1);
        r2 = r0.animatingNoSoundProgress;
        r2 = r2 * r11;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r5 = org.telegram.messenger.AndroidUtilities.dp(r11);
        r5 = r5 - r2;
        r5 = r5 / r8;
        r6 = org.telegram.ui.ActionBar.Theme.chat_msgNoSoundDrawable;
        r9 = r5 + r2;
        r10 = 0;
        r6.setBounds(r10, r5, r2, r9);
        r5 = org.telegram.ui.ActionBar.Theme.chat_msgNoSoundDrawable;
        r5.draw(r7);
        r5 = r0.noSoundCenterX;
        r6 = r0.infoWidth;
        r9 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r6 = r6 + r10;
        r2 = r2 / r8;
        r6 = r6 + r2;
        r5 = r5 + r6;
        r0.noSoundCenterX = r5;
    L_0x0b7e:
        r24.restore();
        r2 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r5 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r2.setAlpha(r5);
    L_0x0b88:
        r2 = r0.animatingDrawVideoImageButton;
        r5 = 1;
        if (r2 != r5) goto L_0x0ba5;
    L_0x0b8d:
        r2 = r0.animatingDrawVideoImageButtonProgress;
        r5 = (float) r3;
        r6 = NUM; // 0x43200000 float:160.0 double:5.564022167E-315;
        r5 = r5 / r6;
        r2 = r2 - r5;
        r0.animatingDrawVideoImageButtonProgress = r2;
        r2 = r0.animatingDrawVideoImageButtonProgress;
        r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1));
        if (r2 > 0) goto L_0x0ba1;
    L_0x0b9c:
        r0.animatingDrawVideoImageButtonProgress = r1;
        r2 = 0;
        r0.animatingDrawVideoImageButton = r2;
    L_0x0ba1:
        r23.invalidate();
        goto L_0x0bc0;
    L_0x0ba5:
        if (r2 != r8) goto L_0x0bc0;
    L_0x0ba7:
        r2 = r0.animatingDrawVideoImageButtonProgress;
        r5 = (float) r3;
        r6 = NUM; // 0x43200000 float:160.0 double:5.564022167E-315;
        r5 = r5 / r6;
        r2 = r2 + r5;
        r0.animatingDrawVideoImageButtonProgress = r2;
        r2 = r0.animatingDrawVideoImageButtonProgress;
        r5 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1));
        if (r2 < 0) goto L_0x0bbd;
    L_0x0bb8:
        r0.animatingDrawVideoImageButtonProgress = r5;
        r2 = 0;
        r0.animatingDrawVideoImageButton = r2;
    L_0x0bbd:
        r23.invalidate();
    L_0x0bc0:
        r2 = r0.animatingNoSound;
        r5 = 1;
        if (r2 != r5) goto L_0x0bdd;
    L_0x0bc5:
        r2 = r0.animatingNoSoundProgress;
        r3 = (float) r3;
        r4 = NUM; // 0x43340000 float:180.0 double:5.570497984E-315;
        r3 = r3 / r4;
        r2 = r2 - r3;
        r0.animatingNoSoundProgress = r2;
        r2 = r0.animatingNoSoundProgress;
        r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1));
        if (r2 > 0) goto L_0x0bd9;
    L_0x0bd4:
        r0.animatingNoSoundProgress = r1;
        r1 = 0;
        r0.animatingNoSound = r1;
    L_0x0bd9:
        r23.invalidate();
        goto L_0x0bf8;
    L_0x0bdd:
        if (r2 != r8) goto L_0x0bf8;
    L_0x0bdf:
        r1 = r0.animatingNoSoundProgress;
        r2 = (float) r3;
        r3 = NUM; // 0x43340000 float:180.0 double:5.570497984E-315;
        r2 = r2 / r3;
        r1 = r1 + r2;
        r0.animatingNoSoundProgress = r1;
        r1 = r0.animatingNoSoundProgress;
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 < 0) goto L_0x0bf5;
    L_0x0bf0:
        r0.animatingNoSoundProgress = r2;
        r1 = 0;
        r0.animatingNoSound = r1;
    L_0x0bf5:
        r23.invalidate();
    L_0x0bf8:
        r1 = r0.drawImageButton;
        if (r1 == 0) goto L_0x0CLASSNAME;
    L_0x0bfc:
        r1 = r0.photoImage;
        r1 = r1.getVisible();
        if (r1 == 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r1 = r0.controlsAlpha;
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r3 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r3 == 0) goto L_0x0CLASSNAME;
    L_0x0c0c:
        r2 = r0.radialProgress;
        r2.setOverrideAlpha(r1);
    L_0x0CLASSNAME:
        r1 = r0.radialProgress;
        r1.draw(r7);
    L_0x0CLASSNAME:
        r1 = r0.drawVideoImageButton;
        if (r1 != 0) goto L_0x0c1e;
    L_0x0c1a:
        r1 = r0.animatingDrawVideoImageButton;
        if (r1 == 0) goto L_0x0CLASSNAME;
    L_0x0c1e:
        r1 = r0.photoImage;
        r1 = r1.getVisible();
        if (r1 == 0) goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r1 = r0.controlsAlpha;
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r2 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r2 == 0) goto L_0x0CLASSNAME;
    L_0x0c2e:
        r2 = r0.videoRadialProgress;
        r2.setOverrideAlpha(r1);
    L_0x0CLASSNAME:
        r1 = r0.videoRadialProgress;
        r1.draw(r7);
    L_0x0CLASSNAME:
        r1 = r0.drawPhotoCheckBox;
        if (r1 == 0) goto L_0x0c7a;
    L_0x0c3c:
        r1 = NUM; // 0x41a80000 float:21.0 double:5.442276803E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r2 = r0.photoCheckBox;
        r3 = 0;
        r4 = 0;
        r5 = r0.currentMessageObject;
        r5 = r5.isOutOwner();
        if (r5 == 0) goto L_0x0CLASSNAME;
    L_0x0c4e:
        r5 = "chat_outBubbleSelected";
        goto L_0x0CLASSNAME;
    L_0x0CLASSNAME:
        r5 = "chat_inBubbleSelected";
    L_0x0CLASSNAME:
        r2.setColor(r3, r4, r5);
        r2 = r0.photoCheckBox;
        r3 = r0.photoImage;
        r3 = r3.getImageX2();
        r4 = NUM; // 0x41CLASSNAME float:25.0 double:5.45263811E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r3 = r3 - r4;
        r4 = r0.photoImage;
        r4 = r4.getImageY();
        r5 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r4 = r4 + r5;
        r2.setBounds(r3, r4, r1, r1);
        r1 = r0.photoCheckBox;
        r1.draw(r7);
    L_0x0c7a:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.drawOverlays(android.graphics.Canvas):void");
    }

    public int getObserverTag() {
        return this.TAG;
    }

    public MessageObject getMessageObject() {
        MessageObject messageObject = this.messageObjectToSet;
        return messageObject != null ? messageObject : this.currentMessageObject;
    }

    public Document getStreamingMedia() {
        int i = this.documentAttachType;
        return (i == 4 || i == 7 || i == 2) ? this.documentAttach : null;
    }

    public boolean isPinnedBottom() {
        return this.pinnedBottom;
    }

    public boolean isPinnedTop() {
        return this.pinnedTop;
    }

    public GroupedMessages getCurrentMessagesGroup() {
        return this.currentMessagesGroup;
    }

    public GroupedMessagePosition getCurrentPosition() {
        return this.currentPosition;
    }

    public int getLayoutHeight() {
        return this.layoutHeight;
    }

    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (i == 16) {
            if (getIconForCurrentState() != 4) {
                didPressButton(true, false);
            } else if (this.currentMessageObject.type == 16) {
                this.delegate.didPressOther(this, (float) this.otherX, (float) this.otherY);
            } else {
                didClickedImage();
            }
            return true;
        }
        if (i == NUM) {
            didPressMiniButton(true);
        } else if (i == NUM) {
            ChatMessageCellDelegate chatMessageCellDelegate = this.delegate;
            if (chatMessageCellDelegate != null) {
                if (this.currentMessageObject.type == 16) {
                    chatMessageCellDelegate.didLongPress(this, 0.0f, 0.0f);
                } else {
                    chatMessageCellDelegate.didPressOther(this, (float) this.otherX, (float) this.otherY);
                }
            }
        }
        return super.performAccessibilityAction(i, bundle);
    }

    public void setAnimationRunning(boolean z) {
        this.animationRunning = z;
    }

    public boolean onHoverEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int i = 0;
        if (motionEvent.getAction() == 9 || motionEvent.getAction() == 7) {
            while (i < this.accessibilityVirtualViewBounds.size()) {
                if (((Rect) this.accessibilityVirtualViewBounds.valueAt(i)).contains(x, y)) {
                    int keyAt = this.accessibilityVirtualViewBounds.keyAt(i);
                    if (keyAt != this.currentFocusedVirtualView) {
                        this.currentFocusedVirtualView = keyAt;
                        sendAccessibilityEventForVirtualView(keyAt, 32768);
                    }
                    return true;
                }
                i++;
            }
        } else if (motionEvent.getAction() == 10) {
            this.currentFocusedVirtualView = 0;
        }
        return super.onHoverEvent(motionEvent);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return new MessageAccessibilityNodeProvider(this, null);
    }

    private void sendAccessibilityEventForVirtualView(int i, int i2) {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(i2);
            obtain.setPackageName(getContext().getPackageName());
            obtain.setSource(this, i);
            getParent().requestSendAccessibilityEvent(this, obtain);
        }
    }

    public static org.telegram.ui.Components.Point getMessageSize(int i, int i2) {
        return getMessageSize(i, i2, 0, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004c  */
    private static org.telegram.ui.Components.Point getMessageSize(int r3, int r4, int r5, int r6) {
        /*
        if (r6 == 0) goto L_0x0004;
    L_0x0002:
        if (r5 != 0) goto L_0x0050;
    L_0x0004:
        r5 = org.telegram.messenger.AndroidUtilities.isTablet();
        r6 = NUM; // 0x3var_ float:0.7 double:5.23867711E-315;
        if (r5 == 0) goto L_0x0016;
    L_0x000d:
        r5 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
    L_0x0011:
        r5 = (float) r5;
        r5 = r5 * r6;
        r5 = (int) r5;
        goto L_0x0035;
    L_0x0016:
        if (r3 < r4) goto L_0x002a;
    L_0x0018:
        r5 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r5.x;
        r5 = r5.y;
        r5 = java.lang.Math.min(r6, r5);
        r6 = NUM; // 0x42800000 float:64.0 double:5.51221563E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5 = r5 - r6;
        goto L_0x0035;
    L_0x002a:
        r5 = org.telegram.messenger.AndroidUtilities.displaySize;
        r0 = r5.x;
        r5 = r5.y;
        r5 = java.lang.Math.min(r0, r5);
        goto L_0x0011;
    L_0x0035:
        r6 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r5;
        r0 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        if (r5 <= r0) goto L_0x0046;
    L_0x0042:
        r5 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x0046:
        r0 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        if (r6 <= r0) goto L_0x0050;
    L_0x004c:
        r6 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x0050:
        r3 = (float) r3;
        r5 = (float) r5;
        r0 = r3 / r5;
        r1 = r3 / r0;
        r1 = (int) r1;
        r4 = (float) r4;
        r0 = r4 / r0;
        r0 = (int) r0;
        r2 = NUM; // 0x43160000 float:150.0 double:5.56078426E-315;
        if (r1 != 0) goto L_0x0063;
    L_0x005f:
        r1 = org.telegram.messenger.AndroidUtilities.dp(r2);
    L_0x0063:
        if (r0 != 0) goto L_0x0069;
    L_0x0065:
        r0 = org.telegram.messenger.AndroidUtilities.dp(r2);
    L_0x0069:
        if (r0 <= r6) goto L_0x0072;
    L_0x006b:
        r3 = (float) r0;
        r4 = (float) r6;
        r3 = r3 / r4;
        r4 = (float) r1;
        r4 = r4 / r3;
        r1 = (int) r4;
        goto L_0x0088;
    L_0x0072:
        r6 = NUM; // 0x42var_ float:120.0 double:5.548480205E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r6);
        if (r0 >= r2) goto L_0x0087;
    L_0x007a:
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r0 = (float) r6;
        r4 = r4 / r0;
        r3 = r3 / r4;
        r4 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r4 >= 0) goto L_0x0088;
    L_0x0085:
        r1 = (int) r3;
        goto L_0x0088;
    L_0x0087:
        r6 = r0;
    L_0x0088:
        r3 = new org.telegram.ui.Components.Point;
        r4 = (float) r1;
        r5 = (float) r6;
        r3.<init>(r4, r5);
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.getMessageSize(int, int, int, int):org.telegram.ui.Components.Point");
    }
}
