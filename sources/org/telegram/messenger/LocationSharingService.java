package org.telegram.messenger;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import java.util.ArrayList;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkedTrackBlacklistUtil;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.ui.LaunchActivity;

public class LocationSharingService extends Service implements NotificationCenterDelegate {
    private Builder builder;
    private Handler handler;
    private Runnable runnable;

    /* renamed from: org.telegram.messenger.LocationSharingService$1 */
    class C02431 implements Runnable {

        /* renamed from: org.telegram.messenger.LocationSharingService$1$1 */
        class C02421 implements Runnable {
            C02421() {
            }

            public void run() {
                for (int a = 0; a < 3; a++) {
                    LocationController.getInstance(a).update();
                }
            }
        }

        C02431() {
        }

        public void run() {
            LocationSharingService.this.handler.postDelayed(LocationSharingService.this.runnable, ChunkedTrackBlacklistUtil.DEFAULT_TRACK_BLACKLIST_MS);
            Utilities.stageQueue.postRunnable(new C02421());
        }
    }

    /* renamed from: org.telegram.messenger.LocationSharingService$2 */
    class C02442 implements Runnable {
        C02442() {
        }

        public void run() {
            if (LocationSharingService.this.getInfos().isEmpty()) {
                LocationSharingService.this.stopSelf();
            } else {
                LocationSharingService.this.updateNotification(true);
            }
        }
    }

    public LocationSharingService() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
    }

    public void onCreate() {
        super.onCreate();
        this.handler = new Handler();
        this.runnable = new C02431();
        this.handler.postDelayed(this.runnable, ChunkedTrackBlacklistUtil.DEFAULT_TRACK_BLACKLIST_MS);
    }

    public IBinder onBind(Intent arg2) {
        return null;
    }

    public void onDestroy() {
        if (this.handler != null) {
            this.handler.removeCallbacks(this.runnable);
        }
        stopForeground(true);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.liveLocationsChanged && this.handler != null) {
            this.handler.post(new C02442());
        }
    }

    private ArrayList<SharingLocationInfo> getInfos() {
        ArrayList<SharingLocationInfo> infos = new ArrayList();
        for (int a = 0; a < 3; a++) {
            ArrayList<SharingLocationInfo> arrayList = LocationController.getInstance(a).sharingLocationsUI;
            if (!arrayList.isEmpty()) {
                infos.addAll(arrayList);
            }
        }
        return infos;
    }

    private void updateNotification(boolean post) {
        if (this.builder != null) {
            String param;
            ArrayList<SharingLocationInfo> infos = getInfos();
            if (infos.size() == 1) {
                String param2;
                SharingLocationInfo info = (SharingLocationInfo) infos.get(0);
                int lower_id = (int) info.messageObject.getDialogId();
                int currentAccount = info.messageObject.currentAccount;
                if (lower_id > 0) {
                    param2 = UserObject.getFirstName(MessagesController.getInstance(currentAccount).getUser(Integer.valueOf(lower_id)));
                } else {
                    Chat chat = MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(-lower_id));
                    if (chat != null) {
                        param2 = chat.title;
                    } else {
                        param2 = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                }
                param = param2;
            } else {
                param = LocaleController.formatPluralString("Chats", infos.size());
            }
            String str = String.format(LocaleController.getString("AttachLiveLocationIsSharing", R.string.AttachLiveLocationIsSharing), new Object[]{LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation), param});
            this.builder.setTicker(str);
            this.builder.setContentText(str);
            if (post) {
                NotificationManagerCompat.from(ApplicationLoader.applicationContext).notify(6, this.builder.build());
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getInfos().isEmpty()) {
            stopSelf();
        }
        if (this.builder == null) {
            Intent intent2 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
            intent2.setAction("org.tmessages.openlocations");
            intent2.setFlags(32768);
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent2, 0);
            this.builder = new Builder(ApplicationLoader.applicationContext);
            this.builder.setWhen(System.currentTimeMillis());
            this.builder.setSmallIcon(R.drawable.live_loc);
            this.builder.setContentIntent(contentIntent);
            this.builder.setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL);
            this.builder.setContentTitle(LocaleController.getString("AppName", R.string.AppName));
            this.builder.addAction(0, LocaleController.getString("StopLiveLocation", R.string.StopLiveLocation), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, new Intent(ApplicationLoader.applicationContext, StopLiveLocationReceiver.class), 134217728));
        }
        updateNotification(false);
        startForeground(6, this.builder.build());
        return 2;
    }
}
