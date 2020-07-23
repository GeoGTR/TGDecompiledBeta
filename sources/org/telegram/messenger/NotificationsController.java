package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$Dialog;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$TL_account_updateNotifySettings;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputNotifyBroadcasts;
import org.telegram.tgnet.TLRPC$TL_inputNotifyChats;
import org.telegram.tgnet.TLRPC$TL_inputNotifyPeer;
import org.telegram.tgnet.TLRPC$TL_inputNotifyUsers;
import org.telegram.tgnet.TLRPC$TL_inputPeerNotifySettings;
import org.telegram.tgnet.TLRPC$TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC$TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.BubbleActivity;
import org.telegram.ui.PopupNotificationActivity;

public class NotificationsController extends BaseController {
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static volatile NotificationsController[] Instance = new NotificationsController[3];
    public static String OTHER_NOTIFICATIONS_CHANNEL = null;
    public static final int SETTING_MUTE_2_DAYS = 2;
    public static final int SETTING_MUTE_8_HOURS = 1;
    public static final int SETTING_MUTE_FOREVER = 3;
    public static final int SETTING_MUTE_HOUR = 0;
    public static final int SETTING_MUTE_UNMUTE = 4;
    public static final int TYPE_CHANNEL = 2;
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_PRIVATE = 1;
    protected static AudioManager audioManager = ((AudioManager) ApplicationLoader.applicationContext.getSystemService("audio"));
    public static long globalSecretChatId = -4294967296L;
    /* access modifiers changed from: private */
    public static NotificationManagerCompat notificationManager;
    private static DispatchQueue notificationsQueue = new DispatchQueue("notificationsQueue");
    private static NotificationManager systemNotificationManager;
    private AlarmManager alarmManager;
    private ArrayList<MessageObject> delayedPushMessages = new ArrayList<>();
    private LongSparseArray<MessageObject> fcmRandomMessagesDict = new LongSparseArray<>();
    private boolean inChatSoundEnabled;
    private int lastBadgeCount = -1;
    private int lastButtonId = 5000;
    private int lastOnlineFromOtherDevice = 0;
    private long lastSoundOutPlay;
    private long lastSoundPlay;
    private LongSparseArray<Integer> lastWearNotifiedMessageId = new LongSparseArray<>();
    private String launcherClassName;
    private Runnable notificationDelayRunnable;
    private PowerManager.WakeLock notificationDelayWakelock;
    private String notificationGroup;
    private int notificationId = (this.currentAccount + 1);
    private boolean notifyCheck = false;
    private HashSet<Long> openedInBubbleDialogs = new HashSet<>();
    private long opened_dialog_id = 0;
    private int personal_count = 0;
    public ArrayList<MessageObject> popupMessages = new ArrayList<>();
    public ArrayList<MessageObject> popupReplyMessages = new ArrayList<>();
    private LongSparseArray<Integer> pushDialogs = new LongSparseArray<>();
    private LongSparseArray<Integer> pushDialogsOverrideMention = new LongSparseArray<>();
    private ArrayList<MessageObject> pushMessages = new ArrayList<>();
    private LongSparseArray<MessageObject> pushMessagesDict = new LongSparseArray<>();
    public boolean showBadgeMessages;
    public boolean showBadgeMuted;
    public boolean showBadgeNumber;
    private LongSparseArray<Point> smartNotificationsDialogs = new LongSparseArray<>();
    private int soundIn;
    private boolean soundInLoaded;
    private int soundOut;
    private boolean soundOutLoaded;
    private SoundPool soundPool;
    private int soundRecord;
    private boolean soundRecordLoaded;
    private int total_unread_count = 0;
    private LongSparseArray<Integer> wearNotificationsIds = new LongSparseArray<>();

    static /* synthetic */ void lambda$updateServerNotificationsSettings$37(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
    }

    static /* synthetic */ void lambda$updateServerNotificationsSettings$38(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
    }

    public String getGlobalNotificationsKey(int i) {
        return i == 0 ? "EnableGroup2" : i == 1 ? "EnableAll2" : "EnableChannel2";
    }

    static {
        notificationManager = null;
        systemNotificationManager = null;
        if (Build.VERSION.SDK_INT >= 26 && ApplicationLoader.applicationContext != null) {
            notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
            systemNotificationManager = (NotificationManager) ApplicationLoader.applicationContext.getSystemService("notification");
            checkOtherNotificationsChannel();
        }
    }

    public static NotificationsController getInstance(int i) {
        NotificationsController notificationsController = Instance[i];
        if (notificationsController == null) {
            synchronized (NotificationsController.class) {
                notificationsController = Instance[i];
                if (notificationsController == null) {
                    NotificationsController[] notificationsControllerArr = Instance;
                    NotificationsController notificationsController2 = new NotificationsController(i);
                    notificationsControllerArr[i] = notificationsController2;
                    notificationsController = notificationsController2;
                }
            }
        }
        return notificationsController;
    }

    public NotificationsController(int i) {
        super(i);
        StringBuilder sb = new StringBuilder();
        sb.append("messages");
        int i2 = this.currentAccount;
        sb.append(i2 == 0 ? "" : Integer.valueOf(i2));
        this.notificationGroup = sb.toString();
        SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
        this.inChatSoundEnabled = notificationsSettings.getBoolean("EnableInChatSound", true);
        this.showBadgeNumber = notificationsSettings.getBoolean("badgeNumber", true);
        this.showBadgeMuted = notificationsSettings.getBoolean("badgeNumberMuted", false);
        this.showBadgeMessages = notificationsSettings.getBoolean("badgeNumberMessages", true);
        notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
        systemNotificationManager = (NotificationManager) ApplicationLoader.applicationContext.getSystemService("notification");
        try {
            audioManager = (AudioManager) ApplicationLoader.applicationContext.getSystemService("audio");
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            this.alarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService("alarm");
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
        try {
            PowerManager.WakeLock newWakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, "telegram:notification_delay_lock");
            this.notificationDelayWakelock = newWakeLock;
            newWakeLock.setReferenceCounted(false);
        } catch (Exception e3) {
            FileLog.e((Throwable) e3);
        }
        this.notificationDelayRunnable = new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$new$0$NotificationsController();
            }
        };
    }

    public /* synthetic */ void lambda$new$0$NotificationsController() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("delay reached");
        }
        if (!this.delayedPushMessages.isEmpty()) {
            showOrUpdateNotification(true);
            this.delayedPushMessages.clear();
        }
        try {
            if (this.notificationDelayWakelock.isHeld()) {
                this.notificationDelayWakelock.release();
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    public static void checkOtherNotificationsChannel() {
        SharedPreferences sharedPreferences;
        if (Build.VERSION.SDK_INT >= 26) {
            if (OTHER_NOTIFICATIONS_CHANNEL == null) {
                sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                OTHER_NOTIFICATIONS_CHANNEL = sharedPreferences.getString("OtherKey", "Other3");
            } else {
                sharedPreferences = null;
            }
            NotificationChannel notificationChannel = systemNotificationManager.getNotificationChannel(OTHER_NOTIFICATIONS_CHANNEL);
            if (notificationChannel != null && notificationChannel.getImportance() == 0) {
                systemNotificationManager.deleteNotificationChannel(OTHER_NOTIFICATIONS_CHANNEL);
                OTHER_NOTIFICATIONS_CHANNEL = null;
                notificationChannel = null;
            }
            if (OTHER_NOTIFICATIONS_CHANNEL == null) {
                if (sharedPreferences == null) {
                    sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                }
                OTHER_NOTIFICATIONS_CHANNEL = "Other" + Utilities.random.nextLong();
                sharedPreferences.edit().putString("OtherKey", OTHER_NOTIFICATIONS_CHANNEL).commit();
            }
            if (notificationChannel == null) {
                NotificationChannel notificationChannel2 = new NotificationChannel(OTHER_NOTIFICATIONS_CHANNEL, "Other", 3);
                notificationChannel2.enableLights(false);
                notificationChannel2.enableVibration(false);
                notificationChannel2.setSound((Uri) null, (AudioAttributes) null);
                systemNotificationManager.createNotificationChannel(notificationChannel2);
            }
        }
    }

    public void cleanup() {
        this.popupMessages.clear();
        this.popupReplyMessages.clear();
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$cleanup$1$NotificationsController();
            }
        });
    }

    public /* synthetic */ void lambda$cleanup$1$NotificationsController() {
        this.opened_dialog_id = 0;
        this.total_unread_count = 0;
        this.personal_count = 0;
        this.pushMessages.clear();
        this.pushMessagesDict.clear();
        this.fcmRandomMessagesDict.clear();
        this.pushDialogs.clear();
        this.wearNotificationsIds.clear();
        this.lastWearNotifiedMessageId.clear();
        this.openedInBubbleDialogs.clear();
        this.delayedPushMessages.clear();
        this.notifyCheck = false;
        this.lastBadgeCount = 0;
        try {
            if (this.notificationDelayWakelock.isHeld()) {
                this.notificationDelayWakelock.release();
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        dismissNotification();
        setBadge(getTotalAllUnreadCount());
        SharedPreferences.Editor edit = getAccountInstance().getNotificationsSettings().edit();
        edit.clear();
        edit.commit();
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                String str = this.currentAccount + "channel";
                List<NotificationChannel> notificationChannels = systemNotificationManager.getNotificationChannels();
                int size = notificationChannels.size();
                for (int i = 0; i < size; i++) {
                    String id = notificationChannels.get(i).getId();
                    if (id.startsWith(str)) {
                        systemNotificationManager.deleteNotificationChannel(id);
                    }
                }
            } catch (Throwable th) {
                FileLog.e(th);
            }
        }
    }

    public void setInChatSoundEnabled(boolean z) {
        this.inChatSoundEnabled = z;
    }

    public /* synthetic */ void lambda$setOpenedDialogId$2$NotificationsController(long j) {
        this.opened_dialog_id = j;
    }

    public void setOpenedDialogId(long j) {
        notificationsQueue.postRunnable(new Runnable(j) {
            public final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NotificationsController.this.lambda$setOpenedDialogId$2$NotificationsController(this.f$1);
            }
        });
    }

    public void setOpenedInBubble(long j, boolean z) {
        notificationsQueue.postRunnable(new Runnable(z, j) {
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationsController.this.lambda$setOpenedInBubble$3$NotificationsController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setOpenedInBubble$3$NotificationsController(boolean z, long j) {
        if (z) {
            this.openedInBubbleDialogs.add(Long.valueOf(j));
        } else {
            this.openedInBubbleDialogs.remove(Long.valueOf(j));
        }
    }

    public void setLastOnlineFromOtherDevice(int i) {
        notificationsQueue.postRunnable(new Runnable(i) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NotificationsController.this.lambda$setLastOnlineFromOtherDevice$4$NotificationsController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setLastOnlineFromOtherDevice$4$NotificationsController(int i) {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("set last online from other device = " + i);
        }
        this.lastOnlineFromOtherDevice = i;
    }

    public void removeNotificationsForDialog(long j) {
        processReadMessages((SparseLongArray) null, j, 0, Integer.MAX_VALUE, false);
        LongSparseArray longSparseArray = new LongSparseArray();
        longSparseArray.put(j, 0);
        processDialogsUpdateRead(longSparseArray);
    }

    public boolean hasMessagesToReply() {
        for (int i = 0; i < this.pushMessages.size(); i++) {
            MessageObject messageObject = this.pushMessages.get(i);
            long dialogId = messageObject.getDialogId();
            TLRPC$Message tLRPC$Message = messageObject.messageOwner;
            if ((!tLRPC$Message.mentioned || !(tLRPC$Message.action instanceof TLRPC$TL_messageActionPinMessage)) && ((int) dialogId) != 0 && (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void forceShowPopupForReply() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$forceShowPopupForReply$6$NotificationsController();
            }
        });
    }

    public /* synthetic */ void lambda$forceShowPopupForReply$6$NotificationsController() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.pushMessages.size(); i++) {
            MessageObject messageObject = this.pushMessages.get(i);
            long dialogId = messageObject.getDialogId();
            TLRPC$Message tLRPC$Message = messageObject.messageOwner;
            if ((!tLRPC$Message.mentioned || !(tLRPC$Message.action instanceof TLRPC$TL_messageActionPinMessage)) && ((int) dialogId) != 0 && (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup())) {
                arrayList.add(0, messageObject);
            }
        }
        if (!arrayList.isEmpty() && !AndroidUtilities.needShowPasscode() && !SharedConfig.isWaitingForPasscodeEnter) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList) {
                public final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$5$NotificationsController(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$null$5$NotificationsController(ArrayList arrayList) {
        this.popupReplyMessages = arrayList;
        Intent intent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
        intent.putExtra("force", true);
        intent.putExtra("currentAccount", this.currentAccount);
        intent.setFlags(NUM);
        ApplicationLoader.applicationContext.startActivity(intent);
        ApplicationLoader.applicationContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    public void removeDeletedMessagesFromNotifications(SparseArray<ArrayList<Integer>> sparseArray) {
        notificationsQueue.postRunnable(new Runnable(sparseArray, new ArrayList(0)) {
            public final /* synthetic */ SparseArray f$1;
            public final /* synthetic */ ArrayList f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationsController.this.lambda$removeDeletedMessagesFromNotifications$9$NotificationsController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$removeDeletedMessagesFromNotifications$9$NotificationsController(SparseArray sparseArray, ArrayList arrayList) {
        Integer num;
        SparseArray sparseArray2 = sparseArray;
        ArrayList arrayList2 = arrayList;
        int i = this.total_unread_count;
        getAccountInstance().getNotificationsSettings();
        int i2 = 0;
        while (i2 < sparseArray.size()) {
            int keyAt = sparseArray2.keyAt(i2);
            ArrayList arrayList3 = (ArrayList) sparseArray2.get(keyAt);
            int i3 = 0;
            while (i3 < arrayList3.size()) {
                long intValue = (long) ((Integer) arrayList3.get(i3)).intValue();
                if (keyAt != 0) {
                    intValue |= ((long) keyAt) << 32;
                }
                MessageObject messageObject = this.pushMessagesDict.get(intValue);
                if (messageObject != null) {
                    long dialogId = messageObject.getDialogId();
                    Integer num2 = this.pushDialogs.get(dialogId);
                    if (num2 == null) {
                        num2 = 0;
                    }
                    Integer valueOf = Integer.valueOf(num2.intValue() - 1);
                    if (valueOf.intValue() <= 0) {
                        this.smartNotificationsDialogs.remove(dialogId);
                        num = 0;
                    } else {
                        num = valueOf;
                    }
                    if (!num.equals(num2)) {
                        int intValue2 = this.total_unread_count - num2.intValue();
                        this.total_unread_count = intValue2;
                        this.total_unread_count = intValue2 + num.intValue();
                        this.pushDialogs.put(dialogId, num);
                    }
                    if (num.intValue() == 0) {
                        this.pushDialogs.remove(dialogId);
                        this.pushDialogsOverrideMention.remove(dialogId);
                    }
                    this.pushMessagesDict.remove(intValue);
                    this.delayedPushMessages.remove(messageObject);
                    this.pushMessages.remove(messageObject);
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count--;
                    }
                    arrayList2.add(messageObject);
                }
                i3++;
                SparseArray sparseArray3 = sparseArray;
            }
            i2++;
            sparseArray2 = sparseArray;
        }
        if (!arrayList.isEmpty()) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList2) {
                public final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$7$NotificationsController(this.f$1);
                }
            });
        }
        if (i != this.total_unread_count) {
            if (!this.notifyCheck) {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            } else {
                scheduleNotificationDelay(this.lastOnlineFromOtherDevice > getConnectionsManager().getCurrentTime());
            }
            AndroidUtilities.runOnUIThread(new Runnable(this.pushDialogs.size()) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$8$NotificationsController(this.f$1);
                }
            });
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    public /* synthetic */ void lambda$null$7$NotificationsController(ArrayList arrayList) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.popupMessages.remove(arrayList.get(i));
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
    }

    public /* synthetic */ void lambda$null$8$NotificationsController(int i) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        getNotificationCenter().postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(i));
    }

    public void removeDeletedHisoryFromNotifications(SparseIntArray sparseIntArray) {
        notificationsQueue.postRunnable(new Runnable(sparseIntArray, new ArrayList(0)) {
            public final /* synthetic */ SparseIntArray f$1;
            public final /* synthetic */ ArrayList f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationsController.this.lambda$removeDeletedHisoryFromNotifications$12$NotificationsController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$removeDeletedHisoryFromNotifications$12$NotificationsController(SparseIntArray sparseIntArray, ArrayList arrayList) {
        boolean z;
        Integer num;
        SparseIntArray sparseIntArray2 = sparseIntArray;
        ArrayList arrayList2 = arrayList;
        int i = this.total_unread_count;
        getAccountInstance().getNotificationsSettings();
        int i2 = 0;
        int i3 = 0;
        while (true) {
            z = true;
            if (i3 >= sparseIntArray.size()) {
                break;
            }
            int keyAt = sparseIntArray2.keyAt(i3);
            long j = (long) (-keyAt);
            int i4 = sparseIntArray2.get(keyAt);
            Integer num2 = this.pushDialogs.get(j);
            if (num2 == null) {
                num2 = i2;
            }
            Integer num3 = num2;
            int i5 = 0;
            while (i5 < this.pushMessages.size()) {
                MessageObject messageObject = this.pushMessages.get(i5);
                if (messageObject.getDialogId() != j || messageObject.getId() > i4) {
                    num = i2;
                } else {
                    num = i2;
                    this.pushMessagesDict.remove(messageObject.getIdWithChannel());
                    this.delayedPushMessages.remove(messageObject);
                    this.pushMessages.remove(messageObject);
                    i5--;
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count--;
                    }
                    arrayList2.add(messageObject);
                    num3 = Integer.valueOf(num3.intValue() - 1);
                }
                i5++;
                i2 = num;
            }
            Integer num4 = i2;
            if (num3.intValue() <= 0) {
                this.smartNotificationsDialogs.remove(j);
                num3 = num4;
            }
            if (!num3.equals(num2)) {
                int intValue = this.total_unread_count - num2.intValue();
                this.total_unread_count = intValue;
                this.total_unread_count = intValue + num3.intValue();
                this.pushDialogs.put(j, num3);
            }
            if (num3.intValue() == 0) {
                this.pushDialogs.remove(j);
                this.pushDialogsOverrideMention.remove(j);
            }
            i3++;
            i2 = num4;
        }
        if (arrayList.isEmpty()) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList2) {
                public final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$10$NotificationsController(this.f$1);
                }
            });
        }
        if (i != this.total_unread_count) {
            if (!this.notifyCheck) {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            } else {
                if (this.lastOnlineFromOtherDevice <= getConnectionsManager().getCurrentTime()) {
                    z = false;
                }
                scheduleNotificationDelay(z);
            }
            AndroidUtilities.runOnUIThread(new Runnable(this.pushDialogs.size()) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$11$NotificationsController(this.f$1);
                }
            });
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    public /* synthetic */ void lambda$null$10$NotificationsController(ArrayList arrayList) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.popupMessages.remove(arrayList.get(i));
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
    }

    public /* synthetic */ void lambda$null$11$NotificationsController(int i) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        getNotificationCenter().postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(i));
    }

    public void processReadMessages(SparseLongArray sparseLongArray, long j, int i, int i2, boolean z) {
        notificationsQueue.postRunnable(new Runnable(sparseLongArray, new ArrayList(0), j, i2, i, z) {
            public final /* synthetic */ SparseLongArray f$1;
            public final /* synthetic */ ArrayList f$2;
            public final /* synthetic */ long f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ int f$5;
            public final /* synthetic */ boolean f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r6;
                this.f$5 = r7;
                this.f$6 = r8;
            }

            public final void run() {
                NotificationsController.this.lambda$processReadMessages$14$NotificationsController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00f6 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$processReadMessages$14$NotificationsController(org.telegram.messenger.support.SparseLongArray r19, java.util.ArrayList r20, long r21, int r23, int r24, boolean r25) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            r2 = r20
            r3 = r23
            r4 = r24
            r5 = 32
            r6 = 1
            if (r1 == 0) goto L_0x0082
            r8 = 0
        L_0x0010:
            int r9 = r19.size()
            if (r8 >= r9) goto L_0x0082
            int r9 = r1.keyAt(r8)
            long r10 = r1.get(r9)
            r12 = 0
        L_0x001f:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r13 = r0.pushMessages
            int r13 = r13.size()
            if (r12 >= r13) goto L_0x007d
            java.util.ArrayList<org.telegram.messenger.MessageObject> r13 = r0.pushMessages
            java.lang.Object r13 = r13.get(r12)
            org.telegram.messenger.MessageObject r13 = (org.telegram.messenger.MessageObject) r13
            org.telegram.tgnet.TLRPC$Message r14 = r13.messageOwner
            boolean r14 = r14.from_scheduled
            if (r14 != 0) goto L_0x0077
            long r14 = r13.getDialogId()
            r16 = r8
            long r7 = (long) r9
            int r17 = (r14 > r7 ? 1 : (r14 == r7 ? 0 : -1))
            if (r17 != 0) goto L_0x0079
            int r7 = r13.getId()
            int r8 = (int) r10
            if (r7 > r8) goto L_0x0079
            boolean r7 = r0.isPersonalMessage(r13)
            if (r7 == 0) goto L_0x0052
            int r7 = r0.personal_count
            int r7 = r7 - r6
            r0.personal_count = r7
        L_0x0052:
            r2.add(r13)
            int r7 = r13.getId()
            long r7 = (long) r7
            org.telegram.tgnet.TLRPC$Message r14 = r13.messageOwner
            org.telegram.tgnet.TLRPC$Peer r14 = r14.to_id
            int r14 = r14.channel_id
            if (r14 == 0) goto L_0x0065
            long r14 = (long) r14
            long r14 = r14 << r5
            long r7 = r7 | r14
        L_0x0065:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r14 = r0.pushMessagesDict
            r14.remove(r7)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r0.delayedPushMessages
            r7.remove(r13)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r0.pushMessages
            r7.remove(r12)
            int r12 = r12 + -1
            goto L_0x0079
        L_0x0077:
            r16 = r8
        L_0x0079:
            int r12 = r12 + r6
            r8 = r16
            goto L_0x001f
        L_0x007d:
            r16 = r8
            int r8 = r16 + 1
            goto L_0x0010
        L_0x0082:
            r7 = 0
            int r1 = (r21 > r7 ? 1 : (r21 == r7 ? 0 : -1))
            if (r1 == 0) goto L_0x00f8
            if (r3 != 0) goto L_0x008c
            if (r4 == 0) goto L_0x00f8
        L_0x008c:
            r1 = 0
        L_0x008d:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r0.pushMessages
            int r7 = r7.size()
            if (r1 >= r7) goto L_0x00f8
            java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r0.pushMessages
            java.lang.Object r7 = r7.get(r1)
            org.telegram.messenger.MessageObject r7 = (org.telegram.messenger.MessageObject) r7
            long r8 = r7.getDialogId()
            int r10 = (r8 > r21 ? 1 : (r8 == r21 ? 0 : -1))
            if (r10 != 0) goto L_0x00f6
            if (r4 == 0) goto L_0x00ae
            org.telegram.tgnet.TLRPC$Message r8 = r7.messageOwner
            int r8 = r8.date
            if (r8 > r4) goto L_0x00c2
            goto L_0x00c4
        L_0x00ae:
            if (r25 != 0) goto L_0x00b9
            int r8 = r7.getId()
            if (r8 <= r3) goto L_0x00c4
            if (r3 >= 0) goto L_0x00c2
            goto L_0x00c4
        L_0x00b9:
            int r8 = r7.getId()
            if (r8 == r3) goto L_0x00c4
            if (r3 >= 0) goto L_0x00c2
            goto L_0x00c4
        L_0x00c2:
            r8 = 0
            goto L_0x00c5
        L_0x00c4:
            r8 = 1
        L_0x00c5:
            if (r8 == 0) goto L_0x00f6
            boolean r8 = r0.isPersonalMessage(r7)
            if (r8 == 0) goto L_0x00d2
            int r8 = r0.personal_count
            int r8 = r8 - r6
            r0.personal_count = r8
        L_0x00d2:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r8 = r0.pushMessages
            r8.remove(r1)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r8 = r0.delayedPushMessages
            r8.remove(r7)
            r2.add(r7)
            int r8 = r7.getId()
            long r8 = (long) r8
            org.telegram.tgnet.TLRPC$Message r7 = r7.messageOwner
            org.telegram.tgnet.TLRPC$Peer r7 = r7.to_id
            int r7 = r7.channel_id
            if (r7 == 0) goto L_0x00ef
            long r10 = (long) r7
            long r10 = r10 << r5
            long r8 = r8 | r10
        L_0x00ef:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r7 = r0.pushMessagesDict
            r7.remove(r8)
            int r1 = r1 + -1
        L_0x00f6:
            int r1 = r1 + r6
            goto L_0x008d
        L_0x00f8:
            boolean r1 = r20.isEmpty()
            if (r1 != 0) goto L_0x0106
            org.telegram.messenger.-$$Lambda$NotificationsController$hYfnxb5aCShrnoDeAgemyzWDJyc r1 = new org.telegram.messenger.-$$Lambda$NotificationsController$hYfnxb5aCShrnoDeAgemyzWDJyc
            r1.<init>(r2)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)
        L_0x0106:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.lambda$processReadMessages$14$NotificationsController(org.telegram.messenger.support.SparseLongArray, java.util.ArrayList, long, int, int, boolean):void");
    }

    public /* synthetic */ void lambda$null$13$NotificationsController(ArrayList arrayList) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.popupMessages.remove(arrayList.get(i));
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004f, code lost:
        if (r5 == 2) goto L_0x0051;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int addToPopupMessages(java.util.ArrayList<org.telegram.messenger.MessageObject> r3, org.telegram.messenger.MessageObject r4, int r5, long r6, boolean r8, android.content.SharedPreferences r9) {
        /*
            r2 = this;
            r0 = 0
            if (r5 == 0) goto L_0x0051
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r1 = "custom_"
            r5.append(r1)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            boolean r5 = r9.getBoolean(r5, r0)
            if (r5 == 0) goto L_0x0030
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r1 = "popup_"
            r5.append(r1)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            int r5 = r9.getInt(r5, r0)
            goto L_0x0031
        L_0x0030:
            r5 = 0
        L_0x0031:
            if (r5 != 0) goto L_0x0049
            if (r8 == 0) goto L_0x003c
            java.lang.String r5 = "popupChannel"
            int r5 = r9.getInt(r5, r0)
            goto L_0x0052
        L_0x003c:
            int r5 = (int) r6
            if (r5 >= 0) goto L_0x0042
            java.lang.String r5 = "popupGroup"
            goto L_0x0044
        L_0x0042:
            java.lang.String r5 = "popupAll"
        L_0x0044:
            int r5 = r9.getInt(r5, r0)
            goto L_0x0052
        L_0x0049:
            r6 = 1
            if (r5 != r6) goto L_0x004e
            r5 = 3
            goto L_0x0052
        L_0x004e:
            r6 = 2
            if (r5 != r6) goto L_0x0052
        L_0x0051:
            r5 = 0
        L_0x0052:
            if (r5 == 0) goto L_0x0063
            org.telegram.tgnet.TLRPC$Message r6 = r4.messageOwner
            org.telegram.tgnet.TLRPC$Peer r6 = r6.to_id
            int r6 = r6.channel_id
            if (r6 == 0) goto L_0x0063
            boolean r6 = r4.isMegagroup()
            if (r6 != 0) goto L_0x0063
            r5 = 0
        L_0x0063:
            if (r5 == 0) goto L_0x0068
            r3.add(r0, r4)
        L_0x0068:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.addToPopupMessages(java.util.ArrayList, org.telegram.messenger.MessageObject, int, long, boolean, android.content.SharedPreferences):int");
    }

    public void processNewMessages(ArrayList<MessageObject> arrayList, boolean z, boolean z2, CountDownLatch countDownLatch) {
        if (!arrayList.isEmpty()) {
            notificationsQueue.postRunnable(new Runnable(arrayList, new ArrayList(0), z2, z, countDownLatch) {
                public final /* synthetic */ ArrayList f$1;
                public final /* synthetic */ ArrayList f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ boolean f$4;
                public final /* synthetic */ CountDownLatch f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    NotificationsController.this.lambda$processNewMessages$17$NotificationsController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        } else if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0105  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$processNewMessages$17$NotificationsController(java.util.ArrayList r32, java.util.ArrayList r33, boolean r34, boolean r35, java.util.concurrent.CountDownLatch r36) {
        /*
            r31 = this;
            r8 = r31
            r9 = r32
            android.util.LongSparseArray r10 = new android.util.LongSparseArray
            r10.<init>()
            org.telegram.messenger.AccountInstance r0 = r31.getAccountInstance()
            android.content.SharedPreferences r11 = r0.getNotificationsSettings()
            java.lang.String r0 = "PinnedMessages"
            r12 = 1
            boolean r13 = r11.getBoolean(r0, r12)
            r0 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r18 = 0
        L_0x0020:
            int r1 = r32.size()
            if (r15 >= r1) goto L_0x01fc
            java.lang.Object r1 = r9.get(r15)
            r7 = r1
            org.telegram.messenger.MessageObject r7 = (org.telegram.messenger.MessageObject) r7
            org.telegram.tgnet.TLRPC$Message r1 = r7.messageOwner
            if (r1 == 0) goto L_0x0045
            boolean r4 = r1.silent
            if (r4 == 0) goto L_0x0045
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp
            if (r4 != 0) goto L_0x003f
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined
            if (r1 == 0) goto L_0x0045
        L_0x003f:
            r22 = r13
            r21 = r15
            goto L_0x0131
        L_0x0045:
            int r1 = r7.getId()
            long r4 = (long) r1
            boolean r1 = r7.isFcmMessage()
            r19 = 0
            if (r1 == 0) goto L_0x005b
            org.telegram.tgnet.TLRPC$Message r1 = r7.messageOwner
            r21 = r15
            long r14 = r1.random_id
            r22 = r13
            goto L_0x0061
        L_0x005b:
            r21 = r15
            r22 = r13
            r14 = r19
        L_0x0061:
            long r12 = r7.getDialogId()
            int r6 = (int) r12
            boolean r1 = r7.isFcmMessage()
            if (r1 == 0) goto L_0x006f
            boolean r1 = r7.localChannel
            goto L_0x008b
        L_0x006f:
            if (r6 >= 0) goto L_0x008e
            org.telegram.messenger.MessagesController r1 = r31.getMessagesController()
            int r2 = -r6
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r1 = r1.getChat(r2)
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r1)
            if (r2 == 0) goto L_0x008a
            boolean r1 = r1.megagroup
            if (r1 != 0) goto L_0x008a
            r1 = 1
            goto L_0x008b
        L_0x008a:
            r1 = 0
        L_0x008b:
            r24 = r1
            goto L_0x0090
        L_0x008e:
            r24 = 0
        L_0x0090:
            org.telegram.tgnet.TLRPC$Message r1 = r7.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x009e
            long r1 = (long) r1
            r25 = 32
            long r1 = r1 << r25
            long r4 = r4 | r1
        L_0x009e:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r1 = r8.pushMessagesDict
            java.lang.Object r1 = r1.get(r4)
            org.telegram.messenger.MessageObject r1 = (org.telegram.messenger.MessageObject) r1
            if (r1 != 0) goto L_0x00c6
            org.telegram.tgnet.TLRPC$Message r2 = r7.messageOwner
            r26 = r4
            long r3 = r2.random_id
            int r2 = (r3 > r19 ? 1 : (r3 == r19 ? 0 : -1))
            if (r2 == 0) goto L_0x00c8
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r1 = r8.fcmRandomMessagesDict
            java.lang.Object r1 = r1.get(r3)
            org.telegram.messenger.MessageObject r1 = (org.telegram.messenger.MessageObject) r1
            if (r1 == 0) goto L_0x00c8
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r2 = r8.fcmRandomMessagesDict
            org.telegram.tgnet.TLRPC$Message r3 = r7.messageOwner
            long r3 = r3.random_id
            r2.remove(r3)
            goto L_0x00c8
        L_0x00c6:
            r26 = r4
        L_0x00c8:
            if (r1 == 0) goto L_0x0105
            boolean r2 = r1.isFcmMessage()
            if (r2 == 0) goto L_0x0131
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r2 = r8.pushMessagesDict
            r4 = r26
            r2.put(r4, r7)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r8.pushMessages
            int r1 = r2.indexOf(r1)
            if (r1 < 0) goto L_0x00f4
            java.util.ArrayList<org.telegram.messenger.MessageObject> r0 = r8.pushMessages
            r0.set(r1, r7)
            r0 = r31
            r1 = r33
            r2 = r7
            r3 = r6
            r4 = r12
            r6 = r24
            r12 = r7
            r7 = r11
            int r0 = r0.addToPopupMessages(r1, r2, r3, r4, r6, r7)
            goto L_0x00f5
        L_0x00f4:
            r12 = r7
        L_0x00f5:
            if (r34 == 0) goto L_0x0131
            boolean r1 = r12.localEdit
            if (r1 == 0) goto L_0x0102
            org.telegram.messenger.MessagesStorage r2 = r31.getMessagesStorage()
            r2.putPushMessage(r12)
        L_0x0102:
            r17 = r1
            goto L_0x0131
        L_0x0105:
            r4 = r26
            if (r17 == 0) goto L_0x010a
            goto L_0x0131
        L_0x010a:
            if (r34 == 0) goto L_0x0113
            org.telegram.messenger.MessagesStorage r1 = r31.getMessagesStorage()
            r1.putPushMessage(r7)
        L_0x0113:
            long r1 = r8.opened_dialog_id
            int r3 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r3 != 0) goto L_0x0123
            boolean r1 = org.telegram.messenger.ApplicationLoader.isScreenOn
            if (r1 == 0) goto L_0x0123
            if (r34 != 0) goto L_0x0131
            r31.playInChatSound()
            goto L_0x0131
        L_0x0123:
            org.telegram.tgnet.TLRPC$Message r1 = r7.messageOwner
            boolean r2 = r1.mentioned
            if (r2 == 0) goto L_0x013c
            if (r22 != 0) goto L_0x0135
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPinMessage
            if (r1 == 0) goto L_0x0135
        L_0x0131:
            r30 = r10
            goto L_0x01f1
        L_0x0135:
            org.telegram.tgnet.TLRPC$Message r1 = r7.messageOwner
            int r1 = r1.from_id
            long r1 = (long) r1
            r2 = r1
            goto L_0x013d
        L_0x013c:
            r2 = r12
        L_0x013d:
            boolean r1 = r8.isPersonalMessage(r7)
            if (r1 == 0) goto L_0x014b
            int r1 = r8.personal_count
            r16 = 1
            int r1 = r1 + 1
            r8.personal_count = r1
        L_0x014b:
            int r1 = r10.indexOfKey(r2)
            if (r1 < 0) goto L_0x015e
            java.lang.Object r1 = r10.valueAt(r1)
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            r26 = r4
            goto L_0x017d
        L_0x015e:
            int r1 = r8.getNotifyOverride(r11, r2)
            r26 = r4
            r4 = -1
            if (r1 != r4) goto L_0x0170
            java.lang.Boolean r1 = java.lang.Boolean.valueOf(r24)
            boolean r1 = r8.isGlobalNotificationsEnabled(r2, r1)
            goto L_0x0176
        L_0x0170:
            r4 = 2
            if (r1 == r4) goto L_0x0175
            r1 = 1
            goto L_0x0176
        L_0x0175:
            r1 = 0
        L_0x0176:
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r1)
            r10.put(r2, r4)
        L_0x017d:
            if (r1 == 0) goto L_0x01ed
            if (r34 != 0) goto L_0x019a
            r0 = r31
            r1 = r33
            r28 = r2
            r2 = r7
            r3 = r6
            r30 = r10
            r9 = r26
            r4 = r28
            r6 = r24
            r23 = r12
            r12 = r7
            r7 = r11
            int r0 = r0.addToPopupMessages(r1, r2, r3, r4, r6, r7)
            goto L_0x01a3
        L_0x019a:
            r28 = r2
            r30 = r10
            r23 = r12
            r9 = r26
            r12 = r7
        L_0x01a3:
            if (r18 != 0) goto L_0x01ab
            org.telegram.tgnet.TLRPC$Message r1 = r12.messageOwner
            boolean r1 = r1.from_scheduled
            r18 = r1
        L_0x01ab:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r8.delayedPushMessages
            r1.add(r12)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r8.pushMessages
            r2 = 0
            r1.add(r2, r12)
            int r1 = (r9 > r19 ? 1 : (r9 == r19 ? 0 : -1))
            if (r1 == 0) goto L_0x01c0
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r1 = r8.pushMessagesDict
            r1.put(r9, r12)
            goto L_0x01c9
        L_0x01c0:
            int r1 = (r14 > r19 ? 1 : (r14 == r19 ? 0 : -1))
            if (r1 == 0) goto L_0x01c9
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r1 = r8.fcmRandomMessagesDict
            r1.put(r14, r12)
        L_0x01c9:
            int r1 = (r23 > r28 ? 1 : (r23 == r28 ? 0 : -1))
            if (r1 == 0) goto L_0x01ef
            android.util.LongSparseArray<java.lang.Integer> r1 = r8.pushDialogsOverrideMention
            r2 = r23
            java.lang.Object r1 = r1.get(r2)
            java.lang.Integer r1 = (java.lang.Integer) r1
            android.util.LongSparseArray<java.lang.Integer> r4 = r8.pushDialogsOverrideMention
            if (r1 != 0) goto L_0x01de
            r16 = 1
            goto L_0x01e5
        L_0x01de:
            int r1 = r1.intValue()
            r5 = 1
            int r16 = r1 + 1
        L_0x01e5:
            java.lang.Integer r1 = java.lang.Integer.valueOf(r16)
            r4.put(r2, r1)
            goto L_0x01ef
        L_0x01ed:
            r30 = r10
        L_0x01ef:
            r16 = 1
        L_0x01f1:
            int r15 = r21 + 1
            r9 = r32
            r13 = r22
            r10 = r30
            r12 = 1
            goto L_0x0020
        L_0x01fc:
            if (r16 == 0) goto L_0x0202
            r1 = r35
            r8.notifyCheck = r1
        L_0x0202:
            boolean r1 = r33.isEmpty()
            if (r1 != 0) goto L_0x021c
            boolean r1 = org.telegram.messenger.AndroidUtilities.needShowPasscode()
            if (r1 != 0) goto L_0x021c
            boolean r1 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r1 != 0) goto L_0x021c
            org.telegram.messenger.-$$Lambda$NotificationsController$QDzfGX8st0KAXCbxJrmXBka4BoE r1 = new org.telegram.messenger.-$$Lambda$NotificationsController$QDzfGX8st0KAXCbxJrmXBka4BoE
            r2 = r33
            r1.<init>(r2, r0)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)
        L_0x021c:
            if (r34 != 0) goto L_0x0220
            if (r18 == 0) goto L_0x02db
        L_0x0220:
            if (r17 == 0) goto L_0x022e
            java.util.ArrayList<org.telegram.messenger.MessageObject> r0 = r8.delayedPushMessages
            r0.clear()
            boolean r0 = r8.notifyCheck
            r8.showOrUpdateNotification(r0)
            goto L_0x02db
        L_0x022e:
            if (r16 == 0) goto L_0x02db
            r0 = r32
            r1 = 0
            java.lang.Object r0 = r0.get(r1)
            org.telegram.messenger.MessageObject r0 = (org.telegram.messenger.MessageObject) r0
            long r1 = r0.getDialogId()
            boolean r3 = r0.isFcmMessage()
            if (r3 == 0) goto L_0x024a
            boolean r0 = r0.localChannel
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            goto L_0x024b
        L_0x024a:
            r0 = 0
        L_0x024b:
            int r3 = r8.total_unread_count
            int r4 = r8.getNotifyOverride(r11, r1)
            r5 = -1
            if (r4 != r5) goto L_0x0259
            boolean r0 = r8.isGlobalNotificationsEnabled(r1, r0)
            goto L_0x0263
        L_0x0259:
            r0 = 2
            if (r4 == r0) goto L_0x025f
            r16 = 1
            goto L_0x0261
        L_0x025f:
            r16 = 0
        L_0x0261:
            r0 = r16
        L_0x0263:
            android.util.LongSparseArray<java.lang.Integer> r4 = r8.pushDialogs
            java.lang.Object r4 = r4.get(r1)
            java.lang.Integer r4 = (java.lang.Integer) r4
            if (r4 == 0) goto L_0x0276
            int r5 = r4.intValue()
            r16 = 1
            int r5 = r5 + 1
            goto L_0x0279
        L_0x0276:
            r16 = 1
            r5 = 1
        L_0x0279:
            boolean r6 = r8.notifyCheck
            if (r6 == 0) goto L_0x0295
            if (r0 != 0) goto L_0x0295
            android.util.LongSparseArray<java.lang.Integer> r6 = r8.pushDialogsOverrideMention
            java.lang.Object r6 = r6.get(r1)
            java.lang.Integer r6 = (java.lang.Integer) r6
            if (r6 == 0) goto L_0x0295
            int r7 = r6.intValue()
            if (r7 == 0) goto L_0x0295
            int r5 = r6.intValue()
            r12 = 1
            goto L_0x0296
        L_0x0295:
            r12 = r0
        L_0x0296:
            if (r12 == 0) goto L_0x02b1
            if (r4 == 0) goto L_0x02a3
            int r0 = r8.total_unread_count
            int r4 = r4.intValue()
            int r0 = r0 - r4
            r8.total_unread_count = r0
        L_0x02a3:
            int r0 = r8.total_unread_count
            int r0 = r0 + r5
            r8.total_unread_count = r0
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.pushDialogs
            java.lang.Integer r4 = java.lang.Integer.valueOf(r5)
            r0.put(r1, r4)
        L_0x02b1:
            int r0 = r8.total_unread_count
            if (r3 == r0) goto L_0x02cd
            java.util.ArrayList<org.telegram.messenger.MessageObject> r0 = r8.delayedPushMessages
            r0.clear()
            boolean r0 = r8.notifyCheck
            r8.showOrUpdateNotification(r0)
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.pushDialogs
            int r0 = r0.size()
            org.telegram.messenger.-$$Lambda$NotificationsController$8dgn4YYZ8Yk1zWGWaoyaxcMFn7c r1 = new org.telegram.messenger.-$$Lambda$NotificationsController$8dgn4YYZ8Yk1zWGWaoyaxcMFn7c
            r1.<init>(r0)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)
        L_0x02cd:
            r0 = 0
            r8.notifyCheck = r0
            boolean r0 = r8.showBadgeNumber
            if (r0 == 0) goto L_0x02db
            int r0 = r31.getTotalAllUnreadCount()
            r8.setBadge(r0)
        L_0x02db:
            if (r36 == 0) goto L_0x02e0
            r36.countDown()
        L_0x02e0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.lambda$processNewMessages$17$NotificationsController(java.util.ArrayList, java.util.ArrayList, boolean, boolean, java.util.concurrent.CountDownLatch):void");
    }

    public /* synthetic */ void lambda$null$15$NotificationsController(ArrayList arrayList, int i) {
        this.popupMessages.addAll(0, arrayList);
        if (!ApplicationLoader.mainInterfacePaused && ApplicationLoader.isScreenOn) {
            return;
        }
        if (i == 3 || ((i == 1 && ApplicationLoader.isScreenOn) || (i == 2 && !ApplicationLoader.isScreenOn))) {
            Intent intent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
            intent.setFlags(NUM);
            try {
                ApplicationLoader.applicationContext.startActivity(intent);
            } catch (Throwable unused) {
            }
        }
    }

    public /* synthetic */ void lambda$null$16$NotificationsController(int i) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        getNotificationCenter().postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(i));
    }

    public int getTotalUnreadCount() {
        return this.total_unread_count;
    }

    public void processDialogsUpdateRead(LongSparseArray<Integer> longSparseArray) {
        notificationsQueue.postRunnable(new Runnable(longSparseArray, new ArrayList()) {
            public final /* synthetic */ LongSparseArray f$1;
            public final /* synthetic */ ArrayList f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationsController.this.lambda$processDialogsUpdateRead$20$NotificationsController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$processDialogsUpdateRead$20$NotificationsController(LongSparseArray longSparseArray, ArrayList arrayList) {
        boolean z;
        Integer num;
        LongSparseArray longSparseArray2 = longSparseArray;
        ArrayList arrayList2 = arrayList;
        int i = this.total_unread_count;
        SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
        int i2 = 0;
        while (true) {
            z = true;
            if (i2 >= longSparseArray.size()) {
                break;
            }
            long keyAt = longSparseArray2.keyAt(i2);
            int notifyOverride = getNotifyOverride(notificationsSettings, keyAt);
            boolean isGlobalNotificationsEnabled = notifyOverride == -1 ? isGlobalNotificationsEnabled(keyAt) : notifyOverride != 2;
            Integer num2 = this.pushDialogs.get(keyAt);
            Integer num3 = (Integer) longSparseArray2.get(keyAt);
            if (this.notifyCheck && !isGlobalNotificationsEnabled && (num = this.pushDialogsOverrideMention.get(keyAt)) != null && num.intValue() != 0) {
                num3 = num;
                isGlobalNotificationsEnabled = true;
            }
            if (num3.intValue() == 0) {
                this.smartNotificationsDialogs.remove(keyAt);
            }
            if (num3.intValue() < 0) {
                if (num2 == null) {
                    i2++;
                } else {
                    num3 = Integer.valueOf(num2.intValue() + num3.intValue());
                }
            }
            if ((isGlobalNotificationsEnabled || num3.intValue() == 0) && num2 != null) {
                this.total_unread_count -= num2.intValue();
            }
            if (num3.intValue() == 0) {
                this.pushDialogs.remove(keyAt);
                this.pushDialogsOverrideMention.remove(keyAt);
                int i3 = 0;
                while (i3 < this.pushMessages.size()) {
                    MessageObject messageObject = this.pushMessages.get(i3);
                    if (!messageObject.messageOwner.from_scheduled && messageObject.getDialogId() == keyAt) {
                        if (isPersonalMessage(messageObject)) {
                            this.personal_count--;
                        }
                        this.pushMessages.remove(i3);
                        i3--;
                        this.delayedPushMessages.remove(messageObject);
                        long id = (long) messageObject.getId();
                        int i4 = messageObject.messageOwner.to_id.channel_id;
                        if (i4 != 0) {
                            id |= ((long) i4) << 32;
                        }
                        this.pushMessagesDict.remove(id);
                        arrayList2.add(messageObject);
                    }
                    i3++;
                }
            } else if (isGlobalNotificationsEnabled) {
                this.total_unread_count += num3.intValue();
                this.pushDialogs.put(keyAt, num3);
            }
            i2++;
        }
        if (!arrayList.isEmpty()) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList2) {
                public final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$18$NotificationsController(this.f$1);
                }
            });
        }
        if (i != this.total_unread_count) {
            if (!this.notifyCheck) {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            } else {
                if (this.lastOnlineFromOtherDevice <= getConnectionsManager().getCurrentTime()) {
                    z = false;
                }
                scheduleNotificationDelay(z);
            }
            AndroidUtilities.runOnUIThread(new Runnable(this.pushDialogs.size()) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationsController.this.lambda$null$19$NotificationsController(this.f$1);
                }
            });
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    public /* synthetic */ void lambda$null$18$NotificationsController(ArrayList arrayList) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.popupMessages.remove(arrayList.get(i));
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
    }

    public /* synthetic */ void lambda$null$19$NotificationsController(int i) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        getNotificationCenter().postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(i));
    }

    public void processLoadedUnreadMessages(LongSparseArray<Integer> longSparseArray, ArrayList<TLRPC$Message> arrayList, ArrayList<MessageObject> arrayList2, ArrayList<TLRPC$User> arrayList3, ArrayList<TLRPC$Chat> arrayList4, ArrayList<TLRPC$EncryptedChat> arrayList5) {
        getMessagesController().putUsers(arrayList3, true);
        getMessagesController().putChats(arrayList4, true);
        getMessagesController().putEncryptedChats(arrayList5, true);
        notificationsQueue.postRunnable(new Runnable(arrayList, longSparseArray, arrayList2) {
            public final /* synthetic */ ArrayList f$1;
            public final /* synthetic */ LongSparseArray f$2;
            public final /* synthetic */ ArrayList f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                NotificationsController.this.lambda$processLoadedUnreadMessages$22$NotificationsController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0049, code lost:
        if ((r13 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined) == false) goto L_0x004c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$processLoadedUnreadMessages$22$NotificationsController(java.util.ArrayList r21, android.util.LongSparseArray r22, java.util.ArrayList r23) {
        /*
            r20 = this;
            r0 = r20
            r1 = r21
            r2 = r22
            r3 = r23
            android.util.LongSparseArray<java.lang.Integer> r4 = r0.pushDialogs
            r4.clear()
            java.util.ArrayList<org.telegram.messenger.MessageObject> r4 = r0.pushMessages
            r4.clear()
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r4 = r0.pushMessagesDict
            r4.clear()
            r4 = 0
            r0.total_unread_count = r4
            r0.personal_count = r4
            org.telegram.messenger.AccountInstance r5 = r20.getAccountInstance()
            android.content.SharedPreferences r5 = r5.getNotificationsSettings()
            android.util.LongSparseArray r6 = new android.util.LongSparseArray
            r6.<init>()
            r7 = 32
            r10 = 1
            if (r1 == 0) goto L_0x00fd
            r11 = 0
        L_0x002f:
            int r12 = r21.size()
            if (r11 >= r12) goto L_0x00fd
            java.lang.Object r12 = r1.get(r11)
            org.telegram.tgnet.TLRPC$Message r12 = (org.telegram.tgnet.TLRPC$Message) r12
            if (r12 == 0) goto L_0x004c
            boolean r13 = r12.silent
            if (r13 == 0) goto L_0x004c
            org.telegram.tgnet.TLRPC$MessageAction r13 = r12.action
            boolean r14 = r13 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp
            if (r14 != 0) goto L_0x0060
            boolean r13 = r13 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined
            if (r13 == 0) goto L_0x004c
            goto L_0x0060
        L_0x004c:
            int r13 = r12.id
            long r13 = (long) r13
            org.telegram.tgnet.TLRPC$Peer r15 = r12.to_id
            int r15 = r15.channel_id
            if (r15 == 0) goto L_0x0058
            long r8 = (long) r15
            long r8 = r8 << r7
            long r13 = r13 | r8
        L_0x0058:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r8 = r0.pushMessagesDict
            int r8 = r8.indexOfKey(r13)
            if (r8 < 0) goto L_0x0065
        L_0x0060:
            r18 = r5
            r12 = r11
            goto L_0x00f3
        L_0x0065:
            org.telegram.messenger.MessageObject r8 = new org.telegram.messenger.MessageObject
            int r9 = r0.currentAccount
            r8.<init>(r9, r12, r4)
            boolean r9 = r0.isPersonalMessage(r8)
            if (r9 == 0) goto L_0x0077
            int r9 = r0.personal_count
            int r9 = r9 + r10
            r0.personal_count = r9
        L_0x0077:
            r12 = r11
            long r10 = r8.getDialogId()
            org.telegram.tgnet.TLRPC$Message r15 = r8.messageOwner
            boolean r9 = r15.mentioned
            if (r9 == 0) goto L_0x0088
            int r9 = r15.from_id
            r17 = r8
            long r7 = (long) r9
            goto L_0x008b
        L_0x0088:
            r17 = r8
            r7 = r10
        L_0x008b:
            int r9 = r6.indexOfKey(r7)
            if (r9 < 0) goto L_0x009c
            java.lang.Object r9 = r6.valueAt(r9)
            java.lang.Boolean r9 = (java.lang.Boolean) r9
            boolean r9 = r9.booleanValue()
            goto L_0x00b5
        L_0x009c:
            int r9 = r0.getNotifyOverride(r5, r7)
            r15 = -1
            if (r9 != r15) goto L_0x00a8
            boolean r9 = r0.isGlobalNotificationsEnabled((long) r7)
            goto L_0x00ae
        L_0x00a8:
            r15 = 2
            if (r9 == r15) goto L_0x00ad
            r9 = 1
            goto L_0x00ae
        L_0x00ad:
            r9 = 0
        L_0x00ae:
            java.lang.Boolean r15 = java.lang.Boolean.valueOf(r9)
            r6.put(r7, r15)
        L_0x00b5:
            r18 = r5
            if (r9 == 0) goto L_0x00f3
            long r4 = r0.opened_dialog_id
            int r9 = (r7 > r4 ? 1 : (r7 == r4 ? 0 : -1))
            if (r9 != 0) goto L_0x00c4
            boolean r4 = org.telegram.messenger.ApplicationLoader.isScreenOn
            if (r4 == 0) goto L_0x00c4
            goto L_0x00f3
        L_0x00c4:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r4 = r0.pushMessagesDict
            r5 = r17
            r4.put(r13, r5)
            java.util.ArrayList<org.telegram.messenger.MessageObject> r4 = r0.pushMessages
            r9 = 0
            r4.add(r9, r5)
            int r4 = (r10 > r7 ? 1 : (r10 == r7 ? 0 : -1))
            if (r4 == 0) goto L_0x00f3
            android.util.LongSparseArray<java.lang.Integer> r4 = r0.pushDialogsOverrideMention
            java.lang.Object r4 = r4.get(r10)
            java.lang.Integer r4 = (java.lang.Integer) r4
            android.util.LongSparseArray<java.lang.Integer> r5 = r0.pushDialogsOverrideMention
            if (r4 != 0) goto L_0x00e4
            r16 = 1
            goto L_0x00ec
        L_0x00e4:
            int r4 = r4.intValue()
            r7 = 1
            int r4 = r4 + r7
            r16 = r4
        L_0x00ec:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r16)
            r5.put(r10, r4)
        L_0x00f3:
            int r11 = r12 + 1
            r5 = r18
            r4 = 0
            r7 = 32
            r10 = 1
            goto L_0x002f
        L_0x00fd:
            r18 = r5
            r1 = 0
        L_0x0100:
            int r4 = r22.size()
            if (r1 >= r4) goto L_0x0159
            long r4 = r2.keyAt(r1)
            int r7 = r6.indexOfKey(r4)
            if (r7 < 0) goto L_0x011e
            java.lang.Object r7 = r6.valueAt(r7)
            java.lang.Boolean r7 = (java.lang.Boolean) r7
            boolean r7 = r7.booleanValue()
            r8 = r7
            r7 = r18
            goto L_0x0139
        L_0x011e:
            r7 = r18
            int r8 = r0.getNotifyOverride(r7, r4)
            r10 = -1
            if (r8 != r10) goto L_0x012c
            boolean r8 = r0.isGlobalNotificationsEnabled((long) r4)
            goto L_0x0132
        L_0x012c:
            r10 = 2
            if (r8 == r10) goto L_0x0131
            r8 = 1
            goto L_0x0132
        L_0x0131:
            r8 = 0
        L_0x0132:
            java.lang.Boolean r10 = java.lang.Boolean.valueOf(r8)
            r6.put(r4, r10)
        L_0x0139:
            if (r8 != 0) goto L_0x013c
            goto L_0x0154
        L_0x013c:
            java.lang.Object r8 = r2.valueAt(r1)
            java.lang.Integer r8 = (java.lang.Integer) r8
            int r8 = r8.intValue()
            android.util.LongSparseArray<java.lang.Integer> r10 = r0.pushDialogs
            java.lang.Integer r11 = java.lang.Integer.valueOf(r8)
            r10.put(r4, r11)
            int r4 = r0.total_unread_count
            int r4 = r4 + r8
            r0.total_unread_count = r4
        L_0x0154:
            int r1 = r1 + 1
            r18 = r7
            goto L_0x0100
        L_0x0159:
            r7 = r18
            if (r3 == 0) goto L_0x025d
            r1 = 0
        L_0x015e:
            int r2 = r23.size()
            if (r1 >= r2) goto L_0x025d
            java.lang.Object r2 = r3.get(r1)
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2
            int r4 = r2.getId()
            long r4 = (long) r4
            org.telegram.tgnet.TLRPC$Message r8 = r2.messageOwner
            org.telegram.tgnet.TLRPC$Peer r8 = r8.to_id
            int r8 = r8.channel_id
            if (r8 == 0) goto L_0x017d
            long r10 = (long) r8
            r8 = 32
            long r10 = r10 << r8
            long r4 = r4 | r10
            goto L_0x017f
        L_0x017d:
            r8 = 32
        L_0x017f:
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r10 = r0.pushMessagesDict
            int r10 = r10.indexOfKey(r4)
            if (r10 < 0) goto L_0x018c
        L_0x0187:
            r5 = 0
            r16 = 1
            goto L_0x0259
        L_0x018c:
            boolean r10 = r0.isPersonalMessage(r2)
            if (r10 == 0) goto L_0x0198
            int r10 = r0.personal_count
            r9 = 1
            int r10 = r10 + r9
            r0.personal_count = r10
        L_0x0198:
            long r10 = r2.getDialogId()
            org.telegram.tgnet.TLRPC$Message r12 = r2.messageOwner
            long r13 = r12.random_id
            boolean r8 = r12.mentioned
            if (r8 == 0) goto L_0x01aa
            int r8 = r12.from_id
            r21 = r10
            long r9 = (long) r8
            goto L_0x01ae
        L_0x01aa:
            r21 = r10
            r9 = r21
        L_0x01ae:
            int r8 = r6.indexOfKey(r9)
            if (r8 < 0) goto L_0x01c0
            java.lang.Object r8 = r6.valueAt(r8)
            java.lang.Boolean r8 = (java.lang.Boolean) r8
            boolean r8 = r8.booleanValue()
            r12 = 2
            goto L_0x01da
        L_0x01c0:
            int r8 = r0.getNotifyOverride(r7, r9)
            r11 = -1
            if (r8 != r11) goto L_0x01cd
            boolean r8 = r0.isGlobalNotificationsEnabled((long) r9)
            r12 = 2
            goto L_0x01d3
        L_0x01cd:
            r12 = 2
            if (r8 == r12) goto L_0x01d2
            r8 = 1
            goto L_0x01d3
        L_0x01d2:
            r8 = 0
        L_0x01d3:
            java.lang.Boolean r11 = java.lang.Boolean.valueOf(r8)
            r6.put(r9, r11)
        L_0x01da:
            if (r8 == 0) goto L_0x0187
            r18 = r13
            long r12 = r0.opened_dialog_id
            int r8 = (r9 > r12 ? 1 : (r9 == r12 ? 0 : -1))
            if (r8 != 0) goto L_0x01e9
            boolean r8 = org.telegram.messenger.ApplicationLoader.isScreenOn
            if (r8 == 0) goto L_0x01e9
            goto L_0x0187
        L_0x01e9:
            r11 = 0
            int r8 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r8 == 0) goto L_0x01f5
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r8 = r0.pushMessagesDict
            r8.put(r4, r2)
            goto L_0x0200
        L_0x01f5:
            int r4 = (r18 > r11 ? 1 : (r18 == r11 ? 0 : -1))
            if (r4 == 0) goto L_0x0200
            android.util.LongSparseArray<org.telegram.messenger.MessageObject> r4 = r0.fcmRandomMessagesDict
            r11 = r18
            r4.put(r11, r2)
        L_0x0200:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r4 = r0.pushMessages
            r5 = 0
            r4.add(r5, r2)
            int r2 = (r21 > r9 ? 1 : (r21 == r9 ? 0 : -1))
            if (r2 == 0) goto L_0x022c
            android.util.LongSparseArray<java.lang.Integer> r2 = r0.pushDialogsOverrideMention
            r11 = r21
            java.lang.Object r2 = r2.get(r11)
            java.lang.Integer r2 = (java.lang.Integer) r2
            android.util.LongSparseArray<java.lang.Integer> r4 = r0.pushDialogsOverrideMention
            if (r2 != 0) goto L_0x021c
            r2 = 1
            r16 = 1
            goto L_0x0224
        L_0x021c:
            int r2 = r2.intValue()
            r16 = 1
            int r2 = r2 + 1
        L_0x0224:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r4.put(r11, r2)
            goto L_0x022e
        L_0x022c:
            r16 = 1
        L_0x022e:
            android.util.LongSparseArray<java.lang.Integer> r2 = r0.pushDialogs
            java.lang.Object r2 = r2.get(r9)
            java.lang.Integer r2 = (java.lang.Integer) r2
            if (r2 == 0) goto L_0x023f
            int r4 = r2.intValue()
            int r4 = r4 + 1
            goto L_0x0240
        L_0x023f:
            r4 = 1
        L_0x0240:
            if (r2 == 0) goto L_0x024b
            int r8 = r0.total_unread_count
            int r2 = r2.intValue()
            int r8 = r8 - r2
            r0.total_unread_count = r8
        L_0x024b:
            int r2 = r0.total_unread_count
            int r2 = r2 + r4
            r0.total_unread_count = r2
            android.util.LongSparseArray<java.lang.Integer> r2 = r0.pushDialogs
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r2.put(r9, r4)
        L_0x0259:
            int r1 = r1 + 1
            goto L_0x015e
        L_0x025d:
            r5 = 0
            r16 = 1
            android.util.LongSparseArray<java.lang.Integer> r1 = r0.pushDialogs
            int r1 = r1.size()
            org.telegram.messenger.-$$Lambda$NotificationsController$5iZSA4iiOYvgCSPcQ2XgPFuD-jI r2 = new org.telegram.messenger.-$$Lambda$NotificationsController$5iZSA4iiOYvgCSPcQ2XgPFuD-jI
            r2.<init>(r1)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r2)
            long r1 = android.os.SystemClock.elapsedRealtime()
            r3 = 1000(0x3e8, double:4.94E-321)
            long r1 = r1 / r3
            r3 = 60
            int r6 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r6 >= 0) goto L_0x027d
            r4 = 1
            goto L_0x027e
        L_0x027d:
            r4 = 0
        L_0x027e:
            r0.showOrUpdateNotification(r4)
            boolean r1 = r0.showBadgeNumber
            if (r1 == 0) goto L_0x028c
            int r1 = r20.getTotalAllUnreadCount()
            r0.setBadge(r1)
        L_0x028c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.lambda$processLoadedUnreadMessages$22$NotificationsController(java.util.ArrayList, android.util.LongSparseArray, java.util.ArrayList):void");
    }

    public /* synthetic */ void lambda$null$21$NotificationsController(int i) {
        if (this.total_unread_count == 0) {
            this.popupMessages.clear();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        getNotificationCenter().postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(i));
    }

    private int getTotalAllUnreadCount() {
        int i;
        int i2 = 0;
        for (int i3 = 0; i3 < 3; i3++) {
            if (UserConfig.getInstance(i3).isClientActivated()) {
                NotificationsController instance = getInstance(i3);
                if (instance.showBadgeNumber) {
                    if (instance.showBadgeMessages) {
                        if (instance.showBadgeMuted) {
                            try {
                                int size = MessagesController.getInstance(i3).allDialogs.size();
                                for (int i4 = 0; i4 < size; i4++) {
                                    TLRPC$Dialog tLRPC$Dialog = MessagesController.getInstance(i3).allDialogs.get(i4);
                                    if (tLRPC$Dialog.unread_count != 0) {
                                        i2 += tLRPC$Dialog.unread_count;
                                    }
                                }
                            } catch (Exception e) {
                                FileLog.e((Throwable) e);
                            }
                        } else {
                            i = instance.total_unread_count;
                        }
                    } else if (instance.showBadgeMuted) {
                        try {
                            int size2 = MessagesController.getInstance(i3).allDialogs.size();
                            for (int i5 = 0; i5 < size2; i5++) {
                                if (MessagesController.getInstance(i3).allDialogs.get(i5).unread_count != 0) {
                                    i2++;
                                }
                            }
                        } catch (Exception e2) {
                            FileLog.e((Throwable) e2);
                        }
                    } else {
                        i = instance.pushDialogs.size();
                    }
                    i2 += i;
                }
            }
        }
        return i2;
    }

    public /* synthetic */ void lambda$updateBadge$23$NotificationsController() {
        setBadge(getTotalAllUnreadCount());
    }

    public void updateBadge() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$updateBadge$23$NotificationsController();
            }
        });
    }

    private void setBadge(int i) {
        if (this.lastBadgeCount != i) {
            this.lastBadgeCount = i;
            NotificationBadge.applyCount(i);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01a0, code lost:
        if (r9.getBoolean("EnablePreviewAll", true) == false) goto L_0x01a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x01ac, code lost:
        if (r9.getBoolean("EnablePreviewGroup", r3) != false) goto L_0x01b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x01b6, code lost:
        if (r9.getBoolean("EnablePreviewChannel", r3) != false) goto L_0x01b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x01b8, code lost:
        r2 = r0.messageOwner;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x01ca, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageService) == false) goto L_0x0a97;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x01cc, code lost:
        r20[0] = null;
        r2 = r2.action;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x01d3, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined) != false) goto L_0x0a87;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x01d7, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp) == false) goto L_0x01db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x01dd, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto) == false) goto L_0x01ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x01ed, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationContactNewPhoto", NUM, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x01f1, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation) == false) goto L_0x0252;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x01f3, code lost:
        r1 = org.telegram.messenger.LocaleController.formatString("formatDateAtTime", NUM, org.telegram.messenger.LocaleController.getInstance().formatterYear.format(((long) r0.messageOwner.date) * 1000), org.telegram.messenger.LocaleController.getInstance().formatterDay.format(((long) r0.messageOwner.date) * 1000));
        r0 = r0.messageOwner.action;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0251, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationUnrecognizedDevice", NUM, getUserConfig().getCurrentUser().first_name, r1, r0.title, r0.address);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0254, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGameScore) != false) goto L_0x0a80;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x0258, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPaymentSent) == false) goto L_0x025c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x025e, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall) == false) goto L_0x026a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0269, code lost:
        return org.telegram.messenger.LocaleController.getString("CallMessageIncomingMissed", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x026c, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser) == false) goto L_0x0382;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x026e, code lost:
        r3 = r2.user_id;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0270, code lost:
        if (r3 != 0) goto L_0x028c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0279, code lost:
        if (r2.users.size() != 1) goto L_0x028c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x027b, code lost:
        r3 = r0.messageOwner.action.users.get(0).intValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x028c, code lost:
        if (r3 == 0) goto L_0x032a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0294, code lost:
        if (r0.messageOwner.to_id.channel_id == 0) goto L_0x02af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0298, code lost:
        if (r8.megagroup != false) goto L_0x02af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x02ae, code lost:
        return org.telegram.messenger.LocaleController.formatString("ChannelAddedByNotification", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x02b7, code lost:
        if (r3 != getUserConfig().getClientUserId()) goto L_0x02ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x02cd, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationInvitedToGroup", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x02ce, code lost:
        r0 = getMessagesController().getUser(java.lang.Integer.valueOf(r3));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02da, code lost:
        if (r0 != null) goto L_0x02dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02dc, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x02df, code lost:
        if (r1 != r0.id) goto L_0x030f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x02e3, code lost:
        if (r8.megagroup == false) goto L_0x02fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x02f9, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupAddSelfMega", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x030e, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupAddSelf", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x0329, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupAddMember", NUM, r7, r8.title, org.telegram.messenger.UserObject.getUserName(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x032a, code lost:
        r1 = new java.lang.StringBuilder();
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x033a, code lost:
        if (r2 >= r0.messageOwner.action.users.size()) goto L_0x0367;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x033c, code lost:
        r3 = getMessagesController().getUser(r0.messageOwner.action.users.get(r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0350, code lost:
        if (r3 == null) goto L_0x0364;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0352, code lost:
        r3 = org.telegram.messenger.UserObject.getUserName(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x035a, code lost:
        if (r1.length() == 0) goto L_0x0361;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x035c, code lost:
        r1.append(", ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0361, code lost:
        r1.append(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x0364, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0381, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupAddMember", NUM, r7, r8.title, r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0385, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByLink) == false) goto L_0x039b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x039a, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationInvitedToGroupByLink", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x039f, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditTitle) == false) goto L_0x03b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x03b2, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationEditedGroupName", NUM, r7, r2.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x03b5, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto) != false) goto L_0x0a1b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x03b9, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto) == false) goto L_0x03bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x03bf, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser) == false) goto L_0x042f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x03cb, code lost:
        if (r2.user_id != getUserConfig().getClientUserId()) goto L_0x03e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x03e1, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupKickYou", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x03e8, code lost:
        if (r0.messageOwner.action.user_id != r1) goto L_0x03ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x03fe, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupLeftMember", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x03ff, code lost:
        r0 = getMessagesController().getUser(java.lang.Integer.valueOf(r0.messageOwner.action.user_id));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0411, code lost:
        if (r0 != null) goto L_0x0414;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x0413, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x042e, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationGroupKickMember", NUM, r7, r8.title, org.telegram.messenger.UserObject.getUserName(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0431, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatCreate) == false) goto L_0x043a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x0439, code lost:
        return r0.messageText.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x043c, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelCreate) == false) goto L_0x0445;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x0444, code lost:
        return r0.messageText.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0447, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatMigrateTo) == false) goto L_0x045b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x045a, code lost:
        return org.telegram.messenger.LocaleController.formatString("ActionMigrateFromGroupNotify", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x045f, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom) == false) goto L_0x0471;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0470, code lost:
        return org.telegram.messenger.LocaleController.formatString("ActionMigrateFromGroupNotify", NUM, r2.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x0473, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionScreenshotTaken) == false) goto L_0x047c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x047b, code lost:
        return r0.messageText.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x047e, code lost:
        if ((r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPinMessage) == false) goto L_0x0a1a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x0482, code lost:
        if (r8 == null) goto L_0x0770;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x0488, code lost:
        if (org.telegram.messenger.ChatObject.isChannel(r8) == false) goto L_0x048e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x048c, code lost:
        if (r8.megagroup == false) goto L_0x0770;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x048e, code lost:
        r0 = r0.replyMessageObject;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:244:0x0490, code lost:
        if (r0 != null) goto L_0x04a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x04a6, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedNoText", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x04ae, code lost:
        if (r0.isMusic() == false) goto L_0x04c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x04c1, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedMusic", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x04cb, code lost:
        if (r0.isVideo() == false) goto L_0x0515;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x04cf, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0500;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x04d9, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0500;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x04ff, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedText", NUM, r7, "📹 " + r0.messageOwner.message, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x0514, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedVideo", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x0519, code lost:
        if (r0.isGif() == false) goto L_0x0563;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x051d, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x054e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0527, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x054e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x054d, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedText", NUM, r7, "🎬 " + r0.messageOwner.message, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:0x0562, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGif", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x056a, code lost:
        if (r0.isVoice() == false) goto L_0x057e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:0x057d, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedVoice", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x0582, code lost:
        if (r0.isRoundVideo() == false) goto L_0x0596;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x0595, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedRound", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x059a, code lost:
        if (r0.isSticker() != false) goto L_0x0740;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x05a0, code lost:
        if (r0.isAnimatedSticker() == false) goto L_0x05a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x05a4, code lost:
        r2 = r0.messageOwner;
        r4 = r2.media;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x05aa, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument) == false) goto L_0x05f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x05ae, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x05dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x05b6, code lost:
        if (android.text.TextUtils.isEmpty(r2.message) != false) goto L_0x05dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x05dc, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedText", NUM, r7, "📎 " + r0.messageOwner.message, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x05f1, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedFile", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x05f4, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo) != false) goto L_0x072b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:296:0x05f8, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue) == false) goto L_0x05fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x05fe, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive) == false) goto L_0x0615;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x0614, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGeoLive", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:302:0x0619, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact) == false) goto L_0x063a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:0x061b, code lost:
        r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x0639, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedContact2", NUM, r7, r8.title, org.telegram.messenger.ContactsController.formatName(r4.first_name, r4.last_name));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x063c, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll) == false) goto L_0x0678;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x063e, code lost:
        r0 = ((org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4).poll;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:0x0644, code lost:
        if (r0.quiz == false) goto L_0x065f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:310:0x065e, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedQuiz2", NUM, r7, r8.title, r0.question);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:312:0x0677, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedPoll2", NUM, r7, r8.title, r0.question);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:314:0x067a, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto) == false) goto L_0x06c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x067e, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x06ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x0686, code lost:
        if (android.text.TextUtils.isEmpty(r2.message) != false) goto L_0x06ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x06ac, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedText", NUM, r7, "🖼 " + r0.messageOwner.message, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:322:0x06c1, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedPhoto", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x06c7, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame) == false) goto L_0x06db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x06da, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGame", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x06db, code lost:
        r2 = r0.messageText;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x06dd, code lost:
        if (r2 == null) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x06e3, code lost:
        if (r2.length() <= 0) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x06e5, code lost:
        r0 = r0.messageText;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x06eb, code lost:
        if (r0.length() <= 20) goto L_0x0704;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x06ed, code lost:
        r2 = new java.lang.StringBuilder();
        r4 = 0;
        r2.append(r0.subSequence(0, 20));
        r2.append("...");
        r0 = r2.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x0704, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x0705, code lost:
        r1 = new java.lang.Object[3];
        r1[r4] = r7;
        r1[1] = r0;
        r1[2] = r8.title;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x0715, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedText", NUM, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x072a, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedNoText", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:340:0x073f, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGeo", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x0740, code lost:
        r0 = r0.getStickerEmoji();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x0746, code lost:
        if (r0 == null) goto L_0x075d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x075c, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedStickerEmoji", NUM, r7, r8.title, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x076f, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedSticker", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:347:0x0770, code lost:
        r0 = r0.replyMessageObject;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:348:0x0773, code lost:
        if (r0 != null) goto L_0x0786;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x0785, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedNoTextChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:352:0x078b, code lost:
        if (r0.isMusic() == false) goto L_0x079d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:0x079c, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedMusicChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x07a6, code lost:
        if (r0.isVideo() == false) goto L_0x07eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x07aa, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x07d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x07b4, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x07d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:362:0x07d8, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedTextChannel", NUM, r8.title, "📹 " + r0.messageOwner.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x07ea, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedVideoChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x07ef, code lost:
        if (r0.isGif() == false) goto L_0x0834;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:368:0x07f3, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0822;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:370:0x07fd, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0822;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:372:0x0821, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedTextChannel", NUM, r8.title, "🎬 " + r0.messageOwner.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:374:0x0833, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGifChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:376:0x083a, code lost:
        if (r0.isVoice() == false) goto L_0x084c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:378:0x084b, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedVoiceChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x0850, code lost:
        if (r0.isRoundVideo() == false) goto L_0x0862;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x0861, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedRoundChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x0866, code lost:
        if (r0.isSticker() != false) goto L_0x09ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x086c, code lost:
        if (r0.isAnimatedSticker() == false) goto L_0x0870;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:387:0x0870, code lost:
        r2 = r0.messageOwner;
        r4 = r2.media;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x0876, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument) == false) goto L_0x08b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:390:0x087a, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x08a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:0x0882, code lost:
        if (android.text.TextUtils.isEmpty(r2.message) != false) goto L_0x08a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:0x08a6, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedTextChannel", NUM, r8.title, "📎 " + r0.messageOwner.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:396:0x08b8, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedFileChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:398:0x08bb, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo) != false) goto L_0x09dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x08bf, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue) == false) goto L_0x08c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:402:0x08c5, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive) == false) goto L_0x08d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x08d8, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:406:0x08dc, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact) == false) goto L_0x08fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:407:0x08de, code lost:
        r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:408:0x08fb, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedContactChannel2", NUM, r8.title, org.telegram.messenger.ContactsController.formatName(r4.first_name, r4.last_name));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:410:0x08fe, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll) == false) goto L_0x0936;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:411:0x0900, code lost:
        r0 = ((org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4).poll;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:412:0x0906, code lost:
        if (r0.quiz == false) goto L_0x091f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:414:0x091e, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedQuizChannel2", NUM, r8.title, r0.question);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:416:0x0935, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedPollChannel2", NUM, r8.title, r0.question);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:418:0x0938, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto) == false) goto L_0x097b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:420:0x093c, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0969;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:422:0x0944, code lost:
        if (android.text.TextUtils.isEmpty(r2.message) != false) goto L_0x0969;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:424:0x0968, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedTextChannel", NUM, r8.title, "🖼 " + r0.messageOwner.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:426:0x097a, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedPhotoChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:428:0x097f, code lost:
        if ((r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame) == false) goto L_0x0991;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:430:0x0990, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGameChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:431:0x0991, code lost:
        r2 = r0.messageText;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:432:0x0993, code lost:
        if (r2 == null) goto L_0x09ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:434:0x0999, code lost:
        if (r2.length() <= 0) goto L_0x09ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:435:0x099b, code lost:
        r0 = r0.messageText;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:436:0x09a1, code lost:
        if (r0.length() <= 20) goto L_0x09ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:437:0x09a3, code lost:
        r2 = new java.lang.StringBuilder();
        r4 = 0;
        r2.append(r0.subSequence(0, 20));
        r2.append("...");
        r0 = r2.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:438:0x09ba, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:439:0x09bb, code lost:
        r1 = new java.lang.Object[2];
        r1[r4] = r8.title;
        r1[1] = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:440:0x09c9, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedTextChannel", NUM, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:442:0x09db, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedNoTextChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:444:0x09ed, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedGeoChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:445:0x09ee, code lost:
        r0 = r0.getStickerEmoji();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:446:0x09f3, code lost:
        if (r0 == null) goto L_0x0a09;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:448:0x0a08, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", NUM, r8.title, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:450:0x0a19, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationActionPinnedStickerChannel", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:451:0x0a1a, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:453:0x0a21, code lost:
        if (r0.messageOwner.to_id.channel_id == 0) goto L_0x0a51;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:455:0x0a25, code lost:
        if (r8.megagroup != false) goto L_0x0a51;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:457:0x0a2b, code lost:
        if (r19.isVideoAvatar() == false) goto L_0x0a3f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:459:0x0a3e, code lost:
        return org.telegram.messenger.LocaleController.formatString("ChannelVideoEditNotification", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:461:0x0a50, code lost:
        return org.telegram.messenger.LocaleController.formatString("ChannelPhotoEditNotification", NUM, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:463:0x0a56, code lost:
        if (r19.isVideoAvatar() == false) goto L_0x0a6c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:465:0x0a6b, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationEditedGroupVideo", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:467:0x0a7f, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationEditedGroupPhoto", NUM, r7, r8.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:469:0x0a86, code lost:
        return r0.messageText.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:471:0x0a96, code lost:
        return org.telegram.messenger.LocaleController.formatString("NotificationContactJoined", NUM, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:473:0x0a9b, code lost:
        if (r19.isMediaEmpty() == false) goto L_0x0ab4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:475:0x0aa5, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0aac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:477:0x0aab, code lost:
        return r0.messageOwner.message;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:479:0x0ab3, code lost:
        return org.telegram.messenger.LocaleController.getString("Message", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:480:0x0ab4, code lost:
        r1 = r0.messageOwner;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:481:0x0aba, code lost:
        if ((r1.media instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto) == false) goto L_0x0af8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:483:0x0abe, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0adc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:485:0x0ac6, code lost:
        if (android.text.TextUtils.isEmpty(r1.message) != false) goto L_0x0adc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:487:0x0adb, code lost:
        return "🖼 " + r0.messageOwner.message;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:489:0x0ae2, code lost:
        if (r0.messageOwner.media.ttl_seconds == 0) goto L_0x0aee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:491:0x0aed, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachDestructingPhoto", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:493:0x0af7, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachPhoto", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:495:0x0afc, code lost:
        if (r19.isVideo() == false) goto L_0x0b3c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:497:0x0b00, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0b20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:499:0x0b0a, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0b20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:501:0x0b1f, code lost:
        return "📹 " + r0.messageOwner.message;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:503:0x0b26, code lost:
        if (r0.messageOwner.media.ttl_seconds == 0) goto L_0x0b32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:505:0x0b31, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachDestructingVideo", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:507:0x0b3b, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachVideo", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:509:0x0b40, code lost:
        if (r19.isGame() == false) goto L_0x0b4c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:511:0x0b4b, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachGame", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:513:0x0b50, code lost:
        if (r19.isVoice() == false) goto L_0x0b5c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:515:0x0b5b, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachAudio", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:517:0x0b60, code lost:
        if (r19.isRoundVideo() == false) goto L_0x0b6c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:519:0x0b6b, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachRound", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:521:0x0b70, code lost:
        if (r19.isMusic() == false) goto L_0x0b7c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:523:0x0b7b, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachMusic", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:524:0x0b7c, code lost:
        r1 = r0.messageOwner.media;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:525:0x0b82, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact) == false) goto L_0x0b8e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:527:0x0b8d, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachContact", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:529:0x0b90, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll) == false) goto L_0x0bae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:531:0x0b98, code lost:
        if (((org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1).poll.quiz == false) goto L_0x0ba4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:533:0x0ba3, code lost:
        return org.telegram.messenger.LocaleController.getString("QuizPoll", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:535:0x0bad, code lost:
        return org.telegram.messenger.LocaleController.getString("Poll", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:537:0x0bb0, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo) != false) goto L_0x0c7a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:539:0x0bb4, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue) == false) goto L_0x0bb8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:541:0x0bba, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive) == false) goto L_0x0bc6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:543:0x0bc5, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachLiveLocation", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:545:0x0bc8, code lost:
        if ((r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument) == false) goto L_0x0CLASSNAME;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:547:0x0bce, code lost:
        if (r19.isSticker() != false) goto L_0x0CLASSNAME;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:549:0x0bd4, code lost:
        if (r19.isAnimatedSticker() == false) goto L_0x0bd7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:551:0x0bdb, code lost:
        if (r19.isGif() == false) goto L_0x0CLASSNAME;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:553:0x0bdf, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0bff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:555:0x0be9, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0bff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:557:0x0bfe, code lost:
        return "🎬 " + r0.messageOwner.message;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:559:0x0CLASSNAME, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachGif", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:561:0x0c0b, code lost:
        if (android.os.Build.VERSION.SDK_INT < 19) goto L_0x0c2b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:563:0x0CLASSNAME, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageOwner.message) != false) goto L_0x0c2b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:565:0x0c2a, code lost:
        return "📎 " + r0.messageOwner.message;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:567:0x0CLASSNAME, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachDocument", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:568:0x0CLASSNAME, code lost:
        r0 = r19.getStickerEmoji();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:569:0x0CLASSNAME, code lost:
        if (r0 == null) goto L_0x0CLASSNAME;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:571:0x0CLASSNAME, code lost:
        return r0 + " " + org.telegram.messenger.LocaleController.getString("AttachSticker", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:573:0x0CLASSNAME, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachSticker", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:575:0x0CLASSNAME, code lost:
        if (android.text.TextUtils.isEmpty(r0.messageText) != false) goto L_0x0CLASSNAME;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:577:0x0CLASSNAME, code lost:
        return r0.messageText.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:579:0x0CLASSNAME, code lost:
        return org.telegram.messenger.LocaleController.getString("Message", NUM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:581:0x0CLASSNAME, code lost:
        return org.telegram.messenger.LocaleController.getString("AttachLocation", NUM);
     */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0153 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0154  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getShortStringForMessage(org.telegram.messenger.MessageObject r19, java.lang.String[] r20, boolean[] r21) {
        /*
            r18 = this;
            r0 = r19
            boolean r1 = org.telegram.messenger.AndroidUtilities.needShowPasscode()
            java.lang.String r3 = "NotificationHiddenMessage"
            if (r1 != 0) goto L_0x0CLASSNAME
            boolean r1 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r1 == 0) goto L_0x0010
            goto L_0x0CLASSNAME
        L_0x0010:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            long r4 = r1.dialog_id
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r6 = r1.chat_id
            if (r6 == 0) goto L_0x001b
            goto L_0x001d
        L_0x001b:
            int r6 = r1.channel_id
        L_0x001d:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.user_id
            r7 = 1
            r8 = 0
            if (r21 == 0) goto L_0x0029
            r21[r8] = r7
        L_0x0029:
            org.telegram.messenger.AccountInstance r9 = r18.getAccountInstance()
            android.content.SharedPreferences r9 = r9.getNotificationsSettings()
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "content_preview_"
            r10.append(r11)
            r10.append(r4)
            java.lang.String r10 = r10.toString()
            boolean r10 = r9.getBoolean(r10, r7)
            boolean r11 = r19.isFcmMessage()
            java.lang.String r12 = "EnablePreviewGroup"
            java.lang.String r13 = "EnablePreviewAll"
            java.lang.String r15 = "Message"
            r2 = 27
            r14 = 2
            if (r11 == 0) goto L_0x00e6
            if (r6 != 0) goto L_0x0075
            if (r1 == 0) goto L_0x0075
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 <= r2) goto L_0x0061
            java.lang.String r1 = r0.localName
            r20[r8] = r1
        L_0x0061:
            if (r10 == 0) goto L_0x0069
            boolean r1 = r9.getBoolean(r13, r7)
            if (r1 != 0) goto L_0x00e1
        L_0x0069:
            if (r21 == 0) goto L_0x006d
            r21[r8] = r8
        L_0x006d:
            r0 = 2131625799(0x7f0e0747, float:1.8878816E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r15, r0)
            return r0
        L_0x0075:
            if (r6 == 0) goto L_0x00e1
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x008f
            boolean r1 = r19.isMegagroup()
            if (r1 == 0) goto L_0x0086
            goto L_0x008f
        L_0x0086:
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 <= r2) goto L_0x0093
            java.lang.String r1 = r0.localName
            r20[r8] = r1
            goto L_0x0093
        L_0x008f:
            java.lang.String r1 = r0.localUserName
            r20[r8] = r1
        L_0x0093:
            if (r10 == 0) goto L_0x00ab
            boolean r1 = r0.localChannel
            if (r1 != 0) goto L_0x009f
            boolean r1 = r9.getBoolean(r12, r7)
            if (r1 == 0) goto L_0x00ab
        L_0x009f:
            boolean r1 = r0.localChannel
            if (r1 == 0) goto L_0x00e1
            java.lang.String r1 = "EnablePreviewChannel"
            boolean r1 = r9.getBoolean(r1, r7)
            if (r1 != 0) goto L_0x00e1
        L_0x00ab:
            if (r21 == 0) goto L_0x00af
            r21[r8] = r8
        L_0x00af:
            boolean r1 = r19.isMegagroup()
            if (r1 != 0) goto L_0x00cd
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x00cd
            r1 = 2131624628(0x7f0e02b4, float:1.8876441E38)
            java.lang.Object[] r2 = new java.lang.Object[r7]
            java.lang.String r0 = r0.localName
            r2[r8] = r0
            java.lang.String r0 = "ChannelMessageNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x00cd:
            r1 = 2131626047(0x7f0e083f, float:1.887932E38)
            java.lang.Object[] r2 = new java.lang.Object[r14]
            java.lang.String r3 = r0.localUserName
            r2[r8] = r3
            java.lang.String r0 = r0.localName
            r2[r7] = r0
            java.lang.String r0 = "NotificationMessageGroupNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x00e1:
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            return r0
        L_0x00e6:
            if (r1 != 0) goto L_0x00fc
            boolean r1 = r19.isFromUser()
            if (r1 != 0) goto L_0x00f7
            int r1 = r19.getId()
            if (r1 >= 0) goto L_0x00f5
            goto L_0x00f7
        L_0x00f5:
            int r1 = -r6
            goto L_0x010a
        L_0x00f7:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            int r1 = r1.from_id
            goto L_0x010a
        L_0x00fc:
            org.telegram.messenger.UserConfig r11 = r18.getUserConfig()
            int r11 = r11.getClientUserId()
            if (r1 != r11) goto L_0x010a
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            int r1 = r1.from_id
        L_0x010a:
            r16 = 0
            int r11 = (r4 > r16 ? 1 : (r4 == r16 ? 0 : -1))
            if (r11 != 0) goto L_0x0118
            if (r6 == 0) goto L_0x0115
            int r4 = -r6
            long r4 = (long) r4
            goto L_0x0118
        L_0x0115:
            if (r1 == 0) goto L_0x0118
            long r4 = (long) r1
        L_0x0118:
            r11 = 0
            if (r1 <= 0) goto L_0x013c
            org.telegram.messenger.MessagesController r14 = r18.getMessagesController()
            java.lang.Integer r7 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r7 = r14.getUser(r7)
            if (r7 == 0) goto L_0x0150
            java.lang.String r7 = org.telegram.messenger.UserObject.getUserName(r7)
            if (r6 == 0) goto L_0x0132
            r20[r8] = r7
            goto L_0x0151
        L_0x0132:
            int r14 = android.os.Build.VERSION.SDK_INT
            if (r14 <= r2) goto L_0x0139
            r20[r8] = r7
            goto L_0x0151
        L_0x0139:
            r20[r8] = r11
            goto L_0x0151
        L_0x013c:
            org.telegram.messenger.MessagesController r7 = r18.getMessagesController()
            int r14 = -r1
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)
            org.telegram.tgnet.TLRPC$Chat r7 = r7.getChat(r14)
            if (r7 == 0) goto L_0x0150
            java.lang.String r7 = r7.title
            r20[r8] = r7
            goto L_0x0151
        L_0x0150:
            r7 = r11
        L_0x0151:
            if (r7 != 0) goto L_0x0154
            return r11
        L_0x0154:
            if (r6 == 0) goto L_0x0179
            org.telegram.messenger.MessagesController r14 = r18.getMessagesController()
            java.lang.Integer r8 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$Chat r8 = r14.getChat(r8)
            if (r8 != 0) goto L_0x0165
            return r11
        L_0x0165:
            boolean r14 = org.telegram.messenger.ChatObject.isChannel(r8)
            if (r14 == 0) goto L_0x0177
            boolean r14 = r8.megagroup
            if (r14 != 0) goto L_0x0177
            int r14 = android.os.Build.VERSION.SDK_INT
            if (r14 > r2) goto L_0x0177
            r2 = 0
            r20[r2] = r11
            goto L_0x017b
        L_0x0177:
            r2 = 0
            goto L_0x017b
        L_0x0179:
            r2 = 0
            r8 = r11
        L_0x017b:
            int r5 = (int) r4
            if (r5 != 0) goto L_0x0188
            r20[r2] = r11
            r0 = 2131626024(0x7f0e0828, float:1.8879273E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r3, r0)
            return r0
        L_0x0188:
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r8)
            if (r2 == 0) goto L_0x0194
            boolean r2 = r8.megagroup
            if (r2 != 0) goto L_0x0194
            r2 = 1
            goto L_0x0195
        L_0x0194:
            r2 = 0
        L_0x0195:
            if (r10 == 0) goto L_0x0CLASSNAME
            if (r6 != 0) goto L_0x01a3
            if (r1 == 0) goto L_0x01a3
            r3 = 1
            boolean r4 = r9.getBoolean(r13, r3)
            if (r4 != 0) goto L_0x01b8
            goto L_0x01a4
        L_0x01a3:
            r3 = 1
        L_0x01a4:
            if (r6 == 0) goto L_0x0CLASSNAME
            if (r2 != 0) goto L_0x01ae
            boolean r4 = r9.getBoolean(r12, r3)
            if (r4 != 0) goto L_0x01b8
        L_0x01ae:
            if (r2 == 0) goto L_0x0CLASSNAME
            java.lang.String r2 = "EnablePreviewChannel"
            boolean r2 = r9.getBoolean(r2, r3)
            if (r2 == 0) goto L_0x0CLASSNAME
        L_0x01b8:
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageService
            java.lang.String r4 = "🎬 "
            java.lang.String r5 = "📎 "
            java.lang.String r6 = "📹 "
            java.lang.String r9 = "🖼 "
            r10 = 19
            if (r3 == 0) goto L_0x0a97
            r3 = 0
            r20[r3] = r11
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            boolean r12 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined
            if (r12 != 0) goto L_0x0a87
            boolean r12 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp
            if (r12 == 0) goto L_0x01db
            goto L_0x0a87
        L_0x01db:
            boolean r12 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto
            if (r12 == 0) goto L_0x01ee
            r0 = 2131626009(0x7f0e0819, float:1.8879242E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r3] = r7
            java.lang.String r2 = "NotificationContactNewPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x01ee:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation
            r12 = 3
            if (r3 == 0) goto L_0x0252
            r1 = 2131627544(0x7f0e0e18, float:1.8882355E38)
            r2 = 2
            java.lang.Object[] r3 = new java.lang.Object[r2]
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterYear
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            int r4 = r4.date
            long r4 = (long) r4
            r6 = 1000(0x3e8, double:4.94E-321)
            long r4 = r4 * r6
            java.lang.String r2 = r2.format((long) r4)
            r4 = 0
            r3[r4] = r2
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterDay
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            int r4 = r4.date
            long r4 = (long) r4
            long r4 = r4 * r6
            java.lang.String r2 = r2.format((long) r4)
            r4 = 1
            r3[r4] = r2
            java.lang.String r2 = "formatDateAtTime"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r2, r1, r3)
            r2 = 2131626074(0x7f0e085a, float:1.8879374E38)
            r3 = 4
            java.lang.Object[] r3 = new java.lang.Object[r3]
            org.telegram.messenger.UserConfig r5 = r18.getUserConfig()
            org.telegram.tgnet.TLRPC$User r5 = r5.getCurrentUser()
            java.lang.String r5 = r5.first_name
            r6 = 0
            r3[r6] = r5
            r3[r4] = r1
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.lang.String r1 = r0.title
            r4 = 2
            r3[r4] = r1
            java.lang.String r0 = r0.address
            r3[r12] = r0
            java.lang.String r0 = "NotificationUnrecognizedDevice"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r2, r3)
            return r0
        L_0x0252:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGameScore
            if (r3 != 0) goto L_0x0a80
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPaymentSent
            if (r3 == 0) goto L_0x025c
            goto L_0x0a80
        L_0x025c:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall
            if (r3 == 0) goto L_0x026a
            r0 = 2131624523(0x7f0e024b, float:1.8876228E38)
            java.lang.String r1 = "CallMessageIncomingMissed"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x026a:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser
            if (r3 == 0) goto L_0x0382
            int r3 = r2.user_id
            if (r3 != 0) goto L_0x028c
            java.util.ArrayList<java.lang.Integer> r2 = r2.users
            int r2 = r2.size()
            r4 = 1
            if (r2 != r4) goto L_0x028c
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            java.util.ArrayList<java.lang.Integer> r2 = r2.users
            r3 = 0
            java.lang.Object r2 = r2.get(r3)
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r3 = r2.intValue()
        L_0x028c:
            if (r3 == 0) goto L_0x032a
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.to_id
            int r0 = r0.channel_id
            if (r0 == 0) goto L_0x02af
            boolean r0 = r8.megagroup
            if (r0 != 0) goto L_0x02af
            r0 = 2131624579(0x7f0e0283, float:1.8876342E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "ChannelAddedByNotification"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x02af:
            org.telegram.messenger.UserConfig r0 = r18.getUserConfig()
            int r0 = r0.getClientUserId()
            if (r3 != r0) goto L_0x02ce
            r0 = 2131626026(0x7f0e082a, float:1.8879277E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "NotificationInvitedToGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x02ce:
            org.telegram.messenger.MessagesController r0 = r18.getMessagesController()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r2)
            if (r0 != 0) goto L_0x02dd
            return r11
        L_0x02dd:
            int r2 = r0.id
            if (r1 != r2) goto L_0x030f
            boolean r0 = r8.megagroup
            if (r0 == 0) goto L_0x02fa
            r0 = 2131626015(0x7f0e081f, float:1.8879254E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "NotificationGroupAddSelfMega"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x02fa:
            r1 = 2
            r2 = 0
            r3 = 1
            r0 = 2131626014(0x7f0e081e, float:1.8879252E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationGroupAddSelf"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x030f:
            r2 = 0
            r3 = 1
            r1 = 2131626013(0x7f0e081d, float:1.887925E38)
            java.lang.Object[] r4 = new java.lang.Object[r12]
            r4[r2] = r7
            java.lang.String r2 = r8.title
            r4[r3] = r2
            java.lang.String r0 = org.telegram.messenger.UserObject.getUserName(r0)
            r2 = 2
            r4[r2] = r0
            java.lang.String r0 = "NotificationGroupAddMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r4)
            return r0
        L_0x032a:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r2 = 0
        L_0x0330:
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r3 = r3.action
            java.util.ArrayList<java.lang.Integer> r3 = r3.users
            int r3 = r3.size()
            if (r2 >= r3) goto L_0x0367
            org.telegram.messenger.MessagesController r3 = r18.getMessagesController()
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r4 = r4.action
            java.util.ArrayList<java.lang.Integer> r4 = r4.users
            java.lang.Object r4 = r4.get(r2)
            java.lang.Integer r4 = (java.lang.Integer) r4
            org.telegram.tgnet.TLRPC$User r3 = r3.getUser(r4)
            if (r3 == 0) goto L_0x0364
            java.lang.String r3 = org.telegram.messenger.UserObject.getUserName(r3)
            int r4 = r1.length()
            if (r4 == 0) goto L_0x0361
            java.lang.String r4 = ", "
            r1.append(r4)
        L_0x0361:
            r1.append(r3)
        L_0x0364:
            int r2 = r2 + 1
            goto L_0x0330
        L_0x0367:
            r0 = 2131626013(0x7f0e081d, float:1.887925E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            r3 = 0
            r2[r3] = r7
            java.lang.String r3 = r8.title
            r4 = 1
            r2[r4] = r3
            java.lang.String r1 = r1.toString()
            r3 = 2
            r2[r3] = r1
            java.lang.String r1 = "NotificationGroupAddMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            return r0
        L_0x0382:
            r3 = 2
            boolean r13 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByLink
            if (r13 == 0) goto L_0x039b
            r0 = 2131626027(0x7f0e082b, float:1.8879279E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r13 = 0
            r1[r13] = r7
            java.lang.String r2 = r8.title
            r14 = 1
            r1[r14] = r2
            java.lang.String r2 = "NotificationInvitedToGroupByLink"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x039b:
            r13 = 0
            r14 = 1
            boolean r15 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditTitle
            if (r15 == 0) goto L_0x03b3
            r0 = 2131626010(0x7f0e081a, float:1.8879244E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r13] = r7
            java.lang.String r2 = r2.title
            r1[r14] = r2
            java.lang.String r2 = "NotificationEditedGroupName"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x03b3:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto
            if (r3 != 0) goto L_0x0a1b
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto
            if (r3 == 0) goto L_0x03bd
            goto L_0x0a1b
        L_0x03bd:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser
            if (r3 == 0) goto L_0x042f
            int r2 = r2.user_id
            org.telegram.messenger.UserConfig r3 = r18.getUserConfig()
            int r3 = r3.getClientUserId()
            if (r2 != r3) goto L_0x03e2
            r0 = 2131626020(0x7f0e0824, float:1.8879264E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "NotificationGroupKickYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x03e2:
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            int r2 = r2.user_id
            if (r2 != r1) goto L_0x03ff
            r0 = 2131626021(0x7f0e0825, float:1.8879266E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "NotificationGroupLeftMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x03ff:
            org.telegram.messenger.MessagesController r1 = r18.getMessagesController()
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            int r0 = r0.user_id
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            org.telegram.tgnet.TLRPC$User r0 = r1.getUser(r0)
            if (r0 != 0) goto L_0x0414
            return r11
        L_0x0414:
            r1 = 2131626019(0x7f0e0823, float:1.8879262E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            r3 = 0
            r2[r3] = r7
            java.lang.String r3 = r8.title
            r4 = 1
            r2[r4] = r3
            java.lang.String r0 = org.telegram.messenger.UserObject.getUserName(r0)
            r3 = 2
            r2[r3] = r0
            java.lang.String r0 = "NotificationGroupKickMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x042f:
            boolean r1 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatCreate
            if (r1 == 0) goto L_0x043a
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
            return r0
        L_0x043a:
            boolean r1 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelCreate
            if (r1 == 0) goto L_0x0445
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
            return r0
        L_0x0445:
            boolean r1 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatMigrateTo
            if (r1 == 0) goto L_0x045b
            r0 = 2131624103(0x7f0e00a7, float:1.8875376E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "ActionMigrateFromGroupNotify"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x045b:
            r1 = 1
            r3 = 0
            boolean r13 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom
            if (r13 == 0) goto L_0x0471
            r0 = 2131624103(0x7f0e00a7, float:1.8875376E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r2.title
            r1[r3] = r2
            java.lang.String r2 = "ActionMigrateFromGroupNotify"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0471:
            boolean r1 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionScreenshotTaken
            if (r1 == 0) goto L_0x047c
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
            return r0
        L_0x047c:
            boolean r1 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPinMessage
            if (r1 == 0) goto L_0x0a1a
            r1 = 20
            if (r8 == 0) goto L_0x0770
            boolean r2 = org.telegram.messenger.ChatObject.isChannel(r8)
            if (r2 == 0) goto L_0x048e
            boolean r2 = r8.megagroup
            if (r2 == 0) goto L_0x0770
        L_0x048e:
            org.telegram.messenger.MessageObject r0 = r0.replyMessageObject
            if (r0 != 0) goto L_0x04a7
            r0 = 2131625988(0x7f0e0804, float:1.88792E38)
            r2 = 2
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r3 = 0
            r1[r3] = r7
            java.lang.String r2 = r8.title
            r11 = 1
            r1[r11] = r2
            java.lang.String r2 = "NotificationActionPinnedNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x04a7:
            r2 = 2
            r3 = 0
            r11 = 1
            boolean r13 = r0.isMusic()
            if (r13 == 0) goto L_0x04c2
            r0 = 2131625986(0x7f0e0802, float:1.8879195E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r1[r3] = r7
            java.lang.String r2 = r8.title
            r1[r11] = r2
            java.lang.String r2 = "NotificationActionPinnedMusic"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x04c2:
            boolean r2 = r0.isVideo()
            r3 = 2131626002(0x7f0e0812, float:1.8879228E38)
            java.lang.String r11 = "NotificationActionPinnedText"
            if (r2 == 0) goto L_0x0515
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0500
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0500
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r6)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r2 = 0
            r1[r2] = r7
            r4 = 1
            r1[r4] = r0
            java.lang.String r0 = r8.title
            r5 = 2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r3, r1)
            return r0
        L_0x0500:
            r2 = 0
            r4 = 1
            r5 = 2
            r0 = 2131626004(0x7f0e0814, float:1.8879232E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0515:
            boolean r2 = r0.isGif()
            if (r2 == 0) goto L_0x0563
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x054e
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x054e
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r2 = 0
            r1[r2] = r7
            r4 = 1
            r1[r4] = r0
            java.lang.String r0 = r8.title
            r6 = 2
            r1[r6] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r3, r1)
            return r0
        L_0x054e:
            r2 = 0
            r4 = 1
            r6 = 2
            r0 = 2131625982(0x7f0e07fe, float:1.8879187E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedGif"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0563:
            r2 = 0
            r4 = 1
            r6 = 2
            boolean r13 = r0.isVoice()
            if (r13 == 0) goto L_0x057e
            r0 = 2131626006(0x7f0e0816, float:1.8879236E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedVoice"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x057e:
            boolean r13 = r0.isRoundVideo()
            if (r13 == 0) goto L_0x0596
            r0 = 2131625996(0x7f0e080c, float:1.8879216E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedRound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0596:
            boolean r2 = r0.isSticker()
            if (r2 != 0) goto L_0x0740
            boolean r2 = r0.isAnimatedSticker()
            if (r2 == 0) goto L_0x05a4
            goto L_0x0740
        L_0x05a4:
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r2.media
            boolean r6 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r6 == 0) goto L_0x05f2
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x05dd
            java.lang.String r1 = r2.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x05dd
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r5)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r2 = 0
            r1[r2] = r7
            r4 = 1
            r1[r4] = r0
            java.lang.String r0 = r8.title
            r5 = 2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r3, r1)
            return r0
        L_0x05dd:
            r2 = 0
            r4 = 1
            r5 = 2
            r0 = 2131625972(0x7f0e07f4, float:1.8879167E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedFile"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x05f2:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r5 != 0) goto L_0x072b
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r5 == 0) goto L_0x05fc
            goto L_0x072b
        L_0x05fc:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r5 == 0) goto L_0x0615
            r0 = 2131625980(0x7f0e07fc, float:1.8879183E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r5 = 0
            r1[r5] = r7
            java.lang.String r2 = r8.title
            r6 = 1
            r1[r6] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoLive"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0615:
            r5 = 0
            r6 = 1
            boolean r13 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r13 == 0) goto L_0x063a
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r4
            r0 = 2131625970(0x7f0e07f2, float:1.8879163E38)
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r1[r5] = r7
            java.lang.String r2 = r8.title
            r1[r6] = r2
            java.lang.String r2 = r4.first_name
            java.lang.String r3 = r4.last_name
            java.lang.String r2 = org.telegram.messenger.ContactsController.formatName(r2, r3)
            r3 = 2
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedContact2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x063a:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r5 == 0) goto L_0x0678
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4
            org.telegram.tgnet.TLRPC$Poll r0 = r4.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x065f
            r1 = 2131625994(0x7f0e080a, float:1.8879212E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            r3 = 0
            r2[r3] = r7
            java.lang.String r3 = r8.title
            r4 = 1
            r2[r4] = r3
            java.lang.String r0 = r0.question
            r5 = 2
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedQuiz2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x065f:
            r3 = 0
            r4 = 1
            r5 = 2
            r1 = 2131625992(0x7f0e0808, float:1.8879208E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            r2[r3] = r7
            java.lang.String r3 = r8.title
            r2[r4] = r3
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedPoll2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x0678:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r5 == 0) goto L_0x06c2
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x06ad
            java.lang.String r1 = r2.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x06ad
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r9)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r2 = 0
            r1[r2] = r7
            r5 = 1
            r1[r5] = r0
            java.lang.String r0 = r8.title
            r6 = 2
            r1[r6] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r3, r1)
            return r0
        L_0x06ad:
            r2 = 0
            r5 = 1
            r6 = 2
            r0 = 2131625990(0x7f0e0806, float:1.8879204E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x06c2:
            r2 = 0
            r5 = 1
            r6 = 2
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r4 == 0) goto L_0x06db
            r0 = 2131625974(0x7f0e07f6, float:1.8879171E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r2] = r7
            java.lang.String r2 = r8.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGame"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x06db:
            java.lang.CharSequence r2 = r0.messageText
            if (r2 == 0) goto L_0x0716
            int r2 = r2.length()
            if (r2 <= 0) goto L_0x0716
            java.lang.CharSequence r0 = r0.messageText
            int r2 = r0.length()
            if (r2 <= r1) goto L_0x0704
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r4 = 0
            java.lang.CharSequence r0 = r0.subSequence(r4, r1)
            r2.append(r0)
            java.lang.String r0 = "..."
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            goto L_0x0705
        L_0x0704:
            r4 = 0
        L_0x0705:
            java.lang.Object[] r1 = new java.lang.Object[r12]
            r1[r4] = r7
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = r8.title
            r5 = 2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r3, r1)
            return r0
        L_0x0716:
            r2 = 1
            r4 = 0
            r5 = 2
            r0 = 2131625988(0x7f0e0804, float:1.88792E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r7
            java.lang.String r3 = r8.title
            r1[r2] = r3
            java.lang.String r2 = "NotificationActionPinnedNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x072b:
            r2 = 1
            r4 = 0
            r5 = 2
            r0 = 2131625978(0x7f0e07fa, float:1.887918E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r7
            java.lang.String r3 = r8.title
            r1[r2] = r3
            java.lang.String r2 = "NotificationActionPinnedGeo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0740:
            r2 = 1
            r4 = 0
            java.lang.String r0 = r0.getStickerEmoji()
            if (r0 == 0) goto L_0x075d
            r1 = 2131626000(0x7f0e0810, float:1.8879224E38)
            java.lang.Object[] r3 = new java.lang.Object[r12]
            r3[r4] = r7
            java.lang.String r4 = r8.title
            r3[r2] = r4
            r5 = 2
            r3[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedStickerEmoji"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r3)
            return r0
        L_0x075d:
            r5 = 2
            r0 = 2131625998(0x7f0e080e, float:1.887922E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r7
            java.lang.String r3 = r8.title
            r1[r2] = r3
            java.lang.String r2 = "NotificationActionPinnedSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0770:
            r2 = 1
            org.telegram.messenger.MessageObject r0 = r0.replyMessageObject
            if (r0 != 0) goto L_0x0786
            r0 = 2131625989(0x7f0e0805, float:1.8879202E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedNoTextChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0786:
            r3 = 0
            boolean r7 = r0.isMusic()
            if (r7 == 0) goto L_0x079d
            r0 = 2131625987(0x7f0e0803, float:1.8879197E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedMusicChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x079d:
            boolean r2 = r0.isVideo()
            r3 = 2131626003(0x7f0e0813, float:1.887923E38)
            java.lang.String r7 = "NotificationActionPinnedTextChannel"
            if (r2 == 0) goto L_0x07eb
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x07d9
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x07d9
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r6)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r4 = 0
            r1[r4] = r2
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r3, r1)
            return r0
        L_0x07d9:
            r2 = 1
            r4 = 0
            r0 = 2131626005(0x7f0e0815, float:1.8879234E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedVideoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x07eb:
            boolean r2 = r0.isGif()
            if (r2 == 0) goto L_0x0834
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0822
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0822
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r4 = 0
            r1[r4] = r2
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r3, r1)
            return r0
        L_0x0822:
            r2 = 1
            r4 = 0
            r0 = 2131625983(0x7f0e07ff, float:1.887919E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedGifChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0834:
            r2 = 1
            r4 = 0
            boolean r6 = r0.isVoice()
            if (r6 == 0) goto L_0x084c
            r0 = 2131626007(0x7f0e0817, float:1.8879238E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedVoiceChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x084c:
            boolean r6 = r0.isRoundVideo()
            if (r6 == 0) goto L_0x0862
            r0 = 2131625997(0x7f0e080d, float:1.8879218E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedRoundChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0862:
            boolean r2 = r0.isSticker()
            if (r2 != 0) goto L_0x09ee
            boolean r2 = r0.isAnimatedSticker()
            if (r2 == 0) goto L_0x0870
            goto L_0x09ee
        L_0x0870:
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r2.media
            boolean r6 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r6 == 0) goto L_0x08b9
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x08a7
            java.lang.String r1 = r2.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x08a7
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r5)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r4 = 0
            r1[r4] = r2
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r3, r1)
            return r0
        L_0x08a7:
            r2 = 1
            r4 = 0
            r0 = 2131625973(0x7f0e07f5, float:1.887917E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedFileChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x08b9:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r5 != 0) goto L_0x09dc
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r5 == 0) goto L_0x08c3
            goto L_0x09dc
        L_0x08c3:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r5 == 0) goto L_0x08d9
            r0 = 2131625981(0x7f0e07fd, float:1.8879185E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r5 = 0
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoLiveChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x08d9:
            r5 = 0
            boolean r6 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r6 == 0) goto L_0x08fc
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r4
            r0 = 2131625971(0x7f0e07f3, float:1.8879165E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r1[r5] = r2
            java.lang.String r2 = r4.first_name
            java.lang.String r3 = r4.last_name
            java.lang.String r2 = org.telegram.messenger.ContactsController.formatName(r2, r3)
            r3 = 1
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedContactChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x08fc:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r5 == 0) goto L_0x0936
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4
            org.telegram.tgnet.TLRPC$Poll r0 = r4.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x091f
            r1 = 2131625995(0x7f0e080b, float:1.8879214E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r8.title
            r4 = 0
            r2[r4] = r3
            java.lang.String r0 = r0.question
            r3 = 1
            r2[r3] = r0
            java.lang.String r0 = "NotificationActionPinnedQuizChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x091f:
            r2 = 2
            r3 = 1
            r4 = 0
            r1 = 2131625993(0x7f0e0809, float:1.887921E38)
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r5 = r8.title
            r2[r4] = r5
            java.lang.String r0 = r0.question
            r2[r3] = r0
            java.lang.String r0 = "NotificationActionPinnedPollChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x0936:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r5 == 0) goto L_0x097b
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0969
            java.lang.String r1 = r2.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0969
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r9)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r5 = 0
            r1[r5] = r2
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r3, r1)
            return r0
        L_0x0969:
            r2 = 1
            r5 = 0
            r0 = 2131625991(0x7f0e0807, float:1.8879206E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedPhotoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x097b:
            r2 = 1
            r5 = 0
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r4 == 0) goto L_0x0991
            r0 = 2131625975(0x7f0e07f7, float:1.8879173E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGameChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0991:
            java.lang.CharSequence r2 = r0.messageText
            if (r2 == 0) goto L_0x09ca
            int r2 = r2.length()
            if (r2 <= 0) goto L_0x09ca
            java.lang.CharSequence r0 = r0.messageText
            int r2 = r0.length()
            if (r2 <= r1) goto L_0x09ba
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r4 = 0
            java.lang.CharSequence r0 = r0.subSequence(r4, r1)
            r2.append(r0)
            java.lang.String r0 = "..."
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            goto L_0x09bb
        L_0x09ba:
            r4 = 0
        L_0x09bb:
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r3, r1)
            return r0
        L_0x09ca:
            r2 = 1
            r4 = 0
            r0 = 2131625989(0x7f0e0805, float:1.8879202E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedNoTextChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x09dc:
            r2 = 1
            r4 = 0
            r0 = 2131625979(0x7f0e07fb, float:1.8879181E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x09ee:
            r4 = 0
            java.lang.String r0 = r0.getStickerEmoji()
            if (r0 == 0) goto L_0x0a09
            r1 = 2131626001(0x7f0e0811, float:1.8879226E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r8.title
            r2[r4] = r3
            r3 = 1
            r2[r3] = r0
            java.lang.String r0 = "NotificationActionPinnedStickerEmojiChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x0a09:
            r3 = 1
            r0 = 2131625999(0x7f0e080f, float:1.8879222E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedStickerChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a1a:
            return r11
        L_0x0a1b:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x0a51
            boolean r1 = r8.megagroup
            if (r1 != 0) goto L_0x0a51
            boolean r0 = r19.isVideoAvatar()
            if (r0 == 0) goto L_0x0a3f
            r0 = 2131624677(0x7f0e02e5, float:1.887654E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "ChannelVideoEditNotification"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a3f:
            r1 = 1
            r3 = 0
            r0 = 2131624644(0x7f0e02c4, float:1.8876474E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r8.title
            r1[r3] = r2
            java.lang.String r2 = "ChannelPhotoEditNotification"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a51:
            r3 = 0
            boolean r0 = r19.isVideoAvatar()
            if (r0 == 0) goto L_0x0a6c
            r0 = 2131626012(0x7f0e081c, float:1.8879248E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r3] = r7
            java.lang.String r2 = r8.title
            r4 = 1
            r1[r4] = r2
            java.lang.String r2 = "NotificationEditedGroupVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a6c:
            r1 = 2
            r4 = 1
            r0 = 2131626011(0x7f0e081b, float:1.8879246E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r3] = r7
            java.lang.String r2 = r8.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationEditedGroupPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a80:
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
            return r0
        L_0x0a87:
            r4 = 1
            r0 = 2131626008(0x7f0e0818, float:1.887924E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r2 = 0
            r1[r2] = r7
            java.lang.String r2 = "NotificationContactJoined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            return r0
        L_0x0a97:
            boolean r1 = r19.isMediaEmpty()
            if (r1 == 0) goto L_0x0ab4
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0aac
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            return r0
        L_0x0aac:
            r0 = 2131625799(0x7f0e0747, float:1.8878816E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r15, r0)
            return r0
        L_0x0ab4:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r2 = r1.media
            boolean r2 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r2 == 0) goto L_0x0af8
            int r2 = android.os.Build.VERSION.SDK_INT
            if (r2 < r10) goto L_0x0adc
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0adc
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r9)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            return r0
        L_0x0adc:
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0aee
            r0 = 2131624329(0x7f0e0189, float:1.8875835E38)
            java.lang.String r1 = "AttachDestructingPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0aee:
            r0 = 2131624344(0x7f0e0198, float:1.8875865E38)
            java.lang.String r1 = "AttachPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0af8:
            boolean r1 = r19.isVideo()
            if (r1 == 0) goto L_0x0b3c
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0b20
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0b20
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r6)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            return r0
        L_0x0b20:
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0b32
            r0 = 2131624330(0x7f0e018a, float:1.8875837E38)
            java.lang.String r1 = "AttachDestructingVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b32:
            r0 = 2131624350(0x7f0e019e, float:1.8875877E38)
            java.lang.String r1 = "AttachVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b3c:
            boolean r1 = r19.isGame()
            if (r1 == 0) goto L_0x0b4c
            r0 = 2131624332(0x7f0e018c, float:1.887584E38)
            java.lang.String r1 = "AttachGame"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b4c:
            boolean r1 = r19.isVoice()
            if (r1 == 0) goto L_0x0b5c
            r0 = 2131624326(0x7f0e0186, float:1.8875829E38)
            java.lang.String r1 = "AttachAudio"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b5c:
            boolean r1 = r19.isRoundVideo()
            if (r1 == 0) goto L_0x0b6c
            r0 = 2131624346(0x7f0e019a, float:1.887587E38)
            java.lang.String r1 = "AttachRound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b6c:
            boolean r1 = r19.isMusic()
            if (r1 == 0) goto L_0x0b7c
            r0 = 2131624343(0x7f0e0197, float:1.8875863E38)
            java.lang.String r1 = "AttachMusic"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b7c:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r2 == 0) goto L_0x0b8e
            r0 = 2131624328(0x7f0e0188, float:1.8875833E38)
            java.lang.String r1 = "AttachContact"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0b8e:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r2 == 0) goto L_0x0bae
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1
            org.telegram.tgnet.TLRPC$Poll r0 = r1.poll
            boolean r0 = r0.quiz
            if (r0 == 0) goto L_0x0ba4
            r0 = 2131626620(0x7f0e0a7c, float:1.8880481E38)
            java.lang.String r1 = "QuizPoll"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0ba4:
            r0 = 2131626527(0x7f0e0a1f, float:1.8880293E38)
            java.lang.String r1 = "Poll"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0bae:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r2 != 0) goto L_0x0c7a
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r2 == 0) goto L_0x0bb8
            goto L_0x0c7a
        L_0x0bb8:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r2 == 0) goto L_0x0bc6
            r0 = 2131624338(0x7f0e0192, float:1.8875853E38)
            java.lang.String r1 = "AttachLiveLocation"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0bc6:
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r1 == 0) goto L_0x0CLASSNAME
            boolean r1 = r19.isSticker()
            if (r1 != 0) goto L_0x0CLASSNAME
            boolean r1 = r19.isAnimatedSticker()
            if (r1 == 0) goto L_0x0bd7
            goto L_0x0CLASSNAME
        L_0x0bd7:
            boolean r1 = r19.isGif()
            if (r1 == 0) goto L_0x0CLASSNAME
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0bff
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0bff
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            return r0
        L_0x0bff:
            r0 = 2131624333(0x7f0e018d, float:1.8875843E38)
            java.lang.String r1 = "AttachGif"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0CLASSNAME:
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 < r10) goto L_0x0c2b
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0c2b
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r5)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            return r0
        L_0x0c2b:
            r0 = 2131624331(0x7f0e018b, float:1.8875839E38)
            java.lang.String r1 = "AttachDocument"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0CLASSNAME:
            java.lang.String r0 = r19.getStickerEmoji()
            if (r0 == 0) goto L_0x0CLASSNAME
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = " "
            r1.append(r0)
            r0 = 2131624347(0x7f0e019b, float:1.8875871E38)
            java.lang.String r2 = "AttachSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            return r0
        L_0x0CLASSNAME:
            r0 = 2131624347(0x7f0e019b, float:1.8875871E38)
            java.lang.String r1 = "AttachSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0CLASSNAME:
            java.lang.CharSequence r1 = r0.messageText
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0CLASSNAME
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = r0.toString()
            return r0
        L_0x0CLASSNAME:
            r0 = 2131625799(0x7f0e0747, float:1.8878816E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r15, r0)
            return r0
        L_0x0c7a:
            r0 = 2131624340(0x7f0e0194, float:1.8875857E38)
            java.lang.String r1 = "AttachLocation"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        L_0x0CLASSNAME:
            if (r21 == 0) goto L_0x0CLASSNAME
            r0 = 0
            r21[r0] = r0
        L_0x0CLASSNAME:
            r0 = 2131625799(0x7f0e0747, float:1.8878816E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r15, r0)
            return r0
        L_0x0CLASSNAME:
            r0 = 2131626024(0x7f0e0828, float:1.8879273E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r3, r0)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.getShortStringForMessage(org.telegram.messenger.MessageObject, java.lang.String[], boolean[]):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:75:0x0148 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0149  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getStringForMessage(org.telegram.messenger.MessageObject r20, boolean r21, boolean[] r22, boolean[] r23) {
        /*
            r19 = this;
            r0 = r20
            boolean r1 = org.telegram.messenger.AndroidUtilities.needShowPasscode()
            if (r1 != 0) goto L_0x1381
            boolean r1 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r1 == 0) goto L_0x000e
            goto L_0x1381
        L_0x000e:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            long r2 = r1.dialog_id
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r4 = r1.chat_id
            if (r4 == 0) goto L_0x0019
            goto L_0x001b
        L_0x0019:
            int r4 = r1.channel_id
        L_0x001b:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.user_id
            r5 = 1
            r6 = 0
            if (r23 == 0) goto L_0x0027
            r23[r6] = r5
        L_0x0027:
            org.telegram.messenger.AccountInstance r7 = r19.getAccountInstance()
            android.content.SharedPreferences r7 = r7.getNotificationsSettings()
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "content_preview_"
            r8.append(r9)
            r8.append(r2)
            java.lang.String r8 = r8.toString()
            boolean r8 = r7.getBoolean(r8, r5)
            boolean r9 = r20.isFcmMessage()
            r10 = 2131626047(0x7f0e083f, float:1.887932E38)
            java.lang.String r11 = "NotificationMessageGroupNoText"
            r12 = 2131626060(0x7f0e084c, float:1.8879346E38)
            java.lang.String r13 = "NotificationMessageNoText"
            r14 = 2
            if (r9 == 0) goto L_0x00c6
            if (r4 != 0) goto L_0x0072
            if (r1 == 0) goto L_0x0072
            if (r8 == 0) goto L_0x0063
            java.lang.String r1 = "EnablePreviewAll"
            boolean r1 = r7.getBoolean(r1, r5)
            if (r1 != 0) goto L_0x00bf
        L_0x0063:
            if (r23 == 0) goto L_0x0067
            r23[r6] = r6
        L_0x0067:
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r0 = r0.localName
            r1[r6] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r13, r12, r1)
            return r0
        L_0x0072:
            if (r4 == 0) goto L_0x00bf
            if (r8 == 0) goto L_0x008e
            boolean r1 = r0.localChannel
            if (r1 != 0) goto L_0x0082
            java.lang.String r1 = "EnablePreviewGroup"
            boolean r1 = r7.getBoolean(r1, r5)
            if (r1 == 0) goto L_0x008e
        L_0x0082:
            boolean r1 = r0.localChannel
            if (r1 == 0) goto L_0x00bf
            java.lang.String r1 = "EnablePreviewChannel"
            boolean r1 = r7.getBoolean(r1, r5)
            if (r1 != 0) goto L_0x00bf
        L_0x008e:
            if (r23 == 0) goto L_0x0092
            r23[r6] = r6
        L_0x0092:
            boolean r1 = r20.isMegagroup()
            if (r1 != 0) goto L_0x00b0
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x00b0
            r1 = 2131624628(0x7f0e02b4, float:1.8876441E38)
            java.lang.Object[] r2 = new java.lang.Object[r5]
            java.lang.String r0 = r0.localName
            r2[r6] = r0
            java.lang.String r0 = "ChannelMessageNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            return r0
        L_0x00b0:
            java.lang.Object[] r1 = new java.lang.Object[r14]
            java.lang.String r2 = r0.localUserName
            r1[r6] = r2
            java.lang.String r0 = r0.localName
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r10, r1)
            return r0
        L_0x00bf:
            r22[r6] = r5
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r0 = (java.lang.String) r0
            return r0
        L_0x00c6:
            org.telegram.messenger.UserConfig r9 = r19.getUserConfig()
            int r9 = r9.getClientUserId()
            if (r1 != 0) goto L_0x00e4
            boolean r1 = r20.isFromUser()
            if (r1 != 0) goto L_0x00df
            int r1 = r20.getId()
            if (r1 >= 0) goto L_0x00dd
            goto L_0x00df
        L_0x00dd:
            int r1 = -r4
            goto L_0x00ea
        L_0x00df:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            int r1 = r1.from_id
            goto L_0x00ea
        L_0x00e4:
            if (r1 != r9) goto L_0x00ea
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            int r1 = r1.from_id
        L_0x00ea:
            r15 = 0
            int r17 = (r2 > r15 ? 1 : (r2 == r15 ? 0 : -1))
            if (r17 != 0) goto L_0x00f8
            if (r4 == 0) goto L_0x00f5
            int r2 = -r4
            long r2 = (long) r2
            goto L_0x00f8
        L_0x00f5:
            if (r1 == 0) goto L_0x00f8
            long r2 = (long) r1
        L_0x00f8:
            r15 = 0
            if (r1 <= 0) goto L_0x0131
            org.telegram.tgnet.TLRPC$Message r10 = r0.messageOwner
            boolean r10 = r10.from_scheduled
            if (r10 == 0) goto L_0x011c
            r17 = r13
            long r12 = (long) r9
            int r18 = (r2 > r12 ? 1 : (r2 == r12 ? 0 : -1))
            if (r18 != 0) goto L_0x0112
            r12 = 2131625816(0x7f0e0758, float:1.887885E38)
            java.lang.String r13 = "MessageScheduledReminderNotification"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r13, r12)
            goto L_0x0146
        L_0x0112:
            r12 = 2131626068(0x7f0e0854, float:1.8879362E38)
            java.lang.String r13 = "NotificationMessageScheduledName"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r13, r12)
            goto L_0x0146
        L_0x011c:
            r17 = r13
            org.telegram.messenger.MessagesController r12 = r19.getMessagesController()
            java.lang.Integer r13 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r12 = r12.getUser(r13)
            if (r12 == 0) goto L_0x0145
            java.lang.String r12 = org.telegram.messenger.UserObject.getUserName(r12)
            goto L_0x0146
        L_0x0131:
            r17 = r13
            org.telegram.messenger.MessagesController r12 = r19.getMessagesController()
            int r13 = -r1
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)
            org.telegram.tgnet.TLRPC$Chat r12 = r12.getChat(r13)
            if (r12 == 0) goto L_0x0145
            java.lang.String r12 = r12.title
            goto L_0x0146
        L_0x0145:
            r12 = r15
        L_0x0146:
            if (r12 != 0) goto L_0x0149
            return r15
        L_0x0149:
            if (r4 == 0) goto L_0x015a
            org.telegram.messenger.MessagesController r13 = r19.getMessagesController()
            java.lang.Integer r10 = java.lang.Integer.valueOf(r4)
            org.telegram.tgnet.TLRPC$Chat r10 = r13.getChat(r10)
            if (r10 != 0) goto L_0x015b
            return r15
        L_0x015a:
            r10 = r15
        L_0x015b:
            int r3 = (int) r2
            if (r3 != 0) goto L_0x0169
            r0 = 2131627476(0x7f0e0dd4, float:1.8882218E38)
            java.lang.String r1 = "YouHaveNewMessage"
            java.lang.String r15 = org.telegram.messenger.LocaleController.getString(r1, r0)
            goto L_0x1380
        L_0x0169:
            java.lang.String r2 = "🎬 "
            java.lang.String r3 = "📎 "
            java.lang.String r13 = "📹 "
            java.lang.String r15 = "🖼 "
            java.lang.String r14 = "NotificationMessageText"
            r6 = 3
            if (r4 != 0) goto L_0x054e
            if (r1 == 0) goto L_0x054e
            if (r8 == 0) goto L_0x053a
            java.lang.String r1 = "EnablePreviewAll"
            boolean r1 = r7.getBoolean(r1, r5)
            if (r1 == 0) goto L_0x053a
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageService
            if (r4 == 0) goto L_0x023f
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined
            if (r2 != 0) goto L_0x022f
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp
            if (r2 == 0) goto L_0x0198
            goto L_0x022f
        L_0x0198:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto
            if (r2 == 0) goto L_0x01ac
            r0 = 2131626009(0x7f0e0819, float:1.8879242E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = "NotificationContactNewPhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x01ac:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation
            if (r2 == 0) goto L_0x020f
            r1 = 2131627544(0x7f0e0e18, float:1.8882355E38)
            r2 = 2
            java.lang.Object[] r3 = new java.lang.Object[r2]
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterYear
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            int r4 = r4.date
            long r7 = (long) r4
            r9 = 1000(0x3e8, double:4.94E-321)
            long r7 = r7 * r9
            java.lang.String r2 = r2.format((long) r7)
            r4 = 0
            r3[r4] = r2
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterDay
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            int r4 = r4.date
            long r7 = (long) r4
            long r7 = r7 * r9
            java.lang.String r2 = r2.format((long) r7)
            r3[r5] = r2
            java.lang.String r2 = "formatDateAtTime"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r2, r1, r3)
            r2 = 2131626074(0x7f0e085a, float:1.8879374E38)
            r3 = 4
            java.lang.Object[] r3 = new java.lang.Object[r3]
            org.telegram.messenger.UserConfig r4 = r19.getUserConfig()
            org.telegram.tgnet.TLRPC$User r4 = r4.getCurrentUser()
            java.lang.String r4 = r4.first_name
            r7 = 0
            r3[r7] = r4
            r3[r5] = r1
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.lang.String r1 = r0.title
            r4 = 2
            r3[r4] = r1
            java.lang.String r0 = r0.address
            r3[r6] = r0
            java.lang.String r0 = "NotificationUnrecognizedDevice"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r0, r2, r3)
            goto L_0x1380
        L_0x020f:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGameScore
            if (r2 != 0) goto L_0x0227
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPaymentSent
            if (r2 == 0) goto L_0x0218
            goto L_0x0227
        L_0x0218:
            boolean r0 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall
            if (r0 == 0) goto L_0x137e
            r0 = 2131624523(0x7f0e024b, float:1.8876228E38)
            java.lang.String r1 = "CallMessageIncomingMissed"
            java.lang.String r15 = org.telegram.messenger.LocaleController.getString(r1, r0)
            goto L_0x1380
        L_0x0227:
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r15 = r0.toString()
            goto L_0x1380
        L_0x022f:
            r0 = 2131626008(0x7f0e0818, float:1.887924E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = "NotificationContactJoined"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x023f:
            boolean r1 = r20.isMediaEmpty()
            if (r1 == 0) goto L_0x0288
            if (r21 != 0) goto L_0x0278
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0268
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x0268:
            r2 = 0
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r0[r2] = r12
            r4 = r17
            r1 = 2131626060(0x7f0e084c, float:1.8879346E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r4, r1, r0)
            goto L_0x1380
        L_0x0278:
            r4 = r17
            r1 = 2131626060(0x7f0e084c, float:1.8879346E38)
            r2 = 0
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r0[r2] = r12
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r4, r1, r0)
            goto L_0x1380
        L_0x0288:
            r4 = r17
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r6 = r1.media
            boolean r6 = r6 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r6 == 0) goto L_0x02ef
            if (r21 != 0) goto L_0x02c8
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r2 < r3) goto L_0x02c8
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x02c8
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r15)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x02c8:
            r2 = 0
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x02e0
            r0 = 2131626065(0x7f0e0851, float:1.8879356E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessageSDPhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x02e0:
            r0 = 2131626061(0x7f0e084d, float:1.8879348E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessagePhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x02ef:
            boolean r1 = r20.isVideo()
            if (r1 == 0) goto L_0x0354
            if (r21 != 0) goto L_0x032d
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x032d
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x032d
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r6 = 0
            r1[r6] = r12
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r13)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r6] = r5
            goto L_0x1380
        L_0x032d:
            r6 = 0
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0345
            r0 = 2131626066(0x7f0e0852, float:1.8879358E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r6] = r12
            java.lang.String r2 = "NotificationMessageSDVideo"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0345:
            r0 = 2131626072(0x7f0e0858, float:1.887937E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r6] = r12
            java.lang.String r2 = "NotificationMessageVideo"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0354:
            r6 = 0
            boolean r1 = r20.isGame()
            if (r1 == 0) goto L_0x0375
            r1 = 2131626034(0x7f0e0832, float:1.8879293E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r2[r6] = r12
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$TL_game r0 = r0.game
            java.lang.String r0 = r0.title
            r2[r5] = r0
            java.lang.String r0 = "NotificationMessageGame"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x1380
        L_0x0375:
            boolean r1 = r20.isVoice()
            if (r1 == 0) goto L_0x038b
            r0 = 2131626029(0x7f0e082d, float:1.8879283E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r6 = 0
            r1[r6] = r12
            java.lang.String r2 = "NotificationMessageAudio"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x038b:
            r6 = 0
            boolean r1 = r20.isRoundVideo()
            if (r1 == 0) goto L_0x03a1
            r0 = 2131626064(0x7f0e0850, float:1.8879354E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r6] = r12
            java.lang.String r2 = "NotificationMessageRound"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x03a1:
            boolean r1 = r20.isMusic()
            if (r1 == 0) goto L_0x03b6
            r0 = 2131626059(0x7f0e084b, float:1.8879343E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r6] = r12
            java.lang.String r2 = "NotificationMessageMusic"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x03b6:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            boolean r7 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r7 == 0) goto L_0x03da
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r1
            r0 = 2131626030(0x7f0e082e, float:1.8879285E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r2[r6] = r12
            java.lang.String r3 = r1.first_name
            java.lang.String r1 = r1.last_name
            java.lang.String r1 = org.telegram.messenger.ContactsController.formatName(r3, r1)
            r2[r5] = r1
            java.lang.String r1 = "NotificationMessageContact2"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            goto L_0x1380
        L_0x03da:
            boolean r6 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r6 == 0) goto L_0x0410
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1
            org.telegram.tgnet.TLRPC$Poll r0 = r1.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x03fa
            r1 = 2131626063(0x7f0e084f, float:1.8879352E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r3 = 0
            r2[r3] = r12
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "NotificationMessageQuiz2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x03fa:
            r2 = 2
            r3 = 0
            r1 = 2131626062(0x7f0e084e, float:1.887935E38)
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r2[r3] = r12
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "NotificationMessagePoll2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
        L_0x040d:
            r15 = r0
            goto L_0x1380
        L_0x0410:
            boolean r6 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r6 != 0) goto L_0x052a
            boolean r6 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r6 == 0) goto L_0x041a
            goto L_0x052a
        L_0x041a:
            boolean r6 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r6 == 0) goto L_0x042e
            r0 = 2131626057(0x7f0e0849, float:1.887934E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessageLiveLocation"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x042e:
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r1 == 0) goto L_0x04fe
            boolean r1 = r20.isSticker()
            if (r1 != 0) goto L_0x04d6
            boolean r1 = r20.isAnimatedSticker()
            if (r1 == 0) goto L_0x0440
            goto L_0x04d6
        L_0x0440:
            boolean r1 = r20.isGif()
            if (r1 == 0) goto L_0x048e
            if (r21 != 0) goto L_0x047e
            int r1 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r1 < r3) goto L_0x047e
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x047e
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r3 = 0
            r1[r3] = r12
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r2)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r3] = r5
            goto L_0x1380
        L_0x047e:
            r3 = 0
            r0 = 2131626036(0x7f0e0834, float:1.8879297E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r3] = r12
            java.lang.String r2 = "NotificationMessageGif"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x048e:
            if (r21 != 0) goto L_0x04c6
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x04c6
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x04c6
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r3)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x04c6:
            r2 = 0
            r0 = 2131626031(0x7f0e082f, float:1.8879287E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessageDocument"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x04d6:
            r2 = 0
            java.lang.String r0 = r20.getStickerEmoji()
            if (r0 == 0) goto L_0x04ef
            r1 = 2131626070(0x7f0e0856, float:1.8879366E38)
            r3 = 2
            java.lang.Object[] r3 = new java.lang.Object[r3]
            r3[r2] = r12
            r3[r5] = r0
            java.lang.String r0 = "NotificationMessageStickerEmoji"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r3)
            goto L_0x040d
        L_0x04ef:
            r0 = 2131626069(0x7f0e0855, float:1.8879364E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessageSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x04fe:
            r2 = 0
            if (r21 != 0) goto L_0x051d
            java.lang.CharSequence r1 = r0.messageText
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x051d
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r2] = r12
            java.lang.CharSequence r0 = r0.messageText
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x051d:
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r0[r2] = r12
            r1 = 2131626060(0x7f0e084c, float:1.8879346E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r4, r1, r0)
            goto L_0x1380
        L_0x052a:
            r2 = 0
            r0 = 2131626058(0x7f0e084a, float:1.8879341E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "NotificationMessageMap"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x053a:
            r4 = r17
            r2 = 0
            if (r23 == 0) goto L_0x0541
            r23[r2] = r2
        L_0x0541:
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r0[r2] = r12
            r1 = 2131626060(0x7f0e084c, float:1.8879346E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r4, r1, r0)
            goto L_0x1380
        L_0x054e:
            if (r4 == 0) goto L_0x137e
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r10)
            if (r4 == 0) goto L_0x055c
            boolean r4 = r10.megagroup
            if (r4 != 0) goto L_0x055c
            r4 = 1
            goto L_0x055d
        L_0x055c:
            r4 = 0
        L_0x055d:
            if (r8 == 0) goto L_0x1350
            if (r4 != 0) goto L_0x0569
            java.lang.String r8 = "EnablePreviewGroup"
            boolean r8 = r7.getBoolean(r8, r5)
            if (r8 != 0) goto L_0x0573
        L_0x0569:
            if (r4 == 0) goto L_0x1350
            java.lang.String r4 = "EnablePreviewChannel"
            boolean r4 = r7.getBoolean(r4, r5)
            if (r4 == 0) goto L_0x1350
        L_0x0573:
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageService
            if (r7 == 0) goto L_0x0dad
            org.telegram.tgnet.TLRPC$MessageAction r4 = r4.action
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser
            if (r7 == 0) goto L_0x068b
            int r2 = r4.user_id
            if (r2 != 0) goto L_0x059c
            java.util.ArrayList<java.lang.Integer> r3 = r4.users
            int r3 = r3.size()
            if (r3 != r5) goto L_0x059c
            org.telegram.tgnet.TLRPC$Message r2 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            java.util.ArrayList<java.lang.Integer> r2 = r2.users
            r3 = 0
            java.lang.Object r2 = r2.get(r3)
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
        L_0x059c:
            if (r2 == 0) goto L_0x0633
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.to_id
            int r0 = r0.channel_id
            if (r0 == 0) goto L_0x05bf
            boolean r0 = r10.megagroup
            if (r0 != 0) goto L_0x05bf
            r0 = 2131624579(0x7f0e0283, float:1.8876342E38)
            r3 = 2
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r4 = 0
            r1[r4] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "ChannelAddedByNotification"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x05bf:
            r3 = 2
            r4 = 0
            if (r2 != r9) goto L_0x05d6
            r0 = 2131626026(0x7f0e082a, float:1.8879277E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r4] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationInvitedToGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x05d6:
            org.telegram.messenger.MessagesController r0 = r19.getMessagesController()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r2)
            if (r0 != 0) goto L_0x05e6
            r2 = 0
            return r2
        L_0x05e6:
            int r2 = r0.id
            if (r1 != r2) goto L_0x0618
            boolean r0 = r10.megagroup
            if (r0 == 0) goto L_0x0603
            r0 = 2131626015(0x7f0e081f, float:1.8879254E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationGroupAddSelfMega"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0603:
            r1 = 2
            r2 = 0
            r0 = 2131626014(0x7f0e081e, float:1.8879252E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationGroupAddSelf"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0618:
            r2 = 0
            r1 = 2131626013(0x7f0e081d, float:1.887925E38)
            java.lang.Object[] r3 = new java.lang.Object[r6]
            r3[r2] = r12
            java.lang.String r2 = r10.title
            r3[r5] = r2
            java.lang.String r0 = org.telegram.messenger.UserObject.getUserName(r0)
            r2 = 2
            r3[r2] = r0
            java.lang.String r0 = "NotificationGroupAddMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r3)
            goto L_0x040d
        L_0x0633:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r2 = 0
        L_0x0639:
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r3 = r3.action
            java.util.ArrayList<java.lang.Integer> r3 = r3.users
            int r3 = r3.size()
            if (r2 >= r3) goto L_0x0670
            org.telegram.messenger.MessagesController r3 = r19.getMessagesController()
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r4 = r4.action
            java.util.ArrayList<java.lang.Integer> r4 = r4.users
            java.lang.Object r4 = r4.get(r2)
            java.lang.Integer r4 = (java.lang.Integer) r4
            org.telegram.tgnet.TLRPC$User r3 = r3.getUser(r4)
            if (r3 == 0) goto L_0x066d
            java.lang.String r3 = org.telegram.messenger.UserObject.getUserName(r3)
            int r4 = r1.length()
            if (r4 == 0) goto L_0x066a
            java.lang.String r4 = ", "
            r1.append(r4)
        L_0x066a:
            r1.append(r3)
        L_0x066d:
            int r2 = r2 + 1
            goto L_0x0639
        L_0x0670:
            r0 = 2131626013(0x7f0e081d, float:1.887925E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r1 = r1.toString()
            r7 = 2
            r2[r7] = r1
            java.lang.String r1 = "NotificationGroupAddMember"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            goto L_0x040d
        L_0x068b:
            r7 = 2
            boolean r8 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByLink
            if (r8 == 0) goto L_0x06a4
            r0 = 2131626027(0x7f0e082b, float:1.8879279E38)
            java.lang.Object[] r1 = new java.lang.Object[r7]
            r8 = 0
            r1[r8] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationInvitedToGroupByLink"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x06a4:
            r8 = 0
            boolean r11 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditTitle
            if (r11 == 0) goto L_0x06bc
            r0 = 2131626010(0x7f0e081a, float:1.8879244E38)
            java.lang.Object[] r1 = new java.lang.Object[r7]
            r1[r8] = r12
            java.lang.String r2 = r4.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationEditedGroupName"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x06bc:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto
            if (r7 != 0) goto L_0x0d48
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto
            if (r7 == 0) goto L_0x06c6
            goto L_0x0d48
        L_0x06c6:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser
            if (r7 == 0) goto L_0x072b
            int r2 = r4.user_id
            if (r2 != r9) goto L_0x06e3
            r0 = 2131626020(0x7f0e0824, float:1.8879264E38)
            r3 = 2
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r4 = 0
            r1[r4] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationGroupKickYou"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x06e3:
            r3 = 2
            r4 = 0
            if (r2 != r1) goto L_0x06fa
            r0 = 2131626021(0x7f0e0825, float:1.8879266E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r4] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationGroupLeftMember"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x06fa:
            org.telegram.messenger.MessagesController r1 = r19.getMessagesController()
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            int r0 = r0.user_id
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            org.telegram.tgnet.TLRPC$User r0 = r1.getUser(r0)
            if (r0 != 0) goto L_0x0710
            r1 = 0
            return r1
        L_0x0710:
            r1 = 2131626019(0x7f0e0823, float:1.8879262E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r0 = org.telegram.messenger.UserObject.getUserName(r0)
            r3 = 2
            r2[r3] = r0
            java.lang.String r0 = "NotificationGroupKickMember"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x1380
        L_0x072b:
            r1 = 0
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatCreate
            if (r7 == 0) goto L_0x0738
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r15 = r0.toString()
            goto L_0x1380
        L_0x0738:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelCreate
            if (r7 == 0) goto L_0x0744
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r15 = r0.toString()
            goto L_0x1380
        L_0x0744:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatMigrateTo
            if (r7 == 0) goto L_0x075a
            r0 = 2131624103(0x7f0e00a7, float:1.8875376E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r7 = 0
            r1[r7] = r2
            java.lang.String r2 = "ActionMigrateFromGroupNotify"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x075a:
            r7 = 0
            boolean r8 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom
            if (r8 == 0) goto L_0x0770
            r0 = 2131624103(0x7f0e00a7, float:1.8875376E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r4.title
            r1[r7] = r2
            java.lang.String r2 = "ActionMigrateFromGroupNotify"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0770:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionScreenshotTaken
            if (r7 == 0) goto L_0x077c
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r15 = r0.toString()
            goto L_0x1380
        L_0x077c:
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPinMessage
            if (r7 == 0) goto L_0x0d3c
            if (r10 == 0) goto L_0x0a80
            boolean r1 = org.telegram.messenger.ChatObject.isChannel(r10)
            if (r1 == 0) goto L_0x078c
            boolean r1 = r10.megagroup
            if (r1 == 0) goto L_0x0a80
        L_0x078c:
            org.telegram.messenger.MessageObject r1 = r0.replyMessageObject
            if (r1 != 0) goto L_0x07a5
            r0 = 2131625988(0x7f0e0804, float:1.88792E38)
            r4 = 2
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r7 = 0
            r1[r7] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedNoText"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x07a5:
            r4 = 2
            r7 = 0
            boolean r8 = r1.isMusic()
            if (r8 == 0) goto L_0x07c0
            r0 = 2131625986(0x7f0e0802, float:1.8879195E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r7] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedMusic"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x07c0:
            boolean r4 = r1.isVideo()
            r7 = 2131626002(0x7f0e0812, float:1.8879228E38)
            java.lang.String r8 = "NotificationActionPinnedText"
            if (r4 == 0) goto L_0x0815
            int r0 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r0 < r2) goto L_0x0800
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            java.lang.String r0 = r0.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0800
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r13)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            r1[r5] = r0
            java.lang.String r0 = r10.title
            r3 = 2
            r1[r3] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r8, r7, r1)
            goto L_0x040d
        L_0x0800:
            r2 = 0
            r3 = 2
            r0 = 2131626004(0x7f0e0814, float:1.8879232E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0815:
            boolean r4 = r1.isGif()
            if (r4 == 0) goto L_0x0865
            int r0 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r0 < r3) goto L_0x0850
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            java.lang.String r0 = r0.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0850
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r2)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            r1[r5] = r0
            java.lang.String r0 = r10.title
            r4 = 2
            r1[r4] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r8, r7, r1)
            goto L_0x040d
        L_0x0850:
            r2 = 0
            r4 = 2
            r0 = 2131625982(0x7f0e07fe, float:1.8879187E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGif"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0865:
            r2 = 0
            r4 = 2
            boolean r9 = r1.isVoice()
            if (r9 == 0) goto L_0x0880
            r0 = 2131626006(0x7f0e0816, float:1.8879236E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedVoice"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0880:
            boolean r9 = r1.isRoundVideo()
            if (r9 == 0) goto L_0x0899
            r0 = 2131625996(0x7f0e080c, float:1.8879216E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedRound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0899:
            boolean r2 = r1.isSticker()
            if (r2 != 0) goto L_0x0a4f
            boolean r2 = r1.isAnimatedSticker()
            if (r2 == 0) goto L_0x08a7
            goto L_0x0a4f
        L_0x08a7:
            org.telegram.tgnet.TLRPC$Message r2 = r1.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r2.media
            boolean r9 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r9 == 0) goto L_0x08f7
            int r0 = android.os.Build.VERSION.SDK_INT
            r4 = 19
            if (r0 < r4) goto L_0x08e2
            java.lang.String r0 = r2.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x08e2
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r3)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            r1[r5] = r0
            java.lang.String r0 = r10.title
            r3 = 2
            r1[r3] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r8, r7, r1)
            goto L_0x040d
        L_0x08e2:
            r2 = 0
            r3 = 2
            r0 = 2131625972(0x7f0e07f4, float:1.8879167E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedFile"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x08f7:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r3 != 0) goto L_0x0a3a
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r3 == 0) goto L_0x0901
            goto L_0x0a3a
        L_0x0901:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r3 == 0) goto L_0x091a
            r0 = 2131625980(0x7f0e07fc, float:1.8879183E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoLive"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x091a:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r3 == 0) goto L_0x0943
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r0
            r1 = 2131625970(0x7f0e07f2, float:1.8879163E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r3 = r0.first_name
            java.lang.String r0 = r0.last_name
            java.lang.String r0 = org.telegram.messenger.ContactsController.formatName(r3, r0)
            r3 = 2
            r2[r3] = r0
            java.lang.String r0 = "NotificationActionPinnedContact2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0943:
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r0 == 0) goto L_0x0981
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4
            org.telegram.tgnet.TLRPC$Poll r0 = r4.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x0968
            r1 = 2131625994(0x7f0e080a, float:1.8879212E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r0 = r0.question
            r4 = 2
            r2[r4] = r0
            java.lang.String r0 = "NotificationActionPinnedQuiz2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0968:
            r3 = 0
            r4 = 2
            r1 = 2131625992(0x7f0e0808, float:1.8879208E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r0 = r0.question
            r2[r4] = r0
            java.lang.String r0 = "NotificationActionPinnedPoll2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0981:
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r0 == 0) goto L_0x09cd
            int r0 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r0 < r3) goto L_0x09b8
            java.lang.String r0 = r2.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x09b8
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r15)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            r1[r5] = r0
            java.lang.String r0 = r10.title
            r3 = 2
            r1[r3] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r8, r7, r1)
            goto L_0x040d
        L_0x09b8:
            r2 = 0
            r3 = 2
            r0 = 2131625990(0x7f0e0806, float:1.8879204E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x09cd:
            r2 = 0
            r3 = 2
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r0 == 0) goto L_0x09e6
            r0 = 2131625974(0x7f0e07f6, float:1.8879171E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGame"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x09e6:
            java.lang.CharSequence r0 = r1.messageText
            if (r0 == 0) goto L_0x0a25
            int r0 = r0.length()
            if (r0 <= 0) goto L_0x0a25
            java.lang.CharSequence r0 = r1.messageText
            int r1 = r0.length()
            r2 = 20
            if (r1 <= r2) goto L_0x0a13
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r2 = 20
            r3 = 0
            java.lang.CharSequence r0 = r0.subSequence(r3, r2)
            r1.append(r0)
            java.lang.String r0 = "..."
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            goto L_0x0a14
        L_0x0a13:
            r3 = 0
        L_0x0a14:
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r1[r3] = r12
            r1[r5] = r0
            java.lang.String r0 = r10.title
            r2 = 2
            r1[r2] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r8, r7, r1)
            goto L_0x040d
        L_0x0a25:
            r2 = 2
            r3 = 0
            r0 = 2131625988(0x7f0e0804, float:1.88792E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r1[r3] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedNoText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0a3a:
            r2 = 2
            r3 = 0
            r0 = 2131625978(0x7f0e07fa, float:1.887918E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r1[r3] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedGeo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0a4f:
            r3 = 0
            java.lang.String r0 = r1.getStickerEmoji()
            if (r0 == 0) goto L_0x0a6c
            r1 = 2131626000(0x7f0e0810, float:1.8879224E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            r4 = 2
            r2[r4] = r0
            java.lang.String r0 = "NotificationActionPinnedStickerEmoji"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0a6c:
            r4 = 2
            r0 = 2131625998(0x7f0e080e, float:1.887922E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r3] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationActionPinnedSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0a80:
            org.telegram.messenger.MessageObject r1 = r0.replyMessageObject
            if (r1 != 0) goto L_0x0a96
            r0 = 2131625989(0x7f0e0805, float:1.8879202E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r4 = 0
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedNoTextChannel"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0a96:
            r4 = 0
            boolean r6 = r1.isMusic()
            if (r6 == 0) goto L_0x0aae
            r0 = 2131625987(0x7f0e0803, float:1.8879197E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedMusicChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0aae:
            boolean r4 = r1.isVideo()
            r6 = 2131626003(0x7f0e0813, float:1.887923E38)
            java.lang.String r7 = "NotificationActionPinnedTextChannel"
            if (r4 == 0) goto L_0x0afe
            int r0 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r0 < r2) goto L_0x0aec
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            java.lang.String r0 = r0.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0aec
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r13)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r10.title
            r3 = 0
            r1[r3] = r2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r6, r1)
            goto L_0x040d
        L_0x0aec:
            r3 = 0
            r0 = 2131626005(0x7f0e0815, float:1.8879234E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedVideoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0afe:
            boolean r4 = r1.isGif()
            if (r4 == 0) goto L_0x0b49
            int r0 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r0 < r3) goto L_0x0b37
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            java.lang.String r0 = r0.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0b37
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r2)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r10.title
            r4 = 0
            r1[r4] = r2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r6, r1)
            goto L_0x040d
        L_0x0b37:
            r4 = 0
            r0 = 2131625983(0x7f0e07ff, float:1.887919E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedGifChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0b49:
            r4 = 0
            boolean r2 = r1.isVoice()
            if (r2 == 0) goto L_0x0b61
            r0 = 2131626007(0x7f0e0817, float:1.8879238E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedVoiceChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0b61:
            boolean r2 = r1.isRoundVideo()
            if (r2 == 0) goto L_0x0b78
            r0 = 2131625997(0x7f0e080d, float:1.8879218E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r4] = r2
            java.lang.String r2 = "NotificationActionPinnedRoundChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0b78:
            boolean r2 = r1.isSticker()
            if (r2 != 0) goto L_0x0d10
            boolean r2 = r1.isAnimatedSticker()
            if (r2 == 0) goto L_0x0b86
            goto L_0x0d10
        L_0x0b86:
            org.telegram.tgnet.TLRPC$Message r2 = r1.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r2.media
            boolean r8 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r8 == 0) goto L_0x0bd1
            int r0 = android.os.Build.VERSION.SDK_INT
            r4 = 19
            if (r0 < r4) goto L_0x0bbf
            java.lang.String r0 = r2.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0bbf
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r3)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r10.title
            r3 = 0
            r1[r3] = r2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r6, r1)
            goto L_0x040d
        L_0x0bbf:
            r3 = 0
            r0 = 2131625973(0x7f0e07f5, float:1.887917E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedFileChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0bd1:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r3 != 0) goto L_0x0cfe
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r3 == 0) goto L_0x0bdb
            goto L_0x0cfe
        L_0x0bdb:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r3 == 0) goto L_0x0bf1
            r0 = 2131625981(0x7f0e07fd, float:1.8879185E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoLiveChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0bf1:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r3 == 0) goto L_0x0CLASSNAME
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r0
            r1 = 2131625971(0x7f0e07f3, float:1.8879165E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r10.title
            r4 = 0
            r2[r4] = r3
            java.lang.String r3 = r0.first_name
            java.lang.String r0 = r0.last_name
            java.lang.String r0 = org.telegram.messenger.ContactsController.formatName(r3, r0)
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedContactChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0CLASSNAME:
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r0 == 0) goto L_0x0CLASSNAME
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4
            org.telegram.tgnet.TLRPC$Poll r0 = r4.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x0c3b
            r1 = 2131625995(0x7f0e080b, float:1.8879214E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r10.title
            r4 = 0
            r2[r4] = r3
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedQuizChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0c3b:
            r2 = 2
            r4 = 0
            r1 = 2131625993(0x7f0e0809, float:1.887921E38)
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r10.title
            r2[r4] = r3
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedPollChannel2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0CLASSNAME:
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r0 == 0) goto L_0x0CLASSNAME
            int r0 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r0 < r3) goto L_0x0CLASSNAME
            java.lang.String r0 = r2.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0CLASSNAME
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r15)
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner
            java.lang.String r1 = r1.message
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r10.title
            r3 = 0
            r1[r3] = r2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r6, r1)
            goto L_0x040d
        L_0x0CLASSNAME:
            r3 = 0
            r0 = 2131625991(0x7f0e0807, float:1.8879206E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedPhotoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0CLASSNAME:
            r3 = 0
            boolean r0 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r0 == 0) goto L_0x0caf
            r0 = 2131625975(0x7f0e07f7, float:1.8879173E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedGameChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0caf:
            java.lang.CharSequence r0 = r1.messageText
            if (r0 == 0) goto L_0x0cec
            int r0 = r0.length()
            if (r0 <= 0) goto L_0x0cec
            java.lang.CharSequence r0 = r1.messageText
            int r1 = r0.length()
            r2 = 20
            if (r1 <= r2) goto L_0x0cdc
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r2 = 20
            r3 = 0
            java.lang.CharSequence r0 = r0.subSequence(r3, r2)
            r1.append(r0)
            java.lang.String r0 = "..."
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            goto L_0x0cdd
        L_0x0cdc:
            r3 = 0
        L_0x0cdd:
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            r1[r5] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r7, r6, r1)
            goto L_0x040d
        L_0x0cec:
            r3 = 0
            r0 = 2131625989(0x7f0e0805, float:1.8879202E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedNoTextChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0cfe:
            r3 = 0
            r0 = 2131625979(0x7f0e07fb, float:1.8879181E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedGeoChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0d10:
            r3 = 0
            java.lang.String r0 = r1.getStickerEmoji()
            if (r0 == 0) goto L_0x0d2b
            r1 = 2131626001(0x7f0e0811, float:1.8879226E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r4 = r10.title
            r2[r3] = r4
            r2[r5] = r0
            java.lang.String r0 = "NotificationActionPinnedStickerEmojiChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0d2b:
            r0 = 2131625999(0x7f0e080f, float:1.8879222E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "NotificationActionPinnedStickerChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x0d3c:
            boolean r2 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGameScore
            if (r2 == 0) goto L_0x137f
            java.lang.CharSequence r0 = r0.messageText
            java.lang.String r15 = r0.toString()
            goto L_0x1380
        L_0x0d48:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.to_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x0d7e
            boolean r1 = r10.megagroup
            if (r1 != 0) goto L_0x0d7e
            boolean r0 = r20.isVideoAvatar()
            if (r0 == 0) goto L_0x0d6c
            r0 = 2131624677(0x7f0e02e5, float:1.887654E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "ChannelVideoEditNotification"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0d6c:
            r3 = 0
            r0 = 2131624644(0x7f0e02c4, float:1.8876474E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            java.lang.String r2 = r10.title
            r1[r3] = r2
            java.lang.String r2 = "ChannelPhotoEditNotification"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0d7e:
            r3 = 0
            boolean r0 = r20.isVideoAvatar()
            if (r0 == 0) goto L_0x0d99
            r0 = 2131626012(0x7f0e081c, float:1.8879248E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r3] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationEditedGroupVideo"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0d99:
            r1 = 2
            r0 = 2131626011(0x7f0e081b, float:1.8879246E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r3] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationEditedGroupPhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0dad:
            boolean r1 = org.telegram.messenger.ChatObject.isChannel(r10)
            if (r1 == 0) goto L_0x1052
            boolean r1 = r10.megagroup
            if (r1 != 0) goto L_0x1052
            boolean r1 = r20.isMediaEmpty()
            if (r1 == 0) goto L_0x0df0
            if (r21 != 0) goto L_0x0de0
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0de0
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x0de0:
            r2 = 0
            r0 = 2131624628(0x7f0e02b4, float:1.8876441E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageNoText"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0df0:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r1.media
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r4 == 0) goto L_0x0e3e
            if (r21 != 0) goto L_0x0e2e
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r2 < r3) goto L_0x0e2e
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0e2e
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r15)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x0e2e:
            r2 = 0
            r0 = 2131624629(0x7f0e02b5, float:1.8876443E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessagePhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0e3e:
            boolean r1 = r20.isVideo()
            if (r1 == 0) goto L_0x0e8c
            if (r21 != 0) goto L_0x0e7c
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x0e7c
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0e7c
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r4 = 0
            r1[r4] = r12
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r13)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r4] = r5
            goto L_0x1380
        L_0x0e7c:
            r4 = 0
            r0 = 2131624635(0x7f0e02bb, float:1.8876455E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r12
            java.lang.String r2 = "ChannelMessageVideo"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0e8c:
            r4 = 0
            boolean r1 = r20.isVoice()
            if (r1 == 0) goto L_0x0ea2
            r0 = 2131624620(0x7f0e02ac, float:1.8876425E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r12
            java.lang.String r2 = "ChannelMessageAudio"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0ea2:
            boolean r1 = r20.isRoundVideo()
            if (r1 == 0) goto L_0x0eb7
            r0 = 2131624632(0x7f0e02b8, float:1.887645E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r12
            java.lang.String r2 = "ChannelMessageRound"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0eb7:
            boolean r1 = r20.isMusic()
            if (r1 == 0) goto L_0x0ecc
            r0 = 2131624627(0x7f0e02b3, float:1.887644E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r4] = r12
            java.lang.String r2 = "ChannelMessageMusic"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0ecc:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            boolean r6 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r6 == 0) goto L_0x0ef0
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r1
            r0 = 2131624621(0x7f0e02ad, float:1.8876427E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r2[r4] = r12
            java.lang.String r3 = r1.first_name
            java.lang.String r1 = r1.last_name
            java.lang.String r1 = org.telegram.messenger.ContactsController.formatName(r3, r1)
            r2[r5] = r1
            java.lang.String r1 = "ChannelMessageContact2"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            goto L_0x1380
        L_0x0ef0:
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r4 == 0) goto L_0x0var_
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1
            org.telegram.tgnet.TLRPC$Poll r0 = r1.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x0var_
            r1 = 2131624631(0x7f0e02b7, float:1.8876447E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r3 = 0
            r2[r3] = r12
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "ChannelMessageQuiz2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0var_:
            r2 = 2
            r3 = 0
            r1 = 2131624630(0x7f0e02b6, float:1.8876445E38)
            java.lang.Object[] r2 = new java.lang.Object[r2]
            r2[r3] = r12
            java.lang.String r0 = r0.question
            r2[r5] = r0
            java.lang.String r0 = "ChannelMessagePoll2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x0var_:
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r4 != 0) goto L_0x1042
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r4 == 0) goto L_0x0var_
            goto L_0x1042
        L_0x0var_:
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r4 == 0) goto L_0x0var_
            r0 = 2131624625(0x7f0e02b1, float:1.8876435E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageLiveLocation"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0var_:
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r1 == 0) goto L_0x1014
            boolean r1 = r20.isSticker()
            if (r1 != 0) goto L_0x0fec
            boolean r1 = r20.isAnimatedSticker()
            if (r1 == 0) goto L_0x0var_
            goto L_0x0fec
        L_0x0var_:
            boolean r1 = r20.isGif()
            if (r1 == 0) goto L_0x0fa4
            if (r21 != 0) goto L_0x0var_
            int r1 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r1 < r3) goto L_0x0var_
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0var_
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r3 = 0
            r1[r3] = r12
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r2)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r3] = r5
            goto L_0x1380
        L_0x0var_:
            r3 = 0
            r0 = 2131624624(0x7f0e02b0, float:1.8876433E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r3] = r12
            java.lang.String r2 = "ChannelMessageGIF"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0fa4:
            if (r21 != 0) goto L_0x0fdc
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x0fdc
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0fdc
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r3)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x0fdc:
            r2 = 0
            r0 = 2131624622(0x7f0e02ae, float:1.8876429E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageDocument"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x0fec:
            r2 = 0
            java.lang.String r0 = r20.getStickerEmoji()
            if (r0 == 0) goto L_0x1005
            r1 = 2131624634(0x7f0e02ba, float:1.8876453E38)
            r3 = 2
            java.lang.Object[] r3 = new java.lang.Object[r3]
            r3[r2] = r12
            r3[r5] = r0
            java.lang.String r0 = "ChannelMessageStickerEmoji"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r3)
            goto L_0x040d
        L_0x1005:
            r0 = 2131624633(0x7f0e02b9, float:1.8876451E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x1014:
            r2 = 0
            if (r21 != 0) goto L_0x1033
            java.lang.CharSequence r1 = r0.messageText
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x1033
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r1[r2] = r12
            java.lang.CharSequence r0 = r0.messageText
            r1[r5] = r0
            r0 = 2131626071(0x7f0e0857, float:1.8879368E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r22[r2] = r5
            goto L_0x1380
        L_0x1033:
            r0 = 2131624628(0x7f0e02b4, float:1.8876441E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageNoText"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1042:
            r2 = 0
            r0 = 2131624626(0x7f0e02b2, float:1.8876437E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageMap"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1052:
            boolean r1 = r20.isMediaEmpty()
            r4 = 2131626054(0x7f0e0846, float:1.8879333E38)
            java.lang.String r7 = "NotificationMessageGroupText"
            if (r1 == 0) goto L_0x1092
            if (r21 != 0) goto L_0x107f
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x107f
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r3 = 2
            r1[r3] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x107f:
            r2 = 0
            r3 = 2
            java.lang.Object[] r0 = new java.lang.Object[r3]
            r0[r2] = r12
            java.lang.String r1 = r10.title
            r0[r5] = r1
            r1 = 2131626047(0x7f0e083f, float:1.887932E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r11, r1, r0)
            goto L_0x1380
        L_0x1092:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r1.media
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r8 == 0) goto L_0x10e4
            if (r21 != 0) goto L_0x10cf
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r2 < r3) goto L_0x10cf
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x10cf
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r15)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r2 = 2
            r1[r2] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x10cf:
            r2 = 2
            r0 = 2131626048(0x7f0e0840, float:1.8879321E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupPhoto"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x10e4:
            boolean r1 = r20.isVideo()
            if (r1 == 0) goto L_0x1136
            if (r21 != 0) goto L_0x1121
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x1121
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x1121
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r13)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r8 = 2
            r1[r8] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x1121:
            r8 = 2
            r0 = 2131626055(0x7f0e0847, float:1.8879335E38)
            java.lang.Object[] r1 = new java.lang.Object[r8]
            r9 = 0
            r1[r9] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = " "
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1136:
            r8 = 2
            r9 = 0
            boolean r1 = r20.isVoice()
            if (r1 == 0) goto L_0x1151
            r0 = 2131626037(0x7f0e0835, float:1.8879299E38)
            java.lang.Object[] r1 = new java.lang.Object[r8]
            r1[r9] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupAudio"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1151:
            boolean r1 = r20.isRoundVideo()
            if (r1 == 0) goto L_0x116a
            r0 = 2131626051(0x7f0e0843, float:1.8879327E38)
            java.lang.Object[] r1 = new java.lang.Object[r8]
            r1[r9] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupRound"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x116a:
            boolean r1 = r20.isMusic()
            if (r1 == 0) goto L_0x1183
            r0 = 2131626046(0x7f0e083e, float:1.8879317E38)
            java.lang.Object[] r1 = new java.lang.Object[r8]
            r1[r9] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupMusic"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1183:
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r8 == 0) goto L_0x11ac
            org.telegram.tgnet.TLRPC$TL_messageMediaContact r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaContact) r1
            r0 = 2131626038(0x7f0e0836, float:1.88793E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r3 = r1.first_name
            java.lang.String r1 = r1.last_name
            java.lang.String r1 = org.telegram.messenger.ContactsController.formatName(r3, r1)
            r3 = 2
            r2[r3] = r1
            java.lang.String r1 = "NotificationMessageGroupContact2"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            goto L_0x1380
        L_0x11ac:
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r8 == 0) goto L_0x11ea
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1
            org.telegram.tgnet.TLRPC$Poll r0 = r1.poll
            boolean r1 = r0.quiz
            if (r1 == 0) goto L_0x11d1
            r1 = 2131626050(0x7f0e0842, float:1.8879325E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r0 = r0.question
            r4 = 2
            r2[r4] = r0
            java.lang.String r0 = "NotificationMessageGroupQuiz2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x11d1:
            r3 = 0
            r4 = 2
            r1 = 2131626049(0x7f0e0841, float:1.8879323E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            java.lang.String r0 = r0.question
            r2[r4] = r0
            java.lang.String r0 = "NotificationMessageGroupPoll2"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            goto L_0x040d
        L_0x11ea:
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r8 == 0) goto L_0x1209
            r0 = 2131626040(0x7f0e0838, float:1.8879305E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r3 = 0
            r2[r3] = r12
            java.lang.String r3 = r10.title
            r2[r5] = r3
            org.telegram.tgnet.TLRPC$TL_game r1 = r1.game
            java.lang.String r1 = r1.title
            r3 = 2
            r2[r3] = r1
            java.lang.String r1 = "NotificationMessageGroupGame"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            goto L_0x1380
        L_0x1209:
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r8 != 0) goto L_0x133c
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r8 == 0) goto L_0x1213
            goto L_0x133c
        L_0x1213:
            boolean r8 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r8 == 0) goto L_0x122c
            r0 = 2131626044(0x7f0e083c, float:1.8879313E38)
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupLiveLocation"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x122c:
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r1 == 0) goto L_0x130d
            boolean r1 = r20.isSticker()
            if (r1 != 0) goto L_0x12dc
            boolean r1 = r20.isAnimatedSticker()
            if (r1 == 0) goto L_0x123e
            goto L_0x12dc
        L_0x123e:
            boolean r1 = r20.isGif()
            if (r1 == 0) goto L_0x1290
            if (r21 != 0) goto L_0x127b
            int r1 = android.os.Build.VERSION.SDK_INT
            r3 = 19
            if (r1 < r3) goto L_0x127b
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x127b
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r3 = 0
            r1[r3] = r12
            java.lang.String r3 = r10.title
            r1[r5] = r3
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r2)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            r2 = 2
            r1[r2] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x127b:
            r2 = 2
            r0 = 2131626042(0x7f0e083a, float:1.887931E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupGif"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1290:
            if (r21 != 0) goto L_0x12c7
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 19
            if (r1 < r2) goto L_0x12c7
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            java.lang.String r1 = r1.message
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x12c7
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r3)
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            java.lang.String r0 = r0.message
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r2 = 2
            r1[r2] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x12c7:
            r2 = 2
            r0 = 2131626039(0x7f0e0837, float:1.8879303E38)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupDocument"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x12dc:
            r2 = 0
            java.lang.String r0 = r20.getStickerEmoji()
            if (r0 == 0) goto L_0x12f9
            r1 = 2131626053(0x7f0e0845, float:1.8879331E38)
            java.lang.Object[] r3 = new java.lang.Object[r6]
            r3[r2] = r12
            java.lang.String r2 = r10.title
            r3[r5] = r2
            r4 = 2
            r3[r4] = r0
            java.lang.String r0 = "NotificationMessageGroupStickerEmoji"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r3)
            goto L_0x040d
        L_0x12f9:
            r4 = 2
            r0 = 2131626052(0x7f0e0844, float:1.887933E38)
            java.lang.Object[] r1 = new java.lang.Object[r4]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x040d
        L_0x130d:
            if (r21 != 0) goto L_0x132a
            java.lang.CharSequence r1 = r0.messageText
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x132a
            java.lang.Object[] r1 = new java.lang.Object[r6]
            r2 = 0
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.CharSequence r0 = r0.messageText
            r3 = 2
            r1[r3] = r0
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r7, r4, r1)
            goto L_0x1380
        L_0x132a:
            r2 = 0
            r3 = 2
            java.lang.Object[] r0 = new java.lang.Object[r3]
            r0[r2] = r12
            java.lang.String r1 = r10.title
            r0[r5] = r1
            r1 = 2131626047(0x7f0e083f, float:1.887932E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r11, r1, r0)
            goto L_0x1380
        L_0x133c:
            r2 = 0
            r3 = 2
            r0 = 2131626045(0x7f0e083d, float:1.8879315E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r2] = r12
            java.lang.String r2 = r10.title
            r1[r5] = r2
            java.lang.String r2 = "NotificationMessageGroupMap"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x1350:
            r2 = 0
            if (r23 == 0) goto L_0x1355
            r23[r2] = r2
        L_0x1355:
            boolean r0 = org.telegram.messenger.ChatObject.isChannel(r10)
            if (r0 == 0) goto L_0x136d
            boolean r0 = r10.megagroup
            if (r0 != 0) goto L_0x136d
            r0 = 2131624628(0x7f0e02b4, float:1.8876441E38)
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r2] = r12
            java.lang.String r2 = "ChannelMessageNoText"
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            goto L_0x1380
        L_0x136d:
            r0 = 2
            java.lang.Object[] r0 = new java.lang.Object[r0]
            r0[r2] = r12
            java.lang.String r1 = r10.title
            r0[r5] = r1
            r1 = 2131626047(0x7f0e083f, float:1.887932E38)
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatString(r11, r1, r0)
            goto L_0x1380
        L_0x137e:
            r1 = 0
        L_0x137f:
            r15 = r1
        L_0x1380:
            return r15
        L_0x1381:
            r0 = 2131627476(0x7f0e0dd4, float:1.8882218E38)
            java.lang.String r1 = "YouHaveNewMessage"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.getStringForMessage(org.telegram.messenger.MessageObject, boolean, boolean[], boolean[]):java.lang.String");
    }

    private void scheduleNotificationRepeat() {
        try {
            Intent intent = new Intent(ApplicationLoader.applicationContext, NotificationRepeat.class);
            intent.putExtra("currentAccount", this.currentAccount);
            PendingIntent service = PendingIntent.getService(ApplicationLoader.applicationContext, 0, intent, 0);
            int i = getAccountInstance().getNotificationsSettings().getInt("repeat_messages", 60);
            if (i <= 0 || this.personal_count <= 0) {
                this.alarmManager.cancel(service);
            } else {
                this.alarmManager.set(2, SystemClock.elapsedRealtime() + ((long) (i * 60 * 1000)), service);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        r3 = r3.action;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isPersonalMessage(org.telegram.messenger.MessageObject r3) {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r3 = r3.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r3.to_id
            if (r0 == 0) goto L_0x0018
            int r1 = r0.chat_id
            if (r1 != 0) goto L_0x0018
            int r0 = r0.channel_id
            if (r0 != 0) goto L_0x0018
            org.telegram.tgnet.TLRPC$MessageAction r3 = r3.action
            if (r3 == 0) goto L_0x0016
            boolean r3 = r3 instanceof org.telegram.tgnet.TLRPC$TL_messageActionEmpty
            if (r3 == 0) goto L_0x0018
        L_0x0016:
            r3 = 1
            goto L_0x0019
        L_0x0018:
            r3 = 0
        L_0x0019:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.isPersonalMessage(org.telegram.messenger.MessageObject):boolean");
    }

    private int getNotifyOverride(SharedPreferences sharedPreferences, long j) {
        int i = sharedPreferences.getInt("notify2_" + j, -1);
        if (i != 3) {
            return i;
        }
        if (sharedPreferences.getInt("notifyuntil_" + j, 0) >= getConnectionsManager().getCurrentTime()) {
            return 2;
        }
        return i;
    }

    public /* synthetic */ void lambda$showNotifications$24$NotificationsController() {
        showOrUpdateNotification(false);
    }

    public void showNotifications() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$showNotifications$24$NotificationsController();
            }
        });
    }

    public void hideNotifications() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$hideNotifications$25$NotificationsController();
            }
        });
    }

    public /* synthetic */ void lambda$hideNotifications$25$NotificationsController() {
        notificationManager.cancel(this.notificationId);
        this.lastWearNotifiedMessageId.clear();
        for (int i = 0; i < this.wearNotificationsIds.size(); i++) {
            notificationManager.cancel(this.wearNotificationsIds.valueAt(i).intValue());
        }
        this.wearNotificationsIds.clear();
    }

    private void dismissNotification() {
        try {
            notificationManager.cancel(this.notificationId);
            this.pushMessages.clear();
            this.pushMessagesDict.clear();
            this.lastWearNotifiedMessageId.clear();
            for (int i = 0; i < this.wearNotificationsIds.size(); i++) {
                if (!this.openedInBubbleDialogs.contains(Long.valueOf(this.wearNotificationsIds.keyAt(i)))) {
                    notificationManager.cancel(this.wearNotificationsIds.valueAt(i).intValue());
                }
            }
            this.wearNotificationsIds.clear();
            AndroidUtilities.runOnUIThread($$Lambda$NotificationsController$Iii6Ysd4L9akcd1WhGl6DiaJBA.INSTANCE);
            if (WearDataLayerListenerService.isWatchConnected()) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("id", getUserConfig().getClientUserId());
                    jSONObject.put("cancel_all", true);
                    WearDataLayerListenerService.sendMessageToWatch("/notify", jSONObject.toString().getBytes(), "remote_notifications");
                } catch (JSONException unused) {
                }
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    private void playInChatSound() {
        if (this.inChatSoundEnabled && !MediaController.getInstance().isRecordingAudio()) {
            try {
                if (audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            try {
                if (getNotifyOverride(getAccountInstance().getNotificationsSettings(), this.opened_dialog_id) != 2) {
                    notificationsQueue.postRunnable(new Runnable() {
                        public final void run() {
                            NotificationsController.this.lambda$playInChatSound$28$NotificationsController();
                        }
                    });
                }
            } catch (Exception e2) {
                FileLog.e((Throwable) e2);
            }
        }
    }

    public /* synthetic */ void lambda$playInChatSound$28$NotificationsController() {
        if (Math.abs(System.currentTimeMillis() - this.lastSoundPlay) > 500) {
            try {
                if (this.soundPool == null) {
                    SoundPool soundPool2 = new SoundPool(3, 1, 0);
                    this.soundPool = soundPool2;
                    soundPool2.setOnLoadCompleteListener($$Lambda$NotificationsController$KifNbzrscru9TRdUtK9fbMo4ilE.INSTANCE);
                }
                if (this.soundIn == 0 && !this.soundInLoaded) {
                    this.soundInLoaded = true;
                    this.soundIn = this.soundPool.load(ApplicationLoader.applicationContext, NUM, 1);
                }
                if (this.soundIn != 0) {
                    try {
                        this.soundPool.play(this.soundIn, 1.0f, 1.0f, 1, 0, 1.0f);
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
            } catch (Exception e2) {
                FileLog.e((Throwable) e2);
            }
        }
    }

    static /* synthetic */ void lambda$null$27(SoundPool soundPool2, int i, int i2) {
        if (i2 == 0) {
            try {
                soundPool2.play(i, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    private void scheduleNotificationDelay(boolean z) {
        try {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("delay notification start, onlineReason = " + z);
            }
            this.notificationDelayWakelock.acquire(10000);
            notificationsQueue.cancelRunnable(this.notificationDelayRunnable);
            notificationsQueue.postRunnable(this.notificationDelayRunnable, (long) (z ? 3000 : 1000));
        } catch (Exception e) {
            FileLog.e((Throwable) e);
            showOrUpdateNotification(this.notifyCheck);
        }
    }

    /* access modifiers changed from: protected */
    public void repeatNotificationMaybe() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$repeatNotificationMaybe$29$NotificationsController();
            }
        });
    }

    public /* synthetic */ void lambda$repeatNotificationMaybe$29$NotificationsController() {
        int i = Calendar.getInstance().get(11);
        if (i < 11 || i > 22) {
            scheduleNotificationRepeat();
            return;
        }
        notificationManager.cancel(this.notificationId);
        showOrUpdateNotification(true);
    }

    private boolean isEmptyVibration(long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            return false;
        }
        for (long j : jArr) {
            if (j != 0) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(26)
    public void deleteNotificationChannel(long j) {
        notificationsQueue.postRunnable(new Runnable(j) {
            public final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NotificationsController.this.lambda$deleteNotificationChannel$30$NotificationsController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$deleteNotificationChannel$30$NotificationsController(long j) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
                String str = "org.telegram.key" + j;
                String string = notificationsSettings.getString(str, (String) null);
                if (string != null) {
                    notificationsSettings.edit().remove(str).remove(str + "_s").commit();
                    systemNotificationManager.deleteNotificationChannel(string);
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    @TargetApi(26)
    public void deleteAllNotificationChannels() {
        notificationsQueue.postRunnable(new Runnable() {
            public final void run() {
                NotificationsController.this.lambda$deleteAllNotificationChannels$31$NotificationsController();
            }
        });
    }

    public /* synthetic */ void lambda$deleteAllNotificationChannels$31$NotificationsController() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
                Map<String, ?> all = notificationsSettings.getAll();
                SharedPreferences.Editor edit = notificationsSettings.edit();
                for (Map.Entry next : all.entrySet()) {
                    String str = (String) next.getKey();
                    if (str.startsWith("org.telegram.key")) {
                        if (!str.endsWith("_s")) {
                            systemNotificationManager.deleteNotificationChannel((String) next.getValue());
                        }
                        edit.remove(str);
                    }
                }
                edit.commit();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    @SuppressLint({"RestrictedApi"})
    private void createNotificationShortcut(NotificationCompat.Builder builder, int i, String str, TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, Person person) {
        String str2;
        if (Build.VERSION.SDK_INT < 29) {
            return;
        }
        if ((!ChatObject.isChannel(tLRPC$Chat) || tLRPC$Chat.megagroup) && SharedConfig.chatBubbles) {
            try {
                String str3 = "ndid_" + i;
                ShortcutInfoCompat.Builder builder2 = new ShortcutInfoCompat.Builder(ApplicationLoader.applicationContext, str3);
                if (tLRPC$Chat != null) {
                    str2 = str;
                } else {
                    str2 = UserObject.getFirstName(tLRPC$User);
                }
                builder2.setShortLabel(str2);
                builder2.setLongLabel(str);
                builder2.setIntent(new Intent("android.intent.action.VIEW"));
                builder2.setLongLived(true);
                Bitmap bitmap = null;
                if (person != null) {
                    builder2.setPerson(person);
                    builder2.setIcon(person.getIcon());
                    if (person.getIcon() != null) {
                        bitmap = person.getIcon().getBitmap();
                    }
                }
                ArrayList arrayList = new ArrayList(1);
                arrayList.add(builder2.build());
                ArrayList arrayList2 = new ArrayList(1);
                arrayList2.add(str3);
                ShortcutManagerCompat.addDynamicShortcuts(ApplicationLoader.applicationContext, arrayList);
                ShortcutManagerCompat.removeDynamicShortcuts(ApplicationLoader.applicationContext, arrayList2);
                builder.setShortcutId(str3);
                NotificationCompat.BubbleMetadata.Builder builder3 = new NotificationCompat.BubbleMetadata.Builder();
                Intent intent = new Intent(ApplicationLoader.applicationContext, BubbleActivity.class);
                intent.setAction("com.tmessages.openchat" + Math.random() + Integer.MAX_VALUE);
                if (i > 0) {
                    intent.putExtra("userId", i);
                } else {
                    intent.putExtra("chatId", -i);
                }
                intent.putExtra("currentAccount", this.currentAccount);
                builder3.setIntent(PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, NUM));
                builder3.setSuppressNotification(true);
                builder3.setAutoExpandBubble(false);
                builder3.setDesiredHeight(AndroidUtilities.dp(640.0f));
                if (bitmap != null) {
                    builder3.setIcon(IconCompat.createWithAdaptiveBitmap(bitmap));
                } else if (tLRPC$User != null) {
                    builder3.setIcon(IconCompat.createWithResource(ApplicationLoader.applicationContext, tLRPC$User.bot ? NUM : NUM));
                } else {
                    builder3.setIcon(IconCompat.createWithResource(ApplicationLoader.applicationContext, NUM));
                }
                builder.setBubbleMetadata(builder3.build());
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    @TargetApi(26)
    private String validateChannelId(long j, String str, long[] jArr, int i, Uri uri, int i2, long[] jArr2, Uri uri2, int i3) {
        long j2 = j;
        long[] jArr3 = jArr;
        int i4 = i;
        Uri uri3 = uri;
        int i5 = i2;
        SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
        String str2 = "org.telegram.key" + j2;
        String string = notificationsSettings.getString(str2, (String) null);
        String string2 = notificationsSettings.getString(str2 + "_s", (String) null);
        StringBuilder sb = new StringBuilder();
        boolean z = ((int) j2) == 0;
        int i6 = 0;
        while (i6 < jArr3.length) {
            sb.append(jArr3[i6]);
            i6++;
            str2 = str2;
        }
        String str3 = str2;
        sb.append(i4);
        if (uri3 != null) {
            sb.append(uri.toString());
        }
        sb.append(i5);
        if (z) {
            sb.append("secret");
        }
        String MD5 = Utilities.MD5(sb.toString());
        if (string != null && !string2.equals(MD5)) {
            systemNotificationManager.deleteNotificationChannel(string);
            string = null;
        }
        if (string == null) {
            string = this.currentAccount + "channel" + j2 + "_" + Utilities.random.nextLong();
            NotificationChannel notificationChannel = new NotificationChannel(string, z ? LocaleController.getString("SecretChatName", NUM) : str, i5);
            if (i4 != 0) {
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(i4);
            }
            if (!isEmptyVibration(jArr3)) {
                notificationChannel.enableVibration(true);
                if (jArr3 != null && jArr3.length > 0) {
                    notificationChannel.setVibrationPattern(jArr3);
                }
            } else {
                notificationChannel.enableVibration(false);
            }
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setContentType(4);
            builder.setUsage(5);
            if (uri3 != null) {
                notificationChannel.setSound(uri3, builder.build());
            } else {
                notificationChannel.setSound((Uri) null, builder.build());
            }
            systemNotificationManager.createNotificationChannel(notificationChannel);
            String str4 = str3;
            notificationsSettings.edit().putString(str4, string).putString(str4 + "_s", MD5).commit();
        }
        return string;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v17, resolved type: android.net.Uri} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v9, resolved type: java.lang.Object} */
    /* JADX WARNING: type inference failed for: r3v63 */
    /* JADX WARNING: type inference failed for: r3v64 */
    /* JADX WARNING: type inference failed for: r3v91 */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:450|451|452|453) */
    /* JADX WARNING: Code restructure failed: missing block: B:418:0x0877, code lost:
        if (android.os.Build.VERSION.SDK_INT >= 26) goto L_0x0879;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:452:0x092a */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x022c A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x028d A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x02d3 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x02ff A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0300 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0305 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0309 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0320  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0335  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x035a  */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x03d1 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x03d8 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x03dd A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x040f  */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x04c1 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x04fd A[ADDED_TO_REGION, Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0510 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x0513 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x051d A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x0532 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0533 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x0563 A[SYNTHETIC, Splitter:B:307:0x0563] */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x0597 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x05a3 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x05bd A[SYNTHETIC, Splitter:B:323:0x05bd] */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x05d5 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0677 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x06f0 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x07d9 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0823  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x082c  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x086f A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x0880 A[ADDED_TO_REGION, Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:457:0x093e A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:458:0x0948 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:461:0x094f A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:488:0x09cc A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:502:0x0a89 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:512:0x0aba A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:513:0x0ad3 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:516:0x0af1 A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:517:0x0b0c A[Catch:{ Exception -> 0x0b18 }] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01cf A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0221 A[Catch:{ Exception -> 0x0b1a }] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showOrUpdateNotification(boolean r47) {
        /*
            r46 = this;
            r12 = r46
            r13 = r47
            java.lang.String r1 = "color_"
            java.lang.String r2 = "currentAccount"
            org.telegram.messenger.UserConfig r3 = r46.getUserConfig()
            boolean r3 = r3.isClientActivated()
            if (r3 == 0) goto L_0x0b25
            java.util.ArrayList<org.telegram.messenger.MessageObject> r3 = r12.pushMessages
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x0b25
            boolean r3 = org.telegram.messenger.SharedConfig.showNotificationsForAllAccounts
            if (r3 != 0) goto L_0x0026
            int r3 = r12.currentAccount
            int r4 = org.telegram.messenger.UserConfig.selectedAccount
            if (r3 == r4) goto L_0x0026
            goto L_0x0b25
        L_0x0026:
            org.telegram.tgnet.ConnectionsManager r3 = r46.getConnectionsManager()     // Catch:{ Exception -> 0x0b1e }
            r3.resumeNetworkMaybe()     // Catch:{ Exception -> 0x0b1e }
            java.util.ArrayList<org.telegram.messenger.MessageObject> r3 = r12.pushMessages     // Catch:{ Exception -> 0x0b1e }
            r4 = 0
            java.lang.Object r3 = r3.get(r4)     // Catch:{ Exception -> 0x0b1e }
            org.telegram.messenger.MessageObject r3 = (org.telegram.messenger.MessageObject) r3     // Catch:{ Exception -> 0x0b1e }
            org.telegram.messenger.AccountInstance r5 = r46.getAccountInstance()     // Catch:{ Exception -> 0x0b1e }
            android.content.SharedPreferences r5 = r5.getNotificationsSettings()     // Catch:{ Exception -> 0x0b1e }
            java.lang.String r6 = "dismissDate"
            int r6 = r5.getInt(r6, r4)     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Message r7 = r3.messageOwner     // Catch:{ Exception -> 0x0b1e }
            int r7 = r7.date     // Catch:{ Exception -> 0x0b1e }
            if (r7 > r6) goto L_0x0053
            r46.dismissNotification()     // Catch:{ Exception -> 0x004e }
            return
        L_0x004e:
            r0 = move-exception
            r1 = r0
            r13 = r12
            goto L_0x0b21
        L_0x0053:
            long r7 = r3.getDialogId()     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Message r9 = r3.messageOwner     // Catch:{ Exception -> 0x0b1e }
            boolean r9 = r9.mentioned     // Catch:{ Exception -> 0x0b1e }
            if (r9 == 0) goto L_0x0063
            org.telegram.tgnet.TLRPC$Message r9 = r3.messageOwner     // Catch:{ Exception -> 0x004e }
            int r9 = r9.from_id     // Catch:{ Exception -> 0x004e }
            long r9 = (long) r9
            goto L_0x0064
        L_0x0063:
            r9 = r7
        L_0x0064:
            r3.getId()     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Message r11 = r3.messageOwner     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Peer r11 = r11.to_id     // Catch:{ Exception -> 0x0b1e }
            int r11 = r11.chat_id     // Catch:{ Exception -> 0x0b1e }
            if (r11 == 0) goto L_0x0076
            org.telegram.tgnet.TLRPC$Message r11 = r3.messageOwner     // Catch:{ Exception -> 0x004e }
            org.telegram.tgnet.TLRPC$Peer r11 = r11.to_id     // Catch:{ Exception -> 0x004e }
            int r11 = r11.chat_id     // Catch:{ Exception -> 0x004e }
            goto L_0x007c
        L_0x0076:
            org.telegram.tgnet.TLRPC$Message r11 = r3.messageOwner     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Peer r11 = r11.to_id     // Catch:{ Exception -> 0x0b1e }
            int r11 = r11.channel_id     // Catch:{ Exception -> 0x0b1e }
        L_0x007c:
            org.telegram.tgnet.TLRPC$Message r14 = r3.messageOwner     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$Peer r14 = r14.to_id     // Catch:{ Exception -> 0x0b1e }
            int r14 = r14.user_id     // Catch:{ Exception -> 0x0b1e }
            if (r14 != 0) goto L_0x0089
            org.telegram.tgnet.TLRPC$Message r14 = r3.messageOwner     // Catch:{ Exception -> 0x004e }
            int r14 = r14.from_id     // Catch:{ Exception -> 0x004e }
            goto L_0x0097
        L_0x0089:
            org.telegram.messenger.UserConfig r15 = r46.getUserConfig()     // Catch:{ Exception -> 0x0b1e }
            int r15 = r15.getClientUserId()     // Catch:{ Exception -> 0x0b1e }
            if (r14 != r15) goto L_0x0097
            org.telegram.tgnet.TLRPC$Message r14 = r3.messageOwner     // Catch:{ Exception -> 0x004e }
            int r14 = r14.from_id     // Catch:{ Exception -> 0x004e }
        L_0x0097:
            org.telegram.messenger.MessagesController r15 = r46.getMessagesController()     // Catch:{ Exception -> 0x0b1e }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r14)     // Catch:{ Exception -> 0x0b1e }
            org.telegram.tgnet.TLRPC$User r4 = r15.getUser(r4)     // Catch:{ Exception -> 0x0b1e }
            if (r11 == 0) goto L_0x00ce
            org.telegram.messenger.MessagesController r15 = r46.getMessagesController()     // Catch:{ Exception -> 0x004e }
            r18 = r6
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ Exception -> 0x004e }
            org.telegram.tgnet.TLRPC$Chat r6 = r15.getChat(r6)     // Catch:{ Exception -> 0x004e }
            if (r6 != 0) goto L_0x00be
            boolean r15 = r3.isFcmMessage()     // Catch:{ Exception -> 0x004e }
            if (r15 == 0) goto L_0x00be
            boolean r15 = r3.localChannel     // Catch:{ Exception -> 0x004e }
            goto L_0x00cb
        L_0x00be:
            boolean r15 = org.telegram.messenger.ChatObject.isChannel(r6)     // Catch:{ Exception -> 0x004e }
            if (r15 == 0) goto L_0x00ca
            boolean r15 = r6.megagroup     // Catch:{ Exception -> 0x004e }
            if (r15 != 0) goto L_0x00ca
            r15 = 1
            goto L_0x00cb
        L_0x00ca:
            r15 = 0
        L_0x00cb:
            r19 = r3
            goto L_0x00d4
        L_0x00ce:
            r18 = r6
            r19 = r3
            r6 = 0
            r15 = 0
        L_0x00d4:
            int r3 = r12.getNotifyOverride(r5, r9)     // Catch:{ Exception -> 0x0b1e }
            r20 = r2
            r2 = -1
            r21 = r4
            r4 = 2
            if (r3 != r2) goto L_0x00e9
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r15)     // Catch:{ Exception -> 0x004e }
            boolean r3 = r12.isGlobalNotificationsEnabled(r7, r3)     // Catch:{ Exception -> 0x004e }
            goto L_0x00ee
        L_0x00e9:
            if (r3 == r4) goto L_0x00ed
            r3 = 1
            goto L_0x00ee
        L_0x00ed:
            r3 = 0
        L_0x00ee:
            if (r13 == 0) goto L_0x00f5
            if (r3 != 0) goto L_0x00f3
            goto L_0x00f5
        L_0x00f3:
            r3 = 0
            goto L_0x00f6
        L_0x00f5:
            r3 = 1
        L_0x00f6:
            java.lang.String r2 = "custom_"
            r23 = 1000(0x3e8, double:4.94E-321)
            if (r3 != 0) goto L_0x0197
            int r25 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r25 != 0) goto L_0x0197
            if (r6 == 0) goto L_0x0197
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r9.<init>()     // Catch:{ Exception -> 0x0b1a }
            r9.append(r2)     // Catch:{ Exception -> 0x0b1a }
            r9.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0b1a }
            r10 = 0
            boolean r9 = r5.getBoolean(r9, r10)     // Catch:{ Exception -> 0x0b1a }
            if (r9 == 0) goto L_0x0145
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x004e }
            r9.<init>()     // Catch:{ Exception -> 0x004e }
            java.lang.String r10 = "smart_max_count_"
            r9.append(r10)     // Catch:{ Exception -> 0x004e }
            r9.append(r7)     // Catch:{ Exception -> 0x004e }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x004e }
            int r9 = r5.getInt(r9, r4)     // Catch:{ Exception -> 0x004e }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x004e }
            r10.<init>()     // Catch:{ Exception -> 0x004e }
            java.lang.String r4 = "smart_delay_"
            r10.append(r4)     // Catch:{ Exception -> 0x004e }
            r10.append(r7)     // Catch:{ Exception -> 0x004e }
            java.lang.String r4 = r10.toString()     // Catch:{ Exception -> 0x004e }
            r10 = 180(0xb4, float:2.52E-43)
            int r10 = r5.getInt(r4, r10)     // Catch:{ Exception -> 0x004e }
            goto L_0x0148
        L_0x0145:
            r10 = 180(0xb4, float:2.52E-43)
            r9 = 2
        L_0x0148:
            if (r9 == 0) goto L_0x0197
            android.util.LongSparseArray<android.graphics.Point> r4 = r12.smartNotificationsDialogs     // Catch:{ Exception -> 0x0b1a }
            java.lang.Object r4 = r4.get(r7)     // Catch:{ Exception -> 0x0b1a }
            android.graphics.Point r4 = (android.graphics.Point) r4     // Catch:{ Exception -> 0x0b1a }
            if (r4 != 0) goto L_0x0167
            android.graphics.Point r4 = new android.graphics.Point     // Catch:{ Exception -> 0x004e }
            long r9 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x004e }
            long r9 = r9 / r23
            int r10 = (int) r9     // Catch:{ Exception -> 0x004e }
            r9 = 1
            r4.<init>(r9, r10)     // Catch:{ Exception -> 0x004e }
            android.util.LongSparseArray<android.graphics.Point> r9 = r12.smartNotificationsDialogs     // Catch:{ Exception -> 0x004e }
            r9.put(r7, r4)     // Catch:{ Exception -> 0x004e }
            goto L_0x0197
        L_0x0167:
            r25 = r3
            int r3 = r4.y     // Catch:{ Exception -> 0x0b1a }
            int r3 = r3 + r10
            long r12 = (long) r3     // Catch:{ Exception -> 0x0b1a }
            long r26 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0b1a }
            long r26 = r26 / r23
            int r3 = (r12 > r26 ? 1 : (r12 == r26 ? 0 : -1))
            if (r3 >= 0) goto L_0x0183
            long r9 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0b1a }
            long r9 = r9 / r23
            int r3 = (int) r9     // Catch:{ Exception -> 0x0b1a }
            r9 = 1
            r4.set(r9, r3)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x0199
        L_0x0183:
            int r3 = r4.x     // Catch:{ Exception -> 0x0b1a }
            if (r3 >= r9) goto L_0x0194
            r9 = 1
            int r3 = r3 + r9
            long r9 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0b1a }
            long r9 = r9 / r23
            int r10 = (int) r9     // Catch:{ Exception -> 0x0b1a }
            r4.set(r3, r10)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x0199
        L_0x0194:
            r25 = 1
            goto L_0x0199
        L_0x0197:
            r25 = r3
        L_0x0199:
            android.net.Uri r3 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r3 = r3.getPath()     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r4 = "EnableInAppSounds"
            r9 = 1
            boolean r4 = r5.getBoolean(r4, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r10 = "EnableInAppVibrate"
            boolean r10 = r5.getBoolean(r10, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r12 = "EnableInAppPreview"
            boolean r12 = r5.getBoolean(r12, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = "EnableInAppPriority"
            r13 = 0
            boolean r9 = r5.getBoolean(r9, r13)     // Catch:{ Exception -> 0x0b1a }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r13.<init>()     // Catch:{ Exception -> 0x0b1a }
            r13.append(r2)     // Catch:{ Exception -> 0x0b1a }
            r13.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r2 = r13.toString()     // Catch:{ Exception -> 0x0b1a }
            r13 = 0
            boolean r2 = r5.getBoolean(r2, r13)     // Catch:{ Exception -> 0x0b1a }
            if (r2 == 0) goto L_0x0221
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r13.<init>()     // Catch:{ Exception -> 0x0b1a }
            r27 = r12
            java.lang.String r12 = "vibrate_"
            r13.append(r12)     // Catch:{ Exception -> 0x0b1a }
            r13.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r12 = r13.toString()     // Catch:{ Exception -> 0x0b1a }
            r13 = 0
            int r12 = r5.getInt(r12, r13)     // Catch:{ Exception -> 0x0b1a }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r13.<init>()     // Catch:{ Exception -> 0x0b1a }
            r28 = r12
            java.lang.String r12 = "priority_"
            r13.append(r12)     // Catch:{ Exception -> 0x0b1a }
            r13.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r12 = r13.toString()     // Catch:{ Exception -> 0x0b1a }
            r13 = 3
            int r12 = r5.getInt(r12, r13)     // Catch:{ Exception -> 0x0b1a }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r13.<init>()     // Catch:{ Exception -> 0x0b1a }
            r29 = r12
            java.lang.String r12 = "sound_path_"
            r13.append(r12)     // Catch:{ Exception -> 0x0b1a }
            r13.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r12 = r13.toString()     // Catch:{ Exception -> 0x0b1a }
            r13 = 0
            java.lang.String r12 = r5.getString(r12, r13)     // Catch:{ Exception -> 0x0b1a }
            r13 = r12
            r12 = r28
            r28 = r6
            r6 = r29
            r29 = r9
            goto L_0x022a
        L_0x0221:
            r27 = r12
            r28 = r6
            r29 = r9
            r6 = 3
            r12 = 0
            r13 = 0
        L_0x022a:
            if (r11 == 0) goto L_0x028d
            if (r15 == 0) goto L_0x025d
            if (r13 == 0) goto L_0x0238
            boolean r15 = r13.equals(r3)     // Catch:{ Exception -> 0x0b1a }
            if (r15 == 0) goto L_0x0238
            r13 = 0
            goto L_0x0240
        L_0x0238:
            if (r13 != 0) goto L_0x0240
            java.lang.String r13 = "ChannelSoundPath"
            java.lang.String r13 = r5.getString(r13, r3)     // Catch:{ Exception -> 0x0b1a }
        L_0x0240:
            java.lang.String r15 = "vibrate_channel"
            r9 = 0
            int r15 = r5.getInt(r15, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = "priority_channel"
            r30 = r13
            r13 = 1
            int r9 = r5.getInt(r9, r13)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r13 = "ChannelLed"
            r31 = r9
            r9 = -16776961(0xfffffffffvar_ff, float:-1.7014636E38)
            int r9 = r5.getInt(r13, r9)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x02d1
        L_0x025d:
            if (r13 == 0) goto L_0x0267
            boolean r9 = r13.equals(r3)     // Catch:{ Exception -> 0x0b1a }
            if (r9 == 0) goto L_0x0267
            r9 = 0
            goto L_0x0271
        L_0x0267:
            if (r13 != 0) goto L_0x0270
            java.lang.String r9 = "GroupSoundPath"
            java.lang.String r9 = r5.getString(r9, r3)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x0271
        L_0x0270:
            r9 = r13
        L_0x0271:
            java.lang.String r13 = "vibrate_group"
            r15 = 0
            int r13 = r5.getInt(r13, r15)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r15 = "priority_group"
            r30 = r9
            r9 = 1
            int r15 = r5.getInt(r15, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = "GroupLed"
            r31 = r13
            r13 = -16776961(0xfffffffffvar_ff, float:-1.7014636E38)
            int r9 = r5.getInt(r9, r13)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x02be
        L_0x028d:
            if (r14 == 0) goto L_0x02c5
            if (r13 == 0) goto L_0x0299
            boolean r9 = r13.equals(r3)     // Catch:{ Exception -> 0x0b1a }
            if (r9 == 0) goto L_0x0299
            r9 = 0
            goto L_0x02a3
        L_0x0299:
            if (r13 != 0) goto L_0x02a2
            java.lang.String r9 = "GlobalSoundPath"
            java.lang.String r9 = r5.getString(r9, r3)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x02a3
        L_0x02a2:
            r9 = r13
        L_0x02a3:
            java.lang.String r13 = "vibrate_messages"
            r15 = 0
            int r13 = r5.getInt(r13, r15)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r15 = "priority_messages"
            r30 = r9
            r9 = 1
            int r15 = r5.getInt(r15, r9)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = "MessagesLed"
            r31 = r13
            r13 = -16776961(0xfffffffffvar_ff, float:-1.7014636E38)
            int r9 = r5.getInt(r9, r13)     // Catch:{ Exception -> 0x0b1a }
        L_0x02be:
            r45 = r31
            r31 = r15
            r15 = r45
            goto L_0x02d1
        L_0x02c5:
            r9 = r13
            r13 = -16776961(0xfffffffffvar_ff, float:-1.7014636E38)
            r30 = r9
            r9 = -16776961(0xfffffffffvar_ff, float:-1.7014636E38)
            r15 = 0
            r31 = 0
        L_0x02d1:
            if (r2 == 0) goto L_0x02fc
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r2.<init>()     // Catch:{ Exception -> 0x0b1a }
            r2.append(r1)     // Catch:{ Exception -> 0x0b1a }
            r2.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b1a }
            boolean r2 = r5.contains(r2)     // Catch:{ Exception -> 0x0b1a }
            if (r2 == 0) goto L_0x02fc
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r2.<init>()     // Catch:{ Exception -> 0x0b1a }
            r2.append(r1)     // Catch:{ Exception -> 0x0b1a }
            r2.append(r7)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r1 = r2.toString()     // Catch:{ Exception -> 0x0b1a }
            r2 = 0
            int r9 = r5.getInt(r1, r2)     // Catch:{ Exception -> 0x0b1a }
        L_0x02fc:
            r1 = 3
            if (r6 == r1) goto L_0x0300
            goto L_0x0302
        L_0x0300:
            r6 = r31
        L_0x0302:
            r2 = 4
            if (r15 != r2) goto L_0x0309
            r5 = 1
            r13 = 2
            r15 = 0
            goto L_0x030b
        L_0x0309:
            r5 = 0
            r13 = 2
        L_0x030b:
            if (r15 != r13) goto L_0x0312
            r2 = 1
            if (r12 == r2) goto L_0x031b
            if (r12 == r1) goto L_0x031b
        L_0x0312:
            if (r15 == r13) goto L_0x0316
            if (r12 == r13) goto L_0x031b
        L_0x0316:
            if (r12 == 0) goto L_0x031c
            r1 = 4
            if (r12 == r1) goto L_0x031c
        L_0x031b:
            r15 = r12
        L_0x031c:
            boolean r1 = org.telegram.messenger.ApplicationLoader.mainInterfacePaused     // Catch:{ Exception -> 0x0b1a }
            if (r1 != 0) goto L_0x0335
            if (r4 != 0) goto L_0x0324
            r30 = 0
        L_0x0324:
            if (r10 != 0) goto L_0x0327
            r15 = 2
        L_0x0327:
            if (r29 != 0) goto L_0x032e
            r2 = r30
            r1 = 2
            r6 = 0
            goto L_0x0338
        L_0x032e:
            r1 = 2
            r2 = r30
            if (r6 != r1) goto L_0x0338
            r6 = 1
            goto L_0x0338
        L_0x0335:
            r1 = 2
            r2 = r30
        L_0x0338:
            if (r5 == 0) goto L_0x034e
            if (r15 == r1) goto L_0x034e
            android.media.AudioManager r1 = audioManager     // Catch:{ Exception -> 0x0349 }
            int r1 = r1.getRingerMode()     // Catch:{ Exception -> 0x0349 }
            if (r1 == 0) goto L_0x034e
            r4 = 1
            if (r1 == r4) goto L_0x034e
            r15 = 2
            goto L_0x034e
        L_0x0349:
            r0 = move-exception
            r1 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)     // Catch:{ Exception -> 0x0b1a }
        L_0x034e:
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r4 = "NoSound"
            r12 = 100
            r10 = 26
            r30 = 0
            if (r1 < r10) goto L_0x03d1
            r1 = 2
            if (r15 != r1) goto L_0x0367
            long[] r10 = new long[r1]     // Catch:{ Exception -> 0x0b1a }
            r1 = 0
            r10[r1] = r30     // Catch:{ Exception -> 0x0b1a }
            r1 = 1
            r10[r1] = r30     // Catch:{ Exception -> 0x0b1a }
            r5 = r10
            goto L_0x0391
        L_0x0367:
            r1 = 1
            if (r15 != r1) goto L_0x0379
            r10 = 4
            long[] r5 = new long[r10]     // Catch:{ Exception -> 0x0b1a }
            r10 = 0
            r5[r10] = r30     // Catch:{ Exception -> 0x0b1a }
            r5[r1] = r12     // Catch:{ Exception -> 0x0b1a }
            r1 = 2
            r5[r1] = r30     // Catch:{ Exception -> 0x0b1a }
            r1 = 3
            r5[r1] = r12     // Catch:{ Exception -> 0x0b1a }
            goto L_0x0391
        L_0x0379:
            if (r15 == 0) goto L_0x038e
            r1 = 4
            if (r15 != r1) goto L_0x037f
            goto L_0x038e
        L_0x037f:
            r1 = 3
            if (r15 != r1) goto L_0x038c
            r1 = 2
            long[] r5 = new long[r1]     // Catch:{ Exception -> 0x0b1a }
            r1 = 0
            r5[r1] = r30     // Catch:{ Exception -> 0x0b1a }
            r1 = 1
            r5[r1] = r23     // Catch:{ Exception -> 0x0b1a }
            goto L_0x0391
        L_0x038c:
            r5 = 0
            goto L_0x0391
        L_0x038e:
            r1 = 0
            long[] r5 = new long[r1]     // Catch:{ Exception -> 0x0b1a }
        L_0x0391:
            if (r2 == 0) goto L_0x03a7
            boolean r1 = r2.equals(r4)     // Catch:{ Exception -> 0x0b1a }
            if (r1 != 0) goto L_0x03a7
            boolean r1 = r2.equals(r3)     // Catch:{ Exception -> 0x0b1a }
            if (r1 == 0) goto L_0x03a2
            android.net.Uri r1 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI     // Catch:{ Exception -> 0x0b1a }
            goto L_0x03a8
        L_0x03a2:
            android.net.Uri r1 = android.net.Uri.parse(r2)     // Catch:{ Exception -> 0x0b1a }
            goto L_0x03a8
        L_0x03a7:
            r1 = 0
        L_0x03a8:
            if (r6 != 0) goto L_0x03b0
            r32 = r1
            r10 = r5
            r33 = 3
            goto L_0x03d6
        L_0x03b0:
            r10 = 1
            if (r6 == r10) goto L_0x03cb
            r10 = 2
            if (r6 != r10) goto L_0x03b7
            goto L_0x03cb
        L_0x03b7:
            r10 = 4
            if (r6 != r10) goto L_0x03c0
            r32 = r1
            r10 = r5
            r33 = 1
            goto L_0x03d6
        L_0x03c0:
            r10 = 5
            r32 = r1
            if (r6 != r10) goto L_0x03c9
            r10 = r5
            r33 = 2
            goto L_0x03d6
        L_0x03c9:
            r10 = r5
            goto L_0x03d4
        L_0x03cb:
            r32 = r1
            r10 = r5
            r33 = 4
            goto L_0x03d6
        L_0x03d1:
            r10 = 0
            r32 = 0
        L_0x03d4:
            r33 = 0
        L_0x03d6:
            if (r25 == 0) goto L_0x03dd
            r1 = 0
            r2 = 0
            r6 = 0
            r15 = 0
            goto L_0x03df
        L_0x03dd:
            r1 = r6
            r6 = r9
        L_0x03df:
            android.content.Intent r5 = new android.content.Intent     // Catch:{ Exception -> 0x0b1a }
            android.content.Context r9 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b1a }
            java.lang.Class<org.telegram.ui.LaunchActivity> r12 = org.telegram.ui.LaunchActivity.class
            r5.<init>(r9, r12)     // Catch:{ Exception -> 0x0b1a }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b1a }
            r9.<init>()     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r12 = "com.tmessages.openchat"
            r9.append(r12)     // Catch:{ Exception -> 0x0b1a }
            double r12 = java.lang.Math.random()     // Catch:{ Exception -> 0x0b1a }
            r9.append(r12)     // Catch:{ Exception -> 0x0b1a }
            r12 = 2147483647(0x7fffffff, float:NaN)
            r9.append(r12)     // Catch:{ Exception -> 0x0b1a }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0b1a }
            r5.setAction(r9)     // Catch:{ Exception -> 0x0b1a }
            r9 = 32768(0x8000, float:4.5918E-41)
            r5.setFlags(r9)     // Catch:{ Exception -> 0x0b1a }
            int r9 = (int) r7
            if (r9 == 0) goto L_0x04c1
            r13 = r46
            android.util.LongSparseArray<java.lang.Integer> r12 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r12 = r12.size()     // Catch:{ Exception -> 0x0b18 }
            r34 = r10
            r10 = 1
            if (r12 != r10) goto L_0x042b
            if (r11 == 0) goto L_0x0424
            java.lang.String r10 = "chatId"
            r5.putExtra(r10, r11)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x042b
        L_0x0424:
            if (r14 == 0) goto L_0x042b
            java.lang.String r10 = "userId"
            r5.putExtra(r10, r14)     // Catch:{ Exception -> 0x0b18 }
        L_0x042b:
            boolean r10 = org.telegram.messenger.AndroidUtilities.needShowPasscode()     // Catch:{ Exception -> 0x0b18 }
            if (r10 != 0) goto L_0x04b6
            boolean r10 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter     // Catch:{ Exception -> 0x0b18 }
            if (r10 == 0) goto L_0x0437
            goto L_0x04b6
        L_0x0437:
            android.util.LongSparseArray<java.lang.Integer> r10 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r10 = r10.size()     // Catch:{ Exception -> 0x0b18 }
            r12 = 1
            if (r10 != r12) goto L_0x04af
            int r10 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r12 = 28
            if (r10 >= r12) goto L_0x04af
            if (r28 == 0) goto L_0x047f
            r10 = r28
            org.telegram.tgnet.TLRPC$ChatPhoto r12 = r10.photo     // Catch:{ Exception -> 0x0b18 }
            if (r12 == 0) goto L_0x0475
            org.telegram.tgnet.TLRPC$ChatPhoto r12 = r10.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r12 = r12.photo_small     // Catch:{ Exception -> 0x0b18 }
            if (r12 == 0) goto L_0x0475
            org.telegram.tgnet.TLRPC$ChatPhoto r12 = r10.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r12 = r12.photo_small     // Catch:{ Exception -> 0x0b18 }
            r28 = r15
            long r14 = r12.volume_id     // Catch:{ Exception -> 0x0b18 }
            int r12 = (r14 > r30 ? 1 : (r14 == r30 ? 0 : -1))
            if (r12 == 0) goto L_0x0477
            org.telegram.tgnet.TLRPC$ChatPhoto r12 = r10.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r12 = r12.photo_small     // Catch:{ Exception -> 0x0b18 }
            int r12 = r12.local_id     // Catch:{ Exception -> 0x0b18 }
            if (r12 == 0) goto L_0x0477
            org.telegram.tgnet.TLRPC$ChatPhoto r12 = r10.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r12 = r12.photo_small     // Catch:{ Exception -> 0x0b18 }
            r35 = r4
            r14 = r12
            r12 = r21
            r21 = r3
            goto L_0x04e9
        L_0x0475:
            r28 = r15
        L_0x0477:
            r35 = r4
            r12 = r21
            r21 = r3
            goto L_0x04e8
        L_0x047f:
            r10 = r28
            r28 = r15
            if (r21 == 0) goto L_0x04ac
            r12 = r21
            org.telegram.tgnet.TLRPC$UserProfilePhoto r14 = r12.photo     // Catch:{ Exception -> 0x0b18 }
            if (r14 == 0) goto L_0x04bc
            org.telegram.tgnet.TLRPC$UserProfilePhoto r14 = r12.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r14 = r14.photo_small     // Catch:{ Exception -> 0x0b18 }
            if (r14 == 0) goto L_0x04bc
            org.telegram.tgnet.TLRPC$UserProfilePhoto r14 = r12.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r14 = r14.photo_small     // Catch:{ Exception -> 0x0b18 }
            long r14 = r14.volume_id     // Catch:{ Exception -> 0x0b18 }
            int r21 = (r14 > r30 ? 1 : (r14 == r30 ? 0 : -1))
            if (r21 == 0) goto L_0x04bc
            org.telegram.tgnet.TLRPC$UserProfilePhoto r14 = r12.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r14 = r14.photo_small     // Catch:{ Exception -> 0x0b18 }
            int r14 = r14.local_id     // Catch:{ Exception -> 0x0b18 }
            if (r14 == 0) goto L_0x04bc
            org.telegram.tgnet.TLRPC$UserProfilePhoto r14 = r12.photo     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$FileLocation r14 = r14.photo_small     // Catch:{ Exception -> 0x0b18 }
            r21 = r3
            r35 = r4
            goto L_0x04e9
        L_0x04ac:
            r12 = r21
            goto L_0x04bc
        L_0x04af:
            r12 = r21
            r10 = r28
            r28 = r15
            goto L_0x04bc
        L_0x04b6:
            r12 = r21
            r10 = r28
            r28 = r15
        L_0x04bc:
            r21 = r3
            r35 = r4
            goto L_0x04e8
        L_0x04c1:
            r13 = r46
            r34 = r10
            r12 = r21
            r10 = r28
            r28 = r15
            android.util.LongSparseArray<java.lang.Integer> r14 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r14 = r14.size()     // Catch:{ Exception -> 0x0b18 }
            r15 = 1
            if (r14 != r15) goto L_0x04bc
            long r14 = globalSecretChatId     // Catch:{ Exception -> 0x0b18 }
            int r21 = (r7 > r14 ? 1 : (r7 == r14 ? 0 : -1))
            if (r21 == 0) goto L_0x04bc
            java.lang.String r14 = "encId"
            r21 = r3
            r35 = r4
            r15 = 32
            long r3 = r7 >> r15
            int r4 = (int) r3     // Catch:{ Exception -> 0x0b18 }
            r5.putExtra(r14, r4)     // Catch:{ Exception -> 0x0b18 }
        L_0x04e8:
            r14 = 0
        L_0x04e9:
            int r3 = r13.currentAccount     // Catch:{ Exception -> 0x0b18 }
            r4 = r20
            r5.putExtra(r4, r3)     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r3 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r15 = 1073741824(0x40000000, float:2.0)
            r36 = r7
            r7 = 0
            android.app.PendingIntent r3 = android.app.PendingIntent.getActivity(r3, r7, r5, r15)     // Catch:{ Exception -> 0x0b18 }
            if (r11 == 0) goto L_0x04ff
            if (r10 == 0) goto L_0x0501
        L_0x04ff:
            if (r12 != 0) goto L_0x050c
        L_0x0501:
            boolean r5 = r19.isFcmMessage()     // Catch:{ Exception -> 0x0b18 }
            if (r5 == 0) goto L_0x050c
            r5 = r19
            java.lang.String r7 = r5.localName     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0517
        L_0x050c:
            r5 = r19
            if (r10 == 0) goto L_0x0513
            java.lang.String r7 = r10.title     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0517
        L_0x0513:
            java.lang.String r7 = org.telegram.messenger.UserObject.getUserName(r12)     // Catch:{ Exception -> 0x0b18 }
        L_0x0517:
            boolean r8 = org.telegram.messenger.AndroidUtilities.needShowPasscode()     // Catch:{ Exception -> 0x0b18 }
            if (r8 != 0) goto L_0x0524
            boolean r8 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter     // Catch:{ Exception -> 0x0b18 }
            if (r8 == 0) goto L_0x0522
            goto L_0x0524
        L_0x0522:
            r8 = 0
            goto L_0x0525
        L_0x0524:
            r8 = 1
        L_0x0525:
            if (r9 == 0) goto L_0x0536
            android.util.LongSparseArray<java.lang.Integer> r9 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r9 = r9.size()     // Catch:{ Exception -> 0x0b18 }
            r15 = 1
            if (r9 > r15) goto L_0x0536
            if (r8 == 0) goto L_0x0533
            goto L_0x0536
        L_0x0533:
            r8 = r7
            r9 = 1
            goto L_0x0558
        L_0x0536:
            if (r8 == 0) goto L_0x054e
            if (r11 == 0) goto L_0x0544
            java.lang.String r8 = "NotificationHiddenChatName"
            r9 = 2131626022(0x7f0e0826, float:1.8879268E38)
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r8, r9)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0557
        L_0x0544:
            java.lang.String r8 = "NotificationHiddenName"
            r9 = 2131626025(0x7f0e0829, float:1.8879275E38)
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r8, r9)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0557
        L_0x054e:
            java.lang.String r8 = "AppName"
            r9 = 2131624238(0x7f0e012e, float:1.887565E38)
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r8, r9)     // Catch:{ Exception -> 0x0b18 }
        L_0x0557:
            r9 = 0
        L_0x0558:
            int r11 = org.telegram.messenger.UserConfig.getActivatedAccountsCount()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r15 = ""
            r19 = r7
            r7 = 1
            if (r11 <= r7) goto L_0x0597
            android.util.LongSparseArray<java.lang.Integer> r11 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r11 = r11.size()     // Catch:{ Exception -> 0x0b18 }
            if (r11 != r7) goto L_0x0578
            org.telegram.messenger.UserConfig r7 = r46.getUserConfig()     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$User r7 = r7.getCurrentUser()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = org.telegram.messenger.UserObject.getFirstName(r7)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0598
        L_0x0578:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r7.<init>()     // Catch:{ Exception -> 0x0b18 }
            org.telegram.messenger.UserConfig r11 = r46.getUserConfig()     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$User r11 = r11.getCurrentUser()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r11 = org.telegram.messenger.UserObject.getFirstName(r11)     // Catch:{ Exception -> 0x0b18 }
            r7.append(r11)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r11 = "・"
            r7.append(r11)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0598
        L_0x0597:
            r7 = r15
        L_0x0598:
            android.util.LongSparseArray<java.lang.Integer> r11 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r11 = r11.size()     // Catch:{ Exception -> 0x0b18 }
            r20 = r6
            r6 = 1
            if (r11 != r6) goto L_0x05b0
            int r6 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r11 = 23
            if (r6 >= r11) goto L_0x05aa
            goto L_0x05b0
        L_0x05aa:
            r40 = r1
            r38 = r2
        L_0x05ae:
            r11 = r7
            goto L_0x060b
        L_0x05b0:
            android.util.LongSparseArray<java.lang.Integer> r6 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r6 = r6.size()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r11 = "NewMessages"
            r38 = r2
            r2 = 1
            if (r6 != r2) goto L_0x05d5
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r7)     // Catch:{ Exception -> 0x0b18 }
            int r6 = r13.total_unread_count     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r6 = org.telegram.messenger.LocaleController.formatPluralString(r11, r6)     // Catch:{ Exception -> 0x0b18 }
            r2.append(r6)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            r40 = r1
            goto L_0x05ae
        L_0x05d5:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r7)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r6 = "NotificationMessagesPeopleDisplayOrder"
            r40 = r1
            r7 = 2
            java.lang.Object[] r1 = new java.lang.Object[r7]     // Catch:{ Exception -> 0x0b18 }
            int r7 = r13.total_unread_count     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r11, r7)     // Catch:{ Exception -> 0x0b18 }
            r11 = 0
            r1[r11] = r7     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = "FromChats"
            android.util.LongSparseArray<java.lang.Integer> r11 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r11 = r11.size()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r7, r11)     // Catch:{ Exception -> 0x0b18 }
            r11 = 1
            r1[r11] = r7     // Catch:{ Exception -> 0x0b18 }
            r7 = 2131626073(0x7f0e0859, float:1.8879372E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r6, r7, r1)     // Catch:{ Exception -> 0x0b18 }
            r2.append(r1)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r7 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            goto L_0x05ae
        L_0x060b:
            androidx.core.app.NotificationCompat$Builder r7 = new androidx.core.app.NotificationCompat$Builder     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r1 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r7.<init>(r1)     // Catch:{ Exception -> 0x0b18 }
            r7.setContentTitle(r8)     // Catch:{ Exception -> 0x0b18 }
            r1 = 2131165759(0x7var_f, float:1.7945744E38)
            r7.setSmallIcon(r1)     // Catch:{ Exception -> 0x0b18 }
            r1 = 1
            r7.setAutoCancel(r1)     // Catch:{ Exception -> 0x0b18 }
            int r1 = r13.total_unread_count     // Catch:{ Exception -> 0x0b18 }
            r7.setNumber(r1)     // Catch:{ Exception -> 0x0b18 }
            r7.setContentIntent(r3)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r1 = r13.notificationGroup     // Catch:{ Exception -> 0x0b18 }
            r7.setGroup(r1)     // Catch:{ Exception -> 0x0b18 }
            r1 = 1
            r7.setGroupSummary(r1)     // Catch:{ Exception -> 0x0b18 }
            r7.setShowWhen(r1)     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$Message r1 = r5.messageOwner     // Catch:{ Exception -> 0x0b18 }
            int r1 = r1.date     // Catch:{ Exception -> 0x0b18 }
            long r1 = (long) r1     // Catch:{ Exception -> 0x0b18 }
            long r1 = r1 * r23
            r7.setWhen(r1)     // Catch:{ Exception -> 0x0b18 }
            r1 = -15618822(0xfffffffffvar_acfa, float:-1.936362E38)
            r7.setColor(r1)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r1 = "msg"
            r7.setCategory(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r10 != 0) goto L_0x066e
            if (r12 == 0) goto L_0x066e
            java.lang.String r1 = r12.phone     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x066e
            java.lang.String r1 = r12.phone     // Catch:{ Exception -> 0x0b18 }
            int r1 = r1.length()     // Catch:{ Exception -> 0x0b18 }
            if (r1 <= 0) goto L_0x066e
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r1.<init>()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = "tel:+"
            r1.append(r2)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r12.phone     // Catch:{ Exception -> 0x0b18 }
            r1.append(r2)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x0b18 }
            r7.addPerson(r1)     // Catch:{ Exception -> 0x0b18 }
        L_0x066e:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r13.pushMessages     // Catch:{ Exception -> 0x0b18 }
            int r1 = r1.size()     // Catch:{ Exception -> 0x0b18 }
            r2 = 1
            if (r1 != r2) goto L_0x06f0
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r13.pushMessages     // Catch:{ Exception -> 0x0b18 }
            r3 = 0
            java.lang.Object r1 = r1.get(r3)     // Catch:{ Exception -> 0x0b18 }
            org.telegram.messenger.MessageObject r1 = (org.telegram.messenger.MessageObject) r1     // Catch:{ Exception -> 0x0b18 }
            boolean[] r6 = new boolean[r2]     // Catch:{ Exception -> 0x0b18 }
            r2 = 0
            java.lang.String r12 = r13.getStringForMessage(r1, r3, r6, r2)     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$Message r1 = r1.messageOwner     // Catch:{ Exception -> 0x0b18 }
            boolean r1 = r1.silent     // Catch:{ Exception -> 0x0b18 }
            if (r12 != 0) goto L_0x068e
            return
        L_0x068e:
            if (r9 == 0) goto L_0x06d9
            if (r10 == 0) goto L_0x06a8
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = " @ "
            r2.append(r3)     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r12.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x06da
        L_0x06a8:
            r2 = 0
            boolean r3 = r6[r2]     // Catch:{ Exception -> 0x0b18 }
            if (r3 == 0) goto L_0x06c3
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = ": "
            r2.append(r3)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r12.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x06da
        L_0x06c3:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = " "
            r2.append(r3)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r12.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x06da
        L_0x06d9:
            r2 = r12
        L_0x06da:
            r7.setContentText(r2)     // Catch:{ Exception -> 0x0b18 }
            androidx.core.app.NotificationCompat$BigTextStyle r3 = new androidx.core.app.NotificationCompat$BigTextStyle     // Catch:{ Exception -> 0x0b18 }
            r3.<init>()     // Catch:{ Exception -> 0x0b18 }
            r3.bigText(r2)     // Catch:{ Exception -> 0x0b18 }
            r7.setStyle(r3)     // Catch:{ Exception -> 0x0b18 }
            r43 = r4
            r44 = r5
            r42 = r14
            goto L_0x07b0
        L_0x06f0:
            r7.setContentText(r11)     // Catch:{ Exception -> 0x0b18 }
            androidx.core.app.NotificationCompat$InboxStyle r1 = new androidx.core.app.NotificationCompat$InboxStyle     // Catch:{ Exception -> 0x0b18 }
            r1.<init>()     // Catch:{ Exception -> 0x0b18 }
            r1.setBigContentTitle(r8)     // Catch:{ Exception -> 0x0b18 }
            r2 = 10
            java.util.ArrayList<org.telegram.messenger.MessageObject> r3 = r13.pushMessages     // Catch:{ Exception -> 0x0b18 }
            int r3 = r3.size()     // Catch:{ Exception -> 0x0b18 }
            int r2 = java.lang.Math.min(r2, r3)     // Catch:{ Exception -> 0x0b18 }
            r3 = 1
            boolean[] r6 = new boolean[r3]     // Catch:{ Exception -> 0x0b18 }
            r3 = 2
            r12 = 0
            r39 = 0
        L_0x070e:
            if (r12 >= r2) goto L_0x07a1
            r41 = r2
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r13.pushMessages     // Catch:{ Exception -> 0x0b18 }
            java.lang.Object r2 = r2.get(r12)     // Catch:{ Exception -> 0x0b18 }
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2     // Catch:{ Exception -> 0x0b18 }
            r43 = r4
            r44 = r5
            r42 = r14
            r4 = 0
            r14 = 0
            java.lang.String r5 = r13.getStringForMessage(r2, r4, r6, r14)     // Catch:{ Exception -> 0x0b18 }
            if (r5 == 0) goto L_0x0791
            org.telegram.tgnet.TLRPC$Message r4 = r2.messageOwner     // Catch:{ Exception -> 0x0b18 }
            int r4 = r4.date     // Catch:{ Exception -> 0x0b18 }
            r14 = r18
            if (r4 > r14) goto L_0x0731
            goto L_0x0793
        L_0x0731:
            r4 = 2
            if (r3 != r4) goto L_0x073a
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner     // Catch:{ Exception -> 0x0b18 }
            boolean r3 = r2.silent     // Catch:{ Exception -> 0x0b18 }
            r39 = r5
        L_0x073a:
            android.util.LongSparseArray<java.lang.Integer> r2 = r13.pushDialogs     // Catch:{ Exception -> 0x0b18 }
            int r2 = r2.size()     // Catch:{ Exception -> 0x0b18 }
            r4 = 1
            if (r2 != r4) goto L_0x078d
            if (r9 == 0) goto L_0x078d
            if (r10 == 0) goto L_0x075d
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r4 = " @ "
            r2.append(r4)     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r5 = r5.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x078d
        L_0x075d:
            r2 = 0
            boolean r4 = r6[r2]     // Catch:{ Exception -> 0x0b18 }
            if (r4 == 0) goto L_0x0778
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r4 = ": "
            r2.append(r4)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r5 = r5.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x078d
        L_0x0778:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r2.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2.append(r8)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r4 = " "
            r2.append(r4)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r5 = r5.replace(r2, r15)     // Catch:{ Exception -> 0x0b18 }
        L_0x078d:
            r1.addLine(r5)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0793
        L_0x0791:
            r14 = r18
        L_0x0793:
            int r12 = r12 + 1
            r18 = r14
            r2 = r41
            r14 = r42
            r4 = r43
            r5 = r44
            goto L_0x070e
        L_0x07a1:
            r43 = r4
            r44 = r5
            r42 = r14
            r1.setSummaryText(r11)     // Catch:{ Exception -> 0x0b18 }
            r7.setStyle(r1)     // Catch:{ Exception -> 0x0b18 }
            r1 = r3
            r12 = r39
        L_0x07b0:
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r3 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            java.lang.Class<org.telegram.messenger.NotificationDismissReceiver> r4 = org.telegram.messenger.NotificationDismissReceiver.class
            r2.<init>(r3, r4)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = "messageDate"
            r4 = r44
            org.telegram.tgnet.TLRPC$Message r5 = r4.messageOwner     // Catch:{ Exception -> 0x0b18 }
            int r5 = r5.date     // Catch:{ Exception -> 0x0b18 }
            r2.putExtra(r3, r5)     // Catch:{ Exception -> 0x0b18 }
            int r3 = r13.currentAccount     // Catch:{ Exception -> 0x0b18 }
            r5 = r43
            r2.putExtra(r5, r3)     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r3 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r6 = 134217728(0x8000000, float:3.85186E-34)
            r8 = 1
            android.app.PendingIntent r2 = android.app.PendingIntent.getBroadcast(r3, r8, r2, r6)     // Catch:{ Exception -> 0x0b18 }
            r7.setDeleteIntent(r2)     // Catch:{ Exception -> 0x0b18 }
            if (r42 == 0) goto L_0x0823
            org.telegram.messenger.ImageLoader r2 = org.telegram.messenger.ImageLoader.getInstance()     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = "50_50"
            r14 = r42
            r8 = 0
            android.graphics.drawable.BitmapDrawable r2 = r2.getImageFromMemory(r14, r8, r3)     // Catch:{ Exception -> 0x0b18 }
            if (r2 == 0) goto L_0x07f0
            android.graphics.Bitmap r2 = r2.getBitmap()     // Catch:{ Exception -> 0x0b18 }
            r7.setLargeIcon(r2)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0824
        L_0x07f0:
            r2 = 1
            java.io.File r3 = org.telegram.messenger.FileLoader.getPathToAttach(r14, r2)     // Catch:{ all -> 0x0824 }
            boolean r2 = r3.exists()     // Catch:{ all -> 0x0824 }
            if (r2 == 0) goto L_0x0824
            r2 = 1126170624(0x43200000, float:160.0)
            r9 = 1112014848(0x42480000, float:50.0)
            int r9 = org.telegram.messenger.AndroidUtilities.dp(r9)     // Catch:{ all -> 0x0824 }
            float r9 = (float) r9     // Catch:{ all -> 0x0824 }
            float r2 = r2 / r9
            android.graphics.BitmapFactory$Options r9 = new android.graphics.BitmapFactory$Options     // Catch:{ all -> 0x0824 }
            r9.<init>()     // Catch:{ all -> 0x0824 }
            r10 = 1065353216(0x3var_, float:1.0)
            int r10 = (r2 > r10 ? 1 : (r2 == r10 ? 0 : -1))
            if (r10 >= 0) goto L_0x0812
            r2 = 1
            goto L_0x0813
        L_0x0812:
            int r2 = (int) r2     // Catch:{ all -> 0x0824 }
        L_0x0813:
            r9.inSampleSize = r2     // Catch:{ all -> 0x0824 }
            java.lang.String r2 = r3.getAbsolutePath()     // Catch:{ all -> 0x0824 }
            android.graphics.Bitmap r2 = android.graphics.BitmapFactory.decodeFile(r2, r9)     // Catch:{ all -> 0x0824 }
            if (r2 == 0) goto L_0x0824
            r7.setLargeIcon(r2)     // Catch:{ all -> 0x0824 }
            goto L_0x0824
        L_0x0823:
            r8 = 0
        L_0x0824:
            r14 = r47
            if (r14 == 0) goto L_0x086f
            r2 = 1
            if (r1 != r2) goto L_0x082c
            goto L_0x086f
        L_0x082c:
            if (r40 != 0) goto L_0x083b
            r2 = 0
            r7.setPriority(r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r2 < r3) goto L_0x087c
            r2 = 1
            r9 = 3
            goto L_0x087e
        L_0x083b:
            r2 = r40
            r3 = 1
            if (r2 == r3) goto L_0x0862
            r3 = 2
            if (r2 != r3) goto L_0x0844
            goto L_0x0862
        L_0x0844:
            r3 = 4
            if (r2 != r3) goto L_0x0854
            r2 = -2
            r7.setPriority(r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r2 < r3) goto L_0x087c
            r2 = 1
            r9 = 1
            goto L_0x087e
        L_0x0854:
            r3 = 5
            if (r2 != r3) goto L_0x087c
            r2 = -1
            r7.setPriority(r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r2 < r3) goto L_0x087c
            goto L_0x0879
        L_0x0862:
            r2 = 1
            r7.setPriority(r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r2 < r3) goto L_0x087c
            r2 = 1
            r9 = 4
            goto L_0x087e
        L_0x086f:
            r2 = -1
            r7.setPriority(r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r2 < r3) goto L_0x087c
        L_0x0879:
            r2 = 1
            r9 = 2
            goto L_0x087e
        L_0x087c:
            r2 = 1
            r9 = 0
        L_0x087e:
            if (r1 == r2) goto L_0x09a1
            if (r25 != 0) goto L_0x09a1
            boolean r1 = org.telegram.messenger.ApplicationLoader.mainInterfacePaused     // Catch:{ Exception -> 0x0b18 }
            if (r1 != 0) goto L_0x0888
            if (r27 == 0) goto L_0x08b7
        L_0x0888:
            int r1 = r12.length()     // Catch:{ Exception -> 0x0b18 }
            r2 = 100
            if (r1 <= r2) goto L_0x08b4
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0b18 }
            r1.<init>()     // Catch:{ Exception -> 0x0b18 }
            r2 = 100
            r3 = 0
            java.lang.String r2 = r12.substring(r3, r2)     // Catch:{ Exception -> 0x0b18 }
            r3 = 10
            r10 = 32
            java.lang.String r2 = r2.replace(r3, r10)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = r2.trim()     // Catch:{ Exception -> 0x0b18 }
            r1.append(r2)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r2 = "..."
            r1.append(r2)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r12 = r1.toString()     // Catch:{ Exception -> 0x0b18 }
        L_0x08b4:
            r7.setTicker(r12)     // Catch:{ Exception -> 0x0b18 }
        L_0x08b7:
            org.telegram.messenger.MediaController r1 = org.telegram.messenger.MediaController.getInstance()     // Catch:{ Exception -> 0x0b18 }
            boolean r1 = r1.isRecordingAudio()     // Catch:{ Exception -> 0x0b18 }
            if (r1 != 0) goto L_0x093b
            if (r38 == 0) goto L_0x093b
            r1 = r35
            r2 = r38
            boolean r1 = r2.equals(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r1 != 0) goto L_0x093b
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 26
            if (r1 < r3) goto L_0x08e3
            r1 = r21
            boolean r1 = r2.equals(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x08de
            android.net.Uri r1 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI     // Catch:{ Exception -> 0x0b18 }
            goto L_0x093c
        L_0x08de:
            android.net.Uri r1 = android.net.Uri.parse(r2)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x093c
        L_0x08e3:
            r1 = r21
            boolean r1 = r2.equals(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x08f2
            android.net.Uri r1 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI     // Catch:{ Exception -> 0x0b18 }
            r2 = 5
            r7.setSound(r1, r2)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x093b
        L_0x08f2:
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 24
            if (r1 < r3) goto L_0x0933
            java.lang.String r1 = "file://"
            boolean r1 = r2.startsWith(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x0933
            android.net.Uri r1 = android.net.Uri.parse(r2)     // Catch:{ Exception -> 0x0b18 }
            boolean r1 = org.telegram.messenger.AndroidUtilities.isInternalUri(r1)     // Catch:{ Exception -> 0x0b18 }
            if (r1 != 0) goto L_0x0933
            android.content.Context r1 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x092a }
            java.lang.String r3 = "org.telegram.messenger.beta.provider"
            java.io.File r10 = new java.io.File     // Catch:{ Exception -> 0x092a }
            java.lang.String r12 = "file://"
            java.lang.String r12 = r2.replace(r12, r15)     // Catch:{ Exception -> 0x092a }
            r10.<init>(r12)     // Catch:{ Exception -> 0x092a }
            android.net.Uri r1 = androidx.core.content.FileProvider.getUriForFile(r1, r3, r10)     // Catch:{ Exception -> 0x092a }
            android.content.Context r3 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x092a }
            java.lang.String r10 = "com.android.systemui"
            r12 = 1
            r3.grantUriPermission(r10, r1, r12)     // Catch:{ Exception -> 0x092a }
            r3 = 5
            r7.setSound(r1, r3)     // Catch:{ Exception -> 0x092a }
            goto L_0x093b
        L_0x092a:
            android.net.Uri r1 = android.net.Uri.parse(r2)     // Catch:{ Exception -> 0x0b18 }
            r2 = 5
            r7.setSound(r1, r2)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x093b
        L_0x0933:
            android.net.Uri r1 = android.net.Uri.parse(r2)     // Catch:{ Exception -> 0x0b18 }
            r2 = 5
            r7.setSound(r1, r2)     // Catch:{ Exception -> 0x0b18 }
        L_0x093b:
            r1 = r8
        L_0x093c:
            if (r20 == 0) goto L_0x0948
            r2 = 1000(0x3e8, float:1.401E-42)
            r3 = 1000(0x3e8, float:1.401E-42)
            r10 = r20
            r7.setLights(r10, r2, r3)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x094a
        L_0x0948:
            r10 = r20
        L_0x094a:
            r15 = r28
            r2 = 2
            if (r15 == r2) goto L_0x0992
            org.telegram.messenger.MediaController r2 = org.telegram.messenger.MediaController.getInstance()     // Catch:{ Exception -> 0x0b18 }
            boolean r2 = r2.isRecordingAudio()     // Catch:{ Exception -> 0x0b18 }
            if (r2 == 0) goto L_0x095b
            r2 = 2
            goto L_0x0992
        L_0x095b:
            r2 = 1
            if (r15 != r2) goto L_0x0972
            r3 = 4
            long[] r3 = new long[r3]     // Catch:{ Exception -> 0x0b18 }
            r8 = 0
            r3[r8] = r30     // Catch:{ Exception -> 0x0b18 }
            r20 = 100
            r3[r2] = r20     // Catch:{ Exception -> 0x0b18 }
            r2 = 2
            r3[r2] = r30     // Catch:{ Exception -> 0x0b18 }
            r2 = 3
            r3[r2] = r20     // Catch:{ Exception -> 0x0b18 }
            r7.setVibrate(r3)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x099d
        L_0x0972:
            if (r15 == 0) goto L_0x098a
            r2 = 4
            if (r15 != r2) goto L_0x0978
            goto L_0x098a
        L_0x0978:
            r2 = 3
            if (r15 != r2) goto L_0x0988
            r2 = 2
            long[] r3 = new long[r2]     // Catch:{ Exception -> 0x0b18 }
            r2 = 0
            r3[r2] = r30     // Catch:{ Exception -> 0x0b18 }
            r2 = 1
            r3[r2] = r23     // Catch:{ Exception -> 0x0b18 }
            r7.setVibrate(r3)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x099d
        L_0x0988:
            r12 = r1
            goto L_0x099f
        L_0x098a:
            r2 = 2
            r7.setDefaults(r2)     // Catch:{ Exception -> 0x0b18 }
            r2 = 0
            long[] r3 = new long[r2]     // Catch:{ Exception -> 0x0b18 }
            goto L_0x099d
        L_0x0992:
            long[] r3 = new long[r2]     // Catch:{ Exception -> 0x0b18 }
            r2 = 0
            r3[r2] = r30     // Catch:{ Exception -> 0x0b18 }
            r2 = 1
            r3[r2] = r30     // Catch:{ Exception -> 0x0b18 }
            r7.setVibrate(r3)     // Catch:{ Exception -> 0x0b18 }
        L_0x099d:
            r12 = r1
            r8 = r3
        L_0x099f:
            r1 = 1
            goto L_0x09b1
        L_0x09a1:
            r10 = r20
            r1 = 2
            long[] r2 = new long[r1]     // Catch:{ Exception -> 0x0b18 }
            r1 = 0
            r2[r1] = r30     // Catch:{ Exception -> 0x0b18 }
            r1 = 1
            r2[r1] = r30     // Catch:{ Exception -> 0x0b18 }
            r7.setVibrate(r2)     // Catch:{ Exception -> 0x0b18 }
            r12 = r8
            r8 = r2
        L_0x09b1:
            boolean r2 = org.telegram.messenger.AndroidUtilities.needShowPasscode()     // Catch:{ Exception -> 0x0b18 }
            if (r2 != 0) goto L_0x0a89
            boolean r2 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter     // Catch:{ Exception -> 0x0b18 }
            if (r2 != 0) goto L_0x0a89
            long r2 = r4.getDialogId()     // Catch:{ Exception -> 0x0b18 }
            r16 = 777000(0xbdb28, double:3.83889E-318)
            int r15 = (r2 > r16 ? 1 : (r2 == r16 ? 0 : -1))
            if (r15 != 0) goto L_0x0a89
            org.telegram.tgnet.TLRPC$Message r2 = r4.messageOwner     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$ReplyMarkup r2 = r2.reply_markup     // Catch:{ Exception -> 0x0b18 }
            if (r2 == 0) goto L_0x0a89
            org.telegram.tgnet.TLRPC$Message r2 = r4.messageOwner     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$ReplyMarkup r2 = r2.reply_markup     // Catch:{ Exception -> 0x0b18 }
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_keyboardButtonRow> r2 = r2.rows     // Catch:{ Exception -> 0x0b18 }
            int r3 = r2.size()     // Catch:{ Exception -> 0x0b18 }
            r15 = 0
            r16 = 0
        L_0x09d9:
            if (r15 >= r3) goto L_0x0a80
            java.lang.Object r17 = r2.get(r15)     // Catch:{ Exception -> 0x0b18 }
            r1 = r17
            org.telegram.tgnet.TLRPC$TL_keyboardButtonRow r1 = (org.telegram.tgnet.TLRPC$TL_keyboardButtonRow) r1     // Catch:{ Exception -> 0x0b18 }
            java.util.ArrayList<org.telegram.tgnet.TLRPC$KeyboardButton> r6 = r1.buttons     // Catch:{ Exception -> 0x0b18 }
            int r6 = r6.size()     // Catch:{ Exception -> 0x0b18 }
            r20 = r2
            r2 = 0
        L_0x09ec:
            if (r2 >= r6) goto L_0x0a64
            r21 = r3
            java.util.ArrayList<org.telegram.tgnet.TLRPC$KeyboardButton> r3 = r1.buttons     // Catch:{ Exception -> 0x0b18 }
            java.lang.Object r3 = r3.get(r2)     // Catch:{ Exception -> 0x0b18 }
            org.telegram.tgnet.TLRPC$KeyboardButton r3 = (org.telegram.tgnet.TLRPC$KeyboardButton) r3     // Catch:{ Exception -> 0x0b18 }
            r22 = r1
            boolean r1 = r3 instanceof org.telegram.tgnet.TLRPC$TL_keyboardButtonCallback     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x0a46
            android.content.Intent r1 = new android.content.Intent     // Catch:{ Exception -> 0x0b18 }
            r23 = r6
            android.content.Context r6 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r24 = r11
            java.lang.Class<org.telegram.messenger.NotificationCallbackReceiver> r11 = org.telegram.messenger.NotificationCallbackReceiver.class
            r1.<init>(r6, r11)     // Catch:{ Exception -> 0x0b18 }
            int r6 = r13.currentAccount     // Catch:{ Exception -> 0x0b18 }
            r1.putExtra(r5, r6)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r6 = "did"
            r25 = r12
            r11 = r36
            r1.putExtra(r6, r11)     // Catch:{ Exception -> 0x0b18 }
            byte[] r6 = r3.data     // Catch:{ Exception -> 0x0b18 }
            if (r6 == 0) goto L_0x0a24
            java.lang.String r6 = "data"
            byte[] r14 = r3.data     // Catch:{ Exception -> 0x0b18 }
            r1.putExtra(r6, r14)     // Catch:{ Exception -> 0x0b18 }
        L_0x0a24:
            java.lang.String r6 = "mid"
            int r14 = r4.getId()     // Catch:{ Exception -> 0x0b18 }
            r1.putExtra(r6, r14)     // Catch:{ Exception -> 0x0b18 }
            java.lang.String r3 = r3.text     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r6 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            int r14 = r13.lastButtonId     // Catch:{ Exception -> 0x0b18 }
            r44 = r4
            int r4 = r14 + 1
            r13.lastButtonId = r4     // Catch:{ Exception -> 0x0b18 }
            r4 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r1 = android.app.PendingIntent.getBroadcast(r6, r14, r1, r4)     // Catch:{ Exception -> 0x0b18 }
            r4 = 0
            r7.addAction(r4, r3, r1)     // Catch:{ Exception -> 0x0b18 }
            r16 = 1
            goto L_0x0a51
        L_0x0a46:
            r44 = r4
            r23 = r6
            r24 = r11
            r25 = r12
            r11 = r36
            r4 = 0
        L_0x0a51:
            int r2 = r2 + 1
            r14 = r47
            r36 = r11
            r3 = r21
            r1 = r22
            r6 = r23
            r11 = r24
            r12 = r25
            r4 = r44
            goto L_0x09ec
        L_0x0a64:
            r21 = r3
            r44 = r4
            r24 = r11
            r25 = r12
            r11 = r36
            r4 = 0
            int r15 = r15 + 1
            r14 = r47
            r2 = r20
            r11 = r24
            r12 = r25
            r4 = r44
            r1 = 1
            r6 = 134217728(0x8000000, float:3.85186E-34)
            goto L_0x09d9
        L_0x0a80:
            r24 = r11
            r25 = r12
            r11 = r36
            r4 = r16
            goto L_0x0a90
        L_0x0a89:
            r24 = r11
            r25 = r12
            r11 = r36
            r4 = 0
        L_0x0a90:
            if (r4 != 0) goto L_0x0aeb
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r2 = 24
            if (r1 >= r2) goto L_0x0aeb
            java.lang.String r1 = org.telegram.messenger.SharedConfig.passcodeHash     // Catch:{ Exception -> 0x0b18 }
            int r1 = r1.length()     // Catch:{ Exception -> 0x0b18 }
            if (r1 != 0) goto L_0x0aeb
            boolean r1 = r46.hasMessagesToReply()     // Catch:{ Exception -> 0x0b18 }
            if (r1 == 0) goto L_0x0aeb
            android.content.Intent r1 = new android.content.Intent     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            java.lang.Class<org.telegram.messenger.PopupReplyReceiver> r3 = org.telegram.messenger.PopupReplyReceiver.class
            r1.<init>(r2, r3)     // Catch:{ Exception -> 0x0b18 }
            int r2 = r13.currentAccount     // Catch:{ Exception -> 0x0b18 }
            r1.putExtra(r5, r2)     // Catch:{ Exception -> 0x0b18 }
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r3 = 19
            if (r2 > r3) goto L_0x0ad3
            r2 = 2131165441(0x7var_, float:1.79451E38)
            java.lang.String r3 = "Reply"
            r4 = 2131626673(0x7f0e0ab1, float:1.8880589E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r4)     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r4 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r5 = 134217728(0x8000000, float:3.85186E-34)
            r6 = 2
            android.app.PendingIntent r1 = android.app.PendingIntent.getBroadcast(r4, r6, r1, r5)     // Catch:{ Exception -> 0x0b18 }
            r7.addAction(r2, r3, r1)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0aeb
        L_0x0ad3:
            r2 = 2131165440(0x7var_, float:1.7945097E38)
            java.lang.String r3 = "Reply"
            r4 = 2131626673(0x7f0e0ab1, float:1.8880589E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r4)     // Catch:{ Exception -> 0x0b18 }
            android.content.Context r4 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0b18 }
            r5 = 134217728(0x8000000, float:3.85186E-34)
            r6 = 2
            android.app.PendingIntent r1 = android.app.PendingIntent.getBroadcast(r4, r6, r1, r5)     // Catch:{ Exception -> 0x0b18 }
            r7.addAction(r2, r3, r1)     // Catch:{ Exception -> 0x0b18 }
        L_0x0aeb:
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x0b18 }
            r2 = 26
            if (r1 < r2) goto L_0x0b0c
            r1 = r46
            r2 = r11
            r4 = r19
            r5 = r8
            r6 = r10
            r12 = r7
            r7 = r25
            r8 = r9
            r9 = r34
            r10 = r32
            r14 = r24
            r11 = r33
            java.lang.String r1 = r1.validateChannelId(r2, r4, r5, r6, r7, r8, r9, r10, r11)     // Catch:{ Exception -> 0x0b18 }
            r12.setChannelId(r1)     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0b0f
        L_0x0b0c:
            r12 = r7
            r14 = r24
        L_0x0b0f:
            r1 = r47
            r13.showExtraNotifications(r12, r1, r14)     // Catch:{ Exception -> 0x0b18 }
            r46.scheduleNotificationRepeat()     // Catch:{ Exception -> 0x0b18 }
            goto L_0x0b24
        L_0x0b18:
            r0 = move-exception
            goto L_0x0b20
        L_0x0b1a:
            r0 = move-exception
            r13 = r46
            goto L_0x0b20
        L_0x0b1e:
            r0 = move-exception
            r13 = r12
        L_0x0b20:
            r1 = r0
        L_0x0b21:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
        L_0x0b24:
            return
        L_0x0b25:
            r13 = r12
            r46.dismissNotification()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.showOrUpdateNotification(boolean):void");
    }

    @SuppressLint({"NewApi"})
    private void setNotificationChannel(Notification notification, NotificationCompat.Builder builder, boolean z) {
        if (z) {
            builder.setChannelId(OTHER_NOTIFICATIONS_CHANNEL);
        } else {
            builder.setChannelId(notification.getChannelId());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x02de  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0302  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0315  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0369  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x037c A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x03c5  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x03d7  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0418  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x0433  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x0449  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x046a  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0474 A[SYNTHETIC, Splitter:B:181:0x0474] */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x04d0  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x04d6  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x04ed A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0506  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x050f  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x0520  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x0559  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x059d  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x0682  */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0687  */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x06fb  */
    /* JADX WARNING: Removed duplicated region for block: B:352:0x07e6  */
    /* JADX WARNING: Removed duplicated region for block: B:358:0x0802  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0831  */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x0841 A[SYNTHETIC, Splitter:B:369:0x0841] */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x08e8  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x08f9  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0917  */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x0973  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x09a6  */
    /* JADX WARNING: Removed duplicated region for block: B:404:0x09c7  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x09e9  */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x0a9c  */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x0aa7  */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x0aae  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x0abe  */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x0ac4  */
    /* JADX WARNING: Removed duplicated region for block: B:420:0x0ac8  */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x0acd  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x0ae1  */
    /* JADX WARNING: Removed duplicated region for block: B:456:0x0be8  */
    /* JADX WARNING: Removed duplicated region for block: B:464:0x0c2f A[Catch:{ JSONException -> 0x0c7f }] */
    /* JADX WARNING: Removed duplicated region for block: B:466:0x0CLASSNAME A[Catch:{ JSONException -> 0x0c7f }] */
    /* JADX WARNING: Removed duplicated region for block: B:469:0x0CLASSNAME  */
    /* JADX WARNING: Removed duplicated region for block: B:472:0x0CLASSNAME A[Catch:{ JSONException -> 0x0c7f }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01aa  */
    @android.annotation.SuppressLint({"InlinedApi"})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showExtraNotifications(androidx.core.app.NotificationCompat.Builder r77, boolean r78, java.lang.String r79) {
        /*
            r76 = this;
            r8 = r76
            android.app.Notification r9 = r77.build()
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 18
            if (r0 >= r1) goto L_0x001d
            androidx.core.app.NotificationManagerCompat r0 = notificationManager
            int r1 = r8.notificationId
            r0.notify(r1, r9)
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x001c
            java.lang.String r0 = "show summary notification by SDK check"
            org.telegram.messenger.FileLog.d(r0)
        L_0x001c:
            return
        L_0x001d:
            org.telegram.messenger.AccountInstance r0 = r76.getAccountInstance()
            android.content.SharedPreferences r0 = r0.getNotificationsSettings()
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            android.util.LongSparseArray r11 = new android.util.LongSparseArray
            r11.<init>()
            r12 = 0
            r1 = 0
        L_0x0031:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r8.pushMessages
            int r2 = r2.size()
            if (r1 >= r2) goto L_0x007e
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r8.pushMessages
            java.lang.Object r2 = r2.get(r1)
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2
            long r3 = r2.getDialogId()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "dismissDate"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            int r5 = r0.getInt(r5, r12)
            org.telegram.tgnet.TLRPC$Message r6 = r2.messageOwner
            int r6 = r6.date
            if (r6 > r5) goto L_0x0061
            goto L_0x007b
        L_0x0061:
            java.lang.Object r5 = r11.get(r3)
            java.util.ArrayList r5 = (java.util.ArrayList) r5
            if (r5 != 0) goto L_0x0078
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r11.put(r3, r5)
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r10.add(r12, r3)
        L_0x0078:
            r5.add(r2)
        L_0x007b:
            int r1 = r1 + 1
            goto L_0x0031
        L_0x007e:
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.wearNotificationsIds
            android.util.LongSparseArray r13 = r0.clone()
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.wearNotificationsIds
            r0.clear()
            java.util.ArrayList r14 = new java.util.ArrayList
            r14.<init>()
            boolean r0 = org.telegram.messenger.WearDataLayerListenerService.isWatchConnected()
            if (r0 == 0) goto L_0x009b
            org.json.JSONArray r0 = new org.json.JSONArray
            r0.<init>()
            r7 = r0
            goto L_0x009c
        L_0x009b:
            r7 = 0
        L_0x009c:
            int r0 = android.os.Build.VERSION.SDK_INT
            r6 = 27
            r5 = 1
            if (r0 <= r6) goto L_0x00ae
            if (r0 <= r6) goto L_0x00ac
            int r0 = r10.size()
            if (r0 <= r5) goto L_0x00ac
            goto L_0x00ae
        L_0x00ac:
            r4 = 0
            goto L_0x00af
        L_0x00ae:
            r4 = 1
        L_0x00af:
            r3 = 26
            if (r4 == 0) goto L_0x00ba
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r3) goto L_0x00ba
            checkOtherNotificationsChannel()
        L_0x00ba:
            org.telegram.messenger.UserConfig r0 = r76.getUserConfig()
            int r2 = r0.getClientUserId()
            boolean r0 = org.telegram.messenger.AndroidUtilities.needShowPasscode()
            if (r0 != 0) goto L_0x00d0
            boolean r0 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r0 == 0) goto L_0x00cd
            goto L_0x00d0
        L_0x00cd:
            r16 = 0
            goto L_0x00d2
        L_0x00d0:
            r16 = 1
        L_0x00d2:
            int r1 = r10.size()
            r6 = 0
        L_0x00d7:
            java.lang.String r15 = "id"
            if (r6 >= r1) goto L_0x0CLASSNAME
            java.lang.Object r0 = r10.get(r6)
            java.lang.Long r0 = (java.lang.Long) r0
            r19 = r6
            long r5 = r0.longValue()
            java.lang.Object r0 = r11.get(r5)
            r3 = r0
            java.util.ArrayList r3 = (java.util.ArrayList) r3
            java.lang.Object r0 = r3.get(r12)
            org.telegram.messenger.MessageObject r0 = (org.telegram.messenger.MessageObject) r0
            int r12 = r0.getId()
            r22 = r11
            int r11 = (int) r5
            r23 = r1
            r1 = 32
            r24 = r14
            r25 = r15
            long r14 = r5 >> r1
            int r15 = (int) r14
            java.lang.Object r0 = r13.get(r5)
            java.lang.Integer r0 = (java.lang.Integer) r0
            if (r0 != 0) goto L_0x011a
            if (r11 == 0) goto L_0x0115
            java.lang.Integer r0 = java.lang.Integer.valueOf(r11)
            goto L_0x011d
        L_0x0115:
            java.lang.Integer r0 = java.lang.Integer.valueOf(r15)
            goto L_0x011d
        L_0x011a:
            r13.remove(r5)
        L_0x011d:
            r14 = r0
            if (r7 == 0) goto L_0x0126
            org.json.JSONObject r0 = new org.json.JSONObject
            r0.<init>()
            goto L_0x0127
        L_0x0126:
            r0 = 0
        L_0x0127:
            r1 = 0
            java.lang.Object r27 = r3.get(r1)
            r1 = r27
            org.telegram.messenger.MessageObject r1 = (org.telegram.messenger.MessageObject) r1
            r27 = r0
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            r28 = r13
            int r13 = r0.date
            r29 = r7
            android.util.LongSparseArray r7 = new android.util.LongSparseArray
            r7.<init>()
            r30 = 0
            if (r11 == 0) goto L_0x024d
            r0 = 777000(0xbdb28, float:1.088809E-39)
            if (r11 == r0) goto L_0x014a
            r0 = 1
            goto L_0x014b
        L_0x014a:
            r0 = 0
        L_0x014b:
            if (r11 <= 0) goto L_0x01bf
            r32 = r0
            org.telegram.messenger.MessagesController r0 = r76.getMessagesController()
            r33 = r9
            java.lang.Integer r9 = java.lang.Integer.valueOf(r11)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r9)
            if (r0 != 0) goto L_0x0184
            boolean r9 = r1.isFcmMessage()
            if (r9 == 0) goto L_0x016a
            java.lang.String r1 = r1.localName
            r34 = r0
            goto L_0x01a7
        L_0x016a:
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x0208
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "not found user to show dialog notification "
            r0.append(r1)
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.w(r0)
            goto L_0x0208
        L_0x0184:
            java.lang.String r1 = org.telegram.messenger.UserObject.getUserName(r0)
            org.telegram.tgnet.TLRPC$UserProfilePhoto r9 = r0.photo
            if (r9 == 0) goto L_0x01a1
            org.telegram.tgnet.TLRPC$FileLocation r9 = r9.photo_small
            if (r9 == 0) goto L_0x01a1
            r34 = r0
            r35 = r1
            long r0 = r9.volume_id
            int r36 = (r0 > r30 ? 1 : (r0 == r30 ? 0 : -1))
            if (r36 == 0) goto L_0x01a5
            int r0 = r9.local_id
            if (r0 == 0) goto L_0x01a5
            r1 = r35
            goto L_0x01a8
        L_0x01a1:
            r34 = r0
            r35 = r1
        L_0x01a5:
            r1 = r35
        L_0x01a7:
            r9 = 0
        L_0x01a8:
            if (r11 != r2) goto L_0x01b3
            r0 = 2131625816(0x7f0e0758, float:1.887885E38)
            java.lang.String r1 = "MessageScheduledReminderNotification"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r0)
        L_0x01b3:
            r35 = r1
            r0 = r9
            r36 = r10
            r9 = r27
            r27 = r34
            r10 = 0
            goto L_0x02d2
        L_0x01bf:
            r32 = r0
            r33 = r9
            org.telegram.messenger.MessagesController r0 = r76.getMessagesController()
            int r9 = -r11
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            org.telegram.tgnet.TLRPC$Chat r0 = r0.getChat(r9)
            if (r0 != 0) goto L_0x020f
            boolean r9 = r1.isFcmMessage()
            if (r9 == 0) goto L_0x01f0
            boolean r9 = r1.isMegagroup()
            r34 = r9
            java.lang.String r9 = r1.localName
            boolean r1 = r1.localChannel
            r37 = r1
            r35 = r9
            r36 = r10
            r9 = r27
            r27 = 0
            r10 = r0
            r0 = 0
            goto L_0x02d6
        L_0x01f0:
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x0208
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "not found chat to show dialog notification "
            r0.append(r1)
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.w(r0)
        L_0x0208:
            r71 = r2
            r5 = r4
            r36 = r10
            goto L_0x0280
        L_0x020f:
            boolean r1 = r0.megagroup
            boolean r9 = org.telegram.messenger.ChatObject.isChannel(r0)
            if (r9 == 0) goto L_0x021f
            boolean r9 = r0.megagroup
            if (r9 != 0) goto L_0x021f
            r34 = r1
            r9 = 1
            goto L_0x0222
        L_0x021f:
            r34 = r1
            r9 = 0
        L_0x0222:
            java.lang.String r1 = r0.title
            r35 = r1
            org.telegram.tgnet.TLRPC$ChatPhoto r1 = r0.photo
            if (r1 == 0) goto L_0x0241
            org.telegram.tgnet.TLRPC$FileLocation r1 = r1.photo_small
            if (r1 == 0) goto L_0x0241
            r37 = r9
            r36 = r10
            long r9 = r1.volume_id
            int r38 = (r9 > r30 ? 1 : (r9 == r30 ? 0 : -1))
            if (r38 == 0) goto L_0x0245
            int r9 = r1.local_id
            if (r9 == 0) goto L_0x0245
            r10 = r0
            r0 = r1
            r9 = r27
            goto L_0x0249
        L_0x0241:
            r37 = r9
            r36 = r10
        L_0x0245:
            r10 = r0
            r9 = r27
            r0 = 0
        L_0x0249:
            r27 = 0
            goto L_0x02d6
        L_0x024d:
            r33 = r9
            r36 = r10
            long r0 = globalSecretChatId
            int r9 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r9 == 0) goto L_0x02bf
            org.telegram.messenger.MessagesController r0 = r76.getMessagesController()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r15)
            org.telegram.tgnet.TLRPC$EncryptedChat r0 = r0.getEncryptedChat(r1)
            if (r0 != 0) goto L_0x0292
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x027d
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "not found secret chat to show dialog notification "
            r0.append(r1)
            r0.append(r15)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.w(r0)
        L_0x027d:
            r71 = r2
            r5 = r4
        L_0x0280:
            r78 = r23
            r3 = r24
            r15 = r29
            r2 = r33
            r1 = 26
            r18 = 1
            r20 = 0
            r23 = 27
            goto L_0x0CLASSNAME
        L_0x0292:
            org.telegram.messenger.MessagesController r1 = r76.getMessagesController()
            int r9 = r0.user_id
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r9)
            if (r1 != 0) goto L_0x02bd
            boolean r1 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r1 == 0) goto L_0x027d
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "not found secret chat user to show dialog notification "
            r1.append(r3)
            int r0 = r0.user_id
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            org.telegram.messenger.FileLog.w(r0)
            goto L_0x027d
        L_0x02bd:
            r0 = r1
            goto L_0x02c0
        L_0x02bf:
            r0 = 0
        L_0x02c0:
            r1 = 2131626805(0x7f0e0b35, float:1.8880857E38)
            java.lang.String r9 = "SecretChatName"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r9, r1)
            r27 = r0
            r35 = r1
            r0 = 0
            r9 = 0
            r10 = 0
            r32 = 0
        L_0x02d2:
            r34 = 0
            r37 = 0
        L_0x02d6:
            java.lang.String r1 = "NotificationHiddenChatName"
            r39 = r10
            java.lang.String r10 = "NotificationHiddenName"
            if (r16 == 0) goto L_0x0302
            if (r11 >= 0) goto L_0x02ea
            r41 = r4
            r4 = 2131626022(0x7f0e0826, float:1.8879268E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r4)
            goto L_0x02f3
        L_0x02ea:
            r41 = r4
            r4 = 2131626025(0x7f0e0829, float:1.8879275E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r10, r4)
        L_0x02f3:
            r35 = r0
            r42 = r10
            r32 = r13
            r4 = 0
            r13 = 0
            r74 = r35
            r35 = r15
            r15 = r74
            goto L_0x0313
        L_0x0302:
            r41 = r4
            r4 = r0
            r42 = r10
            r74 = r32
            r32 = r13
            r13 = r74
            r75 = r35
            r35 = r15
            r15 = r75
        L_0x0313:
            if (r4 == 0) goto L_0x0369
            r10 = 1
            java.io.File r0 = org.telegram.messenger.FileLoader.getPathToAttach(r4, r10)
            int r10 = android.os.Build.VERSION.SDK_INT
            r44 = r1
            r1 = 28
            if (r10 >= r1) goto L_0x0363
            org.telegram.messenger.ImageLoader r1 = org.telegram.messenger.ImageLoader.getInstance()
            java.lang.String r10 = "50_50"
            r45 = r9
            r9 = 0
            android.graphics.drawable.BitmapDrawable r1 = r1.getImageFromMemory(r4, r9, r10)
            if (r1 == 0) goto L_0x0338
            android.graphics.Bitmap r1 = r1.getBitmap()
        L_0x0335:
            r10 = r1
            r1 = r0
            goto L_0x0370
        L_0x0338:
            boolean r1 = r0.exists()     // Catch:{ all -> 0x0366 }
            if (r1 == 0) goto L_0x0361
            r1 = 1126170624(0x43200000, float:160.0)
            r10 = 1112014848(0x42480000, float:50.0)
            int r10 = org.telegram.messenger.AndroidUtilities.dp(r10)     // Catch:{ all -> 0x0366 }
            float r10 = (float) r10     // Catch:{ all -> 0x0366 }
            float r1 = r1 / r10
            android.graphics.BitmapFactory$Options r10 = new android.graphics.BitmapFactory$Options     // Catch:{ all -> 0x0366 }
            r10.<init>()     // Catch:{ all -> 0x0366 }
            r17 = 1065353216(0x3var_, float:1.0)
            int r17 = (r1 > r17 ? 1 : (r1 == r17 ? 0 : -1))
            if (r17 >= 0) goto L_0x0355
            r1 = 1
            goto L_0x0356
        L_0x0355:
            int r1 = (int) r1     // Catch:{ all -> 0x0366 }
        L_0x0356:
            r10.inSampleSize = r1     // Catch:{ all -> 0x0366 }
            java.lang.String r1 = r0.getAbsolutePath()     // Catch:{ all -> 0x0366 }
            android.graphics.Bitmap r1 = android.graphics.BitmapFactory.decodeFile(r1, r10)     // Catch:{ all -> 0x0366 }
            goto L_0x0335
        L_0x0361:
            r1 = r9
            goto L_0x0335
        L_0x0363:
            r45 = r9
            r9 = 0
        L_0x0366:
            r1 = r0
            r10 = r9
            goto L_0x0370
        L_0x0369:
            r44 = r1
            r45 = r9
            r9 = 0
            r1 = r9
            r10 = r1
        L_0x0370:
            java.lang.String r9 = "dialog_id"
            r46 = r4
            java.lang.String r4 = "max_id"
            r47 = r1
            java.lang.String r1 = "currentAccount"
            if (r37 == 0) goto L_0x037e
            if (r34 == 0) goto L_0x0404
        L_0x037e:
            if (r13 == 0) goto L_0x0404
            boolean r0 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r0 != 0) goto L_0x0404
            if (r2 == r11) goto L_0x0404
            android.content.Intent r0 = new android.content.Intent
            r48 = r13
            android.content.Context r13 = org.telegram.messenger.ApplicationLoader.applicationContext
            r49 = r10
            java.lang.Class<org.telegram.messenger.WearReplyReceiver> r10 = org.telegram.messenger.WearReplyReceiver.class
            r0.<init>(r13, r10)
            r0.putExtra(r9, r5)
            r0.putExtra(r4, r12)
            int r10 = r8.currentAccount
            r0.putExtra(r1, r10)
            android.content.Context r10 = org.telegram.messenger.ApplicationLoader.applicationContext
            int r13 = r14.intValue()
            r50 = r14
            r14 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r0 = android.app.PendingIntent.getBroadcast(r10, r13, r0, r14)
            androidx.core.app.RemoteInput$Builder r10 = new androidx.core.app.RemoteInput$Builder
            java.lang.String r13 = "extra_voice_reply"
            r10.<init>(r13)
            r13 = 2131626673(0x7f0e0ab1, float:1.8880589E38)
            java.lang.String r14 = "Reply"
            java.lang.String r13 = org.telegram.messenger.LocaleController.getString(r14, r13)
            r10.setLabel(r13)
            androidx.core.app.RemoteInput r10 = r10.build()
            if (r11 >= 0) goto L_0x03d7
            r14 = 1
            java.lang.Object[] r13 = new java.lang.Object[r14]
            r14 = 0
            r13[r14] = r15
            java.lang.String r14 = "ReplyToGroup"
            r51 = r4
            r4 = 2131626674(0x7f0e0ab2, float:1.888059E38)
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatString(r14, r4, r13)
            goto L_0x03e8
        L_0x03d7:
            r51 = r4
            r4 = 2131626675(0x7f0e0ab3, float:1.8880593E38)
            r13 = 1
            java.lang.Object[] r14 = new java.lang.Object[r13]
            r13 = 0
            r14[r13] = r15
            java.lang.String r13 = "ReplyToUser"
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatString(r13, r4, r14)
        L_0x03e8:
            androidx.core.app.NotificationCompat$Action$Builder r13 = new androidx.core.app.NotificationCompat$Action$Builder
            r14 = 2131165485(0x7var_d, float:1.7945188E38)
            r13.<init>(r14, r4, r0)
            r4 = 1
            r13.setAllowGeneratedReplies(r4)
            r13.setSemanticAction(r4)
            r13.addRemoteInput(r10)
            r4 = 0
            r13.setShowsUserInterface(r4)
            androidx.core.app.NotificationCompat$Action r0 = r13.build()
            r10 = r0
            goto L_0x040e
        L_0x0404:
            r51 = r4
            r49 = r10
            r48 = r13
            r50 = r14
            r4 = 0
            r10 = 0
        L_0x040e:
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.pushDialogs
            java.lang.Object r0 = r0.get(r5)
            java.lang.Integer r0 = (java.lang.Integer) r0
            if (r0 != 0) goto L_0x041c
            java.lang.Integer r0 = java.lang.Integer.valueOf(r4)
        L_0x041c:
            int r0 = r0.intValue()
            int r4 = r3.size()
            int r0 = java.lang.Math.max(r0, r4)
            r4 = 2
            r13 = 1
            if (r0 <= r13) goto L_0x0449
            int r14 = android.os.Build.VERSION.SDK_INT
            r13 = 28
            if (r14 < r13) goto L_0x0433
            goto L_0x0449
        L_0x0433:
            java.lang.Object[] r13 = new java.lang.Object[r4]
            r14 = 0
            r13[r14] = r15
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r14 = 1
            r13[r14] = r0
            java.lang.String r0 = "%1$s (%2$d)"
            java.lang.String r0 = java.lang.String.format(r0, r13)
            r13 = r0
            r52 = r5
            goto L_0x044c
        L_0x0449:
            r52 = r5
            r13 = r15
        L_0x044c:
            long r4 = (long) r2
            java.lang.Object r0 = r7.get(r4)
            r14 = r0
            androidx.core.app.Person r14 = (androidx.core.app.Person) r14
            int r0 = android.os.Build.VERSION.SDK_INT
            r6 = 28
            if (r0 < r6) goto L_0x04c8
            if (r14 != 0) goto L_0x04c8
            org.telegram.messenger.MessagesController r0 = r76.getMessagesController()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r6)
            if (r0 != 0) goto L_0x0472
            org.telegram.messenger.UserConfig r0 = r76.getUserConfig()
            org.telegram.tgnet.TLRPC$User r0 = r0.getCurrentUser()
        L_0x0472:
            if (r0 == 0) goto L_0x04c8
            org.telegram.tgnet.TLRPC$UserProfilePhoto r6 = r0.photo     // Catch:{ all -> 0x04bf }
            if (r6 == 0) goto L_0x04c8
            org.telegram.tgnet.TLRPC$UserProfilePhoto r6 = r0.photo     // Catch:{ all -> 0x04bf }
            org.telegram.tgnet.TLRPC$FileLocation r6 = r6.photo_small     // Catch:{ all -> 0x04bf }
            if (r6 == 0) goto L_0x04c8
            org.telegram.tgnet.TLRPC$UserProfilePhoto r6 = r0.photo     // Catch:{ all -> 0x04bf }
            org.telegram.tgnet.TLRPC$FileLocation r6 = r6.photo_small     // Catch:{ all -> 0x04bf }
            r54 = r9
            r55 = r10
            long r9 = r6.volume_id     // Catch:{ all -> 0x04bd }
            int r6 = (r9 > r30 ? 1 : (r9 == r30 ? 0 : -1))
            if (r6 == 0) goto L_0x04cc
            org.telegram.tgnet.TLRPC$UserProfilePhoto r6 = r0.photo     // Catch:{ all -> 0x04bd }
            org.telegram.tgnet.TLRPC$FileLocation r6 = r6.photo_small     // Catch:{ all -> 0x04bd }
            int r6 = r6.local_id     // Catch:{ all -> 0x04bd }
            if (r6 == 0) goto L_0x04cc
            androidx.core.app.Person$Builder r6 = new androidx.core.app.Person$Builder     // Catch:{ all -> 0x04bd }
            r6.<init>()     // Catch:{ all -> 0x04bd }
            java.lang.String r9 = "FromYou"
            r10 = 2131625456(0x7f0e05f0, float:1.887812E38)
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r9, r10)     // Catch:{ all -> 0x04bd }
            r6.setName(r9)     // Catch:{ all -> 0x04bd }
            org.telegram.tgnet.TLRPC$UserProfilePhoto r0 = r0.photo     // Catch:{ all -> 0x04bd }
            org.telegram.tgnet.TLRPC$FileLocation r0 = r0.photo_small     // Catch:{ all -> 0x04bd }
            r9 = 1
            java.io.File r0 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r9)     // Catch:{ all -> 0x04bd }
            r8.loadRoundAvatar(r0, r6)     // Catch:{ all -> 0x04bd }
            androidx.core.app.Person r6 = r6.build()     // Catch:{ all -> 0x04bd }
            r7.put(r4, r6)     // Catch:{ all -> 0x04ba }
            r14 = r6
            goto L_0x04cc
        L_0x04ba:
            r0 = move-exception
            r14 = r6
            goto L_0x04c4
        L_0x04bd:
            r0 = move-exception
            goto L_0x04c4
        L_0x04bf:
            r0 = move-exception
            r54 = r9
            r55 = r10
        L_0x04c4:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x04cc
        L_0x04c8:
            r54 = r9
            r55 = r10
        L_0x04cc:
            java.lang.String r9 = ""
            if (r14 == 0) goto L_0x04d6
            androidx.core.app.NotificationCompat$MessagingStyle r0 = new androidx.core.app.NotificationCompat$MessagingStyle
            r0.<init>((androidx.core.app.Person) r14)
            goto L_0x04db
        L_0x04d6:
            androidx.core.app.NotificationCompat$MessagingStyle r0 = new androidx.core.app.NotificationCompat$MessagingStyle
            r0.<init>((java.lang.CharSequence) r9)
        L_0x04db:
            r10 = r0
            int r0 = android.os.Build.VERSION.SDK_INT
            r6 = 28
            if (r0 < r6) goto L_0x04e6
            if (r11 >= 0) goto L_0x04e9
            if (r37 != 0) goto L_0x04e9
        L_0x04e6:
            r10.setConversationTitle(r13)
        L_0x04e9:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r6) goto L_0x04f4
            if (r37 != 0) goto L_0x04f2
            if (r11 >= 0) goto L_0x04f2
            goto L_0x04f4
        L_0x04f2:
            r0 = 0
            goto L_0x04f5
        L_0x04f4:
            r0 = 1
        L_0x04f5:
            r10.setGroupConversation(r0)
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            r6 = 1
            java.lang.String[] r14 = new java.lang.String[r6]
            r56 = r2
            boolean[] r2 = new boolean[r6]
            if (r45 == 0) goto L_0x050f
            org.json.JSONArray r0 = new org.json.JSONArray
            r0.<init>()
            r57 = r12
            r12 = r0
            goto L_0x0512
        L_0x050f:
            r57 = r12
            r12 = 0
        L_0x0512:
            int r0 = r3.size()
            int r0 = r0 - r6
            r6 = r0
            r58 = 0
            r59 = 0
        L_0x051c:
            r60 = 1000(0x3e8, double:4.94E-321)
            if (r6 < 0) goto L_0x08ac
            java.lang.Object r0 = r3.get(r6)
            r62 = r6
            r6 = r0
            org.telegram.messenger.MessageObject r6 = (org.telegram.messenger.MessageObject) r6
            java.lang.String r0 = r8.getShortStringForMessage(r6, r14, r2)
            int r63 = (r52 > r4 ? 1 : (r52 == r4 ? 0 : -1))
            if (r63 != 0) goto L_0x0536
            r21 = 0
            r14[r21] = r15
            goto L_0x0553
        L_0x0536:
            r21 = 0
            if (r11 >= 0) goto L_0x0553
            r63 = r15
            org.telegram.tgnet.TLRPC$Message r15 = r6.messageOwner
            boolean r15 = r15.from_scheduled
            if (r15 == 0) goto L_0x0550
            r15 = 2131626068(0x7f0e0854, float:1.8879362E38)
            r64 = r3
            java.lang.String r3 = "NotificationMessageScheduledName"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r15)
            r14[r21] = r3
            goto L_0x0557
        L_0x0550:
            r64 = r3
            goto L_0x0557
        L_0x0553:
            r64 = r3
            r63 = r15
        L_0x0557:
            if (r0 != 0) goto L_0x059d
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x058d
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "message text is null for "
            r0.append(r3)
            int r3 = r6.getId()
            r0.append(r3)
            java.lang.String r3 = " did = "
            r0.append(r3)
            r3 = r9
            r15 = r10
            long r9 = r6.getDialogId()
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.w(r0)
            r68 = r2
            r65 = r4
            r67 = r13
            r40 = r14
            r4 = r15
            goto L_0x0597
        L_0x058d:
            r3 = r9
            r68 = r2
            r65 = r4
            r4 = r10
            r67 = r13
            r40 = r14
        L_0x0597:
            r69 = r42
            r9 = r62
            goto L_0x0898
        L_0x059d:
            r3 = r9
            r15 = r10
            int r9 = r13.length()
            if (r9 <= 0) goto L_0x05aa
            java.lang.String r9 = "\n\n"
            r13.append(r9)
        L_0x05aa:
            int r9 = (r52 > r4 ? 1 : (r52 == r4 ? 0 : -1))
            if (r9 == 0) goto L_0x05d6
            org.telegram.tgnet.TLRPC$Message r9 = r6.messageOwner
            boolean r9 = r9.from_scheduled
            if (r9 == 0) goto L_0x05d6
            if (r11 <= 0) goto L_0x05d6
            r9 = 2
            java.lang.Object[] r10 = new java.lang.Object[r9]
            r65 = r4
            r9 = r62
            r4 = 2131626068(0x7f0e0854, float:1.8879362E38)
            java.lang.String r5 = "NotificationMessageScheduledName"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r5 = 0
            r10[r5] = r4
            r4 = 1
            r10[r4] = r0
            java.lang.String r0 = "%1$s: %2$s"
            java.lang.String r0 = java.lang.String.format(r0, r10)
            r13.append(r0)
            goto L_0x05f6
        L_0x05d6:
            r65 = r4
            r9 = r62
            r5 = 0
            r4 = r14[r5]
            if (r4 == 0) goto L_0x05f3
            r4 = 2
            java.lang.Object[] r10 = new java.lang.Object[r4]
            r21 = r14[r5]
            r10[r5] = r21
            r5 = 1
            r10[r5] = r0
            java.lang.String r5 = "%1$s: %2$s"
            java.lang.String r5 = java.lang.String.format(r5, r10)
            r13.append(r5)
            goto L_0x05f6
        L_0x05f3:
            r13.append(r0)
        L_0x05f6:
            r5 = r0
            if (r11 <= 0) goto L_0x05fc
            r10 = r5
            long r4 = (long) r11
            goto L_0x060b
        L_0x05fc:
            r10 = r5
            if (r37 == 0) goto L_0x0602
            int r0 = -r11
        L_0x0600:
            long r4 = (long) r0
            goto L_0x060b
        L_0x0602:
            if (r11 >= 0) goto L_0x0609
            int r0 = r6.getFromId()
            goto L_0x0600
        L_0x0609:
            r4 = r52
        L_0x060b:
            java.lang.Object r0 = r7.get(r4)
            androidx.core.app.Person r0 = (androidx.core.app.Person) r0
            r21 = 0
            r62 = r14[r21]
            if (r62 != 0) goto L_0x0664
            if (r16 == 0) goto L_0x0659
            if (r11 >= 0) goto L_0x0645
            if (r37 == 0) goto L_0x0633
            r62 = r15
            int r15 = android.os.Build.VERSION.SDK_INT
            r67 = r13
            r13 = 27
            if (r15 <= r13) goto L_0x065d
            r13 = r44
            r15 = 2131626022(0x7f0e0826, float:1.8879268E38)
            java.lang.String r38 = org.telegram.messenger.LocaleController.getString(r13, r15)
            r13 = r38
            goto L_0x0642
        L_0x0633:
            r67 = r13
            r62 = r15
            r13 = r44
            r15 = 2131626023(0x7f0e0827, float:1.887927E38)
            java.lang.String r13 = "NotificationHiddenChatUserName"
            java.lang.String r13 = org.telegram.messenger.LocaleController.getString(r13, r15)
        L_0x0642:
            r15 = r42
            goto L_0x0673
        L_0x0645:
            r67 = r13
            r62 = r15
            int r13 = android.os.Build.VERSION.SDK_INT
            r15 = 27
            if (r13 <= r15) goto L_0x065d
            r15 = r42
            r13 = 2131626025(0x7f0e0829, float:1.8879275E38)
            java.lang.String r40 = org.telegram.messenger.LocaleController.getString(r15, r13)
            goto L_0x0671
        L_0x0659:
            r67 = r13
            r62 = r15
        L_0x065d:
            r15 = r42
            r13 = 2131626025(0x7f0e0829, float:1.8879275E38)
            r13 = r3
            goto L_0x0673
        L_0x0664:
            r67 = r13
            r62 = r15
            r15 = r42
            r13 = 2131626025(0x7f0e0829, float:1.8879275E38)
            r21 = 0
            r40 = r14[r21]
        L_0x0671:
            r13 = r40
        L_0x0673:
            r40 = r14
            if (r0 == 0) goto L_0x0687
            java.lang.CharSequence r14 = r0.getName()
            boolean r14 = android.text.TextUtils.equals(r14, r13)
            if (r14 != 0) goto L_0x0682
            goto L_0x0687
        L_0x0682:
            r68 = r2
        L_0x0684:
            r2 = r0
            goto L_0x06f9
        L_0x0687:
            androidx.core.app.Person$Builder r0 = new androidx.core.app.Person$Builder
            r0.<init>()
            r0.setName(r13)
            r13 = 0
            boolean r14 = r2[r13]
            if (r14 == 0) goto L_0x06ef
            if (r11 == 0) goto L_0x06ef
            int r13 = android.os.Build.VERSION.SDK_INT
            r14 = 28
            if (r13 < r14) goto L_0x06ef
            if (r11 > 0) goto L_0x06e7
            if (r37 == 0) goto L_0x06a1
            goto L_0x06e7
        L_0x06a1:
            if (r11 >= 0) goto L_0x06e3
            int r13 = r6.getFromId()
            org.telegram.messenger.MessagesController r14 = r76.getMessagesController()
            r68 = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r13)
            org.telegram.tgnet.TLRPC$User r2 = r14.getUser(r2)
            if (r2 != 0) goto L_0x06c9
            org.telegram.messenger.MessagesStorage r2 = r76.getMessagesStorage()
            org.telegram.tgnet.TLRPC$User r2 = r2.getUserSync(r13)
            if (r2 == 0) goto L_0x06c9
            org.telegram.messenger.MessagesController r13 = r76.getMessagesController()
            r14 = 1
            r13.putUser(r2, r14)
        L_0x06c9:
            if (r2 == 0) goto L_0x06e5
            org.telegram.tgnet.TLRPC$UserProfilePhoto r2 = r2.photo
            if (r2 == 0) goto L_0x06e5
            org.telegram.tgnet.TLRPC$FileLocation r2 = r2.photo_small
            if (r2 == 0) goto L_0x06e5
            long r13 = r2.volume_id
            int r69 = (r13 > r30 ? 1 : (r13 == r30 ? 0 : -1))
            if (r69 == 0) goto L_0x06e5
            int r13 = r2.local_id
            if (r13 == 0) goto L_0x06e5
            r13 = 1
            java.io.File r2 = org.telegram.messenger.FileLoader.getPathToAttach(r2, r13)
            goto L_0x06eb
        L_0x06e3:
            r68 = r2
        L_0x06e5:
            r2 = 0
            goto L_0x06eb
        L_0x06e7:
            r68 = r2
            r2 = r47
        L_0x06eb:
            r8.loadRoundAvatar(r2, r0)
            goto L_0x06f1
        L_0x06ef:
            r68 = r2
        L_0x06f1:
            androidx.core.app.Person r0 = r0.build()
            r7.put(r4, r0)
            goto L_0x0684
        L_0x06f9:
            if (r11 == 0) goto L_0x0831
            int r0 = android.os.Build.VERSION.SDK_INT
            r4 = 28
            if (r0 < r4) goto L_0x07df
            android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.String r5 = "activity"
            java.lang.Object r0 = r0.getSystemService(r5)
            android.app.ActivityManager r0 = (android.app.ActivityManager) r0
            boolean r0 = r0.isLowRamDevice()
            if (r0 != 0) goto L_0x07df
            if (r16 != 0) goto L_0x07df
            boolean r0 = r6.isSecretMedia()
            if (r0 != 0) goto L_0x07df
            int r0 = r6.type
            r5 = 1
            if (r0 == r5) goto L_0x0724
            boolean r0 = r6.isSticker()
            if (r0 == 0) goto L_0x07df
        L_0x0724:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            java.io.File r0 = org.telegram.messenger.FileLoader.getPathToMessage(r0)
            androidx.core.app.NotificationCompat$MessagingStyle$Message r5 = new androidx.core.app.NotificationCompat$MessagingStyle$Message
            org.telegram.tgnet.TLRPC$Message r13 = r6.messageOwner
            int r13 = r13.date
            long r13 = (long) r13
            long r13 = r13 * r60
            r5.<init>(r10, r13, r2)
            boolean r13 = r6.isSticker()
            if (r13 == 0) goto L_0x073f
            java.lang.String r13 = "image/webp"
            goto L_0x0741
        L_0x073f:
            java.lang.String r13 = "image/jpeg"
        L_0x0741:
            boolean r14 = r0.exists()
            if (r14 == 0) goto L_0x0757
            android.content.Context r14 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0752 }
            java.lang.String r4 = "org.telegram.messenger.beta.provider"
            android.net.Uri r0 = androidx.core.content.FileProvider.getUriForFile(r14, r4, r0)     // Catch:{ Exception -> 0x0752 }
            r69 = r15
            goto L_0x07ad
        L_0x0752:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x07aa
        L_0x0757:
            org.telegram.messenger.FileLoader r4 = r76.getFileLoader()
            java.lang.String r14 = r0.getName()
            boolean r4 = r4.isLoadingFile(r14)
            if (r4 == 0) goto L_0x07aa
            android.net.Uri$Builder r4 = new android.net.Uri$Builder
            r4.<init>()
            java.lang.String r14 = "content"
            android.net.Uri$Builder r4 = r4.scheme(r14)
            java.lang.String r14 = "org.telegram.messenger.beta.notification_image_provider"
            android.net.Uri$Builder r4 = r4.authority(r14)
            java.lang.String r14 = "msg_media_raw"
            android.net.Uri$Builder r4 = r4.appendPath(r14)
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r69 = r15
            int r15 = r8.currentAccount
            r14.append(r15)
            r14.append(r3)
            java.lang.String r14 = r14.toString()
            android.net.Uri$Builder r4 = r4.appendPath(r14)
            java.lang.String r14 = r0.getName()
            android.net.Uri$Builder r4 = r4.appendPath(r14)
            java.lang.String r0 = r0.getAbsolutePath()
            java.lang.String r14 = "final_path"
            android.net.Uri$Builder r0 = r4.appendQueryParameter(r14, r0)
            android.net.Uri r0 = r0.build()
            goto L_0x07ad
        L_0x07aa:
            r69 = r15
            r0 = 0
        L_0x07ad:
            if (r0 == 0) goto L_0x07e1
            r5.setData(r13, r0)
            r4 = r62
            r4.addMessage(r5)
            android.content.Context r5 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.String r13 = "com.android.systemui"
            r14 = 1
            r5.grantUriPermission(r13, r0, r14)
            org.telegram.messenger.-$$Lambda$NotificationsController$2iZFI3opoasnRhiUslwS5Iqt9vs r5 = new org.telegram.messenger.-$$Lambda$NotificationsController$2iZFI3opoasnRhiUslwS5Iqt9vs
            r5.<init>(r0)
            r13 = 20000(0x4e20, double:9.8813E-320)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r5, r13)
            java.lang.CharSequence r0 = r6.caption
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x07dd
            java.lang.CharSequence r0 = r6.caption
            org.telegram.tgnet.TLRPC$Message r5 = r6.messageOwner
            int r5 = r5.date
            long r13 = (long) r5
            long r13 = r13 * r60
            r4.addMessage(r0, r13, r2)
        L_0x07dd:
            r0 = 1
            goto L_0x07e4
        L_0x07df:
            r69 = r15
        L_0x07e1:
            r4 = r62
            r0 = 0
        L_0x07e4:
            if (r0 != 0) goto L_0x07f0
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            int r0 = r0.date
            long r13 = (long) r0
            long r13 = r13 * r60
            r4.addMessage(r10, r13, r2)
        L_0x07f0:
            if (r16 != 0) goto L_0x083f
            boolean r0 = r6.isVoice()
            if (r0 == 0) goto L_0x083f
            java.util.List r0 = r4.getMessages()
            boolean r2 = r0.isEmpty()
            if (r2 != 0) goto L_0x083f
            org.telegram.tgnet.TLRPC$Message r2 = r6.messageOwner
            java.io.File r2 = org.telegram.messenger.FileLoader.getPathToMessage(r2)
            int r5 = android.os.Build.VERSION.SDK_INT
            r13 = 24
            if (r5 < r13) goto L_0x0819
            android.content.Context r5 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0817 }
            java.lang.String r13 = "org.telegram.messenger.beta.provider"
            android.net.Uri r2 = androidx.core.content.FileProvider.getUriForFile(r5, r13, r2)     // Catch:{ Exception -> 0x0817 }
            goto L_0x081d
        L_0x0817:
            r2 = 0
            goto L_0x081d
        L_0x0819:
            android.net.Uri r2 = android.net.Uri.fromFile(r2)
        L_0x081d:
            if (r2 == 0) goto L_0x083f
            int r5 = r0.size()
            r13 = 1
            int r5 = r5 - r13
            java.lang.Object r0 = r0.get(r5)
            androidx.core.app.NotificationCompat$MessagingStyle$Message r0 = (androidx.core.app.NotificationCompat.MessagingStyle.Message) r0
            java.lang.String r5 = "audio/ogg"
            r0.setData(r5, r2)
            goto L_0x083f
        L_0x0831:
            r69 = r15
            r4 = r62
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            int r0 = r0.date
            long r13 = (long) r0
            long r13 = r13 * r60
            r4.addMessage(r10, r13, r2)
        L_0x083f:
            if (r12 == 0) goto L_0x0881
            org.json.JSONObject r0 = new org.json.JSONObject     // Catch:{ JSONException -> 0x0880 }
            r0.<init>()     // Catch:{ JSONException -> 0x0880 }
            java.lang.String r2 = "text"
            r0.put(r2, r10)     // Catch:{ JSONException -> 0x0880 }
            java.lang.String r2 = "date"
            org.telegram.tgnet.TLRPC$Message r5 = r6.messageOwner     // Catch:{ JSONException -> 0x0880 }
            int r5 = r5.date     // Catch:{ JSONException -> 0x0880 }
            r0.put(r2, r5)     // Catch:{ JSONException -> 0x0880 }
            boolean r2 = r6.isFromUser()     // Catch:{ JSONException -> 0x0880 }
            if (r2 == 0) goto L_0x087c
            if (r11 >= 0) goto L_0x087c
            org.telegram.messenger.MessagesController r2 = r76.getMessagesController()     // Catch:{ JSONException -> 0x0880 }
            int r5 = r6.getFromId()     // Catch:{ JSONException -> 0x0880 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ JSONException -> 0x0880 }
            org.telegram.tgnet.TLRPC$User r2 = r2.getUser(r5)     // Catch:{ JSONException -> 0x0880 }
            if (r2 == 0) goto L_0x087c
            java.lang.String r5 = "fname"
            java.lang.String r10 = r2.first_name     // Catch:{ JSONException -> 0x0880 }
            r0.put(r5, r10)     // Catch:{ JSONException -> 0x0880 }
            java.lang.String r5 = "lname"
            java.lang.String r2 = r2.last_name     // Catch:{ JSONException -> 0x0880 }
            r0.put(r5, r2)     // Catch:{ JSONException -> 0x0880 }
        L_0x087c:
            r12.put(r0)     // Catch:{ JSONException -> 0x0880 }
            goto L_0x0881
        L_0x0880:
        L_0x0881:
            r13 = 777000(0xbdb28, double:3.83889E-318)
            int r0 = (r52 > r13 ? 1 : (r52 == r13 ? 0 : -1))
            if (r0 != 0) goto L_0x0898
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$ReplyMarkup r0 = r0.reply_markup
            if (r0 == 0) goto L_0x0898
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_keyboardButtonRow> r0 = r0.rows
            int r2 = r6.getId()
            r59 = r0
            r58 = r2
        L_0x0898:
            int r6 = r9 + -1
            r9 = r3
            r10 = r4
            r14 = r40
            r15 = r63
            r3 = r64
            r4 = r65
            r13 = r67
            r2 = r68
            r42 = r69
            goto L_0x051c
        L_0x08ac:
            r64 = r3
            r3 = r9
            r4 = r10
            r67 = r13
            r63 = r15
            android.content.Intent r0 = new android.content.Intent
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.Class<org.telegram.ui.LaunchActivity> r5 = org.telegram.ui.LaunchActivity.class
            r0.<init>(r2, r5)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "com.tmessages.openchat"
            r2.append(r5)
            double r5 = java.lang.Math.random()
            r2.append(r5)
            r5 = 2147483647(0x7fffffff, float:NaN)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            r0.setAction(r2)
            r2 = 32768(0x8000, float:4.5918E-41)
            r0.setFlags(r2)
            java.lang.String r2 = "android.intent.category.LAUNCHER"
            r0.addCategory(r2)
            if (r11 == 0) goto L_0x08f9
            if (r11 <= 0) goto L_0x08f0
            java.lang.String r2 = "userId"
            r0.putExtra(r2, r11)
            goto L_0x08f6
        L_0x08f0:
            int r2 = -r11
            java.lang.String r5 = "chatId"
            r0.putExtra(r5, r2)
        L_0x08f6:
            r5 = r35
            goto L_0x0900
        L_0x08f9:
            java.lang.String r2 = "encId"
            r5 = r35
            r0.putExtra(r2, r5)
        L_0x0900:
            int r2 = r8.currentAccount
            r0.putExtra(r1, r2)
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            r6 = 1073741824(0x40000000, float:2.0)
            r9 = 0
            android.app.PendingIntent r0 = android.app.PendingIntent.getActivity(r2, r9, r0, r6)
            androidx.core.app.NotificationCompat$WearableExtender r2 = new androidx.core.app.NotificationCompat$WearableExtender
            r2.<init>()
            r6 = r55
            if (r55 == 0) goto L_0x091a
            r2.addAction(r6)
        L_0x091a:
            android.content.Intent r9 = new android.content.Intent
            android.content.Context r10 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.Class<org.telegram.messenger.AutoMessageHeardReceiver> r13 = org.telegram.messenger.AutoMessageHeardReceiver.class
            r9.<init>(r10, r13)
            r10 = 32
            r9.addFlags(r10)
            java.lang.String r10 = "org.telegram.messenger.ACTION_MESSAGE_HEARD"
            r9.setAction(r10)
            r13 = r52
            r10 = r54
            r9.putExtra(r10, r13)
            r15 = r51
            r10 = r57
            r9.putExtra(r15, r10)
            int r15 = r8.currentAccount
            r9.putExtra(r1, r15)
            android.content.Context r15 = org.telegram.messenger.ApplicationLoader.applicationContext
            r26 = r12
            int r12 = r50.intValue()
            r30 = r7
            r7 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r9 = android.app.PendingIntent.getBroadcast(r15, r12, r9, r7)
            androidx.core.app.NotificationCompat$Action$Builder r7 = new androidx.core.app.NotificationCompat$Action$Builder
            r12 = 2131165628(0x7var_bc, float:1.7945478E38)
            r15 = 2131625744(0x7f0e0710, float:1.8878705E38)
            r55 = r6
            java.lang.String r6 = "MarkAsRead"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r6, r15)
            r7.<init>(r12, r6, r9)
            r6 = 2
            r7.setSemanticAction(r6)
            r6 = 0
            r7.setShowsUserInterface(r6)
            androidx.core.app.NotificationCompat$Action r6 = r7.build()
            java.lang.String r9 = "_"
            if (r11 == 0) goto L_0x09a6
            if (r11 <= 0) goto L_0x098d
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "tguser"
            r5.append(r7)
            r5.append(r11)
            r5.append(r9)
            r5.append(r10)
            java.lang.String r5 = r5.toString()
            goto L_0x09c5
        L_0x098d:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "tgchat"
            r5.append(r7)
            int r7 = -r11
            r5.append(r7)
            r5.append(r9)
            r5.append(r10)
            java.lang.String r5 = r5.toString()
            goto L_0x09c5
        L_0x09a6:
            long r42 = globalSecretChatId
            int r7 = (r13 > r42 ? 1 : (r13 == r42 ? 0 : -1))
            if (r7 == 0) goto L_0x09c4
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r12 = "tgenc"
            r7.append(r12)
            r7.append(r5)
            r7.append(r9)
            r7.append(r10)
            java.lang.String r5 = r7.toString()
            goto L_0x09c5
        L_0x09c4:
            r5 = 0
        L_0x09c5:
            if (r5 == 0) goto L_0x09e9
            r2.setDismissalId(r5)
            androidx.core.app.NotificationCompat$WearableExtender r7 = new androidx.core.app.NotificationCompat$WearableExtender
            r7.<init>()
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r15 = "summary_"
            r12.append(r15)
            r12.append(r5)
            java.lang.String r5 = r12.toString()
            r7.setDismissalId(r5)
            r12 = r77
            r12.extend(r7)
            goto L_0x09eb
        L_0x09e9:
            r12 = r77
        L_0x09eb:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "tgaccount"
            r5.append(r7)
            r7 = r56
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            r2.setBridgeTag(r5)
            r5 = r64
            r15 = 0
            java.lang.Object r31 = r5.get(r15)
            r15 = r31
            org.telegram.messenger.MessageObject r15 = (org.telegram.messenger.MessageObject) r15
            org.telegram.tgnet.TLRPC$Message r15 = r15.messageOwner
            int r15 = r15.date
            r31 = r9
            r57 = r10
            long r9 = (long) r15
            long r9 = r9 * r60
            androidx.core.app.NotificationCompat$Builder r15 = new androidx.core.app.NotificationCompat$Builder
            android.content.Context r7 = org.telegram.messenger.ApplicationLoader.applicationContext
            r15.<init>(r7)
            r7 = r63
            r15.setContentTitle(r7)
            r12 = 2131165759(0x7var_f, float:1.7945744E38)
            r15.setSmallIcon(r12)
            java.lang.String r12 = r67.toString()
            r15.setContentText(r12)
            r12 = 1
            r15.setAutoCancel(r12)
            int r5 = r5.size()
            r15.setNumber(r5)
            r5 = -15618822(0xfffffffffvar_acfa, float:-1.936362E38)
            r15.setColor(r5)
            r5 = 0
            r15.setGroupSummary(r5)
            r15.setWhen(r9)
            r15.setShowWhen(r12)
            r15.setStyle(r4)
            r15.setContentIntent(r0)
            r15.extend(r2)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r3)
            r2 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
            long r2 = r2 - r9
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            r15.setSortKey(r0)
            java.lang.String r0 = "msg"
            r15.setCategory(r0)
            android.content.Intent r0 = new android.content.Intent
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            java.lang.Class<org.telegram.messenger.NotificationDismissReceiver> r3 = org.telegram.messenger.NotificationDismissReceiver.class
            r0.<init>(r2, r3)
            java.lang.String r2 = "messageDate"
            r9 = r32
            r0.putExtra(r2, r9)
            java.lang.String r2 = "dialogId"
            r0.putExtra(r2, r13)
            int r2 = r8.currentAccount
            r0.putExtra(r1, r2)
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            int r3 = r50.intValue()
            r4 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r0 = android.app.PendingIntent.getBroadcast(r2, r3, r0, r4)
            r15.setDeleteIntent(r0)
            if (r41 == 0) goto L_0x0aa5
            java.lang.String r0 = r8.notificationGroup
            r15.setGroup(r0)
            r2 = 1
            r15.setGroupAlertBehavior(r2)
        L_0x0aa5:
            if (r55 == 0) goto L_0x0aac
            r2 = r55
            r15.addAction(r2)
        L_0x0aac:
            if (r16 != 0) goto L_0x0ab1
            r15.addAction(r6)
        L_0x0ab1:
            int r0 = r36.size()
            r5 = 1
            if (r0 != r5) goto L_0x0ac4
            boolean r0 = android.text.TextUtils.isEmpty(r79)
            if (r0 != 0) goto L_0x0ac4
            r10 = r79
            r15.setSubText(r10)
            goto L_0x0ac6
        L_0x0ac4:
            r10 = r79
        L_0x0ac6:
            if (r11 != 0) goto L_0x0acb
            r15.setLocalOnly(r5)
        L_0x0acb:
            if (r49 == 0) goto L_0x0ad2
            r2 = r49
            r15.setLargeIcon(r2)
        L_0x0ad2:
            r2 = 0
            boolean r0 = org.telegram.messenger.AndroidUtilities.needShowPasscode(r2)
            if (r0 != 0) goto L_0x0ba7
            boolean r0 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter
            if (r0 != 0) goto L_0x0ba7
            r2 = r59
            if (r2 == 0) goto L_0x0b6e
            int r0 = r2.size()
            r3 = 0
        L_0x0ae6:
            if (r3 >= r0) goto L_0x0b6e
            java.lang.Object r4 = r2.get(r3)
            org.telegram.tgnet.TLRPC$TL_keyboardButtonRow r4 = (org.telegram.tgnet.TLRPC$TL_keyboardButtonRow) r4
            java.util.ArrayList<org.telegram.tgnet.TLRPC$KeyboardButton> r6 = r4.buttons
            int r6 = r6.size()
            r12 = 0
        L_0x0af5:
            if (r12 >= r6) goto L_0x0b5e
            java.util.ArrayList<org.telegram.tgnet.TLRPC$KeyboardButton> r5 = r4.buttons
            java.lang.Object r5 = r5.get(r12)
            org.telegram.tgnet.TLRPC$KeyboardButton r5 = (org.telegram.tgnet.TLRPC$KeyboardButton) r5
            r32 = r0
            boolean r0 = r5 instanceof org.telegram.tgnet.TLRPC$TL_keyboardButtonCallback
            if (r0 == 0) goto L_0x0b45
            android.content.Intent r0 = new android.content.Intent
            r35 = r2
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            r38 = r4
            java.lang.Class<org.telegram.messenger.NotificationCallbackReceiver> r4 = org.telegram.messenger.NotificationCallbackReceiver.class
            r0.<init>(r2, r4)
            int r2 = r8.currentAccount
            r0.putExtra(r1, r2)
            java.lang.String r2 = "did"
            r0.putExtra(r2, r13)
            byte[] r2 = r5.data
            if (r2 == 0) goto L_0x0b25
            java.lang.String r4 = "data"
            r0.putExtra(r4, r2)
        L_0x0b25:
            java.lang.String r2 = "mid"
            r4 = r58
            r0.putExtra(r2, r4)
            java.lang.String r2 = r5.text
            android.content.Context r5 = org.telegram.messenger.ApplicationLoader.applicationContext
            r40 = r1
            int r1 = r8.lastButtonId
            r42 = r4
            int r4 = r1 + 1
            r8.lastButtonId = r4
            r4 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r0 = android.app.PendingIntent.getBroadcast(r5, r1, r0, r4)
            r5 = 0
            r15.addAction(r5, r2, r0)
            goto L_0x0b50
        L_0x0b45:
            r40 = r1
            r35 = r2
            r38 = r4
            r42 = r58
            r4 = 134217728(0x8000000, float:3.85186E-34)
            r5 = 0
        L_0x0b50:
            int r12 = r12 + 1
            r0 = r32
            r2 = r35
            r4 = r38
            r1 = r40
            r58 = r42
            r5 = 1
            goto L_0x0af5
        L_0x0b5e:
            r32 = r0
            r40 = r1
            r35 = r2
            r42 = r58
            r4 = 134217728(0x8000000, float:3.85186E-34)
            r5 = 0
            int r3 = r3 + 1
            r5 = 1
            goto L_0x0ae6
        L_0x0b6e:
            r5 = 0
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 29
            if (r0 < r1) goto L_0x0ba7
            if (r11 == 0) goto L_0x0ba7
            long r0 = (long) r11
            r2 = r30
            java.lang.Object r0 = r2.get(r0)
            androidx.core.app.Person r0 = (androidx.core.app.Person) r0
            r12 = r23
            r6 = r27
            r1 = r76
            r4 = r56
            r2 = r15
            r10 = 26
            r3 = r11
            r71 = r4
            r70 = r41
            r72 = r46
            r73 = r51
            r4 = r7
            r18 = 1
            r20 = 0
            r5 = r6
            r10 = r6
            r23 = 27
            r6 = r39
            r78 = r12
            r12 = r7
            r7 = r0
            r1.createNotificationShortcut(r2, r3, r4, r5, r6, r7)
            goto L_0x0bba
        L_0x0ba7:
            r12 = r7
            r78 = r23
            r10 = r27
            r70 = r41
            r72 = r46
            r73 = r51
            r71 = r56
            r18 = 1
            r20 = 0
            r23 = 27
        L_0x0bba:
            if (r39 != 0) goto L_0x0bde
            if (r10 == 0) goto L_0x0bde
            java.lang.String r0 = r10.phone
            if (r0 == 0) goto L_0x0bde
            int r0 = r0.length()
            if (r0 <= 0) goto L_0x0bde
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "tel:+"
            r0.append(r1)
            java.lang.String r1 = r10.phone
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r15.addPerson(r0)
        L_0x0bde:
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 26
            r2 = r33
            r5 = r70
            if (r0 < r1) goto L_0x0beb
            r8.setNotificationChannel(r2, r15, r5)
        L_0x0beb:
            org.telegram.messenger.NotificationsController$1NotificationHolder r0 = new org.telegram.messenger.NotificationsController$1NotificationHolder
            int r3 = r50.intValue()
            android.app.Notification r4 = r15.build()
            r0.<init>(r3, r4)
            r3 = r24
            r3.add(r0)
            android.util.LongSparseArray<java.lang.Integer> r0 = r8.wearNotificationsIds
            r4 = r50
            r0.put(r13, r4)
            if (r11 == 0) goto L_0x0c7f
            if (r45 == 0) goto L_0x0c7f
            java.lang.String r0 = "reply"
            r6 = r45
            r4 = r48
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
            java.lang.String r0 = "name"
            r6.put(r0, r12)     // Catch:{ JSONException -> 0x0c7f }
            r4 = r57
            r7 = r73
            r6.put(r7, r4)     // Catch:{ JSONException -> 0x0c7f }
            java.lang.String r0 = "max_date"
            r6.put(r0, r9)     // Catch:{ JSONException -> 0x0c7f }
            int r0 = java.lang.Math.abs(r11)     // Catch:{ JSONException -> 0x0c7f }
            r4 = r25
            r6.put(r4, r0)     // Catch:{ JSONException -> 0x0c7f }
            r4 = r72
            if (r4 == 0) goto L_0x0CLASSNAME
            java.lang.String r0 = "photo"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x0c7f }
            r7.<init>()     // Catch:{ JSONException -> 0x0c7f }
            int r9 = r4.dc_id     // Catch:{ JSONException -> 0x0c7f }
            r7.append(r9)     // Catch:{ JSONException -> 0x0c7f }
            r9 = r31
            r7.append(r9)     // Catch:{ JSONException -> 0x0c7f }
            long r12 = r4.volume_id     // Catch:{ JSONException -> 0x0c7f }
            r7.append(r12)     // Catch:{ JSONException -> 0x0c7f }
            r7.append(r9)     // Catch:{ JSONException -> 0x0c7f }
            long r9 = r4.secret     // Catch:{ JSONException -> 0x0c7f }
            r7.append(r9)     // Catch:{ JSONException -> 0x0c7f }
            java.lang.String r4 = r7.toString()     // Catch:{ JSONException -> 0x0c7f }
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
        L_0x0CLASSNAME:
            if (r26 == 0) goto L_0x0c5d
            java.lang.String r0 = "msgs"
            r4 = r26
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
        L_0x0c5d:
            java.lang.String r0 = "type"
            if (r11 <= 0) goto L_0x0CLASSNAME
            java.lang.String r4 = "user"
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            if (r11 >= 0) goto L_0x0CLASSNAME
            if (r37 != 0) goto L_0x0CLASSNAME
            if (r34 == 0) goto L_0x0c6e
            goto L_0x0CLASSNAME
        L_0x0c6e:
            java.lang.String r4 = "group"
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            java.lang.String r4 = "channel"
            r6.put(r0, r4)     // Catch:{ JSONException -> 0x0c7f }
        L_0x0CLASSNAME:
            r15 = r29
            r15.put(r6)     // Catch:{ JSONException -> 0x0CLASSNAME }
            goto L_0x0CLASSNAME
        L_0x0c7f:
            r15 = r29
        L_0x0CLASSNAME:
            int r6 = r19 + 1
            r1 = r78
            r9 = r2
            r14 = r3
            r4 = r5
            r7 = r15
            r11 = r22
            r13 = r28
            r10 = r36
            r2 = r71
            r3 = 26
            r5 = 1
            r12 = 0
            goto L_0x00d7
        L_0x0CLASSNAME:
            r71 = r2
            r5 = r4
            r2 = r9
            r28 = r13
            r3 = r14
            r4 = r15
            r20 = 0
            r15 = r7
            if (r5 == 0) goto L_0x0cc6
            boolean r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r0 == 0) goto L_0x0cbe
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "show summary with id "
            r0.append(r1)
            int r1 = r8.notificationId
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            org.telegram.messenger.FileLog.d(r0)
        L_0x0cbe:
            androidx.core.app.NotificationManagerCompat r0 = notificationManager
            int r1 = r8.notificationId
            r0.notify(r1, r2)
            goto L_0x0cd5
        L_0x0cc6:
            java.util.HashSet<java.lang.Long> r0 = r8.openedInBubbleDialogs
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0cd5
            androidx.core.app.NotificationManagerCompat r0 = notificationManager
            int r1 = r8.notificationId
            r0.cancel(r1)
        L_0x0cd5:
            int r0 = r3.size()
            r1 = 0
        L_0x0cda:
            if (r1 >= r0) goto L_0x0ce8
            java.lang.Object r2 = r3.get(r1)
            org.telegram.messenger.NotificationsController$1NotificationHolder r2 = (org.telegram.messenger.NotificationsController.AnonymousClass1NotificationHolder) r2
            r2.call()
            int r1 = r1 + 1
            goto L_0x0cda
        L_0x0ce8:
            r12 = 0
        L_0x0ce9:
            int r0 = r28.size()
            if (r12 >= r0) goto L_0x0d2e
            r1 = r28
            long r2 = r1.keyAt(r12)
            java.util.HashSet<java.lang.Long> r0 = r8.openedInBubbleDialogs
            java.lang.Long r2 = java.lang.Long.valueOf(r2)
            boolean r0 = r0.contains(r2)
            if (r0 == 0) goto L_0x0d02
            goto L_0x0d29
        L_0x0d02:
            java.lang.Object r0 = r1.valueAt(r12)
            java.lang.Integer r0 = (java.lang.Integer) r0
            boolean r2 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r2 == 0) goto L_0x0d20
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "cancel notification id "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            org.telegram.messenger.FileLog.d(r2)
        L_0x0d20:
            androidx.core.app.NotificationManagerCompat r2 = notificationManager
            int r0 = r0.intValue()
            r2.cancel(r0)
        L_0x0d29:
            int r12 = r12 + 1
            r28 = r1
            goto L_0x0ce9
        L_0x0d2e:
            if (r15 == 0) goto L_0x0d4e
            org.json.JSONObject r0 = new org.json.JSONObject     // Catch:{ Exception -> 0x0d4e }
            r0.<init>()     // Catch:{ Exception -> 0x0d4e }
            r1 = r71
            r0.put(r4, r1)     // Catch:{ Exception -> 0x0d4e }
            java.lang.String r1 = "n"
            r0.put(r1, r15)     // Catch:{ Exception -> 0x0d4e }
            java.lang.String r1 = "/notify"
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x0d4e }
            byte[] r0 = r0.getBytes()     // Catch:{ Exception -> 0x0d4e }
            java.lang.String r2 = "remote_notifications"
            org.telegram.messenger.WearDataLayerListenerService.sendMessageToWatch(r1, r0, r2)     // Catch:{ Exception -> 0x0d4e }
        L_0x0d4e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.showExtraNotifications(androidx.core.app.NotificationCompat$Builder, boolean, java.lang.String):void");
    }

    @TargetApi(28)
    private void loadRoundAvatar(File file, Person.Builder builder) {
        if (file != null) {
            try {
                builder.setIcon(IconCompat.createWithBitmap(ImageDecoder.decodeBitmap(ImageDecoder.createSource(file), $$Lambda$NotificationsController$TyIZKafFEr5zlu0ZpVMXbOeu_I.INSTANCE)));
            } catch (Throwable unused) {
            }
        }
    }

    static /* synthetic */ int lambda$null$33(Canvas canvas) {
        Path path = new Path();
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        int width = canvas.getWidth();
        float f = (float) (width / 2);
        path.addRoundRect(0.0f, 0.0f, (float) width, (float) canvas.getHeight(), f, f, Path.Direction.CW);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawPath(path, paint);
        return -3;
    }

    public void playOutChatSound() {
        if (this.inChatSoundEnabled && !MediaController.getInstance().isRecordingAudio()) {
            try {
                if (audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            notificationsQueue.postRunnable(new Runnable() {
                public final void run() {
                    NotificationsController.this.lambda$playOutChatSound$36$NotificationsController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$playOutChatSound$36$NotificationsController() {
        try {
            if (Math.abs(System.currentTimeMillis() - this.lastSoundOutPlay) > 100) {
                this.lastSoundOutPlay = System.currentTimeMillis();
                if (this.soundPool == null) {
                    SoundPool soundPool2 = new SoundPool(3, 1, 0);
                    this.soundPool = soundPool2;
                    soundPool2.setOnLoadCompleteListener($$Lambda$NotificationsController$OUNJlLfPbdz6QJs8uZCY6NbjGto.INSTANCE);
                }
                if (this.soundOut == 0 && !this.soundOutLoaded) {
                    this.soundOutLoaded = true;
                    this.soundOut = this.soundPool.load(ApplicationLoader.applicationContext, NUM, 1);
                }
                if (this.soundOut != 0) {
                    try {
                        this.soundPool.play(this.soundOut, 1.0f, 1.0f, 1, 0, 1.0f);
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
            }
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
    }

    static /* synthetic */ void lambda$null$35(SoundPool soundPool2, int i, int i2) {
        if (i2 == 0) {
            try {
                soundPool2.play(i, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    public void clearDialogNotificationsSettings(long j) {
        SharedPreferences.Editor edit = getAccountInstance().getNotificationsSettings().edit();
        SharedPreferences.Editor remove = edit.remove("notify2_" + j);
        remove.remove("custom_" + j);
        getMessagesStorage().setDialogFlags(j, 0);
        TLRPC$Dialog tLRPC$Dialog = getMessagesController().dialogs_dict.get(j);
        if (tLRPC$Dialog != null) {
            tLRPC$Dialog.notify_settings = new TLRPC$TL_peerNotifySettings();
        }
        edit.commit();
        getNotificationsController().updateServerNotificationsSettings(j, true);
    }

    public void setDialogNotificationsSettings(long j, int i) {
        SharedPreferences.Editor edit = getAccountInstance().getNotificationsSettings().edit();
        TLRPC$Dialog tLRPC$Dialog = MessagesController.getInstance(UserConfig.selectedAccount).dialogs_dict.get(j);
        if (i == 4) {
            if (isGlobalNotificationsEnabled(j)) {
                edit.remove("notify2_" + j);
            } else {
                edit.putInt("notify2_" + j, 0);
            }
            getMessagesStorage().setDialogFlags(j, 0);
            if (tLRPC$Dialog != null) {
                tLRPC$Dialog.notify_settings = new TLRPC$TL_peerNotifySettings();
            }
        } else {
            int currentTime = ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime();
            if (i == 0) {
                currentTime += 3600;
            } else if (i == 1) {
                currentTime += 28800;
            } else if (i == 2) {
                currentTime += 172800;
            } else if (i == 3) {
                currentTime = Integer.MAX_VALUE;
            }
            long j2 = 1;
            if (i == 3) {
                edit.putInt("notify2_" + j, 2);
            } else {
                edit.putInt("notify2_" + j, 3);
                edit.putInt("notifyuntil_" + j, currentTime);
                j2 = 1 | (((long) currentTime) << 32);
            }
            getInstance(UserConfig.selectedAccount).removeNotificationsForDialog(j);
            MessagesStorage.getInstance(UserConfig.selectedAccount).setDialogFlags(j, j2);
            if (tLRPC$Dialog != null) {
                TLRPC$TL_peerNotifySettings tLRPC$TL_peerNotifySettings = new TLRPC$TL_peerNotifySettings();
                tLRPC$Dialog.notify_settings = tLRPC$TL_peerNotifySettings;
                tLRPC$TL_peerNotifySettings.mute_until = currentTime;
            }
        }
        edit.commit();
        updateServerNotificationsSettings(j);
    }

    public void updateServerNotificationsSettings(long j) {
        updateServerNotificationsSettings(j, true);
    }

    public void updateServerNotificationsSettings(long j, boolean z) {
        int i = 0;
        if (z) {
            getNotificationCenter().postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
        }
        int i2 = (int) j;
        if (i2 != 0) {
            SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
            TLRPC$TL_account_updateNotifySettings tLRPC$TL_account_updateNotifySettings = new TLRPC$TL_account_updateNotifySettings();
            TLRPC$TL_inputPeerNotifySettings tLRPC$TL_inputPeerNotifySettings = new TLRPC$TL_inputPeerNotifySettings();
            tLRPC$TL_account_updateNotifySettings.settings = tLRPC$TL_inputPeerNotifySettings;
            tLRPC$TL_inputPeerNotifySettings.flags |= 1;
            tLRPC$TL_inputPeerNotifySettings.show_previews = notificationsSettings.getBoolean("content_preview_" + j, true);
            TLRPC$TL_inputPeerNotifySettings tLRPC$TL_inputPeerNotifySettings2 = tLRPC$TL_account_updateNotifySettings.settings;
            tLRPC$TL_inputPeerNotifySettings2.flags = tLRPC$TL_inputPeerNotifySettings2.flags | 2;
            tLRPC$TL_inputPeerNotifySettings2.silent = notificationsSettings.getBoolean("silent_" + j, false);
            int i3 = notificationsSettings.getInt("notify2_" + j, -1);
            if (i3 != -1) {
                TLRPC$TL_inputPeerNotifySettings tLRPC$TL_inputPeerNotifySettings3 = tLRPC$TL_account_updateNotifySettings.settings;
                tLRPC$TL_inputPeerNotifySettings3.flags |= 4;
                if (i3 == 3) {
                    tLRPC$TL_inputPeerNotifySettings3.mute_until = notificationsSettings.getInt("notifyuntil_" + j, 0);
                } else {
                    if (i3 == 2) {
                        i = Integer.MAX_VALUE;
                    }
                    tLRPC$TL_inputPeerNotifySettings3.mute_until = i;
                }
            }
            TLRPC$TL_inputNotifyPeer tLRPC$TL_inputNotifyPeer = new TLRPC$TL_inputNotifyPeer();
            tLRPC$TL_account_updateNotifySettings.peer = tLRPC$TL_inputNotifyPeer;
            tLRPC$TL_inputNotifyPeer.peer = getMessagesController().getInputPeer(i2);
            getConnectionsManager().sendRequest(tLRPC$TL_account_updateNotifySettings, $$Lambda$NotificationsController$WV8JpQrNXdfWVJfPV9wKTUTuLBk.INSTANCE);
        }
    }

    public void updateServerNotificationsSettings(int i) {
        SharedPreferences notificationsSettings = getAccountInstance().getNotificationsSettings();
        TLRPC$TL_account_updateNotifySettings tLRPC$TL_account_updateNotifySettings = new TLRPC$TL_account_updateNotifySettings();
        TLRPC$TL_inputPeerNotifySettings tLRPC$TL_inputPeerNotifySettings = new TLRPC$TL_inputPeerNotifySettings();
        tLRPC$TL_account_updateNotifySettings.settings = tLRPC$TL_inputPeerNotifySettings;
        tLRPC$TL_inputPeerNotifySettings.flags = 5;
        if (i == 0) {
            tLRPC$TL_account_updateNotifySettings.peer = new TLRPC$TL_inputNotifyChats();
            tLRPC$TL_account_updateNotifySettings.settings.mute_until = notificationsSettings.getInt("EnableGroup2", 0);
            tLRPC$TL_account_updateNotifySettings.settings.show_previews = notificationsSettings.getBoolean("EnablePreviewGroup", true);
        } else if (i == 1) {
            tLRPC$TL_account_updateNotifySettings.peer = new TLRPC$TL_inputNotifyUsers();
            tLRPC$TL_account_updateNotifySettings.settings.mute_until = notificationsSettings.getInt("EnableAll2", 0);
            tLRPC$TL_account_updateNotifySettings.settings.show_previews = notificationsSettings.getBoolean("EnablePreviewAll", true);
        } else {
            tLRPC$TL_account_updateNotifySettings.peer = new TLRPC$TL_inputNotifyBroadcasts();
            tLRPC$TL_account_updateNotifySettings.settings.mute_until = notificationsSettings.getInt("EnableChannel2", 0);
            tLRPC$TL_account_updateNotifySettings.settings.show_previews = notificationsSettings.getBoolean("EnablePreviewChannel", true);
        }
        getConnectionsManager().sendRequest(tLRPC$TL_account_updateNotifySettings, $$Lambda$NotificationsController$w9HtqTbEDgkwB57xEiog8KyWkW8.INSTANCE);
    }

    public boolean isGlobalNotificationsEnabled(long j) {
        return isGlobalNotificationsEnabled(j, (Boolean) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000b, code lost:
        if (r4.booleanValue() != false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0025, code lost:
        if (r3.megagroup == false) goto L_0x0029;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isGlobalNotificationsEnabled(long r2, java.lang.Boolean r4) {
        /*
            r1 = this;
            int r3 = (int) r2
            r2 = 2
            r0 = 0
            if (r3 >= 0) goto L_0x0028
            if (r4 == 0) goto L_0x0010
            boolean r3 = r4.booleanValue()
            if (r3 == 0) goto L_0x000e
            goto L_0x0029
        L_0x000e:
            r2 = 0
            goto L_0x0029
        L_0x0010:
            org.telegram.messenger.MessagesController r4 = r1.getMessagesController()
            int r3 = -r3
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r3 = r4.getChat(r3)
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r3)
            if (r4 == 0) goto L_0x000e
            boolean r3 = r3.megagroup
            if (r3 != 0) goto L_0x000e
            goto L_0x0029
        L_0x0028:
            r2 = 1
        L_0x0029:
            boolean r2 = r1.isGlobalNotificationsEnabled((int) r2)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.NotificationsController.isGlobalNotificationsEnabled(long, java.lang.Boolean):boolean");
    }

    public boolean isGlobalNotificationsEnabled(int i) {
        return getAccountInstance().getNotificationsSettings().getInt(getGlobalNotificationsKey(i), 0) < getConnectionsManager().getCurrentTime();
    }

    public void setGlobalNotificationsEnabled(int i, int i2) {
        getAccountInstance().getNotificationsSettings().edit().putInt(getGlobalNotificationsKey(i), i2).commit();
        updateServerNotificationsSettings(i);
        getMessagesStorage().updateMutedDialogsFiltersCounters();
    }
}
