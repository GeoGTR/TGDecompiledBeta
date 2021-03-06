package org.telegram.ui.Components.voip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$PhoneCallDiscardReason;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputPhoneCall;
import org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC$TL_phone_setCallRating;
import org.telegram.tgnet.TLRPC$TL_updates;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$UserFull;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.GroupCallActivity;
import org.telegram.ui.LaunchActivity;

public class VoIPHelper {
    public static long lastCallTime;

    static /* synthetic */ void lambda$showRateAlert$8(DialogInterface dialogInterface, int i) {
    }

    public static void startCall(TLRPC$User tLRPC$User, boolean z, boolean z2, Activity activity, TLRPC$UserFull tLRPC$UserFull) {
        String str;
        int i;
        String str2;
        int i2;
        boolean z3 = true;
        if (tLRPC$UserFull != null && tLRPC$UserFull.phone_calls_private) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) activity);
            builder.setTitle(LocaleController.getString("VoipFailed", NUM));
            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("CallNotAvailable", NUM, ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name))));
            builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
            builder.show();
        } else if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != 3) {
            if (Settings.System.getInt(activity.getContentResolver(), "airplane_mode_on", 0) == 0) {
                z3 = false;
            }
            AlertDialog.Builder builder2 = new AlertDialog.Builder((Context) activity);
            if (z3) {
                i = NUM;
                str = "VoipOfflineAirplaneTitle";
            } else {
                i = NUM;
                str = "VoipOfflineTitle";
            }
            builder2.setTitle(LocaleController.getString(str, i));
            if (z3) {
                i2 = NUM;
                str2 = "VoipOfflineAirplane";
            } else {
                i2 = NUM;
                str2 = "VoipOffline";
            }
            builder2.setMessage(LocaleController.getString(str2, i2));
            builder2.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
            if (z3) {
                Intent intent = new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    builder2.setNeutralButton(LocaleController.getString("VoipOfflineOpenSettings", NUM), new DialogInterface.OnClickListener(activity, intent) {
                        public final /* synthetic */ Activity f$0;
                        public final /* synthetic */ Intent f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        public final void onClick(DialogInterface dialogInterface, int i) {
                            this.f$0.startActivity(this.f$1);
                        }
                    });
                }
            }
            try {
                builder2.show();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            ArrayList arrayList = new ArrayList();
            if (activity.checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
                arrayList.add("android.permission.RECORD_AUDIO");
            }
            if (z && activity.checkSelfPermission("android.permission.CAMERA") != 0) {
                arrayList.add("android.permission.CAMERA");
            }
            if (arrayList.isEmpty()) {
                initiateCall(tLRPC$User, (TLRPC$Chat) null, z, z2, false, activity);
            } else {
                activity.requestPermissions((String[]) arrayList.toArray(new String[0]), z ? 102 : 101);
            }
        } else {
            initiateCall(tLRPC$User, (TLRPC$Chat) null, z, z2, false, activity);
        }
    }

    public static void startCall(TLRPC$Chat tLRPC$Chat, boolean z, Activity activity) {
        int i;
        String str;
        int i2;
        String str2;
        if (activity != null) {
            boolean z2 = false;
            if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != 3) {
                if (Settings.System.getInt(activity.getContentResolver(), "airplane_mode_on", 0) != 0) {
                    z2 = true;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder((Context) activity);
                if (z2) {
                    i = NUM;
                    str = "VoipOfflineAirplaneTitle";
                } else {
                    i = NUM;
                    str = "VoipOfflineTitle";
                }
                builder.setTitle(LocaleController.getString(str, i));
                if (z2) {
                    i2 = NUM;
                    str2 = "VoipGroupOfflineAirplane";
                } else {
                    i2 = NUM;
                    str2 = "VoipGroupOffline";
                }
                builder.setMessage(LocaleController.getString(str2, i2));
                builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
                if (z2) {
                    Intent intent = new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        builder.setNeutralButton(LocaleController.getString("VoipOfflineOpenSettings", NUM), new DialogInterface.OnClickListener(activity, intent) {
                            public final /* synthetic */ Activity f$0;
                            public final /* synthetic */ Intent f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onClick(DialogInterface dialogInterface, int i) {
                                this.f$0.startActivity(this.f$1);
                            }
                        });
                    }
                }
                try {
                    builder.show();
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            } else if (Build.VERSION.SDK_INT >= 23) {
                ArrayList arrayList = new ArrayList();
                if (activity.checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
                    arrayList.add("android.permission.RECORD_AUDIO");
                }
                if (arrayList.isEmpty()) {
                    initiateCall((TLRPC$User) null, tLRPC$Chat, false, false, z, activity);
                } else {
                    activity.requestPermissions((String[]) arrayList.toArray(new String[0]), 103);
                }
            } else {
                initiateCall((TLRPC$User) null, tLRPC$Chat, false, false, z, activity);
            }
        }
    }

    private static void initiateCall(TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, boolean z, boolean z2, boolean z3, Activity activity) {
        String str;
        String str2;
        int i;
        String str3;
        String str4;
        int i2;
        if (activity == null) {
            return;
        }
        if (tLRPC$User != null || tLRPC$Chat != null) {
            if (VoIPService.getSharedInstance() != null) {
                int i3 = tLRPC$User != null ? tLRPC$User.id : -tLRPC$Chat.id;
                int callerId = VoIPService.getSharedInstance().getCallerId();
                if (callerId != i3) {
                    if (callerId > 0) {
                        TLRPC$User user = VoIPService.getSharedInstance().getUser();
                        str2 = ContactsController.formatName(user.first_name, user.last_name);
                        if (i3 > 0) {
                            i = NUM;
                            str = "VoipOngoingAlert";
                        } else {
                            i = NUM;
                            str = "VoipOngoingAlert2";
                        }
                    } else {
                        str2 = VoIPService.getSharedInstance().getChat().title;
                        if (i3 > 0) {
                            i = NUM;
                            str = "VoipOngoingChatAlert2";
                        } else {
                            i = NUM;
                            str = "VoipOngoingChatAlert";
                        }
                    }
                    if (tLRPC$User != null) {
                        str3 = ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name);
                    } else {
                        str3 = tLRPC$Chat.title;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder((Context) activity);
                    if (callerId < 0) {
                        i2 = NUM;
                        str4 = "VoipOngoingChatAlertTitle";
                    } else {
                        i2 = NUM;
                        str4 = "VoipOngoingAlertTitle";
                    }
                    builder.setTitle(LocaleController.getString(str4, i2));
                    builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString(str, i, str2, str3)));
                    builder.setPositiveButton(LocaleController.getString("OK", NUM), new DialogInterface.OnClickListener(tLRPC$Chat, z, z2, z3, activity) {
                        public final /* synthetic */ TLRPC$Chat f$1;
                        public final /* synthetic */ boolean f$2;
                        public final /* synthetic */ boolean f$3;
                        public final /* synthetic */ boolean f$4;
                        public final /* synthetic */ Activity f$5;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                        }

                        public final void onClick(DialogInterface dialogInterface, int i) {
                            VoIPHelper.lambda$initiateCall$3(TLRPC$User.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dialogInterface, i);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                    builder.show();
                } else if (tLRPC$User != null || !(activity instanceof LaunchActivity)) {
                    activity.startActivity(new Intent(activity, LaunchActivity.class).setAction(tLRPC$User != null ? "voip" : "voip_chat"));
                } else {
                    GroupCallActivity.create((LaunchActivity) activity, AccountInstance.getInstance(UserConfig.selectedAccount));
                }
            } else if (VoIPService.callIShouldHavePutIntoIntent == null) {
                doInitiateCall(tLRPC$User, tLRPC$Chat, z, z2, z3, activity);
            }
        }
    }

    static /* synthetic */ void lambda$initiateCall$3(TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, boolean z, boolean z2, boolean z3, Activity activity, DialogInterface dialogInterface, int i) {
        if (VoIPService.getSharedInstance() != null) {
            VoIPService.getSharedInstance().hangUp((Runnable) new Runnable(tLRPC$Chat, z, z2, z3, activity) {
                public final /* synthetic */ TLRPC$Chat f$1;
                public final /* synthetic */ boolean f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ boolean f$4;
                public final /* synthetic */ Activity f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    VoIPHelper.lambda$null$2(TLRPC$User.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        } else {
            doInitiateCall(tLRPC$User, tLRPC$Chat, z, z2, z3, activity);
        }
    }

    static /* synthetic */ void lambda$null$2(TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, boolean z, boolean z2, boolean z3, Activity activity) {
        lastCallTime = 0;
        doInitiateCall(tLRPC$User, tLRPC$Chat, z, z2, z3, activity);
    }

    private static void doInitiateCall(TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, boolean z, boolean z2, boolean z3, Activity activity) {
        if (activity == null) {
            return;
        }
        if (tLRPC$User != null || tLRPC$Chat != null) {
            if (SystemClock.elapsedRealtime() - lastCallTime >= ((long) (tLRPC$Chat != null ? 200 : 2000))) {
                lastCallTime = SystemClock.elapsedRealtime();
                Intent intent = new Intent(activity, VoIPService.class);
                if (tLRPC$User != null) {
                    intent.putExtra("user_id", tLRPC$User.id);
                } else {
                    intent.putExtra("chat_id", tLRPC$Chat.id);
                    intent.putExtra("createGroupCall", z3);
                }
                boolean z4 = true;
                intent.putExtra("is_outgoing", true);
                intent.putExtra("start_incall_activity", true);
                int i = Build.VERSION.SDK_INT;
                intent.putExtra("video_call", i >= 18 && z);
                if (i < 18 || !z2) {
                    z4 = false;
                }
                intent.putExtra("can_video_call", z4);
                intent.putExtra("account", UserConfig.selectedAccount);
                try {
                    activity.startService(intent);
                } catch (Throwable th) {
                    FileLog.e(th);
                }
            }
        }
    }

    @TargetApi(23)
    public static void permissionDenied(Activity activity, Runnable runnable, int i) {
        int i2;
        String str;
        if (!activity.shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO") || (i == 102 && !activity.shouldShowRequestPermissionRationale("android.permission.CAMERA"))) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) activity);
            builder.setTitle(LocaleController.getString("AppName", NUM));
            if (i == 102) {
                i2 = NUM;
                str = "VoipNeedMicCameraPermission";
            } else {
                i2 = NUM;
                str = "VoipNeedMicPermission";
            }
            builder.setMessage(LocaleController.getString(str, i2));
            builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
            builder.setNegativeButton(LocaleController.getString("Settings", NUM), new DialogInterface.OnClickListener(activity) {
                public final /* synthetic */ Activity f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    VoIPHelper.lambda$permissionDenied$4(this.f$0, dialogInterface, i);
                }
            });
            builder.show().setOnDismissListener(new DialogInterface.OnDismissListener(runnable) {
                public final /* synthetic */ Runnable f$0;

                {
                    this.f$0 = r1;
                }

                public final void onDismiss(DialogInterface dialogInterface) {
                    VoIPHelper.lambda$permissionDenied$5(this.f$0, dialogInterface);
                }
            });
        }
    }

    static /* synthetic */ void lambda$permissionDenied$4(Activity activity, DialogInterface dialogInterface, int i) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", activity.getPackageName(), (String) null));
        activity.startActivity(intent);
    }

    static /* synthetic */ void lambda$permissionDenied$5(Runnable runnable, DialogInterface dialogInterface) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public static File getLogsDir() {
        File file = new File(ApplicationLoader.applicationContext.getCacheDir(), "voip_logs");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static boolean canRateCall(TLRPC$TL_messageActionPhoneCall tLRPC$TL_messageActionPhoneCall) {
        TLRPC$PhoneCallDiscardReason tLRPC$PhoneCallDiscardReason = tLRPC$TL_messageActionPhoneCall.reason;
        if (!(tLRPC$PhoneCallDiscardReason instanceof TLRPC$TL_phoneCallDiscardReasonBusy) && !(tLRPC$PhoneCallDiscardReason instanceof TLRPC$TL_phoneCallDiscardReasonMissed)) {
            for (String split : MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET)) {
                String[] split2 = split.split(" ");
                if (split2.length >= 2) {
                    String str = split2[0];
                    if (str.equals(tLRPC$TL_messageActionPhoneCall.call_id + "")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void showRateAlert(Context context, TLRPC$TL_messageActionPhoneCall tLRPC$TL_messageActionPhoneCall) {
        for (String split : MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET)) {
            String[] split2 = split.split(" ");
            if (split2.length >= 2) {
                String str = split2[0];
                if (str.equals(tLRPC$TL_messageActionPhoneCall.call_id + "")) {
                    try {
                        long parseLong = Long.parseLong(split2[1]);
                        showRateAlert(context, (Runnable) null, tLRPC$TL_messageActionPhoneCall.video, tLRPC$TL_messageActionPhoneCall.call_id, parseLong, UserConfig.selectedAccount, true);
                        return;
                    } catch (Exception unused) {
                        return;
                    }
                }
            }
        }
    }

    public static void showRateAlert(Context context, Runnable runnable, boolean z, long j, long j2, int i, boolean z2) {
        String str;
        Context context2 = context;
        File logFile = getLogFile(j);
        int i2 = 1;
        int[] iArr = {0};
        LinearLayout linearLayout = new LinearLayout(context2);
        linearLayout.setOrientation(1);
        int dp = AndroidUtilities.dp(16.0f);
        linearLayout.setPadding(dp, dp, dp, 0);
        TextView textView = new TextView(context2);
        textView.setTextSize(2, 16.0f);
        textView.setTextColor(Theme.getColor("dialogTextBlack"));
        textView.setGravity(17);
        textView.setText(LocaleController.getString("VoipRateCallAlert", NUM));
        linearLayout.addView(textView);
        BetterRatingView betterRatingView = new BetterRatingView(context2);
        linearLayout.addView(betterRatingView, LayoutHelper.createLinear(-2, -2, 1, 0, 16, 0, 0));
        LinearLayout linearLayout2 = new LinearLayout(context2);
        linearLayout2.setOrientation(1);
        $$Lambda$VoIPHelper$ARdI_M2FTeOkKSZcoxB92CCJ204 r8 = $$Lambda$VoIPHelper$ARdI_M2FTeOkKSZcoxB92CCJ204.INSTANCE;
        String[] strArr = new String[9];
        strArr[0] = z ? "distorted_video" : null;
        strArr[1] = z ? "pixelated_video" : null;
        strArr[2] = "echo";
        strArr[3] = "noise";
        strArr[4] = "interruptions";
        strArr[5] = "distorted_speech";
        strArr[6] = "silent_local";
        strArr[7] = "silent_remote";
        strArr[8] = "dropped";
        int i3 = 0;
        for (int i4 = 9; i3 < i4; i4 = 9) {
            if (strArr[i3] != null) {
                CheckBoxCell checkBoxCell = new CheckBoxCell(context2, i2);
                checkBoxCell.setClipToPadding(false);
                checkBoxCell.setTag(strArr[i3]);
                switch (i3) {
                    case 0:
                        str = LocaleController.getString("RateCallVideoDistorted", NUM);
                        break;
                    case 1:
                        str = LocaleController.getString("RateCallVideoPixelated", NUM);
                        break;
                    case 2:
                        str = LocaleController.getString("RateCallEcho", NUM);
                        break;
                    case 3:
                        str = LocaleController.getString("RateCallNoise", NUM);
                        break;
                    case 4:
                        str = LocaleController.getString("RateCallInterruptions", NUM);
                        break;
                    case 5:
                        str = LocaleController.getString("RateCallDistorted", NUM);
                        break;
                    case 6:
                        str = LocaleController.getString("RateCallSilentLocal", NUM);
                        break;
                    case 7:
                        str = LocaleController.getString("RateCallSilentRemote", NUM);
                        break;
                    case 8:
                        str = LocaleController.getString("RateCallDropped", NUM);
                        break;
                    default:
                        str = null;
                        break;
                }
                checkBoxCell.setText(str, (String) null, false, false);
                checkBoxCell.setOnClickListener(r8);
                checkBoxCell.setTag(strArr[i3]);
                linearLayout2.addView(checkBoxCell);
            }
            i3++;
            i2 = 1;
        }
        linearLayout.addView(linearLayout2, LayoutHelper.createLinear(-1, -2, -8.0f, 0.0f, -8.0f, 0.0f));
        linearLayout2.setVisibility(8);
        EditTextBoldCursor editTextBoldCursor = new EditTextBoldCursor(context2);
        editTextBoldCursor.setHint(LocaleController.getString("VoipFeedbackCommentHint", NUM));
        editTextBoldCursor.setInputType(147457);
        editTextBoldCursor.setTextColor(Theme.getColor("dialogTextBlack"));
        editTextBoldCursor.setHintTextColor(Theme.getColor("dialogTextHint"));
        editTextBoldCursor.setBackgroundDrawable(Theme.createEditTextDrawable(context2, true));
        editTextBoldCursor.setPadding(0, AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f));
        editTextBoldCursor.setTextSize(1, 18.0f);
        editTextBoldCursor.setVisibility(8);
        linearLayout.addView(editTextBoldCursor, LayoutHelper.createLinear(-1, -2, 8.0f, 8.0f, 8.0f, 0.0f));
        boolean[] zArr = {true};
        CheckBoxCell checkBoxCell2 = new CheckBoxCell(context2, 1);
        $$Lambda$VoIPHelper$49ITCA95fuuoYwIqLNPHUhdmsUw r4 = new View.OnClickListener(zArr, checkBoxCell2) {
            public final /* synthetic */ boolean[] f$0;
            public final /* synthetic */ CheckBoxCell f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                VoIPHelper.lambda$showRateAlert$7(this.f$0, this.f$1, view);
            }
        };
        checkBoxCell2.setText(LocaleController.getString("CallReportIncludeLogs", NUM), (String) null, true, false);
        checkBoxCell2.setClipToPadding(false);
        checkBoxCell2.setOnClickListener(r4);
        linearLayout.addView(checkBoxCell2, LayoutHelper.createLinear(-1, -2, -8.0f, 0.0f, -8.0f, 0.0f));
        TextView textView2 = new TextView(context2);
        textView2.setTextSize(2, 14.0f);
        textView2.setTextColor(Theme.getColor("dialogTextGray3"));
        textView2.setText(LocaleController.getString("CallReportLogsExplain", NUM));
        textView2.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), 0);
        textView2.setOnClickListener(r4);
        linearLayout.addView(textView2);
        checkBoxCell2.setVisibility(8);
        textView2.setVisibility(8);
        if (!logFile.exists()) {
            zArr[0] = false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context2);
        builder.setTitle(LocaleController.getString("CallMessageReportProblem", NUM));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Send", NUM), $$Lambda$VoIPHelper$mLgqaM6RG3yA9DyZ_KdeYOfvGI0.INSTANCE);
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener(runnable) {
            public final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            public final void onDismiss(DialogInterface dialogInterface) {
                VoIPHelper.lambda$showRateAlert$9(this.f$0, dialogInterface);
            }
        });
        AlertDialog create = builder.create();
        if (BuildVars.LOGS_ENABLED && logFile.exists()) {
            create.setNeutralButton("Send log", new DialogInterface.OnClickListener(context2, logFile) {
                public final /* synthetic */ Context f$0;
                public final /* synthetic */ File f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    VoIPHelper.lambda$showRateAlert$10(this.f$0, this.f$1, dialogInterface, i);
                }
            });
        }
        create.show();
        create.getWindow().setSoftInputMode(3);
        View button = create.getButton(-1);
        button.setEnabled(false);
        betterRatingView.setOnRatingChangeListener(new BetterRatingView.OnRatingChangeListener(button) {
            public final /* synthetic */ View f$0;

            {
                this.f$0 = r1;
            }

            public final void onRatingChanged(int i) {
                VoIPHelper.lambda$showRateAlert$11(this.f$0, i);
            }
        });
        $$Lambda$VoIPHelper$AO_oXq5aTvGdQrZlZeO0gH7tYvQ r27 = r0;
        $$Lambda$VoIPHelper$AO_oXq5aTvGdQrZlZeO0gH7tYvQ r0 = new View.OnClickListener(betterRatingView, iArr, linearLayout2, editTextBoldCursor, zArr, j2, j, z2, i, logFile, context, create, textView, checkBoxCell2, textView2, button) {
            public final /* synthetic */ BetterRatingView f$0;
            public final /* synthetic */ int[] f$1;
            public final /* synthetic */ Context f$10;
            public final /* synthetic */ AlertDialog f$11;
            public final /* synthetic */ TextView f$12;
            public final /* synthetic */ CheckBoxCell f$13;
            public final /* synthetic */ TextView f$14;
            public final /* synthetic */ View f$15;
            public final /* synthetic */ LinearLayout f$2;
            public final /* synthetic */ EditTextBoldCursor f$3;
            public final /* synthetic */ boolean[] f$4;
            public final /* synthetic */ long f$5;
            public final /* synthetic */ long f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ int f$8;
            public final /* synthetic */ File f$9;

            {
                this.f$0 = r4;
                this.f$1 = r5;
                this.f$2 = r6;
                this.f$3 = r7;
                this.f$4 = r8;
                this.f$5 = r9;
                this.f$6 = r11;
                this.f$7 = r13;
                this.f$8 = r14;
                this.f$9 = r15;
                this.f$10 = r16;
                this.f$11 = r17;
                this.f$12 = r18;
                this.f$13 = r19;
                this.f$14 = r20;
                this.f$15 = r21;
            }

            public final void onClick(View view) {
                View view2 = view;
                BetterRatingView betterRatingView = this.f$0;
                BetterRatingView betterRatingView2 = betterRatingView;
                VoIPHelper.lambda$showRateAlert$13(betterRatingView2, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, view2);
            }
        };
        button.setOnClickListener(r27);
    }

    static /* synthetic */ void lambda$showRateAlert$6(View view) {
        CheckBoxCell checkBoxCell = (CheckBoxCell) view;
        checkBoxCell.setChecked(!checkBoxCell.isChecked(), true);
    }

    static /* synthetic */ void lambda$showRateAlert$7(boolean[] zArr, CheckBoxCell checkBoxCell, View view) {
        zArr[0] = !zArr[0];
        checkBoxCell.setChecked(zArr[0], true);
    }

    static /* synthetic */ void lambda$showRateAlert$9(Runnable runnable, DialogInterface dialogInterface) {
        if (runnable != null) {
            runnable.run();
        }
    }

    static /* synthetic */ void lambda$showRateAlert$10(Context context, File file, DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(context, LaunchActivity.class);
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
        context.startActivity(intent);
    }

    static /* synthetic */ void lambda$showRateAlert$11(View view, int i) {
        int i2;
        String str;
        view.setEnabled(i > 0);
        TextView textView = (TextView) view;
        if (i < 4) {
            i2 = NUM;
            str = "Next";
        } else {
            i2 = NUM;
            str = "Send";
        }
        textView.setText(LocaleController.getString(str, i2).toUpperCase());
    }

    static /* synthetic */ void lambda$showRateAlert$13(BetterRatingView betterRatingView, int[] iArr, LinearLayout linearLayout, EditTextBoldCursor editTextBoldCursor, boolean[] zArr, long j, long j2, boolean z, int i, File file, Context context, AlertDialog alertDialog, TextView textView, CheckBoxCell checkBoxCell, TextView textView2, View view, View view2) {
        LinearLayout linearLayout2 = linearLayout;
        if (betterRatingView.getRating() >= 4 || iArr[0] == 1) {
            BetterRatingView betterRatingView2 = betterRatingView;
            EditTextBoldCursor editTextBoldCursor2 = editTextBoldCursor;
            AlertDialog alertDialog2 = alertDialog;
            int i2 = UserConfig.selectedAccount;
            TLRPC$TL_phone_setCallRating tLRPC$TL_phone_setCallRating = new TLRPC$TL_phone_setCallRating();
            tLRPC$TL_phone_setCallRating.rating = betterRatingView.getRating();
            ArrayList arrayList = new ArrayList();
            for (int i3 = 0; i3 < linearLayout.getChildCount(); i3++) {
                CheckBoxCell checkBoxCell2 = (CheckBoxCell) linearLayout.getChildAt(i3);
                if (checkBoxCell2.isChecked()) {
                    arrayList.add("#" + checkBoxCell2.getTag());
                }
            }
            if (tLRPC$TL_phone_setCallRating.rating < 5) {
                tLRPC$TL_phone_setCallRating.comment = editTextBoldCursor.getText().toString();
            } else {
                tLRPC$TL_phone_setCallRating.comment = "";
            }
            if (!arrayList.isEmpty() && !zArr[0]) {
                tLRPC$TL_phone_setCallRating.comment += " " + TextUtils.join(" ", arrayList);
            }
            TLRPC$TL_inputPhoneCall tLRPC$TL_inputPhoneCall = new TLRPC$TL_inputPhoneCall();
            tLRPC$TL_phone_setCallRating.peer = tLRPC$TL_inputPhoneCall;
            tLRPC$TL_inputPhoneCall.access_hash = j;
            tLRPC$TL_inputPhoneCall.id = j2;
            tLRPC$TL_phone_setCallRating.user_initiative = z;
            ConnectionsManager.getInstance(i).sendRequest(tLRPC$TL_phone_setCallRating, new RequestDelegate(i2, zArr, file, tLRPC$TL_phone_setCallRating, arrayList, context) {
                public final /* synthetic */ int f$0;
                public final /* synthetic */ boolean[] f$1;
                public final /* synthetic */ File f$2;
                public final /* synthetic */ TLRPC$TL_phone_setCallRating f$3;
                public final /* synthetic */ ArrayList f$4;
                public final /* synthetic */ Context f$5;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    VoIPHelper.lambda$null$12(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, tLObject, tLRPC$TL_error);
                }
            });
            alertDialog.dismiss();
            return;
        }
        iArr[0] = 1;
        BetterRatingView betterRatingView3 = betterRatingView;
        betterRatingView.setVisibility(8);
        textView.setVisibility(8);
        alertDialog.setTitle(LocaleController.getString("CallReportHint", NUM));
        editTextBoldCursor.setVisibility(0);
        if (file.exists()) {
            checkBoxCell.setVisibility(0);
            textView2.setVisibility(0);
        }
        linearLayout.setVisibility(0);
        ((TextView) view).setText(LocaleController.getString("Send", NUM).toUpperCase());
    }

    static /* synthetic */ void lambda$null$12(int i, boolean[] zArr, File file, TLRPC$TL_phone_setCallRating tLRPC$TL_phone_setCallRating, ArrayList arrayList, Context context, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        TLObject tLObject2 = tLObject;
        if (tLObject2 instanceof TLRPC$TL_updates) {
            MessagesController.getInstance(i).processUpdates((TLRPC$TL_updates) tLObject2, false);
        }
        if (zArr[0] && file.exists() && tLRPC$TL_phone_setCallRating.rating < 4) {
            SendMessagesHelper.prepareSendingDocument(AccountInstance.getInstance(UserConfig.selectedAccount), file.getAbsolutePath(), file.getAbsolutePath(), (Uri) null, TextUtils.join(" ", arrayList), "text/plain", 4244000, (MessageObject) null, (MessageObject) null, (InputContentInfoCompat) null, (MessageObject) null, true, 0);
            Toast.makeText(context, LocaleController.getString("CallReportSent", NUM), 1).show();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = new java.io.File(org.telegram.messenger.ApplicationLoader.applicationContext.getExternalFilesDir((java.lang.String) null), "logs");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.io.File getLogFile(long r7) {
        /*
            boolean r0 = org.telegram.messenger.BuildVars.DEBUG_VERSION
            if (r0 == 0) goto L_0x0043
            java.io.File r0 = new java.io.File
            android.content.Context r1 = org.telegram.messenger.ApplicationLoader.applicationContext
            r2 = 0
            java.io.File r1 = r1.getExternalFilesDir(r2)
            java.lang.String r2 = "logs"
            r0.<init>(r1, r2)
            java.lang.String[] r1 = r0.list()
            if (r1 == 0) goto L_0x0043
            int r2 = r1.length
            r3 = 0
        L_0x001a:
            if (r3 >= r2) goto L_0x0043
            r4 = r1[r3]
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "voip"
            r5.append(r6)
            r5.append(r7)
            java.lang.String r6 = ".txt"
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            boolean r5 = r4.endsWith(r5)
            if (r5 == 0) goto L_0x0040
            java.io.File r7 = new java.io.File
            r7.<init>(r0, r4)
            return r7
        L_0x0040:
            int r3 = r3 + 1
            goto L_0x001a
        L_0x0043:
            java.io.File r0 = new java.io.File
            java.io.File r1 = getLogsDir()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r7)
            java.lang.String r7 = ".log"
            r2.append(r7)
            java.lang.String r7 = r2.toString()
            r0.<init>(r1, r7)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.voip.VoIPHelper.getLogFile(long):java.io.File");
    }

    public static void showCallDebugSettings(Context context) {
        SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        TextView textView = new TextView(context);
        textView.setTextSize(1, 15.0f);
        textView.setText("Please only change these settings if you know exactly what they do.");
        textView.setTextColor(Theme.getColor("dialogTextBlack"));
        linearLayout.addView(textView, LayoutHelper.createLinear(-1, -2, 16.0f, 8.0f, 16.0f, 8.0f));
        TextCheckCell textCheckCell = new TextCheckCell(context);
        textCheckCell.setTextAndCheck("Force TCP", globalMainSettings.getBoolean("dbg_force_tcp_in_calls", false), false);
        textCheckCell.setOnClickListener(new View.OnClickListener(globalMainSettings, textCheckCell) {
            public final /* synthetic */ SharedPreferences f$0;
            public final /* synthetic */ TextCheckCell f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                VoIPHelper.lambda$showCallDebugSettings$14(this.f$0, this.f$1, view);
            }
        });
        linearLayout.addView(textCheckCell);
        if (BuildVars.DEBUG_VERSION && BuildVars.LOGS_ENABLED) {
            TextCheckCell textCheckCell2 = new TextCheckCell(context);
            textCheckCell2.setTextAndCheck("Dump detailed stats", globalMainSettings.getBoolean("dbg_dump_call_stats", false), false);
            textCheckCell2.setOnClickListener(new View.OnClickListener(globalMainSettings, textCheckCell2) {
                public final /* synthetic */ SharedPreferences f$0;
                public final /* synthetic */ TextCheckCell f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    VoIPHelper.lambda$showCallDebugSettings$15(this.f$0, this.f$1, view);
                }
            });
            linearLayout.addView(textCheckCell2);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            TextCheckCell textCheckCell3 = new TextCheckCell(context);
            textCheckCell3.setTextAndCheck("Enable ConnectionService", globalMainSettings.getBoolean("dbg_force_connection_service", false), false);
            textCheckCell3.setOnClickListener(new View.OnClickListener(globalMainSettings, textCheckCell3) {
                public final /* synthetic */ SharedPreferences f$0;
                public final /* synthetic */ TextCheckCell f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    VoIPHelper.lambda$showCallDebugSettings$16(this.f$0, this.f$1, view);
                }
            });
            linearLayout.addView(textCheckCell3);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("DebugMenuCallSettings", NUM));
        builder.setView(linearLayout);
        builder.show();
    }

    static /* synthetic */ void lambda$showCallDebugSettings$14(SharedPreferences sharedPreferences, TextCheckCell textCheckCell, View view) {
        boolean z = sharedPreferences.getBoolean("dbg_force_tcp_in_calls", false);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("dbg_force_tcp_in_calls", !z);
        edit.commit();
        textCheckCell.setChecked(!z);
    }

    static /* synthetic */ void lambda$showCallDebugSettings$15(SharedPreferences sharedPreferences, TextCheckCell textCheckCell, View view) {
        boolean z = sharedPreferences.getBoolean("dbg_dump_call_stats", false);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("dbg_dump_call_stats", !z);
        edit.commit();
        textCheckCell.setChecked(!z);
    }

    static /* synthetic */ void lambda$showCallDebugSettings$16(SharedPreferences sharedPreferences, TextCheckCell textCheckCell, View view) {
        boolean z = sharedPreferences.getBoolean("dbg_force_connection_service", false);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("dbg_force_connection_service", !z);
        edit.commit();
        textCheckCell.setChecked(!z);
    }

    public static int getDataSavingDefault() {
        boolean z = DownloadController.getInstance(0).lowPreset.lessCallData;
        boolean z2 = DownloadController.getInstance(0).mediumPreset.lessCallData;
        boolean z3 = DownloadController.getInstance(0).highPreset.lessCallData;
        if (!z && !z2 && !z3) {
            return 0;
        }
        if (z && !z2 && !z3) {
            return 3;
        }
        if (z && z2 && !z3) {
            return 1;
        }
        if (z && z2 && z3) {
            return 2;
        }
        if (BuildVars.LOGS_ENABLED) {
            FileLog.w("Invalid call data saving preset configuration: " + z + "/" + z2 + "/" + z3);
        }
        return 0;
    }

    public static String getLogFilePath(String str) {
        Calendar instance = Calendar.getInstance();
        return new File(ApplicationLoader.applicationContext.getExternalFilesDir((String) null), String.format(Locale.US, "logs/%02d_%02d_%04d_%02d_%02d_%02d_%s.txt", new Object[]{Integer.valueOf(instance.get(5)), Integer.valueOf(instance.get(2) + 1), Integer.valueOf(instance.get(1)), Integer.valueOf(instance.get(11)), Integer.valueOf(instance.get(12)), Integer.valueOf(instance.get(13)), str})).getAbsolutePath();
    }

    public static String getLogFilePath(long j, boolean z) {
        File[] listFiles;
        File logsDir = getLogsDir();
        if (!BuildVars.DEBUG_VERSION && (listFiles = logsDir.listFiles()) != null) {
            ArrayList arrayList = new ArrayList(Arrays.asList(listFiles));
            while (arrayList.size() > 20) {
                File file = (File) arrayList.get(0);
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    File file2 = (File) it.next();
                    if (file2.getName().endsWith(".log") && file2.lastModified() < file.lastModified()) {
                        file = file2;
                    }
                }
                file.delete();
                arrayList.remove(file);
            }
        }
        return new File(logsDir, j + ".log").getAbsolutePath();
    }

    public static void showGroupCallAlert(BaseFragment baseFragment, TLRPC$Chat tLRPC$Chat, boolean z) {
        if (baseFragment != null && baseFragment.getParentActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) baseFragment.getParentActivity());
            builder.setTitle(LocaleController.getString("StartVoipChatTitle", NUM));
            if (z) {
                builder.setMessage(LocaleController.getString("VoipGroupEndedStartNew", NUM));
            } else {
                builder.setMessage(LocaleController.getString("StartVoipChatAlertText", NUM));
            }
            builder.setPositiveButton(LocaleController.getString("Start", NUM), new DialogInterface.OnClickListener(tLRPC$Chat) {
                public final /* synthetic */ TLRPC$Chat f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    VoIPHelper.lambda$showGroupCallAlert$17(BaseFragment.this, this.f$1, dialogInterface, i);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
            baseFragment.showDialog(builder.create());
        }
    }

    static /* synthetic */ void lambda$showGroupCallAlert$17(BaseFragment baseFragment, TLRPC$Chat tLRPC$Chat, DialogInterface dialogInterface, int i) {
        if (baseFragment.getParentActivity() != null) {
            startCall(tLRPC$Chat, true, baseFragment.getParentActivity());
        }
    }
}
