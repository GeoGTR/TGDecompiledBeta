package org.telegram.messenger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;

public class SharedConfig {
    public static boolean allowBigEmoji = false;
    public static boolean allowScreenCapture = false;
    public static boolean appLocked = false;
    public static boolean archiveHidden = false;
    public static int autoLockIn = 3600;
    public static boolean autoplayGifs = true;
    public static boolean autoplayVideo = true;
    public static int badPasscodeTries = 0;
    private static boolean configLoaded = false;
    public static ProxyInfo currentProxy = null;
    public static boolean customTabs = true;
    public static boolean directShare = true;
    public static long directShareHash = 0;
    public static boolean drawDialogIcons = false;
    public static int fontSize = AndroidUtilities.dp(16.0f);
    public static boolean groupPhotosEnabled = true;
    public static boolean hasCameraCache = false;
    public static boolean inappCamera = true;
    public static boolean isWaitingForPasscodeEnter = false;
    public static long lastAppPauseTime = 0;
    private static int lastLocalId = -210000;
    public static int lastPauseTime = 0;
    public static String lastUpdateVersion = null;
    public static long lastUptimeMillis = 0;
    private static final Object localIdSync = new Object();
    public static int mapPreviewType = 2;
    public static boolean noSoundHintShowed = false;
    public static String passcodeHash = "";
    public static long passcodeRetryInMs = 0;
    public static byte[] passcodeSalt = new byte[0];
    public static int passcodeType = 0;
    public static int passportConfigHash = 0;
    private static String passportConfigJson = "";
    private static HashMap<String, String> passportConfigMap = null;
    public static boolean playOrderReversed = false;
    public static ArrayList<ProxyInfo> proxyList = new ArrayList();
    private static boolean proxyListLoaded = false;
    public static byte[] pushAuthKey = null;
    public static byte[] pushAuthKeyId = null;
    public static String pushString = "";
    public static boolean raiseToSpeak = true;
    public static int repeatMode;
    public static boolean roundCamera16to9 = true;
    public static boolean saveIncomingPhotos;
    public static boolean saveStreamMedia = true;
    public static boolean saveToGallery;
    public static boolean showAnimatedStickers = true;
    public static boolean showNotificationsForAllAccounts = true;
    public static boolean shuffleMusic;
    public static boolean sortContactsByName;
    public static boolean streamAllVideo = false;
    public static boolean streamMedia = true;
    public static boolean streamMkv = false;
    public static int suggestStickers;
    private static final Object sync = new Object();
    public static boolean useFingerprint = true;
    public static boolean useSystemEmoji;
    public static boolean useThreeLinesLayout;

    public static class ProxyInfo {
        public String address;
        public boolean available;
        public long availableCheckTime;
        public boolean checking;
        public String password;
        public long ping;
        public int port;
        public long proxyCheckPingId;
        public String secret;
        public String username;

        public ProxyInfo(String str, int i, String str2, String str3, String str4) {
            this.address = str;
            this.port = i;
            this.username = str2;
            this.password = str3;
            this.secret = str4;
            String str5 = "";
            if (this.address == null) {
                this.address = str5;
            }
            if (this.password == null) {
                this.password = str5;
            }
            if (this.username == null) {
                this.username = str5;
            }
            if (this.secret == null) {
                this.secret = str5;
            }
        }
    }

    static {
        loadConfig();
    }

    public static void saveConfig() {
        synchronized (sync) {
            try {
                Editor edit = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0).edit();
                edit.putBoolean("saveIncomingPhotos", saveIncomingPhotos);
                edit.putString("passcodeHash1", passcodeHash);
                edit.putString("passcodeSalt", passcodeSalt.length > 0 ? Base64.encodeToString(passcodeSalt, 0) : "");
                edit.putBoolean("appLocked", appLocked);
                edit.putInt("passcodeType", passcodeType);
                edit.putLong("passcodeRetryInMs", passcodeRetryInMs);
                edit.putLong("lastUptimeMillis", lastUptimeMillis);
                edit.putInt("badPasscodeTries", badPasscodeTries);
                edit.putInt("autoLockIn", autoLockIn);
                edit.putInt("lastPauseTime", lastPauseTime);
                edit.putLong("lastAppPauseTime", lastAppPauseTime);
                edit.putString("lastUpdateVersion2", lastUpdateVersion);
                edit.putBoolean("useFingerprint", useFingerprint);
                edit.putBoolean("allowScreenCapture", allowScreenCapture);
                edit.putString("pushString2", pushString);
                edit.putString("pushAuthKey", pushAuthKey != null ? Base64.encodeToString(pushAuthKey, 0) : "");
                edit.putInt("lastLocalId", lastLocalId);
                edit.putString("passportConfigJson", passportConfigJson);
                edit.putInt("passportConfigHash", passportConfigHash);
                edit.putBoolean("sortContactsByName", sortContactsByName);
                edit.commit();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static int getLastLocalId() {
        int i;
        synchronized (localIdSync) {
            i = lastLocalId;
            lastLocalId = i - 1;
        }
        return i;
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
            saveIncomingPhotos = sharedPreferences.getBoolean("saveIncomingPhotos", false);
            passcodeHash = sharedPreferences.getString("passcodeHash1", "");
            appLocked = sharedPreferences.getBoolean("appLocked", false);
            passcodeType = sharedPreferences.getInt("passcodeType", 0);
            passcodeRetryInMs = sharedPreferences.getLong("passcodeRetryInMs", 0);
            lastUptimeMillis = sharedPreferences.getLong("lastUptimeMillis", 0);
            badPasscodeTries = sharedPreferences.getInt("badPasscodeTries", 0);
            autoLockIn = sharedPreferences.getInt("autoLockIn", 3600);
            lastPauseTime = sharedPreferences.getInt("lastPauseTime", 0);
            lastAppPauseTime = sharedPreferences.getLong("lastAppPauseTime", 0);
            useFingerprint = sharedPreferences.getBoolean("useFingerprint", true);
            lastUpdateVersion = sharedPreferences.getString("lastUpdateVersion2", "3.5");
            allowScreenCapture = sharedPreferences.getBoolean("allowScreenCapture", false);
            lastLocalId = sharedPreferences.getInt("lastLocalId", -210000);
            pushString = sharedPreferences.getString("pushString2", "");
            passportConfigJson = sharedPreferences.getString("passportConfigJson", "");
            passportConfigHash = sharedPreferences.getInt("passportConfigHash", 0);
            String string = sharedPreferences.getString("pushAuthKey", null);
            if (!TextUtils.isEmpty(string)) {
                pushAuthKey = Base64.decode(string, 0);
            }
            if (passcodeHash.length() > 0 && lastPauseTime == 0) {
                lastPauseTime = (int) ((System.currentTimeMillis() / 1000) - 600);
            }
            String string2 = sharedPreferences.getString("passcodeSalt", "");
            if (string2.length() > 0) {
                passcodeSalt = Base64.decode(string2, 0);
            } else {
                passcodeSalt = new byte[0];
            }
            sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
            saveToGallery = sharedPreferences.getBoolean("save_gallery", false);
            autoplayGifs = sharedPreferences.getBoolean("autoplay_gif", true);
            autoplayVideo = sharedPreferences.getBoolean("autoplay_video", true);
            mapPreviewType = sharedPreferences.getInt("mapPreviewType", 2);
            raiseToSpeak = sharedPreferences.getBoolean("raise_to_speak", true);
            customTabs = sharedPreferences.getBoolean("custom_tabs", true);
            directShare = sharedPreferences.getBoolean("direct_share", true);
            shuffleMusic = sharedPreferences.getBoolean("shuffleMusic", false);
            playOrderReversed = sharedPreferences.getBoolean("playOrderReversed", false);
            inappCamera = sharedPreferences.getBoolean("inappCamera", true);
            hasCameraCache = sharedPreferences.contains("cameraCache");
            roundCamera16to9 = true;
            groupPhotosEnabled = sharedPreferences.getBoolean("groupPhotosEnabled", true);
            repeatMode = sharedPreferences.getInt("repeatMode", 0);
            fontSize = sharedPreferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
            allowBigEmoji = sharedPreferences.getBoolean("allowBigEmoji", true);
            useSystemEmoji = sharedPreferences.getBoolean("useSystemEmoji", false);
            streamMedia = sharedPreferences.getBoolean("streamMedia", true);
            saveStreamMedia = sharedPreferences.getBoolean("saveStreamMedia", true);
            streamAllVideo = sharedPreferences.getBoolean("streamAllVideo", BuildVars.DEBUG_VERSION);
            streamMkv = sharedPreferences.getBoolean("streamMkv", false);
            suggestStickers = sharedPreferences.getInt("suggestStickers", 0);
            sortContactsByName = sharedPreferences.getBoolean("sortContactsByName", false);
            noSoundHintShowed = sharedPreferences.getBoolean("noSoundHintShowed", false);
            directShareHash = sharedPreferences.getLong("directShareHash", 0);
            useThreeLinesLayout = sharedPreferences.getBoolean("useThreeLinesLayout", false);
            archiveHidden = sharedPreferences.getBoolean("archiveHidden", false);
            showNotificationsForAllAccounts = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("AllAccounts", true);
            configLoaded = true;
        }
    }

    public static void increaseBadPasscodeTries() {
        badPasscodeTries++;
        int i = badPasscodeTries;
        if (i >= 3) {
            if (i == 3) {
                passcodeRetryInMs = 5000;
            } else if (i == 4) {
                passcodeRetryInMs = 10000;
            } else if (i == 5) {
                passcodeRetryInMs = 15000;
            } else if (i == 6) {
                passcodeRetryInMs = 20000;
            } else if (i != 7) {
                passcodeRetryInMs = 30000;
            } else {
                passcodeRetryInMs = 25000;
            }
            lastUptimeMillis = SystemClock.elapsedRealtime();
        }
        saveConfig();
    }

    public static boolean isPassportConfigLoaded() {
        return passportConfigMap != null;
    }

    public static void setPassportConfig(String str, int i) {
        passportConfigMap = null;
        passportConfigJson = str;
        passportConfigHash = i;
        saveConfig();
        getCountryLangs();
    }

    public static HashMap<String, String> getCountryLangs() {
        if (passportConfigMap == null) {
            passportConfigMap = new HashMap();
            try {
                JSONObject jSONObject = new JSONObject(passportConfigJson);
                Iterator keys = jSONObject.keys();
                while (keys.hasNext()) {
                    String str = (String) keys.next();
                    passportConfigMap.put(str.toUpperCase(), jSONObject.getString(str).toUpperCase());
                }
            } catch (Throwable th) {
                FileLog.e(th);
            }
        }
        return passportConfigMap;
    }

    public static boolean checkPasscode(String str) {
        String str2 = "UTF-8";
        byte[] bytes;
        if (passcodeSalt.length == 0) {
            boolean equals = Utilities.MD5(str).equals(passcodeHash);
            if (equals) {
                try {
                    passcodeSalt = new byte[16];
                    Utilities.random.nextBytes(passcodeSalt);
                    bytes = str.getBytes(str2);
                    byte[] bArr = new byte[(bytes.length + 32)];
                    System.arraycopy(passcodeSalt, 0, bArr, 0, 16);
                    System.arraycopy(bytes, 0, bArr, 16, bytes.length);
                    System.arraycopy(passcodeSalt, 0, bArr, bytes.length + 16, 16);
                    passcodeHash = Utilities.bytesToHex(Utilities.computeSHA256(bArr, 0, bArr.length));
                    saveConfig();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return equals;
        }
        try {
            bytes = str.getBytes(str2);
            byte[] bArr2 = new byte[(bytes.length + 32)];
            System.arraycopy(passcodeSalt, 0, bArr2, 0, 16);
            System.arraycopy(bytes, 0, bArr2, 16, bytes.length);
            System.arraycopy(passcodeSalt, 0, bArr2, bytes.length + 16, 16);
            return passcodeHash.equals(Utilities.bytesToHex(Utilities.computeSHA256(bArr2, 0, bArr2.length)));
        } catch (Exception e2) {
            FileLog.e(e2);
            return false;
        }
    }

    public static void clearConfig() {
        saveIncomingPhotos = false;
        appLocked = false;
        passcodeType = 0;
        passcodeRetryInMs = 0;
        lastUptimeMillis = 0;
        badPasscodeTries = 0;
        passcodeHash = "";
        passcodeSalt = new byte[0];
        autoLockIn = 3600;
        lastPauseTime = 0;
        useFingerprint = true;
        isWaitingForPasscodeEnter = false;
        allowScreenCapture = false;
        lastUpdateVersion = BuildVars.BUILD_VERSION_STRING;
        saveConfig();
    }

    public static void setSuggestStickers(int i) {
        suggestStickers = i;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putInt("suggestStickers", suggestStickers);
        edit.commit();
    }

    public static void toggleShuffleMusic(int i) {
        if (i == 2) {
            shuffleMusic ^= 1;
        } else {
            playOrderReversed ^= 1;
        }
        MediaController.getInstance().checkIsNextMediaFileDownloaded();
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("shuffleMusic", shuffleMusic);
        edit.putBoolean("playOrderReversed", playOrderReversed);
        edit.commit();
    }

    public static void toggleRepeatMode() {
        repeatMode++;
        if (repeatMode > 2) {
            repeatMode = 0;
        }
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putInt("repeatMode", repeatMode);
        edit.commit();
    }

    public static void toggleSaveToGallery() {
        saveToGallery ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("save_gallery", saveToGallery);
        edit.commit();
        checkSaveToGalleryFiles();
    }

    public static void toggleAutoplayGifs() {
        autoplayGifs ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("autoplay_gif", autoplayGifs);
        edit.commit();
    }

    public static void setUseThreeLinesLayout(boolean z) {
        useThreeLinesLayout = z;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("useThreeLinesLayout", useThreeLinesLayout);
        edit.commit();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload, Boolean.valueOf(true));
    }

    public static void toggleArchiveHidden() {
        archiveHidden ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("archiveHidden", archiveHidden);
        edit.commit();
    }

    public static void toggleAutoplayVideo() {
        autoplayVideo ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("autoplay_video", autoplayVideo);
        edit.commit();
    }

    public static boolean isSecretMapPreviewSet() {
        return MessagesController.getGlobalMainSettings().contains("mapPreviewType");
    }

    public static void setSecretMapPreviewType(int i) {
        mapPreviewType = i;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putInt("mapPreviewType", mapPreviewType);
        edit.commit();
    }

    public static void setNoSoundHintShowed(boolean z) {
        if (noSoundHintShowed != z) {
            noSoundHintShowed = z;
            Editor edit = MessagesController.getGlobalMainSettings().edit();
            edit.putBoolean("noSoundHintShowed", noSoundHintShowed);
            edit.commit();
        }
    }

    public static void toogleRaiseToSpeak() {
        raiseToSpeak ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("raise_to_speak", raiseToSpeak);
        edit.commit();
    }

    public static void toggleCustomTabs() {
        customTabs ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("custom_tabs", customTabs);
        edit.commit();
    }

    public static void toggleDirectShare() {
        directShare ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("direct_share", directShare);
        edit.commit();
    }

    public static void toggleStreamMedia() {
        streamMedia ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("streamMedia", streamMedia);
        edit.commit();
    }

    public static void toggleSortContactsByName() {
        sortContactsByName ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("sortContactsByName", sortContactsByName);
        edit.commit();
    }

    public static void toggleStreamAllVideo() {
        streamAllVideo ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("streamAllVideo", streamAllVideo);
        edit.commit();
    }

    public static void toggleStreamMkv() {
        streamMkv ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("streamMkv", streamMkv);
        edit.commit();
    }

    public static void toggleSaveStreamMedia() {
        saveStreamMedia ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("saveStreamMedia", saveStreamMedia);
        edit.commit();
    }

    public static void toggleInappCamera() {
        inappCamera ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("inappCamera", inappCamera);
        edit.commit();
    }

    public static void toggleRoundCamera16to9() {
        roundCamera16to9 ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("roundCamera16to9", roundCamera16to9);
        edit.commit();
    }

    public static void toggleGroupPhotosEnabled() {
        groupPhotosEnabled ^= 1;
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("groupPhotosEnabled", groupPhotosEnabled);
        edit.commit();
    }

    public static void loadProxyList() {
        if (!proxyListLoaded) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
            String str = "";
            String string = sharedPreferences.getString("proxy_ip", str);
            String string2 = sharedPreferences.getString("proxy_user", str);
            String string3 = sharedPreferences.getString("proxy_pass", str);
            String string4 = sharedPreferences.getString("proxy_secret", str);
            int i = sharedPreferences.getInt("proxy_port", 1080);
            proxyListLoaded = true;
            proxyList.clear();
            currentProxy = null;
            String string5 = sharedPreferences.getString("proxy_list", null);
            if (!TextUtils.isEmpty(string5)) {
                SerializedData serializedData = new SerializedData(Base64.decode(string5, 0));
                int readInt32 = serializedData.readInt32(false);
                for (int i2 = 0; i2 < readInt32; i2++) {
                    ProxyInfo proxyInfo = new ProxyInfo(serializedData.readString(false), serializedData.readInt32(false), serializedData.readString(false), serializedData.readString(false), serializedData.readString(false));
                    proxyList.add(proxyInfo);
                    if (currentProxy == null && !TextUtils.isEmpty(string) && string.equals(proxyInfo.address) && i == proxyInfo.port && string2.equals(proxyInfo.username) && string3.equals(proxyInfo.password)) {
                        currentProxy = proxyInfo;
                    }
                }
                serializedData.cleanup();
            }
            if (currentProxy == null && !TextUtils.isEmpty(string)) {
                ProxyInfo proxyInfo2 = new ProxyInfo(string, i, string2, string3, string4);
                currentProxy = proxyInfo2;
                proxyList.add(0, proxyInfo2);
            }
        }
    }

    public static void saveProxyList() {
        SerializedData serializedData = new SerializedData();
        int size = proxyList.size();
        serializedData.writeInt32(size);
        for (int i = 0; i < size; i++) {
            ProxyInfo proxyInfo = (ProxyInfo) proxyList.get(i);
            String str = proxyInfo.address;
            String str2 = "";
            if (str == null) {
                str = str2;
            }
            serializedData.writeString(str);
            serializedData.writeInt32(proxyInfo.port);
            str = proxyInfo.username;
            if (str == null) {
                str = str2;
            }
            serializedData.writeString(str);
            str = proxyInfo.password;
            if (str == null) {
                str = str2;
            }
            serializedData.writeString(str);
            String str3 = proxyInfo.secret;
            if (str3 == null) {
                str3 = str2;
            }
            serializedData.writeString(str3);
        }
        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putString("proxy_list", Base64.encodeToString(serializedData.toByteArray(), 2)).commit();
        serializedData.cleanup();
    }

    public static ProxyInfo addProxy(ProxyInfo proxyInfo) {
        loadProxyList();
        int size = proxyList.size();
        for (int i = 0; i < size; i++) {
            ProxyInfo proxyInfo2 = (ProxyInfo) proxyList.get(i);
            if (proxyInfo.address.equals(proxyInfo2.address) && proxyInfo.port == proxyInfo2.port && proxyInfo.username.equals(proxyInfo2.username) && proxyInfo.password.equals(proxyInfo2.password) && proxyInfo.secret.equals(proxyInfo2.secret)) {
                return proxyInfo2;
            }
        }
        proxyList.add(proxyInfo);
        saveProxyList();
        return proxyInfo;
    }

    public static void deleteProxy(ProxyInfo proxyInfo) {
        if (currentProxy == proxyInfo) {
            currentProxy = null;
            SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
            String str = "proxy_enabled";
            boolean z = globalMainSettings.getBoolean(str, false);
            Editor edit = globalMainSettings.edit();
            String str2 = "";
            edit.putString("proxy_ip", str2);
            edit.putString("proxy_pass", str2);
            edit.putString("proxy_user", str2);
            edit.putString("proxy_secret", str2);
            edit.putInt("proxy_port", 1080);
            edit.putBoolean(str, false);
            edit.putBoolean("proxy_enabled_calls", false);
            edit.commit();
            if (z) {
                ConnectionsManager.setProxySettings(false, "", 0, "", "", "");
            }
        }
        proxyList.remove(proxyInfo);
        saveProxyList();
    }

    public static void checkSaveToGalleryFiles() {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Telegram");
            File file2 = new File(file, "Telegram Images");
            file2.mkdir();
            File file3 = new File(file, "Telegram Video");
            file3.mkdir();
            String str = ".nomedia";
            if (saveToGallery) {
                if (file2.isDirectory()) {
                    new File(file2, str).delete();
                }
                if (file3.isDirectory()) {
                    new File(file3, str).delete();
                    return;
                }
                return;
            }
            if (file2.isDirectory()) {
                new File(file2, str).createNewFile();
            }
            if (file3.isDirectory()) {
                new File(file3, str).createNewFile();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
