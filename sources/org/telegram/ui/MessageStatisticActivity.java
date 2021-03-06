package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LruCache;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.QuickAckDelegate;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$MessageFwdHeader;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC$TL_messages_messagesSlice;
import org.telegram.tgnet.TLRPC$TL_statsGraph;
import org.telegram.tgnet.TLRPC$TL_statsGraphError;
import org.telegram.tgnet.TLRPC$TL_stats_getMessagePublicForwards;
import org.telegram.tgnet.TLRPC$TL_stats_getMessageStats;
import org.telegram.tgnet.TLRPC$TL_stats_loadAsyncGraph;
import org.telegram.tgnet.TLRPC$TL_stats_messageStats;
import org.telegram.tgnet.TLRPC$messages_Messages;
import org.telegram.tgnet.WriteToSocketDelegate;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Charts.BaseChartView;
import org.telegram.ui.Charts.data.ChartData;
import org.telegram.ui.Charts.view_data.ChartHeaderView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.StatisticActivity;

public class MessageStatisticActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    /* access modifiers changed from: private */
    public ChatAvatarContainer avatarContainer;
    /* access modifiers changed from: private */
    public TLRPC$ChatFull chat;
    private final int chatId;
    /* access modifiers changed from: private */
    public LruCache<ChartData> childDataCache = new LruCache<>(15);
    boolean drawPlay;
    /* access modifiers changed from: private */
    public int emptyRow;
    private EmptyTextProgressView emptyView;
    /* access modifiers changed from: private */
    public boolean endReached;
    /* access modifiers changed from: private */
    public int endRow;
    private boolean firstLoaded;
    /* access modifiers changed from: private */
    public int headerRow;
    private RLottieImageView imageView;
    /* access modifiers changed from: private */
    public int interactionsChartRow;
    /* access modifiers changed from: private */
    public StatisticActivity.ChartViewData interactionsViewData;
    /* access modifiers changed from: private */
    public StatisticActivity.ZoomCancelable lastCancelable;
    /* access modifiers changed from: private */
    public LinearLayoutManager layoutManager;
    private FrameLayout listContainer;
    /* access modifiers changed from: private */
    public RecyclerListView listView;
    private ListAdapter listViewAdapter;
    /* access modifiers changed from: private */
    public boolean loading;
    /* access modifiers changed from: private */
    public int loadingRow;
    private final int messageId;
    /* access modifiers changed from: private */
    public MessageObject messageObject;
    /* access modifiers changed from: private */
    public ArrayList<TLRPC$Message> messages = new ArrayList<>();
    private int nextRate;
    /* access modifiers changed from: private */
    public int overviewHeaderRow;
    /* access modifiers changed from: private */
    public int overviewRow;
    /* access modifiers changed from: private */
    public LinearLayout progressLayout;
    /* access modifiers changed from: private */
    public int publicChats;
    /* access modifiers changed from: private */
    public int rowCount;
    ArraySet<Integer> shadowDivideCells = new ArraySet<>();
    /* access modifiers changed from: private */
    public BaseChartView.SharedUiComponents sharedUi;
    private final Runnable showProgressbar = new Runnable() {
        public void run() {
            MessageStatisticActivity.this.progressLayout.animate().alpha(1.0f).setDuration(230);
        }
    };
    /* access modifiers changed from: private */
    public int startRow;
    /* access modifiers changed from: private */
    public boolean statsLoaded;
    ImageReceiver thumbImage;

    public MessageStatisticActivity(MessageObject messageObject2) {
        this.messageObject = messageObject2;
        if (messageObject2.messageOwner.fwd_from == null) {
            this.chatId = messageObject2.getChatId();
            this.messageId = this.messageObject.getId();
        } else {
            this.chatId = -messageObject2.getFromChatId();
            this.messageId = this.messageObject.messageOwner.fwd_msg_id;
        }
        this.chat = getMessagesController().getChatFull(this.chatId);
    }

    private void updateRows() {
        this.shadowDivideCells.clear();
        this.headerRow = -1;
        this.startRow = -1;
        this.endRow = -1;
        this.loadingRow = -1;
        this.interactionsChartRow = -1;
        this.overviewHeaderRow = -1;
        this.overviewRow = -1;
        this.rowCount = 0;
        if (this.firstLoaded && this.statsLoaded) {
            AndroidUtilities.cancelRunOnUIThread(this.showProgressbar);
            if (this.listContainer.getVisibility() == 8) {
                this.progressLayout.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        MessageStatisticActivity.this.progressLayout.setVisibility(8);
                    }
                });
                this.listContainer.setVisibility(0);
                this.listContainer.setAlpha(0.0f);
                this.listContainer.animate().alpha(1.0f).start();
            }
            int i = this.rowCount;
            int i2 = i + 1;
            this.rowCount = i2;
            this.overviewHeaderRow = i;
            int i3 = i2 + 1;
            this.rowCount = i3;
            this.overviewRow = i2;
            ArraySet<Integer> arraySet = this.shadowDivideCells;
            this.rowCount = i3 + 1;
            arraySet.add(Integer.valueOf(i3));
            if (this.interactionsViewData != null) {
                int i4 = this.rowCount;
                int i5 = i4 + 1;
                this.rowCount = i5;
                this.interactionsChartRow = i4;
                ArraySet<Integer> arraySet2 = this.shadowDivideCells;
                this.rowCount = i5 + 1;
                arraySet2.add(Integer.valueOf(i5));
            }
            if (!this.messages.isEmpty()) {
                int i6 = this.rowCount;
                int i7 = i6 + 1;
                this.rowCount = i7;
                this.headerRow = i6;
                this.startRow = i7;
                int size = i7 + this.messages.size();
                this.rowCount = size;
                this.endRow = size;
                int i8 = size + 1;
                this.rowCount = i8;
                this.emptyRow = size;
                ArraySet<Integer> arraySet3 = this.shadowDivideCells;
                this.rowCount = i8 + 1;
                arraySet3.add(Integer.valueOf(i8));
                if (!this.endReached) {
                    int i9 = this.rowCount;
                    this.rowCount = i9 + 1;
                    this.loadingRow = i9;
                }
            }
        }
        ListAdapter listAdapter = this.listViewAdapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (this.chat != null) {
            loadStat();
            loadChats(100);
        } else {
            MessagesController.getInstance(this.currentAccount).loadFullChat(this.chatId, this.classGuid, true);
        }
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoad);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoad);
    }

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        if (i == NotificationCenter.chatInfoDidLoad) {
            TLRPC$ChatFull tLRPC$ChatFull = objArr[0];
            if (this.chat == null && tLRPC$ChatFull.id == this.chatId) {
                TLRPC$Chat chat2 = getMessagesController().getChat(Integer.valueOf(this.chatId));
                if (chat2 != null) {
                    this.avatarContainer.setChatAvatar(chat2);
                    this.avatarContainer.setTitle(chat2.title);
                }
                this.chat = tLRPC$ChatFull;
                loadStat();
                loadChats(100);
                updateMenu();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x02d4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x02d9  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0314  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0334  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.view.View createView(android.content.Context r21) {
        /*
            r20 = this;
            r0 = r20
            r1 = r21
            org.telegram.ui.ActionBar.ActionBar r2 = r0.actionBar
            r3 = 2131165468(0x7var_c, float:1.7945154E38)
            r2.setBackButtonImage(r3)
            android.widget.FrameLayout r2 = new android.widget.FrameLayout
            r2.<init>(r1)
            r0.fragmentView = r2
            java.lang.String r3 = "windowBackgroundGray"
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            r2.setBackgroundColor(r3)
            android.view.View r2 = r0.fragmentView
            android.widget.FrameLayout r2 = (android.widget.FrameLayout) r2
            org.telegram.ui.Components.EmptyTextProgressView r3 = new org.telegram.ui.Components.EmptyTextProgressView
            r3.<init>(r1)
            r0.emptyView = r3
            java.lang.String r4 = "NoResult"
            r5 = 2131626270(0x7f0e091e, float:1.8879771E38)
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r4, r5)
            r3.setText(r4)
            org.telegram.ui.Components.EmptyTextProgressView r3 = r0.emptyView
            r4 = 8
            r3.setVisibility(r4)
            android.widget.LinearLayout r3 = new android.widget.LinearLayout
            r3.<init>(r1)
            r0.progressLayout = r3
            r5 = 1
            r3.setOrientation(r5)
            org.telegram.ui.Components.RLottieImageView r3 = new org.telegram.ui.Components.RLottieImageView
            r3.<init>(r1)
            r0.imageView = r3
            r3.setAutoRepeat(r5)
            org.telegram.ui.Components.RLottieImageView r3 = r0.imageView
            r6 = 2131558475(0x7f0d004b, float:1.8742267E38)
            r7 = 120(0x78, float:1.68E-43)
            r3.setAnimation(r6, r7, r7)
            org.telegram.ui.Components.RLottieImageView r3 = r0.imageView
            r3.playAnimation()
            android.widget.TextView r3 = new android.widget.TextView
            r3.<init>(r1)
            r6 = 1101004800(0x41a00000, float:20.0)
            r3.setTextSize(r5, r6)
            java.lang.String r6 = "fonts/rmedium.ttf"
            android.graphics.Typeface r6 = org.telegram.messenger.AndroidUtilities.getTypeface(r6)
            r3.setTypeface(r6)
            java.lang.String r6 = "player_actionBarTitle"
            int r7 = org.telegram.ui.ActionBar.Theme.getColor(r6)
            r3.setTextColor(r7)
            r3.setTag(r6)
            java.lang.String r7 = "LoadingStats"
            r8 = 2131625958(0x7f0e07e6, float:1.8879139E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r7, r8)
            r3.setText(r7)
            r3.setGravity(r5)
            android.widget.TextView r7 = new android.widget.TextView
            r7.<init>(r1)
            r8 = 1097859072(0x41700000, float:15.0)
            r7.setTextSize(r5, r8)
            java.lang.String r8 = "player_actionBarSubtitle"
            int r9 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r7.setTextColor(r9)
            r7.setTag(r8)
            java.lang.String r9 = "LoadingStatsDescription"
            r10 = 2131625959(0x7f0e07e7, float:1.887914E38)
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r9, r10)
            r7.setText(r9)
            r7.setGravity(r5)
            android.widget.LinearLayout r9 = r0.progressLayout
            org.telegram.ui.Components.RLottieImageView r10 = r0.imageView
            r11 = 120(0x78, float:1.68E-43)
            r12 = 120(0x78, float:1.68E-43)
            r13 = 1
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 20
            android.widget.LinearLayout$LayoutParams r11 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r11, (int) r12, (int) r13, (int) r14, (int) r15, (int) r16, (int) r17)
            r9.addView(r10, r11)
            android.widget.LinearLayout r9 = r0.progressLayout
            r10 = -2
            r11 = -2
            r12 = 1
            r13 = 0
            r16 = 10
            android.widget.LinearLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r10, (int) r11, (int) r12, (int) r13, (int) r14, (int) r15, (int) r16)
            r9.addView(r3, r10)
            android.widget.LinearLayout r3 = r0.progressLayout
            r9 = -2
            android.widget.LinearLayout$LayoutParams r9 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r9, (int) r9, (int) r5)
            r3.addView(r7, r9)
            android.widget.LinearLayout r3 = r0.progressLayout
            r7 = 0
            r3.setAlpha(r7)
            android.widget.LinearLayout r3 = r0.progressLayout
            r9 = 240(0xf0, float:3.36E-43)
            r10 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r11 = 17
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 1106247680(0x41var_, float:30.0)
            android.widget.FrameLayout$LayoutParams r9 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r10, r11, r12, r13, r14, r15)
            r2.addView(r3, r9)
            org.telegram.ui.Components.RecyclerListView r3 = new org.telegram.ui.Components.RecyclerListView
            r3.<init>(r1)
            r0.listView = r3
            androidx.recyclerview.widget.LinearLayoutManager r9 = new androidx.recyclerview.widget.LinearLayoutManager
            r10 = 0
            r9.<init>(r1, r5, r10)
            r0.layoutManager = r9
            r3.setLayoutManager(r9)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            androidx.recyclerview.widget.RecyclerView$ItemAnimator r3 = r3.getItemAnimator()
            androidx.recyclerview.widget.SimpleItemAnimator r3 = (androidx.recyclerview.widget.SimpleItemAnimator) r3
            r3.setSupportsChangeAnimations(r10)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.MessageStatisticActivity$ListAdapter r9 = new org.telegram.ui.MessageStatisticActivity$ListAdapter
            r9.<init>(r1)
            r0.listViewAdapter = r9
            r3.setAdapter(r9)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            boolean r9 = org.telegram.messenger.LocaleController.isRTL
            if (r9 == 0) goto L_0x012c
            r9 = 1
            goto L_0x012d
        L_0x012c:
            r9 = 2
        L_0x012d:
            r3.setVerticalScrollbarPosition(r9)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.-$$Lambda$MessageStatisticActivity$Ky0VtRlsC1WIfR2bZGAK8obuLKU r9 = new org.telegram.ui.-$$Lambda$MessageStatisticActivity$Ky0VtRlsC1WIfR2bZGAK8obuLKU
            r9.<init>()
            r3.setOnItemClickListener((org.telegram.ui.Components.RecyclerListView.OnItemClickListener) r9)
            org.telegram.ui.Components.RecyclerListView r3 = r0.listView
            org.telegram.ui.MessageStatisticActivity$3 r9 = new org.telegram.ui.MessageStatisticActivity$3
            r9.<init>()
            r3.setOnScrollListener(r9)
            org.telegram.ui.Components.EmptyTextProgressView r3 = r0.emptyView
            r3.showTextView()
            android.widget.FrameLayout r3 = new android.widget.FrameLayout
            r3.<init>(r1)
            r0.listContainer = r3
            org.telegram.ui.Components.RecyclerListView r9 = r0.listView
            r11 = -1
            r12 = -1082130432(0xffffffffbvar_, float:-1.0)
            android.widget.FrameLayout$LayoutParams r13 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r12)
            r3.addView(r9, r13)
            android.widget.FrameLayout r3 = r0.listContainer
            org.telegram.ui.Components.EmptyTextProgressView r9 = r0.emptyView
            android.widget.FrameLayout$LayoutParams r13 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r12)
            r3.addView(r9, r13)
            android.widget.FrameLayout r3 = r0.listContainer
            r3.setVisibility(r4)
            android.widget.FrameLayout r3 = r0.listContainer
            android.widget.FrameLayout$LayoutParams r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r12)
            r2.addView(r3, r4)
            java.lang.Runnable r2 = r0.showProgressbar
            r3 = 300(0x12c, double:1.48E-321)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r2, r3)
            r20.updateRows()
            org.telegram.ui.Components.RecyclerListView r2 = r0.listView
            org.telegram.ui.Components.EmptyTextProgressView r3 = r0.emptyView
            r2.setEmptyView(r3)
            org.telegram.ui.MessageStatisticActivity$4 r2 = new org.telegram.ui.MessageStatisticActivity$4
            r3 = 0
            r2.<init>(r1, r3, r10)
            r0.avatarContainer = r2
            org.telegram.messenger.ImageReceiver r1 = new org.telegram.messenger.ImageReceiver
            r1.<init>()
            r0.thumbImage = r1
            org.telegram.ui.Components.ChatAvatarContainer r2 = r0.avatarContainer
            r1.setParentView(r2)
            org.telegram.messenger.ImageReceiver r1 = r0.thumbImage
            r2 = 1073741824(0x40000000, float:2.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r1.setRoundRadius((int) r2)
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            org.telegram.ui.Components.ChatAvatarContainer r2 = r0.avatarContainer
            r11 = -2
            r13 = 51
            boolean r4 = r0.inPreviewMode
            if (r4 != 0) goto L_0x01b5
            r7 = 1113587712(0x42600000, float:56.0)
            r14 = 1113587712(0x42600000, float:56.0)
            goto L_0x01b6
        L_0x01b5:
            r14 = 0
        L_0x01b6:
            r15 = 0
            r16 = 1109393408(0x42200000, float:40.0)
            r17 = 0
            android.widget.FrameLayout$LayoutParams r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r12, r13, r14, r15, r16, r17)
            r1.addView(r2, r10, r4)
            org.telegram.messenger.MessagesController r1 = r20.getMessagesController()
            int r2 = r0.chatId
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r1 = r1.getChat(r2)
            if (r1 == 0) goto L_0x01de
            org.telegram.ui.Components.ChatAvatarContainer r2 = r0.avatarContainer
            r2.setChatAvatar(r1)
            org.telegram.ui.Components.ChatAvatarContainer r2 = r0.avatarContainer
            java.lang.String r1 = r1.title
            r2.setTitle(r1)
        L_0x01de:
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            boolean r1 = r1.needDrawBluredPreview()
            if (r1 != 0) goto L_0x02c9
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            boolean r1 = r1.isPhoto()
            if (r1 != 0) goto L_0x01fe
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            boolean r1 = r1.isNewGif()
            if (r1 != 0) goto L_0x01fe
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            boolean r1 = r1.isVideo()
            if (r1 == 0) goto L_0x02c9
        L_0x01fe:
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            boolean r1 = r1.isWebpage()
            if (r1 == 0) goto L_0x0211
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            org.telegram.tgnet.TLRPC$WebPage r1 = r1.webpage
            java.lang.String r1 = r1.type
            goto L_0x0212
        L_0x0211:
            r1 = r3
        L_0x0212:
            java.lang.String r2 = "app"
            boolean r2 = r2.equals(r1)
            if (r2 != 0) goto L_0x02c9
            java.lang.String r2 = "profile"
            boolean r2 = r2.equals(r1)
            if (r2 != 0) goto L_0x02c9
            java.lang.String r2 = "article"
            boolean r2 = r2.equals(r1)
            if (r2 != 0) goto L_0x02c9
            if (r1 == 0) goto L_0x0234
            java.lang.String r2 = "telegram_"
            boolean r1 = r1.startsWith(r2)
            if (r1 != 0) goto L_0x02c9
        L_0x0234:
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r1 = r1.photoThumbs
            r2 = 40
            org.telegram.tgnet.TLRPC$PhotoSize r1 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r1, r2)
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r2 = r2.photoThumbs
            int r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize()
            org.telegram.tgnet.TLRPC$PhotoSize r2 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r2, r4)
            if (r1 != r2) goto L_0x024d
            goto L_0x024e
        L_0x024d:
            r3 = r2
        L_0x024e:
            if (r1 == 0) goto L_0x02c9
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            boolean r2 = r2.isVideo()
            r0.drawPlay = r2
            java.lang.String r2 = org.telegram.messenger.FileLoader.getAttachFileName(r3)
            org.telegram.messenger.MessageObject r4 = r0.messageObject
            boolean r4 = r4.mediaExists
            if (r4 != 0) goto L_0x0297
            int r4 = r0.currentAccount
            org.telegram.messenger.DownloadController r4 = org.telegram.messenger.DownloadController.getInstance(r4)
            org.telegram.messenger.MessageObject r7 = r0.messageObject
            boolean r4 = r4.canDownloadMedia((org.telegram.messenger.MessageObject) r7)
            if (r4 != 0) goto L_0x0297
            int r4 = r0.currentAccount
            org.telegram.messenger.FileLoader r4 = org.telegram.messenger.FileLoader.getInstance(r4)
            boolean r2 = r4.isLoadingFile(r2)
            if (r2 == 0) goto L_0x027d
            goto L_0x0297
        L_0x027d:
            org.telegram.messenger.ImageReceiver r11 = r0.thumbImage
            r12 = 0
            r13 = 0
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            org.telegram.tgnet.TLObject r2 = r2.photoThumbsObject
            org.telegram.messenger.ImageLocation r14 = org.telegram.messenger.ImageLocation.getForObject(r1, r2)
            r16 = 0
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            r18 = 0
            java.lang.String r15 = "20_20"
            r17 = r1
            r11.setImage((org.telegram.messenger.ImageLocation) r12, (java.lang.String) r13, (org.telegram.messenger.ImageLocation) r14, (java.lang.String) r15, (java.lang.String) r16, (java.lang.Object) r17, (int) r18)
            goto L_0x02c7
        L_0x0297:
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            int r4 = r2.type
            if (r4 != r5) goto L_0x02a6
            if (r3 == 0) goto L_0x02a2
            int r4 = r3.size
            goto L_0x02a3
        L_0x02a2:
            r4 = 0
        L_0x02a3:
            r16 = r4
            goto L_0x02a8
        L_0x02a6:
            r16 = 0
        L_0x02a8:
            org.telegram.messenger.ImageReceiver r11 = r0.thumbImage
            org.telegram.tgnet.TLObject r2 = r2.photoThumbsObject
            org.telegram.messenger.ImageLocation r12 = org.telegram.messenger.ImageLocation.getForObject(r3, r2)
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            org.telegram.tgnet.TLObject r2 = r2.photoThumbsObject
            org.telegram.messenger.ImageLocation r14 = org.telegram.messenger.ImageLocation.getForObject(r1, r2)
            r17 = 0
            org.telegram.messenger.MessageObject r1 = r0.messageObject
            r19 = 0
            java.lang.String r13 = "20_20"
            java.lang.String r15 = "20_20"
            r18 = r1
            r11.setImage(r12, r13, r14, r15, r16, r17, r18, r19)
        L_0x02c7:
            r1 = 1
            goto L_0x02ca
        L_0x02c9:
            r1 = 0
        L_0x02ca:
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.lang.CharSequence r2 = r2.caption
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x02d9
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.lang.CharSequence r2 = r2.caption
            goto L_0x0312
        L_0x02d9:
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            java.lang.String r2 = r2.message
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x030e
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.lang.CharSequence r2 = r2.messageText
            int r3 = r2.length()
            r4 = 150(0x96, float:2.1E-43)
            if (r3 <= r4) goto L_0x02f5
            java.lang.CharSequence r2 = r2.subSequence(r10, r4)
        L_0x02f5:
            org.telegram.ui.Components.ChatAvatarContainer r3 = r0.avatarContainer
            org.telegram.ui.ActionBar.SimpleTextView r3 = r3.getSubtitleTextView()
            android.text.TextPaint r3 = r3.getTextPaint()
            android.graphics.Paint$FontMetricsInt r3 = r3.getFontMetricsInt()
            r4 = 1099431936(0x41880000, float:17.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            java.lang.CharSequence r2 = org.telegram.messenger.Emoji.replaceEmoji(r2, r3, r4, r10)
            goto L_0x0312
        L_0x030e:
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.lang.CharSequence r2 = r2.messageText
        L_0x0312:
            if (r1 == 0) goto L_0x0334
            android.text.SpannableStringBuilder r1 = new android.text.SpannableStringBuilder
            r1.<init>(r2)
            java.lang.String r2 = " "
            r1.insert(r10, r2)
            org.telegram.ui.Cells.DialogCell$FixedWidthSpan r2 = new org.telegram.ui.Cells.DialogCell$FixedWidthSpan
            r3 = 1103101952(0x41CLASSNAME, float:24.0)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            r2.<init>(r3)
            r3 = 33
            r1.setSpan(r2, r10, r5, r3)
            org.telegram.ui.Components.ChatAvatarContainer r2 = r0.avatarContainer
            r2.setSubtitle(r1)
            goto L_0x033d
        L_0x0334:
            org.telegram.ui.Components.ChatAvatarContainer r1 = r0.avatarContainer
            org.telegram.messenger.MessageObject r2 = r0.messageObject
            java.lang.CharSequence r2 = r2.messageText
            r1.setSubtitle(r2)
        L_0x033d:
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            org.telegram.ui.ActionBar.BackDrawable r2 = new org.telegram.ui.ActionBar.BackDrawable
            r2.<init>(r10)
            r1.setBackButtonDrawable(r2)
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            org.telegram.ui.MessageStatisticActivity$5 r2 = new org.telegram.ui.MessageStatisticActivity$5
            r2.<init>()
            r1.setActionBarMenuOnItemClick(r2)
            org.telegram.ui.Components.ChatAvatarContainer r1 = r0.avatarContainer
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r6)
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r1.setTitleColors(r2, r3)
            org.telegram.ui.Components.ChatAvatarContainer r1 = r0.avatarContainer
            org.telegram.ui.ActionBar.SimpleTextView r1 = r1.getSubtitleTextView()
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            r1.setLinkTextColor(r2)
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            java.lang.String r2 = "windowBackgroundWhiteGrayText2"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setItemsColor(r2, r10)
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            java.lang.String r2 = "actionBarActionModeDefaultSelector"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setItemsBackgroundColor(r2, r10)
            org.telegram.ui.ActionBar.ActionBar r1 = r0.actionBar
            java.lang.String r2 = "windowBackgroundWhite"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setBackgroundColor(r2)
            org.telegram.ui.Components.ChatAvatarContainer r1 = r0.avatarContainer
            org.telegram.ui.-$$Lambda$MessageStatisticActivity$VYOvV2JHOA9lhRHpvnHNpNV2VlU r2 = new org.telegram.ui.-$$Lambda$MessageStatisticActivity$VYOvV2JHOA9lhRHpvnHNpNV2VlU
            r2.<init>()
            r1.setOnClickListener(r2)
            r20.updateMenu()
            android.view.View r1 = r0.fragmentView
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.MessageStatisticActivity.createView(android.content.Context):android.view.View");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$0 */
    public /* synthetic */ void lambda$createView$0$MessageStatisticActivity(View view, int i) {
        int i2 = this.startRow;
        if (i >= i2 && i < this.endRow) {
            TLRPC$Message tLRPC$Message = this.messages.get(i - i2);
            int dialogId = (int) MessageObject.getDialogId(tLRPC$Message);
            Bundle bundle = new Bundle();
            if (dialogId > 0) {
                bundle.putInt("user_id", dialogId);
            } else {
                bundle.putInt("chat_id", -dialogId);
            }
            bundle.putInt("message_id", tLRPC$Message.id);
            bundle.putBoolean("need_remove_previous_same_chat_activity", false);
            if (getMessagesController().checkCanOpenChat(bundle, this)) {
                presentFragment(new ChatActivity(bundle));
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$1 */
    public /* synthetic */ void lambda$createView$1$MessageStatisticActivity(View view) {
        if (getParentLayout().fragmentsStack.size() > 1) {
            BaseFragment baseFragment = getParentLayout().fragmentsStack.get(getParentLayout().fragmentsStack.size() - 2);
            if ((baseFragment instanceof ChatActivity) && ((ChatActivity) baseFragment).getCurrentChat().id == this.chatId) {
                finishFragment();
                return;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putInt("chat_id", this.chatId);
        bundle.putInt("message_id", this.messageId);
        bundle.putBoolean("need_remove_previous_same_chat_activity", false);
        presentFragment(new ChatActivity(bundle));
    }

    private void updateMenu() {
        TLRPC$ChatFull tLRPC$ChatFull = this.chat;
        if (tLRPC$ChatFull != null && tLRPC$ChatFull.can_view_stats) {
            ActionBarMenu createMenu = this.actionBar.createMenu();
            createMenu.clearItems();
            createMenu.addItem(0, NUM).addSubItem(1, NUM, LocaleController.getString("ViewChannelStats", NUM));
        }
    }

    /* access modifiers changed from: private */
    public void loadChats(int i) {
        if (!this.loading) {
            this.loading = true;
            ListAdapter listAdapter = this.listViewAdapter;
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
            TLRPC$TL_stats_getMessagePublicForwards tLRPC$TL_stats_getMessagePublicForwards = new TLRPC$TL_stats_getMessagePublicForwards();
            tLRPC$TL_stats_getMessagePublicForwards.limit = i;
            MessageObject messageObject2 = this.messageObject;
            TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = messageObject2.messageOwner.fwd_from;
            if (tLRPC$MessageFwdHeader != null) {
                tLRPC$TL_stats_getMessagePublicForwards.msg_id = tLRPC$MessageFwdHeader.saved_from_msg_id;
                tLRPC$TL_stats_getMessagePublicForwards.channel = getMessagesController().getInputChannel(-this.messageObject.getFromChatId());
            } else {
                tLRPC$TL_stats_getMessagePublicForwards.msg_id = messageObject2.getId();
                tLRPC$TL_stats_getMessagePublicForwards.channel = getMessagesController().getInputChannel((int) (-this.messageObject.getDialogId()));
            }
            if (!this.messages.isEmpty()) {
                ArrayList<TLRPC$Message> arrayList = this.messages;
                TLRPC$Message tLRPC$Message = arrayList.get(arrayList.size() - 1);
                tLRPC$TL_stats_getMessagePublicForwards.offset_id = tLRPC$Message.id;
                tLRPC$TL_stats_getMessagePublicForwards.offset_peer = getMessagesController().getInputPeer((int) MessageObject.getDialogId(tLRPC$Message));
                tLRPC$TL_stats_getMessagePublicForwards.offset_rate = this.nextRate;
            } else {
                tLRPC$TL_stats_getMessagePublicForwards.offset_peer = new TLRPC$TL_inputPeerEmpty();
            }
            getConnectionsManager().bindRequestToGuid(getConnectionsManager().sendRequest(tLRPC$TL_stats_getMessagePublicForwards, new RequestDelegate() {
                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    MessageStatisticActivity.this.lambda$loadChats$3$MessageStatisticActivity(tLObject, tLRPC$TL_error);
                }
            }, (QuickAckDelegate) null, (WriteToSocketDelegate) null, 0, this.chat.stats_dc, 1, true), this.classGuid);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadChats$3 */
    public /* synthetic */ void lambda$loadChats$3$MessageStatisticActivity(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MessageStatisticActivity.this.lambda$null$2$MessageStatisticActivity(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$2 */
    public /* synthetic */ void lambda$null$2$MessageStatisticActivity(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject) {
        if (tLRPC$TL_error == null) {
            TLRPC$messages_Messages tLRPC$messages_Messages = (TLRPC$messages_Messages) tLObject;
            if ((tLRPC$messages_Messages.flags & 1) != 0) {
                this.nextRate = tLRPC$messages_Messages.next_rate;
            }
            int i = tLRPC$messages_Messages.count;
            if (i != 0) {
                this.publicChats = i;
            } else if (this.publicChats == 0) {
                this.publicChats = tLRPC$messages_Messages.messages.size();
            }
            this.endReached = !(tLRPC$messages_Messages instanceof TLRPC$TL_messages_messagesSlice);
            getMessagesController().putChats(tLRPC$messages_Messages.chats, false);
            getMessagesController().putUsers(tLRPC$messages_Messages.users, false);
            this.messages.addAll(tLRPC$messages_Messages.messages);
            EmptyTextProgressView emptyTextProgressView = this.emptyView;
            if (emptyTextProgressView != null) {
                emptyTextProgressView.showTextView();
            }
        }
        this.firstLoaded = true;
        this.loading = false;
        updateRows();
    }

    private void loadStat() {
        TLRPC$TL_stats_getMessageStats tLRPC$TL_stats_getMessageStats = new TLRPC$TL_stats_getMessageStats();
        MessageObject messageObject2 = this.messageObject;
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = messageObject2.messageOwner.fwd_from;
        if (tLRPC$MessageFwdHeader != null) {
            tLRPC$TL_stats_getMessageStats.msg_id = tLRPC$MessageFwdHeader.saved_from_msg_id;
            tLRPC$TL_stats_getMessageStats.channel = getMessagesController().getInputChannel(-this.messageObject.getFromChatId());
        } else {
            tLRPC$TL_stats_getMessageStats.msg_id = messageObject2.getId();
            tLRPC$TL_stats_getMessageStats.channel = getMessagesController().getInputChannel((int) (-this.messageObject.getDialogId()));
        }
        getConnectionsManager().sendRequest(tLRPC$TL_stats_getMessageStats, new RequestDelegate() {
            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                MessageStatisticActivity.this.lambda$loadStat$7$MessageStatisticActivity(tLObject, tLRPC$TL_error);
            }
        }, (QuickAckDelegate) null, (WriteToSocketDelegate) null, 0, this.chat.stats_dc, 1, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadStat$7 */
    public /* synthetic */ void lambda$loadStat$7$MessageStatisticActivity(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MessageStatisticActivity.this.lambda$null$6$MessageStatisticActivity(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$6 */
    public /* synthetic */ void lambda$null$6$MessageStatisticActivity(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject) {
        this.statsLoaded = true;
        if (tLRPC$TL_error != null) {
            updateRows();
            return;
        }
        StatisticActivity.ChartViewData createViewData = StatisticActivity.createViewData(((TLRPC$TL_stats_messageStats) tLObject).views_graph, LocaleController.getString("InteractionsChartTitle", NUM), 1, false);
        this.interactionsViewData = createViewData;
        if (createViewData == null || createViewData.chartData.x.length > 5) {
            updateRows();
            return;
        }
        this.statsLoaded = false;
        TLRPC$TL_stats_loadAsyncGraph tLRPC$TL_stats_loadAsyncGraph = new TLRPC$TL_stats_loadAsyncGraph();
        StatisticActivity.ChartViewData chartViewData = this.interactionsViewData;
        tLRPC$TL_stats_loadAsyncGraph.token = chartViewData.zoomToken;
        long[] jArr = chartViewData.chartData.x;
        tLRPC$TL_stats_loadAsyncGraph.x = jArr[jArr.length - 1];
        tLRPC$TL_stats_loadAsyncGraph.flags |= 1;
        ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_stats_loadAsyncGraph, new RequestDelegate(this.interactionsViewData.zoomToken + "_" + tLRPC$TL_stats_loadAsyncGraph.x, tLRPC$TL_stats_loadAsyncGraph) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ TLRPC$TL_stats_loadAsyncGraph f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                MessageStatisticActivity.this.lambda$null$5$MessageStatisticActivity(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
            }
        }, (QuickAckDelegate) null, (WriteToSocketDelegate) null, 0, this.chat.stats_dc, 1, true), this.classGuid);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$5 */
    public /* synthetic */ void lambda$null$5$MessageStatisticActivity(String str, TLRPC$TL_stats_loadAsyncGraph tLRPC$TL_stats_loadAsyncGraph, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        ChartData chartData = null;
        if (tLObject instanceof TLRPC$TL_statsGraph) {
            try {
                chartData = StatisticActivity.createChartData(new JSONObject(((TLRPC$TL_statsGraph) tLObject).json.data), 1, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (tLObject instanceof TLRPC$TL_statsGraphError) {
            Toast.makeText(getParentActivity(), ((TLRPC$TL_statsGraphError) tLObject).error, 1).show();
        }
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, chartData, str, tLRPC$TL_stats_loadAsyncGraph) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ ChartData f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ TLRPC$TL_stats_loadAsyncGraph f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                MessageStatisticActivity.this.lambda$null$4$MessageStatisticActivity(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$4 */
    public /* synthetic */ void lambda$null$4$MessageStatisticActivity(TLRPC$TL_error tLRPC$TL_error, ChartData chartData, String str, TLRPC$TL_stats_loadAsyncGraph tLRPC$TL_stats_loadAsyncGraph) {
        this.statsLoaded = true;
        if (tLRPC$TL_error != null || chartData == null) {
            updateRows();
            return;
        }
        this.childDataCache.put(str, chartData);
        StatisticActivity.ChartViewData chartViewData = this.interactionsViewData;
        chartViewData.childChartData = chartData;
        chartViewData.activeZoom = tLRPC$TL_stats_loadAsyncGraph.x;
        updateRows();
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
        ListAdapter listAdapter = this.listViewAdapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 0) {
                return ((ManageChatUserCell) viewHolder.itemView).getCurrentObject() instanceof TLObject;
            }
            return false;
        }

        public int getItemCount() {
            return MessageStatisticActivity.this.rowCount;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v2, resolved type: org.telegram.ui.Cells.ManageChatUserCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v4, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: org.telegram.ui.Cells.ShadowSectionCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v7, resolved type: org.telegram.ui.MessageStatisticActivity$ListAdapter$1} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v8, resolved type: org.telegram.ui.MessageStatisticActivity$OverviewCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v9, resolved type: org.telegram.ui.Cells.EmptyCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v10, resolved type: org.telegram.ui.Cells.LoadingCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v11, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v12, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v13, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v14, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v15, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX WARNING: type inference failed for: r9v2, types: [android.view.View] */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r9, int r10) {
            /*
                r8 = this;
                r9 = 6
                r0 = 2
                java.lang.String r1 = "windowBackgroundWhite"
                if (r10 == 0) goto L_0x009b
                r2 = 1
                if (r10 == r2) goto L_0x0093
                if (r10 == r0) goto L_0x0076
                r0 = 4
                if (r10 == r0) goto L_0x005d
                r0 = 5
                r2 = -1
                if (r10 == r0) goto L_0x0043
                if (r10 == r9) goto L_0x002a
                org.telegram.ui.Cells.LoadingCell r9 = new org.telegram.ui.Cells.LoadingCell
                android.content.Context r10 = r8.mContext
                r0 = 1109393408(0x42200000, float:40.0)
                int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
                r1 = 1123024896(0x42var_, float:120.0)
                int r1 = org.telegram.messenger.AndroidUtilities.dp(r1)
                r9.<init>(r10, r0, r1)
                goto L_0x00ab
            L_0x002a:
                org.telegram.ui.Cells.EmptyCell r9 = new org.telegram.ui.Cells.EmptyCell
                android.content.Context r10 = r8.mContext
                r0 = 16
                r9.<init>(r10, r0)
                androidx.recyclerview.widget.RecyclerView$LayoutParams r10 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                r10.<init>((int) r2, (int) r0)
                r9.setLayoutParams(r10)
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r9.setBackgroundColor(r10)
                goto L_0x00ab
            L_0x0043:
                org.telegram.ui.MessageStatisticActivity$OverviewCell r9 = new org.telegram.ui.MessageStatisticActivity$OverviewCell
                org.telegram.ui.MessageStatisticActivity r10 = org.telegram.ui.MessageStatisticActivity.this
                android.content.Context r0 = r8.mContext
                r9.<init>(r0)
                androidx.recyclerview.widget.RecyclerView$LayoutParams r10 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                r0 = -2
                r10.<init>((int) r2, (int) r0)
                r9.setLayoutParams(r10)
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r9.setBackgroundColor(r10)
                goto L_0x00ab
            L_0x005d:
                org.telegram.ui.MessageStatisticActivity$ListAdapter$1 r9 = new org.telegram.ui.MessageStatisticActivity$ListAdapter$1
                android.content.Context r10 = r8.mContext
                org.telegram.ui.MessageStatisticActivity r0 = org.telegram.ui.MessageStatisticActivity.this
                org.telegram.ui.Charts.BaseChartView$SharedUiComponents r3 = new org.telegram.ui.Charts.BaseChartView$SharedUiComponents
                r3.<init>()
                org.telegram.ui.Charts.BaseChartView.SharedUiComponents unused = r0.sharedUi = r3
                r9.<init>(r10, r2, r3)
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r9.setBackgroundColor(r10)
                goto L_0x00ab
            L_0x0076:
                org.telegram.ui.Cells.HeaderCell r9 = new org.telegram.ui.Cells.HeaderCell
                android.content.Context r3 = r8.mContext
                r5 = 16
                r6 = 11
                r7 = 0
                java.lang.String r4 = "windowBackgroundWhiteBlueHeader"
                r2 = r9
                r2.<init>(r3, r4, r5, r6, r7)
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r9.setBackgroundColor(r10)
                r10 = 43
                r9.setHeight(r10)
                goto L_0x00ab
            L_0x0093:
                org.telegram.ui.Cells.ShadowSectionCell r9 = new org.telegram.ui.Cells.ShadowSectionCell
                android.content.Context r10 = r8.mContext
                r9.<init>(r10)
                goto L_0x00ab
            L_0x009b:
                org.telegram.ui.Cells.ManageChatUserCell r10 = new org.telegram.ui.Cells.ManageChatUserCell
                android.content.Context r2 = r8.mContext
                r3 = 0
                r10.<init>(r2, r9, r0, r3)
                int r9 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r10.setBackgroundColor(r9)
                r9 = r10
            L_0x00ab:
                org.telegram.ui.Components.RecyclerListView$Holder r10 = new org.telegram.ui.Components.RecyclerListView$Holder
                r10.<init>(r9)
                return r10
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.MessageStatisticActivity.ListAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            String str;
            Object obj;
            String str2;
            int itemViewType = viewHolder.getItemViewType();
            boolean z = false;
            if (itemViewType == 0) {
                ManageChatUserCell manageChatUserCell = (ManageChatUserCell) viewHolder.itemView;
                TLRPC$Message item = getItem(i);
                int dialogId = (int) MessageObject.getDialogId(item);
                if (dialogId > 0) {
                    obj = MessageStatisticActivity.this.getMessagesController().getUser(Integer.valueOf(dialogId));
                    str = null;
                } else {
                    TLRPC$Chat chat = MessageStatisticActivity.this.getMessagesController().getChat(Integer.valueOf(-dialogId));
                    if (chat.participants_count != 0) {
                        if (!ChatObject.isChannel(chat) || chat.megagroup) {
                            str2 = LocaleController.formatPluralString("Members", chat.participants_count);
                        } else {
                            str2 = LocaleController.formatPluralString("Subscribers", chat.participants_count);
                        }
                        str = String.format("%1$s, %2$s", new Object[]{str2, LocaleController.formatPluralString("Views", item.views)});
                    } else {
                        str = null;
                    }
                    obj = chat;
                }
                if (obj != null) {
                    if (i != MessageStatisticActivity.this.endRow - 1) {
                        z = true;
                    }
                    manageChatUserCell.setData(obj, (CharSequence) null, str, z);
                }
            } else if (itemViewType == 1) {
                viewHolder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, "windowBackgroundGrayShadow"));
            } else if (itemViewType == 2) {
                HeaderCell headerCell = (HeaderCell) viewHolder.itemView;
                if (i == MessageStatisticActivity.this.overviewHeaderRow) {
                    headerCell.setText(LocaleController.formatString("StatisticOverview", NUM, new Object[0]));
                } else {
                    headerCell.setText(LocaleController.formatPluralString("PublicSharesCount", MessageStatisticActivity.this.publicChats));
                }
            } else if (itemViewType == 4) {
                StatisticActivity.BaseChartCell baseChartCell = (StatisticActivity.BaseChartCell) viewHolder.itemView;
                baseChartCell.updateData(MessageStatisticActivity.this.interactionsViewData, false);
                baseChartCell.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
            } else if (itemViewType == 5) {
                ((OverviewCell) viewHolder.itemView).setData();
            }
        }

        public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
            View view = viewHolder.itemView;
            if (view instanceof ManageChatUserCell) {
                ((ManageChatUserCell) view).recycle();
            }
        }

        public int getItemViewType(int i) {
            if (MessageStatisticActivity.this.shadowDivideCells.contains(Integer.valueOf(i))) {
                return 1;
            }
            if (i == MessageStatisticActivity.this.headerRow || i == MessageStatisticActivity.this.overviewHeaderRow) {
                return 2;
            }
            if (i == MessageStatisticActivity.this.loadingRow) {
                return 3;
            }
            if (i == MessageStatisticActivity.this.interactionsChartRow) {
                return 4;
            }
            if (i == MessageStatisticActivity.this.overviewRow) {
                return 5;
            }
            return i == MessageStatisticActivity.this.emptyRow ? 6 : 0;
        }

        public TLRPC$Message getItem(int i) {
            if (i < MessageStatisticActivity.this.startRow || i >= MessageStatisticActivity.this.endRow) {
                return null;
            }
            return (TLRPC$Message) MessageStatisticActivity.this.messages.get(i - MessageStatisticActivity.this.startRow);
        }
    }

    public class OverviewCell extends LinearLayout {
        View[] cell = new View[3];
        TextView[] primary = new TextView[3];
        TextView[] title = new TextView[3];

        public OverviewCell(Context context) {
            super(context);
            setOrientation(1);
            setPadding(AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(16.0f), AndroidUtilities.dp(16.0f));
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(0);
            for (int i = 0; i < 3; i++) {
                LinearLayout linearLayout2 = new LinearLayout(context);
                this.cell[i] = linearLayout2;
                linearLayout2.setOrientation(1);
                this.primary[i] = new TextView(context);
                this.title[i] = new TextView(context);
                this.primary[i].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                this.primary[i].setTextSize(1, 17.0f);
                this.title[i].setTextSize(1, 13.0f);
                linearLayout2.addView(this.primary[i]);
                linearLayout2.addView(this.title[i]);
                linearLayout.addView(linearLayout2, LayoutHelper.createLinear(-1, -2, 1.0f));
            }
            addView(linearLayout, LayoutHelper.createFrame(-1, -2.0f));
        }

        public void setData() {
            this.primary[0].setText(AndroidUtilities.formatWholeNumber(MessageStatisticActivity.this.messageObject.messageOwner.views, 0));
            this.title[0].setText(LocaleController.getString("StatisticViews", NUM));
            if (MessageStatisticActivity.this.publicChats > 0) {
                this.cell[1].setVisibility(0);
                this.primary[1].setText(AndroidUtilities.formatWholeNumber(MessageStatisticActivity.this.publicChats, 0));
                this.title[1].setText(LocaleController.formatString("PublicShares", NUM, new Object[0]));
            } else {
                this.cell[1].setVisibility(8);
            }
            int access$2000 = MessageStatisticActivity.this.messageObject.messageOwner.forwards - MessageStatisticActivity.this.publicChats;
            if (access$2000 > 0) {
                this.cell[2].setVisibility(0);
                this.primary[2].setText(AndroidUtilities.formatWholeNumber(access$2000, 0));
                this.title[2].setText(LocaleController.formatString("PrivateShares", NUM, new Object[0]));
            } else {
                this.cell[2].setVisibility(8);
            }
            updateColors();
        }

        /* access modifiers changed from: private */
        public void updateColors() {
            for (int i = 0; i < 3; i++) {
                this.primary[i].setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
                this.title[i].setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
            }
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        $$Lambda$MessageStatisticActivity$FqYyYyPaDhQSkIN9gs41U7WkSpE r11 = new ThemeDescription.ThemeDescriptionDelegate() {
            public final void didSetColor() {
                MessageStatisticActivity.this.lambda$getThemeDescriptions$8$MessageStatisticActivity();
            }
        };
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class, ManageChatUserCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGray"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        ChatAvatarContainer chatAvatarContainer = this.avatarContainer;
        SimpleTextView simpleTextView = null;
        arrayList.add(new ThemeDescription(chatAvatarContainer != null ? chatAvatarContainer.getTitleTextView() : null, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        ChatAvatarContainer chatAvatarContainer2 = this.avatarContainer;
        if (chatAvatarContainer2 != null) {
            simpleTextView = chatAvatarContainer2.getSubtitleTextView();
        }
        arrayList.add(new ThemeDescription((View) simpleTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, (Class[]) null, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarSubtitle", (Object) null));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r11, "statisticChartLineEmpty"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGrayShadow"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueHeader"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"nameTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        $$Lambda$MessageStatisticActivity$FqYyYyPaDhQSkIN9gs41U7WkSpE r9 = r11;
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusColor"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r9, "windowBackgroundWhiteGrayText"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusOnlineColor"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r9, "windowBackgroundWhiteBlueText"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, (Paint) null, Theme.avatarDrawables, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_text"));
        $$Lambda$MessageStatisticActivity$FqYyYyPaDhQSkIN9gs41U7WkSpE r8 = r11;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundRed"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundOrange"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundViolet"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundGreen"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundCyan"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r8, "avatar_backgroundPink"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuBackground"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuItem"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuItemIcon"));
        StatisticActivity.putColorFromData(this.interactionsViewData, arrayList, r11);
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getThemeDescriptions$8 */
    public /* synthetic */ void lambda$getThemeDescriptions$8$MessageStatisticActivity() {
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            int childCount = recyclerListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                recolorRecyclerItem(this.listView.getChildAt(i));
            }
            int hiddenChildCount = this.listView.getHiddenChildCount();
            for (int i2 = 0; i2 < hiddenChildCount; i2++) {
                recolorRecyclerItem(this.listView.getHiddenChildAt(i2));
            }
            int cachedChildCount = this.listView.getCachedChildCount();
            for (int i3 = 0; i3 < cachedChildCount; i3++) {
                recolorRecyclerItem(this.listView.getCachedChildAt(i3));
            }
            int attachedScrapChildCount = this.listView.getAttachedScrapChildCount();
            for (int i4 = 0; i4 < attachedScrapChildCount; i4++) {
                recolorRecyclerItem(this.listView.getAttachedScrapChildAt(i4));
            }
            this.listView.getRecycledViewPool().clear();
        }
        BaseChartView.SharedUiComponents sharedUiComponents = this.sharedUi;
        if (sharedUiComponents != null) {
            sharedUiComponents.invalidate();
        }
        this.avatarContainer.getSubtitleTextView().setLinkTextColor(Theme.getColor("player_actionBarSubtitle"));
    }

    private void recolorRecyclerItem(View view) {
        if (view instanceof ManageChatUserCell) {
            ((ManageChatUserCell) view).update(0);
        } else if (view instanceof StatisticActivity.BaseChartCell) {
            ((StatisticActivity.BaseChartCell) view).recolor();
            view.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        } else if (view instanceof ShadowSectionCell) {
            CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor("windowBackgroundGray")), Theme.getThemedDrawable(ApplicationLoader.applicationContext, NUM, "windowBackgroundGrayShadow"), 0, 0);
            combinedDrawable.setFullsize(true);
            view.setBackground(combinedDrawable);
        } else if (view instanceof ChartHeaderView) {
            ((ChartHeaderView) view).recolor();
        } else if (view instanceof OverviewCell) {
            ((OverviewCell) view).updateColors();
            view.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        }
        if (view instanceof EmptyCell) {
            view.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        }
    }
}
