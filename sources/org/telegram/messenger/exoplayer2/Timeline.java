package org.telegram.messenger.exoplayer2;

import android.util.Pair;
import org.telegram.messenger.exoplayer2.source.ads.AdPlaybackState;
import org.telegram.messenger.exoplayer2.source.ads.AdPlaybackState.AdGroup;
import org.telegram.messenger.exoplayer2.util.Assertions;

public abstract class Timeline {
    public static final Timeline EMPTY = new C06051();

    public static final class Period {
        private AdPlaybackState adPlaybackState;
        public long durationUs;
        public Object id;
        private long positionInWindowUs;
        public Object uid;
        public int windowIndex;

        public Period set(Object id, Object uid, int windowIndex, long durationUs, long positionInWindowUs) {
            return set(id, uid, windowIndex, durationUs, positionInWindowUs, AdPlaybackState.NONE);
        }

        public Period set(Object id, Object uid, int windowIndex, long durationUs, long positionInWindowUs, AdPlaybackState adPlaybackState) {
            this.id = id;
            this.uid = uid;
            this.windowIndex = windowIndex;
            this.durationUs = durationUs;
            this.positionInWindowUs = positionInWindowUs;
            this.adPlaybackState = adPlaybackState;
            return this;
        }

        public long getDurationMs() {
            return C0600C.usToMs(this.durationUs);
        }

        public long getDurationUs() {
            return this.durationUs;
        }

        public long getPositionInWindowMs() {
            return C0600C.usToMs(this.positionInWindowUs);
        }

        public long getPositionInWindowUs() {
            return this.positionInWindowUs;
        }

        public int getAdGroupCount() {
            return this.adPlaybackState.adGroupCount;
        }

        public long getAdGroupTimeUs(int adGroupIndex) {
            return this.adPlaybackState.adGroupTimesUs[adGroupIndex];
        }

        public int getNextAdIndexToPlay(int adGroupIndex) {
            return this.adPlaybackState.adGroups[adGroupIndex].nextAdIndexToPlay;
        }

        public boolean hasPlayedAdGroup(int adGroupIndex) {
            AdGroup adGroup = this.adPlaybackState.adGroups[adGroupIndex];
            return adGroup.nextAdIndexToPlay == adGroup.count;
        }

        public int getAdGroupIndexForPositionUs(long positionUs) {
            long[] adGroupTimesUs = this.adPlaybackState.adGroupTimesUs;
            if (adGroupTimesUs == null) {
                return -1;
            }
            int index = adGroupTimesUs.length - 1;
            while (index >= 0 && (adGroupTimesUs[index] == Long.MIN_VALUE || adGroupTimesUs[index] > positionUs)) {
                index--;
            }
            if (index < 0 || hasPlayedAdGroup(index)) {
                index = -1;
            }
            return index;
        }

        public int getAdGroupIndexAfterPositionUs(long positionUs) {
            long[] adGroupTimesUs = this.adPlaybackState.adGroupTimesUs;
            if (adGroupTimesUs == null) {
                return -1;
            }
            int index = 0;
            while (index < adGroupTimesUs.length && adGroupTimesUs[index] != Long.MIN_VALUE && (positionUs >= adGroupTimesUs[index] || hasPlayedAdGroup(index))) {
                index++;
            }
            if (index >= adGroupTimesUs.length) {
                index = -1;
            }
            return index;
        }

        public int getAdCountInAdGroup(int adGroupIndex) {
            return this.adPlaybackState.adGroups[adGroupIndex].count;
        }

        public boolean isAdAvailable(int adGroupIndex, int adIndexInAdGroup) {
            AdGroup adGroup = this.adPlaybackState.adGroups[adGroupIndex];
            return (adGroup.count == -1 || adGroup.states[adIndexInAdGroup] == 0) ? false : true;
        }

        public long getAdDurationUs(int adGroupIndex, int adIndexInAdGroup) {
            return this.adPlaybackState.adGroups[adGroupIndex].durationsUs[adIndexInAdGroup];
        }

        public long getAdResumePositionUs() {
            return this.adPlaybackState.adResumePositionUs;
        }
    }

    public static final class Window {
        public long defaultPositionUs;
        public long durationUs;
        public int firstPeriodIndex;
        public Object id;
        public boolean isDynamic;
        public boolean isSeekable;
        public int lastPeriodIndex;
        public long positionInFirstPeriodUs;
        public long presentationStartTimeMs;
        public long windowStartTimeMs;

        public Window set(Object id, long presentationStartTimeMs, long windowStartTimeMs, boolean isSeekable, boolean isDynamic, long defaultPositionUs, long durationUs, int firstPeriodIndex, int lastPeriodIndex, long positionInFirstPeriodUs) {
            this.id = id;
            this.presentationStartTimeMs = presentationStartTimeMs;
            this.windowStartTimeMs = windowStartTimeMs;
            this.isSeekable = isSeekable;
            this.isDynamic = isDynamic;
            this.defaultPositionUs = defaultPositionUs;
            this.durationUs = durationUs;
            this.firstPeriodIndex = firstPeriodIndex;
            this.lastPeriodIndex = lastPeriodIndex;
            this.positionInFirstPeriodUs = positionInFirstPeriodUs;
            return this;
        }

        public long getDefaultPositionMs() {
            return C0600C.usToMs(this.defaultPositionUs);
        }

        public long getDefaultPositionUs() {
            return this.defaultPositionUs;
        }

        public long getDurationMs() {
            return C0600C.usToMs(this.durationUs);
        }

        public long getDurationUs() {
            return this.durationUs;
        }

        public long getPositionInFirstPeriodMs() {
            return C0600C.usToMs(this.positionInFirstPeriodUs);
        }

        public long getPositionInFirstPeriodUs() {
            return this.positionInFirstPeriodUs;
        }
    }

    /* renamed from: org.telegram.messenger.exoplayer2.Timeline$1 */
    static class C06051 extends Timeline {
        C06051() {
        }

        public int getWindowCount() {
            return 0;
        }

        public Window getWindow(int windowIndex, Window window, boolean setIds, long defaultPositionProjectionUs) {
            throw new IndexOutOfBoundsException();
        }

        public int getPeriodCount() {
            return 0;
        }

        public Period getPeriod(int periodIndex, Period period, boolean setIds) {
            throw new IndexOutOfBoundsException();
        }

        public int getIndexOfPeriod(Object uid) {
            return -1;
        }
    }

    public abstract int getIndexOfPeriod(Object obj);

    public abstract Period getPeriod(int i, Period period, boolean z);

    public abstract int getPeriodCount();

    public abstract Window getWindow(int i, Window window, boolean z, long j);

    public abstract int getWindowCount();

    public final boolean isEmpty() {
        return getWindowCount() == 0;
    }

    public int getNextWindowIndex(int windowIndex, int repeatMode, boolean shuffleModeEnabled) {
        switch (repeatMode) {
            case 0:
                return windowIndex == getLastWindowIndex(shuffleModeEnabled) ? -1 : windowIndex + 1;
            case 1:
                return windowIndex;
            case 2:
                if (windowIndex == getLastWindowIndex(shuffleModeEnabled)) {
                    return getFirstWindowIndex(shuffleModeEnabled);
                }
                return windowIndex + 1;
            default:
                throw new IllegalStateException();
        }
    }

    public int getPreviousWindowIndex(int windowIndex, int repeatMode, boolean shuffleModeEnabled) {
        switch (repeatMode) {
            case 0:
                return windowIndex == getFirstWindowIndex(shuffleModeEnabled) ? -1 : windowIndex - 1;
            case 1:
                return windowIndex;
            case 2:
                if (windowIndex == getFirstWindowIndex(shuffleModeEnabled)) {
                    return getLastWindowIndex(shuffleModeEnabled);
                }
                return windowIndex - 1;
            default:
                throw new IllegalStateException();
        }
    }

    public int getLastWindowIndex(boolean shuffleModeEnabled) {
        return isEmpty() ? -1 : getWindowCount() - 1;
    }

    public int getFirstWindowIndex(boolean shuffleModeEnabled) {
        return isEmpty() ? -1 : 0;
    }

    public final Window getWindow(int windowIndex, Window window) {
        return getWindow(windowIndex, window, false);
    }

    public final Window getWindow(int windowIndex, Window window, boolean setIds) {
        return getWindow(windowIndex, window, setIds, 0);
    }

    public final int getNextPeriodIndex(int periodIndex, Period period, Window window, int repeatMode, boolean shuffleModeEnabled) {
        int windowIndex = getPeriod(periodIndex, period).windowIndex;
        if (getWindow(windowIndex, window).lastPeriodIndex != periodIndex) {
            return periodIndex + 1;
        }
        int nextWindowIndex = getNextWindowIndex(windowIndex, repeatMode, shuffleModeEnabled);
        if (nextWindowIndex == -1) {
            return -1;
        }
        return getWindow(nextWindowIndex, window).firstPeriodIndex;
    }

    public final boolean isLastPeriod(int periodIndex, Period period, Window window, int repeatMode, boolean shuffleModeEnabled) {
        return getNextPeriodIndex(periodIndex, period, window, repeatMode, shuffleModeEnabled) == -1;
    }

    public final Pair<Integer, Long> getPeriodPosition(Window window, Period period, int windowIndex, long windowPositionUs) {
        return getPeriodPosition(window, period, windowIndex, windowPositionUs, 0);
    }

    public final Pair<Integer, Long> getPeriodPosition(Window window, Period period, int windowIndex, long windowPositionUs, long defaultPositionProjectionUs) {
        Assertions.checkIndex(windowIndex, 0, getWindowCount());
        getWindow(windowIndex, window, false, defaultPositionProjectionUs);
        if (windowPositionUs == C0600C.TIME_UNSET) {
            windowPositionUs = window.getDefaultPositionUs();
            if (windowPositionUs == C0600C.TIME_UNSET) {
                return null;
            }
        }
        int periodIndex = window.firstPeriodIndex;
        long periodPositionUs = window.getPositionInFirstPeriodUs() + windowPositionUs;
        long periodDurationUs = getPeriod(periodIndex, period).getDurationUs();
        while (periodDurationUs != C0600C.TIME_UNSET && periodPositionUs >= periodDurationUs && periodIndex < window.lastPeriodIndex) {
            periodPositionUs -= periodDurationUs;
            periodIndex++;
            periodDurationUs = getPeriod(periodIndex, period).getDurationUs();
        }
        return Pair.create(Integer.valueOf(periodIndex), Long.valueOf(periodPositionUs));
    }

    public final Period getPeriod(int periodIndex, Period period) {
        return getPeriod(periodIndex, period, false);
    }
}
