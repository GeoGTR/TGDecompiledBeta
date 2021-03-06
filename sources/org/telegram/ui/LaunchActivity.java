package org.telegram.ui;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.arch.core.util.Function;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$LangPackString;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$MessageMedia;
import org.telegram.tgnet.TLRPC$ReplyMarkup;
import org.telegram.tgnet.TLRPC$TL_account_authorizationForm;
import org.telegram.tgnet.TLRPC$TL_account_getAuthorizationForm;
import org.telegram.tgnet.TLRPC$TL_account_getPassword;
import org.telegram.tgnet.TLRPC$TL_account_password;
import org.telegram.tgnet.TLRPC$TL_auth_acceptLoginToken;
import org.telegram.tgnet.TLRPC$TL_authorization;
import org.telegram.tgnet.TLRPC$TL_channels_getChannels;
import org.telegram.tgnet.TLRPC$TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_help_appUpdate;
import org.telegram.tgnet.TLRPC$TL_help_deepLinkInfo;
import org.telegram.tgnet.TLRPC$TL_help_getAppUpdate;
import org.telegram.tgnet.TLRPC$TL_help_termsOfService;
import org.telegram.tgnet.TLRPC$TL_inputChannel;
import org.telegram.tgnet.TLRPC$TL_inputGameShortName;
import org.telegram.tgnet.TLRPC$TL_inputMediaGame;
import org.telegram.tgnet.TLRPC$TL_langPackLanguage;
import org.telegram.tgnet.TLRPC$TL_langpack_getStrings;
import org.telegram.tgnet.TLRPC$TL_messages_chats;
import org.telegram.tgnet.TLRPC$TL_messages_getDiscussionMessage;
import org.telegram.tgnet.TLRPC$TL_theme;
import org.telegram.tgnet.TLRPC$TL_wallPaper;
import org.telegram.tgnet.TLRPC$Updates;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$UserFull;
import org.telegram.tgnet.TLRPC$Vector;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BlockingUpdateView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatAttachAlertContactsLayout;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.GroupCallPip;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.PhonebookShareAlert;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SideMenultItemAnimator;
import org.telegram.ui.Components.TermsOfServiceView;
import org.telegram.ui.Components.ThemeEditorView;
import org.telegram.ui.Components.UpdateAppAlertDialog;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LocationActivity;

public class LaunchActivity extends Activity implements ActionBarLayout.ActionBarLayoutDelegate, NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate {
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList<>();
    /* access modifiers changed from: private */
    public ActionBarLayout actionBarLayout;
    /* access modifiers changed from: private */
    public View backgroundTablet;
    private BlockingUpdateView blockingUpdateView;
    private ArrayList<TLRPC$User> contactsToSend;
    private Uri contactsToSendUri;
    private int currentAccount;
    private int currentConnectionState;
    private String documentsMimeType;
    private ArrayList<String> documentsOriginalPathsArray;
    private ArrayList<String> documentsPathsArray;
    private ArrayList<Uri> documentsUrisArray;
    /* access modifiers changed from: private */
    public DrawerLayoutAdapter drawerLayoutAdapter;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private HashMap<String, String> englishLocaleStrings;
    private Uri exportingChatUri;
    private boolean finished;
    private FrameLayout frameLayout;
    /* access modifiers changed from: private */
    public SideMenultItemAnimator itemAnimator;
    /* access modifiers changed from: private */
    public ActionBarLayout layersActionBarLayout;
    private boolean loadingLocaleDialog;
    private TLRPC$TL_theme loadingTheme;
    private boolean loadingThemeAccent;
    private String loadingThemeFileName;
    private Theme.ThemeInfo loadingThemeInfo;
    private AlertDialog loadingThemeProgressDialog;
    private TLRPC$TL_wallPaper loadingThemeWallpaper;
    private String loadingThemeWallpaperName;
    private AlertDialog localeDialog;
    /* access modifiers changed from: private */
    public Runnable lockRunnable;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private Intent passcodeSaveIntent;
    private boolean passcodeSaveIntentIsNew;
    private boolean passcodeSaveIntentIsRestore;
    private PasscodeView passcodeView;
    private ArrayList<SendMessagesHelper.SendingMediaInfo> photoPathsArray;
    private AlertDialog proxyErrorDialog;
    /* access modifiers changed from: private */
    public ActionBarLayout rightActionBarLayout;
    private String sendingText;
    /* access modifiers changed from: private */
    public FrameLayout shadowTablet;
    /* access modifiers changed from: private */
    public FrameLayout shadowTabletSide;
    /* access modifiers changed from: private */
    public RecyclerListView sideMenu;
    private HashMap<String, String> systemLocaleStrings;
    /* access modifiers changed from: private */
    public boolean tabletFullSize;
    /* access modifiers changed from: private */
    public TermsOfServiceView termsOfServiceView;
    /* access modifiers changed from: private */
    public ImageView themeSwitchImageView;
    /* access modifiers changed from: private */
    public RLottieDrawable themeSwitchSunDrawable;
    /* access modifiers changed from: private */
    public View themeSwitchSunView;
    private String videoPath;
    private ActionMode visibleActionMode;
    private AlertDialog visibleDialog;

    static /* synthetic */ void lambda$onCreate$1(View view) {
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:48:0x0110 */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCreate(android.os.Bundle r14) {
        /*
            r13 = this;
            java.lang.String r0 = "flyme"
            org.telegram.messenger.ApplicationLoader.postInitApplication()
            android.content.res.Resources r1 = r13.getResources()
            android.content.res.Configuration r1 = r1.getConfiguration()
            org.telegram.messenger.AndroidUtilities.checkDisplaySize(r13, r1)
            int r1 = org.telegram.messenger.UserConfig.selectedAccount
            r13.currentAccount = r1
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            boolean r1 = r1.isClientActivated()
            r2 = 1
            r3 = 0
            if (r1 != 0) goto L_0x00ef
            android.content.Intent r1 = r13.getIntent()
            if (r1 == 0) goto L_0x0088
            java.lang.String r4 = r1.getAction()
            if (r4 == 0) goto L_0x0088
            java.lang.String r4 = r1.getAction()
            java.lang.String r5 = "android.intent.action.SEND"
            boolean r4 = r5.equals(r4)
            if (r4 != 0) goto L_0x0081
            java.lang.String r4 = r1.getAction()
            java.lang.String r5 = "android.intent.action.SEND_MULTIPLE"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x0045
            goto L_0x0081
        L_0x0045:
            java.lang.String r4 = r1.getAction()
            java.lang.String r5 = "android.intent.action.VIEW"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x0088
            android.net.Uri r4 = r1.getData()
            if (r4 == 0) goto L_0x0088
            java.lang.String r4 = r4.toString()
            java.lang.String r4 = r4.toLowerCase()
            java.lang.String r5 = "tg:proxy"
            boolean r5 = r4.startsWith(r5)
            if (r5 != 0) goto L_0x007f
            java.lang.String r5 = "tg://proxy"
            boolean r5 = r4.startsWith(r5)
            if (r5 != 0) goto L_0x007f
            java.lang.String r5 = "tg:socks"
            boolean r5 = r4.startsWith(r5)
            if (r5 != 0) goto L_0x007f
            java.lang.String r5 = "tg://socks"
            boolean r4 = r4.startsWith(r5)
            if (r4 == 0) goto L_0x0088
        L_0x007f:
            r4 = 1
            goto L_0x0089
        L_0x0081:
            super.onCreate(r14)
            r13.finish()
            return
        L_0x0088:
            r4 = 0
        L_0x0089:
            android.content.SharedPreferences r5 = org.telegram.messenger.MessagesController.getGlobalMainSettings()
            java.lang.String r6 = "intro_crashed_time"
            r7 = 0
            long r9 = r5.getLong(r6, r7)
            if (r1 == 0) goto L_0x00a1
            java.lang.String r11 = "fromIntro"
            boolean r11 = r1.getBooleanExtra(r11, r3)
            if (r11 == 0) goto L_0x00a1
            r11 = 1
            goto L_0x00a2
        L_0x00a1:
            r11 = 0
        L_0x00a2:
            if (r11 == 0) goto L_0x00af
            android.content.SharedPreferences$Editor r5 = r5.edit()
            android.content.SharedPreferences$Editor r5 = r5.putLong(r6, r7)
            r5.commit()
        L_0x00af:
            if (r4 != 0) goto L_0x00ef
            long r4 = java.lang.System.currentTimeMillis()
            long r9 = r9 - r4
            long r4 = java.lang.Math.abs(r9)
            r6 = 120000(0x1d4c0, double:5.9288E-319)
            int r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r8 < 0) goto L_0x00ef
            if (r1 == 0) goto L_0x00ef
            if (r11 != 0) goto L_0x00ef
            android.content.Context r4 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.String r5 = "logininfo2"
            android.content.SharedPreferences r4 = r4.getSharedPreferences(r5, r3)
            java.util.Map r4 = r4.getAll()
            boolean r4 = r4.isEmpty()
            if (r4 == 0) goto L_0x00ef
            android.content.Intent r0 = new android.content.Intent
            java.lang.Class<org.telegram.ui.IntroActivity> r2 = org.telegram.ui.IntroActivity.class
            r0.<init>(r13, r2)
            android.net.Uri r1 = r1.getData()
            r0.setData(r1)
            r13.startActivity(r0)
            super.onCreate(r14)
            r13.finish()
            return
        L_0x00ef:
            r13.requestWindowFeature(r2)
            r1 = 2131689484(0x7f0var_c, float:1.9007985E38)
            r13.setTheme(r1)
            int r1 = android.os.Build.VERSION.SDK_INT
            r4 = 0
            r5 = 21
            if (r1 < r5) goto L_0x0119
            r1 = -16777216(0xfffffffffvar_, float:-1.7014118E38)
            android.app.ActivityManager$TaskDescription r6 = new android.app.ActivityManager$TaskDescription     // Catch:{ Exception -> 0x0110 }
            java.lang.String r7 = "actionBarDefault"
            int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)     // Catch:{ Exception -> 0x0110 }
            r7 = r7 | r1
            r6.<init>(r4, r4, r7)     // Catch:{ Exception -> 0x0110 }
            r13.setTaskDescription(r6)     // Catch:{ Exception -> 0x0110 }
        L_0x0110:
            android.view.Window r6 = r13.getWindow()     // Catch:{ Exception -> 0x0118 }
            r6.setNavigationBarColor(r1)     // Catch:{ Exception -> 0x0118 }
            goto L_0x0119
        L_0x0118:
        L_0x0119:
            android.view.Window r1 = r13.getWindow()
            r6 = 2131166075(0x7var_b, float:1.7946385E38)
            r1.setBackgroundDrawableResource(r6)
            java.lang.String r1 = org.telegram.messenger.SharedConfig.passcodeHash
            int r1 = r1.length()
            if (r1 <= 0) goto L_0x013d
            boolean r1 = org.telegram.messenger.SharedConfig.allowScreenCapture
            if (r1 != 0) goto L_0x013d
            android.view.Window r1 = r13.getWindow()     // Catch:{ Exception -> 0x0139 }
            r6 = 8192(0x2000, float:1.14794E-41)
            r1.setFlags(r6, r6)     // Catch:{ Exception -> 0x0139 }
            goto L_0x013d
        L_0x0139:
            r1 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
        L_0x013d:
            super.onCreate(r14)
            int r1 = android.os.Build.VERSION.SDK_INT
            r6 = 24
            if (r1 < r6) goto L_0x014c
            boolean r7 = r13.isInMultiWindowMode()
            org.telegram.messenger.AndroidUtilities.isInMultiwindow = r7
        L_0x014c:
            org.telegram.ui.ActionBar.Theme.createChatResources(r13, r3)
            java.lang.String r7 = org.telegram.messenger.SharedConfig.passcodeHash
            int r7 = r7.length()
            if (r7 == 0) goto L_0x0165
            boolean r7 = org.telegram.messenger.SharedConfig.appLocked
            if (r7 == 0) goto L_0x0165
            long r7 = android.os.SystemClock.elapsedRealtime()
            r9 = 1000(0x3e8, double:4.94E-321)
            long r7 = r7 / r9
            int r8 = (int) r7
            org.telegram.messenger.SharedConfig.lastPauseTime = r8
        L_0x0165:
            org.telegram.messenger.AndroidUtilities.fillStatusBarHeight(r13)
            org.telegram.ui.LaunchActivity$1 r7 = new org.telegram.ui.LaunchActivity$1
            r7.<init>(r13)
            r13.actionBarLayout = r7
            android.widget.FrameLayout r7 = new android.widget.FrameLayout
            r7.<init>(r13)
            r13.frameLayout = r7
            android.view.ViewGroup$LayoutParams r8 = new android.view.ViewGroup$LayoutParams
            r9 = -1
            r8.<init>(r9, r9)
            r13.setContentView(r7, r8)
            r7 = 8
            if (r1 < r5) goto L_0x018d
            android.widget.ImageView r8 = new android.widget.ImageView
            r8.<init>(r13)
            r13.themeSwitchImageView = r8
            r8.setVisibility(r7)
        L_0x018d:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r8 = new org.telegram.ui.ActionBar.DrawerLayoutContainer
            r8.<init>(r13)
            r13.drawerLayoutContainer = r8
            java.lang.String r10 = "windowBackgroundWhite"
            int r10 = org.telegram.ui.ActionBar.Theme.getColor(r10)
            r8.setBehindKeyboardColor(r10)
            android.widget.FrameLayout r8 = r13.frameLayout
            org.telegram.ui.ActionBar.DrawerLayoutContainer r10 = r13.drawerLayoutContainer
            r11 = -1082130432(0xffffffffbvar_, float:-1.0)
            android.widget.FrameLayout$LayoutParams r12 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r11)
            r8.addView(r10, r12)
            if (r1 < r5) goto L_0x01c6
            org.telegram.ui.LaunchActivity$2 r1 = new org.telegram.ui.LaunchActivity$2
            r1.<init>(r13)
            r13.themeSwitchSunView = r1
            android.widget.FrameLayout r5 = r13.frameLayout
            r8 = 48
            r10 = 1111490560(0x42400000, float:48.0)
            android.widget.FrameLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createFrame(r8, r10)
            r5.addView(r1, r8)
            android.view.View r1 = r13.themeSwitchSunView
            r1.setVisibility(r7)
        L_0x01c6:
            boolean r1 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r1 == 0) goto L_0x02b0
            android.view.Window r1 = r13.getWindow()
            r5 = 16
            r1.setSoftInputMode(r5)
            org.telegram.ui.LaunchActivity$3 r1 = new org.telegram.ui.LaunchActivity$3
            r1.<init>(r13)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r5 = r13.drawerLayoutContainer
            android.widget.FrameLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r11)
            r5.addView(r1, r8)
            android.view.View r5 = new android.view.View
            r5.<init>(r13)
            r13.backgroundTablet = r5
            android.content.res.Resources r5 = r13.getResources()
            r8 = 2131165336(0x7var_, float:1.7944886E38)
            android.graphics.drawable.Drawable r5 = r5.getDrawable(r8)
            android.graphics.drawable.BitmapDrawable r5 = (android.graphics.drawable.BitmapDrawable) r5
            android.graphics.Shader$TileMode r8 = android.graphics.Shader.TileMode.REPEAT
            r5.setTileModeXY(r8, r8)
            android.view.View r8 = r13.backgroundTablet
            r8.setBackgroundDrawable(r5)
            android.view.View r5 = r13.backgroundTablet
            android.widget.RelativeLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createRelative(r9, r9)
            r1.addView(r5, r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.actionBarLayout
            r1.addView(r5)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = new org.telegram.ui.ActionBar.ActionBarLayout
            r5.<init>(r13)
            r13.rightActionBarLayout = r5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r8 = rightFragmentsStack
            r5.init(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.rightActionBarLayout
            r5.setDelegate(r13)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.rightActionBarLayout
            r1.addView(r5)
            android.widget.FrameLayout r5 = new android.widget.FrameLayout
            r5.<init>(r13)
            r13.shadowTabletSide = r5
            r8 = 1076449908(0x40295274, float:2.6456575)
            r5.setBackgroundColor(r8)
            android.widget.FrameLayout r5 = r13.shadowTabletSide
            r1.addView(r5)
            android.widget.FrameLayout r5 = new android.widget.FrameLayout
            r5.<init>(r13)
            r13.shadowTablet = r5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r8 = layerFragmentsStack
            boolean r8 = r8.isEmpty()
            if (r8 == 0) goto L_0x0249
            r8 = 8
            goto L_0x024a
        L_0x0249:
            r8 = 0
        L_0x024a:
            r5.setVisibility(r8)
            android.widget.FrameLayout r5 = r13.shadowTablet
            r8 = 2130706432(0x7var_, float:1.7014118E38)
            r5.setBackgroundColor(r8)
            android.widget.FrameLayout r5 = r13.shadowTablet
            r1.addView(r5)
            android.widget.FrameLayout r5 = r13.shadowTablet
            org.telegram.ui.-$$Lambda$LaunchActivity$kQBSb900ZfovzV4C2-TBJIVramw r8 = new org.telegram.ui.-$$Lambda$LaunchActivity$kQBSb900ZfovzV4C2-TBJIVramw
            r8.<init>()
            r5.setOnTouchListener(r8)
            android.widget.FrameLayout r5 = r13.shadowTablet
            org.telegram.ui.-$$Lambda$LaunchActivity$2viHQ7iQGsryXfoyH4fBq7-S05A r8 = org.telegram.ui.$$Lambda$LaunchActivity$2viHQ7iQGsryXfoyH4fBq7S05A.INSTANCE
            r5.setOnClickListener(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = new org.telegram.ui.ActionBar.ActionBarLayout
            r5.<init>(r13)
            r13.layersActionBarLayout = r5
            r5.setRemoveActionBarExtraHeight(r2)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            android.widget.FrameLayout r8 = r13.shadowTablet
            r5.setBackgroundView(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            r5.setUseAlphaAnimations(r2)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            r8 = 2131165299(0x7var_, float:1.7944811E38)
            r5.setBackgroundResource(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r8 = layerFragmentsStack
            r5.init(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            r5.setDelegate(r13)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            org.telegram.ui.ActionBar.DrawerLayoutContainer r8 = r13.drawerLayoutContainer
            r5.setDrawerLayoutContainer(r8)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r8 = layerFragmentsStack
            boolean r8 = r8.isEmpty()
            if (r8 == 0) goto L_0x02a6
            goto L_0x02a7
        L_0x02a6:
            r7 = 0
        L_0x02a7:
            r5.setVisibility(r7)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.layersActionBarLayout
            r1.addView(r5)
            goto L_0x02bc
        L_0x02b0:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r1 = r13.drawerLayoutContainer
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.actionBarLayout
            android.view.ViewGroup$LayoutParams r7 = new android.view.ViewGroup$LayoutParams
            r7.<init>(r9, r9)
            r1.addView(r5, r7)
        L_0x02bc:
            org.telegram.ui.LaunchActivity$4 r1 = new org.telegram.ui.LaunchActivity$4
            r1.<init>(r13)
            r13.sideMenu = r1
            org.telegram.ui.Components.SideMenultItemAnimator r5 = new org.telegram.ui.Components.SideMenultItemAnimator
            r5.<init>(r1)
            r13.itemAnimator = r5
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            r1.setItemAnimator(r5)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            java.lang.String r5 = "chats_menuBackground"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
            r1.setBackgroundColor(r5)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            androidx.recyclerview.widget.LinearLayoutManager r5 = new androidx.recyclerview.widget.LinearLayoutManager
            r5.<init>(r13, r2, r3)
            r1.setLayoutManager(r5)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            r1.setAllowItemsInteractionDuringAnimation(r3)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            org.telegram.ui.Adapters.DrawerLayoutAdapter r5 = new org.telegram.ui.Adapters.DrawerLayoutAdapter
            org.telegram.ui.Components.SideMenultItemAnimator r7 = r13.itemAnimator
            r5.<init>(r13, r7)
            r13.drawerLayoutAdapter = r5
            r1.setAdapter(r5)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r1 = r13.drawerLayoutContainer
            org.telegram.ui.Components.RecyclerListView r5 = r13.sideMenu
            r1.setDrawerLayout(r5)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            android.view.ViewGroup$LayoutParams r1 = r1.getLayoutParams()
            android.widget.FrameLayout$LayoutParams r1 = (android.widget.FrameLayout.LayoutParams) r1
            android.graphics.Point r5 = org.telegram.messenger.AndroidUtilities.getRealScreenSize()
            boolean r7 = org.telegram.messenger.AndroidUtilities.isTablet()
            r8 = 1134559232(0x43a00000, float:320.0)
            if (r7 == 0) goto L_0x0317
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r8)
            goto L_0x032e
        L_0x0317:
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r8)
            int r8 = r5.x
            int r5 = r5.y
            int r5 = java.lang.Math.min(r8, r5)
            r8 = 1113587712(0x42600000, float:56.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            int r5 = r5 - r8
            int r5 = java.lang.Math.min(r7, r5)
        L_0x032e:
            r1.width = r5
            r1.height = r9
            org.telegram.ui.Components.RecyclerListView r5 = r13.sideMenu
            r5.setLayoutParams(r1)
            org.telegram.ui.Components.RecyclerListView r1 = r13.sideMenu
            org.telegram.ui.-$$Lambda$LaunchActivity$c7BMHFAZYRAqqOiXCrFzi2Les1s r5 = new org.telegram.ui.-$$Lambda$LaunchActivity$c7BMHFAZYRAqqOiXCrFzi2Les1s
            r5.<init>()
            r1.setOnItemClickListener((org.telegram.ui.Components.RecyclerListView.OnItemClickListenerExtended) r5)
            androidx.recyclerview.widget.ItemTouchHelper r1 = new androidx.recyclerview.widget.ItemTouchHelper
            org.telegram.ui.LaunchActivity$5 r5 = new org.telegram.ui.LaunchActivity$5
            r7 = 3
            r5.<init>(r7, r3)
            r1.<init>(r5)
            org.telegram.ui.Components.RecyclerListView r5 = r13.sideMenu
            r1.attachToRecyclerView(r5)
            org.telegram.ui.Components.RecyclerListView r5 = r13.sideMenu
            org.telegram.ui.-$$Lambda$LaunchActivity$YILGR3yadeycI2j_s8n6fLMaQas r8 = new org.telegram.ui.-$$Lambda$LaunchActivity$YILGR3yadeycI2j_s8n6fLMaQas
            r8.<init>(r1)
            r5.setOnItemLongClickListener((org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener) r8)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r1 = r13.drawerLayoutContainer
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r13.actionBarLayout
            r1.setParentActionBarLayout(r5)
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            org.telegram.ui.ActionBar.DrawerLayoutContainer r5 = r13.drawerLayoutContainer
            r1.setDrawerLayoutContainer(r5)
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r5 = mainFragmentsStack
            r1.init(r5)
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            r1.setDelegate(r13)
            org.telegram.ui.ActionBar.Theme.loadWallpaper()
            org.telegram.ui.Components.PasscodeView r1 = new org.telegram.ui.Components.PasscodeView
            r1.<init>(r13)
            r13.passcodeView = r1
            org.telegram.ui.ActionBar.DrawerLayoutContainer r5 = r13.drawerLayoutContainer
            android.widget.FrameLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r11)
            r5.addView(r1, r8)
            r13.checkCurrentAccount()
            int r1 = r13.currentAccount
            r13.updateCurrentConnectionState(r1)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities
            java.lang.Object[] r8 = new java.lang.Object[r2]
            r8[r3] = r13
            r1.postNotificationName(r5, r8)
            int r1 = r13.currentAccount
            org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
            int r1 = r1.getConnectionState()
            r13.currentConnectionState = r1
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.needShowAlert
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.reloadInterface
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.suggestedLangpack
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.didSetNewTheme
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.needSetDayNightTheme
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r8 = org.telegram.messenger.NotificationCenter.needCheckSystemBarColors
            r1.addObserver(r13, r8)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            r1.addObserver(r13, r5)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.didSetPasscode
            r1.addObserver(r13, r5)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.didSetNewWallpapper
            r1.addObserver(r13, r5)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.notificationsCountUpdated
            r1.addObserver(r13, r5)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.screenStateChanged
            r1.addObserver(r13, r5)
            org.telegram.messenger.NotificationCenter r1 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r5 = org.telegram.messenger.NotificationCenter.showBulletin
            r1.addObserver(r13, r5)
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            boolean r1 = r1.isEmpty()
            if (r1 == 0) goto L_0x0532
            int r1 = r13.currentAccount
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            boolean r1 = r1.isClientActivated()
            if (r1 != 0) goto L_0x0439
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            org.telegram.ui.LoginActivity r4 = new org.telegram.ui.LoginActivity
            r4.<init>()
            r1.addFragmentToStack(r4)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r1 = r13.drawerLayoutContainer
            r1.setAllowOpenDrawer(r3, r3)
            goto L_0x044d
        L_0x0439:
            org.telegram.ui.DialogsActivity r1 = new org.telegram.ui.DialogsActivity
            r1.<init>(r4)
            org.telegram.ui.Components.RecyclerListView r4 = r13.sideMenu
            r1.setSideMenu(r4)
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout
            r4.addFragmentToStack(r1)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r1 = r13.drawerLayoutContainer
            r1.setAllowOpenDrawer(r2, r3)
        L_0x044d:
            if (r14 == 0) goto L_0x0599
            java.lang.String r1 = "fragment"
            java.lang.String r1 = r14.getString(r1)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x0599
            java.lang.String r4 = "args"
            android.os.Bundle r4 = r14.getBundle(r4)     // Catch:{ Exception -> 0x052d }
            int r5 = r1.hashCode()     // Catch:{ Exception -> 0x052d }
            r8 = 5
            r10 = 4
            r11 = 2
            switch(r5) {
                case -1529105743: goto L_0x049a;
                case -1349522494: goto L_0x0490;
                case 3052376: goto L_0x0486;
                case 98629247: goto L_0x047c;
                case 738950403: goto L_0x0472;
                case 1434631203: goto L_0x0468;
                default: goto L_0x0467;
            }     // Catch:{ Exception -> 0x052d }
        L_0x0467:
            goto L_0x04a4
        L_0x0468:
            java.lang.String r5 = "settings"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 1
            goto L_0x04a4
        L_0x0472:
            java.lang.String r5 = "channel"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 3
            goto L_0x04a4
        L_0x047c:
            java.lang.String r5 = "group"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 2
            goto L_0x04a4
        L_0x0486:
            java.lang.String r5 = "chat"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 0
            goto L_0x04a4
        L_0x0490:
            java.lang.String r5 = "chat_profile"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 4
            goto L_0x04a4
        L_0x049a:
            java.lang.String r5 = "wallpapers"
            boolean r1 = r1.equals(r5)     // Catch:{ Exception -> 0x052d }
            if (r1 == 0) goto L_0x04a4
            r9 = 5
        L_0x04a4:
            if (r9 == 0) goto L_0x051a
            if (r9 == r2) goto L_0x04fd
            if (r9 == r11) goto L_0x04e9
            if (r9 == r7) goto L_0x04d5
            if (r9 == r10) goto L_0x04c1
            if (r9 == r8) goto L_0x04b2
            goto L_0x0599
        L_0x04b2:
            org.telegram.ui.WallpapersListActivity r1 = new org.telegram.ui.WallpapersListActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r3)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x04c1:
            if (r4 == 0) goto L_0x0599
            org.telegram.ui.ProfileActivity r1 = new org.telegram.ui.ProfileActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r4)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            boolean r4 = r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            if (r4 == 0) goto L_0x0599
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x04d5:
            if (r4 == 0) goto L_0x0599
            org.telegram.ui.ChannelCreateActivity r1 = new org.telegram.ui.ChannelCreateActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r4)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            boolean r4 = r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            if (r4 == 0) goto L_0x0599
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x04e9:
            if (r4 == 0) goto L_0x0599
            org.telegram.ui.GroupCreateFinalActivity r1 = new org.telegram.ui.GroupCreateFinalActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r4)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            boolean r4 = r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            if (r4 == 0) goto L_0x0599
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x04fd:
            java.lang.String r1 = "user_id"
            int r5 = r13.currentAccount     // Catch:{ Exception -> 0x052d }
            org.telegram.messenger.UserConfig r5 = org.telegram.messenger.UserConfig.getInstance(r5)     // Catch:{ Exception -> 0x052d }
            int r5 = r5.clientUserId     // Catch:{ Exception -> 0x052d }
            r4.putInt(r1, r5)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ProfileActivity r1 = new org.telegram.ui.ProfileActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r4)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x051a:
            if (r4 == 0) goto L_0x0599
            org.telegram.ui.ChatActivity r1 = new org.telegram.ui.ChatActivity     // Catch:{ Exception -> 0x052d }
            r1.<init>(r4)     // Catch:{ Exception -> 0x052d }
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout     // Catch:{ Exception -> 0x052d }
            boolean r4 = r4.addFragmentToStack(r1)     // Catch:{ Exception -> 0x052d }
            if (r4 == 0) goto L_0x0599
            r1.restoreSelfArgs(r14)     // Catch:{ Exception -> 0x052d }
            goto L_0x0599
        L_0x052d:
            r1 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
            goto L_0x0599
        L_0x0532:
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            java.lang.Object r1 = r1.get(r3)
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1
            boolean r4 = r1 instanceof org.telegram.ui.DialogsActivity
            if (r4 == 0) goto L_0x0547
            org.telegram.ui.DialogsActivity r1 = (org.telegram.ui.DialogsActivity) r1
            org.telegram.ui.Components.RecyclerListView r4 = r13.sideMenu
            r1.setSideMenu(r4)
        L_0x0547:
            boolean r1 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r1 == 0) goto L_0x057c
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            int r1 = r1.size()
            if (r1 > r2) goto L_0x0563
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r13.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            boolean r1 = r1.isEmpty()
            if (r1 == 0) goto L_0x0563
            r1 = 1
            goto L_0x0564
        L_0x0563:
            r1 = 0
        L_0x0564:
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r4 = r4.fragmentsStack
            int r4 = r4.size()
            if (r4 != r2) goto L_0x057d
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r4 = r4.fragmentsStack
            java.lang.Object r4 = r4.get(r3)
            boolean r4 = r4 instanceof org.telegram.ui.LoginActivity
            if (r4 == 0) goto L_0x057d
            r1 = 0
            goto L_0x057d
        L_0x057c:
            r1 = 1
        L_0x057d:
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r4 = r4.fragmentsStack
            int r4 = r4.size()
            if (r4 != r2) goto L_0x0594
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r13.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r4 = r4.fragmentsStack
            java.lang.Object r4 = r4.get(r3)
            boolean r4 = r4 instanceof org.telegram.ui.LoginActivity
            if (r4 == 0) goto L_0x0594
            r1 = 0
        L_0x0594:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r4 = r13.drawerLayoutContainer
            r4.setAllowOpenDrawer(r1, r3)
        L_0x0599:
            r13.checkLayout()
            r13.checkSystemBarColors()
            android.content.Intent r1 = r13.getIntent()
            if (r14 == 0) goto L_0x05a7
            r14 = 1
            goto L_0x05a8
        L_0x05a7:
            r14 = 0
        L_0x05a8:
            r13.handleIntent(r1, r3, r14, r3)
            java.lang.String r14 = android.os.Build.DISPLAY     // Catch:{ Exception -> 0x060c }
            java.lang.String r1 = android.os.Build.USER     // Catch:{ Exception -> 0x060c }
            java.lang.String r3 = ""
            if (r14 == 0) goto L_0x05b8
            java.lang.String r14 = r14.toLowerCase()     // Catch:{ Exception -> 0x060c }
            goto L_0x05b9
        L_0x05b8:
            r14 = r3
        L_0x05b9:
            if (r1 == 0) goto L_0x05bf
            java.lang.String r3 = r14.toLowerCase()     // Catch:{ Exception -> 0x060c }
        L_0x05bf:
            boolean r1 = org.telegram.messenger.BuildVars.LOGS_ENABLED     // Catch:{ Exception -> 0x060c }
            if (r1 == 0) goto L_0x05df
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x060c }
            r1.<init>()     // Catch:{ Exception -> 0x060c }
            java.lang.String r4 = "OS name "
            r1.append(r4)     // Catch:{ Exception -> 0x060c }
            r1.append(r14)     // Catch:{ Exception -> 0x060c }
            java.lang.String r4 = " "
            r1.append(r4)     // Catch:{ Exception -> 0x060c }
            r1.append(r3)     // Catch:{ Exception -> 0x060c }
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x060c }
            org.telegram.messenger.FileLog.d(r1)     // Catch:{ Exception -> 0x060c }
        L_0x05df:
            boolean r14 = r14.contains(r0)     // Catch:{ Exception -> 0x060c }
            if (r14 != 0) goto L_0x05eb
            boolean r14 = r3.contains(r0)     // Catch:{ Exception -> 0x060c }
            if (r14 == 0) goto L_0x0610
        L_0x05eb:
            int r14 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x060c }
            if (r14 > r6) goto L_0x0610
            org.telegram.messenger.AndroidUtilities.incorrectDisplaySizeFix = r2     // Catch:{ Exception -> 0x060c }
            android.view.Window r14 = r13.getWindow()     // Catch:{ Exception -> 0x060c }
            android.view.View r14 = r14.getDecorView()     // Catch:{ Exception -> 0x060c }
            android.view.View r14 = r14.getRootView()     // Catch:{ Exception -> 0x060c }
            android.view.ViewTreeObserver r0 = r14.getViewTreeObserver()     // Catch:{ Exception -> 0x060c }
            org.telegram.ui.-$$Lambda$LaunchActivity$ZFrSHrKcJLvy27CVdXXiDrROQmE r1 = new org.telegram.ui.-$$Lambda$LaunchActivity$ZFrSHrKcJLvy27CVdXXiDrROQmE     // Catch:{ Exception -> 0x060c }
            r1.<init>(r14)     // Catch:{ Exception -> 0x060c }
            r13.onGlobalLayoutListener = r1     // Catch:{ Exception -> 0x060c }
            r0.addOnGlobalLayoutListener(r1)     // Catch:{ Exception -> 0x060c }
            goto L_0x0610
        L_0x060c:
            r14 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r14)
        L_0x0610:
            org.telegram.messenger.MediaController r14 = org.telegram.messenger.MediaController.getInstance()
            r14.setBaseActivity(r13, r2)
            org.telegram.messenger.AndroidUtilities.startAppCenter(r13)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.onCreate(android.os.Bundle):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ boolean lambda$onCreate$0$LaunchActivity(View view, MotionEvent motionEvent) {
        if (!this.actionBarLayout.fragmentsStack.isEmpty() && motionEvent.getAction() == 1) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            int[] iArr = new int[2];
            this.layersActionBarLayout.getLocationOnScreen(iArr);
            int i = iArr[0];
            int i2 = iArr[1];
            if (!this.layersActionBarLayout.checkTransitionAnimation() && (x <= ((float) i) || x >= ((float) (i + this.layersActionBarLayout.getWidth())) || y <= ((float) i2) || y >= ((float) (i2 + this.layersActionBarLayout.getHeight())))) {
                if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        ActionBarLayout actionBarLayout2 = this.layersActionBarLayout;
                        actionBarLayout2.removeFragmentFromStack(actionBarLayout2.fragmentsStack.get(0));
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                }
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$2 */
    public /* synthetic */ void lambda$onCreate$2$LaunchActivity(View view, int i, float f, float f2) {
        boolean z = true;
        if (i == 0) {
            DrawerProfileCell drawerProfileCell = (DrawerProfileCell) view;
            if (drawerProfileCell.isInAvatar(f, f2)) {
                openSettings(drawerProfileCell.hasAvatar());
                return;
            }
            DrawerLayoutAdapter drawerLayoutAdapter2 = this.drawerLayoutAdapter;
            drawerLayoutAdapter2.setAccountsShown(!drawerLayoutAdapter2.isAccountsShown(), true);
        } else if (view instanceof DrawerUserCell) {
            switchToAccount(((DrawerUserCell) view).getAccountNumber(), true);
            this.drawerLayoutContainer.closeDrawer(false);
        } else if (view instanceof DrawerAddCell) {
            int i2 = -1;
            int i3 = 0;
            while (true) {
                if (i3 >= 3) {
                    break;
                } else if (!UserConfig.getInstance(i3).isClientActivated()) {
                    i2 = i3;
                    break;
                } else {
                    i3++;
                }
            }
            if (i2 >= 0) {
                lambda$runLinkRequest$41(new LoginActivity(i2));
            }
            this.drawerLayoutContainer.closeDrawer(false);
        } else {
            int id = this.drawerLayoutAdapter.getId(i);
            if (id == 2) {
                lambda$runLinkRequest$41(new GroupCreateActivity(new Bundle()));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 3) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("onlyUsers", true);
                bundle.putBoolean("destroyAfterSelect", true);
                bundle.putBoolean("createSecretChat", true);
                bundle.putBoolean("allowBots", false);
                bundle.putBoolean("allowSelf", false);
                lambda$runLinkRequest$41(new ContactsActivity(bundle));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 4) {
                SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                if (BuildVars.DEBUG_VERSION || !globalMainSettings.getBoolean("channel_intro", false)) {
                    lambda$runLinkRequest$41(new ActionIntroActivity(0));
                    globalMainSettings.edit().putBoolean("channel_intro", true).commit();
                } else {
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt("step", 0);
                    lambda$runLinkRequest$41(new ChannelCreateActivity(bundle2));
                }
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 6) {
                lambda$runLinkRequest$41(new ContactsActivity((Bundle) null));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 7) {
                lambda$runLinkRequest$41(new InviteContactsActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 8) {
                openSettings(false);
            } else if (id == 9) {
                Browser.openUrl((Context) this, LocaleController.getString("TelegramFaqUrl", NUM));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 10) {
                lambda$runLinkRequest$41(new CallLogActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 11) {
                Bundle bundle3 = new Bundle();
                bundle3.putInt("user_id", UserConfig.getInstance(this.currentAccount).getClientUserId());
                lambda$runLinkRequest$41(new ChatActivity(bundle3));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 12) {
                int i4 = Build.VERSION.SDK_INT;
                if (i4 < 23 || checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0) {
                    if (i4 >= 28) {
                        z = ((LocationManager) ApplicationLoader.applicationContext.getSystemService("location")).isLocationEnabled();
                    } else if (i4 >= 19) {
                        try {
                            if (Settings.Secure.getInt(ApplicationLoader.applicationContext.getContentResolver(), "location_mode", 0) == 0) {
                                z = false;
                            }
                        } catch (Throwable th) {
                            FileLog.e(th);
                        }
                    }
                    if (z) {
                        lambda$runLinkRequest$41(new PeopleNearbyActivity());
                    } else {
                        lambda$runLinkRequest$41(new ActionIntroActivity(4));
                    }
                    this.drawerLayoutContainer.closeDrawer(false);
                    return;
                }
                lambda$runLinkRequest$41(new ActionIntroActivity(1));
                this.drawerLayoutContainer.closeDrawer(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$3 */
    public /* synthetic */ boolean lambda$onCreate$3$LaunchActivity(ItemTouchHelper itemTouchHelper, View view, int i) {
        if (!(view instanceof DrawerUserCell)) {
            return false;
        }
        final int accountNumber = ((DrawerUserCell) view).getAccountNumber();
        if (accountNumber == this.currentAccount || AndroidUtilities.isTablet()) {
            itemTouchHelper.startDrag(this.sideMenu.getChildViewHolder(view));
            return false;
        }
        AnonymousClass6 r2 = new DialogsActivity((Bundle) null) {
            /* access modifiers changed from: protected */
            public void onTransitionAnimationEnd(boolean z, boolean z2) {
                super.onTransitionAnimationEnd(z, z2);
                if (!z && z2) {
                    LaunchActivity.this.drawerLayoutContainer.setDrawCurrentPreviewFragmentAbove(false);
                }
            }

            /* access modifiers changed from: protected */
            public void onPreviewOpenAnimationEnd() {
                super.onPreviewOpenAnimationEnd();
                LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                LaunchActivity.this.drawerLayoutContainer.setDrawCurrentPreviewFragmentAbove(false);
                LaunchActivity.this.switchToAccount(accountNumber, true);
            }
        };
        r2.setCurrentAccount(accountNumber);
        this.actionBarLayout.presentFragmentAsPreview(r2);
        this.drawerLayoutContainer.setDrawCurrentPreviewFragmentAbove(true);
        return true;
    }

    static /* synthetic */ void lambda$onCreate$4(View view) {
        int measuredHeight = view.getMeasuredHeight();
        FileLog.d("height = " + measuredHeight + " displayHeight = " + AndroidUtilities.displaySize.y);
        if (Build.VERSION.SDK_INT >= 21) {
            measuredHeight -= AndroidUtilities.statusBarHeight;
        }
        if (measuredHeight > AndroidUtilities.dp(100.0f) && measuredHeight < AndroidUtilities.displaySize.y) {
            int dp = AndroidUtilities.dp(100.0f) + measuredHeight;
            Point point = AndroidUtilities.displaySize;
            if (dp > point.y) {
                point.y = measuredHeight;
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("fix display size y to " + AndroidUtilities.displaySize.y);
                }
            }
        }
    }

    private void openSettings(boolean z) {
        Bundle bundle = new Bundle();
        bundle.putInt("user_id", UserConfig.getInstance(this.currentAccount).clientUserId);
        if (z) {
            bundle.putBoolean("expandPhoto", true);
        }
        lambda$runLinkRequest$41(new ProfileActivity(bundle));
        this.drawerLayoutContainer.closeDrawer(false);
    }

    private void checkSystemBarColors() {
        int i = Build.VERSION.SDK_INT;
        if (i >= 23) {
            boolean z = true;
            AndroidUtilities.setLightStatusBar(getWindow(), Theme.getColor("actionBarDefault", (boolean[]) null, true) == -1);
            if (i >= 26) {
                Window window = getWindow();
                int color = Theme.getColor("windowBackgroundGray", (boolean[]) null, true);
                if (window.getNavigationBarColor() != color) {
                    window.setNavigationBarColor(color);
                    float computePerceivedBrightness = AndroidUtilities.computePerceivedBrightness(color);
                    Window window2 = getWindow();
                    if (computePerceivedBrightness < 0.721f) {
                        z = false;
                    }
                    AndroidUtilities.setLightNavigationBar(window2, z);
                }
            }
        }
        if (SharedConfig.noStatusBar) {
            getWindow().setStatusBarColor(0);
        }
    }

    public void switchToAccount(int i, boolean z) {
        if (i != UserConfig.selectedAccount) {
            ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
            UserConfig.selectedAccount = i;
            UserConfig.getInstance(0).saveConfig(false);
            checkCurrentAccount();
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.removeAllFragments();
                this.rightActionBarLayout.removeAllFragments();
                if (!this.tabletFullSize) {
                    this.shadowTabletSide.setVisibility(0);
                    if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        this.backgroundTablet.setVisibility(0);
                    }
                    this.rightActionBarLayout.setVisibility(8);
                }
                this.layersActionBarLayout.setVisibility(8);
            }
            if (z) {
                this.actionBarLayout.removeAllFragments();
            } else {
                this.actionBarLayout.removeFragmentFromStack(0);
            }
            DialogsActivity dialogsActivity = new DialogsActivity((Bundle) null);
            dialogsActivity.setSideMenu(this.sideMenu);
            this.actionBarLayout.addFragmentToStack(dialogsActivity, 0);
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            this.actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.showLastFragment();
                this.rightActionBarLayout.showLastFragment();
            }
            if (!ApplicationLoader.mainInterfacePaused) {
                ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
            }
            if (UserConfig.getInstance(i).unacceptedTermsOfService != null) {
                showTosActivity(i, UserConfig.getInstance(i).unacceptedTermsOfService);
            }
            updateCurrentConnectionState(this.currentAccount);
        }
    }

    private void switchToAvailableAccountOrLogout() {
        int i = 0;
        while (true) {
            if (i >= 3) {
                i = -1;
                break;
            } else if (UserConfig.getInstance(i).isClientActivated()) {
                break;
            } else {
                i++;
            }
        }
        TermsOfServiceView termsOfServiceView2 = this.termsOfServiceView;
        if (termsOfServiceView2 != null) {
            termsOfServiceView2.setVisibility(8);
        }
        if (i != -1) {
            switchToAccount(i, true);
            return;
        }
        DrawerLayoutAdapter drawerLayoutAdapter2 = this.drawerLayoutAdapter;
        if (drawerLayoutAdapter2 != null) {
            drawerLayoutAdapter2.notifyDataSetChanged();
        }
        Iterator<BaseFragment> it = this.actionBarLayout.fragmentsStack.iterator();
        while (it.hasNext()) {
            it.next().onFragmentDestroy();
        }
        this.actionBarLayout.fragmentsStack.clear();
        if (AndroidUtilities.isTablet()) {
            Iterator<BaseFragment> it2 = this.layersActionBarLayout.fragmentsStack.iterator();
            while (it2.hasNext()) {
                it2.next().onFragmentDestroy();
            }
            this.layersActionBarLayout.fragmentsStack.clear();
            Iterator<BaseFragment> it3 = this.rightActionBarLayout.fragmentsStack.iterator();
            while (it3.hasNext()) {
                it3.next().onFragmentDestroy();
            }
            this.rightActionBarLayout.fragmentsStack.clear();
        }
        startActivity(new Intent(this, IntroActivity.class));
        onFinish();
        finish();
    }

    public int getMainFragmentsCount() {
        return mainFragmentsStack.size();
    }

    private void checkCurrentAccount() {
        int i = this.currentAccount;
        if (i != UserConfig.selectedAccount) {
            NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowPlayServicesAlert);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidFailToLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.historyImportProgressChanged);
        }
        int i2 = UserConfig.selectedAccount;
        this.currentAccount = i2;
        NotificationCenter.getInstance(i2).addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didUpdateConnectionState);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needShowAlert);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.openArticle);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.hasNewContactsToImport);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needShowPlayServicesAlert);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.fileDidFailToLoad);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.historyImportProgressChanged);
    }

    private void checkLayout() {
        if (AndroidUtilities.isTablet() && this.rightActionBarLayout != null) {
            int i = 0;
            if (AndroidUtilities.isInMultiwindow || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
                this.tabletFullSize = true;
                if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    while (this.rightActionBarLayout.fragmentsStack.size() > 0) {
                        BaseFragment baseFragment = this.rightActionBarLayout.fragmentsStack.get(0);
                        if (baseFragment instanceof ChatActivity) {
                            ((ChatActivity) baseFragment).setIgnoreAttachOnPause(true);
                        }
                        baseFragment.onPause();
                        this.rightActionBarLayout.fragmentsStack.remove(0);
                        this.actionBarLayout.fragmentsStack.add(baseFragment);
                    }
                    if (this.passcodeView.getVisibility() != 0) {
                        this.actionBarLayout.showLastFragment();
                    }
                }
                this.shadowTabletSide.setVisibility(8);
                this.rightActionBarLayout.setVisibility(8);
                View view = this.backgroundTablet;
                if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    i = 8;
                }
                view.setVisibility(i);
                return;
            }
            this.tabletFullSize = false;
            if (this.actionBarLayout.fragmentsStack.size() >= 2) {
                while (1 < this.actionBarLayout.fragmentsStack.size()) {
                    BaseFragment baseFragment2 = this.actionBarLayout.fragmentsStack.get(1);
                    if (baseFragment2 instanceof ChatActivity) {
                        ((ChatActivity) baseFragment2).setIgnoreAttachOnPause(true);
                    }
                    baseFragment2.onPause();
                    this.actionBarLayout.fragmentsStack.remove(1);
                    this.rightActionBarLayout.fragmentsStack.add(baseFragment2);
                }
                if (this.passcodeView.getVisibility() != 0) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                }
            }
            ActionBarLayout actionBarLayout2 = this.rightActionBarLayout;
            actionBarLayout2.setVisibility(actionBarLayout2.fragmentsStack.isEmpty() ? 8 : 0);
            this.backgroundTablet.setVisibility(this.rightActionBarLayout.fragmentsStack.isEmpty() ? 0 : 8);
            FrameLayout frameLayout2 = this.shadowTabletSide;
            if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                i = 8;
            }
            frameLayout2.setVisibility(i);
        }
    }

    private void showUpdateActivity(int i, TLRPC$TL_help_appUpdate tLRPC$TL_help_appUpdate, boolean z) {
        if (this.blockingUpdateView == null) {
            AnonymousClass7 r0 = new BlockingUpdateView(this) {
                public void setVisibility(int i) {
                    super.setVisibility(i);
                    if (i == 8) {
                        LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                }
            };
            this.blockingUpdateView = r0;
            this.drawerLayoutContainer.addView(r0, LayoutHelper.createFrame(-1, -1.0f));
        }
        this.blockingUpdateView.show(i, tLRPC$TL_help_appUpdate, z);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
    }

    private void showTosActivity(int i, TLRPC$TL_help_termsOfService tLRPC$TL_help_termsOfService) {
        if (this.termsOfServiceView == null) {
            TermsOfServiceView termsOfServiceView2 = new TermsOfServiceView(this);
            this.termsOfServiceView = termsOfServiceView2;
            termsOfServiceView2.setAlpha(0.0f);
            this.drawerLayoutContainer.addView(this.termsOfServiceView, LayoutHelper.createFrame(-1, -1.0f));
            this.termsOfServiceView.setDelegate(new TermsOfServiceView.TermsOfServiceViewDelegate() {
                public void onAcceptTerms(int i) {
                    UserConfig.getInstance(i).unacceptedTermsOfService = null;
                    UserConfig.getInstance(i).saveConfig(false);
                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (LaunchActivity.mainFragmentsStack.size() > 0) {
                        ((BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1)).onResume();
                    }
                    LaunchActivity.this.termsOfServiceView.animate().alpha(0.0f).setDuration(150).setInterpolator(AndroidUtilities.accelerateInterpolator).withEndAction(new Runnable() {
                        public final void run() {
                            LaunchActivity.AnonymousClass8.this.lambda$onAcceptTerms$0$LaunchActivity$8();
                        }
                    }).start();
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onAcceptTerms$0 */
                public /* synthetic */ void lambda$onAcceptTerms$0$LaunchActivity$8() {
                    LaunchActivity.this.termsOfServiceView.setVisibility(8);
                }
            });
        }
        TLRPC$TL_help_termsOfService tLRPC$TL_help_termsOfService2 = UserConfig.getInstance(i).unacceptedTermsOfService;
        if (tLRPC$TL_help_termsOfService2 != tLRPC$TL_help_termsOfService && (tLRPC$TL_help_termsOfService2 == null || !tLRPC$TL_help_termsOfService2.id.data.equals(tLRPC$TL_help_termsOfService.id.data))) {
            UserConfig.getInstance(i).unacceptedTermsOfService = tLRPC$TL_help_termsOfService;
            UserConfig.getInstance(i).saveConfig(false);
        }
        this.termsOfServiceView.show(i, tLRPC$TL_help_termsOfService);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
        this.termsOfServiceView.animate().alpha(1.0f).setDuration(150).setInterpolator(AndroidUtilities.decelerateInterpolator).setListener((Animator.AnimatorListener) null).start();
    }

    /* access modifiers changed from: private */
    public void showPasscodeActivity() {
        if (this.passcodeView != null) {
            SharedConfig.appLocked = true;
            if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
                SecretMediaViewer.getInstance().closePhoto(false, false);
            } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                PhotoViewer.getInstance().closePhoto(false, true);
            } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                ArticleViewer.getInstance().close(false, true);
            }
            this.passcodeView.onShow();
            SharedConfig.isWaitingForPasscodeEnter = true;
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            this.passcodeView.setDelegate(new PasscodeView.PasscodeViewDelegate() {
                public final void didAcceptedPassword() {
                    LaunchActivity.this.lambda$showPasscodeActivity$5$LaunchActivity();
                }
            });
            this.actionBarLayout.setVisibility(4);
            if (AndroidUtilities.isTablet()) {
                if (this.layersActionBarLayout.getVisibility() == 0) {
                    this.layersActionBarLayout.setVisibility(4);
                }
                this.rightActionBarLayout.setVisibility(4);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPasscodeActivity$5 */
    public /* synthetic */ void lambda$showPasscodeActivity$5$LaunchActivity() {
        SharedConfig.isWaitingForPasscodeEnter = false;
        Intent intent = this.passcodeSaveIntent;
        if (intent != null) {
            handleIntent(intent, this.passcodeSaveIntentIsNew, this.passcodeSaveIntentIsRestore, true);
            this.passcodeSaveIntent = null;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        this.actionBarLayout.setVisibility(0);
        this.actionBarLayout.showLastFragment();
        if (AndroidUtilities.isTablet()) {
            this.layersActionBarLayout.showLastFragment();
            this.rightActionBarLayout.showLastFragment();
            if (this.layersActionBarLayout.getVisibility() == 4) {
                this.layersActionBarLayout.setVisibility(0);
            }
            this.rightActionBarLayout.setVisibility(0);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v3, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v4, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v60, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v61, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v62, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v63, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v48, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v64, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v65, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v66, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v67, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v76, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v77, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v53, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v78, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v12, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v80, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v58, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v14, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v17, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v20, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v81, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v70, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v82, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v34, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v83, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v36, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v0, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v0, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v0, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v0, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v84, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v182, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v38, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v41, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v1, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v1, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v1, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v92, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v210, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v2, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v2, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v2, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v41, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v93, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v211, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v3, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r48v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v39, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v98, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v219, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v4, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v4, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v2, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v220, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v99, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v41, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r48v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v221, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v100, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v48, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r48v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v101, resolved type: java.util.HashMap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r48v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v7, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v5, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v7, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v8, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v59, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v103, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v224, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v60, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v61, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v109, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v110, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v230, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v9, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v7, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v7, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v62, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v111, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v231, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v10, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v8, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v8, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v112, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v63, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v11, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v9, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v9, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v113, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v64, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v34, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v53, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v11, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v111, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v114, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v65, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v238, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v117, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v66, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v3, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v55, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v48, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v71, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v92, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v118, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v246, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v4, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v56, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v72, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v93, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v119, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v247, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v5, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v57, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v73, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v94, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v120, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v248, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v6, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v58, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v74, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v95, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v121, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v249, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v7, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v59, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v75, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v96, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v122, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v250, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v8, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v60, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v53, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v76, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v97, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v123, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v251, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v9, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v61, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v48, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v77, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v98, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v124, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v252, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v10, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v62, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v55, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v78, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v99, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v125, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v253, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v63, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v56, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v79, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v100, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v126, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v254, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v64, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v57, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v80, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v101, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v127, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v255, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v65, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v58, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v81, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v102, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v128, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v256, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v82, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v103, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v129, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v257, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v53, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v59, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v66, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v262, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v132, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v104, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v83, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v84, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v268, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v135, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v105, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v106, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v107, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v276, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v277, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v67, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v278, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v136, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v109, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v90, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v60, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v279, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v112, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v114, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v115, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v117, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v94, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v55, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v61, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v68, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v13, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v16, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v18, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v16, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v18, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v141, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v289, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v297, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v298, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v149, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v118, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v95, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v56, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v62, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v69, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v14, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v307, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v158, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v119, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v96, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v57, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v63, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v70, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v316, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v171, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v123, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v97, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v58, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v64, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v71, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v15, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v322, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v180, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v131, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v99, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v59, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v65, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v72, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v16, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v328, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v187, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v132, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v100, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v60, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v66, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v73, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v17, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v20, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v22, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v20, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v22, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v337, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v192, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v133, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v101, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v61, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v67, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v74, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v339, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v193, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v134, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v102, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v62, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v68, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v75, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v31, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v341, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v194, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v135, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v103, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v63, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v69, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v76, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v342, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v195, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v136, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v104, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v64, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v70, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v77, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v343, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v196, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v137, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v105, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v65, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v71, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v78, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v34, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v138, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v349, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v203, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v139, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v106, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v66, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v72, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v79, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v35, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v23, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v140, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v350, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v208, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v141, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v107, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v67, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v73, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v80, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v24, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v70, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v68, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v352, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v220, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v143, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v109, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v69, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v74, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v81, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v25, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v144, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v72, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v146, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v147, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v73, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v74, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v366, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v233, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v151, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v113, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v75, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v75, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v82, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v26, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v376, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v237, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v152, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v114, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v76, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v76, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v83, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v39, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v27, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v382, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v383, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v241, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v153, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v115, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v77, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v77, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v84, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v28, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v384, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v100, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v85, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v78, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v103, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v154, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v242, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v385, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v78, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v155, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r49v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r54v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r51v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r53v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v22, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v24, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v22, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v24, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v30, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r52v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v120, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v243, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v31, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v35, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v35, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v86, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v79, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v105, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v156, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v244, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v386, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v41, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v87, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v80, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v106, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v157, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v245, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v387, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v88, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v81, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v107, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v158, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v246, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v388, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v89, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v82, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v108, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v159, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v247, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v389, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v90, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v83, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v109, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v160, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v248, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v390, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v39, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v91, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v84, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v110, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v161, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v249, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v391, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v92, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v85, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v111, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v162, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v250, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v392, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v251, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v393, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v163, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v86, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v93, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v394, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v252, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v395, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v164, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v165, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v397, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v399, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v253, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v167, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v87, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v401, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v170, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v172, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v173, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v174, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v179, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v34, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v39, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v111, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v267, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v407, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v92, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v125, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v48, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v408, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v268, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v112, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v41, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v35, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v49, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v409, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v269, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v113, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v41, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v180, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v145, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v181, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v182, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v411, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v277, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v187, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v128, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v94, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v114, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v50, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v42, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v418, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v284, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v193, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v129, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v95, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v117, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v51, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v421, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v285, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v194, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v130, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v96, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v118, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v52, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v423, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v287, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v195, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v131, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v97, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v119, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v53, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v288, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v426, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v428, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v430, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v435, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v43, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v436, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v289, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v196, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v132, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v98, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v120, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v439, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v292, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v198, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v135, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v99, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v121, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v55, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r28v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v44, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r68v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v190, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v304, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v55, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v56, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v57, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v58, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v59, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v60, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v61, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v62, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v63, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v158, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v23, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v25, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v23, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r41v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v24, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v26, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v80, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v25, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v27, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v26, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v25, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v28, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r40v31, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v240, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v321, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v208, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v27, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v26, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v29, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v25, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r42v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v159, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r47v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r46v28, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r45v27, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r44v30, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r43v26, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v39, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v40, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v41, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v42, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v43, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r39v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v44, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v45, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v46, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v47, resolved type: boolean} */
    /* JADX WARNING: type inference failed for: r2v2, types: [android.os.Bundle, java.lang.String] */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* JADX WARNING: type inference failed for: r7v6 */
    /* JADX WARNING: type inference failed for: r7v8 */
    /* JADX WARNING: type inference failed for: r7v11 */
    /* JADX WARNING: type inference failed for: r7v12 */
    /* JADX WARNING: type inference failed for: r20v10, types: [org.telegram.tgnet.TLRPC$TL_wallPaper] */
    /* JADX WARNING: type inference failed for: r1v116, types: [org.telegram.tgnet.TLRPC$TL_wallPaper, org.telegram.tgnet.TLRPC$WallPaper] */
    /* JADX WARNING: type inference failed for: r48v11 */
    /* JADX WARNING: type inference failed for: r48v12 */
    /* JADX WARNING: type inference failed for: r48v13 */
    /* JADX WARNING: type inference failed for: r48v14 */
    /* JADX WARNING: type inference failed for: r48v15 */
    /* JADX WARNING: type inference failed for: r48v16 */
    /* JADX WARNING: type inference failed for: r48v17 */
    /* JADX WARNING: type inference failed for: r48v18 */
    /* JADX WARNING: type inference failed for: r48v19 */
    /* JADX WARNING: type inference failed for: r48v20 */
    /* JADX WARNING: type inference failed for: r48v21 */
    /* JADX WARNING: type inference failed for: r48v22 */
    /* JADX WARNING: type inference failed for: r1v291, types: [org.telegram.tgnet.TLRPC$TL_wallPaper, org.telegram.tgnet.TLRPC$WallPaper] */
    /* JADX WARNING: type inference failed for: r2v203 */
    /* JADX WARNING: type inference failed for: r2v205 */
    /* JADX WARNING: type inference failed for: r2v207 */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0314, code lost:
        if (r15.sendingText == null) goto L_0x018f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x053b, code lost:
        r9 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:422:0x084b, code lost:
        if (r1.intValue() == 0) goto L_0x084d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0114, code lost:
        r0 = r22.getIntent().getExtras();
        r12 = r0.getLong("dialogId", 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        r0 = r0.getString("hash", (java.lang.String) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0126, code lost:
        r18 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0129, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x012a, code lost:
        r18 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0147, code lost:
        if (r2.equals(r0) != false) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:719:0x1285, code lost:
        if (r2 == 0) goto L_0x1287;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:923:0x1873, code lost:
        if (r1.checkCanOpenChat(r0, r2.get(r2.size() - r7)) != false) goto L_0x1875;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:940:0x18de, code lost:
        if (r1.checkCanOpenChat(r0, r2.get(r2.size() - r7)) != false) goto L_0x18e0;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=int, code=?, for r7v5, types: [boolean, int] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:833:0x158d */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:1011:0x1a2a  */
    /* JADX WARNING: Removed duplicated region for block: B:1012:0x1a34  */
    /* JADX WARNING: Removed duplicated region for block: B:1015:0x1a42  */
    /* JADX WARNING: Removed duplicated region for block: B:1016:0x1a53  */
    /* JADX WARNING: Removed duplicated region for block: B:1077:0x1CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1084:0x1CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:1095:0x1cd1  */
    /* JADX WARNING: Removed duplicated region for block: B:1106:0x1d1d  */
    /* JADX WARNING: Removed duplicated region for block: B:1108:0x1d29  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x031b  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0383 A[Catch:{ Exception -> 0x04a9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:264:0x04b0  */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x0691 A[Catch:{ Exception -> 0x069d }] */
    /* JADX WARNING: Removed duplicated region for block: B:608:0x0ee9 A[SYNTHETIC, Splitter:B:608:0x0ee9] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0143  */
    /* JADX WARNING: Removed duplicated region for block: B:713:0x121e A[Catch:{ Exception -> 0x122a }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0159 A[SYNTHETIC, Splitter:B:71:0x0159] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0192  */
    /* JADX WARNING: Removed duplicated region for block: B:838:0x1596 A[SYNTHETIC, Splitter:B:838:0x1596] */
    /* JADX WARNING: Removed duplicated region for block: B:902:0x180d  */
    /* JADX WARNING: Removed duplicated region for block: B:915:0x183e  */
    /* JADX WARNING: Removed duplicated region for block: B:933:0x18ad  */
    /* JADX WARNING: Unknown variable types count: 2 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleIntent(android.content.Intent r66, boolean r67, boolean r68, boolean r69) {
        /*
            r65 = this;
            r15 = r65
            r14 = r66
            r0 = r68
            boolean r1 = org.telegram.messenger.AndroidUtilities.handleProxyIntent(r65, r66)
            r13 = 1
            if (r1 == 0) goto L_0x0023
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r15.actionBarLayout
            r0.showLastFragment()
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x0022
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r15.layersActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r15.rightActionBarLayout
            r0.showLastFragment()
        L_0x0022:
            return r13
        L_0x0023:
            boolean r1 = org.telegram.ui.PhotoViewer.hasInstance()
            r12 = 0
            if (r1 == 0) goto L_0x0049
            org.telegram.ui.PhotoViewer r1 = org.telegram.ui.PhotoViewer.getInstance()
            boolean r1 = r1.isVisible()
            if (r1 == 0) goto L_0x0049
            if (r14 == 0) goto L_0x0042
            java.lang.String r1 = r66.getAction()
            java.lang.String r2 = "android.intent.action.MAIN"
            boolean r1 = r2.equals(r1)
            if (r1 != 0) goto L_0x0049
        L_0x0042:
            org.telegram.ui.PhotoViewer r1 = org.telegram.ui.PhotoViewer.getInstance()
            r1.closePhoto(r12, r13)
        L_0x0049:
            int r1 = r66.getFlags()
            java.lang.String r2 = r66.getAction()
            int[] r11 = new int[r13]
            int r3 = org.telegram.messenger.UserConfig.selectedAccount
            java.lang.String r4 = "currentAccount"
            int r3 = r14.getIntExtra(r4, r3)
            r11[r12] = r3
            r3 = r11[r12]
            r15.switchToAccount(r3, r13)
            if (r2 == 0) goto L_0x0070
            java.lang.String r3 = "voip"
            boolean r3 = r2.equals(r3)
            if (r3 == 0) goto L_0x0070
            r23 = 1
            goto L_0x0072
        L_0x0070:
            r23 = 0
        L_0x0072:
            if (r69 != 0) goto L_0x0095
            boolean r3 = org.telegram.messenger.AndroidUtilities.needShowPasscode(r13)
            if (r3 != 0) goto L_0x007e
            boolean r3 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r3 == 0) goto L_0x0095
        L_0x007e:
            r65.showPasscodeActivity()
            int r3 = r15.currentAccount
            org.telegram.messenger.UserConfig r3 = org.telegram.messenger.UserConfig.getInstance(r3)
            r3.saveConfig(r12)
            if (r23 != 0) goto L_0x0095
            r15.passcodeSaveIntent = r14
            r10 = r67
            r15.passcodeSaveIntentIsNew = r10
            r15.passcodeSaveIntentIsRestore = r0
            return r12
        L_0x0095:
            r10 = r67
            r9 = 0
            r15.photoPathsArray = r9
            r15.videoPath = r9
            r15.sendingText = r9
            r15.documentsPathsArray = r9
            r15.documentsOriginalPathsArray = r9
            r15.documentsMimeType = r9
            r15.documentsUrisArray = r9
            r15.exportingChatUri = r9
            r15.contactsToSend = r9
            r15.contactsToSendUri = r9
            r3 = 1048576(0x100000, float:1.469368E-39)
            r1 = r1 & r3
            java.lang.String r8 = "message_id"
            r3 = 0
            if (r1 != 0) goto L_0x17d5
            if (r14 == 0) goto L_0x17d5
            java.lang.String r1 = r66.getAction()
            if (r1 == 0) goto L_0x17d5
            if (r0 != 0) goto L_0x17d5
            java.lang.String r0 = r66.getAction()
            java.lang.String r1 = "android.intent.action.SEND"
            boolean r0 = r1.equals(r0)
            java.lang.String r1 = "\n"
            java.lang.String r7 = "hash"
            java.lang.String r5 = ""
            if (r0 == 0) goto L_0x0335
            boolean r0 = org.telegram.messenger.SharedConfig.directShare
            if (r0 == 0) goto L_0x0149
            if (r14 == 0) goto L_0x0149
            android.os.Bundle r0 = r66.getExtras()
            if (r0 == 0) goto L_0x0149
            android.os.Bundle r0 = r66.getExtras()
            java.lang.String r2 = "dialogId"
            long r18 = r0.getLong(r2, r3)
            int r0 = (r18 > r3 ? 1 : (r18 == r3 ? 0 : -1))
            if (r0 != 0) goto L_0x0137
            android.os.Bundle r0 = r66.getExtras()     // Catch:{ all -> 0x0131 }
            java.lang.String r2 = "android.intent.extra.shortcut.ID"
            java.lang.String r0 = r0.getString(r2)     // Catch:{ all -> 0x0131 }
            if (r0 == 0) goto L_0x0135
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0131 }
            java.util.List r2 = androidx.core.content.pm.ShortcutManagerCompat.getDynamicShortcuts(r2)     // Catch:{ all -> 0x0131 }
            int r6 = r2.size()     // Catch:{ all -> 0x0131 }
            r13 = 0
        L_0x0102:
            if (r13 >= r6) goto L_0x0135
            java.lang.Object r22 = r2.get(r13)     // Catch:{ all -> 0x0131 }
            androidx.core.content.pm.ShortcutInfoCompat r22 = (androidx.core.content.pm.ShortcutInfoCompat) r22     // Catch:{ all -> 0x0131 }
            java.lang.String r12 = r22.getId()     // Catch:{ all -> 0x0131 }
            boolean r12 = r0.equals(r12)     // Catch:{ all -> 0x0131 }
            if (r12 == 0) goto L_0x012d
            android.content.Intent r0 = r22.getIntent()     // Catch:{ all -> 0x0131 }
            android.os.Bundle r0 = r0.getExtras()     // Catch:{ all -> 0x0131 }
            java.lang.String r2 = "dialogId"
            long r12 = r0.getLong(r2, r3)     // Catch:{ all -> 0x0131 }
            java.lang.String r0 = r0.getString(r7, r9)     // Catch:{ all -> 0x0129 }
            r18 = r12
            goto L_0x013f
        L_0x0129:
            r0 = move-exception
            r18 = r12
            goto L_0x0132
        L_0x012d:
            int r13 = r13 + 1
            r12 = 0
            goto L_0x0102
        L_0x0131:
            r0 = move-exception
        L_0x0132:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0135:
            r0 = r9
            goto L_0x013f
        L_0x0137:
            android.os.Bundle r0 = r66.getExtras()
            java.lang.String r0 = r0.getString(r7, r9)
        L_0x013f:
            java.lang.String r2 = org.telegram.messenger.SharedConfig.directShareHash
            if (r2 == 0) goto L_0x0149
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L_0x014b
        L_0x0149:
            r18 = r3
        L_0x014b:
            java.lang.String r2 = r66.getType()
            if (r2 == 0) goto L_0x0192
            java.lang.String r0 = "text/x-vcard"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0192
            android.os.Bundle r0 = r66.getExtras()     // Catch:{ Exception -> 0x018b }
            java.lang.String r1 = "android.intent.extra.STREAM"
            java.lang.Object r0 = r0.get(r1)     // Catch:{ Exception -> 0x018b }
            android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x018b }
            if (r0 == 0) goto L_0x018f
            int r1 = r15.currentAccount     // Catch:{ Exception -> 0x018b }
            r5 = 0
            java.util.ArrayList r1 = org.telegram.messenger.AndroidUtilities.loadVCardFromStream(r0, r1, r5, r9, r9)     // Catch:{ Exception -> 0x018b }
            r15.contactsToSend = r1     // Catch:{ Exception -> 0x018b }
            int r1 = r1.size()     // Catch:{ Exception -> 0x018b }
            r5 = 5
            if (r1 <= r5) goto L_0x0187
            r15.contactsToSend = r9     // Catch:{ Exception -> 0x018b }
            java.util.ArrayList r1 = new java.util.ArrayList     // Catch:{ Exception -> 0x018b }
            r1.<init>()     // Catch:{ Exception -> 0x018b }
            r15.documentsUrisArray = r1     // Catch:{ Exception -> 0x018b }
            r1.add(r0)     // Catch:{ Exception -> 0x018b }
            r15.documentsMimeType = r2     // Catch:{ Exception -> 0x018b }
            goto L_0x0318
        L_0x0187:
            r15.contactsToSendUri = r0     // Catch:{ Exception -> 0x018b }
            goto L_0x0318
        L_0x018b:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x018f:
            r0 = 1
            goto L_0x0319
        L_0x0192:
            java.lang.String r0 = "android.intent.extra.TEXT"
            java.lang.String r0 = r14.getStringExtra(r0)
            if (r0 != 0) goto L_0x01a6
            java.lang.String r6 = "android.intent.extra.TEXT"
            java.lang.CharSequence r6 = r14.getCharSequenceExtra(r6)
            if (r6 == 0) goto L_0x01a6
            java.lang.String r0 = r6.toString()
        L_0x01a6:
            java.lang.String r6 = "android.intent.extra.SUBJECT"
            java.lang.String r6 = r14.getStringExtra(r6)
            boolean r7 = android.text.TextUtils.isEmpty(r0)
            if (r7 != 0) goto L_0x01dd
            java.lang.String r7 = "http://"
            boolean r7 = r0.startsWith(r7)
            if (r7 != 0) goto L_0x01c2
            java.lang.String r7 = "https://"
            boolean r7 = r0.startsWith(r7)
            if (r7 == 0) goto L_0x01da
        L_0x01c2:
            boolean r7 = android.text.TextUtils.isEmpty(r6)
            if (r7 != 0) goto L_0x01da
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r6)
            r7.append(r1)
            r7.append(r0)
            java.lang.String r0 = r7.toString()
        L_0x01da:
            r15.sendingText = r0
            goto L_0x01e5
        L_0x01dd:
            boolean r0 = android.text.TextUtils.isEmpty(r6)
            if (r0 != 0) goto L_0x01e5
            r15.sendingText = r6
        L_0x01e5:
            java.lang.String r0 = "android.intent.extra.STREAM"
            android.os.Parcelable r0 = r14.getParcelableExtra(r0)
            if (r0 == 0) goto L_0x0312
            boolean r1 = r0 instanceof android.net.Uri
            if (r1 != 0) goto L_0x01f9
            java.lang.String r0 = r0.toString()
            android.net.Uri r0 = android.net.Uri.parse(r0)
        L_0x01f9:
            r1 = r0
            android.net.Uri r1 = (android.net.Uri) r1
            if (r1 == 0) goto L_0x0206
            boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r1)
            if (r0 == 0) goto L_0x0206
            r6 = 1
            goto L_0x0207
        L_0x0206:
            r6 = 0
        L_0x0207:
            if (r6 != 0) goto L_0x0310
            if (r1 == 0) goto L_0x0310
            if (r2 == 0) goto L_0x0215
            java.lang.String r0 = "image/"
            boolean r0 = r2.startsWith(r0)
            if (r0 != 0) goto L_0x0225
        L_0x0215:
            java.lang.String r0 = r1.toString()
            java.lang.String r0 = r0.toLowerCase()
            java.lang.String r7 = ".jpg"
            boolean r0 = r0.endsWith(r7)
            if (r0 == 0) goto L_0x023e
        L_0x0225:
            java.util.ArrayList<org.telegram.messenger.SendMessagesHelper$SendingMediaInfo> r0 = r15.photoPathsArray
            if (r0 != 0) goto L_0x0230
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r15.photoPathsArray = r0
        L_0x0230:
            org.telegram.messenger.SendMessagesHelper$SendingMediaInfo r0 = new org.telegram.messenger.SendMessagesHelper$SendingMediaInfo
            r0.<init>()
            r0.uri = r1
            java.util.ArrayList<org.telegram.messenger.SendMessagesHelper$SendingMediaInfo> r1 = r15.photoPathsArray
            r1.add(r0)
            goto L_0x0310
        L_0x023e:
            java.lang.String r7 = r1.toString()
            int r0 = (r18 > r3 ? 1 : (r18 == r3 ? 0 : -1))
            if (r0 != 0) goto L_0x02b7
            if (r7 == 0) goto L_0x02b7
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x0260
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r12 = "export path = "
            r0.append(r12)
            r0.append(r7)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.d(r0)
        L_0x0260:
            r12 = 0
            r0 = r11[r12]
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            java.util.Set<java.lang.String> r0 = r0.exportUri
            java.lang.String r12 = org.telegram.messenger.MediaController.getFileName(r1)
            java.lang.String r12 = org.telegram.messenger.FileLoader.fixFileName(r12)
            java.util.Iterator r13 = r0.iterator()
        L_0x0275:
            boolean r0 = r13.hasNext()
            if (r0 == 0) goto L_0x02a1
            java.lang.Object r0 = r13.next()
            java.lang.String r0 = (java.lang.String) r0
            java.util.regex.Pattern r0 = java.util.regex.Pattern.compile(r0)     // Catch:{ Exception -> 0x029c }
            java.util.regex.Matcher r22 = r0.matcher(r7)     // Catch:{ Exception -> 0x029c }
            boolean r22 = r22.find()     // Catch:{ Exception -> 0x029c }
            if (r22 != 0) goto L_0x0299
            java.util.regex.Matcher r0 = r0.matcher(r12)     // Catch:{ Exception -> 0x029c }
            boolean r0 = r0.find()     // Catch:{ Exception -> 0x029c }
            if (r0 == 0) goto L_0x0275
        L_0x0299:
            r15.exportingChatUri = r1     // Catch:{ Exception -> 0x029c }
            goto L_0x02a1
        L_0x029c:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x0275
        L_0x02a1:
            android.net.Uri r0 = r15.exportingChatUri
            if (r0 != 0) goto L_0x02b7
            java.lang.String r0 = "content://com.kakao.talk"
            boolean r0 = r7.startsWith(r0)
            if (r0 == 0) goto L_0x02b7
            java.lang.String r0 = "KakaoTalkChats.txt"
            boolean r0 = r7.endsWith(r0)
            if (r0 == 0) goto L_0x02b7
            r15.exportingChatUri = r1
        L_0x02b7:
            android.net.Uri r0 = r15.exportingChatUri
            if (r0 != 0) goto L_0x0310
            java.lang.String r0 = org.telegram.messenger.AndroidUtilities.getPath(r1)
            if (r0 == 0) goto L_0x02fe
            java.lang.String r7 = "file:"
            boolean r7 = r0.startsWith(r7)
            if (r7 == 0) goto L_0x02cf
            java.lang.String r7 = "file://"
            java.lang.String r0 = r0.replace(r7, r5)
        L_0x02cf:
            if (r2 == 0) goto L_0x02dd
            java.lang.String r5 = "video/"
            boolean r2 = r2.startsWith(r5)
            if (r2 == 0) goto L_0x02dd
            r15.videoPath = r0
            goto L_0x0310
        L_0x02dd:
            java.util.ArrayList<java.lang.String> r2 = r15.documentsPathsArray
            if (r2 != 0) goto L_0x02ef
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r15.documentsPathsArray = r2
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r15.documentsOriginalPathsArray = r2
        L_0x02ef:
            java.util.ArrayList<java.lang.String> r2 = r15.documentsPathsArray
            r2.add(r0)
            java.util.ArrayList<java.lang.String> r0 = r15.documentsOriginalPathsArray
            java.lang.String r1 = r1.toString()
            r0.add(r1)
            goto L_0x0310
        L_0x02fe:
            java.util.ArrayList<android.net.Uri> r0 = r15.documentsUrisArray
            if (r0 != 0) goto L_0x0309
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r15.documentsUrisArray = r0
        L_0x0309:
            java.util.ArrayList<android.net.Uri> r0 = r15.documentsUrisArray
            r0.add(r1)
            r15.documentsMimeType = r2
        L_0x0310:
            r0 = r6
            goto L_0x0319
        L_0x0312:
            java.lang.String r0 = r15.sendingText
            if (r0 != 0) goto L_0x0318
            goto L_0x018f
        L_0x0318:
            r0 = 0
        L_0x0319:
            if (r0 == 0) goto L_0x0325
            java.lang.String r0 = "Unsupported content"
            r1 = 0
            android.widget.Toast r0 = android.widget.Toast.makeText(r15, r0, r1)
            r0.show()
        L_0x0325:
            r55 = r3
            r6 = r8
            r68 = r9
            r2 = r68
            r7 = r2
            r8 = r11
            r3 = r15
            r0 = -1
            r1 = 0
            r4 = 0
            r5 = -1
            goto L_0x17e8
        L_0x0335:
            java.lang.String r0 = r66.getAction()
            java.lang.String r6 = "android.intent.action.SEND_MULTIPLE"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x04c5
            java.lang.String r0 = "android.intent.extra.STREAM"
            java.util.ArrayList r0 = r14.getParcelableArrayListExtra(r0)     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r1 = r66.getType()     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x0380
            r2 = 0
        L_0x034e:
            int r6 = r0.size()     // Catch:{ Exception -> 0x04a9 }
            if (r2 >= r6) goto L_0x0378
            java.lang.Object r6 = r0.get(r2)     // Catch:{ Exception -> 0x04a9 }
            android.os.Parcelable r6 = (android.os.Parcelable) r6     // Catch:{ Exception -> 0x04a9 }
            boolean r7 = r6 instanceof android.net.Uri     // Catch:{ Exception -> 0x04a9 }
            if (r7 != 0) goto L_0x0366
            java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x04a9 }
            android.net.Uri r6 = android.net.Uri.parse(r6)     // Catch:{ Exception -> 0x04a9 }
        L_0x0366:
            android.net.Uri r6 = (android.net.Uri) r6     // Catch:{ Exception -> 0x04a9 }
            if (r6 == 0) goto L_0x0375
            boolean r6 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r6)     // Catch:{ Exception -> 0x04a9 }
            if (r6 == 0) goto L_0x0375
            r0.remove(r2)     // Catch:{ Exception -> 0x04a9 }
            int r2 = r2 + -1
        L_0x0375:
            r6 = 1
            int r2 = r2 + r6
            goto L_0x034e
        L_0x0378:
            boolean r2 = r0.isEmpty()     // Catch:{ Exception -> 0x04a9 }
            if (r2 == 0) goto L_0x0380
            r2 = r9
            goto L_0x0381
        L_0x0380:
            r2 = r0
        L_0x0381:
            if (r2 == 0) goto L_0x04ad
            if (r1 == 0) goto L_0x03c2
            java.lang.String r0 = "image/"
            boolean r0 = r1.startsWith(r0)     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x03c2
            r0 = 0
        L_0x038e:
            int r1 = r2.size()     // Catch:{ Exception -> 0x04a9 }
            if (r0 >= r1) goto L_0x04a7
            java.lang.Object r1 = r2.get(r0)     // Catch:{ Exception -> 0x04a9 }
            android.os.Parcelable r1 = (android.os.Parcelable) r1     // Catch:{ Exception -> 0x04a9 }
            boolean r5 = r1 instanceof android.net.Uri     // Catch:{ Exception -> 0x04a9 }
            if (r5 != 0) goto L_0x03a6
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x04a9 }
            android.net.Uri r1 = android.net.Uri.parse(r1)     // Catch:{ Exception -> 0x04a9 }
        L_0x03a6:
            android.net.Uri r1 = (android.net.Uri) r1     // Catch:{ Exception -> 0x04a9 }
            java.util.ArrayList<org.telegram.messenger.SendMessagesHelper$SendingMediaInfo> r5 = r15.photoPathsArray     // Catch:{ Exception -> 0x04a9 }
            if (r5 != 0) goto L_0x03b3
            java.util.ArrayList r5 = new java.util.ArrayList     // Catch:{ Exception -> 0x04a9 }
            r5.<init>()     // Catch:{ Exception -> 0x04a9 }
            r15.photoPathsArray = r5     // Catch:{ Exception -> 0x04a9 }
        L_0x03b3:
            org.telegram.messenger.SendMessagesHelper$SendingMediaInfo r5 = new org.telegram.messenger.SendMessagesHelper$SendingMediaInfo     // Catch:{ Exception -> 0x04a9 }
            r5.<init>()     // Catch:{ Exception -> 0x04a9 }
            r5.uri = r1     // Catch:{ Exception -> 0x04a9 }
            java.util.ArrayList<org.telegram.messenger.SendMessagesHelper$SendingMediaInfo> r1 = r15.photoPathsArray     // Catch:{ Exception -> 0x04a9 }
            r1.add(r5)     // Catch:{ Exception -> 0x04a9 }
            int r0 = r0 + 1
            goto L_0x038e
        L_0x03c2:
            r6 = 0
            r0 = r11[r6]     // Catch:{ Exception -> 0x04a9 }
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)     // Catch:{ Exception -> 0x04a9 }
            java.util.Set<java.lang.String> r6 = r0.exportUri     // Catch:{ Exception -> 0x04a9 }
            r7 = 0
        L_0x03cc:
            int r0 = r2.size()     // Catch:{ Exception -> 0x04a9 }
            if (r7 >= r0) goto L_0x04a7
            java.lang.Object r0 = r2.get(r7)     // Catch:{ Exception -> 0x04a9 }
            android.os.Parcelable r0 = (android.os.Parcelable) r0     // Catch:{ Exception -> 0x04a9 }
            boolean r12 = r0 instanceof android.net.Uri     // Catch:{ Exception -> 0x04a9 }
            if (r12 != 0) goto L_0x03e4
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x04a9 }
            android.net.Uri r0 = android.net.Uri.parse(r0)     // Catch:{ Exception -> 0x04a9 }
        L_0x03e4:
            r12 = r0
            android.net.Uri r12 = (android.net.Uri) r12     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r13 = org.telegram.messenger.AndroidUtilities.getPath(r12)     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x04a9 }
            if (r0 != 0) goto L_0x03f3
            r3 = r13
            goto L_0x03f4
        L_0x03f3:
            r3 = r0
        L_0x03f4:
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x040c
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04a9 }
            r0.<init>()     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r4 = "export path = "
            r0.append(r4)     // Catch:{ Exception -> 0x04a9 }
            r0.append(r3)     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x04a9 }
            org.telegram.messenger.FileLog.d(r0)     // Catch:{ Exception -> 0x04a9 }
        L_0x040c:
            if (r3 == 0) goto L_0x0462
            android.net.Uri r0 = r15.exportingChatUri     // Catch:{ Exception -> 0x04a9 }
            if (r0 != 0) goto L_0x0462
            java.lang.String r0 = org.telegram.messenger.MediaController.getFileName(r12)     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r4 = org.telegram.messenger.FileLoader.fixFileName(r0)     // Catch:{ Exception -> 0x04a9 }
            java.util.Iterator r22 = r6.iterator()     // Catch:{ Exception -> 0x04a9 }
        L_0x041e:
            boolean r0 = r22.hasNext()     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x044b
            java.lang.Object r0 = r22.next()     // Catch:{ Exception -> 0x04a9 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ Exception -> 0x04a9 }
            java.util.regex.Pattern r0 = java.util.regex.Pattern.compile(r0)     // Catch:{ Exception -> 0x0446 }
            java.util.regex.Matcher r25 = r0.matcher(r3)     // Catch:{ Exception -> 0x0446 }
            boolean r25 = r25.find()     // Catch:{ Exception -> 0x0446 }
            if (r25 != 0) goto L_0x0442
            java.util.regex.Matcher r0 = r0.matcher(r4)     // Catch:{ Exception -> 0x0446 }
            boolean r0 = r0.find()     // Catch:{ Exception -> 0x0446 }
            if (r0 == 0) goto L_0x041e
        L_0x0442:
            r15.exportingChatUri = r12     // Catch:{ Exception -> 0x0446 }
            r0 = 1
            goto L_0x044c
        L_0x0446:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ Exception -> 0x04a9 }
            goto L_0x041e
        L_0x044b:
            r0 = 0
        L_0x044c:
            if (r0 == 0) goto L_0x044f
            goto L_0x04a1
        L_0x044f:
            java.lang.String r0 = "content://com.kakao.talk"
            boolean r0 = r3.startsWith(r0)     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x0462
            java.lang.String r0 = "KakaoTalkChats.txt"
            boolean r0 = r3.endsWith(r0)     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x0462
            r15.exportingChatUri = r12     // Catch:{ Exception -> 0x04a9 }
            goto L_0x04a1
        L_0x0462:
            if (r13 == 0) goto L_0x048f
            java.lang.String r0 = "file:"
            boolean r0 = r13.startsWith(r0)     // Catch:{ Exception -> 0x04a9 }
            if (r0 == 0) goto L_0x0472
            java.lang.String r0 = "file://"
            java.lang.String r13 = r13.replace(r0, r5)     // Catch:{ Exception -> 0x04a9 }
        L_0x0472:
            java.util.ArrayList<java.lang.String> r0 = r15.documentsPathsArray     // Catch:{ Exception -> 0x04a9 }
            if (r0 != 0) goto L_0x0484
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ Exception -> 0x04a9 }
            r0.<init>()     // Catch:{ Exception -> 0x04a9 }
            r15.documentsPathsArray = r0     // Catch:{ Exception -> 0x04a9 }
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ Exception -> 0x04a9 }
            r0.<init>()     // Catch:{ Exception -> 0x04a9 }
            r15.documentsOriginalPathsArray = r0     // Catch:{ Exception -> 0x04a9 }
        L_0x0484:
            java.util.ArrayList<java.lang.String> r0 = r15.documentsPathsArray     // Catch:{ Exception -> 0x04a9 }
            r0.add(r13)     // Catch:{ Exception -> 0x04a9 }
            java.util.ArrayList<java.lang.String> r0 = r15.documentsOriginalPathsArray     // Catch:{ Exception -> 0x04a9 }
            r0.add(r3)     // Catch:{ Exception -> 0x04a9 }
            goto L_0x04a1
        L_0x048f:
            java.util.ArrayList<android.net.Uri> r0 = r15.documentsUrisArray     // Catch:{ Exception -> 0x04a9 }
            if (r0 != 0) goto L_0x049a
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ Exception -> 0x04a9 }
            r0.<init>()     // Catch:{ Exception -> 0x04a9 }
            r15.documentsUrisArray = r0     // Catch:{ Exception -> 0x04a9 }
        L_0x049a:
            java.util.ArrayList<android.net.Uri> r0 = r15.documentsUrisArray     // Catch:{ Exception -> 0x04a9 }
            r0.add(r12)     // Catch:{ Exception -> 0x04a9 }
            r15.documentsMimeType = r1     // Catch:{ Exception -> 0x04a9 }
        L_0x04a1:
            int r7 = r7 + 1
            r3 = 0
            goto L_0x03cc
        L_0x04a7:
            r0 = 0
            goto L_0x04ae
        L_0x04a9:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x04ad:
            r0 = 1
        L_0x04ae:
            if (r0 == 0) goto L_0x04ba
            java.lang.String r0 = "Unsupported content"
            r1 = 0
            android.widget.Toast r0 = android.widget.Toast.makeText(r15, r0, r1)
            r0.show()
        L_0x04ba:
            r6 = r8
            r8 = r11
            r1 = r14
            r3 = r15
            r4 = 0
            r24 = -1
            r55 = 0
            goto L_0x17de
        L_0x04c5:
            java.lang.String r0 = r66.getAction()
            java.lang.String r3 = "android.intent.action.VIEW"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x1681
            android.net.Uri r0 = r66.getData()
            if (r0 == 0) goto L_0x1636
            java.lang.String r2 = r0.getScheme()
            java.lang.String r3 = "actions.fulfillment.extra.ACTION_TOKEN"
            java.lang.String r4 = "phone"
            if (r2 == 0) goto L_0x1424
            r2.hashCode()
            int r6 = r2.hashCode()
            switch(r6) {
                case 3699: goto L_0x0503;
                case 3213448: goto L_0x04f8;
                case 99617003: goto L_0x04ed;
                default: goto L_0x04eb;
            }
        L_0x04eb:
            r6 = -1
            goto L_0x050d
        L_0x04ed:
            java.lang.String r6 = "https"
            boolean r6 = r2.equals(r6)
            if (r6 != 0) goto L_0x04f6
            goto L_0x04eb
        L_0x04f6:
            r6 = 2
            goto L_0x050d
        L_0x04f8:
            java.lang.String r6 = "http"
            boolean r6 = r2.equals(r6)
            if (r6 != 0) goto L_0x0501
            goto L_0x04eb
        L_0x0501:
            r6 = 1
            goto L_0x050d
        L_0x0503:
            java.lang.String r6 = "tg"
            boolean r6 = r2.equals(r6)
            if (r6 != 0) goto L_0x050c
            goto L_0x04eb
        L_0x050c:
            r6 = 0
        L_0x050d:
            java.lang.String r12 = "thread"
            r22 = -16777216(0xfffffffffvar_, float:-1.7014118E38)
            switch(r6) {
                case 0: goto L_0x098d;
                case 1: goto L_0x0516;
                case 2: goto L_0x0516;
                default: goto L_0x0514;
            }
        L_0x0514:
            goto L_0x1424
        L_0x0516:
            java.lang.String r6 = r0.getHost()
            java.lang.String r6 = r6.toLowerCase()
            java.lang.String r9 = "telegram.me"
            boolean r9 = r6.equals(r9)
            if (r9 != 0) goto L_0x053e
            java.lang.String r9 = "t.me"
            boolean r9 = r6.equals(r9)
            if (r9 != 0) goto L_0x053e
            java.lang.String r9 = "telegram.dog"
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x0537
            goto L_0x053e
        L_0x0537:
            r28 = r2
            r27 = r11
        L_0x053b:
            r9 = 0
            goto L_0x1428
        L_0x053e:
            java.lang.String r6 = r0.getPath()
            if (r6 == 0) goto L_0x0925
            int r9 = r6.length()
            r13 = 1
            if (r9 <= r13) goto L_0x0925
            java.lang.String r6 = r6.substring(r13)
            java.lang.String r9 = "bg/"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x06b4
            org.telegram.tgnet.TLRPC$TL_wallPaper r1 = new org.telegram.tgnet.TLRPC$TL_wallPaper
            r1.<init>()
            org.telegram.tgnet.TLRPC$TL_wallPaperSettings r9 = new org.telegram.tgnet.TLRPC$TL_wallPaperSettings
            r9.<init>()
            r1.settings = r9
            java.lang.String r9 = "bg/"
            java.lang.String r5 = r6.replace(r9, r5)
            r1.slug = r5
            if (r5 == 0) goto L_0x0588
            int r5 = r5.length()
            r6 = 6
            if (r5 != r6) goto L_0x0588
            org.telegram.tgnet.TLRPC$WallPaperSettings r0 = r1.settings     // Catch:{ Exception -> 0x0582 }
            java.lang.String r5 = r1.slug     // Catch:{ Exception -> 0x0582 }
            r6 = 16
            int r5 = java.lang.Integer.parseInt(r5, r6)     // Catch:{ Exception -> 0x0582 }
            r5 = r5 | r22
            r0.background_color = r5     // Catch:{ Exception -> 0x0582 }
        L_0x0582:
            r5 = 0
            r1.slug = r5
        L_0x0585:
            r9 = -1
            goto L_0x069d
        L_0x0588:
            java.lang.String r5 = r1.slug
            if (r5 == 0) goto L_0x05e7
            int r5 = r5.length()
            r6 = 13
            if (r5 != r6) goto L_0x05e7
            java.lang.String r5 = r1.slug
            r6 = 6
            char r5 = r5.charAt(r6)
            r9 = 45
            if (r5 != r9) goto L_0x05e7
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x05cb }
            java.lang.String r9 = r1.slug     // Catch:{ Exception -> 0x05cb }
            r12 = 0
            java.lang.String r9 = r9.substring(r12, r6)     // Catch:{ Exception -> 0x05cb }
            r6 = 16
            int r9 = java.lang.Integer.parseInt(r9, r6)     // Catch:{ Exception -> 0x05cb }
            r6 = r9 | r22
            r5.background_color = r6     // Catch:{ Exception -> 0x05cb }
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x05cb }
            java.lang.String r6 = r1.slug     // Catch:{ Exception -> 0x05cb }
            r9 = 7
            java.lang.String r6 = r6.substring(r9)     // Catch:{ Exception -> 0x05cb }
            r9 = 16
            int r6 = java.lang.Integer.parseInt(r6, r9)     // Catch:{ Exception -> 0x05cb }
            r6 = r6 | r22
            r5.second_background_color = r6     // Catch:{ Exception -> 0x05cb }
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x05cb }
            r6 = 45
            r5.rotation = r6     // Catch:{ Exception -> 0x05cb }
        L_0x05cb:
            java.lang.String r5 = "rotation"
            java.lang.String r0 = r0.getQueryParameter(r5)     // Catch:{ Exception -> 0x05e3 }
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ Exception -> 0x05e3 }
            if (r5 != 0) goto L_0x05e3
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x05e3 }
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)     // Catch:{ Exception -> 0x05e3 }
            int r0 = r0.intValue()     // Catch:{ Exception -> 0x05e3 }
            r5.rotation = r0     // Catch:{ Exception -> 0x05e3 }
        L_0x05e3:
            r5 = 0
            r1.slug = r5
            goto L_0x0585
        L_0x05e7:
            java.lang.String r5 = "mode"
            java.lang.String r5 = r0.getQueryParameter(r5)
            if (r5 == 0) goto L_0x0624
            java.lang.String r5 = r5.toLowerCase()
            java.lang.String r6 = " "
            java.lang.String[] r5 = r5.split(r6)
            if (r5 == 0) goto L_0x0624
            int r6 = r5.length
            if (r6 <= 0) goto L_0x0624
            r6 = 0
        L_0x05ff:
            int r9 = r5.length
            if (r6 >= r9) goto L_0x0624
            r9 = r5[r6]
            java.lang.String r12 = "blur"
            boolean r9 = r12.equals(r9)
            if (r9 == 0) goto L_0x0612
            org.telegram.tgnet.TLRPC$WallPaperSettings r9 = r1.settings
            r12 = 1
            r9.blur = r12
            goto L_0x0621
        L_0x0612:
            r12 = 1
            r9 = r5[r6]
            java.lang.String r13 = "motion"
            boolean r9 = r13.equals(r9)
            if (r9 == 0) goto L_0x0621
            org.telegram.tgnet.TLRPC$WallPaperSettings r9 = r1.settings
            r9.motion = r12
        L_0x0621:
            int r6 = r6 + 1
            goto L_0x05ff
        L_0x0624:
            java.lang.String r5 = "intensity"
            java.lang.String r5 = r0.getQueryParameter(r5)
            boolean r6 = android.text.TextUtils.isEmpty(r5)
            if (r6 != 0) goto L_0x063d
            org.telegram.tgnet.TLRPC$WallPaperSettings r6 = r1.settings
            java.lang.Integer r5 = org.telegram.messenger.Utilities.parseInt(r5)
            int r5 = r5.intValue()
            r6.intensity = r5
            goto L_0x0643
        L_0x063d:
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings
            r6 = 50
            r5.intensity = r6
        L_0x0643:
            java.lang.String r5 = "bg_color"
            java.lang.String r5 = r0.getQueryParameter(r5)     // Catch:{ Exception -> 0x067e }
            boolean r6 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x067e }
            if (r6 != 0) goto L_0x0680
            org.telegram.tgnet.TLRPC$WallPaperSettings r6 = r1.settings     // Catch:{ Exception -> 0x067e }
            r9 = 6
            r12 = 0
            java.lang.String r13 = r5.substring(r12, r9)     // Catch:{ Exception -> 0x067e }
            r12 = 16
            int r13 = java.lang.Integer.parseInt(r13, r12)     // Catch:{ Exception -> 0x067e }
            r12 = r13 | r22
            r6.background_color = r12     // Catch:{ Exception -> 0x067e }
            int r6 = r5.length()     // Catch:{ Exception -> 0x067e }
            if (r6 <= r9) goto L_0x067e
            org.telegram.tgnet.TLRPC$WallPaperSettings r6 = r1.settings     // Catch:{ Exception -> 0x067e }
            r9 = 7
            java.lang.String r5 = r5.substring(r9)     // Catch:{ Exception -> 0x067e }
            r9 = 16
            int r5 = java.lang.Integer.parseInt(r5, r9)     // Catch:{ Exception -> 0x067e }
            r5 = r5 | r22
            r6.second_background_color = r5     // Catch:{ Exception -> 0x067e }
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x067e }
            r6 = 45
            r5.rotation = r6     // Catch:{ Exception -> 0x067e }
        L_0x067e:
            r9 = -1
            goto L_0x0685
        L_0x0680:
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x067e }
            r9 = -1
            r5.background_color = r9     // Catch:{ Exception -> 0x0685 }
        L_0x0685:
            java.lang.String r5 = "rotation"
            java.lang.String r0 = r0.getQueryParameter(r5)     // Catch:{ Exception -> 0x069d }
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ Exception -> 0x069d }
            if (r5 != 0) goto L_0x069d
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x069d }
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)     // Catch:{ Exception -> 0x069d }
            int r0 = r0.intValue()     // Catch:{ Exception -> 0x069d }
            r5.rotation = r0     // Catch:{ Exception -> 0x069d }
        L_0x069d:
            r31 = r1
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r22 = 0
            r26 = 0
            r27 = 0
            r28 = 0
            r29 = 0
            r30 = 0
            goto L_0x093a
        L_0x06b4:
            r9 = -1
            java.lang.String r13 = "login/"
            boolean r13 = r6.startsWith(r13)
            if (r13 == 0) goto L_0x06f3
            java.lang.String r0 = "login/"
            java.lang.String r0 = r6.replace(r0, r5)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r0 = r0.intValue()
            if (r0 == 0) goto L_0x06dd
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r5)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            goto L_0x06de
        L_0x06dd:
            r0 = 0
        L_0x06de:
            r30 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r22 = 0
            r26 = 0
            r27 = 0
            r28 = 0
            r29 = 0
            goto L_0x0938
        L_0x06f3:
            java.lang.String r13 = "joinchat/"
            boolean r13 = r6.startsWith(r13)
            if (r13 == 0) goto L_0x0703
            java.lang.String r0 = "joinchat/"
            java.lang.String r0 = r6.replace(r0, r5)
            goto L_0x0926
        L_0x0703:
            java.lang.String r13 = "+"
            boolean r13 = r6.startsWith(r13)
            if (r13 == 0) goto L_0x0713
            java.lang.String r0 = "+"
            java.lang.String r0 = r6.replace(r0, r5)
            goto L_0x0926
        L_0x0713:
            java.lang.String r13 = "addstickers/"
            boolean r13 = r6.startsWith(r13)
            if (r13 == 0) goto L_0x0725
            java.lang.String r0 = "addstickers/"
            java.lang.String r0 = r6.replace(r0, r5)
            r1 = r0
            r0 = 0
            goto L_0x0927
        L_0x0725:
            java.lang.String r13 = "msg/"
            boolean r13 = r6.startsWith(r13)
            if (r13 != 0) goto L_0x08ba
            java.lang.String r13 = "share/"
            boolean r13 = r6.startsWith(r13)
            if (r13 == 0) goto L_0x0737
            goto L_0x08ba
        L_0x0737:
            java.lang.String r1 = "confirmphone"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0756
            java.lang.String r1 = r0.getQueryParameter(r4)
            java.lang.String r0 = r0.getQueryParameter(r7)
            r27 = r0
            r22 = r1
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r26 = 0
            goto L_0x0932
        L_0x0756:
            java.lang.String r1 = "setlanguage/"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0775
            r0 = 12
            java.lang.String r0 = r6.substring(r0)
            r28 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r22 = 0
            r26 = 0
            r27 = 0
            goto L_0x0934
        L_0x0775:
            java.lang.String r1 = "addtheme/"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0796
            r0 = 9
            java.lang.String r0 = r6.substring(r0)
            r29 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r22 = 0
            r26 = 0
            r27 = 0
            r28 = 0
            goto L_0x0936
        L_0x0796:
            java.lang.String r1 = "c/"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x07fe
            java.util.List r1 = r0.getPathSegments()
            int r5 = r1.size()
            r6 = 3
            if (r5 != r6) goto L_0x07dd
            r5 = 1
            java.lang.Object r6 = r1.get(r5)
            java.lang.CharSequence r6 = (java.lang.CharSequence) r6
            java.lang.Integer r5 = org.telegram.messenger.Utilities.parseInt(r6)
            r13 = 2
            java.lang.Object r1 = r1.get(r13)
            java.lang.CharSequence r1 = (java.lang.CharSequence) r1
            java.lang.Integer r1 = org.telegram.messenger.Utilities.parseInt(r1)
            int r6 = r1.intValue()
            if (r6 == 0) goto L_0x07cb
            int r6 = r5.intValue()
            if (r6 != 0) goto L_0x07cd
        L_0x07cb:
            r1 = 0
            r5 = 0
        L_0x07cd:
            java.lang.String r0 = r0.getQueryParameter(r12)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r6 = r0.intValue()
            if (r6 != 0) goto L_0x07e1
            r0 = 0
            goto L_0x07e1
        L_0x07dd:
            r13 = 2
            r0 = 0
            r1 = 0
            r5 = 0
        L_0x07e1:
            r34 = r0
            r32 = r1
            r33 = r5
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r22 = 0
            r26 = 0
            r27 = 0
            r28 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            goto L_0x0940
        L_0x07fe:
            r13 = 2
            int r1 = r6.length()
            r5 = 1
            if (r1 < r5) goto L_0x0925
            java.util.ArrayList r1 = new java.util.ArrayList
            java.util.List r5 = r0.getPathSegments()
            r1.<init>(r5)
            int r5 = r1.size()
            if (r5 <= 0) goto L_0x0828
            r5 = 0
            java.lang.Object r6 = r1.get(r5)
            java.lang.String r6 = (java.lang.String) r6
            java.lang.String r9 = "s"
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x0829
            r1.remove(r5)
            goto L_0x0829
        L_0x0828:
            r5 = 0
        L_0x0829:
            int r6 = r1.size()
            if (r6 <= 0) goto L_0x084f
            java.lang.Object r6 = r1.get(r5)
            r5 = r6
            java.lang.String r5 = (java.lang.String) r5
            int r6 = r1.size()
            r9 = 1
            if (r6 <= r9) goto L_0x084d
            java.lang.Object r1 = r1.get(r9)
            java.lang.CharSequence r1 = (java.lang.CharSequence) r1
            java.lang.Integer r1 = org.telegram.messenger.Utilities.parseInt(r1)
            int r6 = r1.intValue()
            if (r6 != 0) goto L_0x0851
        L_0x084d:
            r1 = 0
            goto L_0x0851
        L_0x084f:
            r1 = 0
            r5 = 0
        L_0x0851:
            java.lang.String r6 = "start"
            java.lang.String r6 = r0.getQueryParameter(r6)
            java.lang.String r9 = "startgroup"
            java.lang.String r9 = r0.getQueryParameter(r9)
            java.lang.String r13 = "game"
            java.lang.String r13 = r0.getQueryParameter(r13)
            java.lang.String r12 = r0.getQueryParameter(r12)
            java.lang.Integer r12 = org.telegram.messenger.Utilities.parseInt(r12)
            int r22 = r12.intValue()
            r68 = r1
            if (r22 != 0) goto L_0x0874
            r12 = 0
        L_0x0874:
            java.lang.String r1 = "comment"
            java.lang.String r0 = r0.getQueryParameter(r1)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r1 = r0.intValue()
            r32 = r68
            if (r1 != 0) goto L_0x089e
            r34 = r12
            r26 = r13
            r0 = 0
            r1 = 0
            r13 = 0
            r22 = 0
            r27 = 0
            r28 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r33 = 0
            r35 = 0
            goto L_0x08b5
        L_0x089e:
            r35 = r0
            r34 = r12
            r26 = r13
            r0 = 0
            r1 = 0
            r13 = 0
            r22 = 0
            r27 = 0
            r28 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r33 = 0
        L_0x08b5:
            r12 = r9
            r9 = r6
            r6 = 0
            goto L_0x0942
        L_0x08ba:
            java.lang.String r6 = "url"
            java.lang.String r6 = r0.getQueryParameter(r6)
            if (r6 != 0) goto L_0x08c3
            goto L_0x08c4
        L_0x08c3:
            r5 = r6
        L_0x08c4:
            java.lang.String r6 = "text"
            java.lang.String r6 = r0.getQueryParameter(r6)
            if (r6 == 0) goto L_0x08fa
            int r6 = r5.length()
            if (r6 <= 0) goto L_0x08e3
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r5)
            r6.append(r1)
            java.lang.String r5 = r6.toString()
            r6 = 1
            goto L_0x08e4
        L_0x08e3:
            r6 = 0
        L_0x08e4:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r5)
            java.lang.String r5 = "text"
            java.lang.String r0 = r0.getQueryParameter(r5)
            r9.append(r0)
            java.lang.String r5 = r9.toString()
            goto L_0x08fb
        L_0x08fa:
            r6 = 0
        L_0x08fb:
            int r0 = r5.length()
            r9 = 16384(0x4000, float:2.2959E-41)
            if (r0 <= r9) goto L_0x090b
            r0 = 16384(0x4000, float:2.2959E-41)
            r9 = 0
            java.lang.String r0 = r5.substring(r9, r0)
            goto L_0x090d
        L_0x090b:
            r9 = 0
            r0 = r5
        L_0x090d:
            boolean r5 = r0.endsWith(r1)
            if (r5 == 0) goto L_0x091e
            int r5 = r0.length()
            r12 = 1
            int r5 = r5 - r12
            java.lang.String r0 = r0.substring(r9, r5)
            goto L_0x090d
        L_0x091e:
            r13 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r9 = 0
            r12 = 0
            goto L_0x092c
        L_0x0925:
            r0 = 0
        L_0x0926:
            r1 = 0
        L_0x0927:
            r5 = 0
            r6 = 0
            r9 = 0
            r12 = 0
            r13 = 0
        L_0x092c:
            r22 = 0
            r26 = 0
            r27 = 0
        L_0x0932:
            r28 = 0
        L_0x0934:
            r29 = 0
        L_0x0936:
            r30 = 0
        L_0x0938:
            r31 = 0
        L_0x093a:
            r32 = 0
            r33 = 0
            r34 = 0
        L_0x0940:
            r35 = 0
        L_0x0942:
            r25 = r5
            r10 = r9
            r5 = r22
            r47 = r26
            r49 = r28
            r54 = r29
            r51 = r30
            r53 = r31
            r43 = r32
            r44 = r33
            r45 = r34
            r46 = r35
            r9 = 0
            r20 = 6
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r48 = 0
            r50 = 0
            r52 = 0
            r28 = r2
            r64 = r6
            r6 = r1
            r1 = r27
            r27 = r11
            r11 = r64
            goto L_0x146a
        L_0x098d:
            java.lang.String r6 = r0.toString()
            java.lang.String r9 = "tg:resolve"
            boolean r9 = r6.startsWith(r9)
            java.lang.String r13 = "bot_id"
            java.lang.String r10 = "payload"
            r27 = r11
            java.lang.String r11 = "scope"
            r28 = r2
            java.lang.String r2 = "tg://telegram.org"
            if (r9 != 0) goto L_0x12f2
            java.lang.String r9 = "tg://resolve"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x09af
            goto L_0x12f2
        L_0x09af:
            java.lang.String r9 = "tg:privatepost"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x1255
            java.lang.String r9 = "tg://privatepost"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x09c1
            goto L_0x1255
        L_0x09c1:
            java.lang.String r9 = "tg:bg"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x10d8
            java.lang.String r9 = "tg://bg"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x09d3
            goto L_0x10d8
        L_0x09d3:
            java.lang.String r9 = "tg:join"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x107e
            java.lang.String r9 = "tg://join"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x09e5
            goto L_0x107e
        L_0x09e5:
            java.lang.String r9 = "tg:addstickers"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x1063
            java.lang.String r9 = "tg://addstickers"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x09f7
            goto L_0x1063
        L_0x09f7:
            java.lang.String r9 = "tg:msg"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x0fd9
            java.lang.String r9 = "tg://msg"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x0fd9
            java.lang.String r9 = "tg://share"
            boolean r9 = r6.startsWith(r9)
            if (r9 != 0) goto L_0x0fd9
            java.lang.String r9 = "tg:share"
            boolean r9 = r6.startsWith(r9)
            if (r9 == 0) goto L_0x0a19
            goto L_0x0fd9
        L_0x0a19:
            java.lang.String r1 = "tg:confirmphone"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0f7c
            java.lang.String r1 = "tg://confirmphone"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a2b
            goto L_0x0f7c
        L_0x0a2b:
            java.lang.String r1 = "tg:login"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0var_
            java.lang.String r1 = "tg://login"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a3d
            goto L_0x0var_
        L_0x0a3d:
            java.lang.String r1 = "tg:openmessage"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0eb4
            java.lang.String r1 = "tg://openmessage"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a4f
            goto L_0x0eb4
        L_0x0a4f:
            java.lang.String r1 = "tg:passport"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0e11
            java.lang.String r1 = "tg://passport"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0e11
            java.lang.String r1 = "tg:secureid"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a69
            goto L_0x0e11
        L_0x0a69:
            java.lang.String r1 = "tg:setlanguage"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0dc0
            java.lang.String r1 = "tg://setlanguage"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a7b
            goto L_0x0dc0
        L_0x0a7b:
            java.lang.String r1 = "tg:addtheme"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0d65
            java.lang.String r1 = "tg://addtheme"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a8d
            goto L_0x0d65
        L_0x0a8d:
            java.lang.String r1 = "tg:settings"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0cd0
            java.lang.String r1 = "tg://settings"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0a9f
            goto L_0x0cd0
        L_0x0a9f:
            java.lang.String r1 = "tg:search"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0c8d
            java.lang.String r1 = "tg://search"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0ab1
            goto L_0x0c8d
        L_0x0ab1:
            java.lang.String r1 = "tg:calllog"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0CLASSNAME
            java.lang.String r1 = "tg://calllog"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0ac3
            goto L_0x0CLASSNAME
        L_0x0ac3:
            java.lang.String r1 = "tg:call"
            boolean r1 = r6.startsWith(r1)
            if (r1 != 0) goto L_0x0bba
            java.lang.String r1 = "tg://call"
            boolean r1 = r6.startsWith(r1)
            if (r1 == 0) goto L_0x0ad5
            goto L_0x0bba
        L_0x0ad5:
            java.lang.String r0 = "tg:scanqr"
            boolean r0 = r6.startsWith(r0)
            if (r0 != 0) goto L_0x0b95
            java.lang.String r0 = "tg://scanqr"
            boolean r0 = r6.startsWith(r0)
            if (r0 == 0) goto L_0x0ae7
            goto L_0x0b95
        L_0x0ae7:
            java.lang.String r0 = "tg:addcontact"
            boolean r0 = r6.startsWith(r0)
            if (r0 != 0) goto L_0x0b4e
            java.lang.String r0 = "tg://addcontact"
            boolean r0 = r6.startsWith(r0)
            if (r0 == 0) goto L_0x0af8
            goto L_0x0b4e
        L_0x0af8:
            java.lang.String r0 = "tg://"
            java.lang.String r0 = r6.replace(r0, r5)
            java.lang.String r1 = "tg:"
            java.lang.String r0 = r0.replace(r1, r5)
            r1 = 63
            int r1 = r0.indexOf(r1)
            if (r1 < 0) goto L_0x0b11
            r2 = 0
            java.lang.String r0 = r0.substring(r2, r1)
        L_0x0b11:
            r50 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
            r49 = 0
            goto L_0x10ce
        L_0x0b4e:
            java.lang.String r0 = "tg:addcontact"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://addcontact"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "name"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.String r0 = r0.getQueryParameter(r4)
            r42 = r0
            r41 = r1
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 1
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            goto L_0x10be
        L_0x0b95:
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 1
            goto L_0x10b6
        L_0x0bba:
            int r1 = r15.currentAccount
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            boolean r1 = r1.isClientActivated()
            if (r1 == 0) goto L_0x053b
            int r1 = r15.currentAccount
            org.telegram.messenger.ContactsController r1 = org.telegram.messenger.ContactsController.getInstance(r1)
            boolean r1 = r1.contactsLoaded
            if (r1 != 0) goto L_0x0bfa
            java.lang.String r1 = "extra_force_call"
            boolean r1 = r14.hasExtra(r1)
            if (r1 == 0) goto L_0x0bd9
            goto L_0x0bfa
        L_0x0bd9:
            android.content.Intent r0 = new android.content.Intent
            r0.<init>(r14)
            r0.removeExtra(r3)
            java.lang.String r1 = "extra_force_call"
            r2 = 1
            r0.putExtra(r1, r2)
            org.telegram.ui.-$$Lambda$LaunchActivity$X0aF1Xm_mIISEfWg-pwwTqXnE3w r1 = new org.telegram.ui.-$$Lambda$LaunchActivity$X0aF1Xm_mIISEfWg-pwwTqXnE3w
            r1.<init>(r0)
            r5 = 1000(0x3e8, double:4.94E-321)
            org.telegram.messenger.ContactsLoadingObserver.observe(r1, r5)
            r0 = 0
            r1 = 0
            r2 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            goto L_0x0CLASSNAME
        L_0x0bfa:
            java.lang.String r1 = "format"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.String r2 = "name"
            java.lang.String r2 = r0.getQueryParameter(r2)
            java.lang.String r0 = r0.getQueryParameter(r4)
            r6 = 0
            java.util.List r9 = r15.findContacts(r2, r0, r6)
            boolean r10 = r9.isEmpty()
            if (r10 == 0) goto L_0x0c1f
            if (r0 == 0) goto L_0x0c1f
            r11 = r0
            r10 = r2
            r0 = 1
            r1 = 0
            r2 = 0
            r5 = 0
            r9 = 0
            goto L_0x0CLASSNAME
        L_0x0c1f:
            int r0 = r9.size()
            r10 = 1
            if (r0 != r10) goto L_0x0c2f
            java.lang.Object r0 = r9.get(r6)
            org.telegram.tgnet.TLRPC$TL_contact r0 = (org.telegram.tgnet.TLRPC$TL_contact) r0
            int r0 = r0.user_id
            goto L_0x0CLASSNAME
        L_0x0c2f:
            r0 = 0
        L_0x0CLASSNAME:
            if (r0 != 0) goto L_0x0CLASSNAME
            if (r2 == 0) goto L_0x0CLASSNAME
            r5 = r2
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            r5 = 0
        L_0x0CLASSNAME:
            java.lang.String r2 = "video"
            boolean r1 = r2.equalsIgnoreCase(r1)
            r2 = r1 ^ 1
            r9 = r5
            r6 = 1
            r10 = 0
            r11 = 0
            r5 = r1
            r1 = r0
            r0 = 0
        L_0x0CLASSNAME:
            r37 = r0
            r26 = r1
            r33 = r2
            r34 = r5
            r35 = r6
            r40 = r9
            r41 = r10
            r42 = r11
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r36 = 0
            r38 = 0
            r39 = 0
            goto L_0x10be
        L_0x0CLASSNAME:
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 1
            goto L_0x10aa
        L_0x0c8d:
            java.lang.String r0 = "tg:search"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://search"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "query"
            java.lang.String r0 = r0.getQueryParameter(r1)
            if (r0 == 0) goto L_0x0ca9
            java.lang.String r5 = r0.trim()
        L_0x0ca9:
            r39 = r5
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            goto L_0x10b8
        L_0x0cd0:
            java.lang.String r0 = "themes"
            boolean r0 = r6.contains(r0)
            if (r0 == 0) goto L_0x0cef
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 2
            goto L_0x10a8
        L_0x0cef:
            java.lang.String r0 = "devices"
            boolean r0 = r6.contains(r0)
            if (r0 == 0) goto L_0x0d0e
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 3
            goto L_0x10a8
        L_0x0d0e:
            java.lang.String r0 = "folders"
            boolean r0 = r6.contains(r0)
            if (r0 == 0) goto L_0x0d2e
            r0 = 4
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 4
            goto L_0x10a8
        L_0x0d2e:
            java.lang.String r0 = "change_number"
            boolean r0 = r6.contains(r0)
            if (r0 == 0) goto L_0x0d4e
            r0 = 5
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 5
            goto L_0x10a8
        L_0x0d4e:
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 1
            goto L_0x10a8
        L_0x0d65:
            java.lang.String r0 = "tg:addtheme"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://addtheme"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "slug"
            java.lang.String r0 = r0.getQueryParameter(r1)
            r54 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
            r49 = 0
            r50 = 0
            r51 = 0
            r52 = 0
            r53 = 0
            goto L_0x146a
        L_0x0dc0:
            java.lang.String r0 = "tg:setlanguage"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://setlanguage"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "lang"
            java.lang.String r0 = r0.getQueryParameter(r1)
            r49 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
            goto L_0x10cc
        L_0x0e11:
            java.lang.String r0 = "tg:passport"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://passport"
            java.lang.String r0 = r0.replace(r1, r2)
            java.lang.String r1 = "tg:secureid"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            java.lang.String r2 = r0.getQueryParameter(r11)
            boolean r5 = android.text.TextUtils.isEmpty(r2)
            if (r5 != 0) goto L_0x0e54
            java.lang.String r5 = "{"
            boolean r5 = r2.startsWith(r5)
            if (r5 == 0) goto L_0x0e54
            java.lang.String r5 = "}"
            boolean r5 = r2.endsWith(r5)
            if (r5 == 0) goto L_0x0e54
            java.lang.String r5 = "nonce"
            java.lang.String r5 = r0.getQueryParameter(r5)
            java.lang.String r6 = "nonce"
            r1.put(r6, r5)
            goto L_0x0e5b
        L_0x0e54:
            java.lang.String r5 = r0.getQueryParameter(r10)
            r1.put(r10, r5)
        L_0x0e5b:
            java.lang.String r5 = r0.getQueryParameter(r13)
            r1.put(r13, r5)
            r1.put(r11, r2)
            java.lang.String r2 = "public_key"
            java.lang.String r2 = r0.getQueryParameter(r2)
            java.lang.String r5 = "public_key"
            r1.put(r5, r2)
            java.lang.String r2 = "callback_url"
            java.lang.String r0 = r0.getQueryParameter(r2)
            java.lang.String r2 = "callback_url"
            r1.put(r2, r0)
            r48 = r1
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            goto L_0x10ca
        L_0x0eb4:
            java.lang.String r0 = "tg:openmessage"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://openmessage"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "user_id"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.String r2 = "chat_id"
            java.lang.String r2 = r0.getQueryParameter(r2)
            java.lang.String r0 = r0.getQueryParameter(r8)
            if (r1 == 0) goto L_0x0edc
            int r1 = java.lang.Integer.parseInt(r1)     // Catch:{ NumberFormatException -> 0x0ee5 }
            goto L_0x0ee6
        L_0x0edc:
            if (r2 == 0) goto L_0x0ee5
            int r1 = java.lang.Integer.parseInt(r2)     // Catch:{ NumberFormatException -> 0x0ee5 }
            r2 = r1
            r1 = 0
            goto L_0x0ee7
        L_0x0ee5:
            r1 = 0
        L_0x0ee6:
            r2 = 0
        L_0x0ee7:
            if (r0 == 0) goto L_0x0eee
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x0eee }
            goto L_0x0eef
        L_0x0eee:
            r0 = 0
        L_0x0eef:
            r30 = r0
            r26 = r1
            r29 = r2
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            goto L_0x10a6
        L_0x0var_:
            java.lang.String r0 = "tg:login"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://login"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "token"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.String r2 = "code"
            java.lang.String r0 = r0.getQueryParameter(r2)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r0 = r0.intValue()
            if (r0 == 0) goto L_0x0f3a
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r5)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            goto L_0x0f3b
        L_0x0f3a:
            r0 = 0
        L_0x0f3b:
            r51 = r0
            r52 = r1
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
            r49 = 0
            r50 = 0
            goto L_0x10d2
        L_0x0f7c:
            java.lang.String r0 = "tg:confirmphone"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://confirmphone"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = r0.getQueryParameter(r4)
            java.lang.String r0 = r0.getQueryParameter(r7)
            r5 = r1
            r6 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r39 = 0
            r40 = 0
            r41 = 0
            r42 = 0
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
            r49 = 0
            r50 = 0
            r51 = 0
            r52 = 0
            r53 = 0
            r54 = 0
            r1 = r0
            r0 = 0
            goto L_0x146a
        L_0x0fd9:
            java.lang.String r0 = "tg:msg"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r6 = "tg://msg"
            java.lang.String r0 = r0.replace(r6, r2)
            java.lang.String r6 = "tg://share"
            java.lang.String r0 = r0.replace(r6, r2)
            java.lang.String r6 = "tg:share"
            java.lang.String r0 = r0.replace(r6, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r2 = "url"
            java.lang.String r2 = r0.getQueryParameter(r2)
            if (r2 != 0) goto L_0x0ffe
            goto L_0x0fff
        L_0x0ffe:
            r5 = r2
        L_0x0fff:
            java.lang.String r2 = "text"
            java.lang.String r2 = r0.getQueryParameter(r2)
            if (r2 == 0) goto L_0x1035
            int r2 = r5.length()
            if (r2 <= 0) goto L_0x101e
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r5)
            r2.append(r1)
            java.lang.String r5 = r2.toString()
            r2 = 1
            goto L_0x101f
        L_0x101e:
            r2 = 0
        L_0x101f:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r5)
            java.lang.String r5 = "text"
            java.lang.String r0 = r0.getQueryParameter(r5)
            r6.append(r0)
            java.lang.String r5 = r6.toString()
            goto L_0x1036
        L_0x1035:
            r2 = 0
        L_0x1036:
            int r0 = r5.length()
            r6 = 16384(0x4000, float:2.2959E-41)
            if (r0 <= r6) goto L_0x1046
            r0 = 16384(0x4000, float:2.2959E-41)
            r6 = 0
            java.lang.String r0 = r5.substring(r6, r0)
            goto L_0x1048
        L_0x1046:
            r6 = 0
            r0 = r5
        L_0x1048:
            boolean r5 = r0.endsWith(r1)
            if (r5 == 0) goto L_0x1059
            int r5 = r0.length()
            r9 = 1
            int r5 = r5 - r9
            java.lang.String r0 = r0.substring(r6, r5)
            goto L_0x1048
        L_0x1059:
            r13 = r0
            r11 = r2
            r0 = 0
            r1 = 0
            r5 = 0
            r6 = 0
            r9 = 0
            r10 = 0
            r12 = 0
            goto L_0x109c
        L_0x1063:
            java.lang.String r0 = "tg:addstickers"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://addstickers"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "set"
            java.lang.String r0 = r0.getQueryParameter(r1)
            r6 = r0
            r0 = 0
            r1 = 0
            r5 = 0
            goto L_0x1097
        L_0x107e:
            java.lang.String r0 = "tg:join"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://join"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "invite"
            java.lang.String r0 = r0.getQueryParameter(r1)
            r1 = 0
            r5 = 0
            r6 = 0
        L_0x1097:
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
        L_0x109c:
            r20 = 6
            r25 = 0
            r26 = 0
            r29 = 0
            r30 = 0
        L_0x10a6:
            r31 = 0
        L_0x10a8:
            r32 = 0
        L_0x10aa:
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
        L_0x10b6:
            r39 = 0
        L_0x10b8:
            r40 = 0
            r41 = 0
            r42 = 0
        L_0x10be:
            r43 = 0
            r44 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r48 = 0
        L_0x10ca:
            r49 = 0
        L_0x10cc:
            r50 = 0
        L_0x10ce:
            r51 = 0
            r52 = 0
        L_0x10d2:
            r53 = 0
            r54 = 0
            goto L_0x146a
        L_0x10d8:
            java.lang.String r0 = "tg:bg"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://bg"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            org.telegram.tgnet.TLRPC$TL_wallPaper r1 = new org.telegram.tgnet.TLRPC$TL_wallPaper
            r1.<init>()
            org.telegram.tgnet.TLRPC$TL_wallPaperSettings r2 = new org.telegram.tgnet.TLRPC$TL_wallPaperSettings
            r2.<init>()
            r1.settings = r2
            java.lang.String r2 = "slug"
            java.lang.String r2 = r0.getQueryParameter(r2)
            r1.slug = r2
            if (r2 != 0) goto L_0x1106
            java.lang.String r2 = "color"
            java.lang.String r2 = r0.getQueryParameter(r2)
            r1.slug = r2
        L_0x1106:
            java.lang.String r2 = r1.slug
            if (r2 == 0) goto L_0x1126
            int r2 = r2.length()
            r5 = 6
            if (r2 != r5) goto L_0x1126
            org.telegram.tgnet.TLRPC$WallPaperSettings r0 = r1.settings     // Catch:{ Exception -> 0x111f }
            java.lang.String r2 = r1.slug     // Catch:{ Exception -> 0x111f }
            r5 = 16
            int r2 = java.lang.Integer.parseInt(r2, r5)     // Catch:{ Exception -> 0x111f }
            r2 = r2 | r22
            r0.background_color = r2     // Catch:{ Exception -> 0x111f }
        L_0x111f:
            r2 = 0
            r1.slug = r2
            r9 = r2
        L_0x1123:
            r10 = 6
            goto L_0x122a
        L_0x1126:
            java.lang.String r2 = r1.slug
            if (r2 == 0) goto L_0x1185
            int r2 = r2.length()
            r5 = 13
            if (r2 != r5) goto L_0x1185
            java.lang.String r2 = r1.slug
            r5 = 6
            char r2 = r2.charAt(r5)
            r6 = 45
            if (r2 != r6) goto L_0x1185
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x1169 }
            java.lang.String r6 = r1.slug     // Catch:{ Exception -> 0x1169 }
            r9 = 0
            java.lang.String r6 = r6.substring(r9, r5)     // Catch:{ Exception -> 0x1169 }
            r5 = 16
            int r6 = java.lang.Integer.parseInt(r6, r5)     // Catch:{ Exception -> 0x1169 }
            r5 = r6 | r22
            r2.background_color = r5     // Catch:{ Exception -> 0x1169 }
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x1169 }
            java.lang.String r5 = r1.slug     // Catch:{ Exception -> 0x1169 }
            r6 = 7
            java.lang.String r5 = r5.substring(r6)     // Catch:{ Exception -> 0x1169 }
            r6 = 16
            int r5 = java.lang.Integer.parseInt(r5, r6)     // Catch:{ Exception -> 0x1169 }
            r5 = r5 | r22
            r2.second_background_color = r5     // Catch:{ Exception -> 0x1169 }
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x1169 }
            r5 = 45
            r2.rotation = r5     // Catch:{ Exception -> 0x1169 }
        L_0x1169:
            java.lang.String r2 = "rotation"
            java.lang.String r0 = r0.getQueryParameter(r2)     // Catch:{ Exception -> 0x1181 }
            boolean r2 = android.text.TextUtils.isEmpty(r0)     // Catch:{ Exception -> 0x1181 }
            if (r2 != 0) goto L_0x1181
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x1181 }
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)     // Catch:{ Exception -> 0x1181 }
            int r0 = r0.intValue()     // Catch:{ Exception -> 0x1181 }
            r2.rotation = r0     // Catch:{ Exception -> 0x1181 }
        L_0x1181:
            r9 = 0
            r1.slug = r9
            goto L_0x1123
        L_0x1185:
            r9 = 0
            java.lang.String r2 = "mode"
            java.lang.String r2 = r0.getQueryParameter(r2)
            if (r2 == 0) goto L_0x11c3
            java.lang.String r2 = r2.toLowerCase()
            java.lang.String r5 = " "
            java.lang.String[] r2 = r2.split(r5)
            if (r2 == 0) goto L_0x11c3
            int r5 = r2.length
            if (r5 <= 0) goto L_0x11c3
            r5 = 0
        L_0x119e:
            int r6 = r2.length
            if (r5 >= r6) goto L_0x11c3
            r6 = r2[r5]
            java.lang.String r10 = "blur"
            boolean r6 = r10.equals(r6)
            if (r6 == 0) goto L_0x11b1
            org.telegram.tgnet.TLRPC$WallPaperSettings r6 = r1.settings
            r10 = 1
            r6.blur = r10
            goto L_0x11c0
        L_0x11b1:
            r10 = 1
            r6 = r2[r5]
            java.lang.String r11 = "motion"
            boolean r6 = r11.equals(r6)
            if (r6 == 0) goto L_0x11c0
            org.telegram.tgnet.TLRPC$WallPaperSettings r6 = r1.settings
            r6.motion = r10
        L_0x11c0:
            int r5 = r5 + 1
            goto L_0x119e
        L_0x11c3:
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings
            java.lang.String r5 = "intensity"
            java.lang.String r5 = r0.getQueryParameter(r5)
            java.lang.Integer r5 = org.telegram.messenger.Utilities.parseInt(r5)
            int r5 = r5.intValue()
            r2.intensity = r5
            java.lang.String r2 = "bg_color"
            java.lang.String r2 = r0.getQueryParameter(r2)     // Catch:{ Exception -> 0x1211 }
            boolean r5 = android.text.TextUtils.isEmpty(r2)     // Catch:{ Exception -> 0x1211 }
            if (r5 != 0) goto L_0x1211
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x1211 }
            r6 = 0
            r10 = 6
            java.lang.String r11 = r2.substring(r6, r10)     // Catch:{ Exception -> 0x1212 }
            r6 = 16
            int r11 = java.lang.Integer.parseInt(r11, r6)     // Catch:{ Exception -> 0x1212 }
            r6 = r11 | r22
            r5.background_color = r6     // Catch:{ Exception -> 0x1212 }
            int r5 = r2.length()     // Catch:{ Exception -> 0x1212 }
            if (r5 <= r10) goto L_0x1212
            org.telegram.tgnet.TLRPC$WallPaperSettings r5 = r1.settings     // Catch:{ Exception -> 0x1212 }
            r6 = 7
            java.lang.String r2 = r2.substring(r6)     // Catch:{ Exception -> 0x1212 }
            r6 = 16
            int r2 = java.lang.Integer.parseInt(r2, r6)     // Catch:{ Exception -> 0x1212 }
            r2 = r2 | r22
            r5.second_background_color = r2     // Catch:{ Exception -> 0x1212 }
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x1212 }
            r5 = 45
            r2.rotation = r5     // Catch:{ Exception -> 0x1212 }
            goto L_0x1212
        L_0x1211:
            r10 = 6
        L_0x1212:
            java.lang.String r2 = "rotation"
            java.lang.String r0 = r0.getQueryParameter(r2)     // Catch:{ Exception -> 0x122a }
            boolean r2 = android.text.TextUtils.isEmpty(r0)     // Catch:{ Exception -> 0x122a }
            if (r2 != 0) goto L_0x122a
            org.telegram.tgnet.TLRPC$WallPaperSettings r2 = r1.settings     // Catch:{ Exception -> 0x122a }
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)     // Catch:{ Exception -> 0x122a }
            int r0 = r0.intValue()     // Catch:{ Exception -> 0x122a }
            r2.rotation = r0     // Catch:{ Exception -> 0x122a }
        L_0x122a:
            r53 = r1
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r10 = r6
            r12 = r10
            r13 = r12
            r25 = r13
            r39 = r25
            r40 = r39
            r41 = r40
            r42 = r41
            r43 = r42
            r44 = r43
            r45 = r44
            r46 = r45
            r47 = r46
            r48 = r47
            r49 = r48
            r50 = r49
            r51 = r50
            r52 = r51
            r54 = r52
            goto L_0x12ed
        L_0x1255:
            r9 = 0
            r10 = 6
            java.lang.String r0 = "tg:privatepost"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://privatepost"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "post"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.Integer r5 = org.telegram.messenger.Utilities.parseInt(r1)
            java.lang.String r1 = "channel"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.Integer r1 = org.telegram.messenger.Utilities.parseInt(r1)
            int r2 = r5.intValue()
            if (r2 == 0) goto L_0x1287
            int r2 = r1.intValue()
            if (r2 != 0) goto L_0x1289
        L_0x1287:
            r1 = r9
            r5 = r1
        L_0x1289:
            java.lang.String r2 = r0.getQueryParameter(r12)
            java.lang.Integer r2 = org.telegram.messenger.Utilities.parseInt(r2)
            int r6 = r2.intValue()
            if (r6 != 0) goto L_0x1298
            r2 = r9
        L_0x1298:
            java.lang.String r6 = "comment"
            java.lang.String r0 = r0.getQueryParameter(r6)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r6 = r0.intValue()
            if (r6 != 0) goto L_0x12c4
            r44 = r1
            r45 = r2
            r43 = r5
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r10 = r6
            r12 = r10
            r13 = r12
            r25 = r13
            r39 = r25
            r40 = r39
            r41 = r40
            r42 = r41
            r46 = r42
            r47 = r46
            goto L_0x12df
        L_0x12c4:
            r46 = r0
            r44 = r1
            r45 = r2
            r43 = r5
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r10 = r6
            r12 = r10
            r13 = r12
            r25 = r13
            r39 = r25
            r40 = r39
            r41 = r40
            r42 = r41
            r47 = r42
        L_0x12df:
            r48 = r47
            r49 = r48
            r50 = r49
            r51 = r50
            r52 = r51
            r53 = r52
            r54 = r53
        L_0x12ed:
            r11 = 0
            r20 = 6
            goto L_0x1454
        L_0x12f2:
            r9 = 0
            r20 = 6
            java.lang.String r0 = "tg:resolve"
            java.lang.String r0 = r6.replace(r0, r2)
            java.lang.String r1 = "tg://resolve"
            java.lang.String r0 = r0.replace(r1, r2)
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = "domain"
            java.lang.String r1 = r0.getQueryParameter(r1)
            java.lang.String r2 = "telegrampassport"
            boolean r2 = r2.equals(r1)
            if (r2 == 0) goto L_0x1388
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            java.lang.String r2 = r0.getQueryParameter(r11)
            boolean r5 = android.text.TextUtils.isEmpty(r2)
            if (r5 != 0) goto L_0x1340
            java.lang.String r5 = "{"
            boolean r5 = r2.startsWith(r5)
            if (r5 == 0) goto L_0x1340
            java.lang.String r5 = "}"
            boolean r5 = r2.endsWith(r5)
            if (r5 == 0) goto L_0x1340
            java.lang.String r5 = "nonce"
            java.lang.String r5 = r0.getQueryParameter(r5)
            java.lang.String r6 = "nonce"
            r1.put(r6, r5)
            goto L_0x1347
        L_0x1340:
            java.lang.String r5 = r0.getQueryParameter(r10)
            r1.put(r10, r5)
        L_0x1347:
            java.lang.String r5 = r0.getQueryParameter(r13)
            r1.put(r13, r5)
            r1.put(r11, r2)
            java.lang.String r2 = "public_key"
            java.lang.String r2 = r0.getQueryParameter(r2)
            java.lang.String r5 = "public_key"
            r1.put(r5, r2)
            java.lang.String r2 = "callback_url"
            java.lang.String r0 = r0.getQueryParameter(r2)
            java.lang.String r2 = "callback_url"
            r1.put(r2, r0)
            r48 = r1
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r10 = r6
            r12 = r10
            r13 = r12
            r25 = r13
            r39 = r25
            r40 = r39
            r41 = r40
            r42 = r41
            r43 = r42
            r44 = r43
            r45 = r44
            r46 = r45
            r47 = r46
            r49 = r47
            goto L_0x1449
        L_0x1388:
            java.lang.String r2 = "start"
            java.lang.String r2 = r0.getQueryParameter(r2)
            java.lang.String r5 = "startgroup"
            java.lang.String r5 = r0.getQueryParameter(r5)
            java.lang.String r6 = "game"
            java.lang.String r6 = r0.getQueryParameter(r6)
            java.lang.String r10 = "post"
            java.lang.String r10 = r0.getQueryParameter(r10)
            java.lang.Integer r10 = org.telegram.messenger.Utilities.parseInt(r10)
            int r11 = r10.intValue()
            if (r11 != 0) goto L_0x13ab
            r10 = r9
        L_0x13ab:
            java.lang.String r11 = r0.getQueryParameter(r12)
            java.lang.Integer r11 = org.telegram.messenger.Utilities.parseInt(r11)
            int r12 = r11.intValue()
            if (r12 != 0) goto L_0x13ba
            r11 = r9
        L_0x13ba:
            java.lang.String r12 = "comment"
            java.lang.String r0 = r0.getQueryParameter(r12)
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)
            int r12 = r0.intValue()
            if (r12 != 0) goto L_0x13e3
            r25 = r1
            r12 = r5
            r47 = r6
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r13 = r6
            r39 = r13
            r40 = r39
            r41 = r40
            r42 = r41
            r44 = r42
            r46 = r44
            r48 = r46
            goto L_0x13fb
        L_0x13e3:
            r46 = r0
            r25 = r1
            r12 = r5
            r47 = r6
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r13 = r6
            r39 = r13
            r40 = r39
            r41 = r40
            r42 = r41
            r44 = r42
            r48 = r44
        L_0x13fb:
            r49 = r48
            r50 = r49
            r51 = r50
            r52 = r51
            r53 = r52
            r54 = r53
            r43 = r10
            r45 = r11
            r11 = 0
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r10 = r2
            goto L_0x146a
        L_0x1424:
            r28 = r2
            r27 = r11
        L_0x1428:
            r20 = 6
            r0 = r9
            r1 = r0
            r5 = r1
            r6 = r5
            r10 = r6
            r12 = r10
            r13 = r12
            r25 = r13
            r39 = r25
            r40 = r39
            r41 = r40
            r42 = r41
            r43 = r42
            r44 = r43
            r45 = r44
            r46 = r45
            r47 = r46
            r48 = r47
            r49 = r48
        L_0x1449:
            r50 = r49
            r51 = r50
            r52 = r51
            r53 = r52
            r54 = r53
            r11 = 0
        L_0x1454:
            r26 = 0
            r29 = 0
            r30 = 0
            r31 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
        L_0x146a:
            boolean r2 = r14.hasExtra(r3)
            if (r2 == 0) goto L_0x14b2
            int r2 = r15.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            boolean r2 = r2.isClientActivated()
            if (r2 == 0) goto L_0x148a
            java.lang.String r2 = "tg"
            r9 = r28
            boolean r2 = r2.equals(r9)
            if (r2 == 0) goto L_0x148a
            if (r50 != 0) goto L_0x148a
            r2 = 1
            goto L_0x148b
        L_0x148a:
            r2 = 0
        L_0x148b:
            com.google.firebase.appindexing.builders.AssistActionBuilder r9 = new com.google.firebase.appindexing.builders.AssistActionBuilder
            r9.<init>()
            r28 = r8
            java.lang.String r8 = r14.getStringExtra(r3)
            r9.setActionToken(r8)
            if (r2 == 0) goto L_0x149e
            java.lang.String r2 = "http://schema.org/CompletedActionStatus"
            goto L_0x14a0
        L_0x149e:
            java.lang.String r2 = "http://schema.org/FailedActionStatus"
        L_0x14a0:
            r9.setActionStatus(r2)
            com.google.firebase.appindexing.Action r2 = r9.build()
            com.google.firebase.appindexing.FirebaseUserActions r8 = com.google.firebase.appindexing.FirebaseUserActions.getInstance()
            r8.end(r2)
            r14.removeExtra(r3)
            goto L_0x14b4
        L_0x14b2:
            r28 = r8
        L_0x14b4:
            if (r51 != 0) goto L_0x14ce
            int r2 = r15.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            boolean r2 = r2.isClientActivated()
            if (r2 == 0) goto L_0x14c3
            goto L_0x14ce
        L_0x14c3:
            r3 = r15
            r61 = r27
            r62 = r28
            r24 = -1
            r55 = 0
            goto L_0x162f
        L_0x14ce:
            if (r5 != 0) goto L_0x1612
            if (r1 == 0) goto L_0x14d4
            goto L_0x1612
        L_0x14d4:
            if (r25 != 0) goto L_0x15b8
            if (r0 != 0) goto L_0x15b8
            if (r6 != 0) goto L_0x15b8
            if (r13 != 0) goto L_0x15b8
            if (r47 != 0) goto L_0x15b8
            if (r48 != 0) goto L_0x15b8
            if (r50 != 0) goto L_0x15b8
            if (r49 != 0) goto L_0x15b8
            if (r51 != 0) goto L_0x15b8
            if (r53 != 0) goto L_0x15b8
            if (r44 != 0) goto L_0x15b8
            if (r54 != 0) goto L_0x15b8
            if (r52 == 0) goto L_0x14f0
            goto L_0x15b8
        L_0x14f0:
            android.content.ContentResolver r55 = r65.getContentResolver()     // Catch:{ Exception -> 0x15a1 }
            android.net.Uri r56 = r66.getData()     // Catch:{ Exception -> 0x15a1 }
            r57 = 0
            r58 = 0
            r59 = 0
            r60 = 0
            android.database.Cursor r1 = r55.query(r56, r57, r58, r59, r60)     // Catch:{ Exception -> 0x15a1 }
            if (r1 == 0) goto L_0x1590
            boolean r0 = r1.moveToFirst()     // Catch:{ all -> 0x1582 }
            if (r0 == 0) goto L_0x1590
            java.lang.String r0 = "account_name"
            int r0 = r1.getColumnIndex(r0)     // Catch:{ all -> 0x1582 }
            java.lang.String r0 = r1.getString(r0)     // Catch:{ all -> 0x1582 }
            java.lang.Integer r0 = org.telegram.messenger.Utilities.parseInt(r0)     // Catch:{ all -> 0x1582 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x1582 }
            r2 = 0
            r7 = 3
        L_0x1520:
            if (r2 >= r7) goto L_0x153c
            org.telegram.messenger.UserConfig r3 = org.telegram.messenger.UserConfig.getInstance(r2)     // Catch:{ all -> 0x153a }
            int r3 = r3.getClientUserId()     // Catch:{ all -> 0x153a }
            if (r3 != r0) goto L_0x1536
            r3 = 0
            r27[r3] = r2     // Catch:{ all -> 0x153a }
            r0 = r27[r3]     // Catch:{ all -> 0x153a }
            r9 = 1
            r15.switchToAccount(r0, r9)     // Catch:{ all -> 0x1580 }
            goto L_0x153d
        L_0x1536:
            r9 = 1
            int r2 = r2 + 1
            goto L_0x1520
        L_0x153a:
            r0 = move-exception
            goto L_0x1584
        L_0x153c:
            r9 = 1
        L_0x153d:
            java.lang.String r0 = "data4"
            int r0 = r1.getColumnIndex(r0)     // Catch:{ all -> 0x1580 }
            int r2 = r1.getInt(r0)     // Catch:{ all -> 0x1580 }
            r3 = 0
            r0 = r27[r3]     // Catch:{ all -> 0x1580 }
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getInstance(r0)     // Catch:{ all -> 0x1580 }
            int r4 = org.telegram.messenger.NotificationCenter.closeChats     // Catch:{ all -> 0x1580 }
            java.lang.Object[] r5 = new java.lang.Object[r3]     // Catch:{ all -> 0x1580 }
            r0.postNotificationName(r4, r5)     // Catch:{ all -> 0x1580 }
            java.lang.String r0 = "mimetype"
            int r0 = r1.getColumnIndex(r0)     // Catch:{ all -> 0x157c }
            java.lang.String r0 = r1.getString(r0)     // Catch:{ all -> 0x157c }
            java.lang.String r3 = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call"
            boolean r3 = android.text.TextUtils.equals(r0, r3)     // Catch:{ all -> 0x157c }
            if (r3 == 0) goto L_0x156c
            r26 = r2
            r6 = 1
            goto L_0x1594
        L_0x156c:
            java.lang.String r3 = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video"
            boolean r0 = android.text.TextUtils.equals(r0, r3)     // Catch:{ all -> 0x157c }
            r26 = r2
            r6 = r33
            if (r0 == 0) goto L_0x1594
            r34 = 1
            goto L_0x1594
        L_0x157c:
            r0 = move-exception
            r26 = r2
            goto L_0x1585
        L_0x1580:
            r0 = move-exception
            goto L_0x1585
        L_0x1582:
            r0 = move-exception
            r7 = 3
        L_0x1584:
            r9 = 1
        L_0x1585:
            throw r0     // Catch:{ all -> 0x1586 }
        L_0x1586:
            r0 = move-exception
            r2 = r0
            if (r1 == 0) goto L_0x158d
            r1.close()     // Catch:{ all -> 0x158d }
        L_0x158d:
            throw r2     // Catch:{ Exception -> 0x158e }
        L_0x158e:
            r0 = move-exception
            goto L_0x15a4
        L_0x1590:
            r7 = 3
            r9 = 1
            r6 = r33
        L_0x1594:
            if (r1 == 0) goto L_0x159e
            r1.close()     // Catch:{ Exception -> 0x159a }
            goto L_0x159e
        L_0x159a:
            r0 = move-exception
            r33 = r6
            goto L_0x15a4
        L_0x159e:
            r33 = r6
            goto L_0x15a7
        L_0x15a1:
            r0 = move-exception
            r7 = 3
            r9 = 1
        L_0x15a4:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x15a7:
            r3 = r15
            r12 = r26
            r61 = r27
            r62 = r28
            r13 = r31
            r9 = r39
            r24 = -1
            r55 = 0
            goto L_0x165a
        L_0x15b8:
            r7 = 3
            r9 = 1
            if (r13 == 0) goto L_0x15d7
            java.lang.String r1 = "@"
            boolean r1 = r13.startsWith(r1)
            if (r1 == 0) goto L_0x15d7
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = " "
            r1.append(r2)
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            r8 = r1
            goto L_0x15d8
        L_0x15d7:
            r8 = r13
        L_0x15d8:
            r13 = 0
            r2 = r27[r13]
            r22 = 0
            r1 = r65
            r55 = 0
            r3 = r25
            r4 = r0
            r24 = -1
            r5 = r6
            r6 = r10
            r10 = 3
            r7 = r12
            r12 = r28
            r16 = 1
            r9 = r11
            r11 = 3
            r10 = r43
            r61 = r27
            r11 = r44
            r62 = r12
            r12 = r45
            r13 = r46
            r14 = r47
            r15 = r48
            r16 = r49
            r17 = r50
            r18 = r51
            r19 = r52
            r20 = r53
            r21 = r54
            r1.runLinkRequest(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22)
            r3 = r65
            goto L_0x162f
        L_0x1612:
            r61 = r27
            r62 = r28
            r24 = -1
            r55 = 0
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            r0.putString(r4, r5)
            r0.putString(r7, r1)
            org.telegram.ui.-$$Lambda$LaunchActivity$VK-kZ7jRryo5GCLASSNAMEoFxf--u3Egg r1 = new org.telegram.ui.-$$Lambda$LaunchActivity$VK-kZ7jRryo5GCLASSNAMEoFxf--u3Egg
            r3 = r65
            r1.<init>(r0)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)
        L_0x162f:
            r12 = r26
            r13 = r31
            r9 = r39
            goto L_0x165a
        L_0x1636:
            r62 = r8
            r61 = r11
            r3 = r15
            r24 = -1
            r55 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            r29 = 0
            r30 = 0
            r32 = 0
            r33 = 0
            r34 = 0
            r35 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r40 = 0
            r41 = 0
            r42 = 0
        L_0x165a:
            r11 = r12
            r1 = r13
            r14 = r29
            r4 = r30
            r12 = r33
            r13 = r34
            r63 = r35
            r2 = r40
            r7 = r41
            r68 = r42
            r18 = r55
            r8 = r61
            r6 = r62
            r0 = -1
            r5 = -1
            r15 = 0
            r16 = 0
            r17 = 0
            r20 = 0
            r21 = 0
            r22 = 0
            goto L_0x1801
        L_0x1681:
            r62 = r8
            r61 = r11
            r3 = r15
            r24 = -1
            r55 = 0
            java.lang.String r0 = r66.getAction()
            java.lang.String r1 = "org.telegram.messenger.OPEN_ACCOUNT"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x16a4
            r18 = r55
            r8 = r61
            r6 = r62
            r68 = 0
            r0 = -1
            r1 = 1
            r2 = 0
            r4 = 0
            goto L_0x17e5
        L_0x16a4:
            java.lang.String r0 = r66.getAction()
            java.lang.String r1 = "new_dialog"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x16ce
            r18 = r55
            r8 = r61
            r6 = r62
            r68 = 0
            r0 = -1
            r1 = 0
            r2 = 0
            r4 = 0
            r5 = -1
            r7 = 0
            r9 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r20 = 0
            r21 = 1
            goto L_0x17f5
        L_0x16ce:
            java.lang.String r0 = r66.getAction()
            java.lang.String r1 = "com.tmessages.openchat"
            boolean r0 = r0.startsWith(r1)
            if (r0 == 0) goto L_0x1769
            java.lang.String r0 = "chatId"
            r1 = r66
            r4 = 0
            int r0 = r1.getIntExtra(r0, r4)
            java.lang.String r2 = "userId"
            int r2 = r1.getIntExtra(r2, r4)
            java.lang.String r5 = "encId"
            int r5 = r1.getIntExtra(r5, r4)
            java.lang.String r6 = "appWidgetId"
            int r6 = r1.getIntExtra(r6, r4)
            if (r6 == 0) goto L_0x170b
            java.lang.String r0 = "appWidgetType"
            int r5 = r1.getIntExtra(r0, r4)
            r24 = r6
            r8 = r61
            r6 = r62
            r0 = 0
            r2 = 0
            r7 = 0
            r9 = 6
        L_0x1708:
            r12 = 0
        L_0x1709:
            r13 = 0
            goto L_0x1755
        L_0x170b:
            r6 = r62
            int r7 = r1.getIntExtra(r6, r4)
            if (r0 == 0) goto L_0x1726
            r8 = r61
            r2 = r8[r4]
            org.telegram.messenger.NotificationCenter r2 = org.telegram.messenger.NotificationCenter.getInstance(r2)
            int r5 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r9 = new java.lang.Object[r4]
            r2.postNotificationName(r5, r9)
            r2 = 0
        L_0x1723:
            r5 = -1
            r9 = 0
            goto L_0x1708
        L_0x1726:
            r8 = r61
            if (r2 == 0) goto L_0x173d
            r0 = r8[r4]
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getInstance(r0)
            int r5 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r9 = new java.lang.Object[r4]
            r0.postNotificationName(r5, r9)
            r12 = r2
            r0 = 0
            r2 = 0
            r5 = -1
            r9 = 0
            goto L_0x1709
        L_0x173d:
            if (r5 == 0) goto L_0x174f
            r0 = r8[r4]
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getInstance(r0)
            int r2 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r9 = new java.lang.Object[r4]
            r0.postNotificationName(r2, r9)
            r2 = r5
            r0 = 0
            goto L_0x1723
        L_0x174f:
            r0 = 0
            r2 = 0
            r5 = -1
            r9 = 0
            r12 = 0
            r13 = 1
        L_0x1755:
            r14 = r0
            r15 = r2
            r4 = r7
            r1 = r9
            r11 = r12
            r16 = r13
            r0 = r24
            r18 = r55
            r68 = 0
            r2 = 0
            r7 = 0
            r9 = 0
            r12 = 0
            r13 = 0
            goto L_0x17ef
        L_0x1769:
            r1 = r66
            r8 = r61
            r6 = r62
            r4 = 0
            java.lang.String r0 = r66.getAction()
            java.lang.String r5 = "com.tmessages.openplayer"
            boolean r0 = r0.equals(r5)
            if (r0 == 0) goto L_0x1790
            r18 = r55
            r68 = 0
            r0 = -1
            r1 = 0
            r2 = 0
            r5 = -1
            r7 = 0
            r9 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 1
            goto L_0x17f1
        L_0x1790:
            java.lang.String r0 = r66.getAction()
            java.lang.String r5 = "org.tmessages.openlocations"
            boolean r0 = r0.equals(r5)
            if (r0 == 0) goto L_0x17b2
            r18 = r55
            r68 = 0
            r0 = -1
            r1 = 0
            r2 = 0
            r5 = -1
            r7 = 0
            r9 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r20 = 1
            goto L_0x17f3
        L_0x17b2:
            java.lang.String r0 = "voip_chat"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x17de
            r18 = r55
            r68 = 0
            r0 = -1
            r1 = 0
            r2 = 0
            r5 = -1
            r7 = 0
            r9 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r20 = 0
            r21 = 0
            r22 = 1
            goto L_0x17f7
        L_0x17d5:
            r55 = r3
            r6 = r8
            r8 = r11
            r1 = r14
            r3 = r15
            r4 = 0
            r24 = -1
        L_0x17de:
            r18 = r55
            r68 = 0
            r0 = -1
            r1 = 0
            r2 = 0
        L_0x17e5:
            r5 = -1
            r7 = 0
            r9 = 0
        L_0x17e8:
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
        L_0x17ef:
            r17 = 0
        L_0x17f1:
            r20 = 0
        L_0x17f3:
            r21 = 0
        L_0x17f5:
            r22 = 0
        L_0x17f7:
            r32 = 0
            r36 = 0
            r37 = 0
            r38 = 0
            r63 = 0
        L_0x1801:
            int r10 = r3.currentAccount
            org.telegram.messenger.UserConfig r10 = org.telegram.messenger.UserConfig.getInstance(r10)
            boolean r10 = r10.isClientActivated()
            if (r10 == 0) goto L_0x1CLASSNAME
            if (r9 == 0) goto L_0x1839
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            org.telegram.ui.ActionBar.BaseFragment r10 = r10.getLastFragment()
            r69 = r7
            boolean r7 = r10 instanceof org.telegram.ui.DialogsActivity
            if (r7 == 0) goto L_0x1835
            org.telegram.ui.DialogsActivity r10 = (org.telegram.ui.DialogsActivity) r10
            boolean r7 = r10.isMainDialogList()
            if (r7 == 0) goto L_0x1833
            android.view.View r7 = r10.getFragmentView()
            if (r7 == 0) goto L_0x182e
            r7 = 1
            r10.search(r9, r7)
            goto L_0x183c
        L_0x182e:
            r7 = 1
            r10.setInitialSearchString(r9)
            goto L_0x183c
        L_0x1833:
            r7 = 1
            goto L_0x183c
        L_0x1835:
            r7 = 1
            r16 = 1
            goto L_0x183c
        L_0x1839:
            r69 = r7
            goto L_0x1833
        L_0x183c:
            if (r11 == 0) goto L_0x18ad
            if (r12 != 0) goto L_0x188d
            if (r13 == 0) goto L_0x1843
            goto L_0x188d
        L_0x1843:
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "user_id"
            r0.putInt(r1, r11)
            if (r4 == 0) goto L_0x1853
            r0.putInt(r6, r4)
        L_0x1853:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x1875
            r1 = 0
            r2 = r8[r1]
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r2)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r2 = mainFragmentsStack
            int r4 = r2.size()
            int r4 = r4 - r7
            java.lang.Object r2 = r2.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r2 = (org.telegram.ui.ActionBar.BaseFragment) r2
            boolean r1 = r1.checkCanOpenChat(r0, r2)
            if (r1 == 0) goto L_0x1923
        L_0x1875:
            org.telegram.ui.ChatActivity r11 = new org.telegram.ui.ChatActivity
            r11.<init>(r0)
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            boolean r0 = r10.presentFragment(r11, r12, r13, r14, r15)
            if (r0 == 0) goto L_0x1923
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.closeDrawer()
            goto L_0x1921
        L_0x188d:
            if (r63 == 0) goto L_0x18a7
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            org.telegram.ui.ActionBar.BaseFragment r0 = r0.getLastFragment()
            if (r0 == 0) goto L_0x18aa
            org.telegram.messenger.MessagesController r1 = r0.getMessagesController()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r11)
            org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r2)
            org.telegram.ui.Components.AlertsCreator.createCallDialogAlert(r0, r1, r13)
            goto L_0x18aa
        L_0x18a7:
            org.telegram.messenger.voip.VoIPPendingCall.startOrSchedule(r3, r11, r13)
        L_0x18aa:
            r2 = 0
            goto L_0x1CLASSNAME
        L_0x18ad:
            if (r14 == 0) goto L_0x18f7
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "chat_id"
            r0.putInt(r1, r14)
            if (r4 == 0) goto L_0x18be
            r0.putInt(r6, r4)
        L_0x18be:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x18e0
            r1 = 0
            r2 = r8[r1]
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r2)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r2 = mainFragmentsStack
            int r4 = r2.size()
            int r4 = r4 - r7
            java.lang.Object r2 = r2.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r2 = (org.telegram.ui.ActionBar.BaseFragment) r2
            boolean r1 = r1.checkCanOpenChat(r0, r2)
            if (r1 == 0) goto L_0x1923
        L_0x18e0:
            org.telegram.ui.ChatActivity r11 = new org.telegram.ui.ChatActivity
            r11.<init>(r0)
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            boolean r0 = r10.presentFragment(r11, r12, r13, r14, r15)
            if (r0 == 0) goto L_0x1923
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.closeDrawer()
            goto L_0x1921
        L_0x18f7:
            if (r15 == 0) goto L_0x1929
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "enc_id"
            r0.putInt(r1, r15)
            org.telegram.ui.ChatActivity r1 = new org.telegram.ui.ChatActivity
            r1.<init>(r0)
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r18 = 0
            r19 = 1
            r20 = 1
            r21 = 0
            r16 = r0
            r17 = r1
            boolean r0 = r16.presentFragment(r17, r18, r19, r20, r21)
            if (r0 == 0) goto L_0x1923
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.closeDrawer()
        L_0x1921:
            r13 = 1
            goto L_0x1924
        L_0x1923:
            r13 = 0
        L_0x1924:
            r4 = r67
            r2 = 0
            goto L_0x1c7b
        L_0x1929:
            if (r16 == 0) goto L_0x1964
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 != 0) goto L_0x1937
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.removeAllFragments()
            goto L_0x1962
        L_0x1937:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x1962
        L_0x1941:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            int r0 = r0.size()
            int r0 = r0 - r7
            if (r0 <= 0) goto L_0x195b
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r0.fragmentsStack
            r4 = 0
            java.lang.Object r1 = r1.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1
            r0.removeFragmentFromStack((org.telegram.ui.ActionBar.BaseFragment) r1)
            goto L_0x1941
        L_0x195b:
            r4 = 0
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            r0.closeLastFragment(r4)
            goto L_0x1985
        L_0x1962:
            r4 = 0
            goto L_0x1985
        L_0x1964:
            r4 = 0
            if (r17 == 0) goto L_0x1988
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x1983
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            java.lang.Object r0 = r0.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            org.telegram.ui.Components.AudioPlayerAlert r1 = new org.telegram.ui.Components.AudioPlayerAlert
            r1.<init>(r3)
            r0.showDialog(r1)
        L_0x1983:
            r4 = r67
        L_0x1985:
            r2 = 0
            goto L_0x1c7a
        L_0x1988:
            if (r20 == 0) goto L_0x19ad
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x1983
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            r1 = 0
            java.lang.Object r0 = r0.get(r1)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            org.telegram.ui.Components.SharingLocationsAlert r1 = new org.telegram.ui.Components.SharingLocationsAlert
            org.telegram.ui.-$$Lambda$LaunchActivity$YHxLpwvfAJ-XuUMLXdxr2hO5tqk r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$YHxLpwvfAJ-XuUMLXdxr2hO5tqk
            r2.<init>(r8)
            r1.<init>(r3, r2)
            r0.showDialog(r1)
            goto L_0x1983
        L_0x19ad:
            android.net.Uri r4 = r3.exportingChatUri
            if (r4 == 0) goto L_0x19b8
            java.util.ArrayList<android.net.Uri> r0 = r3.documentsUrisArray
            r3.runImportRequest(r4, r0)
            goto L_0x18aa
        L_0x19b8:
            java.lang.String r4 = r3.videoPath
            if (r4 != 0) goto L_0x1CLASSNAME
            java.util.ArrayList<org.telegram.messenger.SendMessagesHelper$SendingMediaInfo> r4 = r3.photoPathsArray
            if (r4 != 0) goto L_0x1CLASSNAME
            java.lang.String r4 = r3.sendingText
            if (r4 != 0) goto L_0x1CLASSNAME
            java.util.ArrayList<java.lang.String> r4 = r3.documentsPathsArray
            if (r4 != 0) goto L_0x1CLASSNAME
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r4 = r3.contactsToSend
            if (r4 != 0) goto L_0x1CLASSNAME
            java.util.ArrayList<android.net.Uri> r4 = r3.documentsUrisArray
            if (r4 == 0) goto L_0x19d2
            goto L_0x1CLASSNAME
        L_0x19d2:
            if (r1 == 0) goto L_0x1a5a
            if (r1 != r7) goto L_0x19f2
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            int r2 = r3.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            int r2 = r2.clientUserId
            java.lang.String r4 = "user_id"
            r0.putInt(r4, r2)
            org.telegram.ui.ProfileActivity r2 = new org.telegram.ui.ProfileActivity
            r2.<init>(r0)
            r11 = r2
        L_0x19ef:
            r2 = 6
        L_0x19f0:
            r13 = 0
            goto L_0x1a28
        L_0x19f2:
            r4 = 2
            if (r1 != r4) goto L_0x19fd
            org.telegram.ui.ThemeActivity r0 = new org.telegram.ui.ThemeActivity
            r2 = 0
            r0.<init>(r2)
        L_0x19fb:
            r11 = r0
            goto L_0x19ef
        L_0x19fd:
            r2 = 0
            r4 = 3
            if (r1 != r4) goto L_0x1a07
            org.telegram.ui.SessionsActivity r0 = new org.telegram.ui.SessionsActivity
            r0.<init>(r2)
            goto L_0x19fb
        L_0x1a07:
            r2 = 4
            if (r1 != r2) goto L_0x1a10
            org.telegram.ui.FiltersSetupActivity r0 = new org.telegram.ui.FiltersSetupActivity
            r0.<init>()
            goto L_0x19fb
        L_0x1a10:
            r2 = 5
            if (r1 != r2) goto L_0x1a1c
            org.telegram.ui.ActionIntroActivity r0 = new org.telegram.ui.ActionIntroActivity
            r0.<init>(r4)
            r11 = r0
            r2 = 6
            r13 = 1
            goto L_0x1a28
        L_0x1a1c:
            r2 = 6
            if (r1 != r2) goto L_0x1a26
            org.telegram.ui.EditWidgetActivity r4 = new org.telegram.ui.EditWidgetActivity
            r4.<init>(r5, r0, r7)
            r11 = r4
            goto L_0x19f0
        L_0x1a26:
            r11 = 0
            goto L_0x19f0
        L_0x1a28:
            if (r1 != r2) goto L_0x1a34
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            r10.presentFragment(r11, r12, r13, r14, r15)
            goto L_0x1a3c
        L_0x1a34:
            org.telegram.ui.-$$Lambda$LaunchActivity$khhShGuCQfuJII48gK_zJ7yqvar_ r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$khhShGuCQfuJII48gK_zJ7yqvar_
            r0.<init>(r11, r13)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)
        L_0x1a3c:
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1a53
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1a92
        L_0x1a53:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1a92
        L_0x1a5a:
            r4 = 2
            if (r21 == 0) goto L_0x1a97
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "destroyAfterSelect"
            r0.putBoolean(r1, r7)
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            org.telegram.ui.ContactsActivity r11 = new org.telegram.ui.ContactsActivity
            r11.<init>(r0)
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            r10.presentFragment(r11, r12, r13, r14, r15)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1a8c
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1a92
        L_0x1a8c:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
        L_0x1a92:
            r4 = r67
            r2 = 0
            goto L_0x1CLASSNAME
        L_0x1a97:
            if (r2 == 0) goto L_0x1af4
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "destroyAfterSelect"
            r0.putBoolean(r1, r7)
            java.lang.String r1 = "returnAsResult"
            r0.putBoolean(r1, r7)
            java.lang.String r1 = "onlyUsers"
            r0.putBoolean(r1, r7)
            java.lang.String r1 = "allowSelf"
            r4 = 0
            r0.putBoolean(r1, r4)
            org.telegram.ui.ContactsActivity r15 = new org.telegram.ui.ContactsActivity
            r15.<init>(r0)
            r15.setInitialSearchString(r2)
            org.telegram.ui.-$$Lambda$LaunchActivity$_L_5IaBoty343YZQPMn860Vrvd8 r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$_L_5IaBoty343YZQPMn860Vrvd8
            r0.<init>(r13)
            r15.setDelegate(r0)
            org.telegram.ui.ActionBar.ActionBarLayout r14 = r3.actionBarLayout
            org.telegram.ui.ActionBar.BaseFragment r0 = r14.getLastFragment()
            boolean r0 = r0 instanceof org.telegram.ui.ContactsActivity
            r17 = 1
            r18 = 1
            r19 = 0
            r16 = r0
            r14.presentFragment(r15, r16, r17, r18, r19)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1aed
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1a92
        L_0x1aed:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1a92
        L_0x1af4:
            if (r38 == 0) goto L_0x1b2d
            org.telegram.ui.ActionIntroActivity r11 = new org.telegram.ui.ActionIntroActivity
            r0 = 5
            r11.<init>(r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$8KPGrhe6Jj04g840aKU1DGBb1qE r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$8KPGrhe6Jj04g840aKU1DGBb1qE
            r0.<init>(r11)
            r11.setQrLoginDelegate(r0)
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            r10.presentFragment(r11, r12, r13, r14, r15)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1b25
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1a92
        L_0x1b25:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1a92
        L_0x1b2d:
            r1 = 0
            if (r36 == 0) goto L_0x1b7f
            org.telegram.ui.NewContactActivity r11 = new org.telegram.ui.NewContactActivity
            r11.<init>()
            if (r69 == 0) goto L_0x1b4b
            java.lang.String r0 = " "
            r2 = r69
            java.lang.String[] r0 = r2.split(r0, r4)
            r2 = r0[r1]
            int r4 = r0.length
            if (r4 <= r7) goto L_0x1b47
            r0 = r0[r7]
            goto L_0x1b48
        L_0x1b47:
            r0 = 0
        L_0x1b48:
            r11.setInitialName(r2, r0)
        L_0x1b4b:
            if (r68 == 0) goto L_0x1b56
            r0 = r68
            java.lang.String r0 = org.telegram.PhoneFormat.PhoneFormat.stripExceptNumbers(r0, r7)
            r11.setInitialPhoneNumber(r0, r1)
        L_0x1b56:
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r3.actionBarLayout
            r12 = 0
            r13 = 1
            r14 = 1
            r15 = 0
            r10.presentFragment(r11, r12, r13, r14, r15)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1b77
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1a92
        L_0x1b77:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1a92
        L_0x1b7f:
            r0 = r68
            r2 = r69
            if (r22 == 0) goto L_0x1b96
            int r0 = r3.currentAccount
            org.telegram.messenger.AccountInstance r0 = org.telegram.messenger.AccountInstance.getInstance(r0)
            org.telegram.ui.GroupCallActivity.create(r3, r0)
            org.telegram.ui.GroupCallActivity r0 = org.telegram.ui.GroupCallActivity.groupCallInstance
            if (r0 == 0) goto L_0x18aa
            org.telegram.ui.GroupCallActivity.groupCallUiVisible = r7
            goto L_0x18aa
        L_0x1b96:
            if (r37 == 0) goto L_0x1CLASSNAME
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r3.actionBarLayout
            org.telegram.ui.ActionBar.BaseFragment r1 = r1.getLastFragment()
            if (r1 == 0) goto L_0x1c0e
            android.app.Activity r4 = r1.getParentActivity()
            if (r4 == 0) goto L_0x1c0e
            int r4 = r3.currentAccount
            org.telegram.messenger.UserConfig r4 = org.telegram.messenger.UserConfig.getInstance(r4)
            org.telegram.tgnet.TLRPC$User r4 = r4.getCurrentUser()
            r5 = 0
            java.lang.String r0 = org.telegram.ui.NewContactActivity.getPhoneNumber(r3, r4, r0, r5)
            org.telegram.ui.ActionBar.AlertDialog$Builder r4 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r5 = r1.getParentActivity()
            r4.<init>((android.content.Context) r5)
            r5 = 2131626195(0x7f0e08d3, float:1.887962E38)
            java.lang.String r6 = "NewContactAlertTitle"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r4.setTitle(r5)
            r5 = 2131626194(0x7f0e08d2, float:1.8879617E38)
            java.lang.Object[] r6 = new java.lang.Object[r7]
            org.telegram.PhoneFormat.PhoneFormat r10 = org.telegram.PhoneFormat.PhoneFormat.getInstance()
            java.lang.String r10 = r10.format(r0)
            r11 = 0
            r6[r11] = r10
            java.lang.String r10 = "NewContactAlertMessage"
            java.lang.String r5 = org.telegram.messenger.LocaleController.formatString(r10, r5, r6)
            android.text.SpannableStringBuilder r5 = org.telegram.messenger.AndroidUtilities.replaceTags(r5)
            r4.setMessage(r5)
            r5 = 2131626193(0x7f0e08d1, float:1.8879615E38)
            java.lang.String r6 = "NewContactAlertButton"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            org.telegram.ui.-$$Lambda$LaunchActivity$sE5-pyjtIyn8IQYwQNwWAj1sFSE r6 = new org.telegram.ui.-$$Lambda$LaunchActivity$sE5-pyjtIyn8IQYwQNwWAj1sFSE
            r6.<init>(r0, r2, r1)
            r4.setPositiveButton(r5, r6)
            r0 = 2131624637(0x7f0e02bd, float:1.887646E38)
            java.lang.String r2 = "Cancel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r2 = 0
            r4.setNegativeButton(r0, r2)
            org.telegram.ui.ActionBar.AlertDialog r0 = r4.create()
            r1.showDialog(r0)
            r13 = 1
            goto L_0x1CLASSNAME
        L_0x1c0e:
            r2 = 0
            r13 = 0
        L_0x1CLASSNAME:
            r4 = r67
            goto L_0x1c7b
        L_0x1CLASSNAME:
            r2 = 0
            if (r32 == 0) goto L_0x1CLASSNAME
            org.telegram.ui.ActionBar.ActionBarLayout r14 = r3.actionBarLayout
            org.telegram.ui.CallLogActivity r15 = new org.telegram.ui.CallLogActivity
            r15.<init>()
            r16 = 0
            r17 = 1
            r18 = 1
            r19 = 0
            r14.presentFragment(r15, r16, r17, r18, r19)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1c3f
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1CLASSNAME
        L_0x1c3f:
            r1 = 0
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1CLASSNAME
        L_0x1CLASSNAME:
            r1 = 0
            r2 = 0
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 != 0) goto L_0x1c5b
            r0 = r8[r1]
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getInstance(r0)
            int r4 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r5 = new java.lang.Object[r1]
            r0.postNotificationName(r4, r5)
        L_0x1c5b:
            int r0 = (r18 > r55 ? 1 : (r18 == r55 ? 0 : -1))
            if (r0 != 0) goto L_0x1CLASSNAME
            r3.openDialogsToSend(r1)
        L_0x1CLASSNAME:
            r4 = r67
        L_0x1CLASSNAME:
            r13 = 1
            goto L_0x1c7b
        L_0x1CLASSNAME:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.lang.Long r4 = java.lang.Long.valueOf(r18)
            r0.add(r4)
            r3.didSelectDialogs(r2, r0, r2, r1)
            goto L_0x1CLASSNAME
        L_0x1CLASSNAME:
            r2 = 0
            r7 = 1
        L_0x1CLASSNAME:
            r4 = r67
        L_0x1c7a:
            r13 = 0
        L_0x1c7b:
            if (r13 != 0) goto L_0x1d27
            if (r4 != 0) goto L_0x1d27
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1cd1
            int r0 = r3.currentAccount
            org.telegram.messenger.UserConfig r0 = org.telegram.messenger.UserConfig.getInstance(r0)
            boolean r0 = r0.isClientActivated()
            if (r0 != 0) goto L_0x1cac
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x1d12
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            org.telegram.ui.LoginActivity r1 = new org.telegram.ui.LoginActivity
            r1.<init>()
            r0.addFragmentToStack(r1)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1d12
        L_0x1cac:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x1d12
            org.telegram.ui.DialogsActivity r0 = new org.telegram.ui.DialogsActivity
            r0.<init>(r2)
            org.telegram.ui.Components.RecyclerListView r1 = r3.sideMenu
            r0.setSideMenu(r1)
            if (r9 == 0) goto L_0x1cc5
            r0.setInitialSearchString(r9)
        L_0x1cc5:
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r3.actionBarLayout
            r1.addFragmentToStack(r0)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r7, r1)
            goto L_0x1d12
        L_0x1cd1:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x1d12
            int r0 = r3.currentAccount
            org.telegram.messenger.UserConfig r0 = org.telegram.messenger.UserConfig.getInstance(r0)
            boolean r0 = r0.isClientActivated()
            if (r0 != 0) goto L_0x1cf8
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            org.telegram.ui.LoginActivity r1 = new org.telegram.ui.LoginActivity
            r1.<init>()
            r0.addFragmentToStack(r1)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r1, r1)
            goto L_0x1d12
        L_0x1cf8:
            org.telegram.ui.DialogsActivity r0 = new org.telegram.ui.DialogsActivity
            r0.<init>(r2)
            org.telegram.ui.Components.RecyclerListView r1 = r3.sideMenu
            r0.setSideMenu(r1)
            if (r9 == 0) goto L_0x1d07
            r0.setInitialSearchString(r9)
        L_0x1d07:
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r3.actionBarLayout
            r1.addFragmentToStack(r0)
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r3.drawerLayoutContainer
            r1 = 0
            r0.setAllowOpenDrawer(r7, r1)
        L_0x1d12:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.actionBarLayout
            r0.showLastFragment()
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x1d27
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.layersActionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r3.rightActionBarLayout
            r0.showLastFragment()
        L_0x1d27:
            if (r23 == 0) goto L_0x1d2f
            r1 = 0
            r0 = r8[r1]
            org.telegram.ui.VoIPFragment.show(r3, r0)
        L_0x1d2f:
            r1 = r66
            r1.setAction(r2)
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.handleIntent(android.content.Intent, boolean, boolean, boolean):boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$6 */
    public /* synthetic */ void lambda$handleIntent$6$LaunchActivity(Intent intent, boolean z) {
        handleIntent(intent, true, false, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$7 */
    public /* synthetic */ void lambda$handleIntent$7$LaunchActivity(Bundle bundle) {
        lambda$runLinkRequest$41(new CancelAccountDeletionActivity(bundle));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$9 */
    public /* synthetic */ void lambda$handleIntent$9$LaunchActivity(int[] iArr, LocationController.SharingLocationInfo sharingLocationInfo) {
        iArr[0] = sharingLocationInfo.messageObject.currentAccount;
        switchToAccount(iArr[0], true);
        LocationActivity locationActivity = new LocationActivity(2);
        locationActivity.setMessageObject(sharingLocationInfo.messageObject);
        locationActivity.setDelegate(new LocationActivity.LocationActivityDelegate(iArr, sharingLocationInfo.messageObject.getDialogId()) {
            public final /* synthetic */ int[] f$0;
            public final /* synthetic */ long f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void didSelectLocation(TLRPC$MessageMedia tLRPC$MessageMedia, int i, boolean z, int i2) {
                SendMessagesHelper.getInstance(this.f$0[0]).sendMessage(tLRPC$MessageMedia, this.f$1, (MessageObject) null, (MessageObject) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, z, i2);
            }
        });
        lambda$runLinkRequest$41(locationActivity);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$10 */
    public /* synthetic */ void lambda$handleIntent$10$LaunchActivity(BaseFragment baseFragment, boolean z) {
        presentFragment(baseFragment, z, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$11 */
    public /* synthetic */ void lambda$handleIntent$11$LaunchActivity(boolean z, TLRPC$User tLRPC$User, String str, ContactsActivity contactsActivity) {
        TLRPC$UserFull userFull = MessagesController.getInstance(this.currentAccount).getUserFull(tLRPC$User.id);
        VoIPHelper.startCall(tLRPC$User, z, userFull != null && userFull.video_calls_available, this, userFull);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleIntent$15 */
    public /* synthetic */ void lambda$handleIntent$15$LaunchActivity(ActionIntroActivity actionIntroActivity, String str) {
        AlertDialog alertDialog = new AlertDialog(this, 3);
        alertDialog.setCanCacnel(false);
        alertDialog.show();
        byte[] decode = Base64.decode(str.substring(17), 8);
        TLRPC$TL_auth_acceptLoginToken tLRPC$TL_auth_acceptLoginToken = new TLRPC$TL_auth_acceptLoginToken();
        tLRPC$TL_auth_acceptLoginToken.token = decode;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_auth_acceptLoginToken, new RequestDelegate(actionIntroActivity) {
            public final /* synthetic */ ActionIntroActivity f$1;

            {
                this.f$1 = r2;
            }

            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                AndroidUtilities.runOnUIThread(new Runnable(tLObject, this.f$1, tLRPC$TL_error) {
                    public final /* synthetic */ TLObject f$1;
                    public final /* synthetic */ ActionIntroActivity f$2;
                    public final /* synthetic */ TLRPC$TL_error f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        LaunchActivity.lambda$null$13(AlertDialog.this, this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        });
    }

    static /* synthetic */ void lambda$null$13(AlertDialog alertDialog, TLObject tLObject, ActionIntroActivity actionIntroActivity, TLRPC$TL_error tLRPC$TL_error) {
        try {
            alertDialog.dismiss();
        } catch (Exception unused) {
        }
        if (!(tLObject instanceof TLRPC$TL_authorization)) {
            AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error) {
                public final /* synthetic */ TLRPC$TL_error f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    LaunchActivity.lambda$null$12(ActionIntroActivity.this, this.f$1);
                }
            });
        }
    }

    static /* synthetic */ void lambda$null$12(ActionIntroActivity actionIntroActivity, TLRPC$TL_error tLRPC$TL_error) {
        String string = LocaleController.getString("AuthAnotherClient", NUM);
        AlertsCreator.showSimpleAlert(actionIntroActivity, string, LocaleController.getString("ErrorOccurred", NUM) + "\n" + tLRPC$TL_error.text);
    }

    static /* synthetic */ void lambda$handleIntent$16(String str, String str2, BaseFragment baseFragment, DialogInterface dialogInterface, int i) {
        NewContactActivity newContactActivity = new NewContactActivity();
        newContactActivity.setInitialPhoneNumber(str, false);
        if (str2 != null) {
            String[] split = str2.split(" ", 2);
            newContactActivity.setInitialName(split[0], split.length > 1 ? split[1] : null);
        }
        baseFragment.presentFragment(newContactActivity);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x007a, code lost:
        if ((r0.get(r0.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0096, code lost:
        if ((r0.get(r0.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x0098;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openDialogsToSend(boolean r11) {
        /*
            r10 = this;
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "onlySelect"
            r2 = 1
            r0.putBoolean(r1, r2)
            java.lang.String r1 = "dialogsType"
            r3 = 3
            r0.putInt(r1, r3)
            java.lang.String r1 = "allowSwitchAccount"
            r0.putBoolean(r1, r2)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r1 = r10.contactsToSend
            java.lang.String r3 = "selectAlertStringGroup"
            r4 = 2131627316(0x7f0e0d34, float:1.8881893E38)
            java.lang.String r5 = "selectAlertString"
            if (r1 == 0) goto L_0x003d
            int r1 = r1.size()
            if (r1 == r2) goto L_0x0052
            java.lang.String r1 = "SendContactToText"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r4)
            r0.putString(r5, r1)
            r1 = 2131627293(0x7f0e0d1d, float:1.8881846E38)
            java.lang.String r4 = "SendContactToGroupText"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            r0.putString(r3, r1)
            goto L_0x0052
        L_0x003d:
            java.lang.String r1 = "SendMessagesToText"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r4)
            r0.putString(r5, r1)
            r1 = 2131627315(0x7f0e0d33, float:1.888189E38)
            java.lang.String r4 = "SendMessagesToGroupText"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            r0.putString(r3, r1)
        L_0x0052:
            org.telegram.ui.DialogsActivity r5 = new org.telegram.ui.DialogsActivity
            r5.<init>(r0)
            r5.setDelegate(r10)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            r1 = 0
            if (r0 == 0) goto L_0x007d
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            int r0 = r0.size()
            if (r0 <= 0) goto L_0x009a
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            int r3 = r0.size()
            int r3 = r3 - r2
            java.lang.Object r0 = r0.get(r3)
            boolean r0 = r0 instanceof org.telegram.ui.DialogsActivity
            if (r0 == 0) goto L_0x009a
            goto L_0x0098
        L_0x007d:
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            int r0 = r0.size()
            if (r0 <= r2) goto L_0x009a
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            int r3 = r0.size()
            int r3 = r3 - r2
            java.lang.Object r0 = r0.get(r3)
            boolean r0 = r0 instanceof org.telegram.ui.DialogsActivity
            if (r0 == 0) goto L_0x009a
        L_0x0098:
            r0 = 1
            goto L_0x009b
        L_0x009a:
            r0 = 0
        L_0x009b:
            r6 = r0
            org.telegram.ui.ActionBar.ActionBarLayout r4 = r10.actionBarLayout
            r7 = r11 ^ 1
            r8 = 1
            r9 = 0
            r4.presentFragment(r5, r6, r7, r8, r9)
            boolean r0 = org.telegram.ui.SecretMediaViewer.hasInstance()
            if (r0 == 0) goto L_0x00bd
            org.telegram.ui.SecretMediaViewer r0 = org.telegram.ui.SecretMediaViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x00bd
            org.telegram.ui.SecretMediaViewer r0 = org.telegram.ui.SecretMediaViewer.getInstance()
            r0.closePhoto(r1, r1)
            goto L_0x00ec
        L_0x00bd:
            boolean r0 = org.telegram.ui.PhotoViewer.hasInstance()
            if (r0 == 0) goto L_0x00d5
            org.telegram.ui.PhotoViewer r0 = org.telegram.ui.PhotoViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x00d5
            org.telegram.ui.PhotoViewer r0 = org.telegram.ui.PhotoViewer.getInstance()
            r0.closePhoto(r1, r2)
            goto L_0x00ec
        L_0x00d5:
            boolean r0 = org.telegram.ui.ArticleViewer.hasInstance()
            if (r0 == 0) goto L_0x00ec
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x00ec
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            r0.close(r1, r2)
        L_0x00ec:
            if (r11 != 0) goto L_0x0109
            org.telegram.ui.ActionBar.DrawerLayoutContainer r11 = r10.drawerLayoutContainer
            r11.setAllowOpenDrawer(r1, r1)
            boolean r11 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r11 == 0) goto L_0x0104
            org.telegram.ui.ActionBar.ActionBarLayout r11 = r10.actionBarLayout
            r11.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r11 = r10.rightActionBarLayout
            r11.showLastFragment()
            goto L_0x0109
        L_0x0104:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r11 = r10.drawerLayoutContainer
            r11.setAllowOpenDrawer(r2, r1)
        L_0x0109:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.openDialogsToSend(boolean):void");
    }

    private int runCommentRequest(int i, AlertDialog alertDialog, Integer num, Integer num2, Integer num3, TLRPC$Chat tLRPC$Chat) {
        if (tLRPC$Chat == null) {
            return 0;
        }
        TLRPC$TL_messages_getDiscussionMessage tLRPC$TL_messages_getDiscussionMessage = new TLRPC$TL_messages_getDiscussionMessage();
        tLRPC$TL_messages_getDiscussionMessage.peer = MessagesController.getInputPeer(tLRPC$Chat);
        tLRPC$TL_messages_getDiscussionMessage.msg_id = (num2 != null ? num : num3).intValue();
        return ConnectionsManager.getInstance(i).sendRequest(tLRPC$TL_messages_getDiscussionMessage, new RequestDelegate(i, num, tLRPC$Chat, tLRPC$TL_messages_getDiscussionMessage, num2, num3, alertDialog) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ Integer f$2;
            public final /* synthetic */ TLRPC$Chat f$3;
            public final /* synthetic */ TLRPC$TL_messages_getDiscussionMessage f$4;
            public final /* synthetic */ Integer f$5;
            public final /* synthetic */ Integer f$6;
            public final /* synthetic */ AlertDialog f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                LaunchActivity.this.lambda$runCommentRequest$18$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, tLObject, tLRPC$TL_error);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runCommentRequest$18 */
    public /* synthetic */ void lambda$runCommentRequest$18$LaunchActivity(int i, Integer num, TLRPC$Chat tLRPC$Chat, TLRPC$TL_messages_getDiscussionMessage tLRPC$TL_messages_getDiscussionMessage, Integer num2, Integer num3, AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, i, num, tLRPC$Chat, tLRPC$TL_messages_getDiscussionMessage, num2, num3, alertDialog) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ Integer f$3;
            public final /* synthetic */ TLRPC$Chat f$4;
            public final /* synthetic */ TLRPC$TL_messages_getDiscussionMessage f$5;
            public final /* synthetic */ Integer f$6;
            public final /* synthetic */ Integer f$7;
            public final /* synthetic */ AlertDialog f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$17$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0095 A[SYNTHETIC, Splitter:B:15:0x0095] */
    /* renamed from: lambda$null$17 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$17$LaunchActivity(org.telegram.tgnet.TLObject r12, int r13, java.lang.Integer r14, org.telegram.tgnet.TLRPC$Chat r15, org.telegram.tgnet.TLRPC$TL_messages_getDiscussionMessage r16, java.lang.Integer r17, java.lang.Integer r18, org.telegram.ui.ActionBar.AlertDialog r19) {
        /*
            r11 = this;
            r0 = r12
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messages_discussionMessage
            r2 = 0
            r3 = 1
            if (r1 == 0) goto L_0x0092
            org.telegram.tgnet.TLRPC$TL_messages_discussionMessage r0 = (org.telegram.tgnet.TLRPC$TL_messages_discussionMessage) r0
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r13)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r4 = r0.users
            r1.putUsers(r4, r2)
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r13)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r4 = r0.chats
            r1.putChats(r4, r2)
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Message> r1 = r0.messages
            int r1 = r1.size()
            r4 = 0
        L_0x0027:
            if (r4 >= r1) goto L_0x003e
            org.telegram.messenger.MessageObject r5 = new org.telegram.messenger.MessageObject
            int r7 = org.telegram.messenger.UserConfig.selectedAccount
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Message> r8 = r0.messages
            java.lang.Object r8 = r8.get(r4)
            org.telegram.tgnet.TLRPC$Message r8 = (org.telegram.tgnet.TLRPC$Message) r8
            r5.<init>(r7, r8, r3, r3)
            r6.add(r5)
            int r4 = r4 + 1
            goto L_0x0027
        L_0x003e:
            boolean r1 = r6.isEmpty()
            if (r1 != 0) goto L_0x0092
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.Object r2 = r6.get(r2)
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2
            long r4 = r2.getDialogId()
            long r4 = -r4
            int r2 = (int) r4
            java.lang.String r4 = "chat_id"
            r1.putInt(r4, r2)
            int r2 = r14.intValue()
            int r2 = java.lang.Math.max(r3, r2)
            java.lang.String r4 = "message_id"
            r1.putInt(r4, r2)
            org.telegram.ui.ChatActivity r2 = new org.telegram.ui.ChatActivity
            r2.<init>(r1)
            r1 = r16
            int r8 = r1.msg_id
            int r9 = r0.read_inbox_max_id
            int r10 = r0.read_outbox_max_id
            r5 = r2
            r7 = r15
            r5.setThreadMessages(r6, r7, r8, r9, r10)
            if (r17 == 0) goto L_0x0083
            int r0 = r17.intValue()
            r2.setHighlightMessageId(r0)
            goto L_0x008c
        L_0x0083:
            if (r18 == 0) goto L_0x008c
            int r0 = r14.intValue()
            r2.setHighlightMessageId(r0)
        L_0x008c:
            r1 = r11
            r11.lambda$runLinkRequest$41(r2)
            r2 = 1
            goto L_0x0093
        L_0x0092:
            r1 = r11
        L_0x0093:
            if (r2 != 0) goto L_0x00c3
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack     // Catch:{ Exception -> 0x00bf }
            boolean r0 = r0.isEmpty()     // Catch:{ Exception -> 0x00bf }
            if (r0 != 0) goto L_0x00c3
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack     // Catch:{ Exception -> 0x00bf }
            int r2 = r0.size()     // Catch:{ Exception -> 0x00bf }
            int r2 = r2 - r3
            java.lang.Object r0 = r0.get(r2)     // Catch:{ Exception -> 0x00bf }
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0     // Catch:{ Exception -> 0x00bf }
            org.telegram.ui.Components.BulletinFactory r0 = org.telegram.ui.Components.BulletinFactory.of((org.telegram.ui.ActionBar.BaseFragment) r0)     // Catch:{ Exception -> 0x00bf }
            java.lang.String r2 = "ChannelPostDeleted"
            r3 = 2131624742(0x7f0e0326, float:1.8876672E38)
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r2, r3)     // Catch:{ Exception -> 0x00bf }
            org.telegram.ui.Components.Bulletin r0 = r0.createErrorBulletin(r2)     // Catch:{ Exception -> 0x00bf }
            r0.show()     // Catch:{ Exception -> 0x00bf }
            goto L_0x00c3
        L_0x00bf:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x00c3:
            r19.dismiss()     // Catch:{ Exception -> 0x00c7 }
            goto L_0x00cc
        L_0x00c7:
            r0 = move-exception
            r2 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r2)
        L_0x00cc:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$17$LaunchActivity(org.telegram.tgnet.TLObject, int, java.lang.Integer, org.telegram.tgnet.TLRPC$Chat, org.telegram.tgnet.TLRPC$TL_messages_getDiscussionMessage, java.lang.Integer, java.lang.Integer, org.telegram.ui.ActionBar.AlertDialog):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007c A[SYNTHETIC, Splitter:B:29:0x007c] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0087 A[SYNTHETIC, Splitter:B:34:0x0087] */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void runImportRequest(android.net.Uri r11, java.util.ArrayList<android.net.Uri> r12) {
        /*
            r10 = this;
            int r12 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.ui.ActionBar.AlertDialog r0 = new org.telegram.ui.ActionBar.AlertDialog
            r1 = 3
            r0.<init>(r10, r1)
            r1 = 1
            int[] r1 = new int[r1]
            r2 = 0
            r1[r2] = r2
            r3 = 0
            android.content.ContentResolver r4 = r10.getContentResolver()     // Catch:{ Exception -> 0x0076 }
            java.io.InputStream r4 = r4.openInputStream(r11)     // Catch:{ Exception -> 0x0076 }
            java.io.BufferedReader r5 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            r6.<init>(r4)     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            r5.<init>(r6)     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            r6.<init>()     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            r7 = 0
        L_0x0027:
            java.lang.String r8 = r5.readLine()     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            if (r8 == 0) goto L_0x003c
            r9 = 100
            if (r7 >= r9) goto L_0x003c
            r6.append(r8)     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            r8 = 10
            r6.append(r8)     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            int r7 = r7 + 1
            goto L_0x0027
        L_0x003c:
            java.lang.String r5 = r6.toString()     // Catch:{ Exception -> 0x0071, all -> 0x006e }
            if (r4 == 0) goto L_0x004a
            r4.close()     // Catch:{ Exception -> 0x0046 }
            goto L_0x004a
        L_0x0046:
            r4 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r4)
        L_0x004a:
            org.telegram.tgnet.TLRPC$TL_messages_checkHistoryImport r4 = new org.telegram.tgnet.TLRPC$TL_messages_checkHistoryImport
            r4.<init>()
            r4.import_head = r5
            org.telegram.tgnet.ConnectionsManager r5 = org.telegram.tgnet.ConnectionsManager.getInstance(r12)
            org.telegram.ui.-$$Lambda$LaunchActivity$_oizVSS35ihStU6XX8OJB0k3vmg r6 = new org.telegram.ui.-$$Lambda$LaunchActivity$_oizVSS35ihStU6XX8OJB0k3vmg
            r6.<init>(r11, r12, r0)
            int r11 = r5.sendRequest(r4, r6)
            r1[r2] = r11
            org.telegram.ui.-$$Lambda$LaunchActivity$ohKO_W48WGqsecb5AjK-LXFCG3o r11 = new org.telegram.ui.-$$Lambda$LaunchActivity$ohKO_W48WGqsecb5AjK-LXFCG3o
            r11.<init>(r12, r1, r3)
            r0.setOnCancelListener(r11)
            r11 = 300(0x12c, double:1.48E-321)
            r0.showDelayed(r11)     // Catch:{ Exception -> 0x006d }
        L_0x006d:
            return
        L_0x006e:
            r11 = move-exception
            r3 = r4
            goto L_0x0085
        L_0x0071:
            r11 = move-exception
            r3 = r4
            goto L_0x0077
        L_0x0074:
            r11 = move-exception
            goto L_0x0085
        L_0x0076:
            r11 = move-exception
        L_0x0077:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r11)     // Catch:{ all -> 0x0074 }
            if (r3 == 0) goto L_0x0084
            r3.close()     // Catch:{ Exception -> 0x0080 }
            goto L_0x0084
        L_0x0080:
            r11 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r11)
        L_0x0084:
            return
        L_0x0085:
            if (r3 == 0) goto L_0x008f
            r3.close()     // Catch:{ Exception -> 0x008b }
            goto L_0x008f
        L_0x008b:
            r12 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r12)
        L_0x008f:
            goto L_0x0091
        L_0x0090:
            throw r11
        L_0x0091:
            goto L_0x0090
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.runImportRequest(android.net.Uri, java.util.ArrayList):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runImportRequest$20 */
    public /* synthetic */ void lambda$runImportRequest$20$LaunchActivity(Uri uri, int i, AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, uri, i, alertDialog) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ Uri f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ AlertDialog f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$19$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, 2);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0114, code lost:
        if ((r10.get(r10.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0130, code lost:
        if ((r10.get(r10.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x0132;
     */
    /* renamed from: lambda$null$19 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$19$LaunchActivity(org.telegram.tgnet.TLObject r10, android.net.Uri r11, int r12, org.telegram.ui.ActionBar.AlertDialog r13) {
        /*
            r9 = this;
            boolean r0 = r9.isFinishing()
            if (r0 != 0) goto L_0x015d
            r0 = 0
            r1 = 1
            if (r10 == 0) goto L_0x013d
            org.telegram.ui.ActionBar.ActionBarLayout r2 = r9.actionBarLayout
            if (r2 == 0) goto L_0x013d
            org.telegram.tgnet.TLRPC$TL_messages_historyImportParsed r10 = (org.telegram.tgnet.TLRPC$TL_messages_historyImportParsed) r10
            android.os.Bundle r2 = new android.os.Bundle
            r2.<init>()
            java.lang.String r3 = "onlySelect"
            r2.putBoolean(r3, r1)
            java.lang.String r3 = r10.title
            java.lang.String r4 = "importTitle"
            r2.putString(r4, r3)
            java.lang.String r3 = "allowSwitchAccount"
            r2.putBoolean(r3, r1)
            boolean r3 = r10.pm
            r4 = 12
            java.lang.String r5 = "dialogsType"
            if (r3 == 0) goto L_0x0032
            r2.putInt(r5, r4)
            goto L_0x008b
        L_0x0032:
            boolean r10 = r10.group
            r3 = 11
            if (r10 == 0) goto L_0x003c
            r2.putInt(r5, r3)
            goto L_0x008b
        L_0x003c:
            java.lang.String r10 = r11.toString()
            org.telegram.messenger.MessagesController r11 = org.telegram.messenger.MessagesController.getInstance(r12)
            java.util.Set<java.lang.String> r11 = r11.exportPrivateUri
            java.util.Iterator r11 = r11.iterator()
        L_0x004a:
            boolean r6 = r11.hasNext()
            if (r6 == 0) goto L_0x0061
            java.lang.Object r6 = r11.next()
            java.lang.String r6 = (java.lang.String) r6
            boolean r6 = r10.contains(r6)
            if (r6 == 0) goto L_0x004a
            r2.putInt(r5, r4)
            r11 = 1
            goto L_0x0062
        L_0x0061:
            r11 = 0
        L_0x0062:
            if (r11 != 0) goto L_0x008b
            org.telegram.messenger.MessagesController r12 = org.telegram.messenger.MessagesController.getInstance(r12)
            java.util.Set<java.lang.String> r12 = r12.exportGroupUri
            java.util.Iterator r12 = r12.iterator()
        L_0x006e:
            boolean r4 = r12.hasNext()
            if (r4 == 0) goto L_0x0084
            java.lang.Object r4 = r12.next()
            java.lang.String r4 = (java.lang.String) r4
            boolean r4 = r10.contains(r4)
            if (r4 == 0) goto L_0x006e
            r2.putInt(r5, r3)
            r11 = 1
        L_0x0084:
            if (r11 != 0) goto L_0x008b
            r10 = 13
            r2.putInt(r5, r10)
        L_0x008b:
            boolean r10 = org.telegram.ui.SecretMediaViewer.hasInstance()
            if (r10 == 0) goto L_0x00a3
            org.telegram.ui.SecretMediaViewer r10 = org.telegram.ui.SecretMediaViewer.getInstance()
            boolean r10 = r10.isVisible()
            if (r10 == 0) goto L_0x00a3
            org.telegram.ui.SecretMediaViewer r10 = org.telegram.ui.SecretMediaViewer.getInstance()
            r10.closePhoto(r0, r0)
            goto L_0x00d2
        L_0x00a3:
            boolean r10 = org.telegram.ui.PhotoViewer.hasInstance()
            if (r10 == 0) goto L_0x00bb
            org.telegram.ui.PhotoViewer r10 = org.telegram.ui.PhotoViewer.getInstance()
            boolean r10 = r10.isVisible()
            if (r10 == 0) goto L_0x00bb
            org.telegram.ui.PhotoViewer r10 = org.telegram.ui.PhotoViewer.getInstance()
            r10.closePhoto(r0, r1)
            goto L_0x00d2
        L_0x00bb:
            boolean r10 = org.telegram.ui.ArticleViewer.hasInstance()
            if (r10 == 0) goto L_0x00d2
            org.telegram.ui.ArticleViewer r10 = org.telegram.ui.ArticleViewer.getInstance()
            boolean r10 = r10.isVisible()
            if (r10 == 0) goto L_0x00d2
            org.telegram.ui.ArticleViewer r10 = org.telegram.ui.ArticleViewer.getInstance()
            r10.close(r0, r1)
        L_0x00d2:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r10 = r9.drawerLayoutContainer
            r10.setAllowOpenDrawer(r0, r0)
            boolean r10 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r10 == 0) goto L_0x00e8
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.actionBarLayout
            r10.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.rightActionBarLayout
            r10.showLastFragment()
            goto L_0x00ed
        L_0x00e8:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r10 = r9.drawerLayoutContainer
            r10.setAllowOpenDrawer(r1, r0)
        L_0x00ed:
            org.telegram.ui.DialogsActivity r4 = new org.telegram.ui.DialogsActivity
            r4.<init>(r2)
            r4.setDelegate(r9)
            boolean r10 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r10 == 0) goto L_0x0117
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = r10.fragmentsStack
            int r10 = r10.size()
            if (r10 <= 0) goto L_0x0133
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = r10.fragmentsStack
            int r11 = r10.size()
            int r11 = r11 - r1
            java.lang.Object r10 = r10.get(r11)
            boolean r10 = r10 instanceof org.telegram.ui.DialogsActivity
            if (r10 == 0) goto L_0x0133
            goto L_0x0132
        L_0x0117:
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = r10.fragmentsStack
            int r10 = r10.size()
            if (r10 <= r1) goto L_0x0133
            org.telegram.ui.ActionBar.ActionBarLayout r10 = r9.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = r10.fragmentsStack
            int r11 = r10.size()
            int r11 = r11 - r1
            java.lang.Object r10 = r10.get(r11)
            boolean r10 = r10 instanceof org.telegram.ui.DialogsActivity
            if (r10 == 0) goto L_0x0133
        L_0x0132:
            r0 = 1
        L_0x0133:
            r5 = r0
            org.telegram.ui.ActionBar.ActionBarLayout r3 = r9.actionBarLayout
            r6 = 0
            r7 = 1
            r8 = 0
            r3.presentFragment(r4, r5, r6, r7, r8)
            goto L_0x0155
        L_0x013d:
            java.util.ArrayList<android.net.Uri> r10 = r9.documentsUrisArray
            if (r10 != 0) goto L_0x0148
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            r9.documentsUrisArray = r10
        L_0x0148:
            java.util.ArrayList<android.net.Uri> r10 = r9.documentsUrisArray
            android.net.Uri r11 = r9.exportingChatUri
            r10.add(r0, r11)
            r10 = 0
            r9.exportingChatUri = r10
            r9.openDialogsToSend(r1)
        L_0x0155:
            r13.dismiss()     // Catch:{ Exception -> 0x0159 }
            goto L_0x015d
        L_0x0159:
            r10 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r10)
        L_0x015d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$19$LaunchActivity(org.telegram.tgnet.TLObject, android.net.Uri, int, org.telegram.ui.ActionBar.AlertDialog):void");
    }

    static /* synthetic */ void lambda$runImportRequest$21(int i, int[] iArr, Runnable runnable, DialogInterface dialogInterface) {
        ConnectionsManager.getInstance(i).cancelRequest(iArr[0], true);
        if (runnable != null) {
            runnable.run();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x02cb  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x03cd  */
    /* JADX WARNING: Removed duplicated region for block: B:98:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void runLinkRequest(int r24, java.lang.String r25, java.lang.String r26, java.lang.String r27, java.lang.String r28, java.lang.String r29, java.lang.String r30, boolean r31, java.lang.Integer r32, java.lang.Integer r33, java.lang.Integer r34, java.lang.Integer r35, java.lang.String r36, java.util.HashMap<java.lang.String, java.lang.String> r37, java.lang.String r38, java.lang.String r39, java.lang.String r40, java.lang.String r41, org.telegram.tgnet.TLRPC$TL_wallPaper r42, java.lang.String r43, int r44) {
        /*
            r23 = this;
            r15 = r23
            r12 = r24
            r0 = r25
            r5 = r26
            r6 = r27
            r9 = r30
            r13 = r33
            r14 = r37
            r11 = r38
            r10 = r39
            r8 = r42
            r7 = r43
            r1 = r44
            r2 = 2
            if (r1 != 0) goto L_0x0062
            int r3 = org.telegram.messenger.UserConfig.getActivatedAccountsCount()
            if (r3 < r2) goto L_0x0062
            if (r14 == 0) goto L_0x0062
            org.telegram.ui.-$$Lambda$LaunchActivity$gNeXmFj7hFvPbeIqNCRkfeUxViI r4 = new org.telegram.ui.-$$Lambda$LaunchActivity$gNeXmFj7hFvPbeIqNCRkfeUxViI
            r1 = r4
            r2 = r23
            r3 = r24
            r12 = r4
            r4 = r25
            r5 = r26
            r6 = r27
            r0 = r7
            r7 = r28
            r8 = r29
            r9 = r30
            r10 = r31
            r11 = r32
            r0 = r12
            r12 = r33
            r13 = r34
            r14 = r35
            r15 = r36
            r16 = r37
            r17 = r38
            r18 = r39
            r19 = r40
            r20 = r41
            r21 = r42
            r22 = r43
            r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22)
            r14 = r23
            org.telegram.ui.ActionBar.AlertDialog r0 = org.telegram.ui.Components.AlertsCreator.createAccountSelectDialog(r14, r0)
            r0.show()
            return
        L_0x0062:
            r3 = r7
            r14 = r15
            r4 = 2131626473(0x7f0e09e9, float:1.8880183E38)
            java.lang.String r7 = "OK"
            r15 = 0
            r8 = 1
            r11 = 0
            if (r40 == 0) goto L_0x00b6
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            int r1 = org.telegram.messenger.NotificationCenter.didReceiveSmsCode
            boolean r0 = r0.hasObservers(r1)
            if (r0 == 0) goto L_0x0086
            org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
            java.lang.Object[] r2 = new java.lang.Object[r8]
            r2[r11] = r40
            r0.postNotificationName(r1, r2)
            goto L_0x00b5
        L_0x0086:
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r0.<init>((android.content.Context) r14)
            r1 = 2131624280(0x7f0e0158, float:1.8875735E38)
            java.lang.String r2 = "AppName"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setTitle(r1)
            r1 = 2131626513(0x7f0e0a11, float:1.8880264E38)
            java.lang.Object[] r2 = new java.lang.Object[r8]
            r2[r11] = r40
            java.lang.String r3 = "OtherLoginCode"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r3, r1, r2)
            android.text.SpannableStringBuilder r1 = org.telegram.messenger.AndroidUtilities.replaceTags(r1)
            r0.setMessage(r1)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r7, r4)
            r0.setPositiveButton(r1, r15)
            r14.showAlertDialog(r0)
        L_0x00b5:
            return
        L_0x00b6:
            if (r41 == 0) goto L_0x00e0
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r0.<init>((android.content.Context) r14)
            r1 = 2131624412(0x7f0e01dc, float:1.8876003E38)
            java.lang.String r2 = "AuthAnotherClient"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setTitle(r1)
            r1 = 2131624421(0x7f0e01e5, float:1.8876021E38)
            java.lang.String r2 = "AuthAnotherClientUrl"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setMessage(r1)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r7, r4)
            r0.setPositiveButton(r1, r15)
            r14.showAlertDialog(r0)
            return
        L_0x00e0:
            org.telegram.ui.ActionBar.AlertDialog r10 = new org.telegram.ui.ActionBar.AlertDialog
            r4 = 3
            r10.<init>(r14, r4)
            int[] r7 = new int[r8]
            r7[r11] = r11
            if (r0 == 0) goto L_0x0125
            org.telegram.tgnet.TLRPC$TL_contacts_resolveUsername r13 = new org.telegram.tgnet.TLRPC$TL_contacts_resolveUsername
            r13.<init>()
            r13.username = r0
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r24)
            org.telegram.ui.-$$Lambda$LaunchActivity$MUUZN9lbMZ1C5nySiSbKTz0WXF4 r9 = new org.telegram.ui.-$$Lambda$LaunchActivity$MUUZN9lbMZ1C5nySiSbKTz0WXF4
            r1 = r9
            r2 = r23
            r3 = r36
            r4 = r24
            r5 = r32
            r6 = r35
            r8 = r7
            r7 = r34
            r16 = r8
            r15 = r9
            r9 = r10
            r12 = r10
            r10 = r29
            r25 = r12
            r12 = 0
            r11 = r28
            r1.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11)
            int r0 = r0.sendRequest(r13, r15)
            r7 = r16
            r7[r12] = r0
            r10 = r24
            r11 = r25
        L_0x0122:
            r4 = 0
            goto L_0x03c8
        L_0x0125:
            r25 = r10
            r12 = 0
            if (r5 == 0) goto L_0x0161
            if (r1 != 0) goto L_0x0147
            org.telegram.tgnet.TLRPC$TL_messages_checkChatInvite r0 = new org.telegram.tgnet.TLRPC$TL_messages_checkChatInvite
            r0.<init>()
            r0.hash = r5
            org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r24)
            org.telegram.ui.-$$Lambda$LaunchActivity$e-ClUywGxaQ-PSWsW_7mI6USITc r3 = new org.telegram.ui.-$$Lambda$LaunchActivity$e-ClUywGxaQ-PSWsW_7mI6USITc
            r10 = r24
            r11 = r25
            r3.<init>(r10, r11, r5)
            int r0 = r1.sendRequest(r0, r3, r2)
            r7[r12] = r0
            goto L_0x0122
        L_0x0147:
            r10 = r24
            r11 = r25
            if (r1 != r8) goto L_0x0122
            org.telegram.tgnet.TLRPC$TL_messages_importChatInvite r0 = new org.telegram.tgnet.TLRPC$TL_messages_importChatInvite
            r0.<init>()
            r0.hash = r5
            org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r24)
            org.telegram.ui.-$$Lambda$LaunchActivity$tx-zYfRNJpo9OSQMqkrWTTVcmeE r3 = new org.telegram.ui.-$$Lambda$LaunchActivity$tx-zYfRNJpo9OSQMqkrWTTVcmeE
            r3.<init>(r10, r11)
            r1.sendRequest(r0, r3, r2)
            goto L_0x0122
        L_0x0161:
            r10 = r24
            r11 = r25
            if (r6 == 0) goto L_0x01bf
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x01be
            org.telegram.tgnet.TLRPC$TL_inputStickerSetShortName r0 = new org.telegram.tgnet.TLRPC$TL_inputStickerSetShortName
            r0.<init>()
            r0.short_name = r6
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            int r2 = r1.size()
            int r2 = r2 - r8
            java.lang.Object r1 = r1.get(r2)
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1
            boolean r2 = r1 instanceof org.telegram.ui.ChatActivity
            if (r2 == 0) goto L_0x01a8
            r2 = r1
            org.telegram.ui.ChatActivity r2 = (org.telegram.ui.ChatActivity) r2
            org.telegram.ui.Components.StickersAlert r3 = new org.telegram.ui.Components.StickersAlert
            r4 = 0
            org.telegram.ui.Components.ChatActivityEnterView r5 = r2.getChatActivityEnterView()
            r24 = r3
            r25 = r23
            r26 = r1
            r27 = r0
            r28 = r4
            r29 = r5
            r24.<init>(r25, r26, r27, r28, r29)
            boolean r0 = r2.isKeyboardVisible()
            r3.setCalcMandatoryInsets(r0)
            goto L_0x01bb
        L_0x01a8:
            org.telegram.ui.Components.StickersAlert r3 = new org.telegram.ui.Components.StickersAlert
            r2 = 0
            r4 = 0
            r24 = r3
            r25 = r23
            r26 = r1
            r27 = r0
            r28 = r2
            r29 = r4
            r24.<init>(r25, r26, r27, r28, r29)
        L_0x01bb:
            r1.showDialog(r3)
        L_0x01be:
            return
        L_0x01bf:
            if (r9 == 0) goto L_0x01e4
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "onlySelect"
            r0.putBoolean(r1, r8)
            java.lang.String r1 = "dialogsType"
            r0.putInt(r1, r4)
            org.telegram.ui.DialogsActivity r1 = new org.telegram.ui.DialogsActivity
            r1.<init>(r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$xiZ0HxBCdiHST-WvYLTYBrvtpxI r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$xiZ0HxBCdiHST-WvYLTYBrvtpxI
            r2 = r31
            r0.<init>(r2, r10, r9)
            r1.setDelegate(r0)
            r14.presentFragment(r1, r12, r8)
            goto L_0x0122
        L_0x01e4:
            r0 = r37
            if (r0 == 0) goto L_0x0251
            java.lang.String r1 = "bot_id"
            java.lang.Object r1 = r0.get(r1)
            java.lang.CharSequence r1 = (java.lang.CharSequence) r1
            java.lang.Integer r1 = org.telegram.messenger.Utilities.parseInt(r1)
            int r1 = r1.intValue()
            if (r1 != 0) goto L_0x01fb
            return
        L_0x01fb:
            java.lang.String r2 = "payload"
            java.lang.Object r2 = r0.get(r2)
            java.lang.String r2 = (java.lang.String) r2
            java.lang.String r3 = "nonce"
            java.lang.Object r3 = r0.get(r3)
            java.lang.String r3 = (java.lang.String) r3
            java.lang.String r4 = "callback_url"
            java.lang.Object r4 = r0.get(r4)
            java.lang.String r4 = (java.lang.String) r4
            org.telegram.tgnet.TLRPC$TL_account_getAuthorizationForm r5 = new org.telegram.tgnet.TLRPC$TL_account_getAuthorizationForm
            r5.<init>()
            r5.bot_id = r1
            java.lang.String r1 = "scope"
            java.lang.Object r1 = r0.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            r5.scope = r1
            java.lang.String r1 = "public_key"
            java.lang.Object r0 = r0.get(r1)
            java.lang.String r0 = (java.lang.String) r0
            r5.public_key = r0
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r24)
            org.telegram.ui.-$$Lambda$LaunchActivity$6XLU44Aqp3YzcUhosz_IhoLjXEU r1 = new org.telegram.ui.-$$Lambda$LaunchActivity$6XLU44Aqp3YzcUhosz_IhoLjXEU
            r25 = r1
            r26 = r23
            r27 = r7
            r28 = r24
            r29 = r11
            r30 = r5
            r31 = r2
            r32 = r3
            r33 = r4
            r25.<init>(r27, r28, r29, r30, r31, r32, r33)
            int r0 = r0.sendRequest(r5, r1)
            r7[r12] = r0
            goto L_0x0122
        L_0x0251:
            r0 = r39
            if (r0 == 0) goto L_0x026f
            org.telegram.tgnet.TLRPC$TL_help_getDeepLinkInfo r1 = new org.telegram.tgnet.TLRPC$TL_help_getDeepLinkInfo
            r1.<init>()
            r1.path = r0
            int r0 = r14.currentAccount
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$BV_AIdZJ2q_uyN40u8TT-n_w3VI r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$BV_AIdZJ2q_uyN40u8TT-n_w3VI
            r2.<init>(r11)
            int r0 = r0.sendRequest(r1, r2)
            r7[r12] = r0
            goto L_0x0122
        L_0x026f:
            java.lang.String r0 = "android"
            r1 = r38
            if (r1 == 0) goto L_0x0291
            org.telegram.tgnet.TLRPC$TL_langpack_getLanguage r2 = new org.telegram.tgnet.TLRPC$TL_langpack_getLanguage
            r2.<init>()
            r2.lang_code = r1
            r2.lang_pack = r0
            int r0 = r14.currentAccount
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$XpSS05F2CIV2lO1vS07kCnBFcqI r1 = new org.telegram.ui.-$$Lambda$LaunchActivity$XpSS05F2CIV2lO1vS07kCnBFcqI
            r1.<init>(r11)
            int r0 = r0.sendRequest(r2, r1)
            r7[r12] = r0
            goto L_0x0122
        L_0x0291:
            r1 = r42
            if (r1 == 0) goto L_0x02ee
            java.lang.String r0 = r1.slug
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x02c7
            org.telegram.ui.WallpapersListActivity$ColorWallpaper r0 = new org.telegram.ui.WallpapersListActivity$ColorWallpaper     // Catch:{ Exception -> 0x02c1 }
            java.lang.String r2 = "c"
            org.telegram.tgnet.TLRPC$WallPaperSettings r3 = r1.settings     // Catch:{ Exception -> 0x02c1 }
            int r4 = r3.background_color     // Catch:{ Exception -> 0x02c1 }
            int r5 = r3.second_background_color     // Catch:{ Exception -> 0x02c1 }
            int r3 = r3.rotation     // Catch:{ Exception -> 0x02c1 }
            int r3 = org.telegram.messenger.AndroidUtilities.getWallpaperRotation(r3, r12)     // Catch:{ Exception -> 0x02c1 }
            r0.<init>(r2, r4, r5, r3)     // Catch:{ Exception -> 0x02c1 }
            org.telegram.ui.ThemePreviewActivity r2 = new org.telegram.ui.ThemePreviewActivity     // Catch:{ Exception -> 0x02c1 }
            r4 = 0
            r2.<init>(r0, r4)     // Catch:{ Exception -> 0x02bf }
            org.telegram.ui.-$$Lambda$LaunchActivity$1ewIDSX6v3iqNHxvLs57zXfzUOk r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$1ewIDSX6v3iqNHxvLs57zXfzUOk     // Catch:{ Exception -> 0x02bf }
            r0.<init>(r2)     // Catch:{ Exception -> 0x02bf }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)     // Catch:{ Exception -> 0x02bf }
            goto L_0x02c9
        L_0x02bf:
            r0 = move-exception
            goto L_0x02c3
        L_0x02c1:
            r0 = move-exception
            r4 = 0
        L_0x02c3:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x02c8
        L_0x02c7:
            r4 = 0
        L_0x02c8:
            r8 = 0
        L_0x02c9:
            if (r8 != 0) goto L_0x03c8
            org.telegram.tgnet.TLRPC$TL_account_getWallPaper r0 = new org.telegram.tgnet.TLRPC$TL_account_getWallPaper
            r0.<init>()
            org.telegram.tgnet.TLRPC$TL_inputWallPaperSlug r2 = new org.telegram.tgnet.TLRPC$TL_inputWallPaperSlug
            r2.<init>()
            java.lang.String r3 = r1.slug
            r2.slug = r3
            r0.wallpaper = r2
            int r2 = r14.currentAccount
            org.telegram.tgnet.ConnectionsManager r2 = org.telegram.tgnet.ConnectionsManager.getInstance(r2)
            org.telegram.ui.-$$Lambda$LaunchActivity$hgWUm3vZOS6YWZiTjCASeedskVs r3 = new org.telegram.ui.-$$Lambda$LaunchActivity$hgWUm3vZOS6YWZiTjCASeedskVs
            r3.<init>(r11, r1)
            int r0 = r2.sendRequest(r0, r3)
            r7[r12] = r0
            goto L_0x03c8
        L_0x02ee:
            r4 = 0
            if (r3 == 0) goto L_0x0319
            org.telegram.ui.-$$Lambda$LaunchActivity$datiwutOOOqtyVnN6hEr0rYDv9A r15 = new org.telegram.ui.-$$Lambda$LaunchActivity$datiwutOOOqtyVnN6hEr0rYDv9A
            r15.<init>()
            org.telegram.tgnet.TLRPC$TL_account_getTheme r1 = new org.telegram.tgnet.TLRPC$TL_account_getTheme
            r1.<init>()
            r1.format = r0
            org.telegram.tgnet.TLRPC$TL_inputThemeSlug r0 = new org.telegram.tgnet.TLRPC$TL_inputThemeSlug
            r0.<init>()
            r0.slug = r3
            r1.theme = r0
            int r0 = r14.currentAccount
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$teSqgmp29W09k55dP2J5mMX2tVQ r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$teSqgmp29W09k55dP2J5mMX2tVQ
            r2.<init>(r11)
            int r0 = r0.sendRequest(r1, r2)
            r7[r12] = r0
            goto L_0x03c9
        L_0x0319:
            if (r13 == 0) goto L_0x03c8
            if (r32 == 0) goto L_0x03c8
            if (r34 == 0) goto L_0x0376
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r24)
            org.telegram.tgnet.TLRPC$Chat r0 = r0.getChat(r13)
            if (r0 == 0) goto L_0x033f
            r25 = r23
            r26 = r24
            r27 = r11
            r28 = r32
            r29 = r35
            r30 = r34
            r31 = r0
            int r0 = r25.runCommentRequest(r26, r27, r28, r29, r30, r31)
            r7[r12] = r0
            goto L_0x03c8
        L_0x033f:
            org.telegram.tgnet.TLRPC$TL_channels_getChannels r0 = new org.telegram.tgnet.TLRPC$TL_channels_getChannels
            r0.<init>()
            org.telegram.tgnet.TLRPC$TL_inputChannel r1 = new org.telegram.tgnet.TLRPC$TL_inputChannel
            r1.<init>()
            int r2 = r33.intValue()
            r1.channel_id = r2
            java.util.ArrayList<org.telegram.tgnet.TLRPC$InputChannel> r2 = r0.id
            r2.add(r1)
            int r1 = r14.currentAccount
            org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
            org.telegram.ui.-$$Lambda$LaunchActivity$KX-nzHOEQzUNbsu33IC2w_PgHdU r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$KX-nzHOEQzUNbsu33IC2w_PgHdU
            r36 = r2
            r37 = r23
            r38 = r7
            r39 = r24
            r40 = r11
            r41 = r32
            r42 = r35
            r43 = r34
            r36.<init>(r38, r39, r40, r41, r42, r43)
            int r0 = r1.sendRequest(r0, r2)
            r7[r12] = r0
            goto L_0x03c8
        L_0x0376:
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            int r1 = r33.intValue()
            java.lang.String r2 = "chat_id"
            r0.putInt(r2, r1)
            int r1 = r32.intValue()
            java.lang.String r2 = "message_id"
            r0.putInt(r2, r1)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x03a3
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            int r2 = r1.size()
            int r2 = r2 - r8
            java.lang.Object r1 = r1.get(r2)
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1
            goto L_0x03a4
        L_0x03a3:
            r1 = r4
        L_0x03a4:
            if (r1 == 0) goto L_0x03b0
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r24)
            boolean r2 = r2.checkCanOpenChat(r0, r1)
            if (r2 == 0) goto L_0x03c8
        L_0x03b0:
            org.telegram.ui.-$$Lambda$LaunchActivity$-jayKIWnQR28AcfSLJAMeeUlMYM r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$-jayKIWnQR28AcfSLJAMeeUlMYM
            r25 = r2
            r26 = r23
            r27 = r0
            r28 = r33
            r29 = r7
            r30 = r11
            r31 = r1
            r32 = r24
            r25.<init>(r27, r28, r29, r30, r31, r32)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r2)
        L_0x03c8:
            r15 = r4
        L_0x03c9:
            r0 = r7[r12]
            if (r0 == 0) goto L_0x03da
            org.telegram.ui.-$$Lambda$LaunchActivity$bixgLd95ntcdBdsNOZaC8gm-wzw r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$bixgLd95ntcdBdsNOZaC8gm-wzw
            r0.<init>(r10, r7, r15)
            r11.setOnCancelListener(r0)
            r0 = 300(0x12c, double:1.48E-321)
            r11.showDelayed(r0)     // Catch:{ Exception -> 0x03da }
        L_0x03da:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.runLinkRequest(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.String, java.util.HashMap, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.telegram.tgnet.TLRPC$TL_wallPaper, java.lang.String, int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$22 */
    public /* synthetic */ void lambda$runLinkRequest$22$LaunchActivity(int i, String str, String str2, String str3, String str4, String str5, String str6, boolean z, Integer num, Integer num2, Integer num3, Integer num4, String str7, HashMap hashMap, String str8, String str9, String str10, String str11, TLRPC$TL_wallPaper tLRPC$TL_wallPaper, String str12, int i2) {
        int i3 = i2;
        if (i3 != i) {
            switchToAccount(i3, true);
        }
        runLinkRequest(i2, str, str2, str3, str4, str5, str6, z, num, num2, num3, num4, str7, hashMap, str8, str9, str10, str11, tLRPC$TL_wallPaper, str12, 1);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$26 */
    public /* synthetic */ void lambda$runLinkRequest$26$LaunchActivity(String str, int i, Integer num, Integer num2, Integer num3, int[] iArr, AlertDialog alertDialog, String str2, String str3, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, tLRPC$TL_error, str, i, num, num2, num3, iArr, alertDialog, str2, str3) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ String f$10;
            public final /* synthetic */ String f$11;
            public final /* synthetic */ TLRPC$TL_error f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ Integer f$5;
            public final /* synthetic */ Integer f$6;
            public final /* synthetic */ Integer f$7;
            public final /* synthetic */ int[] f$8;
            public final /* synthetic */ AlertDialog f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
                this.f$11 = r12;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$25$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11);
            }
        }, 2);
    }

    /* JADX WARNING: type inference failed for: r1v11 */
    /* JADX WARNING: type inference failed for: r1v12, types: [org.telegram.ui.ActionBar.BaseFragment] */
    /* JADX WARNING: type inference failed for: r1v18, types: [org.telegram.tgnet.TLRPC$User] */
    /* JADX WARNING: type inference failed for: r1v45 */
    /* JADX WARNING: type inference failed for: r1v46 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00d3, code lost:
        if ((r1.get(r1.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00f2, code lost:
        if ((r1.get(r1.size() - 1) instanceof org.telegram.ui.DialogsActivity) != false) goto L_0x00d5;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0117  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0165  */
    /* renamed from: lambda$null$25 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$25$LaunchActivity(org.telegram.tgnet.TLObject r14, org.telegram.tgnet.TLRPC$TL_error r15, java.lang.String r16, int r17, java.lang.Integer r18, java.lang.Integer r19, java.lang.Integer r20, int[] r21, org.telegram.ui.ActionBar.AlertDialog r22, java.lang.String r23, java.lang.String r24) {
        /*
            r13 = this;
            r8 = r13
            r0 = r15
            r1 = r16
            r2 = r17
            r3 = r23
            r4 = r24
            boolean r5 = r13.isFinishing()
            if (r5 != 0) goto L_0x030a
            r5 = r14
            org.telegram.tgnet.TLRPC$TL_contacts_resolvedPeer r5 = (org.telegram.tgnet.TLRPC$TL_contacts_resolvedPeer) r5
            r9 = 1
            r10 = 0
            if (r0 != 0) goto L_0x02ac
            org.telegram.ui.ActionBar.ActionBarLayout r6 = r8.actionBarLayout
            if (r6 == 0) goto L_0x02ac
            if (r1 == 0) goto L_0x0027
            if (r1 == 0) goto L_0x02ac
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r6 = r5.users
            boolean r6 = r6.isEmpty()
            if (r6 != 0) goto L_0x02ac
        L_0x0027:
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r17)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r6 = r5.users
            r0.putUsers(r6, r10)
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r17)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r6 = r5.chats
            r0.putChats(r6, r10)
            org.telegram.messenger.MessagesStorage r0 = org.telegram.messenger.MessagesStorage.getInstance(r17)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r6 = r5.users
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r7 = r5.chats
            r0.putUsersAndChats(r6, r7, r10, r9)
            if (r18 == 0) goto L_0x0075
            if (r19 != 0) goto L_0x004a
            if (r20 == 0) goto L_0x0075
        L_0x004a:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r0 = r5.chats
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0075
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r0 = r5.chats
            java.lang.Object r0 = r0.get(r10)
            r7 = r0
            org.telegram.tgnet.TLRPC$Chat r7 = (org.telegram.tgnet.TLRPC$Chat) r7
            r1 = r13
            r2 = r17
            r3 = r22
            r4 = r18
            r5 = r19
            r6 = r20
            int r0 = r1.runCommentRequest(r2, r3, r4, r5, r6, r7)
            r21[r10] = r0
            r0 = r21[r10]
            if (r0 == 0) goto L_0x0291
            r4 = r22
        L_0x0072:
            r9 = 0
            goto L_0x02ff
        L_0x0075:
            java.lang.String r0 = "dialogsType"
            java.lang.String r6 = "onlySelect"
            if (r1 == 0) goto L_0x016c
            android.os.Bundle r3 = new android.os.Bundle
            r3.<init>()
            r3.putBoolean(r6, r9)
            java.lang.String r4 = "cantSendToChannels"
            r3.putBoolean(r4, r9)
            r3.putInt(r0, r9)
            r0 = 2131627299(0x7f0e0d23, float:1.8881859E38)
            java.lang.String r4 = "SendGameToText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r4, r0)
            java.lang.String r4 = "selectAlertString"
            r3.putString(r4, r0)
            r0 = 2131627298(0x7f0e0d22, float:1.8881856E38)
            java.lang.String r4 = "SendGameToGroupText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r4, r0)
            java.lang.String r4 = "selectAlertStringGroup"
            r3.putString(r4, r0)
            org.telegram.ui.DialogsActivity r0 = new org.telegram.ui.DialogsActivity
            r0.<init>(r3)
            org.telegram.ui.-$$Lambda$LaunchActivity$uEXWQU9sal1FAKLIQuDENvstjxE r3 = new org.telegram.ui.-$$Lambda$LaunchActivity$uEXWQU9sal1FAKLIQuDENvstjxE
            r3.<init>(r1, r2, r5)
            r0.setDelegate(r3)
            boolean r1 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r1 == 0) goto L_0x00d9
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r8.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            int r1 = r1.size()
            if (r1 <= 0) goto L_0x00d7
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r8.layersActionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            int r2 = r1.size()
            int r2 = r2 - r9
            java.lang.Object r1 = r1.get(r2)
            boolean r1 = r1 instanceof org.telegram.ui.DialogsActivity
            if (r1 == 0) goto L_0x00d7
        L_0x00d5:
            r1 = 1
            goto L_0x00f5
        L_0x00d7:
            r1 = 0
            goto L_0x00f5
        L_0x00d9:
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r8.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            int r1 = r1.size()
            if (r1 <= r9) goto L_0x00d7
            org.telegram.ui.ActionBar.ActionBarLayout r1 = r8.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = r1.fragmentsStack
            int r2 = r1.size()
            int r2 = r2 - r9
            java.lang.Object r1 = r1.get(r2)
            boolean r1 = r1 instanceof org.telegram.ui.DialogsActivity
            if (r1 == 0) goto L_0x00d7
            goto L_0x00d5
        L_0x00f5:
            org.telegram.ui.ActionBar.ActionBarLayout r2 = r8.actionBarLayout
            r3 = 1
            r4 = 1
            r5 = 0
            r14 = r2
            r15 = r0
            r16 = r1
            r17 = r3
            r18 = r4
            r19 = r5
            r14.presentFragment(r15, r16, r17, r18, r19)
            boolean r0 = org.telegram.ui.SecretMediaViewer.hasInstance()
            if (r0 == 0) goto L_0x011f
            org.telegram.ui.SecretMediaViewer r0 = org.telegram.ui.SecretMediaViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x011f
            org.telegram.ui.SecretMediaViewer r0 = org.telegram.ui.SecretMediaViewer.getInstance()
            r0.closePhoto(r10, r10)
            goto L_0x014e
        L_0x011f:
            boolean r0 = org.telegram.ui.PhotoViewer.hasInstance()
            if (r0 == 0) goto L_0x0137
            org.telegram.ui.PhotoViewer r0 = org.telegram.ui.PhotoViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x0137
            org.telegram.ui.PhotoViewer r0 = org.telegram.ui.PhotoViewer.getInstance()
            r0.closePhoto(r10, r9)
            goto L_0x014e
        L_0x0137:
            boolean r0 = org.telegram.ui.ArticleViewer.hasInstance()
            if (r0 == 0) goto L_0x014e
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            boolean r0 = r0.isVisible()
            if (r0 == 0) goto L_0x014e
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            r0.close(r10, r9)
        L_0x014e:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r8.drawerLayoutContainer
            r0.setAllowOpenDrawer(r10, r10)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x0165
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r8.actionBarLayout
            r0.showLastFragment()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r8.rightActionBarLayout
            r0.showLastFragment()
            goto L_0x0291
        L_0x0165:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r8.drawerLayoutContainer
            r0.setAllowOpenDrawer(r9, r10)
            goto L_0x0291
        L_0x016c:
            r1 = 0
            if (r3 == 0) goto L_0x01f1
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r4 = r5.users
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x017f
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r1 = r5.users
            java.lang.Object r1 = r1.get(r10)
            org.telegram.tgnet.TLRPC$User r1 = (org.telegram.tgnet.TLRPC$User) r1
        L_0x017f:
            if (r1 == 0) goto L_0x01c2
            boolean r4 = r1.bot
            if (r4 == 0) goto L_0x018a
            boolean r4 = r1.bot_nochats
            if (r4 == 0) goto L_0x018a
            goto L_0x01c2
        L_0x018a:
            android.os.Bundle r4 = new android.os.Bundle
            r4.<init>()
            r4.putBoolean(r6, r9)
            r5 = 2
            r4.putInt(r0, r5)
            r0 = 2131624227(0x7f0e0123, float:1.8875628E38)
            java.lang.Object[] r5 = new java.lang.Object[r5]
            java.lang.String r6 = org.telegram.messenger.UserObject.getUserName(r1)
            r5[r10] = r6
            java.lang.String r6 = "%1$s"
            r5[r9] = r6
            java.lang.String r6 = "AddToTheGroupAlertText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r6, r0, r5)
            java.lang.String r5 = "addToGroupAlertString"
            r4.putString(r5, r0)
            org.telegram.ui.DialogsActivity r0 = new org.telegram.ui.DialogsActivity
            r0.<init>(r4)
            org.telegram.ui.-$$Lambda$LaunchActivity$XZ49yEvInq0WX7xUd5zGpCpU3bI r4 = new org.telegram.ui.-$$Lambda$LaunchActivity$XZ49yEvInq0WX7xUd5zGpCpU3bI
            r4.<init>(r2, r1, r3)
            r0.setDelegate(r4)
            r13.lambda$runLinkRequest$41(r0)
            goto L_0x0291
        L_0x01c2:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack     // Catch:{ Exception -> 0x01ec }
            boolean r0 = r0.isEmpty()     // Catch:{ Exception -> 0x01ec }
            if (r0 != 0) goto L_0x01f0
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack     // Catch:{ Exception -> 0x01ec }
            int r1 = r0.size()     // Catch:{ Exception -> 0x01ec }
            int r1 = r1 - r9
            java.lang.Object r0 = r0.get(r1)     // Catch:{ Exception -> 0x01ec }
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0     // Catch:{ Exception -> 0x01ec }
            org.telegram.ui.Components.BulletinFactory r0 = org.telegram.ui.Components.BulletinFactory.of((org.telegram.ui.ActionBar.BaseFragment) r0)     // Catch:{ Exception -> 0x01ec }
            java.lang.String r1 = "BotCantJoinGroups"
            r2 = 2131624570(0x7f0e027a, float:1.8876323E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r2)     // Catch:{ Exception -> 0x01ec }
            org.telegram.ui.Components.Bulletin r0 = r0.createErrorBulletin(r1)     // Catch:{ Exception -> 0x01ec }
            r0.show()     // Catch:{ Exception -> 0x01ec }
            goto L_0x01f0
        L_0x01ec:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x01f0:
            return
        L_0x01f1:
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r3 = r5.chats
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x0219
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r3 = r5.chats
            java.lang.Object r3 = r3.get(r10)
            org.telegram.tgnet.TLRPC$Chat r3 = (org.telegram.tgnet.TLRPC$Chat) r3
            int r3 = r3.id
            java.lang.String r6 = "chat_id"
            r0.putInt(r6, r3)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r3 = r5.chats
            java.lang.Object r3 = r3.get(r10)
            org.telegram.tgnet.TLRPC$Chat r3 = (org.telegram.tgnet.TLRPC$Chat) r3
            int r3 = r3.id
            int r3 = -r3
            goto L_0x0233
        L_0x0219:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r3 = r5.users
            java.lang.Object r3 = r3.get(r10)
            org.telegram.tgnet.TLRPC$User r3 = (org.telegram.tgnet.TLRPC$User) r3
            int r3 = r3.id
            java.lang.String r6 = "user_id"
            r0.putInt(r6, r3)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r3 = r5.users
            java.lang.Object r3 = r3.get(r10)
            org.telegram.tgnet.TLRPC$User r3 = (org.telegram.tgnet.TLRPC$User) r3
            int r3 = r3.id
        L_0x0233:
            long r6 = (long) r3
            if (r4 == 0) goto L_0x0251
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r3 = r5.users
            int r3 = r3.size()
            if (r3 <= 0) goto L_0x0251
            java.util.ArrayList<org.telegram.tgnet.TLRPC$User> r3 = r5.users
            java.lang.Object r3 = r3.get(r10)
            org.telegram.tgnet.TLRPC$User r3 = (org.telegram.tgnet.TLRPC$User) r3
            boolean r3 = r3.bot
            if (r3 == 0) goto L_0x0251
            java.lang.String r3 = "botUser"
            r0.putString(r3, r4)
            r3 = 1
            goto L_0x0252
        L_0x0251:
            r3 = 0
        L_0x0252:
            if (r18 == 0) goto L_0x025d
            int r5 = r18.intValue()
            java.lang.String r11 = "message_id"
            r0.putInt(r11, r5)
        L_0x025d:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r5 = mainFragmentsStack
            boolean r5 = r5.isEmpty()
            if (r5 != 0) goto L_0x0272
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack
            int r5 = r1.size()
            int r5 = r5 - r9
            java.lang.Object r1 = r1.get(r5)
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1
        L_0x0272:
            if (r1 == 0) goto L_0x027e
            org.telegram.messenger.MessagesController r5 = org.telegram.messenger.MessagesController.getInstance(r17)
            boolean r5 = r5.checkCanOpenChat(r0, r1)
            if (r5 == 0) goto L_0x0291
        L_0x027e:
            if (r3 == 0) goto L_0x0294
            boolean r3 = r1 instanceof org.telegram.ui.ChatActivity
            if (r3 == 0) goto L_0x0294
            org.telegram.ui.ChatActivity r1 = (org.telegram.ui.ChatActivity) r1
            long r11 = r1.getDialogId()
            int r3 = (r11 > r6 ? 1 : (r11 == r6 ? 0 : -1))
            if (r3 != 0) goto L_0x0294
            r1.setBotUser(r4)
        L_0x0291:
            r4 = r22
            goto L_0x02ff
        L_0x0294:
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r17)
            if (r18 != 0) goto L_0x029c
            r2 = 0
            goto L_0x02a0
        L_0x029c:
            int r2 = r18.intValue()
        L_0x02a0:
            org.telegram.ui.LaunchActivity$9 r3 = new org.telegram.ui.LaunchActivity$9
            r4 = r22
            r3.<init>(r4, r0)
            r1.ensureMessagesLoaded(r6, r2, r3)
            goto L_0x0072
        L_0x02ac:
            r4 = r22
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack     // Catch:{ Exception -> 0x02fb }
            boolean r1 = r1.isEmpty()     // Catch:{ Exception -> 0x02fb }
            if (r1 != 0) goto L_0x02ff
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r1 = mainFragmentsStack     // Catch:{ Exception -> 0x02fb }
            int r2 = r1.size()     // Catch:{ Exception -> 0x02fb }
            int r2 = r2 - r9
            java.lang.Object r1 = r1.get(r2)     // Catch:{ Exception -> 0x02fb }
            org.telegram.ui.ActionBar.BaseFragment r1 = (org.telegram.ui.ActionBar.BaseFragment) r1     // Catch:{ Exception -> 0x02fb }
            if (r0 == 0) goto L_0x02e6
            java.lang.String r0 = r0.text     // Catch:{ Exception -> 0x02fb }
            if (r0 == 0) goto L_0x02e6
            java.lang.String r2 = "FLOOD_WAIT"
            boolean r0 = r0.startsWith(r2)     // Catch:{ Exception -> 0x02fb }
            if (r0 == 0) goto L_0x02e6
            org.telegram.ui.Components.BulletinFactory r0 = org.telegram.ui.Components.BulletinFactory.of((org.telegram.ui.ActionBar.BaseFragment) r1)     // Catch:{ Exception -> 0x02fb }
            java.lang.String r1 = "FloodWait"
            r2 = 2131625544(0x7f0e0648, float:1.8878299E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r2)     // Catch:{ Exception -> 0x02fb }
            org.telegram.ui.Components.Bulletin r0 = r0.createErrorBulletin(r1)     // Catch:{ Exception -> 0x02fb }
            r0.show()     // Catch:{ Exception -> 0x02fb }
            goto L_0x02ff
        L_0x02e6:
            org.telegram.ui.Components.BulletinFactory r0 = org.telegram.ui.Components.BulletinFactory.of((org.telegram.ui.ActionBar.BaseFragment) r1)     // Catch:{ Exception -> 0x02fb }
            java.lang.String r1 = "NoUsernameFound"
            r2 = 2131626284(0x7f0e092c, float:1.88798E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r2)     // Catch:{ Exception -> 0x02fb }
            org.telegram.ui.Components.Bulletin r0 = r0.createErrorBulletin(r1)     // Catch:{ Exception -> 0x02fb }
            r0.show()     // Catch:{ Exception -> 0x02fb }
            goto L_0x02ff
        L_0x02fb:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x02ff:
            if (r9 == 0) goto L_0x030a
            r22.dismiss()     // Catch:{ Exception -> 0x0305 }
            goto L_0x030a
        L_0x0305:
            r0 = move-exception
            r1 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
        L_0x030a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$25$LaunchActivity(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error, java.lang.String, int, java.lang.Integer, java.lang.Integer, java.lang.Integer, int[], org.telegram.ui.ActionBar.AlertDialog, java.lang.String, java.lang.String):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$23 */
    public /* synthetic */ void lambda$null$23$LaunchActivity(String str, int i, TLRPC$TL_contacts_resolvedPeer tLRPC$TL_contacts_resolvedPeer, DialogsActivity dialogsActivity, ArrayList arrayList, CharSequence charSequence, boolean z) {
        long longValue = ((Long) arrayList.get(0)).longValue();
        TLRPC$TL_inputMediaGame tLRPC$TL_inputMediaGame = new TLRPC$TL_inputMediaGame();
        TLRPC$TL_inputGameShortName tLRPC$TL_inputGameShortName = new TLRPC$TL_inputGameShortName();
        tLRPC$TL_inputMediaGame.id = tLRPC$TL_inputGameShortName;
        tLRPC$TL_inputGameShortName.short_name = str;
        tLRPC$TL_inputGameShortName.bot_id = MessagesController.getInstance(i).getInputUser(tLRPC$TL_contacts_resolvedPeer.users.get(0));
        int i2 = (int) longValue;
        SendMessagesHelper.getInstance(i).sendGame(MessagesController.getInstance(i).getInputPeer(i2), tLRPC$TL_inputMediaGame, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        int i3 = (int) (longValue >> 32);
        if (i2 == 0) {
            bundle.putInt("enc_id", i3);
        } else if (i2 > 0) {
            bundle.putInt("user_id", i2);
        } else if (i2 < 0) {
            bundle.putInt("chat_id", -i2);
        }
        DialogsActivity dialogsActivity2 = dialogsActivity;
        if (MessagesController.getInstance(i).checkCanOpenChat(bundle, dialogsActivity)) {
            NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            this.actionBarLayout.presentFragment(new ChatActivity(bundle), true, false, true, false);
            return;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$24 */
    public /* synthetic */ void lambda$null$24$LaunchActivity(int i, TLRPC$User tLRPC$User, String str, DialogsActivity dialogsActivity, ArrayList arrayList, CharSequence charSequence, boolean z) {
        long longValue = ((Long) arrayList.get(0)).longValue();
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        int i2 = -((int) longValue);
        bundle.putInt("chat_id", i2);
        if (!mainFragmentsStack.isEmpty()) {
            MessagesController instance = MessagesController.getInstance(i);
            ArrayList<BaseFragment> arrayList2 = mainFragmentsStack;
            if (!instance.checkCanOpenChat(bundle, arrayList2.get(arrayList2.size() - 1))) {
                return;
            }
        }
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        MessagesController.getInstance(i).addUserToChat(i2, tLRPC$User, 0, str, (BaseFragment) null, (Runnable) null);
        this.actionBarLayout.presentFragment(new ChatActivity(bundle), true, false, true, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$29 */
    public /* synthetic */ void lambda$runLinkRequest$29$LaunchActivity(int i, AlertDialog alertDialog, String str, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject, i, alertDialog, str) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ AlertDialog f$4;
            public final /* synthetic */ String f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$28$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        if (r8.chat.has_geo != false) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0079, code lost:
        if (r10.checkCanOpenChat(r7, r11.get(r11.size() - 1)) != false) goto L_0x007b;
     */
    /* renamed from: lambda$null$28 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$28$LaunchActivity(org.telegram.tgnet.TLRPC$TL_error r10, org.telegram.tgnet.TLObject r11, int r12, org.telegram.ui.ActionBar.AlertDialog r13, java.lang.String r14) {
        /*
            r9 = this;
            boolean r0 = r9.isFinishing()
            if (r0 != 0) goto L_0x0123
            r0 = 0
            r1 = 1
            if (r10 != 0) goto L_0x00b3
            org.telegram.ui.ActionBar.ActionBarLayout r2 = r9.actionBarLayout
            if (r2 == 0) goto L_0x00b3
            r8 = r11
            org.telegram.tgnet.TLRPC$ChatInvite r8 = (org.telegram.tgnet.TLRPC$ChatInvite) r8
            org.telegram.tgnet.TLRPC$Chat r10 = r8.chat
            if (r10 == 0) goto L_0x009d
            boolean r10 = org.telegram.messenger.ChatObject.isLeftFromChat(r10)
            if (r10 == 0) goto L_0x0033
            org.telegram.tgnet.TLRPC$Chat r10 = r8.chat
            boolean r11 = r10.kicked
            if (r11 != 0) goto L_0x009d
            java.lang.String r10 = r10.username
            boolean r10 = android.text.TextUtils.isEmpty(r10)
            if (r10 == 0) goto L_0x0033
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_chatInvitePeek
            if (r10 != 0) goto L_0x0033
            org.telegram.tgnet.TLRPC$Chat r10 = r8.chat
            boolean r10 = r10.has_geo
            if (r10 == 0) goto L_0x009d
        L_0x0033:
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r12)
            org.telegram.tgnet.TLRPC$Chat r11 = r8.chat
            r14 = 0
            r10.putChat(r11, r14)
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            org.telegram.tgnet.TLRPC$Chat r11 = r8.chat
            r10.add(r11)
            org.telegram.messenger.MessagesStorage r11 = org.telegram.messenger.MessagesStorage.getInstance(r12)
            r11.putUsersAndChats(r0, r10, r14, r1)
            android.os.Bundle r7 = new android.os.Bundle
            r7.<init>()
            org.telegram.tgnet.TLRPC$Chat r10 = r8.chat
            int r10 = r10.id
            java.lang.String r11 = "chat_id"
            r7.putInt(r11, r10)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = mainFragmentsStack
            boolean r10 = r10.isEmpty()
            if (r10 != 0) goto L_0x007b
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r12)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r11 = mainFragmentsStack
            int r0 = r11.size()
            int r0 = r0 - r1
            java.lang.Object r11 = r11.get(r0)
            org.telegram.ui.ActionBar.BaseFragment r11 = (org.telegram.ui.ActionBar.BaseFragment) r11
            boolean r10 = r10.checkCanOpenChat(r7, r11)
            if (r10 == 0) goto L_0x0119
        L_0x007b:
            boolean[] r6 = new boolean[r1]
            org.telegram.ui.-$$Lambda$LaunchActivity$_hA3_aEzWRFWZyU2V9yxBtC0oqk r10 = new org.telegram.ui.-$$Lambda$LaunchActivity$_hA3_aEzWRFWZyU2V9yxBtC0oqk
            r10.<init>(r6)
            r13.setOnCancelListener(r10)
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r12)
            org.telegram.tgnet.TLRPC$Chat r11 = r8.chat
            int r11 = r11.id
            int r11 = -r11
            long r11 = (long) r11
            org.telegram.ui.LaunchActivity$10 r0 = new org.telegram.ui.LaunchActivity$10
            r3 = r0
            r4 = r9
            r5 = r13
            r3.<init>(r5, r6, r7, r8)
            r10.ensureMessagesLoaded(r11, r14, r0)
            r1 = 0
            goto L_0x0119
        L_0x009d:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r10 = mainFragmentsStack
            int r11 = r10.size()
            int r11 = r11 - r1
            java.lang.Object r10 = r10.get(r11)
            org.telegram.ui.ActionBar.BaseFragment r10 = (org.telegram.ui.ActionBar.BaseFragment) r10
            org.telegram.ui.Components.JoinGroupAlert r11 = new org.telegram.ui.Components.JoinGroupAlert
            r11.<init>(r9, r8, r14, r10)
            r10.showDialog(r11)
            goto L_0x0119
        L_0x00b3:
            org.telegram.ui.ActionBar.AlertDialog$Builder r11 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r11.<init>((android.content.Context) r9)
            r12 = 2131624280(0x7f0e0158, float:1.8875735E38)
            java.lang.String r14 = "AppName"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r11.setTitle(r12)
            java.lang.String r12 = r10.text
            java.lang.String r14 = "FLOOD_WAIT"
            boolean r12 = r12.startsWith(r14)
            if (r12 == 0) goto L_0x00db
            r10 = 2131625544(0x7f0e0648, float:1.8878299E38)
            java.lang.String r12 = "FloodWait"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r12, r10)
            r11.setMessage(r10)
            goto L_0x010a
        L_0x00db:
            java.lang.String r10 = r10.text
            java.lang.String r12 = "INVITE_HASH_EXPIRED"
            boolean r10 = r10.startsWith(r12)
            if (r10 == 0) goto L_0x00fe
            r10 = 2131625431(0x7f0e05d7, float:1.887807E38)
            java.lang.String r12 = "ExpiredLink"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r12, r10)
            r11.setTitle(r10)
            r10 = 2131625819(0x7f0e075b, float:1.8878857E38)
            java.lang.String r12 = "InviteExpired"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r12, r10)
            r11.setMessage(r10)
            goto L_0x010a
        L_0x00fe:
            r10 = 2131625862(0x7f0e0786, float:1.8878944E38)
            java.lang.String r12 = "JoinToGroupErrorNotExist"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r12, r10)
            r11.setMessage(r10)
        L_0x010a:
            r10 = 2131626473(0x7f0e09e9, float:1.8880183E38)
            java.lang.String r12 = "OK"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r12, r10)
            r11.setPositiveButton(r10, r0)
            r9.showAlertDialog(r11)
        L_0x0119:
            if (r1 == 0) goto L_0x0123
            r13.dismiss()     // Catch:{ Exception -> 0x011f }
            goto L_0x0123
        L_0x011f:
            r10 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r10)
        L_0x0123:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$28$LaunchActivity(org.telegram.tgnet.TLRPC$TL_error, org.telegram.tgnet.TLObject, int, org.telegram.ui.ActionBar.AlertDialog, java.lang.String):void");
    }

    static /* synthetic */ void lambda$null$27(boolean[] zArr, DialogInterface dialogInterface) {
        zArr[0] = true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$31 */
    public /* synthetic */ void lambda$runLinkRequest$31$LaunchActivity(int i, AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLRPC$TL_error == null) {
            MessagesController.getInstance(i).processUpdates((TLRPC$Updates) tLObject, false);
        }
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLRPC$TL_error, tLObject, i) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLRPC$TL_error f$2;
            public final /* synthetic */ TLObject f$3;
            public final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$30$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$30 */
    public /* synthetic */ void lambda$null$30$LaunchActivity(AlertDialog alertDialog, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, int i) {
        if (!isFinishing()) {
            try {
                alertDialog.dismiss();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            if (tLRPC$TL_error != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", NUM));
                if (tLRPC$TL_error.text.startsWith("FLOOD_WAIT")) {
                    builder.setMessage(LocaleController.getString("FloodWait", NUM));
                } else if (tLRPC$TL_error.text.equals("USERS_TOO_MUCH")) {
                    builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", NUM));
                } else {
                    builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", NUM));
                }
                builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
                showAlertDialog(builder);
            } else if (this.actionBarLayout != null) {
                TLRPC$Updates tLRPC$Updates = (TLRPC$Updates) tLObject;
                if (!tLRPC$Updates.chats.isEmpty()) {
                    TLRPC$Chat tLRPC$Chat = tLRPC$Updates.chats.get(0);
                    tLRPC$Chat.left = false;
                    tLRPC$Chat.kicked = false;
                    MessagesController.getInstance(i).putUsers(tLRPC$Updates.users, false);
                    MessagesController.getInstance(i).putChats(tLRPC$Updates.chats, false);
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id", tLRPC$Chat.id);
                    if (!mainFragmentsStack.isEmpty()) {
                        MessagesController instance = MessagesController.getInstance(i);
                        ArrayList<BaseFragment> arrayList = mainFragmentsStack;
                        if (!instance.checkCanOpenChat(bundle, arrayList.get(arrayList.size() - 1))) {
                            return;
                        }
                    }
                    ChatActivity chatActivity = new ChatActivity(bundle);
                    NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                    this.actionBarLayout.presentFragment(chatActivity, false, true, true, false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$32 */
    public /* synthetic */ void lambda$runLinkRequest$32$LaunchActivity(boolean z, int i, String str, DialogsActivity dialogsActivity, ArrayList arrayList, CharSequence charSequence, boolean z2) {
        ArrayList arrayList2 = arrayList;
        long longValue = ((Long) arrayList.get(0)).longValue();
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        boolean z3 = z;
        bundle.putBoolean("hasUrl", z);
        int i2 = (int) longValue;
        int i3 = (int) (longValue >> 32);
        if (i2 == 0) {
            bundle.putInt("enc_id", i3);
        } else if (i2 > 0) {
            bundle.putInt("user_id", i2);
        } else if (i2 < 0) {
            bundle.putInt("chat_id", -i2);
        }
        DialogsActivity dialogsActivity2 = dialogsActivity;
        if (MessagesController.getInstance(i).checkCanOpenChat(bundle, dialogsActivity)) {
            NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            MediaDataController.getInstance(i).saveDraft(longValue, 0, str, (ArrayList<TLRPC$MessageEntity>) null, (TLRPC$Message) null, false);
            this.actionBarLayout.presentFragment(new ChatActivity(bundle), true, false, true, false);
            return;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$36 */
    public /* synthetic */ void lambda$runLinkRequest$36$LaunchActivity(int[] iArr, int i, AlertDialog alertDialog, TLRPC$TL_account_getAuthorizationForm tLRPC$TL_account_getAuthorizationForm, String str, String str2, String str3, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        TLRPC$TL_account_authorizationForm tLRPC$TL_account_authorizationForm = (TLRPC$TL_account_authorizationForm) tLObject;
        if (tLRPC$TL_account_authorizationForm != null) {
            iArr[0] = ConnectionsManager.getInstance(i).sendRequest(new TLRPC$TL_account_getPassword(), new RequestDelegate(alertDialog, i, tLRPC$TL_account_authorizationForm, tLRPC$TL_account_getAuthorizationForm, str, str2, str3) {
                public final /* synthetic */ AlertDialog f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ TLRPC$TL_account_authorizationForm f$3;
                public final /* synthetic */ TLRPC$TL_account_getAuthorizationForm f$4;
                public final /* synthetic */ String f$5;
                public final /* synthetic */ String f$6;
                public final /* synthetic */ String f$7;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                    this.f$7 = r8;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    LaunchActivity.this.lambda$null$34$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, tLObject, tLRPC$TL_error);
                }
            });
            return;
        }
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLRPC$TL_error) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLRPC$TL_error f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$35$LaunchActivity(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$34 */
    public /* synthetic */ void lambda$null$34$LaunchActivity(AlertDialog alertDialog, int i, TLRPC$TL_account_authorizationForm tLRPC$TL_account_authorizationForm, TLRPC$TL_account_getAuthorizationForm tLRPC$TL_account_getAuthorizationForm, String str, String str2, String str3, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLObject, i, tLRPC$TL_account_authorizationForm, tLRPC$TL_account_getAuthorizationForm, str, str2, str3) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ TLRPC$TL_account_authorizationForm f$4;
            public final /* synthetic */ TLRPC$TL_account_getAuthorizationForm f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ String f$7;
            public final /* synthetic */ String f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$33$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$33 */
    public /* synthetic */ void lambda$null$33$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, int i, TLRPC$TL_account_authorizationForm tLRPC$TL_account_authorizationForm, TLRPC$TL_account_getAuthorizationForm tLRPC$TL_account_getAuthorizationForm, String str, String str2, String str3) {
        TLRPC$TL_account_getAuthorizationForm tLRPC$TL_account_getAuthorizationForm2 = tLRPC$TL_account_getAuthorizationForm;
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        if (tLObject != null) {
            MessagesController.getInstance(i).putUsers(tLRPC$TL_account_authorizationForm.users, false);
            lambda$runLinkRequest$41(new PassportActivity(5, tLRPC$TL_account_getAuthorizationForm2.bot_id, tLRPC$TL_account_getAuthorizationForm2.scope, tLRPC$TL_account_getAuthorizationForm2.public_key, str, str2, str3, tLRPC$TL_account_authorizationForm, (TLRPC$TL_account_password) tLObject));
            return;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$35 */
    public /* synthetic */ void lambda$null$35$LaunchActivity(AlertDialog alertDialog, TLRPC$TL_error tLRPC$TL_error) {
        try {
            alertDialog.dismiss();
            if ("APP_VERSION_OUTDATED".equals(tLRPC$TL_error.text)) {
                AlertsCreator.showUpdateAppAlert(this, LocaleController.getString("UpdateAppAlert", NUM), true);
                return;
            }
            showAlertDialog(AlertsCreator.createSimpleAlert(this, LocaleController.getString("ErrorOccurred", NUM) + "\n" + tLRPC$TL_error.text));
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$38 */
    public /* synthetic */ void lambda$runLinkRequest$38$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLObject) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLObject f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$37$LaunchActivity(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$37 */
    public /* synthetic */ void lambda$null$37$LaunchActivity(AlertDialog alertDialog, TLObject tLObject) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        if (tLObject instanceof TLRPC$TL_help_deepLinkInfo) {
            TLRPC$TL_help_deepLinkInfo tLRPC$TL_help_deepLinkInfo = (TLRPC$TL_help_deepLinkInfo) tLObject;
            AlertsCreator.showUpdateAppAlert(this, tLRPC$TL_help_deepLinkInfo.message, tLRPC$TL_help_deepLinkInfo.update_app);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$40 */
    public /* synthetic */ void lambda$runLinkRequest$40$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLObject, tLRPC$TL_error) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ TLRPC$TL_error f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$39$LaunchActivity(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$39 */
    public /* synthetic */ void lambda$null$39$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        if (tLObject instanceof TLRPC$TL_langPackLanguage) {
            showAlertDialog(AlertsCreator.createLanguageAlert(this, (TLRPC$TL_langPackLanguage) tLObject));
        } else if (tLRPC$TL_error == null) {
        } else {
            if ("LANG_CODE_NOT_SUPPORTED".equals(tLRPC$TL_error.text)) {
                showAlertDialog(AlertsCreator.createSimpleAlert(this, LocaleController.getString("LanguageUnsupportedError", NUM)));
                return;
            }
            showAlertDialog(AlertsCreator.createSimpleAlert(this, LocaleController.getString("ErrorOccurred", NUM) + "\n" + tLRPC$TL_error.text));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$43 */
    public /* synthetic */ void lambda$runLinkRequest$43$LaunchActivity(AlertDialog alertDialog, TLRPC$TL_wallPaper tLRPC$TL_wallPaper, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLObject, tLRPC$TL_wallPaper, tLRPC$TL_error) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ TLRPC$TL_wallPaper f$3;
            public final /* synthetic */ TLRPC$TL_error f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$42$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v5, resolved type: org.telegram.tgnet.TLRPC$TL_wallPaper} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v6, resolved type: org.telegram.tgnet.TLRPC$TL_wallPaper} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: org.telegram.ui.WallpapersListActivity$ColorWallpaper} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v9, resolved type: org.telegram.tgnet.TLRPC$TL_wallPaper} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* renamed from: lambda$null$42 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$42$LaunchActivity(org.telegram.ui.ActionBar.AlertDialog r9, org.telegram.tgnet.TLObject r10, org.telegram.tgnet.TLRPC$TL_wallPaper r11, org.telegram.tgnet.TLRPC$TL_error r12) {
        /*
            r8 = this;
            r9.dismiss()     // Catch:{ Exception -> 0x0004 }
            goto L_0x0008
        L_0x0004:
            r9 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r9)
        L_0x0008:
            boolean r9 = r10 instanceof org.telegram.tgnet.TLRPC$TL_wallPaper
            if (r9 == 0) goto L_0x0049
            org.telegram.tgnet.TLRPC$TL_wallPaper r10 = (org.telegram.tgnet.TLRPC$TL_wallPaper) r10
            boolean r9 = r10.pattern
            if (r9 == 0) goto L_0x0036
            org.telegram.ui.WallpapersListActivity$ColorWallpaper r9 = new org.telegram.ui.WallpapersListActivity$ColorWallpaper
            java.lang.String r1 = r10.slug
            org.telegram.tgnet.TLRPC$WallPaperSettings r12 = r11.settings
            int r2 = r12.background_color
            int r3 = r12.second_background_color
            int r12 = r12.rotation
            r0 = 0
            int r4 = org.telegram.messenger.AndroidUtilities.getWallpaperRotation(r12, r0)
            org.telegram.tgnet.TLRPC$WallPaperSettings r12 = r11.settings
            int r0 = r12.intensity
            float r0 = (float) r0
            r5 = 1120403456(0x42CLASSNAME, float:100.0)
            float r5 = r0 / r5
            boolean r6 = r12.motion
            r7 = 0
            r0 = r9
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            r9.pattern = r10
            r10 = r9
        L_0x0036:
            org.telegram.ui.ThemePreviewActivity r9 = new org.telegram.ui.ThemePreviewActivity
            r12 = 0
            r9.<init>(r10, r12)
            org.telegram.tgnet.TLRPC$WallPaperSettings r10 = r11.settings
            boolean r11 = r10.blur
            boolean r10 = r10.motion
            r9.setInitialModes(r11, r10)
            r8.lambda$runLinkRequest$41(r9)
            goto L_0x006f
        L_0x0049:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r10 = 2131625308(0x7f0e055c, float:1.887782E38)
            java.lang.String r11 = "ErrorOccurred"
            java.lang.String r10 = org.telegram.messenger.LocaleController.getString(r11, r10)
            r9.append(r10)
            java.lang.String r10 = "\n"
            r9.append(r10)
            java.lang.String r10 = r12.text
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            org.telegram.ui.ActionBar.AlertDialog$Builder r9 = org.telegram.ui.Components.AlertsCreator.createSimpleAlert(r8, r9)
            r8.showAlertDialog(r9)
        L_0x006f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$42$LaunchActivity(org.telegram.ui.ActionBar.AlertDialog, org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_wallPaper, org.telegram.tgnet.TLRPC$TL_error):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$44 */
    public /* synthetic */ void lambda$runLinkRequest$44$LaunchActivity() {
        this.loadingThemeFileName = null;
        this.loadingThemeWallpaperName = null;
        this.loadingThemeWallpaper = null;
        this.loadingThemeInfo = null;
        this.loadingThemeProgressDialog = null;
        this.loadingTheme = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$46 */
    public /* synthetic */ void lambda$runLinkRequest$46$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, alertDialog, tLRPC$TL_error) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ AlertDialog f$2;
            public final /* synthetic */ TLRPC$TL_error f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$45$LaunchActivity(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0086 A[SYNTHETIC, Splitter:B:27:0x0086] */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    /* renamed from: lambda$null$45 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$45$LaunchActivity(org.telegram.tgnet.TLObject r5, org.telegram.ui.ActionBar.AlertDialog r6, org.telegram.tgnet.TLRPC$TL_error r7) {
        /*
            r4 = this;
            boolean r0 = r5 instanceof org.telegram.tgnet.TLRPC$TL_theme
            r1 = 1
            if (r0 == 0) goto L_0x0075
            org.telegram.tgnet.TLRPC$TL_theme r5 = (org.telegram.tgnet.TLRPC$TL_theme) r5
            org.telegram.tgnet.TLRPC$TL_themeSettings r7 = r5.settings
            r0 = 0
            if (r7 == 0) goto L_0x0057
            java.lang.String r7 = org.telegram.ui.ActionBar.Theme.getBaseThemeKey(r7)
            org.telegram.ui.ActionBar.Theme$ThemeInfo r7 = org.telegram.ui.ActionBar.Theme.getTheme(r7)
            if (r7 == 0) goto L_0x0081
            org.telegram.tgnet.TLRPC$TL_themeSettings r2 = r5.settings
            org.telegram.tgnet.TLRPC$WallPaper r2 = r2.wallpaper
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_wallPaper
            if (r3 == 0) goto L_0x004a
            org.telegram.tgnet.TLRPC$TL_wallPaper r2 = (org.telegram.tgnet.TLRPC$TL_wallPaper) r2
            org.telegram.tgnet.TLRPC$Document r3 = r2.document
            java.io.File r3 = org.telegram.messenger.FileLoader.getPathToAttach(r3, r1)
            boolean r3 = r3.exists()
            if (r3 != 0) goto L_0x004b
            r4.loadingThemeProgressDialog = r6
            r4.loadingThemeAccent = r1
            r4.loadingThemeInfo = r7
            r4.loadingTheme = r5
            r4.loadingThemeWallpaper = r2
            org.telegram.tgnet.TLRPC$Document r5 = r2.document
            java.lang.String r5 = org.telegram.messenger.FileLoader.getAttachFileName(r5)
            r4.loadingThemeWallpaperName = r5
            int r5 = r4.currentAccount
            org.telegram.messenger.FileLoader r5 = org.telegram.messenger.FileLoader.getInstance(r5)
            org.telegram.tgnet.TLRPC$Document r6 = r2.document
            r5.loadFile(r6, r2, r1, r1)
            return
        L_0x004a:
            r2 = 0
        L_0x004b:
            r6.dismiss()     // Catch:{ Exception -> 0x004f }
            goto L_0x0053
        L_0x004f:
            r3 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r3)
        L_0x0053:
            r4.openThemeAccentPreview(r5, r2, r7)
            goto L_0x0084
        L_0x0057:
            org.telegram.tgnet.TLRPC$Document r7 = r5.document
            if (r7 == 0) goto L_0x0081
            r4.loadingThemeAccent = r0
            r4.loadingTheme = r5
            java.lang.String r7 = org.telegram.messenger.FileLoader.getAttachFileName(r7)
            r4.loadingThemeFileName = r7
            r4.loadingThemeProgressDialog = r6
            int r7 = r4.currentAccount
            org.telegram.messenger.FileLoader r7 = org.telegram.messenger.FileLoader.getInstance(r7)
            org.telegram.tgnet.TLRPC$TL_theme r2 = r4.loadingTheme
            org.telegram.tgnet.TLRPC$Document r2 = r2.document
            r7.loadFile(r2, r5, r1, r1)
            goto L_0x0084
        L_0x0075:
            if (r7 == 0) goto L_0x0083
            java.lang.String r5 = r7.text
            java.lang.String r7 = "THEME_FORMAT_INVALID"
            boolean r5 = r7.equals(r5)
            if (r5 == 0) goto L_0x0083
        L_0x0081:
            r0 = 1
            goto L_0x0084
        L_0x0083:
            r0 = 2
        L_0x0084:
            if (r0 == 0) goto L_0x00be
            r6.dismiss()     // Catch:{ Exception -> 0x008a }
            goto L_0x008e
        L_0x008a:
            r5 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r5)
        L_0x008e:
            r5 = 2131627609(0x7f0e0e59, float:1.8882487E38)
            java.lang.String r6 = "Theme"
            if (r0 != r1) goto L_0x00aa
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r6 = 2131627629(0x7f0e0e6d, float:1.8882528E38)
            java.lang.String r7 = "ThemeNotSupported"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            org.telegram.ui.ActionBar.AlertDialog$Builder r5 = org.telegram.ui.Components.AlertsCreator.createSimpleAlert(r4, r5, r6)
            r4.showAlertDialog(r5)
            goto L_0x00be
        L_0x00aa:
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r6, r5)
            r6 = 2131627628(0x7f0e0e6c, float:1.8882526E38)
            java.lang.String r7 = "ThemeNotFound"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            org.telegram.ui.ActionBar.AlertDialog$Builder r5 = org.telegram.ui.Components.AlertsCreator.createSimpleAlert(r4, r5, r6)
            r4.showAlertDialog(r5)
        L_0x00be:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$45$LaunchActivity(org.telegram.tgnet.TLObject, org.telegram.ui.ActionBar.AlertDialog, org.telegram.tgnet.TLRPC$TL_error):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$48 */
    public /* synthetic */ void lambda$runLinkRequest$48$LaunchActivity(int[] iArr, int i, AlertDialog alertDialog, Integer num, Integer num2, Integer num3, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, iArr, i, alertDialog, num, num2, num3) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ int[] f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ AlertDialog f$4;
            public final /* synthetic */ Integer f$5;
            public final /* synthetic */ Integer f$6;
            public final /* synthetic */ Integer f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$47$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0037 A[SYNTHETIC, Splitter:B:7:0x0037] */
    /* renamed from: lambda$null$47 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$null$47$LaunchActivity(org.telegram.tgnet.TLObject r11, int[] r12, int r13, org.telegram.ui.ActionBar.AlertDialog r14, java.lang.Integer r15, java.lang.Integer r16, java.lang.Integer r17) {
        /*
            r10 = this;
            r8 = r10
            r0 = r11
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messages_chats
            r9 = 0
            if (r1 == 0) goto L_0x0034
            org.telegram.tgnet.TLRPC$TL_messages_chats r0 = (org.telegram.tgnet.TLRPC$TL_messages_chats) r0
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r1 = r0.chats
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x0034
            int r1 = r8.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r2 = r0.chats
            r1.putChats(r2, r9)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Chat> r0 = r0.chats
            java.lang.Object r0 = r0.get(r9)
            r7 = r0
            org.telegram.tgnet.TLRPC$Chat r7 = (org.telegram.tgnet.TLRPC$Chat) r7
            r1 = r10
            r2 = r13
            r3 = r14
            r4 = r15
            r5 = r16
            r6 = r17
            int r0 = r1.runCommentRequest(r2, r3, r4, r5, r6, r7)
            r12[r9] = r0
            goto L_0x0035
        L_0x0034:
            r9 = 1
        L_0x0035:
            if (r9 == 0) goto L_0x0050
            r14.dismiss()     // Catch:{ Exception -> 0x003b }
            goto L_0x0040
        L_0x003b:
            r0 = move-exception
            r1 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
        L_0x0040:
            r0 = 2131625939(0x7f0e07d3, float:1.88791E38)
            java.lang.String r1 = "LinkNotFound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = org.telegram.ui.Components.AlertsCreator.createSimpleAlert(r10, r0)
            r10.showAlertDialog(r0)
        L_0x0050:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$null$47$LaunchActivity(org.telegram.tgnet.TLObject, int[], int, org.telegram.ui.ActionBar.AlertDialog, java.lang.Integer, java.lang.Integer, java.lang.Integer):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runLinkRequest$51 */
    public /* synthetic */ void lambda$runLinkRequest$51$LaunchActivity(Bundle bundle, Integer num, int[] iArr, AlertDialog alertDialog, BaseFragment baseFragment, int i) {
        if (!this.actionBarLayout.presentFragment(new ChatActivity(bundle))) {
            TLRPC$TL_channels_getChannels tLRPC$TL_channels_getChannels = new TLRPC$TL_channels_getChannels();
            TLRPC$TL_inputChannel tLRPC$TL_inputChannel = new TLRPC$TL_inputChannel();
            tLRPC$TL_inputChannel.channel_id = num.intValue();
            tLRPC$TL_channels_getChannels.id.add(tLRPC$TL_inputChannel);
            iArr[0] = ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_channels_getChannels, new RequestDelegate(alertDialog, baseFragment, i, bundle) {
                public final /* synthetic */ AlertDialog f$1;
                public final /* synthetic */ BaseFragment f$2;
                public final /* synthetic */ int f$3;
                public final /* synthetic */ Bundle f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    LaunchActivity.this.lambda$null$50$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, tLObject, tLRPC$TL_error);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$50 */
    public /* synthetic */ void lambda$null$50$LaunchActivity(AlertDialog alertDialog, BaseFragment baseFragment, int i, Bundle bundle, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(alertDialog, tLObject, baseFragment, i, bundle) {
            public final /* synthetic */ AlertDialog f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ BaseFragment f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ Bundle f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$49$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$49 */
    public /* synthetic */ void lambda$null$49$LaunchActivity(AlertDialog alertDialog, TLObject tLObject, BaseFragment baseFragment, int i, Bundle bundle) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        boolean z = true;
        if (tLObject instanceof TLRPC$TL_messages_chats) {
            TLRPC$TL_messages_chats tLRPC$TL_messages_chats = (TLRPC$TL_messages_chats) tLObject;
            if (!tLRPC$TL_messages_chats.chats.isEmpty()) {
                MessagesController.getInstance(this.currentAccount).putChats(tLRPC$TL_messages_chats.chats, false);
                TLRPC$Chat tLRPC$Chat = tLRPC$TL_messages_chats.chats.get(0);
                if (baseFragment == null || MessagesController.getInstance(i).checkCanOpenChat(bundle, baseFragment)) {
                    this.actionBarLayout.presentFragment(new ChatActivity(bundle));
                }
                z = false;
            }
        }
        if (z) {
            showAlertDialog(AlertsCreator.createSimpleAlert(this, LocaleController.getString("LinkNotFound", NUM)));
        }
    }

    static /* synthetic */ void lambda$runLinkRequest$52(int i, int[] iArr, Runnable runnable, DialogInterface dialogInterface) {
        ConnectionsManager.getInstance(i).cancelRequest(iArr[0], true);
        if (runnable != null) {
            runnable.run();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0075  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.List<org.telegram.tgnet.TLRPC$TL_contact> findContacts(java.lang.String r18, java.lang.String r19, boolean r20) {
        /*
            r17 = this;
            r0 = r17
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r1 = org.telegram.messenger.MessagesController.getInstance(r1)
            int r2 = r0.currentAccount
            org.telegram.messenger.ContactsController r2 = org.telegram.messenger.ContactsController.getInstance(r2)
            java.util.ArrayList r3 = new java.util.ArrayList
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_contact> r4 = r2.contacts
            r3.<init>(r4)
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            r5 = 0
            r6 = 0
            if (r19 == 0) goto L_0x005d
            java.lang.String r7 = org.telegram.PhoneFormat.PhoneFormat.stripExceptNumbers(r19)
            java.util.HashMap<java.lang.String, org.telegram.tgnet.TLRPC$TL_contact> r8 = r2.contactsByPhone
            java.lang.Object r8 = r8.get(r7)
            org.telegram.tgnet.TLRPC$TL_contact r8 = (org.telegram.tgnet.TLRPC$TL_contact) r8
            if (r8 != 0) goto L_0x0043
            int r8 = r7.length()
            int r8 = r8 + -7
            int r8 = java.lang.Math.max(r6, r8)
            java.lang.String r7 = r7.substring(r8)
            java.util.HashMap<java.lang.String, org.telegram.tgnet.TLRPC$TL_contact> r2 = r2.contactsByShortPhone
            java.lang.Object r2 = r2.get(r7)
            r8 = r2
            org.telegram.tgnet.TLRPC$TL_contact r8 = (org.telegram.tgnet.TLRPC$TL_contact) r8
        L_0x0043:
            if (r8 == 0) goto L_0x005d
            int r2 = r8.user_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r2 = r1.getUser(r2)
            if (r2 == 0) goto L_0x005b
            boolean r2 = r2.self
            if (r2 == 0) goto L_0x0057
            if (r20 == 0) goto L_0x005b
        L_0x0057:
            r4.add(r8)
            goto L_0x005d
        L_0x005b:
            r2 = r5
            goto L_0x005f
        L_0x005d:
            r2 = r18
        L_0x005f:
            boolean r7 = r4.isEmpty()
            if (r7 == 0) goto L_0x0163
            if (r2 == 0) goto L_0x0163
            java.lang.String r2 = r2.trim()
            java.lang.String r2 = r2.toLowerCase()
            boolean r7 = android.text.TextUtils.isEmpty(r2)
            if (r7 != 0) goto L_0x0163
            org.telegram.messenger.LocaleController r7 = org.telegram.messenger.LocaleController.getInstance()
            java.lang.String r7 = r7.getTranslitString(r2)
            boolean r8 = r2.equals(r7)
            if (r8 != 0) goto L_0x0089
            int r8 = r7.length()
            if (r8 != 0) goto L_0x008a
        L_0x0089:
            r7 = r5
        L_0x008a:
            r8 = 2
            java.lang.String[] r9 = new java.lang.String[r8]
            r9[r6] = r2
            r2 = 1
            r9[r2] = r7
            int r7 = r3.size()
            r10 = 0
        L_0x0097:
            if (r10 >= r7) goto L_0x0163
            java.lang.Object r11 = r3.get(r10)
            org.telegram.tgnet.TLRPC$TL_contact r11 = (org.telegram.tgnet.TLRPC$TL_contact) r11
            if (r11 == 0) goto L_0x0159
            int r12 = r11.user_id
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)
            org.telegram.tgnet.TLRPC$User r12 = r1.getUser(r12)
            if (r12 == 0) goto L_0x0159
            boolean r13 = r12.self
            if (r13 == 0) goto L_0x00b5
            if (r20 != 0) goto L_0x00b5
            goto L_0x0159
        L_0x00b5:
            r13 = 3
            java.lang.String[] r14 = new java.lang.String[r13]
            java.lang.String r15 = r12.first_name
            java.lang.String r13 = r12.last_name
            java.lang.String r13 = org.telegram.messenger.ContactsController.formatName(r15, r13)
            java.lang.String r13 = r13.toLowerCase()
            r14[r6] = r13
            org.telegram.messenger.LocaleController r13 = org.telegram.messenger.LocaleController.getInstance()
            r15 = r14[r6]
            java.lang.String r13 = r13.getTranslitString(r15)
            r14[r2] = r13
            r13 = r14[r6]
            r15 = r14[r2]
            boolean r13 = r13.equals(r15)
            if (r13 == 0) goto L_0x00de
            r14[r2] = r5
        L_0x00de:
            boolean r13 = org.telegram.messenger.UserObject.isReplyUser((org.telegram.tgnet.TLRPC$User) r12)
            if (r13 == 0) goto L_0x00f4
            r13 = 2131627098(0x7f0e0c5a, float:1.888145E38)
            java.lang.String r15 = "RepliesTitle"
            java.lang.String r13 = org.telegram.messenger.LocaleController.getString(r15, r13)
            java.lang.String r13 = r13.toLowerCase()
            r14[r8] = r13
            goto L_0x0107
        L_0x00f4:
            boolean r13 = r12.self
            if (r13 == 0) goto L_0x0107
            r13 = 2131627209(0x7f0e0cc9, float:1.8881676E38)
            java.lang.String r15 = "SavedMessages"
            java.lang.String r13 = org.telegram.messenger.LocaleController.getString(r15, r13)
            java.lang.String r13 = r13.toLowerCase()
            r14[r8] = r13
        L_0x0107:
            r13 = 0
            r15 = 0
        L_0x0109:
            if (r13 >= r8) goto L_0x0159
            r2 = r9[r13]
            if (r2 != 0) goto L_0x0110
            goto L_0x0150
        L_0x0110:
            r5 = 3
        L_0x0111:
            if (r6 >= r5) goto L_0x013d
            r5 = r14[r6]
            if (r5 == 0) goto L_0x0136
            boolean r16 = r5.startsWith(r2)
            if (r16 != 0) goto L_0x0134
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r0 = " "
            r8.append(r0)
            r8.append(r2)
            java.lang.String r0 = r8.toString()
            boolean r0 = r5.contains(r0)
            if (r0 == 0) goto L_0x0136
        L_0x0134:
            r15 = 1
            goto L_0x013d
        L_0x0136:
            int r6 = r6 + 1
            r0 = r17
            r5 = 3
            r8 = 2
            goto L_0x0111
        L_0x013d:
            if (r15 != 0) goto L_0x014a
            java.lang.String r0 = r12.username
            if (r0 == 0) goto L_0x014a
            boolean r0 = r0.startsWith(r2)
            if (r0 == 0) goto L_0x014a
            r15 = 1
        L_0x014a:
            if (r15 == 0) goto L_0x0150
            r4.add(r11)
            goto L_0x0159
        L_0x0150:
            int r13 = r13 + 1
            r0 = r17
            r2 = 1
            r5 = 0
            r6 = 0
            r8 = 2
            goto L_0x0109
        L_0x0159:
            int r10 = r10 + 1
            r0 = r17
            r2 = 1
            r5 = 0
            r6 = 0
            r8 = 2
            goto L_0x0097
        L_0x0163:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.findContacts(java.lang.String, java.lang.String, boolean):java.util.List");
    }

    public void checkAppUpdate(boolean z) {
        if (!z && BuildVars.DEBUG_VERSION) {
            return;
        }
        if (!z && !BuildVars.CHECK_UPDATES) {
            return;
        }
        if (z || Math.abs(System.currentTimeMillis() - UserConfig.getInstance(0).lastUpdateCheckTime) >= 86400000) {
            TLRPC$TL_help_getAppUpdate tLRPC$TL_help_getAppUpdate = new TLRPC$TL_help_getAppUpdate();
            try {
                tLRPC$TL_help_getAppUpdate.source = ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(ApplicationLoader.applicationContext.getPackageName());
            } catch (Exception unused) {
            }
            if (tLRPC$TL_help_getAppUpdate.source == null) {
                tLRPC$TL_help_getAppUpdate.source = "";
            }
            int i = this.currentAccount;
            ConnectionsManager.getInstance(i).sendRequest(tLRPC$TL_help_getAppUpdate, new RequestDelegate(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    LaunchActivity.this.lambda$checkAppUpdate$54$LaunchActivity(this.f$1, tLObject, tLRPC$TL_error);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$checkAppUpdate$54 */
    public /* synthetic */ void lambda$checkAppUpdate$54$LaunchActivity(int i, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        UserConfig.getInstance(0).lastUpdateCheckTime = System.currentTimeMillis();
        UserConfig.getInstance(0).saveConfig(false);
        if (tLObject instanceof TLRPC$TL_help_appUpdate) {
            AndroidUtilities.runOnUIThread(new Runnable((TLRPC$TL_help_appUpdate) tLObject, i) {
                public final /* synthetic */ TLRPC$TL_help_appUpdate f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    LaunchActivity.this.lambda$null$53$LaunchActivity(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$53 */
    public /* synthetic */ void lambda$null$53$LaunchActivity(TLRPC$TL_help_appUpdate tLRPC$TL_help_appUpdate, int i) {
        if (tLRPC$TL_help_appUpdate.can_not_skip) {
            UserConfig.getInstance(0).pendingAppUpdate = tLRPC$TL_help_appUpdate;
            UserConfig.getInstance(0).pendingAppUpdateBuildVersion = BuildVars.BUILD_VERSION;
            try {
                PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                UserConfig.getInstance(0).pendingAppUpdateInstallTime = Math.max(packageInfo.lastUpdateTime, packageInfo.firstInstallTime);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
                UserConfig.getInstance(0).pendingAppUpdateInstallTime = 0;
            }
            UserConfig.getInstance(0).saveConfig(false);
            showUpdateActivity(i, tLRPC$TL_help_appUpdate, false);
            return;
        }
        new UpdateAppAlertDialog(this, tLRPC$TL_help_appUpdate, i).show();
    }

    public AlertDialog showAlertDialog(AlertDialog.Builder builder) {
        try {
            AlertDialog alertDialog = this.visibleDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            AlertDialog show = builder.show();
            this.visibleDialog = show;
            show.setCanceledOnTouchOutside(true);
            this.visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public final void onDismiss(DialogInterface dialogInterface) {
                    LaunchActivity.this.lambda$showAlertDialog$55$LaunchActivity(dialogInterface);
                }
            });
            return this.visibleDialog;
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showAlertDialog$55 */
    public /* synthetic */ void lambda$showAlertDialog$55$LaunchActivity(DialogInterface dialogInterface) {
        AlertDialog alertDialog = this.visibleDialog;
        if (alertDialog != null) {
            if (alertDialog == this.localeDialog) {
                try {
                    Toast.makeText(this, getStringForLanguageAlert(LocaleController.getInstance().getCurrentLocaleInfo().shortName.equals("en") ? this.englishLocaleStrings : this.systemLocaleStrings, "ChangeLanguageLater", NUM), 1).show();
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
                this.localeDialog = null;
            } else if (alertDialog == this.proxyErrorDialog) {
                MessagesController.getGlobalMainSettings();
                SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
                edit.putBoolean("proxy_enabled", false);
                edit.putBoolean("proxy_enabled_calls", false);
                edit.commit();
                ConnectionsManager.setProxySettings(false, "", 1080, "", "", "");
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged, new Object[0]);
                this.proxyErrorDialog = null;
            }
        }
        this.visibleDialog = null;
    }

    public void showBulletin(Function<BulletinFactory, Bulletin> function) {
        BaseFragment baseFragment;
        if (!layerFragmentsStack.isEmpty()) {
            ArrayList<BaseFragment> arrayList = layerFragmentsStack;
            baseFragment = arrayList.get(arrayList.size() - 1);
        } else if (!rightFragmentsStack.isEmpty()) {
            ArrayList<BaseFragment> arrayList2 = rightFragmentsStack;
            baseFragment = arrayList2.get(arrayList2.size() - 1);
        } else if (!mainFragmentsStack.isEmpty()) {
            ArrayList<BaseFragment> arrayList3 = mainFragmentsStack;
            baseFragment = arrayList3.get(arrayList3.size() - 1);
        } else {
            baseFragment = null;
        }
        if (BulletinFactory.canShowBulletin(baseFragment)) {
            function.apply(BulletinFactory.of(baseFragment)).show();
        }
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    public void didSelectDialogs(DialogsActivity dialogsActivity, ArrayList<Long> arrayList, CharSequence charSequence, boolean z) {
        ChatActivity chatActivity;
        int i;
        DialogsActivity dialogsActivity2 = dialogsActivity;
        ArrayList<Long> arrayList2 = arrayList;
        int currentAccount2 = dialogsActivity2 != null ? dialogsActivity.getCurrentAccount() : this.currentAccount;
        Uri uri = this.exportingChatUri;
        if (uri != null) {
            ArrayList arrayList3 = this.documentsUrisArray != null ? new ArrayList(this.documentsUrisArray) : null;
            AlertDialog alertDialog = new AlertDialog(this, 3);
            SendMessagesHelper.getInstance(currentAccount2).prepareImportHistory(arrayList2.get(0).longValue(), this.exportingChatUri, this.documentsUrisArray, new MessagesStorage.IntCallback(currentAccount2, dialogsActivity, z, arrayList3, uri, alertDialog) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ DialogsActivity f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ ArrayList f$4;
                public final /* synthetic */ Uri f$5;
                public final /* synthetic */ AlertDialog f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                public final void run(int i) {
                    LaunchActivity.this.lambda$didSelectDialogs$56$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, i);
                }
            });
            try {
                alertDialog.showDelayed(300);
            } catch (Exception unused) {
            }
        } else {
            if (arrayList.size() <= 1) {
                long longValue = arrayList2.get(0).longValue();
                int i2 = (int) longValue;
                int i3 = (int) (longValue >> 32);
                Bundle bundle = new Bundle();
                bundle.putBoolean("scrollToTopOnResume", true);
                if (!AndroidUtilities.isTablet()) {
                    NotificationCenter.getInstance(currentAccount2).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                }
                if (i2 == 0) {
                    bundle.putInt("enc_id", i3);
                } else if (i2 > 0) {
                    bundle.putInt("user_id", i2);
                } else if (i2 < 0) {
                    bundle.putInt("chat_id", -i2);
                }
                if (MessagesController.getInstance(currentAccount2).checkCanOpenChat(bundle, dialogsActivity2)) {
                    chatActivity = new ChatActivity(bundle);
                } else {
                    return;
                }
            } else {
                chatActivity = null;
            }
            ArrayList<TLRPC$User> arrayList4 = this.contactsToSend;
            int size = arrayList4 != null ? arrayList4.size() + 0 : 0;
            if (this.videoPath != null) {
                size++;
            }
            ArrayList<SendMessagesHelper.SendingMediaInfo> arrayList5 = this.photoPathsArray;
            if (arrayList5 != null) {
                size += arrayList5.size();
            }
            ArrayList<String> arrayList6 = this.documentsPathsArray;
            if (arrayList6 != null) {
                size += arrayList6.size();
            }
            ArrayList<Uri> arrayList7 = this.documentsUrisArray;
            if (arrayList7 != null) {
                size += arrayList7.size();
            }
            if (this.videoPath == null && this.photoPathsArray == null && this.documentsPathsArray == null && this.documentsUrisArray == null && this.sendingText != null) {
                size++;
            }
            int i4 = 0;
            while (i4 < arrayList.size()) {
                if (!AlertsCreator.checkSlowMode(this, this.currentAccount, arrayList2.get(i4).longValue(), size > 1)) {
                    i4++;
                } else {
                    return;
                }
            }
            ArrayList<TLRPC$User> arrayList8 = this.contactsToSend;
            if (arrayList8 == null || arrayList8.size() != 1 || mainFragmentsStack.isEmpty()) {
                String str = null;
                for (int i5 = 0; i5 < arrayList.size(); i5++) {
                    long longValue2 = arrayList2.get(i5).longValue();
                    AccountInstance instance = AccountInstance.getInstance(UserConfig.selectedAccount);
                    if (chatActivity != null) {
                        i = 1024;
                        this.actionBarLayout.presentFragment(chatActivity, dialogsActivity2 != null, dialogsActivity2 == null, true, false);
                        String str2 = this.videoPath;
                        if (str2 != null) {
                            chatActivity.openVideoEditor(str2, this.sendingText);
                            this.sendingText = null;
                        }
                    } else {
                        i = 1024;
                        if (this.videoPath != null) {
                            String str3 = this.sendingText;
                            if (str3 != null && str3.length() <= 1024) {
                                str = this.sendingText;
                                this.sendingText = null;
                            }
                            ArrayList arrayList9 = new ArrayList();
                            arrayList9.add(this.videoPath);
                            SendMessagesHelper.prepareSendingDocuments(instance, arrayList9, arrayList9, (ArrayList<Uri>) null, str, (String) null, longValue2, (MessageObject) null, (MessageObject) null, (InputContentInfoCompat) null, (MessageObject) null, true, 0);
                        }
                    }
                    if (this.photoPathsArray != null) {
                        String str4 = this.sendingText;
                        if (str4 != null && str4.length() <= i && this.photoPathsArray.size() == 1) {
                            this.photoPathsArray.get(0).caption = this.sendingText;
                            this.sendingText = null;
                        }
                        SendMessagesHelper.prepareSendingMedia(instance, this.photoPathsArray, longValue2, (MessageObject) null, (MessageObject) null, (InputContentInfoCompat) null, false, false, (MessageObject) null, true, 0);
                    }
                    if (!(this.documentsPathsArray == null && this.documentsUrisArray == null)) {
                        String str5 = this.sendingText;
                        if (str5 != null && str5.length() <= i) {
                            ArrayList<String> arrayList10 = this.documentsPathsArray;
                            int size2 = arrayList10 != null ? arrayList10.size() : 0;
                            ArrayList<Uri> arrayList11 = this.documentsUrisArray;
                            if (size2 + (arrayList11 != null ? arrayList11.size() : 0) == 1) {
                                str = this.sendingText;
                                this.sendingText = null;
                            }
                        }
                        SendMessagesHelper.prepareSendingDocuments(instance, this.documentsPathsArray, this.documentsOriginalPathsArray, this.documentsUrisArray, str, this.documentsMimeType, longValue2, (MessageObject) null, (MessageObject) null, (InputContentInfoCompat) null, (MessageObject) null, true, 0);
                    }
                    String str6 = this.sendingText;
                    if (str6 != null) {
                        SendMessagesHelper.prepareSendingText(instance, str6, longValue2, true, 0);
                    }
                    ArrayList<TLRPC$User> arrayList12 = this.contactsToSend;
                    if (arrayList12 != null && !arrayList12.isEmpty()) {
                        for (int i6 = 0; i6 < this.contactsToSend.size(); i6++) {
                            SendMessagesHelper.getInstance(currentAccount2).sendMessage(this.contactsToSend.get(i6), longValue2, (MessageObject) null, (MessageObject) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, true, 0);
                        }
                    }
                    if (!TextUtils.isEmpty(charSequence)) {
                        SendMessagesHelper.prepareSendingText(instance, charSequence.toString(), longValue2, true, 0);
                    }
                }
            } else {
                ArrayList<BaseFragment> arrayList13 = mainFragmentsStack;
                PhonebookShareAlert phonebookShareAlert = new PhonebookShareAlert(arrayList13.get(arrayList13.size() - 1), (ContactsController.Contact) null, (TLRPC$User) null, this.contactsToSendUri, (File) null, (String) null, (String) null);
                phonebookShareAlert.setDelegate(new ChatAttachAlertContactsLayout.PhonebookShareAlertDelegate(chatActivity, arrayList2, currentAccount2) {
                    public final /* synthetic */ ChatActivity f$1;
                    public final /* synthetic */ ArrayList f$2;
                    public final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void didSelectContact(TLRPC$User tLRPC$User, boolean z, int i) {
                        LaunchActivity.this.lambda$didSelectDialogs$57$LaunchActivity(this.f$1, this.f$2, this.f$3, tLRPC$User, z, i);
                    }
                });
                ArrayList<BaseFragment> arrayList14 = mainFragmentsStack;
                arrayList14.get(arrayList14.size() - 1).showDialog(phonebookShareAlert);
            }
            if (dialogsActivity2 != null && chatActivity == null) {
                dialogsActivity.finishFragment();
            }
        }
        this.photoPathsArray = null;
        this.videoPath = null;
        this.sendingText = null;
        this.documentsPathsArray = null;
        this.documentsOriginalPathsArray = null;
        this.contactsToSend = null;
        this.contactsToSendUri = null;
        this.exportingChatUri = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didSelectDialogs$56 */
    public /* synthetic */ void lambda$didSelectDialogs$56$LaunchActivity(int i, DialogsActivity dialogsActivity, boolean z, ArrayList arrayList, Uri uri, AlertDialog alertDialog, int i2) {
        if (i2 != 0) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("scrollToTopOnResume", true);
            if (!AndroidUtilities.isTablet()) {
                NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            }
            if (i2 > 0) {
                bundle.putInt("user_id", i2);
            } else {
                bundle.putInt("chat_id", -i2);
            }
            ChatActivity chatActivity = new ChatActivity(bundle);
            chatActivity.setOpenImport();
            this.actionBarLayout.presentFragment(chatActivity, dialogsActivity != null || z, dialogsActivity == null, true, false);
        } else {
            this.documentsUrisArray = arrayList;
            if (arrayList == null) {
                this.documentsUrisArray = new ArrayList<>();
            }
            this.documentsUrisArray.add(0, uri);
            openDialogsToSend(true);
        }
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didSelectDialogs$57 */
    public /* synthetic */ void lambda$didSelectDialogs$57$LaunchActivity(ChatActivity chatActivity, ArrayList arrayList, int i, TLRPC$User tLRPC$User, boolean z, int i2) {
        if (chatActivity != null) {
            this.actionBarLayout.presentFragment(chatActivity, true, false, true, false);
        }
        for (int i3 = 0; i3 < arrayList.size(); i3++) {
            SendMessagesHelper.getInstance(i).sendMessage(tLRPC$User, ((Long) arrayList.get(i3)).longValue(), (MessageObject) null, (MessageObject) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, z, i2);
        }
    }

    private void onFinish() {
        Runnable runnable = this.lockRunnable;
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
            this.lockRunnable = null;
        }
        if (!this.finished) {
            this.finished = true;
            int i = this.currentAccount;
            if (i != -1) {
                NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.appDidLogout);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowPlayServicesAlert);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidFailToLoad);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.historyImportProgressChanged);
            }
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needShowAlert);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needCheckSystemBarColors);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.notificationsCountUpdated);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.screenStateChanged);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.showBulletin);
        }
    }

    /* renamed from: presentFragment */
    public void lambda$runLinkRequest$41(BaseFragment baseFragment) {
        this.actionBarLayout.presentFragment(baseFragment);
    }

    public boolean presentFragment(BaseFragment baseFragment, boolean z, boolean z2) {
        return this.actionBarLayout.presentFragment(baseFragment, z, z2, true, false);
    }

    public ActionBarLayout getActionBarLayout() {
        return this.actionBarLayout;
    }

    public ActionBarLayout getLayersActionBarLayout() {
        return this.layersActionBarLayout;
    }

    public ActionBarLayout getRightActionBarLayout() {
        return this.rightActionBarLayout;
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        boolean z = false;
        if (!(SharedConfig.passcodeHash.length() == 0 || SharedConfig.lastPauseTime == 0)) {
            SharedConfig.lastPauseTime = 0;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("reset lastPauseTime onActivityResult");
            }
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        if (i != 105) {
            super.onActivityResult(i, i2, intent);
            if (i == 140) {
                LocationController instance = LocationController.getInstance(this.currentAccount);
                if (i2 == -1) {
                    z = true;
                }
                instance.startFusedLocationRequest(z);
                return;
            }
            ThemeEditorView instance2 = ThemeEditorView.getInstance();
            if (instance2 != null) {
                instance2.onActivityResult(i, i2, intent);
            }
            if (this.actionBarLayout.fragmentsStack.size() != 0) {
                ArrayList<BaseFragment> arrayList = this.actionBarLayout.fragmentsStack;
                arrayList.get(arrayList.size() - 1).onActivityResultFragment(i, i2, intent);
            }
            if (AndroidUtilities.isTablet()) {
                if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                    ArrayList<BaseFragment> arrayList2 = this.rightActionBarLayout.fragmentsStack;
                    arrayList2.get(arrayList2.size() - 1).onActivityResultFragment(i, i2, intent);
                }
                if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                    ArrayList<BaseFragment> arrayList3 = this.layersActionBarLayout.fragmentsStack;
                    arrayList3.get(arrayList3.size() - 1).onActivityResultFragment(i, i2, intent);
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            boolean canDrawOverlays = Settings.canDrawOverlays(this);
            ApplicationLoader.canDrawOverlays = canDrawOverlays;
            if (canDrawOverlays) {
                GroupCallActivity groupCallActivity = GroupCallActivity.groupCallInstance;
                if (groupCallActivity != null) {
                    groupCallActivity.dismissInternal();
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public final void run() {
                        LaunchActivity.this.lambda$onActivityResult$58$LaunchActivity();
                    }
                }, 200);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onActivityResult$58 */
    public /* synthetic */ void lambda$onActivityResult$58$LaunchActivity() {
        GroupCallPip.clearForce();
        GroupCallPip.updateVisibility(this);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr == null) {
            iArr = new int[0];
        }
        if (strArr == null) {
            strArr = new String[0];
        }
        boolean z = iArr.length > 0 && iArr[0] == 0;
        if (i == 4) {
            if (!z) {
                showPermissionErrorAlert(LocaleController.getString("PermissionStorage", NUM));
            } else {
                ImageLoader.getInstance().checkMediaPaths();
            }
        } else if (i == 5) {
            if (!z) {
                showPermissionErrorAlert(LocaleController.getString("PermissionContacts", NUM));
                return;
            }
            ContactsController.getInstance(this.currentAccount).forceImportContacts();
        } else if (i == 3) {
            int min = Math.min(strArr.length, iArr.length);
            boolean z2 = true;
            boolean z3 = true;
            for (int i2 = 0; i2 < min; i2++) {
                if ("android.permission.RECORD_AUDIO".equals(strArr[i2])) {
                    z2 = iArr[i2] == 0;
                } else if ("android.permission.CAMERA".equals(strArr[i2])) {
                    z3 = iArr[i2] == 0;
                }
            }
            if (!z2) {
                showPermissionErrorAlert(LocaleController.getString("PermissionNoAudio", NUM));
            } else if (!z3) {
                showPermissionErrorAlert(LocaleController.getString("PermissionNoCamera", NUM));
            } else if (SharedConfig.inappCamera) {
                CameraController.getInstance().initCamera((Runnable) null);
                return;
            } else {
                return;
            }
        } else if (i == 18 || i == 19 || i == 20 || i == 22) {
            if (!z) {
                showPermissionErrorAlert(LocaleController.getString("PermissionNoCamera", NUM));
            }
        } else if (i == 2 && z) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.locationPermissionGranted, new Object[0]);
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ArrayList<BaseFragment> arrayList = this.actionBarLayout.fragmentsStack;
            arrayList.get(arrayList.size() - 1).onRequestPermissionsResultFragment(i, strArr, iArr);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ArrayList<BaseFragment> arrayList2 = this.rightActionBarLayout.fragmentsStack;
                arrayList2.get(arrayList2.size() - 1).onRequestPermissionsResultFragment(i, strArr, iArr);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ArrayList<BaseFragment> arrayList3 = this.layersActionBarLayout.fragmentsStack;
                arrayList3.get(arrayList3.size() - 1).onRequestPermissionsResultFragment(i, strArr, iArr);
            }
        }
        VoIPFragment.onRequestPermissionsResult(i, strArr, iArr);
    }

    private void showPermissionErrorAlert(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
        builder.setTitle(LocaleController.getString("AppName", NUM));
        builder.setMessage(str);
        builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", NUM), new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                LaunchActivity.this.lambda$showPermissionErrorAlert$59$LaunchActivity(dialogInterface, i);
            }
        });
        builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
        builder.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPermissionErrorAlert$59 */
    public /* synthetic */ void lambda$showPermissionErrorAlert$59$LaunchActivity(DialogInterface dialogInterface, int i) {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.stopAllHeavyOperations, 4096);
        ApplicationLoader.mainInterfacePaused = true;
        Utilities.stageQueue.postRunnable(new Runnable(this.currentAccount) {
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                LaunchActivity.lambda$onPause$60(this.f$0);
            }
        });
        onPasscodePause();
        this.actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onPause();
            this.layersActionBarLayout.onPause();
        }
        PasscodeView passcodeView2 = this.passcodeView;
        if (passcodeView2 != null) {
            passcodeView2.onPause();
        }
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onPause();
        }
        if (VoIPFragment.getInstance() != null) {
            VoIPFragment.onPause();
        }
    }

    static /* synthetic */ void lambda$onPause$60(int i) {
        ApplicationLoader.mainInterfacePausedStageQueue = true;
        ApplicationLoader.mainInterfacePausedStageQueueTime = 0;
        if (VoIPService.getSharedInstance() == null) {
            MessagesController.getInstance(i).ignoreSetOnline = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Browser.bindCustomTabsService(this);
        ApplicationLoader.mainInterfaceStopped = false;
        GroupCallPip.updateVisibility(this);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Browser.unbindCustomTabsService(this);
        ApplicationLoader.mainInterfaceStopped = true;
        GroupCallPip.updateVisibility(this);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (PhotoViewer.getPipInstance() != null) {
            PhotoViewer.getPipInstance().destroyPhotoViewer();
        }
        if (PhotoViewer.hasInstance()) {
            PhotoViewer.getInstance().destroyPhotoViewer();
        }
        if (SecretMediaViewer.hasInstance()) {
            SecretMediaViewer.getInstance().destroyPhotoViewer();
        }
        if (ArticleViewer.hasInstance()) {
            ArticleViewer.getInstance().destroyArticleViewer();
        }
        if (ContentPreviewViewer.hasInstance()) {
            ContentPreviewViewer.getInstance().destroy();
        }
        GroupCallActivity groupCallActivity = GroupCallActivity.groupCallInstance;
        if (groupCallActivity != null) {
            groupCallActivity.dismissInternal();
        }
        PipRoundVideoView instance = PipRoundVideoView.getInstance();
        MediaController.getInstance().setBaseActivity(this, false);
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, false);
        if (instance != null) {
            instance.close(false);
        }
        Theme.destroyResources();
        EmbedBottomSheet instance2 = EmbedBottomSheet.getInstance();
        if (instance2 != null) {
            instance2.destroy();
        }
        ThemeEditorView instance3 = ThemeEditorView.getInstance();
        if (instance3 != null) {
            instance3.destroy();
        }
        try {
            AlertDialog alertDialog = this.visibleDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            if (this.onGlobalLayoutListener != null) {
                getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
            }
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
        super.onDestroy();
        onFinish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        MessageObject playingMessageObject;
        super.onResume();
        if (Theme.selectedAutoNightType == 3) {
            Theme.checkAutoNightThemeConditions();
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.startAllHeavyOperations, 4096);
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, true);
        ApplicationLoader.mainInterfacePaused = false;
        showLanguageAlert(false);
        Utilities.stageQueue.postRunnable($$Lambda$LaunchActivity$nZYI2PWYyxHcYI8dHurda3KvFnQ.INSTANCE);
        checkFreeDiscSpace();
        MediaController.checkGallery();
        onPasscodeResume();
        if (this.passcodeView.getVisibility() != 0) {
            this.actionBarLayout.onResume();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onResume();
                this.layersActionBarLayout.onResume();
            }
        } else {
            this.actionBarLayout.dismissDialogs();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.dismissDialogs();
                this.layersActionBarLayout.dismissDialogs();
            }
            this.passcodeView.onResume();
        }
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
        updateCurrentConnectionState(this.currentAccount);
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }
        if (!(PipRoundVideoView.getInstance() == null || !MediaController.getInstance().isMessagePaused() || (playingMessageObject = MediaController.getInstance().getPlayingMessageObject()) == null)) {
            MediaController.getInstance().seekToProgress(playingMessageObject, playingMessageObject.audioProgress);
        }
        if (UserConfig.getInstance(UserConfig.selectedAccount).unacceptedTermsOfService != null) {
            int i = UserConfig.selectedAccount;
            showTosActivity(i, UserConfig.getInstance(i).unacceptedTermsOfService);
        } else if (UserConfig.getInstance(0).pendingAppUpdate != null) {
            showUpdateActivity(UserConfig.selectedAccount, UserConfig.getInstance(0).pendingAppUpdate, true);
        }
        checkAppUpdate(false);
        if (Build.VERSION.SDK_INT >= 23) {
            ApplicationLoader.canDrawOverlays = Settings.canDrawOverlays(this);
        }
        if (VoIPFragment.getInstance() != null) {
            VoIPFragment.onResume();
        }
    }

    static /* synthetic */ void lambda$onResume$61() {
        ApplicationLoader.mainInterfacePausedStageQueue = false;
        ApplicationLoader.mainInterfacePausedStageQueueTime = System.currentTimeMillis();
    }

    public void onConfigurationChanged(Configuration configuration) {
        AndroidUtilities.checkDisplaySize(this, configuration);
        super.onConfigurationChanged(configuration);
        checkLayout();
        PipRoundVideoView instance = PipRoundVideoView.getInstance();
        if (instance != null) {
            instance.onConfigurationChanged();
        }
        EmbedBottomSheet instance2 = EmbedBottomSheet.getInstance();
        if (instance2 != null) {
            instance2.onConfigurationChanged(configuration);
        }
        PhotoViewer pipInstance = PhotoViewer.getPipInstance();
        if (pipInstance != null) {
            pipInstance.onConfigurationChanged(configuration);
        }
        ThemeEditorView instance3 = ThemeEditorView.getInstance();
        if (instance3 != null) {
            instance3.onConfigurationChanged();
        }
        if (Theme.selectedAutoNightType == 3) {
            Theme.checkAutoNightThemeConditions();
        }
    }

    public void onMultiWindowModeChanged(boolean z) {
        AndroidUtilities.isInMultiwindow = z;
        checkLayout();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v14, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v16, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v21, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v27, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v31, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v35, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v37, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v31, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v20, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v45, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v41, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v34, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v50, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v64, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v66, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v36, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v56, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v9, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v91, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v100, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v114, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v116, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v133, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v138, resolved type: org.telegram.tgnet.TLRPC$TL_help_termsOfService} */
    /* JADX WARNING: type inference failed for: r5v0 */
    /* JADX WARNING: type inference failed for: r5v3, types: [int] */
    /* JADX WARNING: type inference failed for: r5v10 */
    /* JADX WARNING: type inference failed for: r5v19 */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0273, code lost:
        if (((org.telegram.ui.ProfileActivity) r2.get(r2.size() - 1)).isSettings() == false) goto L_0x0277;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x04ed  */
    /* JADX WARNING: Removed duplicated region for block: B:279:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0262  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void didReceivedNotification(int r17, int r18, java.lang.Object... r19) {
        /*
            r16 = this;
            r1 = r16
            r0 = r17
            r2 = r18
            r3 = r19
            int r4 = org.telegram.messenger.NotificationCenter.appDidLogout
            if (r0 != r4) goto L_0x0011
            r16.switchToAvailableAccountOrLogout()
            goto L_0x06b5
        L_0x0011:
            int r4 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities
            r5 = 0
            if (r0 != r4) goto L_0x0022
            r0 = r3[r5]
            if (r0 == r1) goto L_0x06b5
            r16.onFinish()
            r16.finish()
            goto L_0x06b5
        L_0x0022:
            int r4 = org.telegram.messenger.NotificationCenter.didUpdateConnectionState
            if (r0 != r4) goto L_0x0051
            org.telegram.tgnet.ConnectionsManager r0 = org.telegram.tgnet.ConnectionsManager.getInstance(r18)
            int r0 = r0.getConnectionState()
            int r3 = r1.currentConnectionState
            if (r3 == r0) goto L_0x06b5
            boolean r3 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r3 == 0) goto L_0x004a
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "switch to state "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            org.telegram.messenger.FileLog.d(r3)
        L_0x004a:
            r1.currentConnectionState = r0
            r1.updateCurrentConnectionState(r2)
            goto L_0x06b5
        L_0x0051:
            int r4 = org.telegram.messenger.NotificationCenter.mainUserInfoChanged
            if (r0 != r4) goto L_0x005c
            org.telegram.ui.Adapters.DrawerLayoutAdapter r0 = r1.drawerLayoutAdapter
            r0.notifyDataSetChanged()
            goto L_0x06b5
        L_0x005c:
            int r4 = org.telegram.messenger.NotificationCenter.needShowAlert
            java.lang.String r7 = "Cancel"
            r8 = 5
            r9 = 2131624280(0x7f0e0158, float:1.8875735E38)
            java.lang.String r10 = "AppName"
            r11 = 4
            r12 = 3
            r13 = 2131626473(0x7f0e09e9, float:1.8880183E38)
            java.lang.String r14 = "OK"
            r15 = 2
            r6 = 1
            if (r0 != r4) goto L_0x01a2
            r0 = r3[r5]
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r4 = r0.intValue()
            r5 = 6
            if (r4 == r5) goto L_0x01a1
            int r4 = r0.intValue()
            if (r4 != r12) goto L_0x0088
            org.telegram.ui.ActionBar.AlertDialog r4 = r1.proxyErrorDialog
            if (r4 == 0) goto L_0x0088
            goto L_0x01a1
        L_0x0088:
            int r4 = r0.intValue()
            if (r4 != r11) goto L_0x0096
            r0 = r3[r6]
            org.telegram.tgnet.TLRPC$TL_help_termsOfService r0 = (org.telegram.tgnet.TLRPC$TL_help_termsOfService) r0
            r1.showTosActivity(r2, r0)
            return
        L_0x0096:
            org.telegram.ui.ActionBar.AlertDialog$Builder r4 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r4.<init>((android.content.Context) r1)
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r4.setTitle(r9)
            int r9 = r0.intValue()
            if (r9 == r15) goto L_0x00c5
            int r9 = r0.intValue()
            if (r9 == r12) goto L_0x00c5
            int r9 = r0.intValue()
            if (r9 == r5) goto L_0x00c5
            r5 = 2131626163(0x7f0e08b3, float:1.8879554E38)
            java.lang.String r9 = "MoreInfo"
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r9, r5)
            org.telegram.ui.-$$Lambda$LaunchActivity$SCcf-_q-u1JVKeTVoRH2zokW458 r9 = new org.telegram.ui.-$$Lambda$LaunchActivity$SCcf-_q-u1JVKeTVoRH2zokW458
            r9.<init>(r2)
            r4.setNegativeButton(r5, r9)
        L_0x00c5:
            int r2 = r0.intValue()
            if (r2 != r8) goto L_0x00e1
            r0 = 2131626289(0x7f0e0931, float:1.887981E38)
            java.lang.String r2 = "NobodyLikesSpam3"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r4.setMessage(r0)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r2 = 0
            r4.setPositiveButton(r0, r2)
            goto L_0x0183
        L_0x00e1:
            r2 = 0
            int r5 = r0.intValue()
            if (r5 != 0) goto L_0x00fd
            r0 = 2131626287(0x7f0e092f, float:1.8879806E38)
            java.lang.String r3 = "NobodyLikesSpam1"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r3, r0)
            r4.setMessage(r0)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r4.setPositiveButton(r0, r2)
            goto L_0x0183
        L_0x00fd:
            int r5 = r0.intValue()
            if (r5 != r6) goto L_0x0117
            r0 = 2131626288(0x7f0e0930, float:1.8879808E38)
            java.lang.String r3 = "NobodyLikesSpam2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r3, r0)
            r4.setMessage(r0)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r4.setPositiveButton(r0, r2)
            goto L_0x0183
        L_0x0117:
            int r2 = r0.intValue()
            if (r2 != r15) goto L_0x0156
            r0 = r3[r6]
            java.lang.String r0 = (java.lang.String) r0
            r4.setMessage(r0)
            r0 = r3[r15]
            java.lang.String r0 = (java.lang.String) r0
            java.lang.String r2 = "AUTH_KEY_DROP_"
            boolean r0 = r0.startsWith(r2)
            if (r0 == 0) goto L_0x014d
            r0 = 2131624637(0x7f0e02bd, float:1.887646E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r7, r0)
            r2 = 0
            r4.setPositiveButton(r0, r2)
            r0 = 2131625980(0x7f0e07fc, float:1.8879183E38)
            java.lang.String r2 = "LogOut"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            org.telegram.ui.-$$Lambda$LaunchActivity$V2eQa3_rAwGNDg0nIbDTI1QF8qk r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$V2eQa3_rAwGNDg0nIbDTI1QF8qk
            r2.<init>()
            r4.setNegativeButton(r0, r2)
            goto L_0x0183
        L_0x014d:
            r2 = 0
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r4.setPositiveButton(r0, r2)
            goto L_0x0183
        L_0x0156:
            int r0 = r0.intValue()
            if (r0 != r12) goto L_0x0183
            r0 = 2131627003(0x7f0e0bfb, float:1.8881258E38)
            java.lang.String r2 = "Proxy"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r4.setTitle(r0)
            r0 = 2131627775(0x7f0e0eff, float:1.8882824E38)
            java.lang.String r2 = "UseProxyTelegramError"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r4.setMessage(r0)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r2 = 0
            r4.setPositiveButton(r0, r2)
            org.telegram.ui.ActionBar.AlertDialog r0 = r1.showAlertDialog(r4)
            r1.proxyErrorDialog = r0
            return
        L_0x0183:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x06b5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            int r2 = r0.size()
            int r2 = r2 - r6
            java.lang.Object r0 = r0.get(r2)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            org.telegram.ui.ActionBar.AlertDialog r2 = r4.create()
            r0.showDialog(r2)
            goto L_0x06b5
        L_0x01a1:
            return
        L_0x01a2:
            int r4 = org.telegram.messenger.NotificationCenter.wasUnableToFindCurrentLocation
            if (r0 != r4) goto L_0x01f9
            r0 = r3[r5]
            java.util.HashMap r0 = (java.util.HashMap) r0
            org.telegram.ui.ActionBar.AlertDialog$Builder r3 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r3.<init>((android.content.Context) r1)
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r3.setTitle(r4)
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r5 = 0
            r3.setPositiveButton(r4, r5)
            r4 = 2131627394(0x7f0e0d82, float:1.8882051E38)
            java.lang.String r5 = "ShareYouLocationUnableManually"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            org.telegram.ui.-$$Lambda$LaunchActivity$clw_9BxtWBWPpwYAnFcwbhkXUaU r5 = new org.telegram.ui.-$$Lambda$LaunchActivity$clw_9BxtWBWPpwYAnFcwbhkXUaU
            r5.<init>(r0, r2)
            r3.setNegativeButton(r4, r5)
            r0 = 2131627393(0x7f0e0d81, float:1.888205E38)
            java.lang.String r2 = "ShareYouLocationUnable"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r3.setMessage(r0)
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x06b5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            int r2 = r0.size()
            int r2 = r2 - r6
            java.lang.Object r0 = r0.get(r2)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            org.telegram.ui.ActionBar.AlertDialog r2 = r3.create()
            r0.showDialog(r2)
            goto L_0x06b5
        L_0x01f9:
            int r4 = org.telegram.messenger.NotificationCenter.didSetNewWallpapper
            if (r0 != r4) goto L_0x020c
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            if (r0 == 0) goto L_0x06b5
            android.view.View r0 = r0.getChildAt(r5)
            if (r0 == 0) goto L_0x06b5
            r0.invalidate()
            goto L_0x06b5
        L_0x020c:
            int r4 = org.telegram.messenger.NotificationCenter.didSetPasscode
            if (r0 != r4) goto L_0x0242
            java.lang.String r0 = org.telegram.messenger.SharedConfig.passcodeHash
            int r0 = r0.length()
            r2 = 8192(0x2000, float:1.14794E-41)
            if (r0 <= 0) goto L_0x022d
            boolean r0 = org.telegram.messenger.SharedConfig.allowScreenCapture
            if (r0 != 0) goto L_0x022d
            android.view.Window r0 = r16.getWindow()     // Catch:{ Exception -> 0x0227 }
            r0.setFlags(r2, r2)     // Catch:{ Exception -> 0x0227 }
            goto L_0x06b5
        L_0x0227:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x06b5
        L_0x022d:
            boolean r0 = org.telegram.messenger.AndroidUtilities.hasFlagSecureFragment()
            if (r0 != 0) goto L_0x06b5
            android.view.Window r0 = r16.getWindow()     // Catch:{ Exception -> 0x023c }
            r0.clearFlags(r2)     // Catch:{ Exception -> 0x023c }
            goto L_0x06b5
        L_0x023c:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x06b5
        L_0x0242:
            int r4 = org.telegram.messenger.NotificationCenter.reloadInterface
            if (r0 != r4) goto L_0x027c
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            int r0 = r0.size()
            if (r0 <= r6) goto L_0x025f
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            int r2 = r0.size()
            int r2 = r2 - r6
            java.lang.Object r0 = r0.get(r2)
            boolean r0 = r0 instanceof org.telegram.ui.ProfileActivity
            if (r0 == 0) goto L_0x025f
            r0 = 1
            goto L_0x0260
        L_0x025f:
            r0 = 0
        L_0x0260:
            if (r0 == 0) goto L_0x0276
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r2 = mainFragmentsStack
            int r3 = r2.size()
            int r3 = r3 - r6
            java.lang.Object r2 = r2.get(r3)
            org.telegram.ui.ProfileActivity r2 = (org.telegram.ui.ProfileActivity) r2
            boolean r2 = r2.isSettings()
            if (r2 != 0) goto L_0x0276
            goto L_0x0277
        L_0x0276:
            r5 = r0
        L_0x0277:
            r1.rebuildAllFragments(r5)
            goto L_0x06b5
        L_0x027c:
            int r4 = org.telegram.messenger.NotificationCenter.suggestedLangpack
            if (r0 != r4) goto L_0x0285
            r1.showLanguageAlert(r5)
            goto L_0x06b5
        L_0x0285:
            int r4 = org.telegram.messenger.NotificationCenter.openArticle
            if (r0 != r4) goto L_0x02b7
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0292
            return
        L_0x0292:
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r2 = mainFragmentsStack
            int r4 = r2.size()
            int r4 = r4 - r6
            java.lang.Object r2 = r2.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r2 = (org.telegram.ui.ActionBar.BaseFragment) r2
            r0.setParentActivity(r1, r2)
            org.telegram.ui.ArticleViewer r0 = org.telegram.ui.ArticleViewer.getInstance()
            r2 = r3[r5]
            org.telegram.tgnet.TLRPC$TL_webPage r2 = (org.telegram.tgnet.TLRPC$TL_webPage) r2
            r3 = r3[r6]
            java.lang.String r3 = (java.lang.String) r3
            r0.open(r2, r3)
            goto L_0x06b5
        L_0x02b7:
            int r4 = org.telegram.messenger.NotificationCenter.hasNewContactsToImport
            if (r0 != r4) goto L_0x033f
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r1.actionBarLayout
            if (r0 == 0) goto L_0x033e
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = r0.fragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x02c8
            goto L_0x033e
        L_0x02c8:
            r0 = r3[r5]
            java.lang.Integer r0 = (java.lang.Integer) r0
            r0.intValue()
            r0 = r3[r6]
            java.util.HashMap r0 = (java.util.HashMap) r0
            r4 = r3[r15]
            java.lang.Boolean r4 = (java.lang.Boolean) r4
            boolean r4 = r4.booleanValue()
            r3 = r3[r12]
            java.lang.Boolean r3 = (java.lang.Boolean) r3
            boolean r3 = r3.booleanValue()
            org.telegram.ui.ActionBar.ActionBarLayout r8 = r1.actionBarLayout
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r8 = r8.fragmentsStack
            int r9 = r8.size()
            int r9 = r9 - r6
            java.lang.Object r6 = r8.get(r9)
            org.telegram.ui.ActionBar.BaseFragment r6 = (org.telegram.ui.ActionBar.BaseFragment) r6
            org.telegram.ui.ActionBar.AlertDialog$Builder r8 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r8.<init>((android.content.Context) r1)
            r9 = 2131627743(0x7f0e0edf, float:1.888276E38)
            java.lang.String r10 = "UpdateContactsTitle"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r8.setTitle(r9)
            r9 = 2131627742(0x7f0e0ede, float:1.8882757E38)
            java.lang.String r10 = "UpdateContactsMessage"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r8.setMessage(r9)
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r14, r13)
            org.telegram.ui.-$$Lambda$LaunchActivity$21s7RGBMu1rdDPmDpjfD3x9rS-E r10 = new org.telegram.ui.-$$Lambda$LaunchActivity$21s7RGBMu1rdDPmDpjfD3x9rS-E
            r10.<init>(r2, r0, r4, r3)
            r8.setPositiveButton(r9, r10)
            r9 = 2131624637(0x7f0e02bd, float:1.887646E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r7, r9)
            org.telegram.ui.-$$Lambda$LaunchActivity$nwcfu6i_mbCrr2wexylSTSY5gMY r9 = new org.telegram.ui.-$$Lambda$LaunchActivity$nwcfu6i_mbCrr2wexylSTSY5gMY
            r9.<init>(r2, r0, r4, r3)
            r8.setNegativeButton(r7, r9)
            org.telegram.ui.-$$Lambda$LaunchActivity$u6y_ASGln28gOCl_6a9-Nj_3XHo r7 = new org.telegram.ui.-$$Lambda$LaunchActivity$u6y_ASGln28gOCl_6a9-Nj_3XHo
            r7.<init>(r2, r0, r4, r3)
            r8.setOnBackButtonListener(r7)
            org.telegram.ui.ActionBar.AlertDialog r0 = r8.create()
            r6.showDialog(r0)
            r0.setCanceledOnTouchOutside(r5)
            goto L_0x06b5
        L_0x033e:
            return
        L_0x033f:
            int r2 = org.telegram.messenger.NotificationCenter.didSetNewTheme
            r4 = 21
            if (r0 != r2) goto L_0x03a0
            r0 = r3[r5]
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            if (r0 != 0) goto L_0x038f
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            if (r0 == 0) goto L_0x0379
            java.lang.String r2 = "chats_menuBackground"
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r0.setBackgroundColor(r3)
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r0.setGlowColor(r2)
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            java.lang.String r2 = "listSelectorSDK21"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r0.setListSelectorColor(r2)
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            androidx.recyclerview.widget.RecyclerView$Adapter r0 = r0.getAdapter()
            r0.notifyDataSetChanged()
        L_0x0379:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r4) goto L_0x038f
            android.app.ActivityManager$TaskDescription r0 = new android.app.ActivityManager$TaskDescription     // Catch:{ Exception -> 0x038f }
            java.lang.String r2 = "actionBarDefault"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)     // Catch:{ Exception -> 0x038f }
            r3 = -16777216(0xfffffffffvar_, float:-1.7014118E38)
            r2 = r2 | r3
            r3 = 0
            r0.<init>(r3, r3, r2)     // Catch:{ Exception -> 0x038f }
            r1.setTaskDescription(r0)     // Catch:{ Exception -> 0x038f }
        L_0x038f:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r1.drawerLayoutContainer
            java.lang.String r2 = "windowBackgroundWhite"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r0.setBehindKeyboardColor(r2)
            r16.checkSystemBarColors()
            goto L_0x06b5
        L_0x03a0:
            int r2 = org.telegram.messenger.NotificationCenter.needSetDayNightTheme
            if (r0 != r2) goto L_0x04f9
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r4) goto L_0x04cd
            r0 = r3[r15]
            if (r0 == 0) goto L_0x04cd
            android.widget.ImageView r0 = r1.themeSwitchImageView
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x03b5
            return
        L_0x03b5:
            r0 = r3[r15]     // Catch:{ all -> 0x04b5 }
            int[] r0 = (int[]) r0     // Catch:{ all -> 0x04b5 }
            r2 = r3[r11]     // Catch:{ all -> 0x04b5 }
            java.lang.Boolean r2 = (java.lang.Boolean) r2     // Catch:{ all -> 0x04b5 }
            boolean r2 = r2.booleanValue()     // Catch:{ all -> 0x04b5 }
            r4 = r3[r8]     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.Components.RLottieImageView r4 = (org.telegram.ui.Components.RLottieImageView) r4     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.ActionBar.DrawerLayoutContainer r7 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            int r7 = r7.getMeasuredWidth()     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.ActionBar.DrawerLayoutContainer r8 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            int r8 = r8.getMeasuredHeight()     // Catch:{ all -> 0x04b5 }
            if (r2 != 0) goto L_0x03d6
            r4.setVisibility(r11)     // Catch:{ all -> 0x04b5 }
        L_0x03d6:
            org.telegram.ui.ActionBar.DrawerLayoutContainer r9 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            int r9 = r9.getMeasuredWidth()     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.ActionBar.DrawerLayoutContainer r10 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            int r10 = r10.getMeasuredHeight()     // Catch:{ all -> 0x04b5 }
            android.graphics.Bitmap$Config r11 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ all -> 0x04b5 }
            android.graphics.Bitmap r9 = android.graphics.Bitmap.createBitmap(r9, r10, r11)     // Catch:{ all -> 0x04b5 }
            android.graphics.Canvas r10 = new android.graphics.Canvas     // Catch:{ all -> 0x04b5 }
            r10.<init>(r9)     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.ActionBar.DrawerLayoutContainer r11 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            r11.draw(r10)     // Catch:{ all -> 0x04b5 }
            android.widget.FrameLayout r10 = r1.frameLayout     // Catch:{ all -> 0x04b5 }
            android.widget.ImageView r11 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
            r10.removeView(r11)     // Catch:{ all -> 0x04b5 }
            r10 = -1082130432(0xffffffffbvar_, float:-1.0)
            r11 = -1
            if (r2 == 0) goto L_0x0411
            android.widget.FrameLayout r13 = r1.frameLayout     // Catch:{ all -> 0x04b5 }
            android.widget.ImageView r14 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r10)     // Catch:{ all -> 0x04b5 }
            r13.addView(r14, r5, r10)     // Catch:{ all -> 0x04b5 }
            android.view.View r10 = r1.themeSwitchSunView     // Catch:{ all -> 0x04b5 }
            r11 = 8
            r10.setVisibility(r11)     // Catch:{ all -> 0x04b5 }
            goto L_0x0442
        L_0x0411:
            android.widget.FrameLayout r13 = r1.frameLayout     // Catch:{ all -> 0x04b5 }
            android.widget.ImageView r14 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
            android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r11, r10)     // Catch:{ all -> 0x04b5 }
            r13.addView(r14, r6, r10)     // Catch:{ all -> 0x04b5 }
            android.view.View r10 = r1.themeSwitchSunView     // Catch:{ all -> 0x04b5 }
            r11 = r0[r5]     // Catch:{ all -> 0x04b5 }
            r13 = 1096810496(0x41600000, float:14.0)
            int r14 = org.telegram.messenger.AndroidUtilities.dp(r13)     // Catch:{ all -> 0x04b5 }
            int r11 = r11 - r14
            float r11 = (float) r11     // Catch:{ all -> 0x04b5 }
            r10.setTranslationX(r11)     // Catch:{ all -> 0x04b5 }
            android.view.View r10 = r1.themeSwitchSunView     // Catch:{ all -> 0x04b5 }
            r11 = r0[r6]     // Catch:{ all -> 0x04b5 }
            int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)     // Catch:{ all -> 0x04b5 }
            int r11 = r11 - r13
            float r11 = (float) r11     // Catch:{ all -> 0x04b5 }
            r10.setTranslationY(r11)     // Catch:{ all -> 0x04b5 }
            android.view.View r10 = r1.themeSwitchSunView     // Catch:{ all -> 0x04b5 }
            r10.setVisibility(r5)     // Catch:{ all -> 0x04b5 }
            android.view.View r10 = r1.themeSwitchSunView     // Catch:{ all -> 0x04b5 }
            r10.invalidate()     // Catch:{ all -> 0x04b5 }
        L_0x0442:
            android.widget.ImageView r10 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
            r10.setImageBitmap(r9)     // Catch:{ all -> 0x04b5 }
            android.widget.ImageView r9 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
            r9.setVisibility(r5)     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.Components.RLottieDrawable r9 = r4.getAnimatedDrawable()     // Catch:{ all -> 0x04b5 }
            r1.themeSwitchSunDrawable = r9     // Catch:{ all -> 0x04b5 }
            r9 = r0[r5]     // Catch:{ all -> 0x04b5 }
            int r9 = r7 - r9
            r10 = r0[r5]     // Catch:{ all -> 0x04b5 }
            int r7 = r7 - r10
            int r9 = r9 * r7
            r7 = r0[r6]     // Catch:{ all -> 0x04b5 }
            int r7 = r8 - r7
            r10 = r0[r6]     // Catch:{ all -> 0x04b5 }
            int r10 = r8 - r10
            int r7 = r7 * r10
            int r9 = r9 + r7
            double r9 = (double) r9     // Catch:{ all -> 0x04b5 }
            double r9 = java.lang.Math.sqrt(r9)     // Catch:{ all -> 0x04b5 }
            r7 = r0[r5]     // Catch:{ all -> 0x04b5 }
            r11 = r0[r5]     // Catch:{ all -> 0x04b5 }
            int r7 = r7 * r11
            r11 = r0[r6]     // Catch:{ all -> 0x04b5 }
            int r11 = r8 - r11
            r13 = r0[r6]     // Catch:{ all -> 0x04b5 }
            int r8 = r8 - r13
            int r11 = r11 * r8
            int r7 = r7 + r11
            double r7 = (double) r7     // Catch:{ all -> 0x04b5 }
            double r7 = java.lang.Math.sqrt(r7)     // Catch:{ all -> 0x04b5 }
            double r7 = java.lang.Math.max(r9, r7)     // Catch:{ all -> 0x04b5 }
            float r7 = (float) r7     // Catch:{ all -> 0x04b5 }
            if (r2 == 0) goto L_0x048a
            org.telegram.ui.ActionBar.DrawerLayoutContainer r8 = r1.drawerLayoutContainer     // Catch:{ all -> 0x04b5 }
            goto L_0x048c
        L_0x048a:
            android.widget.ImageView r8 = r1.themeSwitchImageView     // Catch:{ all -> 0x04b5 }
        L_0x048c:
            r9 = r0[r5]     // Catch:{ all -> 0x04b5 }
            r0 = r0[r6]     // Catch:{ all -> 0x04b5 }
            r10 = 0
            if (r2 == 0) goto L_0x0495
            r11 = 0
            goto L_0x0496
        L_0x0495:
            r11 = r7
        L_0x0496:
            if (r2 == 0) goto L_0x0499
            goto L_0x049a
        L_0x0499:
            r7 = 0
        L_0x049a:
            android.animation.Animator r0 = android.view.ViewAnimationUtils.createCircularReveal(r8, r9, r0, r11, r7)     // Catch:{ all -> 0x04b5 }
            r7 = 400(0x190, double:1.976E-321)
            r0.setDuration(r7)     // Catch:{ all -> 0x04b5 }
            android.view.animation.Interpolator r7 = org.telegram.ui.Components.Easings.easeInOutQuad     // Catch:{ all -> 0x04b5 }
            r0.setInterpolator(r7)     // Catch:{ all -> 0x04b5 }
            org.telegram.ui.LaunchActivity$11 r7 = new org.telegram.ui.LaunchActivity$11     // Catch:{ all -> 0x04b5 }
            r7.<init>(r2, r4)     // Catch:{ all -> 0x04b5 }
            r0.addListener(r7)     // Catch:{ all -> 0x04b5 }
            r0.start()     // Catch:{ all -> 0x04b5 }
            r0 = 1
            goto L_0x04ce
        L_0x04b5:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            android.widget.ImageView r0 = r1.themeSwitchImageView     // Catch:{ Exception -> 0x04c9 }
            r2 = 0
            r0.setImageDrawable(r2)     // Catch:{ Exception -> 0x04c9 }
            android.widget.FrameLayout r0 = r1.frameLayout     // Catch:{ Exception -> 0x04c9 }
            android.widget.ImageView r2 = r1.themeSwitchImageView     // Catch:{ Exception -> 0x04c9 }
            r0.removeView(r2)     // Catch:{ Exception -> 0x04c9 }
            org.telegram.ui.Cells.DrawerProfileCell.switchingTheme = r5     // Catch:{ Exception -> 0x04c9 }
            goto L_0x04cd
        L_0x04c9:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x04cd:
            r0 = 0
        L_0x04ce:
            r2 = r3[r5]
            org.telegram.ui.ActionBar.Theme$ThemeInfo r2 = (org.telegram.ui.ActionBar.Theme.ThemeInfo) r2
            r4 = r3[r6]
            java.lang.Boolean r4 = (java.lang.Boolean) r4
            boolean r4 = r4.booleanValue()
            r3 = r3[r12]
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r1.actionBarLayout
            r5.animateThemedValues(r2, r3, r4, r0)
            boolean r5 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r5 == 0) goto L_0x06b5
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r1.layersActionBarLayout
            r5.animateThemedValues(r2, r3, r4, r0)
            org.telegram.ui.ActionBar.ActionBarLayout r5 = r1.rightActionBarLayout
            r5.animateThemedValues(r2, r3, r4, r0)
            goto L_0x06b5
        L_0x04f9:
            int r2 = org.telegram.messenger.NotificationCenter.notificationsCountUpdated
            if (r0 != r2) goto L_0x052a
            org.telegram.ui.Components.RecyclerListView r0 = r1.sideMenu
            if (r0 == 0) goto L_0x06b5
            r2 = r3[r5]
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r0 = r0.getChildCount()
        L_0x0509:
            if (r5 >= r0) goto L_0x06b5
            org.telegram.ui.Components.RecyclerListView r3 = r1.sideMenu
            android.view.View r3 = r3.getChildAt(r5)
            boolean r4 = r3 instanceof org.telegram.ui.Cells.DrawerUserCell
            if (r4 == 0) goto L_0x0527
            r4 = r3
            org.telegram.ui.Cells.DrawerUserCell r4 = (org.telegram.ui.Cells.DrawerUserCell) r4
            int r4 = r4.getAccountNumber()
            int r6 = r2.intValue()
            if (r4 != r6) goto L_0x0527
            r3.invalidate()
            goto L_0x06b5
        L_0x0527:
            int r5 = r5 + 1
            goto L_0x0509
        L_0x052a:
            int r2 = org.telegram.messenger.NotificationCenter.needShowPlayServicesAlert
            if (r0 != r2) goto L_0x0539
            r0 = r3[r5]     // Catch:{ all -> 0x06b5 }
            com.google.android.gms.common.api.Status r0 = (com.google.android.gms.common.api.Status) r0     // Catch:{ all -> 0x06b5 }
            r2 = 140(0x8c, float:1.96E-43)
            r0.startResolutionForResult(r1, r2)     // Catch:{ all -> 0x06b5 }
            goto L_0x06b5
        L_0x0539:
            int r2 = org.telegram.messenger.NotificationCenter.fileDidLoad
            if (r0 != r2) goto L_0x05fc
            java.lang.String r0 = r1.loadingThemeFileName
            if (r0 == 0) goto L_0x05c7
            r2 = r3[r5]
            java.lang.String r2 = (java.lang.String) r2
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x06b5
            r2 = 0
            r1.loadingThemeFileName = r2
            java.io.File r0 = new java.io.File
            java.io.File r2 = org.telegram.messenger.ApplicationLoader.getFilesDirFixed()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "remote"
            r3.append(r4)
            org.telegram.tgnet.TLRPC$TL_theme r4 = r1.loadingTheme
            long r4 = r4.id
            r3.append(r4)
            java.lang.String r4 = ".attheme"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            r0.<init>(r2, r3)
            org.telegram.tgnet.TLRPC$TL_theme r2 = r1.loadingTheme
            java.lang.String r3 = r2.title
            org.telegram.ui.ActionBar.Theme$ThemeInfo r2 = org.telegram.ui.ActionBar.Theme.fillThemeValues(r0, r3, r2)
            if (r2 == 0) goto L_0x05c2
            java.lang.String r3 = r2.pathToWallpaper
            if (r3 == 0) goto L_0x05ab
            java.io.File r3 = new java.io.File
            java.lang.String r4 = r2.pathToWallpaper
            r3.<init>(r4)
            boolean r3 = r3.exists()
            if (r3 != 0) goto L_0x05ab
            org.telegram.tgnet.TLRPC$TL_account_getWallPaper r0 = new org.telegram.tgnet.TLRPC$TL_account_getWallPaper
            r0.<init>()
            org.telegram.tgnet.TLRPC$TL_inputWallPaperSlug r3 = new org.telegram.tgnet.TLRPC$TL_inputWallPaperSlug
            r3.<init>()
            java.lang.String r4 = r2.slug
            r3.slug = r4
            r0.wallpaper = r3
            int r3 = r2.account
            org.telegram.tgnet.ConnectionsManager r3 = org.telegram.tgnet.ConnectionsManager.getInstance(r3)
            org.telegram.ui.-$$Lambda$LaunchActivity$DjRqksnHFWXNPJJVV5f_Ep2g8-I r4 = new org.telegram.ui.-$$Lambda$LaunchActivity$DjRqksnHFWXNPJJVV5f_Ep2g8-I
            r4.<init>(r2)
            r3.sendRequest(r0, r4)
            return
        L_0x05ab:
            org.telegram.tgnet.TLRPC$TL_theme r2 = r1.loadingTheme
            java.lang.String r3 = r2.title
            org.telegram.ui.ActionBar.Theme$ThemeInfo r8 = org.telegram.ui.ActionBar.Theme.applyThemeFile(r0, r3, r2, r6)
            if (r8 == 0) goto L_0x05c2
            org.telegram.ui.ThemePreviewActivity r0 = new org.telegram.ui.ThemePreviewActivity
            r9 = 1
            r10 = 0
            r11 = 0
            r12 = 0
            r7 = r0
            r7.<init>(r8, r9, r10, r11, r12)
            r1.lambda$runLinkRequest$41(r0)
        L_0x05c2:
            r16.onThemeLoadFinish()
            goto L_0x06b5
        L_0x05c7:
            java.lang.String r0 = r1.loadingThemeWallpaperName
            if (r0 == 0) goto L_0x06b5
            r2 = r3[r5]
            java.lang.String r2 = (java.lang.String) r2
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x06b5
            r2 = 0
            r1.loadingThemeWallpaperName = r2
            r0 = r3[r6]
            java.io.File r0 = (java.io.File) r0
            boolean r2 = r1.loadingThemeAccent
            if (r2 == 0) goto L_0x05ee
            org.telegram.tgnet.TLRPC$TL_theme r0 = r1.loadingTheme
            org.telegram.tgnet.TLRPC$TL_wallPaper r2 = r1.loadingThemeWallpaper
            org.telegram.ui.ActionBar.Theme$ThemeInfo r3 = r1.loadingThemeInfo
            r1.openThemeAccentPreview(r0, r2, r3)
            r16.onThemeLoadFinish()
            goto L_0x06b5
        L_0x05ee:
            org.telegram.ui.ActionBar.Theme$ThemeInfo r2 = r1.loadingThemeInfo
            org.telegram.messenger.DispatchQueue r3 = org.telegram.messenger.Utilities.globalQueue
            org.telegram.ui.-$$Lambda$LaunchActivity$xVGtG59-qSVwAX72hqav3yEKPE8 r4 = new org.telegram.ui.-$$Lambda$LaunchActivity$xVGtG59-qSVwAX72hqav3yEKPE8
            r4.<init>(r2, r0)
            r3.postRunnable(r4)
            goto L_0x06b5
        L_0x05fc:
            int r2 = org.telegram.messenger.NotificationCenter.fileDidFailToLoad
            if (r0 != r2) goto L_0x0619
            r0 = r3[r5]
            java.lang.String r0 = (java.lang.String) r0
            java.lang.String r2 = r1.loadingThemeFileName
            boolean r2 = r0.equals(r2)
            if (r2 != 0) goto L_0x0614
            java.lang.String r2 = r1.loadingThemeWallpaperName
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x06b5
        L_0x0614:
            r16.onThemeLoadFinish()
            goto L_0x06b5
        L_0x0619:
            int r2 = org.telegram.messenger.NotificationCenter.screenStateChanged
            if (r0 != r2) goto L_0x0630
            boolean r0 = org.telegram.messenger.ApplicationLoader.mainInterfacePaused
            if (r0 == 0) goto L_0x0622
            return
        L_0x0622:
            boolean r0 = org.telegram.messenger.ApplicationLoader.isScreenOn
            if (r0 == 0) goto L_0x062b
            r16.onPasscodeResume()
            goto L_0x06b5
        L_0x062b:
            r16.onPasscodePause()
            goto L_0x06b5
        L_0x0630:
            int r2 = org.telegram.messenger.NotificationCenter.needCheckSystemBarColors
            if (r0 != r2) goto L_0x0639
            r16.checkSystemBarColors()
            goto L_0x06b5
        L_0x0639:
            int r2 = org.telegram.messenger.NotificationCenter.historyImportProgressChanged
            if (r0 != r2) goto L_0x0665
            int r0 = r3.length
            if (r0 <= r6) goto L_0x06b5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x06b5
            int r0 = r1.currentAccount
            r2 = r3[r15]
            org.telegram.tgnet.TLRPC$TL_error r2 = (org.telegram.tgnet.TLRPC$TL_error) r2
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r4 = mainFragmentsStack
            int r7 = r4.size()
            int r7 = r7 - r6
            java.lang.Object r4 = r4.get(r7)
            org.telegram.ui.ActionBar.BaseFragment r4 = (org.telegram.ui.ActionBar.BaseFragment) r4
            r3 = r3[r6]
            org.telegram.tgnet.TLObject r3 = (org.telegram.tgnet.TLObject) r3
            java.lang.Object[] r5 = new java.lang.Object[r5]
            org.telegram.ui.Components.AlertsCreator.processError(r0, r2, r4, r3, r5)
            goto L_0x06b5
        L_0x0665:
            int r2 = org.telegram.messenger.NotificationCenter.showBulletin
            if (r0 != r2) goto L_0x06b5
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x06b5
            r0 = r3[r5]
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r2 = mainFragmentsStack
            int r4 = r2.size()
            int r4 = r4 - r6
            java.lang.Object r2 = r2.get(r4)
            org.telegram.ui.ActionBar.BaseFragment r2 = (org.telegram.ui.ActionBar.BaseFragment) r2
            if (r0 != 0) goto L_0x06a4
            r0 = r3[r6]
            org.telegram.tgnet.TLRPC$Document r0 = (org.telegram.tgnet.TLRPC$Document) r0
            org.telegram.ui.Components.StickerSetBulletinLayout r4 = new org.telegram.ui.Components.StickerSetBulletinLayout
            r3 = r3[r15]
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            r5 = 0
            r4.<init>(r1, r5, r3, r0)
            r0 = 1500(0x5dc, float:2.102E-42)
            org.telegram.ui.Components.Bulletin r0 = org.telegram.ui.Components.Bulletin.make((org.telegram.ui.ActionBar.BaseFragment) r2, (org.telegram.ui.Components.Bulletin.Layout) r4, (int) r0)
            r0.show()
            goto L_0x06b5
        L_0x06a4:
            if (r0 != r6) goto L_0x06b5
            org.telegram.ui.Components.BulletinFactory r0 = org.telegram.ui.Components.BulletinFactory.of((org.telegram.ui.ActionBar.BaseFragment) r2)
            r2 = r3[r6]
            java.lang.String r2 = (java.lang.String) r2
            org.telegram.ui.Components.Bulletin r0 = r0.createErrorBulletin(r2)
            r0.show()
        L_0x06b5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.didReceivedNotification(int, int, java.lang.Object[]):void");
    }

    static /* synthetic */ void lambda$didReceivedNotification$62(int i, DialogInterface dialogInterface, int i2) {
        if (!mainFragmentsStack.isEmpty()) {
            MessagesController instance = MessagesController.getInstance(i);
            ArrayList<BaseFragment> arrayList = mainFragmentsStack;
            instance.openByUserName("spambot", arrayList.get(arrayList.size() - 1), 1);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didReceivedNotification$63 */
    public /* synthetic */ void lambda$didReceivedNotification$63$LaunchActivity(DialogInterface dialogInterface, int i) {
        MessagesController.getInstance(this.currentAccount).performLogout(2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didReceivedNotification$65 */
    public /* synthetic */ void lambda$didReceivedNotification$65$LaunchActivity(HashMap hashMap, int i, DialogInterface dialogInterface, int i2) {
        if (!mainFragmentsStack.isEmpty()) {
            ArrayList<BaseFragment> arrayList = mainFragmentsStack;
            if (AndroidUtilities.isGoogleMapsInstalled(arrayList.get(arrayList.size() - 1))) {
                LocationActivity locationActivity = new LocationActivity(0);
                locationActivity.setDelegate(new LocationActivity.LocationActivityDelegate(hashMap, i) {
                    public final /* synthetic */ HashMap f$0;
                    public final /* synthetic */ int f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void didSelectLocation(TLRPC$MessageMedia tLRPC$MessageMedia, int i, boolean z, int i2) {
                        LaunchActivity.lambda$null$64(this.f$0, this.f$1, tLRPC$MessageMedia, i, z, i2);
                    }
                });
                lambda$runLinkRequest$41(locationActivity);
            }
        }
    }

    static /* synthetic */ void lambda$null$64(HashMap hashMap, int i, TLRPC$MessageMedia tLRPC$MessageMedia, int i2, boolean z, int i3) {
        for (Map.Entry value : hashMap.entrySet()) {
            MessageObject messageObject = (MessageObject) value.getValue();
            SendMessagesHelper.getInstance(i).sendMessage(tLRPC$MessageMedia, messageObject.getDialogId(), messageObject, (MessageObject) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, z, i3);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didReceivedNotification$70 */
    public /* synthetic */ void lambda$didReceivedNotification$70$LaunchActivity(Theme.ThemeInfo themeInfo, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLObject, themeInfo) {
            public final /* synthetic */ TLObject f$1;
            public final /* synthetic */ Theme.ThemeInfo f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$69$LaunchActivity(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$69 */
    public /* synthetic */ void lambda$null$69$LaunchActivity(TLObject tLObject, Theme.ThemeInfo themeInfo) {
        if (tLObject instanceof TLRPC$TL_wallPaper) {
            TLRPC$TL_wallPaper tLRPC$TL_wallPaper = (TLRPC$TL_wallPaper) tLObject;
            this.loadingThemeInfo = themeInfo;
            this.loadingThemeWallpaperName = FileLoader.getAttachFileName(tLRPC$TL_wallPaper.document);
            this.loadingThemeWallpaper = tLRPC$TL_wallPaper;
            FileLoader.getInstance(themeInfo.account).loadFile(tLRPC$TL_wallPaper.document, tLRPC$TL_wallPaper, 1, 1);
            return;
        }
        onThemeLoadFinish();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didReceivedNotification$72 */
    public /* synthetic */ void lambda$didReceivedNotification$72$LaunchActivity(Theme.ThemeInfo themeInfo, File file) {
        themeInfo.createBackground(file, themeInfo.pathToWallpaper);
        AndroidUtilities.runOnUIThread(new Runnable() {
            public final void run() {
                LaunchActivity.this.lambda$null$71$LaunchActivity();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$71 */
    public /* synthetic */ void lambda$null$71$LaunchActivity() {
        if (this.loadingTheme != null) {
            File filesDirFixed = ApplicationLoader.getFilesDirFixed();
            File file = new File(filesDirFixed, "remote" + this.loadingTheme.id + ".attheme");
            TLRPC$TL_theme tLRPC$TL_theme = this.loadingTheme;
            Theme.ThemeInfo applyThemeFile = Theme.applyThemeFile(file, tLRPC$TL_theme.title, tLRPC$TL_theme, true);
            if (applyThemeFile != null) {
                lambda$runLinkRequest$41(new ThemePreviewActivity(applyThemeFile, true, 0, false, false));
            }
            onThemeLoadFinish();
        }
    }

    private String getStringForLanguageAlert(HashMap<String, String> hashMap, String str, int i) {
        String str2 = hashMap.get(str);
        return str2 == null ? LocaleController.getString(str, i) : str2;
    }

    private void openThemeAccentPreview(TLRPC$TL_theme tLRPC$TL_theme, TLRPC$TL_wallPaper tLRPC$TL_wallPaper, Theme.ThemeInfo themeInfo) {
        int i = themeInfo.lastAccentId;
        Theme.ThemeAccent createNewAccent = themeInfo.createNewAccent(tLRPC$TL_theme, this.currentAccount);
        themeInfo.prevAccentId = themeInfo.currentAccentId;
        themeInfo.setCurrentAccentId(createNewAccent.id);
        createNewAccent.pattern = tLRPC$TL_wallPaper;
        lambda$runLinkRequest$41(new ThemePreviewActivity(themeInfo, i != themeInfo.lastAccentId, 0, false, false));
    }

    private void onThemeLoadFinish() {
        AlertDialog alertDialog = this.loadingThemeProgressDialog;
        if (alertDialog != null) {
            try {
                alertDialog.dismiss();
            } finally {
                this.loadingThemeProgressDialog = null;
            }
        }
        this.loadingThemeWallpaperName = null;
        this.loadingThemeWallpaper = null;
        this.loadingThemeInfo = null;
        this.loadingThemeFileName = null;
        this.loadingTheme = null;
    }

    private void checkFreeDiscSpace() {
        SharedConfig.checkKeepMedia();
        if (Build.VERSION.SDK_INT < 26) {
            Utilities.globalQueue.postRunnable(new Runnable() {
                public final void run() {
                    LaunchActivity.this.lambda$checkFreeDiscSpace$74$LaunchActivity();
                }
            }, 2000);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$checkFreeDiscSpace$74 */
    public /* synthetic */ void lambda$checkFreeDiscSpace$74$LaunchActivity() {
        File directory;
        long j;
        if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
            try {
                SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                if (Math.abs(globalMainSettings.getLong("last_space_check", 0) - System.currentTimeMillis()) >= NUM && (directory = FileLoader.getDirectory(4)) != null) {
                    StatFs statFs = new StatFs(directory.getAbsolutePath());
                    if (Build.VERSION.SDK_INT < 18) {
                        j = (long) Math.abs(statFs.getAvailableBlocks() * statFs.getBlockSize());
                    } else {
                        j = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
                    }
                    if (j < NUM) {
                        globalMainSettings.edit().putLong("last_space_check", System.currentTimeMillis()).commit();
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public final void run() {
                                LaunchActivity.this.lambda$null$73$LaunchActivity();
                            }
                        });
                    }
                }
            } catch (Throwable unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$73 */
    public /* synthetic */ void lambda$null$73$LaunchActivity() {
        try {
            AlertsCreator.createFreeSpaceDialog(this).show();
        } catch (Throwable unused) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0054 A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0056 A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x005c A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005f A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0064 A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065 A[Catch:{ Exception -> 0x0115 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006d A[Catch:{ Exception -> 0x0115 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showLanguageAlertInternal(org.telegram.messenger.LocaleController.LocaleInfo r17, org.telegram.messenger.LocaleController.LocaleInfo r18, java.lang.String r19) {
        /*
            r16 = this;
            r1 = r16
            java.lang.String r0 = "ChooseYourLanguageOther"
            java.lang.String r2 = "ChooseYourLanguage"
            r3 = 0
            r1.loadingLocaleDialog = r3     // Catch:{ Exception -> 0x0115 }
            r4 = r17
            boolean r5 = r4.builtIn     // Catch:{ Exception -> 0x0115 }
            r6 = 1
            if (r5 != 0) goto L_0x001d
            org.telegram.messenger.LocaleController r5 = org.telegram.messenger.LocaleController.getInstance()     // Catch:{ Exception -> 0x0115 }
            boolean r5 = r5.isCurrentLocalLocale()     // Catch:{ Exception -> 0x0115 }
            if (r5 == 0) goto L_0x001b
            goto L_0x001d
        L_0x001b:
            r5 = 0
            goto L_0x001e
        L_0x001d:
            r5 = 1
        L_0x001e:
            org.telegram.ui.ActionBar.AlertDialog$Builder r7 = new org.telegram.ui.ActionBar.AlertDialog$Builder     // Catch:{ Exception -> 0x0115 }
            r7.<init>((android.content.Context) r1)     // Catch:{ Exception -> 0x0115 }
            java.util.HashMap<java.lang.String, java.lang.String> r8 = r1.systemLocaleStrings     // Catch:{ Exception -> 0x0115 }
            r9 = 2131624878(0x7f0e03ae, float:1.8876948E38)
            java.lang.String r8 = r1.getStringForLanguageAlert(r8, r2, r9)     // Catch:{ Exception -> 0x0115 }
            r7.setTitle(r8)     // Catch:{ Exception -> 0x0115 }
            java.util.HashMap<java.lang.String, java.lang.String> r8 = r1.englishLocaleStrings     // Catch:{ Exception -> 0x0115 }
            java.lang.String r2 = r1.getStringForLanguageAlert(r8, r2, r9)     // Catch:{ Exception -> 0x0115 }
            r7.setSubtitle(r2)     // Catch:{ Exception -> 0x0115 }
            android.widget.LinearLayout r2 = new android.widget.LinearLayout     // Catch:{ Exception -> 0x0115 }
            r2.<init>(r1)     // Catch:{ Exception -> 0x0115 }
            r2.setOrientation(r6)     // Catch:{ Exception -> 0x0115 }
            r8 = 2
            org.telegram.ui.Cells.LanguageCell[] r9 = new org.telegram.ui.Cells.LanguageCell[r8]     // Catch:{ Exception -> 0x0115 }
            org.telegram.messenger.LocaleController$LocaleInfo[] r10 = new org.telegram.messenger.LocaleController.LocaleInfo[r6]     // Catch:{ Exception -> 0x0115 }
            org.telegram.messenger.LocaleController$LocaleInfo[] r11 = new org.telegram.messenger.LocaleController.LocaleInfo[r8]     // Catch:{ Exception -> 0x0115 }
            java.util.HashMap<java.lang.String, java.lang.String> r12 = r1.systemLocaleStrings     // Catch:{ Exception -> 0x0115 }
            java.lang.String r13 = "English"
            r14 = 2131625296(0x7f0e0550, float:1.8877796E38)
            java.lang.String r12 = r1.getStringForLanguageAlert(r12, r13, r14)     // Catch:{ Exception -> 0x0115 }
            if (r5 == 0) goto L_0x0056
            r13 = r4
            goto L_0x0058
        L_0x0056:
            r13 = r18
        L_0x0058:
            r11[r3] = r13     // Catch:{ Exception -> 0x0115 }
            if (r5 == 0) goto L_0x005f
            r13 = r18
            goto L_0x0060
        L_0x005f:
            r13 = r4
        L_0x0060:
            r11[r6] = r13     // Catch:{ Exception -> 0x0115 }
            if (r5 == 0) goto L_0x0065
            goto L_0x0067
        L_0x0065:
            r4 = r18
        L_0x0067:
            r10[r3] = r4     // Catch:{ Exception -> 0x0115 }
            r4 = 0
        L_0x006a:
            r13 = -1
            if (r4 >= r8) goto L_0x00bf
            org.telegram.ui.Cells.LanguageCell r14 = new org.telegram.ui.Cells.LanguageCell     // Catch:{ Exception -> 0x0115 }
            r14.<init>(r1, r6)     // Catch:{ Exception -> 0x0115 }
            r9[r4] = r14     // Catch:{ Exception -> 0x0115 }
            r14 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            r15 = r11[r4]     // Catch:{ Exception -> 0x0115 }
            r3 = r11[r4]     // Catch:{ Exception -> 0x0115 }
            r5 = r18
            if (r3 != r5) goto L_0x0080
            r3 = r12
            goto L_0x0081
        L_0x0080:
            r3 = 0
        L_0x0081:
            r14.setLanguage(r15, r3, r6)     // Catch:{ Exception -> 0x0115 }
            r3 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r4)     // Catch:{ Exception -> 0x0115 }
            r3.setTag(r14)     // Catch:{ Exception -> 0x0115 }
            r3 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            java.lang.String r14 = "dialogButtonSelector"
            int r14 = org.telegram.ui.ActionBar.Theme.getColor(r14)     // Catch:{ Exception -> 0x0115 }
            android.graphics.drawable.Drawable r14 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r14, r8)     // Catch:{ Exception -> 0x0115 }
            r3.setBackgroundDrawable(r14)     // Catch:{ Exception -> 0x0115 }
            r3 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            if (r4 != 0) goto L_0x00a2
            r14 = 1
            goto L_0x00a3
        L_0x00a2:
            r14 = 0
        L_0x00a3:
            r3.setLanguageSelected(r14)     // Catch:{ Exception -> 0x0115 }
            r3 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            r14 = 50
            android.widget.LinearLayout$LayoutParams r13 = org.telegram.ui.Components.LayoutHelper.createLinear(r13, r14)     // Catch:{ Exception -> 0x0115 }
            r2.addView(r3, r13)     // Catch:{ Exception -> 0x0115 }
            r3 = r9[r4]     // Catch:{ Exception -> 0x0115 }
            org.telegram.ui.-$$Lambda$LaunchActivity$GzZv3SpdQcwOop2ZD24j4UDs0b8 r13 = new org.telegram.ui.-$$Lambda$LaunchActivity$GzZv3SpdQcwOop2ZD24j4UDs0b8     // Catch:{ Exception -> 0x0115 }
            r13.<init>(r10, r9)     // Catch:{ Exception -> 0x0115 }
            r3.setOnClickListener(r13)     // Catch:{ Exception -> 0x0115 }
            int r4 = r4 + 1
            r3 = 0
            goto L_0x006a
        L_0x00bf:
            org.telegram.ui.Cells.LanguageCell r3 = new org.telegram.ui.Cells.LanguageCell     // Catch:{ Exception -> 0x0115 }
            r3.<init>(r1, r6)     // Catch:{ Exception -> 0x0115 }
            java.util.HashMap<java.lang.String, java.lang.String> r4 = r1.systemLocaleStrings     // Catch:{ Exception -> 0x0115 }
            r5 = 2131624879(0x7f0e03af, float:1.887695E38)
            java.lang.String r4 = r1.getStringForLanguageAlert(r4, r0, r5)     // Catch:{ Exception -> 0x0115 }
            java.util.HashMap<java.lang.String, java.lang.String> r6 = r1.englishLocaleStrings     // Catch:{ Exception -> 0x0115 }
            java.lang.String r0 = r1.getStringForLanguageAlert(r6, r0, r5)     // Catch:{ Exception -> 0x0115 }
            r3.setValue(r4, r0)     // Catch:{ Exception -> 0x0115 }
            org.telegram.ui.-$$Lambda$LaunchActivity$ArO1NT8ZqCZlhD2rO0IVbTJrd2o r0 = new org.telegram.ui.-$$Lambda$LaunchActivity$ArO1NT8ZqCZlhD2rO0IVbTJrd2o     // Catch:{ Exception -> 0x0115 }
            r0.<init>()     // Catch:{ Exception -> 0x0115 }
            r3.setOnClickListener(r0)     // Catch:{ Exception -> 0x0115 }
            r0 = 50
            android.widget.LinearLayout$LayoutParams r0 = org.telegram.ui.Components.LayoutHelper.createLinear(r13, r0)     // Catch:{ Exception -> 0x0115 }
            r2.addView(r3, r0)     // Catch:{ Exception -> 0x0115 }
            r7.setView(r2)     // Catch:{ Exception -> 0x0115 }
            java.lang.String r0 = "OK"
            r2 = 2131626473(0x7f0e09e9, float:1.8880183E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r0, r2)     // Catch:{ Exception -> 0x0115 }
            org.telegram.ui.-$$Lambda$LaunchActivity$RDJjmjvX9b3dNBMFeBmYVriSiFk r2 = new org.telegram.ui.-$$Lambda$LaunchActivity$RDJjmjvX9b3dNBMFeBmYVriSiFk     // Catch:{ Exception -> 0x0115 }
            r2.<init>(r10)     // Catch:{ Exception -> 0x0115 }
            r7.setNegativeButton(r0, r2)     // Catch:{ Exception -> 0x0115 }
            org.telegram.ui.ActionBar.AlertDialog r0 = r1.showAlertDialog(r7)     // Catch:{ Exception -> 0x0115 }
            r1.localeDialog = r0     // Catch:{ Exception -> 0x0115 }
            android.content.SharedPreferences r0 = org.telegram.messenger.MessagesController.getGlobalMainSettings()     // Catch:{ Exception -> 0x0115 }
            android.content.SharedPreferences$Editor r0 = r0.edit()     // Catch:{ Exception -> 0x0115 }
            java.lang.String r2 = "language_showed2"
            r3 = r19
            android.content.SharedPreferences$Editor r0 = r0.putString(r2, r3)     // Catch:{ Exception -> 0x0115 }
            r0.commit()     // Catch:{ Exception -> 0x0115 }
            goto L_0x0119
        L_0x0115:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0119:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.showLanguageAlertInternal(org.telegram.messenger.LocaleController$LocaleInfo, org.telegram.messenger.LocaleController$LocaleInfo, java.lang.String):void");
    }

    static /* synthetic */ void lambda$showLanguageAlertInternal$75(LocaleController.LocaleInfo[] localeInfoArr, LanguageCell[] languageCellArr, View view) {
        Integer num = (Integer) view.getTag();
        localeInfoArr[0] = ((LanguageCell) view).getCurrentLocale();
        int i = 0;
        while (i < languageCellArr.length) {
            languageCellArr[i].setLanguageSelected(i == num.intValue());
            i++;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showLanguageAlertInternal$76 */
    public /* synthetic */ void lambda$showLanguageAlertInternal$76$LaunchActivity(View view) {
        this.localeDialog = null;
        this.drawerLayoutContainer.closeDrawer(true);
        lambda$runLinkRequest$41(new LanguageSelectActivity());
        AlertDialog alertDialog = this.visibleDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.visibleDialog = null;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showLanguageAlertInternal$77 */
    public /* synthetic */ void lambda$showLanguageAlertInternal$77$LaunchActivity(LocaleController.LocaleInfo[] localeInfoArr, DialogInterface dialogInterface, int i) {
        LocaleController.getInstance().applyLanguage(localeInfoArr[0], true, false, this.currentAccount);
        rebuildAllFragments(true);
    }

    private void showLanguageAlert(boolean z) {
        String str;
        try {
            if (this.loadingLocaleDialog) {
                return;
            }
            if (!ApplicationLoader.mainInterfacePaused) {
                String string = MessagesController.getGlobalMainSettings().getString("language_showed2", "");
                String str2 = MessagesController.getInstance(this.currentAccount).suggestedLangCode;
                if (z || !string.equals(str2)) {
                    LocaleController.LocaleInfo[] localeInfoArr = new LocaleController.LocaleInfo[2];
                    String str3 = str2.contains("-") ? str2.split("-")[0] : str2;
                    if ("in".equals(str3)) {
                        str = "id";
                    } else if ("iw".equals(str3)) {
                        str = "he";
                    } else {
                        str = "jw".equals(str3) ? "jv" : null;
                    }
                    int i = 0;
                    while (true) {
                        if (i >= LocaleController.getInstance().languages.size()) {
                            break;
                        }
                        LocaleController.LocaleInfo localeInfo = LocaleController.getInstance().languages.get(i);
                        if (localeInfo.shortName.equals("en")) {
                            localeInfoArr[0] = localeInfo;
                        }
                        if (localeInfo.shortName.replace("_", "-").equals(str2) || localeInfo.shortName.equals(str3) || localeInfo.shortName.equals(str)) {
                            localeInfoArr[1] = localeInfo;
                        }
                        if (localeInfoArr[0] != null && localeInfoArr[1] != null) {
                            break;
                        }
                        i++;
                    }
                    if (localeInfoArr[0] != null && localeInfoArr[1] != null) {
                        if (localeInfoArr[0] != localeInfoArr[1]) {
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("show lang alert for " + localeInfoArr[0].getKey() + " and " + localeInfoArr[1].getKey());
                            }
                            this.systemLocaleStrings = null;
                            this.englishLocaleStrings = null;
                            this.loadingLocaleDialog = true;
                            TLRPC$TL_langpack_getStrings tLRPC$TL_langpack_getStrings = new TLRPC$TL_langpack_getStrings();
                            tLRPC$TL_langpack_getStrings.lang_code = localeInfoArr[1].getLangCode();
                            tLRPC$TL_langpack_getStrings.keys.add("English");
                            tLRPC$TL_langpack_getStrings.keys.add("ChooseYourLanguage");
                            tLRPC$TL_langpack_getStrings.keys.add("ChooseYourLanguageOther");
                            tLRPC$TL_langpack_getStrings.keys.add("ChangeLanguageLater");
                            ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_langpack_getStrings, new RequestDelegate(localeInfoArr, str2) {
                                public final /* synthetic */ LocaleController.LocaleInfo[] f$1;
                                public final /* synthetic */ String f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                                    LaunchActivity.this.lambda$showLanguageAlert$79$LaunchActivity(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
                                }
                            }, 8);
                            TLRPC$TL_langpack_getStrings tLRPC$TL_langpack_getStrings2 = new TLRPC$TL_langpack_getStrings();
                            tLRPC$TL_langpack_getStrings2.lang_code = localeInfoArr[0].getLangCode();
                            tLRPC$TL_langpack_getStrings2.keys.add("English");
                            tLRPC$TL_langpack_getStrings2.keys.add("ChooseYourLanguage");
                            tLRPC$TL_langpack_getStrings2.keys.add("ChooseYourLanguageOther");
                            tLRPC$TL_langpack_getStrings2.keys.add("ChangeLanguageLater");
                            ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_langpack_getStrings2, new RequestDelegate(localeInfoArr, str2) {
                                public final /* synthetic */ LocaleController.LocaleInfo[] f$1;
                                public final /* synthetic */ String f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                                    LaunchActivity.this.lambda$showLanguageAlert$81$LaunchActivity(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
                                }
                            }, 8);
                        }
                    }
                } else if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("alert already showed for " + string);
                }
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showLanguageAlert$79 */
    public /* synthetic */ void lambda$showLanguageAlert$79$LaunchActivity(LocaleController.LocaleInfo[] localeInfoArr, String str, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        HashMap hashMap = new HashMap();
        if (tLObject != null) {
            TLRPC$Vector tLRPC$Vector = (TLRPC$Vector) tLObject;
            for (int i = 0; i < tLRPC$Vector.objects.size(); i++) {
                TLRPC$LangPackString tLRPC$LangPackString = (TLRPC$LangPackString) tLRPC$Vector.objects.get(i);
                hashMap.put(tLRPC$LangPackString.key, tLRPC$LangPackString.value);
            }
        }
        AndroidUtilities.runOnUIThread(new Runnable(hashMap, localeInfoArr, str) {
            public final /* synthetic */ HashMap f$1;
            public final /* synthetic */ LocaleController.LocaleInfo[] f$2;
            public final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$78$LaunchActivity(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$78 */
    public /* synthetic */ void lambda$null$78$LaunchActivity(HashMap hashMap, LocaleController.LocaleInfo[] localeInfoArr, String str) {
        this.systemLocaleStrings = hashMap;
        if (this.englishLocaleStrings != null && hashMap != null) {
            showLanguageAlertInternal(localeInfoArr[1], localeInfoArr[0], str);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showLanguageAlert$81 */
    public /* synthetic */ void lambda$showLanguageAlert$81$LaunchActivity(LocaleController.LocaleInfo[] localeInfoArr, String str, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        HashMap hashMap = new HashMap();
        if (tLObject != null) {
            TLRPC$Vector tLRPC$Vector = (TLRPC$Vector) tLObject;
            for (int i = 0; i < tLRPC$Vector.objects.size(); i++) {
                TLRPC$LangPackString tLRPC$LangPackString = (TLRPC$LangPackString) tLRPC$Vector.objects.get(i);
                hashMap.put(tLRPC$LangPackString.key, tLRPC$LangPackString.value);
            }
        }
        AndroidUtilities.runOnUIThread(new Runnable(hashMap, localeInfoArr, str) {
            public final /* synthetic */ HashMap f$1;
            public final /* synthetic */ LocaleController.LocaleInfo[] f$2;
            public final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                LaunchActivity.this.lambda$null$80$LaunchActivity(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$80 */
    public /* synthetic */ void lambda$null$80$LaunchActivity(HashMap hashMap, LocaleController.LocaleInfo[] localeInfoArr, String str) {
        this.englishLocaleStrings = hashMap;
        if (hashMap != null && this.systemLocaleStrings != null) {
            showLanguageAlertInternal(localeInfoArr[1], localeInfoArr[0], str);
        }
    }

    private void onPasscodePause() {
        if (this.lockRunnable != null) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("cancel lockRunnable onPasscodePause");
            }
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (SharedConfig.passcodeHash.length() != 0) {
            SharedConfig.lastPauseTime = (int) (SystemClock.elapsedRealtime() / 1000);
            AnonymousClass12 r0 = new Runnable() {
                public void run() {
                    if (LaunchActivity.this.lockRunnable == this) {
                        if (AndroidUtilities.needShowPasscode(true)) {
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("lock app");
                            }
                            LaunchActivity.this.showPasscodeActivity();
                        } else if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("didn't pass lock check");
                        }
                        Runnable unused = LaunchActivity.this.lockRunnable = null;
                    }
                }
            };
            this.lockRunnable = r0;
            if (SharedConfig.appLocked) {
                AndroidUtilities.runOnUIThread(r0, 1000);
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("schedule app lock in 1000");
                }
            } else if (SharedConfig.autoLockIn != 0) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("schedule app lock in " + ((((long) SharedConfig.autoLockIn) * 1000) + 1000));
                }
                AndroidUtilities.runOnUIThread(this.lockRunnable, (((long) SharedConfig.autoLockIn) * 1000) + 1000);
            }
        } else {
            SharedConfig.lastPauseTime = 0;
        }
        SharedConfig.saveConfig();
    }

    private void onPasscodeResume() {
        if (this.lockRunnable != null) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("cancel lockRunnable onPasscodeResume");
            }
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (AndroidUtilities.needShowPasscode(true)) {
            showPasscodeActivity();
        }
        if (SharedConfig.lastPauseTime != 0) {
            SharedConfig.lastPauseTime = 0;
            SharedConfig.saveConfig();
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("reset lastPauseTime onPasscodeResume");
            }
        }
    }

    private void updateCurrentConnectionState(int i) {
        String str;
        if (this.actionBarLayout != null) {
            int i2 = 0;
            int connectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
            this.currentConnectionState = connectionState;
            $$Lambda$LaunchActivity$XHl69xs9YAkfuP4uNzotSBE4NOM r4 = null;
            if (connectionState == 2) {
                i2 = NUM;
                str = "WaitingForNetwork";
            } else if (connectionState == 5) {
                i2 = NUM;
                str = "Updating";
            } else if (connectionState == 4) {
                i2 = NUM;
                str = "ConnectingToProxy";
            } else if (connectionState == 1) {
                i2 = NUM;
                str = "Connecting";
            } else {
                str = null;
            }
            if (connectionState == 1 || connectionState == 4) {
                r4 = new Runnable() {
                    public final void run() {
                        LaunchActivity.this.lambda$updateCurrentConnectionState$82$LaunchActivity();
                    }
                };
            }
            this.actionBarLayout.setTitleOverlayText(str, i2, r4);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    /* renamed from: lambda$updateCurrentConnectionState$82 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$updateCurrentConnectionState$82$LaunchActivity() {
        /*
            r2 = this;
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 == 0) goto L_0x001d
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = layerFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0034
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = layerFragmentsStack
            int r1 = r0.size()
            int r1 = r1 + -1
            java.lang.Object r0 = r0.get(r1)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            goto L_0x0035
        L_0x001d:
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0034
            java.util.ArrayList<org.telegram.ui.ActionBar.BaseFragment> r0 = mainFragmentsStack
            int r1 = r0.size()
            int r1 = r1 + -1
            java.lang.Object r0 = r0.get(r1)
            org.telegram.ui.ActionBar.BaseFragment r0 = (org.telegram.ui.ActionBar.BaseFragment) r0
            goto L_0x0035
        L_0x0034:
            r0 = 0
        L_0x0035:
            boolean r1 = r0 instanceof org.telegram.ui.ProxyListActivity
            if (r1 != 0) goto L_0x0046
            boolean r0 = r0 instanceof org.telegram.ui.ProxySettingsActivity
            if (r0 == 0) goto L_0x003e
            goto L_0x0046
        L_0x003e:
            org.telegram.ui.ProxyListActivity r0 = new org.telegram.ui.ProxyListActivity
            r0.<init>()
            r2.lambda$runLinkRequest$41(r0)
        L_0x0046:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.lambda$updateCurrentConnectionState$82$LaunchActivity():void");
    }

    public void hideVisibleActionMode() {
        ActionMode actionMode = this.visibleActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle bundle) {
        try {
            super.onSaveInstanceState(bundle);
            BaseFragment baseFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    ArrayList<BaseFragment> arrayList = this.layersActionBarLayout.fragmentsStack;
                    baseFragment = arrayList.get(arrayList.size() - 1);
                } else if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    ArrayList<BaseFragment> arrayList2 = this.rightActionBarLayout.fragmentsStack;
                    baseFragment = arrayList2.get(arrayList2.size() - 1);
                } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    ArrayList<BaseFragment> arrayList3 = this.actionBarLayout.fragmentsStack;
                    baseFragment = arrayList3.get(arrayList3.size() - 1);
                }
            } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                ArrayList<BaseFragment> arrayList4 = this.actionBarLayout.fragmentsStack;
                baseFragment = arrayList4.get(arrayList4.size() - 1);
            }
            if (baseFragment != null) {
                Bundle arguments = baseFragment.getArguments();
                if ((baseFragment instanceof ChatActivity) && arguments != null) {
                    bundle.putBundle("args", arguments);
                    bundle.putString("fragment", "chat");
                } else if ((baseFragment instanceof GroupCreateFinalActivity) && arguments != null) {
                    bundle.putBundle("args", arguments);
                    bundle.putString("fragment", "group");
                } else if (baseFragment instanceof WallpapersListActivity) {
                    bundle.putString("fragment", "wallpapers");
                } else if (baseFragment instanceof ProfileActivity) {
                    ProfileActivity profileActivity = (ProfileActivity) baseFragment;
                    if (profileActivity.isSettings()) {
                        bundle.putString("fragment", "settings");
                    } else if (profileActivity.isChat() && arguments != null) {
                        bundle.putBundle("args", arguments);
                        bundle.putString("fragment", "chat_profile");
                    }
                } else if ((baseFragment instanceof ChannelCreateActivity) && arguments != null && arguments.getInt("step") == 0) {
                    bundle.putBundle("args", arguments);
                    bundle.putString("fragment", "channel");
                }
                baseFragment.saveSelfArgs(bundle);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    public void onBackPressed() {
        if (this.passcodeView.getVisibility() == 0) {
            finish();
            return;
        }
        boolean z = false;
        if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
            SecretMediaViewer.getInstance().closePhoto(true, false);
        } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
            ArticleViewer.getInstance().close(true, false);
        } else if (this.drawerLayoutContainer.isDrawerOpened()) {
            this.drawerLayoutContainer.closeDrawer(false);
        } else if (!AndroidUtilities.isTablet()) {
            this.actionBarLayout.onBackPressed();
        } else if (this.layersActionBarLayout.getVisibility() == 0) {
            this.layersActionBarLayout.onBackPressed();
        } else {
            if (this.rightActionBarLayout.getVisibility() == 0 && !this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                ArrayList<BaseFragment> arrayList = this.rightActionBarLayout.fragmentsStack;
                z = !arrayList.get(arrayList.size() - 1).onBackPressed();
            }
            if (!z) {
                this.actionBarLayout.onBackPressed();
            }
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        ActionBarLayout actionBarLayout2 = this.actionBarLayout;
        if (actionBarLayout2 != null) {
            actionBarLayout2.onLowMemory();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onLowMemory();
                this.layersActionBarLayout.onLowMemory();
            }
        }
    }

    public void onActionModeStarted(ActionMode actionMode) {
        super.onActionModeStarted(actionMode);
        this.visibleActionMode = actionMode;
        try {
            Menu menu = actionMode.getMenu();
            if (menu != null && !this.actionBarLayout.extendActionMode(menu) && AndroidUtilities.isTablet() && !this.rightActionBarLayout.extendActionMode(menu)) {
                this.layersActionBarLayout.extendActionMode(menu);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        if (Build.VERSION.SDK_INT < 23 || actionMode.getType() != 1) {
            this.actionBarLayout.onActionModeStarted(actionMode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeStarted(actionMode);
                this.layersActionBarLayout.onActionModeStarted(actionMode);
            }
        }
    }

    public void onActionModeFinished(ActionMode actionMode) {
        super.onActionModeFinished(actionMode);
        if (this.visibleActionMode == actionMode) {
            this.visibleActionMode = null;
        }
        if (Build.VERSION.SDK_INT < 23 || actionMode.getType() != 1) {
            this.actionBarLayout.onActionModeFinished(actionMode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeFinished(actionMode);
                this.layersActionBarLayout.onActionModeFinished(actionMode);
            }
        }
    }

    public boolean onPreIme() {
        if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
            SecretMediaViewer.getInstance().closePhoto(true, false);
            return true;
        } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
            return true;
        } else if (!ArticleViewer.hasInstance() || !ArticleViewer.getInstance().isVisible()) {
            return false;
        } else {
            ArticleViewer.getInstance().close(true, false);
            return true;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        keyEvent.getKeyCode();
        if (VoIPService.getSharedInstance() == null && !mainFragmentsStack.isEmpty() && ((!PhotoViewer.hasInstance() || !PhotoViewer.getInstance().isVisible()) && keyEvent.getRepeatCount() == 0 && keyEvent.getAction() == 0 && (keyEvent.getKeyCode() == 24 || keyEvent.getKeyCode() == 25))) {
            ArrayList<BaseFragment> arrayList = mainFragmentsStack;
            BaseFragment baseFragment = arrayList.get(arrayList.size() - 1);
            if ((baseFragment instanceof ChatActivity) && ((ChatActivity) baseFragment).maybePlayVisibleVideo()) {
                return true;
            }
            if (AndroidUtilities.isTablet() && !rightFragmentsStack.isEmpty()) {
                ArrayList<BaseFragment> arrayList2 = rightFragmentsStack;
                BaseFragment baseFragment2 = arrayList2.get(arrayList2.size() - 1);
                if ((baseFragment2 instanceof ChatActivity) && ((ChatActivity) baseFragment2).maybePlayVisibleVideo()) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 82 && !SharedConfig.isWaitingForPasscodeEnter) {
            if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                return super.onKeyUp(i, keyEvent);
            }
            if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                return super.onKeyUp(i, keyEvent);
            }
            if (AndroidUtilities.isTablet()) {
                if (this.layersActionBarLayout.getVisibility() == 0 && !this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    this.layersActionBarLayout.onKeyUp(i, keyEvent);
                } else if (this.rightActionBarLayout.getVisibility() != 0 || this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    this.actionBarLayout.onKeyUp(i, keyEvent);
                } else {
                    this.rightActionBarLayout.onKeyUp(i, keyEvent);
                }
            } else if (this.actionBarLayout.fragmentsStack.size() != 1) {
                this.actionBarLayout.onKeyUp(i, keyEvent);
            } else if (!this.drawerLayoutContainer.isDrawerOpened()) {
                if (getCurrentFocus() != null) {
                    AndroidUtilities.hideKeyboard(getCurrentFocus());
                }
                this.drawerLayoutContainer.openDrawer(false);
            } else {
                this.drawerLayoutContainer.closeDrawer(false);
            }
        }
        return super.onKeyUp(i, keyEvent);
    }

    public boolean needPresentFragment(BaseFragment baseFragment, boolean z, boolean z2, ActionBarLayout actionBarLayout2) {
        boolean z3;
        ActionBarLayout actionBarLayout3;
        ActionBarLayout actionBarLayout4;
        ActionBarLayout actionBarLayout5;
        ActionBarLayout actionBarLayout6;
        if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
            ArticleViewer.getInstance().close(false, true);
        }
        if (AndroidUtilities.isTablet()) {
            boolean z4 = baseFragment instanceof LoginActivity;
            this.drawerLayoutContainer.setAllowOpenDrawer(!z4 && !(baseFragment instanceof CountrySelectActivity) && this.layersActionBarLayout.getVisibility() != 0, true);
            if ((baseFragment instanceof DialogsActivity) && ((DialogsActivity) baseFragment).isMainDialogList() && actionBarLayout2 != (actionBarLayout6 = this.actionBarLayout)) {
                actionBarLayout6.removeAllFragments();
                this.actionBarLayout.presentFragment(baseFragment, z, z2, false, false);
                this.layersActionBarLayout.removeAllFragments();
                this.layersActionBarLayout.setVisibility(8);
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                if (!this.tabletFullSize) {
                    this.shadowTabletSide.setVisibility(0);
                    if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        this.backgroundTablet.setVisibility(0);
                    }
                }
                return false;
            } else if (!(baseFragment instanceof ChatActivity) || ((ChatActivity) baseFragment).isInScheduleMode()) {
                ActionBarLayout actionBarLayout7 = this.layersActionBarLayout;
                if (actionBarLayout2 != actionBarLayout7) {
                    actionBarLayout7.setVisibility(0);
                    this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                    if (z4) {
                        this.backgroundTablet.setVisibility(0);
                        this.shadowTabletSide.setVisibility(8);
                        this.shadowTablet.setBackgroundColor(0);
                    } else {
                        this.shadowTablet.setBackgroundColor(NUM);
                    }
                    this.layersActionBarLayout.presentFragment(baseFragment, z, z2, false, false);
                    return false;
                }
            } else {
                boolean z5 = this.tabletFullSize;
                if ((!z5 && actionBarLayout2 == this.rightActionBarLayout) || (z5 && actionBarLayout2 == this.actionBarLayout)) {
                    boolean z6 = (z5 && actionBarLayout2 == (actionBarLayout3 = this.actionBarLayout) && actionBarLayout3.fragmentsStack.size() == 1) ? false : true;
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout8 = this.layersActionBarLayout;
                            actionBarLayout8.removeFragmentFromStack(actionBarLayout8.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(!z2);
                    }
                    if (!z6) {
                        this.actionBarLayout.presentFragment(baseFragment, false, z2, false, false);
                    }
                    return z6;
                } else if (!z5 && actionBarLayout2 != (actionBarLayout5 = this.rightActionBarLayout)) {
                    actionBarLayout5.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.presentFragment(baseFragment, z, true, false, false);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout9 = this.layersActionBarLayout;
                            actionBarLayout9.removeFragmentFromStack(actionBarLayout9.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(!z2);
                    }
                    return false;
                } else if (!z5 || actionBarLayout2 == (actionBarLayout4 = this.actionBarLayout)) {
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout10 = this.layersActionBarLayout;
                            actionBarLayout10.removeFragmentFromStack(actionBarLayout10.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(!z2);
                    }
                    ActionBarLayout actionBarLayout11 = this.actionBarLayout;
                    actionBarLayout11.presentFragment(baseFragment, actionBarLayout11.fragmentsStack.size() > 1, z2, false, false);
                    return false;
                } else {
                    actionBarLayout4.presentFragment(baseFragment, actionBarLayout4.fragmentsStack.size() > 1, z2, false, false);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout12 = this.layersActionBarLayout;
                            actionBarLayout12.removeFragmentFromStack(actionBarLayout12.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(!z2);
                    }
                    return false;
                }
            }
        } else {
            if (!(baseFragment instanceof LoginActivity) ? !(baseFragment instanceof CountrySelectActivity) || mainFragmentsStack.size() != 1 : mainFragmentsStack.size() != 0) {
                z3 = true;
            } else {
                z3 = false;
            }
            this.drawerLayoutContainer.setAllowOpenDrawer(z3, false);
        }
        return true;
    }

    public boolean needAddFragmentToStack(BaseFragment baseFragment, ActionBarLayout actionBarLayout2) {
        boolean z;
        ActionBarLayout actionBarLayout3;
        ActionBarLayout actionBarLayout4;
        ActionBarLayout actionBarLayout5;
        if (AndroidUtilities.isTablet()) {
            boolean z2 = baseFragment instanceof LoginActivity;
            this.drawerLayoutContainer.setAllowOpenDrawer(!z2 && !(baseFragment instanceof CountrySelectActivity) && this.layersActionBarLayout.getVisibility() != 0, true);
            if (baseFragment instanceof DialogsActivity) {
                if (((DialogsActivity) baseFragment).isMainDialogList() && actionBarLayout2 != (actionBarLayout5 = this.actionBarLayout)) {
                    actionBarLayout5.removeAllFragments();
                    this.actionBarLayout.addFragmentToStack(baseFragment);
                    this.layersActionBarLayout.removeAllFragments();
                    this.layersActionBarLayout.setVisibility(8);
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (!this.tabletFullSize) {
                        this.shadowTabletSide.setVisibility(0);
                        if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                            this.backgroundTablet.setVisibility(0);
                        }
                    }
                    return false;
                }
            } else if (!(baseFragment instanceof ChatActivity) || ((ChatActivity) baseFragment).isInScheduleMode()) {
                ActionBarLayout actionBarLayout6 = this.layersActionBarLayout;
                if (actionBarLayout2 != actionBarLayout6) {
                    actionBarLayout6.setVisibility(0);
                    this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                    if (z2) {
                        this.backgroundTablet.setVisibility(0);
                        this.shadowTabletSide.setVisibility(8);
                        this.shadowTablet.setBackgroundColor(0);
                    } else {
                        this.shadowTablet.setBackgroundColor(NUM);
                    }
                    this.layersActionBarLayout.addFragmentToStack(baseFragment);
                    return false;
                }
            } else {
                boolean z3 = this.tabletFullSize;
                if (!z3 && actionBarLayout2 != (actionBarLayout4 = this.rightActionBarLayout)) {
                    actionBarLayout4.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.addFragmentToStack(baseFragment);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout7 = this.layersActionBarLayout;
                            actionBarLayout7.removeFragmentFromStack(actionBarLayout7.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(true);
                    }
                    return false;
                } else if (z3 && actionBarLayout2 != (actionBarLayout3 = this.actionBarLayout)) {
                    actionBarLayout3.addFragmentToStack(baseFragment);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            ActionBarLayout actionBarLayout8 = this.layersActionBarLayout;
                            actionBarLayout8.removeFragmentFromStack(actionBarLayout8.fragmentsStack.get(0));
                        }
                        this.layersActionBarLayout.closeLastFragment(true);
                    }
                    return false;
                }
            }
        } else {
            if (!(baseFragment instanceof LoginActivity) ? !(baseFragment instanceof CountrySelectActivity) || mainFragmentsStack.size() != 1 : mainFragmentsStack.size() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.drawerLayoutContainer.setAllowOpenDrawer(z, false);
        }
        return true;
    }

    public boolean needCloseLastFragment(ActionBarLayout actionBarLayout2) {
        if (AndroidUtilities.isTablet()) {
            if (actionBarLayout2 == this.actionBarLayout && actionBarLayout2.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (actionBarLayout2 == this.rightActionBarLayout) {
                if (!this.tabletFullSize) {
                    this.backgroundTablet.setVisibility(0);
                }
            } else if (actionBarLayout2 == this.layersActionBarLayout && this.actionBarLayout.fragmentsStack.isEmpty() && this.layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else if (actionBarLayout2.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        } else if (actionBarLayout2.fragmentsStack.size() >= 2 && !(actionBarLayout2.fragmentsStack.get(0) instanceof LoginActivity)) {
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
        return true;
    }

    public void rebuildAllFragments(boolean z) {
        ActionBarLayout actionBarLayout2 = this.layersActionBarLayout;
        if (actionBarLayout2 != null) {
            actionBarLayout2.rebuildAllFragmentViews(z, z);
        } else {
            this.actionBarLayout.rebuildAllFragmentViews(z, z);
        }
    }

    public void onRebuildAllFragments(ActionBarLayout actionBarLayout2, boolean z) {
        if (AndroidUtilities.isTablet() && actionBarLayout2 == this.layersActionBarLayout) {
            this.rightActionBarLayout.rebuildAllFragmentViews(z, z);
            this.actionBarLayout.rebuildAllFragmentViews(z, z);
        }
        this.drawerLayoutAdapter.notifyDataSetChanged();
    }
}
