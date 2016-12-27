package org.telegram.messenger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.lang.reflect.Array;

public class StatsController {
    private static volatile StatsController Instance = null;
    private static final int TYPES_COUNT = 7;
    public static final int TYPE_AUDIOS = 3;
    public static final int TYPE_CALLS = 0;
    public static final int TYPE_FILES = 5;
    public static final int TYPE_MESSAGES = 1;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_PHOTOS = 4;
    public static final int TYPE_ROAMING = 2;
    public static final int TYPE_TOTAL = 6;
    public static final int TYPE_VIDEOS = 2;
    public static final int TYPE_WIFI = 1;
    private int[] callsTotalTime = new int[3];
    private Editor editor;
    private long[][] receivedBytes = ((long[][]) Array.newInstance(Long.TYPE, new int[]{3, 7}));
    private int[][] receivedItems = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{3, 7}));
    private long[] resetStatsDate = new long[3];
    private long[][] sentBytes = ((long[][]) Array.newInstance(Long.TYPE, new int[]{3, 7}));
    private int[][] sentItems = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{3, 7}));

    public static StatsController getInstance() {
        StatsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (StatsController.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        StatsController localInstance2 = new StatsController();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    private StatsController() {
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("stats", 0);
        boolean save = false;
        this.editor = sharedPreferences.edit();
        for (int a = 0; a < 3; a++) {
            this.callsTotalTime[a] = sharedPreferences.getInt("callsTotalTime" + a, 0);
            this.resetStatsDate[a] = sharedPreferences.getLong("resetStatsDate" + a, 0);
            for (int b = 0; b < 7; b++) {
                this.sentBytes[a][b] = sharedPreferences.getLong("sentBytes" + a + "_" + b, 0);
                this.receivedBytes[a][b] = sharedPreferences.getLong("receivedBytes" + a + "_" + b, 0);
                this.sentItems[a][b] = sharedPreferences.getInt("sentItems" + a + "_" + b, 0);
                this.receivedItems[a][b] = sharedPreferences.getInt("receivedItems" + a + "_" + b, 0);
            }
            if (this.resetStatsDate[a] == 0) {
                save = true;
                this.resetStatsDate[a] = System.currentTimeMillis();
                this.editor.putLong("resetStatsDate" + a, this.resetStatsDate[a]);
            }
        }
        if (save) {
            this.editor.commit();
        }
    }

    public void incrementReceivedItemsCount(int networkType, int dataType, int value) {
        int[] iArr = this.receivedItems[networkType];
        iArr[dataType] = iArr[dataType] + value;
        this.editor.putInt("receivedItems" + networkType + "_" + dataType, this.receivedItems[networkType][dataType]).apply();
    }

    public void incrementSentItemsCount(int networkType, int dataType, int value) {
        int[] iArr = this.sentItems[networkType];
        iArr[dataType] = iArr[dataType] + value;
        this.editor.putInt("sentItems" + networkType + "_" + dataType, this.sentItems[networkType][dataType]).apply();
    }

    public void incrementReceivedBytesCount(int networkType, int dataType, long value) {
        long[] jArr = this.receivedBytes[networkType];
        jArr[dataType] = jArr[dataType] + value;
        this.editor.putLong("receivedBytes" + networkType + "_" + dataType, this.receivedBytes[networkType][dataType]).apply();
    }

    public void incrementSentBytesCount(int networkType, int dataType, long value) {
        long[] jArr = this.sentBytes[networkType];
        jArr[dataType] = jArr[dataType] + value;
        this.editor.putLong("sentBytes" + networkType + "_" + dataType, this.sentBytes[networkType][dataType]).apply();
    }

    public void incrementTotalCallsTime(int networkType, int value) {
        int[] iArr = this.callsTotalTime;
        iArr[networkType] = iArr[networkType] + value;
        this.editor.putInt("callsTotalTime" + networkType, this.callsTotalTime[networkType]).apply();
    }

    public int getRecivedItemsCount(int networkType, int dataType) {
        return this.receivedItems[networkType][dataType];
    }

    public int getSentItemsCount(int networkType, int dataType) {
        return this.sentItems[networkType][dataType];
    }

    public long getSentBytesCount(int networkType, int dataType) {
        if (dataType == 1) {
            return (((this.sentBytes[networkType][6] - this.sentBytes[networkType][5]) - this.sentBytes[networkType][3]) - this.sentBytes[networkType][2]) - this.sentBytes[networkType][4];
        }
        return this.sentBytes[networkType][dataType];
    }

    public long getReceivedBytesCount(int networkType, int dataType) {
        if (dataType == 1) {
            return (((this.receivedBytes[networkType][6] - this.receivedBytes[networkType][5]) - this.receivedBytes[networkType][3]) - this.receivedBytes[networkType][2]) - this.receivedBytes[networkType][4];
        }
        return this.receivedBytes[networkType][dataType];
    }

    public int getCallsTotalTime(int networkType) {
        return this.callsTotalTime[networkType];
    }

    public long getResetStatsDate(int networkType) {
        return this.resetStatsDate[networkType];
    }

    public void resetStats(int networkType) {
        this.resetStatsDate[networkType] = System.currentTimeMillis();
        for (int a = 0; a < 7; a++) {
            this.sentBytes[networkType][a] = 0;
            this.receivedBytes[networkType][a] = 0;
            this.sentItems[networkType][a] = 0;
            this.receivedItems[networkType][a] = 0;
            this.editor.putInt("receivedItems" + networkType + "_" + a, this.receivedItems[networkType][a]);
            this.editor.putInt("sentItems" + networkType + "_" + a, this.sentItems[networkType][a]);
            this.editor.putLong("receivedBytes" + networkType + "_" + a, this.receivedBytes[networkType][a]);
            this.editor.putLong("sentBytes" + networkType + "_" + a, this.sentBytes[networkType][a]);
        }
        this.callsTotalTime[networkType] = 0;
        this.editor.putInt("callsTotalTime" + networkType, this.callsTotalTime[networkType]);
        this.editor.putLong("resetStatsDate" + networkType, this.resetStatsDate[networkType]).apply();
    }
}
