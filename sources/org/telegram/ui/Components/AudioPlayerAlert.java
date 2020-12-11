package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$DocumentAttribute;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$ReplyMarkup;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_photoSize;
import org.telegram.tgnet.TLRPC$TL_photoSizeProgressive;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AudioPlayerCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;

public class AudioPlayerAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate, DownloadController.FileDownloadProgressListener {
    private int TAG;
    /* access modifiers changed from: private */
    public ActionBar actionBar;
    /* access modifiers changed from: private */
    public AnimatorSet actionBarAnimation;
    /* access modifiers changed from: private */
    public View actionBarShadow;
    private ClippingTextViewSwitcher authorTextView;
    /* access modifiers changed from: private */
    public BackupImageView bigAlbumConver;
    /* access modifiers changed from: private */
    public boolean blurredAnimationInProgress;
    /* access modifiers changed from: private */
    public FrameLayout blurredView;
    /* access modifiers changed from: private */
    public View[] buttons = new View[5];
    private CoverContainer coverContainer;
    private boolean currentAudioFinishedLoading;
    private String currentFile;
    /* access modifiers changed from: private */
    public boolean draggingSeekBar;
    /* access modifiers changed from: private */
    public TextView durationTextView;
    private ImageView emptyImageView;
    /* access modifiers changed from: private */
    public TextView emptySubtitleTextView;
    private TextView emptyTitleTextView;
    private LinearLayout emptyView;
    /* access modifiers changed from: private */
    public boolean inFullSize;
    private long lastBufferedPositionCheck;
    /* access modifiers changed from: private */
    public int lastDuration;
    private MessageObject lastMessageObject;
    /* access modifiers changed from: private */
    public int lastTime;
    /* access modifiers changed from: private */
    public LinearLayoutManager layoutManager;
    /* access modifiers changed from: private */
    public ListAdapter listAdapter;
    /* access modifiers changed from: private */
    public RecyclerListView listView;
    private RLottieImageView nextButton;
    private ActionBarMenuItem optionsButton;
    private LaunchActivity parentActivity;
    private ImageView playButton;
    private PlayPauseDrawable playPauseDrawable;
    /* access modifiers changed from: private */
    public ImageView playbackSpeedButton;
    /* access modifiers changed from: private */
    public FrameLayout playerLayout;
    /* access modifiers changed from: private */
    public View playerShadow;
    /* access modifiers changed from: private */
    public ArrayList<MessageObject> playlist;
    private RLottieImageView prevButton;
    private LineProgressView progressView;
    private ActionBarMenuItem repeatButton;
    private ActionBarMenuSubItem repeatListItem;
    private ActionBarMenuSubItem repeatSongItem;
    private ActionBarMenuSubItem reverseOrderItem;
    /* access modifiers changed from: private */
    public int scrollOffsetY = Integer.MAX_VALUE;
    /* access modifiers changed from: private */
    public boolean scrollToSong = true;
    private ActionBarMenuItem searchItem;
    /* access modifiers changed from: private */
    public int searchOpenOffset;
    /* access modifiers changed from: private */
    public int searchOpenPosition = -1;
    /* access modifiers changed from: private */
    public boolean searchWas;
    /* access modifiers changed from: private */
    public boolean searching;
    private SeekBarView seekBarView;
    private ActionBarMenuSubItem shuffleListItem;
    private SimpleTextView timeTextView;
    private ClippingTextViewSwitcher titleTextView;

    static /* synthetic */ boolean lambda$new$7(View view, MotionEvent motionEvent) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean canDismissWithSwipe() {
        return false;
    }

    public void onFailedDownload(String str, boolean z) {
    }

    public void onProgressUpload(String str, long j, long j2, boolean z) {
    }

    public void onSuccessDownload(String str) {
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AudioPlayerAlert(android.content.Context r28) {
        /*
            r27 = this;
            r0 = r27
            r1 = r28
            r2 = 1
            r0.<init>(r1, r2)
            r3 = 5
            android.view.View[] r4 = new android.view.View[r3]
            r0.buttons = r4
            r0.scrollToSong = r2
            r4 = -1
            r0.searchOpenPosition = r4
            r5 = 2147483647(0x7fffffff, float:NaN)
            r0.scrollOffsetY = r5
            org.telegram.messenger.MediaController r5 = org.telegram.messenger.MediaController.getInstance()
            org.telegram.messenger.MessageObject r5 = r5.getPlayingMessageObject()
            if (r5 == 0) goto L_0x0026
            int r6 = r5.currentAccount
            r0.currentAccount = r6
            goto L_0x002a
        L_0x0026:
            int r6 = org.telegram.messenger.UserConfig.selectedAccount
            r0.currentAccount = r6
        L_0x002a:
            r6 = r1
            org.telegram.ui.LaunchActivity r6 = (org.telegram.ui.LaunchActivity) r6
            r0.parentActivity = r6
            int r6 = r0.currentAccount
            org.telegram.messenger.DownloadController r6 = org.telegram.messenger.DownloadController.getInstance(r6)
            int r6 = r6.generateObserverTag()
            r0.TAG = r6
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.messagePlayingDidReset
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.messagePlayingPlayStateChanged
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.messagePlayingDidStart
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.messagePlayingProgressDidChanged
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.fileDidLoad
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.FileLoadProgressChanged
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.musicDidLoad
            r6.addObserver(r0, r7)
            int r6 = r0.currentAccount
            org.telegram.messenger.NotificationCenter r6 = org.telegram.messenger.NotificationCenter.getInstance(r6)
            int r7 = org.telegram.messenger.NotificationCenter.moreMusicDidLoad
            r6.addObserver(r0, r7)
            org.telegram.ui.Components.AudioPlayerAlert$1 r6 = new org.telegram.ui.Components.AudioPlayerAlert$1
            r6.<init>(r1)
            r0.containerView = r6
            r7 = 0
            r6.setWillNotDraw(r7)
            android.view.ViewGroup r6 = r0.containerView
            int r8 = r0.backgroundPaddingLeft
            r6.setPadding(r8, r7, r8, r7)
            org.telegram.ui.Components.AudioPlayerAlert$2 r6 = new org.telegram.ui.Components.AudioPlayerAlert$2
            r6.<init>(r1)
            r0.actionBar = r6
            java.lang.String r8 = "player_actionBar"
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r6.setBackgroundColor(r8)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            r8 = 2131165466(0x7var_a, float:1.794515E38)
            r6.setBackButtonImage(r8)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r8 = "player_actionBarTitle"
            int r9 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r6.setItemsColor(r9, r7)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r9 = "player_actionBarSelector"
            int r9 = org.telegram.ui.ActionBar.Theme.getColor(r9)
            r6.setItemsBackgroundColor(r9, r7)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            int r9 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r6.setTitleColor(r9)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            r9 = 2131624366(0x7f0e01ae, float:1.887591E38)
            java.lang.String r10 = "AttachMusic"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r6.setTitle(r9)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r9 = "player_actionBarSubtitle"
            int r9 = org.telegram.ui.ActionBar.Theme.getColor(r9)
            r6.setSubtitleColor(r9)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            r6.setOccupyStatusBar(r7)
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            r9 = 0
            r6.setAlpha(r9)
            if (r5 == 0) goto L_0x017f
            org.telegram.messenger.MediaController r6 = org.telegram.messenger.MediaController.getInstance()
            boolean r6 = r6.currentPlaylistIsGlobalSearch()
            if (r6 != 0) goto L_0x017f
            long r5 = r5.getDialogId()
            int r10 = (int) r5
            r11 = 32
            long r5 = r5 >> r11
            int r6 = (int) r5
            if (r10 == 0) goto L_0x0150
            if (r10 <= 0) goto L_0x0137
            int r5 = r0.currentAccount
            org.telegram.messenger.MessagesController r5 = org.telegram.messenger.MessagesController.getInstance(r5)
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)
            org.telegram.tgnet.TLRPC$User r5 = r5.getUser(r6)
            if (r5 == 0) goto L_0x017f
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r10 = r5.first_name
            java.lang.String r5 = r5.last_name
            java.lang.String r5 = org.telegram.messenger.ContactsController.formatName(r10, r5)
            r6.setTitle(r5)
            goto L_0x017f
        L_0x0137:
            int r5 = r0.currentAccount
            org.telegram.messenger.MessagesController r5 = org.telegram.messenger.MessagesController.getInstance(r5)
            int r6 = -r10
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$Chat r5 = r5.getChat(r6)
            if (r5 == 0) goto L_0x017f
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r5 = r5.title
            r6.setTitle(r5)
            goto L_0x017f
        L_0x0150:
            int r5 = r0.currentAccount
            org.telegram.messenger.MessagesController r5 = org.telegram.messenger.MessagesController.getInstance(r5)
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$EncryptedChat r5 = r5.getEncryptedChat(r6)
            if (r5 == 0) goto L_0x017f
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            int r5 = r5.user_id
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            org.telegram.tgnet.TLRPC$User r5 = r6.getUser(r5)
            if (r5 == 0) goto L_0x017f
            org.telegram.ui.ActionBar.ActionBar r6 = r0.actionBar
            java.lang.String r10 = r5.first_name
            java.lang.String r5 = r5.last_name
            java.lang.String r5 = org.telegram.messenger.ContactsController.formatName(r10, r5)
            r6.setTitle(r5)
        L_0x017f:
            org.telegram.ui.ActionBar.ActionBar r5 = r0.actionBar
            org.telegram.ui.ActionBar.ActionBarMenu r5 = r5.createMenu()
            r6 = 2131165476(0x7var_, float:1.794517E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r5 = r5.addItem((int) r7, (int) r6)
            r5.setIsSearchField(r2)
            org.telegram.ui.Components.AudioPlayerAlert$3 r6 = new org.telegram.ui.Components.AudioPlayerAlert$3
            r6.<init>()
            r5.setActionBarMenuItemSearchListener(r6)
            r0.searchItem = r5
            java.lang.String r6 = "Search"
            r10 = 2131626999(0x7f0e0bf7, float:1.888125E38)
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r6, r10)
            r5.setContentDescription(r11)
            org.telegram.ui.ActionBar.ActionBarMenuItem r5 = r0.searchItem
            org.telegram.ui.Components.EditTextBoldCursor r5 = r5.getSearchField()
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r6, r10)
            r5.setHint(r6)
            int r6 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r5.setTextColor(r6)
            java.lang.String r6 = "player_time"
            int r10 = org.telegram.ui.ActionBar.Theme.getColor(r6)
            r5.setHintTextColor(r10)
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r5.setCursorColor(r8)
            org.telegram.ui.ActionBar.ActionBar r5 = r0.actionBar
            org.telegram.ui.Components.AudioPlayerAlert$4 r8 = new org.telegram.ui.Components.AudioPlayerAlert$4
            r8.<init>()
            r5.setActionBarMenuOnItemClick(r8)
            android.view.View r5 = new android.view.View
            r5.<init>(r1)
            r0.actionBarShadow = r5
            r5.setAlpha(r9)
            android.view.View r5 = r0.actionBarShadow
            r8 = 2131165463(0x7var_, float:1.7945144E38)
            r5.setBackgroundResource(r8)
            android.view.View r5 = new android.view.View
            r5.<init>(r1)
            r0.playerShadow = r5
            java.lang.String r8 = "dialogShadowLine"
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r5.setBackgroundColor(r8)
            org.telegram.ui.Components.AudioPlayerAlert$5 r5 = new org.telegram.ui.Components.AudioPlayerAlert$5
            r5.<init>(r1)
            r0.playerLayout = r5
            org.telegram.ui.Components.AudioPlayerAlert$6 r5 = new org.telegram.ui.Components.AudioPlayerAlert$6
            r5.<init>(r1)
            r0.coverContainer = r5
            android.widget.FrameLayout r8 = r0.playerLayout
            r10 = 44
            r11 = 1110441984(0x42300000, float:44.0)
            r12 = 53
            r13 = 0
            r14 = 1101004800(0x41a00000, float:20.0)
            r15 = 1101004800(0x41a00000, float:20.0)
            r16 = 0
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r10, r11, r12, r13, r14, r15, r16)
            r8.addView(r5, r10)
            org.telegram.ui.Components.AudioPlayerAlert$7 r5 = new org.telegram.ui.Components.AudioPlayerAlert$7
            r5.<init>(r0, r1, r1)
            r0.titleTextView = r5
            android.widget.FrameLayout r8 = r0.playerLayout
            r10 = -1
            r11 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r12 = 51
            r13 = 1101004800(0x41a00000, float:20.0)
            r15 = 1116733440(0x42900000, float:72.0)
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r10, r11, r12, r13, r14, r15, r16)
            r8.addView(r5, r10)
            org.telegram.ui.Components.AudioPlayerAlert$8 r5 = new org.telegram.ui.Components.AudioPlayerAlert$8
            r5.<init>(r0, r1, r1)
            r0.authorTextView = r5
            android.widget.FrameLayout r8 = r0.playerLayout
            r10 = -1
            r14 = 1111228416(0x423CLASSNAME, float:47.0)
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r10, r11, r12, r13, r14, r15, r16)
            r8.addView(r5, r10)
            org.telegram.ui.Components.SeekBarView r5 = new org.telegram.ui.Components.SeekBarView
            r5.<init>(r1)
            r0.seekBarView = r5
            org.telegram.ui.Components.AudioPlayerAlert$9 r8 = new org.telegram.ui.Components.AudioPlayerAlert$9
            r8.<init>()
            r5.setDelegate(r8)
            org.telegram.ui.Components.SeekBarView r5 = r0.seekBarView
            r5.setReportChanges(r2)
            android.widget.FrameLayout r5 = r0.playerLayout
            org.telegram.ui.Components.SeekBarView r8 = r0.seekBarView
            r10 = -1
            r11 = 1108869120(0x42180000, float:38.0)
            r13 = 1084227584(0x40a00000, float:5.0)
            r14 = 1116471296(0x428CLASSNAME, float:70.0)
            r15 = 1084227584(0x40a00000, float:5.0)
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r10, r11, r12, r13, r14, r15, r16)
            r5.addView(r8, r10)
            org.telegram.ui.Components.LineProgressView r5 = new org.telegram.ui.Components.LineProgressView
            r5.<init>(r1)
            r0.progressView = r5
            r8 = 4
            r5.setVisibility(r8)
            org.telegram.ui.Components.LineProgressView r5 = r0.progressView
            java.lang.String r10 = "player_progressBackground"
            int r10 = org.telegram.ui.ActionBar.Theme.getColor(r10)
            r5.setBackgroundColor(r10)
            org.telegram.ui.Components.LineProgressView r5 = r0.progressView
            java.lang.String r10 = "player_progress"
            int r10 = org.telegram.ui.ActionBar.Theme.getColor(r10)
            r5.setProgressColor(r10)
            android.widget.FrameLayout r5 = r0.playerLayout
            org.telegram.ui.Components.LineProgressView r10 = r0.progressView
            r11 = -1
            r12 = 1073741824(0x40000000, float:2.0)
            r13 = 51
            r14 = 1101529088(0x41a80000, float:21.0)
            r15 = 1119092736(0x42b40000, float:90.0)
            r16 = 1101529088(0x41a80000, float:21.0)
            r17 = 0
            android.widget.FrameLayout$LayoutParams r11 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r12, r13, r14, r15, r16, r17)
            r5.addView(r10, r11)
            org.telegram.ui.ActionBar.SimpleTextView r5 = new org.telegram.ui.ActionBar.SimpleTextView
            r5.<init>(r1)
            r0.timeTextView = r5
            r10 = 12
            r5.setTextSize(r10)
            org.telegram.ui.ActionBar.SimpleTextView r5 = r0.timeTextView
            java.lang.String r10 = "0:00"
            r5.setText(r10)
            org.telegram.ui.ActionBar.SimpleTextView r5 = r0.timeTextView
            int r10 = org.telegram.ui.ActionBar.Theme.getColor(r6)
            r5.setTextColor(r10)
            org.telegram.ui.ActionBar.SimpleTextView r5 = r0.timeTextView
            r10 = 2
            r5.setImportantForAccessibility(r10)
            android.widget.FrameLayout r5 = r0.playerLayout
            org.telegram.ui.ActionBar.SimpleTextView r11 = r0.timeTextView
            r12 = 100
            r13 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r14 = 51
            r15 = 1101004800(0x41a00000, float:20.0)
            r16 = 1120141312(0x42CLASSNAME, float:98.0)
            r18 = 0
            android.widget.FrameLayout$LayoutParams r12 = org.telegram.ui.Components.LayoutHelper.createFrame(r12, r13, r14, r15, r16, r17, r18)
            r5.addView(r11, r12)
            android.widget.TextView r5 = new android.widget.TextView
            r5.<init>(r1)
            r0.durationTextView = r5
            r11 = 1094713344(0x41400000, float:12.0)
            r5.setTextSize(r2, r11)
            android.widget.TextView r5 = r0.durationTextView
            int r6 = org.telegram.ui.ActionBar.Theme.getColor(r6)
            r5.setTextColor(r6)
            android.widget.TextView r5 = r0.durationTextView
            r6 = 17
            r5.setGravity(r6)
            android.widget.TextView r5 = r0.durationTextView
            r5.setImportantForAccessibility(r10)
            android.widget.FrameLayout r5 = r0.playerLayout
            android.widget.TextView r11 = r0.durationTextView
            r12 = -2
            r14 = 53
            r15 = 0
            r16 = 1119879168(0x42CLASSNAME, float:96.0)
            r17 = 1101004800(0x41a00000, float:20.0)
            android.widget.FrameLayout$LayoutParams r12 = org.telegram.ui.Components.LayoutHelper.createFrame(r12, r13, r14, r15, r16, r17, r18)
            r5.addView(r11, r12)
            android.widget.ImageView r5 = new android.widget.ImageView
            r5.<init>(r1)
            r0.playbackSpeedButton = r5
            android.widget.ImageView$ScaleType r11 = android.widget.ImageView.ScaleType.CENTER
            r5.setScaleType(r11)
            android.widget.ImageView r5 = r0.playbackSpeedButton
            r11 = 2131166078(0x7var_e, float:1.7946391E38)
            r5.setImageResource(r11)
            android.widget.ImageView r5 = r0.playbackSpeedButton
            r11 = 2131624008(0x7f0e0048, float:1.8875184E38)
            java.lang.String r12 = "AccDescrPlayerSpeed"
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r12, r11)
            r5.setContentDescription(r11)
            float r5 = org.telegram.messenger.AndroidUtilities.density
            r11 = 1077936128(0x40400000, float:3.0)
            int r5 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r5 < 0) goto L_0x0342
            android.widget.ImageView r5 = r0.playbackSpeedButton
            r5.setPadding(r7, r2, r7, r7)
        L_0x0342:
            android.widget.FrameLayout r5 = r0.playerLayout
            android.widget.ImageView r12 = r0.playbackSpeedButton
            r13 = 36
            r14 = 1108344832(0x42100000, float:36.0)
            r15 = 53
            r16 = 0
            r17 = 1118568448(0x42aCLASSNAME, float:86.0)
            r18 = 1101004800(0x41a00000, float:20.0)
            r19 = 0
            android.widget.FrameLayout$LayoutParams r13 = org.telegram.ui.Components.LayoutHelper.createFrame(r13, r14, r15, r16, r17, r18, r19)
            r5.addView(r12, r13)
            android.widget.ImageView r5 = r0.playbackSpeedButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$GUNMobnKifS5KZiFfcFb6Y3nm8k r12 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$GUNMobnKifS5KZiFfcFb6Y3nm8k
            r12.<init>()
            r5.setOnClickListener(r12)
            r27.updatePlaybackButton()
            org.telegram.ui.Components.AudioPlayerAlert$10 r5 = new org.telegram.ui.Components.AudioPlayerAlert$10
            r5.<init>(r1)
            android.widget.FrameLayout r12 = r0.playerLayout
            r13 = -1
            r14 = 1115947008(0x42840000, float:66.0)
            r15 = 51
            r17 = 1121845248(0x42de0000, float:111.0)
            r18 = 0
            android.widget.FrameLayout$LayoutParams r13 = org.telegram.ui.Components.LayoutHelper.createFrame(r13, r14, r15, r16, r17, r18, r19)
            r12.addView(r5, r13)
            android.view.View[] r12 = r0.buttons
            org.telegram.ui.ActionBar.ActionBarMenuItem r13 = new org.telegram.ui.ActionBar.ActionBarMenuItem
            r14 = 0
            r13.<init>(r1, r14, r7, r7)
            r0.repeatButton = r13
            r12[r7] = r13
            r13.setLongClickEnabled(r7)
            org.telegram.ui.ActionBar.ActionBarMenuItem r12 = r0.repeatButton
            r12.setShowSubmenuByMove(r7)
            org.telegram.ui.ActionBar.ActionBarMenuItem r12 = r0.repeatButton
            r13 = 1126563840(0x43260000, float:166.0)
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
            int r13 = -r13
            r12.setAdditionalYOffset(r13)
            int r12 = android.os.Build.VERSION.SDK_INT
            java.lang.String r13 = "listSelectorSDK21"
            r15 = 21
            if (r12 < r15) goto L_0x03ba
            org.telegram.ui.ActionBar.ActionBarMenuItem r9 = r0.repeatButton
            int r11 = org.telegram.ui.ActionBar.Theme.getColor(r13)
            r18 = 1099956224(0x41900000, float:18.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r18)
            android.graphics.drawable.Drawable r4 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r11, r2, r4)
            r9.setBackgroundDrawable(r4)
        L_0x03ba:
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.repeatButton
            r9 = 48
            r11 = 51
            android.widget.FrameLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r11)
            r5.addView(r4, r6)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.repeatButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$VAW_7HLeaN8EeVMAo0f6wQWqy8o r6 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$VAW_7HLeaN8EeVMAo0f6wQWqy8o
            r6.<init>()
            r4.setOnClickListener(r6)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.repeatButton
            r6 = 2131165905(0x7var_d1, float:1.794604E38)
            r3 = 2131626890(0x7f0e0b8a, float:1.8881029E38)
            java.lang.String r14 = "RepeatSong"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r14, r3)
            r14 = 3
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r4.addSubItem(r14, r6, r3)
            r0.repeatSongItem = r3
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.repeatButton
            r4 = 2131165904(0x7var_d0, float:1.7946038E38)
            r6 = 2131626887(0x7f0e0b87, float:1.8881023E38)
            java.lang.String r14 = "RepeatList"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r14, r6)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r3.addSubItem(r8, r4, r6)
            r0.repeatListItem = r3
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.repeatButton
            r4 = 2131165906(0x7var_d2, float:1.7946042E38)
            r6 = 2131627220(0x7f0e0cd4, float:1.8881698E38)
            java.lang.String r14 = "ShuffleList"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r14, r6)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r3.addSubItem(r10, r4, r6)
            r0.shuffleListItem = r3
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.repeatButton
            r4 = 2131165898(0x7var_ca, float:1.7946026E38)
            r6 = 2131626968(0x7f0e0bd8, float:1.8881187E38)
            java.lang.String r14 = "ReverseOrder"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r14, r6)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r3.addSubItem(r2, r4, r6)
            r0.reverseOrderItem = r3
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.repeatButton
            r3.setShowedFromBottom(r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.repeatButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$4jBe25K-UQcScPi6dlmsCVmPKM8 r4 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$4jBe25K-UQcScPi6dlmsCVmPKM8
            r4.<init>()
            r3.setDelegate(r4)
            java.lang.String r3 = "player_button"
            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            android.view.View[] r6 = r0.buttons
            org.telegram.ui.Components.RLottieImageView r14 = new org.telegram.ui.Components.RLottieImageView
            r14.<init>(r1)
            r0.prevButton = r14
            r6[r2] = r14
            android.widget.ImageView$ScaleType r6 = android.widget.ImageView.ScaleType.CENTER
            r14.setScaleType(r6)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            r14 = 2131558437(0x7f0d0025, float:1.874219E38)
            r8 = 20
            r6.setAnimation(r14, r8, r8)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            java.lang.String r14 = "Triangle 3.**"
            r6.setLayerColor(r14, r4)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            java.lang.String r14 = "Triangle 4.**"
            r6.setLayerColor(r14, r4)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            java.lang.String r14 = "Rectangle 4.**"
            r6.setLayerColor(r14, r4)
            if (r12 < r15) goto L_0x047b
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            int r14 = org.telegram.ui.ActionBar.Theme.getColor(r13)
            r20 = 1102053376(0x41b00000, float:22.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r20)
            android.graphics.drawable.Drawable r8 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r14, r2, r8)
            r6.setBackgroundDrawable(r8)
        L_0x047b:
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            android.widget.FrameLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r11)
            r5.addView(r6, r8)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$iQfGiCAlHsQQpPxLCGS7ELOuSnE r8 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$iQfGiCAlHsQQpPxLCGS7ELOuSnE
            r8.<init>()
            r6.setOnClickListener(r8)
            org.telegram.ui.Components.RLottieImageView r6 = r0.prevButton
            r8 = 2131624009(0x7f0e0049, float:1.8875186E38)
            java.lang.String r14 = "AccDescrPrevious"
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r14, r8)
            r6.setContentDescription(r8)
            android.view.View[] r6 = r0.buttons
            android.widget.ImageView r8 = new android.widget.ImageView
            r8.<init>(r1)
            r0.playButton = r8
            r6[r10] = r8
            android.widget.ImageView$ScaleType r6 = android.widget.ImageView.ScaleType.CENTER
            r8.setScaleType(r6)
            android.widget.ImageView r6 = r0.playButton
            org.telegram.ui.Components.PlayPauseDrawable r8 = new org.telegram.ui.Components.PlayPauseDrawable
            r14 = 28
            r8.<init>(r14)
            r0.playPauseDrawable = r8
            r6.setImageDrawable(r8)
            org.telegram.ui.Components.PlayPauseDrawable r6 = r0.playPauseDrawable
            org.telegram.messenger.MediaController r8 = org.telegram.messenger.MediaController.getInstance()
            boolean r8 = r8.isMessagePaused()
            r8 = r8 ^ r2
            r6.setPause(r8, r7)
            android.widget.ImageView r6 = r0.playButton
            android.graphics.PorterDuffColorFilter r8 = new android.graphics.PorterDuffColorFilter
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            android.graphics.PorterDuff$Mode r14 = android.graphics.PorterDuff.Mode.MULTIPLY
            r8.<init>(r3, r14)
            r6.setColorFilter(r8)
            if (r12 < r15) goto L_0x04ed
            android.widget.ImageView r3 = r0.playButton
            int r6 = org.telegram.ui.ActionBar.Theme.getColor(r13)
            r8 = 1103101952(0x41CLASSNAME, float:24.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            android.graphics.drawable.Drawable r6 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r6, r2, r8)
            r3.setBackgroundDrawable(r6)
        L_0x04ed:
            android.widget.ImageView r3 = r0.playButton
            android.widget.FrameLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r11)
            r5.addView(r3, r6)
            android.widget.ImageView r3 = r0.playButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$7-kc8b58kYjzbW9UxMrQNUbONXM r6 = org.telegram.ui.Components.$$Lambda$AudioPlayerAlert$7kc8b58kYjzbW9UxMrQNUbONXM.INSTANCE
            r3.setOnClickListener(r6)
            android.view.View[] r3 = r0.buttons
            org.telegram.ui.Components.RLottieImageView r6 = new org.telegram.ui.Components.RLottieImageView
            r6.<init>(r1)
            r0.nextButton = r6
            r8 = 3
            r3[r8] = r6
            android.widget.ImageView$ScaleType r3 = android.widget.ImageView.ScaleType.CENTER
            r6.setScaleType(r3)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            r6 = 2131558437(0x7f0d0025, float:1.874219E38)
            r8 = 20
            r3.setAnimation(r6, r8, r8)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            java.lang.String r6 = "Triangle 3.**"
            r3.setLayerColor(r6, r4)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            java.lang.String r6 = "Triangle 4.**"
            r3.setLayerColor(r6, r4)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            java.lang.String r6 = "Rectangle 4.**"
            r3.setLayerColor(r6, r4)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            r6 = 1127481344(0x43340000, float:180.0)
            r3.setRotation(r6)
            if (r12 < r15) goto L_0x0549
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            int r6 = org.telegram.ui.ActionBar.Theme.getColor(r13)
            r8 = 1102053376(0x41b00000, float:22.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            android.graphics.drawable.Drawable r6 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r6, r2, r8)
            r3.setBackgroundDrawable(r6)
        L_0x0549:
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            android.widget.FrameLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r11)
            r5.addView(r3, r6)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$iWtHdrkZSnX8QlYZyR0A9TG8l6w r6 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$iWtHdrkZSnX8QlYZyR0A9TG8l6w
            r6.<init>()
            r3.setOnClickListener(r6)
            org.telegram.ui.Components.RLottieImageView r3 = r0.nextButton
            r6 = 2131626049(0x7f0e0841, float:1.8879323E38)
            java.lang.String r8 = "Next"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            r3.setContentDescription(r6)
            android.view.View[] r3 = r0.buttons
            org.telegram.ui.ActionBar.ActionBarMenuItem r6 = new org.telegram.ui.ActionBar.ActionBarMenuItem
            r8 = 0
            r6.<init>(r1, r8, r7, r4)
            r0.optionsButton = r6
            r4 = 4
            r3[r4] = r6
            r6.setLongClickEnabled(r7)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r3.setShowSubmenuByMove(r7)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131165473(0x7var_, float:1.7945164E38)
            r3.setIcon((int) r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r3.setSubMenuOpenSide(r10)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 1125974016(0x431d0000, float:157.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            int r4 = -r4
            r3.setAdditionalYOffset(r4)
            if (r12 < r15) goto L_0x05ad
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r13)
            r6 = 1099956224(0x41900000, float:18.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            android.graphics.drawable.Drawable r4 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r4, r2, r6)
            r3.setBackgroundDrawable(r4)
        L_0x05ad:
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            android.widget.FrameLayout$LayoutParams r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r11)
            r5.addView(r3, r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131165733(0x7var_, float:1.7945691E38)
            r5 = 2131625473(0x7f0e0601, float:1.8878155E38)
            java.lang.String r6 = "Forward"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r3.addSubItem(r2, r4, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131165792(0x7var_, float:1.7945811E38)
            r5 = 2131627152(0x7f0e0CLASSNAME, float:1.888156E38)
            java.lang.String r6 = "ShareFile"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r3.addSubItem(r10, r4, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131165725(0x7var_d, float:1.7945675E38)
            r5 = 2131626986(0x7f0e0bea, float:1.8881224E38)
            java.lang.String r6 = "SaveToMusic"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r6 = 5
            r3.addSubItem(r6, r4, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131165757(0x7var_d, float:1.794574E38)
            r5 = 2131627210(0x7f0e0cca, float:1.8881678E38)
            java.lang.String r6 = "ShowInChat"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r6 = 4
            r3.addSubItem(r6, r4, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r3.setShowedFromBottom(r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$kvL-ccfKl1KjrNBRVGcgEPFM0_Q r4 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$kvL-ccfKl1KjrNBRVGcgEPFM0_Q
            r4.<init>()
            r3.setOnClickListener(r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$QuMbmSoWzDJMYPC5vcsQw0vuSf8 r4 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$QuMbmSoWzDJMYPC5vcsQw0vuSf8
            r4.<init>()
            r3.setDelegate(r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.optionsButton
            r4 = 2131623984(0x7f0e0030, float:1.8875135E38)
            java.lang.String r5 = "AccDescrMoreOptions"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r3.setContentDescription(r4)
            android.widget.LinearLayout r3 = new android.widget.LinearLayout
            r3.<init>(r1)
            r0.emptyView = r3
            r3.setOrientation(r2)
            android.widget.LinearLayout r3 = r0.emptyView
            r4 = 17
            r3.setGravity(r4)
            android.widget.LinearLayout r3 = r0.emptyView
            r4 = 8
            r3.setVisibility(r4)
            android.view.ViewGroup r3 = r0.containerView
            android.widget.LinearLayout r4 = r0.emptyView
            r5 = -1082130432(0xffffffffbvar_, float:-1.0)
            r6 = -1
            android.widget.FrameLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createFrame(r6, r5)
            r3.addView(r4, r5)
            android.widget.LinearLayout r3 = r0.emptyView
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$QWGccwO-HDL5ngvWnUNTAToRiHI r4 = org.telegram.ui.Components.$$Lambda$AudioPlayerAlert$QWGccwOHDL5ngvWnUNTAToRiHI.INSTANCE
            r3.setOnTouchListener(r4)
            android.widget.ImageView r3 = new android.widget.ImageView
            r3.<init>(r1)
            r0.emptyImageView = r3
            r4 = 2131165817(0x7var_, float:1.7945862E38)
            r3.setImageResource(r4)
            android.widget.ImageView r3 = r0.emptyImageView
            android.graphics.PorterDuffColorFilter r4 = new android.graphics.PorterDuffColorFilter
            java.lang.String r5 = "dialogEmptyImage"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            android.graphics.PorterDuff$Mode r6 = android.graphics.PorterDuff.Mode.MULTIPLY
            r4.<init>(r5, r6)
            r3.setColorFilter(r4)
            android.widget.LinearLayout r3 = r0.emptyView
            android.widget.ImageView r4 = r0.emptyImageView
            r5 = -2
            r6 = -2
            android.widget.LinearLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createLinear(r5, r6)
            r3.addView(r4, r5)
            android.widget.TextView r3 = new android.widget.TextView
            r3.<init>(r1)
            r0.emptyTitleTextView = r3
            java.lang.String r4 = "dialogEmptyText"
            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r4)
            r3.setTextColor(r4)
            android.widget.TextView r3 = r0.emptyTitleTextView
            r4 = 17
            r3.setGravity(r4)
            android.widget.TextView r3 = r0.emptyTitleTextView
            r4 = 2131626052(0x7f0e0844, float:1.887933E38)
            java.lang.String r5 = "NoAudioFound"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r3.setText(r4)
            android.widget.TextView r3 = r0.emptyTitleTextView
            java.lang.String r4 = "fonts/rmedium.ttf"
            android.graphics.Typeface r4 = org.telegram.messenger.AndroidUtilities.getTypeface(r4)
            r3.setTypeface(r4)
            android.widget.TextView r3 = r0.emptyTitleTextView
            r4 = 1099431936(0x41880000, float:17.0)
            r3.setTextSize(r2, r4)
            android.widget.TextView r3 = r0.emptyTitleTextView
            r4 = 1109393408(0x42200000, float:40.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r4)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r3.setPadding(r5, r7, r6, r7)
            android.widget.LinearLayout r3 = r0.emptyView
            android.widget.TextView r5 = r0.emptyTitleTextView
            r20 = -2
            r21 = -2
            r22 = 17
            r23 = 0
            r24 = 11
            r25 = 0
            r26 = 0
            android.widget.LinearLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r20, (int) r21, (int) r22, (int) r23, (int) r24, (int) r25, (int) r26)
            r3.addView(r5, r6)
            android.widget.TextView r3 = new android.widget.TextView
            r3.<init>(r1)
            r0.emptySubtitleTextView = r3
            java.lang.String r5 = "dialogEmptyText"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r3.setTextColor(r5)
            android.widget.TextView r3 = r0.emptySubtitleTextView
            r5 = 17
            r3.setGravity(r5)
            android.widget.TextView r3 = r0.emptySubtitleTextView
            r5 = 1097859072(0x41700000, float:15.0)
            r3.setTextSize(r2, r5)
            android.widget.TextView r3 = r0.emptySubtitleTextView
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r4)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r3.setPadding(r5, r7, r4, r7)
            android.widget.LinearLayout r3 = r0.emptyView
            android.widget.TextView r4 = r0.emptySubtitleTextView
            r24 = 6
            android.widget.LinearLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r20, (int) r21, (int) r22, (int) r23, (int) r24, (int) r25, (int) r26)
            r3.addView(r4, r5)
            org.telegram.ui.Components.AudioPlayerAlert$11 r3 = new org.telegram.ui.Components.AudioPlayerAlert$11
            r3.<init>(r1)
            r0.listView = r3
            r3.setClipToPadding(r7)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            androidx.recyclerview.widget.LinearLayoutManager r4 = new androidx.recyclerview.widget.LinearLayoutManager
            android.content.Context r5 = r27.getContext()
            r4.<init>(r5, r2, r7)
            r0.layoutManager = r4
            r3.setLayoutManager(r4)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            r3.setHorizontalScrollBarEnabled(r7)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            r3.setVerticalScrollBarEnabled(r7)
            android.view.ViewGroup r3 = r0.containerView
            org.telegram.ui.Components.RecyclerListView r4 = r0.listView
            r5 = -1
            android.widget.FrameLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createFrame(r5, r5, r11)
            r3.addView(r4, r6)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.Components.AudioPlayerAlert$ListAdapter r4 = new org.telegram.ui.Components.AudioPlayerAlert$ListAdapter
            r4.<init>(r1)
            r0.listAdapter = r4
            r3.setAdapter(r4)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            java.lang.String r4 = "dialogScrollGlow"
            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r4)
            r3.setGlowColor(r4)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$JlLxmkt-Nj8ttrl9u0aAByTUS9M r4 = org.telegram.ui.Components.$$Lambda$AudioPlayerAlert$JlLxmktNj8ttrl9u0aAByTUS9M.INSTANCE
            r3.setOnItemClickListener((org.telegram.ui.Components.RecyclerListView.OnItemClickListener) r4)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.Components.AudioPlayerAlert$12 r4 = new org.telegram.ui.Components.AudioPlayerAlert$12
            r4.<init>()
            r3.setOnScrollListener(r4)
            org.telegram.messenger.MediaController r3 = org.telegram.messenger.MediaController.getInstance()
            java.util.ArrayList r3 = r3.getPlaylist()
            r0.playlist = r3
            org.telegram.ui.Components.AudioPlayerAlert$ListAdapter r3 = r0.listAdapter
            r3.notifyDataSetChanged()
            android.view.ViewGroup r3 = r0.containerView
            android.widget.FrameLayout r4 = r0.playerLayout
            r5 = 179(0xb3, float:2.51E-43)
            r6 = 83
            r8 = -1
            android.widget.FrameLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createFrame(r8, r5, r6)
            r3.addView(r4, r5)
            android.view.ViewGroup r3 = r0.containerView
            android.view.View r4 = r0.playerShadow
            android.widget.FrameLayout$LayoutParams r5 = new android.widget.FrameLayout$LayoutParams
            int r6 = org.telegram.messenger.AndroidUtilities.getShadowHeight()
            r9 = 83
            r5.<init>(r8, r6, r9)
            r3.addView(r4, r5)
            android.view.View r3 = r0.playerShadow
            android.view.ViewGroup$LayoutParams r3 = r3.getLayoutParams()
            android.widget.FrameLayout$LayoutParams r3 = (android.widget.FrameLayout.LayoutParams) r3
            r4 = 1127415808(0x43330000, float:179.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r3.bottomMargin = r4
            android.view.ViewGroup r3 = r0.containerView
            android.view.View r4 = r0.actionBarShadow
            r5 = 1077936128(0x40400000, float:3.0)
            android.widget.FrameLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createFrame(r8, r5)
            r3.addView(r4, r5)
            android.view.ViewGroup r3 = r0.containerView
            org.telegram.ui.ActionBar.ActionBar r4 = r0.actionBar
            r3.addView(r4)
            org.telegram.ui.Components.AudioPlayerAlert$13 r3 = new org.telegram.ui.Components.AudioPlayerAlert$13
            r3.<init>(r1)
            r0.blurredView = r3
            r4 = 0
            r3.setAlpha(r4)
            android.widget.FrameLayout r3 = r0.blurredView
            r4 = 4
            r3.setVisibility(r4)
            android.widget.FrameLayout r3 = r27.getContainer()
            android.widget.FrameLayout r4 = r0.blurredView
            r3.addView(r4)
            org.telegram.ui.Components.BackupImageView r3 = new org.telegram.ui.Components.BackupImageView
            r3.<init>(r1)
            r0.bigAlbumConver = r3
            r3.setAspectFit(r2)
            org.telegram.ui.Components.BackupImageView r1 = r0.bigAlbumConver
            r2 = 1090519040(0x41000000, float:8.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.setRoundRadius(r2)
            org.telegram.ui.Components.BackupImageView r1 = r0.bigAlbumConver
            r2 = 1063675494(0x3var_, float:0.9)
            r1.setScaleX(r2)
            org.telegram.ui.Components.BackupImageView r1 = r0.bigAlbumConver
            r1.setScaleY(r2)
            android.widget.FrameLayout r1 = r0.blurredView
            org.telegram.ui.Components.BackupImageView r2 = r0.bigAlbumConver
            r8 = -1
            r9 = -1082130432(0xffffffffbvar_, float:-1.0)
            r10 = 51
            r11 = 1106247680(0x41var_, float:30.0)
            r12 = 1106247680(0x41var_, float:30.0)
            r13 = 1106247680(0x41var_, float:30.0)
            r14 = 1106247680(0x41var_, float:30.0)
            android.widget.FrameLayout$LayoutParams r3 = org.telegram.ui.Components.LayoutHelper.createFrame(r8, r9, r10, r11, r12, r13, r14)
            r1.addView(r2, r3)
            r0.updateTitle(r7)
            r27.updateRepeatButton()
            r27.updateEmptyView()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.AudioPlayerAlert.<init>(android.content.Context):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AudioPlayerAlert(View view) {
        if (MediaController.getInstance().getPlaybackSpeed(true) > 1.0f) {
            MediaController.getInstance().setPlaybackSpeed(true, 1.0f);
        } else {
            MediaController.getInstance().setPlaybackSpeed(true, 1.8f);
        }
        updatePlaybackButton();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$AudioPlayerAlert(View view) {
        updateSubMenu();
        this.repeatButton.toggleSubMenu();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$AudioPlayerAlert(int i) {
        if (i == 1 || i == 2) {
            boolean z = SharedConfig.playOrderReversed;
            if ((!z || i != 1) && (!SharedConfig.shuffleMusic || i != 2)) {
                MediaController.getInstance().setPlaybackOrderType(i);
            } else {
                MediaController.getInstance().setPlaybackOrderType(0);
            }
            this.listAdapter.notifyDataSetChanged();
            if (z != SharedConfig.playOrderReversed) {
                this.listView.stopScroll();
                scrollToCurrentSong(false);
            }
        } else if (i == 4) {
            if (SharedConfig.repeatMode == 1) {
                SharedConfig.setRepeatMode(0);
            } else {
                SharedConfig.setRepeatMode(1);
            }
        } else if (SharedConfig.repeatMode == 2) {
            SharedConfig.setRepeatMode(0);
        } else {
            SharedConfig.setRepeatMode(2);
        }
        updateRepeatButton();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$AudioPlayerAlert(View view) {
        MediaController.getInstance().playPreviousMessage();
        this.prevButton.setProgress(0.0f);
        this.prevButton.playAnimation();
    }

    static /* synthetic */ void lambda$new$4(View view) {
        if (!MediaController.getInstance().isDownloadingCurrentMessage()) {
            if (MediaController.getInstance().isMessagePaused()) {
                MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
            } else {
                MediaController.getInstance().lambda$startAudioAgain$7(MediaController.getInstance().getPlayingMessageObject());
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$5 */
    public /* synthetic */ void lambda$new$5$AudioPlayerAlert(View view) {
        MediaController.getInstance().playNextMessage();
        this.nextButton.setProgress(0.0f);
        this.nextButton.playAnimation();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$6 */
    public /* synthetic */ void lambda$new$6$AudioPlayerAlert(View view) {
        this.optionsButton.toggleSubMenu();
    }

    static /* synthetic */ void lambda$new$8(View view, int i) {
        if (view instanceof AudioPlayerCell) {
            ((AudioPlayerCell) view).didPressedButton();
        }
    }

    /* access modifiers changed from: private */
    public void updateEmptyViewPosition() {
        if (this.emptyView.getVisibility() == 0) {
            int dp = this.playerLayout.getVisibility() == 0 ? AndroidUtilities.dp(150.0f) : -AndroidUtilities.dp(30.0f);
            LinearLayout linearLayout = this.emptyView;
            linearLayout.setTranslationY((float) (((linearLayout.getMeasuredHeight() - this.containerView.getMeasuredHeight()) - dp) / 2));
        }
    }

    /* access modifiers changed from: private */
    public void updateEmptyView() {
        this.emptyView.setVisibility((!this.searching || this.listAdapter.getItemCount() != 0) ? 8 : 0);
        updateEmptyViewPosition();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean scrollToCurrentSong(boolean r7) {
        /*
            r6 = this;
            org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.getInstance()
            org.telegram.messenger.MessageObject r0 = r0.getPlayingMessageObject()
            r1 = 0
            if (r0 == 0) goto L_0x005d
            r2 = 1
            if (r7 == 0) goto L_0x003b
            org.telegram.ui.Components.RecyclerListView r7 = r6.listView
            int r7 = r7.getChildCount()
            r3 = 0
        L_0x0015:
            if (r3 >= r7) goto L_0x003b
            org.telegram.ui.Components.RecyclerListView r4 = r6.listView
            android.view.View r4 = r4.getChildAt(r3)
            boolean r5 = r4 instanceof org.telegram.ui.Cells.AudioPlayerCell
            if (r5 == 0) goto L_0x0038
            r5 = r4
            org.telegram.ui.Cells.AudioPlayerCell r5 = (org.telegram.ui.Cells.AudioPlayerCell) r5
            org.telegram.messenger.MessageObject r5 = r5.getMessageObject()
            if (r5 != r0) goto L_0x0038
            int r7 = r4.getBottom()
            org.telegram.ui.Components.RecyclerListView r3 = r6.listView
            int r3 = r3.getMeasuredHeight()
            if (r7 > r3) goto L_0x003b
            r7 = 1
            goto L_0x003c
        L_0x0038:
            int r3 = r3 + 1
            goto L_0x0015
        L_0x003b:
            r7 = 0
        L_0x003c:
            if (r7 != 0) goto L_0x005d
            java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r6.playlist
            int r7 = r7.indexOf(r0)
            if (r7 < 0) goto L_0x005d
            boolean r0 = org.telegram.messenger.SharedConfig.playOrderReversed
            if (r0 == 0) goto L_0x0050
            androidx.recyclerview.widget.LinearLayoutManager r0 = r6.layoutManager
            r0.scrollToPosition(r7)
            goto L_0x005c
        L_0x0050:
            androidx.recyclerview.widget.LinearLayoutManager r0 = r6.layoutManager
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r6.playlist
            int r1 = r1.size()
            int r1 = r1 - r7
            r0.scrollToPosition(r1)
        L_0x005c:
            return r2
        L_0x005d:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.AudioPlayerAlert.scrollToCurrentSong(boolean):boolean");
    }

    public boolean onCustomMeasure(View view, int i, int i2) {
        FrameLayout frameLayout = this.blurredView;
        if (view != frameLayout) {
            return false;
        }
        frameLayout.measure(View.MeasureSpec.makeMeasureSpec(i, NUM), View.MeasureSpec.makeMeasureSpec(i2, NUM));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onCustomLayout(View view, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        FrameLayout frameLayout = this.blurredView;
        if (view != frameLayout) {
            return false;
        }
        frameLayout.layout(i, 0, i5 + i, i6);
        return true;
    }

    private void setMenuItemChecked(ActionBarMenuSubItem actionBarMenuSubItem, boolean z) {
        if (z) {
            actionBarMenuSubItem.setTextColor(Theme.getColor("player_buttonActive"));
            actionBarMenuSubItem.setIconColor(Theme.getColor("player_buttonActive"));
            return;
        }
        actionBarMenuSubItem.setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
        actionBarMenuSubItem.setIconColor(Theme.getColor("actionBarDefaultSubmenuItem"));
    }

    private void updateSubMenu() {
        setMenuItemChecked(this.shuffleListItem, SharedConfig.shuffleMusic);
        setMenuItemChecked(this.reverseOrderItem, SharedConfig.playOrderReversed);
        boolean z = false;
        setMenuItemChecked(this.repeatListItem, SharedConfig.repeatMode == 1);
        ActionBarMenuSubItem actionBarMenuSubItem = this.repeatSongItem;
        if (SharedConfig.repeatMode == 2) {
            z = true;
        }
        setMenuItemChecked(actionBarMenuSubItem, z);
    }

    private void updatePlaybackButton() {
        if (MediaController.getInstance().getPlaybackSpeed(true) > 1.0f) {
            this.playbackSpeedButton.setTag("inappPlayerPlayPause");
            this.playbackSpeedButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("inappPlayerPlayPause"), PorterDuff.Mode.MULTIPLY));
            return;
        }
        this.playbackSpeedButton.setTag("inappPlayerClose");
        this.playbackSpeedButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("inappPlayerClose"), PorterDuff.Mode.MULTIPLY));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:28|29|30|31) */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0068, code lost:
        if (r13.exists() == false) goto L_0x006a;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:30:0x00a4 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSubItemClick(int r13) {
        /*
            r12 = this;
            int r0 = android.os.Build.VERSION.SDK_INT
            org.telegram.messenger.MediaController r1 = org.telegram.messenger.MediaController.getInstance()
            org.telegram.messenger.MessageObject r1 = r1.getPlayingMessageObject()
            if (r1 == 0) goto L_0x01ec
            org.telegram.ui.LaunchActivity r2 = r12.parentActivity
            if (r2 != 0) goto L_0x0012
            goto L_0x01ec
        L_0x0012:
            r3 = 1
            if (r13 != r3) goto L_0x004d
            int r13 = org.telegram.messenger.UserConfig.selectedAccount
            int r0 = r12.currentAccount
            if (r13 == r0) goto L_0x001e
            r2.switchToAccount(r0, r3)
        L_0x001e:
            android.os.Bundle r13 = new android.os.Bundle
            r13.<init>()
            java.lang.String r0 = "onlySelect"
            r13.putBoolean(r0, r3)
            r0 = 3
            java.lang.String r2 = "dialogsType"
            r13.putInt(r2, r0)
            org.telegram.ui.DialogsActivity r0 = new org.telegram.ui.DialogsActivity
            r0.<init>(r13)
            java.util.ArrayList r13 = new java.util.ArrayList
            r13.<init>()
            r13.add(r1)
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$6TFRLYQXhgnPYZQazExaS-UeXrc r1 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$6TFRLYQXhgnPYZQazExaS-UeXrc
            r1.<init>(r13)
            r0.setDelegate(r1)
            org.telegram.ui.LaunchActivity r13 = r12.parentActivity
            r13.lambda$runLinkRequest$38(r0)
            r12.dismiss()
            goto L_0x01ec
        L_0x004d:
            r4 = 2
            r5 = 0
            if (r13 != r4) goto L_0x00ff
            org.telegram.tgnet.TLRPC$Message r13 = r1.messageOwner     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r13 = r13.attachPath     // Catch:{ Exception -> 0x00f9 }
            boolean r13 = android.text.TextUtils.isEmpty(r13)     // Catch:{ Exception -> 0x00f9 }
            if (r13 != 0) goto L_0x006a
            java.io.File r13 = new java.io.File     // Catch:{ Exception -> 0x00f9 }
            org.telegram.tgnet.TLRPC$Message r2 = r1.messageOwner     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r2 = r2.attachPath     // Catch:{ Exception -> 0x00f9 }
            r13.<init>(r2)     // Catch:{ Exception -> 0x00f9 }
            boolean r2 = r13.exists()     // Catch:{ Exception -> 0x00f9 }
            if (r2 != 0) goto L_0x006b
        L_0x006a:
            r13 = r5
        L_0x006b:
            if (r13 != 0) goto L_0x0073
            org.telegram.tgnet.TLRPC$Message r13 = r1.messageOwner     // Catch:{ Exception -> 0x00f9 }
            java.io.File r13 = org.telegram.messenger.FileLoader.getPathToMessage(r13)     // Catch:{ Exception -> 0x00f9 }
        L_0x0073:
            boolean r2 = r13.exists()     // Catch:{ Exception -> 0x00f9 }
            if (r2 == 0) goto L_0x00c9
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r4 = "android.intent.action.SEND"
            r2.<init>(r4)     // Catch:{ Exception -> 0x00f9 }
            if (r1 == 0) goto L_0x008a
            java.lang.String r1 = r1.getMimeType()     // Catch:{ Exception -> 0x00f9 }
            r2.setType(r1)     // Catch:{ Exception -> 0x00f9 }
            goto L_0x008f
        L_0x008a:
            java.lang.String r1 = "audio/mp3"
            r2.setType(r1)     // Catch:{ Exception -> 0x00f9 }
        L_0x008f:
            r1 = 24
            java.lang.String r4 = "android.intent.extra.STREAM"
            if (r0 < r1) goto L_0x00ac
            android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00a4 }
            java.lang.String r1 = "org.telegram.messenger.beta.provider"
            android.net.Uri r0 = androidx.core.content.FileProvider.getUriForFile(r0, r1, r13)     // Catch:{ Exception -> 0x00a4 }
            r2.putExtra(r4, r0)     // Catch:{ Exception -> 0x00a4 }
            r2.setFlags(r3)     // Catch:{ Exception -> 0x00a4 }
            goto L_0x00b3
        L_0x00a4:
            android.net.Uri r13 = android.net.Uri.fromFile(r13)     // Catch:{ Exception -> 0x00f9 }
            r2.putExtra(r4, r13)     // Catch:{ Exception -> 0x00f9 }
            goto L_0x00b3
        L_0x00ac:
            android.net.Uri r13 = android.net.Uri.fromFile(r13)     // Catch:{ Exception -> 0x00f9 }
            r2.putExtra(r4, r13)     // Catch:{ Exception -> 0x00f9 }
        L_0x00b3:
            org.telegram.ui.LaunchActivity r13 = r12.parentActivity     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r0 = "ShareFile"
            r1 = 2131627152(0x7f0e0CLASSNAME, float:1.888156E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r0, r1)     // Catch:{ Exception -> 0x00f9 }
            android.content.Intent r0 = android.content.Intent.createChooser(r2, r0)     // Catch:{ Exception -> 0x00f9 }
            r1 = 500(0x1f4, float:7.0E-43)
            r13.startActivityForResult(r0, r1)     // Catch:{ Exception -> 0x00f9 }
            goto L_0x01ec
        L_0x00c9:
            org.telegram.ui.ActionBar.AlertDialog$Builder r13 = new org.telegram.ui.ActionBar.AlertDialog$Builder     // Catch:{ Exception -> 0x00f9 }
            org.telegram.ui.LaunchActivity r0 = r12.parentActivity     // Catch:{ Exception -> 0x00f9 }
            r13.<init>((android.content.Context) r0)     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r0 = "AppName"
            r1 = 2131624258(0x7f0e0142, float:1.887569E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r0, r1)     // Catch:{ Exception -> 0x00f9 }
            r13.setTitle(r0)     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r0 = "OK"
            r1 = 2131626299(0x7f0e093b, float:1.887983E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r0, r1)     // Catch:{ Exception -> 0x00f9 }
            r13.setPositiveButton(r0, r5)     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r0 = "PleaseDownload"
            r1 = 2131626714(0x7f0e0ada, float:1.8880672E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r0, r1)     // Catch:{ Exception -> 0x00f9 }
            r13.setMessage(r0)     // Catch:{ Exception -> 0x00f9 }
            r13.show()     // Catch:{ Exception -> 0x00f9 }
            goto L_0x01ec
        L_0x00f9:
            r13 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r13)
            goto L_0x01ec
        L_0x00ff:
            r4 = 4
            r6 = 0
            if (r13 != r4) goto L_0x0176
            int r13 = org.telegram.messenger.UserConfig.selectedAccount
            int r0 = r12.currentAccount
            if (r13 == r0) goto L_0x010c
            r2.switchToAccount(r0, r3)
        L_0x010c:
            android.os.Bundle r13 = new android.os.Bundle
            r13.<init>()
            long r2 = r1.getDialogId()
            int r0 = (int) r2
            r4 = 32
            long r2 = r2 >> r4
            int r3 = (int) r2
            if (r0 == 0) goto L_0x014c
            if (r0 <= 0) goto L_0x0124
            java.lang.String r2 = "user_id"
            r13.putInt(r2, r0)
            goto L_0x0151
        L_0x0124:
            if (r0 >= 0) goto L_0x0151
            int r2 = r12.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            int r3 = -r0
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r2 = r2.getChat(r3)
            if (r2 == 0) goto L_0x0145
            org.telegram.tgnet.TLRPC$InputChannel r3 = r2.migrated_to
            if (r3 == 0) goto L_0x0145
            java.lang.String r3 = "migrated_to"
            r13.putInt(r3, r0)
            org.telegram.tgnet.TLRPC$InputChannel r0 = r2.migrated_to
            int r0 = r0.channel_id
            int r0 = -r0
        L_0x0145:
            int r0 = -r0
            java.lang.String r2 = "chat_id"
            r13.putInt(r2, r0)
            goto L_0x0151
        L_0x014c:
            java.lang.String r0 = "enc_id"
            r13.putInt(r0, r3)
        L_0x0151:
            int r0 = r1.getId()
            java.lang.String r1 = "message_id"
            r13.putInt(r1, r0)
            int r0 = r12.currentAccount
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getInstance(r0)
            int r1 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r0.postNotificationName(r1, r2)
            org.telegram.ui.LaunchActivity r0 = r12.parentActivity
            org.telegram.ui.ChatActivity r1 = new org.telegram.ui.ChatActivity
            r1.<init>(r13)
            r0.presentFragment(r1, r6, r6)
            r12.dismiss()
            goto L_0x01ec
        L_0x0176:
            r7 = 5
            if (r13 != r7) goto L_0x01ec
            r13 = 23
            if (r0 < r13) goto L_0x018f
            java.lang.String r13 = "android.permission.WRITE_EXTERNAL_STORAGE"
            int r0 = r2.checkSelfPermission(r13)
            if (r0 == 0) goto L_0x018f
            org.telegram.ui.LaunchActivity r0 = r12.parentActivity
            java.lang.String[] r1 = new java.lang.String[r3]
            r1[r6] = r13
            r0.requestPermissions(r1, r4)
            return
        L_0x018f:
            org.telegram.tgnet.TLRPC$Document r13 = r1.getDocument()
            java.lang.String r13 = org.telegram.messenger.FileLoader.getDocumentFileName(r13)
            boolean r0 = android.text.TextUtils.isEmpty(r13)
            if (r0 == 0) goto L_0x01a1
            java.lang.String r13 = r1.getFileName()
        L_0x01a1:
            r9 = r13
            org.telegram.tgnet.TLRPC$Message r13 = r1.messageOwner
            java.lang.String r13 = r13.attachPath
            if (r13 == 0) goto L_0x01ba
            int r0 = r13.length()
            if (r0 <= 0) goto L_0x01ba
            java.io.File r0 = new java.io.File
            r0.<init>(r13)
            boolean r0 = r0.exists()
            if (r0 != 0) goto L_0x01ba
            goto L_0x01bb
        L_0x01ba:
            r5 = r13
        L_0x01bb:
            if (r5 == 0) goto L_0x01c6
            int r13 = r5.length()
            if (r13 != 0) goto L_0x01c4
            goto L_0x01c6
        L_0x01c4:
            r6 = r5
            goto L_0x01d1
        L_0x01c6:
            org.telegram.tgnet.TLRPC$Message r13 = r1.messageOwner
            java.io.File r13 = org.telegram.messenger.FileLoader.getPathToMessage(r13)
            java.lang.String r13 = r13.toString()
            r6 = r13
        L_0x01d1:
            org.telegram.ui.LaunchActivity r7 = r12.parentActivity
            r8 = 3
            org.telegram.tgnet.TLRPC$Document r13 = r1.getDocument()
            if (r13 == 0) goto L_0x01e1
            org.telegram.tgnet.TLRPC$Document r13 = r1.getDocument()
            java.lang.String r13 = r13.mime_type
            goto L_0x01e3
        L_0x01e1:
            java.lang.String r13 = ""
        L_0x01e3:
            r10 = r13
            org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$lCDEyRHV7FgzpgArdX-jNazT0oQ r11 = new org.telegram.ui.Components.-$$Lambda$AudioPlayerAlert$lCDEyRHV7FgzpgArdX-jNazT0oQ
            r11.<init>()
            org.telegram.messenger.MediaController.saveFile(r6, r7, r8, r9, r10, r11)
        L_0x01ec:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.AudioPlayerAlert.onSubItemClick(int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onSubItemClick$9 */
    public /* synthetic */ void lambda$onSubItemClick$9$AudioPlayerAlert(ArrayList arrayList, DialogsActivity dialogsActivity, ArrayList arrayList2, CharSequence charSequence, boolean z) {
        ArrayList arrayList3 = arrayList2;
        if (arrayList2.size() > 1 || ((Long) arrayList3.get(0)).longValue() == ((long) UserConfig.getInstance(this.currentAccount).getClientUserId()) || charSequence != null) {
            ArrayList arrayList4 = arrayList;
            for (int i = 0; i < arrayList2.size(); i++) {
                long longValue = ((Long) arrayList3.get(i)).longValue();
                if (charSequence != null) {
                    SendMessagesHelper.getInstance(this.currentAccount).sendMessage(charSequence.toString(), longValue, (MessageObject) null, (MessageObject) null, (TLRPC$WebPage) null, true, (ArrayList<TLRPC$MessageEntity>) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, true, 0);
                }
                SendMessagesHelper.getInstance(this.currentAccount).sendMessage(arrayList, longValue, true, 0);
            }
            dialogsActivity.finishFragment();
            return;
        }
        long longValue2 = ((Long) arrayList3.get(0)).longValue();
        int i2 = (int) longValue2;
        int i3 = (int) (longValue2 >> 32);
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        if (i2 == 0) {
            bundle.putInt("enc_id", i3);
        } else if (i2 > 0) {
            bundle.putInt("user_id", i2);
        } else if (i2 < 0) {
            bundle.putInt("chat_id", -i2);
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        ChatActivity chatActivity = new ChatActivity(bundle);
        if (this.parentActivity.presentFragment(chatActivity, true, false)) {
            chatActivity.showFieldPanelForForward(true, arrayList);
        } else {
            dialogsActivity.finishFragment();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onSubItemClick$10 */
    public /* synthetic */ void lambda$onSubItemClick$10$AudioPlayerAlert() {
        BulletinFactory.of((FrameLayout) this.containerView).createDownloadBulletin(BulletinFactory.FileType.AUDIO).show();
    }

    /* access modifiers changed from: private */
    public void showAlbumCover(boolean z, boolean z2) {
        if (z) {
            if (this.blurredView.getVisibility() != 0 && !this.blurredAnimationInProgress) {
                this.blurredView.setTag(1);
                this.bigAlbumConver.setImageBitmap(this.coverContainer.getImageReceiver().getBitmap());
                this.blurredAnimationInProgress = true;
                View fragmentView = this.parentActivity.getActionBarLayout().fragmentsStack.get(this.parentActivity.getActionBarLayout().fragmentsStack.size() - 1).getFragmentView();
                int measuredWidth = (int) (((float) fragmentView.getMeasuredWidth()) / 6.0f);
                int measuredHeight = (int) (((float) fragmentView.getMeasuredHeight()) / 6.0f);
                Bitmap createBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                canvas.scale(0.16666667f, 0.16666667f);
                fragmentView.draw(canvas);
                canvas.translate((float) (this.containerView.getLeft() - getLeftInset()), 0.0f);
                this.containerView.draw(canvas);
                Utilities.stackBlurBitmap(createBitmap, Math.max(7, Math.max(measuredWidth, measuredHeight) / 180));
                this.blurredView.setBackground(new BitmapDrawable(createBitmap));
                this.blurredView.setVisibility(0);
                this.blurredView.animate().alpha(1.0f).setDuration(180).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        boolean unused = AudioPlayerAlert.this.blurredAnimationInProgress = false;
                    }
                }).start();
                this.bigAlbumConver.animate().scaleX(1.0f).scaleY(1.0f).setDuration(180).start();
            }
        } else if (this.blurredView.getVisibility() == 0) {
            this.blurredView.setTag((Object) null);
            if (z2) {
                this.blurredAnimationInProgress = true;
                this.blurredView.animate().alpha(0.0f).setDuration(180).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        AudioPlayerAlert.this.blurredView.setVisibility(4);
                        AudioPlayerAlert.this.bigAlbumConver.setImageBitmap((Bitmap) null);
                        boolean unused = AudioPlayerAlert.this.blurredAnimationInProgress = false;
                    }
                }).start();
                this.bigAlbumConver.animate().scaleX(0.9f).scaleY(0.9f).setDuration(180).start();
                return;
            }
            this.blurredView.setAlpha(0.0f);
            this.blurredView.setVisibility(4);
            this.bigAlbumConver.setImageBitmap((Bitmap) null);
            this.bigAlbumConver.setScaleX(0.9f);
            this.bigAlbumConver.setScaleY(0.9f);
        }
    }

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        AudioPlayerCell audioPlayerCell;
        MessageObject messageObject;
        AudioPlayerCell audioPlayerCell2;
        MessageObject messageObject2;
        MessageObject playingMessageObject;
        int i3 = NotificationCenter.messagePlayingDidStart;
        int i4 = 0;
        if (i == i3 || i == NotificationCenter.messagePlayingPlayStateChanged || i == NotificationCenter.messagePlayingDidReset) {
            int i5 = NotificationCenter.messagePlayingDidReset;
            updateTitle(i == i5 && objArr[1].booleanValue());
            if (i == i5 || i == NotificationCenter.messagePlayingPlayStateChanged) {
                int childCount = this.listView.getChildCount();
                for (int i6 = 0; i6 < childCount; i6++) {
                    View childAt = this.listView.getChildAt(i6);
                    if ((childAt instanceof AudioPlayerCell) && (messageObject = audioPlayerCell.getMessageObject()) != null && (messageObject.isVoice() || messageObject.isMusic())) {
                        (audioPlayerCell = (AudioPlayerCell) childAt).updateButtonState(false, true);
                    }
                }
            } else if (i == i3 && objArr[0].eventId == 0) {
                int childCount2 = this.listView.getChildCount();
                for (int i7 = 0; i7 < childCount2; i7++) {
                    View childAt2 = this.listView.getChildAt(i7);
                    if ((childAt2 instanceof AudioPlayerCell) && (messageObject2 = audioPlayerCell2.getMessageObject()) != null && (messageObject2.isVoice() || messageObject2.isMusic())) {
                        (audioPlayerCell2 = (AudioPlayerCell) childAt2).updateButtonState(false, true);
                    }
                }
            }
        } else if (i == NotificationCenter.messagePlayingProgressDidChanged) {
            MessageObject playingMessageObject2 = MediaController.getInstance().getPlayingMessageObject();
            if (playingMessageObject2 != null && playingMessageObject2.isMusic()) {
                updateProgress(playingMessageObject2);
            }
        } else if (i == NotificationCenter.musicDidLoad) {
            this.playlist = MediaController.getInstance().getPlaylist();
            this.listAdapter.notifyDataSetChanged();
        } else if (i == NotificationCenter.moreMusicDidLoad) {
            this.playlist = MediaController.getInstance().getPlaylist();
            this.listAdapter.notifyDataSetChanged();
            if (SharedConfig.playOrderReversed) {
                this.listView.stopScroll();
                int intValue = objArr[0].intValue();
                this.layoutManager.findFirstVisibleItemPosition();
                int findLastVisibleItemPosition = this.layoutManager.findLastVisibleItemPosition();
                if (findLastVisibleItemPosition != -1) {
                    View findViewByPosition = this.layoutManager.findViewByPosition(findLastVisibleItemPosition);
                    if (findViewByPosition != null) {
                        i4 = findViewByPosition.getTop();
                    }
                    this.layoutManager.scrollToPositionWithOffset(findLastVisibleItemPosition + intValue, i4);
                }
            }
        } else if (i == NotificationCenter.fileDidLoad) {
            if (objArr[0].equals(this.currentFile)) {
                updateTitle(false);
                this.currentAudioFinishedLoading = true;
            }
        } else if (i == NotificationCenter.FileLoadProgressChanged && objArr[0].equals(this.currentFile) && (playingMessageObject = MediaController.getInstance().getPlayingMessageObject()) != null) {
            Long l = objArr[1];
            Long l2 = objArr[2];
            float f = 1.0f;
            if (!this.currentAudioFinishedLoading) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                if (Math.abs(elapsedRealtime - this.lastBufferedPositionCheck) >= 500) {
                    if (MediaController.getInstance().isStreamingCurrentAudio()) {
                        f = FileLoader.getInstance(this.currentAccount).getBufferedProgressFromPosition(playingMessageObject.audioProgress, this.currentFile);
                    }
                    this.lastBufferedPositionCheck = elapsedRealtime;
                } else {
                    f = -1.0f;
                }
            }
            if (f != -1.0f) {
                this.seekBarView.setBufferedProgress(f);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateLayout() {
        if (this.listView.getChildCount() <= 0) {
            RecyclerListView recyclerListView = this.listView;
            int paddingTop = recyclerListView.getPaddingTop();
            this.scrollOffsetY = paddingTop;
            recyclerListView.setTopGlowOffset(paddingTop);
            this.containerView.invalidate();
            return;
        }
        View childAt = this.listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) this.listView.findContainingViewHolder(childAt);
        int top = childAt.getTop();
        int dp = AndroidUtilities.dp(7.0f);
        if (top < AndroidUtilities.dp(7.0f) || holder == null || holder.getAdapterPosition() != 0) {
            top = dp;
        }
        boolean z = top <= AndroidUtilities.dp(12.0f);
        if ((z && this.actionBar.getTag() == null) || (!z && this.actionBar.getTag() != null)) {
            this.actionBar.setTag(z ? 1 : null);
            AnimatorSet animatorSet = this.actionBarAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.actionBarAnimation = null;
            }
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.actionBarAnimation = animatorSet2;
            animatorSet2.setDuration(180);
            AnimatorSet animatorSet3 = this.actionBarAnimation;
            Animator[] animatorArr = new Animator[2];
            ActionBar actionBar2 = this.actionBar;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            float f = 1.0f;
            fArr[0] = z ? 1.0f : 0.0f;
            animatorArr[0] = ObjectAnimator.ofFloat(actionBar2, property, fArr);
            View view = this.actionBarShadow;
            Property property2 = View.ALPHA;
            float[] fArr2 = new float[1];
            if (!z) {
                f = 0.0f;
            }
            fArr2[0] = f;
            animatorArr[1] = ObjectAnimator.ofFloat(view, property2, fArr2);
            animatorSet3.playTogether(animatorArr);
            this.actionBarAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                }

                public void onAnimationCancel(Animator animator) {
                    AnimatorSet unused = AudioPlayerAlert.this.actionBarAnimation = null;
                }
            });
            this.actionBarAnimation.start();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.listView.getLayoutParams();
        int dp2 = top + (layoutParams.topMargin - AndroidUtilities.dp(11.0f));
        if (this.scrollOffsetY != dp2) {
            RecyclerListView recyclerListView2 = this.listView;
            this.scrollOffsetY = dp2;
            recyclerListView2.setTopGlowOffset(dp2 - layoutParams.topMargin);
            this.containerView.invalidate();
        }
    }

    public void dismiss() {
        super.dismiss();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.musicDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.moreMusicDidLoad);
        DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
    }

    public void onBackPressed() {
        ActionBar actionBar2 = this.actionBar;
        if (actionBar2 != null && actionBar2.isSearchFieldVisible()) {
            this.actionBar.closeSearchField();
        } else if (this.blurredView.getTag() != null) {
            showAlbumCover(false, true);
        } else {
            super.onBackPressed();
        }
    }

    public void onProgressDownload(String str, long j, long j2) {
        this.progressView.setProgress(Math.min(1.0f, ((float) j) / ((float) j2)), true);
    }

    public int getObserverTag() {
        return this.TAG;
    }

    private void updateRepeatButton() {
        int i = SharedConfig.repeatMode;
        if (i == 0 || i == 1) {
            if (SharedConfig.shuffleMusic) {
                if (i == 0) {
                    this.repeatButton.setIcon(NUM);
                } else {
                    this.repeatButton.setIcon(NUM);
                }
            } else if (!SharedConfig.playOrderReversed) {
                this.repeatButton.setIcon(NUM);
            } else if (i == 0) {
                this.repeatButton.setIcon(NUM);
            } else {
                this.repeatButton.setIcon(NUM);
            }
            if (i != 0 || SharedConfig.shuffleMusic || SharedConfig.playOrderReversed) {
                this.repeatButton.setTag("player_buttonActive");
                this.repeatButton.setIconColor(Theme.getColor("player_buttonActive"));
                Theme.setSelectorDrawableColor(this.repeatButton.getBackground(), NUM & Theme.getColor("player_buttonActive"), true);
                if (i != 0) {
                    this.repeatButton.setContentDescription(LocaleController.getString("AccDescrRepeatList", NUM));
                } else if (SharedConfig.shuffleMusic) {
                    this.repeatButton.setContentDescription(LocaleController.getString("ShuffleList", NUM));
                } else {
                    this.repeatButton.setContentDescription(LocaleController.getString("ReverseOrder", NUM));
                }
            } else {
                this.repeatButton.setTag("player_button");
                this.repeatButton.setIconColor(Theme.getColor("player_button"));
                Theme.setSelectorDrawableColor(this.repeatButton.getBackground(), Theme.getColor("listSelectorSDK21"), true);
                this.repeatButton.setContentDescription(LocaleController.getString("AccDescrRepeatOff", NUM));
            }
        } else if (i == 2) {
            this.repeatButton.setIcon(NUM);
            this.repeatButton.setTag("player_buttonActive");
            this.repeatButton.setIconColor(Theme.getColor("player_buttonActive"));
            Theme.setSelectorDrawableColor(this.repeatButton.getBackground(), NUM & Theme.getColor("player_buttonActive"), true);
            this.repeatButton.setContentDescription(LocaleController.getString("AccDescrRepeatOne", NUM));
        }
    }

    /* access modifiers changed from: private */
    public void updateProgress(MessageObject messageObject) {
        updateProgress(messageObject, false);
    }

    private void updateProgress(MessageObject messageObject, boolean z) {
        int i;
        SeekBarView seekBarView2 = this.seekBarView;
        if (seekBarView2 != null) {
            if (seekBarView2.isDragging()) {
                i = (int) (((float) messageObject.getDuration()) * this.seekBarView.getProgress());
            } else {
                this.seekBarView.setProgress(messageObject.audioProgress, z);
                float f = 1.0f;
                if (!this.currentAudioFinishedLoading) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (Math.abs(elapsedRealtime - this.lastBufferedPositionCheck) >= 500) {
                        if (MediaController.getInstance().isStreamingCurrentAudio()) {
                            f = FileLoader.getInstance(this.currentAccount).getBufferedProgressFromPosition(messageObject.audioProgress, this.currentFile);
                        }
                        this.lastBufferedPositionCheck = elapsedRealtime;
                    } else {
                        f = -1.0f;
                    }
                }
                if (f != -1.0f) {
                    this.seekBarView.setBufferedProgress(f);
                }
                i = messageObject.audioProgressSec;
            }
            if (this.lastTime != i) {
                this.lastTime = i;
                this.timeTextView.setText(AndroidUtilities.formatShortDuration(i));
            }
        }
    }

    private void checkIfMusicDownloaded(MessageObject messageObject) {
        String str = messageObject.messageOwner.attachPath;
        File file = null;
        if (str != null && str.length() > 0) {
            File file2 = new File(messageObject.messageOwner.attachPath);
            if (file2.exists()) {
                file = file2;
            }
        }
        if (file == null) {
            file = FileLoader.getPathToMessage(messageObject.messageOwner);
        }
        boolean z = SharedConfig.streamMedia && ((int) messageObject.getDialogId()) != 0 && messageObject.isMusic();
        if (file.exists() || z) {
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
            this.progressView.setVisibility(4);
            this.seekBarView.setVisibility(0);
            this.playButton.setEnabled(true);
            return;
        }
        String fileName = messageObject.getFileName();
        DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this);
        Float fileProgress = ImageLoader.getInstance().getFileProgress(fileName);
        this.progressView.setProgress(fileProgress != null ? fileProgress.floatValue() : 0.0f, false);
        this.progressView.setVisibility(0);
        this.seekBarView.setVisibility(4);
        this.playButton.setEnabled(false);
    }

    private void updateTitle(boolean z) {
        MessageObject playingMessageObject = MediaController.getInstance().getPlayingMessageObject();
        if ((playingMessageObject == null && z) || (playingMessageObject != null && !playingMessageObject.isMusic())) {
            dismiss();
        } else if (playingMessageObject == null) {
            this.lastMessageObject = null;
        } else {
            boolean z2 = playingMessageObject == this.lastMessageObject;
            this.lastMessageObject = playingMessageObject;
            if (playingMessageObject.eventId != 0 || playingMessageObject.getId() <= -NUM) {
                this.optionsButton.setVisibility(4);
            } else {
                this.optionsButton.setVisibility(0);
            }
            checkIfMusicDownloaded(playingMessageObject);
            updateProgress(playingMessageObject, !z2);
            updateCover(playingMessageObject, !z2);
            if (MediaController.getInstance().isMessagePaused()) {
                this.playPauseDrawable.setPause(false);
                this.playButton.setContentDescription(LocaleController.getString("AccActionPlay", NUM));
            } else {
                this.playPauseDrawable.setPause(true);
                this.playButton.setContentDescription(LocaleController.getString("AccActionPause", NUM));
            }
            String musicTitle = playingMessageObject.getMusicTitle();
            String musicAuthor = playingMessageObject.getMusicAuthor();
            this.titleTextView.setText(musicTitle);
            this.authorTextView.setText(musicAuthor);
            int duration = playingMessageObject.getDuration();
            this.lastDuration = duration;
            TextView textView = this.durationTextView;
            if (textView != null) {
                textView.setText(duration != 0 ? AndroidUtilities.formatShortDuration(duration) : "-:--");
            }
            if (duration > 1200) {
                this.playbackSpeedButton.setVisibility(0);
            } else {
                this.playbackSpeedButton.setVisibility(8);
            }
            if (!z2) {
                preloadNeighboringThumbs();
            }
        }
    }

    private void updateCover(MessageObject messageObject, boolean z) {
        CoverContainer coverContainer2 = this.coverContainer;
        BackupImageView nextImageView = z ? coverContainer2.getNextImageView() : coverContainer2.getImageView();
        AudioInfo audioInfo = MediaController.getInstance().getAudioInfo();
        if (audioInfo == null || audioInfo.getCover() == null) {
            this.currentFile = FileLoader.getAttachFileName(messageObject.getDocument());
            this.currentAudioFinishedLoading = false;
            String artworkUrl = messageObject.getArtworkUrl(false);
            ImageLocation artworkThumbImageLocation = getArtworkThumbImageLocation(messageObject);
            if (!TextUtils.isEmpty(artworkUrl)) {
                nextImageView.setImage(ImageLocation.getForPath(artworkUrl), (String) null, artworkThumbImageLocation, (String) null, (String) null, 0, 1, messageObject);
            } else if (artworkThumbImageLocation != null) {
                nextImageView.setImage((ImageLocation) null, (String) null, artworkThumbImageLocation, (String) null, (String) null, 0, 1, messageObject);
            } else {
                nextImageView.setImageDrawable((Drawable) null);
            }
            nextImageView.invalidate();
        } else {
            nextImageView.setImageBitmap(audioInfo.getCover());
            this.currentFile = null;
            this.currentAudioFinishedLoading = true;
        }
        if (z) {
            this.coverContainer.switchImageViews();
        }
    }

    private ImageLocation getArtworkThumbImageLocation(MessageObject messageObject) {
        TLRPC$Document document = messageObject.getDocument();
        TLRPC$PhotoSize closestPhotoSizeWithSize = document != null ? FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 240) : null;
        if (!(closestPhotoSizeWithSize instanceof TLRPC$TL_photoSize) && !(closestPhotoSizeWithSize instanceof TLRPC$TL_photoSizeProgressive)) {
            closestPhotoSizeWithSize = null;
        }
        if (closestPhotoSizeWithSize != null) {
            return ImageLocation.getForDocument(closestPhotoSizeWithSize, document);
        }
        String artworkUrl = messageObject.getArtworkUrl(true);
        if (artworkUrl != null) {
            return ImageLocation.getForPath(artworkUrl);
        }
        return null;
    }

    private void preloadNeighboringThumbs() {
        MediaController instance = MediaController.getInstance();
        ArrayList<MessageObject> playlist2 = instance.getPlaylist();
        if (playlist2.size() > 1) {
            ArrayList arrayList = new ArrayList();
            int playingMessageObjectNum = instance.getPlayingMessageObjectNum();
            int i = playingMessageObjectNum + 1;
            int i2 = playingMessageObjectNum - 1;
            if (i >= playlist2.size()) {
                i = 0;
            }
            if (i2 <= -1) {
                i2 = playlist2.size() - 1;
            }
            arrayList.add(playlist2.get(i));
            if (i != i2) {
                arrayList.add(playlist2.get(i2));
            }
            int size = arrayList.size();
            for (int i3 = 0; i3 < size; i3++) {
                MessageObject messageObject = (MessageObject) arrayList.get(i3);
                ImageLocation artworkThumbImageLocation = getArtworkThumbImageLocation(messageObject);
                if (artworkThumbImageLocation != null) {
                    if (artworkThumbImageLocation.path != null) {
                        ImageLoader.getInstance().preloadArtwork(artworkThumbImageLocation.path);
                    } else {
                        FileLoader.getInstance(this.currentAccount).loadFile(artworkThumbImageLocation, messageObject, (String) null, 0, 1);
                    }
                }
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context context;
        private ArrayList<MessageObject> searchResult = new ArrayList<>();
        private Runnable searchRunnable;

        public int getItemViewType(int i) {
            return 0;
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return true;
        }

        public ListAdapter(Context context2) {
            this.context = context2;
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (AudioPlayerAlert.this.playlist.size() > 1) {
                AudioPlayerAlert.this.playerLayout.setBackgroundColor(Theme.getColor("player_background"));
                AudioPlayerAlert.this.playerShadow.setVisibility(0);
                AudioPlayerAlert.this.listView.setPadding(0, AudioPlayerAlert.this.listView.getPaddingTop(), 0, AndroidUtilities.dp(179.0f));
            } else {
                AudioPlayerAlert.this.playerLayout.setBackground((Drawable) null);
                AudioPlayerAlert.this.playerShadow.setVisibility(4);
                AudioPlayerAlert.this.listView.setPadding(0, AudioPlayerAlert.this.listView.getPaddingTop(), 0, 0);
            }
            AudioPlayerAlert.this.updateEmptyView();
        }

        public int getItemCount() {
            if (AudioPlayerAlert.this.searchWas) {
                return this.searchResult.size();
            }
            if (AudioPlayerAlert.this.playlist.size() > 1) {
                return AudioPlayerAlert.this.playlist.size();
            }
            return 0;
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new RecyclerListView.Holder(new AudioPlayerCell(this.context, MediaController.getInstance().currentPlaylistIsGlobalSearch() ? 1 : 0));
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            AudioPlayerCell audioPlayerCell = (AudioPlayerCell) viewHolder.itemView;
            if (AudioPlayerAlert.this.searchWas) {
                audioPlayerCell.setMessageObject(this.searchResult.get(i));
            } else if (SharedConfig.playOrderReversed) {
                audioPlayerCell.setMessageObject((MessageObject) AudioPlayerAlert.this.playlist.get(i));
            } else {
                audioPlayerCell.setMessageObject((MessageObject) AudioPlayerAlert.this.playlist.get((AudioPlayerAlert.this.playlist.size() - i) - 1));
            }
        }

        public void search(String str) {
            if (this.searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(this.searchRunnable);
                this.searchRunnable = null;
            }
            if (str == null) {
                this.searchResult.clear();
                notifyDataSetChanged();
                return;
            }
            DispatchQueue dispatchQueue = Utilities.searchQueue;
            $$Lambda$AudioPlayerAlert$ListAdapter$elPV1Hm8_QILlr56__YDaAcLIcU r1 = new Runnable(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AudioPlayerAlert.ListAdapter.this.lambda$search$0$AudioPlayerAlert$ListAdapter(this.f$1);
                }
            };
            this.searchRunnable = r1;
            dispatchQueue.postRunnable(r1, 300);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$search$0 */
        public /* synthetic */ void lambda$search$0$AudioPlayerAlert$ListAdapter(String str) {
            this.searchRunnable = null;
            processSearch(str);
        }

        private void processSearch(String str) {
            AndroidUtilities.runOnUIThread(new Runnable(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AudioPlayerAlert.ListAdapter.this.lambda$processSearch$2$AudioPlayerAlert$ListAdapter(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$processSearch$2 */
        public /* synthetic */ void lambda$processSearch$2$AudioPlayerAlert$ListAdapter(String str) {
            Utilities.searchQueue.postRunnable(new Runnable(str, new ArrayList(AudioPlayerAlert.this.playlist)) {
                public final /* synthetic */ String f$1;
                public final /* synthetic */ ArrayList f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    AudioPlayerAlert.ListAdapter.this.lambda$null$1$AudioPlayerAlert$ListAdapter(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$1 */
        public /* synthetic */ void lambda$null$1$AudioPlayerAlert$ListAdapter(String str, ArrayList arrayList) {
            TLRPC$Document tLRPC$Document;
            boolean z;
            String str2;
            String lowerCase = str.trim().toLowerCase();
            if (lowerCase.length() == 0) {
                updateSearchResults(new ArrayList(), str);
                return;
            }
            String translitString = LocaleController.getInstance().getTranslitString(lowerCase);
            if (lowerCase.equals(translitString) || translitString.length() == 0) {
                translitString = null;
            }
            int i = (translitString != null ? 1 : 0) + 1;
            String[] strArr = new String[i];
            strArr[0] = lowerCase;
            if (translitString != null) {
                strArr[1] = translitString;
            }
            ArrayList arrayList2 = new ArrayList();
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                MessageObject messageObject = (MessageObject) arrayList.get(i2);
                int i3 = 0;
                while (true) {
                    if (i3 >= i) {
                        break;
                    }
                    String str3 = strArr[i3];
                    String documentName = messageObject.getDocumentName();
                    if (!(documentName == null || documentName.length() == 0)) {
                        if (documentName.toLowerCase().contains(str3)) {
                            arrayList2.add(messageObject);
                            break;
                        }
                        if (messageObject.type == 0) {
                            tLRPC$Document = messageObject.messageOwner.media.webpage.document;
                        } else {
                            tLRPC$Document = messageObject.messageOwner.media.document;
                        }
                        int i4 = 0;
                        while (true) {
                            if (i4 >= tLRPC$Document.attributes.size()) {
                                z = false;
                                break;
                            }
                            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i4);
                            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                                String str4 = tLRPC$DocumentAttribute.performer;
                                z = str4 != null ? str4.toLowerCase().contains(str3) : false;
                                if (!z && (str2 = tLRPC$DocumentAttribute.title) != null) {
                                    z = str2.toLowerCase().contains(str3);
                                }
                            } else {
                                i4++;
                            }
                        }
                        if (z) {
                            arrayList2.add(messageObject);
                            break;
                        }
                    }
                    i3++;
                }
            }
            updateSearchResults(arrayList2, str);
        }

        private void updateSearchResults(ArrayList<MessageObject> arrayList, String str) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList, str) {
                public final /* synthetic */ ArrayList f$1;
                public final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    AudioPlayerAlert.ListAdapter.this.lambda$updateSearchResults$3$AudioPlayerAlert$ListAdapter(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$updateSearchResults$3 */
        public /* synthetic */ void lambda$updateSearchResults$3$AudioPlayerAlert$ListAdapter(ArrayList arrayList, String str) {
            if (AudioPlayerAlert.this.searching) {
                boolean unused = AudioPlayerAlert.this.searchWas = true;
                this.searchResult = arrayList;
                notifyDataSetChanged();
                AudioPlayerAlert.this.layoutManager.scrollToPosition(0);
                AudioPlayerAlert.this.emptySubtitleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("NoAudioFoundPlayerInfo", NUM, str)));
            }
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        $$Lambda$AudioPlayerAlert$9MGdgP4Or_U1NzLzicZnU0UkhtE r10 = new ThemeDescription.ThemeDescriptionDelegate() {
            public final void didSetColor() {
                AudioPlayerAlert.this.lambda$getThemeDescriptions$11$AudioPlayerAlert();
            }
        };
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBar"));
        $$Lambda$AudioPlayerAlert$9MGdgP4Or_U1NzLzicZnU0UkhtE r8 = r10;
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBTITLECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarSelector"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inLoader"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_outLoader"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inLoaderSelected"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inMediaIcon"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inMediaIconSelected"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inAudioSelectedProgress"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AudioPlayerCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_inAudioProgress"));
        arrayList.add(new ThemeDescription(this.containerView, 0, (Class[]) null, (Paint) null, new Drawable[]{this.shadowDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBackground"));
        arrayList.add(new ThemeDescription(this.progressView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progressBackground"));
        arrayList.add(new ThemeDescription(this.progressView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progress"));
        arrayList.add(new ThemeDescription(this.seekBarView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progressBackground"));
        arrayList.add(new ThemeDescription(this.seekBarView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_player_progressCachedBackground"));
        arrayList.add(new ThemeDescription(this.seekBarView, ThemeDescription.FLAG_PROGRESSBAR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progress"));
        arrayList.add(new ThemeDescription(this.playbackSpeedButton, ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerPlayPause"));
        arrayList.add(new ThemeDescription(this.playbackSpeedButton, ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerClose"));
        arrayList.add(new ThemeDescription(this.repeatButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "player_button"));
        arrayList.add(new ThemeDescription(this.repeatButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "player_buttonActive"));
        arrayList.add(new ThemeDescription(this.repeatButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.repeatButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "actionBarDefaultSubmenuItem"));
        arrayList.add(new ThemeDescription(this.repeatButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "actionBarDefaultSubmenuBackground"));
        arrayList.add(new ThemeDescription(this.optionsButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "player_button"));
        arrayList.add(new ThemeDescription(this.optionsButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.optionsButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "actionBarDefaultSubmenuItem"));
        arrayList.add(new ThemeDescription(this.optionsButton, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "actionBarDefaultSubmenuBackground"));
        RLottieImageView rLottieImageView = this.prevButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView.getAnimatedDrawable()}, "Triangle 3", "player_button"));
        RLottieImageView rLottieImageView2 = this.prevButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView2, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView2.getAnimatedDrawable()}, "Triangle 4", "player_button"));
        RLottieImageView rLottieImageView3 = this.prevButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView3, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView3.getAnimatedDrawable()}, "Rectangle 4", "player_button"));
        arrayList.add(new ThemeDescription(this.prevButton, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.playButton, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_button"));
        arrayList.add(new ThemeDescription(this.playButton, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        RLottieImageView rLottieImageView4 = this.nextButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView4, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView4.getAnimatedDrawable()}, "Triangle 3", "player_button"));
        RLottieImageView rLottieImageView5 = this.nextButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView5, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView5.getAnimatedDrawable()}, "Triangle 4", "player_button"));
        RLottieImageView rLottieImageView6 = this.nextButton;
        arrayList.add(new ThemeDescription((View) rLottieImageView6, 0, (Class[]) null, new RLottieDrawable[]{rLottieImageView6.getAnimatedDrawable()}, "Rectangle 4", "player_button"));
        arrayList.add(new ThemeDescription(this.nextButton, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.playerLayout, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_background"));
        arrayList.add(new ThemeDescription(this.playerShadow, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogShadowLine"));
        arrayList.add(new ThemeDescription(this.emptyImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogEmptyImage"));
        arrayList.add(new ThemeDescription(this.emptyTitleTextView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogEmptyText"));
        arrayList.add(new ThemeDescription(this.emptySubtitleTextView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogEmptyText"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogScrollGlow"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
        arrayList.add(new ThemeDescription(this.progressView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "emptyListPlaceholder"));
        arrayList.add(new ThemeDescription(this.progressView, ThemeDescription.FLAG_PROGRESSBAR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "progressCircle"));
        arrayList.add(new ThemeDescription(this.durationTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription(this.timeTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription(this.titleTextView.getTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.titleTextView.getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription(this.authorTextView.getTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription(this.authorTextView.getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription(this.containerView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_sheet_scrollUp"));
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getThemeDescriptions$11 */
    public /* synthetic */ void lambda$getThemeDescriptions$11$AudioPlayerAlert() {
        this.searchItem.getSearchField().setCursorColor(Theme.getColor("player_actionBarTitle"));
        ActionBarMenuItem actionBarMenuItem = this.repeatButton;
        actionBarMenuItem.setIconColor(Theme.getColor((String) actionBarMenuItem.getTag()));
        Theme.setSelectorDrawableColor(this.repeatButton.getBackground(), Theme.getColor("listSelectorSDK21"), true);
        this.optionsButton.setIconColor(Theme.getColor("player_button"));
        Theme.setSelectorDrawableColor(this.optionsButton.getBackground(), Theme.getColor("listSelectorSDK21"), true);
        this.progressView.setBackgroundColor(Theme.getColor("player_progressBackground"));
        this.progressView.setProgressColor(Theme.getColor("player_progress"));
        updateSubMenu();
        this.repeatButton.redrawPopup(Theme.getColor("actionBarDefaultSubmenuBackground"));
        this.optionsButton.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItem"), false);
        this.optionsButton.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItem"), true);
        this.optionsButton.redrawPopup(Theme.getColor("actionBarDefaultSubmenuBackground"));
    }

    private static abstract class CoverContainer extends FrameLayout {
        private int activeIndex;
        private AnimatorSet animatorSet;
        private final BackupImageView[] imageViews = new BackupImageView[2];

        /* access modifiers changed from: protected */
        public abstract void onImageUpdated(ImageReceiver imageReceiver);

        public CoverContainer(Context context) {
            super(context);
            for (int i = 0; i < 2; i++) {
                this.imageViews[i] = new BackupImageView(context);
                this.imageViews[i].getImageReceiver().setDelegate(new ImageReceiver.ImageReceiverDelegate(i) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void didSetImage(ImageReceiver imageReceiver, boolean z, boolean z2, boolean z3) {
                        AudioPlayerAlert.CoverContainer.this.lambda$new$0$AudioPlayerAlert$CoverContainer(this.f$1, imageReceiver, z, z2, z3);
                    }

                    public /* synthetic */ void onAnimationReady(ImageReceiver imageReceiver) {
                        ImageReceiver.ImageReceiverDelegate.CC.$default$onAnimationReady(this, imageReceiver);
                    }
                });
                this.imageViews[i].setRoundRadius(AndroidUtilities.dp(4.0f));
                if (i == 1) {
                    this.imageViews[i].setVisibility(8);
                }
                addView(this.imageViews[i], LayoutHelper.createFrame(-1, -1.0f));
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$AudioPlayerAlert$CoverContainer(int i, ImageReceiver imageReceiver, boolean z, boolean z2, boolean z3) {
            if (i == this.activeIndex) {
                onImageUpdated(imageReceiver);
            }
        }

        public final void switchImageViews() {
            AnimatorSet animatorSet2 = this.animatorSet;
            if (animatorSet2 != null) {
                animatorSet2.cancel();
            }
            this.animatorSet = new AnimatorSet();
            int i = this.activeIndex == 0 ? 1 : 0;
            this.activeIndex = i;
            BackupImageView[] backupImageViewArr = this.imageViews;
            final BackupImageView backupImageView = backupImageViewArr[i == 0 ? (char) 1 : 0];
            BackupImageView backupImageView2 = backupImageViewArr[i];
            boolean hasBitmapImage = backupImageView.getImageReceiver().hasBitmapImage();
            backupImageView2.setAlpha(hasBitmapImage ? 1.0f : 0.0f);
            backupImageView2.setScaleX(0.8f);
            backupImageView2.setScaleY(0.8f);
            backupImageView2.setVisibility(0);
            if (hasBitmapImage) {
                backupImageView.bringToFront();
            } else {
                backupImageView.setVisibility(8);
                backupImageView.setImageDrawable((Drawable) null);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.8f, 1.0f});
            ofFloat.setDuration(125);
            ofFloat.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(hasBitmapImage) {
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AudioPlayerAlert.CoverContainer.lambda$switchImageViews$1(BackupImageView.this, this.f$1, valueAnimator);
                }
            });
            if (hasBitmapImage) {
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{backupImageView.getScaleX(), 0.8f});
                ofFloat2.setDuration(125);
                ofFloat2.setInterpolator(CubicBezierInterpolator.EASE_IN);
                ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(backupImageView2) {
                    public final /* synthetic */ BackupImageView f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        AudioPlayerAlert.CoverContainer.lambda$switchImageViews$2(BackupImageView.this, this.f$1, valueAnimator);
                    }
                });
                ofFloat2.addListener(new AnimatorListenerAdapter(this) {
                    public void onAnimationEnd(Animator animator) {
                        backupImageView.setVisibility(8);
                        backupImageView.setImageDrawable((Drawable) null);
                        backupImageView.setAlpha(1.0f);
                    }
                });
                this.animatorSet.playSequentially(new Animator[]{ofFloat2, ofFloat});
            } else {
                this.animatorSet.play(ofFloat);
            }
            this.animatorSet.start();
        }

        static /* synthetic */ void lambda$switchImageViews$1(BackupImageView backupImageView, boolean z, ValueAnimator valueAnimator) {
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            backupImageView.setScaleX(floatValue);
            backupImageView.setScaleY(floatValue);
            if (!z) {
                backupImageView.setAlpha(valueAnimator.getAnimatedFraction());
            }
        }

        static /* synthetic */ void lambda$switchImageViews$2(BackupImageView backupImageView, BackupImageView backupImageView2, ValueAnimator valueAnimator) {
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            backupImageView.setScaleX(floatValue);
            backupImageView.setScaleY(floatValue);
            float animatedFraction = valueAnimator.getAnimatedFraction();
            if (animatedFraction > 0.25f && !backupImageView2.getImageReceiver().hasBitmapImage()) {
                backupImageView.setAlpha(1.0f - ((animatedFraction - 0.25f) * 1.3333334f));
            }
        }

        public final BackupImageView getImageView() {
            return this.imageViews[this.activeIndex];
        }

        public final BackupImageView getNextImageView() {
            return this.imageViews[this.activeIndex == 0 ? (char) 1 : 0];
        }

        public final ImageReceiver getImageReceiver() {
            return getImageView().getImageReceiver();
        }
    }

    public static abstract class ClippingTextViewSwitcher extends FrameLayout {
        private int activeIndex;
        private AnimatorSet animatorSet;
        private final float[] clipProgress = {0.0f, 0.75f};
        private final Paint erasePaint;
        private final Matrix gradientMatrix;
        private final Paint gradientPaint;
        private LinearGradient gradientShader;
        private final int gradientSize = AndroidUtilities.dp(24.0f);
        /* access modifiers changed from: private */
        public final TextView[] textViews = new TextView[2];

        /* access modifiers changed from: protected */
        public abstract TextView createTextView();

        public ClippingTextViewSwitcher(Context context) {
            super(context);
            for (int i = 0; i < 2; i++) {
                this.textViews[i] = createTextView();
                if (i == 1) {
                    this.textViews[i].setAlpha(0.0f);
                    this.textViews[i].setVisibility(8);
                }
                addView(this.textViews[i], LayoutHelper.createFrame(-2, -1.0f));
            }
            this.gradientMatrix = new Matrix();
            Paint paint = new Paint(1);
            this.gradientPaint = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            Paint paint2 = new Paint(1);
            this.erasePaint = paint2;
            paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        /* access modifiers changed from: protected */
        public void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            LinearGradient linearGradient = new LinearGradient((float) this.gradientSize, 0.0f, 0.0f, 0.0f, 0, -16777216, Shader.TileMode.CLAMP);
            this.gradientShader = linearGradient;
            this.gradientPaint.setShader(linearGradient);
        }

        /* access modifiers changed from: protected */
        public boolean drawChild(Canvas canvas, View view, long j) {
            char c = 0;
            if (view != this.textViews[0]) {
                c = 1;
            }
            if (this.clipProgress[c] > 0.0f) {
                float width = (float) view.getWidth();
                float height = (float) view.getHeight();
                int saveLayer = canvas.saveLayer(0.0f, 0.0f, width, height, (Paint) null, 31);
                boolean drawChild = super.drawChild(canvas, view, j);
                float f = width * (1.0f - this.clipProgress[c]);
                float f2 = ((float) this.gradientSize) + f;
                this.gradientMatrix.setTranslate(f, 0.0f);
                this.gradientShader.setLocalMatrix(this.gradientMatrix);
                canvas.drawRect(f, 0.0f, f2, height, this.gradientPaint);
                if (width > f2) {
                    canvas.drawRect(f2, 0.0f, width, height, this.erasePaint);
                }
                canvas.restoreToCount(saveLayer);
                return drawChild;
            }
            Canvas canvas2 = canvas;
            return super.drawChild(canvas, view, j);
        }

        public void setText(CharSequence charSequence) {
            CharSequence text = this.textViews[this.activeIndex].getText();
            if (TextUtils.isEmpty(text)) {
                this.textViews[this.activeIndex].setText(charSequence);
            } else if (!TextUtils.equals(charSequence, text)) {
                final int i = this.activeIndex;
                int i2 = i == 0 ? 1 : 0;
                this.activeIndex = i2;
                AnimatorSet animatorSet2 = this.animatorSet;
                if (animatorSet2 != null) {
                    animatorSet2.cancel();
                }
                AnimatorSet animatorSet3 = new AnimatorSet();
                this.animatorSet = animatorSet3;
                animatorSet3.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        ClippingTextViewSwitcher.this.textViews[i].setVisibility(8);
                    }
                });
                this.textViews[i2].setText(charSequence);
                this.textViews[i2].bringToFront();
                this.textViews[i2].setVisibility(0);
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.clipProgress[i], 0.75f});
                ofFloat.setDuration(200);
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(i) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        AudioPlayerAlert.ClippingTextViewSwitcher.this.lambda$setText$0$AudioPlayerAlert$ClippingTextViewSwitcher(this.f$1, valueAnimator);
                    }
                });
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{this.clipProgress[i2], 0.0f});
                ofFloat2.setStartDelay(100);
                ofFloat2.setDuration(200);
                ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(i2) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        AudioPlayerAlert.ClippingTextViewSwitcher.this.lambda$setText$1$AudioPlayerAlert$ClippingTextViewSwitcher(this.f$1, valueAnimator);
                    }
                });
                ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.textViews[i], View.ALPHA, new float[]{0.0f});
                ofFloat3.setStartDelay(75);
                ofFloat3.setDuration(150);
                ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(this.textViews[i2], View.ALPHA, new float[]{1.0f});
                ofFloat4.setStartDelay(75);
                ofFloat4.setDuration(150);
                this.animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4});
                this.animatorSet.start();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setText$0 */
        public /* synthetic */ void lambda$setText$0$AudioPlayerAlert$ClippingTextViewSwitcher(int i, ValueAnimator valueAnimator) {
            this.clipProgress[i] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setText$1 */
        public /* synthetic */ void lambda$setText$1$AudioPlayerAlert$ClippingTextViewSwitcher(int i, ValueAnimator valueAnimator) {
            this.clipProgress[i] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }

        public TextView getTextView() {
            return this.textViews[this.activeIndex];
        }

        public TextView getNextTextView() {
            return this.textViews[this.activeIndex == 0 ? (char) 1 : 0];
        }
    }
}
