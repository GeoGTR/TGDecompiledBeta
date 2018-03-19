package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.hockeyapp.android.UpdateFragment;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocaleController.LocaleInfo;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SendMessagesHelper.SendingMediaInfo;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.source.ExtractorMediaSource;
import org.telegram.messenger.exoplayer2.trackselection.AdaptiveTrackSelection;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.support.widget.DefaultItemAnimator;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.LangPackString;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputGameShortName;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_langpack_getStrings;
import org.telegram.tgnet.TLRPC.TL_messages_checkChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.TL_userContact_old2;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarLayout.ActionBarLayoutDelegate;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.PasscodeView.PasscodeViewDelegate;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.SharingLocationsAlert;
import org.telegram.ui.Components.SharingLocationsAlert.SharingLocationsAlertDelegate;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.ThemeEditorView;
import org.telegram.ui.DialogsActivity.DialogsActivityDelegate;
import org.telegram.ui.LocationActivity.LocationActivityDelegate;

public class LaunchActivity extends Activity implements NotificationCenterDelegate, ActionBarLayoutDelegate, DialogsActivityDelegate {
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList();
    private ActionBarLayout actionBarLayout;
    private View backgroundTablet;
    private ArrayList<User> contactsToSend;
    private int currentAccount;
    private int currentConnectionState;
    private String documentsMimeType;
    private ArrayList<String> documentsOriginalPathsArray;
    private ArrayList<String> documentsPathsArray;
    private ArrayList<Uri> documentsUrisArray;
    private DrawerLayoutAdapter drawerLayoutAdapter;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private HashMap<String, String> englishLocaleStrings;
    private boolean finished;
    private ActionBarLayout layersActionBarLayout;
    private boolean loadingLocaleDialog;
    private AlertDialog localeDialog;
    private Runnable lockRunnable;
    private OnGlobalLayoutListener onGlobalLayoutListener;
    private Intent passcodeSaveIntent;
    private boolean passcodeSaveIntentIsNew;
    private boolean passcodeSaveIntentIsRestore;
    private PasscodeView passcodeView;
    private ArrayList<SendingMediaInfo> photoPathsArray;
    private ActionBarLayout rightActionBarLayout;
    private String sendingText;
    private FrameLayout shadowTablet;
    private FrameLayout shadowTabletSide;
    private RecyclerListView sideMenu;
    private HashMap<String, String> systemLocaleStrings;
    private boolean tabletFullSize;
    private String videoPath;
    private AlertDialog visibleDialog;

    private class VcardData {
        String name;
        ArrayList<String> phones;

        private VcardData() {
            this.phones = new ArrayList();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onCreate(Bundle savedInstanceState) {
        int dp;
        ApplicationLoader.postInitApplication();
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
        this.currentAccount = UserConfig.selectedAccount;
        if (!UserConfig.getInstance(this.currentAccount).isClientActivated()) {
            Intent intent = getIntent();
            if (intent == null || intent.getAction() == null || !("android.intent.action.SEND".equals(intent.getAction()) || intent.getAction().equals("android.intent.action.SEND_MULTIPLE"))) {
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                long crashed_time = preferences.getLong("intro_crashed_time", 0);
                boolean fromIntro = intent.getBooleanExtra("fromIntro", false);
                if (fromIntro) {
                    preferences.edit().putLong("intro_crashed_time", 0).commit();
                }
                if (Math.abs(crashed_time - System.currentTimeMillis()) >= 120000 && intent != null && !fromIntro && ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", 0).getAll().isEmpty()) {
                    Intent intent2 = new Intent(this, IntroActivity.class);
                    intent2.setData(intent.getData());
                    startActivity(intent2);
                    super.onCreate(savedInstanceState);
                    finish();
                    return;
                }
            }
            super.onCreate(savedInstanceState);
            finish();
            return;
        }
        requestWindowFeature(1);
        setTheme(R.style.Theme.TMessages);
        if (VERSION.SDK_INT >= 21) {
            try {
                setTaskDescription(new TaskDescription(null, null, Theme.getColor(Theme.key_actionBarDefault) | Theme.ACTION_BAR_VIDEO_EDIT_COLOR));
            } catch (Exception e) {
            }
        }
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        if (SharedConfig.passcodeHash.length() > 0 && !SharedConfig.allowScreenCapture) {
            try {
                getWindow().setFlags(MessagesController.UPDATE_MASK_CHANNEL, MessagesController.UPDATE_MASK_CHANNEL);
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        }
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 24) {
            AndroidUtilities.isInMultiwindow = isInMultiWindowMode();
        }
        Theme.createChatResources(this, false);
        if (SharedConfig.passcodeHash.length() != 0 && SharedConfig.appLocked) {
            SharedConfig.lastPauseTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        }
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        this.actionBarLayout = new ActionBarLayout(this);
        this.drawerLayoutContainer = new DrawerLayoutContainer(this);
        setContentView(this.drawerLayoutContainer, new LayoutParams(-1, -1));
        if (AndroidUtilities.isTablet()) {
            getWindow().setSoftInputMode(16);
            View anonymousClass1 = new RelativeLayout(this) {
                private boolean inLayout;

                public void requestLayout() {
                    if (!this.inLayout) {
                        super.requestLayout();
                    }
                }

                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    this.inLayout = true;
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    setMeasuredDimension(width, height);
                    if (AndroidUtilities.isInMultiwindow || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
                        LaunchActivity.this.tabletFullSize = true;
                        LaunchActivity.this.actionBarLayout.measure(MeasureSpec.makeMeasureSpec(width, NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                    } else {
                        LaunchActivity.this.tabletFullSize = false;
                        int leftWidth = (width / 100) * 35;
                        if (leftWidth < AndroidUtilities.dp(320.0f)) {
                            leftWidth = AndroidUtilities.dp(320.0f);
                        }
                        LaunchActivity.this.actionBarLayout.measure(MeasureSpec.makeMeasureSpec(leftWidth, NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                        LaunchActivity.this.shadowTabletSide.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1.0f), NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                        LaunchActivity.this.rightActionBarLayout.measure(MeasureSpec.makeMeasureSpec(width - leftWidth, NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                    }
                    LaunchActivity.this.backgroundTablet.measure(MeasureSpec.makeMeasureSpec(width, NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                    LaunchActivity.this.shadowTablet.measure(MeasureSpec.makeMeasureSpec(width, NUM), MeasureSpec.makeMeasureSpec(height, NUM));
                    LaunchActivity.this.layersActionBarLayout.measure(MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(530.0f), width), NUM), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(528.0f), height), NUM));
                    this.inLayout = false;
                }

                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    int width = r - l;
                    int height = b - t;
                    if (AndroidUtilities.isInMultiwindow || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
                        LaunchActivity.this.actionBarLayout.layout(0, 0, LaunchActivity.this.actionBarLayout.getMeasuredWidth(), LaunchActivity.this.actionBarLayout.getMeasuredHeight());
                    } else {
                        int leftWidth = (width / 100) * 35;
                        if (leftWidth < AndroidUtilities.dp(320.0f)) {
                            leftWidth = AndroidUtilities.dp(320.0f);
                        }
                        LaunchActivity.this.shadowTabletSide.layout(leftWidth, 0, LaunchActivity.this.shadowTabletSide.getMeasuredWidth() + leftWidth, LaunchActivity.this.shadowTabletSide.getMeasuredHeight());
                        LaunchActivity.this.actionBarLayout.layout(0, 0, LaunchActivity.this.actionBarLayout.getMeasuredWidth(), LaunchActivity.this.actionBarLayout.getMeasuredHeight());
                        LaunchActivity.this.rightActionBarLayout.layout(leftWidth, 0, LaunchActivity.this.rightActionBarLayout.getMeasuredWidth() + leftWidth, LaunchActivity.this.rightActionBarLayout.getMeasuredHeight());
                    }
                    int x = (width - LaunchActivity.this.layersActionBarLayout.getMeasuredWidth()) / 2;
                    int y = (height - LaunchActivity.this.layersActionBarLayout.getMeasuredHeight()) / 2;
                    LaunchActivity.this.layersActionBarLayout.layout(x, y, LaunchActivity.this.layersActionBarLayout.getMeasuredWidth() + x, LaunchActivity.this.layersActionBarLayout.getMeasuredHeight() + y);
                    LaunchActivity.this.backgroundTablet.layout(0, 0, LaunchActivity.this.backgroundTablet.getMeasuredWidth(), LaunchActivity.this.backgroundTablet.getMeasuredHeight());
                    LaunchActivity.this.shadowTablet.layout(0, 0, LaunchActivity.this.shadowTablet.getMeasuredWidth(), LaunchActivity.this.shadowTablet.getMeasuredHeight());
                }
            };
            this.drawerLayoutContainer.addView(anonymousClass1, LayoutHelper.createFrame(-1, -1.0f));
            this.backgroundTablet = new View(this);
            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.catstile);
            drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            this.backgroundTablet.setBackgroundDrawable(drawable);
            anonymousClass1.addView(this.backgroundTablet, LayoutHelper.createRelative(-1, -1));
            anonymousClass1.addView(this.actionBarLayout);
            this.rightActionBarLayout = new ActionBarLayout(this);
            this.rightActionBarLayout.init(rightFragmentsStack);
            this.rightActionBarLayout.setDelegate(this);
            anonymousClass1.addView(this.rightActionBarLayout);
            this.shadowTabletSide = new FrameLayout(this);
            this.shadowTabletSide.setBackgroundColor(NUM);
            anonymousClass1.addView(this.shadowTabletSide);
            this.shadowTablet = new FrameLayout(this);
            this.shadowTablet.setVisibility(layerFragmentsStack.isEmpty() ? 8 : 0);
            this.shadowTablet.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
            anonymousClass1.addView(this.shadowTablet);
            this.shadowTablet.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (LaunchActivity.this.actionBarLayout.fragmentsStack.isEmpty() || event.getAction() != 1) {
                        return false;
                    }
                    float x = event.getX();
                    float y = event.getY();
                    int[] location = new int[2];
                    LaunchActivity.this.layersActionBarLayout.getLocationOnScreen(location);
                    int viewX = location[0];
                    int viewY = location[1];
                    if (LaunchActivity.this.layersActionBarLayout.checkTransitionAnimation() || (x > ((float) viewX) && x < ((float) (LaunchActivity.this.layersActionBarLayout.getWidth() + viewX)) && y > ((float) viewY) && y < ((float) (LaunchActivity.this.layersActionBarLayout.getHeight() + viewY)))) {
                        return false;
                    }
                    if (!LaunchActivity.this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        int a = 0;
                        while (LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            LaunchActivity.this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) LaunchActivity.this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        LaunchActivity.this.layersActionBarLayout.closeLastFragment(true);
                    }
                    return true;
                }
            });
            this.shadowTablet.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                }
            });
            this.layersActionBarLayout = new ActionBarLayout(this);
            this.layersActionBarLayout.setRemoveActionBarExtraHeight(true);
            this.layersActionBarLayout.setBackgroundView(this.shadowTablet);
            this.layersActionBarLayout.setUseAlphaAnimations(true);
            this.layersActionBarLayout.setBackgroundResource(R.drawable.boxshadow);
            this.layersActionBarLayout.init(layerFragmentsStack);
            this.layersActionBarLayout.setDelegate(this);
            this.layersActionBarLayout.setDrawerLayoutContainer(this.drawerLayoutContainer);
            this.layersActionBarLayout.setVisibility(layerFragmentsStack.isEmpty() ? 8 : 0);
            anonymousClass1.addView(this.layersActionBarLayout);
        } else {
            this.drawerLayoutContainer.addView(this.actionBarLayout, new LayoutParams(-1, -1));
        }
        this.sideMenu = new RecyclerListView(this);
        ((DefaultItemAnimator) this.sideMenu.getItemAnimator()).setDelayAnimations(false);
        this.sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        this.sideMenu.setLayoutManager(new LinearLayoutManager(this, 1, false));
        RecyclerListView recyclerListView = this.sideMenu;
        Adapter drawerLayoutAdapter = new DrawerLayoutAdapter(this);
        this.drawerLayoutAdapter = drawerLayoutAdapter;
        recyclerListView.setAdapter(drawerLayoutAdapter);
        this.drawerLayoutContainer.setDrawerLayout(this.sideMenu);
        LayoutParams layoutParams = (FrameLayout.LayoutParams) this.sideMenu.getLayoutParams();
        Point screenSize = AndroidUtilities.getRealScreenSize();
        if (AndroidUtilities.isTablet()) {
            dp = AndroidUtilities.dp(320.0f);
        } else {
            dp = Math.min(AndroidUtilities.dp(320.0f), Math.min(screenSize.x, screenSize.y) - AndroidUtilities.dp(56.0f));
        }
        layoutParams.width = dp;
        layoutParams.height = -1;
        this.sideMenu.setLayoutParams(layoutParams);
        this.sideMenu.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(View view, int position) {
                boolean z = false;
                if (position == 0) {
                    DrawerLayoutAdapter access$700 = LaunchActivity.this.drawerLayoutAdapter;
                    if (!LaunchActivity.this.drawerLayoutAdapter.isAccountsShowed()) {
                        z = true;
                    }
                    access$700.setAccountsShowed(z, true);
                } else if (view instanceof DrawerUserCell) {
                    LaunchActivity.this.switchToAccount(((DrawerUserCell) view).getAccountNumber(), true);
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                } else if (view instanceof DrawerAddCell) {
                    int freeAccount = -1;
                    for (int a = 0; a < 3; a++) {
                        if (!UserConfig.getInstance(a).isClientActivated()) {
                            freeAccount = a;
                            break;
                        }
                    }
                    if (freeAccount >= 0) {
                        LaunchActivity.this.presentFragment(new LoginActivity(freeAccount));
                    }
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                } else {
                    int id = LaunchActivity.this.drawerLayoutAdapter.getId(position);
                    if (id == 2) {
                        LaunchActivity.this.presentFragment(new GroupCreateActivity());
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 3) {
                        args = new Bundle();
                        args.putBoolean("onlyUsers", true);
                        args.putBoolean("destroyAfterSelect", true);
                        args.putBoolean("createSecretChat", true);
                        args.putBoolean("allowBots", false);
                        LaunchActivity.this.presentFragment(new ContactsActivity(args));
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 4) {
                        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                        if (BuildVars.DEBUG_VERSION || !preferences.getBoolean("channel_intro", false)) {
                            LaunchActivity.this.presentFragment(new ChannelIntroActivity());
                            preferences.edit().putBoolean("channel_intro", true).commit();
                        } else {
                            args = new Bundle();
                            args.putInt("step", 0);
                            LaunchActivity.this.presentFragment(new ChannelCreateActivity(args));
                        }
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 6) {
                        LaunchActivity.this.presentFragment(new ContactsActivity(null));
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 7) {
                        LaunchActivity.this.presentFragment(new InviteContactsActivity());
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 8) {
                        LaunchActivity.this.presentFragment(new SettingsActivity());
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 9) {
                        Browser.openUrl(LaunchActivity.this, LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl));
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 10) {
                        LaunchActivity.this.presentFragment(new CallLogActivity());
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else if (id == 11) {
                        args = new Bundle();
                        args.putInt("user_id", UserConfig.getInstance(LaunchActivity.this.currentAccount).getClientUserId());
                        LaunchActivity.this.presentFragment(new ChatActivity(args));
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    }
                }
            }
        });
        this.drawerLayoutContainer.setParentActionBarLayout(this.actionBarLayout);
        this.actionBarLayout.setDrawerLayoutContainer(this.drawerLayoutContainer);
        this.actionBarLayout.init(mainFragmentsStack);
        this.actionBarLayout.setDelegate(this);
        Theme.loadWallpaper();
        this.passcodeView = new PasscodeView(this);
        this.drawerLayoutContainer.addView(this.passcodeView, LayoutHelper.createFrame(-1, -1.0f));
        checkCurrentAccount();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, this);
        this.currentConnectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewTheme);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.needSetDayNightTheme);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeOtherAppActivities);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewWallpapper);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.notificationsCountUpdated);
        if (this.actionBarLayout.fragmentsStack.isEmpty()) {
            if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
                DialogsActivity dialogsActivity = new DialogsActivity(null);
                dialogsActivity.setSideMenu(this.sideMenu);
                this.actionBarLayout.addFragmentToStack(dialogsActivity);
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            } else {
                this.actionBarLayout.addFragmentToStack(new LoginActivity());
                this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            }
            if (savedInstanceState != null) {
                try {
                    String fragmentName = savedInstanceState.getString("fragment");
                    if (fragmentName != null) {
                        ChatActivity chat;
                        BaseFragment settings;
                        BaseFragment groupCreateFinalActivity;
                        ChannelCreateActivity channel;
                        ChannelEditActivity channel2;
                        Bundle args = savedInstanceState.getBundle("args");
                        Object obj = -1;
                        switch (fragmentName.hashCode()) {
                            case -1529105743:
                                if (fragmentName.equals("wallpapers")) {
                                    obj = 6;
                                }
                            case -1349522494:
                                if (fragmentName.equals("chat_profile")) {
                                    obj = 5;
                                }
                            case 3052376:
                                if (fragmentName.equals("chat")) {
                                    obj = null;
                                }
                            case 3108362:
                                if (fragmentName.equals("edit")) {
                                    obj = 4;
                                }
                                switch (obj) {
                                    case null:
                                        if (args != null) {
                                            chat = new ChatActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(chat)) {
                                                chat.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 1:
                                        settings = new SettingsActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                    case 2:
                                        if (args != null) {
                                            groupCreateFinalActivity = new GroupCreateFinalActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 3:
                                        if (args != null) {
                                            channel = new ChannelCreateActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel)) {
                                                channel.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 4:
                                        if (args != null) {
                                            channel2 = new ChannelEditActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel2)) {
                                                channel2.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 5:
                                        if (args != null) {
                                            groupCreateFinalActivity = new ProfileActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 6:
                                        settings = new WallpapersActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                }
                                break;
                            case 98629247:
                                if (fragmentName.equals("group")) {
                                    obj = 2;
                                }
                                switch (obj) {
                                    case null:
                                        if (args != null) {
                                            chat = new ChatActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(chat)) {
                                                chat.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 1:
                                        settings = new SettingsActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                    case 2:
                                        if (args != null) {
                                            groupCreateFinalActivity = new GroupCreateFinalActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 3:
                                        if (args != null) {
                                            channel = new ChannelCreateActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel)) {
                                                channel.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 4:
                                        if (args != null) {
                                            channel2 = new ChannelEditActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel2)) {
                                                channel2.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 5:
                                        if (args != null) {
                                            groupCreateFinalActivity = new ProfileActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 6:
                                        settings = new WallpapersActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                }
                                break;
                            case 738950403:
                                if (fragmentName.equals("channel")) {
                                    obj = 3;
                                }
                                switch (obj) {
                                    case null:
                                        if (args != null) {
                                            chat = new ChatActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(chat)) {
                                                chat.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 1:
                                        settings = new SettingsActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                    case 2:
                                        if (args != null) {
                                            groupCreateFinalActivity = new GroupCreateFinalActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 3:
                                        if (args != null) {
                                            channel = new ChannelCreateActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel)) {
                                                channel.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 4:
                                        if (args != null) {
                                            channel2 = new ChannelEditActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel2)) {
                                                channel2.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 5:
                                        if (args != null) {
                                            groupCreateFinalActivity = new ProfileActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 6:
                                        settings = new WallpapersActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                }
                                break;
                            case 1434631203:
                                if (fragmentName.equals("settings")) {
                                    obj = 1;
                                }
                                switch (obj) {
                                    case null:
                                        if (args != null) {
                                            chat = new ChatActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(chat)) {
                                                chat.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 1:
                                        settings = new SettingsActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                    case 2:
                                        if (args != null) {
                                            groupCreateFinalActivity = new GroupCreateFinalActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 3:
                                        if (args != null) {
                                            channel = new ChannelCreateActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel)) {
                                                channel.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 4:
                                        if (args != null) {
                                            channel2 = new ChannelEditActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(channel2)) {
                                                channel2.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 5:
                                        if (args != null) {
                                            groupCreateFinalActivity = new ProfileActivity(args);
                                            if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                                groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                                break;
                                            }
                                        }
                                        break;
                                    case 6:
                                        settings = new WallpapersActivity();
                                        this.actionBarLayout.addFragmentToStack(settings);
                                        settings.restoreSelfArgs(savedInstanceState);
                                        break;
                                }
                                break;
                        }
                        switch (obj) {
                            case null:
                                if (args != null) {
                                    chat = new ChatActivity(args);
                                    if (this.actionBarLayout.addFragmentToStack(chat)) {
                                        chat.restoreSelfArgs(savedInstanceState);
                                        break;
                                    }
                                }
                                break;
                            case 1:
                                settings = new SettingsActivity();
                                this.actionBarLayout.addFragmentToStack(settings);
                                settings.restoreSelfArgs(savedInstanceState);
                                break;
                            case 2:
                                if (args != null) {
                                    groupCreateFinalActivity = new GroupCreateFinalActivity(args);
                                    if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                        groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                        break;
                                    }
                                }
                                break;
                            case 3:
                                if (args != null) {
                                    channel = new ChannelCreateActivity(args);
                                    if (this.actionBarLayout.addFragmentToStack(channel)) {
                                        channel.restoreSelfArgs(savedInstanceState);
                                        break;
                                    }
                                }
                                break;
                            case 4:
                                if (args != null) {
                                    channel2 = new ChannelEditActivity(args);
                                    if (this.actionBarLayout.addFragmentToStack(channel2)) {
                                        channel2.restoreSelfArgs(savedInstanceState);
                                        break;
                                    }
                                }
                                break;
                            case 5:
                                if (args != null) {
                                    groupCreateFinalActivity = new ProfileActivity(args);
                                    if (this.actionBarLayout.addFragmentToStack(groupCreateFinalActivity)) {
                                        groupCreateFinalActivity.restoreSelfArgs(savedInstanceState);
                                        break;
                                    }
                                }
                                break;
                            case 6:
                                settings = new WallpapersActivity();
                                this.actionBarLayout.addFragmentToStack(settings);
                                settings.restoreSelfArgs(savedInstanceState);
                                break;
                        }
                    }
                } catch (Throwable e22) {
                    FileLog.e(e22);
                }
            }
        } else {
            BaseFragment fragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(0);
            if (fragment instanceof DialogsActivity) {
                ((DialogsActivity) fragment).setSideMenu(this.sideMenu);
            }
            boolean allowOpen = true;
            if (AndroidUtilities.isTablet()) {
                allowOpen = this.actionBarLayout.fragmentsStack.size() <= 1 && this.layersActionBarLayout.fragmentsStack.isEmpty();
                if (this.layersActionBarLayout.fragmentsStack.size() == 1 && (this.layersActionBarLayout.fragmentsStack.get(0) instanceof LoginActivity)) {
                    allowOpen = false;
                }
            }
            if (this.actionBarLayout.fragmentsStack.size() == 1 && (this.actionBarLayout.fragmentsStack.get(0) instanceof LoginActivity)) {
                allowOpen = false;
            }
            this.drawerLayoutContainer.setAllowOpenDrawer(allowOpen, false);
        }
        checkLayout();
        handleIntent(getIntent(), false, savedInstanceState != null, false);
        try {
            String os1 = Build.DISPLAY;
            String os2 = Build.USER;
            if (os1 != null) {
                os1 = os1.toLowerCase();
            } else {
                os1 = TtmlNode.ANONYMOUS_REGION_ID;
            }
            if (os2 != null) {
                os2 = os1.toLowerCase();
            } else {
                os2 = TtmlNode.ANONYMOUS_REGION_ID;
            }
            if (os1.contains("flyme") || os2.contains("flyme")) {
                AndroidUtilities.incorrectDisplaySizeFix = true;
                View view = getWindow().getDecorView().getRootView();
                ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
                final View view2 = view;
                OnGlobalLayoutListener anonymousClass5 = new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        int height = view2.getMeasuredHeight();
                        if (VERSION.SDK_INT >= 21) {
                            height -= AndroidUtilities.statusBarHeight;
                        }
                        if (height > AndroidUtilities.dp(100.0f) && height < AndroidUtilities.displaySize.y && AndroidUtilities.dp(100.0f) + height > AndroidUtilities.displaySize.y) {
                            AndroidUtilities.displaySize.y = height;
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("fix display size y to " + AndroidUtilities.displaySize.y);
                            }
                        }
                    }
                };
                this.onGlobalLayoutListener = anonymousClass5;
                viewTreeObserver.addOnGlobalLayoutListener(anonymousClass5);
            }
        } catch (Throwable e222) {
            FileLog.e(e222);
        }
        MediaController.getInstance().setBaseActivity(this, true);
    }

    public void switchToAccount(int account, boolean removeAll) {
        if (account != UserConfig.selectedAccount) {
            ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
            UserConfig.selectedAccount = account;
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
                }
            }
            if (removeAll) {
                this.actionBarLayout.removeAllFragments();
            } else {
                this.actionBarLayout.removeFragmentFromStack(0);
            }
            DialogsActivity dialogsActivity = new DialogsActivity(null);
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
        }
    }

    private void switchToAvailableAccountOrLogout() {
        int account = -1;
        for (int a = 0; a < 3; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                account = a;
                break;
            }
        }
        if (account != -1) {
            switchToAccount(account, true);
            return;
        }
        if (this.drawerLayoutAdapter != null) {
            this.drawerLayoutAdapter.notifyDataSetChanged();
        }
        Iterator it = this.actionBarLayout.fragmentsStack.iterator();
        while (it.hasNext()) {
            ((BaseFragment) it.next()).onFragmentDestroy();
        }
        this.actionBarLayout.fragmentsStack.clear();
        if (AndroidUtilities.isTablet()) {
            it = this.layersActionBarLayout.fragmentsStack.iterator();
            while (it.hasNext()) {
                ((BaseFragment) it.next()).onFragmentDestroy();
            }
            this.layersActionBarLayout.fragmentsStack.clear();
            it = this.rightActionBarLayout.fragmentsStack.iterator();
            while (it.hasNext()) {
                ((BaseFragment) it.next()).onFragmentDestroy();
            }
            this.rightActionBarLayout.fragmentsStack.clear();
        }
        startActivity(new Intent(this, IntroActivity.class));
        onFinish();
        finish();
    }

    private void checkCurrentAccount() {
        if (this.currentAccount != UserConfig.selectedAccount) {
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
        }
        this.currentAccount = UserConfig.selectedAccount;
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didUpdatedConnectionState);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needShowAlert);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.openArticle);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.hasNewContactsToImport);
        updateCurrentConnectionState(this.currentAccount);
    }

    private void checkLayout() {
        int i = 0;
        int i2 = 8;
        if (AndroidUtilities.isTablet() && this.rightActionBarLayout != null) {
            int a;
            BaseFragment chatFragment;
            if (AndroidUtilities.isInMultiwindow || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
                this.tabletFullSize = true;
                if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    a = 0;
                    while (this.rightActionBarLayout.fragmentsStack.size() > 0) {
                        chatFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(a);
                        if (chatFragment instanceof ChatActivity) {
                            ((ChatActivity) chatFragment).setIgnoreAttachOnPause(true);
                        }
                        chatFragment.onPause();
                        this.rightActionBarLayout.fragmentsStack.remove(a);
                        this.actionBarLayout.fragmentsStack.add(chatFragment);
                        a = (a - 1) + 1;
                    }
                    if (this.passcodeView.getVisibility() != 0) {
                        this.actionBarLayout.showLastFragment();
                    }
                }
                this.shadowTabletSide.setVisibility(8);
                this.rightActionBarLayout.setVisibility(8);
                View view = this.backgroundTablet;
                if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                    i2 = 0;
                }
                view.setVisibility(i2);
                return;
            }
            int i3;
            this.tabletFullSize = false;
            if (this.actionBarLayout.fragmentsStack.size() >= 2) {
                for (a = 1; a < this.actionBarLayout.fragmentsStack.size(); a = (a - 1) + 1) {
                    chatFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(a);
                    if (chatFragment instanceof ChatActivity) {
                        ((ChatActivity) chatFragment).setIgnoreAttachOnPause(true);
                    }
                    chatFragment.onPause();
                    this.actionBarLayout.fragmentsStack.remove(a);
                    this.rightActionBarLayout.fragmentsStack.add(chatFragment);
                }
                if (this.passcodeView.getVisibility() != 0) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                }
            }
            ActionBarLayout actionBarLayout = this.rightActionBarLayout;
            if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                i3 = 8;
            } else {
                i3 = 0;
            }
            actionBarLayout.setVisibility(i3);
            View view2 = this.backgroundTablet;
            if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                i3 = 0;
            } else {
                i3 = 8;
            }
            view2.setVisibility(i3);
            FrameLayout frameLayout = this.shadowTabletSide;
            if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                i = 8;
            }
            frameLayout.setVisibility(i);
        }
    }

    private void showPasscodeActivity() {
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
            this.passcodeView.setDelegate(new PasscodeViewDelegate() {
                public void didAcceptedPassword() {
                    SharedConfig.isWaitingForPasscodeEnter = false;
                    if (LaunchActivity.this.passcodeSaveIntent != null) {
                        LaunchActivity.this.handleIntent(LaunchActivity.this.passcodeSaveIntent, LaunchActivity.this.passcodeSaveIntentIsNew, LaunchActivity.this.passcodeSaveIntentIsRestore, true);
                        LaunchActivity.this.passcodeSaveIntent = null;
                    }
                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    LaunchActivity.this.actionBarLayout.showLastFragment();
                    if (AndroidUtilities.isTablet()) {
                        LaunchActivity.this.layersActionBarLayout.showLastFragment();
                        LaunchActivity.this.rightActionBarLayout.showLastFragment();
                    }
                }
            });
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleIntent(Intent intent, boolean isNew, boolean restore, boolean fromPassword) {
        if (AndroidUtilities.handleProxyIntent(this, intent)) {
            return true;
        }
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible() && (intent == null || !"android.intent.action.MAIN".equals(intent.getAction()))) {
            PhotoViewer.getInstance().closePhoto(false, true);
        }
        int flags = intent.getFlags();
        int[] intentAccount = new int[1];
        intentAccount[0] = intent.getIntExtra("currentAccount", UserConfig.selectedAccount);
        switchToAccount(intentAccount[0], true);
        if (fromPassword || !(AndroidUtilities.needShowPasscode(true) || SharedConfig.isWaitingForPasscodeEnter)) {
            String[] args;
            int a;
            Bundle args2;
            BaseFragment dialogsActivity;
            boolean pushOpened = false;
            Integer push_user_id = Integer.valueOf(0);
            Integer push_chat_id = Integer.valueOf(0);
            Integer push_enc_id = Integer.valueOf(0);
            Integer push_msg_id = Integer.valueOf(0);
            Integer open_settings = Integer.valueOf(0);
            Integer open_new_dialog = Integer.valueOf(0);
            long dialogId = 0;
            if (SharedConfig.directShare) {
                dialogId = (intent == null || intent.getExtras() == null) ? 0 : intent.getExtras().getLong("dialogId", 0);
            }
            boolean showDialogsList = false;
            boolean showPlayer = false;
            boolean showLocations = false;
            this.photoPathsArray = null;
            this.videoPath = null;
            this.sendingText = null;
            this.documentsPathsArray = null;
            this.documentsOriginalPathsArray = null;
            this.documentsMimeType = null;
            this.documentsUrisArray = null;
            this.contactsToSend = null;
            if (!(!UserConfig.getInstance(this.currentAccount).isClientActivated() || (ExtractorMediaSource.DEFAULT_LOADING_CHECK_INTERVAL_BYTES & flags) != 0 || intent == null || intent.getAction() == null || restore)) {
                boolean error;
                String type;
                Uri uri;
                String phone;
                Parcelable parcelable;
                String path;
                if ("android.intent.action.SEND".equals(intent.getAction())) {
                    error = false;
                    type = intent.getType();
                    if (type != null) {
                        if (type.equals("text/x-vcard")) {
                            uri = (Uri) intent.getExtras().get("android.intent.extra.STREAM");
                            if (uri != null) {
                                InputStream stream = getContentResolver().openInputStream(uri);
                                ArrayList<VcardData> vcardDatas = new ArrayList();
                                VcardData currentData = null;
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, C.UTF8_NAME));
                                while (true) {
                                    String line = bufferedReader.readLine();
                                    if (line != null) {
                                        if (BuildVars.LOGS_ENABLED) {
                                            FileLog.d(line);
                                        }
                                        args = line.split(":");
                                        if (args.length == 2) {
                                            if (args[0].equals("BEGIN") && args[1].equals("VCARD")) {
                                                LaunchActivity launchActivity = this;
                                                VcardData vcardData = new VcardData();
                                                vcardDatas.add(vcardData);
                                            } else if (args[0].equals("END") && args[1].equals("VCARD")) {
                                                currentData = null;
                                            }
                                            if (currentData == null) {
                                                continue;
                                            } else if (args[0].startsWith("FN") || (args[0].startsWith("ORG") && TextUtils.isEmpty(currentData.name))) {
                                                String nameEncoding = null;
                                                String nameCharset = null;
                                                for (String param : args[0].split(";")) {
                                                    String[] args22 = param.split("=");
                                                    if (args22.length == 2) {
                                                        if (args22[0].equals("CHARSET")) {
                                                            nameCharset = args22[1];
                                                        } else if (args22[0].equals("ENCODING")) {
                                                            nameEncoding = args22[1];
                                                        }
                                                    }
                                                }
                                                currentData.name = args[1];
                                                if (nameEncoding != null) {
                                                    if (nameEncoding.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                                                        while (currentData.name.endsWith("=") && nameEncoding != null) {
                                                            currentData.name = currentData.name.substring(0, currentData.name.length() - 1);
                                                            line = bufferedReader.readLine();
                                                            if (line == null) {
                                                                break;
                                                            }
                                                            try {
                                                                currentData.name += line;
                                                            } catch (Throwable e) {
                                                                FileLog.e(e);
                                                                error = true;
                                                            }
                                                        }
                                                        byte[] bytes = AndroidUtilities.decodeQuotedPrintable(currentData.name.getBytes());
                                                        if (!(bytes == null || bytes.length == 0)) {
                                                            String str = new String(bytes, nameCharset);
                                                            if (str != null) {
                                                                currentData.name = str;
                                                            }
                                                        }
                                                    } else {
                                                        continue;
                                                    }
                                                } else {
                                                    continue;
                                                }
                                            } else if (args[0].startsWith("TEL")) {
                                                phone = PhoneFormat.stripExceptNumbers(args[1], true);
                                                if (phone.length() > 0) {
                                                    currentData.phones.add(phone);
                                                }
                                            }
                                        }
                                    } else {
                                        try {
                                            break;
                                        } catch (Throwable e2) {
                                            FileLog.e(e2);
                                        }
                                    }
                                }
                                bufferedReader.close();
                                stream.close();
                                for (a = 0; a < vcardDatas.size(); a++) {
                                    VcardData vcardData2 = (VcardData) vcardDatas.get(a);
                                    if (!(vcardData2.name == null || vcardData2.phones.isEmpty())) {
                                        if (this.contactsToSend == null) {
                                            this.contactsToSend = new ArrayList();
                                        }
                                        for (int b = 0; b < vcardData2.phones.size(); b++) {
                                            phone = (String) vcardData2.phones.get(b);
                                            User user = new TL_userContact_old2();
                                            user.phone = phone;
                                            user.first_name = vcardData2.name;
                                            user.last_name = TtmlNode.ANONYMOUS_REGION_ID;
                                            user.id = 0;
                                            this.contactsToSend.add(user);
                                        }
                                    }
                                }
                            } else {
                                error = true;
                            }
                            if (error) {
                                Toast.makeText(this, "Unsupported content", 0).show();
                            }
                        }
                    }
                    String text = intent.getStringExtra("android.intent.extra.TEXT");
                    if (text == null) {
                        CharSequence textSequence = intent.getCharSequenceExtra("android.intent.extra.TEXT");
                        if (textSequence != null) {
                            text = textSequence.toString();
                        }
                    }
                    String subject = intent.getStringExtra("android.intent.extra.SUBJECT");
                    if (text != null && text.length() != 0) {
                        if (!text.startsWith("http://")) {
                        }
                        if (!(subject == null || subject.length() == 0)) {
                            text = subject + "\n" + text;
                        }
                        this.sendingText = text;
                    } else if (subject != null && subject.length() > 0) {
                        this.sendingText = subject;
                    }
                    parcelable = intent.getParcelableExtra("android.intent.extra.STREAM");
                    if (parcelable != null) {
                        if (!(parcelable instanceof Uri)) {
                            parcelable = Uri.parse(parcelable.toString());
                        }
                        uri = (Uri) parcelable;
                        if (uri != null && AndroidUtilities.isInternalUri(uri)) {
                            error = true;
                        }
                        if (!error) {
                            if (uri != null) {
                                if (type != null) {
                                }
                            }
                            path = AndroidUtilities.getPath(uri);
                            if (path != null) {
                                if (path.startsWith("file:")) {
                                    path = path.replace("file://", TtmlNode.ANONYMOUS_REGION_ID);
                                }
                                if (type != null) {
                                    if (type.startsWith("video/")) {
                                        this.videoPath = path;
                                    }
                                }
                                if (this.documentsPathsArray == null) {
                                    this.documentsPathsArray = new ArrayList();
                                    this.documentsOriginalPathsArray = new ArrayList();
                                }
                                this.documentsPathsArray.add(path);
                                this.documentsOriginalPathsArray.add(uri.toString());
                            } else {
                                if (this.documentsUrisArray == null) {
                                    this.documentsUrisArray = new ArrayList();
                                }
                                this.documentsUrisArray.add(uri);
                                this.documentsMimeType = type;
                            }
                        }
                    } else if (this.sendingText == null) {
                        error = true;
                    }
                    if (error) {
                        Toast.makeText(this, "Unsupported content", 0).show();
                    }
                } else if (intent.getAction().equals("android.intent.action.SEND_MULTIPLE")) {
                    error = false;
                    try {
                        ArrayList<Parcelable> uris = intent.getParcelableArrayListExtra("android.intent.extra.STREAM");
                        type = intent.getType();
                        if (uris != null) {
                            a = 0;
                            while (a < uris.size()) {
                                parcelable = (Parcelable) uris.get(a);
                                if (!(parcelable instanceof Uri)) {
                                    parcelable = Uri.parse(parcelable.toString());
                                }
                                uri = (Uri) parcelable;
                                if (uri != null && AndroidUtilities.isInternalUri(uri)) {
                                    uris.remove(a);
                                    a--;
                                }
                                a++;
                            }
                            if (uris.isEmpty()) {
                                uris = null;
                            }
                        }
                        if (uris != null) {
                            if (type != null) {
                                if (type.startsWith("image/")) {
                                    for (a = 0; a < uris.size(); a++) {
                                        parcelable = (Parcelable) uris.get(a);
                                        if (!(parcelable instanceof Uri)) {
                                            parcelable = Uri.parse(parcelable.toString());
                                        }
                                        uri = (Uri) parcelable;
                                        if (this.photoPathsArray == null) {
                                            this.photoPathsArray = new ArrayList();
                                        }
                                        SendingMediaInfo info = new SendingMediaInfo();
                                        info.uri = uri;
                                        this.photoPathsArray.add(info);
                                    }
                                }
                            }
                            for (a = 0; a < uris.size(); a++) {
                                parcelable = (Parcelable) uris.get(a);
                                if (!(parcelable instanceof Uri)) {
                                    parcelable = Uri.parse(parcelable.toString());
                                }
                                uri = (Uri) parcelable;
                                path = AndroidUtilities.getPath(uri);
                                String originalPath = parcelable.toString();
                                if (originalPath == null) {
                                    originalPath = path;
                                }
                                if (path != null) {
                                    if (path.startsWith("file:")) {
                                        path = path.replace("file://", TtmlNode.ANONYMOUS_REGION_ID);
                                    }
                                    if (this.documentsPathsArray == null) {
                                        this.documentsPathsArray = new ArrayList();
                                        this.documentsOriginalPathsArray = new ArrayList();
                                    }
                                    this.documentsPathsArray.add(path);
                                    this.documentsOriginalPathsArray.add(originalPath);
                                } else {
                                    if (this.documentsUrisArray == null) {
                                        this.documentsUrisArray = new ArrayList();
                                    }
                                    this.documentsUrisArray.add(uri);
                                    this.documentsMimeType = type;
                                }
                            }
                        } else {
                            error = true;
                        }
                    } catch (Throwable e22) {
                        FileLog.e(e22);
                        error = true;
                    }
                    if (error) {
                        Toast.makeText(this, "Unsupported content", 0).show();
                    }
                } else if ("android.intent.action.VIEW".equals(intent.getAction())) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String username = null;
                        String group = null;
                        String sticker = null;
                        String[] instantView = null;
                        String botUser = null;
                        String botChat = null;
                        String message = null;
                        phone = null;
                        String game = null;
                        String phoneHash = null;
                        Integer messageId = null;
                        boolean hasUrl = false;
                        String scheme = data.getScheme();
                        if (scheme != null) {
                            if (!scheme.equals("http")) {
                                if (!scheme.equals("https")) {
                                    if (scheme.equals("tg")) {
                                        String url = data.toString();
                                        if (!url.startsWith("tg:resolve")) {
                                            if (!url.startsWith("tg://resolve")) {
                                                if (!url.startsWith("tg:join")) {
                                                    if (!url.startsWith("tg://join")) {
                                                        if (!url.startsWith("tg:addstickers")) {
                                                            if (!url.startsWith("tg://addstickers")) {
                                                                if (!url.startsWith("tg:msg")) {
                                                                    if (!url.startsWith("tg://msg")) {
                                                                        if (!url.startsWith("tg://share")) {
                                                                            if (!url.startsWith("tg:share")) {
                                                                                if (!url.startsWith("tg:confirmphone")) {
                                                                                    if (!url.startsWith("tg://confirmphone")) {
                                                                                        if (!url.startsWith("tg:openmessage")) {
                                                                                        }
                                                                                        data = Uri.parse(url.replace("tg:openmessage", "tg://telegram.org").replace("tg://openmessage", "tg://telegram.org"));
                                                                                        String userID = data.getQueryParameter("user_id");
                                                                                        String chatID = data.getQueryParameter("chat_id");
                                                                                        String msgID = data.getQueryParameter("message_id");
                                                                                        if (userID != null) {
                                                                                            try {
                                                                                                push_user_id = Integer.valueOf(Integer.parseInt(userID));
                                                                                            } catch (NumberFormatException e3) {
                                                                                            }
                                                                                        } else if (chatID != null) {
                                                                                            try {
                                                                                                push_chat_id = Integer.valueOf(Integer.parseInt(chatID));
                                                                                            } catch (NumberFormatException e4) {
                                                                                            }
                                                                                        }
                                                                                        if (msgID != null) {
                                                                                            try {
                                                                                                push_msg_id = Integer.valueOf(Integer.parseInt(msgID));
                                                                                            } catch (NumberFormatException e5) {
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                                data = Uri.parse(url.replace("tg:confirmphone", "tg://telegram.org").replace("tg://confirmphone", "tg://telegram.org"));
                                                                                phone = data.getQueryParameter("phone");
                                                                                phoneHash = data.getQueryParameter("hash");
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                data = Uri.parse(url.replace("tg:msg", "tg://telegram.org").replace("tg://msg", "tg://telegram.org").replace("tg://share", "tg://telegram.org").replace("tg:share", "tg://telegram.org"));
                                                                message = data.getQueryParameter(UpdateFragment.FRAGMENT_URL);
                                                                if (message == null) {
                                                                    message = TtmlNode.ANONYMOUS_REGION_ID;
                                                                }
                                                                if (data.getQueryParameter(MimeTypes.BASE_TYPE_TEXT) != null) {
                                                                    if (message.length() > 0) {
                                                                        hasUrl = true;
                                                                        message = message + "\n";
                                                                    }
                                                                    message = message + data.getQueryParameter(MimeTypes.BASE_TYPE_TEXT);
                                                                }
                                                                if (message.length() > MessagesController.UPDATE_MASK_CHAT_ADMINS) {
                                                                    message = message.substring(0, MessagesController.UPDATE_MASK_CHAT_ADMINS);
                                                                }
                                                                while (message.endsWith("\n")) {
                                                                    message = message.substring(0, message.length() - 1);
                                                                }
                                                            }
                                                        }
                                                        sticker = Uri.parse(url.replace("tg:addstickers", "tg://telegram.org").replace("tg://addstickers", "tg://telegram.org")).getQueryParameter("set");
                                                    }
                                                }
                                                group = Uri.parse(url.replace("tg:join", "tg://telegram.org").replace("tg://join", "tg://telegram.org")).getQueryParameter("invite");
                                            }
                                        }
                                        data = Uri.parse(url.replace("tg:resolve", "tg://telegram.org").replace("tg://resolve", "tg://telegram.org"));
                                        username = data.getQueryParameter("domain");
                                        botUser = data.getQueryParameter(TtmlNode.START);
                                        botChat = data.getQueryParameter("startgroup");
                                        game = data.getQueryParameter("game");
                                        messageId = Utilities.parseInt(data.getQueryParameter("post"));
                                        if (messageId.intValue() == 0) {
                                            messageId = null;
                                        }
                                    }
                                }
                            }
                            String host = data.getHost().toLowerCase();
                            if (!host.equals("telegram.me")) {
                                if (!host.equals("t.me")) {
                                    if (!host.equals("telegram.dog")) {
                                    }
                                }
                            }
                            path = data.getPath();
                            if (path != null && path.length() > 1) {
                                path = path.substring(1);
                                if (path.startsWith("joinchat/")) {
                                    group = path.replace("joinchat/", TtmlNode.ANONYMOUS_REGION_ID);
                                } else {
                                    if (path.startsWith("addstickers/")) {
                                        sticker = path.replace("addstickers/", TtmlNode.ANONYMOUS_REGION_ID);
                                    } else {
                                        if (path.startsWith("iv/")) {
                                            null[0] = data.getQueryParameter(UpdateFragment.FRAGMENT_URL);
                                            null[1] = data.getQueryParameter("rhash");
                                            if (TextUtils.isEmpty(null[0]) || TextUtils.isEmpty(null[1])) {
                                                instantView = null;
                                            }
                                        } else {
                                            if (!path.startsWith("msg/")) {
                                                if (!path.startsWith("share/")) {
                                                    if (path.startsWith("confirmphone")) {
                                                        phone = data.getQueryParameter("phone");
                                                        phoneHash = data.getQueryParameter("hash");
                                                    } else if (path.length() >= 1) {
                                                        List<String> segments = data.getPathSegments();
                                                        if (segments.size() > 0) {
                                                            username = (String) segments.get(0);
                                                            if (segments.size() > 1) {
                                                                messageId = Utilities.parseInt((String) segments.get(1));
                                                                if (messageId.intValue() == 0) {
                                                                    messageId = null;
                                                                }
                                                            }
                                                        }
                                                        botUser = data.getQueryParameter(TtmlNode.START);
                                                        botChat = data.getQueryParameter("startgroup");
                                                        game = data.getQueryParameter("game");
                                                    }
                                                }
                                            }
                                            message = data.getQueryParameter(UpdateFragment.FRAGMENT_URL);
                                            if (message == null) {
                                                message = TtmlNode.ANONYMOUS_REGION_ID;
                                            }
                                            if (data.getQueryParameter(MimeTypes.BASE_TYPE_TEXT) != null) {
                                                if (message.length() > 0) {
                                                    hasUrl = true;
                                                    message = message + "\n";
                                                }
                                                message = message + data.getQueryParameter(MimeTypes.BASE_TYPE_TEXT);
                                            }
                                            if (message.length() > MessagesController.UPDATE_MASK_CHAT_ADMINS) {
                                                message = message.substring(0, MessagesController.UPDATE_MASK_CHAT_ADMINS);
                                            }
                                            while (message.endsWith("\n")) {
                                                message = message.substring(0, message.length() - 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (message != null && message.startsWith("@")) {
                            message = " " + message;
                        }
                        if (phone != null || phoneHash != null) {
                            args2 = new Bundle();
                            args2.putString("phone", phone);
                            args2.putString("hash", phoneHash);
                            final Bundle bundle = args2;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    LaunchActivity.this.presentFragment(new CancelAccountDeletionActivity(bundle));
                                }
                            });
                        } else if (username == null && group == null && sticker == null && message == null && game == null && instantView == null) {
                            try {
                                Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        int accountId = Utilities.parseInt(cursor.getString(cursor.getColumnIndex("account_name"))).intValue();
                                        for (a = 0; a < 3; a++) {
                                            if (UserConfig.getInstance(a).getClientUserId() == accountId) {
                                                intentAccount[0] = a;
                                                switchToAccount(intentAccount[0], true);
                                                break;
                                            }
                                        }
                                        userId = cursor.getInt(cursor.getColumnIndex("DATA4"));
                                        NotificationCenter.getInstance(intentAccount[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                        push_user_id = Integer.valueOf(userId);
                                    }
                                    cursor.close();
                                }
                            } catch (Throwable e222) {
                                FileLog.e(e222);
                            }
                        } else {
                            runLinkRequest(intentAccount[0], username, group, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, 0);
                        }
                    }
                } else if (intent.getAction().equals("org.telegram.messenger.OPEN_ACCOUNT")) {
                    open_settings = Integer.valueOf(1);
                } else if (intent.getAction().equals("new_dialog")) {
                    open_new_dialog = Integer.valueOf(1);
                } else if (intent.getAction().startsWith("com.tmessages.openchat")) {
                    int chatId = intent.getIntExtra("chatId", 0);
                    userId = intent.getIntExtra("userId", 0);
                    int encId = intent.getIntExtra("encId", 0);
                    if (chatId != 0) {
                        NotificationCenter.getInstance(intentAccount[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        push_chat_id = Integer.valueOf(chatId);
                    } else if (userId != 0) {
                        NotificationCenter.getInstance(intentAccount[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        push_user_id = Integer.valueOf(userId);
                    } else if (encId != 0) {
                        NotificationCenter.getInstance(intentAccount[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        push_enc_id = Integer.valueOf(encId);
                    } else {
                        showDialogsList = true;
                    }
                } else if (intent.getAction().equals("com.tmessages.openplayer")) {
                    showPlayer = true;
                } else if (intent.getAction().equals("org.tmessages.openlocations")) {
                    showLocations = true;
                }
            }
            if (push_user_id.intValue() != 0) {
                args = new Bundle();
                args.putInt("user_id", push_user_id.intValue());
                if (push_msg_id.intValue() != 0) {
                    args.putInt("message_id", push_msg_id.intValue());
                }
                if (!mainFragmentsStack.isEmpty()) {
                }
                if (this.actionBarLayout.presentFragment(new ChatActivity(args), false, true, true)) {
                    pushOpened = true;
                }
            } else if (push_chat_id.intValue() != 0) {
                args2 = new Bundle();
                args2.putInt("chat_id", push_chat_id.intValue());
                if (push_msg_id.intValue() != 0) {
                    args2.putInt("message_id", push_msg_id.intValue());
                }
                if (!mainFragmentsStack.isEmpty()) {
                }
                if (this.actionBarLayout.presentFragment(new ChatActivity(args2), false, true, true)) {
                    pushOpened = true;
                }
            } else if (push_enc_id.intValue() != 0) {
                args2 = new Bundle();
                args2.putInt("enc_id", push_enc_id.intValue());
                if (this.actionBarLayout.presentFragment(new ChatActivity(args2), false, true, true)) {
                    pushOpened = true;
                }
            } else if (showDialogsList) {
                if (!AndroidUtilities.isTablet()) {
                    this.actionBarLayout.removeAllFragments();
                } else if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(false);
                }
                pushOpened = false;
                isNew = false;
            } else if (showPlayer) {
                if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    ((BaseFragment) this.actionBarLayout.fragmentsStack.get(0)).showDialog(new AudioPlayerAlert(this));
                }
                pushOpened = false;
            } else if (showLocations) {
                if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    final int[] iArr = intentAccount;
                    ((BaseFragment) this.actionBarLayout.fragmentsStack.get(0)).showDialog(new SharingLocationsAlert(this, new SharingLocationsAlertDelegate() {
                        public void didSelectLocation(SharingLocationInfo info) {
                            iArr[0] = info.messageObject.currentAccount;
                            LaunchActivity.this.switchToAccount(iArr[0], true);
                            LocationActivity locationActivity = new LocationActivity(2);
                            locationActivity.setMessageObject(info.messageObject);
                            final long dialog_id = info.messageObject.getDialogId();
                            locationActivity.setDelegate(new LocationActivityDelegate() {
                                public void didSelectLocation(MessageMedia location, int live) {
                                    SendMessagesHelper.getInstance(iArr[0]).sendMessage(location, dialog_id, null, null, null);
                                }
                            });
                            LaunchActivity.this.presentFragment(locationActivity);
                        }
                    }));
                }
                pushOpened = false;
            } else if (this.videoPath != null || this.photoPathsArray != null || this.sendingText != null || this.documentsPathsArray != null || this.contactsToSend != null || this.documentsUrisArray != null) {
                if (!AndroidUtilities.isTablet()) {
                    NotificationCenter.getInstance(intentAccount[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                }
                if (dialogId == 0) {
                    args2 = new Bundle();
                    args2.putBoolean("onlySelect", true);
                    args2.putInt("dialogsType", 3);
                    args2.putBoolean("allowSwitchAccount", true);
                    if (this.contactsToSend != null) {
                        args2.putString("selectAlertString", LocaleController.getString("SendContactTo", R.string.SendMessagesTo));
                        args2.putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", R.string.SendContactToGroup));
                    } else {
                        args2.putString("selectAlertString", LocaleController.getString("SendMessagesTo", R.string.SendMessagesTo));
                        args2.putString("selectAlertStringGroup", LocaleController.getString("SendMessagesToGroup", R.string.SendMessagesToGroup));
                    }
                    dialogsActivity = new DialogsActivity(args2);
                    dialogsActivity.setDelegate(this);
                    boolean removeLast = AndroidUtilities.isTablet() ? this.layersActionBarLayout.fragmentsStack.size() > 0 && (this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity) : this.actionBarLayout.fragmentsStack.size() > 1 && (this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity);
                    this.actionBarLayout.presentFragment(dialogsActivity, removeLast, true, true);
                    pushOpened = true;
                    if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
                        SecretMediaViewer.getInstance().closePhoto(false, false);
                    } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                        PhotoViewer.getInstance().closePhoto(false, true);
                    } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                        ArticleViewer.getInstance().close(false, true);
                    }
                    this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    if (AndroidUtilities.isTablet()) {
                        this.actionBarLayout.showLastFragment();
                        this.rightActionBarLayout.showLastFragment();
                    } else {
                        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                } else {
                    ArrayList<Long> dids = new ArrayList();
                    dids.add(Long.valueOf(dialogId));
                    didSelectDialogs(null, dids, null, false);
                }
            } else if (open_settings.intValue() != 0) {
                this.actionBarLayout.presentFragment(new SettingsActivity(), false, true, true);
                if (AndroidUtilities.isTablet()) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                    this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                } else {
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                }
                pushOpened = true;
            } else if (open_new_dialog.intValue() != 0) {
                args2 = new Bundle();
                args2.putBoolean("destroyAfterSelect", true);
                this.actionBarLayout.presentFragment(new ContactsActivity(args2), false, true, true);
                if (AndroidUtilities.isTablet()) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                    this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                } else {
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                }
                pushOpened = true;
            }
            if (!(pushOpened || isNew)) {
                if (AndroidUtilities.isTablet()) {
                    if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
                        if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                            dialogsActivity = new DialogsActivity(null);
                            dialogsActivity.setSideMenu(this.sideMenu);
                            this.actionBarLayout.addFragmentToStack(dialogsActivity);
                            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                        }
                    } else if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        this.layersActionBarLayout.addFragmentToStack(new LoginActivity());
                        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    }
                } else if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                    if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
                        dialogsActivity = new DialogsActivity(null);
                        dialogsActivity.setSideMenu(this.sideMenu);
                        this.actionBarLayout.addFragmentToStack(dialogsActivity);
                        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    } else {
                        this.actionBarLayout.addFragmentToStack(new LoginActivity());
                        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    }
                }
                this.actionBarLayout.showLastFragment();
                if (AndroidUtilities.isTablet()) {
                    this.layersActionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                }
            }
            intent.setAction(null);
            return pushOpened;
        }
        showPasscodeActivity();
        this.passcodeSaveIntent = intent;
        this.passcodeSaveIntentIsNew = isNew;
        this.passcodeSaveIntentIsRestore = restore;
        UserConfig.getInstance(this.currentAccount).saveConfig(false);
        return false;
    }

    private void runLinkRequest(int intentAccount, String username, String group, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, int state) {
        final AlertDialog progressDialog = new AlertDialog(this, 1);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        int requestId = 0;
        TLObject req;
        final String str;
        final String str2;
        if (username != null) {
            req = new TL_contacts_resolveUsername();
            req.username = username;
            final String str3 = game;
            final int i = intentAccount;
            str = botChat;
            str2 = botUser;
            final Integer num = messageId;
            requestId = ConnectionsManager.getInstance(intentAccount).sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (!LaunchActivity.this.isFinishing()) {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.e(e);
                                }
                                final TL_contacts_resolvedPeer res = response;
                                if (error != null || LaunchActivity.this.actionBarLayout == null || (str3 != null && (str3 == null || res.users.isEmpty()))) {
                                    try {
                                        Toast.makeText(LaunchActivity.this, LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), 0).show();
                                        return;
                                    } catch (Throwable e2) {
                                        FileLog.e(e2);
                                        return;
                                    }
                                }
                                MessagesController.getInstance(i).putUsers(res.users, false);
                                MessagesController.getInstance(i).putChats(res.chats, false);
                                MessagesStorage.getInstance(i).putUsersAndChats(res.users, res.chats, false, true);
                                Bundle args;
                                DialogsActivity fragment;
                                if (str3 != null) {
                                    args = new Bundle();
                                    args.putBoolean("onlySelect", true);
                                    args.putBoolean("cantSendToChannels", true);
                                    args.putInt("dialogsType", 1);
                                    args.putString("selectAlertString", LocaleController.getString("SendGameTo", R.string.SendGameTo));
                                    args.putString("selectAlertStringGroup", LocaleController.getString("SendGameToGroup", R.string.SendGameToGroup));
                                    fragment = new DialogsActivity(args);
                                    fragment.setDelegate(new DialogsActivityDelegate() {
                                        public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
                                            long did = ((Long) dids.get(0)).longValue();
                                            TL_inputMediaGame inputMediaGame = new TL_inputMediaGame();
                                            inputMediaGame.id = new TL_inputGameShortName();
                                            inputMediaGame.id.short_name = str3;
                                            inputMediaGame.id.bot_id = MessagesController.getInstance(i).getInputUser((User) res.users.get(0));
                                            SendMessagesHelper.getInstance(i).sendGame(MessagesController.getInstance(i).getInputPeer((int) did), inputMediaGame, 0, 0);
                                            Bundle args = new Bundle();
                                            args.putBoolean("scrollToTopOnResume", true);
                                            int lower_part = (int) did;
                                            int high_id = (int) (did >> 32);
                                            if (lower_part == 0) {
                                                args.putInt("enc_id", high_id);
                                            } else if (high_id == 1) {
                                                args.putInt("chat_id", lower_part);
                                            } else if (lower_part > 0) {
                                                args.putInt("user_id", lower_part);
                                            } else if (lower_part < 0) {
                                                args.putInt("chat_id", -lower_part);
                                            }
                                            if (MessagesController.getInstance(i).checkCanOpenChat(args, fragment)) {
                                                NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                                LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                                            }
                                        }
                                    });
                                    boolean removeLast = AndroidUtilities.isTablet() ? LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() > 0 && (LaunchActivity.this.layersActionBarLayout.fragmentsStack.get(LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity) : LaunchActivity.this.actionBarLayout.fragmentsStack.size() > 1 && (LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity);
                                    LaunchActivity.this.actionBarLayout.presentFragment(fragment, removeLast, true, true);
                                    if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
                                        SecretMediaViewer.getInstance().closePhoto(false, false);
                                    } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                                        PhotoViewer.getInstance().closePhoto(false, true);
                                    } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                                        ArticleViewer.getInstance().close(false, true);
                                    }
                                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                                    if (AndroidUtilities.isTablet()) {
                                        LaunchActivity.this.actionBarLayout.showLastFragment();
                                        LaunchActivity.this.rightActionBarLayout.showLastFragment();
                                        return;
                                    }
                                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                                } else if (str != null) {
                                    final User user = !res.users.isEmpty() ? (User) res.users.get(0) : null;
                                    if (user == null || (user.bot && user.bot_nochats)) {
                                        try {
                                            Toast.makeText(LaunchActivity.this, LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), 0).show();
                                            return;
                                        } catch (Throwable e22) {
                                            FileLog.e(e22);
                                            return;
                                        }
                                    }
                                    args = new Bundle();
                                    args.putBoolean("onlySelect", true);
                                    args.putInt("dialogsType", 2);
                                    args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", R.string.AddToTheGroupTitle, UserObject.getUserName(user), "%1$s"));
                                    fragment = new DialogsActivity(args);
                                    fragment.setDelegate(new DialogsActivityDelegate() {
                                        public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
                                            long did = ((Long) dids.get(0)).longValue();
                                            Bundle args = new Bundle();
                                            args.putBoolean("scrollToTopOnResume", true);
                                            args.putInt("chat_id", -((int) did));
                                            if (LaunchActivity.mainFragmentsStack.isEmpty() || MessagesController.getInstance(i).checkCanOpenChat(args, (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))) {
                                                NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                                MessagesController.getInstance(i).addUserToChat(-((int) did), user, null, 0, str, null);
                                                LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                                            }
                                        }
                                    });
                                    LaunchActivity.this.presentFragment(fragment);
                                } else {
                                    boolean isBot = false;
                                    args = new Bundle();
                                    long dialog_id;
                                    if (res.chats.isEmpty()) {
                                        args.putInt("user_id", ((User) res.users.get(0)).id);
                                        dialog_id = (long) ((User) res.users.get(0)).id;
                                    } else {
                                        args.putInt("chat_id", ((Chat) res.chats.get(0)).id);
                                        dialog_id = (long) (-((Chat) res.chats.get(0)).id);
                                    }
                                    if (str2 != null && res.users.size() > 0 && ((User) res.users.get(0)).bot) {
                                        args.putString("botUser", str2);
                                        isBot = true;
                                    }
                                    if (num != null) {
                                        args.putInt("message_id", num.intValue());
                                    }
                                    BaseFragment lastFragment = !LaunchActivity.mainFragmentsStack.isEmpty() ? (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1) : null;
                                    if (lastFragment != null && !MessagesController.getInstance(i).checkCanOpenChat(args, lastFragment)) {
                                        return;
                                    }
                                    if (isBot && lastFragment != null && (lastFragment instanceof ChatActivity) && ((ChatActivity) lastFragment).getDialogId() == dialog_id) {
                                        ((ChatActivity) lastFragment).setBotUser(str2);
                                        return;
                                    }
                                    ChatActivity fragment2 = new ChatActivity(args);
                                    NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                    LaunchActivity.this.actionBarLayout.presentFragment(fragment2, false, true, true);
                                }
                            }
                        }
                    });
                }
            });
        } else if (group != null) {
            if (state == 0) {
                req = new TL_messages_checkChatInvite();
                req.hash = group;
                final int i2 = intentAccount;
                final String str4 = group;
                str = username;
                str2 = sticker;
                final String str5 = botUser;
                final String str6 = botChat;
                final String str7 = message;
                final boolean z = hasUrl;
                final Integer num2 = messageId;
                final String str8 = game;
                final String[] strArr = instantView;
                requestId = ConnectionsManager.getInstance(intentAccount).sendRequest(req, new RequestDelegate() {
                    public void run(final TLObject response, final TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Throwable e) {
                                        FileLog.e(e);
                                    }
                                    Builder builder;
                                    if (error != null || LaunchActivity.this.actionBarLayout == null) {
                                        builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                        return;
                                    }
                                    ChatInvite invite = response;
                                    if (invite.chat != null && !ChatObject.isLeftFromChat(invite.chat)) {
                                        MessagesController.getInstance(i2).putChat(invite.chat, false);
                                        ArrayList<Chat> chats = new ArrayList();
                                        chats.add(invite.chat);
                                        MessagesStorage.getInstance(i2).putUsersAndChats(null, chats, false, true);
                                        Bundle args = new Bundle();
                                        args.putInt("chat_id", invite.chat.id);
                                        if (LaunchActivity.mainFragmentsStack.isEmpty() || MessagesController.getInstance(i2).checkCanOpenChat(args, (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))) {
                                            ChatActivity fragment = new ChatActivity(args);
                                            NotificationCenter.getInstance(i2).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                            LaunchActivity.this.actionBarLayout.presentFragment(fragment, false, true, true);
                                        }
                                    } else if (((invite.chat != null || (invite.channel && !invite.megagroup)) && (invite.chat == null || (ChatObject.isChannel(invite.chat) && !invite.chat.megagroup))) || LaunchActivity.mainFragmentsStack.isEmpty()) {
                                        builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        String str = "ChannelJoinTo";
                                        Object[] objArr = new Object[1];
                                        objArr[0] = invite.chat != null ? invite.chat.title : invite.title;
                                        builder.setMessage(LocaleController.formatString(str, R.string.ChannelJoinTo, objArr));
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                LaunchActivity.this.runLinkRequest(i2, str, str4, str2, str5, str6, str7, z, num2, str8, strArr, 1);
                                            }
                                        });
                                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                    } else {
                                        BaseFragment fragment2 = (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1);
                                        fragment2.showDialog(new JoinGroupAlert(LaunchActivity.this, invite, str4, fragment2));
                                    }
                                }
                            }
                        });
                    }
                }, 2);
            } else if (state == 1) {
                req = new TL_messages_importChatInvite();
                req.hash = group;
                final int i3 = intentAccount;
                ConnectionsManager.getInstance(intentAccount).sendRequest(req, new RequestDelegate() {
                    public void run(final TLObject response, final TL_error error) {
                        if (error == null) {
                            MessagesController.getInstance(i3).processUpdates((Updates) response, false);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Throwable e) {
                                        FileLog.e(e);
                                    }
                                    if (error != null) {
                                        Builder builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                        } else if (error.text.equals("USERS_TOO_MUCH")) {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", R.string.JoinToGroupErrorFull));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                    } else if (LaunchActivity.this.actionBarLayout != null) {
                                        Updates updates = response;
                                        if (!updates.chats.isEmpty()) {
                                            Chat chat = (Chat) updates.chats.get(0);
                                            chat.left = false;
                                            chat.kicked = false;
                                            MessagesController.getInstance(i3).putUsers(updates.users, false);
                                            MessagesController.getInstance(i3).putChats(updates.chats, false);
                                            Bundle args = new Bundle();
                                            args.putInt("chat_id", chat.id);
                                            if (LaunchActivity.mainFragmentsStack.isEmpty() || MessagesController.getInstance(i3).checkCanOpenChat(args, (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))) {
                                                ChatActivity fragment = new ChatActivity(args);
                                                NotificationCenter.getInstance(i3).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                                LaunchActivity.this.actionBarLayout.presentFragment(fragment, false, true, true);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }, 2);
            }
        } else if (sticker != null) {
            if (!mainFragmentsStack.isEmpty()) {
                TL_inputStickerSetShortName stickerset = new TL_inputStickerSetShortName();
                stickerset.short_name = sticker;
                BaseFragment fragment = (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1);
                fragment.showDialog(new StickersAlert(this, fragment, stickerset, null, null));
                return;
            }
            return;
        } else if (message != null) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            DialogsActivity fragment2 = new DialogsActivity(args);
            final boolean z2 = hasUrl;
            final int i4 = intentAccount;
            final String str9 = message;
            fragment2.setDelegate(new DialogsActivityDelegate() {
                public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence m, boolean param) {
                    long did = ((Long) dids.get(0)).longValue();
                    Bundle args = new Bundle();
                    args.putBoolean("scrollToTopOnResume", true);
                    args.putBoolean("hasUrl", z2);
                    int lower_part = (int) did;
                    int high_id = (int) (did >> 32);
                    if (lower_part == 0) {
                        args.putInt("enc_id", high_id);
                    } else if (high_id == 1) {
                        args.putInt("chat_id", lower_part);
                    } else if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        args.putInt("chat_id", -lower_part);
                    }
                    if (MessagesController.getInstance(i4).checkCanOpenChat(args, fragment)) {
                        NotificationCenter.getInstance(i4).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        DataQuery.getInstance(i4).saveDraft(did, str9, null, null, false);
                        LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                    }
                }
            });
            presentFragment(fragment2, false, true);
        } else if (instantView != null) {
        }
        if (requestId != 0) {
            i3 = intentAccount;
            i4 = requestId;
            progressDialog.setButton(-2, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ConnectionsManager.getInstance(i3).cancelRequest(i4, true);
                    try {
                        dialog.dismiss();
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }
            });
            try {
                progressDialog.show();
            } catch (Exception e) {
            }
        }
    }

    public AlertDialog showAlertDialog(Builder builder) {
        AlertDialog alertDialog = null;
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            this.visibleDialog = builder.show();
            this.visibleDialog.setCanceledOnTouchOutside(true);
            this.visibleDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (LaunchActivity.this.visibleDialog != null && LaunchActivity.this.visibleDialog == LaunchActivity.this.localeDialog) {
                        try {
                            Toast.makeText(LaunchActivity.this, LaunchActivity.this.getStringForLanguageAlert(LocaleController.getInstance().getCurrentLocaleInfo().shortName.equals("en") ? LaunchActivity.this.englishLocaleStrings : LaunchActivity.this.systemLocaleStrings, "ChangeLanguageLater", R.string.ChangeLanguageLater), 1).show();
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                        LaunchActivity.this.localeDialog = null;
                    }
                    LaunchActivity.this.visibleDialog = null;
                }
            });
            return this.visibleDialog;
        } catch (Throwable e2) {
            FileLog.e(e2);
            return alertDialog;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    public void didSelectDialogs(DialogsActivity dialogsFragment, ArrayList<Long> dids, CharSequence message, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        int lower_part = (int) did;
        int high_id = (int) (did >> 32);
        Bundle args = new Bundle();
        int account = dialogsFragment != null ? dialogsFragment.getCurrentAccount() : this.currentAccount;
        args.putBoolean("scrollToTopOnResume", true);
        if (!AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        }
        if (lower_part == 0) {
            args.putInt("enc_id", high_id);
        } else if (high_id == 1) {
            args.putInt("chat_id", lower_part);
        } else if (lower_part > 0) {
            args.putInt("user_id", lower_part);
        } else if (lower_part < 0) {
            args.putInt("chat_id", -lower_part);
        }
        if (MessagesController.getInstance(account).checkCanOpenChat(args, dialogsFragment)) {
            boolean z;
            boolean z2;
            BaseFragment chatActivity = new ChatActivity(args);
            ActionBarLayout actionBarLayout = this.actionBarLayout;
            if (dialogsFragment != null) {
                z = true;
            } else {
                z = false;
            }
            if (dialogsFragment == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            actionBarLayout.presentFragment(chatActivity, z, z2, true);
            if (this.videoPath != null) {
                chatActivity.openVideoEditor(this.videoPath, this.sendingText);
                this.sendingText = null;
            }
            if (this.photoPathsArray != null) {
                if (this.sendingText != null && this.sendingText.length() <= Callback.DEFAULT_DRAG_ANIMATION_DURATION && this.photoPathsArray.size() == 1) {
                    ((SendingMediaInfo) this.photoPathsArray.get(0)).caption = this.sendingText;
                    this.sendingText = null;
                }
                SendMessagesHelper.prepareSendingMedia(this.photoPathsArray, did, null, null, false, false);
            }
            if (this.sendingText != null) {
                SendMessagesHelper.prepareSendingText(this.sendingText, did);
            }
            if (!(this.documentsPathsArray == null && this.documentsUrisArray == null)) {
                SendMessagesHelper.prepareSendingDocuments(this.documentsPathsArray, this.documentsOriginalPathsArray, this.documentsUrisArray, this.documentsMimeType, did, null, null);
            }
            if (!(this.contactsToSend == null || this.contactsToSend.isEmpty())) {
                Iterator it = this.contactsToSend.iterator();
                while (it.hasNext()) {
                    SendMessagesHelper.getInstance(account).sendMessage((User) it.next(), did, null, null, null);
                }
            }
            this.photoPathsArray = null;
            this.videoPath = null;
            this.sendingText = null;
            this.documentsPathsArray = null;
            this.documentsOriginalPathsArray = null;
            this.contactsToSend = null;
        }
    }

    private void onFinish() {
        if (!this.finished) {
            this.finished = true;
            if (this.lockRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
                this.lockRunnable = null;
            }
            if (this.currentAccount != -1) {
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
            }
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.notificationsCountUpdated);
        }
    }

    public void presentFragment(BaseFragment fragment) {
        this.actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(SharedConfig.passcodeHash.length() == 0 || SharedConfig.lastPauseTime == 0)) {
            SharedConfig.lastPauseTime = 0;
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.onActivityResult(requestCode, resultCode, data);
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3 || requestCode == 4 || requestCode == 5 || requestCode == 19 || requestCode == 20) {
            boolean showAlert = true;
            if (grantResults.length > 0 && grantResults[0] == 0) {
                if (requestCode == 4) {
                    ImageLoader.getInstance().checkMediaPaths();
                    return;
                } else if (requestCode == 5) {
                    ContactsController.getInstance(this.currentAccount).forceImportContacts();
                    return;
                } else if (requestCode == 3) {
                    if (SharedConfig.inappCamera) {
                        CameraController.getInstance().initCamera();
                        return;
                    }
                    return;
                } else if (requestCode == 19 || requestCode == 20) {
                    showAlert = false;
                }
            }
            if (showAlert) {
                Builder builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (requestCode == 3) {
                    builder.setMessage(LocaleController.getString("PermissionNoAudio", R.string.PermissionNoAudio));
                } else if (requestCode == 4) {
                    builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                } else if (requestCode == 5) {
                    builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                } else if (requestCode == 19 || requestCode == 20) {
                    builder.setMessage(LocaleController.getString("PermissionNoCamera", R.string.PermissionNoCamera));
                }
                builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), new DialogInterface.OnClickListener() {
                    @TargetApi(9)
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                            LaunchActivity.this.startActivity(intent);
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                    }
                });
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                builder.show();
                return;
            }
        } else if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == 0) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.locationPermissionGranted, new Object[0]);
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        SharedConfig.lastAppPauseTime = System.currentTimeMillis();
        ApplicationLoader.mainInterfacePaused = true;
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                ApplicationLoader.mainInterfacePausedStageQueue = true;
                ApplicationLoader.mainInterfacePausedStageQueueTime = 0;
            }
        });
        onPasscodePause();
        this.actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onPause();
            this.layersActionBarLayout.onPause();
        }
        if (this.passcodeView != null) {
            this.passcodeView.onPause();
        }
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
        AndroidUtilities.unregisterUpdates();
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onPause();
        }
    }

    protected void onStart() {
        super.onStart();
        Browser.bindCustomTabsService(this);
    }

    protected void onStop() {
        super.onStop();
        Browser.unbindCustomTabsService(this);
    }

    protected void onDestroy() {
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
        if (StickerPreviewViewer.hasInstance()) {
            StickerPreviewViewer.getInstance().destroy();
        }
        PipRoundVideoView pipRoundVideoView = PipRoundVideoView.getInstance();
        MediaController.getInstance().setBaseActivity(this, false);
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, false);
        if (pipRoundVideoView != null) {
            pipRoundVideoView.close(false);
        }
        Theme.destroyResources();
        EmbedBottomSheet embedBottomSheet = EmbedBottomSheet.getInstance();
        if (embedBottomSheet != null) {
            embedBottomSheet.destroy();
        }
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.destroy();
        }
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            if (this.onGlobalLayoutListener != null) {
                getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
            }
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
        super.onDestroy();
        onFinish();
    }

    protected void onResume() {
        super.onResume();
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, true);
        showLanguageAlert(false);
        ApplicationLoader.mainInterfacePaused = false;
        NotificationsController.lastNoDataNotificationTime = 0;
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                ApplicationLoader.mainInterfacePausedStageQueue = false;
                ApplicationLoader.mainInterfacePausedStageQueueTime = System.currentTimeMillis();
            }
        });
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
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
        updateCurrentConnectionState(this.currentAccount);
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }
        if (PipRoundVideoView.getInstance() != null && MediaController.getInstance().isMessagePaused()) {
            MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
            if (messageObject != null) {
                MediaController.getInstance().seekToProgress(messageObject, messageObject.audioProgress);
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        AndroidUtilities.checkDisplaySize(this, newConfig);
        super.onConfigurationChanged(newConfig);
        checkLayout();
        PipRoundVideoView pipRoundVideoView = PipRoundVideoView.getInstance();
        if (pipRoundVideoView != null) {
            pipRoundVideoView.onConfigurationChanged();
        }
        EmbedBottomSheet embedBottomSheet = EmbedBottomSheet.getInstance();
        if (embedBottomSheet != null) {
            embedBottomSheet.onConfigurationChanged(newConfig);
        }
        PhotoViewer photoViewer = PhotoViewer.getPipInstance();
        if (photoViewer != null) {
            photoViewer.onConfigurationChanged(newConfig);
        }
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.onConfigurationChanged();
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        AndroidUtilities.isInMultiwindow = isInMultiWindowMode;
        checkLayout();
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            switchToAvailableAccountOrLogout();
        } else if (id == NotificationCenter.closeOtherAppActivities) {
            if (args[0] != this) {
                onFinish();
                finish();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            if (this.currentConnectionState != state) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("switch to state " + state);
                }
                this.currentConnectionState = state;
                updateCurrentConnectionState(account);
            }
        } else if (id == NotificationCenter.mainUserInfoChanged) {
            this.drawerLayoutAdapter.notifyDataSetChanged();
        } else if (id == NotificationCenter.needShowAlert) {
            Integer reason = args[0];
            builder = new Builder((Context) this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            if (reason.intValue() != 2) {
                final int i = account;
                builder.setNegativeButton(LocaleController.getString("MoreInfo", R.string.MoreInfo), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!LaunchActivity.mainFragmentsStack.isEmpty()) {
                            MessagesController.getInstance(i).openByUserName("spambot", (BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1), 1);
                        }
                    }
                });
            }
            if (reason.intValue() == 0) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam1", R.string.NobodyLikesSpam1));
            } else if (reason.intValue() == 1) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam2", R.string.NobodyLikesSpam2));
            } else if (reason.intValue() == 2) {
                builder.setMessage((String) args[1]);
            }
            if (!mainFragmentsStack.isEmpty()) {
                ((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(builder.create());
            }
        } else if (id == NotificationCenter.wasUnableToFindCurrentLocation) {
            HashMap<String, MessageObject> waitingForLocation = args[0];
            builder = new Builder((Context) this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            final HashMap<String, MessageObject> hashMap = waitingForLocation;
            final int i2 = account;
            builder.setNegativeButton(LocaleController.getString("ShareYouLocationUnableManually", R.string.ShareYouLocationUnableManually), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!LaunchActivity.mainFragmentsStack.isEmpty() && AndroidUtilities.isGoogleMapsInstalled((BaseFragment) LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))) {
                        LocationActivity fragment = new LocationActivity(0);
                        fragment.setDelegate(new LocationActivityDelegate() {
                            public void didSelectLocation(MessageMedia location, int live) {
                                for (Entry<String, MessageObject> entry : hashMap.entrySet()) {
                                    MessageObject messageObject = (MessageObject) entry.getValue();
                                    SendMessagesHelper.getInstance(i2).sendMessage(location, messageObject.getDialogId(), messageObject, null, null);
                                }
                            }
                        });
                        LaunchActivity.this.presentFragment(fragment);
                    }
                }
            });
            builder.setMessage(LocaleController.getString("ShareYouLocationUnable", R.string.ShareYouLocationUnable));
            if (!mainFragmentsStack.isEmpty()) {
                ((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(builder.create());
            }
        } else if (id == NotificationCenter.didSetNewWallpapper) {
            if (this.sideMenu != null) {
                child = this.sideMenu.getChildAt(0);
                if (child != null) {
                    child.invalidate();
                }
            }
        } else if (id == NotificationCenter.didSetPasscode) {
            if (SharedConfig.passcodeHash.length() <= 0 || SharedConfig.allowScreenCapture) {
                try {
                    getWindow().clearFlags(MessagesController.UPDATE_MASK_CHANNEL);
                    return;
                } catch (Throwable e) {
                    FileLog.e(e);
                    return;
                }
            }
            try {
                getWindow().setFlags(MessagesController.UPDATE_MASK_CHANNEL, MessagesController.UPDATE_MASK_CHANNEL);
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        } else if (id == NotificationCenter.reloadInterface) {
            rebuildAllFragments(false);
        } else if (id == NotificationCenter.suggestedLangpack) {
            showLanguageAlert(false);
        } else if (id == NotificationCenter.openArticle) {
            if (!mainFragmentsStack.isEmpty()) {
                ArticleViewer.getInstance().setParentActivity(this, (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1));
                ArticleViewer.getInstance().open((TL_webPage) args[0], (String) args[1]);
            }
        } else if (id == NotificationCenter.hasNewContactsToImport) {
            if (this.actionBarLayout != null && !this.actionBarLayout.fragmentsStack.isEmpty()) {
                int type = ((Integer) args[0]).intValue();
                final HashMap<String, Contact> contactHashMap = args[1];
                final boolean first = ((Boolean) args[2]).booleanValue();
                final boolean schedule = ((Boolean) args[3]).booleanValue();
                BaseFragment fragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
                builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("UpdateContactsTitle", R.string.UpdateContactsTitle));
                builder.setMessage(LocaleController.getString("UpdateContactsMessage", R.string.UpdateContactsMessage));
                final int i3 = account;
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContactsController.getInstance(i3).syncPhoneBookByAlert(contactHashMap, first, schedule, false);
                    }
                });
                i3 = account;
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContactsController.getInstance(i3).syncPhoneBookByAlert(contactHashMap, first, schedule, true);
                    }
                });
                i3 = account;
                builder.setOnBackButtonListener(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContactsController.getInstance(i3).syncPhoneBookByAlert(contactHashMap, first, schedule, true);
                    }
                });
                AlertDialog dialog = builder.create();
                fragment.showDialog(dialog);
                dialog.setCanceledOnTouchOutside(false);
            }
        } else if (id == NotificationCenter.didSetNewTheme) {
            if (!args[0].booleanValue()) {
                if (this.sideMenu != null) {
                    this.sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
                    this.sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
                    this.sideMenu.getAdapter().notifyDataSetChanged();
                }
                if (VERSION.SDK_INT >= 21) {
                    try {
                        setTaskDescription(new TaskDescription(null, null, Theme.getColor(Theme.key_actionBarDefault) | Theme.ACTION_BAR_VIDEO_EDIT_COLOR));
                    } catch (Exception e3) {
                    }
                }
            }
        } else if (id == NotificationCenter.needSetDayNightTheme) {
            ThemeInfo theme = args[0];
            this.actionBarLayout.animateThemedValues(theme);
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.animateThemedValues(theme);
                this.rightActionBarLayout.animateThemedValues(theme);
            }
        } else if (id == NotificationCenter.notificationsCountUpdated && this.sideMenu != null) {
            Integer accountNum = args[0];
            int count = this.sideMenu.getChildCount();
            for (int a = 0; a < count; a++) {
                child = this.sideMenu.getChildAt(a);
                if ((child instanceof DrawerUserCell) && ((DrawerUserCell) child).getAccountNumber() == accountNum.intValue()) {
                    child.invalidate();
                    return;
                }
            }
        }
    }

    private String getStringForLanguageAlert(HashMap<String, String> map, String key, int intKey) {
        String value = (String) map.get(key);
        if (value == null) {
            return LocaleController.getString(key, intKey);
        }
        return value;
    }

    private void checkFreeDiscSpace() {
        if (VERSION.SDK_INT < 26) {
            Utilities.globalQueue.postRunnable(new Runnable() {
                public void run() {
                    if (UserConfig.getInstance(LaunchActivity.this.currentAccount).isClientActivated()) {
                        try {
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            if (Math.abs(preferences.getLong("last_space_check", 0) - System.currentTimeMillis()) >= 259200000) {
                                File path = FileLoader.getDirectory(4);
                                if (path != null) {
                                    long freeSpace;
                                    StatFs statFs = new StatFs(path.getAbsolutePath());
                                    if (VERSION.SDK_INT < 18) {
                                        freeSpace = (long) Math.abs(statFs.getAvailableBlocks() * statFs.getBlockSize());
                                    } else {
                                        freeSpace = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
                                    }
                                    preferences.edit().putLong("last_space_check", System.currentTimeMillis()).commit();
                                    if (freeSpace < 104857600) {
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    AlertsCreator.createFreeSpaceDialog(LaunchActivity.this).show();
                                                } catch (Throwable th) {
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Throwable th) {
                        }
                    }
                }
            }, AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS);
        }
    }

    private void showLanguageAlertInternal(LocaleInfo systemInfo, LocaleInfo englishInfo, String systemLang) {
        try {
            LocaleInfo localeInfo;
            this.loadingLocaleDialog = false;
            boolean firstSystem = systemInfo.builtIn || LocaleController.getInstance().isCurrentLocalLocale();
            Builder builder = new Builder((Context) this);
            builder.setTitle(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguage", R.string.ChooseYourLanguage));
            builder.setSubtitle(getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguage", R.string.ChooseYourLanguage));
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            final LanguageCell[] cells = new LanguageCell[2];
            final LocaleInfo[] selectedLanguage = new LocaleInfo[1];
            LocaleInfo[] locales = new LocaleInfo[2];
            String englishName = getStringForLanguageAlert(this.systemLocaleStrings, "English", R.string.English);
            if (firstSystem) {
                localeInfo = systemInfo;
            } else {
                localeInfo = englishInfo;
            }
            locales[0] = localeInfo;
            if (firstSystem) {
                localeInfo = englishInfo;
            } else {
                localeInfo = systemInfo;
            }
            locales[1] = localeInfo;
            if (!firstSystem) {
                systemInfo = englishInfo;
            }
            selectedLanguage[0] = systemInfo;
            int a = 0;
            while (a < 2) {
                cells[a] = new LanguageCell(this, true);
                cells[a].setLanguage(locales[a], locales[a] == englishInfo ? englishName : null, true);
                cells[a].setTag(Integer.valueOf(a));
                cells[a].setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 2));
                cells[a].setLanguageSelected(a == 0);
                linearLayout.addView(cells[a], LayoutHelper.createLinear(-1, 48));
                cells[a].setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();
                        selectedLanguage[0] = ((LanguageCell) v).getCurrentLocale();
                        for (int a = 0; a < cells.length; a++) {
                            boolean z;
                            LanguageCell languageCell = cells[a];
                            if (a == tag.intValue()) {
                                z = true;
                            } else {
                                z = false;
                            }
                            languageCell.setLanguageSelected(z);
                        }
                    }
                });
                a++;
            }
            LanguageCell cell = new LanguageCell(this, true);
            cell.setValue(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguageOther", R.string.ChooseYourLanguageOther), getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguageOther", R.string.ChooseYourLanguageOther));
            cell.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    LaunchActivity.this.localeDialog = null;
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(true);
                    LaunchActivity.this.presentFragment(new LanguageSelectActivity());
                    if (LaunchActivity.this.visibleDialog != null) {
                        LaunchActivity.this.visibleDialog.dismiss();
                        LaunchActivity.this.visibleDialog = null;
                    }
                }
            });
            linearLayout.addView(cell, LayoutHelper.createLinear(-1, 48));
            builder.setView(linearLayout);
            builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LocaleController.getInstance().applyLanguage(selectedLanguage[0], true, false, LaunchActivity.this.currentAccount);
                    LaunchActivity.this.rebuildAllFragments(true);
                }
            });
            this.localeDialog = showAlertDialog(builder);
            MessagesController.getGlobalMainSettings().edit().putString("language_showed2", systemLang).commit();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private void showLanguageAlert(boolean force) {
        try {
            if (!this.loadingLocaleDialog) {
                String showedLang = MessagesController.getGlobalMainSettings().getString("language_showed2", TtmlNode.ANONYMOUS_REGION_ID);
                final String systemLang = LocaleController.getSystemLocaleStringIso639().toLowerCase();
                if (force || !showedLang.equals(systemLang)) {
                    String arg;
                    final LocaleInfo[] infos = new LocaleInfo[2];
                    if (systemLang.contains("-")) {
                        arg = systemLang.split("-")[0];
                    } else {
                        arg = systemLang;
                    }
                    String alias;
                    if ("in".equals(arg)) {
                        alias = TtmlNode.ATTR_ID;
                    } else if ("iw".equals(arg)) {
                        alias = "he";
                    } else if ("jw".equals(arg)) {
                        alias = "jv";
                    } else {
                        alias = null;
                    }
                    for (int a = 0; a < LocaleController.getInstance().languages.size(); a++) {
                        LocaleInfo info = (LocaleInfo) LocaleController.getInstance().languages.get(a);
                        if (info.shortName.equals("en")) {
                            infos[0] = info;
                        }
                        if (info.shortName.replace("_", "-").equals(systemLang) || info.shortName.equals(arg) || (alias != null && info.shortName.equals(alias))) {
                            infos[1] = info;
                        }
                        if (infos[0] != null && infos[1] != null) {
                            break;
                        }
                    }
                    if (infos[0] != null && infos[1] != null && infos[0] != infos[1]) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("show lang alert for " + infos[0].getKey() + " and " + infos[1].getKey());
                        }
                        this.systemLocaleStrings = null;
                        this.englishLocaleStrings = null;
                        this.loadingLocaleDialog = true;
                        TL_langpack_getStrings req = new TL_langpack_getStrings();
                        req.lang_code = infos[1].shortName.replace("_", "-");
                        req.keys.add("English");
                        req.keys.add("ChooseYourLanguage");
                        req.keys.add("ChooseYourLanguageOther");
                        req.keys.add("ChangeLanguageLater");
                        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {
                            public void run(TLObject response, TL_error error) {
                                final HashMap<String, String> keys = new HashMap();
                                if (response != null) {
                                    Vector vector = (Vector) response;
                                    for (int a = 0; a < vector.objects.size(); a++) {
                                        LangPackString string = (LangPackString) vector.objects.get(a);
                                        keys.put(string.key, string.value);
                                    }
                                }
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        LaunchActivity.this.systemLocaleStrings = keys;
                                        if (LaunchActivity.this.englishLocaleStrings != null && LaunchActivity.this.systemLocaleStrings != null) {
                                            LaunchActivity.this.showLanguageAlertInternal(infos[1], infos[0], systemLang);
                                        }
                                    }
                                });
                            }
                        }, 8);
                        req = new TL_langpack_getStrings();
                        req.lang_code = infos[0].shortName.replace("_", "-");
                        req.keys.add("English");
                        req.keys.add("ChooseYourLanguage");
                        req.keys.add("ChooseYourLanguageOther");
                        req.keys.add("ChangeLanguageLater");
                        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {
                            public void run(TLObject response, TL_error error) {
                                final HashMap<String, String> keys = new HashMap();
                                if (response != null) {
                                    Vector vector = (Vector) response;
                                    for (int a = 0; a < vector.objects.size(); a++) {
                                        LangPackString string = (LangPackString) vector.objects.get(a);
                                        keys.put(string.key, string.value);
                                    }
                                }
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        LaunchActivity.this.englishLocaleStrings = keys;
                                        if (LaunchActivity.this.englishLocaleStrings != null && LaunchActivity.this.systemLocaleStrings != null) {
                                            LaunchActivity.this.showLanguageAlertInternal(infos[1], infos[0], systemLang);
                                        }
                                    }
                                });
                            }
                        }, 8);
                    }
                } else if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("alert already showed for " + showedLang);
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private void onPasscodePause() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (SharedConfig.passcodeHash.length() != 0) {
            SharedConfig.lastPauseTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
            this.lockRunnable = new Runnable() {
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
                        LaunchActivity.this.lockRunnable = null;
                    }
                }
            };
            if (SharedConfig.appLocked) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, 1000);
            } else if (SharedConfig.autoLockIn != 0) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, (((long) SharedConfig.autoLockIn) * 1000) + 1000);
            }
        } else {
            SharedConfig.lastPauseTime = 0;
        }
        SharedConfig.saveConfig();
    }

    private void onPasscodeResume() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (AndroidUtilities.needShowPasscode(true)) {
            showPasscodeActivity();
        }
        if (SharedConfig.lastPauseTime != 0) {
            SharedConfig.lastPauseTime = 0;
            SharedConfig.saveConfig();
        }
    }

    private void updateCurrentConnectionState(int account) {
        if (this.actionBarLayout != null) {
            String title = null;
            String subtitle = null;
            Runnable action = null;
            if (this.currentConnectionState == 2) {
                title = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
            } else if (this.currentConnectionState == 1) {
                title = LocaleController.getString("Connecting", R.string.Connecting);
                action = new Runnable() {
                    public void run() {
                        if (AndroidUtilities.isTablet()) {
                            if (!LaunchActivity.layerFragmentsStack.isEmpty() && (LaunchActivity.layerFragmentsStack.get(LaunchActivity.layerFragmentsStack.size() - 1) instanceof ProxySettingsActivity)) {
                                return;
                            }
                        } else if (!LaunchActivity.mainFragmentsStack.isEmpty() && (LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1) instanceof ProxySettingsActivity)) {
                            return;
                        }
                        LaunchActivity.this.presentFragment(new ProxySettingsActivity());
                    }
                };
            } else if (this.currentConnectionState == 5) {
                title = LocaleController.getString("Updating", R.string.Updating);
            } else if (this.currentConnectionState == 4) {
                title = LocaleController.getString("ConnectingToProxy", R.string.ConnectingToProxy);
                subtitle = LocaleController.getString("ConnectingToProxyTapToDisable", R.string.ConnectingToProxyTapToDisable);
                action = new Runnable() {
                    public void run() {
                        if (LaunchActivity.this.actionBarLayout != null && !LaunchActivity.this.actionBarLayout.fragmentsStack.isEmpty()) {
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            BaseFragment fragment = (BaseFragment) LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1);
                            Builder builder = new Builder(LaunchActivity.this);
                            builder.setTitle(LocaleController.getString("Proxy", R.string.Proxy));
                            builder.setMessage(LocaleController.formatString("ConnectingToProxyDisableAlert", R.string.ConnectingToProxyDisableAlert, preferences.getString("proxy_ip", TtmlNode.ANONYMOUS_REGION_ID)));
                            builder.setPositiveButton(LocaleController.getString("ConnectingToProxyDisable", R.string.ConnectingToProxyDisable), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Editor editor = MessagesController.getGlobalMainSettings().edit();
                                    editor.putBoolean("proxy_enabled", false);
                                    editor.commit();
                                    for (int a = 0; a < 3; a++) {
                                        ConnectionsManager.native_setProxySettings(a, TtmlNode.ANONYMOUS_REGION_ID, 0, TtmlNode.ANONYMOUS_REGION_ID, TtmlNode.ANONYMOUS_REGION_ID);
                                    }
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged, new Object[0]);
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            fragment.showDialog(builder.create());
                        }
                    }
                };
            }
            this.actionBarLayout.setTitleOverlayText(title, subtitle, action);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
                }
            } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
            }
            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
                if ((lastFragment instanceof ChatActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof SettingsActivity) {
                    outState.putString("fragment", "settings");
                } else if ((lastFragment instanceof GroupCreateFinalActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof WallpapersActivity) {
                    outState.putString("fragment", "wallpapers");
                } else if ((lastFragment instanceof ProfileActivity) && ((ProfileActivity) lastFragment).isChat() && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat_profile");
                } else if ((lastFragment instanceof ChannelCreateActivity) && args != null && args.getInt("step") == 0) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "channel");
                } else if ((lastFragment instanceof ChannelEditActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "edit");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void onBackPressed() {
        if (this.passcodeView.getVisibility() == 0) {
            finish();
        } else if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
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
            boolean cancel = false;
            if (this.rightActionBarLayout.getVisibility() == 0 && !this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                cancel = !((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() + -1)).onBackPressed();
            }
            if (!cancel) {
                this.actionBarLayout.onBackPressed();
            }
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onLowMemory();
            this.layersActionBarLayout.onLowMemory();
        }
    }

    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        try {
            Menu menu = mode.getMenu();
            if (!(menu == null || this.actionBarLayout.extendActionMode(menu) || !AndroidUtilities.isTablet() || this.rightActionBarLayout.extendActionMode(menu))) {
                this.layersActionBarLayout.extendActionMode(menu);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeStarted(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeStarted(mode);
                this.layersActionBarLayout.onActionModeStarted(mode);
            }
        }
    }

    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeFinished(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeFinished(mode);
                this.layersActionBarLayout.onActionModeFinished(mode);
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

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 82 && !SharedConfig.isWaitingForPasscodeEnter) {
            if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                return super.onKeyUp(keyCode, event);
            }
            if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                return super.onKeyUp(keyCode, event);
            }
            if (AndroidUtilities.isTablet()) {
                if (this.layersActionBarLayout.getVisibility() == 0 && !this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    this.layersActionBarLayout.onKeyUp(keyCode, event);
                } else if (this.rightActionBarLayout.getVisibility() != 0 || this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    this.actionBarLayout.onKeyUp(keyCode, event);
                } else {
                    this.rightActionBarLayout.onKeyUp(keyCode, event);
                }
            } else if (this.actionBarLayout.fragmentsStack.size() != 1) {
                this.actionBarLayout.onKeyUp(keyCode, event);
            } else if (this.drawerLayoutContainer.isDrawerOpened()) {
                this.drawerLayoutContainer.closeDrawer(false);
            } else {
                if (getCurrentFocus() != null) {
                    AndroidUtilities.hideKeyboard(getCurrentFocus());
                }
                this.drawerLayoutContainer.openDrawer(false);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        boolean z = true;
        if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
            ArticleViewer.getInstance().close(false, true);
        }
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            boolean z2 = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z2, true);
            if ((fragment instanceof DialogsActivity) && ((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                this.actionBarLayout.removeAllFragments();
                this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                this.layersActionBarLayout.removeAllFragments();
                this.layersActionBarLayout.setVisibility(8);
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                if (this.tabletFullSize) {
                    return false;
                }
                this.shadowTabletSide.setVisibility(0);
                if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    return false;
                }
                this.backgroundTablet.setVisibility(0);
                return false;
            } else if (fragment instanceof ChatActivity) {
                int a;
                ActionBarLayout actionBarLayout;
                if ((!this.tabletFullSize && layout == this.rightActionBarLayout) || (this.tabletFullSize && layout == this.actionBarLayout)) {
                    boolean result;
                    if (this.tabletFullSize && layout == this.actionBarLayout && this.actionBarLayout.fragmentsStack.size() == 1) {
                        result = false;
                    } else {
                        result = true;
                    }
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        actionBarLayout = this.layersActionBarLayout;
                        if (forceWithoutAnimation) {
                            z = false;
                        }
                        actionBarLayout.closeLastFragment(z);
                    }
                    if (!result) {
                        this.actionBarLayout.presentFragment(fragment, false, forceWithoutAnimation, false);
                    }
                    return result;
                } else if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.presentFragment(fragment, removeLast, true, false);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    actionBarLayout = this.layersActionBarLayout;
                    if (forceWithoutAnimation) {
                        z = false;
                    }
                    actionBarLayout.closeLastFragment(z);
                    return false;
                } else if (!this.tabletFullSize || layout == this.actionBarLayout) {
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        r7 = this.layersActionBarLayout;
                        if (forceWithoutAnimation) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        r7.closeLastFragment(z2);
                    }
                    actionBarLayout = this.actionBarLayout;
                    if (this.actionBarLayout.fragmentsStack.size() <= 1) {
                        z = false;
                    }
                    actionBarLayout.presentFragment(fragment, z, forceWithoutAnimation, false);
                    return false;
                } else {
                    r7 = this.actionBarLayout;
                    if (this.actionBarLayout.fragmentsStack.size() > 1) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    r7.presentFragment(fragment, z2, forceWithoutAnimation, false);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    actionBarLayout = this.layersActionBarLayout;
                    if (forceWithoutAnimation) {
                        z = false;
                    }
                    actionBarLayout.closeLastFragment(z);
                    return false;
                }
            } else if (layout == this.layersActionBarLayout) {
                return true;
            } else {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
                }
                this.layersActionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                return false;
            }
        }
        boolean allow = true;
        if (fragment instanceof LoginActivity) {
            if (mainFragmentsStack.size() == 0) {
                allow = false;
            }
        } else if ((fragment instanceof CountrySelectActivity) && mainFragmentsStack.size() == 1) {
            allow = false;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(allow, false);
        return true;
    }

    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            boolean z = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z, true);
            if (fragment instanceof DialogsActivity) {
                if (((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                    this.actionBarLayout.removeAllFragments();
                    this.actionBarLayout.addFragmentToStack(fragment);
                    this.layersActionBarLayout.removeAllFragments();
                    this.layersActionBarLayout.setVisibility(8);
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (this.tabletFullSize) {
                        return false;
                    }
                    this.shadowTabletSide.setVisibility(0);
                    if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    this.backgroundTablet.setVisibility(0);
                    return false;
                }
            } else if (fragment instanceof ChatActivity) {
                int a;
                if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                } else if (this.tabletFullSize && layout != this.actionBarLayout) {
                    this.actionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                }
            } else if (layout != this.layersActionBarLayout) {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
                }
                this.layersActionBarLayout.addFragmentToStack(fragment);
                return false;
            }
            return true;
        }
        boolean allow = true;
        if (fragment instanceof LoginActivity) {
            if (mainFragmentsStack.size() == 0) {
                allow = false;
            }
        } else if ((fragment instanceof CountrySelectActivity) && mainFragmentsStack.size() == 1) {
            allow = false;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(allow, false);
        return true;
    }

    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == this.actionBarLayout && layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == this.rightActionBarLayout) {
                if (!this.tabletFullSize) {
                    this.backgroundTablet.setVisibility(0);
                }
            } else if (layout == this.layersActionBarLayout && this.actionBarLayout.fragmentsStack.isEmpty() && this.layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else if (layout.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        } else if (layout.fragmentsStack.size() >= 2 && !(layout.fragmentsStack.get(0) instanceof LoginActivity)) {
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
        return true;
    }

    public void rebuildAllFragments(boolean last) {
        if (this.layersActionBarLayout != null) {
            this.layersActionBarLayout.rebuildAllFragmentViews(last, last);
        } else {
            this.actionBarLayout.rebuildAllFragmentViews(last, last);
        }
    }

    public void onRebuildAllFragments(ActionBarLayout layout, boolean last) {
        if (AndroidUtilities.isTablet() && layout == this.layersActionBarLayout) {
            this.rightActionBarLayout.rebuildAllFragmentViews(last, last);
            this.actionBarLayout.rebuildAllFragmentViews(last, last);
        }
        this.drawerLayoutAdapter.notifyDataSetChanged();
    }
}
