package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$Dialog;
import org.telegram.tgnet.TLRPC$DraftMessage;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$TL_dialog;
import org.telegram.tgnet.TLRPC$TL_dialogFolder;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.PullForegroundDrawable;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.DialogsActivity;

public class DialogCell extends BaseCell {
    private boolean animatingArchiveAvatar;
    private float animatingArchiveAvatarProgress;
    private float archiveBackgroundProgress;
    private boolean archiveHidden;
    private PullForegroundDrawable archivedChatsDrawable;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private ImageReceiver avatarImage = new ImageReceiver(this);
    private int bottomClip;
    private TLRPC$Chat chat;
    private CheckBox2 checkBox;
    private int checkDrawLeft;
    private int checkDrawTop;
    private boolean clearingDialog;
    private float clipProgress;
    private float cornerProgress;
    private StaticLayout countLayout;
    private int countLeft;
    private int countTop;
    private int countWidth;
    private int currentAccount = UserConfig.selectedAccount;
    private int currentDialogFolderDialogsCount;
    private int currentDialogFolderId;
    private long currentDialogId;
    private int currentEditDate;
    private float currentRevealBounceProgress;
    private float currentRevealProgress;
    private CustomDialog customDialog;
    private boolean dialogMuted;
    private int dialogsType;
    private TLRPC$DraftMessage draftMessage;
    private boolean drawCheck1;
    private boolean drawCheck2;
    private boolean drawClock;
    private boolean drawCount;
    private boolean drawError;
    private boolean drawMention;
    private boolean drawNameBot;
    private boolean drawNameBroadcast;
    private boolean drawNameGroup;
    private boolean drawNameLock;
    private boolean drawPin;
    private boolean drawPinBackground;
    private boolean drawPlay;
    private boolean drawReorder;
    private boolean drawRevealBackground;
    private boolean drawScam;
    private boolean drawVerified;
    private TLRPC$EncryptedChat encryptedChat;
    private int errorLeft;
    private int errorTop;
    private int folderId;
    public boolean fullSeparator;
    public boolean fullSeparator2;
    private int halfCheckDrawLeft;
    private boolean hasMessageThumb;
    private int index;
    private BounceInterpolator interpolator = new BounceInterpolator();
    private boolean isDialogCell;
    private boolean isSelected;
    private boolean isSliding;
    private int lastMessageDate;
    private CharSequence lastMessageString;
    private CharSequence lastPrintString;
    private int lastSendState;
    private boolean lastUnreadState;
    private long lastUpdateTime;
    private boolean markUnread;
    private int mentionCount;
    private StaticLayout mentionLayout;
    private int mentionLeft;
    private int mentionWidth;
    private MessageObject message;
    private int messageId;
    private StaticLayout messageLayout;
    private int messageLeft;
    private StaticLayout messageNameLayout;
    private int messageNameLeft;
    private int messageNameTop;
    private int messageTop;
    private StaticLayout nameLayout;
    private int nameLeft;
    private int nameLockLeft;
    private int nameLockTop;
    private int nameMuteLeft;
    private float onlineProgress;
    private int paintIndex;
    private int pinLeft;
    private int pinTop;
    private boolean promoDialog;
    private RectF rect = new RectF();
    private float reorderIconProgress;
    private ImageReceiver thumbImage = new ImageReceiver(this);
    private StaticLayout timeLayout;
    private int timeLeft;
    private int timeTop;
    private int topClip;
    private boolean translationAnimationStarted;
    private RLottieDrawable translationDrawable;
    private float translationX;
    private int unreadCount;
    public boolean useForceThreeLines;
    private boolean useMeForMyMessages;
    public boolean useSeparator;
    private TLRPC$User user;

    public static class BounceInterpolator implements Interpolator {
        public float getInterpolation(float f) {
            if (f < 0.33f) {
                return (f / 0.33f) * 0.1f;
            }
            float f2 = f - 0.33f;
            return f2 < 0.33f ? 0.1f - ((f2 / 0.34f) * 0.15f) : (((f2 - 0.34f) / 0.33f) * 0.05f) - 89.6f;
        }
    }

    public static class CustomDialog {
        public int date;
        public int id;
        public boolean isMedia;
        public String message;
        public boolean muted;
        public String name;
        public boolean pinned;
        public boolean sent;
        public int type;
        public int unread_count;
        public boolean verified;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public static class FixedWidthSpan extends ReplacementSpan {
        private int width;

        public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
        }

        public FixedWidthSpan(int i) {
            this.width = i;
        }

        public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fontMetricsInt) {
            if (fontMetricsInt == null) {
                fontMetricsInt = paint.getFontMetricsInt();
            }
            if (fontMetricsInt != null) {
                int i3 = 1 - (fontMetricsInt.descent - fontMetricsInt.ascent);
                fontMetricsInt.descent = i3;
                fontMetricsInt.bottom = i3;
                fontMetricsInt.ascent = -1;
                fontMetricsInt.top = -1;
            }
            return this.width;
        }
    }

    public DialogCell(Context context, boolean z, boolean z2) {
        super(context);
        Theme.createDialogsResources(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(28.0f));
        this.thumbImage.setRoundRadius(AndroidUtilities.dp(2.0f));
        this.useForceThreeLines = z2;
        if (z) {
            CheckBox2 checkBox2 = new CheckBox2(context, 21);
            this.checkBox = checkBox2;
            checkBox2.setColor((String) null, "windowBackgroundWhite", "checkboxCheck");
            this.checkBox.setDrawUnchecked(false);
            this.checkBox.setDrawBackgroundAsArc(3);
            addView(this.checkBox);
        }
    }

    public void setDialog(TLRPC$Dialog tLRPC$Dialog, int i, int i2) {
        this.currentDialogId = tLRPC$Dialog.id;
        this.isDialogCell = true;
        if (tLRPC$Dialog instanceof TLRPC$TL_dialogFolder) {
            this.currentDialogFolderId = ((TLRPC$TL_dialogFolder) tLRPC$Dialog).folder.id;
            PullForegroundDrawable pullForegroundDrawable = this.archivedChatsDrawable;
            if (pullForegroundDrawable != null) {
                pullForegroundDrawable.setCell(this);
            }
        } else {
            this.currentDialogFolderId = 0;
        }
        this.dialogsType = i;
        this.folderId = i2;
        this.messageId = 0;
        update(0);
        checkOnline();
    }

    public void setDialogIndex(int i) {
        this.index = i;
    }

    public void setDialog(CustomDialog customDialog2) {
        this.customDialog = customDialog2;
        this.messageId = 0;
        update(0);
        checkOnline();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0008, code lost:
        r0 = r0.status;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkOnline() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$User r0 = r2.user
            if (r0 == 0) goto L_0x0032
            boolean r1 = r0.self
            if (r1 != 0) goto L_0x0032
            org.telegram.tgnet.TLRPC$UserStatus r0 = r0.status
            if (r0 == 0) goto L_0x001a
            int r0 = r0.expires
            int r1 = r2.currentAccount
            org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
            int r1 = r1.getCurrentTime()
            if (r0 > r1) goto L_0x0030
        L_0x001a:
            int r0 = r2.currentAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, java.lang.Integer> r0 = r0.onlinePrivacy
            org.telegram.tgnet.TLRPC$User r1 = r2.user
            int r1 = r1.id
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            boolean r0 = r0.containsKey(r1)
            if (r0 == 0) goto L_0x0032
        L_0x0030:
            r0 = 1
            goto L_0x0033
        L_0x0032:
            r0 = 0
        L_0x0033:
            if (r0 == 0) goto L_0x0038
            r0 = 1065353216(0x3var_, float:1.0)
            goto L_0x0039
        L_0x0038:
            r0 = 0
        L_0x0039:
            r2.onlineProgress = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.DialogCell.checkOnline():void");
    }

    public void setDialog(long j, MessageObject messageObject, int i, boolean z) {
        this.currentDialogId = j;
        this.message = messageObject;
        this.useMeForMyMessages = z;
        this.isDialogCell = false;
        this.lastMessageDate = i;
        this.currentEditDate = messageObject != null ? messageObject.messageOwner.edit_date : 0;
        this.unreadCount = 0;
        this.markUnread = false;
        this.messageId = messageObject != null ? messageObject.getId() : 0;
        this.mentionCount = 0;
        this.lastUnreadState = messageObject != null && messageObject.isUnread();
        MessageObject messageObject2 = this.message;
        if (messageObject2 != null) {
            this.lastSendState = messageObject2.messageOwner.send_state;
        }
        update(0);
    }

    public long getDialogId() {
        return this.currentDialogId;
    }

    public int getDialogIndex() {
        return this.index;
    }

    public int getMessageId() {
        return this.messageId;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isSliding = false;
        this.drawRevealBackground = false;
        this.currentRevealProgress = 0.0f;
        this.reorderIconProgress = (!this.drawPin || !this.drawReorder) ? 0.0f : 1.0f;
        this.avatarImage.onDetachedFromWindow();
        this.thumbImage.onDetachedFromWindow();
        RLottieDrawable rLottieDrawable = this.translationDrawable;
        if (rLottieDrawable != null) {
            rLottieDrawable.stop();
            this.translationDrawable.setProgress(0.0f);
            this.translationDrawable.setCallback((Drawable.Callback) null);
            this.translationDrawable = null;
            this.translationAnimationStarted = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.avatarImage.onAttachedToWindow();
        this.thumbImage.onAttachedToWindow();
        resetPinnedArchiveState();
    }

    public void resetPinnedArchiveState() {
        boolean z = SharedConfig.archiveHidden;
        this.archiveHidden = z;
        float f = 1.0f;
        float f2 = z ? 0.0f : 1.0f;
        this.archiveBackgroundProgress = f2;
        this.avatarDrawable.setArchivedAvatarHiddenProgress(f2);
        this.clipProgress = 0.0f;
        this.isSliding = false;
        if (!this.drawPin || !this.drawReorder) {
            f = 0.0f;
        }
        this.reorderIconProgress = f;
        this.cornerProgress = 0.0f;
        setTranslationX(0.0f);
        setTranslationY(0.0f);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        CheckBox2 checkBox2 = this.checkBox;
        if (checkBox2 != null) {
            checkBox2.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0f), NUM), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0f), NUM));
        }
        setMeasuredDimension(View.MeasureSpec.getSize(i), AndroidUtilities.dp((this.useForceThreeLines || SharedConfig.useThreeLinesLayout) ? 78.0f : 72.0f) + (this.useSeparator ? 1 : 0));
        this.topClip = 0;
        this.bottomClip = getMeasuredHeight();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.currentDialogId != 0 || this.customDialog != null) {
            if (this.checkBox != null) {
                int dp = LocaleController.isRTL ? (i3 - i) - AndroidUtilities.dp(45.0f) : AndroidUtilities.dp(45.0f);
                int dp2 = AndroidUtilities.dp(46.0f);
                CheckBox2 checkBox2 = this.checkBox;
                checkBox2.layout(dp, dp2, checkBox2.getMeasuredWidth() + dp, this.checkBox.getMeasuredHeight() + dp2);
            }
            if (z) {
                try {
                    buildLayout();
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            }
        }
    }

    private CharSequence formatArchivedDialogNames() {
        TLRPC$User tLRPC$User;
        String str;
        ArrayList<TLRPC$Dialog> dialogs = MessagesController.getInstance(this.currentAccount).getDialogs(this.currentDialogFolderId);
        this.currentDialogFolderDialogsCount = dialogs.size();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        int size = dialogs.size();
        for (int i = 0; i < size; i++) {
            TLRPC$Dialog tLRPC$Dialog = dialogs.get(i);
            TLRPC$Chat tLRPC$Chat = null;
            if (DialogObject.isSecretDialogId(tLRPC$Dialog.id)) {
                TLRPC$EncryptedChat encryptedChat2 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int) (tLRPC$Dialog.id >> 32)));
                tLRPC$User = encryptedChat2 != null ? MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(encryptedChat2.user_id)) : null;
            } else {
                int i2 = (int) tLRPC$Dialog.id;
                if (i2 > 0) {
                    tLRPC$User = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i2));
                } else {
                    tLRPC$Chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i2));
                    tLRPC$User = null;
                }
            }
            if (tLRPC$Chat != null) {
                str = tLRPC$Chat.title.replace(10, ' ');
            } else if (tLRPC$User == null) {
                continue;
            } else if (UserObject.isDeleted(tLRPC$User)) {
                str = LocaleController.getString("HiddenName", NUM);
            } else {
                str = ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name).replace(10, ' ');
            }
            if (spannableStringBuilder.length() > 0) {
                spannableStringBuilder.append(", ");
            }
            int length = spannableStringBuilder.length();
            int length2 = str.length() + length;
            spannableStringBuilder.append(str);
            if (tLRPC$Dialog.unread_count > 0) {
                spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor("chats_nameArchived")), length, length2, 33);
            }
            if (spannableStringBuilder.length() > 150) {
                break;
            }
        }
        return Emoji.replaceEmoji(spannableStringBuilder, Theme.dialogs_messagePaint[this.paintIndex].getFontMetricsInt(), AndroidUtilities.dp(17.0f), false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:258:0x05fc, code lost:
        if (r2.post_messages == false) goto L_0x05d8;
     */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0363  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x012d  */
    /* JADX WARNING: Removed duplicated region for block: B:458:0x0a4c A[SYNTHETIC, Splitter:B:458:0x0a4c] */
    /* JADX WARNING: Removed duplicated region for block: B:468:0x0a6c  */
    /* JADX WARNING: Removed duplicated region for block: B:471:0x0a84  */
    /* JADX WARNING: Removed duplicated region for block: B:482:0x0ac8  */
    /* JADX WARNING: Removed duplicated region for block: B:484:0x0ad4  */
    /* JADX WARNING: Removed duplicated region for block: B:527:0x0bb6  */
    /* JADX WARNING: Removed duplicated region for block: B:536:0x0CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x0CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:540:0x0CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:549:0x0c2d  */
    /* JADX WARNING: Removed duplicated region for block: B:550:0x0c3d  */
    /* JADX WARNING: Removed duplicated region for block: B:586:0x0cd9  */
    /* JADX WARNING: Removed duplicated region for block: B:610:0x0d4e  */
    /* JADX WARNING: Removed duplicated region for block: B:611:0x0d58  */
    /* JADX WARNING: Removed duplicated region for block: B:621:0x0d93  */
    /* JADX WARNING: Removed duplicated region for block: B:623:0x0da5  */
    /* JADX WARNING: Removed duplicated region for block: B:643:0x0def  */
    /* JADX WARNING: Removed duplicated region for block: B:647:0x0e2f  */
    /* JADX WARNING: Removed duplicated region for block: B:650:0x0e3b  */
    /* JADX WARNING: Removed duplicated region for block: B:651:0x0e4b  */
    /* JADX WARNING: Removed duplicated region for block: B:654:0x0e63  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0e72  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0eab  */
    /* JADX WARNING: Removed duplicated region for block: B:671:0x0ed4  */
    /* JADX WARNING: Removed duplicated region for block: B:689:0x0var_  */
    /* JADX WARNING: Removed duplicated region for block: B:692:0x0f6e  */
    /* JADX WARNING: Removed duplicated region for block: B:711:0x0fee  */
    /* JADX WARNING: Removed duplicated region for block: B:716:0x109c  */
    /* JADX WARNING: Removed duplicated region for block: B:723:0x114d  */
    /* JADX WARNING: Removed duplicated region for block: B:729:0x1172  */
    /* JADX WARNING: Removed duplicated region for block: B:734:0x11a2  */
    /* JADX WARNING: Removed duplicated region for block: B:768:0x12bf  */
    /* JADX WARNING: Removed duplicated region for block: B:799:0x1362  */
    /* JADX WARNING: Removed duplicated region for block: B:800:0x136b  */
    /* JADX WARNING: Removed duplicated region for block: B:810:0x1390 A[Catch:{ Exception -> 0x1425 }] */
    /* JADX WARNING: Removed duplicated region for block: B:811:0x1399 A[Catch:{ Exception -> 0x1425 }] */
    /* JADX WARNING: Removed duplicated region for block: B:821:0x13b9 A[Catch:{ Exception -> 0x1425 }] */
    /* JADX WARNING: Removed duplicated region for block: B:836:0x140f A[Catch:{ Exception -> 0x1425 }] */
    /* JADX WARNING: Removed duplicated region for block: B:837:0x1412 A[Catch:{ Exception -> 0x1425 }] */
    /* JADX WARNING: Removed duplicated region for block: B:843:0x142d  */
    /* JADX WARNING: Removed duplicated region for block: B:887:0x155d  */
    /* JADX WARNING: Removed duplicated region for block: B:920:0x15ef A[SYNTHETIC, Splitter:B:920:0x15ef] */
    /* JADX WARNING: Removed duplicated region for block: B:936:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void buildLayout() {
        /*
            r36 = this;
            r1 = r36
            boolean r0 = r1.useForceThreeLines
            r2 = 1099431936(0x41880000, float:17.0)
            r3 = 18
            r4 = 1098907648(0x41800000, float:16.0)
            r5 = 1
            r6 = 0
            if (r0 != 0) goto L_0x005b
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x0013
            goto L_0x005b
        L_0x0013:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint
            r0 = r0[r6]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_nameEncryptedPaint
            r0 = r0[r6]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            r0 = r0[r6]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            r0 = r0[r6]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            r7 = r0[r6]
            r0 = r0[r6]
            java.lang.String r8 = "chats_message"
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r0.linkColor = r8
            r7.setColor(r8)
            r1.paintIndex = r6
            r0 = 19
            r7 = 19
            goto L_0x00a4
        L_0x005b:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint
            r0 = r0[r5]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_nameEncryptedPaint
            r0 = r0[r5]
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            r0 = r0[r5]
            r7 = 1097859072(0x41700000, float:15.0)
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            r0 = r0[r5]
            r7 = 1097859072(0x41700000, float:15.0)
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
            float r7 = (float) r7
            r0.setTextSize(r7)
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            r7 = r0[r5]
            r0 = r0[r5]
            java.lang.String r8 = "chats_message_threeLines"
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r0.linkColor = r8
            r7.setColor(r8)
            r1.paintIndex = r5
            r7 = 18
        L_0x00a4:
            r1.currentDialogFolderDialogsCount = r6
            boolean r0 = r1.isDialogCell
            if (r0 == 0) goto L_0x00bb
            int r0 = r1.currentAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            android.util.LongSparseArray<java.lang.CharSequence> r0 = r0.printingStrings
            long r9 = r1.currentDialogId
            java.lang.Object r0 = r0.get(r9)
            java.lang.CharSequence r0 = (java.lang.CharSequence) r0
            goto L_0x00bc
        L_0x00bb:
            r0 = 0
        L_0x00bc:
            android.text.TextPaint[] r9 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r10 = r1.paintIndex
            r9 = r9[r10]
            r1.drawNameGroup = r6
            r1.drawNameBroadcast = r6
            r1.drawNameLock = r6
            r1.drawNameBot = r6
            r1.drawVerified = r6
            r1.drawScam = r6
            r1.drawPinBackground = r6
            r1.hasMessageThumb = r6
            org.telegram.tgnet.TLRPC$User r10 = r1.user
            boolean r10 = org.telegram.messenger.UserObject.isUserSelf(r10)
            if (r10 != 0) goto L_0x00e0
            boolean r10 = r1.useMeForMyMessages
            if (r10 != 0) goto L_0x00e0
            r10 = 1
            goto L_0x00e1
        L_0x00e0:
            r10 = 0
        L_0x00e1:
            int r11 = android.os.Build.VERSION.SDK_INT
            if (r11 < r3) goto L_0x00f7
            boolean r11 = r1.useForceThreeLines
            if (r11 != 0) goto L_0x00ed
            boolean r11 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r11 == 0) goto L_0x00f1
        L_0x00ed:
            int r11 = r1.currentDialogFolderId
            if (r11 == 0) goto L_0x00f4
        L_0x00f1:
            java.lang.String r11 = "%2$s: ⁨%1$s⁩"
            goto L_0x0105
        L_0x00f4:
            java.lang.String r11 = "⁨%s⁩"
            goto L_0x0109
        L_0x00f7:
            boolean r11 = r1.useForceThreeLines
            if (r11 != 0) goto L_0x00ff
            boolean r11 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r11 == 0) goto L_0x0103
        L_0x00ff:
            int r11 = r1.currentDialogFolderId
            if (r11 == 0) goto L_0x0107
        L_0x0103:
            java.lang.String r11 = "%2$s: %1$s"
        L_0x0105:
            r12 = 1
            goto L_0x010a
        L_0x0107:
            java.lang.String r11 = "%1$s"
        L_0x0109:
            r12 = 0
        L_0x010a:
            org.telegram.messenger.MessageObject r13 = r1.message
            if (r13 == 0) goto L_0x0111
            java.lang.CharSequence r13 = r13.messageText
            goto L_0x0112
        L_0x0111:
            r13 = 0
        L_0x0112:
            r1.lastMessageString = r13
            org.telegram.ui.Cells.DialogCell$CustomDialog r13 = r1.customDialog
            r14 = 1117782016(0x42a00000, float:80.0)
            r15 = 1118044160(0x42a40000, float:82.0)
            r17 = 1101004800(0x41a00000, float:20.0)
            r3 = 33
            r19 = 1102053376(0x41b00000, float:22.0)
            r8 = 150(0x96, float:2.1E-43)
            r2 = 2
            r20 = 1117257728(0x42980000, float:76.0)
            r21 = 1099956224(0x41900000, float:18.0)
            r22 = 1117519872(0x429CLASSNAME, float:78.0)
            java.lang.String r4 = ""
            if (r13 == 0) goto L_0x0363
            int r0 = r13.type
            if (r0 != r2) goto L_0x01b2
            r1.drawNameLock = r5
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x0177
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x013c
            goto L_0x0177
        L_0x013c:
            r0 = 1099169792(0x41840000, float:16.5)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.nameLockTop = r0
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x015d
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r14)
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 + r10
            r1.nameLeft = r0
            goto L_0x0283
        L_0x015d:
            int r0 = r36.getMeasuredWidth()
            int r10 = org.telegram.messenger.AndroidUtilities.dp(r20)
            int r0 = r0 - r10
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 - r10
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0177:
            r0 = 1095237632(0x41480000, float:12.5)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.nameLockTop = r0
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x0198
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r15)
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 + r10
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0198:
            int r0 = r36.getMeasuredWidth()
            int r10 = org.telegram.messenger.AndroidUtilities.dp(r22)
            int r0 = r0 - r10
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 - r10
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x01b2:
            boolean r10 = r13.verified
            r1.drawVerified = r10
            boolean r10 = org.telegram.messenger.SharedConfig.drawDialogIcons
            if (r10 == 0) goto L_0x0257
            if (r0 != r5) goto L_0x0257
            r1.drawNameGroup = r5
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x0210
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x01c7
            goto L_0x0210
        L_0x01c7:
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x01ef
            r0 = 1099694080(0x418CLASSNAME, float:17.5)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.nameLockTop = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r14)
            boolean r10 = r1.drawNameGroup
            if (r10 == 0) goto L_0x01e4
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x01e6
        L_0x01e4:
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x01e6:
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 + r10
            r1.nameLeft = r0
            goto L_0x0283
        L_0x01ef:
            int r0 = r36.getMeasuredWidth()
            int r10 = org.telegram.messenger.AndroidUtilities.dp(r20)
            int r0 = r0 - r10
            boolean r10 = r1.drawNameGroup
            if (r10 == 0) goto L_0x01ff
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x0201
        L_0x01ff:
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x0201:
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 - r10
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0210:
            r0 = 1096286208(0x41580000, float:13.5)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.nameLockTop = r0
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x0237
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r15)
            boolean r10 = r1.drawNameGroup
            if (r10 == 0) goto L_0x022d
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x022f
        L_0x022d:
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x022f:
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 + r10
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0237:
            int r0 = r36.getMeasuredWidth()
            int r10 = org.telegram.messenger.AndroidUtilities.dp(r22)
            int r0 = r0 - r10
            boolean r10 = r1.drawNameGroup
            if (r10 == 0) goto L_0x0247
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x0249
        L_0x0247:
            android.graphics.drawable.Drawable r10 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x0249:
            int r10 = r10.getIntrinsicWidth()
            int r0 = r0 - r10
            r1.nameLockLeft = r0
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0257:
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x0272
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x0260
            goto L_0x0272
        L_0x0260:
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x026b
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x026b:
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x0272:
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x027d
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLeft = r0
            goto L_0x0283
        L_0x027d:
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r0
        L_0x0283:
            org.telegram.ui.Cells.DialogCell$CustomDialog r0 = r1.customDialog
            int r10 = r0.type
            if (r10 != r5) goto L_0x030f
            r0 = 2131625420(0x7f0e05cc, float:1.8878047E38)
            java.lang.String r10 = "FromYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r10, r0)
            org.telegram.ui.Cells.DialogCell$CustomDialog r10 = r1.customDialog
            boolean r12 = r10.isMedia
            if (r12 == 0) goto L_0x02c1
            android.text.TextPaint[] r9 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r10 = r1.paintIndex
            r9 = r9[r10]
            java.lang.Object[] r10 = new java.lang.Object[r5]
            org.telegram.messenger.MessageObject r12 = r1.message
            java.lang.CharSequence r12 = r12.messageText
            r10[r6] = r12
            java.lang.String r10 = java.lang.String.format(r11, r10)
            android.text.SpannableStringBuilder r10 = android.text.SpannableStringBuilder.valueOf(r10)
            android.text.style.ForegroundColorSpan r11 = new android.text.style.ForegroundColorSpan
            java.lang.String r12 = "chats_attachMessage"
            int r12 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            r11.<init>(r12)
            int r12 = r10.length()
            r10.setSpan(r11, r6, r12, r3)
            goto L_0x02fb
        L_0x02c1:
            java.lang.String r3 = r10.message
            int r10 = r3.length()
            if (r10 <= r8) goto L_0x02cd
            java.lang.String r3 = r3.substring(r6, r8)
        L_0x02cd:
            boolean r10 = r1.useForceThreeLines
            if (r10 != 0) goto L_0x02ed
            boolean r10 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r10 == 0) goto L_0x02d6
            goto L_0x02ed
        L_0x02d6:
            java.lang.Object[] r10 = new java.lang.Object[r2]
            r12 = 32
            r13 = 10
            java.lang.String r3 = r3.replace(r13, r12)
            r10[r6] = r3
            r10[r5] = r0
            java.lang.String r3 = java.lang.String.format(r11, r10)
            android.text.SpannableStringBuilder r10 = android.text.SpannableStringBuilder.valueOf(r3)
            goto L_0x02fb
        L_0x02ed:
            java.lang.Object[] r10 = new java.lang.Object[r2]
            r10[r6] = r3
            r10[r5] = r0
            java.lang.String r3 = java.lang.String.format(r11, r10)
            android.text.SpannableStringBuilder r10 = android.text.SpannableStringBuilder.valueOf(r3)
        L_0x02fb:
            android.text.TextPaint[] r3 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r11 = r1.paintIndex
            r3 = r3[r11]
            android.graphics.Paint$FontMetricsInt r3 = r3.getFontMetricsInt()
            int r11 = org.telegram.messenger.AndroidUtilities.dp(r17)
            java.lang.CharSequence r3 = org.telegram.messenger.Emoji.replaceEmoji(r10, r3, r11, r6)
            r10 = 0
            goto L_0x031d
        L_0x030f:
            java.lang.String r3 = r0.message
            boolean r0 = r0.isMedia
            if (r0 == 0) goto L_0x031b
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r9 = r1.paintIndex
            r9 = r0[r9]
        L_0x031b:
            r0 = 0
            r10 = 1
        L_0x031d:
            org.telegram.ui.Cells.DialogCell$CustomDialog r11 = r1.customDialog
            int r11 = r11.date
            long r11 = (long) r11
            java.lang.String r11 = org.telegram.messenger.LocaleController.stringForMessageListDate(r11)
            org.telegram.ui.Cells.DialogCell$CustomDialog r12 = r1.customDialog
            int r12 = r12.unread_count
            if (r12 == 0) goto L_0x033d
            r1.drawCount = r5
            java.lang.Object[] r13 = new java.lang.Object[r5]
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)
            r13[r6] = r12
            java.lang.String r12 = "%d"
            java.lang.String r12 = java.lang.String.format(r12, r13)
            goto L_0x0340
        L_0x033d:
            r1.drawCount = r6
            r12 = 0
        L_0x0340:
            org.telegram.ui.Cells.DialogCell$CustomDialog r13 = r1.customDialog
            boolean r13 = r13.sent
            if (r13 == 0) goto L_0x034b
            r1.drawCheck1 = r5
            r1.drawCheck2 = r5
            goto L_0x034f
        L_0x034b:
            r1.drawCheck1 = r6
            r1.drawCheck2 = r6
        L_0x034f:
            r1.drawClock = r6
            r1.drawError = r6
            org.telegram.ui.Cells.DialogCell$CustomDialog r13 = r1.customDialog
            java.lang.String r13 = r13.name
            r23 = r7
            r2 = r12
            r8 = 0
            r12 = 1
            r35 = r11
            r11 = r0
            r0 = r35
            goto L_0x0ded
        L_0x0363:
            boolean r13 = r1.useForceThreeLines
            if (r13 != 0) goto L_0x037e
            boolean r13 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r13 == 0) goto L_0x036c
            goto L_0x037e
        L_0x036c:
            boolean r13 = org.telegram.messenger.LocaleController.isRTL
            if (r13 != 0) goto L_0x0377
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLeft = r13
            goto L_0x038f
        L_0x0377:
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r13
            goto L_0x038f
        L_0x037e:
            boolean r13 = org.telegram.messenger.LocaleController.isRTL
            if (r13 != 0) goto L_0x0389
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLeft = r13
            goto L_0x038f
        L_0x0389:
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r13
        L_0x038f:
            org.telegram.tgnet.TLRPC$EncryptedChat r13 = r1.encryptedChat
            if (r13 == 0) goto L_0x0418
            int r13 = r1.currentDialogFolderId
            if (r13 != 0) goto L_0x05a9
            r1.drawNameLock = r5
            boolean r13 = r1.useForceThreeLines
            if (r13 != 0) goto L_0x03dd
            boolean r13 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r13 == 0) goto L_0x03a2
            goto L_0x03dd
        L_0x03a2:
            r13 = 1099169792(0x41840000, float:16.5)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
            r1.nameLockTop = r13
            boolean r13 = org.telegram.messenger.LocaleController.isRTL
            if (r13 != 0) goto L_0x03c3
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLockLeft = r13
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r14)
            android.graphics.drawable.Drawable r14 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r14 = r14.getIntrinsicWidth()
            int r13 = r13 + r14
            r1.nameLeft = r13
            goto L_0x05a9
        L_0x03c3:
            int r13 = r36.getMeasuredWidth()
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r20)
            int r13 = r13 - r14
            android.graphics.drawable.Drawable r14 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r14 = r14.getIntrinsicWidth()
            int r13 = r13 - r14
            r1.nameLockLeft = r13
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r13
            goto L_0x05a9
        L_0x03dd:
            r13 = 1095237632(0x41480000, float:12.5)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
            r1.nameLockTop = r13
            boolean r13 = org.telegram.messenger.LocaleController.isRTL
            if (r13 != 0) goto L_0x03fe
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLockLeft = r13
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r15)
            android.graphics.drawable.Drawable r14 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r14 = r14.getIntrinsicWidth()
            int r13 = r13 + r14
            r1.nameLeft = r13
            goto L_0x05a9
        L_0x03fe:
            int r13 = r36.getMeasuredWidth()
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r22)
            int r13 = r13 - r14
            android.graphics.drawable.Drawable r14 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r14 = r14.getIntrinsicWidth()
            int r13 = r13 - r14
            r1.nameLockLeft = r13
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r13
            goto L_0x05a9
        L_0x0418:
            int r13 = r1.currentDialogFolderId
            if (r13 != 0) goto L_0x05a9
            org.telegram.tgnet.TLRPC$Chat r13 = r1.chat
            if (r13 == 0) goto L_0x050f
            boolean r2 = r13.scam
            if (r2 == 0) goto L_0x042c
            r1.drawScam = r5
            org.telegram.ui.Components.ScamDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            r2.checkText()
            goto L_0x0430
        L_0x042c:
            boolean r2 = r13.verified
            r1.drawVerified = r2
        L_0x0430:
            boolean r2 = org.telegram.messenger.SharedConfig.drawDialogIcons
            if (r2 == 0) goto L_0x05a9
            boolean r2 = r1.useForceThreeLines
            if (r2 != 0) goto L_0x04a6
            boolean r2 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r2 == 0) goto L_0x043d
            goto L_0x04a6
        L_0x043d:
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            int r13 = r2.id
            if (r13 < 0) goto L_0x045b
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
            if (r2 == 0) goto L_0x0450
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            boolean r2 = r2.megagroup
            if (r2 != 0) goto L_0x0450
            goto L_0x045b
        L_0x0450:
            r1.drawNameGroup = r5
            r2 = 1099694080(0x418CLASSNAME, float:17.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
            goto L_0x0465
        L_0x045b:
            r1.drawNameBroadcast = r5
            r2 = 1099169792(0x41840000, float:16.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
        L_0x0465:
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x0485
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r14)
            boolean r13 = r1.drawNameGroup
            if (r13 == 0) goto L_0x047a
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x047c
        L_0x047a:
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x047c:
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 + r13
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x0485:
            int r2 = r36.getMeasuredWidth()
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r20)
            int r2 = r2 - r13
            boolean r13 = r1.drawNameGroup
            if (r13 == 0) goto L_0x0495
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x0497
        L_0x0495:
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x0497:
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 - r13
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x04a6:
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            int r13 = r2.id
            if (r13 < 0) goto L_0x04c4
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
            if (r2 == 0) goto L_0x04b9
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            boolean r2 = r2.megagroup
            if (r2 != 0) goto L_0x04b9
            goto L_0x04c4
        L_0x04b9:
            r1.drawNameGroup = r5
            r2 = 1096286208(0x41580000, float:13.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
            goto L_0x04ce
        L_0x04c4:
            r1.drawNameBroadcast = r5
            r2 = 1095237632(0x41480000, float:12.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
        L_0x04ce:
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x04ee
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r15)
            boolean r13 = r1.drawNameGroup
            if (r13 == 0) goto L_0x04e3
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x04e5
        L_0x04e3:
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x04e5:
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 + r13
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x04ee:
            int r2 = r36.getMeasuredWidth()
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r22)
            int r2 = r2 - r13
            boolean r13 = r1.drawNameGroup
            if (r13 == 0) goto L_0x04fe
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            goto L_0x0500
        L_0x04fe:
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
        L_0x0500:
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 - r13
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x050f:
            org.telegram.tgnet.TLRPC$User r2 = r1.user
            if (r2 == 0) goto L_0x05a9
            boolean r13 = r2.scam
            if (r13 == 0) goto L_0x051f
            r1.drawScam = r5
            org.telegram.ui.Components.ScamDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            r2.checkText()
            goto L_0x0523
        L_0x051f:
            boolean r2 = r2.verified
            r1.drawVerified = r2
        L_0x0523:
            boolean r2 = org.telegram.messenger.SharedConfig.drawDialogIcons
            if (r2 == 0) goto L_0x05a9
            org.telegram.tgnet.TLRPC$User r2 = r1.user
            boolean r2 = r2.bot
            if (r2 == 0) goto L_0x05a9
            r1.drawNameBot = r5
            boolean r2 = r1.useForceThreeLines
            if (r2 != 0) goto L_0x0571
            boolean r2 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r2 == 0) goto L_0x0538
            goto L_0x0571
        L_0x0538:
            r2 = 1099169792(0x41840000, float:16.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x0558
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r14)
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 + r13
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x0558:
            int r2 = r36.getMeasuredWidth()
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r20)
            int r2 = r2 - r13
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 - r13
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r21)
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x0571:
            r2 = 1095237632(0x41480000, float:12.5)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.nameLockTop = r2
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x0591
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r15)
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 + r13
            r1.nameLeft = r2
            goto L_0x05a9
        L_0x0591:
            int r2 = r36.getMeasuredWidth()
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r22)
            int r2 = r2 - r13
            android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r13 = r13.getIntrinsicWidth()
            int r2 = r2 - r13
            r1.nameLockLeft = r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.nameLeft = r2
        L_0x05a9:
            int r2 = r1.lastMessageDate
            if (r2 != 0) goto L_0x05b5
            org.telegram.messenger.MessageObject r13 = r1.message
            if (r13 == 0) goto L_0x05b5
            org.telegram.tgnet.TLRPC$Message r2 = r13.messageOwner
            int r2 = r2.date
        L_0x05b5:
            boolean r13 = r1.isDialogCell
            if (r13 == 0) goto L_0x0610
            int r13 = r1.currentAccount
            org.telegram.messenger.MediaDataController r13 = org.telegram.messenger.MediaDataController.getInstance(r13)
            long r14 = r1.currentDialogId
            org.telegram.tgnet.TLRPC$DraftMessage r13 = r13.getDraft(r14)
            r1.draftMessage = r13
            if (r13 == 0) goto L_0x05e4
            java.lang.String r13 = r13.message
            boolean r13 = android.text.TextUtils.isEmpty(r13)
            if (r13 == 0) goto L_0x05da
            org.telegram.tgnet.TLRPC$DraftMessage r13 = r1.draftMessage
            int r13 = r13.reply_to_msg_id
            if (r13 == 0) goto L_0x05d8
            goto L_0x05da
        L_0x05d8:
            r2 = 0
            goto L_0x060b
        L_0x05da:
            org.telegram.tgnet.TLRPC$DraftMessage r13 = r1.draftMessage
            int r13 = r13.date
            if (r2 <= r13) goto L_0x05e4
            int r2 = r1.unreadCount
            if (r2 != 0) goto L_0x05d8
        L_0x05e4:
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
            if (r2 == 0) goto L_0x05fe
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            boolean r13 = r2.megagroup
            if (r13 != 0) goto L_0x05fe
            boolean r13 = r2.creator
            if (r13 != 0) goto L_0x05fe
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r2 = r2.admin_rights
            if (r2 == 0) goto L_0x05d8
            boolean r2 = r2.post_messages
            if (r2 == 0) goto L_0x05d8
        L_0x05fe:
            org.telegram.tgnet.TLRPC$Chat r2 = r1.chat
            if (r2 == 0) goto L_0x060e
            boolean r13 = r2.left
            if (r13 != 0) goto L_0x05d8
            boolean r2 = r2.kicked
            if (r2 == 0) goto L_0x060e
            goto L_0x05d8
        L_0x060b:
            r1.draftMessage = r2
            goto L_0x0613
        L_0x060e:
            r2 = 0
            goto L_0x0613
        L_0x0610:
            r2 = 0
            r1.draftMessage = r2
        L_0x0613:
            if (r0 == 0) goto L_0x0624
            r1.lastPrintString = r0
            android.text.TextPaint[] r3 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r9 = r1.paintIndex
            r9 = r3[r9]
            r11 = r2
            r2 = 2
            r3 = 0
        L_0x0620:
            r12 = 1
            r13 = 1
            goto L_0x0CLASSNAME
        L_0x0624:
            r1.lastPrintString = r2
            org.telegram.tgnet.TLRPC$DraftMessage r0 = r1.draftMessage
            if (r0 == 0) goto L_0x06ba
            r0 = 2131625019(0x7f0e043b, float:1.8877234E38)
            java.lang.String r2 = "Draft"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            org.telegram.tgnet.TLRPC$DraftMessage r2 = r1.draftMessage
            java.lang.String r2 = r2.message
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x0665
            boolean r2 = r1.useForceThreeLines
            if (r2 != 0) goto L_0x065d
            boolean r2 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r2 == 0) goto L_0x0646
            goto L_0x065d
        L_0x0646:
            android.text.SpannableStringBuilder r2 = android.text.SpannableStringBuilder.valueOf(r0)
            android.text.style.ForegroundColorSpan r11 = new android.text.style.ForegroundColorSpan
            java.lang.String r12 = "chats_draft"
            int r12 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            r11.<init>(r12)
            int r12 = r0.length()
            r2.setSpan(r11, r6, r12, r3)
            goto L_0x06b7
        L_0x065d:
            r11 = r0
            r0 = r4
        L_0x065f:
            r2 = 2
            r3 = 0
            r12 = 1
            r13 = 0
            goto L_0x0CLASSNAME
        L_0x0665:
            org.telegram.tgnet.TLRPC$DraftMessage r2 = r1.draftMessage
            java.lang.String r2 = r2.message
            int r12 = r2.length()
            if (r12 <= r8) goto L_0x0673
            java.lang.String r2 = r2.substring(r6, r8)
        L_0x0673:
            r12 = 2
            java.lang.Object[] r13 = new java.lang.Object[r12]
            r12 = 32
            r14 = 10
            java.lang.String r2 = r2.replace(r14, r12)
            r13[r6] = r2
            r13[r5] = r0
            java.lang.String r2 = java.lang.String.format(r11, r13)
            android.text.SpannableStringBuilder r2 = android.text.SpannableStringBuilder.valueOf(r2)
            boolean r11 = r1.useForceThreeLines
            if (r11 != 0) goto L_0x06a5
            boolean r11 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r11 != 0) goto L_0x06a5
            android.text.style.ForegroundColorSpan r11 = new android.text.style.ForegroundColorSpan
            java.lang.String r12 = "chats_draft"
            int r12 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            r11.<init>(r12)
            int r12 = r0.length()
            int r12 = r12 + r5
            r2.setSpan(r11, r6, r12, r3)
        L_0x06a5:
            android.text.TextPaint[] r3 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r11 = r1.paintIndex
            r3 = r3[r11]
            android.graphics.Paint$FontMetricsInt r3 = r3.getFontMetricsInt()
            int r11 = org.telegram.messenger.AndroidUtilities.dp(r17)
            java.lang.CharSequence r2 = org.telegram.messenger.Emoji.replaceEmoji(r2, r3, r11, r6)
        L_0x06b7:
            r11 = r0
            r0 = r2
            goto L_0x065f
        L_0x06ba:
            boolean r0 = r1.clearingDialog
            if (r0 == 0) goto L_0x06d2
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r2 = r1.paintIndex
            r9 = r0[r2]
            r0 = 2131625488(0x7f0e0610, float:1.8878185E38)
            java.lang.String r2 = "HistoryCleared"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
        L_0x06cd:
            r2 = 2
            r3 = 0
            r11 = 0
            goto L_0x0620
        L_0x06d2:
            org.telegram.messenger.MessageObject r0 = r1.message
            if (r0 != 0) goto L_0x0746
            org.telegram.tgnet.TLRPC$EncryptedChat r0 = r1.encryptedChat
            if (r0 == 0) goto L_0x0744
            android.text.TextPaint[] r2 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r3 = r1.paintIndex
            r9 = r2[r3]
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_encryptedChatRequested
            if (r2 == 0) goto L_0x06ee
            r0 = 2131625101(0x7f0e048d, float:1.88774E38)
            java.lang.String r2 = "EncryptionProcessing"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            goto L_0x06cd
        L_0x06ee:
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_encryptedChatWaiting
            if (r2 == 0) goto L_0x0706
            r0 = 2131624407(0x7f0e01d7, float:1.8875993E38)
            java.lang.Object[] r2 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$User r3 = r1.user
            java.lang.String r3 = org.telegram.messenger.UserObject.getFirstName(r3)
            r2[r6] = r3
            java.lang.String r3 = "AwaitingEncryption"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r3, r0, r2)
            goto L_0x06cd
        L_0x0706:
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_encryptedChatDiscarded
            if (r2 == 0) goto L_0x0714
            r0 = 2131625102(0x7f0e048e, float:1.8877402E38)
            java.lang.String r2 = "EncryptionRejected"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            goto L_0x06cd
        L_0x0714:
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_encryptedChat
            if (r2 == 0) goto L_0x0744
            int r0 = r0.admin_id
            int r2 = r1.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            int r2 = r2.getClientUserId()
            if (r0 != r2) goto L_0x073a
            r0 = 2131625090(0x7f0e0482, float:1.8877378E38)
            java.lang.Object[] r2 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$User r3 = r1.user
            java.lang.String r3 = org.telegram.messenger.UserObject.getFirstName(r3)
            r2[r6] = r3
            java.lang.String r3 = "EncryptedChatStartedOutgoing"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r3, r0, r2)
            goto L_0x06cd
        L_0x073a:
            r0 = 2131625089(0x7f0e0481, float:1.8877376E38)
            java.lang.String r2 = "EncryptedChatStartedIncoming"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            goto L_0x06cd
        L_0x0744:
            r0 = r4
            goto L_0x06cd
        L_0x0746:
            boolean r0 = r0.isFromUser()
            if (r0 == 0) goto L_0x0762
            int r0 = r1.currentAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            org.telegram.messenger.MessageObject r2 = r1.message
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            int r2 = r2.from_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r2)
            r2 = 0
            goto L_0x077a
        L_0x0762:
            int r0 = r1.currentAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            org.telegram.messenger.MessageObject r2 = r1.message
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.to_id
            int r2 = r2.channel_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r0 = r0.getChat(r2)
            r2 = r0
            r0 = 0
        L_0x077a:
            int r13 = r1.dialogsType
            r14 = 3
            if (r13 != r14) goto L_0x0798
            org.telegram.tgnet.TLRPC$User r13 = r1.user
            boolean r13 = org.telegram.messenger.UserObject.isUserSelf(r13)
            if (r13 == 0) goto L_0x0798
            r0 = 2131626716(0x7f0e0adc, float:1.8880676E38)
            java.lang.String r2 = "SavedMessagesInfo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r2 = 2
            r3 = 0
            r10 = 0
            r11 = 0
            r12 = 0
        L_0x0795:
            r13 = 1
            goto L_0x0bfc
        L_0x0798:
            boolean r13 = r1.useForceThreeLines
            if (r13 != 0) goto L_0x07af
            boolean r13 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r13 != 0) goto L_0x07af
            int r13 = r1.currentDialogFolderId
            if (r13 == 0) goto L_0x07af
            java.lang.CharSequence r0 = r36.formatArchivedDialogNames()
            r2 = 2
        L_0x07a9:
            r3 = 0
            r11 = 0
            r12 = 1
            r13 = 0
            goto L_0x0bfc
        L_0x07af:
            org.telegram.messenger.MessageObject r13 = r1.message
            org.telegram.tgnet.TLRPC$Message r14 = r13.messageOwner
            boolean r14 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageService
            if (r14 == 0) goto L_0x07df
            org.telegram.tgnet.TLRPC$Chat r0 = r1.chat
            boolean r0 = org.telegram.messenger.ChatObject.isChannel(r0)
            if (r0 == 0) goto L_0x07d0
            org.telegram.messenger.MessageObject r0 = r1.message
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear
            if (r2 != 0) goto L_0x07cd
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom
            if (r0 == 0) goto L_0x07d0
        L_0x07cd:
            r0 = r4
            r10 = 0
            goto L_0x07d4
        L_0x07d0:
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.CharSequence r0 = r0.messageText
        L_0x07d4:
            android.text.TextPaint[] r2 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r3 = r1.paintIndex
            r9 = r2[r3]
            r2 = 2
        L_0x07db:
            r3 = 0
            r11 = 0
            r12 = 1
            goto L_0x0795
        L_0x07df:
            boolean r14 = org.telegram.messenger.BuildVars.DEBUG_VERSION
            if (r14 == 0) goto L_0x0853
            int r14 = r1.currentDialogFolderId
            if (r14 != 0) goto L_0x0853
            org.telegram.tgnet.TLRPC$EncryptedChat r14 = r1.encryptedChat
            if (r14 != 0) goto L_0x0853
            boolean r13 = r13.needDrawBluredPreview()
            if (r13 != 0) goto L_0x0853
            org.telegram.messenger.MessageObject r13 = r1.message
            boolean r13 = r13.isPhoto()
            if (r13 != 0) goto L_0x0809
            org.telegram.messenger.MessageObject r13 = r1.message
            boolean r13 = r13.isNewGif()
            if (r13 != 0) goto L_0x0809
            org.telegram.messenger.MessageObject r13 = r1.message
            boolean r13 = r13.isVideo()
            if (r13 == 0) goto L_0x0853
        L_0x0809:
            org.telegram.messenger.MessageObject r13 = r1.message
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r13 = r13.photoThumbs
            r14 = 40
            org.telegram.tgnet.TLRPC$PhotoSize r13 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r13, r14)
            org.telegram.messenger.MessageObject r14 = r1.message
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r14 = r14.photoThumbs
            int r15 = org.telegram.messenger.AndroidUtilities.getPhotoSize()
            org.telegram.tgnet.TLRPC$PhotoSize r14 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r14, r15)
            if (r13 != r14) goto L_0x0822
            r14 = 0
        L_0x0822:
            if (r13 == 0) goto L_0x0853
            r1.hasMessageThumb = r5
            org.telegram.messenger.MessageObject r15 = r1.message
            boolean r15 = r15.isVideo()
            r1.drawPlay = r15
            org.telegram.messenger.ImageReceiver r15 = r1.thumbImage
            org.telegram.messenger.MessageObject r3 = r1.message
            org.telegram.tgnet.TLObject r3 = r3.photoThumbsObject
            org.telegram.messenger.ImageLocation r24 = org.telegram.messenger.ImageLocation.getForObject(r14, r3)
            org.telegram.messenger.MessageObject r3 = r1.message
            org.telegram.tgnet.TLObject r3 = r3.photoThumbsObject
            org.telegram.messenger.ImageLocation r26 = org.telegram.messenger.ImageLocation.getForObject(r13, r3)
            r28 = 0
            org.telegram.messenger.MessageObject r3 = r1.message
            r30 = 0
            java.lang.String r25 = "20_20"
            java.lang.String r27 = "20_20"
            r23 = r15
            r29 = r3
            r23.setImage((org.telegram.messenger.ImageLocation) r24, (java.lang.String) r25, (org.telegram.messenger.ImageLocation) r26, (java.lang.String) r27, (java.lang.String) r28, (java.lang.Object) r29, (int) r30)
            r3 = 0
            goto L_0x0854
        L_0x0853:
            r3 = 1
        L_0x0854:
            org.telegram.tgnet.TLRPC$Chat r13 = r1.chat
            if (r13 == 0) goto L_0x0ab4
            int r13 = r13.id
            if (r13 <= 0) goto L_0x0ab4
            if (r2 != 0) goto L_0x0ab4
            org.telegram.messenger.MessageObject r13 = r1.message
            boolean r13 = r13.isOutOwner()
            if (r13 == 0) goto L_0x0871
            r0 = 2131625420(0x7f0e05cc, float:1.8878047E38)
            java.lang.String r2 = "FromYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
        L_0x086f:
            r2 = r0
            goto L_0x08b4
        L_0x0871:
            if (r0 == 0) goto L_0x08a6
            boolean r2 = r1.useForceThreeLines
            if (r2 != 0) goto L_0x0887
            boolean r2 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r2 == 0) goto L_0x087c
            goto L_0x0887
        L_0x087c:
            java.lang.String r0 = org.telegram.messenger.UserObject.getFirstName(r0)
            java.lang.String r2 = "\n"
            java.lang.String r0 = r0.replace(r2, r4)
            goto L_0x086f
        L_0x0887:
            boolean r2 = org.telegram.messenger.UserObject.isDeleted(r0)
            if (r2 == 0) goto L_0x0897
            r0 = 2131625484(0x7f0e060c, float:1.8878177E38)
            java.lang.String r2 = "HiddenName"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            goto L_0x086f
        L_0x0897:
            java.lang.String r2 = r0.first_name
            java.lang.String r0 = r0.last_name
            java.lang.String r0 = org.telegram.messenger.ContactsController.formatName(r2, r0)
            java.lang.String r2 = "\n"
            java.lang.String r0 = r0.replace(r2, r4)
            goto L_0x086f
        L_0x08a6:
            if (r2 == 0) goto L_0x08b1
            java.lang.String r0 = r2.title
            java.lang.String r2 = "\n"
            java.lang.String r0 = r0.replace(r2, r4)
            goto L_0x086f
        L_0x08b1:
            java.lang.String r0 = "DELETED"
            goto L_0x086f
        L_0x08b4:
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.CharSequence r13 = r0.caption
            if (r13 == 0) goto L_0x0923
            java.lang.String r0 = r13.toString()
            int r12 = r0.length()
            if (r12 <= r8) goto L_0x08c8
            java.lang.String r0 = r0.substring(r6, r8)
        L_0x08c8:
            if (r3 != 0) goto L_0x08cd
            r3 = r4
        L_0x08cb:
            r12 = 2
            goto L_0x08fc
        L_0x08cd:
            org.telegram.messenger.MessageObject r3 = r1.message
            boolean r3 = r3.isVideo()
            if (r3 == 0) goto L_0x08d8
            java.lang.String r3 = "📹 "
            goto L_0x08cb
        L_0x08d8:
            org.telegram.messenger.MessageObject r3 = r1.message
            boolean r3 = r3.isVoice()
            if (r3 == 0) goto L_0x08e3
            java.lang.String r3 = "🎤 "
            goto L_0x08cb
        L_0x08e3:
            org.telegram.messenger.MessageObject r3 = r1.message
            boolean r3 = r3.isMusic()
            if (r3 == 0) goto L_0x08ee
            java.lang.String r3 = "🎧 "
            goto L_0x08cb
        L_0x08ee:
            org.telegram.messenger.MessageObject r3 = r1.message
            boolean r3 = r3.isPhoto()
            if (r3 == 0) goto L_0x08f9
            java.lang.String r3 = "🖼 "
            goto L_0x08cb
        L_0x08f9:
            java.lang.String r3 = "📎 "
            goto L_0x08cb
        L_0x08fc:
            java.lang.Object[] r13 = new java.lang.Object[r12]
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            r12.append(r3)
            r3 = 32
            r14 = 10
            java.lang.String r0 = r0.replace(r14, r3)
            r12.append(r0)
            java.lang.String r0 = r12.toString()
            r13[r6] = r0
            r13[r5] = r2
            java.lang.String r0 = java.lang.String.format(r11, r13)
            android.text.SpannableStringBuilder r0 = android.text.SpannableStringBuilder.valueOf(r0)
            goto L_0x0a39
        L_0x0923:
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r3.media
            if (r3 == 0) goto L_0x0a0b
            boolean r0 = r0.isMediaEmpty()
            if (r0 != 0) goto L_0x0a0b
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r3 = r1.paintIndex
            r9 = r0[r3]
            org.telegram.messenger.MessageObject r0 = r1.message
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r3.media
            boolean r13 = r3 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r13 == 0) goto L_0x0966
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r3 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r3
            int r0 = android.os.Build.VERSION.SDK_INT
            r13 = 18
            if (r0 < r13) goto L_0x0957
            java.lang.Object[] r0 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$Poll r3 = r3.poll
            java.lang.String r3 = r3.question
            r0[r6] = r3
            java.lang.String r3 = "📊 ⁨%s⁩"
            java.lang.String r0 = java.lang.String.format(r3, r0)
            goto L_0x09d0
        L_0x0957:
            java.lang.Object[] r0 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$Poll r3 = r3.poll
            java.lang.String r3 = r3.question
            r0[r6] = r3
            java.lang.String r3 = "📊 %s"
            java.lang.String r0 = java.lang.String.format(r3, r0)
            goto L_0x09d0
        L_0x0966:
            boolean r13 = r3 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r13 == 0) goto L_0x098e
            int r0 = android.os.Build.VERSION.SDK_INT
            r13 = 18
            if (r0 < r13) goto L_0x097f
            java.lang.Object[] r0 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$TL_game r3 = r3.game
            java.lang.String r3 = r3.title
            r0[r6] = r3
            java.lang.String r3 = "🎮 ⁨%s⁩"
            java.lang.String r0 = java.lang.String.format(r3, r0)
            goto L_0x09d0
        L_0x097f:
            java.lang.Object[] r0 = new java.lang.Object[r5]
            org.telegram.tgnet.TLRPC$TL_game r3 = r3.game
            java.lang.String r3 = r3.title
            r0[r6] = r3
            java.lang.String r3 = "🎮 %s"
            java.lang.String r0 = java.lang.String.format(r3, r0)
            goto L_0x09d0
        L_0x098e:
            int r3 = r0.type
            r13 = 14
            if (r3 != r13) goto L_0x09ca
            int r3 = android.os.Build.VERSION.SDK_INT
            r13 = 18
            if (r3 < r13) goto L_0x09b2
            r3 = 2
            java.lang.Object[] r13 = new java.lang.Object[r3]
            java.lang.String r0 = r0.getMusicAuthor()
            r13[r6] = r0
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.String r0 = r0.getMusicTitle()
            r13[r5] = r0
            java.lang.String r0 = "🎧 ⁨%s - %s⁩"
            java.lang.String r0 = java.lang.String.format(r0, r13)
            goto L_0x09d0
        L_0x09b2:
            r3 = 2
            java.lang.Object[] r13 = new java.lang.Object[r3]
            java.lang.String r0 = r0.getMusicAuthor()
            r13[r6] = r0
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.String r0 = r0.getMusicTitle()
            r13[r5] = r0
            java.lang.String r0 = "🎧 %s - %s"
            java.lang.String r0 = java.lang.String.format(r0, r13)
            goto L_0x09d0
        L_0x09ca:
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
        L_0x09d0:
            r3 = 32
            r13 = 10
            java.lang.String r0 = r0.replace(r13, r3)
            r3 = 2
            java.lang.Object[] r13 = new java.lang.Object[r3]
            r13[r6] = r0
            r13[r5] = r2
            java.lang.String r0 = java.lang.String.format(r11, r13)
            android.text.SpannableStringBuilder r3 = android.text.SpannableStringBuilder.valueOf(r0)
            android.text.style.ForegroundColorSpan r0 = new android.text.style.ForegroundColorSpan     // Catch:{ Exception -> 0x0a06 }
            java.lang.String r11 = "chats_attachMessage"
            int r11 = org.telegram.ui.ActionBar.Theme.getColor(r11)     // Catch:{ Exception -> 0x0a06 }
            r0.<init>(r11)     // Catch:{ Exception -> 0x0a06 }
            if (r12 == 0) goto L_0x09fb
            int r11 = r2.length()     // Catch:{ Exception -> 0x0a06 }
            r12 = 2
            int r11 = r11 + r12
            goto L_0x09fc
        L_0x09fb:
            r11 = 0
        L_0x09fc:
            int r12 = r3.length()     // Catch:{ Exception -> 0x0a06 }
            r13 = 33
            r3.setSpan(r0, r11, r12, r13)     // Catch:{ Exception -> 0x0a06 }
            goto L_0x0a3a
        L_0x0a06:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x0a3a
        L_0x0a0b:
            org.telegram.messenger.MessageObject r0 = r1.message
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            if (r0 == 0) goto L_0x0a35
            int r3 = r0.length()
            if (r3 <= r8) goto L_0x0a1d
            java.lang.String r0 = r0.substring(r6, r8)
        L_0x0a1d:
            r3 = 2
            java.lang.Object[] r12 = new java.lang.Object[r3]
            r3 = 32
            r13 = 10
            java.lang.String r0 = r0.replace(r13, r3)
            r12[r6] = r0
            r12[r5] = r2
            java.lang.String r0 = java.lang.String.format(r11, r12)
            android.text.SpannableStringBuilder r0 = android.text.SpannableStringBuilder.valueOf(r0)
            goto L_0x0a39
        L_0x0a35:
            android.text.SpannableStringBuilder r0 = android.text.SpannableStringBuilder.valueOf(r4)
        L_0x0a39:
            r3 = r0
        L_0x0a3a:
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x0a42
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x0a4c
        L_0x0a42:
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x0a6c
            int r0 = r3.length()
            if (r0 <= 0) goto L_0x0a6c
        L_0x0a4c:
            android.text.style.ForegroundColorSpan r0 = new android.text.style.ForegroundColorSpan     // Catch:{ Exception -> 0x0a65 }
            java.lang.String r11 = "chats_nameMessage"
            int r11 = org.telegram.ui.ActionBar.Theme.getColor(r11)     // Catch:{ Exception -> 0x0a65 }
            r0.<init>(r11)     // Catch:{ Exception -> 0x0a65 }
            int r11 = r2.length()     // Catch:{ Exception -> 0x0a65 }
            int r11 = r11 + r5
            r12 = 33
            r3.setSpan(r0, r6, r11, r12)     // Catch:{ Exception -> 0x0a63 }
            r0 = r11
            goto L_0x0a6e
        L_0x0a63:
            r0 = move-exception
            goto L_0x0a67
        L_0x0a65:
            r0 = move-exception
            r11 = 0
        L_0x0a67:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r0 = 0
            goto L_0x0a6e
        L_0x0a6c:
            r0 = 0
            r11 = 0
        L_0x0a6e:
            android.text.TextPaint[] r12 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r13 = r1.paintIndex
            r12 = r12[r13]
            android.graphics.Paint$FontMetricsInt r12 = r12.getFontMetricsInt()
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r17)
            java.lang.CharSequence r3 = org.telegram.messenger.Emoji.replaceEmoji(r3, r12, r13, r6)
            boolean r12 = r1.hasMessageThumb
            if (r12 == 0) goto L_0x0aa9
            boolean r12 = r3 instanceof android.text.SpannableStringBuilder
            if (r12 != 0) goto L_0x0a8e
            android.text.SpannableStringBuilder r12 = new android.text.SpannableStringBuilder
            r12.<init>(r3)
            r3 = r12
        L_0x0a8e:
            r12 = r3
            android.text.SpannableStringBuilder r12 = (android.text.SpannableStringBuilder) r12
            java.lang.String r13 = " "
            r12.insert(r11, r13)
            org.telegram.ui.Cells.DialogCell$FixedWidthSpan r13 = new org.telegram.ui.Cells.DialogCell$FixedWidthSpan
            int r14 = r7 + 6
            float r14 = (float) r14
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r14)
            r13.<init>(r14)
            int r14 = r11 + 1
            r15 = 33
            r12.setSpan(r13, r11, r14, r15)
        L_0x0aa9:
            r11 = r2
            r2 = 2
            r12 = 1
            r13 = 0
            r35 = r3
            r3 = r0
            r0 = r35
            goto L_0x0bfc
        L_0x0ab4:
            org.telegram.messenger.MessageObject r0 = r1.message
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r2 == 0) goto L_0x0ad4
            org.telegram.tgnet.TLRPC$Photo r2 = r0.photo
            boolean r2 = r2 instanceof org.telegram.tgnet.TLRPC$TL_photoEmpty
            if (r2 == 0) goto L_0x0ad4
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0ad4
            r0 = 2131624315(0x7f0e017b, float:1.8875806E38)
            java.lang.String r2 = "AttachPhotoExpired"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
        L_0x0ad1:
            r2 = 2
            goto L_0x0bb2
        L_0x0ad4:
            org.telegram.messenger.MessageObject r0 = r1.message
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r2 == 0) goto L_0x0af2
            org.telegram.tgnet.TLRPC$Document r2 = r0.document
            boolean r2 = r2 instanceof org.telegram.tgnet.TLRPC$TL_documentEmpty
            if (r2 == 0) goto L_0x0af2
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0af2
            r0 = 2131624321(0x7f0e0181, float:1.8875818E38)
            java.lang.String r2 = "AttachVideoExpired"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            goto L_0x0ad1
        L_0x0af2:
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.CharSequence r2 = r0.caption
            if (r2 == 0) goto L_0x0b3c
            if (r3 != 0) goto L_0x0afc
            r0 = r4
            goto L_0x0b28
        L_0x0afc:
            boolean r0 = r0.isVideo()
            if (r0 == 0) goto L_0x0b05
            java.lang.String r0 = "📹 "
            goto L_0x0b28
        L_0x0b05:
            org.telegram.messenger.MessageObject r0 = r1.message
            boolean r0 = r0.isVoice()
            if (r0 == 0) goto L_0x0b10
            java.lang.String r0 = "🎤 "
            goto L_0x0b28
        L_0x0b10:
            org.telegram.messenger.MessageObject r0 = r1.message
            boolean r0 = r0.isMusic()
            if (r0 == 0) goto L_0x0b1b
            java.lang.String r0 = "🎧 "
            goto L_0x0b28
        L_0x0b1b:
            org.telegram.messenger.MessageObject r0 = r1.message
            boolean r0 = r0.isPhoto()
            if (r0 == 0) goto L_0x0b26
            java.lang.String r0 = "🖼 "
            goto L_0x0b28
        L_0x0b26:
            java.lang.String r0 = "📎 "
        L_0x0b28:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.CharSequence r0 = r0.caption
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            goto L_0x0ad1
        L_0x0b3c:
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r2 = r2.media
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r3 == 0) goto L_0x0b5d
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r2 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r2
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "📊 "
            r0.append(r3)
            org.telegram.tgnet.TLRPC$Poll r2 = r2.poll
            java.lang.String r2 = r2.question
            r0.append(r2)
            java.lang.String r0 = r0.toString()
        L_0x0b5b:
            r2 = 2
            goto L_0x0b9e
        L_0x0b5d:
            boolean r2 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r2 == 0) goto L_0x0b7d
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "🎮 "
            r0.append(r2)
            org.telegram.messenger.MessageObject r2 = r1.message
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r2 = r2.media
            org.telegram.tgnet.TLRPC$TL_game r2 = r2.game
            java.lang.String r2 = r2.title
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            goto L_0x0b5b
        L_0x0b7d:
            int r2 = r0.type
            r3 = 14
            if (r2 != r3) goto L_0x0b9b
            r2 = 2
            java.lang.Object[] r3 = new java.lang.Object[r2]
            java.lang.String r0 = r0.getMusicAuthor()
            r3[r6] = r0
            org.telegram.messenger.MessageObject r0 = r1.message
            java.lang.String r0 = r0.getMusicTitle()
            r3[r5] = r0
            java.lang.String r0 = "🎧 %s - %s"
            java.lang.String r0 = java.lang.String.format(r0, r3)
            goto L_0x0b9e
        L_0x0b9b:
            r2 = 2
            java.lang.CharSequence r0 = r0.messageText
        L_0x0b9e:
            org.telegram.messenger.MessageObject r3 = r1.message
            org.telegram.tgnet.TLRPC$Message r11 = r3.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r11 = r11.media
            if (r11 == 0) goto L_0x0bb2
            boolean r3 = r3.isMediaEmpty()
            if (r3 != 0) goto L_0x0bb2
            android.text.TextPaint[] r3 = org.telegram.ui.ActionBar.Theme.dialogs_messagePrintingPaint
            int r9 = r1.paintIndex
            r9 = r3[r9]
        L_0x0bb2:
            boolean r3 = r1.hasMessageThumb
            if (r3 == 0) goto L_0x07db
            int r3 = r0.length()
            if (r3 <= r8) goto L_0x0bc0
            java.lang.CharSequence r0 = r0.subSequence(r6, r8)
        L_0x0bc0:
            java.lang.CharSequence r0 = org.telegram.messenger.AndroidUtilities.replaceNewLines(r0)
            boolean r3 = r0 instanceof android.text.SpannableStringBuilder
            if (r3 != 0) goto L_0x0bce
            android.text.SpannableStringBuilder r3 = new android.text.SpannableStringBuilder
            r3.<init>(r0)
            r0 = r3
        L_0x0bce:
            r3 = r0
            android.text.SpannableStringBuilder r3 = (android.text.SpannableStringBuilder) r3
            java.lang.String r11 = " "
            r3.insert(r6, r11)
            org.telegram.ui.Cells.DialogCell$FixedWidthSpan r11 = new org.telegram.ui.Cells.DialogCell$FixedWidthSpan
            int r12 = r7 + 6
            float r12 = (float) r12
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r11.<init>(r12)
            r12 = 33
            r3.setSpan(r11, r6, r5, r12)
            android.text.TextPaint[] r11 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r12 = r1.paintIndex
            r11 = r11[r12]
            android.graphics.Paint$FontMetricsInt r11 = r11.getFontMetricsInt()
            r12 = 1099431936(0x41880000, float:17.0)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r12)
            org.telegram.messenger.Emoji.replaceEmoji(r3, r11, r13, r6)
            goto L_0x07a9
        L_0x0bfc:
            int r14 = r1.currentDialogFolderId
            if (r14 == 0) goto L_0x0CLASSNAME
            java.lang.CharSequence r11 = r36.formatArchivedDialogNames()
        L_0x0CLASSNAME:
            org.telegram.tgnet.TLRPC$DraftMessage r14 = r1.draftMessage
            if (r14 == 0) goto L_0x0CLASSNAME
            int r14 = r14.date
            long r14 = (long) r14
            java.lang.String r14 = org.telegram.messenger.LocaleController.stringForMessageListDate(r14)
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            int r14 = r1.lastMessageDate
            if (r14 == 0) goto L_0x0c1a
            long r14 = (long) r14
            java.lang.String r14 = org.telegram.messenger.LocaleController.stringForMessageListDate(r14)
            goto L_0x0CLASSNAME
        L_0x0c1a:
            org.telegram.messenger.MessageObject r14 = r1.message
            if (r14 == 0) goto L_0x0CLASSNAME
            org.telegram.tgnet.TLRPC$Message r14 = r14.messageOwner
            int r14 = r14.date
            long r14 = (long) r14
            java.lang.String r14 = org.telegram.messenger.LocaleController.stringForMessageListDate(r14)
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            r14 = r4
        L_0x0CLASSNAME:
            org.telegram.messenger.MessageObject r15 = r1.message
            if (r15 != 0) goto L_0x0c3d
            r1.drawCheck1 = r6
            r1.drawCheck2 = r6
            r1.drawClock = r6
            r1.drawCount = r6
            r1.drawMention = r6
            r1.drawError = r6
            r2 = 0
            r8 = 0
            goto L_0x0d30
        L_0x0c3d:
            int r2 = r1.currentDialogFolderId
            if (r2 == 0) goto L_0x0c7c
            int r2 = r1.unreadCount
            int r15 = r1.mentionCount
            int r18 = r2 + r15
            if (r18 <= 0) goto L_0x0CLASSNAME
            if (r2 <= r15) goto L_0x0c5f
            r1.drawCount = r5
            r1.drawMention = r6
            java.lang.Object[] r8 = new java.lang.Object[r5]
            int r2 = r2 + r15
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r8[r6] = r2
            java.lang.String r2 = "%d"
            java.lang.String r2 = java.lang.String.format(r2, r8)
            goto L_0x0c7a
        L_0x0c5f:
            r1.drawCount = r6
            r1.drawMention = r5
            java.lang.Object[] r8 = new java.lang.Object[r5]
            int r2 = r2 + r15
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r8[r6] = r2
            java.lang.String r2 = "%d"
            java.lang.String r2 = java.lang.String.format(r2, r8)
            r8 = r2
            r2 = 0
            goto L_0x0cc1
        L_0x0CLASSNAME:
            r1.drawCount = r6
            r1.drawMention = r6
            r2 = 0
        L_0x0c7a:
            r8 = 0
            goto L_0x0cc1
        L_0x0c7c:
            boolean r2 = r1.clearingDialog
            if (r2 == 0) goto L_0x0CLASSNAME
            r1.drawCount = r6
            r2 = 0
            r10 = 0
            goto L_0x0cb5
        L_0x0CLASSNAME:
            int r2 = r1.unreadCount
            if (r2 == 0) goto L_0x0caa
            if (r2 != r5) goto L_0x0CLASSNAME
            int r8 = r1.mentionCount
            if (r2 != r8) goto L_0x0CLASSNAME
            if (r15 == 0) goto L_0x0CLASSNAME
            org.telegram.tgnet.TLRPC$Message r2 = r15.messageOwner
            boolean r2 = r2.mentioned
            if (r2 != 0) goto L_0x0caa
        L_0x0CLASSNAME:
            r1.drawCount = r5
            java.lang.Object[] r2 = new java.lang.Object[r5]
            int r8 = r1.unreadCount
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            r2[r6] = r8
            java.lang.String r8 = "%d"
            java.lang.String r2 = java.lang.String.format(r8, r2)
            goto L_0x0cb5
        L_0x0caa:
            boolean r2 = r1.markUnread
            if (r2 == 0) goto L_0x0cb2
            r1.drawCount = r5
            r2 = r4
            goto L_0x0cb5
        L_0x0cb2:
            r1.drawCount = r6
            r2 = 0
        L_0x0cb5:
            int r8 = r1.mentionCount
            if (r8 == 0) goto L_0x0cbe
            r1.drawMention = r5
            java.lang.String r8 = "@"
            goto L_0x0cc1
        L_0x0cbe:
            r1.drawMention = r6
            goto L_0x0c7a
        L_0x0cc1:
            org.telegram.messenger.MessageObject r15 = r1.message
            boolean r15 = r15.isOut()
            if (r15 == 0) goto L_0x0d28
            org.telegram.tgnet.TLRPC$DraftMessage r15 = r1.draftMessage
            if (r15 != 0) goto L_0x0d28
            if (r10 == 0) goto L_0x0d28
            org.telegram.messenger.MessageObject r10 = r1.message
            org.telegram.tgnet.TLRPC$Message r15 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r15 = r15.action
            boolean r15 = r15 instanceof org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear
            if (r15 != 0) goto L_0x0d28
            boolean r10 = r10.isSending()
            if (r10 == 0) goto L_0x0ce8
            r1.drawCheck1 = r6
            r1.drawCheck2 = r6
            r1.drawClock = r5
            r1.drawError = r6
            goto L_0x0d30
        L_0x0ce8:
            org.telegram.messenger.MessageObject r10 = r1.message
            boolean r10 = r10.isSendError()
            if (r10 == 0) goto L_0x0cfd
            r1.drawCheck1 = r6
            r1.drawCheck2 = r6
            r1.drawClock = r6
            r1.drawError = r5
            r1.drawCount = r6
            r1.drawMention = r6
            goto L_0x0d30
        L_0x0cfd:
            org.telegram.messenger.MessageObject r10 = r1.message
            boolean r10 = r10.isSent()
            if (r10 == 0) goto L_0x0d30
            org.telegram.messenger.MessageObject r10 = r1.message
            boolean r10 = r10.isUnread()
            if (r10 == 0) goto L_0x0d1e
            org.telegram.tgnet.TLRPC$Chat r10 = r1.chat
            boolean r10 = org.telegram.messenger.ChatObject.isChannel(r10)
            if (r10 == 0) goto L_0x0d1c
            org.telegram.tgnet.TLRPC$Chat r10 = r1.chat
            boolean r10 = r10.megagroup
            if (r10 != 0) goto L_0x0d1c
            goto L_0x0d1e
        L_0x0d1c:
            r10 = 0
            goto L_0x0d1f
        L_0x0d1e:
            r10 = 1
        L_0x0d1f:
            r1.drawCheck1 = r10
            r1.drawCheck2 = r5
            r1.drawClock = r6
            r1.drawError = r6
            goto L_0x0d30
        L_0x0d28:
            r1.drawCheck1 = r6
            r1.drawCheck2 = r6
            r1.drawClock = r6
            r1.drawError = r6
        L_0x0d30:
            r1.promoDialog = r6
            int r10 = r1.currentAccount
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r10)
            int r15 = r1.dialogsType
            r23 = r7
            if (r15 != 0) goto L_0x0d8e
            long r6 = r1.currentDialogId
            boolean r6 = r10.isPromoDialog(r6, r5)
            if (r6 == 0) goto L_0x0d8e
            r1.drawPinBackground = r5
            r1.promoDialog = r5
            int r6 = r10.promoDialogType
            if (r6 != 0) goto L_0x0d58
            r6 = 2131627232(0x7f0e0ce0, float:1.8881723E38)
            java.lang.String r7 = "UseProxySponsor"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            goto L_0x0d8f
        L_0x0d58:
            if (r6 != r5) goto L_0x0d8e
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "PsaType_"
            r6.append(r7)
            java.lang.String r7 = r10.promoPsaType
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r6)
            boolean r7 = android.text.TextUtils.isEmpty(r6)
            if (r7 == 0) goto L_0x0d80
            r6 = 2131626561(0x7f0e0a41, float:1.8880362E38)
            java.lang.String r7 = "PsaTypeDefault"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
        L_0x0d80:
            java.lang.String r7 = r10.promoPsaMessage
            boolean r7 = android.text.TextUtils.isEmpty(r7)
            if (r7 != 0) goto L_0x0d8f
            java.lang.String r0 = r10.promoPsaMessage
            r7 = 0
            r1.hasMessageThumb = r7
            goto L_0x0d8f
        L_0x0d8e:
            r6 = r14
        L_0x0d8f:
            int r7 = r1.currentDialogFolderId
            if (r7 == 0) goto L_0x0da5
            r7 = 2131624233(0x7f0e0129, float:1.887564E38)
            java.lang.String r10 = "ArchivedChats"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r10, r7)
        L_0x0d9c:
            r10 = r13
            r13 = r7
            r35 = r3
            r3 = r0
            r0 = r6
            r6 = r35
            goto L_0x0ded
        L_0x0da5:
            org.telegram.tgnet.TLRPC$Chat r7 = r1.chat
            if (r7 == 0) goto L_0x0dac
            java.lang.String r7 = r7.title
            goto L_0x0ddd
        L_0x0dac:
            org.telegram.tgnet.TLRPC$User r7 = r1.user
            if (r7 == 0) goto L_0x0ddc
            boolean r7 = org.telegram.messenger.UserObject.isUserSelf(r7)
            if (r7 == 0) goto L_0x0dd5
            boolean r7 = r1.useMeForMyMessages
            if (r7 == 0) goto L_0x0dc4
            r7 = 2131625420(0x7f0e05cc, float:1.8878047E38)
            java.lang.String r10 = "FromYou"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r10, r7)
            goto L_0x0ddd
        L_0x0dc4:
            int r7 = r1.dialogsType
            r10 = 3
            if (r7 != r10) goto L_0x0dcb
            r1.drawPinBackground = r5
        L_0x0dcb:
            r7 = 2131626715(0x7f0e0adb, float:1.8880674E38)
            java.lang.String r10 = "SavedMessages"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r10, r7)
            goto L_0x0ddd
        L_0x0dd5:
            org.telegram.tgnet.TLRPC$User r7 = r1.user
            java.lang.String r7 = org.telegram.messenger.UserObject.getUserName(r7)
            goto L_0x0ddd
        L_0x0ddc:
            r7 = r4
        L_0x0ddd:
            int r10 = r7.length()
            if (r10 != 0) goto L_0x0d9c
            r7 = 2131625484(0x7f0e060c, float:1.8878177E38)
            java.lang.String r10 = "HiddenName"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r10, r7)
            goto L_0x0d9c
        L_0x0ded:
            if (r12 == 0) goto L_0x0e2f
            android.text.TextPaint r7 = org.telegram.ui.ActionBar.Theme.dialogs_timePaint
            float r7 = r7.measureText(r0)
            r14 = r6
            double r5 = (double) r7
            double r5 = java.lang.Math.ceil(r5)
            int r5 = (int) r5
            android.text.StaticLayout r6 = new android.text.StaticLayout
            android.text.TextPaint r26 = org.telegram.ui.ActionBar.Theme.dialogs_timePaint
            android.text.Layout$Alignment r28 = android.text.Layout.Alignment.ALIGN_NORMAL
            r29 = 1065353216(0x3var_, float:1.0)
            r30 = 0
            r31 = 0
            r24 = r6
            r25 = r0
            r27 = r5
            r24.<init>(r25, r26, r27, r28, r29, r30, r31)
            r1.timeLayout = r6
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x0e26
            int r0 = r36.getMeasuredWidth()
            r6 = 1097859072(0x41700000, float:15.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r0 = r0 - r6
            int r0 = r0 - r5
            r1.timeLeft = r0
            goto L_0x0e37
        L_0x0e26:
            r0 = 1097859072(0x41700000, float:15.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.timeLeft = r0
            goto L_0x0e37
        L_0x0e2f:
            r14 = r6
            r5 = 0
            r1.timeLayout = r5
            r5 = 0
            r1.timeLeft = r5
            r5 = 0
        L_0x0e37:
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x0e4b
            int r0 = r36.getMeasuredWidth()
            int r6 = r1.nameLeft
            int r0 = r0 - r6
            r6 = 1096810496(0x41600000, float:14.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r0 = r0 - r6
            int r0 = r0 - r5
            goto L_0x0e5f
        L_0x0e4b:
            int r0 = r36.getMeasuredWidth()
            int r6 = r1.nameLeft
            int r0 = r0 - r6
            r6 = 1117388800(0x429a0000, float:77.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r0 = r0 - r6
            int r0 = r0 - r5
            int r6 = r1.nameLeft
            int r6 = r6 + r5
            r1.nameLeft = r6
        L_0x0e5f:
            boolean r6 = r1.drawNameLock
            if (r6 == 0) goto L_0x0e72
            r6 = 1082130432(0x40800000, float:4.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r7 = r7.getIntrinsicWidth()
        L_0x0e6f:
            int r6 = r6 + r7
            int r0 = r0 - r6
            goto L_0x0ea5
        L_0x0e72:
            boolean r6 = r1.drawNameGroup
            if (r6 == 0) goto L_0x0e83
            r6 = 1082130432(0x40800000, float:4.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            int r7 = r7.getIntrinsicWidth()
            goto L_0x0e6f
        L_0x0e83:
            boolean r6 = r1.drawNameBroadcast
            if (r6 == 0) goto L_0x0e94
            r6 = 1082130432(0x40800000, float:4.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
            int r7 = r7.getIntrinsicWidth()
            goto L_0x0e6f
        L_0x0e94:
            boolean r6 = r1.drawNameBot
            if (r6 == 0) goto L_0x0ea5
            r6 = 1082130432(0x40800000, float:4.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r7 = r7.getIntrinsicWidth()
            goto L_0x0e6f
        L_0x0ea5:
            boolean r6 = r1.drawClock
            r7 = 1084227584(0x40a00000, float:5.0)
            if (r6 == 0) goto L_0x0ed4
            android.graphics.drawable.Drawable r6 = org.telegram.ui.ActionBar.Theme.dialogs_clockDrawable
            int r6 = r6.getIntrinsicWidth()
            int r24 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r6 = r6 + r24
            int r0 = r0 - r6
            boolean r24 = org.telegram.messenger.LocaleController.isRTL
            if (r24 != 0) goto L_0x0ec3
            int r5 = r1.timeLeft
            int r5 = r5 - r6
            r1.checkDrawLeft = r5
            goto L_0x0f4a
        L_0x0ec3:
            int r12 = r1.timeLeft
            int r12 = r12 + r5
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r12 = r12 + r5
            r1.checkDrawLeft = r12
            int r5 = r1.nameLeft
            int r5 = r5 + r6
            r1.nameLeft = r5
            goto L_0x0f4a
        L_0x0ed4:
            boolean r6 = r1.drawCheck2
            if (r6 == 0) goto L_0x0f4a
            android.graphics.drawable.Drawable r6 = org.telegram.ui.ActionBar.Theme.dialogs_checkDrawable
            int r6 = r6.getIntrinsicWidth()
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r6 = r6 + r12
            int r0 = r0 - r6
            boolean r12 = r1.drawCheck1
            if (r12 == 0) goto L_0x0var_
            android.graphics.drawable.Drawable r12 = org.telegram.ui.ActionBar.Theme.dialogs_halfCheckDrawable
            int r12 = r12.getIntrinsicWidth()
            r24 = 1090519040(0x41000000, float:8.0)
            int r24 = org.telegram.messenger.AndroidUtilities.dp(r24)
            int r12 = r12 - r24
            int r0 = r0 - r12
            boolean r12 = org.telegram.messenger.LocaleController.isRTL
            if (r12 != 0) goto L_0x0f0a
            int r5 = r1.timeLeft
            int r5 = r5 - r6
            r1.halfCheckDrawLeft = r5
            r6 = 1085276160(0x40b00000, float:5.5)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r5 = r5 - r6
            r1.checkDrawLeft = r5
            goto L_0x0f4a
        L_0x0f0a:
            int r12 = r1.timeLeft
            int r12 = r12 + r5
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r12 = r12 + r5
            r1.checkDrawLeft = r12
            r5 = 1085276160(0x40b00000, float:5.5)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r12 = r12 + r5
            r1.halfCheckDrawLeft = r12
            int r5 = r1.nameLeft
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_halfCheckDrawable
            int r7 = r7.getIntrinsicWidth()
            int r6 = r6 + r7
            r7 = 1090519040(0x41000000, float:8.0)
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r6 = r6 - r7
            int r5 = r5 + r6
            r1.nameLeft = r5
            goto L_0x0f4a
        L_0x0var_:
            boolean r12 = org.telegram.messenger.LocaleController.isRTL
            if (r12 != 0) goto L_0x0f3b
            int r5 = r1.timeLeft
            int r5 = r5 - r6
            r1.checkDrawLeft = r5
            goto L_0x0f4a
        L_0x0f3b:
            int r12 = r1.timeLeft
            int r12 = r12 + r5
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r7)
            int r12 = r12 + r5
            r1.checkDrawLeft = r12
            int r5 = r1.nameLeft
            int r5 = r5 + r6
            r1.nameLeft = r5
        L_0x0f4a:
            boolean r5 = r1.dialogMuted
            r6 = 1086324736(0x40CLASSNAME, float:6.0)
            if (r5 == 0) goto L_0x0f6e
            boolean r5 = r1.drawVerified
            if (r5 != 0) goto L_0x0f6e
            boolean r5 = r1.drawScam
            if (r5 != 0) goto L_0x0f6e
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_muteDrawable
            int r7 = r7.getIntrinsicWidth()
            int r5 = r5 + r7
            int r0 = r0 - r5
            boolean r7 = org.telegram.messenger.LocaleController.isRTL
            if (r7 == 0) goto L_0x0fa1
            int r7 = r1.nameLeft
            int r7 = r7 + r5
            r1.nameLeft = r7
            goto L_0x0fa1
        L_0x0f6e:
            boolean r5 = r1.drawVerified
            if (r5 == 0) goto L_0x0var_
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedDrawable
            int r7 = r7.getIntrinsicWidth()
            int r5 = r5 + r7
            int r0 = r0 - r5
            boolean r7 = org.telegram.messenger.LocaleController.isRTL
            if (r7 == 0) goto L_0x0fa1
            int r7 = r1.nameLeft
            int r7 = r7 + r5
            r1.nameLeft = r7
            goto L_0x0fa1
        L_0x0var_:
            boolean r5 = r1.drawScam
            if (r5 == 0) goto L_0x0fa1
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r6)
            org.telegram.ui.Components.ScamDrawable r7 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            int r7 = r7.getIntrinsicWidth()
            int r5 = r5 + r7
            int r0 = r0 - r5
            boolean r7 = org.telegram.messenger.LocaleController.isRTL
            if (r7 == 0) goto L_0x0fa1
            int r7 = r1.nameLeft
            int r7 = r7 + r5
            r1.nameLeft = r7
        L_0x0fa1:
            r5 = 1094713344(0x41400000, float:12.0)
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r7 = java.lang.Math.max(r7, r0)
            r12 = 32
            r15 = 10
            java.lang.String r0 = r13.replace(r15, r12)     // Catch:{ Exception -> 0x0fe0 }
            android.text.TextPaint[] r12 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint     // Catch:{ Exception -> 0x0fe0 }
            int r13 = r1.paintIndex     // Catch:{ Exception -> 0x0fe0 }
            r12 = r12[r13]     // Catch:{ Exception -> 0x0fe0 }
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r5)     // Catch:{ Exception -> 0x0fe0 }
            int r13 = r7 - r13
            float r13 = (float) r13     // Catch:{ Exception -> 0x0fe0 }
            android.text.TextUtils$TruncateAt r15 = android.text.TextUtils.TruncateAt.END     // Catch:{ Exception -> 0x0fe0 }
            java.lang.CharSequence r25 = android.text.TextUtils.ellipsize(r0, r12, r13, r15)     // Catch:{ Exception -> 0x0fe0 }
            android.text.StaticLayout r0 = new android.text.StaticLayout     // Catch:{ Exception -> 0x0fe0 }
            android.text.TextPaint[] r12 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint     // Catch:{ Exception -> 0x0fe0 }
            int r13 = r1.paintIndex     // Catch:{ Exception -> 0x0fe0 }
            r26 = r12[r13]     // Catch:{ Exception -> 0x0fe0 }
            android.text.Layout$Alignment r28 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x0fe0 }
            r29 = 1065353216(0x3var_, float:1.0)
            r30 = 0
            r31 = 0
            r24 = r0
            r27 = r7
            r24.<init>(r25, r26, r27, r28, r29, r30, r31)     // Catch:{ Exception -> 0x0fe0 }
            r1.nameLayout = r0     // Catch:{ Exception -> 0x0fe0 }
            goto L_0x0fe4
        L_0x0fe0:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0fe4:
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x109c
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x0fee
            goto L_0x109c
        L_0x0fee:
            r0 = 1091567616(0x41100000, float:9.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r12 = 1106771968(0x41var_, float:31.0)
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.messageNameTop = r12
            r12 = 1098907648(0x41800000, float:16.0)
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.timeTop = r12
            r12 = 1109131264(0x421CLASSNAME, float:39.0)
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.errorTop = r12
            r12 = 1109131264(0x421CLASSNAME, float:39.0)
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.pinTop = r12
            r12 = 1109131264(0x421CLASSNAME, float:39.0)
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.countTop = r12
            r12 = 1099431936(0x41880000, float:17.0)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r12)
            r1.checkDrawTop = r13
            int r12 = r36.getMeasuredWidth()
            r13 = 1119748096(0x42be0000, float:95.0)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
            int r12 = r12 - r13
            boolean r13 = org.telegram.messenger.LocaleController.isRTL
            if (r13 == 0) goto L_0x1050
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r19)
            r1.messageNameLeft = r13
            r1.messageLeft = r13
            int r13 = r36.getMeasuredWidth()
            r15 = 1115684864(0x42800000, float:64.0)
            int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
            int r13 = r13 - r15
            int r15 = r23 + 11
            float r15 = (float) r15
            int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
            int r15 = r13 - r15
            goto L_0x1065
        L_0x1050:
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r20)
            r1.messageNameLeft = r13
            r1.messageLeft = r13
            r13 = 1092616192(0x41200000, float:10.0)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
            r15 = 1116078080(0x42860000, float:67.0)
            int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
            int r15 = r15 + r13
        L_0x1065:
            org.telegram.messenger.ImageReceiver r6 = r1.avatarImage
            float r13 = (float) r13
            float r5 = (float) r0
            r16 = 1113063424(0x42580000, float:54.0)
            r34 = r4
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r16)
            float r4 = (float) r4
            r22 = r12
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r16)
            float r12 = (float) r12
            r6.setImageCoords(r13, r5, r4, r12)
            org.telegram.messenger.ImageReceiver r4 = r1.thumbImage
            float r5 = (float) r15
            r6 = 1106247680(0x41var_, float:30.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r6 = r6 + r0
            float r6 = (float) r6
            r13 = r23
            float r12 = (float) r13
            int r15 = org.telegram.messenger.AndroidUtilities.dp(r12)
            float r15 = (float) r15
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            float r12 = (float) r12
            r4.setImageCoords(r5, r6, r15, r12)
            r4 = r0
            r23 = r14
            goto L_0x1149
        L_0x109c:
            r34 = r4
            r13 = r23
            r0 = 1093664768(0x41300000, float:11.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r4 = 1107296256(0x42000000, float:32.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.messageNameTop = r4
            r4 = 1095761920(0x41500000, float:13.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.timeTop = r4
            r4 = 1110179840(0x422CLASSNAME, float:43.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.errorTop = r4
            r4 = 1110179840(0x422CLASSNAME, float:43.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.pinTop = r4
            r4 = 1110179840(0x422CLASSNAME, float:43.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.countTop = r4
            r4 = 1095761920(0x41500000, float:13.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.checkDrawTop = r4
            int r4 = r36.getMeasuredWidth()
            r5 = 1119485952(0x42ba0000, float:93.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r12 = r4 - r5
            boolean r4 = org.telegram.messenger.LocaleController.isRTL
            if (r4 == 0) goto L_0x1104
            r4 = 1098907648(0x41800000, float:16.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r1.messageNameLeft = r4
            r1.messageLeft = r4
            int r4 = r36.getMeasuredWidth()
            r5 = 1115947008(0x42840000, float:66.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r4 = r4 - r5
            r5 = 1106771968(0x41var_, float:31.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r5 = r4 - r5
            goto L_0x1119
        L_0x1104:
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r22)
            r1.messageNameLeft = r4
            r1.messageLeft = r4
            r4 = 1092616192(0x41200000, float:10.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r5 = 1116340224(0x428a0000, float:69.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r5 = r5 + r4
        L_0x1119:
            org.telegram.messenger.ImageReceiver r6 = r1.avatarImage
            float r4 = (float) r4
            float r15 = (float) r0
            r16 = 1113587712(0x42600000, float:56.0)
            r22 = r12
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r16)
            float r12 = (float) r12
            r23 = r14
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r16)
            float r14 = (float) r14
            r6.setImageCoords(r4, r15, r12, r14)
            org.telegram.messenger.ImageReceiver r4 = r1.thumbImage
            float r5 = (float) r5
            r6 = 1106771968(0x41var_, float:31.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r6 = r6 + r0
            float r6 = (float) r6
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r21)
            float r12 = (float) r12
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r21)
            float r14 = (float) r14
            r4.setImageCoords(r5, r6, r12, r14)
            r4 = r0
        L_0x1149:
            boolean r0 = r1.drawPin
            if (r0 == 0) goto L_0x116e
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 != 0) goto L_0x1166
            int r0 = r36.getMeasuredWidth()
            android.graphics.drawable.Drawable r5 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedDrawable
            int r5 = r5.getIntrinsicWidth()
            int r0 = r0 - r5
            r5 = 1096810496(0x41600000, float:14.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r0 = r0 - r5
            r1.pinLeft = r0
            goto L_0x116e
        L_0x1166:
            r0 = 1096810496(0x41600000, float:14.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.pinLeft = r0
        L_0x116e:
            boolean r0 = r1.drawError
            if (r0 == 0) goto L_0x11a2
            r0 = 1106771968(0x41var_, float:31.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            int r22 = r22 - r0
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x118c
            int r0 = r36.getMeasuredWidth()
            r2 = 1107820544(0x42080000, float:34.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            int r0 = r0 - r2
            r1.errorLeft = r0
            goto L_0x119e
        L_0x118c:
            r2 = 1093664768(0x41300000, float:11.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.errorLeft = r2
            int r2 = r1.messageLeft
            int r2 = r2 + r0
            r1.messageLeft = r2
            int r2 = r1.messageNameLeft
            int r2 = r2 + r0
            r1.messageNameLeft = r2
        L_0x119e:
            r0 = r22
            goto L_0x12bd
        L_0x11a2:
            if (r2 != 0) goto L_0x11ce
            if (r8 == 0) goto L_0x11a7
            goto L_0x11ce
        L_0x11a7:
            boolean r0 = r1.drawPin
            if (r0 == 0) goto L_0x11c8
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedDrawable
            int r0 = r0.getIntrinsicWidth()
            r2 = 1090519040(0x41000000, float:8.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            int r0 = r0 + r2
            int r22 = r22 - r0
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 == 0) goto L_0x11c8
            int r2 = r1.messageLeft
            int r2 = r2 + r0
            r1.messageLeft = r2
            int r2 = r1.messageNameLeft
            int r2 = r2 + r0
            r1.messageNameLeft = r2
        L_0x11c8:
            r2 = 0
            r1.drawCount = r2
            r1.drawMention = r2
            goto L_0x119e
        L_0x11ce:
            if (r2 == 0) goto L_0x1231
            r5 = 1094713344(0x41400000, float:12.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r5)
            android.text.TextPaint r5 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            float r5 = r5.measureText(r2)
            double r5 = (double) r5
            double r5 = java.lang.Math.ceil(r5)
            int r5 = (int) r5
            int r0 = java.lang.Math.max(r0, r5)
            r1.countWidth = r0
            android.text.StaticLayout r0 = new android.text.StaticLayout
            android.text.TextPaint r26 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            int r5 = r1.countWidth
            android.text.Layout$Alignment r28 = android.text.Layout.Alignment.ALIGN_CENTER
            r29 = 1065353216(0x3var_, float:1.0)
            r30 = 0
            r31 = 0
            r24 = r0
            r25 = r2
            r27 = r5
            r24.<init>(r25, r26, r27, r28, r29, r30, r31)
            r1.countLayout = r0
            int r0 = r1.countWidth
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r21)
            int r0 = r0 + r2
            int r22 = r22 - r0
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x121d
            int r0 = r36.getMeasuredWidth()
            int r2 = r1.countWidth
            int r0 = r0 - r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r17)
            int r0 = r0 - r2
            r1.countLeft = r0
            goto L_0x122d
        L_0x121d:
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r17)
            r1.countLeft = r2
            int r2 = r1.messageLeft
            int r2 = r2 + r0
            r1.messageLeft = r2
            int r2 = r1.messageNameLeft
            int r2 = r2 + r0
            r1.messageNameLeft = r2
        L_0x122d:
            r2 = 1
            r1.drawCount = r2
            goto L_0x1234
        L_0x1231:
            r2 = 0
            r1.countWidth = r2
        L_0x1234:
            if (r8 == 0) goto L_0x119e
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x126c
            r2 = 1094713344(0x41400000, float:12.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r2)
            android.text.TextPaint r2 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            float r2 = r2.measureText(r8)
            double r5 = (double) r2
            double r5 = java.lang.Math.ceil(r5)
            int r2 = (int) r5
            int r0 = java.lang.Math.max(r0, r2)
            r1.mentionWidth = r0
            android.text.StaticLayout r0 = new android.text.StaticLayout
            android.text.TextPaint r26 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            int r2 = r1.mentionWidth
            android.text.Layout$Alignment r28 = android.text.Layout.Alignment.ALIGN_CENTER
            r29 = 1065353216(0x3var_, float:1.0)
            r30 = 0
            r31 = 0
            r24 = r0
            r25 = r8
            r27 = r2
            r24.<init>(r25, r26, r27, r28, r29, r30, r31)
            r1.mentionLayout = r0
            goto L_0x1274
        L_0x126c:
            r2 = 1094713344(0x41400000, float:12.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.mentionWidth = r0
        L_0x1274:
            int r0 = r1.mentionWidth
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r21)
            int r0 = r0 + r2
            int r22 = r22 - r0
            boolean r2 = org.telegram.messenger.LocaleController.isRTL
            if (r2 != 0) goto L_0x129c
            int r0 = r36.getMeasuredWidth()
            int r2 = r1.mentionWidth
            int r0 = r0 - r2
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r17)
            int r0 = r0 - r2
            int r2 = r1.countWidth
            if (r2 == 0) goto L_0x1297
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r21)
            int r2 = r2 + r5
            goto L_0x1298
        L_0x1297:
            r2 = 0
        L_0x1298:
            int r0 = r0 - r2
            r1.mentionLeft = r0
            goto L_0x12b8
        L_0x129c:
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r17)
            int r5 = r1.countWidth
            if (r5 == 0) goto L_0x12aa
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r21)
            int r5 = r5 + r6
            goto L_0x12ab
        L_0x12aa:
            r5 = 0
        L_0x12ab:
            int r2 = r2 + r5
            r1.mentionLeft = r2
            int r2 = r1.messageLeft
            int r2 = r2 + r0
            r1.messageLeft = r2
            int r2 = r1.messageNameLeft
            int r2 = r2 + r0
            r1.messageNameLeft = r2
        L_0x12b8:
            r2 = 1
            r1.drawMention = r2
            goto L_0x119e
        L_0x12bd:
            if (r10 == 0) goto L_0x1304
            if (r3 != 0) goto L_0x12c3
            r3 = r34
        L_0x12c3:
            java.lang.String r2 = r3.toString()
            int r3 = r2.length()
            r5 = 150(0x96, float:2.1E-43)
            if (r3 <= r5) goto L_0x12d4
            r3 = 0
            java.lang.String r2 = r2.substring(r3, r5)
        L_0x12d4:
            boolean r3 = r1.useForceThreeLines
            if (r3 != 0) goto L_0x12dc
            boolean r3 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r3 == 0) goto L_0x12de
        L_0x12dc:
            if (r11 == 0) goto L_0x12e7
        L_0x12de:
            r3 = 32
            r5 = 10
            java.lang.String r2 = r2.replace(r5, r3)
            goto L_0x12ef
        L_0x12e7:
            java.lang.String r3 = "\n\n"
            java.lang.String r5 = "\n"
            java.lang.String r2 = r2.replace(r3, r5)
        L_0x12ef:
            android.text.TextPaint[] r3 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r5 = r1.paintIndex
            r3 = r3[r5]
            android.graphics.Paint$FontMetricsInt r3 = r3.getFontMetricsInt()
            r5 = 1099431936(0x41880000, float:17.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            r6 = 0
            java.lang.CharSequence r3 = org.telegram.messenger.Emoji.replaceEmoji(r2, r3, r5, r6)
        L_0x1304:
            r2 = 1094713344(0x41400000, float:12.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r2)
            int r2 = java.lang.Math.max(r5, r0)
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x1316
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x1356
        L_0x1316:
            if (r11 == 0) goto L_0x1356
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x1321
            int r0 = r1.currentDialogFolderDialogsCount
            r5 = 1
            if (r0 != r5) goto L_0x1356
        L_0x1321:
            android.text.TextPaint r25 = org.telegram.ui.ActionBar.Theme.dialogs_messageNamePaint     // Catch:{ Exception -> 0x133c }
            android.text.Layout$Alignment r27 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x133c }
            r28 = 1065353216(0x3var_, float:1.0)
            r29 = 0
            r30 = 0
            android.text.TextUtils$TruncateAt r31 = android.text.TextUtils.TruncateAt.END     // Catch:{ Exception -> 0x133c }
            r33 = 1
            r24 = r11
            r26 = r2
            r32 = r2
            android.text.StaticLayout r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r24, r25, r26, r27, r28, r29, r30, r31, r32, r33)     // Catch:{ Exception -> 0x133c }
            r1.messageNameLayout = r0     // Catch:{ Exception -> 0x133c }
            goto L_0x1340
        L_0x133c:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x1340:
            r0 = 1112276992(0x424CLASSNAME, float:51.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.messageTop = r0
            org.telegram.messenger.ImageReceiver r0 = r1.thumbImage
            r5 = 1109393408(0x42200000, float:40.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r4 = r4 + r5
            r0.setImageY(r4)
            r5 = 0
            goto L_0x137f
        L_0x1356:
            r5 = 0
            r1.messageNameLayout = r5
            boolean r0 = r1.useForceThreeLines
            if (r0 != 0) goto L_0x136b
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r0 == 0) goto L_0x1362
            goto L_0x136b
        L_0x1362:
            r0 = 1109131264(0x421CLASSNAME, float:39.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.messageTop = r0
            goto L_0x137f
        L_0x136b:
            r0 = 1107296256(0x42000000, float:32.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r1.messageTop = r0
            org.telegram.messenger.ImageReceiver r0 = r1.thumbImage
            r6 = 1101529088(0x41a80000, float:21.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r4 = r4 + r6
            r0.setImageY(r4)
        L_0x137f:
            boolean r0 = r1.useForceThreeLines     // Catch:{ Exception -> 0x1425 }
            if (r0 != 0) goto L_0x1387
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout     // Catch:{ Exception -> 0x1425 }
            if (r0 == 0) goto L_0x1399
        L_0x1387:
            int r0 = r1.currentDialogFolderId     // Catch:{ Exception -> 0x1425 }
            if (r0 == 0) goto L_0x1399
            int r0 = r1.currentDialogFolderDialogsCount     // Catch:{ Exception -> 0x1425 }
            r4 = 1
            if (r0 <= r4) goto L_0x1399
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint     // Catch:{ Exception -> 0x1425 }
            int r3 = r1.paintIndex     // Catch:{ Exception -> 0x1425 }
            r9 = r0[r3]     // Catch:{ Exception -> 0x1425 }
            r8 = r5
            r0 = r11
            goto L_0x13b5
        L_0x1399:
            boolean r0 = r1.useForceThreeLines     // Catch:{ Exception -> 0x1425 }
            if (r0 != 0) goto L_0x13a1
            boolean r0 = org.telegram.messenger.SharedConfig.useThreeLinesLayout     // Catch:{ Exception -> 0x1425 }
            if (r0 == 0) goto L_0x13a3
        L_0x13a1:
            if (r11 == 0) goto L_0x13b3
        L_0x13a3:
            r4 = 1094713344(0x41400000, float:12.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r4)     // Catch:{ Exception -> 0x1425 }
            int r0 = r2 - r0
            float r0 = (float) r0     // Catch:{ Exception -> 0x1425 }
            android.text.TextUtils$TruncateAt r4 = android.text.TextUtils.TruncateAt.END     // Catch:{ Exception -> 0x1425 }
            java.lang.CharSequence r0 = android.text.TextUtils.ellipsize(r3, r9, r0, r4)     // Catch:{ Exception -> 0x1425 }
            goto L_0x13b4
        L_0x13b3:
            r0 = r3
        L_0x13b4:
            r8 = r11
        L_0x13b5:
            boolean r3 = r1.useForceThreeLines     // Catch:{ Exception -> 0x1425 }
            if (r3 != 0) goto L_0x13f1
            boolean r3 = org.telegram.messenger.SharedConfig.useThreeLinesLayout     // Catch:{ Exception -> 0x1425 }
            if (r3 == 0) goto L_0x13be
            goto L_0x13f1
        L_0x13be:
            boolean r3 = r1.hasMessageThumb     // Catch:{ Exception -> 0x1425 }
            if (r3 == 0) goto L_0x13d9
            r3 = 1086324736(0x40CLASSNAME, float:6.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r3)     // Catch:{ Exception -> 0x1425 }
            int r4 = r4 + r13
            int r2 = r2 + r4
            boolean r4 = org.telegram.messenger.LocaleController.isRTL     // Catch:{ Exception -> 0x1425 }
            if (r4 == 0) goto L_0x13d9
            int r4 = r1.messageLeft     // Catch:{ Exception -> 0x1425 }
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r3)     // Catch:{ Exception -> 0x1425 }
            int r3 = r13 + r5
            int r4 = r4 - r3
            r1.messageLeft = r4     // Catch:{ Exception -> 0x1425 }
        L_0x13d9:
            android.text.StaticLayout r3 = new android.text.StaticLayout     // Catch:{ Exception -> 0x1425 }
            android.text.Layout$Alignment r28 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x1425 }
            r29 = 1065353216(0x3var_, float:1.0)
            r30 = 0
            r31 = 0
            r24 = r3
            r25 = r0
            r26 = r9
            r27 = r2
            r24.<init>(r25, r26, r27, r28, r29, r30, r31)     // Catch:{ Exception -> 0x1425 }
            r1.messageLayout = r3     // Catch:{ Exception -> 0x1425 }
            goto L_0x1429
        L_0x13f1:
            boolean r3 = r1.hasMessageThumb     // Catch:{ Exception -> 0x1425 }
            if (r3 == 0) goto L_0x13fe
            if (r8 == 0) goto L_0x13fe
            r3 = 1086324736(0x40CLASSNAME, float:6.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r3)     // Catch:{ Exception -> 0x1425 }
            int r2 = r2 + r4
        L_0x13fe:
            android.text.Layout$Alignment r27 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x1425 }
            r28 = 1065353216(0x3var_, float:1.0)
            r3 = 1065353216(0x3var_, float:1.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)     // Catch:{ Exception -> 0x1425 }
            float r3 = (float) r3     // Catch:{ Exception -> 0x1425 }
            r30 = 0
            android.text.TextUtils$TruncateAt r31 = android.text.TextUtils.TruncateAt.END     // Catch:{ Exception -> 0x1425 }
            if (r8 == 0) goto L_0x1412
            r33 = 1
            goto L_0x1414
        L_0x1412:
            r33 = 2
        L_0x1414:
            r24 = r0
            r25 = r9
            r26 = r2
            r29 = r3
            r32 = r2
            android.text.StaticLayout r0 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r24, r25, r26, r27, r28, r29, r30, r31, r32, r33)     // Catch:{ Exception -> 0x1425 }
            r1.messageLayout = r0     // Catch:{ Exception -> 0x1425 }
            goto L_0x1429
        L_0x1425:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x1429:
            boolean r0 = org.telegram.messenger.LocaleController.isRTL
            if (r0 == 0) goto L_0x155d
            android.text.StaticLayout r0 = r1.nameLayout
            if (r0 == 0) goto L_0x14e6
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x14e6
            android.text.StaticLayout r0 = r1.nameLayout
            r3 = 0
            float r0 = r0.getLineLeft(r3)
            android.text.StaticLayout r4 = r1.nameLayout
            float r4 = r4.getLineWidth(r3)
            double r3 = (double) r4
            double r3 = java.lang.Math.ceil(r3)
            boolean r5 = r1.dialogMuted
            if (r5 == 0) goto L_0x147b
            boolean r5 = r1.drawVerified
            if (r5 != 0) goto L_0x147b
            boolean r5 = r1.drawScam
            if (r5 != 0) goto L_0x147b
            int r5 = r1.nameLeft
            double r5 = (double) r5
            double r8 = (double) r7
            java.lang.Double.isNaN(r8)
            double r8 = r8 - r3
            java.lang.Double.isNaN(r5)
            double r5 = r5 + r8
            r8 = 1086324736(0x40CLASSNAME, float:6.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            android.graphics.drawable.Drawable r8 = org.telegram.ui.ActionBar.Theme.dialogs_muteDrawable
            int r8 = r8.getIntrinsicWidth()
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            int r5 = (int) r5
            r1.nameMuteLeft = r5
            goto L_0x14ce
        L_0x147b:
            boolean r5 = r1.drawVerified
            if (r5 == 0) goto L_0x14a5
            int r5 = r1.nameLeft
            double r5 = (double) r5
            double r8 = (double) r7
            java.lang.Double.isNaN(r8)
            double r8 = r8 - r3
            java.lang.Double.isNaN(r5)
            double r5 = r5 + r8
            r8 = 1086324736(0x40CLASSNAME, float:6.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            android.graphics.drawable.Drawable r8 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedDrawable
            int r8 = r8.getIntrinsicWidth()
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            int r5 = (int) r5
            r1.nameMuteLeft = r5
            goto L_0x14ce
        L_0x14a5:
            boolean r5 = r1.drawScam
            if (r5 == 0) goto L_0x14ce
            int r5 = r1.nameLeft
            double r5 = (double) r5
            double r8 = (double) r7
            java.lang.Double.isNaN(r8)
            double r8 = r8 - r3
            java.lang.Double.isNaN(r5)
            double r5 = r5 + r8
            r8 = 1086324736(0x40CLASSNAME, float:6.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            org.telegram.ui.Components.ScamDrawable r8 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            int r8 = r8.getIntrinsicWidth()
            double r8 = (double) r8
            java.lang.Double.isNaN(r8)
            double r5 = r5 - r8
            int r5 = (int) r5
            r1.nameMuteLeft = r5
        L_0x14ce:
            r5 = 0
            int r0 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1))
            if (r0 != 0) goto L_0x14e6
            double r5 = (double) r7
            int r0 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x14e6
            int r0 = r1.nameLeft
            double r7 = (double) r0
            java.lang.Double.isNaN(r5)
            double r5 = r5 - r3
            java.lang.Double.isNaN(r7)
            double r7 = r7 + r5
            int r0 = (int) r7
            r1.nameLeft = r0
        L_0x14e6:
            android.text.StaticLayout r0 = r1.messageLayout
            if (r0 == 0) goto L_0x1527
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x1527
            r3 = 2147483647(0x7fffffff, float:NaN)
            r3 = 0
            r4 = 2147483647(0x7fffffff, float:NaN)
        L_0x14f7:
            if (r3 >= r0) goto L_0x151d
            android.text.StaticLayout r5 = r1.messageLayout
            float r5 = r5.getLineLeft(r3)
            r6 = 0
            int r5 = (r5 > r6 ? 1 : (r5 == r6 ? 0 : -1))
            if (r5 != 0) goto L_0x151c
            android.text.StaticLayout r5 = r1.messageLayout
            float r5 = r5.getLineWidth(r3)
            double r5 = (double) r5
            double r5 = java.lang.Math.ceil(r5)
            double r7 = (double) r2
            java.lang.Double.isNaN(r7)
            double r7 = r7 - r5
            int r5 = (int) r7
            int r4 = java.lang.Math.min(r4, r5)
            int r3 = r3 + 1
            goto L_0x14f7
        L_0x151c:
            r4 = 0
        L_0x151d:
            r0 = 2147483647(0x7fffffff, float:NaN)
            if (r4 == r0) goto L_0x1527
            int r0 = r1.messageLeft
            int r0 = r0 + r4
            r1.messageLeft = r0
        L_0x1527:
            android.text.StaticLayout r0 = r1.messageNameLayout
            if (r0 == 0) goto L_0x15e7
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x15e7
            android.text.StaticLayout r0 = r1.messageNameLayout
            r3 = 0
            float r0 = r0.getLineLeft(r3)
            r4 = 0
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 != 0) goto L_0x15e7
            android.text.StaticLayout r0 = r1.messageNameLayout
            float r0 = r0.getLineWidth(r3)
            double r3 = (double) r0
            double r3 = java.lang.Math.ceil(r3)
            double r5 = (double) r2
            int r0 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x15e7
            int r0 = r1.messageNameLeft
            double r7 = (double) r0
            java.lang.Double.isNaN(r5)
            double r5 = r5 - r3
            java.lang.Double.isNaN(r7)
            double r7 = r7 + r5
            int r0 = (int) r7
            r1.messageNameLeft = r0
            goto L_0x15e7
        L_0x155d:
            android.text.StaticLayout r0 = r1.nameLayout
            if (r0 == 0) goto L_0x15ac
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x15ac
            android.text.StaticLayout r0 = r1.nameLayout
            r2 = 0
            float r0 = r0.getLineRight(r2)
            float r3 = (float) r7
            int r3 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r3 != 0) goto L_0x1591
            android.text.StaticLayout r3 = r1.nameLayout
            float r3 = r3.getLineWidth(r2)
            double r2 = (double) r3
            double r2 = java.lang.Math.ceil(r2)
            double r4 = (double) r7
            int r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r6 >= 0) goto L_0x1591
            int r6 = r1.nameLeft
            double r6 = (double) r6
            java.lang.Double.isNaN(r4)
            double r4 = r4 - r2
            java.lang.Double.isNaN(r6)
            double r6 = r6 - r4
            int r2 = (int) r6
            r1.nameLeft = r2
        L_0x1591:
            boolean r2 = r1.dialogMuted
            if (r2 != 0) goto L_0x159d
            boolean r2 = r1.drawVerified
            if (r2 != 0) goto L_0x159d
            boolean r2 = r1.drawScam
            if (r2 == 0) goto L_0x15ac
        L_0x159d:
            int r2 = r1.nameLeft
            float r2 = (float) r2
            float r2 = r2 + r0
            r3 = 1086324736(0x40CLASSNAME, float:6.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r3)
            float r0 = (float) r0
            float r2 = r2 + r0
            int r0 = (int) r2
            r1.nameMuteLeft = r0
        L_0x15ac:
            android.text.StaticLayout r0 = r1.messageLayout
            if (r0 == 0) goto L_0x15cf
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x15cf
            r2 = 1325400064(0x4var_, float:2.14748365E9)
            r7 = 0
        L_0x15b9:
            if (r7 >= r0) goto L_0x15c8
            android.text.StaticLayout r3 = r1.messageLayout
            float r3 = r3.getLineLeft(r7)
            float r2 = java.lang.Math.min(r2, r3)
            int r7 = r7 + 1
            goto L_0x15b9
        L_0x15c8:
            int r0 = r1.messageLeft
            float r0 = (float) r0
            float r0 = r0 - r2
            int r0 = (int) r0
            r1.messageLeft = r0
        L_0x15cf:
            android.text.StaticLayout r0 = r1.messageNameLayout
            if (r0 == 0) goto L_0x15e7
            int r0 = r0.getLineCount()
            if (r0 <= 0) goto L_0x15e7
            int r0 = r1.messageNameLeft
            float r0 = (float) r0
            android.text.StaticLayout r2 = r1.messageNameLayout
            r3 = 0
            float r2 = r2.getLineLeft(r3)
            float r0 = r0 - r2
            int r0 = (int) r0
            r1.messageNameLeft = r0
        L_0x15e7:
            android.text.StaticLayout r0 = r1.messageLayout
            if (r0 == 0) goto L_0x162d
            boolean r2 = r1.hasMessageThumb
            if (r2 == 0) goto L_0x162d
            java.lang.CharSequence r0 = r0.getText()     // Catch:{ Exception -> 0x1629 }
            int r0 = r0.length()     // Catch:{ Exception -> 0x1629 }
            r3 = r23
            r2 = 1
            if (r3 < r0) goto L_0x15ff
            int r6 = r0 + -1
            goto L_0x1600
        L_0x15ff:
            r6 = r3
        L_0x1600:
            android.text.StaticLayout r0 = r1.messageLayout     // Catch:{ Exception -> 0x1629 }
            float r0 = r0.getPrimaryHorizontal(r6)     // Catch:{ Exception -> 0x1629 }
            android.text.StaticLayout r3 = r1.messageLayout     // Catch:{ Exception -> 0x1629 }
            int r6 = r6 + r2
            float r2 = r3.getPrimaryHorizontal(r6)     // Catch:{ Exception -> 0x1629 }
            float r0 = java.lang.Math.min(r0, r2)     // Catch:{ Exception -> 0x1629 }
            double r2 = (double) r0     // Catch:{ Exception -> 0x1629 }
            double r2 = java.lang.Math.ceil(r2)     // Catch:{ Exception -> 0x1629 }
            int r0 = (int) r2     // Catch:{ Exception -> 0x1629 }
            if (r0 == 0) goto L_0x1620
            r2 = 1077936128(0x40400000, float:3.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)     // Catch:{ Exception -> 0x1629 }
            int r0 = r0 + r2
        L_0x1620:
            org.telegram.messenger.ImageReceiver r2 = r1.thumbImage     // Catch:{ Exception -> 0x1629 }
            int r3 = r1.messageLeft     // Catch:{ Exception -> 0x1629 }
            int r3 = r3 + r0
            r2.setImageX(r3)     // Catch:{ Exception -> 0x1629 }
            goto L_0x162d
        L_0x1629:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x162d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.DialogCell.buildLayout():void");
    }

    public boolean isPointInsideAvatar(float f, float f2) {
        if (!LocaleController.isRTL) {
            if (f < 0.0f || f >= ((float) AndroidUtilities.dp(60.0f))) {
                return false;
            }
            return true;
        } else if (f < ((float) (getMeasuredWidth() - AndroidUtilities.dp(60.0f))) || f >= ((float) getMeasuredWidth())) {
            return false;
        } else {
            return true;
        }
    }

    public void setDialogSelected(boolean z) {
        if (this.isSelected != z) {
            invalidate();
        }
        this.isSelected = z;
    }

    public void checkCurrentDialogIndex(boolean z) {
        MessageObject messageObject;
        MessageObject messageObject2;
        MessageObject messageObject3;
        ArrayList<TLRPC$Dialog> dialogsArray = DialogsActivity.getDialogsArray(this.currentAccount, this.dialogsType, this.folderId, z);
        if (this.index < dialogsArray.size()) {
            TLRPC$Dialog tLRPC$Dialog = dialogsArray.get(this.index);
            boolean z2 = true;
            TLRPC$Dialog tLRPC$Dialog2 = this.index + 1 < dialogsArray.size() ? dialogsArray.get(this.index + 1) : null;
            TLRPC$DraftMessage draft = MediaDataController.getInstance(this.currentAccount).getDraft(this.currentDialogId);
            if (this.currentDialogFolderId != 0) {
                messageObject = findFolderTopMessage();
            } else {
                messageObject = MessagesController.getInstance(this.currentAccount).dialogMessage.get(tLRPC$Dialog.id);
            }
            if (this.currentDialogId != tLRPC$Dialog.id || (((messageObject2 = this.message) != null && messageObject2.getId() != tLRPC$Dialog.top_message) || ((messageObject != null && messageObject.messageOwner.edit_date != this.currentEditDate) || this.unreadCount != tLRPC$Dialog.unread_count || this.mentionCount != tLRPC$Dialog.unread_mentions_count || this.markUnread != tLRPC$Dialog.unread_mark || (messageObject3 = this.message) != messageObject || ((messageObject3 == null && messageObject != null) || draft != this.draftMessage || this.drawPin != tLRPC$Dialog.pinned)))) {
                boolean z3 = this.currentDialogId != tLRPC$Dialog.id;
                this.currentDialogId = tLRPC$Dialog.id;
                boolean z4 = tLRPC$Dialog instanceof TLRPC$TL_dialogFolder;
                if (z4) {
                    this.currentDialogFolderId = ((TLRPC$TL_dialogFolder) tLRPC$Dialog).folder.id;
                } else {
                    this.currentDialogFolderId = 0;
                }
                int i = this.dialogsType;
                if (i == 7 || i == 8) {
                    MessagesController.DialogFilter dialogFilter = MessagesController.getInstance(this.currentAccount).selectedDialogFilter[this.dialogsType == 8 ? (char) 1 : 0];
                    if (!(tLRPC$Dialog instanceof TLRPC$TL_dialog) || tLRPC$Dialog2 == null || dialogFilter == null || dialogFilter.pinnedDialogs.indexOfKey(tLRPC$Dialog.id) < 0 || dialogFilter.pinnedDialogs.indexOfKey(tLRPC$Dialog2.id) >= 0) {
                        z2 = false;
                    }
                    this.fullSeparator = z2;
                    this.fullSeparator2 = false;
                } else {
                    this.fullSeparator = (tLRPC$Dialog instanceof TLRPC$TL_dialog) && tLRPC$Dialog.pinned && tLRPC$Dialog2 != null && !tLRPC$Dialog2.pinned;
                    if (!z4 || tLRPC$Dialog2 == null || tLRPC$Dialog2.pinned) {
                        z2 = false;
                    }
                    this.fullSeparator2 = z2;
                }
                update(0);
                if (z3) {
                    this.reorderIconProgress = (!this.drawPin || !this.drawReorder) ? 0.0f : 1.0f;
                }
                checkOnline();
            }
        }
    }

    public void animateArchiveAvatar() {
        if (this.avatarDrawable.getAvatarType() == 2) {
            this.animatingArchiveAvatar = true;
            this.animatingArchiveAvatarProgress = 0.0f;
            Theme.dialogs_archiveAvatarDrawable.setProgress(0.0f);
            Theme.dialogs_archiveAvatarDrawable.start();
            invalidate();
        }
    }

    public void setChecked(boolean z, boolean z2) {
        CheckBox2 checkBox2 = this.checkBox;
        if (checkBox2 != null) {
            checkBox2.setChecked(z, z2);
        }
    }

    private MessageObject findFolderTopMessage() {
        ArrayList<TLRPC$Dialog> dialogsArray = DialogsActivity.getDialogsArray(this.currentAccount, this.dialogsType, this.currentDialogFolderId, false);
        MessageObject messageObject = null;
        if (!dialogsArray.isEmpty()) {
            int size = dialogsArray.size();
            for (int i = 0; i < size; i++) {
                TLRPC$Dialog tLRPC$Dialog = dialogsArray.get(i);
                MessageObject messageObject2 = MessagesController.getInstance(this.currentAccount).dialogMessage.get(tLRPC$Dialog.id);
                if (messageObject2 != null && (messageObject == null || messageObject2.messageOwner.date > messageObject.messageOwner.date)) {
                    messageObject = messageObject2;
                }
                if (tLRPC$Dialog.pinnedNum == 0 && messageObject != null) {
                    break;
                }
            }
        }
        return messageObject;
    }

    public boolean isFolderCell() {
        return this.currentDialogFolderId != 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:116:0x01b9  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0212  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void update(int r20) {
        /*
            r19 = this;
            r0 = r19
            r1 = r20
            org.telegram.ui.Cells.DialogCell$CustomDialog r2 = r0.customDialog
            r3 = 0
            r4 = 1
            r5 = 0
            if (r2 == 0) goto L_0x0041
            int r1 = r2.date
            r0.lastMessageDate = r1
            int r1 = r2.unread_count
            if (r1 == 0) goto L_0x0014
            goto L_0x0015
        L_0x0014:
            r4 = 0
        L_0x0015:
            r0.lastUnreadState = r4
            org.telegram.ui.Cells.DialogCell$CustomDialog r1 = r0.customDialog
            int r2 = r1.unread_count
            r0.unreadCount = r2
            boolean r2 = r1.pinned
            r0.drawPin = r2
            boolean r2 = r1.muted
            r0.dialogMuted = r2
            org.telegram.ui.Components.AvatarDrawable r2 = r0.avatarDrawable
            int r4 = r1.id
            java.lang.String r1 = r1.name
            r2.setInfo(r4, r1, r3)
            org.telegram.messenger.ImageReceiver r5 = r0.avatarImage
            r6 = 0
            org.telegram.ui.Components.AvatarDrawable r8 = r0.avatarDrawable
            r9 = 0
            r10 = 0
            java.lang.String r7 = "50_50"
            r5.setImage(r6, r7, r8, r9, r10)
            org.telegram.messenger.ImageReceiver r1 = r0.thumbImage
            r1.setImageBitmap((android.graphics.drawable.Drawable) r3)
            goto L_0x0366
        L_0x0041:
            boolean r2 = r0.isDialogCell
            if (r2 == 0) goto L_0x0105
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r2 = r2.dialogs_dict
            long r6 = r0.currentDialogId
            java.lang.Object r2 = r2.get(r6)
            org.telegram.tgnet.TLRPC$Dialog r2 = (org.telegram.tgnet.TLRPC$Dialog) r2
            if (r2 == 0) goto L_0x00fa
            if (r1 != 0) goto L_0x0107
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            long r7 = r2.id
            boolean r6 = r6.isClearingDialog(r7)
            r0.clearingDialog = r6
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r6 = r6.dialogMessage
            long r7 = r2.id
            java.lang.Object r6 = r6.get(r7)
            org.telegram.messenger.MessageObject r6 = (org.telegram.messenger.MessageObject) r6
            r0.message = r6
            if (r6 == 0) goto L_0x0083
            boolean r6 = r6.isUnread()
            if (r6 == 0) goto L_0x0083
            r6 = 1
            goto L_0x0084
        L_0x0083:
            r6 = 0
        L_0x0084:
            r0.lastUnreadState = r6
            boolean r6 = r2 instanceof org.telegram.tgnet.TLRPC$TL_dialogFolder
            if (r6 == 0) goto L_0x0099
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesStorage r6 = org.telegram.messenger.MessagesStorage.getInstance(r6)
            int r6 = r6.getArchiveUnreadCount()
            r0.unreadCount = r6
            r0.mentionCount = r5
            goto L_0x00a1
        L_0x0099:
            int r6 = r2.unread_count
            r0.unreadCount = r6
            int r6 = r2.unread_mentions_count
            r0.mentionCount = r6
        L_0x00a1:
            boolean r6 = r2.unread_mark
            r0.markUnread = r6
            org.telegram.messenger.MessageObject r6 = r0.message
            if (r6 == 0) goto L_0x00ae
            org.telegram.tgnet.TLRPC$Message r6 = r6.messageOwner
            int r6 = r6.edit_date
            goto L_0x00af
        L_0x00ae:
            r6 = 0
        L_0x00af:
            r0.currentEditDate = r6
            int r6 = r2.last_message_date
            r0.lastMessageDate = r6
            int r6 = r0.dialogsType
            r7 = 7
            r8 = 8
            if (r6 == r7) goto L_0x00cd
            if (r6 != r8) goto L_0x00bf
            goto L_0x00cd
        L_0x00bf:
            int r6 = r0.currentDialogFolderId
            if (r6 != 0) goto L_0x00c9
            boolean r2 = r2.pinned
            if (r2 == 0) goto L_0x00c9
            r2 = 1
            goto L_0x00ca
        L_0x00c9:
            r2 = 0
        L_0x00ca:
            r0.drawPin = r2
            goto L_0x00ef
        L_0x00cd:
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.messenger.MessagesController$DialogFilter[] r6 = r6.selectedDialogFilter
            int r7 = r0.dialogsType
            if (r7 != r8) goto L_0x00db
            r7 = 1
            goto L_0x00dc
        L_0x00db:
            r7 = 0
        L_0x00dc:
            r6 = r6[r7]
            if (r6 == 0) goto L_0x00ec
            android.util.LongSparseArray<java.lang.Integer> r6 = r6.pinnedDialogs
            long r7 = r2.id
            int r2 = r6.indexOfKey(r7)
            if (r2 < 0) goto L_0x00ec
            r2 = 1
            goto L_0x00ed
        L_0x00ec:
            r2 = 0
        L_0x00ed:
            r0.drawPin = r2
        L_0x00ef:
            org.telegram.messenger.MessageObject r2 = r0.message
            if (r2 == 0) goto L_0x0107
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            int r2 = r2.send_state
            r0.lastSendState = r2
            goto L_0x0107
        L_0x00fa:
            r0.unreadCount = r5
            r0.mentionCount = r5
            r0.currentEditDate = r5
            r0.lastMessageDate = r5
            r0.clearingDialog = r5
            goto L_0x0107
        L_0x0105:
            r0.drawPin = r5
        L_0x0107:
            if (r1 == 0) goto L_0x0216
            org.telegram.tgnet.TLRPC$User r2 = r0.user
            if (r2 == 0) goto L_0x0128
            r2 = r1 & 4
            if (r2 == 0) goto L_0x0128
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            org.telegram.tgnet.TLRPC$User r6 = r0.user
            int r6 = r6.id
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$User r2 = r2.getUser(r6)
            r0.user = r2
            r19.invalidate()
        L_0x0128:
            boolean r2 = r0.isDialogCell
            if (r2 == 0) goto L_0x015a
            r2 = r1 & 64
            if (r2 == 0) goto L_0x015a
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            android.util.LongSparseArray<java.lang.CharSequence> r2 = r2.printingStrings
            long r6 = r0.currentDialogId
            java.lang.Object r2 = r2.get(r6)
            java.lang.CharSequence r2 = (java.lang.CharSequence) r2
            java.lang.CharSequence r6 = r0.lastPrintString
            if (r6 == 0) goto L_0x0146
            if (r2 == 0) goto L_0x0158
        L_0x0146:
            java.lang.CharSequence r6 = r0.lastPrintString
            if (r6 != 0) goto L_0x014c
            if (r2 != 0) goto L_0x0158
        L_0x014c:
            java.lang.CharSequence r6 = r0.lastPrintString
            if (r6 == 0) goto L_0x015a
            if (r2 == 0) goto L_0x015a
            boolean r2 = r6.equals(r2)
            if (r2 != 0) goto L_0x015a
        L_0x0158:
            r2 = 1
            goto L_0x015b
        L_0x015a:
            r2 = 0
        L_0x015b:
            if (r2 != 0) goto L_0x016e
            r6 = 32768(0x8000, float:4.5918E-41)
            r6 = r6 & r1
            if (r6 == 0) goto L_0x016e
            org.telegram.messenger.MessageObject r6 = r0.message
            if (r6 == 0) goto L_0x016e
            java.lang.CharSequence r6 = r6.messageText
            java.lang.CharSequence r7 = r0.lastMessageString
            if (r6 == r7) goto L_0x016e
            r2 = 1
        L_0x016e:
            if (r2 != 0) goto L_0x0179
            r6 = r1 & 2
            if (r6 == 0) goto L_0x0179
            org.telegram.tgnet.TLRPC$Chat r6 = r0.chat
            if (r6 != 0) goto L_0x0179
            r2 = 1
        L_0x0179:
            if (r2 != 0) goto L_0x0184
            r6 = r1 & 1
            if (r6 == 0) goto L_0x0184
            org.telegram.tgnet.TLRPC$Chat r6 = r0.chat
            if (r6 != 0) goto L_0x0184
            r2 = 1
        L_0x0184:
            if (r2 != 0) goto L_0x018f
            r6 = r1 & 8
            if (r6 == 0) goto L_0x018f
            org.telegram.tgnet.TLRPC$User r6 = r0.user
            if (r6 != 0) goto L_0x018f
            r2 = 1
        L_0x018f:
            if (r2 != 0) goto L_0x019a
            r6 = r1 & 16
            if (r6 == 0) goto L_0x019a
            org.telegram.tgnet.TLRPC$User r6 = r0.user
            if (r6 != 0) goto L_0x019a
            r2 = 1
        L_0x019a:
            if (r2 != 0) goto L_0x01fb
            r6 = r1 & 256(0x100, float:3.59E-43)
            if (r6 == 0) goto L_0x01fb
            org.telegram.messenger.MessageObject r6 = r0.message
            if (r6 == 0) goto L_0x01b5
            boolean r7 = r0.lastUnreadState
            boolean r6 = r6.isUnread()
            if (r7 == r6) goto L_0x01b5
            org.telegram.messenger.MessageObject r2 = r0.message
            boolean r2 = r2.isUnread()
            r0.lastUnreadState = r2
            r2 = 1
        L_0x01b5:
            boolean r6 = r0.isDialogCell
            if (r6 == 0) goto L_0x01fb
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r6 = r6.dialogs_dict
            long r7 = r0.currentDialogId
            java.lang.Object r6 = r6.get(r7)
            org.telegram.tgnet.TLRPC$Dialog r6 = (org.telegram.tgnet.TLRPC$Dialog) r6
            boolean r7 = r6 instanceof org.telegram.tgnet.TLRPC$TL_dialogFolder
            if (r7 == 0) goto L_0x01d9
            int r7 = r0.currentAccount
            org.telegram.messenger.MessagesStorage r7 = org.telegram.messenger.MessagesStorage.getInstance(r7)
            int r7 = r7.getArchiveUnreadCount()
        L_0x01d7:
            r8 = 0
            goto L_0x01e2
        L_0x01d9:
            if (r6 == 0) goto L_0x01e0
            int r7 = r6.unread_count
            int r8 = r6.unread_mentions_count
            goto L_0x01e2
        L_0x01e0:
            r7 = 0
            goto L_0x01d7
        L_0x01e2:
            if (r6 == 0) goto L_0x01fb
            int r9 = r0.unreadCount
            if (r9 != r7) goto L_0x01f2
            boolean r9 = r0.markUnread
            boolean r10 = r6.unread_mark
            if (r9 != r10) goto L_0x01f2
            int r9 = r0.mentionCount
            if (r9 == r8) goto L_0x01fb
        L_0x01f2:
            r0.unreadCount = r7
            r0.mentionCount = r8
            boolean r2 = r6.unread_mark
            r0.markUnread = r2
            r2 = 1
        L_0x01fb:
            if (r2 != 0) goto L_0x0210
            r1 = r1 & 4096(0x1000, float:5.74E-42)
            if (r1 == 0) goto L_0x0210
            org.telegram.messenger.MessageObject r1 = r0.message
            if (r1 == 0) goto L_0x0210
            int r6 = r0.lastSendState
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            int r1 = r1.send_state
            if (r6 == r1) goto L_0x0210
            r0.lastSendState = r1
            r2 = 1
        L_0x0210:
            if (r2 != 0) goto L_0x0216
            r19.invalidate()
            return
        L_0x0216:
            r0.user = r3
            r0.chat = r3
            r0.encryptedChat = r3
            int r1 = r0.currentDialogFolderId
            r2 = 0
            if (r1 == 0) goto L_0x0233
            r0.dialogMuted = r5
            org.telegram.messenger.MessageObject r1 = r19.findFolderTopMessage()
            r0.message = r1
            if (r1 == 0) goto L_0x0231
            long r6 = r1.getDialogId()
            goto L_0x024c
        L_0x0231:
            r6 = r2
            goto L_0x024c
        L_0x0233:
            boolean r1 = r0.isDialogCell
            if (r1 == 0) goto L_0x0247
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            long r6 = r0.currentDialogId
            boolean r1 = r1.isDialogMuted(r6)
            if (r1 == 0) goto L_0x0247
            r1 = 1
            goto L_0x0248
        L_0x0247:
            r1 = 0
        L_0x0248:
            r0.dialogMuted = r1
            long r6 = r0.currentDialogId
        L_0x024c:
            int r1 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r1 == 0) goto L_0x02ed
            int r1 = (int) r6
            r2 = 32
            long r2 = r6 >> r2
            int r3 = (int) r2
            if (r1 == 0) goto L_0x029f
            if (r1 >= 0) goto L_0x028e
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            int r1 = -r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$Chat r1 = r2.getChat(r1)
            r0.chat = r1
            boolean r2 = r0.isDialogCell
            if (r2 != 0) goto L_0x02c5
            if (r1 == 0) goto L_0x02c5
            org.telegram.tgnet.TLRPC$InputChannel r1 = r1.migrated_to
            if (r1 == 0) goto L_0x02c5
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            org.telegram.tgnet.TLRPC$Chat r2 = r0.chat
            org.telegram.tgnet.TLRPC$InputChannel r2 = r2.migrated_to
            int r2 = r2.channel_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r1 = r1.getChat(r2)
            if (r1 == 0) goto L_0x02c5
            r0.chat = r1
            goto L_0x02c5
        L_0x028e:
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r1 = r2.getUser(r1)
            r0.user = r1
            goto L_0x02c5
        L_0x029f:
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$EncryptedChat r1 = r1.getEncryptedChat(r2)
            r0.encryptedChat = r1
            if (r1 == 0) goto L_0x02c5
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            org.telegram.tgnet.TLRPC$EncryptedChat r2 = r0.encryptedChat
            int r2 = r2.user_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r2)
            r0.user = r1
        L_0x02c5:
            boolean r1 = r0.useMeForMyMessages
            if (r1 == 0) goto L_0x02ed
            org.telegram.tgnet.TLRPC$User r1 = r0.user
            if (r1 == 0) goto L_0x02ed
            org.telegram.messenger.MessageObject r1 = r0.message
            boolean r1 = r1.isOutOwner()
            if (r1 == 0) goto L_0x02ed
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            int r2 = r0.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            int r2 = r2.clientUserId
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r2)
            r0.user = r1
        L_0x02ed:
            int r1 = r0.currentDialogFolderId
            if (r1 == 0) goto L_0x030a
            org.telegram.ui.Components.RLottieDrawable r1 = org.telegram.ui.ActionBar.Theme.dialogs_archiveAvatarDrawable
            r1.setCallback(r0)
            org.telegram.ui.Components.AvatarDrawable r1 = r0.avatarDrawable
            r2 = 2
            r1.setAvatarType(r2)
            org.telegram.messenger.ImageReceiver r3 = r0.avatarImage
            r4 = 0
            r5 = 0
            org.telegram.ui.Components.AvatarDrawable r6 = r0.avatarDrawable
            r7 = 0
            org.telegram.tgnet.TLRPC$User r8 = r0.user
            r9 = 0
            r3.setImage(r4, r5, r6, r7, r8, r9)
            goto L_0x0366
        L_0x030a:
            org.telegram.tgnet.TLRPC$User r1 = r0.user
            if (r1 == 0) goto L_0x034a
            org.telegram.ui.Components.AvatarDrawable r2 = r0.avatarDrawable
            r2.setInfo((org.telegram.tgnet.TLRPC$User) r1)
            org.telegram.tgnet.TLRPC$User r1 = r0.user
            boolean r1 = org.telegram.messenger.UserObject.isUserSelf(r1)
            if (r1 == 0) goto L_0x0332
            boolean r1 = r0.useMeForMyMessages
            if (r1 != 0) goto L_0x0332
            org.telegram.ui.Components.AvatarDrawable r1 = r0.avatarDrawable
            r1.setAvatarType(r4)
            org.telegram.messenger.ImageReceiver r5 = r0.avatarImage
            r6 = 0
            r7 = 0
            org.telegram.ui.Components.AvatarDrawable r8 = r0.avatarDrawable
            r9 = 0
            org.telegram.tgnet.TLRPC$User r10 = r0.user
            r11 = 0
            r5.setImage(r6, r7, r8, r9, r10, r11)
            goto L_0x0366
        L_0x0332:
            org.telegram.messenger.ImageReceiver r12 = r0.avatarImage
            org.telegram.tgnet.TLRPC$User r1 = r0.user
            org.telegram.messenger.ImageLocation r13 = org.telegram.messenger.ImageLocation.getForUser(r1, r5)
            org.telegram.ui.Components.AvatarDrawable r15 = r0.avatarDrawable
            r16 = 0
            org.telegram.tgnet.TLRPC$User r1 = r0.user
            r18 = 0
            java.lang.String r14 = "50_50"
            r17 = r1
            r12.setImage(r13, r14, r15, r16, r17, r18)
            goto L_0x0366
        L_0x034a:
            org.telegram.tgnet.TLRPC$Chat r1 = r0.chat
            if (r1 == 0) goto L_0x0366
            org.telegram.ui.Components.AvatarDrawable r2 = r0.avatarDrawable
            r2.setInfo((org.telegram.tgnet.TLRPC$Chat) r1)
            org.telegram.messenger.ImageReceiver r6 = r0.avatarImage
            org.telegram.tgnet.TLRPC$Chat r1 = r0.chat
            org.telegram.messenger.ImageLocation r7 = org.telegram.messenger.ImageLocation.getForChat(r1, r5)
            org.telegram.ui.Components.AvatarDrawable r9 = r0.avatarDrawable
            r10 = 0
            org.telegram.tgnet.TLRPC$Chat r11 = r0.chat
            r12 = 0
            java.lang.String r8 = "50_50"
            r6.setImage(r7, r8, r9, r10, r11, r12)
        L_0x0366:
            int r1 = r19.getMeasuredWidth()
            if (r1 != 0) goto L_0x0377
            int r1 = r19.getMeasuredHeight()
            if (r1 == 0) goto L_0x0373
            goto L_0x0377
        L_0x0373:
            r19.requestLayout()
            goto L_0x037a
        L_0x0377:
            r19.buildLayout()
        L_0x037a:
            r19.invalidate()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.DialogCell.update(int):void");
    }

    public float getTranslationX() {
        return this.translationX;
    }

    public void setTranslationX(float f) {
        float f2 = (float) ((int) f);
        this.translationX = f2;
        RLottieDrawable rLottieDrawable = this.translationDrawable;
        boolean z = false;
        if (rLottieDrawable != null && f2 == 0.0f) {
            rLottieDrawable.setProgress(0.0f);
            this.translationAnimationStarted = false;
            this.archiveHidden = SharedConfig.archiveHidden;
            this.currentRevealProgress = 0.0f;
            this.isSliding = false;
        }
        if (this.translationX != 0.0f) {
            this.isSliding = true;
        }
        if (this.isSliding) {
            boolean z2 = this.drawRevealBackground;
            if (Math.abs(this.translationX) >= ((float) getMeasuredWidth()) * 0.3f) {
                z = true;
            }
            this.drawRevealBackground = z;
            if (z2 != z && this.archiveHidden == SharedConfig.archiveHidden) {
                try {
                    performHapticFeedback(3, 2);
                } catch (Exception unused) {
                }
            }
        }
        invalidate();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x047f  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x048e  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x04ca  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x055a  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x05a8  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0608  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x065a  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x0686  */
    /* JADX WARNING: Removed duplicated region for block: B:265:0x0717  */
    /* JADX WARNING: Removed duplicated region for block: B:266:0x0766  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x08b7  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x08ea  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0921  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x093c  */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0a2d  */
    /* JADX WARNING: Removed duplicated region for block: B:383:0x0a45  */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0aa9  */
    /* JADX WARNING: Removed duplicated region for block: B:414:0x0b02  */
    /* JADX WARNING: Removed duplicated region for block: B:420:0x0b15  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x0b2e  */
    /* JADX WARNING: Removed duplicated region for block: B:437:0x0b57  */
    /* JADX WARNING: Removed duplicated region for block: B:448:0x0b84  */
    /* JADX WARNING: Removed duplicated region for block: B:454:0x0b98  */
    /* JADX WARNING: Removed duplicated region for block: B:464:0x0bbe  */
    /* JADX WARNING: Removed duplicated region for block: B:474:0x0bde  */
    /* JADX WARNING: Removed duplicated region for block: B:476:? A[RETURN, SYNTHETIC] */
    @android.annotation.SuppressLint({"DrawAllocation"})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r25) {
        /*
            r24 = this;
            r1 = r24
            r8 = r25
            long r2 = r1.currentDialogId
            r4 = 0
            int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r0 != 0) goto L_0x0011
            org.telegram.ui.Cells.DialogCell$CustomDialog r0 = r1.customDialog
            if (r0 != 0) goto L_0x0011
            return
        L_0x0011:
            int r0 = r1.currentDialogFolderId
            r9 = 0
            if (r0 == 0) goto L_0x002a
            org.telegram.ui.Components.PullForegroundDrawable r0 = r1.archivedChatsDrawable
            if (r0 == 0) goto L_0x002a
            float r2 = r0.outProgress
            int r2 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r2 != 0) goto L_0x002a
            float r2 = r1.translationX
            int r2 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r2 != 0) goto L_0x002a
            r0.draw(r8)
            return
        L_0x002a:
            long r2 = android.os.SystemClock.elapsedRealtime()
            long r4 = r1.lastUpdateTime
            long r4 = r2 - r4
            r6 = 17
            int r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r0 <= 0) goto L_0x003a
            r10 = r6
            goto L_0x003b
        L_0x003a:
            r10 = r4
        L_0x003b:
            r1.lastUpdateTime = r2
            float r0 = r1.clipProgress
            r12 = 24
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x0069
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 == r12) goto L_0x0069
            r25.save()
            int r0 = r1.topClip
            float r0 = (float) r0
            float r2 = r1.clipProgress
            float r0 = r0 * r2
            int r2 = r24.getMeasuredWidth()
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            int r4 = r1.bottomClip
            float r4 = (float) r4
            float r5 = r1.clipProgress
            float r4 = r4 * r5
            int r4 = (int) r4
            int r3 = r3 - r4
            float r3 = (float) r3
            r8.clipRect(r9, r0, r2, r3)
        L_0x0069:
            float r0 = r1.translationX
            r13 = 1090519040(0x41000000, float:8.0)
            r14 = 2
            r15 = 0
            r7 = 1
            r6 = 1065353216(0x3var_, float:1.0)
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 != 0) goto L_0x0099
            float r0 = r1.cornerProgress
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x007d
            goto L_0x0099
        L_0x007d:
            org.telegram.ui.Components.RLottieDrawable r0 = r1.translationDrawable
            if (r0 == 0) goto L_0x0093
            r0.stop()
            org.telegram.ui.Components.RLottieDrawable r0 = r1.translationDrawable
            r0.setProgress(r9)
            org.telegram.ui.Components.RLottieDrawable r0 = r1.translationDrawable
            r2 = 0
            r0.setCallback(r2)
            r1.translationDrawable = r2
            r1.translationAnimationStarted = r15
        L_0x0093:
            r13 = 1065353216(0x3var_, float:1.0)
            r19 = 1
            goto L_0x0310
        L_0x0099:
            r25.save()
            int r0 = r1.currentDialogFolderId
            java.lang.String r16 = "chats_archivePinBackground"
            java.lang.String r17 = "chats_archiveBackground"
            if (r0 == 0) goto L_0x00d4
            boolean r0 = r1.archiveHidden
            if (r0 == 0) goto L_0x00be
            int r0 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r17)
            r3 = 2131627186(0x7f0e0cb2, float:1.888163E38)
            java.lang.String r4 = "UnhideFromTop"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_unpinArchiveDrawable
            r1.translationDrawable = r4
            goto L_0x00ed
        L_0x00be:
            int r0 = org.telegram.ui.ActionBar.Theme.getColor(r17)
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r3 = 2131625486(0x7f0e060e, float:1.8878181E38)
            java.lang.String r4 = "HideOnTop"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_pinArchiveDrawable
            r1.translationDrawable = r4
            goto L_0x00ed
        L_0x00d4:
            boolean r0 = r1.promoDialog
            if (r0 == 0) goto L_0x00f0
            int r0 = org.telegram.ui.ActionBar.Theme.getColor(r17)
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r3 = 2131626552(0x7f0e0a38, float:1.8880343E38)
            java.lang.String r4 = "PsaHide"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            r1.translationDrawable = r4
        L_0x00ed:
            r5 = r2
            r4 = r3
            goto L_0x0120
        L_0x00f0:
            int r0 = r1.folderId
            if (r0 != 0) goto L_0x010a
            int r0 = org.telegram.ui.ActionBar.Theme.getColor(r17)
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r3 = 2131624218(0x7f0e011a, float:1.887561E38)
            java.lang.String r4 = "Archive"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawable
            r1.translationDrawable = r4
            goto L_0x00ed
        L_0x010a:
            int r0 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r17)
            r3 = 2131627177(0x7f0e0ca9, float:1.8881611E38)
            java.lang.String r4 = "Unarchive"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_unarchiveDrawable
            r1.translationDrawable = r4
            goto L_0x00ed
        L_0x0120:
            boolean r2 = r1.translationAnimationStarted
            r18 = 1110179840(0x422CLASSNAME, float:43.0)
            if (r2 != 0) goto L_0x0146
            float r2 = r1.translationX
            float r2 = java.lang.Math.abs(r2)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r18)
            float r3 = (float) r3
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 <= 0) goto L_0x0146
            r1.translationAnimationStarted = r7
            org.telegram.ui.Components.RLottieDrawable r2 = r1.translationDrawable
            r2.setProgress(r9)
            org.telegram.ui.Components.RLottieDrawable r2 = r1.translationDrawable
            r2.setCallback(r1)
            org.telegram.ui.Components.RLottieDrawable r2 = r1.translationDrawable
            r2.start()
        L_0x0146:
            int r2 = r24.getMeasuredWidth()
            float r2 = (float) r2
            float r3 = r1.translationX
            float r3 = r3 + r2
            float r2 = r1.currentRevealProgress
            int r2 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r2 >= 0) goto L_0x01cb
            android.graphics.Paint r2 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r2.setColor(r0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r13)
            float r0 = (float) r0
            float r0 = r3 - r0
            r19 = 0
            int r2 = r24.getMeasuredWidth()
            float r2 = (float) r2
            int r6 = r24.getMeasuredHeight()
            float r6 = (float) r6
            android.graphics.Paint r20 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r21 = r2
            r2 = r25
            r12 = r3
            r3 = r0
            r0 = r4
            r4 = r19
            r22 = r5
            r5 = r21
            r19 = 1
            r7 = r20
            r2.drawRect(r3, r4, r5, r6, r7)
            float r2 = r1.currentRevealProgress
            int r2 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r2 != 0) goto L_0x01d1
            boolean r2 = org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawableRecolored
            if (r2 == 0) goto L_0x0199
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawable
            int r3 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r17)
            java.lang.String r4 = "Arrow.**"
            r2.setLayerColor(r4, r3)
            org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawableRecolored = r15
        L_0x0199:
            boolean r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawableRecolored
            if (r2 == 0) goto L_0x01d1
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            r2.beginApplyLayerColors()
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r3 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r17)
            java.lang.String r4 = "Line 1.**"
            r2.setLayerColor(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r3 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r17)
            java.lang.String r4 = "Line 2.**"
            r2.setLayerColor(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r3 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r17)
            java.lang.String r4 = "Line 3.**"
            r2.setLayerColor(r4, r3)
            org.telegram.ui.Components.RLottieDrawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            r2.commitApplyLayerColors()
            org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawableRecolored = r15
            goto L_0x01d1
        L_0x01cb:
            r12 = r3
            r0 = r4
            r22 = r5
            r19 = 1
        L_0x01d1:
            int r2 = r24.getMeasuredWidth()
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r18)
            int r2 = r2 - r3
            org.telegram.ui.Components.RLottieDrawable r3 = r1.translationDrawable
            int r3 = r3.getIntrinsicWidth()
            int r3 = r3 / r14
            int r2 = r2 - r3
            boolean r3 = r1.useForceThreeLines
            if (r3 != 0) goto L_0x01ee
            boolean r3 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r3 == 0) goto L_0x01eb
            goto L_0x01ee
        L_0x01eb:
            r3 = 1091567616(0x41100000, float:9.0)
            goto L_0x01f0
        L_0x01ee:
            r3 = 1094713344(0x41400000, float:12.0)
        L_0x01f0:
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            org.telegram.ui.Components.RLottieDrawable r4 = r1.translationDrawable
            int r4 = r4.getIntrinsicWidth()
            int r4 = r4 / r14
            int r4 = r4 + r2
            org.telegram.ui.Components.RLottieDrawable r5 = r1.translationDrawable
            int r5 = r5.getIntrinsicHeight()
            int r5 = r5 / r14
            int r5 = r5 + r3
            float r6 = r1.currentRevealProgress
            int r6 = (r6 > r9 ? 1 : (r6 == r9 ? 0 : -1))
            if (r6 <= 0) goto L_0x0295
            r25.save()
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r13)
            float r6 = (float) r6
            float r6 = r12 - r6
            int r7 = r24.getMeasuredWidth()
            float r7 = (float) r7
            int r13 = r24.getMeasuredHeight()
            float r13 = (float) r13
            r8.clipRect(r6, r9, r7, r13)
            android.graphics.Paint r6 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r7 = r22
            r6.setColor(r7)
            int r6 = r4 * r4
            int r7 = r24.getMeasuredHeight()
            int r7 = r5 - r7
            int r13 = r24.getMeasuredHeight()
            int r13 = r5 - r13
            int r7 = r7 * r13
            int r6 = r6 + r7
            double r6 = (double) r6
            double r6 = java.lang.Math.sqrt(r6)
            float r6 = (float) r6
            float r4 = (float) r4
            float r5 = (float) r5
            android.view.animation.AccelerateInterpolator r7 = org.telegram.messenger.AndroidUtilities.accelerateInterpolator
            float r13 = r1.currentRevealProgress
            float r7 = r7.getInterpolation(r13)
            float r6 = r6 * r7
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r8.drawCircle(r4, r5, r6, r7)
            r25.restore()
            boolean r4 = org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawableRecolored
            if (r4 != 0) goto L_0x0264
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawable
            int r5 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r16)
            java.lang.String r6 = "Arrow.**"
            r4.setLayerColor(r6, r5)
            org.telegram.ui.ActionBar.Theme.dialogs_archiveDrawableRecolored = r19
        L_0x0264:
            boolean r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawableRecolored
            if (r4 != 0) goto L_0x0295
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            r4.beginApplyLayerColors()
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r5 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r16)
            java.lang.String r6 = "Line 1.**"
            r4.setLayerColor(r6, r5)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r5 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r16)
            java.lang.String r6 = "Line 2.**"
            r4.setLayerColor(r6, r5)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            int r5 = org.telegram.ui.ActionBar.Theme.getNonAnimatedColor(r16)
            java.lang.String r6 = "Line 3.**"
            r4.setLayerColor(r6, r5)
            org.telegram.ui.Components.RLottieDrawable r4 = org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawable
            r4.commitApplyLayerColors()
            org.telegram.ui.ActionBar.Theme.dialogs_hidePsaDrawableRecolored = r19
        L_0x0295:
            r25.save()
            float r2 = (float) r2
            float r3 = (float) r3
            r8.translate(r2, r3)
            float r2 = r1.currentRevealBounceProgress
            int r3 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            r13 = 1065353216(0x3var_, float:1.0)
            if (r3 == 0) goto L_0x02c3
            int r3 = (r2 > r13 ? 1 : (r2 == r13 ? 0 : -1))
            if (r3 == 0) goto L_0x02c3
            org.telegram.ui.Cells.DialogCell$BounceInterpolator r3 = r1.interpolator
            float r2 = r3.getInterpolation(r2)
            float r2 = r2 + r13
            org.telegram.ui.Components.RLottieDrawable r3 = r1.translationDrawable
            int r3 = r3.getIntrinsicWidth()
            int r3 = r3 / r14
            float r3 = (float) r3
            org.telegram.ui.Components.RLottieDrawable r4 = r1.translationDrawable
            int r4 = r4.getIntrinsicHeight()
            int r4 = r4 / r14
            float r4 = (float) r4
            r8.scale(r2, r2, r3, r4)
        L_0x02c3:
            org.telegram.ui.Components.RLottieDrawable r2 = r1.translationDrawable
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r2, (int) r15, (int) r15)
            org.telegram.ui.Components.RLottieDrawable r2 = r1.translationDrawable
            r2.draw(r8)
            r25.restore()
            int r2 = r24.getMeasuredWidth()
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            float r3 = (float) r3
            r8.clipRect(r12, r9, r2, r3)
            android.text.TextPaint r2 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            float r2 = r2.measureText(r0)
            double r2 = (double) r2
            double r2 = java.lang.Math.ceil(r2)
            int r2 = (int) r2
            int r3 = r24.getMeasuredWidth()
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r18)
            int r3 = r3 - r4
            int r2 = r2 / r14
            int r3 = r3 - r2
            float r2 = (float) r3
            boolean r3 = r1.useForceThreeLines
            if (r3 != 0) goto L_0x0301
            boolean r3 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r3 == 0) goto L_0x02fe
            goto L_0x0301
        L_0x02fe:
            r3 = 1114374144(0x426CLASSNAME, float:59.0)
            goto L_0x0303
        L_0x0301:
            r3 = 1115160576(0x42780000, float:62.0)
        L_0x0303:
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            float r3 = (float) r3
            android.text.TextPaint r4 = org.telegram.ui.ActionBar.Theme.dialogs_archiveTextPaint
            r8.drawText(r0, r2, r3, r4)
            r25.restore()
        L_0x0310:
            float r0 = r1.translationX
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x031e
            r25.save()
            float r0 = r1.translationX
            r8.translate(r0, r9)
        L_0x031e:
            boolean r0 = r1.isSelected
            if (r0 == 0) goto L_0x0335
            r3 = 0
            r4 = 0
            int r0 = r24.getMeasuredWidth()
            float r5 = (float) r0
            int r0 = r24.getMeasuredHeight()
            float r6 = (float) r0
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_tabletSeletedPaint
            r2 = r25
            r2.drawRect(r3, r4, r5, r6, r7)
        L_0x0335:
            int r0 = r1.currentDialogFolderId
            java.lang.String r12 = "chats_pinnedOverlay"
            if (r0 == 0) goto L_0x0368
            boolean r0 = org.telegram.messenger.SharedConfig.archiveHidden
            if (r0 == 0) goto L_0x0345
            float r0 = r1.archiveBackgroundProgress
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x0368
        L_0x0345:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            float r3 = r1.archiveBackgroundProgress
            int r2 = org.telegram.messenger.AndroidUtilities.getOffsetColor(r15, r2, r3, r13)
            r0.setColor(r2)
            r3 = 0
            r4 = 0
            int r0 = r24.getMeasuredWidth()
            float r5 = (float) r0
            int r0 = r24.getMeasuredHeight()
            float r6 = (float) r0
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r2 = r25
            r2.drawRect(r3, r4, r5, r6, r7)
            goto L_0x038c
        L_0x0368:
            boolean r0 = r1.drawPin
            if (r0 != 0) goto L_0x0370
            boolean r0 = r1.drawPinBackground
            if (r0 == 0) goto L_0x038c
        L_0x0370:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            r0.setColor(r2)
            r3 = 0
            r4 = 0
            int r0 = r24.getMeasuredWidth()
            float r5 = (float) r0
            int r0 = r24.getMeasuredHeight()
            float r6 = (float) r0
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r2 = r25
            r2.drawRect(r3, r4, r5, r6, r7)
        L_0x038c:
            float r0 = r1.translationX
            java.lang.String r16 = "windowBackgroundWhite"
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 != 0) goto L_0x039f
            float r0 = r1.cornerProgress
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x039b
            goto L_0x039f
        L_0x039b:
            r2 = 1090519040(0x41000000, float:8.0)
            goto L_0x044d
        L_0x039f:
            r25.save()
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r0.setColor(r2)
            android.graphics.RectF r0 = r1.rect
            int r2 = r24.getMeasuredWidth()
            r3 = 1115684864(0x42800000, float:64.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            int r2 = r2 - r3
            float r2 = (float) r2
            int r3 = r24.getMeasuredWidth()
            float r3 = (float) r3
            int r4 = r24.getMeasuredHeight()
            float r4 = (float) r4
            r0.set(r2, r9, r3, r4)
            android.graphics.RectF r0 = r1.rect
            r2 = 1090519040(0x41000000, float:8.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r3 = (float) r3
            float r4 = r1.cornerProgress
            float r3 = r3 * r4
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r2 = (float) r4
            float r4 = r1.cornerProgress
            float r2 = r2 * r4
            android.graphics.Paint r4 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r8.drawRoundRect(r0, r3, r2, r4)
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x041a
            boolean r0 = org.telegram.messenger.SharedConfig.archiveHidden
            if (r0 == 0) goto L_0x03ef
            float r0 = r1.archiveBackgroundProgress
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x041a
        L_0x03ef:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            float r3 = r1.archiveBackgroundProgress
            int r2 = org.telegram.messenger.AndroidUtilities.getOffsetColor(r15, r2, r3, r13)
            r0.setColor(r2)
            android.graphics.RectF r0 = r1.rect
            r2 = 1090519040(0x41000000, float:8.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r3 = (float) r3
            float r4 = r1.cornerProgress
            float r3 = r3 * r4
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r2 = (float) r4
            float r4 = r1.cornerProgress
            float r2 = r2 * r4
            android.graphics.Paint r4 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r8.drawRoundRect(r0, r3, r2, r4)
            goto L_0x0423
        L_0x041a:
            boolean r0 = r1.drawPin
            if (r0 != 0) goto L_0x0426
            boolean r0 = r1.drawPinBackground
            if (r0 == 0) goto L_0x0423
            goto L_0x0426
        L_0x0423:
            r2 = 1090519040(0x41000000, float:8.0)
            goto L_0x044a
        L_0x0426:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r12)
            r0.setColor(r2)
            android.graphics.RectF r0 = r1.rect
            r2 = 1090519040(0x41000000, float:8.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r3 = (float) r3
            float r4 = r1.cornerProgress
            float r3 = r3 * r4
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r2)
            float r4 = (float) r4
            float r5 = r1.cornerProgress
            float r4 = r4 * r5
            android.graphics.Paint r5 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r8.drawRoundRect(r0, r3, r4, r5)
        L_0x044a:
            r25.restore()
        L_0x044d:
            float r0 = r1.translationX
            r3 = 1125515264(0x43160000, float:150.0)
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x0467
            float r0 = r1.cornerProgress
            int r4 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r4 >= 0) goto L_0x047a
            float r4 = (float) r10
            float r4 = r4 / r3
            float r0 = r0 + r4
            r1.cornerProgress = r0
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0478
            r1.cornerProgress = r13
            goto L_0x0478
        L_0x0467:
            float r0 = r1.cornerProgress
            int r4 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r4 <= 0) goto L_0x047a
            float r4 = (float) r10
            float r4 = r4 / r3
            float r0 = r0 - r4
            r1.cornerProgress = r0
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 >= 0) goto L_0x0478
            r1.cornerProgress = r9
        L_0x0478:
            r7 = 1
            goto L_0x047b
        L_0x047a:
            r7 = 0
        L_0x047b:
            boolean r0 = r1.drawNameLock
            if (r0 == 0) goto L_0x048e
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            int r4 = r1.nameLockLeft
            int r5 = r1.nameLockTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r4, (int) r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_lockDrawable
            r0.draw(r8)
            goto L_0x04c6
        L_0x048e:
            boolean r0 = r1.drawNameGroup
            if (r0 == 0) goto L_0x04a1
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            int r4 = r1.nameLockLeft
            int r5 = r1.nameLockTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r4, (int) r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_groupDrawable
            r0.draw(r8)
            goto L_0x04c6
        L_0x04a1:
            boolean r0 = r1.drawNameBroadcast
            if (r0 == 0) goto L_0x04b4
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
            int r4 = r1.nameLockLeft
            int r5 = r1.nameLockTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r4, (int) r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_broadcastDrawable
            r0.draw(r8)
            goto L_0x04c6
        L_0x04b4:
            boolean r0 = r1.drawNameBot
            if (r0 == 0) goto L_0x04c6
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            int r4 = r1.nameLockLeft
            int r5 = r1.nameLockTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r4, (int) r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_botDrawable
            r0.draw(r8)
        L_0x04c6:
            android.text.StaticLayout r0 = r1.nameLayout
            if (r0 == 0) goto L_0x053a
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x04e2
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_nameArchived"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
            goto L_0x0516
        L_0x04e2:
            org.telegram.tgnet.TLRPC$EncryptedChat r0 = r1.encryptedChat
            if (r0 != 0) goto L_0x0503
            org.telegram.ui.Cells.DialogCell$CustomDialog r0 = r1.customDialog
            if (r0 == 0) goto L_0x04ef
            int r0 = r0.type
            if (r0 != r14) goto L_0x04ef
            goto L_0x0503
        L_0x04ef:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_name"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
            goto L_0x0516
        L_0x0503:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_namePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_secretName"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
        L_0x0516:
            r25.save()
            int r0 = r1.nameLeft
            float r0 = (float) r0
            boolean r5 = r1.useForceThreeLines
            if (r5 != 0) goto L_0x0528
            boolean r5 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r5 == 0) goto L_0x0525
            goto L_0x0528
        L_0x0525:
            r5 = 1095761920(0x41500000, float:13.0)
            goto L_0x052a
        L_0x0528:
            r5 = 1092616192(0x41200000, float:10.0)
        L_0x052a:
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            r8.translate(r0, r5)
            android.text.StaticLayout r0 = r1.nameLayout
            r0.draw(r8)
            r25.restore()
        L_0x053a:
            android.text.StaticLayout r0 = r1.timeLayout
            if (r0 == 0) goto L_0x0556
            int r0 = r1.currentDialogFolderId
            if (r0 != 0) goto L_0x0556
            r25.save()
            int r0 = r1.timeLeft
            float r0 = (float) r0
            int r5 = r1.timeTop
            float r5 = (float) r5
            r8.translate(r0, r5)
            android.text.StaticLayout r0 = r1.timeLayout
            r0.draw(r8)
            r25.restore()
        L_0x0556:
            android.text.StaticLayout r0 = r1.messageNameLayout
            if (r0 == 0) goto L_0x05a4
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x056c
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.dialogs_messageNamePaint
            java.lang.String r5 = "chats_nameMessageArchived_threeLines"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r0.setColor(r5)
            goto L_0x058b
        L_0x056c:
            org.telegram.tgnet.TLRPC$DraftMessage r0 = r1.draftMessage
            if (r0 == 0) goto L_0x057e
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.dialogs_messageNamePaint
            java.lang.String r5 = "chats_draft"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r0.setColor(r5)
            goto L_0x058b
        L_0x057e:
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.dialogs_messageNamePaint
            java.lang.String r5 = "chats_nameMessage_threeLines"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r0.setColor(r5)
        L_0x058b:
            r25.save()
            int r0 = r1.messageNameLeft
            float r0 = (float) r0
            int r5 = r1.messageNameTop
            float r5 = (float) r5
            r8.translate(r0, r5)
            android.text.StaticLayout r0 = r1.messageNameLayout     // Catch:{ Exception -> 0x059d }
            r0.draw(r8)     // Catch:{ Exception -> 0x059d }
            goto L_0x05a1
        L_0x059d:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x05a1:
            r25.restore()
        L_0x05a4:
            android.text.StaticLayout r0 = r1.messageLayout
            if (r0 == 0) goto L_0x0604
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x05d8
            org.telegram.tgnet.TLRPC$Chat r0 = r1.chat
            if (r0 == 0) goto L_0x05c4
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_nameMessageArchived"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
            goto L_0x05eb
        L_0x05c4:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_messageArchived"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
            goto L_0x05eb
        L_0x05d8:
            android.text.TextPaint[] r0 = org.telegram.ui.ActionBar.Theme.dialogs_messagePaint
            int r5 = r1.paintIndex
            r6 = r0[r5]
            r0 = r0[r5]
            java.lang.String r5 = "chats_message"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r0.linkColor = r5
            r6.setColor(r5)
        L_0x05eb:
            r25.save()
            int r0 = r1.messageLeft
            float r0 = (float) r0
            int r5 = r1.messageTop
            float r5 = (float) r5
            r8.translate(r0, r5)
            android.text.StaticLayout r0 = r1.messageLayout     // Catch:{ Exception -> 0x05fd }
            r0.draw(r8)     // Catch:{ Exception -> 0x05fd }
            goto L_0x0601
        L_0x05fd:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0601:
            r25.restore()
        L_0x0604:
            int r0 = r1.currentDialogFolderId
            if (r0 != 0) goto L_0x064e
            boolean r0 = r1.drawClock
            if (r0 == 0) goto L_0x061b
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_clockDrawable
            int r5 = r1.checkDrawLeft
            int r6 = r1.checkDrawTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_clockDrawable
            r0.draw(r8)
            goto L_0x064e
        L_0x061b:
            boolean r0 = r1.drawCheck2
            if (r0 == 0) goto L_0x064e
            boolean r0 = r1.drawCheck1
            if (r0 == 0) goto L_0x0640
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_halfCheckDrawable
            int r5 = r1.halfCheckDrawLeft
            int r6 = r1.checkDrawTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_halfCheckDrawable
            r0.draw(r8)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_checkReadDrawable
            int r5 = r1.checkDrawLeft
            int r6 = r1.checkDrawTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_checkReadDrawable
            r0.draw(r8)
            goto L_0x064e
        L_0x0640:
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_checkDrawable
            int r5 = r1.checkDrawLeft
            int r6 = r1.checkDrawTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_checkDrawable
            r0.draw(r8)
        L_0x064e:
            boolean r0 = r1.dialogMuted
            if (r0 == 0) goto L_0x0686
            boolean r0 = r1.drawVerified
            if (r0 != 0) goto L_0x0686
            boolean r0 = r1.drawScam
            if (r0 != 0) goto L_0x0686
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_muteDrawable
            int r5 = r1.nameMuteLeft
            boolean r6 = r1.useForceThreeLines
            if (r6 != 0) goto L_0x066a
            boolean r6 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r6 == 0) goto L_0x0667
            goto L_0x066a
        L_0x0667:
            r6 = 1065353216(0x3var_, float:1.0)
            goto L_0x066b
        L_0x066a:
            r6 = 0
        L_0x066b:
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r5 = r5 - r6
            boolean r6 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r6 == 0) goto L_0x0677
            r6 = 1096286208(0x41580000, float:13.5)
            goto L_0x0679
        L_0x0677:
            r6 = 1099694080(0x418CLASSNAME, float:17.5)
        L_0x0679:
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_muteDrawable
            r0.draw(r8)
            goto L_0x06e9
        L_0x0686:
            boolean r0 = r1.drawVerified
            if (r0 == 0) goto L_0x06c7
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedDrawable
            int r5 = r1.nameMuteLeft
            boolean r6 = r1.useForceThreeLines
            if (r6 != 0) goto L_0x069a
            boolean r6 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r6 == 0) goto L_0x0697
            goto L_0x069a
        L_0x0697:
            r6 = 1099169792(0x41840000, float:16.5)
            goto L_0x069c
        L_0x069a:
            r6 = 1095237632(0x41480000, float:12.5)
        L_0x069c:
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedCheckDrawable
            int r5 = r1.nameMuteLeft
            boolean r6 = r1.useForceThreeLines
            if (r6 != 0) goto L_0x06b3
            boolean r6 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r6 == 0) goto L_0x06b0
            goto L_0x06b3
        L_0x06b0:
            r6 = 1099169792(0x41840000, float:16.5)
            goto L_0x06b5
        L_0x06b3:
            r6 = 1095237632(0x41480000, float:12.5)
        L_0x06b5:
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedDrawable
            r0.draw(r8)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_verifiedCheckDrawable
            r0.draw(r8)
            goto L_0x06e9
        L_0x06c7:
            boolean r0 = r1.drawScam
            if (r0 == 0) goto L_0x06e9
            org.telegram.ui.Components.ScamDrawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            int r5 = r1.nameMuteLeft
            boolean r6 = r1.useForceThreeLines
            if (r6 != 0) goto L_0x06db
            boolean r6 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r6 == 0) goto L_0x06d8
            goto L_0x06db
        L_0x06d8:
            r6 = 1097859072(0x41700000, float:15.0)
            goto L_0x06dd
        L_0x06db:
            r6 = 1094713344(0x41400000, float:12.0)
        L_0x06dd:
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r5, (int) r6)
            org.telegram.ui.Components.ScamDrawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_scamDrawable
            r0.draw(r8)
        L_0x06e9:
            boolean r0 = r1.drawReorder
            r5 = 1132396544(0x437var_, float:255.0)
            if (r0 != 0) goto L_0x06f5
            float r0 = r1.reorderIconProgress
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 == 0) goto L_0x070d
        L_0x06f5:
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_reorderDrawable
            float r6 = r1.reorderIconProgress
            float r6 = r6 * r5
            int r6 = (int) r6
            r0.setAlpha(r6)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_reorderDrawable
            int r6 = r1.pinLeft
            int r12 = r1.pinTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r6, (int) r12)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_reorderDrawable
            r0.draw(r8)
        L_0x070d:
            boolean r0 = r1.drawError
            r6 = 1085276160(0x40b00000, float:5.5)
            r12 = 1102577664(0x41b80000, float:23.0)
            r17 = 1094189056(0x41380000, float:11.5)
            if (r0 == 0) goto L_0x0766
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_errorDrawable
            float r2 = r1.reorderIconProgress
            float r2 = r13 - r2
            float r2 = r2 * r5
            int r2 = (int) r2
            r0.setAlpha(r2)
            android.graphics.RectF r0 = r1.rect
            int r2 = r1.errorLeft
            float r5 = (float) r2
            int r4 = r1.errorTop
            float r4 = (float) r4
            int r20 = org.telegram.messenger.AndroidUtilities.dp(r12)
            int r2 = r2 + r20
            float r2 = (float) r2
            int r15 = r1.errorTop
            int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
            int r15 = r15 + r12
            float r12 = (float) r15
            r0.set(r5, r4, r2, r12)
            android.graphics.RectF r0 = r1.rect
            float r2 = org.telegram.messenger.AndroidUtilities.density
            float r4 = r2 * r17
            float r2 = r2 * r17
            android.graphics.Paint r5 = org.telegram.ui.ActionBar.Theme.dialogs_errorPaint
            r8.drawRoundRect(r0, r4, r2, r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_errorDrawable
            int r2 = r1.errorLeft
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r2 = r2 + r4
            int r4 = r1.errorTop
            r5 = 1084227584(0x40a00000, float:5.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r4 = r4 + r5
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r2, (int) r4)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_errorDrawable
            r0.draw(r8)
            goto L_0x08b1
        L_0x0766:
            boolean r0 = r1.drawCount
            if (r0 != 0) goto L_0x078f
            boolean r0 = r1.drawMention
            if (r0 == 0) goto L_0x076f
            goto L_0x078f
        L_0x076f:
            boolean r0 = r1.drawPin
            if (r0 == 0) goto L_0x08b1
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedDrawable
            float r2 = r1.reorderIconProgress
            float r6 = r13 - r2
            float r6 = r6 * r5
            int r2 = (int) r6
            r0.setAlpha(r2)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedDrawable
            int r2 = r1.pinLeft
            int r4 = r1.pinTop
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r0, (int) r2, (int) r4)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedDrawable
            r0.draw(r8)
            goto L_0x08b1
        L_0x078f:
            boolean r0 = r1.drawCount
            if (r0 == 0) goto L_0x0805
            boolean r0 = r1.dialogMuted
            if (r0 != 0) goto L_0x079f
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x079c
            goto L_0x079f
        L_0x079c:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countPaint
            goto L_0x07a1
        L_0x079f:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countGrayPaint
        L_0x07a1:
            float r2 = r1.reorderIconProgress
            float r2 = r13 - r2
            float r2 = r2 * r5
            int r2 = (int) r2
            r0.setAlpha(r2)
            android.text.TextPaint r2 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            float r4 = r1.reorderIconProgress
            float r4 = r13 - r4
            float r4 = r4 * r5
            int r4 = (int) r4
            r2.setAlpha(r4)
            int r2 = r1.countLeft
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r2 = r2 - r4
            android.graphics.RectF r4 = r1.rect
            float r15 = (float) r2
            int r3 = r1.countTop
            float r3 = (float) r3
            int r9 = r1.countWidth
            int r2 = r2 + r9
            r9 = 1093664768(0x41300000, float:11.0)
            int r9 = org.telegram.messenger.AndroidUtilities.dp(r9)
            int r2 = r2 + r9
            float r2 = (float) r2
            int r9 = r1.countTop
            int r23 = org.telegram.messenger.AndroidUtilities.dp(r12)
            int r9 = r9 + r23
            float r9 = (float) r9
            r4.set(r15, r3, r2, r9)
            android.graphics.RectF r2 = r1.rect
            float r3 = org.telegram.messenger.AndroidUtilities.density
            float r4 = r3 * r17
            float r3 = r3 * r17
            r8.drawRoundRect(r2, r4, r3, r0)
            android.text.StaticLayout r0 = r1.countLayout
            if (r0 == 0) goto L_0x0805
            r25.save()
            int r0 = r1.countLeft
            float r0 = (float) r0
            int r2 = r1.countTop
            r3 = 1082130432(0x40800000, float:4.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            int r2 = r2 + r3
            float r2 = (float) r2
            r8.translate(r0, r2)
            android.text.StaticLayout r0 = r1.countLayout
            r0.draw(r8)
            r25.restore()
        L_0x0805:
            boolean r0 = r1.drawMention
            if (r0 == 0) goto L_0x08b1
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countPaint
            float r2 = r1.reorderIconProgress
            float r2 = r13 - r2
            float r2 = r2 * r5
            int r2 = (int) r2
            r0.setAlpha(r2)
            int r0 = r1.mentionLeft
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r0 = r0 - r2
            android.graphics.RectF r2 = r1.rect
            float r3 = (float) r0
            int r4 = r1.countTop
            float r4 = (float) r4
            int r6 = r1.mentionWidth
            int r0 = r0 + r6
            r6 = 1093664768(0x41300000, float:11.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r0 = r0 + r6
            float r0 = (float) r0
            int r6 = r1.countTop
            int r9 = org.telegram.messenger.AndroidUtilities.dp(r12)
            int r6 = r6 + r9
            float r6 = (float) r6
            r2.set(r3, r4, r0, r6)
            boolean r0 = r1.dialogMuted
            if (r0 == 0) goto L_0x0843
            int r0 = r1.folderId
            if (r0 == 0) goto L_0x0843
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countGrayPaint
            goto L_0x0845
        L_0x0843:
            android.graphics.Paint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countPaint
        L_0x0845:
            android.graphics.RectF r2 = r1.rect
            float r3 = org.telegram.messenger.AndroidUtilities.density
            float r4 = r3 * r17
            float r3 = r3 * r17
            r8.drawRoundRect(r2, r4, r3, r0)
            android.text.StaticLayout r0 = r1.mentionLayout
            if (r0 == 0) goto L_0x087c
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.dialogs_countTextPaint
            float r2 = r1.reorderIconProgress
            float r6 = r13 - r2
            float r6 = r6 * r5
            int r2 = (int) r6
            r0.setAlpha(r2)
            r25.save()
            int r0 = r1.mentionLeft
            float r0 = (float) r0
            int r2 = r1.countTop
            r3 = 1082130432(0x40800000, float:4.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            int r2 = r2 + r3
            float r2 = (float) r2
            r8.translate(r0, r2)
            android.text.StaticLayout r0 = r1.mentionLayout
            r0.draw(r8)
            r25.restore()
            goto L_0x08b1
        L_0x087c:
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_mentionDrawable
            float r2 = r1.reorderIconProgress
            float r6 = r13 - r2
            float r6 = r6 * r5
            int r2 = (int) r6
            r0.setAlpha(r2)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_mentionDrawable
            int r2 = r1.mentionLeft
            r3 = 1073741824(0x40000000, float:2.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            int r2 = r2 - r3
            int r3 = r1.countTop
            r4 = 1078774989(0x404ccccd, float:3.2)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            int r3 = r3 + r4
            r4 = 1098907648(0x41800000, float:16.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r5 = 1098907648(0x41800000, float:16.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            org.telegram.ui.Cells.BaseCell.setDrawableBounds(r0, r2, r3, r4, r5)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_mentionDrawable
            r0.draw(r8)
        L_0x08b1:
            boolean r0 = r1.animatingArchiveAvatar
            r9 = 1126825984(0x432a0000, float:170.0)
            if (r0 == 0) goto L_0x08d3
            r25.save()
            org.telegram.ui.Cells.DialogCell$BounceInterpolator r0 = r1.interpolator
            float r2 = r1.animatingArchiveAvatarProgress
            float r2 = r2 / r9
            float r0 = r0.getInterpolation(r2)
            float r0 = r0 + r13
            org.telegram.messenger.ImageReceiver r2 = r1.avatarImage
            float r2 = r2.getCenterX()
            org.telegram.messenger.ImageReceiver r3 = r1.avatarImage
            float r3 = r3.getCenterY()
            r8.scale(r0, r0, r2, r3)
        L_0x08d3:
            int r0 = r1.currentDialogFolderId
            if (r0 == 0) goto L_0x08e1
            org.telegram.ui.Components.PullForegroundDrawable r0 = r1.archivedChatsDrawable
            if (r0 == 0) goto L_0x08e1
            boolean r0 = r0.isDraw()
            if (r0 != 0) goto L_0x08e6
        L_0x08e1:
            org.telegram.messenger.ImageReceiver r0 = r1.avatarImage
            r0.draw(r8)
        L_0x08e6:
            boolean r0 = r1.hasMessageThumb
            if (r0 == 0) goto L_0x091d
            org.telegram.messenger.ImageReceiver r0 = r1.thumbImage
            r0.draw(r8)
            boolean r0 = r1.drawPlay
            if (r0 == 0) goto L_0x091d
            org.telegram.messenger.ImageReceiver r0 = r1.thumbImage
            float r0 = r0.getCenterX()
            android.graphics.drawable.Drawable r2 = org.telegram.ui.ActionBar.Theme.dialogs_playDrawable
            int r2 = r2.getIntrinsicWidth()
            int r2 = r2 / r14
            float r2 = (float) r2
            float r0 = r0 - r2
            int r0 = (int) r0
            org.telegram.messenger.ImageReceiver r2 = r1.thumbImage
            float r2 = r2.getCenterY()
            android.graphics.drawable.Drawable r3 = org.telegram.ui.ActionBar.Theme.dialogs_playDrawable
            int r3 = r3.getIntrinsicHeight()
            int r3 = r3 / r14
            float r3 = (float) r3
            float r2 = r2 - r3
            int r2 = (int) r2
            android.graphics.drawable.Drawable r3 = org.telegram.ui.ActionBar.Theme.dialogs_playDrawable
            org.telegram.ui.Cells.BaseCell.setDrawableBounds((android.graphics.drawable.Drawable) r3, (int) r0, (int) r2)
            android.graphics.drawable.Drawable r0 = org.telegram.ui.ActionBar.Theme.dialogs_playDrawable
            r0.draw(r8)
        L_0x091d:
            boolean r0 = r1.animatingArchiveAvatar
            if (r0 == 0) goto L_0x0924
            r25.restore()
        L_0x0924:
            org.telegram.tgnet.TLRPC$User r0 = r1.user
            if (r0 == 0) goto L_0x0a25
            boolean r2 = r1.isDialogCell
            if (r2 == 0) goto L_0x0a25
            int r2 = r1.currentDialogFolderId
            if (r2 != 0) goto L_0x0a25
            boolean r0 = org.telegram.messenger.MessagesController.isSupportUser(r0)
            if (r0 != 0) goto L_0x0a25
            org.telegram.tgnet.TLRPC$User r0 = r1.user
            boolean r2 = r0.bot
            if (r2 != 0) goto L_0x0a25
            boolean r2 = r0.self
            if (r2 != 0) goto L_0x096a
            org.telegram.tgnet.TLRPC$UserStatus r0 = r0.status
            if (r0 == 0) goto L_0x0952
            int r0 = r0.expires
            int r2 = r1.currentAccount
            org.telegram.tgnet.ConnectionsManager r2 = org.telegram.tgnet.ConnectionsManager.getInstance(r2)
            int r2 = r2.getCurrentTime()
            if (r0 > r2) goto L_0x0968
        L_0x0952:
            int r0 = r1.currentAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, java.lang.Integer> r0 = r0.onlinePrivacy
            org.telegram.tgnet.TLRPC$User r2 = r1.user
            int r2 = r2.id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            boolean r0 = r0.containsKey(r2)
            if (r0 == 0) goto L_0x096a
        L_0x0968:
            r0 = 1
            goto L_0x096b
        L_0x096a:
            r0 = 0
        L_0x096b:
            if (r0 != 0) goto L_0x0974
            float r2 = r1.onlineProgress
            r3 = 0
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 == 0) goto L_0x0a25
        L_0x0974:
            org.telegram.messenger.ImageReceiver r2 = r1.avatarImage
            float r2 = r2.getImageY2()
            boolean r3 = r1.useForceThreeLines
            r4 = 1086324736(0x40CLASSNAME, float:6.0)
            if (r3 != 0) goto L_0x0988
            boolean r3 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r3 == 0) goto L_0x0985
            goto L_0x0988
        L_0x0985:
            r18 = 1090519040(0x41000000, float:8.0)
            goto L_0x098a
        L_0x0988:
            r18 = 1086324736(0x40CLASSNAME, float:6.0)
        L_0x098a:
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r18)
            float r3 = (float) r3
            float r2 = r2 - r3
            int r2 = (int) r2
            boolean r3 = org.telegram.messenger.LocaleController.isRTL
            if (r3 == 0) goto L_0x09ac
            org.telegram.messenger.ImageReceiver r3 = r1.avatarImage
            float r3 = r3.getImageX()
            boolean r5 = r1.useForceThreeLines
            if (r5 != 0) goto L_0x09a3
            boolean r5 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r5 == 0) goto L_0x09a5
        L_0x09a3:
            r4 = 1092616192(0x41200000, float:10.0)
        L_0x09a5:
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r4 = (float) r4
            float r3 = r3 + r4
            goto L_0x09c2
        L_0x09ac:
            org.telegram.messenger.ImageReceiver r3 = r1.avatarImage
            float r3 = r3.getImageX2()
            boolean r5 = r1.useForceThreeLines
            if (r5 != 0) goto L_0x09ba
            boolean r5 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
            if (r5 == 0) goto L_0x09bc
        L_0x09ba:
            r4 = 1092616192(0x41200000, float:10.0)
        L_0x09bc:
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r4 = (float) r4
            float r3 = r3 - r4
        L_0x09c2:
            int r3 = (int) r3
            android.graphics.Paint r4 = org.telegram.ui.ActionBar.Theme.dialogs_onlineCirclePaint
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r4.setColor(r5)
            float r3 = (float) r3
            float r2 = (float) r2
            r4 = 1088421888(0x40e00000, float:7.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r4 = (float) r4
            float r5 = r1.onlineProgress
            float r4 = r4 * r5
            android.graphics.Paint r5 = org.telegram.ui.ActionBar.Theme.dialogs_onlineCirclePaint
            r8.drawCircle(r3, r2, r4, r5)
            android.graphics.Paint r4 = org.telegram.ui.ActionBar.Theme.dialogs_onlineCirclePaint
            java.lang.String r5 = "chats_onlineCircle"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r4.setColor(r5)
            r4 = 1084227584(0x40a00000, float:5.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            float r4 = (float) r4
            float r5 = r1.onlineProgress
            float r4 = r4 * r5
            android.graphics.Paint r5 = org.telegram.ui.ActionBar.Theme.dialogs_onlineCirclePaint
            r8.drawCircle(r3, r2, r4, r5)
            if (r0 == 0) goto L_0x0a0f
            float r0 = r1.onlineProgress
            int r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r2 >= 0) goto L_0x0a25
            float r2 = (float) r10
            r3 = 1125515264(0x43160000, float:150.0)
            float r2 = r2 / r3
            float r0 = r0 + r2
            r1.onlineProgress = r0
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0a23
            r1.onlineProgress = r13
            goto L_0x0a23
        L_0x0a0f:
            float r0 = r1.onlineProgress
            r2 = 0
            int r3 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r3 <= 0) goto L_0x0a25
            float r3 = (float) r10
            r4 = 1125515264(0x43160000, float:150.0)
            float r3 = r3 / r4
            float r0 = r0 - r3
            r1.onlineProgress = r0
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 >= 0) goto L_0x0a23
            r1.onlineProgress = r2
        L_0x0a23:
            r0 = 1
            goto L_0x0a26
        L_0x0a25:
            r0 = r7
        L_0x0a26:
            float r2 = r1.translationX
            r3 = 0
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 == 0) goto L_0x0a30
            r25.restore()
        L_0x0a30:
            int r2 = r1.currentDialogFolderId
            if (r2 == 0) goto L_0x0a41
            float r2 = r1.translationX
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 != 0) goto L_0x0a41
            org.telegram.ui.Components.PullForegroundDrawable r2 = r1.archivedChatsDrawable
            if (r2 == 0) goto L_0x0a41
            r2.draw(r8)
        L_0x0a41:
            boolean r2 = r1.useSeparator
            if (r2 == 0) goto L_0x0aa2
            boolean r2 = r1.fullSeparator
            if (r2 != 0) goto L_0x0a65
            int r2 = r1.currentDialogFolderId
            if (r2 == 0) goto L_0x0a55
            boolean r2 = r1.archiveHidden
            if (r2 == 0) goto L_0x0a55
            boolean r2 = r1.fullSeparator2
            if (r2 == 0) goto L_0x0a65
        L_0x0a55:
            boolean r2 = r1.fullSeparator2
            if (r2 == 0) goto L_0x0a5e
            boolean r2 = r1.archiveHidden
            if (r2 != 0) goto L_0x0a5e
            goto L_0x0a65
        L_0x0a5e:
            r2 = 1116733440(0x42900000, float:72.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            goto L_0x0a66
        L_0x0a65:
            r2 = 0
        L_0x0a66:
            boolean r3 = org.telegram.messenger.LocaleController.isRTL
            if (r3 == 0) goto L_0x0a87
            r3 = 0
            int r4 = r24.getMeasuredHeight()
            int r4 = r4 + -1
            float r4 = (float) r4
            int r5 = r24.getMeasuredWidth()
            int r5 = r5 - r2
            float r5 = (float) r5
            int r2 = r24.getMeasuredHeight()
            int r2 = r2 + -1
            float r6 = (float) r2
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dividerPaint
            r2 = r25
            r2.drawLine(r3, r4, r5, r6, r7)
            goto L_0x0aa2
        L_0x0a87:
            float r3 = (float) r2
            int r2 = r24.getMeasuredHeight()
            int r2 = r2 + -1
            float r4 = (float) r2
            int r2 = r24.getMeasuredWidth()
            float r5 = (float) r2
            int r2 = r24.getMeasuredHeight()
            int r2 = r2 + -1
            float r6 = (float) r2
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dividerPaint
            r2 = r25
            r2.drawLine(r3, r4, r5, r6, r7)
        L_0x0aa2:
            float r2 = r1.clipProgress
            r3 = 0
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 == 0) goto L_0x0af0
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 24
            if (r2 == r3) goto L_0x0ab3
            r25.restore()
            goto L_0x0af0
        L_0x0ab3:
            android.graphics.Paint r2 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r16)
            r2.setColor(r3)
            r3 = 0
            r4 = 0
            int r2 = r24.getMeasuredWidth()
            float r5 = (float) r2
            int r2 = r1.topClip
            float r2 = (float) r2
            float r6 = r1.clipProgress
            float r6 = r6 * r2
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r2 = r25
            r2.drawRect(r3, r4, r5, r6, r7)
            int r2 = r24.getMeasuredHeight()
            int r4 = r1.bottomClip
            float r4 = (float) r4
            float r5 = r1.clipProgress
            float r4 = r4 * r5
            int r4 = (int) r4
            int r2 = r2 - r4
            float r4 = (float) r2
            int r2 = r24.getMeasuredWidth()
            float r5 = (float) r2
            int r2 = r24.getMeasuredHeight()
            float r6 = (float) r2
            android.graphics.Paint r7 = org.telegram.ui.ActionBar.Theme.dialogs_pinnedPaint
            r2 = r25
            r2.drawRect(r3, r4, r5, r6, r7)
        L_0x0af0:
            boolean r2 = r1.drawReorder
            if (r2 != 0) goto L_0x0afe
            float r2 = r1.reorderIconProgress
            r3 = 0
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 == 0) goto L_0x0afc
            goto L_0x0afe
        L_0x0afc:
            r3 = 0
            goto L_0x0b29
        L_0x0afe:
            boolean r2 = r1.drawReorder
            if (r2 == 0) goto L_0x0b15
            float r2 = r1.reorderIconProgress
            int r3 = (r2 > r13 ? 1 : (r2 == r13 ? 0 : -1))
            if (r3 >= 0) goto L_0x0afc
            float r0 = (float) r10
            float r0 = r0 / r9
            float r2 = r2 + r0
            r1.reorderIconProgress = r2
            int r0 = (r2 > r13 ? 1 : (r2 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0b13
            r1.reorderIconProgress = r13
        L_0x0b13:
            r3 = 0
            goto L_0x0b27
        L_0x0b15:
            float r2 = r1.reorderIconProgress
            r3 = 0
            int r4 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r4 <= 0) goto L_0x0b29
            float r0 = (float) r10
            float r0 = r0 / r9
            float r2 = r2 - r0
            r1.reorderIconProgress = r2
            int r0 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r0 >= 0) goto L_0x0b27
            r1.reorderIconProgress = r3
        L_0x0b27:
            r7 = 1
            goto L_0x0b2a
        L_0x0b29:
            r7 = r0
        L_0x0b2a:
            boolean r0 = r1.archiveHidden
            if (r0 == 0) goto L_0x0b57
            float r0 = r1.archiveBackgroundProgress
            int r2 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r2 <= 0) goto L_0x0b80
            float r2 = (float) r10
            r4 = 1130758144(0x43660000, float:230.0)
            float r2 = r2 / r4
            float r0 = r0 - r2
            r1.archiveBackgroundProgress = r0
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 >= 0) goto L_0x0b41
            r1.archiveBackgroundProgress = r3
        L_0x0b41:
            org.telegram.ui.Components.AvatarDrawable r0 = r1.avatarDrawable
            int r0 = r0.getAvatarType()
            if (r0 != r14) goto L_0x0b7f
            org.telegram.ui.Components.AvatarDrawable r0 = r1.avatarDrawable
            org.telegram.ui.Components.CubicBezierInterpolator r2 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_OUT_QUINT
            float r3 = r1.archiveBackgroundProgress
            float r2 = r2.getInterpolation(r3)
            r0.setArchivedAvatarHiddenProgress(r2)
            goto L_0x0b7f
        L_0x0b57:
            float r0 = r1.archiveBackgroundProgress
            int r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r2 >= 0) goto L_0x0b80
            float r2 = (float) r10
            r3 = 1130758144(0x43660000, float:230.0)
            float r2 = r2 / r3
            float r0 = r0 + r2
            r1.archiveBackgroundProgress = r0
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0b6a
            r1.archiveBackgroundProgress = r13
        L_0x0b6a:
            org.telegram.ui.Components.AvatarDrawable r0 = r1.avatarDrawable
            int r0 = r0.getAvatarType()
            if (r0 != r14) goto L_0x0b7f
            org.telegram.ui.Components.AvatarDrawable r0 = r1.avatarDrawable
            org.telegram.ui.Components.CubicBezierInterpolator r2 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_OUT_QUINT
            float r3 = r1.archiveBackgroundProgress
            float r2 = r2.getInterpolation(r3)
            r0.setArchivedAvatarHiddenProgress(r2)
        L_0x0b7f:
            r7 = 1
        L_0x0b80:
            boolean r0 = r1.animatingArchiveAvatar
            if (r0 == 0) goto L_0x0b94
            float r0 = r1.animatingArchiveAvatarProgress
            float r2 = (float) r10
            float r0 = r0 + r2
            r1.animatingArchiveAvatarProgress = r0
            int r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1))
            if (r0 < 0) goto L_0x0b93
            r1.animatingArchiveAvatarProgress = r9
            r2 = 0
            r1.animatingArchiveAvatar = r2
        L_0x0b93:
            r7 = 1
        L_0x0b94:
            boolean r0 = r1.drawRevealBackground
            if (r0 == 0) goto L_0x0bbe
            float r0 = r1.currentRevealBounceProgress
            int r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r2 >= 0) goto L_0x0baa
            float r2 = (float) r10
            float r2 = r2 / r9
            float r0 = r0 + r2
            r1.currentRevealBounceProgress = r0
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0baa
            r1.currentRevealBounceProgress = r13
            r7 = 1
        L_0x0baa:
            float r0 = r1.currentRevealProgress
            int r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r2 >= 0) goto L_0x0bdc
            float r2 = (float) r10
            r3 = 1133903872(0x43960000, float:300.0)
            float r2 = r2 / r3
            float r0 = r0 + r2
            r1.currentRevealProgress = r0
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r0 <= 0) goto L_0x0bdb
            r1.currentRevealProgress = r13
            goto L_0x0bdb
        L_0x0bbe:
            float r0 = r1.currentRevealBounceProgress
            int r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            r2 = 0
            if (r0 != 0) goto L_0x0bc8
            r1.currentRevealBounceProgress = r2
            r7 = 1
        L_0x0bc8:
            float r0 = r1.currentRevealProgress
            int r3 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r3 <= 0) goto L_0x0bdc
            float r3 = (float) r10
            r4 = 1133903872(0x43960000, float:300.0)
            float r3 = r3 / r4
            float r0 = r0 - r3
            r1.currentRevealProgress = r0
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 >= 0) goto L_0x0bdb
            r1.currentRevealProgress = r2
        L_0x0bdb:
            r7 = 1
        L_0x0bdc:
            if (r7 == 0) goto L_0x0be1
            r24.invalidate()
        L_0x0be1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.DialogCell.onDraw(android.graphics.Canvas):void");
    }

    public void startOutAnimation() {
        PullForegroundDrawable pullForegroundDrawable = this.archivedChatsDrawable;
        if (pullForegroundDrawable != null) {
            pullForegroundDrawable.outCy = this.avatarImage.getCenterY();
            this.archivedChatsDrawable.outCx = this.avatarImage.getCenterX();
            this.archivedChatsDrawable.outRadius = this.avatarImage.getImageWidth() / 2.0f;
            this.archivedChatsDrawable.outImageSize = (float) this.avatarImage.getBitmapWidth();
            this.archivedChatsDrawable.startOutAnimation();
        }
    }

    public void onReorderStateChanged(boolean z, boolean z2) {
        if ((this.drawPin || !z) && this.drawReorder != z) {
            this.drawReorder = z;
            float f = 1.0f;
            if (z2) {
                if (z) {
                    f = 0.0f;
                }
                this.reorderIconProgress = f;
            } else {
                if (!z) {
                    f = 0.0f;
                }
                this.reorderIconProgress = f;
            }
            invalidate();
        } else if (!this.drawPin) {
            this.drawReorder = false;
        }
    }

    public void setSliding(boolean z) {
        this.isSliding = z;
    }

    public void invalidateDrawable(Drawable drawable) {
        if (drawable == this.translationDrawable || drawable == Theme.dialogs_archiveAvatarDrawable) {
            invalidate(drawable.getBounds());
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(16);
        accessibilityNodeInfo.addAction(32);
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        TLRPC$User user2;
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        StringBuilder sb = new StringBuilder();
        if (this.currentDialogFolderId == 1) {
            sb.append(LocaleController.getString("ArchivedChats", NUM));
            sb.append(". ");
        } else {
            if (this.encryptedChat != null) {
                sb.append(LocaleController.getString("AccDescrSecretChat", NUM));
                sb.append(". ");
            }
            TLRPC$User tLRPC$User = this.user;
            if (tLRPC$User != null) {
                if (tLRPC$User.bot) {
                    sb.append(LocaleController.getString("Bot", NUM));
                    sb.append(". ");
                }
                TLRPC$User tLRPC$User2 = this.user;
                if (tLRPC$User2.self) {
                    sb.append(LocaleController.getString("SavedMessages", NUM));
                } else {
                    sb.append(ContactsController.formatName(tLRPC$User2.first_name, tLRPC$User2.last_name));
                }
                sb.append(". ");
            } else {
                TLRPC$Chat tLRPC$Chat = this.chat;
                if (tLRPC$Chat != null) {
                    if (tLRPC$Chat.broadcast) {
                        sb.append(LocaleController.getString("AccDescrChannel", NUM));
                    } else {
                        sb.append(LocaleController.getString("AccDescrGroup", NUM));
                    }
                    sb.append(". ");
                    sb.append(this.chat.title);
                    sb.append(". ");
                }
            }
        }
        int i = this.unreadCount;
        if (i > 0) {
            sb.append(LocaleController.formatPluralString("NewMessages", i));
            sb.append(". ");
        }
        MessageObject messageObject = this.message;
        if (messageObject == null || this.currentDialogFolderId != 0) {
            accessibilityEvent.setContentDescription(sb.toString());
            return;
        }
        int i2 = this.lastMessageDate;
        if (i2 == 0 && messageObject != null) {
            i2 = messageObject.messageOwner.date;
        }
        String formatDateAudio = LocaleController.formatDateAudio((long) i2, true);
        if (this.message.isOut()) {
            sb.append(LocaleController.formatString("AccDescrSentDate", NUM, formatDateAudio));
        } else {
            sb.append(LocaleController.formatString("AccDescrReceivedDate", NUM, formatDateAudio));
        }
        sb.append(". ");
        if (this.chat != null && !this.message.isOut() && this.message.isFromUser() && this.message.messageOwner.action == null && (user2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.message.messageOwner.from_id))) != null) {
            sb.append(ContactsController.formatName(user2.first_name, user2.last_name));
            sb.append(". ");
        }
        if (this.encryptedChat == null) {
            sb.append(this.message.messageText);
            if (!this.message.isMediaEmpty() && !TextUtils.isEmpty(this.message.caption)) {
                sb.append(". ");
                sb.append(this.message.caption);
            }
        }
        accessibilityEvent.setContentDescription(sb.toString());
    }

    public void setClipProgress(float f) {
        this.clipProgress = f;
        invalidate();
    }

    public float getClipProgress() {
        return this.clipProgress;
    }

    public void setTopClip(int i) {
        this.topClip = i;
    }

    public void setBottomClip(int i) {
        this.bottomClip = i;
    }

    public void setArchivedPullAnimation(PullForegroundDrawable pullForegroundDrawable) {
        this.archivedChatsDrawable = pullForegroundDrawable;
    }
}
